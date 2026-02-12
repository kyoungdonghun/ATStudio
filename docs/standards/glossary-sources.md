---
version: 1.0
last_updated: 2026-01-06
project: system
owner: EO
category: standard
status: stable
dependencies:
  - path: glossary.md
    reason: Glossary reference
---

# Glossary Sources (Official) — Link/Evidence Only Management

> Principle: Standard terms **reference official standards**.
> This file records **official source links + verification dates only**, rather than copying numbers/data.

## Public Standards (KR)

### Public Data Common Standard Terms (Ministry of the Interior and Safety/Public Data Portal)

- **Local snapshot (repo)**: `docs/standards/public_data/공공데이터_공통표준7차_제·개정(2024.11월).xlsx`
- **Sheets**: Common Standard Terms / Common Standard Words / Common Standard Domains
- **Generated index**: `docs/standards/public_data/standard_glossary/PDS7_INDEX.json` (Regenerate: To be provided via `.claude/scripts/` or `.claude/skills/`)
- **Source**: (Add official link here. E.g., Public Data Portal/Ministry of the Interior and Safety distribution page)
- **Last verified**: YYYY-MM-DD
- **Notes**:
  - This repository uses the above XLSX as "source of definition" and references rows via `External Standard Ref` in `docs/standards/glossary.md`.
  - Must include **which sheet** (Standard Term/Standard Word/Standard Domain) was referenced in Ref.

## Project-specific Additional Standards (Optional)

- Add here if project depends on additional standards
