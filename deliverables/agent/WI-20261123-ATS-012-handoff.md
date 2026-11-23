# WI-20261123-ATS-012 · Release the combined catalog tag column

Parent [REQ-20261123-ATS-002](../user/REQ-20261123-ATS-002.md), with the user's follow-up approval to release [WI011](WI-20261123-ATS-011-evidence-pack.md). Depends on WI011. No successor.

Reliability engineer prepares a local release adapter and assets from the reviewed pilot in `build/WI-20261123-ATS-011/pilot` and the previously deployed WI010 payload. Release owner performs native preflight, one verified frontend root switch, live HTTPS and public browser checks, restores the local SSH agent, and records results. Existing backend, database, public listening, accounts and firewall are outside the change.

## Acceptance

Pin the previous payload, site, JAR and candidate identity. Retain all prior hashed assets, verify the previous application and database process identities, reject unexpected state and preserve rollback to the WI010 site. Use a new release directory and claim. Verify combined genre, instrument and mood on desktop, grouped phone display, title navigation and public detail tags. Keep source and private operational material separate.

## Inputs and deliverables

Tier 0: `docs/standards/core-principles.md`, `docs/standards/development-standards.md`, `docs/standards/documentation-standards.md`, `docs/standards/glossary.md`. Tier 1: `docs/policies/quality-gates.md` and `docs/policies/security-policy.md`. Tier 2: `deploy/single-vps/runbooks/frontend-cache.md`, WI010 and WI011 evidence and local artifacts. Output the scoped operator adapter under ignored build storage and a WI012 evidence pack and user summary with checks, rollback and actual scope. Do not modify past evidence.
