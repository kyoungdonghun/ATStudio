# Evidence Pack: WI-20260713-ATS-002

## Summary

- Defined the implementation, transaction, compatibility, and verification contracts for all three confirmed P0 findings.

## Scope / DoD Check

- [x] Protected-media routing and public/admin DTO boundaries defined.
- [x] Full-original stream fallback replaced by a bounded compatibility preview design.
- [x] Secret-free SMTP logging and generic external behavior defined.
- [x] Local-first withdrawal and after-commit Provider cleanup defined.
- [x] Durable deduplicated incident and daily cleanup retry defined without schema change.
- [x] Decision-to-test and rollback matrices documented.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Domain constitution |
| 0 | `docs/standards/development-standards.md` | Spring transaction and service boundaries |
| 1 | `docs/policies/security-policy.md` | Protected-resource and secret handling |
| 1 | `docs/policies/quality-gates.md` | Verification contract |
| 2 | `docs/design/api-spec.md` | Current API baseline |
| 2 | `docs/design/payment-integration-design.md` | Recurring payment baseline |
| 3 | `docs/audit/full-system-audit-20260713.md` | Confirmed findings |

## Evidence Pointers

- `docs/design/p0-release-blocker-remediation-design.md`: canonical remediation design.
- `src/main/java/com/atstudio/atstudio/config/WebConfig.java`: current broad `/uploads/**` mapping.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`: current static-resource authorization boundary.
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java`: current original storage-key exposure.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`: current complete-original stream fallback.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java`: current recipient/body fallback logging.
- `src/main/java/com/atstudio/atstudio/service/UserService.java`: current withdrawal flow without billing cancellation.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java`: current due-agreement query and charge boundary.
- `src/main/resources/schema.sql`: existing reconciliation issue-type enum; design intentionally reuses an existing value.

## Commands and Results

- Static source and documentation inspection only in this WI.
- No product source, database, stored file, Toss provider, or SMTP delivery was changed or invoked.
- The assigned design Subagent was stopped after repeated timeouts without output. MA completed the design from the approved REQ and verified audit evidence so the WI chain could continue.

## Risks / Rollback

- Bounded fallback preview is a compatibility control, not a replacement for a dedicated low-quality preview-generation pipeline.
- Existing original files remain under the current storage root; route denial is the immediate enforcement layer.
- Rollback removes the design and WI-002 artifacts only when explicitly requested.

## Follow-ups

- WI-003: protected track media implementation.
- WI-004: secret-free mail logging implementation.
- WI-005: withdrawal and recurring-billing stop implementation.
