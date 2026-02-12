---
name: build-check
description: This skill should be used when verifying that the project builds successfully. It runs the build command and reports any compilation errors or warnings.
---

# Build Check

## Purpose

Verify that the project compiles and builds without errors. Catches build-time issues including TypeScript errors, missing imports, bundler configuration problems, and asset processing failures.

## When to Use

- Before committing significant changes
- After dependency updates
- When `qa` agent needs build verification
- Before deployment or release
- After refactoring or restructuring

## Workflow

### 1. Detect Build System

Check for build configuration:
1. `package.json` scripts.build → npm/yarn build
2. `vite.config.ts` → Vite
3. `next.config.js` → Next.js
4. `webpack.config.js` → Webpack

### 2. Run Build

```bash
# Standard npm build
npm run build

# With verbose output
npm run build -- --verbose

# Clean build (remove previous artifacts)
rm -rf dist && npm run build

# Production build
NODE_ENV=production npm run build
```

### 3. Output Format

Report results in structured format:

```
## Build Results

**Status**: ❌ Failed / ✅ Success
**Duration**: X.Xs
**Output Size**: Y MB

### Build Summary

| Metric | Value |
|--------|-------|
| Entry points | 1 |
| Chunks | 15 |
| Assets | 45 |
| Warnings | 3 |
| Errors | 0 |

### Errors (if any)

| File | Line | Error |
|------|------|-------|
| src/App.tsx | 23 | Cannot find module './missing' |

### Warnings

| Type | Count | Description |
|------|-------|-------------|
| Unused exports | 2 | Dead code detected |
| Large bundle | 1 | chunk-vendor.js > 500KB |

### Output Files

| File | Size | Gzipped |
|------|------|---------|
| index.js | 245 KB | 78 KB |
| vendor.js | 512 KB | 156 KB |
| styles.css | 45 KB | 12 KB |
```

## Common Build Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Module not found | Missing import/dependency | Check import path, install package |
| Type errors | TypeScript compilation | Fix type issues (see `/typecheck`) |
| Out of memory | Large build | Increase Node memory limit |
| Asset not found | Missing file reference | Verify asset paths |

## Integration

- Final check in `qa` agent quality workflow
- Runs after `/typecheck`, `/eslint`, `/test`
- Success required before deployment
