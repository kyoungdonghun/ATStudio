---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: docops
category: evidence-pack
status: complete
related_wi: WI-20260817-ATS-015
dependencies:
  - path: WI-20260817-ATS-015-handoff.md
    reason: Approved documentation scope, constraints, and acceptance criteria
  - path: ../user/REQ-20260817-ATS-009.md
    reason: Approved current-state remediation context
  - path: WI-20260817-ATS-014-evidence-pack.md
    reason: Dated React Router remediation and audit evidence
---

# Evidence Pack: WI-20260817-ATS-015

## Summary

- Corrected active current-state documentation for the 43-table source /
  `UNRECORDED` MySQL-manifest boundary and the completed React Router 7.18.2
  remediation, without changing historical deliverables or archived designs.

## Scope / DoD Check

- [x] Replaced the active 39-table claim in payment known limits with the
  verified 43-table/43-entity source fact and `UNRECORDED` manifest state.
- [x] Replaced active Router 6 vulnerability/migration wording in SR-42 and
  SR-93 with the completed 7.18.2 remediation and date-specific audit result.
- [x] Kept SR-93 `OPEN`; current MySQL evidence, production deployment,
  provider, backup, monitoring, and release gates remain open.
- [x] Preserved historical 41-table and WI-067 42-table evidence as historical.
- [x] Did not modify source code, dependencies, scripts, databases, archived
  designs, historical REQ/WI/evidence records, or output artifacts.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and language/traceability rules. |
| 0 | `docs/standards/development-standards.md` | Tier 0 traceability and two-set deliverable rules. |
| 0 | `docs/standards/documentation-standards.md` | Metadata and documentation format. |
| 0 | `docs/standards/glossary.md` | Canonical terminology. |
| 1 | `docs/policies/security-policy.md` | Security documentation boundary. |
| 1 | `docs/policies/quality-gates.md` | Documentation quality-gate context. |
| 2 | `docs/payment/known-limits-and-next-steps.md` | Active payment boundary. |
| 2 | `docs/payment/system-overview.md` | Existing current 43-table/`UNRECORDED` reference. |
| 2 | `docs/payment/feature-inventory.md` | Existing current 43-table/`UNRECORDED` reference. |
| 2 | `docs/SR/SR-42.md` | Active public-acceptance safety addendum. |
| 2 | `docs/SR/SR-93.md` | Active production-readiness SR. |

## Evidence Pointers

- `src/main/resources/schema.sql:24` through
  `src/main/resources/schema.sql:1162`: source contains 43 `CREATE TABLE`
  statements, verified without a database connection.
- `src/main/java`: contains 43 direct `@Entity` declarations, verified with an
  annotation-only search that excludes annotations such as `@EntityGraph`.
- `scripts/database/DisposableMysqlBootstrap.java:53`,
  `scripts/database/DisposableMysqlBootstrap.java:151`, and
  `scripts/database/DisposableMysqlBootstrap.java:586`: current manifest state
  is `UNRECORDED` and guarded Create/Validate paths fail closed.
- `frontend/package.json:21`: pins `react-router-dom` to `7.18.2`.
- `frontend/package-lock.json:14` and `frontend/package-lock.json:4371`:
  resolve `react-router-dom` and `react-router` to `7.18.2`.
- `deliverables/agent/WI-20260817-ATS-014-evidence-pack.md:108` and
  `deliverables/agent/WI-20260817-ATS-014-evidence-pack.md:116`: record the
  2026-08-17 post-remediation `npm audit --omit=dev --json` result of 0
  vulnerabilities.
- `docs/payment/known-limits-and-next-steps.md:39` and
  `docs/payment/known-limits-and-next-steps.md:46`: classify prior disposable
  proof as historical and set current 43-table/`UNRECORDED` state.
- `docs/SR/SR-42.md:9` and `docs/SR/SR-42.md:23`: record the Router remediation
  and retain the public-tunnel/non-production boundary.
- `docs/SR/SR-93.md:24`, `docs/SR/SR-93.md:26`, and `docs/SR/SR-93.md:43`:
  retain SR-93 `OPEN`, record the dated audit outcome, and identify Router 6
  audit material as historical.
- `docs/payment/system-overview.md:87` and
  `docs/payment/feature-inventory.md:158`: already reflected the
  43-table/`UNRECORDED` state and were intentionally not edited by this WI.

## Commands And Results

| Command | Result |
| --- | --- |
| `(rg '^CREATE TABLE' 'src/main/resources/schema.sql' \| Measure-Object -Line).Lines` | PASS: `43`. |
| `(rg -l '^\s*@Entity(?:\s\|$)' 'src/main/java' \| Measure-Object -Line).Lines` | PASS: `43`. |
| `rg -n '"react-router"\|"react-router-dom"' 'frontend/package-lock.json'` | PASS: both resolve to `7.18.2`. |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: Tier 0 documents exist; no broken internal links; 616 supported traceability IDs; all documents listed in the index. |
| `git diff --check` | PASS: no whitespace errors. Existing unrelated CRLF advisory warnings did not fail the command. |
| Focused `rg` stale-claim searches over the five handoff-identified current documents | PASS: no active 39-table claim or unresolved current Router 6 finding. The only Router 6 hit is the explicit upgrade-from context in SR-42; 41/42-table hits are expressly historical. |

## Risks / Rollback

- Risk: The worktree contains unrelated pre-existing modifications, including
  current 43-table/`UNRECORDED` updates in `system-overview.md`,
  `feature-inventory.md`, and SR-93. This WI preserves those changes.
- Rollback: Reverse only this WI's hunks in
  `docs/payment/known-limits-and-next-steps.md`, `docs/SR/SR-42.md`, and
  `docs/SR/SR-93.md`, then remove this WI's two deliverables. Do not revert
  pre-existing current-state updates or any historical evidence.

## Follow-ups

- WI-016: obtain separately approved actual MySQL manifest evidence for the
  current 43-table source snapshot.
- SR-93 remains open for non-router production readiness and release approval.
