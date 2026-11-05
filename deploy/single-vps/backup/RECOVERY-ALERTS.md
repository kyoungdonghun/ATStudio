---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: se
category: guide
status: local-candidate
---
# Recovery inventory and independent alerts

Trace: REQ-20261026-ATS-006 / WI-20261026-ATS-017. These callable modules are not installed, scheduled, or authorized to access live secrets by their existence. Both default CLIs are inert.

## Explicit inventory

`scripts/deployment/backup/recovery_inventory.py` accepts an exact file allowlist, not a directory scan. Each entry has `path`, `category`, optional `key_ids`; the caller supplies the complete retained billing key ID declaration. Categories are settings, service, proxy, tls, billing_keyring, and backup_recovery. These six categories do not prove that every operational dependency was selected. Include actual effective configuration, service/proxy/TLS material, mail/provider recovery references, and every retained decryption key required by old billing records. Template filenames and the active key ID do not establish runtime provenance or semantic completeness.

The collector opens POSIX paths by directory descriptors with no-follow flags, rejects writable/untrusted parents, nonregular/multiply-linked files and group/world-readable secrets, bounds file/total bytes, hashes bytes without returning content, and compares descriptor/path identity before/after reads. Run as the approved owner of private files (normally root on the target). Root or another process with the same privileges remains a trusted boundary. Concurrent changes fail closed; this is not an application-consistent capture or proof of recoverability. Windows ACL verification is unavailable and deliberately fails closed. Native Linux execution has not been tested in this work item.

Every entry defaults to `private: true`. An explicitly reviewed nonsecret service/proxy/public-certificate file may set `private: false` to permit read access for other users; group/world writes and special permission bits remain forbidden. Do not chmod deployed files merely to satisfy this collector, or classify secrets as public to bypass it.

The returned `private_manifest` includes paths and hashes: store it only through an approved private writer. Only `public` aggregate counts/codes may be published. The module does not write a manifest, parse/decrypt key contents, or claim all keys are semantically present. No real allowlist or secret paths are preseeded.

## Independent receipt check and delivery

`scripts/deployment/backup/alert_delivery.py` reuses `backup_protocol.status_report`. Supply the original capture timestamp, schedule window and verified provenance from a trusted transport; copying a receipt must never refresh its timestamp. Missing/stale/missed receipt conditions can be detected on a separate host even if the source stops updating, but there is no network host-health probe here. Arbitrary receipt JSON does not become trusted live evidence. The caller connects the dispatcher result through fixed `dispatch_code` values `DISPATCH_FAILED` / `DISPATCH_UNCERTAIN`; the dispatch ledger does not replace the guarded capture state. A real trusted polling/receipt transport and independent execution host still need integration.

`check_with_dispatch_ledger` actually reads the separate dispatch ledger through its reviewed `ledger_failure_code` interface and maps failure/uncertainty into the alert. It does not replace receipt provenance verification.

Use `notify` with a stable schedule window ID: it derives the event from that window, SMTP destination configuration and sorted fixed alert codes, excluding polling time. Repeated polling of that event cannot resend. Low-level `deliver` accepts a prederived event ID. Delivery also requires a private POSIX ledger directory, SMTP host/port465, sender/recipient, and private credential reference resolved in memory by the caller. Reuse the confirmed ID1 operations mailbox and existing Gmail reference; no new mailbox input is required. Window identity must remain stable across restarts; never mint a new window merely to bypass a held attempt. No actual credential resolver or private path is bundled.

An exclusive claim is flushed and fsynced, including its directory, before credential resolution or SMTP connection. A claim is never automatically deleted. Existing claims return `RESERVED_HOLD`; exceptions after reservation return `OUTCOME_UNKNOWN_HOLD`, even when failure might have occurred before submission. Reconcile manually using independent evidence before any separately controlled resend. Preserve ledger files across process/host restart and backup recovery; a missing or rolled-back ledger invalidates duplicate prevention. Native durability under power loss and concurrent execution remain target verification work.

SMTP uses certificate- and hostname-verifying implicit TLS, fixed subject/body codes, bounded timeout, and rejects header injection. No server responses, addresses, event IDs, credentials, paths or hashes appear in the public delivery result/body. `SMTP_ACCEPTED` means SMTP submission acceptance only; recipient receipt/acknowledgment remains `UNVERIFIED`. No real message was sent during these tests.

## Activation remains separate

Synthetic filesystem/SMTP tests establish local branches, not live recovery or alert delivery. Before activation, connect reviewed native hooks, persistent event/attempt ledgers, trusted fresh receipt transport, independent execution location, cost/target gates and a real acknowledgment exercise. Google Drive remains an encrypted off-provider storage candidate, not a monitoring host. Client authorization/quota, separate password recovery and isolated restore proof remain necessary; no install/upload/connection is performed here.

## WI-20261028-ATS-004 runner integration

See `STATUS-TRANSFER.md` for the explicit protected runner/config contract. No real SMTP, independent monitor host, transport or external supervisor was activated. Test mode separates namespace, source-state directories, receipt stores, window/event IDs and delivery ledgers; messages carry a TEST prefix. Existing production calls without `test_id` retain their prior event identity and message wording. Unknown SMTP outcomes retain the original reservation and cannot automatically resend the same event. SMTP acceptance still does not prove mailbox receipt. A held reservation does not prove success; reconcile it independently without deleting historical claims.

Checker exceptions expose fixed exit-2 failure rather than an apparently healthy service. Attention also exits 2 even after SMTP acceptance, allowing a separately deployed supervisor to observe it. Supervising only on the source host cannot establish independent failure detection. Host selection, authenticated file movement, service enablement, supervisor route, mailbox receipt and native failure injection remain separately gated work.
