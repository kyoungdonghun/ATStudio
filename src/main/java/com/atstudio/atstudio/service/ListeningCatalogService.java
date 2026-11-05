package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.entity.SiteSetting;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.repository.SiteSettingRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.service.storage.StorageReferenceMutation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ListeningCatalogService {
    public static final int MAX_IDS = 1000;
    public static final int MAX_BYTES = 16384;
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    @org.springframework.beans.factory.annotation.Autowired
    private com.atstudio.atstudio.repository.AdminOperationAuditLogRepository auditLogRepository;
    private final ListeningProperties properties;
    private final SiteSettingRepository settings;
    private final TrackRepository tracks;
    private final AdminOperationAuditService audit;

    public record Catalog(long revision, List<Long> trackIds) {
        public Catalog { trackIds = List.copyOf(trackIds); }
    }
    public record View(String source, long revision, List<Long> trackIds, String digest) { }
    public record Update(long expectedRevision, List<Long> trackIds, String reason, boolean creditsConfirmed, String attemptId) {
        // Internal fixture/caller convenience. HTTP deserialization uses the canonical constructor and requires its ID.
        public Update(long revision, List<Long> ids, String reason, boolean confirmed) {
            this(revision, ids, reason, confirmed, UUID.randomUUID().toString());
        }
    }
    public record AuditAfter(String attemptId, View catalog) { }
    public record Reconcile(String attemptId, long expectedRevision, List<Long> beforeTrackIds,
            String beforeDigest, List<Long> trackIds, String reason) { }
    public record Receipt(Long auditId, Long actorId, String attemptId, String reason,
            View before, View after, String recordedAt) { }
    public record Reconciliation(String status, Receipt receipt) {
        static Reconciliation unknown() { return new Reconciliation("UNKNOWN", null); }
    }

    public boolean databaseMode() {
        return properties.isEnabled() && properties.getCatalogSource() == ListeningProperties.CatalogSource.DATABASE;
    }

    /** Called inside the public content transaction; never caches or falls back. */
    public Catalog snapshot() {
        if (!databaseMode()) return new Catalog(0, properties.getTrackIds().stream().sorted().toList());
        requireTransaction();
        try {
            return decode(settings.findByKeyForSnapshot(ReservedSettingPolicy.CATALOG_KEY)
                    .orElseThrow(ListeningCatalogService::unavailable));
        } catch (org.springframework.dao.DataAccessException failure) { throw unavailable(); }
    }

    public SiteSetting lockForWrite() {
        requireTransaction();
        SiteSetting row = settings.findByKeyForUpdate(ReservedSettingPolicy.CATALOG_KEY)
                .orElseThrow(ListeningCatalogService::unavailable);
        decode(row);
        return row;
    }

    public void requireUnselected(Long id) {
        if (databaseMode() && snapshot().trackIds().contains(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unpublish selected track before editing");
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public View get() { return view(snapshot()); }

    @Transactional
    @StorageReferenceMutation
    public View replace(Update request, Long actorId) {
        if (!databaseMode()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Static catalog is read-only");
        if (request == null || !request.creditsConfirmed() || request.reason() == null
                || request.reason().isBlank() || request.reason().length() > 500) bad("Confirm credits and provide a reason");
        requireAttemptId(request.attemptId());
        List<Long> ids = validateIds(request.trackIds());
        SiteSetting row = lockForWrite();
        Catalog before = decode(row);
        if (!attempts(row.getId(), request.attemptId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attempt ID already recorded; reconcile without replay");
        }
        if (request.expectedRevision() != before.revision() || before.revision() == Long.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Catalog revision conflict; reload before deciding");
        }
        SortedSet<Long> lockIds = new TreeSet<>(before.trackIds()); lockIds.addAll(ids);
        for (Long id : lockIds) {
            Track track = tracks.findByIdForUpdate(id).orElse(null);
            if (ids.contains(id) && (track == null || !track.isActive() || !track.isStreamReady()
                    || track.getPendingAudioFile() != null || track.getAudioClaimToken() != null
                    || track.getAudioProcessingState() == com.atstudio.atstudio.entity.enums.AudioProcessingState.PENDING
                    || track.getAudioProcessingState() == com.atstudio.atstudio.entity.enums.AudioProcessingState.PROCESSING)) {
                bad("Selected tracks must exist, be active and have no pending audio generation");
            }
        }
        Catalog after = new Catalog(before.revision() + 1, ids);
        String encoded = encode(after);
        row.updateValue(encoded);
        audit.recordListeningCatalogSuccess(actorId, row.getId(), auditState(before), auditAfter(request.attemptId(), after), request.reason());
        return view(after);
    }

    private java.util.List<com.atstudio.atstudio.entity.AdminOperationAuditLog> attempts(Long settingId, String attemptId) {
        return auditLogRepository.findCatalogAttempts(settingId, "%" + attemptId + "%",
                org.springframework.data.domain.PageRequest.of(0, 2));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Reconciliation reconcile(Reconcile request, Long actorId) {
        if (!databaseMode() || request == null || actorId == null) return Reconciliation.unknown();
        try {
            requireAttemptId(request.attemptId());
            var proposed = validateIds(request.trackIds());
            var expectedBefore = new Catalog(request.expectedRevision(), validateIds(request.beforeTrackIds()));
            if (request.expectedRevision() < 0 || request.reason() == null || request.reason().isBlank()
                    || request.reason().length() > 500 || !digest(encode(expectedBefore)).equals(request.beforeDigest())) {
                return Reconciliation.unknown();
            }
            SiteSetting row = settings.findByKeyForSnapshot(ReservedSettingPolicy.CATALOG_KEY)
                    .orElseThrow(ListeningCatalogService::unavailable);
            Catalog current = decode(row);
            var matches = attempts(row.getId(), request.attemptId());
            if (matches.size() != 1) return Reconciliation.unknown();
            var log = matches.get(0);
            if (!actorId.equals(log.getActorUserId()) || !request.reason().equals(log.getReasonNote())
                    || log.getId() == null || log.getCreatedAt() == null
                    || log.getAction() != com.atstudio.atstudio.entity.enums.AdminOperationAuditAction.LISTENING_CATALOG_REPLACE
                    || log.getTargetType() != com.atstudio.atstudio.entity.enums.AdminOperationAuditTargetType.LISTENING_CATALOG
                    || log.getOutcome() != com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome.SUCCEEDED
                    || !"EXPLICIT_CATALOG_APPROVAL".equals(log.getReasonCode())
                    || !row.getId().equals(log.getTargetId())) return Reconciliation.unknown();
            if (log.getBeforeState() == null || log.getAfterState() == null
                    || log.getBeforeState().getBytes(StandardCharsets.UTF_8).length > MAX_BYTES + 512
                    || log.getAfterState().getBytes(StandardCharsets.UTF_8).length > MAX_BYTES + 1024) return Reconciliation.unknown();
            View before = JSON.readValue(log.getBeforeState(), View.class);
            AuditAfter recorded = JSON.readValue(log.getAfterState(), AuditAfter.class);
            View after = recorded.catalog();
            if (!validAuditView(before) || !validAuditView(after)
                    || !request.attemptId().equals(recorded.attemptId())
                    || before.revision() != request.expectedRevision() || before.revision() == Long.MAX_VALUE
                    || after.revision() != before.revision() + 1 || !before.trackIds().equals(expectedBefore.trackIds())
                    || !before.digest().equals(request.beforeDigest()) || !after.trackIds().equals(proposed)
                    || current.revision() < after.revision()
                    || (current.revision() == after.revision() && !view(current).equals(after))) return Reconciliation.unknown();
            return new Reconciliation("COMMITTED", new Receipt(log.getId(), actorId, recorded.attemptId(),
                    log.getReasonNote(), before, after, log.getCreatedAt().toString()));
        } catch (Exception invalidOrUnavailable) { return Reconciliation.unknown(); }
    }

    private boolean validAuditView(View value) {
        return value != null && "DATABASE".equals(value.source()) && value.revision() >= 0
                && validateIds(value.trackIds()).equals(value.trackIds())
                && digest(encode(new Catalog(value.revision(), value.trackIds()))).equals(value.digest());
    }

    private static void requireAttemptId(String value) {
        try {
            if (value == null || !UUID.fromString(value).toString().equals(value)) bad("A canonical attempt UUID is required");
        } catch (IllegalArgumentException invalid) { bad("A canonical attempt UUID is required"); }
    }

    private String auditAfter(String attemptId, Catalog catalog) {
        try { return JSON.writeValueAsString(new AuditAfter(attemptId, view(catalog))); }
        catch (com.fasterxml.jackson.core.JsonProcessingException invalid) { throw unavailable(); }
    }

    private String auditState(Catalog catalog) {
        try { return JSON.writeValueAsString(view(catalog)); }
        catch (com.fasterxml.jackson.core.JsonProcessingException invalid) { throw unavailable(); }
    }

    /** Uploader nickname is public metadata too; serialize and protect its writer. */
    public void requireUnselectedUploader(Long userId) {
        if (!databaseMode()) return;
        for (Long id : tracks.findIdsByUserId(userId)) {
            requireUnselected(id);
            tracks.findByIdForUpdate(id);
        }
    }

    private View view(Catalog catalog) {
        return new View(databaseMode() ? "DATABASE" : "STATIC", catalog.revision(), catalog.trackIds(), digest(encode(catalog)));
    }

    private Catalog decode(SiteSetting row) {
        try {
            if (!ReservedSettingPolicy.CATALOG_KEY.equals(row.getSettingKey()) || row.getSettingValue() == null
                    || row.getSettingValue().getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) throw unavailable();
            Catalog catalog = JSON.readValue(row.getSettingValue(), Catalog.class);
            if (catalog.revision() < 0 || !validateIds(catalog.trackIds()).equals(catalog.trackIds())) throw unavailable();
            return catalog;
        } catch (Exception invalid) { throw unavailable(); }
    }

    private static List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.size() > MAX_IDS || ids.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(ids).size() != ids.size()) bad("Invalid catalog IDs (positive, unique, maximum 1000)");
        return ids.stream().sorted().toList();
    }

    private static String encode(Catalog catalog) {
        try {
            String json = JSON.writeValueAsString(catalog);
            if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) bad("Catalog exceeds 16 KiB");
            return json;
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) { throw unavailable(); }
    }

    public static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("Catalog requires a DB transaction");
    }
    private static void bad(String reason) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason); }
    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog unavailable; no fallback or implicit bootstrap");
    }
}
