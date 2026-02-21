package com.atstudio.atstudio.dto.util;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserTypeResponse(
        String userType,
        String job
) {
}
