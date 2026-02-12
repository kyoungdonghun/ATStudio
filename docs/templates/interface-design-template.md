---
version: 1.0
last_updated: 2026-01-29
project: system
owner: SA
category: template
status: stable
---

# [Design] Interface/API Design

> **@fileoverview**: Communication protocol design between agents or with external systems
> **Related WI**: [WI-ID]
> **Related ADR**: [ADR-ID]

---

## 1. Overview
- **Purpose**: Define data exchange methods and contracts between system components.
- **Communication Method**: (e.g., REST API, Claude Code Tool, Task/Skill, A2A Protocol, etc.)

## 2. Interface List

| ID | Name | Method | Description |
| :--- | :--- | :--- | :--- |
| INT-001 | GetUserInfo | GET / API | Query user information |

## 3. Detailed Specification (Endpoint/Tool)

### 3.1 [Interface Name]
- **Endpoint/Tool Name**:
- **Request Schema**:
```json
{
  "id": "string",
  "options": "object"
}
```
- **Response Schema**:
```json
{
  "status": "success",
  "data": {}
}
```

## 4. Error Code Definition
| Code | Message | Description |
| :--- | :--- | :--- |
| ERR-401 | Unauthorized | Authentication failed |
| ERR-404 | Not Found | Resource not found |

## 5. Security and Authentication
- Specify authentication methods such as API Key, OAuth2, Scoped Token, etc.
