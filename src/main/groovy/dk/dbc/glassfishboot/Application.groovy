package dk.dbc.glassfishboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .showBanner(false)
                .sources(Application.class)
                .run(args)
    }
}
