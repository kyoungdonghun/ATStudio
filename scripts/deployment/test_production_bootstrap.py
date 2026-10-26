"""WI025 synthetic tests: no real SQL, secrets, subprocesses or target writes."""

from __future__ import annotations

import base64
from contextlib import ExitStack, redirect_stdout
from datetime import datetime, timezone
import importlib.util
import io
import json
import os
from pathlib import Path
import subprocess
import stat
import sys
from types import SimpleNamespace
import unittest
from unittest.mock import Mock, patch


SPEC = importlib.util.spec_from_file_location(
    "production_bootstrap", Path(__file__).with_name("production-bootstrap.py"))
bootstrap = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = bootstrap
SPEC.loader.exec_module(bootstrap)
ROOT = Path(__file__).absolute().parents[2]
FAKE_PASSWORD = "SYNTHETIC-NOT-SECRET-" + "x" * 11
NOW = datetime(2026, 10, 10, 12, 35, tzinfo=timezone.utc)


def approval(bundle):
    return {"req": bootstrap.REQ, "review_wi": "WI-20261010-ATS-027",
            "bundle_sha256": bundle.identity, "machine_id_sha256": "a" * 64,
            "server_uuid_sha256": "b" * 64, "issued_at": "2026-10-10T12:30:00Z",
            "expires_at": "2026-10-10T13:00:00Z",
            "operator_checks": dict.fromkeys(bootstrap.CHECKS, True)}


class PreservedBuffer(io.BytesIO):
    def close(self):
        pass


class BootstrapTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.bundle = bootstrap.load_bundle(ROOT)

    def setUp(self):
        self.stack = ExitStack()
        self.addCleanup(self.stack.close)
        self.native = self.stack.enter_context(patch.object(
            bootstrap.subprocess, "run", side_effect=AssertionError("REAL_PROCESS_FORBIDDEN")))
        self.entropy = self.stack.enter_context(patch.object(
            bootstrap.secrets, "token_bytes", side_effect=AssertionError("REAL_RANDOM_FORBIDDEN")))

    def test_public_bundle_pins_and_43_tables(self):
        self.assertEqual(len(self.bundle.tables), 43)
        self.assertEqual(self.bundle.hashes[bootstrap.SCHEMA], bootstrap.PINS[bootstrap.SCHEMA])
        self.assertFalse(bootstrap.safe_status("PLAN", self.bundle)["app_runnable"])

    def test_contract_failure_stops_before_source_read(self):
        with patch.object(bootstrap.validator, "validate", return_value={"template_status": "FAIL"}), \
                patch.object(bootstrap, "read_public") as reader:
            with self.assertRaisesRegex(bootstrap.GateError, "BUNDLE_CONTRACT"):
                bootstrap.load_bundle(ROOT)
            reader.assert_not_called()

    def test_sql_drift_rejected(self):
        original = bootstrap.read_public
        def changed(path):
            return original(path) + (b"\n" if path.name == "seed.sql" else b"")
        with patch.object(bootstrap, "read_public", side_effect=changed):
            with self.assertRaisesRegex(bootstrap.GateError, "SQL_IDENTITY"):
                bootstrap.load_bundle(ROOT)

    def test_plan_is_offline_and_secret_free(self):
        output = io.StringIO()
        with redirect_stdout(output):
            self.assertEqual(bootstrap.main(["plan", "--root", str(ROOT)]), 0)
        result = json.loads(output.getvalue())
        self.assertEqual(result["canonical_fresh_manifest"], "UNRECORDED")
        self.native.assert_not_called()
        self.entropy.assert_not_called()

    def test_cli_does_not_echo_bad_argument(self):
        output = io.StringIO()
        with redirect_stdout(output):
            self.assertEqual(bootstrap.main([FAKE_PASSWORD]), 2)
        self.assertNotIn(FAKE_PASSWORD, output.getvalue())

    def test_windows_cannot_apply(self):
        output = io.StringIO()
        with patch.object(bootstrap.sys, "platform", "win32"), redirect_stdout(output):
            self.assertEqual(bootstrap.main(["apply", "--root", str(ROOT)]), 2)
        self.assertIn("LINUX_ROOT_REQUIRED", output.getvalue())
        self.native.assert_not_called()
        self.entropy.assert_not_called()

    def test_approval_valid(self):
        value = approval(self.bundle)
        self.assertEqual(bootstrap.parse_approval(json.dumps(value).encode(), self.bundle,
                                                 "a" * 64, NOW), value)

    def test_approval_negative_matrix(self):
        cases = [("req", "other"), ("review_wi", "WI025"), ("bundle_sha256", "c" * 64),
                 ("machine_id_sha256", "c" * 64), ("server_uuid_sha256", "invalid"),
                 ("issued_at", "2026-10-10T12:36:00Z"),
                 ("expires_at", "2026-10-10T12:35:00Z"),
                 ("expires_at", "2026-10-10T13:01:00Z"),
                 ("expires_at", "not-a-time"), ("operator_checks", {})]
        for key, value in cases:
            with self.subTest(key=key, value=value):
                candidate = approval(self.bundle)
                candidate[key] = value
                with self.assertRaises(bootstrap.GateError):
                    bootstrap.parse_approval(json.dumps(candidate).encode(), self.bundle, "a" * 64, NOW)
        for alteration in ("extra", "missing", "integer-true"):
            candidate = approval(self.bundle)
            if alteration == "extra":
                candidate["extra"] = "not-allowed"
            elif alteration == "missing":
                del candidate["req"]
            else:
                candidate["operator_checks"][bootstrap.CHECKS[0]] = 1
            with self.assertRaises(bootstrap.GateError):
                bootstrap.parse_approval(json.dumps(candidate).encode(), self.bundle, "a" * 64, NOW)

    def test_duplicate_approval_key_rejected(self):
        raw = json.dumps(approval(self.bundle)).replace('"req":', '"req":"duplicate","req":')
        with self.assertRaises(bootstrap.validator.AuditError):
            bootstrap.parse_approval(raw.encode(), self.bundle, "a" * 64, NOW)

    def test_password_result_exact_account_length_and_factor(self):
        for tail in ("", "\t1"):
            self.assertEqual(bootstrap.password_result(
                f"atstudio_app\t127.0.0.1\t{FAKE_PASSWORD}{tail}"), FAKE_PASSWORD)
        for value in ("other\t127.0.0.1\t" + FAKE_PASSWORD,
                      "atstudio_app\t%\t" + FAKE_PASSWORD,
                      "atstudio_app\t127.0.0.1\tshort",
                      "atstudio_app\t127.0.0.1\t" + FAKE_PASSWORD + "\t2",
                      "atstudio_app\t127.0.0.1\t" + FAKE_PASSWORD + "\nextra"):
            with self.assertRaises(bootstrap.GateError):
                bootstrap.password_result(value)

    def test_env_preserves_disabled_providers_and_unresolved_gates(self):
        raw = bootstrap.render_env(self.bundle, FAKE_PASSWORD, b"j" * 32, b"b" * 32)
        self.assertIn(b'MAIL_PORT="__SMTP_PORT__"', raw)
        self.assertIn(b'APP_BASE_URL="https://__APP_FQDN__"', raw)
        self.assertIn(b'JWT_SECRET="' + base64.b64encode(b"j" * 32) + b'"', raw)
        for key in bootstrap.validator.EMPTY:
            self.assertIn((key + '=""\n').encode(), raw)
        for key, value in bootstrap.validator.SAFE.items():
            self.assertIn(f'{key}="{value}"\n'.encode(), raw)
        self.assertNotIn(b"__DB_PASSWORD__", raw)
        self.assertIn(b"NOT RUNNABLE", raw)
        self.entropy.assert_not_called()

    def test_env_quotes_random_password_without_expansion(self):
        password = ('a"\\$`#;' + "x" * 32)[:32]
        raw = bootstrap.render_env(self.bundle, password, b"j" * 32, b"b" * 32)
        self.assertIn(b'SPRING_DATASOURCE_PASSWORD="a\\"\\\\$`#;', raw)
        self.assertEqual(raw.count(b"SPRING_DATASOURCE_PASSWORD="), 1)

    def test_env_rejects_nonindependent_or_short_keys(self):
        for jwt, billing in ((b"j" * 32, b"j" * 32), (b"j", b"b" * 32)):
            with self.assertRaises(bootstrap.GateError):
                bootstrap.render_env(self.bundle, FAKE_PASSWORD, jwt, billing)

    def test_mysql_arguments_environment_and_session_binlog_guard(self):
        with patch.object(bootstrap, "secure_path"), patch.object(bootstrap, "absent") as missing:
            self.native.side_effect = None
            self.native.return_value = subprocess.CompletedProcess([], 0, b"1\n")
            self.assertEqual(bootstrap.sql("SELECT 1;", mutation=True), "1")
            call = self.native.call_args
            self.assertEqual(call.args[0][1], "--no-defaults")
            self.assertNotIn("--no-login-paths", call.args[0])
            self.assertNotIn("--force", call.args[0])
            self.assertIn("--skip-reconnect", call.args[0])
            self.assertEqual(call.kwargs["input"], b"SET SESSION sql_log_bin=0;\nSELECT 1;")
            self.assertEqual(call.kwargs["stderr"], subprocess.DEVNULL)
            self.assertEqual(set(call.kwargs["env"]), {"HOME", "PATH", "LC_ALL", "MYSQL_HISTFILE"})
            self.assertEqual(missing.call_count, 2)

    def test_client_configuration_collision_prevents_native_command(self):
        with patch.object(bootstrap, "secure_path"), patch.object(
                bootstrap, "absent", side_effect=bootstrap.GateError("EXISTING_PATH")):
            with self.assertRaises(bootstrap.GateError):
                bootstrap.sql("SELECT 1;")
        self.native.assert_not_called()

    def test_subprocess_failures_never_forward_output(self):
        for result in (subprocess.CompletedProcess([], 1, FAKE_PASSWORD.encode()),
                       subprocess.CompletedProcess([], 0, b"x" * (bootstrap.LIMIT + 1))):
            self.native.side_effect = None
            self.native.return_value = result
            with self.assertRaises(bootstrap.GateError) as caught:
                bootstrap.run_private(["synthetic"])
            self.assertNotIn(FAKE_PASSWORD, str(caught.exception))
        self.native.side_effect = subprocess.TimeoutExpired("synthetic", 1, FAKE_PASSWORD.encode())
        with self.assertRaisesRegex(bootstrap.GateError, "NATIVE_COMMAND_FAILED"):
            bootstrap.run_private(["synthetic"])

    def test_native_database_gates_fail_closed_at_every_query(self):
        def answer(statement):
            return "b" * 64 if "SHA2(" in statement else "1"
        with patch.object(bootstrap, "sql", side_effect=answer) as query:
            bootstrap.database_guard("b" * 64, fresh=True)
            statements = [call.args[0] for call in query.call_args_list]
        for rejected in statements:
            with self.subTest(query=statements.index(rejected)), patch.object(
                    bootstrap, "sql", side_effect=lambda statement:
                    "0" if statement == rejected else answer(statement)):
                with self.assertRaises(bootstrap.GateError):
                    bootstrap.database_guard("b" * 64, fresh=True)
        self.assertTrue(any("partial_revokes=0" in query for query in statements))
        self.assertTrue(any("LOWER(User)='atstudio_app'" in query for query in statements))

    def test_admin_address_query_requires_null_safe_empty_comparison(self):
        def answer(statement):
            return "b" * 64 if "SHA2(" in statement else "1"
        with patch.object(bootstrap, "sql", side_effect=answer) as query:
            bootstrap.database_guard("b" * 64, fresh=True)
        configuration = [call.args[0] for call in query.call_args_list
                         if "@@global.admin_address" in call.args[0]]
        self.assertEqual(len(configuration), 1)
        self.assertIn("AND COALESCE(@@global.admin_address,'')='' AND ", configuration[0])
        self.assertNotIn("AND @@global.admin_address=''", configuration[0])

    def test_admin_address_negative_or_unknown_result_retains_hold(self):
        for result in ("0", "NULL", ""):
            with self.subTest(result=result):
                def answer(statement):
                    return "b" * 64 if "SHA2(" in statement else result
                output = io.StringIO()
                with patch.object(bootstrap, "sql", side_effect=answer), \
                        patch.object(bootstrap, "preflight", side_effect=lambda bundle:
                                     bootstrap.database_guard("b" * 64, fresh=True)), \
                        patch.object(bootstrap, "create_private") as create, \
                        redirect_stdout(output):
                    self.assertEqual(bootstrap.main(["apply", "--root", str(ROOT)]), 2)
                status = json.loads(output.getvalue())
                self.assertEqual(status["production_status"], "HOLD")
                self.assertFalse(status["app_runnable"])
                self.assertIn("MYSQL_CONFIGURATION", output.getvalue())
                create.assert_not_called()
        self.native.assert_not_called()
        self.entropy.assert_not_called()

    def test_plugin_query_exact_contract_and_classification_fixtures(self):
        expected_query = (
            "SELECT COUNT(*)=0 FROM information_schema.plugins WHERE PLUGIN_STATUS='ACTIVE' "
            "AND ((PLUGIN_TYPE='AUDIT' AND (COALESCE(BINARY PLUGIN_NAME IN "
            "('sha2_cache_cleaner','mysqlx_cache_cleaner'),0)=0 OR PLUGIN_LIBRARY IS NOT NULL)) "
            "OR PLUGIN_NAME IN ('mysqlx','group_replication'));"
        )
        allowed_names = ("sha2_cache_cleaner", "mysqlx_cache_cleaner")
        cases = [([], True),
                 ([("ACTIVE", "AUDIT", name, None) for name in allowed_names], True),
                 ([("INACTIVE", "AUDIT", "unknown", "external.so")], True)]
        for name in allowed_names:
            cases.append(([("ACTIVE", "AUDIT", name, None)], True))
            for library in ("", "external.so"):
                cases.append(([("ACTIVE", "AUDIT", name, library)], False))
            for variant in (name.upper(), name + " ", name + "_other"):
                cases.append(([("ACTIVE", "AUDIT", variant, None)], False))
        for name in ("unknown", "", None):
            for library in (None, "", "external.so"):
                cases.append(([("ACTIVE", "AUDIT", name, library)], False))
        for name in ("mysqlx", "group_replication"):
            for kind in ("AUDIT", "DAEMON"):
                for library in (None, "", "external.so"):
                    cases.append(([("ACTIVE", kind, name, library)], False))
        cases.append(([("ACTIVE", "AUDIT", "sha2_cache_cleaner", None),
                       ("ACTIVE", "AUDIT", "unknown", None)], False))
        for rows, accepted in cases:
            with self.subTest(rows=rows):
                def answer(statement):
                    if "information_schema.plugins" in statement:
                        self.assertEqual(statement, expected_query)
                        # Synthetic classification oracle, not native SQL evaluation.
                        blocked = any(
                            status == "ACTIVE" and (
                                (kind == "AUDIT" and not (
                                    name in allowed_names and library is None))
                                or name in ("mysqlx", "group_replication"))
                            for status, kind, name, library in rows)
                        return "0" if blocked else "1"
                    return "b" * 64 if "SHA2(" in statement else "1"
                with patch.object(bootstrap, "sql", side_effect=answer) as query:
                    if accepted:
                        bootstrap.database_guard("b" * 64, fresh=True)
                    else:
                        with self.assertRaisesRegex(bootstrap.GateError, "MYSQL_PLUGIN_CAPTURE"):
                            bootstrap.database_guard("b" * 64, fresh=True)
                    self.assertEqual(sum("information_schema.plugins" in call.args[0]
                                         for call in query.call_args_list), 1)
        self.native.assert_not_called()
        self.entropy.assert_not_called()

    def test_plugin_negative_malformed_and_ambiguous_results_retain_hold(self):
        for result in ("0", "NULL", "", "2", "01", " 1", "1 ", "1\n1", "1\t0"):
            with self.subTest(result=result):
                def answer(statement):
                    if "information_schema.plugins" in statement:
                        return result
                    return "b" * 64 if "SHA2(" in statement else "1"
                output = io.StringIO()
                with patch.object(bootstrap, "sql", side_effect=answer), \
                        patch.object(bootstrap, "preflight", side_effect=lambda bundle:
                                     bootstrap.database_guard("b" * 64, fresh=True)), \
                        patch.object(bootstrap, "create_private") as create, \
                        redirect_stdout(output):
                    self.assertEqual(bootstrap.main(["apply", "--root", str(ROOT)]), 2)
                status = json.loads(output.getvalue())
                self.assertEqual(status["production_status"], "HOLD")
                self.assertFalse(status["app_runnable"])
                self.assertIn("MYSQL_PLUGIN_CAPTURE", output.getvalue())
                create.assert_not_called()
        self.native.assert_not_called()
        self.entropy.assert_not_called()

    def test_plugin_sql_error_retains_hold_without_forwarding_output(self):
        def answer(statement):
            if "information_schema.plugins" in statement:
                return native_sql(statement)
            return "b" * 64 if "SHA2(" in statement else "1"
        native_sql = bootstrap.sql
        output = io.StringIO()
        self.native.side_effect = None
        self.native.return_value = subprocess.CompletedProcess([], 1, FAKE_PASSWORD.encode())
        with patch.object(bootstrap, "sql", side_effect=answer), \
                patch.object(bootstrap, "secure_path"), patch.object(bootstrap, "absent"), \
                patch.object(bootstrap, "preflight", side_effect=lambda bundle:
                             bootstrap.database_guard("b" * 64, fresh=True)), \
                patch.object(bootstrap, "create_private") as create, redirect_stdout(output):
            self.assertEqual(bootstrap.main(["apply", "--root", str(ROOT)]), 2)
        status = json.loads(output.getvalue())
        self.assertEqual(status["production_status"], "HOLD")
        self.assertFalse(status["app_runnable"])
        self.assertIn("NATIVE_COMMAND_FAILED", output.getvalue())
        self.assertNotIn(FAKE_PASSWORD, output.getvalue())
        self.assertEqual(self.native.call_count, 1)
        self.assertEqual(self.native.call_args.kwargs["stderr"], subprocess.DEVNULL)
        create.assert_not_called()
        self.entropy.assert_not_called()

    def test_grants_use_supported_routine_table_and_exact_escaped_schema(self):
        def answer(statement):
            return "DELETE\tNO\nINSERT\tNO\nSELECT\tNO\nUPDATE\tNO" if statement.startswith(
                "SELECT PRIVILEGE_TYPE") else "1"
        with patch.object(bootstrap, "sql", side_effect=answer) as query:
            bootstrap.verify_grants(locked=True)
        statements = "\n".join(call.args[0] for call in query.call_args_list)
        self.assertIn("mysql.procs_priv", statements)
        self.assertNotIn("ROUTINE_PRIVILEGES", statements)
        self.assertIn(bootstrap.GRANT_HEX, statements)
        self.assertNotIn("authentication_string", statements)
        self.assertNotIn("SHOW CREATE USER", statements)

    def test_grants_reject_extra_and_grantable_permissions(self):
        for invalid in ("DELETE\tNO\nINSERT\tNO\nSELECT\tYES\nUPDATE\tNO", "1"):
            with patch.object(bootstrap, "sql", return_value=invalid):
                with self.assertRaisesRegex(bootstrap.GateError, "GRANTS_DML"):
                    bootstrap.verify_grants(locked=True)

    def fake_apply(self, fail_at=None):
        stack = ExitStack()
        self.addCleanup(stack.close)
        files = {}
        queries = []
        def create(path):
            if path in files:
                raise FileExistsError("synthetic")
            files[path] = PreservedBuffer()
            return files[path]
        def query(statement, *, mutation=False):
            text = statement.decode() if isinstance(statement, bytes) else statement
            queries.append((text, mutation))
            if fail_at and fail_at in text:
                raise bootstrap.GateError("SYNTHETIC_FAILURE")
            if "BY RANDOM PASSWORD" in text:
                return f"atstudio_app\t127.0.0.1\t{FAKE_PASSWORD}\t1"
            return "1"
        for name, value in (
            ("preflight", Mock(return_value=approval(self.bundle))),
            ("host_guard", Mock()), ("database_guard", Mock()),
            ("create_private", Mock(side_effect=create)), ("sync_directory", Mock()),
            ("persist", Mock(side_effect=lambda stream, raw: stream.write(raw))),
            ("secure_path", Mock()), ("sql", Mock(side_effect=query)),
            ("observe", Mock(return_value={"tables": 43, "plans": 6, "non_plan_rows": 0})),
            ("verify_grants", Mock()),
        ):
            stack.enter_context(patch.object(bootstrap, name, value))
        clock = stack.enter_context(patch.object(bootstrap, "datetime", wraps=datetime))
        clock.now.return_value = NOW
        self.entropy.side_effect = [b"j" * 32, b"b" * 32]
        return files, queries

    def test_apply_success_stores_only_private_env_and_safe_receipt(self):
        files, queries = self.fake_apply()
        result = bootstrap.apply(self.bundle)
        self.assertEqual(set(files), {bootstrap.CLAIM_PATH, bootstrap.ENV_PATH, bootstrap.RECEIPT_PATH})
        self.assertIn(FAKE_PASSWORD.encode(), files[bootstrap.ENV_PATH].getvalue())
        self.assertNotIn(FAKE_PASSWORD, json.dumps(result))
        self.assertEqual(result["runtime_tcp_authentication"], "NOT_RUN")
        self.assertFalse(result["app_runnable"])
        mutations = [text for text, mutation in queries if mutation]
        self.assertEqual(len(mutations), 6)
        self.assertIn("BY RANDOM PASSWORD ACCOUNT LOCK", mutations[3])
        self.assertIn(r"ON `atstudio\_prod`.*", mutations[4])
        self.assertIn("ACCOUNT UNLOCK", mutations[5])
        for statement in mutations:
            self.assertNotIn(FAKE_PASSWORD, statement)
            self.assertNotIn("IF NOT EXISTS", statement)
            self.assertNotIn("DROP ", statement)

    def test_each_partial_sql_failure_preserves_claim_and_refuses_retry(self):
        for phase in ("CREATE DATABASE", "CREATE TABLE", "INSERT INTO subscriptions",
                      "BY RANDOM PASSWORD", "GRANT SELECT", "ACCOUNT UNLOCK"):
            with self.subTest(phase=phase):
                files, queries = self.fake_apply(phase)
                with self.assertRaisesRegex(bootstrap.GateError, "SYNTHETIC_FAILURE"):
                    bootstrap.apply(self.bundle)
                self.assertIn(bootstrap.CLAIM_PATH, files)
                self.assertIn(bootstrap.ENV_PATH, files)
                self.assertNotIn(bootstrap.RECEIPT_PATH, files)
                before = len(queries)
                with self.assertRaises(FileExistsError):
                    bootstrap.apply(self.bundle)
                self.assertEqual(len(queries), before)

    def test_failure_before_apply_does_not_generate_or_write(self):
        with patch.object(bootstrap, "preflight", side_effect=bootstrap.GateError("EXISTING_DATABASE")), \
                patch.object(bootstrap, "create_private") as create:
            with self.assertRaises(bootstrap.GateError):
                bootstrap.apply(self.bundle)
            create.assert_not_called()
        self.entropy.assert_not_called()

    def test_environment_write_failure_keeps_account_locked(self):
        files, queries = self.fake_apply()
        def persist(stream, raw):
            if FAKE_PASSWORD.encode() in raw:
                raise OSError("synthetic-full-disk")
            stream.write(raw)
        with patch.object(bootstrap, "persist", side_effect=persist):
            with self.assertRaises(OSError):
                bootstrap.apply(self.bundle)
        self.assertTrue(any("ACCOUNT LOCK" in query for query, _ in queries))
        self.assertFalse(any("ACCOUNT UNLOCK" in query for query, _ in queries))
        self.assertNotIn(bootstrap.RECEIPT_PATH, files)

    def test_top_level_suppresses_unexpected_secret_bearing_exception(self):
        output = io.StringIO()
        with patch.object(bootstrap, "apply", side_effect=ValueError(FAKE_PASSWORD)), \
                redirect_stdout(output):
            self.assertEqual(bootstrap.main(["apply", "--root", str(ROOT)]), 2)
        self.assertNotIn(FAKE_PASSWORD, output.getvalue())
        self.assertIn("UNEXPECTED_FAILURE_PRESERVE_STATE", output.getvalue())

    def test_observation_requires_exact_tables_and_zero_nonplan_rows(self):
        with patch.object(bootstrap, "sql", return_value="users"):
            with self.assertRaisesRegex(bootstrap.GateError, "OBSERVATION_TABLES"):
                bootstrap.observe(self.bundle)
        def answer(statement):
            if "SELECT TABLE_NAME FROM" in statement:
                return "\n".join(self.bundle.tables)
            return "99"
        with patch.object(bootstrap, "sql", side_effect=answer):
            with self.assertRaisesRegex(bootstrap.GateError, "OBSERVATION_ROWS"):
                bootstrap.observe(self.bundle)

    def test_mutation_rechecks_expiry_before_every_native_call(self):
        with patch.object(bootstrap, "datetime", wraps=datetime) as clock, \
                patch.object(bootstrap, "sql") as query:
            clock.now.return_value = NOW.replace(hour=13)
            with self.assertRaisesRegex(bootstrap.GateError, "APPROVAL_EXPIRED"):
                bootstrap.mutate(approval(self.bundle), "CREATE DATABASE `atstudio_prod`;")
            query.assert_not_called()

    def test_absence_does_not_follow_dangling_links_and_rejects_uninspectable(self):
        target = Mock()
        target.lstat.return_value = SimpleNamespace(st_mode=stat.S_IFLNK | 0o777)
        with self.assertRaisesRegex(bootstrap.GateError, "EXISTING_PATH"):
            bootstrap.absent(target)
        target.lstat.side_effect = PermissionError("synthetic")
        with self.assertRaises(PermissionError):
            bootstrap.absent(target)
        target.lstat.side_effect = FileNotFoundError()
        bootstrap.absent(target)

    def test_secure_path_rejects_owner_write_modes_hardlinks_and_wrong_type(self):
        target = Mock()
        target.parents = []
        for uid, mode, links in ((1, stat.S_IFREG | 0o600, 1),
                                 (0, stat.S_IFREG | 0o620, 1),
                                 (0, stat.S_IFREG | 0o644, 1),
                                 (0, stat.S_IFREG | 0o600, 2),
                                 (0, stat.S_IFIFO | 0o600, 1)):
            target.stat.return_value = SimpleNamespace(st_uid=uid, st_mode=mode, st_nlink=links)
            with self.subTest(mode=mode, uid=uid, links=links), \
                    patch.object(bootstrap.validator, "no_links"):
                with self.assertRaises(bootstrap.GateError):
                    bootstrap.secure_path(target, private=True)

    def test_source_symlink_is_rejected_before_file_content_read(self):
        target = Mock()
        with patch.object(bootstrap.validator, "no_links", side_effect=bootstrap.validator.AuditError("FILE_LINK")):
            with self.assertRaises(bootstrap.validator.AuditError):
                bootstrap.read_public(target)
        target.open.assert_not_called()

    def test_exclusive_nofollow_file_creation_does_not_truncate(self):
        with patch.object(bootstrap, "secure_path"), \
                patch.object(bootstrap.os, "O_NOFOLLOW", 0x20000, create=True), \
                patch.object(bootstrap.os, "open", side_effect=FileExistsError()) as opened, \
                patch.object(bootstrap.os, "fdopen") as wrap:
            with self.assertRaises(FileExistsError):
                bootstrap.create_private(bootstrap.ENV_PATH)
            flags = opened.call_args.args[1]
            self.assertTrue(flags & os.O_EXCL)
            self.assertTrue(flags & os.O_NOFOLLOW)
            self.assertFalse(flags & os.O_TRUNC)
            self.assertEqual(opened.call_args.args[2], 0o600)
            wrap.assert_not_called()

    def test_host_guard_checks_units_processes_and_actual_mysql_listener(self):
        inactive = b"LoadState=masked\nActiveState=inactive\nUnitFileState=masked\n"
        def answer(argv):
            return b"LISTEN 0 151 127.0.0.1:3306 0.0.0.0:*\n" if "ss" in argv[0] else inactive
        with patch.object(bootstrap, "run_private", side_effect=answer), \
                patch.object(Path, "iterdir", return_value=iter([])):
            bootstrap.host_guard()
        for endpoint in ("0.0.0.0:3306", "[::]:3306", "127.0.0.1:33060",
                         "127.0.0.1:33062",
                         "127.0.0.1:3306 0.0.0.0:*\nLISTEN 0 151 127.0.0.1:33062"):
            with self.subTest(endpoint=endpoint), patch.object(
                    bootstrap, "run_private", side_effect=lambda argv:
                    f"LISTEN 0 151 {endpoint} 0.0.0.0:*\n".encode()
                    if "ss" in argv[0] else inactive), \
                    patch.object(Path, "iterdir", return_value=iter([])):
                with self.assertRaisesRegex(bootstrap.GateError, "MYSQL_LISTENER"):
                    bootstrap.host_guard()
        with patch.object(bootstrap, "run_private", return_value=inactive.replace(b"inactive", b"active")):
            with self.assertRaisesRegex(bootstrap.GateError, "APP_PROXY_NOT_FENCED"):
                bootstrap.host_guard()

    def test_observation_positive_returns_counts_only_not_manifest_acceptance(self):
        def answer(statement):
            if "SELECT TABLE_NAME FROM" in statement:
                return "\n".join(self.bundle.tables)
            if statement.startswith("SELECT COUNT(*) FROM `"):
                return "6" if "`subscriptions`" in statement else "0"
            if statement.startswith("SELECT COUNT(*)=6"):
                return "1"
            return "synthetic-structural-row"
        with patch.object(bootstrap, "sql", side_effect=answer):
            result = bootstrap.observe(self.bundle)
        self.assertEqual(result["tables"], 43)
        self.assertEqual(result["plans"], 6)
        self.assertEqual(result["non_plan_rows"], 0)
        self.assertNotIn("synthetic-structural-row", json.dumps(result))
        self.assertNotIn("canonical_fresh_manifest", result)


if __name__ == "__main__":
    unittest.main()
