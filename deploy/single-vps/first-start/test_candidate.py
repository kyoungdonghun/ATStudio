"""Offline public-candidate checks only; no Spring, systemd, sockets or writes."""

import ipaddress
from pathlib import Path
import re
import shlex
import unittest


HERE = Path(__file__).resolve().parent
OFF = (
    "app.background-tasks.enabled",
    "app.audio.worker-enabled",
    "app.auth.password-login.enabled",
    "app.acceptance.enabled",
    "app.bootstrap.test-users.enabled",
    "app.payment.operations.reconciliation-notification-enabled",
)
EMPTY = (
    "oauth2.google.client-id", "oauth2.google.client-secret",
    "oauth2.kakao.client-id", "oauth2.kakao.client-secret",
    "oauth2.naver.client-id", "oauth2.naver.client-secret",
    "app.payment.toss.client-key", "app.payment.toss.secret-key",
    "spring.mail.username", "spring.mail.password",
)
ENVIRONMENT_FILES = [
    "/etc/atstudio/__EXISTING_APPROVED_PRIVATE_ENV__",
    "/etc/atstudio/first-start-public.env",
]
JAVA_ARGS = [
    "/usr/bin/java", "-Xms256m", "-Xmx1024m", "-XX:MaxMetaspaceSize=256m",
    "-XX:+ExitOnOutOfMemoryError", "-Djava.io.tmpdir=/var/lib/atstudio/tmp",
    "-Duser.timezone=Asia/Seoul", "-jar", "__APP_JAR__",
]
DIRECTIVES = {
    "Unit": set("Description After StartLimitIntervalSec StartLimitBurst".split()),
    "Service": set((
        "Type User Group WorkingDirectory EnvironmentFile UnsetEnvironment ExecStartPre "
        "ExecStart Restart RuntimeMaxSec RuntimeRandomizedExtraSec TimeoutStartSec "
        "TimeoutStopSec KillMode SendSIGKILL UMask LimitCORE LimitNOFILE TasksMax "
        "MemoryHigh MemoryMax NoNewPrivileges ProtectSystem ProtectHome PrivateTmp "
        "PrivateDevices ProtectKernelTunables ProtectKernelModules ProtectControlGroups "
        "RestrictSUIDSGID RestrictAddressFamilies CapabilityBoundingSet ReadOnlyPaths "
        "ReadWritePaths IPAddressDeny IPAddressAllow StandardOutput StandardError "
        "SyslogIdentifier"
    ).split()),
}
FIXED_CLI = {
    "spring.config.location": "classpath:/application.yml",
    "spring.config.additional-location": "", "spring.config.import": "",
    "spring.profiles.active": "prod", "spring.profiles.include": "",
    "server.address": "127.0.0.1", "server.port": "8080",
    "server.forward-headers-strategy": "none", "server.tomcat.accesslog.enabled": "false",
    "app.bootstrap.test-users.default-password": "",
    "app.payment.operations.operator-email": "",
    "spring.sql.init.mode": "never", "spring.jpa.hibernate.ddl-auto": "validate",
    "spring.jpa.show-sql": "false", "app.storage.require-explicit-roots": "true",
    "app.storage.public-path": "/srv/atstudio/public",
    "app.storage.private-path": "/srv/atstudio/private",
    "app.storage.integrity.audit-on-startup": "true",
    "app.storage.integrity.strict-on-startup": "true",
    "app.storage.mutation-admission.enabled": "true",
    "app.storage.mutation-admission.max-concurrent": "2",
    "app.storage.mutation-admission.max-concurrent-per-ip": "1",
    "app.storage.mutation-admission.retry-after-seconds": "2",
    "app.security.rate-limit.enabled": "true",
    "app.security.trusted-client-identity.enabled": "false",
    "spring.datasource.hikari.maximum-pool-size": "10",
    "spring.datasource.hikari.minimum-idle": "2",
    "spring.datasource.hikari.connection-timeout": "5000",
    "spring.servlet.multipart.location": "/var/lib/atstudio/tmp",
    "spring.servlet.multipart.file-size-threshold": "0B",
    "spring.mail.host": "127.0.0.1", "spring.mail.port": "1",
    "spring.mail.test-connection": "false",
    "spring.mail.properties.mail.smtp.auth": "false",
    "spring.mail.properties.mail.smtp.starttls.enable": "false",
    "app.mail.from": "disabled@example.invalid",
    "springdoc.api-docs.enabled": "false", "springdoc.swagger-ui.enabled": "false",
    "spring.mvc.log-request-details": "false",
    "logging.level.org.hibernate.SQL": "off",
    "logging.level.org.hibernate.orm.jdbc.bind": "off",
}
UnitOccurrences = dict[str, dict[str, list[str]]]


def parse_unit(text: str) -> UnitOccurrences:
    """Preserve occurrences; accept plain directives and ExecStart continuations only."""
    unit: UnitOccurrences = {}
    section = ""
    pending = ""
    for raw in text.splitlines():
        line = raw.strip()
        if pending:
            if not raw.startswith("    --"):
                raise ValueError("unsupported continuation")
            line = pending + line
        elif not line or line.startswith("#"):
            continue
        elif raw != line:
            raise ValueError("unsupported indentation")
        if line.endswith("\\"):
            if not line.startswith("ExecStart="):
                raise ValueError("unsupported continuation directive")
            pending = line[:-1] + " "
            continue
        pending = ""
        if line in ("[Unit]", "[Service]", "[Install]"):
            section = line[1:-1]
            if section in unit:
                raise ValueError("repeated section")
            unit[section] = {}
            continue
        if not section or any(char in line for char in "\\$%`\"';#"):
            raise ValueError("unsupported syntax or expansion")
        key, separator, value = line.partition("=")
        if not separator or not re.fullmatch(r"[A-Za-z][A-Za-z0-9]*", key):
            raise ValueError("invalid directive")
        if any(ord(char) < 32 or ord(char) > 126 for char in value):
            raise ValueError("unsupported character")
        unit[section].setdefault(key, []).append(value)
    if pending:
        raise ValueError("unfinished continuation")
    return unit


def candidate_unit(text: str) -> dict[str, dict[str, str]]:
    """Project to scalars only after checking every occurrence and ordered env file."""
    occurrences = parse_unit(text)
    if set(occurrences) != set(DIRECTIVES):
        raise ValueError("unexpected sections")
    for section, keys in DIRECTIVES.items():
        if set(occurrences[section]) != keys:
            raise ValueError("missing or unapproved directive")
        for key, values in occurrences[section].items():
            if key == "EnvironmentFile":
                if values != ENVIRONMENT_FILES:
                    raise ValueError("unexpected environment files or order")
            elif len(values) != 1:
                raise ValueError("unapproved repeated directive")
    unit = {
        section: {key: values[0] for key, values in entries.items()
                  if key != "EnvironmentFile"}
        for section, entries in occurrences.items()
    }
    if unit["Service"]["ExecStartPre"] != "/usr/bin/false":
        raise ValueError("unexpected prestart command")
    cli_values(unit)
    return unit


def cli_values(unit: dict[str, dict[str, str]]) -> dict[str, str]:
    """Parse explicit public Spring properties without expanding environment."""
    values = {}
    args = shlex.split(unit["Service"]["ExecStart"])
    if args[:len(JAVA_ARGS)] != JAVA_ARGS:
        raise ValueError("unexpected Java executable or JVM arguments")
    for token in args[len(JAVA_ARGS):]:
        if not token.startswith("--") or "=" not in token:
            raise ValueError("unexpected application argument")
        key, value = token[2:].split("=", 1)
        if key in values:
            raise ValueError("duplicate property")
        values[key] = value
    expected = FIXED_CLI | {key: "false" for key in OFF} | {key: "" for key in EMPTY}
    if values != expected:
        raise ValueError("unexpected application properties")
    return values


def assert_off_contract(values: dict[str, str]) -> None:
    """Reject missing or unsafe explicit overrides."""
    for key in OFF:
        if values.get(key) != "false":
            raise ValueError("missing OFF override")
    for key in EMPTY:
        if key not in values or values[key] != "":
            raise ValueError("missing blank credential override")
    for key in ("spring.mail.host", "spring.mail.port", "app.storage.public-path",
                "app.storage.private-path", "spring.servlet.multipart.location"):
        if values.get(key) != FIXED_CLI[key]:
            raise ValueError("unsafe SMTP or filesystem target")


class CandidateTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.text = (HERE / "atstudio-first-start.service.candidate").read_text(
            encoding="utf-8"
        )
        cls.unit = candidate_unit(cls.text)
        cls.cli = cli_values(cls.unit)
        lines = (HERE / "first-start-public.env.example").read_text(
            encoding="utf-8"
        ).splitlines()
        cls.env_lines = [line for line in lines if line and not line.startswith("#")]
        cls.env = dict(line.split("=", 1) for line in cls.env_lines)

    def test_unconditional_hold_and_no_automatic_install(self) -> None:
        self.assertEqual(self.unit["Service"]["ExecStartPre"], "/usr/bin/false")
        self.assertNotIn("Install", self.unit)
        self.assertEqual(self.unit["Unit"]["StartLimitBurst"], "1")
        self.assertNotIn("Requires", self.unit["Unit"])

    def test_exact_new_artifact_placeholder_no_old_fallback(self) -> None:
        args = shlex.split(self.unit["Service"]["ExecStart"])
        self.assertEqual(args[args.index("-jar") + 1], "__APP_JAR__")
        self.assertNotIn("current/app.jar", self.text)
        self.assertNotIn("build/libs", self.text)

    def test_off_flags_and_credentials_are_cli_fixed(self) -> None:
        assert_off_contract(self.cli)

    def test_each_missing_off_flag_is_rejected(self) -> None:
        for key in OFF:
            with self.subTest(key=key):
                altered = self.cli.copy()
                altered.pop(key)
                with self.assertRaises(ValueError):
                    assert_off_contract(altered)

    def test_each_reenabled_flag_or_provider_key_is_rejected(self) -> None:
        for key in (*OFF, *EMPTY, "spring.mail.port"):
            with self.subTest(key=key):
                altered = self.cli | {key: "synthetic-unsafe"}
                with self.assertRaises(ValueError):
                    assert_off_contract(altered)

    def test_static_precedence_model_over_poisoned_environment(self) -> None:
        lower = {key: "true" for key in OFF}
        lower.update({key: "SYNTHETIC_NOT_A_SECRET" for key in EMPTY})
        lower["spring.mail.port"] = "__SMTP_PORT__"
        effective = lower | self.cli
        assert_off_contract(effective)
        self.assertEqual(int(effective["spring.mail.port"]), 1)

    def test_public_env_unique_and_critical_aliases_off(self) -> None:
        self.assertEqual(len(self.env_lines), len(self.env))
        for key in ("APP_BACKGROUND_TASKS_ENABLED", "APP_AUDIO_WORKER_ENABLED",
                    "APP_AUTH_PASSWORD_LOGIN_ENABLED", "APP_ACCEPTANCE_ENABLED",
                    "APP_BOOTSTRAP_TEST_USERS_ENABLED",
                    "PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED"):
            self.assertEqual(self.env[key], "false", key)
        self.assertEqual(self.env["MAIL_PORT"], "1")
        for key in ("JWT_SECRET", "SPRING_DATASOURCE_PASSWORD",
                    "PAYMENT_BILLING_KEY_0_SECRET"):
            self.assertNotIn(key, self.env)

    def test_guard_settings_are_not_weakened(self) -> None:
        for key in ("app.storage.require-explicit-roots",
                    "app.storage.integrity.audit-on-startup",
                    "app.storage.integrity.strict-on-startup",
                    "app.storage.mutation-admission.enabled"):
            self.assertEqual(self.cli[key], "true")
        self.assertEqual(self.cli["spring.jpa.hibernate.ddl-auto"], "validate")
        self.assertEqual(self.cli["spring.sql.init.mode"], "never")
        self.assertEqual(self.cli["spring.profiles.active"], "prod")
        self.assertNotIn("jwt.secret", self.cli)
        self.assertFalse(any("encryption-keys" in key for key in self.cli))

    def test_listener_and_network_shape(self) -> None:
        self.assertEqual(self.cli["server.address"], "127.0.0.1")
        self.assertEqual(self.cli["server.port"], "8080")
        service = self.unit["Service"]
        self.assertEqual(service["IPAddressDeny"], "any")
        self.assertEqual(service["IPAddressAllow"].split(), ["127.0.0.1/32", "::1/128"])
        allowed = [ipaddress.ip_network(item) for item in service["IPAddressAllow"].split()]
        for literal in ("192.0.2.1", "2001:db8::1", "127.0.0.53"):
            address = ipaddress.ip_address(literal)
            self.assertFalse(any(address in network for network in allowed))

    def test_no_restart_finite_limits_and_child_cleanup(self) -> None:
        service = self.unit["Service"]
        for key, value in {"Restart": "no", "RuntimeMaxSec": "180s",
                           "RuntimeRandomizedExtraSec": "0", "TimeoutStartSec": "30s",
                           "TimeoutStopSec": "30s", "KillMode": "control-group",
                           "SendSIGKILL": "yes"}.items():
            self.assertEqual(service[key], value)

    def test_existing_resource_and_sandbox_baseline_retained(self) -> None:
        baseline = parse_unit((HERE.parent / "systemd/atstudio.service").read_text(
            encoding="utf-8"
        ))["Service"]
        for key in ("User", "Group", "MemoryHigh", "MemoryMax", "TasksMax",
                    "LimitCORE", "LimitNOFILE", "UMask", "NoNewPrivileges",
                    "ProtectSystem", "ProtectHome", "PrivateTmp", "PrivateDevices",
                    "ProtectKernelTunables", "ProtectKernelModules", "ProtectControlGroups",
                    "RestrictSUIDSGID", "RestrictAddressFamilies", "CapabilityBoundingSet",
                    "ReadOnlyPaths", "ReadWritePaths"):
            self.assertEqual([self.unit["Service"][key]], baseline[key], key)
        for arg in ("-Xms256m", "-Xmx1024m", "-XX:MaxMetaspaceSize=256m"):
            self.assertIn(arg, shlex.split(self.unit["Service"]["ExecStart"]))

    def test_configuration_injection_is_removed_and_no_secret_cli(self) -> None:
        unset = self.unit["Service"]["UnsetEnvironment"].split()
        for key in ("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS",
                    "SPRING_APPLICATION_JSON", "SPRING_CONFIG_IMPORT",
                    "SPRING_CONFIG_ADDITIONAL_LOCATION", "LD_PRELOAD",
                    "GOOGLE_CLIENT_SECRET", "KAKAO_CLIENT_SECRET", "NAVER_CLIENT_SECRET",
                    "TOSS_SECRET_KEY", "MAIL_PASSWORD"):
            self.assertIn(key, unset)
        self.assertEqual(self.cli["spring.config.location"], "classpath:/application.yml")
        self.assertEqual(self.cli["spring.config.import"], "")
        self.assertEqual(self.cli["spring.config.additional-location"], "")
        self.assertNotIn("spring.datasource.password", self.cli)
        self.assertIn("__EXISTING_APPROVED_PRIVATE_ENV__", self.text)

    def test_parser_preserves_ordered_occurrences(self) -> None:
        parsed = parse_unit(self.text)
        self.assertEqual(parsed["Service"]["EnvironmentFile"], ENVIRONMENT_FILES)
        additive = parse_unit("[Service]\nIPAddressAllow=any\nIPAddressAllow=127.0.0.1\n")
        self.assertEqual(additive["Service"]["IPAddressAllow"], ["any", "127.0.0.1"])

    def test_every_singleton_duplicate_and_reset_is_rejected(self) -> None:
        for section, entries in parse_unit(self.text).items():
            for key in entries:
                if key == "EnvironmentFile":
                    continue
                with self.subTest(section=section, key=key):
                    altered = self.text.replace(key + "=", key + "=\n" + key + "=", 1)
                    with self.assertRaises(ValueError):
                        candidate_unit(altered)

    def test_environment_files_require_exact_order_and_count(self) -> None:
        first, second = ("EnvironmentFile=" + path for path in ENVIRONMENT_FILES)
        pair = first + "\n" + second
        for replacement in (second + "\n" + first, first, pair + "\n" + second,
                            pair + "\nEnvironmentFile=/tmp/extra.env",
                            pair.replace("=/etc/", "=-/etc/"), "EnvironmentFile=\n" + pair):
            with self.subTest(replacement=replacement):
                self.assertIn(pair, self.text)
                with self.assertRaises(ValueError):
                    candidate_unit(self.text.replace(pair, replacement))

    def test_unapproved_hooks_and_directives_are_rejected(self) -> None:
        for key in ("ExecCondition", "ExecStartPost", "ExecStop", "ExecStopPost",
                    "ExecReload", "RootDirectory", "PassEnvironment"):
            with self.subTest(key=key):
                altered = self.text.replace("[Service]", "[Service]\n" + key + "=/bin/false")
                with self.assertRaises(ValueError):
                    candidate_unit(altered)
        with self.assertRaises(ValueError):
            candidate_unit(self.text + "\n[Install]\nWantedBy=multi-user.target\n")

    def test_exact_java_argv_rejects_wrappers_prefixes_and_extra_args(self) -> None:
        for replacement in ("/tmp/unreviewed-java", "+/usr/bin/java", "!/usr/bin/java",
                            "-/usr/bin/java", ":/usr/bin/java", "/bin/sh -c /usr/bin/java",
                            "$JAVA", "%h/java", '"/usr/bin/java"',
                            "/usr/bin/java -javaagent:/tmp/example.jar"):
            with self.subTest(replacement=replacement):
                with self.assertRaises(ValueError):
                    candidate_unit(self.text.replace("ExecStart=/usr/bin/java",
                                                     "ExecStart=" + replacement))
        for suffix in (" extra", " --unknown=true", " --server.port=8080"):
            with self.subTest(suffix=suffix):
                with self.assertRaises(ValueError):
                    candidate_unit(self.text.replace("--logging.level.org.hibernate.orm.jdbc.bind=off",
                                                     "--logging.level.org.hibernate.orm.jdbc.bind=off" + suffix))

    def test_fixed_smtp_and_all_filesystem_targets_reject_changes(self) -> None:
        for key in ("spring.mail.host", "spring.mail.port", "app.storage.public-path",
                    "app.storage.private-path", "spring.servlet.multipart.location"):
            with self.subTest(key=key):
                with self.assertRaises(ValueError):
                    candidate_unit(self.text.replace("--" + key + "=" + FIXED_CLI[key],
                                                     "--" + key + "=synthetic-unapproved"))
        with self.assertRaises(ValueError):
            candidate_unit(self.text.replace("-Djava.io.tmpdir=/var/lib/atstudio/tmp",
                                             "-Djava.io.tmpdir=/tmp/unreviewed"))

    def test_restricted_grammar_rejects_ambiguous_input(self) -> None:
        for suffix in ("[Service]\n", "[Unknown]\n", "Restart\n", " Restart=no\n",
                       "Restart=no\x00\n", "Restart=$VALUE\n", "Restart=no;comment\n",
                       "ExecStart=/usr/bin/java \\\n", "Restart=no \\\n    --bad=true\n"):
            with self.subTest(suffix=suffix):
                with self.assertRaises(ValueError):
                    parse_unit(self.text + "\n" + suffix)


if __name__ == "__main__":
    unittest.main(verbosity=2)
