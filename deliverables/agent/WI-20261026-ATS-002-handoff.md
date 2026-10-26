---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: MA
category: reference
status: stable
dependencies:
  - path: ../user/REQ-20261024-ATS-001.md
    reason: Approved local preparation
---
# WI-20261026-ATS-002 Actual privacy flow inventory
[WI HEADER]
WI ID: WI-20261026-ATS-002
REQ: REQ-20261024-ATS-001
Agent: pg
Depends On: WI-20261025-ATS-010
Blocks: WI-20261026-ATS-003
Execution state: completed; integrated by WI-20261026-ATS-003.

[WI SUMMARY]
Why: Prepare necessary developer facts independent of unresolved client commercial policies.
Scope: Source-backed inventory from input/API through persistent store, browser, external services and withdrawal/cleanup. Reuse HWP review but deepen concrete flow and remediation boundaries. No legal/policy invention or data change.
DoD: Useful actual-data matrix and prioritized remediation plan distinguishing factual developerwork, operating/contractinfo, commercialdecision-dependent records, and unavailable runtimeevidence. Include bounded future local verification scenarios without implementing policydependent changes.
Constraints: No network/server/DB/app/provider/secret/privateenv/customerdata reads, deletion, schema/code/HWP edits, costs, mail/payment, commit/push. No reread full HWP with PII; existing WI010 redactedsummary sufficient. Don't claim entityfield implies collected/required/currentlyoperating. No retentiondurations invented. Tossmaintained decision, KGdocumenttypo; NCP notdeployed vs Gmailtested.

[ACCEPTANCE CRITERIA]
- Cover signup/profile required/conditional/optional, social transientvsstored, consentversion/revoke, businesscertattachments/audit, download/license/playlist/channel, payment/billingkey/receipts, emails/tokens, browserstorage/logs where proven.
- Every row records entry/source, actual fields/type only, store, recipient/transienthandling, withdrawal/cleanup, missingevidence/action; no raw data.
- Trace User.withdraw softdelete versus source deletion licenses/downloads and unhandledsocial/playlist. Don't decide to retain/delete without basis. Flag backups and restore re-deletion dependencies, minimum proof needed for existingcontent rights.
- Mark statutory/operator/legalunknown separate from simple disclosedfacts; proposed schema/jobs require later scopedapproval. Provide meaningful focused future test cases for changedbehavior only, no boilerplatetest scripts now.
- Root will independently review highrisk findings. No new internetlegalresearch needed unless actually answering law; cite retained datedreview if used.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: AGENTS.md; docs/policies/security-policy.md.
Context: docs/index.md Current Decision Entry; approvedREQ; deliverables/user/WI-20261025-ATS-010-summary.md; deliverables/agent/WI-20261025-ATS-006-evidence-pack.md.
Source: DTO/controller/service/repository/entities/frontend exactpaths listed WI010; targeted further reads permitted. No fullrepo dump.

[OUTPUT CONTRACT]
Owned: build/WI-20261026-ATS-002/privacy-data-map.md and remediation-and-verification.md.
User-facing: deliverables/user/WI-20261026-ATS-002-summary.md Korean.
Agent-facing: deliverables/agent/WI-20261026-ATS-002-evidence-pack.md via create-wi-evidence-pack.
No REQ/index edits; WI003 ownsintegration.

[TRACEABILITY REQUIREMENTS]
Sourcepaths/lines, exact examined behavior/absence searchlimits, no runtime/legalapprovalclaims. MA Tier0 injection: scoped evidence-first work, Koreanuser/Englishinternaldocs, canonicalterms/metadata, preserve artifacts, no secret/outputPII. main91a16e7 dirtytree noreset. Practical preparation not broad audit/futureframework.
