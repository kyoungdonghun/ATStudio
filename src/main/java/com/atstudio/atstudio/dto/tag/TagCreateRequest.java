package com.atstudio.atstudio.dto.tag;

import com.atstudio.atstudio.entity.enums.TagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    private TagType type;
}
