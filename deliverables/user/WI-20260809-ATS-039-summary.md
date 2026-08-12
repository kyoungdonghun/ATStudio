---
version: 1.2
last_updated: 2026-08-13
project: ATS
owner: se
category: work-summary
status: complete
dependencies:
  - path: ../agent/WI-20260809-ATS-039-evidence-pack.md
    reason: 상세 구현, 검증 결과, 잔여 위험 및 후속 게이트 근거
---

# WI-20260809-ATS-039 보완 요약

## PG 검토와 보완

- 독립 PG 1차 검토 결과는 `FAIL`이었습니다. 기존 썸네일 필터가 체인 실행 전에 `Content-Type`을 설정해, Spring 정적 리소스 처리기가 보관된 `.svg` 또는 `.html` 확장자를 기준으로 값을 덮어쓸 수 있었습니다.
- Album과 Playlist 썸네일 응답에 전용 래퍼를 적용했습니다. 이후 처리 단계가 `setContentType`, `setHeader`, `addHeader`로 `Content-Type`을 바꾸려 해도 최종 값은 `image/jpeg`로 유지됩니다.
- 시스템 임시 경로에만 `.svg`와 `.html` 테스트 파일을 만들어 실제 정적 리소스 HTTP 처리를 검증했습니다. Album과 Playlist에는 고정 안전 헤더가 적용되고, 관계없는 업로드 SVG에는 썸네일 정책이 적용되지 않음을 확인했습니다. 실제 보관 파일은 읽거나 변경하지 않았습니다.
- Notice 다운로드에는 CRLF와 헤더 구분 문자를 포함한 악성 파일명 회귀 테스트를 추가했습니다. 해당 문자는 `filename*` 안에서 percent encoding되고 별도 응답 헤더를 만들지 못합니다.
- 독립 PG 최종 재검토는 `PASS`이며 P1/P2 지적이 없습니다. PG는 응답 래퍼, 실제 정적 리소스 테스트, Notice CRLF 테스트, 범위 미확장을 확인했습니다.

## 유지한 경계

- Album 생성·교체 썸네일은 기존 정규화 절차를 거쳐 서버가 다시 만든 JPEG 바이트와 `.jpg` 키만 PUBLIC 저장소에 기록합니다.
- 새 Notice 첨부파일의 생성·수정·삭제·다운로드는 PRIVATE 저장소를 사용하며, 공개 다운로드 API는 octet-stream과 기존 안전 헤더를 강제합니다.
- Notice 첨부파일의 형식·개수·크기 정책은 만들지 않았습니다. 이 결정은 `WI-20260809-ATS-066`에 남아 있습니다.
- 의존성, DB 스키마, 보관 파일 마이그레이션을 추가하지 않았고 실제 DB, 외부 서비스, secret, 보호된 출력물에 접근하지 않았습니다. commit과 push도 수행하지 않았습니다.

## 검증 결과

- 최초 보완 성공 실행은 `BUILD SUCCESSFUL in 27s`였고, 비대상 `nosniff` 제외 단언까지 추가한 최종 동일 명령도 종료 코드 0으로 완료됐습니다. 최종 JUnit XML은 3개 클래스, 총 20개 테스트 모두 통과, 실패·오류·skip 0입니다.
- 첫 보완 테스트 실행은 현재 Spring 버전에 없는 SVG `MediaType` 상수 2개 때문에 `compileTestJava`에서 중단됐습니다. 표준 MIME 문자열로 테스트를 바로잡은 뒤 동일 명령이 통과했습니다.
- Main의 첫 전체 게이트 실행은 테스트 이후 `build/test-results/test/binary/in-progress-results-*.bin`에 대한 Gradle `NoSuchFileException`으로 중단됐습니다. 테스트 단언 실패는 없었으며 제품 실패로 분류하지 않은 인프라 중단입니다.
- Main이 Gradle daemon을 종료하고 `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --console=plain`로 격리 재실행한 결과, 10개 task가 모두 실행됐고 `BUILD SUCCESSFUL in 2m28s`로 완료됐습니다.
- 전체 Test HTML 결과는 1,560개 중 실패 0, skip 19입니다.
- JaCoCo는 instruction 86.86%, branch 72.02%, line 87.15%, method 84.66%, class 94.81%이며 coverage verification과 assemble이 모두 통과했습니다.
- 문서 검증과 `git diff --check` 결과는 Evidence Pack의 최종 실행 결과를 기준으로 기록했습니다.

## 잔여 위험과 다음 체인

- 기존 PUBLIC 루트에 남아 있는 Notice 파일은 이동하거나 삭제하지 않았습니다. 필요한 경우 별도 승인된 마이그레이션 또는 격리 작업이 필요합니다.
- WI-066 전까지 Notice는 현재 허용 파일 동작을 유지합니다. 이번 조치는 PRIVATE 저장과 강제 다운로드 격리이며 악성코드 무해 판정은 아닙니다.
- 최종 독립 PG 재검토와 Main 전체 backend/JaCoCo/assemble 게이트가 모두 통과해 WI 상태는 `complete`입니다.
- WI-039가 더 이상 차단하지 않는 후속 체인은 `WI-20260809-ATS-048`, `WI-20260809-ATS-050`, `WI-20260809-ATS-055`, `WI-20260809-ATS-066`, `WI-20260809-ATS-071`입니다. 다음 WI의 생성·위임은 Main이 담당하며 이번 문서 전용 마감에서는 수행하지 않았습니다.
