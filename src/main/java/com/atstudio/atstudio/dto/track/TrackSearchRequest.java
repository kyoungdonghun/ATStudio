package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.common.dto.RequestDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackSearchRequest extends RequestDTO {

    {
        setSize(20);
    }

    private String genre;
    private String mood;
    private String instrument;
    private Integer bpmMin;
    private Integer bpmMax;
    private String tonality;
    private String sort = "latest";
}
