# User / Admin — Whitelist Channels Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 12 (Whitelist Channels)
> **DB Reference**: `docs/design/db-schema.md` Section 9 (`whitelist_channels`, `whitelist_export_batches`, `whitelist_export_items`)
>
> **Whitelist Channel Concept**: A member can save multiple YouTube channel profiles and request whitelist registration for selected channels. The external YouTube/agency registration action is performed manually by an operator. Subscription plan limits apply to channels in registration-relevant states, not to all saved draft channels.

---

## Status Model

| Status | Meaning | Counts against plan limit |
|--------|---------|---------------------------|
| `DRAFT` | Saved by the user, not requested yet | No |
| `PENDING` | User requested whitelist registration | Yes |
| `EXPORTED` | Operator exported the request to CSV for external processing | Yes |
| `REGISTERED` | Operator confirmed external whitelist registration | Yes |
| `REVISION_REQUESTED` | Operator requested user-side correction | Yes |
| `REJECTED` | Operator rejected the request | No |
| `CANCELLED` | Operator confirmed completed external removal; terminal | No |
| `REMOVAL_REQUESTED` | User requested removal for an exported/registered channel | Yes |

---

## WL-001: Save Channel Draft

| Field | Value |
|-------|-------|
| **Code** | WL-001 |
| **Version** | 26-06-03 |
| **Description** | A logged-in member saves a YouTube channel profile. |
| **Actor** | User, Backend |
| **Preconditions** | Logged in. Active subscription is not required for saving a draft channel. |
| **Trigger** | User submits the channel form on `/whitelist-channels`. |
| **Related UC** | WL-002 (list), WL-005 (request registration) |

**Main Flow**
1. User enters `channelName`, `channelUrl`, and optional `youtubeHandle` / `youtubeChannelId`.
2. Frontend sends the input to the backend.
3. Backend parses and normalizes `channelUrl`, then accepts only HTTPS URLs with
   no user info, no explicit port other than standard HTTPS port 443, and a
   lowercase host equal to `youtube.com` or ending with `.youtube.com`.
4. Backend locks the owning user and rejects the save when the separate configured saved-row technical cap is already reached.
5. Backend creates a `whitelist_channels` row with status `DRAFT`.
6. If this is the user's first saved channel, backend marks it as `is_primary = true`.
7. Backend returns the saved channel response.

**Exception / Alternative Flow**
- Invalid channel URL: 400 `INVALID_ARGUMENT`.
- Saved-row technical cap reached: 403 `WHITELIST_CHANNEL_LIMIT_EXCEEDED`. This is independent of subscription registration slots.

**Postconditions**
- A draft channel profile exists and can be edited, deleted, set as primary, or submitted for registration.

---

## WL-002: List My Channels

| Field | Value |
|-------|-------|
| **Code** | WL-002 |
| **Version** | 26-06-03 |
| **Description** | A logged-in member retrieves their saved whitelist channel profiles. |
| **Actor** | User, Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User opens `/whitelist-channels`. |
| **Related UC** | WL-001, WL-003, WL-004, WL-005, WL-006 |

**Main Flow**
1. Frontend sends a request with the auth token.
2. Backend extracts userId from JWT and queries at most `APP_WHITELIST_MAX_SAVED_CHANNELS` rows ordered by primary flag and creation time.
3. Backend returns channel metadata, status, primary flag, operator note, and status timestamps.
4. Frontend displays saved channels and the registration slot usage based on plan-counting statuses. It applies the same safe-link predicate before creating an `href`; a retained unsafe value is shown as text only.

**Postconditions**
- Normal users see their bounded channel list in the existing response shape. A retained user above the technical cap sees only the deterministic primary-first/newest-first leading window; older rows are not claimed as returned.

---

## WL-003: Update Channel

| Field | Value |
|-------|-------|
| **Code** | WL-003 |
| **Version** | 26-06-03 |
| **Description** | A logged-in member updates a saved channel profile. |
| **Actor** | User, Backend |
| **Preconditions** | Logged in. Target channel belongs to the user. |
| **Trigger** | User edits a channel. |
| **Related UC** | WL-002, WL-005 |

**Main Flow**
1. User changes channel metadata.
2. Frontend sends `channelId` and updated data to the backend.
3. Frontend and backend apply the same HTTPS-only YouTube URL contract defined
   by WL-001. The backend remains authoritative.
4. Backend validates ownership and channel URL.
5. Backend updates the channel.
6. If a previously exported/registered/revision-requested channel is edited, backend treats the edit as a reprocessing request.
7. For that reprocessing path, backend checks active subscription and plan limit using the same self-slot exclusion rule as WL-005.
8. If allowed, backend moves the channel back to `PENDING` for operator reprocessing.

**Exception / Alternative Flow**
- Attempt to update another user's channel: 403 `RESOURCE_NOT_ACCESS`.
- Invalid channel URL: 400 `INVALID_ARGUMENT`.
- Attempt to edit `REMOVAL_REQUESTED` or terminal `CANCELLED`: 400 `INVALID_STATE_TRANSITION`; external removal metadata remains stable.
- No active subscription when the update would requeue an externally processed channel: 403 `NO_ACTIVE_SUBSCRIPTION`.
- Plan limit exceeded when the update would requeue an externally processed channel: 403 `WHITELIST_CHANNEL_LIMIT_EXCEEDED`.

**Postconditions**
- Updated channel info is reflected in DB and may require operator reprocessing depending on prior status.
- Subscriber and admin screens create a clickable link only when the persisted
  value still satisfies the current safe-link predicate. Retained unsafe values
  remain visible as text for operator correction and are never auto-mutated by
  the read path.

---

## WL-004: Delete Or Request Removal

| Field | Value |
|-------|-------|
| **Code** | WL-004 |
| **Version** | 26-06-03 |
| **Description** | A logged-in member deletes a locally manageable channel or requests external removal for a processed channel. |
| **Actor** | User, Backend, Operator |
| **Preconditions** | Logged in. Target channel belongs to the user. |
| **Trigger** | User clicks Delete / Removal Request. |
| **Related UC** | WL-002, WL-008 |

**Main Flow**
1. User confirms deletion/removal.
2. Backend validates ownership.
3. For `DRAFT`, `PENDING`, `REVISION_REQUESTED`, `REJECTED`, or `CANCELLED`, backend deletes the row.
4. If the deleted row was the primary channel and another saved channel remains, backend promotes one remaining channel as primary.
5. For `EXPORTED` or `REGISTERED`, backend keeps the row and changes status to `REMOVAL_REQUESTED`.
6. Repeating removal for `REMOVAL_REQUESTED` is idempotent and preserves the original request timestamp.
7. Operator completes external removal through `REMOVAL_REQUESTED -> CANCELLED`.

**Account Withdrawal Flow**
1. Backend deletes local-only `DRAFT`, `PENDING`, `REVISION_REQUESTED`, `REJECTED`, and `CANCELLED` rows.
2. Backend changes `EXPORTED` and `REGISTERED` rows to `REMOVAL_REQUESTED`.
3. Backend preserves existing `REMOVAL_REQUESTED` rows and clears primary flags before soft-deleting the user.

**Exception / Alternative Flow**
- Attempt to delete another user's channel: 403 `RESOURCE_NOT_ACCESS`.

**Postconditions**
- Locally manageable channels are removed. Externally processed channels remain auditable and enter the removal workflow.

---

## WL-005: Request Whitelist Registration

| Field | Value |
|-------|-------|
| **Code** | WL-005 |
| **Version** | 26-06-03 |
| **Description** | A subscriber requests external whitelist registration for a saved channel. |
| **Actor** | User, Backend |
| **Preconditions** | Logged in. Has active subscription. Target channel belongs to user. Plan-counting channel count is below `subscriptions.max_whitelist_channels`. |
| **Trigger** | User clicks Register Request. |
| **Related UC** | WL-001, WL-002, WL-007 |

**Main Flow**
1. Frontend calls `POST /api/whitelist-channels/{channelId}/request`.
2. Backend verifies ownership and active subscription.
3. If the channel is already `PENDING`, backend returns the current response idempotently.
4. Backend rejects direct request attempts from `EXPORTED`, `REGISTERED`, `REMOVAL_REQUESTED`, or terminal `CANCELLED`.
5. Backend counts channels in `PENDING`, `EXPORTED`, `REGISTERED`, `REVISION_REQUESTED`, and `REMOVAL_REQUESTED`.
6. If the target channel is already a counted correction state such as `REVISION_REQUESTED`, backend excludes that channel's own slot before comparing the count with the plan limit.
7. If the adjusted count is below the active plan limit, backend changes the channel status to `PENDING` and sets `requested_at`.
8. Backend returns the updated channel.

**Exception / Alternative Flow**
- No active subscription: 403 `NO_ACTIVE_SUBSCRIPTION`.
- Plan limit exceeded: 403 `WHITELIST_CHANNEL_LIMIT_EXCEEDED`.
- Invalid state transition: 400 `INVALID_STATE_TRANSITION`.

**Postconditions**
- The channel is ready for admin review/export.

---

## WL-006: Set Primary Channel

| Field | Value |
|-------|-------|
| **Code** | WL-006 |
| **Version** | 26-06-03 |
| **Description** | A member marks one saved channel as the representative channel. |
| **Actor** | User, Backend |
| **Preconditions** | Logged in. Target channel belongs to user. |
| **Trigger** | User clicks Primary Channel. |
| **Related UC** | WL-002 |

**Main Flow**
1. Frontend calls `PUT /api/whitelist-channels/{channelId}/primary`.
2. Backend clears the current primary channel for the user if one exists.
3. Backend marks the selected channel as primary.
4. Backend returns the updated channel.

**Exception / Alternative Flow**
- `REMOVAL_REQUESTED` or `CANCELLED`: 400 `INVALID_STATE_TRANSITION`.

**Postconditions**
- Exactly one saved channel is treated as the user's representative channel where possible.

---

## WL-007: Admin List / Process Channels

| Field | Value |
|-------|-------|
| **Code** | WL-007 |
| **Version** | 26-06-03 |
| **Description** | An admin reviews whitelist channel requests and updates their processing status. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin authenticated. |
| **Trigger** | Admin opens `/admin/whitelist-channels`. |
| **Related UC** | WL-005, WL-008 |

**Main Flow**
1. Admin filters channels by status and keyword.
2. Backend returns channel request data including `userEmail`, user nickname, channel metadata, active plan, billing cycle, and timestamps.
3. Backend accepts only the source-to-target transitions below and treats a repeated current status as idempotent.
4. Backend records the admin, note, and processed timestamp only for a real transition.
5. Invalidating a primary channel promotes the newest eligible channel by creation time and ID.

| Source | Allowed target |
|---|---|
| `DRAFT` | none |
| `PENDING` | `REGISTERED`, `REVISION_REQUESTED`, `REJECTED` |
| `EXPORTED` | `REGISTERED`, `REVISION_REQUESTED`, `REJECTED`, `REMOVAL_REQUESTED` |
| `REGISTERED` | `REVISION_REQUESTED`, `REMOVAL_REQUESTED` |
| `REVISION_REQUESTED` | `REGISTERED`, `REJECTED` |
| `REJECTED` | none |
| `REMOVAL_REQUESTED` | `CANCELLED` |
| `CANCELLED` | none |

**Postconditions**
- Manual external processing decisions are reflected in the local workflow.

---

## WL-008: Admin CSV Export

| Field | Value |
|-------|-------|
| **Code** | WL-008 |
| **Version** | 26-06-03 |
| **Description** | An admin exports requested channels to CSV for external registration processing. |
| **Actor** | Admin, Backend, External agency / YouTube operation |
| **Preconditions** | Admin authenticated. There are channels in the target export status, normally `PENDING`. |
| **Trigger** | Admin clicks CSV Export. |
| **Related UC** | WL-005, WL-007 |

**Main Flow**
1. Admin applies a status and/or keyword filter. The confirmation names that
   applied status and keyword, never unapplied draft keyword text. For an
   all-status keyword scope, it states that all matching statuses are included,
   matching `PENDING` rows become `EXPORTED`, and other statuses remain
   unchanged.
2. Admin calls `POST /api/admin/whitelist-channels/export` with that exact
   applied status/keyword scope.
3. Backend reads at most the configured hard maximum plus one candidate in deterministic `requestedAt`, `id` order.
4. An oversized selection fails before user/channel locks, status changes, or any partial batch.
5. For an accepted selection, backend locks distinct owning users in ID order and then locks the selected channel rows.
6. Backend creates an immutable `whitelist_export_batches` row with the recorded filters and ordered `whitelist_export_items` snapshots.
7. Backend returns a UTF-8 BOM CSV plus batch ID. CSV includes `userEmail` and operational channel/subscription fields but omits user ID and nickname.
8. Every selected channel whose status at export is `PENDING` becomes
   `EXPORTED`, including a `PENDING` row selected by an all-status keyword
   scope. Other selected statuses remain unchanged.
9. Admin may call `GET /api/admin/whitelist-channels/exports/{batchID}` to rebuild the same bytes from stored items without current-data re-query drift; the stored count and item query remain bounded by the configured maximum.

**Response-loss recovery flow**

1. A definitive export 4xx response is shown as a normal failure without a
   commit claim.
2. If the POST result is ambiguous, the frontend does not repeat the POST. It
   opts the POST out of shared authentication replay and calls
   `GET /api/admin/whitelist-channels/exports/recent` once with the exact applied
   status/keyword scope.
3. Backend trims and case-folds the keyword for scope matching, derives the
   owner from the authenticated ADMIN, rejects a trimmed keyword longer than
   100 characters before repository access, and returns at most 10 matching
   batch summaries ordered by `createdAt` and ID newest-first.
4. A summary contains only batch ID, filename, item count, recorded status and
   keyword, and creation time. It contains no item snapshots or CSV bytes.
5. The operator may explicitly replay a candidate through the existing batch
   download endpoint. Recovery itself creates no batch, status transition, or
   external handoff.
6. If the current channel-list request fails, the frontend clears rows,
   pagination, and pending row edits before showing the error. Older request
   completions cannot restore or erase newer list state.

**Postconditions**
- The CSV can be handed to the external processing party and the local export is auditable.
- An ambiguous response remains explicitly unknown until the operator evaluates
  the scoped candidate metadata; neither the lookup nor replay claims which
  candidate corresponds to the interrupted POST.
