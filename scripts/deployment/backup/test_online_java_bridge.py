"""Actual Java Guard/mailbox and Python capture; only SQL/repository are synthetic."""
from pathlib import Path
import shutil
import subprocess
import tempfile
import time
import unittest
from unittest.mock import patch
import uuid

import online_capture as online
import backup_runtime as runtime
import test_online_capture as synthetic
import test_online_reconcile as recovery


class JavaBridgeTests(unittest.TestCase):
    def test_full_capture_over_actual_java_private_mailbox(self):
        repository = Path(__file__).resolve().parents[3]
        classpath_file = repository / "build/WI-20261029-ATS-007/fixture-classpath.txt"
        if not classpath_file.is_file():
            self.skipTest("Compile WI007 fixture and write its test runtime classpath first")
        java = shutil.which("java")
        self.assertIsNotNone(java, "Java 17 fixture executable required")
        with tempfile.TemporaryDirectory(prefix="ats-online-java-") as temporary:
            root = Path(temporary)
            with (root / "child.log").open("wb") as log:
                child = subprocess.Popen([java, "-cp", classpath_file.read_text().strip(),
                    "com.atstudio.atstudio.service.storage.OnlineCaptureFixture", str(root)],
                    stdout=log, stderr=subprocess.STDOUT)
                try:
                    deadline = time.monotonic() + 15
                    while not (root / "fixture-ready").exists():
                        self.assertIsNone(child.poll(), "Synthetic Java fixture exited before readiness")
                        self.assertLess(time.monotonic(), deadline, "Java fixture readiness deadline")
                        time.sleep(.025)
                    config = synthetic.config_fixture()
                    config["public_root"], config["private_root"] = str(root / "public"), str(root / "private")
                    for key in ("release_root", "work_dir", "state_dir"):
                        (root / key).mkdir()
                        config[key] = str(root / key)
                    config["release_link"] = config["release_root"]
                    config["settings"] = [str(root / "settings")]
                    (root / "settings").write_bytes(b"synthetic settings")
                    (root / "release_root/app.jar").write_bytes(b"synthetic jar")
                    config["online"] = {"schema": "fixture", "tables": ["tracks"],
                        "mysql_binary": "synthetic-mysql", "mysqldump_binary": "synthetic-dump", "drain_seconds": 1,
                        "machine_sha256": "d" * 64, "boot_sha256": "e" * 64}
                    runner = synthetic.Commands(config, None)
                    mailbox = online.Mailbox(root / "control", lambda: None, 5)
                    capture = online.OnlineCapture(config, {"run_id": uuid.uuid4().hex}, runner, mailbox, lambda: None)
                    with patch.object(online, "snapshot_nodes", side_effect=synthetic.cold_fixture.CaptureTests.parse_fixture_nodes):
                        result = capture.run()
                    self.assertEqual(result["status"], "COMPLETE")
                    self.assertEqual(mailbox.request("STATUS", capture.capture_id)["status"], "EMPTY")
                    directory = root / "control/captures" / capture.capture_id
                    self.assertTrue((directory / "seal.json").is_file())
                    self.assertTrue((directory / "release.json").is_file())
                    self.assertEqual(runner.blobs[str(root / "public/tracks/synthetic.wav")], bytes([1, 2, 3, 4]))
                    self.assertEqual(runner.blobs[str(root / "private/attachments/synthetic.txt")], b"synthetic-private")
                    self.assertEqual(runner.calls.count("dump"), len(runner.blobs))
                    receipt = runtime.read_json(directory / "verified-receipt.json")
                    self.assertFalse(receipt["restore_verified"])
                    self.assertTrue(receipt["content_verified"])
                    # A second attempt must not inherit the first snapshot/seal fields.
                    failed = online.OnlineCapture(config, {"run_id": uuid.uuid4().hex},
                        synthetic.Commands(config, None, "dump"), mailbox, lambda: None)
                    with self.assertRaisesRegex(online.GateError, "ONLINE_DUMP_EXIT"):
                        failed.run()
                    self.assertNotIn("snapshot_id", runtime.read_json(failed.state_path))
                    resolved = online.reconcile(config, recovery.approval_for(config, failed, "ABANDON_BEFORE_SEAL"), mailbox)
                    self.assertEqual(resolved["status"], "RECONCILED")
                    self.assertTrue((root / "control/captures" / failed.capture_id / "abort.json").is_file())
                    self.assertEqual(mailbox.request("STATUS", failed.capture_id)["status"], "EMPTY")
                finally:
                    (root / "fixture-stop").touch()
                    try:
                        child.wait(timeout=5)
                    except subprocess.TimeoutExpired:
                        child.kill()
                        child.wait(timeout=5)
                self.assertEqual(child.returncode, 0)


if __name__ == "__main__":
    unittest.main()
