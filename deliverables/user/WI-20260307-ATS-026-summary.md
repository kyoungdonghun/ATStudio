[WI SUMMARY]
WI ID: WI-20260307-ATS-026
REQ: REQ-20260307-ATS-009
Track: 2-B (api-spec §5~9, §12~15 ↔ 백엔드 코드)
Status: Completed ✅

---

## Overall Assessment

11개 Controller, 53개 엔드포인트 전체 검증. **CRITICAL/MAJOR 이슈 없음.** URL/Method/상태코드/권한 모두 일치. BD-1 구독 취소 유예기간 정상 구현 확인. MINOR 2건(응답 DTO superset), SUGGESTION 2건(@PreAuthorize 일관성).

## Issue Count by Controller

| Controller | CRITICAL | MAJOR | MINOR | SUGGESTION | Verdict |
|---|---|---|---|---|---|
| AuthController | 0 | 0 | 0 | 0 | PASS ✅ |
| UserController | 0 | 0 | 1 | 0 | Issues found |
| SubscriptionController | 0 | 0 | 0 | 0 | PASS ✅ |
| UserSubscriptionController | 0 | 0 | 1 | 1 | Issues found |
| LicenseController | 0 | 0 | 0 | 0 | PASS ✅ |
| QuestionController | 0 | 0 | 0 | 0 | PASS ✅ |
| NoticeController | 0 | 0 | 0 | 0 | PASS ✅ |
| WhitelistChannelController | 0 | 0 | 0 | 0 | PASS ✅ |
| CompanyCertificationController | 0 | 0 | 0 | 1 | Issues found |
| UtilController | 0 | 0 | 0 | 0 | PASS ✅ |
| AlbumController | 0 | 0 | 0 | 0 | PASS ✅ |
| **Total** | **0** | **0** | **2** | **2** | |

## Special Verification Results (BD-1, §14.5, §14.8)

| 확인 항목 | 결과 |
|---|---|
| BD-1 구독 취소 유예기간 (findActiveByUser IN ACTIVE/CANCELLED) | PASS ✅ |
| §14.5 nextResetAt 필드 | PASS ✅ |
| §14.8 subscription-change-preview 구현 + 응답 필드 | PASS ✅ |

## Issue Summary

### MINOR-001 — UserSubscriptionResponse DTO superset
- api-spec §6.3 응답보다 많은 필드 반환 (`userId`, `userNickname`, full `SubscriptionResponse` 9필드, `pendingSubscriptionId`, `pendingBillingCycle`, `createdAt`)
- 클라이언트 호환성 문제 없음 (superset)
- **권장**: api-spec §6.3/§6.4 응답 필드 보완 (문서 보완)

### MINOR-002 — UserResponse DTO superset
- api-spec §5.1 응답 미명시 필드 반환 (`phonePersonal`, `phoneCompany`, `role`)
- **권장**: api-spec §5.1/§5.4 응답 필드 보완 (문서 보완)

### SUGGESTION-001 — UserSubscriptionController admin 엔드포인트 @PreAuthorize 없음
- §6.5/§6.6/§6.8/§6.9 ADMIN 엔드포인트 — SecurityConfig에서만 보호
- NoticeController, LicenseController 등은 @PreAuthorize + SecurityConfig 이중 보호
- **권장**: `@PreAuthorize("hasRole('ADMIN')")` 추가 (defense-in-depth)

### SUGGESTION-002 — CompanyCertificationController.processReview @PreAuthorize 없음
- `CompanyCertificationController.java:77-87` — SecurityConfig:101에서만 보호
- **권장**: `@PreAuthorize("hasRole('ADMIN')")` 추가
