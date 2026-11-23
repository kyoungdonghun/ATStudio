# Native release package for the participant library

[Handoff and reference standards](WI-20261123-ATS-017-handoff.md) · [Approved scope](../user/REQ-20261123-ATS-003.md) · [Scoped edge and two-user regression](WI-20261123-ATS-015-evidence-pack.md)

## Candidate and checks

The packaging engineer built the pilot UI locally and verified the Gradle boot JAR. The package contains 204 manifest entries and is 75,397,120 bytes. All 170 fingerprinted files from the previous UI release retain their original bytes. The old index is replaced. The new nginx configuration takes the exact prior native configuration, replaces its single pilot location block and changes its frontend root. The systemd unit transformation changes only the JAR path. It preserves the established 32 participant arguments.

| Artifact | SHA-256 |
|---|---|
| JAR | `199465b5b8f6cdb9a61e43916a305ff7a21941dcc9dfd74a04f2ec6370a83033` |
| Pilot HTML entry | `0d7348546c6974eced1e6773b8fd176810b6e55e00babeb7285a2d4280155ee5` |
| Final nginx site | `5ce593f8ceaa8168c05e83729d8a7b4102cea838fc37bff4ac4472f7a279d5c7` |
| File manifest | `033d92bf2260c3fd5072e1afc4bc2558641a4b5e1759215236a13d867eef5034` |
| Uncompressed tar | `b11a4764548bd65491aeea06b4d5ef65e662df51469f8ac3de07cc3e36ad6c93` |

The local package and host-bound activation adapter are in `build/WI-20261123-ATS-017/`. `release.py` reuses the pinned transport and native helpers of previous releases. `native_ops.py` stages and verifies every member, uses the previous application as a read-only bridge and checks the DB service invocation through the switch. Its temporary write gate pauses downloads, login and personal endpoints while keeping public reads and range streams available. It drains nginx workers and active audio jobs before restarting the application. Service binaries, old web roots and deployment receipts are retained.

## Reproduction

- `gradlew.bat --offline bootJar` passed. From `frontend`, set `VITE_APP_MODE=pilot` and run `node_modules/.bin/vite build --outDir ../build/WI-20261123-ATS-017/pilot`.
- `python -B build/WI-20261123-ATS-017/release.py prepare` reconstructs the tar from pinned previous inputs and verifies artifact paths and hashes.
- `python -B build/WI-20261123-ATS-017/test_release.py -v` passed 7 checks, including archive retention, roster preservation, private GET write fencing, paginated catalog identity, native command size, DB identity drift, and simulated rollback order. The test initially used the Windows path separator for a native proxy path. It now supplies POSIX paths when checking the Linux configuration. The two edge method and path tests also passed.
- Owner reported Chrome passed against the same HTML index, using both accounts and desktop/mobile layouts. On November 23 at 23:40 KST the owner reported a native read-only preflight matching 171 existing UI files and 32 participants, with no pending or claimed audio work.

## Native execution and rollback

The owner executes `release.py apply` within 30 minutes of read-only preflight, then `release.py status` and `release.py verify`. Public HTTPS checks cover pilot SPA deep links, every current asset, the previous entry script, restricted personal APIs and unrelated closed routes. Authorized two-participant interaction is checked separately by the owner. An interrupted connection requires a status read before deciding on recovery. The adapter records the original unit, site, manifest and final state. For a verified committed release, `release.py rollback` restores originals through the previous application bridge and checks the original DB service identity. A staged or ambiguous state retains the evidence and requires reconciliation.

The local rollback test uses simulated operations. The owner's native `apply-receipt.json` recorded COMMITTED at 23:43 KST on November 23. It matched the JAR, site and index hashes listed above. The bridge and new process both served the same 210 public tracks. The DB service invocation and the 32-person roster were preserved. The subsequent `status-receipt.json` independently checked every installed manifest member, matched the live process, and confirmed the bridge was gone. The owner reported that all 15 external range requests returned HTTP 206. External verification passed for the pilot pages, current assets, previous entry script, restricted personal APIs, and closed routes. Personal-account verification and external SSH closure are tracked by the owner under WI018.
