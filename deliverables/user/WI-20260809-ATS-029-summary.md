# WI-20260809-ATS-029 Closeout Summary

## Outcome

The bounded source and existing-assertion audit is complete for Notice, Track/License/Download History, Question attachments, Company Certification documents, Whitelist CSV, Settlement CSV operations, and shared storage mutation/recovery boundaries.

- Defects: **16 total** - `P1` 8 and `P2` 8.
- Non-defect control finding: **A02** confirms that Question attachment access correctly follows documented public/private Question visibility.
- Detailed causes, impacts, and source/test pointers: `deliverables/agent/WI-20260809-ATS-029-findings.md:42-480`.
- Passing targeted suites do not close these findings or establish whole-row/live acceptance.

## Coverage Summary

| Area                                 | UI / frontend                                                   | Server / binary or parser                                                                                                         | Durable state                                                    | Result                                                              |
| ------------------------------------ | --------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------- |
| Notice and Question attachments      | Upload/download/delete call paths traced                        | Header, filename, byte, authorization, and validation boundaries inspected                                                        | Journal, preserve/delete, and cleanup assertions inspected       | Source/assertion complete; live files blocked                       |
| Track, License, and Download History | Named Blob entry points and pending behavior traced             | Entitlement and response contract inspected                                                                                       | License/history/count ordering and redownload behavior inspected | Source/assertion complete; live delivery blocked                    |
| Company Certification                | Subscriber/admin paths traced                                   | ADMIN-only private response and validation assertions inspected                                                                   | Private root and access-grant audit inspected                    | Source/assertion complete; private file blocked                     |
| Whitelist CSV                        | Applied-filter, confirmation, export, and replay paths traced   | UTF-8 BOM, required headers including `userEmail`, escaping, formula neutralization, filename, and deterministic replay inspected | Immutable snapshots, bounds, locks, and status effects inspected | Source/assertion complete; live CSV blocked                         |
| Settlement CSV                       | Import/reconcile/ignore pending, error, and reload paths traced | Multipart/API, decoder/parser, field validation, duplicate, and result contracts inspected                                        | Settlement row/audit/reconcile/ignore behavior inspected         | Source/assertion complete; live import and production audit blocked |
| Storage recovery                     | No user-facing action                                           | Mock/contract and isolated `@TempDir` paths inspected                                                                             | No combined H2 plus real-files restart proof                     | Partial evidence; live recovery blocked                             |

The evidence pack preserves the lane-by-lane crosswalk at `deliverables/agent/WI-20260809-ATS-029-evidence-pack.md`.

## Finding Summary

**P1**

- `A01`: Question backend attachment validation is absent; exact Notice/Question limits are not canonicalized.
- `A03`: first-download durable state can commit before HTTP body delivery completes.
- `B01-B02`: Whitelist keyword-only confirmation and unknown-response recovery are unsafe.
- `B03-B06`: Settlement partial-success feedback, IGNORE integrity, CSV grammar/encoding, and durable financial/provider validation are incomplete.

**P2**

- `A04-A05`: binary filename/byte checks and duplicate-request fencing differ across clients.
- `A06-A08`: combined storage recovery proof, private-file buffering, and download-all bounds are incomplete.
- `B07-B09`: Settlement atomic duplicate handling, reconciliation bounds, and outcome-count completeness are incomplete.

**Confirmed control**

- `A02`: public Question attachments are intentionally available to authenticated viewers who may view the public Question; private Questions remain author/admin restricted. This matches `user-question.md` and is not counted as a defect.

## Verification

| Check                    | Result                                                                 |
| ------------------------ | ---------------------------------------------------------------------- |
| Frontend targeted Vitest | PASS - 15 files, 159 tests, 0 failures/skips; Vitest 9.58s; wall 11.4s |
| Backend targeted Gradle  | PASS - 26 class filters; `BUILD SUCCESSFUL` in 54s; wall 55.4s         |
| Backend XML aggregate    | 38 suites, 278 tests, 0 failures, 0 errors, 1 skipped                  |
| TypeScript               | `npm run typecheck` PASS - 6.3s, no diagnostics                        |
| Targeted ESLint          | PASS - 3.1s, 0 warnings; exact command was not supplied by main        |

The skipped backend case was `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks` because symbolic links were unavailable in the environment. Non-failing backend warnings covered unchecked/unsafe operations, CDS boot-loader sharing, an incubating problems report, and a configuration-cache suggestion.

Main completed the following document-quality checks after the test evidence; product tests and browser actions were not rerun:

- Prettier write over the handoff, findings, evidence pack, and summary: exit 0; handoff unchanged in 48ms, findings 87ms, evidence 27ms, and summary 13ms.
- Prettier check over the same four files: exit 0; all matched files use Prettier code style.
- Docs validation: exit 0; Tier 0, internal links, 541 traceability IDs, document index, and all validations passed.
- `git diff --check`: exit 0; no output.

## Blocked Evidence

- No screenshots were captured or generated.
- No browser file chooser, upload, download, import, export, or authenticated/private-file action occurred.
- No live or production DB, storage, Provider, audit, mail, secret/session, or private/user file was accessed.
- No product source, runtime, test, config, schema, current product documentation, or git state was mutated by this closeout.
- `output/client-demo-screenshots-20260716-140514.zip` remained preserved, untouched, and uninspected; it was not opened, read, hashed, or metadata-probed.
- Targeted passing suites provide jsdom/mock/MockMvc/contract/isolated-`@TempDir` evidence, not completed browser bytes or production durable-state proof.

## Decisions Required

The audit does not silently select policy values. Product approval remains necessary for:

1. Exact Notice and Question attachment type, count, per-file, aggregate-size, filename, signature/MIME, and empty-part rules.
2. Whether a successful Track download means entitlement/resource grant or completed HTTP body delivery.
3. The canonical Settlement CSV filename/MIME/size/encoding/dialect contract.
4. Settlement amount notation/precision/scale, accepted currencies, and Provider identifier bounds.
5. The future accounting system, if any, that receives exported Settlement/accounting data.

ATStudio Settlement CSV export and pre-import preview are **not current requirements**. The current workflow imports Provider-exported CSV and shows a post-import summary; adding either capability requires a new approved requirement.

## Closeout Boundary

This WI produced documentation only. No product rollback or data cleanup is required. The final document checks are complete; preserve all blocked/live classifications and route the independent findings into follow-up work after the required policy decisions.
