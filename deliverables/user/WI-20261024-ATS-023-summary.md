---
version: 1.0
last_updated: 2026-10-24
project: ATS
owner: SE
category: user
status: review_pending
---
# WI-20261024-ATS-023: Stage-two local preparation

The local tool is prepared for existing ID1 ADMIN promotion and buyer ID3's
grant1 expiry extension through 2026-11-24. ID2 ADMIN, ID3 USER, existing secrets,
sessions, plan prices and unrelated records are preserved. No new account or
account-password input is needed. Two truthful offline audit entries are added.

Eleven focused local tests and synthetic payload compilation passed. Independent
review and actual server application remain pending. No server was contacted.

The tool reuses the successful cold DB lifecycle, snapshots every known table,
stores only the four reversible old values privately, and reconciles uncertain
commit outcomes through read-only comparison. The historical backup is not
misrepresented as a current full backup. The MA execution recipe is in
`build/WI20261024-023-stage-two/README.md`; evidence is in the corresponding
WI023 Evidence Pack.
