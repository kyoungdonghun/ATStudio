# Dedicated QA library acceptance preparation

See [WI018 handoff](WI-20261123-ATS-018-handoff.md) and [WI015 local integration](WI-20261123-ATS-015-evidence-pack.md). This entry covers local operator-tool preparation.

The original vendor-independent DPAPI reference for both dedicated synthetic accounts exists on the Windows work PC. The [November 21 deployment evidence](WI-20261121-ATS-010-evidence-pack.md) establishes a one-day grant for IDs 5 and 6. The [November 23 grant evidence](WI-20261123-ATS-001-evidence-pack.md) retains those IDs on the roster while issuing new one-month grants to IDs 7–36. Roster membership alone does not prove current grant eligibility or idle login state.

## Operator usage

- `python -B scripts/validation/feature-trial/pilot_library_https.py public` checks the normal public catalog and 256-byte HTTPS audio range at the canonical site. Credentials are not opened for this mode.
- Use `run --dedicated-qa-ready` only for the two dedicated IDs after independently confirming both current grant windows, identity binding and approved session custody. The fixture loader reads the dated replacement for ID 5 and the original WI010 reference for ID 6. The server's ordinary login changes session state.
- Normal run records baseline track likes, active list IDs and remaining quota, selects published tracks originally unliked by both, marks intended mutations in the local journal, adds one like per owner, creates one tagged QA playlist for ID 5, adds two tracks and reorders them. It logs out and logs back in as that user, verifies order and bilateral list/like separation, and streams a public range.
- On completion it deletes the generated list and only the test likes, then checks the original baseline and logs out both dedicated identities. It never requests the source download endpoint. The normal playlist deletion API deactivates the new row and removes membership rows. Database sequence advancement and login/audit effects are not reversed.
- Interrupted writes are reconciled with the generated marker and a private local receipt in `build/WI-20261123-ATS-018/`. `cleanup --dedicated-qa-ready` is for reconciling an unfinished run after operator verification of custody. A concurrent publication change, ambiguous marker or baseline mismatch is a hold. Unconfirmed logout after confirmed data restoration requires separate read-only operator reconciliation.

## Local checks

`python -B -m unittest discover -s scripts/validation/feature-trial -p test_pilot_library_https.py -v` passed four offline tests covering normal isolation/restoration, committed create with lost response, full existing capacity and hidden like after external unpublication. Preparatory checks made no server call. Operational outcomes belong in a dated owner addendum based on actual remote observations.

## November 23, 2026 KST · production read-only preflight

The dedicated QA workstream ran a bounded, read-only MySQL transaction over the approved WI017 transport. Both account rows had the ordinary user role, verified status and no deletion flag. ID 5's recorded nonrenewing grant runs November 21–30 and its refresh field was populated. ID 6's recorded nonrenewing grant ended November 21 and its refresh field was empty. The field is an indicator of a stored login, not proof of an online device. This inspection returned no personal contact details, token values or other participant rows.

Under subsequent owner approval the workstream prepared the ID-6-only grant adapter for operator use. Its receipt binds the grant primary key, original and expected temporary full-row fingerprints and audit fields. The transaction preserves the original audit timestamp, compares the expected pre-image, requires exactly one changed row, checks the post-image and commits. The restore command uses the saved original expiry and accepts a later KST date. Offline exercised paths covered a normal apply, drift hold, post-midnight restore and repeated restore. The deployment owner records actual mutations, HTTPS acceptance and restoration separately.

The first operator run stopped at ID 5's login with HTTP 401 before any private-library mutation. A dedicated ID 5 credential had been rotated after the original WI010 fixture. A scoped production read-only comparison verified the dated ID 5 replacement and original ID 6 reference against the credential hashes and synthetic identities. The original ID 5 reference mismatched. An independent local Spring Security BCrypt check matched the two current password/hash pairs without displaying those values. The verifier now binds the replacement's email to the original dedicated identity and never loads the participant roster. All five offline tests passed, including a new regression for a rotated credential belonging to the wrong account.

## Production acceptance

The deployment owner ran the corrected verifier through the canonical production HTTPS site. The local receipt records `trial_passed=true`, `state=restored` and `logout_confirmed=true`. Both QA users liked different published tracks. ID 5 added two published tracks to a newly created playlist, reversed their order and recovered that order after logging out and back in. Each owner's list and likes matched their expected independent baseline plus only their own temporary additions. A public audio byte range was served. All newly created library entries were removed from the two active libraries. The original likes, active playlist IDs and remaining download counts were observed again for both identities. This was an API acceptance on the deployed site. It did not exercise a physical phone.

The separate grant operator receipt records `phase=restored` after comparing the restored ID 6 row with the original full-row fingerprint. The ID 5 login used the user's approved replacement of its previous session. Both QA sessions created by this test were logged out. No identity from the 30 participant accounts was used by the verifier.

## November 23, 2026, 23:50 KST · operator closure

The deployment owner verified the native final state. ID 5 expires on November 30 and ID 6 on November 21. Both stored refresh-token fields were empty. The operator removed the exact deployment key from the Windows SSH agent and confirmed that the agent was stopped with its startup type disabled.

The freshly loaded iwinv firewall console showed only inbound TCP 80 and 443. A direct connection from the previously allowed work PC to the public server's TCP 22 failed. With SSH closed, the external HTTPS public catalog and audio byte-range checks passed. [Production release evidence](WI-20261123-ATS-017-evidence-pack.md) · [Completed requirement](../user/REQ-20261123-ATS-003.md).
