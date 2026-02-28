# WI-20260227-ATS-033 Summary — Backend Audit Report Generation

> **대상**: 사용자 (승인/확인용)
> **날짜**: 2026-02-28
> **담당**: docops
> **REQ**: REQ-20260227-ATS-009

---

## 완료 사항

`docs/audit/backend-audit-report.md` 생성 완료.

WI-028(체크리스트) → WI-029(cr-A) → WI-030(cr-B) → WI-031(cr-C) → WI-032(pg) 5개 단계의 감사 결과를 하나의 보고서로 취합하였습니다.

---

## 발견 이슈 통계

| 심각도 | 건수 |
|--------|------|
| CRITICAL | 4 |
| MAJOR | 16 |
| MINOR | 10 |
| SUGGESTION | 5 |
| **합계** | **35** |

### CRITICAL 이슈 요약 (즉시 수정 필수)

| 번호 | 내용 | 파일 |
|------|------|------|
| CR-P-001 | `/api/users/me` SecurityConfig 와일드카드 충돌 → 일반 사용자 프로필 접근 불가 (HTTP 403) | `SecurityConfig.java:71-73` |
| CR-P-004 | JWT 시크릿 기본값 Base64 하드코딩 → 토큰 위조로 ADMIN 권한 탈취 가능 | `application.yml:36` |
| CR-C-001 | 문의 삭제 시 cascade 누락 → 항상 DataIntegrityViolationException (500) | `QuestionService.java:182-188` |
| CR-C-002 | AuthService/OAuth2Service 클래스 @Transactional readOnly 누락 | `AuthService.java:24`, `OAuth2Service.java:22` |

---

## 주요 도메인별 현황

| 도메인 | 상태 |
|--------|------|
| Track, Playlist, Subscription, User, Auth, Inquiry, Company Certification | 이슈 있음 (수정 필요) |
| Play History, Download Queue, Likes, Notice, Util | 이슈 없음 (CLEAN) |

---

## 다음 단계

백엔드 감사 보고서(`docs/audit/backend-audit-report.md`)를 바탕으로 수정 REQ를 작성합니다.

보고서 "Recommended Next Steps" 섹션에 아래 3단계 분류가 준비되어 있습니다:

1. **우선순위 1** — CRITICAL 4건: 단일 REQ로 즉시 처리 (각 1~2라인 수정)
2. **우선순위 2** — MAJOR 16건: 도메인별 REQ 2~3개로 분리 처리
3. **우선순위 3** — MINOR/SUGGESTION 15건: 유지보수 REQ 또는 기능 작업 중 처리

수정 REQ 작성을 진행할까요?
