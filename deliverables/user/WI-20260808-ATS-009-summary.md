---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: tr
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-009-handoff.md
    reason: Approved Work Item scope and constraints
  - path: ../agent/WI-20260808-ATS-009-evidence-pack.md
    reason: Detailed code and runtime evidence
  - path: REQ-20260808-ATS-003.md
    reason: Approved request and acceptance criteria
---
# WI-20260808-ATS-009 Work Summary

## 확인 결과

- 메인 화면은 코드와 공개 화면 모두 `GENRE`와 `MOOD`만 요청·표시한다. `INSTRUMENT`와 `USAGE` 탐색 섹션은 없다.
- 공개 태그 API에는 `GENRE` 5개, `MOOD` 4개, `INSTRUMENT` 4개, `USAGE` 1개가 실제로 존재한다.
- 공개 음원 세 건 중 두 건에 `INSTRUMENT:instrument03`이 연결되어 있으며, `GET /api/tracks?instrument=instrument03`은 두 건을 정상 반환한다. 즉, 악기 검색 백엔드는 현재 데이터로 작동한다.
- `USAGE` 태그 `#비가오면`은 등록되어 있지만 활성 음원에 연결된 건은 없어 해당 태그 검색 결과는 0건이다. `USAGE` 데이터가 없다고 볼 수는 없지만, 바로 홈에 노출하면 결과 없는 진입점을 만들게 된다.
- 공개 음원 목록 화면은 `usage` URL·필터를 처리하지만 `instrument`는 처리하지 않는다. 공통 API 함수와 백엔드는 `instrument`를 지원하므로, 홈에 악기 탐색만 추가하면 목록 화면에서 조건이 유실되는 교차 레이어 결함이 생긴다.

## 우선순위 판단

1. `USAGE`를 탐색 정보 구조의 첫 번째 범주로 둔다. ATStudio는 쇼츠용 음원을 찾는 서비스이고, `Usage Guide Tag`는 콘텐츠 활용 장면을 설명하는 공개 검색 태그라는 기존 계약이 있으므로 구매자의 목적 기반 탐색에 가장 직접적이다.
2. `INSTRUMENT`도 빠짐없이 제공하되 보조 속성으로 배치한다. 현재 실제 음원 연결과 백엔드 검색이 있으므로 제거할 근거는 없지만, 먼저 공개 목록 화면의 `instrument` URL·칩·초기화 계약을 완성해야 한다.
3. 현재 `USAGE` 연결 음원이 0건인 상태는 UI 누락과 별도의 콘텐츠 준비 문제로 다룬다. 결과가 없는 태그를 정상 탐색 진입점처럼 노출하지 않는다.

## 권고 정보 구조

- 홈에 네 개의 세로 섹션을 쌓는 대신 하나의 `태그별 탐색` 모듈을 두고 `용도 → 장르 → 분위기 → 악기` 탭으로 전환한다.
- `용도`를 첫 탭으로 배치한다. 결과가 있는 용도 태그가 없을 때는 빈 상태를 명시하거나 첫 번째 결과 보유 범주를 기본 선택하되, 용도 탭 자체를 silently 삭제하지 않는다.
- 탭 안에는 활성 음원으로 이어지는 태그만 우선 노출한다. 등록된 태그와 실제 검색 가능한 태그를 구분하기 위해 `/api/tags/available` 기반의 가용성 계약을 사용하거나 동등한 서버 응답을 정의한다.
- 모바일에서는 범주 탭을 가로 스크롤 가능한 단일 행으로 제공하고, 태그 칩은 제한된 개수와 `더보기`를 사용해 세로 과밀화를 막는다.
- 태그 선택 후 `/tracks?usage=...`, `/tracks?genre=...`, `/tracks?mood=...`, `/tracks?instrument=...`로 이동하고 목록 화면에서 선택 상태·검색 요청·초기화가 동일하게 유지되어야 한다.

## 대안 비교

- 네 개의 독립 섹션: 구현 재사용은 쉽지만 홈이 길어지고 모바일 과밀화와 빈 섹션 문제가 커서 비권고.
- 단일 통합 탭: 네 범주를 모두 보존하면서 화면 높이를 고정하고 `USAGE` 우선순위를 표현할 수 있어 권고.
- 범주 구분 없는 혼합 태그 구름: 가장 작지만 장르·용도·악기의 의미가 섞여 검색 결과를 예측하기 어려워 비권고.

## SR-04와의 경계

- `SR-04`는 장르 섹션만 있던 당시 동일한 구조의 분위기 섹션을 추가한 완료 기록이다.
- 신규 `SR-100`은 `SR-04`를 재개하거나 실패로 판정하지 않는다. 이미 제공된 장르·분위기를 유지하면서 네 태그 유형을 하나의 정보 구조로 재편하고, `USAGE` 우선순위·`INSTRUMENT` 검색 연결·빈 상태·모바일 밀도를 새 범위로 다룬다.

## 후속 검증 항목

- 홈 네 범주 로드·표시와 `USAGE` 우선 탭
- 결과 없는 `USAGE` 및 태그가 전혀 없는 범주의 빈 상태
- `instrument`·`usage` URL 복원, API 전달, 선택 칩, 전체 초기화
- 공백·`#` 등 URL 인코딩이 필요한 태그명 탐색
- 모바일 탭 스크롤, 태그 `더보기`, 키보드·스크린리더 탭 동작
- 여러 태그 선택 시 기존 AND 검색 의미 유지

## 변경 파일

- `deliverables/user/WI-20260808-ATS-009-summary.md`
- `deliverables/agent/WI-20260808-ATS-009-evidence-pack.md`

제품 코드, SR, 인덱스, DB 태그·음원 데이터와 공개 환경은 변경하지 않음.
