
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A062: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a062). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-032
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: WI-20260817-ATS-029, WI-20260817-ATS-030 (partial), WI-20260817-ATS-031 (interrupted)
Blocks: WI-20260817-ATS-023, WI-20260817-ATS-024, WI-20260817-ATS-025, WI-20260817-ATS-026

[WI SUMMARY]
Why: Preserve accurate, reproducible evidence for the completed non-external isolated rehearsal after WI-031 was interrupted before it could write its own deliverables.
Scope (in/out): Record only the verified local browser/API/runtime observations below. Do not change product source, application configuration, tracked documentation, database data, branches, or external service state. Do not perform or claim Gmail, Toss, refund, backup/restore, Cloudflare, or production validation.
DoD: Produce a concise user summary and agent evidence pack that distinguish UI, API, configuration, and persisted-state evidence; retain the partial/interrupted status of prior WIs; name the remaining explicit gates.
Constraints/Forbidden: Never print credentials, JDBC URLs, tokens, database names, raw provider payloads, or personal data. The existing client acceptance runtime on 5173/8080 and public tunnel are out of scope. Treat the local runner configuration as an untracked, disposable rehearsal artifact, not a source-code patch.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Separates passed local rehearsal scope from unexecuted external-effect scope.
- [ ] Explains the observed local CORS 403 and its isolated runtime-only remediation without claiming a repository code change.
- [ ] Records browser evidence for public catalogue/player, administrator access, active subscriber download, and persisted download history.
- [ ] States that WI-030 was partial and WI-031 was interrupted; this WI is the corrective evidence record.
Quality:
- [ ] Creates both required two-set deliverables.
- [ ] Uses pointers/commands and no secret-bearing values.
- [ ] Runs `git diff --check` and reports the result.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- deliverables/user/WI-20260817-ATS-029-summary.md
- deliverables/agent/WI-20260817-ATS-029-evidence-pack.md
- deliverables/user/WI-20260817-ATS-030-summary.md
- deliverables/agent/WI-20260817-ATS-030-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-031-handoff.md

Runtime Evidence (read-only; do not print secret-bearing file contents):
- %LOCALAPPDATA%\\ATStudio\\release-rehearsal-WI-20260817-ATS-031\\backend.log
- %LOCALAPPDATA%\\ATStudio\\release-rehearsal-WI-20260817-ATS-031\\rehearsal-static-proxy.mjs

Verified observations supplied by MA:
- The isolated backend used a fresh schema-and-seed rehearsal DB and booted with Hibernate `ddl-auto=validate`; public demo catalogue fixture contains 10 tracks, one 10-track album, and category tags.
- Public browser UI at loopback proxy: catalogue, tag filter, detail play, album full play, visible waveform/progress, and no browser console error.
- Initial browser password login failure was reproduced as an HTTP 403 caused by a missing `127.0.0.1` origin in the disposable runner CORS allowlist. Direct backend login and `/api/users/me` were already successful.
- After only the disposable runner CORS allowlist was expanded, browser login succeeded for a QA admin; `/admin/dashboard` and `/admin/payments` loaded with no browser console error.
- A QA subscriber then downloaded one demo track through the UI. `/downloads` displayed one persisted history item and a daily count of `1 / 20`; browser download-event capture timed out, so do not claim that browser download-event API success. The persisted record is the evidence that the application download flow completed.
- SMTP and provider endpoints were loopback fail-closed; no Gmail, Toss, refund, payment, Cloudflare tunnel, backup/restore, or production action was executed.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-032-summary.md:
- Korean concise summary of passed scope, CORS finding/remediation, evidence boundaries, and next approval gates.
Agent-facing -> deliverables/agent/WI-20260817-ATS-032-evidence-pack.md:
- Repro steps, commands, evidence classification, limitations, rollback/cleanup, and next WI dependencies.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-032-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required; do not embed secret-bearing paths/content beyond the approved runtime root pointer.
Tests: `git diff --check` required. Do not rerun long test suites or start/stop existing runtimes.
Rollback: State that no tracked product change occurred; disposable runtime cleanup is separate and must not affect client acceptance runtime.
