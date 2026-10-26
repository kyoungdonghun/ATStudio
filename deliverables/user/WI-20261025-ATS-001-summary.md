# WI-20261025-ATS-001 Summary

The two local recovery defects found by WI027 are repaired in a new frozen candidate. Original evidence and candidate bytes are preserved.

Reconciliation uses a fresh attempt identifier for its service, retained log and result files while keeping the original apply claim. Read-only reconciliation remains available after the grant end date; writes remain blocked after that date.

Three focused tests and three payload compilation checks passed. Independent WI002 review is the next gate. This WI performed no server access, DB operation or business-data change.

Detailed evidence: [WI001 evidence pack](../agent/WI-20261025-ATS-001-evidence-pack.md).
