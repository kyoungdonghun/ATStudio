---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: qa-integ
category: audit
status: stable
dependencies:
  - path: ../agent/WI-20260715-ATS-017-handoff.md
    reason: 승인된 최신 체크포인트 복구 범위
  - path: ../agent/WI-20260715-ATS-017-evidence-pack.md
    reason: 비밀정보를 제거한 상세 검증 증거
---

# WI-20260715-ATS-017 완료 요약

## 판정

**PASS**

최신 승인 체크포인트의 클라이언트 확인용 프리뷰를 다시 열었습니다.

- 브랜치: `codex/acceptance-preview`
- 커밋: `64db91c`
- Git 상태: clean
- 현재 공개 주소: `https://specials-pro-obtained-nuclear.trycloudflare.com`
- 수명주기 상태: `running`
- 실행 구성: 새 런타임이 소유한 터널, 프론트엔드, 백엔드

## 복구 결과

`64db91c` 전용의 새로운 저장소 외부 런타임 루트를 사용했습니다. 현재 기준 `schema.sql`로 승인된 disposable MySQL DB 한 개를 새로 만들었고, 해당 DB를 사용하는 백엔드가 정상 실행 중입니다.

기존 런타임, 기존 disposable DB, 기존 DB 정리 메타데이터는 수정하거나 삭제하지 않고 그대로 보존했습니다. 새 런타임과 새 DB도 사용자의 프리뷰 확인을 위해 종료하거나 삭제하지 않은 채 유지했습니다.

## 접속 확인

응답 본문은 읽거나 저장하지 않고 상태 코드만 확인했습니다.

| 위치 | `/` | `/api/tracks` | `/admin/dashboard` | `/uploads/questions/acceptance-probe.txt` |
|---|---:|---:|---:|---:|
| 로컬 | 200 | 200 | 200 | 401 |
| 공개 주소 | 200 | 200 | 200 | 401 |

Question 첨부파일 직접 접근은 로컬과 공개 주소 모두 익명 요청에 401을 반환해 차단 경계를 통과했습니다.

## 확인 범위와 주의사항

- 이번 PASS는 서버 기동, 기본 화면/API 가용성, 익명 첨부파일 접근 차단을 확인한 제한된 smoke 결과입니다.
- 전체 감사나 로그인 역할별 심층 인수 여정은 실행하지 않았습니다.
- 결제, 메일, OAuth, 데이터 가져오기, 마이그레이션도 실행하지 않았습니다.
- 공개 주소는 Cloudflare Quick Tunnel의 임시 URL입니다. 터널 프로세스나 호스트 세션이 끝나면 주소도 사용할 수 없습니다.
- 문서에는 정확한 DB 이름, JDBC URL, 계정 정보, 비밀번호, 토큰, 프로세스 ID, 응답 본문, 민감한 런타임 경로를 기록하지 않았습니다.

## 종료 및 정리 개념

클라이언트 확인이 끝나면 새 런타임 루트만 대상으로 저장소의 acceptance 종료 절차를 실행해야 합니다. 이후 별도 DB 정리 승인이 있을 때 외부 정리 메타데이터로 대상을 다시 검증하고, 이번에 새로 만든 disposable DB만 삭제해야 합니다. 기존 런타임과 기존 DB 및 메타데이터는 건드리지 않습니다.

이번 WI에서는 종료나 DB 삭제를 실행하지 않았습니다. 현재 프리뷰와 새 disposable DB는 계속 실행 및 보존 중입니다.

## 변경 경로

- `deliverables/agent/WI-20260715-ATS-017-evidence-pack.md`
- `deliverables/user/WI-20260715-ATS-017-summary.md`

## 출력 검증

- 지정된 두 WI-017 문서만 대상으로 한 `git diff --check`: PASS, exit `0`.
- untracked 파일 내용을 보완 확인한 `--no-index --check`: 두 파일 모두 whitespace 진단 없음.

## Related Documents

- [WI-017 Evidence Pack](../agent/WI-20260715-ATS-017-evidence-pack.md): 상태 코드, 보존 상태, 비공개 처리 및 개념적 종료 절차
- [WI-017 Handoff](../agent/WI-20260715-ATS-017-handoff.md): 승인된 범위와 인수 기준
