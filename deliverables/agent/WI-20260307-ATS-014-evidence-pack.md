# WI-20260307-ATS-014 Evidence Pack

**WI ID**: WI-20260307-ATS-014
**REQ**: REQ-20260307-ATS-008
**Agent**: docops
**Date**: 2026-03-07
**Status**: DONE

---

## Acceptance Criteria Verification

### [user-subscription.md]

| Criterion | Status | File:Section |
|-----------|--------|-------------|
| PAYMENT-007 UPGRADE/DOWNGRADE branch added | PASS | `user-subscription.md` — PAYMENT-007 |
| UPGRADE: immediate application + proratedAmount | PASS | `user-subscription.md` — PAYMENT-007, Main Flow UPGRADE, step 5 |
| DOWNGRADE: pendingSubscriptionId + pendingBillingCycle stored | PASS | `user-subscription.md` — PAYMENT-007, Main Flow DOWNGRADE, step 5 |
| DOWNGRADE: current period active until expiresAt, auto-applied at expiresAt | PASS | `user-subscription.md` — PAYMENT-007, Main Flow DOWNGRADE, steps 7–8, Postconditions |
| PAYMENT-010: grace period policy applied | PASS | `user-subscription.md` — PAYMENT-010 |
| PAYMENT-010: status=CANCELLED, expiresAt unchanged, service until expiresAt | PASS | `user-subscription.md` — PAYMENT-010, Main Flow step 6, Postconditions |

### [util.md]

| Criterion | Status | File:Section |
|-----------|--------|-------------|
| UTIL-006 response includes nextResetAt | PASS | `util.md` — UTIL-006, Main Flow step 3 |
| nextResetAt defined as tomorrow 00:00 LocalDateTime | PASS | `util.md` — UTIL-006, Main Flow step 3 (inline note) |
| UTIL-013 new UC added | PASS | `util.md` — UTIL-013 |
| UTIL-013 Actor: subscriber, precondition: active subscription | PASS | `util.md` — UTIL-013, header table |
| UTIL-013 API: GET /api/utils/subscription-change-preview?subscriptionId=X&billingCycle=Y | PASS | `util.md` — UTIL-013, Main Flow step 1 |
| UTIL-013 UPGRADE/DOWNGRADE determination by price comparison | PASS | `util.md` — UTIL-013, Main Flow step 3 |
| UTIL-013 proratedAmount calculation documented | PASS | `util.md` — UTIL-013, Main Flow step 4 |
| UTIL-013 response fields: changeType, proratedAmount, effectiveDate, newPlanName, newBillingCycle | PASS | `util.md` — UTIL-013, Response Fields section |

### [company-certification.md]

| Criterion | Status | File:Section |
|-----------|--------|-------------|
| CC-001 Preconditions — "Reapplication allowed" phrase removed | PASS | `company-certification.md` — CC-001, header table, Preconditions field |
| CC-001 Preconditions — initial version no-UI-reapplication policy stated | PASS | `company-certification.md` — CC-001, header table, Preconditions field |
| CC-001 Preconditions — admin-guided process stated | PASS | `company-certification.md` — CC-001, header table, Preconditions field |
| CC-001 Preconditions — future automation note included | PASS | `company-certification.md` — CC-001, header table, Preconditions field |

### [sound-playlist.md]

| Criterion | Status | File:Section |
|-----------|--------|-------------|
| SOUND-002 Exception/Alternative Flow: 3-limit exception added | PASS | `sound-playlist.md` — SOUND-002, Exception/Alternative Flow |
| 409 PLAYLIST_LIMIT_EXCEEDED referenced | PASS | `sound-playlist.md` — SOUND-002, Exception/Alternative Flow |
| Frontend pre-emption (button hide) documented | PASS | `sound-playlist.md` — SOUND-002, Exception/Alternative Flow |

### [sound-track.md]

| Criterion | Status | File:Section |
|-----------|--------|-------------|
| SOUND-019 not previously defined in sound-track.md (pre-check) | CONFIRMED | `sound-track.md` — not found before this WI |
| SOUND-019 entry added to sound-track.md | PASS | `sound-track.md` — SOUND-019 |
| Actor: subscriber, API: POST /api/playlists/{id}/tracks | PASS | `sound-track.md` — SOUND-019, header table + Main Flow step 4 |
| Trigger: track list / track detail "Add to Playlist" button | PASS | `sound-track.md` — SOUND-019, header table Trigger field |
| SelectModal flow documented | PASS | `sound-track.md` — SOUND-019, Main Flow steps 2–3 |
| Canonical definition pointer to sound-playlist.md included | PASS | `sound-track.md` — SOUND-019, trailing Note |

---

## Numbering Decision Log

**Issue**: Handoff specified "UTIL-007: 구독 변경 미리보기". UTIL-007 is already occupied ("Check Member Type", version 26-02-20).

**Resolution**: New UC assigned code **UTIL-013** (next available after UTIL-012). PAYMENT-007 Related UC field updated to reference UTIL-013 accordingly. No renaming of existing UTIL-007.

**Rationale**: UC codes are stable identifiers. Reusing an occupied code would break existing cross-references. UTIL-013 is unambiguous and does not conflict with any existing or removed UC code.

---

## Scope Compliance

| Rule | Status |
|------|--------|
| Only 5 target files modified | PASS |
| api-spec.md not modified | PASS |
| front-list, modal-list, screen-flow not modified | PASS |
| Backend code not modified | PASS |
| No changes beyond those specified in handoff | PASS |

---

## Format/Style Compliance

| Check | Status |
|-------|--------|
| Existing UC table format maintained (Code/Version/Description/Actor/Preconditions/Trigger/Related UC) | PASS |
| Version field updated to 26-03-07 for changed UCs | PASS |
| New UCs use "[New]" label in heading | PASS |
| SOUND-019 in sound-track.md uses "[Cross-reference]" label to distinguish from canonical definition | PASS |
| No emojis introduced | PASS |
| English language maintained (documentation language policy) | PASS |

---

## Follow-up Items (Out of Scope for This WI)

| Item | Priority | Suggested Owner |
|------|----------|-----------------|
| UTIL-013 backend implementation (GET /api/utils/subscription-change-preview) | Medium | se |
| PAYMENT-007 DOWNGRADE backend implementation (pendingSubscriptionId/pendingBillingCycle fields + auto-apply scheduler) | Medium | se, sa |
| api-spec.md Section 14 update: add UTIL-013 endpoint | Medium | uv or docops |
| api-spec.md Section 6: update PAYMENT-007 description and PAYMENT-010 cancellation policy | Low | uv or docops |
| DB schema: add pending_subscription_id, pending_billing_cycle columns to user_subscriptions | Medium | sa |

---

## Files Modified

| File | Change Type |
|------|-------------|
| `docs/design/usecase/user-subscription.md` | Update (PAYMENT-007, PAYMENT-010) |
| `docs/design/usecase/util.md` | Update (UTIL-006) + Add (UTIL-013) |
| `docs/design/usecase/company-certification.md` | Update (CC-001 Preconditions) |
| `docs/design/usecase/sound-playlist.md` | Update (SOUND-002 Exception Flow) |
| `docs/design/usecase/sound-track.md` | Add (SOUND-019 cross-reference entry) |

---

## Deliverables

| Deliverable | Path |
|-------------|------|
| User-facing summary | `deliverables/user/WI-20260307-ATS-014-summary.md` |
| Agent-facing evidence pack | `deliverables/agent/WI-20260307-ATS-014-evidence-pack.md` |
| Handoff packet | `deliverables/agent/WI-20260307-ATS-014-handoff.md` |
