# WI-20260724-ATS-004 완료 요약

## 결과

현재 공유 작업 트리의 백엔드, acceptance 실행 계약, 데모 시드 도구 계약을
제품 코드 수정 없이 검증했습니다. WI-004의 모든 필수 gate가 통과했습니다.

- 백엔드 전체 테스트: 1,208개 중 1,199개 통과, 실패 0, 오류 0, 건너뜀 9
- JaCoCo 임계값: 라인 85.726%, 메서드 82.931%, 브랜치 71.682%로 통과
- 중요 보안 클래스 7개: 라인 및 메서드 커버리지 100%
- 백엔드 빌드와 실행 JAR 생성: 통과
- Acceptance backend 환경 계약: 9개 점검 통과
- Acceptance dry-run 안전 계약: 10개 점검 통과
- 데모 시드 집중 계약: 14개 점검 통과

검증 기준은 브랜치 `codex/p1-acceptance-hardening`, 커밋
`4b00e99f2293e290d92b1fc56412a90743588c80` 위에 WI-001~003의 미커밋
변경이 함께 존재하는 최종 공유 작업 트리입니다.

## 건너뛴 테스트

건너뜀 9개는 실패가 아니라 실행 환경 조건에 따른 것입니다.

- 7개: 일회성 MySQL 동시성 증명 테스트
- 1개: MySQL 스키마 검증 테스트
- 1개: 현재 Windows 환경에서 symbolic link를 만들 수 없어 중단된 로컬
  스토리지 테스트

앞의 MySQL 테스트 8개는 `ATSTUDIO_MYSQL_PROOF_ENABLED=true`인 별도 폐기형
MySQL 증명 환경에서만 실행됩니다. 이번 WI는 DB 변경을 금지하므로 해당
조건을 활성화하지 않았습니다.

## 경고 및 비차단 사항

- Gradle Problems Report에는 `OAuth2ServiceTest.java`의 unchecked/unsafe
  test compilation 관련 ADVICE 2건이 기록됐습니다. 경고나 오류 등급은
  없었습니다.
- 테스트 JVM은 bootstrap classpath 추가로 CDS sharing이 제한된다는 경고
  1건을 출력했습니다. 테스트 결과에는 영향을 주지 않았습니다.
- `PSScriptAnalyzer`가 설치되지 않아 선택적 정적 분석은
  `not-installed`로 기록됐습니다. PowerShell parser와 10개 안전 계약은
  모두 통과했습니다.
- `--warning-mode all` 실행에서 Gradle deprecation 경고는 없었습니다.

## 안전성

- 외부 credentials 또는 secret bundle을 읽거나 출력하지 않았습니다.
- 실제 결제 Provider, API 서버, DB, 스토리지에 연결하거나 데이터를
  변경하지 않았습니다.
- 실제 데모 seed/cleanup은 실행하지 않았습니다.
- 제품 소스는 수정하지 않았고 WI-004 요약과 Evidence Pack만 작성했습니다.

## 다음 상태

WI-20260724-ATS-004는 PASS입니다. 이 결과는 프론트엔드 WI-005와 문서
WI-006 결과가 완료된 뒤 WI-007 V1 최종 감사로 전달할 수 있습니다.
