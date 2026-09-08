---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260908-ATS-015-evidence-pack.md
    reason: Finding paths, defenses and test-substance evidence
---

# WI-20260908-ATS-015 Summary

Review complete on main `8161f0a`, under approved REQ004. **Security release gate: HOLD (2 P1, 4 P2; no P0 found).** Static code and cached-library bytecode review only; no fresh tests or production exploit executed.

| Priority | Finding | Impact / gate |
|---|---|---|
| P1-01 | Refresh JWT accepted as API Bearer without stored-hash/purpose validation | Logout/reset/rotation cannot stop that token's API access before refresh expiry. Release blocker. |
| P1-02 | Validation exceptions log rejected password/email/phone values | Application emits credentials/PII despite generic client errors. Release blocker. |
| P2-03 | Same-second refresh generation can return the identical token | Rotation or immediate logout/re-login can retain/reissue the old capability. |
| P2-04 | Token-refresh interceptor lacks initiating-session ownership | Late success restores/replaces tokens after logout/account switch; late failure can clear a new session. |
| P2-05 | Reset-token validation precedes the user lock | Two overlapping requests can consume one reset token and overwrite the first successful password change. |
| P2-06 | Password-email verification is required for social refresh too | New social accounts lose their session at refresh; fix before enabling that lifecycle. Deployed enablement unverified. |

No confirmed registration mass assignment, browser-role authorization bypass, auth-page XSS sink or open redirect was found in the reviewed paths. Existing defenses include forced USER registration, server-derived ownership/current DB roles, ADMIN checks, salted dual rate-limit budgets, one-replay fencing, safe internal return targets and minimal mail logs. These do not close the six findings.

The [evidence pack](../agent/WI-20260908-ATS-015-evidence-pack.md) contains exact file/line pointers, trigger/impact, checked defenses, a three-way requirement/code/test map and bounded MA test commands. Existing mocks omit the cross-layer/race scenarios; test PASS would not by itself rebut the findings.

Only this summary and the WI evidence pack were created with apply_patch. No source/tests/config changes, Git writes, Gradle/npm execution, provider/SMTP/live API access, external credential reads or subdelegation occurred. Historical untracked deliverables were untouched.

MA final results: backend 1,708 total, 1,689 passed, 19 skips (18 gated MySQL + 1 platform symlink), 0 failures; frontend 112 files / 1,493 passed. Anonymous/CORS rejections support their narrow boundaries. Vite OPTIONS 204 without ACAO is not a bypass; DEV Swagger and public SPA HTML do not establish production OpenAPI exposure. PG did not independently inspect central logs.

Both P1 paths were reported early in commentary; this task has no callable `send_input` tool. WI015 is complete for WI018 integration; MA owns central tests, operational/dependency review and any remediation request. No production or REQ004 closeout approval is implied.
