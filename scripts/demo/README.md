# Client Demo Seed CLI

The client demo seed is an explicit operator tool. It is not part of application
startup, `schema.sql`, or `seed.sql`.

## Credential Contract

- `Seed`, `Verify`, and destructive `Cleanup` require an explicit credentials
  file outside the repository.
- The tool has no default credentials path and never discovers a runtime bundle
  from a user profile or retired acceptance directory.
- `Seed -DryRun` and `Cleanup -DryRun` do not read credentials, call the API, or
  mutate demo data.
- `Verify` is already non-destructive, so `Verify -DryRun` is rejected rather
  than pretending to provide a second verification mode.
- Do not print, inspect, or commit the credentials file.

## PowerShell Wrapper

```powershell
# Secret-free planning
.\scripts\demo\seed-client-demo.ps1 -Mode Seed -DryRun
.\scripts\demo\seed-client-demo.ps1 -Mode Cleanup -DryRun

# Explicit runtime credentials are required
.\scripts\demo\seed-client-demo.ps1 `
  -Mode Verify `
  -RuntimeCredentialsPath '<external-runtime-credentials.json>'
```

The same contract applies to direct Node execution through `--credentials`.

## Focused Contract Test

```powershell
powershell -NoProfile -ExecutionPolicy Bypass `
  -File scripts/demo/test-seed-client-demo.ps1
```

The test uses only temporary empty directories and missing synthetic paths. It
does not read a credentials file or execute live seed or cleanup operations.
