# WI-20260716-ATS-036 DB/설정 기준선 조사 요약

## 작업 결과

승인된 `REQ-20260716-ATS-004`와 WI 핸드오프에 따라 DB 생성·보정 경로, 9개 수동 SQL, JPA 엔티티, seed/bootstrap, application profile, acceptance 실행 경로를 읽기 전용으로 조사했다.

이번 WI에서는 SQL 실행, MySQL 접속, 서버 기동, 코드·스키마·설정 수정, 파일 삭제, Git 상태 변경을 하지 않았다. 아래 내용은 다음 통합 설계 WI에서 검토할 분류안이며, 즉시 삭제 또는 변경하라는 실행 결과가 아니다.

## 권고 기준선

V1의 DB 기준은 다음 한 경로로 정리하는 것이 적절하다.

1. 비어 있는 MySQL 8 DB에 단일 `src/main/resources/schema.sql`을 적용한다.
2. 최소 필수 기준 데이터만 별도 baseline data 파일에서 소유한다.
3. 서버는 `ddl-auto=validate`와 SQL 자동 초기화 비활성 상태로 기동한다.
4. acceptance/demo 데이터는 운영 기준선과 분리하고 명시적으로 켠 경우에만 생성한다.
5. 기존 DB 보존 요구가 없으므로 9개 수동 보정 SQL은 fresh-DB 증명 완료 후 활성 소스에서 퇴역시키고 Git 이력을 보존한다.

현재 `schema.sql`의 41개 테이블 이름과 41개 JPA 엔티티의 테이블 이름은 정확히 일치한다. 다만 이것만으로 컬럼 타입, 기본값, ENUM, FK 삭제 규칙, 인덱스 순서까지 완전 일치한다고 증명되지는 않는다.

## 분류 결과

분류 단위는 파일 수가 아니라 독립적으로 결정해야 하는 파일 또는 설정·호환 심볼이다.

| 분류 | 수 | 핵심 내용 |
|---|---:|---|
| KEEP | 5 | 기본 application/test 설정 경로, DB 의존성, acceptance 보안 구성, 명시적 비운영 QA 계정 bootstrap |
| REMOVE | 12 | 수동 SQL 9개, 미리듣기 파일 호환 필드, 화이트리스트 구형 snapshot 필드, 빌링키 v1 복호화 호환 |
| REPLACE | 8 | fresh schema, 혼합 seed, acceptance/local example profile, startup guard, 구독 플랜 중복 bootstrap, acceptance lifecycle, WI 전용 MySQL 증명 테스트 |
| ARCHIVE | 1 | 과거 WI의 disposable MySQL 실행 도구와 로그 |
| REVIEW | 4 | 의미가 어긋난 결제 provider selector, 로컬 비추적 설정, demo seed 도구, nullable 결제 provider 호환 |
| 합계 | 30 | 다음 WI에서 교차 영역 결과와 함께 확정할 결정 단위 |

## 주요 발견

1. **수동 SQL 9개는 모두 기존 DB 갱신용이다.** 현재 fresh schema에는 각 DDL 결과가 반영돼 있다. 데이터 보존 요구가 없는 V1에서는 이 체인을 계속 유지할 실익보다 순서·선행조건·backfill 위험이 더 크다.
2. **`schema.sql`은 기준선이지만 아직 fail-closed가 아니다.** 41개 테이블 모두 `CREATE TABLE IF NOT EXISTS`를 사용해 오래된 비어 있지 않은 DB를 조용히 허용할 수 있다. 헤더는 v13, 설계 문서는 v20이며 파일 끝의 테이블 수 38도 실제 41과 다르다.
3. **초기 데이터 소유자가 중복된다.** `seed.sql`은 필수 플랜과 데모 사용자·음원·앨범·태그·공지 및 삭제 문장을 함께 포함하고, acceptance 구독 플랜 runner도 같은 6개 플랜을 다시 만든다.
4. **로컬·acceptance 설정에 숨은 의존 가능성이 있다.** 기본 설정이 저장소 루트의 비추적 `application-local.yml`을 선택적으로 import한다. acceptance 실행도 이 import를 명시적으로 격리하지 않아 로컬 파일의 영향을 받을 수 있다.
5. **빌링키 v2 설정 전달이 불완전하다.** acceptance 환경변수 allowlist에는 구형 단일 암호화 secret은 있지만 현재 key-ring의 active key ID와 indexed key 항목은 없다.
6. **결제 provider 설정의 의미가 일치하지 않는다.** 기본값은 MOCK이지만 정기결제 서비스는 TOSS_BILLING을 직접 사용한다. 반면 startup guard는 이 selector를 조건으로 검사해 필수 설정 검증을 건너뛸 수 있다.
7. **acceptance tooling 자체는 운영 우회로가 아니다.** QA 계정 bootstrap에는 비운영 profile 및 명시적 enable 조건과 production 차단이 있으므로, 운영 기준선과 분리된 테스트 도구로 유지할 가치가 있다.

## 승인 이후 필요한 증명

퇴역이나 교체를 실행하기 전 다음 증명이 필요하다.

- 고유 이름의 disposable MySQL 8 DB가 정말 비어 있는지 확인
- 수정된 `schema.sql`만 단 한 번 적용하고 41개 엔티티 및 전체 DB 메타데이터 대조
- 최소 baseline data만 적용했을 때 정확히 6개 구독 플랜만 존재하는지 확인
- `ddl-auto=validate`, SQL 자동 초기화 비활성, bootstrap 비활성 상태의 서버 정상 기동
- acceptance profile을 별도로 켰을 때만 5개 QA 계정과 예상 fixture가 생성되는지 확인
- MySQL 전용 schema 검증과 7개 동시성 시나리오 재실행
- 수동 SQL 파일명에 대한 활성 코드·현재 문서 참조가 0인지 확인
- 제거된 legacy 컬럼과 설정 경로가 fresh DB 및 실행 설정에 존재하지 않는지 확인
- 두 번째 schema 적용은 실패하도록 해 기준선이 migration처럼 동작하지 않음을 확인
- 증명 완료 후 disposable DB 삭제

이 절차의 DB 생성·삭제, DDL 적용, 파일 삭제 및 설정 변경은 모두 별도 승인 이후 작업이다.

## 조사 한계

- MySQL에 연결하지 않았고 실제 로컬·공유·운영 DB의 스키마나 데이터는 확인하지 않았다.
- SQL, Hibernate validate, 테스트, 빌드, 서버 및 acceptance lifecycle을 실행하지 않았다.
- 비추적 `application-local.yml`은 키 구조와 비민감 모드 플래그만 확인했으며 secret 값은 읽거나 기록하지 않았다.
- 41개 테이블/엔티티 이름과 9개 수동 SQL 파일은 전수 확인했다. 컬럼·기본값·ENUM·인덱스·FK·CHECK의 완전한 실행 정합성은 정적 조사만으로 확정할 수 없어 fresh-DB 증명 계획으로 남겼다.
- DB/config와 직접 연결된 consumer는 조사했지만 제품 코드 전체와 프론트엔드 전체는 이 WI의 전수 범위가 아니다. 각각 WI-034, WI-035 및 후속 통합 WI가 담당한다.

상세 근거, 파일별 분류, rollback 출처와 재현 명령은 `deliverables/agent/WI-20260716-ATS-036-evidence-pack.md`에 기록했다.
