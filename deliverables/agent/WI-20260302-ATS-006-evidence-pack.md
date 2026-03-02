# WI-20260302-ATS-006 Evidence Pack — 성능/안정성 수정 코드 리뷰

## CR-A-002: Track N+1 쿼리

**판정: ✅ PASS**

`TrackRepository.java:18-24` — @EntityGraph 적용:
- L18-20: `findAll(Specification, Pageable)` — `@EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})` 오버라이드
- L22-24: `findByIdWithTags(@Param("id") Long id)` — `@Query` + `@EntityGraph` 동일 적용
- TrackService: `buildTagsMap()` 제거, `track.getTrackTags()` 직접 사용 (L88-95)
- `findByIdWithTags()` 호출: `TrackService.java:108`

테스트: TrackServiceTest — 기존 테스트 모두 통과 (TrackService mock 기반)

**MINOR**: @EntityGraph + Pageable 조합 → Hibernate HHH90003004 WARN (in-memory pagination). 현재 데이터 규모에서 기능 이상 없음. Scale 시 two-query 패턴 전환 권장.

## CR-A-005: PlayHistory saveAll

**판정: ✅ PASS (변경 없음)**

`PlayHistoryService.java` 검토 결과:
- 단건 재생 이력 저장 API만 존재 — 배치 insert 없음
- `save()` 단건 호출이 현재 도메인에 적합
- saveAll() 미적용은 결함 아님 — 배치 API 부재로 변경 불필요

테스트: PlayHistoryServiceTest — 7건 모두 PASS (회귀 검증 결과)

## CR-C-004: DownloadQueueService @Transactional

**판정: ✅ PASS (변경 없음)**

`DownloadQueueService.java:21` — 클래스 레벨 `@Transactional(readOnly = true)` 이미 적용 확인:
- mutating 메서드(requestDownload, cancelDownload 등): 메서드 레벨 `@Transactional` override 확인
- 감사 보고서의 "누락" 판단은 재확인 후 기존 구현이 올바름으로 정정

테스트: DownloadQueueServiceTest — 9건 모두 PASS (회귀 검증 결과)
