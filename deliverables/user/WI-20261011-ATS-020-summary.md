---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: QA
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-002.md
    reason: Approved local preparation
  - path: ../agent/WI-20261011-ATS-020-evidence-pack.md
    reason: Commands, artifact identity and verification limits
---

# WI-20261011-ATS-020: New Local Backend Artifact

> Purpose: Supply a separately preserved current-source JAR for future approved staging.

## Result

**LOCAL PASS; not deployed or started.** Offline compilation/packaging completed
on Windows at 2026-10-11 11:26 KST, Java17/Gradle9.3.0, in29s.

- New JAR: [ATStudio-0.0.1-SNAPSHOT.jar](../../build/WI020-release-v3/build/libs/ATStudio-0.0.1-SNAPSHOT.jar), 70,306,351 bytes.
- SHA-256: `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`.
- [New public manifest](../agent/WI-20261011-ATS-020-artifact.json) records exact inputs, archive entries, prerequisite receipts and retained artifacts.
- Existing SPA remains separately reusable at `build/WI-20261010-ATS-026-frontend-dist`; all143 files and historical336 frontend inputs match.

## Verified Scope

641 backend inputs unchanged before/after; 615 Java17 classes and four public
resources match fresh output/source. All101 libraries and136 entity/related
model classes match the retained JAR, as do schema.sql and seed.sql. No duplicate
or prohibited private-named archive entries. All7,912 preservation records match;
old JAR SHA `517b7454...10e0a` and old manifest SHA `fbeb7442...fa4d9c` remain intact.

PG's latest19/19+27/27 PASS and RE's87/87 are prior independent evidence, bound
to current hashes, not rerun counts. Two local wrapper attempts stopped before
Gradle on environment-name checks; their records were retained. The final build
used a cleared environment and emitted no compiler warning. Existing transitive
JUnit/Hamcrest libraries were preserved, not added or upgraded.

## Limits and Next Step

The old native read-only Hibernate PASS remains historical evidence, not a new
runtime PASS. Linux isolation, actual OFF binding, full app/DB startup and cleanup
remain NOT RUN. No secrets, remote work, installation, frontend rebuild, deletion,
commit or push. Full tests/coverage/SAST/CVE scans were outside this handoff.

For the next separately approved staging, select the new v3-path JAR rather than
`build/libs/ATStudio-0.0.1-SNAPSHOT.jar`. Do not patch the old manifest/checker or
remove its evidence. Withholding the new candidate requires no runtime rollback.

**WI020 complete; MA's immediate successor is WI021 documentation integration.
REQ002 remains open; production remains HOLD.**

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-020-evidence-pack.md): Full receipts and exact historical pins.
- [Public manifest](../agent/WI-20261011-ATS-020-artifact.json): Machine-readable provenance.
- [Approved REQ](REQ-20261011-ATS-002.md): Scope and successor chain.
