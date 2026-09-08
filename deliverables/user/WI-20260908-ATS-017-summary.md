---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: cr
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20260908-ATS-017-evidence-pack.md
    reason: Findings and review evidence
---

# WI-20260908-ATS-017 Summary

Review complete against `main 8161f0a`; no implementation, test execution by CR, or destructive reproduction. Two code-confirmed P1 release blockers require correction before the affected workflows are released; runtime occurrence remains unverified. No P0 was established.

| ID | Priority / confidence | Finding |
|---|---|---|
| F1 | P1, code-confirmed | Track soft deletion erases issued licenses/history despite UI deactivation wording. Same-day history removal also restores already-consumed daily slots for other first downloads. `src/main/java/com/atstudio/atstudio/service/TrackService.java:222-223`; `DownloadService.java:63-68` in the same directory; `frontend/src/pages/admin/TrackManagePage.tsx:377-378`. |
| F2 | P1, code-confirmed | Playlist AND Album deletion removes an unshared thumbnail but retains its inactive parent's DB key; strict startup audit then fails. One shared defect: `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:268-272`, `AlbumService.java:139-145`, `storage/StorageIntegrityService.java:50-53` under the same service directory. |
| F3 | P2, unexecuted concurrency risk | Unlocked, unversioned Track writers may restore a removed key through a stale full-row update. Actual SQL/interleaving needs proof; NOT accepted as maintenance before confirmation. `src/main/java/com/atstudio/atstudio/service/TrackService.java:170-198`. |
| F4 | P2, bounded maintenance candidate | Partial-write I/O failure excludes the failing stage from cleanup/journaling. Low-frequency failure path; production frequency/volume unmeasured, no remote disclosure or runtime reproduction claimed. `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java:69-78`; `LocalStorageService.java:89-95` in the same directory. |

P1 findings were reported early to MA. Recommended containment is to hold the affected delete workflows pending a separately approved preservation/integrity fix, not disable strict auditing. No runtime containment was applied by CR.

Existing defenses checked: original-file static denial, active-only full streaming, paid/quota first downloads and license-based re-downloads, path validation, private documents, ADMIN authorization, canonical image uploads, attachment-only headers, CSV neutralization, whitelist state/self-slot rules, and Playlist ownership/locks.

The evidence pack contains the three-way requirement/code/test map and bounded MA test commands. Existing tests inspect many negative cases but miss license retention, delete-to-startup integration, stale Track writes, and partially written failing stages. Mock-based lock checks are not concurrent database proof.

Fresh results reported by MA, not executed/recounted by CR: H2 backend 1,708 total / 1,689 pass / 19 skip (18 MySQL-gated + 1 LocalStorage platform) / 0 failure; frontend 1,493 PASS. These are execution counts, not coverage, and do not close F1-F4.

Only this summary and the WI017 evidence pack were added with `apply_patch`. No source/tests/config changes, Git writes, Gradle/npm execution, live APIs/provider/SMTP, external credential reads, or subdelegation occurred.

Current production occurrence, target filesystem/proxy behavior and restore readiness remain unverified. Full-song public listening is intentional; no derivative-media or payment policy was invented. WI018 owns central verification and final REQ004 disposition. This is not production GO.
