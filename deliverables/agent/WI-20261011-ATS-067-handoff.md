---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved Gmail functional validation after bounded web success
  - path: WI-20261011-ATS-060-evidence-pack.md
    reason: Reviewed bounded runtime and restoration primitives
  - path: WI-20261011-ATS-059-evidence-pack.md
    reason: Mail and mutation boundaries
---
# WI-20261011-ATS-067
[WI HEADER]
WI ID: WI-20261011-ATS-067
REQ: REQ-20261011-ATS-007
Agent: se
Depends On: WI060 native web PASS; fresh reviewed artifact before release
Blocks: PG review and MA management-only Gmail/account functional window

[WI SUMMARY]
Why: Prepare a real executable account/mail window, not another unrestricted or read-only web clone.
Scope (in/out): Own build/WI067-auth-trial and two reports. Local implementation only. Reuse reviewed runtime supervision, admission, private logs and exact restoration patterns by explicit narrow adaptation. Freeze historical WI060. No source/frontend/provider changes, private files, server operation, mail send, account creation, purchasing or new dependencies. Initial window is account/mail only: audio and all background tasks remain OFF. Future audio window is separate.
DoD: Prepare finite management-only HTTPS app window for designated account signup, verification, login/logout and one password-reset-link request; never reset submission. At most one designated principal and two emails, no automated signup/consent/password-entry or fabricated verified flag. Fixed method/path/body/query bounds derived from current routes; no payment, role change, media, notice mutation, admin wildcard or arbitrary upstream route. Runtime app endpoint/root and exact JAR/SPA pins must be fresh and independently reviewed before native start. Mutations need an existing verified prechange restore boundary; no blind DB-equality assertion after expected writes.
Constraints/Forbidden: Do not inspect/copy real SMTP or app secrets. Require MA-only private references and bounded framed transfer; public prepare/test inputs are synthetic. Existing DB/JWT/keyring unchanged. No secret in argv/output/logs or local public artifact. No native execution, no weakening BPF equality/ingress. Claim possible released state before fallible grant diagnostics. Keep all final observations independent. No subprocess cleanup that deletes history.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Default locked and exact reviewed target/artifact/source/policy gates, finite lifetime and independent stop/restoration.
- [ ] All inbound remains current management source; original quarantine restoration is complete.
- [ ] Gmail direct SMTP destination/port restriction uses existing tools only; safe proposal before implementing new firewall writes.
- [ ] Provider TCP587 was separately approved/applied; app egress still denied. Known native nft and iptables binaries exist. Propose an isolated owned nft table scoped only to app UID and exact resolved Gmail IPv4 TCP587, retain localhost DB/DNS as explicitly bounded, no existing table/rule flush or modification. App BPF remains default-deny with explicit SMTP destinations. This design requires PG approval before native use.
- [ ] SMTP TLS required with hostname verification, finite timeouts, no debug or automatic retry; sender reference is private existing Gmail configuration read only by MA later.
- [ ] Before any mutation, inspect exact account absence and expected zero tokens/no existing work; bounded ledger validates only expected new account/consent/token/auth changes. Failure stops admission and preserves evidence/data.
Quality:
- [ ] Focused synthetic tests for leaks, duplicate/malformed config, unexpected routes/state, counter/budget exhaustion and each cleanup failure.
- [ ] Separate all missing human/secret/backup/artifact inputs from actual technical defects; report earliest concrete gate.
- [ ] No native-ready claim from local tests. No release without independent exact-byte review.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
Tier 2:
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/templates/wi-subagent-handoff-template.md
Context:
- deliverables/user/REQ-20261011-ATS-007.md
- deliverables/agent/WI-20261011-ATS-059-evidence-pack.md
- build/WI060-restricted-web/README.md
- build/WI060-restricted-web/runtime.py
- build/WI060-restricted-web/web_session.py
- build/WI060-restricted-web/web_policy.py
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
- frontend/src/pages/

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-067-summary.md
Agent: deliverables/agent/WI-20261011-ATS-067-evidence-pack.md
Skill-generated handoff: this file. No subdelegation.

[TRACEABILITY REQUIREMENTS]
Concrete executable preparation, narrowly justified adaptation, exact pins/tests/limits/rollback and next PG gate. User will personally create any new password and approve actual legal consents at action time; do not route around browser confirmation using API. ATS; rule source .claude/config/context-injection-rules.json; Tier0 first.

## 2026-10-17 PC Delivery Continuation

[WI HEADER]
WI ID / REQ / Agent: Existing WI067 / REQ007 / se, unchanged.
Depends On: Exact runtime and QA068 artifact; native release additionally requires backup/isolated-restore PASS and human readiness.
Blocks: PG071 delivery review and MA fixed public staging/private trial transport.

[WI SUMMARY]
Why: The reviewed runtime has no complete reviewed Windows delivery command; preserve its whole-tree freeze while supplying the missing operator adapter.
Scope (in/out): Additional owned local paths are build/WI067-pc-delivery and create-new build/WI067-pc-delivery-evidence-* directories. Existing runtime, quality freezes and earlier evidence remain unchanged. Finalize the existing small draft by reusing pinned WI069 SSH/ACL helpers; no new transport framework.
DoD: Separate inert-default local/public staging from release, private stdin and lifecycle actions; fixed target and public manifests, create-new/no overwrite, finite subprocesses, exact independently supplied freeze pin, sanitized receipts, synthetic tests and preserved old bytes.
Constraints/Forbidden: No agent remote access or private inputs. No app start, account mutation, Gmail, public release or purchases. New passwords/actual legal consent remain human-only. Do not manufacture readiness or PASS attestations. No unrelated source/UI/config changes.

[ACCEPTANCE CRITERIA]
- [ ] Public-only action verifies current source and fixed 39 tool, one JAR and 143 SPA files; cannot invoke services or read secrets.
- [ ] Release/private actions retain all runtime gates and binary framing; no argv/output credential exposure or automatic retry.
- [ ] Regression tests cover wrong target/pins, partial transfers, unsafe paths, deadlines, framing and cleanup; measured coverage claims identify their language/scope.
- [ ] Independent PG071 exact-byte review precedes any MA native use; backup and human gates remain separate.

[INPUT POINTERS]
Tier 0 and Tier 1: The injected documents above remain unchanged and mandatory.
Tier 2 / Context: .agents/skills/create-wi-handoff-packet/SKILL.md; .agents/skills/test/SKILL.md; .agents/skills/create-wi-evidence-pack/SKILL.md; build/WI067-auth-trial/binding/README.md; build/WI069-backup/invoke-bootstrap.ps1; build/WI067-pc-delivery/README.md; deliverables/agent/WI-20261011-ATS-068-artifact.json.

[OUTPUT CONTRACT]
Separate delivery freeze and reproducible test receipt; append results to existing WI067 Evidence Pack/summary, then immediately hand exact pins to PG071. This skill-based continuation preserves the original handoff and does not broaden the approved functional or native-access scope.
