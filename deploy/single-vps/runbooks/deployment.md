---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: docops
category: guide
status: draft
dependencies:
  - path: ../CONFIGURATION.md
    reason: Canonical settings and delivered template paths
  - path: ../../../docs/design/db-schema.md
    reason: Fresh-only schema and unrecorded current MySQL manifest
  - path: ../../../docs/design/runtime-storage-operations.md
    reason: Explicit storage tuple and single audio worker
---

# First-Server Deployment

> Purpose: Define executable operator steps with exact approval and evidence gates.

## 1. Authorization and Preflight

**All target steps below are NOT RUN.** WI002 only authors this procedure.
MA reports no usable WSL Linux distribution and no docker/bash/nginx commands
in this Windows session. Do not install substitutes during purchase preparation.
Linux parsing, permissions, service behavior and actual capacity remain unproved.

Complete the [target inventory](../README.md#3-operator-decision-inventory).
Record approver, time, environment label, scope, maintenance window, expected
effects and rollback for each gate; approval for one does not imply the others.

| Gate | Required explicit approval |
|---|---|
| Purchase | Product, term, total/cost exposure and backup costs; never inferred from REQ Stage A |
| Remote setup | Exact host/fingerprint and OS, accounts/packages, files/services, ports and SSH source scope |
| SSH/firewall | Recovery-console access and verified alternative login; exact changes and rollback before applying restrictions |
| DB | Exact host/port/new name, verified-empty state, schema/seed hashes, provisioning privileges and operations; no retained TEST migration |
| Secrets | Named operator and external secret locations/key IDs, environment purpose, exact permitted handling; do not print/read private bundles into agent evidence |
| Runtime | Exact artifact/tuple, single owner and permitted start/stop/reboot; prior local runtime remains untouched |
| External calls | Exact mail recipient/test message or Provider TEST operation, scope and egress window; never LIVE under this WI |
| Release | Restricted target admission first; general public access and real financial operations need separate decisions |

## 2. Host and Filesystem Preparation

After those approvals, the operator follows this sequence on the named Linux
target using its approved distribution/package sources. Package commands and
versions are resolved there, not guessed from Windows or installed by WI002.

1. Verify SSH host fingerprint through an independent provider-console channel.
   Confirm console recovery; retain the original SSH session while testing a
   second least-privilege sudo/key login. Apply and test the approved source
   restriction before retiring password/root access. Never close the last
   working path or change broad firewall rules without tested rollback.
2. Patch/provision Nginx, Java 17, MySQL 8, FFmpeg with `libmp3lame`, the approved
   TLS renewal client and only approved observation/backup tools. Record
   installed versions and trusted provenance. This is future target work.
3. Use the [configuration layout](../CONFIGURATION.md). Separate immutable
   releases, public webroot, external secrets, logs and the two mutable storage
   roots. Resolve real paths and reject symlink/overlap escapes; roots must be
   distinct, non-nested, absolute and paired with the DB. Do not mount the
   repository or either entire storage root into Nginx's static webroot.
4. Run the JVM as its unprivileged service identity. It may write only the
   approved public/private storage and necessary temporary paths. Nginx may
   read frontend assets; it must not read private documents, secret files,
   source, DB files or release configuration. Restrict secret source access
   according to the delivered service mechanism, without logging contents.
5. Bind backend and MySQL to explicit loopback only. Restrict external ports to
   the approved SSH source and restricted HTTPS/ACME needs. Test from an
   independent external client that backend/DB ports are unreachable, on both
   IPv4 and IPv6 when enabled. A local listener listing alone is insufficient.
6. Synchronize the clock and record OS/JVM/MySQL time conventions. Set the
   payment scheduler's deliberate `Asia/Seoul` zone and inspect actual next run
   times. Time zone is not a scheduler-disable mechanism.

## 3. Build, Fresh Database and First ADMIN

Build only the approved revision in a clean build environment, after any
dependency-download approval. Reuse the [source reconstruction build guidance](../../../scripts/reconstruction/README.md#3-source-only-validation).
Record source identity including intended changes, tool versions, JAR hash and
frontend output manifest. Never call the old HEAD a newly tested artifact.

Publish only reviewed React build output (`frontend/dist`) to the webroot.
All frontend assets and `VITE_` values are public. Exclude `.git`, source maps
unless expressly reviewed for publication, source, `.env`, local YAML,
deliverables, documents, SQL/dumps, backups, keys and private receipts. The
existing Axios client uses `/api`; use same-origin Nginx proxying and preserve
that prefix, not a new API host or product-code patch.

**Fresh DB gate:** [db-schema.md](../../../docs/design/db-schema.md#current-source-and-mysql-verification-boundary)
records the current fresh MySQL manifest as **UNRECORDED**.
[Disposable tooling](../../../scripts/database/README.md) fails closed for
Create/Validate/HibernateValidate and must not be used on remote/production
databases. Do not bypass its guard or rerun its old lifecycle in Stage A.
MA must schedule separately approved fresh observation/proof on exact disposable
targets, with immediate cleanup approval, before accepting a canonical new
manifest. Retained TEST column ordinals are not that manifest.

For the later named production bootstrap, approve a target-specific operator
procedure, not reuse the disposable helper. Verify the exact database is empty
with application writers absent. Apply the reviewed
[schema.sql](../../../src/main/resources/schema.sql) once, then
[seed.sql](../../../src/main/resources/seed.sql) once, under temporary scoped
provisioning authority. Capture safe schema/plan counts and the new manifest;
revoke provisioning authority from the runtime account. The seed contains only
six subscription plans, **no users or ADMIN**. Schema/seed are not idempotent
migrations. Set runtime `ddl-auto=validate` and SQL initialization `never`;
never use `update`/`create-drop`, replay seed on retained data, or import TEST
users, payment records or media.

**First ADMIN remains a named-target provisioning gate.** This bundle supplies
no production-admin creation script. Before first login, MA/operator must
approve the exact new principal, account creation/verification method and
reviewed one-time role provisioning transaction/tool, backup/rollback and audit
receipt. Do not invent credentials, reuse QA defaults, enable test bootstrap,
or improvise SQL. Keep `APP_BOOTSTRAP_TEST_USERS_ENABLED=false`,
`APP_ACCEPTANCE_ENABLED=false` and the acceptance profile absent. If the approved
method is not ready, stop at this gate and assign a separate WI. After approved
provisioning verify only intended ADMIN identities and remove the provisioning
access through its approved procedure. Explicit catalog upload is separate
from seed; do not silently publish demo Tracks.

## 4. Settings, TLS and Restricted Proxy

1. Reconcile the [environment contract](../env/contract.json) with the external
   operator configuration and [CONFIGURATION.md](../CONFIGURATION.md), without
   printing values. Reject missing placeholders, unintended overrides and
   inconsistent DB/storage/release identities. A template is not an effective
   configuration receipt. Do not import local or acceptance bundles wholesale.
   A fresh deployment needs its own JWT secret and a valid local billing
   encryption keyring even with Toss/LIVE payments disabled:
   [AcceptanceStartupGuard](../../../src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java)
   calls `BillingKeyCrypto.validateConfiguration` before its non-acceptance
   return. Supply an active key ID and matching key material via the approved
   external mechanism; preserve every historical key ID when restoring data.
   This is local encryption setup, not an additional paid service. WI002 neither
   generates nor inspects actual secrets.
2. Set the exact approved HTTPS origin in CORS, mail `APP_BASE_URL`, enabled
   OAuth redirects and billing success/fail callbacks. Avoid wildcard origins,
   localhost aliases or old tunnel URLs. Disabled Providers remain disabled;
   they are not required new launch features. The existing Gmail evidence stays
   valid in TEST, while a new-host send to selected recipients needs approval.
3. Keep ingress restricted during certificate issuance. Approve ACME method,
   domain/DNS change and any narrow port/path exposure independently. Avoid
   temporarily publishing the application just to solve the challenge. Verify
   hostname, chain, HTTPS redirect and secure private-key access. Exercise the
   selected client's renewal rehearsal, timer and Nginx reload hook on-target,
   and record certificate-expiry alert ownership. No cert/renewal proof exists
   until this is done.
4. Validate the rendered Nginx configuration before any approved reload. Unknown
   hosts must not serve the app. Verify allowlisted clients succeed and
   non-allowlisted clients fail. The loopback proxy must overwrite the internal
   trusted-client header with actual peer identity, not accept client-supplied
   values or blindly chain forwarded headers. Keep the backend's configured
   trusted proxy restricted to that peer. Compare two independent external
   client identities and spoofed-header attempts without retaining raw IPs.
5. Use the template's request/body/concurrent-transfer settings as starting
   controls. Test complete multipart size including thumbnail and overhead,
   controller Range responses and positive thumbnail/attachment behavior.
   Never solve a blocked audio URL by adding an Nginx alias to audio/private
   storage. Probe encoded/mixed-case/traversal and staging paths as well as the
   normal protected prefixes; require no protected bytes for any role.
6. Ensure proxy/application/tracing logs omit Authorization, cookies,
   Idempotency-Key, query tokens, reset URLs, request/response bodies and private
   file names. Check synthetic markers before using real identities. Bound
   disk usage and retention; avoid an unbounded access/error log. Review error
   logging separately because it may include request details even when the
   access log is disabled. See [observation](acceptance-capacity.md#2-observation-and-alerts).

## 5. First Start and Native Checks

Before startup, prove no old application/worker/encoder can reach this tuple.
Require production profile, explicit roots, startup integrity audit and strict
mode all enabled. Missing media must fail startup, never produce a non-strict
waiver. Check service/secret permissions and the rendered systemd unit on-target.
Keep automated restart disabled during manual provisioning/quarantine if needed
by the approved procedure; never accidentally launch a second owner.

Run the following inspection commands only in the separately approved Linux
session, with the configured executable path under the effective service
identity. These examples were **not executed** here:

```sh
java -version
"$FFMPEG" -version
"$FFMPEG" -hide_banner -encoders
"$FFMPEG" -hide_banner -h encoder=libmp3lame
nginx -t
ss -lnt
```

Require the approved version, readable/executable path and libmp3lame. Then run
an approved synthetic encode outside both runtime roots and validate full
duration/128kbps output with original hash unchanged. Mere executable presence
or Java startup is insufficient. No native probing is added to product code.

Start exactly one service only after startup approval. Check schema validation,
strict integrity, actual loopback binding, safe capability response, frontend
deep links and `/api` routing before admitting the named testers. Confirm the
audio worker and FFmpeg child count/process ownership. Subscription schedules
remain active: `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=false` suppresses
notifications only. One JVM must own all schedules and conversion work; no
overlapping old/new services, cron launchers, `bootRun` or blue/green writer.
Provider/SMTP egress stays denied except exact approved tests. On a fresh target,
use no imported LIVE credentials or historical due-payment data.

## 6. Rollout, Reboot and Rollback

Before each later release, approve downtime and take the
[consistent backup](backup-restore.md#3-cold-consistent-capture). Close new
requests, drain known in-flight activity, stop the owned service and verify the
JVM, private audio executor and every FFmpeg child are gone. Preserve unfinished
payment/audio/storage-journal state for review; do not clear it to obtain a
green check. Switch a matched immutable backend/frontend release only after
compatibility review. Preserve the same DB/public/private roots and keyring.

An approved reboot test must verify boot ordering, storage mounts, clock,
loopback bindings, restricted firewall/TLS, one service/encoder owner, strict
integrity and controlled recovery of an interrupted synthetic audio job. Do
not infer automatic orphan cleanup from an operator's manual termination.

Rollback first closes admission and stops/fences the current writer. A previous
binary is usable only if its schema, media, keyring and in-flight payment command
contracts are compatible. In particular, a pre-WAV binary can expose an
original without the derivative guard; never blindly switch to it. No automatic
column drops, file deletion or DB rewind is authorized. Incompatible rollback
requires a separately approved paired restore and reconciliation of events
after the snapshot, especially Provider actions. Until resolved, remain in
restricted maintenance with the current evidence preserved.

## Related Documents

- [Backup and restore](backup-restore.md): Recovery and keyring gates.
- [Acceptance and capacity](acceptance-capacity.md): Target results and stop conditions.
- [Payment operations](../../../docs/design/payment-operations-runbook.md): In-flight monetary state and reconciliation.
- [Security policy](../../../docs/policies/security-policy.md).
