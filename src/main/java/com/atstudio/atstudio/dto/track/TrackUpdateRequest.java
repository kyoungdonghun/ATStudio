package com.atstudio.atstudio.dto.track;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TrackUpdateRequest {

    @Size(max = 100)
    private String title;

    @Min(1)
    private Integer bpm;

    @Size(max = 10)
    private String tonality;

    private String description;

    private List<Long> tagIds;

    private Boolean isActive;
}
