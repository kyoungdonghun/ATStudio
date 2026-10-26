"""Offline WI013 tests: synthetic files and fake helpers, never real services."""

from __future__ import annotations

import contextlib
import io
import json
import stat
import tempfile
import time
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import backup_protocol as protocol
import backup_runtime as runtime
import guarded_backup as app

SNAPSHOT = "a" * 64
OLD = "b" * 64
REPOSITORY = "c" * 64
DUMP = b"-- Synthetic schema and rows, no real database\n"


def result(data: bytes, code: int = 0) -> runtime.CommandResult:
    return runtime.CommandResult(code, data, len(data), protocol.digest(data))


def config_fixture() -> dict:
    config = json.loads((Path(__file__).resolve().parents[3] /
                         "deploy/single-vps/backup/config.example.json").read_text())
    config.update(configured=True, target_host="synthetic", logical_host="synthetic",
                  owner_ref="owner", alert_ref="recipient", recovery_ref="recovery",
                  source_revision="synthetic-revision", dirty_disposition="reviewed",
                  schema_ref="synthetic-schema", repository_id=REPOSITORY,
                  repository="s3:https://storage.example.invalid/private/ats", region="test",
                  restic_sha256=SNAPSHOT, hook_sha256=SNAPSHOT,
                  release_root="/opt/atstudio/releases/fixture",
                  settings=["/etc/atstudio/settings"], expected_filesystems={"/srv": 1})
    config["limits"].update(source_bytes=1000000, retrieval_bytes=1000000, max_entries=1000,
                            upload_kib=100, download_kib=100, seconds=300,
                            cost_limit_krw=100, projected_cost_krw=1)
    return config


class ProtocolTests(unittest.TestCase):
    def test_json_lines_summary_and_omitted_zero_fields(self) -> None:
        data = b'{"message_type":"status"}\n' + protocol.encoded({
            "message_type": "summary", "snapshot_id": SNAPSHOT})
        self.assertEqual(protocol.backup_snapshot(data, 0), SNAPSHOT)

    def test_all_nonzero_exit_codes_fail(self) -> None:
        for code in (1, 2, 3, 10, 11, 12, 130, -9, 99, None):
            with self.subTest(code=code), self.assertRaises(protocol.GateError):
                protocol.backup_snapshot(protocol.encoded({
                    "message_type": "summary", "snapshot_id": SNAPSHOT}), code)

    def test_invalid_summary_forms(self) -> None:
        cases = [b"[]", b"not-json", b'{"message_type":"status"}',
                 protocol.encoded({"message_type": "summary", "snapshot_id": "latest"}),
                 protocol.encoded({"message_type": "summary", "snapshot_id": SNAPSHOT[:8]}),
                 protocol.encoded({"message_type": "summary", "snapshot_id": SNAPSHOT,
                                   "dry_run": True}),
                 b'{"message_type":"status","error_count":1}\n' + protocol.encoded({
                     "message_type": "summary", "snapshot_id": SNAPSHOT})]
        for data in cases:
            with self.subTest(data=data), self.assertRaises(protocol.GateError):
                protocol.backup_snapshot(data, 0)

    def test_ls_modern_and_deprecated_json_lines(self) -> None:
        for field in ("message_type", "struct_type"):
            data = protocol.encoded({field: "snapshot", "id": SNAPSHOT}) + b"\n"
            data += protocol.encoded({field: "node", "path": "/public", "type": "dir"})
            self.assertIn("/public", protocol.snapshot_nodes(data, SNAPSHOT))
        with self.assertRaises(protocol.GateError):
            protocol.snapshot_nodes(b"[]", SNAPSHOT)

    def test_ls_requires_inventory_exact_snapshot_and_unique_nodes(self) -> None:
        header = protocol.encoded({"message_type": "snapshot", "id": SNAPSHOT})
        node = protocol.encoded({"message_type": "node", "path": "/p", "type": "dir"})
        for data in (header, node, header + b"\n" + node + b"\n" + node,
                     header.replace(SNAPSHOT.encode(), OLD.encode()) + b"\n" + node):
            with self.assertRaises(protocol.GateError):
                protocol.snapshot_nodes(data, SNAPSHOT)

    def test_go_mode_directory_bit_and_rejected_special_bits(self) -> None:
        expected = {"/root": {"type": "dir", "mode": 0o700, "uid": 0, "gid": 0}}
        node = {"type": "dir", "mode": (1 << 31) | 0o700}
        protocol.verify_nodes(expected, {"/root": node})
        for bit in (1 << 23, 1 << 22, 1 << 20):
            with self.assertRaisesRegex(protocol.GateError, "SPECIAL_MODE_REVIEW_REQUIRED"):
                protocol.verify_nodes(expected, {"/root": {**node, "mode": node["mode"] | bit}})

    def test_ls_does_not_invent_symlink_target_verification(self) -> None:
        expected = {"/link": {"type": "symlink", "mode": 0o777, "uid": 0, "gid": 0}}
        with self.assertRaisesRegex(protocol.GateError, "SOURCE_SYMLINK_REVIEW_REQUIRED"):
            protocol.verify_nodes(expected, {"/link": {"type": "symlink", "mode": 0o777}})

    def test_config_and_exact_approval(self) -> None:
        config = config_fixture()
        protocol.validate_config(config)
        approval = {"operation": "cold-capture", "config_sha256": protocol.digest(
            protocol.encoded(config)), "run_id": "1" * 32, "resume_authorized": True,
            "not_before": "2026-10-10T00:00:00+00:00", "not_after": "2026-10-10T00:04:00Z"}
        now = protocol.timestamp("2026-10-10T00:02:00Z")
        protocol.validate_approval(config, approval, now)
        for key, value in (("config_sha256", OLD), ("resume_authorized", False),
                           ("not_after", "2026-10-10T00:01:00Z"), ("run_id", "../x")):
            with self.subTest(key=key), self.assertRaises(protocol.GateError):
                protocol.validate_approval(config, {**approval, key: value}, now)

    def test_unconfigured_unsafe_paths_repositories_and_budgets(self) -> None:
        for key, value in (("configured", False), ("public_root", "/"),
                           ("private_root", "/srv/atstudio/public/child"),
                           ("cache_dir", "/srv/atstudio/public/cache"),
                           ("password_file", "/srv/atstudio/public/password"),
                           ("repository", "s3:http://example.invalid/b/p"),
                           ("repository", "s3:https://user:password@example.invalid/b/p"),
                           ("lock_file", "/run/lock/other.lock")):
            config = config_fixture()
            config[key] = value
            with self.subTest(key=key), self.assertRaises(protocol.GateError):
                protocol.validate_config(config)
        config = config_fixture()
        config["limits"]["dump_bytes"] = 3 * protocol.GIB
        with self.assertRaises(protocol.GateError):
            protocol.validate_config(config)

    def test_status_failed_missed_and_stale_independent_of_application(self) -> None:
        now = protocol.timestamp("2026-10-10T10:00:00Z")
        state = {"attempt": {"stage": "FAILED", "maintenance": True},
                 "last_good": {"snapshot_id": OLD, "captured_at": "2026-10-09T07:00:00Z"}}
        report = protocol.status_report(state, now, now - 300, None, now - 600)
        self.assertEqual(set(report["alerts"]), {"CAPTURE_FAILED", "CAPTURE_MISSED",
            "MAINTENANCE_REQUIRES_OPERATOR", "CAPTURE_26H_STALE", "INDEPENDENT_COPY_8D_STALE"})

    def test_status_does_not_miss_newer_success_or_trust_copy_completion_time(self) -> None:
        now = protocol.timestamp("2026-10-10T10:00:00Z")
        state = {"last_good": {"snapshot_id": SNAPSHOT, "captured_at": "2026-10-10T09:00:00Z"}}
        receipt = {"independently_verified": True, "source_snapshot_id": SNAPSHOT,
                   "destination_snapshot_id": OLD, "captured_at": "2026-10-01T09:00:00Z"}
        report = protocol.status_report(state, now, now - 300, receipt, now - 7200)
        self.assertEqual(report["alerts"], ["INDEPENDENT_COPY_8D_STALE"])

    def test_restore_stays_preparatory_and_requires_scheduler_gate(self) -> None:
        plan = {key: True for key in ("new_isolated_target", "fresh_database", "new_roots",
                "egress_denied", "services_disabled", "timers_disabled",
                "scheduler_suppression_proved", "no_effective_live_credentials",
                "exact_target_approved")}
        plan.update(snapshot_id=SNAPSHOT, target="/isolated/new", byte_budget=100)
        protocol.restore_gate(plan)
        plan["scheduler_suppression_proved"] = False
        with self.assertRaises(protocol.GateError):
            protocol.restore_gate(plan)


class FakeRunner:
    def __init__(self, config: dict, failure: str | None = None) -> None:
        self.config = config
        self.failure = failure
        self.calls: list[str] = []
        self.active: dict = {}
        self.stopped = False
        self.expected: dict = {}

    def stop_owned(self) -> None:
        self.stopped = True
        self.active.clear()

    def run(self, argv: list[str], max_bytes: int, output: Path | None = None,
            *, hash_only: bool = False) -> runtime.CommandResult:
        if argv[0] == self.config["hook_binary"]:
            action = argv[1]
            self.calls.append(action)
            if action == self.failure:
                return result(b"private information must never escape", 99)
            if action == "logical-dump":
                runtime.create_bytes(output, DUMP)
                return result(DUMP)
            if self.failure == "unconfigured":
                return result(b'{"configured":false}')
            proof = {key: True for key in app.HOOK_CHECKS[action]}
            proof.update(configured=True, dump_sha256=protocol.digest(DUMP))
            return result(protocol.encoded(proof))
        action = next(arg for arg in argv if arg in {"cat", "backup", "ls", "dump"})
        self.calls.append(action)
        if self.failure == "timeout" and action == "backup":
            self.active[123] = object()
            raise protocol.GateError("WINDOW_EXPIRED")
        if action == "cat":
            return result(protocol.encoded({"id": REPOSITORY, "version": 2}))
        if action == "backup":
            paths = [Path(arg) for arg in argv[argv.index("--") + 1:]]
            self.expected = runtime.inventory(paths, lambda: None, 1000, 1000000, 1000000,
                                               {str(p): p.stat().st_dev for p in paths})
            code = 3 if self.failure == "partial" else 99 if self.failure == "unknown" else 0
            return result(protocol.encoded({"message_type": "summary", "snapshot_id": SNAPSHOT}), code)
        if action == "ls":
            header = protocol.encoded({"message_type": "snapshot", "id": SNAPSHOT})
            if self.failure == "no-inventory":
                return result(header)
            nodes = [{"message_type": "node", "path": path,
                      **{key: value for key, value in row.items()
                         if key in {"type", "size", "mode", "uid", "gid"}}}
                     for path, row in self.expected.items()]
            if self.failure == "missing-member":
                nodes.pop()
            if self.failure == "bad-size":
                next(row for row in nodes if row["type"] == "file")["size"] += 1
            return result(header + b"\n" + b"\n".join(protocol.encoded(row) for row in nodes))
        path = Path(argv[-1])
        require_name = path.name in {"database.sql", "manifest.json"}
        if not require_name:
            raise AssertionError("Cold window must not retrieve media")
        self.calls.append("retrieved:" + path.name)
        data = path.read_bytes()
        if self.failure == "hash-mismatch":
            data = b"wrong"
        return result(data)


class RecurringTests(unittest.TestCase):
    def setUp(self) -> None:
        self.config = config_fixture()
        self.now = protocol.timestamp("2026-10-10T03:00:15+09:00")
        self.policy = {"enabled": True, "operation": "daily-cold-capture",
                       "config_sha256": protocol.digest(protocol.encoded(self.config)),
                       "timezone": "Asia/Seoul", "start_hhmm": "03:00",
                       "start_grace_seconds": 30, "window_seconds": 240,
                       "not_before": "2026-10-10T00:00:00+09:00",
                       "expires_at": "2026-10-12T00:00:00+09:00", "resume_authorized": True}

    def test_daily_id_is_stable_within_day_unique_on_next_day(self) -> None:
        first = protocol.recurring_approval(self.config, self.policy, self.now)
        same = protocol.recurring_approval(self.config, self.policy, self.now + 1)
        tomorrow = protocol.recurring_approval(self.config, self.policy, self.now + 86400)
        self.assertEqual(first["run_id"], same["run_id"])
        self.assertNotEqual(first["run_id"], tomorrow["run_id"])
        protocol.validate_approval(self.config, first, self.now)

    def test_default_off_and_exact_config_policy_gates(self) -> None:
        for key, value in (("enabled", False), ("config_sha256", OLD),
                           ("timezone", "UTC"), ("resume_authorized", False),
                           ("start_hhmm", "25:00"), ("start_grace_seconds", 61),
                           ("window_seconds", 301)):
            with self.subTest(key=key), self.assertRaises(protocol.GateError):
                protocol.recurring_approval(self.config, {**self.policy, key: value}, self.now)

    def test_no_catchup_before_after_or_expired_window(self) -> None:
        for shift in (-60, 30, 3600, 2 * 86400):
            with self.subTest(shift=shift), self.assertRaises(protocol.GateError):
                protocol.recurring_approval(self.config, self.policy, self.now + shift)

    def test_expiry_must_cover_entire_capture_not_only_start(self) -> None:
        self.policy["expires_at"] = "2026-10-10T03:01:00+09:00"
        with self.assertRaisesRegex(protocol.GateError, "RECURRING_EXPIRED"):
            protocol.recurring_approval(self.config, self.policy, self.now)


class CaptureTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory(prefix="ats-wi013-synthetic-")
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.config = config_fixture()
        for key in ("public_root", "private_root", "release_root", "work_dir", "state_dir"):
            path = self.root / key
            path.mkdir()
            self.config[key] = str(path)
        self.config["release_link"] = self.config["release_root"]
        settings = self.root / "settings"
        settings.write_bytes(b"synthetic settings, no credentials")
        self.config["settings"] = [str(settings)]
        (self.root / "public_root" / "original.wav").write_bytes(b"synthetic media")
        (self.root / "public_root" / ".staging").mkdir()
        (self.root / "release_root" / "application.jar").write_bytes(b"synthetic release")
        self.store = app.StateStore(self.root / "state_dir")
        self.old_good = {"snapshot_id": OLD, "captured_at": "2026-10-09T00:00:00Z"}
        self.store.save({"version": 1, "attempt": {"stage": "COMPLETE"}, "last_good": self.old_good})
        self.approval = {"run_id": "1" * 32, "resume_authorized": True}
        self.parser_patch = patch.object(app, "snapshot_nodes", side_effect=self.parse_fixture_nodes)
        self.parser_patch.start()
        self.addCleanup(self.parser_patch.stop)

    @staticmethod
    def parse_fixture_nodes(data: bytes, snapshot: str) -> dict:
        # Map Windows fixture paths to Linux aliases around the actual parser.
        rows = protocol.documents(data)
        mapping = {}
        for index, row in enumerate(rows):
            if "path" in row:
                alias = "/fixture/member-" + str(index)
                mapping[alias] = row["path"]
                row["path"] = alias
        parsed = protocol.snapshot_nodes(b"\n".join(protocol.encoded(row) for row in rows), snapshot)
        return {mapping[path]: row for path, row in parsed.items()}

    def capture(self, failure: str | None = None) -> tuple[app.Capture, FakeRunner]:
        runner = FakeRunner(self.config, failure)
        return app.Capture(self.config, self.root / "config.json", self.approval,
                           runner, self.store, lambda: None), runner

    def test_success_exact_snapshot_inventory_and_only_two_retrievals(self) -> None:
        capture, runner = self.capture()
        self.assertEqual(capture.run()["status"], "COMPLETE")
        state = self.store.load()
        self.assertEqual(state["last_good"]["snapshot_id"], SNAPSHOT)
        self.assertFalse(state["attempt"]["maintenance"])
        self.assertFalse(state["last_good"]["restore_verified"])
        self.assertEqual([call for call in runner.calls if call.startswith("retrieved:")],
                         ["retrieved:database.sql", "retrieved:manifest.json"])
        self.assertLess(runner.calls.index("backup"), runner.calls.index("resume"))
        manifest = runtime.read_json(capture.run_dir / "manifest.json")
        self.assertIn(str(self.root / "private_root"), manifest["members"])
        self.assertIn(str(self.root / "public_root" / ".staging"), manifest["members"])
        self.assertTrue((capture.run_dir / "receipt.json").is_file())

    def test_failures_preserve_old_good_and_never_resume(self) -> None:
        for index, failure in enumerate(("logical-dump", "validate-dump", "partial", "unknown",
                                        "timeout", "no-inventory", "missing-member", "bad-size",
                                        "hash-mismatch", "assert-quiescent", "close-admission")):
            with self.subTest(failure=failure):
                self.approval["run_id"] = format(index + 2, "032x")
                self.store.save({"version": 1, "attempt": {"stage": "COMPLETE"},
                                 "last_good": self.old_good})
                capture, runner = self.capture(failure)
                with self.assertRaises(protocol.GateError):
                    capture.run()
                state = self.store.load()
                self.assertEqual(state["last_good"], self.old_good)
                self.assertEqual(state["attempt"]["stage"], "FAILED")
                self.assertTrue(state["attempt"]["maintenance"])
                self.assertNotIn("resume", runner.calls)
                self.assertFalse(runner.active)
                self.assertTrue(runner.stopped)

    def test_unconfigured_hook_fails_before_maintenance(self) -> None:
        capture, runner = self.capture("unconfigured")
        with self.assertRaisesRegex(protocol.GateError, "HOST_HOOK_NOT_CONFIGURED"):
            capture.run()
        self.assertFalse(self.store.load()["attempt"]["maintenance"])
        self.assertEqual(runner.calls, ["preflight"])

    def test_existing_dump_run_directory_is_never_reused(self) -> None:
        capture, runner = self.capture()
        capture.run_dir.mkdir()
        (capture.run_dir / "database.sql").write_bytes(b"stale")
        with self.assertRaises(FileExistsError):
            capture.run()
        self.assertEqual(runner.calls, [])
        self.assertEqual((capture.run_dir / "database.sql").read_bytes(), b"stale")

    def test_prior_maintenance_requires_operator(self) -> None:
        self.store.save({"version": 1, "attempt": {"stage": "FAILED", "maintenance": True},
                         "last_good": self.old_good})
        capture, runner = self.capture()
        with self.assertRaisesRegex(protocol.GateError, "PRIOR_MAINTENANCE"):
            capture.run()
        self.assertEqual(runner.calls, [])

    def test_atomic_replace_failure_preserves_previous_bytes(self) -> None:
        before = self.store.path.read_bytes()
        with patch.object(runtime.os, "replace", side_effect=OSError("private path")):
            with self.assertRaises(OSError):
                self.store.save({"version": 1, "last_good": None})
        self.assertEqual(before, self.store.path.read_bytes())

    def test_manifest_retrieval_budget_precedes_capture(self) -> None:
        self.config["limits"]["retrieval_bytes"] = 1
        capture, runner = self.capture()
        with self.assertRaisesRegex(protocol.GateError, "RETRIEVAL_BUDGET"):
            capture.run()
        self.assertNotIn("backup", runner.calls)
        self.assertNotIn("resume", runner.calls)

    def test_failed_last_good_publication_keeps_old_good(self) -> None:
        capture, runner = self.capture()
        original_save = self.store.save

        def fail_complete(state: dict) -> None:
            if state["attempt"]["stage"] == "COMPLETE":
                raise OSError("synthetic full disk")
            original_save(state)

        with patch.object(self.store, "save", side_effect=fail_complete), \
                self.assertRaises(protocol.GateError):
            capture.run()
        self.assertEqual(self.store.load()["last_good"], self.old_good)
        self.assertEqual(self.store.load()["attempt"]["stage"], "FAILED")
        self.assertEqual(runner.calls.count("resume"), 1)


class RuntimeTests(unittest.TestCase):
    def test_default_plan_is_offline_without_any_config(self) -> None:
        with patch.object(runtime.subprocess, "Popen") as spawn, \
                patch.object(app, "read_json") as read, \
                contextlib.redirect_stdout(io.StringIO()) as output:
            self.assertEqual(app.main([]), 0)
        spawn.assert_not_called()
        read.assert_not_called()
        self.assertEqual(json.loads(output.getvalue())["status"], "NOT_CONFIGURED")

    def test_error_output_does_not_expose_path_or_exception(self) -> None:
        with patch.object(app, "read_json", side_effect=OSError("SECRET/REAL/PATH")), \
                contextlib.redirect_stdout(io.StringIO()) as output:
            self.assertEqual(app.main(["validate", "--config", "anything"]), 2)
        self.assertNotIn("SECRET", output.getvalue())

    def test_owned_process_group_is_stopped_and_other_groups_untouched(self) -> None:
        runner = runtime.LinuxRunner(MagicMock(), {})
        process = MagicMock()
        runner.active = {42: process}
        with patch.object(runner, "group_exists", side_effect=[True, False, False, False]), \
                patch.object(runtime.signal, "SIGKILL", 9, create=True), \
                patch.object(runtime.os, "killpg", create=True) as kill:
            runner.stop_owned()
        kill.assert_called_once_with(42, runtime.signal.SIGTERM)
        self.assertFalse(runner.active)

    def test_helper_group_not_confirmed_dead_blocks(self) -> None:
        runner = runtime.LinuxRunner(MagicMock(), {})
        process = MagicMock()
        process.wait.side_effect = runtime.subprocess.TimeoutExpired("synthetic", 2)
        runner.active = {42: process}
        with patch.object(runner, "group_exists", return_value=True), \
                patch.object(runtime.signal, "SIGKILL", 9, create=True), \
                patch.object(runtime.os, "killpg", create=True), \
                self.assertRaisesRegex(protocol.GateError, "HELPERS_STILL_RUNNING"):
            runner.stop_owned()
        self.assertIn(42, runner.active)

    def test_symlink_ancestor_escape_is_rejected(self) -> None:
        with patch.object(Path, "lstat", return_value=SimpleNamespace(st_mode=stat.S_IFLNK)), \
                self.assertRaisesRegex(protocol.GateError, "PATH_SYMLINK"):
            runtime.checked_path(Path(__file__).absolute())

    def test_source_symlink_escape_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory(prefix="ats-wi013-link-") as directory:
            root = Path(directory)
            link = root / "link"
            with patch.object(Path, "lstat", return_value=SimpleNamespace(
                    st_dev=1, st_mode=stat.S_IFLNK, st_uid=0, st_gid=0)), \
                    patch.object(Path, "resolve", return_value=root.parent), \
                    self.assertRaisesRegex(protocol.GateError, "SOURCE_SYMLINK_ESCAPE"):
                runtime.inventory([link], lambda: None, 10, 100, 1000, {str(link): 1})

    def test_low_disk_low_inodes_and_deadline(self) -> None:
        monitor = runtime.LinuxMonitor.__new__(runtime.LinuxMonitor)
        monitor.config = config_fixture()
        monitor.deadline = time.time() + 100
        monitor.monotonic_end = time.monotonic() + 100
        monitor.last_scan = 0
        checked = MagicMock()
        checked.stat.return_value.st_dev = 1
        for available, inodes, code in ((1, 500, "DISK_RESERVE"),
                                        (100 * protocol.GIB, 1, "INODE_RESERVE")):
            space = SimpleNamespace(f_blocks=100 * protocol.GIB, f_bavail=available,
                                    f_frsize=1, f_files=1000, f_favail=inodes)
            with patch.object(runtime, "checked_path", return_value=checked), \
                    patch.object(runtime.os, "statvfs", return_value=space, create=True), \
                    self.assertRaisesRegex(protocol.GateError, code):
                monitor.tick(force=True, projected=True)
        monitor.deadline = 0
        with self.assertRaisesRegex(protocol.GateError, "WINDOW_EXPIRED"):
            monitor.tick()

    def test_shared_lock_conflict_never_enters_body(self) -> None:
        fake = MagicMock()
        fake.flock.side_effect = BlockingIOError()
        with patch.dict("sys.modules", {"fcntl": fake}), \
                patch.object(runtime, "checked_path"), \
                patch.object(runtime.os, "O_NOFOLLOW", 0, create=True), \
                patch.object(runtime.os, "open", return_value=42), \
                patch.object(runtime.os, "close") as close, \
                patch.object(runtime.os, "fstat", return_value=SimpleNamespace(
                    st_mode=stat.S_IFREG | 0o600, st_uid=0, st_nlink=1)), \
                self.assertRaisesRegex(protocol.GateError, "OPERATIONS_BUSY"):
            with runtime.operations_lock(Path("/synthetic/lock")):
                self.fail("lock conflict admitted a run")
        close.assert_called_once_with(42)

    def test_command_and_environment_cannot_inherit_insecure_flags(self) -> None:
        config = config_fixture()
        env = runtime.restic_environment(config)
        self.assertNotIn("RESTIC_PASSWORD", env)
        self.assertNotIn("AWS_SECRET_ACCESS_KEY", env)
        self.assertEqual(env["GOMAXPROCS"], "1")
        for operation in ("init", "unlock", "forget", "prune", "restore", "copy"):
            with self.assertRaises(protocol.GateError):
                runtime.restic_command(config, [operation])

    def test_runner_stream_hash_and_output_limit_without_spawning(self) -> None:
        for cap in (100, 1):
            runner = runtime.LinuxRunner(MagicMock(), {})
            process = MagicMock()
            process.pid = 42
            process.stdout = io.BytesIO(b"synthetic stdout")
            process.poll.return_value = 0
            process.returncode = 0
            with patch.object(runtime.subprocess, "Popen", return_value=process) as spawn, \
                    patch.object(runner, "group_exists", return_value=False), \
                    patch.object(runtime.signal, "SIGKILL", 9, create=True):
                if cap == 1:
                    with self.assertRaisesRegex(protocol.GateError, "COMMAND_OUTPUT_LIMIT"):
                        runner.run(["synthetic"], cap, hash_only=True)
                else:
                    reply = runner.run(["synthetic"], cap, hash_only=True)
                    self.assertEqual(reply.sha256, protocol.digest(b"synthetic stdout"))
                    self.assertEqual(reply.data, b"")
            self.assertFalse(runner.active)
            self.assertEqual(spawn.call_args.kwargs["stderr"], runtime.subprocess.DEVNULL)
            self.assertTrue(spawn.call_args.kwargs["start_new_session"])

    def test_runner_timeout_stops_owned_group(self) -> None:
        monitor = MagicMock()
        monitor.tick.side_effect = [None, protocol.GateError("WINDOW_EXPIRED")]
        runner = runtime.LinuxRunner(monitor, {})
        process = MagicMock(pid=42)
        process.stdout = io.BytesIO(b"fixture")
        process.poll.return_value = None
        with patch.object(runtime.subprocess, "Popen", return_value=process), \
                patch.object(runner, "group_exists", return_value=False), \
                patch.object(runtime.signal, "SIGKILL", 9, create=True), \
                self.assertRaisesRegex(protocol.GateError, "WINDOW_EXPIRED"):
            runner.run(["synthetic"], 100)
        self.assertFalse(runner.active)


if __name__ == "__main__":
    unittest.main()
