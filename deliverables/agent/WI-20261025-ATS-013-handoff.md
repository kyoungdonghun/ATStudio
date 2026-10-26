---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261025-ATS-013 Responsibilities and consequences PDF
[WI HEADER]
WI ID: WI-20261025-ATS-013
REQ: REQ-20261024-ATS-001
Agent: docops
Depends On: WI-20261025-ATS-012
Blocks: -

[WI SUMMARY]
Why: User explicitly asks revised client PDF separating developer versus client actions and stating causal consequences of each decision.
Scope: Repackage explained PDF into responsibilities and decision-to-work consequences, retain accessible examples and verified facts. Preserve previous versions; new final PDF only plus WI records and REQ checkpoint.
DoD: Clear client decisions/inputs, developer work, and if-then branches for every major issue. No implication that technical or legally required remediation can be waived by merely weakening policy text.
Constraints: No app code/HWP/actual policy/server/provider changes, no messages/publication/contract/payment/cost, no secrets, no commit/push. Do not estimate money or dates without basis; do not imply added scope already approved. Sources WI010 review and WI012 explainedPDF evidence, not new runtime claims.

[ACCEPTANCE CRITERIA]
Functional:
- Begin with distinct responsibilities: client chooses commercial/service scope and confirms rights/contracts/operator facts; developer audits implementation and proposes/implements approved changes and testing; legal/provider confirmations separate. Client need not supply technical solutions.
- Every topic has labels '클라이언트가 할 일', '결정에 따른 개발자 작업' and clear conditional statements with outcome.
- Products: maintaining6plans requires document alignment, not recoding products; changing hierarchy/user types requires approved mapping/plan/access/UI changes and existinggrants/migration/regression impact review. Custom offline inquiry vs integrated product distinguished, no assumption checkout required.
- PG: Tosskeep fixesdocument with actualcontract naming and remainingLIVE prep; KGswitch changes provider integration, key/settings/checkout where needed, authbilling/renewal/reconcile/refund tests; alreadyactive recurring mandates cannot be assumed transferable. TEST != LIVE; money/cost separatelyapproved.
- Recurring/refund: maintain cancellation principles → clarifyUI/docs+operatingprocedure; automatic refundentitlement linkage vs existing audited manualworkflow choices, do not claim automation mandatory; partial vsfull. Annual formula client confirms intent, legalcheck then dev calculations/adminworkflow/tests as needed; alternatives arithmetic not legallyapproved.
- Rights/certificate: continuing oldcontent requires minimalproof/snapshots, privacy retention basis and withdrawal adjustment; don't arbitrarily reduce acquiredrights or collecteveryvideo. Detailed cert now versus later/limited fact scope consequences. CIDinternalworkflow vs externalcontract;3businessdays check/request not finalexternalcompletion; offering bymanualsupport possible.
- Privacy required currentbehavior remediation is developer-owned, not optional policychoice: actualcollection map, age/consent procedures applicable, withdrawal minimumretention and erasure, providerinventory/browserstorage. Client supplies alreadyheldrecords/currentness/operatorcontact/use-rights; no duplicatebusinessdata. Requiredlegalchecks distinct.
- Keep four major answer groups but make developer consequences immediately adjacent or crosslinked to section. Group extra privacy/operator facts as existinginfo reuse, not huge questionnaire.
Quality: Plain Korean, natural paragraphs with concise comparison tables only where valuable; 6-8 pages acceptable. PDFskill marker, embedded Korean fonts, allpage render/visualQA. Prior7pagePDF unchanged. Root independently reviews semantics and images.
Performance: Reuse working generator and focused sources, no broadcode/tests.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: AGENTS.md.
Context: deliverables/user/REQ-20261024-ATS-001.md; deliverables/user/WI-20261025-ATS-010-summary.md; build/WI-20261025-ATS-012/create_explained_pdf.py; deliverables/agent/WI-20261025-ATS-012-evidence-pack.md.

[OUTPUT CONTRACT]
Final: deliverables/user/client/ATStudio_policy_roles_and_changes.pdf.
Build/render: build/WI-20261025-ATS-013/.
Summary: deliverables/user/WI-20261025-ATS-013-summary.md.
Evidence: deliverables/agent/WI-20261025-ATS-013-evidence-pack.md via create-wi-evidence-pack skill.
REQ approval/assignment/completion append; stage2 remains open; no downstream WI.

[TRACEABILITY REQUIREMENTS]
Record sourcefacts vs conditionalproposedwork, hashes, allpage render/QA, scopedchecks. MA Tier0 injection: Korean client prose, English internal metadata, scoped evidence-led changes, preserve old artifacts and private details, no implementation from document editing approval. Never present speculation as actual completed work.
