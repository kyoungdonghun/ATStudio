---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-037-handoff.md
    reason: Independent review scope and write boundary
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved single restricted web trial
  - path: WI-20261011-ATS-035-evidence-pack.md
    reason: Frozen implementation and execution recipe
  - path: WI-20261011-ATS-036-evidence-pack.md
    reason: Source identity and finite read-only contract
---

# Evidence Pack: WI-20261011-ATS-037

> Purpose: Gate the exact WI035 restricted-web candidate and recipe without performing native operations.

## Verdict

**TL;DR: CONDITIONAL ALLOW for MA's one approved REQ006 attempt, using only the exact frozen recipe and conditions below. No confirmed execution-blocking defect. One nonblocking P2 remains. Native and browser NOT_RUN by PG.**

PG's local review is complete. This is an artifact gate, not a native approval file, current-host attestation, restoration result, or production release. Any byte drift or unmet execution condition means HOLD before execution. No implementation, historical artifact, source, index or native state was changed.

## Finding

**WI037-P2-01: intermediate restoration status is prematurely labelled failed.**
At `build/WI035-restricted-web-check/web_session.py:378`, a supervisor in `deactivating` with no final result returns exit2, `HOLD`, `cleanup=FAILED`, `WEB_RESTORE`, even while restoration is still in progress. Independent `test_status_during_restore_current_behavior` reproduces this with mocked I/O; it is not a native failure observation.

This does not grant access, bypass cleanup or retry startup. Preserve every intermediate receipt and exit code, classify this specific state as an intermediate observation, and continue only the existing bounded S5 read-only observation after finish: at most 60 checks at 5-second intervals. Require the authoritative final `result.json` receipt and independent S7 postcheck. Actual failed diagnostic rows, final HOLD/FAILED, browser/API failure, or a missing final receipt remain HOLD; later clean state cannot erase them.

**The existing recipe permits this condition without code changes:** README lines 332-340 permit bounded status observation, and lines 395-408 permit observing completion after the one-use finish marker. Its HOLD rule prohibits another start and requires preservation; it does not prohibit those bounded read-only observations. No extra invocation, repair or ignored final failure is authorized. Route any future status-display correction through MA separately.

## Exact Identity

Final independent local pin check: **2026-10-11 15:22:57 KST**, Windows workspace. All 23 frozen entries, 11 executable blocks, 17 preservation objects and the external QA helper matched.

| Object | SHA256 |
|---|---|
| Ten-file runtime bundle | `35ff347e1d0e2b0cad930ce1431b073d08fea7d2a48631779360766424760ce5` |
| 23-file review set | `feb373fe3308a8dd06da2bb7e149bbdc255f527750744a987830fccf8b1de063` |
| runtime.py | `8ca9012729f3f89a74dac8e325321aab3f5418e727efd827a394e53151eebffb` |
| web_session.py | `b7b70dece75a1ceebe9126ee659a68b75129c7cf07a6351294f86d79e0b291b7` |
| README.md | `91801af1f56c4f67d5be1c11e28ec8b04f1bb91a0db5d4d1d20015fdc6ccd74a` |
| freeze.json | `66843a26f4681a11cd48e9bde461170a29d4bd807a186822495b112d6a160d1f` |
| recipe-pins.json | `db8d31c6b767e0fd0e286f25fd2651caf0398455ce1819562168de026043daca` |
| WI036 verify-identity.ps1, 7270 bytes | `6fcd7568785a8e25d9391dce09f5a698f7adda13d9ce9fcdbe66c8f679a4ad6f` |
| WI036 allowlist.json | `09df13ad175f4a359e6cbc9d9b68d0f6bf8e3c27eb065ca2e8e6de80e757bf12` |
| Existing JAR, 70306351 bytes | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |

[Freeze](../../build/WI035-restricted-web-check/freeze.json) contains all 23 exact file sizes/hashes; its ordered filename + NUL + bytes aggregates were independently recomputed. [Recipe pins](../../build/WI035-restricted-web-check/recipe-pins.json) binds README and every executable body, normalized to UTF-8/LF with one final LF:

| Block | SHA256 |
|---|---|
| L1 | `bb1dbde74e68be4b63ecd4318f9e91bdd95c113e122e6820192c9f5a4aa28e62` |
| S1 | `84726bcc43413a3f0f4e53e61ccfa88c0fefafd0a5f28b47a316ed5277e9bcae` |
| S2 | `a64dabf913426cb531d68e614545c96b76f073571997239f1aea8615b55ae134` |
| S3 | `37b3cf14c1504c982d81ad6431234b75dcc19ad952da231b65dcd9261faeee62` |
| S4 | `7546a03f42f8bc052d8746885a077d9088f4f250b1e19a95a6899def36327c73` |
| S5 | `5bf4344d041b0b685b294ccad4c73f4b8112b3dbc929a87c3af4d19f5ea072ea` |
| L2 | `d71a7e7d05d79fbe05a2882254c556240161643d70664a768b637769b2086270` |
| L3 | `ca5e783105d99d2c2760fd64a761a6a9129c2f8e7c1c1c1578c37de8f1f8e3f8` |
| S6 | `b9c5399336b8a7a337da7e711e6d6cbeadd7a86bb2c792c25bf04223f1250cb2` |
| S7 | `237575b0c9d53cde8b16b5cb26a8cf644f380ef986feda532dc23da41e7f233c` |
| L4 | `3fe71d9e85fa98373b10d7c48293ebf13d7beb3cbac87463c008b179064f4df0` |

## MA Execution Conditions

1. Recheck these bytes immediately before use, including the external WI036 helper before L1 invokes it. Re-run that pinned identity check for current source/JAR/frontend; QA's 641 backend/336 frontend input evidence is not an evergreen host observation. PG independently matched all 143 frontend file bytes and the 17 exact GET variants.
2. Bind L1's existing `$WI035SshArgs` to the approved current strict host-pinned SSH transport. Verify bare `ssh` resolves to Windows System32 OpenSSH, with no alias/function substitution. Keep host checks, no-forwarding and existing key controls. Capture/redact stderr at the transport boundary; accept only the expected fixed upload receipt and bounded public identity JSON. Never publish raw transport errors or private connection fields.
3. Hash-check and send S1-S7 unchanged as UTF-8/LF buffers through that pinned transport to `/bin/sh -s`, preserving its authentic SSH_CONNECTION. No alternate installer, shell interpolation of source, invented address or old command reuse. S3 is MA's truthful fresh approval act; S4 is once only. Ambiguity/collision retains partials and prohibits retry.
4. Immediately before S3 refresh target/boot/JAR/TLS, fresh paths/claim, stopped/masked services, listener/relay gates, exclusive window and usable root recovery. Confirm the current management peer equals the original Nginx allow and existing provider22/443 management-only policy is unchanged; preserve HTTP80 ACME and certificate/renewal controls. Portal expiry alone is not an architecture blocker. Use independently reliable current evidence; if required provider-policy confirmation is unavailable, re-login before S3 or HOLD. A connected noVNC transport alone is not proof of a usable root prompt.
5. Observe OPEN/admission PASS/app VERIFIED before L2/L3/browser. Native unit/Nginx checks, actual BPF/on-host controls and disabled integrations must pass in this attempt. Configured supervisor1440s/stop180s, app and DB1500s with bounded start/stop, view1380s and admission240s limits remain; no local-client lifetime dependency or restart/retry authorization is introduced.
6. Browser use is the explicitly accepted retained-context observation only. Do not inspect/clear storage or claim pristine identity. Stop/HOLD on retained identity, credential rejection or unexpected auth/member/batch/media/integration behavior. CSP/fence and PC HTTP assertions precede observation; DOM/screenshots/dev.logs do not prove every browser response or absence of arbitrary requests. Expected external font blocking is not an API failure; non-management vantage remains unverified.
7. Finish once; thereafter only bounded S5 read-only observation and S7 postcheck. Preserve all receipts, including P2 intermediates. Require final successful diagnostics and actual stopped/masked app/DB, no residual app/probe listeners/jobs, original Nginx configuration/process and HTTPS restoration. MA also closes scoped browser work and temporary SSH access. Any true failure/missing result is HOLD; no repair, deletion or new start is authorized by this review.

MA's 15:05:31 HTTPS quarantine200/no-redirect/TLS observation and current SSH peer/private-NAT identity correction are attributed context, not PG-native measurements. The earlier public-address comparison was the wrong comparison; it does not establish a host-identity failure. The 14:32 provider view is historical, not a fresh gate by itself.

## Tests and Evidence Pointers

| Independent command | Result |
|---|---|
| `python -I -B build/WI037-restricted-web-review/run_regressions.py` | 85/85 PASS, 0 skipped, 0.311s; audit hook blocks real subprocess/socket/writes |
| `python -I -B build/WI037-restricted-web-review/test_review.py` | 11/11 PASS, 0 skipped, 0.167s; includes 13 web-step fault subcases and P2 characterization |
| `& ./build/WI037-restricted-web-review/test_recipe.ps1` | 8/8 synthetic payload assertions; 4 PowerShell blocks parse; no recipe execution |

[Test receipt](../../build/WI037-restricted-web-review/test-receipt.json) records commands, helper pins, final pin observation and the corrected review-fixture error from the first run. Tests were executed before the checkpoint; this resume only finalized records and rechecked pins. No test result was inferred from owner claims, and no coverage percentage is asserted.

- `web_policy.py:76,94`: exact request oracle/rendering, six API paths, no broad proxy, management/Host/SNI/method/header/CSP constraints; actual Linux parsing remains native-only.
- `web_session.py:85,117,167,190,244`: snapshots, pinned public copy, effective DB bounds, supervised interval, stop/no-worker/restore checks; no reload-only restoration.
- `runtime.py:277,284,798,824,871`: bundle/approval, independent cleanup/baseline and retained containment/secret/DB/GET guards. `web_session.py:300` bounds public receipts.
- `README.md:60,144,217,277,324,338,352,367,395,410,500`: all eleven reviewed executable blocks. L1's external helper was also read and pinned.

## Scope, Limits and Follow-up

- [x] Ordered Tier0 context and security/persona retained; test and create-wi-evidence-pack skills applied; actual WI037 handoff exists.
- [x] Frozen identity, finite read contract, candidate delta, recipe, negative/fault tests, preservation and caller conditions reviewed.
- [x] Two-set verdict and test receipt saved only within WI037 ownership.
- [ ] Native effective configuration, elapsed cap, worker termination, restoration and browser acceptance: MA-owned, NOT_RUN by PG.

Reference context: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary; security-policy and PG persona; REQ006, WI035/036 handoffs/completions, WI034 historical evidence and docs/index current entry. Historical WI034 BOUNDED_PASS is not reused as this trial's acceptance; WI026 cleanup cause remains unknown.

Residual risk: functioning systemd/kernel and recovery availability are required; bounded best-effort cleanup can fail or lack a receipt. No absolute restoration, Internet/IPv6 delivery, pristine-browser, complete traffic visibility, populated media/login/payment, load or production acceptance is claimed. Before execution, rollback is withholding the candidate. During the one approved attempt, use only the reviewed finish/restoration sequence and preserve history.

**Next chain:** MA's conditional one-attempt trial and restoration, then WI038 evidence integration. PG performed no native action or subdelegation; REQ006 remains active.

## Related Documents

- [WI037 summary](../user/WI-20261011-ATS-037-summary.md): concise gate.
- [WI037 handoff](WI-20261011-ATS-037-handoff.md): authorization boundary.
- [WI035 README](../../build/WI035-restricted-web-check/README.md): exact recipe, unchanged.
- [WI036 evidence](WI-20261011-ATS-036-evidence-pack.md): source/read contract and browser limitations.
