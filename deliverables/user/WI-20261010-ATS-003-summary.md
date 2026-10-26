---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: pg
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-003.md
    reason: Approved preparation and no-purchase boundary
  - path: ../agent/WI-20261010-ATS-003-evidence-pack.md
    reason: Validation and attributed MA observations
---
# WI-20261010-ATS-003 Summary

## Current Result

**REQ003/WI003 preparation complete: local ACL/header/fingerprint checks PASS,
approved public key registered and verified, firewall saved but unattached.**
Overall deployment remains HOLD. No server was created, attached, or purchased.

- MA reports `atstudio-bootstrap` saved with zero attached servers, ICMP off,
  one administrator-source TCP 22 inbound rule, and no web/DB/application ingress.
- MA reread the five saved outbound rules: TCP 80/443/53 and UDP 53/123.
  No blanket ALL rule, geography rule, or bot rule is configured.
- [SSH helper](../../scripts/deployment/prepare-operator-ssh.ps1) uses the
  existing Windows OpenSSH and a new protected directory outside the repo.
  It refuses existing destinations and fails closed on unexpected file ACLs.
  The helper remains frozen; its source and recorded hash are unchanged.
- PG syntax, read-only preflight and bounded synthetic safety checks passed.
  MA independently reviewed the entire helper and repeated syntax/preflight.
- MA's later diagnostic confirmed both new key files exist, the private-key
  encryption header passes, and the public Ed25519 fingerprint is valid.
  Native Windows file ACLs differed from the helper's strict current-user-only
  expectation, causing NOT READY. This was not proof of a missing key.
- Under explicit user approval, MA repaired only these two file DACLs and
  rechecked them: one current-user Allow FullControl entry, protected DACL,
  current-user ownership, unchanged parent ACL, size and last-write timestamps.
  Encryption-header and unchanged expected public-fingerprint checks passed.
- The first owner/access Set-Acl attempt failed with SeSecurityPrivilege; MA
  confirmed unchanged ACLs and used access-section-only updates without
  elevation. No key regeneration, content rewrite or deletion occurred.
- After the user's exact action-time approval, MA registered
  `atstudio-admin-20261010`, observed the success alert and one saved entry
  (provider-displayed creation time: 2026-10-10 16:03:03). Read-only reopening
  confirmed exact saved public-key bytes and SHA-256 fingerprint match the
  approved key; MA cancelled without saving an edit.

## Owner Action / Boundaries

The earlier human-input waiting state is superseded by the dated ACL recovery
result. Do not rerun the helper, re-enter a passphrase, or overwrite the keys
under this records-only continuation. Keep private material out of chat.

The separately approved public-key registration is complete. No private key or
passphrase was read or submitted during registration.
Private/public correspondence through decryption and remote SSH login are NOT RUN.
The saved firewall does not protect a server until later approved association
and actual enforcement checks. Public website access remains a separate step.

MA's official-source check reports ELCAP is free. The helper uses installed
Windows tools without a paid dependency. This does not approve server fees or
other products, and no purchase occurred in this WI.

## Verification Limit

2026-10-10 local Windows / PowerShell 7.6.5: PG's static/in-memory checks and
MA's actual local ACL/header/public-fingerprint verification are distinct.
Neither proves decrypted key-pair correspondence, remote SSH access, provider
enforcement, or end-to-end helper success without recovery. Operational results
belong to MA; PG did not access keys, browser, network, or private contexts.
MA's provider readback verifies the registered public key only. REQ003/WI003
preparation is closed; Blocks none, no next WI is automatically started.

The earlier pending receipts, failures and helper hash remain in the dated
Evidence Pack history. This closeout does not authorize server purchase or
association. Overall deployment, remote SSH, off-PC key backup/recovery and
decrypted pair verification remain separate and are not claimed complete.

## Related Documents

- [Approved REQ](REQ-20261010-ATS-003.md)
- [Evidence Pack](../agent/WI-20261010-ATS-003-evidence-pack.md)
