[WI HEADER]
WI ID: WI-20260307-ATS-019
REQ: REQ-20260307-ATS-008
Agent: cr
Depends On: WI-013/014/015 (Phase 3 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 4 — Company Certification / Questions / Notices 도메인 코드 정합성 체크
Scope (in):
  - CompanyCertificationController.java + CompanyCertificationService.java 코드 체크
  - QuestionController.java + QuestionService.java 코드 체크
  - NoticeController.java + NoticeService.java 코드 체크
  - 이 6개 파일(+관련 DTO/Entity) 만
Scope (out):
  - 코드 수정 금지
  - 다른 도메인 파일 탐색 금지

Constraints/Forbidden:
  - 발견 보고만. 코드 수정 절대 금지.

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] CompanyCertificationController: PUT /api/company-certifications/{certificationId} — path param 이름 확인
       기대: {certificationId} (review suffix 없음)
       기존: {certificationId} 또는 {id}? + /review suffix 있는지 확인
       api-spec §13.5 및 modal-list M-17과 일치 여부
- [ ] CompanyCertificationController: review 엔드포인트(상태 변경)가 별도 URL로 존재하는지 확인
       /review suffix가 코드에 있다면 [CONFLICT] MAJOR 보고 (스펙에는 없음)
- [ ] QuestionController: DELETE /api/questions/{questionId} — path param 이름 확인
       기대: {questionId} (api-spec §8.7)
       {id}인 경우 [CONFLICT] MINOR 보고
- [ ] QuestionController: DELETE 엔드포인트 번호가 8.7인지 (7번째 endpoint 순서 확인)
- [ ] NoticeController: CRUD endpoints Auth 확인 (ADMIN only 여부)
- [ ] CompanyCertificationService: 상태 전이 검증 로직 존재 여부
       APPROVED/REJECTED/REVISION_REQUESTED 무효 전이 차단 확인

Quality:
- [ ] 발견 항목별 파일:라인 포인터 포함
- [ ] CONFLICT/GAP/OMISSION/SUGGESTION 형식 준수

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (cr 필수):
- docs/policies/security-policy.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

참조 문서 (코드와 대조 기준):
- docs/design/api-spec.md  ← §8 (Questions), §8.7 (DELETE /api/questions/{questionId}), §9 (Notices), §13 (CompanyCertifications), §13.5 (PUT /api/company-certifications/{certificationId})
- docs/design/usecase/company-certification.md  ← CC-001 (기업인증 신청, 재신청 정책)
- docs/ui/modal-list.md  ← M-17 (ReviewModal), M-20 (문의 삭제)

Files (검사 대상):
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/controller/QuestionController.java
- src/main/java/com/atstudio/atstudio/service/QuestionService.java
- src/main/java/com/atstudio/atstudio/controller/NoticeController.java
- src/main/java/com/atstudio/atstudio/service/NoticeService.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-019-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-019-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-019-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 발견 항목별 파일:라인 포인터 포함
Format:
  [CONFLICT] 상충: (코드 파일:라인) vs (문서:섹션) — 설명
  [GAP]      누락: 문서에는 있으나 코드에 없음 — 설명
  [OMISSION] 미흡: 부분 구현 — 설명
  [SUGGESTION] 제안: 개선 가능 — 설명
심각도: CRITICAL / MAJOR / MINOR / SUGGESTION
