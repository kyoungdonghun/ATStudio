---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261010-ATS-020-handoff.md
    reason: Approved independent review scope
  - path: ../agent/WI-20261010-ATS-020-evidence-pack.md
    reason: Findings, exact reviewed hashes and reproducible checks
  - path: REQ-20261010-ATS-007.md
    reason: Existing host authorization and exclusions
---

# WI-20261010-ATS-020 Review Summary

> TL;DR: Local review PASS for the corrected package. Staging and administrator
> setup may proceed under the approved native gates. SSH restriction requires
> fresh administrator acceptance first. No open local blocking finding remains;
> no remote application or production readiness is certified.

## Findings

**P1, resolved in source: incorrect journal drop-in ordering.** The initial
runbook selected `99-atstudio.conf`, which sorts before vendor `syslog.conf`.
Given the reported vendor `ForwardToSyslog=yes`, the intended `no` would lose.
MA identified the concern; PG independently confirmed the ordering and applicable
Ubuntu rule. SE changed the destination, installation and rollback to
`zz-atstudio.conf`, added all-four-directory effective-order gates and a regression
assertion, and appended correction evidence. PG read and retested that correction.
The journal template bytes did not change. This is source closure only; native
merged-policy and restart acceptance remain mandatory.

## Per-Stage Disposition

APPLY below means permission within the already approved REQ and listed gates,
not an instruction to bypass a failed check or a claim that application occurred.

| Stage | Local verdict | Required MA gate |
|---|---|---|
| Root-only staging | PASS / APPLY | Fresh absent destination; real trusted ancestry; exact reviewed/transferred SHA-256; public key only |
| atadmin and sudo | PASS / APPLY | Approved single key; locked password; home/key ownership; candidate and installed visudo; preserve root/console |
| SSH restriction | PASS / conditional APPLY | New non-multiplexed atadmin key login, sudo and PTY; authenticated console and retained original session; native syntax and actual staged/installed contexts equal before reload |
| Service identity/directories | PASS / conditional APPLY | Linux/root absence check, NSS/mount/ACL/exclusive-maintenance gates; exact after-check and cross-identity access checks |
| MySQL | PASS / conditional APPLY | Exact include/override review, two candidate validators, bounded internal-only probe; query/listener acceptance; confirmed stop and exact remask even on failure |
| Journal | Corrected PASS / conditional APPLY | New zz-atstudio.conf; all four directories and actual merged order/values; retention-effects acknowledgment; restart/diagnostics/synthetic receipt |

Any unmet native gate is HOLD for that stage. After SSH reload require a fresh
administrator connection with sudo/PTY, denial of root using the known valid key,
and a publickey-only server method offer. Preserve root's local password status P.
Do not infer password disablement from an unauthenticated client failure alone.

## Verification and Attribution

- PG, 2026-10-10, local Windows/PowerShell, Python 3.14.3: 30/30 focused tests
  passed before and after correction; corrected run 0.040s, no failures/errors/skips.
- Ten corrected-runbook Bash blocks parsed successfully without execution.
- Direct checker invocation on Windows rejected inspection with native exit 2.
- Three in-memory last-scalar examples reproduced the old failure, corrected
  known-vendor ordering, and a hypothetical later override. Not systemd execution.
- Exact final hashes are in the [Evidence Pack](../agent/WI-20261010-ATS-020-evidence-pack.md).
- MA reports authenticated root recovery console, unchanged root password status P,
  retained original root PTY, successful strict-pinned root SSH and completed fresh
  baseline checks. At the last supplied update no remote configuration was changed.
  PG did not connect to or independently observe the host.

No application tests, actual Linux daemon validation, load/capacity, seven-day
retention, backup/restore, Provider, payment, mail or production validation ran.
No source/configuration edit, secret access, purchase, remote operation, destructive
action, commit, push or descendant delegation was performed by PG.

## Recovery and Next WI

Retain authenticated recovery throughout. Withdraw only exact newly installed
files with matching hashes to fresh root staging names; validate before reload.
Leave partial accounts/directories for review. Confirm MySQL stop before remask;
an elapsed timeout alone is not cleanup. Journal rollback cannot recover aged logs.
MA must finish exact-key removal and Windows agent restoration with actual receipts.

WI020 is complete. **Immediately trigger WI021 through MA's skill-generated
handoff and delegation**, preserving this local review separately from later MA
application receipts. Do not wait for remote application to close WI020. REQ007
remains active; domain purchase before DNS/HTTPS and other deployment gates remain.

## Related Documents

- [Reviewed runbook](../../deploy/single-vps/runbooks/host-hardening.md)
- [WI019 correction evidence](../agent/WI-20261010-ATS-019-evidence-pack.md)
- [WI020 Evidence Pack](../agent/WI-20261010-ATS-020-evidence-pack.md)
- [REQ007](REQ-20261010-ATS-007.md)

## 2026-10-10 MySQL Validation-Path Addendum

PG local review passes the narrow isolated staging-path correction requested by
MA within the user's already approved server-configuration scope. This is not a
new human approval or an expansion of authorization. The review uses MA's reported
enforced AppArmor read denial for the original `/root/wi019` candidate and the
permitted `/etc/mysql/**` read path. Use a new, non-included
`/etc/mysql/atstudio-validation.cnf`, exact reviewed bytes, root:root0644, after
absence/type/ancestry checks. Validate as mysql before final installation, then
validate the actual service include chain. Both gates precede the bounded DB probe.

Preserve original and isolated candidates; no AppArmor or `/root` permission
change, deletion or DB start is authorized by this path review alone. MA owns
native evidence; PG did not connect to the host. The
[Evidence Pack addendum](../agent/WI-20261010-ATS-020-evidence-pack.md) records the
exact hash, command sequence, HOLD conditions and unchanged stop/remask boundary.

MA subsequently reports standalone mysql-user, explicit actual-include and
default-service-provenance validators all rc0, identical staged/final bytes and
no AppArmor change. DB has not yet started. PG did not independently execute or
observe those validations; runtime acceptance and stop/remask gates remain.

## 2026-10-10 MySQL Client Compatibility Addendum

PG focused local review PASS: remove the installed client's unsupported
`--no-login-paths` only with MA's confirmed absence of `/root/.mylogin.cnf` and
`/root/.my.cnf`, renewed immediately before use. Remove `MYSQL_PWD`, `MYSQL_HOME`
and `MYSQL_TEST_LOGIN_FILE` from the final client environment; set HOME=/root.
Retain `--no-defaults` first, explicit socket/protocol/root user, the existing
fixed query,10s timeout and mandatory stop/remask/no queued job/no listener checks.
This is MA-requested compatibility work within existing approval, not new human
approval or authorization expansion. No credential reads or auth changes.

MA reports native server validation/start and restricted listeners passed, but
the old client command failed rc2 on the unsupported flag. MA confirmed fail-closed
stop/remask with no queued job or listeners. The corrected query remains pending;
PG did not execute it or independently observe the host. The Evidence Pack adds
the exact revised command and preserves all prior evidence and historical hashes.

## 2026-10-10 Final Source Reconciliation

PG local source reconciliation PASS: the latest runbook SHA-256
`43755E6F58EF9543EB6DFDCE0F81C7DAF816D93C4FACA1B4125C2BE3B80F1E1A`
and candidate-test SHA-256
`BD24759B6F75BD463D43DF36E0A0388A4CEE6C1541EDCF38486D72B138F42915`
match the reported identities and implement the already reviewed path/client
corrections. No broader review, test rerun or remote verification was performed.

MA reports probe `e741a6` live read-only values and stop/remask PASS, all remote
operations finished, temporary agent keys removed, Windows agent Stopped/Disabled
and noVNC logout. Those are MA-owned receipts, not PG validation; DOCOPS is
integrating them. The Evidence Pack records the source pointers and unchanged
boundaries. Earlier pending observations remain historical; no new authorization
or production acceptance is implied.
