
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A094: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a094). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260817-ATS-028 Independent Review Summary

## Result: FAIL

The submitted WI-027 material does not meet the approved REQ evidence and workflow contract for a PASS.

## Material Findings

1. **Unsupported account-provisioning path.** REQ-011 requires the account, tags, tracks, media, waveform, and album relations to be created through supported application service/API/storage workflows. WI-027 instead records direct JDBC provisioning for the verified `ADMIN` account. The REQ says unsupported workflow coverage is a blocker, not permission for a database bypass.
2. **Exact parity is not independently proved.** WI-027 supplies matching aggregate counts only. It does not provide a credential-safe canonical comparison of the scoped account identity and verification state, tag values by category, ten track identifiers, per-track tag mapping, or ordered album membership. Equal counts cannot establish the required exact parity.
3. **Target isolation and schema boundedness remain assertions.** The evidence pack states guarded loopback targets and a one-table additive alignment, but contains no reproducible, non-secret proof artifact that independently binds the checks to exactly the two approved target classes or shows the pre/post schema delta. Current source confirms that `payment_settlement_import_attempts` is defined, not that the reported live development change was the sole change.
4. **Strict UI/API/storage/persisted-state separation is incomplete.** The submitted material reports API and aggregate persisted-state results, plus media Range results, but not a distinct UI behavior observation for each environment.

## Verified Read-Only Facts

- The current application contracts support ADMIN tag creation, multipart track creation with audio analysis/storage/waveform and tag assignment, track activation/tag replacement, public streaming, album creation, and unique active-track album membership.
- `schema.sql` defines `payment_settlement_import_attempts`; the current tracked worktree has no tracked-file diff. No build, test, database, network, runtime, or credential/bundle inspection was performed in this review.

## Residual Risks

- The live database contents, exact two-target isolation, public-client runtime behavior, and media retrieval claims were not independently re-executed because this WI is read-only and credential/bundle access is forbidden.
- Do not treat the WI-027 seed as acceptance-complete until a compliant supported account-provisioning path and credential-safe canonical parity evidence are supplied under an approved follow-up WI.

## Changed Files

- `deliverables/user/WI-20260817-ATS-028-summary.md`
- `deliverables/agent/WI-20260817-ATS-028-evidence-pack.md`
