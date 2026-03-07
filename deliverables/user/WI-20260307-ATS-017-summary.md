[WI SUMMARY — User-Facing]
WI ID: WI-20260307-ATS-017
REQ: REQ-20260307-ATS-008 Phase 4
Domain: Track / Tag
Date: 2026-03-07
Author: cr (MA 직접 수행)

---

## 발견 건수 요약

| 심각도 | 건수 |
|--------|------|
| CRITICAL | 1 |
| MAJOR | 0 |
| MINOR | 0 |
| **합계** | **1** |

---

## CRITICAL-1: GET /api/tracks/admin 엔드포인트 미구현 (BD-2)

- **위치**: `TrackController.java` — 해당 엔드포인트 없음
- **문서 기준**: api-spec v6 §1.8 — `GET /api/tracks/admin`, Auth=[ADMIN], is_active optional 필터
- **실제 코드**:
  - TrackController에 `@GetMapping` 엔드포인트: `/` (전체 목록), `/{trackId}`, `/{trackId}/download`, `/{trackId}/stream` — `/admin` 없음
  - 기존 `GET /api/tracks` (`TrackController:38`)는 is_active=true 필터만 반환 (구독자용 public API)
  - TrackService에도 비활성 트랙 포함 전체 조회 메서드 없음
- **수정 방안**: `TrackController`에 `@GetMapping("/admin")` + `@PreAuthorize("hasRole('ADMIN')")` 추가, TrackService에 `getTracksForAdmin(is_active, page, size)` 메서드 추가, TrackRepository에 is_active 필터 쿼리 추가

---

## 이상 없음 항목

| 항목 | 결과 |
|------|------|
| Track CRUD 기본 endpoints (1.1~1.5, 1.7) | ✅ 모두 구현 |
| TrackController 각 endpoint Auth | ✅ SecurityConfig에서 관리 |
