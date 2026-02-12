---
version: 2.0
last_updated: 2026-01-15
project: ATS
owner: SE
category: standard
status: stable
dependencies:
  - path: glossary.md
    reason: Standard term usage
  - path: documentation-standards.md
    reason: Documentation standards reference
tier: 0
target_agents:
  - sa
  - se
  - qa
  - cr
task_types:
  - implementation
  - review
---

# Development Standards

> **Purpose:** Define development/maintenance standards for ATStudio (Java/Spring Boot backend) and system scripts (Python), aligned with "File-Based Handoff" architecture and "Strict Governance".

---

## 1. Architecture Principles

### 1.1 Persona-based Context (No Class Hierarchy)

Our agents are defined by **documents (Context/Persona)**, not **classes**.

- **Definition (SoT):** `.claude/agents/{role}.md` (Subagent/persona defined in Markdown)
- **Runtime Injection:** MA includes minimal necessary documents as "instruction packet (context packet)" when calling Subagent to assign role (Context).
- **Composition:** Connect only necessary tools through composition instead of inheritance.

### 1.2 Dual-Language Strategy

| Layer | Language | Location | Purpose |
|-------|----------|----------|---------|
| **Application** | Java 17 + Spring Boot 4.x | `src/main/java/` | ATStudio business logic |
| **System Scripts** | Python 3.10+ | `.claude/scripts/` | Agent tooling, automation |
| **Frontend (Planned)** | React + TypeScript | TBD | SPA frontend (Phase 2) |

**Application Code (Java):**
- Framework: Spring Boot 4.x with Gradle
- Standards: See Section 2A (Java/Spring Boot Coding Standards)

**System Scripts (Python):**
- **Location:** `.claude/scripts/` directory
- **Style:** PEP 8 compliance, Type Hinting required
- **Execution:** Must be executable in `python3 .claude/scripts/...` format (Argparse recommended)

### 1.3 Context Injection & Safety (Tier 0 Check)

All agents must ensure **Tier 0 documents (constitution)** are included in their context before starting work.

- **Tier 0 (Mandatory):** `docs/standards/core-principles.md`, `docs/standards/development-standards.md`
- **Pre-flight Check (Enforcement):**
  - MA ensures Tier 0 documents are injected in every handoff packet via `/create-wi-handoff-packet` skill
  - Subagents must confirm Tier 0 documents are present in their input context before executing
  - If Tier 0 context is missing, the agent must halt and report the omission to MA
- **Traceability:** Agents must log the IDs of documents they referenced in their work logs.

### 1.3.1 Enhanced Traceability (Detailed tracking even from summaries)

Below are **minimum rules** to make "MA able to track details even when receiving only summaries", added only within the scope that doesn't conflict with existing rules.

- **Evidence Pointer First**: Leave **pointers (file/line/command/log/path)** before "explanations".
- **Two-set deliverables**: Separate User-facing and Agent-facing.
  - Recommended locations: `deliverables/user/`, `deliverables/agent/`
- **Subagent result contract**: Subagent includes "evidence pointers + reproduction/test (when possible)" in results.

### 1.4 Project Directory Structure

ATStudio is a single-project workspace. Agent system and application code coexist.

```
ATStudio/
├── .claude/                          # System: Agent tools and runtime
│   ├── agents/                       # Subagent persona definitions
│   ├── skills/                       # Skill definitions
│   └── config/                       # workspace.json, injection rules
├── docs/                             # System: Standards, policies, guides
│   └── standards/                    # Tier 0 documents
├── deliverables/                     # REQ/WI artifacts
│   ├── user/                         # User-facing deliverables
│   └── agent/                        # Agent-facing deliverables
├── src/
│   ├── main/java/com/atstudio/atstudio/
│   │   ├── config/                   # Spring configuration
│   │   ├── controller/               # REST API endpoints
│   │   ├── dto/                      # Request/Response DTOs
│   │   ├── entity/                   # JPA entities
│   │   ├── repository/               # JpaRepository interfaces
│   │   └── service/                  # Business logic
│   ├── main/resources/               # application.yml, templates/
│   └── test/java/                    # JUnit5 tests
├── build.gradle                      # Gradle build config
└── CLAUDE.md                         # Project instructions
```

---

## 2A. Java/Spring Boot Coding Standards (ATStudio Application)

### 2A.1 General Rules

- **Java Version:** 17 (use records, sealed classes, text blocks, pattern matching)
- **Framework:** Spring Boot 4.x
- **Build Tool:** Gradle
- **Lombok:** Allowed (`@Getter`, `@RequiredArgsConstructor`, `@Builder`, `@NoArgsConstructor`)
- **Encoding:** UTF-8

### 2A.2 Layer Architecture

| Layer | Annotation | Responsibility | Rule |
|-------|-----------|---------------|------|
| Controller | `@RestController` | HTTP endpoint, thin | No business logic |
| Service | `@Service`, `@Transactional` | Business logic | Core logic here |
| Repository | `@Repository` (implicit) | Data access | JpaRepository interface |
| Entity | `@Entity` | JPA mapping | Never expose to controller |
| DTO | `record` or class | Data transfer | Separate from Entity |

### 2A.3 Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Class | PascalCase | `MusicService`, `MusicUploadRequest` |
| Method | camelCase | `findByGenre()`, `uploadMusic()` |
| Variable | camelCase | `musicTitle`, `creatorId` |
| Constant | UPPER_SNAKE | `MAX_FILE_SIZE`, `DEFAULT_PAGE_SIZE` |
| Package | lowercase | `com.atstudio.atstudio.service` |
| DB Table | snake_case | `music`, `user_profile` |
| DB Column | snake_case | `created_at`, `music_title` |

### 2A.4 Code Templates

**Controller (Thin):**
```java
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {
    private final MusicService musicService;

    @GetMapping("/{id}")
    public ResponseEntity<MusicResponse> getMusic(@PathVariable Long id) {
        return ResponseEntity.ok(musicService.getMusic(id));
    }
}
```

**Service (Business Logic):**
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MusicService {
    private final MusicRepository musicRepository;

    public MusicResponse getMusic(Long id) {
        Music music = musicRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Music not found: " + id));
        return MusicResponse.from(music);
    }

    @Transactional
    public MusicResponse createMusic(MusicCreateRequest request) {
        Music music = request.toEntity();
        return MusicResponse.from(musicRepository.save(music));
    }
}
```

**Entity (JPA):**
```java
@Entity
@Table(name = "music")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Music extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;
}
```

**DTO (Record preferred):**
```java
public record MusicResponse(
    Long id,
    String title,
    String creatorName,
    LocalDateTime createdAt
) {
    public static MusicResponse from(Music music) {
        return new MusicResponse(
            music.getId(),
            music.getTitle(),
            music.getCreator().getName(),
            music.getCreatedAt()
        );
    }
}
```

**BaseEntity:**
```java
@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### 2A.5 Exception Handling

- Use `@RestControllerAdvice` with a single `GlobalExceptionHandler`.
- Define custom exceptions per domain: `EntityNotFoundException`, `DuplicateException`, etc.
- Return consistent error response format.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }
}
```

### 2A.6 Security Rules

- Sensitive config (`application.yml`): Use environment variables for secrets.
- JWT secret, DB password: **Never hardcode.**
- CSRF: Disabled for REST API (JWT-based auth), re-enable for Thymeleaf form pages.
- CORS: Configure explicitly per environment.

### 2A.7 MySQL / JPA Rules

- PK: `BIGINT AUTO_INCREMENT` (`GenerationType.IDENTITY`)
- Timestamps: `created_at`, `updated_at` via `BaseEntity` + JPA Auditing
- Fetch strategy: Default `LAZY` for `@ManyToOne`, `@OneToMany`
- Index: Apply to frequently searched columns
- N+1 prevention: Use `@EntityGraph` or `JOIN FETCH` for collection queries

---

## 2B. Python Coding Standards (System Scripts)

### 2.1 File and Structure

- **Location:** `.claude/scripts/{filename}.py` (e.g., `.claude/scripts/context_manager.py`)
- **Filename:** Use `snake_case` (e.g., `test_triggers.py`)
- **Encoding:** `utf-8` required (Python 3 defaults to utf-8 without explicit declaration, but explicit declaration recommended for I/O)

### 2.2 Style Guide (PEP 8 + Alpha)

- **Type Hints:** Type hints required in all function signatures.
- **Docstrings:** Use Google Style Docstring.
- **Comments:** Comment bodies must be written in **Korean**. (However, code itself and technical terms may use English)
- **Line Length:** Recommended 88 characters (Black formatter standard), but up to 100 characters allowed for readability.

**Example Template:**

```python
import os
import argparse
from typing import List, Dict, Optional

def process_data(input_list: List[str], options: Optional[Dict] = None) -> bool:
    """
    Processes the input data based on provided options.

    Args:
        input_list: A list of strings to process.
        options: Optional configuration dictionary.

    Returns:
        True if processing was successful, False otherwise.

    Raises:
        ValueError: If input_list is empty.
    """
    if not input_list:
        raise ValueError("Input list cannot be empty")

    # ... logic ...
    return True

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Process Data Script")
    # ... args ...
```

### 2.3 Security Coding

- **No Hardcoded Secrets:** Never include API keys or passwords in code.
- **Environment Variables:** Use `os.environ.get("KEY_NAME")`.
- **File Operations:** When user approval (HITL) is needed for file writes, utilize tool's `SafeToAutoRun=False` option or explicitly request user confirmation.

---

## 3. Agent Persona Definitions

### 3.1 Location and Format

- **Location (SoT):** `.claude/agents/{role}.md` (e.g., `se.md`)
- **Format:** Markdown
- **Frontmatter:** Required (specify Role, Tier, Capabilities)

### 3.2 Required Elements

1. **Role Definition:** Role defined in one sentence.
2. **Key Responsibilities:** List of main responsibilities (bullet points).
3. **Tone & Style:** Conversation tone (e.g., "Professional, Critical, Concise").
4. **Tools:** List of primarily used tools.

**Example (se.md):**

```markdown
---
role: Software Engineer (SE)
tier: 2
type: Implementation
---

# Software Engineer (SE)

## Role
Engineer who implements user requirements as working code and manages technical debt.

## Responsibilities
- **Implementation:** Write Python scripts and configuration files.
- **Refactoring:** Improve structure and remove duplication in existing code.
- **Testing:** Write and verify unit tests.

## Guidelines
- Always check `implementation_plan.md` before starting work.
- Place code in `scripts/` and comply with standards.
```

---

## 4. Documentation and Workflow

### 4.1 Plan-First & Tier 0

- **Plan-First:** Must update `implementation_plan.md` and receive user approval before code changes.
- **Tier 0 Check:** Self-check for violations of `core-principles.md` rules when starting work.

### 4.2 Artifact Management (Clean Workspace Policy)

- **Root Protection:** Prohibit creating temporary files in project root (`ws/`) except for config files (`task.md`, `README.md`, etc.).
- **Paths:**
  - **Reports:** Store one-time analysis/audit reports in `reports/` (e.g., `reports/audit_20260122.md`).
  - **System Data:** Store reusable agent data in `.claude/artifacts/`.
  - **Docs:** Place permanently preserved documents in appropriate classification under `docs/`.
- **Traceability:** Clearly record major decisions or changes in `task_boundary`'s `TaskSummary` or `notify_user` message.
- **Cleanup:** Immediately discard temporary files that achieved their purpose or promote them to assets.

---

## 5. Dependency and Environment Management

### 5.0 Java / Spring Boot (Application)

- **Java Version:** 17
- **Framework:** Spring Boot 4.x
- **Build Tool:** Gradle 9.3.0
- **Dependencies:** Managed via `build.gradle` (Spring Dependency Management plugin)
- **Runtime:** JDK 17+ required
- **Build/Run:** `gradlew.bat build` (Windows), `./gradlew build` (Linux/Mac)

### 5.0.1 Python (System Scripts)

- **Python Version:** 3.10 or higher
- **Dependencies:** Prefer standard library; for external libraries, specify in `requirements.txt` and install after user approval.
- **Virtual Env:** Use system Python or venv within **Single Machine Runtime** (Linux, macOS, WSL).

### 5.1 Workspace Setup

- **Single-Repo:** ATStudio is a single repository containing both agent system and application code.
- **Prerequisites:** JDK 17+, MySQL 8.x, Gradle (wrapper included)
- **Setup Steps:**
  1. Clone repository
  2. Configure `application.yml` (DB connection, JWT secret via environment variables)
  3. `gradlew.bat build` to verify setup

---

## 6. Testing Standards

### 6.1 TDD Cycle (Red → Green → Refactor)

Test-Driven Development is the **preferred development methodology** for all implementations:

1. **Red:** Write a failing test first
   - Define expected behavior before implementation
   - Verify test actually fails (confirms test validity)
   - Clarify acceptance criteria through test cases

2. **Green:** Write minimal code to pass the test
   - Implement only what's necessary to pass
   - Avoid premature optimization
   - Focus on correctness first

3. **Refactor:** Improve code quality while keeping tests green
   - Improve structure, remove duplication
   - Maintain test coverage throughout refactoring
   - Run tests continuously to ensure no regression

**Workflow Integration:**
- `se` agent follows TDD cycle for all implementations
- `re` agent verifies test quality and coverage
- Tests are written **before** implementation whenever possible

### 6.2 Test Categories and Criteria

#### 6.2.1 Unit Tests

**Purpose:** Validate individual functions/modules in isolation.

**Criteria:**
- No external dependencies (database, network, filesystem)
- Use mocks/stubs for dependencies
- Fast execution (< 100ms per test)
- High coverage of edge cases and error conditions

**Example (Python):**
```python
def test_parse_config_valid_input():
    """Test config parser with valid input."""
    config = parse_config({"key": "value"})
    assert config.key == "value"

def test_parse_config_missing_key():
    """Test config parser raises error on missing required key."""
    with pytest.raises(ValueError):
        parse_config({})
```

**Example (Java / JUnit5):**
```java
@SpringBootTest
class MusicServiceTest {
    @Autowired
    private MusicService musicService;

    @Test
    @DisplayName("Should return music by valid ID")
    void getMusicById() {
        MusicResponse result = musicService.getMusic(1L);
        assertThat(result).isNotNull();
        assertThat(result.title()).isNotEmpty();
    }

    @Test
    @DisplayName("Should throw when music not found")
    void getMusicByInvalidId() {
        assertThatThrownBy(() -> musicService.getMusic(999L))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

**Example (TypeScript — Phase 2 Frontend, Planned):**
```typescript
describe('parseConfig', () => {
  it('should parse valid input', () => {
    const config = parseConfig({ key: 'value' });
    expect(config.key).toBe('value');
  });

  it('should throw on missing required key', () => {
    expect(() => parseConfig({})).toThrow(ValueError);
  });
});
```

#### 6.2.2 Integration Tests

**Purpose:** Validate interaction between multiple components.

**Criteria:**
- Test component boundaries and contracts
- May use real dependencies (in-memory database, test filesystem)
- Moderate execution time (< 5s per test)
- Focus on critical integration points

**Examples:**
- Script + Config file interaction
- Agent + Context injection mechanism
- API client + mock server

#### 6.2.3 End-to-End (E2E) Tests

**Purpose:** Validate complete workflows from user perspective.

**Criteria:**
- Test full system integration
- Use realistic data and scenarios
- Slower execution acceptable (< 30s per test)
- Cover critical user journeys only (selective coverage)

**Examples:**
- REQ → WI → Delegation → Evidence Pack workflow
- Multi-agent collaboration scenarios
- Skill execution with real file I/O

### 6.3 Test Coverage Expectations

#### 6.3.1 Minimum Coverage Thresholds

| Metric | Threshold | Criticality |
|--------|-----------|----------|
| **Lines** | 80% | High |
| **Branches** | 70% | High |
| **Functions** | 80% | High |
| **Statements** | 80% | Medium |

**Exceptions:**
- Configuration files: No coverage required
- Generated code: Exclude from coverage
- Experimental/prototype code: Mark explicitly, lower threshold acceptable (60%)

#### 6.3.2 Critical Path Coverage

**100% coverage required** for:
- Security-sensitive code (authentication, authorization, data sanitization)
- Core orchestration logic (REQ approval gates, WI generation)
- Context injection mechanisms (Tier 0 document loading)
- Governance enforcement (policy violation detection)

**Verification:**
- Use `/test-coverage` skill to measure coverage
- Review coverage reports before completing WI
- Delegate to `qa` agent for unified quality check

#### 6.3.3 Coverage Verification Workflow

```bash
# Java (Gradle + JaCoCo)
gradlew.bat test jacocoTestReport      # Windows
./gradlew test jacocoTestReport         # Linux/Mac

# System scripts (Python)
/test-coverage

# Address gaps in critical paths first
# Add tests for uncovered branches/edge cases
```

### 6.4 Test File Location and Naming Conventions

#### 6.4.1 File Naming Patterns

**Java (JUnit5):**
- Pattern: `*Test.java` or `*Tests.java`
- Example: `MusicServiceTest.java`, `MusicControllerTest.java`
- Location: `src/test/java/` (mirror main package structure)

**Python:**
- Pattern: `test_*.py` (pytest convention)
- Example: `test_context_manager.py`, `test_workspace_setup.py`

**TypeScript/JavaScript (Phase 2 Frontend):**
- Pattern: `*.test.ts` or `*.spec.ts`
- Example: `helpers.test.ts`, `Button.spec.tsx`

#### 6.4.2 Directory Structure

**Java Tests (Spring Boot — Current):**
```
src/
├── main/java/com/atstudio/atstudio/
│   ├── service/
│   │   └── MusicService.java
│   └── controller/
│       └── MusicController.java
└── test/java/com/atstudio/atstudio/
    ├── service/
    │   └── MusicServiceTest.java
    └── controller/
        └── MusicControllerTest.java
```

**Centralized Tests (System Scripts — Python):**
```
.claude/
├── scripts/
│   ├── context_manager.py
│   └── workspace_setup.py
└── tests/
    ├── test_context_manager.py
    └── test_workspace_setup.py
```

**Co-located Tests (Phase 2 Frontend — Planned):**
```
frontend/src/
├── utils/
│   ├── helpers.ts
│   └── helpers.test.ts
└── components/
    ├── Button.tsx
    └── Button.test.tsx
```

**Rationale:**
- Java: Mirror package structure under `src/test/` (Spring Boot convention)
- Python: Centralized in `.claude/tests/` for system scripts
- TypeScript (Phase 2): Co-located for natural organization

#### 6.4.3 Test File Structure

**Python (pytest):**
```python
"""Tests for context_manager.py"""

import pytest
from .claude.scripts.context_manager import ContextManager

class TestContextManager:
    """Test suite for ContextManager class."""

    def test_load_tier0_documents(self):
        """Should load all Tier 0 documents successfully."""
        # Arrange
        manager = ContextManager()

        # Act
        docs = manager.load_tier0()

        # Assert
        assert len(docs) == 4
        assert "core-principles.md" in docs
```

**TypeScript (Jest/Vitest — Phase 2 Frontend, Planned):**
```typescript
import { describe, it, expect } from 'vitest';
import { ContextManager } from '../context-manager';

describe('ContextManager', () => {
  describe('loadTier0', () => {
    it('should load all Tier 0 documents', () => {
      // Arrange
      const manager = new ContextManager();

      // Act
      const docs = manager.loadTier0();

      // Assert
      expect(docs).toHaveLength(4);
      expect(docs).toContain('core-principles.md');
    });
  });
});
```

### 6.5 Skill Integration

#### 6.5.1 `/test` Skill Usage

**Purpose:** Execute project test suites and report structured results.

**When to Use:**
- After implementing new features
- Before committing code changes
- During WI verification phase
- When investigating test failures

**Invocation:**
```bash
/test                           # Run all tests
/test --verbose                 # Detailed output
/test src/utils/helpers.test.ts # Specific file
```

**Output Contract:**
- Pass/fail status
- Test counts (total, passed, failed, skipped)
- Failure details (error messages, locations)
- Duration metrics

#### 6.5.2 `/test-coverage` Skill Usage

**Purpose:** Analyze test coverage metrics and identify gaps.

**When to Use:**
- Before completing WI (coverage verification)
- During code review preparation
- When assessing quality for releases
- When `qa` agent performs comprehensive checks

**Invocation:**
```bash
/test-coverage                  # Run with coverage
/test-coverage --html           # Generate HTML report
```

**Output Contract:**
- Overall coverage percentage
- Per-metric breakdown (lines, branches, functions, statements)
- Files with low coverage (< 50%)
- Uncovered line ranges
- Recommendations for improvement

#### 6.5.3 Integration with Quality Workflow

**Standard Quality Check Sequence:**
1. `/typecheck` - Type validation
2. `/lint` - Code style/quality
3. `/test` - Test execution
4. `/test-coverage` - Coverage verification

**Delegation:**
- `qa` agent runs unified quality checks (all 4 skills)
- `re` agent investigates test failures and performs regression testing
- `cr` agent verifies test quality during code review

### 6.6 Subagent Testing Standards

#### 6.6.1 Reliability Engineer (`re`) Role

**Responsibilities:**
- Execute test suites independently (verification agent)
- Investigate test failures and identify root causes
- Perform regression testing after fixes
- Validate test quality (coverage, edge cases)

**Test Evidence Requirements:**
- Test execution logs (stdout/stderr)
- Coverage reports (if applicable)
- Failure reproduction steps
- Root cause analysis (for failures)

**Workflow:**
```
se implements → re tests independently →
  → Pass: Evidence Pack created
  → Fail: Report to se for fixes → re retests
```

#### 6.6.2 Quality Assurance (`qa`) Role

**Responsibilities:**
- Unified quality verification (typecheck + lint + test)
- Pre-commit quality gate enforcement
- Coverage threshold validation
- Quality metrics reporting

**Quality Gate Criteria:**
- All type checks pass
- No lint violations
- All tests pass
- Coverage meets thresholds (80% lines, 70% branches)

**Workflow:**
```
se completes implementation → qa runs unified checks →
  → Pass: Approve for commit
  → Fail: Block commit, report problems to se
```

#### 6.6.3 Evidence Pack Requirements

**Test Section (Mandatory in all Evidence Packs):**

```markdown
## Test Evidence

### Test Execution
- **Command**: `/test` or `gradlew.bat test`
- **Status**: ✅ All tests passed (X tests, Y assertions)
- **Duration**: Z.Zs
- **Log**: [test-output.log](path/to/log)

### Coverage Metrics
- **Overall**: 85.2%
- **Lines**: 87.3% (✅ above 80% threshold)
- **Branches**: 75.1% (✅ above 70% threshold)
- **Functions**: 89.5% (✅ above 80% threshold)
- **Report**: [coverage/index.html](path/to/report)

### Test Files Created/Modified
- `src/test/java/.../MusicServiceTest.java` (lines 1-45)
- `src/test/java/.../MusicControllerTest.java` (lines 23-67)

### Reproduction Steps
1. Run `gradlew.bat test` or `/test` to execute all tests
2. Run `/test-coverage` to verify coverage
3. Review coverage report for uncovered paths
```

**Verification:**
- `re` agent validates test evidence independently
- `qa` agent confirms coverage thresholds met
- Evidence Pack must be traceable (commands reproducible)

---
