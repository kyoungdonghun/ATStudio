---
version: 1.1
last_updated: 2026-09-08
project: ATS
owner: docops
category: work-summary
status: active
dependencies:
  - path: ../agent/WI-20260908-ATS-009-evidence-pack.md
    reason: Successful latest runtime adoption and retained historical attempts
---

# WI-20260908-ATS-009 Summary

## Current Summary

**WI009 restart/HTTP adoption is complete at MA's 2026-09-08 20:11+ KST
evidence boundary.** See [the latest successful evidence](../agent/WI-20260908-ATS-009-evidence-pack.md#latest-2026-09-08-2011-kst--restart-and-http-adoption-complete)
and the [central current snapshot](../../docs/payment/index.md#2026-09-08-source-and-runtime).
The failed attempts below are historical, not current backend status. No fresh
SMTP or complete role/financial acceptance is claimed. REQ002's subsequently
approved WI010 depends on WI009 and is complete at the bounded check/documentation
and verified source/test/documentation commit/push `7eae086` boundary. This
later documentation receipt is not yet claimed committed/pushed. MA retains
browser/runtime/Git ownership. SR-93 remains OPEN.

## Historical Attempt: Rejected Before Execution

**Pending: blocked by exec tool policy before execution.** MA reports no environment-file write, artifact copy, Stop or Start occurred. The tested JAR is ready but was not copied or launched. This turn did not complete a restart.

REQ002's source-complete record and prior history are preserved; runtime approval is a dated addendum to the same REQ. MA confirmed the original backend 19376, frontend 19932 and tunnel 2372 remain alive, and local/public root and API return HTTP 200. This is old-runtime availability only: the new runtime JAR does not exist and the old backend has not adopted the new mail wording. No new SMTP or provider financial test, post-restart HTTP/UI verification, or production approval is claimed.

Only the REQ addendum and WI009 evidence/summary were changed. Public payment docs and product files were untouched by WI009. Documentation handoff is finished and frozen; runtime work remains pending an authorized MA outcome. No alternate process-control or policy workaround was attempted.

## Related Documents

- [Evidence Pack](../agent/WI-20260908-ATS-009-evidence-pack.md)
- [REQ002](REQ-20260908-ATS-002.md)

## Historical Attempt: 2026-09-08 Backend Down

Latest MA tool observations supersede the earlier runtime snapshot; that attempt is retained above. The user reaffirmed restart approval under WI009. Tested-JAR copy to `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/ATStudio-20260908-copy-polish.jar` PASSED with SHA-256 `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`; stopping verified backend PID 19376 PASSED. `Start-Process java -version` PASSED (Java 17.0.12), but both hashtable-based and explicit-environment server launches were blocked by policy before execution.

**Backend DOWN, not restarted:** port 8080 has no listener and PID 19376 is absent; frontend 5173 / PID 19932 and cloudflare PID 2372 remain alive. Source work is complete; DB and credentials are unchanged. No runtime completion or post-restart HTTP/UI verification is claimed; DocOps did not independently recheck runtime state.

Active `approval_policy=never` governs; the read-only local `config.toml` value `on-request` was not changed. Empty `matchedRules` from the local `java -jar` exec-policy check does not explain the review denial or establish a general Java prohibition. Rejection scope remains unknown. Only these three documentation follow-ups are in scope; no alternate launch helper, policy workaround, nested agents or commit. Documentation freezes after validation; WI009 runtime closure stays blocked.

## Successful Evidence: 2026-09-08 20:11+ KST / Restart and HTTP Adoption Complete

**WI009 restart/HTTP adoption is complete; `Blocks: -` at that closure.** This MA evidence supersedes earlier blocked/backend-down/frozen states, retained above as history. The subsequently approved WI010 dependency is recorded in REQ002. DocOps did not independently recheck runtime state.

- Approved execution succeeded after the user switched the visible UI to approval requests (`workspace-write`, `require_escalated`). Backend started at 20:08:51, PID 20860, with startup confirmed at 20:09:06 in 14.856s; frontend started at 20:11:07, PID 20468. Earlier rejection conditions remain unknown; this does not establish a universal prohibition caused by `never` alone.
- Preserved post-PC-reboot Cloudflare PID 12512 serves [the current development URL](https://debian-reliable-round-responses.trycloudflare.com). Previous generation/server-sub URLs are obsolete. The unchanged WI008-tested `ATStudio-20260908-copy-polish.jar` has SHA-256 `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`.
- Same local profile/MySQL, `ddl-auto=validate`, bootstrap `false`, public/private storage roots, non-strict audit and Gmail/test Toss settings were preserved; callback/mail/CORS origins changed only in the process. Audit checked 30 / missing 10 remains known historical-reference debt. No real mail/provider calls or DB/schema/source changes occurred during restart.
- Five GETs passed 200: backend tracks API, frontend root/proxied API, public root/API. Backend public-origin OPTIONS passed 200 with exact CORS origin; public Vite-transformed `PaymentOperationsPage.tsx` served the new Korean correction/reconciliation/receipt labels (all checks true).
- MA CUA AX/screenshot inspection of the unauthenticated current public root confirmed loading resolved to HomePage with AT.M, creator hero, album/track lists and footer: basic public-page rendering PASS only, not full role/financial/UI regression. Album covers showed existing `cover cannot be loaded` fallback; exact origin and any link to startup missing 10 are uninvestigated, so all-media health is not claimed. No playback/login/form actions; tab closure was planned after the read-only check.
- Artifact/log root: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908`. Use actual PIDs and `backend-approved-restart.out.log`/`.err.log`, `frontend-approved-restart.out.log`/`.err.log`, `cloudflare-reboot.err.log`; `runtime-manifest.json` is stale and intentionally unchanged.
- HTTP/source delivery is not fresh visual-admin/user acceptance or SMTP receipt. Prior WI008 backend 1,689 passed / 19 skipped and frontend 1,493 passed were not rerun. Production/external limits and SR-93 OPEN remain. Only REQ002 and WI009 evidence/summary were appended; no code/runtime/security/manifest/client-worktree edits, commit or nested agents. Documentation checks are recorded in the [Evidence Pack](../agent/WI-20260908-ATS-009-evidence-pack.md).
