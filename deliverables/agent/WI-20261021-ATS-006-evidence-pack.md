---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# Evidence Pack: WI-20261021-ATS-006

## Scope and References

Approved WI006 DNS-safe repair under REQ007; WI007 independent PG review blocks
native execution. Retained Tier0 core/development/documentation/glossary and
security-policy instructions. Handoff and prior WI004 evidence distinguish current
DNS reproduction from an unrecorded historical exception. Evidence-pack skill used.
Only new v8/supporting folders and this WI's records changed. No remote access,
production secrets, app main boot, DB, actual mail, source/JAR edit or purchase.

## Final Frozen Candidates

| Folder / freeze | SHA256 |
|---|---|
| build/WI067-auth-trial-v8/binding/freeze.json | 416f8ebc39204953e1748a4a9e12e3ab85ba89162a88c19e5f6e3460ecdc42eb |
| build/WI067-pc-delivery-v8/freeze.json | 01c8fba8985e5aa8ea587cc353259cdbf9a28603a868d2bd497c07ef4edbd6d9 |
| build/WI067-private-producer-v8/freeze.json | 4cd19f22b4353cdaa84cc099cd5253bc586f8869d3a4b12c89dcd6187e010cac |

Runtime freeze has42 members;43 installed tool files include the freeze itself.
Public manifest has187 files:144 original release assets plus43 tools. Source
ResolveProbe.java adds one audit/probe asset; it is not run at app startup. All
package references and Public source hashes checked. JAR/SPA and v7 are preserved.

## Implementation

- binding/native.py: one fresh release snapshot renders canonical root-owned0444
  smtp.hosts, same public IPv4 set as existing nft/BPF. Exact bytes/path/mode and
  expiry are checked before launch and while monitoring. JVM jdk.net.hosts.file
  prevents independent SMTP DNS rotation; smtp.gmail.com remains TLS identity.
- CLI STARTTLS enable/required and ssl.checkserveridentity all true. Correction:
  v7 already enforced these via contract.environment's SPRING_APPLICATION_JSON;
  tracked application.yml alone was insufficient evidence of effective settings.
- binding/controller.py: fixed allowlisted failure_code, arbitrary errors become
  UNCLASSIFIED. No error text/private values exported.
- restore: skip destructive cleanup only for proven unarmed PREFLIGHT/NOT_RUN,
  absence of claim/nonce/mutation markers/overlays/grant and successful cold
  verify_restore. Otherwise existing safety cleanup executes.
- Release transport now takes explicit ReviewedPGReceiptSHA256 for the real future
  PG receipt at build/WI071-review/pg-final-dns-v8.json. Runtime still validates
  exact approval status and candidate. No approval receipt was manufactured.

## Tests and Evidence

- Oracle Java17.0.12 Windows actual process: default SMTP resolver control PASS;
  two distinct maps in fresh JVMs PASS; multiIP exactness, localhost/numeric DB
  loopback and unknown host checks PASS. Missing file and existing file omitting
  the known-resolvable SMTP hostname both fail closed (no DNS fallback).
- MailProbe.java extracts actual pinned JAR libraries and YAML; SpringApplication
  loads only MailSenderAutoConfiguration with prod profile (not app/DB). Synthetic
  JSON disables TLS flags; exact launch CLI overrides them in real Session.
- Actual loopback fake SMTP: NO_TLS (9c48a4), UNTRUSTED (63168c), trusted wrong-host
  certificate (e92c58) all PASS, specific failure cause checked, before AUTH.
  No real SMTP server was contacted by these negative tests.
- Binding regressions72 PASS (67f806), original component boundaries51 PASS
  (4c35cd), DNS/cleanup focused tests5 PASS (9dae63).
- Delivery Python29 PASS (316879), producer Python12 PASS (93b08f).
- Delivery PowerShell17 PASS including full CheckPublic187 (bed223), synthetic
  Release framing5 PASS (b570b4), producer PowerShell8 PASS (ec76de).
- Exact runtime verification PASS (9bcb19); delivery and producer package/reference
  verification PASS (e1bc23). No coverage percentage claim.

Local preparation failures were corrected before freeze: TLS harness needed to
recognize Windows socket-close behavior and JSSE's SAN-mismatch error; synthetic
tests needed current expiry isolation; count propagation initially altered a hash
substring and was restricted to numeric token boundaries; root component freeze
must remain the original component-only manifest. No failed candidate ran remotely.

## Reproduction / Native Handoff

build/WI067-dns-v8-probe/README.md contains commands, limitations and transport
invocation delta. Native Ubuntu Java17 proof remains mandatory. Compile locally
with javac --release17; no target compiler/package installation required:
classes/ResolveProbe.class SHA49ea542146e84253a27e552e35456dcf21639d4e882286b2ff7eb668311abe2a.
Class-only local run PASS (8511cd). Class transfer is separately reviewed MA work.

After PG: MA first verifies resolver behavior on target Java17 and bounded no-auth
STARTTLS against the fresh Gmail endpoint with smtp.gmail.com/system CA. No AUTH,
MAIL or RCPT. Then fresh technical/human evidence and Release; no permanent IP pin.
Preserve current consumed attempt and old tooling with callbacks retired before
v8 Public. Never reuse an older fixed preservation operator for a new attempt.

## Limits and Recovery

Local candidate/tests complete; PG approval and native equivalence remain pending.
Do not restart for human trial until native prerequisites pass. Mapping loss,
tampering, nonpublic/IPv6 destinations, expiry and drift remain HOLD conditions.
No public-access widening. On failed native proof preserve records and use reviewed
cold/quarantine recovery; no automatic rollback over evidence or app replay.

## MA Native Continuation Addendum (2026-10-21)

Evidence owner: MA. The following are MA-reported native/browser observations,
not operations repeated or independently inspected by SE. Earlier pending notes
and failure records remain historical; this addendum does not relabel them.

| Evidence | Bounded observation |
|---|---|
| d59e49 | Actual Ubuntu Java 17 resolver checks: 5 PASS; no-auth TLS: 1 PASS. |
| e61bb2 | Public STAGED; receipt wi067-d6b04d74b6344c9d8d23a0489134dd1a. |
| 4a2beb | Source check 643 PASS, as reported by MA. |
| aa4de5 | Release STAGED; receipt 8277c82a4e05475f9742d37ab2c25916. |
| 5d0251 | LIVE_PREFLIGHT PASS. |
| 4dfd37 | Producer STAGED; receipt 228920e03de8444a9cf1a0b5482a2033. |
| a019d6 | START_REQUESTED. |
| 7fde55 / e01e94 | Early status ENTRY_GATE before readiness files existed; no attempt/result yet, services reported active. Not a final failure or completion result. |
| 8683f3 / 2fac6b | Subsequent status OPEN, then OPEN again. |

MA reports the actual signup form loaded in the browser and the test profile was
filled. A request for the user to enter password and consent was sent; this is
not evidence of consent submission, successful signup, or email receipt.

At this reported checkpoint app and DB were ACTIVE within the bounded trial;
cleanup had not occurred. Actual signup, mail receipt and final cleanup remain
PENDING. No whole-mail-workflow completion or stopped-server state is claimed.
No credential, token, private profile value or message URL is recorded here.

## MA Bounded Flow Addendum (2026-10-21, Subsequent Checkpoint)

MA evidence c0f538 reports ledger action names only: CAPABILITIES,
AVAILABILITY x3, SIGNUP, VERIFY. These entries establish observed actions, not
independent acceptance of each action's result.

The user reported that verification appeared complete. MA observed the email
verification page and subsequently prepared the login form, awaiting the user's
password entry. This is a user-reported verification indication, not an assertion
of independently verified mail delivery or completed login.

At this checkpoint v8r1 remained OPEN. Login, password reset and final cleanup
remain PENDING; no stopped app/DB state or whole-workflow completion is claimed.
SE performed no remote operation and recorded no secrets or verification URLs.

## Final MA Bounded Flow Addendum (2026-10-21)

Evidence owner: MA, with explicit user confirmation of reset-mail receipt.
This subsequent checkpoint supersedes the preceding PENDING flow and cleanup
states only within the bounded test scope. SE performed no remote operations.

| Evidence | Reported result |
|---|---|
| 469fc6 | Actions SIGNUP, VERIFY, LOGIN, ME, LOGOUT, RESET_LINK, plus capabilities and availability observations. |
| User confirmation | Reset email received; link remained unopened and no password change was performed. |
| 2d1926 | HumanReceipt STAGED. |
| d8876f | Finish requested. |
| a16753 / 642988 | BOUNDED_AUTH_FLOW_OBSERVED; cleanup PASS; app_start JAVA_RELEASED. |
| 6cbdc6 | Independent restoration and pin verification PASS; app/DB PID0; recovery console logged out. |
| 3d99f4 | SSH key removed; agent Stopped/Disabled. |

JAVA_RELEASED describes historical startup in this trial, not a still-running
app: the later independent checkpoint confirmed app and DB PID0. The bounded
Gmail signup/verification/login/session/logout/reset-mail-request flow and its
cleanup are observed complete. Reset-link consumption and password replacement
were not tested. This is not general public-release acceptance, Naver Cloud mail
integration acceptance, or acceptance of unrelated media/payment workflows.

WI009 DocOps owns current-entry/REQ reconciliation. Earlier failures and frozen
candidate records remain unchanged; no private values or mail links were added.
