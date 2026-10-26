---
version: 1.3
last_updated: 2026-10-10
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../../deliverables/user/REQ-20261003-ATS-001.md
    reason: Approved pre-purchase scope and target approval gates
  - path: ../../docs/design/runtime-storage-operations.md
    reason: Single-writer storage tuple and strict startup integrity
  - path: ../../docs/policies/security-policy.md
    reason: Secret, proxy, private-media and logging boundaries
---

# Single-VPS Configuration Candidates

> Purpose: Supply reviewable configuration for a later approved Linux target.

## 2026-10-10 Current Candidate

[REQ006](../../deliverables/user/REQ-20261010-ATS-006.md) implements the
[selected baseline](runbooks/resource-baseline.md) in source and templates.
WI011/012 isolated tests and [final PG review](../../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md)
cover local preparation only. **No settings were applied to a running service.**
The old JAR is unchanged: build/hash the reviewed new source and prove active
pre-body/pre-transaction admission before applying the new env or Hikari10.
Increasing the old artifact's pool alone is prohibited and does not fix starvation.

| Current source/candidate | Implemented value | Effective target |
|---|---|---|
| App unit heap / metaspace / cgroup | 256-1024MiB / 256MiB / high2GiB, max3GiB | NOT RUN |
| MySQL buffer pool / connections | 512MiB / max30 | NOT RUN |
| Hikari max / idle / acquire timeout | 10 / 2 / 5000ms | NOT RUN |
| Storage-mutation admission | Enabled; global2 / trusted IP1; 429 + Retry-After2s | NOT RUN |
| Multipart | 100MiB file / 120MiB request / threshold0B; private temp | NOT RUN |
| SMTP connect / read / write | 5000 / 10000 / 10000ms, explicit JavaMail properties | NOT RUN |
| Nginx media shaping | Stream256KiB/s; download2MiB/s; shared media4/IP,16/global; downloads1/IP,2/global | NOT RUN |
| Nginx multipart admission | Case-insensitive multipart Content-Type; 1/IP,2/global; explicit general/media child zones | NOT RUN |

The [backup guide](backup/README.md) records the implemented core, default-OFF
policy and whole-helper1GiB candidate. Every host hook remains unconfigured;
daily-service integration, independent alert transport, copy and restore are
not implemented. Historical design-only/unchanged-template evidence remains in
WI008-010; the sections below describe the current candidate and retained
security contract. Production HOLD.

## 1. Status And File Interface

The original WI-20261003-ATS-001 bundle is extended by WI011/012/013 under
REQ006, not an installer or production approval. DOCOPS source inspection:
2026-10-10, Windows/PowerShell, `main` HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6` with dirty/untracked candidate files.
Use reviewed implementation hashes, not HEAD alone. Unrelated dirty files are preserved.
Linux syntax/runtime checks, actual capacity, firewall and secret provisioning
are **NOT RUN**. Production remains **HOLD**.

| Repository file under this directory | Intended Linux destination / use |
|---|---|
| [nginx/nginx.conf.template](nginx/nginx.conf.template) | Rendered candidate for `/etc/nginx/nginx.conf`; complete dedicated-host configuration |
| [nginx/http-bootstrap.conf.template](nginx/http-bootstrap.conf.template) | Initial `/etc/atstudio/nginx/active-site.conf`, HTTP-01 only |
| [nginx/https.conf.template](nginx/https.conf.template) | Replacement `active-site.conf` after certificate issuance |
| [nginx/proxy.conf.template](nginx/proxy.conf.template) | `/etc/atstudio/nginx/proxy.conf` |
| [nginx/restricted-access.conf](nginx/restricted-access.conf) | `/etc/atstudio/nginx/restricted-access.conf`, initially denies every client |
| [systemd/atstudio.service](systemd/atstudio.service) | `/etc/systemd/system/atstudio.service` |
| [env/atstudio.env.example](env/atstudio.env.example) | External `/etc/atstudio/atstudio.env`; never commit the rendered file |
| [env/contract.json](env/contract.json) | Repository-only interface for WI003 offline validation; never an approval token |
| [mysql/atstudio.cnf](mysql/atstudio.cnf) | Candidate under the target's verified MySQL 8 included configuration directory |
| [journald/atstudio.conf](journald/atstudio.conf) | `/etc/systemd/journald.conf.d/atstudio.conf`; host-wide policy, not per-unit |

The contract's `required_template_paths` are repository-relative. `required_env`
lists explicit keys; `safe_literals` and `must_be_empty` describe the checked-in
pre-activation example. `required_placeholders` maps tokens to keys and duties.
WI003 must independently assert safety, not accept a weakened contract as proof.
Template-mode PASS means unresolved targets/secrets are intentionally present.
Deployment-mode checks must reject them, without printing values. No runtime
validator is installed by this WI; do not start merely because files exist.

## 2. Paths, Ownership And Configuration Sources

| Path | Owner/access contract |
|---|---|
| `/opt/atstudio/releases/<release-id>` and `/opt/atstudio/current` | Operator/root-owned immutable JAR and built SPA; app and nginx read only. `current` may be an operator-owned release symlink. No private files, source maps containing private material, or `.env` in the SPA. |
| `/srv/atstudio/public`, `/srv/atstudio/private` | Distinct non-nested real directories, owned only by `atstudio`, mode `0700`; nginx must not read either. Public naming is a storage classification, not permission to serve audio statically. |
| `/var/lib/atstudio/tmp` | `atstudio:atstudio`, `0700`, for multipart/Java/FFmpeg temporary work |
| `/var/lib/atstudio/nginx-body` | Only the selected nginx worker identity, `0700`; no symlink to app storage |
| `/var/lib/atstudio/acme/.well-known/acme-challenge` | ACME/operator writable; nginx read only; token files only, no secrets or symlinks |
| `/etc/atstudio/atstudio.env` | root-owned `0600`; systemd reads the required EnvironmentFile before changing service identity |
| `/etc/atstudio/tls/privkey.pem` | root-only private key, `0600`; fullchain is separately readable by nginx master |
| `/etc/atstudio/nginx` | root-owned; worker may read configuration if required, never write it |

Pre-create writable directories under separately approved target operations.
The service never runs as root; read-only releases and a `0077` umask prevent
application writes from becoming nginx-readable. Environment variables remain
visible to sufficiently privileged/same-user process inspection: root-only
files are not encrypted runtime secret storage. No shell `source`, `export`,
`$VAR` expansion, environment dumps or secret-bearing command-line arguments.

There is **no `application-prod.yml`** in the inspected source.
`SPRING_PROFILES_ACTIVE=prod` activates source guards; base configuration plus
explicit environment values supply settings. `SPRING_CONFIG_LOCATION=classpath:/`
selects packaged configuration only, and the unit unsets additional/import/JSON
config and JVM-option injection variables. Never add a local YAML import or
unreviewed unit drop-in. Check effective settings by safe names/status, not
`systemctl show ... Environment` output.

The prebuilt SPA uses `frontend/src/api/client.ts` `baseURL: '/api'`; it needs
no host-specific API URL rebuild variable. Vite is not an operating service.

## 3. Source-To-Environment Contract

| Values | Authoritative mapping / effect |
|---|---|
| `SERVER_ADDRESS`, `SERVER_PORT`, `SERVER_FORWARD_HEADERS_STRATEGY` | Spring Boot server binding: `127.0.0.1:8080`, forwarded interpretation off |
| `SPRING_DATASOURCE_*`, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, `SPRING_SQL_INIT_MODE=never` | `src/main/resources/application.yml` plus Boot configuration; no runtime DDL/seed initialization |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE`, `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE`, `SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT` | Explicit YAML bindings: max10, min2, acquire5000ms; only with reviewed new artifact and active admission |
| `APP_STORAGE_MUTATION_ADMISSION_ENABLED`, `APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT`, `APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT_PER_IP`, `APP_STORAGE_MUTATION_ADMISSION_RETRY_AFTER_SECONDS` | `app.storage.mutation-admission.*`: true / 2 / 1 / 2; servlet filter before body parsing and transactions |
| `SPRING_SERVLET_MULTIPART_FILE_SIZE_THRESHOLD` | `spring.servlet.multipart.file-size-threshold=0B`; spool behavior still needs target checks |
| `MAIL_SMTP_CONNECTION_TIMEOUT`, `MAIL_SMTP_TIMEOUT`, `MAIL_SMTP_WRITE_TIMEOUT` | `spring.mail.properties.mail.smtp.connectiontimeout`, `.timeout`, `.writetimeout`: 5000 / 10000 / 10000ms; not total DNS/retry deadline |
| `JWT_SECRET` | `config/JwtConfig.java`: Base64 decode must yield at least 32 bytes; an arbitrary 32-character string is insufficient |
| `APP_STORAGE_*` | `service/storage/LocalStorageService.java`, `config/StorageIntegrityStartupGuard.java`: explicit absolute disjoint roots and strict audit; missing references refuse startup |
| `APP_ACCEPTANCE_ENABLED=false`, `APP_BOOTSTRAP_TEST_USERS_ENABLED=false` | `config/AcceptanceStartupGuard.java`: acceptance/test bootstrap forbidden with production profiles |
| `APP_SECURITY_TRUSTED_CLIENT_IDENTITY_ENABLED=true`, `APP_SECURITY_TRUSTED_PROXY_ADDRESSES=127.0.0.1` | `security/TrustedClientIdentityResolver.java`: one validated literal from a configured loopback peer only |
| `APP_AUDIO_*` | `application.yml`, `service/audio/TrackAudioProcessingWorker.java`: one private worker per JVM, original retained, new WAV derivative is full-length MP3 CBR 128kbps |
| `PAYMENT_BILLING_KEY_ACTIVE_KEY_ID`, `PAYMENT_BILLING_KEY_0_ID`, `PAYMENT_BILLING_KEY_0_SECRET` | `AcceptanceStartupGuard.validate()` unconditionally calls `BillingKeyCrypto.validateConfiguration()`, before early return. Matching active/key IDs and an independent strong secret are **required even with blank Toss keys**. This is local encryption, not a paid service or permission to charge. |
| `APP_BASE_URL`, `CORS_ALLOWED_ORIGINS`, callback URLs | Exact `https://__APP_FQDN__` origin; no localhost or wildcard CORS in target configuration |
| `APP_AUTH_PASSWORD_LOGIN_ENABLED=false`, blank OAuth/Toss credentials | No enabled authentication flow is promised. Select and verify a mail/auth target before functional acceptance; do not infer Gmail is missing from historical TEST. |
| `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=false` | Notification flag only. `service/SubscriptionScheduler.java` and other maintenance/reconciliation jobs still run and may mutate rows or attempt external work. |

Full Java paths in this table are relative to
`src/main/java/com/atstudio/atstudio/`. Required secrets and SMTP target tokens
remain unresolved. Real SMTP authentication/TLS settings depend on the selected
provider; never use the example's false flags as a production mail recipe.
Before any approved first start or isolated restore, enforce and verify
external-network isolation. Blank Provider keys, disabled mail-dependent UI,
disabled notification and an empty allowlist are **not an egress firewall**.
Do not reuse retained payment data with an invented replacement keyring.

## 4. Proxy, Access And Resource Boundaries

Only direct Internet client -> nginx -> loopback Java is supported. No CDN,
load balancer, PROXY protocol or real-IP rewriting is configured. The upstream
header allowlist retains auth, CORS, idempotency and conditional/Range headers;
all other client headers, including arbitrary `Forwarded`, `X-Forwarded-*`,
`X-Real-IP` and provider IP headers, are omitted. `X-ATStudio-Client-IP` is written
once from `$remote_addr`. Host is the fixed rendered FQDN, never client-derived.
New application header contracts require review of this allowlist.

`/api` and `/uploads/` go through Java. There is no storage `alias` or static
storage root. The deny regex additionally covers staging, audio, company
documents, question and notice files. Backend checks remain authoritative for
encoded, traversal, case, semicolon and repeated-slash variants; target tests
must prove no file delivery. Do not add a `^~ /uploads/` that bypasses regex checks.
`/api` cannot fall back to the SPA. The `/uploads/notices/` deny does not affect
public `/api/notices/{id}/attachments/{id}` controller downloads or
`/uploads/albums/` and `/uploads/playlists/` thumbnails. SPA responses add
`nosniff`, `Referrer-Policy: no-referrer` and `X-Frame-Options: DENY`, all with
`always` in the same `location /` as SPA fallback. This denies framing of the
SPA, not child frames it creates; no broad application CSP is added. The
[nginx header inheritance rule](https://nginx.org/en/docs/http/ngx_http_headers_module.html#add_header)
means a server-only frame header would not suffice with location-local headers.

The quoted media location pattern is
`^/api(?:;[^/]*)?/tracks(?:;[^/]*)?/[^/]+/(stream|download)(?:;[^/]*)?/?$`.
A complete non-slash ID segment covers decimal, signed and hexadecimal forms
without duplicating Spring's numeric parser. Optional matrix suffixes on fixed
segments and any ID suffix also receive the media limits. Quotes are required
around this nginx regex because it contains literal semicolons. Matching a
candidate for tighter limits does not authorize it or assert that the backend
accepts it. No local firewall/path-matching override was found; native
normalization, matrix acceptance/rejection and framing remain target tests.
The contract exposes `nginx.media_location_pattern`, download/multipart maps,
explicit connection-zone composition and `nginx.spa_response_headers`;
schema version remains 1 with reviewed additive fields under WI012.

`proxy_pass` deliberately has **no URI suffix**. Buffering/cache/store/retry and
error interception are off; application Range/206/416 responses are not replaced
or cached. Uploads stream to Java; nginx still has a private fallback body-temp
directory. Spring and nginx request caps are both 120 MiB; each Spring multipart
file is capped at 100 MiB. Lower endpoint-specific limits still apply.
[Official proxy documentation](https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_pass)
explains URI handling; its [buffering controls](https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_buffering)
explain response disk spill and streaming.

Initial limits are **unmeasured settings**, not supported user counts:
10 API requests/s/client with burst 40; media adds 3/s with burst 8; TLS request
concurrency 8/client and 32/host, tightened to 4/client and 16/host for track
stream/download; downloads additionally 1/client and 2/host. Streams use
256KiB/s, downloads 2MiB/s. Multipart requests add 1/client and 2/host independently
of path, method or authentication; child locations explicitly retain applicable
general/multipart/media/download zones and API/media rate rules. Application
admission also covers non-multipart writes across six domain families.
Concurrent downloads/multiple IPs
can exceed a purchased link budget. These are not a DDoS defense or a provider
charge ceiling. HTTP/2 is not enabled here; if later enabled, concurrent streams
count individually under [nginx connection limiting](https://nginx.org/en/docs/http/ngx_http_limit_conn_module.html#limit_conn).
Body/send 60s and proxy-read 360s are **inactivity**, not whole-request deadlines.
Slow transfers may occupy connections/storage longer; measure this on target.
Every capacity run must record exact rendered limits, JVM/cgroup settings,
software versions and artifact identity alongside measured results.

The initial restricted-access file has only `satisfy all; deny all;`. Add only
approved exact client IP/CIDR entries before the final deny, preserving it.
Removing all approved entries must leave the final deny. ACLs inherit into app
locations; never add a location-local allow without its deny. See the
[access-rule inheritance reference](https://nginx.org/en/docs/http/ngx_http_access_module.html).
HTTP-01 token files on port 80 are an explicit public exception. The HTTPS
candidate's fixed-host HTTP redirect, and error-only `return` directives,
execute before the access phase; they are not restricted by the HTTPS ACL.
No HTTP bootstrap/redirect location serves application or storage content.

## 5. Logs, Service And Database

The access format contains only time, method, status, byte count and elapsed
time; no URI (even path), query, address, header or body. It goes to local syslog
`/dev/log` with tag `atstudio_access`. The journal candidate bounds size/age and
disables forwarding to syslog. Verify actual receipt and any distro rsyslog,
remote journal, APM or security-agent copies before processing real credentials.
Host-wide journal limits can drop unrelated logs and are not per-service quotas.

**Access-log filtering is not error-log redaction.** nginx request-context errors
may contain raw request targets, including verification/reset tokens. This
minimal pre-activation policy discards nginx error logs to `/dev/null` at the
top level, without lower-level overrides. This sacrifices nginx error detail;
use safe access status counts and service state for initial monitoring. Never
enable request/debug logs against real tokens; reproduce with synthetic values
under a separately reviewed logging change. Startup/test stderr, application
exceptions, SQL/driver failures, OS tooling, existing files and external agents
are separate surfaces, **not proven redacted**. No retrospective log cleanup is
authorized. [nginx logging](https://nginx.org/en/docs/http/ngx_http_log_module.html)
and [error_log levels](https://nginx.org/en/docs/ngx_core_module.html#error_log)
do not provide a query-token redaction guarantee.

The unit launches one foreground JVM, and control-group stop covers FFmpeg.
Do not start a second unit/manual JAR against the same tuple; stop old workers
before a release switch. Java heap 256-1024 MiB, metaspace 256 MiB, cgroup soft
2 GiB/hard 3 GiB include native/encoder pressure outside heap. The cap can
kill valid workloads; MySQL, nginx, OS, disk/temp and backup need separate
headroom. `/usr/bin/java` must actually resolve to Java 17; verify FFmpeg and
`libmp3lame` as the service identity. These existence checks are not capability
or synthetic-encode checks. No `MemoryDenyWriteExecute` rule blocks JVM JIT.

MySQL binds only IPv4 loopback and disables the unused X Plugin (which otherwise
has its own listener; see [MySQL X Plugin options](https://dev.mysql.com/doc/refman/8.0/en/x-plugin-options-system-variables.html)).
No users, DB, grants or SQL execution are supplied. Approve the exact fresh DB
before initialization; runtime user needs only the application's required DML
on that schema, no global/DDL/FILE/GRANT privileges. Use a separate bootstrap
operator, then `ddl-auto=validate`. Confirm actual MySQL include paths, service
name (`mysql.service` is provisional), socket permissions and all IPv4/IPv6
listeners. Local TCP is not an encrypted transport guarantee. Query logging is
off, but MySQL error/binlog retention and host logs require target review.

## 6. Later Approved Deployment Order And HOLD Gates

1. Confirm host/product/domain, target distro/tool versions and recovery console.
   Review ingress/egress rules, SSH administrator CIDRs and IPv4/IPv6 together.
   Keep a proven recovery session before any SSH/firewall change. None is applied.
2. Prepare reviewed immutable release, identities, directories and private config.
   Replace every token **outside Git**; check exact origins, Base64 JWT, required
   keyring and least-privilege DB identity without revealing values. Resolve mail
   target or explicitly approve an isolated non-mail startup. No tool installation
   or secret-generation commands are part of this package.
3. Apply the separately approved fresh-only schema/seed procedure to the exact
   verified-empty DB; no test users. Record the DB/media tuple and source/JAR/SPA
   hashes. Never apply against the retained TEST DB. This template runs no SQL.
4. Stage the HTTP-bootstrap candidate and shared snippets with the deny-all ACL.
   Validate the complete configuration, then only under target-change approval
   load it. Complete an approved ACME HTTP-01 issuance workflow with token-only
   webroot (or separately approved DNS procedure). No ACME client is selected or
   installed here. The TLS candidate cannot pass without actual certificate files.
5. Stage the TLS candidate. Certificate renewal must validate and reload nginx
   only on success, preserve key permissions and the restricted ACL, and report
   failure/expiry. HTTP-01 renewal retains the token-only exception. No unattended
   issuance/renewal or firewall opening is claimed.
6. Validate service/DB configuration, tool versions and external-network isolation.
   Start one backend only after explicit runtime approval; Type=simple is not
   readiness. Verify strict storage audit and a bounded read-only health/capability
   check before adding approved tester IPs. No general public release follows.
7. Verify intended/unknown hosts, deny-all then approved/denied clients, forged IP
   headers, IPv6, private-path variants, 200/206/416 streaming and original download,
   multipart limits, effective logs, stop/restart, memory/disk and renewal failure.
   Restore, notifications, selected SMTP and paid behavior need their separate gates.

Examples of **future target commands, NOT RUN here**, after the associated gates:

```sh
nginx -t -c /etc/nginx/nginx.conf
systemd-analyze verify /etc/systemd/system/atstudio.service
mysqld --validate-config
/usr/bin/java -version
/usr/bin/ffmpeg -hide_banner -h encoder=libmp3lame
```

Use the exact distro-supported validators and selected config. `nginx -t` may
open log/config files; it is not a side-effect-free Windows test. Avoid public
`nginx -T`, environment or journal dumps. Reload/daemon-reload/start/enable,
reboot and any DB command remain target operations, not executed by this WI.
SSH, firewall, ACME, MySQL/journal changes and monitoring are not automated.

Rollback: before application, withdraw only these owned candidates. After a
later approved deployment, restore the prior reviewed config/release with no
overlapping JVM, validate before reload, and check schema/media compatibility.
Do not delete storage, DB rows, keys, logs or other agents' files as rollback.

## Related Documents

- [Approved REQ](../../deliverables/user/REQ-20261003-ATS-001.md): Staged approval and exclusion boundaries.
- [Runtime storage operations](../../docs/design/runtime-storage-operations.md): Integrity, WAV/MP3 and historical TEST/phone evidence.
- [Security policy](../../docs/policies/security-policy.md): Logging, token, media and Provider rules.
- [Current decision entry](../../docs/index.md#current-decision-entry): Preserved Gmail/TEST and current production HOLD.
- [SR-93](../../docs/SR/SR-93.md#remaining-production-gates): Unclosed target readiness gates.
