---
version: 1.1
last_updated: 2026-04-15
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
| deprecated  | Deprecated                         | Scheduled for deprecation (provide alternative path, migration needed) | scheduled for deprecation, end-of-life | Deleted (immediate deletion) | TBD                   | Follow `docs/policies/versioning-policy.md` procedure |
| archived    | Archived                           | Preserved for historical/reference value only; not current SoT | historical, reference-only | deleted, current | TBD                   | Use with `archived_date` / `archive_reason`; see archive policy |
| stable      | Stable                             | Interface/behavior stabilized, backward compatibility guaranteed | stable, Production-ready | Final (change prohibited) | TBD                   | Draft → Stable → Deprecated → Archived sequence    |

## 3-A) ATStudio Domain Terms

| Key | Canonical Term | Definition | Synonyms (allowed) | Forbidden | External Standard Ref | Notes/Examples |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| track | Track | Shorts-ready audio content uploaded by creators and sold on the platform | music, song, audio | file (too generic) | - | Core marketplace product; DB table `tracks`, API `/api/tracks` |
| creator | Creator | User who uploads and sells tracks on the platform | uploader, artist, seller | user (ambiguous) | - | Supply side of marketplace |
| buyer | Buyer | User who searches and purchases tracks for shorts | purchaser, customer | user (ambiguous) | - | Demand side of marketplace |
| shorts | Shorts | Short-form video content (target use case for tracks) | short-form, clip | video (too generic) | - | YouTube Shorts, TikTok, Reels |
| genre | Genre | Track classification category | category, type | tag (different concept) | - | e.g., EDM, Lo-fi, Hip-hop |
| tag | Tag | Admin-managed category for track discovery and classification | keyword, label | genre (different concept) | - | Predefined categories managed by admin; not user-defined |
| usage-tag | Usage Guide Tag | Visible guide/search hashtag that describes the expected content use case for a track | usage hashtag, guide tag | license, usage license | - | Stored as `tags.type=USAGE`; examples: `#쇼츠용`, `#유튜브용`, `#릴스용` |
| license | License | Track usage rights automatically issued upon download | permission, rights | copyright (broader) | - | DB table `licenses`; issued per (user, track) pair |
| upload | Upload | Process of creator submitting a track to the platform | submit, publish | post (ambiguous) | - | Includes file validation and async preview generation |
| subscription | Subscription | Paid plan that grants download and playlist access | plan, membership | purchase (different) | - | DB table `user_subscriptions`; required for downloads |
| whitelist-channel | Whitelist Channel | YouTube channel profile saved by a member and optionally submitted for manual whitelist registration | channel | account (ambiguous) | - | DB table `whitelist_channels`; request/registered/removal states are limited by subscription plan |
| download-queue | Download Queue | Temporary collection of tracks queued for sequential download | cart (incorrect) | cart (no purchase concept) | - | DB table `download_queue`; frontend calls SOUND-011 per track |
| company-certification | Company Certification | Document review process for BUSINESS-type members to unlock subscription | corporate review | personal license (different) | - | DB table `company_certifications`; required before BUSINESS subscription |
| royalty | Royalty | Revenue share paid to creator per sale | commission, revenue share | profit (broader) | - | Platform takes commission % |
| playlist | Playlist | Curated collection of tracks for subscribers | collection, mix | album (different) | - | DB table `playlists`; subscriber-only feature |
| album-like | AlbumLike | A user's like action on an album; persisted as a composite-PK entity | album favorite | track like (different entity) | - | DB table `album_likes`; managed by `useAlbumLikeStore` on frontend |
| track-like | TrackLike | A user's like action on a track; persisted as a composite-PK entity | like, favorite | — | - | DB table `likes`; managed by `useLikeStore` on frontend; triggers `incrementLikeCount()` on Track entity |
| view-count | viewCount | Number of times a Notice has been viewed; incremented on each detail fetch | views | hits (too casual) | - | Field on `notices.view_count`; incremented by `notice.incrementViewCount()` in service layer |
| like-count | likeCount | Denormalized count of likes on a Track or Album; kept in sync via domain methods | likes | — | - | Fields `tracks.like_count`, `albums.like_count`; updated by `incrementLikeCount()` / `decrementLikeCount()` |
| download-count | downloadCount | Denormalized count of times a Track has been downloaded | downloads | — | - | Field `tracks.download_count`; updated by `incrementDownloadCount()` in service layer |
| subscriber-route | SubscriberRoute | A React route guard that verifies the user has an active subscription before rendering | subscription guard | ProtectedRoute (different — role-based) | - | `frontend/src/router/SubscriberRoute.tsx`; redirects inactive users to `/subscriptions` |

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
