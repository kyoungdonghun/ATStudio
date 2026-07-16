[WI HEADER]
WI ID: WI-20260716-ATS-005
REQ: REQ-20260716-ATS-002
Agent: pg
Depends On: WI-20260716-ATS-004
Blocks: WI-20260716-ATS-010, WI-20260716-ATS-011, WI-20260716-ATS-012

[WI SUMMARY]
Why: 공개 가입·정확 일치 중복확인 API의 abuse surface를 제한하고 ADMIN이 사용자 정기결제 흐름을 사용할 수 없도록 FE와 BE 양쪽 경계를 고정한다.
Scope (in/out):
- In: P2-01, P2-02, X-02/X-03의 저장소 내 fail-closed 문서 경계. Endpoint별 budget, trusted client identity, PII 비노출 key, 429/Retry-After 테스트, ADMIN checkout route/API 거부, 관련 API·보안·UI 문서 최신화.
- Out: 소셜 전용 계정 탈퇴와 typed OAuth(P2-09/WI-009), 실제 proxy 다중 egress 시험, JWT rotation, 운영 secret, 클라이언트 브랜치.
DoD:
- POST `/api/users`와 GET `/api/utils/check-email|phone|nickname`에 독립된 abuse budget이 적용된다.
- availability key는 IP와 정규화된 식별자 fingerprint를 사용하되 원문 PII를 저장·로그하지 않는다. 회원가입 body를 filter에서 임의 파싱하지 않는다.
- ADMIN은 `/subscriptions/checkout*`에서 안전하게 관리 화면으로 이동하고 `/api/payments/**` 사용자 mutation을 직접 호출해도 403이다.
- USER의 기존 결제 흐름과 공개 가입 흐름은 유지된다.
- 보안 단위·통합 테스트, 프론트 route 테스트, 관련 문서와 Evidence Pack이 완료된다.
Constraints/Forbidden:
- public signup을 막거나 CAPTCHA/외부 라이브러리를 추가하지 않는다.
- PII·JWT·IP 원문을 새 로그에 남기지 않는다.
- 실제 proxy/JWT 환경 증거 없이 X-02/X-03을 CLOSED 처리하지 않는다.
- 결제 정책·Provider 호출·클라이언트 worktree를 변경하지 않는다.
- 다른 작업자의 변경을 되돌리지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] endpoint별 허용량 이내 요청은 기존 응답을 유지하고 초과 요청은 429와 `Retry-After`를 반환한다.
- [ ] 서로 다른 식별자 fingerprint 및 신뢰된 서로 다른 client identity가 분리된다.
- [ ] 위조·다중·잘못된 내부 client header는 direct peer로 수렴한다.
- [ ] ADMIN checkout UI 진입과 payment mutation이 거부되며 USER는 정상 동작한다.
Performance:
- [ ] in-memory window cleanup과 key 수명은 설정된 최대 window에 맞춰 bounded 된다.
Quality:
- [ ] 관련 backend tests, frontend typecheck·test·lint·changed-file Prettier가 통과한다.
- [ ] API·보안·UI 문서가 코드와 일치하고 docs validation이 통과한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0/1 (Security - Required for pg):
- docs/policies/security-policy.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2 (Task Context):
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/ui/screen-flow.md
- docs/design/payment-integration-design.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-004-summary.md
- deliverables/user/WI-20260715-ATS-020-summary.md

Files:
- src/main/java/com/atstudio/atstudio/security/AuthRateLimitFilter.java
- src/main/java/com/atstudio/atstudio/config/AuthRateLimitProperties.java
- src/main/java/com/atstudio/atstudio/security/TrustedClientIdentityResolver.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/controller/UtilController.java
- src/main/java/com/atstudio/atstudio/controller/PaymentController.java
- frontend/src/router/index.tsx
- frontend/src/components/auth/ProtectedRoute.tsx
- src/test/java/com/atstudio/atstudio/security/AuthRateLimitFilterTest.java
- src/test/java/com/atstudio/atstudio/config/SecurityConfigTest.java
- frontend/src/router/index.test.tsx

Repro/Logs:
- `gradlew.bat test --tests "*AuthRateLimit*" --tests "*SecurityConfig*" --tests "*PaymentController*"`
- `npm test -- --run frontend/src/router/index.test.tsx` or repository-equivalent focused Vitest command

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-005-summary.md :
- Security behavior, compatibility, remaining X-02/X-03 environment evidence
Agent-facing -> deliverables/agent/WI-20260716-ATS-005-evidence-pack.md :
- Standard Evidence Pack with Tier references, changed files/lines, tests, rollback, follow-ups
Handoff Packet -> deliverables/agent/WI-20260716-ATS-005-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Focused backend/frontend tests plus changed-file lint/Prettier and docs validation
Rollback: Revert WI-owned source/test/doc files; preserve existing trusted-proxy and USER payment behavior.
