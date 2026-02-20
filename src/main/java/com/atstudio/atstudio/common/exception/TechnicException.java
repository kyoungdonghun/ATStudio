package com.atstudio.atstudio.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TechnicException extends RuntimeException {

    private final TECHNIC_ERROR errorCode;

    public TechnicException(TECHNIC_ERROR errorCode) {
        super(errorCode.getDeveloperMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    public String getClientMessage() {
        return errorCode.getClientMessage();
    }

    public String getDeveloperMessage() {
        return errorCode.getDeveloperMessage();
    }
}
