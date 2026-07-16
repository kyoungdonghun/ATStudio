[WI HEADER]
WI ID: WI-20260716-ATS-004
REQ: REQ-20260716-ATS-002
Agent: sa
Depends On: -
Blocks: WI-20260716-ATS-005, WI-20260716-ATS-006, WI-20260716-ATS-007, WI-20260716-ATS-008, WI-20260716-ATS-009

[WI SUMMARY]
Why: 기존 전수조사에서 남은 P2/X/P3 항목을 구현 전에 현재 코드 기준으로 재확정하고, 제품 정책을 바꾸지 않는 수정 불변식과 교차 레이어 영향 범위를 고정한다.
Scope (in/out):
- In: ATS020-P2-01~18, X-01~03, P3-01~02의 현재 근거 재확인, 구현 단위별 상태 전이·동시성·API·DB·프론트·문서 영향 맵, 검증 전략, 환경 조건 구분.
- Out: 코드 구현, DB 실행, 클라이언트 브랜치 변경, 공개 데모 변경, 멀티서버 lock, 제품 정책 변경.
DoD:
- 현재 코드와 최신 재판정 문서를 근거로 구현 가능한 항목과 환경 조건 항목을 분리한다.
- 인증, 결제, 화이트리스트, 기업 인증, 앨범·재생목록, 프론트, 문서·도구에 대한 불변식과 단계별 변경 계약을 작성한다.
- 공개 전체 감상, 구독 다운로드, 카드 정기결제 정책을 명시적으로 보존한다.
- 후속 WI-005~017이 참조할 설계 문서, 사용자 요약, Evidence Pack을 작성한다.
Constraints/Forbidden:
- 역사적 감사 문서를 소급 수정하지 않는다.
- 구현을 선행하거나 운영환경을 추정하지 않는다.
- retained DB, 실제 secret/JWT, 외부 proxy 상태를 확인된 것처럼 닫지 않는다.
- 클라이언트 작업트리 `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable`을 수정하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] P2-01~18, X-01~03, P3-01~02가 후속 소유 WI 또는 명시적 보류 근거에 매핑된다.
- [ ] 각 구현 영역의 허용 상태 전이와 동시성·보안 불변식이 명확하다.
- [ ] 문서 현행화 대상과 최종 검증 명령이 정의된다.
Performance:
- [ ] 결제 대사와 export의 bounded batch·pagination·index 검증 기준이 포함된다.
Quality:
- [ ] 설계 문서 내부 링크와 traceability ID가 유효하다.
- [ ] `git diff --check`와 문서 검증이 통과한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/versioning-policy.md

Tier 2 (Task Context):
- docs/design/index.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/design/db-schema.md
- docs/design/api-spec.md
- docs/ui/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260715-ATS-020-summary.md
- deliverables/user/WI-20260715-ATS-021-summary.md
- deliverables/user/WI-20260715-ATS-022-summary.md
- deliverables/user/WI-20260715-ATS-023-summary.md
- deliverables/user/WI-20260715-ATS-024-summary.md
- docs/audit/full-system-audit-20260713.md
- docs/audit/p1-remediation-trace-matrix-20260714.md

Files:
- src/main/java/com/atstudio/atstudio/security/AuthRateLimitFilter.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java
- src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java
- src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/store/playerStore.ts

Repro/Logs:
- `npm run format`
- `npm audit --omit=dev --json`
- current branch: `codex/p1-acceptance-hardening`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-004-summary.md :
- Current-state decision, ownership matrix, risks, approval boundaries
Agent-facing -> deliverables/agent/WI-20260716-ATS-004-evidence-pack.md :
- Evidence pointers, inspected files, commands, reproducibility, follow-up WI map
Handoff Packet -> deliverables/agent/WI-20260716-ATS-004-handoff.md :
- This packet
Design -> docs/design/remaining-remediation-design-20260716.md :
- Invariants, status transitions, implementation boundaries, verification matrix

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: `python .claude/scripts/validate_docs.py`, `git diff --check`
Rollback: Revert only WI-004 documentation files; do not alter historical audit evidence.
