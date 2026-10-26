---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-002.md
    reason: Approved local-only review
  - path: ../agent/WI-20261011-ATS-018-evidence-pack.md
    reason: Reproduction, hashes and findings
---

# WI-20261011-ATS-018 Security Review Summary

> Purpose: Report local candidate-validation blockers separately from future runtime gates.

## Decision

**LOCAL VALIDATION HOLD. WI020 packaging is not cleared by PG.**

The unchanged candidate passes its original12 tests, but independent red-team
testing finds8 unsafe mutations still pass all12. PG's27 rejection/baseline
cases return19 PASS and8 FAIL. No original candidate or application file was
modified, and no application, database or network operation was performed.

## Findings

1. **P2: repeated unit directives are silently discarded.** The parser checks
   only the last occurrence, missing an added all-address allow rule, writable
   path, prestart shell command or extra EnvironmentFile.
2. **P2: key values are unasserted.** An unreviewed/privileged Java executable,
   external SMTP hostname and changed storage root also pass.

The actual reviewed unit does not contain these mutations. No scoped defect was
found in WI016 automatic-work suppression; manual methods and mandatory
integrity/root/keyring/JWT protections remain. WI019 independently reports87/87
Java tests PASS, no source defects; PG read that Evidence Pack before final
delivery and did not duplicate those executions.

## Next Step and Limits

MA should route bounded validation corrections to SA, then obtain PG rerun and
updated hashes. After WI018 and WI019 clear, proceed to WI020 and then WI021.
This is not a demand to finish unrelated production gates.

Keep the false prestart gate. Linux syntax/BPF/IPv4/IPv6/relay proof, actual
Spring binding, real secrets, startup and cleanup are separately NOT RUN.
OFF flags are not a filesystem/DB read-only or universal network guarantee.
Historical JAR/manifest pins match; no build/install/commit/push/deletion.

## Related Documents

- [PG Evidence Pack](../agent/WI-20261011-ATS-018-evidence-pack.md): Exact findings and hashes.
- [REQ002](REQ-20261011-ATS-002.md): Approval and dependent WIs.

## 2026-10-11 Superseding Independent Retest

**LOCAL PASS. PG018-01/02 closed; WI020 local packaging is cleared by PG.**
The original HOLD above is preserved as historical evidence of the old checker.

PG independently reran the repaired owner suite19/19 and unchanged red-team
harness27/27, with no failures/errors/skips. All8 formerly accepted unsafe
mutations now reject. Reviewed checker SHA-256:
`43f48bf958cbb9221e70fe66af28caad128a4aafb5350e9ab8f566c5c0d535a5`.
The other18 original public pins match, including candidate unit/env, Java,
PG harness and historical artifacts. No candidate or application source edit.

With WI019's already completed87/87, MA can dispatch WI020, then WI021. Java
tests were not duplicated. Actual Linux/network/runtime/secret/startup/cleanup
gates remain separately NOT RUN; the false prestart gate stays intact. No
remote operator, app/DB launch, install, commit/push or deletion was performed.

See the [independent retest addendum](../agent/WI-20261011-ATS-018-evidence-pack.md#2026-10-11-independent-repair-retest-local-pass)
for exact commands, hashes, finding closure and verification limits.

