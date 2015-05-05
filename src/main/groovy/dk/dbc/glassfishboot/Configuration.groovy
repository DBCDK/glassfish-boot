package dk.dbc.glassfishboot

import groovy.transform.ToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * Created by atu on 03/05-15.
 */
@Component
@ConfigurationProperties
@ToString
class Configuration {
    @Min(1L)
    @Max(65535L)
    @NotNull
    private Integer httpPort;

    @Min(1L)
    @Max(65535L)
    @NotNull
    private Integer httpsPort;

    @Min(1L)
    @Max(65535L)
    @NotNull
    private Integer adminPort;

    @Min(1L)
    @Max(65535L)
    @NotNull
    private Integer jmxPort;

    @Min(1L)
    @Max(65535L)
    @NotNull
    private Integer jmsPort;

    private List<GlassfishApp> apps = new ArrayList<>();

    private Integer minThreads;
    private Integer maxThreads = 200;

    private List<File> commandFiles = new ArrayList<>();

    Integer getHttpPort() {
        return httpPort
    }

    void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort
    }

    Integer getHttpsPort() {
        return httpsPort
    }

    void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort
    }

    Integer getAdminPort() {
        return adminPort
    }

    void setAdminPort(Integer adminPort) {
        this.adminPort = adminPort
    }

    Integer getJmxPort() {
        return jmxPort
    }

    void setJmxPort(Integer jmxPort) {
        this.jmxPort = jmxPort
    }

    Integer getJmsPort() {
        return jmsPort
    }

    void setJmsPort(Integer jmsPort) {
        this.jmsPort = jmsPort
    }

    void setApp(List<String> app) {
        app.each {
            apps.add(new GlassfishApp(file: new File(it)))
        }
    }

    void setMappedApp(List<String> app) {
        app.each {
            int splitPoint = it.indexOf(':')
            if (splitPoint < 0) {
                System.err.println("Invalid mapped app: ${app}\nMust be of the form /path:file.war")
                System.exit(1)
            }

            apps.add(new GlassfishApp(
                    path: it.substring(0, splitPoint),
                    file: new File(it.substring(splitPoint + 1))))
        }
    }

    List<GlassfishApp> getApps() {
        return apps
    }

    Integer getMinThreads() {
        if (minThreads == null) {
            return maxThreads/2
        } else {
            return minThreads
        }
    }

    void setMinThreads(int minThreads) {
        this.minThreads = minThreads
    }

    int getMaxThreads() {
        return maxThreads
    }

    void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads
    }

    List<File> getCommandFiles() {
        return commandFiles
    }

    void setCommandFile(List<File> commandFiles) {
        this.commandFiles = commandFiles
    }
}
