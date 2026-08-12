# Evidence Pack: WI-20260809-ATS-022

## Summary (one-liner)

- Completed the frozen authentication/account-entry audit, recorded 8 product findings, 3 documentation drifts, 2 review decisions, and 3 blocked evidence boundaries, and left product/runtime state unchanged.

## Scope / DoD Check

- DoD items:
  - [x] Classified every owned row (`AUTH-01` through `AUTH-06`, `MEM-04`) as `FAIL` or `BLOCKED` with no unclassified row.
  - [x] Separated frontend validation, capability/API behavior, backend enforcement, and persistence/session evidence.
  - [x] Recorded duplicate-submit behavior: Signup is fenced before availability calls; SocialCompleteProfile is not.
  - [x] Recorded responsive evidence at `1024x768` and `360x800` for six audited routes with no horizontal overflow.
  - [x] Kept valid/expired/reused mail links, authenticated mutation, and live provider OAuth explicitly blocked.
  - [x] Captured findings, documentation drift, review decisions, screenshots, capability response, focused-test result, and effect boundary.
  - [x] Applied no product fix and performed no mutation, cleanup, provider, mail, file, DB, payment, secret, or storage operation.

### Owned Row Classification

| Row                          | Result    | Executed evidence                                                                                                       | Remaining boundary / finding                                                                                                                 |
| ---------------------------- | --------- | ----------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| `AUTH-01` Login              | `BLOCKED` | Empty and malformed validation; capability state recorded.                                                              | Valid login and authenticated recovery require `B-UI-022-001`.                                                                               |
| `AUTH-02` Signup             | `FAIL`    | Individual/business modes, existing duplicate fixture, and pre-`Promise.all` loading fence verified.                    | `F-UI-022-002`, `F-UI-022-008`, `D-UI-022-001`, `D-UI-022-002`, `R-UI-022-001`, `R-UI-022-002`. No Signup duplicate-submit finding.          |
| `AUTH-03` Email verification | `BLOCKED` | Missing and invalid callback states.                                                                                    | Valid, expired, and reused links require `B-UI-022-002`.                                                                                     |
| `AUTH-04` Password reset     | `BLOCKED` | Request/reset empty, malformed, and invalid states; enumeration-safe request behavior.                                  | Valid, expired, and reused links require `B-UI-022-002`; guidance loss is `F-UI-022-006`.                                                    |
| `AUTH-05` Social login       | `BLOCKED` | Malformed and unknown callbacks fail closed.                                                                            | Live OAuth requires `B-UI-022-003`.                                                                                                          |
| `AUTH-06` Complete profile   | `FAIL`    | Anonymous access redirects safely; source-level pending and completed-profile behavior verified.                        | `F-UI-022-001`, `F-UI-022-005`; authenticated fixture coverage continues under `B-UI-022-001`.                                               |
| `MEM-04` Profile             | `BLOCKED` | Anonymous access redirects safely; source-level tab, subscription, password-error, and accessibility behavior verified. | Valid presentation/mutation/restoration requires `B-UI-022-001`; defects are `F-UI-022-003`, `F-UI-022-004`, `F-UI-022-007`, `F-UI-022-008`. |

Totals: `PASS 0`, `FAIL 2`, `BLOCKED 5`, `N/A 0`.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier       | Document                                            | Reason                                                   |
| ---------- | --------------------------------------------------- | -------------------------------------------------------- |
| Repository | `AGENTS.md`                                         | Role, language, scope, and deliverable rules             |
| 0          | `docs/standards/core-principles.md`                 | Constitution and traceability                            |
| 0          | `docs/standards/development-standards.md`           | Evidence and testing standards                           |
| 1          | `docs/policies/security-policy.md`                  | Auth capability, rate-limit, secret, and mail boundaries |
| 1          | `docs/policies/access-control-policy.md`            | Least privilege and effect classification                |
| 1          | `docs/policies/quality-gates.md`                    | Audit quality gates                                      |
| 1          | `docs/standards/frontend-standards.md`              | React routing, state, API, and form conventions          |
| Context    | `deliverables/user/REQ-20260809-ATS-001.md`         | Approved audit scope and constraints                     |
| Context    | `deliverables/agent/WI-20260809-ATS-022-handoff.md` | WI scope, acceptance, and output contract                |
| Skill      | `.agents/skills/create-wi-evidence-pack/SKILL.md`   | Evidence Pack structure                                  |

**Injection Rules Applied**:

- Assignee: `qa-fe`
- Task type: frontend audit / review / testing
- Required tiers present: Tier 0 and task-relevant Tier 1/2 pointers
- Pointer correction: removed nonexistent `frontend/src/api/users.ts`; corrected AuthService to `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java`.

## Evidence Pointers (required)

### Files Changed

- `deliverables/agent/WI-20260809-ATS-022-handoff.md`: corrected source pointers only.
- `deliverables/agent/WI-20260809-ATS-022-findings.md`: defect, drift, review, and blocker register.
- `deliverables/agent/WI-20260809-ATS-022-evidence-pack.md`: scenario and verification evidence.
- `deliverables/user/WI-20260809-ATS-022-summary.md`: user-facing completion report.

### Runtime and Fixture Preflight

- Baseline: `codex/v1-release-rehearsal-fixes` at `e343c20`.
- Capability `GET`: password login `true`; verification `true` with `REMOTE_SMTP`; reset `true`; test users `true`.
- Fixture boundary: existing duplicate-signup fixture was used; no uncontrolled identity was created.
- Valid authenticated mutation was not run because original/restorable Profile values were not available.

### Browser and API Evidence

- Login: empty and malformed input validation observed.
- Signup: individual/business modes and duplicate existing fixture observed. Loading is set before parallel availability checks, so no duplicate-submit finding applies to Signup.
- Email verification: missing and invalid callbacks observed without exposing tokens.
- Password reset: empty, malformed, and invalid states observed.
- Social callback: malformed and unknown provider inputs failed closed.
- Anonymous routing: Profile and `/complete-profile` redirected safely.
- Responsive: six audited routes at `1024x768` and `360x800` had no horizontal overflow.
- Forgot-password request: the browser client reached its rate bucket. No further browser calls were made. Direct-backend and Vite-proxy requests using nonexistent synthetic addresses returned `200`, preserving account-enumeration resistance.
- Screenshot pointers:
  - `output/ui-ux-audit/20260809/WI-022/AUTH-01_VD_login.png`
  - `output/ui-ux-audit/20260809/WI-022/AUTH-02_VM_signup.png`
  - `output/ui-ux-audit/20260809/WI-022/AUTH-03_VD_invalid-token.png`

### Findings Traceability

- Product findings: `F-UI-022-001` through `F-UI-022-008`.
- Documentation drift: `D-UI-022-001` through `D-UI-022-003`.
- Review decisions: `R-UI-022-001` through `R-UI-022-002`.
- Blocked evidence: `B-UI-022-001` through `B-UI-022-003`.
- Full rows: `deliverables/agent/WI-20260809-ATS-022-findings.md`.

## Commands & Outputs

- Runtime preflight verification:
  - `git branch --show-current` -> `codex/v1-release-rehearsal-fixes`
  - `git rev-parse --short HEAD` -> `e343c20`
- Deliverable closeout checks:
  - Targeted whitespace/EOF check across the four WI-022 deliverables -> `PASS`.
  - Targeted `rg` checks for required IDs, corrected pointers, blocked carry-forward, and WI-023 trigger -> `PASS`.
- No broad test, build, provider, mail, storage, or DB command was run during deliverable closeout.

## Tests

- Focused auth/account suite: `6` files / `58` tests -> `PASS` (verified audit input; not rerun during documentation closeout).
- Browser checks -> partial `PASS` within the row classifications above; blocked boundaries remain explicit.
- Responsive checks -> six routes at `1024x768` and `360x800`, no horizontal overflow.
- No coverage claim is made for this audit-only WI.

## Risks / Rollback

- Risks:
  - Authenticated Profile, successful auth recovery, controlled mail-link lifecycle, and live OAuth remain unverified and must not be inferred from source behavior.
  - `R-UI-022-001` and `R-UI-022-002` require user policy decisions before implementation or documentation can be made authoritative.
  - Capability discovery currently fails open in the UI, but backend policy enforcement prevents a security bypass.
- Effects and cleanup:
  - No mutation, secret inspection, browser-storage inspection, provider authorization, mail delivery, file transfer, DB operation, payment operation, or cleanup occurred.
- Rollback:
  - Product/runtime rollback: none required.
  - Documentation rollback: remove the three new WI-022 reports and revert only the two handoff pointer corrections.

## Follow-ups

- WI-022 is complete as an audit WI.
- Carry `B-UI-022-001` through `B-UI-022-003` and their affected rows to WI-030.
- Trigger WI-023 immediately; `WI-20260809-ATS-022` no longer blocks its start.
- Route `R-UI-022-001` and `R-UI-022-002` to the user decision gate before remediation.
