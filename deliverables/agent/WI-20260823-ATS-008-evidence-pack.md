# Evidence Pack: WI-20260823-ATS-008

## Summary

- Repaired the HomePage subtitle assertion and restored only the 20 exact,
  missing public fixtures referenced by the active `AT.M Demo` Track set.

## Scope / DoD Check

- [x] The HomePage hero subtitle matcher accepts the rendered line break and
  still asserts the complete intended subtitle.
- [x] Guarded local DB tooling enumerated exactly 10 active scoped Tracks and
  their 20 unique storage keys without emitting credentials or datasource
  values.
- [x] Every DB audio and thumbnail key has a matching retained source fixture.
- [x] All 20 exact target paths were absent before copy; no existing file was
  overwritten.
- [x] Source/target SHA-256 parity passed for all 20 copied files.
- [x] Each Track full stream, Range stream, and thumbnail request succeeded.
- [x] Focused HomePage test and `git diff --check` passed.

## Reference Documents

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Scope and execution controls. |
| 0 | `docs/standards/development-standards.md` | Implementation and validation conventions. |
| 1 | `docs/policies/quality-gates.md` | Regression and evidence expectations. |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React test review guidance. |
| 2 | `docs/standards/frontend-standards.md` | Active frontend test and API conventions. |
| REQ | `deliverables/user/REQ-20260823-ATS-001.md` | Approved scope. |
| WI | `deliverables/agent/WI-20260823-ATS-008-handoff.md` | Acceptance and rollback contract. |
| Prior WI | `deliverables/agent/WI-20260823-ATS-007-evidence-pack.md` | Residual-condition source evidence. |

## Evidence Pointers

- `frontend/src/pages/public/HomePage.test.tsx:74-78`: The hero title/footer
  assertions remain whitespace-tolerant. The repaired subtitle matcher now
  covers both line-separated sentences, rather than requiring an isolated
  exact text node.
- `frontend/src/pages/public/HomePage.tsx:297-305`: Read-only confirmation of
  the intentional title and subtitle line breaks; this WI did not edit it.
- `src/main/resources/application.yml:82-85` and
  `src/main/java/com/atstudio/atstudio/config/WebConfig.java:17-25`: The active
  public storage resolves to the workspace `uploads` root. Local config has no
  storage override and the current process `user.dir` is this workspace.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java:154-193`:
  The full and Range stream endpoint contract verified below.
- `uploads/tracks/audio/` and `uploads/tracks/thumbnail/`: 20 new, Git-ignored
  local runtime fixtures. `git check-ignore` confirms the runtime path is
  ignored by `.gitignore:57`.

### Exact Runtime Key Parity

Guarded read-only DB query selection: active Track titles matching `AT.M Demo
%`; the guard rejected any count other than 10, blank key, duplicate key, or
source/target mismatch before copy.

| Track ID | Title | Audio key | Thumbnail key |
| ---: | --- | --- | --- |
| 4 | AT.M Demo 01 - Dawn Signal | `tracks/audio/d7e04e335477415f8ba665412d5ed6d0.wav` | `tracks/thumbnail/451994df1c224649816cc7e4ce5d1e48.jpg` |
| 5 | AT.M Demo 02 - City Paper | `tracks/audio/6c7956b65a44481fa1abc5dfa7cdc881.wav` | `tracks/thumbnail/7d2a422aad0a449e834c3c02ccfc9cec.jpg` |
| 6 | AT.M Demo 03 - Soft Circuit | `tracks/audio/0f2bdfd991744b61989c4b749d33a2bb.wav` | `tracks/thumbnail/c14d530607b04daabeb67481a345df04.jpg` |
| 7 | AT.M Demo 04 - Window Light | `tracks/audio/6b610658978a4e7db950ca0a0ead5511.wav` | `tracks/thumbnail/9fdae81eef354fcd942efdf769e8e539.jpg` |
| 8 | AT.M Demo 05 - Slow Bloom | `tracks/audio/64dfdfd282a346d190cb2344efe56f6e.wav` | `tracks/thumbnail/08a2c6fbfcf742d78a831cd673d0228b.jpg` |
| 9 | AT.M Demo 06 - Night Marker | `tracks/audio/4be17957d29741369b9293fe7b66bd7f.wav` | `tracks/thumbnail/988c7d1704ce46bc8da7e07316012103.jpg` |
| 10 | AT.M Demo 07 - Open Road | `tracks/audio/cc170541e6c74cb29b8a53f6f48180d7.wav` | `tracks/thumbnail/a4fe0bda77fd4dc9929dd15271f99677.jpg` |
| 11 | AT.M Demo 08 - Amber Room | `tracks/audio/b3218db4770e4ff291dc132ef8fa2884.wav` | `tracks/thumbnail/d27a3e3f326242fca4c3bc391c64b7d1.jpg` |
| 12 | AT.M Demo 09 - Blue Outline | `tracks/audio/6aa84a9ff63b47769402f1832e28e81f.wav` | `tracks/thumbnail/22fda875f72242d28b1f52e5f9339c25.jpg` |
| 13 | AT.M Demo 10 - Last Frame | `tracks/audio/2474effe76b6469db8a84311ddafe99b.wav` | `tracks/thumbnail/b4b130670876457e901f8b0702422043.jpg` |

Pre-copy guard result: 10 source audio keys, 10 source thumbnail keys, exact
DB/source parity for both sets, and 0 existing exact target paths. The guarded
copy verified each new target with SHA-256 immediately after copy. A separate
post-copy comparison found 20 exact keys and 0 hash mismatches.

## Commands And Results

```text
npm test -- --run src/pages/public/HomePage.test.tsx
```

- PASS: 1 test file, 7 tests, 0 failures.

```text
GET /api/tracks/{id}/stream
GET /api/tracks/{id}/stream with Range: bytes=0-1023
GET /uploads/{thumbnail-key}
```

- For IDs 4 through 13: full stream 10/10 HTTP 200, Range stream 10/10 HTTP
  206 with `Content-Range: bytes 0-1023/112044`, thumbnail 10/10 HTTP 200.
- Full streams returned `audio/wav`; thumbnails returned `image/jpeg`.

```text
git diff --check
```

- PASS: no whitespace diagnostics. Existing CRLF-to-LF warnings in unrelated
  dirty files were emitted by Git and were not changed by this WI.

The pre- and post-restore guarded catalog proof reported the same scoped
fixture state: one verified admin, 20 tags, 10 active media-and-waveform-ready
Tracks, 40 Track-tag relations, one active album, and 10 album memberships.
All guarded DB operations were `SELECT` only.

## Risks / Rollback

- Risk: `uploads/` is local ignored runtime state and is not a source-control
  transport mechanism. The post-WI independent verification is
  `WI-20260823-ATS-009`.
- Rollback: after reconfirming DB rows remain unchanged, delete only the 20
  exact key paths listed above from `uploads/`. Do not remove any pre-existing
  upload or alter any DB row.
