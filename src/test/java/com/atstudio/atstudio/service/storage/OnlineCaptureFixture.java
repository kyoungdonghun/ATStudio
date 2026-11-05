package com.atstudio.atstudio.service.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Synthetic private-mailbox peer for Python wire tests; no application context or database. */
public final class OnlineCaptureFixture {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath();
        Files.createDirectories(root);
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            LocalStorageService storage = new LocalStorageService(root.resolve("public").toString(), root.resolve("private").toString());
            storage.setContinuity(guard);
            storage.init();
            Files.createDirectories(root.resolve("public/tracks"));
            Files.createDirectories(root.resolve("private/attachments"));
            if (!Files.exists(root.resolve("public/tracks/synthetic.wav"))) Files.write(root.resolve("public/tracks/synthetic.wav"), new byte[]{1,2,3,4});
            if (!Files.exists(root.resolve("private/attachments/synthetic.txt"))) Files.writeString(root.resolve("private/attachments/synthetic.txt"), "synthetic-private");
            CaptureReferenceSource references = () -> List.of(
                    new CaptureReferenceSource.Reference("PUBLIC", "tracks/synthetic.wav", true, true),
                    new CaptureReferenceSource.Reference("PRIVATE", "attachments/synthetic.txt", true, true));
            var bridge = new StorageCaptureMailbox(root.resolve("control"), guard, references,
                    root.resolve("public"), root.resolve("private"));
            Files.writeString(root.resolve("fixture-ready"), "ready");
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.MINUTES.toNanos(3);
            while (!Files.exists(root.resolve("fixture-stop")) && System.nanoTime() < deadline) {
                bridge.processOnce();
                Thread.sleep(10);
            }
        }
    }
}
