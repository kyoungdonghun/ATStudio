---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-integ
category: evidence-pack
status: blocked
dependencies:
  - path: WI-20260724-ATS-015-handoff.md
    reason: Approved Work Item scope and safety boundaries
  - path: WI-20260724-ATS-024-evidence-pack.md
    reason: Corrected runtime prerequisite
  - path: ../../docs/payment/acceptance-test-checklist.md
    reason: Executable payment acceptance criteria
---

# Evidence Pack: WI-20260724-ATS-015

## Summary

- **Verdict: BLOCKED**
- The test-only key gate passed.
- The real Toss test billing-auth iframe opened and accepted a test
  card-registration submission.
- Post-submit browser observation was denied by the automation surface's
  security policy. No workaround or alternate browser surface was used.
- The prepared HTTPS loopback callback origin did not match the running HTTP
  loopback frontend origin.
- No backend Provider mutation was executed. Browser-side authorization
  outcome remains unknown.
- No product-code change was made.

## Scope / DoD

- [x] Test/live/unknown key gate completed without emitting values.
- [x] QA account and initial subscription state confirmed.
- [x] A real Toss test billing-auth path was attempted through the supported
  SDK surface.
- [x] Local order, agreement, payment, receipt, audit, refund, and Incident
  state was inspected through support-safe APIs.
- [x] Logs were checked for Provider, refund, mail, and restricted-value
  markers.
- [ ] Billing auth and first recurring charge completed.
- [ ] Representative plan change, cancellation, and reactivation completed.
- [ ] Refund request, approval, execution, and local/Provider parity completed.

The incomplete criteria are blocked, not passed or deferred as successful.

## Runtime

| Component | State |
|---|---|
| Source branch | `codex/v1-release-rehearsal-fixes` |
| Backend source commit | `677c3780f997f55b3e6f380e5e6c70113116b25c` |
| Backend | Loopback `8080`, restarted after QA credential rotation |
| Frontend | Loopback `15173`, retained |
| Database | WI-013 regex-guarded disposable MySQL database |
| Protected database access | 0 |
| Cloudflare | Not started |
| Mail | Not invoked |

The repo-external evidence directory is:

```text
C:\Users\jm991\AppData\Local\ATStudio\
release-rehearsal-runtime-3147873-20260724\
wi015-20260724T142846Z-16f958ec
```

Files:

| File | Purpose |
|---|---|
| `key-gate.json` | Secret-safe test/live/unknown classification |
| `billing-auth-automation.json` | Prepare, iframe, callback, and blocker result |
| `local-ledger-summary.json` | Support-safe local payment-state counts |
| `runtime-ownership.json` | Loopback listener and retained-runtime ownership |
| `credential-rotation.json` | QA-only credential remediation |
| `run-summary.json` | Final executed/blocked matrix |
| `final-validation.json` | Documentation, diff, secret scan, and retained-runtime gate |

No restricted environment value, bearer token, card value, auth key, customer
key, billing key, or exact Provider identifier is stored in this pack or the
repo-external evidence.

## Secret-Safe Gate

| Check | Result |
|---|---|
| Toss client key present | Yes |
| Toss secret key present | Yes |
| Client key classification | Test |
| Secret key classification | Test |
| Gate | `TEST_ONLY` |
| Raw value emitted or persisted | No |

Live or unknown classification would have stopped the Work Item before any
Toss interaction.

## QA State

| Fixture | Login | Role / type | Subscription |
|---|---:|---|---|
| New subscription | `200` | `USER` / `INDIVIDUAL` | None |
| Existing subscriber | `200` | `USER` / `INDIVIDUAL` | `ACTIVE` |
| Administrator | `200` | `ADMIN` / `INDIVIDUAL` | Not applicable |

The acceptance bootstrap remained explicitly enabled in the non-production
acceptance profile.

## Billing-Auth Attempt

### Preparation

| Field | Safe result |
|---|---|
| HTTP | `201` |
| Plan | `STANDARD` |
| Cycle | `MONTHLY` |
| Purpose | `SUBSCRIBE` |
| Amount | KRW 9,900 |
| Checkout | `TOSS_BILLING_AUTH` |
| Required checkout fields | Present |

Two `IN_PROGRESS` orders were created during this run. One was the intended
preparation. The other was created when an HTTP expression completed before a
strict runtime assignment diagnostic failed. A third `IN_PROGRESS` order
predated this resumed run. All three remain unconfirmed and subject to the
normal order-expiration job. They were not deleted because the shared
disposable database must remain available for later Work Items.

### External Test Screen

Observed:

- Toss Payments branding
- explicit non-charging test notice
- personal/corporate card selector
- card number, expiry, and identity fields
- required terms consent

The test form was submitted. Immediately afterward, the browser automation
surface rejected additional observation and interaction under its URL security
policy. The policy also prohibited workaround, indirect execution, or
switching to another browser surface.

Therefore:

- browser-side Provider authorization: **UNKNOWN**
- backend billing-key confirmation: **NOT CALLED**
- backend initial charge: **NOT CALLED**
- subscription activation: **NOT APPLIED**

## Callback Environment

| Property | Prepared callback | Running frontend |
|---|---|---|
| Scheme | HTTPS | HTTP |
| Host class | Loopback | Loopback |
| Port | `15173` | `15173` |
| Origin match | No | No |

This is an acceptance-runtime blocker. It is not evidence of a payment-domain
code defect, but the current runtime cannot receive the prepared callback.

## Local State

| Ledger | Count / state |
|---|---|
| Payment orders | 3, all `IN_PROGRESS` |
| Billing agreements | 1, `READY` |
| Subscription payments | 0 |
| Receipts | 0 |
| Refunds | 0 |
| Reconciliation Incidents | 0 |
| Current user subscription | None |

ADMIN read APIs returned successfully. Provider parity is not applicable
because no payment reached a finalized local state.

Backend runtime scan:

| Marker | Count |
|---|---:|
| Billing-key issue / Provider confirm | 0 |
| Recurring charge | 0 |
| Refund / payment cancel | 0 |
| Mail / SMTP delivery | 0 |
| Restricted value patterns | 0 |

## Credential Incident and Remediation

A transient browser DOM diagnostic included the disposable QA bootstrap
password after a failed local UI login. The value was not copied into a file,
deliverable, or repo-external evidence, but it was treated as exposed.

Remediation:

1. Cleared the browser form immediately.
2. Generated a new QA-only bootstrap password.
3. Replaced the restricted bundle value without printing it.
4. Preserved and rechecked the restricted file ACL.
5. Restarted the backend so the acceptance bootstrap rotated all QA fixtures.
6. Confirmed API login with the rotated value.

No production or retained credential was involved.

## Blocker Classification

| Blocker | Classification |
|---|---|
| Post-submit Toss page cannot be observed or controlled | Automation surface |
| Prepared HTTPS callback is not served by the HTTP frontend | Acceptance environment |
| Product payment implementation defect | Not found in the executed slice |

The correct verdict is `BLOCKED`, not `PASS` and not `FAIL`.

## Resume Contract

1. Keep the key gate fail-closed.
2. Provide a reachable HTTPS callback origin.
3. Use an operator-controlled Toss test browser for the external card step.
4. Start from a fresh prepared order.
5. Use only the newly finalized payment for upgrade, cancellation/reactivation,
   and refund verification.
6. Record receipt, audit, reconciliation, and Provider parity without exact
   Provider identifiers.

The shared runtime and disposable database remain intact for WI-016 and the
eventual WI-017 cleanup.

## Related Documents

- [WI-015 Handoff](WI-20260724-ATS-015-handoff.md)
- [WI-024 Evidence Pack](WI-20260724-ATS-024-evidence-pack.md)
- [WI-015 User Summary](../user/WI-20260724-ATS-015-summary.md)
- [Payment Acceptance Checklist](../../docs/payment/acceptance-test-checklist.md)
