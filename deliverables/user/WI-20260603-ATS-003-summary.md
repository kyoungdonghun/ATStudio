# WI-20260603-ATS-003 Summary

## 결과
- `api-spec.md`를 v17 / 145 APIs로 갱신했다.
- `db-schema.md`를 v12 / 38 tables로 갱신했다.
- whitelist usecase를 WL-001~WL-008 상태 기반 워크플로우로 재작성했다.
- UI 목록, screen-flow, modal-list, 클라이언트 시나리오, 사이트 정책, glossary, project registry/index 카운트를 현행화했다.

## 검증
- `python .agents/skills/validate-docs/scripts/validate_docs.py` 통과.
- `git diff --check` 통과.

## 남은 주의사항
- 새 schema가 기존 DB에 자동 반영되지는 않는다. 서버 기동 전 새 컬럼/테이블 반영이 필요하다.
