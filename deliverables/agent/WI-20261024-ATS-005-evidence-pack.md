---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: SE
category: evidence
status: completed
---
# Evidence Pack: WI-20261024-ATS-005

## Summary

Implemented the authorized local audio wiring: dedicated Korean ID1 capture, private reuse of saved ADMIN2/buyer3, genuine external receipt binding, exact root-pipe stage/start preparation, strict one-use SSH caller, sanitized milestones and deterministic one-MP3 private transfer. Native execution remains separately gated and NOT_RUN.

## Scope / DoD Check

- [x] New owned wrapper only; frozen cd32 audio candidate, source/JAR and prior captures unchanged.
- [x] Existing ID1 password contract 1..256/no NUL, with Unicode/spaces/short values preserved; no new-account password policy.
- [x] Owner-only ACL, exclusive files, Korean fixed errors, retained invalid entries, unchanged saved account inputs.
- [x] Current material is deterministically reserialized before future execute; stale payload/authority substitution rejected.
- [x] Original approval/receipt/source bytes are required and pinned; no native user/PG/target/backup PASS is generated.
- [x] Exact fixed root child `audio_entry.py --stage` receives the actor frame via root-owned stdin PIPE and EOF; original runtime/cleanup preserved.
- [x] Public code modes 0755/0644 and private evidence 0700/0600 separated; prior files/evidence are never overwritten to force progress.
- [x] One-MP3 linkage, all 13 cleanup checks, final cold re-observation and local byte/hash receipt precede private delivery; partial milestones survive HOLD.
- [x] SE Python 7 and actual Save/private synthetic storage 1 PASS. Final PG independent Python 8 and Save 1 PASS; exact local capture/preparation allowance issued, native authorization false.
- [ ] Actual ID1 input/binding/login, native authorization, current target/backup coverage, server/audio/transfer/listening: NOT_RUN by SE.

## Reference Documents (Tier 0-2)

The [generated WI005 handoff](WI-20261024-ATS-005-handoff.md) supplied the governing context.

| Tier | Injected sources | Applied scope |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` | Approved scope, bounded work, Korean conversation/English technical records |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md`, `.claude/agents/se.md` | Private data custody, exact-byte review, fixed safe errors |
| 2 | `docs/index.md` Current Decision Entry; `REQ-20261011-ATS-007`; `WI-20261024-ATS-004-evidence-pack.md` | Current local-only approval and honest native gaps |

Injection rule source: `.claude/config/context-injection-rules.json`; assignee SE; approved local security/transport preparation. The MA-only saved-input pointer and actual private files were never read by SE.

## Evidence Pointers

Candidate directory: `build/WI20261024-005-audio-wiring/`.

| File | Evidence |
|---|---|
| `common.py` | Fixed identities/day, genuine record-byte bindings, existing-login contract, cleanup and MP3 proof, safe HOLD projection |
| `capture.ps1` | Reused current-user ACL helpers, Korean UI, real Save event, UTF-8 stdin BaseStream protocol |
| `local.py` | Local readiness/capture/build; explicit future execute only; deterministic serialization, strict SSH, bounded process I/O, exclusive private MP3 receipt |
| `remote.py` | Verified frozen staging, public/private modes, actual root child pipe, at-most-one start, supervisor stop/ExecStopPost ownership, read-only restore verification, exact export |
| `test_wiring.py`, `test_capture.ps1` | Seven Python tests and actual event/private-storage regression without visible UI |
| `README.md` | Exact MA Prepare/copy/ready/Capture/Verify commands, future external record schema and remaining gates |

Candidate manifest SHA-256: `3425c08551ad168d99854a8150c3cc67fce77738248dc043173f48e49049c441`.

Bundle SHA-256: `eae634975cbcbbed7026830e34e259608fc8220bca081eac7906f3e22b88b47d`, computed as SHA256 of canonical sorted compact JSON member-name/hash map. Seven members plus manifest; test output is recorded here rather than adding an unmanifested candidate file.

Frozen audio manifest SHA-256: `cd32ca3fb64e84b324d95c1172041395d6413c7648be4995cec198835489ff92`. Its 68 members are checked before importing the pure contracts and when building/staging. No rebuild was run. WI014 was read as the ACL/UX reference and not edited.

Final independent receipt: `build/WI20261024-006-audio-wiring-review/review-receipt.json`, SHA-256 `24c52c605055f7f9c99219a1b8907d9fd9c6b5adfcf8635614653c285a990089`; verdict `ALLOW_LOCAL_CAPTURE_AND_PREPARATION_ONLY`, `native_authorized=false`. SE verified the published receipt bytes and matching candidate pin. PG additionally verified source643/JAR/WAV/audio68/WI0144 preservation. This does not issue `ALLOW_EXACT_AUDIO_CANDIDATE`.

## Commands & Outputs

Observed locally on Windows, 2026-10-24 KST; evidence owner SE. The following is the safe captured console result of the final test invocation (no real/private values). No separate test log exists inside the exact candidate.

```powershell
& "$env:LOCALAPPDATA/Python/pythoncore-3.14-64/python.exe" -B -m unittest discover -s build/WI20261024-005-audio-wiring -p test_wiring.py -v
& powershell.exe -NoProfile -NonInteractive -STA -ExecutionPolicy Bypass -File build/WI20261024-005-audio-wiring/test_capture.ps1
```

```text
test_bounded_child_no_stderr_reflection ... ok
test_capture_check_requires_valid_preserved_actors_without_authority ... ok
test_cleanup_and_hold_projection ... ok
test_external_receipts_bound_and_not_created ... ok
test_id1_existing_contract_and_actor_merge ... ok
test_payload_determinism_and_current_material_binding ... ok
test_strict_ssh_and_exact_pipe_child ... ok
Ran 7 tests in 0.641s
OK
SYNTHETIC_EXACT_SAVE_INVALID_CORRECTION_UNICODE_PRIVATE_STORAGE_PASS
```

The Save regression invokes the actual Button event without displaying a form, rejects a 257-character password while retaining entries, then stores a short Unicode password with leading/trailing spaces via the actual Python process and private synthetic file. It verifies all reused passwords and saved-input bytes remain unchanged, actor file ACL, success clear and no reflected input. Synthetic test directories are retained; no real credentials were accessed. Pipe tests inject the fixed child call and separately exercise bounded local Python child I/O; they do not claim native Linux FIFO observation.

Initial native PowerShell 5.1 Save-path testing found that `ProcessStartInfo.StandardInputEncoding` is unavailable. The repaired callback explicitly encodes UTF-8 bytes to stdin BaseStream, and the final actual Save path passed. This was a local compatibility finding, not a diagnosis of any historical real-input failure.

PG pre-freeze findings were resolved narrowly: bind current serialized payload, restore all prior strict SSH options, allow app UID access to public code modes, require nonempty provenance bytes, and preserve observed API/cleanup on transfer failures. Final MA/PG review also required `check` to validate the actual saved actor file, frozen frame and exact saved-account merge; `capture_check` now does so without requiring native authority. The added normal/missing/changed-actor regression passed. Capture/Save code was unchanged by that final correction; its actual Save result above is reused. No frozen runtime guard/function was modified.

## Risks / Rollback

Current authority is local preparation/capture only. The README's future CLI cannot run without explicit native flag and genuine current records. Opaque provenance bytes plus pins do not independently prove their human meaning: MA/PG must verify genuine user decision, exact native PG permission, current target and backup/restore coverage of current fixture data. Historical local PG/backup evidence is insufficient by itself. Missing evidence means HOLD, never manufactured PASS.

Native stage failure can retain control/run/log/supervisor/authority artifacts without starting DB/app. Preserve them and distinguish NOT_STARTED from possible start; no automatic deletion/re-stage/retry. After possible start or lost response, preserve claims/evidence, stop further mutations and confirm cleanup independently. The original supervisor/ExecStopPost remains responsible for restoration; the wrapper never invokes restore outside its required cgroup. Transfer failures retain observed API/cleanup milestones but do not claim listening or browser-player UX.

Local rollback is to withhold this new candidate/window; no product/source/JAR/frozen-runtime reversal is needed. No server, service, DB, login, native audio, transfer, payment, actual UI window or private-input operation was performed by SE.

## Follow-up

Exact frozen candidate and focused results were immediately sent to PG WI-20261024-ATS-006; its final local allowance is recorded above. MA alone may now run the README local capture workflow once within the existing user approval. Native execution, current backup scope, fresh target approval and human listening remain separate gates. The user does not need to re-enter credentials merely because future server receipts expire.
