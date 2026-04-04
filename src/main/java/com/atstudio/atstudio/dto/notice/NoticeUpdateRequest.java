package com.atstudio.atstudio.dto.notice;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
public class NoticeUpdateRequest {

    @Size(max = TITLE_NOTICE_MAX)
    private String title;

    @Size(max = DESCRIPTION_MAX)
    private String content;

    private Boolean isPinned;

    /** IDs of existing attachments to delete */
    private List<Long> deleteAttachmentIds;

    /** New attachments to add */
    private List<MultipartFile> newAttachments;
}
