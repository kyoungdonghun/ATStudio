[EVIDENCE PACK]
WI ID: WI-20260306-ATS-007
REQ: REQ-20260306-ATS-006
Agent: docops
Completed: 2026-03-06

---

[GENERATED FILES]

| File | Action | Lines |
|------|--------|-------|
| `docs/ui/modal-list.md` | Created | ~230 |
| `deliverables/user/WI-20260306-ATS-007-summary.md` | Created | ~55 |
| `deliverables/agent/WI-20260306-ATS-007-evidence-pack.md` | Created (this file) | - |

---

[COUNT VERIFICATION]

| Batch | Range | Count | Status |
|-------|-------|-------|--------|
| Section 1 (화면 목록 기반) | M-01 ~ M-10 | 10 | PASS |
| Section 2 (유스케이스 추가) | M-11 ~ M-27 | 17 | PASS |
| Total | M-01 ~ M-27 | **27** | PASS |

컴포넌트별 집계:

| Component | Modal IDs | Count |
|-----------|-----------|-------|
| ConfirmModal | M-07, M-08, M-11, M-13, M-14, M-19, M-20, M-21, M-22, M-23, M-24, M-25 | 12 |
| SelectModal | M-03, M-04, M-05, M-06, M-12, M-18 | 6 |
| InputModal | M-01, M-02 | 2 |
| FileUploadModal | M-15 [보류], M-16 | 2 |
| PlanCompareModal | M-09 | 1 |
| StatusModal | M-10, M-17 | 2 |
| PG 보류 | M-26, M-27 | 2 |
| **Total** | | **27** |

---

[ACCEPTANCE CRITERIA VERIFICATION]

| Criteria | Status | Notes |
|----------|--------|-------|
| 1차 M-01~M-10 전체 기재 | PASS | Section 1 표 10개 항목 확인 |
| 2차 M-11~M-27 전체 기재 | PASS | Section 2 표 17개 항목 확인 |
| 컴포넌트 6종 분류 테이블 | PASS | Component Classification 섹션 |
| PlanCompareModal 분기 흐름 (업그레이드/다운그레이드) | PASS | Section 3 Flow 2 ASCII 흐름도 |
| 재생목록 3개 제한 — 버튼 비노출(차단) 명시 | PASS | Playlist 3-Item Limit Handling 섹션 |
| TODO 3건 기재 (T-1/T-2/T-3) | PASS | TODO Section |
| 보류 항목 (M-15, M-26, M-27) 명시 | PASS | Deferred Items 섹션 + 표 내 [보류] 표기 |
| 화면 흐름 예시 4종 | PASS | Flow 1~4 (ConfirmModal/PlanCompareModal/SelectModal/InputModal) |
| 총 27개 카운트 | PASS | 문서 하단 총계 명시 |
| 명칭 일관성 ("재생목록"/"앨범") | PASS | 혼용 없음 확인 |
| API 섹션 번호 + URL 포함 | PASS | 각 모달 API 컬럼 확인 |

---

[SOURCE TRACEABILITY]

1차 소스 (화면 목록):
- `docs/ui/atstudio-front-list.md` v2
  - M-01~M-10 화면 번호 체계 및 API 참조 사용

2차 소스 (유스케이스):
- `docs/design/usecase/sound-track.md` → M-11 (SOUND-016)
- `docs/design/usecase/sound-playlist.md` → M-12 (SOUND-019), M-13 (SOUND-017)
- `docs/design/usecase/sound-album.md` → M-14 (ALBUM-005), M-06 추가확인 (ALBUM-006)
- `docs/design/usecase/user-info.md` → M-25 (INFO-006)
- `docs/design/usecase/user-subscription.md` → M-09/M-10 (PAYMENT-007/010), M-24 (PAYMENT-009), M-26/M-27 (PAYMENT-001/007)
- `docs/design/usecase/company-certification.md` → M-15 (CC-001), M-17 (CC-005)
- `docs/design/usecase/user-question.md` → M-16 (QUESTION-001), M-18 (QUESTION-007), M-20 (QUESTION-006)
- `docs/design/usecase/user-notice.md` → M-19 (ANNOUNCE-005)
- `docs/design/usecase/whitelist.md` → M-21 (WL-004)
- `docs/design/usecase/download-queue.md` → M-22 (DLQ-003)
- `docs/design/usecase/likes.md` → M-23 (LIKE-003)

Confirmed Design Decisions 소스:
- `deliverables/agent/WI-20260306-ATS-007-handoff.md` — PlanCompareModal 분기, 재생목록 제한 정책, 컴포넌트 6종 정의

---

[TODO ITEMS]

| ID | 항목 | 관련 API | 영향 모달 |
|----|------|---------|---------|
| T-1 | UTIL-006 `nextResetAt` 필드 추가 | `GET /api/utils/download-count` | Screen 11 (장바구니) |
| T-2 | `GET /api/utils/subscription-change-preview` 신규 API 구현 | 미구현 신규 | M-09 업그레이드 분기, M-27 보류 해제 전제 |
| T-3 | `user_subscriptions` `pendingSubscriptionId`, `pendingBillingCycle` 컬럼 추가 | `6.7 PUT /api/user-subscriptions/me` | M-09 다운그레이드 분기 |

---

[DEFERRED ITEMS]

| ID | 항목 | 보류 사유 | 해제 조건 |
|----|------|---------|---------|
| M-15 | 기업인증 서류 FileUploadModal | 파일 확장자/크기 정책 미확정 | 정책 확정 후 사양 추가 |
| M-26 | 구독 최초 결제 PG 모달 | PG 연동 미결정 | PG 선정 및 연동 설계 완료 후 |
| M-27 | 업그레이드 결제 PG 모달 | PG 연동 미결정 + T-2 API 미구현 | T-2 구현 + PG 연동 완료 후 |

---

[ROLLBACK]

git checkout -- docs/ui/modal-list.md

---

[NEXT ACTIONS]

- T-1/T-2/T-3 백엔드 보완은 별도 REQ로 등록 필요 (우선순위: T-2/T-3 높음)
- M-26/M-27 PG 보류 해제 시 modal-list.md v1.1 업데이트 필요
- React 프론트엔드 착수 시 이 문서를 컴포넌트 설계 기준 입력으로 사용
