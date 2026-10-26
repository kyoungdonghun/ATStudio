---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-015
---

# Evidence Pack: WI-20261023-ATS-015

## Exact Verdict and Scope

`ALLOW_EXACT_LOCAL_CAPTURE_REPAIR_ONLY` for WI014 candidate SHA256
`907206f3e918310b30c70afcd5bdd02f448fbf42cd8446ce6b2b58460e989450`,
four members, bundle
`97965d8016575e148270ddaf3660cf89f5028c29519fab40ab3ca8e97f7c7c0a`.
MA received the exact receipt before launching any revised human window.
The existing private input/retention approval supports local capture only.
No server contact, native approval issue/renewal/retry, DB, application, login,
business DML, date, apply, audio, mail or payment is authorized by this verdict.

The actual reported Save failure remains `UNCONFIRMED`. The original callback
also succeeds with valid synthetic values and actual protected file storage.
The confirmed problem is that its generic VALIDATION message conflates
confirmation mismatch, field-contract rejection, serialization and runtime
failures. This repair improves diagnosis and preserves entries without assigning
blame to the user or claiming the unknown original cause was fixed.

## Governing Context and Method

The [WI015 handoff](WI-20261023-ATS-015-handoff.md) injects the approved local-only
scope and Tier0 [core principles](../../docs/standards/core-principles.md),
[development standards](../../docs/standards/development-standards.md),
[documentation standards](../../docs/standards/documentation-standards.md) and
[glossary](../../docs/standards/glossary.md); Tier1
[security policy](../../docs/policies/security-policy.md) and
[quality gates](../../docs/policies/quality-gates.md); Tier2
[REQ007](../user/REQ-20261011-ATS-007.md) and [current entry](../../docs/index.md).
Applied create-wi-evidence-pack and focused-test reporting, using
[WI012 evidence](WI-20261023-ATS-012-evidence-pack.md) as the dated scoped baseline.

Prior implementation 8, independent 10 and ACL checks remain valid for their
stated scope; they did not exercise the actual Save callback or prove visible
human GUI success. The new review owns only its review directory and WI015
deliverables. No implementation or frozen candidate was modified. No real
window or private input was read, modified or closed. No broad
application/auth/audio suite was run.

## Reproducible Evidence

- [Review receipt](../../build/WI20261023-015-capture-review/review-receipt.json),
  SHA256 `fc4897a1c981fa495cfe6ab5bab995dc407a5723e990116059c71528502b1c1c`.
- [Candidate](../../build/WI20261023-014-capture-repair/candidate.json) and
  [operator recipe](../../build/WI20261023-014-capture-repair/README.md).
- [Independent Python checks](../../build/WI20261023-015-capture-review/test_capture_review.py),
  [independent event/storage script](../../build/WI20261023-015-capture-review/test_save_review.ps1),
  [independent output](../../build/WI20261023-015-capture-review/independent-tests.txt) and
  [implementation output](../../build/WI20261023-015-capture-review/implementation-tests.txt).
- [Identity verifier](../../build/WI20261023-015-capture-review/verify_review.py)
  validates the four candidate members and bundle, preserved WI011 five members
  and manifest, and the exact WI005 22-member tree plus manifest.

```powershell
powershell.exe -NoProfile -NonInteractive -STA -ExecutionPolicy Bypass -File build/WI20261023-014-capture-repair/test_save.ps1
python -B -m unittest discover -s build/WI20261023-015-capture-review -p test_capture_review.py -v
python -B build/WI20261023-015-capture-review/verify_review.py
```

Verification date 2026-10-23; environment Windows local; owner PG. Seven SE Save
checks and four independent tests pass. No full test matrix was added.
Preserved WI011 manifest:
`21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245`.
Preserved WI005 manifest:
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`.
No product source or retained JAR was edited by this review.

## Results

- The independent test dispatches the exact Click callback through a real
  WinForms Button using synthetic TextBox controls. An invalid email yields the
  correct Korean actor/field message, preserves all entries and writes no file.
  Correcting only that field and dispatching the same event stores all six
  intended values under the private ACL, then clears controls and closes the
  synthetic form adapter. No visible dialog or real input is involved.
- SE checks also cover nickname format, confirmation mismatch, 73-character
  password, missing synthetic Python executable and an existing-file collision.
  Errors preserve entries and do not overwrite existing content. The original
  frozen callback succeeds with actual protected synthetic storage.
- Validator diagnostics return fixed actor/field/code values. Malformed or
  duplicate-key protocol data yields fixed `VALIDATOR_BOUNDARY`, with no input
  or raw exception reflected. UI runtime codes identify fixed processing stages;
  they do not prove the underlying reason for an earlier unobserved failure.
- The unchanged frozen input contract remains the final authority. The repair
  preserves password masking, rejects overlength values, and uses a UTF-8 BOM
  for Korean PowerShell 5.1 source text.
- Local readiness succeeds in an empty private directory and creates no target
  or approval record. `-LocalCaptureApproved` records MA's already-established
  capture authority. This tool cannot contact the server or issue, renew or
  replay a native approval. Subsequent inspect retains WI011's fresh target,
  exact operator-record pin, expiry, staging and cleanup requirements.

## Limits, Preservation and Next Chain

Actual human interaction with the repaired window remains unverified. A repair
file cannot update a previously loaded process, and no automatic entry transfer
or process-memory recovery is implemented. A newly opened repair window requires
user-mediated entry or transfer; no promise of avoiding re-entry is made. MA
later reported the old process and input file absent using metadata only; PG did
not inspect either and does not independently attest that observation.

The current outcome is local capture preparation. Parent-reported earlier stage
success, inspect NOT_RUN/DB never started, and access restoration belong to the
separate native evidence chain; no such result is inferred from these tests.
Keep all synthetic and real evidence. Rollback means stop/preserve, without
deletion, overwrite or retry. WI015 allowance was sent to MA and DocOps and
unblocks WI013 integration; it does not reopen server work.
