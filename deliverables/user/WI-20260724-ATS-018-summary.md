---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20260724-ATS-018-handoff.md
    reason: Approved WI scope and acceptance criteria
  - path: ../agent/WI-20260724-ATS-018-evidence-pack.md
    reason: Reproducible implementation and verification evidence
---

# WI-20260724-ATS-018 Summary

## 판정

**PASS**입니다. EOL, PDF source hash, font preflight 구현과 집중 테스트를
완료했고, 고정된 Python 3.12.13 runtime으로 수행한 독립 replay에서 기존
PDF·manifest가 byte-identical하게 재현되었습니다.

## 변경 내용

- 저장소 text 파일의 checkout 줄바꿈을 LF로 고정했습니다.
- Windows batch 파일은 CRLF, PDF·이미지·음원·폰트·압축 파일은 binary로
  명시했습니다.
- PDF source record의 hash와 byte count를 UTF-8/LF 정규화 기준으로
  생성하고 동일한 공통 함수로 검증합니다.
- Manifest JSON은 `Path.write_text()` 대신 명시적인 UTF-8/LF bytes로
  기록해 Windows에서도 raw CRLF가 생성되지 않습니다.
- Malgun Gothic regular/bold 파일이 없거나 승인된 SHA-256과 다르면 PDF
  생성 전에 명확한 오류로 중단합니다.
- LF, CRLF, lone CR 입력이 같은 hash와 byte count를 만드는 집중 테스트를
  추가했습니다.
- 추적 파일을 일괄 renormalize하지 않았고 client 문서 내용, PDF layout,
  product behavior는 변경하지 않았습니다.

## 검증 결과

| 검증 | 결과 |
|---|---|
| Python compile | PASS |
| Focused Python tests | PASS, 5/5 |
| Git EOL attribute check | PASS |
| Frontend Prettier | PASS |
| PDF verify/replay | PASS, 12 pages, 295/295 source segments |
| Post-fix exact-runtime replay | PASS, exit 0, 14.701 seconds |
| `git diff --check` | PASS |
| Existing PDF SHA reproduction | PASS |
| PDF and manifest diff | PASS, `git diff --exit-code` |
| Manifest raw line endings | PASS, CRLF 0, CR 0, final LF present |

독립 replay는 bundled Python
`C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`
버전 3.12.13과 고정된 Poppler `pdftoppm.exe`를 사용했습니다. PDF SHA-256은
`d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4`,
manifest SHA-256은
`f05cace32f363b4cd97ebfce0b86d1c33094bf31103d266b3b1cd5d97cb916fb`으로
기존 artifact와 정확히 일치했습니다.

수정 전 Windows replay의 `Path.write_text()`는 Git normalization으로
숨겨지는 CRLF manifest를 만들 수 있었습니다. 수정 후 raw manifest는
3,973 bytes, CRLF 0건, CR 0건이며 LF로 끝납니다.

Python 3.14.3은 PDF bytes를 다르게 생성합니다. 따라서 byte-identical
증거에는 위의 고정된 explicit Python 3.12.13 runtime을 사용해야 합니다.

## 운영 주의사항

- Poppler는 display font lookup warning을 출력했지만 rendering과 source
  coverage 검증은 통과했습니다.
- 현재 시스템 Python에는 `pypdf`가 없어 verifier 단독 실행은 실패합니다.
  고정된 runtime과 정식 replay 스크립트의 임시 격리 환경에서는 정상
  통과했습니다.

## Related Documents

- [WI-018 Handoff](../agent/WI-20260724-ATS-018-handoff.md)
- [WI-018 Evidence Pack](../agent/WI-20260724-ATS-018-evidence-pack.md)
