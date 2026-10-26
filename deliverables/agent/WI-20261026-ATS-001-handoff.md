---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: MA
category: reference
status: stable
dependencies:
  - path: ../user/REQ-20261024-ATS-001.md
    reason: Approved local preparation
---
# WI-20261026-ATS-001 Local operating safeguards preparation
[WI HEADER]
WI ID: WI-20261026-ATS-001
REQ: REQ-20261024-ATS-001
Agent: se
Depends On: WI-20261025-ATS-005
Blocks: WI-20261026-ATS-003
Execution state: completed; integrated by WI-20261026-ATS-003.

[WI SUMMARY]
Why: Advance useful policy-independent work while client policy/provider inputs are pending.
Scope: Local audit of existing backup/restore/scheduling/alert/reboot/deployment rollback/TLS assets and retained receipts. Produce actionable evidence matrix and ordered bounded execution plan using existing tool paths, expected evidence, stop/cleanup and cost/access prerequisites. Run only inspected public fixed-path local validators where informative; preserve prior outputs, no full trial replay.
DoD: Distinguish confirmed dated target evidence, current source-only preparation, gaps and provider/user/runtime prerequisites. Correct stale interpretations in new report, not historic evidence. Explain exact blockers to stage3 completion, no vague repeated planning.
Constraints: No network/server/SSH/DB/app/provider/backup/repository calls, env/private body reads, installs, secrets, realpayment, costs, activation, production template/source mutations, commit/push. Read public source and safe receipts only. Do not generate another execution tool when existing tools suffice. No unsupported current host/cost/recovery claims. No retention/prune authorization.

[ACCEPTANCE CRITERIA]
- Inventory exact backup coverage and omissions: latest actual snapshot vs later ID/grant/audio/reset changes; bounded restore versus fullDR; secrets references without secret values; secondcopy and cost/retention caveats.
- Determine actual template timer/hooks/alerts readiness; distinguish unconfigured fail-closed hooks from functioning recurringbackup. Reboot/restart limits and sideeffects of app scheduler/payment must be explicit. TLS renew and expiry monitoring are separate.
- Reuse 20261025 WI005 source pin checker only after checking it is read-only; stdout/new receipt in ownfolder, do not overwrite original historicalreceipt. Bundlevalidator optional if useful not redundant.
- Create operator-useful ordered checklist with effect/read-only labels, exact reused tool/command references, gates/success evidence/cleanup; no executable destructive blind recipe.
- No source/filecode corrections required; record potential minimal fixes as concrete followup with scoped effect, not applied.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: AGENTS.md; docs/policies/security-policy.md.
Context: docs/index.md Current Decision Entry; approvedREQ; build/WI-20261025-ATS-005/README.md and sourcepins; deliverables/agent/WI-20261024-ATS-018-evidence-pack.md finaladdenda; deliverables/agent/WI-20261025-ATS-003-evidence-pack.md.
Files: deploy/single-vps/runbooks/backup-restore.md; restic-backup-plan.md; domain-tls.md; os-maintenance.md; deployment.md; deploy/single-vps/systemd/; deploy/single-vps/backup/; scripts/deployment/backup/. Expand targeted sourcepaths only as needed.

[OUTPUT CONTRACT]
Owned artifacts: build/WI-20261026-ATS-001/ operating-readiness.md, next-runtime-checklist.md, local-check evidence.
User-facing: deliverables/user/WI-20261026-ATS-001-summary.md Korean.
Agent-facing: deliverables/agent/WI-20261026-ATS-001-evidence-pack.md via create-wi-evidence-pack skill.
No REQ/index edits; WI003 integration owner.

[TRACEABILITY REQUIREMENTS]
Exact sources/lines, target verificationdates and owners, current localcheck commands/results, nonverified boundaries. MA injected Tier0: scoped sustainable work, Korean conversation, English internaldocs, metadata/canonical terms, smallestauthorized action, preserve history/unrelated tree. Tier0 pointers for focused reference; no broad reread. Source HEAD main91a16e7 dirtytree, never reset. No nextstage execution approval inferred.
