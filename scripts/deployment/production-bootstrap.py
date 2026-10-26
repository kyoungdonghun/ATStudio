"""WI025: offline plan and separately authorized, one-shot production bootstrap."""

from __future__ import annotations

import argparse
import base64
import hashlib
import json
import os
import re
import secrets
import stat
import subprocess
import sys
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import BinaryIO

import validate_bundle as validator


DB = "atstudio_prod"
ACCOUNT = "'atstudio_app'@'127.0.0.1'"
GRANT_DB = r"atstudio\_prod"
GRANT_HEX = GRANT_DB.encode("ascii").hex()
CONFIG = Path("/etc/atstudio")
ENV_PATH = CONFIG / "atstudio.env"
CLAIM_PATH = CONFIG / "production-bootstrap.claim"
RECEIPT_PATH = CONFIG / "production-bootstrap-receipt.json"
APPROVAL_PATH = CONFIG / "production-bootstrap-approval.json"
SOCKET = "/run/mysqld/mysqld.sock"
REQ = "REQ-20261010-ATS-008"
WI = "WI-20261010-ATS-025"
SCHEMA = "src/main/resources/schema.sql"
SEED = "src/main/resources/seed.sql"
PINS = {
    SCHEMA: "6fb7df1518394371bbfc5513df5ebe2c97e067e7f77a1f20661afb1c00b63fb7",
    SEED: "c848e0da6b9b35eb26563d6af3666b67fc119430c1afd888ea7b0246800a579a",
}
CHECKS = (
    "pinned_ssh_target", "exclusive_maintenance", "no_session_io_recording",
    "no_sql_response_capture", "no_external_secret_export",
    "mysql_temporarily_loopback", "stop_remask_on_exit", "root_recovery_ready",
)
LIMIT = 256 * 1024
CHILD_ENV = {"PATH": "/usr/sbin:/usr/bin:/sbin:/bin", "HOME": "/root",
             "LC_ALL": "C", "MYSQL_HISTFILE": "/dev/null"}
MYSQL_ARGS = [
    "/usr/bin/mysql", "--no-defaults", "--protocol=SOCKET", "--socket=" + SOCKET,
    "--user=root", "--skip-password", "--batch", "--raw", "--skip-column-names",
    "--skip-reconnect", "--skip-auto-rehash", "--binary-mode", "--local-infile=0",
    "--connect-timeout=5", "--default-character-set=utf8mb4",
]


class GateError(Exception):
    """Fixed identifiers only; never attach command output or secret values."""


def require(condition: bool, code: str) -> None:
    if not condition:
        raise GateError(code)


def digest(raw: bytes) -> str:
    return hashlib.sha256(raw).hexdigest()


def read_public(path: Path) -> bytes:
    validator.no_links(path)
    require(path.is_file() and path.stat().st_nlink == 1, "SOURCE_FILE")
    with path.open("rb") as stream:
        raw = stream.read(LIMIT + 1)
    require(len(raw) <= LIMIT, "SOURCE_SIZE")
    return raw


@dataclass(repr=False)
class Bundle:
    schema: bytes
    seed: bytes
    env: dict[str, str]
    tables: list[str]
    hashes: dict[str, str]
    identity: str


def load_bundle(root: Path) -> Bundle:
    require(validator.validate(root)["template_status"] == "PASS", "BUNDLE_CONTRACT")
    paths = set(validator.TEMPLATE_PATHS) | {
        SCHEMA, SEED, validator.SOURCE, "scripts/deployment/validate_bundle.py",
        "scripts/deployment/production-bootstrap.py",
    }
    content = {name: read_public(root / name) for name in sorted(paths)}
    hashes = {name: digest(raw) for name, raw in content.items()}
    require(all(hashes[name] == value for name, value in PINS.items()), "SQL_IDENTITY")
    # Extract names only from hash-pinned DDL; this is not a general SQL parser.
    tables = sorted(re.findall(r"^CREATE TABLE ([a-z_]+)\s*$",
                               content[SCHEMA].decode("utf-8"), re.MULTILINE))
    require(len(tables) == len(set(tables)) == 43, "SCHEMA_TABLES")
    env = validator.parse_env(content[validator.ENV].decode("utf-8"))
    identity = digest(json.dumps(hashes, sort_keys=True).encode("ascii"))
    return Bundle(content[SCHEMA], content[SEED], env, tables, hashes, identity)


def safe_status(status: str, bundle: Bundle | None = None) -> dict:
    result = {"wi": WI, "status": status, "app_runnable": False,
              "production_status": "HOLD", "canonical_fresh_manifest": "UNRECORDED"}
    if bundle:
        result["bundle_sha256"] = bundle.identity
    return result


def secure_path(path: Path, *, directory: bool = False, private: bool = False) -> None:
    validator.no_links(path)
    for parent in path.parents:
        info = parent.stat()
        require(info.st_uid == 0 and not info.st_mode & 0o022, "PATH_ANCESTRY")
    info = path.stat()
    require(info.st_uid == 0 and not info.st_mode & 0o022, "PATH_OWNER")
    require(stat.S_ISDIR(info.st_mode) if directory else stat.S_ISREG(info.st_mode),
            "PATH_TYPE")
    if not directory:
        require(info.st_nlink == 1, "PATH_HARDLINK")
    if private:
        require(stat.S_IMODE(info.st_mode) == (0o700 if directory else 0o600),
                "PATH_MODE")


def absent(path: Path) -> None:
    try:
        path.lstat()
    except FileNotFoundError:
        return
    raise GateError("EXISTING_PATH")


def parse_approval(raw: bytes, bundle: Bundle, machine_hash: str,
                   now: datetime) -> dict:
    approval = json.loads(raw, object_pairs_hook=validator.unique_object)
    require(isinstance(approval, dict), "APPROVAL_SHAPE")
    require(set(approval) == {
        "req", "review_wi", "bundle_sha256", "machine_id_sha256",
        "server_uuid_sha256", "issued_at", "expires_at", "operator_checks",
    }, "APPROVAL_SHAPE")
    require(approval["req"] == REQ and approval["review_wi"] == "WI-20261010-ATS-027",
            "APPROVAL_REVIEW")
    for key in ("bundle_sha256", "machine_id_sha256", "server_uuid_sha256"):
        require(isinstance(approval[key], str) and
                re.fullmatch(r"[0-9a-f]{64}", approval[key]) is not None, "APPROVAL_HASH")
    require(approval["bundle_sha256"] == bundle.identity and
            approval["machine_id_sha256"] == machine_hash, "APPROVAL_IDENTITY")
    require(approval["operator_checks"] == dict.fromkeys(CHECKS, True) and
            all(type(value) is bool for value in approval["operator_checks"].values()),
            "APPROVAL_CHECKS")
    for key in ("issued_at", "expires_at"):
        require(isinstance(approval[key], str) and re.fullmatch(
            r"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ", approval[key]) is not None,
            "APPROVAL_TIME")
    issued = datetime.fromisoformat(approval["issued_at"].replace("Z", "+00:00"))
    expires = datetime.fromisoformat(approval["expires_at"].replace("Z", "+00:00"))
    require(issued <= now < expires <= issued + timedelta(minutes=30), "APPROVAL_EXPIRED")
    return approval


def run_private(argv: list[str], payload: bytes = b"") -> bytes:
    try:
        result = subprocess.run(argv, input=payload, stdout=subprocess.PIPE,
                                stderr=subprocess.DEVNULL, env=CHILD_ENV,
                                cwd="/", timeout=60, check=False)
    except (OSError, subprocess.SubprocessError):
        raise GateError("NATIVE_COMMAND_FAILED") from None
    require(result.returncode == 0, "NATIVE_COMMAND_FAILED")
    require(len(result.stdout) <= LIMIT, "NATIVE_OUTPUT_BOUND")
    return result.stdout


def sql(statement: str | bytes, *, mutation: bool = False) -> str:
    secure_path(Path("/root"), directory=True)
    for name in (".my.cnf", ".mylogin.cnf"):
        absent(Path("/root") / name)
    raw = statement.encode("utf-8") if isinstance(statement, str) else statement
    # Disable binlog on every non-reconnecting mutation session; SQL has no secrets.
    if mutation:
        raw = b"SET SESSION sql_log_bin=0;\n" + raw
    return run_private(MYSQL_ARGS, raw).decode("utf-8", errors="strict").rstrip("\n")


def yes(statement: str, code: str) -> None:
    require(sql(statement) == "1", code)


def mutate(approval: dict, statement: str | bytes) -> str:
    expires = datetime.fromisoformat(approval["expires_at"].replace("Z", "+00:00"))
    require(datetime.now(timezone.utc) < expires, "APPROVAL_EXPIRED")
    return sql(statement, mutation=True)


def native_guard() -> None:
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    import ctypes
    import resource

    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    require(ctypes.CDLL(None).prctl(4, 0, 0, 0, 0) == 0, "PROCESS_DUMP_GUARD")
    os.umask(0o077)
    secure_path(CONFIG, directory=True, private=True)
    for binary in ("/usr/bin/mysql", "/usr/bin/systemctl", "/usr/bin/ss"):
        secure_path(Path(binary))
    secure_path(Path(__file__).absolute())
    secure_path(Path(validator.__file__).absolute())
    for path in (ENV_PATH, CLAIM_PATH, RECEIPT_PATH):
        absent(path)


def host_guard() -> None:
    for unit in ("atstudio.service", "nginx.service"):
        output = run_private(["/usr/bin/systemctl", "show", unit,
                              "--property=LoadState,ActiveState,UnitFileState"]).decode("ascii")
        values = dict(line.split("=", 1) for line in output.splitlines())
        require(values.get("ActiveState") == "inactive" and
                (values.get("LoadState") == "not-found" or
                 values.get("UnitFileState") == "masked"), "APP_PROXY_NOT_FENCED")
    for process in Path("/proc").iterdir():
        if process.name.isdigit():
            try:
                name = (process / "comm").read_text().strip()
            except FileNotFoundError:
                continue
            require(name not in ("java", "ffmpeg", "atstudio"), "WRITER_PROCESS")
    listeners = run_private(["/usr/bin/ss", "-H", "-ltn"]).decode("ascii")
    db_listeners = []
    for line in listeners.splitlines():
        columns = line.split()
        require(len(columns) >= 5, "LISTENER_SHAPE")
        address = columns[3]
        if address.rsplit(":", 1)[-1] in ("3306", "33060", "33062"):
            db_listeners.append(address)
    require(db_listeners == ["127.0.0.1:3306"], "MYSQL_LISTENER")


def database_guard(server_hash: str, *, fresh: bool) -> None:
    require(sql("SELECT SHA2(@@server_uuid,256);") == server_hash, "MYSQL_TARGET")
    yes("SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND CURRENT_USER()='root@localhost' "
        "AND @@global.bind_address='127.0.0.1' AND @@global.port=3306 "
        "AND @@global.socket IN ('/run/mysqld/mysqld.sock','/var/run/mysqld/mysqld.sock') "
        "AND COALESCE(@@global.admin_address,'')='' "
        "AND @@global.local_infile=0 AND @@global.general_log=0 "
        "AND @@global.slow_query_log=0 AND @@global.log_raw=0 "
        "AND @@global.mandatory_roles='' AND @@global.activate_all_roles_on_login=0 "
        "AND @@global.partial_revokes=0;",
        "MYSQL_CONFIGURATION")
    yes("SELECT COUNT(*)=1 FROM mysql.user WHERE User='root' AND Host='localhost' "
        "AND plugin='auth_socket';", "MYSQL_ROOT_AUTH")
    yes("SELECT COUNT(*)=0 FROM mysql.user WHERE User='';", "MYSQL_ANONYMOUS_ACCOUNT")
    yes("SELECT COUNT(*)=0 FROM information_schema.plugins WHERE PLUGIN_STATUS='ACTIVE' "
        "AND ((PLUGIN_TYPE='AUDIT' AND (COALESCE(BINARY PLUGIN_NAME IN "
        "('sha2_cache_cleaner','mysqlx_cache_cleaner'),0)=0 OR PLUGIN_LIBRARY IS NOT NULL)) "
        "OR PLUGIN_NAME IN ('mysqlx','group_replication'));",
        "MYSQL_PLUGIN_CAPTURE")
    yes("SELECT COUNT(*)=0 FROM mysql.component "
        "WHERE component_urn <> 'file://component_validate_password';", "MYSQL_COMPONENT")
    yes("SELECT COUNT(*)=0 FROM performance_schema.replication_connection_configuration;",
        "MYSQL_REPLICATION")
    yes("SELECT COUNT(*)=0 FROM information_schema.PROCESSLIST WHERE ID<>CONNECTION_ID() "
        "AND USER NOT IN ('event_scheduler','system user');", "MYSQL_OTHER_CLIENT")
    if fresh:
        yes("SELECT COUNT(*)=0 FROM information_schema.SCHEMATA "
            "WHERE LOWER(SCHEMA_NAME)='atstudio_prod';", "EXISTING_DATABASE")
        yes("SELECT COUNT(*)=0 FROM mysql.user WHERE LOWER(User)='atstudio_app';",
            "EXISTING_ACCOUNT")


def preflight(bundle: Bundle) -> dict:
    native_guard()
    secure_path(APPROVAL_PATH, private=True)
    secure_path(Path("/etc/machine-id"))
    machine_hash = digest(Path("/etc/machine-id").read_bytes().strip())
    raw = read_public(APPROVAL_PATH)
    require(len(raw) <= 8192, "APPROVAL_SIZE")
    approval = parse_approval(raw, bundle, machine_hash, datetime.now(timezone.utc))
    host_guard()
    database_guard(approval["server_uuid_sha256"], fresh=True)
    return approval


def create_private(path: Path) -> BinaryIO:
    secure_path(path.parent, directory=True, private=True)
    descriptor = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600)
    return os.fdopen(descriptor, "wb")


def persist(stream: BinaryIO, raw: bytes) -> None:
    stream.write(raw)
    stream.flush()
    os.fsync(stream.fileno())


def sync_directory() -> None:
    descriptor = os.open(CONFIG, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
    try:
        os.fsync(descriptor)
    finally:
        os.close(descriptor)


def password_result(output: str) -> str:
    cells = output.split("\t")
    require(len(cells) in (3, 4) and cells[:2] == ["atstudio_app", "127.0.0.1"],
            "PASSWORD_RESULT")
    require(len(cells) == 3 or cells[3] == "1", "PASSWORD_RESULT")
    require(re.fullmatch(r"[!-~]{32}", cells[2]) is not None, "PASSWORD_RESULT")
    return cells[2]


def render_env(bundle: Bundle, password: str, jwt: bytes, billing: bytes) -> bytes:
    require(len(jwt) == len(billing) == 32 and jwt != billing, "KEY_INDEPENDENCE")
    require(re.fullmatch(r"[!-~]{32}", password) is not None, "PASSWORD_RESULT")
    env = dict(bundle.env)
    env.update({
        "SPRING_DATASOURCE_URL": env["SPRING_DATASOURCE_URL"].replace("__DB_NAME__", DB),
        "SPRING_DATASOURCE_USERNAME": "atstudio_app",
        "SPRING_DATASOURCE_PASSWORD": password,
        "JWT_SECRET": base64.b64encode(jwt).decode("ascii"),
        "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID": "prod-bootstrap-20261010",
        "PAYMENT_BILLING_KEY_0_ID": "prod-bootstrap-20261010",
        "PAYMENT_BILLING_KEY_0_SECRET": base64.b64encode(billing).decode("ascii"),
    })
    require(set(env) == set(validator.EXPECTED_ENV), "ENV_KEYS")
    lines = ["# NOT RUNNABLE: domain, SMTP, exact artifact and startup approval pending."]
    for key, value in env.items():
        require(all(32 <= ord(char) <= 126 for char in value), "ENV_VALUE")
        value = value.replace("\\", "\\\\").replace('"', '\\"')
        lines.append(f'{key}="{value}"')
    return ("\n".join(lines) + "\n").encode("ascii")


def observe(bundle: Bundle) -> dict:
    actual = sql("SELECT TABLE_NAME FROM information_schema.TABLES "
                 "WHERE TABLE_SCHEMA='atstudio_prod' AND TABLE_TYPE='BASE TABLE' "
                 "ORDER BY TABLE_NAME;").splitlines()
    require(actual == bundle.tables, "OBSERVATION_TABLES")
    for table in bundle.tables:
        count = "6" if table == "subscriptions" else "0"
        require(sql(f"SELECT COUNT(*) FROM `{DB}`.`{table}`;") == count, "OBSERVATION_ROWS")
    yes("SELECT COUNT(*)=6 AND COUNT(DISTINCT name,user_type)=6 AND SUM("
        "description=CONCAT(IF(user_type='INDIVIDUAL','Individual','Business'),' ',"
        "CONCAT(UPPER(LEFT(name,1)),LOWER(SUBSTRING(name,2))),' plan') "
        "AND is_active=1 AND CASE CONCAT(user_type,':',name) "
        "WHEN 'INDIVIDUAL:STANDARD' THEN price_monthly=9900 AND price_yearly=99000 "
        "AND download_per_day=5 AND max_whitelist_channels=1 AND max_playlists=3 "
        "WHEN 'INDIVIDUAL:DELUXE' THEN price_monthly=19900 AND price_yearly=199000 "
        "AND download_per_day=20 AND max_whitelist_channels=2 AND max_playlists=10 "
        "WHEN 'INDIVIDUAL:PREMIUM' THEN price_monthly=29900 AND price_yearly=299000 "
        "AND download_per_day=-1 AND max_whitelist_channels=2 AND max_playlists=10 "
        "WHEN 'BUSINESS:STANDARD' THEN price_monthly=19900 AND price_yearly=199000 "
        "AND download_per_day=10 AND max_whitelist_channels=1 AND max_playlists=3 "
        "WHEN 'BUSINESS:DELUXE' THEN price_monthly=49900 AND price_yearly=499000 "
        "AND download_per_day=50 AND max_whitelist_channels=2 AND max_playlists=10 "
        "WHEN 'BUSINESS:PREMIUM' THEN price_monthly=99900 AND price_yearly=999000 "
        "AND download_per_day=-1 AND max_whitelist_channels=2 AND max_playlists=10 "
        "ELSE 0 END)=6 FROM atstudio_prod.subscriptions;", "OBSERVATION_PLANS")
    queries = {
        "columns": "SELECT TABLE_NAME,COLUMN_NAME,ORDINAL_POSITION,COLUMN_TYPE,IS_NULLABLE,"
                   "COALESCE(COLUMN_DEFAULT,'<NULL>'),EXTRA FROM information_schema.COLUMNS "
                   "WHERE TABLE_SCHEMA='atstudio_prod' ORDER BY TABLE_NAME,ORDINAL_POSITION;",
        "indexes": "SELECT TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX,COLUMN_NAME,NON_UNIQUE,INDEX_TYPE "
                   "FROM information_schema.STATISTICS WHERE TABLE_SCHEMA='atstudio_prod' "
                   "ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX;",
        "foreign_keys": "SELECT TABLE_NAME,CONSTRAINT_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME,"
                        "REFERENCED_COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE "
                        "WHERE TABLE_SCHEMA='atstudio_prod' AND REFERENCED_TABLE_NAME IS NOT NULL "
                        "ORDER BY TABLE_NAME,CONSTRAINT_NAME,ORDINAL_POSITION;",
    }
    metadata = {key: sql(query) for key, query in queries.items()}
    return {"tables": len(actual), "plans": 6, "non_plan_rows": 0,
            **{key: len(value.splitlines()) for key, value in metadata.items()},
            "observation_sha256": digest(json.dumps(metadata, sort_keys=True).encode("utf-8"))}


def verify_grants(*, locked: bool) -> None:
    grantee = "CONCAT(CHAR(39),'atstudio_app',CHAR(39),'@',CHAR(39),'127.0.0.1',CHAR(39))"
    require(sql("SELECT PRIVILEGE_TYPE,IS_GRANTABLE FROM information_schema.SCHEMA_PRIVILEGES "
                f"WHERE GRANTEE={grantee} AND HEX(TABLE_SCHEMA)=UPPER('{GRANT_HEX}') "
                "ORDER BY PRIVILEGE_TYPE;") ==
            "DELETE\tNO\nINSERT\tNO\nSELECT\tNO\nUPDATE\tNO", "GRANTS_DML")
    for view, condition in (
        ("USER_PRIVILEGES", "PRIVILEGE_TYPE<>'USAGE' OR IS_GRANTABLE<>'NO'"),
        ("SCHEMA_PRIVILEGES", f"HEX(TABLE_SCHEMA)<>UPPER('{GRANT_HEX}')"),
        ("TABLE_PRIVILEGES", "1=1"), ("COLUMN_PRIVILEGES", "1=1"),
    ):
        yes(f"SELECT COUNT(*)=0 FROM information_schema.{view} "
            f"WHERE GRANTEE={grantee} AND ({condition});", "GRANTS_EXTRA")
    for table, condition in (
        ("global_grants", "USER='atstudio_app'"),
        ("procs_priv", "User='atstudio_app'"),
        ("role_edges", "TO_USER='atstudio_app' OR FROM_USER='atstudio_app'"),
        ("default_roles", "USER='atstudio_app'"),
        ("proxies_priv", "User='atstudio_app' OR Proxied_user='atstudio_app'"),
    ):
        yes(f"SELECT COUNT(*)=0 FROM mysql.{table} WHERE {condition};", "GRANTS_EXTRA")
    yes("SELECT COUNT(*)=1 FROM mysql.user WHERE User='atstudio_app' AND Host='127.0.0.1' "
        "AND plugin='caching_sha2_password' AND password_expired='N' "
        f"AND account_locked='{'Y' if locked else 'N'}';", "ACCOUNT_STATE")
    yes("SELECT COUNT(*)=1 FROM mysql.user WHERE LOWER(User)='atstudio_app';", "ACCOUNT_SCOPE")


def apply(bundle: Bundle) -> dict:
    approval = preflight(bundle)
    # Keep the exclusive permanent claim on success or failure; never retry partial DDL.
    with create_private(CLAIM_PATH) as claim:
        persist(claim, (WI + "\n" + bundle.identity + "\n").encode("ascii"))
    sync_directory()
    with create_private(ENV_PATH) as env_file:
        persist(env_file, b"# INCOMPLETE BOOTSTRAP; DO NOT START OR RETRY.\n")
        sync_directory()
        host_guard()
        database_guard(approval["server_uuid_sha256"], fresh=True)
        mutate(approval, "CREATE DATABASE `atstudio_prod` CHARACTER SET utf8mb4 "
               "COLLATE utf8mb4_unicode_ci;")
        yes("SELECT COUNT(*)=0 FROM information_schema.TABLES "
            "WHERE TABLE_SCHEMA='atstudio_prod';", "DATABASE_NOT_EMPTY")
        mutate(approval, b"USE `atstudio_prod`;\n" + bundle.schema)
        mutate(approval, b"USE `atstudio_prod`;\n" + bundle.seed)
        observation = observe(bundle)
        database_guard(approval["server_uuid_sha256"], fresh=False)
        password = password_result(mutate(approval,
            "SET SESSION generated_random_password_length=32;\n"
            f"CREATE USER {ACCOUNT} IDENTIFIED WITH caching_sha2_password "
            "BY RANDOM PASSWORD ACCOUNT LOCK;"))
        persist(env_file, render_env(bundle, password, secrets.token_bytes(32), secrets.token_bytes(32)))
        del password
        mutate(approval, f"GRANT SELECT,INSERT,UPDATE,DELETE ON `{GRANT_DB}`.* TO {ACCOUNT};")
        verify_grants(locked=True)
        host_guard()
        database_guard(approval["server_uuid_sha256"], fresh=False)
        mutate(approval, f"ALTER USER {ACCOUNT} ACCOUNT UNLOCK;")
        verify_grants(locked=False)
    secure_path(ENV_PATH, private=True)
    result = safe_status("BOOTSTRAP_APPLIED_NOT_RUNNABLE", bundle)
    result["observation"] = observation
    result["mysql_cleanup"] = "MA_MUST_STOP_AND_REMASK"
    result["runtime_dml_only"] = True
    result["runtime_tcp_authentication"] = "NOT_RUN"
    result["input_sha256"] = bundle.hashes
    with create_private(RECEIPT_PATH) as receipt:
        persist(receipt, (json.dumps(result, sort_keys=True) + "\n").encode("ascii"))
    sync_directory()
    return result


class SafeParser(argparse.ArgumentParser):
    def error(self, message: str) -> None:
        raise GateError("CLI_ARGUMENTS")


def main(argv: list[str] | None = None) -> int:
    try:
        parser = SafeParser(description=__doc__)
        parser.add_argument("mode", choices=("plan", "preflight", "apply"), nargs="?", default="plan")
        parser.add_argument("--root", type=Path, default=Path(__file__).absolute().parents[2])
        args = parser.parse_args(argv)
        bundle = load_bundle(args.root)
        if args.mode == "apply":
            result = apply(bundle)
        elif args.mode == "preflight":
            preflight(bundle)
            result = safe_status("PREFLIGHT_ONLY", bundle)
        else:
            result = safe_status("PLAN_ONLY", bundle)
            result["input_sha256"] = bundle.hashes
            result["database"] = DB
            result["account"] = ACCOUNT
        print(json.dumps(result, sort_keys=True))
        return 0
    except GateError as error:
        result = safe_status("HOLD")
        result["rule"] = str(error)
    except KeyboardInterrupt:
        result = safe_status("HOLD")
        result["rule"] = "INTERRUPTED_PRESERVE_STATE"
    except Exception:
        result = safe_status("HOLD")
        result["rule"] = "UNEXPECTED_FAILURE_PRESERVE_STATE"
    print(json.dumps(result, sort_keys=True))
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
