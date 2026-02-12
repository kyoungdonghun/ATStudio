#!/usr/bin/env python3
"""
File Lock Management Script (Lock Manager)

REQ-20260129-002 Phase 3 Implementation
- Git hash-based Optimistic Locking
- Per-session file lock acquisition/release
- Conflict detection and warnings
- Automatic lock cleanup on session termination

Usage:
    python3 lock_manager.py --help
    python3 lock_manager.py acquire FILE_PATH SESSION_ID
    python3 lock_manager.py release FILE_PATH SESSION_ID
    python3 lock_manager.py list [SESSION_ID]
    python3 lock_manager.py check
    python3 lock_manager.py cleanup SESSION_ID

Lock file structure:
    .claude/locks/
    ├── SES-a7f3/
    │   ├── CLAUDE.md.lock
    │   └── docs-index.md.lock
    └── SES-b9e2/
        └── workspace.json.lock

Lock file format (JSON):
    {
        "file": "CLAUDE.md",
        "file_path": "/full/path/to/CLAUDE.md",
        "session": "SES-20260129-1432-a7f3",
        "hash": "abc123...",
        "locked_at": "2026-01-29T14:32:15+09:00"
    }
"""

import argparse
import hashlib
import json
import os
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, Any, Optional, List, Tuple


# ============================================================================
# Constants
# ============================================================================

# Directory paths
SCRIPT_DIR = Path(__file__).parent.resolve()
CLAUDE_DIR = SCRIPT_DIR.parent
LOCKS_DIR = CLAUDE_DIR / "locks"
PROJECT_ROOT = CLAUDE_DIR.parent

# Lock file extension
LOCK_EXTENSION = ".lock"


# ============================================================================
# Exception Classes
# ============================================================================

class LockError(Exception):
    """Base exception for lock-related errors."""
    pass


class LockAcquireError(LockError):
    """Exception for lock acquisition failure."""
    pass


class LockReleaseError(LockError):
    """Exception for lock release failure."""
    pass


class LockConflictError(LockError):
    """Exception for lock conflicts."""
    def __init__(self, message: str, conflicts: List[Dict[str, Any]] = None):
        super().__init__(message)
        self.conflicts = conflicts or []


# ============================================================================
# Utility Functions
# ============================================================================

def get_now_iso() -> str:
    """Returns the current time in ISO 8601 format."""
    return datetime.now(timezone.utc).astimezone().isoformat()


def get_session_short(session_id: str) -> str:
    """
    Extracts a short identifier from the session ID.

    Example: SES-20260129-1432-a7f3 -> SES-a7f3

    Args:
        session_id: Full session ID

    Returns:
        Short session identifier
    """
    parts = session_id.split("-")
    if len(parts) >= 4:
        return f"SES-{parts[-1]}"
    return session_id


def normalize_file_path(file_path: str) -> Path:
    """
    Normalizes the file path.

    Converts relative paths to absolute paths and resolves symbolic links.

    Args:
        file_path: File path (absolute/relative)

    Returns:
        Normalized absolute path
    """
    path = Path(file_path)
    if not path.is_absolute():
        path = PROJECT_ROOT / path
    return path.resolve()


def get_file_display_name(file_path: Path) -> str:
    """
    Returns a display name for the file.

    Returns a relative path from the project root,
    converting slashes to hyphens for use as lock file names.

    Args:
        file_path: Absolute file path

    Returns:
        Display file name
    """
    try:
        relative = file_path.relative_to(PROJECT_ROOT)
        # Convert slashes to hyphens (for lock file names)
        return str(relative).replace("/", "-").replace("\\", "-")
    except ValueError:
        # For files outside the project, use a hash of the full path
        return hashlib.md5(str(file_path).encode()).hexdigest()[:16]


def get_lock_file_path(file_path: Path, session_id: str) -> Path:
    """
    Returns the lock file path.

    Args:
        file_path: Target file path to lock
        session_id: Session ID

    Returns:
        Lock file path
    """
    session_short = get_session_short(session_id)
    file_name = get_file_display_name(file_path)
    return LOCKS_DIR / session_short / f"{file_name}{LOCK_EXTENSION}"


def run_git(args: List[str], capture_output: bool = True) -> subprocess.CompletedProcess:
    """
    Executes a Git command.

    Args:
        args: Git command argument list
        capture_output: Whether to capture output

    Returns:
        CompletedProcess object
    """
    cmd = ["git", "-C", str(PROJECT_ROOT)] + args
    result = subprocess.run(
        cmd,
        capture_output=capture_output,
        text=True,
        check=False
    )
    return result


def get_git_hash(file_path: Path) -> Optional[str]:
    """
    Returns the Git blob hash of a file.

    For files not tracked by Git, computes the hash of file contents.

    Args:
        file_path: File path

    Returns:
        Git hash or None (if file doesn't exist)
    """
    if not file_path.exists():
        return None

    # Try to get file hash from Git
    try:
        relative_path = file_path.relative_to(PROJECT_ROOT)
        result = run_git(["hash-object", str(relative_path)])
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip()
    except (ValueError, subprocess.SubprocessError):
        pass

    # If Git fails, compute content hash directly
    try:
        with open(file_path, "rb") as f:
            content = f.read()
        return hashlib.sha1(content).hexdigest()
    except IOError:
        return None


def get_git_head_hash(file_path: Path) -> Optional[str]:
    """
    Returns the Git hash of the file at HEAD.

    Args:
        file_path: File path

    Returns:
        Git hash at HEAD or None
    """
    try:
        relative_path = file_path.relative_to(PROJECT_ROOT)
        result = run_git(["rev-parse", f"HEAD:{relative_path}"])
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip()
    except (ValueError, subprocess.SubprocessError):
        pass
    return None


# ============================================================================
# Lock Management Functions
# ============================================================================

def acquire_lock(file_path: str, session_id: str, force: bool = False) -> Dict[str, Any]:
    """
    Acquires a lock on a file.

    Uses optimistic locking:
    1. Records the current Git hash of the file
    2. Saves lock information as a JSON file
    3. Raises conflict error if another session already holds the lock

    Args:
        file_path: Target file path to lock
        session_id: Session ID
        force: If True, forcefully overwrites existing locks

    Returns:
        Lock information dictionary

    Raises:
        LockAcquireError: On lock acquisition failure
        LockConflictError: If another session already holds the lock
    """
    normalized_path = normalize_file_path(file_path)

    # Check file existence
    if not normalized_path.exists():
        raise LockAcquireError(f"File does not exist: {normalized_path}")

    # Check existing locks
    existing_locks = find_locks_for_file(str(normalized_path))

    for lock_info in existing_locks:
        if lock_info.get("session") != session_id:
            if not force:
                raise LockConflictError(
                    f"Already locked by another session: {lock_info.get('session')}",
                    conflicts=[lock_info]
                )
            else:
                # Force mode: remove existing lock
                old_session = lock_info.get("session")
                old_lock_file = get_lock_file_path(normalized_path, old_session)
                if old_lock_file.exists():
                    old_lock_file.unlink()

    # Get Git hash
    current_hash = get_git_hash(normalized_path)
    if current_hash is None:
        raise LockAcquireError(f"Cannot compute file hash: {normalized_path}")

    # Create lock information
    lock_info = {
        "file": normalized_path.name,
        "file_path": str(normalized_path),
        "relative_path": str(normalized_path.relative_to(PROJECT_ROOT)) if normalized_path.is_relative_to(PROJECT_ROOT) else str(normalized_path),
        "session": session_id,
        "hash": current_hash,
        "locked_at": get_now_iso()
    }

    # Save lock file
    lock_file_path = get_lock_file_path(normalized_path, session_id)
    lock_file_path.parent.mkdir(parents=True, exist_ok=True)

    with open(lock_file_path, "w", encoding="utf-8") as f:
        json.dump(lock_info, f, indent=2, ensure_ascii=False)

    return lock_info


def release_lock(file_path: str, session_id: str, verify: bool = True) -> Dict[str, Any]:
    """
    Releases a lock on a file.

    Args:
        file_path: File path to release lock
        session_id: Session ID
        verify: If True, checks hash changes and warns

    Returns:
        Lock release result dictionary

    Raises:
        LockReleaseError: On lock release failure
        LockConflictError: If file was modified by another session
    """
    normalized_path = normalize_file_path(file_path)
    lock_file_path = get_lock_file_path(normalized_path, session_id)

    # Check lock file existence
    if not lock_file_path.exists():
        raise LockReleaseError(f"Lock does not exist: {file_path} (session: {session_id})")

    # Load existing lock information
    try:
        with open(lock_file_path, "r", encoding="utf-8") as f:
            lock_info = json.load(f)
    except (json.JSONDecodeError, IOError) as e:
        raise LockReleaseError(f"Failed to read lock file: {e}")

    result = {
        "file_path": str(normalized_path),
        "session": session_id,
        "released_at": get_now_iso(),
        "original_hash": lock_info.get("hash"),
        "conflict_detected": False
    }

    # Hash verification (detect changes by other sessions)
    if verify and normalized_path.exists():
        current_hash = get_git_hash(normalized_path)
        original_hash = lock_info.get("hash")

        if current_hash and original_hash and current_hash != original_hash:
            # Conflict detected: warn but still release lock
            result["conflict_detected"] = True
            result["current_hash"] = current_hash
            result["warning"] = "File was modified after locking. Manual merge may be required."

    # Delete lock file
    lock_file_path.unlink()

    # Clean up empty session directory
    session_dir = lock_file_path.parent
    if session_dir.exists() and not any(session_dir.iterdir()):
        session_dir.rmdir()

    return result


def check_lock_conflicts() -> List[Dict[str, Any]]:
    """
    Checks for conflicts in all locks.

    Compares the current hash of each locked file with the hash at lock time
    and returns a list of files with conflicts.

    Returns:
        List of lock information with detected conflicts
    """
    conflicts = []

    if not LOCKS_DIR.exists():
        return conflicts

    for session_dir in LOCKS_DIR.iterdir():
        if not session_dir.is_dir() or session_dir.name.startswith("."):
            continue

        for lock_file in session_dir.glob(f"*{LOCK_EXTENSION}"):
            try:
                with open(lock_file, "r", encoding="utf-8") as f:
                    lock_info = json.load(f)
            except (json.JSONDecodeError, IOError):
                continue

            file_path = Path(lock_info.get("file_path", ""))
            if not file_path.exists():
                # File deleted -> conflict
                conflicts.append({
                    **lock_info,
                    "conflict_type": "file_deleted",
                    "message": "File has been deleted."
                })
                continue

            # Compare hashes
            current_hash = get_git_hash(file_path)
            original_hash = lock_info.get("hash")

            if current_hash and original_hash and current_hash != original_hash:
                conflicts.append({
                    **lock_info,
                    "conflict_type": "hash_mismatch",
                    "current_hash": current_hash,
                    "message": "File was modified after locking."
                })

    return conflicts


def list_locks(session_id: Optional[str] = None) -> List[Dict[str, Any]]:
    """
    Returns a list of locks.

    Args:
        session_id: Query locks for a specific session only (None for all sessions)

    Returns:
        List of lock information
    """
    locks = []

    if not LOCKS_DIR.exists():
        return locks

    # If session ID is specified, query only that directory
    if session_id:
        session_short = get_session_short(session_id)
        session_dirs = [LOCKS_DIR / session_short]
    else:
        session_dirs = [d for d in LOCKS_DIR.iterdir() if d.is_dir() and not d.name.startswith(".")]

    for session_dir in session_dirs:
        if not session_dir.exists():
            continue

        for lock_file in session_dir.glob(f"*{LOCK_EXTENSION}"):
            try:
                with open(lock_file, "r", encoding="utf-8") as f:
                    lock_info = json.load(f)
                locks.append(lock_info)
            except (json.JSONDecodeError, IOError):
                continue

    return locks


def find_locks_for_file(file_path: str) -> List[Dict[str, Any]]:
    """
    Finds all locks for a specific file.

    Args:
        file_path: File path

    Returns:
        List of lock information for the file
    """
    normalized_path = normalize_file_path(file_path)
    all_locks = list_locks()

    return [
        lock for lock in all_locks
        if Path(lock.get("file_path", "")).resolve() == normalized_path
    ]


def cleanup_session_locks(session_id: str, force: bool = False) -> Dict[str, Any]:
    """
    Cleans up all locks for a session.

    Args:
        session_id: Session ID
        force: If True, cleans up regardless of conflicts

    Returns:
        Cleanup result dictionary
    """
    session_short = get_session_short(session_id)
    session_dir = LOCKS_DIR / session_short

    result = {
        "session_id": session_id,
        "cleaned_at": get_now_iso(),
        "released_locks": [],
        "conflicts": [],
        "errors": []
    }

    if not session_dir.exists():
        return result

    for lock_file in list(session_dir.glob(f"*{LOCK_EXTENSION}")):
        try:
            with open(lock_file, "r", encoding="utf-8") as f:
                lock_info = json.load(f)

            file_path = lock_info.get("file_path", "")

            # Check for conflicts
            if not force:
                file_p = Path(file_path)
                if file_p.exists():
                    current_hash = get_git_hash(file_p)
                    original_hash = lock_info.get("hash")

                    if current_hash and original_hash and current_hash != original_hash:
                        result["conflicts"].append({
                            "file": file_path,
                            "original_hash": original_hash,
                            "current_hash": current_hash
                        })
                        continue

            # Delete lock file
            lock_file.unlink()
            result["released_locks"].append(lock_info.get("file", lock_file.name))

        except (json.JSONDecodeError, IOError, OSError) as e:
            result["errors"].append({
                "file": lock_file.name,
                "error": str(e)
            })

    # Clean up empty session directory
    if session_dir.exists() and not any(session_dir.iterdir()):
        session_dir.rmdir()

    return result


# ============================================================================
# CLI Interface
# ============================================================================

def cmd_acquire(args: argparse.Namespace) -> int:
    """Handler for acquire command."""
    try:
        lock_info = acquire_lock(args.file_path, args.session_id, force=args.force)
        print(f"[OK] Lock acquired: {args.file_path}")
        print(f"     Session: {args.session_id}")
        print(f"     Hash: {lock_info.get('hash', 'N/A')[:12]}...")
        print(f"     Time: {lock_info.get('locked_at')}")
        return 0
    except LockConflictError as e:
        print(f"[CONFLICT] {e}", file=sys.stderr)
        if e.conflicts:
            for conflict in e.conflicts:
                print(f"           Existing lock session: {conflict.get('session')}", file=sys.stderr)
                print(f"           Lock time: {conflict.get('locked_at')}", file=sys.stderr)
        return 2
    except LockError as e:
        print(f"[ERROR] {e}", file=sys.stderr)
        return 1


def cmd_release(args: argparse.Namespace) -> int:
    """Handler for release command."""
    try:
        result = release_lock(args.file_path, args.session_id, verify=not args.no_verify)
        print(f"[OK] Lock released: {args.file_path}")
        print(f"     Session: {args.session_id}")

        if result.get("conflict_detected"):
            print(f"[WARN] {result.get('warning')}", file=sys.stderr)
            print(f"       Original hash: {result.get('original_hash', 'N/A')[:12]}...", file=sys.stderr)
            print(f"       Current hash: {result.get('current_hash', 'N/A')[:12]}...", file=sys.stderr)
            return 2

        return 0
    except LockError as e:
        print(f"[ERROR] {e}", file=sys.stderr)
        return 1


def cmd_list(args: argparse.Namespace) -> int:
    """Handler for list command."""
    locks = list_locks(session_id=args.session_id)

    if not locks:
        print("No active locks.")
        return 0

    print(f"Total {len(locks)} lock(s):\n")

    # Group by session
    by_session = {}
    for lock in locks:
        session = lock.get("session", "unknown")
        if session not in by_session:
            by_session[session] = []
        by_session[session].append(lock)

    for session, session_locks in sorted(by_session.items()):
        print(f"[{get_session_short(session)}] ({len(session_locks)} lock(s))")
        for lock in session_locks:
            print(f"  - {lock.get('relative_path', lock.get('file', 'N/A'))}")
            print(f"    Hash: {lock.get('hash', 'N/A')[:12]}...")
            print(f"    Time: {lock.get('locked_at', 'N/A')}")
        print()

    return 0


def cmd_check(args: argparse.Namespace) -> int:
    """Handler for check command."""
    conflicts = check_lock_conflicts()

    if not conflicts:
        print("[OK] No conflicts detected.")
        return 0

    print(f"[WARN] {len(conflicts)} conflict(s) detected:\n", file=sys.stderr)

    for conflict in conflicts:
        print(f"  File: {conflict.get('relative_path', conflict.get('file', 'N/A'))}", file=sys.stderr)
        print(f"  Session: {conflict.get('session')}", file=sys.stderr)
        print(f"  Type: {conflict.get('conflict_type')}", file=sys.stderr)
        print(f"  Message: {conflict.get('message')}", file=sys.stderr)
        if conflict.get("current_hash"):
            print(f"  Original hash: {conflict.get('hash', 'N/A')[:12]}...", file=sys.stderr)
            print(f"  Current hash: {conflict.get('current_hash', 'N/A')[:12]}...", file=sys.stderr)
        print(file=sys.stderr)

    return 1


def cmd_cleanup(args: argparse.Namespace) -> int:
    """Handler for cleanup command."""
    result = cleanup_session_locks(args.session_id, force=args.force)

    if result["conflicts"] and not args.force:
        print(f"[WARN] Some locks were not released due to {len(result['conflicts'])} conflict(s).", file=sys.stderr)
        for conflict in result["conflicts"]:
            print(f"       {conflict.get('file')}", file=sys.stderr)
        print("\nUse --force option to force cleanup.", file=sys.stderr)

    if result["errors"]:
        print(f"[ERROR] {len(result['errors'])} error(s) occurred:", file=sys.stderr)
        for error in result["errors"]:
            print(f"        {error.get('file')}: {error.get('error')}", file=sys.stderr)

    released_count = len(result["released_locks"])
    if released_count > 0:
        print(f"[OK] {released_count} lock(s) released")
        for lock in result["released_locks"]:
            print(f"     - {lock}")
    else:
        print("No locks released.")

    return 0 if not result["errors"] else 1


def main() -> int:
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="File Lock Management Script (REQ-20260129-002 Phase 3)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
    # Acquire file lock
    python3 lock_manager.py acquire CLAUDE.md SES-20260129-1432-a7f3

    # Release lock
    python3 lock_manager.py release CLAUDE.md SES-20260129-1432-a7f3

    # List all locks
    python3 lock_manager.py list

    # List locks for a specific session only
    python3 lock_manager.py list SES-20260129-1432-a7f3

    # Check for conflicts
    python3 lock_manager.py check

    # Clean up session locks
    python3 lock_manager.py cleanup SES-20260129-1432-a7f3

Optimistic Locking:
    This tool uses optimistic locking.
    - Records the Git hash of the file when acquiring lock.
    - Compares hashes when releasing lock to detect conflicts.
    - Outputs warnings on conflicts and recommends manual merge.
"""
    )

    subparsers = parser.add_subparsers(dest="command", help="Commands")

    # acquire command
    acquire_parser = subparsers.add_parser("acquire", help="Acquire file lock")
    acquire_parser.add_argument("file_path", help="Target file path to lock")
    acquire_parser.add_argument("session_id", help="Session ID")
    acquire_parser.add_argument("--force", "-f", action="store_true", help="Force overwrite existing lock")
    acquire_parser.set_defaults(func=cmd_acquire)

    # release command
    release_parser = subparsers.add_parser("release", help="Release file lock")
    release_parser.add_argument("file_path", help="File path to release lock")
    release_parser.add_argument("session_id", help="Session ID")
    release_parser.add_argument("--no-verify", action="store_true", help="Skip hash verification")
    release_parser.set_defaults(func=cmd_release)

    # list command
    list_parser = subparsers.add_parser("list", help="List locks")
    list_parser.add_argument("session_id", nargs="?", help="Query locks for a specific session only (optional)")
    list_parser.set_defaults(func=cmd_list)

    # check command
    check_parser = subparsers.add_parser("check", help="Check for conflicts")
    check_parser.set_defaults(func=cmd_check)

    # cleanup command
    cleanup_parser = subparsers.add_parser("cleanup", help="Clean up session locks")
    cleanup_parser.add_argument("session_id", help="Session ID to clean up")
    cleanup_parser.add_argument("--force", "-f", action="store_true", help="Force cleanup ignoring conflicts")
    cleanup_parser.set_defaults(func=cmd_cleanup)

    args = parser.parse_args()

    if args.command is None:
        parser.print_help()
        return 0

    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
