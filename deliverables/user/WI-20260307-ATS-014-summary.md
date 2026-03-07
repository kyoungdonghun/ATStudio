# WI-20260307-ATS-014 — Usecase Consistency Patch (Phase 3)

**WI ID**: WI-20260307-ATS-014
**REQ**: REQ-20260307-ATS-008
**Date**: 2026-03-07
**Status**: DONE

---

## What Changed

5 usecase files were updated to align with implemented backend behavior and confirmed product decisions. No backend code was modified.

---

## Changes by File

### 1. `docs/design/usecase/user-subscription.md`

**PAYMENT-007 (Change My Subscription)**
- Before: Single flow — immediate application with proratedAmount for all plan changes.
- After: Split into two explicit flows.
  - UPGRADE (new plan price > current): Immediate application. PG payment for proratedAmount. New benefits active at once.
  - DOWNGRADE (new plan price equal or lower): Deferred. pendingSubscriptionId + pendingBillingCycle stored. Current plan stays active until expiresAt. New plan applied automatically at expiresAt. No PG payment at change time.
- Related UC updated: UTIL-013 added as preview step.

**PAYMENT-010 (Cancel My Subscription)**
- Before: "Immediate cancellation. Benefits stop at once."
- After: Status set to CANCELLED immediately, but expiresAt is unchanged. Service (downloads, channel registration, playlists) remains available until expiresAt. Benefits terminate automatically at expiresAt.
- Frontend advisory text updated to reflect grace period.

---

### 2. `docs/design/usecase/util.md`

**UTIL-006 (Check Download Count)**
- Response fields: Added `nextResetAt` (tomorrow 00:00:00 as LocalDateTime).
- Postconditions: Added note on frontend use of `nextResetAt`.

**UTIL-013 — New UC: Subscription Change Preview**
- Code: UTIL-013 (UTIL-007 was already in use for "Check Member Type").
- API: `GET /api/utils/subscription-change-preview?subscriptionId=X&billingCycle=Y`
- Actor: Subscriber (logged in, active subscription required).
- Returns: changeType (UPGRADE/DOWNGRADE), proratedAmount, effectiveDate, newPlanName, newBillingCycle.
- No DB state changes. Read-only preview endpoint.

---

### 3. `docs/design/usecase/company-certification.md`

**CC-001 Preconditions**
- Before: "Reapplication allowed after REJECTED or REVISION_REQUESTED."
- After: "Initial version: no UI re-application flow after REJECTED or REVISION_REQUESTED. Admin guides the member directly via email or 1:1 inquiry. Automated re-application flow planned after site stabilization."

---

### 4. `docs/design/usecase/sound-playlist.md`

**SOUND-002 (Create Playlist) — Exception/Alternative Flow**
- Before: Empty ("- -").
- After: Active playlist count at 3 returns 409 `PLAYLIST_LIMIT_EXCEEDED`. Frontend pre-empts by hiding the 'Create Playlist' button when 3 active playlists exist.

---

### 5. `docs/design/usecase/sound-track.md`

**SOUND-019 (Add Track to Playlist) — Added as cross-reference entry**
- SOUND-019 is canonically defined in `sound-playlist.md`.
- An entry was added to `sound-track.md` to document the trigger-side UX flow (track list Screen 1/3, track detail B-1).
- Actor: Subscriber. API: `POST /api/playlists/{id}/tracks`. Trigger: "Add to Playlist" button on track list or detail.
- Canonical definition pointer included.

---

## Approval Points

No approval is required for this WI. All changes reflect product decisions already confirmed in REQ-20260307-ATS-008. No backend code, DB schema, or API spec was modified.

---

## Impact

| Area | Impact |
|------|--------|
| Backend code | None |
| DB schema | None |
| API spec | None (UTIL-013 endpoint to be added in separate WI if backend implementation is needed) |
| Frontend | PAYMENT-007 split flow and PAYMENT-010 grace period wording are input for frontend screen spec |
| Other usecase files | None |
