---
name: typecheck
description: This skill should be used when verifying type safety in a project. For Java it runs Gradle compile check; for TypeScript it runs the TypeScript compiler in type-check-only mode.
---

# Typecheck

## Purpose

Verify type safety across the codebase. Supports Java (Phase 1) and TypeScript (Phase 2) via auto-detection.

## When to Use

- Before creating commits or pull requests
- After making significant code changes
- When reviewing code for type/compilation issues
- As part of CI/CD quality gates

## Workflow

### 1. Detect Project Type

Check **in order**:

1. `build.gradle` or `gradlew.bat` → **Java/Gradle mode**
2. `tsconfig.json` → **TypeScript mode**
3. Both present → **Java mode** (primary)

### 2. Run Type Check

#### Java / Gradle (Phase 1 — Current)

```bash
# Windows — compile Java sources only (no test compilation, fast)
gradlew.bat compileJava

# Linux/Mac
./gradlew compileJava

# Full compile including tests
gradlew.bat compileJava compileTestJava

# Check only (equivalent to build without test/jar)
gradlew.bat check -x test
```

**Success output:**
```
> Task :compileJava UP-TO-DATE
BUILD SUCCESSFUL in 2s
```

**Failure output:**
```
> Task :compileJava FAILED
src/main/java/com/atstudio/atstudio/service/MusicService.java:42:
  error: cannot find symbol
      Music music = request.toEntity();
                           ^
  symbol:   method toEntity()
BUILD FAILED in 3s
```

#### TypeScript (Phase 2 — React Frontend)

```bash
# Check entire project
npx tsc --noEmit

# Check with specific config
npx tsc --noEmit --project tsconfig.json
```

**Prerequisites:** TypeScript installed, valid `tsconfig.json`

### 3. Output Format

Report results in structured format:

```
## Typecheck Results

**Status**: ❌ Failed / ✅ Passed
**Mode**: Java/Gradle | TypeScript
**Files checked**: X

### Errors (if any)

| File | Line | Error |
|------|------|-------|
| src/.../MusicService.java | 42 | cannot find symbol: method toEntity() |
| src/.../MusicController.java | 15 | incompatible types: MusicResponse cannot be converted to ResponseDTO |

### Fix Suggestions
1. Remove `toEntity()` call — mapping is Service layer responsibility
2. Wrap return value in `ResponseDTO.withSingleData().data(...).build()`
```

## Common Java Compilation Errors

| Error | Likely Cause | Fix |
|-------|-------------|-----|
| `cannot find symbol` | Missing import, wrong method name | Check imports and method signatures |
| `incompatible types` | Wrong return type | Match declared vs actual type |
| `package does not exist` | Missing dependency | Add to `build.gradle` |
| `method not applicable` | Wrong argument types | Check method signature |

## Common TypeScript Errors

| Error Code | Description | Fix |
|------------|-------------|-----|
| TS2322 | Type not assignable | Check variable/parameter types |
| TS2345 | Argument type mismatch | Verify function call arguments |
| TS7006 | Parameter implicitly `any` | Add explicit type annotation |
| TS2304 | Cannot find name | Import missing type or declare it |

## Integration with Other Skills

- Use `/lint` for code style checks
- Use `/test` after fixing compilation errors
- Use `/build-check` to verify full build succeeds
