[EVIDENCE PACK]
WI ID: WI-20260307-ATS-024
REQ: REQ-20260307-ATS-009
Agent: cr
Completed: 2026-03-08

---

## MAJOR-001: api-spec §5.11 Password Change — UC 누락

- api-spec: `docs/design/api-spec.md:825~845` (§5.11 Update Password)
  - URL: `PUT /api/users/me/password`
  - Auth: 인증 필요
  - Status: 204 No Content / 400 Bad Request
- usecase: `docs/design/usecase/user-info.md` 전체 검토
  - INFO-001~008, INFO-013~014 존재
  - "password change" UC 없음 (INFO-007 Withdraw 내 비밀번호 확인 flow만 존재)
- index: `docs/design/usecase/index.md` — 비밀번호 변경 UC 미등록
- 수정 제안: user-info.md에 INFO-015 "Update Password" 신규 UC 추가
  - Actor: User (Member)
  - Trigger: 내 정보 화면에서 비밀번호 변경 클릭
  - Main Flow: 현재 비밀번호 + 새 비밀번호 입력 → 검증 → 204
  - Exception: 현재 비밀번호 불일치 → 400

---

## MINOR-001: §8 API/UC 번호 순서 역전

- api-spec: `docs/design/api-spec.md:1179~1204`
  - 8.6 = Change Inquiry Status (`PUT /api/questions/{questionId}/status`, ADMIN)
  - 8.7 = Delete Inquiry (`DELETE /api/questions/{questionId}`)
- usecase: `docs/design/usecase/user-question.md:140~191`
  - QUESTION-006 = Delete Inquiry (line 140)
  - QUESTION-007 = Change Inquiry Status (line 167)
- 판단: URL/Method/Auth/상태코드 모두 일치, 순서만 역전. 기능 오류 없음.
- 수정 제안: UC에 api-spec 섹션 번호 교차 참조 추가, 또는 UC 순서 재정렬

---

## PASS 도메인 근거 요약

| Domain | 검증 근거 |
|--------|----------|
| §6 Subscription | PAYMENT-007 UPGRADE/DOWNGRADE, PAYMENT-010 취소 유예 — api-spec §6.7/§6.10 완벽 일치 |
| §7 License | URL 패턴, Auth, 응답 필드(licenseCode, issuedAt) 모두 일치 |
| §9 Notice | URL, Method, Auth(PUBLIC/ADMIN), 상태코드(201/200/204) 모두 일치 |
| §12 Whitelist | URL, Auth, 에러코드(WHITELIST_CHANNEL_LIMIT_EXCEEDED) 모두 일치 |
| §13 CompanyCert | URL, Auth, 상태코드, 필드(status, adminNote, certificationCode) 모두 일치 |
| §14 Util | nextResetAt, proratedAmount, changeType 등 모든 필드 일치 |
| §15 Album | URL, Method, Auth(ADMIN/none), 에러코드 모두 일치 |
