package com.atstudio.atstudio.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<E> {

    private E data;
    private String message;
    private List<E> dataList;
    private PageInfo pageInfo;

    public static <T> ResponseDTOBuilder<T> withMessage() {
        return ResponseDTO.<T>builder();
    }

    public static <T> ResponseDTOBuilder<T> withSingleData() {
        return ResponseDTO.<T>builder();
    }

    public static <T> ResponseDTOBuilder<T> withAll() {
        return ResponseDTO.<T>builder();
    }
}
