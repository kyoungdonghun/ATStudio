[WI HEADER]
WI ID: WI-20260307-ATS-014
REQ: REQ-20260307-ATS-008
Agent: docops
Depends On: WI-007~012 (Phase 1 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 3 — usecase 파일들 정합성 보완 (Phase 1 발견 기반, 사용자 컨펌 완료)
Scope (in):
  - docs/design/usecase/user-subscription.md
  - docs/design/usecase/util.md
  - docs/design/usecase/company-certification.md
  - docs/design/usecase/sound-playlist.md
  - docs/design/usecase/sound-track.md
  (이 5개 파일만)
Scope (out):
  - api-spec.md 수정 금지 (WI-013 담당)
  - front-list, modal-list, screen-flow 수정 금지 (WI-015 담당)
  - 백엔드 코드 수정 금지

DoD:
  - 아래 변경 사항 전부 해당 usecase 파일에 반영

Constraints/Forbidden:
  - 위 5개 파일 외 수정 금지
  - 아래 명시된 변경 외 임의 추가/삭제 금지

---

[ACCEPTANCE CRITERIA]

Functional:

[user-subscription.md]
- [ ] PAYMENT-007 (플랜 변경 UC): UPGRADE/DOWNGRADE 분기 추가
      UPGRADE: 즉시 적용 + 잔여기간 비례 결제 (proratedAmount)
      DOWNGRADE: pending 저장, 현재 기간 만료 후 적용 (pendingSubscriptionId, pendingBillingCycle)
- [ ] PAYMENT-010 (구독 취소 UC): 취소 후 유예 기간 정책 반영
      현재: status=CANCELLED, 혜택 즉시 중단
      → 변경: status=CANCELLED이나 expiresAt까지 서비스 이용 가능. expiresAt 이후 자동 만료.

[util.md]
- [ ] UTIL-006 (다운로드 카운트 조회 UC): 응답에 nextResetAt 필드 추가 (내일 00:00 LocalDateTime)
- [ ] 신규 UC 추가: UTIL-007 구독 변경 미리보기 (GET /api/utils/subscription-change-preview)
      Actor: 구독자
      Preconditions: 로그인, 활성 구독 보유
      Main Flow: subscriptionId + billingCycle 파라미터로 신규 플랜 조회 → UPGRADE/DOWNGRADE 판정 → proratedAmount 계산 → 응답
      Response fields: changeType, proratedAmount, effectiveDate, newPlanName, newBillingCycle

[company-certification.md]
- [ ] CC-001 Preconditions 수정:
      현재: "Reapplication allowed after REJECTED or REVISION_REQUESTED"
      → 변경: "초기 버전에서는 REJECTED/REVISION_REQUESTED 후 UI 재신청 흐름 없음. 관리자가 1:1 이메일 또는 문의를 통해 직접 안내 후 처리. 사이트 안정화 후 자동화 예정."

[sound-playlist.md]
- [ ] SOUND-002 (재생목록 생성 UC) Exception/Alternative Flow에 3개 제한 예외 추가:
      "활성 재생목록이 이미 3개인 경우 → 409 PLAYLIST_LIMIT_EXCEEDED 반환. 프론트엔드는 API 호출 전 버튼 비노출로 차단."

[sound-track.md]
- [ ] SOUND-019 UC 추가 (또는 기존 UC에 포함 확인):
      SOUND-019: 재생목록에 트랙 추가 (음원 목록/상세에서 "재생목록에 추가" 버튼)
      Actor: 구독자
      Trigger: 음원 목록(Screen 1/3) 또는 음원 상세(B-1)에서 "재생목록에 추가" 클릭
      Main Flow: 내 활성 재생목록 목록 SelectModal 표시 → 재생목록 선택 → POST /api/playlists/{id}/tracks → 201 Created → 완료 토스트
      (이미 정의된 UC와 중복이면 번호 참조만 추가)

Quality:
- [ ] 기존 UC 형식/스타일과 일관성 유지
- [ ] 변경 내역 파일:섹션 포인터 포함

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

Files (수정 대상 — 이 목록만):
- docs/design/usecase/user-subscription.md
- docs/design/usecase/util.md
- docs/design/usecase/company-certification.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/sound-track.md

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-014-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-014-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-014-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 항목별 파일:섹션 명시
