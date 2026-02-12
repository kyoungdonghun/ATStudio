---
name: test-coverage
description: This skill should be used when analyzing test coverage metrics. It runs tests with coverage collection and reports line, branch, and function coverage percentages.
---

# Test Coverage

## Purpose

Analyze test coverage to identify untested code paths. Reports coverage metrics (lines, branches, functions, statements) and highlights files with low coverage for improvement.

## When to Use

- Before major releases to assess quality
- When adding tests to improve coverage
- During code review to verify test adequacy
- When `qa` agent needs coverage metrics

## Workflow

### 1. Detect Coverage Tool

Check for coverage configuration:
1. Jest with `--coverage` flag
2. Vitest with `--coverage` flag
3. `nyc` / `c8` for Node.js projects
4. Istanbul configuration

### 2. Run Coverage

```bash
# Jest
npx jest --coverage

# Vitest
npx vitest run --coverage

# Node.js with c8
npx c8 npm test

# Generate HTML report
npx jest --coverage --coverageReporters="html"
```

### 3. Output Format

Report results in structured format:

```
## Coverage Report

**Overall Coverage**: 78.5%

| Metric | Coverage | Threshold | Status |
|--------|----------|-----------|--------|
| Lines | 78.5% | 80% | ⚠️ Below |
| Branches | 65.2% | 70% | ❌ Failed |
| Functions | 82.1% | 80% | ✅ Passed |
| Statements | 79.3% | 80% | ⚠️ Below |

### Files with Low Coverage (<50%)

| File | Lines | Branches | Functions |
|------|-------|----------|-----------|
| src/utils/parser.ts | 32% | 25% | 40% |
| src/api/client.ts | 45% | 38% | 50% |

### Uncovered Lines

**src/utils/parser.ts**:
- Lines 45-67: Error handling branch
- Lines 89-102: Edge case validation

### Recommendations

1. Add tests for `parser.ts` error handling
2. Cover edge cases in `client.ts` retry logic
```

## Coverage Thresholds

Common threshold configuration:

```json
{
  "coverageThreshold": {
    "global": {
      "branches": 70,
      "functions": 80,
      "lines": 80,
      "statements": 80
    }
  }
}
```

## Integration

- Runs as part of `qa` agent comprehensive check
- Supplements `/test` skill with coverage metrics
- Results inform `cr` agent review decisions
