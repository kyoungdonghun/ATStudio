# Evidence Pack: WI-20260714-ATS-024

## Security Gate

- **BLOCK**: one confirmed High same-origin active-content exposure remains unresolved.
- No Critical finding was confirmed.
- One Medium privacy/logging defect remains unresolved.
- Review completion does not satisfy the handoff DoD requirement that no confirmed Critical/High exposure remain unresolved.

## Severity-Ordered Findings

### HIGH - PG-024-01: Question attachments can become anonymous same-origin active content

**OWASP relevance:** Broken Access Control / Security Misconfiguration / untrusted active-content upload.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/service/QuestionService.java:216-227`
  - Authenticated users submit arbitrary non-empty `MultipartFile` values.
  - Bytes are stored under `StorageRoot.PUBLIC` and `questions/attachments` without authenticity/canonicalization.
- `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java:59-62`
  - Generated keys preserve a safe-form copy of the submitted extension, including an active `.html` extension.
- `src/main/java/com/atstudio/atstudio/config/WebConfig.java:20-24`
  - The complete PUBLIC root is mounted at `/uploads/**`.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:83-84,133-136`
  - Only Track source audio and Company Certification legacy paths are denied.
  - Non-API static paths fall through to `anyRequest().permitAll()`.
- `src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java:17,25-29`
  - Forced media type, CSP, and `nosniff` apply only to canonical Playlist thumbnails.
- `frontend/src/store/authStore.ts:38-48`
  - Auth tokens are staged in browser storage, increasing the impact of same-origin script execution.
- `src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java:139-159`
  - Existing tests cover the authenticated controller route only; no direct static-path denial assertion exists.

**Impact:** An authenticated user can create an attachment whose generated public key retains an active extension. A victim can then load the anonymous `/uploads/questions/attachments/**` resource from the application origin without the Question ownership check. Same-origin script execution can access browser-managed application state and tokens. This is a release/client-share security blocker.

**Required narrow fix:** Add `/uploads/questions/attachments/**` to the explicit static `denyAll` rules before the static `permitAll` fallback. Preserve the authorized API attachment route. Add generated HTML-fixture tests for anonymous, USER, and ADMIN static denial plus safe authorized attachment download.

### MEDIUM - PG-024-02: Bootstrap logs emit configured email identifiers

**OWASP relevance:** Security Logging and Monitoring / privacy data minimization.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:138-144`
  - The success log emits all configured QA account emails.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:173-175`
  - Subscription reset skip logging emits a user email.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:194-202`
  - Subscription alignment and missing-plan logs emit a user email.
- `docs/policies/security-policy.md` Section 2-3
  - Logs must minimize sensitive data and mask email when it is necessary to disclose it.

**Impact:** Acceptance configuration can place email identifiers into repo-external runtime logs. The values are not needed to establish bootstrap readiness or explain a skip reason.

**Required narrow fix:** Remove email/user identifiers from these messages and retain only aggregate role readiness and bounded reason codes. Add a focused log-capture test asserting configured fixture emails are absent.

## No Additional Confirmed Finding

- Upload/certification quarantine:
  - `CompanyCertificationService.java:84-102,200-210,306-423` validates before mutation, uses PRIVATE load, and verifies signature/extension/MIME.
  - `CompanyCertificationController.java:99-118` returns ADMIN-only, attachment-only, no-store, `nosniff`, sandboxed, no-Range content.
  - `CompanyCertificationResponse.java:23-40` redacts `documentPath`.
- Private/public storage and journal:
  - `LocalStorageService.java:41-55,113-133` enforces disjoint roots, strict typed loading, and refuses PRIVATE URLs.
  - `StorageMutationJournalService.java:27-67` and `StorageMutation.java:41-73` retain opaque operation/key/state data, not original filename/content/PII/raw exception.
  - `StorageMutationCoordinator.java:280-287` logs operation ID and exception class only.
- Session/logout/social:
  - `AuthService.java:75-115`, `UserService.java:183-193`, and `EmailService.java:140-160` serialize refresh state and clear it on termination paths.
  - `frontend/src/pages/auth/SocialLoginPage.tsx:45-78` stages tokens before `fetchMe` and clears partial state on failure.
  - `frontend/src/api/client.ts:36-45` excludes logout from refresh recursion.
- CSV:
  - `AdminWhitelistChannelService.java:171-203` neutralizes user-controlled formula-leading cells before CSV escaping while preserving BOM/order.
- Host/CORS/proxy/rate-limit identity:
  - `frontend/vite.config.ts:22-111,121-145` validates exact Host input, strips forwarding headers, and writes one bounded internal identity.
  - `TrustedClientIdentityResolver.java:33-62,118-127` trusts the identity only from configured loopback peers and parses one IP literal.
  - `AuthRateLimitFilter.java:43-48` consumes the effective identity.
  - `AcceptanceHostFilter.java:35-68` and `CorsConfig.java:29-43` enforce exact acceptance boundaries.
- Acceptance secrets/bootstrap:
  - `AcceptanceStartupGuard.java:48-84,88-171` requires explicit non-production/acceptance state, resolved external values, and callback equality without logging values.
  - The separate email logging finding above remains open.
- Lifecycle/teardown:
  - `scripts/acceptance/AcceptanceLifecycle.psm1:367-482` checks PID/start-time/executable/command ownership and cleans up tunnel -> frontend -> backend.
  - `scripts/acceptance/AcceptanceLifecycle.psm1:685-725` verifies closed ports and public unreachability.
  - `scripts/acceptance/AcceptanceLifecycle.psm1:729-836` guarantees abnormal-start cleanup through `finally`.

## WI-022 Limitation Decisions

### Two external egress clients

- **Classification:** residual security acceptance limitation; not a confirmed vulnerability.
- **Release impact:** not a security release blocker.
- **Client-share impact:** not a blocker by itself for a controlled, low-concurrency acceptance session.
- **Reason:** WI-022 proved that alternating spoofed forwarding headers did not create new rate-limit identities and reached `429`, while code/tests constrain the trusted assertion. What remains unproved is deployment-specific separation of two genuine Cloudflare client identities. The residual risk is primarily shared-bucket availability/false positives, not an authorization bypass.
- **Constraint:** X-02 multi-client operational proof remains open and must not be claimed complete.

### Active-subscriber success path

- **Classification:** residual fixture/acceptance-coverage limitation; not a security defect.
- **Release impact:** not a security release blocker.
- **Client-share impact:** **blocker for the approved client acceptance scope** because the promised subscriber workflow cannot be exercised successfully without a disposable plan/subscription fixture.
- **Reason:** WI-022 observed expected `403` results when no active plan existed. That does not establish broken authorization, but it also provides no positive entitlement/navigation evidence.

## Scope / DoD Check

- [x] Read the handoff and all listed pointers, including WI-022.
- [x] Independently reviewed upload/quarantine, storage, session/logout/social, CSV, ingress identity, acceptance/bootstrap, lifecycle, and teardown evidence.
- [x] Findings lead with severity and exact source pointers.
- [x] No actual secret, private file body, live credential, token, client IP, or runtime URL was inspected or reproduced here.
- [ ] No confirmed Critical/High exposure remains unresolved.
- [ ] WI-024 security gate passes.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability |
| 0 | `docs/standards/development-standards.md` | Review and test standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable format |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Secrets, PII, storage, auth boundaries |
| 1 | `docs/policies/access-control-policy.md` | Default deny and least privilege |
| 1 | `docs/policies/quality-gates.md` | Independent High-risk review gate |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved scope and acceptance promises |
| Design | `docs/design/p1-security-acceptance-hardening-design.md` | Security contracts |
| Prior WI | `WI-009` through `WI-017`, `WI-019`, `WI-020`, `WI-022` Evidence Packs | Implementation, focused verification, public smoke, teardown |

## Commands / Tests

- Review-only source and evidence inspection was performed before the user's stop instruction.
- No actual secret/private/runtime artifact was opened.
- No code fix or test command was executed in WI-024 after the user instructed immediate finalization and no further commands.
- Existing cited focused evidence:
  - WI-019: backend upload/quarantine/session/storage/CSV focused tests passed.
  - WI-020: 23 frontend auth/social/proxy tests passed with typecheck/lint/build.
  - WI-022: public smoke and teardown passed except the two explicitly classified limitations.

## Risks / Rollback

- Risk: client sharing before PG-024-01 is fixed can expose same-origin active content.
- Risk: acceptance logs can retain configured email identifiers until PG-024-02 is fixed.
- Rollback: no application or test file was changed by WI-024, so no code rollback is required. Withdraw only these two WI-024 deliverables if the review record itself must be replaced.

## Follow-up Gate

1. Fix PG-024-01 and run the focused static-path/API attachment tests.
2. Fix PG-024-02 and run a focused log-capture test.
3. Re-review those exact diffs before changing WI-024 from BLOCK to PASS.
4. Seed a disposable active subscriber plan before client URL sharing within the approved acceptance scope.
