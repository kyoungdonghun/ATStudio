---
name: eslint
description: This skill should be used when checking JavaScript/TypeScript code quality with ESLint. It detects code style issues, potential bugs, and enforces coding standards.
---

# ESLint

## Purpose

Run ESLint to analyze JavaScript/TypeScript code for potential errors, code style violations, and best practice issues. Provides actionable feedback with file locations and fix suggestions.

## When to Use

- Before committing code changes
- During code review process
- When `qa` agent needs lint verification
- After refactoring to ensure no new issues

## Workflow

### 1. Detect Project Configuration

Check for ESLint configuration in order:
1. `eslint.config.js` (flat config)
2. `.eslintrc.js` / `.eslintrc.json` / `.eslintrc.yaml`
3. `package.json` eslintConfig field

### 2. Run ESLint

```bash
# Check all files
npx eslint . --ext .js,.jsx,.ts,.tsx

# Check specific files
npx eslint src/components/**/*.tsx

# With auto-fix (when requested)
npx eslint . --fix
```

### 3. Output Format

Report results in structured format:

```
## ESLint Results

**Status**: ❌ Failed / ✅ Passed
**Errors**: X | **Warnings**: Y

### Issues Found

| File | Line | Rule | Severity | Message |
|------|------|------|----------|---------|
| src/App.tsx | 15 | no-unused-vars | error | 'foo' is defined but never used |
```

## Common Rules Reference

| Rule | Description | Auto-fixable |
|------|-------------|--------------|
| `no-unused-vars` | Disallow unused variables | No |
| `no-console` | Disallow console statements | No |
| `prefer-const` | Prefer const over let | Yes |
| `@typescript-eslint/no-explicit-any` | Disallow any type | No |
| `react-hooks/exhaustive-deps` | Verify hook dependencies | No |

## Integration

- Runs as part of `qa` agent quality workflow
- Can be combined with `/prettier` for complete code style check
- Results feed into `cr` agent review process
