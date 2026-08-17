---
version: 30.10
last_updated: 2026-08-14
project: ATS
owner: SA
category: design
status: confirmed
dependencies:
  - path: ../../src/main/java/com/atstudio/atstudio/controller/
    reason: Authoritative method-level mapping source
  - path: ../../src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
    reason: Authoritative route authorization source
  - path: ../../frontend/src/router/index.tsx
    reason: Active SPA route consumers
  - path: db-schema.md
    reason: Current persistence contract
---

# ATStudio API Specification v30.10

## Current Contract

The current V1 backend exposes **152 method-level mappings across 25 controller
classes**. This count is derived from the current Java source by counting
method-level `@GetMapping`, `@PostMapping`, `@PutMapping`,
`@PatchMapping`, and `@DeleteMapping` annotations. Class-level
`@RequestMapping` declarations are not counted.

| Verb      |   Count |
| --------- | ------: |
| GET       |      77 |
| POST      |      41 |
| PUT       |      20 |
| DELETE    |      14 |
| PATCH     |       0 |
| **Total** | **152** |

`SecurityConfig` is authoritative for authorization. Controller annotations are
authoritative for paths and verbs. OpenAPI output generated from the running
application is authoritative for request and response schemas.

## V1 Boundaries

- The React SPA is the only active UI. `SpaForwardController` provides SPA
  deep-link forwarding; it is not an SSR or Thymeleaf compatibility layer.
- Public Listening streams the complete active Track through
  `GET /api/tracks/{trackId}/stream`. Official Download remains a separate,
  authorized path with License, quota, history, and atomic count behavior.
- Play History is browser-local under `localStorage` key `playHistory`.
  There is no server Play History API or persistence contract.
- Download History uses `/api/downloads/history`; there is no Download Queue
  API or persistence contract.
- Subscription payment is card recurring-only through Toss billing agreement
  endpoints. Persisted provider identity is `TOSS`.
- The recurring, status-lookup, and refund provider interfaces remain
  provider-neutral extension boundaries. No second provider is active in V1.
- Direct user subscription creation and legacy payment prepare/confirm/cancel
  contracts do not exist.
- Direct ADMIN update/cancel mappings under `/api/user-subscriptions/{id}` are
  retired. General local entitlement correction uses the explicit ADMIN
  preview, request, approval, and execution workflow and never charges or
  refunds through a payment provider.
- Each correction mutation pessimistically reloads the actor after its existing
  domain/correction locks and immediately before changing state. Role-change,
  ADMIN-withdrawal, and correction request/approval/execution rejections create
  minimal audits in an independent transaction without replacing the API's
  original stable business error.
- Rejection audits retain stable action, target, actor when available, error
  code, and bounded state while persisting a null `reasonNote`. Required
  operator text remains in successful role-change audit and the authoritative
  correction workflow/success context.
- Existing-Track audio analysis is exposed only as a read-only ADMIN dry-run.
  Applying a duration backfill remains a separately approved operation.
- Track create and replacement audio accepts MP3 and WAV only. On non-iOS
  platforms, the active SPA advertises those two formats through the native
  picker hint. On iOS, it omits that hint to avoid valid MP3 files being
  disabled by UTI matching while JavaScript still rejects every non-MP3/WAV
  selection and clears the rejected input.
- `PUT /api/tracks/{trackId}` replaces Tag associations only when multipart
  field `replaceTags=true`. A true value with no `tagIds` clears associations;
  false or omitted preserves them for non-UI callers, including when `tagIds`
  is present. The Track edit SPA always sends explicit replace intent.
- `PUT /api/albums/{id}` preserves description for callers that omit the
  multipart `description` field. A present blank value is an explicit clear;
  both active Album edit entry points send the field on update.
- `GET /api/tags/{tagId}/deletion-impact` is ADMIN-only and returns only Tag
  identity (`id`, `name`, `type`) plus `trackAssociationCount`. It performs no
  deletion and exposes no associated Track details.
- Album and Playlist thumbnail uploads accept the existing bounded JPEG/PNG
  input contract, decode and re-encode supplied images once, and persist only a
  generated `.jpg` key with canonical JPEG bytes. Their public static paths use
  fixed JPEG, `nosniff`, sandboxed Content Security Policy, and same-origin
  resource-policy headers. Downstream static resource MIME inference cannot
  replace the fixed JPEG type, including for retained non-JPEG-extension keys;
  unrelated uploads do not inherit those headers.
- Notice attachments retain the current accepted-file behavior but new objects
  are stored under the private root. Public download remains available only at
  `GET /api/notices/{noticeId}/attachments/{attachmentId}` and forces an
  octet-stream attachment with private/no-store, `nosniff`, sandboxed Content
  Security Policy, and same-origin resource-policy headers. The `filename*`
  value percent-encodes CRLF and disposition delimiters so filename data cannot
  inject another response header. Exact type, count, and byte limits remain
  pending WI-066; no retained-file migration is part of this contract.
- `GET /api/notices/{noticeId}/admin` is ADMIN-only and returns only the edit
  projection: `title`, `content`, `isPinned`, and attachment metadata. It uses
  one read projection and does not increment public `viewCount`. Public
  `GET /api/notices/{noticeId}` retains its existing one-increment contract.

### Binary Download Contract

- Successful binary-download clients normalize the response into one shared
  `BinaryDownload` result: a non-empty `Blob`, a safe filename, and a
  normalized content type. The helper accepts the installed Axios header shape
  (`AxiosHeaders.get`) and plain header maps, and parses RFC 5987 `filename*`
  plus quoted or basic `filename` disposition parameters.
- A safe server response filename takes precedence over caller metadata. A
  malformed, Unicode-control, traversal-like, blank, or dot-only response
  filename is never used for the browser action; it selects the deterministic
  fallback derived from the resource's stable ID and validated metadata.
  Response `Content-Type` takes precedence over the Blob type, followed by
  `application/octet-stream`; callers do not override a valid server type or
  synthesize a filename after normalization.
- A non-Blob or zero-byte response is a download failure and creates no object
  URL or browser action. HTTP error Blobs remain on the canonical asynchronous
  `getApiErrorCode()` path. Track consumers use that Blob-aware error
  normalization for `NO_ACTIVE_SUBSCRIPTION`, `DOWNLOAD_LIMIT_EXCEEDED`,
  cancellation where the caller owns it, and unknown failures.
- Each Track-download entry point synchronously claims its current
  projection-and-Track identity before its first awaited request. The same
  visible identity therefore invokes at most one request until the exact claim
  settles; guarded release permits a later retry without allowing stale cleanup
  to release a newer owner.
- Download History shares one `{readKey, trackId}` claim registry between
  single and bulk actions. An already claimed Track is skipped before bulk
  result accounting, while distinct Track IDs continue. An all-skipped bulk
  action publishes no competing result or count refresh.

Question attachments and Company Certification documents remain private,
authorized `StreamingResponseBody` responses. The controller resolves the
service `Resource` only after the existing authorization/ownership checks,
transfers it without a controller-sized intermediate byte array, and closes the
input stream after transfer. Both responses retain UTF-8 encoded attachment
disposition, `application/octet-stream`, `no-store, private`, `Pragma:
no-cache`, `nosniff`, sandbox CSP, and `Accept-Ranges: none`.

For Company Certification, `DOCUMENT_ACCESS_GRANTED` records authorized private
resource access at authorization/resource resolution; it is not evidence of
completed client byte delivery. This WI does not decide whether a durable grant
or completed bytes define download success, a bulk-download ceiling, or
route-outliving cancellation/ownership policy. Browser activation and client
feedback must not be documented as durable server completion.

## Controller Inventory

| Controller                                  | Mappings | Current boundary                                                                              |
| ------------------------------------------- | -------: | --------------------------------------------------------------------------------------------- |
| `AdminPaymentController`                    |       27 | ADMIN payment ledgers, incidents, settlement, refund, and entitlement correction              |
| `AdminSettingController`                    |        1 | ADMIN site-setting upsert                                                                     |
| `AdminStatsController`                      |        1 | ADMIN dashboard statistics                                                                    |
| `AdminTrackAudioAnalysisController`         |        1 | ADMIN read-only existing-Track audio-analysis dry-run                                         |
| `AdminUserSubscriptionCorrectionController` |        7 | ADMIN local subscription correction workflow                                                  |
| `AdminWhitelistChannelController`           |        5 | ADMIN whitelist review, export, and owner-scoped recovery                                     |
| `AlbumController`                           |        8 | Public album reads and ADMIN mutations                                                        |
| `AuthController`                            |        7 | Login, logout, refresh, social auth, email and password flows                                 |
| `CompanyCertificationController`            |        7 | BUSINESS submission and ADMIN review/document access                                          |
| `DownloadController`                        |        2 | Current user's download history and downloaded Track IDs                                      |
| `LicenseController`                         |        4 | User and ADMIN License reads                                                                  |
| `LikeController`                            |        6 | Track and album likes                                                                         |
| `NoticeController`                          |        7 | Public Notice reads/download plus ADMIN non-counting edit read and mutations                  |
| `PaymentController`                         |        6 | USER-only recurring billing agreement lifecycle and read-only outcome recovery                |
| `PlaylistController`                        |        9 | Subscriber playlist CRUD and Track membership                                                 |
| `QuestionController`                        |        7 | Inquiry, answer, attachment, status, and deletion                                             |
| `SettingController`                         |        1 | Public site-setting read                                                                      |
| `SpaForwardController`                      |        1 | SPA deep-link forwarding                                                                      |
| `SubscriptionController`                    |        3 | Public active plans and ADMIN all-plan read                                                   |
| `TagController`                             |        6 | Public reads, ADMIN deletion impact, and ADMIN mutations                                      |
| `TrackController`                           |       10 | Public Track reads/listening/batch hydration and protected create/update/download/admin reads |
| `UserController`                            |        9 | Registration, profile, password, withdrawal, and ADMIN user operations                        |
| `UserSubscriptionController`                |        5 | My subscription lifecycle plus ADMIN list read                                                |
| `UtilController`                            |        6 | Availability, download count, change preview, and public capabilities                         |
| `WhitelistChannelController`                |        6 | User whitelist draft/request/primary lifecycle                                                |

## Route Inventory

### Admin Payment (27)

- `GET /api/admin/payments/orders`
- `GET /api/admin/payments/billing-agreements`
- `GET /api/admin/payments/subscription-payments`
- `GET /api/admin/payments/reconciliation`
- `GET /api/admin/payments/reconciliation-incidents`
- `PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status`
- `GET /api/admin/payments/receipts`
- `GET /api/admin/payments/operation-audit-logs`
- `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}`
- `GET /api/admin/payments/refunds`
- `GET /api/admin/payments/refunds/{refundId}`
- `POST /api/admin/payments/refunds`
- `POST /api/admin/payments/refunds/{refundId}/approve`
- `POST /api/admin/payments/refunds/{refundId}/execute`
- `POST /api/admin/payments/entitlement-correction-preview`
- `GET /api/admin/payments/entitlement-corrections`
- `GET /api/admin/payments/entitlement-corrections/{correctionId}`
- `POST /api/admin/payments/entitlement-corrections`
- `POST /api/admin/payments/entitlement-corrections/{correctionId}/approve`
- `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute`
- `GET /api/admin/payments/settlements`
- `POST /api/admin/payments/settlements/import`
- `GET /api/admin/payments/settlement-import-attempts`
- `GET /api/admin/payments/settlement-import-attempts/recovery`
- `GET /api/admin/payments/settlement-import-attempts/{attemptId}`
- `POST /api/admin/payments/settlements/reconcile`
- `PUT /api/admin/payments/settlements/{settlementId}/ignore`

#### ADMIN Settlement Integrity Contract

All seven Settlement mappings are ADMIN-only through both `/api/admin/**`
authorization and controller method authorization.

`POST /api/admin/payments/settlements/import` consumes `multipart/form-data`.
It requires the `file` request part and `Idempotency-Key` header and accepts an
optional multipart text part named `note`. A query-only `note` does not bind,
and there is no query-parameter compatibility fallback. `Idempotency-Key` must
contain one canonical lowercase UUIDv4. The server does not trim, case-fold, or
replace the key. It derives a deterministic 64-character SHA-256 digest from
the operation namespace, authenticated ADMIN ID, and canonical key. Only that
owner-scoped opaque digest is persisted. The raw key is not put in a URL or
query parameter, server database column, or application log.

Before claiming an attempt, the server requires a present, nonblank filename
of at most 255 characters ending in `.csv` case-insensitively; a missing/blank
part media type or exactly `text/csv`, `application/csv`,
`text/comma-separated-values`, or `application/vnd.ms-excel`; and nonempty
declared and actual content of at most 5 MiB (5,242,880 bytes). Envelope
violations use the existing invalid-argument response and create no attempt.

After claim, decoding is strict UTF-8 with one optional leading BOM. The CSV
contract supports comma delimiter, double-quote fields, doubled-quote escapes,
quoted comma/newlines, and LF or CRLF records; bare CR, malformed/unbalanced
quotes, duplicate normalized headers, unknown headers, and missing required
headers fail the file. Headers are trim-plus-lowercase normalized and may be in
any order. Every nonblank logical data record counts toward the 1,000-row
ceiling, while header and blank records do not. Exact-width violations become
row errors; the 1,001st data record fails the file. A claimed file-level failure
terminates the attempt with bounded code `CSV_READ_FAILED`.

Required headers are `provider`, `order_id`, `gross_amount`,
`net_settlement_amount`, and `settlement_base_date`. Allowed optional headers
are `provider_payment_key`, `provider_settlement_id`, `refund_amount`,
`fee_amount`, `vat_amount`, `currency`, `settlement_payout_date`,
`provider_status`, and `note`; no other header is accepted.

CSV V1 accepts only exact `TOSS` and `KRW`. `order_id` is required and bounded
to 64 characters; provider payment/settlement identifiers are optional and
bounded to 200; provider status is optional and bounded to 100. These evidence
values reject controls, U+2028/U+2029, and edge whitespace, preserve accepted
case/content, and are never truncated. Amounts use plain nonnegative decimal
notation, scale at most 2, and the `DECIMAL(15,2)` maximum
9,999,999,999,999.99. They are rejected rather than rounded and canonicalized
to exact scale 2 before deduplication and persistence. Dates are strict
`yyyy-MM-dd`; payout must not precede base date. No oldest/future CSV date bound
is implemented.

The first request that claims the unique digest creates one
`PROCESSING` import-attempt row before CSV processing. A repeated POST with the
same ADMIN and key never parses or processes the file again and returns a
state-specific HTTP `409` business error for `PROCESSING`, `COMPLETED`, or
`FAILED`. No file fingerprint or request-payload equivalence policy is part of
this contract.

Attempt recovery is read-only:

- `GET /api/admin/payments/settlement-import-attempts` returns the ADMIN-visible
  paged ledger under `dataList` and `pageInfo`.
- `GET /api/admin/payments/settlement-import-attempts/{attemptId}` returns one
  numeric-ID detail under `data`.
- `GET /api/admin/payments/settlement-import-attempts/recovery` requires the
  same raw key only in `Idempotency-Key`, derives the authenticated ADMIN's
  digest, and returns that owner's outcome under `data`. The raw key is absent
  from the request path and query.

Attempt responses expose `attemptId`, the server-derived `importBatchKey`,
`actorUserId`, state, total/imported/duplicate/failed counts, the optional
bounded operator note, bounded internal failure code, and timestamps. They do
not expose the digest or raw key. File bytes, raw CSV rows, raw Provider
payloads, credentials, and per-row validation errors are not retained in the
attempt ledger. Per-row errors and `statusCounts` remain available only in the
original import response.

`POST /api/admin/payments/settlements/import` can return HTTP `200` with both
durable imported rows and row errors. `failedRows > 0` is a partial result, not
full success. The response carries `importBatchKey`, `totalRows`, `importedRows`,
`skippedDuplicateRows`, `failedRows`, `statusCounts`, `errors`, and
`omittedErrorCount`. Import returns every row-number/message error within the
1,000-row ceiling and sets `omittedErrorCount` to `0`. Valid rows and their row
audit events are persisted; invalid rows are represented by returned errors.
Every normal import response
and every completed attempt satisfies
`totalRows == importedRows + skippedDuplicateRows + failedRows`, while
`sum(statusCounts) == importedRows` because status counts describe only newly
persisted Settlement rows. A completed attempt can therefore represent full,
partial, or all-duplicate processing.

Each candidate Settlement row runs in its own `REQUIRES_NEW` transaction. The
database unique constraint `uq_payment_settlements_deduplication_key` is the
atomic fence. A conflict is classified as a duplicate only when the exception
identifies that exact constraint (or the exact H2 table/column signature) and a
post-rollback read confirms the winning deduplication key. An unrelated
integrity violation is not translated to duplicate. The attempt itself is
claimed through `uq_payment_settlement_import_attempts_key_digest`; MySQL
classification additionally requires SQLState `23000`, error `1062`, and the
exact constraint reference, while H2 requires SQLState `23505` and the exact
constraint/table/column signature.

For one explicit import, the SPA creates one canonical key, stores the pending
key in browser `sessionStorage`, sends it only in the header, and opts the POST
out of authentication replay. A successful POST clears the pending key and
attempts exactly one Settlement-list reload. A partial result renders every
returned error and retains the exact selected `File`, the same DOM file input,
and the operator note; only the request receives the trimmed note. A
zero-failure result clears both React selected-file state and the keyed DOM
input only after that reload succeeds.

If the POST outcome is uncertain, the SPA performs one read-only recovery GET
with the same key and exposes a manual `import result recovery` action for
later reads. It does not issue a second import POST or poll. A stored pending
attempt blocks a new import; corrupt stored recovery state fails closed.
`PROCESSING` keeps the key and correction context. `COMPLETED` or `FAILED`
clears the pending key; only completed zero-failure recovery plus a successful
Settlement-list reload clears the file input. A new key is created only for a
new explicit operator action after terminal recovery.

The import note is optional. The UI limits it to 500 characters, trims a
nonblank value before adding the multipart part, and displays a warning not to
enter personal data, credentials, payment keys, or other sensitive information.
The server stores bounded operator-supplied text in applicable local evidence;
it does not derive a secret for that field and does not provide free-text DLP.
Operators remain responsible for following the warning.

Reconciliation has no import-attempt key, operation identity, cursor, progress
ledger, automatic retry, polling, or recovery endpoint. Omitted dates default
independently to today minus 29 days and today; the inclusive range must be
ordered and at most 90 days. The query selects `DONE` local payments by
`createdAt`, numeric ID ascending, using a 5,001-row probe. A 5,001-row result
returns the existing invalid-argument response before any Settlement/audit
mutation; at most 5,000 rows are processed. There is no separate oldest/future
date rejection.

Reconciliation returns at most the first 200 deterministic row errors and sets
`omittedErrorCount` to `max(0, failedRows - errors.size())`. It classifies an
orderless finalized local payment once as `failedRows` without creating a
Settlement or audit. Every normal response satisfies the same total-count and
status-count invariants. Import and reconciliation can read local
payment/refund evidence but write only Settlement/attempt/audit evidence; they
do not mutate payment, refund, subscription, billing-agreement, receipt, mail,
or Provider state.

`PUT /api/admin/payments/settlements/{settlementId}/ignore` requires JSON
`note` text that is nonblank after trimming and at most 500 characters after
trimming. The DTO enforces this at the HTTP boundary, and the service repeats
the same validation for direct callers. For an otherwise valid request, the
service then requires an authenticated principal with role `ADMIN`, locks and
validates the authoritative non-deleted ADMIN user row, and only then locks the
Settlement row before mutation or audit.

The first valid IGNORE stores the authoritative actor, decision time,
normalized note, and `IGNORED` status and appends one audit event. Every
otherwise-valid repeat, whether its note matches or conflicts, returns
`INVALID_STATE_TRANSITION` under the existing HTTP `400` business-error
mapping. It does not change the first decision fields or append another audit.
The SPA still requires the note and the existing danger confirmation; this WI
added no typed phrase.

This contract includes the implemented DG-067-01 through DG-067-08 decisions
for `CR-031-115`, `CR-031-116`, and `CR-031-118`. QA-INTEG v1.2 and PG v1.1
returned `ACCEPT` with no open P1/P2 finding. DG-067-09B is
`RUN-PASS-CLEANED`: the current recorded fresh-MySQL manifest matched an
independent proof database and all three isolated settlement concurrency tests
passed under Hibernate `ddl-auto=validate`. This does not claim live Provider
behavior, deployment, client acceptance, retained-data migration, or overall
production readiness.

#### ADMIN Refund and Entitlement-Correction Recovery Contract

WI-035 uses the two existing detail mappings below. WI-056 later added three
Settlement import-attempt GET mappings, so `AdminPaymentController` is now at
**27** and the current backend is at **151** method-level mappings:

- `GET /api/admin/payments/refunds/{refundId}`
- `GET /api/admin/payments/entitlement-corrections/{correctionId}`

Both endpoints are ADMIN-only through `SecurityConfig` and controller
authorization. They return the exact requested local durable row under `data`;
an absent ID uses the existing `RESOURCE_NOT_FOUND` error contract. A detail
read performs zero mutation, Provider, finalization, and audit-write calls.

Before either execute POST, the SPA freezes the domain and durable ID, performs
the matching exact detail GET, verifies the returned ID, and requires the fresh
status to be `APPROVED`. An unreadable response, ID mismatch, terminal status,
pre-execution `REQUESTED`, or in-flight status produces zero execute POSTs. A
fresh `APPROVED` row permits at most one POST from the already typed operator
action.

Only these execute wrappers set the frontend `skipAuthReplay` request option:

- `POST /api/admin/payments/refunds/{refundId}/execute`
- `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute`

The response interceptor rejects an opted-out `401` before token refresh,
refresh-queue entry, or request replay. Other protected requests retain the
normal authentication refresh behavior. If an execute response is rejected or
lost, the SPA performs one bounded matching detail GET and never repeats the
execute POST.

For normal authentication replay, every eligible protected request receives an
internal retry marker before it starts refresh or joins the one in-flight
refresh queue. Concurrent first `401` responses share one refresh, and each
marked request is replayed at most once. A replay that receives another `401`
rejects that second response without another refresh, queue entry, or replay.

| UI outcome      | Refund durable predicate                                                                                                               | Entitlement-correction durable predicate                                                            |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| `COMMITTED`     | Exact detail status `SUCCEEDED`                                                                                                        | Exact detail status `SUCCEEDED`                                                                     |
| `FAILED`        | Exact execute/detail status `FAILED` or `CANCELLED`                                                                                    | Exact execute/detail status `FAILED` or `CANCELLED`                                                 |
| `RELOAD_FAILED` | Execute returned `SUCCEEDED`, then the required detail or committed-result list reload failed                                          | Execute returned `SUCCEEDED`, then the required detail or committed-result list reload failed       |
| `UNKNOWN`       | Any unproved result, including `PROCESSING` or `PENDING_PROVIDER_CONFIRMATION`, a failed recovery read, or another non-terminal status | Any unproved result, including `PROCESSING`, a failed recovery read, or another non-terminal status |

`RELOAD_FAILED` preserves the successful execute response and uses distinct
feedback; it is not converted into an execute failure. A later successful exact
detail read may converge to the current durable `COMMITTED`, `FAILED`, or
`UNKNOWN` result.

Refund list hydration creates an exact-ID `UNKNOWN` intent for durable
`PROCESSING` and `PENDING_PROVIDER_CONFIRMATION` rows. Entitlement-correction
list hydration does the same for durable `PROCESSING` rows. The manual `status
again` action calls only the matching detail GET and is deduplicated while that
read is pending. It performs zero approve, execute, Provider, or local mutation
calls.

An `UNKNOWN` intent may clear its pre-execution lock only after an exact detail
read returns `REQUESTED` or `APPROVED`. `REQUESTED` restores approval-only
eligibility. `APPROVED` still requires a later typed action and a new exact
preflight before one execute POST. `UNKNOWN` and `RELOAD_FAILED` otherwise lock
the exact durable row and linked refund/correction mutations across both
domains. Execute, detail-read, intent, and current-view generations fence rapid
clicks and stale detail/list success or failure.

Automatic refund execute retries are `0`; automatic entitlement-correction
execute retries are `0`; recovery GET mutation and Provider calls are `0`.

The existing refund list/detail DTO still carries a raw `idempotencyKey`, actor
emails, and `failureMessage`; the existing entitlement-correction list/detail
DTO still carries actor emails and `failureMessage`. These are pre-existing,
non-blocking ADMIN DTO-minimization follow-ups. WI-035 added no field or schema,
and the recovery UI renders none of those debt fields. A later approved change
should omit or mask the raw idempotency key, retain actor email only for a
documented operational need, and sanitize or omit failure text.

### Settings, Dashboard, and Whitelist Admin (7)

- `PUT /api/admin/settings/{key}`
- `GET /api/admin/stats`
- `GET /api/admin/whitelist-channels`
- `PUT /api/admin/whitelist-channels/{channelId}/status`
- `POST /api/admin/whitelist-channels/export`
- `GET /api/admin/whitelist-channels/exports/recent`
- `GET /api/admin/whitelist-channels/exports/{batchID}`

#### Site Settings Canonical Publication Contract

`PUT /api/admin/settings/{key}` remains ADMIN-authorized and stores the exact
submitted value. While a save is pending, the ADMIN screen freezes the setting
input. After the PUT succeeds, it reads `GET /api/settings/{key}` and shows
success only after that public read returns; the returned value replaces the
submitted draft as the canonical visible value. A failed confirmation read
does not claim success and does not retry the PUT. The company certification
application consumes the same public setting read.

#### ADMIN Whitelist Export Recovery Contract

Whitelist export remains an ADMIN-only mutation. Its request scope consists of
an optional exact status and an optional applied keyword; at least one must be
present after trimming blank keyword input. Confirmation copy is derived from
that applied request scope, not from an unapplied draft input. An all-status
keyword export includes every matching status and changes matching `PENDING`
rows to `EXPORTED`; other matching statuses remain unchanged. An explicit
`PENDING` export changes every matching row to `EXPORTED`, while an explicit
non-`PENDING` export does not change channel status.

`GET /api/admin/whitelist-channels/exports/recent` is the read-only recovery
lookup for an export whose POST response was not definitive. It accepts the
same `status` and `keyword` scope, trims blank keyword input, and compares
keyword scope case-insensitively because channel search is case-insensitive.
After trimming, a non-null keyword is limited to 100 characters, matching
`AdminWhitelistExportRequest`; an oversized or unscoped request returns the
existing HTTP `400 INVALID_ARGUMENT` result before repository access. The
repository query requires the authenticated ADMIN's ID and exact normalized
status/keyword scope, orders by `createdAt` descending then batch ID descending,
and applies a repository-level maximum of 10 rows.

Each recent row contains only `batchId`, `fileName`, `itemCount`, recorded
`status`, recorded `keyword`, and `createdAt`. It does not load or return batch
items or CSV bytes and never performs a channel/status mutation. The existing
`GET /api/admin/whitelist-channels/exports/{batchID}` remains the only byte
replay path.

When the SPA receives a definitive 4xx export result, it reports a normal
failure without claiming that a batch committed. When the result is ambiguous,
it issues one exact-scope recent lookup, performs no automatic second export
POST, and offers only explicit replay of returned candidate batch IDs. The
export request opts out of the shared authentication refresh/replay path so a
`401` cannot cause an interceptor-driven second POST. A failed current list
request clears rows, pagination, and pending row edits; stale list success,
failure, and completion paths cannot replace a newer request state.

### ADMIN Subscription Correction and Track Analysis (8)

- `POST /api/admin/user-subscription-corrections/preview`
- `GET|POST /api/admin/user-subscription-corrections`
- `GET /api/admin/user-subscription-corrections/open`
- `GET /api/admin/user-subscription-corrections/{correctionId}`
- `POST /api/admin/user-subscription-corrections/{correctionId}/approve`
- `POST /api/admin/user-subscription-corrections/{correctionId}/execute`
- `GET /api/admin/tracks/audio-analysis/dry-run`

### Albums, Tracks, Tags, and Playlists (33)

- `GET|POST /api/albums`
- `GET|PUT|DELETE /api/albums/{id}`
- `POST|PUT /api/albums/{id}/tracks`
- `DELETE /api/albums/{id}/tracks/{trackId}`
- `GET|POST /api/tracks`
- `POST /api/tracks/batch`
- `GET|PUT|DELETE /api/tracks/{trackId}`
- `GET /api/tracks/{trackId}/stream`
- `GET /api/tracks/{trackId}/download`
- `GET /api/tracks/admin`
- `GET /api/tracks/admin/{trackId}`
- `GET|POST /api/tags`
- `PUT|DELETE /api/tags/{tagId}`
- `GET /api/tags/{tagId}/deletion-impact`
- `GET /api/tags/available`
- `GET|POST /api/playlists`
- `GET|PUT|DELETE /api/playlists/{playlistId}`
- `POST|PUT /api/playlists/{playlistId}/tracks`
- `DELETE /api/playlists/{playlistId}/tracks/{trackId}`
- `POST /api/playlists/{playlistId}/tracks/batch`

### Authentication and Users (16)

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`
- `POST /api/auth/social/{provider}`
- `GET /api/auth/verify-email`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET|POST /api/users`
- `GET|PUT /api/users/{userId}`
- `GET|PUT|DELETE /api/users/me`
- `PUT /api/users/me/complete-profile`
- `PUT /api/users/me/password`

For password registration, `POST /api/users` requires affirmative Terms and
Privacy consent; Marketing consent is optional and persists only when
affirmative. The consent record holds only the user, consent type,
server-owned version, and server-generated agreement time. Successful password
registration directs the SPA to email verification and creates no session.

`POST /api/auth/login` and `POST /api/auth/refresh` reject an unverified
password account with `EMAIL_VERIFICATION_REQUIRED` before access or refresh
token material is issued or rotated. `POST /api/auth/logout` returns `204` for
confirmed server revocation. A bare `401` or another failed request is an
unconfirmed server outcome; the SPA still performs local logout and presents
its fixed warning. Social login and profile-completion onboarding are unchanged
by WI-068.

`GET /api/users` returns paginated `UserListItemResponse` rows with exactly
`id`, `nickname`, `email`, `userType`, `role`, `isVerified`, and `createdAt`.
`GET|PUT /api/users/{userId}` returns `UserDetailResponse`, which additionally
contains `phonePersonal`, `phoneCompany`, `job`, and `companyName`. ADMIN role
assignment accepts only backend roles `USER` and `ADMIN`; frontend-only `GUEST`
is never an assignable wire value.

The ADMIN SPA opens `GET /api/users/{userId}` in a read-only, latest-request-
owned detail dialog. It renders only `id`, `nickname`, `email`,
`phonePersonal`, `phoneCompany`, `job`, `companyName`, `userType`, `role`,
`isVerified`, and `createdAt`; no credential or token field is part of this
contract. Server ADMIN authorization remains authoritative.

The `updateUserAdmin` request opts out of centralized ADMIN `403` role
synchronization. For the exact `403 ADMIN_ROLE_REQUIRED` response, the User
Management page performs one current-user read, adopts the server-returned
profile through the auth store, and lets the canonical route guard reevaluate
access. The rejected PUT is never replayed. Generic ADMIN `403` requests retain
centralized role synchronization, and the opt-out does not affect `401`
authentication refresh/replay.

### Subscription and Recurring Payment (14)

- `GET /api/subscriptions`
- `GET /api/subscriptions/{subscriptionId}`
- `GET /api/subscriptions/admin`
- `POST /api/payments/billing-agreements/prepare`
- `POST /api/payments/billing-agreements/confirm`
- `GET|DELETE /api/payments/billing-agreements/me`
- `GET /api/payments/orders/{orderId}/outcome`
- `GET /api/payments/subscription-upgrades/outcome?subscriptionId={targetPlanId}&billingCycle={targetCycle}`
- `GET /api/user-subscriptions`
- `GET|PUT|DELETE /api/user-subscriptions/me`
- `POST /api/user-subscriptions/me/reactivate`

#### Subscription Read and Checkout UI Contract

`GET /api/user-subscriptions/me` represents subscription absence only as HTTP
`403 NO_ACTIVE_SUBSCRIPTION`. `GET /api/payments/billing-agreements/me`
represents Billing Agreement absence only as HTTP
`404 BILLING_AGREEMENT_NOT_FOUND`. The SPA may project only those exact
status/code pairs as absence; authentication, authorization, other not-found,
server, and network failures remain visible retryable errors.

Plan, Subscription, Billing Agreement, and change-preview reads are owned by
the latest audience or selection request. Retired async completions cannot
replace a newer audience, subscription, payment-method, or preview state.
Preview read failure remains visible and retryable rather than becoming an
empty preview.

#### Billing Agreement Prepare Contract

`POST /api/payments/billing-agreements/prepare` is USER-only. Every request
requires this header:

```http
Idempotency-Key: 123e4567-e89b-42d3-a456-426614174000
```

The value must be an exact lowercase canonical UUIDv4 matching
`^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$`.
The API does not trim, normalize, case-fold, or generate a replacement. A
missing, blank, uppercase, malformed, non-v4, wrong-variant, oversized, or
control-character value returns HTTP `400`
`PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID` before repository or Provider work.

The request body contains exactly the payment-intent fields below and never
contains the idempotency key:

```json
{
  "subscriptionId": 123,
  "billingCycle": "MONTHLY",
  "purpose": "SUBSCRIBE"
}
```

| Field            | Contract                                                            |
| ---------------- | ------------------------------------------------------------------- |
| `subscriptionId` | Required positive exact plan ID                                     |
| `billingCycle`   | Required `MONTHLY` or `YEARLY`                                      |
| `purpose`        | Required `SUBSCRIBE` or `BILLING_AGREEMENT`; consistency claim only |

The body does not accept plan name, `userType`, amount, callback URL, or client
checkout metadata as authority. Missing, malformed, or unsupported purpose,
including `UPGRADE`, fails request validation before service invocation.

Before calling this endpoint, `/subscriptions/checkout` requires exactly one
`planId`, `userType`, `billingCycle`, and `purpose` query value. The accepted
allowlists are positive integer plan ID, `INDIVIDUAL|BUSINESS`,
`MONTHLY|YEARLY`, and `SUBSCRIBE|BILLING_AGREEMENT`. A missing, malformed,
unsupported, or duplicate required value leaves checkout non-actionable and
causes zero prepare calls. Optional payment-method-registration return context
is accepted only as one complete, same-audience plan/cycle tuple; partial,
malformed, duplicate, or initial-subscription return context also causes zero
prepare calls.

The server resolves the authenticated USER and exact plan, verifies the plan
audience against the authenticated `userType`, resolves the service-enabled
subscription state, and derives authoritative purpose before any
billing-agreement mutation, payment-order save, or recurring Provider prepare:

| Current state                                     | Required request                                  |                          Server amount |
| ------------------------------------------------- | ------------------------------------------------- | -------------------------------------: |
| No service-enabled subscription                   | Exact selected plan/cycle with `SUBSCRIBE`        | Exact server plan price for that cycle |
| `ACTIVE`, or non-expired `CANCELLED` grace period | Exact current plan/cycle with `BILLING_AGREEMENT` |                                    `0` |

Purpose mismatch, cross-audience plan, or current plan/cycle mismatch fails
before those side effects. The server does not use plan name for resolution.

After those authoritative checks, the server derives an authenticated
owner-scoped command digest and persists only
`BILLING_PREPARE:v1:<64-lowercase-hex-sha256>` in
`payment_orders.command_key`. The digest input includes the authenticated owner
and accepted raw UUID, but the stored value discloses neither. The raw key is
never persisted or logged.

The replay contract is:

| Attempt state                                                                                                                  | API result                                               | Durable and Provider result                                                                                     |
| ------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| Same owner and key, exact authoritative purpose/plan/audience/cycle/agreement/subscription tuple, reusable and unexpired order | HTTP `201`, same `orderId`, equal authoritative response | Reuse one order and immutable command key; repeated prepare descriptor calls remain pure and side-effect-free   |
| Same owner and key with a changed authoritative tuple                                                                          | HTTP `409` `PAYMENT_PREPARE_ATTEMPT_CONFLICT`            | No mutation and no Provider descriptor call                                                                     |
| Same raw UUID under another authenticated owner                                                                                | Normal independent owner namespace                       | Cannot select or disclose the first owner's order; may create or reuse only the second owner's digest and order |
| Exact tuple with `expiresAt <= now` or status `EXPIRED`                                                                        | HTTP `400` `PAYMENT_ORDER_EXPIRED`                       | No reuse, Provider call, or command-key rewrite; old row remains history                                        |
| Exact tuple with safe terminal status `FAILED` or `CANCELLED`                                                                  | HTTP `409` `PAYMENT_ORDER_TERMINAL`                      | No reuse, Provider call, or command-key rewrite; old row remains history                                        |
| Exact tuple with `PROCESSING`, `PROVIDER_SUCCEEDED`, or `PENDING_PROVIDER_CONFIRMATION`                                        | HTTP `400` `PAYMENT_ORDER_INVALID_STATE`                 | No replacement prepare; use the WI-034 read-only outcome recovery path after confirm or upgrade starts          |

Legacy rows whose `command_key` is null do not participate in prepare replay
lookup and are not rewritten, backfilled, or deleted. A non-null prepare
`command_key` remains immutable when confirm starts. Confirm derives
`BILLING_CONFIRM:<orderID>` only for the specifically selected legacy null-key
order; `provider_idempotency_key` is a separate, advancing Provider-attempt
fence.

A successful response exposes these actionable fields under `data`:

```json
{
  "orderId": "ATS-BILL-...",
  "provider": "TOSS",
  "purpose": "SUBSCRIBE",
  "agreementStatus": "READY",
  "subscriptionId": 123,
  "billingCycle": "MONTHLY",
  "amount": 9900,
  "currency": "KRW",
  "expiresAt": "2026-08-10T12:15:00",
  "checkout": {
    "type": "TOSS_BILLING_AUTH",
    "clientKey": "<server-issued>",
    "customerKey": "<server-issued>",
    "successUrl": "https://example.test/subscriptions/checkout/success",
    "failUrl": "https://example.test/subscriptions/checkout/fail",
    "method": "CARD"
  }
}
```

The SPA retains this response as an actionable order only when the order ID is
nonblank; Provider is `TOSS`; purpose, exact `subscriptionId`, and cycle equal
the validated checkout request; status is `READY`; amount equals the exact
server plan price for `SUBSCRIBE` or zero for `BILLING_AGREEMENT`; currency is
`KRW`; expiry is parseable; checkout type is `TOSS_BILLING_AUTH`; method is
`CARD`; client/customer keys are nonblank; and both callback URLs are absolute
HTTP(S) URLs. A mismatch clears actionable state and prevents Toss SDK loading
and `requestBillingAuth`. Callback query construction uses the validated
response `orderId`, `amount`, and `purpose`.

Prepare failure is a terminal `ERROR` presentation with fixed product copy and
an explicit same-attempt retry; it is not displayed as still preparing. The
initial subscription command names both payment-method registration and the
immediate first charge. Payment-method re-registration remains a distinct
zero-immediate-charge command. Callback fields are parsed as single values;
invalid required state invokes neither prepare nor confirmation. Fail callback
query text is never rendered directly, including a blank `message` value.

WI-033 owns prepare-attempt identity, exact replay, and local claim reuse. The
prepare contract does not treat in-flight or unknown-outcome states as
replaceable. WI-034 now supplies the owner-scoped read-only recovery contract
below for callback response loss, charged-upgrade response loss, canonical
reload failure, and reconciliation after a mutation starts. WI-033 made no
payment-policy or schema change and used no real Provider, SDK, charge, refund,
mail, retained database, deployment, or secret action.

#### Payment Command Outcome Recovery Contract

Both outcome APIs are USER-only under `SecurityConfig`. They are owner-scoped
reads and return the same HTTP `404 PAYMENT_ORDER_NOT_FOUND` result when the
requested payment command is absent, belongs to another user, or does not match
the endpoint's supported purpose. They do not disclose whether a foreign order
exists.

`GET /api/payments/orders/{orderId}/outcome` reads the exact authenticated
owner's `TOSS` order and accepts only callback purposes `SUBSCRIBE` and
`BILLING_AGREEMENT`. `GET /api/payments/subscription-upgrades/outcome` requires
a positive target `subscriptionId` and `MONTHLY` or `YEARLY` `billingCycle`.
For upgrade recovery, the server resolves the current service-enabled
Subscription aggregate and deterministically reconstructs the existing command
identity from all of these values:

```text
current UserSubscription aggregate ID
+ current period startedAt/expiresAt
+ exact target Subscription plan ID
+ exact target BillingCycle
```

The repository lookup then requires both that exact command key and the
authenticated owner, and the returned order must still be `TOSS`, `UPGRADE`,
and match the exact target plan/cycle. The API never guesses from the latest
payment order. Because current-period identity is intentional, crossing a
subscription period boundary can make an earlier period's upgrade command
unavailable through this endpoint; operational reconciliation remains the
appropriate path instead of a latest-order fallback.

Both endpoints return the same minimal response under `data`:

```json
{
  "purpose": "UPGRADE",
  "orderStatus": "DONE",
  "userSubscriptionId": 91,
  "targetSubscriptionId": 30,
  "targetBillingCycle": "YEARLY"
}
```

| Field                  | Contract                                                                                |
| ---------------------- | --------------------------------------------------------------------------------------- |
| `purpose`              | `SUBSCRIBE`, `BILLING_AGREEMENT`, or `UPGRADE`, constrained by the selected endpoint    |
| `orderStatus`          | Current persisted payment-order status; not by itself a frontend success claim          |
| `userSubscriptionId`   | Canonical aggregate linkage recorded by the order; nullable until final linkage exists  |
| `targetSubscriptionId` | Exact target plan identity stored on the order                                          |
| `targetBillingCycle`   | Exact effective target cycle; upgrade target cycle for `UPGRADE`, order cycle otherwise |

The response contains no `authKey`, `customerKey`, billing key, Provider
payload, payment method, email, nickname, amount, or unrelated identifier. A
read performs zero Provider calls, zero mutation calls, and zero local
finalization. It does not acquire a Provider result, advance command status,
retry confirm/upgrade, or repair durable state.

Frontend recovery treats terminal `FAILED`, `CANCELLED`, or `EXPIRED` order
status as authoritative failure and any non-`DONE` non-terminal state as
ambiguous. `DONE` is still insufficient for `COMMITTED`: the frontend must
reload the canonical Subscription and Billing Agreement and prove the returned
`userSubscriptionId`, exact target plan, target cycle, and aggregate linkage
before announcing success or navigating.

### Download, License, and Likes (12)

- `GET /api/downloads/history`
- `GET /api/downloads/history/track-ids`
- `GET /api/licenses/me`
- `GET /api/licenses/{licenseId}`
- `GET /api/users/{userId}/licenses`
- `GET /api/users/{userId}/licenses/{licenseId}`
- `GET /api/likes`
- `POST|DELETE /api/likes/{trackId}`
- `GET /api/likes/albums`
- `POST|DELETE /api/likes/albums/{albumId}`

### Questions and Notices (14)

- `GET|POST /api/questions`
- `GET|DELETE /api/questions/{questionId}`
- `POST /api/questions/{questionId}/answers`
- `GET /api/questions/{questionId}/attachments/{attachmentId}`
- `PUT /api/questions/{questionId}/status`
- `GET|POST /api/notices`
- `GET|PUT|DELETE /api/notices/{noticeId}`
- `GET /api/notices/{noticeId}/admin`
- `GET /api/notices/{noticeId}/attachments/{attachmentId}`

### Company Certification and User Whitelist (13)

- `GET|POST /api/company-certifications`
- `GET|PUT /api/company-certifications/{certificationId}`
- `GET /api/company-certifications/{certificationId}/documents/{documentId}`
- `GET /api/company-certifications/me`
- `POST /api/company-certifications/me/documents`
- `GET|POST /api/whitelist-channels`
- `PUT|DELETE /api/whitelist-channels/{channelId}`
- `PUT /api/whitelist-channels/{channelId}/primary`
- `POST /api/whitelist-channels/{channelId}/request`

#### Subscriber Whitelist and Certification Read Contract

Whitelist channel create/update inputs trim `channelUrl` at the SPA boundary,
enforce the existing 255-character DTO limit, and mirror the backend absolute
HTTPS YouTube host, no-user-info, and standard-port predicate. Subscriber action
visibility mirrors backend status predicates: primary selection excludes
`REMOVAL_REQUESTED` and `CANCELLED`; an existing `REMOVAL_REQUESTED` row exposes
no repeated destructive-looking call. Editing `EXPORTED`, `REGISTERED`, or
`REVISION_REQUESTED` requires an explicit disclosure and confirmation that the
existing update contract returns the row to `PENDING`. This adds no status or
workflow transition and does not change ADMIN export behavior.

The company-certification apply route remains closed until its owned latest
`GET /api/company-certifications/me` returns definitive `404` absence or an
existing `REJECTED` application. Another returned status navigates to the status
route; `403` remains denied; a network or server failure is blocking and
manually retryable. User status and ADMIN list/detail reads expose bounded
manual retries. Request generations ensure stale completions cannot replace a
newer route, status-filter/page, or selected-detail context.

ADMIN Whitelist review notes retain the existing 500-character DTO bound. The
Whitelist review UI displays the bound and counter, trims the note, rejects 501
characters before invocation, and sends the exact normalized `adminNote` or
omits a blank note.

### Utilities and SPA (8)

- `GET /api/utils/check-email`
- `GET /api/utils/check-phone`
- `GET /api/utils/check-nickname`
- `GET /api/utils/download-count`
- `GET /api/utils/subscription-change-preview`
- `GET /api/utils/public-capabilities`
- `GET /api/settings/{key}`
- `GET /{path:^(?!api|uploads|swagger-ui|v3|oauth2|assets|.*\\..*).*$}/**`

## Configuration and Data Rules

- Runtime schema validation uses `spring.jpa.hibernate.ddl-auto=validate`.
- The committed base configuration does not import ignored local configuration.
  Local configuration is loaded only when explicitly supplied by the operator.
- `src/main/resources/schema.sql` and `seed.sql` are fresh-database inputs,
  not migration scripts.
- API examples and generated OpenAPI output must serialize payment provider as
  `TOSS`.
- Paginated application responses use `dataList` plus `pageInfo`; non-paginated
  collection responses such as Tag reads and PlayableTrack hydration use
  `dataList` without inventing a `content` field.
- Public Track search accepts `page >= 1` and `1 <= size <= 100`. Invalid
  pagination returns 400 `INVALID_ARGUMENT` before repository access, while
  `pageInfo.page` remains 1-based.
- Public Track `keyword` search matches title and associated `USAGE` Tag names;
  it does not search creator or artist display names.
- `genre`, `mood`, `instrument`, and `usage` search values are repeated query
  parameters. Values are canonicalized, de-duplicated, and combined with AND
  semantics within and across Tag types. Commas and `#` remain part of one Tag
  value rather than acting as a CSV delimiter.
- `POST /api/tracks/batch` is public, accepts 1 to 100 positive Track IDs,
  de-duplicates in first-requested-ID order, returns active Tracks only, and
  preserves that requested order in `dataList`.
- Playlist list counts, detail rows, and owner reorder requests use active Track
  memberships only. Reorder payloads contain every visible active Track exactly
  once with zero-based contiguous orders `0..n-1`; inactive membership rows
  remain persisted and are assigned deterministic orders after the active rows.
- Public Album `trackCount`, detail rows, and `trackCount` sorting use active
  Track memberships only. All-membership counts used by administrative
  mutation paths remain separate.
- The audio-analysis dry-run is ordered by Track ID, accepts `page >= 1` and
  `1 <= size <= 100`, and returns report rows only. It exposes no storage key
  and has no update/backfill side effect.

## Verification

Recount from source:

```powershell
$controllers = Get-ChildItem src/main/java/com/atstudio/atstudio/controller -Filter *.java
($controllers | Select-String '^\s*@(Get|Post|Put|Patch|Delete)Mapping\b').Count
```

Expected result: `151`.
