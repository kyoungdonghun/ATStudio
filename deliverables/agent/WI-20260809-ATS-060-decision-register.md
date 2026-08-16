---
version: 1.0
last_updated: 2026-08-16
project: ATS
owner: PG
category: agent
status: accepted
dependencies:
  - path: deliverables/agent/WI-20260809-ATS-060-handoff.md
    reason: Approved decision-only WI contract and exact decision input
  - path: deliverables/user/REQ-20260809-ATS-001.md
    reason: Approved parent REQ and schema-change approval boundary
  - path: deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md
    reason: Identity, privacy, session, navigation, and logout findings
---

# WI-20260809-ATS-060 Decision Register

> Purpose: Record the USER-approved identity, consent, session, return-target, and logout policies without changing product or external state.

## 1. Decision Authority And Boundary

- WI: `WI-20260809-ATS-060`
- Parent REQ: `REQ-20260809-ATS-001`, approved on 2026-08-09.
- Decision status: accepted USER policy for future implementation and verification.
- This record is decision-only. It changes no frontend, backend, API, schema, migration, test, runtime configuration, authentication state, database state, provider state, mail state, or Git state.
- A database structure change remains held until a later explicit schema-change approval under the parent REQ.

## 2. Approved Policies (Verbatim)

1. Signup requires separate Terms of Service and Privacy Collection/Use consent; marketing is optional and independent. Persist the authenticated user identity, policy type/version, and agreement timestamp; do not invent additional retention such as IP/device fingerprint.
2. An unverified account cannot receive or persist an authenticated application session. A correct credential result must lead only to email-verification guidance/resend flow; no protected/member capability.
3. Login return targets retain only same-application pathname and query; reject external/protocol-relative/backslash/control/API/uploads/auth-flow/hash values. Apply current role/user-type access rules after login; unsupported or inaccessible values fall back to slash.
4. Logout follows hybrid server-first: attempt POST /api/auth/logout; 204 and confirmed invalid/revoked session are success; any settled network/server failure still clears local session and navigates away with an explicit server-revocation-could-not-be-confirmed notice. Callers await settlement, prevent duplicates, and do not claim confirmed server revocation on failure.

## 3. Required Invariants And Notice Semantics

### 3.1 Consent

| Invariant | Required future behavior |
| --- | --- |
| Separate required consents | Terms of Service and Privacy Collection/Use are independently presented and independently required before signup completes. One cannot stand in for the other. |
| Optional marketing | Marketing is optional and independent. Declining or omitting it does not block signup or alter the required-consent result. |
| Minimum durable consent record | Each recorded agreement contains only the authenticated user identity, consent category, document version, and agreed timestamp. Consent category covers the approved Terms of Service, Privacy Collection/Use, and an affirmative optional marketing agreement when recorded. |
| Signup identity association | At unauthenticated signup, the server associates each required consent record with the newly created persistent user ID in the same transaction. In the approved policy, "authenticated user identity" means that durable account identity, never a pre-login `SecurityContext`, token, or `/users/me` identity. |
| Exact presented policy and agreement time | The consent category and immutable policy/document version identify the exact policy presented and accepted. The agreed timestamp is server-generated when acceptance is persisted in the signup transaction, never client supplied or rewritten by later reads or login. This selects no timestamp precision, time-zone representation, schema layout, or retention period. |
| Prohibited retention in scope | No IP address, device fingerprint, or comparable additional retention is selected by this decision. |
| Authority | Server-side validation and persistence are authoritative; UI state is not proof of agreement. |

### 3.2 Verification And Authenticated Session

| Condition | Server invariant | UI and local-state invariant |
| --- | --- | --- |
| Credentials identify an unverified account | Do not return or persist an authenticated application session, including access or refresh credentials. Do not grant protected/member capability. | Do not call authenticated-session persistence or protected navigation. Show only email-verification guidance and the resend flow. |
| Verified account succeeds | Authentication may proceed under existing authentication and authorization rules. | Persist and use an authenticated session only after the verified server result. |
| Protected request | Authentication/authorization remains server-authoritative; client route checks do not replace it. | No local role, user record, or requested return target may create capability. |

The unverified-account result is an expected bounded account state, not a credential-mismatch notice. Its client copy must be a fixed verification/resend notice and must not render arbitrary server text. This decision does not select an HTTP status or error-code identifier; it does require that the response be non-authenticated and contain no session credentials.

### 3.3 Login Return Target

| Invariant | Required future behavior |
| --- | --- |
| Accepted shape | Retain only the same-application pathname and query. |
| Rejected shape | Reject external, protocol-relative, backslash, control-character, API, uploads, auth-flow, and hash values. |
| Authorization after authentication | Re-evaluate the requested target using the current authenticated role and user type after login. |
| Fallback | Unsupported, malformed, unauthorized, or inaccessible input navigates to `/`. |
| Notice semantics | Rejection is a safe fallback, not a successful authorization decision. Do not reflect the rejected value in a notice or request it as a resource. |

`returnTo` is untrusted navigation input. It is neither an authorization grant nor evidence that the destination should be fetched, preserved, or followed before canonical post-login identity is available.

### 3.4 Logout

| Settled outcome | Required server and client state | Notice semantics |
| --- | --- | --- |
| `204 No Content` | Confirmed terminal success. Clear local session after the server attempt settles and navigate away. | Do not show a server-revocation warning. |
| Logout-contract-confirmed already-invalid or revoked `401` session | Confirmed terminal success. Clear local session and navigate away. | Do not show a server-revocation warning. |
| Bare or otherwise unclassified `401`, or any other settled network/server failure | Local sign-out completes: clear local session and navigate away. Server revocation is unconfirmed. | Show an explicit warning that server revocation could not be confirmed. Do not present this as confirmed server revocation or as full logout success. |

All logout callers must await the single in-flight logout settlement, disable or otherwise fence duplicate invocation while it is pending, then apply the matching settled-outcome notice. Local session clearing is required in every settled outcome; confirmed server revocation is claimed only for `204 No Content` and a logout-contract-confirmed already-invalid or revoked `401` session.

## 4. Observed Current Implementation (Read-Only)

- The registration request, `User` entity, and schema have no consent category, document-version, or agreement-timestamp representation. The current schema retains `users.is_verified` and a hashed refresh-token field, but no consent record.
- Password login issues access and refresh credentials after credential authentication without an `isVerified` gate. The user-details and JWT authentication path likewise excludes deleted accounts but does not enforce verification. New social accounts also retain the default unverified state while the social login path issues credentials.
- The SPA login flow persists the returned credentials and user after a current-user read, including the user's `isVerified` value. Consequently, current behavior can persist an authenticated session for an unverified account.
- The current return-target utility already normalizes to same-application pathname plus query, rejects the listed unsafe classes, and rechecks ADMIN, USER checkout, and BUSINESS certification access before navigation. This is an observed implementation, not evidence that future server and UI invariants are complete.
- The auth store attempts server logout before clearing local state and exposes a boolean result. Its API helper treats every `401` as terminal without logout-contract classification. Header and Admin layout logout callers currently start logout and navigate without awaiting settlement or presenting the required unconfirmed-revocation warning.

## 5. Abuse And Privacy Analysis

| Surface | Risk without this decision | Binding containment |
| --- | --- | --- |
| Unverified login | Correct credentials could create durable browser credentials and protected/member access before email ownership is proven. This weakens verification and can expose account-linked data or actions. | Fail closed before token issuance or session persistence; present only verification guidance/resend. |
| Unverified refresh or existing credentials | A token lifecycle that does not re-check verification can preserve or restore an unauthorized authenticated state. | Future auth work must enforce the verified-session invariant at issuance, refresh, and protected-session restoration boundaries. |
| `returnTo` | An untrusted destination can become an open redirect, route confusion input, or a path into API, uploads, or auth loops. It can also attempt to exploit stale role assumptions. | Retain only the approved safe shape, treat it as data, and apply current role/user-type access rules after authentication. |
| Consent evidence | Extra telemetry would increase privacy exposure without being approved as necessary evidence. | Retain only identity, category, document version, and timestamp; do not add IP or device fingerprint in this scope. |

## 6. Future Follow-Ups (Not Implemented By This WI)

| Follow-up area | Assigned future owner | Required scope | Approval and dependency |
| --- | --- | --- | --- |
| DB schema | SA with PG review | Define the minimal durable consent representation for the four approved fields only. | Held for later explicit schema-change approval; no structure is selected or added here. |
| API and server auth | SE with PG review | Enforce separate required signup consents, minimal persistence, verified-only session issuance/refresh, and the bounded unverified response. | New implementation WI after the schema decision where persistence is required. |
| UI | SE with PG review | Render separate required consent controls, independent optional marketing, verification/resend-only unverified handling, and safe return fallback behavior. | New implementation WI; server result remains authoritative. |
| Auth and logout callers | SE with PG review | Preserve `204` and only a logout-contract-confirmed already-invalid or revoked `401` session as terminal success; await settlement, coalesce duplicate logout, clear local state in every settled outcome, and show the unconfirmed-revocation warning after a bare/unclassified `401` or any other settled failure. | New implementation WI; update every logout caller and associated auth-state contract together. |
| Documentation synchronization | DocOps | Synchronize authentication, Profile, and screen-flow documentation after verified implementation evidence. | `WI-072` is blocked by this decision and must distinguish current behavior from future policy. |
| Acceptance evidence | QA Integration with PG review | Verify verified/unverified, consent, return-target, logout success, and transient-failure state transitions without exposing tokens or PII. | `WI-077` remains blocked until the relevant implementation and quality gates complete. |

This decision blocks the corresponding corrections in `WI-042`, documentation synchronization in `WI-072`, and authenticated Profile/social-OAuth acceptance evidence in `WI-077` until the required future WIs are approved and complete.

## 7. Validation, Rollback, And External-Effect Boundary

### Validation

- Decision transcription check: the four policies in Section 2 are copied verbatim from the approved WI handoff.
- Documentation validation: run the repository documentation validator after this record is written.
- Whitespace validation: run `git diff --check` after this record is written.
- Runtime, browser, API, database, mail, provider, and authentication execution are out of scope for this decision-only WI.

### Rollback

No product or external state was changed, so no runtime rollback exists. A correction to this record requires a superseding approved REQ/WI decision; it must not be applied by changing runtime state or by silently extending retention.

### External-Effect Attestation

This WI performed read-only repository review and created this decision register only. It did not inspect secrets or protected output, execute browser/network/auth/mail/provider/database activity, stage/commit/push, or mutate product state.

## Related Documents

### Required References

- [WI-20260809-ATS-060 Handoff](WI-20260809-ATS-060-handoff.md): Exact approved policy input and decision-only boundary.
- [REQ-20260809-ATS-001](../user/REQ-20260809-ATS-001.md): Parent approval and schema-change gate.
- [WI-20260809-ATS-031 Consolidated Findings](WI-20260809-ATS-031-consolidated-findings.md): Identity/session finding traceability.
- [Security Policy](../../docs/policies/security-policy.md): Secret, PII, authentication, and fixed-message constraints.

### Implementation Context

- [P1 Security Acceptance Hardening Design](../../docs/design/p1-security-acceptance-hardening-design.md): Decision-held identity/session context.
- [Screen Flow](../../docs/ui/screen-flow.md): Authentication and navigation context.
