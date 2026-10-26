---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-018-handoff.md
    reason: Approved scope and final MA snapshot gate
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved work and progress ownership
  - path: WI-20261010-ATS-016-evidence-pack.md
    reason: Local procedure preparation evidence
  - path: WI-20261010-ATS-017-evidence-pack.md
    reason: Independent local procedure review
---

# Evidence Pack: WI-20261010-ATS-018

> Purpose: Integrate first-host navigation and separately attributed execution evidence.

## Summary

WI018 documentation integration complete from MA's final operational snapshot.
REQ007 remains active for unmet setup/security work; production remains HOLD.
MA's final cleanup receipt confirms exact-key removal, an empty identity list,
Windows agent Stopped/Disabled at19:57:21KST and noVNC root-console logout.

## Scope / DoD Check

- [x] Read the existing skill-generated WI018 handoff and approved REQ007.
- [x] Link REQ007, WI016/017/018 and the reviewed first-host runbook.
- [x] Preserve historical evidence, source and runbook bodies.
- [x] Keep domain purchase before DNS/HTTPS and production HOLD visible.
- [x] Integrate final MA-attributed target, local-agent cleanup and console-logout receipt.
- [x] Record scoped validation and final preservation checks.
- [x] Finalize the two deliverables without prematurely closing REQ007.

## Reference Documents (Tier 0-2)

The handoff existed and was read before writing this Evidence Pack; the injected
create-wi-evidence-pack skill's direct-write contract is applied. ATS was checked
in `.claude/config/workspace.json`. No descendant agent was invoked.

| Tier | Handoff input | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md | Injected approval and evidence boundaries |
| 0 | docs/standards/documentation-standards.md | Injected metadata and English documentation |
| 0 | docs/standards/development-standards.md | Injected scoped changes and reproducibility |
| 0 | docs/standards/glossary.md | Injected canonical terminology |
| 1 | .claude/agents/docops.md | Injected documentation ownership |
| 1 | docs/policies/security-policy.md | Injected secret-safe reporting |
| 2 | docs/index.md | Current decision entry and preserved checkpoints |
| 2 | deploy/single-vps/README.md | Operator navigation |
| 2 | deliverables/user/REQ-20261010-ATS-007.md | Approval, exclusions and active chain |
| 2 | deliverables/agent/WI-20261010-ATS-016-evidence-pack.md | SE local preparation and historical MA attribution |
| 2 | deliverables/agent/WI-20261010-ATS-017-evidence-pack.md | PG local review and source identities |
| 2 | deploy/single-vps/runbooks/first-host-setup.md | Reviewed unchanged procedure |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md | Injected direct-write contract; on-disk header checked |
| 2 | .agents/skills/validate-docs/SKILL.md | Read scoped validation guidance and bundled validator |

Injection order: Tier0, DocOps role/security policy, task context, MA snapshot.
Assignee docops; task type documentation. Rule source is
`.claude/config/context-injection-rules.json`; no configuration audit claimed.

## Evidence Pointers

Only these five paths are owned by this WI:

- `docs/index.md`: additive first-host checkpoint before older local preparation.
- `deploy/single-vps/README.md`: first-host navigation; no runbook implementation.
- `deliverables/user/REQ-20261010-ATS-007.md`: dated successor to original progress.
- `deliverables/user/WI-20261010-ATS-018-summary.md`: bounded user-facing result.
- This Evidence Pack: validation, attribution and completion gate.

## Drift Report and Fix Plan

| Finding | Impact | Scoped response |
|---|---|---|
| Current entry starts with local REQ006 evidence | Could hide later approved host work | Add REQ007 checkpoint and reviewed procedure links |
| Original REQ007 progress predates subsequent MA actions | Could be mistaken for current runtime | Preserve original text; append dated progress |
| First-host runbook and WI016 contain earlier rebuilding/pending wording | Historical preparation could be read as live status | Link current checkpoint; preserve reviewed bytes and registered hashes |
| Final snapshot and cleanup verification arrived after preparation | Earlier pending statements no longer describe target setup | Integrate MA result and observed19:57:21KST agent state, not merely executed commands |

## Identity and Preservation

DOCOPS observed local branch `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`, with pre-existing dirty/untracked
work. HEAD alone does not identify a deployed artifact. No old JAR replacement
or target inspection is claimed.

| Preserved input | Initial SHA-256 |
|---|---|
| deploy/single-vps/runbooks/first-host-setup.md | 1C805ABFBC64E18662E0822DCBC6C2E4C5EFD0493D59ECA7334E0FF00AB88D33 |
| deploy/single-vps/CONFIGURATION.md | BD1A8E2FF426F7660AA063D890FBE446E7D60A8C323EC49CF6DDF9184B9C4664 |
| deliverables/agent/WI-20261010-ATS-016-evidence-pack.md | 1557BA7AB00D4025A4FC0C050FE0390A362B4C538DD5F3282F4147BB2FAE8879 |
| deliverables/agent/WI-20261010-ATS-017-evidence-pack.md | 8584DBBAB4C5800F79CF59FED7EE1D238013515648424130ED6F85F9A54C354E |
| deliverables/agent/WI-20261010-ATS-018-handoff.md | D8396E0C819B3E1E61D35F5B4DF57C4204CF6BF3060309248A386663C3F19E74 |

## Commands and Tests

Owner DOCOPS; date 2026-10-10; Windows/PowerShell, local documentation only.
Read-only `git status --short --branch`, `git rev-parse HEAD`, scoped
`Get-Content`/`rg` and `Get-FileHash -Algorithm SHA256` established inputs.
Edits used apply_patch. Preparation checks passed: five owned documents,
142 local Markdown file links, 11 frontmatter dependency paths, seven required
metadata fields per file, four Tier0 paths and 87 supported-format REQ/WI
occurrences. The fence-aware PowerShell checker resolves links/dependencies
relative to each document and rejects trailing whitespace. This checks file
existence, not external URLs or every fragment anchor. A first overbroad ID
check rejected existing index text; rerunning with the bundled validator's
supported-format matching rule passed, with no content change for that probe.

`git diff --check -- docs/index.md deploy/single-vps/README.md
deliverables/user/REQ-20261010-ATS-007.md
deliverables/user/WI-20261010-ATS-018-summary.md
deliverables/agent/WI-20261010-ATS-018-evidence-pack.md` returned exit0 (existing
CRLF-to-LF advisory only). Git does not check untracked contents; the five-file
checker supplies their whitespace check. All five preservation hashes above
matched on recheck. Reproduce by applying the same metadata/path/whitespace
checks to the five owned paths and comparing Get-FileHash to the table.
Final operational-integration recheck: PASS five documents,143 local file links,
11 dependencies,91 supported-format REQ/WI occurrences, all metadata and Tier0
paths, trailing whitespace and the new checkpoint heading/anchor. Scoped
`git diff --check` again exited0; all five preserved-input hashes matched.
The same scoped checks were rerun after integrating MA's19:57:21KST cleanup
receipt and console logout: unchanged counts, PASS, and5/5 preservation matches.
Full repository validation and application tests were not run for this
documentation-only WI. No external provider/pricing research was performed.

## MA Operational Receipt

Date2026-10-10; environment the single approved host and local Windows operator
machine; evidence owner/operator MA. The final snapshot was explicitly supplied
to DOCOPS for integration. DOCOPS did not independently execute or observe these
runtime checks; SE/PG evidence remains local procedure review.

### Purchase, Identity and Access

One existing server was purchased, then rebuilt in place once at19:42KST under
the exact no-data approval at displayed0KRW. No new instance, additional goods,
domain or storage purchase followed. The dated server amount is53,020KRW/month
including VAT;70,000KRW/month is a budget, not a billing block or refreshed quote.
Provider SSH firewall remained ON with the same source restriction after rebuild.

The user authenticated the noVNC recovery console; MA confirmed the root prompt.
Independent post-rebuild Ed25519 fingerprint matched native network observation;
hex-transcribed public key independently rehashed SHA256 PASS. An owner-only
dedicated local public trust file was created without overwriting an old file.
Native root key login and repeated reconnection passed with strict pin and
BatchMode. Initial root password rotation by the user succeeded in noVNC;
MA observed success without reading or recording the password. No endpoint,
account contact, raw host key, fingerprint or credential is reproduced here.

### Installation and Bounded Verification

The user's explicit package approval includes MySQL distribution system tables
and local administrative account initialization; its exception is recorded in
REQ007. MA reviewed actual Nginx/MySQL maintainer scripts, invoke-rc.d handling
of masked services and deb-systemd-helper preservation of administrator masks.
Both services were masked before installation and remained masked/inactive/dead
afterward. The mask-related preset warning was expected, not an apt failure.

| Installed runtime | Exact reported distribution version |
|---|---|
| Java17.0.20.1 | 17.0.20.1+1-1~24.04 |
| Nginx1.24.0 | 1.24.0-2ubuntu7.18 |
| FFmpeg6.1.1 | 7:6.1.1-3ubuntu5 |
| MySQL8.0.46 | 8.0.46-0ubuntu0.24.04.4 |

- Official-repository apt update:12.2MB. Approved installation download:182MB;
  planned installed footprint:763MB, not a measured post-install delta.
- 164 new packages, two dependency upgrades (libevent-core/libssh), zero removals,
  182 not upgraded. No full OS upgrade, reboot or reboot marker.
- Apt exit0; `dpkg --audit` empty/exit0; `systemctl --failed` empty.
- MySQL distribution initialized its own system data/accounts, including
  internally managed credentials. This is not ATStudio SQL/schema/user creation
  or new application-secret provisioning; no private configuration was read.
- MySQL/mysqlx default binding was loopback-only. Both services remained stopped,
  so config inspection is not a running-DB network acceptance test.
- One-second silent synthetic FFmpeg lavfi to libmp3lame128k to `/dev/null` exit0.
  Not a real WAV, upload, storage, throughput or load test.

### Host Measurements and Network Limits

| Observation | MA-reported value / boundary |
|---|---|
| OS / CPUs | Ubuntu24.04.4;2vCPU |
| Final RAM | total8327675904 bytes; available7870971904 bytes; swap0 |
| Final root filesystem | total102888095744 bytes; used4547354624; free98323963904 |
| Clock | Asia/Seoul; NTP synchronized |
| cloud-init | done/degraded, exit2; zero fatal errors; DEPRECATED category only |
| Final listeners | SSH22 IPv4/IPv6 and loopbackDNS53 only; no globally scoped IPv6 address |
| Current Windows line | TCP22 connect true;80/443/3306/33060/8080 false within1500ms; no other-network test |

Measured headroom is not an application-capacity or future cost guarantee.
The fresh survey initially found all four runtime packages absent; installation
above supersedes that observation, not the historical SE/PG evidence.

### Temporary Agent Cleanup

MA verified initial Stopped/Disabled and temporarily changed to Running/Manual
under separate approval. The initial identity list was empty; the owning user
loaded only the existing owned key through the native hidden passphrase prompt.
No secret was captured. MA reports successful exact-owned-key removal and a
verified empty list, then administrator Stop/Disabled restoration commands.
The final MA-owned `Get-Service ssh-agent` observation at **19:57:21 KST on
2026-10-10** confirmed **Stopped / Disabled**. This supersedes the earlier
pending-query statement; key cleanup and service restoration are both verified
within MA's operator receipt, not independently re-executed by DOCOPS.

MA also reports successful noVNC root-console logout. A fresh screenshot showed
the Ubuntu tty1 login prompt; no session screenshot, console token or account
details are stored here. This confirms that console logout, not broader SSH
hardening or application readiness.

### Remaining Gates

Global sshd defaults still permit root/password login, X11 forwarding, agent
forwarding and TCP forwarding. Provider SSH source restriction is present but
does not close hardening. Require a separate administrator/sudo and verified
second session before exact approved SSH changes; preserve recovery access.
Service identity/directories, log retention and MySQL tuning remain pending.
No application deployment, ATStudio DB/schema/users/secrets, public web activation,
domain purchase/DNS/HTTPS, backup/restore, alert or load acceptance occurred.
REQ007 stays active; local review and this documentation closeout cannot approve
production or retroactively authorize future operations.

## Risks / Rollback

No endpoint, account/contact data, fingerprint, raw host pin, credential or token
is added by this WI. No remote/service/provider action, purchase, source edit,
runbook edit, stage, commit, push or memory write is performed by DOCOPS.
Correct only owned additions through a reviewed patch; preserve unrelated dirty
work, historical records and source hashes. Documentation rollback must not
undo MA's approved host actions or substitute a document edit for runtime cleanup.

## Follow-ups

Agent cleanup and console logout are recorded complete; MA retains the pending
exact-change setup/security work above. WI016/017/018 chain results do not close the
REQ007 scope. Chain check: WI018 Blocks is empty; no successor is omitted and
no descendants are created. Domain purchase remains tracked before DNS/HTTPS.

## Related Documents

- [Handoff](WI-20261010-ATS-018-handoff.md)
- [REQ007](../user/REQ-20261010-ATS-007.md)
- [Summary](../user/WI-20261010-ATS-018-summary.md)
- [Current entry](../../docs/index.md#2026-10-10-first-host-checkpoint)
- [First-host runbook](../../deploy/single-vps/runbooks/first-host-setup.md)
