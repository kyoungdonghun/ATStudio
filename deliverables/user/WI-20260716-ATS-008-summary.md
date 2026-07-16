# WI-20260716-ATS-008 Summary

## Behavior Changes

- Company certification apply, resubmit, and review mutations now follow a deterministic owning-user then certification-row lock order. The certification aggregate also has an optimistic version fence.
- Duplicate open applications and concurrent review/resubmission overwrites are blocked under the cooperating-write contract.
- `REVISION_REQUESTED` and `REJECTED` require a trimmed applicant-visible reason of at most 500 characters. `APPROVED` may omit its note; an optional note has the same bound.
- ADMIN review and guarded document access create narrow audit records. A document access event means authorization and private-resource resolution succeeded, not that byte streaming completed.
- Certification responses no longer expose `documentPath` or individual stored paths.

## File and Frontend Contract

- Backend and frontend accept PDF/JPG/JPEG/PNG, at most 10 files, 20 MiB per file, 50 MiB in aggregate, and 255 characters per filename. Null/empty files are rejected, including across consecutive frontend selections.
- Backend extension, signature, and compatible MIME checks remain authoritative. PNG input is verified as `image/png`, then decoded and stored as canonical JPEG with `image/jpeg`; PDF remains `application/pdf`.
- `/company-certification/apply` and `/company-certification/status` require an authenticated USER with `userType=BUSINESS`. INDIVIDUAL and ADMIN users receive the existing access-denied redirect UX.
- The admin review modal requires reasons for revision/rejection, keeps approval notes optional, and displays the 500-character bound.

## Schema and Documentation

- Fresh schema includes certification optimistic versioning and `company_certification_audit_logs`.
- `src/main/resources/db/manual/20260716_company_certification_integrity_and_audit.sql` is a source-only retained-MySQL patch. It was not executed.
- The DB specification is aligned to the actual fresh schema count of 41 tables. The API endpoint count remains 148 because WI-008 adds no endpoint.
- API, DB, use-case, security, and screen-flow documents now describe the route, review, audit, response-minimization, and file contracts.

## Verification

| Check | Result |
|---|---|
| Focused company-certification backend tests | PASS, 12 suites / 73 tests / 0 failures / 0 errors / 0 skipped; `BUILD SUCCESSFUL` with `--rerun-tasks` |
| Focused frontend Vitest | PASS, 6 files / 30 tests |
| Frontend typecheck | PASS |
| Impacted frontend ESLint | PASS, zero warnings allowed |
| Frontend production build | PASS, 261 modules transformed |
| Changed-file Prettier | PASS |
| Documentation validator | PASS, Tier 0, links, 401 traceability IDs, and index coverage |
| `git diff --check` | PASS; only repository line-ending conversion warnings were emitted |

## Remaining Conditions

- Retained-MySQL patch application, copied-DB rehearsal, backfill review, and Hibernate validation are `ENVIRONMENT-CONDITIONAL`.
- External malware scanning and certification-document retention/deletion duration remain `POLICY-PENDING`. Signature/MIME/canonicalization checks are not malware scanning.
- No DDL, retained-DB mutation, user-data mutation, private-file inspection, server restart, or public-demo operation was performed.
- The client-demo worktree/branch and Cloudflare runtime were not modified or restarted.

Broader release verification remains assigned to the later QA/integration/security WIs. Existing WI-004 through WI-009 changes in shared files were preserved.
