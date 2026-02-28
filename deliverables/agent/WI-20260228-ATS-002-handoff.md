[WI HEADER]
WI ID: WI-20260228-ATS-002
REQ: REQ-20260228-ATS-010
Agent: se
Depends On: -
Blocks: WI-20260228-ATS-004

[WI SUMMARY]
Why: CR-C-001 (CRITICAL) — Question 삭제 시 연관 Answer / QuestionAttachment cascade 처리 누락.
     현재 QuestionService.deleteQuestion()은 question만 삭제하고 자식 레코드를 남겨둠.
     → DB FK 제약으로 DataIntegrityViolationException 발생 → 500 응답.
     서비스 레이어에서 자식 먼저 삭제하는 방식으로 수정 (DB 스키마 변경 없음).
Scope (in):
  - QuestionService.java: deleteQuestion() 내 Answer, QuestionAttachment 명시 삭제 추가
  - Question.java entity: JPA orphanRemoval/cascade 옵션 검토 및 필요 시 추가
  - Answer.java entity: cascade 연관 관계 확인
  - QuestionAttachment.java entity: 확인
  - QuestionServiceTest.java: cascade 삭제 시나리오 단위 테스트 추가
Scope (out):
  - DB 스키마 ON DELETE CASCADE 추가: 금지 (서비스 레이어 처리로 대체)
  - Answer 삭제 API 기능 변경
  - 다른 도메인 코드 수정
DoD:
  - Question 삭제 시 연관 Answer, QuestionAttachment 모두 삭제됨
  - DataIntegrityViolationException 발생 없음
  - 단위 테스트: cascade 삭제 케이스 추가 (Answer, Attachment 삭제 verify)
  - 기존 테스트 포함 0 failures
Constraints/Forbidden:
  - DB 스키마(migration) 파일 생성/변경 금지
  - 삭제 순서 주의: Attachment → Answer → Question 순으로 삭제 (FK 방향 확인 필수)
  - 다른 WI 범위 파일 수정 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] QuestionService.deleteQuestion() — answer/attachment 삭제 후 question 삭제
  - [ ] FK 제약 위반 없이 정상 삭제 완료
  - [ ] Answer가 없는 Question도 정상 삭제됨 (null-safe)
  - [ ] Attachment가 없는 Question도 정상 삭제됨 (null-safe)
Quality:
  - [ ] QuestionServiceTest: Answer 포함 Question 삭제 테스트 추가
  - [ ] QuestionServiceTest: Attachment 포함 Question 삭제 테스트 추가
  - [ ] 기존 테스트 전체 통과 (no regressions)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards — se):
  - docs/standards/development-standards.md

REQ:
  - deliverables/user/REQ-20260228-ATS-010.md

감사 근거 (이슈 출처):
  - docs/audit/backend-audit-report.md  ← CR-C-001 (CRITICAL)
  - deliverables/user/WI-20260227-ATS-031-summary.md  ← CR-C-001 상세 (cr-C 발견)

수정 대상 파일:
  - src/main/java/com/atstudio/atstudio/service/inquiry/QuestionService.java   ← 주 수정 대상
  - src/main/java/com/atstudio/atstudio/entity/Question.java                   ← cascade 옵션 확인
  - src/main/java/com/atstudio/atstudio/entity/Answer.java                     ← 연관 관계 확인
  - src/main/java/com/atstudio/atstudio/entity/QuestionAttachment.java         ← 확인
  - src/main/java/com/atstudio/atstudio/repository/AnswerRepository.java       ← deleteAllByQuestion() 확인
  - src/main/java/com/atstudio/atstudio/repository/QuestionAttachmentRepository.java
  - src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java

DB 스키마 참조 (FK 방향 확인):
  - docs/design/db-schema.md  ← answers, question_attachments 테이블 FK 정의

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260228-ATS-002-summary.md :
  - 수정 완료 확인, cascade 삭제 동작 방식, 테스트 결과

Agent-facing → deliverables/agent/WI-20260228-ATS-002-evidence-pack.md :
  - 수정된 파일:라인 목록
  - 삭제 순서 (Attachment → Answer → Question) 근거
  - 추가 테스트 케이스 목록
  - `./gradlew test` 결과 요약

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일명·라인번호 필수. deleteQuestion() 변경 전후 로직 스니펫 포함.
Tests: cascade 삭제 시나리오 테스트 메서드명 + verify 내용 포함
Rollback: git revert
