
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A091: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a091). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260817-ATS-019 Summary

## Result

The current release candidate is `codex/v1-release-rehearsal-fixes` at `13fc37e`. The client snapshot for this new acceptance cycle must be created from this commit, not from the older acceptance branch.

No branch, server, tunnel, database, email, or Toss state was changed during this investigation.

## Branch Disposition

- Already contained in the current release candidate: `codex/p1-acceptance-hardening`, `codex/payment-integration-clean`, and `dev/PRIVATE-CONTRIBUTOR`. They may become deletion candidates after preservation-tag confirmation.
- Still need content disposition: `master`, `codex-payment-integration-design`, and `codex-sr-91-tag-taxonomy-layout`. They have commits not contained in the current release candidate, so they must not be deleted yet.

## Runtime Finding

Only a Vite frontend is currently listening on port `5173`; the Spring backend and Cloudflare tunnel are not running. The documented acceptance launcher needs both `5173` and `8080`, so the existing `5173` process must be handled deliberately before starting the complete client environment.

## Next Approval Gate

Approve either of these exact options:

1. Stop the existing Vite process on `127.0.0.1:5173`, then start the complete frozen client-acceptance runtime using the documented launcher.
2. Keep it running and separately approve a launcher/configuration change for different ports.

The recommended option is 1 because the existing launcher, readiness checks, and operator guide are built around fixed ports `5173` and `8080`.
