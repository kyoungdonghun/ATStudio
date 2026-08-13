# WI-20260809-ATS-047 교정 상태 요약

## 결과

문의 삭제, 첨부파일 다운로드, 관리자 상태 변경, 문의 목록 요청 소유권 보완을 완료했습니다. 독립 QA에서 두 차례 **FAIL**이 발생했고 각 문제를 보정한 뒤 복구 전 최종 독립 QA에서 남은 P0-P2 기능 결함 없이 **PASS** 판정을 받았습니다. 그러나 이후 커밋 전 검토에서 테스트 소스 덮어쓰기가 발견되어 복구와 재검증을 수행했습니다. 복구 후 독립 QA는 기능 및 복구 무결성에는 **PASS**를 부여했지만, 복구 이력이 두 마감 문서에 누락되어 전체 판정은 **FAIL**, 커밋은 불허했습니다. 그 누락을 교정한 뒤 최종 post-correction 문서 QA에서 P0-P2 없음, 복구 공개 기준 충족, 문서 검증과 diff 검증 통과를 확인해 최종 **PASS** 및 커밋 **AUTHORIZED** 판정을 받았습니다.

## 사용자에게 보이는 변경

- 일반 문의 작성자는 본인 문의가 `OPEN`일 때만 삭제할 수 있습니다. 관리자는 문의 상태와 관계없이 기존 삭제 권한을 유지합니다.
- 첨부파일 다운로드 중에는 중복 요청이 차단되고 진행 상태가 표시됩니다. 실패 시 오류를 표시한 뒤 다시 시도할 수 있습니다.
- 경로, 사용자/토큰, 같은 문의의 새로고침, 화면 이탈로 요청 소유권이 바뀌면 이전 다운로드 완료 결과는 브라우저 다운로드나 오류로 반영되지 않습니다.
- 관리자는 백엔드 정책과 같은 상태 전이만 선택할 수 있습니다.
  - `OPEN` -> `IN_PROGRESS` 또는 `CLOSED`
  - `IN_PROGRESS` -> `RESOLVED` 또는 `CLOSED`
  - `RESOLVED` -> `CLOSED`
  - `CLOSED` -> 변경 불가
- 상태 변경 중에는 충돌하는 다른 상태 변경을 시작할 수 없고, 성공 시 요청값을 가정하지 않고 서버 응답 상태를 사용합니다.
- 활성 상태 필터에서 행이 빠져야 하는 경우 목록을 백엔드에서 다시 조회해 `dataList`와 `pageInfo`를 함께 최신 상태로 맞춥니다.

## 독립 QA와 보정

| 단계 | 결과 |
|------|------|
| 독립 QA 1차 | **FAIL**: 문의 목록의 최신 요청 소유권, 상태 변경 응답 타입, 관리자 상태 변경 화면 문서가 실제 계약과 맞지 않았습니다. |
| 보정 1차 | 목록·상태 변경 결과를 시작한 page/filter projection에 귀속하고, 정확한 응답 타입과 API 테스트를 추가했으며, 문서의 상태 변경 화면을 관리자 문의 목록으로 바로잡았습니다. |
| 독립 QA 2차 | **FAIL**: `OPEN` 필터에서 `OPEN` -> `CLOSED`로 변경하면 `CLOSED` 행과 이전 `pageInfo`가 `OPEN` 목록에 남을 수 있었습니다. |
| 보정 2차 | 서버에서 목록을 다시 조회하도록 수정하고, 새 `dataList`와 `pageInfo`가 실제로 반영되는 반례 테스트를 추가했습니다. |
| 복구 전 최종 독립 QA | **PASS**: 남은 P0-P2 기능 결함 없이 전체 품질 게이트 진행 승인. 이후 발견된 테스트 소스 덮어쓰기보다 앞선 시점의 판정입니다. |
| 보정 3차: 덮어쓰기 발견 및 복구 | 커밋 전 staged diff에서 `frontend/src/api/domainApis.test.ts`가 JSON 테스트 이름 배열로 대체된 사실을 발견했습니다. 덮어쓰기는 커밋되지 않았습니다. 파일을 HEAD에서 복구한 뒤 한 테스트 블록에 Question API 계약 변경만 `+18/-15`로 좁게 다시 적용했고, 기존 테스트 이름 15개를 모두 보존했습니다. |
| 복구 후 첫 전체 커버리지 | **FAIL**: WI-047과 무관한 `DownloadHistoryPage` 빈 상태 테스트가 로딩 상태를 렌더링하면서 실패했습니다. |
| `DownloadHistoryPage` 단독 재실행 | `frontend/`에서 `npm test -- --run src/pages/subscriber/DownloadHistoryPage.test.tsx` 실행. **PASS**: 1개 suite, 13/13 테스트 통과 |
| 복구 후 두 번째 전체 커버리지 | **PASS**: 91개 파일, 1,092개 테스트 통과. 커버리지 수치는 변경되지 않았습니다. |
| 복구 후 독립 QA | 기능 무결성 **PASS**, 복구 무결성 **PASS**. 다만 두 마감 문서에 복구 이력이 빠져 있어 전체 **FAIL**, 커밋 불허 판정이 내려졌습니다. 이 판정은 최종 PASS가 아니며, 교정 후 최종 문서 QA가 아직 필요합니다. |
| 최종 post-correction 문서 QA | **PASS**: P0 없음, P1 없음, P2 없음. 모든 복구 공개 기준을 통과했고, `domainApis.test.ts`는 기존 테스트 이름 15개를 보존한 한 블록 `+18/-15` 패치를 유지했습니다. 문서 검증과 `git diff HEAD --check`가 통과해 커밋 **AUTHORIZED** 판정을 받았습니다. |

## 추적 범위

- `CR-031-043`: 문의 작성자 삭제가 `OPEN` 밖에서 노출되는 문제를 종료했습니다.
- `CR-031-048`: 첨부파일 다운로드에 pending/failure/request ownership이 없던 문제를 종료했습니다.
- `CR-031-097`: 관리자 문의 UI가 불법 상태 전이를 제공하던 문제를 종료했습니다.
- `CR-031-096`은 세 관리자 목록의 최신 요청 소유권 문제입니다. 이번 작업에 projection ownership이 필요해 **Question slice만 선행 해결**했습니다. **License와 Track slice는 WI-20260809-ATS-053에 남아 있으며, `CR-031-096` 전체가 종료된 것은 아닙니다.**

## 검증 결과

### 프론트엔드

- 집중 검증: 3개 suite, 75개 테스트 PASS
- 복구 후 첫 전체 커버리지: 무관한 `DownloadHistoryPage` 빈 상태 테스트가 로딩 상태를 관찰해 FAIL
- `DownloadHistoryPage` 단독 재실행: `frontend/`에서 `npm test -- --run src/pages/subscriber/DownloadHistoryPage.test.tsx` 실행, 1개 suite 및 13/13 PASS
- 복구 후 두 번째 전체 커버리지: 91개 테스트 파일, 1,092개 테스트 PASS
- Statements: 88.92% (9,056/10,184)
- Branches: 80.95% (5,914/7,305)
- Functions: 89.41% (2,137/2,390)
- Lines: 91.28% (8,333/9,129)
- TypeScript typecheck: PASS
- ESLint 경고 0건: PASS
- Prettier: PASS
- production build: PASS

프론트엔드 커버리지 수치와 분모·분자는 `frontend/coverage/coverage-summary.json`에서 다시 대조했습니다. 첫 전체 커버리지 실패, 단독 1개 suite·13/13 PASS, 두 번째 전체 Vitest PASS 개수는 복구 근거로 전달된 결과이며, 입력 포인터에는 별도 영구 Vitest 실행 report가 없습니다. 단독 재실행의 정확한 명령은 `frontend/`에서 실행한 `npm test -- --run src/pages/subscriber/DownloadHistoryPage.test.tsx`입니다.

### 백엔드와 문서

- `QuestionServiceTest`: 37/37 PASS
- 전체 백엔드: 1,577개 중 1,558개 PASS, 19개 제외, 실패 0, 오류 0
- JaCoCo: instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%, class 94.824%
- JaCoCo verification 및 `assemble`: PASS
- 문서 검증: Tier 0, 내부 링크, 추적성 ID 585개, 문서 인덱스 PASS
- `git diff --check`: PASS. `QuestionServiceTest.java`의 CRLF -> LF 경고만 보고됐습니다.
- 최종 문서 QA의 `python .agents/skills/validate-docs/scripts/validate_docs.py`: Tier 0, 내부 링크, 추적성 ID 585개, 문서 인덱스 PASS
- 최종 문서 QA의 `git diff HEAD --check`: PASS, exit 0, 출력 또는 경고 없음

백엔드 테스트 수치는 `build/reports/tests/test/index.html`과 `build/test-results/test/TEST-*.xml`, JaCoCo 수치는 `build/reports/jacoco/test/jacocoTestReport.xml`에서 다시 대조했습니다.

## 잔여위험과 후속 범위

- 상태 변경 요청과 그 뒤의 필터 목록 refresh는 하나의 트랜잭션이 아니라 별도 HTTP 요청입니다. 백엔드 상태 변경이 커밋된 뒤 refresh가 실패하면, 백엔드 상태는 유지되고 UI에는 다시 시도할 수 있는 목록 오류가 표시됩니다. 재조회로 최신 `dataList`와 `pageInfo`를 복구할 수 있지만 이미 커밋된 상태 변경을 프론트엔드가 되돌리지는 않습니다.
- `CR-031-096`의 License/Track 최신 요청 소유권은 WI-20260809-ATS-053에서 계속 처리합니다.
- 문의 키보드·렌더링 의미론은 WI-20260809-ATS-059 범위입니다.
- 실제 브라우저/UAT, 운영 배포, production readiness, 외부 provider/mail/download 검증을 수행했다고 주장하지 않습니다.
- 스키마 변경, 데이터 변경, 실제 외부 부작용, 배포, merge, branch 삭제는 없었습니다.
- 보호된 output 산출물은 열거나 수정하지 않았고 untracked 상태를 유지했습니다.
- 복구 후 독립 QA는 기능과 복구 무결성에는 PASS를 부여했지만, 문서 누락 때문에 전체 FAIL 및 커밋 불허로 판정했습니다. 두 문서를 교정한 뒤 최종 post-correction 문서 QA는 P0-P2 없음과 복구 공개 기준 충족을 확인하고 최종 PASS 및 커밋 AUTHORIZED로 전환했습니다. 영구 결과는 `deliverables/agent/WI-20260809-ATS-047-final-doc-qa-result.md`에 기록했습니다.

## 롤백

WI-047 구현·테스트·현재 상태 문서 patch는 향후 WI commit 단위로 되돌릴 수 있습니다. 이번 문서 마감 자체를 되돌릴 때는 이 사용자 요약과 Agent Evidence Pack 두 파일만 제거하면 되며, DB·스키마·외부 시스템 롤백은 필요하지 않습니다.
