---
version: 1.1
last_updated: 2026-01-06
project: system
owner: EO
category: registry
status: stable
---

# Standards Document Index

> Purpose: Detailed list of standard documents

## Document List

| Document Name | Project | Description | Status |
|---------------|---------|-------------|--------|
| [Documentation Standards](documentation-standards.md) | system | Documentation writing standards | stable |
| [Development Standards](development-standards.md) | system | Development standards | stable |
| [Glossary](glossary.md) | system | Glossary (Canonical Terms) | stable |
| [Glossary Sources (Official)](glossary-sources.md) | system | Glossary sources and references | stable |
| [Pricing Sources (Official)](pricing-sources.md) | system | Pricing information sources and references | stable |
| [System Constitution](core-principles.md) | system | System Constitution (Core Principles) | stable |
| [Prompt Caching Strategy](prompt-caching-strategy.md) | system | Prompt caching strategy | stable |
| [Standard Evolution Procedure and Patterns](evolution-pattern.md) | system | System evolution patterns (Subagent Evolution) | stable |
| [Evidence Pack Standard](evidence-pack-standard.md) | system | Evidence Pack format standard | stable |
| [DTO Standards](dto-standards.md) | ATS | Entity/DTO separation, ResponseDTO, RequestDTO | stable |
| [Exception Handling Standards](exception-handling.md) | ATS | Business/Technic exceptions, GlobalExceptionHandler | stable |
| [Frontend Standards](frontend-standards.md) | ATS | React + TypeScript architecture (Phase 2) | draft |

## Document Dependencies

- **documentation-standards.md**: Basis for all documentation, references glossary.md
- **development-standards.md**: Development standards, references glossary.md
- **glossary.md**: Standard term basis, referenced by all documents
- **glossary-sources.md**: Source information for glossary.md
- **dto-standards.md**: References development-standards.md (layer architecture), exception-handling.md
- **exception-handling.md**: References development-standards.md, dto-standards.md (ExceptionResponseDTO)
- **frontend-standards.md**: References development-standards.md, dto-standards.md, exception-handling.md
