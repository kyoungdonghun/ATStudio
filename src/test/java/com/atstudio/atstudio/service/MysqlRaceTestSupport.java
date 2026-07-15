package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class MysqlRaceTestSupport {

    private static final Duration START_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration FUTURE_TIMEOUT = Duration.ofSeconds(10);

    private MysqlRaceTestSupport() {
    }

    static <T> RacePair<T> runPair(Callable<T> first, Callable<T> second) {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<RaceOutcome<T>> firstFuture = CompletableFuture.supplyAsync(
                    () -> captureAfterStart(first, ready, start),
                    executor);
            CompletableFuture<RaceOutcome<T>> secondFuture = CompletableFuture.supplyAsync(
                    () -> captureAfterStart(second, ready, start),
                    executor);

            await(ready, START_TIMEOUT, "race workers did not become ready");
            start.countDown();
            RacePair<T> pair = new RacePair<>(
                    getStrict(firstFuture, "first race future"),
                    getStrict(secondFuture, "second race future"));
            pair.outcomes().forEach(MysqlRaceTestSupport::rejectInfrastructureFailure);
            return pair;
        } finally {
            start.countDown();
            executor.shutdownNow();
            awaitTermination(executor);
        }
    }

    static <T> RaceOutcome<T> capture(Callable<T> operation) {
        try {
            return RaceOutcome.success(operation.call());
        } catch (Throwable failure) {
            return RaceOutcome.failure(failure);
        }
    }

    static <T> RaceOutcome<T> getStrict(
            CompletableFuture<RaceOutcome<T>> future,
            String label) {
        try {
            RaceOutcome<T> outcome = future.get(FUTURE_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            rejectInfrastructureFailure(outcome);
            return outcome;
        } catch (TimeoutException exception) {
            future.cancel(true);
            throw new AssertionError(label + " exceeded the strict 10 second timeout.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(label + " was interrupted.", exception);
        } catch (java.util.concurrent.ExecutionException exception) {
            throw new AssertionError(label + " failed outside the captured race contract.", exception);
        }
    }

    static void await(CountDownLatch latch, Duration timeout, String failureMessage) {
        try {
            if (!latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new AssertionError(failureMessage);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(failureMessage, exception);
        }
    }

    static BUSINESS_ERROR exactBusinessError(RaceOutcome<?> outcome) {
        rejectInfrastructureFailure(outcome);
        if (!(outcome.failure() instanceof BusinessException businessException)) {
            throw new AssertionError("Expected an exact BusinessException loser.");
        }
        return businessException.getErrorCode();
    }

    static void assertSucceeded(RaceOutcome<?> outcome) {
        rejectInfrastructureFailure(outcome);
        if (!outcome.succeeded()) {
            throw new AssertionError("Expected the race participant to succeed.");
        }
    }

    private static <T> RaceOutcome<T> captureAfterStart(
            Callable<T> operation,
            CountDownLatch ready,
            CountDownLatch start) {
        ready.countDown();
        await(start, START_TIMEOUT, "race start latch timed out");
        return capture(operation);
    }

    private static void rejectInfrastructureFailure(RaceOutcome<?> outcome) {
        Throwable failure = outcome.failure();
        if (failure == null) {
            return;
        }

        SQLException sqlFailure = findCause(failure, SQLException.class);
        if (sqlFailure != null) {
            String state = sqlFailure.getSQLState() == null ? "unknown" : sqlFailure.getSQLState();
            throw new AssertionError("SQL failure is never an accepted race loser; SQLState=" + state, failure);
        }

        String typeName = failure.getClass().getName().toLowerCase(Locale.ROOT);
        String message = failure.getMessage() == null
                ? ""
                : failure.getMessage().toLowerCase(Locale.ROOT);
        if (typeName.contains("timeout")
                || typeName.contains("deadlock")
                || typeName.contains("cannotacquirelock")
                || typeName.contains("pessimisticlocking")
                || typeName.contains("connection")
                || message.contains("lock wait timeout")
                || message.contains("deadlock")
                || message.contains("sqlstate 40001")) {
            throw new AssertionError(
                    "Lock, timeout, deadlock, or connection failure is never an accepted race loser.",
                    failure);
        }

        if (!(failure instanceof BusinessException)) {
            throw new AssertionError(
                    "Arbitrary exception is never an accepted race loser: "
                            + failure.getClass().getSimpleName(),
                    failure);
        }
    }

    private static <T extends Throwable> T findCause(Throwable failure, Class<T> type) {
        Throwable current = failure;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private static void awaitTermination(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(START_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                throw new AssertionError("Race executor did not terminate within five seconds.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while stopping the race executor.", exception);
        }
    }

    record RacePair<T>(RaceOutcome<T> first, RaceOutcome<T> second) {

        List<RaceOutcome<T>> outcomes() {
            return List.of(first, second);
        }
    }

    record RaceOutcome<T>(T value, Throwable failure) {

        static <T> RaceOutcome<T> success(T value) {
            return new RaceOutcome<>(value, null);
        }

        static <T> RaceOutcome<T> failure(Throwable failure) {
            return new RaceOutcome<>(null, failure);
        }

        boolean succeeded() {
            return failure == null;
        }
    }
}
