---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: PG
category: evidence-pack
status: completed
---
# Evidence Pack: WI-20261025-ATS-006

## Summary

Independent source/contract review completed for WI004/WI005/WI008. One P2 NCP
literal-content defect was corrected by SE and re-reviewed. Final local evidence
matches; activation and the whole stage remain open. Reviewed 2026-10-25 KST on
Windows, branch main, HEAD `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8` with shared
uncommitted work preserved. Evidence owner: PG; implementation/test owner: SE.

## Scope / DoD Check

- [x] Reused current account/grant completion, IDs1/2 ADMIN, ID3 USER, six plans,
  daily-five grant through Nov24; no account/grant replay.
- [x] Examined scheduler and unscheduled side-effect boundaries, Gmail/key-ring
  preservation, provider cost claims, optional transport and client drafts.
- [x] Reported F01 directly to SE/MA, re-reviewed the minimal correction and
  distinguished superseded candidate evidence from the final candidate.
- [x] Independently checked final public hashes and operating consistency; no
  private-body, provider mutation, app startup, server, payment or Git operation.

## Reference Documents (Tier 0-2)

MA injected the core-principles/development/documentation/glossary Tier0 rules:
Korean conversation, English documentation except requested Korean drafts/REQ,
small approved scope, honest evidence, protected secrets and preserved history.
Tier1: AGENTS.md single approval gate/security-policy pointers. Role: PG/CR.
Task pointers: approved REQ-20261024-ATS-001, WI006 handoff, current docs/index
checkpoint, WI003 account/grant completion, WI024 preparation and WI004/005/008
handoffs/evidence. Applied create-wi-evidence-pack after confirming the handoff.
Only relevant sources and sections were inspected; no claim of full repository
or unrelated policy review.

## Findings and Source Evidence

1. **F01, P2, resolved:** initial transport serialized completed HTML containing
   `${key}` without corresponding provider parameters. The official contract
   rejects missing substitution keys. Incident alert details may contain provider
   failure text; current nickname validation excludes the syntax and was not
   claimed vulnerable. See [preserved finding](../../build/WI-20261025-ATS-006/finding-01.md).
   Final NcpMailTransport.java:72-82,110-113 encodes only the NCP HTML boundary and
   rejects title substitution syntax before exchange. Two actual-payload/no-call
   tests were added; default SMTP/template/token logic is unchanged.
2. **Operating boundaries are accurate:** BackgroundTasksEnabledCondition.java:
   17-27 and BackgroundTasksConfiguration.java:15-35 separate audio executor from
   Spring scheduling. StorageMutationRecoveryService.java:41-46 suppresses
   startup recovery; WithdrawalBillingCleanupCoordinator.java:19-27 retains
   after-commit cleanup. Direct billing/API activity is not disabled by audio-only.
   Startup integrity checks and storage initialization also remain; neither
   candidate is claimed a globally read-only running application.
3. **Mail controls:** NcpMailTransport.java:44-67 validates selected configuration;
   :72-107 uses a fixed endpoint, one individual recipient and ACCEPTED semantics;
   :115-164 signs correctly and bounds response/deadline, with no application
   retry or redirect/fallback. EmailService.java:172-194 preserves SMTP and logs
   only local correlation/outcome/exception class. No provider ID persistence or
   delivery polling exists; manual provider-history reconciliation is required
   before an ambiguous resend. This limit is documented, not a delivery claim.
4. **Provider/policy drafts:** independently checked official request/signature
   and Toss public pricing. NCP numerical price remains UNKNOWN; enrollment and
   payment-fee VAT are not conflated. Canonical plan rows 32-37 and existing payment
   policy Sections1/4/5 support the draft prices and recurring/refund description.
   Privacy text is an explicitly incomplete source inventory, not legal advice.
   Customer policy links/operator facts/review access are still unfinished.

Official sources independently retrieved on 2026-10-25:
[MAIL request/acceptance/substitution](https://api.ncloud-docs.com/docs/sens-mail-request-create),
[NCP signature](https://api.ncloud-docs.com/docs/common-ncpapi),
[Toss public fees](https://www.tosspayments.com/about/fee).
Only public documentation was retrieved; no provider API or console interaction.

## Evidence Pointers

- [Review receipt](../../build/WI-20261025-ATS-006/review-result.json) and
  [read-only reproduction](../../build/WI-20261025-ATS-006/check_review.py).
- `build/WI-20261025-ATS-008/checks.json`: seven current source pins; changed NCP
  XML16 separately from retained XML26; matching source/test hashes verified.
- `build/WI-20261025-ATS-008/attempt3-interpolation-output.txt`: changed NCP suite
  and isolated bootJar PASS. `attempt2-output.txt`/`test-results-v1` retain earlier
  four-suite40 PASS; initial constructor compile failure remains retained.
- Current candidate SHA256:
  `23a312c331d10d2875308ddea7e8034fb6e13ea48291027064565e833dc80d15`.
  Superseded mail candidate:
  `0ced47dc2fb6de2a01ad4984504a0fa23aa362c5c0cd0845a86c1a1e7debac3a`.
  Historical validated JAR:
  `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.
  All three hashes independently matched; neither new candidate was deployed.
- `build/WI-20261025-ATS-005/operating-modes-v1.json`/`local-check-v1.json`
  preserve initial evidence. Current manifest differs only in application.yml's
  pin `1f3151a554124831997cca8979b4927a8f5bff4baf349afe9326e4d2da2955e6`.
  Other ten pins, operating modes, metadata and old release remain identical.
- WI004 `provider-readiness.md`, `client-signup-handoff-ko.md` and
  `policy-content-and-inputs-ko.md`: reviewed public evidence/drafts, all unsent.

## Commands & Outputs / Tests

Read targeted source/diffs with Get-Content/rg and verified branch/HEAD. Ran:
`& "$env:LOCALAPPDATA/Python/pythoncore-3.14-64/python.exe" -B build/WI-20261025-ATS-006/check_review.py`
→ PUBLIC_LOCAL_EVIDENCE_MATCH; delegated WI005 read-only checker →
LOCAL_PREPARATION_PASS. The checker parses only XML metadata, streams JAR hashes
and reads public pins; it does not launch the app, read private env or call network.

Reviewer did not rerun Gradle. Author evidence: changed NCP16 PASS after F01;
unchanged EmailService17/AuthSecurity6/ResetConcurrency3 = 26 earlier PASS reused.
This is not a claim that 42 cases ran together or that native HTTP behavior,
provider credentials, sender, inbox delivery or production boot were tested.
Scoped whitespace/document checks are recorded in `document-checks.json`.

## Risks / Rollback / Follow-up

No blocking finding remains for this reviewed local preparation scope. Keep
SMTP/current Gmail effective settings and billing key ring. Never infer delivery
from acceptance, retry an unknown send blindly, activate LIVE billing from TEST
keys or treat audio-only as a payment guard. Real provider costs, sender/domain,
contract/MID and customer-policy/access requirements remain explicit boundaries.

This review edited only its build folder and two deliverables. Correct its text
by superseding these artifacts; do not reset shared work or historical evidence.
Any future runtime rollback must reconcile outcomes first and restore exact
approved config/release pointers, not the DB or account/grant/audit history.

WI006 **unblocks WI007** current-entry integration. User deferred NCP/Toss signup
to one later client handoff; no immediate signup question is pending. New provider
activation, costs, publication or protected-input handoff must use the actual
remaining boundary. This local review is not authority to perform those actions.
