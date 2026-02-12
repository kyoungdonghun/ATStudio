#!/usr/bin/env python3
"""
Project Workspace Batch Restore Script.

- Restores (clone/pull) projects under the `projects/` directory in the system repo (root).
- Source of truth for project list: `docs/registry/project-registry.md`

Notes:
- Actual `git clone/pull` requires network access.
- Skips if Repo is `TBD` or empty.
"""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Optional


ROOT_DIR = Path(__file__).resolve().parents[2]  # .../.claude/scripts -> repo root
PROJECTS_DIR = ROOT_DIR / "projects"
REGISTRY_PATH = ROOT_DIR / "docs" / "registry" / "project-registry.md"


@dataclass(frozen=True)
class ProjectRow:
    project_id: str
    name: str
    repo: str
    status: str
    owner: str
    last_updated: str
    notes: str


def _run(cmd: List[str], cwd: Optional[Path] = None) -> int:
    """
    Executes an external command. Returns exit code even on failure.
    """
    proc = subprocess.run(cmd, cwd=str(cwd) if cwd else None)
    return int(proc.returncode)


def _is_probably_url(s: str) -> bool:
    return bool(re.match(r"^(https?://|git@)", s))


def parse_project_registry(md: str) -> List[ProjectRow]:
    """
    Parses the project table from `docs/registry/project-registry.md`.
    """
    lines = [ln.rstrip("\n") for ln in md.splitlines()]
    rows: List[ProjectRow] = []

    # Find table header
    header_idx = None
    for i, ln in enumerate(lines):
        if ln.strip().startswith("| Project ID |") and "Repo" in ln:
            header_idx = i
            break
    if header_idx is None:
        return rows

    # Rows start after the separator line
    start = header_idx + 2
    for ln in lines[start:]:
        if not ln.strip().startswith("|"):
            # End of table
            break
        # Skip separator/empty lines
        if re.match(r"^\|\s*:?-", ln.strip()):
            continue

        # Parse cells
        parts = [p.strip() for p in ln.strip().strip("|").split("|")]
        if len(parts) < 7:
            continue

        row = ProjectRow(
            project_id=parts[0],
            name=parts[1],
            repo=parts[2],
            status=parts[3],
            owner=parts[4],
            last_updated=parts[5],
            notes=parts[6],
        )

        # Skip example/placeholder
        if row.project_id.upper().startswith("PRJ-EXAMPLE"):
            continue

        rows.append(row)

    return rows


def iter_action_targets(rows: Iterable[ProjectRow]) -> Iterable[ProjectRow]:
    for r in rows:
        repo = (r.repo or "").strip()
        if not repo or repo.upper() == "TBD" or repo.lower() == "repo-url-or-path":
            continue
        yield r


def ensure_projects_dir() -> None:
    PROJECTS_DIR.mkdir(parents=True, exist_ok=True)


def project_dir(project_id: str) -> Path:
    # Use PRJ-XXX as folder name (user requirement: create projects under projects folder)
    return PROJECTS_DIR / project_id


def clone_or_pull(row: ProjectRow, mode: str, dry_run: bool) -> int:
    """
    mode: clone | pull | sync
    """
    target = project_dir(row.project_id)
    repo = row.repo.strip()

    is_git_repo = (target / ".git").exists()

    if dry_run:
        print(f"[DRY] {row.project_id}: repo={repo} -> {target} (git={is_git_repo})")
        return 0

    ensure_projects_dir()

    if not target.exists():
        target.mkdir(parents=True, exist_ok=True)

    if mode in ("clone", "sync") and not is_git_repo:
        # To clone into an empty directory, go to parent and clone
        # Check if target is empty to be safe, as clone will fail if not empty
        if any(target.iterdir()):
            print(f"[WARN] {row.project_id}: target not empty, skip clone: {target}")
            return 1

        if _is_probably_url(repo):
            return _run(["git", "clone", repo, str(target)])

        # For local paths: attempt clone (works for both local bare/normal repos)
        return _run(["git", "clone", repo, str(target)])

    if mode in ("pull", "sync") and is_git_repo:
        # If git repo, pull
        return _run(["git", "pull", "--ff-only"], cwd=target)

    # Otherwise noop
    print(f"[SKIP] {row.project_id}: mode={mode}, git={is_git_repo}")
    return 0


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Restore projects under ./projects from registry")
    parser.add_argument(
        "--mode",
        choices=["clone", "pull", "sync"],
        default="sync",
        help="clone: clone only, pull: pull only, sync: clone missing + pull existing",
    )
    parser.add_argument("--dry-run", action="store_true", help="print actions only")
    args = parser.parse_args(argv)

    if not REGISTRY_PATH.exists():
        print(f"[ERROR] registry not found: {REGISTRY_PATH}")
        return 2

    md = REGISTRY_PATH.read_text(encoding="utf-8")
    rows = parse_project_registry(md)
    targets = list(iter_action_targets(rows))

    if not targets:
        print("[INFO] No actionable projects found in registry.")
        return 0

    failed = 0
    for r in targets:
        rc = clone_or_pull(r, mode=args.mode, dry_run=args.dry_run)
        if rc != 0:
            failed += 1

    if failed:
        print(f"[ERROR] failures: {failed}")
        return 1

    print("[OK] done")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
