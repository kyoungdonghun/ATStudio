package com.atstudio.atstudio.service.storage;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/** Single-process storage foundation, not a DB snapshot fence. Every business reference writer
 * must be integrated before an exporter can treat freeze as capture readiness.
 * No online capture/export or cross-JVM handoff API is exposed. */
@Component
public class StorageContinuityGuard implements AutoCloseable {
    private final Path directory;
    private final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);
    private final Set<FileRef> pins = new HashSet<>();
    private FileChannel ownerChannel;
    private FileLock ownerLock;
    private BasicFileAttributes expectedState;
    private String captureId = "-";
    private String mode = "EMPTY";
    private int active;
    private boolean draining;
    private boolean poisoned;
    private boolean closed;
    private final java.util.concurrent.locks.ReentrantLock fileMutation = new java.util.concurrent.locks.ReentrantLock();

    public StorageContinuityGuard(@Value("${app.storage.continuity-state-path:}") String path) {
        directory = path == null || path.isBlank() ? null : Path.of(path).toAbsolutePath().normalize();
        if (directory == null) return;
        if (!Path.of(path).isAbsolute()) throw failure();
        try {
            Files.createDirectories(directory);
            rejectLinks(directory);
            Path owner = directory.resolve("owner.lock");
            boolean existing = Files.exists(owner, LinkOption.NOFOLLOW_LINKS);
            if (Files.isSymbolicLink(owner)) throw failure();
            ownerChannel = FileChannel.open(owner, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            ownerLock = ownerChannel.tryLock();
            if (ownerLock == null) throw failure();
            if (Files.exists(statePath(), LinkOption.NOFOLLOW_LINKS)) load();
            else if (existing) throw failure();
            else persist();
        } catch (IOException | RuntimeException exception) {
            closeResources();
            throw failure();
        }
    }

    public static StorageContinuityGuard disabled() { return new StorageContinuityGuard(""); }

    /** A private-root marker prevents silently removing/changing enabled configuration on restart. */
    synchronized void bindRoots(Path publicRoot, Path privateRoot) {
        Path marker = privateRoot.resolve(".continuity-owner");
        try {
            if (directory == null) {
                if (Files.exists(marker, LinkOption.NOFOLLOW_LINKS)) throw failure();
                return;
            }
            if (directory.startsWith(publicRoot) || publicRoot.startsWith(directory)
                    || directory.startsWith(privateRoot) || privateRoot.startsWith(directory)) throw failure();
            String binding = directory + "\n" + publicRoot + "\n" + privateRoot + "\n";
            if (Files.exists(marker, LinkOption.NOFOLLOW_LINKS)) {
                if (Files.isSymbolicLink(marker) || !Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)
                        || !Files.readString(marker).equals(binding)) throw failure();
            } else {
                try (FileChannel channel = FileChannel.open(marker, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    writeFully(channel, binding.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    channel.force(true);
                }
                forceDirectory(privateRoot);
            }
        } catch (IOException exception) { throw failure(); }
    }

    public synchronized Lease enter() {
        checkState();
        if (mode.equals("HALTED") || (depth.get() == 0 && (draining || mode.equals("FROZEN")))) throw new AdmissionClosedException();
        active++;
        depth.set(depth.get() + 1);
        return new Lease(Thread.currentThread());
    }

    /** Internal only: closes admission, drains existing lifecycle leases, then durably freezes. */
    public synchronized String freeze(Duration timeout) throws InterruptedException {
        return freeze(timeout, UUID.randomUUID().toString());
    }

    public synchronized String freeze(Duration timeout, String requestedCaptureId) throws InterruptedException {
        if (requestedCaptureId == null || !UUID.fromString(requestedCaptureId).toString().equals(requestedCaptureId)) throw failure();
        checkState();
        if (directory == null || depth.get() != 0 || draining || !mode.equals("EMPTY")
                || timeout.isNegative() || timeout.compareTo(Duration.ofHours(1)) > 0) throw failure();
        long remaining = timeout.toNanos();
        long start = System.nanoTime();
        draining = true;
        try {
            while (active != 0) {
                if (remaining <= 0) throw new AdmissionClosedException();
                long millis = remaining / 1_000_000;
                int nanos = (int) (remaining % 1_000_000);
                wait(millis, nanos);
                remaining = timeout.toNanos() - (System.nanoTime() - start);
                checkState();
            }
            if (!mode.equals("EMPTY")) throw failure();
            captureId = requestedCaptureId;
            mode = "FROZEN";
            persist();
            return captureId;
        } finally { draining = false; notifyAll(); }
    }

    public synchronized void seal(String id, Set<FileRef> references) {
        requireCapture(id);
        if (!mode.equals("FROZEN")) throw failure();
        Set<FileRef> validated = Set.copyOf(references);
        pins.clear();
        pins.addAll(validated);
        mode = "PINNED";
        persist();
    }

    /** Caller must reconcile completion/abandonment, prove no capture reader remains,
     * and, for HALTED state, independently confirm termination of every uncertain native writer. */
    public synchronized void releaseReconciled(String id) {
        requireCapture(id);
        if (active != 0) throw failure();
        pins.clear();
        mode = "EMPTY";
        captureId = "-";
        persist();
    }

    public synchronized String retainedCaptureId() { checkState(); return captureId; }
    public synchronized String retainedMode() { checkState(); return mode; }
    public synchronized boolean currentThreadAdmitted() { checkState(); return depth.get() > 0; }
    synchronized Path stateDirectory() { return directory; }
    public boolean isConfigured() { return directory != null; }

    /** A native writer could still be alive; neither cleanup nor drain may claim safety. */
    public synchronized void haltForUncertainWriter() {
        checkState();
        if (captureId.equals("-")) captureId = UUID.randomUUID().toString();
        mode = "HALTED";
        if (directory != null) persist();
        notifyAll();
    }

    <T> T mutate(FileRef reference, T deferred, Supplier<T> action) {
        try (Lease ignored = enter()) {
            synchronized (this) { if (pins.contains(reference)) return deferred; }
            // An active lease prevents new pin publication/release. Never hold the admission
            // monitor across I/O: freeze's bounded drain must remain able to time out.
            fileMutation.lock();
            try {
                synchronized (this) { checkState(); }
                return action.get();
            } finally { fileMutation.unlock(); }
        }
    }

    private void requireCapture(String id) {
        checkState();
        if (directory == null || mode.equals("EMPTY") || !captureId.equals(id)) throw failure();
    }

    private Path statePath() { return directory.resolve("capture.state"); }

    private void checkState() {
        if (closed || poisoned) throw failure();
        if (directory == null) return;
        try {
            BasicFileAttributes actual = Files.readAttributes(statePath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!actual.isRegularFile() || actual.size() != expectedState.size()
                    || !actual.lastModifiedTime().equals(expectedState.lastModifiedTime())
                    || !Objects.equals(actual.fileKey(), expectedState.fileKey()) || !ownerLock.isValid()) throw failure();
        } catch (IOException | RuntimeException exception) { poisoned = true; throw failure(); }
    }

    private void load() throws IOException {
        if (!Files.isRegularFile(statePath(), LinkOption.NOFOLLOW_LINKS)) throw failure();
        if (Files.size(statePath()) > 16 * 1024 * 1024) throw failure();
        String contents = Files.readString(statePath());
        int digestAt = contents.lastIndexOf("SHA256:");
        if (digestAt < 1 || !contents.endsWith("\nEND\n")) throw failure();
        String payload = contents.substring(0, digestAt);
        if (!contents.substring(digestAt).equals("SHA256:" + digest(payload) + "\nEND\n")) throw failure();
        List<String> lines = payload.lines().toList();
        if (lines.size() < 3 || !lines.get(0).equals("ATS-CONTINUITY-1")) throw failure();
        captureId = lines.get(1);
        mode = lines.get(2);
        if (!Set.of("EMPTY", "FROZEN", "PINNED", "HALTED").contains(mode)) throw failure();
        if (mode.equals("EMPTY") ? !captureId.equals("-") : !UUID.fromString(captureId).toString().equals(captureId)) throw failure();
        for (String line : lines.subList(3, lines.size())) {
            String[] pieces = line.split(":", 2);
            if (pieces.length != 2 || !pins.add(new FileRef(StorageRoot.valueOf(pieces[0]),
                    new String(Base64.getUrlDecoder().decode(pieces[1]), java.nio.charset.StandardCharsets.UTF_8)))) throw failure();
        }
        if (!mode.equals("PINNED") && !mode.equals("HALTED") && !pins.isEmpty()) throw failure();
        expectedState = Files.readAttributes(statePath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    }

    private void persist() {
        Path temporary = directory.resolve("capture-" + UUID.randomUUID() + ".tmp");
        StringBuilder body = new StringBuilder("ATS-CONTINUITY-1\n").append(captureId).append('\n').append(mode).append('\n');
        pins.stream().sorted(Comparator.comparing(FileRef::toString)).forEach(ref -> body.append(ref.root()).append(':')
                .append(Base64.getUrlEncoder().withoutPadding().encodeToString(ref.key().getBytes(java.nio.charset.StandardCharsets.UTF_8))).append('\n'));
        String checksum = digest(body.toString());
        body.append("SHA256:").append(checksum).append("\nEND\n");
        try {
            if (body.length() > 16 * 1024 * 1024) throw failure();
            rejectLinks(directory);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                writeFully(channel, body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                channel.force(true);
            }
            Files.move(temporary, statePath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            forceDirectory(directory);
            expectedState = Files.readAttributes(statePath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException | RuntimeException exception) { poisoned = true; throw failure(); }
    }

    private static void writeFully(FileChannel channel, byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining()) channel.write(buffer);
    }

    private static String digest(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) { throw failure(); }
    }

    private static void forceDirectory(Path path) throws IOException {
        // Windows cannot open directories through this API; tests there prove process restart,
        // not power-loss durability. Linux target acceptance remains mandatory before use.
        if (!System.getProperty("os.name").startsWith("Windows")) {
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) { channel.force(true); }
        }
    }

    private static void rejectLinks(Path path) {
        for (Path current = path; current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current)) throw failure();
        }
    }

    @Override @PreDestroy
    public synchronized void close() {
        if (active != 0) throw failure();
        closed = true;
        closeResources();
    }

    private void closeResources() {
        try { if (ownerLock != null) ownerLock.release(); } catch (IOException ignored) { }
        try { if (ownerChannel != null) ownerChannel.close(); } catch (IOException ignored) { }
    }

    private static IllegalStateException failure() { return new IllegalStateException("Storage continuity state unavailable"); }

    public record FileRef(StorageRoot root, String key) {
        public FileRef {
            Objects.requireNonNull(root);
            if (key == null || key.isBlank() || key.contains("\\") || key.contains(":") || key.contains("\n")
                    || key.contains("\r") || key.startsWith("/") || key.endsWith("/") || key.contains("//")
                    || !Path.of(key).normalize().equals(Path.of(key)) || Path.of(key).isAbsolute()) throw failure();
            for (Path part : Path.of(key)) if (part.toString().equals(".") || part.toString().equals("..")) throw failure();
        }
    }

    public final class Lease implements AutoCloseable {
        private final Thread thread;
        private boolean released;
        private Lease(Thread thread) { this.thread = thread; }
        @Override public void close() {
            synchronized (StorageContinuityGuard.this) {
                if (released) return;
                if (thread != Thread.currentThread()) throw failure();
                released = true;
                active--;
                int remaining = depth.get() - 1;
                if (remaining == 0) depth.remove(); else depth.set(remaining);
                StorageContinuityGuard.this.notifyAll();
            }
        }
    }

    public static final class AdmissionClosedException extends IllegalStateException {
        public AdmissionClosedException() { super("Storage mutation admission is closed"); }
    }
}
