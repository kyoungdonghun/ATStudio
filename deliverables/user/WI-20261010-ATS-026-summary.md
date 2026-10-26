---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: QA
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-008.md
    reason: Approved local artifact preparation
  - path: ../agent/WI-20261010-ATS-026-evidence-pack.md
    reason: Reproducible bounded verification and frontend blocker
---

# WI-20261010-ATS-026: Artifact Verification

> Result: Backend local PASS; frontend typecheck PASS; frontend production build HOLD.

## Results

QA verified the local Windows dirty checkout on 2026-10-10, with final input
identity checks at 21:38:52 KST. Branch `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6` does not alone identify this artifact.

| Check | Result |
|---|---|
| Offline forced Java compilation | PASS, 33 seconds |
| Focused admission tests | PASS, 57 tests; zero failures/errors/skips |
| Offline forced executable JAR build | PASS, 36 seconds |
| Packaged classes/resources | 612 classes and four resources match current compiled/source bytes |
| Frontend typecheck | PASS with installed TypeScript 5.6.3 |
| Frontend production build | NOT RUN: unexpected `VITE_GOOGLE_CLIENT_ID` |
| Preservation | 974 source/tooling inputs unchanged; previous 143-file dist unchanged; old JARs saved |

## Deliverable And Blocker

The new backend candidate is `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`,
70,303,135 bytes, SHA-256
`517B745479792A7EAE6CE75357C7D1299AB644B50D5BC5B331FEA384CCB10E0A`.
Use this executable JAR, not the old unchanged `-plain.jar`.

Production environment name inspection found `VITE_GOOGLE_CLIENT_ID` with
`.env.local` present and no inherited `VITE_` names. No application source
reference justified a public `VITE_` key. The explicit fail-closed gate stopped
before building; no private value was printed, copied to reports or hashed.
`build/WI-20261010-ATS-026-frontend-dist` was not created. A reviewed decision
to isolate the build from private env files or approve an exact public key is
needed before the frontend artifact can be completed. No env file was changed.

## Limits And Next WI

Existing compiler warnings and runtime-transitive JUnit 4.13.2 are recorded in
the Evidence Pack. Full regression, coverage, SAST/CVE scans, frontend tests,
browser checks and native target acceptance were not run. No source changes,
downloads, DB operations, remote execution or application startup occurred.
The 2vCPU/8GB/100GB target baseline was not tested or changed; production HOLD.

MA should trigger WI027 with WI025 and this bounded result, carrying the frontend
blocker explicitly. WI026 reporting is ready; the frontend artifact is incomplete.
No descendant delegation or REQ closure was performed by QA.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-026-evidence-pack.md): Commands, warnings and scope.
- [Artifact manifest](../agent/WI-20261010-ATS-026-artifact.json): Complete file and artifact identities.

## Completion Addendum: Isolated Frontend Build

**Current result: local backend and frontend artifact preparation PASS.**
This 2026-10-10 addendum supersedes the frontend HOLD above after the user
explicitly approved the isolated env-free build. The original HOLD remains
historical evidence; production and native target acceptance remain HOLD.

At 21:44:58-21:45:12 KST, installed Node/Vite/React tooling passed a fresh
isolated TypeScript check and production build. No private dotenv file was read,
copied or changed. The child environment was cleared, then populated only with
six OS/build names; Vite exposed its four built-in variables only.

The new output is `build/WI-20261010-ATS-026-frontend-dist`: 143 files,
1,194,351 bytes. Aggregate SHA-256:
`43A0384C5F48D100E4AF6B20D989A2186B34E6900CF4E69C94B010E9671C8647`.
Individual file hashes and exact invocation are in `frontendCompletion` in
the manifest. Its current SHA-256 is
`FBEB74425ADE0FE3882A9DDDC4C97F7B67B75DDA711FFA37ACBD755A02FA4D9C`.

Final artifact checks at 21:46:26 KST confirmed all 974 source/tooling inputs,
the 143-file previous dist, saved JARs and existing verified backend JAR remained
unchanged. Output filename/marker scans and two local HTML entry references
passed. No backend rebuild, download, server, browser or network operation ran.
Two Rollup circular-dependency warnings remain advisory. The existing public
CDN font reference is retained, without fetching or browser verification.

WI026's local preparation blocker is resolved. MA can supply these exact
identities to the already-running WI027 PG review; no direct PG message or
descendant delegation was sent by QA. Runtime acceptance remains unverified.
