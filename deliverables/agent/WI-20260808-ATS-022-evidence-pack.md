# Evidence Pack: WI-20260808-ATS-022

## Summary (one-liner)

- Reconciled SR-94 through SR-101 documentation with the approved design, current implementation, API/schema sources, and WI-014 through WI-021 evidence; corrected the WI-022 summary's traceability mapping and recorded two remaining implementation mismatches without changing code or data.

## Status

**Documentation correction complete; WI chain remains blocked by implementation mismatches.**

The 36-system-document update and the two WI outputs are complete. Full three-way consistency remains open for two implementation findings: `DisposableMysqlBootstrap` still validates the prior 39-table manifest while the canonical schema contains 41 tables, and `SecurityConfig` still contains matchers for retired direct administrator subscription-mutation routes. The first finding blocks disposable MySQL validation; the second is dead policy configuration that does not expose a controller route but still requires removal and regression coverage.

**Post-WI-20260809-ATS-001 verified update:** both findings are resolved. The refreshed validator passed isolated `[redacted probe]` mismatch cleanup and `[redacted proof]` create/standalone-validate/two-drop checks for `41/493/168/89`, six plans, and SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`; the retired matchers were removed with focused regression coverage. These two implementation blockers no longer hold the WI chain.

## Scope / DoD Check

- [x] Planned SR-94 through SR-101 design was compared with current code/API/schema and WI-014 through WI-021 evidence using the approved SR-to-WI mapping.
- [x] All explicit SR/WI references in the 36 changed system documents and two WI-022 outputs were searched; the incorrect feature mapping was confined to the user summary and was corrected.
- [x] Historical SR observations and decisions were preserved; current status and evidence were added without mass translation.
- [x] API specification, database schema, use cases, UI flows, operational boundaries, glossary, registries, and indexes were updated where current code required it.
- [x] Dry-run and real-backfill boundaries are explicit; no claim says existing persistent rows were changed.
- [x] Existing non-square thumbnails are distinguished from new or replacement 1:1 uploads.
- [x] Local subscription correction is distinguished from provider charging/refunding and documents approval, audit, idempotency, and concurrency behavior.
- [x] PlayableTrack batch hydration, ID persistence, ordering, bounded active-only lookup, and stale-result fences are documented.
- [x] Repeated four-category tag parameters, AND semantics, Usage terminology, Home fallback, and available-tag failure fallback are documented.
- [x] `dataList`/`pageInfo` wrapper contracts are current where applicable.
- [x] API/controller counts and documentation indexes were recomputed rather than copied from stale documentation.
- [x] `validate-docs`, index synchronization check, scoped output Prettier, and `git diff --check` pass.
- [x] No application suite, build, coverage, browser session, schema/data mutation, secret access, provider call, or external-system call occurred.
- [ ] Full implementation/documentation consistency is not achieved because the bootstrap validator expects the previous schema manifest and security configuration retains two retired-route matchers.

## Reference Documents

| Tier     | Document                                                                                                             | Use                                                                        |
| -------- | -------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| 0        | `docs/standards/core-principles.md`                                                                                  | Approval, evidence, traceability, and scope-preservation rules             |
| 0        | `docs/standards/documentation-standards.md`                                                                          | English system-document and Evidence Pack requirements                     |
| 0        | `docs/standards/development-standards.md`                                                                            | Current controller, service, API, and persistence boundaries               |
| 0        | `docs/standards/glossary.md`                                                                                         | Canonical domain terminology                                               |
| REQ      | `deliverables/user/REQ-20260808-ATS-004.md`                                                                          | Approved acceptance-hardening plan and WI dependencies                     |
| WI       | `deliverables/agent/WI-20260808-ATS-022-handoff.md`                                                                  | Mandatory scope, DoD, prohibitions, and outputs                            |
| SR       | `docs/SR/SR-94.md` through `docs/SR/SR-101.md`                                                                       | Planned behavior, historical evidence, and remaining acceptance boundaries |
| Evidence | `deliverables/user/WI-20260808-ATS-014-summary.md` through `WI-20260808-ATS-021-summary.md` and matching agent packs | Implementation results and focused verification                            |

## Approved Traceability Matrix

| SR     | Approved feature                                                                | Implementation evidence |
| ------ | ------------------------------------------------------------------------------- | ----------------------- |
| SR-94  | Tag duplicate precheck, race translation, and modal/list state preservation     | WI-017                  |
| SR-95  | Tag canonicalization, validation, and duplicate contract                        | WI-017                  |
| SR-96  | Administrator role protection, audit, and current-role synchronization          | WI-014                  |
| SR-97  | Local administrator subscription correction                                     | WI-015                  |
| SR-98  | Exact-square Track thumbnail validation and preview                             | WI-020                  |
| SR-99  | Decoded-PCM audio analysis and read-only dry-run                                | WI-016                  |
| SR-100 | Usage-first unified tag discovery and four-category filtering                   | WI-021                  |
| SR-101 | Delayed buffering state and shared PlayableTrack hydration/persistence contract | WI-018 and WI-019       |

## Reconciliation Method

1. Read the approved REQ, handoff, Tier 0 standards, each SR, and the matching WI-014 through WI-021 summaries and Evidence Packs.
2. Traced every SR behavior to current controller, DTO, service, repository, security, React API/state/UI, schema, and focused-test files.
3. Counted method mappings directly from controller annotations: 69 GET, 41 POST, 20 PUT, 0 PATCH, and 14 DELETE mappings across 25 controllers, for 144 total.
4. Counted 41 canonical `CREATE TABLE` statements in `src/main/resources/schema.sql` and 41 `@Entity` classes. The aggregate 493 columns, 168 physical indexes, and 89 foreign keys were derived from the previously validated 39-table baseline (449/153/80) plus exact additive DDL: `admin_operation_audit_logs` adds 12 columns, 4 indexes, and no foreign keys; `admin_subscription_corrections` adds 32 columns, 11 physical indexes including FK-created indexes, and 9 foreign keys.
5. Applied the project `sync-docs-index` rules: Design is recursive; the other categories count top-level Markdown; every `index.md` is excluded. The result is 201 managed Markdown files.
6. Searched current non-historical docs for stale API/schema counts and superseded SR-94 through SR-101 phrases.
7. Searched all explicit SR-94 through SR-101 and WI-014 through WI-022 references in the 36 changed system documents and two WI outputs. The SR documents pointed to the approved implementation WIs; the incorrect feature-to-SR mapping was confined to the user summary and was corrected.

No MySQL instance was started. Therefore a replacement live `information_schema` manifest hash was not fabricated or claimed.

The requested `docs/guides/operations/` path does not exist in the current repository. The active operational equivalents were updated instead: `docs/design/payment-operations-runbook.md`, `scripts/database/README.md`, `docs/client/3-admin-checklist.md`, and `docs/client/testing-guide.md`.

## Evidence Pointers

Current API and schema documentation:

- `docs/design/api-spec.md` - version 28, 25-controller/144-mapping inventory, public PlayableTrack batch contract, audio-analysis dry-run, subscription-correction workflow, repeated tag parameters, wrappers, and retired direct admin mutation routes.
- `docs/design/db-schema.md` - version 22, 41-table/41-entity baseline, additive aggregate derivation, two admin tables, media metadata, and no-backfill boundary.
- `docs/design/index.md` - current API and DB specification versions/counts.
- `scripts/database/README.md` - verified current manifest (`41/493/168/89`, six plans, and SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`) plus the guarded fail-closed and isolated Create/Validate/Drop proof recorded by WI-20260809-ATS-001.

SR and operational status:

- `docs/SR/SR-94.md` through `docs/SR/SR-101.md` - implementation status, exact code/test/WI evidence, retained historical sections, and remaining manual/production gates.
- `docs/SR/index.md` - all eight SRs remain OPEN until later acceptance boundaries complete.
- `docs/design/payment-operations-runbook.md` - local correction workflow and provider-operation separation.
- `docs/client/3-admin-checklist.md` and `docs/client/testing-guide.md` - manual operational/browser checks remain unchecked.

UI and domain contracts:

- `docs/ui/screen-flow.md`, `docs/ui/atstudio-front-list.md`, and `docs/ui/modal-list.md` - current player, history, search, role, correction, thumbnail, and modal inventory behavior.
- `docs/design/usecase/sound-tag.md`, `sound-track.md`, `sound-playhistory.md`, `sound-playlist.md`, `download-queue.md`, and `user-subscription.md` - current repeated-parameter, hydration, persistence, and correction contracts.
- `docs/standards/glossary.md` - Local Subscription Correction, PlayableTrack, and Audio Analysis Dry Run terms.

Indexes and inventory:

- `docs/index.md` - 201 managed-document counts under the project synchronization rules.
- `docs/registry/project-registry.md` - 144 endpoints, 25 controllers, and 41 entities/tables with dirty-worktree provenance.
- `docs/payment/feature-inventory.md` and `docs/client/_internal-feature-map.md` - current administrative and public feature boundaries.

The exact inventory of 36 changed system documents and two WI outputs is recorded in `deliverables/user/WI-20260808-ATS-022-summary.md`.

## Implementation Findings

### Historical finding: stale disposable MySQL manifest

- At WI-022 completion, `scripts/database/DisposableMysqlBootstrap.java:47-52` hardcoded the prior manifest hash and `39/449/153/80` table/column/index/FK counts.
- `src/main/resources/schema.sql:1031` and `src/main/resources/schema.sql:1054` define the two additive tables that produce the current `41/493/168/89` baseline.
- The then-current comparison used those stale constants, so `create`/`validate` correctly failed closed until the approved repair.
- WI-022 did not edit the Java validator or run MySQL.
- **Resolved by WI-20260809-ATS-001:** the validator constants now match the observed `41/493/168/89` and six-plan manifest with SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`; the expected mismatch cleanup and independent proof passed with target names redacted.

### Historical finding: retired-route matchers

- At WI-022 completion, `SecurityConfig` still listed ADMIN matchers for direct `PUT`/`DELETE /api/user-subscriptions/*` paths.
- Current `UserSubscriptionController` no longer exposes those methods; admin correction uses the dedicated correction controller.
- The matchers did not create routes, but they left controller, API documentation, and security configuration out of alignment.
- **Resolved by WI-20260809-ATS-001:** the matchers are absent and the existing direct-mapping regression passed.

## Traceability Audit

```powershell
$files = @(git diff --name-only -- docs scripts/database/README.md) + @(
  'deliverables/user/WI-20260808-ATS-022-summary.md',
  'deliverables/agent/WI-20260808-ATS-022-evidence-pack.md'
)
rg -n 'SR-(94|95|96|97|98|99|100|101)|WI-20260808-ATS-0(14|15|16|17|18|19|20|21|22)' -- $files
```

- PASS after correction: all 38 files were covered. The eight SR documents reference the approved WI evidence, no other changed system document contains a contrary SR/WI pairing, and the user summary now matches the approved traceability matrix above.

## Commands & Outputs

### API inventory

```powershell
$m = @(rg -o --no-filename '@(Get|Post|Put|Patch|Delete)Mapping' src/main/java/com/atstudio/atstudio/controller)
$m | Group-Object | Sort-Object Name | Select-Object Name, Count
"TOTAL=$($m.Count)"
$controllers = @(rg --files src/main/java/com/atstudio/atstudio/controller | Where-Object { $_ -like '*Controller.java' })
"CONTROLLERS=$($controllers.Count)"
```

- PASS: GET 69, POST 41, PUT 20, DELETE 14, total 144; controllers 25.

### Schema/entity source counts

```powershell
rg -n --glob '*.sql' '^CREATE TABLE' src/main/resources/schema.sql
rg -l '@Entity' src/main/java/com/atstudio/atstudio/entity --glob '*.java'
```

- PASS: 41 `CREATE TABLE` statements and 41 JPA entity files.
- Aggregate column/index/FK method is recorded under Reconciliation Method; no live database count or manifest hash was claimed.

### Documentation validation

```powershell
$env:PYTHONIOENCODING='utf-8'
python .agents/skills/validate-docs/scripts/validate_docs.py
```

- PASS: all Tier 0 references present, no broken internal links, 510 trace IDs scanned, and all documentation indexed.

The same script initially encountered a Windows CP949 output encoding error while printing a Unicode failure marker. Re-running with `PYTHONIOENCODING=utf-8` completed successfully; this was a console-output issue, not a document failure.

### Metadata check

```powershell
$files = @(git diff --name-only --diff-filter=ACM -- '*.md') + @('deliverables/user/WI-20260808-ATS-022-summary.md', 'deliverables/agent/WI-20260808-ATS-022-evidence-pack.md')
foreach ($file in $files | Select-Object -Unique) {
    $text = Get-Content -Raw -Encoding utf8 $file
    if ($text.StartsWith('---') -and ($text -notmatch '(?m)^version:\s*\S+' -or $text -notmatch '(?m)^last_updated:\s*2026-08-09\s*$')) {
        throw "FRONTMATTER_MISMATCH: $file"
    }
}
```

- PASS: all changed documents that use frontmatter contain a version and `last_updated: 2026-08-09`. The WI output format does not use frontmatter, consistent with adjacent WI outputs.

### Index synchronization check

```powershell
$rules = [ordered]@{
  Architecture = @('architecture', $false); Design = @('design', $true)
  Policies = @('policies', $false); Standards = @('standards', $false)
  Templates = @('templates', $false); Registry = @('registry', $false)
  Audit = @('audit', $false); Client = @('client', $false)
  Payment = @('payment', $false); SR = @('SR', $false)
  Retrospective = @('retrospective', $false); ADR = @('adr', $false)
  UI = @('ui', $false); Eval = @('eval', $false)
}
$index = Get-Content -Raw -Encoding utf8 docs/index.md
$total = 0
foreach ($name in $rules.Keys) {
  $directory = Join-Path docs $rules[$name][0]
  $files = if ($rules[$name][1]) {
    Get-ChildItem -LiteralPath $directory -Recurse -File -Filter *.md
  } else {
    Get-ChildItem -LiteralPath $directory -File -Filter *.md
  }
  $actual = @($files | Where-Object Name -ne 'index.md').Count
  $declared = [regex]::Match(
    $index,
    '(?m)^\|\s*' + [regex]::Escape($name) + '\s*\|\s*(\d+)\s*\|'
  ).Groups[1].Value
  if ($actual -ne [int]$declared) { throw "INDEX_COUNT_MISMATCH: $name" }
  $total += $actual
}
if ($total -ne 201) { throw "INDEX_TOTAL_MISMATCH: $total" }
```

- PASS after correction: all 14 category rows match `docs/index.md`; total 201. Design was counted recursively, every other category at top level, and every `index.md` was excluded.

### Markdown/tool lint

```powershell
$env:PYTHONIOENCODING='utf-8'
python .agents/skills/lint/scripts/lint_all.py
```

- TOOLING BLOCKED: `markdownlint`, `jq`, and `ruff` are not installed or on PATH. The script did not produce content diagnostics. No package or dependency was installed under this WI.

### Prettier

```powershell
$changed = @(git diff --name-only --diff-filter=ACM -- '*.md')
& .\frontend\node_modules\.bin\prettier.cmd --check @changed
```

- BASELINE STYLE FINDING: 30 changed existing documents report Prettier differences. They include historical SR and mixed-format project documents. A mass rewrite was intentionally not performed because it would obscure historical evidence and exceed the narrow documentation correction scope.

```powershell
& .\frontend\node_modules\.bin\prettier.cmd --check deliverables/user/WI-20260808-ATS-022-summary.md deliverables/agent/WI-20260808-ATS-022-evidence-pack.md
```

- Post-correction checks reported formatting differences only in the WI output being edited. Prettier `--write` was applied only to the two WI-022 outputs; the final check passed for both. No existing system document was mass-formatted.

### Stale-text and whitespace checks

```powershell
$stale = @(rg -n -i '107 APIs?|current.{0,40}39 tables|39 tables.{0,40}current|CSV-encoded tags?|comma-separated tags?' docs --glob '*.md' --glob '!docs/audit/**' --glob '!docs/retrospective/**')
if ($stale.Count) { $stale; throw 'CURRENT_STALE_ASSERTION_FOUND' }
'STALE_SCAN=PASS'
git diff --check
```

- PASS: the scoped current-state search returned no stale hits. Historical 39-table evidence and explicit statements that Usage is not License were retained and therefore intentionally excluded from this stale-assertion pattern.
- PASS: no whitespace errors. Git reports only existing working-copy CRLF-to-LF warnings; no line-ending rewrite command was run.

Application tests, builds, coverage, browser acceptance, MySQL validation, persistent-data work, and provider/external actions were not run by design.

## Scope Preservation

- Branch: `codex/v1-release-rehearsal-fixes`; observed HEAD: `c7f779df35e2175405d837230edf61962e2bae42`.
- Existing WI-014 through WI-021 implementation, tests, and deliverables were preserved in the shared dirty worktree.
- `output/client-demo-screenshots-20260716-140514.zip` remains untouched and untracked at 700,703 bytes; final SHA-256 observation: `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8`.
- No file was deleted, staged, or committed.
- WI-022's exact write scope is 36 system documents and its two outputs. No Java, TypeScript, CSS, SQL, schema, seed, configuration, secret, dependency, provider, storage, or external-system mutation was made.

## Risks / Rollback

Risks:

- WI-20260809-ATS-001 resolved the two WI-022 implementation findings: the verified fresh manifest is `41/493/168/89`, six plans, SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`; the expected mismatch cleanup and independent Create/Validate/Drop proof passed with target names redacted, and the retired matchers were removed with the focused test passing.
- Broad Markdownlint could not run because its required executable is absent. Broad Prettier exposes baseline formatting differences that were intentionally not rewritten.
- Full application suites, builds, coverage, and browser acceptance remain later-WI responsibilities.
- Retained-data, deployment, and later-WI gates remain unchanged.

Rollback:

1. Revert only the documentation portions listed in the WI-022 user summary and remove the two WI-022 outputs.
2. Preserve all pre-existing WI-014 through WI-021 dirty changes and the intentional ZIP.
3. No application, schema, persistent-data, provider, dependency, or external-system rollback is required.

## Follow-ups / WI Chain

- WI-20260809-ATS-001 completed the approved repair and focused evidence.
- WI-022 is documentation complete and its three-way consistency is restored.
- WI-023 through WI-027 are unblocked; each successor retains its own approved scope and gates.
