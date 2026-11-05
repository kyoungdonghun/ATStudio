---
version: 1.2
last_updated: 2026-11-05
project: ATS
owner: SE
category: guide
status: local-candidate-only
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Approved local implementation
---
# Public listening local candidate

WI-20261105-ATS-003 implements static listening; WI-20261105-ATS-008 adds the approved
local DB catalog candidate under REQ-20261105-ATS-003. Neither is a deployed runtime.
This directory and `../nginx/listening-https.conf.template` are unapplied local candidates.
No server, production database, mail, payment or public deployment was run.

## Mode and catalog

The default `app.listening.enabled=false` preserves the selling security chain and catalog.
Pair `app.listening.enabled=true` with the frontend build `VITE_APP_MODE=listening`.
`app.listening.track-ids` is an explicit positive track-ID selection, intersected with active
records for list/count/pagination, detail, batch, stream, public tags and thumbnail ownership.
An empty selection publishes nothing. Selection never activates or edits database records.
Changing the configured selection requires a controlled application restart; this is not a
live catalog editor. The user has now selected ongoing additions/edits after opening.
The reviewed contract is [REQ-20261105-ATS-002: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits).
The DB-mode implementation below is a local candidate; no actual catalog/bootstrap was applied.

Anonymous APIs: GET `/api/tracks`, `/api/tracks/{id}`, `/api/tracks/{id}/stream`,
`/api/tags`, `/api/tags/available`; read-only POST `/api/tracks/batch`; GET selected
active `/uploads/tracks/thumbnail/{key}`. Other APIs/methods are denied by default,
including album metadata, downloads, payments, registration, reset, likes, playlists,
raw files, Swagger and Actuator. Public requests discard Authorization before JWT lookup.
Stream still supports the existing full resource and Range semantics; no anti-copy claim.

## Database catalog mode (WI008)

Set `app.listening.catalog-source=DATABASE` together with `app.listening.enabled=true` to
read the reserved `site_settings` key `listening.catalog.v1`. STATIC is the default and retains
startup ID behavior; disabled listening always keeps selling semantics. No automatic seed,
last-success cache or static fallback exists. Missing/corrupt/read-failed DB catalog is unavailable
(503); an explicitly valid empty catalog publishes zero tracks. A mode switch is an operational
exposure change: revalidate approvals/content before switching back from selling or STATIC to DB.

GET/PUT `/api/tracks/admin/listening-catalog` require exact operator source and ADMIN, also
when the listening security chain is disabled. GET/PUT use ResponseDTO.data:
`{source,revision,trackIds,digest}`. PUT receives the full `trackIds` array, `expectedRevision`,
`reason` (nonblank, <=500 characters), and `creditsConfirmed:true`. IDs must be unique positive
integers, <=1,000 IDs and <=16 KiB serialized JSON; stored lists are sorted. Each selected track
must exist, be active, have a playable resource and have no pending/claimed/processing generation.
The confirmation is an operator assertion, not automated copyright verification. Conflict is409;
validation is400; static PUT is409. There is no bootstrap or row-recreation endpoint.

Every public catalog/content request holds a shared catalog-row lock in one explicit
REPEATABLE_READ transaction through DTO/tag/count/resource-key materialization. Catalog writes
and all annotated storage reference writers share admission -> transaction -> exclusive catalog
row -> ordered track locks. Content updates, deletes, retry/worker swaps, related tag edits and
uploader nickname/withdrawal are blocked for selected tracks in DATABASE listening mode.
Even no-op edit submissions are conservatively rejected: unpublish first. General profile changes
for an uploader with selected tracks also require unpublication because nickname is public metadata.
This restriction does not apply to selling or STATIC mode. Ordinary source/role/field guards remain.
The catalog writer cannot approve a pending replacement and later let its worker silently swap it.

The old H2 REPEATABLE_READ-only experiment observed old catalog plus new track content; the
shared row-lock correction passed the concurrent scenario. H2 is not native MySQL evidence.
The lock is released before streaming/response serialization; DTO values and the selected resource
key are already fixed. There is no lazy content query or second key lookup in the body phase.
Writes may wait for metadata reads. The query lock timeout hint is3seconds; actual database lock
wait/deadlock behavior and server timeout configuration must be tested before deployment.
Lock failure returns an error without publication; never blindly repeat an uncertain PUT.

Public listening API/cover responses use `Cache-Control: no-store`; proxy caching remains off.
After unpublication commits, new requests reject the removed ID. Requests already admitted,
browser buffers and previously downloaded bytes cannot be revoked; a stream may finish or fail
if its old resource is retired. No forced-stop or uninterrupted-playback guarantee is made.

### Reserved setting and audit protection

Generic setting controllers and both getValue overloads/setValue reject the reserved prefix in
all modes. Keys are limited to canonical ASCII letters/digits/underscore/dot/hyphen without
whitespace; existing uppercase keys such as COMPANY_CERT_GUIDE remain supported. Case and
punctuation-folded listening prefixes are reserved; accents, trailing spaces and percent encodings
are rejected. A database-collation equality probe rejects aliases to existing reserved rows, and
persisted keys are checked again before return/update. Native utf8mb4_unicode_ci behavior and
actual column collation remain pre-deployment checks; H2 does not establish MySQL collation.

Catalog revision and `admin_operation_audit_logs` commit together. Action LISTENING_CATALOG_REPLACE,
target LISTENING_CATALOG records setting-row ID, actor, reason, before/after revision+IDs+digest.
Audit failures roll back publication. Older enum readers may reject these new values; do not
roll back to an old JAR without checking audit-reader compatibility. Restore an earlier approved
set as a **new** revision; never lower revision or rewrite historical evidence.

### Bootstrap and uncertain-write recovery (not executed)

A separately authorized bootstrap must pin target/database/schema/collation, confirm the row is
absent (including aliases), freeze ordinary writers, validate exact approved IDs, active/playable
content and rights, then insert exactly one unique row with canonical JSON such as
`{"revision":0,"trackIds":[]}` (empty synthetic example only). Preserve before/after, actor/reason,
DB backup and current artifact pins. Nonempty IDs must be sorted and meet the same API validation.
Do not copy STATIC IDs automatically. Do not bootstrap while serving public traffic or retry a
lost insert result without a read-only reconciliation. Actual data changes require their own approval.

Every PUT now requires a fresh canonical lowercase UUID `attemptId`, persisted in the same
transaction's audit after-state envelope `{attemptId,catalog}`. A repeated UUID is rejected,
not replayed. This is attribution evidence, not an idempotency/retry framework.

If a PUT response is lost, GET proves only current state, not who caused it. Keep HOLD.
The source-plus-ADMIN read-only `POST /api/tracks/admin/listening-catalog/reconcile` accepts
`{attemptId,expectedRevision,beforeTrackIds,beforeDigest,trackIds,reason}`. Response data is
`{status:"UNKNOWN",receipt:null}` or `{status:"COMMITTED",receipt:{auditId,actorId,attemptId,
reason,before,after,recordedAt}}`. It never writes. Exact actor, before/proposal/reason,
revision sequence and canonical digests must match one valid successful audit record.
Candidate lookup uses the UUID substring, at most two rows and a query timeout hint;
whitespace variants and malformed duplicates therefore fail closed. Native database scan
cost and timeout enforcement remain unverified. Missing, duplicated, malformed, wrong-actor
or contradictory evidence means UNKNOWN, never failed or safe-to-retry.

A later current revision does not erase a proven historical commit. Current revision below
the receipt, or same revision with a different state, remains UNKNOWN. UI must validate the
full receipt against its durable journal, preserve that journal, and separately obtain a
fresh GET before any new operator decision. No automatic PUT replay or rollback follows
reconciliation. Without a valid receipt HOLD remains; there is no checkbox/manual success
substitute or general audit viewer. Pre-correction audits without attempt UUID cannot use
this recovery path and require separately authorized evidence investigation.

DATABASE upload/edit preparation now holds storage admission without a catalog write lock:
analysis, thumbnail canonicalization and generated-file staging happen outside a transaction.
A separately proxied short final transaction obtains catalog then track locks, rechecks
selection and the captured content/generation stamp, and attaches prepared writes. Publication
during preparation rejects final attachment and cleans prepared files. Old keys are deleted
only after commit. Admission spans preparation, commit/rollback and generated-handle cleanup;
FREEZE drains that whole interval. STATIC/selling retain their prior transactional paths.

Catalog mutations are included in StorageReferenceMutation FREEZE admission; leases span commit.
The bounded adapter must include site_settings and admin_operation_audit_logs in the same dump.
Capture success does not establish restored-catalog verification. See the separate operating decision.

## Operator access

`app.listening.operator-addresses` contains exact canonical client IP literals (empty by
default; loopback cannot grant access). The existing trusted identity resolver accepts
`X-ATStudio-Client-IP` only from configured loopback proxy peers with its feature enabled.
Use `server.forward-headers-strategy=none`. The supplied existing `proxy.conf.template`
**overwrites** that internal header with nginx `$remote_addr`, discards incoming forwarding
headers, and never appends client-supplied assertions. Keep upstream bound to loopback,
firewall internal ports, disallow an untrusted local proxy, and configure the real exact
operator addresses in both app settings and `restricted-access.conf`. Do not put proxy
loopback addresses in the operator list. Missing/duplicate/invalid assertions fall back
to loopback and therefore fail closed. A remote caller's spoofed header is ignored.

Allowed source-gated entry points: `/login`, the small admin track SPA set, POST auth
login/refresh, and GET public capabilities. Remaining operator APIs also require ADMIN:
GET users/me, tags/admin (full taxonomy), tracks/admin and detail/audio-status;
POST tracks, audio retry and auth/logout; PUT tracks/{id}; GET/PUT the dedicated listening catalog and POST its read-only reconcile endpoint. Source approval alone never
permits those operations. Delete, tag mutation and unrelated administration are excluded.
Operator APIs retain existing service validation, upload/processing and storage guards.
GET `/api/tracks/admin/{id}/thumbnail` returns a private no-store resource for the
authenticated operator blob preview, including inactive/nonselected tracks. Public raw
thumbnail URLs remain selected-only.

## Deployment and operating gates

The nginx example reuses existing rate/connection zones and proxy snippet. Render and
validate `nginx -t`, raw/encoded path variants, methods, external source ACL, TLS, real
browser full playback/seek, and actual artifact identity on a separately approved target.
MockMvc does not establish native nginx behavior. Do not combine this template with a
public static `/uploads` alias. The listen template alone is unsafe unless the matching
backend mode and catalog settings are enabled and verified; do not publish it independently.

The example suppresses general scheduled business work via existing audio-only settings
and retains the audio worker needed for operator uploads. It does not activate mail,
backups, subscriptions or payment jobs. Verify effective external overrides before any
future start. Keep service ownership/continuity, chosen maintenance windows, recovery
point and current-catalog restore evidence in the lead's OPERATING-DECISION document.
Do not invoke the old PHASE-B single-shot capture as an always-on service manager.

No actual catalog IDs, client addresses, rights, publication dates, credentials or public
URL are supplied here. Those are future approved operational inputs. Historical artifacts
and evidence remain unchanged; the new build is recorded in the WI evidence pack.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
