---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: docops
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-001-handoff.md
    reason: Approved Work Item scope and constraints
  - path: ../agent/WI-20260808-ATS-001-evidence-pack.md
    reason: Detailed evidence and verification results
  - path: REQ-20260808-ATS-001.md
    reason: Approved request and acceptance criteria
---
# WI-20260808-ATS-001 Work Summary

## 결과

- 관리자 태그 중복 저장 오류를 정상적인 사용자 입력 피드백 흐름으로 개선하는 `SR-94`를 작성함.
- 태그 이름의 공백·특수문자 규칙을 현재 구현과 공식 음악 서비스 사례에 기반한 권고안으로 정리한 `SR-95`를 작성함.
- 두 SR을 `OPEN`으로 등록하고 SR 문서 집계를 94개, 전체 문서 집계를 196개로 동기화함.
- 코드, DB, 기존 태그 데이터, 활성 설계·정책 문서는 변경하지 않음.

## 핵심 판단

### 중복 저장 오류

- 백엔드는 이미 중복 태그 이름에 HTTP `409`, 오류 코드 `TAG_NAME_DUPLICATED`, 안전한 사용자 메시지를 반환하고 있으므로 이 계약을 유지해야 함.
- 프론트엔드는 현재 모든 저장 실패를 `Failed to save tag`라는 페이지 공용 오류로 바꾸어 모달과 목록을 제거함.
- 목록 기반 사전 중복 검사는 빠른 안내를 위해 추가하되, 동시 요청과 직접 API 호출을 고려하여 서버의 409 처리를 대체하지 않도록 요구함.
- 생성·수정 양쪽에서 필드 단위 메시지, 입력값·모달·목록·필터 상태 보존을 완료 조건으로 명시함.

### 공백·특수문자 규칙

- 앞뒤 공백 제거, 내부 단일 공백 허용, 연속 공백의 단일 공백 정규화를 우선 권고함.
- 음악 장르에는 `Hip Hop / R&B`, `Synth-Pop`, `R&B`, `Electronic Dance Music (EDM)`처럼 공백과 문장부호가 실제로 필요하므로 모든 특수문자 금지는 적절하지 않다고 판단함.
- 한글·영문자·숫자·공백을 기본으로 하고, 하이픈·앰퍼샌드·슬래시·아포스트로피·괄호를 초기 검토 후보로 둔 명시적 allowlist 방식을 권고함.
- 이 권고는 최종 정책 확정이 아니며, 대소문자, Unicode 정규화, `#`, 전역 유일성 범위, 기존 데이터 충돌은 후속 설계 결정으로 남김.

## 변경 파일

- `docs/SR/SR-94.md`
- `docs/SR/SR-95.md`
- `docs/SR/index.md`
- `docs/index.md`
- `deliverables/user/WI-20260808-ATS-001-summary.md`
- `deliverables/agent/WI-20260808-ATS-001-evidence-pack.md`

## 검증 결과

- SR 파일 수: 94개
- SR 인덱스 행 수: 94개
- 상태 집계: 82 `DONE`, 9 `OPEN`, 2 `NOT CONFIRMED`, 1 `DROPPED`
- 새 SR의 로컬 Markdown 링크: 모두 존재
- `git diff --check`: 통과
- 기존 추적되지 않은 REQ·WI 핸드오프와 스크린샷 ZIP: 보존

## 후속 결정 사항

- 태그 이름의 대소문자 중복 판정 방식
- Unicode 정규화 형식과 호환 문자 처리
- 문장부호 allowlist의 최종 목록
- `#`의 저장 값 포함 여부
- 태그 이름 유일성을 전역으로 유지할지 유형별로 변경할지 여부
- 정규화 적용 전 기존 데이터 충돌 및 마이그레이션 필요 여부

## 다음 단계

- `WI-20260808-ATS-002`에서 코드·예외 계약·문서·외부 근거의 교차 레이어 정합성과 Markdown 품질을 독립 검증함.
