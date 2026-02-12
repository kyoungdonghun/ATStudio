---
version: 1.2
last_updated: 2026-02-01
project: system
owner: EO
category: standard
status: stable
dependencies:
  - path: glossary.md
    reason: Term usage standards
tier: 0
target_agents:
  - "*"
task_types:
  - documentation
---

# Documentation Standards

> **Universal Standard**: This standard defines common documentation rules applicable to all projects.
> Project-specific content should be minimized, focusing on general documentation principles.

---

## Metadata

```yaml
---
version: 1.2
last_updated: 2026-02-01
project: system
owner: EO
category: standard
status: stable
dependencies:
  - path: docs/standards/glossary.md
    reason: Term usage standards
---
```

---

## 1. Document Structure Standards

### 1.1 Document Metadata (Required)

All documents must include the following metadata at the top:

```yaml
---
version: 1.0
last_updated: YYYY-MM-DD
project: [PRJ-...] | system | (optional, required for project-related docs)
owner: [role or assignee]
category: plan | guide | policy | standard | template | registry | agent
status: draft | stable | deprecated
dependencies:
  - path: [relative path]
    reason: [dependency reason]

# Optional: Dynamic context injection fields
tier: 0                    # 0=Constitution, 1=Policy, 2=Guide, 3=Reference
target_agents:             # Agents that should always receive this document
  - eo
  - sa
task_types:                # Task types that require this document
  - security
  - architecture
---
```

**Required Fields (7 fields):**

- **version**: Document version (Semantic Versioning recommended)
- **last_updated**: Last modification date (YYYY-MM-DD format)
- **project**: Project identifier
  - `PRJ-...`: Specific project document (required for project-related docs)
  - `system`: System-wide document (standards, policies, guides, etc.)
- **owner**: Document owner/assignee (role or name)
- **category**: Document category (see classification below)
- **status**: Document status (`draft` / `stable` / `deprecated`)
- **dependencies**: List of documents this document depends on

**Optional Fields (3 fields for dynamic context injection):**

- **tier**: Context injection priority tier (integer 0-3)
  - `0` = Constitution: System constitution, absolute standards (always injected to all agents)
  - `1` = Policy: Policies, architecture, decisions (injected to governance/architecture agents)
  - `2` = Guide: Guides, templates, procedures (injected on-demand per task type)
  - `3` = Reference: Analysis, eval, work items (injected only when explicitly requested)
  - Default: Determined by rule-based pattern matching if not specified
- **target_agents**: Array of agent names that should always receive this document
  - Valid values: `ps`, `eo`, `sa`, `se`, `re`, `pg`, `tr`, `uv`, `docops`, `qa`, `cr`
  - Default: Empty array (no specific agent targeting)
  - Purpose: Ensures specific agents always receive this document regardless of task type
- **task_types**: Array of task types that require this document
  - Valid values: `security`, `architecture`, `testing`, `documentation`, `implementation`, `review`, `research`, `design`
  - Default: Empty array (no task type association)
  - Purpose: Automatically injects document when matching task type is detected

**Phase 2+ Optional Extended Fields:**

The following fields have been replaced by trigger-based on-demand injection (`.claude/config/context-triggers.json`) or are rarely used in actual files. They can be restored in Phase 2+ if needed.

- `audience` — Reader distinction (user / agent / both)
- `keywords` — Injection/search hint keywords
- `inject` — Declarative injection hints by Subagent
- `related_work_item` — Related WI reference
- `related_decision` — Related ADR reference

**Project Identification Rules:**

- **Project-related documents**: `project: {identifier}` required (e.g., project-specific plan, agent documents, workboard)
- **System-wide documents**: `project: system` or omitted (e.g., standards, policies, guides, templates)
- **Project ID format**: Three-letter code (e.g., `ATS`, `AMS`, `ECP`)
  - Format: Three uppercase letters (e.g., `ATS`, `AMS`, `ECP`)
  - Rule: Must be unique, duplication check in `docs/registry/project-registry.md` required
  - Recommended: Project name-based initials (e.g., "ATStudio" → `ATS`, "Agentic System" → `AS`, "E-commerce Platform" → `ECP`)
- **Project ID source**: Use Project ID registered in `docs/registry/PROJECTS.md`

**Project Identifier Management and Issuance Procedure:**

Project identifiers are centrally managed in the system project. When a new project arises, follow this procedure:

1. **New Project Request**: Create request using `project-request-template.md`

   - Template: `docs/templates/project-request-template.md`
   - Guide: See `docs/guides/request-intake.md`

2. **Project Identifier Issuance**: EO issues Project ID

   - Format: Three uppercase letters (e.g., `AMS`, `ECP`, `WEB`)
   - Issuance timing: After project creation request approval
   - Issuer: EO
   - Issuance rules:
     - **Format**: Three uppercase letters (e.g., `ATS`, `AMS`, `ECP`)
     - **Recommended**: Use project name-based initials
       - Example: Project name "ATStudio" → `ATS`
       - Example: Project name "Agentic System" → `AS`
       - Example: Project name "E-commerce Platform" → `ECP`
     - **Duplicate prevention**: Must check existing IDs in `docs/registry/project-registry.md` before issuance
     - **Uniqueness**: All project IDs must be unique across the entire system

3. **Project Registry Registration**: Register in `docs/registry/project-registry.md`

   - Required info: Project ID, Name/Alias, Repo (use `TBD` if none), Status, Owner, Last Updated
   - Registration handler: EO
   - Registration timing: Immediately after Project ID issuance

4. **Use Project Identifier in Document Creation**:
   - When creating project-related documents, `project: {identifier}` required in metadata (e.g., `project: AMS`)
   - Only use Project IDs registered in `docs/registry/project-registry.md`
   - Using unregistered Project IDs is prohibited

**Project Identifier Use Cases:**

Project identifiers (three-letter codes, e.g., `AMS`) are used for:

1. **Document Metadata**: `project` field in document frontmatter

   - Example: `project: AMS` or `project: system`
   - Purpose: Identify which project the document belongs to

2. **Project Registry**: Registered in `docs/registry/project-registry.md`

   - Purpose: Manage project list and serve as source of truth for project selection

3. **Context Instance**: Project reference in `docs/registry/context-registry.md`

   - Purpose: Specify which project a context instance belongs to for parallel processing

4. **Requests/Requirements**: Specify project in request templates

   - Purpose: Indicate which project a request pertains to

5. **Index Files**: Distinguish documents by project in document indexes
   - Purpose: Provide project-specific document lists

**Project Identifier and File Names:**

- **Work Item/Requirement Files**: Must include project identifier in filename

  - Format: `{identifier}_WI-YYYYMMDD-###.md` (e.g., `AMS_WI-20250101-001.md`, `ECP_WI-20250101-002.md`)
  - Applies to: Work Item (WI), Impact Analysis Sheet, and other work item/requirement files
  - Location: `docs/project/WI-YYYYMMDD-###.md` in project repo
  - Purpose: Managed together with project context, integrated by project in Workboard
  - Rule: WI files are located in project repo, so project identifier in filename is unnecessary

- **General Document Files**: Do not include project identifier in filename

  - Format: `kebab-case.md` (e.g., `system-design.md`, `operation-process.md`, `glossary.md`)
  - Applies to: Architecture, Guide, Policy, Standard, Template, Registry, Agent documents, etc.
  - Purpose: General documents identify project through `project` field in metadata
  - Rule: General documents do not include project ID in filename (managed through metadata)

- **Document Number/ID**: Not used as document's unique number

  - Documents are identified by filename, no separate document numbering system
  - Project identifier indicates which project the document belongs to

- **WI/ADR/REQ Identifiers**: Separate from work item, decision record, and request identifiers
  - WI: `WI-YYYYMMDD-###` (work item)
  - ADR: `ADR-YYYYMMDD-###` (architecture decision record)
  - REQ: `REQ-YYYYMMDD-###` (request)
  - Project Identifier: Three-letter code (e.g., `AMS`, `ECP`) (project)
  - Each is a distinct identifier with different purpose and scope

**Related Documents:**

- Project Registry: `docs/registry/project-registry.md`
- Project Creation Template: `docs/templates/project-request-template.md`
- Request Intake Guide: `docs/guides/request-intake.md`

**Notes:**

- `related_work_item`, `related_decision` should follow the format used by the project
- `owner` should be a role name or assignee name

---

### 1.2 Document Header Structure

All documents should follow this structure:

```markdown
# Document Title

> Purpose: One-sentence description of the document's purpose

---

## 1. Overview (or Purpose)

Clearly explain the document's purpose and scope.

## 2. Main Content

Explain the core content of the document.

## 3. Notes

Explain additional information or precautions.

## Related Documents

- [Document Name](relative path): Description
- [Document Name](relative path): Description
```

**Header Rules:**

- Title should be clear and concise
- Purpose summarized in one sentence
- Section titles should reveal "what" they explain

---

### 1.3 Section Structure Rules

- **Maximum 3-level depth**: Use only `##`, `###`, `####`
- **Clear section titles**: Should reveal "what" is explained
- **Use checklists**: Use checkbox format for task lists
- **Code examples**: Use code blocks when needed (specify language)

**Example:**

```markdown
## 1. Main Features

### 1.1 Feature A

#### 1.1.1 Details

- [ ] Task 1
- [ ] Task 2
```

---

## 2. Document Style Guide

### 2.1 Language and Tone

- **Positive phrasing**: Use "Use B" instead of "Not A, but B"
- **Clear terms**: Use Canonical Terms from the project Glossary
- **Consistent tone**: Maintain consistency in directive/explanatory style
- **Conciseness**: Remove unnecessary modifiers

**Example:**

```markdown
✅ Good: "Track work with WI"
❌ Bad: "Track work with WI, not issues"

✅ Good: "EX is responsible for execution"
❌ Bad: "Execution is done by EX, not by other roles"
```

---

### 2.2 Format Rules

- **Code blocks**: Specify language (e.g., ` ```typescript`, ` ```yaml`)
- **Links**: Use relative paths (e.g., `[Document Name](../guides/operation-process.md)`)
- **Tables**: Use Markdown table format
- **Emojis**: Prohibited (for consistency)
- **Emphasis**: Use `**bold**` or `*italic*`

**Example:**

````markdown
```typescript
// Code example
const example = "code";
```

[Related Document](../guides/operation-process.md)
````

---

### 2.3 Term Usage Rules

- **Use Canonical Terms**: Use standard terms from the project Glossary
- **No synonyms**: Do not use synonyms registered in the Glossary
- **No forbidden terms**: Do not use forbidden terms specified in the Glossary

**Checklist:**

- [ ] Check Glossary before using new terms
- [ ] Use standard terms
- [ ] Do not use synonyms/forbidden terms

---

## 3. Document Links and Reference Rules

### 3.1 Inter-document Links

- **Use relative paths**: Absolute paths prohibited
- **Clear link text**: Use document name instead of "here", "this document"
- **Related documents section**: Include "Related Documents" section at document bottom

**Example:**

```markdown
✅ Good: [Process Guide](../guides/operation-process.md)
❌ Bad: [here](../guides/operation-process.md)
❌ Bad: https://github.com/.../docs/guides/operation-process.md
```

---

### 3.2 External References

- **Prefer official links**: Use official documentation/source links
- **Link validity**: Regularly verify link validity
- **Link description**: Provide external links with description

**Example:**

```markdown
[Claude Code Documentation](https://docs.anthropic.com/claude/docs/claude-code) - Official Claude Code documentation
```

---

### 3.3 Related Documents Section

All documents should include a "Related Documents" section at the bottom:

```markdown
## Related Documents

### Required References

- [Document Name](path): Reason

### Reference Documents

- [Document Name](path): Reason

### Dependent Documents (documents that reference this document)

- [Document Name](path): Reason
```

---

## 4. Document Classification and Naming Rules

### 4.1 Document Classification

Documents are classified into the following categories:

- **architecture/**: Design/roadmap documents
- **guides/**: Practical guide documents
- **policies/**: Operational policy documents
- **standards/**: Standard documents
- **templates/**: Template documents
- **registry/**: Registry documents
- **design/agents/**: Role-specific documents
- **projects/**: Project-specific document containers

**Category Characteristics and Locations:**

| Type | Description | Path Pattern | Example |
| :--- | :--- | :--- | :--- |
| **System Docs** | Documents defining the agent system itself | `docs/{category}/` | `docs/standards/glossary.md` |
| **Project Docs** | Documents for 'target projects' agents work on | `docs/projects/{ProjectID}/` | `docs/projects/DTX/specs/req.md` |

> **Directory Strategy**:
>
> - **System (Meta)**: Category folders under `docs/` root describe "the agent system (this tool)".
> - **Project (Product)**: Documents for actual development targets (e.g., Diet App) must be isolated under `docs/projects/{ProjectID}/`.

**Category Characteristics:**

| Category     | Purpose                              | Metadata `project` field rule       | Example                                       |
| ------------ | ------------------------------------ | ----------------------------------- | --------------------------------------------- |
| architecture | Overall design and roadmap           | Project-specific: `PRJ-...` required | system-design.md                              |
| guide        | Practical guides and procedures      | System-wide: `system` or omitted    | operation-process.md, traceability.md         |
| policy       | Operational policies and rules       | System-wide: `system` or omitted    | quality-gates.md, execution-policy.md         |
| standard     | Standards and specifications         | System-wide: `system` or omitted    | glossary.md, documentation-standards.md       |
| template     | Document/artifact templates          | System-wide: `system` or omitted    | adr-template.md, impact-analysis-template.md  |
| registry     | Asset/capability registries          | System-wide: `system` or omitted    | asset-registry.md, project-registry.md        |
| agent        | Role-specific documents              | Project-specific: `PRJ-...` required | se.md (project-specific)                      |

---

### 4.2 Naming Rules

**File Name Format:**

- **Work Item/Requirement Files**: `WI-YYYYMMDD-###.md`

  - Example: `WI-20250101-001.md`, `WI-20250101-002.md`
  - Applies to: Work Item (WI), Impact Analysis Sheet, and other work item/requirement files
  - Location: `docs/projects/{ProjectID}/work-items/` (refined from previous `docs/project`)
  - Purpose: Managed together with project context, integrated by project in Workboard
  - Rule: WI files are located in project folder, so project identifier in filename is unnecessary

- **General Document Files**: `kebab-case.md`

  - Example: `system-design.md`, `operation-process.md`, `glossary.md`, `quality-gates.md`
  - Applies to: Architecture, Guide, Policy, Standard, Template, Registry, Agent documents, etc.
  - Purpose: General documents identify project through `project` field in metadata
  - Rule: General documents do not include project ID in filename

- **Index File Exception**: Index files are standardized as `index.md`

**Naming Rules:**

- **Consistency**: Use same naming pattern for same category
- **Clarity**: Content should be discernible from filename alone
- **Project identification**: Project-specific documents must include project ID in filename

**Naming Examples:**

```
✅ Good (work item): WI-20250101-001.md (located in docs/project/ in project repo)
✅ Good (work item): WI-20250101-002.md (located in docs/project/ in project repo)
✅ Good (general doc): system-design.md
✅ Good (general doc): operation-process.md
✅ Good (index): index.md
❌ Bad: AMS_WI-20250101-001.md (WI is in project repo, so project identifier unnecessary)
❌ Bad: plan.md (unclear)
❌ Bad: agent_plan_v2.md (version inclusion prohibited)
❌ Bad: AMS AGENT_PLAN.md (space inclusion prohibited)
```

---

### 4.3 Document Index Files

Maintain index files to help agents quickly find needed documents.

**Index Strategy: Role Separation Approach**

To reduce duplicate management burden, separate roles of main index and category indexes:

- **Main Index**: Overview/mapping role (document count by category, required documents mapping by role)
- **Category Index**: Detailed list role (actual document list and descriptions)

#### 4.3.1 Main Index (Required)

**Location:** `docs/index.md`

**Purpose:** Provide overview of overall document structure and document mapping by role/project

**Structure (Keep concise):**

```markdown
# Documentation Index

> Purpose: Overview of overall document structure and document mapping by role/project

## Document Overview by Category

| Category     | Document Count | Index File                                     | Description          |
| ------------ | -------------- | ---------------------------------------------- | -------------------- |
| Architecture | 5              | [architecture/index.md](../architecture/index.md) | Design/roadmap docs  |
| Guides       | 11             | [guides/index.md](../guides/index.md)             | Practical guide docs |
| Policies     | 11             | [policies/index.md](../policies/index.md)         | Operational policy docs |

...

## Required Documents Mapping by Role

### MA (Main Agent)

- Required: [request-intake.md](../guides/request-intake.md), [operation-process.md](../guides/operation-process.md)
- Reference: [system-design.md](../architecture/system-design.md)
- Detailed list: See [guides/index.md](../guides/index.md)

### SE (Software Engineer)

- Required: [development-standards.md](development-standards.md)
- Reference: [system-design.md](../architecture/system-design.md)
- Detailed list: See [standards/index.md](index.md)

...

## Project Document Overview

### PRJ-001

- Architecture documents: See project section in [architecture/index.md](../architecture/index.md)
- Subagents(SoT): See `.claude/agents/`
- Design reference: See [design/index.md](../design/index.md)
```

**Update Rules:**

- Update only document count by category (detailed lists managed in category indexes)
- Maintain only required document mapping by role
- For project-specific documents, refer to project section in category indexes

#### 4.3.2 Category Index (Required)

**Location:** `index.md` in each category folder (e.g., `docs/guides/index.md`)

**Purpose:** List **all documents** in the category without omission and provide 1-line summaries to aid navigation.

**Structure (Standard Template):**

```markdown
# [Category Name] Document Index

> Purpose: Guide to all documents in this directory (`docs/[category]/`).

## 1. Document List

| Document Name | Description | Status |
| :--- | :--- | :--- |
| **[file-name.md](file-name.md)** | 1-line summary of core purpose | stable |
| ... | ... | ... |

## 2. Document Map (Guidelines)

(Optional: Explain relationships between documents or reading order)

---

## Metadata
```yaml
---
version: 1.0
last_updated: YYYY-MM-DD
project: system
owner: EO
category: registry
status: stable
---
```
```

**Update Rules:**
- **Sync**: When files are added/deleted in folder, `index.md` must be updated **immediately**.
- **Coverage**: All `.md` files except hidden files (`.*`) must be in the list.

---

## 5. Document Version Control

### 5.1 Version Rules

Follow Semantic Versioning:

- **Major (1.0)**: Structural changes, backward compatibility broken
- **Minor (0.1)**: Content additions, backward compatibility maintained
- **Patch (0.0.1)**: Typo fixes, link fixes

**Version Change Examples:**

- `1.0.0` → `1.1.0`: New section added
- `1.0.0` → `2.0.0`: Structural changes (section reorganization)
- `1.0.0` → `1.0.1`: Typo fixes

---

### 5.2 Change History

- **Change history document**: Record major decision/change history
  - Example: `design-history.md`, `CHANGELOG.md`
- **In-document change history**: Add change history section in document for Major changes (optional)

**Change History Example:**

```markdown
## Change History

| Version | Date       | Changes                      |
| ------- | ---------- | ---------------------------- |
| 1.0.0   | 2025-01-01 | Initial creation             |
| 1.1.0   | 2025-01-15 | Added reference document tracking system |
```

---

## 6. Document Template Standards

### 6.1 Template Structure

Provide templates for each document type:

- **Architecture Document Template**: For design/roadmap documents
- **Guide Document Template**: For practical guide documents
- **Policy Document Template**: For operational policy documents
- **Agent Document Template**: For role-specific documents (optional)

**Template Location:** `docs/templates/`

---

### 6.2 Template Usage Rules

- **Follow templates**: Use corresponding template when creating new documents
- **Template changes**: Comply with template governance policy

---

## 7. Reference Document Tracking System

### 7.1 Document Creation Workflow

**Phase 1 (Manual Checklist):**

```
1. Start document creation
2. Manually search related documents in document index or registry
3. Check for similar content documents (prevent duplication)
4. Identify and specify dependency relationships (metadata dependencies field)
5. Add links in related documents section
```

**Checklist:**

**Before Document Creation:**

- [ ] Search related documents in document index (`docs/index.md` or category `index.md`)
- [ ] Check for similar content documents (prevent duplication)
- [ ] Verify project identifier (project-related docs require `project: PRJ-...`)
- [ ] Identify and specify dependency relationships (metadata)
- [ ] Prepare related documents section

**During Document Creation:**

- [ ] Use Glossary terms (if project Glossary exists)
- [ ] Follow document structure standards
- [ ] Follow link format (relative paths)

**After Document Creation:**

- [ ] Verify no duplicate content
- [ ] Verify project identifier (`PRJ-...` or `system`)
- [ ] Verify reference document links
- [ ] Verify dependency relationships
- [ ] Verify metadata completeness
- [ ] Complete related documents section
- [ ] Update index files (`docs/index.md` and category `index.md`)

---

### 7.2 Specify Inter-document Dependencies

**Dependencies field in metadata:**

```yaml
---
dependencies:
  - path: docs/guides/operation-process.md
    reason: Reference work process
  - path: docs/policies/quality-gates.md
    reason: Reference quality gate standards
---
```

**Dependency Rules:**

- **Required dependencies**: Documents that must be read to understand this document
- **Reference dependencies**: Documents helpful to reference
- **Prevent circular dependencies**: Prohibit circular references like A → B → A

---

### 7.3 Related Documents Section Auto-generation

Include the following section at document bottom:

```markdown
## Related Documents

### Required References

- [Document Name](path): Reason

### Reference Documents

- [Document Name](path): Reason

### Dependent Documents (documents that reference this document)

- [Document Name](path): Reason
```

**Dependent documents can be managed manually or automated in Phase 2+**

---

## 8. Prevent Duplicate Content

### 8.1 Duplicate Detection Method (Phase 1)

**Manual Check:**

1. Before document creation: Search similar documents in registry/index
2. During document creation: Check related documents section
3. After document creation: Verify no duplicate content

**Duplicate Criteria:**

- Documents addressing same topic
- Documents with similar content exceeding 50%
- Documents with overlapping purposes

---

### 8.2 Duplicate Resolution Method

**When Duplication Found:**

1. Extend existing document: Add content to existing document
2. Merge documents: Consolidate multiple documents into one
3. Separate documents: Maintain separate documents if purposes differ (specify dependencies)

---

## 9. Document Creation Guidelines by Role

### 9.1 Document Creation Roles

Main roles creating documents:

- **MA (Main Agent)**: Process, guide documents
- **PS (Product Strategist)**: Domain, requirements documents
- **SE (Software Engineer)**: Development standards, technical documents
- **EO (Ensemble Overseer)**: Policy, standard documents
- **Other roles**: Create documents as needed

---

### 9.2 Role-specific Checklists

**Common Checklist:**

- [ ] Follow documentation standards
- [ ] Complete metadata
- [ ] Use Glossary terms
- [ ] Complete related documents section

**Role-specific Additional Checklists:**

- **MA**: Process documents should reference operation-process.md
- **SE**: Development standards should reference development-standards.md
- **EO**: Policy documents should reference template-governance.md

---

## 10. Quality Assurance & Automation

Document integrity is fundamentally about "building it right from the start", but automated tools are used auxiliarily to prevent omissions/errors.

### 10.1 Document Integrity Audit

- **Tool**: (To be added) `.claude/scripts/audit_docs.py` or `.claude/skills/` based audit skill
- **Purpose**: Support "burden-free management" by periodically detecting mechanical errors (broken links, missing metadata, forbidden terms).
- **Execution**:
  ```bash
  # Phase 2+: Execute after audit script/skill introduction
  # (Example) python3 .claude/scripts/audit_docs.py
  ```
- **Verification Items**:
  1. **Metadata**: YAML Frontmatter existence
  2. **Links**: Relative path (`../`) validity check
  3. **Terms**: Usage of forbidden terms like `MCP`, `Node`
  4. **Index**: Whether sibling files are registered in `index.md`

### 10.2 Future Improvements (Phase 2+)

10.2.1 RAG-based Document Search (Review Deferred)

**Review Timing:** Phase 2+ (after documentation standards establishment and stabilization)

**Review Items:**

- RAG introduction pros/cons analysis
- Vector DB structure design (document embedding)
- Document search API design
- Duplicate detection automation

**Note:** Current manual checklist is sufficient; review scheduled after documentation standards establishment

---

## Related Documents

### Required References

- [Glossary](glossary.md): Term usage standards for document creation
- [Template Governance](../policies/template-governance.md): Template change policy

### Reference Documents

- [Process Guide](../guides/operation-process.md): Document creation process
- [Registry](../registry/asset-registry.md): Document search and reuse

---

**Last Updated:** 2025-01-01
**Status:** Stable
