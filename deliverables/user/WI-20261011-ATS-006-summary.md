---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-001.md
    reason: Approved local preparation scope
  - path: ../agent/WI-20261011-ATS-006-evidence-pack.md
    reason: Exact candidate, tests and limitations
---

# WI006 Release and Schema Probe Preparation

> Purpose: Hand a bounded local candidate to WI007 for independent review.

TL;DR: local implementation complete; native NOT_RUN, production HOLD.

- WI026's recorded backend638/frontend336 inputs, unchanged existing JAR and
  isolated143-file SPA match. Four explicitly named coverage test files remain
  outside the historical comparison; the Evidence Pack lists every path.
- Existing JAR libraries and43 entity mappings load successfully without any
  connection: `MAPPING_READY`. The probe directly invokes Hibernate's schema
  validator, with Boot's actual bundled naming strategies; it does not construct
  Spring, a SessionFactory, an application process or worker.
- New tests19/19 and reused JDBC guard regression29/29 pass locally. No real DB,
  private env, network, SSH, browser, provider, installation or remote action.
- Final candidate: `build/WI006-schema-v3`, aggregate bundle SHA-256
  `f06f7f2de2a891a482515f92abac8dfe75b4dfdddd1718a91f9c9592958a642b`.
- The native procedure preserves restricted TLS and renewal. Old `host_guard()`
  is prohibited. Fresh approval uses pinned historical identities from the
  prior private approval, not the bootstrap receipt or a newly observed UUID.
- Risks: native JDBC metadata/validation remain untested; target structure
  digests are not a canonical fresh-schema proof. Full startup, configuration,
  recovery, mail, ADMIN, capacity and public exposure are separate gates.

## Next WI

WI006 blocks WI-20261011-ATS-007. MA should delegate PG now for exact code,
artifact and runbook review. No native execution may precede PG PASS. No
descendant was created by SE and REQ/WI008 are not closed here.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-006-evidence-pack.md): Reproduction, hashes and exclusions.
- [Runbook](../../deploy/single-vps/runbooks/release-readiness.md): Gated native sequence.
