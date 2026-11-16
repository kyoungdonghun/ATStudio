# Listening frontend cache and release continuity

Owner: WI-20261117-ATS-002 / REQ-20261117-ATS-001.

## Policy

SPA HTML at the exact public/login/admin route allowlist uses `Cache-Control:
no-cache` on 200/304. Queries do not change the nginx location match. The browser
may retain HTML but must revalidate it before reuse. Fingerprinted JS/CSS/image/
font paths receive one-year immutable caching only on HTTP 200. Other asset
responses, including missing-file 404, are `no-store`; they never fall back to
SPA HTML. API, authorization, WWW redirects, TLS and media policy remain intact.

The renderer inserts maps in the site's existing **http-context include** and
repeats all three inherited security headers inside every location that gains
an `add_header`. Do not move these maps into a `server` block. No nginx modules
or external packages are added.

## Build and retention

1. Build reviewed source twice: normal mode for verification and
   `VITE_APP_MODE=listening` for the deployable candidate. Never package normal
   mode assets into the listening release.
2. Record the new build's original file SHA-256 manifest. Select up to three
   previously approved **original** listening generation manifests and verify
   their historical SHA-256 pins and exact directory contents.
3. Use `scripts/deployment/frontend_cache.py` `assemble()` with newest-first
   `{root, manifest, sha256}` inputs. It accepts only index.html and fingerprinted
   public assets; retains only the newest HTML; rejects extra files, symlinks,
   path/name violations, byte changes and same-name/different-content collisions.
   Limit: 4 original generations total (new plus three previous), 512 union files,
   64 MiB. The initial migration includes the proven older tag generation after
   a real retained browser tab identified it. The preceding
   union must never be passed as an original generation, which would make
   retention unbounded. Keep lineage/manifests outside the public directory.
4. Render the site with `render_site()` using the pinned canonical unmanaged
   site/template including the current WWW/TLS appendix, exact old/new root.
   Existing managed output is rejected to prevent stacked maps/headers. When
   other site rules change, review the canonical baseline first; never silently
   fall back to an older apex-only template. The WI002 baseline is stored with
   its pinned release bundle, not fetched from arbitrary content.
5. Keep the old complete releases privately/in place until a separately approved
   cleanup. The active union is bounded; this procedure does not remove releases
   or claim total server disk retention is automated. Inventory storage before
   subsequent releases and plan cleanup separately.

## Switch and rollback

The reviewed WI002 `scripts/deployment/frontend_cache_native.py` is an exact
target native adapter, not an unattended general deploy daemon. It verifies
fresh identity/config/runtime/TLS/frontend hashes, current disk space, and a
manifest-pinned frontend-only archive. It writes a new exclusive release, tests
the candidate nginx wrapper before replacing the active site, then performs
only a graceful nginx reload. JAR, app unit, DB and their invocations stay
unchanged. Old nginx requests/workers can finish normally.

Each attempt consumes an exclusive private claim. Success/failure and the old
site are preserved privately. On failure after active replacement, restore
only if the active bytes are still owned by that attempt, syntax-check and
reload. Unknown transfer, missing receipt, foreign config or failed rollback
means HOLD: inspect exact state, do not replay, delete the claim, or label PASS.
New deployment targets/pins require a newly reviewed adapter, preserving the
old one and its evidence. Native filesystem permissions, nginx syntax and live
response headers must be verified on the target; mocked local tests are not
that evidence.

## Verification and limits

Verify `/tracks`, `/tracks/ID`, `/login`, `/admin`, `/admin/track-manage`,
`/admin/tracks/upload`, `/admin/tracks/ID/edit`, `/admin/tags`, including queries:
HTML200 no-cache, conditional304 no-cache; current and retained hashed assets
200 immutable; nonexistent hashed assets404 no-store; security headers remain.
Verify WWW HTTP/HTTPS still redirect to the literal apex preserving path/query,
TLS names/expiry and app/DB invocation IDs remain unchanged. Do not change songs
or account state to test cache behavior.

Keep an old page open through a deployment and request an unloaded route. The
retained original assets serve old import graphs. The explicit latest-screen
action handles aged-out or unavailable chunks without automatic form loss or
reload loops. This protects the bounded release window, not arbitrarily old
tabs or backwards-incompatible future API changes. Future API changes require
their own compatibility/migration review. Existing browser-cached HTML may need
one explicit reload before its first new revalidation policy is received.

Focused commands:

```
python scripts/deployment/test_frontend_cache.py -v
python scripts/deployment/test_frontend_cache_native.py -v
```

The historical WI002 loopback probe loads actual prior HTML/entry, switches the
served root, then requests all prior assets/import targets by exact hash. It is
asset-availability evidence, not DOM execution or nginx behavior proof.

## Initial older-tag migration

A real predeployment browser retained the 2026-11-14 tag-generation entry
`index-7EeFqHAZ.js`, older than the first candidate's WAV/publication retention.
That tab failed on its delayed route after the first switch. Preserve that
failure rather than claiming the first retention window covered it.

`scripts/deployment/frontend_cache_additive.py` is the separately reviewed
one-time exact migration adapter: verifies all 65 first-candidate files and the
unchanged config/runtime, then adds precisely 16 manifest-proven older tag JS
files. It uses complete private temporary bytes and atomic hard-link creation
without overwrite; entry is last. No HTML/site/JAR/media mutation, nginx reload
or app/DB restart. Final static union is 81 files. Additions use a new private
claim/receiver, preserving the first apply receipts. On partial failure, retain
complete additions and HOLD for reconciliation; do not delete or blindly replay.
The short additions window is not a guarantee that an old browser's concurrent
import cannot fail; a browser that already cached an import rejection may need
an explicit document reload after availability is restored.

Future normal builds retain at most three reviewed original predecessors plus
the new generation under the same 64 MiB cap. The additive adapter is not a
general scheduler or permission to expand asset scope.

`test_frontend_cache_additive.py` includes one optional historical integration
fixture check. The exact reviewed archive is kept in local build custody and
is intentionally absent from Git; a clean checkout explicitly skips that one
test. The other synthetic/atomic checks still run. An explicit skip does not
replace the archived local PASS evidence and does not prove native deployment.
