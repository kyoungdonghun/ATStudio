# WI-20260715-ATS-020 감사 재판정 요약

## 결론

현재 스냅샷은 `64db91c4a216336e52ea2cabdfa9445c6a657e9b`
(`codex/p1-acceptance-hardening`)와 감사 중 유입된 미커밋 WI-018/019 작업트리다.
이 기준에서 ATS020의 P1 13건은 모두 현재 코드와 독립 증거로 종료됐다. 다만 이
결론은 운영 DB, 실 Toss, 배포 설정, 클라이언트 인수, 전체 프로덕션 준비 완료를
뜻하지 않는다.

남은 위험은 P2 제품 결함, 환경별 배포 증명, 품질 도구 부채, 문서/추적성 부채로
분리된다. 특히 승인된 현재 정책은 **공개 전체 음원 재생 허용 + 원본 다운로드
구독자 제한**이다. 아래 조치는 공개 재생을 다시 미리듣기나 로그인 제한으로
축소하지 않는다.

## P1 재판정

| ID | 상태 | 현재 근거와 정책 보존 조치 |
|---|---|---|
| ATS020-P1-01 | CLOSED | 재생목록 썸네일은 `CanonicalImageService`로 재인코딩되고 공개 원본 경로는 차단된다. 레거시 파일은 X-01에서 별도 인벤토리/격리하며 공개 음원 재생 정책은 변경하지 않는다. |
| ATS020-P1-02 | CLOSED | 기업 인증 파일은 실제 PDF/JPEG/PNG 구조 검증, 이미지 재인코딩, private 저장소, 관리자 attachment 다운로드를 사용한다. 기존 파일은 X-01 절차로 백필/폐기한다. |
| ATS020-P1-03 | CLOSED | 로그아웃·비밀번호 변경·재설정 시 refresh capability가 폐기되고 replay/race 증거가 있다. access-token 수명 정책은 그대로 둔다. |
| ATS020-P1-04 | CLOSED | 저장소 변경 coordinator, journal, after-commit 삭제와 복구 경로가 주요 파일 도메인에 적용됐다. 운영 저장소 복구 훈련은 배포 게이트로 유지한다. |
| ATS020-P1-05 | CLOSED | 결제 감사 ENUM/DDL이 현재 Java 값과 정렬됐고 disposable MySQL 검증을 통과했다. 보유 DB 적용은 X-01에서 별도 증명한다. |
| ATS020-P1-06 | CLOSED | 최초 결제 실패 증거가 독립 트랜잭션으로 지속된다. Provider 실패를 성공으로 바꾸지 않고 재시도 가능 상태만 보존한다. |
| ATS020-P1-07 | CLOSED | 명령 idempotency, 잠금, 유일성, finalization fence 및 동시성 증거가 있다. Provider 경계와 중복 과금 금지는 유지한다. |
| ATS020-P1-08 | CLOSED | 갱신 order가 결제 기간·구독 identity에 묶여 stale order 재사용이 차단됐다. 기존 구독 수명주기는 유지한다. |
| ATS020-P1-09 | CLOSED | 갱신은 agreement별 짧은 로컬 단계와 트랜잭션 밖 Provider 호출로 분리됐다. 다중 인스턴스 소유권은 P2-04에서 다룬다. |
| ATS020-P1-10 | CLOSED | 환불 예약은 잠금·lease·합계 불변식과 동시성 증거를 갖췄다. 환불과 권한 보정의 분리 정책은 유지한다. |
| ATS020-P1-11 | CLOSED | CSV 셀의 formula-leading 값이 중화되고 회귀 테스트가 있다. 필요한 운영 필드는 유지하되 PII 범위 문제는 P2-07에서 줄인다. |
| ATS020-P1-12 | CLOSED | 소셜 콜백은 새 access token을 staging해 `/users/me`를 호출한 후 인증 상태를 commit한다. 로그인 원자성은 유지한다. |
| ATS020-P1-13 | CLOSED | 현 결제·클라이언트 문서는 retained DB, live Toss, 배포, 인수, 프로덕션 준비를 OPEN으로 명시하고 실제 회사 서류/중요 데이터 작업을 금지한다. 이후 문서 변경도 같은 launch gate를 유지한다. |

## 조건부 X 재판정

| ID | 상태 | 현재 근거와 정책 보존 조치 |
|---|---|---|
| ATS020-X-01 | ENVIRONMENT-CONDITIONAL | fresh-schema MySQL은 통과했지만 보유 DB와 레거시 인증/업로드 row는 미검증이다. DB 복제본 인벤토리, 순서화된 migration/backfill, Hibernate validate를 실행하고 원본을 보존한다. |
| ATS020-X-02 | PARTIALLY ADDRESSED / ENVIRONMENT-CONDITIONAL | trusted-proxy client identity 구현은 있으나 실제 다중 egress 증명은 없다. 배포별 trusted CIDR을 고정하고 두 외부 클라이언트의 독립 rate key를 증명한다. |
| ATS020-X-03 | ENVIRONMENT-CONDITIONAL | 과거 JWT fallback 사용 여부는 저장소만으로 판정할 수 없다. 비밀값을 기록하지 않은 환경별 조사, key rotation, 세션 폐기 증거가 필요하다. |
| ATS020-X-04 | CLOSED | 테스트 사용자 bootstrap과 acceptance startup guard가 비운영 profile/외부 secret 조건을 강제한다. 운영에서는 계속 fail-closed로 유지한다. |
| ATS020-X-05 | SUPERSEDED | 현재 썸네일 write path에는 P1-01 active-content 전제가 없다. 레거시 공개 파일은 X-01로 이관하고, ingress/CSP 검증은 수행하되 공개 음원 스트리밍을 차단하지 않는다. |

## P2 재판정

| ID | 상태 | 현재 근거와 정책 보존 조치 |
|---|---|---|
| ATS020-P2-01 | CONFIRMED OPEN | 가입/이메일·전화·닉네임 정확 일치 API는 전용 abuse control 대상이 아니다. 계정/IP 복합 key, endpoint별 budget, 429/관측 테스트를 추가하되 공개 가입은 유지한다. |
| ATS020-P2-02 | CONFIRMED OPEN | checkout route가 모든 로그인 role에 열리고 backend도 ADMIN을 배제하지 않는다. FE와 BE 양쪽에서 ADMIN 결제를 거부하고 관리 기능은 유지한다. |
| ATS020-P2-03 | CONFIRMED OPEN | 로컬 대사는 최근 100건, active agreement는 전수 조회한다. keyset pagination, bounded batch, query-aligned index와 MySQL 계획 증거를 추가한다. |
| ATS020-P2-04 | PARTIALLY ADDRESSED | billing-key 삭제 실패 Incident/retry는 있으나 crypto startup validation/rotation, scheduler zone, replica ownership은 미완료다. key ring/versioning과 startup guard를 추가하고 다중 인스턴스일 때만 분산 lock을 활성화한다. |
| ATS020-P2-05 | CONFIRMED OPEN | removal 완료 target과 `CANCELLED` 의미가 명확한 transition contract로 고정되지 않았다. 상태표와 완료 API/운영 절차를 정의하되 외부 whitelist 수동 처리 정책은 유지한다. |
| ATS020-P2-06 | CONFIRMED OPEN | plan count, primary 전환, export selection에 사용자 단위 잠금/DB 불변식이 없고 일부 조회·export가 무제한이다. 잠금·unique constraint·bounded page/export를 추가한다. |
| ATS020-P2-07 | CONFIRMED OPEN | 화면 keyword가 export에 적용되지 않고 status 전체 PII를 mutation하며 batch 재다운로드가 없다. export filter를 명시적으로 전달하고 immutable batch 재생성/다운로드와 최소 PII를 적용한다. |
| ATS020-P2-08 | PARTIALLY ADDRESSED | BUSINESS backend gate와 안전한 파일 경계는 닫혔지만 review reason 필수 규칙, locking, retention, review/download audit가 없다. 상태별 사유 검증, optimistic lock, 보존기간, 감사 이벤트를 추가한다. |
| ATS020-P2-09 | PARTIALLY ADDRESSED | WI-018에서 전체 길이 Range 처리는 종료됐지만 social-only 탈퇴, untyped OAuth token map, album count/order, count-then-write 한도 경쟁이 남았다. provider-aware 재인증, typed DTO, DB 집계/잠금으로 수정하며 공개 전체 스트림 정책을 유지한다. |
| ATS020-P2-10 | PARTIALLY ADDRESSED | 일부 구독 화면은 404와 서비스 실패를 구분하지만 plan/profile/drawer에는 실패→inactive 축약과 stale response 가능성이 남는다. 공통 상태 taxonomy와 abort/latest-request-wins를 적용한다. |
| ATS020-P2-11 | PARTIALLY ADDRESSED | WI-019가 실제 `play()` 결과·오류·stalled player 상태를 종료했고 공통 modal focus와 일부 return/playlist route도 개선됐다. global profile refresh, 일반 retry, 남은 a11y/focus restore를 별도 검증한다. |
| ATS020-P2-12 | PARTIALLY ADDRESSED / DOCUMENTATION | billing 계약 문서는 크게 정렬됐지만 화면 목록은 play-history API를 계속 활성 계약처럼 적는다. localStorage를 현재 SoT로 정하고 legacy API를 명시적으로 분리한다. |
| ATS020-P2-13 | PARTIALLY ADDRESSED / DOCUMENTATION | admin dashboard/site settings와 53 screen 통계는 반영됐지만 screen counting unit의 명시적 계약은 없다. route/page/overlay 포함 규칙과 생성 근거를 문서화한다. |
| ATS020-P2-14 | DOCUMENTATION/TRACEABILITY-ONLY | frontmatter와 registry/workboard/CTX/deliverables 추적이 일관되지 않다. 현재 산출물을 등록하고 validator를 강화하되 과거 증거를 소급 변경하지 않는다. |
| ATS020-P2-15 | PARTIALLY ADDRESSED / DOCUMENTATION | root count와 Phase 2 active 표기는 갱신됐지만 SR freshness/status metadata는 균일하지 않다. 단일 count rule과 자동 freshness/status 검증을 적용한다. |
| ATS020-P2-16 | CONFIRMED OPEN / TOOLING | 전체 Prettier gate는 현재 143개 파일에서 실패한다. 별도 승인 WI로 baseline을 고정하고 batch formatting 후 full-tree gate를 복구한다. 현재 WI의 changed-file gate 결과는 소급 변경하지 않는다. |
| ATS020-P2-17 | CONFIRMED OPEN / TOOLING | JaCoCo와 Vitest coverage provider/report가 없다. 두 도구를 별도 품질 WI로 도입하고 우선 관측 baseline 후 threshold를 승인한다. |
| ATS020-P2-18 | PARTIALLY ADDRESSED / ENVIRONMENT-CONDITIONAL | fresh MySQL schema/Hibernate validate와 7개 race는 통과했지만 retained DB migration, production index와 query-plan 증거는 없다. 복제 DB rehearsal과 EXPLAIN/인덱스 증거를 추가한다. |

## P3 재판정

| ID | 상태 | 현재 근거와 정책 보존 조치 |
|---|---|---|
| ATS020-P3-01 | DOCUMENTATION/TRACEABILITY-ONLY | PDF 본문 일치는 확인됐지만 Unicode title metadata와 generator/source-hash provenance가 없다. 결정적 생성 manifest와 hash를 추가하고 승인된 본문은 바꾸지 않는다. |
| ATS020-P3-02 | PARTIALLY ADDRESSED / DOCUMENTATION | one-time 구독 API는 blocked legacy로 표시됐지만 인증 directory hint, withdrawn semantics, 일부 frontend 문서가 남았다. deprecation 표와 제거 조건을 기록하고 호환 endpoint는 승인 전 삭제하지 않는다. |

## 증거 공백과 다음 조치

- `WI-20260714-ATS-025`~`034`에는 handoff만 있고 해당 ID의 Evidence Pack이 없다. 이후
  WI가 테스트·문서·결제 검증 일부를 대체했지만, 원 체인의 종료 여부는
  **DOCUMENTATION/TRACEABILITY-ONLY OPEN**이다. 각 WI를 대체 증거에 매핑해 공식 종료하거나
  미실행으로 닫아야 한다.
- 감사 진행 중 `WI-20260715-ATS-018`(공개 전체 스트림)과 `019`(player 상태)의
  Evidence Pack이 유입됐고 각각 70개 backend focused test 및 7개 frontend test와
  compile/typecheck/lint 증거로 완료됐다. 이 변경은 다른 작업자가 만든 미커밋 작업트리이며
  본 WI가 수정하지 않았다.
- `WI-20260715-ATS-021` 문서 현행화는 handoff만 존재한다. 따라서 현재 코드는 전체
  스트림이지만 active 문서의 bounded-preview 표현은 아직 stale하며, 공개 재생을 축소하지
  않고 supersession 문구로 정리해야 한다.
- `full-system-audit-20260713.md`의 P1-01~12 OPEN 서술과 2026-07-14 trace matrix의
  P1-05~10 OPEN baseline은 역사 기록으로는 유효하지만 현재 상태로는 stale하다. 또한
  bounded-preview current-state 문서는 WI-018 완료 후 stale하다. 반대로 WI-014의 전체
  Prettier FAIL은 WI-015의 scoped PASS가 지우지 않는다.

이번 WI는 읽기 전용 재판정이다. 애플리케이션 코드, 설정, 현재 상태 문서는 변경하지 않았고
테스트도 재실행하지 않았다. 기존 Evidence Pack의 검증 결과만 재사용했다.
