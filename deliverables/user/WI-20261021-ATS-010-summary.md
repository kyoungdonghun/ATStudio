---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# WI010 Local Audio Tool Preparation

REQ: [REQ007](REQ-20261011-ATS-007.md).
Evidence: [WI010](../agent/WI-20261021-ATS-010-evidence-pack.md).

The local component package now verifies retained identities, prepares a bounded
single-WAV trial and validates its expected requests, responses and data effects.
All 643 source inputs, the existing JAR, approved WAV and retained manifest member
checks match. Fifty local synthetic tests pass. No server was contacted and
no product source, JAR, account or database was changed.

The flow uses three distinct existing verified principals: ADMIN for upload and
activation, entitled USER for original download, and non-entitled USER for denial.
Existing accounts should be reused; their current target state is unverified.
Any role change, additional account or nonbilling entitlement fixture needs a
separate exact approval. No grant script or payment operation was executed.

Expected trial changes: one track, WAV plus MP3, four storage-journal records,
one USER license, one download-history record and download_count increment of one.
The two DELETE journal intents retain the still-referenced source WAV. Default
proposal is preservation of rows/files/evidence; later deactivation, role restore
or fixture expiry must be explicitly selected. Physical deletion is not implied.

The native lifecycle integration continues in WI013, followed by independent
WI011 review. These local tests do not establish audible playback, target FFmpeg
conversion, deployed permissions, cleanup or native readiness. Server resumption
remains separately gated after local integration and review finish.
