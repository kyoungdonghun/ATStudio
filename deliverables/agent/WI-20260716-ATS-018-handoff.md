---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: handoff
status: ready
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved development-branch remediation scope
  - path: WI-20260716-ATS-017-evidence-pack.md
    reason: Prior closure evidence and current baseline
---

# WI Handoff: WI-20260716-ATS-018

[WI HEADER]

WI ID: WI-20260716-ATS-018
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-017
Blocks: final development-branch release-readiness judgment

[WI SUMMARY]

Why: Independent post-WI-017 review found two remaining payment-evidence exposure paths. The admin receipt table renders persisted provider `receiptUrl` values directly as anchors, and provider reconciliation logs serialize the full result object whose issue details can contain an exact `providerTransactionId`. Provider payloads and retained database values are untrusted evidence; unsafe URLs must not become executable links and exact provider identifiers must not enter logs.

Scope (in):
- Define one provider-neutral safe receipt URL contract: absolute HTTPS, no credentials, and only default/443 port.
- Enforce the contract at provider-evidence ingestion or response mapping so newly retained unsafe URLs are not exposed as actionable links.
- Add a frontend defense-in-depth renderer that turns unsafe retained receipt URLs into non-clickable text/reference state.
- Add focused backend and frontend tests for HTTPS, unsafe schemes, credentials, non-standard ports, malformed values, and retained legacy data.
- Replace full provider-reconciliation result logging with aggregate/correlation-only fields so raw provider identifiers and issue free text cannot be serialized to logs; preserve detailed structured Incident persistence with its existing masking boundary.
- Replace the Toss cancel unknown-outcome exception stack log with bounded exception-class metadata because transport exception messages/stacks may include the request URI containing the provider payment key.
- Add focused static or log-capture proof that reconciliation logging cannot emit exact provider transaction identifiers.
- Update payment API/security/operations/client documentation and the deterministic client PDF only where behavior or acceptance instructions change.
- Re-run focused and complete backend, frontend, docs, PDF, diff, generated-file, and client-worktree integrity gates.

Scope (out):
- No provider integration change, receipt issuance feature, cash-receipt feature, DB schema migration, payment policy change, or client-branch promotion.
- Do not broaden URL host allowlists to a Toss-only contract; future providers must remain possible.

DoD:
- No unsafe receipt URL can be stored as a new actionable URL or rendered as an anchor.
- Provider reconciliation application logs contain only bounded aggregate/correlation metadata and never exact provider identifiers or full issue lists.
- Existing valid HTTPS receipt links remain usable.
- Existing unsafe retained rows remain inspectable without becoming executable.
- Tests and current-state documentation prove the contract.
- All quality gates pass and `frontend/tsconfig.tsbuildinfo` retains its verified baseline bytes.

Constraints/Forbidden:
- Work only in `C:/Users/jm991/Desktop/project/ATStudio` on `codex/p1-acceptance-hardening`.
- The repository is shared and dirty; do not revert unrelated or prior WI changes.
- Do not touch `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable` except read-only status/HEAD verification.
- No stage, commit, push, merge, reset, restore, clean, DB/data/provider/secret access, runtime restart, or destructive file operation.
- Preserve full public playback, gated download, recurring billing-key card payment, and single-server deployment invariants.

[ACCEPTANCE CRITERIA]

Functional:
- [ ] Valid absolute HTTPS provider receipt URLs are normalized and remain openable.
- [ ] `javascript:`, `data:`, `file:`, `ftp:`, protocol-relative, credential-bearing, malformed, and non-standard-port URLs are rejected or suppressed.
- [ ] Unsafe retained values render as non-clickable text/reference state in the admin receipt table.
- [ ] Raw provider identifiers remain server-only and current `REF-` support-reference behavior is preserved.
- [ ] Provider reconciliation warnings/info do not serialize `ProviderReconciliationResult.issues` or raw `providerTransactionId` values.
- [ ] Unknown Toss cancel transport failures do not log exception messages, stack traces, request URIs, or provider payment keys.

Performance:
- [ ] URL validation is local and bounded; no network lookup is introduced.

Quality:
- [ ] Focused backend receipt-evidence/DTO/controller tests pass.
- [ ] Focused frontend admin payment tests pass.
- [ ] Full backend tests/build/JaCoCo pass.
- [ ] Frontend audits/typecheck/ESLint/Vitest/coverage/build/Prettier pass.
- [ ] Documentation validation, PDF verification, `git diff --check`, tsbuildinfo hash, and client-worktree integrity pass.

[INPUT POINTERS]

Tier 0 (Constitution and development standards):
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Security and quality):
- `docs/policies/security-policy.md`
- `docs/policies/quality-gates.md`

Tier 2 (React and payment contracts):
- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/payment-refund-receipt-settlement-policy.md`
- `docs/design/payment-operations-runbook.md`
- `docs/payment/system-overview.md`
- `docs/payment/admin-operations-guide.md`
- `docs/client/testing-guide.md`

REQ / prior evidence:
- `deliverables/user/REQ-20260716-ATS-002.md`
- `deliverables/agent/WI-20260716-ATS-017-evidence-pack.md`
- `deliverables/user/WI-20260716-ATS-017-summary.md`

Files:
- `src/main/java/com/atstudio/atstudio/service/PaymentReceiptEvidenceService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReceiptResponse.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentReceiptEvidenceServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java`
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx`
- `frontend/src/pages/admin/PaymentReadOnlyPage.test.tsx`
- `frontend/src/utils/safeYoutubeUrl.ts` (pattern reference only; do not make the receipt contract YouTube-specific)
- `scripts/docs/generate_client_testing_pdf.py`
- `scripts/docs/verify_client_testing_pdf.py`

Repro:
- Search `href={` sinks in `frontend/src`; the receipt link is the remaining direct untrusted external anchor after whitelist URL hardening.

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260716-ATS-018-summary.md`:
- Korean summary, exact behavior change, verification, residual environment gates, and release judgment.

Agent-facing -> `deliverables/agent/WI-20260716-ATS-018-evidence-pack.md`:
- Closure evidence, changed files/lines, commands/results, tests, docs/PDF provenance, rollback, and follow-up WI if any.

Handoff Packet -> `deliverables/agent/WI-20260716-ATS-018-handoff.md`:
- This packet.

[TRACEABILITY REQUIREMENTS]

- Evidence pointers to code, tests, docs, and commands are required.
- Record any regression found during full verification and the exact correction.
- Distinguish locally closed findings from environment-conditional gates.
- Document coherent rollback by backend/frontend/docs groups without reverting prior WIs.
