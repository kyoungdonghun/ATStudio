# Evidence Pack: WI-20261025-ATS-001

## Summary

Minimal local repair of the two WI027 recovery defects; frozen candidate ready for independent WI002 review and MA execution. No remote operation occurred in this WI.

## Scope / DoD Check

- [x] Original WI023 candidate and all registered bytes preserved.
- [x] Reconcile service/log and evidence names use a fresh 32-hex attempt; original apply nonce remains the evidence directory and claim binding.
- [x] Only read-only reconcile survives the grant write cutoff; fresh authority, candidate and target checks remain.
- [x] Focused real-adapter lifecycle and retained exclusive-log helper check, date/authority checks, three-mode payload compilation.
- [x] MA recipe updated; independent review follows as WI002.

## Reference Documents (Tier 0-2)

Injected context follows WI-20261025-ATS-001-handoff.md: core-principles, development-standards, documentation-standards, glossary (Tier 0); AGENTS.md and security-policy (Tier 1); current index, approved REQ-20261024-ATS-001, WI027 review evidence and original WI023/WI024/WI025 tool folders (Tier 2). Assignee: SE. Authority persists under the stage-two single scope gate. Private references were reused as paths only, without reading secret bodies.

## Evidence Pointers

- Candidate: `build/WI20261025-001-stage-two-repair/candidate.json`
- Candidate SHA256: `480834a7fadc69d542dd9113deb0af877d67a570170aa1819c682feec81efe2c`
- Original manifest SHA256 remains `3fbbec606c60ac73c752d4f082965692f589311a413746a8bb938733a0070b46`; all original manifest entries verified unchanged.
- `transaction.py`: mode-specific exact authority shape, reconcile-attempt format/distinctness, reconcile-only cutoff exemption.
- `native_adapter.py`: attempt-specific service and exclusive plan/status/result files; original apply nonce and candidate claim binding retained.
- `test_recovery.py`: three focused checks. `test_stage_two.py` only adapts its proposal fixture to the new reconcile field.
- `README.md`: current builder path, fresh probe/server-clock sequence, inspect/apply/reconcile and cleanup instructions.
- Test output: `build/WI20261025-001-stage-two-repair-tests.log`.

## Commands & Outputs

`& "$env:LOCALAPPDATA/Python/pythoncore-3.14-64/python.exe" -B build/WI20261025-001-stage-two-repair/test_recovery.py` → 3 tests PASS, 0.027 seconds.

Actual `make_payload.build` called with synthetic inspect/apply/reconcile records bound to the final manifest; all returned payloads compiled without execution. Builder also verifies its reused transitive artifacts. Original manifest and every listed original file hash matched.

## Tests and Limits

The lifecycle test executes actual `native()` and AST-loaded retained `prepare_mysql_log` / `mysql_log_path`: first start fails after exclusive log creation; second fresh attempt succeeds read-only using the same apply nonce and same issued second; replay of that attempt is denied. Two retained log files, unchanged claim bytes, no DML/COMMIT, stopped state and cleanup checks are asserted. Date matrix covers Nov 24/25; fresh read-only authority cannot be replaced by write permission, expired time or incorrect identity.

Windows/Linux ownership, signals, process/socket, systemd and MySQL boundaries are synthetic; local exclusive log creation is real. An initial local harness assertion failed because Windows lacks SIGHUP and the test had not adapted that signal boundary; cleanup was empty because no start occurred. The harness signal boundary was corrected, then all three checks passed. This was a harness failure, not a production/native-host failure. No server execution or production PASS is claimed. Existing 11 contract and seven fault tests were reused as historical evidence, not rerun.

## Risks / Rollback

The actual source host, schema and complete snapshots still require fresh authorized MA inspection. Do not change system clocks or weaken authority windows: obtain fresh server epoch and wait until issued_at has passed. Old approval label remains Oct 24 because it identifies the original scope; issued_at is fresh.

An uncertain write must be reconciled against its original apply claim with a new attempt identifier. Preserve all failed attempts and logs. No automatic reverse mutation or new full backup is introduced. The four-column private recovery evidence remains bounded to this transaction; it is not a complete current disaster-recovery point. Parent owns exact-delta native verification and DB/access restoration.

## Follow-ups

WI002 independent review gates MA continuation. WI003 documentation records final native outcome after actual evidence exists. No source/JAR, application behavior, product policies, mail provider or PG changes are included.
