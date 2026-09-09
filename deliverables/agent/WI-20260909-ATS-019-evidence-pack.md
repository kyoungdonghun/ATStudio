---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260909-ATS-019-handoff.md
    reason: Approved ownership and acceptance contract
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved reconstruction scope and WI020 dependency
---

# Evidence Pack: WI-20260909-ATS-019

## Summary (one-liner)

Source reconstruction entry points, a bounded read-only PowerShell preflight,
synthetic failure-path tests and explicit private restoration quarantine gates.

## Scope / DoD Check

- [x] Root README links active rules, current SoT and historical retention.
- [x] Guide derives tool/config contracts from current source, distinguishes
  ordinary local development, fresh guarded acceptance and retained-data restore.
- [x] Private settings are not read; preflight starts no processes, servers,
  DB connection, dependency installation, tunnel, Provider or mail action.
- [x] Complete billing keyring, paired DB/public/private snapshot, off-device
  encrypted backup, independently recoverable decryption access and DPAPI
  limitations are explicit; no backup destination or completed restore invented.
- [x] Actual unconditional scheduler/startup paths documented; no global kill
  switch invented. Keep application disconnected until separately approved containment.
- [x] Narrow `/private-uploads/` ignore safeguard authorized by MA follow-up;
  virtual-path probes verify matching and no broader source/nested match.
- [x] Synthetic normal/missing/invalid/reparse/CLI failure tests pass.
- [x] After WI018 created its history registry, added direct links from README,
  guide and docs/index, a required preflight input, and its missing-file test.
- [ ] WI020 independent review, clean staged-source validation and final Git
  integration remain MA/QA-owned; this evidence does not close REQ004.

## Reference Documents (Tier 0-2)

Injected/read context follows WI019 INPUT POINTERS and the direct execution instruction.

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | STD-001 approval, sustainability and private backup boundary |
| 0 | `docs/standards/development-standards.md` | STD-002 minimal tooling and testing |
| 0 | `docs/standards/documentation-standards.md` | STD-004 English technical documentation and traceability |
| 0 | `docs/standards/glossary.md` | STD-005 Source of Truth, historical and WI terminology |
| 1 | `docs/policies/security-policy.md` | Private settings, explicit local import, credentials and keyring |
| 1 | `docs/policies/archive-policy.md` | Preserve original history; identify current versus historical claims |
| 2 | `docs/design/runtime-storage-operations.md` | Paired roots, strict integrity audit and recovery boundary |
| 2 | `scripts/acceptance/README.md` | Test-only bootstrap, allowlisted bundle and launcher boundary |
| 2 | `scripts/database/README.md` | Fresh-only guarded disposable schema; no retained-data migration |
| 2 | `docs/payment/index.md`, `docs/SR/SR-93.md` | Current payment/source versus production gates; dated missing media |
| 2 | `deliverables/user/REQ-20260909-ATS-004.md` | WI018/WI019 ownership and WI020 successor |

Injection rule source: `.claude/config/context-injection-rules.json`; assignee
`se`; documentation/implementation/testing. Project tag `ATS` is confirmed in
`.claude/config/workspace.json`. Completion uses the loaded
`.agents/skills/create-wi-evidence-pack/SKILL.md`; its handoff-exists gate passed.

## Evidence Pointers

Owned changes only:

- [Root README](../../README.md): Rules, current/history and reconstruction entry points.
- [Reconstruction guide](../../scripts/reconstruction/README.md): Toolchain,
  source checks, explicit config selection, portable private assets and quarantine.
- [Preflight](../../scripts/reconstruction/Test-DevelopmentRecovery.ps1): Named
  file allowlist; `ConvertFrom-Json -AsHashtable` handles lockfile empty root key;
  no settings paths, content values or parser exceptions serialized.
- [Tests](../../scripts/reconstruction/test-development-recovery.ps1): Synthetic
  temporary fixtures, locked private sentinel files, hash preservation, tool
  discovery mocks, junction refusal and real preflight CLI exit/JSON checks.
- [Documentation index](../../docs/index.md): Reconstruction route and registry-led
  historical availability instead of repeating superseded retention totals.
- [AGENTS.md](../../AGENTS.md), [CLAUDE.md](../../CLAUDE.md): Short entrypoint links only.
- [.gitignore](../../.gitignore): Only `/private-uploads/` added.
- [WI summary](../user/WI-20260909-ATS-019-summary.md): Handoff and limitations.

Source/config evidence:

- `build.gradle`, `gradle/wrapper/gradle-wrapper.properties`, parsed
  `frontend/package.json`/`package-lock.json`: JDK 17, Gradle 9.3.0, Spring Boot
  4.0.8 and locked frontend/test engines. Node 22.13+ within 22.x or 24.x selected.
- `application-local.example.yml`, `frontend/.env.example`, committed base and
  acceptance YAML: public templates only. Actual private settings were not read.
- `AtStudioApplication`, `SubscriptionScheduler`, `PaymentReconciliationService`,
  `WithdrawalBillingCleanupCoordinator`, `StorageMutationRecoveryService`:
  unconditional scheduling, after-commit cleanup and startup recovery hazards.
- `BillingKeyCrypto`, `PaymentProperties`: complete ID-addressed V2 keyring;
  lost historical key material cannot be recreated from DB or Provider credentials.
- `service/audio/AudioAnalysisService.java`: Java Sound/MP3 SPI; no current
  FFmpeg executable or outbox disable setting found in the scoped source scan.

## Commands & Outputs

Run from repository root in PowerShell 7.2+:

| Command/check | Result |
|---|---|
| `pwsh -NoProfile -File ./scripts/reconstruction/test-development-recovery.ps1` | Exit 0; 33 assertions, 33 passed, 0 failed after history registry integration |
| `pwsh -NoProfile -File ./scripts/reconstruction/Test-DevelopmentRecovery.ps1 -AsJson` | Exit 0; SOURCE_ONLY, 38 checks, no failures |
| Same preflight with `-CheckTools -AsJson` | Exit 0; 46 checks, 0 failures, 1 WARN (mysql CLI missing); required PATH tools found, installed versions explicitly NOT_CHECKED |
| `git check-ignore -v --no-index -- private-uploads/__recovery_probe__.pdf` | Exit 0, `.gitignore:58:/private-uploads/`; no probe file created |
| `git check-ignore --no-index -- scripts/reconstruction/Test-DevelopmentRecovery.ps1 frontend/src/private-uploads/__recovery_probe__.pdf` | Exit 1, no matches; new guard is root-scoped |
| PowerShell AST `Parser.ParseFile` for both new scripts | 0 parser errors each |
| Scoped Markdown link-target check of README, guide, AGENTS, CLAUDE, docs/index and own deliverables | 115 targets, 0 missing; not a full repository/anchor audit |
| `sync-docs-index` skill procedure, recursive category metadata count | WI018 added one registry: updated registry 5 -> 6 and total 204 -> 205; no other count changed |
| `git diff --check` | Exit 0; only existing CRLF-to-LF Git normalization warnings |

The link check uses the repository validator's relative link pattern, scoped
to owned documents, resolving target paths and ignoring external/anchor-only
links. Full `validate_docs.py` scans and clean staged-source checks remain
integration gates; they were not represented as completed here.

## Tests

The focused suite checks valid manifests, absent/empty/file/UNC roots, empty
templates, missing wrapper, WI skill and history registry, malformed/non-object/oversized JSON,
missing lock root/resolved dependency, dependency drift, wrong lock version,
required versus optional tool absence, root/ancestor/manifest junctions, and
success/failure CLI exit codes with bounded redacted JSON. Default preflight
does not invoke tool discovery. Locked synthetic private files remain unread
and hashes remain identical. Tests use only their owned GUID-named OS temp
directory; canonical path containment is checked before cleanup. The two
child processes are preflight CLI tests, not application/server processes.
No external test dependencies were added. This is not coverage for product
logic, arbitrary future dependency graphs or real restore compatibility.

## Risks / Rollback

- Source checks prove presence and direct package pairing, not complete source
  correctness or compatible installed versions. Run manual version checks and
  approved clean build validation before relying on a new machine.
- A same-PC source copy still shares installed tools/caches. New-PC install,
  encrypted off-device backup and realDB/fullrestore were **not run**.
- Restored data remains quarantined; notification/bootstrap flags cannot disable
  all current business writes or storage recovery. Separate isolation work is
  required before application startup against retained data.
- No product/runtime configuration, DB, media, external launcher, private bundle,
  provider/mail state or running service was modified. MA's live preservation
  observations are not independently rechecked by this WI.
- Rollback: review and reverse only this WI's documented file changes after
  checking concurrent work; do not revert WI018 registry/recovered documents.
  No DB/media/config/runtime rollback exists because none was changed. No commit,
  staging or push was performed.

## Follow-ups

WI019 is ready for WI020; all WI019 implementation and focused verification
items are complete. Integration/restore gates above remain explicitly separate.

- WI018 history registry connection is complete; its contents are owned and
  reviewed by DocOps/QA, not independently re-audited by WI019.
- WI chain checked: WI019 **Blocks WI-20260909-ATS-020**, which also depends on
  WI018. MA must hand both Evidence Packs to QA using the WI020 skill-generated
  handoff immediately after both work items are ready. Do not close REQ004 here.
- MA owns final clean staged-source validation/Git and user confirmation of an
  off-device encrypted backup destination. No destination has been supplied.
- Production steps 2-4, selected target acceptance and final release approval
  remain unchanged under SR-93.
