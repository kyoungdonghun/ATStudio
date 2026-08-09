---
version: 2.3
last_updated: 2026-08-09
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: 1-quick-checklist.md
    reason: First-pass test sequence
  - path: 4-sr-format.md
    reason: Issue reporting format
  - path: ../ui/atstudio-front-list.md
    reason: Current screen and workflow source
---

# AT.M Client Testing Guide

## Before Testing

- Use only the URL supplied by the operator for the current acceptance run.
- Do not reuse a URL from a screenshot, ZIP, old message, or historical report.
- Keep personal, business, and ADMIN test accounts separate.
- Do not use real cards, real personal data, or real business documents.
- Use Toss test configuration only.
- Do not repost the test URL in a public channel.

The official V1 baseline branch is `codex/p1-acceptance-hardening`; its current
frontend install resolves Vite 6.4.3. No separate client-demo branch is
maintained. This is not by itself proof that a public environment is current.
The operator must verify the local page, proxied API, and newly issued public
URL for each acceptance run.

## Recommended Order

1. Run the quick checklist for login, playback, subscription, and primary ADMIN
   screens.
2. Run the full feature checklist for detailed workflows.
3. Run the ADMIN checklist with an ADMIN account.
4. Report each issue using the SR format.

## Current Behavior Notes

### Play History

Play History is stored in the current browser under `localStorage` key
`playHistory`.

- Recording starts only after playback starts.
- Replaying a Track moves it to the newest position.
- At most 100 Tracks are retained.
- Clearing browser storage removes this history.
- Another browser or device does not receive it.
- No server Play History API or table participates.

### Downloads

The subscriber route is `/downloads`. It shows Official Download history from
`/api/downloads/history`. License, plan quota, download history, and Track
count rules remain enforced by the backend. There is no Download Queue screen
or API.

### Subscription Payment

Subscription payment uses Toss card recurring billing only.

- Checkout starts at `/subscriptions/checkout`.
- A new subscription charges the first period after billing-key issue.
- Payment-method registration uses a zero-amount billing-agreement order.
- Upgrade may charge immediately; downgrade/cycle-only changes are scheduled.
- Removed payment aliases do not redirect to another checkout.
- Provider identity shown by current APIs is `TOSS`.

### ADMIN Payment Operations

`/admin/payments` provides payment ledgers, incidents, receipts, audit logs,
refunds, entitlement corrections, and settlement operations. Typed
confirmation is required for emergency mutation flows. ADMIN subscription
correction is a separate local preview/request/approve/execute workflow. It
does not charge, refund, or delete a billing key through Toss. A UI success
message confirms only the local API workflow result; payment/provider evidence
must be checked in the corresponding payment ledger and provider environment.

### Current Acceptance Boundary

Focused automated evidence exists for SR-94 through SR-101, but full browser
acceptance, a real existing-row audio dry-run/backfill, production deployment,
and live provider actions are not completed by that evidence.

## Reporting

Record:

- test account role, without passwords or tokens;
- current route;
- exact action;
- expected and actual result;
- timestamp;
- screenshot with personal or payment data redacted.

Never include passwords, access tokens, billing keys, provider secrets, raw
card data, ignored local configuration, or credential-file paths.
