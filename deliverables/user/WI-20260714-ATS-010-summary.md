# WI-20260714-ATS-010 Summary

## 요약
- 기업 인증 문서 업로드를 PDF/JPEG/PNG baseline으로 제한하고, PDF signature/EOF 검증과 이미지 canonical JPEG 변환을 적용했습니다.
- 신규/재제출 인증 문서는 `StorageRoot.PRIVATE`에 저장되며, `/uploads/company-docs/**` 정적 경로는 차단했습니다.
- ADMIN 다운로드는 API attachment-only 경로만 사용하며, `application/octet-stream`, `no-store`, `nosniff`, sandbox CSP, `Accept-Ranges: none` 헤더를 고정했습니다.
- 인증 응답 DTO의 `documentPath`는 항상 `null`로 redaction했습니다.

## 범위 준수
- HWP/HWPX/DOC/DOCX는 이번 baseline에서 거부합니다.
- 기존 legacy row/file은 migration/delete하지 않았습니다. 기존 공개 저장 row는 새 PRIVATE 다운로드 경계에서 공개하지 않는 fail-closed 대상입니다.
- payment, acceptance/auth, storage journal, playlist 구현은 되돌리거나 덮어쓰지 않았습니다.

## 검증
- `.\gradlew.bat compileTestJava` PASS
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest"` PASS
- `.\gradlew.bat test` PASS
- `git diff --check -- <WI-010 changed files>` PASS, CRLF warning only
