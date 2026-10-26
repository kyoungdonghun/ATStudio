---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-050-evidence-pack.md
    reason: Exact code, native receipt and diagnostic limits
---

# WI050: State-Change Failure Diagnosis

> Purpose: Explain what the preserved evidence establishes without restarting the app.

## Assessment

**TL;DR: The post-view comparison failed. The exact changed item is NOT_DETERMINED because the checker did not preserve its comparison snapshots or failure branch.**

The stored app VERIFIED and view FINISHED fields prove the initial checks passed and the failure occurred after the viewing window. The final expression compares policy, BPF attachments[^bpf] and DB aggregates in that order. An earlier mismatch skips later reads, so the receipt does not prove the final DB check ran.

A reload occurs inside the viewing path. Official systemd v255 source supports a mechanism that can replace BPF programs, making their IDs differ even when intended rules remain equivalent. This is a credible hypothesis, not a verified cause on the installed server. Raw IPAddressDeny ordering or UnitFileState differences can also trip the comparison; local probes reproduced those possibilities. The later Nginx.After recovery mismatch is a separate event.

Ordinary row-count drift would normally produce the distinct DB_AGGREGATES error. DB involvement is nevertheless not fully excluded: a valid changed table-name set could pass the current aggregate guard but return unequal bytes. Counts alone also cannot prove that all data stayed unchanged.

## Result and Next Step

Runtime/session hashes match the approved diagnostic inputs. Thirteen pure, mocked in-memory checks passed; no app/service/main execution, SSH, SQL, secrets or source changes occurred. An initial probe-fixture mistake was corrected and disclosed in the evidence. No new purchase or manual input was needed.

Current server reads cannot recover historical snapshots that were never stored. Preserve the original HOLD. Before any separately approved future trial, add reviewed, bounded per-check diagnostics; do not simply ignore BPF ID changes or replace the baseline. WI051 can integrate these findings now. This report grants no restart or public-access authority.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-050-evidence-pack.md): Source lines, hashes, upstream references and actual probe outcomes.
- [Preserved Native Receipt](../agent/WI-20261011-ATS-045-native-receipt.json): Original failure and separate HTTPS recovery.

[^bpf]: Berkeley Packet Filter; here, kernel programs attached to the application's control group to enforce network restrictions.
