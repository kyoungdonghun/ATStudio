package com.atstudio.atstudio.dto.track;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
public class TrackUpdateRequest {

    @Size(max = TITLE_TRACK_MAX)
    private String title;

    @Min(BPM_MIN)
    @Max(BPM_MAX)
    private Integer bpm;

    @Size(max = 10)
    private String tonality;

    @Size(max = DESCRIPTION_MAX)
    private String description;

    private List<Long> tagIds;

    private Boolean replaceTags;

    private Boolean isActive;
}
