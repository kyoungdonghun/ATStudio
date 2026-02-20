[WI HEADER]
WI ID: WI-20260221-ATS-002
REQ: REQ-20260221-ATS-001
Agent: sa
Depends On: -
Blocks: WI-20260221-ATS-003, WI-20260221-ATS-004

[WI SUMMARY]
Why: Track/Tag CRUD 구현에 앞서 도메인 구조, 파일 저장 전략, DTO 구조, 쿼리 설계를 확정한다.
      설계 없이 구현에 들어가면 se 간 충돌 및 재작업이 발생한다.

Scope (in/out):
  In:
    - StorageService 인터페이스 설계 (로컬 → 추후 S3 교체 가능 구조)
    - TrackController / TrackService / TrackRepository 계층 구조 정의
    - TrackCreateRequest, TrackUpdateRequest, TrackResponse, TrackListResponse DTO 필드 확정
    - TagCreateRequest, TagResponse DTO 필드 확정
    - Track-Tag 연결 처리 방식 (TrackTag 복합 PK, save/delete 전략)
    - 목록 검색/필터 쿼리 설계 (keyword, tagId, bpm 범위, sort)
    - 다운로드 일일 한도 집계 쿼리 설계 (TrackDownload 기반)
    - ADMIN 권한 체크 방식 확인 (Spring Security hasRole)
    - 로컬 파일 저장 경로 구조 (예: /uploads/tracks/audio/, /uploads/tracks/thumbnail/)
  Out:
    - 실제 구현 코드 작성 (→ WI-003, WI-004)
    - preview_file 자동 생성 설계 (별도 REQ)
    - play_histories, Like, Playlist 설계 (별도 REQ)

DoD:
  - 위 항목 각각에 대한 설계 결정이 evidence-pack에 문서화됨
  - se가 설계 문서만 보고 구현 착수 가능한 수준

Constraints/Forbidden:
  - 구현 코드 작성 금지 (설계·문서화만)
  - 기존 Entity 수정 금지 (Track, Tag, TrackTag, TrackDownload 이미 존재)
  - 파일 저장소: 로컬 파일시스템 확정 (S3 설계 불필요)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] StorageService 인터페이스 시그니처 정의 (store, getUrl, delete)
  - [ ] Track DTO 4종 필드 목록 확정 (Create/Update/Response/ListItem)
  - [ ] Tag DTO 2종 필드 목록 확정 (Create/Response)
  - [ ] Track-Tag 연결 처리 방식 결정 (기존 TrackTagRepository 활용 방법)
  - [ ] 목록 검색 쿼리 방식 결정 (JPQL / QueryDSL / Specification 중 택일)
  - [ ] 다운로드 한도 집계 쿼리 시그니처 정의
  - [ ] 로컬 파일 저장 경로 구조 확정

Quality:
  - [ ] 설계 결정 근거가 evidence-pack에 기록됨
  - [ ] se가 추가 질의 없이 구현 가능한 수준의 명세

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

Tier 1 (Architecture):
- docs/architecture/system-design.md

Tier 2 (Design Specs):
- docs/design/api-spec.md          ← 1. Sound Track (1.1~1.7), 2. Tag (2.1~2.2)
- docs/design/db-schema.md         ← tracks, tags, track_tags, track_downloads, licenses 테이블

Files (기존 Entity):
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/java/com/atstudio/atstudio/entity/Tag.java
- src/main/java/com/atstudio/atstudio/entity/TrackTag.java
- src/main/java/com/atstudio/atstudio/entity/key/TrackTagId.java
- src/main/java/com/atstudio/atstudio/entity/TrackDownload.java
- src/main/java/com/atstudio/atstudio/entity/License.java
- src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
- src/main/java/com/atstudio/atstudio/repository/TagRepository.java
- src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java
- src/main/java/com/atstudio/atstudio/repository/TrackDownloadRepository.java
- src/main/java/com/atstudio/atstudio/repository/LicenseRepository.java

REQ:
- deliverables/user/REQ-20260221-ATS-001.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-002-summary.md :
  - 설계 결정 요약 (DTO 구조, 쿼리 방식, 파일 경로)
  - 리스크 및 주의사항

Agent-facing -> deliverables/agent/WI-20260221-ATS-002-evidence-pack.md :
  - StorageService 인터페이스 시그니처
  - DTO 필드 명세 (4+2종)
  - Track-Tag 연결 처리 결정
  - 검색 쿼리 방식 결정 + 근거
  - 다운로드 한도 쿼리 시그니처
  - 로컬 파일 경로 구조
  - se가 참조할 구현 가이드라인

Handoff Packet -> deliverables/agent/WI-20260221-ATS-002-handoff.md :
  - 이 파일

[TRACEABILITY REQUIREMENTS]
Evidence pointers: evidence-pack에 각 설계 결정 항목별 근거 기록 필수
Tests: N/A (설계 WI)
Rollback: N/A (설계 문서만 생성, 코드 변경 없음)
