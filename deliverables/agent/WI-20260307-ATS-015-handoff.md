[WI HEADER]
WI ID: WI-20260307-ATS-015
REQ: REQ-20260307-ATS-008
Agent: docops
Depends On: WI-007~012 (Phase 1 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 3 — check 파일들 정합성 보완 (front-list, modal-list, screen-flow)
Scope (in):
  - docs/ui/atstudio-front-list.md
  - docs/ui/modal-list.md
  - docs/ui/screen-flow.md
  (이 3개 파일만)
Scope (out):
  - api-spec.md, usecase 파일 수정 금지 (WI-013/014 담당)
  - 백엔드 코드 수정 금지

Constraints/Forbidden:
  - 위 3개 파일 외 수정 금지
  - 아래 명시된 변경 외 임의 추가/삭제 금지

---

[ACCEPTANCE CRITERIA]

[atstudio-front-list.md]
- [ ] B-1 (음원 상세) 관련 API에 `4.1 POST /api/play-histories` 추가
- [ ] Screen 1 (메인화면) 관련 API에 `10.1 POST /api/likes/{trackId}`, `11.1 POST /api/download-queue` 추가
- [ ] Screen 3 (음원 목록) 관련 API에 `10.1 POST /api/likes/{trackId}`, `11.1 POST /api/download-queue` 추가
- [ ] Screen 15 (문의글 보기) 관련 API에 `8.7 DELETE /api/questions/{id}` 추가
- [ ] K-7 (트랙 관리) 관련 API에 `1.8 GET /api/tracks/admin` 추가 (비활성 포함 전체 목록)
- [ ] 버전 v3 → v4, 날짜 2026-03-07 업데이트

[modal-list.md]
- [ ] M-17 API 컬럼: `13.5 PUT /api/company-certifications/{id}/review` → `13.5 PUT /api/company-certifications/{certificationId}` (/review suffix 제거)
- [ ] M-17 컴포넌트: StatusModal → ReviewModal (신규 컴포넌트 타입 — 상태 선택 드롭다운 + adminNote 입력 + 확인 버튼)
- [ ] Component Classification 섹션에 ReviewModal 정의 추가: "관리자 심사 처리 (상태 선택 + 메모 입력)" | 상태 드롭다운 + 텍스트 입력 + `[취소]` `[처리]` 2-button
- [ ] M-19 발생 화면: "Screen 21/22" → "Screen 22"
- [ ] M-20 API 컬럼: `8.4 DELETE /api/questions/{id}` → `8.7 DELETE /api/questions/{questionId}`
- [ ] M-22 API 컬럼: `DELETE /api/download-queue/{id}` → `DELETE /api/download-queue/{trackId}`
- [ ] Section 3 Flow 4 (비밀번호 변경): 성공 응답 `200 OK` → `204 No Content`
- [ ] 버전 v1.1 → v1.2, 날짜 2026-03-07 업데이트
- [ ] frontmatter dependencies의 atstudio-front-list.md 버전 v3 → v4로 업데이트 (위 front-list 변경 후)

[screen-flow.md]
- [ ] §1 [관리자] GNB에 "앨범" 항목 추가 (예: "메인 / 앨범 / 음원관리 / 앨범관리 / 관리자대시보드 / 로그아웃")
- [ ] §2 인증 흐름 로그아웃 부분에 방식 명시:
      "[로그아웃] 클라이언트 측 토큰 삭제 (서버 엔드포인트 없음, 초기 버전) → [1 메인] (비로그인 상태)"
- [ ] §7 구독 흐름 다운그레이드 경로에서 `(pendingSubscriptionId TODO T-3)` 레이블 제거 (T-3 완료 반영)
- [ ] §11 전역 내비게이션 패턴 표의 404/500 항목:
      "API 에러 404 | [404 에러 페이지]" → "[ERR-1 404 Not Found 에러 페이지]"
      "API 에러 500 | [500 에러 페이지]" → "[ERR-2 500 Server Error 에러 페이지]"
- [ ] 헤더 의존 문서 버전: "modal-list.md v1" → "modal-list.md v1.2" (위 변경 후)
- [ ] 버전 v1.1 → v1.2, 날짜 2026-03-07 업데이트

Quality:
- [ ] 변경 항목 전부 반영 확인
- [ ] 기존 서식/형식 일관성 유지

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

Files (수정 대상 — 이 목록만):
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/ui/screen-flow.md

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-015-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-015-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-015-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 항목별 파일:라인 포인터 포함
