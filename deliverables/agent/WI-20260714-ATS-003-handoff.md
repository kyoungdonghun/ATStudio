[WI HEADER]
WI ID: WI-20260714-ATS-003
REQ: REQ-20260714-ATS-001
Agent: pg
Depends On: -
Blocks: WI-20260714-ATS-009 through WI-20260714-ATS-017, WI-20260714-ATS-019 through WI-20260714-ATS-022, WI-20260714-ATS-024, WI-20260714-ATS-025

[WI SUMMARY]
Why: Define security and acceptance-environment contracts before modifying untrusted-content, session, file-lifecycle, export, social-login, proxy, bootstrap, and tunnel behavior.
Scope (in/out):
- In: ATS020-P1-01 through P1-04, P1-11, P1-12, and acceptance-environment ATS020-X-02/X-04.
- In: Image decode/re-encode and serving policy, certification signature/MIME validation and quarantine, refresh-session revocation, file/DB mutation lifecycle, CSV formula neutralization, social callback token ordering, trusted-proxy client identity, Host allowlist, public callback/base URL, bootstrap guards, and Cloudflare lifecycle boundaries.
- In: Exact trust boundaries, allowed formats, storage roots, response headers, failure behavior, test cases, and operational preconditions.
- Out: Implementation edits, live credentials/services, production deployment, data deletion, legacy document migration, and external client sharing.
- Out: General UI redesign, physical source-audio relocation, dedicated preview generation, and historical JWT key rotation.
DoD:
- Every in-scope finding has an explicit security invariant, enforcement layer, failure response, evidence requirement, and owning implementation WI.
- Uploaded images cannot remain active document formats after processing; certification documents are never trusted from extension or client MIME alone.
- Refresh capability revocation semantics are defined for logout, password change, and password reset without logging token material.
- File and DB mutation ordering defines rollback cleanup, after-commit deletion, retry evidence, and safe download boundaries.
- Trusted proxy and Host rules work for direct local use and Cloudflare-through-Vite acceptance without accepting arbitrary forwarded headers or wildcard hosts.
- Acceptance startup requires explicit non-production profile and external secrets; public URL injection and Toss callback behavior are deterministic.
Constraints/Forbidden:
- Read-only inspection except for the three WI deliverables listed in the output contract.
- Do not edit application code, schemas, tests, existing docs, or runtime logs.
- Do not read certification document contents or expose secrets/tokens/raw card/billing data.
- Do not introduce a content-scanning library, proxy dependency, or tunnel service change without approval.
- Do not start servers, create a tunnel, or share a public URL in this WI.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] P1-01 through P1-04, P1-11, P1-12, X-02, and X-04 each map to a concrete enforcement contract and implementation WI.
- [ ] Image and certification validation matrices define accepted signatures, decoded formats, canonical output, storage, and download headers.
- [ ] Session revocation covers all three termination paths and token replay tests.
- [ ] File/DB coordination covers create, replace, delete, rollback, after-commit, and retry outcomes.
- [ ] CSV neutralization preserves data while preventing spreadsheet formula execution.
- [ ] Social callback ordering cannot call authenticated user APIs before token state is committed.
- [ ] Proxy/Host/bootstrap/public-URL contracts include local and Cloudflare acceptance cases.
Performance:
- [ ] Upload verification has explicit size and decode bounds and avoids unbounded in-memory processing.
- [ ] Trusted-client identity parsing is deterministic and bounded.
Quality:
- [ ] Controls are enforced server-side where client behavior is insufficient.
- [ ] Abuse cases and negative tests are specified, including spoofed forwarded headers and polyglot content.
- [ ] `git diff --check` passes for the WI deliverables.

[INPUT POINTERS]
Tier 0 (Constitution and development standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Security and access policies):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2 (Acceptance and UI/runtime contracts):
- docs/standards/frontend-standards.md
- docs/SR/SR-42.md
- docs/client/
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/full-system-audit-20260713.md
- docs/audit/p1-remediation-trace-matrix-20260714.md (consume if available; do not block on parallel WI)

Files:
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
- src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java
- src/main/java/com/atstudio/atstudio/security/AuthRateLimitFilter.java
- src/main/java/com/atstudio/atstudio/security/JwtAuthenticationFilter.java
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/resources/application.yml
- src/test/java/com/atstudio/atstudio/
- frontend/src/pages/auth/SocialLoginPage.tsx
- frontend/src/api/client.ts
- frontend/vite.config.ts

Repro/Logs:
- `rg -n "MultipartFile|contentType|originalFilename|Files\\.|delete|move|copy|refreshToken|logout|password|X-Forwarded|Forwarded|getRemoteAddr|allowedHosts|bootstrap" src/main/java src/main/resources frontend`
- `rg -n "CSV|export|escape|quote|userEmail" src/main/java/com/atstudio/atstudio/service src/main/java/com/atstudio/atstudio/controller`
- `git diff --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-003-summary.md:
- Korean explanation of the security and acceptance contracts, limitations, and approval points.
Agent-facing -> deliverables/agent/WI-20260714-ATS-003-evidence-pack.md:
- Threat/evidence matrix, exact impacted symbols/files, abuse tests, operational checks, and next-WI triggers.
Handoff Packet -> deliverables/agent/WI-20260714-ATS-003-handoff.md:
- This packet.
Additional artifact -> docs/design/p1-security-acceptance-hardening-design.md:
- English implementation-ready security and acceptance-environment contract.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Specify unit/integration/browser/public-smoke cases; do not call live Toss, SMTP, production DB, or share a public URL.
Rollback: Document reversible configuration and file-lifecycle behavior; revert only WI-owned deliverables at this stage.
