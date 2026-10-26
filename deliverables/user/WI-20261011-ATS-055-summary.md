---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-007.md
    reason: Approved bounded trial preparation
  - path: ../agent/WI-20261011-ATS-055-evidence-pack.md
    reason: Exact implementation and local verification evidence
---

# WI055 Restricted Web Candidate

**PG056 F1/F2/F3 repaired locally; final PG re-review pending. No remote action by SE.**

The fresh WI055 candidate fixes HTTP child exit/receipt handling, preserves overlays
durably before same-directory no-overwrite detachment, and continues independent
cleanup attempts after failures. It retains WI052/v2 state diagnostics and strict
policy/BPF/DB checks. The existing primary error is preserved. The only restoration
comparison exception is WI048's explicitly documented Nginx After-token ordering.
Status and final postcheck now require an exact-byte completion marker; full bytes
left by a failed result fsync cannot be republished as success. Failed evidence is retained.

Windows verification: historical106/106, diagnostic100/100, candidate130/130,
PowerShell38 assertions and sealed SelfTest8 assertions PASS. PG-authored reproducers
rerun by SE: Python16/16, transport8/8, HTTP18/18 PASS; step-specific schema50 PASS.
Counts overlap; these reruns do not replace final independent approval.
All84 historical files remain unchanged. Local byte transport tests cover UTF-8/LF
and final newline; real SSH and Linux durability remain unproven.

MA's current access/firewall/quarantine observations are separate evidence.
This work does not change the target, authorize public access/billing, expand into
mail-sender work, or establish production readiness. PG056 exact-byte review comes
next; MA WI058 may proceed only after review and fresh current preflight.

- [Executable Recipe](../../build/WI055-restricted-web/README.md)
- [Frozen Manifest](../../build/WI055-restricted-web/freeze.json)
- [Detailed Evidence](../agent/WI-20261011-ATS-055-evidence-pack.md)

Use the new candidate only after review. Preserve all originals and failed attempts;
no history deletion, claim reuse or automatic retry is part of rollback.
