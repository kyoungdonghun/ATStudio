[WI HEADER]
WI ID: WI-20260809-ATS-021
REQ: REQ-20260809-ATS-001
Agent: qa-fe
Depends On: WI-20260809-ATS-020
Blocks: WI-20260809-ATS-022

[WI SUMMARY]
Why: Establish the first actual-browser baseline on frozen product code by checking the shared public shell, Notice pages, error pages, deep links, public/guard behavior, console/network health, and representative responsive/keyboard behavior before stateful feature WIs begin.
Scope (in/out): Execute matrix rows `G-PUBLIC`, the read-only portions of `G-AUTH/G-SUB/G-PAY/G-BUS/G-ADMIN`, `PUB-08/09`, `ERR-01/02`, and `SH-01`, plus route-level loading fallback and deep-link refresh across each route family. Use the current local acceptance runtime, and the public Cloudflare URL only after verifying it points to the same local build. Capture screenshots, DOM/accessibility observations, browser console, sanitized network status, redirect targets, and viewport results. Do not change product code/current-state docs, mutate business data, invoke payment/mail/file side effects, alter browser auth fixtures beyond login/logout needed for guard reads, restart runtime without evidence, or touch the intentional ZIP.
DoD: Every owned matrix row is classified `PASS`, `FAIL`, `BLOCKED`, or `N/A`; local and public runtime identity is checked; all public/denied-role redirects and deep-link refreshes have exact targets; Notice list/detail, 404, server-error and Header states have desktop/mobile/keyboard evidence; uncaught console/network errors are captured; findings are recorded with source expectation and no product fix is applied during this initial audit WI.
Constraints/Forbidden: Baseline remains `e343c20` and product-code freeze remains active. Do not inspect secret configuration or output tokens. No real provider, mail, upload/download, schema/data deletion, branch operation, dependency change, or runtime deployment. If the runtime is unavailable, diagnose read-only first and record `BLOCKED`; do not silently substitute a historical public URL.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] Verify local frontend and API health, then verify the current public URL resolves to the same runtime before using it as evidence.
- [ ] Check Header brand, public/USER/ADMIN navigation variants where existing sessions permit, search target construction, theme, active item, Login/logout/subscribe affordances, and mobile menu close paths.
- [ ] Check Notice list and one valid/invalid detail, including loading, empty/error/not-found and attachment boundary without downloading a private or external file.
- [ ] Check explicit Server Error and wildcard Not Found pages, safe navigation, and absence of stack/secret output.
- [ ] Deep-link refresh representative public, auth-required, subscriber-only, payment-only, business-only, admin-only, error, and contextual admin routes.
- [ ] Record exact anonymous denied-route redirects without performing downstream mutations.
- [ ] Check 1440x900, 1024x768, 390x844, and targeted 360x800 layouts for Header, Notice, error, route fallback, fixed PlayerBar/footer overlap, long text, and horizontal scroll.
- [ ] Check keyboard focus/order and accessible names for Header search/nav/mobile menu and Notice/error primary actions.
- [ ] Capture browser console and failed network requests for every failure; do not mark a visual page PASS when its required request failed.
      Performance:

- [ ] Reuse one controlled browser session and bounded screenshots; close extra tabs and avoid full-page capture when a focused viewport proves the assertion.
- [ ] Store large/raw browser evidence outside chat and summarize it by pointer.
      Quality:

- [ ] Every result references a matrix row, role/session, viewport, URL, expected source, and evidence pointer.
- [ ] Environment failure, fixture absence, and product defect are not conflated.
- [ ] No product/runtime/DB mutation occurs beyond normal read-only session/navigation behavior.
- [ ] WI deliverables pass Prettier, documentation validation, and whitespace checks.

[INPUT POINTERS]
Tier 0:

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:

- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
- docs/policies/security-policy.md

Tier 2:

- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/ui/screen-flow.md
- docs/design/usecase/user-notice.md
- docs/design/api-spec.md
- scripts/acceptance/

REQ/Context:

- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-019-inventory.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md
- AGENTS.md

Primary code entry points:

- frontend/src/router/index.tsx
- frontend/src/router/ProtectedRoute.tsx
- frontend/src/router/SubscriberRoute.tsx
- frontend/src/layouts/MainLayout.tsx
- frontend/src/layouts/Header.tsx
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/pages/public/NoticeListPage.tsx
- frontend/src/pages/public/NoticeDetailPage.tsx
- frontend/src/pages/error/
- frontend/src/api/client.ts
- frontend/src/api/notices.ts
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/controller/NoticeController.java
- src/main/java/com/atstudio/atstudio/controller/SpaForwardController.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-021-summary.md:

- Runtime identity, rows tested, pass/fail/blocked totals, material UX/API findings, limits, and next-WI readiness.

Agent-facing -> deliverables/agent/WI-20260809-ATS-021-evidence-pack.md:

- Environment preflight, scenario results, screenshot/log/network pointers, reproducible steps, finding IDs, cleanup, rollback, and WI-022 trigger.

Findings -> deliverables/agent/WI-20260809-ATS-021-findings.md:

- One row per defect/drift/blocker with expected source, actual evidence, severity, classification, and adjacent scope.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-021-handoff.md:

- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every matrix result. Screenshots must name row, viewport, and state; network/console evidence must be sanitized.
Tests: Browser and API read evidence only for this WI. Do not run stateful provider/file/mail scenarios. Record local/public health commands and any browser automation commands used.
Rollback: Browser navigation, theme/session/local-state changes must be restored where practical. WI documents/evidence can be removed; product/runtime/DB must remain unchanged.
