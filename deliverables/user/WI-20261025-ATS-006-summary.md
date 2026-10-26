---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: PG
category: summary
status: completed
---
# WI-20261025-ATS-006: Independent preparation review

Independent local review is complete. WI004 provider/client drafts, WI005 operating
preparation and corrected WI008 optional mail candidate have no remaining blocking
finding within the reviewed local scope. This is not stage-two completion or
production/activation approval.

- Found and resolved one NCP compatibility defect: completed HTML containing
  literal `${key}` could be interpreted as an undefined provider parameter.
  The NCP boundary now preserves the literal HTML through a character reference;
  plain-text titles containing that syntax fail before submission. SMTP stays
  unchanged and remains the default.
- Rechecked fixed endpoint/signature, accepted-versus-delivered meaning,
  bounded response/deadline and absence of application retry/SMTP fallback.
  Native HTTP delivery and provider identity were not tested.
- Confirmed scheduler suppression does not suppress manual billing or after-commit
  billing-key cleanup. WI005 records these limits and preserves the existing
  Gmail configuration, key ring and original release.
- Confirmed the operating manifest changed only its application.yml source pin
  after the five optional mail settings were added. Original manifest/PASS remain.
- Reused the author's 16 passing changed transport cases and 26 prior passing
  unchanged cases. Independently verified their XML hashes, seven source pins,
  new/old JAR hashes and the read-only operating checker; no suite replay.

Reviewed replacement JAR SHA256:
`23a312c331d10d2875308ddea7e8034fb6e13ea48291027064565e833dc80d15`.
The initial mail candidate and historical validated JAR are retained.

The combined client signup/contract drafts remain unsent. Actual NCP pricing,
provider account/sender/domain/credentials, Toss automatic-billing contract and
merchant conditions remain unresolved. Public policy/operator facts, linked
customer policy screens and restricted review access are separate unfinished
work; no legal or merchant approval is claimed. Complete those boundaries before
the relevant activation, without redoing already completed account/grant work.

[Detailed review](../agent/WI-20261025-ATS-006-evidence-pack.md) ·
[Local receipt](../../build/WI-20261025-ATS-006/review-result.json)

No server, private-body, provider-send, payment, enrollment, publication or Git
operation was performed by this review. WI007 can integrate the current checkpoint.
