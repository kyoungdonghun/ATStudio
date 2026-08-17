# WI-20260817-ATS-001 완료 요약

## 상태

WI-20260817-ATS-001은 클라이언트 확인 전용 브랜치
`codex/v1-client-acceptance-20260817`에서 완료되었습니다. Track 신규 등록과
수정에서 새로 선택한 비정사각형 JPEG/PNG 썸네일을 프론트엔드와 백엔드가
임시로 허용합니다.

개발 워크트리와 개발 브랜치는 수정하지 않았습니다. 커밋, 서버 프로세스,
DB, 공개 데이터에도 변경을 가하지 않았습니다. 후속 독립 검증 및 공개 서버
반영은 WI-20260817-ATS-002 범위입니다.

## 변경 결과

- 프론트엔드의 선택 이미지 자연 크기 검사는 계속 수행하되, 가로와 세로가
  다르다는 이유만으로 `invalid` 처리하지 않도록 임시 플래그를 껐습니다.
- 신규·수정 안내에서 `1:1 필수`를 제거했습니다. `JPEG 또는 PNG, 10MB 이하`와
  `2048x2048px 권장 (필수 아님)`은 유지합니다.
- 이미지 로드 완료 전에는 제출을 막고, 디코딩 실패·지원하지 않는 형식·10MB
  초과는 계속 거절합니다.
- 선택 이미지 미리보기는 기존과 같은 1:1 컨테이너와 중앙 `cover` 표시를
  유지하므로 실제 카드에서 예상되는 잘림을 계속 확인할 수 있습니다.
- 수정 화면의 기존 비정사각형 썸네일 교체 권장 경고도 유지했습니다.
- 백엔드는 Track 전용 canonical 경로와 모든 이미지 방어를 유지하면서
  `SQUARE_TRACK` 정책의 정사각형 요구만 임시 플래그로 껐습니다.
- 비정사각형 원본은 종횡비를 보존해 최대 2048px로 축소되고 canonical JPEG로
  저장됩니다. 작은 이미지는 확대하지 않습니다.

## 정확한 변경 파일

제품 코드:

- `frontend/src/pages/creator/TrackThumbnailField.tsx`
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java`

테스트:

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx`
- `frontend/src/pages/creator/TrackUploadPage.test.tsx`
- `frontend/src/pages/creator/TrackEditPage.test.tsx`
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`

WI 산출물:

- `deliverables/user/REQ-20260817-ATS-001.md`
- `deliverables/agent/WI-20260817-ATS-001-handoff.md`
- `deliverables/user/WI-20260817-ATS-001-summary.md`
- `deliverables/agent/WI-20260817-ATS-001-evidence-pack.md`

## 검증

| 명령                                                                                                                                                  | 결과                                                  |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx`      | PASS, 3 files / 17 tests / 실패 0                     |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest"` | PASS, 2 classes / 48 tests / 실패 0 / 오류 0 / 스킵 0 |
| `npm run typecheck`                                                                                                                                   | PASS, `tsc --noEmit`, exit code 0                     |
| `npm run lint`                                                                                                                                        | PASS, 오류 0 / 경고 0                                 |
| `npx prettier --check ...`                                                                                                                            | PASS, 변경된 프론트엔드 4개 파일                      |
| `.\gradlew.bat compileJava compileTestJava`                                                                                                           | PASS                                                  |
| `git diff --check`                                                                                                                                    | PASS                                                  |

추가 문서 검증:

- WI 산출물 2개에 대한 `npx prettier --check` PASS
- `python .agents/skills/validate-docs/scripts/validate_docs.py` PASS
  (Tier 0, 내부 링크, 추적 ID, 문서 인덱스)

## 임시 편차와 위험

- 이 변경은 SR-98의 장기 1:1 정책을 폐기한 것이 아니라 클라이언트 확인용
  브랜치에서만 적용한 임시 편차입니다.
- 비정사각형 원본은 기존 정사각형 `cover` 카드에서 일부가 잘릴 수 있습니다.
  이번 변경은 그 결과를 실제로 확인하기 위한 것이며 crop, `contain`, padding
  정책을 결정하지 않습니다.
- `TRACK_THUMBNAIL_NOT_SQUARE` 오류 정의와 정사각형 검사 코드는 복구를 위해
  남아 있지만 두 임시 플래그가 `false`인 동안에는 비율만으로 발생하지 않습니다.
- 전체 제품 suite, 브라우저 업로드, 실행 서버 반영은 후속 독립 QA 범위입니다.

## 롤백

1. `TrackThumbnailField.tsx`의 `TRACK_THUMBNAIL_SQUARE_REQUIRED`를 `true`로
   변경합니다.
2. `CanonicalImageService.java`의 `REQUIRE_SQUARE_TRACK_THUMBNAIL`을 `true`로
   변경합니다.
3. 신규 안내에 `1:1 필수`를 복원하고 변경된 5개 테스트 파일을 기존 정사각형
   필수 계약으로 되돌립니다.
4. 스키마, 데이터, dependency 또는 외부 시스템 롤백은 필요하지 않습니다.

## WI 체인

- WI-20260817-ATS-001의 구현과 집중 검증 조건은 충족되었습니다.
- WI-20260817-ATS-002는 독립 검증과 클라이언트 확인 서버 반영을 진행할 수
  있는 상태입니다.
