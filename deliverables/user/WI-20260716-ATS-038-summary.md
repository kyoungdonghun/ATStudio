# WI-20260716-ATS-038 통합 판정 요약

## 결론

백엔드, 프론트엔드, DB·설정, 문서·저장소 전수조사인 WI-034~037을 하나의 V1 잔여 코드 판정표로 통합했습니다.

- 원천 판정 단위: **101개**
- 중복 제거 후 실행 단위: **56개**
- 제품 코드·설정·SQL·기존 문서·DB·Git·서버 변경: **0건**
- 현재 단계: **삭제 승인 직전에서 정지**

| 판정 | 수 | 의미 |
|---|---:|---|
| `KEEP` | 13 | 현재 기능·안전장치·역사 기록이므로 보존 |
| `REMOVE` | 16 | 제거 후보지만 각 선행 증명과 별도 승인이 필요 |
| `REPLACE` | 12 | 경로 또는 기능을 유지하면서 현재 V1 방식으로 통합 |
| `ARCHIVE` | 3 | 활성 기준에서 제외하고 역사 증거로 보존 |
| `REVIEW` | 12 | 외부 호출·DB·운영 정책 등의 근거가 부족해 아직 실행 금지 |
| **합계** | **56** | 중복 제거된 통합 판정 행 |

## 냉정한 핵심 판단

정리 후보는 충분히 발견됐지만, 전부 같은 위험도가 아닙니다.

### 바로 삭제 후보로 볼 수 있는 내부 잔여물

- 호출 없는 deprecated 결제 upgrade overload
- 실제 의존성이 없는 Thymeleaf 설정
- 존재하지 않는 `/api/settings/*`용 보안 matcher
- 사용하지 않는 `DataTable`
- 호출 없는 프론트 API wrapper와 타입
- 의미 없는 `.gitkeep`

이들은 외부 계약이나 DB 구조와 직접 묶이지 않아 가장 먼저 정리할 수 있습니다. 그래도 삭제 후 전체 컴파일·테스트·SPA 기동 검증은 필요합니다.

### 반드시 묶어서 처리해야 하는 항목

- 서버 play history와 프론트 API, `play_histories` 테이블, 문서
- 구형 download queue API·엔티티·테이블과 프론트 wrapper
- `preview_file` 필드·컬럼·스토리지 검사
- 화이트리스트의 구형 user ID/nickname snapshot
- `/download-queue`를 실제 역할인 `/downloads`로 변경
- 네이티브 확인창, 관리자 결제 화면명, Mock CSS 명칭

이 항목은 파일 하나만 지우면 계약이나 화면이 깨집니다. 백엔드·프론트·DB·테스트·문서를 하나의 변경으로 처리해야 합니다.

### 절대 잔여 코드로 오인하면 안 되는 항목

- 결제 멱등성, claim/fence, 상태 전이
- reconciliation과 Incident
- 감사로그
- 환불·빌링키 정리 lease
- 비관적·낙관적 lock
- 저장소 mutation recovery
- acceptance의 production 차단, host/origin/CORS/secret 검증
- OAuth state·PKCE, 요청 generation fence, 브라우저 storage fallback

이들은 과거 호환 코드가 아니라 현재 결제·구독·운영 정합성을 지키는 안전장치입니다.

## 승인 묶음

삭제·수정은 다음 여섯 묶음으로 나눠 승인받는 구조가 안전합니다.

### A. 안전한 내부 사체 코드

deprecated overload, inert Thymeleaf 설정, stale security matcher, 사용하지 않는 컴포넌트·프론트 wrapper·placeholder를 정리합니다.

외부 API와 DB는 건드리지 않습니다.

### B. 교차 계층 정리

play history, 구형 download queue, preview 컬럼, whitelist snapshot, playlist 호환 route, 다운로드 기록 명칭과 UI 확인창 등을 백엔드·프론트·DB·문서 단위로 함께 정리합니다.

### C. V1 DB 기준선

- `schema.sql`을 빈 DB 전용 fail-closed 기준선으로 확정
- 최소 기준 데이터의 단일 소유자 확정
- 빌링키 V1 호환을 제거하고 V2 key-ring만 유지
- acceptance/local 설정 격리
- MySQL 검증 테스트 일반화
- 증명 완료 후 수동 보정 SQL 9개 퇴역

이 묶음은 disposable MySQL DB 생성·DDL·삭제가 포함되므로 실행 직전에 다시 승인이 필요합니다.

### D. 문서·도구·생성물

활성 문서 14개를 현재 V1 상태로 고치고, 완료 설계 2개는 제자리 archive 처리합니다. 생성된 demo seed, PDF 임시 렌더, 펼쳐진 캡처, attachment 복사본을 정리하고 `tsconfig.tsbuildinfo` 추적을 종료합니다.

역사 REQ/WI/SR/audit 문서는 그대로 보존합니다.

### E. 브랜치·worktree 통합

- 끊어진 worktree 등록과 대응 브랜치 35개
- 병합된 일반 로컬 브랜치 3개
- 공식 서버 전환 후 acceptance/client-demo worktree와 브랜치 2개

를 정리합니다. 개발 기준 태그 2개와 공식 후보 브랜치는 보존합니다.

### F. 아직 결정하면 안 되는 항목

외부 호출·운영 정책·로컬 설정·독자 커밋 근거가 부족한 12개 항목입니다. 결정 전까지 유지합니다.

## 사용자 결정이 필요한 9가지

1. 구형 단건 결제 API·callback과 직접 구독 생성 API를 telemetry 없이 제거할지, 관찰 기간을 둘지
2. 호출자가 보이지 않는 `subscription-status`, `user-type`, 관리자 구독 상세 API를 제거할지
3. acceptance QA 계정 bootstrap을 애플리케이션 내부에 유지할지, 외부 provisioning으로 바꿀지
4. V1 결제 provider 체계를 Toss 정기결제 기준으로 어떻게 단일화할지
5. 비추적 `application-local.yml`을 비밀값을 노출하지 않고 교체·격리하도록 승인할지
6. 프론트 패키지 버전을 `1.0.0`으로 올릴지, private 패키지라 `0.1.0`을 유지할지
7. 독자 커밋이 있는 로컬 브랜치 3개의 tip을 보존할지
8. 현재 서버 로그를 공식 런타임 전환 후 삭제할지
9. 관리자 직접 구독 수정·취소를 비상 운영 기능으로 유지할지, 별도 감사 workflow를 설계할지

## 추천 승인 순서

1. **A 승인·구현·검증**
2. F의 API/provider/로컬 설정 결정을 먼저 확정
3. **B와 C를 설계상 연결하되 커밋과 검증은 영역별 분리**
4. **D로 문서와 생성물 현행화**
5. 개발 브랜치 인수테스트
6. 공식 런타임 전환 확인 후 **E 실행**
7. 최종 독립 전수검수 후 V1 기준선 종료

## 명시적 정지 지점

이번 WI에서는 판정표만 만들었습니다.

- 코드 삭제·수정 안 함
- SQL·DB 실행 안 함
- 기존 문서 수정 안 함
- 브랜치·worktree·tag 조작 안 함
- 파일 정리 안 함
- 서버 시작·중지·재시작 안 함
- stage·commit·push 안 함

상세 파일·심볼·근거·검증 명령과 101개 원천 판정 매핑은 `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md`에 있습니다. 다음 단계는 이 판정표에 대한 사용자 승인입니다.
