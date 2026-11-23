# WI-20261123-ATS-001 · Friend invitation issuance

[Scope, inputs and ownership](WI-20261123-ATS-001-handoff.md)

The local fixture in `build/WI-20261123-ATS-001/friends.py` issued 30 distinct credentials into Windows current-user DPAPI custody. Only the protected local file holds the combined inventory. The viewer shows one numbered participant and masks the password when the selection changes. The two-word and eight-digit credentials are independently drawn with the OS random source. The local BCrypt hashing class is reused from the previous fixture.

On November 23, 2026 KST the local SE ran `friends.py prepare` followed by `verify.py` against an isolated MySQL 8 process using Windows shared memory. The first transaction created 30 ordinary users with November 23 to December 22 inclusive nonrenewing grants. A second invocation returned the same 30 IDs and retained exactly 30 grants. A deliberately conflicting identity at the last position caused the batch to fail. An independent rollback check confirmed zero partial grants and zero extra users. The temporary DB shut down. A Tk widget smoke test confirmed masked startup, explicit reveal, switching to participant 30 and re-masking.

The owner's invocation must stream the single transaction via stdin to a non-forcing MySQL batch and retain only its bounded ID output. If the result is ambiguous, reconcile state before retry. Exact rows can be reused during the guarded issue date. Existing identity or grant drift is rejected. Expiry ends access without deleting records. Remote activation and acceptance belong to the release owner.

## Operational addendum · November 23, 2026 KST

The release owner confirmed 30 USER accounts with IDs 7 through 36 and verified all 30 credential hashes and trial grants. The active Linux service roster included 32 participants, retaining IDs 5 and 6. The DB process was preserved. A public audio range request returned HTTP 206 and 1,024 bytes. The sample HTTPS loop exercised login and personal quota, history and license reads. HTTP 429 interrupted the loop, leaving live login verification partial. No participant download quota was consumed and no invitations were sent. The private viewer was opened. The SSH key was removed and the Windows SSH agent was verified stopped and disabled.
