"""WI017 explicit private recovery inventory; importing/CLI performs no reads."""
from __future__ import annotations

import hashlib
import os
import stat
from pathlib import PurePosixPath

from backup_protocol import GateError, require

CATEGORIES = {"settings", "service", "proxy", "tls", "billing_keyring", "backup_recovery"}


def _identity(info):
    return (info.st_dev, info.st_ino, info.st_size, info.st_mtime_ns,
            info.st_ctime_ns, info.st_mode, info.st_uid, info.st_nlink)


def secure_read(path: str, limit: int, *, private=True) -> dict:
    """POSIX descriptor traversal, no symlinks; Windows ACL verification unavailable."""
    require(os.name == "posix", "NATIVE_PERMISSION_CHECK_UNAVAILABLE")
    require(type(limit) is int and 0 < limit <= 64 * 1024 * 1024, "READ_BOUND")
    parsed = PurePosixPath(path)
    require(parsed.is_absolute() and ".." not in parsed.parts and len(parsed.parts) > 1,
            "ALLOWLIST_PATH")
    descriptors = []
    try:
        flags = os.O_RDONLY | os.O_NOFOLLOW
        current = os.open("/", flags | os.O_DIRECTORY)
        descriptors.append(current)
        for part in parsed.parts[1:-1]:
            current = os.open(part, flags | os.O_DIRECTORY, dir_fd=current)
            descriptors.append(current)
        parents = [os.fstat(fd) for fd in descriptors]
        for info in parents:
            require(info.st_uid in {0, os.geteuid()} and not info.st_mode & 0o022,
                    "UNSAFE_PARENT")
        fd = os.open(parsed.name, flags | os.O_NONBLOCK, dir_fd=current)
        descriptors.append(fd)
        before = os.fstat(fd)
        require(stat.S_ISREG(before.st_mode) and before.st_nlink == 1,
                "REGULAR_SINGLE_LINK_REQUIRED")
        require(type(private) is bool, "FILE_CLASSIFICATION")
        require(not before.st_mode & 0o7000, "UNSAFE_SPECIAL_MODE")
        forbidden = 0o077 if private else 0o022
        require(before.st_uid in ({os.geteuid()} if private else {0, os.geteuid()})
                and not before.st_mode & forbidden,
                "PRIVATE_FILE_MODE")
        require(before.st_size <= limit, "READ_BOUND")
        digest = hashlib.sha256()
        count = 0
        while True:
            block = os.read(fd, min(65536, limit + 1 - count))
            if not block:
                break
            count += len(block)
            require(count <= limit, "READ_BOUND")
            digest.update(block)
        require(_identity(before) == _identity(os.fstat(fd)) and count == before.st_size,
                "FILE_CHANGED")
        require(_identity(before) == _identity(os.stat(parsed.name, dir_fd=current,
                                                      follow_symlinks=False)), "FILE_CHANGED")
        # Detect parent replacement/rename as well as leaf replacement.
        for index, part in enumerate(parsed.parts[1:-1]):
            require(_identity(parents[index + 1]) == _identity(os.stat(
                part, dir_fd=descriptors[index], follow_symlinks=False)), "PARENT_CHANGED")
        return {"sha256": digest.hexdigest(), "bytes": count,
                "mode": stat.S_IMODE(before.st_mode), "uid": before.st_uid}
    except OSError:
        raise GateError("INVENTORY_IO_FAILED") from None
    finally:
        for fd in reversed(descriptors):
            os.close(fd)


def collect(entries: list[dict], retained_key_ids: list[str], *, total_limit=128 * 1024 * 1024):
    """Caller owns private return value; key completeness remains a declaration."""
    require(type(entries) is list and 0 < len(entries) <= 256, "ALLOWLIST_REQUIRED")
    require(type(total_limit) is int and 0 < total_limit <= 256 * 1024 * 1024, "TOTAL_BOUND")
    require(type(retained_key_ids) is list and retained_key_ids and
            all(type(k) is str and 0 < len(k) <= 128 for k in retained_key_ids) and
            len(set(retained_key_ids)) == len(retained_key_ids), "KEY_DECLARATION_REQUIRED")
    paths, categories, declared = set(), set(), set()
    for entry in entries:
        require(type(entry) is dict and entry.get("category") in CATEGORIES, "CATEGORY_REQUIRED")
        path = entry.get("path")
        require(type(path) is str and path not in paths, "ALLOWLIST_PATH")
        require(type(entry.get("private", True)) is bool, "FILE_CLASSIFICATION")
        paths.add(path)
        categories.add(entry["category"])
        keys = entry.get("key_ids", [])
        require(type(keys) is list and all(type(k) is str for k in keys), "KEY_DECLARATION_REQUIRED")
        require(not keys or entry["category"] == "billing_keyring", "KEY_CATEGORY")
        declared.update(keys)
    require(categories == CATEGORIES, "RECOVERY_CATEGORY_MISSING")
    require(declared == set(retained_key_ids), "RETAINED_KEY_SET_MISMATCH")
    files, total = [], 0
    for entry in entries:
        require(total < total_limit, "TOTAL_BOUND")
        info = secure_read(entry["path"], min(64 * 1024 * 1024, total_limit - total),
                           private=entry.get("private", True))
        total += info["bytes"]
        files.append({"path": entry["path"], "category": entry["category"], **info})
    return {"private_manifest": files, "public": {"status": "COLLECTED",
            "files": len(files), "bytes": total, "semantic_key_completeness": "UNVERIFIED",
            "restore_status": "NOT_TESTED"}}


if __name__ == "__main__":
    print("INERT: explicit allowlist and authorized native caller required")
