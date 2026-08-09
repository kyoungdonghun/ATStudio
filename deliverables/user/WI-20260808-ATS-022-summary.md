# WI-20260808-ATS-022 작업 요약

## 상태

**문서 보정은 COMPLETE이며, WI-20260809-ATS-001 repair로 3-way consistency가 복원되었습니다. WI-023~027은 각자의 승인 범위와 게이트 아래 unblocked입니다.**

SR-94~101 구현 결과를 현재 코드, API, `schema.sql`, WI-014~021 증거와 대조해 설계, UI, 운영, 레지스트리, 용어, 인덱스 문서를 갱신했습니다. 36개 변경 시스템 문서와 두 WI-022 산출물의 명시적 SR/WI 참조를 다시 전수 검색한 결과, 개별 SR 문서와 다른 시스템 문서의 연결은 승인 매핑과 일치했고 이 요약의 기존 계약 목록만 SR-94~99 및 SR-101의 기능을 잘못 연결하고 있었습니다. 해당 목록을 아래와 같이 교정했습니다.

WI-022 완료 시점에는 문서 밖 구현 불일치 두 건이 확인되었습니다. 이 historical finding은 WI-20260809-ATS-001 repair로 해소되었으며, 문서·코드·검증의 3-way consistency가 복원되었습니다.

**Post-WI-20260809-ATS-001 verified update:** both implementation mismatches were resolved. The disposable validator now matches `41/493/168/89`, six seeded plans, and SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`; isolated `[redacted probe]` and `[redacted proof]` runs passed their required cleanup checks. The retired direct subscription mutation matchers are absent and their focused regression remains passing. This closes these two blockers only; successor WIs retain their own gates.

## 문서화한 현재 계약

- API: 25 controllers, 144 method mappings (`GET 69`, `POST 41`, `PUT 20`, `DELETE 14`).
- DB source: the verified fresh manifest is 41 tables, 493 columns, 168 physical indexes, 89 foreign keys, 6 plans, and SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`. The earlier 39-table-plus-additive-DDL reconciliation is preserved as historical WI-022 evidence.
- SR-94·95 / WI-017: Tag 이름 정규화·검증, 사전 중복 안내, DB 경합의 `TAG_NAME_DUPLICATED` 변환, 저장 실패 시 모달·입력·목록·필터 상태 보존을 같은 계약으로 사용합니다.
- SR-96 / WI-014: 자기 강등·마지막 관리자 강등·동시 강등을 차단하고, 역할 변경 감사, Refresh Token 제거, 현재 역할 재동기화와 stale-result guard를 적용합니다.
- SR-97 / WI-015: 관리자 구독 보정은 외부 결제·환불을 호출하지 않는 명시적 Local Subscription Correction이며, 승인, idempotency, 동시성, 감사와 provider agreement 상태를 분리합니다.
- SR-98 / WI-020: 신규·교체 Track 썸네일은 정확한 1:1을 강제하고, 기존 비정사각형 썸네일은 명시적 교체 전까지 보존합니다.
- SR-99 / WI-016: Audio Analysis Dry Run은 duration 차이와 후보 작업을 읽기 전용으로 보고합니다. 기존 행 backfill은 실행하지 않았고 실제 backfill은 별도 승인 대상입니다.
- SR-100 / WI-021: 검색은 Genre, Mood, Instrument, Usage 반복 파라미터와 AND 의미를 사용합니다. Usage는 License가 아니며, Home fallback과 available-tag 실패 fallback을 구분합니다.
- SR-101 / WI-018·019: 2초 이상 지속되는 buffering과 실제 재생 오류를 분리하고, public·bounded·active-only `POST /api/tracks/batch` 및 ID 기반 player/history hydration으로 순서와 stale-result fence를 유지합니다.
- API wrappers use `dataList` and `pageInfo` where applicable.
- Full browser acceptance, production deployment, real data backfill, and external provider actions remain future gates.

## 발견 사항

### Historical implementation mismatch 1/2

At WI-022 completion, `scripts/database/DisposableMysqlBootstrap.java:47-52` expected 39 tables, 449 columns, 153 indexes, 80 foreign keys, and the prior manifest hash. Current `schema.sql` already contained the two WI-014/015 tables. This historical source inspection established that the then-current validator would reject the 41-table baseline until a repair WI regenerated the constants and hash.

**Resolved by WI-20260809-ATS-001:** a controlled isolated probe observed `41/493/168/89`, six plans, and SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`; the expected mismatch cleanup and independent proof create/validate/two-drop checks passed with target names redacted.

### Historical implementation mismatch 2/2

At WI-022 completion, `SecurityConfig` retained authorization matchers for the removed direct `PUT`/`DELETE /api/user-subscriptions/*` admin routes. No controller method exposed those routes, so the matchers did not create a live undocumented API, but controller, API documentation, and security configuration were not fully aligned.

**Resolved by WI-20260809-ATS-001:** the retired matchers were removed and the direct-controller-mapping regression test passed.

## 변경 파일

SR and indexes:

- `docs/SR/SR-94.md`
- `docs/SR/SR-95.md`
- `docs/SR/SR-96.md`
- `docs/SR/SR-97.md`
- `docs/SR/SR-98.md`
- `docs/SR/SR-99.md`
- `docs/SR/SR-100.md`
- `docs/SR/SR-101.md`
- `docs/SR/index.md`
- `docs/index.md`

Design and use cases:

- `docs/design/api-spec.md`
- `docs/design/db-schema.md`
- `docs/design/index.md`
- `docs/design/payment-integration-design.md`
- `docs/design/payment-operations-runbook.md`
- `docs/design/usecase/download-queue.md`
- `docs/design/usecase/index.md`
- `docs/design/usecase/likes.md`
- `docs/design/usecase/sound-album.md`
- `docs/design/usecase/sound-playhistory.md`
- `docs/design/usecase/sound-playlist.md`
- `docs/design/usecase/sound-tag.md`
- `docs/design/usecase/sound-track.md`
- `docs/design/usecase/user-info.md`
- `docs/design/usecase/user-subscription.md`

UI, client, operations, and registries:

- `docs/ui/atstudio-front-list.md`
- `docs/ui/index.md`
- `docs/ui/modal-list.md`
- `docs/ui/screen-flow.md`
- `docs/client/3-admin-checklist.md`
- `docs/client/_internal-feature-map.md`
- `docs/client/testing-guide.md`
- `docs/payment/feature-inventory.md`
- `docs/registry/project-registry.md`
- `docs/standards/glossary.md`
- `scripts/database/README.md`

WI outputs:

- `deliverables/user/WI-20260808-ATS-022-summary.md`
- `deliverables/agent/WI-20260808-ATS-022-evidence-pack.md`

Total: 38 files (36 system documents and 2 WI outputs). No production, test, SQL, or configuration source file was edited by WI-022.

## 검증 결과

| Command                                                                                                           | Result                                                                                                             |
| ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| `$env:PYTHONIOENCODING='utf-8'; python .agents/skills/validate-docs/scripts/validate_docs.py`                     | PASS: Tier 0 references, internal links, 510 trace IDs, and index coverage passed.                                 |
| Project `sync-docs-index` rules: recursive Design count; top-level count elsewhere; all `index.md` files excluded | PASS: all 14 categories match `docs/index.md`; total 201 managed Markdown files.                                   |
| Controller annotation inventory command recorded in the Evidence Pack                                             | PASS: GET 69, POST 41, PUT 20, DELETE 14, total 144; 25 controller files.                                          |
| `$env:PYTHONIOENCODING='utf-8'; python .agents/skills/lint/scripts/lint_all.py`                                   | NOT RUN TO COMPLETION: `markdownlint`, `jq`, and `ruff` are not installed or on PATH. No dependency was installed. |
| `frontend/node_modules/.bin/prettier.cmd --check` over all changed Markdown                                       | 30 baseline-style differences reported. Historical SR and mixed-format documents were not mass-reformatted.        |
| `frontend/node_modules/.bin/prettier.cmd --check` for the two WI-022 outputs                                      | PASS after formatting only the corrected WI outputs; no broad rewrite occurred.                                    |
| `git diff --check`                                                                                                | PASS: no whitespace errors; existing CRLF-to-LF warnings only.                                                     |

Application suites, builds, coverage, browser acceptance, and database/provider operations were intentionally not run because later WIs own them or this WI forbids them.

## 보존 및 미완료 경계

- WI-014~021의 기존 dirty implementation and evidence changes were preserved.
- `output/client-demo-screenshots-20260716-140514.zip` remains untracked and untouched at 700,703 bytes.
- No files were deleted, staged, or committed.
- No schema, persistent data, secrets, provider, storage, or external system was touched.
- SR-94~101 remain `OPEN` in the SR registry until their remaining full-suite, browser, operational, backfill, deployment, or external gates are satisfied.

## 다음 WI 상태

WI-20260809-ATS-001 completed the approved repair: the verified manifest constants/hash are current, the retired SecurityConfig matchers are absent, and focused checks passed. WI-022 is documentation complete with 3-way consistency restored. WI-023~027 are unblocked, while each retains its own approved scope and execution gates.
