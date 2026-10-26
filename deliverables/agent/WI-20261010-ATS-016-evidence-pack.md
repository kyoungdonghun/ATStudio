---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-016-handoff.md
    reason: Skill-generated approved scope and ownership
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Initial-host approval and subsequent MA approval addendum
  - path: ../../deploy/single-vps/runbooks/first-host-setup.md
    reason: Owned local procedure
---

# Evidence Pack: WI-20261010-ATS-016

## Summary

Local first-host access/setup preparation is complete and ready for independent
PG review. A runbook is sufficient; no executable installer or credential helper
was added. Remote work remains pending, not failed. This receipt does not close
REQ007 or authorize application/DB activation.

## Scope / DoD Check

- [x] External exact target and independently verified Ed25519 public fingerprint;
  missing/mismatched values stop before login.
- [x] Untrusted connectivity/key observation separated from VNC trust and login.
- [x] Native Windows OpenSSH, user-only passphrase input, no key replacement/export.
- [x] Approved-but-unexecuted optional agent lifecycle with exact-key cleanup and
  original Stopped/Disabled restoration; no blanket identity removal.
- [x] Ubuntu24.04 package plan, no automatic web exposure, separate MySQL bootstrap gate.
- [x] Fresh-host survey before MySQL/journald/provider configuration application.
- [x] Local syntax, negative/synthetic checks and native option parsing.
- [x] Firewall, recovery, DB, secret, domain/TLS and exact-change gates retained.
- [x] Two-set deliverables and immediate WI017 handoff request to MA.

## Reference Documents (Tier 0-2)

The on-disk handoff existed and was read before evidence generation. The
`create-wi-evidence-pack` skill was loaded and its direct-write contract followed.

| Tier | Handoff input pointer | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md | Injected constitution and approval boundaries |
| 0 | docs/standards/documentation-standards.md | Injected English two-set documentation |
| 0 | docs/standards/development-standards.md | Injected scoped changes and verification |
| 0 | docs/standards/glossary.md | Injected canonical terms |
| 1 | docs/policies/security-policy.md | Injected secrets, access and deployment boundaries |
| 1 | docs/policies/access-control-policy.md | Explicitly read least-privilege policy |
| 2 | deploy/single-vps/CONFIGURATION.md | Existing paths/settings and artifact gate |
| 2 | deploy/single-vps/runbooks/deployment.md | Existing first-host operational sequence |
| 2 | deploy/single-vps/runbooks/resource-baseline.md | Survey, resource reserves and escalation |
| 2 | scripts/deployment/prepare-operator-ssh.ps1 | Read source only; reuse existing owner-key/native-prompt pattern, never invoke generation |
| 2 | docs/index.md#current-decision-entry | Current local-candidate versus runtime evidence |
| Context | deliverables/user/REQ-20261010-ATS-007.md | Approved bounded work; latest addendum owned by MA |
| Context | deliverables/agent/WI-20261010-ATS-014-evidence-pack.md | Previous independent local review and remaining target gates |

Rule source `.claude/config/context-injection-rules.json`; assignee se;
required tiers [0], with security/access policies supplied for this task.
ATS tag verified in `.claude/config/workspace.json`. Test and validate-docs skill
instructions were read; verification remained focused on the new runbook, not
the unrelated Java/frontend suites. No descendant agent was invoked by SE.

## Evidence Pointers

Only three new files were written with apply_patch:

- `deploy/single-vps/runbooks/first-host-setup.md`: section2 public-pin/native
  input guard and login; section2.5 optional agent lifecycle; sections3-5 survey,
  candidate packages, no-start behavior and hardening gates.
- `deliverables/user/WI-20261010-ATS-016-summary.md`: bounded closeout for MA/user.
- This Evidence Pack: preparation evidence, ownership and verification limits.

No new file under `scripts/deployment/first-host/` was needed. Existing templates,
source, REQ and indexes were not edited. The runbooks index/navigation update is
left to DOCOPS WI018 because WI016 explicitly excludes existing-file edits.

## Baseline and Identities

SE observed `main` at `58b1011dad796017e47dbcc82a4225ca449bcdf6` using
`git status --short --branch` and `git rev-parse HEAD`, with substantial existing
dirty/untracked work. This identifies the local checkout, not the remote runtime.

| Reviewed/new path | SHA-256 |
|---|---|
| deploy/single-vps/runbooks/first-host-setup.md | 1C805ABFBC64E18662E0822DCBC6C2E4C5EFD0493D59ECA7334E0FF00AB88D33 |
| scripts/deployment/prepare-operator-ssh.ps1 | 9562BD1472A97AF781E795A4C1221B9C16CEB5EAF0DA076D37B87781A018F5D0 |
| deploy/single-vps/CONFIGURATION.md | BD1A8E2FF426F7660AA063D890FBE446E7D60A8C323EC49CF6DDF9184B9C4664 |
| deploy/single-vps/runbooks/deployment.md | 410A6BCCCF3FDD28FE3D7B8BE5407570692BEC678BF95CF8E4CE7B065C5F6ACD |
| deploy/single-vps/runbooks/resource-baseline.md | C5DE63EF90E311486E28DB80692217445A1A3F28D8D1F6D47C238B120AD9C454 |
| deploy/single-vps/mysql/atstudio.cnf | 008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233 |
| deploy/single-vps/journald/atstudio.conf | 229473C1D88E2CF30DAC6E9F4ED34327A39A66A74653DC72465E42239B2FC963 |
| deliverables/agent/WI-20261010-ATS-016-handoff.md | 1294C1EF930D4DC13A5ADD340C671C7203224EB2221F4844193607223FB23090 |

The seven pre-existing paths above matched before/after hashes. REQ007 changed
concurrently through MA's approval addendum, as expected; it is not included in
the unchanged claim. No reset, checkout, deletion, staging or commit occurred.

## Commands and Test Evidence

Verification owner SE; date2026-10-10; Windows/PowerShell; local only.

| Check / command | Result and limit |
|---|---|
| `C:\Windows\System32\OpenSSH\ssh.exe -V` | OpenSSH_for_Windows_9.5p1, LibreSSL3.8.2 |
| `ssh-keyscan.exe -?` | Expected usage exit1; no target/network. Usage advertises `-O`, but support for a KEX override was not assumed or tested. No workaround implemented. |
| PowerShell Parser.ParseInput on all seven `powershell` fences | PASS7, no parse errors; no service/authentication blocks executed |
| Extract first fence's ScriptBlockExpressionAst; invoke only its parameterized preflight with invalid synthetic inputs | PASS7: missing fingerprint, short fingerprint, hostname rather than literal, option-like target, port0, port65536, option-like account. Every case threw the expected HOLD before file/native operations. |
| Same preflight with Test-Path/Get-Item/Get-Content and its sole ssh-keygen call replaced in memory by synthetic stubs | PASS10: valid arguments; wrong target, wildcard, wrong key type, duplicate pin, empty pin, oversize pin, fingerprint mismatch, keygen failure, malformed keygen output. No cryptographic key validation claimed from mocks. |
| Native `ssh.exe -G -F NUL` with documented options and synthetic target/path | PASS2: direct `IdentityAgent=none/BatchMode=no` and agent `IdentityAgent=//./pipe/openssh-ssh-agent/BatchMode=yes`. StrictHostKeyChecking=true, Ed25519, no password/keyboard-interactive, no forwarding, no host-key update. No connection. |
| `Get-FileHash -Algorithm SHA256 -LiteralPath <exact public source paths above>` | Seven preservation matches and new runbook identity recorded |
| Scoped document check, excluding fenced code from Markdown link matching | PASS: three owned documents,12 local links,9 dependencies,seven required metadata fields each,no trailing whitespace; runbook hash matches this EP. Full repository validator NOT RUN. The first ad-hoc link scan misread code syntax as a link; excluding fences resolved that checker-only false positive. |

Native option checks used only documentation address `192.0.2.10`, synthetic
account `operator`, nonexistent `C:/__WI016_fixture__/nonexistent-key` and
`C:/__WI016_fixture__/known_hosts`. These are not real targets or created files.
The fixture fingerprint was `SHA256:` plus43 `A` characters; mock public-pin
content used `AAAA`, not generated or loaded key material. The stubbed Get-Content
rejected every path except the public `known_hosts.first-host`. Successful output
was checked for strict verification, no agent and the exact synthetic target.

Minimal syntax reproduction (no native operations):

```powershell
$doc = Get-Content -Raw -LiteralPath deploy/single-vps/runbooks/first-host-setup.md
$blocks = [regex]::Matches($doc, '(?ms)^```powershell\r?\n(.*?)^```')
foreach ($block in $blocks) {
    $tokens = $null
    $errors = $null
    [void][Management.Automation.Language.Parser]::ParseInput($block.Groups[1].Value, [ref]$tokens, [ref]$errors)
    if ($errors.Count) { throw 'PowerShell snippet parse failure.' }
}
```

For negative reproduction, extract the first fence's parameterized script block
via ScriptBlockExpressionAst, invoke with the named synthetic cases above, and
expect HOLD before any native call. For public-pin fixtures, replace exactly the
single literal `& C:\Windows\System32\OpenSSH\ssh-keygen.exe` with a stub returning
exit status and fingerprint output; shadow metadata/public-file cmdlets in that
test scope. Never run the surrounding Read-Host/login/service blocks as a test.

No full Gradle/frontend suite or Linux shell/runtime check ran: product code did
not change. Synthetic checks do not prove VNC identity, key encryption, ACLs,
actual prompt handling, agent loading/cleanup, package scripts or remote startup.

## Attributed MA Progress, Not SE Execution

- Before rebuild: provider Ubuntu24.04; inbound TCP22 from one approved source;
  TCP22 reachable. TCP80/443/3306/8080 unreachable within1500ms from one Windows
  line only. No other-network/IPv6 proof. Outbound TCP80/443/53 and UDP53/123 to
  all IPv4 destinations, not an any-port rule or application egress isolation.
- Keyscan failed at unsupported sntrup KEX, not TCP. Native authentication-disabled
  diagnostic completed key exchange and intentionally rejected an untrusted host
  with exit255; no known_hosts mutation. No untrusted fingerprint is recorded here.
- User approved temporary Manual/start and native existing-key load, followed by
  exact owned-key removal and original Stopped/Disabled restoration. **NOT EXECUTED**;
  MA defers it until host identity is independently available.
- Latest user exactly approved no data and irreversible Ubuntu24.04 rebuild of
  the same existing instance at displayed0KRW. MA reports one confirmation click
  at **19:42 KST**, provider request confirmation and status **rebuilding**. REQ007
  approval addendum is MA-owned. This was not recreation or a new purchase.
- Old VNC/fingerprint evidence is invalidated. Rebuild completion/new notice
  delivery/recovery authentication/new independent host pin are pending. SSH login
  and package installation remain **NOT RUN**. Prior network observations cannot
  be reused as proof of the rebuilt instance's effective configuration.

## Risks / Rollback

The plain public host pin must stay in an owner-protected, non-linked local path;
operator metadata/ACL verification is a prerequisite, not a claim proven by
PowerShell syntax. Wrong or changed identity stops login. Native prompt, agent
ownership and exact cleanup require actual operator/MA receipts. Stopping the
Windows agent is not a substitute for verifying removal of the loaded identity.

Nginx masking and package effects require independent review and native evidence;
the runbook does not claim a mask suppresses every maintainer-script operation.
MySQL package-managed data initialization stays held under the DB exclusion.
No default site, application/DB activation, broad firewall, automatic reboot,
secret access, external send or backup operation is authorized by this receipt.

SE performed only public workspace/source reads, public official-document retrieval,
local syntax/mocked checks, native version/help/config-only calls and three additive
document writes. No browser UI, remote connection/command, private-key/config read,
local service mutation, purchase or provider action by SE. This is bounded activity
evidence, not an independent OS/network audit of every process on the shared machine.

Rollback before adoption is to withhold the plan. Preserve all existing files,
secrets and historical evidence; correct only owned document text through scoped
follow-up edits. Never undo MA's authorized rebuild or erase an old pin implicitly.

## Follow-ups

**MA: immediately generate/delegate WI-20261010-ATS-017 using
create-wi-handoff-packet.** Local preparation no longer blocks independent PG
review. Review the target/fingerprint guard, exact-key agent cleanup and restoration,
Nginx no-start plan and MySQL initialization hold. Then trigger WI018 DOCOPS for
current-state/index integration. SE does not create descendants or close REQ007.
Progress/final messages are the handoff channel; no separate chat was messaged.

## Related Documents

- [WI016 summary](../user/WI-20261010-ATS-016-summary.md)
- [WI016 handoff](WI-20261010-ATS-016-handoff.md)
- [First-host runbook](../../deploy/single-vps/runbooks/first-host-setup.md)
- [Win32-OpenSSH issue2140](https://github.com/PowerShell/Win32-OpenSSH/issues/2140): Primary keyscan defect report, checked2026-10-10.
- [OpenSSH options](https://man.openbsd.org/ssh_config): Native identity and strict-host checking contract.
- [Ubuntu MySQL installation](https://ubuntu.com/server/docs/how-to/databases/install-mysql/): Automatic start and initial local administrative account.
