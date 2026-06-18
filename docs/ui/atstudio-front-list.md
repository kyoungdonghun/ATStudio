# ATStudio 화면 목록 (Frontend)

> API Spec v18 기준 | updated 2026-06-18
> `[PUBLIC]` = 인증 불필요 / `auth required` = 로그인 필요 / `[ADMIN]` = 관리자 전용 / `⚠️` = API 미정의

> `🗑️ 삭제` = 상세/목록 페이지에서 `confirm()` 처리 / 회원탈퇴만 비밀번호 재확인 모달

---

## 👤 인증 / 회원가입

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| A-1 | 로그인 | `14.12 GET /api/utils/public-capabilities` `5.2 POST /api/auth/login` | [PUBLIC] |
| A-2 | 일반 회원가입 | `14.2 GET /api/utils/check-email` `14.3 GET /api/utils/check-phone` `14.7 GET /api/utils/check-nickname` `14.12 GET /api/utils/public-capabilities` `5.1 POST /api/users` | [PUBLIC] |
| A-3 | 소셜 로그인 (Google/Kakao/Naver) | `5.3 POST /api/auth/social/{provider}` | [PUBLIC] |
| A-4 | 소셜 회원 추가정보 입력 | `5.10 PUT /api/users/me/complete-profile` | auth required |
| A-5 | 이메일 인증 | `14.9 GET /api/auth/verify-email` | [PUBLIC] |
| A-6 | 비밀번호 찾기 / 재설정 | `14.12 GET /api/utils/public-capabilities` `14.10 POST /api/auth/forgot-password` `14.11 POST /api/auth/reset-password` | [PUBLIC] |

---

## 🎵 음원 (Track)

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 1 | 메인화면 | `1.2 GET /api/tracks` `2.2 GET /api/tags` `10.1 POST /api/likes/{trackId}` `1.5 GET /api/tracks/{trackId}/download` | [PUBLIC] |
| 3 | 음원 목록 (리스트 타입) | `1.2 GET /api/tracks` `10.1 POST /api/likes/{trackId}` `1.5 GET /api/tracks/{trackId}/download` | [PUBLIC] |
| B-1 | 음원 상세 | `1.3 GET /api/tracks/{trackId}` `1.4 GET /api/tracks/{trackId}/stream` `4.1 POST /api/play-histories` | [PUBLIC] |
| 6 | 음원 업로드 (단일/다수) | `1.1 POST /api/tracks` `2.2 GET /api/tags` | [ADMIN] |
| 7 | 음원 수정 | `1.6 PUT /api/tracks/{trackId}` `1.3 GET /api/tracks/{trackId}` | [ADMIN] |

---

## 💿 앨범 (Album)

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| L-1 | 앨범 목록 (이미지 타입) | `15.2 GET /api/albums` | [PUBLIC] |
| L-2 | 앨범 목록 (리스트 타입) | `15.2 GET /api/albums` | [PUBLIC] |
| L-3 | 앨범 상세 | `15.3 GET /api/albums/{id}` | [PUBLIC] |
| L-4 | 앨범 생성 | `15.1 POST /api/albums` | [ADMIN] |
| L-5 | 앨범 수정 + 트랙 관리 | `15.4 PUT /api/albums/{id}` `15.5 DELETE /api/albums/{id}` `15.6 POST (트랙 추가)` `15.7 DELETE (트랙 제거)` `15.8 PUT (순서 변경)` | [ADMIN] |

---

## 💿 재생목록 (Playlist)

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 4 | 재생목록 목록 (이미지 타입) | `3.2 GET /api/playlists` | subscriber required |
| 5 | 재생목록 목록 (리스트 타입) | `3.2 GET /api/playlists` | subscriber required |
| C-1 | 재생목록 상세 | `3.3 GET /api/playlists/{playlistId}` | subscriber required |
| 8 | 재생목록 생성 | `3.1 POST /api/playlists` | subscriber required |
| 9 | 재생목록 수정 | `3.5 PUT /api/playlists/{playlistId}` `3.6 PUT (트랙 순서)` `3.7 DELETE (트랙 삭제)` `3.4 POST (트랙 추가)` `3.8 DELETE /api/playlists/{playlistId}` | subscriber required |

---

## 👤 개인 페이지 (회원)

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 10 | 개인정보 페이지 (비밀번호 변경 모달 포함) | `5.4 GET /api/users/me` `5.7 PUT /api/users/me` `5.9 DELETE /api/users/me` `5.11 PUT /api/users/me/password` | auth required |
| D-1 | 좋아요 목록 (음원 탭 + 앨범 탭) | `10.1~10.3 /api/likes` `10.4~10.6 /api/likes/albums` (SR-34) | auth required |
| E-1 | 재생 기록 | `4.2 GET /api/play-histories` `4.3 DELETE /api/play-histories` | auth required |
| F-1 | 내 라이선스 목록 | `7.1 GET /api/licenses/me` | auth required |
| F-2 | 라이선스 상세 | `7.3 GET /api/licenses/{licenseId}` | auth required |

---

## ⬇️ 다운로드 기록

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 11 | 다운로드 기록 (legacy route: `/download-queue`) | `11.4 GET /api/downloads/history` `11.5 GET /api/downloads/history/track-ids` `14.5 GET /api/utils/download-count` `1.5 GET /api/tracks/{trackId}/download` | auth required |

---

## 💳 구독

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 16-1 | 구독 플랜 비교/선택 | `6.1 GET /api/subscriptions` | [PUBLIC] |
| 16-2 | 구독 결제 (`/subscriptions/checkout`) | `6.3.4 POST /api/payments/billing-agreements/prepare` `6.3.5 POST /api/payments/billing-agreements/confirm` | auth required |
| 16-3 | 내 구독 현황 (업그레이드·다운그레이드·해지) | `6.4 GET /api/user-subscriptions/me` `6.7 PUT` `6.10 DELETE` `6.3.6 GET` `6.3.7 DELETE` | auth required |
| K-10 | 결제 운영 (`/admin/payments`) | `6.3.8 GET /api/admin/payments/orders` `6.3.8 GET /api/admin/payments/billing-agreements` `6.3.8 GET /api/admin/payments/subscription-payments` `6.3.8 GET /api/admin/payments/reconciliation-incidents` `6.3.8 PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status` `6.3.8 GET /api/admin/payments/receipts` `6.3.8 GET /api/admin/payments/operation-audit-logs` `6.3.8 GET/POST /api/admin/payments/refunds` `6.3.8 POST /api/admin/payments/entitlement-corrections` `6.3.8 GET/POST/PUT /api/admin/payments/settlements` | admin required |

> K-10 current UI tabs: `주문`, `자동결제`, `결제내역`, `대사 Incident`, `영수증`, `감사로그`, `정산`, `환불`, `권한 보정`.
> Boundary with K-2b: `사용자 구독 관리` handles ordinary local subscription status/cycle/expiration edits and grace-period cancellation. `결제 운영` handles payment-backed evidence, incident triage, refund execution, and refund-linked entitlement correction.
> Settlement UI is accounting review only: CSV import, mismatch review, missing-provider scan, and ignore workflow do not mutate subscriptions, payments, refunds, billing agreements, or provider state.
> Refund and entitlement-correction UI keeps the backend policy boundary: provider refund execution and local entitlement correction are separate admin-confirmed operations, and destructive execution requires typed confirmation.

---

## 📺 유튜브 채널 화이트리스트

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| H-1 | 채널 저장/목록/수정/등록 요청 | `12.1~12.6 /api/whitelist-channels` | auth required |

---

## 🏢 기업 인증

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| I-1 | 기업 인증 신청 | `13.1 POST /api/company-certifications` | auth required |
| I-2 | 기업 인증 현황 / 보완 재제출 | `13.2 POST /api/company-certifications/me/documents` `13.3 GET /api/company-certifications/me` | auth required |

---

## ❓ 문의하기

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 13 | 문의글 목록 (전체/내 문의 탭) | `8.3 GET /api/questions` | auth required |
| 14 | 문의글 작성 | `8.1 POST /api/questions` | auth required |
| 15 | 문의글 보기 | `8.4 GET /api/questions/{id}` `8.2 POST (답변 작성)` `8.5 GET (첨부파일)` `8.7 DELETE /api/questions/{id}` | auth required |

---

## 📢 공지사항

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 20 | 공지 목록 | `9.2 GET /api/notices` | [PUBLIC] |
| 21 | 공지 작성 (관리자 전용) | `9.1 POST /api/notices` | [ADMIN] |
| 21-2 | 공지 수정 (관리자 전용) | `9.4 PUT /api/notices/{noticeId}` | [ADMIN] |
| 22 | 공지 조회 | `9.3 GET /api/notices/{noticeId}` | [PUBLIC] |

---

## 🛡️ 관리자 페이지

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 18 | 통계 대시보드 | ⚠️ API 미정의 — 별도 설계 필요 | [ADMIN] |
| K-1 | 회원 목록 / 상세 / 권한 수정 | `5.5 GET /api/users` `5.6 GET /api/users/{id}` `5.8 PUT /api/users/{id}` | [ADMIN] |
| K-2 | 구독 플랜 관리 (읽기 전용) | `6.1 GET /api/subscriptions/admin` | [ADMIN] |
| K-2b | 사용자 구독 목록 / 강제 취소 (SR-14) | `6.5 GET /api/user-subscriptions` `6.6 GET` `6.8 PUT` `6.9 DELETE` | [ADMIN] |
| K-3 | 라이선스 조회 (회원별) | `7.2 GET /api/users/{userId}/licenses` `7.4 GET /api/users/{userId}/licenses/{licenseId}` | [ADMIN] |
| K-4 | 문의 관리 (상태 변경) | `8.3 GET /api/questions` `8.6 PUT /api/questions/{id}/status` | [ADMIN] |
| K-5 | 기업 인증 목록 / 상세 / 문서 다운로드 / 심사 처리 | `13.4 GET /api/company-certifications` `13.5 GET` `13.6 GET document` `13.7 PUT` | [ADMIN] |
| K-6 | 태그 관리 (생성/수정/삭제) | `2.1 POST /api/tags` `2.3 PUT` `2.4 DELETE` | [ADMIN] |
| K-7 | 트랙 관리 (전체 목록 + 활성화/삭제) | `1.8 GET /api/tracks/admin` `1.6 PUT /api/tracks/{id}` `1.7 DELETE /api/tracks/{id}` | [ADMIN] |
| K-11 | 화이트리스트 운영 | `12.7~12.9 /api/admin/whitelist-channels` | [ADMIN] |

---

---

## ❌ 에러 페이지

| No | 화면명 | 설명 | 인증 |
|----|--------|------|------|
| ERR-1 | 404 Not Found | 존재하지 않는 경로 접근 시 표시 | [PUBLIC] |
| ERR-2 | 500 Server Error | 서버 오류 발생 시 표시 | [PUBLIC] |

---

> 총 **53개** 화면 (관리자 전용 포함)
>
> **변경 이력**
> - v6 (2026-06-03): H-1 whitelist channel workflow expanded; K-11 admin whitelist operations screen added.
> - v5 (2026-03-29): K-2 분리 → K-2(구독 플랜 관리) + K-2b(사용자 구독 관리, SR-14); D-1 좋아요 목록 탭 분리(SR-34); 문의 목록 탭 추가(SR-30)
> - v4 (2026-03-07): 초기 확정 (48개)
