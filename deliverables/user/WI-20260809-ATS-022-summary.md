# WI-20260809-ATS-022 Completion Summary

## Outcome

WI-022 is complete as a frozen authentication and account-entry audit. The seven owned rows are classified as `PASS 0`, `FAIL 2`, `BLOCKED 5`, and `N/A 0`. Partial checks inside blocked rows remain recorded rather than being promoted to full-row passes.

Baseline was `codex/v1-release-rehearsal-fixes` at `e343c20`. No product code, runtime configuration, database, branch/index/commit state, browser storage, secret, provider, mail, file, or payment state was changed.

## Coverage and Evidence

- Browser checks covered Login validation; Signup individual/business and duplicate-existing-fixture behavior; missing/invalid email verification; empty/malformed/invalid password reset; malformed/unknown social callbacks; and anonymous Profile/complete-profile redirects.
- Six audited routes at `1024x768` and `360x800` showed no horizontal overflow.
- Focused auth/account tests passed: `6` files / `58` tests.
- Public capabilities reported password login enabled, verification enabled with `REMOTE_SMTP`, password reset enabled, and test users enabled.
- Forgot-password requests remained enumeration-safe for nonexistent synthetic addresses. The browser client reached its rate bucket, so no further calls were made.
- Sanitized screenshots are under `output/ui-ux-audit/20260809/WI-022/`.

## Material Findings

- SocialCompleteProfile has a duplicate-submit window because network validation completes before `loading=true`. Signup does not have the same defect because it fences submission before its parallel availability checks.
- Capability discovery failure leaves Login, Signup, and PasswordReset enabled through `?? true`. Backend `PasswordLoginPolicy` still enforces the setting, so this is fail-open UX, not a security bypass.
- Profile can render blank for an invalid tab, conflates every subscription load error with no subscription, and drops useful password-update error guidance.
- Completed profiles are not route-guarded from `/complete-profile`, although the backend rejects completion.
- Forgot-password drops safe rate-limit/server guidance.
- Auth/account forms have label, live-region, and member-type selection semantic gaps.
- Documentation drifts at `INFO-001`, `INFO-002`, and `INFO-015` from implemented registration, business-field, and Profile feedback behavior.

## Decisions and Blockers

User decisions are required for the missing consent contract and the inconsistent unverified-login policy. The audit does not infer either policy.

The following evidence remains blocked and is carried to WI-030:

- Valid Login and authenticated Profile mutation/restoration.
- Valid, expired, and reused verification/reset mail links.
- Live social OAuth.

No cleanup or restoration was needed because no mutation occurred. WI-023 is now unblocked and ready to start.
