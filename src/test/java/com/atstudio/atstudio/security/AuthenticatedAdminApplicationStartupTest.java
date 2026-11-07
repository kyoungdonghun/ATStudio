package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.ListeningProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;

import static org.assertj.core.api.Assertions.assertThat;

/** Full application graph using test H2; no listening socket, mail connection or workers. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "app.listening.enabled=true", "app.listening.operator-access=AUTHENTICATED_ADMIN", "app.listening.catalog-source=DATABASE",
        "app.background-tasks.enabled=false", "app.audio.worker-enabled=false",
        "app.storage.capture-control-enabled=false", "app.bootstrap.test-users.enabled=false",
        "spring.mail.test-connection=false", "spring.datasource.url=jdbc:h2:mem:wi019-authenticated-admin;DB_CLOSE_DELAY=-1"
})
class AuthenticatedAdminApplicationStartupTest {
    @Autowired ApplicationContext context;

    @Test void wholeApplicationStartsInListeningMode() {
        assertThat(context.getBean(ListeningProperties.class).isEnabled()).isTrue();
        assertThat(context.getBean(ListeningProperties.class).getOperatorAccess()).isEqualTo(ListeningProperties.OperatorAccess.AUTHENTICATED_ADMIN);
        assertThat(context.getBean(FilterChainProxy.class).getFilterChains()).hasSize(1);
        assertThat(context.containsBean("listeningSecurity")).isTrue();
        assertThat(context.containsBean("filterChain")).isFalse();
        assertThat(context.getBean(PasswordEncoder.class)).isNotNull();
        assertThat(context.getBean(AuthenticationManager.class)).isNotNull();
    }
}
