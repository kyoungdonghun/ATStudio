# WI-20260714-ATS-009 완료 요약

## 결과

Playlist thumbnail 업로드 경로에 canonical image validation/re-encode 서비스를 연결했습니다. 신규 Playlist thumbnail은 JPEG/PNG signature와 client MIME을 검증한 뒤 JDK ImageIO로 decode하고, RGB JPEG(`thumbnail.jpg`)로 새로 인코딩한 `MultipartFile`만 `StorageMutationCoordinator`에 전달합니다. 따라서 신규 저장 key는 기존 WI-012 coordinator의 generated key 경로를 통과하며, 제출 파일명/원본 metadata/후행 payload는 저장 bytes에 복사되지 않습니다.

Public thumbnail 응답 경계도 좁혔습니다. `/uploads/playlists/thumbnails/**` 요청에만 `image/jpeg`, `nosniff`, `Content-Security-Policy: default-src 'none'; sandbox`, `Cross-Origin-Resource-Policy: same-origin` 헤더를 붙이도록 했고, `WebConfig`는 WI-012의 public storage root 설정(`app.storage.public-path`)과 같은 root를 서빙하도록 맞췄습니다.

## 검증 범위

- 유효 JPEG/PNG canonical JPEG 변환 테스트 작성
- SVG/HTML/GIF/WebP, MIME mismatch, truncation, APNG, excessive dimensions, oversized input 테스트 작성
- JPEG/PNG 후행 active payload가 canonical output에 남지 않는 테스트 작성
- PlaylistService가 canonicalized file만 storage coordinator에 넘기는 테스트 갱신
- Thumbnail response fixed header/boundary 테스트 작성

## 실행 결과

- `.\gradlew.bat compileJava`: PASS
- `.\gradlew.bat compileTestJava`: PASS
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.config.PublicThumbnailHeaderFilterTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest"`: PASS
  - CanonicalImageServiceTest: 9 tests, failures 0, errors 0
  - PublicThumbnailHeaderFilterTest: 2 tests, failures 0, errors 0
  - PlaylistServiceTest: 22 tests, failures 0, errors 0
- `git diff --check` scoped 실행: PASS, CRLF warning만 출력
- 신규 WI-009 파일 trailing whitespace scan: PASS

Payment/acceptance/auth/storage journal 변경은 수정하거나 되돌리지 않았습니다. Legacy migration, 인증문서(WI-010), GIF/WebP/SVG 지원은 범위 밖으로 유지했습니다.

상세 근거는 `deliverables/agent/WI-20260714-ATS-009-evidence-pack.md`에 기록했습니다.
