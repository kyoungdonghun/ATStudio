package com.atstudio.atstudio.dto.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
public class NoticeCreateRequest {

    @NotBlank
    @Size(max = TITLE_NOTICE_MAX)
    private String title;

    @NotBlank
    @Size(max = DESCRIPTION_MAX)
    private String content;

    @NotNull
    private Boolean isPinned;

    private List<MultipartFile> attachments;
}
