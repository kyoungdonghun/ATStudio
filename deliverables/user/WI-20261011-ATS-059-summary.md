---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: work-summary
status: draft
dependencies:
  - path: ../agent/WI-20261011-ATS-059-handoff.md
    reason: Approved local design scope
  - path: ../agent/WI-20261011-ATS-059-evidence-pack.md
    reason: Source pointers, implementation split and mutation ledger
  - path: REQ-20261011-ATS-007.md
    reason: Phased work and separate runtime gates
---

# WI-20261011-ATS-059 Summary

## Result

**Local design complete; no implementation or feature runtime performed.** The immediate route is Gmail SMTP reuse, selective audio-worker isolation and a narrow one-time first-admin/test-buyer provisioner. Only the two WI059 reports were created. WI055 remains read-only with no SMTP, and REQ007 remains open.

## Recommended Split

| Next local assignment | Concrete result |
|---|---|
| SE-A: isolate audio processing | Add a default-off audio-only mode. Preserve the master OFF switch; suppress all six scheduled jobs and startup storage recovery while the existing single-thread audio worker runs. Preserve legacy behavior when the new mode is absent. |
| SE-B: prepare two one-time operator actions | Reuse standalone JDBC/private-input/one-shot receipt patterns. Promote only the designated already verified USER when no ADMIN exists, with a transactional role audit. Separately create at most one test-buyer entitlement without any payment or billing agreement. No public endpoint, startup bootstrap, schema change or seed replay. |
| SE-C: small synthetic media preparation | Two 10-second WAVs and local deterministic/hash checks. Keep current SMTP implementation and authentication/download regressions; no Naver adapter or mail framework now. |
| RE/PG, then MA | Independently verify isolation, exact DML/receipts, concurrency, privacy and stop controls. MA prepares a separate mutation-aware feature window only after WI058 PASS and the outstanding web/security gates. |

Exact files and tests are in the [Evidence Pack](../agent/WI-20261011-ATS-059-evidence-pack.md). These are implementation assignments for new approved handoffs, not actions executed by SA or authority to operate the server.

## Important Boundaries

- The audio worker still scans the global queue and recovers interrupted claims. Start only after proving no pre-existing pending/processing/claimed work or unresolved storage-recovery debt, with competing writers fenced. This is scheduler isolation, not per-Track isolation.
- First admin is not a signup classification or subscription. Signup/real verification comes first; missing/unverified/deleted accounts cause HOLD. Existing ADMIN means no fresh bootstrap. Exact completed receipt plus unchanged state is a zero-write replay; any drift or uncertain commit requires reconciliation, never automatic retry.
- ADMIN downloads bypass buyer entitlement and cannot prove the buyer flow. Use a separately designated verified USER, matching existing plan, explicit dates and no payment history. Existing membership must not be overwritten; its adjustment uses the existing Local Subscription Correction flow under separate scope.
- The first entitlement grant uses a protected operator receipt, not a fabricated payment/correction record. PG acceptance of this audit coverage and a real disposable-MySQL concurrency test are required before native use.

## Mail Decision

Reuse the existing tested Gmail SMTP path for the bounded trial. New-host sender/TLS/origin and actual receipt still need verification; an HTTP success does not prove delivery. At most one first-admin verification message and one reset-link message, plus one buyer verification message only after separate designation. No password reset submission or automatic resend.

MA's approximately20:05 KST provider UI observation allows only global TCP80/443/53 and UDP53/123, not Gmail TCP587. Therefore **mail stage HOLD** until destination/port-scoped SMTP egress is reviewed and explicitly authorized at both provider and app layers, with a finite window and rule restoration. No firewall change occurred. Local Gmail reference presence is confirmed by MA, but native credentials were not copied and SEND is NOT RUN. No values are needed for this design, and WI055 remains unchanged.

Naver SENS remains a possible later migration on a client-owned account. No account creation, subscription, activation, pricing/free-quota promise or Naver local implementation is authorized. A future selected-provider task changes only the transport boundary while preserving token/template behavior and SMTP compatibility; it must verify the then-current SENS MAIL API contract. No speculative interface extraction is needed for Gmail.

## Inputs Still Needed

The first-admin/signup/reset recipient is already designated privately, and synthetic WAVs are accepted. Do not request those choices again or copy their values into reports.

1. Private signup profile, account classification and actual required consents; a distinct non-ADMIN buyer designation.
2. Existing plan/quota, exact start/expiry and retention choice for one non-payment test entitlement.
3. Existing SMTP configuration/sender reference owner and person opening links from the allowed network. No credential values in chat or repository.
4. Staffed test/stop window, private execution references and recoverable prechange evidence. These are not supplied by the earlier read-only web trial.

## Budget And Closeout

Retain WI057's <=15-minute/64MiB aggregate-wire ceiling, stopping admission at48MiB. At most two accounts, one admin promotion, one buyer entitlement, three emails, two Tracks, two first Official Downloads and two re-downloads. Two originals total3,528,088bytes; allow32MiB media/temp and32MiB DB/log growth. No backup/load overlap or capacity claim.

The ledger includes consents, verification/reset tokens, login refresh state, role audit, membership, processing state, files and storage journals. Public Listening must add no License or Download History. A first Official Download may write accounting before transfer completes; reconcile before retrying. These are planned deltas, not writes performed.

Close admission and stop the owned worker/application; restore master=false before removing audio-only mode. Preserve accounts, original media, Licenses and evidence. Expiry/CANCELLED status or logout is not complete access revocation; keep restricted ingress and approve deactivation of only the two owned Tracks before later restart. No destructive cleanup or sole-admin demotion is assumed.

## Verification

Evidence owner: SA, local Windows source inspection, 2026-10-11. WI057's dated local641/336/143 identity evidence is reused, not rerun. No private configuration, remote target, mail, DB, provider, build or product test was accessed/executed in WI059. Local report checks do not establish feature or production acceptance.
