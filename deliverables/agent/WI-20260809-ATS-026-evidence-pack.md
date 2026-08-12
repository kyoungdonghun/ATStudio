# Evidence Pack: WI-20260809-ATS-026

## Summary

- Completed the WI-026 source and read-only guard audit for USER/BUSINESS/ADMIN Whitelist and Company Certification workflows. Product/runtime behavior remains unchanged.
- Baseline: `e343c20`. Branch: `codex/v1-release-rehearsal-fixes`.
- Findings: 12 confirmed independent findings: `P1=2`, `P2=8`, `P3=2`.

## Scope / DoD Check

| Scope item                                    | Outcome   | Evidence                                                                                                                                                         |
| --------------------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `G-BUS`                                       | `FAIL`    | F10: existing-status lookup error can leave the application form active; F11: status load error has no retry. This does not mean every BUSINESS behavior failed. |
| `MEM-12`                                      | `FAIL`    | F01-F06: confirmed Whitelist UI/backend contract defects.                                                                                                        |
| `MEM-13`                                      | `FAIL`    | F10 affects the company-certification application precondition/error path. Other file-validation and storage lanes remain blocked/not inspected.                 |
| `MEM-14`                                      | `FAIL`    | F11 affects the certification status/recovery path. Replacement identity, document metadata, storage mutation, audit, and reload agreement remain blocked.       |
| `ADM-06`                                      | `FAIL`    | F11: admin list/detail load errors have no retry; F12: loading/legacy wording issues. Other admin review/document lanes remain blocked.                          |
| `ADM-11`                                      | `FAIL`    | F07-F09: export scope confirmation, stale failed reload rows, and note-length contract defects.                                                                  |
| Anonymous guard sublanes                      | `PASS`    | Five exact routes below redirected after async guard settlement.                                                                                                 |
| Authenticated USER/BUSINESS/ADMIN variants    | `BLOCKED` | No authorized session or safe fixture.                                                                                                                           |
| Responsive/live mutation/durable-state checks | `BLOCKED` | No authorized fixture and no approved side effect.                                                                                                               |

The six row results are `FAIL` because each has at least one confirmed defect; they do not assert that every behavior in a row failed.

## Evidence Index

| ID  | Severity | Row                                             | Confirmed evidence                                                                                                                                                                                                                                                                 |
| --- | -------- | ----------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| F01 | P2       | `MEM-12` / `WL-004`                             | `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:253-270,453-461,470-482`; `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:187-189`. `REMOVAL_REQUESTED` is shown as delete/deleted with success copy while backend preserves the row as a no-op. |
| F02 | P2       | `MEM-12` / `WL-006`                             | `WhitelistChannelPage.tsx:423-432`; `WhitelistChannelService.java:160-162`; `WhitelistChannel.java:133-135`. Primary action is exposed for backend-ineligible states.                                                                                                              |
| F03 | P2       | `MEM-12` / `WL-001, WL-003`                     | `frontend/src/utils/validation.ts:52-56`; `WhitelistChannelPage.tsx:153-166`; `WhitelistChannelService.java:205-221`; `docs/design/usecase/whitelist.md:38-50,94-98`. Frontend accepts broad HTTP(S), backend requires HTTPS YouTube.                                              |
| F04 | P3       | `MEM-12` / `WL-001, WL-003`                     | `WhitelistChannelPage.tsx:356-362`; `validation.ts:52-56`; `src/main/java/com/atstudio/atstudio/dto/whitelist/WhitelistChannelRequest.java:6-10`. Server DTO rejects `channelUrl` over 255; UI has no limit.                                                                       |
| F05 | P2       | `MEM-12` / `WL-003`                             | `WhitelistChannelPage.tsx:183-199,448-451`; `WhitelistChannelService.java:102-104,265-269`; `WhitelistChannel.java:87-93`; `docs/design/usecase/whitelist.md:94-108`. Processed/revision edits requeue with generic save copy.                                                     |
| F06 | P2       | `MEM-12` / `WL-003, WL-005`                     | `WhitelistChannelPage.tsx:51-55,434-446`; `WhitelistChannelService.java:121-135,265-269`. “수정 후 재요청” can direct-requeue without demonstrated correction while edit already requeues; semantics are ambiguous/redundant and the label is not enforced.                        |
| F07 | P1       | `ADM-11` / `WL-007, WL-008`                     | `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:70-73,122-125,176-202`. Export uses applied keyword, which can differ from draft, while confirmation omits exact scope.                                                                                                   |
| F08 | P1       | `ADM-11`                                        | `WhitelistChannelManagePage.tsx:83-113,302-418`. Failed reload leaves old actionable rows under changed controls.                                                                                                                                                                  |
| F09 | P2       | `ADM-11` / `WL-007`                             | `WhitelistChannelManagePage.tsx:390-402`; `src/main/java/com/atstudio/atstudio/dto/whitelist/AdminWhitelistChannelStatusRequest.java:7-10`. UI lacks 500-character limit/guidance; server DTO establishes late rejection.                                                          |
| F10 | P2       | `G-BUS` / `CC-001`                              | `frontend/src/pages/subscriber/CompanyCertApplyPage.tsx:58-91,190+`. Non-403/non-404 existing-status failure sets an error but does not suppress the active form.                                                                                                                  |
| F11 | P2       | `G-BUS` / `CC-003`; `ADM-06` / `CC-004, CC-005` | `frontend/src/pages/subscriber/CompanyCertStatusPage.tsx:48-72,125-133`; `frontend/src/pages/admin/CompanyCertManagePage.tsx:100-129,138-155,229-243`. Load errors have no retry action.                                                                                           |
| F12 | P3       | `G-BUS`, `ADM-06`, `ADM-11`                     | `CompanyCertStatusPage.tsx:117-121`; `CompanyCertManagePage.tsx:229-233,350-355`; `WhitelistChannelManagePage.tsx:302-304`. Three `Loading...` copies and stale `이전 방식으로 저장된 신청` wording.                                                                               |

## Anonymous Guard Evidence

Read-only navigation was allowed. Each route was allowed to settle for 500ms after async guard activity. No authenticated API was invoked.

| Route                                      | Observed redirect                                                  | Result |
| ------------------------------------------ | ------------------------------------------------------------------ | ------ |
| `/whitelist-channels?from=audit`           | `/login?returnTo=%2Fwhitelist-channels%3Ffrom%3Daudit`             | `PASS` |
| `/company-certification/apply?from=audit`  | `/login?returnTo=%2Fcompany-certification%2Fapply%3Ffrom%3Daudit`  | `PASS` |
| `/company-certification/status?from=audit` | `/login?returnTo=%2Fcompany-certification%2Fstatus%3Ffrom%3Daudit` | `PASS` |
| `/admin/company-certifications?from=audit` | `/login?returnTo=%2Fadmin%2Fcompany-certifications%3Ffrom%3Daudit` | `PASS` |
| `/admin/whitelist-channels?from=audit`     | `/login?returnTo=%2Fadmin%2Fwhitelist-channels%3Ffrom%3Daudit`     | `PASS` |

Guard lanes: UI navigation and redirect were observed. Request invocation was limited to the guard/navigation path; no protected ADMIN or member API was observed. Server and durable-state lanes are `NOT INSPECTED` for these anonymous checks.

## Four-Lane Classification

| Lane                            | Classification                                                                                                 | Boundary                                                                                    |
| ------------------------------- | -------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| UI                              | `PASS` for the five anonymous redirects; `CONFIRMED` source observations for F01-F12                           | Source pointers and read-only guard outcomes only.                                          |
| Frontend request invocation     | `SOURCE-CONFIRMED` where findings cite API calls; live authenticated/mutation invocation `BLOCKED`             | No claim that a protected request succeeded.                                                |
| Server response                 | `SOURCE-CONFIRMED` for DTO/service rejection/no-op contracts cited by findings; live responses `NOT INSPECTED` | Source is not substituted for runtime response.                                             |
| Durable DB/storage/export state | `BLOCKED` / `NOT INSPECTED`                                                                                    | No DB, storage, audit, CSV bytes, provider, mail, payment, or download state was inspected. |

## Test and Quality Evidence

Results below were supplied for this closeout. This evidence-pack update did not rerun them.

| Check                    | Result | Notes                                                                                                  |
| ------------------------ | ------ | ------------------------------------------------------------------------------------------------------ |
| Targeted frontend tests  | `PASS` | 11 files, 57 tests, 4.20s.                                                                             |
| Targeted backend tests   | `PASS` | 27 XML suites, 176 tests; failures/errors/skipped `0`; `BUILD SUCCESSFUL` in 34s.                      |
| `npm run typecheck`      | `PASS` | Exit code `0`.                                                                                         |
| Targeted ESLint          | `PASS` | Exit code `0`.                                                                                         |
| Prettier write           | `PASS` | Exit code `0`; handoff unchanged 59ms, findings unchanged 35ms, evidence 23ms, summary 6ms.            |
| Prettier check           | `PASS` | Exit code `0`; all matched files use Prettier code style.                                              |
| Documentation validation | `PASS` | Exit code `0`; Tier 0, internal links, 538 traceability IDs, document index, and all validations pass. |
| `git diff --check`       | `PASS` | Exit code `0`; no output.                                                                              |

These checks do not cover authenticated variants, responsive viewports, live server responses, or durable state.

## Commands and Outputs

- Frontend working directory: `C:\Users\jm991\Desktop\project\ATStudio\frontend`.
- Prettier write command:
  - `npx prettier --write ../deliverables/agent/WI-20260809-ATS-026-handoff.md ../deliverables/agent/WI-20260809-ATS-026-findings.md ../deliverables/agent/WI-20260809-ATS-026-evidence-pack.md ../deliverables/user/WI-20260809-ATS-026-summary.md`
  - Exit `0`; handoff `59ms (unchanged)`, findings `35ms (unchanged)`, evidence pack `23ms`, summary `6ms`.
- Prettier check command:
  - `npx prettier --check ../deliverables/agent/WI-20260809-ATS-026-handoff.md ../deliverables/agent/WI-20260809-ATS-026-findings.md ../deliverables/agent/WI-20260809-ATS-026-evidence-pack.md ../deliverables/user/WI-20260809-ATS-026-summary.md`
  - Exit `0`; `All matched files use Prettier code style!`.
- Repository working directory: `C:\Users\jm991\Desktop\project\ATStudio`.
- Documentation validator command: `python .agents/skills/validate-docs/scripts/validate_docs.py`.
- Diff check command: `git diff --check`.

## Documentation Closeout Validation

- Prettier write: exit `0` for all four WI-026 documents; handoff and findings were unchanged.
- Prettier check: exit `0`; all four WI-026 documents matched Prettier style.
- Documentation validator: exit `0`; Tier 0 documents, internal links, all `538` traceability IDs, document index coverage, and all validations passed.
- `git diff --check`: exit `0` with no output.

## Browser and Runtime State

- Browser restored to `http://127.0.0.1:5173/`.
- Restored viewport: `1280x720`; scroll position: `0`.
- Dialogs: `0`; file inputs: `0`; no active download or upload.
- Screenshot inventory: `NONE`.
- No runtime mutation, download, upload, DB/storage/provider/mail/payment action, secret inspection, stage, or commit.
- Intentional ZIP `output/client-demo-screenshots-20260716-140514.zip` was preserved and uninspected.
- No tracked product diff was made.

## Reference Documents

The following references were specified by the WI handoff for traceability; this closeout relies on the existing handoff/findings and their source pointers.

| Tier       | Reference                                                                                                                                                                                                                      |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 0          | `docs/standards/core-principles.md`; `docs/standards/development-standards.md`                                                                                                                                                 |
| 1          | `docs/policies/security-policy.md`; `docs/policies/access-control-policy.md`; `docs/policies/quality-gates.md`                                                                                                                 |
| 2          | `docs/design/usecase/whitelist.md`; `docs/design/usecase/company-certification.md`; `docs/design/api-spec.md`; `docs/design/db-schema.md`; `docs/ui/screen-flow.md`; `docs/ui/atstudio-front-list.md`; `docs/ui/modal-list.md` |
| WI context | `deliverables/agent/WI-20260809-ATS-026-handoff.md`; `deliverables/agent/WI-20260809-ATS-026-findings.md`; WI-020 acceptance matrix/evidence pack as named by the handoff                                                      |

## Limitations and Rollback

- Authenticated role coverage, responsive checks at `1440x900`, `1024x768`, `390x844`, and `360x800`, loading/empty/populated/error/retry/race/focus runtime checks, mutation outcomes, private documents, CSV output, DB/storage/audit state, and browser screenshots are `BLOCKED`, `NOT INSPECTED`, or `NONE` as stated above.
- This WI produced documentation only. No product/runtime rollback is required. If separately approved, remove only the WI-026 evidence pack and summary created by this closeout; do not alter product files, the handoff/findings, prior WI artifacts, the intentional ZIP, secrets, or environment state.

## Next WI

- `WI-20260809-ATS-030` is listed as blocked by WI-026 in the handoff. This pack records the evidence needed for that dependency review; it does not perform the next WI.
