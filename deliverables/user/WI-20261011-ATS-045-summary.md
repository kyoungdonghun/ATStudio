---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-045-evidence-pack.md
    reason: Actual observation provenance and validation
  - path: REQ-20261011-ATS-006.md
    reason: Approved trial/recovery boundaries and operational HOLD
---

# WI045: Original HTTPS Restored, Web Goal HOLD

> Purpose: Close bounded recovery and access evidence without closing the original web objective.

**TL;DR: WI045 recording, original HTTPS recovery and access cleanup are COMPLETE. REQ006 remains active/HOLD because the original web objective is incomplete.**

MA ran WI048 v2 once: exit0/RECOVERED_ORIGINAL_HTTPS,12 checks PASS.18:05:40 verified the exact quarantine HTTPS response;18:05:42 confirmed original Nginx active, app/DB masked/PID0 and preserved TLS/evidence. The provisional disk receipt is not the success authority.

The owned key was removed and agent emptied at18:06:23, agent restored Stopped/Disabled at18:06:28. User reply plus MA's explicit tty1 login prompt confirmed root logout at18:08 (minute precision). Owned noVNC tab closed; provider tab retained for handoff. No required native sessions remain.

## Preserved Waiting Checkpoint

The observations below preserve the preceding login-wait checkpoint. No exact time was supplied for the later human reply; it is not native recovery evidence.

MA's2026-10-11 observations establish:

- WI042 app startup VERIFIED. The17 HTTP cases passed internally, but the L2 wrapper exited1/HOLD. L3/browser/S7 were not run.
- Final trial HOLD/POST_STATE_CHANGED, cause NOT_DETERMINED; cleanup25 PASS/2 NOT_APPLICABLE/9 FAILED. Cross-device overlay retention failure is strongly supported, without captured native errno.
- WI046 preserved/detached both overlays, then stopped on After output ordering before Nginx start. That consumed recovery remains HOLD; the later seven-token comparison does not explain the original trial failure.
- WI048 v2 local tests and PG049 CONDITIONAL_ALLOW are complete. MA verified14 frozen files and bounded read-only target conditions; no continuation execution has occurred.
- Last observed app/DB stopped/masked; original Nginx inactive. Provider login is required for a fresh firewall UI check.17:52 root recovery console remained logged in;17:54 SSH agent remained Running/Manual with the expected sole key. Continued temporary access is approved, but cleanup is NOT_YET_DONE.

These earlier pending conditions were resolved by the later recovery/cleanup above. Original WI042 and WI046 HOLD results remain unchanged; POST_STATE_CHANGED cause is still NOT_DETERMINED. No new app/DB trial or public release is authorized. No additional purchase; existing server charges continue.

Only the five assigned documentation/receipt files changed. Bounded documentation validation passed for five selected Markdown bodies with0 errors/warnings, plus direct JSON/metadata/whitespace and7/7 historical-document preservation checks. Final operation acceptance is not claimed.

## Related Documents

- [REQ006](REQ-20261011-ATS-006.md): Exact approvals and current HOLD.
- [Evidence](../agent/WI-20261011-ATS-045-evidence-pack.md): Times, owners, identities and validation.
- [Receipt](../agent/WI-20261011-ATS-045-native-receipt.json): Native failure, HTTP split, recovery and waiting observations.
- [PG049 Verdict](../agent/WI-20261011-ATS-049-evidence-pack.md): Conditional exact-v2 gate, not native recovery proof.
