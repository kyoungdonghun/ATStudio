---
version: 1.0
last_updated: 2026-08-18
project: ATS
owner: se
category: work-summary
status: complete
related_wi: WI-20260818-ATS-001
dependencies:
  - path: REQ-20260818-ATS-001.md
    reason: Approved scope and acceptance criteria
  - path: ../agent/WI-20260818-ATS-001-handoff.md
    reason: Implementation instructions and output contract
---

# WI-20260818-ATS-001 Summary

## 완료 내용

- Spring MVC의 `NoResourceFoundException`을 기존 `RESOURCE_NOT_FOUND` 오류 계약으로 분류하도록
  `GlobalExceptionHandler`를 수정했습니다. 이제 해당 예외는 HTTP 404와
  `errorCode: RESOURCE_NOT_FOUND`를 반환합니다.
- `MockMvc` 기반 회귀 테스트를 추가해 Spring 7의
  `NoResourceFoundException(HttpMethod, String, String)` 생성자와 advice 처리 경로를 검증했습니다.

## 변경 경로

- `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java`
- `src/test/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandlerTest.java`
- `deliverables/user/WI-20260818-ATS-001-summary.md`
- `deliverables/agent/WI-20260818-ATS-001-evidence-pack.md`

## 검증 결과

| 검증 | 결과 |
| --- | --- |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.common.exception.GlobalExceptionHandlerTest"` | PASS - `BUILD SUCCESSFUL in 8s`; 9 tests, 0 failures, 0 errors, 0 skipped |
| `git diff --check` | PASS - whitespace error 없음; CRLF advisory warning 2건만 출력 |

## 실행 경계

- 애플리케이션 런타임과 포트 5173/8080은 시작, 중지, 재시작하지 않았습니다.
- DB, 외부 서비스, 결제/메일 제공자, Cloudflare, 설정 및 시크릿은 접근하거나 변경하지 않았습니다.
