# Evidence Pack: WI-20260714-ATS-003

## Summary

- Defined implementation and verification contracts for untrusted content, refresh-session revocation, storage lifecycle, CSV export, social callback ordering, and Cloudflare-through-Vite acceptance security.

## Scope / DoD Check

- [x] P1-01 through P1-04, P1-11, P1-12, X-02, and X-04 map to enforcement and owning WIs.
- [x] Image/document matrices define signatures, bounds, canonical output, roots, headers, and failures.
- [x] Logout, password change, and password reset define refresh replay rejection.
- [x] Create, replace, delete, rollback, after-commit, retry, and safe reads are specified.
- [x] CSV, social callback, trusted proxy, Host, bootstrap, callback, and tunnel lifecycle contracts are specified.
- [x] Unproven runtime/provider facts are preserved as follow-up evidence requirements.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, language, traceability |
| 0 | `docs/standards/development-standards.md` | Service/transaction/evidence conventions |
| 0 | `docs/standards/documentation-standards.md` | Document metadata and reference structure |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio terms |
| 1 | `docs/policies/security-policy.md` | Secrets, tokens, storage, environment baseline |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default deny |
| 1 | `docs/policies/quality-gates.md` | P1 review/evidence gates |
| 1 | `docs/standards/frontend-standards.md` | Auth store, Axios, Vite SPA baseline |
| 2 | `docs/SR/SR-42.md`; `docs/client/`; `docs/design/api-spec.md` | Acceptance topology and current contracts |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved scope and WI plan |
| Audit | `docs/audit/full-system-audit-20260713.md` | Canonical findings and release gates |

`docs/audit/p1-remediation-trace-matrix-20260714.md` was absent and, per handoff, did not block this WI.

## Threat / Evidence Matrix

| Threat | Current evidence | Contract / negative evidence | Owner |
|---|---|---|---|
| Same-origin active thumbnail | `PlaylistService.java:52-54,186-188`; `LocalStorageService.java:51-70` | SVG/HTML/GIF/WebP rejected; valid input becomes new bounded JPEG only | WI-009 / WI-019 |
| Spoofed certification format | `CompanyCertificationService.java:233-276` | Signature mismatch, malformed PDF, active image, oversize, and polyglot cases | WI-010 / WI-019 |
| Public/private boundary bypass | `CompanyCertificationService.java:310-315`; `WebConfig.java:20-25` | Private root is outside `/uploads`; ADMIN attachment headers and traversal/symlink denial | WI-010 / WI-019 |
| Refresh replay after termination | `AuthService.java:76-110`; `UserService.java:183-193`; `EmailService.java:141-159` | Replay after logout/change/reset returns 401; concurrent refresh is serialized | WI-011 / WI-019 |
| DB rollback causes missing/orphan file | `TrackService.java:164-183`; `NoticeService.java:92-132`; `QuestionService.java:173-189` | Rollback cleanup, after-commit old delete, stale journal recovery, durable retry | WI-012 / WI-019/WI-021 |
| Spreadsheet formula execution | `AdminWhitelistChannelService.java:171-198` | `= + - @`, leading whitespace/control, quote/newline, UTF-8 cases | WI-013 / WI-019 |
| Social callback unauthenticated first request | `SocialLoginPage.tsx:42-59`; `auth.ts:99-103` | Assert token commit precedes `/users/me`; failure clears partial state | WI-014 / WI-020 |
| Spoofed forwarded identity / collapsed clients | `AuthRateLimitFilter.java:43-48`; `vite.config.ts:12-24` | Direct spoof rejection, malformed/list header rejection, two-public-client separation | WI-016 / WI-020/WI-022 |
| Host poisoning / wildcard origin | `vite.config.ts:15`; `CorsConfig.java:21-29` | Unknown Host rejected; exact ephemeral host accepted; wildcard CORS absent | WI-016 / WI-020/WI-022 |
| Bootstrap outside non-prod | `TestUserBootstrapRunner.java:33-52`; `TestUserBootstrapProperties.java:14-22`; `application.yml:87-90` | Production/default startup refusal; external password/secret requirement | WI-015 / WI-020 |
| Stale localhost/Toss callback | `application.yml:93-108`; `PaymentProperties.java:22-41` | One validated HTTPS base derives all expected callback paths | WI-015 / WI-020/WI-022 |
| Leaked or orphaned tunnel | `frontend/vite.config.ts:16-24`; `docs/SR/SR-42.md:21-31,61-66` | Partial-start cleanup, ownership-checked stop, public unreachability after stop | WI-017 / WI-022 |

## Exact Impacted Symbols

- Image/document: `PlaylistService.createPlaylist/updatePlaylist`, `CompanyCertificationService.validateDocuments/storeDocuments/downloadDocument`, `CompanyCertificationController.downloadDocument`, `LocalStorageService`, `WebConfig`.
- Session: `AuthService.refresh` plus new logout, `UserService.updatePassword`, `EmailService.resetPassword`, `UserRepository` locked lookup, frontend logout flow.
- Lifecycle: `TrackService`, `PlaylistService`, `AlbumService`, `CompanyCertificationService`, `NoticeService`, `QuestionService`, `StorageService`, `LocalStorageService`, and the approved mutation journal.
- CSV/social: `AdminWhitelistChannelService.csv`, `SocialLoginPage`, `authStore`, `auth.fetchMe`, Axios client.
- Acceptance: `AuthRateLimitFilter`, new client-identity/Host startup guards, `CorsConfig`, `TestUserBootstrapRunner/Properties`, `PaymentProperties`, `application.yml`, `vite.config.ts`, and WI-017 lifecycle scripts.

## Commands and Results

- Handoff reproduction searches were executed with `rg` against the listed backend/frontend paths.
- No server, tunnel, live provider, production DB, uploaded document body, runtime log, dependency installation, build, or test suite was used.
- Whitespace gate: `git diff --no-index --check -- /dev/null <owned-file>` for each new owned file; PASS when this Evidence Pack was finalized (exit 1 is the expected new-file diff status, with no whitespace-error output).

## Risks / Rollback / Approvals

- Certification signature checks do not prove malware absence. HWP/HWPX/DOC/DOCX are fail-closed pending product/parser or isolated-review approval.
- `storage_mutations` DDL and disposable MySQL require separate approval before WI-012/WI-021 execution.
- Cloudflare header behavior, client-IP separation, Toss test credentials/callback registration, and public URL reachability remain external evidence for WI-022.
- Rollback must disable new uploads first, preserve/drain pending storage journal rows, and revert only the relevant implementation WI. This design performs no file migration.

## WI-Chain Triggers

- Immediate implementation handoffs: `WI-20260714-ATS-009` through `WI-20260714-ATS-017`.
- Verification after implementation: `WI-20260714-ATS-019` through `WI-20260714-ATS-022`.
- Review closure: `WI-20260714-ATS-024` and `WI-20260714-ATS-025`.
- MA must resolve the certification-format and storage-schema approval points before delegating the affected implementation WIs; all unaffected WIs may proceed from this contract.
