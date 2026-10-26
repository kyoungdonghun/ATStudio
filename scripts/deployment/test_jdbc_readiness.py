"""WI029 synthetic checks; never use a real env, JDBC connection or service."""

from __future__ import annotations

from contextlib import redirect_stdout
from datetime import datetime, timezone
import importlib.util
import io
import json
import os
from pathlib import Path
import shutil
import stat
import struct
import subprocess
import sys
import time
from types import SimpleNamespace
import unittest
from unittest.mock import Mock, patch


SPEC = importlib.util.spec_from_file_location(
    "jdbc_readiness", Path(__file__).with_name("jdbc-readiness.py"))
probe = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = probe
SPEC.loader.exec_module(probe)
ROOT = Path(__file__).absolute().parents[2]
SENTINEL = "SYNTHETIC-NOT-SECRET-" + "x" * 11
NOW = datetime(2026, 10, 10, 13, 0, tzinfo=timezone.utc)


def environment(password: str = SENTINEL) -> bytes:
    values = dict(probe.DB_VALUES, SPRING_DATASOURCE_PASSWORD=password)
    values["JWT_SECRET"] = "SYNTHETIC-IGNORED"
    lines = ['# INCOMPLETE BOOTSTRAP; DO NOT START OR RETRY.']
    for key, value in values.items():
        quoted = value.replace("\\", "\\\\").replace('"', '\\"')
        lines.append(f'{key}="{quoted}"')
    return ("\n".join(lines) + "\n").encode("ascii")


def approval() -> dict:
    return {"req": probe.REQ, "review_wi": "WI-20261010-ATS-031",
            "bundle_sha256": "a" * 64, "machine_id_sha256": "b" * 64,
            "server_uuid_sha256": "c" * 64,
            "issued_at": "2026-10-10T12:59:00Z",
            "expires_at": "2026-10-10T13:10:00Z",
            "operator_checks": dict.fromkeys(probe.CHECKS, True)}


class ReadinessTests(unittest.TestCase):
    def test_generated_grammar_roundtrip_including_all_printable_password_chars(self):
        for offset in range(94):
            password = "".join(chr(33 + (offset + index) % 94) for index in range(32))
            self.assertEqual(probe.parse_env(environment(password)), password)

    def test_unrelated_values_are_not_returned(self):
        self.assertEqual(probe.parse_env(environment()), SENTINEL)

    def test_duplicate_keys_including_non_db_keys_are_rejected(self):
        for key in (*probe.DB_VALUES, "SPRING_DATASOURCE_PASSWORD", "JWT_SECRET"):
            with self.subTest(key=key), self.assertRaises(probe.GateError):
                probe.parse_env(environment() + f'{key}="ignored"\n'.encode())

    def test_missing_required_key(self):
        for key in (*probe.DB_VALUES, "SPRING_DATASOURCE_PASSWORD"):
            raw = b"\n".join(line for line in environment().split(b"\n")
                              if not line.startswith(key.encode() + b"="))
            with self.subTest(key=key), self.assertRaises(probe.GateError):
                probe.parse_env(raw)

    def test_url_security_and_target_cannot_be_overridden(self):
        original = probe.URL.encode()
        for replacement in (original + b"&allowPublicKeyRetrieval=true",
                            original + b"&useSSL=false", original + b"&sslMode=DISABLED",
                            original.replace(b"127.0.0.1", b"localhost"),
                            original.replace(b"atstudio_prod", b"atstudio"),
                            original.replace(b"3306", b"3307")):
            with self.subTest(url_case=replacement[-20:]), self.assertRaises(probe.GateError):
                probe.parse_env(environment().replace(original, replacement))
        with self.assertRaises(probe.GateError):
            probe.parse_env(environment().replace(b'atstudio_app"', b'root"'))

    def test_parser_rejects_shell_multiline_unknown_escape_and_controls(self):
        invalid = (b'export A="x"\n', b'A=x\n', b"A='x'\n", b'A="x" trailing\n',
                   b'A="x\\n"\n', b'A="x\\$"\n', b'A="x\\\nmore"\n',
                   b'A="a"b"\n', b'A="x\x00"\n', b'A="x\t"\n',
                   b'A="x\r"\n', b'A="\xff"\n', b'a="x"\n', b' A="x"\n',
                   b'JAVA_TOOL_OPTIONS="x"\n',
                   b'SPRING_DATASOURCE_HIKARI_DATA_SOURCE_PROPERTIES_SSLMODE="DISABLED"\n')
        for addition in invalid:
            with self.subTest(addition=addition), self.assertRaises(probe.GateError):
                probe.parse_env(environment() + addition)
        for raw in (environment()[:-1], b"x" * (probe.ENV_LIMIT + 1)):
            with self.assertRaises(probe.GateError):
                probe.parse_env(raw)

    def test_password_shape_is_exact_generated_32_printable_nonspace_bytes(self):
        for password in ("", "x" * 31, "x" * 33, " " + "x" * 31):
            with self.assertRaises(probe.GateError):
                probe.parse_env(environment(password))

    def test_frame_is_length_delimited_and_has_no_env_or_url(self):
        value = probe.frame(SENTINEL, "c" * 64)
        self.assertEqual(value[:4], b"ATS1")
        self.assertEqual(struct.unpack(">I", value[4:8])[0], 32)
        self.assertEqual(value[8:40], SENTINEL.encode())
        self.assertEqual(struct.unpack(">I", value[40:44])[0], 64)
        self.assertEqual(value[44:], b"c" * 64)
        self.assertNotIn(b"JWT_SECRET", value)
        self.assertNotIn(b"jdbc:", value)

    def test_approval_positive_and_strict_failure_matrix(self):
        self.assertEqual(probe.parse_approval(json.dumps(approval()).encode(),
                                             "a" * 64, "b" * 64, NOW), approval())
        cases = [("req", "wrong"), ("review_wi", "WI029"),
                 ("bundle_sha256", "d" * 64), ("machine_id_sha256", "d" * 64),
                 ("server_uuid_sha256", "x"), ("operator_checks", {}),
                 ("issued_at", "2026-10-10T13:01:00Z"),
                 ("expires_at", "2026-10-10T13:00:00Z"),
                 ("expires_at", "2026-10-10T13:30:00Z"),
                 ("expires_at", "invalid")]
        for key, value in cases:
            candidate = approval()
            candidate[key] = value
            with self.subTest(key=key), self.assertRaises(probe.GateError):
                probe.parse_approval(json.dumps(candidate).encode(), "a" * 64, "b" * 64, NOW)

    def test_approval_rejects_duplicate_extra_and_truthy_nonboolean(self):
        raw = json.dumps(approval()).replace('"req":', '"req":"wrong","req":')
        candidates = [raw.encode()]
        value = approval()
        value["extra"] = True
        candidates.append(json.dumps(value).encode())
        value = approval()
        value["operator_checks"][probe.CHECKS[0]] = 1
        candidates.append(json.dumps(value).encode())
        for raw in candidates:
            with self.assertRaises(probe.GateError):
                probe.parse_approval(raw, "a" * 64, "b" * 64, NOW)

    def test_child_environment_discards_injection_and_secrets(self):
        with patch.dict(os.environ, {"JAVA_TOOL_OPTIONS": SENTINEL,
                                     "JDK_JAVA_OPTIONS": SENTINEL,
                                     "_JAVA_OPTIONS": SENTINEL,
                                     "CLASSPATH": SENTINEL,
                                     "SPRING_DATASOURCE_PASSWORD": SENTINEL}):
            child = probe.child_env()
        self.assertNotIn(SENTINEL, str(child))
        self.assertEqual(set(child) - {"SystemRoot", "WINDIR"}, {"PATH", "LC_ALL", "HOME"})

    def test_command_has_only_two_classpath_entries_and_dump_controls(self):
        command = probe.java_command(Path("/trusted/java"), Path("/trusted/output"), "probe")
        self.assertNotIn(SENTINEL, str(command))
        self.assertIn("-XX:-CreateCoredumpOnCrash", command)
        self.assertIn("-XX:-HeapDumpOnOutOfMemoryError", command)
        self.assertIn("-XX:+DisableAttachMechanism", command)
        self.assertIn("-XX:-UsePerfData", command)
        self.assertNotIn("-jar", command)
        cp = command[command.index("-cp") + 1].split(os.pathsep)
        self.assertEqual(len(cp), 2)
        self.assertNotIn("app.jar", str(cp))

    def test_safe_file_rejects_link_owner_mode_hardlink_and_nonregular(self):
        path = Mock()
        path.parents = []
        for uid, mode, count in ((0, stat.S_IFLNK | 0o600, 1),
                                 (1, stat.S_IFREG | 0o600, 1),
                                 (0, stat.S_IFREG | 0o620, 1),
                                 (0, stat.S_IFREG | 0o644, 1),
                                 (0, stat.S_IFREG | 0o600, 2),
                                 (0, stat.S_IFIFO | 0o600, 1)):
            path.lstat.return_value = SimpleNamespace(st_uid=uid, st_mode=mode, st_nlink=count)
            with self.subTest(mode=mode, uid=uid, count=count), self.assertRaises(probe.GateError):
                probe.secure_path(path, private=True)

    def test_untrusted_parent_rejected(self):
        parent = Mock()
        parent.lstat.return_value = SimpleNamespace(st_uid=0, st_mode=stat.S_IFDIR | 0o777)
        path = Mock()
        path.parents = [parent]
        path.lstat.return_value = SimpleNamespace(st_uid=0, st_mode=stat.S_IFREG | 0o600,
                                                  st_nlink=1)
        with self.assertRaises(probe.GateError):
            probe.secure_path(path, private=True)

    def test_private_open_checks_before_read_and_uses_nofollow(self):
        with patch.object(probe, "secure_path", side_effect=probe.GateError("PATH_OWNER")), \
                patch.object(probe.os, "open") as opened:
            with self.assertRaises(probe.GateError):
                probe.read_private(probe.ENV_PATH, probe.ENV_LIMIT)
            opened.assert_not_called()

    def test_probe_on_windows_does_not_read_private_or_spawn(self):
        with patch.object(probe.sys, "platform", "win32"), \
                patch.object(probe, "read_private") as private, \
                patch.object(probe, "run_child") as child:
            with self.assertRaisesRegex(probe.GateError, "LINUX_ROOT_REQUIRED"):
                probe.native_probe({}, ROOT, ROOT)
            private.assert_not_called()
            child.assert_not_called()

    def test_cli_errors_never_echo_untrusted_input(self):
        stdout = io.StringIO()
        with redirect_stdout(stdout):
            self.assertEqual(probe.main([SENTINEL]), 2)
        self.assertNotIn(SENTINEL, stdout.getvalue())
        self.assertIn("CLI_ARGUMENTS", stdout.getvalue())

    def test_full_exceptions_are_suppressed_even_gate_error_details(self):
        for failure in (RuntimeError(SENTINEL), probe.GateError(SENTINEL),
                        subprocess.TimeoutExpired(SENTINEL, 1, SENTINEL.encode()),
                        KeyboardInterrupt(SENTINEL)):
            stdout = io.StringIO()
            with patch.object(probe, "load_plan", side_effect=failure), redirect_stdout(stdout):
                self.assertEqual(probe.main(["plan", "--output", "build/WI029-synthetic"]), 2)
            self.assertNotIn(SENTINEL, stdout.getvalue())
            self.assertNotIn("Traceback", stdout.getvalue())
            self.assertEqual(json.loads(stdout.getvalue())["production_status"], "HOLD")

    def test_status_validation_rejects_logs_unknown_codes_and_exit_mismatch(self):
        for result in ((0, b"PASS\n" + SENTINEL.encode()), (0, b"UNKNOWN\n"),
                       (2, b"PASS\n"), (0, b"JDBC_CONNECT_FAILED\n"),
                       (0, b"DRIVER_LOADED\n")):
            with self.assertRaises(probe.GateError):
                probe.checked_status(*result, mode="probe")
        self.assertEqual(probe.checked_status(0, b"PASS\n", mode="probe"), "PASS")
        self.assertEqual(probe.checked_status(2, b"JDBC_CONNECT_FAILED\n", mode="probe"),
                         "JDBC_CONNECT_FAILED")

    def test_no_sql_mutations_or_spring_in_production_java(self):
        source = (ROOT / probe.JAVA_SOURCE).read_text(encoding="utf-8")
        for forbidden in ("executeUpdate(", "executeBatch(", "prepareCall(",
                          "SpringApplication", "printStackTrace(", "getMessage(",
                          '"allowPublicKeyRetrieval"', '"sslMode"', '"useSSL"'):
            self.assertNotIn(forbidden, source)
        self.assertIn('"SELECT @@session.transaction_read_only"', source)
        self.assertIn("setReadOnly(true)", source)
        self.assertIn("rollback()", source)

    def test_bounded_subprocess_success_stderr_and_overflow(self):
        code, output = probe.run_child([sys.executable, "-I", "-c",
                                       "import sys;sys.stderr.write('SYNTHETIC-NOISE');"
                                       "sys.stdout.buffer.write(b'OK\\n')"])
        self.assertEqual((code, output), (0, b"OK\n"))
        with self.assertRaisesRegex(probe.GateError, "PROCESS_OUTPUT"):
            probe.run_child([sys.executable, "-I", "-c", "print('x'*10000)"], limit=16)

    def test_bounded_subprocess_timeout_reaps_child(self):
        with self.assertRaisesRegex(probe.GateError, "PROCESS_TIMEOUT"):
            probe.run_child([sys.executable, "-I", "-c", "import time;time.sleep(30)"], timeout=1)

    def test_payload_bound_before_spawn(self):
        with patch.object(probe.subprocess, "Popen") as spawn:
            with self.assertRaises(probe.GateError):
                probe.run_child(["never"], b"x" * 4097)
            spawn.assert_not_called()

    def test_public_pinned_jar_and_output_restriction(self):
        self.assertEqual(probe.sha_file(ROOT / probe.JAR), probe.JAR_SHA)
        for name in ("/tmp/test", "build/../secret", "build/WI029-x/y", "build/other"):
            with self.assertRaises(probe.GateError):
                probe.output_path(ROOT, name)


# 테스트 전용 드라이버는 모든 연결을 메모리 프록시로 처리한다.
FAKE_DRIVER = r'''
package com.mysql.cj.jdbc;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class Driver implements java.sql.Driver {
    static String scenario = System.getProperty("fixture", "success");
    static int executed = 0;
    static boolean readonly = false;
    static boolean auto = true;
    static {
        try { DriverManager.registerDriver(new Driver()); }
        catch (SQLException e) { throw new RuntimeException(e); }
        System.out.println("SYNTHETIC-CLASS-LOG");
        System.err.println("SYNTHETIC-CLASS-ERROR");
    }
    public boolean acceptsURL(String url) { return url.startsWith("jdbc:mysql:"); }
    public int getMajorVersion() { return 9; }
    public int getMinorVersion() { return scenario.equals("version") ? 6 : 7; }
    public boolean jdbcCompliant() { return false; }
    public Logger getParentLogger() { return Logger.getGlobal(); }
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties p) {
        return new DriverPropertyInfo[0];
    }
    static void check(boolean test) throws SQLException {
        if (!test) throw new SQLException("SYNTHETIC-ASSERTION-FAILED");
    }
    static void mark(String value) throws IOException {
        String path = System.getProperty("marker");
        if (path != null) Files.writeString(Path.of(path), value + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
    static Object proxy(Class<?> type, InvocationHandler handler) {
        return Proxy.newProxyInstance(Driver.class.getClassLoader(), new Class<?>[]{type}, handler);
    }
    static List<String> tables() throws Exception {
        Field field = Class.forName("RuntimeJdbcProbe").getDeclaredField("TABLES");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) field.get(null);
        return names;
    }
    public Connection connect(String url, Properties p) throws SQLException {
        try {
            mark("connect");
            check(url.equals("jdbc:mysql://127.0.0.1:3306/atstudio_prod?serverTimezone=Asia/Seoul"
                    + "&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull"));
            check(!p.containsKey("allowPublicKeyRetrieval") && !p.containsKey("sslMode")
                    && !p.containsKey("useSSL"));
            check(p.getProperty("user").equals("atstudio_app"));
            check(p.getProperty("logger").equals("com.mysql.cj.log.NullLogger"));
            check(p.getProperty("socketTimeout").equals("5000")
                    && p.getProperty("connectTimeout").equals("5000"));
            check(p.getProperty("queryTimeoutKillsConnection").equals("true"));
            check(p.getProperty("openTelemetry").equals("DISABLED"));
            for (String name : List.of("profileSQL", "traceProtocol", "enablePacketDebug",
                    "dumpQueriesOnException", "autoGenerateTestcaseScript", "logSlowQueries",
                    "gatherPerfMetrics", "autoReconnect")) {
                check(p.getProperty(name).equals("false"));
            }
            System.out.println(p.getProperty("password"));
            System.err.println(url + p.getProperty("password"));
            if (scenario.equals("connect_error")) {
                SQLException fail = new SQLException("SYNTHETIC-SECRET-EXCEPTION", "42000");
                fail.initCause(new RuntimeException("SYNTHETIC-CAUSE"));
                fail.addSuppressed(new RuntimeException("SYNTHETIC-SUPPRESSED"));
                throw fail;
            }
            if (scenario.equals("sleep")) Thread.sleep(30000);
            return (Connection) proxy(Connection.class, (o, method, args) -> {
                switch (method.getName()) {
                    case "setReadOnly": readonly = (Boolean) args[0]; return null;
                    case "setAutoCommit": auto = (Boolean) args[0]; return null;
                    case "isReadOnly": return readonly && !scenario.equals("readonly");
                    case "getAutoCommit": return auto;
                    case "createStatement": check(readonly && !auto); return statement();
                    case "rollback": mark("rollback"); return null;
                    case "close": mark("close");
                        if (scenario.equals("close_error")) throw new SQLException("SYNTHETIC-CLOSE");
                        return null;
                    default: throw new SQLException("UNEXPECTED_CONNECTION_METHOD");
                }
            });
        } catch (SQLException e) { throw e; }
        catch (Exception e) { throw new SQLException("SYNTHETIC-FAILURE", e); }
    }
    static Statement statement() {
        return (Statement) proxy(Statement.class, (o, method, args) -> {
            switch (method.getName()) {
                case "setQueryTimeout": check(args[0].equals(3)); return null;
                case "setMaxRows": check(args[0].equals(44)); return null;
                case "close": mark("statement_close"); return null;
                case "executeQuery":
                    String query = (String) args[0];
                    check(readonly && !auto && query.startsWith("SELECT "));
                    executed++;
                    if (scenario.equals("query_error")) throw new SQLException("SYNTHETIC-QUERY-SECRET");
                    List<String> values = new ArrayList<>();
                    if (query.contains("transaction_read_only")) {
                        values.add(scenario.equals("server_readonly") ? "0" : "1");
                    } else if (query.contains("CURRENT_USER")) {
                        values.add(scenario.equals("identity") ? "wrong" : "atstudio_app@127.0.0.1");
                    } else if (query.contains("DATABASE()")) values.add("atstudio_prod");
                    else if (query.contains("SHA2(")) values.add(scenario.equals("server") ? "d".repeat(64) : "c".repeat(64));
                    else if (query.contains("SELECT TABLE_NAME")) {
                        values.addAll(tables());
                        if (scenario.equals("table")) values.set(0, "unexpected");
                        if (scenario.equals("extra_table")) values.add("extra");
                    } else if (query.contains("SELECT COUNT(*) FROM")) {
                        values.add(scenario.equals("rows") ? "42" : query.contains("`subscriptions`") ? "6" : "0");
                    } else values.add(scenario.equals("plans") && query.contains("DISTINCT") ? "0" : "1");
                    if (scenario.equals("null")) values.set(0, null);
                    if (scenario.equals("empty")) values.clear();
                    if (scenario.equals("extra_row")) values.add("extra");
                    int[] cursor = {-1};
                    return proxy(ResultSet.class, (r, m, a) -> {
                        switch (m.getName()) {
                            case "next": return ++cursor[0] < values.size();
                            case "getString": return values.get(cursor[0]);
                            case "close": return null;
                            default: throw new SQLException("UNEXPECTED_RESULT_METHOD");
                        }
                    });
                default: throw new SQLException("UNEXPECTED_STATEMENT_METHOD");
            }
        });
    }
}
'''


class JavaSyntheticTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.output = ROOT / "build" / ("WI029-tests-" + str(time.time_ns()))
        cls.output.mkdir()
        cls.classes = cls.output / "classes"
        cls.classes.mkdir()
        cls.java = Path(shutil.which("java")).resolve(strict=True)
        compiler = Path(shutil.which("javac")).resolve(strict=True)
        source = cls.output / "Driver.java"
        source.write_text(FAKE_DRIVER, encoding="ascii")
        result = subprocess.run([str(compiler), "--release", "17", "-encoding", "UTF-8",
                                 "-Xlint:all", "-d", str(cls.classes),
                                 str(ROOT / probe.JAVA_SOURCE), str(source)],
                                stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                env=probe.child_env(), timeout=30)
        if result.returncode != 0:
            raise AssertionError(result.stderr.decode("utf-8"))

    def execute(self, scenario: str, payload: bytes | None = None,
                mode: str = "probe", timeout: int = 10) -> tuple[int, bytes, Path]:
        marker = self.output / (scenario + "-" + str(time.time_ns()) + ".txt")
        command = [str(self.java), "-XX:-UsePerfData", "-Dfixture=" + scenario,
                   "-Dmarker=" + str(marker), "-cp", str(self.classes), "RuntimeJdbcProbe", mode]
        code, output = probe.run_child(command, probe.frame(SENTINEL, "c" * 64)
                                       if payload is None else payload, timeout=timeout)
        self.assertNotIn(b"SYNTHETIC", output)
        return code, output, marker

    def test_success_select_only_transaction_rollback_close_and_log_suppression(self):
        code, output, marker = self.execute("success")
        self.assertEqual((code, output), (0, b"PASS\n"))
        self.assertEqual(marker.read_text().splitlines(),
                         ["connect", "statement_close", "rollback", "close"])

    def test_class_loading_never_connects(self):
        code, output, marker = self.execute("success", b"", "smoke")
        self.assertEqual((code, output), (0, b"DRIVER_LOADED\n"))
        self.assertFalse(marker.exists())

    def test_failure_matrix_outputs_only_stage_and_closes_connection(self):
        cases = {"connect_error": "JDBC_CONNECT_FAILED", "readonly": "SESSION_REJECTED",
                 "server_readonly": "SESSION_REJECTED", "query_error": "SESSION_REJECTED",
                 "identity": "IDENTITY_REJECTED", "server": "IDENTITY_REJECTED",
                 "table": "TABLES_REJECTED", "extra_table": "TABLES_REJECTED",
                 "rows": "ROWS_REJECTED", "plans": "ROWS_REJECTED", "version": "DRIVER_REJECTED",
                 "close_error": "CLOSE_FAILED", "null": "SESSION_REJECTED",
                 "empty": "SESSION_REJECTED", "extra_row": "SESSION_REJECTED"}
        for scenario, expected in cases.items():
            with self.subTest(scenario=scenario):
                code, output, marker = self.execute(scenario)
                self.assertEqual((code, output), (2, (expected + "\n").encode()))
                if scenario == "version":
                    self.assertFalse(marker.exists())
                elif scenario != "connect_error":
                    self.assertEqual(marker.read_text().splitlines()[-1], "close")

    def test_framing_failures_do_not_load_or_connect(self):
        valid = probe.frame(SENTINEL, "c" * 64)
        cases = (b"", valid[:-1], valid + b"x", b"FAIL" + valid[4:],
                 valid[:4] + struct.pack(">I", 2147483647) + valid[8:],
                 valid[:8] + b"\x00" + valid[9:], valid[:-64] + b"X" * 64)
        for payload in cases:
            with self.subTest(length=len(payload)):
                code, output, marker = self.execute("success", payload)
                self.assertEqual((code, output), (2, b"INPUT_REJECTED\n"))
                self.assertFalse(marker.exists())

    def test_stalled_fake_driver_is_killed_by_parent_deadline(self):
        with self.assertRaisesRegex(probe.GateError, "PROCESS_TIMEOUT"):
            self.execute("sleep", timeout=1)


if __name__ == "__main__":
    unittest.main()
