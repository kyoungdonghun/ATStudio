package com.atstudio.atstudio.dto.track;

import com.atstudio.atstudio.common.dto.RequestDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TrackSearchRequest extends RequestDTO {

    {
        setSize(20);
    }

    private List<String> genre;
    private List<String> mood;
    private List<String> instrument;
    private List<String> usage;
    private Integer bpmMin;
    private Integer bpmMax;
    private String tonality;
    private String sort = "latest";
}
