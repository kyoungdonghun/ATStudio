---
version: 1.1
last_updated: 2026-10-25
project: ATS
owner: SE
category: reference
status: completed
---
# WI-20261025-ATS-008 Summary

Optional NCP MAIL transport is locally implemented and tested. SMTP remains the
default; no server change, provider send or paid action occurred. All 40 focused
tests and the isolated candidate JAR build passed. The validated old JAR is intact.

NCP activation still needs account, verified sender/DNS, protected API credentials,
price approval and bounded real delivery tests. HTTP202 is submission acceptance,
not inbox delivery. No automatic resend/SMTP fallback on uncertain outcome.
See [evidence](../agent/WI-20261025-ATS-008-evidence-pack.md) and
[configuration/limitations](../../build/WI-20261025-ATS-008/README.md).

Review correction: NCP body variable markers now preserve literal HTML rendering;
plain-text variable-marker titles fail before sending. Changed NCP suite16 and
new candidate build PASS; the prior26 tests are reused, not rerun.
