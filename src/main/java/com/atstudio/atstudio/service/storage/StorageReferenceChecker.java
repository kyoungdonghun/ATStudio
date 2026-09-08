package com.atstudio.atstudio.service.storage;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StorageReferenceChecker {

    private static final List<StorageDomain> PUBLIC_DOMAINS =
            List.of(StorageDomain.TRACK, StorageDomain.PLAYLIST, StorageDomain.ALBUM);

    private final EntityManager entityManager;

    public boolean isReferenced(StorageDomain domain, String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        if (hasDomainReference(domain, key)) {
            return true;
        }
        // Public keys can be shared across catalog domains, including retained inactive rows.
        return PUBLIC_DOMAINS.contains(domain) && PUBLIC_DOMAINS.stream()
                .filter(candidate -> candidate != domain)
                .anyMatch(candidate -> hasDomainReference(candidate, key));
    }

    private boolean hasDomainReference(StorageDomain domain, String key) {
        String query = switch (domain) {
            case TRACK -> """
                    SELECT COUNT(track) FROM Track track
                    WHERE track.audioFile = :key
                       OR track.thumbnail = :key
                    """;
            case PLAYLIST -> """
                    SELECT COUNT(playlist) FROM Playlist playlist
                    WHERE playlist.thumbnail = :key
                    """;
            case ALBUM -> """
                    SELECT COUNT(album) FROM Album album
                    WHERE album.thumbnail = :key
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
