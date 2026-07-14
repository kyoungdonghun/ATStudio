[WI HEADER]
WI ID: WI-20260714-ATS-015
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-002, WI-20260714-ATS-003
Blocks: WI-20260714-ATS-016, WI-20260714-ATS-017, WI-20260714-ATS-020, WI-20260714-ATS-022

[WI SUMMARY]
Why: Create an explicit acceptance profile with external-secret and public-base-URL guards.
Scope: Acceptance configuration, public URL validation/derivation, Toss callback derivation, bootstrap profile/secret refusal, and focused startup/config tests.
Out: Starting a tunnel, using live credentials, changing production secrets, or exposing a public URL.
DoD: Acceptance mode requires HTTPS public base and external secrets; production/default refuses acceptance/bootstrap flags; callbacks share the approved origin and paths.
Constraints: Never commit secret values or defaults. Do not edit Vite/proxy behavior owned by WI-016. No live Toss/SMTP/DB calls.

[ACCEPTANCE CRITERIA]
- [ ] `application-acceptance.yml` or equivalent contains placeholders only and no secrets.
- [ ] One validated `APP_PUBLIC_BASE_URL` drives mail/social/Toss test callback URLs.
- [ ] Bootstrap requires acceptance/non-prod profile plus externally supplied password.
- [ ] Production-like startup guard tests fail closed without logging values.
- [ ] Focused tests, compile, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-security-acceptance-hardening-design.md; docs/design/p1-payment-db-integrity-design.md; docs/SR/SR-42.md
Files: src/main/resources/application.yml; acceptance profile resource; PaymentProperties; TestUserBootstrapRunner/Properties; related configuration tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-015-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-015-evidence-pack.md
Implementation ownership: backend acceptance/public URL/bootstrap configuration and focused tests only.

[TRACEABILITY REQUIREMENTS]
Evidence/commands/tests/rollback required; secret-redaction proof required.
