---
version: 1.0
last_updated: 2026-02-12
project: ATS
owner: EO
category: standard
status: stable
dependencies:
  - path: glossary-sources.md
    reason: Term sources and reference information
tier: 0
target_agents:
  - docops
  - "*"
task_types:
  - documentation
  - research
---

# Glossary — Canonical Terms

> Purpose: Ensure **using the same word with the same meaning** throughout development/operation to prevent synonym/similar term proliferation and meaning drift.

## 1) Operating Rules (Core)

- Use only **Canonical Terms** as "official names".
- To use different expressions in documents/ADR/WI/code (comments), register them only as **Synonyms** and use Canonical Terms in body text.
- Do not use **Forbidden** terms (cause confusion).
- When possible, use **Canonical Term Key** for tags/keywords (e.g., `auth`, `billing`, `wi`)
- When possible, position **official standards (e.g., public data common standard terms)** as "higher criteria", and this glossary only adds **mapping (aliases/reference IDs)** on top.
  - Record official source links/verification dates in `docs/standards/glossary-sources.md`.

## 2) Scope

- **System (Global) Glossary**: This file (`docs/standards/glossary.md`) is the standard.
- **Project (Local) Glossary**: Each project repo manages separately in `docs/project/glossary.md` (or equivalent location).
  - If project terms conflict with Global terms, specify in **Project Domain Context** and fix "In this project, X is used with Y meaning".

## 3) Term Entry Format

| Key         | Canonical Term                     | Definition                                      | Synonyms (allowed)        | Forbidden           | External Standard Ref | Notes/Examples                                     |
| :---------- | :--------------------------------- | :---------------------------------------------- | :------------------------ | :------------------ | :-------------------- | :------------------------------------------------- |
| auth        | Authentication/Authorization       | Overall authentication/authorization (can be separated by project) | login, sign-in, permissions | security (umbrella term) | TBD                   | Subdivide into `authentication`, `authorization` if needed |
| mcp         | Model Context Protocol (MCP)       | External standardized tool interface protocol (not used in current system) | Tool Standard             | Agent (misleading)  | TBD                   | Not used in Claude Code environment; retained for external ecosystem reference |
| wi          | Work Item (WI)                     | Minimum tracking unit for change/new work       | task, ticket              | issue (scope unclear) | TBD                   | "WI-YYYYMMDD-###"                                  |
| adr         | Architecture Decision Record (ADR) | Record of important decision rationale          | decision document         | meeting minutes     | TBD                   | "Why/alternatives/risks/rollback"                  |
| criticality | Criticality                        | Work importance (HIGH/MEDIUM/LOW)               | importance, priority      | Priority (confusion risk) | TBD                   | HIGH=accuracy priority, LOW=speed priority         |
| hitl        | Human-in-the-Loop (HITL)           | User approval before complex/destructive work execution | user confirmation, approval | Approval (umbrella) | TBD                   | See EXECUTION_POLICY.md                            |
| req         | Request ID (REQ)                   | Request object identifier                       | request_id, order         | -                   | TBD                   | E.g., `REQ-...` (Request → WI normalization)       |
| prj         | Project ID (PRJ)                   | Project (product/service) identifier            | project_id                | branch (misleading) | TBD                   | E.g., `PRJ-...` (registry: project-registry.md)    |
| ctx         | Context ID (CTX)                   | Project context instance for parallel processing | context_id               | project switching   | TBD                   | E.g., `CTX-...` (registry: context-registry.md)    |
| eo          | Ensemble Overseer (EO)             | System governance (policy/audit/asset promotion) | governor                 | -                   | TBD                   | Final approval for policy/registry                 |
| re          | Reliability Engineer (RE)          | Independent verification/regression/reliability | verifier                 | self-verify         | TBD                   | "Independent verification" principle               |
| sa          | System Architect (SA)              | Architecture/structure decisions (ADR-centered) | architect                | -                   | TBD                   | MEDIUM/HIGH design                                 |
| tr          | Technology Researcher (TR)         | Investigation and evaluation of external latest technologies/tools/frameworks | tech scout, researcher | -                   | TBD                   | Technology trend monitoring, benchmarking, migration strategy |
| pg          | Privacy Guardian (PG)              | Security/sensitive information policy/inspection | security reviewer        | -                   | TBD                   | secrets/privacy                                    |
| consumer    | Consumer                           | Target using asset/contract (code/project/user) | user, dependent          | User (confusing)    | TBD                   | Specified in Registry Consumers field              |
| asset       | Asset                              | Reusable unit (policy/template/code/tool)       | resource                 | Resource (umbrella) | TBD                   | Identified by ID like "AST-POL-001"                |
| reuse       | Reuse-first                        | Principle of prioritizing reuse review before creating new | reuse priority           | Copy-paste          | TBD                   | Search→reuse/extend→promote sequence               |
| domain-fit  | Domain Fit                         | Whether reuse asset has no conflict with domain invariants/terms | domain suitability       | -                   | TBD                   | Verified in PROJECT_DOMAIN_CONTEXT.md              |
| deprecated  | Deprecated                         | Scheduled for deprecation (provide alternative path, migration needed) | scheduled for deprecation, end-of-life | Deleted (immediate deletion) | TBD                   | Follow VERSIONING_AND_DEPRECATION.md procedure     |
| stable      | Stable                             | Interface/behavior stabilized, backward compatibility guaranteed | stable, Production-ready | Final (change prohibited) | TBD                   | Draft → Stable → Deprecated sequence               |

## 3-A) ATStudio Domain Terms

| Key | Canonical Term | Definition | Synonyms (allowed) | Forbidden | External Standard Ref | Notes/Examples |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| music | Music | Shorts-ready audio content uploaded by creators | track, song, audio | file (too generic) | - | Core marketplace product |
| creator | Creator | User who uploads and sells music on the platform | uploader, artist, seller | user (ambiguous) | - | Supply side of marketplace |
| buyer | Buyer | User who searches and purchases music for shorts | purchaser, customer | user (ambiguous) | - | Demand side of marketplace |
| shorts | Shorts | Short-form video content (target use case for music) | short-form, clip | video (too generic) | - | YouTube Shorts, TikTok, Reels |
| genre | Genre | Music classification category | category, type | tag (different concept) | - | e.g., EDM, Lo-fi, Hip-hop |
| tag | Tag | User-defined keyword for music discovery | keyword, label | genre (different concept) | - | Free-form search metadata |
| license | License | Usage rights granted upon music purchase | permission, rights | copyright (broader) | - | Defines how buyer can use music |
| upload | Upload | Process of creator submitting music to platform | submit, publish | post (ambiguous) | - | Includes file validation |
| purchase | Purchase | Transaction where buyer acquires music license | buy, order, transaction | download (subset) | - | Includes payment processing |
| royalty | Royalty | Revenue share paid to creator per sale | commission, revenue share | profit (broader) | - | Platform takes commission % |
| playlist | Playlist | Curated collection of music tracks | collection, mix | album (different) | - | For discovery/recommendation |

### External Standard Ref Format (Recommended)

When basing on public standard XLSX (see `docs/standards/GLOSSARY_SOURCES.md`), recommend the following format.

- `PDS7:TERM:<number>`: Corresponding row in Common Standard Term sheet (headers: `number`, `Common Standard Term Name`, ...)
- `PDS7:WORD:<number>`: Corresponding row in Common Standard Word sheet
- `PDS7:DOMAIN:<number>`: Corresponding row in Common Standard Domain sheet

> Here, `<number>` is the "number" column value (1-based) in XLSX.

### Search/Verification (Auto-generated index)

Standard data can be quickly searched in auto-extracted index.

- Index: `docs/standards/public_data/standard_glossary/PDS7_INDEX.json`
- Regenerate: (To be added) Provided via `.claude/scripts/` or `.claude/skills/`

## 4) Addition/Change Procedure

When adding new term or changing existing term:

1. **Search existing terms** (check Key/Canonical Term duplication with grep/ctrl+F)
2. **Check conflicts**
   - Conflict with System (Global) glossary: Define separately in project glossary
   - Conflict between projects: Specify in Domain Context
3. **Create WI** (LOW OK, `WI-YYYYMMDD-###: Term addition/change`)
4. **Write Key/Canonical Term/Definition/Synonyms/Forbidden**
5. **Create PR + EO approval required**

**Responsibility:**

- Term addition/change proposal: Anyone (R)
- Conflict verification: PS (C), DocOps (C)
- Final approval: EO (A)
