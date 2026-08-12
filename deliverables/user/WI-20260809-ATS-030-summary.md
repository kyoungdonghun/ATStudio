# WI-20260809-ATS-030 종료 요약

## 결과

공유 프론트엔드 동작에 대한 교차 진입점 소스·기존 assertion·제한적 브라우저 감사가 완료되었습니다.

- 독립 결함: **12건** - `P1` 1건, `P2` 11건.
- 분류: `NEW` 11건, `ADJACENT-REGRESSION` 1건.
- 상세 근거: `deliverables/agent/WI-20260809-ATS-030-findings.md:39-171`.
- 기존 WI-021~WI-029 소유 원인은 새 ID로 중복 발행하지 않고 `OWNER-REFERENCE` 또는 `SHARED-ROOT`로 연결했습니다.
- 자동 검증 PASS는 기존 assertion·컴파일·린트·빌드가 통과했다는 뜻이며, 아래 12개 결함을 해소하거나 서버 응답·영속 상태를 증명하지 않습니다.

## 결함 요약

**P1**

- `F-QAFE-030-002`: 동시 `401` 대기 요청의 재실행에 retry 표식이 없어 갱신이 반복될 수 있습니다.

**P2**

- `F-QAFE-030-001`: 전역 재생 단축키가 버튼·링크·슬라이더 등 다른 포커스 컨트롤의 키 동작을 가로챕니다.
- `F-QAFE-030-003`: 중앙 `401` fallback이 검증된 복귀 경로를 보존하거나 일관되게 Login으로 이동하지 않습니다.
- `F-QAFE-030-004`: Header와 ADMIN 로그아웃 호출자가 로그아웃 트랜잭션 완료 전에 이동합니다.
- `F-QAFE-030-005`: 비어 있지 않은 이미지 URL이 로드 실패할 때 공유 fallback이 없습니다.
- `F-QAFE-030-006`: ADMIN 모바일 내비게이션에 Escape, 포커스 복원, 닫힌 interaction tree 소유권이 없습니다. 이 건만 `ADJACENT-REGRESSION`입니다.
- `F-QAFE-030-007`: 사용자와 ADMIN Question 목록의 상세 이동이 마우스 전용 row/cell로 노출됩니다.
- `F-QAFE-030-008`: Track 다운로드 진입점들이 공통 Blob 오류 정규화를 우회해 같은 오류를 다르게 표시하고 일부 페이지의 정상 읽기 영역까지 대체합니다.
- `F-QAFE-030-009`: Modal opener가 삭제되거나 비활성화되면 복원할 대체 포커스 대상이 없습니다.
- `F-QAFE-030-010`: lazy route import 실패에 대한 애플리케이션 소유 오류 화면·재시도·복구 경로가 없습니다.
- `F-QAFE-030-011`: 공개 Track/Album 목록의 보이는 제목이 semantic heading이 아닌 `div`입니다.
- `F-QAFE-030-012`: 데스크톱 Header가 `Link` 안에 `Button`을 중첩해 동일 명령에 두 개의 독립 focusable node를 만듭니다.

## 자동 검증

| 검사           | 결과                                                     | 한계                                                                          |
| -------------- | -------------------------------------------------------- | ----------------------------------------------------------------------------- |
| Scoped Vitest  | `PASS` - 44 files, 426 tests, 0 failures, skip 표시 없음 | 기존 jsdom/mock assertion 범위이며 누락된 assertion과 결함은 그대로 남습니다. |
| TypeScript     | `npm run typecheck` `PASS`                               | 타입/컴파일 경계만 증명합니다.                                                |
| Scoped ESLint  | `PASS` - 0 warnings                                      | 단일 aggregate 명령과 실행 시간은 제공되지 않았습니다.                        |
| Frontend build | `PASS` - Vite 6.4.3, 272 modules, 2.17s                  | 번들 생성 결과이며 live/server acceptance가 아닙니다.                         |

단일 aggregate Vitest/ESLint 명령과 Vitest 실행 시간은 제공된 증거에 없었습니다. 검사 파일 경계와 재현용 명령 inventory는 `deliverables/agent/WI-20260809-ATS-030-findings.md:327-343`에 보존했습니다.

## 브라우저 확인

안전한 익명 로컬 브라우저에서 현재 고정 viewport `1280x720`만 확인했습니다.

- 방문 경로: `/`, `/tracks`, `/tracks/3`, `/albums`, `/subscriptions`, `/notices`, `/login`, `/signup`, `/missing-screen?from=audit`, `/error`.
- `/profile?from=audit` -> `/login?returnTo=%2Fprofile%3Ffrom%3Daudit`.
- `/playlists?from=audit` -> bare `/login`; 기존 소유자는 `F-UI-021-002`입니다.
- `/subscriptions/manage?plan=STANDARD&cycle=MONTHLY` -> `/login?returnTo=%2Fsubscriptions%2Fmanage%3Fplan%3DSTANDARD%26cycle%3DMONTHLY`.
- `/tracks?keyword=genre01&genre=genre01&genre=genre02&page=1`은 reload 후에도 정확한 URL, keyword, 두 genre chip을 보존했습니다. `/albums` 이동 후 Back/Forward에서도 검색 상태와 `/albums`가 각각 복원됐습니다.
- `/missing-screen?from=audit`와 `/error`는 유효한 한국어 404/500 복구 페이지를 표시했고 Home 링크는 `/`로 돌아갔습니다.
- 나열한 경로에서 현재 폭 기준 horizontal overflow, dialog, file input은 없었습니다.
- 브라우저 DOM에서 `F-QAFE-030-011`과 `F-QAFE-030-012`를 직접 확인했습니다.

최종 정리 상태:

- 로컬 Home `/`.
- viewport `1280x720`.
- active element `BODY`.
- dialogs `0`, file inputs `0`.
- horizontal overflow `false`.
- media elements `0`, playing media `0`.

## 차단·미증명 근거

- `1440x900`, `1024x768`, `390x844`, `360x800` live 검증은 `BLOCKED`입니다. `1280x720` 결과로 대체하지 않았습니다.
- 브라우저 console은 사용할 수 없고 authoritative하지 않아 console error 0건을 주장하지 않습니다.
- 인증 사용자/ADMIN/private 화면, mutation, 결제·구독, Question/Playlist 변경, media 재생·다운로드, file chooser/upload/import/export를 실행하지 않았습니다.
- 직접 API probe, production DB/storage/Provider/audit/secret/session 검사를 하지 않았습니다. 브라우저 navigation 과정의 page-owned read 요청은 server-response 증거로 사용하지 않았습니다.
- 제품/runtime/backend/test/config/schema/fixture/current product documentation 또는 Git 상태를 변경하지 않았습니다.
- `output/client-demo-screenshots-20260716-140514.zip`은 열기·읽기·hash·metadata 확인·이동·교체·삭제·stage·fixture 사용 없이 그대로 보존했습니다.
- 따라서 server response, durable state, binary completion, authenticated role, route-chunk failure, assistive technology, 전체 responsive acceptance는 통과로 분류하지 않았습니다.

## WI-031 결정 질문

1. 다운로드 성공은 entitlement/resource의 영속 부여를 뜻합니까, 아니면 client byte 전달 완료를 뜻합니까? 소유자: `F-INTEG-029-A03`.
2. 중앙 refresh 실패 후 어떤 인증 경로를 안전한 `returnTo`로 보존할 수 있으며 ADMIN 경로도 허용합니까?
3. refresh token이 없을 때 interceptor가 즉시 이동해야 합니까, 아니면 identity만 지우고 route guard가 이동을 소유해야 합니까?
4. 로그아웃 이동은 server revocation을 기다려야 합니까, 아니면 즉시 local logout 후 제한된 background revocation feedback을 제공해야 합니까?
5. 공개 shell을 사용하는 ADMIN이 active subscription/license 없이 공식 Track을 다운로드할 수 있습니까, 아니면 USER와 같은 entitlement 표시를 따라야 합니까?
6. 비어 있지 않은 이미지 URL이 실패할 때 사용할 canonical fallback asset과 alt 정책은 무엇입니까?
7. 시작된 Track 다운로드 또는 취소 불가능한 Playlist mutation이 route/modal보다 오래 지속되면 global feedback과 함께 계속합니까, 조용히 분리합니까, 아니면 invocation 전에 취소합니까?

이 감사에서는 위 결정을 선택하지 않았습니다.

## 다음 WI와 종료 경계

직접 다음 작업은 `WI-20260809-ATS-031`입니다. WI-031은 제품 수정 전에 WI-021~WI-030 finding을 통합·중복 제거하고, 공통 root cause로 묶고, 소유 ID와 증거 lane을 보존하며, 위 7개 정책 질문을 명시적인 decision gate로 처리해야 합니다. 이 절차 전에는 개별 product fix를 선택하거나 구현하면 안 됩니다.

이번 WI의 산출물은 문서뿐입니다. 철회할 경우 `deliverables/agent/WI-20260809-ATS-030-evidence-pack.md`와 `deliverables/user/WI-20260809-ATS-030-summary.md`만 제거하면 되며 제품·runtime·DB·storage·browser state 정리는 필요하지 않습니다.

최종 문서 검사가 완료되었습니다. handoff/findings/evidence/summary 대상 최초 Prettier check는 exit 1로 4개 모두 formatting이 필요했고, 같은 4개에 대한 Prettier `--write`는 exit 0으로 완료됐습니다(handoff 69ms, findings 129ms, evidence 37ms, summary 22ms). 최초 최종 Prettier check는 exit 0으로 모든 대상 파일이 Prettier code style을 사용했습니다. docs validation은 exit 0으로 Tier 0, internal links, 542 traceability IDs, document index와 전체 validation을 통과했고, `git diff --check`도 exit 0, 출력 없음이었습니다. 이후 closeout patch 뒤 Prettier check가 exit 1로 한 번 실패했으며 evidence-pack만 formatting이 필요했습니다. evidence-pack 단독 Prettier `--write`는 exit 0, 65ms였고, WI-030 문서 4개 전체 최종 recheck는 exit 0으로 모든 대상 파일이 Prettier code style을 사용했습니다. 반복 docs validation은 exit 0으로 542 traceability IDs와 전체 validation을 통과했고, 반복 `git diff --check`도 exit 0, 출력 없음이었습니다. 단, `git diff --check`는 tracked diff만 검사하고 4개 산출물은 untracked이므로, 산출물 자체의 직접 검사는 Prettier와 docs validation 결과를 근거로 합니다.
