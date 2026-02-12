---
version: 2.0
last_updated: 2026-02-07
project: system
owner: SA
category: standard
status: stable
dependencies:
  - path: core-principles.md
    reason: Context ordering efficiency principle (Article 12)
tier: 2
target_agents:
  - sa
  - eo
task_types:
  - architecture
  - implementation
---

# Context Ordering and Efficiency Strategy

## 1. Overview

Context ordering matters for prompt caching efficiency. While Claude Code manages caching transparently, the order in which documents are injected to subagents affects cache hit rates and consistency.

> [!IMPORTANT]
> **Consistent Prefix**: Injecting documents in a consistent order ensures maximum cache reuse. Random or arbitrary ordering reduces efficiency and violates the System Constitution.

## 2. When Ordering Matters

Context ordering is critical when:

1. **Static, large content is reused frequently**: Constitution documents, standards, glossaries
2. **Subagent delegation is common**: MA frequently calls subagents with similar base context
3. **Tier 0 documents are mandatory**: Every task requires constitutional context

| Scenario | Ordering Impact | Reason |
| :--- | :--- | :--- |
| **System Constitution** | ✅ **CRITICAL** | Injected in every delegation and never changes. |
| **Agent Persona** | ✅ **HIGH** | Stable per agent role, reused across tasks. |
| **Task Context** | ⚠️ **MEDIUM** | Varies per task but should follow consistent patterns. |
| **Snapshot Data** | ❌ **LOW** | Changes every turn, minimal reuse benefit. |

## 3. Ordering Architecture

To maximize prefix consistency and cache hit rates, the system enforces strict ordering:

```
[Tier 0: Constitution] → [Tier 1: Persona] → [Tier 2: Context] → [Snapshot]
```

**Rationale:**
- **Tier 0 first**: Mandatory for all agents, never changes, largest shared prefix
- **Tier 1 second**: Agent role/persona, stable per agent type
- **Tier 2 third**: Task-specific context (standards, guides, ADRs)
- **Snapshot last**: Dynamic, changes per task

**Reference:** See `CLAUDE.md` Section "Tier 0 Required Documents" and "Injection Order (Prompt Caching)".

## 4. Cost Awareness

Beyond ordering, minimize context size to reduce token consumption:

1. **Use pointers instead of full documents**: Reference file paths and line numbers instead of injecting entire files
2. **Apply context engineering**: Use `/ce` skill to design minimal injection bundles
3. **Avoid redundant injection**: Don't inject documents already available in higher tiers
4. **Filter by scope**: Exclude "meta-only" documents when working in domain projects (tag != SYS)

**Anti-Pattern:**
- ❌ Injecting all documents "just in case"
- ❌ Randomly loading documents in arbitrary order
- ❌ Duplicating Tier 0 content in Tier 2 context

## 5. Implementation Guidelines

### 5.1 Use Automation

- **Always use `/create-wi-handoff-packet` skill**: Automatically orders Tier 0 documents first
- **Use `/ce` skill for context engineering**: Designs minimal injection bundles based on task type
- **Trust the system**: Skills handle ordering automatically; manual packet creation risks violations

### 5.2 Verification

Before delegating to a subagent, verify:
1. ✅ Tier 0 documents are present and ordered first
2. ✅ Agent persona (Tier 1) follows Tier 0
3. ✅ Task-specific context (Tier 2) follows persona
4. ✅ Snapshot/dynamic data comes last

### 5.3 Enforcement

Violations of context ordering are treated as **constitutional violations** per `core-principles.md` Article 12:

> "Loading documents in arbitrary order is considered 'context waste' and violates this constitution."

## 6. Action Items

| Action | Responsibility | Enforcement |
| :--- | :--- | :--- |
| Ensure handoff packets follow Tier ordering | MA | Use `/create-wi-handoff-packet` skill |
| Design minimal context bundles | MA | Use `/ce` skill before delegation |
| Verify Tier 0 presence in subagent context | Subagent | Halt and report if missing |
| Monitor for ordering violations | EO | Weekly governance audit |

---

## Related Documents

- [Core Principles](core-principles.md): Article 12 - Context Injection Efficiency
- [Development Standards](development-standards.md): Section 1.3 - Context Injection & Safety
- [CLAUDE.md](../../CLAUDE.md): Tier 0 Required Documents, Injection Order
