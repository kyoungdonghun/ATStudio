---
version: 1.1
last_updated: 2026-01-06
project: system
owner: EO
category: guide
status: stable
related_work_item: WI-20251230-001
related_decision: ADR-20251230-001-reuse-first-registry-traceability
dependencies:
  - path: ../standards/core-principles.md
    reason: Highest priority value judgment criteria for operations
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
  - path: traceability.md
    reason: Traceability system reference
---

# Operation Process

> Purpose: Secure system traceability and maintainability through reuse-first principle and change management process.

---

## 1. Overview

Aim to prevent duplication and drift from inability to reuse sources, and maintain system stability by clearly understanding impact during changes.

## 2. Reuse-first Principle

- **Default**: Before "creating new", **must review in order of search → reuse/extend → promote**.
- **Optimization Direction**: Prioritize long-term operations (traceability/testing/versioning) over short-term convenience (copy-paste).

### 2.1 Reuse Ladder (Promotion Ladder)

Specify where work results belong among below, and promote to higher level when needed.

1. **Local**: Temporary logic within one file/one function scope
2. **Module**: Intra-project reuse module (e.g., `scripts/*/utils` or `project/utils`)
3. **Package**: Library with version (confirmed reuse unit)
4. **Platform Tool**: Execution unit also used externally (includes contract/permissions/failure modes)
5. **Global Asset**: Common asset across multiple projects (EO approval + stabilization)

**Promotion Triggers (Recommended)**:
- Same pattern appears 2+ times (duplication occurred)
- Included in MEDIUM/HIGH criticality flow (quality/safety needed)
- Policy/contract/tool requiring "traceability" (operational observation target)

## 3. Work Management — Agent Planning and Tracking

When user request (REQ) is received, **MA** analyzes it, breaks it down into executable task units (WI), and manages. (Standardize with Skills if needed)

### 3.1 Request Analysis and Task Breakdown

- **Ambiguity Assessment**: Proceed directly to exploration if request file/feature/action clear. If ambiguous, first confirm intent with 1-2 questions, and write REQ if needed.
- **Task Breakdown (Complex Cases)**: If scope large after exploration, break down into WI and delegate to Subagents.
  - **WI Unit**: Minimum execution unit completable as one task.
  - **Dependency Definition**: Clearly define preceding/succeeding relationships (DAG) between each WI.
- **Workboard Registration (Recommended)**: Register broken-down WI to WI section of [workboard.md](../registry/workboard.md) to visualize plan.

### 3.2 Task Orchestration and Status Management

Agents control task order and parallel processing through workboard.

- **Status Update**: Update workboard status (Status) in real-time according to work progress stage.
  - `Ready` → `In Progress` → `In Review` → `Done`
- **Parallel Processing (CTX)**: Assign different `Context ID (CTX-...)` to tasks without dependencies for parallel execution.
- **Trigger Detection**: When preceding task completes (Done/Merged), immediately transition follow-up task to `Ready` status and start.

### 3.3 User Reporting and Approval (Milestone Gates)

Agents work autonomously but must obtain user approval at **critical milestones** where direction is decided. This is core device for non-developer users to control project progress direction.

| Milestone | When | Report Content | Why Approval |
| :--- | :--- | :--- | :--- |
| **M1. Plan Freeze** | After requirements analysis and WI breakdown complete | Full task list (Workboard), expected cost/duration | Verify "understood my intent correctly?" |
| **M2. Design Freeze** | After architecture/design/tech stack decision | Design document (Design Doc), decision justification (ADR) | Verify "okay to build this way?" |
| **M3. Impl Ready** | After implementation and test pass, before deploy/merge | Implementation result summary, test results, screenshots | Verify "satisfied with result?" |

- **Approval Procedure**: Agent temporarily pauses work at this point and outputs summary report to user. Only proceed to next step after receiving user's `Y/N` or `feedback`.
- **Approval Channel**: Operate based on Console Input (HITL). (External channels like mobile/Discord managed separately per project)
- **Execution Gate**: **Business logic approval** procedure operating separately from tool execution approval (HITL) in [execution-policy.md](../policies/execution-policy.md).

## 4. Change Management — Execution/Verification/Cleanup

### 4.1 Before Starting Work (Required)

- **All Criticality**: **Issue Work Item ID** (e.g., `WI-YYYYMMDD-001`) — 10 seconds sufficient
- **Context Discovery (Required)**: Explore standards, policies, ADR, similar WI related to work.
  - Method: Refer to section 5 of [agent-docs-map.md](agent-docs-map.md) (dynamic context exploration)
  - Result: Record discovered core documents in `Reference Context` of impact analysis sheet
- **MEDIUM/HIGH**: **Write impact analysis sheet** with `docs/templates/impact-analysis-template.md` (okay to be short) + file creation required
- **LOW**: Leave only **light record** (minimize operational burden)
  - `WI-YYYYMMDD-###` in commit message + "Goal/Reuse/Impact" 3 lines
  - Or write `docs/templates/impact-analysis-template.md` very briefly (optional)
  - **Exception (N/A allowed)**: Only trivial changes like typo fixes, whitespace cleanup, formatting
- Find related Capability/Asset in `docs/registry/asset-registry.md` and write **reuse candidates**
- (For project work) First write domain context if none: `docs/templates/domain-context-template.md`

**Responsible (Recommended Assignment):**
- **MA (R/A)**: Issue WI + write impact analysis sheet (draft)
- **SE (R)**: Present 3 reuse candidates (code/pattern) + suggest implementation direction
- **SA/PG/RE/PS (C)**: Consult on domain/security/regression/business perspective if needed

### 4.2 Impact Analysis

Include below in impact analysis sheet.
- **Change Scope**: Impact to which layer (Local/Module/Package/Tool/Global)?
- **Dependencies**: What are targets consuming (consumer) this change?
- **Regression Risk**: What "contract/format/behavior" can break?
- **Test/Verification**: What are minimum regression protection devices?
- **Rollback Strategy**: Can immediately revert on failure?

#### 4.2.1 Domain Fit Gate

"Reuse according to rules" can be risky. Reuse fails if conflicts with **domain invariants**.
Therefore, must check below before deciding reuse.
- **Applicability**: Is this asset applicable to this domain? (purpose/cost/risk)
- **Invariants**: Does not break core invariants of domain?
- **Terminology**: No words used with different meanings? (term conflict)
- **Ownership**: Can domain owner (PS) approve "reuse is correct"?

#### 4.2.2 Traceability Gate

Refer to [traceability.md](traceability.md) for WI ↔ ADR ↔ Asset connection rules.

**Minimum Rule Summary**:
- All changes have WI
- MEDIUM/HIGH require ADR, WI ↔ ADR mutual links required
- Register reuse assets in registry and record Asset ID in WI

### 4.3 Implementation/Modification (Required Rules)

- **Prioritize Commonalization**: Prioritize commonalization if logic for same purpose already exists.
- **Contract-First**: For common tools (Platform Tool), fix **Contract (Schema/Permission/Failure)** before "code".
- **Maintain Traceability**: Leave as ADR if change reason/alternatives/trade-offs exist (light ADR also OK).

### 4.4 Cleanup (Required)

- Update `docs/registry/asset-registry.md` (new Asset registration/version/owner/usage location)
- Add ADR to `docs/adr/` if needed (especially MEDIUM/HIGH)
- **(MEDIUM/HIGH Required)** Verify WI ↔ ADR mutual links (traceability gate)
- Specify at least 1 **observability point (log/metric/alert)** from operational perspective
- (Optional) Create/update Work Item file: `docs/work-items/`

**Responsible (Recommended Assignment):**
- **SE (or change worker) (R)**: Update Consumers draft when modifying assets
- **DocOps (R)**: Verify registry Consumers/usage location update (check omissions during review)
- **EO (A)**: Approve common asset promotion/template/glossary policy + verify overall consistency

## 5. Definition of Done (DoD) — By Criticality

### 5.1 LOW
- Reuse candidate search record (even briefly)
- Update document/registry (if needed)
- (Recommended) Light record (commit/PR or brief impact analysis sheet)

### 5.2 MEDIUM
- Impact analysis (consumer/contract/regression points)
- Define minimum regression test or verification scenario
- Update registry (required)

### 5.3 HIGH
- ADR required (decision/alternatives/risks/rollback)
- PG/RE perspective review (security/reliability)
- Specify version policy/backward compatibility strategy if contract change

## 6. Reuse and Improvement Rules

When reusing, **explicitly** handle by choosing one of below.
- **A. Reuse As-Is**: Use without changes (safest)
- **B. Add Extension Point**: Add hook/option to existing code (maintain backward compatibility)
- **C. Refactoring/Cleanup**: Safely migrate with commonalization + testing
- **D. Replace (Deprecated)**: Migrate to new implementation + phase out existing (include migration plan)

## 7. Project Closure & Refining

When project ends, promote or organize knowledge and code created during execution as system-wide assets.

### 7.1 Closure Checklist
- [ ] **Review Asset Promotion**: Verify if useful Module/Package within project is global asset (Global Asset) candidate (use `asset-promotion-checklist.md`)
- [ ] **Synchronize Knowledge**: Migrate project-specific knowledge useful system-wide to `docs/standards/` or `docs/registry/`
- [ ] **Record Technical Debt**: Record unresolved problems to Technical Debt section of `asset-registry.md`
- [ ] **Environment Cleanup**: Delete temporary branches, cleanup local test environment (Docker etc.)
- [ ] **Final Retrospective**: Analyze agent failure patterns during work process to decide whether to update `Golden Set`

### 7.2 Refining
- Maintain project-specific code, but promote general utilities or policies to higher layer according to **Reuse Ladder** for immediate reuse in next project.

---

## Related Documents

### Required References
- **[development-workflow.md](development-workflow.md)**: Overall process flow overview.
- **[traceability.md](traceability.md)**: WI ↔ ADR ↔ Asset connection and traceability rules.
- **[asset-registry.md](../registry/asset-registry.md)**: System assets and capability list.

### Reference Documents
- **[system-design.md](../architecture/system-design.md)**: Overall design and agent collaboration mechanism.
- **[workboard.md](../registry/workboard.md)**: System-level task status management.
- **[quality-gates.md](../policies/quality-gates.md)**: Operation checklist by criticality.
