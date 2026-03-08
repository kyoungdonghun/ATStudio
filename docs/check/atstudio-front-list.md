# ATStudio 화면 목록 (Frontend)

> API Spec v6 기준 | v4 2026-03-07
> `[PUBLIC]` = 인증 불필요 / `auth required` = 로그인 필요 / `[ADMIN]` = 관리자 전용 / `⚠️` = API 미정의

> `🗑️ 삭제` = 상세/목록 페이지에서 `confirm()` 처리 / 회원탈퇴만 비밀번호 재확인 모달

---

## 👤 인증 / 회원가입

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| A-1 | 로그인 | `5.2 POST /api/auth/login` | [PUBLIC] |
| A-2 | 일반 회원가입 | `5.1 POST /api/users` | [PUBLIC] |
| A-3 | 소셜 로그인 (Google/Kakao/Naver) | `5.3 POST /api/auth/social/{provider}` | [PUBLIC] |
| A-4 | 소셜 회원 추가정보 입력 | `5.10 PUT /api/users/me/complete-profile` | auth required |

---

## 🎵 음원 (Track)

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 1 | 메인화면 | `1.2 GET /api/tracks` `2.2 GET /api/tags` `10.1 POST /api/likes/{trackId}` `11.1 POST /api/download-queue/{trackId}` | [PUBLIC] |
| 3 | 음원 목록 (리스트 타입) | `1.2 GET /api/tracks` `10.1 POST /api/likes/{trackId}` `11.1 POST /api/download-queue/{trackId}` | [PUBLIC] |
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
| 4 | 재생목록 목록 (이미지 타입) | `3.2 GET /api/playlists` | auth required |
| 5 | 재생목록 목록 (리스트 타입) | `3.2 GET /api/playlists` | auth required |
| C-1 | 재생목록 상세 | `3.3 GET /api/playlists/{playlistId}` | auth required |
| 8 | 재생목록 생성 | `3.1 POST /api/playlists` | auth required |
| 9 | 재생목록 수정 | `3.5 PUT /api/playlists/{playlistId}` `3.6 PUT (트랙 순서)` `3.7 DELETE (트랙 삭제)` `3.4 POST (트랙 추가)` `3.8 DELETE /api/playlists/{playlistId}` | auth required |

---

## 👤 개인 페이지 (회원)

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 10 | 개인정보 페이지 (비밀번호 변경 모달 포함) | `5.4 GET /api/users/me` `5.7 PUT /api/users/me` `5.9 DELETE /api/users/me` `5.11 PUT /api/users/me/password` | auth required |
| D-1 | 좋아요 목록 | `10.1~10.3 /api/likes` | auth required |
| E-1 | 재생 기록 | `4.2 GET /api/play-histories` `4.3 DELETE /api/play-histories` | auth required |
| F-1 | 내 라이선스 목록 | `7.1 GET /api/licenses/me` | auth required |
| F-2 | 라이선스 상세 | `7.3 GET /api/licenses/{licenseId}` | auth required |

---

## 🛒 장바구니 (다운로드 큐)

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 11 | 장바구니 (다운로드 큐) | `11.1~11.3 /api/download-queue` `1.5 GET /api/tracks/{trackId}/download` | auth required |

---

## 💳 구독

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 16-1 | 구독 플랜 비교/선택 | `6.1 GET /api/subscriptions` | [PUBLIC] |
| 16-2 | 구독 결제 | `6.3 POST /api/user-subscriptions` | auth required |
| 16-3 | 내 구독 현황 (업그레이드·해지) | `6.4 GET /api/user-subscriptions/me` `6.7 PUT` `6.10 DELETE` | auth required |

---

## 📺 유튜브 채널 화이트리스트

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| H-1 | 채널 등록/목록/수정 | `12.1~12.4 /api/whitelist-channels` | auth required |

---

## 🏢 기업 인증

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| I-1 | 기업 인증 신청 | `13.1 POST /api/company-certifications` | auth required |
| I-2 | 기업 인증 현황 | `13.2 GET /api/company-certifications/me` | auth required |

---

## ❓ 문의하기

| No | 화면명 | 관련 API | 인증 |
|----|--------|---------|------|
| 13 | 문의글 목록 | `8.3 GET /api/questions` | auth required |
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
| K-2 | 구독 목록 / 상세 / 강제 취소 | `6.5 GET /api/user-subscriptions` `6.6 GET` `6.8 PUT` `6.9 DELETE` | [ADMIN] |
| K-3 | 라이선스 조회 (회원별) | `7.2 GET /api/users/{userId}/licenses` `7.4 GET /api/users/{userId}/licenses/{licenseId}` | [ADMIN] |
| K-4 | 문의 관리 (상태 변경) | `8.3 GET /api/questions` `8.6 PUT /api/questions/{id}/status` | [ADMIN] |
| K-5 | 기업 인증 목록 / 심사 처리 | `13.3 GET /api/company-certifications` `13.4 GET` `13.5 PUT` | [ADMIN] |
| K-6 | 태그 관리 (생성/수정/삭제) | `2.1 POST /api/tags` `2.3 PUT` `2.4 DELETE` | [ADMIN] |
| K-7 | 트랙 관리 (전체 목록 + 활성화/삭제) | `1.8 GET /api/tracks/admin` `1.6 PUT /api/tracks/{id}` `1.7 DELETE /api/tracks/{id}` | [ADMIN] |

---

---

## ❌ 에러 페이지

| No | 화면명 | 설명 | 인증 |
|----|--------|------|------|
| ERR-1 | 404 Not Found | 존재하지 않는 경로 접근 시 표시 | [PUBLIC] |
| ERR-2 | 500 Server Error | 서버 오류 발생 시 표시 | [PUBLIC] |

---

> 총 **48개** 화면 (관리자 전용 포함)
