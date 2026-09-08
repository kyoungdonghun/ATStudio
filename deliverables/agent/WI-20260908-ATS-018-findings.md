---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: audit
status: confirmed
dependencies:
  - path: ../user/REQ-20260908-ATS-004.md
    reason: Approved bounded review
  - path: WI-20260908-ATS-018-evidence-pack.md
    reason: Central execution evidence and preservation boundaries
---

# V1 Release Risk Register: WI-20260908-ATS-018

## Verdict

**Review complete; release HOLD.** Source: main `8161f0a00b088001a37962da719c3ace8fea44ad`. Six P1 application defects were independently traced by the reviewer and MA. Six P2 findings need the distinct dispositions below. No P0 was established. None was deliberately executed against existing accounts, storage, a real database or a payment provider; code-confirmed paths are not claims of observed production incidents.

Existing tests passing does not close these findings: their fixtures omit the relevant cross-layer or competing-request conditions. This register is not authorization to implement, deploy, change policy, or mutate data.

## P1: Fix Before Release

All Java paths below begin at `src/main/java/com/atstudio/atstudio/`. Full trigger, defense and test pointers are in the linked specialist evidence.

| ID / source | Confirmed path and consequence | Minimal closure evidence |
|---|---|---|
| SEC-01 / [WI015 P1-01](WI-20260908-ATS-015-evidence-pack.md) | `security/JwtAuthenticationFilter.java:34-43` accepts a real refresh JWT as API Bearer. `JwtTokenProvider.java:39-54,74-81` checks signature/expiry but not purpose; the API path never compares the stored refresh hash. A revoked refresh capability remains usable as API authorization until its own expiry. | Explicit access/refresh purpose separation; real-token negative API tests after logout, reset and rotation. Preserve current DB role lookup; ordinary access-token TTL retention is a separate policy, not a new finding. |
| SEC-02 / WI015 P1-02 | `common/exception/GlobalExceptionHandler.java:71-75` logs validation exception text and throwable. Field-error text includes rejected passwords/email/phone. Generic HTTP responses do not sanitize the log event. | Capture synthetic rejected-value markers and prove they are absent from all validation log output; retain bounded field/category diagnostics. Do not inspect or purge historical logs without a separate sensitive-data scope. |
| PAY-01 / [WI016 F1](WI-20260908-ATS-016-evidence-pack.md) | `BillingAgreementPrepareTransactionService.java:111-151` permits a legitimate expired subscriber to purchase again. `BillingAgreementApplicationService.java:287-346` charges and records Provider success, but `PaymentCommandTransactionService.java:527,552,1319-1324` rejects ANY retained subscription row. The row-reuse branch at 576-581 is unreachable. Replay/reconciliation repeat the rejection. | Expired-history prepare -> fake successful charge -> finalize -> recovery replay, with one charge, one correct ledger and restarted access. Preserve lifecycle-change guards; do not merely remove the entire guard. |
| PAY-02 / WI016 F2 | `PaymentCommandTransactionService.java:100-150,823-849` fences the exact upgrade key only. Different next-cycle choices for the same current paid period obtain distinct orders/Provider keys before either finalizes. Finalization at 635-663,1015-1028 does not check the originally priced source generation; both differences can be charged. | Hold the first fake Provider call and submit another cycle/target from an independent context with distinct Provider transaction IDs. Require one accepted monetary intent and guarded finalize-only recovery. No multi-server architecture is needed. |
| DATA-01 / [WI017 F1](WI-20260908-ATS-017-evidence-pack.md) | `TrackService.java:217-228` describes soft deletion but physically deletes issued licenses and download history at 222-223. `LicenseRepository.deleteAllByTrack` is a real derived delete. Reactivation cannot restore evidence; deleting today's history also reduces the count used by `DownloadService.java:65`. | Seed existing licenses/history, deactivate/reactivate the Track, assert evidence and daily count are preserved. Keep the approved soft-delete contract; a stronger warning is not a sufficient fix. |
| DATA-02 / WI017 F2 | `PlaylistService.java:268-272` deactivates a row but retains its thumbnail key while scheduling file deletion. The checker excludes inactive parents, whereas `StorageIntegrityService.java:50-53` audits all parents. Successful cleanup therefore makes strict next startup fail. MA confirmed the same chain for `AlbumService.java:139-146` and `Album.softDelete`. | Compose normal thumbnail deletion, after-commit cleanup, integrity inspection and strict startup for Playlist AND Album; test shared/no-thumbnail cases too. Align retained-reference semantics, not by disabling strict startup or deleting history. |

MA rechecked existing counter-defenses: short transaction locks and exact-command uniqueness do not fence two different upgrade commands; retained-expiry state is excluded at prepare but included at finalization; authorized deletion and atomic transactions do not preserve rows explicitly deleted; current database role lookup does not establish token purpose; response sanitization does not cover throwable logging.

## P2: Disposition Must Be Explicit

P2 is severity, not automatic permission to defer security or data-integrity work.

| ID / source | Finding / evidence | Disposition and required bounded follow-up |
|---|---|---|
| SEC-03 / WI015 P2-03 | Real refresh generation has no random issuance ID. Same-user, same-second JJWT timestamp serialization can produce identical tokens, undermining rotation. `JwtTokenProvider.java:39-47`. | Include with auth correction before security release approval. Fixed-clock real-generator uniqueness and old-token rejection tests; no major session redesign required. |
| SEC-04 / WI015 P2-04 | `frontend/src/api/client.ts:114-158` applies a delayed refresh result without checking its initiating session. Success can restore old tokens after logout/account switch; failure can clear the replacement session. | Include with auth correction. Deferred success/failure after logout, A-to-B switch and same-user relogin; bind token persistence, queued replay and cleanup to the original session. No browser reproduction was executed here. |
| SEC-05 / WI015 P2-05 | `EmailService.java:145-160` validates reset-token used/expiry before acquiring only the user lock. Two overlapping transactions can both consume a previously valid token. | Close the atomic-consumption boundary before security release approval; deterministic independent-persistence-context test. The risky ordering is confirmed; actual MySQL interleaving remains unexecuted. |
| AUTH-06 / WI015 P2-06 | New social users are unverified; `AuthService` refresh applies the password verification gate to them too. Profile completion does not change that flag. | Fix before enabling that social lifecycle. A disabled OAuth provider need not become a launch feature. Do not blindly trust an unverified provider email. |
| DATA-03 / WI017 F3 | Unversioned/unlocked Track metadata or Like writes can restore an old file key after another transaction replaced and cleaned it. `TrackService.java:170-198`; `LikeService.java:33-48`; `entity/Track.java`. | Needs deterministic persistence/SQL confirmation before accepting media integrity. If reproduced, correct competing writers and add regression. Do NOT classify as harmless maintenance merely because the schedule was not executed in this review. |
| STORAGE-04 / WI017 F4 | `LocalStorageService.java:89-95` can leave a partially written stage; coordinator 69-78 excludes the failing target from cleanup and no journal owns it. The opened input stream also lacks explicit close. | Bounded failure-path maintenance candidate with disk/private-staging ownership and cleanup policy; easiest to close during related storage fixes. Test write-then-throw on first/later stage and cleanup failure. No remote file disclosure or disk exhaustion was demonstrated. |

## Dependency And Resource Gates

### DEP-01: Reconcile the Backend Security Patch Baseline

Pinned running JAR inventory: Boot 4.0.2, Framework 7.0.3, Security 7.0.2, Data JPA 4.0.2, Tomcat 11.0.15, JJWT 0.12.5. Artifact identity and raw list are in WI018 evidence. This was a selected reachability review, not a complete Maven/OS SCA scan.

- Tomcat 11.0.15 is in the affected range of CVE-2026-24880, conditional on a proxy forwarding a malformed chunk extension; a public fix is included in 11.0.20. Backend HTTP/2 advisories are distinct from Cloudflare's edge HTTP/2. No request-smuggling or load probe was attempted. Resolve the selected production patch/proxy disposition before release, preferably a tested compatible Boot patch line, not an arbitrary component override. [Apache Tomcat advisories](https://tomcat.apache.org/security-11.html)
- Data JPA CVE-2026-47834 additionally requires untrusted Sort in native queries. No `nativeQuery=true`/`@NativeQuery` path was found in application source; an affected version alone does not establish exploitation here. [Spring Data JPA advisory](https://spring.io/security/cve-2026-47834/)
- Framework CVE-2026-41852 requires untrusted SpEL evaluation; no application SpEL parser path was found. The similarly named serialization advisory affects Spring Integration, not all Spring MVC deployments; Integration is absent from the inspected runtime list. [SpEL advisory](https://spring.io/security/cve-2026-41852/), [Integration advisory](https://spring.io/security/cve-2026-47864/)
- Security advisories for DPoP replay, WebAuthn distributed sessions, `AesBytesEncryptor`, and Authorization Server consent were checked against the custom JWT/card application. Their prerequisites were not found; none is counted as a separate confirmed app exploit. [DPoP](https://spring.io/security/cve-2026-41707/), [WebAuthn](https://spring.io/security/cve-2026-47841/), [AES API](https://spring.io/security/cve-2026-47842/), [Consent](https://spring.io/security/cve-2026-47877/)

### DEP-02: Development Dependencies

Fresh npm audit: 6 affected dependency groups (5 high, 1 moderate); production-only audit: 0. `npm explain` marks the reviewed PostCSS, Undici, Browserslist, Nano ID and brace-expansion paths dev-only. These are not six exploitable production features. Advisory fixes are available; no audit-fix/install or lockfile change was made.

PostCSS's advisory requires crafted CSS/source-map handling and Undici advisories depend on specific client behavior. Scope a compatible development-tool update and rerun its tests/build. The current public Vite demo is NOT the recommended production server, so dev-only is not proof that every demo tool path is unreachable. [PostCSS advisory](https://github.com/advisories/GHSA-fxqj-rqcc-2cmp), [Undici advisory](https://github.com/advisories/GHSA-4cwx-7wf7-3272)

### OPS-01: Bound Multipart Work At The Selected Edge

`config/AppConfig.java:18-29` explicitly sets `maxPartCount=-1`; base multipart limits are 30 MB/file and 60 MB/request (`application.yml:35-37`). Service-level count checks run after parsing. No resource-exhaustion experiment was performed. Confirm a finite valid part count plus body/time/concurrency budgets for the deployed topology rather than treating byte limits or version upgrades as complete resource protection. This is a targeted configuration check, not a confirmed unauthenticated DoS incident.

## Maintenance / Optional Items

- Storage audit pagination remains the previously accepted scale-dependent maintenance item; strictness must not be weakened to hide DATA-02/DATA-03.
- Notification delivery reputation, correction edit/cancel UX and advanced operator alerts stay in the existing maintenance backlog. No new cash receipt, tax invoice, multi-PG or scheduler-lock feature is required here.
- `.claude/config/workspace.json:62` still names Thymeleaf; context injection notes at `context-injection-rules.json:84,259` describe the retired UI phase. These are optional authoring-context corrections, not active runtime routes. Do not erase historical REQ/WI evidence.

## Still Target-Dependent

The [SR-93 operating gates](../../docs/SR/SR-93.md#remaining-production-gates) remain open independently of the code findings. Name the actual host/domain/artifact, exact proxy and callback origins, secrets distribution, frontend production asset serving, file ACLs/static isolation, DB/public/private-media tuple, backup destination/retention and target restore evidence, alert owner, and single scheduler responsibility. Previously accepted Toss TEST/Gmail actions stay accepted evidence; repeat only what differs at the selected live target and requires that account's cooperation.

Fresh HTTP observation: public/local home and catalog 200; anonymous profile/admin/raw-audio/private-static 401; backend permitted preflights 200 with exact ACAO and credentials; hostile backend preflight and public GET 403. Public Vite OPTIONS returns 204 without ACAO. Backend OpenAPI is enabled locally (JSON 200); the public `/v3/api-docs` is SPA HTML, not an exposed OpenAPI document. HTML had no CSP/HSTS/frame headers in this demo, while API responses had nosniff/DENY. These are development-topology observations requiring target checks, not a production approval or a demonstrated CORS bypass.

## Finite Next Sequence

1. Scope fixes for SEC-01/02 with SEC-03/04/05; add targeted negative/race tests.
2. Scope PAY-01/02 corrections with deterministic fake-Provider recovery/interleaving tests. No actual new charge is needed to prove these local invariants.
3. Scope DATA-01/02 plus confirmation of DATA-03; include STORAGE-04 if the small adjacent fix is accepted. Never repair history by deleting it or bypassing strict guards.
4. Resolve compatible dependency/resource settings, then run the relevant aggregate gates once and independently review the changed boundaries. Do not restart a full unrelated audit after every small patch.
5. Perform only remaining selected-target/user-dependent operation checks; record explicit release approval. REQ004 completion means this review is finished, not these corrections.

## Related Evidence

- [Auth/PII review](WI-20260908-ATS-015-evidence-pack.md)
- [Payment review](WI-20260908-ATS-016-evidence-pack.md)
- [File/domain review](WI-20260908-ATS-017-evidence-pack.md)
- [Central verification](WI-20260908-ATS-018-evidence-pack.md)
- [User summary](../user/WI-20260908-ATS-018-summary.md)
