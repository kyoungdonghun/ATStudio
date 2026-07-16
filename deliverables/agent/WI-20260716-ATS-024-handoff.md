[WI HEADER]
WI ID: WI-20260716-ATS-024
REQ: REQ-20260716-ATS-003
Agent: re
Depends On: WI-20260716-ATS-023
Blocks: -

[WI SUMMARY]
Why: Independently verify that demo data is complete, usable, isolated, and safe to rerun.
Scope (in/out): Verify counts, identifiers, stream responses, duration/waveform metadata, title and usage-tag search, playlist membership, and idempotency. Do not mutate product code or unrelated data.
DoD: All REQ-003 success criteria have reproducible evidence, or deviations are explicitly reported.
Constraints/Forbidden: Never expose credentials. Do not execute destructive cleanup against the live demo during verification. Do not commit or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Confirm 30+ QA Demo tracks and 30+ tags.
- [ ] Confirm every generated track has a successful public stream response.
- [ ] Confirm title and Usage Guide Tag searches return expected demo tracks.
- [ ] Confirm 8-12 playlists and valid track membership through authenticated API calls.
- [ ] Confirm a second seed run does not increase the identified dataset.
Performance:
- [ ] Spot-check public list and stream endpoints without material latency regression.
Quality:
- [ ] Capture commands and sanitized outputs.
- [ ] Confirm no existing non-demo data was removed.
- [ ] Record residual limitations relevant to screenshot use.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-003.md
- deliverables/agent/WI-20260716-ATS-023-evidence-pack.md

Files:
- scripts/demo/seed-client-demo.mjs
- scripts/demo/seed-client-demo.ps1
- output/demo-seed/manifest.json

Repro/Logs:
- Public demo: https://challenged-efficiently-void-jonathan.trycloudflare.com
- Local API: http://127.0.0.1:8080

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-024-summary.md:
- Verification outcome, counts, limitations, and recommended next action.
Agent-facing -> deliverables/agent/WI-20260716-ATS-024-evidence-pack.md:
- Sanitized evidence, reproducibility commands, and risk assessment.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-024-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Live local/public API checks and browser-visible verification
Rollback: Verification only; reference WI-023 cleanup command
