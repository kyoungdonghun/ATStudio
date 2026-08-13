package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    interface AdminEditRow {
        String getTitle();
        String getContent();
        Boolean getIsPinned();
        Long getAttachmentId();
        String getAttachmentOriginalName();
        Long getAttachmentFileSize();
    }

    Page<Notice> findAllByOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

    @Query("""
            select n.title as title,
                   n.content as content,
                   n.isPinned as isPinned,
                   attachment.id as attachmentId,
                   attachment.originalName as attachmentOriginalName,
                   attachment.fileSize as attachmentFileSize
            from Notice n
            left join NoticeAttachment attachment on attachment.notice = n
            where n.id = :noticeId
            order by attachment.id
            """)
    List<AdminEditRow> findAdminEditRowsById(@Param("noticeId") Long noticeId);
}
