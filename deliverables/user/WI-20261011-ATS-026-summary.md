---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: work-summary
status: stable
dependencies:
  - path: REQ-20261011-ATS-003.md
    reason: Fresh repair and one-attempt approval
  - path: ../agent/WI-20261011-ATS-026-evidence-pack.md
    reason: Exact identities and test evidence
---

# WI026 Log Parent Repair

> Purpose: Report the narrow local repair and the remaining WI027 gate.

## 1. Result

**TL;DR: Local complete. WI026 native NOT_RUN. Independent WI027 required.**

The new helper uses `/var/lib/atstudio/wi026-private-logs` and separate WI026
upload/tools/approval/claim/run/release/unit names. It leaves `/var/log` and all
existing parent permissions untouched. The JAR input remains the WI022 upload.
Runtime logic differs from WI024 v2 only by log path and fresh identifiers.

All21 original WI024 files, including history, retain their exact hashes. The
12:56:16 KST WI024 HOLD/PREPARE/PATH_ANCESTRY and cleanup PASS remain MA-reported
historical results, not a successful startup or an authorization to reuse its claim.

## 2. Verification and Identity

- Owner tests42/42, retargeted PG regressions8/8, HTTP lifetime3/3, parent metadata4/4 PASS.
- Supplied root:syslog0775 is rejected; supplied root:root0755 ancestry with new private0700/0600 objects is accepted in inert tests.
- Group-writable/symlink/nonroot ancestry remains rejected; no permission-repair code added.
- Five Python files compile in memory; both staging/approval Python snippets parse.
- One-start180s, five-second/64KiB GET, BPF, secret/keyring and cleanup boundaries remain unchanged.

Bundle: `7136e749de6b7dd2ae6ecbf2585a3dc1ddb0fd083ef81ab48f96e2f82aa44f05`.
README: `e2305e742db1af5da6a272c31d3b641fc2386e96053f7511fb99035c23c52360`.
Individual file and three command-block hashes accompany the bundle.

## 3. Next Step and Limits

Recommend exact WI027 review, followed only by MA's fresh target checks and
approved single attempt. Local tests use observed metadata as fixtures; no new
target measurement, socket, service, DB operation, secret access or installation
was performed. Failed runtime checks remain HOLD. SIGKILL/host loss can prevent
cleanup and require the retained recovery console.

Rollback before operation: do not execute WI026. Preserve every WI024 artifact
and claim; do not delete, overwrite, weaken guards or automatically retry.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-026-evidence-pack.md): Delta, pins, commands and limits.
- [MA recipe](../../build/WI026-private-start/README.md): Exact staging/approval/run blocks.
- [Handoff](../agent/WI-20261011-ATS-026-handoff.md): Scope and native metadata.
