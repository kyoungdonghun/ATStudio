---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: work-summary
status: confirmed
---

# WI-20260908-ATS-018 Summary

## Decision

The approved final review is complete at main `8161f0a`: **release HOLD, not implementation complete**. Six P1 defects were confirmed by independent source tracing and MA verification. Existing source and data were not changed; no live exploit, financial action or production failure was reproduced.

| Release blocker | Why it matters |
|---|---|
| Refresh JWT accepted as API access token | Logout/reset/rotation can leave that refresh capability usable against protected APIs until expiry. |
| Validation exceptions log rejected inputs | Invalid password/email/phone values can enter application logs. |
| Expired subscriber's new purchase cannot finalize | Provider charge may succeed while local access and payment ledger completion fail. |
| Competing upgrade choices can charge twice | Different next-cycle selections bypass the exact-command duplicate fence. |
| Track soft deletion erases licenses/history | Issued evidence and quota history cannot be recovered by reactivation. |
| Playlist/Album deletion conflicts with strict storage audit | Successful thumbnail cleanup leaves retained keys and can block the next production restart. |

## Additional Dispositions

Six P2 findings are separated in the register: same-second refresh identity, late refresh crossing sessions, reset-token concurrency, social-refresh verification, stale Track media writes, and partial-stage cleanup. The first three belong with auth correction; stale media writes require deterministic persistence confirmation. Social renewal is an enablement gate when OAuth is enabled; partial-stage cleanup is a bounded maintenance candidate. P2 does not automatically mean safe to postpone.

The selected backend dependency/proxy patch disposition and finite multipart budgets need closure at the deployment boundary. Development dependency warnings and obsolete authoring metadata do not all become release blockers. The current public Vite demo must not be mistaken for a production serving configuration.

## Verification

- Fresh backend: 1,708 total / 1,689 passed / 0 failed or errors / 19 skipped (18 opt-in MySQL tests and 1 Windows symlink case).
- Fresh frontend: 112 files / 1,493 passed. These are jsdom tests, not mobile/browser acceptance.
- HTTP: local/public home and catalog 200; anonymous protected/private paths denied; untrusted Origin rejected. Exact CORS and Swagger/SPA distinctions are in the evidence.
- npm audit: 6 dev-inclusive affected groups; production-only 0. No dependency update was applied.
- No product/test/config/schema edits, DB/media changes, restart, payment/refund/mail, commit or push. REQ/WI records and a minimal current SR-93 pointer are the deliverables.
- Document validation and diff checks passed; 125 pre-existing untracked files retained identical hashes. All three reviewers were closed after their evidence was integrated.

## Next Step

Approve a bounded remediation scope for the confirmed auth/payment/data defects and their missing regression tests, then verify only the changed boundaries and aggregate gates. After that, finish the remaining selected-production-target checks with the user where actual account/target authority is required. Do not add unrelated features or repeat all earlier accepted TEST flows.

Details: [Risk register](../agent/WI-20260908-ATS-018-findings.md), [central evidence](../agent/WI-20260908-ATS-018-evidence-pack.md), [approved REQ004](REQ-20260908-ATS-004.md), [open operating gates](../../docs/SR/SR-93.md#remaining-production-gates).
