---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-047-handoff.md
    reason: Independent review authority and boundaries
  - path: WI-20261011-ATS-046-evidence-pack.md
    reason: Frozen HTTPS-only recovery tool and owner evidence
  - path: ../user/REQ-20261011-ATS-006.md
    reason: New explicit original-HTTPS-only recovery approval
---

# Evidence Pack: WI-20261011-ATS-047

> Purpose: Gate the exact original-quarantine recovery tool and two MA recipes independently.

## 1. Verdict

**TL;DR: CONDITIONAL_ALLOW for MA's exact L1 staging once and L2 recovery once, solely to recover original quarantine HTTPS under the existing recovery approval.** F01 is resolved in the pinned candidate; no remaining blocking defect was found in this bounded review. This is neither app/MySQL restart approval nor success evidence for the failed web trial.

PG performed local mocked tests and read-only source/pin checks only. No SSH, agent, network, browser, service, DB, private environment or native operation was performed. All writes are confined to WI047 helpers, receipts and two reports; SE and historical bytes remain untouched.

## 2. Exact Gate

The [freeze manifest](../../build/WI046-quarantine-recovery/freeze.json) contains nine files; ten objects include freeze itself. The ordered bundle is UTF-8 filename + NUL + exact bytes in manifest order. Full test/log/receipt pins are transitively bound by the manifest, independently checked, and not replaced by chat abbreviations.

| Object | SHA256 |
|---|---|
| recover.py (12256 bytes) | `7ab8be7b2b1b94df4eeabbc8be5c03620d0ad219749ce571014d587a92ad1d94` |
| freeze.json | `90b446e6aa4af89ac7aee184d7b0dc74b258a7d92427e3017d169bbb31bf418b` |
| Ordered nine-file bundle | `8a249e7b2628bc7c8bc66628e90e79b820713cb4b7d26909820de4ff1b7e30e4` |
| README.md | `7449844a217c4f29bffa6661d53709688b5d9ff65b2e91825e0fb34d45f965cc` |
| recipe-pins.json | `1227b5e924e97d70635ff68f0ff355f638ea65863e69e09fc1324966af7f537d` |
| L1, PowerShell, 2528 normalized bytes | `0b5b3e6cb031513eecc99165640a8b9f518cbf4ac56f20d8a9e5a95109818ef7` |
| L2, PowerShell, 2307 normalized bytes | `0b32438b5d104f019b4fac9bdb5efa2fe79caf283a25be613d497624efc9129a` |
| Imported WI042 runtime.py | `ad3e8a20ddb17fd1e3e4878ac2096bfa74a63561b9b1e8572b6bc2a7cda7a7b6` |
| Imported WI042 web_session.py | `3092ae8c5598d9b97e1dedc6e0bb74ca9c0d3a8989493465114229d45727257a` |
| Imported WI042 web_policy.py | `1a22b3fe28e13ea986fc098a123d4a5a7a3d50734771eccb3288bcd15a6df9cc` |
| Exact Nginx overlay template | `d4c5f241a49aceddf2651b570024b48d7c5eef7fc01226c75df77a359ef98df5` |
| Exact MySQL overlay template | `9bff9f2e2fdd7f8ff09718b1117a4546d75a06504a727f63789e8e95f2ea7aa5` |

Recipe normalization: UTF-8/LF, exactly one final newline, fences excluded. L1's reviewed payload placeholder is filled only with the locally pin-checked recover.py bytes. No alternate installer, edited command block or helper import is covered.

## 3. Findings and Safety Rationale

**PG047-F01, previously P1, resolved for these exact bytes.** Draft `f2fea7ea87f69074b7dfee12cacb8de3b28b255d8db1eb56d379b0ff7eb79a46` left Nginx running after receipt-write/fsync failure. Its [failure receipt](../../build/WI047-quarantine-review/draft-receipt.json) and [finding](../../build/WI047-quarantine-review/draft-findings.md) are unchanged. The final code invokes the same guarded safety stop after receipt failure, including an already VERIFIED start. It attempts all safety checks even if stop fails and does not retry a prior stop.

| Source pointer in recover.py | Reviewed boundary |
|---|---|
| `44` / `62` | Root owner/group, type/mode/link/ancestry and exact public hashes; held verified bytes execute without reopening a mutable import. |
| `86` | Same directory FD for both rename names, `RENAME_NOREPLACE`, directory fsync; no cross-device move/delete fallback. |
| `109` | App/MySQL masked, PID0 and stopped; supervisor failed/PID0, no jobs/app processes, listener gate and certbot state. |
| `179` / `191` / `198` | Exclusive fresh recovery directory/claim; both durable verified copies before either exact overlay is renamed outside `.conf` discovery. Collisions and partials remain preserved. |
| `199` / `200` / `209` | Daemon reload precedes ordered original service/TLS comparison and stopped/no-worker recheck, then original-config nginx syntax test, all before start. |
| `211` / `218` | At most one original Nginx start; original process/config/cgroup, listeners and relay ownership checked afterward. |
| `135` / `225` | Bounded fail-closed stop on ambiguous start, verification failure or receipt failure; provisional disk receipt never attests its own later fsync. |
| `241` | Exact root entry/path/arguments, 120-second primary alarm and interrupt handling. |

`listener_gate(..., False)` intentionally permits original quarantine ports; `process_absent()` intentionally excludes Nginx. These are not defects. Secret-reading `runtime.final_baseline()` is not called. Original baseline/failed-result private records are read internally as approved recovery evidence, never emitted; actual environment secrets, DB contents, certificate contents and raw logs are not read.

## 4. Independent Tests

Environment/owner/date: local Windows, PG, 2026-10-11. All native effects mocked. The actual `run()` and rename argument construction execute; service projections and filesystem durability are not native proofs. Owner tests were inspected before PG execution.

| Command/check | Actual PG result |
|---|---|
| `python -I -B build/WI047-quarantine-review/test_recovery_review.py` | 7/7 PASS, 0 failures/errors/skips, 0.021s |
| `python -I -B build/WI047-quarantine-review/test_final_boundaries.py` | 6/6 PASS, 0 failures/errors/skips, 0.019s |
| `python -I -B -m unittest discover -s build/WI046-quarantine-recovery -p 'test_*.py' -v` | 23/23 PASS, 0 failures/errors/skips, 0.091s |
| `python -I -B build/WI047-quarantine-review/verify_final.py` | Nine files, ordered bundle, separate freeze, both recipe pins, seven selected preservation pins and two historical PG failure records PASS |
| PowerShell `Language.Parser.ParseInput` of both README bodies | 2/2 syntax PASS; neither recipe executed |
| Compile only: two candidate Python files and two embedded Python bodies | 4/4 PASS; no embedded native execution |

Coverage includes collisions, wrong bytes, symlink/link count/owner/mode/ancestry/inode replacement, distinct-device modeling, no-replace rejection, partial copy/detach, changed service command order, running-state/jobs rejection, failed syntax/start/postcheck, receipt failure, failed safety stop and continuation. Successful disk receipts remain PENDING_DURABILITY even when stdout can establish success. No percentage coverage or exhaustive host-failure guarantee is claimed.

The unchanged seven-test script still prints its historical `DRAFT_ONLY` scope label. Its final run explicitly prints the final candidate hash; this report and the [new final receipt](../../build/WI047-quarantine-review/final-receipt.json) bind that run to freeze. Prior test files and failure receipts were not rewritten to hide the red result or the initial Windows harness error.

## 5. MA Conditions

1. Use only exact L1 then exact L2, each once, under the user's original-HTTPS-only recovery approval. Immediately confirm expected host/boot, live recovery console, current management-only provider/host restrictions, healthy pinned SSH and no competing operator/imminent certificate renewal. MA's 17:14 metadata and 17:25:16 identity observations are separate reports, not PG target checks or permanent authority.
2. Bind `$WI046SshArgs` to the currently verified strict host-pinned transport, no forwarding, no extra remote command or disabled host checks. Both blocks must resolve the actual Windows System32 OpenSSH executable; PowerShell 7 is required. Capture/redact at the transport boundary as documented. Preserve raw transport errors privately if necessary; do not publish them.
3. Require exact source/helper/overlay pins, safe ancestry/metadata, app/MySQL masks and PID0, original Nginx stopped/no workers, failed supervisor/PID0, clear jobs and intact original TLS bytes. New staging/recovery/retained targets must be absent. Any unexpected state is HOLD, never permission repair or drift tolerance.
4. L1 must return its exact single success marker and exit0. Partial staging or ambiguity is HOLD; retain it. L2 consumes the fresh record boundary and performs no app/MySQL start/unmask, reset, data change, firewall/domain/certificate change or old-trial mutation. Do not remove a claim or rerun to recover from failure.
5. Accept recovery only from bounded stdout with exit0, `status=RECOVERED_ORIGINAL_HTTPS`, `receipt_durability=PASS` and `trial_status=HOLD`. The disk result is provisional; a missing/partial/PENDING_DURABILITY receipt or transport loss is not success. A HOLD or `safety_stop=FAILED` requires MA read-only state inspection through the recovery path, not another tool invocation or ad hoc repair.
6. After the recovery outcome, MA records current app/MySQL masks/PID0 and original quarantine service/response observations without app/API test continuation, then removes the approved temporary key, restores agent Stopped/Disabled and logs out recovery access. WI045 integrates actual outcomes separately. PG has not claimed these operations occurred.

## 6. Limits, Rollback and Handoff

No broad application/JAR/repository audit was repeated. Preservation verification is the seven selected pins plus the two retained PG failure records. Real Linux rename/fsync support and success remain target-dependent; unavailable support fails closed. The 120-second primary alarm and finite command timeouts do not guarantee cleanup under SIGKILL, host loss or blocked kernel I/O. No packages are required or approved.

Before execution, rollback is withholding L1/L2. After a partial or consumed claim, retain durable copies, same-directory originals and all receipts; no deletion, overwrite or automatic overlay reinstallation is authorized. A failed stop is honestly FAILED, not guaranteed termination.

Historical WI042 remains HOLD/READ_ONLY_CHECKS/POST_STATE_CHANGED with cleanup FAILED. EXDEV is strongly supported by MA's distinct-device observation and old rename path, but raw errno was not captured. POST_STATE_CHANGED remains NOT_DETERMINED; no unchanged-business-data, full HTTP/browser success or new trial acceptance follows from recovery.

**Next: MA current gates -> one original-HTTPS-only recovery -> WI045 integration and access cleanup evidence.**

## 7. References

Retained Tier0: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary. Retained Tier1: PG role and security-policy. Tier2: approved REQ006 recovery amendment, WI046/047 handoffs, actual failure snapshot, WI046 completion, frozen WI042 helpers and original TLS pins. Skills applied: `test` for the authorized Python/mock scope and `create-wi-evidence-pack`. No subdelegation or memory updates.

## Related Documents

- [WI047 Handoff](WI-20261011-ATS-047-handoff.md): Review scope.
- [WI046 Evidence](WI-20261011-ATS-046-evidence-pack.md): Owner completion.
- [Exact README](../../build/WI046-quarantine-recovery/README.md): Two conditional MA recipes.
- [Freeze](../../build/WI046-quarantine-recovery/freeze.json): Full reviewed identities.
- [Final Test Receipt](../../build/WI047-quarantine-review/final-receipt.json): Independent counts and pins.
- [WI047 Summary](../user/WI-20261011-ATS-047-summary.md): Concise verdict.
- [REQ006](../user/REQ-20261011-ATS-006.md): Original HTTPS-only approval.
