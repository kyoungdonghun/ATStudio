---
version: 1.3
last_updated: 2026-09-09
project: ATS
owner: CR
category: summary
status: stable
dependencies:
  - path: REQ-20260909-ATS-005.md
    reason: Approved source implementation and preservation scope
  - path: ../agent/WI-20260909-ATS-026-evidence-pack.md
    reason: Independent review findings and exact evidence
---

# WI-20260909-ATS-026 Summary

**PASS: reviewed source implementation complete. Deployment pending.**

All four confirmed findings are resolved; no blocking source finding remains
within the approved review scope.

| Finding | Status |
|---|---|
| P1: staged and Windows case-aliased final audio bypass static protection | Reproduced actual 200/audio bytes; resolved with a narrow two-prefix case-insensitive deny and 65/65 security/MVC tests |
| P2: duration-derived output bound can expand far beyond input cap | Resolved by SE: 256MiB pre-process rejection; exact boundary and overflow regressions reviewed |
| P2: native child unnecessarily inherits unrelated server environment | Resolved by SE: minimal case-insensitive child-only allowlist and synthetic-secret tests reviewed |
| P2: FAILED processing reason is missing in frontend | CR fixed safe Korean guidance for known codes only; unknown values use fixed generic text |

CR changed three frontend files (safe helper/component/test), `SecurityConfig`,
one new security/MVC test, current operations/REQ notes and the two WI026
reports. Component **26/26 tests**, TypeScript, scoped ESLint and three-file
Prettier PASS. Direct security/MVC **65/65 PASS**, including exact public
thumbnail bytes and encoded dot/slash rejection; not a deployed browser check.

Lifecycle/security source review found generation/token fencing, retained
original/stream/pending/claimed references, journal-before-stage ordering,
ADMIN-only processing routes, denied static audio and unchanged original
download selection in the inspected paths. Existing tests cover these paths;
their final execution is artifact-audited separately. CR independently
inspected SE's final **50/50 PASS** XML/log (28 encoder, 20 pipeline including
ten real FFmpeg cases, two configuration; zero skips).

The initial full build failed seven outdated pre-lock concurrency barriers.
CR independently confirmed the cause; SE moved the test barrier after actual
lock acquisition, retained seven writer/media/counter checks and added an
analysis-time preservation regression. SE **9/9 PASS** XML was inspected.
No product lock logic or coverage threshold was weakened.

DocOps handed current closure to CR. Parent final full Gradle **PASS, 2m16s**:
204 suites, 1912 total / 1893 passed / 0 failed or errors / 19 skipped (18 opt-in
MySQL, one Windows symlink). CR independently verified XML/log and JaCoCo:
LINE 88.625%, METHOD 86.625%, BRANCH 74.216%; existing critical-security gates pass.
Final frontend **114 files / 1600 tests PASS, 236.59s**, and `npm.cmd run build`
**PASS** (Vite phase 2.40s) were independently log-verified by CR. Parent also
confirmed final global lint/typecheck and 17-file Prettier PASS.
Final document validation PASS (718 IDs, links/index/Tier0); scoped nine-file
diff/whitespace and four-document metadata/dependency checks PASS.

REQ005 source implementation is closed under the parent's explicit delegation;
WI026 blocks no further WI and none was created. Single-runtime native-orphan
handling and actual DB/media/proxy/browser deployment remain explicit operational limits. The
running backend was not restarted or upgraded; 100MiB Vite text is not backend
deployment evidence. No actual DB, original media, private settings/backups,
unrelated baseline documents or Git state was changed by CR.

Deployment still requires separate approval for the existing DB's nine columns
plus queue index, matching roots/backup, service FFmpeg path, schema/integrity,
restart and browser/whole-request-cap verification. No new database is required.

See [evidence pack](../agent/WI-20260909-ATS-026-evidence-pack.md) for exact
commands, findings, pointers and rollback boundaries.
