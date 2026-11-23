# WI-20261123-ATS-011 · Combined catalog tags

Parent [REQ-20261123-ATS-002](../user/REQ-20261123-ATS-002.md). The user's November 23 follow-up selects the final local combined desktop cell and adds instruments to it. Frontend engineer with local QA. Depends on WI010. No blocked successor.

## Acceptance

Use the merged desktop column, header, chip appearance and corresponding responsive sizing from `f0c73d4`. Display genre, instrument and mood in that cell. Preserve the current mobile layout, usage line, filter expansion, search and pilot actions. Verify focused row and page regressions plus the production-mode frontend build. Check the combined column at desktop and intermediate widths and the current phone view. This change does not request a per-row expansion control.

## Inputs and deliverables

Tier 0: `docs/standards/core-principles.md`, `docs/standards/development-standards.md`, `docs/standards/documentation-standards.md`, `docs/standards/glossary.md`. Tier 1: `docs/policies/quality-gates.md`. Tier 2: `.agents/skills/react-best-practices/SKILL.md`, `docs/ui/mockup/track-list.html`, the parent REQ, and WI006, WI009 and WI010. The historical TrackListPage and TrackRow source and CSS are the visual reference. Scope changes to the current versions of those components, styles and matching tests. Report commands, checks and rollback in the sibling evidence pack and user summary. Keep unrelated local changes intact.
