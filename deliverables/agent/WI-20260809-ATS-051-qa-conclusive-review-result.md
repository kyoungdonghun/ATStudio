# QA Integration Conclusive Review Result: WI-20260809-ATS-051

## Verdict

**FAIL** - 1 open P2 finding remains. No P0 or P1 finding was identified.

## P2 Findings

### ATS-051-QI-06 - A late review mutation invalidates and strands the newer detail request

- **Type:** Observed implementation defect
- **File / line:** `frontend/src/pages/admin/CompanyCertManagePage.tsx:142-160,191-221`; the existing race tests at `frontend/src/pages/admin/CompanyCertManagePage.test.tsx:289-360` cover schedule (a), not schedule (b).
- **Mandatory schedule (b):** Detail A is open and `processCompanyCert(A)` remains pending. The admin closes/replaces that context and opens detail B, so `loadDetail(B)` owns the current request generation. When the old A mutation later resolves, `confirmReview()` unconditionally calls `loadDetail(A, false)`. That call increments `detailRequestId`, invalidating the pending B request even though `selectedDetailIdRef` still owns B. B therefore cannot commit. The A refresh also cannot commit or clear loading because its selected-ID check fails. Neither completion owns the finalizer, so `detailLoading` remains `true` and B is stranded.
- **Direct confirmation:** The seven focused frontend suites pass (`126/126`), demonstrating that the committed tests do not exercise this ordering. A no-file sequence harness using the implementation's generation and selected-ID conditions produced `B generation 2 -> old A refresh generation 3`, `selectedDetailId=2`, `committedDetail=null`, and `detailLoading=true` after both B and A reads completed.
- **Impact:** A stale mutation for A can retire the valid B request and leave the ADMIN detail UI indefinitely loading. If B has already committed, the late A refresh can still turn loading back on with no eligible completion to turn it off. The unconditional post-mutation review state writes can also act on the newer modal context.
- **Bounded remediation:** Give the review mutation its own initiating target/ownership token. After the mutation resolves, mutate review state and start a detail refresh only if the open detail still owns A; otherwise skip the A detail refresh without incrementing the detail generation or touching B loading. Add an exact schedule (b) component test with B pending when A resolves, plus late A success/failure and B success/failure variants.

## P3 Findings

### ATS-051-QI-07 - Canonicalization length-growth behavior lacks focused proof

- **Type:** Missing proof, not an observed production defect
- **File / line:** The raw and canonical length guards exist at `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:171-185`, but URL tests at `frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx:294-369` cover raw 255/256 boundaries and ordinary canonicalization only.
- **Scenario:** A raw URL is at most 255 characters, but `new URL(...).href` expands it beyond 255 characters through canonicalization such as percent encoding.
- **Observed proof gap:** The implementation rechecks canonical length, but no focused test demonstrates zero API invocation for this growth case.
- **Bounded remediation:** Add one raw-length-within-bound input whose canonical URL exceeds 255 characters and assert the local 255-character error plus zero register/update calls.
