[WI HEADER]
WI ID: WI-20260227-ATS-033
REQ: REQ-20260227-ATS-009
Agent: docops
Depends On: WI-20260227-ATS-032
Blocks: -

[WI SUMMARY]
Why: 백엔드 감사 Phase 4 — 최종 단계. WI-028~032의 모든 감사 결과를 하나의 Backend Audit Report로
     취합하여 docs/audit/backend-audit-report.md를 생성한다.
     이 보고서는 이후 수정 REQ 작성의 직접적인 인풋으로 사용된다.
Scope (in):
  - WI-028 (sa): 체크리스트 — 79 API, 21 테이블, 22 코딩 규칙, 14 도메인 비즈니스 규칙
  - WI-029 (cr-A): Track·License·Tag·Playlist·PlayHistory (CR-A-001~013)
  - WI-030 (cr-B): Subscription·Whitelist·DownloadQueue·Likes (CR-B-001~007)
  - WI-031 (cr-C): User·Auth·Inquiry·Notice·CompanyCert·Util (CR-C-001~016)
  - WI-032 (pg): SecurityConfig 79 API 전수·JWT·ResponseDTO (CR-P-001~009)
  - 최종 보고서: docs/audit/backend-audit-report.md
Scope (out): 코드 수정, 새로운 이슈 발굴 (취합만 수행)
DoD:
  - docs/audit/backend-audit-report.md 파일 생성
  - 전체 이슈를 ✅/⚠️/❌/📋 분류
  - ❌ 항목 전부 파일·라인 포인터 포함
  - 수정 우선순위별 그룹화 (CRITICAL → MAJOR → MINOR → SUGGESTION)
  - 도메인별 현황 요약표 포함
  - 수정 REQ 인풋으로 즉시 사용 가능한 수준
Constraints/Forbidden: 코드 수정 절대 금지. 새로운 이슈 추가 금지 (취합만). Read-only.

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] docs/audit/backend-audit-report.md 파일 생성 완료
  - [ ] WI-029~032 전체 이슈(CR-A/B/C/P) 누락 없이 포함
  - [ ] CRITICAL 이슈 최상단 강조
  - [ ] 이슈별 파일:라인 포인터 포함
  - [ ] 도메인별 판정 통계 요약표 포함
  - [ ] "다음 단계 권장 조치" 섹션 포함 (수정 REQ 작성 가이드)
Quality:
  - [ ] WI-031 summary의 deliverables/user/ 파일들과 판정 일치
  - [ ] 이슈 번호 체계 일관성 (CR-A/B/C/P)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (docops 필수):
  - docs/standards/documentation-standards.md
  - docs/standards/glossary.md

Phase 1 결과:
  - deliverables/agent/WI-20260227-ATS-028-evidence-pack.md  ← 체크리스트 기준 (79 API, 21 테이블, 22 규칙)

Phase 2 결과:
  - deliverables/agent/WI-20260227-ATS-029-evidence-pack.md  ← cr-A (CR-A-001~013)
  - deliverables/agent/WI-20260227-ATS-030-evidence-pack.md  ← cr-B (CR-B-001~007)
  - deliverables/agent/WI-20260227-ATS-031-evidence-pack.md  ← cr-C (CR-C-001~016)

Phase 3 결과:
  - deliverables/agent/WI-20260227-ATS-032-evidence-pack.md  ← pg (CR-P-001~009)

User-facing 요약 (참고):
  - deliverables/user/WI-20260227-ATS-029-summary.md
  - deliverables/user/WI-20260227-ATS-030-summary.md
  - deliverables/user/WI-20260227-ATS-031-summary.md
  - deliverables/user/WI-20260227-ATS-032-summary.md

REQ:
  - deliverables/user/REQ-20260227-ATS-009.md

[OUTPUT CONTRACT]
Primary deliverable → docs/audit/backend-audit-report.md :
  형식:
  # ATStudio Backend Audit Report
  ## Executive Summary (판정 통계, 도메인별 현황)
  ## CRITICAL Issues (즉시 수정 필수)
  ## MAJOR Issues (프론트 전 수정)
  ## MINOR Issues (권장 수정)
  ## SUGGESTION (추후 개선)
  ## Domain Compliance Matrix (도메인별 ✅/⚠️/❌)
  ## Recommended Next Steps (수정 REQ 작성 가이드)

User-facing  → deliverables/user/WI-20260227-ATS-033-summary.md
  형식: 보고서 생성 완료 확인, 이슈 통계, 다음 단계 안내

Agent-facing → deliverables/agent/WI-20260227-ATS-033-evidence-pack.md
  형식: 취합 과정, 이슈 카운트 검증, 생성된 파일 경로

[TRACEABILITY REQUIREMENTS]
Evidence: 각 이슈 출처 WI 번호 포함 (예: "CR-A-001 (WI-029)")
Rollback: Read-only + 신규 파일 생성 → 불필요
