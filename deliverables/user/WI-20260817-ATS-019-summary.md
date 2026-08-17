# WI-20260817-ATS-019 완료 요약

## 상태

WI-20260817-ATS-019는 클라이언트 확인 전용 브랜치
`codex/v1-client-acceptance-20260817`에서 완료되었습니다. Track 신규 등록과
수정 화면이 백엔드와 같은 이미지 해상도 경계를 파일 제출 전에 검사합니다.

`DSC_2446.JPG`의 `6048×4032` 해상도와 `24,385,536`픽셀은 다음 필드 오류로
즉시 차단됩니다.

> 트랙 썸네일은 가로·세로 각각 4096px 이하이고 전체 16,777,216픽셀 이하여야 합니다.

백엔드, Album, Playlist, DB, 저장 파일, 서비스 프로세스, 터널, 개발
워크트리는 변경하지 않았습니다. 커밋도 생성하지 않았습니다.

## 구현 결과

- 공통 프론트엔드 이미지 경계 상수로 `4096px`과 `16,777,216`픽셀을
  정의했습니다. 기존 Album 상수는 같은 공통값을 참조하므로 동작은 변하지
  않습니다.
- Track 썸네일은 기존 미리보기 이미지의 단일 `load` 이벤트에서 얻은
  `naturalWidth`와 `naturalHeight`로만 경계를 검사합니다. 두 번째 이미지
  디코딩이나 네트워크 요청은 추가하지 않았습니다.
- 가로 또는 세로가 4096px을 초과하거나 전체 픽셀 수가 16,777,216을
  초과하면 선택 상태를 `invalid`로 전환하여 Upload와 Edit 제출을 막습니다.
- 해상도 검사는 정사각형 검사와 별도 조건으로 먼저 실행됩니다.
  `TRACK_THUMBNAIL_SQUARE_REQUIRED`는 독립된 복구 플래그로 남아 있으며 현재
  `false`입니다.
- `4096×4096` 경계값은 허용되고, `800×600`과 `900×600` 같은 범위 내
  비정사각형 파일도 계속 허용됩니다.
- 안내는 `JPEG 또는 PNG, 10MB 이하, 가로·세로 각각 최대 4096px`으로
  확장했습니다. 정사각형을 유일한 권장 형태로 오해하지 않도록
  `2048x2048px 권장`은 `긴 변 2048px 권장 (필수 아님)`으로 바꿨습니다.
- JPEG/PNG, 10MB, 디코딩 실패, 오래된 load 결과 차단, 객체 URL 해제,
  정사각형 `cover` 미리보기, 기존 비정사각형 파일 경고는 유지했습니다.

## 정확한 변경 파일

제품 코드:

- `frontend/src/utils/validation.ts`
- `frontend/src/pages/creator/TrackThumbnailField.tsx`

테스트:

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx`
- `frontend/src/pages/creator/TrackUploadPage.test.tsx`
- `frontend/src/pages/creator/TrackEditPage.test.tsx`

WI 산출물:

- `deliverables/agent/WI-20260817-ATS-019-handoff.md`
- `deliverables/user/WI-20260817-ATS-019-summary.md`
- `deliverables/agent/WI-20260817-ATS-019-evidence-pack.md`

## 검증

| 검증                      | 결과                              |
| ------------------------- | --------------------------------- |
| 프론트 대상 Vitest        | PASS, 3 files / 19 tests / 실패 0 |
| TypeScript typecheck      | PASS, `tsc --noEmit`              |
| ESLint                    | PASS, 오류·경고 0                 |
| 변경 프론트 파일 Prettier | PASS                              |
| `git diff --check`        | PASS                              |

집중 테스트에는 다음 시나리오가 포함됩니다.

- `6048×4032` JPEG 선택 시 명시적 해상도 오류와 제출 차단
- `4096×4096` 정확한 경계값 허용
- 범위 내 비정사각형 PNG의 Upload·Edit 제출 허용
- 형식·10MB·디코딩·stale load·객체 URL·기존 이미지 경고 회귀

## 발견 사항과 잔여 위험

- **BLOCKER 0 / MAJOR 0 / MINOR 0**
- 현재 두 경계는 `4096×4096 = 16,777,216`이므로 각 변 제한을 만족하면
  픽셀 제한도 함께 만족합니다. 두 검사를 명시적으로 유지하여 백엔드 계약을
  그대로 표현하고 향후 두 상수 중 하나가 달라져도 정책이 누락되지 않게
  했습니다.
- 프론트엔드 검사는 운영자에게 빠른 이유를 알려주는 사전 검사입니다. 실제
  파일 바이트, MIME, APNG·프레임과 Java 디코딩 가능 여부의 최종 권위는 계속
  백엔드입니다.
- 실제 인증된 공개 화면에서 `DSC_2446.JPG`를 선택하는 검증과 서버 반영은
  후속 WI-20260817-ATS-020 범위입니다.

## 롤백

1. `TrackThumbnailField.tsx`에서 `pixelCount`와 `IMAGE_MAX_DIMENSION`·
   `IMAGE_MAX_PIXELS` 오류 분기를 제거합니다.
2. 안내를 이전 `JPEG 또는 PNG, 10MB 이하`와
   `2048x2048px 권장 (필수 아님)`으로 복원합니다.
3. `validation.ts`의 공통 이미지 상수를 제거하고 기존 Album 상수 리터럴을
   복원합니다.
4. WI-019에서 추가·수정한 프론트 테스트 기대값을 되돌립니다.
5. `TRACK_THUMBNAIL_SQUARE_REQUIRED = false`와 WI-001의 임시 비정사각형 허용
   변경은 그대로 유지합니다.

## WI 체인

- WI-20260817-ATS-019의 구현과 집중 검증 조건은 충족되었습니다.
- WI-20260817-ATS-020은 독립 검증과 클라이언트 확인 서버 반영을 진행할 수
  있는 상태입니다.
