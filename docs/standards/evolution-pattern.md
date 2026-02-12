---
version: 1.0
last_updated: 2026-01-07
project: system
owner: EO (Ensemble Overseer)
category: standard
status: stable
dependencies:
  - path: ../standards/core-principles.md
    reason: Implementation of Principle 5 (Sustainability of Evolution)
---

# Standard Evolution Procedure and Patterns (Standard Integration Procedure - SIP)

> **"Unorganized improvement is akin to destruction."**
>
> This document defines **concise and powerful procedures** that agents must follow when attempting to improve (evolve) the system. Prevents cost waste and allows only meaningful evolution.

---

## 1. Evolution Trigger (The Rule of Three)

Do not refactor arbitrarily. Start improvement process (SIP) only when the following conditions are met.

1. **Repetition:** "I'm writing this code/this document identically for the 3rd time." → **Pattern candidate**
2. **Intuitive Error (Friction):** "User confused about this setting 3 times in a row." → **UI/UX improvement candidate**
3. **Explicit Request:** When user directly says "This is inconvenient".

> **Anti-Pattern (Prohibited):** "Just because the code doesn't look pretty", "Because a new library was released" (No modification without business benefit)

---

## 2. Standard Evolution Procedure (SIP Steps)

Improvement work follows the following 3-step pipeline.

### Step 1. Isolation

Detach target from entire system for analysis.

- **Action:** Extract only one repeating code block or inefficient process.
- **Check:** "Will other parts break if only this is fixed?" (Check dependencies)

### Step 2. Abstraction

Convert hardcoded values to variables and make into **template/function** usable in general situations.

- **Action:** Transform concrete values (`UserA`) → parameters (`user_id`).
- **Result:** Birth of reusable 'asset'.

### Step 3. Integration

Register created asset in system standard location (`libs/`, `docs/templates/`) and replace existing code to use this asset.

- **Action:** Delete existing duplicate code → `import new_tool`

---

## 3. Agent Action Guidelines (Agent Protocols)

### 👨‍💻 Routine Task

- **Guideline:** "Don't think, just execute."
- **Example:** Typo fixes, simple log checks, document creation in defined format.
- **Gate:** **Fast Path**. No improvement attempts. (Target cost $0 convergence)

### 👷 Evolution Task

- **Guideline:** "Follow SIP procedure."
- **Trigger:** When `Trigger` condition found during work, immediately stop work and issue **[Improvement Proposal]** ticket to MA.
  - _"Boss, this logic is repeating for the 3rd time. Should we activate SIP to extract it as common function?"_
- **Gate:** Execute only with user approval (M-Gate) or `EO` approval.

---

## 4. Evolution Pattern Examples

| Situation       | Before (Existing)              | After (Evolved)                        | Benefit (ROI)             |
| :-------------- | :----------------------------- | :------------------------------------- | :------------------------ |
| **Duplicate Questions** | Ask "Which DB to use?" every time | Fixed in `.claude/config/context.json` as `db: sqlite` | 90% token cost reduction  |
| **Repeated Coding** | Write `request.get` and handle exceptions every time | Call `fetch_url()` utility function    | Error rate converges to 0% |
| **Repeated Documents** | Write new table of contents every time | Use copy of `template.md`              | Standardize document quality |

---

## 5. Conclusion

**"Keep simple things simple, complex things as patterns."**
When following this principle, we can reduce unnecessary costs to 0 and continuously grow the system.
