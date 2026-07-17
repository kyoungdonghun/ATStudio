---
version: 2.0
last_updated: 2026-07-17
project: ATS
owner: docops
category: design
status: stable
dependencies:
  - path: ../api-spec.md
    reason: Current subscription and payment routes
  - path: ../payment-integration-design.md
    reason: Recurring payment lifecycle
  - path: ../db-schema.md
    reason: Subscription persistence
---

# User - Subscription Use Cases

## PAYMENT-001: Subscribe

1. A USER selects an active plan and cycle.
2. BUSINESS users must have approved company certification.
3. `/subscriptions/checkout` starts Toss billing auth.
4. The backend issues and encrypts the billing key.
5. The first recurring charge succeeds.
6. Only then does the backend create or activate `user_subscriptions`.

There is no direct subscription-creation endpoint or alternate checkout path.

## PAYMENT-002: List Plans

- `GET /api/subscriptions`: public active plans.
- `GET /api/subscriptions/{subscriptionId}`: public plan detail.
- `GET /api/subscriptions/admin`: ADMIN list including inactive plans.

The six runtime baseline plans are owned by `seed.sql`.

## PAYMENT-004: List Member Subscriptions (ADMIN)

`GET /api/user-subscriptions` returns the ADMIN subscription list used by the
management screen. There is no separate ADMIN detail-read endpoint.

## PAYMENT-006: View My Subscription

`GET /api/user-subscriptions/me` returns the current service-enabled
subscription. Service-enabled includes `ACTIVE` and a valid `CANCELLED`
grace period before `expiresAt`.

## PAYMENT-007: Change My Subscription

`PUT /api/user-subscriptions/me` applies these rules:

- upgrade: charge the prorated difference through the active billing agreement,
  then apply the higher plan;
- downgrade or cycle-only change: store a pending change for the next successful
  renewal;
- current plan/cycle: clear a pending change;
- removed/invalid billing key: expire local agreement metadata and require
  payment-method registration without mutating the subscription.

## PAYMENT-008/009: Emergency ADMIN Control

- `PUT /api/user-subscriptions/{id}`
- `DELETE /api/user-subscriptions/{id}`

These authorized operations remain emergency controls. They are not a general
checkout or payment substitute.

## PAYMENT-010: Cancel My Subscription

`DELETE /api/user-subscriptions/me` stops future renewal while paid access
continues through `expiresAt`.

## PAYMENT-011: Reactivate Grace-Period Subscription

`POST /api/user-subscriptions/me/reactivate` reactivates a valid cancelled
grace-period subscription when its billing agreement is reusable.

## Provider Boundary

Persisted provider identity is `TOSS`. Recurring, lookup, and refund
interfaces remain provider-neutral, but V1 has no second active provider.
