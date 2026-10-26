---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-023-evidence-pack.md
    reason: Exact pins, safety decision and parser finding
  - path: REQ-20261011-ATS-003.md
    reason: Approved bounded scope
---

# WI023 Read-Only Inspector Review

> Purpose: Separate permission to collect metadata from acceptance of metadata or application startup.

## Decision

**ALLOW_WITH_CONDITIONS: exact inspector read-only execution as root.
Metadata acceptance and startup remain HOLD. PG native work NOT_RUN.**

Inspector SHA-256:
`d2d5d3e38339f3d42c1c66c11d15ed1eec3b0841aab4529f6741e342450c55b7`.

MA must verify the exact target/hash and trusted root-owned non-symlink path,
then use `/usr/bin/python3 -I -B <reviewed-absolute-path> --inspect`.
No known private-body read/hash/output or service mutation path was found.
The18 fixed commands are read-only. Java/FFmpeg/MySQL are not launched.
This is bounded source review, not a guarantee about an unverified host/toolchain.

PG's earlier execution HOLD was overbroad and is withdrawn. The user's clarified
request separates native read safety from diagnostic completeness. This does
not erase or waive the following unresolved finding.

## Finding and Tests

**P2 / PG023-F01:** an invalid empty-object `addr_info` is accepted as zero
addresses and can leave `inspection_complete=true`. Reject the malformed
nested type in an owner repair and re-review the new bytes. Do not use current
address counts or completion status to discharge any prerequisite.

- Owner tests:26/26 PASS; independently rerun.
- Independent tests:14 run,13 PASS,1 FAIL reproducing F01; not an all-green gate.
- Tested locally on Windows/Python3.14.3,2026-10-11; exact owner pins unchanged
  at12:15:31 KST. No native inspection or service/DB/secret operation by PG.

## Limits and Next Step

Exit2/HOLD is expected, even after collection. Target identity, effective
configuration, network/BPF/relay controls, capacity and first-start are not
proved. MA may collect only bounded observations and pass them to SA024 with
these limitations. Unknown/failed observations remain unresolved.

Staging Markdown is NOT_EXECUTED and does not approve unexamined root-copy
code. No JAR copy, production-link change, service operation, private content
read, deletion, install, commit or push occurred. Historical evidence remains
intact. IPv4-only implementation/review belongs to WI024/025; REQ003 stays open.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-023-evidence-pack.md): Pins, reproduction and conditions.
- [Independent test](../../build/WI023-review/test_inspector_boundaries.py): Inert boundary tests and F01 regression.
- [REQ003](REQ-20261011-ATS-003.md): Current authority and WI chain.
