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
| `CANCELLED` | Request was cancelled before external registration | No |
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
3. Backend validates that `channelUrl` host is exactly `youtube.com` or ends with `.youtube.com`.
4. Backend creates a `whitelist_channels` row with status `DRAFT`.
5. If this is the user's first saved channel, backend marks it as `is_primary = true`.
6. Backend returns the saved channel response.

**Exception / Alternative Flow**
- Invalid channel URL: 400 `INVALID_ARGUMENT`.

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
2. Backend extracts userId from JWT and queries the user's channels ordered by primary flag and creation time.
3. Backend returns channel metadata, status, primary flag, operator note, and status timestamps.
4. Frontend displays saved channels and the registration slot usage based on plan-counting statuses.

**Postconditions**
- The user can see draft, requested, exported, registered, revision, rejected, cancelled, and removal-requested channels in one list.

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
3. Backend validates ownership and channel URL.
4. Backend updates the channel.
5. If a previously exported/registered/revision-requested channel is edited, backend treats the edit as a reprocessing request.
6. For that reprocessing path, backend checks active subscription and plan limit using the same self-slot exclusion rule as WL-005.
7. If allowed, backend moves the channel back to `PENDING` for operator reprocessing.

**Exception / Alternative Flow**
- Attempt to update another user's channel: 403 `RESOURCE_NOT_ACCESS`.
- Invalid channel URL: 400 `INVALID_ARGUMENT`.
- No active subscription when the update would requeue an externally processed channel: 403 `NO_ACTIVE_SUBSCRIPTION`.
- Plan limit exceeded when the update would requeue an externally processed channel: 403 `WHITELIST_CHANNEL_LIMIT_EXCEEDED`.

**Postconditions**
- Updated channel info is reflected in DB and may require operator reprocessing depending on prior status.

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
6. Operator handles the external removal and updates status later.

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
4. Backend rejects direct request attempts from `EXPORTED`, `REGISTERED`, or `REMOVAL_REQUESTED`.
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
3. Admin changes a channel to `REGISTERED`, `REVISION_REQUESTED`, `REJECTED`, `REMOVAL_REQUESTED`, or `CANCELLED`.
4. Backend records the admin, note, and processed timestamp.

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
1. Admin calls `POST /api/admin/whitelist-channels/export`.
2. Backend loads channels in the selected status. Keyword filtering from the admin list view is not applied to CSV export in the current implementation.
3. Backend creates a `whitelist_export_batches` row and `whitelist_export_items` snapshot rows.
4. Backend returns a UTF-8 BOM CSV file with `userEmail`, user/channel metadata, plan, billing cycle, requested time, and exported time.
5. If the exported status is `PENDING`, backend marks exported channels as `EXPORTED`.
6. If the exported status is not `PENDING`, backend keeps the current workflow status and records only the CSV/export snapshots.

**Postconditions**
- The CSV can be handed to the external processing party and the local export is auditable.
