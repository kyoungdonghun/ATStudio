[EVIDENCE PACK]
WI ID: WI-20260306-ATS-001
REQ: REQ-20260306-ATS-004
Agent: docops
Date: 2026-03-06
Status: Complete

---

[SCOPE SCAN RESULTS]

Files scanned for "앨범" occurrence (before change):

| File | Match Count | Context |
|------|-------------|---------|
| `docs/ui/atstudio-front-list.md` | 4 | Playlist section (lines 36–39) |
| `docs/design/api-spec.md` | 0 | — |
| `docs/design/usecase/sound-playlist.md` | 0 | — |
| `docs/design/usecase/index.md` | 0 | — |
| `docs/design/db-schema.md` | 0 | — |
| `docs/standards/glossary.md` | 0 | — |

Only one file required changes.

---

[CHANGE DIFF — docs/ui/atstudio-front-list.md]

Lines 36–39 (inside section `## 💿 플레이리스트 (Playlist)`, all rows reference `GET /api/playlists` or `POST /api/playlists`):

Line 36:
  BEFORE: | 4 | 앨범 목록 (이미지 타입) | `3.2 GET /api/playlists` | auth required |
  AFTER:  | 4 | 재생목록 목록 (이미지 타입) | `3.2 GET /api/playlists` | auth required |

Line 37:
  BEFORE: | 5 | 앨범 목록 (리스트 타입) | `3.2 GET /api/playlists` | auth required |
  AFTER:  | 5 | 재생목록 목록 (리스트 타입) | `3.2 GET /api/playlists` | auth required |

Line 38:
  BEFORE: | 8 | 앨범 생성 | `3.1 POST /api/playlists` | auth required |
  AFTER:  | 8 | 재생목록 생성 | `3.1 POST /api/playlists` | auth required |

Line 39:
  BEFORE: | 9 | 앨범 수정 | `3.5 PUT /api/playlists/{id}` `3.6 PUT (트랙 순서)` `3.7 DELETE (트랙 삭제)` | auth required |
  AFTER:  | 9 | 재생목록 수정 | `3.5 PUT /api/playlists/{id}` `3.6 PUT (트랙 순서)` `3.7 DELETE (트랙 삭제)` | auth required |

Context discrimination rationale:
- Section header: `## 💿 플레이리스트 (Playlist)` — unambiguously Playlist domain
- API endpoints: `/api/playlists` — Playlist domain
- Auth: `auth required` (subscriber, not ADMIN) — Playlist domain (Album is [ADMIN] or [PUBLIC])
- All four "앨범" tokens are Playlist-context mislabels.

---

[POST-CHANGE GREP VERIFICATION]

Command: grep "앨범" docs/ui/atstudio-front-list.md
Result: No matches found
Status: PASS — 0 Playlist-context "앨범" remaining

Command: grep "재생목록" docs/ui/atstudio-front-list.md
Result:
  Line 36: | 4 | 재생목록 목록 (이미지 타입) | `3.2 GET /api/playlists` | auth required |
  Line 37: | 5 | 재생목록 목록 (리스트 타입) | `3.2 GET /api/playlists` | auth required |
  Line 38: | 8 | 재생목록 생성 | `3.1 POST /api/playlists` | auth required |
  Line 39: | 9 | 재생목록 수정 | `3.5 PUT /api/playlists/{id}` `3.6 PUT (트랙 순서)` `3.7 DELETE (트랙 삭제)` | auth required |
Status: PASS — 4 corrections confirmed

---

[ALBUM DOMAIN "앨범" PRESERVATION VERIFICATION]

Files inspected for Album domain "앨범" (to confirm no unintended change):

1. `docs/design/api-spec.md` Section 15
   - grep "앨범" result: 0 matches
   - Section 15 uses English label "Album" only
   - Status: PRESERVED

2. `docs/design/db-schema.md` Section 14
   - grep "앨범" result: 0 matches
   - Section header: `# 14. Albums`, tables: `albums`, `album_tracks` (English only)
   - Status: PRESERVED

3. `docs/standards/glossary.md` playlist entry (line 81)
   - Content: `| playlist | Playlist | Curated collection of tracks for subscribers | collection, mix | album (different) | - | ...`
   - Forbidden column uses English `album (different)` — no Korean "앨범" present
   - Status: PRESERVED

4. `docs/design/usecase/sound-album.md` (reference, not a target file)
   - No Korean "앨범" label scanned (out of scope; no changes made)
   - Status: PRESERVED

---

[FILES NOT CHANGED — REASON]

| File | Reason |
|------|--------|
| `docs/design/api-spec.md` | No "앨범" text present. Section 3 (Playlist) uses English labels only. |
| `docs/design/usecase/sound-playlist.md` | No "앨범" text present. Document already uses English + correct Korean labels. |
| `docs/design/usecase/index.md` | No "앨범" text present. Playlist row description uses English. |
| `docs/design/db-schema.md` | No "앨범" text present. Section 5 (Playlists) uses English column/table names only. |
| `docs/standards/glossary.md` | No Korean "앨범" text present. Playlist Forbidden column uses English `album (different)`. No correction needed. |

---

[ACCEPTANCE CRITERIA CHECKLIST]

- [x] `docs/ui/atstudio-front-list.md`: Screen names corrected to "재생목록 목록(이미지)", "재생목록 목록(리스트)", "재생목록 생성", "재생목록 수정"
- [x] `docs/design/api-spec.md`: No "앨범" present in Section 3 — confirmed no action needed
- [x] `docs/design/usecase/sound-playlist.md`: No "앨범" present — confirmed no action needed
- [x] `docs/design/usecase/index.md`: No "앨범" present — confirmed no action needed
- [x] `docs/design/db-schema.md`: No "앨범" present — confirmed no action needed
- [x] `docs/standards/glossary.md`: No Korean "앨범" present — confirmed no action needed
- [x] Album domain (Section 15, `/api/albums`) "앨범" label preserved — no Korean label existed, all English
- [x] Grep verification: 0 Playlist-context "앨범" remaining
- [x] API URLs unchanged (`/api/playlists` retained)
- [x] English identifiers unchanged (`Playlist`, `playlist`, `playlists`)

---

[ROLLBACK POINTER]

git checkout -- docs/ui/atstudio-front-list.md

Reversal restores lines 36–39 to prior "앨범" labels.
