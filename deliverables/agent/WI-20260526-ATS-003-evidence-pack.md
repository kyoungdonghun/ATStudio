# Evidence Pack: WI-20260526-ATS-003

## Summary

- Reviewed settlement import security/privacy boundary.

## Scope / DoD Check

- [x] Admin-only access boundary confirmed.
- [x] Allowed support-safe settlement fields listed.
- [x] Forbidden fields listed.
- [x] Implementation avoids raw provider payload storage by using an allowlist payload.

## Reference Documents

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 1 | docs/policies/security-policy.md | Sensitive data boundary |
| 2 | docs/design/payment-settlement-import-design.md | Settlement design |
| 2 | deliverables/user/REQ-20260526-ATS-001.md | Approved scope |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java` - settlement endpoints use `@PreAuthorize("hasRole('ADMIN')")`.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java` - `sanitizedPayload` uses allowlisted settlement fields only.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementResponse.java` - admin response contains support-safe fields only.
- `docs/design/payment-settlement-import-design.md` - allowed/forbidden fields documented.

## Commands & Outputs

- No separate command was required for this review.
- Backend settlement tests were run in WI-20260526-ATS-002.

## Risks / Rollback

- Risk: operator-uploaded CSV can include extra sensitive columns. Current parser ignores unknown columns, and UI/docs must tell operators to use the approved template.
- Rollback: N/A, no code changes in this WI.
