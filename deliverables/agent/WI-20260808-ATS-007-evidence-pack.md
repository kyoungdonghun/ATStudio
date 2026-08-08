---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
related_wi: WI-20260808-ATS-007
dependencies:
  - path: WI-20260808-ATS-007-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../user/REQ-20260808-ATS-002.md
    reason: Approved request and acceptance criteria
  - path: WI-20260808-ATS-006-evidence-pack.md
    reason: Documents submitted for independent verification
  - path: ../user/WI-20260808-ATS-007-summary.md
    reason: User-facing verification verdict
---
# Evidence Pack: WI-20260808-ATS-007

## Summary (one-liner)

- Independently verified SR-96 through SR-98 against current frontend, API, service, entity, authentication, storage, runtime, test, index, and official-reference evidence; final result is PASS with no BLOCKER, MAJOR, or MINOR findings.

## Scope / DoD Check

- [x] Rechecked every SR's central current-state claim against current product code rather than relying only on preceding reports.
- [x] Verified SR-96's JWT-claim, DB-current-role authorization, and stale SPA-role distinction.
- [x] Verified SR-97's absent plan-change contract, independent field persistence, valid cancelled grace period, and invalid future-dated expired state.
- [x] Verified SR-98's raw Track storage, aspect-preserving `cover` crop, runtime evidence, proposed 1:1 minimum, and separation from SR-68.
- [x] Verified SR file count, index rows, status totals, category counts, and overall document total.
- [x] Verified local links, official external references, UTF-8 replacement characters, Markdown structure, and whitespace checks.
- [x] Ran the bundled `validate-docs` script and `git diff --check` with successful exit codes.
- [x] Changed only this WI's user summary and Evidence Pack.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and transparent evidence requirements |
| 0 | `docs/standards/development-standards.md` | Cross-layer, testing, and traceability standards |
| 1 | `docs/policies/quality-gates.md` | Independent review and regression gate |
| 1 | `docs/standards/evidence-pack-standard.md` | Evidence Pack metadata, pointers, reproduction, and rollback contract |
| 1 | `docs/policies/security-policy.md` | JWT, browser token storage, ADMIN, payment, and PII boundaries |
| 2 | `docs/standards/frontend-standards.md` | Active React SPA role and validation patterns |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React implementation context supplied by the handoff |
| 2 | `docs/design/api-spec.md` | Current ADMIN user/subscription endpoint boundaries |
| 2 | `docs/design/usecase/user-subscription.md` | Service-enabled grace period and ADMIN emergency-control semantics |
| 2 | `docs/SR/SR-14.md` | Historical subscription product/status request |
| 2 | `docs/SR/SR-68.md` | Existing album image layout-protection scope |
| 2 | `docs/SR/SR-96.md` | Administrator-role SR under verification |
| 2 | `docs/SR/SR-97.md` | Subscription-edit SR under verification |
| 2 | `docs/SR/SR-98.md` | Track-thumbnail SR under verification |
| 2 | `docs/SR/index.md` | SR number and status source |
| 2 | `docs/index.md` | Overall document-count source |
| Context | `deliverables/user/REQ-20260808-ATS-002.md` | Approved scope and quality gates |
| Context | `deliverables/agent/WI-20260808-ATS-003-evidence-pack.md` | Role-change security investigation |
| Context | `deliverables/agent/WI-20260808-ATS-004-evidence-pack.md` | Subscription cross-layer investigation |
| Context | `deliverables/agent/WI-20260808-ATS-005-evidence-pack.md` | Thumbnail code, runtime, and official-reference investigation |
| Context | `deliverables/agent/WI-20260808-ATS-006-evidence-pack.md` | SR integration and preliminary document checks |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task type: integration, documentation, security, and validation review
- Injected tiers: Tier 0, relevant Tier 1/2, approved REQ, and all predecessor Evidence Packs named by the handoff

## Independent Findings

### Severity Summary

| Severity | Count | Result |
| --- | ---: | --- |
| BLOCKER | 0 | None |
| MAJOR | 0 | None |
| MINOR | 0 | None |

The handoff initially referenced a nonexistent `.claude/scripts/validate_docs.py` path. MA corrected the pointer during verification. The current packet now names `.agents/skills/validate-docs/scripts/validate_docs.py`, which exists and was executed successfully; therefore this is not an outstanding finding.

### SR-96: Administrator Demotion and Authorization

- `frontend/src/pages/admin/UserManagePage.tsx:12,150-169` renders the same `USER`/`ADMIN` selector for every row, with no current-user or last-admin guard.
- `frontend/src/pages/admin/UserManagePage.tsx:76-89` submits the selected role and maps failures to a generic page error.
- `src/main/java/com/atstudio/atstudio/controller/UserController.java:94-101` requires ADMIN at entry but passes only target ID and request to the service.
- `src/main/java/com/atstudio/atstudio/service/UserService.java:262-267` uses ordinary `findById` and applies the request directly.
- `src/main/java/com/atstudio/atstudio/entity/User.java:98-101` accepts every non-null role; `UserRepository.java:17-19,32-34` has a target lock and active-role count helper, but the mutation path uses neither.
- `src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java:27-36` places issue-time role in the access token.
- `JwtAuthenticationFilter.java:34-43`, `CustomUserDetailsService.java:27-33`, and `CustomUserDetails.java:27-29,44-52` use only token user ID to reload the current DB user and build request authorities from its current role.
- `frontend/src/store/authStore.ts:8-20` initializes role from persisted user data, `ProtectedRoute.tsx:28-40` gates the SPA from that cached role, and `frontend/src/api/client.ts:95-105` refreshes tokens without refreshing user/role state.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:659-675` covers only a successful ADMIN user update and does not contradict the stated missing invariant.
- Verdict: `docs/SR/SR-96.md:17-34` is factually correct. Its server invariant, shared serialization, actor recheck, UI/session convergence, audit, recovery, and test requirements at lines 36-84 are clearly proposals rather than current behavior.

### SR-97: Plan, Status, and Expiration Consistency

- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:38-45,88-95` loads plans but has no target-plan edit state.
- `UserSubscriptionManagePage.tsx:107-121` sends only changed status, billing cycle, and expiration; lines 193-197 use plans only to name a pending plan; lines 221-265 expose no current-plan selector or relational validation.
- `frontend/src/api/userSubscriptions.ts:102-114` and `AdminUpdateSubscriptionRequest.java:8-12` contain no target subscription ID.
- `UserSubscriptionService.java:196-202` performs an unlocked lookup and direct entity call; `UserSubscription.java:131-134` independently assigns each provided field while leaving current/pending plan state separate.
- `UserSubscriptionRepository.java:22-41` defines service-enabled access as `ACTIVE` or `CANCELLED` with `expiresAt >= today`, and selects past-dated rows for expiration.
- `UserSubscriptionServiceTest.java:645-680` accepts a future-dated `CANCELLED` update and a null no-op; lines 829-888 explicitly cover future-dated cancelled grace and past-expiry denial.
- `SubscriptionScheduler.java:79-99` converts past-dated active/cancelled rows to `EXPIRED` on the configured schedule.
- `AdminPaymentEntitlementCorrectionService.java:289-320` already rejects future-dated `EXPIRED`, past-dated non-expired states, inactive/wrong-type plans, and no-op targets in the guarded correction path.
- Verdict: `docs/SR/SR-97.md:17-44` correctly distinguishes current facts and the proposed matrix. The plan-change alternatives and payment/provider boundary at lines 46-87 do not claim that a direct plan edit already exists.

### SR-98: Raw Storage, Cover Crop, and 1:1 Contract

- `frontend/src/pages/creator/TrackUploadPage.tsx:210-215,422-445` validates no image dimensions or ratio, gives no preview, accepts `image/*`, and checks only the 10 MB frontend limit; lines 251-261 send the selected file unchanged.
- `TrackService.java:63-81,180-187` sends Track thumbnails directly to public storage on both create and update.
- `LocalStorageService.java:65-74` copies the multipart input stream unchanged to staged storage.
- `PlaylistService.java:58-63` demonstrates the existing canonicalizer being used for playlist thumbnails, while Track code has no such call.
- `CanonicalImageService.java:157-187` applies one shared scale factor capped by maximum dimension, never upscales, and does not square-crop; `CanonicalImageServiceTest.java:56-64` proves 3000×1000 becomes 2048×683.
- `TrackDetailPage.module.css:51-74,290-306`, `TrackRow.module.css:131-150`, `PlayerBar.module.css:67-85`, and `TrackManagePage.module.css:138-155` use square/fixed boxes and `object-fit: cover`.
- Repository-wide CSS recount found 18 `cover` declarations and 0 `contain` declarations.
- Live read-only verification of `GET http://localhost:8080/api/tracks/1` returned the same acceptance description and key `tracks/thumbnail/baecf782d5f04e2997cbea7d6ef094f1.png`; the fetched image measured 564×1404px and 1,229,440 bytes.
- `docs/SR/SR-68.md:1-21` concerns album layout overflow and responsive protection. `docs/SR/SR-98.md:40-76` instead specifies the Track upload contract, production-equivalent preview, canonicalization reuse, and existing-asset policy.
- Verdict: `docs/SR/SR-98.md:11-38` correctly identifies aspect-preserving presentation crop rather than pixel distortion. The 1:1 requirement is explicitly proposed, 2048×2048px is labeled recommended, and the interactive cropper is deferred.

## External Reference Verification

Official pages were resolved and inspected on 2026-08-08:

- `docs/SR/SR-96.md:89-91`
  - OWASP Authorization: default deny, permission validation on every request, contextual authorization, logging, and authorization tests.
  - OWASP Logging: privilege changes and administrator actions as loggable events; when/where/who/what, result, and reason fields; sensitive-data exclusions.
  - OWASP Business Logic Security: server-side authority, explicit state validation, atomic check-and-act operations, locks, contextual authorization, and concurrency tests.
- `docs/SR/SR-98.md:86-88`
  - MDN `object-fit`: `contain` preserves the whole object with letterboxing, `cover` preserves ratio and clips overflow, and `fill` may stretch.
  - Shopify Help: square product images commonly display best at 2048×2048px; this supports a recommendation, not an ATStudio mandatory limit.
  - Cloudinary: `fill` can crop without distortion, `fit` preserves the full image inside bounds, and `pad` preserves content with added space.

The references are grouped in dedicated evidence sections. SR-98 maps each link directly to the supported claim; SR-96's three link titles map one-to-one to the authorization, audit, and business-logic controls in the preceding sections. No irrelevant or broken external citation was found.

## Index and Document Integrity

| Check | Expected | Actual | Result |
| --- | ---: | ---: | --- |
| Numbered `docs/SR/SR-*.md` files | 97 | 97 | PASS |
| Numbered SR index rows | 97 | 97 | PASS |
| DONE | 82 | 82 | PASS |
| OPEN | 12 | 12 | PASS |
| NOT CONFIRMED | 2 | 2 | PASS |
| DROPPED | 1 | 1 | PASS |
| Non-index Markdown documents under `docs/` | 199 | 199 | PASS |

Per-category recount also matched `docs/index.md`: architecture 1, design 29, policies 8, standards 13, templates 18, registry 4, audit 6, client 8, payment 7, SR 97, retrospective 4, ADR 1, UI 3, and eval 0.

## Files Changed

- `deliverables/user/WI-20260808-ATS-007-summary.md`
  - User-facing PASS verdict, severity count, SR conclusions, and validation summary.
- `deliverables/agent/WI-20260808-ATS-007-evidence-pack.md`
  - This independent verification and reproducibility record.

No product code, SR, index, database, image, runtime, external provider, or unrelated user file was modified.

## Commands & Outputs

| Command / check | Exit / result |
| --- | --- |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | Exit 0; Tier 0, internal links, 485 traceability ID matches, and document index all PASS |
| `git diff --check` | Exit 0; no whitespace errors, only CRLF→LF notices for the two tracked index files |
| PowerShell SR file/index/status aggregation | `97 files == 97 rows`; `82/12/2/1` totals PASS |
| PowerShell per-category and non-index Markdown recount | Every category and total 199 match `docs/index.md` |
| Regex local-link resolution for SR-96 through SR-98 | PASS; no missing local targets |
| UTF-8 replacement-character scan | PASS; none found |
| Targeted Markdown structure/whitespace scan over SR-96~98 and both indexes | PASS; one H1 per file, no heading jump, unbalanced fence, trailing whitespace, conflict marker, or missing final LF |
| Focused numbered reads and `rg` across role, subscription, payment-correction, Track storage, canonicalizer, CSS, and tests | PASS; facts listed above independently reproduced |
| Read-only `GET /api/tracks/1` and thumbnail dimension read | PASS; description/key matched, image 564×1404px and 1,229,440 bytes |
| Official OWASP, MDN, Shopify Help, and Cloudinary page inspection | PASS; all six links resolved and directly support the cited claims |

## Tests

- Documentation integrity: PASS.
- Cross-layer static verification: PASS.
- Runtime thumbnail reproduction: PASS, read-only.
- Product build/unit/integration suites: not run because WI-007 is documentation-only, no product code changed, and the handoff requested focused evidence rather than implementation validation.

## Risks / Rollback

### Risks

- Runtime `track 1` evidence is a snapshot of the currently running local acceptance environment, not production evidence; the code-based storage and CSS conclusions do not depend on that row remaining unchanged.
- SR-96 intentionally leaves the shared-lock and full session-revocation mechanism open; SR-97 leaves the general correction approval/evidence model open; SR-98 leaves existing non-square asset handling open. These are documented design decisions, not validation failures.
- Official reference pages can evolve. The current URLs and supporting sections were valid on the verification date.

### Rollback

- Remove only `deliverables/user/WI-20260808-ATS-007-summary.md` and `deliverables/agent/WI-20260808-ATS-007-evidence-pack.md` to roll back this verification output.
- Do not alter the approved REQ, handoffs, predecessor Evidence Packs, SR files, indexes, product code, DB, runtime assets, or unrelated user files.

## Follow-ups

- No SR correction WI is required.
- Each OPEN SR requires a separately approved implementation REQ/WI after its unresolved design and policy decisions are made.
