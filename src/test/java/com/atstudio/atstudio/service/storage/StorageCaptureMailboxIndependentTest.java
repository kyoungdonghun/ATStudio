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

import static org.assertj.core.api.Assertions.assertThat;

/** WI009 independent recovery/replay checks: real mailbox files and persistent guard. */
class StorageCaptureMailboxIndependentTest {
    @TempDir Path root;
    private final ObjectMapper json = new ObjectMapper();

    private StorageCaptureMailbox mailbox(StorageContinuityGuard guard, CaptureReferenceSource source) {
        return new StorageCaptureMailbox(root.resolve("bridge"), guard, source,
                root.resolve("public"), root.resolve("private"));
    }

    private void write(Path path, byte[] bytes) throws Exception {
        Files.write(path, bytes);
        if (path.getFileSystem().supportedFileAttributeViews().contains("posix"))
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
    }

    private ObjectNode command(String action, String request, String capture) {
        var value = json.createObjectNode().put("version", 1).put("protocol", "ats-online-v1")
                .put("request_id", request).put("action", action);
        if (action.equals("FREEZE")) value.put("timeout_seconds", 0);
        else value.put("capture_id", capture);
        return value;
    }

    private JsonNode send(StorageCaptureMailbox mailbox, ObjectNode request) throws Exception {
        String id = request.path("request_id").asText();
        byte[] raw = json.writeValueAsBytes(request);
        write(root.resolve("bridge/requests/" + id + ".json"), raw);
        mailbox.processOnce();
        JsonNode response = json.readTree(Files.readAllBytes(root.resolve("bridge/responses/" + id + ".json")));
        assertThat(response.path("request_sha256").asText()).isEqualTo(StorageCaptureMailbox.sha(raw));
        return response;
    }

    @Test
    void completedQueueEntriesDoNotStarveNewRequests() throws Exception {
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = mailbox(guard, List::of);
            for (int i = 0; i < 1001; i++) {
                String name = new UUID(0, i).toString() + ".json";
                write(root.resolve("bridge/requests/" + name), new byte[]{0});
                write(root.resolve("bridge/responses/" + name), new byte[]{0});
            }
            String capture = "ffffffff-ffff-ffff-ffff-ffffffffffff";
            assertThat(send(mailbox, command("FREEZE", capture, capture)).path("status").asText()).isEqualTo("FROZEN");
        }
    }

    @Test
    void rejectedManifestCanResumeSameCaptureWithoutOpeningAdmissions() throws Exception {
        AtomicBoolean broken = new AtomicBoolean(true);
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = mailbox(guard, () -> {
                if (broken.get()) throw new IllegalStateException("synthetic inventory failure");
                return List.of();
            });
            String capture = UUID.randomUUID().toString();
            assertThat(send(mailbox, command("FREEZE", capture, capture)).path("status").asText()).isEqualTo("REJECTED");
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
            broken.set(false);
            JsonNode resumed = send(mailbox, command("REFERENCES", UUID.randomUUID().toString(), capture));
            assertThat(resumed.path("status").asText()).isEqualTo("FROZEN");
            assertThat(guard.retainedCaptureId()).isEqualTo(capture);
            assertThat(Files.exists(root.resolve("bridge/claims/" + capture + ".json"))).isTrue();
        }
    }

    @Test
    void abortRequiresEndedReadersAndExactReceipt() throws Exception {
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = mailbox(guard, List::of);
            String capture = UUID.randomUUID().toString();
            send(mailbox, command("FREEZE", capture, capture));
            var receipt = json.createObjectNode().put("version", 1).put("protocol", "ats-online-v1")
                    .put("capture_id", capture).put("run_id", capture.replace("-", ""))
                    .put("status", "ABANDONED").put("helpers_ended", true).put("readers_ended", false)
                    .put("reconciled_at", Instant.now().toString());
            byte[] raw = json.writeValueAsBytes(receipt);
            write(root.resolve("bridge/captures/" + capture + "/abandon-receipt.json"), raw);
            JsonNode refused = send(mailbox, command("ABORT", UUID.randomUUID().toString(), capture)
                    .put("receipt_sha256", StorageCaptureMailbox.sha(raw)));
            assertThat(refused.path("status").asText()).isEqualTo("REJECTED");
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
        }
    }

    @Test
    void claimedRequestCannotBeReboundToAnotherActionAfterResponseLoss() throws Exception {
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = mailbox(guard, List::of);
            String capture = UUID.randomUUID().toString();
            send(mailbox, command("FREEZE", capture, capture));
            Files.delete(root.resolve("bridge/responses/" + capture + ".json"));
            JsonNode refused = send(mailbox, command("STATUS", capture, capture));
            assertThat(refused.path("status").asText()).isEqualTo("REJECTED");
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
        }
    }
}
