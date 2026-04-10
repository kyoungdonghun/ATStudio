package com.atstudio.atstudio.config;

import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    /**
     * Tomcat 11 defaults Connector.maxPartCount=10 (security hardening).
     * spring.servlet.multipart.max-parts only sets the servlet-level config
     * and does NOT override Tomcat's connector-level parsing limit.
     * This customizer sets it to -1 (unlimited) directly on the Connector.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatMaxPartCountCustomizer() {
        return factory -> factory.addConnectorCustomizers(
                (TomcatConnectorCustomizer) connector -> connector.setMaxPartCount(-1)
        );
    }
}
