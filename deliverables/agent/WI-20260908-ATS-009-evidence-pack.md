---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: evidence-pack
status: active
dependencies:
  - path: WI-20260908-ATS-009-handoff.md
    reason: Approved runtime follow-up and documentation ownership
  - path: WI-20260908-ATS-008-evidence-pack.md
    reason: Completed source verification retained without rerunning suites
---

# Evidence Pack: WI-20260908-ATS-009

## Summary

**PENDING / BLOCKED BY EXEC TOOL POLICY.** MA reported rejection before restart execution. The tested artifact is ready but was not copied or launched; no runtime completion is claimed. Documentation is frozen at this boundary.

## Scope / DoD Check

- [x] Append the user's runtime approval to REQ002 without rewriting its source-complete status or earlier constraints. This is an approved follow-up, not a new feature REQ.
- [x] Record MA's execution rejection and preserve preceding dirty work.
- [x] Validate the three documentation files and freeze the documentation handoff.
- [ ] Receive MA's actual restart, artifact identity, environment preservation and HTTP/UI results before runtime closure.
- [ ] Complete WI009 after successful runtime evidence and final documentation checks; current runtime status remains pending.

## Reference Documents (Tier 0-2)

- Tier 0: injected STD-001/004/002/005 (`core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md` under `docs/standards/`); truncated standard sections supplemented from disk.
- Tier 1: `.claude/agents/docops.md`, `docs/policies/security-policy.md`.
- Tier 2: REQ002, WI008 handoff/evidence/summary, WI009 handoff, `docs/payment/index.md`, `docs/payment/acceptance-test-checklist.md`, `docs/design/runtime-storage-operations.md`.
- Skills: `create-wi-evidence-pack`, `validate-docs`. Existing handoff: `create-wi-handoff-packet`; configuration: `.claude/config/workspace.json` (ATS), `.claude/config/context-injection-rules.json` (DocOps tiers 0/1).

## Evidence Pointers

- Changed only `deliverables/user/REQ-20260908-ATS-002.md` (dated approval addendum), this evidence pack, and `deliverables/user/WI-20260908-ATS-009-summary.md`.
- Latest MA handoff: restart command rejected before execution with `CreateProcess Rejected` / `blocked by policy`. MA reports no environment-file write, Copy, Stop or Start occurred. DocOps did not execute or independently inspect runtime operations.
- Artifact supplied by MA: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/build-copy-polish/libs/ATStudio-0.0.1-SNAPSHOT.jar`. Earlier `runtime/` evidence pointers use `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908` as their base. No deployed hash/path or new PID is available.
- Final MA read-only confirmation: backend 19376, frontend 19932 and tunnel 2372 remain alive with their original creation times. The intended runtime copy `ATStudio-20260908-copy-polish.jar` does not exist; copy/start never executed. The old backend has not adopted the new mail wording.
- MA observed HTTP 200 for local port 5173 root, port 8080 `/api/tracks?page=1&size=1`, public root and that same public API. These confirm the old runtime's availability only, not restart or updated-artifact adoption; no new UI result was supplied.
- MA read-only DB preflight: storage journal `DONE` 30, due agreements 0; subscription 5 `CANCELLED`, plan 2, `YEARLY`, expiry 2027-09-08; orders `DONE` 5 / `EXPIRED` 1 / `IN_PROGRESS` 2; refunds `SUCCEEDED` 1. MA reports no runtime-file or data edits. DocOps ran no DB commands.
- Intended same DB/storage/SMTP/callback environment and tunnel preservation remain unverified for a restart because none occurred. No new SMTP/send/receipt, provider financial test, or post-restart HTTP/UI PASS exists.
- No WI009 note was added to either public payment document before interruption, so there is no completion claim to remove. WI008 evidence and earlier acceptance history remain unchanged.

## Commands & Outputs

- DocOps used read-only document/configuration reads, initial `git status --short --branch`, and a scoped `rg` check: branch matched `codex/v1-release-rehearsal-fixes`; no WI009 public-document note was found.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: exit 0; Tier 0, links, 676 traceability IDs and index PASS.
- Scoped `git diff --check`: exit 0. Because all three deliverable paths are untracked, a separate PowerShell content check also confirmed no trailing whitespace or conflict markers in each file.

## Tests

WI008 retains MA's backend 1,708 total / 1,689 passed / 19 skipped / 0 failures/errors and frontend 1,493 passed / 0 failures. Full suites were not rerun. These are source evidence, not restart or deployment verification.

## Risks / Rollback

No process-control fallback or policy workaround is authorized or attempted. DocOps made no product, runtime, DB, secret, client-worktree or commit changes and used no nested agents. Remove only this WI's addendum/deliverable content for an approved rollback; never restore whole dirty files or alter prior evidence.

## Follow-ups

`Depends On: WI-20260908-ATS-008`; `Blocks: -`. Documentation handoff is finished and frozen; WI009 runtime work remains pending, blocked by exec tool policy. Resume only after MA supplies an authorized execution outcome; there is no new feature REQ and no production GO. SR-93 remains OPEN.

## Related Documents

- [REQ002 Approval Addendum](../user/REQ-20260908-ATS-002.md)
- [WI009 Handoff](WI-20260908-ATS-009-handoff.md)
- [WI008 Source Evidence](WI-20260908-ATS-008-evidence-pack.md)
- [WI009 Summary](../user/WI-20260908-ATS-009-summary.md)

## 2026-09-08 Latest Follow-up: Backend Down / Launch Blocked

- Source: MA tool observations following the user's reaffirmed backend-restart approval under the existing WI009 handoff. This supersedes earlier current-state statements only; the earlier attempt remains historical evidence. DocOps did not rerun runtime checks or process commands.
- MA `Copy-Item` of the tested JAR to `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/ATStudio-20260908-copy-polish.jar` PASSED; SHA-256: `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`. `Stop-Process` for the verified old backend PID 19376 PASSED. `Start-Process java -version` PASSED with Java 17.0.12.
- Both hashtable-based and explicit-environment server launch commands were rejected before execution as `blocked by policy`. Latest reported state: port 8080 has no listener, PID 19376 is absent, frontend port 5173 / PID 19932 and cloudflare PID 2372 remain alive. **Backend DOWN; not restarted; runtime completion remains blocked.** Earlier HTTP 200 results do not describe this latest backend state.
- Active instruction: `approval_policy=never`. Local `config.toml` showed `on-request` during read-only inspection and was not changed. The local exec-policy check for `java -jar` returned empty `matchedRules`; this does not establish why review denied the commands. The rejection scope is unknown; Java execution is not established as generally forbidden.
- Existing source work remains complete; DB and credentials remain unchanged. No new backend PID, successful restart, post-restart HTTP/UI PASS, SMTP receipt, provider success or production GO is claimed.
- DocOps scope: append this dated follow-up only to WI009 evidence, WI009 summary and REQ002. No product/runtime/policy edits, alternate launch helper, policy workaround, nested agents or commit. Rollback, if approved, removes only these latest follow-up sections. `Blocks: -`; runtime closure remains pending and the documentation is frozen after validation.
- Follow-up validation: `python .agents/skills/validate-docs/scripts/validate_docs.py` PASS (exit 0; Tier 0, links, 676 traceability IDs and index). This is documentation validation only, not backend availability evidence.

## LATEST: 2026-09-08 20:11+ KST / Restart and HTTP Adoption Complete

This record supersedes all earlier blocked, backend-down and frozen current-state statements and pending checkboxes above; their historical evidence is retained. This is the existing approved WI009 under REQ002, not a new WI. Runtime facts below are supplied MA observations, not an independent DocOps runtime recheck.

### Scope / DoD and Execution Evidence

- [x] Receive successful approved restart, tested artifact identity, preserved environment and HTTP/source-delivery evidence; restart/HTTP adoption is complete. `Depends On: WI-20260908-ATS-008`; `Blocks: -`.
- User switched the visible UI to approval requests; effective `workspace-write` supported `require_escalated`. Approved explicit backend `Start-Process` succeeded at 20:08:51, PID 20860; startup log at 20:09:06: `Started AtStudioApplication in 14.856s`. Approved frontend start succeeded at 20:11:07, PID 20468. The specific earlier blocked predicate remains unknown; success after switching to approved execution does not prove that `never` alone universally forbids execution.
- Existing post-PC-reboot Cloudflare PID 12512 was preserved. Current URL: [Development Runtime](https://debian-reliable-round-responses.trycloudflare.com). Previous generation/server-sub URLs and earlier PID snapshots are obsolete operational references.
- Runtime/log root: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908`. Artifact: `ATStudio-20260908-copy-polish.jar`; SHA-256 `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`, unchanged from the tested WI008 build.
- Backend retained `local` profile, the same `atstudio` MySQL database, `ddl-auto=validate`, bootstrap `false`, repository `uploads` and `private-uploads` roots, and non-strict startup audit. Audit warning checked 30 / missing 10 describes known historical references, not a new regression or completed repair. Gmail/test Toss settings were preserved; callback, mail and CORS origins were set only in the process to the current URL. No real mail/provider calls or DB/schema/source changes occurred during restart.

### HTTP and Source-Delivery Results

| MA observation | Result |
| --- | --- |
| Backend 8080 `GET /api/tracks?page=1&size=1` | PASS 200 |
| Frontend 5173 `GET /` and proxied same API | PASS 200, both |
| Current public URL `GET /` and same API | PASS 200, both |
| Backend OPTIONS with current public origin | PASS 200; exact `Access-Control-Allow-Origin` |
| Public Vite-transformed `PaymentOperationsPage.tsx` | New Korean correction, reconciliation and receipt label checks all true |

Additional MA browser evidence: CUA opened the current public root unauthenticated. Initial loading resolved to HomePage with AT.M, creator hero, album/track lists and footer; AX and screenshot were inspected. **Basic public-page rendering PASS only.** Album covers displayed the existing `cover cannot be loaded` fallback; its exact origin was not investigated, and no causal link to the known startup missing 10 or all-media-health claim is established. No playback, login or form actions occurred. Tab closure after the read-only check was planned, not reported complete.

### Operational Pointers and Limits

- Logs under the runtime/log root: `backend-approved-restart.out.log`, `backend-approved-restart.err.log`, `frontend-approved-restart.out.log`, `frontend-approved-restart.err.log`, `cloudflare-reboot.err.log`. Future operations must verify actual PID ownership against these logs; the old `runtime-manifest.json` is stale and intentionally unchanged by WI009.
- Verification boundary: HTTP availability, source delivery and basic unauthenticated public-page rendering only, not full role/financial/UI regression. No fresh visual-admin/user acceptance or new SMTP receipt is claimed. Production/external acceptance limits remain; SR-93 stays OPEN. WI008's prior backend 1,708 total / 1,689 passed / 19 skipped / 0 failures/errors and frontend 1,493 passed remain prior source-test evidence; full suites were not rerun.
- DocOps changed only REQ002, WI009 evidence and WI009 summary by appending this latest status. No code, runtime/security settings, manifest, client worktree, commit or nested-agent action. Approved documentation rollback would remove only these appended sections, preserving prior content and unrelated work.

### Documentation Validation

- [x] `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS, exit 0; Tier 0, internal links, 676 traceability IDs and index. `git diff --check`: PASS, exit 0 (existing CRLF conversion warnings only).
- All three deliverables are untracked: separate `git diff --no-index --check` against pre-edit temporary copies produced no diagnostics; PowerShell confirmed unchanged historical prefixes after CRLF/LF normalization, no trailing whitespace and no conflict markers. No-index exit 1 indicates content differences here, not a whitespace failure. WI009 documentation closure is complete within the latest restart/HTTP/basic-public-rendering boundary.
