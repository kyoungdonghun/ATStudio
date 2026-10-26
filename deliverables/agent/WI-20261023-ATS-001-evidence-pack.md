---
version: 1.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: reference
status: active
---

# Evidence Pack: WI-20261023-ATS-001

## Summary

Implemented whole-HTTP-stream versus the same trial track's derivative file length/SHA-256 linkage in a new local WI013 candidate. All 41 focused tests pass; native audio and human playback remain NOT_RUN. Independent acceptance belongs to WI-20261023-ATS-003.

## Scope / DoD Check

- [x] Own only `build/WI20261023-001-audio-hash/`, this Evidence Pack, and the corresponding user summary.
- [x] Pin original WI013 manifest; preserve original WI013/WI010/v8r1 trees and all 53 frozen copied dependencies.
- [x] Capture complete bounded STREAM bytes/SHA-256 without changing the original WAV download check.
- [x] Bind the persisted receipt to candidate/boot/nonce and final track/generation/source/derivative key.
- [x] Re-read durable evidence before final AUDIO ledger/file PASS; missing, malformed, mismatched, cross-track and write failures fail closed.
- [x] Supply deterministic overlay reconstruction and current source/JAR identity evidence.
- [x] Run bounded synthetic linkage/closeout checks and existing native-adapter checks; no application rebuild or broad project suite.
- [ ] Independent PG acceptance: owned by WI-20261023-ATS-003, not self-approved here.
- [ ] Native audio runtime, actual storage/HTTP behavior, human playback: NOT_RUN; no phase-2 authority inferred.

## Reference Documents (Tier 0-2)

**Injected Context** from [WI handoff](WI-20261023-ATS-001-handoff.md):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved bounded scope, preservation, evidence accuracy |
| 0 | `docs/standards/development-standards.md` | SE implementation quality |
| 0 | `docs/standards/documentation-standards.md` | English evidence and traceability |
| 0 | `docs/standards/glossary.md` | Shared WI/REQ terminology |
| 1 | `docs/policies/security-policy.md` | No secret output; fail-closed receipt binding |
| 1 | `docs/policies/quality-gates.md` | Scoped verification and independent acceptance |
| 2 | `deliverables/user/REQ-20261011-ATS-007.md` | Active approved REQ and explicit 2026-10-23 local scope |
| 2 | `docs/index.md` current entry | Historical/current decision boundary |
| 2 | `deliverables/agent/WI-20261021-ATS-009-evidence-pack.md` | Earlier auth/mail boundary only |
| 2 | `deliverables/user/WI-20261021-ATS-012-summary.md` | Human playback boundary |

**Injection Rules Applied:** `.claude/config/context-injection-rules.json`; assignee SE; local Python harness overlay; MA supplied the operative Tier 0 principles and scope in the handoff/task. Listed context is injected guidance, not a claim that every linked policy was re-opened by this subagent. `create-wi-evidence-pack` was read and applied; the required handoff existed before processing. `.claude/config/workspace.json` confirms project tag ATS.

## Evidence Pointers

- `build/WI20261023-001-audio-hash/audio_hash.py`: `HashTrial` captures the original full HTTP STREAM response; `bind_result` adds exact attempt context; `link_stream` validates result shape and complete action chain, then compares the same derivative's key, bytes and SHA-256.
- `build/WI20261023-001-audio-hash/audio_native.py`: minimal generated overlay imports `HashTrial`; `window` durably publishes the bound result; `final_ledger` re-reads it and publishes `files-final.json` -> `stream-linkage.json` -> `audio-final.json`. The final status includes the linkage result.
- `build/WI20261023-001-audio-hash/audio_native.py`: `files_snapshot` now requires actual read count to equal the stable file size; byte-length linkage cannot rely on an earlier stale size alone.
- `build/WI20261023-001-audio-hash/build_candidate.py`: validates original 62-member WI013 manifest, performs exact single-occurrence source replacements, and independently validates every original/destination frozen dependency pair.
- `build/WI20261023-001-audio-hash/provenance.json`: maps all original WI013 members to COPY or OVERLAY, including before/after hashes. Original root builder/readme/test output are retained under `upstream/` as historical evidence.
- `build/WI20261023-001-audio-hash/verify_candidate.py`: read-only exact tree check and in-memory reconstruction of both overlays.
- `build/WI20261023-001-audio-hash/test_hash_linkage.py`: 15 focused tests, including native final-ledger integration and recovered-ledger reconstruction.
- `build/WI20261023-001-audio-hash/test_native_audio.py`: 26 retained adapter tests; only the final-file test fixture is updated to supply the newly mandatory receipt.
- `build/WI20261023-001-audio-hash/test-result.json`: local test/identity receipt, intentionally excluded from the candidate manifest by the inherited rule.
- `build/WI20261023-001-audio-hash/README.md`: local commands, behavior, provenance, and unverified limits.

## Exact Identity

Verification date: **2026-10-23**. Environment: **local Windows/Python standard library**, with synthetic HTTP/native operations. Evidence owner: **SE implementation agent**. The document date is not a target-runtime acceptance date.

| Item | SHA-256 |
|---|---|
| New manifest, 68 members | `cd32ca3fb64e84b324d95c1172041395d6413c7648be4995cec198835489ff92` |
| New `audio_hash.py` | `798210dfce8fe91aacf0d6863709c1c46ad90bc0030e52f304222a71e9abaef7` |
| Generated `audio_native.py` | `870365465f01b8d9f2375ff194450d78fce26d538a8ea5b7fb4cb38027120086` |
| New builder | `abdea39674739139f856014b947050656981020d5d0f2313aeaa7a586b739800` |
| New provenance | `1f944c57c02a4917cac3057973221eebc6673d7209919909928a55576e363450` |
| Read-only verifier | `ebd1bfbb283699837bdd54397652f3b6a92837fbf5cc8312d74eba0fec901652` |
| Focused linkage tests | `d70c2dd09c7e8298415926d255da5c0e5ba9d81af9fd9e0d91b020d12b0afe2d` |
| Original WI013 manifest, unchanged 62 members | `3c837f740160d6a54fb3093d75e2148101efe9f67c11b39b615f3086598b48f2` |
| Original WI010 freeze | `0d7ae85c4ca3f0425826c2784867ee57169efb96c14cb5d2b315567d8b9bc516` |
| Original v8r1 binding freeze | `3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d` |
| Current source receipt, 643 entries | `c6fc15d8f38c6597d159c7dd258048d25e74b3d03f5eb5596b701219a8ae207e` |
| Retained application JAR | `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81` |
| Approved WAV fixture | `d7b1011a45c84eb0c0d0308cc744289aa277b8b43ff0a4b97d6c59a1254ffa13` |

Branch/HEAD observed: `main` / `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`. The workspace already contains extensive unrelated modifications and untracked evidence. They were preserved; nothing was staged, committed, or pushed. The current 643-entry source identity check matched its registered source receipt with no mismatches; no app source/JAR was edited or rebuilt by this WI.

## Commands & Outputs

Run from the repository root:

```powershell
python -B build/WI20261023-001-audio-hash/build_candidate.py
python -B build/WI20261023-001-audio-hash/verify_candidate.py
python -B -m unittest discover -s build/WI20261023-001-audio-hash -p test_hash_linkage.py -v
python -B -m unittest discover -s build/WI20261023-001-audio-hash -p test_native_audio.py -v
python -B build/WI011-audio-review/verify_identity.py
```

- Builder/read-only verifier: 68 candidate members; all 62 original members verified; 53 copied-source dependencies exact at both source and destination; both overlays reproduced; no mismatches.
- Source identity script: all 643 source entries exact, JAR/WAV pins exact, retained auth/PC-delivery/private-producer bundle checks exact.
- Standard library only; `-B`; no target entrypoint, network, service command, database query, account login or native trial performed by this subagent.

## Tests

| Suite | Result | Measured elapsed |
|---|---|---|
| New focused hash/receipt/native integration tests | 15/15 PASS | 0.147 s |
| Retained native adapter tests with valid updated final receipt fixture | 26/26 PASS | 0.028 s |

The new tests verify a correct full-body/file match; equal-length corruption outside the tested Range/Seek slices; length mismatch despite equal hash; missing/malformed/invalid receipt fields; cross-track and generation/source/derivative mismatch; candidate/boot/nonce mismatch; missing/foreign/baseline derivative files; original WAV hash rejection; durable re-read despite valid memory state; each final receipt write failure; result-write exception; real final-linkage mismatch causing aggregate HOLD while all cleanup steps execute; and recovered ledger linkage. Invalid-value subcases are grouped within these 15 test methods, not counted as separate independent tests.

## Risks / Rollback

- Native MP3 conversion/128 kbps, real network/storage behavior, Linux secure publication/fsync, target resource behavior and human playback remain NOT_RUN/unverified. Synthetic tests do not establish any of them.
- A publication failure propagates to the retained controller and prevents aggregate PASS. A partial local observation receipt is never independent proof of aggregate success; use the final controller/cleanup outcome and exact candidate review.
- Native approval shape/names remain WI013. The new candidate manifest is different: the old PG receipt cannot authorize it because receipt/candidate hashes must match. No policy exception or phase-2 authority was added.
- Runtime file/DB keys remain protected by inherited ledger controls. The added receipt stores only hashes, byte counts, track/generation and attempt context; it does not persist HTTP media or credentials.
- Rollback: decline selection of this candidate and preserve it for evidence. Original WI013/WI010/v8r1 assets remain unchanged. Do not delete original or new evidence; no remote rollback is applicable because this WI performed no remote action.

## Follow-ups

WI-20261023-ATS-001 implementation is complete and immediately unblocks WI-20261023-ATS-003 independent PG review. The exact new manifest pin and test commands were sent to PG. MA owns subsequent phase decisions; no audio run is authorized or claimed by this Evidence Pack.
