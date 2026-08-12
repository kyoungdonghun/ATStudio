---
version: 1.1
last_updated: 2026-08-12
project: ATS
owner: PG
category: audit
status: accepted
dependencies:
  - path: deliverables/agent/WI-20260809-ATS-041-handoff.md
    reason: Approved WI scope, acceptance criteria, and held boundaries
  - path: docs/policies/security-policy.md
    reason: Provider identifier and audit-text minimization requirements
  - path: docs/policies/access-control-policy.md
    reason: Least-privilege and default-deny requirements
---

# WI-20260809-ATS-041 PG Final Review

> Purpose: Record the mandatory PG review of the final bounded Settlement diff for `CR-031-113` and `CR-031-114` after P1 remediation.

## 1. Review Metadata

| Field | Value |
| --- | --- |
| WI | `WI-20260809-ATS-041` |
| Reviewer | PG |
| Review type | Final bounded diff re-review after P1 remediation |
| Canonical roots | `CR-031-113`, `CR-031-114` |
| Review date | 2026-08-12 |
| Verdict | **APPROVE** |

No open P0, P1, or P2 incremental security or privacy finding remains in the
final bounded WI-041 Settlement product and test diff.

## 2. Finding History

### [P1][RESOLVED] IGNORE permitted an unattributed or non-ADMIN direct service mutation

**Original finding:** The first reviewed version validated the note but allowed
null, ID-less, and non-ADMIN direct service callers to reach Settlement mutation.
It could persist `ignored_by = null` and create an unattributed successful audit.
The original verdict was `REQUEST_CHANGES`.

**Resolution:** `AdminPaymentSettlementService.ignoreSettlement` now performs
the required fail-closed sequence inside one transaction:

1. Normalize and validate the required note.
2. Require a principal ID and token role `ADMIN`.
3. Lock the authoritative User row with `PESSIMISTIC_WRITE`.
4. Require that row to exist, remain `ADMIN`, and not be deleted.
5. Lock the Settlement row with `PESSIMISTIC_WRITE`.
6. Reject an existing `IGNORED` decision, otherwise mutate once and append one
   audit with the normalized note and authenticated actor.

Null, ID-less, token non-ADMIN, missing authoritative user, DB role drift, and
deleted authoritative ADMIN paths now fail before Settlement access or audit.
The successful HTTP test supplies a real `CustomUserDetails` ADMIN principal and
proves the same principal reaches the service. Anonymous and USER requests are
still rejected before service invocation.

**Resolution evidence:**

- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:239-273`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:556-568`
- `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:18-20`
- `src/main/java/com/atstudio/atstudio/repository/PaymentSettlementRepository.java:27-29`
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java:232-294`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:460-685`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementIgnoreIntegrationTest.java:57-197`

## 3. Final Security Assessment

- **Authorization and ordering:** Note validation precedes principal validation;
  authoritative User validation and lock precede Settlement lock, mutation, and
  audit. Unit `InOrder` evidence confirms User lock -> Settlement lock -> audit.
- **Fail-closed direct callers:** Every required invalid principal/current-user
  case leaves status, actor, time, and note unchanged and makes zero Settlement
  repository and audit calls. Missing/role-drift/deleted rows perform only the
  required authoritative User lookup before rejection.
- **HTTP boundary:** `/api/admin/**` and method security remain ADMIN-only. The
  valid request forwards the exact ADMIN principal and one trimmed note once;
  anonymous and USER requests invoke no Settlement service.
- **DTO and service validation:** Missing, null, empty, whitespace-only, and
  trimmed-over-500 notes are rejected at HTTP with zero service calls. The
  service independently rejects null request, null/blank note, and
  trimmed-over-500 note before actor, Settlement, or audit access.
- **First-decision integrity:** The actor and Settlement row locks serialize the
  decision. Repeated same-note and conflicting-note requests return the existing
  invalid-transition error with no overwrite or additional audit. The entity
  also independently rejects a second `ignore` mutation.
- **Actor and audit integrity:** Focused H2 evidence retains the first actor,
  decision time, normalized note, status, audit ID, audit creation time, and one
  audit row. Concurrent first decisions produce one winner, one rejected loser,
  and one audit whose actor and note match the durable winner.
- **Audit minimization:** The changed path persists only the bounded normalized
  operator note and existing stable Settlement audit fields. It does not compose
  exception text, CSV cells, Provider responses, secrets, or PII into error or
  audit text. Raw Provider identifiers remain protected fields and existing
  ADMIN response mapping uses support references/sanitization.
- **Partial import UI:** Mixed HTTP 200 results remain visibly partial, retain
  the exact File, DOM input, and note, render every returned fixed-form row
  error, and perform one import plus one reload. Full success clears the File
  only after reload; request/reload failure retains correction context. No raw
  row payload is rendered and no held parser or count policy is changed.
- **External effects:** The reviewed paths contain no Provider, payment, refund,
  subscription, billing-agreement, receipt, email, or deployment invocation.
  Verification used mocks, H2, and synthetic values only.

## 4. Commands and Results

| Command | Result |
| --- | --- |
| `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.entity.PaymentSettlementTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementIgnoreIntegrationTest"` | Exit `0`; 38 passed, 0 failed, 0 errors, 0 skipped: controller 13, service 22, entity 1, H2 integration 2 |
| `npm test -- src/pages/admin/PaymentOperationsPage.test.tsx -t "fully successful import\|mixed-result error\|import request fails\|required reload fails\|settlement ignore"` | Exit `0`; 1 file passed; 5 passed, 81 skipped |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | Exit `0`; Tier 0, internal links, 557 traceability IDs, and document index passed |
| `git diff --check` | Exit `0`; no whitespace errors; unrelated shared-worktree CRLF-to-LF warnings only |

## 5. Limitations

- No full `Q-ALL`, browser, staging, production import, real data, or real
  Provider verification was run; this PG re-review intentionally used focused
  safe tests only.
- H2 proves the exercised JPA pessimistic-lock and transaction behavior, but it
  is not a MySQL concurrency or lock-wait/deadlock rehearsal. Production MySQL
  scheduling and timeout behavior remain residual integration risk.
- The audit-text conclusion is bounded to this changed data flow and synthetic
  inputs. No generic free-text DLP capability was added or claimed.
- Shared-worktree refund/correction changes and non-Settlement API hunks were
  excluded from this review.

## 6. Held Boundaries

- `CR-031-115`, `CR-031-116`, and `CR-031-118` remain held: no CSV filename,
  MIME, byte, encoding, grammar, header, row-width, provider/financial-field,
  date/range, or row-ceiling policy was changed or approved.
- `CR-031-117` and `CR-031-119` remain held: no duplicate constraint,
  concurrent import atomicity, file-level audit, unusable-row accounting, or
  aggregate count-conservation contract was changed or claimed closed.
- No payment, refund, subscription, billing-agreement, receipt, Incident,
  Provider, schema, dependency, external integration, or shared modal behavior
  is approved by this review.
- No secret or ignored-config access, `output/` or ZIP access, destructive
  action, external effect, deployment, or Git mutation occurred.

## Related Documents

- [WI-041 Handoff](WI-20260809-ATS-041-handoff.md): Approved review contract
- [Security Policy](../../docs/policies/security-policy.md): Audit and Provider-data safety
- [Access Control Policy](../../docs/policies/access-control-policy.md): Default-deny boundary
