[WI HEADER]
WI ID: WI-20260809-ATS-048-QA-INTEG-REVIEW
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-048 implementation
Blocks: WI-20260809-ATS-048 remediation/finalization

[WI SUMMARY]
Why: Independently attack the Track/Tag contract hardening before final gates and commit.
Scope (in): Read-only review of every WI-048 source, test, and current-state document change; backend/frontend contract consistency; authorization; malformed input; asynchronous state recovery; preservation of deliberate existing behavior; focused counterexample tests when useful.
Scope (out): Editing files, Git operations, schema/data mutation, real Track/Tag deletion, external provider/mail/download effects, generic Track latest-request ownership assigned to WI-053, Album/Notice IDs, feature expansion.
DoD: Return PASS or actionable P0-P3 findings with exact file/line evidence, reproduction, expected behavior, and minimal remediation. Explicitly verify every concern and boundary listed below.
Constraints/Forbidden: READ ONLY. Do not create reporter output files, rewrite snapshots, run formatting with write mode, or touch/open/hash the protected output ZIP/folder. Do not inspect ignored secrets/local environment values.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] MP3/WAV UI contract matches backend analyzer without regressing the deliberate iOS picker workaround. Determine whether making `AUDIO_ACCEPT` unconditional while leaving `isIOS()` unused is a behavioral regression.
- [ ] `replaceTags=true` with absent IDs clears, true with IDs replaces, and omitted/false preserves even if IDs are supplied; multipart binding and UI request construction agree.
- [ ] Invalid/missing Track edit IDs issue zero Track/Tag calls; title/BPM/tonality validation and description clearing are correct.
- [ ] Track management URL normalization cannot loop or emit malformed requests; draft/applied keyword navigation, beyond-last pages, load retry, delete failure/retry, pending single-flight, committed-refresh recovery, and modal retirement are coherent.
- [ ] Decide whether changing established Korean labels such as `음원 관리`, `새 음원`, and `곡 제목 검색` to mixed `Track` labels is an unjustified UI/content regression rather than a required contract change.
- [ ] ADMIN-only Tag impact authorization holds even if broad GET Tag routes are permitted elsewhere; response is bounded and count is authoritative.
- [ ] Used/unused/impact-failure Tag confirmation cannot expose uninformed deletion and stale impact responses cannot replace the current target.
- [ ] Existing Track soft-delete behavior, Tag canonicalization/delete ordering, and WI-053 latest-request boundary are preserved.
Quality:
- [ ] Existing test files were not overwritten or materially reduced; the new untracked TrackManage test is valid UTF-8 and not dependent on corrupted strings.
- [ ] Tests assert behavior rather than merely mirroring implementation; identify missing counterexamples or false-positive mocks.
- [ ] Docs API count, fields, parameter names, use-case language, and deferred boundaries match code.
- [ ] Report P0-P2 as blocking. P3 can be nonblocking only with explicit rationale.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
- docs/policies/security-policy.md

Tier 2:
- docs/standards/frontend-standards.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-tag.md
- docs/design/api-spec.md

REQ/WI:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-048-handoff.md
- deliverables/agent/WI-20260809-ATS-048-evidence-pack.md
- deliverables/user/WI-20260809-ATS-048-summary.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md

Implementation/tests:
- All files listed under `Exact Changed Files` in the WI-048 evidence pack
- `frontend/src/utils/routeId.ts`
- `frontend/src/api/tracks.ts`
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
- Existing pre-WI versions available through `git diff` and `git show HEAD:<path>`

[OUTPUT CONTRACT]
Agent-facing -> deliverables/agent/WI-20260809-ATS-048-qa-integ-review-result.md
- PASS/FAIL, ordered P0-P3 findings, evidence, commands, coverage gaps, and exact remediation recommendation.
User-facing -> no separate summary; parent will consolidate.

[TRACEABILITY REQUIREMENTS]
- Map findings to CR-031-056, CR-031-057, Track slice of CR-031-061, bounded CR-031-063, and CR-031-064.
- Distinguish verified implementation defects from intentional WI-053 deferrals.
- Record every command and result. Do not claim a full suite unless actually run.
- State explicitly that no file was edited and protected output was untouched.
