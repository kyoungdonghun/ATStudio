# WI-20260808-ATS-020 완료 요약

## 상태

WI-20260808-ATS-020은 `codex/v1-release-rehearsal-fixes` 브랜치에서 **완료**되었습니다. 새로 선택하는 Track 썸네일에만 JPEG/PNG, 10MB 이하, 정확한 1:1 비율을 적용했고 2048x2048px는 권장 규격으로 안내합니다.

기존 비정사각형 Track 썸네일은 변경하거나 삭제하지 않습니다. 수정 화면에서 기존 이미지를 실제로 불러온 뒤 자연 크기가 비정사각형으로 확인된 경우에만 교체 권장을 표시합니다. 이미지 로드 실패는 비정사각형으로 판정하지 않습니다.

WI-20260808-ATS-021은 **unblocked** 상태입니다. 커밋은 생성하지 않았습니다.

## 구현 결과

### 백엔드 최종 방어

- `CanonicalImageService`에 Track 전용 `canonicalizeSquareTrackThumbnail` 경로를 추가했습니다.
- 실제 바이트의 JPEG/PNG 시그니처, 클라이언트 MIME 일치, PNG 애니메이션, 단일 프레임, 최대 크기와 픽셀 경계를 확인한 뒤 이미지를 디코딩합니다.
- 디코딩된 가로와 세로가 정확히 같지 않으면 `TRACK_THUMBNAIL_NOT_SQUARE` 400 오류와 고정 메시지로 거절합니다.
- 유효한 이미지는 기존 서비스 정책을 재사용해 비율을 유지하면서 최대 2048px JPEG로 정규화합니다. 작은 이미지는 확대하지 않습니다.
- 일반 `canonicalizeThumbnail` 동작은 유지되어 Playlist 등 기존 호출부의 비정사각형 허용 정책이 바뀌지 않습니다. Album과 Playlist 코드는 수정하지 않았습니다.
- Track create/update는 같은 헬퍼로 저장 전에 검증·정규화하며 저장 계층에는 canonical JPEG만 전달합니다.
- update는 썸네일 검증과 선택적 오디오 분석을 엔티티 변경보다 먼저 수행합니다. 거절 시 저장·교체 호출, 제목/BPM/활성 상태/태그 변경이 발생하지 않습니다.

### 프론트엔드 업로드와 수정

- Track 전용 재사용 썸네일 필드가 JPEG 또는 PNG, 1:1 필수, 10MB 이하, 2048x2048px 권장 문구를 표시합니다.
- 파일 input은 `image/jpeg,image/png`만 허용합니다.
- 선택 파일은 160px 반응형 정사각형 컨테이너에서 `object-fit: cover`, 중앙 정렬로 미리 봅니다.
- 자연 크기 판별이 pending이거나 invalid이면 Upload 전체 제출 또는 Edit 저장이 비활성화됩니다.
- 비정사각형, 디코딩 실패, 형식 불일치, 10MB 초과는 해당 썸네일 필드 아래에 명확한 오류로 표시됩니다.
- 빠른 파일 교체 시 선택 버전으로 오래된 load/error 결과를 무시합니다. 이전 객체 URL은 교체 즉시, 현재 객체 URL은 언마운트 시 해제합니다.
- Track Edit는 기존 cover를 그대로 렌더링하며 비정사각형 경고가 있어도 새 파일을 선택하지 않은 저장 요청에는 `thumbnail`을 포함하지 않습니다.
- Album/Playlist UI, crop editor, 자동 crop, `contain` 전환은 범위에 포함하지 않았습니다.

## 정확한 변경 파일

백엔드 production:

- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`

프론트엔드 production:

- `frontend/src/pages/creator/trackThumbnail.ts`
- `frontend/src/pages/creator/TrackThumbnailField.tsx`
- `frontend/src/pages/creator/TrackThumbnailField.module.css`
- `frontend/src/pages/creator/TrackUploadPage.tsx`
- `frontend/src/pages/creator/TrackEditPage.tsx`

백엔드 tests:

- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java`

프론트엔드 tests:

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx`
- `frontend/src/pages/creator/TrackUploadPage.test.tsx`
- `frontend/src/pages/creator/TrackEditPage.test.tsx`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`

WI 산출물:

- `deliverables/user/WI-20260808-ATS-020-summary.md`
- `deliverables/agent/WI-20260808-ATS-020-evidence-pack.md`

기존 WI-014부터 WI-019까지의 dirty 변경과 `output/client-demo-screenshots-20260716-140514.zip`은 보존했습니다.

## 검증

| 명령                                                                                                                                                                                                                          | 결과                                                                 |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest"` | PASS, 3 classes / 47 tests / 실패 0 / 오류 0 / 스킵 0                |
| `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx`                                                                              | PASS, 최종 3 files / 10 tests / 실패 0. ESLint 수정 전후 총 2회 실행 |
| `npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx -t "uploads a validated track\|loads and updates a track"`                                                                                                   | PASS, 1 file / 선택 2 tests / 실패 0 / 비선택 26 skipped             |
| `npm run typecheck`                                                                                                                                                                                                           | PASS, 최종 `tsc --noEmit`, exit code 0                               |
| `npm run lint`                                                                                                                                                                                                                | PASS, 전체 `frontend/src`, 오류 0 / 경고 0                           |
| `npm run format`                                                                                                                                                                                                              | PASS, 전체 frontend Prettier 검사                                    |
| `npx prettier --check ../deliverables/user/WI-20260808-ATS-020-summary.md ../deliverables/agent/WI-20260808-ATS-020-evidence-pack.md`                                                                                         | PASS, WI 산출물 2개 형식 일치                                        |
| `git diff --check`                                                                                                                                                                                                            | PASS, whitespace 오류 없음                                           |

고유 집중 테스트는 백엔드 47개와 프론트엔드 12개로 총 59개입니다. 전용 프론트 10개를 ESLint 수정 뒤 재실행했으므로 실제 테스트 실행 횟수는 총 69회입니다. 전체 백엔드/프론트엔드 suite, build, coverage는 요청에 따라 실행하지 않았습니다.

## 잔여 위험

- 브라우저 자연 크기 검사는 빠른 안내이며 백엔드 디코딩 검사가 최종 권위입니다. 브라우저가 읽지만 Java ImageIO가 거절하는 파일은 API에서 최종 거절될 수 있습니다.
- 선택 파일 미리보기는 원본 객체 URL이고 저장 파일은 JPEG 정규화 결과입니다. 정사각형 레이아웃과 중앙 `cover` 동작은 동일하지만 JPEG 재인코딩에 따른 색상·압축 차이는 있을 수 있습니다.
- 기존 이미지 경고는 브라우저에서 파일을 성공적으로 불러온 경우에만 표시됩니다. 로드 실패 이미지는 보존되며 경고를 표시하지 않습니다.
- 전체 suite, build, coverage, 실제 브라우저 시각 점검은 후속 QA WI 범위로 남습니다.

## 롤백

1. 위 15개 production/test 파일과 WI-020 산출물 2개만 되돌립니다.
2. `TRACK_THUMBNAIL_NOT_SQUARE`, Track 전용 canonical 메서드, TrackService 호출부를 하나의 백엔드 단위로 함께 되돌립니다.
3. Track 썸네일 필드, Upload/Edit 연결, 전용 테스트를 하나의 프론트엔드 단위로 함께 되돌립니다.
4. 스키마, 데이터, 외부 시스템, dependency 롤백은 필요하지 않습니다.

## WI 체인

- WI-020의 기능 및 집중 검증 조건은 충족되었습니다.
- WI-021은 기존 승인된 handoff 흐름으로 즉시 진행할 수 있습니다.
