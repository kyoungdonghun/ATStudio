---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: QA
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260909-ATS-020-handoff.md
    reason: Approved independent review scope and ownership
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Reconstruction scope and separate private recovery gates
  - path: WI-20260909-ATS-018-evidence-pack.md
    reason: Historical recovery and subsequent E1 correction
  - path: WI-20260909-ATS-019-evidence-pack.md
    reason: Reconstruction implementation and focused verification
---

# Evidence Pack: WI-20260909-ATS-020

> Current state: see the [Subsequent MA Integration Receipt](#subsequent-ma-integration-receipt) and [REQ004](../user/REQ-20260909-ATS-004.md#ma-delivery-receipt). Earlier implementation/review checkpoints and their pending labels below are preserved as dated history, not current publication status.

## Summary (one-liner)

**PASS for the reviewed source candidate:** independently checked preservation,
privacy, historical boundaries, reconstruction logic and focused tests; reviewed
MA's actual source-export/build receipt. Remote publication/clone and private
recovery are separate, not completed by this verdict.

## Scope / DoD Check

- [x] Read the approved REQ004, existing skill-generated WI020 handoff, Tier 0
  and task-specific rules, and both implementers' handoffs/evidence.
- [x] Independently reconcile all 205 original identities and 96 public hashes.
- [x] Verify 13 unchanged originals and all 83 allowed text transformations,
  including the two explicitly authorized E1 EOF corrections.
- [x] Screen the historical/public candidate without printing sensitive matches;
  inspect substantive decision and conflicting-evidence samples.
- [x] Compare reconstruction instructions with actual configuration loading,
  billing keyring, database/storage, scheduler and startup source.
- [x] Independently execute the focused synthetic tests and source preflight.
- [x] Validate documentation, category counts, scoped references, staged privacy,
  allowed paths and staged whitespace without Git writes.
- [x] Read and distinguish MA's actual source-only export/build receipt.
- [ ] Actual candidate publication and post-update origin-clone verification:
  MA-owned after this review; no completed candidate receipt at this checkpoint.
- [ ] Off-device private backup, new-OS installation, real DB/media restoration
  and production acceptance: not performed; not implied by source PASS.

Only this WI's Evidence Pack and user summary were edited. Product, scripts,
other documents, original archives, private settings, DB, media and existing
runtime were not modified. The existing synthetic suite creates and removes
only its GUID-named private OS-temp fixtures; this is not a runtime/data edit.

## Reference Documents (Tier 0-2)

The handoff's required-read pointers were loaded explicitly.

| Tier | Document | Review purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved scope, privacy and truthful evidence |
| 0 | `docs/standards/development-standards.md` | Independent verification and bounded changes |
| 0 | `docs/standards/documentation-standards.md` | English technical documents and two-set outputs |
| 0 | `docs/standards/glossary.md` | Historical versus current authority |
| 1 | `docs/policies/security-policy.md` | Secret-safe output and explicit private configuration |
| 1 | `docs/policies/archive-policy.md` | Preserve originals and dated claims |
| 2 | `docs/design/runtime-storage-operations.md` | Paired DB/public/private roots and audit limits |
| 2 | `docs/SR/SR-93.md`, Remaining Production Gates | Historical missing references and HOLD boundary |
| 2 | `docs/registry/v1-artifact-retention-20260909.md` | Original 205-member ledger |
| 2 | `docs/registry/development-history-recovery-20260909.md` | Public mapping, notices and semantic reading notes |
| 2 | `scripts/reconstruction/README.md`, root `README.md` | Current recovery route and command boundaries |
| Supporting | `scripts/database/README.md`, `scripts/acceptance/README.md` | Guarded fresh-only and acceptance contracts |
| Task | REQ004; WI018/WI019/WI020 handoffs; WI018/WI019 evidence | Authorization, ownership and supplied evidence |

Injection rule source: `.claude/config/context-injection-rules.json`; assignee
`qa`, required tiers `[0]`, with explicit Tier 1/2 handoff pointers for this
review. `ATS` was confirmed in `.claude/config/workspace.json`. The QA role and
`create-wi-evidence-pack`, `test`, `validate-docs`, and `sync-docs-index` skills
were read. The Evidence Pack skill's existing-handoff gate and required output
sections are applied here. Product-wide build/test defaults were narrowed by
the explicit WI020 instruction; no heavy product suite was rerun.

## Evidence Pointers

- [Recovery register](../../docs/registry/development-history-recovery-20260909.md):
  A001-A096, original/public hashes, P0-P3 and E1 transformations.
- [Original ledger](../../docs/registry/v1-artifact-retention-20260909.md#original-path-register):
  stable A001-A205, prior and current dispositions.
- [Preflight](../../scripts/reconstruction/Test-DevelopmentRecovery.ps1):
  fixed allowlist, bounded public JSON parsing, reparse refusal, fixed diagnostics.
- [Focused suite](../../scripts/reconstruction/test-development-recovery.ps1):
  locked synthetic private sentinels, negative paths and bounded CLI processes.
- [Guide](../../scripts/reconstruction/README.md): sections 4-7, explicit local
  import, full historical keyring, retained-DB quarantine and completion limits.
- [.gitattributes](../../.gitattributes): existing LF document contract, CRLF
  batch exceptions and binary exclusions; unchanged by this work.
- [WI018 E1 correction](WI-20260909-ATS-018-evidence-pack.md#integration-eof-correction):
  original staged failure and authorized two-byte aggregate correction.
- Private read-only originals:
  `%LOCALAPPDATA%/ATStudio/archives/v1-artifacts-20260909-084405/`.
- MA receipt directory:
  `%LOCALAPPDATA%/ATStudio/archives/development-recovery-20260909-094234/`.
  `baseline.json`, `zip-recheck.json`, and `candidate-validation.json` are private
  evidence pointers, not files promised by a Git checkout.

## Commands & Outputs

All results in this table were independently executed by WI020.

| Command or bounded check | Actual result |
|---|---|
| `pwsh -NoProfile -File ./scripts/reconstruction/test-development-recovery.ps1` | Exit 0; 33 total, 33 passed, 0 failed |
| `pwsh -NoProfile -File ./scripts/reconstruction/Test-DevelopmentRecovery.ps1 -AsJson` | Exit 0; SOURCE_ONLY, 38 checks, 0 failures |
| Same preflight with `-CheckTools -AsJson` | Exit 0; 46 checks, 0 failures, 1 WARN: mysql CLI missing; versions NOT_CHECKED |
| PowerShell `Parser.ParseFile` for both scripts | 0 syntax errors each |
| `python -B .agents/skills/validate-docs/scripts/validate_docs.py` | Exit 0; Tier 0, links, 711 supported traceability IDs and index coverage PASS |
| `sync-docs-index` read-only procedure against actual table categories | 205 recursive non-index Markdown files; 0 category mismatches |
| Structured original manifest/disposition versus public ledger | 205 identities/order/hashes and previous dispositions match; current 13 retained / 83 derivatives / 109 private |
| Open original ZIP read-only; stream SHA-256 and length comparison for every manifest member | 205 ZIP members, 205 matches, 0 missing/mismatch; total 3,082,533 bytes |
| Original-document restore-check SHA-256 | 96/96 originals match manifest |
| Public worktree SHA-256 after E1 correction | 96/96 match register; 13/13 retained hashes equal original hashes |
| `git show :<public-path>` bytes through Node `execFileSync`/SHA-256, against staged register | 96/96 staged public hashes match; no newline conversion in capture |
| Original-to-public normalized text comparison | 83/83 exact allowed transformations; 33 original frontmatter blocks unchanged; 83 correct provenance notices |
| Raw original source-path existence check | All 109 private raw paths absent from source |
| Existing `js-yaml` plus scoped Markdown/ledger-anchor resolver | 106 files; 50 frontmatter objects, 129 dependencies, 544 Markdown targets, 179 ledger anchors; 0 errors |
| Bounded credential/private-identifier screening | 96 historical files plus 10 entrypoint files: 0 matches; actual staged 102-file screening: 0 matches |
| `git diff --cached --name-only` against explicit WI018/WI019/REQ/WI handoff allowlist | 102 expected, 102 staged; 0 extra/missing paths, 0 product/runtime/raw assets |
| `git diff --cached --check` after parent restaging | Exit 0, no diagnostics; independently repeated |
| `git check-ignore --no-index` virtual-path probes | Root private-uploads matched; reconstruction script and nested frontend path did not; no probe files created |
| `git config --get core.autocrlf`; `git check-attr --cached eol -- <all 96 paths>` | Effective autocrlf true; all 96 public documents eol=lf; attributes already tracked in HEAD and unchanged |

Original identities independently confirmed:

- ZIP SHA-256: `486744B423DDDDD4DAFDB96224F5612EBCB858DF2FB128417AC4CFD5C52E4163`.
- Manifest SHA-256: `C3D1F9D90DC36D440AC0659E4ED29C26592457457D5EC478782CD9E452F7EE28`.

### Reproduction Notes

Use the public-hash reproduction in WI018 against an LF checkout respecting
the existing `.gitattributes`, or compare raw Git blobs/export bytes. Do not
hash PowerShell-formatted `git show` strings: encoding/newline reconstruction
can change bytes. WI020 used Node `execFileSync` buffers for staged hashes.

For authorized original comparisons, parse `manifest.json.artifacts` and
`disposition.json.retainedPaths`; compare every ordered path/hash with the
ledger and stream each corresponding ZIP entry without extracting or executing
anything. Hashes prove identity, not historical claim truth.

For all 83 derivatives, normalize CRLF to LF, remove exactly the inserted
provenance paragraph and its separator (50 no-frontmatter files also have one
leading notice-separator LF), and apply only the specified P2/P3 substitutions
to original text. Exactly 10 home-prefix and 2 contributor-label substitutions
were observed. For A056/A095 only, remove one redundant terminal LF from the
original comparison text. All 83 then compare exactly; no general trim or
substantive rewrite is needed. The 33 original frontmatter blocks are equal.

### MA Source-Export Receipt, Not QA-Executed Builds

WI020 read the actual structured `candidate-validation.json`, SHA-256
`1CF045BE16B9757E8E917AE7A3E9885F753A923FD40C1ED42A762813990429AE`.
It names staged tree `08d8276e3b4ff1fd434d6c3afb4d6cc7c3ccb8c5`, before the
WI020 additions and final WI018 evidence/guide clarifications. MA, not WI020, executed these candidate-copy
operations:

| Receipt field | Recorded result |
|---|---|
| Source / focused suite / docs | 38 PASS; 33/33; PASS with 711 supported IDs |
| npm dependency install | 322 packages, `ignore-scripts`; existing deprecated dependency warnings |
| Frontend build | PASS |
| Backend `compileJava compileTestJava bootJar` | PASS, 37 seconds; no tests; offline Gradle uses local caches |
| Private config/media inputs | 0 present |
| Exported public history | 96 hashes matched, 0 mismatches |
| Built JAR prohibited entries | 0; archive SHA-256 `12FB2D67E5D71C266CC2419D865136249E1130482E36CEAC6EB157B3E54D8997` |
| New OS / DB restore / external backup | All false |

MA supplied tool versions: Java/javac 17.0.12, Node 24.14.0, npm 11.9.0 and
Python 3.14.3. These are supplied measurements, not WI020 version-command runs.
The reviewed source/lockfile corroborates the guide's tool requirements.
The build receipt is neither a full test-suite pass nor an install-script,
fresh-cache, new-machine or runtime acceptance result.

## Tests And Findings

**Open actionable findings: none.**

- **Resolved parent finding, EOF:** A056 and A095 originally had one redundant
  terminal LF each. WI018 applied the approved E1 correction and regenerated
  the two public hashes. WI020 verified exact transformations, all 96 worktree
  and staged hashes, and the passing cached whitespace gate. Not still open.
- **Withdrawn newline concern:** QA initially proposed a portability issue from
  an in-memory LF-to-CRLF simulation. Inspection of existing tracked
  `.gitattributes` and all 96 effective staged attributes disproved the premise:
  documents explicitly use LF despite autocrlf=true. The simulation did not
  reproduce a real clone. No configuration/attribute change is required. The
  implementer's final guide now points to this existing contract; QA reviewed
  that explanation. Updated-candidate clone verification remains MA-owned.

The full-body transformation comparison covers all 83 derivatives; it does
not claim QA independently repeated DocOps' full semantic reading of all 96
originals. QA inspected actual decision/contradiction samples including A001,
A006/A007, A044, A054, A061 and A072, alongside the reading map. Consent
approval attribution, seed producer versus independent FAIL, interrupted versus
later supplied observations, and private contributor substitution remain
inspectable. Dated test/runtime/source claims are not promoted into current
acceptance; original approvals are not fresh execution authority.

Privacy screening covered home prefixes, email/phone forms, credential
literals, provider/GitHub tokens, JWTs and private-key markers, returning only
file/line/category if matched. It is bounded detection plus selected semantic
inspection, not a guarantee of general DLP or review of raw 109/private bundles.
No private runtime configuration or credential source was loaded.

Source audit confirms explicit local-file selection, environment precedence
warning, complete ID-addressed `BillingKeyCrypto` v2 keyring, paired roots,
fresh-only DB tooling, and acceptance bootstrap isolation. `AtStudioApplication`
enables scheduling; `SubscriptionScheduler`, `PaymentReconciliationService`,
`WithdrawalBillingCleanupCoordinator` and `StorageMutationRecoveryService`
contain active cron/startup/after-commit paths. Storage recovery claims and
cleans journal work; audit/DDL flags cannot disable those business mutations.
The guide correctly keeps restored data disconnected and does not invent an
all-actions-off switch. No application/worker startup or DB call was used to
verify these static contracts.

### Final Documentation Clarifications

After the build receipt, QA reviewed the actual working diff of the guide's
existing-attributes paragraph and section 6 step 5, plus WI018's final E1
verification addendum. These are documentation-only changes outside the
recorded build tree, not grounds to rerun the product suite.

The guide now explicitly separates **PREEXISTING_MISSING** (the documented
backup baseline exception) from **RESTORE_LOSS** (a newly missing restored
object). Matching counts are insufficient: compare the paired recovery state,
member hashes and missing-reference identities. Equal baseline state with no
new loss may be reported as **baseline-equivalent with known missing
exceptions**, not zero-missing healthy or production-ready. Existing historical
records are deliberately preserved; repairing the old 10 references is not a
new prerequisite for that equivalence claim. Strict-audit failures must remain
visible; quarantine, exact-target startup approval and production gates remain
unchanged. This is accurate reporting guidance, not a new runtime flag/policy
or evidence that any actual restore was run.

## Risks / Rollback

MA's baseline preservation reports 955 protected and 79 local files unchanged,
three process start identities unchanged and four HTTP 200 responses. WI020 did
not repeat those private/live checks; archive/member verification above is its
independent preservation evidence. No guarantee of unchanged live DB contents
or future scheduler behavior follows from this read-only source review.

MA subsequently reported a completed full origin clone at baseline
`52bbdcc0627aff404ad282cdccf4d6ce2418ea84`, 3,303 files, in the new receipt
directory's `remote-clone/`, with no local configuration copy. This is supplied
baseline-clone evidence, not candidate publication or candidate hash validation.
MA will fast-forward that clone only after publication and verify normal
checkout under the existing autocrlf=true/LF-attribute contract. WI020 neither
performed nor claims that future candidate update.

Candidate remote availability is pending publication/update receipts. Source and
safe history are not backups of private configuration, the complete keyring,
DB, media or raw evidence. The off-device encrypted backup destination and
independently recoverable decryption access remain unresolved. No private
backup upload/dump/fullrestore, new-OS installation, retained-data repair,
financial/mail action or production GO was performed. The 10 missing-reference
figure remains dated SR-93 evidence, not a fresh count or a repair target here.

No rollback ran. Correct only these two QA documents if the review needs an
addendum; deletion requires separate approval. Product/DB/runtime rollback is
inapplicable because WI020 changed none. Do not reset the shared worktree or
alter original/private archives to reverse this report.

## Follow-ups

WI chain checked: WI020 **Blocks none**. Return the candidate PASS and these
two deliverables to MA. MA must include the QA additions in the final allowed
stage, rerun documentation/whitespace checks, publish the selected revision,
then record actual updated origin-clone SHA, source checks and 96 public hashes.
This report does not close REQ004 or claim those future operations succeeded.

## Related Documents

- [WI020 summary](../user/WI-20260909-ATS-020-summary.md)
- [WI020 handoff](WI-20260909-ATS-020-handoff.md)
- [Approved REQ004](../user/REQ-20260909-ATS-004.md)

## Subsequent MA Integration Receipt

On 2026-09-09 MA supplied actual publication of
`65b8cce9d7c60370d761e3e6d3c34821a7c7e675` (104 scoped files), an exact
`origin/main` match and a clean full-origin clone fast-forward to that commit.
Source checks 38, synthetic tests 33/33 and docs (711 IDs, confirmed exit 0)
passed; all 96 public hashes matched, 104 selected tooling files were present
and private inputs were absent. WI020 completed PASS with no open findings.
These are subsequent MA executions, not retroactive WI020 execution claims.

[REQ004's MA receipt](../user/REQ-20260909-ATS-004.md#ma-delivery-receipt) is the current-state reference
for `remote-validation.json`, candidate-build limits and final preservation.
Source/history and preparation are delivered; REQ004 remains partial because
the private off-device backup destination is unanswered and backup/restore
was not performed. Production steps 2-4 remain unchanged.
