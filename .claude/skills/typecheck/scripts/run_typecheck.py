#!/usr/bin/env python3
"""
Run TypeScript (tsc) or Python (mypy) type checking for a project.

Usage:
    python3 run_typecheck.py [project_path]

If no project_path is provided, uses the current working directory.
Detects the project type automatically:
  - If tsconfig.json exists -> runs tsc --noEmit
  - If pyproject.toml or .py files exist -> runs mypy
  - Both can coexist in a monorepo
"""

import subprocess
import sys
from pathlib import Path


def run_command(cmd, cwd=None):
    """Run a command and return (returncode, stdout, stderr)."""
    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            cwd=cwd,
            shell=isinstance(cmd, str),
        )
        return result.returncode, result.stdout, result.stderr
    except FileNotFoundError:
        return -1, "", f"Command not found: {cmd[0] if isinstance(cmd, list) else cmd}"


def count_ts_files(project_path):
    """Count TypeScript files in the project, excluding node_modules."""
    extensions = (".ts", ".tsx")
    count = 0
    for ext in extensions:
        count += len(
            [f for f in project_path.rglob(f"*{ext}") if "node_modules" not in str(f)]
        )
    return count


def parse_tsc_errors(output):
    """Parse tsc output and return list of error lines."""
    errors = []
    for line in output.strip().splitlines():
        line = line.strip()
        if line and ("error TS" in line):
            errors.append(line)
    return errors


def run_tsc(project_path):
    """Run TypeScript compiler in type-check-only mode."""
    tsconfig = project_path / "tsconfig.json"
    if not tsconfig.exists():
        return None, "No tsconfig.json found"

    file_count = count_ts_files(project_path)
    returncode, stdout, stderr = run_command(
        ["npx", "tsc", "--noEmit", "--project", str(tsconfig)],
        cwd=str(project_path),
    )

    output = stdout + stderr
    errors = parse_tsc_errors(output)

    if returncode == -1:
        return None, output

    print("[TYPECHECK]")
    print(f"Project: {project_path.resolve()}")

    if returncode == 0:
        print("Status: PASSED")
        print(f"Files checked: {file_count}")
        print("No type errors found.")
        return True, ""
    else:
        print(f"Status: FAILED ({len(errors)} error(s))")
        print(f"Files checked: {file_count}")
        print()
        print("Errors:")
        for err in errors:
            print(f"  {err}")
        print()
        print("[FIX SUGGESTIONS]")
        print("1. Check type annotations at indicated line numbers")
        print("2. Ensure imported types match expected usage")
        print("3. Consider using type guards for union types")
        return False, output


def run_mypy(project_path):
    """Run mypy type checker for Python projects."""
    returncode, stdout, stderr = run_command(
        ["mypy", "."],
        cwd=str(project_path),
    )

    output = stdout + stderr

    if returncode == -1:
        return None, output

    error_lines = [
        line for line in stdout.strip().splitlines() if "error:" in line
    ]

    print("[TYPECHECK - Python/mypy]")
    print(f"Project: {project_path.resolve()}")

    if returncode == 0:
        print("Status: PASSED")
        print("No type errors found.")
        return True, ""
    else:
        print(f"Status: FAILED ({len(error_lines)} error(s))")
        print()
        print("Errors:")
        for err in error_lines:
            print(f"  {err}")
        print()
        print("[FIX SUGGESTIONS]")
        print("1. Add type annotations to function signatures")
        print("2. Use Optional[] for values that can be None")
        print("3. Check stub packages for third-party libraries (types-*)")
        return False, output


def main():
    if len(sys.argv) > 1:
        project_path = Path(sys.argv[1]).resolve()
    else:
        project_path = Path.cwd()

    if not project_path.is_dir():
        print(f"Error: {project_path} is not a valid directory")
        sys.exit(1)

    results = {}
    has_tsconfig = (project_path / "tsconfig.json").exists()
    has_python = bool(list(project_path.glob("*.py"))) or (
        project_path / "pyproject.toml"
    ).exists()

    if not has_tsconfig and not has_python:
        print("[TYPECHECK]")
        print(f"Project: {project_path}")
        print("Status: SKIPPED")
        print("No tsconfig.json or Python project detected.")
        sys.exit(0)

    if has_tsconfig:
        ts_result, _ = run_tsc(project_path)
        results["typescript"] = ts_result
        if ts_result is None:
            print("TypeScript: SKIPPED (tsc not available)")
            print("  Install: npm install -D typescript")

    if has_python:
        if has_tsconfig:
            print()
        py_result, _ = run_mypy(project_path)
        results["python"] = py_result
        if py_result is None:
            print("Python/mypy: SKIPPED (mypy not available)")
            print("  Install: pip install mypy")

    any_failed = any(v is False for v in results.values())
    sys.exit(1 if any_failed else 0)


if __name__ == "__main__":
    main()
