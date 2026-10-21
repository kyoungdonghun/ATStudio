package com.atstudio.atstudio.config;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.context.annotation.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class StorageMutationAdmissionBindingTest {

    @ParameterizedTest
    @ValueSource(strings = {"max-concurrent", "max-concurrent-per-ip", "retry-after-seconds"})
    void invalidLimitsFailBindingInsteadOfSilentlyDisablingProtection(String property) {
        new ApplicationContextRunner()
                .withInitializer(context -> {
                    context.getEnvironment().getPropertySources()
                            .remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                    context.getEnvironment().getPropertySources()
                            .remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
                })
                .withUserConfiguration(OnlyAdmissionProperties.class)
                .withPropertyValues("app.storage.mutation-admission." + property + "=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(StorageMutationAdmissionProperties.class)
    static class OnlyAdmissionProperties { }

    @Test
    void shippedDefaultsBindToActualAdmissionMailMultipartAndPoolProperties() throws IOException {
        Binder binder = binder(Map.of());
        var admission = binder.bind("app.storage.mutation-admission", StorageMutationAdmissionProperties.class).get();
        assertThat(admission.isEnabled()).isTrue();
        assertThat(admission.getMaxConcurrent()).isEqualTo(2);
        assertThat(admission.getMaxConcurrentPerIp()).isEqualTo(1);
        assertThat(admission.getRetryAfterSeconds()).isEqualTo(2);
        assertMail(binder, "5000", "10000", "10000");
        var multipart = binder.bind("spring.servlet.multipart", MultipartProperties.class).get();
        assertThat(multipart.getFileSizeThreshold().toBytes()).isZero();
        assertThat(multipart.getMaxFileSize().toMegabytes()).isEqualTo(100);
        assertThat(multipart.getMaxRequestSize().toMegabytes()).isEqualTo(120);
        var hikari = binder.bind("spring.datasource.hikari", HikariConfig.class).get();
        assertThat(hikari.getMaximumPoolSize()).isEqualTo(10);
        assertThat(hikari.getMinimumIdle()).isEqualTo(2);
        assertThat(hikari.getConnectionTimeout()).isEqualTo(5000);
    }

    @Test
    void exactSharedEnvironmentNamesOverrideDefaultsWithoutLoadingExternalSettings() throws IOException {
        Binder binder = binder(Map.ofEntries(
                Map.entry("APP_STORAGE_MUTATION_ADMISSION_ENABLED", "false"),
                Map.entry("APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT", "3"),
                Map.entry("APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT_PER_IP", "2"),
                Map.entry("APP_STORAGE_MUTATION_ADMISSION_RETRY_AFTER_SECONDS", "7"),
                Map.entry("MAIL_SMTP_CONNECTION_TIMEOUT", "1200"),
                Map.entry("MAIL_SMTP_TIMEOUT", "2300"),
                Map.entry("MAIL_SMTP_WRITE_TIMEOUT", "3400"),
                Map.entry("SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE", "7"),
                Map.entry("SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE", "1"),
                Map.entry("SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT", "2500"),
                Map.entry("SPRING_SERVLET_MULTIPART_FILE_SIZE_THRESHOLD", "1KB")));
        var admission = binder.bind("app.storage.mutation-admission", StorageMutationAdmissionProperties.class).get();
        assertThat(admission.isEnabled()).isFalse();
        assertThat(admission.getMaxConcurrent()).isEqualTo(3);
        assertThat(admission.getMaxConcurrentPerIp()).isEqualTo(2);
        assertThat(admission.getRetryAfterSeconds()).isEqualTo(7);
        assertMail(binder, "1200", "2300", "3400");
        var hikari = binder.bind("spring.datasource.hikari", HikariConfig.class).get();
        assertThat(hikari.getMaximumPoolSize()).isEqualTo(7);
        assertThat(hikari.getMinimumIdle()).isEqualTo(1);
        assertThat(hikari.getConnectionTimeout()).isEqualTo(2500);
        assertThat(binder.bind("spring.servlet.multipart", MultipartProperties.class).get()
                .getFileSizeThreshold().toBytes()).isEqualTo(1024);
    }

    private void assertMail(Binder binder, String connect, String read, String write) {
        var mail = binder.bind("spring.mail", MailProperties.class).get();
        Properties properties = new Properties();
        properties.putAll(mail.getProperties());
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setJavaMailProperties(properties);
        // Session creation is local: no connect, testConnection, or send is called.
        assertThat(sender.getSession().getProperty("mail.smtp.connectiontimeout")).isEqualTo(connect);
        assertThat(sender.getSession().getProperty("mail.smtp.timeout")).isEqualTo(read);
        assertThat(sender.getSession().getProperty("mail.smtp.writetimeout")).isEqualTo(write);
    }

    private Binder binder(Map<String, Object> overrides) throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().addFirst(new MapPropertySource("synthetic", overrides));
        environment.getPropertySources().addLast(new YamlPropertySourceLoader()
                .load("shipped", new FileSystemResource("src/main/resources/application.yml")).get(0));
        return Binder.get(environment);
    }
}
