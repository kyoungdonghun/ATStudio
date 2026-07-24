# Evidence Pack: WI-20260724-ATS-008

## 1. Work Item

| Field | Value |
|---|---|
| WI | `WI-20260724-ATS-008` |
| Role | `qa-integ` |
| Requirement | `deliverables/user/REQ-20260724-ATS-001.md` |
| Handoff | `deliverables/agent/WI-20260724-ATS-008-handoff.md` |
| Mode | Independent read-only cross-layer review |
| Verdict | **PASS** |
| Severity count | **P0 0 / P1 0 / P2 0** |
| Git state | **MA verification pending** |

## 2. Scope / DoD Check

- [x] Independently inspected the final patches for the three original fixes.
- [x] Independently verified the three WI-007 P2 corrections delivered by
      WI-009.
- [x] Reproduced the focused acceptance, lifecycle, demo, Java, PDF, and
      documentation checks.
- [x] Reconciled WI-004 through WI-006 full evidence with the bounded WI-009
      delta.
- [x] Cross-checked configuration, security policy, current-state docs, and
      unchanged API/DB/frontend contract boundaries.
- [x] Classified all current findings at P0/P1/P2.
- [x] Preserved ignored secrets and external acceptance bundles.
- [x] Left commit, upstream, remote, and push verification to MA as instructed.

## 3. Reference Documents

| Tier | Document | Review use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Transparency, scope, and security boundaries |
| 0 | `docs/standards/development-standards.md` | Test and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable and provenance rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Secret variables and runtime contracts |
| 1 | `docs/policies/quality-gates.md` | Quality and review gate criteria |
| 2 | `docs/design/api-spec.md` | API inventory and V1 boundaries |
| 2 | `docs/design/db-schema.md` | Fresh-only DB and entity boundaries |
| 2 | `docs/ui/atstudio-front-list.md` | Frontend inventory and role boundaries |
| Context | `deliverables/user/REQ-20260724-ATS-001.md` | Approved goal, scope, and quality gates |
| Context | `deliverables/agent/WI-20260724-ATS-007-evidence-pack.md` | Three P2 findings and contradiction record |
| Context | `deliverables/agent/WI-20260724-ATS-009-evidence-pack.md` | Claimed corrections and focused evidence |
| Context | `docs/SR/SR-93.md` | V1 versus production-readiness boundary |

## 4. Independent Verification Matrix

| Target | Independent evidence | Result |
|---|---|---|
| Original fix 1: acceptance obsolete variables | Patch inspection plus 9-check backend-environment contract and 10-check lifecycle dry-run | **PASS** |
| Original fix 2: demo credential path | Direct/wrapper source inspection plus 14-check focused contract | **PASS** |
| Original fix 3: PDF portability | Current manifest/verifier plus isolated exact replay and visual samples | **PASS after WI-009 correction** |
| WI-007 P2-001: false PDF replay provenance | Explicit-path wrapper, pinned dependency file, schema-v3 manifest, exact replay | **CLOSED** |
| WI-007 P2-002: six hidden rate-limit aliases | Active config removal plus Java absence/presence assertions | **CLOSED** |
| WI-007 P2-003: DB password variable mismatch | Local example and security policy both use `SPRING_DATASOURCE_PASSWORD` | **CLOSED** |

### Contradiction Handling

WI-003's original PATH-based replay claim was not accepted as final evidence.
WI-007 correctly showed that plain `python` did not identify the dependency
runtime truthfully. WI-009 replaced that contract with an explicit bootstrap
Python path, an isolated temporary venv, pinned dependency versions, and an
explicit Poppler path. This review executed that replacement contract in a
temporary repository fixture and obtained `REPLAY=PASS`.

## 5. Commands and Exact Results

### Acceptance environment contract

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-backend-environment.ps1
```

Result: exit `0`; status `passed`; 9 checks, including
`obsolete-payment-name-rejection`, environment isolation, restoration, and
temporary-fixture cleanup.

### Acceptance lifecycle dry-run

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-dry-run.ps1
```

Result: exit `0`; status `passed`; 10 checks. Optional PSScriptAnalyzer was
reported as `not-installed`.

### Demo seed contract

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\demo\test-seed-client-demo.ps1
```

Result: exit `0`; status `passed`; 14 checks covering direct and wrapper
dry-runs, live-mode fail-closed behavior, explicit input forwarding,
secret-safe output, syntax, and fixture cleanup.

### Focused Java baseline contract

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest" --rerun-tasks
```

Result: `BUILD SUCCESSFUL in 15s`; 5 actionable tasks executed. Gradle emitted
only the existing unchecked-test compilation note and configuration-cache
suggestion.

### Current PDF verification

```powershell
& "C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" scripts/docs/verify_client_testing_pdf.py
```

Result: exit `0`; `PAGES=12`; `SOURCE_SEGMENTS=295/295 (100.00%)`;
`VERIFY=PASS`; PDF SHA-256
`d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4`.

### Exact documented PDF replay

The current non-secret PDF scripts and seven source documents were copied to a
temporary fixture outside the repository. The documented command was executed
there so the tracked generated artifact was not modified:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/docs/replay-client-testing-pdf.ps1 -PythonExecutable $env:ATSTUDIO_PDF_PYTHON -RenderTool $env:ATSTUDIO_PDF_RENDER_TOOL
```

Result: exit `0`; pinned dependencies installed in a fresh temporary venv;
`PAGES=12`; `SOURCE_SEGMENTS=295/295 (100.00%)`; `VERIFY=PASS`;
`RENDERED_PAGES=12`; `REPLAY=PASS`.

Reproduced hashes:

| Artifact | SHA-256 |
|---|---|
| PDF | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| Manifest | `a48e0b02e6c453a8f72ea16215dbd472592a9527c57e5d1d5524b5c229406f53` |

Poppler emitted non-fatal fallback display-font warnings. It rendered all 12
pages. Independent visual inspection of pages 1, 6, and 12 found no blank
page, clipping, overlap, unreadable Korean text, broken footer, or page-number
defect. The temporary fixture and renders were removed.

### Documentation validation

```powershell
& "C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" .agents/skills/validate-docs/scripts/validate_docs.py
```

Result: exit `0`; all Tier 0 documents present; 0 broken internal links; 455
supported traceability-ID matches; all documents covered by indexes.

### Non-gating full-backend command note

```powershell
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification build --warning-mode all --rerun-tasks
```

The command was launched once during this review, but the unified Windows
runner did not return a usable Gradle stdout/exit record before the task
interruption. It was not repeated and is **not used** as PASS evidence.
Freshness is established from WI-004's complete backend gate plus the
independently rerun WI-009 delta checks above.

## 6. Quality Evidence Freshness

| Evidence slice | Freshness decision |
|---|---|
| WI-004 backend/acceptance full gate | Valid baseline for the original three fixes: 1,208 tests, 0 failures/errors, 9 classified skips; JaCoCo and build passed |
| WI-005 frontend full gate | Still applicable: WI-009 did not change frontend source, configuration, dependencies, or generated frontend contract |
| WI-006 documentation gate | Baseline retained; WI-009's PDF/docs delta was independently replayed, verified, and revalidated in this review |
| WI-009 configuration/test delta | Freshly covered by the rerun Java baseline, acceptance contracts, PDF replay/verifier, and docs validator |

WI-009 introduced no controller, entity, schema, seed, frontend, or product
service change. Therefore a bounded baseline-plus-delta evidence model is
supported; no whole-system result is represented as newer than it is.

## 7. API / Configuration / Documentation Consistency

- API inventory remains the documented 137 mappings across 23 controllers.
  No WI-009 controller or security-route change exists.
- DB inventory remains 39 tables and 39 JPA entities. No schema, seed, entity,
  or retained-data migration change exists.
- Frontend inventory remains 53 distinct visual page UIs. No frontend file or
  dependency changed in WI-009.
- The acceptance launcher rejects the three obsolete payment/billing names,
  while SR-93 and the payment acceptance checklist describe the same
  fail-closed contract.
- `application.yml` retains only canonical availability client-limit names
  with unchanged defaults of 30 requests and 60 seconds.
- `application-local.example.yml` consumes
  `SPRING_DATASOURCE_PASSWORD`, matching `security-policy.md`.
- The PDF manifest's replay command matches `docs/client/index.md`; its
  normalized replay-script and dependency-lock hashes verify successfully.
- SR-93 remains OPEN for production readiness and does not convert local V1
  quality evidence into a production-readiness claim.

## 8. Finding Register

| Severity | Count | Current findings |
|---|---:|---|
| P0 | 0 | None |
| P1 | 0 | None |
| P2 | 0 | None; all three WI-007 P2 findings are closed |

Non-blocking observations:

- PSScriptAnalyzer was unavailable for the lifecycle dry-run.
- Poppler emitted fallback display-font warnings but rendered and verified all
  pages.
- The fresh-MySQL and symbolic-link environment-dependent boundaries from
  WI-004 were not reopened.

## 9. Git and Publication Readiness

**MA verification pending.**

Per the user's latest instruction, this review does not determine final branch,
staging, commit contents, upstream, remote-branch existence, or push readiness.
No commit, push, fetch, branch deletion, or remote mutation was performed.

WI-008's code/config/docs/test verdict is PASS. MA must complete the Git
verification before claiming G6, a reproducible remote baseline, or official
publication.

## 10. Unverified Scope

- Final Git status, staged set, commit, upstream, remote branch, and push.
- Ignored local secret files and external acceptance bundles.
- Live client acceptance and Cloudflare/public URL state.
- Fresh empty-MySQL initialization and live Hibernate validation.
- Live Toss charge, renewal, refund, reconciliation, and billing-key cleanup.
- Production HTTPS/proxy/CORS, secret store, backup/restore, monitoring,
  scheduler ownership, incident response, and final release approval.

These are explicit environment, MA-owned, acceptance, or production gates.
They are not hidden P0/P1/P2 findings in the WI-008 review scope.

## 11. Rollback / Readiness

This review changed no runtime source, configuration, current-state
documentation, generated PDF artifact, DB schema, branch, or remote.

Rollback consists only of removing:

- `deliverables/user/WI-20260724-ATS-008-summary.md`
- `deliverables/agent/WI-20260724-ATS-008-evidence-pack.md`

Final decision: **PASS, with Git publication readiness pending MA
verification.**
