# WI-20260817-ATS-002 독립 검증 요약

## 판정

- **최종 결과:** PASS
- **발견 사항:** BLOCKER 0 / MAJOR 0 / MINOR 1
- **대상 브랜치:** `codex/v1-client-acceptance-20260817`
- **대상 HEAD:** `18928a702ea5281e535faac855c17ce78166653a`

Track 신규 등록·수정의 썸네일 1:1 필수 검증은 클라이언트 확인용
워크트리에서만 임시 비활성화되어 있습니다. 비정사각형 JPEG/PNG를
프론트엔드가 유효한 선택으로 처리하고 백엔드도 비율을 보존하여 canonical
JPEG로 정규화합니다.

## 확인 결과

- 프론트엔드의 `TRACK_THUMBNAIL_SQUARE_REQUIRED`는 `false`이며 비정사각형
  이미지를 디코딩한 뒤 `valid`로 전환합니다.
- 신규·수정 안내에서 `1:1 필수`가 제거됐고 `JPEG 또는 PNG, 10MB 이하`와
  `2048x2048px 권장 (필수 아님)`은 유지됩니다.
- 백엔드의 `REQUIRE_SQUARE_TRACK_THUMBNAIL`은 `false`입니다. 컴파일된
  `SQUARE_TRACK` 정책도 `false`로 확인했습니다.
- JPEG/PNG 시그니처·MIME, 10MB, APNG·다중 프레임, 디코딩 크기·픽셀,
  비율 보존 축소, no-upscale, canonical JPEG 출력 방어는 그대로입니다.
- Album·Playlist·DB·스키마 파일은 변경되지 않았습니다. 새 네트워크 요청,
  이미지 이중 디코딩 또는 N+1도 추가되지 않았습니다.
- 기존 비정사각형 이미지 경고와 정사각형 `cover` 미리보기는 클라이언트
  표시 확인을 위해 유지됩니다.

## 브랜치와 실행 서버 분리

- 포트 5173의 Vite 프로세스와 포트 8080의 Java classpath는 모두
  `C:\Users\jm991\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817`
  경로를 사용합니다.
- 두 프로세스 어디에도 개발 워크트리
  `C:\Users\jm991\Desktop\project\ATStudio` 경로가 포함되지 않습니다.
- 개발 브랜치는 `codex/v1-release-rehearsal-fixes`, HEAD는
  `3e1123416bd106fbeb8b1ad2b3f8bf343a54100e`로 유지됐습니다. 개발
  워크트리의 tracked diff는 0건이며 원래의 `1:1 필수`와
  `SQUARE_TRACK(true)` 계약도 그대로입니다.

## 로컬·공개 반영

다음 네 요청은 모두 HTTP 200을 반환했습니다.

- `http://127.0.0.1:5173/`
- `http://127.0.0.1:8080/api/tracks`
- `https://cleaners-capital-spirit-commerce.trycloudflare.com/`
- `https://cleaners-capital-spirit-commerce.trycloudflare.com/api/tracks`

로컬과 공개 응답은 프론트·API 각각 SHA-256과 바이트 길이가 일치했습니다.
공개 Vite 모듈도 로컬과 동일했고 임시 프론트 플래그 `false`, 변경된 안내,
2048px 권장 문구를 포함했습니다.

백엔드는 `--rerun-tasks` 검증 과정에서 지정 워크트리의 클래스를 다시
컴파일했습니다. 컴파일된 enum bytecode에서 `SQUARE_TRACK`의 생성 인자가
`false`임을 확인했고, 실행 JVM에는 DevTools `RestartClassLoader`가 존재하며
재컴파일 후 로컬·공개 API 헬스가 모두 정상임을 확인했습니다.

공개 관리자 업로드 URL은 인증되지 않은 브라우저에서
`/login?returnTo=%2Fadmin%2Ftracks%2Fupload`로 정상 리디렉션되었습니다.
인증 우회나 사용자 데이터 제출은 하지 않았으므로 공개 화면에서의 실제
비정사각형 파일 선택은 수행하지 않았습니다. 이 동작은 대상 프론트 테스트로
검증했습니다.

## 품질 검증

| 검증                               | 결과                                          |
| ---------------------------------- | --------------------------------------------- |
| 프론트 대상 테스트                 | PASS, 3 files / 17 tests                      |
| 백엔드 대상 테스트                 | PASS, 2 classes / 48 tests / 실패·오류·스킵 0 |
| TypeScript typecheck               | PASS                                          |
| ESLint                             | PASS, 오류·경고 0                             |
| 변경 프론트 파일 Prettier          | PASS                                          |
| Java `compileJava compileTestJava` | PASS                                          |
| `git diff --check`                 | PASS                                          |

## MINOR 1 — 실행 수명주기 증거 제한

현재 5173/8080 프로세스는 올바른 클라이언트 워크트리에서 실행 중이지만
`scripts/acceptance/status.ps1`의 manifest 상태는 `not-started`입니다. 따라서
수명주기 스크립트가 프로세스 소유권이나 DevTools 재시작 로그를 직접 증명하지
못합니다.

이번 검증에서는 포트 PID·명령 경로, 클래스 재컴파일, JVM
`RestartClassLoader`, 컴파일 bytecode와 로컬·공개 헬스를 결합해 반영을
확인했습니다. 제품 동작을 막는 문제는 아니지만 다음 수동 재기동 시에는
acceptance lifecycle을 통해 실행해 manifest와 stdout/stderr 포인터를 남기는
것이 좋습니다.

## 롤백

1. `TrackThumbnailField.tsx`의 `TRACK_THUMBNAIL_SQUARE_REQUIRED`를 `true`로
   복원합니다.
2. `CanonicalImageService.java`의 `REQUIRE_SQUARE_TRACK_THUMBNAIL`을 `true`로
   복원합니다.
3. `1:1 필수` 안내와 관련 테스트 기대값을 복원합니다.
4. 대상 테스트와 정적 검증을 다시 실행한 뒤 지정 워크트리의 backend classes를
   갱신하고 로컬·공개 헬스를 확인합니다.

DB·기존 파일·공개 데이터 변경은 없으므로 데이터 롤백은 필요하지 않습니다.
