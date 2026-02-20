package com.atstudio.atstudio.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
public class RequestDTO {

    private int page = 1;
    private int size = 10;
    private String keyword;
    private String type;

    public Pageable getPageable() {
        return PageRequest.of(
                Math.max(0, this.page - 1),
                Math.max(1, this.size)
        );
    }
}
