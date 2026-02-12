---
version: 1.0
last_updated: 2026-01-06
project: system
owner: EO
category: guide
status: stable
dependencies:
  - path: request-intake.md
    reason: Understanding request process
  - path: ../policies/execution-policy.md
    reason: Approval (HITL) policy reference
---

# Actor Guidelines

> Purpose: Clearly define the roles and responsibilities of Users and Agents within the system to establish an efficient collaboration framework.

---

## 1. User Guidelines

Users are the entity that determines the **direction** of the system and **approves** final results.

### 1.1 Key Roles
- **Define Goals (What)**: Clearly present the problem to be solved or the goal to be achieved.
- **Provide Context**: Share necessary background knowledge and constraints so agents can make correct judgments.
- **Decision Making and Approval (HITL)**: Make final approval decisions for destructive or important changes.
- **Review Results and Provide Feedback**: After task completion, review if results meet goals and provide feedback.

### 1.2 Collaboration Principles
- **Clarity**: Requests with specific examples or criteria produce better results than vague requests.
- **Trust and Verification**: Respect agent autonomy but thoroughly verify at critical checkpoints (quality gates).
- **Continuous Feedback**: Provide specific feedback so agents can learn and improve.

---

## 2. Agent Guidelines

Agents are the entity that **plans** and **executes** to achieve user goals.

### 2.1 Key Roles
- **Assess Ambiguity and Explore**: When receiving a request, ask questions to clarify intent if ambiguous, or proceed directly to exploration if clear. Refer to [development-workflow.md](development-workflow.md) for detailed flow.
- **Safe Execution**: Report snapshots (expected changes/impact) before file modifications, and always obtain user approval for tasks with risk factors.
- **Transparent Reporting**: Document decisions (ADR) and results during work processes and report to users.

### 2.2 Collaboration Principles
- **Proactive Reporting**: If problems arise or ambiguous points exist, immediately ask users and suggest alternatives.
- **Maintain Traceability**: File modifications and MEDIUM+ tasks must have justification, with records showing "why it was done this way".
- **Reuse-first**: Maximize efficiency by utilizing existing assets before creating new ones.
- **Independent Verification**: Work performed is objectively verified through other actors (RE, etc.).

---

## 3. Collaboration Checkpoints (Interaction Points)

| Situation | User's Role | Agent's Role |
|------|--------------|----------------|
| **Request Phase** | Write and submit template | Analyze request and report WI breakdown |
| **Planning Phase** | Review execution plan and ADR | Define WI dependencies and register to workboard |
| **Execution Phase** | Approve destructive tasks (HITL) | Perform tasks and update status in real-time |
| **Completion Phase** | Review final results and provide feedback | Report independent verification results and asset conversion |

---

## Related Documents

### Required References
- **[request-intake.md](request-intake.md)**: Guide for user request submission methods.
- **[execution-policy.md](../policies/execution-policy.md)**: Execution approval (HITL) and authority policy.

### Reference Documents
- **[operation-process.md](operation-process.md)**: Detailed task management rules for agents.
- **[development-workflow.md](development-workflow.md)**: Complete step-by-step workflow map.
- **[quality-gates.md](../policies/quality-gates.md)**: Quality gate checklist.
