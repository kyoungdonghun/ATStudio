---
version: 1.0
last_updated: 2026-11-17
project: ATS
owner: MA-DOCOPS
category: registry
status: confirmed
dependencies:
  - path: git-checkpoint-20261107.md
    reason: Previous bounded source-preservation checkpoint
---
# Phase-one client-test source checkpoint — 2026-11-17

This scoped checkpoint preserves the source for administrator login, music upload,
tagging, editing and explicit publication, followed by anonymous listening.
Clients own the actual music and its upload/publication decisions. This checkpoint
supplements [November 7](git-checkpoint-20261107.md); it is not full operating-stage
acceptance, a private-data backup, or grant-agency approval.

## Previously observed repairs included in this source snapshot

The following are dated MA observations recorded in locally retained evidence,
not server actions performed by this Git review.

| Date and local evidence | Bounded observed outcome |
|---|---|
| November 14, WI-20261114-ATS-006 | Existing ADMIN tag dictionary/create/update restored in listening mode. Actual tag creation, track association and saved edit-page readback passed. Other restricted administrator/payment/download operations remained restricted. |
| November 15, WI-20261115-ATS-001/004 | Six actual single/multiple upload and tag-edit scenarios passed. A separate anonymous playback/seek check and bounded QA cleanup completed. This is not a fresh claim about current client records. |
| November 16, WI-20261116-ATS-011 | WAV classification corrected without changing original audio. Actual original-WAV upload reached READY and saved edit readback passed. Safe upload/edit error messages replaced generic HTTP errors. |
| November 16, WI-20261116-ATS-014 | WWW DNS, the two-name certificate, fixed-origin HTTP/HTTPS redirects and current-config renewal hook were applied. External redirect/TLS checks and saved-renewal rehearsal passed; first failed rehearsal remains preserved. |
| November 16, WI-20261116-ATS-019 | Explicit one-track publication now atomically activates a ready inactive track and publishes it, retaining revision, authorization, readiness and unknown-outcome guards. Reviewed artifact deployment and management UI checks passed; customer publication was left to the user. |

The last recorded backend artifact is SHA-256
`feff6919a291caff985d0c386d23ba30ee53ff0b2a9a32fd5d162208bf401a91`.
This cache task does not require a new JAR, schema change or application/DB restart.
Historical tests and repeat reviews overlap; their case counts must not be summed
as new unique coverage. Native READY and physical audio assessment are different.

## Cache repair verified on November 17

MA applied the reviewed frontend and webserver configuration; independent CR
reconciled the native and external evidence in WI-20261117-ATS-003/005. HTML now
requires revalidation; successful fingerprinted assets use immutable caching;
missing assets return 404/no-store. The shared failure screen offers an explicit
current-document reload, warns about unsaved input and does not automatically
reload, clear authentication storage or replay a mutation.

The local evidence comprises 18 focused frontend/route tests, 10 listening-mode
tests, both frontend builds, TypeScript and scoped ESLint; cache/native suites
passed 10/7 cases and independent local probes passed 10 cases. The additive
correction below passed four author and six independent probes. Repeated and
overlapping suites are separate evidence, not a sum of unique cases.

The first live delayed-route test failed: its cached entry belonged to an older
approved tag release outside the initial retained generations. The correction
added 16 exact-manifest-approved static files without replacing existing files,
HTML, server configuration or application data. The final union contains 81 files
from four original approved listening generations, bounded to 512 files/64 MiB.
The first failure remains recorded; it was not relabeled as a successful trial.

After that correction, the same old browser tab opened its detail route through
the existing retry button without a document reload; its old entry script remained
unchanged. A separate current-version detail page also survived a normal reload.
These checks did not alter client records or trigger playback.

External HTTPS readback matched all 81 distinct frontend byte hashes. Eight
allowed HTML route variants returned 200/no-cache and conditional 304/no-cache.
The original 75-check and additive 28-check receipts overlap and must not be
reported as 103 unique tests. Missing-file behavior, inherited security headers,
WWW redirects, TLS and specified registration/download restrictions passed.
The backend JAR and application/DB/nginx-master process identities were preserved;
only the initial configuration change used a graceful nginx reload.

| Reviewed deployed item | SHA-256 |
|---|---|
| Initial static/config payload manifest | `476f91ad9d4d1def3086033d32bd0b18b5fed0867a32ebd2decd6f40ea66eee2` |
| Additive approved-static manifest | `d91cc68c9907fb79fe0bdb894a25e21f015cbbce52458555fc55be7e4be8ea04` |
| Active cache configuration | `b5964410d3a98e0ee3708a6b827609a5b6f6d27c79b0ccae8adfdca5882ffadc` |

Temporary TCP22 access was restored to its previous restricted source and the
owned SSH key was removed. Direct Windows service readback confirmed SSH agent
Stopped/Disabled. A bounded current-PC TCP22 probe timed out after restoration,
while the public HTTPS listing still returned 200/no-cache. Public HTTP/HTTPS
access and the running application/DB are retained.

The implementation/native/access-cleanup scope is complete and the phase-one
site is prepared for client testing. This document is selected for the final
privacy-reviewed local Git commit; no push is part of this checkpoint. The
commit's revision is recorded separately after the exact index is committed.

This common deployment behavior is reusable for subsequent phases. It cannot
guarantee arbitrarily old cached pages or compatibility with future API changes.
Old already-loaded code does not gain the new recovery button until refreshed.

## Public reconstruction pointers and exclusions

- [Client upload guide](../../deliverables/user/ATStudio-client-upload-guide-20261107.html).
- [Authenticated-administrator edge template](../../deploy/single-vps/nginx/listening-authenticated-admin-https.conf.template).
- [WWW and TLS renewal runbook](../../deploy/single-vps/runbooks/www-alias.md).
- [Frontend cache and retained-asset runbook](../../deploy/single-vps/runbooks/frontend-cache.md).
- [Static union preparation](../../scripts/deployment/frontend_cache.py), [native apply](../../scripts/deployment/frontend_cache_native.py) and [bounded additive repair](../../scripts/deployment/frontend_cache_additive.py).

Detailed REQ/WI history, raw server receipts, private inputs, credentials, original
client audio, generated JAR/frontend artifacts and unrelated historical dirty
files remain under local custody outside this Git selection. The Git index uses a
HEAD-derived documentation index with this bounded new entry; the active local
chronology is not overwritten. Uncommitted local files may therefore remain.

The subsequent client test, physical/mobile checks, operational mail and PG
onboarding, periodic backup/alerting/recovery, load/cost limits and grant-agency
recognition retain their separate verification and approval boundaries.
