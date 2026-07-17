# WI-20260716-ATS-037 문서·저장소 잔여 자산 조사 요약

## 결론

- 이번 WI는 읽기 전용으로 수행했습니다. 코드, 기존 문서, Git 브랜치/worktree, 생성물, 공개 서버는 변경하지 않았습니다.
- ATStudio V1 통합 전에 정리할 대상은 확인됐지만, REQ/WI/SR/audit/retrospective 기록은 삭제하거나 현재 문체로 다시 쓰지 않는 것이 맞습니다.
- 활성 문서에서는 **14개 파일의 현행화(REPLACE)**, **2개 완료 설계의 제자리 아카이브(ARCHIVE)**가 필요합니다.
- Git에는 **38개 worktree 등록**이 있으며, 이 중 **35개는 이미 경로가 사라진 prunable 등록**입니다. 나머지 3개는 개발, acceptance preview, client demo worktree입니다.
- 로컬 브랜치 44개 중 공식 후보 1개는 유지하고, 개발 브랜치에 이미 병합된 40개는 승인 후 정리할 수 있습니다. 독자 커밋이 있는 3개는 별도 검토가 필요합니다.

## 가장 중요한 발견

1. `docs/design/payment-integration-design.md`에 `draft`와 `stable` front matter가 연속으로 두 번 존재합니다. 인덱스가 잘못된 `draft` 상태를 표시하는 직접 원인입니다.
2. 9개 활성 문서가 분리된 개발/client-demo 브랜치와 Vite 6.4.1/6.4.3 상태를 현재 사실처럼 기록합니다. V1 단일 브랜치 전환 시 공식 브랜치와 환경 검증 기준 중심으로 바꿔야 합니다.
3. `docs/design/remaining-remediation-design-20260716.md`와 `docs/design/p1-security-acceptance-hardening-design.md`는 완료된 시점의 설계·조사 기록입니다. 파일을 이동하지 않고 archive metadata와 대체 경로를 추가하는 편이 기존 79개 참조 파일을 깨뜨리지 않습니다.
4. `scripts/demo/seed-client-demo.ps1`의 기본 credentials 경로가 특정 `acceptance-preview-64db91c` 실행 폴더에 하드코딩되어 있습니다. 스크립트는 유지하되 경로를 명시 입력 또는 공식 runtime root 기준으로 대체해야 합니다.
5. 소스 파일 이름 기준 `.bak`, `.backup`, `.old`, `copy`, `backup` 형태의 백업 파일은 발견되지 않았습니다. 의미상 죽은 fallback/호환 코드는 다른 코드 조사 WI와 함께 판정해야 합니다.

## 생성물·작업공간 정리 후보

| 대상 | 현재 규모 | 권고 |
|---|---:|---|
| `.claude/worktrees/*`의 끊어진 Git 등록 | 35개 | 사용자 승인 후 prune 및 대응 `claude/*` 로컬 브랜치 제거 |
| `ATStudio-acceptance-preview`, `ATStudio-client-demo-stable` | worktree 2개 | 공개 서버를 공식 브랜치로 전환한 뒤 제거 |
| `output/demo-seed/` | 73개, 32,025,608 bytes | 재생성 가능하므로 제거; seed 스크립트는 유지 |
| `tmp/` | PNG 35개, 5,241,016 bytes | PDF 검증 임시 렌더이므로 제거 |
| 펼쳐진 client-demo 캡처 폴더 | 52개, 1,177,769 bytes | ZIP 검증 후 제거 |
| client-demo 캡처 ZIP | 1개, 700,703 bytes | 현재 SoT가 아닌 역사적 캡처로 외부 보관 |
| `.codex-remote-attachments/` | 1개, 68,040 bytes | 원본 필요 여부 확인 후 작업공간에서 제거 |
| Cloudflare/Vite 로그 | 4개, 4,249 bytes | 현재 공개 서버 중지·전환 후 제거 |
| `frontend/tsconfig.tsbuildinfo` | 추적 중인 생성물 1개 | Git 추적 해제 후 기존 `*.tsbuildinfo` ignore 적용 |

## 보존 대상

- `docs/SR/` 93개 파일, `docs/audit/` 7개 파일, `docs/retrospective/` 5개 파일
- 조사 시작 시점 `deliverables/` 1,027개 파일: REQ/WI 이력으로 보존
- `docs/design/base-agent.md`, `docs/design/p1-payment-db-integrity-design.md`: archive metadata가 이미 적절함
- `scripts/acceptance/` 6개: 별도 acceptance 프로필과 외부 환경값을 요구하는 운영 도구이며 임시 우회 코드로 보지 않음
- `scripts/docs/` 2개와 추적 중인 client PDF/manifest 2개
- V1 통합 전 개발/client 기준 태그 2개

## 별도 승인 후 가능한 파괴적 작업

- 35개 prunable worktree 등록과 대응 브랜치 정리
- 공식 런타임 전환 후 auxiliary worktree 2개와 병합 완료 로컬 브랜치 정리
- 생성물, 로그, attachment, 펼쳐진 캡처 폴더 삭제
- `frontend/tsconfig.tsbuildinfo` 추적 해제
- 완료 설계의 archive metadata 변경과 14개 활성 문서 현행화

`validate-docs`는 Tier 0, 내부 링크, 426개 추적 ID, 문서 인덱스를 모두 통과했고 실행 전후 Git 상태도 동일했습니다. 상세 판정과 재현 명령은 `deliverables/agent/WI-20260716-ATS-037-evidence-pack.md`에 기록했습니다.
