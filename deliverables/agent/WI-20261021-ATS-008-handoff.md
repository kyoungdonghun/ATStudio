---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: reference
status: active
---
# WI008 Preflight Attempt Preservation and v8r1 Transition

[WI HEADER]
REQ: REQ-20261011-ATS-007, latest explicit DNS repair and bounded mail trial authorization.
Assignee: SE; independent PG continuation under WI007. Created using create-wi-handoff-packet.
Depends on WI006/WI007. Blocks MA Public/Release and signup trial.

[WI SUMMARY]
Prepare the minimal exact-target cold preservation derivative for the consumed PREFLIGHT/NOT_RUN attempt, then retire v7 tools without deleting records or touching JAR/SPA. Use fresh native metadata 7d1351, not the older WINDOW preservation target. Native Java17 and no-auth TLS proof d59e49 passed; apps/DB remain off, original Nginx active (7376d3). No remote actions by SE.

[ACCEPTANCE CRITERIA]
- Match exact release, evidence hashes, private file shapes and cold state before any change; expired release only.
- Preserve current failed result verbatim. No cleanup PASS relabeling.
- Current app mask is absent; run/log directories are empty; supervisor loaded failed PID0. Retire callbacks before tools, using established no-replace preservation and durable-copy patterns.
- Preserve JAR/SPA unchanged, root-only private records and prior operators. No app/DB restart, mail, public exposure, secrets output, deletion or purchase.
- Focused native-format-compatible tests and exact freeze; PG reviews delta before MA execution. Do not invent general deployment abstraction.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md. Tier1: docs/policies/security-policy.md.
Tier2: REQ007; WI006 and WI007 evidence; build/WI067-profile-retry-20261021; build/WI067-ledger-preserve; reviewed v8r1 delivery and runtime.
MA supplies exact metadata from 7d1351 in delegation. Current failed release SHA1b666b4773edeae3a812dc3f4e169543070bc1b89672052f837bc323b4802cde, size1555, issued1792576557, expires1792577457. v7 tools SHAe1fd2145c8ff7ce30033cc2e52e6e94b4f6470e6136fc06a6e53c7e57d38471a. Supervisor SHA937c6e3e0a68291204eaeb7f1dde5b361087a684438c1bf19774e5c7be837d2e.

[OUTPUT CONTRACT]
Own new build/WI067-dns-preserve-20261021 and WI008 evidence/summary only. Preserve originals; apply_patch for manual changes. No remote or credential reads. Use create-wi-evidence-pack. Notify MA immediately when exact candidate is ready for PG; current SSH session stays available for authorized native continuation.
