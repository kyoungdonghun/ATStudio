---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved implementation and independent verification chain
  - path: WI-20261011-ATS-066-handoff.md
    reason: Disposable local transaction proof under review
---

# WI072 Independent Transaction Proof Review

> Purpose: Review the bounded local MySQL proof without authorizing any production mutation.

## WI Header

- WI ID: WI-20261011-ATS-072
- REQ: REQ-20261011-ATS-007
- Agent: re
- Depends On: WI066 candidate and fixed diagnostics
- Blocks: MA local proof gate and later first-admin admission

## WI Summary

Why: Distinguish real transaction evidence from synthetic identity adapters and verify process containment.

In: Independently review the current WI066 launcher, Windows job, harness, synthetic loopback init SQL and prepared manifests; run no-network synthetic tests in a WI072-owned directory; inspect existing sanitized proof receipts. Verify exact candidate code executes real MySQL transactions while only three identity reads are adapted. Report concrete defects promptly to MA, then review repaired exact bytes.

Out: No existing database access, MySQL startup, service changes, network, SSH, browser, credentials, cloud, app starts, provider changes or first-admin execution. Do not edit WI066 or shared source.

DoD: An exact-byte local-proof-only ALLOW or actionable HOLD, reproducible tests, preserved failed attempts, distinction between Windows 8.0.45 proof and native 8.0.46 acceptance, and two-set reports. A local ALLOW does not release production.

Constraints: Own only build/WI072-review and this WI's evidence/summary. Do not read private logs, private environment bodies, credentials or SQL containing actual user data. Fresh synthetic SQL in WI066 is permitted. No deletion, overwrite of historical receipts, commits, pushes or subdelegation. MA retains one-use launch authority.

## Acceptance Criteria

Functional:
- [ ] Exact input pins, absent fresh datadir, verified owned PID/port and Windows job kill behavior remain required.
- [ ] No skip-grant-tables, wildcard-host grants or accidental existing-service use.
- [ ] All six cases test real transaction behavior, including concurrent one-winner, replay, rollback and uncertain-commit handling.
- [ ] Fixed diagnostics expose only phase, class, SQLSTATE and numeric vendor code, never raw exception text or secrets.

Performance:
- [ ] Local proof retains finite deadline, loopback-only listener and owned-process cleanup.
- [ ] Review itself starts no database or network process.

Quality:
- [ ] Same candidate bytes before/after review, independent risk-based tests and explicit limits.
- [ ] Read evidence using fixed selected fields; preserve existing source and failed runs.

## Input Pointers

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Context:
- deliverables/user/REQ-20261011-ATS-007.md
- deliverables/agent/WI-20261011-ATS-066-handoff.md
- build/WI066-mysql/launcher.py
- build/WI066-mysql/winjob.py
- build/WI066-mysql/WI066MysqlHarness.java
- build/WI066-mysql/synthetic-loopback-account.sql
- scripts/deployment/RuntimeFeatureProvisioner.java
- build/WI066-mysql/run-0bb61b9851c54dcd8886feb056a3ee35/result.json

## Output Contract

- User-facing: deliverables/user/WI-20261011-ATS-072-summary.md
- Agent-facing: deliverables/agent/WI-20261011-ATS-072-evidence-pack.md
- Review tools and receipts: build/WI072-review/
- Use create-wi-evidence-pack and test skills. State exact command, time, counts, hashes, limitations and next owner.

## Traceability And Rollback

All findings reference precise code and reproducible evidence. No production change exists to roll back. Never delete fixtures as cleanup without separate approval. After a final local-proof verdict, immediately return pins to MA for the one-use gate; later first-admin native approval remains separate.
