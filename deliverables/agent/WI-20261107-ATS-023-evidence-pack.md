---
version: 1.0
last_updated: 2026-11-07
project: ATS
owner: PG
category: evidence-pack
status: content-review-in-progress
dependencies:
  - path: WI-20261107-ATS-023-handoff.md
    reason: Approved independent privacy review
  - path: ../user/REQ-20261107-ATS-006.md
    reason: Selected commit and push authorization without server effects
---
# Evidence Pack: WI-20261107-ATS-023

## Summary

Triaged the pending visible tree, preserved prior exclusions, and cleared 27 exact
source/template/client-guide files. Public document exports, actual staged objects
and the resulting outgoing commits require the subsequent exact-object gates.

## Scope / DoD Check

- [x] Initial visible snapshot: 473 paths, with category/location-only findings.
- [x] All 63 prior exclusion hashes remain identical; originals are preserved.
- [x] Source/template literal and configuration-context review completed.
- [x] Static client guide inspected for account details, scripts, links and embedded data.
- [x] No preexisting outgoing commits or local tracked push workflows found.
- [ ] Final selected public document/export delta must be independently cleared.
- [ ] MA's actual staged objects and resulting commits must match exact allowances.

This pack records the preparation-time review. Later ignored review receipts may
close the final two gates without rewriting already committed evidence bytes.

## Reference Documents (Tier 0-2)

Tier 0: core-principles, development-standards, documentation-standards and glossary.
Tier 1: AGENTS.md and security-policy. Tier 2: approved REQ006, WI023 handoff,
WI022 selection, current bounded scanner, October 26 exclusion classification and
November 6 exact public checkpoint review. Role PG; task type security/review;
context rule source `.claude/config/context-injection-rules.json`.

Applied approved-scope-once execution, no plain secrets/PII in Git, preserved
history, exact WI traceability and separate PG/EO/MA responsibilities. The handoff
existed before applying the create-wi-evidence-pack skill.

## Evidence Pointers

Ignored local receipts under `build/WI-20261107-ATS-023/`:

| Receipt | Result |
| --- | --- |
| `triage-01.json` | 473 visible paths: 409 pattern-clear, 49 with findings, 15 binary/unknown |
| `baseline-review.json` | HEAD equals the local remote-tracking ref; no preexisting outgoing commits; prior 63 exclusions unchanged |
| `source-supplemental-context-01.json` | 13 masked credential/URI/encoded-data contexts across 25 source/test files and one Nginx template |
| `client-guide-review.json` | Four public HTTPS product links; no query/userinfo, scripts, inline handlers or data URI payloads |
| `source-public-allowlist-01.json` | 27 raw SHA-256/expected Git-object allowances and per-file review reasons |

The 27-file allowance SHA-256 is
`5cf8570598555e6d9feb134c6c96e5a704094d043926ac067fb0185df27285c3`.
The starting and outgoing-range base is
`6a8732a5c056ac175afb8d5c8110cedbd081e89c`.
MA supplied the fresh authorized remote-fetch result; PG independently checked
local references and absence of outgoing commits, without a network operation.

The prior [exclusion classification](WI-20261026-ATS-004-classification.json)
remains unchanged. Historical untracked reports, screenshots and binary artifacts
are not made publishable by a no-match result. Original local operator documents
can intentionally differ from their previously committed public copies; WI022
must provide exact export bytes rather than stage these originals indiscriminately.

## Findings and Decisions

The selected 27-file baseline has no primary scanner finding. Supplemental review
identified password-route and encoder symbols, credential-related explanatory
comments and a placeholder redirect. The Nginx example contains an unfilled host
placeholder and generic certificate file references, with no certificate/key body,
real client address or private target endpoint. Tests use synthetic mode/role
fixtures, isolated temporary storage and mocked collaborators. No actual account
or credential body was found in the reviewed source.

The client upload guide is static HTML. It contains public product routes and
instructions to use separately provided login information; it includes no account
name or password. Its service-verification claims remain attributable to the
original operational WIs and are not newly validated by this local privacy review.

Of the 49 initial flagged paths, 48 belong to the unchanged prior exclusions.
The remaining `docs/index.md` finding is a Java version interpreted as an IP.
That original index is not automatically approved: the final public export still
requires its own hash-bound content review.

## Commands & Outputs

- `python scripts/reconstruction/scan_commit_candidates_wi004.py --output build/WI-20261107-ATS-023/triage-01.json`
- Read-only Git status/ref/history/workflow/hook metadata inspection.
- SHA-256 comparison, `git hash-object` without write, supplemental masked context
  inspection and standard-library HTML structure/link inspection.
- Existing scanner limitations remain explicit: it is a bounded pattern detector,
  retains its historical WI004 label and cannot provide universal secret absence.

No `.github/workflows` directory or tracked workflow was found; `core.hooksPath`
is unset and the local hook directory contains no nonsample hook. This local
observation is separate from MA's remote-side deployment-effect check.

## Tests and Validation Boundaries

No application test suite, build, native tool, server, endpoint probe, database,
mail/payment/backup operation or installation was run by PG. Source/runtime
validation belongs to the original WIs and independent WI024 quality review.
No ignored credential body was read. PG performs no index, commit or push mutation.

The final allowance must identify the actual `source_export_path` bytes and
expected repository-filtered blob ID. Raw CRLF bytes and LF Git objects are
separate identities. Actual staged paths/objects and each outgoing commit must
be compared before MA pushes; newly changed bytes invalidate that file's allowance.

## Risks / Rollback / Follow-ups

Only this pack, its paired summary and ignored review receipts were written by PG.
Preserve every exclusion and private original. Do not rewrite historical evidence
or force-add local artifacts. A pattern-clear scan is not complete anonymity or
application-security proof. WI022 receives findings for any necessary fix; WI024
and MA depend on the final PG object gate before authorized publication.
