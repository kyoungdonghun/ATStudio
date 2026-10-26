---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: SA
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261011-ATS-024-handoff.md
    reason: Approved implementation scope
  - path: ../user/REQ-20261011-ATS-003.md
    reason: IPv4-only controlled first-start authorization
  - path: WI-20261011-ATS-020-evidence-pack.md
    reason: Pinned executable provenance
  - path: WI-20261011-ATS-025-handoff.md
    reason: Independent final security gate
---

# Evidence Pack: WI-20261011-ATS-024

> Purpose: Hand off the frozen bounded first-start operator for PG025 final review.

**Current: v2 replaces v1 for review; see Section 7. Sections 1-6 preserve the
historical v1 report and pins. Their live artifact links now resolve to v2;
exact v1 artifacts/receipts remain in [history/v1](../../build/WI024-private-start/history/v1/freeze.json).**

## 1. Summary and Scope

**TL;DR: LOCAL_COMPLETE; native NOT_RUN; PG025 final review remains required.**
SA prepared a separate unit, one standard-library runtime, public configuration,
focused inert tests and exact MA staging instructions. No SSH, private-file read,
target socket, service, DB operation, installation, app rebuild, commit or push
was performed by SA. Original deployment candidates and unrelated work remain intact.

- [x] Feasibility checkpoint resolved within the approved pinned-JAR/Java17 threat model; no new architecture decision required.
- [x] Separate configuration/probe/operator/test bundle; original mandatory guards retained.
- [x] Inert negative-path tests cover parsing, pins, controls, deadlines, signals and cleanup failure.
- [x] Exact Linux proof/staging/recovery recipe supplied; local results distinguished from target evidence.
- [x] Fixed diagnostic codes and one-attempt control; cleanup failure cannot report PASS in tested control flow.

The selected design and alternative HOLD are recorded in the
[operator decision](../../build/WI024-private-start/README.md#decision-and-boundary).
Host firewall/network changes, packages and external receivers are outside scope.
The earlier concern about proving every OS channel was withdrawn after the approved
threat model was clarified; no additional syscall filter or architecture was introduced.

## 2. Frozen Evidence

[freeze.json](../../build/WI024-private-start/freeze.json) records the five code/test
pins at **2026-10-11T03:39:08Z**. Runtime bundle SHA-256:
`d9493b397847e07015e66d751e24d37815c1a528eeb569dceb934dfdb308b976`.
Hash input is UTF-8 filename + NUL + exact bytes, concatenated in order:
`runtime.py`, `atstudio-first-start.service`, `public.env`, `env-shape.json`.

| Artifact | SHA-256 |
| :--- | :--- |
| runtime.py | `88026d714cb8b50ac8e60b298180c40eb16f0239f8559add0b81a3475263f783` |
| atstudio-first-start.service | `0638e908030a448c064a6d0d837954faadbc422e3f0cf5eba9edf88d7b487350` |
| public.env | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| env-shape.json | `e8445fd79492e17c3f816f32850ca7803a63fcfb2a9b53b45444c9fd5dd1f100` |
| test_runtime.py | `74602b57a3d906ca8af34aeabf6aa9eb0fa66dfbbe00fcfd51736000aa4a81f0` |
| README.md, separately pinned staging recipe | `3d7b2152c2d191fde60f374ea5f5df887e230902aa08564351604fa3afbf8ab7` |

The existing JAR is not rebuilt: 70306351 bytes,
`01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`.
Runtime requires the exact WI022 input and new WI024 destination documented in README.
The original candidate retains SHA-256
`09d691d4b0c90813d7fdc6922cb15b11c0f58fb162d588cf8d88019c1da7941d`;
the new public overlay matches the original overlay bytes. The public bootstrap
template remains `412e838b532c7249d0bbb9ca44eb114a659f7a78f1e2675bf87a676392fa18b0`.

## 3. Implementation Pointers

- [runtime.py](../../build/WI024-private-start/runtime.py): `assigned_ipv4` (line 257), strict independent address schema; `parse_env` (280), exact public shape and numeric approved DB target without private output or rewriting the keyring.
- `policy` (357), `attachment_snapshot` (381), `Receivers` (403), `prestart` (491): mandatory same-cgroup family/FD/loopback checks, effective policy and ingress/egress attachment comparison, nonce-correlated before/after controls and zero restricted deliveries.
- `public_get` (619): two fixed read-only public endpoints, bounded bodies, no redirects/auth/cookies, per-I/O timeout and independent overall five-second interruption.
- `cleanup` (714), `cleanup_result` (746), `signal_handlers` (758), `run` (763): exclusive claim, one start, fixed stages/codes, stop/remask attempts and final-state verification. SIGINT/SIGTERM/SIGHUP/SIGALRM route through cleanup.
- [unit](../../build/WI024-private-start/atstudio-first-start.service): AF_INET only, IPv4 Java stack, deny-any/allow-127.0.0.1, mandatory prestart, original Spring guards and bounded resource/runtime settings; no enable/restart/dependency start.
- [README staging](../../build/WI024-private-start/README.md#ma-staging-after-pg025-final-pass): four exclusive root tool copies, exact pins, root approval creation and one fixed invocation. No overwrite/deletion; private append-log path/file/link checks remain runtime gates.

The env shape is not a runtime env. Unknown keys, including extra keyring entries,
cause HOLD without truncation or regeneration. Raw app logs stay private; only
bounded known markers are inspected internally. Root socket SQL is aggregate
read-only observation, not schema/DML. Starting MySQL still entails normal engine
writes and requires the explicit MA attestation described in the recipe.

## 4. Test Evidence

Verification owner **SA**, **Windows / Python 3.14.3**, latest run
**2026-10-11T03:42:26Z (12:42:26 KST)**.
[Machine receipt](../../build/WI024-private-start/test-result.json).

| Command | Result | Duration | Log |
| :--- | :--- | :--- | :--- |
| `python -B build/WI024-private-start/test_runtime.py` | 41/41 PASS; zero failures/errors/skips; exit 0 | 0.107 s | [Owner log](../../build/WI024-private-start/owner-tests.log) |
| `python -I -B build/WI025-review/test_runtime_preliminary.py` | 8/8 PASS; zero failures/errors/skips; exit 0 | 0.022 s | [PG regression rerun](../../build/WI024-private-start/pg-regression.log) |

The second row is SA rerunning unchanged PG tests, **not independent final PG
signoff**. PG test SHA-256 remains
`e2b75bbf134e207e48ca2f6c144682d977472ef9c67b3f4c968574ad3983c9dc`.
The suites overlap; their counts are not 49 independent behavior proofs.

PG preliminary F01 (missing bpftool returning None) now fails closed; attachment
absence cannot pass via None equality. F02 (only per-operation HTTP timeout) now
has an elapsed budget and independent socket-interrupt timer. Both unchanged PG
regressions pass, alongside owner cases. Focused owner tests also cover SIGHUP,
actual Tag ResponseDTO `dataList`, fixed diagnostic-code coverage and cleanup
failure overriding success. Tests forbid real sockets/processes/target writes;
all relevant operations are mocked.

Additional checks: five freeze pins matched; two README Python snippets parsed
without execution; runtime/test ASCII checks passed; three original public
template pins remained unchanged. No full app suite, build, coverage measurement,
Linux unit verification or actual network/DB/signal enforcement was performed.

## 5. Native Proof and Recovery Boundary

[Finite native plan](../../build/WI024-private-start/README.md#native-checks-and-restoration)
requires fresh gates, both outside positive controls, same-unit negatives, live
after-controls, unchanged effective policy/BPF attachments, exact false API flags,
equal aggregate DB counts and verified cleanup. Missing bpftool is HOLD.
This can establish bounded **on-host IPv4 IP-filter enforcement**, not observed
Internet delivery denial or external IPv4+IPv6 proof.

MA's 12:19 and 12:29 KST read-only metadata and working bpftool observation are
attributed baseline observations only. Inspector address counts are not acceptance
because of its malformed `addr_info` finding; this operator independently validates
the schema. Storage retains its expected application ownership. No native result
from this new operator exists in this Evidence Pack.

Cleanup attempts app stop/mask, MySQL stop/remask and receiver closure on failures
and handled signals; verified cleanup failure is HOLD. SIGKILL, host loss and
unrecoverable kernel IO can prevent cleanup. MA must use the retained recovery
console for exact-service restoration if no receipt arrives. Preserve all logs,
claims, approvals, releases and partials; do not delete or automatically retry.
Runtime180s is not a MySQL recovery guarantee. Original candidates/current link
remain untouched; this is containment, not a production deployment or data rollback.

## 6. References and Next Gate

Injected in order: Tier 0 core principles (`STD-001`), documentation standards
(`STD-004`), development standards (`STD-002`), glossary (`STD-005`); Tier 1 SA
persona and security policy; Tier 2 approved REQ, current decision entry, original
first-start README/inventory and WI020 artifact/evidence. Applied skills: `test`
and `create-wi-evidence-pack`. Relevant actual controller/config contracts were
checked; the tags object contract supersedes the stale raw-list assumption here.
Official systemd and bpftool references are linked in the frozen README.

**Next: WI025 final exact-byte review of runtime bundle and separately pinned
README snippets, then MA preflight/native operation only if that gate passes.**
MA reported PG025 notified. SA does not subdelegate or authorize target execution.
No helper edits after this freeze unless PG returns a concrete defect; retain the
prior pins in history and issue a new freeze for any such correction.

## 7. F03 Correction and v2 Freeze Addendum

PG025 confirmed F03/P2: after a normal Connection-close body was drained,
`public_get` reset a timeout on the now-closed original socket, raising EBADF.
SA reproduced this against v1 with the unchanged PG actual-stdlib/inert-byte
suite: three tests, one pass and two errors (Content-Length and chunked success).
No native socket was used.

Before editing, all ten v1 bundle-directory files were copied exclusively into
`build/WI024-private-start/history/v1/`; each copy's SHA-256 matched its source.
This includes the exact runtime, unit, overlay, shape, tests, README, freeze and
three receipts/logs. No original was deleted. The historical bundle remains
`d9493b397847e07015e66d751e24d37815c1a528eeb569dceb934dfdb308b976`.

The correction is limited to `budget(update_timeout=False)` after `response.read`.
That final check still tests elapsed time and the expiry event without touching
a closed FD. Pre-I/O remaining-time limits, the independent five-second interrupt
timer, timer cleanup and all other operator behavior are unchanged. One owner
regression was added; PG tests were not edited.

**v2 frozen at 2026-10-11T03:50:22Z; owner SA, Windows/Python 3.14.3.**

| v2 identity | SHA-256 |
| :--- | :--- |
| Runtime four-file bundle, same algorithm/order | `ae5f468e51655bedb71f997dddd18cb7ba3a50b7afcee33fdcdc5980eb960423` |
| runtime.py | `0d3fbcc65ee0af4c31a2dfcaa0767cfabce5d69d9f27f76d537aeb36f63f4566` |
| test_runtime.py | `87eaa71c406b4bacfd25c9790c8b3451b6acc4851475949c798bf466838ecba6` |
| README.md, updated staging/approval pins | `74571c45524a93d7b888809c82e5a3dd0bbaa6995c84e894e57eded568f8881e` |
| Unchanged PG HTTP-lifetime test | `4dc460577b71ab10f24487b17edc4586953fc7463b3ec949c841fe6175995811` |

Unit/public.env/env-shape pins are unchanged from Section 2. The current
[freeze](../../build/WI024-private-start/freeze.json) links the previous freeze.
The README staging snippet uses the new runtime pin and the approval snippet uses
the new bundle pin; no staging behavior or approval scope changed.

| Reproduction command | v2 result | Duration |
| :--- | :--- | :--- |
| `python -B build/WI024-private-start/test_runtime.py` | 42/42 PASS, exit 0 | 0.130 s |
| `python -I -B build/WI025-review/test_runtime_preliminary.py` | 8/8 PASS, exit 0 | 0.017 s |
| `python -I -B build/WI025-review/test_http_lifetime.py` | 3/3 PASS, exit 0 | 0.002 s |

All have zero failures/errors/skips. Evidence: current
[receipt](../../build/WI024-private-start/test-result.json),
[owner log](../../build/WI024-private-start/owner-tests.log),
[PG8 log](../../build/WI024-private-start/pg-regression.log), and
[HTTP lifetime log](../../build/WI024-private-start/http-lifetime.log).
SA reruns do not constitute independent PG final signoff. Native remains
**NOT_RUN**; return exact v2 and README to PG025. v1 is historical, not an
executable fallback recommendation. No new architecture or target action occurred.

## Related Documents

- [WI024 handoff](WI-20261011-ATS-024-handoff.md): Scope and acceptance criteria.
- [REQ approval](../user/REQ-20261011-ATS-003.md): Approved bounded direction.
- [User summary](../user/WI-20261011-ATS-024-summary.md): Concise completion report.
- [WI025 handoff](WI-20261011-ATS-025-handoff.md): Independent security gate.
- [Operator README](../../build/WI024-private-start/README.md): Frozen staging, invocation, references and recovery.
