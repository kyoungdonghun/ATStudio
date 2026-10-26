---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-043-handoff.md
    reason: Approved local-only independent review scope
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Restricted trial boundaries and local-only repair amendment
  - path: WI-20261011-ATS-042-evidence-pack.md
    reason: Frozen owner candidate and provenance
---

# Evidence Pack: WI-20261011-ATS-043

> Purpose: Bind the independent local preparation verdict to the exact WI042 repair, regressions and prepared recipes.

## 1. Verdict

**TL;DR: LOCAL_READY. No blocking defect found in the bounded multi-Exec repair and prepared pre-claim check. This is not native ALLOW.** Remote preparation, upload, SSH, services, secrets, browser, app and DB remain NOT_RUN by PG and are not authorized by this verdict. MA must trigger WI044 immediately; future native work requires separate user approval and fresh target gates.

The previous PG review missed legitimate repeated `ExecReload` properties. This review reproduced that failure against preserved WI039 through genuine `prepare`, then tested WI042 through genuine prepare, baseline serialization and restore. It does not relabel the consumed WI039 attempt as successful.

## 2. Scope and Evidence

- [x] Ordered Exec arrays reviewed across parser, masked and loaded MySQL checks, baseline JSON and exact restore comparison.
- [x] Independent two-command Nginx, malformed/scalar/JSON duplicates, dropped/reordered/changed commands, and cleanup continuation tested.
- [x] Exact S2 embedded Python executed with mocked effects and actual service parsers, before approval/claim; no real private environment access.
- [x] All 27 frozen files, ordered 10-file runtime bundle, 11 recipe bodies and 79 preservation pins independently verified.
- [x] Independent 9/9, owner regressions 106/106 and PowerShell synthetic tests 8/8 PASS; 17 Python syntax units and four PowerShell bodies checked.
- [x] Implementation/history unchanged. All writes confined to WI043 review helpers, receipts and two reports. No remote or service effects.

Evidence owner/environment/date: PG, local Windows with mocked native effects, 2026-10-11. MA's separate 16:39:57 KST identity observation is corroborating evidence, not a substitute for these checks.

### Source Pointers

| Location | Reviewed behavior |
|---|---|
| `build/WI042-restricted-web-check/web_session.py:67` | Only the four named Exec properties accumulate ordered records. Scalar duplicates still reject; unknown/malformed keys and mixed/repeated empty Exec records reject. |
| `web_session.py:103` | Masked MySQL baseline requires the appropriate masked/inactive state and empty Exec arrays. |
| `web_session.py:112` | Shared read-only Nginx baseline used by preflight and prepare; required quarantine command predicates retained. |
| `web_session.py:130` | Genuine prepare records ordered arrays into baseline JSON. |
| `web_session.py:206` | Loaded MySQL still requires exactly one approved ExecStart record and existing running-stage predicates. |
| `web_session.py:291` | Restore compares the full normalized service state, including ordered arrays; dropped, reordered or changed second commands fail. |
| `build/WI042-restricted-web-check/README.md:254` | S2 parses actual Nginx and masked MySQL before fresh approval/claim; S3 remains separately gated. |
| `README.md:537` | Lifetime, restoration, intermediate status and browser-evidence limitations retained. |
| `build/WI043-restricted-web-review/test_multi_exec_review.py:147` | Eight independent prepare/restore and negative cases. |
| `test_multi_exec_review.py:240` | Ninth case executes exact complete S2 embedded Python under mocks, including invalid Nginx scalar and invalid MySQL stage. |

The parser is not a new general command-line grammar validator. Command records originate from trusted `systemctl` output; timestamp normalization remains the inherited behavior. This review claims strict property framing/scalar/JSON handling and ordered record retention, not arbitrary command-content validation.

## 3. Exact Identity

All SHA256 values are lowercase. Full 27-file names, sizes and hashes are in the pinned [freeze manifest](../../build/WI042-restricted-web-check/freeze.json). Runtime and review-set digests concatenate each ordered UTF-8 filename, NUL and exact file bytes; the review set follows manifest insertion order. Freeze and reports are not self-included.

| Object | SHA256 |
|---|---|
| freeze.json | `b92cbe9cf2b24da99953b9f6c496028205a1a5daab754a915dce30690537f6bc` |
| Ordered runtime bundle | `78ce821757111b6f4bd440a79680ed12e4e40b6592616293e4d768ddd6dbe00c` |
| Ordered review set | `9019f8d94c0346659bf6f18b8fb1cae7c06550fa5fd19371072a934e443ca316` |
| README.md | `4d4c9111718845698cc188d21830bfec31b8e6d5764a7b3f9e6a9f317b42bc23` |
| recipe-pins.json | `7000c23f0d353e5e02f92a7d07ec63c215b9d54a513a5b6283d1e472e52d0d21` |
| runtime.py | `ad3e8a20ddb17fd1e3e4878ac2096bfa74a63561b9b1e8572b6bc2a7cda7a7b6` |
| web_session.py | `3092ae8c5598d9b97e1dedc6e0bb74ca9c0d3a8989493465114229d45727257a` |
| web_policy.py | `1a22b3fe28e13ea986fc098a123d4a5a7a3d50734771eccb3288bcd15a6df9cc` |
| contract.json | `7133f07aae13a2e7b0982520429f200478307f3ce2e2cfd911328b5428aac2a5` |
| atstudio-restricted-web-wi042.service | `2ff1069da19978dfd3ee51307cd6c1e76bdd4a8fce85765476cf3f383d36ced6` |
| supervisor.service | `be9f67d022b3ea0a716dcaad28756f99c56b23489dbc2f7819c399d7cab17770` |
| mysql-bound.conf | `9bff9f2e2fdd7f8ff09718b1117a4546d75a06504a727f63789e8e95f2ea7aa5` |
| nginx-bound.conf | `d4c5f241a49aceddf2651b570024b48d7c5eef7fc01226c75df77a359ef98df5` |
| public.env | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| env-shape.json | `e8445fd79492e17c3f816f32850ca7803a63fcfb2a9b53b45444c9fd5dd1f100` |
| management-check.ps1 | `e35d15da9407614793eef490a58f1a07717b8b8267bc5bd9681782a0f6892868` |
| External WI036 verify-identity.ps1 | `6fcd7568785a8e25d9391dce09f5a698f7adda13d9ce9fcdbe66c8f679a4ad6f` |

### Recipe Pins

All 11 bodies were reviewed and independently matched by order, language, byte length and hash. Normalize CRLF to LF, retain exactly one final LF, omit fences; no substitutions or alternate installer are covered. These are prepared recipes, not executed commands.

| ID | Language | Bytes | SHA256 |
|---|---|---:|---|
| L1 | powershell | 5013 | `c8864ddcfca63718169624472b047dbd3916c8d8a76e3fd01a8ef6e7be5ad405` |
| S1 | sh | 2672 | `c059626e10d1e249fab680cbd34aa99d45ceb9e766bc7a861fdc9028878f3007` |
| S2 | sh | 4065 | `193a95ee2730987ff467967dca3c2bd92a0a083662f274165bf9e32c6c8bd6d7` |
| S3 | sh | 2195 | `2496ca8b819c20f3c2e3f799b6cf7e95472399afead26bedfa72450206172f02` |
| S4 | sh | 168 | `b3b44886f1fcfaf6d5f19f242974ca3728964eeb1f5a07dd6057404b242a9b8a` |
| S5 | sh | 146 | `5eb686d5ba65eade9e6e2a17ef5ce789a30a9be745b5ba8f9c75d516340bcb92` |
| L2 | powershell | 392 | `4bffff67d3f59cd57d01e8b79f6c4252731e121984812f8367e5bcc234ca19c0` |
| L3 | powershell | 390 | `9b642ac0863e10f08b2bb5df83b235b28ec20029f571d0cce8aa2c21ca7c3bdb` |
| S6 | sh | 146 | `8bae708bab6f9b8b2ea9c38629355f9eb0c81f01f7429b8dd007e2552b3190f5` |
| S7 | sh | 2865 | `09212fe7ab1c0bf20fbaf5c511e40d4875ba0051d4415245d46a2195454a25b0` |
| L4 | powershell | 272 | `2d6f8adbd55f2082c80bdfd9194e198575cd90430b615ae7e41e916dc6444fa9` |

## 4. Independent Test Evidence

Reproduce from the repository root. The runner denies subprocess/socket and filesystem mutation effects and uses `-I -B`. Native command results, root metadata and secret-shape input are mocked. The S2 test executes its actual embedded Python body, not a hand-reimplemented preflight; the enclosing SSH/shell transport is not executed.

```powershell
python -I -B build/WI043-restricted-web-review/verify_snapshot.py
python -I -B build/WI043-restricted-web-review/run_review.py --candidate WI042-restricted-web-check
python -I -B build/WI043-restricted-web-review/run_review.py --candidate WI042-restricted-web-check --owner
& ./build/WI043-restricted-web-review/test_recipe.ps1
```

| Check | Actual result |
|---|---|
| Independent PG cases | 9/9 PASS, 0 failures/errors/skips, 0.945s |
| Owner suite independently rerun | 106/106 PASS, 0 failures/errors/skips, 0.509s |
| PowerShell synthetic SelfTest | 8/8 PASS; four recipe bodies parse |
| Identity/preservation/syntax | 27 files, 10 ordered bundle files, 11 bodies, 79 historical pins; 17 Python compile units PASS |
| Preserved WI039 reproduction | Expected RED, one test error `DUPLICATE_JSON` in genuine prepare, 0.074s, exit 1 |

The old reproduction command is the same runner with `--candidate WI039-restricted-web-check --reproduce-old`. It proves the fixture reaches the former failure. No old artifact was repaired. No coverage percentage was measured. Counts represent test cases, not native actions or exhaustive production coverage.

[Final receipt](../../build/WI043-restricted-web-review/final-receipt.json) records commands, results, tool output identifiers and exact current review-helper pins. [Preparation receipt](../../build/WI043-restricted-web-review/preparation-receipt.json) is retained unchanged; its earlier eight-case/helper snapshot is historical, superseded by the nine-case final run.

## 5. Provenance and Limits

The local public [Nginx service template](../../deploy/single-vps/tls-quarantine/nginx-service.conf), SHA256 `cecaa38ef49f92518e6c5a776cedc7c03cecb54764afd1455b8a404f13161b93`, supplies the two exact command strings. MA reported that hash equal to the deployed drop-in at 16:17:12 KST, following the 16:16:39 observation of repeated `ExecReload`. PG did not remotely re-measure either fact. Rendered wrappers, timestamps, PIDs and remaining scalar values are derived/synthetic; this is not a complete captured native output fixture.

The corrected local test reaches real prepare, state, baseline JSON and restore paths with mocked effects. Record drop/reorder/change causes `WEB_SERVICE_RESTORE` failure, prevents the tested Nginx restart path and still reaches final baseline checks. Unknown/duplicate scalar or invalid masked MySQL output fails the exact S2 body before its environment-shape read. Loaded MySQL rendering and real restoration remain target-only evidence gaps, not inferred PASS.

Namespace-only runtime/policy/unit changes preserve the prior route allowlist, server-enforced lifetime bounds, egress checks, disabled jobs, secret-shape model and cleanup. No application/framework audit or host-failure guarantee is claimed. Historical WI039 remains HOLD/PREPARE/DUPLICATE_JSON, app NOT_RUN, cleanup PASS (6 PASS / 9 NOT_APPLICABLE). WI026's historical cleanup cause remains undetermined.

## 6. Future Execution Conditions and Rollback

These are prerequisites for a separately authorized future trial, not instructions to execute now:

- Obtain new explicit native/SSH/secret-use approval. Re-pin all frozen bytes and the external QA helper; any mismatch means HOLD. Re-check source/artifact identity, new target absence, same host/boot, current management peer and recovery access. Preserve old WI035/WI039 claims, supervisors and all historical receipts.
- Freshly confirm provider management-only rules and host/Nginx fences. An expired portal alone does not invalidate architecture, but missing fresh firewall or recovery evidence cannot be replaced by old screenshots. Re-login when needed before approval/claim.
- L1 must resolve Windows System32 OpenSSH and use the freshly verified strict pinned argument array with no forwarding or disabled host checks. Contain/redact transport stderr, require fixed public upload count and identity result; do not expose raw private output. Hash-check exact UTF-8/LF shell buffers, preserving `SSH_CONNECTION` for `/bin/sh -s`.
- Prepared S2 invokes both actual service parsers before S3/claim; it only reads existing state and existing bounded environment shape. A failed parser or precondition stops the sequence. Vendor-unit evidence does not prove the later loaded systemctl representation.
- Retain finite GET/query routes, management-only access, no auth/cookie forwarding and bounded HTTP checks. Retained browser context is not pristine; DOM/screenshots do not prove arbitrary network silence or storage absence. Browser success cannot establish login, payment, media or public capacity success.
- Existing P2 intermediate `deactivating`/`WEB_RESTORE` observations are not authoritative final cleanup results. The existing unchanged recipe already permits bounded S5 read-only polling after finish until final `result.json`, then S7 independent postcheck. Preserve every receipt. No code relaxation is needed for that observation procedure; never discard a true final FAIL or retry/repair under it.
- Server-side caps and no automatic retry remain mandatory. Any failure is HOLD; a consumed claim stays consumed. Rollback of this review is to withhold execution, not erase evidence. No native rollback was performed.

## 7. References and Handoff

Retained Tier 0: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary. Role: PG. Tier 1: security-policy, quality-gates and access-control-policy as assigned. Tier 2: current decision context, REQ006 amendments, WI039/040/041 history, WI042 completion, WI043 handoff, QA036 contract and deployed Nginx template. Skills applied: `test` and `create-wi-evidence-pack`. No new subagent context was injected or delegated.

**Next: MA triggers WI044 DocOps immediately to integrate LOCAL_READY and preserve the consumed-attempt history. Native authorization remains absent.**

## Related Documents

- [WI043 Handoff](WI-20261011-ATS-043-handoff.md): Assigned authority and scope.
- [REQ006](../user/REQ-20261011-ATS-006.md): Governing requirement.
- [WI042 Evidence](WI-20261011-ATS-042-evidence-pack.md): Owner completion and full candidate pointers.
- [WI043 Summary](../user/WI-20261011-ATS-043-summary.md): Concise verdict.
- [Final Test Receipt](../../build/WI043-restricted-web-review/final-receipt.json): Independent execution record.
- [Frozen Manifest](../../build/WI042-restricted-web-check/freeze.json): Exact full reviewed file set.
- [Recipe Manifest](../../build/WI042-restricted-web-check/recipe-pins.json): Exact recipe bodies.
