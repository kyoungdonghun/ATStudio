# WI-20260817-ATS-020 독립 검증 요약

## 판정

**PASS — BLOCKER 0 / MAJOR 0 / MINOR 0**

클라이언트 확인 전용 브랜치 `codex/v1-client-acceptance-20260817`의 Track
썸네일 프론트엔드 해상도 검사를 독립적으로 검증했습니다. 개발 워크트리,
백엔드 코드, 서비스·터널, DB와 공개 데이터는 변경하지 않았습니다.

## 확인된 동작

- Track 썸네일은 가로 또는 세로가 `4096px`을 초과하거나 전체 픽셀이
  `16,777,216`을 초과하면 다음 메시지와 함께 `invalid` 상태가 됩니다.

  > 트랙 썸네일은 가로·세로 각각 4096px 이하이고 전체 16,777,216픽셀 이하여야 합니다.

- 실제 `DSC_2446.JPG`를 읽어 `6048×4032`, `24,385,536`픽셀,
  `7.815MiB`임을 확인했습니다. 10MB 용량 제한은 통과하지만 변 길이와 전체
  픽셀 제한을 모두 초과하므로 프론트에서 제출 전에 차단되는 것이 맞습니다.
- 정확한 경계인 `4096×4096 = 16,777,216`픽셀은 허용됩니다.
- `640×480`, `800×600`, `900×600`처럼 제한 안의 비정사각형 이미지는 현재
  임시 정책대로 계속 허용됩니다.
- 해상도 분기는 정사각형 분기보다 먼저 별도로 실행됩니다.
  `TRACK_THUMBNAIL_SQUARE_REQUIRED`는 독립된 `false` 플래그로 유지되어,
  추후 `true`로 복구해도 해상도 검사와 1:1 검사가 함께 적용됩니다.
- Upload와 Edit 모두 과대 이미지를 차단하고, 범위 안의 새 비정사각형
  이미지로 교체하면 제출을 허용하는 테스트가 통과했습니다.
- 기존 미리보기 이미지의 단일 `load` 이벤트에서 `naturalWidth`와
  `naturalHeight`를 재사용합니다. 추가 디코딩, 네트워크 요청, 객체 URL,
  effect 또는 의존성은 추가되지 않았습니다.

## 변경 범위 검토

- 제품 diff는 다음 프론트엔드 5개 파일로만 제한됩니다.
  - `frontend/src/utils/validation.ts`
  - `frontend/src/pages/creator/TrackThumbnailField.tsx`
  - `frontend/src/pages/creator/TrackThumbnailField.test.tsx`
  - `frontend/src/pages/creator/TrackUploadPage.test.tsx`
  - `frontend/src/pages/creator/TrackEditPage.test.tsx`
- 백엔드 `CanonicalImageService`에는 이번 WI diff가 없으며, 프론트 상수
  `4096`과 `16,777,216`은 백엔드 계약과 일치합니다.
- 기존 Album 상수는 같은 값의 공통 상수를 별칭으로 참조하므로 Album 동작은
  바뀌지 않습니다. Playlist 파일과 동작에는 diff가 없습니다.
- 개발 워크트리 `C:\Users\jm991\Desktop\project\ATStudio`는
  `codex/v1-release-rehearsal-fixes`, HEAD `3e1123416bd106fbeb8b1ad2b3f8bf343a54100e`,
  tracked diff 0건으로 확인했습니다.

## 독립 품질 검사

| 검사 | 결과 |
| --- | --- |
| 집중 Vitest | PASS, 3 files / 19 tests / 실패 0 |
| TypeScript typecheck | PASS, `tsc --noEmit`, exit 0 |
| ESLint | PASS, 오류·경고 0, exit 0 |
| 변경 파일 Prettier | PASS, 5개 파일, exit 0 |
| `git diff --check` | PASS, exit 0 |

## 실행 서버 반영

- 로컬 프론트 `http://127.0.0.1:5173/`: HTTP 200
- 로컬 API `http://127.0.0.1:8080/api/tracks`: HTTP 200
- 공개 프론트 `https://cleaners-capital-spirit-commerce.trycloudflare.com/`: HTTP 200
- 공개 API `https://cleaners-capital-spirit-commerce.trycloudflare.com/api/tracks`: HTTP 200
- 5173의 Node PID `36108`과 8080의 Java PID `18060` 모두 클라이언트 확인
  worktree를 가리키며 개발 worktree를 가리키지 않습니다.
- 로컬·공개 `validation.ts` 제공 모듈의 SHA-256이 같고, `4096`과
  `16,777,216` 상수를 포함합니다.
- 로컬·공개 `TrackThumbnailField.tsx` 제공 모듈의 SHA-256이 같고, 변·픽셀
  검사, 정확한 오류 메시지, `TRACK_THUMBNAIL_SQUARE_REQUIRED = false`, 안내
  문구를 모두 포함합니다.

검증 시작 시점에는 로컬 서비스가 잠시 내려가 공개 주소가 502였지만, 대상
worktree 전용 detached 서비스가 복구된 뒤 위 최종 상태를 다시 확인했습니다.

## 제한과 롤백

- 인증된 공개 관리자 세션으로 실제 파일을 제출하거나 DB·저장소를 변경하지
  않았습니다. 집중 컴포넌트·페이지 테스트와 실제 제공 모듈 대조로 제출 전
  차단을 검증했습니다.
- 롤백 시 WI-019의 프론트 해상도 상수·분기·안내·테스트만 되돌리고,
  `TRACK_THUMBNAIL_SQUARE_REQUIRED = false`와 기존 비정사각형 임시 허용은
  유지하면 됩니다. 백엔드 롤백은 필요하지 않습니다.
