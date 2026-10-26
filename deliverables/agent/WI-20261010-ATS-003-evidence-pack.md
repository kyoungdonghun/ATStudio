---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: pg
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-003-handoff.md
    reason: Approved delegation and no-purchase boundary
  - path: ../user/REQ-20261010-ATS-003.md
    reason: Scope and current operator-input gate
---
# Evidence Pack: WI-20261010-ATS-003

> **Latest result:** [Public-key registration closeout](#public-key-registration-closeout-2026-10-10)
> closes REQ003/WI003 preparation only. The public key is registered/verified;
> the firewall remains unattached and overall deployment is HOLD. Earlier
> summaries, checklists and pending receipts below are historical observations.

## Summary

The reviewed native Windows SSH helper is ready. MA saved an unattached ELCAP
policy and launched the helper in an owner-operated console. **Human passphrase
input is pending; no key readiness, registration, server creation, or purchase
is claimed. REQ and WI remain active.** Verification date: 2026-10-10 KST.

## Scope / DoD Check

- [x] Existing Windows OpenSSH only; no dependency installation.
- [x] Fixed external destination under the current Windows profile:
  `.ssh/atstudio-iwinv/id_ed25519`. Existing destinations and reparse points fail
  closed. Existing `.ssh` ACLs are not changed; a missing parent requires review.
- [x] A new dedicated directory receives a protected current-user-only ACL at
  creation. `CreateDirectoryW` refuses an existing destination, including an
  incomplete previous attempt. ACL construction tested in memory only by PG.
- [x] Native `ssh-keygen -q -t ed25519 -a 100` prompts locally. No `-N`, captured
  passphrase, clipboard, environment injection, or agent-provided passphrase.
- [x] Generated-file ACL checks, bounded OpenSSH cipher/KDF header validation,
  and public Ed25519 fingerprint validation are implemented. Unencrypted keys
  fail readiness; failures preserve files and require review, never auto-delete.
- [x] `-CheckOnly` has no key reads, writes, compilation, or process launch.
- [x] MA reports independent full-file review, syntax PASS, CheckOnly PASS.
- [x] MA reports saved firewall, inbound and outbound rereads, zero attachments.
- [ ] Owner finishes native passphrase entry and reports `LOCAL KEY READY`.
- [ ] Actual generated-file ACL/encrypted-header/fingerprint checks complete.
- [ ] Exact public-key fingerprint confirmation and provider registration.
- [ ] Server association and remote login/security verification: not authorized
  by this no-purchase step; require later named-target approval.

## Current Operator Boundary

| Area | Evidence owner and observed state | Limit |
|---|---|---|
| Helper | PG source and local read-only/synthetic checks; MA independent review | Actual generation path not executed by PG |
| Interactive handoff | MA reports detached visible PowerShell launch and native ssh-keygen child present | Directory exists; public key not yet present at that observation. Waiting for owner input, not a successful key-generation receipt |
| ELCAP policy | MA saved `atstudio-bootstrap`; list shows 0 attached servers and ICMP No | Saved policy is not applied to a server |
| Saved inbound | MA reread exactly one TCP 22 rule restricted to the current administrator source address | No address literal retained here; source may change with the connection |
| Saved outbound | MA reread exactly TCP 80/443/53 and UDP 53/123, any IPv4 for these ports only | No blanket ALL rule. No runtime connectivity or enforcement proof |
| Other policy controls | MA reports no geography/bot rules | No security-product effectiveness claim |
| Server / payment | MA reports no server created, attached, applied, or purchased | Purchase remains expressly excluded |

The provider console warning about blanket outbound permissions bypassing
inbound restrictions is MA-supplied live evidence. Do not replace the five
specific egress rules with an ALL-ports/IP rule. Public web ingress and direct
DB/application ingress are not part of this bootstrap policy.

Privacy-safe MA screenshot receipt: `%TEMP%/atstudio-firewall-created-20261010.jpg`
(list crop without account identifiers). PG did not browse, contact the provider,
read keys, or independently inspect the saved policy. Browser results above are
explicitly attributed to MA, not inferred from this script's tests.

## Reference Documents (Tier 0-2)

Handoff header, approval, project tag ATS, and input pointers were confirmed.
Truncated context was supplemented with pertinent sections, not assumed loaded.

| Tier | Document | Scope used |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, privacy, language, traceability |
| 0 | `docs/standards/development-standards.md` | Headers and architecture/context rules |
| 0 | `docs/standards/documentation-standards.md` | Metadata, language, links, evidence records |
| 0 | `docs/standards/glossary.md` | Headers and canonical REQ/WI/security terminology |
| 1 | `docs/policies/security-policy.md` | Classification and storage/logging rules |
| 1 | `.claude/agents/pg.md` | PG ownership and two-set deliverables |
| 2 | `docs/index.md#current-decision-entry` | Current deployment HOLD and evidence boundaries |
| 2 | `deploy/single-vps/README.md` | External secrets and separate purchase/target gates |
| 2 | `deploy/single-vps/runbooks/deployment.md` | Operator, SSH recovery and host approval gates |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Evidence format and handoff prerequisite |
| 2 | `.agents/skills/validate-docs/SKILL.md` | Document integrity check |

Injection rules: `.claude/config/context-injection-rules.json`; assignee `pg`,
task type security, required tiers `[0, 1]`. Tag source:
`.claude/config/workspace.json`. Approved delegation:
[handoff](WI-20261010-ATS-003-handoff.md).

Provider facts supplied by MA, not re-fetched by PG:
[free ELCAP](https://docs.iwinv.kr/service/compute/server-overview/) and
[SSH key guidance](https://docs.iwinv.kr/docs/authentication-key/generate-ssh%20keys-and-register-servers/).
MA reports current console support for Ed25519, a maximum of five account keys,
and key selection during server creation. No price guarantee for other products.

## Evidence Pointers

- [Helper](../../scripts/deployment/prepare-operator-ssh.ps1): lines 18 and 33
  path/ACL guards; 47 and 61 bounded header parsing; 111 read-only early return;
  142/156 atomic protected directory creation; 169 native prompts; 173-181
  readiness and public-only reporting; 183 generic failure without raw exceptions.
- Helper SHA-256 at freeze:
  `9562BD1472A97AF781E795A4C1221B9C16CEB5EAF0DA076D37B87781A018F5D0`.
- [User summary](../user/WI-20261010-ATS-003-summary.md).
- [REQ progress](../user/REQ-20261010-ATS-003.md).
- Source base: `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`.
  Prior dirty/untracked work preserved; no commit or push.

## Commands & Outputs / Tests

Environment: local Windows, PowerShell **7.6.5**; evidence owner PG unless noted.

| Check | Result and scope |
|---|---|
| `Parser.ParseFile` on the helper | PASS, zero PowerShell parse errors |
| `& ./scripts/deployment/prepare-operator-ssh.ps1 -CheckOnly` | Exit 0; `PREFLIGHT PASS: no writes or key reads. No key is ready or registered.` |
| Repeated CheckOnly plus parent last-write timestamp comparison | PASS, unchanged; no directory contents or keys read |
| AST-extracted native directory declaration compiled with `Add-Type` | PASS; `CreateNew` never invoked by PG |
| In-memory directory ACL descriptor | PASS: protected, exactly current-user allow rule; no ACL applied |
| AST-extracted bounded string parser | PASS: valid field and four malformed/oversized/truncated synthetic fields |
| AST-extracted header parsing statements, excluding file reader | PASS: supported cipher/KDF accepted; four synthetic unsafe/malformed headers rejected, including `none` |
| CheckOnly with mocked destination-exists metadata | PASS: exit 1; no filesystem fixture written |
| Existing Windows OS junction metadata only | PASS: reparse point rejected; no traversal or key fixture |
| MA independent whole-file review / syntax / CheckOnly | MA reports PASS before interactive handoff |
| Actual ACL creation / encrypted key / public fingerprint | PENDING owner input; not a PG test |
| Remote login, provider enforcement, capacity, billing | NOT RUN |

Parser and mock tests used only in-memory synthetic header metadata, not real
keys or disk key fixtures. No private bytes or passphrase were emitted. The
header check proves the generated file advertises the expected encryption/KDF;
it does not decrypt the key, measure passphrase strength, or prove remote login.

## Risks / Rollback

- Run only in the separate owner-operated console after MA review; do not enter
  secrets in chat, transcript, automation inputs, command arguments, or ENV.
- Helper edits are frozen while MA's interactive invocation is running. This
  record does not authorize PG to inspect its arguments, files, or private input.
- Blank passphrase, interruption, or validation failure preserves the dedicated
  directory. Reruns refuse it. Do not delete/overwrite it or repair broad ACLs;
  obtain an exact-path operator recovery decision.
- Local current-user-only ACLs do not protect against a compromised owner
  account or privileged administrators. Ancestor reparse checks are preflight,
  not protection against hostile concurrent path changes by privileged code.
- Safe rollback now is to withhold public-key registration and server purchase.
  Any firewall removal or private-file cleanup requires explicit scope approval.
- If the administrator's public source changes, review the specific allow rule;
  do not open SSH to the whole internet as a workaround.

## Follow-ups / Chain

Blocks: none. REQ/WI remain **active / awaiting human input**. MA collects only
the readiness result and permitted public fingerprint, verifies registration
separately, and records subsequent state. No downstream WI is auto-triggered.

At the user's explanation/pause boundary, MA explained the native input window
versus provider SSH registration. PG finalized only these records; the helper
remains frozen. No further tests, research, process control, or key inspection
were conducted for this closeout. Document validation after record creation was
not run under that instruction. An open native window is not evidence of a
generated/ready key, registration, or payment. Overall work remains active.

## Related Documents

- [Approved REQ](../user/REQ-20261010-ATS-003.md)
- [Delegation handoff](WI-20261010-ATS-003-handoff.md)
- [User summary](../user/WI-20261010-ATS-003-summary.md)

## ACL Recovery Addendum (2026-10-10)

**Current boundary: local ACL, encrypted-header and public-fingerprint checks
PASS after approved recovery. Public-key registration remains pending; REQ/WI
remain active.** This addendum supersedes earlier input-pending observations,
not the historical test records, failed operations, helper hash, or purchase
and registration gates. Scope is the approved ACL recovery addendum in the
existing [handoff](WI-20261010-ATS-003-handoff.md).

Evidence owner: MA, actual local Windows operation on the two newly generated
key files only. PG read the addendum and updated records; PG did not access or
alter real key files, run the helper, collect private contexts, or independently
repeat the operational checks.

### Diagnostic and Failed First Attempt

- MA confirmed both files exist, current-user ownership, encryption-header
  PASS and a valid expected public Ed25519 fingerprint. The private file had
  SYSTEM, Administrators and current-user ACL entries; the public file also
  had Everyone Read. The helper's strict user-only check therefore returned
  NOT READY. Key generation and ACL readiness were separate outcomes.
- The user explicitly approved permission repair/recheck of these exact two
  files. Parent ACL changes, key regeneration and content rewriting were excluded.
- MA's first `Set-Acl` attempt with a new FileSecurity containing owner/access
  failed with `SeSecurityPrivilege`. Read-only recheck confirmed unchanged
  ACLs: three private-file entries and four public-file entries. No elevation
  was used; the failed attempt is retained as evidence.

### Successful Access-Only Recovery

- Before mutation, MA validated the exact parent/targets, no reparse points or
  hard links, current ownership, nonempty files, passing encrypted header and
  the expected public fingerprint.
- MA obtained access-only FileSecurity via
  `GetAccessControl(FileInfo, Access)`, replaced the DACL with a protected
  current-user-only FullControl rule using `SetSecurityDescriptorSddlForm`
  for the Access section, and applied `FileSystemAclExtensions.SetAccessControl`
  to the exact two files. No owner or SACL change was requested.
- MA rechecked both files: exactly one current-user Allow FullControl entry,
  protected DACL and current-user ownership. Parent SDDL, file sizes and
  LastWriteUtc values remained unchanged. Metadata equality is not a separate
  full-file byte-hash proof; no content-rewrite operation was performed.
- Encrypted-header verification passed and the public fingerprint matched the
  earlier expected value. No SID, fingerprint literal, private bytes, account
  identifier, passphrase, or source IP is copied into repository records.
- MA reports helper SHA-256 unchanged from the recorded
  `9562BD1472A97AF781E795A4C1221B9C16CEB5EAF0DA076D37B87781A018F5D0`.
  No helper change, new helper, key fixture, or test framework was introduced.

### Remaining Boundary

No key regeneration, content rewriting, passphrase input, deletion, elevation,
parent ACL changes, browser/network operations, registration, server creation,
attachment, or payment occurred in this recovery. The previously verified
firewall remains saved/unattached; this records-only continuation did not
reinspect the provider. Source HEAD remains the MA-supplied `58b1011` baseline;
unrelated dirty work is preserved.

The private/public pair was not verified through decryption and remote SSH
login is NOT RUN. A valid public fingerprint plus an encrypted private header
does not independently prove that correspondence. Registration requires its
separate confirmation. The helper remains frozen and its earlier NOT READY
is not relabelled as an end-to-end helper PASS. Blocks none.

Records-only validation by PG after reconciliation:
`python .agents/skills/validate-docs/scripts/validate_docs.py` PASS, exit 0;
741 supported traceability IDs, Tier 0 presence, internal links and document
index all passed. No further key/helper tests or operational checks were run.

## Public-Key Registration Closeout (2026-10-10)

**REQ-20261010-ATS-003 and WI-20261010-ATS-003 preparation complete. Overall
deployment remains HOLD.** This current result supersedes earlier input and
registration pending receipts without rewriting their history, the ACL failure
or the preserved helper hash.

### Approval / Evidence

- The user gave exact action-time approval for the named key, SHA-256 public
  fingerprint and destination iwinv account. MA owns that approval receipt and
  browser operation; no key bytes, fingerprint literals, personal identifiers
  or source IPs are copied into these repository records.
- MA submitted registration, observed the success alert and one saved entry
  named `atstudio-admin-20261010`, with provider-displayed creation time
  `2026-10-10 16:03:03`.
- MA reopened the saved entry read-only, obtained the public textarea value,
  and compared its exact public bytes and Node crypto SHA-256 fingerprint with
  the approved key. Both comparisons were true. MA cancelled that inspection
  without saving an edit. This verifies provider storage of the approved public
  key, not decrypted correspondence to a private key or successful SSH access.
- Privacy-safe receipt: `%TEMP%/atstudio-sshkey-registered-20261010.jpg`, a list
  screenshot outside the repository without personal data or key body.
- No private key or passphrase was read/submitted in this registration step.
  No server creation, association, payment, other cloud change, firewall change,
  local key mutation or remote login occurred.

### DoD / Closeout Boundary

- [x] Earlier MA local ACL, encrypted-header and public-fingerprint checks PASS
  after the explicitly approved recovery; original failure remains recorded.
- [x] Action-time public-key registration approval, saved entry and approved-key
  readback match confirmed by MA.
- [x] Previously saved/reread bootstrap firewall remains unattached; no server
  or purchase is claimed.
- [x] PG reconciled only this Evidence Pack, the existing REQ and user summary
  using `create-wi-evidence-pack` and the latest approved handoff section.

PG reused the loaded Tier 0/security context and read the latest handoff. PG did
not access real keys, helper code, browser, provider APIs or network and did not
repeat the MA operation. Source HEAD remains the MA-supplied `58b1011` baseline;
the helper and recorded hash are unchanged, unrelated dirty work is preserved.

Purchase approval, server association, remote SSH/enforcement, off-PC key
backup/recovery and decrypted key-pair verification are separate future gates,
not satisfied by this preparation closeout. No pending in-scope WI remains;
Blocks none, no downstream WI. Overall deployment is not complete. No commit,
push, deletion or rollback operation was performed.

PG closeout document validation:
`python .agents/skills/validate-docs/scripts/validate_docs.py` PASS, exit 0;
741 traceability IDs, Tier 0 presence, internal links and document index passed.
No new research, operational checks, code tests or helper changes were performed.
