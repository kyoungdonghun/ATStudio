# Grant-date proposal only

No business DML is included in this candidate. No target date or mutation is approved by the local-preparation request.

Observed basis: WI010's safe receipt has grant1/user3/plan1 quota5, MONTHLY/CANCELLED, `started_at=expires_at=2026-10-24`, both pending fields NULL. If the approved trial remains on October 24 KST, no date change is needed. If execution moves to a new day D, halt and obtain a precise new date/state approval; do not silently extend at midnight.

The repository's `findActiveByUser(LocalDate)` filters ACTIVE/CANCELLED and `expires_at >= today`; it does **not** filter `started_at`. Setting both dates to D therefore does not prevent access before D or enforce one-day activation. The minimal future proposal preserves `started_at` and changes only `expires_at` to the explicitly approved D, with updated_at and one truthful audit record; IDs, plan/cycle/status/pending and every other existing row/cell remain protected. Changing start date requires a separate exact choice, and still supplies no activation barrier. Bounded use on D comes from a separately reviewed application/test execution gate that halts outside D, not from these database dates. No app code change or such execution gate is introduced here.

Existing product correction behavior provides a reference, not authority to simulate it: `UserSubscription.applyEntitlementCorrection` changes plan/cycle/status/expiry and optional pending fields, but not start date; `AdminSubscriptionCorrectionService.executeCorrection` locks/rechecks before-state and creates a successful audit through `AdminOperationAuditService.recordUserSubscriptionCorrectionSuccess`. The full product route also uses `admin_subscription_corrections` and approval actors. A direct fixture maintenance transaction must not invent a completed product approval/event. The future reviewed design must specify its true operator attribution, before/after dates and reason, and whether it uses that full product workflow or a separately approved nonbilling fixture transaction.

Before any such future transaction: re-observe the exact row/plan and billing state, authorize the selected D and exact audit effects, protect all ID1 fields and every unrelated row, lock/check the exact preimage, use one transaction with full row-delta verification and durable commit-intent evidence, and treat lost commit acknowledgment as HOLD_RECONCILE. No payment/agreement/consent/mail record may be fabricated. Recheck KST day before commit and before the later trial.

Backup ordering must match the chosen recovery objective. This adapter accepts an explicitly approved expected audit count of 1 (current observation) or 2 (one separately completed correction) and the exact expected grant dates. It performs neither correction. If a product correction adds its own row, the complete 43-table dump captures that row too. Other changes or unexpected count/state drift HOLD. A pre-correction backup and a post-correction backup are different recovery points; do not claim one covers the other.

Source pointers:

- `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:26`
- `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java:137`
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:265`
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java:61`
- `build/WI20261024-readonly-evidence-native/resume-inventory-600-public.json`
