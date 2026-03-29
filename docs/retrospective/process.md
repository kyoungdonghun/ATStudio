# Process Lessons

> Covers multi-agent workflow, REQ/WI operations, documentation, and memory management.
> Discovered during ATStudio development using the MA → Subagent orchestration model.

---

## REQ → WI → Subagent Flow

### What Worked

The three-gate model (REQ approval → WI creation → delegation) prevented wasted work effectively. Key wins:
- REQ forced scope clarity before any code was written
- WI handoff packets gave subagents self-contained context — minimal back-and-forth
- Two-set deliverables (user-facing summary + agent-facing evidence pack) made review and traceability clean

### What to Improve

**Lock vocabulary in REQ-001.**
Domain naming confusion (Playlist vs Album) cascaded across 3+ sessions and required a dedicated REQ to fix. Before any WI is created, define and lock domain terms in a glossary or REQ-001 scope section.

**Define API response envelope in REQ-001.**
`{ "dataList": [...] }` vs raw array was decided inconsistently per endpoint. Retrofitting mid-project affected 15+ APIs. Decide once, enforce from day one.

---

## WI Granularity

### Optimal Size

- **5–15 files** per WI worked well. Under 5 = overhead. Over 20 = bottleneck.
- Quality checks should **always** be split: `typecheck ∥ eslint ∥ test` → 3 separate WIs
- Code reviews should be split by functional area — not "full review" in one WI

### WI Chain Rule (Non-Negotiable)

After completing any WI, **immediately** check which WI it unblocks in REQ PARALLEL WORK PLAN and trigger it. This was the most common failure mode — getting absorbed in writing deliverables/commits and forgetting to chain.

Checklist after each WI completion:
1. Mark WI as complete in evidence pack
2. Check REQ PARALLEL WORK PLAN → find WIs that depended on this one
3. Immediately create next WI handoff → delegate
4. Then commit/memory update

---

## Agent Role Separation

### se vs re

| Role | Responsibility |
|------|---------------|
| `se` | Implement + **write tests** |
| `re` | Run tests + verify + report |

Mixing these caused test duplication and unclear ownership. `re` should never write new tests — only run existing ones and verify results.

### cr vs re

| Role | When |
|------|------|
| `re` | After `se` completes — run tests, verify build |
| `cr` | After `re` passes — review code quality, find bugs |

Running `cr` before `re` wasted review effort on code that would change after test failures.

### Sandbox Limitations

- `re` and `cr` agents may lack Write permission in some environments
- When they cannot write deliverable files (evidence pack, summary), MA should write them directly using the Write tool
- This is expected behavior — do not retry; just handle it at MA level

---

## Documentation Workflow

### Screen List → Modal List → Wireframe Sequence

For frontend preparation, this order worked well:
1. **Screen list** (`atstudio-front-list.md`) — enumerate all screens with API refs
2. **Modal list** (`modal-list.md`) — enumerate all modals per screen with component classification
3. **Wireframe analysis** — image → text screen spec → approval → subagent injection

Starting with wireframes before the screen list creates confusion about what's in scope. Screen list first forces scope clarity.

### Two-Track Documentation

| Category | Language | Example |
|----------|----------|---------|
| System docs (standards, guides) | English | `docs/standards/`, `docs/retrospective/` |
| User-facing requirements | Korean | `deliverables/user/REQ-*.md` |
| Conversation | Korean | Chat, commit messages |

This separation worked well throughout the project. English docs survive translation overhead when team changes; Korean REQs allow precise user intent capture without translation loss.

---

## Memory Management

### MEMORY.md Size Limit

The project memory file (`MEMORY.md`) has a 200-line display limit. Beyond that, older content is truncated and invisible to the agent. Key practices:

1. **Keep MEMORY.md as an index** — short summaries + file paths, not full content
2. **Move detail to topic files** — e.g., `memory/engineering.md`, `memory/decisions.md`
3. **Prune after each major phase** — old REQ completion details become irrelevant once the project moves to the next phase
4. **Never duplicate CLAUDE.md** — don't repeat project instructions in MEMORY.md

### What to Persist in Memory

| Keep | Remove |
|------|--------|
| Current phase + next session starting point | Detailed WI completion logs |
| Confirmed design decisions | Old REQ details once phase is complete |
| Unresolved issues (low priority) | Detailed test counts per commit |
| Key file paths | Per-commit change tables |
| Lessons learned (compressed) | Repeated content from CLAUDE.md |

---

## Commit Discipline

- Commit after each WI completion — one commit per REQ phase or WI is clean
- Commit message format: `type: description (REQ-ID)` e.g., `feat: Album 신규 도메인 구현 (REQ-20260303-ATS-003)`
- Never mix multiple WIs in a single commit — traceability breaks
- Use `build -x test` only when explicitly needed — never skip test verification before push

---

## Context Injection Rules

Subagents do not read files independently — MA injects context. Key rule:
- **Tier 0 first** (standards/constitution) → **Tier 1** (policies) → **Tier 2** (task context)
- This order enables prompt caching — same prefix across WIs
- Never inject the entire API spec when only 2 sections are relevant — use line-range pointers

When a subagent produces wrong output, the first thing to check is whether the relevant context was injected in the handoff packet.
