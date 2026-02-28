# WI-20260227-ATS-030 Summary — cr-B 검토: Subscription·Whitelist·DownloadQueue·Likes

**검토 범위:** 20개 API (6.x Subscription / 12.x Whitelist / 11.x DownloadQueue / 10.x Likes)
**최종 판정:** CONDITIONAL PASS — MAJOR 3건 수정 후 승인

---

## 판정 통계

| 판정 | 건수 |
|------|------|
| ✅ 정상 | 15 |
| ⚠️ 미흡 | 2 |
| ❌ 수정 필요 | 3 |
| 📋 추후 개선 | 2 |

---

## MAJOR (프론트 전 반드시 수정)

| # | 이슈 | 파일:라인 |
|---|------|---------|
| CR-B-001 | 6.9 관리자 취소: 응답 `200 OK` (명세: `204 No Content`) | `UserSubscriptionController.java:97-103` |
| CR-B-002 | 6.10 셀프 취소: 응답 `200 OK` (명세: `204 No Content`) | `UserSubscriptionController.java:107-114` |
| CR-B-003 | 6.7 업/다운그레이드: `proratedAmount.abs()` 사용 → 다운그레이드 환불(-) 을 추가 청구(+)로 변환 | `UserSubscriptionService.java:176` |

---

## MINOR (권장 수정)

| # | 이슈 | 파일:라인 |
|---|------|---------|
| CR-B-004 | 6.1 유효하지 않은 `userType` 문자열 → `IllegalArgumentException` → 500 에러 (400이어야 함) | `SubscriptionService.java:25` |
| CR-B-005 | Whitelist URL 검증: `contains("youtube.com")` → `evil.site/youtube.com` 통과 가능 | `WhitelistChannelService.java:98` |

---

## 전반적 평가

코딩 표준(readOnly=true, DTO 분리, @EntityGraph, 복합PK existsById 체크 등) 전반적으로 우수. Whitelist/DownloadQueue/Likes는 이슈 없음. Subscription은 HTTP 응답 코드 불일치(6.9/6.10) + 다운그레이드 금액 부호 버그(6.7) 3건 수정 필요.
