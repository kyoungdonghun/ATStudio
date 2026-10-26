---
version: 1.2.0
last_updated: 2026-10-23
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-012
---

# Evidence Pack: WI-20261023-ATS-012

## Summary and Authorization Boundary

`ALLOW_EXACT_APPROVED_INSPECT_ONLY_PREPARATION_AND_MA_EXECUTION` for WI011
candidate SHA256 `21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245`,
five members, bundle `69b0dcd464ff08f690ab7274c744ff617fe70d9a618ea2d9a760fbe807d085a2`.
MA received this exact allowance before actual private capture/native inspection.
Implementation 8 and independent 10 tests pass, as does the actual Windows
synthetic ACL/validation/collision selftest. No open local finding remains.

Current explicit user authorization covers private input/evidence retention,
exact staging and read-only inspection with temporary DB start/stop. Agreement
with future effect scope and TEST verification semantics does not authorize an
apply plan, entitlement date, account creation, promotion, subscription or other
business DML. Application, login, audio, mail, payment, publication, deletion,
commit and push are excluded. Existing ID1 and all existing rows remain immutable.

PG performed only local synthetic review and did not collect or read real
inputs, open a capture UI, contact a server or execute a database/application.
Native execution and access restoration remain MA-owned and pending receipt
interpretation. This local allowance is not a native success claim.

## Method and References

The [WI012 handoff](WI-20261023-ATS-012-handoff.md) supplies the approved scope and
injected Tier0 [core principles](../../docs/standards/core-principles.md),
[documentation standards](../../docs/standards/documentation-standards.md),
[development standards](../../docs/standards/development-standards.md) and
[glossary](../../docs/standards/glossary.md); Tier1
[security policy](../../docs/policies/security-policy.md) and
[quality gates](../../docs/policies/quality-gates.md); Tier2
[REQ007](../user/REQ-20261011-ATS-007.md), [current entry](../../docs/index.md),
[WI007 summary](../user/WI-20261023-ATS-007-summary.md) and
[WI010 summary](../user/WI-20261023-ATS-010-summary.md).

Applied create-wi-evidence-pack and focused-test reporting. Reviewed the new
wrapper against the preserved [WI005 recipe](../../build/WI20261023-005-account-fixture/README.md)
and [WI006 review](WI-20261023-ATS-006-evidence-pack.md). Prior
[WI009 native interpretation](WI-20261023-ATS-009-evidence-pack.md) is dated
read-only evidence, not proof of new-input noncollision or present schema state.
No implementation edits or broad project/auth/audio tests were performed.

## Exact Evidence and Reproduction

- [Review receipt](../../build/WI20261023-012-fixture-inspect-review/review-receipt.json),
  SHA256 `1de3b8b0543164439c88006d8142822505c702bc8237ac94aa1b468fc0a2072a`.
- [Candidate](../../build/WI20261023-011-fixture-inspect/candidate.json) and
  [operator recipe](../../build/WI20261023-011-fixture-inspect/README.md).
- [Independent tests](../../build/WI20261023-012-fixture-inspect-review/test_inspect_review.py),
  [output](../../build/WI20261023-012-fixture-inspect-review/independent-tests.txt),
  [implementation output](../../build/WI20261023-012-fixture-inspect-review/implementation-tests.txt)
  and [Windows selftest output](../../build/WI20261023-012-fixture-inspect-review/windows-selftest.txt).
- [Identity verifier](../../build/WI20261023-012-fixture-inspect-review/verify_review.py)
  checks all five candidate files, sorted filename/NUL/member-byte bundle,
  retained source 643 and exact frozen WI005 tree 22 plus its manifest.

```powershell
python -B -m unittest discover -s build/WI20261023-011-fixture-inspect -p test_inspect.py -v
python -B -m unittest discover -s build/WI20261023-012-fixture-inspect-review -p test_inspect_review.py -v
powershell.exe -NoProfile -NonInteractive -ExecutionPolicy Bypass -File build/WI20261023-011-fixture-inspect/capture.ps1 -Action SelfTest
python -B build/WI20261023-012-fixture-inspect-review/verify_review.py
```

Verification date: 2026-10-23, Windows local, owner PG. Source 643 mismatch0;
retained JAR `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`
matches. Frozen WI005 manifest remains
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`,
all 22 members unchanged. Branch/HEAD and safe test-log hashes are in the receipt.

## Findings Closed and Tested Boundaries

- The exact raw external MA operator record and independently supplied pin are
  verified locally and remotely, then retained privately. A marker string is
  not human authority: MA must establish the actual current approval. The new
  wrapper only translates that decision into frozen legacy `mode=inspect`
  records; both `allow_writes=false` and the literal native call prevent widening.
- Private data travels through stdin and protected files, never secret argv/env
  or public diagnostics. Payload and input digests remain private. Public
  projection discards identity, cell, schema, snapshot and server hashes.
- Current-SID-only protected directory creation precedes writing; file creation
  is exclusive, with inherited private permissions during writes and explicit
  protection afterward. Existing paths, hardlinks, reparse paths and unexpected
  ACL principals HOLD. Actual synthetic Windows positive/negative ACL tests pass.
- The form masks passwords and validates confirmation and the frozen input
  contract. Overlength passwords are rejected rather than truncated at 72.
  Only entry to the form checks fresh staged readiness. Valid input is preserved
  if authority expires while typing; later native building still HOLDs.
- Staging verifies the frozen tree, runtime/source/JAR/target/cold prerequisites
  and refuses existing legacy approval or nonce evidence before capture.
  Existing matching candidate bytes can be reused without overwriting others.
- The built request has identity/input binding only: no day, plan or effects.
  AST inspection confirms one native invocation with literal `inspect`.
  No BCrypt, apply, reconcile, arbitrary SQL, application or login path is exposed.
- `INSPECTION_RECORDED` reports completed inspection. Separate readiness HOLD
  reasons cover identity collision, existing ADMIN/work, invalid ID1 state or no
  finite eligible plan. Even passing observed conditions explicitly retain
  `apply_authorized=false` and `date_selected=false`.

The first PG negative-ACL harness attempt required explicit loading of the
Windows PowerShell Security module; its subsequent ACL mutation used the full
security descriptor and lacked a requested privilege. The harness was corrected
to load that module and change only the Access DACL section. Product code was
unchanged. Final independent 10/10 pass; the initial output is retained in
[independent-initial.txt](../../build/WI20261023-012-fixture-inspect-review/independent-initial.txt).
Synthetic private test directories are retained; no cleanup deletion occurred.

## Pending Native Evidence and Stop Rules

Actual GUI interaction, target staging, current schema/identity collisions and
native lifecycle are not established by synthetic tests. MA must provide safe
staging and inspection results, all DB cleanup checks, target-bound independent
cold post-probe, and separate provider/key/agent restoration evidence. Preserve
which owner observed each surface; PG will interpret safe receipts only.

Expiry before native inspect preserves inputs. The recipe permits MA to create
a fresh private directory/nonce and actual inspect-only record, restage guards,
and exclusive-copy already captured inputs after ACL checks. No recollection,
automatic renewal/retry, date or apply authority is implied. A possibly started
operation or fixed approval/nonce collision stops for MA review and restoration
verification; no overwrite, evidence deletion or DML rollback is authorized.

## Next Chain

WI012 local allowance was sent to MA and DocOps; WI013 can integrate it while
native evidence remains pending. Any later result interpretation is additive and
must preserve this exact pre-execution review record.

## Native Inspection Addendum: 2026-10-23

`OBSERVED_READONLY_INSPECTION_AND_DB_CLEANUP_PASS`. This additive result preserves
the earlier local test and pending-state record. MA owns native execution; PG
read only the sanitized [inspection result](../../build/WI20261023-fixture-inspect-resume/inspect-result.json)
and [post-stop probe](../../build/WI20261023-fixture-inspect-resume/post-stop-probe.json).
The [independent interpretation](../../build/WI20261023-012-fixture-inspect-review/native-interpretation.json)
records their exact hashes, owner, method and limits. No private input, target
payload, database or server was opened by PG, and no additional tests ran.

The inspection receipt is `INSPECTION_RECORDED`, phase COMPLETE, SSH exit0 and
`INSPECTED_CONDITIONS_PASS`, with no HOLD reasons. ADMIN count, selected-input
identity collision count, and pending/claimed audio plus unfinished storage
mutation count are zero. The preserved frozen query gives the true ID1 guard
its precise meaning: USER, nondeleted, verified, nonempty password and no
`user_subscriptions` rows. This does not verify known credentials or login.

Six active plans were observed. The finite active INDIVIDUAL plans eligible for
the frozen fixture contract are ID1 with daily quota5 and ID2 with daily quota20.
The unlimited INDIVIDUAL and BUSINESS plans are not eligible under that contract.
No plan or date is selected: `apply_authorized=false` and `date_selected=false`.
No account creation/promotion, subscription write, app/login, mail, audio or
payment operation follows from this inspection success.

DB_SESSION, STOP, PID_ZERO, SOCKET_GONE and BASELINE are all PASS. The separately
run post-stop probe is PREFLIGHT_PASS/COLD_QUARANTINE_MATCH with inventory NOT_RUN
and SSH exit0. Its safe metadata names the current explicit saved-input
inspect-only approval and reused WI008 target-bound probe. Exact private target
and payload binding were reported by MA; PG did not independently recompute
those private pins. MA also reported source643/JAR and frozen WI005/WI011/WI014
unchanged; this interpretation did not repeat that identity scan.

Provider/firewall/key/agent restoration remains PENDING a separate final safe
receipt. DB cleanup PASS alone does not establish access restoration.

Successful inspection retains fixed native HUMAN/APPROVAL files and private
evidence by design. A later wrapper stage will reject those existing approvals.
No automatic new inspection, expiry renewal or apply can be assumed runnable
unchanged. Any future approval/collision handling must first be planned and
reviewed locally under the applicable authority; no deletion or replacement is
authorized by this result. This future gate does not negate the completed
read-only inspection or DB cleanup.

## Access Restoration Addendum: 2026-10-23

`BOUNDED_READONLY_INSPECTION_AND_RESTORATION_ACCEPTED`. This later addendum
supersedes only the earlier access-PENDING state. The native interpretation
remains byte-for-byte unchanged with SHA256
`29d5a07f745c195bcc2d4ad393a644efaf11eba2dd3f7a2550401126a8ceeb76`.

MA's [final restoration receipt](../../build/WI20261023-fixture-inspect-resume/final-restoration.json),
SHA256 `98aa3c5f0024d2516210cd909f7e38f85c7a5da7bf935606e601631dea8d57bc`,
reports RESTORATION_CONFIRMED. It records original TCP22 source restoration,
unchanged ports80/443, provider logout, zero created browser tabs remaining,
owned SSH key removal, zero identities before agent stop, agent Stopped/Disabled,
key input window closure, private ACL verification and retained private inputs.
The five DB cleanup results and independent cold probe remain consistent with
the separately read native receipts.

PG's [access interpretation](../../build/WI20261023-012-fixture-inspect-review/access-interpretation.json)
checks the safe receipt and existence of its referenced firewall/logout images.
PG did not inspect image contents, use the browser, query OS services, access
private input or reconnect to the server. Browser, firewall, key and window facts
remain MA-owned observations. Key input window closure is not a claim that every
unrelated process/window was closed.

The bounded saved-input read-only inspection and restoration are accepted.
Business changes, app start/login, date selection, apply and audio trial remain
false. No credential login, account/role/subscription creation, mail/payment event
or native audio acceptance is established. Fixed approvals and private evidence
remain retained; future renewal/collision handling is still a local planning and
review gate with applicable approval, not an automatic next server operation.
WI013 receives this completed interpretation; no further PG operational work is
required for the current bounded inspection.
