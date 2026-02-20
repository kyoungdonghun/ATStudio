# WI-20260221-ATS-002 설계 요약: Track/Tag 도메인 설계

> **날짜:** 2026-02-21
> **상태:** 완료
> **담당:** SA (Software Architect)

---

## 설계 결정 요약

### 1. 파일 저장소: StorageService 인터페이스 추상화

- `StorageService` 인터페이스 (store / getUrl / delete 3개 메서드)
- `LocalStorageService` 구현체 (로컬 파일시스템 기반)
- 파일 경로 구조:
  - 오디오: `uploads/tracks/audio/`
  - 썸네일: `uploads/tracks/thumbnail/`
  - 프리뷰: `uploads/tracks/preview/`
- 파일명: UUID 접두사 + 원본 파일명 (충돌 방지)
- 향후 S3 전환 시 `S3StorageService` 구현체만 추가하면 됨

### 2. DTO 구조

| DTO | 용도 | 주요 필드 |
|-----|------|----------|
| `TrackCreateRequest` | 트랙 생성 (multipart) | title, bpm, tonality, audioFile, thumbnail, tagIds |
| `TrackUpdateRequest` | 트랙 수정 (multipart) | 전부 optional + isActive |
| `TrackResponse` | 트랙 상세 | 전체 필드 + tags + updatedAt |
| `TrackListItemResponse` | 목록 항목 | description/audioFile/updatedAt 제외 |
| `TrackSearchRequest` | 목록 검색 조건 | RequestDTO 상속 + genre/mood/instrument/bpm/tonality/sort |
| `TagCreateRequest` | 태그 생성/수정 | name, type |
| `TagResponse` | 태그 응답 | id, name, type, createdAt |

### 3. 쿼리 방식: JPA Specification 선택

- **QueryDSL 미채택 이유:** build.gradle에 QueryDSL 의존성 없음. 추가 시 APT 코드 생성 플러그인 설정 필요하여 현 단계에서 과도함.
- **JPQL 미채택 이유:** 선택적 필터 7개 이상 -- 동적 조합에 부적합.
- **Specification 채택 근거:** spring-data-jpa에 내장, 추가 의존성 불필요, 동적 필터 조합에 적합.
- `TrackRepository`에 `JpaSpecificationExecutor<Track>` 추가 필요.

### 4. Track-Tag 연결: 전체 교체(Full Replacement) 전략

- 트랙 생성 시: 요청의 tagIds로 TrackTag 생성
- 트랙 수정 시: tagIds가 제공되면 기존 전부 삭제 후 새로 생성 (tagIds=null이면 변경 없음)
- `TrackTagRepository`에 `deleteAllByTrack`, `findAllWithTagByTrack` 메서드 추가 필요

### 5. 다운로드 일일 한도

- `TrackDownloadRepository`에 당일 다운로드 수 카운트 쿼리 추가
- `LocalDate.now().atStartOfDay()` 기준으로 `downloadedAt >= startOfDay` 조건
- 기존 인덱스 `(user_id, downloaded_at)` 활용

### 6. 라이선스 자동 발급

- 다운로드 시 `LicenseRepository.findByUserAndTrack()` 조회
- 없으면 UUID 기반 licenseCode 생성 후 저장
- 있으면 무시 (멱등성)
- 동시성 대비: DB UNIQUE 제약 + DataIntegrityViolationException catch

### 7. ADMIN 권한: 이미 구성 완료

- SecurityConfig에 URL 기반 `.hasRole("ADMIN")` 이미 설정됨
- 별도 `@PreAuthorize` 추가 불필요
- 다운로드 구독 체크는 Service 레이어에서 비즈니스 로직으로 처리

---

## SE가 구현 시 주의할 사항

| # | 주의사항 |
|---|---------|
| 1 | **기존 Entity 수정 금지** -- 모든 설계가 현행 엔티티 구조에 맞춰져 있음 |
| 2 | **N+1 방지** -- 목록 조회 시 태그를 배치 로딩해야 함 (`findAllWithTagByTrackIdIn` 쿼리 사용) |
| 3 | **Tag List (2.2) 응답은 배열** -- ResponseDTO 래퍼 없이 `List<TagResponse>` 직접 반환 (API 명세 준수) |
| 4 | **sort 파라미터 매핑** -- "latest" -> `createdAt DESC`, "popular" -> `playCount DESC` |
| 5 | **multipart 설정** -- `application.yml`에 `max-file-size: 50MB`, `max-request-size: 100MB` 추가 필요 |
| 6 | **WebConfig 리소스 매핑** -- `/uploads/**` 경로를 로컬 파일시스템에 매핑하는 설정 필요 |
| 7 | **TrackSearchRequest의 size 기본값** -- API 명세에서 기본값 20인데, `RequestDTO` 기본값은 10. 오버라이드 필요 |

---

## 상세 설계: Evidence Pack 참조

전체 설계 근거, 대안 분석, ADR, 파일 참조 목록은 아래 문서에 포함:
- `deliverables/agent/WI-20260221-ATS-002-evidence-pack.md`
