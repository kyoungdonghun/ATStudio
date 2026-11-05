"""Linux-only bounded helpers; importing this module performs no operations."""

from __future__ import annotations

import hashlib
import json
import os
import signal
import stat
import subprocess
import threading
import time
from contextlib import contextmanager
from dataclasses import dataclass
from pathlib import Path
from typing import Callable, Iterator

from backup_protocol import GIB, GateError, encoded, require


def checked_path(value: str | Path, *, private: bool = False,
                 trusted: bool = False) -> Path:
    path = Path(value)
    require(path.is_absolute() and ".." not in path.parts, "INVALID_PATH")
    for member in [*reversed(path.parents), path]:
        info = member.lstat()
        require(not stat.S_ISLNK(info.st_mode), "PATH_SYMLINK")
        if trusted or private:
            require(info.st_uid == 0 and not info.st_mode & 0o022, "PATH_OWNERSHIP")
    if private:
        require(not path.stat().st_mode & 0o077, "PRIVATE_MODE")
    return path


def checked_setting_source(config: dict, value: str | Path) -> Path:
    """Exact config-bound tmp source exceptions; control/code paths never use it."""
    online_bindings = config.get("online", {}).get("source_ownership", {})
    if online_bindings:
        require(config.get("capture_mode") == "ats-online-v1" and
                config.get("maintenance_mode") == "live-resume" and not config.get("cold_host") and
                set(online_bindings) == {"/var/lib/atstudio/tmp"} and
                "/var/lib/atstudio/tmp" in config["settings"], "ONLINE_SOURCE_SCOPE")
        if str(value) == "/var/lib/atstudio/tmp":
            expected = online_bindings[str(value)]
            require(set(expected) == {"uid", "gid", "mode", "device", "inode"} and
                    all(type(v) is int and v >= 0 for v in expected.values()) and
                    expected["uid"] == config["online"]["app_uid"] and
                    expected["gid"] == config["online"]["app_gid"] and
                    expected["uid"] > 0 and expected["mode"] == 0o700, "ONLINE_SOURCE_METADATA")
            path = Path(value)
            checked_path(path.parent, trusted=True)
            before = path.lstat()
            actual = {"uid": before.st_uid, "gid": before.st_gid, "mode": stat.S_IMODE(before.st_mode),
                      "device": before.st_dev, "inode": before.st_ino}
            require(stat.S_ISDIR(before.st_mode) and not stat.S_ISLNK(before.st_mode) and
                    actual == expected, "ONLINE_SOURCE_METADATA_DRIFT")
            after = path.lstat()
            require((before.st_dev, before.st_ino, before.st_mode, before.st_uid, before.st_gid) ==
                    (after.st_dev, after.st_ino, after.st_mode, after.st_uid, after.st_gid), "ONLINE_SOURCE_CHANGED")
            return path
    bindings = config.get("cold_host", {}).get("source_ownership", {})
    if str(value) not in bindings:
        return checked_path(value, trusted=True)
    require(config.get("maintenance_mode") == "cold-preserve" and
            set(bindings) == {"/var/lib/atstudio/tmp"} and str(value) == "/var/lib/atstudio/tmp"
            and str(value) in config["settings"], "COLD_SOURCE_SCOPE")
    expected = bindings[str(value)]
    require(set(expected) == {"uid", "gid", "mode", "device", "inode"} and
            all(type(v) is int and v >= 0 for v in expected.values()) and
            expected["uid"] > 0 and expected["mode"] == 0o700, "COLD_SOURCE_METADATA")
    path = Path(value)
    checked_path(path.parent, trusted=True)  # unchanged root-owned ancestor rule
    before = path.lstat()
    require(stat.S_ISDIR(before.st_mode) and not stat.S_ISLNK(before.st_mode) and
            not before.st_mode & 0o077, "COLD_SOURCE_TYPE_MODE")
    actual = {"uid": before.st_uid, "gid": before.st_gid, "mode": stat.S_IMODE(before.st_mode),
              "device": before.st_dev, "inode": before.st_ino}
    require(actual == expected, "COLD_SOURCE_METADATA_DRIFT")
    after = path.lstat()
    require((before.st_dev, before.st_ino, before.st_mode, before.st_uid, before.st_gid) ==
            (after.st_dev, after.st_ino, after.st_mode, after.st_uid, after.st_gid), "COLD_SOURCE_CHANGED")
    return path


def read_json(path: Path, max_bytes: int = 65536) -> dict:
    with path.open("rb") as stream:
        data = stream.read(max_bytes + 1)
    require(len(data) <= max_bytes, "JSON_SIZE")
    try:
        result = json.loads(data)
        require(isinstance(result, dict), "JSON_OBJECT_REQUIRED")
        return result
    except (ValueError, UnicodeError):
        raise GateError("INVALID_JSON") from None


def create_bytes(path: Path, data: bytes) -> None:
    descriptor = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL |
                         getattr(os, "O_NOFOLLOW", 0), 0o600)
    with os.fdopen(descriptor, "wb") as stream:
        stream.write(data)
        stream.flush()
        os.fsync(stream.fileno())


def atomic_json(path: Path, value: dict) -> None:
    # Publish attempt and last_good together in one atomic state replacement.
    staging = path.with_name(path.name + ".pending-" + os.urandom(8).hex())
    create_bytes(staging, encoded(value))
    os.replace(staging, path)
    if os.name == "posix":
        descriptor = os.open(path.parent, os.O_RDONLY | os.O_DIRECTORY)
        try:
            os.fsync(descriptor)
        finally:
            os.close(descriptor)


@contextmanager
def operations_lock(path: Path) -> Iterator[tuple[int, int]]:
    import fcntl

    checked_path(path.parent, trusted=True)
    descriptor = os.open(path, os.O_RDWR | os.O_CREAT | os.O_NOFOLLOW, 0o600)
    try:
        info = os.fstat(descriptor)
        require(stat.S_ISREG(info.st_mode) and info.st_uid == 0 and
                not info.st_mode & 0o077 and info.st_nlink == 1, "LOCK_UNSAFE")
        try:
            fcntl.flock(descriptor, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            raise GateError("OPERATIONS_BUSY") from None
        named = path.lstat()
        require((named.st_dev, named.st_ino) == (info.st_dev, info.st_ino), "LOCK_REPLACED")
        yield (info.st_dev, info.st_ino)
    finally:
        os.close(descriptor)


def hash_file(path: Path, tick: Callable[[], None], cap: int) -> tuple[int, str]:
    count = 0
    result = hashlib.sha256()
    descriptor = os.open(path, os.O_RDONLY | getattr(os, "O_NOFOLLOW", 0))
    with os.fdopen(descriptor, "rb") as stream:
        before = os.fstat(stream.fileno())
        require(stat.S_ISREG(before.st_mode), "SOURCE_NOT_REGULAR")
        while chunk := stream.read(1024 * 1024):
            tick()
            count += len(chunk)
            require(count <= cap, "SOURCE_BYTE_LIMIT")
            result.update(chunk)
        after = os.fstat(stream.fileno())
        require((before.st_ino, before.st_size, before.st_mtime_ns) ==
                (after.st_ino, after.st_size, after.st_mtime_ns), "SOURCE_CHANGED")
    return count, result.hexdigest()


def inventory(paths: list[Path], tick: Callable[[], None], max_entries: int,
              max_bytes: int, max_metadata: int,
              devices: dict[str, int]) -> dict[str, dict]:
    result: dict[str, dict] = {}
    total = 0
    metadata = 0
    pending = [(path, path) for path in paths]
    while pending:
        tick()
        path, root = pending.pop()
        info = path.lstat()
        require(info.st_dev == devices[str(root)], "NESTED_MOUNT_REVIEW_REQUIRED")
        row = {"mode": stat.S_IMODE(info.st_mode), "uid": info.st_uid,
               "gid": info.st_gid}
        require(not row["mode"] & 0o7000, "SPECIAL_MODE_REVIEW_REQUIRED")
        if stat.S_ISLNK(info.st_mode):
            resolved = path.resolve(strict=True)
            require(resolved == root or root in resolved.parents, "SOURCE_SYMLINK_ESCAPE")
            raise GateError("SOURCE_SYMLINK_REVIEW_REQUIRED")
        elif stat.S_ISDIR(info.st_mode):
            row["type"] = "dir"
            with os.scandir(path) as entries:
                for entry in entries:
                    require(len(pending) + len(result) < max_entries, "ENTRY_LIMIT")
                    pending.append((Path(entry.path), root))
        elif stat.S_ISREG(info.st_mode):
            size, sha = hash_file(path, tick, max_bytes - total)
            total += size
            row.update(type="file", size=size, sha256=sha)
        else:
            raise GateError("SPECIAL_SOURCE_FILE")
        require(str(path) not in result, "DUPLICATE_SOURCE")
        result[str(path)] = row
        metadata += len(encoded({str(path): row}))
        require(len(result) <= max_entries and metadata <= max_metadata, "INVENTORY_LIMIT")
    return result


def footprint(path: Path, cap: int) -> int:
    total = 0
    visited = 0
    pending = [path]
    while pending:
        member = pending.pop()
        info = member.lstat()
        require(not stat.S_ISLNK(info.st_mode), "RUNTIME_SYMLINK")
        visited += 1
        require(visited <= 100000, "RUNTIME_ENTRY_LIMIT")
        if stat.S_ISDIR(info.st_mode):
            with os.scandir(member) as entries:
                for entry in entries:
                    pending.append(Path(entry.path))
                    require(len(pending) <= 100000, "RUNTIME_ENTRY_LIMIT")
        else:
            require(stat.S_ISREG(info.st_mode), "RUNTIME_SPECIAL_FILE")
            total += info.st_size
            require(total <= cap, "RUNTIME_BYTE_LIMIT")
    return total


class LinuxMonitor:
    """Observe reserve and cgroup v2 ceilings for the whole inherited helper group."""

    def __init__(self, config: dict, deadline: float) -> None:
        self.config = config
        self.deadline = deadline
        self.monotonic_end = time.monotonic() + max(0, deadline - time.time())
        self.last_scan = 0.0
        entries = Path("/proc/self/cgroup").read_text().splitlines()
        unified = [line[3:] for line in entries if line.startswith("0::")]
        require(len(unified) == 1 and unified[0] != "/" and
                ".." not in Path(unified[0]).parts, "CGROUP_REQUIRED")
        self.group = Path("/sys/fs/cgroup") / unified[0].lstrip("/")
        require(self.group.resolve().is_relative_to(Path("/sys/fs/cgroup")), "CGROUP_PATH")
        require(int((self.group / "memory.high").read_text()) <= 768 * 1024**2 and
                int((self.group / "memory.max").read_text()) <= GIB, "GROUP_MEMORY_CAP")
        quota, period = (self.group / "cpu.max").read_text().split()
        require(quota != "max" and int(quota) <= int(period), "GROUP_CPU_CAP")
        require(os.getpriority(os.PRIO_PROCESS, 0) >= 10, "GROUP_NICE")
        self.events = self.memory_events()
        self.swap = int((self.group / "memory.swap.current").read_text())
        self.tick(force=True, projected=True)

    def memory_events(self) -> dict[str, int]:
        return {key: int(value) for key, value in
                (line.split() for line in (self.group / "memory.events").read_text().splitlines())}

    def tick(self, *, force: bool = False, projected: bool = False) -> None:
        require(time.time() < self.deadline and time.monotonic() < self.monotonic_end,
                "WINDOW_EXPIRED")
        if not force and time.monotonic() - self.last_scan < 1:
            return
        self.last_scan = time.monotonic()
        limits = self.config["limits"]
        growth = sum(limits[key] for key in ("dump_bytes", "metadata_bytes", "cache_bytes",
                                             "temp_bytes", "other_growth_bytes"))
        for value, expected_device in self.config["expected_filesystems"].items():
            path = checked_path(value)
            require(path.stat().st_dev == expected_device, "MOUNT_IDENTITY")
            space = os.statvfs(path)
            total, free = space.f_blocks * space.f_frsize, space.f_bavail * space.f_frsize
            reserve = max(total // 5, 10 * GIB)
            require(free >= reserve + (growth if projected else limits["other_growth_bytes"]),
                    "DISK_RESERVE")
            require(space.f_files > 0 and space.f_favail >= space.f_files // 10,
                    "INODE_RESERVE")
        footprint(Path(self.config["cache_dir"]), limits["cache_bytes"])
        footprint(Path(self.config["temp_dir"]), limits["temp_bytes"])
        memory = {line.split(":")[0]: int(line.split()[1]) * 1024
                  for line in Path("/proc/meminfo").read_text().splitlines()
                  if line.startswith("MemAvailable:")}
        require(memory.get("MemAvailable", 0) >= GIB, "MEMORY_RESERVE")
        events = self.memory_events()
        require(all(events.get(key, 0) <= self.events.get(key, 0)
                    for key in ("high", "max", "oom", "oom_kill")), "GROUP_MEMORY_PRESSURE")
        require(int((self.group / "memory.swap.current").read_text()) <= self.swap,
                "GROUP_SWAP_GROWTH")


@dataclass
class CommandResult:
    code: int
    data: bytes
    size: int
    sha256: str


class LinuxRunner:
    """Sequential, shell-free child groups with bounded streaming stdout."""

    def __init__(self, monitor: LinuxMonitor, environment: dict[str, str]) -> None:
        self.monitor = monitor
        self.environment = environment
        self.active: dict[int, subprocess.Popen] = {}

    @staticmethod
    def group_exists(group: int) -> bool:
        try:
            os.killpg(group, 0)
            return True
        except ProcessLookupError:
            return False

    def stop_owned(self) -> None:
        for group, process in list(self.active.items()):
            for sig in (signal.SIGTERM, signal.SIGKILL):
                if self.group_exists(group):
                    os.killpg(group, sig)
                try:
                    process.wait(timeout=2)
                except subprocess.TimeoutExpired:
                    continue
                until = time.monotonic() + 2
                while self.group_exists(group) and time.monotonic() < until:
                    time.sleep(0.05)
                if not self.group_exists(group):
                    break
            require(not self.group_exists(group), "HELPERS_STILL_RUNNING")
            del self.active[group]

    def run(self, argv: list[str], max_bytes: int,
            output: Path | None = None, *, hash_only: bool = False,
            input_path: Path | None = None) -> CommandResult:
        require(not self.active, "HELPER_OVERLAP")
        self.monitor.tick(force=True)
        chunks: list[bytes] = []
        errors: list[str] = []
        size = 0
        sha = hashlib.sha256()
        sink = None
        source = None
        try:
            if input_path is not None:
                checked_path(input_path, private=True)
                descriptor = os.open(input_path, os.O_RDONLY | os.O_NOFOLLOW)
                info = os.fstat(descriptor)
                if not stat.S_ISREG(info.st_mode) or info.st_nlink != 1 or info.st_size > 16 * 1024**2:
                    os.close(descriptor)
                    require(False, "COMMAND_INPUT_BOUND")
                source = os.fdopen(descriptor, "rb")
            if output:
                descriptor = os.open(output, os.O_WRONLY | os.O_CREAT | os.O_EXCL |
                                     os.O_NOFOLLOW, 0o600)
                sink = os.fdopen(descriptor, "wb")
            process = subprocess.Popen(argv, stdin=source if source is not None else subprocess.DEVNULL,
                                       stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                                       env=self.environment, start_new_session=True,
                                       close_fds=True)
            self.active[process.pid] = process

            def consume() -> None:
                nonlocal size
                try:
                    while chunk := process.stdout.read(65536):
                        size += len(chunk)
                        if size > max_bytes:
                            errors.append("COMMAND_OUTPUT_LIMIT")
                            return
                        sha.update(chunk)
                        if sink:
                            sink.write(chunk)
                        elif not hash_only:
                            chunks.append(chunk)
                except (OSError, ValueError):
                    errors.append("COMMAND_OUTPUT_IO")

            reader = threading.Thread(target=consume, daemon=True)
            reader.start()
            try:
                while process.poll() is None or reader.is_alive():
                    self.monitor.tick()
                    require(not errors, errors[0] if errors else "COMMAND_OUTPUT_IO")
                    time.sleep(0.05)
                require(not errors, errors[0] if errors else "COMMAND_OUTPUT_IO")
                require(not self.group_exists(process.pid), "HELPER_DESCENDANTS")
                del self.active[process.pid]
                if sink:
                    sink.flush()
                    os.fsync(sink.fileno())
                return CommandResult(process.returncode, b"".join(chunks), size, sha.hexdigest())
            finally:
                self.stop_owned()
                reader.join(timeout=2)
                require(not reader.is_alive(), "HELPER_READER_ACTIVE")
                process.stdout.close()
        finally:
            if sink:
                sink.close()
            if source:
                source.close()


def restic_environment(config: dict) -> dict[str, str]:
    return {"PATH": "/usr/bin:/bin", "LANG": "C.UTF-8", "GOMAXPROCS": "1",
            "HOME": config["state_dir"], "TMPDIR": config["temp_dir"],
            "RESTIC_REPOSITORY": config["repository"],
            "RESTIC_PASSWORD_FILE": config["password_file"],
            "AWS_SHARED_CREDENTIALS_FILE": config["aws_credentials_file"],
            "AWS_DEFAULT_REGION": config["region"], "AWS_EC2_METADATA_DISABLED": "true"}


def restic_command(config: dict, command: list[str]) -> list[str]:
    require(command[0] in {"cat", "backup", "ls", "dump"}, "COMMAND_NOT_ALLOWED")
    limits = config["limits"]
    options = []
    if "s3_bucket_lookup" in config:
        import re
        require(config["s3_bucket_lookup"] == "path" and
                isinstance(config.get("region"), str) and
                re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9-]{0,63}", config["region"]) is not None,
                "S3_PATH_OPTIONS")
        options = ["-o", "s3.bucket-lookup=path", "-o", "s3.region=" + config["region"]]
    return [config["restic_binary"], "--cache-dir", config["cache_dir"],
            "--compression", "auto", "--pack-size", "16", "--limit-upload",
            str(limits["upload_kib"]), "--limit-download", str(limits["download_kib"]),
            "-o", "s3.connections=2", *options, *command]
