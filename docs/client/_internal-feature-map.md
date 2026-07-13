---
version: 1.0
last_updated: 2026-07-11
project: ATS
owner: qa
category: reference
status: stable
dependencies:
  - path: ../ui/screen-flow.md
    reason: Current screen flow
  - path: ../ui/atstudio-front-list.md
    reason: Screen inventory
  - path: ../design/api-spec.md
    reason: API inventory
  - path: ../payment/feature-inventory.md
    reason: Payment scope
---

# 내부 기능 근거 맵

> 목적: 쉬운 클라이언트 체크리스트가 현재 구현/설계와 어긋나지 않도록 근거 위치를 남깁니다.
> 이 문서는 클라이언트에게 먼저 보여주는 안내서가 아니라, 운영/개발자가 확인할 때 쓰는 내부 참고 문서입니다.

## 1. 주요 근거 위치

| 영역 | 프론트 근거 | 백엔드 근거 | 설계/문서 근거 |
|------|-------------|-------------|----------------|
| 라우트와 화면 흐름 | `frontend/src/router/index.tsx` | - | `docs/ui/screen-flow.md`, `docs/ui/atstudio-front-list.md` |
| 인증과 프로필 | `frontend/src/pages/auth/`, `frontend/src/pages/subscriber/ProfilePage.tsx` | `AuthController`, `UserController`, `UtilController` | `docs/design/usecase/user-info.md` |
| 음원과 검색 | `frontend/src/pages/public/TrackListPage.tsx`, `frontend/src/api/tracks.ts` | `TrackController`, `TrackService`, `TagController` | `docs/design/usecase/sound-track.md`, `docs/design/usecase/sound-tag.md` |
| 앨범 | `frontend/src/pages/public/Album*`, `frontend/src/pages/creator/Album*` | `AlbumController` | `docs/design/usecase/sound-album.md` |
| 재생목록 | `frontend/src/pages/subscriber/Playlist*` | `PlaylistController` | `docs/design/usecase/sound-playlist.md` |
| 다운로드와 라이선스 | `DownloadQueuePage`, `License*Page` | `DownloadController`, `DownloadQueueController`, `LicenseController` | `docs/design/usecase/download-queue.md`, `docs/design/usecase/user-license.md` |
| 구독 결제 | `SubscriptionPlanPage`, `SubscriptionPaymentPage`, `SubscriptionManagePage` | `PaymentController`, `UserSubscriptionController`, `SubscriptionController` | `docs/payment/feature-inventory.md`, `docs/payment/user-flows.md` |
| 관리자 결제 운영 | `frontend/src/pages/admin/PaymentReadOnlyPage.tsx` | `AdminPaymentController` | `docs/payment/admin-operations-guide.md` |
| 화이트리스트 채널 | `WhitelistChannelPage`, `WhitelistChannelManagePage` | `WhitelistChannelController`, `AdminWhitelistChannelController` | `docs/design/usecase/whitelist.md` |
| 기업 인증 | `CompanyCertApplyPage`, `CompanyCertStatusPage`, `CompanyCertManagePage` | `CompanyCertificationController` | `docs/design/usecase/company-certification.md` |
| 문의와 공지 | `Question*Page`, `Notice*Page` | `QuestionController`, `NoticeController` | `docs/design/usecase/user-question.md`, `docs/design/usecase/user-notice.md` |
| 관리자 메뉴 | `frontend/src/layouts/AdminLayout.tsx` | Admin controllers | `docs/ui/screen-flow.md` |

## 2. 체크리스트에 꼭 반영해야 하는 현재 동작

| 기능 | 현재 동작 |
|------|-----------|
| 음원 키워드 검색 | 클라이언트 체크리스트에서는 곡 제목 검색과 제목 아래 용도/가이드 문구 검색을 확인한다. 태그명 검색을 핵심 요구로 쓰지 않는다. |
| 화이트리스트 채널 | 사용자는 여러 유튜브 채널을 저장할 수 있다. 플랜 한도는 단순 저장이 아니라 등록 요청/처리 대상 채널에 적용된다. |
| 화이트리스트 CSV | 관리자 CSV에는 사용자 이메일과 채널 정보가 포함된다. |
| 기업 인증 | 기업 회원은 구독 전 기업 인증을 신청할 수 있다. 기업 구독 결제는 승인 후 가능하다. |
| 구독 결제 | 사용자 구독은 Toss 카드 정기결제 흐름을 기본으로 한다. 단건 결제는 주 사용자 흐름이 아니다. |
| 업그레이드 | 남은 기간 차액 결제 후 상위 플랜이 즉시 적용된다. |
| 다운그레이드 | 하위 플랜은 다음 결제일부터 적용되도록 예약된다. |
| 구독 취소 | 취소는 다음 자동 결제를 멈추는 것이며, 만료일까지 이용 가능하다. |
| 관리자 결제 운영 | 환불과 권한 보정은 별도 운영 절차이며 고위험 작업으로 안내한다. |
| 정산 | 정산 CSV 가져오기/대사는 운영자가 증빙을 확인하는 기능이며, 사용자 구독 권한을 직접 바꾸지 않는다. |

## 3. 문구 작성 규칙

- 클라이언트용 문서에는 API 이름보다 "어느 화면에서 무엇을 누르는지"를 먼저 씁니다.
- URL은 도움이 될 때만 적습니다.
- 클라이언트에게 직접 API 호출을 요청하지 않습니다.
- 카드번호 원문, 비밀번호, 빌링키, authKey, customerKey, provider payload, 회사 서류 원문은 문서나 스크린샷에 남기지 않게 안내합니다.
- 위험한 관리자 기능은 미리보기와 확인 절차를 먼저 안내하고, 실행은 승인된 테스트 DB에서만 하도록 적습니다.
