---
name: test
description: This skill should be used when running project test suites. It executes tests using the project's configured test framework (Jest, Vitest, etc.) and reports results.
---

# Test

## Purpose

Execute project test suites and report results. Supports common test frameworks (Jest, Vitest, Mocha) with structured output for pass/fail status, coverage hints, and failure details.

## When to Use

- After implementing new features
- Before committing code changes
- When `qa` agent needs test verification
- During CI/CD pipeline validation
- When `re` agent investigates failures

## Workflow

### 1. Detect Test Framework

Check for test configuration:
1. `vitest.config.ts` / `vite.config.ts` with test config → Vitest
2. `jest.config.js` / `jest.config.ts` → Jest
3. `package.json` scripts (test command) → Infer framework

### 2. Run Tests

```bash
# Run all tests
npm test

# Run with verbose output
npm test -- --verbose

# Run specific test file
npm test -- src/utils/helpers.test.ts

# Run tests matching pattern
npm test -- --testNamePattern="should validate"

# Watch mode (interactive development)
npm test -- --watch
```

### 3. Output Format

Report results in structured format:

```
## Test Results

**Status**: ❌ Failed / ✅ Passed
**Total**: X tests | **Passed**: Y | **Failed**: Z | **Skipped**: W

### Summary

| Suite | Tests | Passed | Failed | Duration |
|-------|-------|--------|--------|----------|
| utils/helpers.test.ts | 5 | 5 | 0 | 0.3s |
| components/Button.test.tsx | 8 | 6 | 2 | 1.2s |

### Failed Tests

#### components/Button.test.tsx

**Test**: should render with correct text
**Error**: Expected "Submit" but received "Cancel"
**Location**: line 45

\`\`\`
Expected: "Submit"
Received: "Cancel"
\`\`\`
```

## Framework-Specific Commands

| Framework | Run All | Watch | Coverage |
|-----------|---------|-------|----------|
| Jest | `npx jest` | `npx jest --watch` | `npx jest --coverage` |
| Vitest | `npx vitest run` | `npx vitest` | `npx vitest --coverage` |
| Mocha | `npx mocha` | `npx mocha --watch` | `npx nyc mocha` |

## Integration

- Runs as part of `qa` agent quality workflow
- For detailed failure investigation, delegate to `re` agent
- Coverage analysis available via `/test-coverage` skill
