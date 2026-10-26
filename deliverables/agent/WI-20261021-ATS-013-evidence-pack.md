---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# Evidence Pack: WI-20261021-ATS-013

## Summary (one-liner)

Prepared a source-pinned, local-only tested audio native adapter reusing v8r1 lifecycle and unchanged JAR; independent final review follows under WI011, and all target execution remains NOT_RUN.

## Scope / DoD Check

- [x] Only new `build/WI013-audio-native/` and this WI's evidence/user summary were changed.
- [x] Source-pinned v8r1 lifecycle/native-format parsers and WI010 bounded audio components are copied without changing their originals.
- [x] Concrete exact-approval entry, actor-only private pipe contract, same-window existing logins, ME identity checks, quiet pre-Java baseline, audio-only JVM launch, process observation, API adapter, file/DB ledger and independent cleanup are connected.
- [x] No SMTP credentials/egress, fresh signup/mail, token minting, account provisioning, entitlement grant or payment path.
- [x] Login refresh cell changes are separate from audio baseline. Three distinct preexisting actors are checked; absent prerequisites cause HOLD.
- [x] 25 local tests pass with injected commands/DB/HTTP observations; failures, timeout, partial start and cleanup independence are covered.
- [x] Human audible playback remains a distinct mandatory gate, never replaced by MP3 byte/range tests.
- [ ] Native Linux target compatibility, upload/conversion, actual cleanup and human playback: NOT_RUN; future authorization required.

## Reference Documents (Tier 0-2)

Injected through [WI013 handoff](WI-20261021-ATS-013-handoff.md):

| Tier | Document | Reason |
|---|---|---|
| 0 | [Core principles](../../docs/standards/core-principles.md) | Approved local-only scope and delegation |
| 0 | [Documentation standards](../../docs/standards/documentation-standards.md) | Additive evidence and relative pointers |
| 0 | [Development standards](../../docs/standards/development-standards.md) | Bounded standard-library scripts and source-bound validation |
| 0 | [Glossary](../../docs/standards/glossary.md) | REQ/WI/evidence terms |
| 1 | [Security policy](../../docs/policies/security-policy.md) | Private transport, no secret output, explicit runtime authority |
| 1 | [Quality gates](../../docs/policies/quality-gates.md) | Independent review and bounded validation |
| 1 | [SE role](../../.claude/agents/se.md) | Implementation responsibility |
| 2 | [WI009 evidence](WI-20261021-ATS-009-evidence-pack.md) | Completed auth/cleanup evidence reused, not overwritten |
| 2 | [WI010 evidence](WI-20261021-ATS-010-evidence-pack.md) | Bounded audio request/ledger components |

Injection rules: MA-provided Tier0/Tier1 summaries plus exact source/evidence pointers; SE integration assignee. Evidence Pack generated with `create-wi-evidence-pack` after checking the handoff exists. No subdelegation.

## Evidence Pointers

- [Candidate README](../../build/WI013-audio-native/README.md): supported API phase, private-input contract, scope proposal, budgets and unverified gates.
- [Native adapter](../../build/WI013-audio-native/audio_native.py): exact release/actor/data plan, same-runtime login/ME, pre-Java queue and all-table digest baseline, no-SMTP policy, worker process/argv observation, final ledger and retained cleanup.
- [Target entry](../../build/WI013-audio-native/audio_entry.py): exact location/root gates; local invocations HOLD.
- [Private frame library](../../build/WI013-audio-native/audio_private.py): inert actor-only length-prefixed frame; no prompting, secret lookup or transmission.
- [Builder](../../build/WI013-audio-native/build_candidate.py): verifies known source freeze bytes/member hashes before copying public Python/JSON files.
- [Manifest](../../build/WI013-audio-native/manifest.json): 62 candidate members, SHA-256 `5d4b72b6e85993fb50656f3420b1d477589b0aa69d58315b166ee29ed2d73eb4`.
- [Source pins](../../build/WI013-audio-native/source-pins.json): 53 copied-source dependencies. Original v8r1 binding freeze `3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d`; WI010 freeze `0d7ae85c4ca3f0425826c2784867ee57169efb96c14cb5d2b315567d8b9bc516`.
- Unchanged JAR SHA-256 `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`; approved WAV SHA-256 `d7b1011a45c84eb0c0d0308cc744289aa277b8b43ff0a4b97d6c59a1254ffa13`.

## Commands & Outputs

Only local standard-library Python and read-only source inspection were used; no app/DB/service execution or dependency installation.

```powershell
python -B build/WI013-audio-native/build_candidate.py
python -B -m unittest discover -s build/WI013-audio-native -p test_native_audio.py -v
```

- [Test log](../../build/WI013-audio-native/test-output.txt): 25 tests PASS on 2026-10-21, local Windows/Python; SE-owned evidence.
- [Tests](../../build/WI013-audio-native/test_native_audio.py): original controller phase order, all partial-start failures, every cleanup failure, deadline before claim, no-secret observer logs, source-bound worker semantics/double hashing/FFmpeg fixture, actor/frame scope, no-SMTP nft recorded-format parsing and login→ME→audio-baseline ordering.
- Intermediate source-informed defects were corrected before freezing: worker master flag, unit-name isolation, FFmpeg argument count, refresh-token double hash, source binding before upload response and duplicate cleanup status handling. Tests do not establish real Linux compatibility or audible playback.

## Risks / Rollback

- Current release receipts are absent; none was fabricated. The candidate is not a server restart approval. Root-owned receipt custody and actual human/PG approval must be established in the future approved operator session.
- Private staged inputs/child environment follow retained root0600 storage and include sensitive material. Future approval must explicitly cover retention/secure removal; never publish their contents. No real inputs were read or produced here.
- Storage inventory limits and strict process observations may HOLD on larger existing data or unsupported native formats. Preserve evidence and reproduce the minimal failing check; do not delete data or repeat the entire flow to pass.
- Human playback/UI routing is not implemented by the API candidate. A separately scoped management-only player/seek path or private verified derivative delivery must be approved before claiming the overall playback goal complete.
- Rollback for this local preparation is to stop referencing the new candidate and preserve it for audit. Original source/tool packages and historical receipts remain unchanged; no revert/delete/commit was performed. Any future target rollback uses the retained independently attempted lifecycle, with app/cgroup absence before network-policy teardown and explicit restore verification.

## Follow-ups

WI013 local implementation completion triggers WI011 final exact-manifest independent review. Only after review/required fixes should WI012 report local readiness and ask separately for server/data resumption. Actual actor IDs, entitlement and any role/account/grant changes remain undecided and unexecuted. Human playback remains open until separately observed.
