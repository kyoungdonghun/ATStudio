# WI-20260714-ATS-037 완료 요약

## 결과

- Question 신규 첨부파일의 생성, 권한 확인 후 조회, Question 삭제 후 정리를 모두 `StorageRoot.PRIVATE`로 통일했습니다.
- 기존 공개 경로인 `/uploads/questions/**`는 익명, USER, ADMIN 요청 모두 명시적으로 차단됩니다.
- 인가된 API 다운로드는 항상 `application/octet-stream`과 `attachment`로 응답하며 `no-store`, `nosniff`, sandbox CSP, `Accept-Ranges: none`을 적용합니다.
- 원래 파일명은 UTF-8 percent-encoding 후 헤더에 사용하므로 CR/LF 또는 콜론으로 응답 헤더를 삽입할 수 없습니다.
- Question 응답 DTO는 저장 경로를 노출하지 않으며, 기존 공개/비공개 Question의 열람 정책은 변경하지 않았습니다.
- QA bootstrap 로그에서 계정 이메일을 제거하고 fixture 수 또는 제한된 reason code만 기록하도록 변경했습니다.

## 접근 동작

| 요청 | 결과 |
|---|---|
| 익명 사용자의 Question API 첨부 다운로드 | `401` |
| 인증 사용자의 공개 Question 첨부 다운로드 | 기존 정책대로 허용 |
| 비공개 Question 소유자 또는 ADMIN 다운로드 | 기존 정책대로 허용 |
| 비공개 Question의 다른 USER 다운로드 | 기존 정책대로 거부 |
| `/uploads/questions/**` 직접 접근 | 역할과 무관하게 거부 |

## Legacy 처리

- 기존 파일을 이동, 삭제, 변환 또는 백필하지 않았습니다.
- 기존 PUBLIC 저장 파일은 정적 경로에서 차단되고 새 API 경계는 PRIVATE만 조회하므로 fail closed 상태입니다.
- legacy 첨부파일을 다시 제공하려면 별도 승인된 마이그레이션이 필요합니다.

## 검증

- 집중 테스트: `QuestionServiceTest`, `QuestionControllerTest`, `TestUserBootstrapRunnerTest`
- 결과: 51개 통과, 실패 0, 오류 0, 스킵 0 (`BUILD SUCCESSFUL`)
- 범위 diff 검사: whitespace 오류 없음. Windows LF/CRLF 정규화 경고만 확인했습니다.
- 전체 테스트 스위트는 지시대로 실행하지 않았습니다.
- DB 접근, schema 변경, 실제 파일 이동/삭제, private content 조회는 수행하지 않았습니다.

## 변경 파일

- `src/main/java/com/atstudio/atstudio/service/QuestionService.java`
- `src/main/java/com/atstudio/atstudio/controller/QuestionController.java`
- `src/main/java/com/atstudio/atstudio/dto/question/QuestionAttachmentDownload.java`
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java`
- `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java`
- `src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java`
- `src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java`
- `deliverables/user/WI-20260714-ATS-037-summary.md`
- `deliverables/agent/WI-20260714-ATS-037-evidence-pack.md`

## 잔여 위험

- 첨부파일 악성코드 검사와 파일 형식 제한은 이번 WI 범위 밖입니다. 현재 방어는 private 저장과 강제 다운로드 응답 경계입니다.
- legacy PUBLIC 파일 마이그레이션은 별도 승인 전까지 수행하지 않습니다.
