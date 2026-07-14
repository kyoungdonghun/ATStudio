# WI-20260714-ATS-035 완료 요약

## 결과
- `Track.waveformData`와 신규 DB 스키마의 계약을 nullable `TEXT`로 일치시켰습니다.
- 기존 DB용 수동 패치를 별도 파일로 추가했으며, Spring Boot가 자동 실행하지 않습니다.
- 기존 disposable MySQL 리허설 증거에서 Hibernate 검증 성공과 DB 정리 완료를 확인했습니다.
- 기존 DB에는 접속하거나 DDL을 적용하지 않았습니다.

## 검증 근거
- 신선 스키마: `src/main/resources/schema.sql`
  - `tracks.waveform_data TEXT NULL`이 `duration` 다음, `user_id` 앞에 존재합니다.
- 기존 DB 수동 패치: `src/main/resources/db/manual/20260715_track_waveform_data.sql`
  - `tracks` 테이블 존재 여부를 먼저 확인합니다.
  - 컬럼이 없을 때만 nullable `TEXT`로 추가합니다.
  - 기존 컬럼 계약이 다르면 강제 변환하지 않고 중단합니다.
  - 별도 운영자 승인 없이는 실행하면 안 됩니다.
- 집중 계약 테스트 XML:
  - `build/test-results/test/TEST-com.atstudio.atstudio.entity.TrackWaveformSchemaContractTest.xml`
  - `01:22:02` 기준 3건 실행, 실패 0건, 오류 0건입니다.
- Hibernate 로그: `deliverables/agent/WI-20260714-ATS-035/hibernate-validate.log`
  - `Started AtStudioApplication` 성공 표지가 있습니다.
  - `missing column [waveform_data]` 또는 스키마 검증 실패 표지가 없습니다.
  - JDBC URL은 전체 값 대신 `[REDACTED_JDBC_URL; database=...]`로 저장됐습니다.
- 정리 로그: `deliverables/agent/WI-20260714-ATS-035/drop-after-hibernate-validate.log`
  - `drop.database: OK`
  - `cleanup.database.exists: 0`
  - `RESULT: PASS`

## DB 및 비밀정보 안전성
- 검증 대상은 `ats_wi021_20260715_w035a7b9` disposable DB뿐입니다.
- 로그상 애플리케이션 DB와 disposable DB가 다르며, 애플리케이션 DB 이름은 마스킹돼 있습니다.
- 기존 DB용 수동 패치는 이번 WI에서 실행하지 않았습니다.
- 검토한 산출물에는 완전한 JDBC URL, 비밀번호, 토큰, 정확한 credential 값이 없습니다.
- WI-021 및 WI-035 산출물 경로에 컴파일된 리허설 helper `.class` 파일이 없습니다.

## 변경 파일
- `src/main/resources/schema.sql`
- `src/main/resources/db/manual/20260715_track_waveform_data.sql`
- `src/test/java/com/atstudio/atstudio/entity/TrackWaveformSchemaContractTest.java`
- `deliverables/agent/WI-20260714-ATS-035/rehearsal-jdbc-validate-db.log`
- `deliverables/agent/WI-20260714-ATS-035/hibernate-validate.log`
- `deliverables/agent/WI-20260714-ATS-035/drop-after-hibernate-validate.log`
- `deliverables/agent/WI-20260714-ATS-035-evidence-pack.md`
- `deliverables/user/WI-20260714-ATS-035-summary.md`

## 참고
- 사용자의 중단 지시 후 Gradle, bootRun, DB 또는 추가 셸 명령을 실행하지 않았습니다.
- `git diff --check`는 중단 지시 후 재실행하지 않았으며, 이번 인수 작업에서 통과했다고 주장하지 않습니다.
- `schema.sql`에 함께 존재하는 다른 WI의 동시 변경은 수정하거나 되돌리지 않았습니다.
