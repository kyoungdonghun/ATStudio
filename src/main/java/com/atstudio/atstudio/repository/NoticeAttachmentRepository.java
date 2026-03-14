package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Notice;
import com.atstudio.atstudio.entity.NoticeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachment, Long> {

    List<NoticeAttachment> findAllByNoticeId(Long noticeId);

    Optional<NoticeAttachment> findByIdAndNoticeId(Long id, Long noticeId);

    void deleteAllByNotice(Notice notice);
}
