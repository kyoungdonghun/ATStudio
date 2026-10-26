---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: pg
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-003.md
    reason: Approved no-purchase security preparation
---
# WI-20261010-ATS-003 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-003
REQ: REQ-20261010-ATS-003
Agent: pg
Depends On: -
Blocks: -

[WI SUMMARY]
Why: Prepare safe owner-controlled credentials and an unattached bootstrap firewall, without creating or purchasing a server.
Scope: Implement a minimal Windows PowerShell SSH-key preparation helper at scripts/deployment/prepare-operator-ssh.ps1; concise security evidence and summary. MA owns browser actions and operator handoff. No existing source/config changes.
DoD: No-overwrite, user-only external key directory, native hidden passphrase entry by human, encrypted Ed25519 key validation without secret disclosure, read-only CheckOnly mode, clear pending vs complete boundaries.
Forbidden: Do not generate actual keys, launch UI, read existing keys/secrets, contact cloud/browser/network, register credentials, change firewall/SSH configuration, create server, accept agreements, buy, commit/push or delete files. Do not collect passphrase into agent logs/arguments/ENV/clipboard. Do not amend any prior artifact.

[ACCEPTANCE CRITERIA]
- [ ] Existing Windows OpenSSH is used. No library installation.
- [ ] Helper defaults to user-profile .ssh/atstudio-iwinv/id_ed25519, not the repository. Refuse overwrites and reparse-point paths; do not alter existing .ssh permissions.
- [ ] Create only a dedicated new directory with protected ACL for current user; keys inherit safe permissions.
- [ ] Interactive ssh-keygen -t ed25519 -a 100 uses user-entered passphrase, not a blank or agent-provided one. Never return private key/passphrase to model or transcript.
- [ ] Validate that the resulting OpenSSH key is encrypted, report only public fingerprint and public path; never print base64/private bytes. If a blank passphrase was used, report NOT READY, preserve files, do not auto-delete or register.
- [ ] CheckOnly is read-only and no key generation in agent tests. Syntax and safe-argument/overwrite checks documented.
- [ ] MA independently reviews script before running it in a separate visible owner-operated terminal. Do not start it yourself.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
Tier 2:
- docs/index.md#current-decision-entry
- deploy/single-vps/README.md
- deploy/single-vps/runbooks/deployment.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
Task:
- deliverables/user/REQ-20261010-ATS-003.md
- MA official browser evidence and source URLs supplied in assignment

[OUTPUT CONTRACT]
- scripts/deployment/prepare-operator-ssh.ps1
- deliverables/agent/WI-20261010-ATS-003-evidence-pack.md
- deliverables/user/WI-20261010-ATS-003-summary.md
- Current REQ progress only on MA instruction; actual owner/key/firewall steps may remain pending.

[TRACEABILITY REQUIREMENTS]
- Base main, HEAD 58b1011dad796017e47dbcc82a4225ca449bcdf6 with unrelated dirty/untracked work. Preserve all prior files. Use apply_patch for manual edits.
- No IP addresses, private keys, passphrases, account identities or key bodies in repo records.
- Official provider console warns outbound ALL ports/IP can bypass inbound rules: do not recommend blanket egress allow. Bootstrap ingress only administrator SSH; no public web, DB, application ports.
- Tests must not generate real keys or alter ~/.ssh. Capture exact review limits. Blocks none; no downstream WI.

## Approved ACL Recovery Addendum (2026-10-10)

The user approved correcting only the two newly generated key-file ACLs and
rechecking them ("엉 진행해."). MA owns the exact-path local ACL operation;
PG owns the current REQ/evidence/summary reconciliation. This supersedes the
earlier input-pending observation, not the no-purchase or no-registration gate.

- Exact targets: `%USERPROFILE%/.ssh/atstudio-iwinv/id_ed25519` and its `.pub`.
- In scope: current-user-only file ACLs, owner/reparse/path checks, metadata
  preservation checks, bounded encryption-header and public-fingerprint checks.
- Out of scope: key regeneration, content rewriting, deletion, passphrase input,
  existing keys, parent ACL changes, helper changes, browser or cloud mutations.
- PG must not read or alter the key files; attribute operational results to MA.
- Preserve historical records and script hashes. Append dated recovery evidence
  and update only the current-state summary/REQ boundary after MA results arrive.
- Registration, private/public key correspondence via decryption, and remote
  login remain unverified. Blocks none; the enclosing REQ remains active.

## Approved Public-Key Registration Closeout (2026-10-10)

The user approved the exact public-key fingerprint, destination iwinv account,
and name `atstudio-admin-20261010` at action time. MA owns browser execution;
PG reconciles only the existing REQ, summary and Evidence Pack using this skill
packet and `create-wi-evidence-pack`. No new WI or source change is required.

- MA observed the registration success alert and one saved entry with provider
  creation time `2026-10-10 16:03:03`. Read-only reopening of the entry confirmed
  the saved public-key bytes and SHA-256 fingerprint match the approved key;
  MA cancelled that inspection without saving an edit.
- No private key or passphrase was submitted. No server creation, association,
  payment, firewall mutation, local key mutation, or remote login was performed.
- PG scope: update the current REQ/summary to preparation complete, append a
  dated registration addendum, and add a current-result pointer above historical
  evidence. Preserve historical failures, earlier pending observations and hashes.
- Keep raw keys, fingerprints, personal identifiers and source IPs out of repo
  records. Privacy-safe list screenshot remains outside the repository at
  `%TEMP%/atstudio-sshkey-registered-20261010.jpg`.
- This closes only REQ-20261010-ATS-003 preparation. Overall deployment HOLD,
  purchase approval, server association, remote SSH, off-PC key backup/recovery,
  and decrypted pair verification remain separate and not claimed complete.
- Blocks: none. Do not access real keys, browser, provider APIs or helper code.
