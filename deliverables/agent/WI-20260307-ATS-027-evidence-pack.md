# WI-20260307-ATS-027 Evidence Pack

> **WI**: WI-20260307-ATS-027 | **REQ**: REQ-20260307-ATS-009
> **Agent**: docops | **Date**: 2026-03-08
> **Task**: Track 3 — Frontend document internal cross-validation + api-spec reference validity (read-only)

---

## 1. Validation Scope and Method

| Document | Version | Lines Read | Status |
|----------|---------|------------|--------|
| `docs/ui/atstudio-front-list.md` | v4 | 152 | Read complete |
| `docs/ui/modal-list.md` | v1.2 | 277 | Read complete |
| `docs/ui/screen-flow.md` | v1.2 | 359 | Read complete |
| `docs/design/api-spec.md` | v6 | 1815 | Read complete |

Validation method: Manual systematic cross-reference. No files were modified.

---

## 2. Check 1 — front-list vs screen-flow

### Screen ID Inventory (48 screens from front-list)

| Section | IDs |
|---------|-----|
| Auth | A-1, A-2, A-3, A-4 |
| Track | 1, 3, B-1, 6, 7 |
| Album | L-1, L-2, L-3, L-4, L-5 |
| Playlist | 4, 5, C-1, 8, 9 |
| Personal | 10, D-1, E-1, F-1, F-2 |
| Cart | 11 |
| Subscription | 16-1, 16-2, 16-3 |
| Whitelist | H-1 |
| Company Cert | I-1, I-2 |
| Question | 13, 14, 15 |
| Notice | 20, 21, 21-2, 22 |
| Admin | 18, K-1, K-2, K-3, K-4, K-5, K-6, K-7 |
| Error | ERR-1, ERR-2 |

### Findings

**MINOR-001 — Screen 8 (재생목록 생성): ID block not labeled in screen-flow**

- `docs/ui/atstudio-front-list.md:52` — Screen 8 = "재생목록 생성"
- `docs/ui/screen-flow.md:98-101` — Section 4 describes the creation action ("새 재생목록 만들기" button → form submit → 3.1 POST → return to previous) but never labels the destination as `[8 재생목록 생성]`
- All other 47 screens appear as `[ID 화면명]` format in screen-flow. Screen 8 is the sole exception.
- Impact: Low. The screen exists implicitly but lacks the traceable ID reference.

**MINOR-002 — K-5 screen name inconsistency between front-list and screen-flow**

- `docs/ui/atstudio-front-list.md:134` — K-5 name: `기업 인증 목록 / 심사 처리`
- `docs/ui/screen-flow.md:282` — K-5 labeled as: `[K-5 기업인증 심사]`
- Differences: (1) spacing "기업 인증" vs "기업인증", (2) description truncated
- front-list is the primary source per modal-list.md dependencies metadata.

**47/48 screen IDs confirmed present in screen-flow with matching names. Only Screen 8 ID unlabeled.**

---

## 3. Check 2 — front-list vs modal-list

### Method

front-list does not contain M-xx columns. Verification performed as: for each modal in modal-list, confirmed that "발생 화면" references a screen ID valid in front-list.

### Result: 0 Issues

| Modal Range | Screens Referenced | Valid |
|-------------|-------------------|-------|
| M-01~M-10 | 10, 6, 7, 9, L-5, 16-3 | All valid |
| M-11~M-20 | K-7, 1/3/B-1, 4/5, L-1/L-2, I-1, 14, K-5, K-4, 22, 15 | All valid |
| M-21~M-28 | H-1, 11, D-1, K-2, K-1, 16-2, M-09(in-modal), K-6 | All valid |

Note: M-27 references "M-09 (PlanCompareModal 내)" as the trigger context — this is an in-modal sub-trigger, not a screen reference. Acceptable design pattern.

---

## 4. Check 3 — front-list vs api-spec

### MAJOR Finding

**MAJOR-001 — 11.1 POST /api/download-queue missing /{trackId} path parameter**

- `docs/ui/atstudio-front-list.md:25` — Screen 1 (메인화면): `11.1 POST /api/download-queue`
- `docs/ui/atstudio-front-list.md:26` — Screen 3 (음원 목록): `11.1 POST /api/download-queue`
- `docs/design/api-spec.md:1343-1348` — §11.1 actual URL: `POST /api/download-queue/{trackId}`
- The `{trackId}` path parameter is absent in front-list entries for Screens 1 and 3.
- Same error propagated to screen-flow.md lines 81 and 87.
- Note: Screen 11 references `11.1~11.3 /api/download-queue` (abbreviated grouped format) — acceptable.

### MINOR Findings

**MINOR-003 — Screen 11: {id} vs {trackId} (api-spec §1.5)**

- `docs/ui/atstudio-front-list.md:73` — `1.5 GET /api/tracks/{id}/download`
- `docs/design/api-spec.md:247-253` — §1.5 URL: `GET /api/tracks/{trackId}/download`
- Path param name: `{id}` vs `{trackId}`

**MINOR-004 — C-1: {id} vs {playlistId} (api-spec §3.3)**

- `docs/ui/atstudio-front-list.md:51` — `3.3 GET /api/playlists/{id}`
- `docs/design/api-spec.md:461-481` — §3.3 URL: `GET /api/playlists/{playlistId}`

**MINOR-005 — Screen 9: {id} vs {playlistId} (api-spec §3.5, §3.8)**

- `docs/ui/atstudio-front-list.md:53` — `3.5 PUT /api/playlists/{id}` and `3.8 DELETE /api/playlists/{id}`
- `docs/design/api-spec.md:506-552` — §3.5 URL: `PUT /api/playlists/{playlistId}`, §3.8 URL: `DELETE /api/playlists/{playlistId}`

**MINOR-006 — K-3: {id}/{id} vs {userId}/{licenseId} (api-spec §7.4)**

- `docs/ui/atstudio-front-list.md:132` — `7.4 GET /api/users/{id}/licenses/{id}`
- `docs/design/api-spec.md:1057-1062` — §7.4 URL: `GET /api/users/{userId}/licenses/{licenseId}`
- Both path params use `{id}`, losing semantic distinction between user and license identifiers.

**Additional metadata note:**

- `docs/ui/atstudio-front-list.md:3` — Header reads "API Spec v5 기준"
- `docs/design/api-spec.md:1` — Current version is v6
- Not a functional issue but should be updated for accuracy.

### All Other API References: Confirmed Valid

Sections verified as present in api-spec: §1.1–1.8, §2.1–2.4, §3.1–3.8, §4.1–4.3, §5.1–5.11, §6.1–6.10, §7.1–7.4, §8.1–8.7, §9.1–9.5, §10.1–10.3, §11.1–11.3, §12.1–12.4, §13.1–13.5, §14.1–14.8, §15.1–15.8. Screen 18 marked ⚠️ API 미정의 in front-list — correct annotation, not a defect.

---

## 5. Check 4 — screen-flow vs modal-list

### Modal IDs Referenced in screen-flow (full scan)

| screen-flow Section | Modal IDs |
|--------------------|-----------|
| Section 3 (Track Discovery) | M-12 |
| Section 4 (Playlist) | M-05, M-07, M-13 |
| Section 5 (Album) | M-06, M-08 |
| Section 6 (Cart) | M-22 |
| Section 7 (Subscription) | M-09, M-10, M-26, M-27 |
| Section 8 (My Page) | M-01, M-02, M-15, M-16, M-17, M-18, M-19, M-20, M-21, M-23, M-24, M-25 |
| Section 9 (Q&A/Notice) | M-16, M-19, M-20 |
| Section 10 (Admin) | M-03, M-04, M-11, M-17, M-18, M-24, M-25, M-28 |

All M-xx IDs confirmed within M-01~M-28 range, all exist in modal-list.

### MAJOR Finding

**MAJOR-002 — M-17 component name: StatusModal in screen-flow vs ReviewModal in modal-list**

- `docs/ui/screen-flow.md:282` — `[K-5 기업인증 심사]  "심사 처리" → [M-17 StatusModal] → 화면 갱신`
- `docs/ui/modal-list.md:88` — M-17 row: Component = `ReviewModal`
- Component definitions in modal-list header (`docs/ui/modal-list.md:46-54`):
  - `StatusModal`: 안내 텍스트 + `[확인]` 1-button (informational only)
  - `ReviewModal`: 상태 드롭다운 + adminNote 텍스트입력 + `[취소]` `[처리]` 2-button
- These are structurally different UI components. StatusModal cannot perform APPROVED/REVISION_REQUESTED/REJECTED state selection plus adminNote input.
- Impact: If a developer implements this screen using StatusModal, the admin review workflow would be functionally broken.

---

## 6. Check 5 — modal-list vs api-spec

### MINOR Findings (path parameter naming)

**MINOR-007 — M-05, M-12: {id} vs {playlistId} (api-spec §3.4)**

- `docs/ui/modal-list.md:68` (M-05) — `3.4 POST /api/playlists/{id}/tracks`
- `docs/ui/modal-list.md:83` (M-12) — `3.4 POST /api/playlists/{id}/tracks`
- `docs/design/api-spec.md:484-498` — §3.4 URL: `POST /api/playlists/{playlistId}/tracks`

**MINOR-008 — M-07, M-13: {id} vs {playlistId} (api-spec §3.8)**

- `docs/ui/modal-list.md:69` (M-07) — `3.8 DELETE /api/playlists/{id}`
- `docs/ui/modal-list.md:84` (M-13) — `3.8 DELETE /api/playlists/{id}`
- `docs/design/api-spec.md:546-552` — §3.8 URL: `DELETE /api/playlists/{playlistId}`

**MINOR-009 — M-11: {id} vs {trackId} (api-spec §1.7)**

- `docs/ui/modal-list.md:82` — `1.7 DELETE /api/tracks/{id}`
- `docs/design/api-spec.md:282-289` — §1.7 URL: `DELETE /api/tracks/{trackId}`

**MINOR-010 — M-18, M-21, M-24, M-25, M-28: {id} vs semantic param names**

| Modal | modal-list Line | URL in modal-list | api-spec Param |
|-------|-----------------|-------------------|----------------|
| M-18 | 89 | `8.6 PUT /api/questions/{id}/status` | `{questionId}` (api-spec:1183) |
| M-21 | 92 | `12.4 DELETE /api/whitelist-channels/{id}` | `{channelId}` (api-spec:1447) |
| M-24 | 95 | `6.9 DELETE /api/user-subscriptions/{id}` | `{userSubscriptionId}` (api-spec:983) |
| M-25 | 96 | `5.8 PUT /api/users/{id}` | `{userId}` (api-spec:793) |
| M-28 | 99 | `2.4 DELETE /api/tags/{id}` | `{tagId}` (api-spec:397) |

Pattern: All three check documents (front-list, modal-list, screen-flow) consistently use `{id}` as shorthand for all path parameters. This is a document convention, not a per-file anomaly. A single cleanup pass to align to api-spec parameter names is recommended.

### All Other Modal API References: Confirmed Valid

M-01, M-02, M-03, M-04, M-06, M-08, M-09, M-10, M-14, M-15, M-16, M-17, M-19, M-20, M-22, M-23 — section numbers and HTTP methods confirmed matching api-spec. M-26/M-27 marked ⚠️ PG 보류 — deferred by design, not a defect.

---

## 7. Complete Issue Registry

| ID | Severity | Check | File | Line(s) | Description |
|----|----------|-------|------|---------|-------------|
| MAJOR-001 | MAJOR | 3 | `front-list.md` | 25, 26 | `POST /api/download-queue` missing `/{trackId}` |
| MAJOR-001b | MAJOR | 3 (propagated) | `screen-flow.md` | 81, 87 | Same URL error from MAJOR-001 propagated |
| MAJOR-002 | MAJOR | 4 | `screen-flow.md` | 282 | M-17 component: `StatusModal` should be `ReviewModal` |
| MINOR-001 | MINOR | 1 | `screen-flow.md` | Section 4 (~line 98-101) | Screen 8 `[8 재생목록 생성]` ID block not labeled |
| MINOR-002 | MINOR | 1 | `front-list.md` + `screen-flow.md` | 134 / 282 | K-5 name: "기업 인증 목록 / 심사 처리" vs "기업인증 심사" |
| MINOR-003 | MINOR | 3 | `front-list.md` | 73 | `tracks/{id}/download` — should be `{trackId}` |
| MINOR-004 | MINOR | 3 | `front-list.md` | 51 | `playlists/{id}` (3.3) — should be `{playlistId}` |
| MINOR-005 | MINOR | 3 | `front-list.md` | 53 | `playlists/{id}` (3.5, 3.8) — should be `{playlistId}` (2 occurrences) |
| MINOR-006 | MINOR | 3 | `front-list.md` | 132 | `users/{id}/licenses/{id}` — should be `{userId}/{licenseId}` |
| MINOR-007 | MINOR | 5 | `modal-list.md` | 68, 83 | M-05/M-12: `playlists/{id}/tracks` — should be `{playlistId}` |
| MINOR-008 | MINOR | 5 | `modal-list.md` | 69, 84 | M-07/M-13: `playlists/{id}` — should be `{playlistId}` |
| MINOR-009 | MINOR | 5 | `modal-list.md` | 82 | M-11: `tracks/{id}` — should be `{trackId}` |
| MINOR-010 | MINOR | 5 | `modal-list.md` | 89, 92, 95, 96, 99 | M-18/M-21/M-24/M-25/M-28: `{id}` → semantic param names |
| SUGGESTION-001 | SUGGESTION | — | `modal-list.md` | Section 3 | M-28 (태그 삭제) flow example not included in Section 3 "Screen Flow Examples" |

---

## 8. Fix Recommendation

### Immediate (before frontend kickoff)

1. `docs/ui/atstudio-front-list.md` lines 25–26: `POST /api/download-queue` → `POST /api/download-queue/{trackId}`
2. `docs/ui/screen-flow.md` lines 81, 87: Same URL correction
3. `docs/ui/screen-flow.md` line 282: `[M-17 StatusModal]` → `[M-17 ReviewModal]`

### Deferred (single cleanup WI after kickoff)

Group MINOR-001~010 as one cleanup WI. Primary action: align all `{id}` shorthand to api-spec semantic parameter names across front-list.md, modal-list.md, and screen-flow.md. Update front-list header API Spec version from v5 to v6.

---

## 9. Traceability

| Item | Value |
|------|-------|
| WI | WI-20260307-ATS-027 |
| REQ | REQ-20260307-ATS-009 |
| api-spec version verified | v6 (`docs/design/api-spec.md` line 1) |
| front-list version | v4 (`docs/ui/atstudio-front-list.md` line 3) |
| modal-list version | v1.2 (`docs/ui/modal-list.md` line 3) |
| screen-flow version | v1.2 (`docs/ui/screen-flow.md` line 19) |
| Validation date | 2026-03-08 |
| Agent | docops (claude-sonnet-4-6) |
| Files modified | 0 (read-only task) |

---

> Generated by: docops | WI-20260307-ATS-027 | Read-only validation — no files modified
