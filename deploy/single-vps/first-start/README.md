---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261011-ATS-002.md
    reason: Local-only preparation approval
  - path: ../CONFIGURATION.md
    reason: Existing least-privilege and resource baseline
  - path: ../../../docs/index.md
    reason: Latest dated decision and restored host state
---

# Private First-Start Candidate

> Purpose: Define a finite, externally fenced future startup without installing, enabling, or launching it.

## Status and Files

**LOCAL PREPARATION ONLY. Linux syntax, network enforcement, secret binding,
application startup, and cleanup proof: NOT RUN. Production HOLD.**

WI-20261011-ATS-017 owns only this directory and its two deliverables. No existing
template, contract, historical checker, manifest, TLS site, or application source
is changed here. MA's WI015 native read-only schema PASS is dated evidence, not
full-start approval. Its final baseline is MySQL inactive/persistently masked,
app absent, no DB/app ports/jobs, restricted TLS and Certbot timer retained.

| File | Review purpose |
|---|---|
| [atstudio-first-start.service.candidate](atstudio-first-start.service.candidate) | Separate, non-installed unit; unconditional false prestart gate; no install section |
| [first-start-public.env.example](first-start-public.env.example) | Public second EnvironmentFile; no DB/JWT/keyring secrets; numeric disabled SMTP target |
| [inventory.md](inventory.md) | Source entry points, external destinations and local effects |
| [test_candidate.py](test_candidate.py) | Offline standard-library assertions; never invokes systemd, Java, network or DB |

The `__APP_JAR__` placeholder MUST remain unresolved in this repository candidate.
WI020 plans a new backend under `build/WI020-release/` (exact filename TBC), recorded
in `deliverables/agent/WI-20261011-ATS-020-artifact.json`. A later approved render
must name the exact staged absolute new-JAR path and verify its SHA-256 against
that new manifest, including WI016 source identity and WI018/019 results. Never
fall back to `/opt/atstudio/current/app.jar` or the old `build/libs` artifact.
WI026 manifest and old JAR remain immutable. The old `release-readiness.py`
checker is WI026-pinned and expected to report source drift after WI016; do not
patch it or interpret that result as the new release check. No new artifact
hash is invented here. No build is performed by WI017.

## Design and Alternatives

Recommendation within the approved plan: use a separate finite unit, explicit
application OFF settings and per-unit IP filtering. This isolates the experiment
from the normal unit and preserves existing HTTP/TLS. It still needs target
proof and does not enforce HTTP method restrictions on other local processes.

Alternative: retain HOLD with no startup. This has the least operational risk
and is mandatory if any proof is missing. A separate network namespace could
provide a stronger boundary, but requires a reviewed MySQL reachability design
and separate approval; it is not implemented. Host-wide firewall changes are
outside scope. No new library or permanent architecture decision is introduced.

## Configuration and Guard Contract

The first EnvironmentFile path is deliberately unresolved. Later approval must
identify the existing private file and exact existing DB/account/JWT/billing
keyring use without displaying values or replacing keys. The second file is a
reviewed public overlay. It neither supplies nor regenerates secrets. Preserve
the complete existing billing keyring, not merely a newly invented active key.
Real private config is neither opened nor parsed by WI017.

Critical OFF, numeric SMTP, blank Provider credentials, strict roots/audit and
DDL settings are also fixed as **canonical command-line properties**. An env-only
overlay is insufficient when a private file contains canonical Spring env names.
The command line overrides OS environment and ordinary JVM system properties;
this assumes the reviewed ordinary SpringApplication entry, no custom property
source or disabled command-line binding. Test that assumption with WI016/019
isolated Spring tests and the exact built artifact. No secrets are CLI arguments.
[Spring Boot configuration order](https://docs.spring.io/spring-boot/reference/features/external-config.html)
documents this precedence.

`UnsetEnvironment` removes JVM option/config/proxy injection at process assembly,
including values originating in EnvironmentFile. It is not an environment
allowlist. A future private preflight must reject unknown imports, profile-group
activation, custom mail session/JNDI settings, Java agents, logging/file capture,
proxy/relay routes and unreviewed drop-ins without printing values. Effective
unit inspection must include parent slices and prefix/global drop-ins, not just
this file. Do not dump `Environment`, `/proc/*/environ`, private files or complete
service logs. [systemd execution reference](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.exec.xml)
describes final-step environment removal.

Required application protections stay enabled: `prod`, explicit disjoint roots,
strict integrity startup inspection, `ddl-auto=validate`, SQL init `never`, JWT
validation and unconditional billing-keyring validation. Do not disable guards,
auto-create schema, enable test users/acceptance, suppress validation failures,
or rotate a key to make startup pass. SMTP loopback port 1 is a deliberately
nonfunctional numeric value, NOT a mail switch; prove it has no listener/relay.
Password-login OFF does not disable payment-failure mail. Notification OFF does
not disable schedulers. Blank Provider credentials are not a network boundary.

The master `APP_BACKGROUND_TASKS_ENABLED=false` maps to
`app.background-tasks.enabled=false` in WI016. Together with audio OFF it must
suppress all entries in the inventory. This candidate requires the new reviewed
WI016 artifact: the old JAR may silently ignore the new flag. No claim that the
flag prevents explicit request-driven writes or all local side effects is made.

## Network Proof Gate Before Java

`IPAddressDeny=any` plus only `127.0.0.1/32` and `::1/128` allow entries is intended
to filter IPv4/IPv6 ingress and egress. It does **not** restrict loopback ports.
Parent-slice policies combine with unit policy, so effective inherited allow
entries matter. Unsupported cgroup/eBPF filtering can have no effect; successful
unit parsing or an active service is not proof. These documented limitations
require tests, not a warning-only waiver.
[systemd resource-control reference](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.resource-control.xml)

Keep `ExecStartPre=/usr/bin/false` until a separately approved procedure replaces
it with a reviewed fail-closed same-unit prestart proof. Do not merely remove it.
No proof executable, installer, SSH helper, or remote operator is delivered here.
The future proof must run before Java under the same identity, effective sandbox,
cgroup and IP policies; a normal shell test or a differently configured transient
unit is insufficient. No shell wrapper or `+` privilege bypass is permitted.

Future required results, all **NOT RUN**:

1. Record target/kernel/systemd version, effective cgroup/BPF capability and unit
   controls. Native `systemd-analyze verify` and resource checks must pass without
   unknown/ignored controls. Verify required Java17/FFmpeg/read paths and one
   owner of the exact release/DB/storage tuple; no old app, worker or second unit.
2. Select explicitly approved controlled external IPv4 AND IPv6 test endpoints,
   never mail/OAuth/payment APIs. Establish positive reachability controls and
   receiver-side observations outside the restricted cgroup, then prove bounded
   TCP connection and UDP delivery denial inside it. A timeout, closed port,
   failed DNS, missing IPv6 route or empty IP counters alone cannot prove denial.
   A missing positive control means HOLD, not PASS. Recheck effective rules just
   before Java; no policy change between proof and launch.
3. Prove exact loopback DB TCP reachability and the intended loopback HTTP bind
   with a harmless bounded probe before Java. No DB writes or real Provider
   request. Validate IPv6 loopback if allowed. Audit all reachable loopback
   listeners, Unix sockets and local agents for SMTP/HTTP/SOCKS/DNS/tunnel relays.
   These can forward via another cgroup and escape an IP-only boundary; AF_UNIX
   is retained for normal runtime needs and is not fenced by these IP rules.
   Unresolved relay capability is HOLD; approve narrower isolation before start.
4. Prove the 8080 listener cannot be reached externally over either family and
   that the existing Nginx configuration has no app upstream or proxy route to
   this port. Keep host 80/443/TLS, ACME and Certbot state unchanged. Do not use a
   broad firewall replacement or expose an app route for the test.

The source-level exclusions reduce intended activity; the target network proof
is the independent boundary if credentials or an external call path exist.

## Future Finite Runbook (Approval Required)

1. **Authorize and pin:** user approves exact target/boot, current state, new
   JAR/manifest, existing private secrets and internal DB account/schema, storage
   roots, allowed filesystem/log/temp/DB-engine writes, probe endpoints, one
   attempt and deadlines. Name operator and independent observer. No credentials
   appear in the approval's public version. Historical secret-use approval is
   exhausted. Freshness/identity/guard failure means stop, not an automatic retry.
2. **Record baseline:** secret-safe snapshot of MySQL inactive/persistently masked
   state, listeners/jobs, app absent, exact tuple, directory metadata, allowable
   aggregate DB observations, and existing Nginx/Certbot/TLS fingerprints/state.
   Verify no log forwarding/APM captures secrets; app/driver exception messages
   can leak private endpoints despite quiet SQL logging. Do not claim redaction.
3. **Prepare only the approved runtime candidate:** operator-owned immutable JAR
   and unit/public overlay; private file root0600 under root0700 parent; app cannot
   modify its config or binary. Review unknown env names privately; numeric SMTP
   CLI resolves inherited `__SMTP_PORT__` without using real mail credentials.
   Resolve only the approved placeholders, never switch the production `current`
   symlink, add an install link or weaken any Spring guard. Keep prestart proof
   mandatory and no automatic restart. Do not start MySQL through a dependency.
4. **Target fence proof:** finish all network/relay checks above. Only in that
   approval may the operator temporarily unmask/start the exact MySQL service;
   this is a distinct write-capable service transition and may cause engine/log
   writes. Confirm its exact loopback bind and no X Plugin/public listener.
5. **One bounded application attempt:** only after proof success, start the
   separately named `atstudio-first-start.service`. Observe private startup
   completion, intact guards, disabled background entries, no FFmpeg, no external
   delivery and intended listeners. `Type=simple` being active is NOT readiness.
   `RuntimeMaxSec=180s` limits active time, with separate prestart/start30s and
   stop30s budgets. A hung kernel task is not guaranteed dead at the deadline;
   escalate and prove cleanup rather than reporting success. No retries or
   `reset-failed` to bypass StartLimitBurst=1 in the same approval.
6. **Bounded GET checks only:** after verified startup completion, at most three
   attempts of `GET /api/utils/public-capabilities`, 5s total per request, 2s
   connect limit, 2s between attempts, no redirects/auth/cookies and max64KiB
   received body. Expect 200 and false password-login, signup/reset capability,
   all three social Providers and test-user bootstrap. Record booleans/status
   only. Once only, `GET /api/tags` may confirm the read-only DB route under the
   same bounds, after approving its response-size/row baseline. Do not log tag
   values; an empty list is acceptable. These routes are observations, not a
   universal health/readiness endpoint. No added actuator or permissive endpoint.
7. **Stop and restore even on failure:** operator manually stops this exact unit,
   waits at most30s, checks its cgroup for Java/FFmpeg/children and no app port/job.
   If needed, only an explicitly approved exact-unit kill is allowed; never
   `pkill java` or broad process cleanup. Stop the exact MySQL service, restore
   its prior persistent mask (not just runtime mask), and prove inactive/masked,
   no DB/X/app listeners or jobs. Preserve the existing TLS site and timer.
   Residual process or failed remask is a failed cleanup requiring escalation.
8. **Evidence and artifact cleanup:** preserve private logs with minimal public
   status/counts/hash receipts. Disable further starts of the temporary candidate
   and verify no install links; remove only newly owned staged files/unit under
   separately explicit deletion authorization. Do not delete storage, logs,
   secrets, old releases or evidence. Verify unchanged TLS and independent
   postcheck, close only owned sessions and restore any temporary operator access
   under its own approved procedure. No reboot or backup operations are implied.

Never probe notices detail (increments viewCount), download, attachments,
streaming, email verification, login/refresh, account availability enumeration,
ADMIN/payment/settlement paths, or any mutating method. Local APIs retain normal
write permissions: this is a bounded experiment, **not a read-only guarantee**.

[systemd service reference](https://raw.githubusercontent.com/systemd/systemd/v255/man/systemd.service.xml)
documents prestart failure, runtime limits and restart policy. Windows assertions
cannot prove these Linux semantics on the target. Resource values reuse current
unit settings, not old memory values: heap256-1024MiB, metaspace256MiB,
memory high2GiB/max3GiB, TasksMax256. No capacity, latency or production sizing
claim is made.

## Local Validation and Rollback

Run `python -B deploy/single-vps/first-start/test_candidate.py` from the repository.
It reads only these public candidates and explicitly named public sources. It
does not bind Spring, compile a JAR, launch Java, open sockets, use private env,
write test fixtures or install dependencies. Its precedence scenario is a static
model of reviewed CLI overrides, not a framework/runtime proof.

Proposed local reversal: stop using this candidate and retain it as superseded
evidence, or request approval to remove only WI017-created files. No reversal has
been executed; no earlier files need restoration. Runtime restoration above is
a future separately approved operation, not a script delivered by this WI.

## Related Documents

- [Inventory](inventory.md): Entry-point and outbound review.
- [WI017 evidence](../../../deliverables/agent/WI-20261011-ATS-017-evidence-pack.md): Local proof and limits.
- [WI017 summary](../../../deliverables/user/WI-20261011-ATS-017-summary.md): Approval-facing result.
- [Current decision entry](../../../docs/index.md#current-decision-entry): Latest evidence supersedes older readiness notes.
- [Configuration](../CONFIGURATION.md): Unchanged production candidates and strict guard contract.
