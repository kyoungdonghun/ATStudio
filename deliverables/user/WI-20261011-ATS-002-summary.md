---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-010.md
    reason: Approved bounded quarantine TLS scope
  - path: ../agent/WI-20261011-ATS-002-handoff.md
    reason: Independent review assignment and ownership limits
  - path: ../agent/WI-20261011-ATS-002-evidence-pack.md
    reason: Exact reviewed identities, test evidence and native gates
---

# WI-20261011-ATS-002 Summary

> Purpose: Decide whether the exact WI001 candidate may enter MA's gated native sequence.

## Verdict

TL;DR: **PASS for the reviewed local candidate. No concrete blocking finding.**
Independent PG rerun: 35 tests passed, zero failures/errors/skips, 0.281s,
exit 0 on Windows/PowerShell/Python 3.14.3, 2026-10-11 KST.
All eight identities published by WI001 match; its Evidence Pack was additionally
hashed. The full nine-file SHA-256 table is in the WI002 Evidence Pack.

This releases only the independent-review prerequisite for MA's already-approved
sequence, with the operational clarification below. It is not native Nginx/TLS
success, CA agreement/contact consent, unattended-renewal proof, application
approval or REQ completion. Changed candidate bytes require focused re-review.

## Review and Operational Clarifications

- HTTP is limited to isolated ACME token files; HTTPS has only the source-restricted
  static marker. Host/SNI/raw-path guards, access-phase ACL, IPv4 listeners and no
  app/storage/include graph were reviewed. No early successful return bypasses ACL.
- The hook checks exact event/lineage, protected metadata and archive links,
  matching generations, SAN, trust and native configuration/key-pair validation
  before reload. A reload request is correctly distinguished from served TLS.
- **Scoped apt confirmation accepted:** after the exact seven package versions,
  signed origins, hashes, maintainer scripts and masks are checked, and the final
  plan still has no additional packages, upgrades or removals, MA may use
  `apt-get --assume-yes --no-install-recommends install <exact-version-set>`.
  This explicit confirmation replaces the interactive-input wording in runbook
  lines 83-87 for this window only; native input is not mandatory. Any plan drift
  remains HOLD. This is not approval of unreviewed package bytes or other changes.
- No `getfacl` installation is required. Successful checks for
  `system.posix_acl_access` and `system.posix_acl_default` on exact public targets
  satisfy the existing ACL observation gate when both are absent. An inspection
  error is not absence. Check newly created targets through the same existing gate.

## Remaining Native Gates

1. MA retains fresh target/source/recovery and DB/app closure evidence, exact
   source/transfer/render identities, package checks and service/timer masks.
2. Native Certbot 2.9 help, candidate `nginx -t`, effective unit selection and
   IPv4-only sockets; exact DNS/provider rules and external synthetic HTTP checks.
3. Explicit action-time CA agreement/contact or no-contact consent before any
   staging registration; staging success before production issuance.
4. Protected lineage/key-pair/trust and allowed/denied Host/SNI/path/source checks;
   independent non-allowlisted network denial, renewal dry-run, hook receipt and
   trusted served-certificate verification. Mocks do not close these checks.
5. Record scheduler/continuing-boot intent and the existing alerting gap; complete
   exact-key/empty-agent/Stopped-Disabled and session cleanup. MA triggers WI003
   through the handoff skill; DocOps integrates actual execution/cleanup receipts.

MA's new package, module, recovery, path/xattr and firewall observations are
attributed in the Evidence Pack, not independently measured by PG. PG made no
remote/browser/network/private-file/install/runtime/Git changes or delegation.
Only this summary and the WI002 Evidence Pack were created; dirty work preserved.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-002-evidence-pack.md): Exact hashes and review anchors.
- [Operator runbook](../../deploy/single-vps/runbooks/domain-tls.md): Staged checks and preserving rollback.

## 2026-10-11 Test Isolation Correction Review

TL;DR: **PASS for the corrected test-only delta; native application remains HOLD
pending MA's fresh-stage Ubuntu/Python 3.12 rerun.** This addendum supersedes the
earlier current-PASS/guard-isolation assurance while preserving its historical
record. MA reported the original exact-hash native run as 35 total, 34 passed,
one error: real `check_path` raised `FileNotFoundError` before the intended
`check_lineage` failure. The Windows PASS had accepted an incidental `GateError`;
it did not prove that the intended guard was reached. No native changes beyond
protected public source staging were reported at that failure checkpoint.

After explicit approval, PG reviewed the SE test-only correction and independently
ran the same focused unittest command: **36/36 PASS, zero failures/errors/skips,
0.271s, exit 0**, local Windows/Python 3.14.3, 2026-10-11 KST.

- All non-target guards are mocked; the named target must be called once with
  exact arguments and raise exactly `GUARD`. SAN/native/filesystem calls remain
  absent. A separate mocked missing-filesystem case checks early refusal.
- No new blocking finding in the test delta. An in-memory reconstruction matches
  the original test hash, proving no unrelated test edit; six non-test source
  hashes remain unchanged. No implementation edits by PG or claimed native rerun.

Corrected test SHA-256:
`a9ca1406ef209a9eb67406dbf14c889ca85d5c5eab01274933b1cd78bcf56436`.

MA must retain the failed stage, verify the corrected hash in a fresh stage with
the same implementation, and supply the Ubuntu 3.12 36-test receipt before the
native-application HOLD can be closed. Existing CA consent, runtime/exposure,
renewal and cleanup gates remain unchanged. See the Evidence Pack addendum for
exact changed lines, source identities and reproduction.
