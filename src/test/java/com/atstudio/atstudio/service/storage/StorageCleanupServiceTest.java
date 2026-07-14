package com.atstudio.atstudio.service.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageCleanupServiceTest {

    @Mock StorageService storageService;
    @Mock StorageReferenceChecker referenceChecker;

    @InjectMocks StorageCleanupService cleanupService;

    @Test
    void missingStagedAndFinalFilesAreIdempotentSuccess() {
        given(storageService.deleteStaged(
                StorageRoot.PUBLIC,
                "10000000-0000-0000-0000-000000000000",
                "notices/attachments/file.pdf"))
                .willReturn(StorageDeleteResult.NOT_FOUND);
        given(storageService.delete(StorageRoot.PUBLIC, "notices/attachments/file.pdf"))
                .willReturn(StorageDeleteResult.NOT_FOUND);

        assertThat(cleanupService.cleanupNew(
                StorageRoot.PUBLIC,
                "10000000-0000-0000-0000-000000000000",
                "notices/attachments/file.pdf"))
                .isEqualTo(StorageCleanupService.CleanupOutcome.DONE);
    }

    @Test
    void sharedOldReferenceIsNeverDeleted() {
        given(referenceChecker.isReferenced(
                StorageDomain.PLAYLIST,
                "playlists/thumbnails/shared.jpg"))
                .willReturn(true);

        assertThat(cleanupService.cleanupOld(
                StorageDomain.PLAYLIST,
                StorageRoot.PUBLIC,
                "playlists/thumbnails/shared.jpg"))
                .isEqualTo(StorageCleanupService.CleanupOutcome.SHARED_REFERENCE);
        verify(storageService, never()).delete(
                StorageRoot.PUBLIC,
                "playlists/thumbnails/shared.jpg");
    }
}
