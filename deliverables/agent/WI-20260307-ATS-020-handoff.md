[WI HEADER]
WI ID: WI-20260307-ATS-020
REQ: REQ-20260307-ATS-008
Agent: cr
Depends On: WI-013/014/015 (Phase 3 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 4 — User/Auth/PlayHistory/Likes/License/Whitelist 도메인 코드 정합성 체크
Scope (in):
  - UserController.java + UserService.java 코드 체크
  - AuthController.java 코드 체크
  - PlayHistoryController.java + PlayHistoryService.java 코드 체크
  - LikeController.java + LikeService.java 코드 체크
  - LicenseController.java + LicenseService.java 코드 체크
  - WhitelistChannelController.java + WhitelistChannelService.java 코드 체크
  - 이 12개 파일(+관련 DTO) 만
Scope (out):
  - 코드 수정 금지
  - 다른 도메인 파일 탐색 금지

Constraints/Forbidden:
  - 발견 보고만. 코드 수정 절대 금지.

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] UserController: PUT /api/users/me/password — HTTP 응답 코드 확인
       api-spec §5.11: 204 No Content
       200 OK이면 [CONFLICT] MAJOR 보고
- [ ] AuthController: POST /api/auth/logout 엔드포인트 존재 여부
       기대: 존재하지 않음 (client-side only, BD MAJOR-002 결정)
       만약 존재한다면 [CONFLICT] MINOR 보고 (문서와 불일치)
- [ ] PlayHistoryController: POST /api/play-histories — 구현 존재 여부, Auth=구독자
       api-spec §4.1 대조
- [ ] LikeController: POST /api/likes/{trackId} — path param 이름 {trackId} 확인
       api-spec §10.1 대조
- [ ] LikeController: 좋아요 토글 구현 방식 — POST만인지, 별도 DELETE인지
       api-spec §10.2 (DELETE /api/likes/{trackId}) 존재 여부 함께 확인
- [ ] LicenseController: GET /api/licenses/{id}/tracks — 구현 존재 여부
       api-spec §7.3 대조
- [ ] WhitelistChannelController: POST /api/whitelist-channels — 한도 초과(WHITELIST_CHANNEL_LIMIT_EXCEEDED) 에러 처리 확인
       api-spec §12.1 에러케이스 대조
- [ ] AuthController: 리프레시 토큰 만료 시 거부 로직 확인 (기존 PR-P-005 수정 완료 여부 재확인)

Quality:
- [ ] 발견 항목별 파일:라인 포인터 포함
- [ ] CONFLICT/GAP/OMISSION/SUGGESTION 형식 준수

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (cr 필수 + security 관련):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

참조 문서 (코드와 대조 기준):
- docs/design/api-spec.md  ← §4 (PlayHistory), §5 (Users/Auth), §5.11 (비밀번호 변경), §7 (Licenses), §10 (Likes), §12 (Whitelist)
- docs/design/usecase/user-info.md  ← INFO-008 (로그인), INFO-007 (회원탈퇴)
- docs/check/screen-flow.md  ← §2 인증 흐름 (로그아웃 client-side 명시)

Files (검사 대상):
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/controller/AuthController.java
- src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java
- src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java
- src/main/java/com/atstudio/atstudio/controller/LikeController.java
- src/main/java/com/atstudio/atstudio/service/LikeService.java
- src/main/java/com/atstudio/atstudio/controller/LicenseController.java
- src/main/java/com/atstudio/atstudio/service/LicenseService.java
- src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java  (참조용)

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-020-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-020-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-020-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 발견 항목별 파일:라인 포인터 포함
Format:
  [CONFLICT] 상충: (코드 파일:라인) vs (문서:섹션) — 설명
  [GAP]      누락: 문서에는 있으나 코드에 없음 — 설명
  [OMISSION] 미흡: 부분 구현 — 설명
  [SUGGESTION] 제안: 개선 가능 — 설명
심각도: CRITICAL / MAJOR / MINOR / SUGGESTION
