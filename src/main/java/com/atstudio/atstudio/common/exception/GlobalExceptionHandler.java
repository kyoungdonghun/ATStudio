package com.atstudio.atstudio.common.exception;

import com.atstudio.atstudio.common.dto.ExceptionResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Set<String> DIAGNOSTIC_FIELDS = Set.of(
            "email", "password", "newPassword", "phonePersonal", "phoneCompany",
            "nickname", "token", "refreshToken", "code", "codeVerifier",
            "name", "companyName", "role", "userType", "job");

    // ── Direct Handlers (explicitly thrown) ──────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponseDTO> handleBusinessException(BusinessException ex) {
        logBusinessFailure(ex, ex.getErrorCode());
        return buildErrorResponse(ex.getStatus(), ex.getClientMessage(), ex.getErrorCode().name());
    }

    @ExceptionHandler(TechnicException.class)
    public ResponseEntity<ExceptionResponseDTO> handleTechnicException(TechnicException ex) {
        logTechnicalFailure(ex, ex.getErrorCode());
        return buildErrorResponse(ex.getStatus(), ex.getClientMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Request rejected. category=ACCESS_DENIED");
        return buildErrorResponse(HttpStatus.FORBIDDEN, "해당 정보를 열람할 수 없습니다.", null);
    }

    // ── Fallback Handler (unthrown Spring/Java exceptions) ───────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleAllExceptions(Exception ex) {
        BusinessException businessEx = null;
        TechnicException technicEx = null;

        if (ex instanceof BadCredentialsException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);

        } else if (ex instanceof DisabledException || ex instanceof LockedException) {
            businessEx = new BusinessException(BUSINESS_ERROR.ACCOUNT_DEACTIVATED);

        } else if (ex instanceof MethodArgumentNotValidException
                || ex instanceof ConstraintViolationException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_VALID);
            if (ex instanceof MethodArgumentNotValidException invalid) {
                logger.warn("Validation rejected. category=BODY_FIELDS, fieldCount={}, fields={}",
                        invalid.getBindingResult().getFieldErrorCount(),
                        invalid.getBindingResult().getFieldErrors().stream()
                                .map(error -> diagnosticField(error.getField()))
                                .distinct().sorted().limit(10).toList());
            } else {
                logger.warn("Validation rejected. category=CONSTRAINT, violationCount={}",
                        ((ConstraintViolationException) ex).getConstraintViolations().size());
            }

        } else if (ex instanceof MissingServletRequestParameterException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_VALIDATED);

        } else if (ex instanceof MethodArgumentTypeMismatchException
                || ex instanceof HttpMessageNotReadableException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_TYPE);

        } else if (ex instanceof HttpRequestMethodNotSupportedException) {
            businessEx = new BusinessException(BUSINESS_ERROR.METHOD_NOT_ALLOWED);

        } else if (ex instanceof MaxUploadSizeExceededException) {
            businessEx = new BusinessException(BUSINESS_ERROR.IO_LARGE);

        } else if (ex instanceof NoResourceFoundException) {
            businessEx = new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);

        } else if (ex instanceof DataIntegrityViolationException) {
            businessEx = new BusinessException(BUSINESS_ERROR.DATA_INTEGRITY_VIOLATION);

        } else if (ex instanceof SQLException) {
            technicEx = new TechnicException(TECHNIC_ERROR.DATA_SQL_EXCEPTION);

        } else if (ex instanceof DataAccessException) {
            technicEx = new TechnicException(TECHNIC_ERROR.DATA_ACCESS_EXCEPTION);

        } else if (ex instanceof ConnectException) {
            technicEx = new TechnicException(TECHNIC_ERROR.CONNECT_EXCEPTION);

        } else if (ex instanceof IOException) {
            technicEx = new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);

        } else {
            technicEx = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);
        }

        if (businessEx != null) {
            logBusinessFailure(ex, businessEx.getErrorCode());
            return buildErrorResponse(businessEx.getStatus(), businessEx.getClientMessage(),
                    businessEx.getErrorCode().name());
        }
        logTechnicalFailure(ex, technicEx.getErrorCode());
        return buildErrorResponse(technicEx.getStatus(), technicEx.getClientMessage(), null);
    }

    // ── Shared Builder ────────────────────────────────────────────────────────

    // Causes and exception messages can contain request values, even in wrapped errors.
    private void logBusinessFailure(Exception ex, BUSINESS_ERROR error) {
        logger.warn("Request rejected. category={}, exceptionClass={}", error.name(), ex.getClass().getName());
    }

    private void logTechnicalFailure(Exception ex, TECHNIC_ERROR error) {
        logger.error("Request failed. category={}, exceptionClass={}", error.name(), ex.getClass().getName());
    }

    private static String diagnosticField(String field) {
        return DIAGNOSTIC_FIELDS.contains(field) ? field : "other";
    }

    private ResponseEntity<ExceptionResponseDTO> buildErrorResponse(
            HttpStatus status, String clientMessage, String errorCode) {
        ExceptionResponseDTO response = ExceptionResponseDTO.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(errorCode)
                .message(clientMessage)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
