---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-010.md
    reason: Restricted TLS scope and latest explicit user exception
  - path: ../agent/WI-20261011-ATS-004-handoff.md
    reason: Skill-generated resumption review assignment
  - path: ../agent/WI-20261011-ATS-004-evidence-pack.md
    reason: Exact commands, evidence and remaining boundaries
---

# WI-20261011-ATS-004 Summary

> Purpose: Review restricted TLS resumption and the exact no-email staging sequence.

## Verdict

TL;DR: **PASS for unchanged quarantine bootstrap resumption and the two exact
Certbot 2.9 staging commands in the Evidence Pack. No concrete blocking finding.**
This is a procedural/security review, not a CA execution or HTTPS success receipt.
The user's latest explicit unresolved-NS exception supersedes the earlier
DNS-resolution-first decision for this bounded attempt only. Earlier HOLD and
safe closure remain historical facts, not mistakes or current runtime evidence.

MA supplied fresh approved SSH-window/target/source/recovery, masks, exact seven
source hashes, retained r2 render/install comparison, native parsing/effective
unit, metadata and installed-help receipts. The r2 path correction resolved an
earlier assumed path; no candidate change or reinstall was needed. MA subsequently
reported bootstrap IPv4 HTTP-only activation, approved firewall save and ten
external HTTP probes PASS. PG did not repeat remote checks or operate the target.

## Staging Boundary

- Existing consent covers LE v1.8 and no-email staging/production/renewal; MA
  confirmed both directory agreements still match. The fresh NS exception and
  firewall/SSH-window approvals were explicitly supplied during this review.
- Run the reviewed staging `register` once. Require successful completion and
  exactly one verified account for that staging endpoint before the reviewed
  `certonly --dry-run`. A failed or ambiguous result is HOLD, not a retry.
- Registration intentionally creates CA account/key/log state; dry-run is also
  not read-only. Keep root-private account/work/log paths and umask 077, with no
  key, account body, raw identifier or raw log output in shared evidence.
- The second command needs no repeated `--agree-tos`, no-email-registration or
  `--no-eff-email` flag when using the already-verified account. Do not use it as
  an implicit registration fallback or accept an unexpected account selection.
- Production issuance is a later stage requiring staging PASS and its own exact
  command/action-time gates. This verdict does not approve a production command,
  forced renewal, timer enablement, application/DB startup or broad HTTPS access.

## Remaining Risks and Closure

Parent delegation and current A/SOA observations support the limited attempt;
child apex NS AA/NODATA remains an unresolved observation. Provider cause and
future resolver/renewal impact are unknown. TLS success cannot close this DNS
follow-up or establish public application readiness. SOA refresh is not a
provider publication SLA. No guessed NS/DS/CAA or glue changes are authorized.

Keep MySQL masked/inactive, app absent/off and Certbot service/timer fenced.
HTTP stays ACME-only; HTTPS remains limited to the approved management /32.
On failure, retain artifacts, close only this window's new 80/443 rules,
quiesce/remask Nginx, preserve SSH/egress and complete exact-key/empty-agent/
Stopped-Disabled cleanup. Runtime Certbot masks are not persistent reboot fencing.

Only the WI004 summary and Evidence Pack were written. MA owns subsequent
execution receipts and immediately triggers WI005 through the handoff skill;
final integration cannot claim unperformed issuance, TLS, renewal or cleanup.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-004-evidence-pack.md): Exact reviewed staging commands and identities.
- [Historical closure](../agent/WI-20261011-ATS-003-evidence-pack.md): Earlier stopped/masked window.
- [Runbook](../../deploy/single-vps/runbooks/domain-tls.md): Existing isolation, TLS and recovery gates.
