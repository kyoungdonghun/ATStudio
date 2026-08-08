---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: docops
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-006-handoff.md
    reason: Approved Work Item scope and constraints
  - path: ../agent/WI-20260808-ATS-006-evidence-pack.md
    reason: Detailed document and validation evidence
  - path: REQ-20260808-ATS-002.md
    reason: Approved request and acceptance criteria
---
# WI-20260808-ATS-006 Work Summary

## 완료 결과

- `SR-96`: 현재 자기 강등과 마지막 관리자 강등이 가능하다는 사실을 기록하고, 서버 불변조건, 동시 요청 직렬화, 현재 DB 역할 기반 인증과 stale SPA 역할의 차이, 감사·복구 요구를 정리함.
- `SR-97`: 현재 플랜 변경은 불가능하고 상태·주기·만료일만 관계 검증 없이 저장된다는 사실을 기록함. `CANCELLED`의 미래 만료일은 정상 유예 기간으로 보존하고, `EXPIRED`의 미래 만료일은 거절하는 행렬과 일반 관리자 권한 보정 흐름을 권고함.
- `SR-98`: 실환경 `track 1`의 564×1404px 근거를 기록하고, 현재 현상이 왜곡이 아니라 정사각형 `cover` 잘림임을 구분함. 단일 운영자 환경에는 1:1 업로드 계약, 실제 카드와 같은 미리보기, 서버 비율 검증을 1차안으로 권고함.
- 세 SR을 모두 `OPEN`으로 인덱싱함.

## 핵심 판단

1. 관리자 보호는 화면 선택지 제한만으로 닫을 수 없고, 모든 역할 변경이 공유하는 서버 직렬화 지점과 적용 직전 요청자·관리자 수 재검사가 필요함.
2. 구독 플랜 선택은 단순 DB 필드 편집으로 추가하면 안 됨. 현재 환불 연계 보정의 미리보기·잠금·감사 패턴을 일반 운영 교정으로 확장하는 안을 우선 권고함.
3. 썸네일은 강제 늘림이나 복잡한 크롭 도구보다 1:1 규격 안내와 실제 `cover` 결과 미리보기를 먼저 제공하는 편이 현재 운영 규모에 적합함.

## 남은 결정 사항

- 역할 변경 직렬화 방식, 강등 시 세션 무효화 수준, 운영 관리자 복구 수단
- 구독 빠른 편집과 일반 권한 보정의 경계, 승인 수준, 플랜 변경의 적용 시점과 결제 의미
- 썸네일 2048×2048px의 권장/필수 범위와 기존 비정사각형 이미지 처리 방식

## 인덱스·품질 확인

- SR 파일: 97개
- SR 인덱스: 97행, `DONE 82 / OPEN 12 / NOT CONFIRMED 2 / DROPPED 1`
- 인덱스 파일을 제외한 `docs/**/*.md`: 199개
- 신규 SR의 로컬 Markdown 링크: 통과
- Unicode 대체 문자 검사: 없음
- `git diff --check`: 공백 오류 없음. 기존 CRLF 파일의 향후 LF 변환 경고만 확인됨

## 변경 파일

- `docs/SR/SR-96.md`
- `docs/SR/SR-97.md`
- `docs/SR/SR-98.md`
- `docs/SR/index.md`
- `docs/index.md`
- `deliverables/user/WI-20260808-ATS-006-summary.md`
- `deliverables/agent/WI-20260808-ATS-006-evidence-pack.md`

제품 코드, DB, 기존 SR 본문과 사용자 파일은 변경하지 않음.
