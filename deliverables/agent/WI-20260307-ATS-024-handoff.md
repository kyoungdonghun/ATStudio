[WI HEADER]
WI ID: WI-20260307-ATS-024
REQ: REQ-20260307-ATS-009
Agent: cr
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Track 1-B — api-spec §5~9, §12~15 ↔ usecase 정합성 검증 (read-only)
Scope (in):
  - api-spec.md §5(User/Auth), §6(Subscription), §7(License), §8(Inquiry),
    §9(Notice), §12(Whitelist), §13(CompanyCert), §14(Util), §15(Album)
  - usecase: user-info.md, user-subscription.md, user-license.md, business-license.md,
    user-question.md, user-notice.md, whitelist.md, company-certification.md, util.md, sound-album.md
  - 검증 항목: UC ID별 API URL/Method/응답코드/권한/요청·응답 필드 일치 여부
Scope (out):
  - 파일 수정 금지 (발견·보고만)
  - §1~4, §10~11 (WI-023 담당)
  - 백엔드 코드 검토 (WI-026 담당)

DoD:
  - 9개 도메인 각각 불일치 항목 목록 산출
  - CRITICAL/MAJOR/MINOR/SUGGESTION 분류 명시
  - 발견 없으면 "PASS" 명시

Constraints/Forbidden:
  - 절대 파일 수정 금지
  - 판단 근거(api-spec 섹션 + usecase UC ID)를 증거로 명시

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] api-spec §5 User/Auth (5.1~5.11) ↔ user-info.md 교차 확인
- [ ] api-spec §6 Subscription (6.1~6.10) ↔ user-subscription.md 교차 확인
- [ ] api-spec §7 License (7.1~7.4) ↔ user-license.md, business-license.md 교차 확인
- [ ] api-spec §8 Inquiry (8.1~8.7) ↔ user-question.md 교차 확인
- [ ] api-spec §9 Notice (9.1~9.5) ↔ user-notice.md 교차 확인
- [ ] api-spec §12 Whitelist (12.1~12.4) ↔ whitelist.md 교차 확인
- [ ] api-spec §13 CompanyCert (13.1~13.5) ↔ company-certification.md 교차 확인
- [ ] api-spec §14 Util (14.1~14.8) ↔ util.md 교차 확인
- [ ] api-spec §15 Album ↔ sound-album.md 교차 확인
- [ ] 검증 항목: URL, HTTP Method, 상태코드, 권한(Auth), 주요 요청·응답 필드

Quality:
- [ ] 이슈 각각에 근거 포인터 (api-spec 섹션# + usecase UC-ID + 라인) 포함
- [ ] CRITICAL: API 존재 여부 불일치, 상태코드 오류, 권한 오류
- [ ] MAJOR: 필드명 불일치, 요청·응답 구조 불일치
- [ ] MINOR: 설명 누락, 예시 오류, 오탈자

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-009.md

API Spec (검증 기준):
- docs/design/api-spec.md  ← §5(User/Auth), §6(Subscription), §7(License), §8(Inquiry),
  §9(Notice), §12(Whitelist), §13(CompanyCert), §14(Util), §15(Album)

Usecase (검증 기준):
- docs/design/usecase/user-info.md
- docs/design/usecase/user-subscription.md
- docs/design/usecase/user-license.md
- docs/design/usecase/business-license.md
- docs/design/usecase/user-question.md
- docs/design/usecase/user-notice.md
- docs/design/usecase/whitelist.md
- docs/design/usecase/company-certification.md
- docs/design/usecase/util.md
- docs/design/usecase/sound-album.md

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-024-summary.md :
- 도메인별 발견 이슈 요약 (CRITICAL/MAJOR/MINOR/SUGGESTION 카운트)
Agent-facing -> deliverables/agent/WI-20260307-ATS-024-evidence-pack.md :
- 이슈별 상세 근거 포인터, 판단 기준, 수정 제안
Handoff Packet -> deliverables/agent/WI-20260307-ATS-024-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 이슈별 api-spec 섹션# + usecase UC-ID + 파일:라인 포인터 포함
Tests: 해당 없음 (read-only 검증)
