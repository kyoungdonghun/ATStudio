---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-002-handoff.md
    reason: Approved Work Item scope and constraints
  - path: ../agent/WI-20260808-ATS-002-evidence-pack.md
    reason: Detailed evidence and verification results
  - path: REQ-20260808-ATS-001.md
    reason: Approved request and acceptance criteria
  - path: ../agent/WI-20260808-ATS-001-evidence-pack.md
    reason: SR authoring evidence independently reviewed by this WI
---
# WI-20260808-ATS-002 Work Summary

## 결과

- `SR-94`와 `SR-95`를 React/Spring 예외 계약, DB 스키마, 태그 설계 문서, 공식 Splice·Epidemic Sound 자료와 독립적으로 교차 검증함.
- `SR-95`의 현행 입력 제약, 외부 사례, 권고안과 미결정 사항 구분은 근거와 일치하여 수정하지 않음.
- `SR-94`에서 동시 요청 경합의 최종 방어선을 `TAG_NAME_DUPLICATED`로 단정한 부분 1건을 실제 계약에 맞게 보정함.
- SR 인덱스와 전체 문서 인덱스의 수량·상태·링크는 정확하여 수정하지 않음.
- 코드, DB, 정책, 기존 SR, WI-001 산출물과 기존 ZIP은 변경하지 않음.

## SR-94 보정 내용

- 일반적인 중복 요청은 서비스의 `existsByName` 검사에서 `409 TAG_NAME_DUPLICATED`로 처리됨.
- 그러나 두 요청이 사전 검사를 동시에 통과하면 DB의 `uq_tags_name` 제약이 최종적으로 충돌을 막고, 현재 공용 예외 처리는 이를 `409 DATA_INTEGRITY_VIOLATION`으로 응답할 수 있음.
- 따라서 SR에 서버 사전 검사, DB 최종 무결성 방어, 태그 이름 제약 충돌의 `TAG_NAME_DUPLICATED` 변환 요구를 각각 분리하여 기록함.
- 프론트엔드는 상태 코드만이 아니라 도메인 오류 코드를 기준으로 필드 안내를 표시하고, 모달·입력값·목록·필터 상태를 보존하도록 유지함.

## SR-95 검증 결과

- 프론트엔드는 최대 50자와 제출 시 `trim()`만 적용하며, 내부 공백·연속 공백·특수문자 규칙은 없음.
- 백엔드는 `@NotBlank`, 최대 50자만 검사하고, 서비스는 전달된 이름으로 중복 조회와 저장을 수행함.
- `tags.name`은 태그 유형과 무관한 전역 유일 값으로 선언되어 있음.
- Splice와 Epidemic Sound의 공식 장르 목록에서 공백, `/`, `&`, `-`, 괄호, 아포스트로피, 숫자를 포함한 실제 명칭을 확인함.
- 앞뒤 공백 제거, 내부 공백 허용, 연속 공백 정규화, 제한적 문장부호 allowlist는 권고로 유지하고 대소문자·Unicode·`#`·유일성 범위·기존 데이터 충돌은 후속 결정으로 유지함.

## 검증 결과

- `validate_docs.py`: 통과 — Tier 0, 내부 링크, 추적 ID, 문서 인덱스 오류 없음.
- SR 파일 수와 인덱스 행 수: 각각 94개로 일치.
- 상태 집계: 82 `DONE`, 9 `OPEN`, 2 `NOT CONFIRMED`, 1 `DROPPED`.
- `docs/index.md` 범주 합계와 선언 총계: 모두 196.
- 새 SR의 로컬 Markdown 링크: 누락 없음.
- `git diff --check`: 종료 코드 0, 공백 오류 없음.

## 기존 기준선 경고와 새 결함 구분

- 기존 기준선 문서 오류·경고: 없음.
- 참고 알림: Git이 두 기존 수정 인덱스 파일의 CRLF가 향후 LF로 변환될 수 있다고 알렸으나, `git diff --check` 오류는 아니며 이번 WI에서 줄바꿈을 일괄 변경하지 않음.
- 새로 발견한 결함: SR-94의 동시성 경합 설명 1건이며, 허용 범위 안에서 수정 완료함.

## 남은 결정 사항

- SR-95에 남긴 대소문자, Unicode 정규화, 문장부호 allowlist, `#`, 유일성 범위와 기존 데이터 마이그레이션 정책.
- 구현 시 `uq_tags_name` 충돌만 안전하게 식별하여 `TAG_NAME_DUPLICATED`로 변환하는 구체적인 백엔드 방식.
