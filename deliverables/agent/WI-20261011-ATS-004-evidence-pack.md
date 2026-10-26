---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261011-ATS-004
dependencies:
  - path: WI-20261011-ATS-004-handoff.md
    reason: Existing skill-generated review and two-file write scope
  - path: WI-20261011-ATS-002-evidence-pack.md
    reason: Exact implementation and corrected-test independent review
  - path: WI-20261011-ATS-003-evidence-pack.md
    reason: Native corrected tests and historical safe closure
  - path: ../user/REQ-20261010-ATS-010.md
    reason: Restricted operation with latest user-approved NS exception
---

# Evidence Pack: WI-20261011-ATS-004

> Purpose: Decide the bounded bootstrap/staging resumption without operating the target.

## Summary / Verdict

TL;DR: **PASS for unchanged bootstrap reactivation and the exact no-email
staging register/dry-run sequence below; no concrete blocker.** This releases
the review prerequisite only. PG made no target connection, browser action,
CA request, installation, secret read, implementation edit, Git write or
delegation. Native observations are attributed to MA, not independently replayed.

The direct latest user approval and saved WI004 handoff permit the limited
attempt while preserving the unresolved child-apex NS issue. The earlier
REQ/current-entry DNS-first HOLD and WI003 closure describe the previous window.
They are not rewritten or treated as present-state observations. The original
implementation review remains applicable because all seven candidate hashes
match, including the corrected test file; no full source audit was repeated.

## Scope / DoD Check

- [x] Verified and read the existing WI004 handoff before processing evidence.
- [x] Confirmed re-injected Tier 0, PG/security and task context.
- [x] Reviewed exception/consent, native-help and existing-account boundaries.
- [x] Reconciled fresh MA receipts with retained artifacts and prior safe closure.
- [x] Reviewed exact staging commands, bounded attempts and preserving recovery.
- [x] Wrote only WI004 summary/evidence using `apply_patch`.
- [x] Separated review PASS from production, TLS, renewal and final cleanup proof.

## Reference Documents (Tier 0-2)

Extracted from the saved handoff INPUT POINTERS; no descendant was invoked.

| Tier | Injected document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` (STD-001) | Approval and explicit action scope |
| 0 | `docs/standards/development-standards.md` (STD-002) | Reproducible scoped review |
| 0 | `docs/standards/documentation-standards.md` (STD-004) | Two-set metadata and history preservation |
| 0 | `docs/standards/glossary.md` (STD-005) | WI/REQ/PG terminology |
| 0, per handoff | `docs/standards/evidence-pack-standard.md` | Evidence pointers and boundaries |
| 1 | `.claude/agents/pg.md` | Independent security review role |
| 1 | `docs/policies/security-policy.md` | Key/account/log and private-value protection |
| 1 | `docs/policies/access-control-policy.md` | MA-only runtime operations |
| 1 | `docs/policies/quality-gates.md` | Scope, risks and reproducibility |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Handoff precondition and saved output |
| 2 | `docs/templates/eval-report-template.md` | Bounded evaluation |
| 2 | `deploy/single-vps/runbooks/domain-tls.md` | Retained bootstrap, no-email option, TLS and recovery |

The four constitutional Tier 0 documents remain STD-001/002/004/005. PG required
tiers `[0, 1]` and ATS tag were established from the public workspace/injection
configuration in WI002. The injected test skill remains available, but no suite
rerun was needed for this unchanged-source/procedure-only review.

Context read: current REQ010, WI002 identities/correction, WI003 receipt ledger
and closure, WI004 handoff and `docs/index.md:20-59`. Latest direct MA/user updates
provide the new exception and fresh operational receipts below. Memory search
found no task-specific entry and supplied no decision evidence. No online refresh
or private operational file was accessed by PG.

## Fresh MA Receipts / Preconditions

All dates are 2026-10-11 KST. Times are only those actually supplied; later
updates without timestamps are not assigned invented execution times.

| MA receipt | Review interpretation |
|---|---|
| 01:00:47+ fresh pinned SSH/sudo exit 0, same boot, recovery root tty1 count 1, sole expected key and newly approved temporary window, source matches prior provider /32 | Current access/recovery receipt replaces previous-window assumptions. Private identifiers are omitted. |
| All expected masks/inactive, no jobs/app/DB/web listeners, Certbot 2.9, free 92241 MiB | Opening containment confirmed by MA; no reinstall or capacity guarantee inferred. |
| 01:00:58 both authorities A/SOA match; apex NS AA/NODATA persists, AAAA/CAA AA/NODATA, parent delegation correct and DS NODATA | Same approved unresolved anomaly; no guessed DNS repair. |
| Both ACME directories show unchanged approved LE v1.8; installed help confirms all flags in the final proposed commands | Target-version/consent prerequisites supported by MA, not a PG local Certbot invocation. |
| Initial assumed root-stage render absent; MA stopped before comparison, then found retained r2 renders | Corrected path assumption, not source drift. Retain both earlier stages; no recreate/overwrite. |
| r2 exact seven source hashes; bootstrap/install and drop-in/source comparison PASS; native nginx syntax PASS; sole reviewed drop-in and standard vendor unit | Retained configuration can be reused after these gates. Private rendered hashes supplied to PG are not copied into tracked evidence. |
| TLS render source allow /32 matches current source; 22 exact path metadata/xattr checks PASS; only public marker/token in served tree | MA-reported containment; no getfacl install needed or performed. |
| Known root-owned Certbot directory/packaged cli.ini with only the two reported package defaults; no other config/hooks/renewal/accounts/live/work/log | No conflicting server/authenticator/hook setting reported. No account or private body was read by PG. |
| New action-time firewall approval, then 01:04:56 bootstrap active IPv4 :80 only; loopback marker 200, API/root 404, DB off/app absent | Bootstrap activation is an MA receipt, not a PG operation or CA success. |
| Latest update: firewall save and ten external HTTP probes PASS; umask 077/root-private account/work/log handling before CA action | Immediate staging prerequisites reported satisfied. No CA result was supplied at the review cutoff. |

Continue to keep MySQL masked/inactive, app absent/off, Certbot service/timer
runtime-masked/inactive and without timer enablement. Nginx must use only the
reviewed quarantine config: IPv4 HTTP ACME tokens, later HTTPS only the exact
approved /32 marker; no app/storage proxy, IPv6 opening or broad 443 rule.
This resume does not replay install/mkdir/overwrite sections for retained files.

## Exact Staging Commands / Security Review

These are the user's final proposed invocations, reviewed after MA confirmed
native 2.9 help for all flags. Run separately, not as an unattended block.
Registration is the deliberate first CA-writing boundary, not a read-only test.
The no-email warning flag names an explicit supported mode, not a bypass of CA
domain validation. `--agree-tos` relies on the already-approved agreement and
fresh comparison; it does not itself pin an agreement version.

### 1. Register Once When No Staging Account Exists

```bash
/usr/bin/certbot register --non-interactive --agree-tos --register-unsafely-without-email --no-eff-email --server https://acme-staging-v02.api.letsencrypt.org/directory
```

This intentionally creates the staging account and local key/log state, without
a contact address or marketing consent. It requests no domain certificate.
`register` is account/endpoint-scoped, so no domain or webroot argument is needed
here. Require exit 0, an unambiguous successful registration receipt and exactly
one verified account at that same endpoint before continuing. Keep identifiers,
keys, account bodies and raw logs private.

If exactly one previously created, approved no-email staging account is already
known from a successful receipt, skip registration and reuse it. An unexpected
account, multiple accounts, failed/ambiguous registration or changed terms is
HOLD: do not delete, overwrite, re-register, change directories, guess `--account`
or silently accept a new agreement. The supplied fresh state reports no accounts.

### 2. Dry-Run Only After That Account Is Verified

```bash
/usr/bin/certbot certonly --non-interactive --dry-run --webroot --webroot-path /var/lib/atstudio-tls-quarantine/acme --preferred-challenges http --cert-name atmusic.co.kr -d atmusic.co.kr --server https://acme-staging-v02.api.letsencrypt.org/directory
```

The fixed staging endpoint, HTTP-01 webroot, exact single apex and certificate
name preserve scope. No installer/plugin changes Nginx; no wildcard/www, alternate
server, forced renewal, deploy-hook execution or production certificate install
is requested. The absent `--no-eff-email` on this second command is not a blocker:
the already-verified account is reused, with no intended registration/contact
change. Do not add registration flags as a fallback if account selection fails.
Do not rely on Certbot dry-run's implicit-registration behavior to satisfy the
explicit account gate; require the known account immediately before this command.

Require exit 0 and the actual successful staging validation result, not merely
the earlier synthetic HTTP probe. Dry-run performs real CA validation and local
challenge/log activity; it is not read-only and does not prove production trust,
installed certificates, HTTPS access, deploy-hook behavior or future renewal.
Keep normal Certbot challenge cleanup; no manual deletion of retained evidence.

### Bounds and Later Stages

One planned registration when absent, then one staging dry-run; no blind or
continuous retries. Existing ten external probes are sufficient for this review;
do not restart a broad probe/audit campaign. Retain the runbook's bounded probe
set (at most 24 synthetic HTTP/TLS requests) and fail closed on unexpected success.
Any failed, interrupted or ambiguous CA command is HOLD with preserved state.

Production is a later distinct stage, only after staging PASS and its own exact
invocation/action-time checks. Prior v1.8 no-email consent covers that scope when
unchanged, but these two staging commands do not perform or validate production
issuance. Continue the reviewed lineage/key-pair/trust, native TLS, allowed/denied
Host/SNI/path/source, renewal-hook/dry-run, scheduler/boot intent and cleanup
gates. No staging certificate is served and no validation bypass is permitted.

## DNS Finding and Approval Boundary

The strong observation is repeated authoritative child-apex NS AA/NOERROR/NODATA
despite correct parent delegation and current A/SOA responses. Neither ordinary
recursive-cache propagation nor an initial nslookup PTR refusal explains the
separately verified direct NS responses. Provider internal generation/sync delay
is possible but unconfirmed; there is no verified application SLA, and SOA
refresh 3600 is not one. No exact provider fault or future resolver behavior is
asserted. Optional own-nameserver glue registration is not a demonstrated missing
setup step for the supplied provider nameservers.

The user now explicitly accepts tracking this anomaly while trying restricted
HTTPS. Ordinary staging validation may proceed; CA validation failure remains
HOLD and must not be bypassed. Success does not repair or close DNS, prove
unattended renewal reliability, authorize public app/DB access or remove the
existing monitoring gap. No provider inquiry or NS/DS/CAA/glue change by PG.

## Public Candidate Identities

Recomputed locally during WI004: seven of seven match WI002's current candidate.

| Public file | SHA-256 |
|---|---|
| `scripts/deployment/tls-quarantine.py` | `723ed64dbbb48ca1a729dce7684498f6ff74fb6c85f0167e11e046783d761ace` |
| `scripts/deployment/test_tls_quarantine.py` | `a9ca1406ef209a9eb67406dbf14c889ca85d5c5eab01274933b1cd78bcf56436` |
| `deploy/single-vps/tls-quarantine/nginx.conf.template` | `ebc0ae231780ae2b16e82cf571cd144dd1ae7e2bdd9cb9d4ff564ad0c4a527ea` |
| `deploy/single-vps/tls-quarantine/https.conf.template` | `4fe91f83763303600149c532591b59ac043e28edc25dbdf395bc748ff83e42cb` |
| `deploy/single-vps/tls-quarantine/nginx-service.conf` | `cecaa38ef49f92518e6c5a776cedc7c03cecb54764afd1455b8a404f13161b93` |
| `deploy/single-vps/tls-quarantine/reload-hook.py` | `4c9d7ab410e6fa34597827fa679cc1e434f2cef108a9a7043edd32ab9f9104f0` |
| `deploy/single-vps/runbooks/domain-tls.md` | `ff9405ec71a39fe619780570b49f011773b001ab876b795827e34da00c0f8ab7` |

Input identities at read: WI002 evidence
`3d6e25482f7cbde1f32edc0f786bf529ffabba602f1526e0acfecb64f512b899`;
WI003 evidence `8e85a6cad7e9101b853bc7c87343f21da59d0b2c7eaa38e3965d8c70eaa61469`;
WI004 handoff `293d560e4323ae055ca1cf56c4210db645b564cc18b77e5d142db3b17315e220`.
Private render hashes and exact management/host values remain operational evidence
outside this report. No on-disk private render was read by PG.

## Commands / Scoped Validation

Actual PG actions: local `Test-Path`, `Get-Content`, scoped `rg`, `Get-FileHash`,
Git read-only status/HEAD and clock checks, then `apply_patch` for the two outputs.
An initial rg path wildcard failed on Windows and was rerun with `-g`; this was
a local lookup error, not a candidate failure. No remote or Certbot command above
was executed by PG.

Local review environment: Windows/PowerShell, shared dirty `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`; clock sample 00:59:29+09:00,
2026-10-11, followed by the timestamped MA updates above. Dirty work preserved.
Final output checks cover metadata, relative local links, ASCII/LF/final newline,
trailing whitespace and exact presence of both proposed command strings.

Tests this WI: **N/A, no implementation/test change**. Reuse the already recorded
PG Windows 36/36 and MA Ubuntu 3.12 36/36 corrected-suite PASS from WI002/WI003.
No new coverage, native compatibility execution, DNS probe, TLS acceptance or
runtime test is claimed by PG. Native help and fresh preconditions are MA receipts.
No broad repository validator, network research or source re-audit was performed.

## Risks / Rollback / WI Chain

PG changed only `deliverables/user/WI-20261011-ATS-004-summary.md` and this Evidence
Pack. No local runtime rollback is needed. MA retains earlier stages, installed
packages, DNS, configs and any newly created accounts/certificates and receipts.
On failure use runbook section 6: close only this resumed window's new provider
80/443 rules, preserve SSH/egress, stop window-owned scheduling if any, quiesce
Nginx and verify inactive/no jobs/no listeners before remasking. Keep MySQL
masked/app off; no unknown process kill, deletion, default-site restart or
automatic DNS rollback. Certbot runtime masks are not persistent reboot fencing.
Complete only this window's exact-key removal, empty-agent verification,
Stopped/Disabled restoration and session/recovery logout with fresh receipts.

WI004 review is complete. MA immediately triggers WI005 through the handoff
skill, with actual issuance/TLS/renewal/cleanup results required for its final
integration. No descendant is launched by PG. Evaluation: reuse retained exact
bytes and fresh receipts; preserve the distinction between approved risk,
observed DNS anomaly, successful staging and operational readiness. No new
architecture, library, policy or implementation control is proposed.

## Related Documents

- [Summary](../user/WI-20261011-ATS-004-summary.md): Bounded verdict.
- [Handoff](WI-20261011-ATS-004-handoff.md): Approved assignment and latest exception.
- [WI002](WI-20261011-ATS-002-evidence-pack.md): Exact source and corrected-test review.
- [WI003](WI-20261011-ATS-003-evidence-pack.md): Historical native checks and closure.
- [Runbook](../../deploy/single-vps/runbooks/domain-tls.md): Existing isolation and preserving recovery.
