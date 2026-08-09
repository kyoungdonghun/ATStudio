package com.atstudio.atstudio.dto.tag;

import com.atstudio.atstudio.entity.enums.TagType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagCreateRequest {

    private String name;

    @NotNull
    private TagType type;
}
