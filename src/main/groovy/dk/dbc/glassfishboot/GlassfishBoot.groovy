package dk.dbc.glassfishboot

import groovy.xml.XmlUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


@Component
class GlassfishBoot implements CommandLineRunner {

    @Autowired
    private Configuration config

    @Autowired
    private Glassfish glassfish

    @Override
    void run(String... args) throws Exception {
        println glassfish.location
        println config
        println config.apps

        def domainDir = Paths.get(glassfish.location.toString(), 'glassfish', 'domains', 'domain1').toString()
        //def loggingProperties = toAbsPath('glassfish4/glassfish/domains/domain1/config/logging.properties')

        def domainXmlPath = Paths.get(domainDir, 'config', 'domain.xml').toString()
        def libExt = Paths.get(domainDir, 'lib', 'ext')

        new File(System.properties['user.dir']).eachFile { f ->
            if (f.path.endsWith('.jar')) {
                println "Found jar $f, copying it to $libExt"
                Files.copy(Paths.get(f.path), libExt, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        def xml = new XmlParser().parse(domainXmlPath)
        def serverConfig = xml.configs.config.find {it.@name == 'server-config'}
        //def defaultConfig = xml.configs.config.find {it.@name == 'default-config'}
        def networkListeners = serverConfig."network-config"."network-listeners"."network-listener"
        networkListeners.find {it.@name == 'http-listener-1'}.@port = config.getHttpPort()
        networkListeners.find {it.@name == 'http-listener-2'}.@port = config.getHttpsPort()
        networkListeners.find {it.@name == 'admin-listener'}.@port = config.getAdminPort()
        serverConfig.'system-property'.find {it.@name = 'JMS_PROVIDER_PORT'}.@value = config.getJmsPort()
        serverConfig.'admin-service'.'jmx-connector'.@port = config.getJmxPort()

        def threadPools = serverConfig.'thread-pools'.'thread-pool'
        threadPools.find {it.@name == 'http-thread-pool'}.'@min-thread-pool-size' = config.getMinThreads()
        threadPools.find {it.@name == 'http-thread-pool'}.'@max-thread-pool-size' = config.getMaxThreads()

        if (serverConfig.'iiop-service') {
            serverConfig.remove(serverConfig.'iiop-service')
        }
        //serverConfig.remove(serverConfig.'admin-service')
        //serverConfig.remove(serverConfig.'jms-service')


        def domainXmlOutputSteam = new FileOutputStream(domainXmlPath)
        XmlUtil.serialize(xml, domainXmlOutputSteam)
        domainXmlOutputSteam.close()

        try {
            glassfish.start()

            config.commandFiles.each {
                glassfish.runCommandFile(it)
            }

            config.apps.each { app ->
                glassfish.deploy(app)
            }

            glassfish.waitFor()
        } finally {
            glassfish.stop()
        }
    }
}
