# WI-20260724-ATS-003 완료 요약

## 결과

클라이언트 테스트 PDF의 생성·검증 기록에서 사용자 PC에 종속된 Python과
Poppler 절대경로를 제거했습니다.

- 생성 명령은 `python scripts/docs/generate_client_testing_pdf.py` 형태의
  이식 가능한 명령으로 기록합니다.
- `--render-tool`은 PATH에서 찾을 수 있는 `pdftoppm` 같은 명령 이름과
  명시적 실행 파일 경로를 모두 받습니다.
- 실행 파일의 절대경로는 저장하지 않고 `pdftoppm`이라는 명령 식별자와
  실제 조회한 버전만 기록합니다.
- 렌더 도구를 전달하지 않으면 버전을 추측하지 않고 `not captured`로
  기록합니다.
- 검증기는 Windows/macOS의 `/Users/` 및 Linux의 `/home/` 사용자 경로가
  매니페스트에 들어오면 실패합니다.
- PDF 본문을 구성하는 클라이언트 원문은 수정하지 않았습니다.

## 산출물

- PDF: `output/pdf/atstudio-client-testing-guide.pdf`
- 매니페스트: `output/pdf/atstudio-client-testing-guide.manifest.json`
- PDF SHA-256:
  `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4`
- 매니페스트 SHA-256:
  `644659f08baf747ca3fbf3d112f9b15a7fa0563d6bd6076668678954411daafc`
- 페이지: 12
- 원문 구간 검증: 295/295, 100%

기존 PDF 매니페스트는 현재 저장소에 추적 중인 클라이언트 원문 세 파일의
과거 해시를 가리키고 있었습니다. 이번 재생성은 원문을 바꾸지 않고 현재
원문 해시와 PDF를 다시 일치시켰습니다.

## 검증

- 지정된 bundled Python으로 생성: 통과
- 지정된 Poppler에서 버전 `26.05.0` 조회: 통과
- 매니페스트에 기록된
  `python scripts/docs/generate_client_testing_pdf.py --render-tool pdftoppm`
  명령을 PATH 기반으로 그대로 재실행: 통과
- 명시적 Poppler 절대경로 입력과 매니페스트 경로 비노출: 통과
- `--render-tool` 포함 생성·검증: 통과
- `--render-tool` 미포함 생성·검증: 통과
- 같은 원문으로 두 번 생성한 PDF 해시 일치: 통과
- 12페이지 Poppler 렌더 및 전 페이지 시각 검사: 통과
- 한글 글리프, 표제, 목록, 페이지 경계, 잘림·겹침: 이상 없음
- Python 구문 검사: 통과
- 사용자별 런타임 경로 잔존 검색: 0건
- 대상 파일 `git diff --check`: 통과

## 위험 및 롤백

- 매니페스트 스키마는 portable provenance 계약을 명시하기 위해 1에서 2로
  변경됐습니다. 현재 저장소에서 이 매니페스트를 읽는 실행 코드는 검증기
  하나뿐이며 함께 갱신했습니다.
- 기록된 명령을 재실행하려면 `python`과 `pdftoppm`이 PATH에 있어야
  합니다. 그렇지 않은 환경에서는 동일 파서에 각 실행 파일의 명시적
  경로를 전달할 수 있습니다.
- Poppler 렌더 중 시스템에 없는 보조 표시 글꼴 경고가 출력됐지만, PDF에
  사용된 한글은 12페이지 모두 정상 렌더링됐습니다.
- 롤백하려면 생성기, 검증기, PDF, 매니페스트 네 파일을 함께 되돌려야
  합니다. 일부만 되돌리면 해시와 스키마 계약이 어긋납니다.

## 다음 상태

WI-20260724-ATS-003은 완료됐으며 커밋하지 않았습니다. 승인된 작업계획에
따라 WI-20260724-ATS-006의 문서 정합성 검증을 진행할 수 있습니다.
