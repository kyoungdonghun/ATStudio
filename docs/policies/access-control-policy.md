---
version: 1.0
last_updated: 2026-01-06
project: system
owner: PG
category: policy
status: stable
dependencies:
  - path: execution-policy.md
    reason: Execution policy reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage baseline
---
# Access Control Policy (Definition First, Implementation Later)

> Purpose: Fix tool/repository/deployment permissions based on "least privilege + approval" principles.
> In Phase 1, we only define the policy; automated permission checks and periodic reviews will happen in Phase 2+.

## 1) Principles

- **Least Privilege**: Grant only the minimum necessary permissions
- **Separation of Duties**: Separate execution (tool execution) from verification (RE) and approval (user/MA/EO)
- **Default Deny**: Deny permissions that are not explicitly granted

## 2) Permission Classification (Definition)

Permissions are classified based on the nature of the execution action:

- **Read-only**: View/search/read (file reading, directory traversal, etc.)
- **Write**: File modification/creation (non-destructive) (file creation, content modification, etc.)
- **Destructive**: Deletion/bulk changes/initialization/force (file deletion, bulk overwriting, etc.)
- **Network**: External network calls/downloads/deployments (API calls, package downloads, etc.)

> **HITL Approval Policy**: For approval procedures for Destructive/Network execution, refer to [execution-policy.md](execution-policy.md)

## 3) Minimum Operating Rules (Definition)

- By default, execution starts as **Read-only**.
- If **Write** or higher permissions are required, the approval procedures in [execution-policy.md](execution-policy.md) must be followed.

## 4) Implementation Order (Explicit)

- Phase 1 (MVP): Fix permission classification/approval rules only (manual approval)
- Phase 2+: Regular permission reviews/automated policy checks (e.g., "prohibit destructive actions without approval" verification)


