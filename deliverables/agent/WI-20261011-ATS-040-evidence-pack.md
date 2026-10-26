---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-040-handoff.md
    reason: Independent review scope and write boundary
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved repair amendment and fresh single-attempt scope
  - path: WI-20261011-ATS-039-evidence-pack.md
    reason: Frozen candidate and implementation evidence
  - path: WI-20261011-ATS-036-evidence-pack.md
    reason: Unchanged source, public artifact and finite GET contract
  - path: WI-20261011-ATS-038-evidence-pack.md
    reason: Preserved consumed attempt and measured service omissions
---

# Evidence Pack: WI-20261011-ATS-040

> Purpose: Independently gate the exact WI039 repair and recipe for one management-only trial.

**TL;DR: CONDITIONAL ALLOW**, exclusively for the frozen identity below and the MA conditions in this report. No new blocking defect found in the bounded delta. Local tests PASS; WI039 native, browser and Internet proof remain NOT_RUN by PG. This is a new byte-specific review, not reuse of WI037 or permission to retry a consumed attempt.

## Scope and Findings

- [x] Review optional Exec omissions, strict scalar parsing, masked versus loaded MySQL stages, fresh namespace and exact recipe.
- [x] Independently verify 26 frozen files, both ordered hashes, 11 recipe bodies, 42 historical pins and external QA helper.
- [x] Independently rerun 95 owner mocks, 9 PG tests and 8 PowerShell synthetic assertions.
- [x] Exercise post-unmask failure, separate ExecStopPost cleanup, simultaneous failures, final baseline continuation and sticky HOLD.
- [x] Preserve implementation, old claims, bundles and evidence; write only WI040 review helpers and two reports.

No newly confirmed blocking finding. The following retained limitations constrain the ALLOW:

| Item | Assessment and required handling |
|---|---|
| Loaded MySQL rendering | MA measured the installed vendor unit, not loaded systemctl ExecStart output after unmask. The exact loaded gate remains mandatory; mismatch is HOLD before start, then cleanup. No dynamic tolerance or retry. |
| Existing P2 intermediate WEB_RESTORE | `web_session.py:413` can report HOLD/cleanup FAILED while the supervisor is deactivating and final result is not yet present. README lines54 and391 explicitly permit bounded read-only S5 observations. Existing recipe permits this without code change. Preserve every receipt; distinguish intermediate status from authoritative result.json. Never discard or override a true final failure. |
| Native restoration/lifetime | Mocked continuation does not prove target timers or restoration. Independent service caps and ExecStopPost remain; host/kernel loss or exhausted cleanup budget can prevent completion. Missing/failing final result stays HOLD. |

The consumed WI035 result remains HOLD/PREPARE/WEB_SERVICE, app NOT_RUN, cleanup PASS with exactly **6 PASS / 9 NOT_APPLICABLE**, not the earlier incorrect 7/8 aggregate. WI026's older cleanup cause remains NOT_DETERMINED.

## Evidence Pointers

| Pointer | Review evidence |
|---|---|
| [web_session.py](../../build/WI039-restricted-web-check/web_session.py), lines66,87,96 | Only three optional empty Exec properties; ExecStart exception confined to masked MySQL with empty Type. Exact five-field stopped state, mask link and baseline. Required scalars, unknown/malformed/duplicate output stay fail-closed. |
| Same file, lines114,195 | Prepare uses masked baseline. Post-unmask gate requires loaded/inactive/dead/PID0, exact vendor fragment/command, overlay and unchanged lifetime/kill/restart/binding controls. |
| [runtime.py](../../build/WI039-restricted-web-check/runtime.py), lines919,940,1019 | Baseline prepared before unmask; gate before MySQL start. Failure records safe HOLD and closes receiver; supervisor owns separate restoration. |
| [web_session.py](../../build/WI039-restricted-web-check/web_session.py), line279; runtime.py lines798,824,852 | Existing baseline makes MySQL stop/mask applicable after attempted unmask. All remaining cleanup and baseline checks continue; failed cleanup cannot become PASS. |
| [semantic-delta.patch](../../build/WI039-restricted-web-check/semantic-delta.patch); [test_service_shape.py](../../build/WI039-restricted-web-check/test_service_shape.py) | Runtime, policy, units, PC checker and public contract remain namespace-only or byte-identical outside the specific service repair. |
| [README](../../build/WI039-restricted-web-check/README.md), lines98,184,246,336,383,402,452 | Reviewed all eleven exact blocks, current vendor/masked preflight, retained WI035 state, fresh approval and one dispatch, HTTP/browser limits, finish and postcheck. |
| [PG service tests](../../build/WI040-restricted-web-review/test_service_review.py); [PG failure tests](../../build/WI040-restricted-web-review/test_failure_review.py) | Adversarial parsing and genuine run/restore orchestration with mocked effects. Three primary failure paths, fourteen single cleanup failures and four simultaneous failures retain final checks. |

## Exact Identity

All SHA256 values are exact bytes except recipe bodies, normalized as specified below. Full individual 26-file pins are bound by the pinned freeze; ten-file runtime order and 26-file insertion order use filename UTF-8 + NUL + exact bytes.

| Object | SHA256 |
|---|---|
| Runtime bundle | `fb643ff04e5c0f1e90f1b110eafa312c311a3d560a822769ff43f5ed432d379b` |
| Review set | `1a19015eb8e890b02d6973331dfb2ef7fd7458821694e929ba425cd1ab3c1572` |
| freeze.json | `e29624f41ccfd51f43005721d05e1e619bf38cac459f5281707c9dd9530f772e` |
| runtime.py, 51898 bytes | `07392a980ab6a029a73e27d0ce7e1c00d8c296964d47adc5344020029d57877f` |
| web_session.py, 24333 bytes | `d2aba516a7e9850ebfe18e5e48f1adcc22fc103845a1372e0e05f244ff8dc2cc` |
| README.md, 33999 bytes | `580e38a29533e0c4c366304d72b7fc725ff4bf0c25457e8c64d4ed2c03e85d51` |
| recipe-pins.json | `a8e2f7b5d164b7372e277117515a3a4000ca83f428badc5ecb93a13b3ad90aef` |
| External QA036 verify-identity.ps1 | `6fcd7568785a8e25d9391dce09f5a698f7adda13d9ce9fcdbe66c8f679a4ad6f` |
| QA036 allowlist.json | `09df13ad175f4a359e6cbc9d9b68d0f6bf8e3c27eb065ca2e8e6de80e757bf12` |
| Existing JAR, 70306351 bytes | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |

Recipe bodies exclude fences, use UTF-8/LF and one final newline. No alternate installer or body edits are allowed.

| Block | SHA256 |
|---|---|
| L1 | `0f0b72e5167d6695f10236a7149cab21a26975d2d7a2f607b2a8b9dd695d131f` |
| S1 | `4c19e062891da8be9d67eb1814328c9fc5923be6d040bcfc8d1c8543c17bf9f8` |
| S2 | `deca7968acbb493b96326ecea9d342893c170ae5500dc1259f4167be4d39d97e` |
| S3 | `3a7b32ad35c3647aa19a105293ad45ca299f08fecf5a859199fe8e72f9f072da` |
| S4 | `f8d1094409adad33e00e4f8b82d7bfd8c9748d07a73cb48df03ddedbf0b9c7bd` |
| S5 | `56b284aed05ac95533821b950a88b469771b146775b48a781aa9c84c8b6c4e30` |
| L2 | `3b59bb72a242af58a36cb092d4f8c2198cea74e2049a00769a48ed486c2b8832` |
| L3 | `b21df3b24b5c945f52d24420fb4dc706ac6a9c4797cbfb5939f31b1f40c31fdd` |
| S6 | `e75552c29970167004b73b1b2983db0b6d06490831f22212988905640b26dba3` |
| S7 | `11c679942b303c6b689e16f8f1045a576db4850c575c955797be5019ca09f7a9` |
| L4 | `6642087b5acc8b853ccff287740624332c697b777a9603a39dd20c6588b7ee11` |

## Independent Test Evidence

Local Windows, 2026-10-11; PG-owned execution. `test` skill applied to operator-only unittest/PowerShell scope; no Gradle/npm build or broad source audit. [Test receipt](../../build/WI040-restricted-web-review/test-receipt.json) records commands, helper pins, counts and limits.

| Command | Result |
|---|---|
| `python -I -B build/WI040-restricted-web-review/verify_snapshot.py` | PASS:26 files,11 bodies,42 preserved pins, both ordered digests; repeated before final report |
| `python -I -B build/WI040-restricted-web-review/run_regressions.py --pg` |9/9 PASS;0 failed/skipped;0.134s |
| `python -I -B build/WI040-restricted-web-review/run_regressions.py` |95/95 PASS;0 failed/skipped;0.416s |
| `& ./build/WI040-restricted-web-review/test_recipe.ps1` |8/8 synthetic assertions PASS;4 PowerShell bodies parse; network NOT_RUN |

Python audit guards prohibit actual subprocess/socket operations and filesystem mutations; all run/restore effects are mocks. The post-unmask suite retains real cleanup/final_baseline iteration and checks all36 rows, redacted failures, no skipped MySQL mask and unchanged startup outcome. Preliminary6/6 parser results are superseded by the frozen9-test run. Coverage is NOT_MEASURED. Seven shell blocks were reviewed/hashed, not executed or native syntax-tested. No running sessions remain.

## MA Conditions and Limits

1. Recheck freeze, bundle, recipe bodies and external QA helper before use; L1 must confirm current source/JAR/frontend identity through that helper and the143-file manifest. Changed bytes invalidate this verdict. Preserve all42 historical pins and WI038 receipt.
2. Bind the current approved SSH destination/options; verify bare `ssh` resolves to Windows System32 OpenSSH, retain strict existing host/key checks, no forwarding, and no alternate configuration. Capture/redact transport stderr and avoid raw diagnostic disclosure. Require successful L1 identity and `UPLOAD_PUBLIC_FILES=153`. S1-S7 are the exact pinned buffers sent to `/bin/sh -s`, preserving authentic SSH_CONNECTION without logging it.
3. Before S2/S3/S4, refresh expected host/boot/peer, current management-/32 provider22/443 rules and unchanged80/ACME policy, logged-in root recovery, no competing jobs/operators, masked inactive MySQL, old attempts unchanged, fresh WI039 targets, original TLS/no-proxy/capture guards, resources and exact artifact pins. The private NAT server address is not compared to the public IP. No target state is certified by PG.
4. MA reports vendor/masked fixture checks at15:55:08KST, independent26+11 hash checks at16:04:46, and fresh provider rules plus successful root recovery login at16:07. These supersede the earlier recovery login-prompt observation but are MA evidence, not PG native results. Refresh again on delay/change; portal expiry alone is not an architecture defect, but inability to establish current firewall/recovery is HOLD. Relogin is required if fresh evidence cannot otherwise be obtained.
5. S3 requires all eleven truthful operator attestations; fresh REQ006/PG040 approval and exclusive WI039 claim only. S4 once, no retries. Loaded MySQL format remains a target gate, not an inferred PASS. Keep all time/egress/BPF/secret/jobs/read-only controls intact; no packages or new access.
6. Only OPEN/admission PASS/app VERIFIED permits L2; L2 fence/17 approved GET cases precede L3 synthetic denials and the fixed browser plan. Retained browser context is conditional: no storage inspection or pristine-profile claim; observed identity/auth/media/unexpected requests mean stop/HOLD. DOM/screenshots/dev.logs do not establish full network absence or browser API200. CSP/font blocking is expected; no populated-catalog/login/media/payment or public-release claim.
7. Finish once with S6, then no more browser/HTTP work. Existing recipe allows only bounded S5 status observations (5s, maximum60 after finish) to distinguish P2 intermediate state; preserve every receipt. Require authoritative final result and independent S7. Any real final FAILED/missing receipt/unsafe actual state remains HOLD, even if a later state looks clean. No repair, rerun, reset-failed, claim reuse or deletion.

## Risks, Rollback and Chain

Before dispatch, rollback is withholding the candidate. After dispatch, use only the reviewed one-shot finish/restore and read-only postcheck; additional recovery needs approval. App/DB bounds remain below1800s under functioning systemd/kernel, with independent caps if SSH disappears; successful restoration is not guaranteed under host failure. Restore compares original bytes/configuration, stops old workers, retains overlays, and preserves service/history evidence.

WI040 local review is complete. **Next: MA validates fresh gates, executes at most one approved WI039 attempt, then routes actual evidence to WI041.** PG has performed no remote/native/browser/secret access. REQ006 remains open for native evidence closure.

## Reference Documents

Retained injection: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary; PG persona and security policy; quality/access policies; approved REQ006/WI040 handoff and WI035-039 dated evidence. Skills: `test` and `create-wi-evidence-pack`. Current context and exact owner completion supersede older pending summaries; no unrelated architecture audit was added.

## Related Documents

- [WI040 handoff](WI-20261011-ATS-040-handoff.md): approved scope.
- [REQ006](../user/REQ-20261011-ATS-006.md): approved repair and one-attempt boundary.
- [WI039 completion](WI-20261011-ATS-039-evidence-pack.md): owner evidence.
- [WI038 receipt](WI-20261011-ATS-038-native-receipt.json): preserved consumed result.
- [WI037 review](WI-20261011-ATS-037-evidence-pack.md): unchanged controls and P2 context.
- [WI040 summary](../user/WI-20261011-ATS-040-summary.md): concise user-facing decision.
