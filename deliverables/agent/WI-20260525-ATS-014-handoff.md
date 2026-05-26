[WI HEADER]
WI ID: WI-20260525-ATS-014
REQ: REQ-20260525-ATS-006
Agent: docops
Depends On: -
Blocks: WI-20260525-ATS-015

[WI SUMMARY]
Why: 결제 운영 문서 일부에 과거 단계 표현과 entitlement correction ledger 누락 표현이 남아 있어, 현행 구현 상태와 읽는 사람이 이해하는 상태를 맞춘다.
Scope (in/out): In은 `payment-integration-design.md`, `payment-refund-receipt-settlement-policy.md`, 필요 시 SR-93/UI 문서 확인이다. Out은 새 기능 설계, backend/frontend 구현, settlement/tax invoice 정책 변경이다.
DoD: 문서가 현재 구현된 receipt/audit/refund/entitlement correction 상태와 모순되지 않고, remaining scope가 settlement/tax invoice/admin UI follow-up으로 분리되어 있다.
Constraints/Forbidden: 현금영수증 발급/취소, settlement, tax invoice가 구현된 것처럼 쓰지 않는다. Toss secret, billing key, authKey, customerKey, raw card data, raw provider payload를 예시로 노출하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Phase B historical note가 현재 구현 상태를 오해시키지 않는다.
- [ ] Current data gap 문장이 entitlement correction ledger를 포함한다.
- [ ] SR-93의 remaining/completed 상태와 충돌하지 않는다.
Quality:
- [ ] `validate-docs`가 통과한다.
- [ ] `git diff --check`가 통과한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260525-ATS-006.md
- docs/SR/SR-93.md
- docs/design/payment-integration-design.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-014-summary.md :
- 문서 변경 요약, 검증 결과, 후속 범위
Agent-facing -> deliverables/agent/WI-20260525-ATS-014-evidence-pack.md :
- 변경 파일, 근거, 검증 명령, rollback
Handoff Packet -> deliverables/agent/WI-20260525-ATS-014-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: changed markdown files and validation command output.
Tests: `python .agents/skills/validate-docs/scripts/validate_docs.py`, `git diff --check`.
Rollback: revert the documentation-only commit or restore changed markdown files.
