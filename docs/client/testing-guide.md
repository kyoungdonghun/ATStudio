---
version: 2.0
last_updated: 2026-07-11
project: ATS
owner: qa
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Category entry point
  - path: 0-site-policy.md
    reason: Access and subscription baseline
  - path: 1-quick-checklist.md
    reason: Quick review checklist
  - path: 2-full-feature-checklist.md
    reason: Full acceptance checklist
  - path: 3-admin-checklist.md
    reason: Admin acceptance checklist
  - path: 4-sr-format.md
    reason: Feedback intake format
---

# ATStudio 테스트 가이드

> 목적: 클라이언트가 ATStudio를 직접 확인할 때, 어떤 순서로 무엇을 보면 되는지 안내합니다.

## 1. 테스트 전에 준비할 것

운영자에게 아래 정보를 받아주세요.

- 테스트 사이트 주소
- 관리자 계정 정보
- 결제 테스트를 진행해도 되는지 여부
- 결제 테스트를 한다면 Toss 테스트용 안내

실제 고객 정보, 실제 카드 정보, 실제 회사 서류는 사용하지 마세요. 테스트용 이름, 이메일, 파일을 사용합니다.

## 2. 추천 테스트 순서

1. [빠른 체크리스트](1-quick-checklist.md)로 큰 문제가 없는지 먼저 확인합니다.
2. [전체 기능 체크리스트](2-full-feature-checklist.md)로 사용자 기능을 차례대로 확인합니다.
3. 관리자 계정이 있으면 [관리자 체크리스트](3-admin-checklist.md)를 확인합니다.
4. 문제가 있으면 [SR 작성 양식](4-sr-format.md)에 맞춰 남깁니다.

## 3. 테스트 데이터 안내

테스트 서버는 확인 전에 초기화될 수 있습니다. 이 경우:

- 회원 계정이 거의 없을 수 있습니다.
- 음원, 앨범, 태그가 비어 있을 수 있습니다.
- 결제 기록, 구독 기록, 화이트리스트 요청, 기업 인증 요청이 비어 있을 수 있습니다.
- 필요한 콘텐츠는 관리자 화면에서 새로 만들어 확인합니다.

목록이 비어 있다는 것만으로는 오류가 아닙니다. 다만 새로 만든 데이터가 보이지 않거나 화면이 깨지면 SR로 남겨주세요.

## 4. 결제 테스트 주의

현재 구독 결제는 Toss 카드 정기결제 흐름입니다. 테스트 환경이라도 운영자가 허용하지 않았다면 실제 결제처럼 진행하지 마세요.

- 새 구독: 카드 등록 후 첫 결제가 진행됩니다.
- 업그레이드: 남은 기간 차액을 즉시 결제하고 바로 상위 플랜이 적용됩니다.
- 다운그레이드: 다음 결제일부터 낮은 플랜이 적용되도록 예약됩니다.
- 결제 주기 변경: 월간/연간 변경은 보통 다음 결제일부터 적용됩니다.
- 구독 취소: 만료일까지는 계속 이용할 수 있고 다음 자동 결제가 중단됩니다.

예상하지 못한 실제 결제처럼 보이면 즉시 테스트를 멈추고 보고해주세요.

## 5. 화면을 볼 때 기준

각 화면에서 아래 네 가지를 보면 됩니다.

1. 다음에 무엇을 해야 하는지 이해되는가?
2. 버튼이나 입력창의 설명과 실제 동작이 맞는가?
3. 다른 화면에 갔다가 돌아와도 결과가 유지되는가?
4. 잘못 입력했을 때 이해할 수 있는 안내가 나오는가?

하나라도 애매하면 SR로 남겨주세요.
