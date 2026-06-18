[WI HEADER]
WI ID: WI-20260618-ATS-001
REQ: REQ-20260618-ATS-001
Agent: MA/local implementation
Depends On: -
Blocks: WI-20260618-ATS-002

[WI SUMMARY]
Why: 기업회원 인증 기능을 실제 운영 가능한 신청/보완요청/심사/문서보호 흐름으로 확정한다.
Scope (in/out): In - backend schema/API/service/tests, frontend user/admin UI, docs current-state updates. Out - external business registry verification, OCR, email/SMS automation, S3 migration, payment provider expansion.
DoD: REQ success criteria를 코드/문서/검증 산출물로 충족한다.
Constraints/Forbidden: 승인 없는 실제 DB DDL 실행 금지. 기업 인증 서류 원본은 공개 URL이나 불필요한 응답 필드로 노출하지 않는다. 기존 결제 기능의 기업 인증 승인 게이트는 유지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] BUSINESS 사용자는 신규 인증 신청을 제출할 수 있다.
- [ ] PENDING/APPROVED/REVISION_REQUESTED 상태의 중복 신규 신청은 막힌다.
- [ ] REVISION_REQUESTED 상태에서는 같은 신청 건으로 서류 재제출 후 PENDING 복귀가 가능하다.
- [ ] REJECTED 상태에서는 기존 기록을 보존하고 새 신청이 가능하다.
- [ ] 관리자는 인증 상세에서 신청자 정보와 제출 서류 목록을 확인하고 승인/보완요청/반려를 처리할 수 있다.
- [ ] 관리자만 기업 인증 문서를 다운로드할 수 있다.
Performance:
- [ ] 문서 목록/다운로드 API는 기존 페이지네이션과 파일 저장소 구조를 크게 훼손하지 않는다.
Quality:
- [ ] Backend tests pass.
- [ ] Frontend typecheck/lint/test/build pass.
- [ ] Docs validation and diff check pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Design/UI/REQ context):
- deliverables/user/REQ-20260618-ATS-001.md
- docs/design/usecase/company-certification.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- docs/client/1-scenarios.md
- docs/client/2-test-cases.md

Files:
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java
- src/main/resources/schema.sql
- frontend/src/pages/subscriber/CompanyCertApplyPage.tsx
- frontend/src/pages/subscriber/CompanyCertStatusPage.tsx
- frontend/src/pages/admin/CompanyCertManagePage.tsx
- frontend/src/api/companyCerts.ts
- frontend/src/api/admin.ts
- frontend/src/types/index.ts

Repro/Logs:
- `gradlew.bat test`
- `cd frontend; npm run typecheck; npm run lint; npm run test; npm run build`
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260618-ATS-001-summary.md :
- Summary, risks, acceptance checklist, manual DB note.
Agent-facing -> deliverables/agent/WI-20260618-ATS-001-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260618-ATS-001-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include command and pass/fail result.
Rollback (if needed): Document how to revert code and schema/manual SQL changes.
