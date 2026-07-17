# Evidence Pack: WI-20260717-ATS-005

## Summary (one-liner)

- Aligned active V1 SoT documentation and approved repository tooling/artifacts
  to authoritative WI-002/003/004 evidence without changing product behavior or
  historical records.

## Scope / DoD Check

- [x] Active API, DB, UI, route, payment, client, policy, and registry documents
  match the current source/schema baseline.
- [x] Current docs state TOSS card recurring-only V1 and retain provider-neutral
  recurring interfaces without claiming another active provider.
- [x] Current docs state explicit local-config loading, one six-plan seed owner,
  retired manual SQL, and current emergency administration operations.
- [x] Removed current-state APIs/routes/tables/symbols are absent from active SoT;
  historical records are byte-identical.
- [x] Two completed designs are archived in place with date, reason, replacement,
  and index status.
- [x] Demo seed PowerShell has no hard-coded acceptance credential path and fails
  closed for non-dry-run execution without an explicit path.
- [x] `frontend/tsconfig.tsbuildinfo` is no longer tracked and has a narrow local
  cache ignore; generated logs/artifacts were removed and narrowly ignored.
- [x] Tracked client PDF/manifest and historical screenshot ZIP remain intact.
- [x] Docs, links, index, traceability, script contract/dry-run, diff, ignore,
  count, stale-reference, historical-hash, and secret checks passed.
- [x] No WI-005 product source or test file was changed.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/documentation-standards.md` | Documentation rules |
| 0 | `docs/standards/glossary.md` | Controlled terminology |
| 1 | `docs/policies/versioning-policy.md` | Current/versioned document rules |
| 1 | `docs/policies/archive-policy.md` | In-place archive requirements |
| 1 | `docs/policies/quality-gates.md` | Required validation gates |
| 1 | `docs/policies/security-policy.md` | Secret and local-config constraints |
| 2 | `docs/index.md` | Repository documentation entry point |
| 2 | `docs/design/index.md` | Design status and replacement pointers |
| 2 | `docs/registry/` | Current project/workboard state |
| 2 | `docs/guides/` | Injected pointer; directory absent |
| 2 | `docs/client/` | Current client-facing test state |

**Decision sources:**

- `deliverables/user/REQ-20260716-ATS-004.md`
- `deliverables/agent/WI-20260717-ATS-001-evidence-pack.md`
- `deliverables/agent/WI-20260716-ATS-037-evidence-pack.md`
- `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md`
- `deliverables/agent/WI-20260717-ATS-002-evidence-pack.md`
- `deliverables/agent/WI-20260717-ATS-003-evidence-pack.md`
- `deliverables/agent/WI-20260717-ATS-004-evidence-pack.md`

**Injection Rules Applied:**

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `docops`
- Task type: documentation/tooling/artifact reconciliation
- Authoritative final implementation evidence: WI-002, WI-003, WI-004

## Evidence Pointers

### Active SoT and entry points

- `AGENTS.md`, `CLAUDE.md`, `docs/index.md`
- `docs/client/_internal-feature-map.md`, `docs/client/testing-guide.md`
- `docs/design/api-spec.md`, `docs/design/db-schema.md`,
  `docs/design/payment-integration-design.md`, `docs/design/index.md`
- `docs/design/p0-release-blocker-remediation-design.md`
- `docs/design/p1-payment-integrity-remediation-design.md`
- `docs/design/payment-operations-runbook.md`
- `docs/design/payment-refund-receipt-settlement-policy.md`
- `docs/design/payment-settlement-import-design.md`
- `docs/design/usecase/download-queue.md`, `docs/design/usecase/index.md`,
  `docs/design/usecase/sound-playhistory.md`,
  `docs/design/usecase/user-subscription.md`, `docs/design/usecase/util.md`
- `docs/payment/acceptance-test-checklist.md`,
  `docs/payment/feature-inventory.md`, `docs/payment/index.md`,
  `docs/payment/known-limits-and-next-steps.md`,
  `docs/payment/system-overview.md`
- `docs/policies/security-policy.md`
- `docs/registry/project-registry.md`, `docs/registry/workboard.md`
- `docs/standards/development-standards.md`,
  `docs/standards/frontend-standards.md`, `docs/standards/glossary.md`
- `docs/ui/atstudio-front-list.md`, `docs/ui/screen-flow.md`

### Archived in place

- `docs/design/p1-security-acceptance-hardening-design.md`
  - Archived 2026-07-17; replacement: `docs/policies/security-policy.md`.
- `docs/design/remaining-remediation-design-20260716.md`
  - Archived 2026-07-17; replacement: `docs/design/index.md`.

### Tooling, ignores, and tracking

- `.gitignore`: narrow root log/generated-directory rules.
- `frontend/.gitignore`: `/tsconfig.tsbuildinfo` only.
- `scripts/demo/seed-client-demo.ps1`: empty default credential path, explicit
  non-dry-run requirement, conditional forwarding only.
- `frontend/tsconfig.tsbuildinfo`: staged index removal; unchanged ignored local
  file remains with SHA-256
  `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.
- `frontend/public/.gitkeep`: deleted after confirming it was the directory's only
  entry and no placeholder was needed.

## Traceability Map

| Decision | Edit/deletion evidence | Result |
|---|---|---|
| INT-P10 | `frontend/.gitignore`; index removal of `frontend/tsconfig.tsbuildinfo` | Local narrow cache retained, Git tracking stopped |
| INT-P11 | Active SoT paths listed above | Current code/schema/routes/evidence reflected |
| INT-P12 | `scripts/demo/seed-client-demo.ps1` | Explicit runtime credential path; no embedded credential/path default |
| INT-A01 | Two archived design files; `docs/design/index.md` | Archive date/reason/replacement/status recorded |
| INT-A02 | DB/payment/current-state docs; 1,128-file hash baseline | Manual SQL retired; historical evidence unchanged |
| INT-A03 | Screenshot ZIP retained; expanded directory removed | 52 ZIP entries matched 52 expanded files before deletion |
| INT-R06 | `AGENTS.md`, `CLAUDE.md`, standards/client/UI docs | React SPA is the only active UI; no Thymeleaf runtime compatibility claim |
| INT-R11 | `frontend/public/.gitkeep` deletion | Empty placeholder retired |
| INT-R13 | `.gitignore`; generated-directory deletions | Approved generated copies removed with narrow ignores |
| INT-V11 | Four log deletions; `.gitignore` | Stopped runtime logs removed and narrowly ignored |

## Derived Current Counts

| Surface | Source method | Result |
|---|---|---:|
| Controller files | `controller/**/*.java` | 23 |
| Method API mappings | `@(Get|Post|Put|Delete|Patch)Mapping` | 137 |
| Verb distribution | Same mapping scan | GET 65, POST 36, PUT 21, DELETE 15, PATCH 0 |
| Schema tables | Unique `CREATE TABLE` in `schema.sql` | 39 |
| JPA entities | `@Entity` in entity sources | 39 |
| Subscription seed rows | `subscriptions` values in `seed.sql` | 6 |
| Lazy-loaded screen symbols | `const ... = lazyPage(` in router source | 53 |
| Router entries | `path:` plus `index: true` in router source | 56 + 1 |
| Installed Vite | `npm --prefix frontend ls vite --depth=0 --json` | 6.4.3 |

## Deletion and Preservation Evidence

All deleted generated/runtime paths were confirmed inside the workspace and
untracked before deletion. No matching runtime process or listener remained on
ports 5173 or 8080.

| Removed path | Before-delete evidence |
|---|---|
| `cloudflared.err.log` | 3,953 bytes; SHA-256 `A68249173CE7757C2F35764150D7B65B01830EE1DA1A8417532E57AA0289A7C1` |
| `cloudflared.out.log` | 0 bytes; empty-file SHA-256 |
| `frontend/vite.err.log` | 0 bytes; empty-file SHA-256 |
| `frontend/vite.out.log` | 296 bytes; SHA-256 `E5EAB8DD1F7E6EDBE624E0B30F3B1A7C055A5B8B41DF31D8C75A679C444FE96A` |
| `output/demo-seed/` | 73 files; 32,025,608 bytes; manifest plus 36 WAV and 36 PNG files |
| `tmp/` | 35 files; 5,241,016 bytes |
| `.codex-remote-attachments/` | 1 file; 68,040 bytes; SHA-256 `8F1CDDB133BB60F4985853F64C990E742E2835B91900A6E32610198E9C1ED8B7` |
| Expanded screenshot directory | 52 files; 1,177,769 bytes; all 52 per-file hashes matched ZIP entries |

Preserved assets:

| Preserved path | Ownership/integrity evidence |
|---|---|
| `output/client-demo-screenshots-20260716-140514.zip` | Exists, intentionally not ignored; SHA-256 `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8` |
| `output/pdf/atstudio-client-testing-guide.pdf` | Tracked; SHA-256 `AFBA32CCE2460D5D38B80F4A88278E31D1F7344A2258E240BFD61DF74F4C6095` |
| `output/pdf/atstudio-client-testing-guide.manifest.json` | Tracked; SHA-256 `11A1C91AF1EBF77FBB5CE6B913D3EB197B3AC68D29F2E62B31231C553E0E398D` |

Historical preservation baseline:

- Scope: pre-existing `deliverables/user/**/*.md`,
  `deliverables/agent/**/*.md`, `docs/SR/**/*.md`, `docs/audit/**/*.md`, and
  `docs/retrospective/**/*.md`.
- Baseline files: 1,128.
- Final comparison: 0 changed, 0 missing.

## Commands & Outputs

- `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - Pass: Tier 0, internal links, 433 traceability IDs, and document index.
- Source/schema/router count probes using PowerShell structured regex and
  `npm --prefix frontend ls vite --depth=0 --json`
  - Pass: counts in the table above.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/demo/seed-client-demo.ps1 -DryRun`
  - Exit 0; planned 36 tags, 36 tracks, 9 playlists; no credential/secret terms.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/demo/seed-client-demo.ps1 -Mode Verify`
  - Exit 1 as required without an explicit credential path; message states the
    path is required for non-dry-run operations.
- `git diff --check -- <WI-005 paths>` and
  `git diff --cached --check -- frontend/tsconfig.tsbuildinfo`
  - Pass; LF-to-CRLF advisory warnings only.
- `git check-ignore -v frontend/tsconfig.tsbuildinfo`
  - Pass: `frontend/.gitignore:5:/tsconfig.tsbuildinfo`.
- `git check-ignore -v --no-index -- <generated-path>/.ignore-probe`
  - Pass for demo seed, temp, remote attachment, expanded screenshots, and four
    stopped runtime logs; the retained ZIP is not ignored.
- Active-document stale searches
  - Zero old counts (107/28/51), Vite 6.0.5/6.4.1 public-baseline wording,
    `codex/client-demo-stable`, `TOSS_BILLING`, `KAKAOPAY`, removed route/API
    assertions, and active manual-SQL filename instructions. Five references to
    `codex/p1-acceptance-hardening` are intentional current-candidate statements.
- High-confidence secret scan over added WI-005 lines
  - 0 hits. `application-local.yml` was not read or modified.

## Tests

- Documentation validation: PASS.
- Link/index/traceability validation: PASS.
- Demo script dry-run and fail-closed contract: PASS.
- Working and staged diff checks: PASS.
- Historical hash, ignore, preserved-asset, stale-reference, count, and secret
  checks: PASS.
- Product build/test suites were not rerun by WI-005 because this WI made no
  product behavior changes and treats final WI-002/003/004 evidence as
  authoritative.

## Validation Incidents

- An initial orchestration command had a PowerShell hash-table parser error; no
  repository operation from that command executed. The corrected probe passed.
- The first `git diff --check` found only extra blank lines at EOF in nine new
  active-document rewrites. Those nine EOF issues were fixed; the final working
  and staged checks passed.
- An initial router regex counted only line-leading keys and reported 4/0. The
  corrected token scan reported the actual 56 path entries plus one index
  redirect. This was a harness error, not a repository failure.

## Risks / Rollback

- Risks:
  - The shared worktree contains concurrent product changes from preceding WIs.
    They were preserved and are not claimed as WI-005 changes.
  - Current counts can drift if product changes continue after this snapshot;
    WI-006 should rerun the same source-derived probes.
- Rollback:
  - Restore only the WI-005 active-document, ignore, and PowerShell paths from
    version control; do not revert concurrent product work.
  - Re-add `frontend/tsconfig.tsbuildinfo` only if the tracking policy itself is
    intentionally reversed; the unchanged ignored local cache already remains.
  - Regenerate demo/temp artifacts as needed. Recover expanded screenshots from
    the preserved ZIP. Runtime logs are recreated by their owning processes.

## Follow-ups

- Next WI: `WI-20260717-ATS-006` (unblocked by this completed WI).
