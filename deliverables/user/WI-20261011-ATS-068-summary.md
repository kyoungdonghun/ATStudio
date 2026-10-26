---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: QA
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-068-evidence-pack.md
    reason: Build, source, audit and preservation evidence
  - path: ../agent/WI-20261011-ATS-068-artifact.json
    reason: Canonical fresh release identity
---

# WI068 Fresh Local Release

> Purpose: Hand MA the audited JAR and exact current source identity.

## Result

**LOCAL_ARTIFACT_PASS_TARGET_NOT_RUN.** Local build/audit complete; native release
remains separate. Verified by QA on 2026-10-11 at 22:10 KST, Windows/JDK 17/Gradle 9.3.

- [Fresh JAR](../../build/WI068-release-run-02/build/libs/ATStudio-0.0.1-SNAPSHOT.jar):70,307,578 bytes.
- JAR SHA-256: `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.
- Full643 input manifest, equal before/after: `c6fc15d8f38c6597d159c7dd258048d25e74b3d03f5eb5596b701219a8ae207e`.
- PG637 reviewed-source digest: `fb367e966e72b6ceb24de6afabde9efb74333275fe41de8aa613eb119979a58e`.
- [Canonical manifest](../agent/WI-20261011-ATS-068-artifact.json) SHA-256: `af998a73bf61da6cbf1717751e2e79c16186fc3a037f90102d77da71d1ead3ba`.

Offline compileJava/bootJar passed in 32.44 seconds with 4 executed tasks. Audit
verified 616 application classes, 4 resources, 101 byte-identical runtime libraries,
zero duplicate/private named entries and unchanged frontend336/SPA143.160 exact
preservation records and pinned failed-run/review receipts stayed unchanged.

SE 127 and independent PG 147 tests are separately attributed and overlap; QA checked
their XML/hashes and did not rerun them. Full suite/coverage and actual server behavior are
not claimed. Source checkout remains dirty at HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`.

## Retained History And Next Gate

A prelaunch Git-path ambiguity was fixed without starting Gradle. The first actual
Gradle run failed because the read-only cache path pointed one directory too deep.
MA explicitly approved its correction and one fresh attempt;10 fix checks and the
second build passed. All old helpers/gates and failed run-01 remain preserved.

No source/frontend edits, secrets, dependency downloads, server/app/DB/native,
backup/cloud activation, deletion, commit or push occurred. MA may advance WI067's
fresh artifact pins using this manifest; independent/native release is still required.

## Related Documents

- [Detailed Evidence Pack](../agent/WI-20261011-ATS-068-evidence-pack.md)
- [Canonical manifest](../agent/WI-20261011-ATS-068-artifact.json)
- [WI068 handoff](../agent/WI-20261011-ATS-068-handoff.md)
