[WI HEADER]
WI ID: WI-20260221-ATS-004
REQ: REQ-20260221-ATS-001
Agent: se
Depends On: WI-20260221-ATS-002
Blocks: WI-20260221-ATS-005

[WI SUMMARY]
Why: Track 핵심 CRUD(생성/수정/삭제/목록/상세/스트리밍)와 파일 저장 인프라를 구현한다.
     StorageService 인터페이스 + LocalStorageService를 선행 구현하여 파일 업로드/서빙 기반을 마련한다.

Scope (in/out):
  In:
    - StorageService 인터페이스 + LocalStorageService (로컬 파일시스템)
    - WebMvcConfigurer: /uploads/** 정적 리소스 매핑
    - application.yml: multipart 설정 (max-file-size: 50MB, max-request-size: 100MB)
    - application.yml: app.storage.base-path=uploads
    - TrackController: POST/GET/GET{id}/PUT/DELETE /api/tracks + GET /api/tracks/{id}/stream
    - TrackService: 생성/수정/삭제/조회/목록/스트리밍 로직
    - TrackRepository: JpaSpecificationExecutor<Track> 추가
    - TrackSpecification: 동적 필터 (isActive, titleContains, hasBpmBetween, hasTonality, hasTagWithNameAndType)
    - DTO: TrackCreateRequest, TrackUpdateRequest, TrackResponse, TrackListItemResponse, TrackSearchRequest
    - Track-Tag 연결: 생성/수정 시 WI-003에서 추가된 TrackTagRepository 메서드 활용
    - BUSINESS_ERROR 추가: TRACK_NOT_FOUND, TAG_NOT_FOUND (없는 경우)
  Out:
    - Download API 1.5 (→ WI-005)
    - 라이선스 자동 발급 (→ WI-005)
    - preview_file 자동 생성 (별도 REQ)

DoD:
  - Track API 6종 (1.1~1.4, 1.6, 1.7) 엔드포인트 정상 동작
  - 파일 업로드 → uploads/tracks/audio/, uploads/tracks/thumbnail/ 저장 확인
  - 스트리밍(1.4) preview_file 우선, NULL이면 audio_file 폴백
  - 목록 검색/필터 (keyword, genre, mood, instrument, bpmMin, bpmMax, tonality, sort) 동작
  - 소프트 삭제(is_active=0), 목록 is_active=1 필터 적용
  - 빌드 통과

Constraints/Forbidden:
  - 기존 Track, Tag, TrackTag Entity 수정 금지
  - @PreAuthorize 추가 금지 (SecurityConfig URL 기반 ADMIN 설정 이미 완료)
  - QueryDSL 의존성 추가 금지 (JPA Specification 사용)
  - Tag 목록 조회 시 N+1 방지 필수:
      단건 상세: TrackTagRepository.findAllWithTagByTrack() JOIN FETCH 사용
      목록: TrackTagRepository.findAllWithTagByTrackIdIn() 배치 로딩 사용
  - sort 파라미터 매핑: "latest" → Sort.by(DESC, "createdAt"), "popular" → Sort.by(DESC, "playCount")
  - tagIds 유효성 검증: 존재하지 않는 tagId → BusinessException(TAG_NOT_FOUND)
  - WI-003(Tag)과 병렬 실행 — TrackTagRepository 메서드가 WI-003에서 추가됨.
    구현 시 해당 메서드가 없으면 스텁(TODO)으로 처리하고 evidence-pack에 기록

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/tracks → 201, TrackResponse (파일 저장 + Track-Tag 연결)
  - [ ] GET /api/tracks → 200, dataList + pageInfo (is_active=1만 반환)
  - [ ] GET /api/tracks?keyword=summer → 제목 검색 필터 동작
  - [ ] GET /api/tracks?genre=Pop&bpmMin=100&bpmMax=140 → 복합 필터 동작
  - [ ] GET /api/tracks?sort=popular → play_count 내림차순 정렬
  - [ ] GET /api/tracks/{id} → 200, TrackResponse (tags 포함)
  - [ ] GET /api/tracks/{id}/stream → 200, audio stream (preview 우선 폴백)
  - [ ] PUT /api/tracks/{id} → 200, 수정된 TrackResponse
  - [ ] DELETE /api/tracks/{id} → 204, is_active=0 처리
  - [ ] uploads/ 디렉토리에 파일 실제 저장 확인

Quality:
  - [ ] 빌드 에러 없음
  - [ ] N+1 쿼리 없음 (배치 로딩 적용)
  - [ ] Entity 수정 없음

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

Tier 2 (Design):
- docs/design/api-spec.md              ← Section 1. Sound Track (1.1~1.4, 1.6, 1.7)
- docs/standards/dto-standards.md      ← ResponseDTO/RequestDTO 패턴, PageInfo
- docs/standards/exception-handling.md ← BusinessException, BUSINESS_ERROR

Files (설계 근거):
- deliverables/agent/WI-20260221-ATS-002-evidence-pack.md
  ← Section 1(StorageService), 2(DTO명세), 3(Track-Tag연결), 4(Specification), 9(주의사항)
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/java/com/atstudio/atstudio/entity/Tag.java
- src/main/java/com/atstudio/atstudio/entity/TrackTag.java
- src/main/java/com/atstudio/atstudio/entity/key/TrackTagId.java
- src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
- src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java
- src/main/java/com/atstudio/atstudio/repository/TagRepository.java
- src/main/java/com/atstudio/atstudio/common/dto/RequestDTO.java
- src/main/java/com/atstudio/atstudio/common/dto/ResponseDTO.java
- src/main/java/com/atstudio/atstudio/common/dto/PageInfo.java
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/resources/application.yml
- build.gradle

REQ:
- deliverables/user/REQ-20260221-ATS-001.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-004-summary.md :
  - 구현 완료 항목 (API 6종, StorageService, Specification)
  - 생성/수정 파일 목록
  - WI-005(Download) 착수 전 확인 사항

Agent-facing -> deliverables/agent/WI-20260221-ATS-004-evidence-pack.md :
  - 생성/수정 파일 전체 경로 목록
  - 주요 설계 결정 (Specification 필터 구성, N+1 해소 방법)
  - WI-003 병렬 실행 관련 스텁 처리 내용 (있는 경우)
  - WI-005(Download)에 필요한 참조 정보

Handoff Packet -> deliverables/agent/WI-20260221-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/수정된 파일 경로 + 핵심 로직 라인 범위
Tests: N/A (테스트는 re → WI-007)
Rollback: git revert WI-004 커밋
