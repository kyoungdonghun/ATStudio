# WI-20260716-ATS-034 Backend Residual-Code Audit Summary

## 작업 결과

승인된 `REQ-20260716-ATS-004`와 WI 핸드오프에 따라 백엔드 잔여 코드를 정적으로 감사했다. 제품 코드, 설정, SQL, Git 상태, DB, 브랜치, worktree, 실행 중인 서버는 변경하지 않았다.

총 18개 후보 묶음을 다음과 같이 분류했다.

| 분류 | 수 | 의미 |
|---|---:|---|
| KEEP | 2 | 현재 운영 안전성 또는 인수환경 경계를 구성하므로 유지 |
| REMOVE | 7 | 저장소 내부 호출 부재와 대체 경로가 확인된 제거 후보 |
| REPLACE | 2 | 단순 삭제가 아니라 안전한 대체 경로가 먼저 필요한 후보 |
| REVIEW | 7 | 외부 호출, 운영 데이터 또는 제품 정책 확인 없이는 삭제를 확정할 수 없는 후보 |
| ARCHIVE | 0 | 소스 트리 안에서 별도 보관할 백업성 파일은 발견되지 않음 |

## 주요 판단

### 1. 운영 무결성 제어는 반드시 KEEP

다음 기능은 이름만 보고 임시 보정 코드로 오인해서 제거하면 안 된다.

- 결제 주문 멱등성 키와 provider attempt claim
- provider 성공 후 로컬 확정 처리 및 상태 전이 검증
- 결제 대사(reconciliation)와 incident 기록
- 결제 운영 감사 로그
- 환불·빌링키 정리 작업의 lease, claim, fence
- 결제·구독 저장소의 비관적 락
- 기업 인증·화이트리스트의 낙관적 락
- 저장소 mutation 복구 스케줄러
- acceptance 환경의 production 차단, host/origin 제한, 필수 secret 검증

이들은 과거 호환 코드가 아니라 현재 결제·구독·운영 정합성을 지키는 안전장치다.

### 2. 높은 확신의 REMOVE 후보

- 서버 play-history 호환 스택: 활성 SPA는 `localStorage`를 사용하고 서버 API 모듈은 import되지 않는다.
- 구형 download-queue 백엔드 스택: 현재 `/download-queue` 화면은 다운로드 내역 화면이며 구형 queue API 모듈은 호출되지 않는다.
- `PaymentCommandTransactionService.finalizeUpgrade`의 deprecated 4-인자 overload: 호출부가 모두 3-인자 메서드로 이동했다.
- `Track.previewFile`: 현재 정책과 구현은 전체 원본 재생이며 필드는 저장소 참조 검사 외에 사용되지 않는다.
- whitelist export의 `userIdSnapshot`, `userNicknameSnapshot`: 현재 export item 생성·응답에서 사용되지 않는 legacy 컬럼이다.
- Thymeleaf 설정 블록: starter, template, SSR controller가 없고 React SPA forward만 활성이다.
- 실제 경로와 일치하지 않는 `PUT /api/settings/*` 보안 matcher: 관리 API는 `/api/admin/settings/*`이고 `/api/admin/**` 보호가 적용된다.

위 항목도 실제 삭제는 WI-038 통합 판단과 사용자 승인 후, 관련 엔티티·테스트·스키마를 원자적으로 정리해야 한다.

### 3. REVIEW 또는 REPLACE가 필요한 항목

- 구형 단건 결제 endpoint/provider/DTO 묶음은 활성 프론트 호출이 없고 구독 결제에는 차단되어 있다. 다만 API 문서가 외부 stale client 관찰 또는 합의된 대체 근거를 삭제 조건으로 두고 있으므로 즉시 삭제가 아니라 REVIEW다.
- `POST /api/user-subscriptions` 직접 구독 생성 endpoint도 현재 항상 checkout-required 오류를 내지만 외부 호출 관찰 근거가 필요하다.
- billing-key V1 복호화는 보존할 DB가 정말 없다는 V1 전환 증거가 확보된 뒤 V2 key-ring만 남기는 REPLACE 대상이다.
- 관리자 구독 직접 수정·삭제는 실제 UI가 사용한다. 결제/권한 보정 감사 흐름과 분리되어 있어 단순 삭제보다 감사 가능한 관리 workflow로 대체할지 결정해야 한다.
- `subscription-status`, `user-type`, 관리자 구독 상세 API는 저장소 내 활성 호출이 없지만 외부 소비자를 정적으로 증명할 수 없어 REVIEW다.
- acceptance fixture bootstrap은 현재 인수환경 재현에 사용된다. production 차단 장치는 KEEP하고, bootstrap을 애플리케이션 밖 provisioning으로 옮길지는 별도 REVIEW다.
- provider enum의 구형 `MOCK`/`TOSS` 단건 값과 미구현 `KAKAOPAY` 값은 DB 및 멀티 PG 방향과 함께 검토해야 한다.

## 우선 위험

1. **High:** 운영 무결성 제어를 legacy/fallback으로 오인해 제거하면 중복 결제, 로컬·PG 상태 불일치, 이중 환불 또는 lease 탈취가 발생할 수 있다.
2. **Medium:** 관리자 구독 직접 mutation은 실제 사용 중이지만 일반 결제 감사/권한 보정 ledger와 의미가 분리되어 있다.
3. **Medium:** 단건 결제 및 직접 구독 endpoint는 내부 SPA에서 사용되지 않지만 외부 stale client 부재를 이번 정적 조사만으로 증명할 수 없다.
4. **Low:** play-history, download-queue, preview 필드, Thymeleaf 설정 등은 현재 동작보다 유지보수 오해와 스키마 잡음을 만든다.

## 승인 민감 항목

- 외부 호출 telemetry 없이 공개 API를 제거할지 여부
- 보존 DB가 없음을 전제로 billing-key V1 호환을 제거할지 여부
- 관리자 직접 구독 mutation을 유지할지, 감사 가능한 workflow로 대체할지 여부
- acceptance fixture provisioning을 애플리케이션 내부에 유지할지 여부
- provider enum에서 구현되지 않은 PG 값을 미리 보유할지 여부

## 조사 한계

저장소 내부의 백엔드 소스·설정·테스트·정적 프론트 API 소비자·관련 문서는 검색 기반으로 조사했다. 다음 표면은 WI의 read-only 제약 때문에 완전 확인할 수 없었다.

- 외부 클라이언트와 실제 API traffic/telemetry
- 운영 또는 보존 대상 DB 안의 legacy billing-key/테이블 데이터
- 모든 환경변수 조합에서의 실제 Spring profile 및 conditional bean 활성 상태
- reflection, 외부 스크립트 또는 저장소 밖 소비자가 만드는 동적 endpoint 호출
- 런타임 동작과 DB mutation을 요구하는 통합 검증

따라서 해당 증거가 필요한 후보는 REMOVE가 아니라 REVIEW/REPLACE로 분류했다.

## 다음 단계

WI-038에서 이 감사 결과를 프론트엔드·DB/설정·문서 감사 결과와 합쳐 V1 기준선을 제안해야 한다. 실제 삭제나 교체는 그 통합 목록에 대한 별도 승인 뒤 수행해야 한다.
