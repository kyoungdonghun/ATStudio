---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-017-handoff.md
    reason: Approved scope, injected references and output ownership
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approval and exclusions
  - path: WI-20261010-ATS-016-evidence-pack.md
    reason: Preparation evidence independently reviewed here
  - path: ../../deploy/single-vps/runbooks/first-host-setup.md
    reason: Reviewed procedure
  - path: ../../deploy/single-vps/CONFIGURATION.md
    reason: Configuration and activation boundaries
---

# Evidence Pack: WI-20261010-ATS-017

> Purpose: Record bounded independent PG review of first-host preparation.

## Summary

**Local review PASS with existing mitigations/gates; target execution NOT RUN by PG.**
No actionable blocking defect found within the supplied procedure. No implementation
change requested. This does not certify the live host, packages or production.

## Scope / DoD Check

- [x] Review exact-target native SSH validation and secret-entry boundaries.
- [x] Review agent ownership, cancellation, exact-key removal and restoration.
- [x] Review Nginx no-start plan and separate MySQL initialization hold.
- [x] Record source hashes, independent narrow checks and execution limits.
- [x] Write only the two owned deliverables; return WI018 chain trigger to MA.

## Reference Documents (Tier 0-2)

The on-disk WI017 handoff existed and was read before evidence generation.
The injected `create-wi-evidence-pack` skill was followed, including direct writing.

| Tier | Handoff input | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md | Injected approval and evidence rules |
| 0 | docs/standards/documentation-standards.md | Injected English metadata/two-set format |
| 0 | docs/standards/development-standards.md | Injected scoped verification and preservation |
| 0 | docs/standards/glossary.md | Injected canonical terms |
| 1 | .claude/agents/pg.md | Injected independent security-review role |
| 1 | docs/policies/security-policy.md | Injected secrets and operational boundaries |
| 2 | deliverables/user/REQ-20261010-ATS-007.md | Read current approval including rebuild/agent addendum |
| 2 | deliverables/agent/WI-20261010-ATS-016-evidence-pack.md | Read preparation evidence and attributed MA history |
| 2 | deploy/single-vps/runbooks/first-host-setup.md | Read complete procedure with line numbers |
| 2 | deploy/single-vps/CONFIGURATION.md | Read configuration and later activation gates |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md | Injected mandatory evidence contract |

Injection order: Tier0, PG role/security policy, task context, MA snapshot.
Assignee pg; task type security/review; required tiers [0, 1]. References are
derived from the actual handoff; no dynamic injection configuration audit claimed.

## Evidence Pointers and Risk Assessment

Line references below identify the hashed local sources, not deployed files.

| Location | Assessment / required mitigation |
|---|---|
| first-host-setup.md:26-60, 66-109 | PASS: independent authenticated recovery console precedes trust; old pin invalidated after rebuild; one new exact-target public pin, no TOFU or silent overwrite. ACL/non-reparse verification remains an operator prerequisite. |
| first-host-setup.md:120-177 | PASS: canonical literal/port/account validation, exact one-line Ed25519 host binding, bounded public-file read, native fingerprint comparison and fixed executable. Target authorization remains MA's explicit check, not something syntax can prove. |
| first-host-setup.md:164-195 | PASS: dedicated trust file, no user config, strict host checking, fixed host-key type, no agent/forwarding/password fallback; user-only hidden native client-key passphrase. No PowerShell private-key content read. |
| first-host-setup.md:208-246 | PASS with operational checks: original service state rechecked, owning-user absence check, distinguish empty/unavailable agent, no removal of pre-existing key; preserve validated SSH options with agent-only BatchMode. No live agent test. |
| first-host-setup.md:248-271 | PASS: only owned key removed; verify absence and unrelated set preservation before restoring Stopped/Disabled; failed add/cancellation and unexpected console closure retain MA cleanup duty. Stopping alone is explicitly insufficient. |
| first-host-setup.md:273-328 | PASS: sanitized fresh-host survey, no private environment/journal dump, cache-only planning, unknown existing data HOLD, no automatic repository/upgrade/reboot action. |
| first-host-setup.md:330-350 | CONDITIONAL: review actual scripts and existing policy, require mask before approved install, retain mask and verify inactive/listeners afterward. Mask/NEEDRESTART_MODE is not claimed to suppress all scripts; unknown effects HOLD. No Linux execution proof. |
| first-host-setup.md:352-359 | HOLD correctly retained: package initialization/system accounts can cross DB exclusion; require explicit decision before mysql-server install, not merely a mask. |
| first-host-setup.md:363-387; CONFIGURATION.md:23-47, 80-106, 254-304 | PASS: subsequent identity/directory, logging, firewall, DB, domain/TLS and app changes gated. New artifact/admission required; no old-JAR pool-only activation, secret provisioning or unapproved rollback. |

No ranked corrective finding is issued. The runbook's rebuild-in-progress and
notification-investigation wording at lines 40-54 and 203-205 is attributed
historical preparation context, not a fresh status source. WI018 should record
the newer MA snapshot in current entry documents without rewriting old receipts.

## Source Identity

PG observed branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, with
pre-existing tracked and untracked changes. HEAD alone does not identify candidates.

| Reviewed path | SHA-256 |
|---|---|
| deploy/single-vps/runbooks/first-host-setup.md | 1C805ABFBC64E18662E0822DCBC6C2E4C5EFD0493D59ECA7334E0FF00AB88D33 |
| deploy/single-vps/CONFIGURATION.md | BD1A8E2FF426F7660AA063D890FBE446E7D60A8C323EC49CF6DDF9184B9C4664 |
| deliverables/user/REQ-20261010-ATS-007.md | 1F8FDC3E5D18247C6AAD0227C93A777EF0DD4F06BACF3297E62E55CDD2184233 |
| deliverables/agent/WI-20261010-ATS-016-evidence-pack.md | 1557BA7AB00D4025A4FC0C050FE0390A362B4C538DD5F3282F4147BB2FAE8879 |
| deliverables/agent/WI-20261010-ATS-017-handoff.md | 0E42FB47C952577CF3BAC6B7676A44077D728A5E39BB33CD43AD301E9822264E |

Runbook and CONFIGURATION hashes match WI016's receipt. Other hashes identify the
current reviewed inputs; they are not claims that MA will leave them unchanged.

## Commands and Tests

Owner PG; date 2026-10-10; environment local Windows/PowerShell only.

| Command/check | Result |
|---|---|
| `git status --short --branch`; `git rev-parse HEAD` | Current checkout identified; dirty work preserved |
| `Get-Content` on listed task pointers; numbered source reads | Completed targeted review; no private files read |
| `Get-FileHash -Algorithm SHA256 -LiteralPath <five paths above>` | Hash identities recorded |
| PowerShell `Parser.ParseInput` for each powershell fence | PASS 7; snippets not executed |
| First fence's parameterized ScriptBlockExpressionAst, seven invalid fixtures | PASS 7; missing/short fingerprint, hostname, option-like address, port0/65536, option-like user rejected before file/native operations |
| Native `ssh.exe -G -F NUL` with runbook options, direct and agent variants | PASS 2; strict checking, exact trust path, Ed25519, forwarding OFF, password/interactive auth OFF, publickey only, no key update/add |
| `Get-Item` on two output paths with SilentlyContinue | Neither existed before creation; expected absence/exit1, not a test failure |

Reproduction: read runbook fences in memory; parse without executing surrounding
Read-Host/login/service commands. For the negative cases extract only the first
parameterized script block and use synthetic literal `192.0.2.10`, account
`operator`, port22 and `SHA256:` plus43 `A` characters, replacing one value per
case listed above. Require the specific early HOLD, not any arbitrary exception.

For native options, add `-G` to the documented `-F NUL` options and substitute
nonexistent `C:/__WI017_fixture__/known_hosts` and `nonexistent-key` paths, synthetic
address/account above. Direct mode uses IdentityAgent=none and BatchMode=no;
agent mode uses IdentityAgent=//./pipe/openssh-ssh-agent and BatchMode=yes.
Both returned exit0 and the intended configuration. No fixture files or connection
were created. This checks parsing, not trust validation or agent communication.

No real pin/key/ACL read, service status query/mutation, ssh-add, browser, remote
connection, package install, shell execution on Linux, broad test suite, external
send or secret access occurred. WI016's mocked positive-pin cases were reviewed
as SE evidence, not claimed independently rerun by PG. Actual key correspondence,
native prompting, agent cleanup and package effects remain target/operator tests.

## Changes, Risks and Rollback

Only this Evidence Pack and `deliverables/user/WI-20261010-ATS-017-summary.md`
were created with apply_patch. No source/runbook/index/REQ edit, descendant,
staging, commit, push or memory write occurred. No raw target identifier,
account, address, fingerprint, console token or credential is recorded here.

Review cannot prove live ownership, host identity, service inhibition or cleanup.
Missing evidence means HOLD at the corresponding stage. PG made no remote change,
so remote rollback is not applicable. Withhold unsafe/unverified procedures;
preserve all existing work and historical evidence, and never delete trust/data
or auto-unmask/restart a service as a response to this review.

## Follow-ups

**WI017 complete: immediately trigger WI-20261010-ATS-018 through MA's
create-wi-handoff-packet and DOCOPS delegation.** PG is prohibited from descendants
and does not perform that delegation. WI018 can record pending login without
waiting for it; integrate MA's newest sanitized identity/execution receipts,
runbook navigation and all remaining gates. REQ007 stays open until its full chain
and accepted completion criteria are satisfied. Production remains HOLD.

## Related Documents

- [Handoff](WI-20261010-ATS-017-handoff.md): Scope and ownership.
- [User summary](../user/WI-20261010-ATS-017-summary.md): Stage readiness.
- [REQ007](../user/REQ-20261010-ATS-007.md): Approval and chain.
- [Runbook](../../deploy/single-vps/runbooks/first-host-setup.md): Reviewed source.
- [Configuration](../../deploy/single-vps/CONFIGURATION.md): Later deployment gates.
