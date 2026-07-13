# WI-20260711-ATS-001 문서 감사 요약

## TL;DR

- 감사 기준선은 `dev/kyoung`, HEAD `27d22446e5d21324dadcfcb322dbe51704dfe914`이다.
- 시작 시점 dirty worktree는 21개 경로였다. 감사 중 다른 작업자가 WI-002/003 산출물 4개를 추가했지만, `docs/client/` 9개 파일과 PDF의 SHA-256은 변하지 않았다.
- 문서 인덱스 수량은 정확하다. 카테고리 인덱스를 제외한 Markdown은 185개이며 `docs/index.md`의 모든 카테고리 수와 일치한다.
- 공식 문서 검증은 통과했다. Tier 0, Markdown 링크, 지원 형식 ID 284개, 문서 인덱스 등록이 모두 PASS다.
- 다만 검증기는 코드 스팬 경로와 ID 참조 대상의 존재 여부, 메타데이터 필수 필드/허용값을 검사하지 않는다. 따라서 PASS만으로 추적성과 최신성을 보장할 수 없다.

## 주요 발견

| 우선 | 분류 | 발견 | 근거 | 신뢰도 |
|---|---|---|---|---|
| P1 | 오래된 진술 | `development-standards.md`의 TypeScript 테스트 예시와 테스트 디렉터리/구조가 아직 Phase 2 `Planned`로 표시된다. React SPA와 Vitest는 이미 활성 상태이며 공존 테스트 파일도 14개다. | `docs/standards/development-standards.md:532`, `:664`, `:705`; `AGENTS.md:40-41`; `docs/standards/core-principles.md:210-213`; `frontend/package.json:10-11`, `:39` | 높음 |
| P1 | 확정 결함 | 문서 메타데이터 표준 준수율이 낮다. 201개 Markdown 중 119개는 frontmatter가 없고, 19개는 필수 `dependencies`가 없다. | `docs/standards/documentation-standards.md:45-47`, `:72-82`; 전수 스캔 명령 | 높음 |
| P1 | 확정 결함 + 모호성 | 레지스트리가 실제 산출물 위치와 어긋난다. 존재하지 않는 `docs/work-items/`, `docs/project/`를 가리키고 프로젝트 Repo는 `TBD`다. 현재 REQ/WI도 system-wide workboard에 없다. | `docs/registry/asset-registry.md:22`; `docs/registry/workboard.md:23-24`, `:36`, `:88-89`; `docs/registry/project-registry.md:29` | 높음 |
| P1 | 오래된 진술 + 모호성 | 화면 수 기준이 섞여 있다. 문서 인벤토리는 53개지만 활성 프론트 표준은 49개, 라우터 주석은 49+2, 실제 lazy page 컴포넌트는 54개다. 통계 대시보드는 여전히 API 미정의로 적혀 있으나 `GET /api/admin/stats`가 구현돼 있다. | `docs/index.md:71`; `docs/ui/atstudio-front-list.md:137`, `:161`; `docs/standards/frontend-standards.md:305`; `frontend/src/router/index.tsx:117`; `docs/design/api-spec.md:3647-3656`; `AdminStatsController.java:13-20` | 높음 |
| P1 | 확정 결함 | SR 인덱스 끝이 U+FFFD 문자로 손상됐고 SR-C 상태도 상단 완료 항목과 충돌한다. | `docs/SR/index.md:41-42`, `:104-105` | 높음 |
| P2 | 확정 결함 | PDF 본문은 현재 소스와 일치하지만 PDF Title 메타데이터의 한글이 물음표로 저장됐다. 검색/접근성/문서 속성 품질 문제다. | `output/pdf/atstudio-client-testing-guide.pdf`의 `/Title` 엔트리 | 높음 |
| P2 | 오래된 메타데이터 | 현재 2026-07-11 클라이언트 문서를 가리키는 `docs/index.md`의 `last_updated`는 2026-06-18로 남아 있다. | `docs/index.md:3`, `:85-88` | 높음 |
| P2 | 확정 결함 | `atstudio-front-list.md`에 구현된 사이트 설정 화면이 없다. 클라이언트 체크리스트와 라우터/API에는 존재한다. | `frontend/src/router/index.tsx:97`, `:209`; `docs/client/3-admin-checklist.md:105-106`; `docs/design/api-spec.md:3696`, `:3725`; 화면 인벤토리 검색 0건 | 높음 |

## 확인된 정상 항목

- 문서 수: 185개, 모든 카테고리 수 일치.
- Backend REST API: 24개 `@RestController`에서 method mapping 147개. `docs/design/api-spec.md:3760-3782`와 일치.
- DB: `schema.sql`의 `CREATE TABLE` 39개, JPA `@Entity` 39개. `docs/design/db-schema.md:1061-1105`와 일치.
- Agent: `.claude/agents/*.md` 13개. `docs/index.md:108`과 일치.
- PDF: 20페이지, 196,135 bytes, SHA-256 `5D5A743F9772362042EBCC5E29E3E8EC92AAE1F35EE680BBF0636823554D2DF5`.
- PDF 수록 소스 7개에서 검사한 실질 원문 397줄이 모두 PDF 텍스트에 존재했다. 전 페이지 렌더링 성공, 가장자리 잘림 신호 0건이다.

## 추적성 요약

- 시작 기준선: REQ 파일 46개, WI summary 179개, handoff 200개, evidence pack 178개, SR 파일 92개.
- 고유 WI ID 206개 중 167개만 summary/handoff/evidence 3종이 모두 존재했다. 나머지 39개는 과거 정책 차이, 미완료, 취소 미기록이 섞여 있어 일괄 결함 판정은 보류한다.
- `REQ-20260420-ATS-001`은 파일이 없는데 기존 Evidence Pack이 경로를 참조한다.
- 과거 후속 WI 5개는 참조만 있고 산출물이 없다: `WI-20260220-ATS-008`, `WI-20260226-ATS-026`, `WI-20260310-ATS-005`, `WI-20260517-ATS-013`, `WI-20260618-ATS-002`.
- `WI-20260711-ATS-006~008`은 현재 Phase 1이 차단하는 후속 계획이므로 이번 기준선에서는 결함으로 보지 않는다.

## 권장 조치

1. P1: `development-standards.md`의 세 `Planned` 표기와 화면/API 인벤토리를 현재 코드 기준으로 동기화한다.
2. P1: 레지스트리의 WI 위치, Repo, workboard 운영 범위를 현재 `deliverables/` 체계로 정리하고 누락된 과거 체인에 Done/Cancelled/Not-created 상태를 기록한다.
3. P1: 손상된 SR 인덱스와 SR-C 상태 충돌을 복구한다.
4. P2: 메타데이터 부채는 138개 문서를 한 번에 바꾸지 말고 카테고리별 WI로 나눠 적용한다. 동시에 `active/confirmed/accepted/reference`를 허용할지 표준을 먼저 결정한다.
5. P2: PDF 생성 절차와 소스 해시를 재현 가능하게 기록하고 Unicode Title 메타데이터를 수정한다.

## 미검증 외부 의존

운영 DB, 배포 환경, 실제 Toss/provider 상태, 실제 결제, 외부 URL의 현재 응답은 이번 read-only 문서 감사 범위에서 호출하지 않았다. 클라이언트 문서의 구현 의미 검증은 후속 WI-006~008의 3-way 감사 입력으로 넘긴다.
