---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: reference
status: active
---

# WI-20260908-ATS-014: Main Baseline Consolidation

## Outcome

- `main` is the only active local/remote branch and the GitHub default; the root checkout is the only remaining worktree.
- Eight previous remote branch tips and the client snapshot including its two dirty HomePage files are preserved in nine remotely verified annotated tags. Seven obsolete remote branches and two obsolete local branches were removed after preservation.
- The old README-only main was replaced with the verified V1 history using an exact-tip lease. No unrelated-history merge or client-only temporary thumbnail policy was imported.
- Six current documentation files now point to main; historical evidence remains dated and SR-93 remains OPEN.

## Verification

- Documentation validation passed with 682 traceability IDs; whitespace checks passed.
- Product/config source matches pre-promotion `5328482`; 125 unrelated untracked files remain byte-identical and outside the commit scope.
- Backend/frontend/Tunnel PIDs and start times remain unchanged. Local/public pages and APIs each returned HTTP 200; the pinned backend JAR hash is unchanged.
- No DB/media/secret change, process restart, mail/payment operation or application-test rerun was performed. This is baseline consolidation, not production deployment or security approval.

## Evidence And Next Step

[Integrated evidence and remote archive ledger](../agent/WI-20260908-ATS-014-evidence-pack.md) includes recovery pointers and the publication-verification procedure. The final response supplies the committed/pushed main SHA after verification.

Next is the bounded code/security review against this single source baseline, followed by remaining target-production checks. No additional feature development is implied.
