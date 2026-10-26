---
version: 1.0.0
last_updated: 2026-10-24
project: ATS
owner: MA
category: reference
status: active
---
# WI-20261024-ATS-001

Generated using create-wi-handoff-packet for the explicit current user approval under REQ007.

[WI HEADER]
WI ID: WI-20261024-ATS-001
REQ: REQ-20261011-ATS-007
Agent: SE
Depends On: Current explicit 2026-10-24 KST apply approval
Blocks: WI-20261024-ATS-002

[WI SUMMARY]
Why: Minimal reviewed execution wrapper for saved-input TEST fixture application
Scope (in/out): Own build/WI20261024-001-fixture-apply/ and own two-set deliverables. Preserve frozen WI005 22 files, WI011/014 and source/JAR exactly. Reuse existing transaction, crypto helper, native DB lifecycle and strict transport. Prepare a minimal local builder and target wrapper that reuses saved private input without recollection, stages exact old candidate, preserves existing fixed approval files/evidence, creates new immutable per-operation/phase approval records and distinct inspection/apply nonces. A possible minimal solution is wrapper-bound native.HUMAN/APPROVAL paths to fresh root0700 evidence subdirectories; do not weaken or replace authority/date/target/candidate/delta guards or core transaction functions. Bind the actual latest external MA decision record, exact input/candidate, fresh target, plan ID1 with quota exactly5, approved day2026-10-24. Fresh inspect must pass conditions and all cleanup before constructing private exact plan and applying; inspect age <=900 sec. Scope may run sequential inspect+apply in one bounded invocation under already explicit approval, preventing a second human unlock/timed approval cycle. No remote action by SE. Save plan/approval/commit evidence privately and emit only fixed sanitized statuses/effect counts/new numeric IDs/cleanup. Date rollover/expired approval/state drift -> halt writes; always cleanup. COMMIT uncertainty -> no automatic write retry; retain evidence and expose fixed HOLD for MA. Provide runnable strict-SSH stdin instructions, bounded timeouts and independent cold post-probe reuse. No new framework or full app test.
DoD: Minimum synthetic tests prove exact effect/date/plan/input binding, old approval/evidence preservation, fresh inspect->plan->apply ordering, cleanup/failed inspect prevents apply, no retry on uncertain commit, no private values/digests in public projection. Reuse previously-tested core rather than retesting fullsuite. Exact manifest and executable recipe before human action.
Constraints/Forbidden: No remote access by subagents. No deletion/overwrite of old evidence/approvals, no schema changes, unrelated edits, app/Spring startup, login, audio, mail, consent event, payment/refund/billing agreement/renewal, publication, extra purchases, commit or push. Date fixed to 2026-10-24 Asia/Seoul; no automatic date extension. Existing ID1 and every pre-existing row/column immutable. Do not touch/read/display real private input or ask human setup before technical readiness.

[ACCEPTANCE CRITERIA]
Functional: Exactly users INSERT2; only newly inserted uploader USER->ADMIN UPDATE1; audit INSERT1; buyer subscription INSERT1 plan1 INDIVIDUAL quota5 active. Both users verified TEST flag only. Subscription MONTHLY/CANCELLED, started_at=expires_at=2026-10-24, pending fields NULL, no billing.
Performance: Minimum delta implementation/tests, reuse reviewed successful artifacts; report blockers promptly, avoid broad trial reruns.
Quality: Minimum synthetic tests prove exact effect/date/plan/input binding, old approval/evidence preservation, fresh inspect->plan->apply ordering, cleanup/failed inspect prevents apply, no retry on uncertain commit, no private values/digests in public projection. Reuse previously-tested core rather than retesting fullsuite. Exact manifest and executable recipe before human action.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier2: docs/index.md Current Decision Entry; deliverables/user/REQ-20261011-ATS-007.md; deliverables/agent/WI-20261023-ATS-013-evidence-pack.md.
Code/evidence: build/WI20261023-005-account-fixture/{README.md,fixture_core.py,fixture_native.py,fixture_cli.py,candidate.json}; build/WI20261023-011-fixture-inspect/{inspect_tools.py,remote_inspect.py}; build/WI20261023-014-capture-repair/; build/WI20261023-012-fixture-inspect-review/{review-receipt.json,access-interpretation.json}; build/WI20261023-fixture-inspect-resume/{inspect-result.json,post-stop-probe.json,final-restoration.json}.
Injected governing context: Conversation/REQ Korean, technical docs English. MA is sole operator; subagents local only. User Astra Extra High preference continues. Prior current safe receipts show input collisions/admin/existing_work0, ID1 unentitled USER guard true, plan1 quota5 eligible, five DB cleanup checks and cold postprobePASS, firewall/provider/key/agent restored. These are dated evidence, not freshness for new apply. Saved input exists in user-only local storage; MA reuses it without showing values. Source643/JAR/frozen005/011/014 were unchanged at last check; refresh before execution. Retained fixed HUMAN/APPROVAL files block old unchanged stage, so new wrapper must preserve them and narrowly address paths under exact review. No new authority is invented by JSON; user approval below is authoritative.

[CURRENT USER APPROVAL]
좋아. 기존 ID 1과 기존 데이터 보존 조건으로 위 변경안을 2026-10-24 KST 기준으로 승인할게. 로컬 보완·독립 검토와 최신 사전 조회 통과 후 적용하고 DB 종료·접근 원복까지 확인해줘. 날짜가 바뀌면 중단하고, 앱 실행·로그인·음원 시험·결제는 제외해.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261024-ATS-001-summary.md.
Agent-facing: deliverables/agent/WI-20261024-ATS-001-evidence-pack.md using create-wi-evidence-pack.
Handoff: this file. Notify MA immediately when successor unblocks.

[TRACEABILITY REQUIREMENTS]
Exact candidate/review pins, scoped tests, commands and safe evidence paths. Private inputs/identity hashes/snapshots remain private, no secret argv/env/output. Preserve prior records. Failure/uncertain commit means stop writes and confirm cleanup, never automatic replay or delete to force progress.

