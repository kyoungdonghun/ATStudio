---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# WI006 Local Repair Summary

v8 aligns Java SMTP resolution with the existing bounded firewall IPv4 snapshot,
without changing the application JAR, system hosts, hostname TLS identity, public
access or mail budget. Pure preflight failures leave quarantine untouched only
when cold state is proven. Unknown/partial states retain normal safety cleanup.

Actual local Java17 resolution and Spring/Angus TLS negative tests passed, together
with runtime/transport regression checks. Existing v7 effective JSON already
required TLS and hostname verification; v8 makes those same settings explicit at
CLI priority. No production credentials, server access or real mail used.

Frozen package pins, test receipts and the native handoff are in the WI006 Evidence
Pack. Independent PG review, Ubuntu Java17 proof and no-auth endpoint TLS proof
remain before a fresh bounded signup trial. Signup/mail are not yet accepted.
