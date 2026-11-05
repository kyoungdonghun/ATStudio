package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.storage.StorageContinuityGuard;
import com.atstudio.atstudio.service.storage.StorageReferenceMutation;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

@Configuration(proxyBeanMethods = false)
public class StorageReferenceAdmissionConfiguration {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public DefaultPointcutAdvisor storageReferenceAdmissionAdvisor(StorageContinuityGuard guard) {
        var pointcut = new StaticMethodMatcherPointcut() {
            @Override public boolean matches(Method method, Class<?> targetClass) {
                return AnnotatedElementUtils.hasAnnotation(AopUtils.getMostSpecificMethod(method, targetClass),
                        StorageReferenceMutation.class)
                        || AnnotatedElementUtils.hasAnnotation(AopUtils.getMostSpecificMethod(method, targetClass),
                        com.atstudio.atstudio.service.storage.StorageReferencePreparation.class);
            }
        };
        MethodInterceptor interceptor = invocation -> {
            if (!guard.isConfigured()) return invocation.proceed();
            // A previously unadmitted outer transaction could already hold locks/mutate refs.
            // Fail closed rather than pretending a late admission protects its whole lifetime.
            if (TransactionSynchronizationManager.isActualTransactionActive() && !guard.currentThreadAdmitted()) {
                throw new IllegalStateException("Storage reference admission must precede transaction creation");
            }
            try (var ignored = guard.enter()) { return invocation.proceed(); }
        };
        var advisor = new DefaultPointcutAdvisor(pointcut, interceptor);
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
        return advisor;
    }
}
