---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-017-handoff.md
    reason: Approved ownership and output contract
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Local-only approval and WI chain
  - path: WI-20261011-ATS-015-evidence-pack.md
    reason: Latest attributed host restoration and native schema evidence
---

# Evidence Pack: WI-20261011-ATS-017

> Purpose: Record local preparation of a gated private-start candidate without runtime or remote effects.

## Summary

Local-only first-start candidate complete:12/12 static/synthetic checks PASS;
Linux/network/Spring startup proof NOT RUN; production HOLD.

## Scope / DoD Check

- [x] New first-start directory and own two-set deliverables only.
- [x] Separate non-installed/non-enabled candidate with false prestart gate and no old-JAR fallback.
- [x] Master/audio/acceptance/test-user/auth/notification OFF plus empty Provider credentials and numeric SMTP CLI precedence.
- [x] Strict integrity, explicit roots, JWT/keyring validation and DDL validate preserved.
- [x] Source inventory, independent IPv4/IPv6 fence proof gate, loopback relay caveats, bounded GETs and cleanup procedure.
- [x] Existing resources reused, finite lifetime/no restart, no host firewall/TLS changes.
- [x] Local test evidence and prior artifact preservation recorded.
- [ ] Target syntax, actual IP denial/loopback proof, startup and cleanup: NOT RUN, separately approved future scope.

## Reference Documents (Tier 0-2)

Injection order supplied by MA: Tier0 constitution/standards, SA persona/security,
Tier2 handoff, snapshot. Tier0 four documents were fully supplied and present.

| Tier | Document / input | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md, STD-001 | Approval, answer/execution and effect boundaries |
| 0 | docs/standards/documentation-standards.md, STD-004 | Metadata, links, English docs, historical preservation |
| 0 | docs/standards/development-standards.md, STD-002 | Source traceability, public config and bounded tests |
| 0 | docs/standards/glossary.md, STD-005 | Canonical terms |
| 1 | SA persona; docs/policies/security-policy.md | Alternatives, least privilege, no secrets/log disclosure |
| 1 | docs/policies/quality-gates.md | Local proof and independent review boundary |
| 2 | docs/index.md latest Current Decision Entry | Native schema PASS and restored MySQL/TLS baseline |
| 2 | deploy/single-vps/CONFIGURATION.md; systemd/atstudio.service; env/atstudio.env.example | Current templates and effective resource baseline |
| 2 | deploy/single-vps/runbooks/production-readiness.md | Dated source inventory; latest index supersedes older readiness status |
| 2 | REQ002; WI017 handoff; WI015 Evidence Pack; WI026 artifact manifest | Scope, prior evidence owner, artifact preservation |
| Skills | .agents/skills/create-wi-evidence-pack/SKILL.md | Existing handoff precondition verified; this output follows the skill |
| Skills | .agents/skills/test/SKILL.md; validate-docs/SKILL.md | Test result reporting and scoped document validation |
| Context | .claude/config/workspace.json | ATS tag verified |
| Supplemental MA | New build/WI020-release plan; WI020 artifact manifest; no old-checker patch | New JAR placeholder and exact future gate |

Handoff rule source identified by project: `.claude/config/context-injection-rules.json`;
assignee SA, architecture/configuration preparation, required Tier0. No sub-delegation.
Memory served only as navigation; live configuration superseded earlier design-only
resource notes. No private env/body or secret was read.

## Evidence Pointers

Created paths (all owned by WI017):

1. [first-start/README.md](../../deploy/single-vps/first-start/README.md): alternatives, precedence, target proof, finite runbook, stop/remask and limitations.
2. [first-start/inventory.md](../../deploy/single-vps/first-start/inventory.md): exact source entry points and external destinations.
3. [candidate unit](../../deploy/single-vps/first-start/atstudio-first-start.service.candidate): false prestart gate, fixed CLI settings, finite runtime, IP filter, resource/sandbox parity.
4. [public overlay](../../deploy/single-vps/first-start/first-start-public.env.example): public names/literals, no secret slots or actual values.
5. [offline tests](../../deploy/single-vps/first-start/test_candidate.py):12 standard-library tests, in-memory adverse variants, no private environment access.
6. [User summary](../user/WI-20261011-ATS-017-summary.md): result, recommendation and target exclusions.
7. This Evidence Pack: attribution, tests and remaining gates.

Source recheck during concurrent WI016 work found
`config/BackgroundTasksConfiguration.java` conditional scheduling and a
BeanFactoryPostProcessor refusal guard, `BackgroundTasksEnabledCondition.java`
strict true/false parsing with default true, audio's combined master/audio
conditions, and `StorageMutationRecoveryService.recoverOnStartup` master guard.
This is source inspection only, not independent Java regression evidence. SA
did not edit those files; WI016/019 own tests and WI018 owns security review.

## Commands and Outputs

Verification date2026-10-11, owner SA, Windows/PowerShell; `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`, pre-existing dirty/untracked files.

- `git status --short`, `git branch --show-current`, `git rev-parse HEAD`: baseline recorded; unrelated changes preserved.
- Scoped `Get-Content` and `rg` for scheduled/startup/HTTP/mail/process entries: inventory linked above. An initial nonexistent guessed Provider filename and PowerShell wildcard path were corrected using directory-based `rg`; no source absence was inferred from those errors.
- `Get-ChildItem deploy/single-vps -Recurse -File` excluding `first-start`, piped to `Get-FileHash -Algorithm SHA256`: before/after public deployment hash maps equal.
- `Get-FileHash` of WI026 manifest and existing JAR: unchanged before/after; pins below.
- `python -B deploy/single-vps/first-start/test_candidate.py`: exit0,12 tests/12 PASS/0 fail/0 skip,0.008s reported by unittest.

| Preserved item | SHA-256 |
|---|---|
| WI-20261010-ATS-026-artifact.json | fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c |
| build/libs/ATStudio-0.0.1-SNAPSHOT.jar | 517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a |

Official documentation checked live via read-only web retrieval: freedesktop
rendered manual URLs returned403; fallback was upstream systemd v255 manual
source. This is documentation research, not a connection to the target host.

- [systemd resource-control](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.resource-control.xml): IP allow/deny precedence, inherited lists and unsupported BPF limitations.
- [systemd service](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.service.xml): prestart failure and finite runtime; no readiness inference from Type=simple.
- [systemd execution](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.exec.xml): final UnsetEnvironment processing.
- [Spring Boot external configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html): CLI property precedence. Target-version/exact-artifact binding still needs independent proof.

## Tests and Reproduction

The single Python command above is reproducible without extra installs and with
`-B` (no bytecode output). It reads only the candidates and existing public unit.
ConfigParser/shlex cover this candidate subset, not complete systemd parsing.
The poisoned-env case is a static precedence model, not real Spring binding.
Adverse variants remove each OFF flag and replace each critical flag/credential
or SMTP value, expecting rejection. IP tests evaluate address membership only,
not sockets/BPF. Baseline comparison checks existing resource/sandbox values.

Document validation uses the existing DocValidator with enumeration restricted
to the four WI017 Markdown files, then checks metadata fields, dependency targets
and direct whitespace/EOF on all seven owned files (Git alone omits untracked
files). Internal link checks concern file targets, not URL reachability or anchor
semantics. No whole-repository documentation PASS or index-count change claimed.
The directory README indexes its files; MA/WI021 owns broader entry integration.
Result:4 Markdown files,10 supported traceability IDs,0 errors/0 warnings;
metadata/dependency targets and7-file whitespace/EOF PASS.
`git diff --check -- deploy/single-vps/first-start deliverables/user/WI-20261011-ATS-017-summary.md deliverables/agent/WI-20261011-ATS-017-evidence-pack.md`
returned exit0. All seven new files remain untracked, unstaged and uncommitted.

No app/unit launch, network test, Java suite/build, existing secret use, DB service,
DDL/DML, remote shell, purchase/install, commit/push or deletion was performed.
Code coverage is N/A for this documentation/public-candidate WI; no application
coverage threshold or production sizing result is claimed.

## Risks / Rollback

IP filters can be ineffective on unsupported systems; inherited parent rules and
loopback/Unix relays require actual proof before Java. Local app permissions still
permit writes. No proxy exposure does not prevent local callers. Startup can create
directories/logs/temp state and MySQL startup itself writes. Root-owned private
configuration, exact existing secret reuse and log capture need new approval.

Keep the false prestart gate and placeholder until a reviewed fail-closed target
procedure exists. Do not remove it based on local PASS. A new reviewed staged
JAR must match WI020's new manifest; old JAR silently ignoring the flag is unsafe.
Runtime timeout is not proof that uninterruptible processes have exited.

Proposed local rollback: retain/supersede WI017 files or obtain explicit deletion
approval for only these seven new files. No existing files require restoration.
Future target cleanup must stop exact owned unit/cgroup, restore MySQL inactive
and persistently masked, verify no DB/app ports/jobs and unchanged TLS/Certbot;
no storage/secret/log/history deletion. No rollback executed here.

## Follow-ups and Chain

WI017 local scope complete. Blocks **WI-20261011-ATS-018** with WI016; MA should
immediately prepare/dispatch its approved PG handoff after both are available.
WI019 provides independent regression; WI020 then records exact new JAR/manifest
without touching old build/libs or WI026; WI021 integrates current entry.
SA has no sub-delegation authority and does not close REQ002. No new scope blocker
encountered; target proof, real secret use and full startup remain planned HOLD gates.

## Related Documents

- [WI017 handoff](WI-20261011-ATS-017-handoff.md): Unchanged approved contract.
- [User summary](../user/WI-20261011-ATS-017-summary.md): Concise result.
- [Candidate runbook](../../deploy/single-vps/first-start/README.md): Exact future boundaries.
- [REQ002](../user/REQ-20261011-ATS-002.md): Open approved WI chain.

## 2026-10-11 Approved Validation Repair Addendum

**Superseding local result: owner repair PASS; WI018 independent re-review
pending.** This addendum preserves the original12-test receipts and all prior
hashes. Those results did not detect PG018-01/02 and are not sufficient release
clearance. SA reproduced PG's8 survivors before editing: unchanged harness
exit1,27 cases,19 PASS/8 FAIL/0 ERROR/0 SKIP,0.250s.

Authority: MA's Approved Validation Repair Addendum in the
[WI017 packet](WI-20261011-ATS-017-handoff.md#approved-validation-repair-addendum)
and updated REQ002, following explicit user approval. The same injected Tier0,
SA persona and security policy remain governing context. SA read the full
[PG018 evidence](WI-20261011-ATS-018-evidence-pack.md) and unchanged reproduction.
The create-wi-evidence-pack skill's existing-handoff gate was reconfirmed.

### Narrow Repair

Only `deploy/single-vps/first-start/test_candidate.py` changed, plus appended
addenda in this Evidence Pack and its user summary. Unit/env/README/inventory,
application source, PG harness, historical artifacts and old receipts unchanged.

- `parse_unit` now retains every ordered occurrence in a restricted plain-text
  grammar. Only ExecStart continuation lines are accepted; ambiguous syntax,
  expansion, quoting, repeated sections and unfinished continuations reject.
- `candidate_unit` requires the exact Unit/Service directive names and exactly
  two EnvironmentFiles in the approved order. Every other directive is a
  singleton; reset/additive duplicates and unapproved command hooks reject
  before scalar projection. The literal false prestart remains mandatory.
- `cli_values` requires the exact unprefixed `/usr/bin/java` and JVM argument
  prefix, unresolved new-JAR token and complete fixed Spring property map.
  Wrappers, privilege prefixes, extra/missing/duplicate arguments, SMTP host or
  port drift, public/private/multipart/Java-temp path drift all reject.
- Seven focused regression methods cover occurrence preservation, singleton
  duplicates/resets, EnvironmentFile order/count, hooks, Java arguments, fixed
  targets and malformed grammar. Original12 test methods remain exercised.

No general systemd parser library, install, operator or executable unit is
introduced. This intentionally narrow validator rejects syntax outside the
candidate contract; it does not establish native parser equivalence, effective
drop-in behavior, Spring binding or network enforcement.

### Repair Verification

Date2026-10-11; owner SA; Windows/PowerShell, local public/synthetic inputs only;
HEAD remains `58b1011dad796017e47dbcc82a4225ca449bcdf6` with existing dirty state.

| Command | Owner rerun result |
|---|---|
| `python -B deploy/single-vps/first-start/test_candidate.py` | exit0;19/19 PASS,0 failures/errors/skips,0.061s |
| `python -B build/WI018-review/test_redteam.py` | exit0;27/27 PASS,0 failures/errors/skips,0.336s; original candidate accepted and all26 unsafe mutations rejected, including all8 former survivors |

The unchanged PG harness now executes the expanded19-test CandidateTests class
inside each outer case. Nested assertions/subtests are not counted as additional
independent tests. No synthetic command, hostname, private env or service is
executed/accessed. No Java suite or build was rerun by SA.

| Item | SHA-256 |
|---|---|
| Previous test_candidate.py, historical | `0ac8e5d94d14ded2b6da8d861aeaf269400d94f75ba078022d1e5c51f46f75db` |
| Repaired test_candidate.py, re-review input | `43f48bf958cbb9221e70fe66af28caad128a4aafb5350e9ab8f566c5c0d535a5` |
| PG harness, unchanged | `f1757259863c02254d00e806884e62b93aaf122f7e70d36e408f3f5af18f3888` |

Before/after hashes matched13/13 protected inputs: four immutable first-start
files (unit/env/README/inventory), PG harness, old JAR, WI026 manifest and six
application implementation/configuration paths listed in PG018's reviewed pins
(AtStudioApplication, BackgroundTasksConfiguration, BackgroundTasksEnabledCondition,
audio worker, storage recovery and application.yml). The candidate retains
unit09d691d4...da7941d and env624a45c6...8a24fe6; exact full pins remain in PG018.

### Limits, Rollback and Immediate Next WI

Owner repair and unchanged-harness PASS do not replace PG's independent WI018
closure. MA returns this exact test hash for WI018 re-review, then WI020 only
after PG clearance and WI019 PASS. No new blocker or scope expansion occurred.
Native/Linux/effective network proof remains **NOT RUN** in a later approved
window; actual app/DB service, remote access and existing-secret use remain
unauthorized here. No production clearance, commit/push or deletion.

Local rollback proposal: retain these addenda and restore only the checker
through an approved follow-up if needed, returning local validation to HOLD;
never treat the old12 PASS as closing PG018-01/02. No rollback was performed.
