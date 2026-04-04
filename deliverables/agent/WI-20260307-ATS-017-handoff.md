[WI HEADER]
WI ID: WI-20260307-ATS-017
REQ: REQ-20260307-ATS-008
Agent: cr
Depends On: WI-013/014/015 (Phase 3 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 4 — Track/Tag 도메인 코드 정합성 체크 (Phase 3 문서 기준)
Scope (in):
  - TrackController.java + TrackService.java 코드 체크
  - TrackRepository.java 코드 체크
  - TagController.java + TagService.java 코드 체크
  - 이 5개 파일(+관련 DTO/Entity) 만
Scope (out):
  - 코드 수정 금지
  - 다른 도메인 파일 탐색 금지
  - 문서 수정 금지

Constraints/Forbidden:
  - 발견 보고만. 코드 수정 절대 금지.

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] TrackController: GET /api/tracks/admin 엔드포인트 존재 여부 (§1.8, BD-2)
       존재하지 않으면 [GAP] CRITICAL 보고
       존재한다면: Auth=[ADMIN] 처리 여부, is_active optional 파라미터 지원 여부
- [ ] TrackService: 비활성 트랙 포함 전체 목록 조회 메서드 존재 여부
       기존 Track 목록 API(GET /api/tracks)는 is_active=true 필터 적용 — Admin API는 is_active 파라미터로 필터하거나 전체 반환
- [ ] TrackController/TrackRepository: is_active 필터 쿼리 구현 방식 확인
- [ ] TagController: DELETE /api/tags/{id} Auth=[ADMIN] 여부 (front-list K-6 참조)
- [ ] TrackService/TrackRepository: N+1 @EntityGraph 적용 상태 확인 (기존 이슈 WI-006 MINOR — 현황 보고)

Quality:
- [ ] 발견 항목별 파일:라인 포인터 포함
- [ ] CONFLICT/GAP/OMISSION/SUGGESTION 형식 준수

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (cr 필수):
- docs/policies/security-policy.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

참조 문서 (코드와 대조 기준):
- docs/design/api-spec.md  ← §1 (Tracks), §1.8 (GET /api/tracks/admin), §2 (Tags)
- docs/ui/atstudio-front-list.md  ← K-7 (트랙 관리 화면)

Files (검사 대상):
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
- src/main/java/com/atstudio/atstudio/controller/TagController.java
- src/main/java/com/atstudio/atstudio/service/TagService.java
- src/main/java/com/atstudio/atstudio/dto/  (관련 DTO — TrackResponse 등)

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-017-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-017-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-017-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 발견 항목별 파일:라인 포인터 포함
Format:
  [CONFLICT] 상충: (코드 파일:라인) vs (문서:섹션) — 설명
  [GAP]      누락: 문서에는 있으나 코드에 없음 — 설명
  [OMISSION] 미흡: 부분 구현 — 설명
  [SUGGESTION] 제안: 개선 가능 — 설명
심각도: CRITICAL / MAJOR / MINOR / SUGGESTION
