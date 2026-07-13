# WI-20260711-ATS-019 문서/운영 독립 리뷰 요약

## 결론

- **판정: FAIL / 후속 수정 필요.** 문서 링크 검사 자체는 통과했지만, 결제·DB 배포 전제, 클라이언트 운영 안내, API 응답 예시, 화면/재생기록 문서에 확인된 내용 오류가 남아 있다.
- 현재 저장소에서 **147 REST API, 39 DB 테이블, 13 agents**는 재확인됐다.
- **53 screens는 확정 수치로 유지할 근거가 부족하다.** 현재 router에는 lazy page component 54개(오류 화면 제외 52개), route element 행 62개가 있어 먼저 화면 집계 단위를 정해야 한다.
- 문서 총계는 `sync-docs-index`의 명시 규칙(Design만 재귀, 나머지 category는 직접 하위)을 적용하면 **184**다. Standards 13/총 185는 Standards의 nested README를 재귀 집계한 다른 규칙을 섞은 값이다.

## 우선 발견사항

| 우선순위 | 판정 | 핵심 내용 |
|---|---|---|
| P1 | 내용/운영 오류 | 기존 DB를 재현 가능하게 올리는 ordered migration chain이 없고, fresh schema의 payment audit ENUM은 설계/코드보다 뒤처진다. `schema.sql`의 v12/38 표기도 실제 v13/39와 다르다. |
| P1 | 내용/운영 오류 | 결제 문서는 "current delivery is complete"라고 표현하지만 탈퇴 시 자동결제 중단, billing-key issue/delete 보상, key rotation, scheduler timezone/replica 강제, MySQL 배포 검증이 release gate로 정리되지 않았다. |
| P1 | 운영 안내 오류 | 관리자 문서는 기업 인증 서류 다운로드/검토와 whitelist CSV 내보내기를 안내하지만 악성 파일 격리·안전 열람, CSV formula 방어, 검색 결과와 실제 status-wide export 범위를 설명하지 않는다. |
| P2 | API 계약 오류 | billing prepare/confirm/my/cancel 응답 예시가 현재 Java record와 TypeScript 계약과 다르다. URL/요청은 맞지만 응답 필드는 맞지 않는다. |
| P2 | 사용자 문서 오류 | 클라이언트 문서는 로그인 사용자의 서버 재생기록처럼 설명하지만 활성 SPA는 SR-89에 따라 비회원도 포함한 `localStorage` 기록을 사용한다. |
| P2 | 문서 체계 오류 | registry/workboard가 없는 `docs/work-items/`, `docs/project/`를 가리키고 실제 `deliverables/user/`, `deliverables/agent/` 및 현재 REQ/WI를 추적하지 않는다. |
| P2 | 인벤토리 오류 | 화면 목록은 admin stats API를 미정의로 표시하고 site settings 화면을 누락한다. 53이라는 수치의 집계 단위도 정의되지 않았다. |
| P2 | 문서 무결성 오류 | 201개 Markdown 중 119개가 frontmatter 없음, 19개가 required `dependencies` 누락, 4개가 현재 enum 밖의 status/category를 사용한다. |
| P2 | SR 인덱스 오류 | SR 파일/행 92개와 DONE 82개 집계는 맞지만, SR-C-01은 SR-35 DONE과 충돌하고 마지막 SR-C-02 행은 U+FFFD로 잘렸으며 `confirm/`은 비어 있다. |
| P3 | 산출물 품질 | 클라이언트 PDF 본문 drift는 재확인되지 않았지만 Title metadata가 깨졌고 재생성 명령/ordered source/hash provenance가 없다. |

## Validator와 집계 규칙 판정

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: **PASS**, exit 0, 지원 형식 ID 296개.
- 이 PASS는 네 가지 구현 검사에만 유효하다: Tier 0 파일 존재, Markdown link target 존재, 지원 ID 정규식 매치, broad index 문자열 포함 여부.
- **내용 오류가 validator의 false pass인 것은 아니다.** metadata, 문서 최신성, API/DB/화면 의미 정합성, bare/code-span 경로, ID 대상 존재, 숫자 집계는 validator 계약 밖이다.
- Standards 12 대 13은 validator 문제가 아니라 **집계 규칙 차이**다. root overview에는 sync skill의 구체 규칙에 따른 12/184를 사용하고, Standards category index는 nested reference까지 13개를 계속 나열할 수 있다.
- 화면 53 대 52/54/62도 **집계 계약 미정**이다. route, page component, conceptual screen 중 하나를 먼저 canonical unit으로 승인해야 한다.
- `git diff --check`: 오류 0. 여섯 tracked 문서의 LF→CRLF 메시지는 whitespace 오류가 아니라 line-ending 경고다.

## Canonical Source 결정

| 논쟁 사실 | 현재 판정 기준 |
|---|---|
| REST API 수/현재 응답 shape | `@RestController` method mappings와 Java response records. `api-spec.md`는 이를 반영해야 하는 설명 문서다. |
| DB fresh schema | JPA entity table set + `schema.sql`; 기존 DB upgrade는 별도 ordered migration chain이 기준이어야 하나 현재 부재한다. |
| 화면 수 | `frontend/src/router/index.tsx`가 구현 기준. 단, 수치화 단위는 아직 미결정이다. |
| 문서 category 수 | `.agents/skills/sync-docs-index/SKILL.md`의 category별 명시 규칙. |
| 재생기록의 현재 SPA 동작 | SR-89와 `frontend/src/store/playerStore.ts`; server API/use case는 유지/폐기 정책 결정 전 legacy drift로 본다. |
| 운영 준비 완료 | 문서 선언만으로 확정하지 않고 copied DB migration rehearsal, MySQL 검증, 배포 topology/config evidence가 필요하다. |

## 권장 수정 순서

1. **배포 차단 해소:** ordered DB baseline/migration, audit ENUM patch, schema v13/39 metadata, copied-MySQL 검증을 하나의 승인된 remediation 범위로 묶는다.
2. **운영 안전:** 결제 탈퇴/복구/key rotation/timezone/scheduler/maker-checker 절차와 기업 서류·whitelist export 안전 절차를 release gate에 추가한다.
3. **계약 정합성:** billing API 응답 예시와 재생기록 정책/클라이언트 문서를 현행 계약에 맞춘다.
4. **추적성:** registry/workboard를 `deliverables/*` 구조와 현재 REQ/WI lifecycle에 맞춘다.
5. **문서 위생:** 화면 집계 단위 확정, root count/date 수정, metadata category별 정비, SR index 복원, PDF metadata/provenance를 처리한다.

수정 구현은 이 WI 범위 밖이며, 본 WI에서는 기존 문서·코드·설정·PDF를 변경하지 않았다.
