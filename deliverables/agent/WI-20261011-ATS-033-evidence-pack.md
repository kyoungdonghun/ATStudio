---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-033-handoff.md
    reason: Exact native-artifact review authority and write boundary
  - path: ../user/REQ-20261011-ATS-005.md
    reason: Fresh user-approved one-attempt scope
  - path: WI-20261011-ATS-030-evidence-pack.md
    reason: Reused reviewed diagnostic logic
  - path: WI-20261011-ATS-028-evidence-pack.md
    reason: Preserved native outcome and unresolved cleanup cause
---

# Evidence Pack: WI-20261011-ATS-033

> Purpose: Gate exact WI032 native bytes and three recipe blocks without performing native operations.

## 1. Verdict and Scope

**TL;DR: CONDITIONAL_ALLOW_EXACT_ONE_ATTEMPT under REQ005. No confirmed blocker
in the reviewed delta. PG independently ran 88/88 owner regressions and 7/7 new
focused tests. This is a code/recipe gate, not current-target or native PASS.**

PG verification: 2026-10-11, Windows / Python 3.14.3. Native operations by PG:
NOT_RUN. Implementation is read-only; only WI033 review/results and two reports
were created. No SSH, network, secrets, service operations, install, deletion,
commit, push or subdelegation.

- [x] WI029 diagnostics unchanged; only native entry, fresh namespace and REQ005/PG033 identity restored.
- [x] Unit is the exact WI026 retarget; public env and shape remain byte-identical.
- [x] Native Linux/root/argument guards, exclusive claim and no retry retained.
- [x] No code operation targets the historical masked/failed WI026 unit.
- [x] Exact four-file bundle, 11 freeze entries, README and all three blocks verified.
- [x] All 51 recorded historical files and WI028 receipt preserved.
- [x] Conditional MA prerequisites, failure handling and unverified limits explicit.

The requested owner completion file
`deliverables/agent/WI-20261011-ATS-032-evidence-pack.md` was not yet available at
the review read (receipt `9bea67`). This is a reporting limitation, not a code
failure: PG reviewed the actual frozen candidate, README, manifests and tests,
independently reproduced results, and relied on MA's explicit freeze declaration.
WI034 should include the owner completion report when available; no owner report
content is claimed reviewed here.

## 2. Exact Pins

| Artifact | SHA-256 |
| :--- | :--- |
| Four-file bundle | `11db69c6d82da14083864ea7e14fa239f0823e2665b5d1c3048c70edf9271dc4` |
| runtime.py, 49841 bytes | `189342bef8ac90496b59efe754efbc4f846a582efd409f56e19dc8adaa16857f` |
| atstudio-first-start-wi032.service | `ad70a57442060140f46009cfd582eaebb07050619b2fb6343cf3f2297a8b994a` |
| public.env | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| env-shape.json | `e8445fd79492e17c3f816f32850ca7803a63fcfb2a9b53b45444c9fd5dd1f100` |
| README.md | `1a70aea2249fd9a4b3783efd68b0165977d307be0b98aa8e4ddd1ebf65c0e284` |
| Block 1: root staging | `968b291819d727690cf90c81e5cc40af6ca993596081aa0106ed5cf5751e5534` |
| Block 2: fresh approval | `bf45c398f70ff79595fbb18d9f848249e556b36873924edd2f7a8794b28a9f29` |
| Block 3: single invocation | `61a99965ffb4bde5861e905cb09d3cbecbf7903ba7fc54760dce059e15c20325` |

Bundle computation: concatenate UTF-8 filename, NUL, exact bytes in the four-file
order above, then SHA-256. Shell blocks: UTF-8/LF, one final newline, no fences.
Use the exact extracted buffers, not rewritten/re-rendered commands. Additional
freeze/recipe/preservation and PG test pins are in the
[machine result](../../build/WI033-review/test-results.json).

## 3. Review Evidence

[Runtime](../../build/WI032-private-diagnostic-start/runtime.py), exact pin above:

| Pointer | Result |
| :--- | :--- |
| 26-57, 269, 515 | Fresh WI032 tools/approval/claim/unit/cgroup/run/log/release; REQ005/PG033 and probe identity. Shared existing env/JAR input intentionally retained. |
| 72-245, 749-839, 990-999 | WI030-reviewed error projection, command bounds, 23-step continuation, sticky HOLD and startup result retention unchanged. |
| 851-898 | Original exact path/bundle/boot/expiry/config/baseline gates and exclusive claim before mutation retained. |
| 913, 930 | Only fresh WI032 unit installed/started; no old-unit stop/unmask/reset-failed or claim reuse. |
| 1021 | Native main restored exactly to reviewed WI026 AST; wrong platform, UID, GID and arguments rejected in mocks. |

The unit differs from WI026 only in namespace/review labels. App runtime180s,
start/stop30s, Restart=no, AF_INET-only, IP deny-all with exact loopback allowance,
private root log descriptors and existing OFF/DB/storage/config controls remain.
Runtime retains operator alarm330s and individual command/read/GET bounds. The
alarm is cancelled before cleanup; 330s is not an aggregate cleanup deadline.

All three README shell bodies equal the previous reviewed recipe after only
namespace, REQ/reviewer and exact-pin replacement. Staging buffers four public
files, verifies hashes before writing, checks metadata/ancestry, and exclusively
creates root-owned outputs. Approval asserts root, bundle, one assigned UP/global
IPv4, root0700 config ancestry and exclusive root0600 approval with600s expiry.
The seven true attestations must be genuinely established by MA first; the block
does not establish them itself. The last block invokes the exact staged runtime
once in a clean isolated Python environment, stdin /dev/null. No block was run.

## 4. Independent Tests and Preservation

Applied `test` skill to the scoped Python suites; commands from repository root:

```powershell
python -I -B -m unittest discover -s build/WI032-private-diagnostic-start -p 'test_*.py' -v
python -I -B build/WI033-review/test_review.py
```

| Suite | Result | Duration | Receipt |
| :--- | :--- | :--- | :--- |
| Frozen owner suite, independently executed by PG | 88/88 PASS, exit0 | 0.366s | `af0dff` |
| New PG exact-delta/recipe/entry suite | 7/7 PASS, exit0 | 0.032s | `42966a` |

The new tests verify all freeze and bundle pins, the exact permitted whole-runtime
delta, unchanged unit restrictions/public configuration, three shell pins and
in-memory Python compilation, exact recipe retarget, mocked entry rejection and
historical hashes. No native shell block is evaluated; only isolated selected
entry functions execute with synthetic objects/mocked dispatch. Existing88 tests
include the17 previously independent diagnostic cases and actual mocked run-finally
integration. Totals overlap and are not95 native proofs. No full app build/test,
Linux timing/enforcement test, native systemd validation or Internet probe occurred.

All51 recorded WI024/WI026/WI029/WI030 files and the exact WI028 receipt match.
JAR/source identity below is MA evidence, not a new PG application/JAR audit.
No test process remains running.

## 5. MA Conditions and Observations

MA-reported observations, not independently observed by PG:

- 14:01:16 KST preflight22/23 true; only historical release-readiness approval
  absence false because evidence is retained. This is not blanket preflight PASS.
- 14:02:48 KST all9 fresh WI032 targets absent, source641/641 and JAR70306351
  bytes / `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`
  matched; same boot, recovery root TTY and pinned SSH context.
- MySQL inactive/dead/masked/PID0; historical WI026 masked/failed/PID0 untouched;
  new WI032 not-found. Nginx active/enabled/PID11156; Certbot timer active/enabled.
  Same9 listener rows, no app/DB/probe ports; reported disk90GB and memory7436MiB available.

**Conditions immediately before execution:**

1. MA retains the authorized exact target/boot, verified recovery root access,
   strict existing SSH host pin, no forwarding and approved temporary agent scope.
   Transmit exact locally hash-checked block buffers through the existing pinned
   SSH stdin pattern; no alternate installer, shell interpolation or added commands.
2. Reconfirm all seven attestations: PG033 pins, target/boot/recovery, no usable
   listener relay, pinned Nginx no-proxy, private logs without forwarding/capture,
   exclusive maintenance window, and approved existing-secret/internal engine use.
3. Fresh upload/tools/approval/claim/run/log/release/unit targets must be absent
   before their prescribed creation; after staging they must be this attempt's
   exact pinned outputs. Source JAR, parent ownership/modes, public pins, storage,
   MySQL baseline, old WI026 mask/no-process state and probe port availability
   must remain valid. Preserve old /var/log permissions and all partials.
4. Use README blocks1,2,3 in order only after prerequisites pass. Block2 uses new
   REQ005/PG033 boot-bound approval; old approval/claim is never reused. One block3
   invocation only; no automatic retry, restart, enable or reset-failed.
5. Any failed gate, changed pin, expired approval, missing receipt or cleanup
   failure means HOLD. First perform only the separately authorized read-only
   postcheck. Additional corrective action needs separate approval. Retain every
   artifact; remove only the owned temporary SSH identity and restore the agent
   state under MA's already approved access-cleanup procedure.

## 6. Limits, Rollback and Chain

This conditional gate applies only to REQ005's one private experiment, not public
release, future retries or changed bytes. On-host IPv4/BPF evidence is not Internet
or IPv6 delivery proof. Local tests do not establish target restoration, native
command outcomes or immunity to SIGKILL/host failure. No old cleanup cause can be
inferred: WI026 stays startup VERIFIED / cleanup FAILED / overall HOLD with cause
NOT_DETERMINED, irrespective of a later experiment's outcome.

Rollback before execution is to withhold the candidate. PG performed no native
rollback. After the single attempt, use its actual diagnostics and independent
bounded MA postcheck without rewriting the original result.

**WI033 complete. Next: MA's conditional one attempt, then WI034 integration.**
MA owns these actions; PG has not delegated or executed them.

## 7. Reference Traceability

Retained Tier0 STD-001/004/002/005 and Tier1 PG/security/quality context. Read the
actual skill-generated WI033 handoff and approved REQ005; reused WI030 diagnostic
review and WI028 historical evidence. Directly reviewed WI032 freeze/README/unit/
runtime/tests and WI026 recipe delta. Applied retained `test` and
`create-wi-evidence-pack` instructions. Owner completion report availability limit
is stated in section1. Shared indexes/REQ updates are outside PG's write scope.

## Related Documents

- [WI033 handoff](WI-20261011-ATS-033-handoff.md): Delegated boundary.
- [REQ005](../user/REQ-20261011-ATS-005.md): One-attempt authorization.
- [WI030 evidence](WI-20261011-ATS-030-evidence-pack.md): Prior diagnostics review.
- [WI028 evidence](WI-20261011-ATS-028-evidence-pack.md): Historical result.
- [Exact README](../../build/WI032-private-diagnostic-start/README.md): Three reviewed shell blocks.
- [Review result](../../build/WI033-review/test-results.json): Pins, tests and conditions.
- [Independent tests](../../build/WI033-review/test_review.py): Local-only reproduction.
- [User summary](../user/WI-20261011-ATS-033-summary.md): Compact gate.
