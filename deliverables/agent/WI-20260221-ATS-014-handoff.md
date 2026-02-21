[WI HEADER]
WI ID: WI-20260221-ATS-014
REQ: REQ-20260221-ATS-003
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-017

[WI SUMMARY]
Why: WI-013 코드 리뷰에서 발견된 보안 이슈(C-01), ResponseDTO 표준 위반(M-06), size 파라미터 검증 누락(m-07/08) 수정.
Scope (in/out):
  In:
    - TagController.createTag()에 @PreAuthorize("hasRole('ADMIN')") 추가 [C-01]
    - NoticeController.createNotice(), getNotice(), updateNotice() 응답을 ResponseDTO<NoticeResponse> 래퍼로 변경 [M-06]
    - PlayHistoryService.getMyHistory() size 파라미터: size → Math.max(1, size) [m-07]
    - NoticeService.getNotices() size 파라미터: size → Math.max(1, size) [m-08]
  Out:
    - 그 외 파일 수정 금지
    - 테스트 파일 수정 금지 (NoticeControllerTest는 status만 확인하므로 영향 없음)
    - 새 기능 추가 금지
DoD:
  - TagController.java에 @PreAuthorize("hasRole('ADMIN')") 존재 (createTag 메서드)
  - NoticeController 3개 메서드가 ResponseDTO<NoticeResponse> 반환
  - PlayHistoryService + NoticeService size 검증 적용
Constraints/Forbidden:
  - 수정 대상 4개 파일 외 변경 금지
  - 로직 변경 금지 (표준 준수 수정만)
  - 테스트 코드 수정 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] TagController.createTag()에 @PreAuthorize("hasRole('ADMIN')") 추가됨
  - [ ] NoticeController.createNotice() → ResponseEntity<ResponseDTO<NoticeResponse>> 반환
  - [ ] NoticeController.getNotice() → ResponseEntity<ResponseDTO<NoticeResponse>> 반환
  - [ ] NoticeController.updateNotice() → ResponseEntity<ResponseDTO<NoticeResponse>> 반환
  - [ ] PlayHistoryService line 51: PageRequest.of(Math.max(0, page - 1), Math.max(1, size))
  - [ ] NoticeService line 49: PageRequest.of(Math.max(0, page - 1), Math.max(1, size))
Performance:
  - [ ] N/A
Quality:
  - [ ] 컴파일 오류 없음
  - [ ] 기존 테스트 통과 (WI-017에서 검증)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards):
  - docs/standards/development-standards.md

Tier 1 (Security):
  - docs/policies/security-policy.md

REQ/Context Docs:
  - deliverables/user/REQ-20260221-ATS-003.md
  - deliverables/user/WI-20260221-ATS-013-summary.md

Files (primary targets):
  - src/main/java/com/atstudio/atstudio/controller/TagController.java
  - src/main/java/com/atstudio/atstudio/controller/NoticeController.java
  - src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java
  - src/main/java/com/atstudio/atstudio/service/NoticeService.java

Reference (for ResponseDTO pattern):
  - src/main/java/com/atstudio/atstudio/common/dto/ResponseDTO.java
  - src/main/java/com/atstudio/atstudio/controller/LikeController.java (ResponseDTO 사용 예시)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-014-summary.md :
  - 수정된 파일 목록 + 변경 내용 요약
Agent-facing -> deliverables/agent/WI-20260221-ATS-014-evidence-pack.md :
  - 수정 전/후 코드 스니펫 (파일:라인)
  - 각 이슈별 수정 완료 확인
Handoff Packet -> deliverables/agent/WI-20260221-ATS-014-handoff.md :
  - This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 각 수정 파일:라인 번호 필수
Tests: WI-017 (qa)에서 검증
Rollback: git diff로 추적 가능
