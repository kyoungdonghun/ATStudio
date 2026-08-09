# WI-20260808-ATS-021 완료 요약

## 상태

WI-20260808-ATS-021은 `codex/v1-release-rehearsal-fixes` 브랜치에서 **완료**되었습니다. Home의 기존 장르/분위기 탐색을 하나의 `태그별 탐색` 모듈로 통합하고, 탭 순서를 용도, 장르, 분위기, 악기로 고정했습니다. 용도는 라이선스와 분리된 태그 범주로 유지됩니다.

등록 태그와 활성 음원에 실제로 연결된 탐색 가능 태그를 구분해 로드하며, 용도 탭은 결과가 없어도 항상 표시합니다. 최초 활성 탭은 결과가 있는 첫 범주이고, 모든 범주가 비어 있으면 용도입니다.

TrackList는 네 태그 범주의 URL 복원, 반복 API 파라미터, 선택 상태, 표시/초기화, 정렬 및 페이지 보존을 지원합니다. 커밋이나 스테이징은 수행하지 않았습니다. WI-20260808-ATS-022는 **unblocked** 상태입니다.

## 구현 결과

### Home 태그별 탐색

- 용도, 장르, 분위기, 악기 순서의 단일 탭 모듈을 구성했습니다.
- 전체 등록 태그와 활성 음원 기반 available 태그를 별도로 조회해 정상, 등록 태그 없음, 등록됐지만 활성 결과 없음, API 실패를 구분합니다.
- API 실패는 태그 모듈에만 오류와 다시 시도를 표시하며 Home의 나머지 SR-04 콘텐츠는 유지합니다.
- 첫 화면에는 범주당 최대 8개 태그를 표시하고 `더보기`와 `접기`로 확장합니다.
- 선택한 태그는 `URLSearchParams`로 `/tracks?usage=...`, `genre`, `mood`, `instrument` 링크를 만듭니다. 한국어, 공백, 쉼표, `#`를 값 단위로 인코딩하고 반복 값은 AND 검색으로 전달합니다.
- `tablist`/`tab`/`tabpanel`, 단일 탭 정지점, `ArrowLeft`, `ArrowRight`, `Home`, `End` 로빙 포커스를 적용했습니다.
- 모바일에서는 탭을 가로 스크롤하고 태그 목록 크기를 제한합니다. 페이지 섹션 카드나 카드 중첩은 추가하지 않았습니다.

### TrackList 네 범주 필터

- URL의 `getAll` 결과를 네 범주 모두 복원하고, API에는 쉼표 결합 문자열이 아닌 반복 배열로 전달합니다.
- 악기 필터를 인라인 행과 전체 필터 모달에 추가하고 네 범주의 선택 상태와 available 상태를 함께 유지합니다.
- 필터 변경과 초기화는 페이지를 1로 되돌리되 기존 정렬을 보존합니다. 페이지 이동과 정렬 변경은 네 범주 태그 값을 유지합니다.
- 모든 태그 초기화는 장르, 분위기, 악기, 용도, BPM만 제거하고 정렬 등 다른 URL 상태를 보존합니다.
- 필터 칩은 native `button`과 `aria-pressed`를 사용합니다.

### 백엔드 available 태그와 AND 검색

- Track 검색 DTO와 available 태그 API가 네 범주의 반복 `List<String>` 파라미터를 받습니다.
- `TagService` available 조회에 Instrument를 추가하고 활성 Track만 대상으로 한 번의 native query를 실행합니다.
- 태그 type, 태그 name, BPM은 모두 위치 파라미터로 바인딩합니다. 동적 SQL에는 내부에서 생성한 숫자 alias만 사용하며 사용자/도메인 값은 보간하지 않습니다.
- 선택 태그별 독립 subquery를 추가해 범주 내 다중 값과 범주 간 값을 모두 AND로 결합합니다.
- available 태그와 Track 검색은 `TagNamePolicy.canonicalize`로 NFC 및 공백 정규화를 먼저 수행한 뒤 중복을 제거합니다. 쉼표와 `#`가 포함된 legacy 이름은 검증 경계로 다시 거절하거나 분해하지 않고 원자값으로 유지합니다.
- 응답은 기존 wrapper의 `dataList`/`pageInfo` 계약을 유지하고 Entity를 Controller에서 직접 노출하지 않습니다.

## 정확한 변경 파일

백엔드 production:

- `src/main/java/com/atstudio/atstudio/controller/TagController.java`
- `src/main/java/com/atstudio/atstudio/dto/track/TrackSearchRequest.java`
- `src/main/java/com/atstudio/atstudio/service/TagService.java`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`

프론트엔드 production:

- `frontend/src/api/tags.ts`
- `frontend/src/api/tracks.ts`
- `frontend/src/components/filter/TagFilterModal.tsx`
- `frontend/src/components/ui/FilterChip.tsx`
- `frontend/src/components/ui/FilterChip.module.css`
- `frontend/src/pages/public/HomePage.tsx`
- `frontend/src/pages/public/HomePage.module.css`
- `frontend/src/pages/public/TrackListPage.tsx`

백엔드 tests:

- `src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/TagServiceBranchCoverageTest.java`
- `src/test/java/com/atstudio/atstudio/service/TagServiceAvailableTagsIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`

프론트엔드 tests:

- `frontend/src/api/domainApis.test.ts`
- `frontend/src/components/catalogComponents.test.tsx`
- `frontend/src/pages/public/HomePage.test.tsx`
- `frontend/src/pages/public/TrackListPage.test.tsx`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx`

WI 산출물:

- `deliverables/user/WI-20260808-ATS-021-summary.md`
- `deliverables/agent/WI-20260808-ATS-021-evidence-pack.md`

총 25개 production/test/산출물 파일에 WI-021 변경을 적용했습니다. WI-014부터 WI-020까지의 기존 dirty 변경 위에 필요한 부분만 추가했고, `output/client-demo-screenshots-20260716-140514.zip`은 700,703바이트 상태로 보존했습니다.

### Independent review repair pass 변경 파일

- `frontend/src/pages/public/TrackListPage.tsx`
- `frontend/src/pages/public/TrackListPage.test.tsx`
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx`
- `src/main/java/com/atstudio/atstudio/service/TagService.java`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/test/java/com/atstudio/atstudio/service/TagServiceBranchCoverageTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `deliverables/user/WI-20260808-ATS-021-summary.md`
- `deliverables/agent/WI-20260808-ATS-021-evidence-pack.md`

Repair pass는 위 9개 파일만 변경했습니다. 모달 진입점, available 요청의 show-all fallback과 race fence, query-side canonicalization, WI-020 썸네일 광역 회귀 계약, 관련 테스트와 산출물만 수정했습니다.

## 검증

| 명령                                                                                                                                                                                                                                                                                                                                                          | 결과                                                  |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TagServiceBranchCoverageTest" --tests "com.atstudio.atstudio.service.TagServiceAvailableTagsIntegrationTest" --tests "com.atstudio.atstudio.controller.TagControllerTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.TrackServiceTest"` | PASS, 5 classes / 69 tests / 실패 0 / 오류 0 / 스킵 0 |
| `npm test -- src/pages/public/HomePage.test.tsx src/pages/public/TrackListPage.test.tsx src/components/catalogComponents.test.tsx src/api/domainApis.test.ts`                                                                                                                                                                                                 | PASS, 4 files / 35 tests / 실패 0                     |
| `npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx`                                                                                                                                                                                                                                  | PASS, 2 files / 54 tests / 실패 0                     |
| `npm run typecheck`                                                                                                                                                                                                                                                                                                                                           | PASS, `tsc --noEmit`, exit code 0                     |
| `npm run lint`                                                                                                                                                                                                                                                                                                                                                | PASS, 전체 `frontend/src`, 오류 0 / 경고 0            |
| `npm run format`                                                                                                                                                                                                                                                                                                                                              | PASS, 전체 frontend 형식 일치                         |
| WI 산출물 2개 `npx prettier --check ...`                                                                                                                                                                                                                                                                                                                      | PASS, 산출물 형식 일치                                |
| `git diff --check`                                                                                                                                                                                                                                                                                                                                            | PASS, whitespace 오류 없음                            |

최종 재실행에서 WI-021 집중 검증은 백엔드 69/69와 프론트엔드 35/35, 별도 광역 회귀는 54/54가 통과했습니다. 광역 썸네일 테스트는 현재 정확한 오류 문구를 확인하고 유효 이미지의 `pending -> valid` dimension-load 전환까지 수행합니다.

중간 실패도 기록합니다. 첫 백엔드 선택 실행은 새 테스트의 Mockito `times` import 누락으로 `compileTestJava`가 실패했고, import 보완 후 동일 테스트와 전체 WI-021 백엔드 선택 묶음을 통과했습니다. 첫 타입체크는 테스트의 `Array.at` 사용이 현재 TS lib target과 맞지 않아 `TS2550`으로 실패했고, 기존 인덱스 방식으로 바꾼 뒤 최종 타입체크가 통과했습니다.

전체 백엔드/프론트엔드 suite, build, coverage, 실제 브라우저 시각 점검은 요청된 WI-021 검증 경계에 따라 실행하지 않았습니다.

## 잔여 위험

- 반복 query parameter가 쉼표 포함 태그 이름을 보존하는 canonical 계약입니다. 과거 외부 링크가 단일 CSV 값 하나에 여러 태그를 넣었다면 이제 하나의 태그 이름으로 해석되므로 반복 파라미터로 변환해야 합니다.
- available 태그 조회는 한 SQL query를 유지하지만 선택 태그 수에 따라 AND subquery 수가 증가합니다. 일반 UI 선택 범위에는 적합하며 비정상적으로 큰 외부 요청은 후속 성능/입력 제한 검토 대상입니다.
- 전체 suite, build, coverage, 실제 브라우저 키보드/모바일 시각 점검은 후속 QA WI 범위입니다.

## 롤백

1. 위 23개 production/test 파일에서 WI-021 변경만 되돌리고 WI-021 산출물 2개를 제거합니다. 같은 파일에 존재하는 WI-014부터 WI-020 변경은 유지합니다.
2. 반복 태그 배열 DTO/API, Instrument available query, TrackService AND 정규화를 백엔드 테스트와 함께 하나의 단위로 되돌립니다.
3. Home 태그 모듈, TrackList/Modal/FilterChip 연결, 프론트 API 직렬화를 프론트 테스트와 함께 하나의 단위로 되돌립니다.
4. 애플리케이션 스키마, 데이터, dependency, 외부 시스템 롤백은 필요하지 않습니다. 통합 테스트의 격리된 in-memory H2 fixture는 테스트 종료 시 폐기됐습니다.

## WI 체인

- WI-021의 기능 및 집중 검증 조건은 충족되었습니다.
- WI-022는 승인된 handoff 흐름으로 즉시 진행할 수 있습니다.
