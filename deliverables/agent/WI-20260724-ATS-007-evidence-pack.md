# WI-20260724-ATS-007 Evidence Pack

## 1. Work Item

| Field | Value |
|---|---|
| WI | `WI-20260724-ATS-007` |
| Role | `cr` |
| Requirement | `deliverables/user/REQ-20260724-ATS-001.md` |
| Handoff | `deliverables/agent/WI-20260724-ATS-007-handoff.md` |
| Mode | Read-only review; only this evidence pack and the user summary were created |
| Verdict | **PASS** |
| Severity count | `P0 0 / P1 0 / P2 3 / P3 0` |

## 2. Finding Register

### ATS007-P2-001: Non-reproducible PDF command provenance

**Severity:** P2
**Class:** Current tooling/documentation defect
**Pointers:**

- `scripts/docs/generate_client_testing_pdf.py:63-64`
- `scripts/docs/generate_client_testing_pdf.py:442-460`
- `docs/client/index.md:38`
- `output/pdf/atstudio-client-testing-guide.manifest.json:12-13`

**Evidence:** `portable_command_name()` uses `Path.stem`, and the manifest
records `python scripts/docs/generate_client_testing_pdf.py --render-tool
pdftoppm`. A direct import probe with plain `python` failed on missing
`reportlab`; generation/verification evidence used the bundled workspace
Python. The generated PDF content and hash verification still pass.

**Impact:** A new operator cannot reproduce the recorded artifact by following
the committed command. This is false replay provenance, not a product runtime
or customer-data failure.

**Correction:** preserve a truthful non-secret runtime identity and test the
recorded replay path in a fresh process. Avoid `Path.stem` for executable
identity.

**Verification after correction:** execute the exact recorded command in a
fresh shell, run `scripts/docs/verify_client_testing_pdf.py`, and compare the
manifest/PDF hashes.

### ATS007-P2-002: Active hidden aliases for availability rate limits

**Severity:** P2
**Class:** Dead compatibility / configuration ambiguity
**Pointers:**

- `src/main/resources/application.yml:155-156`
- `src/main/resources/application.yml:162-163`
- `src/main/resources/application.yml:169-170`

**Evidence:** Six old `APP_SECURITY_RATE_LIMIT_*_AVAILABILITY_LIMIT` and
`*_WINDOW_SECONDS` names remain nested beneath the canonical
`*_CLIENT_LIMIT` and `*_CLIENT_WINDOW_SECONDS` properties. Repository searches
found no active consumer or current documentation for the old names.

**Impact:** Stale deployment values can silently affect the V1 client-rate-limit
contract and preserve a compatibility path that the consolidation requirement
intends to remove.

**Correction:** remove the six nested aliases and extend
`V1BackendBaselineContractTest` with explicit absence checks.

**Verification after correction:** run the baseline contract test and the
backend environment script; search the active tree for all six removed names.

### ATS007-P2-003: Local DB password environment-name mismatch

**Severity:** P2
**Class:** Cross-document/config contract mismatch
**Pointers:**

- `application-local.example.yml:14`
- `docs/policies/security-policy.md:82-94`
- `docs/policies/security-policy.md:172`

**Evidence:** The example consumes `DB_PASSWORD`; the security policy defines
`SPRING_DATASOURCE_PASSWORD` as the canonical database password variable and
states that the local example is the developer bootstrap contract.

**Impact:** Local bootstrap can fail or create an undocumented second
configuration dialect.

**Correction:** use `SPRING_DATASOURCE_PASSWORD` in the example unless a
separate local-only alias is explicitly approved and documented.

**Verification after correction:** bootstrap with canonical environment names
only and run the acceptance backend environment contract.

## 3. Negative and Consistency Evidence

| Check | Result | Evidence |
|---|---|---|
| P0/P1 review | PASS | No blocking security, integrity, runtime-path, API, or DB defect identified |
| Backend environment contract | PASS | `scripts/acceptance/test-backend-environment.ps1`, 9 checks |
| Acceptance dry run | PASS | `scripts/acceptance/test-dry-run.ps1`, 10 checks; PSScriptAnalyzer unavailable |
| Demo seed contract | PASS | `scripts/demo/test-seed-client-demo.ps1`, 14 checks |
| V1 backend baseline contract | PASS | `V1BackendBaselineContractTest`, Gradle build successful |
| Documentation validation | PASS | Tier 0 valid, 0 broken links, 454 traceability IDs indexed |
| Client PDF verification | PASS | 12 pages, 295/295 checks, SHA-256 `d7ad6184...e2332f4` |
| API inventory | MATCH | 23 controllers; 65 GET, 36 POST, 21 PUT, 15 DELETE; total 137 |
| UI inventory | MATCH | 56 path entries plus one index route; 53 lazy page modules |
| DB source inventory | MATCH | 39 unique schema tables and 39 JPA entities |
| Tracked secret scan | PASS | No production JWT/Toss secret, URL credential, or inline MySQL password found |
| Retired payment/runtime names | PASS with exclusions | Only rejection tests and historical/current-state explanations remain |
| Tracked backup sources | PASS | No active `.bak`, `.old`, `.orig`, or duplicate backup source found |

## 4. Historical, Conditional, Deferred, and Backlog Classification

### Historical evidence

The full-system audit, archived remaining-remediation design, archived
deliverable logs, and WI-004 through WI-006 packs were used only to establish
provenance and prior validation. Their old snapshots were not treated as current
runtime state.

### Environment-conditional

- Ignored `application-local.yml` was intentionally not opened.
- The external acceptance secret bundle was not available.
- Trusted proxy deployment evidence and JWT rotation remain conditional at
  `docs/policies/security-policy.md:174-176`.
- Windows system-font paths at
  `output/pdf/atstudio-client-testing-guide.manifest.json:39-44` are portable
  overrides within the generator contract, not personal machine paths.

### Deferred policy

- Retained-data migration is explicitly outside the fresh-only V1 contract:
  `docs/policies/security-policy.md:215`.
- Live Toss, production topology, backup/restore, monitoring, client acceptance,
  and release approval remain open at `docs/SR/SR-93.md:40-46`.

### Unrelated backlog

No unrelated backlog item was promoted into a WI-007 defect without a current,
reproducible code or contract violation.

## 5. Git and Artifact State

The audit observed branch `codex/p1-acceptance-hardening` at
`4b00e99f2293e290d92b1fc56412a90743588c80` with no configured upstream and no
matching official branch on the inspected remote. The working tree contained
the active WI-001 through WI-006 deliverables/scripts/docs and an untracked
client screenshot ZIP. Local preservation tags existed; remote publication and
old remote branch cleanup were not performed.

The final Git-state decision is delegated to MA. An orphan
`git status --porcelain` process was stopped, and WI-007 did not wait for or
re-run status.

## 6. Unverified Scope and Confidence Boundary

- No fresh empty-MySQL initialization or `ddl-auto=validate` live boot was
  rerun.
- Full backend and frontend suites were not rerun; WI-004 through WI-006 results
  were accepted and supplemented by targeted checks.
- Live Cloudflare, production network, secret store, provider account, and
  retained production data were not inspected.
- The ignored local secret file and external acceptance bundle were not read.
- Git status after this evidence-pack creation is intentionally left to MA.

These boundaries do not hide a known P0/P1 finding. They prevent this WI from
claiming production readiness or final remote-baseline closure.

## 7. Required Corrective Work

1. Correct PDF runtime/replay provenance and regenerate/verify the artifact.
2. Remove six obsolete availability-rate-limit aliases and add absence tests.
3. Align `application-local.example.yml` with the canonical database password
   environment variable.
4. Run WI-008 as an independent review of the corrections and MA-owned Git
   state before remote baseline declaration.

## 8. Rollback

This WI did not edit runtime source, configuration, DB schema, or scripts.
Rollback consists only of removing:

- `deliverables/user/WI-20260724-ATS-007-summary.md`
- `deliverables/agent/WI-20260724-ATS-007-evidence-pack.md`

## 9. Final Gate Decision

**PASS: P0 = 0 and P1 = 0.**

The three P2 findings are explicit, bounded, and have reproducible correction
steps. They do not convert this WI to FAIL under the handoff rule, but they
should be closed before the final remote baseline is presented as complete.
