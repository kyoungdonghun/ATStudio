#!/usr/bin/env python3
"""
Session Management Script (Session Manager)

REQ-20260129-002 Phase 1 Implementation
- Session ID generation and management
- WI-ID generation
- Automatic Git branch creation/switching
- Metadata save/load

Usage:
    python3 session_manager.py --help
    python3 session_manager.py start [--req REQ_ID]
    python3 session_manager.py end SESSION_ID [--auto-merge]
    python3 session_manager.py active
    python3 session_manager.py list
    python3 session_manager.py wi SESSION_ID [--counter N]
"""

import argparse
import json
import os
import secrets
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, Any, Optional, List

# Import lock_manager module from the same directory
try:
    from .lock_manager import cleanup_session_locks
except ImportError:
    # When running directly, use absolute path import instead of relative import
    import importlib.util
    _lock_manager_path = Path(__file__).parent / "lock_manager.py"
    _spec = importlib.util.spec_from_file_location("lock_manager", _lock_manager_path)
    _lock_manager = importlib.util.module_from_spec(_spec)
    _spec.loader.exec_module(_lock_manager)
    cleanup_session_locks = _lock_manager.cleanup_session_locks


# ============================================================================
# Constants
# ============================================================================

# Directory paths
SCRIPT_DIR = Path(__file__).parent.resolve()
CLAUDE_DIR = SCRIPT_DIR.parent
SESSIONS_DIR = CLAUDE_DIR / "sessions"
PROJECT_ROOT = CLAUDE_DIR.parent

# Session ID format: SES-YYYYMMDD-HHmm-{4-digit hex}
SESSION_ID_PREFIX = "SES"

# WI-ID format: WI-YYYYMMDD-SES-{session_random}-###
WI_ID_PREFIX = "WI"


# ============================================================================
# Utility Functions
# ============================================================================

def get_now_iso() -> str:
    """Returns the current time in ISO 8601 format."""
    return datetime.now(timezone.utc).astimezone().isoformat()


def get_now_compact() -> str:
    """Returns the current time in YYYYMMDD-HHmm format."""
    return datetime.now().strftime("%Y%m%d-%H%M")


def get_date_compact() -> str:
    """Returns the current date in YYYYMMDD format."""
    return datetime.now().strftime("%Y%m%d")


def generate_random_hex(length: int = 4) -> str:
    """Generates a random hexadecimal string of the specified length."""
    return secrets.token_hex(length // 2 + length % 2)[:length]


def run_git(args: List[str], capture_output: bool = True) -> subprocess.CompletedProcess:
    """
    Executes a Git command.

    Args:
        args: Git command argument list (e.g., ["checkout", "-b", "branch-name"])
        capture_output: Whether to capture output

    Returns:
        CompletedProcess object

    Raises:
        subprocess.CalledProcessError: On Git command failure
    """
    cmd = ["git", "-C", str(PROJECT_ROOT)] + args
    result = subprocess.run(
        cmd,
        capture_output=capture_output,
        text=True,
        check=False
    )
    return result


def get_current_branch() -> Optional[str]:
    """Returns the current Git branch name."""
    result = run_git(["rev-parse", "--abbrev-ref", "HEAD"])
    if result.returncode == 0:
        return result.stdout.strip()
    return None


def branch_exists(branch_name: str) -> bool:
    """Checks if a branch exists."""
    result = run_git(["rev-parse", "--verify", branch_name])
    return result.returncode == 0


# ============================================================================
# Session Management Functions
# ============================================================================

def generate_session_id() -> str:
    """
    Generates a new session ID.

    Format: SES-YYYYMMDD-HHmm-{4-digit hex}
    Example: SES-20260129-1432-a7f3

    Returns:
        Generated session ID string
    """
    timestamp = get_now_compact()
    random_part = generate_random_hex(4)
    return f"{SESSION_ID_PREFIX}-{timestamp}-{random_part}"


def get_session_file_path(session_id: str) -> Path:
    """Returns the session metadata file path."""
    return SESSIONS_DIR / f"{session_id}.json"


def save_metadata(session_id: str, metadata: Dict[str, Any]) -> Path:
    """
    Saves session metadata as a JSON file.

    Args:
        session_id: Session ID
        metadata: Metadata dictionary to save

    Returns:
        Saved file path
    """
    SESSIONS_DIR.mkdir(parents=True, exist_ok=True)
    file_path = get_session_file_path(session_id)

    with open(file_path, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2, ensure_ascii=False)

    return file_path


def load_metadata(session_id: str) -> Optional[Dict[str, Any]]:
    """
    Loads session metadata.

    Args:
        session_id: Session ID

    Returns:
        Metadata dictionary or None (if file doesn't exist)
    """
    file_path = get_session_file_path(session_id)

    if not file_path.exists():
        return None

    try:
        with open(file_path, "r", encoding="utf-8") as f:
            return json.load(f)
    except (json.JSONDecodeError, IOError) as e:
        print(f"[ERROR] Failed to load metadata: {e}", file=sys.stderr)
        return None


def start_session(req_id: Optional[str] = None) -> str:
    """
    Starts a new session.

    1. Generate session ID
    2. Create and checkout Git branch
    3. Save metadata

    Args:
        req_id: REQ ID to link (optional)

    Returns:
        Generated session ID

    Raises:
        RuntimeError: On branch creation failure
    """
    session_id = generate_session_id()
    random_part = session_id.split("-")[-1]  # Last 4-digit hex
    branch_name = f"session/SES-{random_part}"

    # Save current branch
    original_branch = get_current_branch()

    # Create and checkout branch
    if branch_exists(branch_name):
        # If already exists, retry with new random value
        print(f"[WARN] Branch {branch_name} already exists. Retrying.", file=sys.stderr)
        return start_session(req_id)

    result = run_git(["checkout", "-b", branch_name])
    if result.returncode != 0:
        error_msg = result.stderr.strip() if result.stderr else "Unknown error"
        raise RuntimeError(f"Branch creation failed: {error_msg}")

    # Create metadata
    metadata = {
        "session_id": session_id,
        "started_at": get_now_iso(),
        "branch": branch_name,
        "original_branch": original_branch,
        "req_ids": [req_id] if req_id else [],
        "wi_ids": [],
        "wi_counter": 0,
        "status": "active"
    }

    # Save metadata
    file_path = save_metadata(session_id, metadata)

    print(f"[INFO] Session started: {session_id}")
    print(f"[INFO] Branch: {branch_name}")
    print(f"[INFO] Metadata: {file_path}")

    return session_id


def end_session(session_id: str, auto_merge: bool = False, skip_lock_cleanup: bool = False) -> bool:
    """
    Ends a session.

    Args:
        session_id: Session ID to end
        auto_merge: If True, attempts automatic merge to main branch
        skip_lock_cleanup: If True, skips lock cleanup

    Returns:
        Success status
    """
    metadata = load_metadata(session_id)

    if metadata is None:
        print(f"[ERROR] Session not found: {session_id}", file=sys.stderr)
        return False

    if metadata.get("status") != "active":
        print(f"[WARN] Session already ended: {session_id}", file=sys.stderr)
        return False

    # Lock cleanup (execute before ending)
    if not skip_lock_cleanup:
        print(f"[INFO] Cleaning up session locks: {session_id}")
        try:
            cleanup_result = cleanup_session_locks(session_id, force=False)
            released_count = len(cleanup_result.get("released_locks", []))
            conflict_count = len(cleanup_result.get("conflicts", []))

            if released_count > 0:
                print(f"[INFO] {released_count} lock(s) released")
            if conflict_count > 0:
                print(f"[WARN] {conflict_count} conflict(s) detected (manual check required)", file=sys.stderr)
                for conflict in cleanup_result.get("conflicts", []):
                    print(f"       - {conflict.get('file')}", file=sys.stderr)

            # Record lock cleanup result in metadata
            metadata["lock_cleanup"] = {
                "released": released_count,
                "conflicts": conflict_count,
                "cleaned_at": cleanup_result.get("cleaned_at")
            }
        except Exception as e:
            print(f"[WARN] Error during lock cleanup: {e}", file=sys.stderr)
            metadata["lock_cleanup"] = {"error": str(e)}
    else:
        print(f"[INFO] Lock cleanup skipped (--skip-lock-cleanup)")

    # Update status
    metadata["status"] = "completed"
    metadata["ended_at"] = get_now_iso()

    branch_name = metadata.get("branch")
    original_branch = metadata.get("original_branch", "main")

    if auto_merge and branch_name:
        # Checkout original branch
        result = run_git(["checkout", original_branch])
        if result.returncode != 0:
            print(f"[WARN] Failed to checkout {original_branch}", file=sys.stderr)
        else:
            # Attempt merge
            result = run_git(["merge", branch_name, "--no-edit"])
            if result.returncode == 0:
                metadata["status"] = "merged"
                print(f"[INFO] Merge completed: {branch_name} -> {original_branch}")
            else:
                print(f"[WARN] Merge failed. Manual merge required.", file=sys.stderr)
    else:
        # Return to original branch
        if branch_name and get_current_branch() == branch_name:
            run_git(["checkout", original_branch])

    # Save metadata
    save_metadata(session_id, metadata)

    print(f"[INFO] Session ended: {session_id}")
    print(f"[INFO] Status: {metadata['status']}")

    return True


def get_active_session() -> Optional[Dict[str, Any]]:
    """
    Returns the currently active session.

    Finds the session based on the Git branch.

    Returns:
        Active session metadata or None
    """
    current_branch = get_current_branch()

    if not current_branch or not current_branch.startswith("session/"):
        return None

    # Find session for the branch in session directory
    if not SESSIONS_DIR.exists():
        return None

    for session_file in SESSIONS_DIR.glob("SES-*.json"):
        try:
            with open(session_file, "r", encoding="utf-8") as f:
                metadata = json.load(f)

            if metadata.get("branch") == current_branch and metadata.get("status") == "active":
                return metadata
        except (json.JSONDecodeError, IOError):
            continue

    return None


def list_sessions(status_filter: Optional[str] = None) -> List[Dict[str, Any]]:
    """
    Returns a list of all sessions.

    Args:
        status_filter: Status to filter (active, completed, merged)

    Returns:
        List of session metadata
    """
    sessions = []

    if not SESSIONS_DIR.exists():
        return sessions

    for session_file in sorted(SESSIONS_DIR.glob("SES-*.json"), reverse=True):
        try:
            with open(session_file, "r", encoding="utf-8") as f:
                metadata = json.load(f)

            if status_filter is None or metadata.get("status") == status_filter:
                sessions.append(metadata)
        except (json.JSONDecodeError, IOError):
            continue

    return sessions


def generate_wi_id(session_id: str, counter: Optional[int] = None) -> str:
    """
    Generates a WI-ID.

    Format: WI-YYYYMMDD-SES-{session_random}-###
    Example: WI-20260129-SES-a7f3-001

    Args:
        session_id: Session ID
        counter: WI counter (None for auto-increment)

    Returns:
        Generated WI-ID

    Raises:
        ValueError: When session not found
    """
    metadata = load_metadata(session_id)

    if metadata is None:
        raise ValueError(f"Session not found: {session_id}")

    # Extract random part from session ID
    session_random = session_id.split("-")[-1]
    date_part = get_date_compact()

    # Determine counter
    if counter is not None:
        wi_counter = counter
    else:
        wi_counter = metadata.get("wi_counter", 0) + 1
        metadata["wi_counter"] = wi_counter

        # Add WI-ID to list
        wi_id = f"{WI_ID_PREFIX}-{date_part}-SES-{session_random}-{wi_counter:03d}"
        if wi_id not in metadata.get("wi_ids", []):
            metadata.setdefault("wi_ids", []).append(wi_id)

        # Save metadata
        save_metadata(session_id, metadata)

        return wi_id

    return f"{WI_ID_PREFIX}-{date_part}-SES-{session_random}-{wi_counter:03d}"


def add_req_to_session(session_id: str, req_id: str) -> bool:
    """
    Adds a REQ ID to a session.

    Args:
        session_id: Session ID
        req_id: REQ ID to add

    Returns:
        Success status
    """
    metadata = load_metadata(session_id)

    if metadata is None:
        print(f"[ERROR] Session not found: {session_id}", file=sys.stderr)
        return False

    if req_id not in metadata.get("req_ids", []):
        metadata.setdefault("req_ids", []).append(req_id)
        save_metadata(session_id, metadata)
        print(f"[INFO] REQ added: {req_id}")

    return True


# ============================================================================
# CLI Interface
# ============================================================================

def cmd_start(args: argparse.Namespace) -> int:
    """Handler for start command."""
    try:
        session_id = start_session(req_id=args.req)
        print(f"\nSession ID: {session_id}")
        return 0
    except RuntimeError as e:
        print(f"[ERROR] {e}", file=sys.stderr)
        return 1


def cmd_end(args: argparse.Namespace) -> int:
    """Handler for end command."""
    success = end_session(
        args.session_id,
        auto_merge=args.auto_merge,
        skip_lock_cleanup=args.skip_lock_cleanup
    )
    return 0 if success else 1


def cmd_active(args: argparse.Namespace) -> int:
    """Handler for active command."""
    session = get_active_session()

    if session is None:
        print("No active session.")
        return 1

    print(json.dumps(session, indent=2, ensure_ascii=False))
    return 0


def cmd_list(args: argparse.Namespace) -> int:
    """Handler for list command."""
    sessions = list_sessions(status_filter=args.status)

    if not sessions:
        print("No sessions found.")
        return 0

    print(f"Total {len(sessions)} session(s):\n")

    for session in sessions:
        status_icon = {
            "active": "[Active]",
            "completed": "[Completed]",
            "merged": "[Merged]"
        }.get(session.get("status", ""), "[?]")

        print(f"{status_icon} {session.get('session_id')}")
        print(f"    Branch: {session.get('branch')}")
        print(f"    Started: {session.get('started_at')}")
        if session.get("ended_at"):
            print(f"    Ended: {session.get('ended_at')}")
        print(f"    REQ: {session.get('req_ids', [])}")
        print(f"    WI count: {len(session.get('wi_ids', []))}")
        print()

    return 0


def cmd_wi(args: argparse.Namespace) -> int:
    """Handler for wi command."""
    try:
        wi_id = generate_wi_id(args.session_id, counter=args.counter)
        print(f"WI-ID: {wi_id}")
        return 0
    except ValueError as e:
        print(f"[ERROR] {e}", file=sys.stderr)
        return 1


def main() -> int:
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="Session Management Script (REQ-20260129-002)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
    # Start new session
    python3 session_manager.py start --req REQ-20260129-002

    # Check current active session
    python3 session_manager.py active

    # Generate WI-ID
    python3 session_manager.py wi SES-20260129-1432-a7f3

    # End session
    python3 session_manager.py end SES-20260129-1432-a7f3

    # List all sessions
    python3 session_manager.py list
"""
    )

    subparsers = parser.add_subparsers(dest="command", help="Commands")

    # start command
    start_parser = subparsers.add_parser("start", help="Start new session")
    start_parser.add_argument("--req", "-r", help="REQ ID to link")
    start_parser.set_defaults(func=cmd_start)

    # end command
    end_parser = subparsers.add_parser("end", help="End session")
    end_parser.add_argument("session_id", help="Session ID to end")
    end_parser.add_argument("--auto-merge", "-m", action="store_true", help="Auto merge")
    end_parser.add_argument("--skip-lock-cleanup", action="store_true", help="Skip lock cleanup")
    end_parser.set_defaults(func=cmd_end)

    # active command
    active_parser = subparsers.add_parser("active", help="Check current active session")
    active_parser.set_defaults(func=cmd_active)

    # list command
    list_parser = subparsers.add_parser("list", help="List all sessions")
    list_parser.add_argument("--status", "-s", choices=["active", "completed", "merged"], help="Status filter")
    list_parser.set_defaults(func=cmd_list)

    # wi command
    wi_parser = subparsers.add_parser("wi", help="Generate WI-ID")
    wi_parser.add_argument("session_id", help="Session ID")
    wi_parser.add_argument("--counter", "-c", type=int, help="WI counter (default: auto-increment)")
    wi_parser.set_defaults(func=cmd_wi)

    args = parser.parse_args()

    if args.command is None:
        parser.print_help()
        return 0

    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
