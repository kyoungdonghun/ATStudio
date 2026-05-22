# Evidence Pack: WI-20260521-ATS-007

## Summary
- Added subscription renewal failure email notifications.

## Scope / DoD Check
- [x] Renewal charge failure triggers safe user notification via existing email infrastructure.
- [x] Final failure copy differs from retry/grace-period copy.
- [x] Email copy does not include card numbers, billing keys, auth keys, customer keys, or provider secrets.

## Evidence Pointers
- `src/main/java/com/atstudio/atstudio/service/EmailService.java`
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java`
- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java`

## Validation
- `RecurringRenewalServiceTest` passed as part of the focused backend payment run.

## Risks / Rollback
- Email delivery still depends on environment SMTP settings and existing fallback behavior.
- Rollback by removing the renewal email call and helper method.
