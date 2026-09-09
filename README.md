---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: guide
status: active
dependencies:
  - path: scripts/reconstruction/README.md
    reason: Development reconstruction and private restore boundaries
---

# ATStudio

ATStudio (customer-facing brand **AT.M**) is a Shorts music marketplace with a
Java 17 / Spring Boot backend and a React / TypeScript SPA in `frontend/`.

**Start with the [development reconstruction guide](scripts/reconstruction/README.md).**
A checkout restores source and reviewed documents. It does not restore secrets,
the database, media, private attachments, or a running service.

## Working Context

- [AGENTS.md](AGENTS.md): Codex rules, approval gates and WI ownership.
- [CLAUDE.md](CLAUDE.md): Active Claude entry point; `.claude/` remains valid.
- [Documentation index](docs/index.md): Current Source of Truth (SoT), design,
  security policy, UI and payment guides. Read the four Tier 0 documents first.
- [Historical retention register](docs/registry/v1-artifact-retention-20260909.md):
  Locate preserved/recovered development records and private-original lookup
  entries. Historical records are dated evidence, not current startup instructions.
- [Development history recovery register](docs/registry/development-history-recovery-20260909.md):
  Per-document rationale and provenance for recovered public historical documents.
- [SR-93](docs/SR/SR-93.md#remaining-production-gates): Separate production gates.

## First Safe Check

Run in PowerShell 7.2+ from the checkout root:

```powershell
pwsh -NoProfile -File ./scripts/reconstruction/Test-DevelopmentRecovery.ps1 -AsJson
pwsh -NoProfile -File ./scripts/reconstruction/test-development-recovery.ps1
```

The preflight reads only named public source manifests and file metadata. It
does not install dependencies, load private settings, connect to MySQL, launch
servers or contact Providers. `ok=true` is a source-check result, not recovery
or runtime readiness. See the guide for tools, source validation and the
separately authorized local/fresh/retained-data paths.

## Related Documents

- [Runtime storage operations](docs/design/runtime-storage-operations.md): DB,
  public root and private root must remain one paired recovery point.
- [Acceptance lifecycle](scripts/acceptance/README.md): Test-only guarded
  environment, not an equivalent replacement for a retained local runtime.
