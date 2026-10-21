---
version: 1.2
last_updated: 2026-10-21
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261021-ATS-002-handoff.md
    reason: Approved bounded commit-security review
  - path: ../user/REQ-20261021-ATS-001.md
    reason: Local cleanup authorization and remote-operation pause
---
# WI-20261021-ATS-002 Evidence Pack

> Purpose: Record exact local commit-security allowances without changing reviewed content.

## TL;DR

ALLOW the 18 source/test files below; MA reports commit `86f744a` after staged-blob normalization and whitespace checks. DEFER all 74 reusable deployment candidates for this timeboxed cleanup. ALLOW the 12 new documentation files identified below at their reviewed content. This is content/security review, not new functional or production acceptance. MA alone stages and commits; no push is authorized.

## Scope and Evidence

- Approved REQ and WI handoff read. Tier 0 STD-001, STD-004, STD-002 and STD-005 and the security policy retained from the injected context.
- Starting branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; later commits do not change historical build provenance.
- Inspected tracked source diff, new production classes, configuration and test literals. Pattern inspection covered credential labels, tokens, addresses, URLs, private-key markers and personal paths; matches were classified rather than blindly rejected.
- Configuration references and synthetic fixtures are not credential disclosures. No commit-blocking sensitive content found in these 18 reviewed files.
- `2c5df7`: independent SHA-256 comparison with `build/WI068-release-run-02/source-before.json`: 18/18 exact.
- No build, application, test suite, SSH, browser, cloud, network, DB, secret access, staging, commit or push performed for this WI. No source files modified.
- No universal absence-of-secrets claim: this review covers the listed bytes, not all repository history or ignored files.

## Commit 1 Allowlist

Hashes below are raw working-file SHA-256, not Git blob IDs. MA must check the final staged diff, including any normal Git line-ending conversion, and request confirmation of its proposed manifest.

| Path | SHA-256 |
| --- | --- |
| `src/main/java/com/atstudio/atstudio/AtStudioApplication.java` | `8b9e8bba491dab5d8ae999c37c6ed93c7dd2c7740470b06ac8c65ba8ac018ae8` |
| `src/main/java/com/atstudio/atstudio/config/BackgroundTasksConfiguration.java` | `5bee9d224c9cc90aecd49a45c797a29190a9a33c0b8b77ff6764467e42be4d4c` |
| `src/main/java/com/atstudio/atstudio/config/BackgroundTasksEnabledCondition.java` | `6cfb3393e2d722d6a953fae9f7af544d5b00063e6ad4d9a63bba27d6bff5dddc` |
| `src/main/java/com/atstudio/atstudio/config/ScheduledBackgroundTasksEnabledCondition.java` | `95b0d8eeecb049bc9d094b869ec8a7c74b620e05ce013f98669d221f3d10d0e7` |
| `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` | `47676ebeb550c839f24ff0085d74339c191eb6aa277bc578eea5a654b514b5cd` |
| `src/main/java/com/atstudio/atstudio/config/StorageMutationAdmissionConfiguration.java` | `138acc117253dc28e51383dda82cac40e61ff311fa20c630ab49c1fda3775f72` |
| `src/main/java/com/atstudio/atstudio/config/StorageMutationAdmissionProperties.java` | `cd2d4230d0ab3cee563c5d64ee9550c1c82813cb17151a71244db158310f82ae` |
| `src/main/java/com/atstudio/atstudio/security/StorageMutationAdmissionFilter.java` | `2cf36e355ed1fa29147022b2802eec23ae55f978b9ae37943a4cb9cca9c5cd48` |
| `src/main/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorker.java` | `dfa81ed8bfed5338de9be1976f9f16d2fdce97164977591bf8bbc3558c8fe5b9` |
| `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java` | `4a062207d7e6a242ce81f919f1d75015485c91256a3d12a7187e40b972e20315` |
| `src/main/resources/application.yml` | `783e20ced58a50cbbaf7ad3eae30aa931ab5ae4c5c352f0165124efeab04457e` |
| `src/test/java/com/atstudio/atstudio/config/BackgroundTasksConfigurationTest.java` | `7396a60a8d3acbfbe885c6e36913054acf1dd375c064ae7a6797381e022f5f27` |
| `src/test/java/com/atstudio/atstudio/config/StorageMutationAdmissionBindingTest.java` | `dbef991d9e02d7d0373579a34153d974ed42bb316c073da91608256387ca0ef8` |
| `src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionFilterTest.java` | `929ee41382727ae799c5f835f7552abbc81affa97bac5fbbd945088645c4d47a` |
| `src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionServletOrderTest.java` | `344a462f6a23a440a81b8cdec945491fd0783d85bc559a989a8075fb2ae3cbb5` |
| `src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionTransactionTest.java` | `0f3d5fce89a12d773c529d22dd809f3baadc0f1efda1e27c5ef3c352adb1d776` |
| `src/test/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorkerIsolationTest.java` | `6b570d04b5635fdc1b45bf49549155c74a6f423e192ba9d00c2f8c91e74186db` |
| `src/test/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryServiceTest.java` | `cd1685970be1d9e9bffc3b92c009c16095965dea893ba3f367ec9df340055296` |

## Documentation Allowlist

Read the new REQ, three handoffs, path-only candidate JSON, WI001 and WI003 Evidence Packs/summaries, and evening guide. No commit-blocking sensitive content was found in this bounded documentation set. The candidate JSON lists relative repository paths, not the referenced historical file bodies. Generated output filenames are not private endpoint disclosures. All referenced historical bodies remain outside the allowance.

The following ten files plus this Evidence Pack and `deliverables/user/WI-20261021-ATS-002-summary.md` form the exact twelve-file allowance (eleven cleanup documents plus one guide). Capture the final two PG report hashes separately to avoid self-referential hashes. Any subsequent content edits need a delta check.

| Path | Working-file SHA-256 |
| --- | --- |
| `deliverables/agent/WI-20261021-ATS-001-handoff.md` | `3ef16a4d9f66f80b275b8b1315673e7f2e781effe7c0dd983b7fec57cf8c4e39` |
| `deliverables/agent/WI-20261021-ATS-002-handoff.md` | `d4d48b4f977ba96c681e3ec201184bbec80faa5d6deb5574db22f316ad4cec6b` |
| `deliverables/agent/WI-20261021-ATS-003-handoff.md` | `5e9b527668c05750eb229dffd299ebcc1742af06631d232a7cef1c5c2ca81312` |
| `deliverables/user/REQ-20261021-ATS-001.md` | `513bc5a3e930f7cfccf4a4e87d225deba16035e0869bd28baf95b8207ae6e7f9` |
| `deliverables/agent/WI-20261021-ATS-001-candidates.json` | `1180f234ecbd70077466eac5b40fc4ba384e308f96aac1be71f35719560c51bb` |
| `deliverables/agent/WI-20261021-ATS-001-evidence-pack.md` | `b3dc67519311bfbaa6cc8903edf2b6e0a339174641b6f31b25b2248bc75a34c2` |
| `deliverables/user/WI-20261021-ATS-001-summary.md` | `dc86733605645406baa7925a8dd72fe52a513d9dc3686348435691682ce99f1c` |
| `deliverables/agent/WI-20261021-ATS-003-evidence-pack.md` | `593b2616111c4c1e0d5a6e67e5315c6ce962f5f22d7e202d9d5831167f535e29` |
| `deliverables/user/WI-20261021-ATS-003-summary.md` | `d23329c891cd6cd127f426ae66aa87565896b5a952de74d23889440251fa6bbc` |
| `docs/guides/evening-auth-trial-resume.md` | `c50d6cdd44f3c695e56e5249d1c777a446e4dc9aaa1daafffdb0a24c0260daea` |

Source staged normalization, commit completion and post-commit 643/336/143 identity results are attributed to MA/SE; PG did not repeat those completed checks. WI001's shorthand referring to PG staged-byte approval means prior PG raw-content allowance plus MA's independent Git-filtered blob comparison, not a new PG index test. WI003 pending references are its pre-final-review snapshot; this section supersedes them for commit-security disposition without rewriting that history.

All 74 `reusable_deferred_this_turn` paths are DEFER by MA's timebox instruction. Partial pattern triage is not a completed review and grants no execution or publication clearance. This does not classify all 74 as unsafe. No source, deployment, operational or historical file was edited.

## Final REQ Delta and Freeze

ALLOW the final local REQ delta at the hash above, independently matched in `e92a27`. Version/status/dependency updates and the local completion/checklist record preserve the approved scope: source commit and identity results are attributed to MA/SE, all 74 generic candidates remain deferred, documentation commit remains pending, server work stays paused, and REQ007 stays active. No sensitive operational values or new execution authority were introduced. The remaining eleven allowed files are unchanged per MA; the prior allowance remains in force. MA reports the twelve-document link check passed; PG did not rerun it. This Evidence Pack is now frozen; its final hash is supplied separately. The PG summary is unchanged.

## Retained Exclusions

- All historical operational deliverables, screenshots, PDFs, generated outputs and ignored build artifacts remain outside these allowances; do not rewrite registered history.
- Retain existing changes to `AGENTS.md`, `CLAUDE.md`, `README.md`, `docs/index.md`, `docs/payment/index.md`, `docs/templates/ma-session-kickoff-prompt.md`, and `scripts/reconstruction/README.md` outside this cleanup commit set, as directed by MA.
- Unlisted files are not implicitly approved. The documentation allowance includes no historical operational records, binaries or generic deployment candidates.

## Risks and Rollback

Any content drift invalidates the affected file allowance. Exclusion means leave the file unchanged and unstaged, not delete or redact it. No operational rollback is needed because PG performed no operational action.

## Related Documents

- [WI002 Handoff](WI-20261021-ATS-002-handoff.md): scope and output ownership.
- [Approved Request](../user/REQ-20261021-ATS-001.md): local-only cleanup.
- [User Summary](../user/WI-20261021-ATS-002-summary.md): concise disposition.
- [Security Policy](../../docs/policies/security-policy.md): commit/privacy gate.
