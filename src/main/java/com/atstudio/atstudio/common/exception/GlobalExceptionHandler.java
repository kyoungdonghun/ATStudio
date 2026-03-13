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

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Direct Handlers (explicitly thrown) ──────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponseDTO> handleBusinessException(BusinessException ex) {
        logger.warn("BusinessException(Throw): {}. Detail: {}", ex.getDeveloperMessage(), ex.toString(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getClientMessage(), ex.getErrorCode().name());
    }

    @ExceptionHandler(TechnicException.class)
    public ResponseEntity<ExceptionResponseDTO> handleTechnicException(TechnicException ex) {
        logger.error("TechnicException(Throw): {}. Detail: {}", ex.getDeveloperMessage(), ex.toString(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getClientMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("AccessDeniedException: insufficient authority. Detail: {}", ex.toString());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "해당 정보를 열람할 수 없습니다.", null);
    }

    // ── Fallback Handler (unthrown Spring/Java exceptions) ───────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDTO> handleAllExceptions(Exception ex) {
        BusinessException businessEx = null;
        TechnicException technicEx = null;

        if (ex instanceof BadCredentialsException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof DisabledException || ex instanceof LockedException) {
            businessEx = new BusinessException(BUSINESS_ERROR.ACCOUNT_DEACTIVATED);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof MethodArgumentNotValidException
                || ex instanceof ConstraintViolationException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_VALID);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof MissingServletRequestParameterException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_VALIDATED);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof MethodArgumentTypeMismatchException
                || ex instanceof HttpMessageNotReadableException) {
            businessEx = new BusinessException(BUSINESS_ERROR.INVALID_TYPE);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof HttpRequestMethodNotSupportedException) {
            businessEx = new BusinessException(BUSINESS_ERROR.METHOD_NOT_ALLOWED);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof MaxUploadSizeExceededException) {
            businessEx = new BusinessException(BUSINESS_ERROR.IO_LARGE);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof DataIntegrityViolationException) {
            businessEx = new BusinessException(BUSINESS_ERROR.DATA_INTEGRITY_VIOLATION);
            logger.warn("BusinessException(Fallback): {}. Detail: {}",
                    businessEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof SQLException) {
            technicEx = new TechnicException(TECHNIC_ERROR.DATA_SQL_EXCEPTION);
            logger.error("TechnicException(Fallback): {}. Detail: {}",
                    technicEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof DataAccessException) {
            technicEx = new TechnicException(TECHNIC_ERROR.DATA_ACCESS_EXCEPTION);
            logger.error("TechnicException(Fallback): {}. Detail: {}",
                    technicEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof ConnectException) {
            technicEx = new TechnicException(TECHNIC_ERROR.CONNECT_EXCEPTION);
            logger.error("TechnicException(Fallback): {}. Detail: {}",
                    technicEx.getDeveloperMessage(), ex.toString(), ex);

        } else if (ex instanceof IOException) {
            technicEx = new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);
            logger.error("TechnicException(Fallback): {}. Detail: {}",
                    technicEx.getDeveloperMessage(), ex.toString(), ex);

        } else {
            technicEx = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);
            logger.error("TechnicException(Fallback): {}. Detail: {}",
                    technicEx.getDeveloperMessage(), ex.toString(), ex);
        }

        if (businessEx != null) {
            return buildErrorResponse(businessEx.getStatus(), businessEx.getClientMessage(),
                    businessEx.getErrorCode().name());
        }
        return buildErrorResponse(technicEx.getStatus(), technicEx.getClientMessage(), null);
    }

    // ── Shared Builder ────────────────────────────────────────────────────────

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
