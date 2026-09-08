package com.atstudio.atstudio.config;

import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    static final int MAX_MULTIPART_PART_COUNT = 128;

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    /**
     * Track forms send each tagIds value as a separate part. The largest fixed
     * form is a Track edit: six scalar fields and two files, leaving room for
     * 120 tag parts. Notice replacement needs 13 parts; certification needs 10.
     * Bound connector parsing before service validation without reducing the
     * existing byte, parameter, or part-header limits.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatMaxPartCountCustomizer() {
        return factory -> factory.addConnectorCustomizers(
                (TomcatConnectorCustomizer) connector -> connector.setMaxPartCount(MAX_MULTIPART_PART_COUNT)
        );
    }
}
