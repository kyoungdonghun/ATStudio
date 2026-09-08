---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260908-ATS-006-handoff.md
    reason: Approved scope and ownership
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved presentation-only request
---

# Evidence Pack: WI-20260908-ATS-006

## Summary

Korean payment email copy completed; 38 focused unit tests pass. Source changes are frozen for MA verification and WI-20260908-ATS-008.

## Scope / DoD Check

- [x] Customer subject `[AT.M] 구독 결제 안내`, greeting, heading and blank defaults localized.
- [x] Retry and suspended guidance distinguish automatic retry from service-access grace expiry; exact `graceEndsAt` and order identity retained. No invented next retry date or immediate re-charge promise.
- [x] Operator subject/heading use `결제 점검 이슈`; summary and detail labels localized while technical field names and values remain identifiable.
- [x] Korean UTF-8 MIME serialization/readback, adversarial dynamic escaping, null/empty/blank fallbacks, failure branches and operator detail assertions pass.
- [x] Production diff contains only string changes. Escaping, provider-response handling, secret-safe logs, retry gates, financial state, notification guards and auth/reset emails unchanged.

## Reference Documents (Tier 0-2)

| Tier | Documents | Use |
| :-- | :-- | :-- |
| 0 | `docs/standards/core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md` | User-injected standards; documentation metadata rechecked locally |
| 1 | `docs/policies/security-policy.md`, `.claude/agents/se.md` | Secret handling and SE role |
| 2 | Approved REQ and WI handoff; `docs/payment/user-flows.md`, `admin-operations-guide.md`, `known-limits-and-next-steps.md` | Grace, suspended renewal, operator terminology and delivery boundaries |

Applied `test` and `create-wi-evidence-pack` skills. Handoff injection source: `.claude/config/context-injection-rules.json`; assignee `se`, task `implementation`. ATS tag verified in `.claude/config/workspace.json`. No nested delegation.

## Evidence Pointers

| Changed file | Location and purpose |
| :-- | :-- |
| `src/main/java/com/atstudio/atstudio/service/EmailService.java` | Lines 120, 136, 221, 245: subjects and HTML text only |
| `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java` | Line 277: retry/suspended copy and order label only |
| `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java` | Line 358: operator email summary/detail labels only |
| `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java` | Lines 239-332: MIME roundtrip, UTF-8, fallbacks and dynamic escaping |
| `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java` | Line 182: retry, final charge failure and expired-grace no-attempt guidance; private provider fixture excluded |
| `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java` | Line 73: exact operator summary and all detail labels/values |

Worktree: `C:/Users/jm991/Desktop/project/ATStudio`; branch: `codex/v1-release-rehearsal-fixes`. These six code/test files were clean at start. Earlier dirty work, including `RecurringRenewalCommandIntegrationTest.java`, was not modified by this WI.

## Commands & Outputs

Runtime base: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908`.
Authorized helper: `<runtime-base>/copy-polish-build.gradle`, created with `apply_patch` from `closeout-build.gradle`. Comparison after newline normalization confirmed the exact worktree guard is retained; the only content substitution is `build-closeout` to `build-copy-polish`. Repository `build.gradle` is unchanged.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
.\gradlew.bat --no-daemon --console=plain --offline --init-script C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/copy-polish-build.gradle test --tests com.atstudio.atstudio.service.EmailServiceTest --tests com.atstudio.atstudio.service.RecurringRenewalServiceTest --tests com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest
```

Java version: `17.0.12`. Red run: 38 tests, 11 expected copy assertion failures, exit 1. Green run: exit 0, `BUILD SUCCESSFUL in 26s`. No `build`, `jar`, `bootJar`, `clean` or runtime restart. Compilation reported unchecked/unsafe-operation and JVM class-sharing warnings, not errors. Scoped `git diff --check` passed; Git emitted an existing working-copy CRLF normalization notice for `EmailService.java`.

## Tests

| Suite | Tests | Failures / errors / skipped |
| :-- | --: | :-- |
| EmailServiceTest | 15 | 0 / 0 / 0 |
| RecurringRenewalServiceTest | 15 | 0 / 0 / 0 |
| PaymentReconciliationIncidentServiceTest | 8 | 0 / 0 / 0 |
| Total | 38 | 0 / 0 / 0 |

Counts read from `<runtime-base>/build-copy-polish/test-results/test/TEST-*.xml`; HTML report: `<runtime-base>/build-copy-polish/reports/tests/test/index.html`. Prior `build-closeout` results and running JAR are outside this output directory. Mail sender, Provider and repositories are mocks; no SMTP, real Provider or DB calls were made.

## Risks / Rollback

- Local source/unit verification only. Actual delivery, inbox placement, running-server adoption, browser and aggregate release validation are not claimed here.
- If rollback is approved, reverse only the six WI-owned diff hunks and these two deliverables; preserve all other dirty work. Do not use whole-file checkout/reset.

## Follow-ups

`Blocks: WI-20260908-ATS-008`. Evidence is ready for MA's existing WI-008 documentation/aggregate-verification chain once WI-007 is also ready. No additional source edits after this freeze.

## Related Documents

- [Approved REQ](../user/REQ-20260908-ATS-002.md)
- [WI handoff](WI-20260908-ATS-006-handoff.md)
- [User summary](../user/WI-20260908-ATS-006-summary.md)
