# WI-20260227-ATS-029 Summary — cr-A 검토: Track·License·Tag·Playlist·PlayHistory

**검토 범위:** 26개 API (1.x Track / 7.x License / 2.x Tag / 3.x Playlist / 4.x PlayHistory)
**최종 판정:** CONDITIONAL APPROVAL — CRITICAL 1건, MAJOR 5건 수정 후 승인

---

## 판정 통계

| 판정 | 건수 |
|------|------|
| ✅ 정상 | 16 |
| ⚠️ 미흡 | 5 |
| ❌ 수정 필요 | 5 |
| 📋 추후 개선 | 3 |

---

## CRITICAL (즉시 수정 필수)

### CR-A-001: TrackSpecification — 런타임 크래시 위험
- **API**: 1.2 GET /api/tracks (태그 필터 사용 시)
- `TrackSpecification.java:40`에서 `root.join("trackTags")`를 수행하는데, `Track` 엔티티에 `trackTags` @OneToMany 매핑이 없음 → 태그 필터(genre/mood/instrument) 사용 시 `IllegalArgumentException` 런타임 크래시
- **수정**: `Track` 엔티티에 `@OneToMany(mappedBy="track") List<TrackTag> trackTags` 추가

---

## MAJOR (프론트 전 반드시 수정)

| # | API | 이슈 | 파일:라인 |
|---|-----|------|---------|
| CR-A-002 | - | DownloadService 클래스 레벨 `@Transactional(readOnly=true)` 누락 | `DownloadService.java:19-21` |
| CR-A-003 | 1.5 다운로드 | `downloadPerDay=-1`(무제한) 시 항상 한도 초과 판정 → 무제한 플랜 사용자 다운로드 불가 | `DownloadService.java:46` |
| CR-A-004 | 1.7 소프트삭제 | 트랙 삭제 시 `track_tags` 물리 삭제 누락 (RULE-TRACK-003) | `TrackService.java:163-167` |
| CR-A-005 | 7.1/7.2 라이선스 목록 | `@EntityGraph` 누락 → N+1 (track LAZY 로딩) | `LicenseRepository.java:18,20` |
| CR-A-006 | 3.2 플레이리스트 목록 | trackCount 루프 내 N+1 쿼리 | `PlaylistService.java:66-71` |

---

## MINOR (권장 수정)

| # | 이슈 | 파일 |
|---|------|------|
| CR-A-007 | TagController 응답이 `ResponseDTO` 미사용, 원시 `List<>` 반환 | `TagController.java:37-40` |
| CR-A-008 | 플레이리스트 소프트삭제 시 `playlist_tracks` 고아 레코드 잔존 | `PlaylistService.java:181-186` |
| CR-A-009 | `TrackResponse`에 서버 내부 `audioFile` 경로 노출 | `TrackResponse.java:16` |
| CR-A-010 | 스트리밍 fallback 리소스 존재 여부 미검증 | `TrackService.java:116-131` |

---

## 전반적 평가

전반적으로 Controller 얇게 유지, DTO/Entity 분리, `@RequiredArgsConstructor`, record DTO 등 코딩 표준을 잘 준수함. CRITICAL 1건(TrackSpecification 런타임 크래시), MAJOR 5건(다운로드 무제한 버그, 소프트삭제 cascade, N+1) 수정이 필수.
