---
version: 1.0
last_updated: 2026-11-16
project: ATS
owner: SE-OPS
status: prepared
---
# WWW alias and listening TLS renewal

The approved canonical site remains `https://atmusic.co.kr`. The `www` hostname
is an alias that redirects to the literal canonical origin while preserving
the request path and query. It does not add an application origin or API surface.

## Durable configuration

- Preserve the existing apex listening configuration byte for byte.
- Append `deploy/single-vps/nginx/www-alias.conf.template` only after issuing a
  valid certificate for **both** `atmusic.co.kr` and `www.atmusic.co.kr` in the
  existing `/etc/letsencrypt/live/atmusic.co.kr` lineage.
- During initial issuance, use only the HTTP block from that template so the
  existing webroot `/var/lib/atstudio-tls-quarantine/acme` can answer HTTP-01.
- Replace the installed historical
  `/etc/letsencrypt/renewal-hooks/deploy/40-atstudio-quarantine` body with reviewed
  `deploy/single-vps/tls-listening/reload-hook.py`. Its retained filename is not
  evidence that it should still test the old quarantine configuration.
- The replacement hook checks the live `/etc/atstudio/nginx/nginx.conf`, the exact
  two-name certificate/event, protected same-generation certificate/key links,
  then performs only a graceful Nginx reload. No application or database restart.

The historical quarantine template/hook sources and recorded hashes remain
unchanged. New deployments must preserve this alias appendix, expanded SAN and
live renewal hook. Do not regenerate an apex-only site from an older deployment
template and overwrite the active configuration. Existing exact-hash preflight
guards should reject such a stale deployment baseline; review/rebind the new
baseline instead of bypassing those guards.

## Validation boundaries

Verify DNS, certificate chain and both SANs, both www HTTP/HTTPS redirects,
path/query preservation, apex health, and unchanged app/DB invocation IDs.
Validate Nginx syntax before reload and retain original site/hook bytes for a
bounded rollback. An ACME failure/timeout requires read-only reconciliation before
any retry. Keep old certificate archive generations.

Use existing ACME accounts without account creation/TOS acceptance. The initial
issuer uses `--no-directory-hooks` only after proving no saved or global command
hooks. The final saved-renewal rehearsal uses `--run-deploy-hooks`, checking both
Certbot exit status and the hook's exact success marker. A successful CLI exit
alone does not prove hook success or future scheduled operation.

The existing Ubuntu `preconfigured-renewal = True` flag adjusts Certbot's
automated-renewal user notice; it does not itself prove the timer works. This is
documented in the [Certbot2.9 packaging guide](https://raw.githubusercontent.com/certbot/certbot/v2.9.0/certbot/docs/packaging.rst).
Retain and verify the actual timer and resulting renewal mappings separately.

## Execution evidence

REQ-20261116-ATS-003 and WI012–014 track preparation, independent review and
MA-owned operation. Current exact source pins, immutable native receipts and
completion status belong to their evidence packs. This runbook is not a claim of
production completion.
