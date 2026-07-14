# Evidence Pack: WI-20260714-ATS-012

## 요약

- 트랜잭션과 파일 시스템 변경을 조정하는 typed storage abstraction, `StorageMutationCoordinator`, 내구성 있는 `storage_mutations` journal/recovery를 구현했다.
- Track, Playlist, Album, Company Certification, Notice, Question의 파일 생성/교체/삭제 경로를 coordinator로 통합하고 Download의 typed load 계약을 반영했다.
- WI-009 이미지 진위성 및 WI-010 인증 문서 형식 진위성/격리는 구현하지 않았으며, typed root와 coordinator 호출 지점만 후속 통합 훅으로 남겼다.

## 범위 및 DoD

- [x] PUBLIC/PRIVATE root, 생성 키, staging, atomic promote, strict load/delete 계약
- [x] rollback 시 신규/임시 파일 정리, commit 후 기존 파일 정리
- [x] 정리 실패 journal 기록, bounded retry, startup/scheduled recovery
- [x] 참조 확인 후 삭제 및 shared reference 보존
- [x] fresh DDL과 별도 수동 patch 작성, DB 미적용
- [x] 관련 서비스와 테스트의 `StorageService` 계약 변경 완결
- [x] WI-004 payment schema 변경 보존
- [x] focused tests 및 Java/test compilation 검증
- [ ] MySQL 8 실행 검증과 DDL 적용: WI-021 및 별도 승인 범위

## 참조 문서

| 구분 | 경로 | 사용 근거 |
|---|---|---|
| WI | `deliverables/agent/WI-20260714-ATS-012-handoff.md` | 범위, DoD, 차단 관계, 금지 사항 |
| 승인 설계 | `docs/design/p1-security-acceptance-hardening-design.md` Section 6 | journal 상태, ordering, recovery 정책 |
| 선행 Evidence | `deliverables/agent/WI-20260714-ATS-004-evidence-pack.md` | payment DDL 보존 기준 |
| Fresh schema | `src/main/resources/schema.sql` | 신규 환경용 `storage_mutations` DDL |
| Manual patch | `src/main/resources/db/manual/20260714_storage_mutations_journal.sql` | 기존 DB용 별도 승인 patch |

## 구현 근거 포인터

- `src/main/java/com/atstudio/atstudio/service/storage/StorageService.java:6` - typed root 기반 저수준 저장소 계약
- `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java:25` - 분리 root, 생성 키, staging/atomic promote, strict path/symlink/regular-file 검사
- `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java:18` - 트랜잭션 동기화 기반 mutation 조정
- `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java:162` - commit/rollback callback과 익명 메서드 이름 충돌 수정
- `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java:17` - startup/scheduled bounded recovery
- `src/main/java/com/atstudio/atstudio/service/storage/StorageReferenceChecker.java:11` - live DB reference 확인
- `src/main/java/com/atstudio/atstudio/entity/StorageMutation.java:35` - journal JPA 모델
- `src/main/resources/schema.sql:1026` - fresh `storage_mutations` DDL
- `src/main/resources/db/manual/20260714_storage_mutations_journal.sql:6` - 별도 수동 patch
- `src/test/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinatorTest.java:126` - 다중 stage 중간 실패 journal/정리 검증
- `src/test/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinatorTest.java:184` - 트랜잭션 동기화 없는 mutation 거부 검증

## 파일 생명주기 매트릭스

| 도메인 | 생성 | 교체 | 삭제 | 의도적 예외/후속 훅 |
|---|---|---|---|---|
| Track | audio/thumbnail 일괄 stage, rollback 신규 정리 | commit 후 교체된 audio/thumbnail 정리 | soft delete 시 원본 파일 보존 | 물리적 source-audio relocation은 범위 밖 |
| Playlist | thumbnail stage, rollback 신규 정리 | commit 후 이전 thumbnail 정리 | soft delete commit 후 thumbnail 정리 | WI-009가 canonical image 검사/변환을 coordinator 앞에 연결 |
| Album | thumbnail stage, rollback 신규 정리 | commit 후 이전 thumbnail 정리 | soft delete commit 후 thumbnail 정리 | 이미지 authenticity는 WI-009 범위 |
| Company Certification | 문서 일괄 stage, rollback 신규 정리 | resubmit commit 후 이전 문서 정리 | 거절/취소 흐름에서 참조 해제 후 정리 | 현재 PUBLIC root 유지; PRIVATE quarantine와 형식 진위성은 WI-010 |
| Notice | 첨부 일괄 stage, rollback 신규 정리 | 제거된 첨부만 commit 후 정리 | hard delete commit 후 첨부 정리 | 콘텐츠 authenticity 변경 없음 |
| Question | 첨부 일괄 stage, rollback 신규 정리 | 해당 없음 | hard delete commit 후 첨부 정리 | 콘텐츠 authenticity 변경 없음 |

## 스키마 증거

- `schema.sql`에 `operation_id`, domain/type/root, new/old key, 상태, attempt/next-attempt/reason/timestamp와 조회 인덱스를 포함한 fresh DDL을 추가했다.
- 수동 patch는 `CREATE TABLE IF NOT EXISTS`와 정적 확인 query만 포함하며, legacy data migration이나 삭제를 포함하지 않는다.
- `schema.sql:485` 이후의 WI-004 payment order/finalization 제약 및 `schema.sql:808` 이후 audit ENUM 변경을 유지했다.
- DDL은 실행하지 않았고 DB를 생성/삭제하거나 연결하지 않았다.

## 검증 결과

| 명령/검증 | 결과 |
|---|---|
| `gradlew.bat compileJava` | PASS, `BUILD SUCCESSFUL`; 초기 `afterCommit` 이름 충돌 수정 후 재검증 |
| `gradlew.bat compileTestJava` | PASS, `BUILD SUCCESSFUL`; 기존 storage 호출부를 새 계약으로 전환 후 재검증 |
| storage focused 4개 클래스 전체 | MA 재실행 PASS, 16 tests, failure 0, 이전 출력 기준 1 skipped |
| 기존 6개 도메인 서비스 테스트 | MA 재실행 전체 PASS |
| `LocalStorageServiceTest`, `StorageMutationCoordinatorTest`, `StorageMutationRecoveryServiceTest`, `StorageCleanupServiceTest`, `StorageMutationContractTest` | 재실행 PASS, `BUILD SUCCESSFUL` |
| 7개 관련 서비스 테스트와 focused storage/schema 결합 실행 | PASS, `BUILD SUCCESSFUL` |
| `SubscriptionControllerTest` 개별 재실행 | PASS, `BUILD SUCCESSFUL` |
| 전체 `gradlew.bat test` 1차 | 미완료; 846 tests 진행 중 21 context failures 후 다른 동시 Gradle 실행과 `build/test-results` 결과 파일 경합 관찰 |

초기 focused red 2건은 다음과 같이 수정했다.

- UUID generated key를 실제 matcher로 받도록 stage stubbing을 변경해 Mockito `PotentialStubbingProblem`을 제거했다.
- teardown에서 `TransactionSynchronizationManager.isSynchronizationActive()`를 확인한 뒤에만 synchronization을 정리하도록 변경했다.

Windows에서 symlink 생성 권한이 없어 symlink 케이스 1건은 assumption skip이다. traversal, root escape, private URL 거부, non-regular file 및 cleanup/retry 계약은 실행됐다.

## 변경 범위 및 diff 확인

- WI-012 소유 파일은 storage package, journal entity/repository, 6개 도메인 서비스와 Download typed-load adaptation, application storage 설정, schema/manual patch, 관련 focused/domain tests, 본 Evidence/요약이다.
- 작업 트리에 WI-001/002/003/004/008/011/013/014/015 및 frontend/security/payment 동시 변경이 존재한다. 해당 변경과 runtime log 파일은 수정하거나 되돌리지 않았다.
- `schema.sql`은 WI-004 payment 변경 위에 additive하게 작성했으며 payment 구간을 교체하거나 축소하지 않았다.

## 위험 및 롤백

- 수동 SQL은 정적으로만 검증했다. MySQL 8 실행, Hibernate validation, disposable DB rehearsal은 WI-021과 별도 승인 전까지 미검증이다.
- recovery는 승인 설계대로 단일 서버 scheduler 기준이다. 다중 서버 scheduler lock은 범위 밖이다.
- 인증 문서는 WI-010 전까지 PUBLIC root를 사용한다. 현 상태를 quarantine 보안 완료로 간주하면 안 된다.
- 기존 legacy key/data는 migration하지 않았다.
- 롤백은 애플리케이션을 먼저 되돌린다. 별도 승인으로 journal DDL이 적용된 환경에서는 pending journal row와 table을 보존하고 파일을 일괄 삭제하지 않는다.

## WI 체인 트리거

- WI-012 완료로 handoff의 차단 관계상 WI-009, WI-010, WI-019, WI-021, WI-024, WI-025가 해제된다.
- WI-009 트리거: Playlist/Album 이미지 입력을 검증/정규화한 뒤 현재 coordinator write/replace 훅으로 전달한다. 본 WI는 이미지 진위성이나 canonical JPEG를 구현하지 않았다.
- WI-010 트리거: 인증 문서 signature/format 검증과 PRIVATE quarantine root를 현재 typed root/coordinator 훅에 연결한다. 본 WI는 형식 진위성, quarantine, attachment-only 응답을 구현하지 않았다.

## 완료 판정

- WI-012 구현 및 focused 검증은 완료했다.
- DB 적용과 authenticity 정책은 명시된 후속 WI로 남는다.
