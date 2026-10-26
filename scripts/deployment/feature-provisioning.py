"""WI063 first-admin candidate: local prepare/plan; native release is intentionally locked."""

from __future__ import annotations

from datetime import datetime, timedelta, timezone
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import struct
import sys
import zipfile


sys.dont_write_bytecode = True
SPEC = importlib.util.spec_from_file_location("wi063_jdbc_primitives", Path(__file__).with_name("jdbc-readiness.py"))
base = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(base)
GateError = base.GateError
require = base.require
REQ = "REQ-20261011-ATS-007"
WI = "WI-20261011-ATS-063"
NATIVE_RELEASED = False
SOURCES = ("scripts/deployment/feature-provisioning.py",
           "scripts/deployment/RuntimeFeatureProvisioner.java",
           "scripts/deployment/jdbc-readiness.py", "scripts/deployment/RuntimeJdbcProbe.java",
           "src/main/resources/schema.sql")
CLASSES = {"RuntimeFeatureProvisioner.class", "RuntimeFeatureProvisioner$Input.class",
           "RuntimeFeatureProvisioner$Outcome.class", "RuntimeJdbcProbe.class"}
CHECKS = ("explicit_designated_principal", "app_stopped", "competing_writers_fenced",
          "prechange_recovery_verified", "no_session_sql_or_process_capture",
          "same_target_boot_verified", "independent_review_same_bytes",
          "disposable_mysql_concurrency_proof")
IDENTITY_KEYS = ("bundle_sha256", "machine_id_sha256", "server_uuid_sha256", "metadata_sha256",
                 "user_id", "principal_sha256", "designation_sha256", "nonce")
APPROVAL = base.CONFIG / "feature-provisioning-approval.json"
CLAIM = base.CONFIG / "feature-first-admin.claim.json"
RECEIPT = base.CONFIG / "feature-first-admin.receipt.json"
STATUSES = {"READY", "APPLIED", "ALREADY_APPLIED", "HOLD_NATIVE_GATE", "HOLD_CONNECT",
            "HOLD_INPUT", "HOLD_DRIVER",
            "HOLD_SESSION", "HOLD_IDENTITY", "HOLD_ADMIN", "HOLD_TARGET", "HOLD_RECEIPT",
            "HOLD_WRITE", "HOLD_UNKNOWN"}


def canonical(value: object) -> bytes:
    return (json.dumps(value, sort_keys=True, separators=(",", ":")) + "\n").encode("ascii")


def object_json(raw: bytes) -> dict:
    require(0 < len(raw) <= 8192, "INPUT_SIZE")
    try:
        value = json.loads(raw, object_pairs_hook=base.unique_object)
        require(type(value) is dict, "INPUT_SHAPE")
        return value
    except (ValueError, TypeError):
        raise GateError("INPUT_SHAPE") from None


def parse_approval(raw: bytes, bundle: str, machine: str, now: datetime) -> dict:
    value = object_json(raw)
    require(set(value) == {*IDENTITY_KEYS, "req", "wi", "operation", "issued_at",
                           "expires_at", "operator_checks"}, "APPROVAL_SHAPE")
    require(value["req"] == REQ and value["wi"] == WI and
            value["operation"] in ("inspect", "promote-first-admin"), "APPROVAL_SCOPE")
    for key in IDENTITY_KEYS:
        pattern = r"[1-9][0-9]{0,18}" if key == "user_id" else (
            r"[a-f0-9]{32}" if key == "nonce" else r"[a-f0-9]{64}")
        require(isinstance(value[key], str) and re.fullmatch(pattern, value[key]) is not None,
                "APPROVAL_IDENTITY")
    require(int(value["user_id"]) <= 2**63 - 1 and value["bundle_sha256"] == bundle and
            value["machine_id_sha256"] == machine, "APPROVAL_IDENTITY")
    checks = value["operator_checks"]
    require(type(checks) is dict and set(checks) == set(CHECKS) and
            all(item is True for item in checks.values()), "APPROVAL_CHECKS")
    try:
        for key in ("issued_at", "expires_at"):
            require(isinstance(value[key], str) and re.fullmatch(
                r"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ", value[key]) is not None, "APPROVAL_TIME")
        issued = datetime.fromisoformat(value["issued_at"].replace("Z", "+00:00"))
        expires = datetime.fromisoformat(value["expires_at"].replace("Z", "+00:00"))
        require(issued <= now < expires <= issued + timedelta(minutes=15), "APPROVAL_EXPIRED")
    except ValueError:
        raise GateError("APPROVAL_TIME") from None
    return value


def intent(approved: dict) -> dict:
    return {"req": REQ, "wi": WI, **{key: approved[key] for key in IDENTITY_KEYS}}


def frame(password: str, approved: dict, audit_id: int) -> bytes:
    require(isinstance(password, str) and re.fullmatch(r"[!-~]{32}", password) is not None,
            "PASSWORD_SHAPE")
    require(type(audit_id) is int and 0 <= audit_id <= 2**63 - 1, "RECEIPT_SHAPE")
    fields = (password, approved["server_uuid_sha256"], approved["metadata_sha256"],
              approved["user_id"], approved["principal_sha256"], approved["nonce"], str(audit_id))
    return b"AFP1" + b"".join(struct.pack(">I", len(item)) + item.encode("ascii") for item in fields)


def checked_status(code: int, raw: bytes) -> tuple[str, int]:
    match = re.fullmatch(rb"([A-Z_]+):(0|[1-9][0-9]{0,18})\n", raw)
    require(match is not None, "JAVA_PROTOCOL")
    status, identity = match.groups()
    status, audit_id = status.decode("ascii"), int(identity)
    require(status in STATUSES and audit_id <= 2**63 - 1, "JAVA_PROTOCOL")
    require((status in {"APPLIED", "ALREADY_APPLIED"}) == (audit_id > 0), "JAVA_PROTOCOL")
    require(code == (2 if status.startswith("HOLD_") else 0), "JAVA_PROTOCOL")
    return status, audit_id


def sync_directory(directory: Path) -> None:
    descriptor = os.open(directory, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
    try:
        os.fsync(descriptor)
    finally:
        os.close(descriptor)


def persist_new(path: Path, value: dict) -> None:
    flags = os.O_WRONLY | os.O_CREAT | os.O_EXCL | getattr(os, "O_NOFOLLOW", 0)
    descriptor = os.open(path, flags, 0o600)
    with os.fdopen(descriptor, "wb") as stream:
        stream.write(canonical(value))
        stream.flush()
        os.fsync(stream.fileno())
    sync_directory(path.parent)


def receipt_id(receipt: dict, expected: dict) -> int:
    require(type(receipt) is dict and set(receipt) == {"intent", "status", "audit_id"} and
            receipt["intent"] == expected and receipt["status"] == "APPLIED" and
            type(receipt["audit_id"]) is int and 0 < receipt["audit_id"] <= 2**63 - 1,
            "RECEIPT_MISMATCH")
    return receipt["audit_id"]


def output_path(root: Path, relative: str) -> Path:
    require(re.fullmatch(r"build/WI063-[A-Za-z0-9-]+", relative) is not None, "OUTPUT_PATH")
    base.no_links(root / "build")
    return root / relative


def source_pins(root: Path) -> dict:
    return {name: base.sha_file(root / name) for name in (*SOURCES, base.JAR)}


def prepare(root: Path, output: Path) -> None:
    pins = source_pins(root)
    compiler_name = shutil.which("javac")
    require(compiler_name is not None, "JAVA17_REQUIRED")
    compiler = Path(compiler_name).resolve(strict=True)
    code, version = base.run_child([str(compiler), "-version"])
    require(code == 0 and re.fullmatch(rb"javac 17\.\d+\.\d+\r?\n", version) is not None,
            "JAVA17_REQUIRED")
    with zipfile.ZipFile(root / base.JAR) as archive:
        entries = [entry for entry in archive.infolist() if entry.filename == "BOOT-INF/lib/" + base.DRIVER]
        require(len(entries) == 1 and entries[0].file_size == base.DRIVER_SIZE, "DRIVER_IDENTITY")
        driver = archive.read(entries[0])
    require(hashlib.sha256(driver).hexdigest() == base.DRIVER_SHA, "DRIVER_IDENTITY")
    output.mkdir(mode=0o700, exist_ok=False)
    (output / "classes").mkdir(mode=0o700)
    with (output / base.DRIVER).open("xb") as stream:
        stream.write(driver)
    code, _ = base.run_child([str(compiler), "--release", "17", "-encoding", "UTF-8", "-Xlint:all",
                              "-d", str(output / "classes"), str(root / SOURCES[1]), str(root / SOURCES[3])])
    require(code == 0, "COMPILE_FAILED")
    require(pins == source_pins(root), "SOURCE_DRIFT")
    manifest = {"sources": pins, "driver_sha256": base.DRIVER_SHA,
                "classes": {name: base.sha_file(output / "classes" / name) for name in sorted(CLASSES)}}
    with (output / "artifact.json").open("xb") as stream:
        stream.write(canonical(manifest))


def load_plan(root: Path, output: Path) -> dict:
    base.no_links(output)
    require({path.name for path in output.iterdir()} == {"classes", "artifact.json", base.DRIVER},
            "ARTIFACT_SHAPE")
    base.no_links(output / "classes")
    require({path.name for path in (output / "classes").iterdir()} == CLASSES, "ARTIFACT_SHAPE")
    base.no_links(output / "artifact.json")
    require((output / "artifact.json").stat().st_size <= 8192, "ARTIFACT_SHAPE")
    manifest = object_json((output / "artifact.json").read_bytes())
    expected = {"sources": source_pins(root), "driver_sha256": base.sha_file(output / base.DRIVER),
                "classes": {name: base.sha_file(output / "classes" / name) for name in sorted(CLASSES)}}
    require(manifest == expected and expected["driver_sha256"] == base.DRIVER_SHA, "ARTIFACT_IDENTITY")
    return {"bundle_sha256": hashlib.sha256(canonical(expected)).hexdigest(), "pins": expected}


def native(plan: dict, root: Path, output: Path, operation: str) -> str:
    """Dormant MA path; no switch can bypass the independent native-release gate."""
    require(NATIVE_RELEASED, "NATIVE_GATE_LOCKED")
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    import ctypes
    import resource

    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    require(ctypes.CDLL(None).prctl(4, 0, 0, 0, 0) == 0, "PROCESS_DUMP_GUARD")
    os.umask(0o077)
    require(Path(__file__).absolute() == root / SOURCES[0], "SOURCE_IDENTITY")
    for name in (*SOURCES, base.JAR):
        base.secure_path(root / name)
    for path in (output / "artifact.json", output / base.DRIVER,
                 *(output / "classes" / name for name in CLASSES)):
        base.secure_path(path)
    base.secure_path(base.CONFIG, directory=True, private=True)
    base.secure_path(Path("/etc/machine-id"))
    machine = hashlib.sha256(Path("/etc/machine-id").read_bytes().strip()).hexdigest()
    raw = base.read_private(APPROVAL, 8192)
    approved = parse_approval(raw, plan["bundle_sha256"], machine, datetime.now(timezone.utc))
    require(approved["operation"] == operation, "APPROVAL_SCOPE")
    expected = intent(approved)
    claimed = CLAIM.exists() or CLAIM.is_symlink()
    prior = RECEIPT.exists() or RECEIPT.is_symlink()
    audit_id = 0
    if prior:
        require(claimed and object_json(base.read_private(CLAIM, 8192)) == expected, "CLAIM_MISMATCH")
        audit_id = receipt_id(object_json(base.read_private(RECEIPT, 8192)), expected)
    elif claimed:
        require(operation == "inspect" and object_json(base.read_private(CLAIM, 8192)) == expected,
                "CLAIM_CONSUMED")
    java = base.native_java()
    require(load_plan(root, output) == plan, "ARTIFACT_IDENTITY")
    if operation == "promote-first-admin" and not prior:
        persist_new(CLAIM, expected)
    parse_approval(raw, plan["bundle_sha256"], machine, datetime.now(timezone.utc))
    password = base.parse_env(base.read_private(base.ENV_PATH, base.ENV_LIMIT))
    payload = frame(password, approved, audit_id)
    del password
    command = base.java_command(java, output, operation)
    command[-2] = "RuntimeFeatureProvisioner"
    try:
        status, observed_id = checked_status(*base.run_child(command, payload))
    finally:
        del payload
    expected_status = "ALREADY_APPLIED" if prior else (
        "APPLIED" if operation == "promote-first-admin" else "READY")
    require(status.startswith("HOLD_") or status == expected_status, "JAVA_PROTOCOL")
    if status == "APPLIED":
        require(operation == "promote-first-admin" and not prior, "JAVA_PROTOCOL")
        persist_new(RECEIPT, {"intent": expected, "status": status, "audit_id": observed_id})
    elif status == "ALREADY_APPLIED":
        require(prior and observed_id == audit_id, "RECEIPT_MISMATCH")
    if claimed and not prior:
        return "HOLD_RECONCILE"
    return status


def main(argv: list[str] | None = None) -> int:
    argv = sys.argv[1:] if argv is None else argv
    result = {"wi": WI, "status": "HOLD", "native": "NOT_RUN", "buyer_grant": "DEFERRED"}
    try:
        parser = base.SafeParser(description=__doc__)
        parser.add_argument("mode", choices=("prepare", "plan", "inspect", "promote-first-admin"))
        parser.add_argument("--output", required=True)
        args = parser.parse_args(argv)
        root = Path(__file__).absolute().parents[2]
        output = output_path(root, args.output)
        if args.mode in ("inspect", "promote-first-admin"):
            require(NATIVE_RELEASED, "NATIVE_GATE_LOCKED")
        if args.mode == "prepare":
            prepare(root, output)
        plan = load_plan(root, output)
        if args.mode in ("inspect", "promote-first-admin"):
            result["native"] = "ATTEMPTED"
            result["status"] = native(plan, root, output, args.mode)
        else:
            result.update(plan)
            result["status"] = "PREPARED" if args.mode == "prepare" else "PLAN_ONLY"
        print(json.dumps(result, sort_keys=True))
        return 2 if result["status"].startswith("HOLD") else 0
    except (Exception, KeyboardInterrupt):
        result["rule"] = "NATIVE_GATE_LOCKED" if not NATIVE_RELEASED and argv and \
            argv[0] in ("inspect", "promote-first-admin") else "LOCAL_OR_PRIVATE_GATE_REJECTED"
        print(json.dumps(result, sort_keys=True))
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
