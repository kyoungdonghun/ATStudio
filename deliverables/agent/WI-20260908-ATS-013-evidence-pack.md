---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260908-ATS-013-handoff.md
    reason: Bounded delegated scope and exclusive output ownership
  - path: ../user/REQ-20260908-ATS-003.md
    reason: Approved non-destructive assessment
---

# Evidence Pack: WI-20260908-ATS-013

## Summary

Completed the independent repository deployment/current-document assessment; no deployment CI was found, current baseline wording needs correction, and historical evidence must remain intact.

## Scope / DoD Check

- [x] Distinguished repository deployment setup, acceptance tooling, and unavailable external-host evidence.
- [x] Identified bounded current-document correction candidates with exact locations.
- [x] Classified historical references and sampled their local versus Git-tracked availability.
- [x] Wrote only this Evidence Pack and the corresponding user summary; no current source/document edits.
- Branch/default/tag decisions remain MA-owned. No Git history comparison, network, runtime operation, external configuration access, test mail, product audit, or nested agent was performed.
- Completion is DocOps scope only, not closure of REQ003, the combined WI013, SR-93, or production GO. Handoff `Blocks: -`; no downstream delegation is required.

## Reference Documents (Tier 0-2)

| Tier | Loaded context | Purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md` (STD-001); `documentation-standards.md` (STD-004); `development-standards.md` (STD-002); `glossary.md` (STD-005), all under `docs/standards/` | User-injected excerpts plus repository reading; approval, metadata, evidence and terminology rules |
| 1 | `docs/policies/security-policy.md`; `docs/policies/versioning-policy.md`; `.claude/agents/docops.md` | Secret exclusion, historical preservation, two-set delivery |
| 2 | WI013 handoff; REQ003; `docs/index.md`; `docs/SR/SR-93.md`; `docs/payment/index.md`; payment/client current guides | Delegated scope and current versus dated documentation |
| 2 | `docs/design/payment-operations-runbook.md`; `docs/design/runtime-storage-operations.md`; `scripts/acceptance/README.md`; `start.ps1`; `AcceptanceLifecycle.psm1` | Procedure and startup coupling |
| Skills | `.agents/skills/create-wi-evidence-pack/SKILL.md`; `.agents/skills/validate-docs/SKILL.md` | Output structure and one final document gate |

Injection: user-supplied Tier 0 excerpts and MA-generated handoff; assignee `docops`, task `documentation`, tiers 0-2. MA's routing/configuration checks were accepted as supplied, not independently repeated.

## Evidence Pointers

### Deployment Inventory And Actual Coupling

| Finding | Evidence | Interpretation |
|---|---|---|
| No repository-owned deployment CI/configuration found in the bounded tracked-file inventory | `git ls-files`: 3,203 paths; zero conventional workflow/deployment-manifest matches; `.github` directory absent in this checkout | No configured production deployment pipeline was found here. This is stronger than merely lacking host visibility, but does not exclude an external pipeline or unconventional out-of-scope setup. |
| Application configuration and operator tooling do exist | Tracked `src/main/resources/application.yml`, `application-acceptance.yml`, `application-local.example.yml`, `frontend/.env.example`; `scripts/acceptance/README.md:5-10` | Filename inventory only for application configuration; no secret values inspected. These assets are not a production host definition. |
| Launcher follows its containing checkout, not a named branch | `scripts/acceptance/start.ps1:15-21,30-36`; `scripts/acceptance/AcceptanceLifecycle.psm1:1248-1265` | Repo root is derived from script location; backend runs `gradlew.bat bootRun` there and frontend runs Vite dev from its `frontend` directory. No named-branch selection was found in these launch files. |
| Acceptance is explicitly not production | `scripts/acceptance/README.md:9-10,234-236`; `AcceptanceLifecycle.psm1:1003-1015` | Quick tunnel plus development processes are acceptance tooling, not automatic production release or a branch-pinned deployment. |
| Production procedures exist, execution remains target-dependent | `docs/design/payment-operations-runbook.md:490-521`; `docs/design/runtime-storage-operations.md:21-29,64-76`; `docs/SR/SR-93.md:65-71` | Host/domain, artifact/ref, proxy, secrets, DB/media tuple, restore, monitoring, scheduler owner and release approval need named-target evidence. No operational verification was attempted. |

Search covered conventional GitHub/GitLab/Azure/Jenkins CI, Docker/Compose, Procfile, hosting manifests, Terraform, Kubernetes/Helm, proxy and service-unit locations/names. Broad tracked deployment/script candidates were also inspected. A documentation policy stub at `docs/policies/future-policy-stubs.md:45-50` describes deferred pipeline integration; it is not operational proof.

### Current Sources Of Truth

| Purpose | Current pointer | Boundary |
|---|---|---|
| Production closure requirements | `docs/SR/SR-93.md:57-71` | OPEN; no new production GO or mandatory repeat of accepted TEST flows |
| Central source/runtime record | `docs/payment/index.md:58-73` | Dated supplied observations, not current process verification; correction candidate below |
| Payment and storage operating contracts | `docs/design/payment-operations-runbook.md:490-522`; `docs/design/runtime-storage-operations.md:21-39,64-76` | Keep procedures active; their existence does not establish configured infrastructure |
| Acceptance launch contract | `scripts/acceptance/README.md:5-10,234-236`; `docs/payment/acceptance-test-checklist.md:138-139` | Operator must identify the approved checkout/revision/origin for each run |

### Proposed Current-Document Corrections Only

All candidates below are P2 documentation follow-ups, not authorization to change a branch or deploy. The active `codex/v1-release-rehearsal-fixes` / `5328482` context and default `main` / `736fdc4` discovery were supplied by MA/user, not independently verified here. MA owns the final baseline selection and history analysis.

| Candidate | Exact current location | Proposed correction / classification |
|---|---|---|
| C1 | `docs/index.md:73` | Replace the unqualified official `codex/p1-acceptance-hardening`/no-client claim with the approved current-baseline pointer; date dependency observations. Current navigation text, not an archive candidate. |
| C2 | `docs/client/testing-guide.md:28-32`; `docs/client/_internal-feature-map.md:60` | Apply the same central baseline pointer; separate a selected acceptance checkout from existence/maintenance of other worktrees. Preserve per-run URL verification. Active client guidance. |
| C3 | `docs/payment/feature-inventory.md:184` | Qualify the old branch/Vite assertion as a dated predecessor or point to the central snapshot. Do not globally replace historical branch names. Active inventory text. |
| C4 | `docs/payment/index.md:60,67`; `docs/SR/SR-93.md:92` | Label the existing `7eae086` receipt as its dated stage and add the later MA-confirmed documentation baseline (`5328482`, supplied here). Remove ambiguity from 'not yet claimed committed/pushed' only for the exact receipt MA confirms. Do not infer a new running artifact or production deployment. |
| C5 | `docs/client/_internal-feature-map.md:62-64` | Qualify 'focused test evidence only' and 'full suites ... remain later gates' as the historical WI-014~021 boundary, then link later dated quality records such as `docs/SR/SR-93.md:118-130`. Keep external/production gates separate. |

`docs/payment/index.md:115-117` already identifies the old p1 baseline as historical, directly supporting C1-C3 without duplicating MA's Git analysis. Any later update must separately reconcile dated mail/runtime receipts with their owning evidence; this assessment neither rechecks delivery nor changes acceptance outcomes.

### Historical Preservation And Fresh-Clone Availability

- Keep `docs/payment/index.md:75-87` as the explicitly historical WI005 table, including cached ref numbers and client-worktree observations; do not reinterpret them as current remote facts.
- Keep `docs/SR/SR-93.md:33-55,94-130` as dated quality/schema/runtime evidence, and `docs/design/remaining-remediation-design-20260716.md:35` as a historical implementation-branch reference. No file needs deletion merely because it names p1.
- If a whole document later leaves active use, apply archive notice/date/reason/replacement/validation-scope requirements from `docs/policies/versioning-policy.md:79-83`; no archive move or metadata change was performed.
- Direct-reference sample only: WI005/WI009/WI010 (2026-09-08), WI021 (2026-07-24), WI067 (2026-08-09) Evidence Packs, and `docs/audit/p1-payment-integrity-closure-20260715.md` all exist locally AND are Git-tracked in the inspected checkout. Their referring locations are `docs/payment/index.md:62,73,87`, `docs/SR/SR-93.md:24,43`, and `docs/payment/acceptance-test-checklist.md:271`.
- Thus none of these six sampled links relies solely on an untracked local file. This is not a fresh-clone test or proof of availability in every ref. MA reports default `main` has only a two-line README; that tree must not be assumed to carry these docs. No historical-record-wide scan was performed.

## Commands & Outputs

- Read handoff, approved REQ, skill/policy pointers and bounded current guides with `Get-Content -LiteralPath`; located claims with `rg -n`.
- Read-only `git ls-files` inventory/filter: 3,203 tracked paths, zero conventional deployment configuration matches; six exact reference-path checks plus `Test-Path` all tracked/present.
- Initial `rg` against absent `README.md`, `.github`, `docs/guides` and a literal PowerShell wildcard returned path errors; subsequent exact-path/file-inventory reads resolved the intended scope. No missing path was treated as evidence about the remote default branch.

## Tests

- No product tests or runtime checks: documentation-only assessment.
- Final document gate: `python .agents/skills/validate-docs/scripts/validate_docs.py` ran ONCE after both assessment bodies were written: exit 0, all Tier 0 files present, no broken links, 681 traceability IDs, index PASS. Only this result receipt and the summary receipt were updated afterward; no repeat validation. Output lengths: 103/120 and 37/40 lines.

## Risks / Rollback

- Risk: stale current wording can send operators to a predecessor checkout; a local link PASS does not prove a fresh clone of a different ref contains the same evidence.
- Only additions: `deliverables/agent/WI-20260908-ATS-013-evidence-pack.md` and `deliverables/user/WI-20260908-ATS-013-summary.md`. No current source/docs, data, process, branch or tag was changed by DocOps.
- Rollback, if explicitly authorized: remove only these two assessment artifacts. No rollback/deletion was executed.
- MA can integrate C1-C5 and the deployment gaps into its separate decision register. Current-document corrections and target provisioning remain follow-up approval/execution work; DocOps does not wait for them.
