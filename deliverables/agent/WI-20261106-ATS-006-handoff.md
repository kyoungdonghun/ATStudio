---
version: 1.0
last_updated: 2026-11-06
project: ATS
owner: MA
status: delegated
---
# WI-20261106-ATS-006 — Select and validate the Git checkpoint

[WI HEADER]
WI ID: WI-20261106-ATS-006
REQ: REQ-20261106-ATS-003
Agent: eo/docops (checkpoint_prepare)
Depends On: WI005 PG clearance for final approval; independent inventory may start now
Blocks: MA Git execution, then final checkpoint closure

[WI SUMMARY]
Why: Commit coherent reviewed functionality and operational sources with safe traceability, without including unrelated/private accumulated files.
Scope: Inventory current diffs/untracked files against latest completed WIs and prior Oct26 commit exclusion records; choose functional groups and required dependency closure; reuse existing validation source hashes; prepare exact path/hash manifest and meaningful commit messages; safe minimal docs/ignore/export fixes if needed. EO shareability gate after PG.
DoD: Reviewed subset ready to stage plus reproducibility/verification evidence, retained exclusions and two-set closure. MA executes actual Git commands.
Forbidden: No server/network/provider/cloud/services/real DB/backup/restore/mail/payment/UAC/Git writes, no deletion/reset/clean/history rewrite, no broad source refactor or new deps. Do not read private ignored bodies or expose identifiers. Preserve original raw evidence and prior candidate hashes. Ask MA before any application source modification, since pinned Java/FE candidates should remain reusable.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Map every candidate to completed work; leave unrelated old untracked docs/screenshots/PDFs/local outputs intact.
- [ ] Group coherent code/tests/config templates, then safe docs; clean checkout dependencies must not rely silently on excluded files.
- [ ] Reuse existing build/test evidence only for matching source; necessary syntax/whitespace/dependency/targeted checks pass with honest baseline limitations.
- [ ] Existing source candidate hashes maintained; artifacts/raw logs/credentials excluded; no misleading Git-backup claim.
- [ ] PG hash-bound review and EO final include/exclude decision; proposed commits in Korean with WI traceability.
Performance:
- [ ] No blanket rerun of unrelated Java/FE suites; use focused checks justified by delta.
Quality:
- [ ] Apply lint/evidence/validate-docs skills within explicit selected scope; inspect CLI implementation before invoking help (prior validator ignores --help).
- [ ] Explain global archived-doc failures separately; no sweeping cleanup to make them green.

[INPUT POINTERS]
Tier 0: AGENTS.md; docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md; docs/policies/execution-policy.md
Tier 2: deliverables/user/REQ-20261106-ATS-003.md; WI005 handoff; docs/index.md; .gitignore; .claude/hooks/pre-commit.sh; scripts/reconstruction/scan_commit_candidates_wi004.py; latest WI-20261106-ATS-003/004 evidence, WI-20261105-ATS-007 integration, earlier 10/26 Git/privacy checkpoint records discovered by git log and rg.
Snapshot: main@f0c73d449e4f2773ad91cb2fb34093ddd57cd741; 56 tracked changed/423 untracked before these new docs; staged empty. Prior candidates remain under ignored build/. Existing local remote origin is GitHub HTTPS. MA handles remote/hooks safety and auth.

[OUTPUT CONTRACT]
Use evidence skill for WI006 summary/evidence. Save reviewed file paths/hash/group manifest, exclusion list, reproduction commands and proposed commit messages under build/WI-20261106-ATS-006; provide safe tracked checkpoint summary if originals cannot be shared. Communicate with /root/commit_privacy. Do not stage/commit/push. After MA Git results, append exact commits/remote confirmation and close REQ/Current Decision Entry through a separate small safe closure commit if needed; avoid recursive documentation commits.

[TRACEABILITY REQUIREMENTS]
Preserve old docs; explicitly identify missing public reconstruction evidence when private links are retained. Safe extracted artifacts may need a tracked sanitized manifest rather than committing ignored build logs. Source changes after security review invalidate relevant hashes; rescan only delta. No downstream server WI triggered by this Git checkpoint.
