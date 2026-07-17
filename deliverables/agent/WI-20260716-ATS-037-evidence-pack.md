# Evidence Pack: WI-20260716-ATS-037

## Summary (one-liner)

- Completed a read-only documentation, worktree, branch, generated-artifact, and repository-hygiene audit for the ATStudio V1 consolidation baseline.

## Scope / DoD Check

- [x] Distinguished live operational documents from historical REQ/WI, SR, audit, and retrospective records.
- [x] Classified documentation and repository candidates as `KEEP`, `REMOVE`, `REPLACE`, `ARCHIVE`, or `REVIEW` with exact evidence.
- [x] Inventoried worktrees, branches, logs, screenshots, demo seed output, temporary PDF renders, attachments, tracked generated files, and ignore gaps.
- [x] Recorded index/link consequences and approval-sensitive cleanup sequencing.
- [x] Ran documentation validation and confirmed that it did not change repository state.
- [x] Did not delete, move, archive, prune, format, stage, commit, switch branches, alter runtime, or modify files outside the two WI outputs.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approval boundary |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure and metadata |
| 0 | `docs/standards/development-standards.md` | Repository and implementation evidence context |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/versioning-policy.md` | Deprecation and archive procedure |
| 1 | `docs/policies/archive-policy.md` | Live, historical, archived, and generated classification |
| 1 | `docs/policies/execution-policy.md` | Read-only and destructive-action approval boundary |
| 1 | `docs/policies/quality-gates.md` | Verification requirements |
| 2 | `docs/index.md` | Root documentation registry and current-state claims |
| 2 | `docs/registry/project-registry.md` | Current project baseline |
| 2 | `docs/registry/workboard.md` | Advisory tracking registry |
| 2 | `docs/SR/index.md` | Historical SR inventory |
| 2 | `docs/design/index.md` | Design status registry |
| REQ | `deliverables/user/REQ-20260716-ATS-004.md` | Approved V1 consolidation scope |
| WI | `deliverables/agent/WI-20260716-ATS-037-handoff.md` | Output and non-mutation contract |

## Audit Snapshot

| Item | Observed value |
|---|---|
| Branch | `codex/p1-acceptance-hardening` |
| HEAD | `a96d2e0c5d249723bbf449b6834299a04cf2ad30` |
| Local branches | 44 |
| Worktree registrations | 38: 35 prunable, 3 active |
| Tracked docs files | 237 |
| Markdown docs | 210 |
| Deliverables present at audit start | 1,027 files; 1,022 tracked plus 5 current untracked REQ/WI orchestration files |
| Historical doc zones | `docs/SR/` 93 files; `docs/audit/` 7; `docs/retrospective/` 5 |
| Named backup-file search | 0 matches after excluding dependencies/build/reference-data noise |

The archive policy classifies `docs/SR/`, `docs/audit/`, `docs/retrospective/`, and `deliverables/` as historical records and `.claude/worktrees/` as generated workspace state (`docs/policies/archive-policy.md:36-38`). Live documents must remain current, while historical records must not be normalized into present tense (`docs/policies/archive-policy.md:49-54`).

## Documentation Inventory

### Actionable count

| Disposition | Count | Meaning |
|---|---:|---|
| `REPLACE` | 14 live documents | Update in place; do not create version-suffixed copies |
| `ARCHIVE` | 2 completed design documents | Archive in place with notice and replacement path |
| `REVIEW` | 5 primary compatibility documents | Resolve only with backend/frontend/DB findings |
| `KEEP` | Historical zones and verified current assets | Preserve paths and history |

### `REPLACE` candidates

| ID | Path(s) | Exact evidence | Recommendation and link/index impact |
|---|---|---|---|
| DOC-01 | `docs/design/payment-integration-design.md` | Lines 1-21 declare v0.8/draft with `docs/design/...` dependency paths; lines 23-37 start a second v2.0/stable front matter. It is the only domain document with duplicate `version:` fields in its first 60 lines. | Keep one v2.0/stable front matter and valid relative dependencies. Preserve the path because 142 files reference the filename. No consumer-link rewrite should be needed. |
| DOC-02 | `docs/design/index.md` | Lines 31-35 show payment design/runbook/policy/settlement statuses as draft while the runbook is stable (`payment-operations-runbook.md:1-13`), the integration document's current front matter says stable (`payment-integration-design.md:23-37`), and policy text says the operations are implemented (`payment-refund-receipt-settlement-policy.md:13-26`). | Align status/description with the corrected documents; update completed design archive rows in the same edit. |
| DOC-03 | `docs/design/payment-refund-receipt-settlement-policy.md` | Front matter is draft at line 7, but lines 14-26 and 44-63 describe implemented refund, receipt, audit, entitlement, settlement, and admin UI behavior as current policy. | Promote the current policy to stable after WI-038 confirms implementation parity. Keep the path; 48 files reference it. |
| DOC-04 | `docs/design/payment-settlement-import-design.md` | Front matter is draft at line 7 and lines 35-39 call CSV/system reconciliation the first implementation, while the current payment policy states settlement APIs/UI and ledger are implemented (`payment-refund-receipt-settlement-policy.md:14,57,63`). | Rewrite future-tense implementation language as current behavior and set a verified lifecycle status. Keep the path; 15 files reference it. |
| DOC-05 | `docs/registry/workboard.md` | Lines 20-23 describe current progress tracking, then disclaim current SoT; rows 86-87 retain two old system WIs as apparent live entries. | Make the purpose explicitly advisory/template-only and remove current-state ambiguity. Preserve old WI history in deliverables rather than this live registry. |
| DOC-06 | `docs/index.md:73`, `docs/client/testing-guide.md:27`, `docs/client/_internal-feature-map.md:46-48`, `docs/payment/index.md:72`, `docs/payment/feature-inventory.md:153-154`, `docs/payment/known-limits-and-next-steps.md:46`, `docs/payment/acceptance-test-checklist.md:28`, `docs/ui/screen-flow.md:72`, `docs/ui/atstudio-front-list.md:79` | Nine live files hard-code a split development/client-demo branch boundary and Vite 6.4.1/6.4.3 audit snapshot. The public runtime is currently launched from `ATStudio-client-demo-stable`, while REQ-004 intends one official branch. | After consolidation verification, replace branch-specific prose with the official V1 branch/runtime gate and current dependency evidence. Do not rewrite historical SR/audit snapshots. |

DOC-06 represents nine files. Together with DOC-01 through DOC-05, the `REPLACE` total is 14 files.

### `ARCHIVE` candidates

| ID | Path | Exact evidence | Recommendation and link/index impact |
|---|---|---|---|
| DOC-07 | `docs/design/remaining-remediation-design-20260716.md` | Status is `accepted` at line 7; lines 19-22 bind it to completed REQ-002/WI-017, and lines 28-55 record implemented/closed findings. `docs/design/index.md:33` still labels it active. | Archive in place with archived date, reason, notice, and current replacement pointers. Do not move it: 47 files reference the filename. |
| DOC-08 | `docs/design/p1-security-acceptance-hardening-design.md` | Status is draft at line 7; line 23 explicitly says it does not claim controls are implemented, and lines 29-43 describe pre-remediation gaps. Later REQ/WI records show the implementation chain. | After WI-038 confirms closure ownership, archive in place as the pre-implementation contract and point to current security/API/operational sources. Do not move it: 32 files reference the filename. |

Existing archived references are correctly structured and should remain `KEEP`: `docs/design/base-agent.md:7-15` and `docs/design/p1-payment-db-integrity-design.md:7-36` contain archive metadata, notice, reason, and replacement pointers.

### `REVIEW` candidates requiring cross-WI decisions

| ID | Path(s) | Evidence | Required decision |
|---|---|---|---|
| DOC-09 | `AGENTS.md:27,40`, `CLAUDE.md:30,43` | Thymeleaf is described as legacy/compatibility while React is active. | Update only after backend/frontend audits prove whether any fallback remains. |
| DOC-10 | `docs/design/api-spec.md:894-944` | Three server play-history APIs and `play_histories` are retained for legacy callers while SPA history is localStorage-based. | Remove or retain together with backend API/table/caller proof; document-only deletion is unsafe. |
| DOC-11 | `docs/design/api-spec.md:1325-1404,1588-1590`; `docs/client/0-site-policy.md:42` | Direct/one-time subscription endpoints remain as explicit failure boundaries for stale callers. | Follow the documented telemetry/caller/removal gate and backend WI result. |
| DOC-12 | `docs/design/db-schema.md:374,382,444,570,702,717,914-931` | Multiple legacy columns/provider/decryption/snapshot compatibility claims remain. | Defer to WI-036/WI-038 DB baseline judgment before changing current schema documentation. |
| DOC-13 | `docs/SR/SR-42.md:3-9`, `docs/SR/SR-93.md:24-34` | Historical SR files contain dated current-safety addenda with branch-specific facts. | Preserve the dated record. If a current operator needs a pointer, append a V1 replacement reference instead of rewriting the historical snapshot. |

### `KEEP` documentation/assets

- Preserve all historical REQ/WI records. Audit-start inventory: 1,027 deliverable files.
- Preserve `docs/SR/` 93 files, `docs/audit/` 7 files, and `docs/retrospective/` 5 files under the historical-record policy.
- Keep `docs/registry/project-registry.md:29-43`; its 149 API, 41 table/entity, 53 screen, and 13 agent figures agree with `docs/index.md:70-73` in this snapshot.
- Keep intentionally draft `docs/payment/client-brief.md:1-19`; its index descriptions also identify it as a draft (`docs/index.md:91`, `docs/payment/index.md:100`).
- Keep tracked `output/pdf/atstudio-client-testing-guide.pdf` and `.manifest.json`, plus `scripts/docs/generate_client_testing_pdf.py` and `verify_client_testing_pdf.py`.

## Repository, Worktree, Branch, and Artifact Inventory

### Worktrees and local branches

| ID | Disposition | Evidence | Action after approval |
|---|---|---|---|
| REP-01 | `KEEP` | Main worktree `C:/Users/jm991/Desktop/project/ATStudio`, branch `codex/p1-acceptance-hardening`, HEAD `a96d2e0`. | Retain as the V1 official branch candidate. |
| REP-02 | `REMOVE` | `git worktree list --porcelain` reports 35 entries whose gitdir path no longer exists; every corresponding `claude/*` branch is fully merged and 82 commits behind the dev branch. | Run prune only after destructive approval, then remove the 35 merged local branches. |
| REP-03 | `REMOVE` after runtime cutover | Active `codex/acceptance-preview` is 7 commits behind and fully merged; active `codex/client-demo-stable` is 2 commits behind and fully merged. Process inspection shows the public Vite/backend runtime currently comes from `ATStudio-client-demo-stable`. | Stop/migrate the owned public runtime first, then remove both auxiliary worktrees and branches. |
| REP-04 | `REMOVE` after approval | Merged local branches without required independent history: `codex/p0-release-blockers`, `codex/payment-integration-clean`, `dev/kyoung`. | Remove locally after final tag/commit reachability check. Remote deletion is out of scope without push approval. |
| REP-05 | `REVIEW` | `codex-payment-integration-design` has 10 branch-only commits; `codex-sr-91-tag-taxonomy-layout` has 3; `master` has 3. | Inspect unique commits and preserve any needed tips by tag/remote before local removal. |
| REP-06 | `KEEP` | Annotated tags resolve to dev `a96d2e0` and client `cd876fc`: `v1-pre-consolidation-dev-20260716`, `v1-pre-consolidation-client-20260716`. | Preserve as pre-consolidation rollback anchors. |

Local branch disposition totals: 44 branches = 1 `KEEP`, 40 approval-gated `REMOVE`, 3 `REVIEW`.

#### Exact 35 prunable worktree/branch registrations

`claude/adoring-agnesi`, `claude/brave-nobel`, `claude/brave-yalow`, `claude/charming-chandrasekhar`, `claude/distracted-bhabha`, `claude/dreamy-perlman`, `claude/elastic-mestorf`, `claude/epic-ptolemy`, `claude/exciting-ellis`, `claude/friendly-rhodes`, `claude/funny-albattani`, `claude/gifted-khorana`, `claude/hardcore-rubin`, `claude/heuristic-ride`, `claude/infallible-northcutt`, `claude/inspiring-leakey`, `claude/interesting-matsumoto`, `claude/jolly-poincare`, `claude/laughing-cartwright`, `claude/loving-villani`, `claude/lucid-swanson`, `claude/musing-goldwasser`, `claude/musing-morse`, `claude/nervous-khorana`, `claude/nice-shamir`, `claude/pensive-ritchie`, `claude/quizzical-snyder`, `claude/recursing-diffie`, `claude/sad-cerf`, `claude/strange-moser`, `claude/stupefied-herschel`, `claude/sweet-ride`, `claude/vigilant-varahamihira`, `claude/wizardly-golick`, `claude/youthful-tesla`.

All point under `C:/Users/jm991/Desktop/project/ATStudio/.claude/worktrees/<name>`, all are marked `prunable gitdir file points to non-existent location`, and all point to merged commit `fec16f1`.

### Generated and tracked artifacts

| ID | Path | State and evidence | Disposition |
|---|---|---|---|
| ART-01 | `output/demo-seed/` | Untracked/unignored; 73 files, 32,025,608 bytes: 36 WAV, 36 PNG, 1 JSON manifest. Generated by tracked seed tooling. | `REMOVE` generated output after approval; keep source tooling. |
| ART-02 | `tmp/` | Untracked/unignored; 35 PDF-render PNGs, 5,241,016 bytes, under `tmp/pdfs/wi012-*`, `wi017-*`, `wi018-*`. | `REMOVE` after approval. |
| ART-03 | `output/client-demo-screenshots-20260716-140514/` | Untracked/unignored; 52 files, 1,177,769 bytes. README says 45/45 captures and identifies the ephemeral Cloudflare URL. | `REMOVE` expanded duplicate after ZIP integrity is retained. |
| ART-04 | `output/client-demo-screenshots-20260716-140514.zip` | 700,703 bytes; 52 entries; 1,177,769 uncompressed bytes; SHA-256 `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8`. README labels it preliminary, not final acceptance proof. | `ARCHIVE` outside active source/SoT; do not treat its URL as current. |
| ART-05 | `.codex-remote-attachments/` | Untracked/unignored; one 68,040-byte attachment copied into the worktree. | `REMOVE` after confirming the original attachment is no longer needed in the worktree. |
| ART-06 | `cloudflared.err.log`, `cloudflared.out.log`, `frontend/vite.err.log`, `frontend/vite.out.log` | Untracked/unignored; four files, 4,249 bytes. Cloudflare and client-demo processes are active. | `REVIEW` now; remove only after owned runtime cutover/stop. |
| ART-07 | `frontend/tsconfig.tsbuildinfo` | Tracked and modified generated file, 5,421 bytes. `frontend/.gitignore:5` already contains `*.tsbuildinfo`, but tracked files bypass ignore. | `REPLACE`: remove from Git tracking under approval; leave generation ignored. |
| ART-08 | `.gitignore`, `frontend/.gitignore` | Root ignore covers `.claude/worktrees/`, boot/server logs, uploads, build output; it does not cover the observed output/demo/tmp/attachment/cloudflared/vite paths. | `REPLACE`: add targeted ignore rules; do not ignore all `output/` because tracked PDF/manifest are intentional. |
| ART-09 | `output/pdf/*`, `scripts/docs/*` | Two tracked client artifacts and two tracked reproducible generator/verifier scripts. | `KEEP`. |

### Acceptance and demo source tools

| Path | Disposition | Rationale |
|---|---|---|
| `scripts/acceptance/` (6 tracked files) | `KEEP` | This is an explicit operator lifecycle tool, not a product fallback. `start.ps1:26-27` requires an external environment bundle for non-dry-run startup; `AcceptanceLifecycle.psm1:9-54` uses explicit environment-name allowlists; application acceptance code is profile/property guarded. |
| `scripts/demo/seed-client-demo.mjs` | `KEEP` | Manifest-scoped QA seed/verify/cleanup source with explicit `[QA Demo]` markers; separate from generated WAV/PNG output. |
| `scripts/demo/seed-client-demo.ps1` | `REPLACE` | Line 6 hard-codes `C:\Users\jm991\AppData\Local\ATStudio\acceptance-preview-64db91c\backend-environment-credentials.json`, which is branch/worktree-specific. Require an explicit path or derive it through the official acceptance runtime contract. |

No source file named as `.bak`, `.backup`, `.old`, `copy`, `backup`, or equivalent was found in the scoped repository file list. Semantic fallback/legacy code remains owned by WI-034, WI-035, and WI-036.

## Index and Link Consequences

- Update `docs/design/index.md` whenever DOC-01, DOC-03, DOC-04, DOC-07, or DOC-08 lifecycle metadata changes.
- Archive DOC-07 and DOC-08 in place rather than moving them. They have 47 and 32 referencing files respectively; path moves would create broad historical-link churn.
- Keep the `payment-integration-design.md` path fixed because 142 files reference it. Front-matter correction has no link impact.
- Preserve historical references from deliverables and SR records even when active indexes change their status labels.
- Do not ignore all `output/`; `output/pdf/atstudio-client-testing-guide.pdf` and its manifest are tracked current artifacts.
- After branch consolidation, update only live operational branch/audit statements. Dated SR/audit/REQ/WI snapshots remain historical.

## Commands & Outputs

- `git status --short --branch` -> dev branch with pre-existing tracked `frontend/tsconfig.tsbuildinfo` modification and untracked runtime/generated/WI inputs.
- `git worktree list --porcelain` -> 38 registrations, 35 prunable, 3 active.
- `git branch -vv`, `git branch --merged`, `git rev-list --left-right --count <branch>...codex/p1-acceptance-hardening` -> branch disposition evidence above.
- `git ls-files`, `git check-ignore -v`, PowerShell recursive file counts/byte sums -> artifact tracking and size evidence.
- .NET `System.IO.Compression.ZipFile::OpenRead` plus `Get-FileHash -Algorithm SHA256` -> screenshot archive entry count, uncompressed size, and hash.
- `rg` currentness/legacy/status searches -> exact documentation evidence above.
- `Get-CimInstance Win32_Process` read-only inspection -> Cloudflare plus frontend/backend processes running from `ATStudio-client-demo-stable`; no process was mutated.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> pass; status comparison before/after was equal.

## Tests

| Check | Result |
|---|---|
| Tier 0 documents | PASS |
| Internal links | PASS, no broken links |
| Traceability IDs | PASS, 426 supported IDs |
| Document index coverage | PASS, no orphan documents |
| Validation mutation check | PASS, Git porcelain status equal before/after |
| Public/client runtime mutation | NONE; inspection only |

## Risks / Rollback

### Risks

- Pruning worktree metadata while the public client runtime is still attached to the client worktree can break operator control and cleanup.
- Removing a compatibility document before WI-034/035/036 caller proof can hide a still-supported boundary.
- Moving completed design documents instead of archiving in place can break 79 reference files.
- Deleting the screenshot directory before verifying the ZIP can lose the only unpacked copy; deleting the ZIP removes the user-requested capture archive.
- Deleting logs while processes are still active can lose incident evidence or interfere with operator expectations.
- Broadly ignoring `output/` would hide the tracked client PDF and manifest.

### Rollback

- This WI made no destructive or product/document changes.
- Remove only:
  - `deliverables/user/WI-20260716-ATS-037-summary.md`
  - `deliverables/agent/WI-20260716-ATS-037-evidence-pack.md`
- No branch, worktree, process, database, index, or existing file rollback is required.

## Follow-ups

- WI-038 must merge this inventory with WI-034, WI-035, and WI-036 and present one exact destructive-action approval table.
- Cleanup implementation must sequence: official runtime validation -> client runtime cutover/stop -> auxiliary worktree removal -> branch/prunable cleanup -> generated artifact cleanup -> targeted ignore update -> documentation lifecycle update -> `validate-docs` rerun.
- Remote branch deletion and any push remain outside scope until explicitly approved.
