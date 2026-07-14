[WI HEADER]
WI ID: WI-20260714-ATS-037
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-012, WI-20260714-ATS-019, WI-20260714-ATS-024
Blocks: WI-20260714-ATS-025, WI-20260714-ATS-027, WI-20260714-ATS-034

[WI SUMMARY]
Why: Close the WI-024 client-share blocker caused by same-origin public Question attachment storage and remove acceptance fixture email logging.
Scope: Store new Question attachments in PRIVATE storage, load/delete them from PRIVATE storage, deny legacy `/uploads/questions/**` static paths, return authorized downloads as forced attachments with hardened headers and safe filename handling, update focused tests, and replace bootstrap email logging with a non-identifying count/status message.
Out: Migrating/deleting legacy files, malware scanning, changing Question visibility policy, attachment type restrictions, schema changes, live DB/files, or broad upload refactoring.
DoD: New attachments cannot be fetched through `/uploads/**`; owner/admin API access still works; unauthorized users fail; active content is never inline-rendered; legacy public paths fail closed; bootstrap logs contain no account emails; focused service/controller/security tests pass.
Constraints: Do not migrate or delete existing files. Preserve Question response DTOs without exposing stored paths. Reuse Company Certification download-header patterns and the storage mutation journal. Do not modify unrelated runtime logs or concurrent payment work.

[ACCEPTANCE CRITERIA]
- [ ] `saveAttachments`, authorized load, and deletion use `StorageRoot.PRIVATE`.
- [ ] `/uploads/questions/**` is denied for anonymous, USER, and ADMIN, including encoded traversal variants where relevant.
- [ ] API download enforces existing owner/admin/public-question access policy and returns `Content-Disposition: attachment`, octet-stream, no-store, nosniff, sandbox, and no range support.
- [ ] Download filename cannot inject response headers and stored path is not returned.
- [ ] Bootstrap success log has no email/account identifier.
- [ ] Focused tests and scoped diff check pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-security-acceptance-hardening-design.md
- deliverables/agent/WI-20260714-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-012-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-019-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-024-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/QuestionService.java
- src/main/java/com/atstudio/atstudio/controller/QuestionController.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java
- src/main/java/com/atstudio/atstudio/entity/QuestionAttachment.java
- src/main/java/com/atstudio/atstudio/dto/question/
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java
- relevant security tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-037-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-037-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-037-handoff.md

[TRACEABILITY REQUIREMENTS]
Finding-to-code mapping, legacy/new-file behavior, authorization/header matrix, test commands/results, no-DB/no-file-migration proof, rollback, and residual risks are required.
