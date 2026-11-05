#!/usr/bin/python3
"""WI019 cold-preserve adapter. No live-service start/resume implementation."""
from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path
import re
import stat
import subprocess
import sys
import threading
import time

from backup_protocol import GateError, digest, encoded, require, timestamp, exact_id
from scheduled_dispatch import load, read, write_new

IDENT = re.compile(r"[A-Za-z_][A-Za-z0-9_]{0,63}\Z")
UNITS = ("mysql.service", "nginx.service", "atstudio.service",
         "atstudio-restricted-web-wi060.service", "atstudio-wi060-supervisor.service",
         "atstudio-restricted-web-wi055.service", "atstudio-wi055-supervisor.service",
         "atstudio-restricted-web-wi042.service", "atstudio-first-start-wi026.service",
         "atstudio-first-start-wi032.service", "atstudio-wi035-supervisor.service",
         "atstudio-wi039-supervisor.service")
MASKED = {"mysql.service", "atstudio-restricted-web-wi060.service",
          "atstudio-restricted-web-wi055.service", "atstudio-restricted-web-wi042.service",
          "atstudio-first-start-wi026.service", "atstudio-first-start-wi032.service"}
FIELDS = "LoadState,ActiveState,SubState,MainPID,ControlPID,UnitFileState"


def verify_parent_lock(config):
    from backup_runtime import checked_path
    # The generic executor owns this flock; hooks must not create a second owner.
    import fcntl
    lock = checked_path(config["lock_file"], private=True)
    fd = os.open(lock, os.O_RDONLY | os.O_NOFOLLOW)
    try:
        try:
            fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            pass
        else:
            fcntl.flock(fd, fcntl.LOCK_UN)
            raise GateError("PARENT_OPERATION_LOCK_REQUIRED")
    finally:
        os.close(fd)


def verify_host_receipts(config, run_dir):
    """Validate independently reviewed, approval-pinned proofs; never issue them.

    Subject excludes only evidence references to avoid a circular content hash;
    the outer exact config approval still binds those paths and content hashes.
    """
    import copy
    from backup_runtime import checked_path, inventory
    host = config["cold_host"]
    verify_parent_lock(config)
    subject = copy.deepcopy(config)
    del subject["cold_host"]["evidence"]
    subject_hash = digest(encoded(subject))
    proofs = {}
    for kind in ("repository_acl", "recovery_access", "source_inventory", "cost"):
        ref = host["evidence"][kind]
        path = checked_path(ref["path"], private=True)
        raw = read(path)
        require(digest(raw) == exact_id(ref["sha256"]), "EVIDENCE_PIN")
        proof = json.loads(raw)
        require(proof["kind"] == kind and proof["version"] == 1 and
                proof["subject_sha256"] == subject_hash and proof["repository_id"] == config["repository_id"] and
                proof["machine_sha256"] == host["machine_sha256"] and proof["boot_sha256"] == host["boot_sha256"], "EVIDENCE_BINDING")
        exact_id(proof["source_evidence_sha256"])
        observed, expiry = timestamp(proof["observed_at"]), timestamp(proof["expires_at"])
        require(observed <= time.time() and time.time() + config["limits"]["seconds"] <= expiry
                and expiry - observed <= 6 * 3600, "EVIDENCE_STALE")
        proofs[kind] = proof
    require(proofs["repository_acl"]["access"] == "PRIVATE_VERIFIED", "PRIVATE_ACL_PROOF")
    require(proofs["recovery_access"]["operation"] == "EXACT_SNAPSHOT_READ_VERIFIED", "RECOVERY_READ_PROOF")
    exact_id(proofs["recovery_access"]["snapshot_id"])
    paths = [Path(config[key]) for key in ("public_root", "private_root", "release_root")] + [Path(p) for p in config["settings"]]
    limits = config["limits"]
    members = inventory(paths, lambda: None, limits["max_entries"], limits["source_bytes"],
                        limits["metadata_bytes"], {str(p): p.stat().st_dev for p in paths})
    require(digest(encoded(members)) == proofs["source_inventory"]["members_sha256"], "SOURCE_INVENTORY_DRIFT")
    cost = proofs["cost"]
    require(type(cost["upper_bound_krw"]) is int and type(cost["approved_limit_krw"]) is int and
            0 <= cost["upper_bound_krw"] <= cost["approved_limit_krw"] == limits["cost_limit_krw"] and
            cost["upper_bound_krw"] == limits["projected_cost_krw"], "COST_AUTHORITY")
    require(type(cost["new_bytes_bound"]) is int and cost["new_bytes_bound"] >=
            sum(limits[k] for k in ("source_bytes", "dump_bytes", "metadata_bytes")), "COST_BYTES_BOUND")
    require(cost["scope"] == "ONE_COLD_CAPTURE" and cost["unknown_components"] == [], "COST_UNKNOWN")


class Commands:
    """Fixed caller argv only; same process group as owning guarded hook process."""
    def run(self, argv, cap=65536, seconds=30):
        process = subprocess.Popen(argv, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                                   stderr=subprocess.DEVNULL, env={"PATH": "/usr/bin:/bin", "LC_ALL": "C"},
                                   close_fds=True)  # no new process session/group
        parts, errors = [], []
        def consume():
            remaining = cap
            try:
                while True:
                    block = process.stdout.read(min(65536, remaining + 1))
                    if not block:
                        break
                    remaining -= len(block)
                    if remaining < 0:
                        errors.append("OUTPUT_LIMIT")
                        break
                    parts.append(block)
            except (OSError, ValueError):
                errors.append("OUTPUT_READ")
        thread = threading.Thread(target=consume, daemon=True)
        thread.start()
        deadline = time.monotonic() + seconds
        try:
            while process.poll() is None or thread.is_alive():
                require(not errors and time.monotonic() < deadline, "COMMAND_BOUND")
                time.sleep(.02)
            require(not errors, "COMMAND_BOUND")
            require(process.returncode == 0, "COMMAND_FAILED")
            return b"".join(parts)
        finally:
            if process.poll() is None:
                process.kill()
            process.wait(timeout=3)
            thread.join(timeout=3)
            process.stdout.close()
            require(not thread.is_alive(), "COMMAND_READER_REMAINS")


def properties(raw):
    rows = raw.decode("ascii").splitlines()
    require(all("=" in row for row in rows), "UNIT_OUTPUT")
    result = dict(row.split("=", 1) for row in rows)
    require(len(rows) == len(result), "UNIT_DUPLICATE")
    return result


class ColdHost:
    def __init__(self, config, run_dir, commands, *, filesystem=Path, verifier=None):
        self.config, self.host, self.run = config, config["cold_host"], Path(run_dir)
        self.command, self.fs, self.verifier = commands, filesystem, verifier
        require(config.get("maintenance_mode") == "cold-preserve", "COLD_MODE_REQUIRED")
        require(type(config["limits"]["dump_bytes"]) is int and 0 < config["limits"]["dump_bytes"] <= 16 * 1024 * 1024,
                "COLD_DUMP_MEMORY_CAP")
        require(type(config["limits"]["seconds"]) is int and 60 <= config["limits"]["seconds"] <= 1800, "COLD_WINDOW_CAP")
        require(re.fullmatch("[a-f0-9]{32}", self.run.name) is not None and
                self.run.parent == Path(config["work_dir"]), "RUN_DIRECTORY")
        self.unit = "atstudio-cold-" + self.run.name + ".service"
        self.runtime = "/run/mysqld"
        self.socket = self.runtime + "/mysqld.sock"
        self.log = Path("/var/log/mysql") / (self.unit + ".log")
        self.claim = self.run / "cold-owned.json"
        self.binding = {"config_sha256": digest(encoded(config)), "unit": self.unit,
                        "machine": self.host["machine_sha256"], "boot": self.host["boot_sha256"]}
        require(IDENT.fullmatch(self.host["schema"]) is not None, "SCHEMA_IDENTIFIER")
        for path in (self.host["datadir"], self.host["mysql_config"], self.host["quarantine_file"]):
            require(isinstance(path, str) and re.fullmatch(r"/[A-Za-z0-9_./-]+", path) is not None and
                    ".." not in Path(path).parts, "FIXED_PATH_REQUIRED")
        owner, logdir = self.host["mysql_identity"], self.host["mysql_log_directory"]
        require(set(owner) == {"uid", "gid"} and all(type(v) is int and v > 0 for v in owner.values()), "MYSQL_ACCOUNT_IDENTITY")
        require(set(logdir) == {"uid", "gid", "mode", "device", "inode"} and
                all(type(v) is int and v >= 0 for v in logdir.values()) and
                logdir["uid"] == owner["uid"] and logdir["mode"] == 0o750, "MYSQL_LOG_DIRECTORY")
        parent = self.host["mysql_log_parent"]
        require(set(parent) == {"uid", "gid", "mode", "device", "inode"} and
                all(type(v) is int and v >= 0 for v in parent.values()) and
                parent["uid"] == 0 and parent["mode"] == 0o775, "MYSQL_LOG_PARENT")
        tables = self.host["tables"]
        require(isinstance(tables, list) and len(tables) == 43 and tables == sorted(set(tables))
                and all(IDENT.fullmatch(t) for t in tables), "EXACT_43_TABLES_REQUIRED")

    def show(self, unit):
        require(unit in UNITS or unit == self.unit, "UNIT_ALLOWLIST")
        return properties(self.command.run(["/usr/bin/systemctl", "show", "--no-pager", "--property=" + FIELDS, "--", unit]))

    def target_identity(self):
        require(digest(self.fs("/etc/machine-id").read_bytes()) == self.host["machine_sha256"] and
                digest(self.fs("/proc/sys/kernel/random/boot_id").read_bytes()) == self.host["boot_sha256"], "HOST_IDENTITY")

    def identity(self):
        self.target_identity()
        pins = self.host["artifact_pins"]
        required = {"/usr/bin/systemctl", "/usr/bin/systemd-run", "/usr/sbin/mysqld",
                    "/usr/bin/mysql", "/usr/bin/mysqldump", "/etc/apparmor.d/usr.sbin.mysqld", self.host["mysql_config"]}
        require(required <= set(pins), "ARTIFACT_PINS_INCOMPLETE")
        for path, expected in pins.items():
            require(digest(read(self.fs(path), 64 * 1024 * 1024)) == expected, "ARTIFACT_CHANGED")

    def baseline(self, *, owned=False):
        self.identity()
        if owned:
            require(self.claim.exists() and load(self.claim) == self.binding, "OWNERSHIP_MISMATCH")
        units = {name: self.show(name) for name in UNITS}
        require(units == self.host["baseline_units"], "BASELINE_UNITS_CHANGED")
        for name, row in units.items():
            if name == "nginx.service":
                require(row.get("ActiveState") == "active" and row.get("SubState") == "running" and
                        row.get("MainPID", "").isdigit() and int(row["MainPID"]) > 0 and
                        row.get("ControlPID") == "0" and row.get("UnitFileState") == "enabled", "NGINX_BASELINE")
            else:
                require(row.get("MainPID") == row.get("ControlPID") == "0" and
                        row.get("ActiveState") in {"inactive", "failed"} and
                        (name not in MASKED or row.get("UnitFileState") == "masked"), "WRITER_UNIT_NOT_MASKED")
        allowed_pid = self.show(self.unit).get("MainPID") if owned else None
        for proc in self.fs("/proc").iterdir():
            if not proc.name.isdigit():
                continue
            try:
                comm = (proc / "comm").read_text().strip()
            except FileNotFoundError:
                continue
            if comm in {"java", "ffmpeg", "mysqld", "mariadbd", "docker", "containerd"}:
                require(owned and comm == "mysqld" and proc.name == allowed_pid, "UNOWNED_WRITER_PROCESS")
        require(owned or not self.fs(self.socket).exists(), "UNMANAGED_DB_SOCKET")
        # Quarantine remains exactly the reviewed configuration; no live reopening.
        require(digest(read(self.fs(self.host["quarantine_file"]))) == self.host["quarantine_sha256"], "QUARANTINE_CHANGED")

    def query(self, sql):
        return self.command.run(["/usr/bin/mysql", "--no-defaults", "--protocol=socket",
                                 "--socket=" + self.socket, "--user=root", "--batch", "--skip-column-names",
                                 "--connect-timeout=3", "--default-character-set=utf8mb4", "--execute=" + sql])

    def fence(self):
        self.baseline(owned=True)
        state = self.show(self.unit)
        require(state.get("ActiveState") == "active" and state.get("SubState") == "running" and
                state.get("MainPID", "").isdigit() and int(state["MainPID"]) > 0 and
                state.get("ControlPID") == "0", "OWNED_MYSQL_NOT_RUNNING")
        original = self.fs(self.host["datadir"]).stat()
        mounted = self.fs("/proc/" + state["MainPID"] + "/root" + self.host["datadir"]).stat()
        require((original.st_dev, original.st_ino) == (mounted.st_dev, mounted.st_ino), "DB_MOUNT_IDENTITY")
        flags = self.query("SELECT @@skip_networking,@@event_scheduler,@@local_infile,@@read_only,@@super_read_only;")
        require(flags.strip() == b"1\tDISABLED\t0\t1\t1", "READ_ONLY_FENCE")

    def start(self):
        self.baseline()
        require(self.verifier is not None, "EXTERNAL_RECEIPTS_REQUIRED")
        self.verifier(self.config, self.run)
        require(self.show(self.unit).get("LoadState") == "not-found" and not self.fs(self.runtime).exists()
                and not self.fs(self.runtime).is_symlink(), "OWNED_UNIT_EXISTS")
        write_new(self.claim, self.binding)  # owned before potentially partial creation
        self.prepare_log()
        props = ["Type=exec", "User=mysql", "Group=mysql", "UMask=0077", "PrivateNetwork=yes",
                 "RestrictAddressFamilies=AF_UNIX", "IPAddressDeny=any", "NoNewPrivileges=yes",
                 "PrivateTmp=yes", "ProtectHome=yes", "ProtectSystem=strict", "KillMode=control-group",
                 "RuntimeDirectory=" + Path(self.runtime).name, "RuntimeDirectoryMode=0750",
                 "TimeoutStopSec=20", "SendSIGKILL=yes", "RuntimeMaxSec=" + str(self.config["limits"]["seconds"] + 120), "MemoryMax=768M",
                 "MemorySwapMax=0", "CPUQuota=100%", "TasksMax=128", "LimitCORE=0",
                 "StandardOutput=null", "StandardError=null", "ReadWritePaths=" + self.host["datadir"] + " " + self.runtime + " " + str(self.log)]
        args = ["/usr/bin/systemd-run", "--quiet", "--collect", "--unit=" + self.unit,
                *["--property=" + p for p in props], "--", "/usr/sbin/mysqld",
                "--defaults-file=" + self.host["mysql_config"], "--datadir=" + self.host["datadir"],
                "--socket=" + self.socket, "--pid-file=" + self.runtime + "/mysqld.pid",
                "--log-error=" + str(self.log), "--skip-networking", "--mysqlx=OFF",
                "--event-scheduler=DISABLED", "--skip-slave-start", "--skip-log-bin",
                "--persisted-globals-load=OFF", "--local-infile=OFF", "--secure-file-priv=NULL",
                "--innodb-buffer-pool-size=134217728", "--max-connections=16", "--performance-schema=OFF",
                "--skip-name-resolve", "--default-time-zone=+00:00", "--upgrade=NONE",
                "--read-only=ON", "--super-read-only=ON"]
        self.command.run(args, seconds=45)
        end = time.monotonic() + 30
        while not self.fs(self.socket).exists():
            require(time.monotonic() < end, "MYSQL_READY_TIMEOUT")
            time.sleep(.1)
        self.fence()

    def log_layout(self):
        from backup_runtime import checked_path
        directory, parent = self.log.parent, self.log.parent.parent
        checked_path(parent.parent, trusted=True)  # /var and / remain strict
        parent_info = parent.lstat()
        parent_actual = {"uid": parent_info.st_uid, "gid": parent_info.st_gid, "mode": stat.S_IMODE(parent_info.st_mode),
                         "device": parent_info.st_dev, "inode": parent_info.st_ino}
        require(stat.S_ISDIR(parent_info.st_mode) and not stat.S_ISLNK(parent_info.st_mode) and
                parent_actual == self.host["mysql_log_parent"], "MYSQL_LOG_PARENT")
        before = directory.lstat()
        expected = self.host["mysql_log_directory"]
        actual = {"uid": before.st_uid, "gid": before.st_gid, "mode": stat.S_IMODE(before.st_mode),
                  "device": before.st_dev, "inode": before.st_ino}
        require(stat.S_ISDIR(before.st_mode) and not stat.S_ISLNK(before.st_mode) and
                actual == expected and actual["mode"] == 0o750, "MYSQL_LOG_DIRECTORY")
        return before

    def prepare_log(self):
        """Retained-success AppArmor layout; create only a fresh owned log file."""
        import pwd
        directory = self.log.parent
        before = self.log_layout()
        owner = self.host["mysql_identity"]
        account = pwd.getpwnam("mysql")
        require(owner == {"uid": account.pw_uid, "gid": account.pw_gid} and before.st_uid == owner["uid"], "MYSQL_ACCOUNT_IDENTITY")
        parent = os.open(directory, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
        try:
            opened = os.fstat(parent)
            require((opened.st_dev, opened.st_ino) == (before.st_dev, before.st_ino), "MYSQL_LOG_DIRECTORY_CHANGED")
            fd = os.open(self.log.name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600, dir_fd=parent)
            try:
                os.fchmod(fd, 0o600)
                os.fchown(fd, owner["uid"], owner["gid"])
                os.fsync(fd)
            finally:
                os.close(fd)
            os.fsync(parent)
        finally:
            os.close(parent)
        after = self.log_layout()
        require((after.st_dev, after.st_ino) == (before.st_dev, before.st_ino), "MYSQL_LOG_DIRECTORY_CHANGED")

    def stop(self):
        if self.claim.exists():
            require(load(self.claim) == self.binding, "OWNERSHIP_MISMATCH")
            self.target_identity()
            row = self.show(self.unit)
            if row.get("MainPID") != "0" or row.get("ControlPID") != "0":
                self.command.run(["/usr/bin/systemctl", "stop", "--", self.unit], seconds=35)
            row = self.show(self.unit)
            require(row.get("MainPID") == row.get("ControlPID") == "0", "CLEANUP_PID")
        self.baseline()
        require(not self.fs(self.runtime).exists() and not self.fs(self.runtime).is_symlink(), "OWNED_RUNTIME_REMAINS")

    def inventory(self):
        schema, tables = self.host["schema"], self.host["tables"]
        raw = self.query("SELECT TABLE_NAME,TABLE_TYPE,COALESCE(ENGINE,'') FROM information_schema.TABLES WHERE TABLE_SCHEMA='" + schema + "' ORDER BY TABLE_NAME;")
        expected = [t + "\tBASE TABLE\tInnoDB" for t in tables]
        require(raw.decode("ascii").splitlines() == expected, "TABLE_INVENTORY_CHANGED")
        sql = " UNION ALL ".join("SELECT '" + t + "',COUNT(*) FROM `" + schema + "`.`" + t + "`" for t in tables) + ";"
        rows = self.query(sql).decode("ascii").splitlines()
        require(len(rows) == 43, "TABLE_COUNT_MISSING")
        counts = {}
        for table, row in zip(tables, rows):
            parts = row.split("\t")
            require(len(parts) == 2 and parts[0] == table and re.fullmatch(r"[0-9]{1,20}", parts[1]), "ROW_COUNT_INVALID")
            counts[table] = int(parts[1])
        return counts

    def dump(self):
        self.fence()
        counts = self.inventory()
        args = ["/usr/bin/mysqldump", "--no-defaults", "--protocol=socket", "--socket=" + self.socket,
                "--user=root", "--single-transaction", "--quick", "--hex-blob", "--routines", "--events",
                "--triggers", "--set-gtid-purged=OFF", "--no-tablespaces", "--skip-comments",
                "--skip-dump-date", "--skip-extended-insert", "--order-by-primary", "--skip-add-drop-table",
                "--tz-utc", "--default-character-set=utf8mb4", "--databases", self.host["schema"]]
        data = self.command.run(args, cap=self.config["limits"]["dump_bytes"], seconds=120)
        require(data and counts == self.inventory(), "SQL_DRIFT")
        creates = re.findall(rb"^CREATE TABLE `([A-Za-z_][A-Za-z0-9_]*)`", data, re.M)
        require([x.decode() for x in creates] == self.host["tables"], "DUMP_TABLES_INCOMPLETE")
        for table, count in counts.items():
            require(len(re.findall(rb"^INSERT INTO `" + table.encode() + rb"` VALUES ", data, re.M)) == count, "DUMP_ROWS_INCOMPLETE")
        self.fence()
        return data

    def hook(self, name):
        if name == "cleanup":
            self.stop()
            return {"configured": True, "cold_baseline_preserved": True}
        if name == "preflight":
            self.baseline()
            require(self.verifier is not None, "EXTERNAL_RECEIPTS_REQUIRED")
            self.verifier(self.config, self.run)
            return dict(configured=True, target_matched=True, private_repository_acl=True,
                        recovery_access_verified=True, source_inventory_reviewed=True,
                        byte_cost_budget_approved=True, no_overlap=True)
        if name in {"close-admission", "quiesce"}:
            self.baseline()
            return dict(configured=True, admission_closed=True, drained_or_aborted=True,
                        jvm_stopped=True, workers_stopped=True, relaunch_disabled=True)
        if name == "fence":
            self.start()
        elif name in {"assert-quiescent", "logical-dump", "validate-dump"}:
            self.fence()
        elif name == "resume":
            self.stop()
            return dict(configured=True, matching_tuple=True, cold_baseline_preserved=True)
        else:
            raise GateError("HOOK_NOT_ALLOWED")
        if name == "logical-dump":
            return self.dump()
        if name == "validate-dump":
            original = read(self.run / "database.partial", self.config["limits"]["dump_bytes"])
            require(original == self.dump(), "FULL_DUMP_DRIFT")
            return dict(configured=True, schema_complete=True, all_application_tables=True,
                        rows_complete=True, dump_sha256=digest(original))
        return dict(configured=True, admission_closed=True, jvm_stopped=True, workers_stopped=True,
                    relaunch_disabled=True, db_writers_fenced=True, file_writers_fenced=True,
                    schema_rotation_fenced=True)


def main():
    if len(sys.argv) == 1:
        print('{"configured":false,"status":"DISABLED"}')
        return 0
    adapter = None
    try:
        require(sys.platform == "linux" and os.geteuid() == 0 and len(sys.argv) == 4, "LINUX_ROOT_REQUIRED")
        from backup_runtime import checked_path
        name, config_path, run_dir = sys.argv[1:]
        checked_path(config_path, private=True)
        checked_path(run_dir, private=True)
        config = load(config_path)
        require(not config.get("recovery_gate") and not
                (Path(config["state_dir"]) / "live-service-ledger" / "operation.guard").exists(),
                "LEGACY_GATE_INCOMPATIBLE")
        adapter = ColdHost(config, run_dir, Commands(), verifier=verify_host_receipts)
        output = adapter.hook(name)
        if isinstance(output, bytes):
            sys.stdout.buffer.write(output)
        else:
            print(json.dumps(output))
        return 0
    except Exception as error:
        code = str(error) if isinstance(error, GateError) else "HOST_ADAPTER_HOLD"
        original_code = code
        cleanup = "NOT_ATTEMPTED"
        if adapter is not None:
            try:
                adapter.stop()
                cleanup = "VERIFIED_COLD"
            except Exception:
                code = "COLD_CLEANUP_UNCERTAIN"
                cleanup = "UNCERTAIN"
            try:
                write_new(adapter.run / "cold-hook-diagnostic.json", {"code": original_code, "cleanup": cleanup})
            except Exception:
                pass  # Preserve any earlier diagnostic; outcome remains failure.
        print(json.dumps({"configured": False, "code": code}), file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
