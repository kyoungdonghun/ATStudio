---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: PG
category: reference
status: stable
dependencies:
  - path: privacy-data-map.md
    reason: Source-backed input to follow-up work
---
# Privacy follow-up order and focused verification

This is preparation only. No code, schema, HWP, retention setting or real record changed. The ordered work below avoids waiting for provider signup where possible, but does not infer permission to delete data or alter product promises.

## Order, owner, cause and effect

| Order | Work and present readiness | Owner / prerequisite | Resulting implementation or operating change |
|---|---|---|---|
| 1 — ready now | Use the completed data-flow matrix to prepare factual disclosure inputs: mandatory/conditional/optional signup fields, certification files/audits, localStorage/sessionStorage and provider roles. Toss is selected. | Developer facts are ready. Operator verifies existing business/contact details rather than re-entering them. | Later policy editing can use actual fields. No PG migration and no change to six products is implied. |
| 2 — decide minimum record treatment | For users, social links, consent, certification files/audits, downloads/licenses, channels/exports, billing/receipts and tokens: record purpose, required evidence, treatment, start event, period, approver. Leave unknowns explicit. | Client decides continuing-content rights and launch workflow; appropriate legal/contract confirmation supplies record-specific retention basis. | If continuing rights require proof, developer preserves a minimal durable proof record before changing license/download deletion. If no ongoing purpose remains, developer designs deletion/anonymization instead of only a deleted flag. Do not retain full profiles merely to retain license proof. |
| 3 — implement one coherent withdrawal change | Use a treatment table to specify what is removed immediately, retained separately, scheduled for removal, or waiting for external confirmation. Preserve last-admin and unknown-payment safeguards. | Step 2 accepted; separate scoped implementation approval. | Developer changes withdrawal transactions, related storage cleanup and necessary schema/jobs together. This can include social unlink, certification file disposal, token removal, consent history and channel evidence; exact subset follows the accepted table. No whole-database deletion. |
| 4 — confirm consent/age/onboarding | Required consent capture for selected onboarding routes; consent version/content lookup, marketing revocation and recipient exclusion. Age policy must become an explicit acceptance rule. | Client confirms actual age/marketing launch choices; developer verifies input paths. | If social signup opens, developer ensures required consent is captured there. If marketing opens, developer implements revoke/current status and excludes withdrawn users; an immutable historical agreement alone is insufficient. Age choices may require screen/API validation; no unnecessary birth-date collection is assumed. |
| 5 — connect copies and restore | Trace relevant data into storage, exports, logs and backups. Record who can access exports and evidence; define restore-time disposal reconciliation. | Accepted record treatment plus WI001 operating inventory; actual backup/storage terms and cost/access approval for runtime work. | Developer/operator add bounded copy expiry and restore checks. A recovered DB must not silently re-enable a withdrawn account or revive records already due for disposal. Backup retention does not by itself authorize immediate prune. |
| 6 — validate changed behavior and finish text | Run focused scenarios below on synthetic fixtures first; only then approved limited runtime checks. Update final client policy and publishing UI after facts/choices agree. | Changed scope implemented/reviewed; provider credentials/contract only for external cases. | Evidence ties the final wording to behavior and operating responsibility. No replay of the whole completed stage-one trial without a new cause. |

## Questions that remain genuinely dependent on the client or provider

- Continuing rights for previously published content: what must remain provable after subscription expiry/withdrawal, and what channel/project evidence is needed? This determines the minimal license proof record.
- Which optional services are in the initial release: social login providers, marketing, business certification and external CID? This determines which workflows must be operational at release.
- Final contacts and responsibilities can reuse already provided business details and the selected ID1 operations mailbox. No password, key, identity document or actual customer export is needed for this decision.
- NCP/Toss contracts establish actual recipients, work and regions; no country or retention rule is inferred from a brand. Provider signup does not determine the unrelated license/withdrawal policy.
- The existing commercial policy questions (product scopes and annual refund formula) remain in the client PDF. Toss-versus-KG is now resolved and should not be asked again.

## Future focused verification cases — not executed

| Case | Minimal fixture and trigger | Required evidence / failure condition |
|---|---|---|
| V1: field classification | Synthetic individual/business requests, only the changed validation branches. | Required vs optional and forbidden combinations match accepted disclosure; values are not logged. No real signup/email needed for the unit boundary. |
| V2: consent coverage/revocation | Each enabled signup route, no marketing opt-in, opt-in then revoke, repeated revoke. | Exact policy version/content link; historical acceptance preserved as required, current revoke state and recipient exclusion honored; no unwanted send. Do not invent marketing traffic merely to test a database flag. |
| V3: coordinated withdrawal | Synthetic user with each relation in the accepted treatment table; no real provider. | Before/after table and storage path counts match individual treatment. Personal data erased/minimized where required; required evidence remains accessible only to authorized actors. Transaction rollback must not leave files/DB inconsistent. |
| V4: guards and external uncertainty | Last active admin; provider charge pending; key deletion timeout/unknown vs confirmed success. Use mocks/local fakes first. | Last-admin/pending-charge protection remains; unknown external outcome recorded and reconciled, not blindly repeated. Withdrawal after-commit external calls remain explicit. |
| V5: continuing-content evidence | Synthetic download/license, plan change/expiry then withdrawal, against accepted rights policy. | Minimal proof preserves original relevant facts/version; later profile/plan edits cannot rewrite historical rights. Unauthorized proof lookup denied. This is conditional on the chosen rights policy. |
| V6: token disposal | Issued/used/expired verification and reset tokens, reissue and approved purge boundary. | Correct validity/reissue behavior and record disposal are separately checked; no raw token in logs or receipts. A validity TTL is not mislabeled a purge schedule. |
| V7: browser shared-device boundary | Synthetic browser session A then logout/account B; OAuth consumed/expired; history clear. | Auth keys removed at correct session generation. Player history/storage behavior matches the declared scope; no accidental exposure to account B if isolation is promised. Track-ID hydration remains documented. |
| V8: restore and copies | Later authorized isolated recovery from a dated snapshot containing a now-withdrawn synthetic subject. | Disposal/withdrawal decisions newer than snapshot are reapplied before app opens; no charges/mail/CID registrations trigger; external export/file copies follow the same accepted rule. Do not run against production just to prove this checklist. |

Existing tests should be reused and supplemented only for changed behavior. These are acceptance cases, not a claim that any privacy remediation or new operational acceptance has passed.

## Safe reporting format

Report case ID, synthetic subject alias, source revision, record classes/counts, masked outcome codes and cleanup state. Never attach real passwords, tokens, reset URLs, full receipt URLs, uploaded business documents or customer CSV bodies. Record failed attempts distinctly and reconcile an unknown effect before retrying it.
