---
version: 1.0
last_updated: 2026-10-18
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: WI-20261011-ATS-067-listener-padding-handoff.md
    reason: Preserved v3 history
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved autonomous bounded fixes and Gmail trial
---
# WI067 Full Operating Environment Reader Correction

[WI HEADER]
WI: WI-20261011-ATS-067; REQ: REQ-20261011-ATS-007; Agent: se.
Depends On: Native v3 diagnostic and PG071 narrow correction analysis.
Blocks: PG071 exact-byte review, MA native preflight, bounded human Gmail trial.

[WI SUMMARY]
Why: original_env incorrectly requires the complete operating environment to contain only nine private keys. Actual root-only file has 83 assignments, all required nine keys present, no duplicates, valid uppercase names. It fails on SPRING_PROFILES_ACTIVE before private staging or child execution.
Scope: New sibling build/WI067-auth-trial-v4, build/WI067-pc-delivery-v4, build/WI067-private-producer-v4, build/WI067-env-supersede and create-new evidence. Preserve all prior candidates and exact JAR/SPA. Correct only complete environment validation and nine-key extraction, with established configuration writer/parser provenance and explicit complete key/value policy. Do not edit the operating environment.
DoD: Full valid operating configuration accepted; malformed, unknown, duplicate, injected, missing or mismatched fields rejected. Decode only the documented literal format, never execute or generally strip quotes. Keep child environment construction and nine-key validation unchanged, full original file hash pinning, stage/recheck identity and every network/BPF/security gate. Include integrated producer-to-stage synthetic tests against the full configuration fixture, not only nine-key mocks.
Forbidden: No remote/secret/browser operations by agents; no actual app/DB/mail; no purchases, public exposure, operating config or secret changes; no deletion, overwrite, automatic retry or removal of existing security checks. Native use remains MA-only after exact PG review.

[ACCEPTANCE CRITERIA]
- [ ] Identify authoritative writer/template and permitted key/value grammar without reading private files.
- [ ] Minimal complete-config reader with explicit validation and exactly nine decoded private values.
- [ ] Regressions cover valid full 83-key shape, quotes/escapes, duplicates, unknown keys, injection, missing keys, secret substitution, child isolation and unchanged full-file drift guard.
- [ ] Fixed cold supersession preserves installed v3 tools and the exact now-staged release/evidence only after expiry and cold-state validation; no active private stage or unit exists. Obtain exact public release/evidence metadata from MA before fixing preservation contract. Never recreate expired approval or extend its expiry.
- [ ] New freezes/pins and finite operator procedure, independently reviewed before use. Read-only base-env plus full preflight proof precedes a fresh timed release/private producer where feasible.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md, development-standards.md, documentation-standards.md, glossary.md (retained full MA injections).
Tier1: docs/policies/security-policy.md and existing WI067/071 governance.
Tier2: create-wi-handoff-packet, test, create-wi-evidence-pack skills; existing WI067 handoffs and v3 files.
Related parsers: scripts/deployment/jdbc-readiness.py parse_env; scripts/deployment/validate_bundle.py parse_env; production-bootstrap.py and operating env example. Reuse existing bounded patterns with required full policy rather than shell interpretation.
Native evidence: c116e7 RELEASE_STAGED; 4f1de0 producer HOLD ENTRY_GATE; 87cdc3 r.load PASS and all stage/control/unit paths absent; 5df8d6 original_env BASE_ENV native.py216; 7f7f7a key metadata confirms full 83 assignments and all nine private keys. No actual values were printed. No Start occurred. Current release issued1792304173 expires1792305073. Original env remains untouched.

[OUTPUT CONTRACT]
SE owns new candidates, WI067 evidence/summary. PG071 owns incremental security review and exact final receipts. Communicate ready source to PG promptly; freeze only reviewed bytes. Do not rebuild unchanged JAR or re-audit unrelated code. MA executes approved native continuation and independently checks cleanup.

[TRACEABILITY]
Skill-generated continuation under existing approved REQ. Preserve native failures as historical evidence. Local tests or read-only preflight do not establish real Gmail success. Current human participation is available, but fresh readiness and timed release are required at actual trial time.

## Read-Only Preflight Addendum

MA611666 independently called the unchanged full read-only preflight before staging. It passed earlier service/listener checks but held at native.py250 with PATH: the general root-owned reader rejects the legitimate resolver-owned parent. MA4a69ae proves /etc/resolv.conf is the root-owned symlink ../run/systemd/resolve/stub-resolv.conf; /run and /run/systemd are root:root0755; /run/systemd/resolve is systemd-resolve991:9910755; stub-resolv.conf is991:9910644, regular, single-link. Existing resolver content and unit hashes match the release evidence.

Include one dedicated fixed resolver reader in v4, preserving general host.read/secure unchanged. Validate the exact link route, root ancestry, resolver numeric identity tied to the existing service/process checks, expected directory/file types and ownership/modes, no-follow bounded FD reads, stable metadata and content pin. Do not chmod/chown the OS file or admit arbitrary non-root files. Add exact native metadata fixtures plus wrong owner/path/mode/link/race/hash rejection tests. MA/PG reviews both observed compatibility defects together before another private stage. No general security bypass, preflight skipping or mutating diagnostic is authorized.
