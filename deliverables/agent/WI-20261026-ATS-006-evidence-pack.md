---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: QA
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261026-ATS-006-handoff.md
    reason: Approved scoped source validation
  - path: WI-20261025-ATS-008-evidence-pack.md
    reason: Author source and focused test evidence
  - path: WI-20261025-ATS-006-evidence-pack.md
    reason: Prior independent review and resolved interpolation finding
---
# Evidence Pack: WI-20261026-ATS-006

## Summary
Seven optional-mail source/configuration/test files match their reviewed identities and retained focused evidence; source review permits a coherent local commit after PG and staged-content checks. No product edit or test rerun was needed.

## Scope / DoD Check
- [x] Reviewed the five tracked diffs and two new mail files; no unrelated source changes included.
- [x] Explained both integration-test constructor adaptations and preserved authentication/reset semantics.
- [x] Independently matched seven current raw source hashes, four retained test XML hashes and three JAR hashes.
- [x] Reused 16 changed NCP cases and 26 earlier unchanged cases precisely; no fresh whole-42 run claimed.
- [x] Recorded LF-normalized expected Git blob identities without altering working source or Git index.
- [x] No live mail/provider/server/database/application start, network download, install, source correction or Git write.

## Reference Documents (Tier 0–2)
MA injected core-principles (evidence-led scoped work), development-standards (minimal reproducible changes), documentation-standards (metadata/two deliverable sets) and glossary (canonical terms). Current AGENTS approve-once and approved REQ govern this work. Security-policy forbids plaintext Secrets/PII; quality-gates/versioning-policy define scoped validation/retention. Role QA, rules source `.claude/config/context-injection-rules.json`. Read WI006 handoff before applying test, build-check and create-wi-evidence-pack skills. Source/JAR evidence is reused rather than executing their example full-suite commands.

## Reviewed Change
| Files | Finding |
|---|---|
| `EmailService.java`, `application.yml` | Add selector dependency and five environment-backed settings. SMTP remains default. NCP branch occurs only at existing send boundary, returns after accepted submission and has no automatic SMTP fallback. Existing message/token/public success handling is preserved. |
| `NcpMailTransport.java`, `NcpMailTransportTest.java` | Fixed public provider origin, selected-config checks, HMAC, one application-level submission, bounded body/deadline, sanitized errors and literal HTML substitution correction match the prior reviewed candidate. Test credentials/addresses are explicit synthetic fixtures. Provider acceptance is not delivery. |
| `EmailServiceTest.java` | Mock transport injection plus acceptance/no-SMTP and unknown-outcome/no-fallback/no-sensitive-log cases. |
| `AuthTokenSecurityIntegrationTest.java:119` | Only adds a mocked NCP constructor argument. Real token rotation/refresh/bearer/reset assertions remain unchanged. This test was included in the prior six-case passing suite. |
| `PasswordResetConsumptionIntegrationTest.java:180` | Only adds the mocked NCP constructor argument to the H2 test bean. Concurrent single consumption, rollback and invalid-token assertions remain unchanged. Prior three-case passing suite includes this source. |

No blocking source finding was found within this seven-file scope. No new dependencies/build script changes exist in this group. Broader application review and runtime acceptance are not inferred.

## Evidence Pointers
- [Fresh sanitized source/evidence receipt](WI-20261026-ATS-006-source-check.json): seven exact raw pins, prior four XML metadata/hashes and three artifacts match.
- [Exact source allowlist and normalization identities](WI-20261026-ATS-006-source-allowlist.json): source-only candidate list; PG and actual staged gate still required.
- Prior authority: `build/WI-20261025-ATS-008/checks.json`, `checks-v1.json`, `test-results-v1/`, current isolated NCP XML and three preserved JARs. No prior result was overwritten.
- Reproduction helpers: `build/WI-20261026-ATS-006/verify_retained_evidence.py` and `make_commit_allowlist.py`, fixed-path local reads and write-once new receipts only. They never run an app or copied operational tool.
- [User result](../user/WI-20261026-ATS-006-summary.md).

## Commands & Outputs
Read scoped diffs, current seven files, prior WI008/006 receipts, `build.gradle`, test/build-check/evidence-pack skills. Inspected old `check_review.py` but did not execute it because it overwrites its historical review receipt.

`python.exe -I -S -B build/WI-20261026-ATS-006/verify_retained_evidence.py` returned `CURRENT_SOURCE_AND_RETAINED_EVIDENCE_MATCH`, seven sources, four reused suites, fresh_test_execution=false.

`python.exe -I -S -B build/WI-20261026-ATS-006/make_commit_allowlist.py` records raw SHA256, LF SHA256 and LF Git SHA1. Existing `.gitattributes` uses `text=auto eol=lf`; source paths have no clean filter. `git diff --check -- <five tracked source/config/test paths>` passed, with expected CRLF-to-LF warnings for EmailService/application.yml only. New files were inspected for whitespace separately.

`build/WI-20261026-ATS-006/document-check.json` records PASS for six owned-report/new-source files, five local links, metadata/JSON parsing and whitespace/newline checks. It is a local document check, not a test execution.

## Tests and Boundaries
Current source pins match the exact prior candidate. Prior author executed NCP16 after the interpolation repair; unchanged Email17/Auth6/Reset3 reuse earlier evidence. All four XML files still have matching hashes and zero failures/errors/skips. Initial author constructor compile failure and initial candidate remain preserved. No new Gradle/test process ran because there was no source drift or unresolved behavior requiring repetition. No real mailbox, NCP credentials, target Spring boot or deployed-source equivalence is claimed.

## Risks / Rollback / Follow-up
Before staging, recheck raw source pins. After staging, compare every index blob to its expected LF-only bytes/hash; unexpected changes require re-review, not acceptance as line-ending noise. Preserve raw historical source/evidence identities. A local commit is not deployment or provider activation. Revert only by a separately reviewed scoped change; no reset/clean or whole-database rollback.

WI006 unblocks WI007 together with WI004/005. Root/EO owns actual stage/commit after PG classification; this WI made none.
