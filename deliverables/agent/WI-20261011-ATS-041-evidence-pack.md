---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-041-handoff.md
    reason: Approved documentation-only write boundary
  - path: WI-20261011-ATS-039-evidence-pack.md
    reason: Frozen candidate and owner tests
  - path: WI-20261011-ATS-040-evidence-pack.md
    reason: Preserved independent conditional review
  - path: WI-20261011-ATS-038-evidence-pack.md
    reason: Preserved first restricted-web HOLD
---

# Evidence Pack: WI-20261011-ATS-041

> Purpose: Separate the new native HOLD, parser diagnosis and final access cleanup from local readiness.

## Summary and Scope

**TL;DR: MA's WI039 trial ended PREPARE / DUPLICATE_JSON / HOLD at16:15:40
KST,2026-10-11, before app/DB start. REQ006 remains active/HOLD.**

- [x] Exact native result and15 rows retained:6 PASS/9 NOT_APPLICABLE.
- [x] Source identity, local tests, conditional PG review and actual native result separated.
- [x] Final MA key/agent/console cleanup and quarantine HTTPS evidence received.
- [x] Current checkpoint/REQ update and own two reports/receipt only; old evidence preserved.
- [x] New local-only approval recorded separately; no further native trial authority.

All target/HTTP/browser observations belong to MA. PG owns the final bounded
diagnosis; DocOps performed document integration only, without source/runtime
edits, remote/network/browser/secret access, application tests, installation,
deletion, Git mutation, memory write or subdelegation.

## Actual Evidence

[Machine receipt](WI-20261011-ATS-041-native-receipt.json) keeps native.result
exactly as supplied. Supporting sections reduce public observations, omit private
addresses/logs/keys/tokens, and retain receipt IDs/timestamps. No screenshot file
pointer was supplied or invented. All times below are2026-10-11 KST.

| Time / receipt | Owner and bounded observation |
|---|---|
|16:14:47 / b47be0|MA used QA036 helper:641 backend/336 frontend/143 SPA files match;4 exclusions separate. L1 actual upload153 once. No rebuild.|
|16:14:58 / 2af38a|S1 root stage10 public files.|
|16:15:00|MA read-only provider inbound screenshot/outbound accessibility observation: management-only22/443,80 public IPv4; outbound TCP80/443/53 and UDP53/123 unchanged. No management address published.|
|16:15:05 / 0b7e2e|S2 PASS_READ_ONLY; runtime/browser NOT_RUN.|
|16:15:26 / d9230f|S3 fresh approval once.|
|16:15:33 / 642825|S4 DISPATCHED/PENDING once, not startup acceptance.|
|16:15:40 / 5f4491|Authoritative final HOLD; ssh_exit0/empty stderr does not imply trial success. App NOT_RUN,6 PASS/9 NOT_APPLICABLE.|
|16:16:39 / e16998|Nginx duplicate property ExecReload; public contract duplicate keys0. Baseline/web release/MySQL overlay/app unit absent.|
|16:17:12 / 6ef710|Legacy preflight HOLD22/23,exit2. Only retained historical approval-absence false; every other check true. No reinterpretation/deletion.|
|16:18:03 / d2e1bf|Supervisor loaded/static/failed/PID0; app not-found/inactive/dead/PID0; MySQL masked/inactive/dead/PID0.|
|16:18:03 / 16:18:23.975|Owned SSH key removed; list rc1/no identities. UAC helper exit0; Get-Service Stopped/Disabled confirmed.|
|16:18:30|User logout confirmation plus MA screenshot login prompt; recovery tab subsequently closed, provider portal retained. No required exec sessions remain.|
|16:18:43 / edd026|Normal TLS, no cookies/proxy/redirect: HTTPS200/exact quarantine body. Not restricted-app HTTP acceptance.|

The first outer PowerShell nested here-string failed parsing before any execution.
Only caller quoting changed; exact L1 body was hash-verified unchanged. This is a
local transport error, not another upload or server attempt.

No baseline/app/DB/web activation occurred. OPEN, native containment proof,
L2/L3, application-browser navigation and S6/S7 are NOT_RUN. PREPARE already
finished cleanup, so no finish marker was needed; S7 permits only BOUNDED_PASS.
The ON_HOST_ONLY scope is not a successful isolation proof. Resource snapshots
are not capacity measurements. Earlier portal expiry/re-login history remains
dated WI038 evidence; the fresh16:15 policy observation does not claim an
evergreen portal session or current cloud state beyond that observation.

## Diagnosis and Local Gates

MA property-shape observation and PG's final read-only diagnosis establish the
new P2: `deploy/single-vps/tls-quarantine/nginx-service.conf:8` defines two valid
ExecReload commands. `build/WI039-restricted-web-check/web_session.py:70` uses
r.unique and rejects repeated Exec command properties with DUPLICATE_JSON.
PG's inert reproduction returned that code. Prior tests covered rejection, not
valid multi-ExecReload. This is not a duplicate public JSON contract or WI037's
intermediate deactivating-status P2. Preserve WI040's exact conditional ALLOW as
review-time evidence; this actual final HOLD supersedes deployment readiness.

Potential repair must preserve command contents/order in an ordered list, retain
JSON/scalar duplicate rejection, and add native-shape preflight before claim.
No implementation was changed in WI041. The user subsequently approved local-only
repair/tests/review/preflight preparation via WI042 SE -> WI043 PG -> WI044
DocOps; results belong to that later chain. No renewed remote/start/secret use.

| Local owner | Evidence, not native proof |
|---|---|
|SE, Windows, freeze16:01:54|95/95 Python mocks,0.406s;8/8 PowerShell synthetic assertions;16 in-memory compile units and4 parsed PowerShell blocks.|
|PG, Windows|95/95 regressions,0.416s;9/9 focused tests,0.134s;8/8 PowerShell synthetic assertions.26 files/11 bodies/42 historical pins match.|
|MA after native HOLD|Read-only local snapshot again matched26 frozen files,11 recipe bodies,42 historical pins; no source/candidate changes or new target operation.|

Overlapping counts are not unique tests or coverage percentages. See
[owner receipt](../../build/WI039-restricted-web-check/local-receipt.json) and
[PG receipt](../../build/WI040-restricted-web-review/test-receipt.json).

## Exact Identity

| Artifact | SHA-256 |
|---|---|
|Existing JAR,70306351 bytes|`01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`|
|SPA143-file set|`43a0384c5f48d100e4af6b20d989a2186b34e6900cf4e69c94b010e9671c8647`|
|WI039 ten-file bundle|`fb643ff04e5c0f1e90f1b110eafa312c311a3d560a822769ff43f5ed432d379b`|
|WI03926-file review set|`1a19015eb8e890b02d6973331dfb2ef7fd7458821694e929ba425cd1ab3c1572`|
|runtime.py|`07392a980ab6a029a73e27d0ce7e1c00d8c296964d47adc5344020029d57877f`|
|web_session.py|`d2aba516a7e9850ebfe18e5e48f1adcc22fc103845a1372e0e05f244ff8dc2cc`|

[Freeze](../../build/WI039-restricted-web-check/freeze.json) and
[recipe pins](../../build/WI039-restricted-web-check/recipe-pins.json) retain exact
files, algorithms and all11 bodies. SPA is a file-set hash, not an archive hash.
The identity receipt is current local byte matching, not new build provenance.

## Documentation Verification

Five changed paths only: docs/index.md, REQ006 and own WI041 receipt/summary/EP.

- Existing validate-docs skill loaded via `python -I -B -`/`runpy`; root Markdown
  enumeration bounded to docs/**/*.md, REQ006, WI034/038/039/040/041 Evidence
  Packs, WI039-041 handoffs and the WI041 summary. Result:232 files,239 supported
  traceability IDs,zero errors/warnings,exit0 (`20a27e`). External links/anchors,
  other deliverables and broad build history were not scanned.
- Direct PowerShell checks: four Markdown metadata/dependency sets, five
  whitespace/final-newline checks, native object exact equality and15 ordered
  cleanup rows with6/9 counts, new-report address/token-locator scan: PASS,
  exit0 (`de6d8f`). These checks include untracked outputs; the locator scan is
  bounded, not general secret-detection proof.
- Scoped `git diff --check --` over the five paths: exit0; only the existing
  index CRLF-to-LF normalization notice. One attempted reuse of an unavailable
  in-memory validation command failed before shell execution; the explicit
  command above ran successfully, with no file or runtime effect from the failure.
- Previous index checkpoint content compared unchanged. Nine public SHA-256
  before/after matches: WI038 evidence/summary/receipt, WI039/040 evidence and
  WI039 freeze/recipe-pins/runtime/web_session. No application/JAR re-audit.

No full-repository, application or native validation is claimed. The frozen
WI041 handoff lacks YAML metadata; outside this five-file scope and left unchanged.

## Limits and Chain

WI035/WI038 history and WI040 verdict remain unchanged. Prior WI034 BOUNDED_PASS
retains its scope; older WI026 cleanup cause remains NOT_DETERMINED. Popular/
latest ordering and Tag-document discrepancies remain deferred. No login/media/
mail/payment, pristine-browser/full-traffic, capacity/load or public-release
acceptance. No additional paid product; existing server billing continues.

WI039 -> WI040 -> MA consumed trial/cleanup -> WI041 closes this reporting chain,
not REQ006. The web goal is unmet. WI042/043/044 is local-only and separate;
no new native trial is authorized. Documentation rollback would require a scoped
authorized reversal of this patch, never history deletion or operational retry.

## Reference Documents and Related Documents

Retained Tier0 STD-001/004/002/005, DocOps role and security context. Applied
create-wi-evidence-pack and validate-docs skills to the existing
[WI041 handoff](WI-20261011-ATS-041-handoff.md). No replacement handoff generated.

- [REQ006](../user/REQ-20261011-ATS-006.md): active/HOLD and local-only amendment.
- [WI039](WI-20261011-ATS-039-evidence-pack.md): preserved local implementation.
- [WI040](WI-20261011-ATS-040-evidence-pack.md): preserved historical review.
- [WI038](WI-20261011-ATS-038-evidence-pack.md): original restricted-web HOLD.
- [WI034](WI-20261011-ATS-034-evidence-pack.md): earlier bounded native success.
- [Summary](../user/WI-20261011-ATS-041-summary.md): user-facing result.
