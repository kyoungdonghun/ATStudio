# WI-20260714-ATS-012 완료 요약

## 결과

파일과 DB 트랜잭션의 생명주기를 맞추는 storage abstraction/coordinator와 `storage_mutations` journal/recovery를 구현했습니다. 신규 파일은 rollback 시 정리되고, 교체/삭제 대상의 기존 파일은 DB commit 이후 live reference 확인을 거쳐 정리되며, 실패는 journal에 남아 제한된 재시도와 복구 대상이 됩니다.

Track, Playlist, Album, Company Certification, Notice, Question의 파일 변경 경로와 Download의 load 계약을 새 storage 계약으로 전환했습니다. WI-009 이미지 진위성과 WI-010 인증 문서 형식 진위성/격리는 구현하지 않았고, 후속 작업이 연결할 typed root/coordinator 훅만 마련했습니다.

## 생명주기 매트릭스

| 도메인 | 생성/교체 | 삭제 |
|---|---|---|
| Track | audio/thumbnail rollback 정리, 교체 파일 commit 후 정리 | soft delete 원본 파일 보존 |
| Playlist | thumbnail rollback 정리, 이전 파일 commit 후 정리 | soft delete commit 후 정리 |
| Album | thumbnail rollback 정리, 이전 파일 commit 후 정리 | soft delete commit 후 정리 |
| Company Certification | 문서 일괄 rollback 정리, resubmit 이전 문서 commit 후 정리 | 현재 PUBLIC 유지; PRIVATE quarantine는 WI-010 |
| Notice | 첨부 rollback 정리, 제거 첨부 commit 후 정리 | hard delete commit 후 정리 |
| Question | 첨부 rollback 정리 | hard delete commit 후 정리 |

## 스키마

- Fresh DDL: `src/main/resources/schema.sql`
- 별도 수동 patch: `src/main/resources/db/manual/20260714_storage_mutations_journal.sql`
- DDL 적용, DB 생성/삭제, legacy migration은 수행하지 않았습니다.
- WI-004 payment schema 변경은 유지했습니다.

## 검증

- `compileJava`: PASS
- `compileTestJava`: PASS
- storage focused 4개 클래스: MA 재실행 PASS, 16 tests, failure 0, 이전 출력 기준 1 skipped
- 기존 6개 도메인 서비스 테스트: MA 재실행 전체 PASS
- storage/schema 및 7개 관련 서비스 결합 focused 실행: PASS
- symlink 테스트 1건은 Windows 권한 제약으로 skip됐습니다.
- 전체 backend 1차 실행은 846 tests 진행 중 동시 Gradle 실행이 같은 결과 디렉터리를 사용해 결과 파일 경합이 발생했습니다. focused 범위의 실패는 없습니다.

## 위험 및 다음 체인

- MySQL 8 실행 검증과 DDL rehearsal은 WI-021 및 별도 승인 대상입니다.
- recovery는 단일 서버 scheduler 기준이며 다중 서버 lock은 포함하지 않습니다.
- 인증 문서는 WI-010 전까지 PUBLIC이므로 quarantine 완료 상태가 아닙니다.
- WI-012 완료로 WI-009와 WI-010의 storage 선행 조건이 해제됐습니다. WI-009는 이미지 검증/정규화를 write 훅 앞에, WI-010은 문서 진위성 검증과 PRIVATE quarantine를 typed root 훅에 연결해야 합니다.

상세 재현 근거는 `deliverables/agent/WI-20260714-ATS-012-evidence-pack.md`에 기록했습니다.
