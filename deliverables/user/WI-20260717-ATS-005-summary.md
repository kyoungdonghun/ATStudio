# WI-20260717-ATS-005 Summary

## Status

- **Result:** Complete
- **Blocker:** None
- **Scope:** Active source-of-truth documentation, approved tooling, ignore rules,
  generated artifacts, and WI-005 deliverables only
- **Product behavior:** No WI-005 product-source or test changes

## Completed Work

- Aligned active API, schema, payment, UI, client, policy, registry, and entry-point
  documentation to the authoritative WI-002/003/004 baseline.
- Recorded current source-derived counts: 137 API mappings across 23 controller
  files, 39 schema tables, 39 JPA entities, 53 lazy-loaded screens, 56 route
  paths plus one index redirect, six subscription seed rows, and Vite 6.4.3.
- Documented TOSS card recurring-only V1, provider-neutral recurring interfaces,
  explicit local configuration loading, one six-plan seed owner, retired manual
  SQL, and current emergency administration operations.
- Archived the two completed design documents in place with archive date, reason,
  replacement pointer, and index status.
- Removed the hard-coded credential-path default from
  `scripts/demo/seed-client-demo.ps1`. Non-dry-run use now requires an explicit
  runtime credential path; dry-run requires no credentials.
- Stopped tracking `frontend/tsconfig.tsbuildinfo` while retaining the unchanged
  local file under the narrow `/tsconfig.tsbuildinfo` frontend ignore rule.
- Removed four stopped runtime logs, approved generated/duplicate directories,
  and the empty `frontend/public/.gitkeep` placeholder. Added only path-specific
  ignore rules.
- Preserved the tracked client PDF and manifest and the historical screenshot ZIP.
  No branch, worktree, tag, remote, or `application-local.yml` operation was
  performed.

## WI-005 File Set

Modified active/tooling files (37):

- `.gitignore`, `AGENTS.md`, `CLAUDE.md`
- `docs/index.md`
- `docs/client/_internal-feature-map.md`, `docs/client/testing-guide.md`
- `docs/design/api-spec.md`, `docs/design/db-schema.md`,
  `docs/design/index.md`, `docs/design/p0-release-blocker-remediation-design.md`,
  `docs/design/p1-payment-integrity-remediation-design.md`,
  `docs/design/p1-security-acceptance-hardening-design.md`,
  `docs/design/payment-integration-design.md`,
  `docs/design/payment-operations-runbook.md`,
  `docs/design/payment-refund-receipt-settlement-policy.md`,
  `docs/design/payment-settlement-import-design.md`,
  `docs/design/remaining-remediation-design-20260716.md`
- `docs/design/usecase/download-queue.md`, `docs/design/usecase/index.md`,
  `docs/design/usecase/sound-playhistory.md`,
  `docs/design/usecase/user-subscription.md`, `docs/design/usecase/util.md`
- `docs/payment/acceptance-test-checklist.md`,
  `docs/payment/feature-inventory.md`, `docs/payment/index.md`,
  `docs/payment/known-limits-and-next-steps.md`,
  `docs/payment/system-overview.md`
- `docs/policies/security-policy.md`
- `docs/registry/project-registry.md`, `docs/registry/workboard.md`
- `docs/standards/development-standards.md`,
  `docs/standards/frontend-standards.md`, `docs/standards/glossary.md`
- `docs/ui/atstudio-front-list.md`, `docs/ui/screen-flow.md`
- `frontend/.gitignore`, `scripts/demo/seed-client-demo.ps1`

Tracked deletions (2):

- `frontend/public/.gitkeep`
- `frontend/tsconfig.tsbuildinfo` from the Git index only; the ignored local cache
  remains present

New deliverables (2):

- `deliverables/user/WI-20260717-ATS-005-summary.md`
- `deliverables/agent/WI-20260717-ATS-005-evidence-pack.md`

Removed untracked generated/runtime paths (8):

- `cloudflared.err.log`, `cloudflared.out.log`
- `frontend/vite.err.log`, `frontend/vite.out.log`
- `.codex-remote-attachments/`, `output/demo-seed/`
- `output/client-demo-screenshots-20260716-140514/`, `tmp/`

## Verification

- Historical preservation: 1,128 pre-existing REQ/WI/SR/audit/retrospective
  Markdown files rehashed with **0 changed and 0 missing**.
- Documentation validation: Tier 0, links, 433 traceability IDs, and document
  index all passed.
- Diff checks: working-tree and staged `git diff --check` passed. Git emitted
  LF-to-CRLF advisory warnings only.
- Demo script: dry-run passed with 36 tags, 36 tracks, and 9 playlists; no
  credential or secret was emitted. Non-dry-run verification without an explicit
  credential path failed closed as required.
- Ignore checks: all removed generated/log paths match narrow rules;
  `frontend/tsconfig.tsbuildinfo` is untracked, locally present, and ignored.
- Preserved assets: screenshot ZIP SHA-256
  `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8`;
  client PDF SHA-256
  `AFBA32CCE2460D5D38B80F4A88278E31D1F7344A2258E240BFD61DF74F4C6095`.
- Stale-reference and high-confidence secret scans passed for the active WI-005
  document/tooling set.

Concurrent product changes already present in the shared worktree were preserved
and were neither reverted nor claimed as WI-005 work. WI-005 unblocks
`WI-20260717-ATS-006`.
