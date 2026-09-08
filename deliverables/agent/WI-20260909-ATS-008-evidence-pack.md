---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa-fe
category: evidence-pack
status: stable
related_wi: WI-20260909-ATS-008
dependencies:
  - path: WI-20260909-ATS-008-handoff.md
    reason: Assigned type-safety evidence scope
  - path: ../user/REQ-20260909-ATS-001.md
    reason: Approved remediation and serialized MA runners
  - path: WI-20260909-ATS-007-evidence-pack.md
    reason: Exact final frontend snapshot and source hashes
  - path: WI-20260909-ATS-006-evidence-pack.md
    reason: Existing backend compilation evidence without reopening backend work
---

# Evidence Pack: WI-20260909-ATS-008

## Summary

Post-R1 frontend typecheck PASS: MA confirms exit 0 for `npm run typecheck`; the completed final log records `tsc --noEmit` with no diagnostics. QA verified the tested 334-input snapshot and unchanged compiler configuration. Existing successful backend compilation is referenced from completed WI006, not rerun or re-audited.

## Scope / DoD Check

- [x] Skill-created WI008 handoff read before analysis; approved scope followed.
- [x] Actual final typecheck command, exit provenance, compiler scope and unrun checks explicit.
- [x] Backend compilation history referenced without new backend work or WI006 edits.
- [x] Only this evidence pack and `deliverables/user/WI-20260909-ATS-008-summary.md` created with `apply_patch`.
- [x] No compiler/heavy runner, product/test/config edit, subdelegation, Git mutation or runtime/external action.
- [x] Return completed evidence to MA for WI013/WI014; parent REQ remains MA-owned.

## Reference Documents (Tier 0-2)

| Tier | Document | Purpose |
|---|---|---|
| Entry | `AGENTS.md` | Ownership and two-set English reporting |
| 0 | `docs/standards/core-principles.md`; `development-standards.md`; `documentation-standards.md`; `glossary.md` in the same directory | Approved execution, Java/TypeScript conventions, exact evidence and preservation; loaded in this QA task |
| 1 | `docs/policies/security-policy.md`; `docs/policies/quality-gates.md` | No sensitive/runtime mutation and no gate weakening |
| 2 | Approved REQ; WI008 handoff; `WI-20260908-ATS-018-findings.md`; WI004/WI005/WI010 evidence packs in `deliverables/agent/` | SEC-04/F1/R1 and dependency context |
| 2 | `frontend/package.json`; `frontend/tsconfig.json`; WI007 evidence pack | Actual scripts, compiler scope and final snapshot |
| 2 | `deliverables/user/WI-20260909-ATS-006-summary.md`; WI006 evidence pack | Already completed backend compilation and isolated-environment limits |
| Skills | `.agents/skills/create-wi-evidence-pack/SKILL.md`; `typecheck/SKILL.md`; `build-check/SKILL.md` under `.agents/skills/` | Report structure and compiler/build evidence |

Injection source: `.claude/config/context-injection-rules.json`; `qa-fe`, task type `testing`, required tiers `[0]`, plus handoff security/quality policies. Project tag is `ATS`; no subdelegation.

## Evidence Pointers

Repository root: `C:/Users/jm991/Desktop/project/ATStudio`, `main`/`8161f0a` plus approved changes. Artifact base: `output/release-remediation-20260909/`.

| Pointer | Meaning |
|---|---|
| `frontend-final-typecheck.log:2` | Final package command: `typecheck`, then `tsc --noEmit`; no compiler diagnostics |
| `frontend-final-build.log:3` | Separate final `tsc -b && vite build` reached successful Vite bundling; supplementary compilation evidence, owned by WI009 |
| `frontend-final-snapshot.json` | Exact 334-input snapshot recorded 05:01:02.4007414 KST after R1 correction |
| `tested-source-final-check.json` | MA's final unchanged-source record; frontend 334 inputs with no issues |
| `command-exit-status.json`, `frontend` typecheck row | Persisted MA transcription of the actual tool result: `exit_code = 0`, exact final typecheck log |
| `frontend/tsconfig.json` | Actual single compiler configuration, unchanged from `8161f0a` |
| WI006 evidence pack, Preserved Execution History | First InputBuffer compile failure, fixture correction and eventual full success retained |

Isolated root: `C:/Users/jm991/AppData/Local/ATStudio/validation/release-remediation-20260909-frontend-5c1364c2`. QA independently matched all 334 manifest entries in both source and isolated roots, including `package.json`, `package-lock.json` and `tsconfig.json`. Installed TypeScript 5.6.3 matches that isolated lock. No `.env*` file was present at the isolated root.

| Input / log | SHA-256 |
|---|---|
| `frontend-final-typecheck.log` | `60A6F6E3B322355690CEA80154C8347322E4D21C03FF61BE62162D17D3D2C0AA` |
| `frontend-final-snapshot.json` | `F182B3E3921F692BED260166C08BAE036BBD5792D47E1E860AA0D3490353C530` |
| `frontend/tsconfig.json` | `D9D66AB08C6E944D8CB7ABF7623477A5F6E621364DDD8F47576CBBC15C399182` |
| `frontend/package.json` | `81B927632F8CD435F2A3BE1C19FA9614EE6AA300889989BFDB1AA8EF1B20529C` |

## Commands & Outputs

**Executed by MA, not QA**, in the isolated frontend root:

```powershell
npm run typecheck
```

Package script: `tsc --noEmit`. MA's final completion message explicitly confirms exit 0. The quiet four-line log independently confirms the command and absence of diagnostics, but does not contain its own numeric shell exit record. No elapsed compiler duration or exact visited-file count is recorded, so neither is invented. The 334-input manifest count is not a compiler file count.

The later `command-exit-status.json` persists that typecheck exit as 0, paired with `frontend-final-typecheck.log`. It is labelled **MA transcription from tool results**, not an inference from silence or a QA compiler execution. Its backend row also records MA's earlier command `gradlew.bat build --no-daemon --max-workers=2 --console=plain`, exit 0, `backend-full-final.log`; this supplemental pointer does not reopen backend work or modify WI006.

QA executed read-only artifact/config parsing, source/copy SHA-256 comparison and the unchanged-gate diff documented in WI007. No `tsc`, Gradle, npm, application process or backend proof was started. An initial lookup for conventional split tsconfig files found none; the existing single `tsconfig.json` is the authoritative scope.

## Type-Safety Results

| Check | Result | Boundary |
|---|---|---|
| Final standalone TypeScript check | PASS, MA-confirmed exit 0, no reported diagnostics | Corrected R1 snapshot; `frontend-final-typecheck.log` |
| Final build compiler stage | PASS implied by successful `tsc -b && vite build`, with MA-confirmed build exit 0 | Separate WI009 evidence; not another QA compiler run |
| Backend compilation | Existing WI006 PASS | No backend source/config/run reopened |

Compiler configuration remains `strict: true`, `noUnusedLocals: true`, `noUnusedParameters: true`, `noFallthroughCasesInSwitch: true`, `forceConsistentCasingInFileNames: true`, `noEmit: true`, bundler resolution and ES2020 target. Existing `skipLibCheck: true` remains; this is not exhaustive dependency-declaration validation. `include` is `['src', 'vite-env.d.ts']`, including source tests but not an independent strict compiler pass over root `vite.config.ts` or `vite.config.test.ts`. Vite loading/bundling does not broaden that standalone TypeScript scope.

### Backend Cross-Role Evidence Only

WI006 records Boot 4.0.8 build success in 2m 20s, with compilation/package tasks up-to-date in the final run. Its first full attempt failed at `compileTestJava` because the multipart fixture used an invalid InputBuffer lambda. The next compile succeeded but exposed nine fixture NPEs; the corrected fixture compiled/passed focused tests and the final full backend gate. This sequence is preserved, not rewritten as a warning-free first-pass compilation.

The same WI006 report records 620 matched backend inputs and an unchanged built JAR hash. MA's newer `tested-source-final-check.json` reports 620 entries/no issues, without any new backend operation by QA. H2/fake/temp and 19 backend skip limitations remain in WI006. This WI provides no new MySQL, Provider/mail, runtime or production evidence.

## Risks / Rollback

- Static TypeScript PASS does not establish dynamic session ownership, browser behavior, data/Provider contracts or production correctness. WI007 and WI010 provide separate automated and independent-review evidence; WI010 now closes F1/R1.
- Source/test/config files, compiler exclusions, product policy, WI006 and unrelated shared changes were preserved. No real DB/DDL/media/log/secret/account/provider/mail/runtime/Git mutation occurred.
- Rollback is limited to correcting these two WI008 reports; removal requires approval. Never reset shared code or remove evidence to change a result.
- Full documentation validation after WI013 edits remains with MA/docops. The earlier reported 702-ID preclosure PASS is not a post-edit validation claim.

## Follow-ups / Chain

Bounded report checks passed for both WI008 outputs: required metadata present, link/dependency targets exist, and no trailing whitespace or UTF-8 replacement characters. This is report validation only, not another compiler or repository-wide documentation run.

Ready check requested from MA was the final standalone typecheck; it is complete. **No additional compiler/heavy test requested.** WI008 evidence delivery is complete and unblocks its contribution to WI013/WI014. No parent REQ or deployment closure by this role.

## Related Documents

- [Assigned WI008 handoff](WI-20260909-ATS-008-handoff.md)
- [WI008 user summary](../user/WI-20260909-ATS-008-summary.md)
- [Frontend snapshot and regression evidence](WI-20260909-ATS-007-evidence-pack.md)
- [Existing backend evidence](WI-20260909-ATS-006-evidence-pack.md)
- [Frontend final build evidence](WI-20260909-ATS-009-evidence-pack.md)
