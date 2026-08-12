# WI-20260809-ATS-020 Evidence Pack

## Status

- Result: PASS for the executable acceptance-test design.
- Product code changed: No.
- Existing current-state documentation changed: No.
- Runtime or DB changed: No.
- Baseline: `e343c20` on `codex/v1-release-rehearsal-fixes`.

One delegated `qa-fe` sidecar remained in `running` state through two bounded
waits and was stopped. It wrote no files and returned no analysis. The MA
recovered the function-consumer mapping from reproducible TypeScript AST and
Spring annotation scans; no worker-authored result was integrated.

## Deliverables

- `deliverables/agent/WI-20260809-ATS-020-handoff.md`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`
- `deliverables/agent/WI-20260809-ATS-020-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-020-summary.md`

## Coverage Reconciliation

| Unit                              | Expected |             Verified in matrix |
| --------------------------------- | -------: | -----------------------------: |
| Distinct visual page UIs          |       53 |                             53 |
| Checkout callback variant rows    |        2 |                              2 |
| Path-bearing route declarations   |       56 |  56, no missing effective path |
| Index redirects                   |        1 |                              1 |
| Modal owner files                 |       17 |                             17 |
| Modal render occurrences          |       22 |                             22 |
| Shared shell/player/dialog groups |        7 |                              7 |
| Cross-entry invariant families    |        8 |                              8 |
| High-risk state-machine families  |        8 |                              8 |
| Scheduled methods                 |        6 | 6 across four operation groups |

The 53 visual rows plus two callback variants cover 55 path UIs. The `/admin`
parent path and its child index redirect complete the 56 path-bearing and 57
routable-declaration contracts.

## Source Evidence

| Evidence                                     | Pointer                                                          |
| -------------------------------------------- | ---------------------------------------------------------------- |
| Routes, guard helpers, callback reuse        | `frontend/src/router/index.tsx`                                  |
| Route/page count contract                    | `docs/ui/atstudio-front-list.md`                                 |
| Modal count and interaction contract         | `docs/ui/modal-list.md`                                          |
| Screen/state flow contracts                  | `docs/ui/screen-flow.md`                                         |
| Frontend validation/routing/responsive rules | `docs/standards/frontend-standards.md`                           |
| API mappings and V1 boundaries               | `docs/design/api-spec.md`                                        |
| Table inventory and state ownership          | `docs/design/db-schema.md`                                       |
| Page-level API imports                       | Non-test `frontend/src/pages/**/*.tsx` TypeScript AST            |
| Shared API consumers                         | Non-test components, layouts, and Stores under `frontend/src`    |
| Backend mappings                             | Controller mapping annotations under the Java controller package |
| Scheduled operations                         | `@Scheduled` methods under backend source                        |

## Mechanical Results

1. All 56 effective route paths and the one index redirect are represented.
2. All 17 Modal owner files and 22 occurrences are represented.
3. The matrix contains 55 unique page/callback row IDs, of which 53 are
   distinct visual UIs and two are callback variants of one UI.
4. The current frontend has 131 distinct direct Axios method/path contracts;
   all normalize to a current backend mapping.
5. Thirteen backend mappings have no direct shared-client Axios match. Three
   are active infrastructure paths: bare-Axios auth refresh, direct audio stream,
   and SPA forwarding. Ten are API-only/operator/support candidates assigned to
   downstream API and policy classification.
6. `fetchAdminSubscriptionCorrections` is an exported frontend wrapper without
   a non-test UI importer and is explicitly deferred for classification.

## Reproduction Commands

```powershell
# Route declarations and matrix coverage: use the TypeScript AST walk recorded
# during WI-020 to recursively join parent/child paths and compare each path
# against the matrix. Expected: paths=56, indexes=1, missing=[].

$matrix = Get-Content `
  deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md `
  -Encoding UTF8 -Raw
$files = rg -l '<Modal\b' frontend/src -g '*.tsx' -g '!*.test.tsx'
$occurrences = 0
foreach ($file in $files) {
  $occurrences += (rg -o '<Modal\b' $file | Measure-Object).Count
}
$files.Count
$occurrences

rg -n '@(Get|Post|Put|Patch|Delete)Mapping' `
  src/main/java/com/atstudio/atstudio/controller
rg -n '@Scheduled' src/main/java/com/atstudio/atstudio
```

The frontend/backend reconciliation used the installed TypeScript parser to
collect `client.<verb>(path)` calls, normalized template parameters to `{}`,
and compared them with class-plus-method Spring mapping annotations. It is a
declaration check; it does not replace runtime network evidence.

## Validation

The following validations passed on 2026-08-09:

- Route AST reconciliation: 56 paths, one index, no missing matrix path.
- Matrix row reconciliation: 55 page/callback rows, 53 distinct visual UIs.
- Modal reconciliation: 17 owner files, 22 occurrences, no missing owner.
- Targeted Prettier check: PASS for all nine current REQ/WI documents.
- Repository documentation validation: PASS with no broken links, missing Tier
  0 documents, unsupported traceability IDs, or unindexed documents.
- Targeted trailing-whitespace scan: zero findings.

Reproduction commands:

```powershell
Set-Location frontend
npm.cmd exec -- prettier --check `
  ../deliverables/agent/WI-20260809-ATS-020-handoff.md `
  ../deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md `
  ../deliverables/agent/WI-20260809-ATS-020-evidence-pack.md `
  ../deliverables/user/WI-20260809-ATS-020-summary.md

Set-Location ..
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
git status --short
```

## Limits and Open Classifications

- This WI designs tests; it does not claim any browser row passes.
- Generic accessibility/responsive/error heuristics do not override product
  policy. A conflict becomes a policy decision rather than an automatic fix.
- API/import presence does not prove a rendered control is reachable. Browser
  WIs must capture DOM and network evidence.
- The ten API-only/support candidates are not labeled dead code. They require
  authorization/shape/use-case checks and later keep/remove/UI decisions.
- Existing branch-promotion wording and the workspace tech-stack declaration
  are not changed during the frozen audit. Any current-state drift is recorded
  during finding synthesis.
- Real Toss money movement, real refund/cancel, uncontrolled external mail,
  schema/data destruction, secret changes, deployment, and destructive Git
  remain explicit user gates.

## Rollback

Delete the four WI-020 deliverables to remove this documentation-only test
design. Product, runtime, DB, and existing documentation require no rollback.

## Next WI

WI-20260809-ATS-021 starts the frozen-code, read-only browser sweep for the
public shell, Notice pages, error pages, public guards, deep links, console, and
basic viewport behavior. Findings are recorded without product-code edits until
the initial audit sequence is complete.
