package com.atstudio.atstudio.service.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** Opt-in private filesystem control. No HTTP listener, credentials or outbound calls. */
@Component
@ConditionalOnProperty(name = "app.storage.capture-control-enabled", havingValue = "true")
public class StorageCaptureMailbox {
    private static final String PROTOCOL = "ats-online-v1";
    private static final int MAX_BYTES = 16 * 1024 * 1024;
    private final Path mailbox;
    private final StorageContinuityGuard guard;
    private final CaptureReferenceSource references;
    private final ObjectMapper json = new ObjectMapper();

    @Autowired
    public StorageCaptureMailbox(@Value("${app.storage.capture-control-path:}") String path,
            StorageContinuityGuard guard, StorageCaptureReferences references, LocalStorageService storage) {
        this(Path.of(path), guard, references, storage.captureRoot(StorageRoot.PUBLIC), storage.captureRoot(StorageRoot.PRIVATE));
    }

    public StorageCaptureMailbox(Path path, StorageContinuityGuard guard, CaptureReferenceSource references,
            Path publicRoot, Path privateRoot) {
        if (!path.isAbsolute() || guard.stateDirectory() == null) throw rejected();
        this.mailbox = path.normalize();
        this.guard = guard;
        this.references = references;
        json.enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        for (Path other : List.of(publicRoot, privateRoot, guard.stateDirectory())) {
            Path normalized = other.toAbsolutePath().normalize();
            if (mailbox.startsWith(normalized) || normalized.startsWith(mailbox)) throw rejected();
        }
        try {
            directory(mailbox);
            for (String child : List.of("requests", "responses", "claims", "captures")) directory(mailbox.resolve(child));
        } catch (IOException exception) { throw rejected(); }
    }

    /** One command per tick; the global scheduler remains subject to existing background controls. */
    @Scheduled(fixedDelayString = "${app.storage.capture-control-poll-ms:1000}")
    public synchronized void processOnce() {
        try {
            secure(mailbox, true);
            try (var files = Files.list(mailbox.resolve("requests"))) {
                List<Path> pending = files.filter(path -> path.getFileName().toString().matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.json"))
                        .filter(path -> !Files.exists(mailbox.resolve("responses").resolve(path.getFileName()), LinkOption.NOFOLLOW_LINKS))
                        .sorted().limit(1000).toList();
                for (Path request : pending) {
                    String id = request.getFileName().toString().replace(".json", "");
                    uuid(id);
                    if (!Files.exists(mailbox.resolve("responses").resolve(id + ".json"), LinkOption.NOFOLLOW_LINKS)) {
                        process(request, id);
                        return;
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            // No private path, key or request body enters logs. Invalid mailbox remains inert.
        }
    }

    private void process(Path request, String id) throws IOException {
        ObjectNode response = base(id, id);
        try {
            byte[] bytes = read(request, 64 * 1024);
            response.put("request_sha256", sha(bytes));
            JsonNode command = json.readTree(bytes);
            if (!command.isObject() || !command.path("version").isIntegralNumber() || command.path("version").asInt(-1) != 1
                    || !command.path("protocol").asText().equals(PROTOCOL)
                    || !id.equals(command.path("request_id").asText())) throw rejected();
            String action = text(command, "action");
            String capture = action.equals("FREEZE") ? id : uuid(text(command, "capture_id"));
            response.put("capture_id", capture);
            Set<String> fields = new HashSet<>(Set.of("version", "protocol", "request_id", "action"));
            if (action.equals("FREEZE")) fields.add("timeout_seconds"); else fields.add("capture_id");
            if (Set.of("SEAL", "RELEASE").contains(action)) fields.addAll(Set.of("manifest_sha256", "dump_sha256"));
            if (Set.of("RELEASE", "ABORT").contains(action)) fields.add("receipt_sha256");
            command.fieldNames().forEachRemaining(field -> { if (!fields.contains(field)) throw rejected(); });
            Path claim = mailbox.resolve("claims").resolve(id + ".json");
            if (Files.exists(claim, LinkOption.NOFOLLOW_LINKS)) {
                if (!Arrays.equals(bytes, read(claim, 64 * 1024))) throw rejected();
            } else publish(claim, bytes);
            switch (action) {
                case "FREEZE" -> freeze(command, capture, response);
                case "STATUS" -> status(capture, response);
                case "REFERENCES" -> references(capture, response);
                case "SEAL" -> seal(command, capture, response);
                case "RELEASE" -> release(command, capture, response);
                case "ABORT" -> abort(command, capture, response);
                default -> throw rejected();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            response.put("status", "REJECTED").put("code", "INTERRUPTED_RECONCILE_REQUIRED");
        } catch (IOException | RuntimeException exception) {
            response.put("status", "REJECTED").put("code", "CAPTURE_REJECTED_RECONCILE_REQUIRED");
        }
        publish(mailbox.resolve("responses").resolve(id + ".json"), json.writeValueAsBytes(response));
    }

    private void freeze(JsonNode command, String capture, ObjectNode response) throws IOException, InterruptedException {
        JsonNode timeout = command.get("timeout_seconds");
        if (timeout == null || !timeout.isIntegralNumber() || !timeout.canConvertToInt() || timeout.asInt() < 0 || timeout.asInt() > 300) throw rejected();
        Path capturePath = capturePath(capture);
        if (guard.retainedMode().equals("EMPTY")) {
            if (Files.exists(capturePath, LinkOption.NOFOLLOW_LINKS)) throw rejected();
            guard.freeze(Duration.ofSeconds(timeout.asLong()), capture);
        }
        references(capture, response);
    }

    private void references(String capture, ObjectNode response) throws IOException {
        if (!capture.equals(guard.retainedCaptureId()) || !guard.retainedMode().equals("FROZEN")) throw rejected();
        Path capturePath = capturePath(capture);
        directory(capturePath);
        Path manifest = capturePath.resolve("manifest.json");
        if (!Files.exists(manifest, LinkOption.NOFOLLOW_LINKS)) {
            List<CaptureReferenceSource.Reference> snapshot = references.snapshot();
            ObjectNode content = json.createObjectNode().put("version", 1).put("protocol", PROTOCOL).put("capture_id", capture);
            content.set("refs", json.valueToTree(snapshot));
            publish(manifest, json.writeValueAsBytes(content));
        }
        response.put("status", "FROZEN");
        manifestResponse(capture, response);
    }

    private void abort(JsonNode command, String capture, ObjectNode response) throws IOException {
        if (!capture.equals(guard.retainedCaptureId()) || !guard.retainedMode().equals("FROZEN")) throw rejected();
        String receiptDigest = digestField(command, "receipt_sha256");
        byte[] bytes = read(capturePath(capture).resolve("abandon-receipt.json"), 64 * 1024);
        if (!receiptDigest.equals(sha(bytes))) throw rejected();
        JsonNode receipt = json.readTree(bytes);
        if (receipt.path("version").asInt() != 1 || !PROTOCOL.equals(receipt.path("protocol").asText())
                || !"ABANDONED".equals(receipt.path("status").asText()) || !capture.equals(receipt.path("capture_id").asText())
                || !receipt.path("run_id").asText().matches("[a-f0-9]{32}")
                || !receipt.path("helpers_ended").isBoolean() || !receipt.path("helpers_ended").asBoolean()
                || !receipt.path("readers_ended").isBoolean() || !receipt.path("readers_ended").asBoolean()) throw rejected();
        Instant reconciled = Instant.parse(text(receipt, "reconciled_at"));
        if (reconciled.isAfter(Instant.now().plusSeconds(60)) || reconciled.isBefore(Instant.now().minus(Duration.ofHours(6)))) throw rejected();
        bindOnce(capturePath(capture).resolve("abort.json"), json.createObjectNode().put("capture_id", capture).put("receipt_sha256", receiptDigest));
        guard.releaseReconciled(capture);
        response.put("status", "ABORTED").put("receipt_sha256", receiptDigest);
    }

    private void status(String capture, ObjectNode response) throws IOException {
        String current = guard.retainedCaptureId();
        if (!current.equals("-") && !current.equals(capture)) throw rejected();
        response.put("status", guard.retainedMode());
        Path manifest = capturePath(capture).resolve("manifest.json");
        if (Files.exists(manifest, LinkOption.NOFOLLOW_LINKS)) manifestResponse(capture, response);
    }

    private void seal(JsonNode command, String capture, ObjectNode response) throws IOException {
        if (!capture.equals(guard.retainedCaptureId())) throw rejected();
        String manifestDigest = digestField(command, "manifest_sha256"), dumpDigest = digestField(command, "dump_sha256");
        byte[] manifestBytes = read(capturePath(capture).resolve("manifest.json"), MAX_BYTES);
        if (!sha(manifestBytes).equals(manifestDigest)) throw rejected();
        JsonNode manifest = json.readTree(manifestBytes);
        if (manifest.path("version").asInt() != 1 || !PROTOCOL.equals(manifest.path("protocol").asText())
                || !capture.equals(manifest.path("capture_id").asText()) || !manifest.path("refs").isArray()) throw rejected();
        Set<StorageContinuityGuard.FileRef> pins = new HashSet<>();
        for (JsonNode reference : manifest.path("refs")) {
            if (!reference.path("required").isBoolean() || !reference.path("present").isBoolean()) throw rejected();
            if (reference.path("required").asBoolean() && !reference.path("present").asBoolean()) throw rejected();
            if (reference.path("present").asBoolean()) pins.add(new StorageContinuityGuard.FileRef(
                    StorageRoot.valueOf(text(reference, "root")), text(reference, "key")));
        }
        ObjectNode binding = json.createObjectNode().put("capture_id", capture).put("manifest_sha256", manifestDigest).put("dump_sha256", dumpDigest);
        Path sealed = capturePath(capture).resolve("seal.json");
        bindOnce(sealed, binding);
        if (guard.retainedMode().equals("FROZEN")) guard.seal(capture, pins);
        else if (!guard.retainedMode().equals("PINNED")) throw rejected();
        response.put("status", "PINNED").put("manifest_sha256", manifestDigest).put("dump_sha256", dumpDigest);
    }

    private void release(JsonNode command, String capture, ObjectNode response) throws IOException {
        String manifestDigest = digestField(command, "manifest_sha256"), dumpDigest = digestField(command, "dump_sha256");
        String receiptDigest = digestField(command, "receipt_sha256");
        JsonNode sealed = json.readTree(read(capturePath(capture).resolve("seal.json"), 64 * 1024));
        if (!capture.equals(sealed.path("capture_id").asText()) || !manifestDigest.equals(sealed.path("manifest_sha256").asText())
                || !dumpDigest.equals(sealed.path("dump_sha256").asText())) throw rejected();
        byte[] receiptBytes = read(capturePath(capture).resolve("verified-receipt.json"), 64 * 1024);
        if (!sha(receiptBytes).equals(receiptDigest)) throw rejected();
        JsonNode receipt = json.readTree(receiptBytes);
        if (receipt.path("version").asInt() != 1 || !PROTOCOL.equals(receipt.path("protocol").asText())
                || !"VERIFIED".equals(receipt.path("status").asText()) || !capture.equals(receipt.path("capture_id").asText())
                || !manifestDigest.equals(receipt.path("manifest_sha256").asText()) || !dumpDigest.equals(receipt.path("dump_sha256").asText())
                || !receipt.path("run_id").asText().matches("[a-f0-9]{32}")
                || !receipt.path("helpers_ended").isBoolean() || !receipt.path("helpers_ended").asBoolean()
                || !receipt.path("inventory_verified").isBoolean() || !receipt.path("inventory_verified").asBoolean()
                || !receipt.path("content_verified").isBoolean() || !receipt.path("content_verified").asBoolean()
                || !receipt.path("restore_verified").isBoolean() || receipt.path("restore_verified").asBoolean()) throw rejected();
        digestField(receipt, "snapshot_id"); digestField(receipt, "repository_id"); digestField(receipt, "content_manifest_sha256");
        Instant verified = Instant.parse(text(receipt, "verified_at"));
        if (verified.isAfter(Instant.now().plusSeconds(60)) || verified.isBefore(Instant.now().minus(Duration.ofHours(6)))) throw rejected();
        ObjectNode release = json.createObjectNode().put("capture_id", capture).put("receipt_sha256", receiptDigest);
        Path released = capturePath(capture).resolve("release.json");
        if (guard.retainedMode().equals("EMPTY")) {
            if (!Files.exists(released, LinkOption.NOFOLLOW_LINKS)) throw rejected();
            bindOnce(released, release);
        } else {
            if (!guard.retainedMode().equals("PINNED") || !capture.equals(guard.retainedCaptureId())) throw rejected();
            bindOnce(released, release);
            guard.releaseReconciled(capture);
        }
        response.put("status", "RELEASED").put("receipt_sha256", receiptDigest);
    }

    private void manifestResponse(String capture, ObjectNode response) throws IOException {
        response.put("manifest_file", "captures/" + capture + "/manifest.json")
                .put("manifest_sha256", sha(read(capturePath(capture).resolve("manifest.json"), MAX_BYTES)));
    }

    private void bindOnce(Path path, JsonNode value) throws IOException {
        byte[] body = json.writeValueAsBytes(value);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            if (!json.readTree(read(path, 64 * 1024)).equals(value)) throw rejected();
        } else publish(path, body);
    }

    private ObjectNode base(String request, String capture) {
        return json.createObjectNode().put("version", 1).put("protocol", PROTOCOL).put("request_id", request).put("capture_id", capture);
    }

    private Path capturePath(String capture) { return mailbox.resolve("captures").resolve(uuid(capture)); }
    private static String text(JsonNode node, String field) {
        if (!node.path(field).isTextual()) throw rejected();
        return node.path(field).textValue();
    }
    private static String digestField(JsonNode node, String field) {
        String value = text(node, field);
        if (!value.matches("[a-f0-9]{64}")) throw rejected();
        return value;
    }
    private static String uuid(String value) {
        if (value == null || !UUID.fromString(value).toString().equals(value)) throw rejected();
        return value;
    }
    private static IllegalStateException rejected() { return new IllegalStateException("Private capture control rejected"); }

    static String sha(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (Exception exception) { throw rejected(); }
    }

    private void directory(Path path) throws IOException {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            if (posix()) Files.createDirectories(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            else Files.createDirectories(path);
        }
        secure(path, true);
    }

    private byte[] read(Path path, int limit) throws IOException {
        secure(path, false);
        if (Files.size(path) > limit) throw rejected();
        byte[] bytes = Files.readAllBytes(path);
        if (bytes.length > limit) throw rejected();
        return bytes;
    }

    private void secure(Path path, boolean directory) throws IOException {
        if (!path.normalize().startsWith(mailbox)) throw rejected();
        for (Path current = path; current != null; current = current.getParent()) if (Files.isSymbolicLink(current)) throw rejected();
        if (directory ? !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) : !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw rejected();
        if (posix()) {
            String required = directory ? "rwx------" : "rw-------";
            if (!Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS).equals(PosixFilePermissions.fromString(required))
                    || !Files.getOwner(path, LinkOption.NOFOLLOW_LINKS).getName().equals(System.getProperty("user.name"))) throw rejected();
        }
    }

    private boolean posix() { return mailbox.getFileSystem().supportedFileAttributeViews().contains("posix"); }

    private void publish(Path destination, byte[] bytes) throws IOException {
        if (bytes.length > MAX_BYTES) throw rejected();
        secure(destination.getParent(), true);
        Path temp = posix()
                ? Files.createTempFile(destination.getParent(), ".publish-", ".tmp", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
                : Files.createTempFile(destination.getParent(), ".publish-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            // Atomic create-if-absent; ATOMIC_MOVE alone may replace an existing destination.
            Files.createLink(destination, temp);
            if (posix()) try (FileChannel channel = FileChannel.open(destination.getParent(), StandardOpenOption.READ)) { channel.force(true); }
        } finally { Files.deleteIfExists(temp); }
    }
}
