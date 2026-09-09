---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: evidence-pack
status: blocked
related_wi: WI-20260817-ATS-008
dependencies:
  - path: WI-20260817-ATS-008-handoff.md
    reason: Single-attempt execution and retention contract
  - path: ../user/REQ-20260816-ATS-001.md
    reason: Approved DB-only isolation boundary
  - path: ../user/WI-20260816-ATS-002-summary.md
    reason: Historical incomplete-preflight-capture cause
  - path: ../user/WI-20260817-ATS-007-summary.md
    reason: Documented predecessor readiness result
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A025: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a025). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-008

## Change Summary

- Created the required pre-execution handoff packet and the two final WI
  deliverables only.
- Performed one bounded wrapper `Preflight` and, after its complete safe
  capture, one wrapper `Observe` against one generated in-memory target.
- Stopped terminally when the observation capture failed strict safe-field
  cardinality validation. No raw output was retained or used for diagnosis.

## Scope / DoD Check

- [x] Used next valid `WI-20260817-ATS-008` after scanning current 2026-08-17
  agent WI identifiers.
- [x] Generated exactly one fresh guarded target and an in-memory-only
  high-entropy correlation secret. Retained only non-reversible token
  `accf249a60466b72fa701417966859727b7d7cbf1d3e9bbdd676f1f76f23bf3c`.
- [x] Proved the explicit wrapper `Preflight` and wrapper `Observe` inputs
  were the same in-memory target (`targetCorrelation=PASS`); retained no
  target identifier.
- [x] Parsed every expected preflight safe field. Source count was `43`, its
  check was `PASS`, and the active expectation was `UNRECORDED`.
- [x] Invoked wrapper `Observe` exactly once only after complete preflight
  capture, using the opaque external bundle without opening or recording it.
- [x] Stopped with `OBSERVE_FIELD_COUNT_MISMATCH`; no raw observation fields,
  controlled refusal reason, manifest, or cleanup result were retained.
- [x] Did not invoke standalone `Create`, `Validate`, or `Drop`; did not
  regenerate a target, retry, inspect raw output, change an expectation, or
  modify product/source/configuration files.
- [x] Kept existing databases, external services, app/browser, mail, payment,
  refund, OAuth, and Git actions out of scope.
- [ ] Current manifest expectation proposal: not supported by this incomplete
  observation capture.

## Sanitized Action Results

| Action / Evidence | Result |
| --- | --- |
| Explicit wrapper `Preflight` | `1; PASS` |
| Preflight source count / check | `43; PASS` |
| Preflight manifest expectation | `UNRECORDED` |
| Wrapper `Observe` | `1; capture rejected` |
| Target correlation | `PASS` |
| Observation manifest / refusal reason | `NOT_RETAINED` |
| Guarded cleanup-after-failure / target removal | `UNCLEAR` |
| Direct `Create` / `Validate` / `Drop` | `0 / 0 / 0` |
| Further DB action after stop | `0` |
| Existing database target / change | `0 / 0` |

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved execution and fail-closed boundary |
| 0 | `docs/standards/development-standards.md` | QA traceability and verification controls |
| 0 | `docs/standards/documentation-standards.md` | Deliverable convention |
| 0 | `docs/standards/glossary.md` | Canonical WI terminology |
| 0 | `docs/standards/evidence-pack-standard.md` | Evidence-pack structure |
| 1 | `docs/policies/quality-gates.md` | Quality-gate mapping |
| 1 | `docs/policies/security-policy.md` | Opaque input and secret-safe evidence boundary |
| 1 | `docs/policies/access-control-policy.md` | Least-privilege database access boundary |
| 1 | `docs/architecture/system-design.md` | WI traceability model |
| 2 | `scripts/database/README.md` | Guarded disposable-database procedure |
| 2 | `scripts/database/bootstrap-disposable-mysql.ps1` | Wrapper preflight-first and opaque-input flow |
| 2 | `scripts/database/DisposableMysqlBootstrap.java` | Current source guard, observation, refusal, and cleanup logic |

Injection rules applied: `.claude/config/context-injection-rules.json` was
read for assignee `qa-integ`; quality, security, access, and WI-workflow
context were selected. The handoff packet contains the complete pointer set.

## Evidence Pointers

- `deliverables/agent/WI-20260817-ATS-008-handoff.md`: pre-execution contract,
  scope, strict parser conditions, and output limits.
- `deliverables/user/REQ-20260816-ATS-001.md`: approved DB-only boundaries and
  no-runtime/no-browser restriction.
- `deliverables/user/WI-20260816-ATS-002-summary.md`: historical target-
  preflight-capture block this WI was authorized to isolate.
- `deliverables/user/WI-20260817-ATS-007-summary.md`: predecessor readiness
  addendum without scope expansion.
- `scripts/database/README.md` (Safety Contract and Future Evidence Procedure):
  43-table/`UNRECORDED` contract and no current create/validate path.
- `scripts/database/bootstrap-disposable-mysql.ps1`
  (`Invoke-BootstrapJava` and preflight-first entry flow): credentials are read
  only after wrapper preflight returns.
- `scripts/database/DisposableMysqlBootstrap.java` (`run`,
  `validateBeforeConnection`, `enforceManifestAction`, `Bootstrap.create`, and
  `cleanupCreatedDatabase`): safe output, source-count guard, expected
  unrecorded behavior, and exact-target cleanup implementation.

## Reproduction / Verification

- Database action: one in-memory process generated target and correlation
  material, executed wrapper `Preflight`, strictly parsed its safe fields, then
  executed exactly one wrapper `Observe` with the same target. It emitted only
  the sanitized result above; no command transcript, target, bundle path,
  connection detail, credential, raw output, or row was retained.
- Documentation validation: `python .agents/skills/validate-docs/scripts/validate_docs.py`
  returned exit code `0`; Tier 0 documents, internal links, 603 supported
  traceability IDs, and the document index all passed.

## Results

- Terminal result: `BLOCKED_OBSERVE_CAPTURE`.
- First unmet condition: `OBSERVE_FIELD_COUNT_MISMATCH`.
- Decision: do not infer an expected refusal, manifest counts/hash, or target
  removal from incomplete capture; do not propose or record a manifest
  expectation.

## Quality-Gate Mapping

| Gate | Sanitized evidence |
| --- | --- |
| G1 | One fresh in-memory target passed preflight; existing databases were not targeted. The observation uncertainty stopped all further action. |
| G3 | Source-level `43` count/check passed before opaque input. Current MySQL manifest evidence was not accepted without complete bounded capture. |
| G4 | No standalone drop is permitted. Target removal remains `UNCLEAR` and is reported rather than guessed. |
| G5 | Only a non-reversible token and approved safe result fields were retained; no secret, path, raw output, or row was recorded. |
| G6 | No app/browser or external workflow was invoked. |

## Risks / Rollback

- Residual risk: the one observation's exact target removal is unverified from
  sanitized evidence. This WI prohibits every further DB action, including a
  cleanup command.
- Rollback: no product/source/configuration/manifest-expectation change exists.
  The only changes are this handoff packet, summary, and evidence pack; no
  rollback is required for product behavior.

## Follow-ups

- None. WI-008 itself authorized only one observation attempt and did not
  authorize further action; later work requires separate approval.
