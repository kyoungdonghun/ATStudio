---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: reference
status: active
---
# WI-20261021-ATS-004 Evening Trial Continuation

[WI HEADER]
REQ-20261011-ATS-007, user explicitly renewed the bounded Gmail signup trial tonight. SE prepares, PG reviews, MA alone performs remote operations. Created with create-wi-handoff-packet.

[WI SUMMARY]
Prepare the smallest exact cold preservation/resume procedure for the completed morning v7 trial, without changing runtime/JAR/SPA. Existing one-use Release and control paths cannot be reused. Preserve original results and private material without printing contents. No new architecture or generic framework. First report exact read-only metadata needed by MA; do not guess current remote state.

[ACCEPTANCE CRITERIA]
Use current v7 and tested preservation primitives. Require exact cold state, no process/jobs, original runtime/artifact/quarantine identity, no overwrite/delete. Preserve expired trial records then allow fresh Release/preflight/producer/Start under existing bounds. Focused local tests and independent PG exact review required before MA invokes any new mutating operator. Do not reproduce historical failed operators unchanged against a different attempt.

[INPUT POINTERS]
Tier0 injected by MA: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary. Tier1 security-policy. Tier2 docs/guides/evening-auth-trial-resume.md; current docs/index.md; WI062 top current checkpoint; build/WI067-auth-trial-v7, WI067-pc-delivery-v7, WI067-private-producer-v7; previous WI067-ledger-preserve and release-refresh operators as references only. Current MA read-only check2360df: app masked/PID0/failed; supervisor static/PID0/failed; MySQL masked/inactive/PID0; quarantine Nginx active/PID86404; same approved management IP. Human is home and SSH key unlocked.

[OUTPUT CONTRACT]
Own only new build/WI067-evening-resume-20261021 and matching evidence folder, plus WI004 evidence-pack/user summary. No credential/private PC reads, server/browser access, source edits, commits or purchases. Return metadata query first, then bounded local candidate, hashes, tests, exact invocation. PG review blocks remote mutation; MA supplies fresh facts. Use create-wi-evidence-pack for output.

[TRACEABILITY]
Original morning OPEN/POLICY-BPF-LEDGER PASS and deferred MAIL remain original evidence. Aggregate cleanup FAILED is not safety failure or new signup acceptance. No public exposure, additional paid service or password/consent automation. Stop on changed scope or unknown state rather than weakening controls.
