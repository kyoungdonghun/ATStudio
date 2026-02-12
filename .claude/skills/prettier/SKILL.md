---
name: prettier
description: This skill should be used when verifying code formatting consistency with Prettier. It checks if code matches the project's formatting rules without modifying files.
---

# Prettier

## Purpose

Verify code formatting compliance with Prettier configuration. Reports formatting violations and provides fix commands without auto-modifying files (user decides when to apply fixes).

## When to Use

- Before committing code changes
- During code review to verify formatting
- When `qa` agent needs format verification
- After code generation to ensure consistency

## Workflow

### 1. Detect Configuration

Check for Prettier configuration in order:
1. `.prettierrc` / `.prettierrc.json` / `.prettierrc.yaml`
2. `.prettierrc.js` / `prettier.config.js`
3. `package.json` prettier field

### 2. Check Formatting

```bash
# Check all files (report only, no changes)
npx prettier --check .

# Check specific files
npx prettier --check "src/**/*.{ts,tsx}"

# Show diff of what would change
npx prettier --check . --list-different
```

### 3. Output Format

Report results in structured format:

```
## Prettier Results

**Status**: ❌ Formatting issues found / ✅ All files formatted
**Files checked**: X
**Files with issues**: Y

### Files Requiring Formatting

| File | Status |
|------|--------|
| src/components/Button.tsx | ❌ Needs formatting |
| src/utils/helpers.ts | ❌ Needs formatting |

### Fix Command

To fix all formatting issues:
\`\`\`bash
npx prettier --write .
\`\`\`
```

## Common Configuration

```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100
}
```

## Integration

- Runs as part of `qa` agent quality workflow
- Typically runs after `/eslint` check
- Non-destructive: reports only, user applies fixes
