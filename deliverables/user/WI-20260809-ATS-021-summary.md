# WI-20260809-ATS-021 Summary

## Result

The first frozen-code browser sweep is complete with `11 PASS / 3 FAIL / 1
BLOCKED` scenario groups. No product code, database row, runtime setting, or
external provider/mail/file operation was changed.

## Confirmed Working Baseline

- The local and current Cloudflare Home/API responses matched exactly.
- Public Home, valid Notice list/detail, Korean 404/500 recovery pages, theme
  switching, mobile navigation, and representative public deep-link reloads
  worked.
- Anonymous protected, Checkout, BUSINESS, and ADMIN routes preserved safe
  Login return targets.
- Six representative pages had no horizontal overflow at 1024px or 360px.

## Findings

1. A missing Notice discards the localized backend error and shows only the
   English `Failed to load notice`, without retry or back recovery.
2. An anonymous subscriber-only deep link such as `/playlists` loses its
   destination when it redirects to Login.
3. The mobile menu cannot be closed with Escape.
4. The theme toggle's screen-reader label is English in the Korean UI.

These are recorded for later synthesis and correction; no fix was applied
during the initial frozen audit.

## Open Verification

The in-app browser accepted search text but did not reliably dispatch native
Enter or Tab behavior. The current Header component tests passed 27 focused
tests, so this was recorded as an automation limitation rather than a search
defect. A second input surface or physical-keyboard acceptance pass remains
required.

## Next Step

WI-022 can now audit authentication and account flows: Login, Signup, email
verification, password reset, safe return behavior, and Profile states. Mail
delivery and account mutations stay within the approved acceptance boundary.
