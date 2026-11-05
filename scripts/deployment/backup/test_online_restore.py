"""Local, synthetic WI004 tests: no MySQL, restic, SSH or systemd execution."""
import copy
import io
import json
import os
from pathlib import Path
import tempfile
import time
import unittest
from unittest.mock import patch

import online_restore as r
from backup_protocol import GateError, digest, encoded
from backup_runtime import CommandResult, LinuxRunner

SQL = b"""-- MySQL dump 10.13
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `atstudio_prod` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `atstudio_prod`;
CREATE TABLE `alpha` (
  `id` bigint NOT NULL,
  `name` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
LOCK TABLES `alpha` WRITE;
/*!40000 ALTER TABLE `alpha` DISABLE KEYS */;
INSERT INTO `alpha` VALUES (1,'semicolon; CHARACTER SET utf8mb4');
INSERT INTO `alpha` VALUES (2,'second');
/*!40000 ALTER TABLE `alpha` ENABLE KEYS */;
UNLOCK TABLES;
-- Dump completed on 2026-10-30
"""


class SqlTests(unittest.TestCase):
    def test_empty_mysqldump_line_comments_lf_and_crlf(self):
        for separator in (b'--\n', b'--\r\n', b'-- ordinary\n', b'--\tordinary\n'):
            self.assertEqual(r.sql_model(separator+SQL,'atstudio_prod',['alpha']),r.sql_model(SQL,'atstudio_prod',['alpha']))

    def test_dash_sequences_in_literals_and_noncomments_remain_exact(self):
        self.assertEqual(r.tokens(b"'--\nsecret'"),[b"'--\nsecret'"])
        self.assertEqual(r.tokens(b'--x'),[b'-',b'-',b'x'])
        self.assertEqual(r.tokens(b'1--2'),[b'1',b'-',b'-',b'2'])

    def test_native_dump_header_and_dynamic_counts(self):
        proof = r.compare_sql(SQL, SQL.replace(b"2026-10-30", b"2026-10-31"), "atstudio_prod", ["alpha"])
        self.assertEqual(proof["tables"], 1)
        self.assertEqual(proof["rows"], {"alpha": 2})

    def test_known_charset_equivalence_not_fixed_count(self):
        restored = SQL.replace(b"varchar(40) COLLATE", b"varchar(40) CHARACTER SET utf8mb4 COLLATE")
        r.compare_sql(SQL, restored, "atstudio_prod", ["alpha"])

    def test_string_data_not_normalized(self):
        with self.assertRaisesRegex(GateError, "RESTORE_SQL_DIFFERENT"):
            r.compare_sql(SQL, SQL.replace(b"semicolon; CHARACTER SET utf8mb4", b"semicolon;"), "atstudio_prod", ["alpha"])

    def test_row_reordering_preserves_multiplicity(self):
        one = b"INSERT INTO `alpha` VALUES (1,'semicolon; CHARACTER SET utf8mb4');"
        two = b"INSERT INTO `alpha` VALUES (2,'second');"
        r.compare_sql(SQL, SQL.replace(one + b"\n" + two, two + b"\n" + one), "atstudio_prod", ["alpha"])
        with self.assertRaises(GateError):
            r.compare_sql(SQL, SQL.replace(two, one), "atstudio_prod", ["alpha"])

    def test_programmable_and_multistatement_comments_rejected(self):
        for addition in (b"DELIMITER ;;", b"CREATE EVENT `x` ON SCHEDULE EVERY 1 DAY DO SELECT 1;",
                         b"/*!50003 SET @x=1; DROP DATABASE `atstudio_prod` */;"):
            with self.assertRaises(GateError):
                r.sql_model(SQL + addition, "atstudio_prod", ["alpha"])

    def test_extra_table_and_missing_row_rejected(self):
        with self.assertRaises(GateError):
            r.compare_sql(SQL, SQL.replace(b"INSERT INTO `alpha` VALUES (2,'second');", b""), "atstudio_prod", ["alpha"])
        with self.assertRaises(GateError):
            r.sql_model(SQL, "atstudio_prod", ["alpha", "beta"])


def config():
    return {"version": 1, "operation": "online-isolated-restore", "restore_id": "b" * 32,
            "target_parent": r.RESTORE_PARENT, "target_root": r.RESTORE_PARENT + "/" + "b" * 32,
            "source_roots": ["/srv/atstudio", "/opt/atstudio", "/source/capture/" + "a" * 32],
            "capture_dir": "/source/capture/" + "a" * 32, "online_state_path": "/source/state.json",
            "capture_receipt_sha256": "c" * 64, "repository_id": "d" * 64,
            "capture_config_sha256": "7" * 64, "restore_module_sha256": "6" * 64,
            "machine_sha256": "e" * 64, "boot_sha256": "f" * 64, "approved_cost_krw": 0,
            "limits": {"retrieval_bytes": 288 * 1024**2, "dump_bytes": r.MAX_SQL,
                       "metadata_bytes": r.MAX_SQL, "seconds": 300},
            "proof": {"path": "/private/proof.json", "sha256": "a" * 64},
            "schema": "atstudio_prod", "tables": ["alpha"], "mysql_uid": 110, "mysql_gid": 112,
            "helper_allow_ips": ["192.0.2.10/32", "127.0.0.53/32"]}


class ContractTests(unittest.TestCase):
    def test_exact_approval_and_source_overlap(self):
        c = config()
        a = {"operation": c["operation"], "config_sha256": digest(encoded(c)),
             "not_before": "2026-10-30T00:00:00Z", "not_after": "2026-10-30T00:05:00Z"}
        r.validate(c, a, r.timestamp("2026-10-30T00:01:00Z"))
        c["source_roots"].append(c["target_parent"])
        a["config_sha256"] = digest(encoded(c))
        with self.assertRaisesRegex(GateError, "RESTORE_TARGET_OVERLAP"):
            r.validate(c, a, r.timestamp("2026-10-30T00:01:00Z"))

    def test_native_command_keeps_source_datadir_readonly(self):
        c = config()
        backend = r.NativeRestoreBackend(c, {}, object(), object())
        args = backend.unit_argv("db", Path(c["target_root"]) / "isolated-database")
        self.assertIn("--property=PrivateNetwork=yes", args)
        self.assertIn("--property=RestrictAddressFamilies=AF_UNIX", args)
        self.assertIn("--no-defaults", args)
        self.assertIn("--event-scheduler=DISABLED", args)
        self.assertIn("--secure-file-priv=NULL", args)
        self.assertTrue(any(x.startswith("--property=BindPaths=") and x.endswith(":/var/lib/mysql") for x in args))
        self.assertNotIn("--defaults-extra-file", " ".join(args))

    def test_restore_helper_has_actual_monitor_envelope(self):
        args = r.helper_argv(config(), "/private/code/online_restore.py", "/private/restore.json",
                             "/private/approval.json", "/private/online.json")
        for expected in ("--property=MemoryHigh=768M", "--property=MemoryMax=1G",
                         "--property=CPUQuota=100%", "--property=Nice=10", "--property=IPAddressDeny=any",
                         "--property=UnsetEnvironment=PYTHONPATH PYTHONHOME PYTHONSTARTUP PYTHONINSPECT PYTHONSAFEPATH",
                         "--property=IPAddressAllow=127.0.0.53/32 192.0.2.10/32"):
            self.assertIn(expected, args)
        self.assertEqual(args[-6:], ["--config", "/private/restore.json", "--approval", "/private/approval.json",
                                     "--capture-config", "/private/online.json"])

    def test_broad_egress_scope_rejected(self):
        c = config()
        for bad in ("0.0.0.0/0", "192.0.2.0/24", "::/0", "example.invalid"):
            c["helper_allow_ips"] = [bad]
            with self.assertRaisesRegex(GateError, "RESTORE_EGRESS_SCOPE"):
                r.helper_argv(c, "/private/online_restore.py", "/private/config.json",
                              "/private/approval.json", "/private/capture.json")

    def test_cleanup_continues_after_reader_failure_and_expired_monitor(self):
        class Runner:
            active = {}
            def stop_owned(self):
                raise RuntimeError("synthetic reader failure")
        backend = r.NativeRestoreBackend(config(), {}, Runner(), object())
        backend.client = Runner()
        backend.units = {"one.service", "two.service"}
        commands = []
        backend.show = lambda unit: {"LoadState": "loaded", "Description": backend.description, "MainPID": "0", "ControlPID": "0"}
        backend.command = lambda argv, *args: commands.append(argv)
        with self.assertRaisesRegex(GateError, "RESTORE_CLEANUP_UNCERTAIN"):
            backend.stop_owned()
        self.assertEqual(len(commands), 2)
        backend.monitor.tick()

    def test_bounded_input_rejects_links_and_oversize(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "x"
            path.write_bytes(b"abc")
            self.assertEqual(r.bounded_read(path, 3), b"abc")
            with self.assertRaises(GateError):
                r.bounded_read(path, 2)
            with patch.object(Path, "is_symlink", return_value=True), \
                    self.assertRaisesRegex(GateError, "RESTORE_INPUT_SYMLINK"):
                r.bounded_read(path, 3)


class StdinTests(unittest.TestCase):
    def test_real_local_child_receives_exact_stdin_bytes(self):
        import sys
        class Monitor:
            def tick(self, force=False): pass
        with tempfile.TemporaryDirectory() as temp:
            file = Path(temp) / "dump.sql"
            file.write_bytes(SQL)
            runner = LinuxRunner(Monitor(), dict(os.environ))
            with patch("backup_runtime.checked_path", lambda p, **kw: p), \
                 patch("backup_runtime.os.O_NOFOLLOW", 0, create=True), \
                 patch.object(runner, "group_exists", lambda p: False):
                result = runner.run([sys.executable, "-I", "-S", "-c",
                                     "import sys;sys.stdout.buffer.write(sys.stdin.buffer.read())"], len(SQL), input_path=file)
            self.assertEqual(result.code, 0)
            self.assertEqual(result.data, SQL)
            self.assertFalse(runner.active)

    def test_input_is_file_handle_and_closed_on_spawn_failure(self):
        class Monitor:
            def tick(self, force=False):
                pass
        with tempfile.TemporaryDirectory() as temp:
            file = Path(temp) / "dump.sql"
            file.write_bytes(SQL)
            handles = []
            def fail(*args, **kwargs):
                handles.append(kwargs["stdin"])
                self.assertEqual(kwargs["stdin"].read(), SQL)
                raise OSError("synthetic spawn failure")
            with patch("backup_runtime.checked_path", lambda p, **kw: p), \
                 patch("backup_runtime.os.O_NOFOLLOW", 0, create=True), \
                 patch("backup_runtime.subprocess.Popen", fail):
                with self.assertRaises(OSError):
                    LinuxRunner(Monitor(), {}).run(["fake-mysql"], 20, input_path=file)
            self.assertTrue(handles[0].closed)


class SessionTests(unittest.TestCase):
    def fixture(self, base):
        capture = base / ("a" * 32)
        capture.mkdir()
        dump = capture / "database.sql"
        dump.write_bytes(SQL)
        manifest = {"version": 1, "protocol": "ats-online-v1", "capture_id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    "config_sha256": "7" * 64,
                    "dump_sha256": digest(SQL), "manifest_sha256": "9" * 64,
                    "members": {str(dump): {"type": "file", "size": len(SQL), "sha256": digest(SQL),
                                           "mode": 0o600, "uid": 0, "gid": 0}}}
        (capture / "manifest.json").write_bytes(encoded(manifest))
        receipt = {"protocol": "ats-online-v1", "status": "VERIFIED", "restore_verified": False,
                   "helpers_ended": True, "inventory_verified": True, "content_verified": True,
                   "run_id": capture.name, "repository_id": "d" * 64, "snapshot_id": "8" * 64,
                   "capture_id": manifest["capture_id"], "manifest_sha256": "9" * 64,
                   "dump_sha256": digest(SQL), "content_manifest_sha256": digest(encoded(manifest))}
        (capture / "verified-receipt.json").write_bytes(encoded(receipt))
        prep = {k: receipt[k] for k in ("capture_id", "dump_sha256", "content_manifest_sha256", "snapshot_id", "repository_id")}
        prep.update(execution_authorized=False, restore_verified=False)
        (capture / "restore-preparation.json").write_bytes(encoded(prep))
        state = base / "state.json"
        state.write_bytes(encoded({"stage": "COMPLETE", "run_id": receipt["run_id"], "capture_id": receipt["capture_id"],
                                  "last_good": receipt, "receipt_sha256": digest(encoded(receipt))}))
        c = config()
        c.update(capture_dir=str(capture), online_state_path=str(state),
                 capture_receipt_sha256=digest(encoded(receipt)), target_root=str(base / "fresh"))
        class Backend:
            environment = "SYNTHETIC"
            cleanup = False
            calls = []
            def now(self): return time.time()
            def tick(self): pass
            def preflight(self, c, a): pass
            def verify_snapshot(self, receipt, members, directory):
                self.calls.append(("verify", receipt["snapshot_id"]))
            def fetch(self, snapshot, source, output, cap):
                self.calls.append(("fetch", snapshot))
                output.write_bytes(SQL)
            def roundtrip(self, dump, target): return SQL
            def stop_owned(self): self.cleanup = True
            def ended(self): return self.cleanup
        return c, capture, Backend()

    def run_session(self, c, capture, backend):
        # Windows stage harness only: native path/authority gates are tested separately.
        with patch.object(r, "validate", lambda *a: None), patch.object(r, "linux_path", lambda p: p):
            return r.RestoreSession(c, {}, capture, backend).run()

    def test_complete_stage_retrieves_exact_snapshot_and_separate_simulated_receipt(self):
        with tempfile.TemporaryDirectory() as temp:
            c, capture, backend = self.fixture(Path(temp))
            original = (capture / "verified-receipt.json").read_bytes()
            result = self.run_session(c, capture, backend)
            self.assertEqual(result["status"], "SIMULATED_RESTORE_VERIFIED")
            self.assertFalse(result["restore_verified"])
            self.assertTrue(result["cleanup"])
            self.assertEqual(result["files_verified"], 1)
            self.assertEqual(original, (capture / "verified-receipt.json").read_bytes())
            self.assertTrue(all(snapshot == "8" * 64 for _, snapshot in backend.calls))

    def test_bad_restored_bytes_hold_and_preserve(self):
        with tempfile.TemporaryDirectory() as temp:
            c, capture, backend = self.fixture(Path(temp))
            backend.fetch = lambda s, p, output, cap: output.write_bytes(b"bad")
            result = self.run_session(c, capture, backend)
            self.assertEqual(result["status"], "HOLD")
            self.assertTrue(result["cleanup"])
            self.assertTrue((Path(c["target_root"]) / "files/0.blob").exists())

    def test_cleanup_failure_never_upgrades_to_success(self):
        with tempfile.TemporaryDirectory() as temp:
            c, capture, backend = self.fixture(Path(temp))
            backend.ended = lambda: False
            result = self.run_session(c, capture, backend)
            self.assertEqual(result["status"], "HOLD")
            self.assertFalse(result["restore_verified"])

    def test_hold_capture_and_preexisting_target_block_before_retrieval(self):
        with tempfile.TemporaryDirectory() as temp:
            c, capture, backend = self.fixture(Path(temp))
            Path(c["target_root"]).mkdir()
            with self.assertRaisesRegex(GateError, "RESTORE_TARGET_EXISTS"):
                self.run_session(c, capture, backend)
            state = json.loads(Path(c["online_state_path"]).read_bytes())
            state["stage"] = "HOLD"
            Path(c["online_state_path"]).write_bytes(encoded(state))
            with self.assertRaisesRegex(GateError, "RESTORE_CAPTURE_NOT_COMPLETE"):
                self.run_session(c, capture, backend)

    def test_manifest_mutation_blocks_before_target_creation(self):
        with tempfile.TemporaryDirectory() as temp:
            c, capture, backend = self.fixture(Path(temp))
            manifest = json.loads((capture / "manifest.json").read_bytes())
            manifest["dump_sha256"] = "0" * 64
            (capture / "manifest.json").write_bytes(encoded(manifest))
            with self.assertRaisesRegex(GateError, "RESTORE_MANIFEST_BINDING"):
                self.run_session(c, capture, backend)
            self.assertFalse(Path(c["target_root"]).exists())


if __name__ == "__main__":
    unittest.main()
