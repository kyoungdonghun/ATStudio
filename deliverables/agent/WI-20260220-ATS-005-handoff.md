[WI HEADER]
WI ID: WI-20260220-ATS-005
REQ: REQ-20260220-ATS-001
Agent: se (Software Engineer)
Depends On: WI-20260220-ATS-003 (se)
Blocks: WI-20260220-ATS-007 (qa), WI-20260220-ATS-008 (re)

[WI SUMMARY]
Why: User 회원 기능 구현 — 회원가입, 내 프로필 관리, 소셜 프로필 완성, 유틸 API (UserController + UserService + UtilController + user DTOs)
     WI-004(Auth)와 파일 충돌 없이 병렬 실행 가능.

Scope:
  In:
    - POST /api/users (회원가입)
    - GET /api/users/me (내 프로필 조회)
    - PUT /api/users/me (내 프로필 수정)
    - DELETE /api/users/me (회원탈퇴 - soft delete)
    - PUT /api/users/me/complete-profile (소셜 프로필 완성)
    - GET /api/utils/check-email
    - GET /api/utils/check-phone
    - GET /api/utils/check-nickname (추가)
    - UserController, UserService, UtilController
    - user DTOs: RegisterRequest, UserResponse, UpdateProfileRequest, WithdrawRequest, CompleteProfileRequest
  Out:
    - /api/auth/* (WI-004 담당)
    - 테스트 (WI-008 담당)

DoD:
  - 7개 User/Util 엔드포인트 구현 완료
  - gradlew.bat compileJava 성공
  - BCrypt 비밀번호 해시 처리 (평문 저장 금지)
  - 회원탈퇴: is_deleted=1 + clearRefreshToken()

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md
Tier 2: deliverables/agent/WI-20260220-ATS-001-evidence-pack.md, deliverables/agent/WI-20260220-ATS-002-evidence-pack.md, deliverables/agent/WI-20260220-ATS-003-evidence-pack.md, docs/standards/dto-standards.md

[OUTPUT CONTRACT]
deliverables/user/WI-20260220-ATS-005-summary.md
deliverables/agent/WI-20260220-ATS-005-evidence-pack.md
