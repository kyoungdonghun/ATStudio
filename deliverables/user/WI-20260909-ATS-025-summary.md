---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: DocOps
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20260909-ATS-025-evidence-pack.md
    reason: Exact changed paths, validation and evidence boundaries
  - path: REQ-20260909-ATS-005.md
    reason: Approved audio scope; independent review still required
---

# WI-20260909-ATS-025 Summary

WI Status: **CLOSED (documentation only)**. REQ005/WI026 are not closed.

Updated the five scoped current documents and design index plus REQ005 and this WI's evidence/
summary only. The contract now covers 100MiB audio, 120MB multipart requests,
original download/full-length MP3 CBR 128kbps listening, manual activation,
safe asynchronous replacement, exact ADMIN status/retry and bounded polling.

The operations document includes actual FFmpeg environment mappings, safe
error meanings, halted-worker/restart/manual-retry
procedures and parent synthetic benchmark/provenance. Cloudflare request caps
remain a deployment condition, not solved by the backend's 120MB setting.

Parent clarified that high-rate/multichannel WAV is not excluded by product
policy. The earlier encoder restriction is superseded. Revised source uses the
nearest MP3-128kbps rate (lower on a tie) and at most stereo for the derivative
only; original preservation, 128kbps, full length and no volume normalization
remain. Parent reports codec-compatibility focused 61/61 PASS, including all
ten actual native app-pipeline cases. The later final SE focused 50/50 also
passed the resource/environment guards and all ten actual FFmpeg cases.

WI026 follow-up source now includes the 256MiB estimated full-output budget
(64KiB framing included), pre-encoder `AUDIO_OUTPUT_TOO_LARGE`, minimal child
environment allowlist and fixed Korean FAILED reasons. Original bytes and
full duration remain protected. Parent reports favorable CR source review and
known-code UI component 26/26, typecheck/scoped ESLint/3-file Prettier PASS.
Final SE focused 50/50 PASS is received; full Gradle/frontend-build results
remain pending in [WI026](../agent/WI-20260909-ATS-026-evidence-pack.md).
The first full Gradle run reported 1885 total / 7 failed / 19 skipped;
SE is investigating all seven concurrency fixtures before final acceptance.

## Verification

- Static recount: 155 mappings / 26 controllers, 43 tables / 43 JPA entities.
- `validate-docs`: PASS (718 IDs, Tier0/links/index); all nine changed files pass
  metadata/dependency/trailing-whitespace checks and scoped `git diff --check`.
  Eight protected pre-existing documents have identical before/after SHA-256.
- Design index added per parent: v2.7, current API/DB/operations links and
  source-vs-runtime status. Recount confirms 155 mappings, not old 150 + 2;
  current MySQL expectation is UNRECORDED. Nine-file checks passed.
- Received WI024: 6 focused files / 45 tests plus typecheck/lint/format PASS,
  mock/jsdom only. Parent full Vitest: 114 files / 1584 tests, 0 failed,
  239.13s, `--maxWorkers=1` PASS before WI026 friendly-reason changes;
  not rerun/log-audited by DocOps.
- Parent backend focused result before worker-private-executor adjustment: 210 total,
  209 passed, 1 Windows symlink skip. Later codec-compatibility focused run:
  61/61 PASS including all ten native app cases, distinct from CLI benchmarks.
  Final SE focused 50/50 PASS: encoder 28, pipeline 20 (actual FFmpeg 10),
  configuration 2. Known-code UI component 26/26 and typecheck/scoped ESLint/
  3-file Prettier PASS followed. Parent full-test/build closure remains PENDING
  in WI026. DocOps ran no application tests.

## Open Gates

Nine added Track columns plus the queue index mean the new MySQL manifest is
**UNRECORDED**, despite unchanged table count. The historical 43-table/
511-column record and frozen pre-feature private backup remain historical.
`scripts/database/manual-wi023-audio-processing.sql` is source only; installation
into the existing approved DB does not require creating another database.

No SQL/DB/media/private config/backup/server or Git state was changed. The
pinned backend remains undeployed; frontend HMR is not backend rollout proof.
Service FFmpeg provisioning, schema/strict integrity, proxy-boundary upload,
actual browser/full listening/seek/waveform/original-download verification
remain pending. **WI-20260909-ATS-026 independent review is underway**;
parent owns final finding closure. REQ005 stays open and no full release GO is
declared. Document completion is not application deployment.

WI025 documentation/validation closeout is complete. Parent explicitly owns
the remaining full-test evidence in WI026; this is not REQ005 completion.
CR/WI026 owns the `/uploads/.staging/**` security follow-up and any subsequent
protected-path documentation correction. Parent records final build evidence
once in WI026 before updating REQ005 closure. WI025 requires no further wait
and has performed no deployment.

See [the evidence pack](../agent/WI-20260909-ATS-025-evidence-pack.md) for all
nine changed paths and command/result boundaries.
