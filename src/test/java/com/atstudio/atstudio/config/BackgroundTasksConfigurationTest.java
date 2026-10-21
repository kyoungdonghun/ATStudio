package com.atstudio.atstudio.config;

import com.atstudio.atstudio.bootstrap.TestUserBootstrapProperties;
import com.atstudio.atstudio.dto.storage.StorageIntegrityReportResponse;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.*;
import com.atstudio.atstudio.service.audio.*;
import com.atstudio.atstudio.service.storage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BackgroundTasksConfigurationTest {
    private final ApplicationContextRunner runner = isolatedRunner()
            .withUserConfiguration(BackgroundTasksConfiguration.class, StorageMutationRecoveryService.class,
                    TrackAudioProcessingWorker.class)
            .withBean(TaskScheduler.class, () -> mock(TaskScheduler.class))
            .withBean(StorageMutationJournalService.class, () -> mock(StorageMutationJournalService.class))
            .withBean(StorageCleanupService.class, () -> mock(StorageCleanupService.class))
            .withBean(StorageReferenceChecker.class, () -> mock(StorageReferenceChecker.class))
            .withBean(TrackAudioProcessingTransactions.class, () -> mock(TrackAudioProcessingTransactions.class))
            .withBean(StorageService.class, () -> mock(StorageService.class))
            .withBean(StorageMutationCoordinator.class, () -> mock(StorageMutationCoordinator.class))
            .withBean(AudioAnalysisService.class, () -> mock(AudioAnalysisService.class))
            .withBean(FfmpegAudioEncoder.class, () -> mock(FfmpegAudioEncoder.class))
            .withBean(RecurringRenewalService.class, () -> mock(RecurringRenewalService.class))
            .withBean(UserSubscriptionRepository.class, () -> mock(UserSubscriptionRepository.class))
            .withBean(PaymentOrderRepository.class, () -> mock(PaymentOrderRepository.class))
            .withBean(PaymentProperties.class, PaymentProperties::new)
            .withBean(PaymentReconciliationTransactionService.class,
                    () -> mock(PaymentReconciliationTransactionService.class))
            .withBean(PaymentReconciliationIncidentService.class,
                    () -> mock(PaymentReconciliationIncidentService.class))
            .withBean(PaymentCommandTransactionService.class, () -> mock(PaymentCommandTransactionService.class))
            .withBean(WithdrawalBillingCleanupService.class, () -> mock(WithdrawalBillingCleanupService.class))
            .withUserConfiguration(SubscriptionScheduler.class, PaymentReconciliationService.class,
                    WithdrawalBillingCleanupCoordinator.class);

    @ParameterizedTest
    @MethodSource("backgroundModes")
    void backgroundModeTruthTable(String master, String audioOnly, String worker) {
        ApplicationContextRunner configured = runner;
        if (!master.equals("MISSING")) {
            configured = configured.withPropertyValues("app.background-tasks.enabled=" + master);
        }
        if (!audioOnly.equals("MISSING")) {
            configured = configured.withPropertyValues("app.background-tasks.audio-only=" + audioOnly);
        }
        if (!worker.equals("MISSING")) {
            configured = configured.withPropertyValues("app.audio.worker-enabled=" + worker);
        }
        boolean scheduled = !master.equals("false") && !audioOnly.equals("true");
        boolean audio = !master.equals("false") && !worker.equals("false");
        configured.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBeansOfType(ScheduledAnnotationBeanPostProcessor.class))
                    .hasSize(scheduled ? 1 : 0);
            assertThat(context.getBeansOfType(TrackAudioProcessingWorker.class)).hasSize(audio ? 1 : 0);
            assertThat(context).hasSingleBean(SubscriptionScheduler.class);
            assertThat(context).hasSingleBean(PaymentReconciliationService.class);
            assertThat(context).hasSingleBean(WithdrawalBillingCleanupCoordinator.class);
            assertThat(context).hasSingleBean(StorageMutationRecoveryService.class);
            assertThat(context).doesNotHaveBean(java.util.concurrent.ScheduledExecutorService.class);
            if (scheduled) {
                assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks())
                        .hasSize(6);
            } else {
                verifyNoInteractions(context.getBean(TaskScheduler.class));
            }

            context.publishEvent(new ApplicationReadyEvent(new SpringApplication(Object.class),
                    new String[0], context.getSourceApplicationContext(), Duration.ZERO));
            if (scheduled) {
                verify(context.getBean(StorageMutationJournalService.class))
                        .claimBatch(any(), any(), any(), eq(50), eq(8));
            } else {
                verifyNoInteractions(context.getBean(StorageMutationJournalService.class));
            }
            if (audio) {
                verify(context.getBean(TrackAudioProcessingTransactions.class)).recoverInterruptedBatch();
                verify(context.getBean(TrackAudioProcessingTransactions.class), timeout(2000)).claimNext();
            } else {
                verifyNoInteractions(context.getBean(TrackAudioProcessingTransactions.class));
            }
            verifyNoInteractions(context.getBean(StorageCleanupService.class),
                    context.getBean(StorageReferenceChecker.class), context.getBean(FfmpegAudioEncoder.class),
                    context.getBean(StorageService.class), context.getBean(StorageMutationCoordinator.class),
                    context.getBean(AudioAnalysisService.class), context.getBean(RecurringRenewalService.class),
                    context.getBean(PaymentReconciliationTransactionService.class),
                    context.getBean(PaymentReconciliationIncidentService.class),
                    context.getBean(PaymentCommandTransactionService.class),
                    context.getBean(WithdrawalBillingCleanupService.class));

            if (!scheduled) {
                context.getBean(StorageMutationRecoveryService.class).recoverBatch();
                verify(context.getBean(StorageMutationJournalService.class))
                        .claimBatch(any(), any(), any(), eq(50), eq(8));
                context.getBean(SubscriptionScheduler.class).processRecurringRenewals();
                verify(context.getBean(RecurringRenewalService.class)).processDueRenewals();
            }
        });
    }

    private static Stream<Arguments> backgroundModes() {
        return Stream.of("true", "false", "MISSING").flatMap(master ->
                Stream.of("true", "false", "MISSING").flatMap(audioOnly ->
                        Stream.of("true", "false", "MISSING")
                                .map(worker -> Arguments.of(master, audioOnly, worker))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "invalid", "yes", "0", " true ", "${UNRESOLVED_AUDIO_ONLY_SWITCH}"})
    void malformedAudioOnlyFailsBeforeDependenciesEvenWhenMasterOrWorkerIsOff(String value) {
        for (String master : List.of("true", "false", "MISSING")) {
            Runnable created = mock(Runnable.class);
            ApplicationContextRunner configured = isolatedRunner()
                    .withUserConfiguration(BackgroundTasksConfiguration.class, TrackAudioProcessingWorker.class)
                    .withBean("sideEffectSentinel", Object.class, () -> {
                        created.run();
                        return new Object();
                    })
                    .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                            new org.springframework.core.env.MapPropertySource("audio-only-test",
                                    java.util.Map.of("app.background-tasks.audio-only", value))))
                    .withPropertyValues("app.audio.worker-enabled=false");
            if (!master.equals("MISSING")) {
                configured = configured.withPropertyValues("app.background-tasks.enabled=" + master);
            }
            configured.run(context -> {
                assertThat(context).hasFailed();
                verifyNoInteractions(created);
            });
        }
    }

    @Test
    void strictFlagsRemainCaseInsensitiveWithoutAcceptingWhitespace() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.background-tasks.enabled", "TrUe")
                .withProperty("app.background-tasks.audio-only", "TrUe");
        assertThat(BackgroundTasksEnabledCondition.isEnabled(environment)).isTrue();
        assertThat(BackgroundTasksEnabledCondition.isScheduledEnabled(environment)).isFalse();
        environment.setProperty("app.background-tasks.audio-only", "FaLsE");
        assertThat(BackgroundTasksEnabledCondition.isScheduledEnabled(environment)).isTrue();
        environment.setProperty("app.background-tasks.enabled", "FaLsE");
        assertThat(BackgroundTasksEnabledCondition.isEnabled(environment)).isFalse();
        environment.setProperty("app.background-tasks.audio-only", " true ");
        assertThatThrownBy(() -> BackgroundTasksEnabledCondition.isEnabled(environment))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("audio-only must be true or false");
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "MISSING"})
    void offSuppressesAllSchedulesAndReadyWorkButRetainsManualServices(String legacy) {
        ApplicationContextRunner configured = legacy.equals("MISSING") ? runner
                : runner.withPropertyValues("app.audio.worker-enabled=" + legacy);
        configured.withPropertyValues("app.background-tasks.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
                    assertThat(context).doesNotHaveBean(TrackAudioProcessingWorker.class);
                    assertThat(context).hasSingleBean(SubscriptionScheduler.class);
                    assertThat(context).hasSingleBean(PaymentReconciliationService.class);
                    assertThat(context).hasSingleBean(WithdrawalBillingCleanupCoordinator.class);
                    context.publishEvent(new ApplicationReadyEvent(new SpringApplication(Object.class),
                            new String[0], context.getSourceApplicationContext(), Duration.ZERO));
                    verifyNoInteractions(context.getBean(TaskScheduler.class),
                            context.getBean(StorageMutationJournalService.class),
                            context.getBean(StorageCleanupService.class),
                            context.getBean(StorageReferenceChecker.class),
                            context.getBean(TrackAudioProcessingTransactions.class),
                            context.getBean(FfmpegAudioEncoder.class),
                            context.getBean(AudioAnalysisService.class),
                            context.getBean(StorageService.class),
                            context.getBean(StorageMutationCoordinator.class),
                            context.getBean(RecurringRenewalService.class),
                            context.getBean(PaymentReconciliationTransactionService.class),
                            context.getBean(WithdrawalBillingCleanupService.class));

                    context.getBean(StorageMutationRecoveryService.class).recoverBatch();
                    verify(context.getBean(StorageMutationJournalService.class))
                            .claimBatch(any(), any(), any(), eq(50), eq(8));
                    context.getBean(SubscriptionScheduler.class).processRecurringRenewals();
                    verify(context.getBean(RecurringRenewalService.class)).processDueRenewals();
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "MISSING"})
    void enabledAndMissingPreserveSixSchedulesAndReadyWork(String value) {
        ApplicationContextRunner configured = value.equals("MISSING") ? runner
                : runner.withPropertyValues("app.background-tasks.enabled=" + value);
        configured.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TrackAudioProcessingWorker.class);
            assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).hasSize(6);
            verifyNoInteractions(context.getBean(TrackAudioProcessingTransactions.class));
            context.publishEvent(new ApplicationReadyEvent(new SpringApplication(Object.class),
                    new String[0], context.getSourceApplicationContext(), Duration.ZERO));
            verify(context.getBean(StorageMutationJournalService.class))
                    .claimBatch(any(), any(), any(), eq(50), eq(8));
            verify(context.getBean(TrackAudioProcessingTransactions.class)).recoverInterruptedBatch();
            verify(context.getBean(TrackAudioProcessingTransactions.class), timeout(2000)).claimNext();
            verifyNoInteractions(context.getBean(FfmpegAudioEncoder.class), context.getBean(StorageService.class));
        });
    }

    @Test
    void legacyAudioDisableStillWinsWhenMasterEnabled() {
        runner.withPropertyValues("app.background-tasks.enabled=true", "app.audio.worker-enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(TrackAudioProcessingWorker.class);
                    assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks())
                            .hasSize(6);
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "invalid", "yes", "0", "${UNRESOLVED_BACKGROUND_SWITCH}"})
    void malformedMasterFailsBeforeDependenciesAreCreated(String value) {
        Runnable created = mock(Runnable.class);
        isolatedRunner().withUserConfiguration(BackgroundTasksConfiguration.class)
                .withBean("sideEffectSentinel", Object.class, () -> {
                    created.run();
                    return new Object();
                })
                .withPropertyValues("app.background-tasks.enabled=" + value)
                .run(context -> {
                    assertThat(context).hasFailed();
                    verifyNoInteractions(created);
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void accidentalEnableSchedulingFailsClosedBeforeAnyJobRegistration(boolean audioOnly) {
        TaskScheduler scheduler = mock(TaskScheduler.class);
        isolatedRunner()
                .withUserConfiguration(BackgroundTasksConfiguration.class, AccidentalScheduling.class)
                .withBean(TaskScheduler.class, () -> scheduler)
                .withPropertyValues("app.background-tasks.enabled=" + audioOnly,
                        "app.background-tasks.audio-only=" + audioOnly)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasMessageContaining("background tasks are disabled");
                    verifyNoInteractions(scheduler);
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void accidentalCustomScheduledProcessorAlsoFailsClosed(boolean audioOnly) {
        isolatedRunner().withUserConfiguration(BackgroundTasksConfiguration.class)
                .withBean("customSchedulerProcessor", ScheduledAnnotationBeanPostProcessor.class,
                        ScheduledAnnotationBeanPostProcessor::new)
                .withPropertyValues("app.background-tasks.enabled=" + audioOnly,
                        "app.background-tasks.audio-only=" + audioOnly)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void productionHasOnlyTheConditionalSchedulingOptIn() throws Exception {
        try (var paths = java.nio.file.Files.walk(java.nio.file.Path.of("src/main/java"))) {
            List<String> schedulingSources = paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> {
                        try {
                            return java.nio.file.Files.readString(path).contains("@EnableScheduling");
                        } catch (java.io.IOException exception) {
                            throw new java.io.UncheckedIOException(exception);
                        }
                    }).map(path -> path.getFileName().toString()).toList();
            assertThat(schedulingSources).containsExactly("BackgroundTasksConfiguration.java");
        }
    }

    @Test
    void yamlMapsExplicitEnvironmentSwitchAndPreservesSeparateBootstrapControls() throws Exception {
        var properties = new YamlPropertySourceLoader().load("application",
                new FileSystemResource("src/main/resources/application.yml")).get(0);
        assertThat(properties.getProperty("app.background-tasks.enabled"))
                .isEqualTo("${APP_BACKGROUND_TASKS_ENABLED:true}");
        assertThat(properties.getProperty("app.background-tasks.audio-only"))
                .isEqualTo("${APP_BACKGROUND_TASKS_AUDIO_ONLY:false}");
        assertThat(properties.getProperty("app.bootstrap.test-users.enabled"))
                .isEqualTo("${APP_BOOTSTRAP_TEST_USERS_ENABLED:false}");
        assertThat(properties.getProperty("app.acceptance.enabled"))
                .isEqualTo("${APP_ACCEPTANCE_ENABLED:false}");
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true", "MISSING"})
    void publicYamlResolvesSyntheticEnvironmentOverride(String value) throws Exception {
        var properties = new YamlPropertySourceLoader().load("application",
                new FileSystemResource("src/main/resources/application.yml")).get(0);
        ApplicationContextRunner configured = runner.withInitializer(context ->
                context.getEnvironment().getPropertySources().addLast(properties));
        if (!value.equals("MISSING")) {
            configured = configured.withPropertyValues("APP_BACKGROUND_TASKS_ENABLED=" + value);
        }
        configured.run(context -> {
            assertThat(context).hasNotFailed();
            if (value.equals("false")) {
                assertThat(context).doesNotHaveBean(TrackAudioProcessingWorker.class);
                assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
                context.publishEvent(new ApplicationReadyEvent(new SpringApplication(Object.class),
                        new String[0], context.getSourceApplicationContext(), Duration.ZERO));
                verifyNoInteractions(context.getBean(StorageMutationJournalService.class),
                        context.getBean(TrackAudioProcessingTransactions.class));
            } else {
                assertThat(context).hasSingleBean(TrackAudioProcessingWorker.class);
                assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).hasSize(6);
            }
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void suppressionDoesNotBypassStrictIntegrityAudit(boolean audioOnly) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.background-tasks.enabled", Boolean.toString(audioOnly))
                .withProperty("app.background-tasks.audio-only", Boolean.toString(audioOnly));
        environment.setActiveProfiles("production");
        StorageIntegrityProperties properties = new StorageIntegrityProperties();
        properties.setAuditOnStartup(true);
        properties.setStrictOnStartup(true);
        StorageIntegrityService integrity = mock(StorageIntegrityService.class);
        when(integrity.inspect()).thenReturn(new StorageIntegrityReportResponse(
                Instant.EPOCH, 1, 0, 1, false, List.of()));
        StorageIntegrityStartupGuard guard = new StorageIntegrityStartupGuard(properties, integrity, environment);
        assertThatThrownBy(() -> guard.run(null)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missingReferences=1");
        verify(integrity).inspect();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void suppressionDoesNotBypassMandatoryBillingKeyValidation(boolean audioOnly) {
        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                new MockEnvironment().withProperty("app.background-tasks.enabled", Boolean.toString(audioOnly))
                        .withProperty("app.background-tasks.audio-only", Boolean.toString(audioOnly)),
                new AcceptanceProperties(), new TestUserBootstrapProperties(), new PaymentProperties());
        assertThatThrownBy(guard::validate).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAYMENT_BILLING_KEY_ACTIVE_KEY_ID");
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true", "MISSING"})
    void publicYamlResolvesAudioOnlyEnvironmentOverride(String value) throws Exception {
        var properties = new YamlPropertySourceLoader().load("application",
                new FileSystemResource("src/main/resources/application.yml")).get(0);
        ApplicationContextRunner configured = runner.withInitializer(context ->
                context.getEnvironment().getPropertySources().addLast(properties));
        if (!value.equals("MISSING")) {
            configured = configured.withPropertyValues("APP_BACKGROUND_TASKS_AUDIO_ONLY=" + value);
        }
        configured.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TrackAudioProcessingWorker.class);
            if (value.equals("true")) {
                assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
            } else {
                assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).hasSize(6);
            }
        });
    }

    private static ApplicationContextRunner isolatedRunner() {
        return new ApplicationContextRunner().withInitializer(context -> {
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    static class AccidentalScheduling {
    }
}
