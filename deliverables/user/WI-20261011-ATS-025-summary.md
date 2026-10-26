---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261011-ATS-025-evidence-pack.md
    reason: Preliminary pins, comparisons and limitations
---

# WI025 Bounded Native-Use Review

> Purpose: State the exact v2 conditional native-use gate and verification limits.

**TL;DR: v2 CONDITIONAL ALLOW; F01-F03 resolved; native NOT_RUN.**
The full bounded runtime/configuration/README review found no additional
confirmed blocker. PG independently ran owner42/42, prior PG8/8 and HTTP3/3:
all PASS. These suites overlap; they are not53 independent behavior proofs.

Final bundle:
`ae5f468e51655bedb71f997dddd18cb7ba3a50b7afcee33fdcdc5980eb960423`.
Final runtime:
`0d3fbcc65ee0af4c31a2dfcaa0767cfabce5d69d9f27f76d537aeb36f63f4566`.
Separately pinned README:
`74571c45524a93d7b888809c82e5a3dd0bbaa6995c84e894e57eded568f8881e`.

MA may use only that README's exact staging/approval/once-invocation blocks
after confirming the pinned target, seven attestations, new exclusive paths,
unchanged pins and fresh boot-bound approval. No alternate installer or v1
fallback. Any runtime guard failure is HOLD; no automatic retry or deletion.
The historical approval-absence flag is not the current launch gate.

The native command is:
```sh
sudo -n env -i PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -I -S -B /opt/atstudio/wi024-tools/runtime.py --run </dev/null
```

The Evidence Pack records all artifact/codeblock pins and conditional scope.
Local ALLOW is not first-start PASS, Internet/IPv6 delivery proof or production
approval. Retain the recovery console; verify app/DB stop/remask, no remaining
ports/jobs and fixed baseline restoration. Raw logs/secrets remain private.
WI025 review is complete for v2; MA owns native observations and WI026 follow-up.

## Historical Preliminary Review

The remaining sections preserve earlier snapshots and superseded HOLD findings.

**PRELIMINARY ONLY. Execution HOLD; WI025 remains open.**

No confirmed security defect was found in the three reviewed public files.
Six inert static comparisons passed. Existing canonical OFF/strict/DDL settings
and limits are preserved; the public overlay is byte-identical to the original.

## Early Findings

1. Unit line16 now calls `runtime.py --prestart`. Final review needs the completed
   same-unit fail-closed proof and operator tests for one attempt, receiver
   lifetime and app/DB cleanup/remask on every failure. Unit text is not proof.
2. Lines104-105 introduce an append log path. The finished operator must establish
   private ownership/modes, no symlink or existing-file reuse, and no raw-log
   output. No actual unsafe file was observed or read.
3. `env-shape.json` is the old public bootstrap shape, not effective runtime
   configuration. Audio=true and placeholders are overridden for first start.
   The private parser must bind exact targets and preserve all approved keyring
   entries without regenerating or printing the private environment.

## Next Gate

MA's WI023 readonly observation is attributed evidence, not PG native validation
or network-filter PASS. Completed exact WI024 runtime/operator/test bytes are
required for final review. No native action, repair, source edit or sub-delegation
was performed. No polling or full application/security audit was added.

## Runtime Preliminary Addendum

At approximately12:34 KST, the evolving runtime produced two concrete findings:

- **P1 / F01:** absent bpftool returns None and allows attachment comparison to
  be skipped; require HOLD instead. MA's current working-tool observation does
  not justify a future missing-tool fallback.
- **P2 / F02:** HTTP timeout2 does not enforce the approved request-total5s
  budget; add a cumulative deadline and a trickle-response regression.

Independent inert AST tests:8 run,6 PASS,2 FAIL reproducing these findings.
Mocked cleanup continued after a stop failure and could not retain PASS.
Root-created probe/grant mode intent and the repaired dataList fixture passed.
systemd255 CIDR/address-family and bpftool7.4/libbpf1.4 JSON formats were checked
against the versioned upstream sources linked in the Evidence Pack; this is
not native enforcement proof. The changed unit now uses Python `-I -S -B`.

Owner bytes are still changing. Snapshot hashes are recorded for traceability,
not as executable approvals. The earlier6/6 static result belongs to the old
unit snapshot. Final exact bundle and owner/independent test gates remain open.
No native action, owner-file repair or sub-delegation occurred.

## Frozen HTTP Addendum

**P2 / PG025-F03 confirmed: do not execute this frozen candidate.**
`runtime.py:657` calls the socket-changing budget check after draining the
response; line641 touches a descriptor that stdlib can already have closed.
Both normal200 Content-Length and chunked close-response fixtures reproduce
EBADF. This is a correctness blocker, not a secret leak or observed native
failure. MA raised the concern; PG independently verified it with stdlib
parsing and memory-only socket lifetime fixtures.

- Independent lifetime tests:3 run,1 PASS,2 ERROR reproducing F03; no sockets.
- Independent unchanged prior regressions:8/8 PASS; F01/F02 reproductions fixed.
- Owner41/41 remains attributed SA evidence, not a PG final rerun/signoff.
- Bundle independently repinned:
  `d9493b397847e07015e66d751e24d37815c1a528eeb569dceb934dfdb308b976`.
- Runtime independently repinned:
  `88026d714cb8b50ac8e60b298180c40eb16f0239f8559add0b81a3475263f783`.

Required owner correction: retain the interrupting5s deadline and pre-IO
timeout updates, but make the post-body check elapsed/expired-only. Refreeze
the corrected bytes and README pins before final whole-bundle review.
PG did not repair owner files. Native NOT_RUN; no staging approval is issued.

MA's12:46:13 readonly23/24 observation is attributed, not re-executed by PG.
The sole historical approval-absence flag is not a current gate or this block.
WI025 remains open; detailed pins, upstream sources and commands are in the
Evidence Pack. No repeated polling or expanded general audit is required.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-025-evidence-pack.md): Exact three-file hashes and6/6 comparison evidence.
- [Static tests](../../build/WI025-review/test_partial_config.py): Public-only reproduction.
- [HTTP lifetime test](../../build/WI025-review/test_http_lifetime.py): F03 reproduction.
