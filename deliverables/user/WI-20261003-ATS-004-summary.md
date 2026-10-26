---
version: 1.1
last_updated: 2026-10-03
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261003-ATS-004-evidence-pack.md
    reason: Independent findings and reproducible checks
  - path: REQ-20261003-ATS-001.md
    reason: Approved Stage A boundary
---

# WI-20261003-ATS-004 Security Review

> Purpose: Report concrete deployment-candidate defects without claiming target approval.

## Result

**Template-only security PASS: both P2 findings resolved after same-WI re-review.**
Review owner PG; verified 2026-10-03 on
Windows/PowerShell against local `main` / `58b1011dad796017e47dbcc82a4225ca449bcdf6`
and the new deployment files. This date is independent of historical TEST dates.

| ID | Initial finding and historical location | Verified correction |
|---|---|---|
| PG004-F01 / P2 | `deploy/single-vps/nginx/https.conf.template:68`: digits-only media matcher missed `+1` and `0x1`, both accepted as Long ID 1 by Spring. | RESOLVED: current line 69 covers the full ID segment and matrix suffixes on fixed segments. Independent 54 route fixtures PASS; all 30 Spring utility paths covered. Media limits, ACL and proxy controls retained. |
| PG004-F02 / P2 | `deploy/single-vps/nginx/https.conf.template:85-88`: directly served SPA HTML lacked frame protection. | RESOLVED: current line 89 adds location-local `X-Frame-Options DENY always` beside nosniff/referrer and the existing fallback. Contract and template agree. |

Both were reported to MA and corrected by WI001, not this reviewer. This
same-WI004 re-review closes the two findings without waivers. Original findings
and candidate hashes remain historical evidence in the Evidence Pack. WI005's
WI004 dependency is satisfied; its independent QA and other REQ gates remain.

## Checks and Boundaries

- Initial review: 22 bounded static invariants and 10 protected/public fixtures
  passed; the two original defect reproductions remain history.
- Re-review PASS: 48 media and 6 negative regex fixtures; template/contract
  agreement, five media directives, three location-local always headers.
- Spring 7.0.9 PathPattern/Long conversion: 30 fixtures PASS, including
  `/api/tracks/1/stream;x` and `/api/tracks/1;x/stream` extracting ID 1.
- Separate default StrictHttpFirewall 7.0.7 mock checks: 3 ordinary/alternate-ID
  paths accepted, 5 literal/encoded-semicolon paths rejected. Utility matching
  is not full HTTP acceptance; no firewall relaxation was made or recommended.
- Static review found no additional blocker in ACL inheritance, trusted-header
  overwrite, controller-only Notice attachments, strict storage/keyring settings,
  service write boundaries, cold capture or restored-runtime quarantine.
- Error-log discard is an explicit diagnostic tradeoff, not redaction; it is
  not counted as a new defect. No missing Linux/capacity result is called a bug.
- No product services, DB, provider/mail calls, private configuration, install,
  commit or push. Only this summary and the Evidence Pack were updated.

Actual Nginx parsing/HTTP routing, browser framing, Linux permissions/systemd,
TLS/IP/firewall, FFmpeg, MySQL bootstrap, reboot, backup/restore, alerts and
capacity remain **NOT RUN**. Restricted deployment, public release and LIVE
financial operations retain their separate approvals. Existing Gmail/WAV/MP3/
phone TEST evidence is unchanged.

## Related Documents

- [Evidence Pack](../agent/WI-20261003-ATS-004-evidence-pack.md): Reproduction and scope.
- [Handoff](../agent/WI-20261003-ATS-004-handoff.md): Ownership and downstream WI005.
- [Configuration](../../deploy/single-vps/CONFIGURATION.md): Candidate contract and target gates.
