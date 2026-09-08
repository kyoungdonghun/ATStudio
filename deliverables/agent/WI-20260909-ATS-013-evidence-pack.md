---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260909-ATS-013-handoff.md
    reason: Owned scope and dependency gate
  - path: ../user/REQ-20260909-ATS-001.md
    reason: Approved bounded remediation
  - path: WI-20260909-ATS-006-evidence-pack.md
    reason: Final backend verification
  - path: WI-20260909-ATS-007-evidence-pack.md
    reason: Final frontend regression and coverage
  - path: WI-20260909-ATS-008-evidence-pack.md
    reason: Final typecheck and exit provenance
  - path: WI-20260909-ATS-009-evidence-pack.md
    reason: Final lint, format, build and audit
  - path: WI-20260909-ATS-010-evidence-pack.md
    reason: Final authentication F1 and R1 review
  - path: WI-20260909-ATS-011-evidence-pack.md
    reason: Payment F1 closure
  - path: WI-20260909-ATS-012-evidence-pack.md
    reason: Storage and dependency review
---

# Evidence Pack: WI-20260909-ATS-013

## Summary

Updated twelve current-contract documents after MA's dependency-ready signal.
The new dated matrix below records bounded source/test closure, without
rewriting the historical WI018 register. The pinned public backend is **not
deployed** from this patch; production remains **HOLD**. WI006-WI009 final
packets and WI010-WI012 review dispositions have now been read and accepted
for this documentation handback. WI013 scoped documentation/evidence work is
complete. MA's 05:17 post-update docs checkpoint passed 702 IDs; the final
validator after the two review nits/report completion and WI014 integration
remain downstream, without a claim that those final gates already passed.

## Scope / DoD Check

- [x] Read the assigned handoff first; respected read-only preparation until
  MA explicitly authorized dependency-ready documentation edits.
- [x] Changed only the twelve current docs listed below and this WI's two
  reports, using `apply_patch`; no subdelegation or heavy runner.
- [x] Preserved product policies, schema, historical records and unrelated
  edits; kept the pre-existing SR-93 review paragraph and added later status.
- [x] Read final raw artifacts and dated independent-review dispositions;
  distinguish MA execution, docops static checks and not-run target checks.
- [x] Received and checked WI007/WI008/WI009 final evidence packets and MA's
  formal dependency acceptance; final repository docs validation is MA-owned.
- [x] Return completed scoped documentation/evidence to MA for WI014, including
  the explicit outstanding post-edit document validator gate.

## Reference Documents (Tier 0-2)

| Tier | Documents | Applied purpose |
|---|---|---|
| Entry | `AGENTS.md`; assigned WI013 handoff | Owned scope, Korean conversation, English technical reports, preserved shared state |
| 0 | `docs/standards/core-principles.md` | Approved bounded execution and transparency |
| 0 | `docs/standards/development-standards.md` | Java 17, transaction/DTO boundaries, risk-scoped evidence |
| 0 | `docs/standards/documentation-standards.md`; `docs/standards/glossary.md` | Metadata, links, canonical terms and historical/current separation |
| 1 | `docs/policies/security-policy.md`; `docs/policies/quality-gates.md` | No sensitive/runtime/provider operations; unchanged quality thresholds |
| 2 | Approved REQ; historical WI018 register; WI001-012 evidence | Finding/source/test mapping, final QA and later dispositions |
| Skills | `.agents/skills/create-wi-evidence-pack/SKILL.md`; `.agents/skills/validate-docs/SKILL.md` | Two-set evidence format and MA-ready document validation |

MA injected the Tier 0 summary; docops checked the linked live guidance.
`.claude/config/context-injection-rules.json` lists docops required tiers
`[0, 1]`; `.claude/config/workspace.json` confirms tag `ATS`. No subagent was
called. No past observation is treated as current runtime acceptance.

## Evidence Pointers

Repository root: `C:/Users/jm991/Desktop/project/ATStudio`. Results describe the
patched shared working tree based on main `8161f0a`, not the unmodified commit
or running pinned backend. Paths below are repository-relative.

| Changed current document | Bounded change |
|---|---|
| `docs/policies/security-policy.md` | Token purpose/rotation, passwordless OAuth boundary, reset atomicity, log minimization, session ownership, retained history |
| `docs/design/usecase/user-info.md` | Access-only Bearer, stale-refresh rejection, reset and login/social owner boundaries including synchronous R1 |
| `docs/design/api-spec.md` | Existing route contracts: purpose tokens, preserved history, active-Track prerequisite, cleanup and finite multipart parsing |
| `docs/design/usecase/sound-track.md` | User-then-Track lock, retained License/event/quota behavior and stale-media write prevention |
| `docs/design/usecase/user-license.md` | Retained License identity/history versus inactive-Track download rejection |
| `docs/design/usecase/sound-album.md` | Transactional thumbnail detach, after-commit cleanup, rollback/shared/inactive references |
| `docs/design/usecase/sound-playlist.md` | Same thumbnail lifecycle; existing membership deletion unchanged |
| `docs/design/runtime-storage-operations.md` | Prospective journal ownership, retained references and strict audit; no historical repair or distributed guarantee |
| `docs/design/payment-integration-design.md` | Conditional source suffix, returning purchase, competing-intent and local-mutation fences |
| `docs/design/p1-payment-integrity-remediation-design.md` | Dated source-bound extension while preserving the historical design |
| `docs/design/payment-operations-runbook.md` | Separately approved admission pause/drain/reconcile, no mixed writers or rehashing |
| `docs/SR/SR-93.md` | Preserve the existing dirty review paragraph; link later matrix and not-deployed HOLD boundaries |

New reports: this file and `deliverables/user/WI-20260909-ATS-013-summary.md`.
No blanket document rewrite, other index/standard update, product/test/config
change or historical register edit belongs to WI013.

## Dated Closure Matrix (2026-09-09)

`CLOSED` below means the approved prospective source/test correction and bounded
independent review, not production deployment or repair of existing data.
Java source abbreviations use `src/main/java/com/atstudio/atstudio/`; Java test
classes use `src/test/java/com/atstudio/atstudio/`. Exact owned files and hashes
are in the linked implementation/review packs and final source manifests.

| Finding | Current disposition and source | Executed evidence / remaining boundary |
|---|---|---|
| SEC-01 | CLOSED: `security/JwtTokenProvider.java:90`, `security/JwtAuthenticationFilter.java:34`; explicit access/refresh purpose | WI006: `security/JwtTokenPurposeTest` 17 and `security/AuthTokenSecurityIntegrationTest` 6 pass; WI010 review. Typeless sessions must log in after deployment; access-token TTL policy unchanged. |
| SEC-02 | CLOSED: `common/exception/GlobalExceptionHandler.java:71`; bounded diagnostics omit rejected values and throwable detail | WI006: `common/exception/SensitiveValidationLogTest` 4 pass; WI010. No historical log purge or infrastructure capture certification. |
| SEC-03 | CLOSED: `security/JwtTokenProvider.java:52`; unique refresh issuance ID and digest rotation | Purpose/security suites above include real synthetic-token uniqueness and rejection; counts overlap SEC-01. No secret rotation performed. |
| SEC-04 | CLOSED: `frontend/src/api/client.ts`, `frontend/src/store/authStore.ts`; session-generation ownership of refresh/replay/cleanup | WI010 latest addendum and final 1,563-test suite. No browser, live OAuth or multi-tab acceptance inferred. |
| WI010 F1/R1 | CLOSED: login/social ownership plus `authStore.ts:69` recheck after synchronous clear notification, before token writes | MA original-source R1 run: all 5 selected cases fail; fixed focused run: 163/163 pass; final 1,563/1,563 pass. WI010 05:05 KST addendum supersedes its earlier pending-FE text. |
| SEC-05 | CLOSED: `service/EmailService.java:144`, `repository/PasswordResetTokenRepository.java:22`; User-then-token single consumption and atomic password/refresh update | WI006: `service/PasswordResetConsumptionIntegrationTest` 3 pass; WI010. Isolated H2, not MySQL concurrency proof. |
| AUTH-06 | CLOSED: `service/auth/AuthService.java:66,101`; social digest replacement and passwordless refresh without password-email gate | WI006 auth integration 6 pass and AuthService regressions; WI010. Email is not marked verified; no disabled Provider enabled. |
| PAY-01 | CLOSED: `service/BillingAgreementPrepareTransactionService.java:114`, `service/PaymentCommandTransactionService.java:533`; expired retained source is bound/revalidated and reused | WI006: `service/PaymentReturningSubscriberIntegrationTest` 12 pass; WI011. Legacy unbound charged returns require reviewed disposition, not invented source evidence. |
| PAY-02 | CLOSED: `service/PaymentCommandKeyFactory.java:53`, `service/PaymentCommandTransactionService.java:97`; priced source, competing-intent fence and finalize-only recovery | WI006: `service/SubscriptionUpgradeCommandIntegrationTest` 21 pass; WI011. Annual current period and next-cycle policy unchanged. |
| WI011 F1 | CLOSED: `service/UserSubscriptionService.java:125,201,217,239`; locks before mutable read and busy fence | WI006 final run: `service/SubscriptionMutationFenceIntegrationTest` 10 pass; WI011 dated executed closure. Earlier whole-build failure was multipart-only and is superseded by WI006 final build. |
| DATA-01 | CLOSED: `service/TrackService.java:217`, `service/DownloadService.java:46`; preserve License/events, keep quota and active-Track checks | WI006: `service/storage/RetainedHistoryStorageIntegrationTest` 22 pass (shared set); WI012. No reconstruction of already deleted history. |
| DATA-02 | CLOSED: `entity/Album.java:53`, `entity/Playlist.java:42`, corresponding services and `storage/StorageReferenceChecker.java`; detach then reference-aware cleanup | Same 22-case retained-history set includes Album/Playlist shared/no-thumbnail, rollback and strict startup; WI012. Old broken references still fail strict audit. |
| DATA-03 | CLOSED: `repository/TrackRepository.java:21`, Track/Like/Download/AlbumLike writers; narrow lock protocol | WI006: `service/TrackMutationConcurrencyIntegrationTest` 8 and `service/DownloadConcurrencyContractTest` 3 pass, plus shared Album cases; WI012. H2 controls are not original-checkout RED or production deadlock proof. |
| STORAGE-04 | CLOSED: `service/storage/LocalStorageService.java`, `StorageMutationCoordinator.java:68`, `StorageMutationJournalService.java`; own targets before staged writes | WI006: coordinator 11, journal 6, LocalStorage 13 pass/1 skip, plus shared integration cases; WI012. Single-process registry; recovery batch fairness remains maintenance. |
| DEP-01 | Selected compatible source/build patch CLOSED: `build.gradle`, Boot 4.0.8; built inventory Security 7.0.7/Tomcat 11.0.24 | WI005/WI012 and WI006 full backend gate. Sample built JAR differs from pinned public runtime. No exhaustive Maven/OS SCA or deployed proxy assurance. |
| DEP-02 | Compatible dev lock patch and isolated verification CLOSED: `frontend/package-lock.json` | MA final isolated audit JSON: zero findings, and final frontend gates PASS. Live installed code remains old; hidden-lock metadata changed, as disclosed by WI005/WI012. No live install repair. |
| OPS-01 | Finite parser correction CLOSED: `config/AppConfig.java:13`; 128 parts, existing byte/header/parameter bounds retained | WI006: `config/AppConfigMultipartBoundaryTest` 10 pass; earlier fixture failures preserved. Target body/time/concurrency/proxy budgets remain HOLD checks. |

## Commands And Outputs

### Executed By Docops

- `Get-Content -LiteralPath ... -Encoding UTF8`, focused `rg -n`/`rg --files`
  and read-only `git --no-optional-locks diff` inspected named docs, source,
  tests, review packets and raw verification artifacts. One preparatory search
  named absent `docs/guides`; actual candidates were resolved in `docs/design`.
- `apply_patch` edited exactly the twelve current docs and created two owned
  reports. Before-images of the twelve docs were retained in memory for scoped
  comparison; no extra snapshot/report file was written.
- Scoped `git --no-optional-locks diff --check -- <12 owned docs>` returned 0.
  Git emitted CRLF-to-LF normalization notices for two existing use-case files;
  these are not test failures or permission to normalize unrelated files.
- `ConvertFrom-Json` plus `Get-FileHash -Algorithm SHA256` compared all 149
  entries in `preserved-artifacts.json`: zero missing/mismatched files. This
  includes the original WI018 findings/evidence/handoff/summary. No old PASS
  text or prior risk disposition was rewritten.
- The same read-only hash comparison checked all 620 backend and 334 frontend
  manifest entries after current-doc edits: zero mismatches. It independently
  agrees with MA's `tested-source-final-check.json`; it is not a new test run.
- Read MA's `preservation-final.json`, recorded 05:15:24 KST: 149 protected
  files unchanged, all 468 prior SR-93 lines preserved in order, empty schema
  diff and unchanged main HEAD. This is separately attributed MA evidence;
  the 149-file and tested-source hash checks above were also executed by docops.
- MA's two review nits were corrected: keep Track postcondition bullets together
  before the closure link; name the actual `payment_orders.status` field and
  `PaymentOrderStatus` enum. There is no separate entity `commandStatus`;
  `UNKNOWN` outcome maps to `PENDING_PROVIDER_CONFIRMATION`. An initial guessed
  `PaymentCommandStatus.java` lookup was absent; actual entity/enum/repository
  reads resolved the contract before the correction.
- Scoped version-reference search of `docs/index.md`, `docs/design/index.md`
  and `docs/design/api-spec.md` found 30.13 only in the API metadata/title and
  no 30.12 reference in those two indexes. This is not a whole-repository
  version-drift audit and caused no index expansion.
- Post-nit static checks covered all fourteen owned files: 71 local Markdown
  file-link targets resolved; no trailing whitespace, replacement characters,
  unbalanced fences or missing report metadata/dependency targets. All 468
  prior SR-93 lines remain in order by direct comparison with the protected
  snapshot. This bounded check is not the repository-wide validator. The first
  orchestration script failed JavaScript parsing before any command ran; the
  corrected read-only check exited 0 with no issues.

### MA Execution Reviewed, Not Re-executed By Docops

All raw names below are under `output/release-remediation-20260909/`.

| Gate | Result and exact artifact |
|---|---|
| Backend final build/test/JaCoCo | `backend-full-final.log`, `backend-full-final-results.json`, `backend-coverage-final.json`; WI006 reconciles 200 XML suites, 1,825 total, 1,806 passed, 19 skips, zero failures/errors. Build 2m20s; compilation/package tasks up-to-date, not a clean build. |
| Backend coverage | LINE 88.5599%, METHOD 86.2736%, BRANCH 74.4033%. Seven configured critical classes have 100% LINE/METHOD, not universal 100% branch coverage. Unchanged gates PASS per WI006. |
| Frontend R1 RED | `frontend-r1-red.log`, `frontend-r1-red-snapshot.json`: 5 selected cases fail on the earlier source; 57 name-filter exclusions, not permanent skips. |
| Frontend fixed focused | `frontend-r1-green-focused.log`: 5 files, 163 tests pass; not additional unique cases beyond the full suite. |
| Frontend final coverage | `frontend-r1-full-final-coverage.log`, `frontend-coverage-final.json`: 112 files, 1,563 tests pass; S90.26/L92.84/F91.21/B82.83, 79.28s. The navigation-not-implemented diagnostic is not browser verification. |
| Typecheck/lint | `frontend-final-typecheck.log` (`tsc --noEmit`), `frontend-final-lint.log` (`eslint src --ext .ts,.tsx --max-warnings 0`). WI008/WI009 and MA's `command-exit-status.json` confirm exit 0; quiet logs alone do not encode exit status. |
| Format/build | `frontend-final-format.log` confirms Prettier check success; `frontend-final-build.log` completes in 2.91s. MA confirms PASS. |
| Dependency audit | `frontend-audit-final.json`: zero findings including dev dependencies, as a dated isolated audit only. |
| Tested source identity | `backend-tested-source-manifest.json` 620; `frontend-final-snapshot.json` 334; `tested-source-final-check.json` has empty issues for both. |
| Sample build inventory | `built-artifact-dependencies.json`: Boot 4.0.8, Security 7.0.7, Tomcat 11.0.24, Data JPA 4.0.7, Framework 7.0.9; sample SHA-256 `8117DF561E633706C6DA78BDE39BA3B5155982B9AA604BE1413EBB0FE404D7A0`. Inventory evidence, not deployed-runtime evidence. |

`command-exit-status.json` is explicitly MA's transcription of actual tool
results, not a raw shell transcript or inference from quiet logs. It records
exit 0 for `gradlew.bat build --no-daemon --max-workers=2 --console=plain`,
final frontend coverage/typecheck/lint/format/build and npm audit. Its
`docsPreclosure` row is earlier evidence only. WI007 independently reconciles
coverage counters and all 334 inputs in shared and isolated roots; WI008/WI009
retain compiler exclusions, installation warnings and no-live-install limits.

Later document checkpoint: MA reported `docs-postupdate.log` at 05:17 after
the twelve-doc edit and initial WI013 reports. Docops read its 702 supported
IDs, no broken links, index PASS and final success summary. This later PASS is
distinct from the earlier preclosure 702 result, and still predates the final
two readability corrections/report completion. MA will run the final validator
after WI014; it is not preemptively certified here.

The final backend skips are 18 guarded MySQL cases plus one symbolic-link
environment abort; none is a PASS for its excluded environment. WI006 lists
the exact seven suites. No count is summed across focused/full/earlier runs.
Initial compile failure, second-run multipart fixture failures and incomplete/
pre-F1/pre-R1 frontend checkpoints remain historical, not final acceptance.

## Risks / Rollback

- Production remains HOLD for named host/domain/artifact, exact proxy/origins,
  secret distribution, production frontend serving, ACL/static isolation,
  DB/public/private-root tuple, backup/restore proof, alert owner and one
  scheduler/writer responsibility. Existing accepted TEST/Gmail evidence is not
  reopened wholesale; verify only target-dependent differences under approval.
- Old typeless JWT sessions require login after deployment, without a legacy
  bypass. No secret/token inspection, rotation or session migration occurred.
- Old unbound unfinished monetary commands require separately approved
  admission pause, inventory, drain/reconciliation and recorded disposition
  before deployment or rollback. No mixed old/new writers, rehashing history,
  stripping source suffixes, invented snapshots or automatic replacement charge.
- No real/retained DB access or DDL, media/log repair, Provider charge/refund,
  mail, runtime restart, deployment or Git mutation was performed by docops.
  H2 schema creation belongs to MA's disposable tests, not retained-data DDL.
- Live installed frontend bytes were not updated by the isolated `npm ci`.
  WI005/WI012 disclose the changed hidden lock metadata versus old installed
  packages. This report does not claim byte-identical installed dependencies.
- Not run by docops: Gradle/npm/test/build/audit runners, browser/mobile/live
  OAuth, MySQL concurrency/schema, Windows symbolic-link acceptance, proxy/load,
  OS ACL/process-kill recovery, production backup/restore or deployment checks.
- Rollback only WI013's exact documentation hunks through a scoped patch.
  Preserve SR-93's preceding dirty paragraph, all other agents' edits and all
  historical artifacts. Do not reset the shared tree, delete reports/history
  without approval or roll back product/payment state as a documentation action.

## Follow-ups / Ready MA List

1. WI006-WI009 final packets and WI010-WI012 reviews are received and checked;
   no additional Gradle/npm/product test or broad audit is requested.
2. MA runs the final document validator after WI014 on the stabilized docs/reports:
   `python .agents/skills/validate-docs/scripts/validate_docs.py`, followed by
   scoped diff checks and preservation review. Docops does not claim a current
   final repository-wide validator PASS from either `docs-preclosure.log` or
   the intermediate `docs-postupdate.log` checkpoint.
3. WI013 is ready for MA's WI014 integration and parent REQ disposition after
   those final document/preservation checks. Do not close the parent REQ,
   stage/commit/push or deploy from this report.

## Related Documents

- [WI013 handoff](WI-20260909-ATS-013-handoff.md)
- [Approved REQ](../user/REQ-20260909-ATS-001.md)
- [WI013 user summary](../user/WI-20260909-ATS-013-summary.md)
- [Historical WI018 register](WI-20260908-ATS-018-findings.md)
- [Backend final evidence](WI-20260909-ATS-006-evidence-pack.md)
- [Frontend regression and coverage](WI-20260909-ATS-007-evidence-pack.md)
- [Typecheck evidence](WI-20260909-ATS-008-evidence-pack.md)
- [Lint, format, build and audit](WI-20260909-ATS-009-evidence-pack.md)
- [Authentication final review](WI-20260909-ATS-010-evidence-pack.md)
- [Payment review](WI-20260909-ATS-011-evidence-pack.md)
- [Storage/dependency review](WI-20260909-ATS-012-evidence-pack.md)
- [Payment rollout runbook](../../docs/design/payment-operations-runbook.md#source-bound-command-rollout-2026-09-09)
- [Production gates](../../docs/SR/SR-93.md#remaining-production-gates)
