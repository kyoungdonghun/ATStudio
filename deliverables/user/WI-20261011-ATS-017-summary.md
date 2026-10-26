---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-002.md
    reason: Approved local-only preparation
  - path: ../agent/WI-20261011-ATS-017-evidence-pack.md
    reason: Reproducible local evidence and limitations
---

# WI-20261011-ATS-017 Summary

> TL;DR: Local first-start candidate prepared; 12/12 offline tests PASS. Nothing installed, started or exposed.

## Result

- Separate [candidate unit](../../deploy/single-vps/first-start/atstudio-first-start.service.candidate) with unconditional false prestart gate, unresolved new-JAR/private-file paths, no install section, loopback binding, no restart and finite180s active runtime.
- [Public overlay](../../deploy/single-vps/first-start/first-start-public.env.example) plus explicit CLI overrides keeps master background work, audio, password auth, acceptance/test users and notifications OFF. Provider credentials are blank and SMTP port is numeric even when lower configuration contains a placeholder.
- Strict storage/JWT/billing-keyring/DDL guards remain required. Existing unit resource limits are reused; no new production sizing claim.
- [Finite runbook](../../deploy/single-vps/first-start/README.md) records approval, same-unit external IPv4/IPv6 deny and loopback proof BEFORE Java, bounded GETs, stop/remask cleanup and unchanged TLS.
- [Source inventory](../../deploy/single-vps/first-start/inventory.md) distinguishes scheduled jobs, readiness listeners, independent audio executor, request-driven effects, mail/OAuth/Toss and startup filesystem/DB effects.

## Recommendation and Alternatives

Use this separate candidate only after independent review and new target approval.
Keep HOLD if network/relay proof, exact existing secret-use approval or new-JAR
identity is missing. A separately reviewed network namespace is an alternative
if IP filtering/relay controls cannot meet the boundary; none is implemented.

## Verification and Limits

Verification owner SA; date2026-10-11; environment Windows/PowerShell, local public
source and synthetic values only. `python -B deploy/single-vps/first-start/test_candidate.py`:
12 tests,12 passed,0 failed,0 skipped. Tests include missing/re-enabled OFF flags,
poisoned inherited credentials/SMTP, new-JAR placeholder, guard preservation,
loopback address shape, resource parity and finite-stop settings. They do not
execute systemd, Spring, Java, a database or any network call.

Prior deployment files, old JAR and WI026 manifest hashes unchanged. No existing
template, source, contract, historical checker or manifest was edited by SA.
WI020 new backend path is planned under `build/WI020-release/`; exact path/hash
must come from its new artifact manifest, never from WI026 or a silent fallback.

Linux syntax/runtime/network proof, real secret binding, first application start
and cleanup: **NOT RUN**. IP filtering may be unsupported and loopback/Unix
relays can bypass the intended boundary. No first-start read-only guarantee.
No remote operator, secret read, DB service action, package install, commit/push,
deletion, Nginx upstream or TLS modification.

## Next and Rollback

WI017 local scope complete; MA must trigger **WI018** after WI016 is available.
WI018/019 gate WI020's build; WI021 integrates the result. REQ remains open.
Runtime proof/secret use/app start requires a later exact approval, not this WI.

Local rollback proposal: retain as superseded evidence or request deletion of
only WI017-created files. Runtime cleanup requires exact-unit stop and restoration
of MySQL inactive/persistently masked plus no ports/jobs and unchanged TLS. No
rollback or deletion has been executed.

## Related Documents

- [WI017 Evidence Pack](../agent/WI-20261011-ATS-017-evidence-pack.md): Evidence and commands.
- [WI017 handoff](../agent/WI-20261011-ATS-017-handoff.md): Original scope, unchanged.
- [REQ002](REQ-20261011-ATS-002.md): Scope and WI chain.

## 2026-10-11 Validation Repair Addendum

**Owner repair complete; WI018 independent re-review pending.** Earlier12-test
PASS is historical and did not catch PG018-01/02. SA reproduced the8 unsafe
survivors, then changed only the public candidate checker and appended these
two deliverable updates under MA's approved repair packet.

The checker preserves directive occurrences, permits only the exact ordered
two EnvironmentFiles, rejects other duplicates and unapproved hooks, and pins
the exact Java argument shape, SMTP host/port and storage/temp paths. No unit,
environment example, README/inventory, Java source or PG harness was changed.

- Expanded regression: **19/19 PASS**,0 failures/errors/skips.
- Unchanged PG harness: **27/27 PASS**; all26 mutations rejected, including the8 former survivors.
- Protected before/after hashes:13/13 matched, including old JAR and WI026 manifest.
- Repaired checker SHA-256: `43f48bf958cbb9221e70fe66af28caad128a4aafb5350e9ab8f566c5c0d535a5`.

Verification: SA,2026-10-11, Windows/local synthetic input only. No remote/native
work, secret use, application/DB start, dependency install or scope expansion.
Linux/network proof remains NOT RUN. MA should return this hash to WI018 for
independent re-review; WI020 remains gated by PG clearance and WI019 PASS.
See the [repair evidence](../agent/WI-20261011-ATS-017-evidence-pack.md#2026-10-11-approved-validation-repair-addendum)
for reproducible commands, old/new pins and rollback limitations.
