---
version: 1.0
last_updated: 2026-08-10
project: ATS
owner: docops
category: work-summary
status: stable
related_wi: WI-20260809-ATS-032
dependencies:
  - path: ../agent/WI-20260809-ATS-032-evidence-pack.md
    reason: 상세 변경, 검증, 롤백 근거
  - path: ../agent/WI-20260809-ATS-032-re-review.md
    reason: 최종 제한 범위 재검토 결정
---

# WI-20260809-ATS-032 사용자 요약

## 결과

WI-032의 제한 범위 구현과 문서화를 완료했습니다. 결제 준비 단계에서
플랜 이름이나 URL의 결제 목적만 믿지 않고, 정확한 플랜 ID, 인증된 회원
유형, 결제 주기, 현재 구독 상태, 서버 금액이 모두 일치해야 Toss 카드
등록 단계로 진행됩니다.

## 수정한 문제

### CR-031-081: 화면 목적과 서버 결제 목적 불일치

기존에는 URL만 `BILLING_AGREEMENT`로 바꾸면 화면은 즉시 결제가 없는
결제수단 등록으로 표시하면서, 서버는 비구독자 상태를 근거로 정상 구독
금액의 `SUBSCRIBE` 주문을 준비할 수 있었습니다.

- 준비 요청을 `{subscriptionId, billingCycle, purpose}`로 고정했습니다.
- `purpose`는 `SUBSCRIBE` 또는 `BILLING_AGREEMENT`만 허용하며, 서버
  결정을 대신하지 않는 일치 확인 값입니다.
- 서버가 현재 구독 상태에서 목적을 다시 계산하고, 요청 목적과 다르면
  Billing Agreement 변경, 주문 저장, Provider 준비 호출 전에 거절합니다.
- 신규 구독 금액은 서버의 정확한 플랜/주기 가격이고, 결제수단 재등록
  금액은 정확히 `0`입니다.

### CR-031-082: 같은 이름의 다른 회원 유형 플랜 선택

플랜 이름만 전달하고 첫 번째 같은 이름을 선택하던 경로를 제거했습니다.

- 체크아웃은 정확한 `planId`, 인증된 `userType`, `billingCycle`을
  사용합니다.
- 화면은 인증 회원 유형으로 플랜을 조회하고 정확한 ID를 선택하며,
  서버도 인증 사용자와 플랜 회원 유형을 독립적으로 다시 검사합니다.
- 결제수단 재등록과 업그레이드 미리보기 복귀 정보도 이름 대신 정확한
  현재/복귀 플랜 ID와 회원 유형을 유지합니다.

## 결제 진행 조건

프런트엔드는 준비 응답의 주문 ID, `TOSS`, 목적, `READY`, 정확한 플랜
ID와 주기, 서버 금액, `KRW`, 만료 시각, `TOSS_BILLING_AUTH`, `CARD`,
client/customer key, 절대 HTTP(S) 성공/실패 URL을 모두 확인합니다. 하나라도
다르면 결제 주문을 활성 상태로 보관하지 않고 Toss SDK를 불러오거나
`requestBillingAuth`를 호출하지 않습니다. 화면 문구와 callback 값도 검증된
서버 응답의 목적, 금액, 주문 ID만 사용합니다.

## 검토 결과

- PG: 조건부 승인, 차단 사유 없음. 요청 목적을 권한으로 사용하지 않고
  불일치 시 부수효과 전에 닫히는 경계를 요구했습니다.
- QA-INTEG: 조건부 승인, 스키마/아키텍처/정책 차단 없음. 화면, 요청,
  서버 결정, 응답, SDK, Provider, 저장 상태를 분리한 계약을 확정했습니다.
- RE: 최초 P2 두 건과 P3 한 건의 수정을 확인한 뒤 `ACCEPTED`로
  재검토를 종료했습니다. RE 자체는 테스트를 실행하지 않았습니다.

## 검증 결과

- 집중 프런트엔드: 5개 파일, 77개 테스트 PASS.
- 집중 백엔드 `PaymentControllerTest` +
  `BillingAgreementApplicationServiceTest`: `BUILD SUCCESSFUL`.
- 전체 백엔드 테스트: exit 0. 구현 근거 기준 1,400개, 실패/오류 없음,
  13개 skip. 메인 재실행도 exit 0.
- 전체 프런트엔드 첫 실행: 629/630. WI-032와 무관한
  `TrackEditPage` 썸네일 타이밍 실패 1건.
- `TrackEditPage` 단독 재실행: 3/3 PASS.
- 전체 프런트엔드 재실행: 71개 파일, 630/630 PASS.
- 프런트엔드 typecheck, ESLint, Prettier, build PASS.
- 백엔드 build와 JaCoCo verification PASS.
- `git diff --check`: 공백 오류 없음. CRLF 변환 경고만 있음.
- 문서 5개 집중 Prettier, 상대 링크 0건 오류, 메타데이터/H1, 범위 지정
  `git diff --check` PASS.
- 전체 문서 검증기 최초 실행은 RE 재검토 문서의 절대경로+행번호 링크
  22개를 파일 경로로 판정해 exit 1이었습니다. 링크 대상을 행번호 없는
  저장소 상대 경로로 고친 뒤 최종 실행은 exit 0이었고, Tier 0, 내부 링크,
  545개 추적 ID, 문서 인덱스가 모두 PASS했습니다.

## 남은 범위

- WI-033의 prepare idempotency 및 중복 주문 제어는 구현하지 않았습니다.
- WI-034의 callback 응답 유실, 결과 불명, 새로고침 복구는 구현하지
  않았습니다.
- 실제 Toss Provider/SDK, 실제 DB/runtime, 배포, 실결제/환불 근거는
  만들지 않았습니다.
- 결제 정책, 가격 정책, 아키텍처, 스키마, 기존 데이터는 변경하지
  않았습니다.

## 롤백과 다음 작업

롤백은 WI-032의 제품 8개, 테스트 7개, 문서 마감 5개 파일에만 한정됩니다.
스키마와 기존 데이터가 바뀌지 않았으므로 데이터 롤백, Provider 취소,
결제 취소, 환불은 필요하지 않습니다.

WI 체인 규칙에 따라 다음 작업은 `WI-20260809-ATS-033`의 prepare
idempotency 및 중복 주문 제어입니다. `WI-20260809-ATS-034` 결과 복구는
별도 후속 범위로 유지합니다.

## 관련 문서

- [상세 Evidence Pack](../agent/WI-20260809-ATS-032-evidence-pack.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [API Specification](../../docs/design/api-spec.md)
- [Screen Flows](../../docs/ui/screen-flow.md)
