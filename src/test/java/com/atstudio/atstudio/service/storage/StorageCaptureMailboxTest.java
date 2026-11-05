package com.atstudio.atstudio.service.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

class StorageCaptureMailboxTest {
    @TempDir Path root;
    private final ObjectMapper json = new ObjectMapper();
    private static final String HASH = "a".repeat(64);
    private StorageCaptureMailbox bridge(StorageContinuityGuard guard, CaptureReferenceSource source) {
        return new StorageCaptureMailbox(root.resolve("control"), guard, source, root.resolve("public"), root.resolve("private"));
    }
    private ObjectNode command(String action, String capture) {
        var command = json.createObjectNode().put("version", 1).put("protocol", "ats-online-v1")
                .put("request_id", action.equals("FREEZE") ? capture : UUID.randomUUID().toString()).put("action", action);
        if (action.equals("FREEZE")) command.put("timeout_seconds", 0); else command.put("capture_id", capture);
        return command;
    }
    private JsonNode send(StorageCaptureMailbox bridge, ObjectNode command) throws Exception {
        Path request = root.resolve("control/requests/" + command.get("request_id").asText() + ".json");
        writePrivate(request, json.writeValueAsBytes(command));
        bridge.processOnce();
        byte[] bytes = Files.readAllBytes(root.resolve("control/responses/" + command.get("request_id").asText() + ".json"));
        JsonNode response = json.readTree(bytes);
        assertThat(response.path("request_sha256").asText()).isEqualTo(StorageCaptureMailbox.sha(Files.readAllBytes(request)));
        return response;
    }
    private void writePrivate(Path path, byte[] bytes) throws Exception {
        Files.write(path, bytes);
        if (path.getFileSystem().supportedFileAttributeViews().contains("posix"))
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
    }
    private CaptureReferenceSource source() {
        return () -> List.of(new CaptureReferenceSource.Reference("PUBLIC", "tracks/file.wav", true, true));
    }
    private ObjectNode receipt(String capture, String manifest) {
        return json.createObjectNode().put("version", 1).put("protocol", "ats-online-v1").put("status", "VERIFIED")
                .put("run_id", "b".repeat(32)).put("capture_id", capture).put("manifest_sha256", manifest)
                .put("dump_sha256", HASH).put("content_manifest_sha256", HASH).put("snapshot_id", HASH)
                .put("repository_id", HASH).put("verified_at", Instant.now().toString()).put("helpers_ended", true)
                .put("inventory_verified", true).put("content_verified", true).put("restore_verified", false);
    }

    @Test
    void actualFilesConnectFreezeSealAndReceiptBoundRelease() throws Exception {
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = bridge(guard, source());
            String capture = UUID.randomUUID().toString();
            var frozen = send(mailbox, command("FREEZE", capture));
            assertThat(frozen.path("status").asText()).isEqualTo("FROZEN");
            String manifest = frozen.path("manifest_sha256").asText();
            assertThat(send(mailbox, command("SEAL", capture).put("manifest_sha256", "0".repeat(64)).put("dump_sha256", HASH)).path("status").asText()).isEqualTo("REJECTED");
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
            assertThat(send(mailbox, command("SEAL", capture).put("manifest_sha256", manifest).put("dump_sha256", HASH)).path("status").asText()).isEqualTo("PINNED");
            byte[] receipt = json.writeValueAsBytes(receipt(capture, manifest));
            writePrivate(root.resolve("control/captures/" + capture + "/verified-receipt.json"), receipt);
            assertThat(send(mailbox, command("RELEASE", capture).put("manifest_sha256", manifest).put("dump_sha256", HASH)
                    .put("receipt_sha256", "0".repeat(64))).path("status").asText()).isEqualTo("REJECTED");
            assertThat(guard.retainedMode()).isEqualTo("PINNED");
            assertThat(send(mailbox, command("RELEASE", capture).put("manifest_sha256", manifest).put("dump_sha256", HASH)
                    .put("receipt_sha256", StorageCaptureMailbox.sha(receipt))).path("status").asText()).isEqualTo("RELEASED");
            assertThat(guard.retainedMode()).isEqualTo("EMPTY");
            assertThat(send(mailbox, command("SEAL", capture).put("manifest_sha256", manifest).put("dump_sha256", HASH)).path("status").asText()).isEqualTo("REJECTED");
        }
    }

    @Test
    void failedReferenceReadRetainsFreezeAndExplicitReferencesResumesAfterRestart() throws Exception {
        String capture = UUID.randomUUID().toString();
        AtomicBoolean fail = new AtomicBoolean(true);
        CaptureReferenceSource references = () -> {
            if (fail.get()) throw new IllegalStateException("synthetic repository failure");
            return source().snapshot();
        };
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            assertThat(send(bridge(guard, references), command("FREEZE", capture)).path("status").asText()).isEqualTo("REJECTED");
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
        }
        fail.set(false);
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = bridge(guard, references);
            assertThat(send(mailbox, command("STATUS", capture)).path("status").asText()).isEqualTo("FROZEN");
            assertThat(send(mailbox, command("REFERENCES", capture)).path("status").asText()).isEqualTo("FROZEN");
            assertThat(send(mailbox, command("FREEZE", UUID.randomUUID().toString())).path("status").asText()).isEqualTo("REJECTED");
        }
    }

    @Test
    void abandonmentNeedsExactReceiptAndEndsOnlyFrozenCapture() throws Exception {
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = bridge(guard, source());
            String capture = UUID.randomUUID().toString();
            send(mailbox, command("FREEZE", capture));
            var abandoned = json.createObjectNode().put("version",1).put("protocol","ats-online-v1").put("status","ABANDONED")
                    .put("capture_id",capture).put("run_id","b".repeat(32)).put("helpers_ended",true)
                    .put("readers_ended",false).put("reconciled_at", Instant.now().toString());
            Path receipt = root.resolve("control/captures/" + capture + "/abandon-receipt.json");
            byte[] bytes = json.writeValueAsBytes(abandoned); writePrivate(receipt,bytes);
            assertThat(send(mailbox, command("ABORT",capture).put("receipt_sha256",StorageCaptureMailbox.sha(bytes))).path("status").asText()).isEqualTo("REJECTED");
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
            abandoned.put("readers_ended",true); bytes = json.writeValueAsBytes(abandoned); writePrivate(receipt,bytes);
            assertThat(send(mailbox, command("ABORT",capture).put("receipt_sha256",StorageCaptureMailbox.sha(bytes))).path("status").asText()).isEqualTo("ABORTED");
            assertThat(guard.retainedMode()).isEqualTo("EMPTY");
        }
    }

    @Test
    void completedRequestHistoryDoesNotStarveNewCommands() throws Exception {
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = bridge(guard, source());
            for(int number=0;number<1001;number++) {
                String id=String.format("00000000-0000-0000-0000-%012x",number);
                writePrivate(root.resolve("control/requests/"+id+".json"), "{}".getBytes());
                writePrivate(root.resolve("control/responses/"+id+".json"), "{}".getBytes());
            }
            String capture="ffffffff-ffff-ffff-ffff-ffffffffffff";
            assertThat(send(mailbox, command("FREEZE",capture)).path("status").asText()).isEqualTo("FROZEN");
        }
    }

    @Test
    void controlDirectoryCannotOverlapMediaOrGuardState() {
        try(var guard=new StorageContinuityGuard(root.resolve("state").toString())) {
            assertThatThrownBy(() -> new StorageCaptureMailbox(root.resolve("public/control"),guard,source(),root.resolve("public"),root.resolve("private")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
