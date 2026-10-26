---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-035-handoff.md
    reason: Local implementation and MA-only operational boundary
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved management-only temporary web session
  - path: WI-20261011-ATS-036-evidence-pack.md
    reason: Independently verified artifact identity and read contract
  - path: WI-20261011-ATS-034-evidence-pack.md
    reason: Historical private startup and cleanup result
---

# Evidence Pack: WI-20261011-ATS-035

> Purpose: Freeze the restricted-web candidate and exact MA procedure for independent WI037 review.

## Summary

**TL;DR: LOCAL COMPLETE / WI037 FINAL REVIEW REQUIRED / NATIVE NOT_RUN.**
A separate WI035 candidate, eleven executable recipe blocks, target-free receipts
and both deliverable sets are complete. Python85/85 and PowerShell8/8 PASS.
No source, JAR, old candidate, provider, remote host, secret, service, DB or browser
operation was performed by SE. No commit, push, installation or subdelegation.

Frozen at 2026-10-11T06:07:31.2917197Z, Windows/PowerShell local workspace.
This is the observation/freeze time; document date is not a native verification time.

## Scope / DoD Check

- [x] Fresh WI035 upload/tools/claim/approval/run/log/release/unit/control paths; REQ006 and WI037 identity gates.
- [x] Original JAR and public env/shape bytes retained; exact existing-secret/keyring and DB guards preserved.
- [x] WI036 final six GET paths / seventeen query sets integrated, including available-tags without query.
- [x] Manifest-only143 SPA files; bounded exact raw-target permutations, no unknown-file/SPA API fallback.
- [x] Management peer derived from SSH and matched to original nginx allow; provider changes prohibited.
- [x] Method, Host/SNI, forwarded-header, auth/cookie, path/query and external-resource fences locally checked.
- [x] Separate systemd supervisor, independent app/DB bounds, explicit finish/status, preserved baseline restoration and safe36-step diagnostics.
- [x] Exact staging/preflight/approval/start/status/finish/postcheck and management-PC HTTP recipe frozen.
- [x] Local tests, in-memory syntax checks, preservation hashes, receipts and two-set documents recorded.
- [ ] WI037 final exact-byte verdict and current MA execution gates: separate work, not SE acceptance.
- [ ] Native nginx/systemd/BPF/timing/HTTP/browser/restoration evidence: NOT_RUN.

## Evidence Pointers

All implementation paths below are under `build/WI035-restricted-web-check/`.

| Pointer | Purpose |
|---|---|
| runtime.py:56,277,284 | Eleven approval attestations, ten-file bundle, exact REQ/review/boot/expiry |
| runtime.py:669 | Inherited bounded HTTP implementation and closed-socket lifetime correction |
| runtime.py:798,824,852 | Independent cleanup/postconditions, original23 labels, sticky failure |
| runtime.py:871,1059 | Single supervised session, mandatory prestart/BPF checks, no old run CLI |
| web_policy.py:76,94 | Pure policy oracle and exact-location nginx renderer |
| web_session.py:85,117,167 | Original configuration snapshot, exclusive manifest copy, actual MySQL bound gate |
| web_session.py:190,244,339 | Bounded view, ExecStopPost restoration, start/status/finish entry |
| management-check.ps1:10,53,96 | 5s/64KiB no-credential HTTP, payload assertions, pinned fence prerequisite |
| README.md | Exact reviewed transport contract and all eleven command blocks |
| freeze.json; recipe-pins.json | Full hashes, ordered algorithms and recipe block identities |
| local-receipt.json; local-tests.log | Target-free test counts, limits and captured test output |
| preservation.json | Seventeen original/contract objects checked against existing pins |

[Candidate README](../../build/WI035-restricted-web-check/README.md) contains the directly
executable recipe. MA binds only its existing approved SSH argument array, including
current host-key pin/options/destination; no guessed host or address is embedded.
The root/native shell blocks run unchanged through that transport. No pseudo-command
or newly issued credential is required. The SSH server-side address is validated only
as IPv4, not compared to a public address; NAT does not require a code change.

## Frozen Identity

| Object | SHA256 |
|---|---|
| Ten-file runtime bundle | `35ff347e1d0e2b0cad930ce1431b073d08fea7d2a48631779360766424760ce5` |
| runtime.py | `8ca9012729f3f89a74dac8e325321aab3f5418e727efd827a394e53151eebffb` |
| web_session.py | `b7b70dece75a1ceebe9126ee659a68b75129c7cf07a6351294f86d79e0b291b7` |
| README.md | `91801af1f56c4f67d5be1c11e28ec8b04f1bb91a0db5d4d1d20015fdc6ccd74a` |
| Review set,23 files | `feb373fe3308a8dd06da2bb7e149bbdc255f527750744a987830fccf8b1de063` |
| freeze.json | `66843a26f4681a11cd48e9bde461170a29d4bd807a186822495b112d6a160d1f` |
| WI036 allowlist | `09df13ad175f4a359e6cbc9d9b68d0f6bf8e3c27eb065ca2e8e6de80e757bf12` |
| Existing JAR,70306351 bytes | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |

Runtime bundle hashes UTF-8 filename + NUL + exact bytes in freeze.bundle_order.
The review-set hash uses the same algorithm over freeze.files insertion order.
Freeze itself and the two deliverables are excluded to avoid self-reference.
Recipe hashes use each fenced executable body, UTF-8/LF with exactly one final LF.
L1-L4 and S1-S7 are all pinned; the generated upload carries only separately pinned
public bytes. Sizes and individual helper/test/recipe/receipt hashes are in the freeze.
Review-set freeze is not an OS immutable flag; any later byte change invalidates review.

Preservation verified all eleven WI032 freeze entries plus its README, WI026 runtime,
WI029 runtime, existing JAR, WI028 native receipt and final WI036 allowlist:17 MATCH.
Those measurements do not claim a repository-wide unchanged inventory. No writes were
made to any older candidates or application sources; existing unrelated changes remain.

## Bounded Delta and Decision

Reuse WI032 containment and WI029 diagnostics in a separate candidate. The required
web interval adds a root-owned systemd supervisor, exact nginx renderer, manifest
copy and restoration helper, rather than holding service lifetime in an SSH shell.
It does not add a framework, new application build, dependency or product feature.

The old180s app cap is deliberately replaced by1500s only in WI035, under REQ006's
maximum1800s scope. Supervisor runtime1440s, stop180s; app start/stop30s and MySQL
start60s/stop30s. BindsTo/After makes supervisor loss stop dependents; independent
runtime limits remain even if the main operator disappears. The view deadline is
1380s from operator start, and late admission after240s fails. No restart policy or
enable/WantedBy is added. These are configured bounds under a functioning kernel
and systemd, not a measured native maximum or host-loss guarantee.

Nginx stop/start with process absence was selected over reload-only because reload
can leave old workers serving existing clients. Original config, service drop-in,
renewal hook and certificates are not rewritten. Only new /run overlays are added;
restoration renames them into retained private evidence, checks original effective
service configuration and bytes, then starts original quarantine once. Failure to
prove restoration prevents that start and keeps HOLD. Nginx MainPID changes by
design; restored command/cgroup/listener and service checks replace PID equality.
Only the supervisor's own running stop job can be ignored during ExecStopPost;
unrelated jobs still fail.

Original23 diagnostic labels are retained with13 additional web restoration labels.
Secret/command output remains internal. Public receipts contain fixed enums, bounded
counts/rc/timeouts only; unknown exceptions map to a fixed category. A cleanup failure
cannot be overwritten by later successful checks. A browser failure is separate:
MA retains overall HOLD even if server cleanup finishes successfully.

## Tests and Reproduction

See [local receipt](../../build/WI035-restricted-web-check/local-receipt.json) and
[captured output](../../build/WI035-restricted-web-check/local-tests.log).

- `python -I -B -m unittest discover -s build/WI035-restricted-web-check -p 'test_*.py' -v`:85/85 PASS,0 skipped,0.347s,exit0 in the captured final run.
- `./build/WI035-restricted-web-check/management-check.ps1 -Mode SelfTest`:8/8 synthetic payload assertions,exit0,network NOT_RUN.
- In-memory Python compile:10 candidate Python files,4 native embedded Python blocks and1 upload receiver,15 units PASS.
- PowerShell parser:all4 README PowerShell blocks PASS;11 executable fences total.
- All seventeen preservation entries match their prior public pins.
- No Gradle/Vite build, full application suite, real sockets/services/DB/HTTP, coverage threshold claim, Linux shell/unit/nginx syntax execution or browser execution.

Python suite split:42 inherited runtime,8 preliminary,3 HTTP lifetime,4 parent metadata,
10 cleanup diagnostics,8 pure web policy and10 session/restoration tests.
Native boundaries are synthetic/mocked. Tests include multiple failures/continuation,
every web restoration step, pending/interrupted result HOLD, expiry, command finalizer
failure, wrong env/path/hash, missing BPF proof, misleading network positives and HTTP
body lifetime. PowerShell tests cover empty data, missing pagination, OFF capability
and provider-enabled rejection, correct/incorrect plan values and available-tags.
They do not measure actual HttpClient cancellation behavior. Existing independent
tests not included in these suites are not relabelled as newly executed.

## Runtime Acceptance and Remaining Limits

MA-supplied14:36 and15:06 observations described current pinned host/boot/recovery,
unchanged TLS/JAR, stopped/masked older units and a matching SSH management peer.
SE did not independently access the host. Those observations are context only;
recipe preflight and operator runtime gates remain fresh requirements.

WI036 independently reports641 backend inputs,336 frontend inputs and143 SPA files
matched, with four exclusions separately checked. WI035 uses the final pinned
contract; it does not replace QA's identity evidence or claim a source rebuild.
Exact list envelopes and PageInfo are used, not the stale raw-array Tag standard.

The PC checker observes real HTTP status/payload only when MA later runs Allowed/
Denied. Browser DOM/screenshots/dev.logs are separately reported. Existing browser
context is not pristine, cannot be called a new profile, and is not inspected/cleared.
No supported response interception/network dump is assumed. Complete absence of
extra requests and browser resource-hint traffic remains NOT_VERIFIED with those
surfaces alone. Default-none/self-only CSP and DNS-prefetch-off are configured;
intentional jsDelivr CSS rejection/fallback font is not an application failure.
No independent non-management vantage is fabricated or added.

Native30-minute timing, BPF attachment/controlled on-host enforcement, effective unit/
nginx parsing, actual anonymous UI, no retained worker, original HTTPS restoration and
SSH cleanup remain MA/WI037-owned. The proof is on-host IPv4, not observed Internet
delivery denial. No populated search/popular ordering, media/login/payment success,
public release, real-device or load acceptance. WI036's existing popular-sort mismatch
remains outside scope. WI026's old cleanup failure cause remains NOT_DETERMINED.

## Risks / Rollback and Follow-up

Cleanup commands are best-effort under the supervisor180s stop bound. Slow commands,
SIGKILL/host loss or unrecoverable OS failure can prevent complete restoration or a
receipt. Missing final result means HOLD, not assumed success. MA first uses the
retained recovery channel for read-only inspection; unapproved recovery or another
start requires a new decision. No private raw logs or env may be forwarded.

Rollback before execution is **withhold this candidate**. During the approved attempt,
use the exact one-use finish marker and observe the reviewed restoration; retain all
original/new evidence and partials. Do not delete, overwrite approvals/claims, alter
host parent permissions, or retry a failed start.

**WI035 local implementation complete; Blocks WI037.** MA has already started PG037
preliminary review. Hand this exact freeze/recipe plus final WI036 contract to PG037
for final review immediately. Preliminary architecture review is not final ALLOW.
Only MA may perform approved native work after that verdict and current gates.
REQ006 remains active; it is not closed by this local deliverable.

## Reference Documents (Tier 0-2)

| Tier | Context | Application |
|---|---|---|
| 0 | STD-001 core-principles; STD-004 documentation-standards; STD-002 development-standards; STD-005 glossary | Fully injected ordered context, approval, terminology, two sets |
| 1 | Security policy; assigned SE handoff | Secret containment, fixed safe reporting, no remote delegation |
| 2 | REQ006; WI035 handoff; WI032 runtime/unit; WI034 evidence | Exact scope, reusable implementation and historical limits |
| 2 | TLS quarantine templates/hook/service; final WI036 evidence/contract/browser plan | Exact baseline and narrow read surface |
| Skills | test; create-wi-evidence-pack | Target-free suite reporting and standardized evidence |

Official references used for decisions: [nginx process control](https://nginx.org/en/docs/control.html),
[systemd v255 service](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.service.xml),
[systemd v255 unit](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.unit.xml),
[CSP3](https://www.w3.org/TR/CSP3/). Documentation semantics do not prove target behavior.

## Related Documents

- [User summary](../user/WI-20261011-ATS-035-summary.md): concise local result.
- [WI035 handoff](WI-20261011-ATS-035-handoff.md): scope and successor.
- [REQ006](../user/REQ-20261011-ATS-006.md): user authority.
- [WI036 evidence](WI-20261011-ATS-036-evidence-pack.md): identity/read contract and limitations.
- [README](../../build/WI035-restricted-web-check/README.md): exact unexecuted recipe.
- [Freeze](../../build/WI035-restricted-web-check/freeze.json): current exact bytes.
- [Recipe pins](../../build/WI035-restricted-web-check/recipe-pins.json): all eleven executable blocks.
