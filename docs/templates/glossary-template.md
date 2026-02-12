---
version: 1.0
last_updated: 2026-01-29
project: system
owner: PS
category: template
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: System global glossary reference
  - path: domain-context-template.md
    reason: Domain context template reference
  - path: ../work-items/examples/project-glossary-example.md
    reason: Example reference
---
# Project Glossary

> Purpose: Define terms used in this project and prevent conflicts with System (Global) glossary.
>
> **Example:** See `docs/work-items/examples/project-glossary-example.md`

## 1) Operating Rules

- This file is stored in `docs/project/glossary.md` (or equivalent location) in **project repo**.
- If **conflict** with System (Global) glossary (`agentic-subagent-team/docs/standards/glossary.md`):
  - Specify "meaning in this project" in this file
  - Reflect in Terminology section of `docs/templates/domain-context-template.md`
- For tags/keywords, use **Key (standard term key)** when possible

## 2) Term List

| Key | Canonical Term | Definition | Synonyms (allowed) | Forbidden | Notes |
| :-- | :------------- | :--------- | :-------------- | :--------------- | :---- |
|     |                |            |                 |                  |       |

### Example

| Key    | Canonical Term | Definition                           | Synonyms   | Forbidden    | Notes                        |
| :----- | :------------- | :----------------------------------- | :--------- | :----------- | :--------------------------- |
| order  | Order | Purchase request with completed payment (not cart) | purchase | cart     | In this project, only post-payment |
| user   | User | Account holder who logged in | member       | customer (non-member) | Non-members distinguished as Guest       |

## 3) Conflict Record with System (Global) Terms

> List terms used differently from Global glossary below.

| Key | Global meaning | Meaning in this project | Conflict reason |
| :-- | :---------- | :--------------------- | :-------- |
|     |             |                        |           |
