# WI P3 Closure Handoff: WI-20260809-ATS-051

[WI HEADER]

- WI ID: `WI-20260809-ATS-051-P3-CLOSURE`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-051-REMEDIATION`
- Blocks: WI-051 conclusive review

[SCOPE]

1. Close `ATS-051-QI-05` with exact positive and zero API invocation proof for every Whitelist subscriber status/action combination: edit, request, primary, immediate delete, removal request, and already-removal-requested inert state. Confirm dialogs where applicable and assert exactly one intended call.
2. Close `ATS-051-QI-04` by reducing `docs/design/api-spec.md`, `docs/design/usecase/whitelist.md`, and `docs/design/usecase/company-certification.md` to semantic WI-051 changes only. Remove unrelated table/list/payment formatting churn. Correct CR-031-077 language to ADMIN Whitelist review note. Preserve verified company-certification retry/gating documentation, but do not claim certification note tests as CR-077.
3. Correct `deliverables/agent/WI-20260809-ATS-051-handoff.md` if needed so CR-031-077 and its acceptance criteria point to ADMIN Whitelist note.

[CONSTRAINTS]

- Modify only tests and the four named docs/handoff. Do not change production code.
- Use `apply_patch` for manual edits. Do not use `git checkout`, `git restore`, reset, or file-replacement shell writes.
- Do not change policy, schema, dependencies, backend behavior, or WI-040 export behavior.
- No protected-output/ignored-secret access, real external/DB effect, commit, or push.

[ACCEPTANCE]

- Focused Whitelist tests pass with per-status call-count proof.
- `git diff --ignore-all-space` and normal diff show only relevant semantic doc additions; unrelated payment/API tables remain byte-equivalent where not touched.
- Changed-file Prettier check, docs validator, and `git diff --check` pass.
