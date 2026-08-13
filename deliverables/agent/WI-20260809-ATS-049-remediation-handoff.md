# Remediation Handoff: WI-20260809-ATS-049

[WI HEADER]

- WI ID: `WI-20260809-ATS-049-REM`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-049-QA-INTEG`
- Blocks: WI-049 QA re-review and final full gates

[WI SUMMARY]

## Why

Close all four independent P2 findings in `WI-20260809-ATS-049-qa-integ-review-result.md` with exact counterexample tests. Preserve the rest of the WI-049 patch and all WI-038 reorder behavior.

## Required Remediation

1. `QA-049-001`: retain membership-refresh provenance across retries. A rejected/unknown reorder must never become “committed” merely because a later read retry also fails. Prove rejection -> recovery read failure -> retry read failure and the corresponding successful retry state.
2. `QA-049-002`: remove invented filename-extension rejection. Keep the file picker `accept` as an advisory hint and client-check only current contract facts: JPEG/PNG MIME when supplied, size, decodability, dimensions, pixels. The backend remains byte-signature authority. Prove extensionless valid JPEG/PNG reaches decode/selection, incompatible supplied MIME is rejected, and corrupt data is rejected by decode.
3. `QA-049-003`: implement Home/End navigation plus focus-aware blur/focus-out dismissal without breaking pointer option selection. Prove active option and `aria-activedescendant`, Home, End, Enter, Escape, outside focus, and pointer selection.
4. `QA-049-004`: exclude current members from search and prevent duplicate add after a committed add whose authoritative refresh failed. Use a bounded local committed-membership fence or disable membership mutation until refresh; clear/reconcile it only from an authoritative read. Prove initial-member exclusion and committed-but-refresh-failed duplicate exclusion with mutation call counts.

## Documentation and Evidence

- Correct any WI-049 docs, evidence, or summary text affected by the remediation.
- Add a remediation section to the evidence pack that maps each QA finding to source and exact test.
- Do not change the independent QA result; it is immutable historical evidence.

## Constraints

- No schema/API/dependency/product-policy changes.
- No broad redesign or WI-059/WI-070 work.
- No real browser mutation, DB/storage/media/external effect, secret inspection, protected output access, Git stage/commit/push, branch operation, or deployment.
- Do not weaken tests, change backend signature rules, or restore filename-extension authority.
- Run focused frontend tests, backend `AlbumServiceTest` only if backend changes or contract proof needs rerun, typecheck, lint, changed-file Prettier, build, docs validation, and diff check.

[ACCEPTANCE CRITERIA]

- [ ] All four QA counterexamples fail before remediation or are demonstrated against prior source and pass after remediation.
- [ ] Mutation call counts prove no duplicate reorder/add.
- [ ] Combobox ownership remains accessible and pointer-operable.
- [ ] Existing 81-test focused/adjacent suite and new counterexamples pass.
- [ ] Static/build/doc gates pass.
- [ ] Evidence/summary are final-state accurate and no independent result is overwritten.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-049-qa-integ-review-result.md`
- `deliverables/agent/WI-20260809-ATS-049-handoff.md`
- `deliverables/agent/WI-20260809-ATS-049-continuation-handoff.md`
- `deliverables/agent/WI-20260809-ATS-049-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-049-summary.md`
- Current WI-049 working diff excluding `output/**`
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`
- `docs/standards/frontend-standards.md`
- `docs/design/usecase/sound-album.md`

[OUTPUT CONTRACT]

- Scoped source/test/doc/evidence/summary corrections in the shared workspace.
- Final report with finding-by-finding closure, exact tests/results, changed files, residual risks, and escalation status.
- No commit/push.
