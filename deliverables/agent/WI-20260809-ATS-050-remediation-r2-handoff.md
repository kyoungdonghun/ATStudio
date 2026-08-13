# Remediation R2 Handoff: WI-20260809-ATS-050

[WI HEADER]

- WI ID: `WI-20260809-ATS-050-REMEDIATION-R2`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: WI-050 QA integration rereview
- Blocks: WI-050 final independent review, finalization, full gates, and commit

[WI SUMMARY]

## Why

Close the remaining/new P2 findings `F-QA-INTEG-050-001`, `-004`, `-005` and complete the P3 transition matrix without schema, dependency, endpoint, or policy expansion.

## Scope and Required Direction

### Ambiguous create observation fence

- Persist a minimal non-secret, session-scoped `ambiguous Notice create requires observation` flag across route retirement/remount. Do not persist Notice title/content/files or claim outcome.
- An unrelated route must not clear it. The create page must remain non-mutable while the flag exists.
- Clear the flag only after the existing ADMIN Notice list successfully completes its observation-only GET in the same browser session. A failed/cancelled list GET must not clear it.
- Preserve normal create behavior for a fresh session and after a proven successful list observation.
- Keep update/delete recovery on the existing non-counting ADMIN GET path.
- Use a small named utility with guarded storage access so unavailable/sessionStorage exceptions do not crash rendering. In-memory conservative fallback is acceptable for the current tab lifetime.

### Pending unload ownership

- Replace unconditional React Router `useBeforeUnload` registration with an actual add/remove lifecycle tied to reactive pending ownership.
- Keep `useBlocker` using current immutable/ref ownership so same-tick navigation cannot slip through.
- Spy on listener registration/removal for idle, pending, authoritative success, authoritative 4xx, ambiguous outcome, and unmount.

### Busy Modal focus

- When a busy modal has zero enabled focusable descendants, prevent Tab/Shift+Tab and retain/focus the dialog itself.
- Test the real all-disabled Notice delete modal, not only an artificial enabled child.
- Preserve non-busy Modal close/focus behavior.

### Evidence matrix

- Add blocked-transition-before-4xx and blocked-transition-before-ambiguous tests with exact mutation/navigation counts.
- Add create and delete success destination GET failure/retry schedules where existing page/list boundaries permit it; prove retries execute GET only and never repeat POST/DELETE.
- If a destination schedule genuinely belongs to a separate component outside the WI-050 change surface, prove it through an integration harness without altering that component's behavior.

[ACCEPTANCE CRITERIA]

- [ ] Ambiguous create, unrelated navigation, remount, and repeated submit remains exactly one POST and zero unlock until a successful ADMIN list GET.
- [ ] Successful ADMIN list observation clears the fence; failed/cancelled GET does not.
- [ ] No user content/file metadata is stored in session storage.
- [ ] `beforeunload` add/remove ownership is exact across all terminal schedules.
- [ ] Busy zero-action Modal traps Tab on the dialog; failure restores normal focus/close behavior.
- [ ] Original/new P2 counterexamples and P3 matrix have focused exact-count tests.
- [ ] Focused/adjacent tests, typecheck, ESLint, changed-file Prettier, docs validation, and diff check pass.

[CONSTRAINTS]

- No schema, dependency, endpoint, attachment policy, provider, security-policy change, real mutation/download, live DB/storage/file/external effect, secret/protected-output access, staging, commit, push, or branch action.
- Do not create final evidence/summary; finalization follows an independent PASS.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-050-qa-integ-rereview-result.md`
- `deliverables/agent/WI-20260809-ATS-050-remediation-handoff.md`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`
- Current WI-050 diff excluding `output/**`.

[OUTPUT CONTRACT]

- Implement directly in the shared workspace.
- Report changed files, red/green exact test counts, and any unresolved blocker.
