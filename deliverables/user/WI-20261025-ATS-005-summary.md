---
version: 1.1
last_updated: 2026-10-25
project: ATS
owner: SE
category: reference
status: completed
---
# WI-20261025-ATS-005 Summary

Local operating preparation is complete; server settings were not applied.

The existing implementation already separates audio-only work from general scheduled jobs. Prepared two exact nonsecret modes: cold preparation and a bounded audio candidate; both disable test seeding and acceptance mode. The single application process owns scheduled jobs, user owns deployment approval and MA owns reviewed execution/evidence. Current Gmail, target billing key ring, six plans and existing accounts remain unchanged.

Audio-only is not a blanket payment kill switch: synchronous payment APIs and after-commit withdrawal cleanup remain possible. Full scheduled operation requires provider readiness and pending-job review. No new library, source edit, rebuilt JAR, secret read, mail, charge or server action occurred.

Fixed-input preflight matched 11 public files and the existing JAR; four focused mode guards passed. The reused deployment-template validator passed, explicitly retaining deployment readiness false. [Procedure and runnable commands](../../build/WI-20261025-ATS-005/README.md) include actual configuration provenance, limited rollback and human/provider boundaries. [Evidence](../agent/WI-20261025-ATS-005-evidence-pack.md).

Next: WI006 independent review; combine provider inputs with exact MAILv2 transport work if assigned, then preserve current Gmail until the new transport and delivery are verified. Existing NCP account absence, unrequested Toss contract and chosen ID1 mailbox are already answered; do not ask them again.

## WI008 reviewed source-pin update

WI006 approved the additive application.yml mail selector/configuration diff. WI005 preserved operating-modes-v1.json, local-check-v1.json and artifact-hashes-v1.json, updated only that YAML source pin to 1f3151a554124831997cca8979b4927a8f5bff4baf349afe9326e4d2da2955e6, and reran the local checker. All other pins/modes and the historical JAR remain unchanged. This is not a deployment or a claim that the new source equals the old JAR. WI008 independently owns the new mail artifact; SMTP remains default. Earlier no-source-edit statements describe WI005 itself and its initial checkpoint. No repeated full test suite.
