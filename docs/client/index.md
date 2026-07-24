---
version: 2.1
last_updated: 2026-07-24
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: testing-guide.md
    reason: Client testing entry point
  - path: ../ui/atstudio-front-list.md
    reason: Current screen-count contract
---

# AT.M 클라이언트 테스트 문서

아래 순서대로 확인하면 됩니다.

1. [테스트 안내](testing-guide.md)
2. [빠른 점검표](1-quick-checklist.md)
3. [전체 기능 점검표](2-full-feature-checklist.md)
4. [관리자 점검표](3-admin-checklist.md)
5. [문제 제보 양식](4-sr-format.md)
6. [AI 정리용 문구](5-ai-prompt.md)
7. [서비스 정책 요약](0-site-policy.md)

최종 PDF는 위 7개 문서를 이 순서로 묶습니다. 이 인덱스와 내부 기능표는 PDF 본문에서 제외됩니다.

내부 유지보수용 현재 수치와 코드 포인터는 [_internal-feature-map.md](_internal-feature-map.md)에 있습니다. 클라이언트 PDF에는 포함하지 않습니다.

테스트 주소와 계정은 운영 담당자가 전달한 것만 사용하세요. 주소가 열리더라도 운영 담당자가 공개 테스트 가능 상태라고 확인하기 전에는 다른 사람에게 전달하지 않습니다.

## PDF 유지보수

PDF와 매니페스트는 직접 수정하지 않고 저장소 루트에서 함께 재생성하고
검증합니다. Python 3.10 이상과 Poppler `pdftoppm` 실행 파일의 절대경로를
현재 셸에만 설정합니다. 이 값은 저장소 파일이나 매니페스트에 기록하지
않습니다.

```powershell
$env:ATSTUDIO_PDF_PYTHON = "<Python 3.10+ executable path>"
$env:ATSTUDIO_PDF_RENDER_TOOL = "<pdftoppm executable path>"
```

다음 명령이 공식 재생 계약입니다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/docs/replay-client-testing-pdf.ps1 -PythonExecutable $env:ATSTUDIO_PDF_PYTHON -RenderTool $env:ATSTUDIO_PDF_RENDER_TOOL
```

래퍼는 `scripts/docs/client-testing-pdf-requirements.txt`의 고정 버전
의존성으로 임시 가상환경을 만들고, PDF 생성, Poppler 렌더링, 매니페스트
검증을 수행한 뒤 임시 환경을 제거합니다. 매니페스트에는 이 재생 명령,
의존성 파일 해시, 실제 Python/라이브러리/Poppler 버전만 기록합니다.
