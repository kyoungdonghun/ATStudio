# User — Whitelist Channels Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 12 (Whitelist Channels)
> **DB Reference**: `docs/design/db-schema.md` Section 9 (`whitelist_channels`)
>
> **Whitelist Channel Concept**: A feature that allows subscribers to register the YouTube channels where they will use tracks. The maximum number of channels that can be registered is limited by the subscription plan (`subscriptions.max_whitelist_channels`).

---

## WL-001: Register Channel [New]

| Field | Value |
|-------|-------|
| **Code** | WL-001 |
| **Version** | 26-02-20 |
| **Description** | A logged-in subscriber registers a YouTube channel to the whitelist. |
| **Actor** | User (subscriber), Backend |
| **Preconditions** | Logged in. Has active subscription (user_subscriptions.status=ACTIVE). Current registered channel count < max_whitelist_channels. |
| **Trigger** | User clicks the 'Register Channel' button. |
| **Related UC** | WL-002 (list), PAYMENT-006 (my subscription) |

**Main Flow**
1. User enters the channel URL (channelUrl, required) and channel name (channelName, required).
2. Frontend sends the input to the backend.
3. Backend validates that channelUrl contains `youtube.com` (loose format check).
4. Backend verifies that the user has an active subscription.
5. Backend checks that the current number of registered channels is less than max_whitelist_channels.
6. Backend creates a whitelist_channels record and returns 201 Created.

**Exception / Alternative Flow**
- Invalid channel URL (not youtube.com): 400 `INVALID_ARGUMENT`.
- No active subscription: 403 response.
- Channel count exceeded: 403 `WHITELIST_CHANNEL_LIMIT_EXCEEDED`.

**Postconditions**
- A record is created in the whitelist_channels table.

---

## WL-002: List My Channels [New]

| Field | Value |
|-------|-------|
| **Code** | WL-002 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member retrieves their whitelist channel list. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User accesses the 'Whitelist Channel Management' screen. |
| **Related UC** | WL-001 (register), WL-003 (update), WL-004 (delete) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend.
2. Backend extracts userId from the JWT and queries the user's whitelist_channels list.
3. Backend returns the channel list (id, channelUrl, channelName, createdAt).

**Postconditions**
- Whitelist channel list displayed on screen.

---

## WL-003: Update Channel [New]

| Field | Value |
|-------|-------|
| **Code** | WL-003 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member updates their whitelist channel information. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target channel must belong to the logged-in user. |
| **Trigger** | User clicks the 'Edit' button for a specific channel in the channel list. |
| **Related UC** | WL-002 (list) |

**Main Flow**
1. User changes the channel URL (channelUrl) and channel name (channelName).
2. Frontend sends channelId and the updated data to the backend.
3. Backend verifies that the channel belongs to the user.
4. Backend updates the whitelist_channels record and returns 200 OK.

**Exception / Alternative Flow**
- Attempt to update another user's channel: 403 response.

**Postconditions**
- Updated channel info reflected in DB.

---

## WL-004: Delete Channel [New]

| Field | Value |
|-------|-------|
| **Code** | WL-004 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member deletes their whitelist channel. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target channel must belong to the logged-in user. |
| **Trigger** | User clicks the 'Delete' button for a specific channel in the channel list. |
| **Related UC** | WL-002 (list) |

**Main Flow**
1. User clicks the 'Delete' button and confirms.
2. Frontend sends a delete request with channelId to the backend.
3. Backend verifies that the channel belongs to the user.
4. Backend deletes the whitelist_channels record and returns 204 No Content.

**Exception / Alternative Flow**
- Attempt to delete another user's channel: 403 response.

**Postconditions**
- The channel record is deleted from the whitelist_channels table.
