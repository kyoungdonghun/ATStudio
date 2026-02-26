[WI HEADER]
WI ID: WI-20260226-ATS-021
REQ: REQ-20260221-ATS-005
Agent: se
Depends On: -
Blocks: WI-20260226-ATS-022

---

[WI SUMMARY]
Why: Inquiry(문의/답변) 도메인 7개 API 전체 구현 — 8.1~8.7. CS 핵심 기능. Question/Answer/QuestionAttachment 엔티티 + StorageService 인프라 이미 완비 → Repository/Service/Controller/DTO + 테스트 작성에 집중.

Scope (in):
- 8.1 POST /api/questions — 문의 등록 (multipart: title, content, category, isPublic, attachments?)
- 8.2 POST /api/questions/{id}/answers — 답변 작성 (문의자 or ADMIN, OPEN→IN_PROGRESS 자동 전환)
- 8.3 GET /api/questions — 문의 목록 (일반: 공개+내것, ADMIN: 전체, category/status/mine 필터)
- 8.4 GET /api/questions/{id} — 문의 상세 (비공개: 소유자+ADMIN만, answers 포함)
- 8.5 GET /api/questions/{id}/attachments/{attachmentId} — 첨부파일 다운로드 (stream)
- 8.6 PUT /api/questions/{id}/status — 상태 변경 (ADMIN only)
- 8.7 DELETE /api/questions/{id} — 문의 삭제 (소유자+OPEN 상태 only, 또는 ADMIN)
- QuestionRepository, AnswerRepository, QuestionAttachmentRepository 신규 작성
- QuestionService, QuestionController, dto/question/ DTO들 신규 작성
- QuestionServiceTest.java, QuestionControllerTest.java 신규 작성

Scope (out):
- 결제/구독 관련 기능
- 문의 수정 API (v3에서 제거됨, 구현 금지)

DoD:
- 7개 엔드포인트 API 명세서 응답 형식 100% 충족
- 비공개 문의(isPublic=false): 소유자+ADMIN만 조회 가능, 타인 접근 시 403
- 어드민 첫 답변 시 OPEN → IN_PROGRESS 자동 전환 (서비스 레이어)
- 삭제 제약: 소유자이고 OPEN 상태일 때만 가능, 또는 ADMIN은 항상 가능
- 첨부파일 다운로드: StorageService.loadAsResource() 사용, stream 방식
- 컴파일 오류 없음
- 테스트 코드 작성 포함 (ServiceTest + ControllerTest)

Constraints/Forbidden:
- Entity 직접 Controller 반환 금지 — DTO 변환 필수
- @Transactional(readOnly=true) 클래스 레벨 표준 준수, mutating 메서드만 @Transactional override
- 문의 수정 API(PUT /api/questions/{id}) 구현 금지 (v3에서 명시적 제거)
- 상태 전환은 반드시 Service 레이어에서 처리 (Controller에서 직접 상태 변경 금지)
- 첨부파일 저장 디렉토리: "questions/attachments" (StorageService.store(file, "questions/attachments"))
- Question 엔티티에 status 업데이트 메서드 없음 → updateStatus() 메서드 추가 필요

---

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 8.1: 문의 등록 — title(max200), content, category(enum 검증), isPublic, attachments(List<MultipartFile> 선택) 처리
- [ ] 8.1: 첨부파일 있으면 StorageService.store()로 저장 후 QuestionAttachment 엔티티 생성
- [ ] 8.2: 답변 작성 — 문의 소유자 또는 ADMIN만 가능
- [ ] 8.2: ADMIN이 첫 번째로 답변할 때 question.status OPEN → IN_PROGRESS 자동 전환
- [ ] 8.3: 일반 유저 — isPublic=true 또는 자신의 문의만 조회
- [ ] 8.3: ADMIN — 전체 조회, category/status/mine 필터 적용
- [ ] 8.4: 비공개 문의(isPublic=false) — 소유자 or ADMIN만 접근, 타인 403
- [ ] 8.4: answers 목록 포함 응답
- [ ] 8.5: 첨부파일 다운로드 — 8.4와 동일한 접근 권한 체크, stream 방식 반환
- [ ] 8.6: 상태 변경 — ADMIN only, 유효한 상태 전환만 허용 (OPEN→IN_PROGRESS→RESOLVED→CLOSED, OPEN→CLOSED)
- [ ] 8.7: 삭제 — 소유자이고 status=OPEN인 경우 또는 ADMIN
- [ ] 모든 에러 케이스 GlobalExceptionHandler 통일 처리

Quality:
- [ ] 컴파일 오류 없음 (gradlew.bat compileJava)
- [ ] QuestionServiceTest — 7개 API 핵심 로직 단위테스트 (Mockito)
- [ ] QuestionControllerTest — MockMvc 기반 컨트롤러 테스트
- [ ] 기존 323개 테스트 회귀 없음

---

[INPUT POINTERS]

Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/dto-standards.md
- docs/standards/exception-handling.md

Tier 1 (Policy - 접근제어/소유자 검증 포함):
- docs/policies/security-policy.md

Tier 2 (Design - API/DB 명세):
- docs/design/api-spec.md (Section 8: Question/Inquiry 7개 API)
- docs/design/db-schema.md (questions, answers, question_attachments 테이블)

REQ/Context:
- deliverables/user/REQ-20260221-ATS-005.md

Existing Entities (읽기 필수):
- src/main/java/com/atstudio/atstudio/entity/Question.java
  - ⚠️ status 업데이트 메서드 없음 → updateStatus(QuestionStatus) 메서드 추가 필요
- src/main/java/com/atstudio/atstudio/entity/Answer.java
- src/main/java/com/atstudio/atstudio/entity/QuestionAttachment.java
- src/main/java/com/atstudio/atstudio/entity/enums/QuestionCategory.java (DOWNLOAD, PAYMENT, COPYRIGHT, PRODUCTION, OTHER)
- src/main/java/com/atstudio/atstudio/entity/enums/QuestionStatus.java (OPEN, IN_PROGRESS, RESOLVED, CLOSED)

StorageService (첨부파일 인프라):
- src/main/java/com/atstudio/atstudio/service/storage/StorageService.java
  - store(MultipartFile, String dir): String — 저장 후 relativePath 반환
  - loadAsResource(String relativePath): Resource — 파일 다운로드용
  - delete(String relativePath): void

패턴 참조 (구현 레퍼런스):
- src/main/java/com/atstudio/atstudio/controller/TrackController.java (다운로드/stream 패턴 참조)
- src/main/java/com/atstudio/atstudio/service/TrackService.java (Specification 필터 패턴 참조)
- src/main/java/com/atstudio/atstudio/service/NoticeService.java (목록/상세 서비스 패턴 참조)

기존 테스트 참조:
- src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java

---

[KEY IMPLEMENTATION NOTES]

1. **Question 엔티티 수정 필요**: updateStatus(QuestionStatus) 메서드 추가
   ```java
   public void updateStatus(QuestionStatus status) {
       this.status = status;
   }
   ```

2. **첫 답변 자동 상태 전환 로직**:
   - 8.2 답변 작성 시: 작성자가 ADMIN이고 question.status == OPEN이면 → IN_PROGRESS 전환
   - AnswerRepository.countByQuestion(question) == 0 체크 후 전환 (첫 답변 판단)

3. **비공개 문의 접근 제어**:
   - isPublic=false 문의: currentUser.id == question.user.id OR currentUser.role == ADMIN
   - 위반 시 403 Forbidden

4. **문의 목록(8.3) 필터 전략**:
   - Specification 패턴 권장 (기존 TrackService 참조)
   - 일반 유저: WHERE (is_public=true OR user_id=currentUserId)
   - ADMIN: 필터 없음

5. **첨부파일 다운로드(8.5)**:
   - Resource = storageService.loadAsResource(attachment.getFilePath())
   - Content-Disposition: attachment; filename="originalName"
   - Content-Type: MediaType.APPLICATION_OCTET_STREAM

6. **삭제(8.7) 조건**:
   - (currentUser.id == question.user.id AND question.status == OPEN) OR (currentUser.role == ADMIN)
   - 삭제 시 연관 첨부파일 storageService.delete() 호출 후 DB 삭제

---

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260226-ATS-021-summary.md
- 구현 완료 API 목록, 주요 설계 결정, 잔여 위험

Agent-facing → deliverables/agent/WI-20260226-ATS-021-evidence-pack.md
- 생성/수정 파일 목록 (경로)
- 테스트 결과 (gradlew.bat test 출력)
- 회귀 위험 여부

Handoff Packet → deliverables/agent/WI-20260226-ATS-021-handoff.md (이 파일)

---

[TRACEABILITY REQUIREMENTS]
- Evidence: 생성/수정 파일 전체 경로 목록
- Tests: gradlew.bat test 결과 (총 테스트 수, failures=0 확인)
- Rollback: 신규 파일이므로 삭제로 롤백 가능. Question 엔티티 수정 포함 시 git diff 기록
