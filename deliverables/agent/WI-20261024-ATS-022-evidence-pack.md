---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: evidence-pack
status: completed
related_wi: WI-20261024-ATS-022
---

# Evidence Pack: WI-20261024-ATS-022

## Summary

Updated only the approval section of AGENTS.md under the user's direct
2026-10-24 governance approval. From stage 2 onward, approved-scope recovery,
tool corrections, review and safe retries proceed without repeated permission.
New cost/scope boundaries and mandatory tool/user handoffs remain gates.
The amendment does not authorize starting stage 2.

## Scope / DoD Check

- [x] Replaced blanket always-ask exceptions with scope/effect-based boundaries.
- [x] Covered in-scope unexpected blockers and progress without approval stops.
- [x] Required read-only reconciliation before uncertain side-effect retries.
- [x] Required diagnosis/focused verification instead of same-cause full reruns.
- [x] Preserved unrelated AGENTS.md edits and all historical evidence.
- [x] Created this evidence pack and the user summary; no operational changes.

## Reference Documents (Tier 0-2)

[Generated WI022 handoff](WI-20261024-ATS-022-handoff.md) supplies the direct
approval and injected Tier0 core-principles, documentation/development standards
and glossary. Its Tier2 pointers are AGENTS.md, current index and REQ007; no
changes to the index or REQ were authorized or made. Applied the previously
loaded create-wi-evidence-pack workflow. The direct user instruction explicitly
supersedes the broader blanket ask rules within this edit's approved scope.

## Evidence Pointers

- [AGENTS.md approval policy](../../AGENTS.md#approval-before-execution-req-based-single-gate): the sole existing-file edit.
- [User summary](../user/WI-20261024-ATS-022-summary.md): effective stage and retained boundaries.
- [Handoff](WI-20261024-ATS-022-handoff.md): direct approval, exact three-file scope and Blocks: none.

## Commands & Outputs

Read the handoff and targeted policy, and inspected the existing AGENTS.md diff.
The pre-existing Evidence Reading Before Operational Decisions addition is
unrelated and preserved. Captured byte hashes of the file prefix/suffix outside
the approval section for focused preservation verification.

Validation PASS: scoped `git diff --check` exit 0, byte-identical prefix/suffix
outside the approval section, and metadata/whitespace plus six relative links
across the two new documents. Git emitted only its existing CRLF-to-LF notice;
no line-ending conversion was performed. No build, test suite, source/runtime
action, server/provider/secret access, commit or push.

## Risks / Rollback

Approval persistence does not authorize new cost, scope expansion, unapproved
destructive action or bypassing tool safeguards. Unknown-outcome mutations must
not be blindly replayed. Any future policy adjustment should be a targeted
approved edit preserving the existing unrelated working-tree changes.

## Follow-ups

WI022 is completed and blocks no next WI. Stage 2 was not started; this
documentation-only approval does not change broader REQ completion status.
