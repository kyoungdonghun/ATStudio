---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: cr
category: work-summary
status: active
dependencies:
  - path: ../agent/WI-20260909-ATS-012-evidence-pack.md
    reason: Independent review evidence and exact manifests
---

# WI-20260909-ATS-012: Independent Storage And Dependency Review

## Result
No additional unresolved product blocker was found in the exact WI003/WI005 source and test scope. One concrete build blocker was observed in MA's first patched build: the new multipart test used a lambda for Tomcat's two-method InputBuffer. WI005 corrected it during this review; the corrected source was rechecked. **Execution acceptance remains pending**, not a full verification pass.

## Verified
- DATA-01 preserves License/download event identity and quota input on Track soft deletion.
- DATA-02 detaches Album/Playlist thumbnail references before after-commit cleanup, protects shared/inactive references, and keeps strict startup audit unchanged.
- DATA-03 covers identified material Track/Album writers with established lock ordering; the reviewed H2 tests include stale-write control and independent competing transactions.
- STORAGE-04 journals before writing, registers in-flight protection before prepare, excludes active work before claim/attempt mutation, releases on failure/completion, and retains cleanup ownership for retry.
- Boot 4.0.8 and managed Tomcat/Framework/Security versions were checked against published metadata and the newly assembled JAR, not the running server.
- The 128-part connector limit and ten real-parser/Spring-resolver/production-handler test cases were inspected. Corrected test execution remains MA-owned.
- Exactly 15 dev-only lock records changed; 16 checked dependency edges satisfy ranges. All 15 live installed package versions remain old. The hidden lock lists new versions, matching WI005's disclosed metadata side effect.

## Execution Boundary
WI012 ran no Gradle/npm runner or application and changed only this summary and its evidence pack. MA artifacts were read back: initial 184 total / 183 passed / 1 skipped; second storage/auth 158 passed; payment 75 total / 72 passed / 3 skipped, counted only for reconciliation. These overlapping, earlier checkpoints are not patched full-build proof. The first patched build failed at test compilation before any tests or coverage gates ran.

MA still owns the corrected multipart/full backend rerun, which awaits a payment-related fix outside WI012 scope. No JVM product incompatibility has been found so far. MA also reported that the initial isolated frontend snapshot omitted four inputs; all 334 inputs were restored and the full run is being repeated. The initial 1,399 result is not a full PASS. These latest scheduling/inventory facts are MA-provided, not independently rerun by WI012.

H2/object recreation is not live MySQL/process-restart/proxy proof. Hidden-lock repair, recovery batch fairness and contention remain separate maintenance/operational matters.

## Deliverables And Next Step
- [Evidence pack and exact 26-file hashes](../agent/WI-20260909-ATS-012-evidence-pack.md)
- Return to MA for remaining quality results, WI013 documentation and WI014 integration. No subdelegation, unrelated audit, product changes, runtime restart or release approval.
