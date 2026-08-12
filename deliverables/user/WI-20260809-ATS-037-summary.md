---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: work-summary
status: active
related_wi: WI-20260809-ATS-037
dependencies:
  - path: ../agent/WI-20260809-ATS-037-evidence-pack.md
    reason: 상세 변경, 테스트, 롤백 근거
  - path: ../agent/WI-20260809-ATS-037-handoff.md
    reason: 승인 범위와 인수 기준
---

# WI-20260809-ATS-037 사용자 요약

## 결과

Playlist Drawer의 드래그 및 터치 순서 변경이 백엔드 계약과 같은 0 기반
연속 순서(`0..n-1`)를 전송하도록 수정했습니다. 화면에 먼저 반영되는
낙관적 순서도 API payload와 같은 값을 사용합니다.

순서 변경 요청이 거절되면 잘못된 낙관 순서를 즉시 제거하고, 서버의
권위 있는 Playlist 상세를 정확히 한 번 다시 조회합니다. 재조회도
실패하면 마지막으로 확인된 순서를 유지하며 추가 mutation, 재시도,
polling은 수행하지 않습니다.

## 변경 파일

- `frontend/src/components/player/PlaylistDrawer.tsx`: 0 기반 매핑과 제한된
  거절 복구
- `frontend/src/components/player/playerComponents.test.tsx`: drag/touch 정확한
  payload, 낙관 순서, no-op, 성공/실패 reload 회귀 테스트
- `frontend/src/api/domainApis.test.ts`: Playlist reorder API의 0 기반 요청
  assertion
- `deliverables/agent/WI-20260809-ATS-037-handoff.md`: 입증된 acceptance 항목만
  체크
- `deliverables/agent/WI-20260809-ATS-037-evidence-pack.md`: 상세 증거와 롤백
- `deliverables/user/WI-20260809-ATS-037-summary.md`: 이 사용자 보고서

현재 Playlist use case와 API specification은 이미 정확한 0 기반 계약을
기술하고 있어 WI-037에서는 수정하지 않았습니다. WI-036이 변경한 파일도
건드리지 않았습니다.

## 검증

- 최종 집중 테스트:
  `npm test -- --run src/components/player/playerComponents.test.tsx src/pages/subscriber/PlaylistEditPage.test.tsx src/api/domainApis.test.ts`
  -> 3 files, 28 tests 모두 PASS, exit 0
- Red 재현: 구현 전 같은 범위에서 27 tests 중 25 PASS, 2 FAIL. drag와
  touch가 실제로 `1,2`를 보내 `0,1` assertion에 실패했습니다.
- 인접 Playlist 테스트: 5 files, 37 tests 모두 PASS.
- Main 최종 전체 프론트 테스트: 73 files, 833 tests 모두 PASS.
- Main 최종 coverage: statements 88.61%, branches 79.73%, functions 88.16%,
  lines 90.84%로 설정된 기준을 통과했습니다.
- typecheck, ESLint, Prettier check 모두 PASS.
- production build PASS, 274 modules transformed.
- 문서 validation PASS, 566 traceability IDs 확인.
- `git diff --check` exit 0. 기존 `security-policy.md` CRLF warning만 있었고
  WI-037 변경으로 발생한 오류는 없습니다.

Main의 최종 품질 게이트까지 모두 확인되어 handoff의 남은 acceptance
항목을 완료로 표시했습니다.

## 잔여 위험과 롤백

- 로컬 mock 기반 회귀 테스트만으로 복구 경로를 검증했으며 live backend,
  DB, 외부 서비스는 사용하지 않았습니다.
- 권위 재조회 실패 시 사용자에게 오류를 표시하는 정책은 후속 Playlist
  mutation recovery 범위이며 이번 WI에는 포함하지 않았습니다.
- 롤백 시 Drawer 매핑/복구, 집중 테스트, API assertion, handoff 체크,
  두 WI 산출물을 함께 되돌리면 됩니다. 데이터 롤백은 없습니다.

## 다음 WI

다음 체인은 `WI-20260809-ATS-045`이며, 이후 `WI-20260809-ATS-046`이
Playlist mutation 복구를 별도 범위로 다룹니다. `WI-20260809-ATS-058`도
WI-037이 차단하던 별도 후속 WI입니다. Main의 전체 품질 게이트 기록 후
체인을 진행해야 합니다.

## 관련 문서

- [상세 Evidence Pack](../agent/WI-20260809-ATS-037-evidence-pack.md)
- [WI-037 Handoff](../agent/WI-20260809-ATS-037-handoff.md)
- [Playlist Use Case](../../docs/design/usecase/sound-playlist.md)
- [API Specification](../../docs/design/api-spec.md)
