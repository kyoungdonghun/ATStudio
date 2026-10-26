---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-007.md
    reason: Approved bounded first-host work
  - path: ../agent/WI-20261010-ATS-017-evidence-pack.md
    reason: Independent review evidence and verification limits
---

# WI-20261010-ATS-017: First-Host Safety Review

> Purpose: Report independent local procedure review without claiming target execution.

## Result

**PASS for local procedure review, with existing execution gates retained.**
No actionable blocking defect was found in the reviewed scope. No source fix is
requested. This is not production readiness or permission to bypass a pending gate.

| Stage | Decision |
|---|---|
| Post-rebuild identity and direct-key SSH | Conditional: fresh independent console key, exact provider target, protected new pin and MA confirmation must precede login. PG did not verify the host or log in. |
| Temporary Windows agent | Conditional: recheck Stopped/Disabled, verify intended key absent before loading, preserve unrelated keys, remove only this window's key and prove absence, then restore Stopped/Disabled. Cleanup failure remains open until resolved. |
| Read-only host survey | Ready after authenticated access; report only sanitized observations. |
| Java17/Nginx/FFmpeg installation | HOLD until actual package provenance/dependencies/scripts, host survey, exact change and persistent Nginx start inhibition are reviewed. Retain Nginx mask; no default site. |
| MySQL installation | HOLD: package-managed initialization/account creation requires explicit resolution of the DB exclusion. A mask is not proof against initialization. |
| App/DB activation, SSH/firewall changes, secrets, backup, domain/TLS and purchases | Separate gates remain; this review authorizes none of them. |

## Verified Bounds

PG, 2026-10-10, local Windows/PowerShell: seven PowerShell fences parsed, seven
invalid synthetic inputs rejected before file/native operations, and two native
`ssh -G -F NUL` configuration-only checks passed. Reviewed runbook and
configuration hashes match WI016. No remote, service, key-loading or package action
was performed; actual prompt handling, cleanup and Linux behavior remain unverified.

The handoff attributes rebuild completion/operating status and user-reported new
console login to MA/user. PG did not independently observe those events. Earlier
network and host-key observations do not establish post-rebuild identity.

## Next Work Item

**WI017 review is complete and unblocks WI-20261010-ATS-018.** MA should immediately
use `create-wi-handoff-packet` and delegate DOCOPS integration with this receipt
and MA's latest sanitized execution evidence. Do not wait for SSH success to
record the accurately pending state. Do not close REQ007 on this review alone.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-017-evidence-pack.md): Hashes, pointers and reproduction.
- [First-host runbook](../../deploy/single-vps/runbooks/first-host-setup.md): Reviewed procedure.
