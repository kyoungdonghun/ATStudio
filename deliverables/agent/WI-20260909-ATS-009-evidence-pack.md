---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa-fe
category: evidence-pack
status: stable
related_wi: WI-20260909-ATS-009
dependencies:
  - path: WI-20260909-ATS-009-handoff.md
    reason: Assigned lint, formatting and build evidence scope
  - path: ../user/REQ-20260909-ATS-001.md
    reason: Approved bounded remediation
  - path: WI-20260909-ATS-007-evidence-pack.md
    reason: Exact final tested snapshot and common QA boundaries
---

# Evidence Pack: WI-20260909-ATS-009

## Summary

Final post-R1 frontend lint, formatting and production build PASS, each with MA-confirmed exit 0. The build reports Vite 6.4.3, 301 transformed modules and completion in 2.91s. The supplied final npm audit reports zero vulnerabilities. QA read artifacts and verified source/config hashes only; no runner, formatter, installation, fresh audit or browser/runtime operation was executed by QA.

## Scope / DoD Check

- [x] Assigned skill-generated handoff read first; exact two-set scope followed.
- [x] Final MA gates completed before acceptance; commands, exits and warning boundaries explicit.
- [x] Earlier formatting failure preserved; final output not confused with pre-F1/pre-R1 checks.
- [x] Quality scripts/configuration/exclusions unchanged; 334-source manifest matches both roots.
- [x] Only this report and `deliverables/user/WI-20260909-ATS-009-summary.md` created with `apply_patch`.
- [x] No source/test/config edits, heavy commands, subdelegation, Git writes or WI006 changes.
- [x] Return to MA/WI013/WI014 without deployment or parent REQ closure.

## Reference Documents (Tier 0-2)

| Tier | Document | Purpose |
|---|---|---|
| Entry | `AGENTS.md` | Owned scope, language and two-set delivery |
| 0 | `docs/standards/core-principles.md`; `development-standards.md`; `documentation-standards.md`; `glossary.md` in the same directory | Approved execution, stable gates, exact history and terminology; loaded in this QA task |
| 1 | `docs/policies/security-policy.md`; `docs/policies/quality-gates.md` | No external/sensitive mutation; distinguish warning, failure and acceptance |
| 2 | Approved REQ; WI009 handoff; `WI-20260908-ATS-018-findings.md`; WI004/WI005/WI010 evidence packs in `deliverables/agent/` | SEC-04/F1/R1, DEP-02 and retained implementation/formatter history |
| 2 | `frontend/package.json`; `.eslintrc.cjs`; `.prettierrc`; `.gitignore`; `vite.config.ts` under `frontend/` | Actual quality scripts, rules and output exclusions |
| Skills | `.agents/skills/create-wi-evidence-pack/SKILL.md`; `eslint/SKILL.md`; `prettier/SKILL.md`; `build-check/SKILL.md`; `react-best-practices/SKILL.md` under `.agents/skills/` | Evidence contract and scoped frontend quality reporting |

Injection: `.claude/config/context-injection-rules.json`, `qa-fe`, task type `testing`, required tiers `[0]`, plus assigned security/quality policies. Project tag `ATS` verified in workspace configuration. No subagent invoked.

## Evidence Pointers

Repository root: `C:/Users/jm991/Desktop/project/ATStudio`, `main`/`8161f0a` plus approved uncommitted changes. Artifact names below resolve under `output/release-remediation-20260909/`.

| Artifact | Outcome |
|---|---|
| `frontend-final-lint.log:2` | `eslint src --ext .ts,.tsx --max-warnings 0`; no reported diagnostics |
| `frontend-final-format.log:2` | `prettier --check . --ignore-unknown`; all matched files conform |
| `frontend-final-build.log:3` | `tsc -b && vite build`; Vite 6.4.3; 301 transformed modules at line 7; built in 2.91s at line 151 |
| `frontend-audit-final.json` | Empty vulnerabilities object, all severity counts zero, total zero |
| `frontend-final-snapshot.json`; `tested-source-final-check.json` | Exact final source identity and no mismatch result |
| `command-exit-status.json` | MA-transcribed actual tool exits: lint, format, build and npm audit all 0, with matching final artifact names |
| `frontend-format.log` | Earlier `src/api/client.test.ts` formatting warning/failure checkpoint |
| `frontend-install.log` | Earlier isolated installation: 322 packages added, 323 audited, six deprecation warnings, zero vulnerabilities |

Isolated destination: `C:/Users/jm991/AppData/Local/ATStudio/validation/release-remediation-20260909-frontend-5c1364c2`. QA matched all 334 input hashes in this destination and the shared workspace against the final manifest, recorded 05:01:02.4007414 KST. No root `.env*` file was present. No secret value was read. The generated `dist` belongs to this isolated build, not the running public application.

| Artifact / input | SHA-256 |
|---|---|
| `frontend-final-lint.log` | `C4C47E50498BADC4D8ED21074828D2032D31995CBF84B8AE53E2089C5B77D526` |
| `frontend-final-format.log` | `3FB734AC82663E5623D79AAE32855628B81FA1D15F9D42FC8B968DCCA368663E` |
| `frontend-final-build.log` | `D7A388222753CF70E69D09BE4A1B8AF27FCF8D0E8C053A24D9039F882DE68098` |
| `frontend-audit-final.json` | `CF4E269EC69D2FD2786330B6392ACE191C6E21780FAFE635889F04DD5CF91A81` |
| `frontend-final-snapshot.json` | `F182B3E3921F692BED260166C08BAE036BBD5792D47E1E860AA0D3490353C530` |
| `frontend/package-lock.json` | `0BD31892B1D7A2A84A1BF7AFC0FC69772ED6685E46AC513B2E8F27E2A52FDA92` |
| `frontend/.eslintrc.cjs` | `4BEC242C0E5B991CEFD939B6E3F31F7646D5D01F5B17E2D0E9A2905F5B84354C` |
| `frontend/.prettierrc` | `C043A3FC85F17FE1D1E4198DE8387969CB1D2BC6DD921F3C16444579FC797AA4` |

## Commands & Outputs

Executed serially by MA in the isolated frontend, **not by QA**:

```powershell
npm run lint
npm run format
npm run build
```

MA explicitly confirmed exit 0 for all three final commands. The text logs contain commands/results but no independent numeric shell-exit field; exit provenance is MA's completion message. The final build duration of 2.91s is Vite's reported bundling duration, not the entire npm command including TypeScript. No standalone lint/formatter duration or matched-file count is recorded, so none is inferred.

MA subsequently persisted these exits in `command-exit-status.json`: frontend lint/format/build rows have `exit_code = 0`, and `npmAudit.exitCode = 0`. The record explicitly identifies itself as **MA transcription from actual tool results**, not an inference from quiet logs or an independent QA execution. The paired artifact names agree with this report's final pointers; preclosure documentation exits remain separate from later document validation.

QA executed scoped read-only log/config reads, structured audit/package JSON parsing, current source/copy hashes and unchanged-gate diff as recorded in WI007. No `npm`, ESLint, Prettier, Vite, browser, server or audit process was started. The initial npm-lock JSON parser attempt was corrected with `-AsHashtable` for its empty root key; subsequent sampled comparisons matched, rather than treating null failed-parse values as actual lock differences.

### Unchanged Rules And Exclusions

- `package.json`: lint remains `eslint src --ext .ts,.tsx --max-warnings 0`; format remains read-only `prettier --check . --ignore-unknown`; build remains `tsc -b && vite build`.
- `.eslintrc.cjs`: existing recommended JavaScript/TypeScript/React Hooks rules, Prettier integration and React Refresh warning rule remain. The zero-warning script is unchanged. Ignores remain `dist` and `.eslintrc.cjs`; no new source/test exclusion was added.
- `.prettierrc`: existing semicolons, single quotes, all trailing commas, print width 100 and tab width 2 remain.
- Source and isolated `.gitignore` match. `/coverage/` is anchored to generated root output, not `src/test/coverage`. Existing ignored paths also include node_modules, dist, dist-ssr, root tsbuildinfo, local and env files. No custom `.prettierignore` exists in either inspected root. The four source coverage-test files are included in the final 334-input manifest and were not omitted again.
- `vite.config.ts` coverage thresholds/exclusions and the TypeScript compiler configuration are unchanged from `8161f0a`. The exact read-only no-diff command is in WI007. No rule, threshold or ignore pattern was weakened to obtain PASS.

## Results, Warnings And History

| Check | Final outcome | Warnings / limits |
|---|---|---|
| ESLint | PASS, MA exit 0 | No reported errors or warnings; zero-warning gate retained |
| Prettier | PASS, MA exit 0 | All matched files conform; no formatter write by QA |
| Build | PASS, MA exit 0 | 301 modules; no warning/error diagnostics in final log; bundle creation is not browser acceptance |
| Supplied final npm audit | 0 reported vulnerabilities, all severity counts 0 | Read-only review of MA's generated result; not a fresh QA scan or guarantee of no undiscovered vulnerabilities |

The largest JavaScript asset reported by the final build is `dist/assets/index-DhFAZSkI.js`, 404.42 kB (gzip 131.14 kB). This is logged bundle output, not measured download latency, heap use or a performance benchmark. No bundle restructuring is requested in this evidence-only WI.

The earlier `frontend-format.log` reports a style issue in `src/api/client.test.ts`; it is not PASS. WI004 records the authorized single-file formatting correction, followed later by formatting of only `LoginPage.test.tsx` in the nine-file F1 check and a read-only R1 formatting check. Those implementation-owned changes preserved assertions/behavior as documented there. The final `frontend-final-format.log` is the post-R1 PASS; prior logs remain untouched.

The installation log retains six deprecation notices: inflight, config-array, rimraf, glob, object-schema and ESLint. They are installation warnings, not final ESLint failures. Neither zero audit findings nor successful build removes those maintenance notices. WI005's disclosed live hidden-lock metadata change remains historical evidence; no live dependency repair or installation was performed by QA, and isolated validation does not prove the running Vite installation was updated.

QA read installed package manifests for TypeScript 5.6.3, Vite 6.4.3, Vitest 4.1.4, ESLint 8.57.1, Prettier 3.8.1, React 18.3.1, Zustand 5.0.11, PostCSS 8.5.28, Undici 7.29.1, js-yaml 4.3.2 and nanoid 3.3.18. All eleven sampled versions match the isolated lock. This is a bounded sample, not a byte audit of every installed dependency. MA's fresh `npm ci` and final audit remain the installation/audit execution evidence.

## Risks / Rollback

- Production-mode bundle creation is not deployment, public-browser, mobile, actual-account or provider acceptance. No application/dev server was launched or restarted, and no generated bundle was installed into the pinned runtime.
- No product policy, source/test/config, real DB/DDL/media/log/secret/runtime/provider/mail or Git mutation occurred. Shared SR-93, unrelated changes, earlier evidence and WI006 were preserved.
- Rollback is limited to scoped corrections of the two WI009 reports; deletion requires approval. Do not remove source/tests/history, reset shared work or repair live dependencies as part of report rollback.
- The MA-reported preclosure documentation validator PASS with 702 IDs predates WI013 edits and this six-report QA package. It is not a final post-edit documentation PASS and was not rerun by QA.

## Follow-ups / Chain

Bounded report checks passed for both WI009 outputs: required metadata present, link/dependency targets exist, and no trailing whitespace or UTF-8 replacement characters. No full repository documentation validator or additional audit was run by QA.

Ready list was final lint, format and build on the R1 snapshot; MA completed all three. **No additional heavy commands or dependency audit requested.** WI009 evidence-only delivery is complete. Return the WI007/WI008/WI009 package promptly for WI013 documentation, then WI014 integration. WI010's independent F1/R1 review is closed; remaining document/REQ/deployment decisions stay with MA.

## Related Documents

- [Assigned WI009 handoff](WI-20260909-ATS-009-handoff.md)
- [WI009 user summary](../user/WI-20260909-ATS-009-summary.md)
- [Final frontend regression and snapshot](WI-20260909-ATS-007-evidence-pack.md)
- [Type safety](WI-20260909-ATS-008-evidence-pack.md)
- [Implementation and formatting history](WI-20260909-ATS-004-evidence-pack.md)
- [Independent F1/R1 review](WI-20260909-ATS-010-evidence-pack.md)
