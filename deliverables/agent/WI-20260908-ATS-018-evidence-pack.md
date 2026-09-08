---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: evidence-pack
status: confirmed
---

# Evidence Pack: WI-20260908-ATS-018

## Summary

Integrated the bounded review of main `8161f0a00b088001a37962da719c3ace8fea44ad`. Six code-confirmed P1 findings prevent a clean release recommendation; the [register](WI-20260908-ATS-018-findings.md) separates P2 closure, maintenance and target-dependent gates. No product correction or production incident reproduction was performed.

## Scope / DoD Check

- [x] Current main baseline, unchanged pinned runtime and pre-existing-artifact preservation boundary identified.
- [x] Independent auth, payment and domain reviews received; MA traced P1 paths and challenged the existing guards.
- [x] Existing safe backend/frontend suites and read-only HTTP checks completed; selected dependency advisories checked against actual versions/use.
- [x] Unified release/maintenance/target register prepared without changing product policy or starting another audit cycle.
- [x] Final document validation, scope/hash checks and reviewer cleanup recorded below.

## Reference Documents (Tier 0-2)

| Tier | Document / input | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` | Approved bounded scope, transaction/evidence rules, current terms |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md` | Sensitive boundaries, severity and verification limits |
| 2 | [REQ004](../user/REQ-20260908-ATS-004.md), [WI018 handoff](WI-20260908-ATS-018-handoff.md) | Scope, central execution ownership and finite chain |
| 2 | [WI015](WI-20260908-ATS-015-evidence-pack.md), [WI016](WI-20260908-ATS-016-evidence-pack.md), [WI017](WI-20260908-ATS-017-evidence-pack.md) | Independent code/document/test-substance evidence |
| 2 | `docs/design/runtime-storage-operations.md`, `payment-operations-runbook.md`, `docs/SR/SR-93.md` | DB/media tuple, payment and target ownership contracts |

Skills: create-req, create-wi-handoff-packet, create-wi-evidence-pack, test, validate-docs. Three delegated reviewers used their scoped handoffs without forked full conversation context. Heavy Gradle/npm test execution was serialized by MA. WI018 is the final dependency consumer; it blocks no further approved WI.

## Fresh Tests

| Verification | Actual result | Boundary |
|---|---|---|
| `gradlew.bat --offline --no-daemon --max-workers=2 --console=plain test --rerun-tasks` | Exit 0; 191 suites, 1,708 total, 1,689 passed, 0 failures/errors, 19 skipped; 2m23s. Fresh XML parsed after completion. | 18 gated MySQL proofs deliberately disabled; 1 LocalStorage symlink case aborted because this Windows environment could not create its link. H2 is not MySQL concurrency proof. |
| `npm --prefix frontend test -- --maxWorkers=2` | Exit 0; 112 files, 1,493 passed; 120.35s. | jsdom tests, not a fresh browser/mobile acceptance run. One non-failing jsdom navigation-not-implemented warning remains. |
| npm audit, all dependencies | Exit 1; 6 affected groups: 5 high, 1 moderate. | Advisory result, not command failure or proven application exploit. |
| npm audit, `--omit=dev` | Exit 0; 0 reported production dependency vulnerabilities. | Point-in-time npm database result, not proof of absence of app/backend vulnerabilities. |

No coverage, build, typecheck, ESLint or Prettier result was freshly produced by this review. Earlier dated successes remain historical; only product-independent audit documents were added/updated. Existing green tests do not cover the newly identified cross-boundary schedules.

### Backend Test Isolation

The child process used `SPRING_PROFILES_ACTIVE=test`, H2 `jdbc:h2:mem:wi018audit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`, driver `org.h2.Driver`, user `sa`, blank password; additional config/import were cleared. Four opt-ins (`ATSTUDIO_MYSQL_PROOF_ENABLED`, `ATSTUDIO_ADMIN_ROLE_MYSQL_PROOF_ENABLED`, `ATSTUDIO_SETTLEMENT_MYSQL_PROOF_ENABLED`, `ATSTUDIO_SUBSCRIPTION_CORRECTION_MYSQL_PROOF_ENABLED`) were false. Acceptance/bootstrap/notification were off; SMTP was localhost port 1 with blank credentials and Toss keys were blank. Public/private storage roots were under this review's `output/security-readiness-20260908/` test directories.

These overrides were scoped to the test-launch process, not the running backend or the user's permanent environment. Hibernate create/drop lines in the test log refer to the isolated H2 test databases, not either existing MySQL database. No new MySQL database or restore/drop operation occurred.

## HTTP And Runtime Evidence

- Before/final checkpoints use public origin `https://final-expression-heading-header.trycloudflare.com`, local frontend 5173 and backend 8080; no authentication token or response body with user data was captured.
- Fresh cases and nuanced outcomes are in the register and `output/security-readiness-20260908/http-probes.json` plus `http-backend-probes.json`. 401 establishes anonymous denial only, not current authenticated owner isolation.
- Runtime ownership remained Cloudflare PID 1888 (21:00:16), frontend PID 16160 (21:00:21), backend PID 24792 (21:55:09), 2026-09-08 KST, with expected frontend path/pinned JAR/tunnel target checks. No restart or process termination was performed.
- Backend artifact: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/ATStudio-20260908-copy-polish.jar`; SHA-256 `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`. Building test classes does not replace this running JAR.
- Actual backend JAR library list was read without extracting secrets; selected official Spring/Tomcat/GitHub advisory references and applicability are in the register. No intrusive exploit, public load, provider charge/refund, real mail, DB/media repair or credential-file read was performed.

## Raw Evidence Pointers

Generated local reports under `output/security-readiness-20260908/`:

- `backend-tests.log`; fresh JUnit XMLs in `build/test-results/test/`.
- `frontend-tests.log`.
- `npm-audit.json`, `npm-audit-production.json`, `npm-dependency-paths.json`.
- `backend-runtime-libraries.json`.
- `http-probes.json`, `http-backend-probes.json`.
- Final `docs-validation.log` and `preservation-check.json` are recorded after closeout checks.

These are local audit outputs, not a published client ZIP or evidence of live financial execution. Do not mix their counts with historical subsets.

## Changes And Final Checks

Allowed tracked change: a dated finding-register pointer in `docs/SR/SR-93.md`. New REQ004 and WI015-018 handoffs/evidence/summaries form the review record. Source, tests, package locks, schema, configuration, branches and 125 pre-existing untracked artifacts are protected. No commit or push is authorized by REQ004.

Final checks: validate-docs exit 0 (Tier 0, internal links, 687 supported traceability IDs, index); `git diff --check` PASS. Fourteen new Markdown audit records passed scoped trailing-whitespace and JWT/Toss-secret-pattern checks; no actual credentials were copied into them. All 125 protected pre-existing untracked files retained identical SHA-256 hashes (zero missing/changed); new paths were confined to the approved audit records and generated output directory. The sole tracked diff is five added SR-93 pointer/status lines; source/tests/config/schema/package locks remain unchanged, HEAD still `8161f0a`, branch main. All three completed reviewers were closed; both needed test sessions ended successfully. Final status edits were revalidated before reporting.

## Risks / Rollback

Review completion does not imply fixes. Six P1 defects and the auth/media confirmation conditions must not be hidden in an optional cleanup backlog. No live customer incident was demonstrated. Production filesystem ACLs, reverse-proxy behavior, complete dependency inventory, infrastructure secrets/access, target restore, disabled OAuth integration and provider-live outcomes remain outside this evidence.

No product-state rollback is necessary. If the user later requests reverting the review, limit it to these new audit documents and the scoped SR-93 pointer; never blanket-clean the pre-existing untracked files or logs.

## Follow-ups

Use the finite corrective sequence in the register. It requires separately scoped implementation, not new features, policy changes, automatic deployment or restarting the accepted TEST flows from scratch. WI015/016/017 results feed this WI; no downstream approved WI remains after its documentation closeout.
