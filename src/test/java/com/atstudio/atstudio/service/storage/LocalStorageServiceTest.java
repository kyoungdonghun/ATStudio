package com.atstudio.atstudio.service.storage;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private LocalStorageService storage() {
        LocalStorageService storage = new LocalStorageService(
                tempDirectory.resolve("public").toString(),
                tempDirectory.resolve("private").toString());
        storage.init();
        return storage;
    }
}
