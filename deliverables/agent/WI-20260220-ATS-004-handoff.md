[WI HEADER]
WI ID: WI-20260220-ATS-004
REQ: REQ-20260220-ATS-001
Agent: se (Software Engineer)
Depends On: WI-20260220-ATS-003 (se)
Blocks: WI-20260220-ATS-007 (qa), WI-20260220-ATS-008 (re)

[WI SUMMARY]
Why: Auth 인증 기능 구현 — 로그인, 소셜 로그인, 토큰 재발급 (AuthController + AuthService + OAuth2Service + auth DTOs)
     WI-005(User)와 파일 충돌 없이 병렬 실행 가능하도록 역할 분리됨.

Scope:
  In:
    - POST /api/auth/login (일반 로그인)
    - POST /api/auth/social/{provider} (소셜 로그인 - GOOGLE/KAKAO/NAVER)
    - POST /api/auth/refresh (토큰 재발급)
    - AuthController, AuthService, OAuth2Service
    - auth DTOs: LoginRequest, AuthResponse, RefreshRequest, SocialLoginRequest, SocialAuthResponse
  Out:
    - /api/users/* (WI-005 담당)
    - /api/utils/* (WI-005 담당)
    - 테스트 작성 (WI-008 담당)

DoD:
  - 3개 Auth 엔드포인트 구현 완료
  - gradlew.bat compileJava 성공
  - ResponseDTO wrapper 사용 (기존 표준 준수)

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md
Tier 2: deliverables/agent/WI-20260220-ATS-001-evidence-pack.md, deliverables/agent/WI-20260220-ATS-002-evidence-pack.md, deliverables/agent/WI-20260220-ATS-003-evidence-pack.md, docs/standards/dto-standards.md

[OUTPUT CONTRACT]
deliverables/user/WI-20260220-ATS-004-summary.md
deliverables/agent/WI-20260220-ATS-004-evidence-pack.md
