package com.atstudio.atstudio.dto.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistCreateRequest {

    @NotBlank
    @Size(max = TITLE_PLAYLIST_MAX)
    private String title;

    @Size(max = DESCRIPTION_MAX)
    private String description;
}
