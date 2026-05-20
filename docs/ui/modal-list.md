---
version: 1.4
last_updated: 2026-05-20
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: docs/ui/atstudio-front-list.md
    version: v4
    reason: Screen number system and screen names (primary source)
  - path: docs/design/usecase/sound-track.md
    reason: SOUND-016 Track deletion modal
  - path: docs/design/usecase/sound-playlist.md
    reason: SOUND-017/019 Playlist modals
  - path: docs/design/usecase/sound-album.md
    reason: ALBUM-005/006 Album modals
  - path: docs/design/usecase/user-info.md
    reason: INFO-006/007 User info modals
  - path: docs/design/usecase/user-subscription.md
    reason: PAYMENT-007/009/010 Subscription modals
  - path: docs/design/usecase/company-certification.md
    reason: CC-001/005 Certification modals
  - path: docs/design/usecase/user-question.md
    reason: QUESTION-001/006/007 Question modals
  - path: docs/design/usecase/user-notice.md
    reason: ANNOUNCE-005 Notice deletion modal
  - path: docs/design/usecase/whitelist.md
    reason: WL-004 Channel deletion modal
  - path: docs/design/usecase/download-queue.md
    reason: DLQ-003 Queue removal modal
  - path: docs/design/usecase/likes.md
    reason: LIKE-003 Like removal modal
---

# ATStudio Modal/Popup Interaction List

> API Spec v5 / Usecase v5 기준 | v1.2 2026-03-07
> 관련 화면 목록: [docs/ui/atstudio-front-list.md](atstudio-front-list.md) v4

---

## Component Classification

| Component | 용도 | Pattern |
|-----------|------|---------|
| **ConfirmModal** | 삭제/취소 확인 | `[취소]` `[확인]` 2-button |
| **SelectModal** | 목록에서 항목 선택 (검색 + 선택 리스트) | 검색 입력 + 리스트 + `[선택]` |
| **FileUploadModal** | 파일 첨부 | 드래그앤드롭 또는 파일 선택 버튼 |
| **InputModal** | 텍스트 입력 (비밀번호 재확인 등) | 입력 필드 + `[취소]` `[확인]` |
| **PlanCompareModal** | 구독 플랜 비교/변경 (업그레이드/다운그레이드 분기) | 플랜 선택 + 분기 안내 + 실행 버튼 |
| **StatusModal** | 상태 안내/확인 (안내 텍스트 + `[확인]`) | 안내 텍스트 + `[확인]` 1-button |
| **ReviewModal** | 관리자 심사 처리 (상태 선택 + 메모 입력) | 상태 드롭다운 + adminNote 텍스트입력 + `[취소]` `[처리]` 2-button |

---

## Section 1: Screen-Based Modals (M-01 ~ M-10)

> 소스: `docs/ui/atstudio-front-list.md`

| ID | 화면 | 트리거 | 내용 | 컴포넌트 | API |
|----|------|--------|------|----------|-----|
| M-01 | Screen 10 (개인정보) | "비밀번호 변경" 클릭 | 현재 비밀번호 + 새 비밀번호 입력 | InputModal | `5.11 PUT /api/users/me/password` |
| M-02 | Screen 10 (개인정보) | "회원탈퇴" 클릭 | 탈퇴 안내 + 비밀번호 재확인 | InputModal | `5.9 DELETE /api/users/me` |
| M-03 | Screen 6 (음원 업로드) | 태그 선택 영역 클릭 | 태그 검색 + 다중 선택 | SelectModal | `2.2 GET /api/tags` |
| M-04 | Screen 7 (음원 수정) | 태그 수정 영역 클릭 | 태그 검색 + 다중 선택 | SelectModal | `2.2 GET /api/tags` |
| M-05 | Screen 9 (재생목록 수정) | "트랙 추가" 클릭 | 트랙 검색 + 선택 | SelectModal | `3.4 POST /api/playlists/{playlistId}/tracks` |
| M-06 | L-5 (앨범 수정 + 트랙 관리) | "트랙 추가" 클릭 | 트랙 검색 + 선택 | SelectModal | `15.6 POST /api/albums/{id}/tracks` |
| M-07 | Screen 9 (재생목록 수정) | "재생목록 삭제" 클릭 | "재생목록을 삭제하시겠습니까?" | ConfirmModal | `3.8 DELETE /api/playlists/{playlistId}` |
| M-08 | L-5 (앨범 수정) | "앨범 삭제" 클릭 | "앨범을 삭제하시겠습니까?" | ConfirmModal | `15.5 DELETE /api/albums/{id}` |
| M-09 | Screen 16-3 (내 구독) | "플랜 변경" 클릭 | 플랜 비교 + 업그레이드/다운그레이드 분기 | PlanCompareModal | `6.7 PUT /api/user-subscriptions/me` |
| M-10 | Screen 16-3 (내 구독) | "구독 취소" 클릭 | 취소 후 유예 안내 + 확인 | StatusModal | `6.10 DELETE /api/user-subscriptions/me` |

---

## Section 2: Usecase-Derived Modals (M-11 ~ M-31)

> 소스: `docs/design/usecase/*.md`

| ID | UC | 발생 화면 | 트리거 | 내용 | 컴포넌트 | API |
|----|-----|---------|--------|------|----------|-----|
| M-11 | SOUND-016 | K-7 (트랙 관리) | "트랙 삭제" 클릭 | "트랙을 삭제하시겠습니까?" | ConfirmModal | `1.7 DELETE /api/tracks/{trackId}` |
| M-12 | SOUND-019 | 1/3/B-1 (음원 목록/상세) | "재생목록에 추가" 클릭 | 내 재생목록 목록 선택 | SelectModal | `3.4 POST /api/playlists/{playlistId}/tracks` |
| M-13 | SOUND-017 | Screen 4/5 (재생목록 목록) | "재생목록 삭제" 클릭 | "재생목록을 삭제하시겠습니까?" | ConfirmModal | `3.8 DELETE /api/playlists/{playlistId}` |
| M-14 | ALBUM-005 | L-1/L-2 (앨범 목록) | "앨범 삭제" 클릭 | "앨범을 삭제하시겠습니까?" | ConfirmModal | `15.5 DELETE /api/albums/{id}` |
| M-15 | CC-001 | I-1 (기업인증 신청) | 서류 첨부 영역 클릭 | 파일 업로드 (복수 가능) **[보류]** | FileUploadModal | `13.1 POST /api/company-certifications` |
| M-16 | QUESTION-001 | Screen 14 (문의글 작성) | 첨부파일 클릭 | 파일 업로드 | FileUploadModal | `8.1 POST /api/questions` |
| M-17 | CC-005 | K-5 (기업인증 심사) | 심사결과 처리 클릭 | APPROVED / REVISION_REQUESTED / REJECTED 선택 + adminNote 입력 | ReviewModal | `13.5 PUT /api/company-certifications/{certificationId}` |
| M-18 | QUESTION-007 | K-4 (문의 관리) | 상태 변경 클릭 | 문의 상태 선택 (OPEN / IN_PROGRESS / RESOLVED / CLOSED) | SelectModal | `8.6 PUT /api/questions/{questionId}/status` |
| M-19 | ANNOUNCE-005 | Screen 22 (공지 조회) | "공지 삭제" 클릭 | "공지를 삭제하시겠습니까?" | ConfirmModal | `9.5 DELETE /api/notices/{noticeId}` |
| M-20 | QUESTION-006 | Screen 15 (문의 보기) | "문의 삭제" 클릭 | "문의를 삭제하시겠습니까?" | ConfirmModal | `8.7 DELETE /api/questions/{questionId}` |
| M-21 | WL-004 | H-1 (채널 등록/목록/수정) | "채널 삭제" 클릭 | "채널을 삭제하시겠습니까?" | ConfirmModal | `12.4 DELETE /api/whitelist-channels/{channelId}` |
| M-22 | DLQ-003 | Legacy Screen 11 (pre-SR-79) | "항목 제거" 클릭 | "장바구니에서 제거하시겠습니까?" | ConfirmModal | `11.3 DELETE /api/download-queue/{trackId}` |
| M-23 | LIKE-003 | D-1 (좋아요 목록) | "좋아요 취소" 클릭 | "좋아요를 취소하시겠습니까?" | ConfirmModal | `10.3 DELETE /api/likes/{trackId}` |
| M-24 | PAYMENT-009 | K-2 (구독 목록/상세) | "구독 강제 취소" 클릭 | "구독을 강제 취소하시겠습니까?" | ConfirmModal | `6.9 DELETE /api/user-subscriptions/{userSubscriptionId}` |
| M-25 | INFO-006 | K-1 (회원 목록/상세) | "권한 수정 저장" 클릭 | "회원 권한을 변경하시겠습니까?" | ConfirmModal | `5.8 PUT /api/users/{userId}` |
| M-26 | PAYMENT-001 / SR-92 | Screen 16-2 (구독 결제) | "카드 등록하기" 클릭 | Toss billing auth 진행 | SR-92 one-time widget UX is retired for subscription scope | `6.3.4 POST /api/payments/billing-agreements/prepare` + `6.3.5 POST /api/payments/billing-agreements/confirm` |
| M-27 | PAYMENT-007 | M-09 (PlanCompareModal 내) | 업그레이드 확인 | 차액 즉시 결제 후 플랜 변경 | No route transition; server charges active billing agreement through `PUT 6.7` | `6.7 PUT /api/user-subscriptions/me` |
| M-28 | - | K-6 (태그 관리) | "태그 삭제" 클릭 | "태그를 삭제하시겠습니까?" | ConfirmModal | `2.4 DELETE /api/tags/{tagId}` |
| M-29 | SR-34 | D-1 (좋아요 목록 > 앨범 탭) | "좋아요 취소" 클릭 | "좋아요를 취소하시겠습니까?" | ConfirmModal | `DELETE /api/likes/albums/{albumId}` |
| M-30 | SR-79 | Screen 11 (다운로드 기록) | "전체 재다운로드" 클릭 | "{N}곡을 다운로드합니다. 계속하시겠습니까?" | ConfirmDialog | `GET /api/downloads/history/track-ids` + `GET /api/tracks/{trackId}/download` |
| M-31 | REQ-20260518-ATS-001 | Screen 16-2 / billing callback | "카드 등록하기" 클릭 | Toss billing auth 진행 후 성공/실패/복귀/재시도 안내 | Target: dedicated checkout/callback route; local debug may keep inline state panel | `6.3.4 POST /api/payments/billing-agreements/prepare` + `6.3.5 POST /api/payments/billing-agreements/confirm` |

---

## Section 3: Screen Flow Examples

### Flow 1: ConfirmModal — 재생목록 삭제 (M-07 / M-13)

```
[Screen 4/5 or Screen 9]
  재생목록 항목 → "삭제" 버튼 클릭
        |
        v
  ConfirmModal
  +----------------------------------+
  | 재생목록을 삭제하시겠습니까?        |
  | 이 작업은 되돌릴 수 없습니다.       |
  |                                  |
  |         [취소]    [확인]          |
  +----------------------------------+
        |                |
      dismiss         3.8 DELETE /api/playlists/{playlistId}
                          |
                      204 No Content
                          |
                      목록 화면으로 이동 (재조회)
```

---

### Flow 2: PlanCompareModal — 구독 변경 분기 (M-09)

```
[Screen 16-3 내 구독]
  "플랜 변경" 버튼 클릭
        |
        v
  PlanCompareModal
  +-------------------------------------------------------+
  | 플랜 변경                                              |
  | 현재: BASIC MONTHLY  →  변경: PREMIUM MONTHLY          |
  |                                                       |
  |  [업그레이드 분기]        [다운그레이드 분기]            |
  |  즉시 적용               다음 결제일부터 적용             |
  |  즉시 결제 차액 표시      "추가 결제 없음" 안내           |
  |  다음 결제일/금액 표시    다음 결제일: {expires_at}      |
  |                                                       |
  |  [취소]   [플랜 변경 확인]                               |
  +-------------------------------------------------------+

  업그레이드 경로:
    GET /api/utils/subscription-change-preview (T-2)
      → proratedAmount = 남은 기간의 정수 원 플랜 차액
      → [플랜 변경 확인] 클릭
      → PUT 6.7 /api/user-subscriptions/me
      → 서버가 active billing agreement로 차액 즉시 결제
      → proratedAmount = 0이면 provider charge 없이 즉시 적용
      → 결제 성공 후 상위 플랜 즉시 적용

  다운그레이드 경로:
    "다음 결제일({expires_at})부터 변경 · 추가 결제 없음" 안내
      → [변경 예약] 클릭
      → PUT 6.7 /api/user-subscriptions/me (pendingSubscriptionId)
      → 200 OK → 화면 갱신
```

---

### Flow 3: SelectModal — 재생목록에 트랙 추가 (M-12)

```
[Screen 1/3 음원 목록 or B-1 음원 상세]
  트랙 항목 → "재생목록에 추가" 버튼 클릭
        |
        v
  SelectModal
  +----------------------------------+
  | 재생목록 선택                      |
  | [검색창: 재생목록 이름 검색]         |
  |                                  |
  |  [ ] 재생목록 A  (트랙 5개)        |
  |  [ ] 재생목록 B  (트랙 2개)        |
  |  [ ] 재생목록 C  (트랙 0개)        |
  |                                  |
  |  * 활성 재생목록 0개 시:            |
  |    "재생목록을 먼저 만들어주세요"    |
  |                                  |
  |         [취소]    [추가]          |
  +----------------------------------+
        |                |
      dismiss     3.4 POST /api/playlists/{playlistId}/tracks
                      {trackId}
                          |
                      201 Created → 완료 토스트
```

---

### Flow 4: InputModal — 비밀번호 변경 (M-01)

```
[Screen 10 개인정보]
  "비밀번호 변경" 버튼 클릭
        |
        v
  InputModal
  +----------------------------------+
  | 비밀번호 변경                      |
  |                                  |
  | 현재 비밀번호: [______________]    |
  | 새 비밀번호:   [______________]    |
  | 비밀번호 확인: [______________]    |
  |                                  |
  |         [취소]    [변경]          |
  +----------------------------------+
        |                |
      dismiss    5.11 PUT /api/users/me/password
                 {currentPassword, newPassword}
                          |
                  204 No Content → 완료 토스트
                  400/401 → 오류 메시지 인라인 표시
```

---

## Playlist Tier Limit Handling
> **Updated baseline (2026-04-18)**: playlist creation limit follows the current subscription tier's `subscriptions.max_playlists`, not a fixed 3 items.
> Frontend and backend both use the same tier limit, and admin has no playlist-specific bypass.

활성 재생목록 개수 기준: `GET /api/playlists` 응답에서 `is_active=true` 항목 수.

| 조건 | UI 처리 |
|------|---------|
| 활성 재생목록 >= 3개 | "새 재생목록 만들기" 버튼 **비노출(차단)** |
| 활성 재생목록 0개 (M-12 SelectModal 내) | "재생목록을 먼저 만들어주세요" 안내 텍스트 표시 |
| 활성 재생목록 1~2개 | 버튼 정상 노출 |

> **근거**: `PlaylistService.java:46-48` — `countByUserAndIsActiveTrue >= 3` 시 `PLAYLIST_LIMIT_EXCEEDED` (409) 반환. 프론트엔드는 API 호출 전 버튼 비노출로 UX 차단.

---

## Backend Supplement (완료)

> REQ-20260307-ATS-007 에서 T-1/T-2/T-3 전부 구현 완료 (2026-03-07)

| # | 항목 | 상태 |
|---|------|------|
| T-1 | `nextResetAt` 필드 (`GET /api/utils/download-count`) | ✅ 완료 |
| T-2 | `GET /api/utils/subscription-change-preview` | ✅ 완료 (UPGRADE/DOWNGRADE 분기, proratedAmount, nextBillingDate, nextBillingAmount) |
| T-3 | `user_subscriptions` pending 컬럼 + DOWNGRADE 예약 | ✅ 완료 (스케줄러 적용은 별도 REQ) |

---

## Deferred Items

| # | 항목 | 사유 |
|---|------|------|
| M-26 | Toss billing auth checkout — 구독 최초 결제 (Screen 16-2) | One-time Toss widget UX work under SR-92 is retired. Production focus is billing auth return/retry recovery. |
| M-27 | Billing-key upgrade charge — 업그레이드 결제 (M-09 내) | No checkout modal is needed. Server-side recurring charge, failure recovery, and user copy are the remaining hardening focus. |
| M-31 | Toss billing auth checkout — 정기결제 등록 | Dedicated checkout/callback route is preferred for mobile auth return, stale redirect recovery, and retry messaging. |
| M-15 | 기업인증 서류 파일 제한 (I-1) | 업로드 허용 파일 확장자 및 최대 크기 정책 미확정. FileUploadModal 구현 시 별도 정의 필요. |

---

> 총 **31개** 모달 (M-01 ~ M-31)
> - 1차 (화면 목록 기반): M-01 ~ M-10 (10개)
> - 2차 (유스케이스 추가): M-11 ~ M-31 (21개)
> - 보류/구현 후보: M-15, production checkout presentation for M-26/M-27/M-31
> v1.2 2026-03-07 → v1.3 2026-03-29 → v1.4 2026-05-16

## Related Documents

### Required References

- [atstudio-front-list.md](atstudio-front-list.md): 화면 번호 체계 및 화면명 기준 (1차 소스)
- [docs/design/usecase/sound-track.md](../design/usecase/sound-track.md): SOUND-016 트랙 삭제 UC
- [docs/design/usecase/sound-playlist.md](../design/usecase/sound-playlist.md): SOUND-017/019 재생목록 UC
- [docs/design/usecase/sound-album.md](../design/usecase/sound-album.md): ALBUM-005/006 앨범 UC
- [docs/design/usecase/user-subscription.md](../design/usecase/user-subscription.md): PAYMENT-007/009/010 구독 UC

### Reference Documents

- [docs/design/api-spec.md](../design/api-spec.md): API 섹션 번호 및 URL 기준
- [deliverables/agent/WI-20260306-ATS-007-handoff.md](../../deliverables/agent/WI-20260306-ATS-007-handoff.md): WI 핸드오프 (Confirmed Design Decisions)
