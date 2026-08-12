# Reliability Re-Review: WI-20260809-ATS-032

## Findings

### P2 - The prepare API does not reject non-positive subscription IDs at the boundary

`BillingAgreementPrepareRequest` accepts any non-null `Long` for `subscriptionId` at
[`BillingAgreementPrepareRequest.java:10`](../../src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareRequest.java).
The checkout route correctly rejects non-positive IDs, but a direct authenticated
prepare request with `0` or a negative ID still reaches service lookup rather than
failing request validation. This is inconsistent with the approved positive exact-ID
contract and leaves the API boundary weaker than the route boundary. Add a positive-ID
constraint and controller tests for zero and negative IDs that prove the service is
not invoked.

### P2 - The claimed response-mismatch matrix is only partially evidenced

The runtime gate checks `expiresAt`, integer amount safety, and usable callback URLs
in [`SubscriptionPaymentPage.tsx:527`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx)-[`SubscriptionPaymentPage.tsx:565`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx), but the new negative matrix tests only blank URLs and numeric amount-direction mismatches
([`SubscriptionPaymentPage.test.tsx:443`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx)-[`SubscriptionPaymentPage.test.tsx:472`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx)). It does not exercise a missing or malformed `expiresAt`, non-numeric or unsafe amount, or a syntactically present but unusable URL (for example, a non-HTTP(S) or parse-invalid value). Those are explicit fail-closed conditions in the PG and QA-INTEG response contract. Add test-double cases for them and assert both disabled billing auth and no SDK invocation.

### P3 - Ordering test asserts an implementation detail instead of the required safety relation

The valid subscribe test requires exactly two consecutive
`findActiveByUser` invocations in its `InOrder` verification
([`BillingAgreementApplicationServiceTest.java:166`](../../src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java)). The second lookup is a current implementation detail of
`validateSubscriptionPreconditions`, not a WI-032 requirement. A safe refactor that
reuses the already-read subscription state would fail this test without weakening the
required ordering. Verify that validation lookup(s) precede agreement/order/Provider
effects without pinning their count, while retaining the existing no-side-effect
assertions for rejected paths.

## Resolution Re-Review

### P2 positive-ID boundary - RESOLVED

The request DTO now applies `@Positive` together with `@NotNull` to
`subscriptionId`
([`BillingAgreementPrepareRequest.java:10`](../../src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareRequest.java)).
The controller parameterized test sends both `0` and `-1`, requires HTTP 400, and
verifies that the application service has no interactions
([`PaymentControllerTest.java:91`](../../src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java)).
This closes the direct authenticated API path at the same positive-ID boundary as
the checkout route.

### P2 response-mismatch evidence - RESOLVED

The runtime gate now requires a parseable expiry and absolute HTTP(S) callback URLs
([`SubscriptionPaymentPage.tsx:545`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx),
[`SubscriptionPaymentPage.tsx:559`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx),
[`SubscriptionPaymentPage.tsx:563`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx)).
The negative matrix now includes non-number, fractional, unsafe, and negative
amounts; missing, blank, and malformed expiry; and blank, non-HTTP(S), and invalid
success/fail URLs
([`SubscriptionPaymentPage.test.tsx:448`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx),
[`SubscriptionPaymentPage.test.tsx:455`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx),
[`SubscriptionPaymentPage.test.tsx:471`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx)).
Every parameterized row proves that billing auth remains disabled and that neither
the Toss SDK loader nor `requestBillingAuth` is invoked
([`SubscriptionPaymentPage.test.tsx:487`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx)).

### P3 ordering fragility - RESOLVED

The valid subscribe ordering assertion now accepts one or more authoritative
subscription-state lookups with `atLeastOnce()` while retaining `InOrder` checks
that those lookups precede billing-agreement access/mutation, payment-order save,
and Provider preparation
([`BillingAgreementApplicationServiceTest.java:166`](../../src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java)).
It no longer pins the test to the current duplicate lookup count.

No remaining finding was identified in the three requested correction areas.

## Confirmed Bounded Behavior

- The service derives authoritative purpose from the service-enabled subscription
  state and compares the request purpose before agreement preparation, order save,
  or recurring Provider preparation
  ([`BillingAgreementApplicationService.java:112`](../../src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java)-[`BillingAgreementApplicationService.java:127`](../../src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java)).
- Exact ID/audience route checks occur before prepare, and checkout resolves by ID
  from the authenticated audience list. The validated prepare response gates
  payment-order state before billing authorization
  ([`SubscriptionPaymentPage.tsx:149`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx)-[`SubscriptionPaymentPage.tsx:208`](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx)).
- ACTIVE and mocked CANCELLED-grace re-registration remain represented, and the
  approved BUSINESS subscription path remains covered with the Provider test double
  ([`BillingAgreementApplicationServiceTest.java:404`](../../src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java)-[`BillingAgreementApplicationServiceTest.java:480`](../../src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java)).
- The callback/return-context change carries immutable plan ID and audience only;
  this diff does not add WI-033 idempotency behavior, WI-034 outcome recovery, or a
  policy/architecture change.

## Decision And Evidence Limits

**ACCEPTED. All three prior findings are resolved in the current bounded diff.**

The resolution re-review inspected the exact current DTO, controller test, checkout
runtime gate and negative matrix, and service ordering test changes. No tests were
run. No real Provider, SDK authorization, database, runtime, or callback recovery
behavior was exercised; acceptance is limited to the source and mock/interaction
evidence in the bounded diff.
