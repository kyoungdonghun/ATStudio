---
version: 1.0
last_updated: 2026-11-06
project: ATS
owner: PG
category: evidence-pack
status: content-review-complete
dependencies:
  - path: WI-20261106-ATS-005-handoff.md
    reason: Approved privacy review scope
  - path: ../user/REQ-20261106-ATS-003.md
    reason: Authorized selected commit and public push
---
# Evidence Pack: WI-20261106-ATS-005

## Summary

Reviewed 161 selected working files and all 774 path/blob pairs added or changed
through seven outgoing commits for a public destination. Issued exact hash-bound
content allowances. Final new document and staged-blob checks remain separate
gates owned jointly with EO/MA; PG performs no Git writes.

## Scope / DoD Check

- [x] Triaged all 482 visible changed/untracked paths in the initial snapshot.
- [x] Reconfirmed all 63 prior exclusions at their original SHA-256 values.
- [x] Reviewed selected source, examples, operational guides and fixture findings.
- [x] Checked every intermediate outgoing commit, including blobs later replaced.
- [x] Reused prior content reviews only after exact content/normalization comparison.
- [x] Preserved historical evidence, private inputs, binaries and unrelated files.
- [ ] New reporting delta and final staged/committed object verification must pass
  before MA pushes; the initial content allowance alone does not satisfy this gate.

## Reference Documents (Tier 0-2)

Injected Tier 0: core-principles, development-standards,
documentation-standards and glossary. Applied rules: approved scope once,
evidence-led decisions, language tracks, WI traceability and preserved history.
Tier 1: AGENTS.md, security-policy, execution-policy and quality-gates.
Tier 2: approved REQ, WI005 handoff, current index, existing bounded scanner,
Oct21 WI002 content review, Oct26 WI004 allowances/deltas and WI007 integration,
WI006 selected candidate manifest. Role PG; context rule source is
`.claude/config/context-injection-rules.json`.

The handoff existed and was read before applying the create-wi-evidence-pack
skill. This pack follows that skill's output contract.

## Evidence Pointers

Local sanitized receipts are under `build/WI-20261106-ATS-005/`. They are intentionally
ignored and are not promised as clone-available files:

| Receipt | Result |
| --- | --- |
| `triage-01.json` | 482 paths: 415 pattern-clear candidates, 52 with findings, 15 binary/unknown |
| `supplemental-context-01.json` | 388 masked credential/URI/target/long-literal contexts for 161 selected paths |
| `selected-public-allowlist-01.json` | 161 per-path raw SHA-256 and expected filtered Git blob IDs; 321 snapshot exclusions |
| `outgoing-history-triage-01.json` | Seven commits, 774 distinct path/blob pairs; 25 flagged text blobs, five binaries |
| `outgoing-history-prior-comparison-02.json` | 760 previous exact attestations, 14 separately reviewed source/report/self-manifest blobs |
| `outgoing-history-extra-01.json` | Supplemental URI and target-binding locations, without values |
| `outgoing-public-allowlist-01.json` | Exact seven-commit public-content allowance with per-finding dispositions |

Working-file allowance SHA-256:
`c436f73bf198c8ddc36e71932e525c11370d5d7c62c9136a13ca4e0341f79303`.
Outgoing-history allowance SHA-256:
`498e4d7c2f8608746cf9cae9926cbf65640b201aa5d2606741fc73b731fa8769`.

The outgoing range is strictly
`58b1011dad796017e47dbcc82a4225ca449bcdf6..f0c73d449e4f2773ad91cb2fb34093ddd57cd741`.
It contains seven commits and 774 path/blob pairs, including intermediate versions,
not merely the final tree diff. Destination visibility was supplied by MA's fresh
authorized remote inspection; PG made no network call.

Durable prior evidence: [Oct21 PG review](WI-20261021-ATS-002-evidence-pack.md),
[Oct26 PG review](WI-20261026-ATS-004-evidence-pack.md),
[baseline allowance](WI-20261026-ATS-004-allowlist.json),
[final integration delta](WI-20261026-ATS-004-final-integration-delta.json),
[source normalization](WI-20261026-ATS-006-source-allowlist.json), and
[integration record](WI-20261026-ATS-007-evidence-pack.md).

## Findings and Decisions

The selected 161 files contain three primary scanner matches: a generic login
placeholder email, a deliberately stale authentication fixture and a rejected
example-domain URI containing synthetic userinfo. These were context-cleared.
Supplemental review covered configuration inputs, credential references, long
literals, service identities, URI destinations and test construction. Actual
credential bodies and unique private target endpoints were not found in this
selected content. Generic deployment paths and source/artifact hashes are not
credentials. Disabled examples require external private inputs.

The historical 25 flagged text blobs contain public noreply defaults, an SSH
algorithm identifier, synthetic rejection fixtures, version numbers, marker-only
key validation or route prose. The five document binaries match the prior PG
text/metadata/structure review exactly; no new rendering or extraction was needed
to establish unchanged content. The prior review reported unfilled client material,
no flagged text/metadata and no embedded media/macros or active PDF features.

Public-destination supplemental review covered 232 URI contexts and 34
target-binding contexts across the outgoing history. Unclassified URI candidates
resolved to public documentation/OAuth endpoints, test domains or dated development
tunnel references. Two such tunnel URLs in `docs/payment/index.md` are already
identical in the public remote base. They are not newly disclosed by this range;
their present reachability or validity was neither checked nor asserted.

All 63 earlier exclusions remain byte-identical. This includes historical personal
paths, unresolved operational context and unreviewed generated media. The current
selection excludes `deploy/single-vps/runbooks/host-hardening.md`, which retains
personal home-path references. Exclusion means preserve locally, never delete or
rewrite. The other unselected snapshot paths are not declared unsafe by absence
from the allowlist; their publication was not needed for the selected checkpoint.

## Commands & Outputs

- `python scripts/reconstruction/scan_commit_candidates_wi004.py --output build/WI-20261106-ATS-005/triage-01.json`
- Read-only `git diff-tree --raw --no-abbrev -r -z`, `git cat-file`, SHA-256
  comparisons and `git hash-object --stdin-paths` for exact paths.
- Supplemental local Python classification emitted locations, categories and
  masked contexts only. No private file body was used as an input.
- The reusable scanner retains its historical WI004 label; current receipt folder,
  this evidence pack and the explicit scope identify WI005. Pattern-clear alone
  was never treated as approval.
- A local encoding-name typo and console encoding limitation interrupted display
  helpers. Corrected helper invocations completed; neither failure changed source,
  Git, historical evidence or review outcomes.

## Tests and Validation Limits

This is a content/privacy review, not application testing. No application build,
test suite, operational module execution, server/DB/provider action or installation
was performed. EO owns functional evidence reconciliation. Reusing old exact
content review does not reuse old publication authority: current REQ supplies the
authorized push scope and this WI reviews the public destination explicitly.

The allowance is bounded, not a universal absence-of-secrets or complete security
assurance. Newly authored reports require delta review. At staging, raw hashes must
still match and each staged blob must equal its expected filtered Git object ID.
The final commit tree/path set must contain only reviewed objects before push.

## Risks / Rollback / Follow-up

No implementation or historical file was edited by PG. Only this pack, the paired
summary and ignored sanitized receipts were created. Preserve exclusions and
original failures. Do not force-add private material or rewrite existing history.
WI006 is the immediate dependent work item and receives both allowance manifests;
MA alone performs authorized stage/commit/push after the final object check.
