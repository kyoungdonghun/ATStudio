package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.dto.storage.StorageIntegrityIssueResponse;
import com.atstudio.atstudio.dto.storage.StorageIntegrityReportResponse;
import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.CompanyCertificationDocument;
import com.atstudio.atstudio.entity.NoticeAttachment;
import com.atstudio.atstudio.entity.Playlist;
import com.atstudio.atstudio.entity.QuestionAttachment;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.repository.AlbumRepository;
import com.atstudio.atstudio.repository.CompanyCertificationDocumentRepository;
import com.atstudio.atstudio.repository.NoticeAttachmentRepository;
import com.atstudio.atstudio.repository.PlaylistRepository;
import com.atstudio.atstudio.repository.QuestionAttachmentRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only audit of persistent storage references. It never repairs, deletes, or exposes object keys.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StorageIntegrityService {

    static final int ISSUE_RESPONSE_LIMIT = 100;

    private final StorageService storageService;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final CompanyCertificationDocumentRepository companyCertificationDocumentRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;
    private final QuestionAttachmentRepository questionAttachmentRepository;

    public StorageIntegrityReportResponse inspect() {
        Audit audit = new Audit();

        trackRepository.findAll().forEach(track -> {
            auditReference(audit, "TRACK", StorageRoot.PUBLIC, track.getId(), "AUDIO", track.getAudioFile());
            auditOptionalReference(audit, "TRACK", StorageRoot.PUBLIC, track.getId(), "THUMBNAIL", track.getThumbnail());
        });
        albumRepository.findAll().forEach(album -> auditOptionalReference(
                audit, "ALBUM", StorageRoot.PUBLIC, album.getId(), "THUMBNAIL", album.getThumbnail()));
        playlistRepository.findAll().forEach(playlist -> auditOptionalReference(
                audit, "PLAYLIST", StorageRoot.PUBLIC, playlist.getId(), "THUMBNAIL", playlist.getThumbnail()));
        companyCertificationDocumentRepository.findAll().forEach(document -> auditReference(
                audit, "COMPANY_CERTIFICATION_DOCUMENT", StorageRoot.PRIVATE, document.getId(),
                "DOCUMENT", document.getStoredPath()));
        noticeAttachmentRepository.findAll().forEach(attachment -> auditReference(
                audit, "NOTICE_ATTACHMENT", StorageRoot.PRIVATE, attachment.getId(),
                "ATTACHMENT", attachment.getFilePath()));
        questionAttachmentRepository.findAll().forEach(attachment -> auditReference(
                audit, "QUESTION_ATTACHMENT", StorageRoot.PRIVATE, attachment.getId(),
                "ATTACHMENT", attachment.getFilePath()));

        return new StorageIntegrityReportResponse(
                Instant.now(),
                audit.checkedReferenceCount,
                audit.availableReferenceCount,
                audit.missingReferenceCount,
                audit.issueListTruncated,
                List.copyOf(audit.issues));
    }

    private void auditOptionalReference(
            Audit audit,
            String domain,
            StorageRoot storageRoot,
            Long recordId,
            String referenceType,
            String relativeKey) {
        if (relativeKey == null || relativeKey.isBlank()) {
            return;
        }
        auditReference(audit, domain, storageRoot, recordId, referenceType, relativeKey);
    }

    private void auditReference(
            Audit audit,
            String domain,
            StorageRoot storageRoot,
            Long recordId,
            String referenceType,
            String relativeKey) {
        audit.checkedReferenceCount++;
        if (storageService.exists(storageRoot, relativeKey)) {
            audit.availableReferenceCount++;
            return;
        }
        audit.missingReferenceCount++;
        if (audit.issues.size() >= ISSUE_RESPONSE_LIMIT) {
            audit.issueListTruncated = true;
            return;
        }
        audit.issues.add(new StorageIntegrityIssueResponse(domain, storageRoot, recordId, referenceType));
    }

    private static final class Audit {
        private final List<StorageIntegrityIssueResponse> issues = new ArrayList<>();
        private int checkedReferenceCount;
        private int availableReferenceCount;
        private int missingReferenceCount;
        private boolean issueListTruncated;
    }
}
