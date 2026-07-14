---
version: 1.0
last_updated: 2026-07-14
project: ATS
owner: qa-integ
category: evidence-pack
status: stable
related_wi: WI-20260714-ATS-001
dependencies:
  - path: WI-20260714-ATS-001-handoff.md
    reason: Approved WI output contract
  - path: ../../docs/audit/p1-remediation-trace-matrix-20260714.md
    reason: Complete current-state trace matrix
  - path: ../user/REQ-20260714-ATS-001.md
    reason: Approved remediation request
---

# Evidence Pack: WI-20260714-ATS-001

## Summary

- Re-verified all 13 P1 findings and three bounded acceptance-environment items against repository snapshot `0ad4bc5`, corrected stale interpretations, and produced the implementation/test/document/reviewer trace baseline without closing findings.

## Scope / DoD Check

- [x] `ATS020-P1-01` through `ATS020-P1-13` each have a complete current-state trace row.
- [x] Each P1 row identifies current behavior, exact pointers, target invariant, implementing WI, required test/command, documentation target, and reviewer.
- [x] `ATS020-X-01`, `X-02`, and `X-04` are bounded separately.
- [x] Conflicts, overlap boundaries, hidden dependencies, and changed interpretations are explicit.
- [x] No finding is closed without reproducible evidence.
- [x] Only the three WI-owned files were created.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, transparency, language, and traceability |
| 0 | `docs/standards/development-standards.md` | Layer, transaction, test, and evidence rules |
| 0 | `docs/standards/documentation-standards.md` | Metadata, structure, and relative-link rules |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio terms |
| 1 | `docs/policies/security-policy.md` | Secrets, sessions, protected media, and environment baseline |
| 1 | `docs/policies/quality-gates.md` | Review and reproducibility gates |
| 1 | `docs/standards/evidence-pack-standard.md` | Evidence Pack contract |
| 2 | `docs/standards/frontend-standards.md` | Active React/auth/API conventions |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Frontend review reference |
| 2 | `docs/design/api-spec.md` | API contract |
| 2 | Payment designs/runbook/use case and `docs/payment/*` | Payment and operations contracts |
| 2 | `docs/SR/SR-42.md`, `docs/client/*` | Acceptance/tunnel and client contracts |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved request |
| Context | `docs/audit/full-system-audit-20260713.md` | Canonical findings |
| Context | `docs/audit/p0-release-blocker-closure-20260713.md` | Post-P0 current baseline |

## Evidence Pointers

### Changed Files

- `docs/audit/p1-remediation-trace-matrix-20260714.md`: English current-state P1/X trace matrix and WI-chain rules.
- `deliverables/user/WI-20260714-ATS-001-summary.md`: Korean scope, corrections, risks, approvals, and chain report.
- `deliverables/agent/WI-20260714-ATS-001-evidence-pack.md`: Reproduction, evidence, uncertainty, and rollback record.

### High-Signal Current Evidence

- Untrusted content/session/file lifecycle: `PlaylistService.java:40-53,179-190`; `CompanyCertificationService.java:233-307`; `UserService.java:183-193`; `EmailService.java:142-159`; `LocalStorageService.java:51-87`.
- Payment/DB integrity: `PaymentOperationAuditAction.java:17-19`; `schema.sql:800-815`; `BillingAgreementApplicationService.java:161-226`; `RecurringRenewalService.java:84-206`; `AdminPaymentRefundService.java:89-111,248-262`.
- Export/frontend: `AdminWhitelistChannelService.java:127-198`; `SocialLoginPage.tsx:42-59`; `frontend/src/api/auth.ts:99-103`.
- Conditional acceptance: `20260615_align_payment_whitelist_schema.sql:18-23`; `20260618_company_certification_documents.sql:1-22`; `AuthRateLimitFilter.java:46`; `TestUserBootstrapRunner.java:33-52`; `vite.config.ts:12-24`.
- Documentation readiness: `docs/payment/known-limits-and-next-steps.md:25-39`; `docs/payment/acceptance-test-checklist.md:80-140`; `docs/client/3-admin-checklist.md:82-95`.

## Conflict Adjudication

| Topic | Decision |
|---|---|
| P1-07 renewal serialization | Narrowed: renewal now obtains an agreement row lock, but stable command identity and unique finalization remain open; confirm/upgrade remain unlocked. |
| P1-07 vs P1-09 | P1-07 owns command identity/finalization; P1-09 owns per-agreement transaction isolation. |
| P1-04 vs P1-01/P1-02 | P1-04 owns DB/file convergence; P1-01/P1-02 own content authenticity and serving. |
| P1-13 withdrawal procedure | Prior absence claim is stale after P0 docs; remaining completion-language and safety-gate defects stand. |
| P1-01 severity | P1 confirmed; P0 same-origin execution impact remains conditional. |
| X-01 scope | Fresh DDL repair does not close retained-DB migration/backfill proof. Legacy certification disposition has no explicit implementing WI owner. |

## Reproduction / Commands

Commands used for read-only verification included:

```powershell
git status --short --branch
git log -5 --oneline --decorate
rg -n "ATS020-P1-|ATS020-X-0[124]" docs/audit/full-system-audit-20260713.md
rg -n "@Transactional|find.*ForUpdate|orderId|failureCode|PaymentOperationAudit" src/main/java/com/atstudio/atstudio
rg -n "payment_operation_audit_logs|payment_orders|payment_refunds|ENUM|UNIQUE" src/main/resources/schema.sql src/main/resources/db/manual
rg -n "MultipartFile|contentType|originalFilename|refreshToken|logout|password|getRemoteAddr|allowedHosts|bootstrap" src/main/java src/main/resources frontend
rg -n "CSV|export|escape|quote|userEmail" src/main/java/com/atstudio/atstudio/service src/main/java/com/atstudio/atstudio/controller
git diff --check -- docs/audit/p1-remediation-trace-matrix-20260714.md deliverables/user/WI-20260714-ATS-001-summary.md deliverables/agent/WI-20260714-ATS-001-evidence-pack.md
git -c core.autocrlf=false diff --no-index --check -- /dev/null <owned-file>
```

## Results and Test Status

- Trace result: 13/13 P1 rows and 3/3 bounded X rows mapped.
- Finding closure: 0; this WI creates no closure claims.
- Tests: not executed. Existing tests were inspected to identify coverage gaps; required new test classes/cases are named in the trace matrix.
- Live/runtime proof: not executed; Cloudflare client identity, target DB history, and deployed bootstrap state remain uncertain.
- Documentation index: not changed because the output contract permits only three owned files. Index registration belongs in the approved documentation WI (`WI-026`); therefore a full document-validator claim is not made here.
- Whitespace verification: PASS for all three owned files. The standard owned-path `git diff --check` passed, and each untracked output was also checked as an added file with the `--no-index --check` form above. The first untracked-aware attempt emitted only Git's LF-to-CRLF conversion warning; rerunning with `core.autocrlf=false` separated that warning from whitespace validation and passed.

## Risks / Approval Points / Blockers

- No blocker prevents completion of this baseline WI.
- `ATS020-X-01` cannot close until MA assigns an explicit owner for legacy certification backfill/disposition.
- Separate approval is required for disposable DB creation/deletion, DDL/data changes, backfill, test-data reset, a new dependency such as Testcontainers, live Toss/SMTP/production infrastructure, and external public-URL delivery.
- P1-03 session model semantics and payment transaction/constraint choices remain design decisions for `WI-002`/`WI-003`; this WI does not select architecture for them.

## Rollback

- Remove only:
  - `docs/audit/p1-remediation-trace-matrix-20260714.md`
  - `deliverables/user/WI-20260714-ATS-001-summary.md`
  - `deliverables/agent/WI-20260714-ATS-001-evidence-pack.md`
- Do not revert application code, schema, tests, existing documentation, logs, or unrelated concurrent files.

## WI-Chain Triggers

- WI-001 completion releases its dependency edge for `WI-004` through `WI-034`.
- Phase 1 implementation remains gated on completion of parallel Phase 0 designs `WI-002` and `WI-003`.
- Domain review triggers: `WI-023` payment, `WI-024` security, then `WI-025` cross-layer integration.
- Documentation triggers after review: `WI-026` current-state docs and `WI-027` client procedure.
- Quality chain: `WI-028` through `WI-033`; final closure/evidence decision: `WI-034`.

## Related Documents

- [P1 Trace Matrix](../../docs/audit/p1-remediation-trace-matrix-20260714.md)
- [Approved REQ](../user/REQ-20260714-ATS-001.md)
- [WI Handoff](WI-20260714-ATS-001-handoff.md)
