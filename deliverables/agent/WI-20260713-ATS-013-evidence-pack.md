# Evidence Pack: WI-20260713-ATS-013

## Summary (one-liner)

- Independently reran the complete Gradle backend test suite and recorded reproducible XML aggregates for the P0 remediation.

## Scope / DoD Check

- DoD items:
  - [x] `.\gradlew.bat test` exited with code `0`.
  - [x] The full suite was forced to execute with `--rerun-tasks` instead of relying on Gradle's up-to-date cache.
  - [x] Aggregate suite, test, failure, error, and skipped counts were recorded from every XML result file.
  - [x] Existing P0 focused test classes were confirmed in the full-suite results.
  - [x] No live Toss, SMTP, or external database call was made.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and transparent evidence requirements |
| 0 | `docs/standards/development-standards.md` | Backend test and QA evidence standards |
| 1 | `docs/policies/quality-gates.md` | Regression and high-criticality quality gates |
| REQ | `deliverables/user/REQ-20260713-ATS-001.md` | Approved P0 scope and success criteria |
| Context | `deliverables/agent/WI-20260713-ATS-011-evidence-pack.md` | P0 cross-layer traceability matrix and focused suite baseline |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa`
- Task type: `testing`
- Context order: Tier 0, Tier 1, REQ/context, test result snapshot

## Evidence Pointers (required)

- Files created:
  - `deliverables/user/WI-20260713-ATS-013-summary.md` - user-facing QA result
  - `deliverables/agent/WI-20260713-ATS-013-evidence-pack.md` - reproducible test evidence
- Test result source:
  - `build/test-results/test/TEST-*.xml` - 102 parsed JUnit XML suite files
  - `build/reports/tests/test/index.html` - Gradle HTML test report
- Test environment:
  - `src/test/resources/application.yml` - Hibernate `create-drop`, SQL init disabled, local mail placeholder; embedded H2 is selected by the test classpath

## Commands & Outputs

- Commands executed:
  - `.\gradlew.bat test`
  - `.\gradlew.bat test --rerun-tasks`
  - PowerShell XML aggregation over `build/test-results/test/TEST-*.xml` with explicit UTF-8 decoding
- Outputs:
  - Initial command: exit `0`, `BUILD SUCCESSFUL`, but Gradle reported all five tasks `UP-TO-DATE`.
  - Definitive forced run: exit `0`, `BUILD SUCCESSFUL in 1m 22s`, five tasks executed.
  - XML parser coverage: 102 files discovered, 102 files parsed.
  - Aggregate: 102 suites, 786 tests, 0 failures, 0 errors, 0 skipped.
  - Aggregate XML-reported test time: 64.05 seconds.
  - Tool-observed forced-run wall time: 82.6 seconds.

## Tests

### Full Backend Regression

- `.\gradlew.bat test --rerun-tasks` - PASS
- Result: 786 / 786 tests passed.

### P0 Focused Tests Included in the Full Run

| Test class | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `SecurityFilterChainTest` | 17 | 0 | 0 | 0 |
| `TrackControllerTest` | 22 | 0 | 0 | 0 |
| `BillingAgreementRepositoryTest` | 3 | 0 | 0 | 0 |
| `DownloadServiceTest` | 10 | 0 | 0 | 0 |
| `EmailServiceTest` | 4 | 0 | 0 | 0 |
| `PaymentReconciliationIncidentServiceTest` | 6 | 0 | 0 | 0 |
| `RecurringRenewalServiceTest` | 10 | 0 | 0 | 0 |
| `TrackServiceTest` | 26 | 0 | 0 | 0 |
| `UserServiceTest` | 28 | 0 | 0 | 0 |
| `WithdrawalBillingCleanupCoordinatorTest` | 2 | 0 | 0 | 0 |
| `WithdrawalBillingCleanupServiceTest` | 5 | 0 | 0 | 0 |
| **P0 focused total** | **133** | **0** | **0** | **0** |

## Risks / Rollback

- Risks:
  - This WI verifies automated backend behavior only; it does not establish live Provider, SMTP, production MySQL, frontend, compile-only, build, or documentation-gate results.
  - The first XML aggregation attempt used PowerShell's default text decoding and failed to parse some Korean suite names. Its partial count was discarded. The recorded count comes from a second pass with explicit UTF-8 decoding and a 102-of-102 parse assertion.
  - `--rerun-tasks` regenerates ignored Gradle build outputs but does not modify product source or persistent application data.
- Rollback:
  - Remove only the two WI-013 deliverables if this evidence record must be withdrawn.
  - No product, test, schema, or data rollback is required.

## Follow-ups

- WI-014: compile and typecheck verification.
- WI-015: build verification.
- WI-016: documentation validation.
- WI-017: final P0 closure evidence and decision.
