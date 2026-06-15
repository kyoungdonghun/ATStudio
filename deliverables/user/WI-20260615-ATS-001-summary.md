# WI-20260615-ATS-001 Summary

## 작업 요약
- 결제, 음원 검색 태그, 화이트리스트 채널 기능 이후 코드, DB 스키마, 설계 문서, UI 문서, 작업 산출물의 현행 정합성을 재점검했다.
- `schema.sql` 변경이 기존 로컬/운영 DB에 자동 반영되지 않는 점을 명확히 문서화하고, 기존 MySQL DB에 적용할 수 있는 수동 검토용 SQL 패치를 추가했다.
- 관리자 화이트리스트 CSV export에서 화면 검색어가 export note로 잘못 전달되던 프론트/API 문서 불일치를 수정했다.

## 주요 결과
- `src/main/resources/schema.sql` 헤더와 테이블 수 주석을 현재 기준으로 갱신했다.
- `src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql`을 추가했다.
- `docs/design/db-schema.md`, `docs/payment/*`, `docs/SR/SR-93.md`, 주요 index/registry 문서에 DB 반영 절차와 현재 기준을 보강했다.
- `docs/design/api-spec.md`, `docs/design/usecase/whitelist.md`에 화이트리스트 CSV export가 status 기반이며 keyword filter는 목록 조회에만 적용된다는 점을 명확히 했다.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx`에서 keyword를 export note로 넘기던 호출을 제거했다.

## 검증
- `.\gradlew.bat test` 통과
- `npm run typecheck` 통과
- `npm run lint` 통과
- `npm run build` 통과
- `python .agents\skills\validate-docs\scripts\validate_docs.py` 통과
- `git diff --check` 통과
- API count 145, schema table count 38, docs index total 184 확인

## 주의 사항
- 실제 로컬/운영 DB에는 아직 DDL을 적용하지 않았다.
- `src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql`은 자동 실행 파일이 아니라, 기존 DB를 갱신하기 전 검토 후 수동 적용하는 기준 파일이다.
- 운영 DB나 공유 DB에 적용하기 전에는 반드시 백업본 또는 스테이징 DB에서 먼저 실행해야 한다.
