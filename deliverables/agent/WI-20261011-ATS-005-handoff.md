---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-010.md
    reason: Latest approved unresolved-NS exception and restricted test scope
  - path: WI-20261011-ATS-004-handoff.md
    reason: Resume review and final receipt dependency
---

# WI-20261011-ATS-005: Restricted TLS Resume Ledger

[WI HEADER]
WI ID: WI-20261011-ATS-005
REQ: REQ-20261010-ATS-010
Agent: docops
Depends On: WI003 historical closure; WI004 plus MA receipts for final integration
Blocks: -

[WI SUMMARY]
Why: Reflect the latest explicit user continuation approval without erasing earlier DNS hold/cleanup or falsely claiming TLS completion.
Scope: Append the latest approval and outstanding NS issue to REQ010/current entry now; integrate only subsequently supplied MA operational receipts. Produce two-set deliverables.
DoD: Latest entry distinguishes approval, observed state, test result, outstanding gaps and final cleanup. Do not finalize runtime claims before MA supplies them.
Constraints: Only four listed files. No old evidence rewrite, production/config edits, secret/private-IP disclosure, remote/browser/CA actions, install, billing, commit/push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] REQ status supersedes historical HOLD_DNS_ISSUANCE with approved limited resume/preflight, not successful issuance.
- [ ] Explicitly retain authoritative apex NS NOERROR/NODATA as unresolved with unknown cause.
- [ ] Add WI004 review and WI005 receipts to the existing parallel plan.
- [ ] Certificate success must not clear NS anomaly or imply app/DB release.
- [ ] Final receipts distinguish staging, production, TLS, renewal, alerts and cleanup, including NOT_RUN where applicable.
Performance:
- [ ] Focused four-file update; avoid repository-wide churn.
Quality:
- [ ] Use create-wi-evidence-pack and validate-docs skills; scoped validation and diff check.
- [ ] Preserve all historical receipts and unrelated changes.

[INPUT POINTERS]
Tier0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md
Tier1:
- .claude/agents/docops.md
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/validate-docs/SKILL.md
- deploy/single-vps/runbooks/domain-tls.md
Context:
- deliverables/user/REQ-20261010-ATS-010.md
- docs/index.md
- deliverables/agent/WI-20261011-ATS-003-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-004-handoff.md

[OUTPUT CONTRACT]
Write scope only:
- deliverables/user/REQ-20261010-ATS-010.md
- docs/index.md
- deliverables/user/WI-20261011-ATS-005-summary.md
- deliverables/agent/WI-20261011-ATS-005-evidence-pack.md
Use apply_patch. Initial approval update may complete while PG/MA preflight proceeds; final integration awaits exact MA receipts.

[TRACEABILITY REQUIREMENTS]
User latest request: "NS 문제는 미해결로 기록해두고, 제한된 HTTPS 시험부터 진행해보자. 앱이나 DB는 아직 공개하지 말고, 추가 비용이나 내가 직접 해야 할 건 먼저 알려줘."
User just separately approved temporary Windows SSH-agent activation for this test; key passphrase not yet entered. Agent was freshly Stopped/Disabled before approval. No remote change yet.
Prior LE v1.8 no-email staging/production/renewal consent retained; agreement/scope must be checked before CA action.
DNS observations before this resume: parent delegation expected; both authority apex A correct; apex NS AA NOERROR/NODATA SOA serial2026101101; independent Google/Cloudflare DoH NS same. Earlier target REFUSED resolved. nslookup PTR REFUSED is not target NS failure.
Provider UI supports A/CNAME/MX/TXT; empty apex host input is correct. Own nameserver-host/glue registration unnecessary when using provider NS. No confirmed input correction found; internal cause not proven.
No extra product purchase; ordinary server network usage under existing contract. No app/DB operations.
Rollback: Preserve historical text, append supersession; no operational rollback by DocOps.

