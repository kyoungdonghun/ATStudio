---
version: 1.0
last_updated: 2026-10-24
project: ATS
owner: CR
category: work-summary
status: accepted
---
# WI-20261024-ATS-027: Local Review and Resume Checkpoint

Review completed with **two recovery defects; HOLD before stage-two execution**.
The exact unexecuted candidate is
`3fbbec606c60ac73c752d4f082965692f589311a413746a8bb938733a0070b46`.
This supersedes WI025's local allowance for this candidate; its historical review
and tests remain unchanged. No operating code was repaired in this review.

## Findings

| Priority | Finding and consequence | Minimum follow-up |
|---|---|---|
| P1 | Reconcile reuses the original apply nonce in its service/log name. Its log is created exclusively and retained. If the first reconcile fails after log creation, another fresh read-only attempt fails on the existing log before MySQL can start. The documented uncertain-COMMIT recovery sequence is therefore not reliably repeatable. | Keep the original apply claim/evidence binding, but give each reconciliation attempt its own bounded service/log identity. Retain earlier logs and claims. Prove failure-after-log-creation followed by a fresh successful read-only attempt, plus unchanged cleanup and no DML. |
| P2 | The write end-date guard also rejects read-only reconciliation after 2026-11-24 KST. A result left uncertain on the last authorized day cannot be checked through the documented path on November 25. | Keep the original write cutoff. Permit fresh, target-bound, read-only reconciliation of the original attempt after that cutoff. Test both sides of midnight and prove apply still rejects the expired date. |

Exact source lines, reproduction and limits are in the
[Evidence Pack](../agent/WI-20261024-ATS-027-evidence-pack.md).
Both conditions were reproduced locally; no DB or service ran. Existing 11 author
and 7 reviewer tests were inspected and reused, not replayed or mistaken for native proof.

## Confirmed boundaries

- Stage one remains complete within its bounded audio/reset scope. Nine retained
  artifact pins and the final restoration pin match. MA/humans own the actual
  server, UI and playback observations. Historical FAILED_LEDGER stays failed.
- Stage-two role/grant writes have not run. The MA pause receipt records original
  firewall unchanged, no SSH server access/DB/app/new backup/business writes,
  owned-key removal, agent Stopped/Disabled and provider logout. Current server
  cold state was not reobserved by this local review.
- Intended delta is still ID1 USER to ADMIN, ID2 ADMIN/ID3 USER preserved, grant1
  expiry through November 24 inclusive, two updated_at values and two truthful
  offline audit inserts. Existing start date, MONTHLY/CANCELLED, pending fields,
  six plans and unrelated rows remain protected by the whole-snapshot comparison.
- Four old column values support bounded correction, not full DB restoration.
  The earlier backup predates audio/reset. Buyer3's old saved password is forbidden;
  the new password was not stored and is unnecessary for this DB-only change.

## Resume after usage replenishment

1. On the user's resume instruction, prepare only the two minimal local repairs
   under a follow-up WI and independently review the new manifest. Complete the
   focused lifecycle/date checks before requesting key unlock. Do not replay stage one.
2. Verify actual candidate/source/runtime identities, current time and target
   admission. A reboot or other pin drift requires bounded diagnosis; never loosen
   historical bindings automatically. Issue fresh short-lived records, not old payloads.
3. At the real access boundary, complete the still-ungranted temporary current-PC
   TCP22 confirmation and direct key entry. No TCP443 change or app startup is needed.
   Reuse known references; do not repeat answered account/mailbox questions.
4. Fresh cold probe -> inspect with all cleanup PASS -> exact-before apply once ->
   read-only reconciliation of the original apply nonce -> independent cold probe ->
   restore access and remove only the owned key. Never replay an uncertain write.
   Any drift, cleanup failure or unresolved result holds writes for bounded diagnosis.
5. Record actual before/after and restoration receipts, then update WI026/REQ.
   Account/grant success alone does not complete all stage-two operating preparation.

NCP account absence, Toss merchant/billing contract not applied, and ID1 mailbox
selection are user-confirmed inputs. Existing MAIL/TEST Toss/key-ring presence is
not validity, MID, LIVE contract, pricing or deployment proof. Preserve Gmail's
effective SMTP auth/TLS settings and the target encryption key ring. The proposal
is an overlay, not an importable or complete production configuration:
`APP_BACKGROUND_TASKS_ENABLED=false` also disables audio processing workers.
Finalize worker/scheduler ownership, effective config, release/rollback and exact
mail/PG prerequisites before calling operating readiness complete. Planned Toss
webhooks are optional future work. New cost and real payment remain separate gates.

The process improvement is to prepare once before human presence, use current
evidence instead of stale pending notes, and diagnose a repeated checker failure
with the smallest reproduction. The MA-reported hidden accessibility-tree login
error was superseded by screenshot/normal-submit evidence; invisible stale text
alone must not be treated as a fresh authentication failure. This review did not
revisit the provider. Approved in-scope recovery needs no repeated permission;
only the actual new boundary or mandatory human/tool handoff does.
