"""WI019 synthetic filesystem/command integration. Never starts native services."""
import copy
import contextlib
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from backup_protocol import GateError, digest, encoded
from host_adapter import ColdHost, UNITS, MASKED, verify_host_receipts


class HostTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)
        self.root = self.base / "fs"
        self.root.mkdir()
        self.work = self.base / "work"
        self.work.mkdir()
        self.run_dir = self.work / ("a" * 32)
        self.run_dir.mkdir()
        self.fs("/proc").mkdir(parents=True)
        self.fs("/data/mysql").mkdir(parents=True)
        for path, value in (("/etc/machine-id", b"machine"), ("/proc/sys/kernel/random/boot_id", b"boot"),
                            ("/etc/quarantine", b"quarantined")):
            self.file(path, value)
        pins = {}
        for path in ("/usr/bin/systemctl", "/usr/bin/systemd-run", "/usr/sbin/mysqld",
                     "/usr/bin/mysql", "/usr/bin/mysqldump", "/etc/apparmor.d/usr.sbin.mysqld", "/etc/mysql-cold.cnf"):
            self.file(path, path.encode())
            pins[path] = digest(path.encode())
        units = {name: {"LoadState": "loaded", "ActiveState": "inactive", "SubState": "dead",
                       "MainPID": "0", "ControlPID": "0", "UnitFileState": "masked" if name in MASKED else "disabled"}
                 for name in UNITS}
        units["nginx.service"].update(ActiveState="active", SubState="running", MainPID="2", UnitFileState="enabled")
        self.config = {"maintenance_mode": "cold-preserve", "work_dir": str(self.work),
                       "limits": {"dump_bytes": 100000, "seconds": 60}, "cold_host": {
                           "schema": "synthetic", "tables": [f"t{x:02}" for x in range(43)],
                           "machine_sha256": digest(b"machine"), "boot_sha256": digest(b"boot"),
                           "artifact_pins": pins, "baseline_units": units,
                           "mysql_config": "/etc/mysql-cold.cnf", "datadir": "/data/mysql",
                           "mysql_identity": {"uid": 1200, "gid": 1201},
                           "mysql_log_directory": {"uid": 1200, "gid": 4, "mode": 0o750, "device": 1, "inode": 2},
                           "mysql_log_parent": {"uid": 0, "gid": 100, "mode": 0o775, "device": 1, "inode": 3},
                           "quarantine_file": "/etc/quarantine", "quarantine_sha256": digest(b"quarantined")}}
        self.running = False
        self.fail_start = False
        self.calls = []
        self.host = ColdHost(self.config, self.run_dir, self, filesystem=self.fs, verifier=lambda c, r: None)
        self.host.prepare_log = self.fake_log

    def fake_log(self):
        path = self.fs(str(self.host.log))
        path.parent.mkdir(parents=True, exist_ok=True)
        with path.open("xb") as stream:
            stream.write(b"synthetic retained log")

    def fs(self, path):
        if str(path) == "/proc/42/root/data/mysql":
            return self.root / "data/mysql"
        return self.root / str(path).replace("\\", "/").lstrip("/")

    def file(self, path, data):
        p = self.fs(path)
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_bytes(data)

    def command(self, argv, cap=65536, seconds=30):
        self.calls.append(argv)
        if argv[0] == "/usr/bin/systemctl" and argv[1] == "show":
            name = argv[-1]
            if name in UNITS:
                row = self.config["cold_host"]["baseline_units"][name]
            else:
                row = {"LoadState": "loaded" if self.running else "not-found", "ActiveState": "active" if self.running else "inactive",
                       "SubState": "running" if self.running else "dead", "MainPID": "42" if self.running else "0", "ControlPID": "0"}
            return "\n".join(k + "=" + v for k, v in row.items()).encode()
        if argv[0] == "/usr/bin/systemd-run":
            self.assertTrue(self.host.claim.exists())
            self.running = True
            self.file(self.host.socket, b"")
            self.file("/proc/42/comm", b"mysqld\n")
            if self.fail_start:
                raise GateError("SYNTHETIC_PARTIAL_START")
            return b""
        if argv[:2] == ["/usr/bin/systemctl", "stop"]:
            self.assertEqual(argv[-1], self.host.unit)
            self.running = False
            self.fs(self.host.socket).unlink()
            self.fs(self.host.runtime).rmdir()
            self.fs("/proc/42/comm").unlink()
            self.fs("/proc/42").rmdir()
            return b""
        if argv[0] == "/usr/bin/mysql":
            sql = argv[-1]
            if "@@skip_networking" in sql:
                return b"1\tDISABLED\t0\t1\t1\n"
            if "information_schema.TABLES" in sql:
                return "\n".join(t + "\tBASE TABLE\tInnoDB" for t in self.config["cold_host"]["tables"]).encode()
            return "\n".join(t + "\t" + ("1" if t == "t00" else "0") for t in self.config["cold_host"]["tables"]).encode()
        if argv[0] == "/usr/bin/mysqldump":
            return ("\n".join("CREATE TABLE `" + t + "` (id int);" for t in self.config["cold_host"]["tables"]) + "\nINSERT INTO `t00` VALUES (1);\n").encode()
        raise AssertionError("unexpected command")

    def adapter(self):
        from types import SimpleNamespace
        self.host.command = SimpleNamespace(run=self.command)
        return self.host

    def test_complete_cold_hook_lifecycle(self):
        h = self.adapter()
        for action in ("preflight", "close-admission", "quiesce", "fence", "assert-quiescent"):
            self.assertTrue(h.hook(action)["configured"])
        data = h.hook("logical-dump")
        (self.run_dir / "database.partial").write_bytes(data)
        self.assertEqual(h.hook("validate-dump")["dump_sha256"], digest(data))
        self.assertTrue(h.hook("resume")["cold_baseline_preserved"])
        self.assertFalse(self.running)
        self.assertFalse(any(a[:2] == ["/usr/bin/systemctl", "start"] for a in self.calls))
        start = next(a for a in self.calls if a[0] == "/usr/bin/systemd-run")
        self.assertIn("--property=RuntimeMaxSec=180", start)
        self.assertIn("--socket=/run/mysqld/mysqld.sock", start)
        self.assertIn("--pid-file=/run/mysqld/mysqld.pid", start)
        self.assertIn("--property=RuntimeDirectory=mysqld", start)
        self.assertTrue(self.fs(str(h.log)).exists())

    def test_partial_start_cleanup_owned_only(self):
        h = self.adapter()
        self.fail_start = True
        with self.assertRaises(GateError):
            h.hook("fence")
        h.stop()
        self.assertFalse(self.running)
        self.assertEqual([a[-1] for a in self.calls if a[:2] == ["/usr/bin/systemctl", "stop"]], [h.unit])

    def test_boot_change_prevents_start(self):
        self.file("/proc/sys/kernel/random/boot_id", b"changed")
        with self.assertRaisesRegex(GateError, "HOST_IDENTITY"):
            self.adapter().hook("fence")
        self.assertFalse(self.host.claim.exists())

    def test_writer_process_blocks(self):
        self.file("/proc/99/comm", b"java")
        with self.assertRaisesRegex(GateError, "UNOWNED_WRITER_PROCESS"):
            self.adapter().hook("quiesce")

    def test_dump_content_mismatch_and_cleanup(self):
        h = self.adapter()
        h.hook("fence")
        (self.run_dir / "database.partial").write_bytes(b"truncated")
        with self.assertRaisesRegex(GateError, "FULL_DUMP_DRIFT"):
            h.hook("validate-dump")
        h.stop()
        self.assertFalse(self.running)

    def test_live_mode_rejected(self):
        self.config["maintenance_mode"] = "live-resume"
        with self.assertRaisesRegex(GateError, "COLD_MODE_REQUIRED"):
            ColdHost(self.config, self.run_dir, self)

    def test_external_proof_gate_real_validation(self):
        c = copy.deepcopy(self.config)
        c.update(repository_id="1" * 64, public_root=str(self.work), private_root=str(self.root),
                 release_root=str(self.run_dir), settings=[])
        c["limits"].update(max_entries=100, source_bytes=100, metadata_bytes=100,
                            cost_limit_krw=0, projected_cost_krw=0)
        subject = digest(encoded(c))
        c["cold_host"]["evidence"] = {}
        import datetime
        now = datetime.datetime.now(datetime.timezone.utc)
        for kind in ("repository_acl", "recovery_access", "source_inventory", "cost"):
            proof = dict(version=1, kind=kind, subject_sha256=subject, repository_id=c["repository_id"],
                         machine_sha256=c["cold_host"]["machine_sha256"], boot_sha256=c["cold_host"]["boot_sha256"],
                         source_evidence_sha256="2" * 64, observed_at=(now-datetime.timedelta(minutes=1)).isoformat(),
                         expires_at=(now+datetime.timedelta(minutes=5)).isoformat(), access="PRIVATE_VERIFIED",
                         operation="EXACT_SNAPSHOT_READ_VERIFIED", snapshot_id="3"*64,
                         members_sha256=digest(encoded({})), upper_bound_krw=0, approved_limit_krw=0,
                         new_bytes_bound=100200, scope="ONE_COLD_CAPTURE", unknown_components=[])
            path = self.base / (kind + ".json")
            path.write_bytes(encoded(proof))
            c["cold_host"]["evidence"][kind] = {"path": str(path), "sha256": digest(encoded(proof))}
        with patch("host_adapter.verify_parent_lock"), patch("backup_runtime.checked_path", side_effect=lambda p, **k: Path(p)), patch("backup_runtime.inventory", return_value={}):
            verify_host_receipts(c, self.run_dir)
            c["cold_host"]["evidence"]["cost"]["sha256"] = "0" * 64
            with self.assertRaisesRegex(GateError, "EVIDENCE_PIN"):
                verify_host_receipts(c, self.run_dir)

    def test_generic_capture_cold_success_and_failure_cleanup(self):
        from types import SimpleNamespace
        import guarded_backup
        import host_adapter
        from test_guarded_backup import CaptureTests, FakeRunner, result
        for failure in (None, "timeout"):
            fixture = CaptureTests()
            fixture.setUp()
            try:
                fixture.config.update(maintenance_mode="cold-preserve", cold_host=copy.deepcopy(self.config["cold_host"]))
                fixture.config["limits"].update(dump_bytes=100000, seconds=60)
                fixture.config["hook_binary"] = str(Path(host_adapter.__file__).resolve())
                fixture.config["hook_sha256"] = digest(Path(host_adapter.__file__).read_bytes())
                self.config = fixture.config
                self.run_dir = Path(self.config["work_dir"]) / fixture.approval["run_id"]
                self.host = ColdHost(self.config, self.run_dir, SimpleNamespace(run=self.command),
                                     filesystem=self.fs, verifier=lambda c, r: None)
                self.host.prepare_log = self.fake_log
                runner = FakeRunner(self.config, failure)
                original = runner.run
                def connected(argv, cap, output=None, **kwargs):
                    if argv[0] != self.config["hook_binary"]:
                        return original(argv, cap, output, **kwargs)
                    value = self.host.hook(argv[1])
                    raw = value if isinstance(value, bytes) else encoded(value)
                    if output:
                        output.write_bytes(raw)
                    return result(raw)
                runner.run = connected
                capture = guarded_backup.Capture(self.config, fixture.root / "config.json", fixture.approval,
                                                 runner, fixture.store, lambda: None)
                with patch("host_adapter.ColdHost", return_value=self.host):
                    if failure:
                        with self.assertRaises(GateError):
                            capture.run()
                    else:
                        self.assertEqual(capture.run()["status"], "COMPLETE")
                        receipt = json.loads((capture.run_dir / "receipt.json").read_bytes())
                        self.assertEqual(receipt["status"], "VERIFIED_AND_PRESERVED_COLD")
                self.assertFalse(self.running)
            finally:
                fixture.doCleanups()

    def test_copied_claim_wrong_boot_cannot_stop(self):
        h = self.adapter()
        h.hook("fence")
        self.file("/proc/sys/kernel/random/boot_id", b"other")
        with self.assertRaisesRegex(GateError, "HOST_IDENTITY"):
            h.stop()
        self.assertFalse(any(a[:2] == ["/usr/bin/systemctl", "stop"] for a in self.calls))
        self.file("/proc/sys/kernel/random/boot_id", b"boot")
        h.stop()

    def test_existing_shared_runtime_and_log_are_preserved(self):
        h = self.adapter()
        runtime = self.fs(h.runtime)
        runtime.mkdir(parents=True)
        with self.assertRaisesRegex(GateError, "OWNED_UNIT_EXISTS"):
            h.hook("fence")
        self.assertTrue(runtime.exists())
        self.assertFalse(h.claim.exists())
        runtime.rmdir()  # only this synthetic fixture's empty directory
        log = self.fs(str(h.log))
        self.assertTrue(log.is_relative_to(self.root))
        log.parent.mkdir(parents=True)
        log.write_bytes(b"historical synthetic log")
        with self.assertRaises(FileExistsError):
            h.hook("fence")
        h.stop()
        self.assertEqual(log.read_bytes(), b"historical synthetic log")
        self.assertFalse(self.running)

    def test_private_hook_diagnostic_survives_discarded_stderr(self):
        from types import SimpleNamespace
        import host_adapter
        def fail(name):
            raise GateError("SYNTHETIC_GATE")
        adapter = SimpleNamespace(run=self.run_dir, hook=fail, stop=lambda: None)
        with patch.object(host_adapter.sys, "argv", ["hook", "fence", "config", "run"]), patch.object(host_adapter.sys, "platform", "linux"), patch.object(host_adapter.os, "geteuid", return_value=0, create=True), patch("backup_runtime.checked_path"), patch.object(host_adapter, "load", return_value={"state_dir": str(self.base / "state")}), patch.object(host_adapter, "ColdHost", return_value=adapter), contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(host_adapter.main(), 2)
        self.assertEqual(json.loads((self.run_dir / "cold-hook-diagnostic.json").read_bytes()),
                         {"code": "SYNTHETIC_GATE", "cleanup": "VERIFIED_COLD"})


if __name__ == "__main__":
    unittest.main()
