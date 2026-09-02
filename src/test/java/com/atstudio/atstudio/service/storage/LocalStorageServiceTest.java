package com.atstudio.atstudio.service.storage;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LocalStorageServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void stagesPromotesLoadsAndDeletesGeneratedKey() throws IOException {
        LocalStorageService storage = storage();
        String operationId = UUID.randomUUID().toString();
        String key = storage.generateKey("notices/attachments", "Client Secret.PDF");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Client Secret.PDF",
                "application/pdf",
                "payload".getBytes(StandardCharsets.UTF_8));

        storage.stage(StorageRoot.PUBLIC, operationId, key, file);
        storage.promote(StorageRoot.PUBLIC, operationId, key);

        assertThat(key).startsWith("notices/attachments/").endsWith(".pdf");
        assertThat(key).doesNotContain("Client", "Secret");
        assertThat(storage.loadAsResource(StorageRoot.PUBLIC, key).getContentAsByteArray())
                .isEqualTo("payload".getBytes(StandardCharsets.UTF_8));
        assertThat(storage.getUrl(StorageRoot.PUBLIC, key)).isEqualTo("/uploads/" + key);
        assertThat(storage.delete(StorageRoot.PUBLIC, key)).isEqualTo(StorageDeleteResult.DELETED);
        assertThat(storage.delete(StorageRoot.PUBLIC, key)).isEqualTo(StorageDeleteResult.NOT_FOUND);
    }

    @Test
    void rejectsUntrustedKeysAndPrivateUrls() {
        LocalStorageService storage = storage();
        List<String> invalidKeys = List.of(
                "../outside.txt",
                "notices/../outside.txt",
                "/absolute.txt",
                "C:/absolute.txt",
                "notices\\attachment.txt",
                "notices/file:stream",
                "notices//attachment.txt",
                "notices/attachment.txt\0suffix"
        );

        invalidKeys.forEach(key -> assertThatThrownBy(
                () -> storage.loadAsResource(StorageRoot.PUBLIC, key))
                .isInstanceOf(RuntimeException.class));
        assertThatThrownBy(() -> storage.getUrl(StorageRoot.PRIVATE, "documents/file.pdf"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsDirectoriesAndSymbolicLinks() throws IOException {
        LocalStorageService storage = storage();
        Path publicRoot = tempDirectory.resolve("public");
        Files.createDirectories(publicRoot.resolve("notices/attachments/folder"));

        assertThatThrownBy(() -> storage.delete(
                StorageRoot.PUBLIC,
                "notices/attachments/folder"))
                .isInstanceOf(RuntimeException.class);

        Path outside = Files.writeString(tempDirectory.resolve("outside.txt"), "outside");
        Path link = publicRoot.resolve("notices/attachments/link.txt");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (UnsupportedOperationException | IOException | SecurityException exception) {
            Assumptions.abort("Symbolic links are unavailable in this environment");
        }
        assertThatThrownBy(() -> storage.loadAsResource(
                StorageRoot.PUBLIC,
                "notices/attachments/link.txt"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsNestedPublicAndPrivateRoots() throws IOException {
        Path publicRoot = Files.createDirectories(tempDirectory.resolve("shared"));
        Path privateRoot = Files.createDirectories(publicRoot.resolve("private"));
        LocalStorageService storage = new LocalStorageService(
                publicRoot.toString(),
                privateRoot.toString());

        assertThatThrownBy(storage::init)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsEqualReverseNestedBlankAndFileStorageRoots() throws IOException {
        Path shared = Files.createDirectories(tempDirectory.resolve("same"));
        assertThatThrownBy(() -> {
            LocalStorageService storage = new LocalStorageService(shared.toString(), shared.toString());
            storage.init();
        }).isInstanceOf(IllegalStateException.class);

        Path privateParent = Files.createDirectories(tempDirectory.resolve("private-parent"));
        Path publicChild = Files.createDirectories(privateParent.resolve("public-child"));
        assertThatThrownBy(() -> {
            LocalStorageService storage = new LocalStorageService(publicChild.toString(), privateParent.toString());
            storage.init();
        }).isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> new LocalStorageService(" ", privateParent.toString()).init())
                .isInstanceOf(IllegalStateException.class);

        Path regularFile = Files.writeString(tempDirectory.resolve("not-a-directory"), "data");
        assertThatThrownBy(() -> new LocalStorageService(
                regularFile.toString(),
                tempDirectory.resolve("private-file-case").toString()).init())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void explicitRootRuntimeRejectsRelativeStorageRoots() throws IOException {
        Path privateRoot = Files.createDirectories(tempDirectory.resolve("private"));
        LocalStorageService storage = new LocalStorageService("uploads", privateRoot.toString(), true);

        assertThatThrownBy(storage::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Explicit absolute public and private storage roots");
    }

    @Test
    void productionProfileRejectsRelativeStorageRootsWithoutAnOptInFlag() throws IOException {
        Path privateRoot = Files.createDirectories(tempDirectory.resolve("private"));
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        LocalStorageService storage = new LocalStorageService(
                "uploads",
                privateRoot.toString(),
                false,
                environment);

        assertThatThrownBy(storage::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Explicit absolute public and private storage roots");
    }

    @Test
    void generatedKeysKeepOnlySafeNormalizedExtensions() {
        LocalStorageService storage = storage();

        assertThat(storage.generateKey("tracks/audio", null)).doesNotContain(".");
        assertThat(storage.generateKey("tracks/audio", "README")).doesNotContain(".");
        assertThat(storage.generateKey("tracks/audio", "song.MP3")).endsWith(".mp3");
        assertThat(storage.generateKey("tracks/audio", "song.toolongextension")).doesNotContain(".toolongextension");
        assertThat(storage.generateKey("tracks/audio", "song.exe/path")).doesNotContain(".exe/path");
    }

    @Test
    void stageRejectsMissingEmptyUnreadableAndDuplicateUploads() throws IOException {
        LocalStorageService storage = storage();
        String operationId = UUID.randomUUID().toString();
        String key = "tracks/audio/song.mp3";

        assertThatThrownBy(() -> storage.stage(StorageRoot.PUBLIC, operationId, key, null))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> storage.stage(
                StorageRoot.PUBLIC,
                operationId,
                key,
                new MockMultipartFile("file", new byte[0])))
                .isInstanceOf(RuntimeException.class);

        MultipartFile unreadable = mock(MultipartFile.class);
        given(unreadable.isEmpty()).willReturn(false);
        given(unreadable.getInputStream()).willThrow(new IOException("unreadable"));
        assertThatThrownBy(() -> storage.stage(StorageRoot.PUBLIC, operationId, key, unreadable))
                .isInstanceOf(RuntimeException.class);

        MockMultipartFile payload = new MockMultipartFile(
                "file", "song.mp3", "audio/mpeg", new ByteArrayInputStream(new byte[] {1, 2, 3}));
        storage.stage(StorageRoot.PUBLIC, operationId, key, payload);
        assertThatThrownBy(() -> storage.stage(StorageRoot.PUBLIC, operationId, key, payload))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void stagingRejectsInvalidOperationIdsAndBlockedDirectories() throws IOException {
        LocalStorageService storage = storage();
        MockMultipartFile payload = new MockMultipartFile("file", "data".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> storage.stage(StorageRoot.PUBLIC, null, "tracks/file.mp3", payload))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> storage.stage(StorageRoot.PUBLIC, "not-a-uuid", "tracks/file.mp3", payload))
                .isInstanceOf(RuntimeException.class);

        String operationId = UUID.randomUUID().toString();
        Path blocker = tempDirectory.resolve("public/.staging").resolve(operationId).resolve("blocked");
        Files.createDirectories(blocker.getParent());
        Files.writeString(blocker, "not a directory");

        assertThatThrownBy(() -> storage.stage(
                StorageRoot.PUBLIC,
                operationId,
                "blocked/file.mp3",
                payload))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void promoteAndDeleteStagedHandleMissingAndNonEmptyStagingTrees() throws IOException {
        LocalStorageService storage = storage();
        String operationId = UUID.randomUUID().toString();
        MockMultipartFile first = new MockMultipartFile("file", "first".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile second = new MockMultipartFile("file", "second".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> storage.promote(
                StorageRoot.PUBLIC, operationId, "tracks/missing.mp3"))
                .isInstanceOf(RuntimeException.class);

        storage.stage(StorageRoot.PUBLIC, operationId, "tracks/first.mp3", first);
        storage.stage(StorageRoot.PUBLIC, operationId, "tracks/second.mp3", second);
        storage.promote(StorageRoot.PUBLIC, operationId, "tracks/first.mp3");

        assertThat(storage.deleteStaged(StorageRoot.PUBLIC, operationId, "tracks/second.mp3"))
                .isEqualTo(StorageDeleteResult.DELETED);
        assertThat(storage.deleteStaged(StorageRoot.PUBLIC, operationId, "tracks/second.mp3"))
                .isEqualTo(StorageDeleteResult.NOT_FOUND);
    }

    @Test
    void uninitializedStorageAndAdditionalUnsafeKeysFailClosed() {
        LocalStorageService uninitialized = new LocalStorageService(
                tempDirectory.resolve("unused-public").toString(),
                tempDirectory.resolve("unused-private").toString());
        MockMultipartFile payload = new MockMultipartFile("file", "data".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> uninitialized.stage(
                StorageRoot.PUBLIC,
                UUID.randomUUID().toString(),
                "tracks/file.mp3",
                payload))
                .isInstanceOf(RuntimeException.class);

        LocalStorageService storage = storage();
        List<String> additionalInvalidKeys = List.of("", " ", "tracks/", "tracks/.", "tracks/..");
        additionalInvalidKeys.forEach(key -> assertThatThrownBy(
                () -> storage.getUrl(StorageRoot.PUBLIC, key))
                .isInstanceOf(RuntimeException.class));
        assertThatThrownBy(() -> storage.loadAsResource(StorageRoot.PUBLIC, "tracks/missing.mp3"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void privateNoticeObjectCannotResolveThroughThePublicRoot() throws IOException {
        LocalStorageService storage = storage();
        String operationId = UUID.randomUUID().toString();
        String key = storage.generateKey("notices/attachments", "announcement.html");
        byte[] payload = "<html><script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "attachments",
                "announcement.html",
                "text/html",
                payload);

        storage.stage(StorageRoot.PRIVATE, operationId, key, file);
        storage.promote(StorageRoot.PRIVATE, operationId, key);

        assertThat(storage.loadAsResource(StorageRoot.PRIVATE, key).getContentAsByteArray())
                .isEqualTo(payload);
        assertThatThrownBy(() -> storage.loadAsResource(StorageRoot.PUBLIC, key))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> storage.getUrl(StorageRoot.PRIVATE, key))
                .isInstanceOf(RuntimeException.class);
    }

    private LocalStorageService storage() {
        LocalStorageService storage = new LocalStorageService(
                tempDirectory.resolve("public").toString(),
                tempDirectory.resolve("private").toString());
        storage.init();
        return storage;
    }
}
