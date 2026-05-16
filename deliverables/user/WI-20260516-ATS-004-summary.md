# WI-20260516-ATS-004 Summary

Frontend and backend payment contracts were aligned.

- Frontend request/response types match backend DTO fields.
- Subscribe payment page uses `SUBSCRIBE`.
- Upgrade manage page uses `UPGRADE`.
- Cancel/fail UI sends `FAILED` or `CANCELLED`.

Verification:
- `npm run typecheck` passed.
- `./gradlew.bat test` passed.
