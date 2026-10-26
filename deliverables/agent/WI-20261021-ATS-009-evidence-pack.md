---
version: 1.0.0
last_updated: 2026-10-21
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261021-ATS-009
dependencies:
  - path: WI-20261021-ATS-009-handoff.md
    reason: Approved bounded completion checkpoint
  - path: WI-20261021-ATS-007-evidence-pack.md
    reason: Independent local PG review and superseding r1 allowance
---

# Evidence Pack: WI-20261021-ATS-009

> Purpose: Record completed bounded Gmail authentication without extending it to production acceptance.

**TL;DR:** MA native final around2026-10-21 19:50 KST: BOUNDED_AUTH_FLOW_OBSERVED,
JAVA_RELEASED, cleanup PASS. Independent restoration PASS, console logged out,
owned key removed and agent Stopped/Disabled. REQ007 remains active.

## Scope And Exact Pins

| Object | SHA-256 |
| --- | --- |
| v8r1 runtime | `3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d` |
| v8r1 delivery | `fe4deec9ad1ef9881d7dc828395e51daca2191ee1aec0d1dc5e2333029db08a2` |
| v8r1 producer | `0a43d185540fd33f01b6a9d24df951d7979911e46d963f579575673e46c19efd` |
| Actual PG approval receipt | `60c12e1f54e60e22af28262845aaec2fdd4441f4fb42c1855d1103d61b9f254b` |

MA supplied candidate pins. DocOps independently hashed only the local public
[PG approval receipt](../../build/WI071-review/pg-final-dns-v8r1.json); it matches.
No candidate rebuild/rehash or remote action by DocOps. MA source643/JAR unchanged
verification applies to the retained candidate, not a new build or Git HEAD claim.

## Native And Human Evidence

Evidence owner is MA unless explicitly human-confirmed. Native operations were
not repeated by DocOps; sanitized receipt references are the evidence pointers.

| Receipt | Observation and boundary |
| --- | --- |
| `d59e49` | Target Java17 resolver5 checks PASS and no-auth TLS1 PASS; separate prerequisite, not SMTP delivery by itself |
| `81add4`, `ddfcdd` | Preservation PASS, independent preserved inventory/cold/JAR verification; older failed records preserved |
| `e61bb2`, `4a2beb` | Public staged; current source643 PASS, retained JAR unchanged |
| `aa4de5`, `5d0251`, `4dfd37`, `a019d6`, `8683f3` | Fresh Release, preflight PASS, producer, Start, then actual OPEN |
| Human flow / `c0f538` | User entered signup password/consents and followed verification link; ledger SIGNUP/VERIFY observed; no values retained |
| Browser / `469fc6` | Logged-in nickname displayed, value omitted. Ledger: CAPABILITIES, AVAILABILITY x3, SIGNUP, VERIFY, CAPABILITIES, LOGIN, ME, LOGOUT, CAPABILITIES x2, RESET_LINK |
| Human confirmation / `2d1926` | Reset email received; reset link not opened and password not changed. HumanReceipt recorded; this is not password-reset execution |
| `d8876f`, `a16753` | Finish, final Status; receipt `wi067-aed5d4a6c79349378c6cab88d3471c0e` |
| `642988` | Final BOUNDED_AUTH_FLOW_OBSERVED, JAVA_RELEASED, cleanup PASS; successful result uses `cleanup_steps` |
| `6cbdc6` | Independent restore/configuration pins/service PID0/console agetty checks PASS; cold state and quarantine restored |
| `3d99f4` | Owned SSH key removed, no identities, agent Stopped/Disabled |

MA read-only diagnostics `caf2ff` called a nonexistent module method; `6dd489`
queried an incorrect success-result key (`steps`). Corrected query `6cbdc6` PASS
uses the actual result shape, including `cleanup_steps`. These diagnostic errors
caused no mutation/restart and do not relabel the successful trial or past failures.

## Local Review And Validation

[WI007 r1 review](WI-20261021-ATS-007-evidence-pack.md) supersedes its original v8
HOLD, without deleting that finding. Local PG focused6/binding72/delivery
PowerShell17/producer PowerShell8 checks passed; manifest verifiers and
CheckPublic187 passed. WI008 transition review recorded14 Python/5 PowerShell
tests PASS. These are attributed local results, not DocOps reruns or native mail proof.

- [x] Current index and REQ checkpoint added; historical bodies preserved.
- [x] Native/human completion, exact pins and access cleanup separated from local review.
- [x] Reset-link use/password change excluded; remaining scope and privacy follow-up stated.
- [x] Focused owned-file documentation validation complete:4 files,294 relative links, required metadata fields and whitespace/terminal newline PASS. Scoped diff exit0; index CRLF-to-LF notice only. Both existing historical tails unchanged after line-ending normalization.

Validation scope: four owned files only. Check metadata, relative paths, whitespace
and historical-tail equality, plus scoped `git diff --check`. New files require
direct content checks because an unstaged diff alone omits untracked files.
No product tests, native test rerun, full-repository audit, full YAML/anchor
validation or blanket privacy claim. These are direct scoped checks, not a claim
that the repository-wide bundled validator or historical links all pass.

## Remaining Scope And Safety

Gmail is the real SMTP sender and the designated Naver mailbox is the recipient.
Paid Naver Cloud mail was not provisioned. No public release or new purchase.
Album/Track404 within narrow auth routing does not establish those features'
failure or success. Streaming/Official Download, provider migration and overall
production readiness remain outside this bounded authentication acceptance.

MA observed a password-like suffix in recovery-console login-name input.
Credential rotation is recommended, not performed. No input value, screenshot,
recipient, password, token-bearing link or private file was read/copied by DocOps.

WI006/WI007/WI008 completion -> WI009 documentation -> MA remaining REQ007 gates.
No further runtime work is delegated by WI009 and REQ007 is not closed. Future
authorized work must use current state and exact reviewed pointers, not consumed
Release/Start commands. Reuse the WI006 probe and WI007 review evidence instead of
repeating format discovery; coordinate human readiness and one authorized access
session, avoiding early repeated unlock requests.

## References And Rollback

Retained injected Tier0: STD-001 core-principles, STD-004 documentation-standards,
STD-002 development-standards, STD-005 glossary; Tier1 security-policy. Existing
WI009 handoff, evidence-pack/validate-docs skills and required context were read.
Only current additive index/REQ progress and own evidence/summary edited with
apply_patch. No source/build change, secret access, remote calls, commit or push.
Rollback is limited to this WI's additions/metadata via reviewed apply_patch,
preserving unrelated changes and every historical failure/freeze.

## Related Documents

- [WI009 handoff](WI-20261021-ATS-009-handoff.md)
- [WI006 implementation and probe evidence](WI-20261021-ATS-006-evidence-pack.md)
- [WI008 preservation evidence](WI-20261021-ATS-008-evidence-pack.md)
- [REQ007 current progress](../user/REQ-20261011-ATS-007.md)
- [User summary](../user/WI-20261021-ATS-009-summary.md)
