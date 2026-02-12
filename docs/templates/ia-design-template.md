---
version: 1.0
last_updated: 2026-01-29
project: system
owner: PS/SA
category: template
status: stable
---

# [Design] Information Architecture Design

> **@fileoverview**: System data flow and logical hierarchy structure design
> **Related WI**: [WI-ID]

---

## 1. Overview
- **Purpose**: Design classification system for information handled by the system and information navigation paths for users/agents.
- **Tech Stack**: (e.g., Mermaid, Lucidchart, Draw.io, etc.)

## 2. Information Hierarchy
- Describe hierarchical structure of system's main data entities and functions.
```mermaid
graph TD
    Root[System Root] --> Auth[Authentication]
    Root --> Project[Project Management]
    Project --> WI[Work Items]
    Project --> Asset[Assets]
```

## 3. Data Flow
- Describe data movement and transformation process for main scenarios.
```mermaid
sequenceDiagram
    User->>MA: Request
    MA->>SE: Assign Task
    SE->>MA: Execute Tool
    MA-->>SE: Result
    SE-->>MA: Draft
```

## 4. Sitemap / Menu Structure (if needed)
- Define connection relationships between screens if UI is included.

## 5. Term and Concept Mapping
- Mapping between domain-specific terms and system entities.
| Domain Term | System Entity | Description |
| :--- | :--- | :--- |
| Work | Work Item | Minimum execution unit |
