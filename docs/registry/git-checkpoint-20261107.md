---
version: 1.0
last_updated: 2026-11-07
project: ATS
owner: DOCOPS
category: registry
status: active
dependencies:
  - path: ../index.md
    reason: Current decision entry
  - path: ../../scripts/reconstruction/README.md
    reason: Source reconstruction and private data boundary
---
# Operational listening source checkpoint — 2026-11-07

This checkpoint preserves the source needed for client-owned music upload,
editing and explicit publication, with anonymous listening to published tracks.
It supplements the [November 6 source checkpoint](git-checkpoint-20261106.md).
It does not supply live configuration, accounts, private data or runtime authority.

## Current bounded result

On November 7, the MA observed the deployed HTTPS site, a complete synthetic
upload/convert/edit/publish/anonymous Range/unpublish cycle, and desktop-browser
administrator login, management, upload and edit screens, publication-dialog
cancellation and logout. Normal application and database services were retained
running and enabled. Temporary SSH access and local key/agent state were restored.
The public site was reloaded after that administrative cleanup.

These are dated MA runtime observations recorded by WI-20261107-ATS-019/020/021,
not a fresh server check by this commit review. The public catalog was empty;
synthetic validation records remained private. Clients upload and publish their
actual music themselves. Use the [client guide](../../deliverables/user/ATStudio-client-upload-guide-20261107.html)
for the observed interface flow.

Boot enable is not a reboot test. API Range verification is not human audio
playback or physical-mobile testing. Recurring backup and independent alerting,
complete recovery, operational mail onboarding, PG LIVE/payment/refund and
grant-agency acceptance remain separate work. This checkpoint does not establish
a new successful backup or restore.

## Source changes preserved

| Area | Current behavior |
|---|---|
| Security chains | Ordinary and listening configurations are mutually exclusive at application startup. Both full application modes have regression coverage. |
| Capture polling | Audio-only operation can use its dedicated capture-mailbox consumer without enabling general business schedules. Ordinary scheduling must not create a second consumer. |
| Administrator admission | Listening defaults to `SOURCE_RESTRICTED`. Explicit `AUTHENTICATED_ADMIN` permits credential login from client networks while protected APIs still require ADMIN. Invalid mode values fail binding. |
| Proxy and edge configuration | The separately named authenticated-admin nginx template retains exact route restrictions, proxy identity handling, TLS and rate/connection limits. SSH access is independent. |
| Test fixtures | Ordinary-mode and worker/catalog integration fixtures are explicit; production restrictions are not disabled to make tests pass. |

Source, regression tests and the nginx template form one coherent candidate.
The frontend source was reused. No schema, account, payment-policy or dependency
change was made for this preservation task. See the
[listening guide](../../deploy/single-vps/listening/README.md) for mode pairing.

## Evidence identity and limits

The deployed backend candidate was BEv5, SHA-256
`a8df2bbb8d3a347b1a829d5c6d6222bf2d3497b8b4fa31f78550f391b790e287`.
Its source and test manifests, build output, original failures and raw runtime
receipts remain in local private/ignored custody. They are not checkout files.
The preserved BEv4 recovery candidate must not be substituted with BEv5 merely
because BEv5 is the normal-site artifact.

The retained BEv5 test evidence reports 66 passing latest-suite outcomes with no
failures/errors/skips. This combines unchanged passing suites from one run with
the corrected suite and two full-application suites from a later run; it is not
one execution of 66 tests. A subsequent `bootJar` completed. Independent review
was recorded separately. Original failed attempts were retained.

WI022 selects exact source/export hashes; WI023 checks privacy; WI024 reviews
source coherence, evidence identity and shareability before MA Git operations.
Their local manifests distinguish original Windows bytes from Git-filtered LF
bytes. A fresh checkout can rebuild implementation, but neither an identical JAR
nor a restored service follows from source alone. Recreate reviewed settings and
native pins for an approved target; do not replay historical execution inputs.

## Retained evidence and excluded material

Earlier local work contains host-bound recovery, deployment and validation tools,
private execution inputs, runtime receipts, generated JAR/frontend artifacts,
screenshots, older documents and PDFs. These remain preserved outside the selected
Git change. Existing reusable backup tools stay tracked without bulk-copying
dated execution directories or silently rewriting registered originals.

Public documentation variants are derived from the previously reviewed public
HEAD with a bounded current addition. Active original local documents are
retained, so they can legitimately remain modified after the selected commit.
A clean Git status is not a reason to delete or publish excluded evidence.

This commit task performs no server, provider, database, mail, payment, backup
or restore action. Git publication is source preservation, not data backup.
