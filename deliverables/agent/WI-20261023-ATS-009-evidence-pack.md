---
version: 1.1.0
last_updated: 2026-10-23
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-009
---

# Evidence Pack: WI-20261023-ATS-009

## Summary and Scope

`ALLOW_EXACT_APPROVED_READONLY_ACCOUNT_PLAN_RECHECK` for candidate SHA256
`448253685c0bd2c2fc7fc3e0d2e70a7c99421dbe9c204bf0ebce2f09b4cdab3c`.
All five members and bundle
`3799c0145883e4fc201b2581f547287ce8f3e7c053a21d8536452e5c7fc76313`
match. Implementation 21 and independent 8 tests pass. MA received the exact
allowance before native execution. PG performed local review only.

The existing explicit user approval covers server contact and temporary
read-only account/plan inspection. It excludes fixture inputs, account/profile
or subscription changes, schema changes, application start, login, mail, audio,
payment, publication, deletion, commit and push. The WI005 write adapter is not
used. MA native execution and safe-receipt interpretation are initially pending;
any later completion is recorded in an additive section below.

## Reference Documents and Method

The [WI009 handoff](WI-20261023-ATS-009-handoff.md) injects Tier0
[core principles](../../docs/standards/core-principles.md),
[documentation standards](../../docs/standards/documentation-standards.md),
[development standards](../../docs/standards/development-standards.md) and
[glossary](../../docs/standards/glossary.md); Tier1
[security policy](../../docs/policies/security-policy.md) and
[quality gates](../../docs/policies/quality-gates.md); Tier2
[REQ007](../user/REQ-20261011-ATS-007.md), [current entry](../../docs/index.md),
[WI007 summary](../user/WI-20261023-ATS-007-summary.md) and
[WI003 prior evidence](WI-20261023-ATS-003-evidence-pack.md).

Applied create-wi-evidence-pack and focused-test reporting. Review owns only
`build/WI20261023-009-readonly-review/` and these WI009 documents. No implementation
file was edited, no remote operation was performed by PG, and no broad project
build/auth/audio suite was run. Existing model and approval boundaries remain.

## Exact Evidence and Reproduction

- [Review receipt](../../build/WI20261023-009-readonly-review/review-receipt.json),
  SHA256 `fd27e370d61774540401aa939ecf890819e0c2168af5044f24a4c652b606ae8c`.
- [Candidate](../../build/WI20261023-008-readonly-recheck/candidate.json) and
  [operator recipe](../../build/WI20261023-008-readonly-recheck/README.md).
- [Independent tests](../../build/WI20261023-009-readonly-review/test_readonly_review.py),
  [independent output](../../build/WI20261023-009-readonly-review/independent-tests.txt)
  and [implementation output](../../build/WI20261023-009-readonly-review/implementation-tests.txt).

```powershell
python -B -m unittest discover -s build/WI20261023-008-readonly-recheck -p test_preflight.py -v
python -B -m unittest discover -s build/WI20261023-009-readonly-review -p test_readonly_review.py -v
Get-FileHash -Algorithm SHA256 build/WI20261023-008-readonly-recheck/candidate.json
```

The receipt records every candidate member hash and both test-log hashes.
Bundle verification hashes sorted relative filename, NUL byte and member bytes
in sequence. The generated payload test decodes and verifies all five embedded
sources and compares probe/inventory AST bodies without executing either.

## Review Results

- Operator equality is exact after only the docstring, new scope and new unit
  substitutions. Successful WI002 lifecycle, source/JAR/runtime pins, source-mode
  read-only flags, private socket checks and independent cleanup are preserved.
- Fixed query statement types are SET, START READ ONLY, SELECT three times and
  ROLLBACK. Date injection is rejected. No email, nickname, password/token value
  or password/token hash is projected.
- Historical total subscription rows are separate from effective entitlement.
  Parser checks total >= effective >= eligible, ordered unique IDs, exact fields,
  counts and integer types. Expired rows do not become evidence of no rows.
- Plan eligibility is active INDIVIDUAL with finite quota 1..1000000. Unlimited
  -1 is reported separately and is not eligible for the frozen WI005 proposal.
  Eligibility neither selects a plan nor authorizes a future write.
- Old scope and expired authority fail. Payload expiry is fixed when built,
  never renewed by remote execution. Inventory requires exact probe target;
  a post-stop probe supplied that same target also enforces it.
- The new unit/log is `atstudio-wi20261023-008-account-plan-source.service`.
  A retained log collision HOLDs; no deletion or silent retry name is allowed.
- No open local finding remains in this exact candidate. Independent 8 and SE 21
  tests pass; native SQL/data/cleanup are not inferred from these tests.

Verification date 2026-10-23, Windows local, evidence owner PG. Source 643 mismatch0;
retained JAR `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`
matches. Earlier WI002 4, WI005 22, WI001 68 and WI013 62 manifest members match.

## Required Native Receipts and Remaining Boundary

MA must verify the candidate and fresh absolute expiry <=900 seconds, require
initial PREFLIGHT_PASS, bind inventory and post-stop probe to that exact target,
then require inventory PASS, STOP/PID_ZERO/SOCKET_GONE/BASELINE all PASS and a
target-bound post-stop PREFLIGHT_PASS. SQL engine/log startup effects are not
business-row changes. Process lifetime or SSH return alone does not prove cleanup.

The authorization receipt describes the current human approval reference; it
does not issue a new grant. Actual account/plan contents, password knowledge and
native restoration remain unverified until MA provides the appropriate receipts.
Fixture/DML/login/audio authority remains absent after a successful read.

No business rollback is needed for local review. Preserve all old tools, logs and
claims. MA alone coordinates authorized native execution; PG will interpret its
safe local receipts and then notify MA/DocOps to complete WI010 integration.

## Native Result Interpretation Addendum (2026-10-23)

MA subsequently executed the approved read-only procedure. PG independently
read its local sanitized receipts and compared the three generated payloads;
PG did not connect to the server. [Independent interpretation](../../build/WI20261023-009-readonly-review/native-interpretation.json)
SHA256 is `749977f411fac5aa0d78ae9354b9988e41ae2631dc435e0109f2fb3fc09db66b`.

The [successful preprobe](../../build/WI20261023-recheck-native/probe-result-02.json),
[inventory](../../build/WI20261023-recheck-native/inventory-result.json) and
[post-stop probe](../../build/WI20261023-recheck-native/post-stop-probe.json) use
the same authorization scope, expiry and target. Generated probe/inventory/post
payloads have identical AST bodies after their five constant assignments; all
five embedded sources match reviewed local bytes. Inventory and post-stop probe
bind the successful preprobe's exact boot/machine pair. The inventory payload
SHA256 is `085b9d940893202dd30234f0a3a1c04c312cba6734dc4b76c9a99fffe695e02e`.

Inventory is `INVENTORY_OBSERVED`/PASS. STOP, PID_ZERO, SOCKET_GONE and BASELINE
are all PASS; the independent target-bound post-stop probe is PREFLIGHT_PASS.
This establishes the recorded bounded DB restoration, not overall provider/SSH
access cleanup. Final access restoration is pending a separate MA receipt at
this addendum's creation.

Observed on application day 2026-10-23 Asia/Seoul:

| Observation | Result |
| --- | --- |
| Existing users | Only ID 1, USER; no ADMIN |
| ID 1 status | Verified, not deleted, local password present, latest-auth-signup provenance |
| ID 1 subscription rows | Total 0 and effective 0; historical absence is now directly observed |
| Pending audio / unfinished storage | 0 / 0 |
| Plans | Six existing rows |
| Active finite INDIVIDUAL candidates | ID 1 quota 5/day; ID 2 quota 20/day |
| Other plans | ID 3 unlimited INDIVIDUAL; IDs 4/5/6 BUSINESS; not eligible under frozen WI005 contract |
| Password knowledge / actual login | NOT_CHECKED |

For a separately approved one-download fixture, plan ID 1 has the smallest
eligible quota observed. This is a recommendation only: no plan was selected,
seeded or changed; no fixture/account/subscription DML, app start, login, mail or
audio trial occurred. Future fixture execution still needs its own exact inputs,
plan/date approval and current target/data gates. Native audio hash comparison
remains NOT_RUN.

The [first probe](../../build/WI20261023-recheck-native/probe-result.json) is
retained as GUARD HOLD with inventory NOT_RUN and no DB startup. The subsequent
[clock sample](../../build/WI20261023-recheck-native/clock-check.json) showed the
local clock two seconds ahead, with 857 seconds of remote validity remaining.
A payload initially built at local +900 seconds can therefore exceed the
server-side <=900 bound briefly. This supports a clock-boundary explanation;
the sanitized GUARD receipt does not identify the exact failing check, so the
specific cause is not proven. MA reports that retrying the same probe bytes
passed; it did not rebuild or relax the candidate. Preserve both results rather
than replacing the earlier HOLD with a retrospective PASS.

## Final Access Restoration and WI Completion

The later [MA final restoration receipt](../../build/WI20261023-recheck-native/final-restoration.json),
SHA256 `ad00aec62f49a879a9faa74421fc0d53eff9d5d903f2a4751dd3f71b4b1470e6`,
supersedes only the access-pending state above. Its observation time is
2026-10-23T21:35:55.5548638+09:00. The prior native interpretation is preserved
unchanged. [Separate access interpretation](../../build/WI20261023-009-readonly-review/access-interpretation.json)
SHA256 is `7fc5ffc1fdddd70436958aea8cbb98fc2340d51a631d64cb7a0478d1115fb873`.

MA records the original TCP22 source rule restored with saved readback,
ports 80/443 and other rules preserved, provider logout confirmed by its login
link, and all created browser tabs closed with zero remaining IAB tabs. The
owned SSH key removal succeeded, zero agent identities remained, and the agent
was Stopped/Disabled. PG verified the local receipt's consistency, not the
provider or OS UI independently.

The owned input process/window closure is **not confirmed**: MA's last recorded
process observation was present and `owned_input_window_closed=false`. Do not
claim that every native window/process was closed. This limitation is separate
from the recorded key removal, disabled agent and restored firewall.

Both local independent review and native safe-receipt interpretation are now
complete. MA and DocOps were notified to finalize WI010. A future fixture still
requires its own exact approval, private inputs and fresh WI005 schema/snapshot
and collision inspection; this WI008 projection cannot replace that inspection.
No fixture/account/business/schema mutation, app start/login or audio trial was
authorized by this read-only operation. REQ007 remains active.
