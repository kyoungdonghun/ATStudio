---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved local private staging amendment
---

# WI-20260909-ATS-021 Handoff

[WI HEADER]
REQ: REQ-20260909-ATS-004
Agent: se
Depends On: WI-20260909-ATS-020
Blocks: WI-20260909-ATS-022

[WI SUMMARY]
Why: The user will upload the private recovery materials after work; prepare one clearly labelled sibling folder without changing the existing environment.
Scope: Prepare a new `ATStudio-private-handoff-20260909-*` directory immediately outside this repository under its parent project directory. Use user/SYSTEM restricted ACL before writing private bytes. Copy only recovery-relevant local settings, key material, current public/private roots, the original 205-member archive and selected receipt metadata. Prepare a read-only native MySQL logical dump of the verified current loopback database. Provide a SHA-256 inventory, verification receipt and short Korean operator instructions. Do not create a new unencrypted aggregate ZIP: the user asked for local folder staging, and STD-001 requires portable encryption before sensitive compression/cloud storage. Keep the existing original ZIP unchanged as historical input. Local preparation scripts may live outside Git; do not introduce a product backup subsystem.
DoD: Every included copy and archive member verified; omissions/known old missing files and non-atomic DB/filesystem capture disclosed. No source or live settings modified. Public evidence contains counts/status only.
Forbidden: Source file movement/deletion, DB CREATE/DROP/restore/DDL/data changes, service stop/restart, provider/mail operations, account changes, cloud upload, outputting credentials, copying Codex/browser account sessions, importing acceptance settings into live runtime. No encryption key invention or DPAPI-as-portable-backup claim. No new persistent runtime or development dependencies.

[ACCEPTANCE CRITERIA]
- [ ] Resolve source/destination and reject reparse points/path traversal; destination outside repository, new unique folder only.
- [ ] Preserve config bytes without printing values. Retain local YAML and relevant external environment/key bundle separately as raw sources with NOT AUTO-IMPORT warnings.
- [ ] Verify actual DB/root selection; do not assume the external acceptance bundle is current. Capture appropriate overrides privately and preserve historical key IDs needed by ciphertext where identifiable via read-only queries.
- [ ] Dump with installed native mysqldump, no secrets in argv/logs; verify loopback target, transactional engine support, schema/table coverage, native exit code and dump completion. No destructive command is executed. Record DB/filesystem consistency limitations instead of claiming an atomic live snapshot.
- [ ] Verify source-to-copy hashes, file tree stability before/after capture and original ZIP entries after copying. Include original 205-member ZIP unchanged and its manifest. No regenerated caches or broad unrelated folders.
- [ ] Restrict ACLs; mark plaintext/local-only status prominently. State cloud upload and portable encryption/recovery access remain user work, not completed.
- [ ] English public WI evidence and summary; private user handoff README may be Korean for usability. Keep all private values out of public files.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md, docs/standards/documentation-standards.md, docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md.
Tier 2: scripts/reconstruction/README.md, docs/design/runtime-storage-operations.md, docs/registry/development-history-recovery-20260909.md, scripts/database/README.md.
Context: approved REQ004 local staging amendment. Current main HEAD 69a5145. Source/root is the current repository. Native MySQL tools verified at `C:/Program Files/MySQL/MySQL Server 8.0/bin/`. Source local YAML is ignored; js-yaml already available via frontend/node_modules for structured private parsing. Parent inspected URL only: localhost:3306/atstudio. Do not pass its createDatabaseIfNotExist JDBC property to backup clients.
Private original archive: %LOCALAPPDATA%/ATStudio/archives/v1-artifacts-20260909-084405/ (original ZIP 205 members).
Private recovery receipts: %LOCALAPPDATA%/ATStudio/archives/development-recovery-20260909-094234/ (select small metadata, omit source clones/build caches).
Private environment source: %LOCALAPPDATA%/ATStudio/acceptance-backend-environment.json; includes bootstrap flags and secrets, NOT an automatic current environment import.
Existing media: workspace uploads (78 files total across current roots, about 276 MB in latest inventory); private-uploads existence must be reported accurately. Known earlier storage audit reported 10 historical missing references; preserve, do not repair. Current runtime PID evidence is in prior final-preservation receipt; refresh identities before final comparison.

[OUTPUT CONTRACT]
Own: new sibling handoff directory, minimal one-time preparation script there, WI021 evidence pack and user summary. You may amend only the local-staging receipt in REQ004 and the private staging status paragraph in scripts/reconstruction/README.md after actual evidence. Do not commit/push.
Use create-wi-evidence-pack skill. Return final folder/archive paths, counts/bytes/hashes, DB backup status, ACL checks, precise limitations. Never print secrets or raw dump rows. Independent WI022 follows immediately.
