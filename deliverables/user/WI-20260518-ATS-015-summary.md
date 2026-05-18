# WI-20260518-ATS-015 Summary

- Captured sensitive-data boundaries for payment UX and operations design.
- Ordinary users must not see raw `authKey`, `customerKey`, `billingKey`, Toss secret key, or raw provider payload.
- Operators may see only support-safe metadata such as internal `orderId`, provider, purpose, amount, sanitized failure code/message, masked method, and billing state.
- Billing keys remain encrypted server-side only.
