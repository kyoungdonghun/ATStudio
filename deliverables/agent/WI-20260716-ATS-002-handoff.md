[WI HEADER]
WI ID: WI-20260716-ATS-002
REQ: REQ-20260716-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260716-ATS-003

[WI SUMMARY]
Why: 클라이언트 시연과 실제 사용자 접점에 노출되는 기존 브랜드명 `ATStudio`를 `AT.M`으로 통일해야 한다.
Scope (in/out): In: 활성 React SPA 표시 문구, 브라우저 제목, 이메일 표시 문구, Toss 결제 표시 상품명, seed 공지 원본, 관련 테스트, 사용자 요약과 evidence. Out: URL·도메인·이메일 주소, Java/package/class, npm package, Spring application name, DB/schema, 환경변수, 내부 HTTP header, 암호화 associated data, 기존 DB 데이터 변경, 디자인 개편.
DoD: 모든 범위 내 사용자 노출 문구가 정확히 `AT.M`으로 바뀌고, 내부 식별자와 호환성 문자열은 유지되며, 관련 검증이 통과한다.
Constraints/Forbidden: 기능·정책·스타일을 변경하지 않는다. 기존 DB를 수정하지 않는다. 내부 `atstudio` 문자열을 일괄 치환하지 않는다. 다른 작업자의 변경과 런타임 로그를 되돌리거나 stage하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 공개 헤더, 관리자 헤더, 로그인·소셜 로그인, 회원가입, 홈 푸터, 브라우저 탭에 `AT.M`이 표시된다.
- [ ] 사용자 이메일 제목/기본 문구와 신규 Toss 주문 표시명에 `AT.M`이 사용된다.
- [ ] 신규 DB 초기화용 공지 seed의 표시 문구가 `AT.M`으로 최신화된다.
- [ ] URL, 도메인, 이메일 주소, package/class, 내부 header와 암호화 호환 문자열은 유지된다.
Quality:
- [ ] 범위 제한 검색으로 사용자 노출 `ATStudio` 잔존 여부를 분류한다.
- [ ] 관련 프론트 테스트와 백엔드 테스트 또는 컴파일 검증을 수행한다.
- [ ] 변경 파일에 diff 오류가 없다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/quality-gates.md
Tier 2 (Tech Stack / Context):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-001.md
Files:
- frontend/index.html
- frontend/src/layouts/Header.tsx
- frontend/src/layouts/AdminLayout.tsx
- frontend/src/pages/auth/LoginPage.tsx
- frontend/src/pages/auth/SocialLoginPage.tsx
- frontend/src/pages/auth/SignupPage.tsx
- frontend/src/pages/public/HomePage.tsx
- frontend/src/pages/subscriber/WhitelistChannelPage.tsx
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/main/resources/seed.sql
Repro/Logs:
- User screenshot shows green header brand `ATStudio` at the public Track detail page.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260716-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for each changed user-facing surface and each intentionally preserved identifier.
Tests: Record exact commands and results.
Rollback: Explain code-only rollback; no DB rollback is needed because existing data must not be mutated.
