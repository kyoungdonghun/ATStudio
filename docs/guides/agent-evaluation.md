---
version: 1.0
last_updated: 2026-01-06
project: system
owner: RE
category: guide
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
  - path: eval-golden-set.md
    reason: Golden Set operation guide reference
---
# Agent Activity Evaluation Operation Guide

> Purpose: Fix **measurement → improvement → regression prevention** loop for "how well each agent is doing their work".
> This document is an **operational discipline** to prevent quality degradation even when models/prompts/policies/routing/tools change.

## 1) Why Evaluation is Needed (Brief)

- **Improvement points become visible**: Improvement points become clear based on failure patterns (why it failed).
- **Role-specific responsibilities become clear**: What SA/PG/RE/SE/MA/EO did well/poorly is separated.
- **Prevent recurrence**: Fix problems once solved as Golden Set/checklist.

## 2) Evaluation Targets (What to Evaluate)

### 2.1 Actor Units (By Role)

- **MA**: Was task breakdown/criticality/impact analysis appropriate?
- **SA**: Did design decisions remain as ADR including alternatives/risks/rollback?
- **SE**: Was reuse candidate search sufficient? (prevent duplication/copy-paste)
- **PG**: Were secrets/PII/permission risks blocked in advance?
- **RE**: Was regression protection (tests/scenarios/Golden Set) sufficient?
- **EO**: Were policy/registry/template governance consistently applied?
- **Execution (HITL)**: Were execution approval compliance, destructive prevention, rollback capability secured?

### 2.2 Deliverable Units (By Artifact)

- WI (Work Item): Did goal/reuse/impact/verification/rollback meet minimum criteria?
- ADR: Were alternatives/decisions/risks/mitigation/migration/rollback included?
- Registry: Are Asset/Consumers/Status up-to-date?
- Glossary: Are there no term conflicts and is Key-based tagging maintained?

## 3) Evaluation Criteria (Minimum Metrics)

> Use "quantitative + qualitative" together. Quantitative alone allows pretense of being good, qualitative alone makes improvement slow.

### 3.1 Common (All Actors)

- **Policy Compliance**: Compliance with `policies/quality-gates.md`/HITL/Secrets/Versioning
- **Traceability Completeness**: WI↔ADR↔Asset link omission rate
- **Reuse Evidence**: Presence of reuse candidate search traces (Asset ID/path)
- **Regression Protection**: Minimum verification/regression protection presence

### 3.2 Actor-Specific Core Metrics (Examples)

- **MA**: Criticality misjudgment (cases treated as LOW but actually MEDIUM/HIGH) count
- **SA**: ADR omission count in MEDIUM/HIGH
- **SE**: Duplicate creation (created new when existing asset existed) count
- **PG**: Secret/PII exposure attempts (or risks) count, blocking success rate
- **RE**: Golden Set failure case recurrence count
- **EO**: Policy/template drift (document contradictions) occurrence count
- **Execution (HITL)**: Destructive execution attempts without approval (=must be 0), execution plan/verification plan omission count

## 4) Operational Process (Most Realistic Minimum Loop)

### 4.1 Evaluation Triggers and Timing

- **MEDIUM/HIGH Tasks**: **Immediately after PR merge** perform post-evaluation (mandatory DoD item).
- **LOW Tasks**: Sampling evaluation (recommended, weekly or 1 random out of 10).
- **System Changes**: Perform Golden Set regression immediately after model/routing/policy changes (mandatory).

### 4.2 Evaluation Procedure (10~20 minute cut)

1) **Execute immediately after task completion**: Trigger evaluation session when PR merges by MA or `RE`.
2) **Review based on Work Item**: View WI/PR/commit/log as one bundle.
3) **Checklist evaluation**: PASS/FAIL judgment based on `docs/policies/quality-gates.md`.
4) **Failure classification (select 1~3)**: Reuse failure / Impact omission / Traceability omission / HITL omission / Secrets risk / Regression deficiency / Domain Fit conflict.
5) **Write improvement actions**: Record only 1~3 "who/what/by when".
6) **Fix regression prevention**: Add Golden Set case or modify policy document if repeated failure.

## 5) Evaluation Deliverables (Minimum Records to Keep)

### 5.1 Evaluation Report (Recommended Location)

- `docs/eval/eval-YYYYMMDD-###.md` (or project repo's `docs/project/eval/`)

### 5.2 Report Template (Simple)

- Template: `docs/templates/eval-report-template.md`

- **WI**: WI-YYYYMMDD-###
- **Criticality**: HIGH | MEDIUM | LOW
- **Actors involved**: MA/SA/SE/PG/RE/EO
- **PASS/FAIL**: (based on `policies/quality-gates.md`)
- **Findings**: (1~2 good points, 1~3 problems)
- **Actions**: (Owner + Due)
- **Golden Set update?**: Yes/No (case ID)

## 6) Roles/Responsibilities (Recommended)

- **RE (R/A)**: Operate evaluation system (metrics/Golden Set), maintain regression criteria
- **EO (A)**: Prevent policy/template/registry drift, final responsibility for operational quality
- **MA (R)**: Trigger/schedule/track evaluation at WI level + share execution phase (HITL) compliance (log/plan)
- **PG (C)**: Consult on security/sensitive information cases
- **SA/SE (C)**: Provide improvement proposals from design/implementation perspectives
