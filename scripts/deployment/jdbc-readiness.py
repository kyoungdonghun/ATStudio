"""WI029: offline preparation and separately approved, read-only JDBC probe."""

from __future__ import annotations

import argparse
from datetime import datetime, timedelta, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import stat
import struct
import subprocess
import sys
import threading
import zipfile


REQ = "REQ-20261010-ATS-009"
WI = "WI-20261010-ATS-029"
JAR = "build/libs/ATStudio-0.0.1-SNAPSHOT.jar"
JAR_SHA = "517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a"
DRIVER = "mysql-connector-j-9.7.0.jar"
DRIVER_SHA = "0353648eaa1c91e0f4020c959abf756bc866ffd583df22ae6b6f6e0cbd43eb44"
DRIVER_SIZE = 2607237
JAVA_SOURCE = "scripts/deployment/RuntimeJdbcProbe.java"
SOURCES = ("scripts/deployment/jdbc-readiness.py", JAVA_SOURCE)
URL = ("jdbc:mysql://127.0.0.1:3306/atstudio_prod?serverTimezone=Asia/Seoul"
       "&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull")
DB_VALUES = {"SPRING_DATASOURCE_URL": URL, "SPRING_DATASOURCE_USERNAME": "atstudio_app"}
CONFIG = Path("/etc/atstudio")
ENV_PATH = CONFIG / "atstudio.env"
APPROVAL_PATH = CONFIG / "jdbc-readiness-approval.json"
ENV_LIMIT = 16384
CHILD_TIMEOUT = 45
OUTPUT_LIMIT = 128
CHECKS = (
    "explicit_user_secret_use", "pinned_ssh_same_boot", "exclusive_maintenance",
    "no_session_io_recording", "no_debugger_or_process_capture",
    "no_sql_or_external_capture", "mysql_guards_rechecked",
    "mysql_temporarily_loopback", "app_proxy_workers_fenced",
    "stop_remask_on_exit", "exact_key_agent_cleanup",
)
JAVA_STATUSES = frozenset(("PASS", "DRIVER_LOADED", "INPUT_REJECTED", "DRIVER_REJECTED",
                           "JDBC_CONNECT_FAILED", "SESSION_REJECTED", "IDENTITY_REJECTED",
                           "TABLES_REJECTED", "ROWS_REJECTED", "CLOSE_FAILED"))
CODES = frozenset((
    "CLI_ARGUMENTS", "FILE_LINK", "FILE_TYPE", "FILE_SIZE", "PATH_OWNER", "PATH_MODE",
    "PATH_ANCESTRY", "PATH_HARDLINK", "PRIVATE_FILE_CHANGED", "ENV_GRAMMAR", "ENV_KEYS",
    "ENV_TARGET", "ENV_PASSWORD", "OUTPUT_PATH", "JAR_IDENTITY", "DRIVER_IDENTITY",
    "JAVA17_REQUIRED", "COMPILE_FAILED", "ARTIFACT_IDENTITY", "ARTIFACT_SHAPE",
    "SOURCE_IDENTITY", "APPROVAL_SHAPE", "APPROVAL_IDENTITY", "APPROVAL_CHECKS",
    "APPROVAL_EXPIRED", "APPROVAL_TIME", "LINUX_ROOT_REQUIRED", "PROCESS_DUMP_GUARD",
    "PROCESS_FAILED", "PROCESS_TIMEOUT", "PROCESS_OUTPUT", "JAVA_PROTOCOL",
    "INTERRUPTED", "UNEXPECTED_FAILURE",
))


class GateError(Exception):
    """Allowlisted identifiers only; exceptions never carry output to the caller."""


def require(condition: bool, code: str) -> None:
    if not condition:
        raise GateError(code)


def no_links(path: Path) -> None:
    """Match the existing validator's symlink/reparse-point rejection contract."""
    for part in (path, *path.parents):
        info = part.lstat()
        require(not stat.S_ISLNK(info.st_mode) and
                not getattr(info, "st_file_attributes", 0) & 0x400, "FILE_LINK")


def secure_path(path: Path, *, private: bool = False, directory: bool = False) -> None:
    """Match WI025 root ownership, ancestry, mode and single-link protections."""
    no_links(path)
    for parent in path.parents:
        info = parent.lstat()
        require(stat.S_ISDIR(info.st_mode) and info.st_uid == 0 and
                not info.st_mode & 0o022, "PATH_ANCESTRY")
    info = path.lstat()
    require(info.st_uid == 0 and not info.st_mode & 0o022, "PATH_OWNER")
    require(stat.S_ISDIR(info.st_mode) if directory else stat.S_ISREG(info.st_mode),
            "FILE_TYPE")
    if not directory:
        require(info.st_nlink == 1, "PATH_HARDLINK")
    if private:
        require(stat.S_IMODE(info.st_mode) == (0o700 if directory else 0o600), "PATH_MODE")


def read_private(path: Path, limit: int) -> bytes:
    secure_path(CONFIG, private=True, directory=True)
    secure_path(path, private=True)
    before = path.lstat()
    descriptor = os.open(path, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK)
    with os.fdopen(descriptor, "rb") as stream:
        info = os.fstat(stream.fileno())
        require((info.st_dev, info.st_ino) == (before.st_dev, before.st_ino) and
                stat.S_ISREG(info.st_mode) and info.st_uid == 0 and info.st_nlink == 1 and
                stat.S_IMODE(info.st_mode) == 0o600, "PRIVATE_FILE_CHANGED")
        require(info.st_size <= limit, "FILE_SIZE")
        raw = stream.read(limit + 1)
        after = os.fstat(stream.fileno())
        require((info.st_size, info.st_mtime_ns, info.st_ctime_ns) ==
                (after.st_size, after.st_mtime_ns, after.st_ctime_ns), "PRIVATE_FILE_CHANGED")
    require(len(raw) <= limit, "FILE_SIZE")
    return raw


def parse_env(raw: bytes) -> str:
    require(0 < len(raw) <= ENV_LIMIT and raw.endswith(b"\n"), "ENV_GRAMMAR")
    try:
        text = raw.decode("ascii")
    except UnicodeError:
        raise GateError("ENV_GRAMMAR") from None
    seen: set[str] = set()
    db: dict[str, str] = {}
    for line in text.split("\n"):
        require(all(32 <= ord(char) <= 126 for char in line), "ENV_GRAMMAR")
        if not line or line.startswith("#"):
            continue
        match = re.fullmatch(r'([A-Z][A-Z0-9_]*)="((?:[^"\\]|\\["\\])*)"', line)
        require(match is not None, "ENV_GRAMMAR")
        key, encoded = match.groups()
        require(key not in seen, "ENV_KEYS")
        seen.add(key)
        require(key not in {"JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "CLASSPATH"}, "ENV_KEYS")
        if key.startswith("SPRING_DATASOURCE_"):
            require(key in {*DB_VALUES, "SPRING_DATASOURCE_PASSWORD",
                           "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE",
                           "SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE",
                           "SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT"}, "ENV_KEYS")
        if key in {*DB_VALUES, "SPRING_DATASOURCE_PASSWORD"}:
            db[key] = re.sub(r'\\(["\\])', r'\1', encoded)
    require(set(db) == {*DB_VALUES, "SPRING_DATASOURCE_PASSWORD"}, "ENV_KEYS")
    require(all(db[key] == value for key, value in DB_VALUES.items()), "ENV_TARGET")
    password = db["SPRING_DATASOURCE_PASSWORD"]
    require(re.fullmatch(r"[!-~]{32}", password) is not None, "ENV_PASSWORD")
    return password


def frame(password: str, server_hash: str) -> bytes:
    require(re.fullmatch(r"[!-~]{32}", password) is not None, "ENV_PASSWORD")
    require(re.fullmatch(r"[0-9a-f]{64}", server_hash) is not None, "APPROVAL_IDENTITY")
    return b"ATS1" + struct.pack(">I", 32) + password.encode("ascii") + \
        struct.pack(">I", 64) + server_hash.encode("ascii")


def unique_object(pairs: list[tuple[str, object]]) -> dict:
    result = {}
    for key, value in pairs:
        require(key not in result, "APPROVAL_SHAPE")
        result[key] = value
    return result


def parse_approval(raw: bytes, identity: str, machine_hash: str, now: datetime) -> dict:
    try:
        value = json.loads(raw, object_pairs_hook=unique_object)
        require(isinstance(value, dict) and set(value) == {
            "req", "review_wi", "bundle_sha256", "machine_id_sha256",
            "server_uuid_sha256", "issued_at", "expires_at", "operator_checks",
        }, "APPROVAL_SHAPE")
        require(value["req"] == REQ and value["review_wi"] == "WI-20261010-ATS-031",
                "APPROVAL_IDENTITY")
        for key in ("bundle_sha256", "machine_id_sha256", "server_uuid_sha256"):
            require(isinstance(value[key], str) and
                    re.fullmatch(r"[0-9a-f]{64}", value[key]) is not None, "APPROVAL_IDENTITY")
        require(value["bundle_sha256"] == identity and value["machine_id_sha256"] == machine_hash,
                "APPROVAL_IDENTITY")
        require(value["operator_checks"] == dict.fromkeys(CHECKS, True) and
                all(type(item) is bool for item in value["operator_checks"].values()),
                "APPROVAL_CHECKS")
        for key in ("issued_at", "expires_at"):
            require(isinstance(value[key], str) and re.fullmatch(
                r"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ", value[key]) is not None, "APPROVAL_TIME")
        issued = datetime.fromisoformat(value["issued_at"].replace("Z", "+00:00"))
        expires = datetime.fromisoformat(value["expires_at"].replace("Z", "+00:00"))
        require(issued <= now < expires <= issued + timedelta(minutes=30), "APPROVAL_EXPIRED")
        return value
    except (ValueError, TypeError, KeyError):
        raise GateError("APPROVAL_SHAPE") from None


def sha_file(path: Path) -> str:
    no_links(path)
    info = path.stat()
    require(stat.S_ISREG(info.st_mode) and info.st_nlink == 1, "FILE_TYPE")
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def child_env() -> dict[str, str]:
    result = {"PATH": "/usr/bin:/bin", "LC_ALL": "C", "HOME": "/root"}
    if os.name == "nt":
        result.update({key: os.environ[key] for key in ("SystemRoot", "WINDIR") if key in os.environ})
    return result


def run_child(command: list[str], payload: bytes = b"", *, timeout: int = CHILD_TIMEOUT,
              limit: int = OUTPUT_LIMIT) -> tuple[int, bytes]:
    """Drain at most limit+1 bytes; never retain stderr or include failed output."""
    require(len(payload) <= 4096, "FILE_SIZE")
    process = None
    reader = None
    output: list[bytes] = []
    read_error: list[bool] = []
    try:
        process = subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=subprocess.DEVNULL, env=child_env(), cwd=os.path.abspath(os.sep),
                                   close_fds=True)

        def drain() -> None:
            try:
                output.append(process.stdout.read(limit + 1))
                if len(output[0]) > limit:
                    process.kill()
            except (OSError, ValueError):
                read_error.append(True)

        reader = threading.Thread(target=drain, daemon=True)
        reader.start()
        process.stdin.write(payload)
        process.stdin.close()
        try:
            code = process.wait(timeout=timeout)
        except subprocess.TimeoutExpired:
            raise GateError("PROCESS_TIMEOUT") from None
        reader.join(timeout=2)
        require(not reader.is_alive() and not read_error and len(output) == 1 and
                len(output[0]) <= limit, "PROCESS_OUTPUT")
        return code, output[0]
    except (OSError, subprocess.SubprocessError):
        raise GateError("PROCESS_FAILED") from None
    finally:
        if process is not None:
            if process.poll() is None:
                process.kill()
            process.wait()
            if reader is not None:
                reader.join(timeout=2)
            for stream in (process.stdin, process.stdout):
                stream.close()


def java_command(java: Path, output: Path, mode: str) -> list[str]:
    return [str(java), "-Xms16m", "-Xmx64m", "-XX:ActiveProcessorCount=1",
            "-XX:-CreateCoredumpOnCrash", "-XX:-HeapDumpOnOutOfMemoryError",
            "-XX:ErrorFile=" + os.devnull, "-XX:-UsePerfData", "-XX:+DisableAttachMechanism",
            "-Xlog:disable", "-cp", str(output / "classes") + os.pathsep + str(output / DRIVER),
            "RuntimeJdbcProbe", mode]


def checked_status(code: int, raw: bytes, *, mode: str) -> str:
    allowed = {name.encode("ascii") + b"\n": name for name in JAVA_STATUSES}
    require(raw in allowed, "JAVA_PROTOCOL")
    status = allowed[raw]
    success = "DRIVER_LOADED" if mode == "smoke" else "PASS"
    require((code == 0 and status == success) or
            (code == 2 and status not in {"PASS", "DRIVER_LOADED"}), "JAVA_PROTOCOL")
    return status


def output_path(root: Path, relative: str) -> Path:
    require(re.fullmatch(r"build/WI029-[A-Za-z0-9-]+", relative) is not None, "OUTPUT_PATH")
    no_links(root / "build")
    return root / relative


def prepare(root: Path, output: Path) -> None:
    require(sha_file(root / JAR) == JAR_SHA, "JAR_IDENTITY")
    sources = {name: sha_file(root / name) for name in SOURCES}
    compiler_name = shutil.which("javac")
    require(compiler_name is not None, "JAVA17_REQUIRED")
    compiler = Path(compiler_name).resolve(strict=True)
    code, version = run_child([str(compiler), "-version"])
    require(code == 0 and re.fullmatch(rb"javac 17\.\d+\.\d+\r?\n", version) is not None,
            "JAVA17_REQUIRED")
    with zipfile.ZipFile(root / JAR) as archive:
        entries = [entry for entry in archive.infolist() if entry.filename == "BOOT-INF/lib/" + DRIVER]
        require(len(entries) == 1 and entries[0].file_size == DRIVER_SIZE, "DRIVER_IDENTITY")
        driver = archive.read(entries[0])
    require(hashlib.sha256(driver).hexdigest() == DRIVER_SHA, "DRIVER_IDENTITY")
    output.mkdir(mode=0o700, exist_ok=False)
    (output / "classes").mkdir(mode=0o700)
    with (output / DRIVER).open("xb") as stream:
        stream.write(driver)
    code, _ = run_child([str(compiler), "--release", "17", "-encoding", "UTF-8", "-Xlint:all",
                         "-d", str(output / "classes"), str(root / JAVA_SOURCE)])
    require(code == 0, "COMPILE_FAILED")
    require(sources == {name: sha_file(root / name) for name in SOURCES} and
            sha_file(root / JAR) == JAR_SHA, "SOURCE_IDENTITY")
    manifest = {"schema": 1, "sources": sources, "jar_sha256": JAR_SHA,
                "driver_sha256": DRIVER_SHA, "javac": version.decode("ascii").strip(),
                "compiler_sha256": sha_file(compiler),
                "class_sha256": sha_file(output / "classes/RuntimeJdbcProbe.class")}
    with (output / "artifact.json").open("x", encoding="ascii", newline="\n") as stream:
        json.dump(manifest, stream, sort_keys=True)
        stream.write("\n")


def load_plan(root: Path, output: Path) -> dict:
    require(sha_file(root / JAR) == JAR_SHA, "JAR_IDENTITY")
    require(sha_file(output / DRIVER) == DRIVER_SHA, "DRIVER_IDENTITY")
    require(set(path.name for path in output.iterdir()) == {DRIVER, "classes", "artifact.json"},
            "ARTIFACT_SHAPE")
    no_links(output / "classes")
    require([path.name for path in (output / "classes").iterdir()] == ["RuntimeJdbcProbe.class"],
            "ARTIFACT_SHAPE")
    hashes = {name: sha_file(root / name) for name in SOURCES}
    hashes[JAR] = JAR_SHA
    for name in (DRIVER, "classes/RuntimeJdbcProbe.class", "artifact.json"):
        hashes[output.relative_to(root).as_posix() + "/" + name] = sha_file(output / name)
    require((output / "artifact.json").stat().st_size <= 4096, "ARTIFACT_SHAPE")
    raw = (output / "artifact.json").read_bytes()
    manifest = json.loads(raw, object_pairs_hook=unique_object)
    require(isinstance(manifest, dict) and set(manifest) == {
        "schema", "sources", "jar_sha256", "driver_sha256", "javac", "compiler_sha256", "class_sha256",
    }, "ARTIFACT_SHAPE")
    require(manifest["schema"] == 1 and manifest["sources"] ==
            {name: hashes[name] for name in SOURCES} and manifest["jar_sha256"] == JAR_SHA and
            manifest["driver_sha256"] == DRIVER_SHA and manifest["class_sha256"] ==
            sha_file(output / "classes/RuntimeJdbcProbe.class"), "ARTIFACT_IDENTITY")
    identity = hashlib.sha256(json.dumps(hashes, sort_keys=True).encode("ascii")).hexdigest()
    return {"bundle_sha256": identity, "input_sha256": hashes}


def native_java() -> Path:
    launcher = Path("/usr/bin/java")
    for _ in range(8):
        for parent in launcher.parents:
            secure_path(parent, directory=True)
        info = launcher.lstat()
        require(info.st_uid == 0, "PATH_OWNER")
        if not stat.S_ISLNK(info.st_mode):
            secure_path(launcher)
            return launcher
        target = launcher.readlink()
        launcher = Path(os.path.abspath(target if target.is_absolute() else launcher.parent / target))
    raise GateError("FILE_LINK")


def native_probe(plan: dict, root: Path, output: Path) -> str:
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    import ctypes
    import resource

    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    require(ctypes.CDLL(None).prctl(4, 0, 0, 0, 0) == 0, "PROCESS_DUMP_GUARD")
    os.umask(0o077)
    require(Path(__file__).absolute() == root / SOURCES[0], "SOURCE_IDENTITY")
    for name in plan["input_sha256"]:
        secure_path(root / name)
    secure_path(CONFIG, private=True, directory=True)
    java = native_java()
    machine = Path("/etc/machine-id")
    secure_path(machine)
    machine_hash = hashlib.sha256(machine.read_bytes().strip()).hexdigest()
    raw_approval = read_private(APPROVAL_PATH, 8192)
    approved = parse_approval(raw_approval, plan["bundle_sha256"], machine_hash,
                              datetime.now(timezone.utc))
    require(load_plan(root, output) == plan, "ARTIFACT_IDENTITY")
    smoke = checked_status(*run_child(java_command(java, output, "smoke")), mode="smoke")
    require(smoke == "DRIVER_LOADED", "JAVA17_REQUIRED")
    parse_approval(raw_approval, plan["bundle_sha256"], machine_hash, datetime.now(timezone.utc))
    password = parse_env(read_private(ENV_PATH, ENV_LIMIT))
    payload = frame(password, approved["server_uuid_sha256"])
    del password
    try:
        return checked_status(*run_child(java_command(java, output, "probe"), payload), mode="probe")
    finally:
        del payload


class SafeParser(argparse.ArgumentParser):
    def error(self, message: str) -> None:
        raise GateError("CLI_ARGUMENTS")


def main(argv: list[str] | None = None) -> int:
    result = {"wi": WI, "status": "HOLD", "production_status": "HOLD", "app_runnable": False,
              "hibernate": "NOT_RUN", "canonical_fresh_manifest": "UNRECORDED"}
    try:
        parser = SafeParser(description=__doc__)
        parser.add_argument("mode", choices=("prepare", "plan", "smoke", "probe"))
        parser.add_argument("--root", type=Path, default=Path(__file__).absolute().parents[2])
        parser.add_argument("--output", required=True)
        args = parser.parse_args(argv)
        root = args.root.absolute()
        output = output_path(root, args.output)
        if args.mode == "prepare":
            prepare(root, output)
        plan = load_plan(root, output)
        result.update(plan)
        if args.mode == "probe":
            status = native_probe(plan, root, output)
        elif args.mode == "smoke":
            java_name = shutil.which("java")
            require(java_name is not None, "JAVA17_REQUIRED")
            status = checked_status(*run_child(java_command(Path(java_name).resolve(strict=True),
                                                            output, "smoke")), mode="smoke")
        else:
            status = "PREPARED" if args.mode == "prepare" else "PLAN_ONLY"
        result["status"] = status
        print(json.dumps(result, sort_keys=True))
        return 0 if status in {"PASS", "DRIVER_LOADED", "PLAN_ONLY", "PREPARED"} else 2
    except GateError as error:
        result["rule"] = str(error) if str(error) in CODES else "UNEXPECTED_FAILURE"
    except KeyboardInterrupt:
        result["rule"] = "INTERRUPTED"
    except Exception:
        result["rule"] = "UNEXPECTED_FAILURE"
    print(json.dumps(result, sort_keys=True))
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
