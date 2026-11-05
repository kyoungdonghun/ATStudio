"""WI022 LF package/assembly and read-only target probe. No default execution."""
from __future__ import annotations

import argparse
import copy
import json
import os
from pathlib import Path, PurePosixPath
import socket
import sys
import time
import subprocess

from backup_protocol import digest, encoded, exact_id, require, timestamp, validate_config, recurring_subject, recurring_approval
from scheduled_dispatch import load, read, write_new, sync_dir

FILES = ("backup_protocol.py", "backup_runtime.py", "guarded_backup.py", "scheduled_dispatch.py",
         "host_adapter.py", "native_trial.py")
BOOTSTRAP = '''import base64,hashlib,json,sys,types
p=json.loads(sys.stdin.buffer.read(2097153))
assert set(p['code'])=={'backup_protocol.py','backup_runtime.py','guarded_backup.py','scheduled_dispatch.py','host_adapter.py','native_trial.py'}
for name in ('backup_protocol.py','backup_runtime.py','scheduled_dispatch.py','host_adapter.py','guarded_backup.py','native_trial.py'):
 row=p['code'][name]; raw=base64.b64decode(row['base64'],validate=True)
 assert hashlib.sha256(raw).hexdigest()==row['sha256']
 module=types.ModuleType(name[:-3]);module.__file__='/nonexistent/atstudio-readonly/'+name
 sys.modules[module.__name__]=module
 exec(compile(raw,module.__file__,'exec'),module.__dict__)
print(json.dumps(sys.modules['native_trial'].native_probe(p['spec']),sort_keys=True))
'''


def remote_root(value):
    path = PurePosixPath(value)
    require(str(path) == value and path.is_absolute() and ".." not in path.parts and str(path).startswith("/root/atstudio-trials/wi022-")
            and len(path.parts) == 4 and all(c.isalnum() or c in "/-_" for c in value), "FRESH_REMOTE_ROOT")
    return path


def windows_private_path(path, *, create=False):
    shell = Path(os.environ["SystemRoot"]) / "System32/WindowsPowerShell/v1.0/powershell.exe"
    result = subprocess.run([str(shell), "-NoProfile", "-NonInteractive", "-File",
                             str(Path(__file__).with_name("private-custody.ps1")),
                             "-Action", "Create" if create else "Verify", "-Path", str(path.absolute())],
                            capture_output=True, timeout=30)
    require(result.returncode == 0 and result.stdout.strip() == b"PRIVATE_CUSTODY_OK", "PRIVATE_OUTPUT_ACL")


def private_directory(path, *, create=False):
    """Create with a private DACL on Windows; fail closed before input writes."""
    path = Path(path)
    if os.name == "nt":
        windows_private_path(path, create=create)
        require(path.is_dir(), "PRIVATE_OUTPUT_DIRECTORY")
    else:
        if create:
            path.mkdir(mode=0o700)
        import stat
        info = path.lstat()
        require(stat.S_ISDIR(info.st_mode) and info.st_uid == os.geteuid() and
                stat.S_IMODE(info.st_mode) == 0o700, "PRIVATE_OUTPUT_MODE")


def bind_trial_paths(config, root, credential_references):
    """Only named runtime fields move; retained credential references never do."""
    require(set(credential_references) == {"password_file", "aws_credentials_file"}, "CREDENTIAL_BINDING")
    for key, value in credential_references.items():
        require(config[key] == value and str(PurePosixPath(value)) == value and
                ".." not in PurePosixPath(value).parts and PurePosixPath(value).is_absolute() and
                not value.startswith("/root/atstudio-trials/"), "CREDENTIAL_BINDING")
    for key, name in (("state_dir", "state"), ("work_dir", "work"), ("cache_dir", "cache"), ("temp_dir", "temp")):
        config[key] = str(root / name)
    config["mysql_defaults_file"] = str(root / "inputs" / "mysql-unused.cnf")
    return config


def private_write_new(path, value):
    path = Path(path)
    private_directory(path.parent)
    if os.name != "nt":
        write_new(path, value)
        return
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(fd, "wb") as stream:
        windows_private_path(path)
        stream.write(encoded(value))
        stream.flush()
        os.fsync(stream.fileno())
    sync_dir(path.parent)


def boot_identity():
    # procfs reports st_size=0 for this nonempty kernel attribute; regular-file
    # exact-size readers intentionally cannot be reused here.
    with Path("/proc/sys/kernel/random/boot_id").open("rb") as stream:
        data = stream.read(128)
    import re
    require(re.fullmatch(rb"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\n?", data) is not None, "BOOT_ATTRIBUTE")
    return digest(data)


def verify_quarantine_response(spec, response, observed_config_sha256):
    expected = spec["expected_http_status"]
    require(type(expected) is int and expected in {404, 503}, "QUARANTINE_STATUS_BINDING")
    require(exact_id(spec["quarantine_sha256"]) == exact_id(observed_config_sha256), "QUARANTINE_CONFIG_DRIFT")
    require(response == str(expected).encode("ascii"), "HTTP_QUARANTINE_STATUS_MISMATCH")


def package(destination, remote):
    target = remote_root(remote)
    destination = Path(destination)
    require(not destination.exists(), "DESTINATION_EXISTS")
    private_directory(destination, create=True)
    code = destination / "code"
    private_directory(code, create=True)
    manifest = {}
    for name in FILES:
        source = read(Path(__file__).parent / name, 1024 * 1024)
        payload = source.replace(b"\r\n", b"\n")
        require(b"\r" not in payload, "SOURCE_LINE_ENDINGS")
        compile(payload, name, "exec")
        if name == "host_adapter.py":
            require(payload.startswith(b"#!/usr/bin/python3\n"), "SHEBANG")
            payload = payload.replace(b"#!/usr/bin/python3\n", b"#!/usr/bin/python3 -B\n", 1)
        path = code / name
        fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o700 if name == "host_adapter.py" else 0o600)
        with os.fdopen(fd, "wb") as stream:
            stream.write(payload)
        manifest[name] = {"source_sha256": digest(source), "payload_sha256": digest(payload),
                          "remote": str(target / "code" / name), "mode": "0700" if name == "host_adapter.py" else "0600"}
    write_new(destination / "manifest.json", {"version": 1, "remote_root": str(target), "files": manifest})
    (destination / "probe-bootstrap.py").write_bytes(BOOTSTRAP.encode("ascii"))
    return {"status": "PACKAGED_NOT_DEPLOYED", "files": len(manifest), "manifest_sha256": digest(read(destination / "manifest.json"))}


def probe_payload(bundle, spec, destination):
    import base64
    bundle = Path(bundle)
    manifest = load(bundle / "manifest.json")
    require(spec["remote_root"] == manifest["remote_root"], "PROBE_TARGET_ROOT")
    code = {}
    for name in FILES:
        data = read(bundle / "code" / name, 1024 * 1024)
        require(digest(data) == manifest["files"][name]["payload_sha256"], "PAYLOAD_DRIFT")
        code[name] = {"base64": base64.b64encode(data).decode("ascii"), "sha256": digest(data)}
    payload = {"code": code, "spec": spec}
    require(len(encoded(payload)) <= 2 * 1024 * 1024, "PROBE_PAYLOAD_CAP")
    private_write_new(Path(destination), payload)
    return {"status": "PROBE_PAYLOAD_PREPARED_NOT_EXECUTED", "sha256": digest(encoded(payload))}


def native_probe(spec):
    """No DB/restic/app start and no writes. Private result must be kept private."""
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    from backup_runtime import checked_path, inventory
    from host_adapter import Commands, UNITS, FIELDS, properties, MASKED
    require(socket.gethostname() == spec["target_host"] and
            digest(read(Path("/etc/machine-id"))) == exact_id(spec["machine_sha256"]), "TARGET_IDENTITY")
    target_root = Path(str(remote_root(spec["remote_root"])))
    phase = spec.get("phase", "preflight")
    require(phase in {"preflight", "final"}, "PROBE_PHASE")
    if phase == "preflight":
        require(not target_root.exists(), "REMOTE_DESTINATION_EXISTS")
    else:
        checked_path(target_root, private=True)
        require(boot_identity() == spec["boot_sha256"], "BOOT_DRIFT")
        require(set(spec["deployed_code_pins"]) == {str(target_root / "code" / n) for n in FILES}, "FINAL_CLOSURE")
        for path, sha in spec["deployed_code_pins"].items():
            require(digest(read(checked_path(path, trusted=True))) == sha, "DEPLOYED_CODE_DRIFT")
    command = Commands()
    units = {u: properties(command.run(["/usr/bin/systemctl", "show", "--no-pager", "--property=" + FIELDS, "--", u])) for u in UNITS}
    if phase == "final":
        require(units == spec["baseline_units"], "FINAL_BASELINE_DRIFT")
        require(len(spec["run_id"]) == 32 and all(c in "0123456789abcdef" for c in spec["run_id"]), "RUN_ID")
        owned = properties(command.run(["/usr/bin/systemctl", "show", "--no-pager", "--property=MainPID,ControlPID", "--", "atstudio-cold-" + spec["run_id"] + ".service"]))
        require(owned == {"MainPID": "0", "ControlPID": "0"} and not Path("/run/mysqld").exists()
                and not Path("/run/mysqld").is_symlink(), "FINAL_OWNED_DB_REMAINS")
    for name, row in units.items():
        if name == "nginx.service":
            require(row["ActiveState"] == "active" and row["SubState"] == "running" and int(row["MainPID"]) > 0 and row["ControlPID"] == "0", "NGINX_BASELINE")
        else:
            require(row["ActiveState"] in {"inactive", "failed"} and row["MainPID"] == row["ControlPID"] == "0" and
                    (name not in MASKED or row["UnitFileState"] == "masked"), "WRITER_ACTIVE")
    for proc in Path("/proc").iterdir():
        if proc.name.isdigit():
            try:
                name = (proc / "comm").read_text().strip()
            except FileNotFoundError:
                continue
            require(name not in {"java", "ffmpeg", "mysqld", "mariadbd"}, "WRITER_PROCESS")
    require(not Path("/run/mysqld").exists() and not Path("/run/mysqld").is_symlink(), "DB_RUNTIME_PRESENT")
    # TLS is verified against the reviewed hostname; only loopback is contacted.
    import re
    host = spec["https_host"]
    require(re.fullmatch(r"[A-Za-z0-9](?:[A-Za-z0-9.-]{0,251}[A-Za-z0-9])?", host) is not None, "HTTPS_HOST")
    http = command.run(["/usr/bin/curl", "--silent", "--show-error", "--noproxy", "*", "--proto", "=https",
                        "--connect-timeout", "3", "--max-time", "8", "--resolve", host + ":443:127.0.0.1",
                        "--output", "/dev/null", "--write-out", "%{http_code}", "https://" + host + "/"], cap=32, seconds=10)
    quarantine = checked_path(spec["quarantine_file"], trusted=True)
    verify_quarantine_response(spec, http, digest(read(quarantine)))
    paths = [Path(p) for p in spec["source_paths"]]
    for path in paths:
        checked_path(path)
    limits = spec["probe_limits"]
    members = inventory(paths, lambda: None, limits["max_entries"], limits["source_bytes"], limits["metadata_bytes"], {str(p): p.stat().st_dev for p in paths})
    files = {p: row["sha256"] for p, row in members.items() if row["type"] == "file"}
    require(files == spec["approved_files"], "APPROVED_SOURCE_SCOPE_DRIFT")
    artifacts = {p: digest(read(checked_path(p, trusted=True), 64 * 1024 * 1024)) for p in spec["artifact_paths"]}
    mysql_config = spec["mysql_config"]
    require(mysql_config in artifacts, "MYSQL_CONFIG_PIN_REQUIRED")
    command.run(["/usr/sbin/runuser", "-u", "mysql", "--", "/usr/bin/test", "-r", mysql_config], cap=16, seconds=5)
    config_bytes = read(Path(mysql_config), 1024 * 1024)
    require(not any(line.lstrip().lower().startswith(b"!include") for line in config_bytes.splitlines()), "MYSQL_CONFIG_INCLUDE_REVIEW")
    for path in spec["credential_references"]:
        checked_path(path, private=True)  # metadata only, never contents
    devices, capacities = {}, {}
    for requested in spec["filesystem_paths"]:
        path = Path(requested)
        while not path.exists():
            require(path != path.parent, "FILESYSTEM_PARENT")
            path = path.parent
        checked_path(path)
        row = os.statvfs(path)
        free, total = row.f_bavail * row.f_frsize, row.f_blocks * row.f_frsize
        reserve = max(total // 5, 10 * 1024**3)
        require(free >= reserve + limits["projected_local_growth"] and row.f_files > 0 and row.f_favail >= row.f_files // 10, "RESOURCE_CAPACITY")
        devices[requested] = path.stat().st_dev
        capacities[requested] = {"free_bytes": free, "reserve_bytes": reserve}
    available = next(int(line.split()[1]) * 1024 for line in Path("/proc/meminfo").read_text().splitlines() if line.startswith("MemAvailable:"))
    require(available >= 1024**3, "MEMORY_RESERVE")
    require(Path("/sys/fs/cgroup/cgroup.controllers").exists(), "CGROUP_V2_REQUIRED")
    return {"status": "OBSERVED_NOT_AUTHORITY", "observed_at": time.time(), "target_host": spec["target_host"],
            "phase": phase,
            "machine_sha256": spec["machine_sha256"], "boot_sha256": boot_identity(),
            "units": units, "http_status": spec["expected_http_status"], "quarantine_sha256": spec["quarantine_sha256"],
            "artifact_pins": artifacts, "members_sha256": digest(encoded(members)),
            "source_bytes": sum(x.get("size", 0) for x in members.values()), "expected_filesystems": devices,
            "capacities": capacities, "memory_available": available, "db_auth": "RETAINED_PROOF_ONLY_NOT_NEWLY_TESTED"}


def assemble(bundle, spec):
    """Consume MA-only explicit inputs/original records; never invent observations."""
    bundle = Path(bundle)
    private_directory(bundle)
    manifest = load(bundle / "manifest.json")
    root = remote_root(manifest["remote_root"])
    require(set(manifest["files"]) == set(FILES), "PAYLOAD_CLOSURE")
    for name, row in manifest["files"].items():
        require(digest(read(bundle / "code" / name)) == row["payload_sha256"], "PAYLOAD_DRIFT")
    config = copy.deepcopy(spec["config"])
    require(config["maintenance_mode"] == "cold-preserve", "COLD_ONLY")
    require(type(spec["expected_http_status"]) is int and spec["expected_http_status"] in {404, 503}, "QUARANTINE_STATUS_BINDING")
    config["cold_host"]["quarantine_http_status"] = spec["expected_http_status"]
    source_paths = [config[k] for k in ("public_root", "private_root", "release_root")] + config["settings"]
    require(source_paths == spec["approved_source_paths"], "APPROVED_SOURCE_SCOPE")
    bind_trial_paths(config, root, spec["credential_references"])
    config["hook_binary"] = str(root / "code" / "host_adapter.py")
    config["hook_sha256"] = manifest["files"]["host_adapter.py"]["payload_sha256"]
    from host_adapter import ColdHost, UNITS, MASKED
    ColdHost(config, Path(config["work_dir"]) / spec["run_id"], None)
    host = config["cold_host"]
    for key in ("machine_sha256", "boot_sha256", "quarantine_sha256"):
        exact_id(host[key])
    require(set(host["baseline_units"]) == set(UNITS), "BASELINE_UNIT_CLOSURE")
    for name, row in host["baseline_units"].items():
        require(set(row) == {"LoadState", "ActiveState", "SubState", "MainPID", "ControlPID", "UnitFileState"}, "UNIT_FIELDS")
        if name == "nginx.service":
            require(row["ActiveState"] == "active" and row["SubState"] == "running" and row["MainPID"].isdigit()
                    and int(row["MainPID"]) > 0 and row["ControlPID"] == "0" and row["UnitFileState"] == "enabled", "NGINX_BASELINE")
        else:
            require(row["MainPID"] == row["ControlPID"] == "0" and row["ActiveState"] in {"inactive", "failed"}
                    and (name not in MASKED or row["UnitFileState"] == "masked"), "COLD_BASELINE")
    required_artifacts = {"/usr/bin/systemctl", "/usr/bin/systemd-run", "/usr/sbin/mysqld", "/usr/bin/mysql", "/usr/bin/mysqldump", "/etc/apparmor.d/usr.sbin.mysqld", host["mysql_config"]}
    require(required_artifacts <= set(host["artifact_pins"]), "ARTIFACT_CLOSURE")
    for path, sha in host["artifact_pins"].items():
        require(PurePosixPath(path).is_absolute() and ".." not in PurePosixPath(path).parts, "ARTIFACT_PATH")
        exact_id(sha)
    config["cold_host"]["evidence"] = {}
    subject = copy.deepcopy(config)
    del subject["cold_host"]["evidence"]
    subject_hash = digest(encoded(subject))
    inputs = bundle / "inputs"
    require(not inputs.exists(), "INPUTS_EXIST")
    private_directory(inputs, create=True)
    for kind in ("repository_acl", "recovery_access", "source_inventory", "cost"):
        ref = spec["observed_originals"][kind]
        raw = read(Path(ref["path"]))
        require(digest(raw) == exact_id(ref["sha256"]), "ORIGINAL_PIN")
        original = json.loads(raw)
        require(original["record_type"] == "MA_OBSERVED_ORIGINAL" and original["kind"] == kind and
                original["repository_id"] == config["repository_id"] and original["machine_sha256"] == config["cold_host"]["machine_sha256"] and
                original["boot_sha256"] == config["cold_host"]["boot_sha256"], "ORIGINAL_SCOPE")
        # Fixed payload keys; a prior error, declaration or synthetic fixture is
        # never upgraded here. Authority still comes from review of raw originals.
        require(original["classification"] == "OBSERVED" and original["synthetic"] is False, "ORIGINAL_NOT_OBSERVED")
        origins = original["originrefs"]
        require(isinstance(origins, list) and 1 <= len(origins) <= 8, "PRESERVED_ORIGINS_REQUIRED")
        for origin in origins:
            require(origin["path"] != ref["path"] and digest(read(Path(origin["path"]), 16 * 1024 * 1024)) == exact_id(origin["sha256"]), "ORIGIN_PIN")
        proof = {k: original[k] for k in ("kind", "repository_id", "machine_sha256", "boot_sha256", "observed_at", "expires_at")}
        fields = {"repository_acl": ("access",), "recovery_access": ("operation", "snapshot_id"),
                  "source_inventory": ("members_sha256",), "cost": ("upper_bound_krw", "approved_limit_krw", "new_bytes_bound", "scope", "unknown_components")}[kind]
        proof.update({k: original[k] for k in fields})
        proof.update(version=1, source_evidence_sha256=digest(raw), subject_sha256=subject_hash)
        require(timestamp(proof["observed_at"]) <= time.time() and
                time.time() + config["limits"]["seconds"] <= timestamp(proof["expires_at"]), "ORIGINAL_STALE")
        require(timestamp(proof["expires_at"]) - timestamp(proof["observed_at"]) <= 6 * 3600, "ORIGINAL_WINDOW")
        if kind == "repository_acl":
            require(proof["access"] == "PRIVATE_VERIFIED", "PRIVATE_ACL_REQUIRED")
        elif kind == "recovery_access":
            require(proof["operation"] == "EXACT_SNAPSHOT_READ_VERIFIED", "READ_PROOF_REQUIRED")
            exact_id(proof["snapshot_id"])
        elif kind == "source_inventory":
            require(proof["members_sha256"] == spec["observed_members_sha256"], "OBSERVED_INVENTORY_BINDING")
        else:
            limits = config["limits"]
            require(type(proof["upper_bound_krw"]) is int and proof["upper_bound_krw"] == 0 and
                    type(proof["approved_limit_krw"]) is int and proof["approved_limit_krw"] == 0 and
                    limits["projected_cost_krw"] == limits["cost_limit_krw"] == 0 and
                    proof["unknown_components"] == [] and proof["scope"] == "ONE_COLD_CAPTURE", "ZERO_COST_ONLY")
            require(type(proof["new_bytes_bound"]) is int and proof["new_bytes_bound"] >=
                    sum(limits[k] for k in ("source_bytes", "dump_bytes", "metadata_bytes")), "COST_BYTE_BOUND")
        filename = kind + ".json"
        private_write_new(inputs / filename, proof)
        config["cold_host"]["evidence"][kind] = {"path": str(root / "inputs" / filename), "sha256": digest(encoded(proof))}
    validate_config(config)
    require(set(source_paths + [config[k] for k in ("state_dir", "work_dir", "cache_dir", "temp_dir")]) <=
            set(config["expected_filesystems"]), "MOUNT_COVERAGE")
    require(config["limits"]["dump_bytes"] <= 16 * 1024**2, "COLD_DUMP_MEMORY_CAP")
    require(config["limits"]["source_bytes"] >= spec["observed_source_bytes"] + config["limits"]["dump_bytes"], "SOURCE_PLUS_DUMP_REQUIRED")
    import math
    limits = config["limits"]
    seconds_floor = 360 + math.ceil((limits["source_bytes"] + limits["metadata_bytes"]) / (1024 * limits["upload_kib"])) + math.ceil(limits["retrieval_bytes"] / (1024 * limits["download_kib"]))
    require(seconds_floor <= limits["seconds"] <= 1800, "TRANSFER_WINDOW_TOO_SHORT")
    private_write_new(inputs / "config.json", config)
    # Approval is separately issued by MA after current cost/target validation.
    plan = {"status": "ASSEMBLED_NOT_AUTHORIZED", "config_sha256": digest(encoded(config)),
            "remote_root": str(root), "approval": "NOT_ISSUED", "directory_modes": "0700", "input_modes": "0600",
            "command": ["/usr/bin/systemd-run", "--quiet", "--wait", "--collect", "--unit=atstudio-trial-" + spec["run_id"],
                        "--property=Type=exec", "--property=User=root", "--property=UMask=0077", "--property=MemoryHigh=768M",
                        "--property=MemoryMax=1G", "--property=MemorySwapMax=0", "--property=CPUQuota=100%",
                        "--property=Nice=10", "--property=KillMode=control-group", "--property=TimeoutStopSec=35",
                        "--property=RuntimeMaxSec=" + str(config["limits"]["seconds"] + 180),
                        "--property=StandardOutput=append:" + config["state_dir"] + "/executor.stdout",
                        "--property=StandardError=append:" + config["state_dir"] + "/executor.stderr", "--", "/usr/bin/python3", "-B",
                        str(root / "code" / "guarded_backup.py"), "execute", "--config", str(root / "inputs" / "config.json"),
                        "--approval", str(root / "inputs" / "approval.json")],
            "cleanup_command": ["/usr/bin/python3", "-B", str(root / "code" / "host_adapter.py"), "cleanup",
                                str(root / "inputs" / "config.json"), str(root / "work" / spec["run_id"])],
            "final_probe_binding": {"phase": "final", "remote_root": str(root), "run_id": spec["run_id"],
                                    "expected_http_status": spec["expected_http_status"],
                                    "quarantine_file": host["quarantine_file"], "quarantine_sha256": host["quarantine_sha256"],
                                    "boot_sha256": host["boot_sha256"], "baseline_units": host["baseline_units"],
                                    "deployed_code_pins": {row["remote"]: row["payload_sha256"] for row in manifest["files"].values()}}}
    require(len(spec["run_id"]) == 32 and all(c in "0123456789abcdef" for c in spec["run_id"]), "RUN_ID")
    private_write_new(bundle / "execution-plan.json", plan)
    private_write_new(bundle / "custody.json", {"observed_originals": spec["observed_originals"],
                                       "authority": "MA_REVIEW_REQUIRED", "config_sha256": plan["config_sha256"]})
    return {"status": plan["status"], "config_sha256": plan["config_sha256"], "approval": "NOT_ISSUED"}


def assemble_scheduled(bundle, spec):
    """Explicit one-attempt assembly only; never execute or reset a ledger."""
    from datetime import datetime, timedelta
    import re
    bundle = Path(bundle)
    private_directory(bundle)
    schedule = spec["scheduled"]
    require(set(schedule) == {"policy_id", "kst_date", "start_hhmm", "start_grace_seconds",
                             "window_seconds", "not_before", "expires_at", "python", "code_pins", "budget"}, "SCHEDULE_FIELDS")
    require(isinstance(schedule["policy_id"], str) and
            re.fullmatch(r"[a-zA-Z0-9_-]{1,64}", schedule["policy_id"]), "POLICY_ID")
    start_text = schedule["kst_date"] + "T" + schedule["start_hhmm"] + ":00+09:00"
    start = datetime.fromisoformat(start_text)
    require(start.isoformat() == start_text and start.timestamp() >= time.time(), "SCHEDULE_START")
    require(type(schedule["window_seconds"]) is int and schedule["window_seconds"] > 0, "RECURRING_WINDOW")
    end = start + timedelta(seconds=schedule["window_seconds"])
    require(schedule["not_before"] == start.isoformat() and schedule["expires_at"] == end.isoformat(), "SCHEDULE_AUTHORITY_WINDOW")
    run_id = digest(encoded([schedule["policy_id"], schedule["kst_date"], "daily-cold-capture"]))[:32]
    require("run_id" not in spec or spec["run_id"] == run_id, "SCHEDULE_RUN_ID")
    manifest = load(bundle / "manifest.json")
    root = remote_root(manifest["remote_root"])
    require(set(manifest["files"]) == set(FILES), "PAYLOAD_CLOSURE")
    python = schedule["python"]
    require(set(python) == {"resolved_path", "sha256"}, "PYTHON_IDENTITY")
    path = PurePosixPath(python["resolved_path"])
    require(path.is_absolute() and str(path) == python["resolved_path"] and ".." not in path.parts,
            "PYTHON_IDENTITY")
    exact_id(python["sha256"])
    code_pins = {str(root / "code" / name): row["payload_sha256"] for name, row in manifest["files"].items()}
    require(python["resolved_path"] not in code_pins, "PYTHON_IDENTITY")
    code_pins[python["resolved_path"]] = python["sha256"]
    require(schedule["code_pins"] == code_pins, "SCHEDULE_CODE_PINS")
    for name, row in manifest["files"].items():
        require(row["remote"] == str(root / "code" / name) and
                digest(read(bundle / "code" / name)) == row["payload_sha256"], "PAYLOAD_DRIFT")
    # Preview exactly the named changes performed by assemble, without reading secrets.
    config = copy.deepcopy(spec["config"])
    bind_trial_paths(config, root, spec["credential_references"])
    config["hook_binary"] = str(root / "code" / "host_adapter.py")
    config["hook_sha256"] = code_pins[config["hook_binary"]]
    config["cold_host"]["quarantine_http_status"] = spec["expected_http_status"]
    config["cold_host"]["evidence"] = {}
    policy = {"version": 2, "enabled": True, "policy_id": schedule["policy_id"],
              "config_subject_sha256": recurring_subject(config), "code_pins": code_pins,
              "operation": "daily-cold-capture", "timezone": "Asia/Seoul",
              "start_hhmm": schedule["start_hhmm"], "start_grace_seconds": schedule["start_grace_seconds"],
              "window_seconds": schedule["window_seconds"], "not_before": start.isoformat(),
              "expires_at": end.isoformat(), "resume_authorized": True}
    require(recurring_approval(config, policy, start.timestamp())["run_id"] == run_id, "SCHEDULE_RUN_ID")
    bounds = schedule["budget"]
    fields = {"max_attempts", "per_attempt_bytes", "per_attempt_cost_krw", "existing_bytes",
              "reserve_bytes", "capacity_bytes", "cost_cap_krw", "max_output_bytes"}
    require(set(bounds) == fields and all(type(value) is int and value >= 0 for value in bounds.values()), "FINITE_BUDGET_REQUIRED")
    require(bounds["max_attempts"] == 1 and 0 < bounds["max_output_bytes"] <= 1024 * 1024, "ONE_ATTEMPT_REQUIRED")
    limits = config["limits"]
    require(bounds["per_attempt_bytes"] >= sum(limits[k] for k in ("source_bytes", "dump_bytes", "metadata_bytes")) and
            bounds["existing_bytes"] + bounds["reserve_bytes"] + bounds["per_attempt_bytes"] <= bounds["capacity_bytes"], "CUMULATIVE_STORAGE_EXCEEDED")
    require(bounds["per_attempt_cost_krw"] >= limits["projected_cost_krw"] and
            bounds["per_attempt_cost_krw"] <= bounds["cost_cap_krw"] <= limits["cost_limit_krw"], "CUMULATIVE_COST_EXCEEDED")
    # No overwrite, repair, replacement identity or implicit continuation of a bundle.
    for name in ("inputs", "scheduled-execution-plan.json", "execution-plan.json", "custody.json", "state"):
        require(not (bundle / name).exists(), "EXISTING_ASSEMBLY_PRESERVED")
    for ref in spec["observed_originals"].values():
        original = load(Path(ref["path"]))
        require(timestamp(original["expires_at"]) >= end.timestamp(), "SCHEDULE_EVIDENCE_EXPIRES")
    prepared = copy.deepcopy(spec)
    prepared["run_id"] = run_id
    assemble(bundle, prepared)
    config = load(bundle / "inputs/config.json")
    require(recurring_subject(config) == policy["config_subject_sha256"] and
            recurring_approval(config, policy, start.timestamp())["run_id"] == run_id, "SCHEDULE_CONFIG_DRIFT")
    policy_path = str(root / "inputs/scheduled-policy.json")
    config_path = str(root / "inputs/config.json")
    argv = [python["resolved_path"], "-B", str(root / "code/guarded_backup.py"), "scheduled",
            "--config", config_path, "--approval", policy_path]
    pins = {**code_pins, config_path: digest(encoded(config)), policy_path: digest(encoded(policy))}
    budget = {**bounds, "config_subject_sha256": recurring_subject(config), "policy_id": policy["policy_id"],
              "policy_sha256": digest(encoded(policy)), "not_before": start.isoformat(), "expires_at": end.isoformat()}
    run_manifest = {"version": 1, "policy_id": policy["policy_id"], "run_id": run_id,
                    "config_sha256": digest(encoded(config)), "policy_sha256": digest(encoded(policy)),
                    "argv_sha256": digest(encoded(argv)), "pins_sha256": digest(encoded(pins))}
    for name, value in (("scheduled-policy.json", policy), ("scheduled-budget.json", budget),
                        ("scheduled-pins.json", pins), ("scheduled-manifest.json", run_manifest)):
        private_write_new(bundle / "inputs" / name, value)
    direct = load(bundle / "execution-plan.json")
    plan = copy.deepcopy(direct)
    prefix = direct["command"][:direct["command"].index("--") + 1]
    ledger = config["state_dir"] + "/scheduled-ledger"
    command = [python["resolved_path"], "-B", str(root / "code/scheduled_dispatch.py"), "--execute",
               "--config", config_path, "--policy", policy_path,
               "--budget", str(root / "inputs/scheduled-budget.json"),
               "--pins", str(root / "inputs/scheduled-pins.json"),
               "--manifest", str(root / "inputs/scheduled-manifest.json"), "--state", ledger]
    plan.update(status="SCHEDULED_ASSEMBLED_NOT_EXECUTED", approval="ONE_ATTEMPT_POLICY_PREPARED",
                command=prefix + command, run_id=run_id, ledger=ledger, dispatcher_argv=argv,
                not_before=start.isoformat(), expires_at=end.isoformat(),
                native_prerequisites=["FRESH_TARGET_AND_LEDGER_ABSENCE", "NO_PRIOR_SCHEDULED_POLICY_OR_CLAIMS_ACROSS_ROOTS", "CURRENT_PYTHON_AND_CODE_PINS",
                                      "CURRENT_OBSERVATIONS_AND_ZERO_COST", "NO_EXISTING_AUTHORITY_RESET"],
                native_prerequisites_verified=False, timer_activation=False)
    plan["cleanup_command"][0] = python["resolved_path"]
    private_write_new(bundle / "scheduled-execution-plan.json", plan)
    return {"status": plan["status"], "run_id": run_id, "ledger": ledger,
            "policy_sha256": digest(encoded(policy)), "budget_sha256": digest(encoded(budget))}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", nargs="?", choices=("package", "probe", "probe-payload", "assemble", "assemble-scheduled"))
    parser.add_argument("--output")
    parser.add_argument("--remote-root")
    parser.add_argument("--input")
    parser.add_argument("--bundle")
    args = parser.parse_args()
    try:
        if args.action == "package":
            result = package(args.output, args.remote_root)
        elif args.action == "probe":
            result = native_probe(load(args.input))
        elif args.action == "probe-payload":
            result = probe_payload(args.bundle, load(args.input), args.output)
        elif args.action == "assemble":
            result = assemble(args.output, load(args.input))
        elif args.action == "assemble-scheduled":
            result = assemble_scheduled(args.output, load(args.input))
        else:
            result = {"status": "DISABLED", "side_effects": False}
        print(json.dumps(result, sort_keys=True))
        return 0
    except Exception:
        print('{"status":"HOLD","code":"NATIVE_TRIAL_PREPARATION_FAILED"}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
