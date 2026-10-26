---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261011-ATS-027-evidence-pack.md
    reason: Exact pins, tests, conditions and limitations
---

# WI027 Bounded Review

> Purpose: State the independent gate for the fresh WI026 attempt.

**TL;DR: CONDITIONAL ALLOW. No confirmed blocker or running test remains.
WI026 staging/native and Internet proof remain NOT_RUN at the latest MA report.**

The runtime/unit delta is only fresh identities and the new private log path.
The strict ancestry checks remain; old `/var/log`0775 still rejects and supplied
`/var/lib/atstudio`0755 metadata accepts the new root0700 log leaf. No host
permission weakening. All21 WI024 files were verified unchanged; WI025 evidence
is preserved and supplies the review of unchanged containment/cleanup behavior.

Independent tests:42/42 owner,8/8 prior PG,3/3 HTTP,4/4 ancestry,6/6 new review
checks PASS. They overlap and are not63 unique native/security proofs.

| Current identity | SHA-256 |
|---|---|
| Bundle | `7136e749de6b7dd2ae6ecbf2585a3dc1ddb0fd083ef81ab48f96e2f82aa44f05` |
| Runtime | `c61f590efabae4b94196cfde13b1773c67becb04bdfff8b1e954a022a3f08274` |
| README | `e2305e742db1af5da6a272c31d3b641fc2386e96053f7511fb99035c23c52360` |

MA may use only that README's three exactly pinned blocks with the approved
pinned SSH stdin transport, fresh seven attestations and new WI026 paths/claim/
approval. One invocation only; changed bytes, expired approval, existing paths
or failed guards mean HOLD. No automatic retry, old claim reuse, alternative
installer, server permission change or deletion is approved.

The current preflight is5831-byte `6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383`.
MA reports13:11:58 KST mysql_config=true and23/24 true; only historical approval
absence is false and retained. The accidentally selected old fbd216 checker and
its22/24 diagnostic are historical, not a current blocker or permission to weaken
the gate. No server/checker change was made. PG did not run a native checker.

Local ALLOW is not native success. Keep the recovery console, private log
boundary, RuntimeMax180s and verified app/DB stop/remask. Preserve partials on
failure. MA owns the approved one attempt and WI028 outcome integration.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-027-evidence-pack.md): All file/codeblock pins, actual tests and conditions.
- [Exact README](../../build/WI026-private-start/README.md): Staging, approval and invocation.
