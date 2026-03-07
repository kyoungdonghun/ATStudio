[WI HEADER]
WI ID: WI-20260307-ATS-013
REQ: REQ-20260307-ATS-008
Agent: docops
Depends On: WI-007~012 (Phase 1 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 3 — api-spec.md 정합성 보완 (Phase 1 발견 기반, 사용자 컨펌 완료)
Scope (in):
  - docs/design/api-spec.md 수정 (이 파일만)
  - 적용할 변경 사항 (아래 상세 참조)
Scope (out):
  - usecase, front-list, modal-list, screen-flow 수정 금지 (WI-014/015 담당)
  - 백엔드 코드 수정 금지

DoD:
  - 아래 변경 사항 전부 api-spec.md에 반영
  - api-spec 버전 v5 → v6으로 업데이트
  - 수정 후 파일 일관성 확인

Constraints/Forbidden:
  - api-spec.md 외 다른 파일 수정 금지
  - 아래 명시된 변경 외 임의 추가/삭제 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] §14.5 GET /api/utils/download-count 응답 본문에 nextResetAt (LocalDateTime) 필드 추가
- [ ] §14 신규 항목 추가: GET /api/utils/subscription-change-preview?subscriptionId=X&billingCycle=Y
      Auth: subscribers only
      Response: { changeType: "UPGRADE"|"DOWNGRADE", proratedAmount: BigDecimal, effectiveDate: LocalDate, newPlanName: String, newBillingCycle: String }
      Error: 401, 400(잘못된 파라미터), 404(구독 미보유)
- [ ] §6.7 PUT /api/user-subscriptions/me 설명에 UPGRADE/DOWNGRADE 분기 추가
      UPGRADE: 즉시 적용 + proratedAmount 결제
      DOWNGRADE: 예약 저장 (pendingSubscriptionId, pendingBillingCycle), 현재 기간 만료 후 적용
      Response에 changeType 필드 포함 명시
- [ ] §6.10 DELETE /api/user-subscriptions/me 설명 수정
      현재: status=CANCELLED 즉시 적용, 혜택 즉시 중단
      → 변경: status=CANCELLED로 변경되나 expiresAt까지 서비스 이용 가능 (유예 기간)
- [ ] §1 Tracks 섹션에 신규 Admin 트랙 목록 API 추가 (§1.8)
      1.8 GET /api/tracks/admin
      Auth: [ADMIN]
      Description: 비활성 포함 전체 트랙 목록 (관리자 전용). is_active 파라미터로 필터 가능.
      Query Params: page, size, is_active(optional, 미전달 시 전체)
      Response: 기존 §1.2와 동일 구조 + is_active 필드 포함
      Error: 401, 403
- [ ] §3.1 POST /api/playlists Error Cases에 PLAYLIST_LIMIT_EXCEEDED 추가
      409 Conflict: { errorCode: "PLAYLIST_LIMIT_EXCEEDED", message: "활성 재생목록은 최대 3개까지 생성할 수 있습니다." }
- [ ] api-spec 버전 헤더 v5 → v6, 날짜 업데이트 (2026-03-07)

Quality:
- [ ] 기존 섹션 구조/형식과 일관성 유지
- [ ] 섹션 번호 충돌 없음

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

Files (수정 대상):
- docs/design/api-spec.md  ← 전체 읽은 후 위 항목 반영

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-013-summary.md:
- 수정 내역 요약 (변경 항목별 before/after 요약)

Agent-facing -> deliverables/agent/WI-20260307-ATS-013-evidence-pack.md:
- 변경 항목별 파일:라인 포인터

Handoff Packet -> deliverables/agent/WI-20260307-ATS-013-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 항목별 파일:라인 명시
