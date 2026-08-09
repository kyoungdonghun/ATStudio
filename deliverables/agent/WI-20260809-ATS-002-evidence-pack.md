---
version: 1.0
last_updated: 2026-08-09
project: ATS
owner: se
category: evidence-pack
status: complete
related_wi: WI-20260809-ATS-002
---

# Evidence Pack - WI-20260809-ATS-002

## Summary

Repaired the `@DataJpaTest` slice dependency for the Track audio replacement
rollback integration test with one Mockito bean and verified the focused test.

## Scope / DoD Check

- [x] Supplied `CanonicalImageService` through the existing Spring test-mock
  mechanism.
- [x] Preserved execution through `TrackService` and the database constraint
  rollback assertion.
- [x] Focused test reran with `--rerun-tasks` and passed.
- [x] Java compilation and `git diff --check` passed.
- [x] WI-023 records the repair dependency and required clean full rerun.

## Reference Documents

**Injected Context from WI Handoff Packet**

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Required constitutional context |
| 0 | `docs/standards/development-standards.md` | Required development and evidence context |
| 1 | `docs/policies/quality-gates.md` | Test and quality gate requirements |
| Context | `deliverables/agent/WI-20260808-ATS-023-handoff.md` | Failure and dependency context |

## Failure Evidence

- `deliverables/agent/WI-20260808-ATS-023-evidence-pack.md` records the
  original `NoSuchBeanDefinitionException: CanonicalImageService` during
  application-context creation.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:51-54` is the
  constructor dependency source identified by WI-023.

## Change Evidence

- `src/test/java/com/atstudio/atstudio/service/TrackAudioReplacementTransactionIntegrationTest.java:12`
  adds the `CanonicalImageService` import.
- `src/test/java/com/atstudio/atstudio/service/TrackAudioReplacementTransactionIntegrationTest.java:47`
  adds `@MockitoBean CanonicalImageService canonicalImageService;`.
- No production source or unrelated test was changed by this WI.

## Commands and Results

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackAudioReplacementTransactionIntegrationTest" --rerun-tasks --console=plain
```

- `BUILD SUCCESSFUL`.
- JUnit XML: `build/test-results/test/TEST-com.atstudio.atstudio.service.TrackAudioReplacementTransactionIntegrationTest.xml`
  reports `tests="1"`, `failures="0"`, `errors="0"`, `skipped="0"`.
- The rollback test method executed and passed.

```powershell
.\gradlew.bat compileJava compileTestJava --rerun-tasks --console=plain
git diff --check
```

- Compilation: `BUILD SUCCESSFUL`.
- Diff check: exit code 0; only existing-worktree CRLF conversion warnings were
  emitted.

## Risks / Rollback

- Risk: the focused test is green, but the clean full-suite and coverage gate
  have not yet been rerun.
- Rollback: remove the two test-wiring lines identified above. Do not alter the
  preserved dirty worktree, ZIP, database, external systems, secrets, stage, or
  commit state.

## Follow-up

- WI-023 must run the exact clean backend full-suite command with
  `clean test jacocoTestCoverageVerification --no-build-cache --rerun-tasks`
  and record fresh JaCoCo results before WI-023 can close.
