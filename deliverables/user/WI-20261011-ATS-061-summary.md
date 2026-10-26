---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-061-evidence-pack.md
    reason: Independent findings, tests and exact review boundary
  - path: REQ-20261011-ATS-007.md
    reason: Approved bounded repair and execution gates
---

# WI-20261011-ATS-061 Summary

**ALLOW_ONE_BOUNDED_READ_ONLY_TRIAL** for the exact final WI060 candidate.

- Freeze: `e0896db9fce85f9a1d802b2d85e412c371f3d447805f8b473c6171c85da31d7a`
- Bundle: `3f9edd75b5ebc32281cc651d45fe16861a2dd2a19bc8c5dd5deac24494cc4316`
- Recipe: `2f230fcf6c724ff2df89c9d80e7097987ba8c9abbcee750a1dd1669e40e519bd`

PG independently reran9 local groups, all PASS: original regressions8,
requested checks5, candidate141, PG schema47, PowerShell38, step schema53,
SelfTest8, transport8 and HTTP18. Counts overlap, not additive coverage.
Pre/post identity matched81 candidate files,12 runtime files and161 historical
files. Review window2026-10-11 21:19:41-21:20:02 KST.

F061-01/02 are closed: final comparisons run independently on all attempted-grant
exits, and POSSIBLE/RELEASED labels preserve grant-publication uncertainty.
Historical PG056 attribution is corrected. No new blocking finding remains in
this reviewed scope. [Final receipt](../../build/WI061-review/final-receipt.json).

This permits only the fresh MA management-only read-only trial after existing
current target/boot/firewall/maintenance/recovery gates. Changed bytes invalidate
this verdict. Native remains NOT_RUN by PG. SMTP, writes, public access and a
separate cause probe are excluded. Provider587 approval/save is a separate
MA-reported fact, not SMTP activation in this trial. WI058 remains HOLD with
unknown cause; local testing is not native success. MA owns trial/cleanup;
WI062 records those future receipts. REQ007 remains active.

## Interim Review (Historical)

The initial findings below are retained; the final same-byte result above
supersedes their open status.

**Interim HOLD. SE060 final-ready and final freeze are still pending.**
Independent synthetic tests:8 run,6 passed,2 failed.

- F061-01: after a grant is published, POST_GRANT BPF failure skips the final
  independent DB observation. Exact BPF drift still causes HOLD.
- F061-02: a failed POST_GRANT snapshot leaves app_start ATTEMPTED even though
  Java admission was released. This is not proof that Java actually started.
- Preserve WI056 attribution for inherited findings; the unfinished WI060 README
  must not label historical F1/F3 as new PG061 review results.

The overlay/reload move and independent POST_VIEW group checks are present;
their native effect and the historical BPF cause remain unproven. Final ALLOW
requires repaired errors, final freeze/pins, and independent tests of those bytes.

MA-requested focused checks passed:5 Python methods and47 PowerShell assertions
cover false-PASS rejection, config-test/marker-before-overlay ordering, and
independent pregrant cleanup with original-Nginx restart gates. They do not
supersede the two post-grant findings or prove target crash behavior.

Only PG-owned tests/logs and these reports were written. No native operation,
credential access, candidate/source edit, subdelegation, commit or push occurred.
MA-owned firewall, SSH/recovery and identity receipts are recorded as observations,
not PG verification or runtime acceptance. Candidate identity is WI060, not WI066.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-061-evidence-pack.md)
- [Handoff](../agent/WI-20261011-ATS-061-handoff.md)
- [Approved REQ007](REQ-20261011-ATS-007.md)
