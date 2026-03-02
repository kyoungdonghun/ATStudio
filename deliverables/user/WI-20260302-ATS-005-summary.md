# WI-20260302-ATS-005 Summary — 보안 수정 코드 리뷰

**검토 범위:** WI-001/002 — CR-C-006, CR-A-004, CR-C-008, CR-B-005, CR-P-005
**최종 판정:** ✅ PASS — 모든 보안 이슈 올바르게 수정됨. MINOR 1건, SUGGESTION 2건 (비차단)

---

## 이슈별 판정

| ID | 내용 | 판정 |
|----|------|------|
| CR-C-006 | Notice 소유권 + ADMIN 예외 | ✅ PASS |
| CR-A-004 | Playlist 소유권 (기존 구현 확인) | ✅ PASS |
| CR-C-008 | TestController 삭제 | ✅ PASS |
| CR-B-005 | URL 검증 강화 (URI 파싱) | ✅ PASS |
| CR-P-005 | 만료 RefreshToken 거부 | ✅ PASS |

---

## MINOR / SUGGESTION (비차단)

| 심각도 | 위치 | 내용 |
|--------|------|------|
| MINOR | `NoticeController.java:55,68` + `NoticeService.java:90-97` | `@PreAuthorize("hasRole('ADMIN')")` 이미 적용되어 서비스 레이어 ADMIN 우회 분기가 실질적 중복. 기능 오류 아님. Defense-in-depth 관점에서 유지 가능 |
| SUGGESTION | `WhitelistChannelService.java:98-108` | scheme(http/https) 미검증 — `ftp://youtube.com` 통과 가능. `uri.getScheme()` 체크 추가 권장 |
| SUGGESTION | `AuthService.java:86-96` | 탈퇴 계정 체크가 BCrypt 연산 이후 위치 — 순서 변경으로 불필요한 연산 절약 가능 |
