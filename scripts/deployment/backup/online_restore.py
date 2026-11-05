"""Exact online-snapshot recovery adapter; import/default CLI never executes it.

Native execution is separately authorized. Capture receipts are never rewritten.
Only file retrieval and an isolated MySQL round trip are supported, not app start.
"""
from __future__ import annotations

from collections import Counter
import json
import ipaddress
import os
from pathlib import Path, PurePosixPath
import re
import signal
import stat
import sys
import time

from backup_protocol import (GateError, digest, encoded, exact_id, linux_path,
                             overlap, require, snapshot_nodes, timestamp, verify_nodes)
from backup_runtime import (checked_path, create_bytes, hash_file, read_json,
                            restic_command, LinuxRunner, LinuxMonitor, footprint,
                            operations_lock, restic_environment)

MAX_SQL = 16 * 1024**2
IDENT = re.compile(r"[A-Za-z_][A-Za-z0-9_]{0,63}\Z")
RUN = re.compile(r"[a-f0-9]{32}\Z")
RESTORE_PARENT = "/var/lib/atstudio-online-restore"


def bounded_read(path: Path, cap: int) -> bytes:
    for item in [*path.parents, path]:
        require(not item.is_symlink(), "RESTORE_INPUT_SYMLINK")
    before = path.stat()
    require(stat.S_ISREG(before.st_mode) and before.st_size <= cap and before.st_nlink == 1,
            "RESTORE_INPUT_BOUND")
    with path.open("rb") as stream:
        raw = stream.read(cap + 1)
    after = path.stat()
    require(len(raw) <= cap and (before.st_dev, before.st_ino, before.st_size, before.st_mtime_ns) ==
            (after.st_dev, after.st_ino, after.st_size, after.st_mtime_ns), "RESTORE_INPUT_CHANGED")
    return raw


def tokens(raw: bytes) -> list[bytes]:
    """Reuse WI069's bounded lexical strategy; strings/comments remain opaque.

    Ordinary dump comments vary by time/host and are omitted. Executable version
    comments remain exact opaque tokens. Unsupported DELIMITER blocks fail closed.
    """
    require(0 < len(raw) <= MAX_SQL, "RESTORE_SQL_BOUND")
    out, i = [], 0
    while i < len(raw):
        start, char = i, raw[i]
        if char in b" \t\r\n":
            i += 1
            continue
        if raw[i:i + 2] == b"/*":
            end = raw.find(b"*/", i + 2)
            require(end >= 0 and b"/*" not in raw[i + 2:end], "RESTORE_SQL_LEXER")
            i = end + 2
            if raw[start:start + 3] == b"/*!":
                out.append(raw[start:i])
            continue
        if char == 35 or (raw[i:i + 2] == b"--" and raw[i + 2:i + 3] in (b" ", b"\t", b"\r", b"\n")):
            end = raw.find(b"\n", i)
            i = len(raw) if end < 0 else end + 1
            continue
        if char in (39, 34, 96):
            i += 1
            while i < len(raw):
                if raw[i] == 92:
                    i += 2
                    continue
                if raw[i] == char:
                    i += 1
                    if i < len(raw) and raw[i] == char:
                        i += 1
                        continue
                    break
                i += 1
            else:
                raise GateError("RESTORE_SQL_LEXER")
            require(i <= len(raw), "RESTORE_SQL_LEXER")
        elif char in b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_":
            i += 1
            while i < len(raw) and raw[i] in b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_":
                i += 1
        else:
            i += 1
        out.append(raw[start:i])
        require(len(out) <= 1000000, "RESTORE_SQL_TOKENS")
    return out


def sql_model(raw: bytes, schema: str, tables: list[str]) -> dict:
    """Strict dump proof with dynamic rows and one known equivalent DDL spelling.

    No arbitrary SQL normalization. utf8mb4_unicode_ci itself fixes utf8mb4,
    so the redundant explicit CHARACTER SET is removed only in CREATE TABLE
    tokens, never in strings or INSERT values. Unsupported programmable SQL fails.
    """
    require(IDENT.fullmatch(schema) is not None and tables == sorted(set(tables))
            and bool(tables) and all(IDENT.fullmatch(x) for x in tables), "RESTORE_SCHEMA")
    statements, current = [], []
    for item in tokens(raw):
        require(item.upper() != b"DELIMITER", "RESTORE_PROGRAMMABLE_SQL")
        if item == b";":
            if current:
                statements.append(current)
            current = []
        else:
            current.append(item)
    require(not current, "RESTORE_SQL_TERMINATOR")
    creates, structure, rows = [], [], {name: Counter() for name in tables}
    table_tokens = {("`" + t + "`").encode(): t for t in tables}
    for statement in statements:
        head = [x.upper() for x in statement[:3]]
        if head[:2] == [b"CREATE", b"TABLE"]:
            require(len(statement) > 4 and statement[2] in table_tokens and statement[3] == b"(", "RESTORE_DDL")
            creates.append(table_tokens[statement[2]])
            normalized, i = [], 0
            while i < len(statement):
                if statement[i:i + 5] == [b"CHARACTER", b"SET", b"utf8mb4", b"COLLATE", b"utf8mb4_unicode_ci"]:
                    i += 3
                normalized.append(statement[i])
                i += 1
            structure.append(normalized)
        elif head[:2] == [b"INSERT", b"INTO"]:
            require(len(statement) > 5 and statement[2] in table_tokens and
                    statement[3] == b"VALUES" and statement[4] == b"(" and statement[-1] == b")",
                    "RESTORE_INSERT")
            # --skip-extended-insert: a single row per statement, with nesting tracked.
            depth = 0
            for item in statement[4:]:
                depth += (item == b"(") - (item == b")")
                require(depth >= 0 and (depth > 0 or item == b")"), "RESTORE_MULTIROW")
            require(depth == 0, "RESTORE_INSERT")
            rows[table_tokens[statement[2]]][encoded([x.hex() for x in statement[4:]])] += 1
        else:
            allowed = (head[0] == b"SET" or head[:2] == [b"LOCK", b"TABLES"] or
                       head[:2] == [b"UNLOCK", b"TABLES"] or
                       (head[0] == b"USE" and statement[1:] == [("`" + schema + "`").encode()]) or
                       (head[:2] == [b"CREATE", b"DATABASE"] and
                        [x for x in statement[2:] if not x.startswith(b"/*!")] == [("`" + schema + "`").encode()]) or
                       (len(statement) == 1 and statement[0].startswith(b"/*!")))
            require(allowed, "RESTORE_UNSUPPORTED_SQL")
            # Version comments can contain active SQL; reject all except dump SET/ALTER wrappers.
            if len(statement) == 1 and statement[0].startswith(b"/*!"):
                require(re.fullmatch(rb"/\*!\d{5} (?:SET [^;\r\n]+|ALTER TABLE `[A-Za-z_][A-Za-z0-9_]*` (?:DISABLE|ENABLE) KEYS) ?\*/", statement[0]),
                        "RESTORE_PROGRAMMABLE_SQL")
            structure.append(statement)
    require(creates == tables, "RESTORE_TABLES")
    return {"structure": structure, "rows": rows}


def compare_sql(original: bytes, restored: bytes, schema: str, tables: list[str]) -> dict:
    before, after = sql_model(original, schema, tables), sql_model(restored, schema, tables)
    require(before == after, "RESTORE_SQL_DIFFERENT")
    return {"tables": len(tables), "rows": {t: sum(before["rows"][t].values()) for t in tables},
            "original_sha256": digest(original), "roundtrip_sha256": digest(restored),
            "comparison": "EXACT_TOKENS_AND_ROW_MULTISET_WITH_REDUNDANT_UTF8MB4_CHARSET"}


def bind_capture(directory: Path, config: dict) -> tuple[dict, dict]:
    receipt_raw = bounded_read(directory / "verified-receipt.json", 65536)
    receipt = json.loads(receipt_raw)
    prep = json.loads(bounded_read(directory / "restore-preparation.json", 65536))
    manifest_raw = bounded_read(directory / "manifest.json", config["limits"]["metadata_bytes"])
    require(len(manifest_raw) <= config["limits"]["metadata_bytes"], "RESTORE_MANIFEST_BOUND")
    manifest = json.loads(manifest_raw)
    state = json.loads(bounded_read(Path(config["online_state_path"]), 65536))
    require(state.get("stage") == "COMPLETE" and state.get("last_good") == receipt and
            state.get("run_id") == receipt.get("run_id") and state.get("capture_id") == receipt.get("capture_id") and
            state.get("receipt_sha256") == digest(receipt_raw),
            "RESTORE_CAPTURE_NOT_COMPLETE")
    require(receipt.get("protocol") == "ats-online-v1" and receipt.get("status") == "VERIFIED" and
            receipt.get("restore_verified") is False and
            all(receipt.get(x) is True for x in ("helpers_ended", "inventory_verified", "content_verified")),
            "RESTORE_CAPTURE_RECEIPT")
    require(receipt.get("run_id") == directory.name and RUN.fullmatch(directory.name) and
            receipt.get("repository_id") == config["repository_id"], "RESTORE_CAPTURE_BINDING")
    exact_id(receipt.get("snapshot_id"))
    for key in ("capture_id", "dump_sha256", "content_manifest_sha256", "snapshot_id", "repository_id"):
        require(prep.get(key) == receipt.get(key), "RESTORE_PREPARATION_BINDING")
    require(prep.get("execution_authorized") is False and prep.get("restore_verified") is False and
            manifest.get("version") == 1 and manifest.get("protocol") == "ats-online-v1" and
            manifest.get("capture_id") == receipt["capture_id"] and
            manifest.get("dump_sha256") == receipt["dump_sha256"] and
            manifest.get("manifest_sha256") == receipt["manifest_sha256"] and
            manifest.get("config_sha256") == config["capture_config_sha256"] and
            digest(manifest_raw) == receipt["content_manifest_sha256"], "RESTORE_MANIFEST_BINDING")
    require(isinstance(manifest.get("members"), dict) and
            all(row.get("type") in {"file", "dir"} for row in manifest["members"].values()), "RESTORE_MEMBER_TYPE")
    members = {p: row for p, row in manifest["members"].items() if row["type"] == "file"}
    dump_path = str(directory / "database.sql")
    require(dump_path in members and members[dump_path]["sha256"] == receipt["dump_sha256"], "RESTORE_DUMP_BINDING")
    for path, row in members.items():
        linux_path(path)
        exact_id(row["sha256"])
        require(type(row["size"]) is int and row["size"] >= 0, "RESTORE_MEMBER_SIZE")
    return receipt, members


def validate(config: dict, approval: dict, now: float) -> None:
    require(config.get("version") == 1 and config.get("operation") == "online-isolated-restore", "RESTORE_CONFIG")
    require(RUN.fullmatch(config.get("restore_id", "")) is not None, "RESTORE_ID")
    target = linux_path(config["target_root"])
    require(config["target_parent"] == RESTORE_PARENT and
            target == config["target_parent"] + "/" + config["restore_id"] and
            re.fullmatch(r"/[A-Za-z0-9_./-]+", target), "RESTORE_TARGET")
    linux_path(config["target_parent"])
    require(config["source_roots"] and all(not overlap(target, linux_path(p)) for p in config["source_roots"]),
            "RESTORE_TARGET_OVERLAP")
    require(isinstance(config["schema"], str) and IDENT.fullmatch(config["schema"]) and
            isinstance(config["tables"], list) and bool(config["tables"]) and
            config["tables"] == sorted(set(config["tables"])) and
            all(isinstance(t, str) and IDENT.fullmatch(t) for t in config["tables"]), "RESTORE_SCHEMA")
    require(type(config["approved_cost_krw"]) is int and config["approved_cost_krw"] >= 0, "RESTORE_APPROVED_COST")
    helper_addresses(config)
    require(approval.get("operation") == config["operation"] and
            approval.get("config_sha256") == digest(encoded(config)), "RESTORE_APPROVAL")
    begin, end = timestamp(approval["not_before"]), timestamp(approval["not_after"])
    require(begin <= now < end and end - begin <= 1800, "RESTORE_WINDOW")
    for key in ("capture_receipt_sha256", "capture_config_sha256", "restore_module_sha256",
                "repository_id", "machine_sha256", "boot_sha256"):
        exact_id(config[key])
    limits = config["limits"]
    for key, cap in (("retrieval_bytes", 288 * 1024**2), ("dump_bytes", MAX_SQL),
                     ("metadata_bytes", 16 * 1024**2), ("seconds", 1800)):
        require(type(limits[key]) is int and 0 < limits[key] <= cap, "RESTORE_LIMIT")
    proof = config["proof"]
    exact_id(proof["sha256"])
    linux_path(proof["path"])


def helper_addresses(config: dict) -> list[str]:
    addresses = config.get("helper_allow_ips")
    require(isinstance(addresses, list) and 0 < len(addresses) <= 16 and
            len(addresses) == len(set(addresses)), "RESTORE_EGRESS_SCOPE")
    normalized = []
    for value in addresses:
        try:
            network = ipaddress.ip_network(value, strict=True)
        except (TypeError, ValueError):
            raise GateError("RESTORE_EGRESS_SCOPE") from None
        require(network.prefixlen == network.max_prefixlen and str(network) == value,
                "RESTORE_EGRESS_SCOPE")
        normalized.append(str(network))
    return sorted(normalized)


def verify_proof(config: dict, now: float) -> None:
    ref = config["proof"]
    raw = bounded_read(checked_path(ref["path"], private=True), 65536)
    require(len(raw) <= 65536 and digest(raw) == ref["sha256"], "RESTORE_PROOF_HASH")
    proof = json.loads(raw)
    require(proof.get("operation") == "ONE_ISOLATED_ONLINE_RESTORE" and
            all(proof.get(k) == config[k] for k in ("restore_id", "target_root", "repository_id",
                "capture_receipt_sha256", "machine_sha256", "boot_sha256")), "RESTORE_PROOF_BINDING")
    require(timestamp(proof["observed_at"]) <= now and now + config["limits"]["seconds"] <= timestamp(proof["expires_at"])
            and timestamp(proof["expires_at"]) - timestamp(proof["observed_at"]) <= 21600, "RESTORE_PROOF_STALE")
    for field in ("source_app_db_stopped", "sole_restore_owner", "no_scheduled_relaunch", "private_repository"):
        require(proof.get(field) is True, "RESTORE_ISOLATION_PROOF")
    exact_id(proof["source_evidence_sha256"])
    require(proof.get("unknown_cost_components") == [] and type(proof.get("cost_upper_krw")) is int and
            0 <= proof["cost_upper_krw"] <= config["approved_cost_krw"] and
            proof.get("additional_retrieval_bytes", -1) >= config["limits"]["retrieval_bytes"] and
            proof.get("includes_prior_capture_and_retrieval") is True, "RESTORE_COST_PROOF")


class RestoreSession:
    """Reusable stage connection; backend performs bounded native operations."""
    def __init__(self, config: dict, approval: dict, capture_dir: Path, backend):
        self.config, self.approval, self.capture_dir, self.backend = config, approval, capture_dir, backend

    def run(self) -> dict:
        c, backend = self.config, self.backend
        validate(c, self.approval, backend.now())
        require(str(self.capture_dir) == c["capture_dir"], "RESTORE_CAPTURE_DIRECTORY")
        backend.preflight(c, self.approval)
        receipt, members = bind_capture(self.capture_dir, c)
        require(digest(bounded_read(self.capture_dir / "verified-receipt.json", 65536)) == c["capture_receipt_sha256"],
                "RESTORE_RECEIPT_PIN")
        target = Path(c["target_root"])
        require(not target.exists() and not target.is_symlink(), "RESTORE_TARGET_EXISTS")
        require(all(not overlap(str(target), p) for p in members), "RESTORE_MEMBER_OVERLAP")
        total = sum(row["size"] for row in members.values()) + (self.capture_dir / "manifest.json").stat().st_size
        require(total <= c["limits"]["retrieval_bytes"], "RESTORE_RETRIEVAL_BUDGET")
        target.mkdir(mode=0o700, exist_ok=False)
        create_bytes(target / "owner.json", encoded({"restore_id": c["restore_id"], "receipt": c["capture_receipt_sha256"]}))
        result = {"status": "HOLD", "restore_verified": False, "restore_id": c["restore_id"],
                  "capture_id": receipt["capture_id"], "snapshot_id": receipt["snapshot_id"],
                  "repository_id": receipt["repository_id"], "capture_receipt_sha256": c["capture_receipt_sha256"]}
        result["environment"] = backend.environment
        try:
            backend.verify_snapshot(receipt, members, self.capture_dir)
            files = target / "files"
            files.mkdir(mode=0o700)
            recovered_map = {}
            for number, (source, row) in enumerate(sorted(members.items())):
                # Opaque filenames retain no executable source layout or permissions.
                output = files / (str(number) + ".blob")
                backend.fetch(receipt["snapshot_id"], source, output, row["size"])
                require(hash_file(output, backend.tick, row["size"]) == (row["size"], row["sha256"]), "RESTORE_FILE_HASH")
                recovered_map[source] = {"relative_path": "files/" + output.name, **row}
                if source == str(self.capture_dir / "database.sql"):
                    dump = output
            create_bytes(target / "restored-map.json", encoded(recovered_map))
            original = bounded_read(dump, c["limits"]["dump_bytes"])
            sql_model(original, c["schema"], c["tables"])
            roundtrip = backend.roundtrip(dump, target)
            result["sql"] = compare_sql(original, roundtrip, c["schema"], c["tables"])
            result["files_verified"] = len(members)
            result["retrieved_bytes"] = total
            result["status"] = "VERIFIED_PENDING_CLEANUP"
        except BaseException as error:
            result["failure"] = str(error) if isinstance(error, GateError) else "RESTORE_FAILURE"
        finally:
            try:
                backend.stop_owned()
                require(backend.ended(), "RESTORE_HELPERS_ACTIVE")
                result["cleanup"] = True
            except BaseException:
                result.update(status="HOLD", cleanup=False, failure="RESTORE_CLEANUP_UNCERTAIN")
        if result["status"] == "VERIFIED_PENDING_CLEANUP":
            native = backend.environment == "NATIVE_LINUX"
            result.update(status="ISOLATED_RESTORE_VERIFIED" if native else "SIMULATED_RESTORE_VERIFIED",
                          restore_verified=native)
        create_bytes(target / "restore-result.json", encoded(result))
        return result


class NativeRestoreBackend:
    """Real command adapter, dormant until separately approved native invocation.

    Reuses bounded LinuxRunner and WI069's socket-only MySQL/systemd contract.
    No source DB is queried; only fresh mounted datadir is writable to mysqld.
    """
    environment = "NATIVE_LINUX"

    def __init__(self, config: dict, restic_config: dict, restic_runner: LinuxRunner,
                 monitor):
        self.config, self.restic_config, self.repository = config, restic_config, restic_runner
        self.monitor = monitor
        self.client = LinuxRunner(monitor, {"PATH": "/usr/bin:/bin", "LANG": "C.UTF-8",
                                  "HOME": config["target_root"], "MYSQL_TEST_LOGIN_FILE": "/dev/null"})
        self.units = set()
        self.description = "ATS-online-restore-" + digest(encoded(config))
        self.deadline = None
        self.last_capacity = 0.0

    def now(self):
        return time.time()

    def tick(self):
        require(self.deadline is not None and self.now() < self.deadline, "RESTORE_DEADLINE")
        self.monitor.tick(force=True)
        if self.now() - self.last_capacity >= 2:
            self.last_capacity = self.now()
            root = Path(self.config["target_root"])
            space = os.statvfs(root if root.exists() else root.parent)
            require(space.f_bavail * space.f_frsize >= max(space.f_blocks * space.f_frsize // 5, 10 * 1024**3),
                    "RESTORE_DISK_FLOOR")
            if root.exists():
                footprint(root, 4 * 1024**3)
            for unit in self.units:
                log = self.log(unit)
                if log.exists():
                    require(log.stat().st_size <= 1024**2, "RESTORE_LOG_BOUND")

    def command(self, argv, cap=65536, output=None):
        self.tick()
        result = self.client.run(argv, cap, output)
        require(result.code == 0, "RESTORE_COMMAND_EXIT")
        return result.data

    def show(self, unit):
        raw = self.command(["/usr/bin/systemctl", "show", "--no-pager", "--property=LoadState,ActiveState,MainPID,ControlPID,Description,User,PrivateNetwork", "--", unit])
        return dict(line.split("=", 1) for line in raw.decode().splitlines())

    def preflight(self, config, approval):
        require(sys.platform == "linux" and os.geteuid() == 0, "RESTORE_NATIVE_ONLY")
        self.deadline = min(timestamp(approval["not_after"]), self.now() + config["limits"]["seconds"])
        available = next(int(line.split()[1]) * 1024 for line in Path("/proc/meminfo").read_text().splitlines()
                         if line.startswith("MemAvailable:"))
        require(available >= (768 + 1024 + 1024) * 1024**2, "RESTORE_COMBINED_MEMORY")
        require(digest(Path("/etc/machine-id").read_bytes()) == config["machine_sha256"] and
                digest(Path("/proc/sys/kernel/random/boot_id").read_bytes()) == config["boot_sha256"], "RESTORE_HOST")
        require(self.restic_config["repository_id"] == config["repository_id"], "RESTORE_REPOSITORY_CONFIG")
        helper = "ats-online-restore-helper-" + config["restore_id"] + ".service"
        require(self.monitor.group.name == helper, "RESTORE_HELPER_GROUP")
        policy_raw = self.command(["/usr/bin/systemctl", "show", "--no-pager",
                                   "--property=IPAddressDeny,IPAddressAllow", "--", helper])
        policy = dict(line.split("=", 1) for line in policy_raw.decode().splitlines())
        require(set(policy.get("IPAddressDeny", "").split()) == {"0.0.0.0/0", "::/0"} and
                sorted(policy.get("IPAddressAllow", "").split()) == helper_addresses(config), "RESTORE_HELPER_EGRESS")
        from online_capture import validate_online
        validate_online(self.restic_config)
        require(digest(encoded(self.restic_config)) == config["capture_config_sha256"], "RESTORE_CAPTURE_CONFIG_PIN")
        require(hash_file(Path(__file__), self.tick, 1024**2)[1] == config["restore_module_sha256"], "RESTORE_MODULE_PIN")
        for name, pin in self.restic_config["online"]["module_sha256"].items():
            path = checked_path(Path(__file__).parent / name, trusted=True)
            require(hash_file(path, self.tick, 1024**2)[1] == pin, "RESTORE_CAPTURE_MODULE_PIN")
        restic = checked_path(self.restic_config["restic_binary"], trusted=True)
        require(hash_file(restic, self.tick, 64 * 1024**2)[1] == self.restic_config["restic_sha256"], "RESTORE_RESTIC_PIN")
        for key in ("password_file", "aws_credentials_file"):
            checked_path(self.restic_config[key], private=True)
        verify_proof(config, self.now())
        checked_path(config["target_parent"], private=True)
        for path in (config["online_state_path"], config["capture_dir"]):
            checked_path(path, private=True)
        require(config["capture_dir"] in config["source_roots"], "RESTORE_CAPTURE_SCOPE")
        require(not Path("/run/mysqld").exists(), "RESTORE_SOCKET_IN_USE")
        for proc in Path("/proc").iterdir():
            if proc.name.isdigit():
                try:
                    name = (proc / "comm").read_text().strip()
                except FileNotFoundError:
                    continue
                require(name not in {"java", "mysqld", "mariadbd", "ffmpeg"}, "RESTORE_SOURCE_PROCESS_ACTIVE")
        bins = config["binary_pins"]
        require(set(bins) == {"/usr/bin/systemd-run", "/usr/bin/systemctl", "/usr/bin/mysql",
                             "/usr/bin/mysqldump", "/usr/sbin/mysqld"}, "RESTORE_BINARY_SCOPE")
        for path, pin in bins.items():
            checked_path(path, trusted=True)
            require(hash_file(Path(path), self.tick, 64 * 1024**2)[1] == exact_id(pin), "RESTORE_BINARY_PIN")
        require(type(config["mysql_uid"]) is int and config["mysql_uid"] > 0 and
                type(config["mysql_gid"]) is int and config["mysql_gid"] > 0, "RESTORE_MYSQL_OWNER")
        import pwd
        require((pwd.getpwnam("mysql").pw_uid, pwd.getpwnam("mysql").pw_gid) ==
                (config["mysql_uid"], config["mysql_gid"]), "RESTORE_MYSQL_OWNER")
        # Existing log parent is observed, never chowned. One exclusive new log/unit only.
        log_parent = Path("/var/log/mysql")
        require(log_parent.is_dir() and not log_parent.is_symlink() and
                log_parent.stat().st_uid == config["mysql_uid"] and
                not log_parent.stat().st_mode & 0o002, "RESTORE_LOG_PARENT")
        for suffix in ("init", "db"):
            unit = self.unit(suffix)
            require(self.show(unit).get("LoadState") == "not-found", "RESTORE_UNIT_EXISTS")
            require(not self.log(unit).exists() and not self.log(unit).is_symlink(), "RESTORE_LOG_EXISTS")

    def unit(self, suffix):
        return "ats-online-restore-" + self.config["restore_id"] + "-" + suffix + ".service"

    def log(self, unit):
        return Path("/var/log/mysql") / (unit + ".log")

    def verify_snapshot(self, receipt, members, capture_dir):
        c = self.restic_config
        reply = self.repository.run(restic_command(c, ["cat", "config"]), 65536)
        require(reply.code == 0 and json.loads(reply.data).get("id") == receipt["repository_id"], "RESTORE_REPOSITORY_ID")
        reply = self.repository.run(restic_command(c, ["ls", "--json", receipt["snapshot_id"]]), self.config["limits"]["metadata_bytes"])
        require(reply.code == 0, "RESTORE_LIST_EXIT")
        # Capture's exact file list includes its content manifest as the final member.
        manifest_path = capture_dir / "manifest.json"
        info = manifest_path.stat()
        expected = dict(members)
        expected[str(manifest_path)] = {"type": "file", "size": info.st_size, "mode": stat.S_IMODE(info.st_mode),
                                         "uid": info.st_uid, "gid": info.st_gid}
        verify_nodes(expected, snapshot_nodes(reply.data, receipt["snapshot_id"]))
        reply = self.repository.run(restic_command(c, ["dump", receipt["snapshot_id"], str(manifest_path)]), info.st_size)
        require(reply.code == 0 and digest(reply.data) == receipt["content_manifest_sha256"], "RESTORE_STORED_MANIFEST")

    def fetch(self, snapshot, source, output, cap):
        self.tick()
        result = self.repository.run(restic_command(self.restic_config, ["dump", snapshot, source]), cap, output)
        require(result.code == 0, "RESTORE_FETCH_EXIT")

    def unit_argv(self, suffix, database):
        unit = self.unit(suffix)
        props = ["Type=exec", "Description=" + self.description, "User=mysql", "Group=mysql", "UMask=0077",
                 "PrivateNetwork=yes", "RestrictAddressFamilies=AF_UNIX", "IPAddressDeny=any",
                 "NoNewPrivileges=yes", "PrivateTmp=yes", "ProtectHome=yes", "ProtectSystem=strict",
                 "RuntimeDirectory=mysqld", "RuntimeDirectoryMode=0750", "KillMode=control-group",
                 "TimeoutStopSec=20", "SendSIGKILL=yes", "RuntimeMaxSec=900", "MemoryMax=768M",
                 "MemorySwapMax=0", "CPUQuota=100%", "TasksMax=128", "LimitCORE=0",
                 "StandardOutput=null", "StandardError=null",
                 "BindPaths=" + str(database) + ":/var/lib/mysql",
                 "ReadWritePaths=/var/lib/mysql /run/mysqld " + str(self.log(unit))]
        args = ["/usr/sbin/mysqld", "--no-defaults", "--datadir=/var/lib/mysql",
                "--socket=/run/mysqld/mysqld.sock", "--pid-file=/run/mysqld/mysqld.pid",
                "--log-error=" + str(self.log(unit)), "--skip-networking", "--mysqlx=OFF",
                "--event-scheduler=DISABLED", "--skip-slave-start", "--skip-log-bin", "--persisted-globals-load=OFF",
                "--local-infile=OFF", "--secure-file-priv=NULL", "--innodb-buffer-pool-size=134217728",
                "--max-connections=16", "--performance-schema=OFF", "--skip-name-resolve", "--default-time-zone=+00:00",
                "--initialize-insecure" if suffix == "init" else "--upgrade=NONE"]
        return ["/usr/bin/systemd-run", "--quiet", "--collect", "--unit=" + unit,
                *(["--wait"] if suffix == "init" else []), *["--property=" + p for p in props],
                "--", "/usr/bin/env", "-i", "PATH=/usr/bin:/bin", "LANG=C.UTF-8", *args]

    def start(self, suffix, database):
        unit = self.unit(suffix)
        create_bytes(self.log(unit), b"")
        os.chown(self.log(unit), self.config["mysql_uid"], self.config["mysql_gid"])
        self.units.add(unit)  # Register before an ambiguous systemd-run outcome.
        self.command(self.unit_argv(suffix, database))

    def roundtrip(self, dump, target):
        database = target / "isolated-database"
        database.mkdir(mode=0o700)
        os.chown(database, self.config["mysql_uid"], self.config["mysql_gid"])
        self.start("init", database)
        self.start("db", database)
        row = self.show(self.unit("db"))
        require(row.get("Description") == self.description and row.get("User") == "mysql" and
                row.get("PrivateNetwork") == "yes" and row.get("MainPID", "0").isdigit(), "RESTORE_LIVE_UNIT")
        pid = int(row["MainPID"])
        require(pid > 1, "RESTORE_LIVE_PID")
        mounted = Path("/proc") / str(pid) / "root/var/lib/mysql"
        actual, expected = mounted.stat(), database.stat()
        require((actual.st_dev, actual.st_ino) == (expected.st_dev, expected.st_ino), "RESTORE_LIVE_MOUNT")
        require(os.readlink(f"/proc/{pid}/ns/net") != os.readlink("/proc/self/ns/net"), "RESTORE_LIVE_NETWORK")
        client = ["/usr/bin/mysql", "--no-defaults", "--protocol=socket", "--socket=/run/mysqld/mysqld.sock",
                  "--user=root", "--batch", "--skip-column-names", "--connect-timeout=3", "--default-character-set=utf8mb4"]
        until = min(self.deadline, self.now() + 30)
        while True:
            self.tick()
            probe = self.client.run([*client, "--execute=SELECT 1"], 64)
            if probe.code == 0 and probe.data.strip() == b"1":
                break
            require(self.now() < until, "RESTORE_DB_READINESS")
            time.sleep(0.2)
        self.tick()
        imported = self.client.run([*client, "--binary-mode"], 65536, input_path=dump)
        require(imported.code == 0, "RESTORE_IMPORT_EXIT")
        c = self.config
        listing = self.command([*client, "--execute=SELECT TABLE_NAME,TABLE_TYPE,ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='" + c["schema"] + "' ORDER BY TABLE_NAME"], c["limits"]["metadata_bytes"])
        require(listing.decode().splitlines() == [t + "\tBASE TABLE\tInnoDB" for t in c["tables"]], "RESTORE_DB_TABLES")
        return self.command(["/usr/bin/mysqldump", "--no-defaults", "--protocol=socket", "--socket=/run/mysqld/mysqld.sock",
                             "--user=root", "--single-transaction", "--quick", "--hex-blob", "--routines", "--events", "--triggers",
                             "--set-gtid-purged=OFF", "--no-tablespaces", "--skip-extended-insert", "--skip-add-drop-table",
                             "--tz-utc", "--default-character-set=utf8mb4", "--databases", c["schema"]], c["limits"]["dump_bytes"])

    def stop_owned(self):
        # Cleanup deliberately receives a fresh bounded window; expired task time cannot skip stop.
        self.deadline = self.now() + 60
        failures = []
        for runner in (self.repository, self.client):
            try:
                runner.stop_owned()
            except BaseException:
                failures.append("reader")
        class CleanupMonitor:
            def __init__(self, deadline):
                self.deadline = deadline
            def tick(self, force=False):
                require(time.time() < self.deadline, "RESTORE_CLEANUP_DEADLINE")
        self.monitor = CleanupMonitor(self.deadline)
        self.client.monitor = self.monitor
        # No capture/resource gate may prevent owned-unit termination.
        self.tick = lambda: self.monitor.tick()
        for unit in sorted(self.units):
            try:
                row = self.show(unit)
                if row.get("LoadState") == "not-found":
                    continue
                require(row.get("Description") == self.description, "RESTORE_UNIT_OWNERSHIP")
                self.command(["/usr/bin/systemctl", "stop", "--", unit])
                row = self.show(unit)
                require(row.get("MainPID") == row.get("ControlPID") == "0", "RESTORE_UNIT_ACTIVE")
            except BaseException:
                failures.append(unit)
        require(not failures, "RESTORE_CLEANUP_UNCERTAIN")
        require(not Path("/run/mysqld").exists(), "RESTORE_RUNTIME_REMAINS")

    def ended(self):
        return not self.client.active and not self.repository.active


def execute_restore(config: dict, approval: dict, capture_config: dict) -> dict:
    """Only native entry: exact files/approval, shared lock and bounded helper cgroup."""
    require(sys.platform == "linux" and os.geteuid() == 0, "RESTORE_NATIVE_ONLY")
    validate(config, approval, time.time())
    from online_capture import validate_online
    validate_online(capture_config)
    require(digest(encoded(capture_config)) == config["capture_config_sha256"], "RESTORE_CAPTURE_CONFIG_PIN")
    import outer_recovery as outer
    with outer.gate(capture_config) as owner:
        if outer.enabled(capture_config):
            outer.pins(capture_config)
            outer.scan(capture_config, owner)
        monitor = LinuxMonitor(capture_config, timestamp(approval["not_after"]))
        runner = LinuxRunner(monitor, restic_environment(capture_config))
        backend = NativeRestoreBackend(config, capture_config, runner, monitor)
        def terminated(signum, frame):
            raise GateError("RESTORE_TERMINATED")
        previous = signal.signal(signal.SIGTERM, terminated)
        try:
            return RestoreSession(config, approval, Path(config["capture_dir"]), backend).run()
        finally:
            signal.signal(signal.SIGTERM, previous)


def helper_argv(config: dict, module_path: str, config_path: str,
                approval_path: str, capture_config_path: str) -> list[str]:
    """Pure argv preparation; exact private paths become part of reviewed execution plan.

    No launch or authority issuance here. Native entry independently verifies
    exact config/approval/proofs, module bytes and actual cgroup limits.
    """
    require(RUN.fullmatch(config.get("restore_id", "")) is not None, "RESTORE_ID")
    for path in (module_path, config_path, approval_path, capture_config_path):
        linux_path(path)
        require(re.fullmatch(r"/[A-Za-z0-9_./-]+", path), "RESTORE_HELPER_PATH")
    require(PurePosixPath(module_path).name == "online_restore.py", "RESTORE_HELPER_MODULE")
    unit = "ats-online-restore-helper-" + config["restore_id"] + ".service"
    return ["/usr/bin/systemd-run", "--quiet", "--wait", "--pipe", "--collect", "--unit=" + unit,
            "--property=Type=exec", "--property=UMask=0077", "--property=MemoryHigh=768M",
            "--property=MemoryMax=1G", "--property=MemorySwapMax=0", "--property=CPUQuota=100%",
            "--property=Nice=10", "--property=KillMode=control-group", "--property=TimeoutStopSec=70",
            "--property=RuntimeMaxSec=1900", "--property=LimitCORE=0", "--property=IPAddressDeny=any",
            "--property=UnsetEnvironment=PYTHONPATH PYTHONHOME PYTHONSTARTUP PYTHONINSPECT PYTHONSAFEPATH",
            "--property=IPAddressAllow=" + " ".join(helper_addresses(config)), "--", "/usr/bin/env", "-i",
            "PATH=/usr/bin:/bin", "LANG=C.UTF-8", "/usr/bin/python3", "-B", module_path,
            "execute", "--config", config_path, "--approval", approval_path,
            "--capture-config", capture_config_path]


def main(argv=None):
    import argparse
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("plan", "execute"), default="plan", nargs="?")
    for name in ("config", "approval", "capture-config"):
        parser.add_argument("--" + name)
    args = parser.parse_args(argv)
    if args.command == "plan":
        print(json.dumps({"status": "PREPARATION_ONLY", "execution_authorized": False}))
        return 0
    try:
        require(sys.platform == "linux" and os.geteuid() == 0, "RESTORE_NATIVE_ONLY")
        documents = [json.loads(bounded_read(checked_path(p, private=True), 1024**2))
                     for p in (args.config, args.approval, args.capture_config)]
        result = execute_restore(*documents)
        # Public output contains no paths/SQL/identities. Detailed receipt stays private.
        print(json.dumps({k: result[k] for k in ("status", "restore_verified", "cleanup")}))
        return 0 if result["restore_verified"] else 1
    except BaseException as error:
        code = str(error) if isinstance(error, GateError) else "RESTORE_EXECUTION_FAILURE"
        print(json.dumps({"status": "HOLD", "error_code": code, "restore_verified": False}))
        return 1


if __name__ == "__main__":
    sys.exit(main())
