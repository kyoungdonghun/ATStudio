package com.atstudio.atstudio.dto.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String title;

    private String description;
}
