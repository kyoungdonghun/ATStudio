---
version: 1.1
last_updated: 2026-10-25
project: ATS
owner: SE
category: evidence-pack
status: completed
---
# Evidence Pack: WI-20261025-ATS-008

## Summary / scope
Prepared optional NCP SENS MAIL v2 transport while retaining default SMTP. No new
dependencies, server/provider calls, secret access or deployment. Local DoD passed;
independent WI006 review and provider activation remain open.

## Reference documents
MA injected Tier0 language, smallest approved scope, evidence and environment-secret
rules; handoff lists core-principles/development/documentation/glossary and security
pointers. Actually read WI008 handoff, EmailService/tests, application.yml, build.gradle,
test/build-check/create-wi-evidence-pack skills. TR004 supplied current official
API request/signature/response contract; no additional provider call by SE.

## Changes / evidence
- `src/main/java/com/atstudio/atstudio/service/mail/NcpMailTransport.java`: opt-in fixed
  provider endpoint, startup config checks, HMAC, bounded request and body, sanitized
  outcome. Accepted is not delivered; no application resend/fallback.
- EmailService: injected selector at the existing send boundary only; templates,
  tokens, anti-enumeration and SMTP success/error behavior retained.
- application.yml: five optional environment-backed settings; SMTP default.
- New transport test and two EmailService integration-boundary cases. Two existing
  direct-constructor tests only receive the additional transport mock.
- [Usage/limits](../../build/WI-20261025-ATS-008/README.md),
  [source/JAR/test pins](../../build/WI-20261025-ATS-008/checks.json),
  [first compile failure](../../build/WI-20261025-ATS-008/attempt1-compile-constructor-failure.txt),
  [passing run](../../build/WI-20261025-ATS-008/attempt2-output.txt).

## Tests
Focused offline Gradle tests + isolated bootJar PASS: EmailService17, transport14,
AuthTokenSecurityIntegration6, PasswordResetConsumptionIntegration3 = 40 tests,
zero failures/errors/skips. No whole-suite replay. Original JAR SHA remains
`f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.
Initial candidate JAR `0ced47dc2fb6de2a01ad4984504a0fa23aa362c5c0cd0845a86c1a1e7debac3a` is preserved as candidate-v1-0ced47.jar.
WI006 identified NCP interpolation syntax in completed HTML. At the NCP boundary
`${` is encoded as `&#36;{`; plain-text titles with `${` fail before submission.
Two payload/no-network tests added; only changed NCP suite rerun: 16 PASS and
new isolated bootJar PASS. Other 26 source-unchanged tests reuse prior evidence.
Latest candidate JAR `23a312c331d10d2875308ddea7e8034fb6e13ea48291027064565e833dc80d15`.
See attempt3-interpolation-output.txt and checks.json; v1 checks/XML preserved.
Known existing deprecation/unchecked compiler warnings only. Owned tracked diff
whitespace check PASS. Build directory isolated, no old artifact overwritten.

## Risks / rollback
No sender/DNS/account/key validity, real delivery, production Spring boot or LIVE
PG claim. Provider request IDs are not persisted by EmailService; manual provider
history reconciliation is needed before ambiguous resend. No automatic fallback.
Rollback uses intact Gmail settings/default SMTP and original release; no DB restore.

## Follow-up chain
WI008 Blocks WI006 independent review, then WI007 current-entry integration.
Source/application.yml hash changed; WI005 must explicitly update its source pins
without overwriting prior validation evidence. Candidate remains NOT_DEPLOYED.
