"""Read-only offline deployment audit; WI-012 extends the WI-003 security baseline."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shlex
import stat
from collections import Counter
from dataclasses import dataclass
from pathlib import Path, PurePosixPath
from typing import Any


BASE = "deploy/single-vps/"
CONTRACT = BASE + "env/contract.json"
ENV = BASE + "env/atstudio.env.example"
SOURCE = "src/main/resources/application.yml"
MAX_BYTES = 256 * 1024

# Reviewed active-line shapes, independent of the editable contract. Not parsers.
SHAPES = {
    "nginx/http-bootstrap.conf.template": "b7ea5d9b55c58061dfbcb1ab7a5dcf9e1fd750ce96bbbb3f5cc3b553fec2d6b2",
    "nginx/https.conf.template": "f152baa9f93fe94853481647a1eccc4839f829d8fceb931a351e973b40e5609d",
    "nginx/nginx.conf.template": "499420f1c5d29ab774ca7cdeefa32c98a747185583cc716cbdf0519d596868df",
    "nginx/proxy.conf.template": "02fa11753804961518578b147ce1bce6ac332ebe22934adfc4970f924ae94e48",
    "nginx/restricted-access.conf": "5012a0596c600385bfdfa8c840b5ece1ecbdcf5320de5ff06a7efc0e3bc2b75d",
    "systemd/atstudio.service": "eeb428872b51658009f59155b6e4f2fcc109a24cc1a11d65dc15ff2b7ff407ba",
    "mysql/atstudio.cnf": "4c83adf53f28d68802820eaf9333706d3126f9395f374749f6d98a87af0daf0e",
    "journald/atstudio.conf": "f276899666065a2e5b476f96176eb9886cac1bb4edf2a9ac4c2e7a92f6ed45b3",
}
TEMPLATE_PATHS = frozenset({BASE + p for p in SHAPES} | {
    CONTRACT, ENV, BASE + "CONFIGURATION.md",
})
SAFE = {
    "SPRING_PROFILES_ACTIVE": "prod",
    "SPRING_CONFIG_LOCATION": "classpath:/",
    "SERVER_ADDRESS": "127.0.0.1",
    "SERVER_PORT": "8080",
    "SERVER_FORWARD_HEADERS_STRATEGY": "none",
    "SERVER_TOMCAT_ACCESSLOG_ENABLED": "false",
    "SPRING_SQL_INIT_MODE": "never",
    "SPRING_JPA_HIBERNATE_DDL_AUTO": "validate",
    "SPRING_JPA_SHOW_SQL": "false",
    "SWAGGER_ENABLED": "false",
    "APP_ACCEPTANCE_ENABLED": "false",
    "APP_BOOTSTRAP_TEST_USERS_ENABLED": "false",
    "APP_STORAGE_PUBLIC_PATH": "/srv/atstudio/public",
    "APP_STORAGE_PRIVATE_PATH": "/srv/atstudio/private",
    "APP_STORAGE_REQUIRE_EXPLICIT_ROOTS": "true",
    "APP_STORAGE_INTEGRITY_AUDIT_ON_STARTUP": "true",
    "APP_STORAGE_INTEGRITY_STRICT_ON_STARTUP": "true",
    "APP_STORAGE_MUTATION_ADMISSION_ENABLED": "true",
    "APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT": "2",
    "APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT_PER_IP": "1",
    "APP_STORAGE_MUTATION_ADMISSION_RETRY_AFTER_SECONDS": "2",
    "SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE": "100MB",
    "SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE": "120MB",
    "SPRING_SERVLET_MULTIPART_LOCATION": "/var/lib/atstudio/tmp",
    "SPRING_SERVLET_MULTIPART_FILE_SIZE_THRESHOLD": "0B",
    "APP_AUDIO_WORKER_ENABLED": "true",
    "APP_AUDIO_FFMPEG_PATH": "/usr/bin/ffmpeg",
    "APP_SECURITY_TRUSTED_CLIENT_IDENTITY_ENABLED": "true",
    "APP_SECURITY_TRUSTED_PROXY_ADDRESSES": "127.0.0.1",
    "APP_SECURITY_RATE_LIMIT_ENABLED": "true",
    "APP_PAYMENT_SCHEDULER_ZONE": "Asia/Seoul",
    "TZ": "Asia/Seoul",
    "APP_AUTH_PASSWORD_LOGIN_ENABLED": "false",
    "MAIL_SMTP_AUTH": "false",
    "MAIL_SMTP_STARTTLS": "false",
    "MAIL_SMTP_CONNECTION_TIMEOUT": "5000",
    "MAIL_SMTP_TIMEOUT": "10000",
    "MAIL_SMTP_WRITE_TIMEOUT": "10000",
    "PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED": "false",
    "LOG_LEVEL_HIBERNATE_SQL": "warn",
    "LOG_LEVEL_HIBERNATE_BINDER": "warn",
    "LOGGING_LEVEL_ORG_HIBERNATE_ORM_JDBC_BIND": "off",
    "LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB": "warn",
    "SPRING_MVC_LOG_REQUEST_DETAILS": "false",
}
EMPTY = frozenset("""
APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD MAIL_USERNAME MAIL_PASSWORD
GOOGLE_CLIENT_ID GOOGLE_CLIENT_SECRET KAKAO_CLIENT_ID KAKAO_CLIENT_SECRET
NAVER_CLIENT_ID NAVER_CLIENT_SECRET TOSS_CLIENT_KEY TOSS_SECRET_KEY
PAYMENT_OPERATIONS_OPERATOR_EMAIL
""".split())
TARGET = {
    "SPRING_DATASOURCE_URL": "jdbc:mysql://127.0.0.1:3306/__DB_NAME__?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
    "SPRING_DATASOURCE_USERNAME": "__DB_APP_USER__",
    "SPRING_DATASOURCE_PASSWORD": "__DB_PASSWORD__",
    "JWT_SECRET": "__JWT_BASE64_32_BYTES_MINIMUM__",
    "CORS_ALLOWED_ORIGINS": "https://__APP_FQDN__",
    "APP_BASE_URL": "https://__APP_FQDN__",
    "MAIL_HOST": "__SMTP_HOST__",
    "MAIL_PORT": "__SMTP_PORT__",
    "MAIL_FROM": "__SMTP_FROM__",
    "GOOGLE_REDIRECT_URI": "https://__APP_FQDN__/social-login/google",
    "KAKAO_REDIRECT_URI": "https://__APP_FQDN__/social-login/kakao",
    "NAVER_REDIRECT_URI": "https://__APP_FQDN__/social-login/naver",
    "TOSS_BILLING_AUTH_SUCCESS_URL": "https://__APP_FQDN__/subscriptions/checkout/success",
    "TOSS_BILLING_AUTH_FAIL_URL": "https://__APP_FQDN__/subscriptions/checkout/fail",
    "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID": "__BILLING_KEY_ID__",
    "PAYMENT_BILLING_KEY_0_ID": "__BILLING_KEY_ID__",
    "PAYMENT_BILLING_KEY_0_SECRET": "__BILLING_KEY_SECRET__",
}
STARTING = {
    "SERVER_TOMCAT_THREADS_MAX": "40",
    "SERVER_TOMCAT_MAX_CONNECTIONS": "128",
    "SERVER_TOMCAT_ACCEPT_COUNT": "20",
    "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE": "10",
    "SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE": "2",
    "SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT": "5000",
    "JWT_EXPIRATION": "3600000",
    "JWT_REFRESH_EXPIRATION": "1209600000",
    "APP_AUDIO_TIMEOUT_SECONDS": "300",
    "APP_AUDIO_POLL_INTERVAL_MS": "5000",
}
EXPECTED_ENV = SAFE | dict.fromkeys(EMPTY, "") | TARGET | STARTING
ROOTS = {
    "release": "/opt/atstudio/current",
    "jar": "/opt/atstudio/current/app.jar",
    "frontend": "/opt/atstudio/current/frontend",
    "public_storage": "/srv/atstudio/public",
    "private_storage": "/srv/atstudio/private",
    "config": "/etc/atstudio",
    "environment_file": "/etc/atstudio/atstudio.env",
    "temporary": "/var/lib/atstudio/tmp",
    "nginx_body_temporary": "/var/lib/atstudio/nginx-body",
    "acme_webroot": "/var/lib/atstudio/acme",
}
PLACEHOLDERS: dict[str, list[str]] = {"__NGINX_USER__": []}
for _key, _value in TARGET.items():
    for _token in re.findall(r"__[A-Z0-9_]+__", _value):
        PLACEHOLDERS.setdefault(_token, []).append(_key)

# These use Boot/JVM/OS binding, rather than explicit ${ENV} source references.
EXTERNAL_BINDINGS = frozenset("""
SPRING_PROFILES_ACTIVE SPRING_CONFIG_LOCATION SERVER_ADDRESS SERVER_PORT
SERVER_FORWARD_HEADERS_STRATEGY SERVER_TOMCAT_ACCESSLOG_ENABLED
SERVER_TOMCAT_THREADS_MAX SERVER_TOMCAT_MAX_CONNECTIONS SERVER_TOMCAT_ACCEPT_COUNT
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE SPRING_SQL_INIT_MODE
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE
SPRING_SERVLET_MULTIPART_FILE_SIZE_THRESHOLD
SPRING_SERVLET_MULTIPART_LOCATION TZ LOGGING_LEVEL_ORG_HIBERNATE_ORM_JDBC_BIND
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB SPRING_MVC_LOG_REQUEST_DETAILS
""".split())
GATES = (
    ("TARGET_IDENTITY", "HOLD"), ("SECRETS_FRESH_DB_APPROVAL", "HOLD"),
    ("LINUX_NATIVE_VALIDATION", "NOT_RUN"), ("TLS_FIREWALL_IP_STORAGE", "NOT_RUN"),
    ("EGRESS_SINGLE_JVM_WORKER", "NOT_RUN"),
    ("AUTH_MAIL_LOGS_RESTORE_REBOOT_LOAD", "NOT_RUN"),
    ("PUBLIC_RELEASE_LIVE_PAYMENT", "HOLD"),
)
MEDIA_PATTERN = r"^/api(?:;[^/]*)?/tracks(?:;[^/]*)?/[^/]+/(stream|download)(?:;[^/]*)?/?$"
DOWNLOAD_PATTERN = MEDIA_PATTERN.replace("(stream|download)", "download")
LEGACY_MEDIA_PATTERN = r"^/api/tracks/[0-9]+/(stream|download)/?$"
MEDIA_CASES = {
    "/api/tracks/1/stream": True,
    "/api/tracks/1/download/": True,
    "/api/tracks/+1/stream": True,
    "/api/tracks/+1/download": True,
    "/api/tracks/0x1/stream": True,
    "/api/tracks/0x1/download/": True,
    "/api/tracks/1;x=y/stream": True,
    "/api/tracks/+1;x=y/download;v=1": True,
    "/api;v=1/tracks;v=1/0x1;x=y/stream;v=1/": True,
    "/api/tracks": False,
    "/api/tracks/1": False,
    "/api/tracks//stream": False,
    "/api/tracks/1/streaming": False,
    "/api/tracks/1/download/extra": False,
    "/api/notices/1/attachments/1": False,
    "/uploads/tracks/audio/1": False,
}
GENERAL_LIMITS = {
    ("ats_client", "8"), ("ats_server", "32"),
    ("ats_multipart_client", "1"), ("ats_multipart_server", "2"),
}
MEDIA_LIMITS = GENERAL_LIMITS | {
    ("ats_media_client", "4"), ("ats_media_server", "16"),
    ("ats_download_client", "1"), ("ats_download_server", "2"),
}
SOURCE_BINDINGS = {
    "spring.datasource.hikari.maximum-pool-size": "${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:10}",
    "spring.datasource.hikari.minimum-idle": "${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:2}",
    "spring.datasource.hikari.connection-timeout": "${SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT:5000}",
    "spring.servlet.multipart.file-size-threshold": "${SPRING_SERVLET_MULTIPART_FILE_SIZE_THRESHOLD:0B}",
    "spring.mail.properties.mail.smtp.connectiontimeout": "${MAIL_SMTP_CONNECTION_TIMEOUT:5000}",
    "spring.mail.properties.mail.smtp.timeout": "${MAIL_SMTP_TIMEOUT:10000}",
    "spring.mail.properties.mail.smtp.writetimeout": "${MAIL_SMTP_WRITE_TIMEOUT:10000}",
    "app.storage.mutation-admission.enabled": "${APP_STORAGE_MUTATION_ADMISSION_ENABLED:true}",
    "app.storage.mutation-admission.max-concurrent": "${APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT:2}",
    "app.storage.mutation-admission.max-concurrent-per-ip": "${APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT_PER_IP:1}",
    "app.storage.mutation-admission.retry-after-seconds": "${APP_STORAGE_MUTATION_ADMISSION_RETRY_AFTER_SECONDS:2}",
}


class AuditError(Exception):
    """Carries a fixed diagnostic identifier, never input text."""


def unique_object(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise AuditError("CONTRACT_JSON")
        result[key] = value
    return result


def reject_constant(value: str) -> None:
    raise AuditError("CONTRACT_JSON")


def string_set(value: Any) -> frozenset[str] | None:
    if not isinstance(value, list) or any(not isinstance(v, str) for v in value):
        return None
    return frozenset(value) if len(value) == len(set(value)) else None


def no_links(path: Path) -> None:
    """Reject symlinks/reparse points, including ancestors, before opening files."""
    for part in (path, *path.parents):
        info = part.lstat()
        if stat.S_ISLNK(info.st_mode) or getattr(info, "st_file_attributes", 0) & 0x400:
            raise AuditError("FILE_LINK")


def read_text(root: Path, relative: str, *, template: bool = True) -> str:
    if relative not in TEMPLATE_PATHS | {SOURCE}:
        raise AuditError("FILE_ALLOWLIST")
    path = root / relative
    try:
        no_links(path)
        if not path.is_file():
            raise AuditError("FILE_REGULAR")
        with path.open("rb") as stream:
            raw = stream.read(MAX_BYTES + 1)
        if len(raw) > MAX_BYTES:
            raise AuditError("FILE_SIZE")
        value = raw.decode("utf-8")
    except (OSError, UnicodeError):
        raise AuditError("FILE_READ_UTF8") from None
    if not value.endswith("\n") or "\r" in value.replace("\r\n", ""):
        raise AuditError("FILE_NEWLINE")
    if "\ufeff" in value or any(ord(c) < 32 and c not in "\r\n\t" for c in value):
        raise AuditError("FILE_CONTROL")
    if template and (not value.isascii() or "\x7f" in value):
        raise AuditError("FILE_TEMPLATE_ASCII")
    if template and any(line.rstrip() != line for line in value.splitlines()):
        raise AuditError("FILE_TRAILING_SPACE")
    return value


def parse_env(text: str) -> dict[str, str]:
    """Accept this example's single-line literal subset, never shell evaluation."""
    result: dict[str, str] = {}
    for line in text.splitlines():
        if not line or line.startswith("#"):
            continue
        match = re.fullmatch(r"([A-Z][A-Z0-9_]*)=(.*)", line)
        if not match or match[1] in result:
            raise AuditError("ENV_SYNTAX_DUPLICATE")
        key, value = match.groups()
        if value.startswith(('"', "'")):
            if len(value) < 2 or value[-1] != value[0]:
                raise AuditError("ENV_SYNTAX_DUPLICATE")
            value = value[1:-1]
        if re.search(r"[\s'\"\\$`#;]", value):
            raise AuditError("ENV_SYNTAX_DUPLICATE")
        result[key] = value
    return result


def active_shape(text: str) -> str:
    lines = (line.strip() for line in text.splitlines())
    body = "\n".join(line for line in lines if line and not line.startswith("#"))
    return hashlib.sha256(body.encode("ascii")).hexdigest()


@dataclass
class Directive:
    words: tuple[str, ...]
    children: list[Directive] | None = None


def parse_nginx(text: str) -> list[Directive]:
    """Parse the candidate's bounded directive/block subset, not native syntax."""
    lexer = shlex.shlex(text, posix=True, punctuation_chars="{};")
    lexer.whitespace_split = True
    lexer.escape = ""
    try:
        tokens = [part for token in lexer for part in
                  (list(token) if token and set(token) <= set("{};") else [token])]
    except ValueError:
        raise AuditError("NGINX_STRUCTURE") from None
    index = 0

    def block(depth: int) -> list[Directive]:
        nonlocal index
        if depth > 20:
            raise AuditError("NGINX_STRUCTURE")
        result: list[Directive] = []
        words: list[str] = []
        while index < len(tokens):
            token = tokens[index]
            index += 1
            if token == "}":
                if words or not depth:
                    raise AuditError("NGINX_STRUCTURE")
                return result
            if token in (";", "{"):
                if not words:
                    raise AuditError("NGINX_STRUCTURE")
                result.append(Directive(tuple(words), block(depth + 1) if token == "{" else None))
                words = []
            else:
                words.append(token)
        if depth or words:
            raise AuditError("NGINX_STRUCTURE")
        return result

    return block(0)


def directive_values(nodes: list[Directive], name: str) -> list[tuple[str, ...]]:
    return [node.words[1:] for node in nodes if node.words[0] == name and node.children is None]


def nginx_structure_regressions(main: str, https: str, proxy: str, acl: str) -> list[str]:
    """Check scope, inheritance and maps independently of identity hashes."""
    try:
        roots = parse_nginx(main)
        sites = parse_nginx(https)
        snippet = parse_nginx(proxy)
        access = parse_nginx(acl)
    except AuditError as error:
        return [str(error)]
    http = [node.children for node in roots if node.words == ("http",)]
    servers = [node.children for node in sites if node.words == ("server",)
               and node.children and directive_values(node.children, "ssl_certificate")]
    if len(http) != 1 or http[0] is None or len(servers) != 1:
        return ["NGINX_STRUCTURE"]
    context, server = http[0], servers[0]
    failures: list[str] = []

    def enforcement_disabled(nodes: list[Directive]) -> bool:
        return any(
            (node.words[0] in ("limit_conn_dry_run", "limit_req_dry_run")
             and node.words[1:] != ("off",)) or
            (node.words[0] in ("limit_conn_status", "limit_req_status")
             and node.words[1:] != ("429",)) or
            (node.words[0] == "limit_rate_after" and node.words[1:] != ("0",)) or
            (node.children is not None and enforcement_disabled(node.children))
            for node in nodes)

    if enforcement_disabled(roots + sites + snippet):
        failures.append("NGINX_ENFORCEMENT")
    maps = {
        ("map", "$uri", "$ats_download_scope"): [("default", ""), ("~" + DOWNLOAD_PATTERN, "original")],
        ("map", "$ats_download_scope", "$ats_download_client"): [("default", ""), ("original", "$binary_remote_addr")],
        ("map", "$ats_download_scope", "$ats_media_rate"): [("default", "256k"), ("original", "2m")],
        ("map", "$http_content_type", "$ats_multipart_scope"): [("default", ""), ("~*^multipart/", "multipart")],
        ("map", "$ats_multipart_scope", "$ats_multipart_client"): [("default", ""), ("multipart", "$binary_remote_addr")],
    }
    actual_maps = [node for node in context if node.words[0] == "map"]
    if len(actual_maps) != len(maps) or any(
        len([node for node in actual_maps if node.words == words and node.children is not None
             and [child.words for child in node.children] == entries
             and all(child.children is None for child in node.children)]) != 1
        for words, entries in maps.items()
    ):
        failures.append("NGINX_MAPS")
    zones = {
        ("$binary_remote_addr", "zone=ats_client:10m"),
        ("$server_name", "zone=ats_server:1m"),
        ("$binary_remote_addr", "zone=ats_media_client:10m"),
        ("$server_name", "zone=ats_media_server:1m"),
        ("$ats_download_scope", "zone=ats_download_server:1m"),
        ("$ats_download_client", "zone=ats_download_client:10m"),
        ("$ats_multipart_scope", "zone=ats_multipart_server:1m"),
        ("$ats_multipart_client", "zone=ats_multipart_client:10m"),
    }
    if Counter(directive_values(context, "limit_conn_zone")) != Counter(zones):
        failures.append("NGINX_ZONES")
    if directive_values(context, "limit_conn_status") != [("429",)] or \
            directive_values(context, "limit_req_status") != [("429",)]:
        failures.append("NGINX_REJECTION_STATUS")
    required_proxy = {
        "proxy_pass": [("http://127.0.0.1:8080",)],
        "proxy_pass_request_headers": [("off",)],
        "proxy_request_buffering": [("off",)], "proxy_buffering": [("off",)],
        "proxy_cache": [("off",)], "proxy_store": [("off",)],
        "proxy_ignore_headers": [("X-Accel-Redirect", "X-Accel-Buffering")],
    }
    if any(directive_values(snippet, key) != value for key, value in required_proxy.items()) or \
            [v for v in directive_values(snippet, "proxy_set_header")
             if v and v[0].lower() == "x-atstudio-client-ip"] != [
                 ("X-ATStudio-Client-IP", "$remote_addr")]:
        failures.append("NGINX_PROXY_SAFETY")
    if [node.words for node in access] != [("satisfy", "all"), ("deny", "all")]:
        failures.append("NGINX_INVITATION_DEFAULT")
    inherited = directive_values(server, "limit_conn")
    if Counter(inherited) != Counter(GENERAL_LIMITS):
        failures.append("NGINX_EFFECTIVE_LIMITS")
    media_count = 0
    proxy_count = 0
    for location in server:
        if location.words[0] != "location" or location.children is None:
            continue
        entries = location.children
        if ("/etc/atstudio/nginx/proxy.conf",) not in directive_values(entries, "include"):
            continue
        proxy_count += 1
        media = location.words == ("location", "~", MEDIA_PATTERN)
        media_count += int(media)
        expanded = entries + snippet
        if any(directive_values(expanded, key) != value for key, value in required_proxy.items()):
            failures.append("NGINX_PROXY_SAFETY")
        limits = directive_values(expanded, "limit_conn") or inherited
        if Counter(limits) != Counter(MEDIA_LIMITS if media else GENERAL_LIMITS):
            failures.append("NGINX_EFFECTIVE_LIMITS")
        rates = [("zone=ats_api", "burst=40", "nodelay")]
        if media:
            rates.append(("zone=ats_media", "burst=8", "nodelay"))
        if Counter(directive_values(expanded, "limit_req")) != Counter(rates):
            failures.append("NGINX_EFFECTIVE_RATES")
    if media_count != 1 or proxy_count != 4:
        failures.append("NGINX_EFFECTIVE_LOCATIONS")
    protected = [node for node in server if node.words == (
        "location", "~*", r"^/uploads/(\.staging|tracks/audio|company-docs|questions|notices)(/|$)")]
    if len(protected) != 1 or not protected[0].children or \
            [n.words for n in protected[0].children] != [("return", "404")]:
        failures.append("NGINX_PROTECTED_ROUTES")
    if ("/etc/atstudio/nginx/restricted-access.conf",) not in directive_values(server, "include"):
        failures.append("NGINX_INVITATION_DEFAULT")

    def unsafe_route(nodes: list[Directive]) -> bool:
        return any(node.words[0] == "alias" or
                   node.words[:3] == ("location", "^~", "/uploads/") or
                   (node.children is not None and unsafe_route(node.children)) for node in nodes)

    if unsafe_route(sites):
        failures.append("NGINX_PROTECTED_ROUTES")
    return sorted(set(failures))


def resource_regressions(service: str, mysql: str) -> list[str]:
    """Check section and duplicate-sensitive resource directives."""
    expected = {
        "service": {
            "ExecStart": "/usr/bin/java -Xms256m -Xmx1024m -XX:MaxMetaspaceSize=256m "
                         "-XX:+ExitOnOutOfMemoryError -Djava.io.tmpdir=/var/lib/atstudio/tmp "
                         "-Duser.timezone=Asia/Seoul -jar /opt/atstudio/current/app.jar",
            "MemoryHigh": "2G", "MemoryMax": "3G", "TasksMax": "256", "LimitNOFILE": "4096",
            "KillMode": "control-group", "User": "atstudio", "Type": "simple",
        },
        "mysql": {"innodb-buffer-pool-size": "512M", "max-connections": "30",
                  "bind-address": "127.0.0.1", "mysqlx": "OFF"},
    }
    for kind, text, target in (("service", service, "[Service]"), ("mysql", mysql, "[mysqld]")):
        values: dict[str, list[str]] = {}
        section = ""
        for line in text.splitlines():
            line = line.strip()
            if not line or line.startswith(("#", ";")):
                continue
            if line.startswith("["):
                section = line
            elif section == target and "=" in line:
                key, value = line.split("=", 1)
                values.setdefault(key, []).append(value)
        if any(values.get(key) != [value] for key, value in expected[kind].items()):
            return ["RESOURCE_BASELINE"]
    return []


def source_binding_regressions(text: str) -> list[str]:
    """Inspect the source's plain indented mapping subset; not a YAML runtime proof."""
    parents: list[tuple[int, str]] = []
    entries: dict[str, list[str]] = {}
    for line in text.splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        indent = len(line) - len(line.lstrip(" "))
        while parents and parents[-1][0] >= indent:
            parents.pop()
        match = re.fullmatch(r" *([a-zA-Z0-9_.-]+):(?: +(.*))?", line)
        if not match:
            continue
        key, value = match.groups()
        path = ".".join([p[1] for p in parents] + [key])
        entries.setdefault(path, []).append(value or "")
        if not value:
            parents.append((indent, key))
    failures: list[str] = []
    for path, expected in SOURCE_BINDINGS.items():
        ancestors = [key for key in entries if path.startswith(key + ".")]
        if entries.get(path) != [expected] or any(len(entries[key]) != 1 for key in ancestors):
            failures.append("SOURCE_BINDING." + path)
    return failures


def nginx_regressions(text: str) -> list[str]:
    """Synthetic cases on two known regexes and flat blocks, not Nginx parsing."""
    lines = [line.strip() for line in text.splitlines()
             if line.strip() and not line.lstrip().startswith("#")]
    media: list[tuple[str, list[str]]] = []
    spa: list[list[str]] = []
    for index, line in enumerate(lines):
        match = re.fullmatch(r'location ~ (?:"([^"\r\n]+)"|([^\s"]+)) \{', line)
        if line != "location / {" and not match:
            continue
        body: list[str] = []
        for entry in lines[index + 1:]:
            if entry == "}" or "{" in entry:
                break
            body.append(entry)
        if match:
            pattern = match[1] or match[2]
            if "stream" in pattern or "download" in pattern:
                media.append((pattern, body))
        elif "try_files $uri $uri/ /index.html;" in body:
            spa.append(body)
    failures: list[str] = []
    if len(media) != 1:
        failures.append("NGINX_MEDIA_BLOCK")
    else:
        pattern, body = media[0]
        # Do not execute arbitrary input regexes (including potentially slow ones).
        if pattern not in (MEDIA_PATTERN, LEGACY_MEDIA_PATTERN):
            failures.append("NGINX_MEDIA_UNSUPPORTED")
        elif any(bool(re.search(pattern, path)) != wanted for path, wanted in MEDIA_CASES.items()):
            failures.append("NGINX_MEDIA_CASES")
        if not {
            "include /etc/atstudio/nginx/proxy.conf;",
            "limit_req zone=ats_media burst=8 nodelay;", "limit_rate $ats_media_rate;",
            *("limit_conn " + " ".join(pair) + ";" for pair in MEDIA_LIMITS),
        }.issubset(body):
            failures.append("NGINX_MEDIA_LIMITS")
    if len(spa) != 1 or [line for line in spa[0] if line.startswith("add_header X-Frame-Options ")] != [
        "add_header X-Frame-Options DENY always;"
    ]:
        failures.append("NGINX_SPA_FRAME")
    return failures


def check_contract(contract: Any) -> None:
    if not isinstance(contract, dict):
        raise AuditError("CONTRACT_STRUCTURE")
    if type(contract.get("schema_version")) is not int or contract["schema_version"] != 1:
        raise AuditError("CONTRACT_VERSION")
    if contract.get("path_base") != "repository_root":
        raise AuditError("CONTRACT_PATHS")
    paths = string_set(contract.get("required_template_paths"))
    if paths is None or any(
        PurePosixPath(p).is_absolute() or ".." in PurePosixPath(p).parts
        or "\\" in p or ":" in p or p not in TEMPLATE_PATHS for p in paths
    ) or paths != TEMPLATE_PATHS:
        raise AuditError("CONTRACT_PATHS")
    if string_set(contract.get("required_env")) != frozenset(EXPECTED_ENV):
        raise AuditError("CONTRACT_ENV_KEYS")
    if contract.get("safe_literals") != SAFE or string_set(contract.get("must_be_empty")) != EMPTY:
        raise AuditError("CONTRACT_SAFETY")
    if contract.get("deployment_roots") != ROOTS:
        raise AuditError("CONTRACT_ROOTS")
    placeholders = contract.get("required_placeholders")
    if not isinstance(placeholders, dict) or set(placeholders) != set(PLACEHOLDERS):
        raise AuditError("CONTRACT_PLACEHOLDERS")
    for token, keys in PLACEHOLDERS.items():
        item = placeholders[token]
        if not isinstance(item, dict) or string_set(item.get("env")) != frozenset(keys):
            raise AuditError("CONTRACT_PLACEHOLDERS")
        if not isinstance(item.get("kind"), str) or not item["kind"]:
            raise AuditError("CONTRACT_PLACEHOLDERS")
    nginx = contract.get("nginx")
    if not isinstance(nginx, dict) or any(nginx.get(key) != expected for key, expected in {
        "upstream": "http://127.0.0.1:8080", "proxy_uri_suffix": "",
        "client_identity_header": "X-ATStudio-Client-IP",
        "client_identity_value": "$remote_addr", "restricted_default": "deny all;",
        "forward_request_headers": "explicit_allowlist_only", "client_max_body_size": "120m",
        "media_location_pattern": MEDIA_PATTERN,
        "download_location_pattern": DOWNLOAD_PATTERN,
        "connection_limits": {"general": {"per_ip": 8, "global": 32},
                              "media": {"per_ip": 4, "global": 16},
                              "download": {"per_ip": 1, "global": 2},
                              "multipart": {"per_ip": 1, "global": 2}},
        "transfer_rates": {"stream": "256k", "download": "2m"},
        "multipart_classification": "case-insensitive multipart/ prefix on Content-Type; all paths and methods; pre-body/pre-auth",
        "spa_response_headers": {"X-Content-Type-Options": "nosniff", "Referrer-Policy": "no-referrer", "X-Frame-Options": "DENY"},
        "private_upload_prefixes": [".staging", "tracks/audio", "company-docs", "questions", "notices"],
        "include_targets": {
            "/etc/atstudio/nginx/active-site.conf": [BASE + "nginx/http-bootstrap.conf.template", BASE + "nginx/https.conf.template"],
            "/etc/atstudio/nginx/proxy.conf": [BASE + "nginx/proxy.conf.template"],
            "/etc/atstudio/nginx/restricted-access.conf": [BASE + "nginx/restricted-access.conf"],
        },
    }.items()):
        raise AuditError("CONTRACT_NGINX")
    gates = contract.get("target_gates")
    if not isinstance(gates, list) or len(gates) != len(GATES) or any(
        not isinstance(gate, dict) or gate.get("status") != expected[1]
        or not isinstance(gate.get("gate"), str) or not gate["gate"]
        for gate, expected in zip(gates, GATES)
    ):
        raise AuditError("CONTRACT_GATES")


def validate(root: Path) -> dict[str, Any]:
    errors: list[dict[str, str]] = []
    result: dict[str, Any] = {
        "mode": "template-audit-only", "template_status": "FAIL",
        "deployment_ready": False, "production_status": "HOLD",
        "target_gates": [{"rule": rule, "status": status} for rule, status in GATES],
        "errors": errors,
    }

    def fail(rule: str, path: str) -> None:
        errors.append({"rule": rule, "path": path})

    # Contract paths are data, not filesystem instructions. Only fixed paths open.
    try:
        if str(root).startswith(("\\\\", "//")):
            raise AuditError("ROOT_LOCAL_ONLY")
        root = root.absolute()
        no_links(root)
        if not root.is_dir():
            raise AuditError("ROOT_DIRECTORY")
    except (OSError, ValueError, AuditError):
        fail("ROOT_LOCAL_DIRECTORY", "<root>")
        return result
    try:
        contract = json.loads(read_text(root, CONTRACT), object_pairs_hook=unique_object,
                              parse_constant=reject_constant)
        check_contract(contract)
    except (ValueError, RecursionError):
        fail("CONTRACT_JSON", CONTRACT)
        return result
    except AuditError as error:
        fail(str(error), CONTRACT)
        return result
    texts: dict[str, str] = {}
    for path in sorted(TEMPLATE_PATHS - {CONTRACT}):
        try:
            texts[path] = read_text(root, path)
        except AuditError as error:
            fail(str(error), path)
    if ENV in texts:
        try:
            entries = parse_env(texts[ENV])
            if set(entries) != set(EXPECTED_ENV):
                fail("ENV_KEYS", ENV)
            for key, expected in EXPECTED_ENV.items():
                if entries.get(key) != expected:
                    fail("ENV_LITERAL." + key, ENV)
        except AuditError as error:
            fail(str(error), ENV)
    for relative, expected in SHAPES.items():
        path = BASE + relative
        if path in texts and active_shape(texts[path]) != expected:
            fail("TEMPLATE_IDENTITY_DRIFT", path)
    https = BASE + "nginx/https.conf.template"
    if https in texts:
        for rule in nginx_regressions(texts[https]):
            fail(rule, https)
    nginx_paths = [BASE + "nginx/" + name for name in (
        "nginx.conf.template", "https.conf.template", "proxy.conf.template", "restricted-access.conf")]
    if all(path in texts for path in nginx_paths):
        for rule in nginx_structure_regressions(*(texts[path] for path in nginx_paths)):
            fail(rule, https)
    resource_paths = [BASE + "systemd/atstudio.service", BASE + "mysql/atstudio.cnf"]
    if all(path in texts for path in resource_paths):
        for rule in resource_regressions(*(texts[path] for path in resource_paths)):
            fail(rule, resource_paths[0])
    try:
        source = read_text(root, SOURCE, template=False)
        active = "\n".join(line for line in source.splitlines() if not line.lstrip().startswith("#"))
        references = set(re.findall(r"\$\{([A-Z][A-Z0-9_]*)(?=[:}])", active))
        for key in sorted(set(EXPECTED_ENV) - EXTERNAL_BINDINGS - references):
            fail("SOURCE_REFERENCE." + key, SOURCE)
        for rule in source_binding_regressions(source):
            fail(rule, SOURCE)
    except AuditError as error:
        fail(str(error), SOURCE)
    result["template_status"] = "PASS" if not errors else "FAIL"
    return result


class SafeParser(argparse.ArgumentParser):
    def error(self, message: str) -> None:
        self.exit(2, "ERROR CLI_ARGUMENTS; values suppressed; production HOLD\n")


def main(argv: list[str] | None = None) -> int:
    parser = SafeParser(description="Offline unrendered-template audit; never deployment approval.")
    parser.add_argument("--root", type=Path, default=Path(__file__).absolute().parents[2],
                        help="Local repository root or synthetic bundle with the same relative layout.")
    parser.add_argument("--json", action="store_true", help="Emit sanitized machine-readable results.")
    args = parser.parse_args(argv)
    result = validate(args.root)
    if args.json:
        print(json.dumps(result, indent=2))
    else:
        print("TEMPLATE " + result["template_status"] + "; DEPLOYMENT NOT READY; PRODUCTION HOLD")
        print("Unresolved placeholders required. Static regression audit, not native syntax/runtime proof.")
        for gate in result["target_gates"]:
            print(gate["status"] + " " + gate["rule"])
        for error in result["errors"]:
            print("FAIL " + error["rule"] + " " + error["path"])
    return 1 if result["errors"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
