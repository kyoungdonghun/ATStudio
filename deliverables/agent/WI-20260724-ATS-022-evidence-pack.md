---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20260724-ATS-022
related_req: REQ-20260724-ATS-002
---

# Evidence Pack: WI-20260724-ATS-022

## Change Summary

- Replaced three stale current-state zero-advisory claims with the exact
  dependency status and reachability boundary established by
  WI-20260724-ATS-021.
- Kept package vulnerability status separate from current ATStudio call-path
  reachability.
- Preserved dated historical WI and Evidence Pack text.
- Made no dependency, lockfile, product-code, runtime, or database change.

## Inputs Read

- `docs/standards/core-principles.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`
- `docs/policies/security-policy.md`
- `docs/policies/quality-gates.md`
- `docs/standards/evidence-pack-standard.md`
- `deliverables/user/REQ-20260724-ATS-002.md`
- `deliverables/user/WI-20260724-ATS-021-summary.md`
- `deliverables/agent/WI-20260724-ATS-021-evidence-pack.md`
- `docs/SR/SR-42.md`
- `docs/SR/SR-93.md`
- `docs/design/remaining-remediation-design-20260716.md`

## WI-021 Source Evidence

The corrected documents preserve the following WI-021 conclusions:

- Locked graph: `react-router-dom@6.30.4` and `react-router@6.30.4`.
- Audit result: two moderate vulnerable package records containing three
  advisory records.
- Open redirect/XSS:
  - `GHSA-wrjc-x8rr-h8h6` / `CVE-2026-53669`
  - `GHSA-jjmj-jmhj-qwj2` / `CVE-2026-53668`
  - No exploitable current ATStudio call site was found.
- SSR hydration constructor injection:
  - `GHSA-337j-9hxr-rhxg` / `CVE-2026-53666`
  - Not reachable in the current client-only architecture.
- Disposition: low current application exploitability for the first advisory
  family, no current runtime applicability for the second family, and moderate
  residual dependency risk while the vulnerable packages remain installed.
- Production boundary: approve and complete a controlled migration to
  `react-router-dom@7.18.1` and its matching `react-router` dependency before
  production. Do not use `npm audit fix --force`.

Primary evidence:

- `deliverables/user/WI-20260724-ATS-021-summary.md`
- `deliverables/agent/WI-20260724-ATS-021-evidence-pack.md`

## Pointers

Changed current-state claims:

- `docs/SR/SR-42.md:7`
  - Public sharing now records the vulnerable package status, current
    non-reachability assessment, non-production condition, and production
    migration gate.
- `docs/SR/SR-93.md:33`
  - The V1 baseline dependency row now distinguishes audit status from
    application reachability and keeps production readiness open.
- `docs/design/remaining-remediation-design-20260716.md:48`
  - The former WI-011 zero-finding result is explicitly retained as a dated
    historical checkpoint and superseded for current-state use.
- `deliverables/user/WI-20260724-ATS-022-summary.md`
- `deliverables/agent/WI-20260724-ATS-022-evidence-pack.md`

No historical WI or prior Evidence Pack was modified.

## Reproduction And Verification

Document integrity:

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
```

Result:

- Tier 0 documents: PASS
- Internal links: PASS, zero broken links
- Traceability: PASS, 469 supported-format IDs
- Document index: PASS
- Exit code: 0

Stale current-state claim search:

```powershell
rg -n -i "audits? report 0|npm audit.{0,80}report 0|no development advisory residual|current.{0,80}zero.{0,40}advis" docs/SR docs/design --glob "*.md"
```

Result:

- Zero matches
- `rg` exit code 1, which is the expected no-match result

Whitespace and patch integrity:

```powershell
git diff --check -- docs/SR/SR-42.md docs/SR/SR-93.md docs/design/remaining-remediation-design-20260716.md
git diff --no-index --check -- /dev/null deliverables/user/WI-20260724-ATS-022-summary.md
git diff --no-index --check -- /dev/null deliverables/agent/WI-20260724-ATS-022-evidence-pack.md
```

Result:

- Tracked document check: PASS, exit code 0
- Each untracked deliverable emitted no whitespace error. `--no-index` returned
  exit code 1 because each new file differs from the empty input, which is the
  expected content-difference result.

Scope verification:

```powershell
git status --short
git diff --name-only
```

Result:

- WI-022 changed only the three authorized current-state documents and the two
  required WI-022 deliverables.
- Other pre-existing working-tree changes were not modified as part of WI-022.

## Acceptance Criteria

- [x] All three stale zero-advisory current-state claims are corrected.
- [x] Current call-path reachability is distinguished from package
      vulnerability status.
- [x] Public acceptance remains conditional and production readiness remains
      open.
- [x] Locked versions, advisory count/severity, reachability conclusions, and
      the separately approved React Router 7 migration gate are recorded.
- [x] Internal links and traceability validate.
- [x] Stale-claim search returns zero matches.
- [x] `git diff --check` passes.

## Risk And Rollback

- Documentation-only risk: a reader could otherwise mistake current
  non-reachability for dependency remediation. The corrected wording states
  both facts independently.
- Rollback by reverting only:
  - `docs/SR/SR-42.md`
  - `docs/SR/SR-93.md`
  - `docs/design/remaining-remediation-design-20260716.md`
- The two WI-022 deliverables may be removed if the WI is superseded.
