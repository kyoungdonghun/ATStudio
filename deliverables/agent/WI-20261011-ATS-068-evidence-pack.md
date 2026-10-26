---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: QA
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-068-handoff.md
    reason: Approved local release ownership and acceptance criteria
  - path: WI-20261011-ATS-064-evidence-pack.md
    reason: Applied source and SE focused-test provenance
  - path: WI-20261011-ATS-065-evidence-pack.md
    reason: Independent exact-source build ALLOW
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved requirement with separate native release
---

# Evidence Pack: WI-20261011-ATS-068

> Purpose: Identify the fresh audited JAR and its exact tested/reviewed source.

## Summary

**LOCAL_ARTIFACT_PASS_TARGET_NOT_RUN. WI068 local build/audit is complete.**
The fresh JAR is 70,307,578 bytes and has SHA-256
`f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.
The first Gradle attempt failed on a read-only cache path error; after explicit MA
fix/retry approval, the second attempt and all release audits passed. Both attempts,
all helper versions, original frontend and old JARs remain preserved.

No application/source/frontend modification, app/server/native/DB/mail/payment,
private-input, dependency download, cloud activation, deletion, commit or push was
performed by QA. This result supersedes the preparation-only checkpoint for this WI,
without changing that historical checkpoint or granting native release.

## Scope / DoD Check

- [x] Explicit MA source ALLOW and final WI065 `ALLOW_SOURCE_FOR_WI068_BUILD` mapped.
- [x] SE127 and PG147 passing tests verified through exact receipts/XML, separately.
- [x] Full643 source/build-input manifest equal before/after successful build.
- [x] Offline compileJava/bootJar in fresh isolated output with sanitized environment.
- [x] Packaged classes/resources, dependencies, manifest and Java17 format verified.
- [x] Duplicate/private filenames absent; frontend336/SPA143 and160 preservation records unchanged.
- [x] Fresh canonical artifact manifest and both reports published without overwrite.
- [x] Failure history retained; retry occurred only after explicit bounded MA approval.

## Identity And Evidence Pointers

Verification owner QA; local Windows/PowerShell 7.6.5, JDK 17.0.12, Gradle 9.3.0,
Spring Boot 4.0.8. Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`;
dirty shared worktree, not a clean-room release. Verification completed
**2026-10-11T22:10:13.5408784+09:00**. Document date is not the build timestamp.

| Object | SHA-256 / pointer |
| --- | --- |
| [Fresh JAR](../../build/WI068-release-run-02/build/libs/ATStudio-0.0.1-SNAPSHOT.jar) | `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81` |
| [Canonical manifest](WI-20261011-ATS-068-artifact.json) | `af998a73bf61da6cbf1717751e2e79c16186fc3a037f90102d77da71d1ead3ba` |
| [643-file before](../../build/WI068-release-run-02/source-before.json) and [after](../../build/WI068-release-run-02/source-after.json) | Both `c6fc15d8f38c6597d159c7dd258048d25e74b3d03f5eb5596b701219a8ae207e` |
| PG637 source mapping | `fb367e966e72b6ceb24de6afabde9efb74333275fe41de8aa613eb119979a58e` |
| [Applied patch](../../build/WI064-audio-only/audio-only-applied.patch) | `eb7efd4f25415310e1a9c89755589f3bef530af42e4a828a73e762ef412937c9` |
| [PG receipt](../../build/WI065-review/audio-20261011T214029320234/receipt.json) | `bcdb9b9d967f8b75535311a67b28ae7a721f6d20a7040139c1128ff6f3813a44` |
| [Final gate](../../build/WI068-release-tools/gate-03.json) | `acb5ce701379effe5f33e8ab8ed49b8ffdaf5575564aa3ae33848b73d46ee25d` |
| [Source mapping](../../build/WI068-release-tools/source-mapping-03.json) | Maps all637 reviewed src files,2 PG-matched build definitions and4 unchanged wrapper inputs. |

The PG source digest was independently reproduced from its ordinal-sorted compact
ASCII path-to-hash JSON. It is a different representation/scope from the643-record
manifest, not a contradictory source identity. Wrapper inputs match the historical
WI020 input bytes; the wrapper was not executed and old approvals were not imported.
All660 PG-preserved input hashes and8 applied WI064 hashes matched before release.

## Commands And Results

Java/Gradle mode detected via `build.gradle`; Build Check skill narrowed to the
approved `compileJava bootJar` tasks. No `build`, `test`, `clean`, package install,
frontend rebuild or application task was requested. Exact arguments, environment
names, tool version/release and launcher hash are in the
[invocation receipt](../../build/WI068-release-run-02/command.json).

```powershell
& ./build/WI068-release-tools/release-build-v3.ps1 -Build `
  -GatePath 'build/WI068-release-tools/gate-03.json' `
  -GateSha256 'acb5ce701379effe5f33e8ab8ed49b8ffdaf5575564aa3ae33848b73d46ee25d' `
  -RunName 'WI068-release-run-02'
```

This is the completed command, **not a rerun instruction**. Its output directory
exists and is immutable. Later execution requires new authority/gate/run identity.

[Result](../../build/WI068-release-run-02/result.json): exit0,
32.4445051 seconds for the Gradle child; console32s,4 actionable tasks executed.
The overall helper/audit window was22:06:42.9492769 to22:10:13.6979624 KST.
It includes repeated byte verification and is not the Gradle duration.
[Stdout](../../build/WI068-release-run-02/gradle-stdout.json) reports BUILD SUCCESSFUL;
[stderr](../../build/WI068-release-run-02/gradle-stderr.json) decodes to empty text.
The lexical warning counter is1 solely because Gradle's welcome banner says
"Error and warning improvements". No compiler diagnostics were emitted. Gradle
also identifies its read-only cache as an incubating feature.

The environment was cleared and replaced with a literal nonsecret allowlist.
User/project Gradle homes, profile, temp and build outputs are isolated under run-02.
`GRADLE_RO_DEP_CACHE` points to the existing cache **parent**, `.gradle/caches`;
resolution is `--offline`, with toolchain auto-download disabled. No shared writable
cache fallback occurred. Offline Gradle is not OS-level network isolation: local
Gradle process coordination is outside the no-external-dependency-download claim.
Owned daemon PIDs54864(run-01) and50684(run-02) were absent at the post-audit check;
no unrelated process was stopped or inspected for cleanup.

## Audit Results

[Archive inventory](../../build/WI068-release-run-02/archive-inventory.json):824 file
entries,616 application classes,4 application resources and101 runtime libraries.
All application class bytes match fresh compilation and class major version61.
All resource bytes match public source, including default-off audio-only YAML.
Reviewed background/scheduled conditions, configuration, worker and storage recovery
classes are present. Library set/bytes match WI020. Boot JarLauncher and ATStudio
Start-Class entries are correct. Duplicate entries0; private/unsafe named entries0.
Filename checks are not a secret-content scan.

Frontend source336 and existing SPA output143 have identical enumerations and bytes;
the four historical coverage-test exclusions remain explicit and preserved. No UI
rebuild occurred. [160 exact preservation records](../../build/WI068-release-run-02/preserved-before.json)
remain unchanged; failed run-01 receipts and final SE/PG evidence were also checked
before/after as pinned evidence. These overlapping groups are not a unique-file sum.
Unrelated active build trees were not recursively hashed.

## Tests And Failure History

SE:127 PASS,11 suites. PG:147 PASS,12 suites, including the same127 plus20 independent
cases. Zero failures/errors/skips in each set. QA revalidated exact XML hashes and
counts, but did not rerun tests. Do not sum127+147. Full suite, numeric coverage,
SAST/CVE scan, native audio/DB behavior and server acceptance remain NOT RUN here.

1. Preparation:14 bounded checks passed. An actual XML preflight later exposed the
   PowerShell `.Name`/XML name-attribute collision; execution copy uses `.LocalName`.
2. Prelaunch Git discovery returned two executables. Invocation stopped before
   Gradle/output creation; [repair evidence](../../build/WI068-release-tools/prelaunch-repair.md)
   and original helper copies remain. Explicit selection fixed it; no Gradle attempt
   was consumed at that point.
3. [run-01 result](../../build/WI068-release-run-01/result.json): exit1 in6.4121928s.
   Its stdout explains the incorrect nested `modules-2/modules-2` expectation.
   This was a cache-path error, not proof that Spring Boot dependencies were absent.
4. MA explicitly approved the bounded cache-parent fix and one fresh offline attempt.
   [10 repair checks](../../build/WI068-release-tools/cache-fix-check.json) passed;
   new helper/gate/run retained all prior inputs and failed outputs. run-02 passed.

## References And Skills

Tier0 STD-001/004/002/005 remained fully injected: core-principles, documentation-
standards, development-standards and glossary. Tier1: QA role, security-policy,
quality-gates and evidence-pack-standard. Tier2: `build-check`,
`create-wi-evidence-pack`, test skill guidance and the existing skill-generated
WI068 packet. Evidence skill's handoff-existence gate passed. Tag ATS was verified
in `.claude/config/workspace.json`; rule pointer
`.claude/config/context-injection-rules.json`. No subdelegation occurred.

## Risks, Rollback And Next Chain

No native release is granted. Cached dependencies/JDK are trusted local inputs,
not provenance-attested; this is a dirty local build. Historical native web PASS
belongs to its prior artifact and does not validate this JAR. Existing global audio
queue, single-worker, fenced-writer and target-specific prerequisites remain.
No secret/runtime configuration, backup/cloud system or target host was accessed.

Rollback at this stage means withholding the new JAR from deployment. Existing JAR,
SPA, source and failed evidence were neither removed nor overwritten. Any native
rollback belongs to a separately approved runtime contract, not this build WI.

Chain checked: WI068 completes the fresh-artifact prerequisite for WI067 final pins
and MA review. MA can now pin the canonical artifact and source identity above;
WI067's independent/native gates remain. QA did not delegate, alter WI067, close
REQ007 or start native execution.

## Related Documents

- [Canonical artifact manifest](WI-20261011-ATS-068-artifact.json)
- [WI068 summary](../user/WI-20261011-ATS-068-summary.md)
- [WI068 handoff](WI-20261011-ATS-068-handoff.md)
- [WI065 source review](WI-20261011-ATS-065-evidence-pack.md)
- [WI067 next handoff](WI-20261011-ATS-067-handoff.md)
- [Preserved preparation checkpoint](../../build/WI068-release-tools/prepared-evidence.md)
