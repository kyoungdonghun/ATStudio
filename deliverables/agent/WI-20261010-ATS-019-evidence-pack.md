---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-019-handoff.md
    reason: Existing skill-generated ownership and output contract
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved continuation and explicit administrator mechanism
  - path: ../../deploy/single-vps/runbooks/host-hardening.md
    reason: Reviewable staged operations and remaining target gates
---

# Evidence Pack: WI-20261010-ATS-019

> TL;DR: Local preparation complete; ready for independent PG review. Remote application is not a SE result.

## Summary

Added a staged host runbook, three small byte-preserving upload candidates,
one read-only fixed-layout checker and two focused test files. Existing MySQL,
journal and application-unit templates remain unchanged. No remote action,
private-key/config access, password handling, purchase, SQL mutation, service
activation, destructive cleanup, stage/commit/push or descendant delegation ran.

## Scope / DoD Check

- [x] Existing WI019 handoff and approved REQ007 read before implementation.
- [x] Explicit atadmin/key/NOPASSWD mechanism, locked admin password and preserved ubuntu.
- [x] Separate new admin/sudo/PTY and authenticated-console gates before SSH restriction.
- [x] First-value/Include/Match/socket review, candidate/installed context validation,
  post-reload new admin success, root denial and publickey-only method-offer checks.
- [x] Nonlogin service account, disjoint storage/temp roots, immutable-release ownership,
  root-private config and nginx/ACME directories with collision/symlink/ACL checks.
- [x] MySQL exact-byte template, include/override gates, native validation and bounded
  internal read-only query; stop/remask and timeout-job recovery specified.
- [x] Journal vendor conflict addressed by runtime target99-atstudio.conf, source
  unchanged; safe synthetic receipt, separate logrotate and copying/retention limits.
- [x] Focused local tests, shell parse checks, bounded rollback and two deliverables.
- [ ] Native target validators and applied artifact hashes: MA target stage, NOT RUN by SE.
- [ ] Fresh admin/sudo and console recovery proof: remote gates, not local completion.

These checked items describe prepared procedures/contracts, not successful remote
application. Latest MA report: root SSH intact; console password not recalled,
separate native user-only reset approval pending. SSH restriction remains HOLD.
Do not wait for or handle a password to close this local WI.

## Reference Documents (Tier 0-2)

The injected create-wi-evidence-pack skill was applied directly: handoff existence
was verified and read, INPUT POINTERS mapped, and this file written with apply_patch.
ATS resolved from `.claude/config/workspace.json`; routing was read from
`.claude/config/context-injection-rules.json`, assignee se, required tiers[0],
security/testing/operation context. No new handoff or descendant was created.

| Tier | Handoff input | Use / boundary |
|---|---|---|
| 0 | docs/standards/core-principles.md | Injected approval and bounded evidence |
| 0 | docs/standards/documentation-standards.md | Injected metadata, language, history preservation |
| 0 | docs/standards/development-standards.md | Injected implementation and test standards |
| 0 | docs/standards/glossary.md | Injected canonical terms |
| 1 | .claude/agents/se.md | Injected SE role; current Tier0 SPA status supersedes stale persona phase wording |
| 1 | docs/policies/security-policy.md | Injected secret-safe constraints |
| 1 | docs/policies/access-control-policy.md | Read least privilege and explicit operation authority |
| 1 | docs/policies/quality-gates.md | Read independent review and evidence gates |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md | Injected and read direct-write contract |
| 2 | docs/standards/evidence-pack-standard.md | Read reproducibility and scope requirements |
| 2 | docs/index.md#current-decision-entry | Read current operational checkpoint, not old pending claims |
| 2 | deploy/single-vps/CONFIGURATION.md | Read exact ownership/path contract |
| 2 | deploy/single-vps/mysql/atstudio.cnf | Read/preserved exact candidate |
| 2 | deploy/single-vps/journald/atstudio.conf | Read/preserved exact candidate; corrected deployment filename only |
| 2 | deploy/single-vps/systemd/atstudio.service | Read/preserved single-service identity contract |
| 2 | deploy/single-vps/runbooks/first-host-setup.md | Read/preserved native pin/agent cleanup procedure |
| 2 | docs/templates/eval-report-template.md; wi-subagent-handoff-template.md; requirements-request-template.md | Catalog pointers in handoff; no new template execution or claim of full reading |
| 2 | .agents/skills/react-best-practices/AGENTS.md; docs/standards/frontend-standards.md | Conditional catalog only; no frontend work or reading required |
| Snapshot | REQ007; WI018 Evidence Pack; subsequent MA survey messages | Approval/prior receipt and separately attributed fresh observations |

Additional `.agents/skills/test/SKILL.md` read; production Java/React suites were
not applicable to this isolated standard-library Python tooling. The local suite
uses unittest, consistent with existing deployment-helper tests. Official Ubuntu
SSH/sshd/journald and MySQL validation documentation were checked; direct sources
and specific limitations are linked in the runbook. No provider pricing research.

## Evidence Pointers

Only these nine newly created paths are owned:

| Path | Pointer / rationale |
|---|---|
| deploy/single-vps/runbooks/host-hardening.md | Sections1-7: attributed survey, exact stages, stop points, rollback |
| scripts/deployment/host-hardening/00-atstudio-admin.conf | Fixed approved SSH policy, no AllowUsers or shell restriction |
| scripts/deployment/host-hardening/90-atstudio-admin | One approved atadmin sudo rule |
| scripts/deployment/host-hardening/sshd-validation.conf | Two ordered Includes; never installed into /etc/ssh |
| scripts/deployment/host-hardening/check_layout.py | check_layout(), resolve_owners(), main(): read-only pre/post checks |
| scripts/deployment/host-hardening/test_check_layout.py | 24 synthetic metadata/account/CLI failure-path tests |
| scripts/deployment/host-hardening/test_candidates.py | Six exact candidate/encoding/template-preservation tests |
| deliverables/user/WI-20261010-ATS-019-summary.md | User-facing scope, ready/held state and limitations |
| deliverables/agent/WI-20261010-ATS-019-evidence-pack.md | This evidence and review trigger |

No edits to source, REQ, handoff, current index, historical receipts or other
agents' files. Initial branch main/HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6
had extensive pre-existing dirty/untracked work, preserved. HEAD is not artifact
identity. Final source hashes are recorded below; this evidence file has no
self-referential hash.

| Final new artifact | SHA-256 |
|---|---|
| deploy/single-vps/runbooks/host-hardening.md | D5B21176BADC57EE7437F4D6F80844BB955F9FF4BDAFCD9EBEEA66E7BD7836D5 |
| scripts/deployment/host-hardening/00-atstudio-admin.conf | 3E629E4C3E5EAF42C89C9A2FC51F23C691FC824105409041F0F4BA08013ECB03 |
| scripts/deployment/host-hardening/90-atstudio-admin | BF1F13292906B556F3589074924F672BF2B453E21A68C586A5B10661EAAF2B94 |
| scripts/deployment/host-hardening/sshd-validation.conf | 3C650342D8325D170EF249B96EBA8BA6A0CAB9D140DEAB80334726E2D1EA6CDF |
| scripts/deployment/host-hardening/check_layout.py | CE755FDBDC88D2E1B8CCE83834CF167568239C6FA2273EB56320277440AFAD81 |
| scripts/deployment/host-hardening/test_check_layout.py | 6F49C21E13A781E69FBC6A7E5DD2E2D1494C8A78E8CFB09B8EA4DD5AF5DDEF33 |
| scripts/deployment/host-hardening/test_candidates.py | 994A1146A85907CAB3A429A86926D66DC9457226CE7A8A7E00DEA9FF97A791EA |
| deliverables/user/WI-20261010-ATS-019-summary.md | 9BB66DF6435F1A313D9BA645797B289C18B36F39A571FE8980CC88CB78707AAD |

## Commands and Tests

Verification owner SE; date2026-10-10; local Windows/PowerShell, Python3.14.3 and
Git Bash. Native Linux services and target filesystem were never accessed by SE.

| Check | Result / reproducible command |
|---|---|
| Red phase | Initial unittest discovery failed with ModuleNotFoundError before checker implementation; not a daemon failure |
| Focused tests | `python -B -m unittest discover -s scripts/deployment/host-hardening -p 'test_*.py' -v`:30 passed,0 failed,0 errors,0 skipped;0.059s |
| Bash syntax | Extract each fenced bash block from the runbook and pipe individually to Git Bash `bash -n`:10/10 passed; no block executed |
| Unsupported host | `python -B scripts/deployment/host-hardening/check_layout.py before`: HOLD, native exit2 explicitly checked using `$LASTEXITCODE`; no host inspection |
| Artifact identity | `Get-FileHash -Algorithm SHA256` on all candidates/tests/runbook/summary and six preserved inputs |

Final local recheck:30/30 tests passed in0.055s; all nine owned files passed
trailing-whitespace checks, all three documents passed required metadata and
dependency checks, and19 local Markdown file links resolved. Six preserved-input
hashes and nine full-path Evidence Pack hash entries matched. Scoped
`git diff --check` exited0; because these paths are untracked, the separate
nine-file whitespace check is the content check, not Git's empty diff alone.
External-link reachability and every fragment anchor were not comprehensively
validated. Bash syntax results remain parse-only10/10.

Failure coverage includes existing targets, dangling/ancestor symlinks, missing
parents, files as ancestors, untrusted ownership/write bits, access/default ACLs,
unreadable metadata, wrong UID/GID/mode, inode aliases, unprivileged/non-Linux use,
unsafe account identity and error redaction. Tests mock POSIX metadata/account
lookup; they do not prove real Linux ACLs, NSS, modes, bind mounts or sudo behavior.
No numerical coverage percentage or full-project test result is claimed.

Native `sshd -t/-T -C`, visudo, candidate mysqld validation, real journal parser,
socket auth/plugin query, metadata creation and resource effects remain target
checks. MA's reported baseline mysql validator exit0 predates the candidate and
is not substituted for these checks. Review the wrapper on actual OpenSSH before
installation. Actual ACL/mount checks and concurrent-change exclusion remain MA
preconditions. Failed/unfinished checks require HOLD, not a bypass.

## Preserved Input Hashes

SHA-256 baseline, recorded before edits and matched on local recheck:

| Input | SHA-256 |
|---|---|
| CONFIGURATION.md | BD1A8E2FF426F7660AA063D890FBE446E7D60A8C323EC49CF6DDF9184B9C4664 |
| mysql/atstudio.cnf | 008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233 |
| journald/atstudio.conf | 229473C1D88E2CF30DAC6E9F4ED34327A39A66A74653DC72465E42239B2FC963 |
| systemd/atstudio.service | 118805E388B24E013148A796AED41B70F596ADA18FF627F8417B6B7B6262E8D1 |
| runbooks/first-host-setup.md | 1C805ABFBC64E18662E0822DCBC6C2E4C5EFD0493D59ECA7334E0FF00AB88D33 |
| deliverables/agent/WI-20261010-ATS-018-evidence-pack.md | 1368ED2E7CBE1711CDC5B42B5404188869CC2BA38894EB57AB21AACCEDEB8B83 |

First five paths are under deploy/single-vps. No old registered hash was rewritten.

## Risks / Rollback

The approved key/NOPASSWD account is root-equivalent. No forwarding restriction
contains an administrator with a shell. SSH configuration mistakes require the
independently authenticated recovery route, currently HOLD. Root local password
and provider ubuntu account are not changed by this package.

Runbook section7 requires exact new-file hashes, original absence/baselines and
fresh quarantine names before withdrawal; no recursive cleanup or data removal.
Partial directories/accounts are left in place for review. MySQL startup has
normal distribution writes; timeout must be followed by confirmed stop/remask,
not just timeout exit. Journal aging is irreversible and does not bound other
logs or prove all external copies absent. No paid goods/capacity guarantee.

For local rollback, withdraw only this WI's new candidates through a reviewed
patch; preserve historical artifacts, other dirty files and MA's runtime state.
No remote rollback was run or needed by SE.

## Follow-ups and Chain Trigger

WI019 Blocks WI020. **Immediately hand this package to MA for skill-generated
WI020/PG independent review**, followed by MA-approved stage application and
WI021 integration. This completion report is the trigger, not a claim that PG
already reviewed or that SE created/delegated the next WI. SE owns only WI019.
No new chat or remote communication was created. REQ007 remains active.
Console recovery/reset approval can remain held without blocking local handback.

## Related Documents

- [Handoff](WI-20261010-ATS-019-handoff.md)
- [User summary](../user/WI-20261010-ATS-019-summary.md)
- [Runbook](../../deploy/single-vps/runbooks/host-hardening.md)
- [REQ007](../user/REQ-20261010-ATS-007.md)

## 2026-10-10 Correction and Recovery Addendum

MA identified an error during WI020 review:99-atstudio.conf sorts BEFORE vendor
syslog.conf, so the earlier claim that it resolved that override is withdrawn.
The runbook's destination, install/preflight and rollback now use
`/etc/systemd/journald.conf.d/zz-atstudio.conf`. Its lexical relation to the known
vendor basename is tested, but only MA's actual all-directory inventory,
same-basename precedence resolution, merged order and final values can establish
target policy. A later basename, collision or unresolved inventory remains HOLD.
SE performed no remote inventory, installation or restart.

Only the runbook and its candidate-contract test changed; the user summary and
this Evidence Pack received dated addenda. All SSH/sudo candidates, helper and
MySQL/journal template bytes remain unchanged. The earlier hash table is the
historical pre-correction snapshot, not the current identity for these three
updated artifacts. No previously registered hash or historical body was replaced.

Pre-addendum SHA-256 of this Evidence Pack:
`FEDA5BB4963B56C711CCEFBE3914A0A4DE07F7EEE4337E740C5BB221E47D7BD2`.
This identifies the exact prior file, not the file containing this addendum.

| Corrected artifact | New SHA-256 |
|---|---|
| deploy/single-vps/runbooks/host-hardening.md | B3B4E7BE77D17FC1042AEC11B575920EAF52A4F2EAFBC99D9B95E17BB0D2D636 |
| scripts/deployment/host-hardening/test_candidates.py | A3F648A4350276940BAFCD2ADE7F82FECC58433ABDFF38F2191B66F31CACF5CE |
| deliverables/user/WI-20261010-ATS-019-summary.md | 89B0034AD305A55E7D944A1FD4B9EBA06713C8E24691D2CC633F9BE3A4C99E33 |

Local SE revalidation on2026-10-10: unittest30/30 passed,0 failures/errors/skips,
0.085s; all10 fenced Bash blocks passed Git Bash `bash -n` without execution.
The existing candidate test now checks the correct destination, rejects the old
destination and asserts99 < syslog < zz plus all four inventory-directory pointers.
MySQL/journal hashes still match the original table. Native daemon validation,
merged target policy and installation remain MA/PG work, not local PASS claims.

At20:23 KST on2026-10-10, MA reports the user completed the reset privately and
the noVNC root prompt and `who` tty1 were verified. This supersedes the earlier
password/recovery HOLD statements without deleting them. SE did not handle or
reset a password and did not independently observe the console. WI020 review,
fresh admin key/sudo/PTY proof and remaining stage gates still precede application.
The corrected package is returned for ongoing MA/PG review; no new scope is added.

## 2026-10-10 AppArmor Validation Addendum

MA reported a confirmed exact audit denial when the enforced `/usr/sbin/mysqld`
AppArmor profile tried to read `/root/wi019/atstudio.cnf`; its existing policy
allows `/etc/mysql/**` reads. No candidate MySQL drop-in had been installed and
MySQL had not been started at that report. SE did not reproduce the denial or
connect remotely; this is attributed MA evidence, not a local test result.

Only runbook section5 and its dated addendum changed, followed by this append-only
evidence update. The standalone candidate is now copied from the unchanged
root-stage original to the exact `/etc/mysql/atstudio-validation.cnf`, outside
both included directories and subject to a fresh complete include-graph check.
The procedure requires absence including dangling symlinks, safe real ancestry,
ACL/mount review, source identity/hash, root:root0644 regular single-link target
and matching target hash before `runuser -u mysql -- /usr/sbin/mysqld
--defaults-file=/etc/mysql/atstudio-validation.cnf --validate-config
--log-error-verbosity=2`. A failed prerequisite or validator is HOLD.

The enforced AppArmor profile is not changed, disabled or relaxed. No alternate
privilege bypass or server start is added. Both original and isolated validation
copy are retained, with the latter explicitly outside the include graph until
separately reviewed cleanup. Final include installation remains gated behind
standalone validation, then the existing effective-chain validator and PG gate.
No tests, helper, SSH/sudo candidate, user summary or baseline template changed.

Pre-addendum SHA-256 of this Evidence Pack:
`54493802E92C903C9BFF904EA084F9E31672DAB7AB11870A1AC69FD6F3BE2DFF`.
Earlier Evidence Pack bodies and hash registrations remain unchanged. The new
runbook identity below supersedes only its earlier current-artifact claims:

| Corrected artifact | New SHA-256 |
|---|---|
| deploy/single-vps/runbooks/host-hardening.md | 41DB5A49DE5B1C550360DEA4922C49B7F8C6E3F8A75F959963C85D6F38067B18 |

SE local verification on2026-10-10: existing unittest30/30 passed with0
failures/errors/skips in0.097s; all11 fenced Bash blocks passed Git Bash `bash -n`
without execution. MySQL template remains
`008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233`;
candidate-contract test remains
`A3F648A4350276940BAFCD2ADE7F82FECC58433ABDFF38F2191B66F31CACF5CE`.
These checks do not prove target AppArmor/DAC access, native validation or actual
include exclusion. PG review and MA's checked native validator remain HOLD; the
scoped correction is ready for their review with no additional runtime action.

## 2026-10-10 MySQL Client Compatibility Addendum

MA reports native MySQL8.0.46 client help returned unknown-option exit2 for
`--no-login-paths`; the read-only query never ran. MA's bounded start observed
only loopback3306 and the Unix socket, with no X listener, then successful stop
and remask. Those outcomes do not establish the pending SQL variable values.
MA separately reports that the AppArmor-compatible standalone, included-chain
and default-chain validators all passed. This supersedes their historical
pending status; these are MA-owned runtime observations, not SE execution.

Section5 removes the unsupported client option and requires metadata-only absence
of both `/root/.mylogin.cnf` and `/root/.my.cnf`, including dangling symlinks,
immediately before the query. Any presence, inability to inspect or concurrent
change is HOLD. The exact query environment is
`env -u MYSQL_PWD -u MYSQL_HOME -u MYSQL_TEST_LOGIN_FILE HOME=/root mysql
--no-defaults`, with explicit protocol=SOCKET, verified socket and user=root.
The existing10s bound and allowlisted read-only SQL remain unchanged. No credential
body read, credential deletion/move, password argument/prompt/fallback, profile
change or automatic authentication retry is introduced.

Changed paths are only the runbook, its existing candidate-contract test and
this append-only Evidence Pack. Baseline templates, helper, SSH/sudo candidates,
user summary and all earlier evidence bodies/hash registrations are preserved.
Pre-addendum Evidence Pack SHA-256:
`5CC081A552411371A769E847F8E329692B9263BBDD3C6176CF7DE6887B6D136C`.

| Corrected artifact | New SHA-256 |
|---|---|
| deploy/single-vps/runbooks/host-hardening.md | 43755E6F58EF9543EB6DFDCE0F81C7DAF816D93C4FACA1B4125C2BE3B80F1E1A |
| scripts/deployment/host-hardening/test_candidates.py | BD24759B6F75BD463D43DF36E0A0388A4CEE6C1541EDCF38486D72B138F42915 |

SE local verification on2026-10-10, Windows/Python3.14.3/Git Bash: the updated
contract test first failed against the old command, then unittest30/30 passed
with0 failures/errors/skips in0.068s after the runbook correction. The test checks
both metadata guards precede the query, the exact environment cleanup and explicit
connection options, and absence of unsupported/password options in that command.
All11 Bash blocks passed `bash -n` without execution. MySQL template SHA-256 remains
`008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233`.

No root configuration metadata/content, native client, remote service or target
query was inspected/executed by SE. Local text/syntax tests do not prove target
file absence, authentication or query values. PG review and a fresh bounded MA
query remain HOLD; the server's last reported state is stopped/remasked PASS.
