---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261021-ATS-001-handoff.md
    reason: Approved local inventory and identity scope
  - path: ../user/REQ-20261021-ATS-001.md
    reason: Local cleanup approval and server pause
  - path: WI-20261021-ATS-002-evidence-pack.md
    reason: Independent commit-security allowlist
---
# Evidence Pack: WI-20261021-ATS-001

## TL;DR

Local identity PASS: backend643, exact JAR, frontend336 inputs/143 outputs and
four preserved historical test exclusions all match existing records. No source,
artifact, index, runtime or historical file was modified by SE. MA subsequently
reported commit86f744a for the exact18 source files after PG staged-byte review.
The path-only list is a classification snapshot, not permission to stage all paths.

## Scope and Inventory

- [x] Inventory tracked modifications and untracked paths without private bodies.
- [x] Confirm source/artifact identity without rebuild or execution.
- [x] Propose at most three purpose commits; explicitly retain other work.
- [x] Send candidates and identity results to WI002 for independent review.

Initial read-only Git snapshot: branch main, HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6;
13 tracked modifications,480 untracked files, empty index. This predates MA's commit.
No deletions/renames were observed. Directory-level untracked entries were expanded
with git ls-files, not classified as safe by directory name alone.

| Group | Initial count | Disposition |
| --- | ---: | --- |
| Application source/config/tests | 18 (6 modified,12 new) | One cohesive commit; shared application.yml stays together |
| Reusable deployment/config/tool review candidates | 74 (42 deploy,32 scripts) | DEFER all74 this turn by MA; no allowance implied |
| Current approved cleanup REQ/handoffs | 4 | Commit3 with new reports, subject to PG |
| Unrelated tracked root/document changes | 7 | Retain unstaged; no edits |
| Historical operational deliverables | 369 | Retain unstaged; no bulk publication or retrospective redaction |
| Client guides/screenshots/PDF and QA assets | 18 | Retain; possible private operational content, not reviewed here |
| Other generated output files | 3 | Retain; not commit candidates |

The final [path-only list](WI-20261021-ATS-001-candidates.json) includes11 current
cleanup document paths: the original4 plus7 new WI001/002/003 outputs. This explains
the difference from the initial snapshot; other agents own their outputs.
Ignored build, frontend/node_modules, frontend/dist, .gradle, uploads and
private-uploads were confirmed ignored without opening private files. No force-add,
clean-tree requirement, .gitignore change, deletion or cleanup of generated trees.

## Identity Evidence

| Read-only check | Measured result | Receipt |
| --- | --- | --- |
| WI068 source-before.json recorded hash | c6fc15d8f38c6597d159c7dd258048d25e74b3d03f5eb5596b701219a8ae207e matches artifact pointer | 582e39 |
| Backend input SHA-256 and lengths | 643/643 exact; missing0,changed0 | 582e39 |
| Current backend path enumeration | 643 exact; no extra/missing path | ad5176 |
| JAR | 70307578 bytes; f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81 | 582e39 |
| Frontend input SHA-256 and lengths | 336/336 exact | 582e39 |
| Frontend source and output enumeration | Both exact; no extra/missing path | 961de1 |
| Frozen SPA output SHA-256 and lengths | 143/143 exact; actual count143 | 582e39 |
| V7 public-manifest frontend members | 143/143 exact | 961de1 |
| Public-manifest SHA-256 | 4470db7c61707f33c53036ebdd9f62dabdd559f366cc4bbedc91454360ab6734 | ad5176 |
| Source candidates covered by backend manifest | 18/18 | 961de1 |
| Historical coverage-test exclusions | All4 bytes/lengths unchanged | 961de1 |
| Tracked source whitespace check | git diff --check -- src, exit0 | b16350 |

Provenance: [WI068 artifact](../../build/WI068-release-run-02/artifact.json),
[backend inputs](../../build/WI068-release-run-02/source-before.json),
[preserved exclusions](../../build/WI068-release-run-02/preserved-before.json),
[public manifest](../../build/WI067-pc-delivery-v7/public-manifest.json).

Exact frontend exclusions, preserved rather than removed or silently counted:
- frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
- frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx

## Commit Proposal and Deferred Categories

1. Backend background-task isolation, storage mutation admission, resource/mail
   timeout configuration and associated tests:18 paths. MA reports86f744a completed
   with PG raw-byte/index-filter approval; SE did not stage or commit.
2. Reusable single-VPS configuration and deployment/validation tooling: proposed
   future group only. MA timeboxed all74 to DEFER this turn. Retain all unchanged;
   no privacy clearance or operational permission is implied.
3. Current cleanup classification/security/resume records: exact PG-reviewed
   current REQ/WI documents. No historical operations corpus or screenshots.

Seven unrelated tracked edits retained: AGENTS.md, CLAUDE.md, README.md,
docs/index.md, docs/payment/index.md, docs/templates/ma-session-kickoff-prompt.md,
scripts/reconstruction/README.md. Full retained untracked paths are in the JSON.
No historical operational report corpus was reread. Deployment guide sampling
showed reusable tooling mixed with historical/personal references, so only PG's
exact per-file list may be committed. No existing file is sanitized by this WI.

## Reproduction and Validation Boundaries

Inventory: git status --short --untracked-files=normal; git ls-files --modified;
git ls-files --others --exclude-standard; git diff --cached --name-only;
git branch --show-current; git log -1; git diff --stat; git diff --check -- src.

For each row in source-before.json, artifact.frontendSource and
artifact.frontendOutput.files, compare Get-Item.Length with bytes and
Get-FileHash -Algorithm SHA256 with sha256, case-insensitively. Check the backend
artifact row identically. Enumerate src plus gradle and build.gradle,
settings.gradle,gradlew,gradlew.bat; compare the sorted set with643 recorded paths.
Frontend enumeration follows release-build-v3.ps1 FrontNames: src/public trees,
root json/ts/cjs/html files excluding private .env files, plus the three recorded
public dotfiles, minus only the four named historical tests. Compare those four
separately against preserved-before.json. No build script was invoked.

No functional test suite, rebuild, app run, remote/SSH/UI/network/cloud/DB action,
secret access, staging, commit or push by SE. Hash equality is local byte evidence,
not new production or Gmail acceptance. Historical build HEAD stays unchanged;
MA's Git commit does not itself redeploy or alter the JAR. Git may normalize line
endings in staged blobs; MA/PG own the index-filter check and post-commit hash check.

## Risks and Follow-up

Only three owned WI001 files were created. No operational rollback is necessary.
Leave excluded work in place. Do not reset or clean the shared tree. The candidate
list is point-in-time; final exact PG allowances and current bytes govern commits.
WI001 classification is complete; actual cleanup now needs only source and document
commits, with the optional reusable-assets group deferred. WI002 review and MA commit ownership
continue independently. WI003 owns the evening resume guide. Server work is paused.

## Related Documents

- [Handoff](WI-20261021-ATS-001-handoff.md): owned output boundary.
- [Approved REQ](../user/REQ-20261021-ATS-001.md): local-only scope.
- [WI002 review](WI-20261021-ATS-002-evidence-pack.md): exact allowed bytes.
- [Summary](../user/WI-20261021-ATS-001-summary.md): concise result.
- [Core principles](../../docs/standards/core-principles.md): STD-001 retained injection.
- [Development standards](../../docs/standards/development-standards.md): STD-002 retained injection.
- [Documentation standards](../../docs/standards/documentation-standards.md): STD-004 retained injection.
- [Glossary](../../docs/standards/glossary.md): STD-005 retained injection.
- [Security policy](../../docs/policies/security-policy.md): read for commit boundary.
