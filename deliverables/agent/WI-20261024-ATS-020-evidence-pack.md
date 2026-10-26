---
version: 1.0
last_updated: 2026-10-24
project: ATS
owner: SE
category: agent
status: review_pending
---

# Evidence Pack: WI-20261024-ATS-020

## Summary

Prepared a separate bounded buyer3 reset candidate using retained v8r1 lifecycle and the unchanged application release; nine focused local tests pass. Native execution and independent exact approval remain pending.

## Scope / DoD Check

- [x] Separate runtime copy; original auth/audio/JAR evidence untouched.
- [x] Buyer3-only one-mail reset, actual human new-password submit and new login.
- [x] Exact full-table preservation, old password/refresh and consumed-token denials, new session identity and logout.
- [x] Pre-forward possible-stale credential marker covers ambiguous reset outcomes; original actor files remain unchanged.
- [x] Local focused tests and executable-byte freeze.
- [ ] Independent exact PG review (WI-20261024-ATS-018).
- [ ] MA native SMTP/UI/reset/session and cleanup evidence; not established by these tests.

## Reference Documents (Tier 0-2)

Injected context: handoff WI-20261024-ATS-020 and MA Tier0 summary (explicit stage1 authorization, preservation, privacy, exact evidence, Korean conversation/English technical documentation). Source reading was bounded to this assignment.

| Tier | Reference | Use |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md; development-standards.md; documentation-standards.md; glossary.md | MA-injected governance summary |
| 1 | docs/policies/security-policy.md | Privacy and unchanged lifecycle scope from handoff |
| 2 | deliverables/agent/WI-20261024-ATS-020-handoff.md | Explicit delegated changes and native limits |
| 2 | build/WI067-auth-trial-v8r1/ | Source lifecycle, transport, ledger, admission, release and original artifact |
| 2 | build/WI067-private-producer-v8r1/producer.py | Retained bounded root pipe exchange |
| 2 | build/WI20261024-005-audio-wiring/README.md | Actor identities and ordering |
| 2 | src/main/java/com/atstudio/atstudio/service/EmailService.java | Token deletion, consumption, password and refresh effects |
| 2 | src/main/java/com/atstudio/atstudio/service/auth/AuthService.java | Session and denial effects |
| 2 | frontend/src/pages/auth/PasswordResetPage.tsx | Actual human reset/next-login flow |

Evidence-pack skill read and used: `.agents/skills/create-wi-evidence-pack/SKILL.md`. WI handoff existed before file changes. MA remains owner of actual credentials, remote staging/execution and native receipts; subagent made no server/provider/mailbox/secret access.

## Evidence Pointers

- `build/WI20261024-020-reset-completion/README.md`: exact MA recipe and human preparation.
- `reset_flow.py` within that directory: bounded routes, one-shot gateway and complete-cell ledger.
- `prepare_candidate.py` and `producer_tail.txt`: deterministic isolated derivation and fixed target producer.
- `runtime/binding/freeze.json`: executable runtime manifest, SHA256 `1fd18abc8c15094e57ca4827ac31766d3e5e3e809319bb05fae0d5b51e6a376a`.
- `derivation.json`: changed members and 33 unchanged retained members (including historical tests, not newly rerun evidence).
- `test_reset.py`: nine focused synthetic checks; no actual native operation.

## Commands & Outputs

`python -I -S -B build/WI20261024-020-reset-completion/prepare_candidate.py` -> `DERIVED_LOCAL_CANDIDATE`.

`python -I -S -B build/WI20261024-020-reset-completion/test_reset.py` -> `Ran 9 tests ... OK` (latest local run 0.105 seconds). The native reservation method was extracted by AST and exercised against a response-loss case: server mutation may succeed while the client gets 503, but the possible-stale marker precedes forwarding.

No old full suite, Gradle build, JAR rebuild, native trial, real account login, mail, provider request or private capture was run by SE.

## Risks / Rollback

- SMTP/browser/native compatibility and live cleanup are unverified until MA observes them.
- Fresh original schema-compatible target, Gmail, provider587, backup and human readiness records are mandatory; none are generated here.
- A possible reset makes the original buyer credential unusable for later automatic work. MA must preserve safe PC-side stale metadata on success or uncertainty. Original private actor inputs are retained, not edited.
- Fresh new password is not saved; future use requires human private capture only if needed.
- Access JWTs may remain valid until expiry; refresh revocation is the tested contract. Clear normal SPA storage through the bounded logout acknowledgement.
- KST 2026-10-24 gate forbids late starts; independent cleanup remains available after rollover.
- Rollback of local preparation is selecting the unchanged old candidate, not deleting evidence. No automatic password rollback, token replay, data deletion, mutation retry, restore to production or grant extension is authorized by this candidate.

## Follow-up

WI020 blocks WI018 exact PG review, then MA native execution if approved and all genuine prerequisites are met. DocOps WI019 must report actual stage1 outcomes separately from local readiness. Until then reset completion is NOT_RUN.
