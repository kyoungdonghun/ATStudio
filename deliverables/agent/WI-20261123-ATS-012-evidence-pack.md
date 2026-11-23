# WI-20261123-ATS-012 · Combined catalog tag release

[Handoff and Tier 0–2 inputs](WI-20261123-ATS-012-handoff.md) · [Verified UI](WI-20261123-ATS-011-evidence-pack.md) · [Previous deployment](WI-20261123-ATS-010-evidence-pack.md).

## Local preparation

On November 23, 2026 KST the reliability engineer ran `python -B build/WI-20261123-ATS-012/release.py prepare`. The adapter checks the previous operator/native code, payload and manifest digests. It pins the new 40-file pilot asset tree and entry HTML. Its native switch keeps the prior application's JAR root, retains a previous-site copy, and targets an exclusive new frontend release directory.

The package contains 171 frontend files. A separate local archive read verified every member digest, the 148 previous fingerprinted assets and the single frontend-root substitution in the active-site configuration. The 4,321,280-byte package SHA-256 is `8f0dee7263f0c93a578da6890fb84e8d0508d6dee8ce5b559464521eb02f6354`. The candidate index digest is `d2f2a8b1775832c4bc525d999e5b29efa3fce85c6b9c284f10f5d2f2588b864c`.

The CLI accepts `preflight`, `apply` and `verify` in that order. HTTPS checks cover the actual previous entry referenced in the prior HTML. The `rollback` command requires a matching native receipt and active-site ownership, and restores the previous verified site after checking process consistency.

## Production verification

On November 23, 2026 KST the release owner matched all 149 baseline frontend files in the read-only preflight. One native apply returned `FRONTEND_CACHE_APPLIED` with 171 frontend files and site SHA-256 `8c06a270a05b47271551c18c28e03f2c1740748672d36658325c4e87527947f0`. The application and database service identities matched their preflight values. HTTPS verification matched 40 current file hashes and the actual previous entry script.

In unauthenticated Chrome at 1,440px and 390px, the release owner checked the merged 450px desktop column, phone groups, visible chips against public API data and page width. Opening the sampled track by title showed all nine tags. The public tag responses exposed 38 mood tags at this observation. Real tracks carrying genre or instrument therefore remain outside this live sample. WI011's local fixture covers both categories.

In a separate isolated Chrome check, the browser loaded the published production HTML and assets and substituted synthetic API responses for that browser only. The 11-tag fixture verified genre, instrument and mood chip order and all detail tags at 1,440px, 1,100px, 900px and 390px. This check created no production track or tag records.

The release owner removed the loaded deployment key, verified the local SSH agent was stopped and disabled, and left the existing firewall policy in place.
