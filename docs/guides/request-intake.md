---
version: 1.1
last_updated: 2026-01-06
project: system
owner: MA
category: guide
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
  - path: ../templates/project-request-template.md
    reason: Request template reference
---

# Starting Point: Request Intake Guide (User Start Here)

> Purpose: Guide how users deliver new goals or requirements to the system.
> Users **just speak requirements**, then **MA (Main Agent)** confirms intent through questions and writes **REQ (requirements definition)**.

## 1. What Users Do

Users start with **one convenient method** among below.

### A) Easiest Method (Recommended): Just Speak
- Just speak "what/why/roughly what state is success" in natural language.

### B) Start with Template (Optional)
Can write with one of below templates (optional).

- **New Project Start**: [project-request-template.md](../templates/project-request-template.md)
- **Additional Requirements/Feature Request**: [requirements-request-template.md](../templates/requirements-request-template.md)
- **Parallel Work Context Request**: [context-request-template.md](../templates/context-request-template.md)

> [!TIP]
> If difficult to fill all template items, prioritize writing only core goal (Goal) and background (Context). **MA supplements** rest through questions.

## 2. Process After Request Submission (Agent's Role)

When user delivers requirements, **MA (Main Agent)** performs:

1. **Ambiguity Assessment**: Proceed directly to exploration if request file/feature/action clear. If ambiguous, first confirm intent with 1-2 questions.
2. **(If Ambiguous) Write and Report REQ**: Write requirements definition (REQ) draft → report to user
3. **Exploration**: Investigate related files/code (proceed freely without gates)
4. **Snapshot Before Execution**: Output snapshot and receive user approval when file modification scope large or action change
5. **(Complex Tasks) Delegate to Subagent**: Write WI (execution unit) → instruct Subagents
6. **Reflect Progress**: Reflect status in [workboard.md](../registry/workboard.md) etc. index (optional/recommended)

## 3. How to Check Progress

Users can check progress status anytime through **[workboard.md](../registry/workboard.md)**.

---

## Related Documents

### Required References
- **[actor-guidelines.md](actor-guidelines.md)**: Roles and responsibilities of users and agents.
- **[operation-process.md](operation-process.md)**: Detailed task management rules for agents.

### Reference Documents
- **[development-workflow.md](development-workflow.md)**: Standard flow (1~7).
- **[workboard.md](../registry/workboard.md)**: Real-time task dashboard.
- **[future-policy-stubs.md](../policies/future-policy-stubs.md)**: Phase 2+ reserved policies (remote requests etc.).
