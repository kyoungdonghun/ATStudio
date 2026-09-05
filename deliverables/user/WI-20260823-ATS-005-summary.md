# WI-20260823-ATS-005 Summary

## Result

**Review result: remediation required.**

### P2 - BUSINESS requests can still persist an INDIVIDUAL-only `job`

The approved contract keeps `job` for `INDIVIDUAL` members only. However, a
direct BUSINESS request with a valid `companyName` and a non-null `job` passes
the request validators and is written by the service:

- `RegisterProfileValidator.java:19-32` and
  `CompleteProfileValidator.java:19-32` require a `job` only for
  `INDIVIDUAL`; they do not reject one for `BUSINESS`.
- `UserService.java:80-100` forwards a BUSINESS registration `job` into the
  new User, `UserService.java:235-244` forwards it during profile completion,
  and `UserService.java:136-143,491-503` accepts and applies it on profile
  update.
- `docs/design/api-spec.md:582-585` now describes the mutually exclusive
  profile contract, so the API implementation and current-state documentation
  disagree.

This is release-blocking for this REQ: UI omission alone cannot keep the
INDIVIDUAL-only policy true at the API boundary. Add rejection (or explicit
normalization to `null`, if that is the approved API behavior) in all three
write paths and tests for register, complete-profile, and update.

## Checks

- `git diff --check 3ea2781` passed with no whitespace diagnostics; Git only
  reported existing CRLF-to-LF warnings for touched Java/example files.
- `npm run typecheck`, `npm run lint`, the scoped WI frontend Prettier check,
  and `npm run build` passed.
- `npm run test -- --reporter=dot` finished with 110 passing files / 1 failed
  file and 1,446 passing tests / 1 failed test. The sole failure is the
  explicitly excluded `HomePage.test.tsx` exact-text matcher; no WI-005 source
  finding is assigned to it.

## Residual Validation Limits

- The final backend full suite was started but stopped at the user's request;
  it has no result in this WI. Documentation validation was not rerun.
- No browser/authenticated interaction was run: no login, signup submission,
  mutation, payment/refund/mail/provider call, or media playback occurred.
- The known development media/storage mismatch and the excluded HomePage test
  remain outside this REQ.
