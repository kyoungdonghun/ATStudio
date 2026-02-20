package com.atstudio.atstudio.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponseDTO {

    private int status;
    private String error;
    private String errorCode;
    private String message;
}
