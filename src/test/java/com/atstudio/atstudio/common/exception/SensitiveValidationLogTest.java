package com.atstudio.atstudio.common.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SensitiveValidationLogTest {
    private static final String PASSWORD = "SENTINEL_PASSWORD_8b52";
    private static final String EMAIL = "SENTINEL_EMAIL_8b52@example.test";
    private static final String PHONE = "SENTINEL_PHONE_01012345678";
    private static final String TOKEN = "SENTINEL_TOKEN_8b52";
    private static final String CAUSE = "SENTINEL_CAUSE_8b52";
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ListAppender<ILoggingEvent> events = new ListAppender<>();
    private Level previousLevel;

    @BeforeEach
    void capture() {
        previousLevel = logger.getLevel();
        logger.setLevel(Level.WARN);
        events.setContext(logger.getLoggerContext());
        events.start();
        logger.addAppender(events);
    }

    @AfterEach
    void release() {
        logger.detachAppender(events);
        logger.setLevel(previousLevel);
        events.stop();
    }

    @Test
    void rejectedFieldsKeepOnlyBoundedAllowlistedFieldNamesAndCategory() throws Exception {
        MethodArgumentNotValidException exception = rejectedFields();
        assertThat(exception.toString()).contains(PASSWORD, EMAIL, PHONE, TOKEN);

        var response = handler.handleAllExceptions(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_VALID");
        assertThat(messages()).contains("category=BODY_FIELDS", "password", "email", "phonePersonal", "other");
        assertSafeEvents();
        assertThat(messages().length()).isLessThan(1024);
    }

    @Test
    void constraintMessagesInvalidValuesAndThrowableDetailsAreOmitted() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        ConstraintViolationException exception = new ConstraintViolationException(
                PASSWORD + EMAIL + PHONE + TOKEN, Set.of(violation));
        exception.addSuppressed(new IllegalStateException(CAUSE));

        handler.handleAllExceptions(exception);

        assertThat(messages()).contains("category=CONSTRAINT", "violationCount=1", "category=INVALID_VALID");
        assertSafeEvents();
    }

    @Test
    void malformedBodyTypeMismatchAndMissingParameterNeverLogRejectedInputOrCause() throws Exception {
        var malformed = new HttpMessageNotReadableException(PASSWORD + EMAIL,
                new IllegalArgumentException(CAUSE + TOKEN),
                new MockHttpInputMessage(PHONE.getBytes(StandardCharsets.UTF_8)));
        var mismatch = new MethodArgumentTypeMismatchException(TOKEN, Integer.class, PHONE,
                parameter(), new IllegalArgumentException(CAUSE));
        var missing = new MissingServletRequestParameterException(EMAIL, PASSWORD);
        assertThat(handler.handleAllExceptions(malformed).getBody().getErrorCode()).isEqualTo("INVALID_TYPE");
        assertThat(handler.handleAllExceptions(mismatch).getBody().getErrorCode()).isEqualTo("INVALID_TYPE");
        assertThat(handler.handleAllExceptions(missing).getBody().getErrorCode()).isEqualTo("INVALID_VALIDATED");
        assertSafeEvents();
    }

    @Test
    void wrappedValidationAndAuthenticationFailuresCannotLeakThroughOtherHandlers() throws Exception {
        MethodArgumentNotValidException validation = rejectedFields();
        handler.handleBusinessException(new BusinessException(BUSINESS_ERROR.INVALID_VALID, validation));
        handler.handleAllExceptions(new IllegalStateException(CAUSE, validation));
        handler.handleAllExceptions(new DataIntegrityViolationException(EMAIL, validation));
        handler.handleAllExceptions(new BadCredentialsException(PASSWORD, validation));
        handler.handleAllExceptions(new DisabledException(PHONE, validation));
        handler.handleAllExceptions(new LockedException(TOKEN, validation));
        handler.handleAccessDeniedException(new AccessDeniedException(CAUSE, validation));
        TechnicException technical = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);
        technical.initCause(validation);
        handler.handleTechnicException(technical);
        assertSafeEvents();
    }

    private MethodArgumentNotValidException rejectedFields() throws Exception {
        var result = new BeanPropertyBindingResult(new Object(), EMAIL);
        String[] fields = {"password", "email", "phonePersonal", "refreshToken", TOKEN + "[" + EMAIL + "]"};
        String[] values = {PASSWORD, EMAIL, PHONE, TOKEN, CAUSE};
        for (int index = 0; index < fields.length; index++) {
            result.addError(new FieldError(EMAIL, fields[index], values[index], false,
                    new String[]{TOKEN}, new Object[]{PHONE}, PASSWORD));
        }
        for (int index = 0; index < 30; index++) {
            result.addError(new FieldError(EMAIL, TOKEN + index, PASSWORD));
        }
        result.addError(new ObjectError(EMAIL, CAUSE));
        return new MethodArgumentNotValidException(parameter(), result);
    }

    private MethodParameter parameter() throws NoSuchMethodException {
        return new MethodParameter(SensitiveValidationLogTest.class.getDeclaredMethod("accept", Object.class), 0);
    }

    private static void accept(Object value) {}

    private String messages() {
        return events.list.stream().map(ILoggingEvent::getFormattedMessage)
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private void assertSafeEvents() {
        assertThat(events.list).isNotEmpty();
        for (ILoggingEvent event : events.list) {
            assertThat(event.getThrowableProxy()).isNull();
            assertThat(event.getFormattedMessage()).doesNotContain(PASSWORD, EMAIL, PHONE, TOKEN, CAUSE);
            assertThat(Arrays.toString(event.getArgumentArray())).doesNotContain(PASSWORD, EMAIL, PHONE, TOKEN, CAUSE);
        }
    }
}
