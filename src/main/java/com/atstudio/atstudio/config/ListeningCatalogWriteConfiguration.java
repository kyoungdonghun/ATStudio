package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.ListeningCatalogService;
import com.atstudio.atstudio.service.storage.StorageReferenceMutation;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;

/** Shared lock order: existing storage admission -> transaction -> catalog -> ascending track locks. */
@Configuration(proxyBeanMethods = false)
public class ListeningCatalogWriteConfiguration {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    DefaultPointcutAdvisor listeningPreparationEntryAdvisor(ObjectProvider<ListeningCatalogService> catalogs) {
        var pointcut = new StaticMethodMatcherPointcut() {
            @Override public boolean matches(Method method, Class<?> type) {
                return AnnotatedElementUtils.hasAnnotation(AopUtils.getMostSpecificMethod(method, type),
                        com.atstudio.atstudio.service.storage.StorageReferencePreparation.class);
            }
        };
        MethodInterceptor interceptor = invocation -> {
            if (catalogs.getObject().databaseMode()
                    && org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new IllegalStateException("Catalog preparation must precede transaction creation");
            }
            return invocation.proceed();
        };
        var advisor = new DefaultPointcutAdvisor(pointcut, interceptor);
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    DefaultPointcutAdvisor listeningCatalogWriteAdvisor(ObjectProvider<ListeningCatalogService> catalogs,
            ObjectProvider<PlatformTransactionManager> managers) {
        var pointcut = new StaticMethodMatcherPointcut() {
            @Override public boolean matches(Method method, Class<?> type) {
                return AnnotatedElementUtils.hasAnnotation(AopUtils.getMostSpecificMethod(method, type), StorageReferenceMutation.class);
            }
        };
        ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);
        MethodInterceptor interceptor = invocation -> {
            var catalog = catalogs.getObject();
            if (!catalog.databaseMode()) return invocation.proceed();
            if (depth.get() == 0 && org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new IllegalStateException("Catalog admission must precede transaction creation");
            }
            var tx = new TransactionTemplate(managers.getObject());
            depth.set(depth.get() + 1);
            try {
            return tx.execute(status -> {
                catalog.lockForWrite();
                try { return invocation.proceed(); }
                catch (RuntimeException | Error error) { throw error; }
                catch (Throwable error) { throw new IllegalStateException(error); }
            });
            } finally {
                if (depth.get() == 1) depth.remove(); else depth.set(depth.get() - 1);
            }
        };
        var advisor = new DefaultPointcutAdvisor(pointcut, interceptor);
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        return advisor;
    }
}
