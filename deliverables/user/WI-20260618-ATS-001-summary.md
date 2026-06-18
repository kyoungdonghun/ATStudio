# WI-20260618-ATS-001 Summary

## 작업 요약

- 기업회원 인증 흐름을 신청 / 보완요청 재제출 / 반려 후 새 신청 / 관리자 상세 심사 / 문서 다운로드 구조로 확정했다.
- 기업 인증 서류를 개별 파일 메타데이터로 관리하기 위해 `company_certification_documents` 테이블과 엔티티를 추가했다.
- 관리자 문서 다운로드 API를 추가하고, `/uploads/company-docs/**` 직접 접근은 관리자 권한으로 제한했다.
- 사용자 화면은 상태별 CTA를 보강했다: 보완요청 재제출, 반려 후 새 신청, 승인 후 구독 이동.
- 관리자 화면은 목록에서 상세 모달로 들어가 신청자 정보, 제출 문서, 관리자 메모, 승인/보완요청/반려 처리를 할 수 있게 했다.
- 기업 구독 결제 진입 시 기업 인증 미승인 상태를 더 명확히 안내하고 인증 관리 화면으로 이동할 수 있게 했다.
- 전수 재점검 중 재제출 파일 정리 예외를 보강했다. 기존 파일은 DB 커밋 후 삭제하고, 롤백 시 새로 저장한 파일을 정리한다.
- 최신 인증 신청 조회는 `createdAt DESC, id DESC`로 tie-breaker를 명확히 했다.
- API spec / DB schema / usecase / UI flow / client test 문서를 현재 구현 기준으로 현행화했다.

## 수용 기준 체크

- [x] BUSINESS 사용자는 신규 인증 신청을 제출할 수 있다.
- [x] `PENDING`, `APPROVED`, `REVISION_REQUESTED` 상태의 중복 신규 신청은 막힌다.
- [x] `REVISION_REQUESTED` 상태에서는 같은 신청 건으로 서류 재제출 후 `PENDING`으로 복귀한다.
- [x] `REJECTED` 상태에서는 기존 기록을 보존하고 새 신청이 가능하다.
- [x] 관리자는 인증 상세에서 신청자 정보와 제출 서류 목록을 확인하고 승인 / 보완요청 / 반려를 처리할 수 있다.
- [x] 관리자만 기업 인증 문서를 다운로드할 수 있다.
- [x] 문서와 코드의 API count / table count / 상태 전이 설명을 현행화했다.

## DB 반영 메모

- `src/main/resources/schema.sql`은 신규 DB 기준 최신 구조로 업데이트했다.
- 기존 로컬/운영 DB용 수동 패치 파일을 추가했다: `src/main/resources/db/manual/20260618_company_certification_documents.sql`
- 2026-06-18 사용자 승인 후 로컬 DB(`atstudio`)에는 위 DDL을 적용했고, `company_certification_documents` 테이블 존재와 컬럼 구성을 확인했다.
- 운영 DB에는 적용하지 않았다. 운영 반영 시 같은 manual SQL을 백업 후 별도 승인 절차로 적용해야 한다.

## 검증 결과

- `gradlew.bat test` → 통과
- `gradlew.bat test --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest"` → 통과
- `cd frontend; npm run typecheck` → 통과
- `cd frontend; npm run lint` → 통과
- `cd frontend; npm run test` → 통과
- `cd frontend; npm run build` → 통과
- 변경한 프론트 파일 대상 `npx prettier --check ...` → 통과
- `python .agents/skills/validate-docs/scripts/validate_docs.py` → 통과
- `git diff --check` → 통과
- 로컬 DB manual DDL 적용 확인 → `company_certification_documents` 테이블 존재 확인
- 로컬 DB FK 확인 → `company_certification_documents` → `company_certifications` FK 존재 확인

## 참고

- `npm run format` 전체 검사는 기존 프론트 코드 153개 파일의 baseline 포맷 불일치 때문에 실패한다. 이번 WI에서 수정한 프론트 파일들은 별도 Prettier 체크를 통과했다.
- `frontend/tsconfig.tsbuildinfo`는 빌드 중 변경되었지만 기능 변경 파일이 아니므로 되돌렸다.
