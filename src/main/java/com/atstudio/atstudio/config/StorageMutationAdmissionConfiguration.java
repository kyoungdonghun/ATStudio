package com.atstudio.atstudio.config;

import com.atstudio.atstudio.security.StorageMutationAdmissionFilter;
import com.atstudio.atstudio.security.TrustedClientIdentityResolver;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(StorageMutationAdmissionProperties.class)
public class StorageMutationAdmissionConfiguration {

    @Bean
    public StrictHttpFirewall storageAdmissionFirewall() {
        return new StrictHttpFirewall();
    }

    @Bean
    public WebSecurityCustomizer storageAdmissionWebSecurity(StrictHttpFirewall storageAdmissionFirewall) {
        return web -> web.httpFirewall(storageAdmissionFirewall);
    }

    @Bean
    public FilterRegistrationBean<StorageMutationAdmissionFilter> storageMutationAdmissionRegistration(
            StorageMutationAdmissionProperties properties, TrustedClientIdentityResolver identityResolver,
            StrictHttpFirewall storageAdmissionFirewall) {
        var registration = new FilterRegistrationBean<>(
                new StorageMutationAdmissionFilter(properties, identityResolver, storageAdmissionFirewall));
        // Before FormContentFilter (-9900), security (-100), and auto-registered component filters.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setAsyncSupported(true);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
