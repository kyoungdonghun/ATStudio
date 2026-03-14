package com.atstudio.atstudio.dto.notice;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class NoticeUpdateRequest {

    @Size(max = 200)
    private String title;

    private String content;

    private Boolean isPinned;

    /** IDs of existing attachments to delete */
    private List<Long> deleteAttachmentIds;

    /** New attachments to add */
    private List<MultipartFile> newAttachments;
}
