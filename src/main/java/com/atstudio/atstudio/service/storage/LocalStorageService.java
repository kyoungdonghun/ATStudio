package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.common.exception.TECHNIC_ERROR;
import com.atstudio.atstudio.common.exception.TechnicException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class LocalStorageService implements StorageService {

    private static final Pattern SAFE_EXTENSION = Pattern.compile("\\.[a-z0-9]{1,10}");
    private static final Pattern OPERATION_ID = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private final String publicPath;
    private final String privatePath;
    private final Map<StorageRoot, Path> roots = new EnumMap<>(StorageRoot.class);

    public LocalStorageService(
            @Value("${app.storage.public-path:${app.storage.base-path:uploads}}") String publicPath,
            @Value("${app.storage.private-path:private-uploads}") String privatePath) {
        this.publicPath = publicPath;
        this.privatePath = privatePath;
    }

    @PostConstruct
    void init() {
        try {
            Path publicRoot = initializeRoot(publicPath);
            Path privateRoot = initializeRoot(privatePath);
            if (publicRoot.equals(privateRoot)
                    || publicRoot.startsWith(privateRoot)
                    || privateRoot.startsWith(publicRoot)) {
                throw new IllegalStateException("Public and private storage roots must be disjoint");
            }
            roots.put(StorageRoot.PUBLIC, publicRoot);
            roots.put(StorageRoot.PRIVATE, privateRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Storage roots could not be initialized", exception);
        }
    }

    @Override
    public String generateKey(String directory, String submittedFilename) {
        String normalizedDirectory = validateRelativeKey(directory);
        String extension = safeExtension(submittedFilename);
        return normalizedDirectory + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    @Override
    public void stage(StorageRoot root, String operationId, String finalKey, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ioFailure();
        }
        Path stagedFile = resolveStaged(root, operationId, finalKey);
        try {
            createSecureDirectories(stagedFile.getParent(), rootPath(root));
            Files.copy(file.getInputStream(), stagedFile);
        } catch (IOException exception) {
            throw ioFailure();
        }
    }

    @Override
    public void promote(StorageRoot root, String operationId, String finalKey) {
        Path stagedFile = resolveStaged(root, operationId, finalKey);
        Path finalFile = resolveForWrite(root, finalKey);
        try {
            requireRegularFile(stagedFile);
            createSecureDirectories(finalFile.getParent(), rootPath(root));
            Files.move(stagedFile, finalFile, StandardCopyOption.ATOMIC_MOVE);
            pruneEmptyStagingDirectories(stagedFile.getParent(), stagingOperationRoot(root, operationId));
        } catch (AtomicMoveNotSupportedException exception) {
            throw ioFailure();
        } catch (IOException exception) {
            throw ioFailure();
        }
    }

    @Override
    public StorageDeleteResult delete(StorageRoot root, String relativeKey) {
        return deletePath(resolveExistingCandidate(root, relativeKey));
    }

    @Override
    public StorageDeleteResult deleteStaged(StorageRoot root, String operationId, String finalKey) {
        Path stagedFile = resolveStaged(root, operationId, finalKey);
        StorageDeleteResult result = deletePath(stagedFile);
        try {
            pruneEmptyStagingDirectories(stagedFile.getParent(), stagingOperationRoot(root, operationId));
        } catch (IOException exception) {
            return StorageDeleteResult.FAILED;
        }
        return result;
    }

    @Override
    public Resource loadAsResource(StorageRoot root, String relativeKey) {
        Path file = resolveExistingCandidate(root, relativeKey);
        try {
            requireRegularFile(file);
            Path realFile = file.toRealPath(LinkOption.NOFOLLOW_LINKS);
            if (!realFile.startsWith(rootPath(root))) {
                throw ioFailure();
            }
            return new UrlResource(realFile.toUri());
        } catch (IOException exception) {
            throw ioFailure();
        }
    }

    @Override
    public String getUrl(StorageRoot root, String relativeKey) {
        if (root != StorageRoot.PUBLIC) {
            throw ioFailure();
        }
        return "/uploads/" + validateRelativeKey(relativeKey);
    }

    private Path initializeRoot(String configuredPath) throws IOException {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException("Storage root must be configured");
        }
        Path candidate = Path.of(configuredPath).toAbsolutePath().normalize();
        Files.createDirectories(candidate);
        if (Files.isSymbolicLink(candidate) || !Files.isDirectory(candidate, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException("Storage root must be a real directory");
        }
        return candidate.toRealPath(LinkOption.NOFOLLOW_LINKS);
    }

    private Path resolveForWrite(StorageRoot root, String relativeKey) {
        Path target = rootPath(root).resolve(validateRelativeKey(relativeKey)).normalize();
        validateInsideRoot(root, target);
        return target;
    }

    private Path resolveExistingCandidate(StorageRoot root, String relativeKey) {
        Path target = resolveForWrite(root, relativeKey);
        validateNoSymbolicLinkSegments(rootPath(root), target);
        return target;
    }

    private Path resolveStaged(StorageRoot root, String operationId, String finalKey) {
        validateOperationId(operationId);
        Path target = stagingOperationRoot(root, operationId)
                .resolve(validateRelativeKey(finalKey))
                .normalize();
        validateInsideRoot(root, target);
        return target;
    }

    private Path stagingOperationRoot(StorageRoot root, String operationId) {
        validateOperationId(operationId);
        return rootPath(root).resolve(".staging").resolve(operationId).normalize();
    }

    private String validateRelativeKey(String value) {
        if (value == null || value.isBlank()
                || value.indexOf('\0') >= 0
                || value.contains("\\")
                || value.contains(":")
                || value.startsWith("/")
                || value.endsWith("/")
                || value.contains("//")) {
            throw ioFailure();
        }
        Path path;
        try {
            path = Path.of(value);
        } catch (RuntimeException exception) {
            throw ioFailure();
        }
        if (path.isAbsolute() || !path.normalize().equals(path)) {
            throw ioFailure();
        }
        for (Path part : path) {
            String name = part.toString();
            if (name.equals(".") || name.equals("..") || name.isBlank()) {
                throw ioFailure();
            }
        }
        return path.toString().replace('\\', '/');
    }

    private String safeExtension(String submittedFilename) {
        if (submittedFilename == null) {
            return "";
        }
        String normalized = submittedFilename.toLowerCase(Locale.ROOT);
        int dot = normalized.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String extension = normalized.substring(dot);
        return SAFE_EXTENSION.matcher(extension).matches() ? extension : "";
    }

    private void validateOperationId(String operationId) {
        if (operationId == null || !OPERATION_ID.matcher(operationId).matches()) {
            throw ioFailure();
        }
    }

    private Path rootPath(StorageRoot root) {
        Path path = roots.get(root);
        if (path == null) {
            throw ioFailure();
        }
        return path;
    }

    private void validateInsideRoot(StorageRoot root, Path target) {
        if (!target.startsWith(rootPath(root))) {
            throw ioFailure();
        }
    }

    private void createSecureDirectories(Path directory, Path root) throws IOException {
        if (!directory.startsWith(root)) {
            throw ioFailure();
        }
        Path current = root;
        for (Path part : root.relativize(directory)) {
            current = current.resolve(part);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
                if (Files.isSymbolicLink(current)
                        || !Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS)) {
                    throw ioFailure();
                }
            } else {
                Files.createDirectory(current);
            }
        }
    }

    private void validateNoSymbolicLinkSegments(Path root, Path target) {
        Path current = root;
        for (Path part : root.relativize(target)) {
            current = current.resolve(part);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw ioFailure();
            }
        }
    }

    private void requireRegularFile(Path file) {
        validateNoSymbolicLinkSegments(file.getRoot() == null ? file : rootFor(file), file);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw ioFailure();
        }
    }

    private Path rootFor(Path file) {
        return roots.values().stream()
                .filter(file::startsWith)
                .findFirst()
                .orElseThrow(this::ioFailure);
    }

    private StorageDeleteResult deletePath(Path target) {
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            return StorageDeleteResult.NOT_FOUND;
        }
        requireRegularFile(target);
        try {
            Files.delete(target);
            return StorageDeleteResult.DELETED;
        } catch (IOException exception) {
            return StorageDeleteResult.FAILED;
        }
    }

    private void pruneEmptyStagingDirectories(Path start, Path operationRoot) throws IOException {
        Path current = start;
        while (current != null && current.startsWith(operationRoot)) {
            if (!Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
                if (current.equals(operationRoot)) {
                    return;
                }
                current = current.getParent();
                continue;
            }
            try (var children = Files.list(current)) {
                if (children.findAny().isPresent()) {
                    return;
                }
            }
            Files.deleteIfExists(current);
            if (current.equals(operationRoot)) {
                return;
            }
            current = current.getParent();
        }
    }

    private TechnicException ioFailure() {
        return new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);
    }
}
