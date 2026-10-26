---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: QA
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-026-handoff.md
    reason: Approved scope including frontend addendum
  - path: ../user/REQ-20261010-ATS-008.md
    reason: Local artifact preparation and operational exclusions
  - path: WI-20261010-ATS-014-evidence-pack.md
    reason: Prior independent admission review and artifact activation gate
---

# Evidence Pack: WI-20261010-ATS-026

## Summary

Backend local compilation, focused tests and executable JAR packaging PASS.
Frontend typecheck PASS; production build NOT RUN after the required unexpected
VITE-key gate rejected `VITE_GOOGLE_CLIENT_ID`. Reporting is complete with this
explicit blocker; no frontend artifact or target activation is claimed.

Verification owner QA, Windows 11 local shared checkout, 2026-10-10. Backend
input capture 21:32:53 KST; focused XML timestamps 12:35:00-12:35:13 UTC;
final input/artifact preservation check 21:38:52 KST. These are execution times,
separate from this document's edit date. No host receipt is asserted by QA.

## Scope / DoD Check

- [x] Read existing skill-generated handoff, approved REQ and frontend addendum.
- [x] Confirm nine reviewed admission-related source hashes against WI011.
- [x] Preserve old JARs before replacing the executable build output.
- [x] Force offline compilation, 57 focused tests and executable JAR generation.
- [x] Check Java 17 classes, current public resources and packaging exclusions.
- [x] Capture complete backend/frontend source inputs and artifact identities.
- [x] Run frontend typecheck; stop safely on unexpected public environment key.
- [ ] Frontend production artifact: HOLD, environment gate requires resolution.
- [x] Write two reports and manifest; return next WI027 dependency to MA.

Fresh-only DB preparation/negative paths belong to WI025; independent review
belongs to WI027. This WI ran no SQL, MySQL proof, app startup, browser, remote
command, Provider/mail operation, install/download, source edit, commit or push.
No descendants were delegated. The 2vCPU/8GB/100GB target baseline remains an
unmeasured target, not a capacity finding. Production remains HOLD.

## Reference Documents (Tier 0-2)

| Tier | Pointer | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md | Fully injected STD-001/004/002/005, approval and evidence boundaries |
| 1 | .claude/agents/qa.md | Injected QA persona, bounded quality reporting |
| 1 | docs/policies/security-policy.md; access-control-policy.md; quality-gates.md | Secret minimization, least privilege, release exclusions |
| 2 | .agents/skills/build-check/SKILL.md; test/SKILL.md; typecheck/SKILL.md; lint/SKILL.md | Cached-only tooling, selected execution and reporting |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md | Existing handoff gate and this directly generated pack |
| 2 | .agents/skills/react-best-practices/SKILL.md; AGENTS.md; docs/standards/frontend-standards.md | Conditional frontend pointers loaded after addendum; no UI/code change |
| 2 | docs/standards/evidence-pack-standard.md; docs/templates/eval-report-template.md | Traceability and evaluation |
| 2 | docs/index.md#current-decision-entry; deploy/single-vps/CONFIGURATION.md; runbooks/deployment.md | Current receipt entry and artifact/startup separation |
| 2 | scripts/reconstruction/README.md; docs/design/db-schema.md | Dirty-source identity and fresh-manifest HOLD |
| Context | REQ006, REQ008; WI011, WI014 and WI024 Evidence Packs | Prior reviewed source, approved new build, historical host boundary |

The full paths are in the handoff. Context injection source is
`.claude/config/context-injection-rules.json`; QA required tiers [0], with
explicit security/policy pointers also loaded. ATS tag was checked in
`.claude/config/workspace.json`. Entry documents are historical before this WI;
QA did not modify them, REQ progress or the handoff. WI028 owns integration.

## Evidence Pointers And Identity

Owned reports: this file and `deliverables/user/WI-20261010-ATS-026-summary.md`.
The [artifact manifest](WI-20261010-ATS-026-artifact.json) records 638 backend
inputs, 336 frontend inputs, all 612 application class hashes, 101 nested
runtime library hashes, four resource hashes, four test XML receipts, tool
versions/hashes, public configuration hashes and before/after dirty path lists.
These are public source/artifact hashes, never private env-value hashes.

Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, dirty before this
work. Backend changes include SecurityConfig/application.yml and seven new
admission source/test files. All nine match WI011's final reviewed identities.
All 974 backend/frontend inputs remain byte-identical at the final check.
Concurrent `scripts/deployment/production-bootstrap.py` appeared outside QA's
write set and was not changed by QA. Git index/branch/HEAD were not modified.

| Object | SHA-256 |
|---|---|
| New executable JAR, 70,303,135 bytes | `517B745479792A7EAE6CE75357C7D1299AB644B50D5BC5B331FEA384CCB10E0A` |
| Backend input set | `AB37256115088DB4251BE89330AB054C3828CA2226BAEA5DB528E9CBD299BA4B` |
| Frontend input set | `F4C481E62E7D5D3642B3FD2B062D36D572C5E6EB3228BB9C22437D19589649E5` |
| Packaged application.yml | `A493FFE54DA9DA77B56009338438F9C0718A2CA45CAD1C21241548E1052C9709` |
| Packaged schema.sql | `6FB7DF1518394371BBFC5513DF5EBE2C97E067E7F77A1F20661AFB1C00B63FB7` |
| Packaged seed.sql | `C848E0DA6B9B35EB26563D6AF3666B67FC119430C1AFD888EA7B0246800A579A` |

Input-set digest is SHA-256 of UTF-8 concatenated sorted records, one record
`path + TAB + uppercase SHA-256 + LF`, with forward-slash repository-relative
paths. Backend enumeration uses `git ls-files --cached --others --exclude-standard
-- src gradle build.gradle settings.gradle gradlew gradlew.bat`. Frontend uses
the same Git enumeration for frontend, excluding dist/node_modules/coverage and
private `.env*` files while retaining `.env.example`. Actual main-resource
inventory was separately checked: exactly the four public resources below.
This is a same-machine dirty-checkout build, not a hermetic clean-room claim.

### Preservation And Packaged Content

Both old JARs were copied with exclusive-create semantics before building to
`output/qa/WI-20261010-ATS-026-preserved-20261010T213253/`; source/copy hashes match:

- Executable `ATStudio-0.0.1-SNAPSHOT.jar`: 70,293,748 bytes,
  `E7802311EE2F25762A8F601D5F9A9CAA2954DFB817D904DB351B0E0DB7B72784`.
- Plain `ATStudio-0.0.1-SNAPSHOT-plain.jar`: 1,163,958 bytes,
  `717B3597DEC7DB53029A79F2A4BE5A6C2C7861C07F7846A1DFD96B680E049657`.

The original plain JAR in build/libs remains unchanged and is NOT the newly
verified executable. No clean task, old-artifact deletion or runtime replacement
was performed. The previous frontend/dist's 143 files remain byte-identical.

New executable: `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`. All 612 packaged
application classes equal build/classes/java/main and have class major version
61. Required SecurityConfig, admission Configuration/Properties/Filter and both
inner classes are present. Exactly four non-class BOOT-INF/classes resources
equal source: application.yml, application-acceptance.yml, schema.sql, seed.sql.
Zero duplicate file entries and zero filename-pattern matches for local YAML,
`.env`, SSH key names or PEM/P12/PFX/JKS/key files. No devtools, H2 or Mockito
runtime library is packaged. This inventory is not generic secret-DLP of every
third-party binary. No private configuration file was loaded as a test resource.

Manifest reports Spring Boot 4.0.8, Build-Jdk-Spec 17, JarLauncher and
`com.atstudio.atstudio.AtStudioApplication`. The public acceptance profile is
packaged because it is source; that is not permission to activate it. Native
runtime admission registration and effective target settings remain NOT RUN.

## Commands And Test Evidence

Installed tools: Gradle 9.3.0 revision
`701205ed2f78811508466c8e1952304c2ea869f5`; Oracle JDK 17.0.12+8-LTS-286;
javac 17.0.12; Windows 11 10.0 amd64. Gradle distribution cache was checked
before wrapper use. No fallback online run, clean, build scan or daemon reuse.

```powershell
.\gradlew.bat --offline --no-daemon --version
.\gradlew.bat --offline --no-daemon --console=plain compileJava --rerun-tasks
git diff --check -- src build.gradle settings.gradle gradle gradlew gradlew.bat
.\gradlew.bat --offline --no-daemon --console=plain test --rerun-tasks --tests 'com.atstudio.atstudio.security.StorageMutationAdmissionFilterTest' --tests 'com.atstudio.atstudio.security.StorageMutationAdmissionServletOrderTest' --tests 'com.atstudio.atstudio.security.StorageMutationAdmissionTransactionTest' --tests 'com.atstudio.atstudio.config.StorageMutationAdmissionBindingTest'
.\gradlew.bat --offline --no-daemon --console=plain bootJar --rerun-tasks
.\gradlew.bat --offline --no-daemon --console=plain dependencyInsight --dependency junit --configuration runtimeClasspath
```

All commands exited 0. Compilation: BUILD SUCCESSFUL 33s, one executed task.
Tests: BUILD SUCCESSFUL 1m17s, five executed tasks. Packaging: BUILD SUCCESSFUL
36s, four executed tasks. Dependency inspection: BUILD SUCCESSFUL 13s, one
executed task. This was forced execution, not an UP-TO-DATE-only receipt.
Diff/whitespace check passed; Git noted existing CRLF-to-LF normalization only.
Direct trailing-whitespace check of all nine changed source/test files passed.
The repository-wide lint script was inspected but not run: it recursively scans
unrelated/private JSON and requires external linters; no tool was installed.

| Suite | Tests | Failures | Errors | Skips | Seconds |
|---|---:|---:|---:|---:|---:|
| StorageMutationAdmissionBindingTest | 5 | 0 | 0 | 0 | 3.737 |
| StorageMutationAdmissionFilterTest | 46 | 0 | 0 | 0 | 3.724 |
| StorageMutationAdmissionServletOrderTest | 4 | 0 | 0 | 0 | 5.615 |
| StorageMutationAdmissionTransactionTest | 2 | 0 | 0 | 0 | 1.038 |
| Total | 57 | 0 | 0 | 0 | 14.114 |

XML evidence: `build/test-results/test/TEST-<suite>.xml`; report:
`build/reports/tests/test/index.html`. Exact XML hashes/timestamps are captured
in the manifest because normal build reports are mutable. These 57 equal WI014's
same focused selector count; they are deliberately a subset of MA's prior
105-test wider run, not a removal of tests or a new full-suite PASS. Selected
tests use isolated Spring contexts, mock servlet/JDBC objects and local Binder/
JavaMail Session creation; no app scan, listener, real DB or mail delivery.

Compiler output noted AppConfigMultipartBoundaryTest's existing deprecated API
and other unchecked test operations. Mockito emitted the usual CDS classpath
warning. No compilation error or test failure occurred. Coverage was NOT RUN;
the production/security coverage thresholds are not waived or marked passed.

### Runtime Dependency Observation

The first QA inventory probe rejected a test-named runtime dependency, then
read-only offline dependencyInsight established the existing path:

```text
junit:junit:3.8.2 -> 4.13.2 (selected by rule)
\--- com.googlecode.soundlibs:jlayer:1.0.1.4
     \--- com.googlecode.soundlibs:mp3spi:1.9.5.4
          \--- runtimeClasspath
```

JUnit 4.13.2 remains recorded in the 101-library inventory. No dependency was
excluded or altered. This is an advisory dependency-hygiene finding, not proof
of an exploitable vulnerability. No Java SAST plugin was configured in build.gradle
and semgrep was absent; SAST and current dependency-CVE scanning are NOT RUN.
No release security gate is declared satisfied by this local build.

## Frontend Addendum And Fail-Closed Blocker

Installed versions: Node 24.14.0, npm 11.9.0, TypeScript 5.6.3, Vite 6.4.3,
React/React DOM 18.3.1, Vitest 4.1.4, React plugin 4.7.0 and esbuild 0.25.12.
Public package-lock and installed node_modules/.package-lock.json hashes are
recorded. Package scripts/config were read before use. No install or npx fetch.

`npm.cmd run typecheck --offline` in frontend exited 0 (`tsc --noEmit`). npm
printed an update notice only; no update was requested. `rg` over src/index.html/
vite-env.d.ts found no import.meta.env/process.env/VITE_ reference. The Vite
config does load dotenv plus process.env and reads APP_PUBLIC_BASE_URL for its
development proxy, so uncontrolled environment exposure cannot be assumed safe.

The production-mode name probe used the installed Vite parser, with names-only
output, before any production build:

```javascript
import fs from 'node:fs';
import { loadEnv } from 'vite';
const files = ['.env', '.env.local', '.env.production', '.env.production.local']
  .filter(p => fs.existsSync(p));
const inheritedNames = Object.keys(process.env).filter(k => k.startsWith('VITE_'));
let effective;
try { effective = loadEnv('production', process.cwd(), 'VITE_'); }
catch { console.error('ENV_NAME_INSPECTION_FAILED'); process.exit(2); }
const exposedNames = Object.keys(effective);
console.log(JSON.stringify({ mode: 'production', files, inheritedNames, exposedNames }));
if (exposedNames.length) {
  console.error('UNEXPECTED_VITE_EXPOSED_KEYS');
  process.exit(3);
}
```

Run from frontend using `node --input-type=module -e <body>`. Result:
`.env.local` present; inherited VITE names empty; effective exposed names
`[VITE_GOOGLE_CLIENT_ID]`; gate rejected, PowerShell command receipt nonzero.
Private file bodies/values were not printed, persisted in reports, hashed or
copied. No acceptance tunnel origin is in these reports or a new frontend build.
No env file was edited or removed, and no hidden bypass/automatic retry occurred.

The planned new output `build/WI-20261010-ATS-026-frontend-dist` does not exist.
Production build, frontend tests and browser/runtime checks are NOT RUN.
MA needs an explicit scoped decision on an isolated env-free build, or review
and authorize an exact exposed public-key contract. Neither is inferred here.
Frontend preparation therefore remains incomplete, even though typecheck passed.

## Reproduction Of Artifact Identity

After reading the manifest, this read-only PowerShell verifier checks recorded
source inputs, unchanged previous dist, saved JARs and the built artifact. It
does not open private env files or run an application:

```powershell
$ErrorActionPreference = 'Stop'
$m = Get-Content deliverables/agent/WI-20261010-ATS-026-artifact.json -Raw | ConvertFrom-Json
$records = @($m.sourceIdentity.files) + @($m.frontend.files) + @($m.frontend.previousDist)
foreach ($record in $records) {
    if ((Get-FileHash -LiteralPath $record.path -Algorithm SHA256).Hash -ne $record.sha256) {
        throw 'INPUT_OR_PRESERVATION_IDENTITY_MISMATCH'
    }
}
foreach ($record in $m.backend.preservedArtifacts) {
    if ((Get-FileHash -LiteralPath $record.savedPath -Algorithm SHA256).Hash -ne $record.sha256) {
        throw 'SAVED_ARTIFACT_MISMATCH'
    }
}
$artifact = $m.backend.artifact
if ((Get-FileHash -LiteralPath $artifact.path -Algorithm SHA256).Hash -ne $artifact.sha256) {
    throw 'JAR_IDENTITY_MISMATCH'
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead((Join-Path (Get-Location).Path $artifact.path))
try {
    foreach ($record in @($artifact.classes) + @($artifact.resources) + @($artifact.libraries)) {
        $entry = $zip.GetEntry($record.entry)
        if ($null -eq $entry) { throw 'PACKAGED_ENTRY_MISSING' }
        $stream = $entry.Open()
        try { $hash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($stream)) }
        finally { $stream.Dispose() }
        if ($hash -ne $record.sha256) { throw 'PACKAGED_ENTRY_MISMATCH' }
    }
} finally { $zip.Dispose() }
'WI026_IDENTITY_PASS'
```

Enumeration and the full inventory, rather than this recorded-file-only verifier,
establish the absence of extra input files at the recorded checkpoint. Archive
checks also compared all entries/counts and class-major bytes at build time.

Final report validation at 21:41 KST executed the reproduction block above and
returned `WI026_IDENTITY_PASS`. Both reports passed seven required metadata
fields, metadata dependency existence, eight relative Markdown links and direct
whitespace checks. JSON parsing and planned-frontend-output absence passed;
scoped git diff --check exited 0 (untracked documents were checked directly).
Manifest SHA-256:
`0C4FF84654CBBAFC778C56C6DA04CFCC5028BEC25C7EB7CEB455D76039A2C959`.

## Risks / Rollback / Evaluation

The new JAR closes only the local artifact-identity gap. Applying it and Hikari10
still requires the exact target configuration and live admission proof. Fresh
MySQL canonical-manifest HOLD, first ADMIN, external secret backup, domain/DNS/
HTTPS, Provider/mail approval, backup/restore, alerts and load gates are unchanged.
Historical WI014/WI024 receipts were preserved; local checks do not update them.

No product rollback is needed: source was untouched, old JAR copies remain, old
plain JAR and frontend/dist remain unchanged. Do not copy old JARs over an active
runtime or delete evidence automatically. Any future deployment rollback needs
its own target approval; document corrections must remain within these outputs.

Evaluation: exact before/after hashes and forced execution provided bounded
identity evidence; the names-only frontend gate prevented accidental public
environment inclusion. Runtime JUnit shows why a broad filename heuristic needs
dependency provenance before drawing a security conclusion. Transient read-only
probes encountered a missing PATH jar executable, two nonexistent conventional
tsconfig names, a Windows glob error and a command-length limit; corrected with
existing .NET ZIP APIs, actual tsconfig.json and compact hash enumeration. No
dependency installation or source workaround was used.

## Follow-ups

WI026 blocks WI027. MA should immediately provide this report together with
WI025's completed procedure to PG, carrying frontend BUILD HOLD explicitly.
Review may assess the prepared backend/DB procedure without calling the SPA
complete. QA has released all Gradle/npm sessions; no descendant task or REQ
closure was created. WI028 remains the later actual-receipt integration owner.

## Related Documents

- [Handoff](WI-20261010-ATS-026-handoff.md): Original scope and frontend addendum.
- [REQ008](../user/REQ-20261010-ATS-008.md): Approved authority and exclusions.
- [User summary](../user/WI-20261010-ATS-026-summary.md): Compact status and blocker.
- [Artifact manifest](WI-20261010-ATS-026-artifact.json): File hashes and version evidence.
- [Prior admission review](WI-20261010-ATS-014-evidence-pack.md): Historical source review.

## Completion Addendum: Approved Isolated Frontend Build

**Current result: WI026 local backend/frontend artifact preparation PASS;
target NOT RUN / production HOLD.** This addendum supersedes only the historical
frontend build HOLD and incomplete-artifact statements above. Those statements,
the original environment probe and original manifest hash remain historical.
User explicitly approved the isolated env-free build after reviewing the
VITE_GOOGLE_CLIENT_ID blocker. The refreshed skill-generated handoff SHA-256 is
`E29F86CC765C8164D379DD66DA918BDCADD0EA5550AB4D74162C5C9CE47BD108`.

QA resumed the same WI. Preflight at 21:44:26 KST reconfirmed all 974 recorded
input files, the previous 143-file dist and the existing verified backend JAR.
No backend compilation or test rerun was necessary or performed. The prior
57-test receipt and backend JAR hash continue to identify the backend candidate.

### Isolated Execution And Config Equivalence

Actual execution: 2026-10-10 12:44:58.424Z through 12:45:12.940Z
(21:44:58-21:45:12 KST), exit 0. Node 24.14.0, TypeScript 5.6.3,
Vite 6.4.3, React plugin 4.7.0 and the previously recorded installed dependencies.
The installed dependency-lock, React-plugin metadata and Node executable hashes
still match their original manifest records.

The parent used .NET ProcessStartInfo, UseShellExecute=false, CreateNoWindow=true,
redirected stdin/stdout/stderr and absolute installed node.exe. It cleared the
child environment completely and set only `SystemRoot`, `WINDIR`, `TEMP`, `TMP`,
`PATH`, `NODE_ENV`. PATH contained the installed Node directory and Windows
System32 only; NODE_ENV was the explicit non-secret literal `production`.
No inherited integration value or NODE_OPTIONS was passed. Environment values
were not printed or recorded. No dotenv file was opened, copied or modified.

The child rejected any environment name outside those six. It first ran installed
TypeScript via `node node_modules/typescript/bin/tsc --noEmit`, then invoked the
installed Vite API. Both passed. This deliberately avoids npm script hooks and
tsc -b incremental metadata; no source file or old dist was changed.

Build configuration used `configFile:false`, `envDir:false`, `mode:'production'`,
`base:'/'`, default `react()` plugin and `@` alias resolving to frontend/src.
Those match the public production-affecting settings in vite.config.ts.
Server/proxy/acceptance and test settings were intentionally omitted. No private
config import, loadEnv call, VITE key value, Provider credential or target origin
was used. Vite defaults were retained apart from the new output location,
`emptyOutDir:false` and explicit `sourcemap:false`. A transient configResolved
gate verified these properties and production mode before output generation.

Resolved client environment names were exactly `BASE_URL`, `DEV`, `MODE`, `PROD`.
No `VITE_` key was admitted. The output directory was required not to exist
before execution; this was a single build, not an overwrite/retry.

The exact JavaScript and PowerShell invocation is stored in the manifest at
`frontendCompletion.exactPowerShellCommand`, with the output verification command
at `frontendCompletion.exactVerificationPowerShellCommand`. The former can be
reviewed without private configuration and intentionally refuses an existing
output directory. Do not rerun it over this artifact. A later build requires a
new approved unused output location and a new identity receipt.

### Artifact Identity And Verification

New output: `build/WI-20261010-ATS-026-frontend-dist`, 143 files,
1,194,351 bytes. Sorted-record aggregate SHA-256 uses the same UTF-8
`path + TAB + uppercase hash + LF` contract as the earlier source manifest:
`43A0384C5F48D100E4AF6B20D989A2186B34E6900CF4E69C94B010E9671C8647`.

| File | Bytes | SHA-256 |
|---|---:|---|
| assets/index-r1k6WbmR.js | 404816 | `10A9B5740C236F741DCC28DD818916B4CFFBD8B0B4D0CEC931F779B2A51366F3` |
| assets/PaymentOperationsPage-D_j8buNA.js | 57938 | `66B7440E9698945429B56D04B354BA06A78F8D254ABA19E12AD6BB3889EB9A68` |
| assets/index-Di84VtT3.css | 37117 | `80D81E157D141AEAE505462C15D3D7FCF448B894F189181CACDA7D3695A0FECC` |

All 143 file hashes are in `frontendCompletion.artifact.files`; resolved config,
timestamps, exact commands and provenance are in `frontendCompletion`.
The manifest's top-level status is now `LOCAL_ARTIFACTS_PASS_TARGET_NOT_RUN`.
The original `frontend.productionBuild` object is intentionally retained as a
historical HOLD snapshot, not the current result. Current manifest SHA-256:
`FBEB74425ADE0FE3882A9DDDC4C97F7B67B75DDA711FFA37ACBD755A02FA4D9C`.

At 21:46:26 KST, validation confirmed:

- All 974 recorded backend/frontend inputs unchanged, with frontend path-set
  enumeration also unchanged; no new frontend source file was introduced.
- Previous dist's 143 files unchanged; both saved JARs and current backend JAR
  unchanged. Branch/HEAD remain the prior main/58b1011 identity.
- Zero output reparse points or forbidden filename matches for env files,
  source maps, SQL, local application YAML or private-key extensions.
- Zero text-file matches for checked acceptance tunnel suffixes, private-key
  markers or uppercase VITE_ identifiers. This is a bounded static scan, not
  a general secret-DLP or a guarantee about arbitrary external endpoints.
- Both local HTML asset references resolve inside the new output directory.

The first case-insensitive VITE-marker probe matched Vite's generated lowercase
`vite__mapDeps` helper. Read-only diagnosis found no tunnel/private-key markers;
the corrected uppercase environment-identifier check passed. No artifact or
source was changed to suppress a finding.

Rollup reported two `CIRCULAR_DEPENDENCY` warning codes. No optimization/refactor
was authorized or performed. The largest emitted file is 404,816 bytes.
Existing index.html includes the public CDN font stylesheet reference from the
source; the build did not fetch it. Font availability, rendering, browser
behavior, API integration and actual network policy remain unverified.

Read-only reproduction for the new file identity, after checking the manifest:

```powershell
$ErrorActionPreference = 'Stop'
$m = Get-Content deliverables/agent/WI-20261010-ATS-026-artifact.json -Raw | ConvertFrom-Json
$artifact = $m.frontendCompletion.artifact
$actual = @(Get-ChildItem -LiteralPath $artifact.outputPath -Recurse -File)
if ($actual.Count -ne $artifact.fileCount) { throw 'FRONTEND_FILE_COUNT_MISMATCH' }
foreach ($record in $artifact.files) {
    if ((Get-FileHash -LiteralPath $record.path -Algorithm SHA256).Hash -ne $record.sha256) {
        throw 'FRONTEND_ARTIFACT_IDENTITY_MISMATCH'
    }
}
'WI026_FRONTEND_IDENTITY_PASS'
```

### Preservation And Handoff

Original report prefixes are retained exactly: Evidence Pack first 18,698 bytes
SHA-256 `525A3314D94CEC05F877067BF88A23726BA831CB141D2FBD07FA07A5E311CFB8`;
summary first 2,908 bytes SHA-256
`81334860FCB63BD6B11BF369C12ECA82B38DA8583B93474565403B797238A262`.
The previous manifest identity remains registered above and in the new completion
object. No old report, prior test receipt or artifact was erased.

The local frontend blocker is resolved. MA should supply the exact current
manifest/JAR/output identities to the already-running WI027 PG review. QA sent
no direct PG message, delegated no descendant and made no REQ closure. All build
processes have exited. WI028 remains responsible for receipt integration after
PG review and authorized MA operations. Local build success does not authorize
application startup, public release, billing, DB mutation or host changes.
