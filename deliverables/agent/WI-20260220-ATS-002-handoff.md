[WI HEADER]
WI ID: WI-20260220-ATS-002
REQ: REQ-20260220-ATS-001
Agent: pg (Privacy Guardian)
Depends On: WI-20260220-ATS-001 (sa)
Blocks: WI-20260220-ATS-003 (se) — pg 승인 후 se 구현 시작

[WI SUMMARY]
Why: sa(WI-001)가 완성한 아키텍처 설계를 보안 관점에서 검토한다.
     JWT, BCrypt, CORS, CSRF, OAuth2 등 Auth 보안 요소에 취약점/미흡 사항이 없는지
     사전에 확인하여 se 구현 단계에서 보안 결함 없이 진행할 수 있도록 한다.

Scope:
  In:
    - JWT 설계 보안 검토 (HS256 적합성, TTL, Secret Key 환경변수 처리)
    - Refresh Token DB 저장 방식 검토 (BCrypt 해시 저장 적합성, VARCHAR(512) 충분 여부)
    - Spring Security 6 SecurityFilterChain 보안 검토 (CSRF/CORS 설정, 엔드포인트 접근 규칙)
    - OAuth2 소셜 로그인 플로우 보안 검토 (Authorization Code 유출 방지, State 파라미터)
    - 비밀번호 정책 검토 (BCryptPasswordEncoder 강도)
    - 회원탈퇴 소프트 딜리트 시 refresh_token 무효화 검토
    - 보안 리스크 항목 식별 및 개선 권고 목록 작성
  Out:
    - 실제 코드 수정 (se 담당)
    - 기능 구현

DoD:
  - 보안 검토 보고서 작성 (승인/수정 필요 항목 명시)
  - 치명적 보안 결함 없음 확인 OR 수정 필요 사항 명확히 기술
  - se WI-003/004/005/006이 참조할 "보안 구현 지침" 포함

Constraints/Forbidden:
  - 코드 파일 수정 금지 (검토 및 보고서 작성만)
  - "수정 필요" 판정 시 구체적 개선 방안 함께 제시 (sa 재작업 불필요 수준으로)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] JWT 설계 (HS256, TTL, Secret Key 관리)에 보안 승인 또는 구체적 수정 의견 제시
  - [ ] Refresh Token BCrypt 해시 저장 방식 보안 승인 또는 대안 제시
  - [ ] SecurityFilterChain CSRF/CORS 설정 승인 또는 수정 의견
  - [ ] OAuth2 Authorization Code 플로우 보안 승인 또는 취약점 지적
  - [ ] 비밀번호 처리 (BCrypt strength) 승인 또는 권고

Quality:
  - [ ] 보안 리스크 심각도 분류 (Critical/High/Medium/Low)
  - [ ] se 구현 시 반드시 지켜야 할 "보안 구현 체크리스트" 포함

[INPUT POINTERS]
Tier 0 (Constitution - Required):
  - docs/standards/core-principles.md

Tier 1 (Policies - pg required):
  - docs/policies/security-policy.md
  - docs/policies/access-control-policy.md

Tier 2 (Architecture to Review):
  - deliverables/agent/WI-20260220-ATS-001-evidence-pack.md  ← 검토 대상 설계 문서
  - deliverables/user/REQ-20260220-ATS-001.md                ← 승인된 REQ

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260220-ATS-002-summary.md:
  - 보안 검토 결과 요약 (승인/수정 필요 항목)
  - se 구현 전 반드시 반영해야 할 사항

Agent-facing → deliverables/agent/WI-20260220-ATS-002-evidence-pack.md:
  - 항목별 상세 보안 검토 결과
  - 리스크 목록 (심각도 포함)
  - se 구현 시 참조할 "보안 구현 체크리스트"
  - 추가 권고사항 (현재 단계에서 구현 권장/불필요 분류 포함)

Handoff Packet → deliverables/agent/WI-20260220-ATS-002-handoff.md:
  - 이 파일 (추적성용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 각 보안 의견에 근거 명시 (OWASP, 표준 등)
Tests: N/A (검토 단계)
Rollback: N/A
