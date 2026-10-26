---
version: 1.0
last_updated: 2026-10-24
project: ATS
owner: CR
category: evidence-pack
status: accepted
---
# Evidence Pack: WI-20261024-ATS-027

## Summary
Completed the requested Astra Extra High local review. Two reproduced recovery
defects place candidate `3fbbec606c60ac73c752d4f082965692f589311a413746a8bb938733a0070b46`
on HOLD before remote execution. No operational source change or native action.

## Scope / DoD Check
- [x] Actual stage-one completion versus unexecuted stage two reconciled.
- [x] Exact mutation, audit, recovery, native session/lifecycle and date guards examined.
- [x] Two concrete failure paths reproduced locally; focused follow-up specified.
- [x] Provider/configuration and approval boundaries preserved; resume sequence recorded.
- [x] User summary and current entry/REQ addenda produced; historical evidence preserved.
- [ ] Native syntax, current host/schema/containment and successful cleanup remain untested here.

## Reference Documents (Tier 0-2)
MA injected the Tier0 language, evidence, scope-persistence and preservation rules
from core-principles/development-standards/documentation-standards/glossary;
local relevant sections were read. Large combined reads were truncated, so no
full-body re-read claim is made. Security policy relevant sections and current
AGENTS stage-two rule govern; the user's local-only scope remains controlling.
Assignee CR, task review; handoff existed before work. Applied
`.agents/skills/create-wi-evidence-pack/SKILL.md` output contract.

Tier2: [handoff](WI-20261024-ATS-027-handoff.md),
[approved REQ](../user/REQ-20261024-ATS-001.md), current docs/index,
WI019/022/023/024/025/026 Evidence Packs, pause receipt, stage-one final
interpretation, WI023 candidate, frozen WI005 lifecycle, WI011 builder and WI014
preflight. Source checks include schema/seed, UserSubscription repository/entity,
AdminOperationAuditState/Log, BaseEntity, mail/application and background-worker config.
Memory was navigation for native-checker/repeated-trial lessons; current files
and retained receipts are the decision evidence.

## Findings and Evidence Pointers

**F1 / P1: Reconciliation cannot retry after its retained log was created.**
`build/WI20261024-023-stage-two/native_adapter.py:46` derives every reconcile unit
from the original apply nonce, mode and candidate. Lines 63-64 correctly bind that
nonce to the original claim, but line 70 invokes the reused log creator before
startup. `build/WI20261023-005-account-fixture/reused/cold_capture.py:296-300`
maps that name to one log; lines 549-566 create it with `O_CREAT|O_EXCL` and retain
it. A first failure after this creation leaves the next fresh reconciliation
blocked by FileExistsError. Changing issued_at alone does not change the name.
Do not remove evidence, reuse a new apply nonce or blindly replay DML to escape.
Minimum repair: a separate bounded per-reconciliation attempt identity for
service/log, still bound to original apply evidence; lifecycle test covering
first failure, subsequent read-only recovery, cleanup and no business write.

**F2 / P2: Write deadline also disables later outcome reconciliation.**
`build/WI20261024-023-stage-two/transaction.py:22` applies `day_now() <= END` to
all modes accepted at line 19. Fresh valid reconcile records pass on November 24
and fail `EXPIRED_END_DATE` on November 25, including a possible COMMIT from the
last authorized day. Minimum repair: retain the write deadline, keep fresh
authority/identity/original-claim checks, and admit only read-only reconciliation
after that deadline. Test after-date apply denial alongside reconcile admission.

These are recovery-path defects, not evidence of an actual bad write. WI025's
11/7 PASS remains historical local evidence; its allowance is superseded here.
No defect repair was authorized in this review-only turn or attempted.

**Controls inspected without additional blocking findings in this scope:**
transaction.py:29-63 preserves intended actors/grant/plans and truthful NULL-actor
audits; :66-114 captures current four-value recovery before DML, compares the
exact snapshot delta, rolls back before COMMIT and retains uncertain-COMMIT status.
native_adapter.py:30-43 binds fresh inspection/cleanup and target; :85-115 uses
the persistent session and read-only reconciliation; :118-135 attempts independent
cleanup steps and retains failure. Frozen fixture_core.py:102-135 uses explicit
collation and bounded per-cell hashes; mysql_session.py:34-91 rejects non-JSON
errors/unknown ACK and bounds/reaps its child. Reused cold_capture.py:314-357
disables networking/events/infile and bounds the MySQL cgroup lifetime to 300s.
This source review does not replace actual MySQL/systemd behavior verification.

**Operating prerequisites, not new candidate defects:**
The WI024 overlay is explicitly not final production configuration. Source
application.yml:34-35 defaults SMTP auth/STARTTLS false: preserve effective known
settings, not only the five MAIL fields. BackgroundTasksEnabledCondition.java:19-28
and TrackAudioProcessingWorker.java:31 show that the master false flag also
disables audio workers. Resolve actual worker/scheduler ownership and effective
configuration before app deployment. NCP/Toss account/contract/cost/keys, SMTP/API
delivery and LIVE billing readiness are still separate pending work; no provider
facts were refreshed online during this local-only review. Optional webhook work
is not promoted to a stage-two prerequisite.

## Commands & Outputs
- Local branch `main`, HEAD `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`; unrelated
  dirty/untracked work observed and left untouched. No commit/push.
- Bounded source/receipt reads; no private environment/key/transport body read.
- `<local-python> -B build/WI20261024-027-astra-review/reproduce.py` ->
  `TWO_RECOVERY_DEFECTS_REPRODUCED_LOCAL_ONLY`.
- [Checks](../../build/WI20261024-027-astra-review/review-checks.json): four manifests,
  38 members match; stage-one nine artifact pins plus final-restoration pin match.
- [Reproduction](../../build/WI20261024-027-astra-review/reproduce.py): actual
  service-name expression and extracted prepare_mysql_log helper execute with
  real exclusive file creation; Linux ownership/directory-fd boundary is simulated
  on Windows. First post-log failure is injected. No systemd/MySQL execution.
  Actual transaction.validate is called with synthetic clock/day inputs.
- Existing WI023 eleven/WI025 seven tests were source-inspected and their reported
  PASS reused; no whole suite, build, stage-one trial or redundant runtime test.

## Risks / Rollback
Review date 2026-10-24; environment local Windows; reviewer CR. MA/humans own
prior native/UI observations. Pause receipt's current-server-cold field remains
NOT_REOBSERVED. No fresh host/cost/provider/production acceptance is claimed.
Do not use the held candidate merely because earlier tests passed. Code/date/boot
drift needs bounded diagnosis, not changed pins or copied old payloads.
Four saved columns support only an exact current-after-guarded correction with
new truthful audit; never restore unrelated DB state or remove earlier audits.
Document rollback is limited to these WI027 additions. Operating code, original
claims/logs, historical failures and unrelated working-tree changes are preserved.

## Follow-ups
See [user summary and resume sequence](../user/WI-20261024-ATS-027-summary.md).
Review complete; REQ/stage two remain open and server work paused until user
resume after replenishment. Next work is the two local repairs and independent
review before human unlock, then fresh native gates and actual result recording.
WI027 Blocks:-; it does not automatically trigger a server action or extra WI.
