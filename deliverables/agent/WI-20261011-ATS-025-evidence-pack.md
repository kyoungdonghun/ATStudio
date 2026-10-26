---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261011-ATS-025-handoff.md
    reason: Preliminary and final review boundaries
  - path: WI-20261011-ATS-024-handoff.md
    reason: Approved IPv4-only implementation contract
  - path: ../user/REQ-20261011-ATS-003.md
    reason: Approved scope and successor chain
---

# Evidence Pack: WI-20261011-ATS-025

> Purpose: Record bounded independent review and exact v2 conditional native-use gate.

**Current gate: CONDITIONAL ALLOW for the exact v2 bundle and README only.
See Final v2 Gate below. F01-F03 are resolved in that snapshot. Native NOT_RUN;
earlier preliminary/HOLD sections and v1 hashes remain historical, not current
execution authority.**

## Summary / Gate

**PRELIMINARY ONLY. Initial six static comparisons PASS; the later runtime
review below reproduces two defects. Execution approval HOLD. WI025 remains
open. PG native activity NOT_RUN.**

No confirmed security defect was established in these three configuration files.
The following are concrete final-review dependencies, not findings that the
unfinished operator already violates them. That initial pass excluded runtime
and operator inspection. The addendum below extends review to the evolving
runtime. Do not use this partial report to run the unit.

## Early Findings for MA and SA024

1. **HIGH gate: the new prestart is executable, not the historical false gate.**
   `build/WI024-private-start/atstudio-first-start.service:16` now invokes
   `runtime.py --prestart`. Final review must prove it rejects missing/stale
   receiver observations, failed positive controls, unexpected inherited FDs
   and failed AF_INET6/AF_UNIX denial before Java. The unit preserves
   `Restart=no`,180s and stop limits, but these declarations alone do not prove
   one-use authorization, receiver lifetime, DB remask or cleanup on every
   operator failure. Those are explicit unfinished-bundle checks, not new scope.
2. **HIGH confidentiality dependency: protect the new raw-log sink before use.**
   Unit lines104-105 append stdout/stderr to
   `/var/log/atstudio-wi024/first-start.log`. The unit text does not establish
   the existing inode, ownership, mode or symlink ancestry of that destination.
   The completed installer/operator must provide exclusive/private creation
   and descriptor/path checks, preserve existing files on collision, and never
   publish raw application/prestart output. `UMask=0077` is present but is not
   evidence of the state of an already existing path. No actual unsafe log
   file was observed; no native path was read.
3. **MEDIUM correctness dependency: distinguish bootstrap shape from effective values.**
   `env-shape.json:3,42,46,52-54,75-77` reproduces the public bootstrap template,
   including audio=true, trusted-client=true, classpath:/, SMTP placeholders
   and one example billing-key entry. `public.env` and canonical CLI arguments
   override the first-start behavior. Do not treat the shape as effective
   runtime configuration or generate/replace the private environment from it.
   Final parser tests must reject duplicates/unknown injections, bind the exact
   approved JDBC schema/account, handle retained template placeholders safely,
   and preserve the complete existing approved keyring without truncating it
   to the example key0. No private contents were read, so their shape is unknown
   to this reviewer.

## Verified Configuration Delta

- All canonical `--...` application argument lines are identical to the original
  candidate: background/audio/bootstrap/auth/notification OFF; SQL never;
  DDL validate; strict storage/startup guards; loopback bind; disabled Provider
  credentials and numeric nonfunctional SMTP destination are preserved.
- Public overlay is byte-identical to the original candidate overlay.
- JSON shape is exactly the parsed public bootstrap template, with only its
  quoted JDBC value decoded. It is not a dump of native configuration.
- New unit fixes the private-env/public-overlay/JAR paths, adds Java IPv4 stack
  preference, narrows address-family declaration to AF_INET and allowance to
  127.0.0.1/32, uses null stdin and the new append log sink.
- Existing user/group, resource limits, hardening, no-restart and bounded
  start/stop declarations are unchanged. No Install section was added.
- These are static comparisons, not systemd parsing, cgroup/BPF enforcement,
  Java17 compatibility, network delivery or successful cleanup evidence.

## Exact Preliminary Snapshot

Windows/PowerShell, Python3.14.3,2026-10-11. Initial hash observation at
12:20:39 KST, rechecked before the six tests. Local HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`; inherited dirty worktree retained.
Owner work is ongoing; any changed byte requires a new final pin/review.

| Public path | SHA-256 |
|---|---|
| `build/WI024-private-start/atstudio-first-start.service` | `a1c5d64d83137faff43c8a1ec25f3fb663ce744ca7acc2146d84ce5d7ff1c48a` |
| `build/WI024-private-start/public.env` | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| `build/WI024-private-start/env-shape.json` | `e8445fd79492e17c3f816f32850ca7803a63fcfb2a9b53b45444c9fd5dd1f100` |
| `deploy/single-vps/first-start/atstudio-first-start.service.candidate` | `09d691d4b0c90813d7fdc6922cb15b11c0f58fb162d588cf8d88019c1da7941d` |
| `deploy/single-vps/first-start/first-start-public.env.example` | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| `deploy/single-vps/env/atstudio.env.example` | `412e838b532c7249d0bbb9ca44eb114a659f7a78f1e2675bf87a676392fa18b0` |

## Commands and Test Evidence

```powershell
python -I -B build/WI025-review/test_partial_config.py
git diff --no-index -- deploy/single-vps/first-start/atstudio-first-start.service.candidate build/WI024-private-start/atstudio-first-start.service
```

- Static suite:6 tests,6 PASS,0 failures/errors/skips,0.004s,exit0; receipt `b0c063`.
- The diff command exits1 because the approved separate candidate differs;
  this is not an execution/test failure. Receipt `fa5b0e`.
- Exact hashes: receipts `c7e764`, `ec9e50` and the pinned snapshot test.
- Test helper reads only the six public files above. It imports no owner
  runtime/operator, launches no subprocess and opens no socket/private file.
  Its ConfigParser comparison applies only to the observed simple public
  fixtures; it is not a systemd parser or private-env validator.
- Test and create-wi-evidence-pack skills applied; no Gradle/npm/coverage run.
  Full negative runtime/operator tests remain pending the completed owner bundle.

## Attribution, Limits and Follow-up

MA reported WI023 native read-only completion: noerrors,exit2/HOLD,
systemd255,cgroupv2,bpftool present and service ports closed. No native timestamp
or raw receipt was supplied with that message. PG attributes it to MA; no new
host observation, BPF enforcement or first-start PASS is inferred.

No source/owner repair, private read, native/service/DB/network action, install,
deletion, commit/push or sub-delegation occurred. Only this report, the summary
and `build/WI025-review/test_partial_config.py` were created by PG.

Rollback is withholding this unfinished candidate; no native rollback occurred.
Do not close WI025 or trigger native execution/WI026 completion from this report.
Return these early findings now. Resume final review when MA supplies the
completed exact WI024 bundle and test evidence; no polling loop is required.

## Reference Documents (Tier 0-2)

Tier0 STD-001/004/002/005 and PG/security injection remain unchanged from WI023,
as explicitly reconfirmed by the user. Current WI025 and WI024 handoffs were
read. REQ003, current decision entry and first-start runbook/inventory context
remain loaded from the previous bounded review. The exact original candidate,
overlay and public bootstrap were freshly read. No full source/history audit.
The memory lookup supplied no new current-state evidence.

## 2026-10-11 Runtime Preliminary Addendum

Review completed around12:34 KST while owner files were still changing.
No freeze, final bundle acceptance or native execution is implied.

### Confirmed Findings

- **PG025-F01 / P1: missing BPF tool silently weakens the proof gate.**
  `runtime.py:380-383` returns `None` when the fixed bpftool path is absent.
  At lines816,826,852,856 the operator can compare `None == None` and eventually
  return BOUNDED_PASS with `bpf_attachment_checked=false`. The approved proof
  requires attachment/effective-rule evidence, and MA reports this tool works
  on the target. Missing/unavailable tooling must HOLD, not select an optional
  proof path. Mocked missing-path input reproduces the non-rejection. This is
  not a claim that the currently observed target lacks bpftool.
- **PG025-F02 / P2: HTTP timeout is per operation, not the approved total bound.**
  `runtime.py:619-631` uses HTTPConnection timeout2 and a single bounded-size
  `response.read`, but no total5s deadline. A response making intermittent
  progress can exceed the request budget; the330s operator alarm/180s service
  lifetime is not that request budget. The inert test models10s cumulative
  delivery without a per-operation timeout and demonstrates no deadline check.
  Use a real total deadline and remaining-time-aware bounded reads; test a
  trickling response. No real network timing experiment was performed.

Pointers above were rechecked at12:34:19 KST and may shift in ongoing owner edits.
Neither defect was repaired by PG. F01 and F02 remain open pending owner fixes.

### Compatibility and Narrow Positive Evidence

- systemd255 prints IP properties as address/prefix, so `127.0.0.1/32` and the
  two deny prefixes match its formatter. Its address-family allowlist formatter
  supports `AF_INET`; USec properties use formatted timespans. These are source
  compatibility checks, not native effective-policy verification.
  Sources: [systemctl v255](https://raw.githubusercontent.com/systemd/systemd/v255/src/systemctl/systemctl-show.c),
  [CIDR formatter](https://raw.githubusercontent.com/systemd/systemd/v255/src/basic/in-addr-util.c),
  [USec formatter](https://raw.githubusercontent.com/systemd/systemd/v255/src/shared/bus-print-properties.c).
- bpftool7.4 `cgroup show ... effective` emits a JSON array with integer `id`
  and string `attach_type`; libbpf1.4 names ingress/egress as accepted by the
  current parser. Missing/empty attachments still reject in the mocked tests.
  Sources: [bpftool v7.4.0](https://raw.githubusercontent.com/libbpf/bpftool/v7.4.0/src/cgroup.c),
  [libbpf v1.4.0](https://raw.githubusercontent.com/libbpf/libbpf/v1.4.0/src/libbpf.c).
- Root staging explicitly chmods RUN0755 and probe/grant0644 after restrictive
  umask, so the configured app UID has read/traverse access in the ordinary
  DAC model. It need not write beneath ProtectSystem/ReadOnlyPaths to read the
  grant. Log descriptors are passed from a root0600 inode; prestart checks
  descriptor identity rather than opening the root0700 log directory.
  Destination JAR mode0444/directory0555 is likewise app-readable in that model.
  Actual sandbox/mount visibility and installer tool-file modes remain native
  prerequisites; no existing path or native permission was inspected by PG.
- Mocked app-stop failure did not skip app masking, MySQL stop/mask or receiver
  closure; cleanup returned failure. `cleanup_result` changed a prior PASS to
  HOLD. This is not proof against process death, SIGKILL or uninterruptible IO.
- Revised Tag ResponseDTO `dataList` validation passes the supplied empty-list
  fixture and rejects the obsolete `data` shape. Fixed SAFE_CODES and unit
  `-I -S -B` are present in the later read; the earlier empty-code placeholder
  is not presented as a current defect.

### Inert Evidence and Snapshot Pins

`python -I -B build/WI025-review/test_runtime_preliminary.py` returned exit1:
**8 tests,6 PASS,2 FAIL (F01/F02),0 errors/skips,0.023s**; receipt `27409c`.
The helper extracts only named AST functions into a mock namespace; it does
not import/execute the owner's module entrypoint, root operator or prestart.
No subprocess, socket, private read, Linux action or real delay is invoked.
The HTTP case is a cumulative-time model, not a native slow-response proof.

- Runtime bytes consumed by that test:43020;
  SHA-256 `64260a6c950551d243876b59b3295ac6e281fe54dea3fb32ed10ac39c6f5b958`.
- Revised unit SHA-256:
  `0638e908030a448c064a6d0d837954faadbc422e3f0cf5eba9edf88d7b487350`.
- Independent test SHA-256:
  `e2b75bbf134e207e48ca2f6c144682d977472ef9c67b3f4c968574ad3983c9dc`.
- Initial three-file snapshot/test is historical. Its old unit pin and exact
  prestart argv intentionally no longer match the revised unit; do not count
  that earlier6/6 as a fresh result for the new candidate.

MA's12:29:05 KST listener projection and working bpftool7.4/libbpf1.4 report
are attributed, not rerun: wildcard TCP22/80/443 have the stated sshd/systemd/
nginx owners; DNS is on other loopback addresses, DHCP on a non-loopback
interface, with no127.0.0.1-specific listener. The current listener filter does
not conflate those other addresses with the exact127.0.0.1 allowance. Their
unreachability still depends on the mandatory target filter/family proof; the
inventory alone cannot close it. No new general capture-threat audit was added.

## 2026-10-11 Frozen HTTP Lifetime Addendum

### Gate and Finding

**PG025-F03 / P2: a successful close-response fails after its body is drained.**
On the frozen `runtime.py`, line657 invokes `budget()` after `response.read`;
line641 unconditionally calls `settimeout` on the retained original socket.
For an HTTP/1.1 response carrying `Connection: close`, stdlib `getresponse`
closes the connection's socket reference while its response file keeps the
descriptor alive. Draining a Content-Length or chunked body releases that last
file reference. The subsequent timeout change raises EBADF. This is a concrete
normal-success-path defect, not a confidentiality escape or native finding.
The request header alone is not proof that every possible server response has
this behavior; both ordinary close-response fixtures reproduce it.

MA identified the lifetime concern; PG independently reproduced it using real
stdlib HTTPConnection/HTTPResponse with an in-memory reference-counted socket
double. No OS socket was created. The timer was mocked and socket creation
was blocked. Official CPython3.12 sources independently establish the close
and reference-release sequence:
[HTTP client](https://raw.githubusercontent.com/python/cpython/3.12/Lib/http/client.py)
(`getresponse`, `read`, `_close_conn`) and
[socket wrapper](https://raw.githubusercontent.com/python/cpython/3.12/Lib/socket.py)
(`close`, `_decref_socketios`). Local execution used Python3.14.3; target-native
behavior was not exercised.

Keep the interrupting total5s deadline and remaining-time socket limits before
blocking IO. After a full body read, check elapsed time and expiration without
touching a potentially closed descriptor. Do not suppress arbitrary EBADF or
remove the cumulative deadline. The owner must fix, test, update exact README
pins and refreeze; PG made no owner repair. Final whole-bundle approval remains
unfinished and no native staging/execution recipe is authorized by this report.

### Independent Evidence

```powershell
python -I -B build/WI025-review/test_http_lifetime.py
python -I -B build/WI025-review/test_runtime_preliminary.py
```

- Lifetime suite:3 tests,1 PASS,2 ERROR,0 assertion failures/skips,0.007s,exit1;
  receipt `632906`. Both successful-body cases error at runtime657/641 with
  errno9. The passing control verifies stdlib releases the last reference.
- Unchanged preliminary runtime suite:8 tests,8 PASS,0 failures/errors/skips,
  0.016s,exit0; receipt `95e6a3`. F01 missing-tool and F02 cumulative-time
  regressions now pass. This does not cover F03 or prove timer interruption
  under native IO. The earlier6/6 configuration result remains historical.
- Owner Evidence Pack was read:41/41 owner PASS and owner rerun8/8 PASS are
  attributed to SA, not relabelled as independent PG execution. PG has not yet
  independently rerun the owner suite for final acceptance.
- PowerShell independently recomputed the ordered four-file bundle and all
  five code/test hashes plus the separately pinned README; receipt `fed62f`.
  All match the published freeze/owner evidence. The complete README, including
  staging and approval snippets, was read; no snippet was executed.

| Artifact | Independently observed SHA-256 |
|---|---|
| Ordered runtime bundle | `d9493b397847e07015e66d751e24d37815c1a528eeb569dceb934dfdb308b976` |
| runtime.py | `88026d714cb8b50ac8e60b298180c40eb16f0239f8559add0b81a3475263f783` |
| atstudio-first-start.service | `0638e908030a448c064a6d0d837954faadbc422e3f0cf5eba9edf88d7b487350` |
| public.env | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| env-shape.json | `e8445fd79492e17c3f816f32850ca7803a63fcfb2a9b53b45444c9fd5dd1f100` |
| test_runtime.py | `74602b57a3d906ca8af34aeabf6aa9eb0fa66dfbbe00fcfd51736000aa4a81f0` |
| README.md | `3d7b2152c2d191fde60f374ea5f5df887e230902aa08564351604fa3afbf8ab7` |
| PG test_http_lifetime.py | `4dc460577b71ab10f24487b17edc4586953fc7463b3ec949c841fe6175995811` |

### Native Attribution and Next Step

MA reports readonly preflight at12:46:13 KST:23/24 true, with only retained
historical release-approval absence false. This historical metadata is not the
current execution gate or the reason for HOLD. MA reports pinned boot/SSH,
public-key access without ForceCommand, root recovery tty, TLS/no-proxy state,
MySQL mask, closed app/DB ports, no jobs, no named capture workers/services,
disabled tracing and sudo IO logging off. PG did not re-observe these native
conditions. MA also reports no runtime staging or execution yet.

Rollback remains withholding the affected frozen bundle; no native rollback
occurred. WI025 remains open for the corrected owner freeze, full exact-byte
review and independent owner/PG test reruns. No polling, helper repair, private
read, native action, deletion, commit/push or subdelegation was performed.

## Final v2 Gate

### Decision and Completed Scope

**CONDITIONAL ALLOW. No additional confirmed blocker was found in the complete
bounded operator/configuration/staging review.** This allows MA to carry out
only the exact v2 README staging, fresh approval and one-attempt invocation,
subject to all live conditions below. It is not native success, production
approval, an Internet/IPv6 delivery proof or approval of other root code.

- [x] All runtime, unit, overlay, shape, owner test and README executable bytes reviewed.
- [x] v1-to-v2 runtime delta is only the three-line F03 budget change; the timer and pre-IO limits remain.
- [x] Runtime outside public_get is AST-identical; unit/overlay/shape are byte-identical to retained v1.
- [x] Published v2 code/test sizes and hashes, ordered bundle and README independently verified.
- [x] Both README Python bodies parsed without execution; their runtime/bundle pins match v2; exact once command checked.
- [x] Independent owner42/42, unchanged PG8/8 and HTTP3/3 all PASS.
- [x] Findings, conditional target requirements, native NOT_RUN and recovery boundary distinguished.

Owner v2 completion addendum was read. F01 requires bpftool/attachments instead
of optional proof; F02 retains interrupting5s and cumulative checks; F03 now
uses `budget(update_timeout=False)` after body drain. The user's shorthand
`touch_socket=False` is not the source identifier; no handwritten substitution
is needed or authorized. Prior v1 freeze/code/test pins and README hash still
match the preserved history files checked by PG. PG did not audit all ten
historical receipts or rebuild anything.

### Exact v2 Identity

Owner freeze:2026-10-11T03:50:22Z. Independent verification:2026-10-11,
Windows/PowerShell,Python3.14.3, owner PG. Hash receipts `8c792d`, `5e5588` bracket
the independent tests and agree. The four-file bundle algorithm is the same
ordered filename/NUL/exact-byte construction defined above.

| Artifact | Final SHA-256 |
|---|---|
| Runtime bundle | `ae5f468e51655bedb71f997dddd18cb7ba3a50b7afcee33fdcdc5980eb960423` |
| runtime.py | `0d3fbcc65ee0af4c31a2dfcaa0767cfabce5d69d9f27f76d537aeb36f63f4566` |
| atstudio-first-start.service | `0638e908030a448c064a6d0d837954faadbc422e3f0cf5eba9edf88d7b487350` |
| public.env | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| env-shape.json | `e8445fd79492e17c3f816f32850ca7803a63fcfb2a9b53b45444c9fd5dd1f100` |
| test_runtime.py | `87eaa71c406b4bacfd25c9790c8b3451b6acc4851475949c798bf466838ecba6` |
| README.md | `74571c45524a93d7b888809c82e5a3dd0bbaa6995c84e894e57eded568f8881e` |

README has exactly three `sh` fenced blocks. Hashes below cover the bytes inside
each fence, preserving original line endings and the trailing line ending;
they exclude the fence lines. They are transport/extraction checks, not new code.

| Block | SHA-256 |
|---|---|
| 1: exclusive root staging | `5c2e8eb114363e2596a66e82c35a439b38a09a9e1cf37c8888597f1651f40113` |
| 2: fresh approval | `6f6a58c7c210d4173a8f394737cee6cbf519fb0948e83753e1f23fd8ffc19252` |
| 3: once invocation | `4a63c4395a185ac890bede0f253ad646d3b7a3fedcf798abc08b3059b7bc5eb0` |

### Independent Reproduction

| Command | PG result | Receipt |
|---|---|---|
| `python -I -B build/WI024-private-start/test_runtime.py` | 42/42 PASS,0.112s,exit0 | `a3d493` |
| `python -I -B build/WI025-review/test_runtime_preliminary.py` | 8/8 PASS,0.017s,exit0 | `ec7d40` |
| `python -I -B build/WI025-review/test_http_lifetime.py` | 3/3 PASS,0.002s,exit0 | `a9c5ea` |

All have zero failures/errors/skips; suites overlap and are not53 independent
proofs. The owner suite was fully read before execution: module import defines
constants/functions only, native effects are mocked and no root/native entry
is exercised. The PG lifetime suite uses actual stdlib HTTP parsing with inert
reference-counted socket bytes. No target operation or wall-clock network
deadline enforcement was tested. No full app build/tests/coverage were run.

Diff receipts `6affea`, `ace984`, `ad02e2` establish the narrow runtime change,
README hash/test-count update and one new owner regression. Exit1 is expected
for these no-index diffs. The AST/pin check receipt `5e5588` also verified both
heredoc bodies parse, the exact third block, unchanged non-HTTP runtime AST,
retained v1 code/test pins and unchanged three configuration files.

### Remaining Runtime and Recipe Review

- Fixed absolute command callers, bounded output/time handling, shell-free
  runtime execution, private-environment grammar/target/keyring checks and
  fixed public diagnostics were reviewed. Unknown private keys HOLD without
  truncation. No private values/hashes are published and no environment is sourced.
- Staging hashes bounded public source bytes before creating a fresh root
  directory; files use exclusive/no-follow creation, fixed ownership context
  and final0444 modes. No earlier destination is overwritten. Approval binds
  v2/boot/time/address and is root0600, exclusive under root0700. The approval
  snippet's `ip` capture checks its64KiB bound after capture, not as a streaming
  cap; this fixed trusted local command on the scoped host is not treated as
  arbitrary hostile-output handling. Runtime command capture is streaming-bounded.
- Root-created RUN0755 and probe/grant0644 are app-readable; root0600 log FDs
  are checked by inode/device without making the log directory app-readable.
  The pinned new JAR becomes root0444 beneath root0555. No current link changes.
- Same-unit prestart, controlled before/after TCP/UDP receivers, exact listener
  roles, mandatory BPF attachment/policy comparisons and no automatic retries
  were reviewed. Java is released only by the root nonce/PID grant after proof.
  Policy compatibility remains source-level, not native kernel/systemd proof.
- One exclusive claim, one app start, resource/time limits, exception/signal
  handling and cleanup were reviewed. Cleanup attempts remaining steps after a
  failure and downgrades success if verification fails. MySQL normal engine
  writes are within the separate explicit attestation, not a read-only DB claim.

### Conditions for MA, Exact Invocation and Recovery

1. Use the existing verified/pinned SSH target and current boot/recovery context.
   Reconfirm all seven README attestations; the approval snippet records them
   as true but does not independently prove human operational attestations.
   The prior historical release-approval absence is not a launch prerequisite.
2. Verify the exact v2 bundle/files and separately pinned README before extracting
   the three blocks. Use only its fresh atadmin0700/public0600 upload scope and
   exact root staging block. No alternate installer, edits, overwrite or fallback
   to v1 is covered. Stop on existing paths, pin mismatch or any HOLD.
3. Create the fresh root0600 approval only after those checks. It selects the
   sole UP/global IPv4 and fixed TCP55124/UDP55125; runtime independently rechecks
   assignment, free ports, receivers, boot and expiry. Ten-minute expiry or a
   failed condition is HOLD, not authority to silently regenerate/retry.
4. Run the following command exactly once through the same approved channel.
   All guards must pass; no reload/start variations, enable, restart, packages,
   firewall/TLS/DNS changes, schema/DML, secret rotation or deletion are approved.
   The runtime's reviewed daemon-reload and exact MySQL/app start/stop/mask
   operations are the only service mutations covered.

```sh
sudo -n env -i PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -I -S -B /opt/atstudio/wi024-tools/runtime.py --run </dev/null
```

Native remains NOT_RUN by PG and, as last reported by MA, not staged/executed.
Local ALLOW is not BOUNDED_PASS. Only subsequent fixed receipt plus verified
cleanup can establish this experiment's result. Do not expose raw app logs,
environment, approval content or private configuration. SIGKILL/host loss/
uninterruptible IO may prevent cleanup; retain the recovery console and exact
service stop/remask procedure. Preserve claims, approvals, releases and partials;
do not repeat the one-use attempt or delete evidence after failure.

WI025 review is complete for this exact snapshot. MA owns the already-approved
native chain and WI026 evidence integration after observing its outcome. No
subdelegation was performed by PG. Any changed executable/configuration/snippet
bytes require renewed exact-delta review; normal native unmet gates require HOLD.

## Related Documents

- [WI025 handoff](WI-20261011-ATS-025-handoff.md): Final review contract.
- [WI024 handoff](WI-20261011-ATS-024-handoff.md): Implementation ownership.
- [Summary](../user/WI-20261011-ATS-025-summary.md): Early MA findings.
- [Static comparisons](../../build/WI025-review/test_partial_config.py): Reproduction.
- [HTTP lifetime regression](../../build/WI025-review/test_http_lifetime.py): F03 reproduction without native sockets.
- [WI024 evidence](WI-20261011-ATS-024-evidence-pack.md): Attributed owner freeze and test results.
- [REQ003](../user/REQ-20261011-ATS-003.md): Approval and remaining WI chain.
