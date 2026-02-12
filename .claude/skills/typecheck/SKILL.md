---
name: typecheck
description: This skill should be used when verifying TypeScript type safety in a project. It runs the TypeScript compiler in type-check-only mode to catch type errors without emitting files.
---

# Typecheck

## Purpose

Run TypeScript compiler to verify type safety across the codebase. This skill detects type mismatches, missing types, and incorrect type annotations before code is committed or deployed.

## When to Use

- Before creating commits or pull requests in TypeScript projects
- After making significant code changes that affect type definitions
- When reviewing code for type safety issues
- As part of CI/CD quality gates

## How to Use

### Quick Start

Run the bundled typecheck script:

```bash
python3 .claude/skills/typecheck/scripts/run_typecheck.py [project_path]
```

If no project path is provided, it uses the current working directory.

### Manual Commands

For direct TypeScript compiler usage:

```bash
# Check entire project
npx tsc --noEmit

# Check specific file
npx tsc --noEmit src/index.ts

# Check with specific config
npx tsc --noEmit --project tsconfig.json
```

### Prerequisites

- TypeScript installed (`npm install -D typescript`)
- Valid `tsconfig.json` in project root
- Node.js and npm available

### Interpreting Results

**Success Output:**
```
[TYPECHECK]
Project: /path/to/project
Status: PASSED
Files checked: 42
No type errors found.
```

**Failure Output:**
```
[TYPECHECK]
Project: /path/to/project
Status: FAILED (5 errors)

Errors:
  src/components/Button.tsx:15:3 - error TS2322: Type 'string' is not assignable to type 'number'.
  src/utils/helpers.ts:42:10 - error TS2345: Argument of type 'null' is not assignable to parameter of type 'string'.
  ...

[FIX SUGGESTIONS]
1. Check type annotations at indicated line numbers
2. Ensure imported types match expected usage
3. Consider using type guards for union types
```

### Common Type Errors

| Error Code | Description | Fix |
|------------|-------------|-----|
| TS2322 | Type not assignable | Check variable/parameter types |
| TS2345 | Argument type mismatch | Verify function call arguments |
| TS2551 | Property does not exist | Check property names, use optional chaining |
| TS7006 | Parameter implicitly has 'any' type | Add explicit type annotation |
| TS2304 | Cannot find name | Import missing type or declare it |

### Integration with Other Skills

- Use `/eslint` for code style and best practices
- Use `/test` after fixing type errors
- Use `/build-check` to verify full build succeeds
