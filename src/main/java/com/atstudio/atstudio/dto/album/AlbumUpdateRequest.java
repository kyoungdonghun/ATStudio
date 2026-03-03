package com.atstudio.atstudio.dto.album;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlbumUpdateRequest {

    @Size(max = 100)
    private String title;

    private String description;
}
