# WI-20260711-ATS-014 문서 품질 검증 요약

## TL;DR

- 문서 validator는 종료 코드 `0`으로 통과했습니다. Tier 0 문서 존재, Markdown 내부 링크, 지원 형식의 추적 ID 295개 매칭, 문서 인덱스 포함 여부가 모두 PASS였습니다.
- `git diff --check`도 종료 코드 `0`이며 공백 오류는 없었습니다. 다만 추적 중인 문서 6개에 LF가 향후 Git 처리 시 CRLF로 바뀔 수 있다는 줄바꿈 경고가 출력됐습니다.
- 별도 읽기 전용 수량 점검에서 인덱스 드리프트 1건을 확인했습니다. `docs/index.md`는 Standards 13개와 총 185개를 기재하지만, 동기화 스킬의 비재귀 집계 규칙으로는 Standards 12개와 총 184개입니다.
- 기존 문서나 소스는 수정하지 않았고, 이 WI의 summary/evidence 두 파일만 작성했습니다.

## 검증 결과

| 검증 | 명령 | 종료 코드 | 판정 |
|---|---|---:|---|
| 문서 validator | `python .agents/skills/validate-docs/scripts/validate_docs.py` | 0 | PASS |
| Git 공백 검사 | `git diff --check` | 0 | PASS, 줄바꿈 경고 6건 |
| 문서 인덱스 수량 점검 | `/sync-docs-index --check` 절차를 PowerShell 읽기 전용 집계로 재현 | 0 | MISMATCH 1건 |

### Validator 적용 범위

- Tier 0 필수 문서 4개의 존재를 확인했습니다.
- Markdown 링크 구문으로 표현된 내부 경로에서 깨진 링크를 찾지 못했습니다.
- 지원 정규식에 맞는 REQ/WI/STD ID 295개를 매칭했습니다.
- validator 기준으로 모든 문서가 루트 또는 상위 카테고리 인덱스에 포함됐습니다.

이 PASS는 frontmatter 필수 필드/허용값, 내용 최신성, 외부 링크, 코드 스팬의 경로, ID 대상 문서의 실제 존재, 인덱스 수량을 검증하지 않습니다. 특히 인덱스 검사는 이름/경로 문자열 포함 여부를 확인할 뿐 Document Overview의 숫자를 비교하지 않습니다.

### `git diff --check` 해석

- trailing whitespace 등 Git이 오류로 판정한 공백 문제는 0건입니다.
- LF→CRLF 경고 대상은 `docs/client/0-site-policy.md`, `docs/client/4-sr-format.md`, `docs/client/5-ai-prompt.md`, `docs/client/index.md`, `docs/client/testing-guide.md`, `docs/index.md`입니다.
- 이 명령은 기본적으로 추적된 diff를 검사하므로 현재 untracked 파일의 내용까지 검증한 결과는 아닙니다.

## 인덱스 드리프트

| 항목 | 인덱스 값 | 실제 값 | 결과 |
|---|---:|---:|---|
| Standards | 13 | 12 | MISMATCH |
| 전체 합계 | 185 | 184 | MISMATCH |

나머지 13개 카테고리 행은 현재 작업 트리의 Markdown 파일 수와 일치했습니다. `docs/standards/public_data/standard_glossary/README.md`는 하위 디렉터리에 있으므로 Standards를 비재귀로 세는 동기화 스킬 규칙에는 포함되지 않습니다. 이 WI의 제약에 따라 인덱스는 수정하지 않았습니다.

## 후속 입력

- `WI-20260711-ATS-019`에서 Standards 집계 규칙을 비재귀 12개로 유지할지, 참조 자산의 하위 Markdown까지 포함하도록 계약을 바꿀지 결정한 뒤 인덱스를 정정해야 합니다.
- 줄바꿈 경고는 공백 오류와 구분해 다뤄야 하며, 이 WI의 PASS를 줄바꿈 정규화 완료로 해석하면 안 됩니다.
