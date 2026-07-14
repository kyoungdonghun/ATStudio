package com.atstudio.atstudio.service.storage;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StorageReferenceChecker {

    private final EntityManager entityManager;

    public boolean isReferenced(StorageDomain domain, String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String query = switch (domain) {
            case TRACK -> """
                    SELECT COUNT(track) FROM Track track
                    WHERE track.audioFile = :key
                       OR track.previewFile = :key
                       OR track.thumbnail = :key
                    """;
            case PLAYLIST -> """
                    SELECT COUNT(playlist) FROM Playlist playlist
                    WHERE playlist.isActive = true AND playlist.thumbnail = :key
                    """;
            case ALBUM -> """
                    SELECT COUNT(album) FROM Album album
                    WHERE album.isActive = true AND album.thumbnail = :key
                    """;
            case COMPANY_CERTIFICATION -> """
                    SELECT COUNT(document) FROM CompanyCertificationDocument document
                    WHERE document.storedPath = :key
                    """;
            case NOTICE -> """
                    SELECT COUNT(attachment) FROM NoticeAttachment attachment
                    WHERE attachment.filePath = :key
                    """;
            case QUESTION -> """
                    SELECT COUNT(attachment) FROM QuestionAttachment attachment
                    WHERE attachment.filePath = :key
                    """;
        };
        return entityManager.createQuery(query, Long.class)
                .setParameter("key", key)
                .getSingleResult() > 0;
    }
}
