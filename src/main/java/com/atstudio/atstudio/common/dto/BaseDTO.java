package com.atstudio.atstudio.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseDTO {

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
