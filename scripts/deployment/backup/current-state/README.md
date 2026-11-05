---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: SE
category: guide
status: draft
---
# Current approved-state backup successor

REQ-20261026-ATS-002 / WI-20261026-ATS-008. Local preparation only. This code does
not authorize server access, database startup, repository requests, a new backup,
isolated restoration, payments, or new costs. Native acceptance remains NOT_RUN.

## Why this successor exists

The retained successful WI011/WI017 candidate expects ID1 USER and audit count
1/2. The approved stage-two completion changed ID1 to ADMIN and added two audit
rows. Simply widening the count or accepting whatever is observed would lose
the approved-state boundary.

`current_state.py` requires IDs1/2 ADMIN and ID3 USER, verified and nondeleted;
exactly the six existing plans (prices, quotas and limits); and grant1 for user3,
plan1, MONTHLY/CANCELLED, 2026-10-24 through 2026-11-24 with no pending change.
No grant date or database row is modified.

## Preparation and checks

From the repository root:

```text
python -B scripts/deployment/backup/current-state/prepare_successor.py
python -B scripts/deployment/backup/current-state/test_current_state.py
```

The first command is inert. To assemble an additional candidate, choose a new,
nonexistent child of `build/WI-20261026-ATS-008/` and pass `--output <new-child>`.
The parent must already exist. Preparation authenticates the original 19-file
WI011 and 8-file WI017 bundles and the retained 43-table schema, applies exact
context patches, compiles source without executing it, and writes new pinned
`backup/` and `stage/` bundles. It then rechecks originals and derived trees.
Existing destinations, altered sources and symlink/reparse paths fail closed.

The ignored output retains target-bound source constants from the historical
tools. It must not be force-added to Git or shared. No private input, snapshot,
credential file, run authority, or anchor is copied or created. Public code and
tests contain no actual host/repository credentials or current database hashes.

## Evidence contract for a later authorized session

Capture approval adds `approved_state_anchor` and
`approved_state_anchor_sha256`. The raw external decision includes both, and the
retained decision/candidate/target/time checks remain active. Inventory mode
requires both fields to be null and starts no database.

The anchor is independently established from the protected, reconciled
post-stage-two evidence, **not from the current observation**:

| Field | Required meaning |
|---|---|
| `schema` | `ats-approved-state-v1` |
| `evidence_kind` | `NATIVE_POST_STAGE_TWO_RECONCILIATION`; SYNTHETIC is rejected |
| `source_wi` | `WI-20261025-ATS-003` |
| `source_receipt_sha256` | Independently verified protected applied/reconciliation evidence identity |
| `machine_id_sha256` | Same protected target identity as the new approval |
| `schema_sha256` | Canonical retained column/primary-key registry digest |
| `snapshot_sha256` | Approved whole-43-table canonical cell-hash snapshot digest |
| `table_counts` | Exact named 43-table counts, including the current audit baseline |

Anchor preparation/reconciliation is NOT_RUN in WI008. The operator must verify
the source receipt and expected-after state through the established private
evidence flow before issuing the external decision pin. A JSON label or digest
alone is not evidence authenticity. The code cannot prove the provenance of an
arbitrarily supplied operator authorization. It has no function that promotes a
fresh, differing state into an approved anchor.

The existing source-DB window performs a new SELECT-only consistent transaction:
it checks all column names/order/primary fields, reads each table through bounded
queries, and hashes cells in MySQL using the same representation as the retained
stage-two snapshot. Python canonicalization matches the previous `fixture_core`
format. This includes audit content, session/reset state and all other rows,
not just the three selected fixture table counts. Source dump counts must match
that observation before new backup admission. A same-count content change fails.

No cleartext private cell values are returned by this new observer. The snapshot
and its hashes still constitute private evidence; they are not public report
data. Primary identifiers are numeric. Per-table maximum 256 rows and aggregate
response maximum 4 MiB are unchanged in intent from the bounded fixture snapshot.
A larger dataset or changed schema requires separate review, not relaxed limits.
Dates/DECIMAL/null/cell canonicalization are tested locally against the retained
implementation; real MySQL representation on the next target is not newly proved.
Column type metadata and deterministic dump equivalence continue to use the
retained complete SQL/isolated-restore checks.

## Existing controls retained

- WI017's schema selection fix and complete release inventory are connected in
  the successor serializer; the successful cost modules remain byte-identical.
- Existing target/source/JAR/runtime pins, one-use paths, cost/retention evidence,
  source quietness, full SQL capture and exact new snapshot isolated restore remain.
- Existing process ownership precedes startup; STOP, PID_ZERO, SOCKET_GONE and
  COLD_BASELINE results remain separate. A failed check is not converted to PASS.
- Unknown backup outcome remains POSSIBLE/HOLD with automatic retry false. Read-only
  reconciliation must precede any later retry; no snapshot deletion is added.
- No repository initialization, restore over original DB, secret/config expansion,
  app startup, retention deletion, or automatic scheduling is introduced.

The prepared command interface remains the derived `stage/stage_one.py` inventory,
capture and check-cost interface, with new candidate and scope pins. Do not call
the base backup entry directly: that would omit WI017's release/schema/cost
extension. No native example with real inputs or execution approval is supplied.

## Validation boundary

Synthetic tests exercise the connected derived serializer, authority, adapter
admission, exact snapshot parser and existing cleanup using injected command/file
seams. They do not launch MySQL/restic/SSH or contact a provider. Synthetic records
are in-memory only and must never be used as issued native authority. The new
successor's inherited historical test files are provenance copies, not a claim
that their old role/count fixtures are valid for the new contract. The public
focused suite tests the new boundary; unchanged WI017 tests cover the retained
fixes separately. Original successful trials are historical evidence, not new
native success.
