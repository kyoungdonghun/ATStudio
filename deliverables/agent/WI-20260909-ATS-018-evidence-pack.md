---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: docops
category: reference
status: active
dependencies:
  - path: WI-20260909-ATS-018-handoff.md
    reason: Approved ownership and delivery contract
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved development recovery requirement
  - path: ../../docs/registry/development-history-recovery-20260909.md
    reason: Complete semantic review and original/public identity mapping
---

# Evidence Pack: WI-20260909-ATS-018

## Summary (one-liner)

Content-reviewed all 96 original documents, restored 83 privacy-reviewed
historical derivatives at their original paths, and reconciled all 205 original
identities without changing the private originals or republishing raw 109.

## Scope / DoD Check

- [x] Read the approved REQ, handoff, Tier 0 and task-specific pointers before edits.
- [x] Review all 96 document bodies, including 83 previously local-only documents.
- [x] Preserve all 96 useful historical records: 13 byte-identical retained files,
  73 derivatives without identifier substitutions (71 notice-only, two with
  E1 EOF normalization) and 10 narrowly sanitized derivatives.
- [x] Preserve dates, language, status, failed/partial results and contradictory
  claims; distinguish actual historical evidence from missing or supplied proof.
- [x] Reconcile all 205 original path/hash identities and old/new dispositions;
  keep 109 raw outputs/media/SQL private and absent at their original source paths.
- [x] Map reusable rules/decisions to current entry points without rewriting policy
  or repeating a product audit.
- [x] Correct the retention register's stale WI016 handoff-ready statement using
  its subsequent MA publication/closeout receipt.
- [x] Finish scoped public-reference, privacy and ownership checks.
- [x] Leave runtime, DB, product, Git publication, off-device backup and integrated
  verification to MA; WI020 remains the independent review successor.

## Reference Documents (Tier 0-2)

Injected source pointers were explicitly read, not assumed preloaded.

| Tier | Document | Reason |
|---|---|---|
| 0 | [Core Principles](../../docs/standards/core-principles.md) | Constitution, approval and evidence boundaries |
| 0 | [Documentation Standards](../../docs/standards/documentation-standards.md) | Historical preservation, metadata and references |
| 0 | [Development Standards](../../docs/standards/development-standards.md) | Engineering and scope constraints |
| 0 | [Glossary](../../docs/standards/glossary.md) | Shared terminology |
| 1 | [Archive Policy](../../docs/policies/archive-policy.md) | Original preservation and local evidence availability |
| 1 | [Security Policy](../../docs/policies/security-policy.md) | No secrets, account identifiers or private homes in public derivatives |
| 2 | [Documentation Index](../../docs/index.md) | Current entry points; owned by another agent |
| 2 | [Original retention register](../../docs/registry/v1-artifact-retention-20260909.md) | All 205 original identities and previous classification |
| 2 | [Runtime Storage Operations](../../docs/design/runtime-storage-operations.md) | DB/storage tuple and non-mutating recovery boundary |
| Task | [Approved REQ004](../user/REQ-20260909-ATS-004.md) and [WI018 handoff](WI-20260909-ATS-018-handoff.md) | Scope, ownership and Blocks WI020 |
| Prior receipts | [WI016](WI-20260909-ATS-016-evidence-pack.md), [WI017](WI-20260909-ATS-017-evidence-pack.md) | Dated MA cleanup publication, not new WI018 delivery |
| Role / skill | [DocOps role](../../.claude/agents/docops.md), [Evidence Pack skill](../../.agents/skills/create-wi-evidence-pack/SKILL.md), [Validate Docs skill](../../.agents/skills/validate-docs/SKILL.md) | Role boundary, generated deliverable structure and bounded integrity checks |

Injection rules: handoff Tier 0-2 pointers plus explicit user governing rules;
assignee `docops`; task type documentation/history/privacy. The required handoff
existed before processing. The project tag `ATS` was confirmed from
`.claude/config/workspace.json`. The evidence skill's precondition and output
sections were applied directly to this file; this is not a substitute handoff.

## Evidence Pointers

- [Development History Recovery Register](../../docs/registry/development-history-recovery-20260909.md):
  all 96 semantic rationales, original/public SHA-256 pairs, P0-P3 methods,
  current reading map and preserved contradictions.
- [V1 Artifact Retention Register](../../docs/registry/v1-artifact-retention-20260909.md):
  stable A001-A205 anchors; previous `13 RETAIN_GIT / 192 ARCHIVE_LOCAL`;
  current `13 RETAIN_GIT / 83 RESTORED_DERIVATIVE / 109 ARCHIVE_LOCAL`.
- [Registry Index](../../docs/registry/index.md): discoverability of both records.
- Every P1/P2/P3 row identifies one of the **83 added files** under the original
  `deliverables/agent/` or `deliverables/user/` path; each has a provenance
  notice that links back to its row. P0 rows identify the 13 untouched originals.
- [WI018 summary](../user/WI-20260909-ATS-018-summary.md): concise delivery boundary.
- No edit to archive-policy, README, scripts/reconstruction, docs/index, product,
  DB/config/runtime/media or any archive original was made by WI018.

## Commands & Outputs

The commands used read-only PowerShell/Node standard APIs and the existing
`js-yaml` parser. All manual file edits used `apply_patch`; no recovery SQL,
runtime helper, destructive operation, install, product test or Git write ran.

| Check / command family | Output and limit |
|---|---|
| `Get-Content -Raw` with `ConvertFrom-Json` / `JSON.parse` | Inspected actual manifest object and its `artifacts[].path/bytes/sha256` schema; 205 originals, 96 Markdown documents |
| Bounded body reads from private `restore-check/` | All 96 reviewed; exact repeated blocks matched to already-read body content, not skipped by headings or links |
| Node `fs.readFileSync`, `crypto.createHash('sha256')` | 96 public-file hashes measured; all 13 retained hashes equal original manifest values |
| Read-only original/public text comparison | All 83 equal original normalized text plus the explicit notice and allowed substitutions; 33 original frontmatter objects unchanged; 25 API route occurrences preserved |
| Narrow transformation tally | P0 13; P1 73 (two with E1 EOF normalization); P2 8 with 10 home-prefix replacements; P3 2 with 2 contributor-label replacements |
| Original raw-path existence check | 109 raw original paths absent; no raw content republished |
| Existing `js-yaml.load` on original-document frontmatter | 111 dependency references resolve; no parse failures; no source dependency repair required |
| Scoped final reference/privacy/ownership pass | PASS: 88 owned paths, 38 YAML objects, 101 dependency references, 436 Markdown references and 184 fragment references; zero errors or privacy-screen findings |

The 96 public hashes are necessary derivative provenance, not a repeated
205-member original hash walk. Original ZIP and manifest identities were taken
from MA's receipt, then checked against the structured baseline fields:

- ZIP: `486744B423DDDDD4DAFDB96224F5612EBCB858DF2FB128417AC4CFD5C52E4163`.
- Original manifest: `C3D1F9D90DC36D440AC0659E4ED29C26592457457D5EC478782CD9E452F7EE28`.
- Receipt: `%LOCALAPPDATA%/ATStudio/archives/development-recovery-20260909-094234/baseline.json`.
- MA supplied 205 restored-member matches, unchanged 955 protected and 79 local
  files, three unchanged process start identities and four HTTP 200s.
  WI018 did **not** execute those runtime/preservation checks.

Minimal independent reproduction, from the repository root, for all 96 public
hashes (no private archive, DB or runtime required):

```powershell
$rows = Get-Content docs/registry/development-history-recovery-20260909.md |
    Select-String '^\| <a id="a\d{3}"></a>\[A\d{3}\]'
if ($rows.Count -ne 96) { throw 'Expected 96 public document records' }
foreach ($row in $rows) {
    $cells = $row.Line.Split('|')
    $match = [regex]::Match($cells[2], '\]\(../../([^)]*)\)')
    if (-not $match.Success) { throw 'Missing public document path' }
    $expected = $cells[4].Trim().Trim([char]96)
    $actual = (Get-FileHash -LiteralPath $match.Groups[1].Value -Algorithm SHA256).Hash
    if ($actual -ne $expected) { throw "Public hash mismatch: $($match.Groups[1].Value)" }
}
'PASS: 96 public document hashes'
```

For original transformation checks, obtain authorized access to the saved
manifest and `restore-check/`, compare the 13 P0 originals byte-for-byte, and
compare each P1-P3 original after only the documented notice insertion, newline
normalization and P2/P3 substitution. For A056/A095 only, also remove one
redundant terminal LF (E1), keeping the final content-line terminator.
Do not restore raw files into this checkout.

## Tests

Only bounded documentation/provenance checks are in WI018 scope.
The validate-docs skill guided the link/frontmatter/index-discoverability checks;
the full repository/source-clone validator and integrated product tests are
MA-owned, not claimed as WI018 passes.

| Final scoped gate | Result |
|---|---|
| Manifest-to-ledger identity and disposition comparison | PASS: all 205 paths, original hashes and stable IDs match; previous 13/192 and current 13/83/109 counts exact |
| Public provenance mapping | PASS: all 96 original/public mapping rows match; all 96 public SHA-256 values match actual bytes |
| Allowed text transformations | PASS: 83 exact normalized-text comparisons; 33 original frontmatter objects unchanged; all 25 API route occurrences unchanged |
| Public references | PASS: 436 Markdown destinations, 184 fragment references, 101 YAML dependency references in 88 owned files; zero errors |
| YAML parsing | PASS: 38 owned-file frontmatter objects parse; the wider 96-original-document dependency pass also passed all 111 references |
| Bounded privacy screen | PASS: no private home prefixes, known private contributor/operator identifiers, email addresses, provider keys, JWTs or private-key markers in the 88 owned public files; full semantic review remains the primary privacy check |
| Source availability | PASS: 83 derivative files present; 13 retained original hashes unchanged; all 109 raw original paths absent |
| Owned Git visibility | PASS: 88 paths visible, 86 added/untracked and 2 modified; none ignored; no staging or publication performed |
| Tracked edit whitespace | `git diff --check -- docs/registry/v1-artifact-retention-20260909.md docs/registry/index.md`: exit 0; only a CRLF-to-LF advisory for the existing index |
| Reproduction snippet shape | PASS: 96 rows with valid path and SHA-256 fields; public hash verification was executed by the equivalent Node standard-library check |

The ownership check observed other agents' concurrent paths and left them alone.
Existing historical hard breaks and body whitespace remain preserved in the
exact-text checks. The later, explicitly documented E1 correction below removes
only a redundant terminal empty line in two public derivatives, never originals.

MA additionally supplied an independent 37-check source preflight and 32 passing
synthetic tests while WI018 was finishing. Those are not WI018-executed results
and do not imply actual machine, DB or media recovery. Final QA and Git delivery
receipts must supersede candidate wording only after actual execution evidence
is supplied; they must not be backdated into this DocOps check.

## Integration EOF Correction

On 2026-09-09 MA reported that the actual 102-path staged candidate failed
`git diff --cached --check` at A056 line 110 and A095 line 39: a new blank line
at EOF inherited from each historical source. WI018 reproduced both messages
before this correction. The earlier whitespace check covered only two already
tracked registry edits and did not establish a passing staged-candidate gate.
MA reports that this blocked attempt created **no new archive export**.

The authorized correction removed exactly one final LF byte from each public
derivative, retaining one newline after the last content line. No other byte,
historical fact, frontmatter field, body hard break or archive original changed.
P1 with the E1 modifier describes these two files; the 96/109 availability split
and all original identities/dispositions remain unchanged.

| Entry / public file | Previous bytes | Corrected bytes | Previous public SHA-256 | Corrected public SHA-256 |
|---|---:|---:|---|---|
| A056 / `deliverables/agent/WI-20260817-ATS-029-evidence-pack.md` | 11194 | 11193 | `D9FB1B79669008AF1B138F02A2A2141CF503AE7825B48F7363CB2840463CDEF8` | `72B2C7A3D3CE657EF67B133A9C8CC612FD6364D10AA02CFD2C407771174E6C60` |
| A095 / `deliverables/user/WI-20260817-ATS-029-summary.md` | 4210 | 4209 | `4DBCD5C2424DF2688DE4D27FA784398042385B429B55519B19CC1FFC4C264E51` | `92658EB0DD0C17497F11EFEA965ADE3C2F663354D733646B3AC8CA90A2959906` |

Read-only proof against the pre-correction staged blobs: both new byte sequences
equal the corresponding old blob with only the last of two terminal LF bytes
removed; both end in exactly one LF. The two archived originals were independently
rehash-checked against their manifest entries and still match. Their unchanged
original hashes remain in the recovery register; only the two public hashes
were replaced. All other public document identities are unaffected.

Correction checks completed:

- Original-to-public transformation: PASS for A056/A095, exactly the unchanged
  original normalized text plus the existing provenance notice and E1 removal.
- Staged-blob-to-working-file comparison: PASS, one LF byte removed per file,
  all remaining bytes identical; one final LF retained.
- Public mapping: PASS, both corrected hashes match actual files and both
  original hashes remain unchanged; the other 94 public mapping values are unchanged.
- Scoped metadata: PASS, six correction paths, four YAML objects, 14 dependency
  references and 337 Markdown destinations; zero privacy-screen findings/errors.
- `git diff HEAD --check --` restricted to the six correction paths: exit 0,
  no output. This checks current working content, not an independently rerun
  cached integration gate.

No index write, commit, archive export, runtime or DB action was performed by WI018. MA owns
restaging these edits and rerunning the complete cached check and export gate;
working-tree checks alone must not be reported as a successful restaged gate.

## Risks / Rollback

Historical assertions can conflict or refer to missing private evidence. The
recovery register retains and explains those conflicts instead of upgrading
them to current proof or quietly rewriting the originals. Hash identity proves
which bytes were delivered, not the truth of every historical assertion.

Public history is not a backup of configuration, credentials, DB, media or raw
evidence. The private off-device backup destination remains unanswered; no upload,
DB dump, actual reconstruction or production readiness is established.
A later source clone depends on MA publishing the final candidate.

Rollback remains MA-owned and requires authorization for deletion. Reverse only
this WI's 83 derivative additions, its new registry/evidence/summary and its two
specific registry edits. Do not revert the 13 retained documents, other agents'
changes or the original archive. After publication, use a scoped Git reversal;
do not reset the shared worktree. No rollback was executed.

## Follow-ups

WI018 **Blocks WI-20260909-ATS-020**. Return the candidate and this Evidence Pack
to MA for independent review after WI019 is ready. MA owns final source-only
validation, protected/live-state preservation and verified Git/clone delivery.
Do not close REQ004 or report off-device recovery complete at this DocOps handoff.
