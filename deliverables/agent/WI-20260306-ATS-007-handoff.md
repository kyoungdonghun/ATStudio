[WI HEADER]
WI ID: WI-20260306-ATS-007
REQ: REQ-20260306-ATS-006
Agent: docops
Depends On: -
Blocks: -

---

[WI SUMMARY]

Why:
- 화면 목록 v2 정비 완료 후 프론트엔드 착수 전 필요한 모달/팝업 인터랙션 목록 문서화
- 세션 내 사용자 구두 승인 확보 (2026-03-06 "오케이. 잘부탁해.")

Scope:
- In: `docs/check/modal-list.md` 신규 작성
- Out: api-spec.md 수정 없음, 백엔드 코드 변경 없음

DoD:
- 1차 모달 (M-01~M-10, 10개): 화면 목록 기반
- 2차 모달 (M-11~M-27, 17개): 유스케이스 추가
- 컴포넌트 6종 분류 테이블
- PlanCompareModal 업그레이드/다운그레이드 분기 흐름
- 재생목록 3개 제한 처리 명시
- TODO/보류 항목 기재

Constraints/Forbidden:
- 명칭 일관성: "재생목록" (Playlist), "앨범" (Album)
- 화면 No 체계는 `atstudio-front-list.md`와 동일하게 사용
- api-spec.md 직접 수정 금지

---

[CONFIRMED DESIGN DECISIONS]

> MA 세션 분석 결과. 모두 사용자 확정 사항.

**구독 플랜 변경 로직 (PlanCompareModal)**
- 업그레이드: 즉시 적용 + `(newDailyRate - oldDailyRate) × 남은 일수` PG 결제 (클로드 방식)
- 다운그레이드: 현재 기간 유지, 다음 결제일부터 적용, 추가 결제 없음 (C방식)
- 취소: status=CANCELLED, `expires_at`까지 서비스 유지 (유예)

**재생목록 3개 제한**
- 활성 재생목록 >= 3개 시 "새 재생목록 만들기" 버튼 **비노출(차단)**

**컴포넌트 6종**
| 컴포넌트 | 용도 |
|---------|------|
| ConfirmModal | 삭제/취소 확인 (confirm() 패턴) |
| SelectModal | 목록에서 항목 선택 |
| FileUploadModal | 파일 첨부 |
| InputModal | 텍스트 입력 (비밀번호 재확인 등) |
| PlanCompareModal | 구독 플랜 비교/변경 분기 |
| StatusModal | 상태 안내/확인 |

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] 1차 M-01~M-10 (10개): 화면 목록 기반 모달 전체 기재
  - M-01: Screen 10 비밀번호 변경 (5.11)
  - M-02: Screen 10 회원탈퇴 비밀번호 재확인 (5.9)
  - M-03: Screen 6 음원 업로드 태그 선택 (2.2)
  - M-04: Screen 7 음원 수정 태그 선택 (2.2)
  - M-05: Screen 9 재생목록 수정 트랙 추가 (3.4)
  - M-06: L-5 앨범 수정 트랙 추가 (15.6)
  - M-07: Screen 9 재생목록 삭제 확인 (3.8)
  - M-08: L-5 앨범 삭제 확인 (15.5)
  - M-09: Screen 16-3 구독 플랜 변경 (6.7)
  - M-10: Screen 16-3 구독 취소 안내 (6.10)
- [ ] 2차 M-11~M-27 (17개): 유스케이스 추가 모달 전체 기재
  - M-11: K-7 트랙 삭제 확인 (1.7)
  - M-12: 음원 목록/상세 → 재생목록 선택 (3.4)
  - M-13: 재생목록 목록 삭제 확인 (3.8)
  - M-14: 앨범 목록 삭제 확인 (15.5)
  - M-15: I-1 기업인증 서류 업로드 (13.1) [보류]
  - M-16: Screen 14 문의글 첨부파일 업로드 (8.1)
  - M-17: K-5 기업인증 심사결과 처리 (13.5)
  - M-18: K-4 문의 상태 변경 (8.6)
  - M-19: 공지 삭제 확인 (9.5)
  - M-20: 문의 삭제 확인 (8.4)
  - M-21: H-1 채널 삭제 확인 (12.4)
  - M-22: Screen 11 장바구니 항목 제거 확인 (11.3)
  - M-23: D-1 좋아요 취소 확인 (10.3)
  - M-24: K-2 어드민 구독 강제 취소 확인 (6.9)
  - M-25: K-1 어드민 회원 권한 수정 확인 (5.8)
  - M-26: Screen 16-2 구독 결제 PG 창 [보류]
  - M-27: M-09 업그레이드 결제 PG 창 [보류]
- [ ] 컴포넌트 6종 분류 테이블 포함
- [ ] PlanCompareModal 분기 흐름:
  - 업그레이드: preview API → proratedAmount 표시 → PG 결제 [보류]
  - 다운그레이드: 다음 결제일 안내 → 변경 예약 → PUT 6.7
- [ ] 재생목록 3개 제한: 생성 버튼 비노출(차단) 명시
- [ ] TODO 백엔드 보완 3건 기재:
  - T-1: UTIL-006 nextResetAt 필드 추가
  - T-2: GET /api/utils/subscription-change-preview 신규 API
  - T-3: user_subscriptions pendingSubscriptionId, pendingBillingCycle 컬럼
- [ ] 보류 항목 2건 명시: PG 결제(M-26/27), 기업인증 서류 제한(M-15)

Quality:
- [ ] 명칭 일관: "재생목록"/"앨범" 혼용 없음
- [ ] 각 모달에 관련 API 참조 기재 (섹션번호 + URL)
- [ ] 총 27개 카운트 정확 (M-01~M-27)
- [ ] 화면 흐름 예시 4종 기재 (ConfirmModal, PlanCompareModal, SelectModal, InputModal)

---

[INPUT POINTERS]

Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - docops):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ/Context Docs:
- deliverables/user/REQ-20260306-ATS-006.md
- deliverables/agent/WI-20260306-ATS-007-handoff.md (이 파일 — Confirmed Design Decisions 참조)

Files (1차 소스 — 화면 목록):
- docs/check/atstudio-front-list.md

Files (2차 소스 — 유스케이스):
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/sound-album.md
- docs/design/usecase/user-info.md
- docs/design/usecase/user-subscription.md
- docs/design/usecase/company-certification.md
- docs/design/usecase/user-question.md
- docs/design/usecase/user-notice.md
- docs/design/usecase/whitelist.md
- docs/design/usecase/download-queue.md
- docs/design/usecase/likes.md

---

[OUTPUT CONTRACT]

User-facing → deliverables/user/WI-20260306-ATS-007-summary.md:
- 작성 완료 확인
- 총 모달 수 (27개) 및 컴포넌트 분류 요약

Agent-facing → deliverables/agent/WI-20260306-ATS-007-evidence-pack.md:
- 생성된 파일 경로
- 1차/2차/3차 카운트 검증
- TODO 항목 목록

Handoff Packet → deliverables/agent/WI-20260306-ATS-007-handoff.md:
- 이 파일

---

[TRACEABILITY REQUIREMENTS]

Evidence pointers:
- 생성 파일: `docs/check/modal-list.md`
- 1차 소스: `docs/check/atstudio-front-list.md`
- 2차 소스: `docs/design/usecase/*.md`

Tests: N/A
Rollback: git checkout -- docs/check/modal-list.md
