---
version: 1.1
last_updated: 2026-10-25
project: ATS
owner: SE
category: evidence-pack
status: completed
---
# Evidence Pack: WI-20261025-ATS-005

## Summary
Local operating decisions and fixed-input preflight completed. Existing JAR and production source unchanged; no target activation or private input access.

## Scope / DoD
- [x] Reused WI024, single-VPS templates, current source and existing bundle validator.
- [x] Explicit cold/audio-only modes, seeding off, single application scheduler owner and config-only rollback sequence.
- [x] Runnable fixed-input drift/mode guard, four focused tests and preserved JAR identity.
- [x] Documented synchronous billing/event caveat, SMTP/auth/TLS/key-ring preservation and external gates.

## Reference Documents (Tier 0-2)
MA injected Tier0 language, WI traceability, smallest approved change, secret handling and canonical term requirements from core-principles, development-standards, documentation-standards and glossary through the handoff and inherited context. Handoff/security constraints were applied; no independent complete Tier0 reread claimed. Read WI005 handoff, WI024 proposal and preparation evidence, source/config/template paths listed in the manifest, deployment README, EmailService and tests, build.gradle. create-wi-evidence-pack skill read fully and used. Project tag ATS is supplied by approved REQ/handoff and inherited workspace configuration.

## Evidence Pointers
- [Operating procedure and limits](../../build/WI-20261025-ATS-005/README.md)
- [Exact nonsecret modes and 11 source pins](../../build/WI-20261025-ATS-005/operating-modes.json)
- [Read-only checker](../../build/WI-20261025-ATS-005/check_local.py)
- [Local result](../../build/WI-20261025-ATS-005/local-check.json)
- [Four regression tests](../../build/WI-20261025-ATS-005/test_modes.py), [result](../../build/WI-20261025-ATS-005/tests.txt)
- [Unchanged template validator result](../../build/WI-20261025-ATS-005/template-audit.json)
- BackgroundTasksEnabledCondition.java:17-27; BackgroundTasksConfiguration.java:15-35; TrackAudioProcessingWorker.java:31-40; StorageMutationRecoveryService.java:42-46; WithdrawalBillingCleanupCoordinator.java:19-31.

## Commands and Results
`check_local.py`: LOCAL_PREPARATION_PASS, 11 pins matched, historical JAR preserved. JAR SHA256 f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81.
`python -B -m unittest discover -s build/WI-20261025-ATS-005 -p test_modes.py -v`: 4 PASS. Rejection coverage: general schedules enabled, seeding/unknown secret injected, cold master accidentally enabled.
`python -B scripts/deployment/validate_bundle.py --json`: template PASS; deployment_ready false / production HOLD deliberately retained. No full Java suite/build needed because application source was not edited.
Initial PowerShell brace-path read failed before execution, corrected to rg parent-directory globs. No consequential retry.

## Risks / Rollback
Preparation files are not target env imports. Source/hash checks are local identities, not effective Spring precedence or native syntax proof. Audio-only suppresses Spring schedules and startup recovery but not synchronous payment APIs or after-commit withdrawal key deletion. Full operating activation remains gated. SMTP defaults must not replace effective Gmail auth/TLS; acceptance keys must not replace target key ring. No server configuration changed, so no runtime rollback required; remove only this WI's local artifacts if abandoned. Do not overwrite historical JAR, prior evidence or unrelated work.

## Follow-ups
WI005 Blocks WI006 independent review, then WI007 integration per approved REQ. MA should chain review promptly. Exact MAILv2 adapter is possible with existing dependencies but was not implemented in this build-only WI; API contract, transport tests and new artifact review precede any switch. Provider account/contract/cost inputs remain separate human boundaries. Do not replay completed account/grant writes.

## WI008 reviewed source-pin update

WI006 approved the additive application.yml mail selector/configuration diff. WI005 preserved operating-modes-v1.json, local-check-v1.json and artifact-hashes-v1.json, updated only that YAML source pin to 1f3151a554124831997cca8979b4927a8f5bff4baf349afe9326e4d2da2955e6, and reran the local checker. All other pins/modes and the historical JAR remain unchanged. This is not a deployment or a claim that the new source equals the old JAR. WI008 independently owns the new mail artifact; SMTP remains default. Earlier no-source-edit statements describe WI005 itself and its initial checkpoint. No repeated full test suite.
