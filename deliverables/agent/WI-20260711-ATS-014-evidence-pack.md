# Evidence Pack: WI-20260711-ATS-014

## Summary

- Ran the configured documentation validator and `git diff --check` read-only, inspected index-count drift without correcting it, and recorded the exact outcomes and coverage limitations.

## Scope / DoD Check

- [x] Ran the configured documentation validator.
- [x] Ran `git diff --check`.
- [x] Recorded exact commands, exit codes, and output summaries.
- [x] Distinguished validator coverage from broader documentation correctness.
- [x] Inspected Document Overview count drift without fixing it.
- [x] Recorded whitespace results and line-ending warnings separately.
- [x] Modified no documentation or source files.
- [x] Created only this Evidence Pack and the paired user summary.

## Reference Documents (Tier 0-2)

| Tier | Document / pointer | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, language, traceability, and non-destructive execution rules |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure and index requirements |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 0 | `docs/standards/development-standards.md` | Workspace-mandated Tier 0 context and evidence-first rules |
| 1 | `docs/policies/quality-gates.md` | Documentation validation and quality evidence expectations |
| 2 | `.agents/skills/validate-docs/SKILL.md` | Configured validator command and declared checks |
| 2 | `.agents/skills/sync-docs-index/SKILL.md` | Read-only count-check procedure and category counting rules |
| REQ | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| WI | `deliverables/agent/WI-20260711-ATS-001-evidence-pack.md` | Upstream documentation baseline |
| WI | `deliverables/agent/WI-20260711-ATS-014-handoff.md` | Output contract, constraints, and acceptance criteria |

## Execution Baseline

| Field | Value |
|---|---|
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Upstream state | `origin/dev/kyoung [ahead 3]` |
| Worktree | Dirty before execution; concurrent modifications and untracked WI artifacts were preserved |

The worktree already contained modified/deleted client documentation, a modified root index, logs, generated output, and other WI deliverables. None were reverted or edited by this WI.

## Commands & Outputs

### Documentation Validator

Exact command:

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
```

Result:

```text
Tier 0 Documents: PASSED - all required files exist
Internal Links: PASSED - no broken internal links
Traceability IDs: PASSED - 295 IDs matched supported formats
Document Index: PASSED - all documents listed in index
SUMMARY: All validations passed
Exit code: 0
```

The handoff also points to `scripts/validate_docs.py`, but that path does not exist in the current worktree. The configured skill command above resolves to the existing bundled validator and was used.

### Git Whitespace Check

Exact command:

```powershell
git diff --check
```

Result:

```text
Whitespace errors: 0
Exit code: 0
Line-ending warnings: 6 tracked files
```

Warning targets:

- `docs/client/0-site-policy.md`
- `docs/client/4-sr-format.md`
- `docs/client/5-ai-prompt.md`
- `docs/client/index.md`
- `docs/client/testing-guide.md`
- `docs/index.md`

Each warning states that LF will be replaced by CRLF the next time Git touches the working-copy file. These are warnings, not `git diff --check` whitespace errors.

### Document Overview Count Check

The `.agents/skills/sync-docs-index/SKILL.md` check procedure was reproduced with a read-only PowerShell enumeration. It counted direct `.md` children excluding `index.md`, except Design, which was recursive. Client and Payment were also included because they are present in the current root Document Overview.

| Category | Indexed | Actual | Result |
|---|---:|---:|---|
| Architecture | 1 | 1 | MATCH |
| Design | 24 | 24 | MATCH |
| Policies | 8 | 8 | MATCH |
| Standards | 13 | 12 | **MISMATCH** |
| Templates | 18 | 18 | MATCH |
| Registry | 4 | 4 | MATCH |
| Audit | 2 | 2 | MATCH |
| Client | 8 | 8 | MATCH |
| Payment | 7 | 7 | MATCH |
| SR | 92 | 92 | MATCH |
| Retrospective | 4 | 4 | MATCH |
| ADR | 1 | 1 | MATCH |
| UI | 3 | 3 | MATCH |
| Eval | 0 | 0 | MATCH |
| **Total** | **185** | **184** | **MISMATCH** |

Evidence details:

- `docs/index.md:22` records Standards as 13.
- `docs/index.md:34` records the total as 185.
- `docs/standards/` currently has 12 direct non-index Markdown files.
- `docs/standards/public_data/standard_glossary/README.md` is nested and is excluded by the skill's non-recursive Standards rule.
- HEAD already recorded Standards as 13; the current root-index diff changes Client from 7 to 8 and total from 184 to 185, but does not change the Standards row.

## Validator Coverage Limitations

The successful validator result is authoritative only for its implemented checks:

1. Existence of the four hard-coded Tier 0 paths.
2. Existence of targets for Markdown links parsed as `[text](path)`.
3. Counting strings that match supported REQ/WI/STD regular expressions.
4. Index coverage inferred through path, filename, or stem substring presence in the root or an ancestor index.

It does not validate:

- required frontmatter fields, allowed metadata values, or metadata freshness;
- semantic/content accuracy or implementation-to-document alignment;
- external URL availability;
- bare paths or paths in code spans;
- whether every ID-like string is valid or whether a matched ID resolves to an artifact;
- Document Overview numeric counts or total arithmetic;
- untracked-file whitespace through `git diff --check`.

The index result can also be satisfied by broad filename/stem substring matches, so it must not be interpreted as a strict parsed index graph.

## Tests

- `python .agents/skills/validate-docs/scripts/validate_docs.py` - PASS, exit `0`.
- `git diff --check` - PASS, exit `0`; zero whitespace errors and six LF-to-CRLF warnings.
- Read-only Document Overview count check - FAIL for count synchronization: Standards `13 -> 12`, total `185 -> 184`; all other rows match.
- No Gradle, npm, browser, database, network, or write-mode index synchronization was run; they are outside this WI.

## Evidence Pointers

- Files created:
  - `deliverables/user/WI-20260711-ATS-014-summary.md` - user-facing verdict and limitations.
  - `deliverables/agent/WI-20260711-ATS-014-evidence-pack.md` - reproducible commands, outputs, count matrix, and constraints.
- Inputs inspected:
  - `deliverables/agent/WI-20260711-ATS-014-handoff.md`
  - `.agents/skills/validate-docs/scripts/validate_docs.py`
  - `.agents/skills/validate-docs/SKILL.md`
  - `.agents/skills/sync-docs-index/SKILL.md`
  - `docs/index.md:15-34`
  - `docs/standards/`

## Risks / Rollback

### Risks

- Concurrent workers may change the shared worktree after this snapshot; rerun the commands before treating these counts as current.
- The line-ending warnings may become actual diff churn when Git next rewrites the affected files.
- A validator PASS does not close the Standards count mismatch or any documentation quality dimension outside the implemented checks.

### Rollback

- No existing file was changed.
- If explicitly requested, rollback consists only of removing:
  - `deliverables/user/WI-20260711-ATS-014-summary.md`
  - `deliverables/agent/WI-20260711-ATS-014-evidence-pack.md`

## Follow-up / WI Chain

- Per the handoff, this WI blocks `WI-20260711-ATS-019`.
- `WI-20260711-ATS-019` should decide whether Standards remains a non-recursive count of 12 or whether the index contract should include nested reference Markdown, then align the Standards row and total under an approved write scope.
