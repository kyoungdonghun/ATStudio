# WI-20261121-ATS-010 · Two-user B1 acceptance preparation

The [approved release](../user/REQ-20261121-ATS-004.md) delegates local preparation in the [handoff](WI-20261121-ATS-010-handoff.md). The fixture and HTTPS validator are under the ignored `build/WI-20261121-ATS-010/` directory. The delivery owner coordinates remote execution separately.

- `pilot_fixture.py prepare` generates two isolated synthetic identities and uses the retained WI-20261023 account BCrypt library. Credentials are stored under the Windows user's local application data with current-user DPAPI protection. Output contains only the readiness mark.
- `pilot_fixture.py sql` emits a **private** single-session MySQL batch for the deployment owner to stream directly to the authorized database client. The ordinary non-forcing mysql batch must terminate on the first error. The transaction checks the target schema, Seoul day, unique active individual five-per-day plan and exact stored fixture identity. It creates verified ordinary users with a same-day nonrenewing nonbilling grant. A repeat reuses matching rows and returns their IDs. Ambiguous outcomes require read-only reconciliation. Neither this reviewer nor the verifier sends mail or creates payment records.
- `verify_fixture.py` used a disposable MySQL 8.0.45 datadir and synthetic schema over local shared memory. The initial batch and an identical repeat committed two users and two grants total. After a deliberate synthetic identity mismatch the batch failed. The disposable instance shut down and its temporary directory was removed.
- `pilot_https.py` uses the two protected credentials, standard TLS hostname checking, and a fixed canonical host. Its checks cover anonymous public streaming, original-download denial without login, first and repeated private file receipt, SHA-256 and length, remaining quota, own history and license UUID, bilateral denial of the other's license and logout attempts. Only counts, status, track IDs and file digests are printed on success. The release owner may pass two public track IDs and the trusted original file digests. Application-level behavior was already tested independently under [WI008](WI-20261121-ATS-008-evidence-pack.md).
- Local parsing, DPAPI round trip, retained BCrypt hash verification and disposable MySQL checks passed. This document records preparation only. Remote acceptance and access cleanup are recorded by the release owner.

## Native HTTPS acceptance on 2026-11-21 KST

The deployment owner executed the verifier on the Windows work PC against production HTTPS. On the first run the verifier compared A's email with the last login loop's B identity. A read-only reconciliation found A had exactly one original record for track 106, while B had none. The verifier was corrected to compare each response with that participant's own fixture. It resumed using the existing accounts, original record and license.

| Trial account | Published track | Bytes per complete file | Remaining daily allowance |
| --- | --- | ---: | ---: |
| 5 | 106 | 36,534,938 | 4 |
| 6 | 105 | 40,608,080 | 4 |

The first and repeat receipts matched the trusted original SHA-256 and each other. Each participant saw only its own history and license, retained the same UUID on repeat, and received HTTP 404 for the other participant's detail. The UUIDs differed. Anonymous original requests were denied; guest streaming passed. Both logouts returned 204. The two nonbilling five-per-day test grants cover November 21 KST only. The [native output](../../build/WI-20261121-ATS-010/native-result.txt) records the final HTTPS run; the phone download/save workflow remains unverified.
