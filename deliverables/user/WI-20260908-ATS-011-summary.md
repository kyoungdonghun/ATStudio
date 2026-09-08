---
version: 1.1
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: REQ-20260908-ATS-002.md
    reason: Three-message SMTP approval
  - path: ../agent/WI-20260908-ATS-011-evidence-pack.md
    reason: Bounded send evidence and remaining confirmation
---

# WI-20260908-ATS-011: 한글 테스트 메일 확인

## Current recipient status (WI012 follow-up)
Actual receipt: **3/3; Korean readable; Inbox placement confirmed for these three messages**. The WI012 delegation supplies the user's smartphone-image observations; DocOps did not independently inspect image files in this context. Two same-subject subscription messages are grouped with TEST1/3 and TEST2/3; the admin payment-review message carries TEST3/3. This is three receipts across two subject groups, with no future deliverability claim.

Running-backend SMTP remains a separate [WI012](../agent/WI-20260908-ATS-012-evidence-pack.md) verification. No resend occurred in this documentation follow-up. Earlier recipient-pending and validation statements below are preserved as send-time history, not the current receipt status.

## Historical send record (before recipient confirmation)

현재 상태: **SMTP 수락 3/3, 실제 수신·Inbox/Spam 분류 대기**. MA 제공 근거이며 DocOps의 독립 실행 검증은 아니다.

- 2026-09-08 KST 21:39:44/48/52에 갱신 실패, 재시도 종료, 관리자 결제 점검 메일을 Gmail SMTP가 수락했다. 수락 3·실패 0·불명 0, 종료 코드 0이다. 제목은 `[AT.M] 구독 결제 안내` 2통과 `[AT.M] 결제 점검 이슈` 1통이다.
- 검증 JAR의 실제 EmailService 양식에 합성 입력과 TEST1/3~3/3·실제 구독 변동 없음 표시를 사용했다. 발송 전 3통의 MIME 확인은 PASS다. 초기 준비 시도는 실제 SMTP 시도 0건이며, 실제 발송 배치는 1회뿐이다. 재발송하지 않았다.
- **백엔드 SMTP는 미검증이다.** MA가 이전 21:00 재시작 때 외부 Gmail 설정을 가져오지 않았다고 보고했다. 과거의 "Gmail 설정 보존" 표현은 해당 재시작에 대해 입증되지 않았다. 이번 성공은 별도 단기 발송 프로세스에 한정되며 백엔드 설정·재시작에는 별도 승인이 필요하다.
- 이번 WI에서 Spring 기동·DB·금융 호출·스케줄러·서버 재시작·제품/Git 변경은 없었다. 수신 주소·비밀값은 기록하지 않으며 기존 이력과 SR-93/프로덕션 게이트 **OPEN**을 유지한다.
- 문서 기록 마감: validate-docs 1회 PASS, exit 0 (Tier 0·내부 링크·678 IDs·인덱스). 상세 산출물 식별자와 준비 시도 이력은 아래 근거에 기록했다. 실제 수신·백엔드 SMTP 미확인 상태는 유지한다.

상세 근거: [WI011 Evidence](../agent/WI-20260908-ATS-011-evidence-pack.md).
