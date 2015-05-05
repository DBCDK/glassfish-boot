package dk.dbc.glassfishboot

import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

import java.nio.file.Path
import java.nio.file.Paths

@Component
class Glassfish implements ApplicationContextAware {
    @Autowired
    private Configuration config

    @Value("\${glassfishLocation}")
    private String location

    private Process glassfishProcess
    private ApplicationContext applicationContext

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext
        this.applicationContext.addShutdownHook {
            if (glassfishProcess.isAlive()) {
                stop()
            }
        }
    }

    Path getLocation() {
        Paths.get(location).toAbsolutePath()
    }

    Path asadminLocation() {
        Paths.get(getLocation().toString(), 'glassfish', 'bin', 'asadmin')
    }

    void start() {
        ProcessBuilder procBuilder = new ProcessBuilder(Paths.get('glassfish4', 'glassfish', 'bin', 'startserv').toString())
        glassfishProcess = procBuilder.start();
        print 'Waiting for Glassfish to start'

        int pingCount = 0;

        while (!isRunning()) {
            if (pingCount > 60) {
                System.err.println("Glassfish took too long to start, giving up.");
                System.exit(2);
            }

            print '.'

            Thread.sleep(1000);
            pingCount++;
        }

        println ' Started.'
    }

    void waitFor() {
        glassfishProcess.waitFor()
    }

    void stop() {
        glassfishProcess.destroy()
    }

    void deploy(GlassfishApp glassfishApp) {
        println "Deploying $glassfishApp"
        if (glassfishApp.hasPath()) {
            deployTo(glassfishApp.path, glassfishApp.file.absolutePath)
        } else {
            deploy(glassfishApp.file.absolutePath)
        }
    }

    void deploy(String appPath) {
        println "Deploying $appPath to default context root"
        try {
            runAsAdminCommand(['deploy', appPath])
        } catch (CommandFailedException e) {
            throw new DeploymentFailedException("Deployment failed", e)
        }
    }

    void deployTo(String contextRoot, String appPath) {
        println "Deploying $appPath to $contextRoot"
        try {
            runAsAdminCommand(['deploy', '--contextroot', contextRoot, appPath])
        } catch (CommandFailedException e) {
            throw new DeploymentFailedException("Deployment failed", e)
        }
    }

    void runCommandFile(File file) {
        println "Running multimode-file ${file.path}"
        runAsAdminCommand(['multimode', '--file', file.absolutePath])
    }

    void runAsAdminCommand(List<String> args) {
        simpleRun([asadminLocation(), '--port', config.adminPort] + args)
    }

    private void simpleRun(List<String> command) {
        def process = command.execute()
        process.consumeProcessOutputStream(System.out)
        process.consumeProcessErrorStream(System.err)
        process.waitFor()
        if (process.exitValue() != 0) {
            throw new CommandFailedException("Command failed: $command")
        }
    }

    boolean isRunning() {
        return glassfishProcess?.isAlive() && isListening(config.getHttpPort()) && isListening(config.getAdminPort());
    }

    static boolean isListening(int port) {
        return isListening('127.0.0.1', port)
    }

    static boolean isListening(String host, int port) {
        try {
            URL url = new URL("http://${host}:${port}");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(1000);
            int responseCode = conn.getResponseCode();
            conn.disconnect()
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    static String toAbsPath(String path) {
        Paths.get(System.getProperty("user.dir"), path).toString()
    }
}
