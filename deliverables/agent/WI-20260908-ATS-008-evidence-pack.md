---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: WI-20260908-ATS-008-handoff.md
    reason: Approved documentation ownership and closure gate
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved copy-only requirement
---

# Evidence Pack: WI-20260908-ATS-008

## Summary

REQ002/WI008 are complete and frozen at the source-delivery boundary after final MA aggregate/browser results and document checks. No production approval is implied.

## Scope / DoD Check

- [x] Current admin display names and both exact/trimmed confirmation contracts updated; old confirmation phrase rejected in both flows.
- [x] Korean mail source completion distinguished from the dated English Gmail/spam observation. Receipt display describes stored evidence, not refund aggregation.
- [x] Previous WI001-005/REQ001, dated counts, acceptance tables and dirty work preserved. The index explicitly identifies its existing runtime table as the pre-REQ002 WI005 snapshot.
- [x] Final MA aggregate results recorded and REQ002 closed after document checks; initial failed aggregate preserved.

## References and Changed Paths

Tier 0: injected STD-001/004/002/005; Tier 1: `.claude/agents/docops.md`, `docs/policies/security-policy.md`; Tier 2: approved REQ, supplied WI008 handoff, WI006/007 evidence and related current payment/UI documents. Skills used: `validate-docs`, `create-wi-evidence-pack`; existing handoff uses `create-wi-handoff-packet` and `.claude/config/context-injection-rules.json`.

- `docs/payment/admin-operations-guide.md`: display labels, receipt status map/caption, existing safe URL/raw fallback, payment exact versus general local trimmed execute phrase.
- `docs/payment/known-limits-and-next-steps.md`, `docs/SR/SR-93.md`: small copy items source-complete; delivery and other future scope remain open. SR-93 edits are maintenance disposition only.
- `docs/payment/acceptance-test-checklist.md`, `docs/payment/index.md`: a short later copy follow-up, retaining earlier tables/counts and runtime evidence unchanged.
- `docs/standards/glossary.md`: existing Local Subscription Correction entry gains display synonym only; no canonical rename.
- `docs/ui/modal-list.md`, `docs/ui/screen-flow.md`, `docs/design/usecase/user-subscription.md`: exact current phrase substitution only.
- `deliverables/user/REQ-20260908-ATS-002.md`: progress and source-delivery completion after final gates; this evidence and its user summary are the two WI008 deliverables.

## Verification

- WI006: 38 focused unit tests pass, including UTF-8 MIME readback and escaping. MA also reviewed the three service diffs as string-only with escaping unchanged.
- WI007: 139 focused component tests plus TypeScript/ESLint/Prettier pass. MA extended ownership within the already-approved corresponding-test scope; the coverage-test extension passed 24/24 and source is refrozen. These focused counts overlap full suites and are not additive.
- Initial MA frontend aggregate: **1,493 total / 1,490 passed / 3 failed**, stale UI expectations in `adminSubscriberGaps.coverage.test.tsx`. MA reviewed the test-only changes. This failed run is preserved; no product failure was established by those failures.
- Final MA frontend r2: **exit 0, 112 files / 1,493 passed / 0 failures**, 45.27s. Coverage: statements 90.22%, lines 92.8%, functions 91.18%, branches 82.75%. Fixture-file hash stayed stable during the run. Log: `runtime/copy-polish-frontend-test-r2.log`.
- MA reports TypeScript, ESLint, frontend build and full frontend formatting PASS.
- MA final backend: isolated `build -I runtime/copy-polish-build.gradle` exit 0, BUILD SUCCESSFUL in 3m45s; 191 JUnit XML suites, 1,708 tests / 1,689 passed / 19 skipped / 0 failures / 0 errors. JaCoCo gate PASS: lines 87.69%, methods 85.40%, branches 73.07%. Log: `runtime/copy-polish-backend-build.log`; artifacts only in `runtime/build-copy-polish/`. Temporary H2 schemas only; no actual MySQL, live provider or mail calls. Running JAR hash and original runtime process unchanged.
- MA reports synthetic-browser PASS at widths 1440/390, API-stub-only, new labels/header/status, mobile receipt horizontal scrolling without page overflow, and confirmation text fitting at 390. MA visually inspected screenshots; DocOps did not run the browser.
- MA runtime directory (as recorded by WI006): `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908`. Above `runtime/` pointers use that base. Other artifacts: `copy-polish-frontend-test.log` (first failure), `copy-polish-{typecheck,lint,frontend-build,format}.log`, `copy-admin-browser.cjs`, `copy-admin-browser.log`, `copy-receipts-{width}.png`, `copy-correction-confirmation-{width}.png` (width 1440 or 390). MA docs log: `copy-polish-docs.log`.
- MA reports the running JAR SHA-256 unchanged. This is not deployment or actual delivery of changed mail.
- DocOps ran `python .agents/skills/validate-docs/scripts/validate_docs.py`: exit 0, 675 valid traceability IDs, Tier 0/link/index checks PASS; MA independently reported the same. Scoped `git diff --check`: exit 0 (existing glossary CRLF normalization notice only).

## Risks / Rollback

No SMTP, provider, DB, runtime restart, commit or nested-agent action was performed by DocOps. WI008 changes only its ten explicitly owned existing documents and two deliverables. The separately authorized WI007 coverage-only extension is recorded in its own handoff/evidence, not hidden as documentation scope.

No updated-mail delivery, spam mitigation, refund aggregate/API or SR-93 production GO is claimed. Rollback removes only this WI's targeted document hunks; do not revert full dirty files or rewrite dated acceptance evidence.

## Follow-up

`Blocks: -`. All REQ002 WIs are complete. No further WI is blocked by WI008; preserve SR-93 OPEN and its target-production gates. Source and owned documentation are frozen; no commit or runtime action is requested.

## Related Documents

- [Approved REQ](../user/REQ-20260908-ATS-002.md)
- [WI008 Handoff](WI-20260908-ATS-008-handoff.md)
- [WI006 Evidence](WI-20260908-ATS-006-evidence-pack.md)
- [WI007 Evidence and Test Extension](WI-20260908-ATS-007-evidence-pack.md)
- [WI008 Summary](../user/WI-20260908-ATS-008-summary.md)
