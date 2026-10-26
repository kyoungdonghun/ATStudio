---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: draft
dependencies:
  - path: REQ-20261011-ATS-007.md
    reason: Approved local sidecar preparation
  - path: ../agent/WI-20261011-ATS-063-evidence-pack.md
    reason: Exact files, hashes, commands and verification boundaries
---

# WI063: First-Admin Candidate And Tiny WAV Fixtures

## Summary

**Ready for independent review, not native execution.** The new helper prepares the
first-admin inspect/promotion path and two synthetic WAV files. Both native-release
constants remain false; buyer entitlement is deferred. No app source, old deployment
tool, server, DB, account, mail or secret configuration was changed by this work.

## Delivered

- New Python wrapper and Java JDBC helper: approved exact existing verified/logged-out
  USER only, zero ADMIN including deleted ADMIN rows, conditional one-row promotion and
  matching audit, bounded/redacted I/O, private expiring approval and one-use receipts.
- New wrapper tests and in-memory JDBC harness, with replay, drift, rollback and
  uncertain-commit/receipt-publication failure cases. No automatic retry or account creation.
- Two deterministic 10-second stereo 44.1 kHz/16-bit WAVs under
  `build/WI063-fixtures-final/`, 1,764,044 bytes each. No uploads or conversions were run.
- New isolated Gradle init and test output under `build/WI063-focused-20261011/`.
  Test JVM environment was restricted to SystemRoot/WINDIR/TEMP/TMP/PATH.

## Verification

| Check | Local result |
|---|---|
| Java 17 compilation | PASS, no compiler diagnostics |
| In-memory JDBC harness | 52 cases passed |
| Python provisioning tests | 12 passed |
| WAV tests | 6 passed, 1 actual-symlink test skipped for Windows privilege; simulated reparse rejection passed |
| Existing focused service tests | 119 passed, 0 failure/error/skip, confirmed from five XML reports |
| Native/DB/concurrency/SMTP/browser trial | NOT RUN |
| Numeric coverage / independent review | Not established |
| WI063 report links / nine-file whitespace | PASS; full repository document validation still reports six unrelated existing link errors |

The existing focused tests comprise Email 15, User 74, Auth 17, Download 10 and download
concurrency source contracts 3. They use mocks/source inspection, not a Spring app or DB.
Full compilation of existing tests emitted unrelated deprecation/unchecked notes and a
Mockito JVM sharing warning. The application and old build results were preserved.

## Next Executable Gate

MA can now create a PG/RE review WI using the exact file hashes and local artifact
manifest in the [Evidence Pack](../agent/WI-20261011-ATS-063-evidence-pack.md).
The reviewer can rerun the local tests in fresh WI063 build directories with no secrets.

After review, a separately approved isolated MySQL proof must exercise two competing
principals/connections and demonstrate at most one committed promotion plus audit,
rollback/error behavior and safe receipt reconciliation. Keep both native gates false
through that proof. Native release needs new reviewed pins, current target/source/schema
identity and MA-controlled private inputs; no production PASS is implied here.

The prepared archive pin only identifies the existing local driver-source JAR, not
MA's current remote artifact or current application source. MA must resolve that before
release. Stopped writers, recovery readiness and no-capture controls remain explicit
operator attestations rather than observations made by this sidecar.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-063-evidence-pack.md): Paths, pins, results, limits and rollback.
- [Handoff](../agent/WI-20261011-ATS-063-handoff.md): Bounded file ownership.
- [REQ007](REQ-20261011-ATS-007.md): Active approved request; not closed by this local work.
