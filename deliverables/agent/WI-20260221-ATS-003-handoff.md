[WI HEADER]
WI ID: WI-20260221-ATS-003
REQ: REQ-20260221-ATS-001
Agent: se
Depends On: WI-20260221-ATS-002
Blocks: WI-20260221-ATS-005

[WI SUMMARY]
Why: Tag CRUD API와 Track-Tag 연결 공통 인프라를 구현한다.
     Tag 없이는 Track 생성 시 태그 연결이 불가능하므로, Track CRUD(WI-004)와 병렬로 선행 작업한다.

Scope (in/out):
  In:
    - TagController: POST /api/tags (ADMIN), GET /api/tags (PUBLIC)
    - TagService: 태그 생성, 목록 조회
    - TagCreateRequest, TagResponse DTO
    - TrackTagRepository 메서드 추가:
        deleteAllByTrack(Track track)
        findAllWithTagByTrack(@Query JOIN FETCH)
        findAllWithTagByTrackIdIn(@Query JOIN FETCH, 배치 로딩용)
    - 중복 태그명 검증 (BusinessException)
  Out:
    - Tag 수정/삭제 API (별도 REQ)
    - Track CRUD 구현 (→ WI-004)
    - Stream/Download (→ WI-005)

DoD:
  - Tag API 2종 엔드포인트 정상 동작
  - TrackTagRepository 메서드 3종 추가
  - 빌드 통과

Constraints/Forbidden:
  - 기존 Tag, TrackTag, TrackTagId Entity 수정 금지
  - Tag 목록 API(GET /api/tags)는 ResponseDTO 래퍼 없이 List<TagResponse> 직접 반환
    (api-spec.md Section 2.2 — raw array 명세, 표준 예외)
  - SecurityConfig ADMIN 규칙은 이미 설정됨 → @PreAuthorize 추가 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/tags → 201 Created, TagResponse 반환
  - [ ] POST /api/tags 중복 태그명 → BusinessException (TECHNIC or BUSINESS 에러)
  - [ ] GET /api/tags → 200 OK, List<TagResponse> (ResponseDTO 래퍼 없는 raw array)
  - [ ] TrackTagRepository.deleteAllByTrack() 정상 동작
  - [ ] TrackTagRepository.findAllWithTagByTrack() JOIN FETCH 정상 동작
  - [ ] TrackTagRepository.findAllWithTagByTrackIdIn() 배치 로딩 정상 동작

Quality:
  - [ ] 빌드 에러 없음
  - [ ] Entity 수정 없음

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

Tier 2 (Design):
- docs/design/api-spec.md          ← Section 2. Tag (2.1~2.2)
- docs/standards/dto-standards.md  ← ResponseDTO/RequestDTO 패턴
- docs/standards/exception-handling.md ← BusinessException 패턴

Files (설계 근거):
- deliverables/agent/WI-20260221-ATS-002-evidence-pack.md  ← Section 2.5/2.6 DTO 명세, 3.3 TrackTagRepository 메서드
- src/main/java/com/atstudio/atstudio/entity/Tag.java
- src/main/java/com/atstudio/atstudio/entity/TrackTag.java
- src/main/java/com/atstudio/atstudio/entity/key/TrackTagId.java
- src/main/java/com/atstudio/atstudio/repository/TagRepository.java
- src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java
- src/main/java/com/atstudio/atstudio/entity/enums/TagType.java
- src/main/java/com/atstudio/atstudio/common/dto/ResponseDTO.java
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java

REQ:
- deliverables/user/REQ-20260221-ATS-001.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-003-summary.md :
  - 구현 완료 항목 목록
  - 생성/수정 파일 목록

Agent-facing -> deliverables/agent/WI-20260221-ATS-003-evidence-pack.md :
  - 생성 파일 전체 경로 목록
  - 주요 결정 사항 (특히 Tag 목록 raw array 반환 처리)
  - WI-004 참조 사항 (TrackTagRepository 메서드 완료 여부)

Handoff Packet -> deliverables/agent/WI-20260221-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/수정된 파일 경로 + 라인 범위
Tests: N/A (테스트는 re → WI-007)
Rollback: git revert WI-003 커밋
