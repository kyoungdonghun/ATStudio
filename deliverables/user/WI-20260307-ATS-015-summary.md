# WI-20260307-ATS-015 Summary

**WI ID**: WI-20260307-ATS-015
**REQ**: REQ-20260307-ATS-008
**Date**: 2026-03-07
**Agent**: docops
**Status**: DONE

---

## What Changed

Three check documents updated for API reference accuracy and version consistency.

### docs/ui/atstudio-front-list.md (v3 → v4)

| Screen | Change |
|--------|--------|
| Screen 1 (메인화면) | API 추가: `10.1 POST /api/likes/{trackId}`, `11.1 POST /api/download-queue` |
| Screen 3 (음원 목록) | API 추가: `10.1 POST /api/likes/{trackId}`, `11.1 POST /api/download-queue` |
| B-1 (음원 상세) | API 추가: `4.1 POST /api/play-histories` |
| Screen 15 (문의글 보기) | API 추가: `8.7 DELETE /api/questions/{id}` |
| K-7 (트랙 관리) | API 교체: `1.2 GET /api/tracks` (비활성 포함) → `1.8 GET /api/tracks/admin` |
| 버전 | v3 → v4 |

### docs/ui/modal-list.md (v1.1 → v1.2)

| Item | Change |
|------|--------|
| Component Classification | ReviewModal 행 추가 (상태 선택 + adminNote 입력 + `[취소]` `[처리]`) |
| M-17 API | `13.5 PUT /api/company-certifications/{id}/review` → `13.5 PUT /api/company-certifications/{certificationId}` |
| M-17 컴포넌트 | StatusModal → ReviewModal |
| M-19 발생 화면 | "Screen 21/22" → "Screen 22" |
| M-20 API | `8.4 DELETE /api/questions/{id}` → `8.7 DELETE /api/questions/{questionId}` |
| M-22 API | `DELETE /api/download-queue/{id}` → `DELETE /api/download-queue/{trackId}` |
| Flow 4 성공 응답 | `200 OK` → `204 No Content` |
| Flow 2 다운그레이드 | `(pendingSubscriptionId TODO T-3)` → `(pendingSubscriptionId)` (T-3 완료 반영) |
| dependencies | atstudio-front-list.md version v3 → v4 |
| 버전 | v1.1 → v1.2 |

### docs/ui/screen-flow.md (v1.1 → v1.2)

| Item | Change |
|------|--------|
| §1 관리자 GNB | `앨범관리` 항목 추가 (메인 / 앨범 / 앨범관리 / 음원관리 / 관리자대시보드 / 로그아웃) |
| §2 로그아웃 | 방식 명시: "클라이언트 측 토큰 삭제 (서버 무효화 엔드포인트 없음 — 초기 버전)" |
| §11 API 에러 404 | `[404 에러 페이지]` → `[ERR-1 404 Not Found 에러 페이지]` |
| §11 API 에러 500 | `[500 에러 페이지]` → `[ERR-2 500 Server Error 에러 페이지]` |
| 헤더 의존 문서 | `atstudio-front-list.md v3 / modal-list.md v1` → `v4 / v1.2` |
| 버전 | v1.1 → v1.2 |

---

## Impact

- 백엔드 코드 변경 없음
- 문서 전용 변경 (3개 파일)
- 프론트엔드 구현 시 참조 정확도 향상
