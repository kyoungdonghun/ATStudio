---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: PG
category: reference
status: active
---
# WI007 Review Status

HOLD: the new unarmed cleanup success path must also validate pinned quarantine configuration hashes. The existing service/listener checks alone do not prove those file bytes. A focused fix and regression are required before native approval.

Independent Python suites and three real Java17 loopback TLS negative probes passed. These results do not override the uncovered cleanup gap or establish target-server readiness.

Exact receipt: `build/WI071-review/pg-dns-v8-review-20261021.json`.

## R1 Update
The narrow quarantine hash fix and independent regression passed. Exact v8r1 local approval was issued at `build/WI071-review/pg-final-dns-v8r1.json`, with conditions in `pg-dns-v8-r1-review-20261021.json`. Native Java17/TLS proofs remain mandatory before human readiness. The original HOLD above is retained as historical evidence, not the current r1 verdict.
