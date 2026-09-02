package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.dto.storage.StorageIntegrityReportResponse;
import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.NoticeAttachment;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.repository.AlbumRepository;
import com.atstudio.atstudio.repository.CompanyCertificationDocumentRepository;
import com.atstudio.atstudio.repository.NoticeAttachmentRepository;
import com.atstudio.atstudio.repository.PlaylistRepository;
import com.atstudio.atstudio.repository.QuestionAttachmentRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class StorageIntegrityServiceTest {

    @Test
    void reportsMissingReferencesWithoutExposingStorageKeysOrOriginalNames() {
        StorageService storageService = mock(StorageService.class);
        TrackRepository trackRepository = mock(TrackRepository.class);
        AlbumRepository albumRepository = mock(AlbumRepository.class);
        PlaylistRepository playlistRepository = mock(PlaylistRepository.class);
        CompanyCertificationDocumentRepository companyDocumentRepository = mock(CompanyCertificationDocumentRepository.class);
        NoticeAttachmentRepository noticeAttachmentRepository = mock(NoticeAttachmentRepository.class);
        QuestionAttachmentRepository questionAttachmentRepository = mock(QuestionAttachmentRepository.class);
        StorageIntegrityService service = new StorageIntegrityService(
                storageService,
                trackRepository,
                albumRepository,
                playlistRepository,
                companyDocumentRepository,
                noticeAttachmentRepository,
                questionAttachmentRepository);

        Track track = Track.builder()
                .title("Visible title")
                .bpm(120)
                .tonality("C")
                .audioFile("tracks/audio/private-key.mp3")
                .thumbnail("tracks/thumbnails/private-thumbnail.png")
                .build();
        ReflectionTestUtils.setField(track, "id", 7L);
        Album album = Album.builder().title("Album").build();
        ReflectionTestUtils.setField(album, "id", 8L);
        ReflectionTestUtils.setField(album, "thumbnail", "albums/thumbnails/present.png");
        NoticeAttachment attachment = NoticeAttachment.builder()
                .originalName("secret-notice.pdf")
                .filePath("notices/attachments/private-document.pdf")
                .fileSize(1L)
                .build();
        ReflectionTestUtils.setField(attachment, "id", 9L);

        given(trackRepository.findAll()).willReturn(List.of(track));
        given(albumRepository.findAll()).willReturn(List.of(album));
        given(playlistRepository.findAll()).willReturn(List.of());
        given(companyDocumentRepository.findAll()).willReturn(List.of());
        given(noticeAttachmentRepository.findAll()).willReturn(List.of(attachment));
        given(questionAttachmentRepository.findAll()).willReturn(List.of());
        given(storageService.exists(StorageRoot.PUBLIC, "tracks/audio/private-key.mp3")).willReturn(false);
        given(storageService.exists(StorageRoot.PUBLIC, "tracks/thumbnails/private-thumbnail.png")).willReturn(false);
        given(storageService.exists(StorageRoot.PUBLIC, "albums/thumbnails/present.png")).willReturn(true);
        given(storageService.exists(StorageRoot.PRIVATE, "notices/attachments/private-document.pdf")).willReturn(false);

        StorageIntegrityReportResponse report = service.inspect();

        assertThat(report.checkedReferenceCount()).isEqualTo(4);
        assertThat(report.availableReferenceCount()).isEqualTo(1);
        assertThat(report.missingReferenceCount()).isEqualTo(3);
        assertThat(report.issues()).extracting("domain")
                .containsExactly("TRACK", "TRACK", "NOTICE_ATTACHMENT");
        assertThat(report.toString())
                .doesNotContain("private-key", "private-thumbnail", "private-document", "secret-notice");
    }
}
