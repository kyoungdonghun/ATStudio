# Evidence Pack: WI-20260716-ATS-004

## Summary (one-liner)

- Defined the remaining P2/X/P3 remediation ownership, product invariants, environment holds, and verification contracts for REQ-20260716-ATS-002.

## Scope / DoD Check

- [x] Mapped ATS020-P2-01~18, X-01~03, and P3-01~02 to WI-005~017 or an explicit environment condition.
- [x] Distinguished implementation work from environment-conditional evidence for retained DB, trusted proxy, and JWT fallback concerns.
- [x] Defined boundaries for authentication/authorization, payments, playlists/exports, business certification, album/playback, frontend state, documentation, and tooling.
- [x] Preserved the public full-listening, subscription-download, card recurring-payment, and single-server invariants.
- [x] Included bounded batch, pagination, query-aligned index, EXPLAIN, and retained-DB rehearsal requirements.
- [x] Defined downstream verification ownership through WI-005~017 and G2~G7.
- [x] Registered the design in the active design index; the row was already present when this follow-up began, so no index edit was required.
- [x] Validated Tier 0 documents, internal links, traceability IDs, and document indexing with the current bundled validator.
- [x] Ran the required whitespace check successfully.

## Reference Documents (Tier 0-2)

**Injected Context declared by the WI handoff packet:**

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution required for all agents |
| 0 | `docs/standards/development-standards.md` | Development standards required for the `sa` assignee |
| 1 | `docs/policies/security-policy.md` | Security boundaries for auth, JWT, payment, and sensitive data |
| 1 | `docs/policies/quality-gates.md` | Required quality and verification gates |
| 1 | `docs/policies/versioning-policy.md` | Versioning and active/superseded document handling |
| 2 | `docs/design/index.md` | Active design registry and entry point |
| 2 | `docs/design/payment-integration-design.md` | Recurring-card and billing-key contract |
| 2 | `docs/design/payment-operations-runbook.md` | Payment operations and incident boundaries |
| 2 | `docs/design/db-schema.md` | Database contract and index context |
| 2 | `docs/design/api-spec.md` | API contract context |
| 2 | `docs/ui/index.md` | Frontend/UI documentation entry point |

**Injection Rules Applied:**

- Rule source: WI-20260716-ATS-004 handoff `INPUT POINTERS` and repository context-injection policy.
- Assignee: `sa`.
- Task type: remediation design and cross-layer verification planning.
- Required tiers: Tier 0, Tier 1, and task-specific Tier 2 pointers.
- This follow-up transcribes the handoff reference table; it does not claim a new semantic review of every referenced document.

## Evidence Pointers

### Changed Files

- `docs/design/remaining-remediation-design-20260716.md`: complete item-to-WI map, invariants, state boundaries, approval boundaries, and verification matrix.
- `deliverables/user/WI-20260716-ATS-004-summary.md`: user-facing current-state decision, ownership matrix, risks, approval boundaries, and validation results.
- `deliverables/agent/WI-20260716-ATS-004-evidence-pack.md`: reproducible, skill-standard evidence for WI-004.

`docs/design/index.md` already contained the active entry for the new design when this follow-up began; it was inspected but not modified by this follow-up.

### Key Locations

- `docs/design/remaining-remediation-design-20260716.md`: `Traceability Map` maps every P2-01~18, X-01~03, and P3-01~02 item.
- `docs/design/remaining-remediation-design-20260716.md`: `Invariants` preserves the four approved product and deployment contracts.
- `docs/design/remaining-remediation-design-20260716.md`: `Verification Matrix` assigns proof to WI-005~017 and environment evidence.
- `deliverables/user/WI-20260716-ATS-004-summary.md`: `Ownership Matrix` gives the concise downstream assignment.
- `docs/design/index.md`: `ATStudio Domain Design` contains the active design link.

## Commands & Outputs

| Command | Result |
|---|---|
| `python .claude/scripts/validate_docs.py` | FAIL: exit code 1; handoff-recorded legacy path is absent (`[Errno 2] No such file or directory`) |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: exit code 0; all Tier 0 documents, internal links, 395 traceability IDs, and index coverage passed |
| `git diff --check` | PASS: exit code 0; no whitespace errors; existing LF-to-CRLF warning for `docs/design/index.md` |

## Tests

- Documentation-only WI; no application test suite was required or run.
- `python .claude/scripts/validate_docs.py`: FAIL because the handoff-recorded path is absent.
- Current bundled documentation validator: PASS with all checks successful.
- `git diff --check`: PASS with no whitespace errors.
- No implementation, retained database, live provider, proxy, secret, or deployment claim is made.

## Reproducibility

From the repository root:

```text
python .claude/scripts/validate_docs.py
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
```

The first command reproduces the stale-path error. The second is the current `validate-docs` skill entry point and exits 0 with all checks passing. The third exits 0 with no whitespace errors.

## Risks / Rollback

### Risks

- X-01~03 remain `ENVIRONMENT-CONDITIONAL`; source or document inspection alone cannot close retained-DB, trusted-proxy, or JWT fallback evidence gaps.
- The handoff source text contains encoding damage in descriptive Korean text. Stable IDs, paths, ownership tables, and readable constraints were used; corrupted wording was not reconstructed as fact.
- The handoff records a stale validator path. Future handoffs should point to `.agents/skills/validate-docs/scripts/validate_docs.py`; the current validator itself passes.
- Existing changes in `docs/design/index.md` belong to the surrounding worktree and must not be reverted as part of this WI.

### Rollback

- Revert only `docs/design/remaining-remediation-design-20260716.md`, `deliverables/user/WI-20260716-ATS-004-summary.md`, and `deliverables/agent/WI-20260716-ATS-004-evidence-pack.md` if WI-004 documentation must be withdrawn.
- Do not alter historical audit evidence, code, schemas, retained data, secrets, branches, deployment configuration, or unrelated worktree changes.
- If the active index entry is later removed, handle that as an explicit index update; this follow-up did not create or modify that row.

## Follow-ups

- WI-005~011: implement the assigned security, payment, data-state, certification, OAuth/player, frontend-state, tooling, and coverage work.
- WI-012: align design, API, DB, UI, operations, traceability, deprecation, and provenance documents.
- WI-013~017: execute backend/frontend regression, 3-way integration review, security review, and release-readiness verification.
- X-01~03: retain environment holds until named environment evidence is attached.
