[WI HEADER]
WI ID: WI-20260809-ATS-031
REQ: REQ-20260809-ATS-001
Session ID: none
Agent: cr
Depends On: WI-20260809-ATS-030
Blocks: WI-20260809-ATS-032+
Baseline: codex/v1-release-rehearsal-fixes @ e343c2085fbc82c66b44fb8e5edde35bf920980f

[WI SUMMARY]

Why:

- Consolidate all independent and owner-referenced findings from WI-021 through WI-030 into one root-cause register before any product correction begins.
- Prevent the same shared defect from being fixed repeatedly under page-specific symptoms, and prevent policy, external-provider, or evidence-only gaps from being silently implemented as product behavior.
- Produce a bounded remediation sequence with explicit approval gates, affected surfaces, required regression evidence, and documentation impact.

Scope (in):

- Read and reconcile every finding, control, evidence boundary, blocked lane, and policy question from WI-021 through WI-030.
- Trace each symptom to one canonical root cause where the supplied evidence supports that relationship.
- Preserve separate causes when UI/DOM, frontend invocation, server response, durable state, provider, or policy evidence differs materially.
- Classify every source item as one of:
  - `FIX-NOW`: clear implementation defect inside the approved product contract.
  - `POLICY-GATE`: behavior cannot be selected without user approval.
  - `SECURITY-GATE`: security/privacy/access-control decision or review is required.
  - `EXTERNAL/BLOCKED`: provider, browser capability, real device, file delivery, or other unavailable evidence.
  - `TEST-GAP`: missing automated proof without an independently established product defect.
  - `DOC-GAP`: current documentation is incomplete or inconsistent with the implementation/evidence.
  - `CONTROL`: confirmed non-defect or already-safe behavior.
- Reassess severity only where cross-WI evidence justifies a change; record the original IDs and reason for any change.
- Identify shared roots whose correction affects multiple routes, roles, API wrappers, services, DTOs, stores, or documents.
- Build a remediation dependency graph and propose sequential WI-032+ slices with disjoint primary write scopes.
- For each proposed correction WI, define focused tests, adjacent-regression tests, full quality gates, browser/runtime evidence, rollback, and documentation follow-up.
- Extract a concise user decision register. Do not answer any decision on the user's behalf.
- Identify any P0/P1 item that blocks correction work or requires immediate escalation.

Scope (out):

- Product/runtime code, tests, fixtures, configuration, schema, seed data, current product documentation, database, browser, Provider, storage, or mail changes.
- Re-running broad source audits, automated suites, browser flows, API probes, DB queries, or external-provider actions.
- Creating a new feature, deleting an existing feature, changing product policy, selecting architecture/dependencies, or resolving security questions.
- Fixing findings, creating SR documents, updating current-state design documents, committing, pushing, merging, deleting branches, or deploying.
- Treating a passing test as proof that a reported missing assertion or runtime lane is resolved.
- Treating one current-width browser observation as proof for blocked viewports or authenticated/server/durable lanes.

DoD:

- [ ] Every defect/control/policy question from WI-021 through WI-030 appears exactly once in the source-to-root crosswalk.
- [ ] Every canonical root lists all original finding IDs, affected entry points, roles, layers, and evidence lanes.
- [ ] Duplicate symptoms are merged only with explicit shared-source/shared-contract evidence.
- [ ] Distinct UI, invocation, HTTP/server, durable, Provider, and policy causes remain separate.
- [ ] Every canonical root has one disposition, severity, confidence, remediation boundary, and verification boundary.
- [ ] Every severity/disposition change records the original value and evidence-based reason.
- [ ] `FIX-NOW` contains no unresolved policy/security/architecture choice.
- [ ] Policy, security, external, test-only, and document-only items are separately enumerated.
- [ ] A proposed WI-032+ dependency graph covers every `FIX-NOW` root without overlapping primary write ownership.
- [ ] Each proposed remediation WI names focused, adjacent, full-regression, browser/runtime, and documentation checks.
- [ ] P0/P1 blockers and user decisions are surfaced before correction begins.
- [ ] Findings consolidation, Evidence Pack, and user summary are written to the exact output paths below; no other file is changed.
- [ ] Output Prettier, applicable docs validation, and `git diff --check` are completed by main after final outputs exist.

Constraints / Forbidden:

- Write only:
  - `deliverables/agent/WI-20260809-ATS-031-handoff.md`
  - `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
  - `deliverables/agent/WI-20260809-ATS-031-evidence-pack.md`
  - `deliverables/user/WI-20260809-ATS-031-summary.md`
- Product/runtime/DB/schema/config/test/fixture/current-doc state is frozen.
- Use the supplied WI evidence as primary evidence. Read product source only when an exact pointer is necessary to distinguish two conflicting roots; do not reopen a general audit.
- Do not execute product tests, builds, browser sessions, API requests, DB queries, or external operations in this WI.
- Never open, read, hash, metadata-probe, move, replace, delete, stage, or use `output/client-demo-screenshots-20260716-140514.zip` as evidence or a fixture.
- Never inspect ignored secrets, `application-local.yml`, `.env`, credentials, tokens, keys, cookies, sessions, or environment secret values.
- No Git mutation. Read-only branch/HEAD/status evidence is permitted, and `git status --short` may reveal the intentional ZIP path without inspecting it.
- Do not infer that a missing UI consumer means a backend endpoint is dead; classify insufficient evidence explicitly.
- Do not turn a general industry convention into AT.M policy when project documents or user decisions are absent.
- Stop and escalate immediately for a credible P0, data-loss path, secret disclosure, unauthorized access, or correction plan that inherently requires schema/destructive/provider/architecture change.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] One canonical root-cause register covers all WI-021 through WI-030 findings and controls.
- [ ] One complete source-ID crosswalk demonstrates no omission and no duplicate issuance.
- [ ] Shared roots identify all affected routes, components, APIs, services, roles, and documents named by the evidence.
- [ ] User decisions are phrased as concrete mutually exclusive policy questions with impact, not vague follow-ups.
- [ ] Proposed correction WIs are small enough for one sequential Subagent and large enough to eliminate one coherent root.
- [ ] Proposed order handles P1/security/shared infrastructure before page-local P2 presentation, while respecting dependencies.
- [ ] No proposed implementation silently adds/removes product capability or invokes an external side effect.

Performance:

- [ ] Do not invent latency, batch, size, retry, memory, or accessibility thresholds absent a current contract.
- [ ] Any unbounded collection/request concern remains a policy or bounded-remediation item with the exact existing evidence.

Quality:

- [ ] All original IDs are machine-searchable in the consolidated crosswalk.
- [ ] Counts reconcile by source WI, canonical root, severity, and disposition.
- [ ] Ambiguous merges remain separate with confidence and missing evidence recorded.
- [ ] No finding is marked resolved merely because existing tests passed.
- [ ] The correction plan includes focused, adjacent, and full-regression verification plus document synchronization.
- [ ] Final output documents pass Prettier and docs validation; tracked diff passes `git diff --check` with the untracked-output boundary stated.

[INPUT POINTERS]

Tier 0 (Constitution - required):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Policies - required/inferred for `cr`, security, access, and quality):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`
- `docs/architecture/system-design.md`

Tier 2 (Frontend and review context):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/standards/evidence-pack-standard.md`
- `docs/design/api-spec.md`
- `docs/design/db-schema.md`
- `docs/ui/screen-flow.md`

REQ / Matrix:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-019-inventory.md`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`

Owning audit evidence, in order:

- `deliverables/agent/WI-20260809-ATS-021-findings.md`
- `deliverables/agent/WI-20260809-ATS-021-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-022-findings.md`
- `deliverables/agent/WI-20260809-ATS-022-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-023-findings.md`
- `deliverables/agent/WI-20260809-ATS-023-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-024-findings.md`
- `deliverables/agent/WI-20260809-ATS-024-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-025-findings.md`
- `deliverables/agent/WI-20260809-ATS-025-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-026-findings.md`
- `deliverables/agent/WI-20260809-ATS-026-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-027-findings.md`
- `deliverables/agent/WI-20260809-ATS-027-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-028-findings.md`
- `deliverables/agent/WI-20260809-ATS-028-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-029-findings.md`
- `deliverables/agent/WI-20260809-ATS-029-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-030-findings.md`
- `deliverables/agent/WI-20260809-ATS-030-evidence-pack.md`

User-facing audit summaries:

- `deliverables/user/WI-20260809-ATS-021-summary.md` through `deliverables/user/WI-20260809-ATS-030-summary.md`

Repro / evidence commands:

- Use `rg` over the named finding/evidence files to enumerate every finding/control ID and disposition.
- Use deterministic count tables in the consolidated output; do not rely on prose-only totals.
- Main will run final document Prettier, docs validation, and `git diff --check`; the review agent must not invent those results.

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-031-summary.md`:

- Korean summary of canonical root counts, P0/P1 blockers, policy/security decisions, proposed correction order, blocked evidence, and the exact point at which user input is required.

Agent-facing findings -> `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`:

- Complete source-ID inventory and omission check.
- Canonical root-cause register with severity, disposition, confidence, affected layers/entry points, evidence lanes, and original IDs.
- Duplicate/merge rationale and deliberately separate causes.
- Policy/security/external/test/document/control registers.
- Proposed WI-032+ dependency graph, primary write scopes, regression scopes, and approval gates.

Agent-facing Evidence Pack -> `deliverables/agent/WI-20260809-ATS-031-evidence-pack.md`:

- Evidence pointers, count reconciliation, reproducible enumeration commands, risks, rollback, and next-WI handoff requirements.

Handoff Packet -> `deliverables/agent/WI-20260809-ATS-031-handoff.md`:

- This packet.

[TRACEABILITY REQUIREMENTS]

- Evidence pointers: required for every canonical root and every merge/split decision.
- Original IDs: required and unique in the source-to-root crosswalk.
- Tests: no product test execution in this WI; cite prior exact results and preserve their limits.
- Browser/server/durable evidence: cite prior lanes only; never promote blocked or unexecuted evidence.
- Rollback: document-only removal of WI-031 outputs; no product/data rollback should be required.
- Follow-up: every `FIX-NOW` root must map to one proposed correction WI; every gate must name its approving party and blocking impact.
