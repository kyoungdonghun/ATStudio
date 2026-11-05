package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;

/** Private capture inventory, never an HTTP response. All reference writers are frozen. */
@Service
@RequiredArgsConstructor
public class StorageCaptureReferences implements CaptureReferenceSource {
    private final StorageContinuityGuard guard;
    private final StorageService storage;
    private final TrackRepository tracks;
    private final AlbumRepository albums;
    private final PlaylistRepository playlists;
    private final CompanyCertificationDocumentRepository documents;
    private final NoticeAttachmentRepository notices;
    private final QuestionAttachmentRepository questions;
    private final StorageMutationRepository mutations;
    private static final int MAX_ROWS = 100_000;

    @Override @Transactional(readOnly = true)
    public List<Reference> snapshot() {
        if (!guard.retainedMode().equals("FROZEN")) throw new IllegalStateException("Capture is not frozen");
        String captureId = guard.retainedCaptureId();
        Map<String, Reference> refs = new TreeMap<>();
        scan(tracks, track -> {
            add(refs, StorageRoot.PUBLIC, track.getAudioFile(), true);
            optional(refs, StorageRoot.PUBLIC, track.getStreamAudioFile());
            optional(refs, StorageRoot.PUBLIC, track.getPendingAudioFile());
            optional(refs, StorageRoot.PUBLIC, track.getClaimedAudioFile());
            optional(refs, StorageRoot.PUBLIC, track.getThumbnail());
        });
        scan(albums, value -> optional(refs, StorageRoot.PUBLIC, value.getThumbnail()));
        scan(playlists, value -> optional(refs, StorageRoot.PUBLIC, value.getThumbnail()));
        // CompanyCertification.documentPath is a directory prefix, not a file reference.
        // The document child rows below are the authoritative stored-file references.
        scan(documents, value -> add(refs, StorageRoot.PRIVATE, value.getStoredPath(), true));
        scan(notices, value -> add(refs, StorageRoot.PRIVATE, value.getFilePath(), true));
        scan(questions, value -> add(refs, StorageRoot.PRIVATE, value.getFilePath(), true));
        scan(mutations, value -> {
            if (value.getState() == StorageMutationState.DONE) return;
            if (value.getNewKey() != null) {
                add(refs, value.getStorageRoot(), value.getNewKey(), false);
                add(refs, value.getStorageRoot(), ".staging/" + value.getOperationId() + "/" + value.getNewKey(), false);
            }
            if (value.getOldKey() != null) add(refs, value.getStorageRoot(), value.getOldKey(), false);
        });
        if (!guard.retainedMode().equals("FROZEN") || !captureId.equals(guard.retainedCaptureId())) throw new IllegalStateException("Capture changed during reference read");
        return List.copyOf(refs.values());
    }

    private void optional(Map<String, Reference> refs, StorageRoot root, String key) {
        if (key != null && !key.isBlank()) add(refs, root, key, true);
    }

    private void add(Map<String, Reference> refs, StorageRoot root, String key, boolean required) {
        new StorageContinuityGuard.FileRef(root, key);
        boolean present = storage.exists(root, key);
        if (required && !present) throw new IllegalStateException("Required capture reference is missing");
        String identity = root.name() + ":" + key;
        Reference previous = refs.get(identity);
        refs.put(identity, new Reference(root.name(), key, required || (previous != null && previous.required()), present));
        if (refs.size() > MAX_ROWS) throw new IllegalStateException("Capture reference limit exceeded");
    }

    private <T> void scan(JpaRepository<T, Long> repository, Consumer<T> consumer) {
        for (int page = 0; page < MAX_ROWS / 500; page++) {
            var result = repository.findAll(PageRequest.of(page, 500, Sort.by("id")));
            if (result.getTotalElements() > MAX_ROWS) throw new IllegalStateException("Capture table limit exceeded");
            result.forEach(consumer);
            if (!result.hasNext()) return;
        }
        throw new IllegalStateException("Capture table limit exceeded");
    }
}
