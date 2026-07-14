# WI-20260714-ATS-019 검증 요약

## 결과

WI-019 독립 검증 범위에서 재현된 결함은 없었습니다. production code는 수정하지 않았고, fixture 기반 focused verification test만 추가해 이미지, 기업문서, storage rollback/recovery, refresh session replay, CSV neutralization 경계를 다시 검증했습니다.

- `/uploads/company-docs/**` 정적 경로는 비인증, USER, ADMIN 모두 공개 접근이 차단됐습니다.
- 기업문서 다운로드는 `attachment`, `application/octet-stream`, `no-store`, `nosniff`, `sandbox`, `Accept-Ranges: none` 헤더 계약을 유지했고, `Range` 요청에도 부분 공개로 내려가지 않았습니다.
- PNG polyglot payload는 기업문서 경계에서 canonical JPEG로 재인코딩되어 private storage로만 전달됐고, forged PDF extension/bytes 조합과 truncated PNG는 DB mutation 전에 거부됐습니다.
- storage recovery는 stale `REPLACE` 상태에서 live DB reference가 없을 때 새 파일만 cleanup하고 old key는 건드리지 않는 rollback/recovery 경계를 유지했습니다.
- refresh session은 회전 후 stale token 재사용과 logout 이후 replay가 모두 거부됐고, 최신 session hash/null 상태가 보존됐습니다.
- CSV neutralization, 기존 password change/reset refresh termination, shared-reference cleanup 계약은 focused rerun으로 다시 확인했습니다.

## 이번 WI에서 추가한 검증 파일

- `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java`
- `src/test/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryVerificationTest.java`

## 실행한 검증

- `.\gradlew.bat compileTestJava` PASS
- WI-019 focused classes only:
  - `CompanyCertificationControllerTest`
  - `CompanyCertificationServiceTest`
  - `CompanyCertificationSecurityVerificationTest`
  - `CanonicalImageServiceTest`
  - `StorageMutationCoordinatorTest`
  - `StorageMutationRecoveryServiceTest`
  - `StorageMutationRecoveryVerificationTest`
  - `AuthServiceTest`
  - `UserServiceTest`
  - `EmailServiceTest`
  - `AdminWhitelistChannelServiceTest`
  - `UserRepositoryTest`
- `git diff --check -- <WI-019 owned test files>` PASS

## 메모

- 사용자 지시대로 전체 `gradlew test`는 실행하지 않았습니다. Phase 7 단일 실행으로 남겨 두었습니다.
- shared `build/test-results` 경합 가능성이 있어도 이번 WI 증거는 focused rerun console success 기준으로만 기록했습니다.
