"""WI013 independent local regressions; no native operation is executed."""
import contextlib
import copy
from datetime import datetime, timezone
import io
import json
import os
from pathlib import Path
import py_compile
import shutil
import subprocess
import sys
import tempfile
import threading
import unittest
from unittest.mock import patch

import backup_runtime
import online_scheduled_dispatch as app
from backup_protocol import GateError, digest, encoded, timestamp
import test_online_scheduled_dispatch as author_tests


class IndependentDispatchTests(unittest.TestCase):
    def setUp(self):
        self.fixture = author_tests.DispatchTests("runTest")
        self.fixture.setUp()
        self.addCleanup(self.fixture.doCleanups)

    def test_corrupt_capture_identity_blocks_next_day(self):
        f = self.fixture
        first = f.run_dispatch()
        result_path = f.root / ("attempt-" + first["run_id"]) / "result.json"
        result = json.loads(result_path.read_bytes())
        result["capture_id"] = "ffffffff-ffff-ffff-ffff-ffffffffffff"
        damaged = encoded(result)
        result_path.write_bytes(damaged)
        f.set_approval(f.now + 86400)
        with self.assertRaises(GateError):
            f.run_dispatch(now=f.now + 86400)
        self.assertEqual(f.calls, 1)
        self.assertEqual(result_path.read_bytes(), damaged)

    def test_missing_claim_approval_hash_blocks_next_day(self):
        f = self.fixture
        first = f.run_dispatch()
        claim_path = f.root / ("attempt-" + first["run_id"]) / "claim.json"
        claim = json.loads(claim_path.read_bytes())
        claim.pop("approval_sha256")
        claim_path.write_bytes(encoded(claim))
        f.set_approval(f.now + 86400)
        with self.assertRaises(GateError):
            f.run_dispatch(now=f.now + 86400)
        self.assertEqual(f.calls, 1)

    def test_actual_ledger_lock_excludes_concurrent_contender(self):
        f = self.fixture
        entered, release = threading.Event(), threading.Event()
        results, failures = [], []

        def paused_executor():
            entered.set()
            if not release.wait(5):
                raise TimeoutError("TEST_BARRIER_TIMEOUT")
            return f.execute()

        def first():
            try:
                results.append(f.run_dispatch(paused_executor))
            except BaseException as exc:
                failures.append(exc)

        worker = threading.Thread(target=first)
        worker.start()
        try:
            self.assertTrue(entered.wait(5))
            with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"):
                f.run_dispatch()
        finally:
            release.set()
            worker.join(5)
        self.assertFalse(worker.is_alive())
        self.assertEqual(failures, [])
        self.assertEqual(len(results), 1)
        self.assertEqual(f.calls, 1)

    def test_wrong_receipt_blocks_following_day_without_rewriting_claim(self):
        f = self.fixture
        with self.assertRaisesRegex(GateError, "ONLINE_RECEIPT_REQUIRED"):
            f.run_dispatch(lambda: {"status": "COMPLETE", "restore": "NOT_RUN",
                                    "capture_id": "ffffffff-ffff-ffff-ffff-ffffffffffff"})
        claim_path = next(f.root.glob("attempt-*/claim.json"))
        claimed = claim_path.read_bytes()
        f.set_approval(f.now + 86400)
        with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"):
            f.run_dispatch(now=f.now + 86400)
        self.assertEqual(claim_path.read_bytes(), claimed)
        self.assertFalse(claim_path.with_name("result.json").exists())
        self.assertEqual(f.calls, 0)

    def test_cli_cannot_adopt_reread_wider_approval(self):
        """Exercise real online authority validation, stop before host access."""
        f = self.fixture
        original = copy.deepcopy(f.approval)
        changed = copy.deepcopy(original)
        changed["not_after"] = datetime.fromtimestamp(
            timestamp(original["not_after"]) + 120, timezone.utc).isoformat()
        accepted = []
        validator = app.online.validate_authority

        def record_authority(config, approval, now):
            validator(config, approval, now)
            accepted.append(copy.deepcopy(approval))

        directory = Path(app.__file__).resolve().parent
        pins = {str(directory / name): "a" * 64 for name in (
            "online_scheduled_dispatch.py", "scheduled_dispatch.py",
            "online_capture.py", "backup_runtime.py", "backup_protocol.py")}
        pins[str(Path(sys.executable).resolve())] = "a" * 64
        policy = copy.deepcopy(f.policy)
        policy["pins_sha256"] = digest(encoded(pins))
        values = {"config": f.config, "approval": original,
                  "policy": policy, "pins": pins}
        state = str(Path(f.config["state_dir"]) / "online-scheduled-ledger")

        def safe_dispatch(root, config, approval, policy, pins, execute, now):
            return execute()

        # 경로/장부/캐시 검사는 별도 시험하고 실제 승인 검증 뒤 호스트 접근 전에 중지한다.
        with contextlib.ExitStack() as stack:
            stack.enter_context(patch.object(app.sys, "platform", "linux"))
            stack.enter_context(patch.object(app.os, "geteuid", return_value=0, create=True))
            stack.enter_context(patch.object(backup_runtime, "checked_path", side_effect=lambda p, **kw: Path(p)))
            stack.enter_context(patch.object(app.online, "checked_path", side_effect=lambda p, **kw: Path(p)))
            stack.enter_context(patch.object(app, "load", side_effect=lambda p: copy.deepcopy(values[str(p)])))
            stack.enter_context(patch.object(app, "dispatch", side_effect=safe_dispatch))
            stack.enter_context(patch.object(app, "validate_source_custody"))
            stack.enter_context(patch.object(app.time, "time", return_value=f.now))
            stack.enter_context(patch.object(app.online, "read_json", return_value=changed))
            stack.enter_context(patch.object(app.online, "validate_authority", side_effect=record_authority))
            stack.enter_context(patch.object(app.online.socket, "gethostname", side_effect=GateError("TEST_STOP_BEFORE_HOST")))
            stack.enter_context(contextlib.redirect_stdout(io.StringIO()))
            app.main(["--execute", "--config", "config", "--approval", "approval",
                      "--policy", "policy", "--pins", "pins", "--state", state])
        self.assertTrue(accepted, "Expected real authority validation to run")
        self.assertNotIn(changed, accepted, "Executor accepted a wider approval after exact schedule validation")

    def test_cli_synthetic_success_passes_exact_approval_digest(self):
        """Native custody is mocked; actual policy, pins and local ledger run."""
        f = self.fixture
        directory = Path(app.__file__).resolve().parent
        files = [directory / name for name in (
            "online_scheduled_dispatch.py", "scheduled_dispatch.py", "online_capture.py",
            "backup_runtime.py", "backup_protocol.py")] + [Path(sys.executable).resolve()]
        pins = {str(path): digest(path.read_bytes()) for path in files}
        policy = copy.deepcopy(f.policy)
        policy["pins_sha256"] = digest(encoded(pins))
        values = {"config": f.config, "approval": f.approval,
                  "policy": policy, "pins": pins}
        state = str(Path(f.config["state_dir"]) / "online-scheduled-ledger")
        dispatch = app.dispatch

        def local_dispatch(root, *args):
            return dispatch(f.root, *args)

        with contextlib.ExitStack() as stack:
            stack.enter_context(patch.object(app.sys, "platform", "linux"))
            stack.enter_context(patch.object(app.os, "geteuid", return_value=0, create=True))
            stack.enter_context(patch.object(backup_runtime, "checked_path", side_effect=lambda p, **kw: Path(p)))
            stack.enter_context(patch.object(app, "validate_source_custody"))
            stack.enter_context(patch.object(app, "load", side_effect=lambda p: copy.deepcopy(values[str(p)])))
            stack.enter_context(patch.object(app, "dispatch", side_effect=local_dispatch))
            stack.enter_context(patch.object(app.time, "time", return_value=f.now))
            executor = stack.enter_context(patch.object(app.online, "execute", return_value=f.execute()))
            output = stack.enter_context(contextlib.redirect_stdout(io.StringIO()))
            result = app.main(["--execute", "--config", "config", "--approval", "approval",
                               "--policy", "policy", "--pins", "pins", "--state", state])
        self.assertEqual(result, 0, output.getvalue())
        executor.assert_called_once_with(f.config, Path("config"), Path("approval"),
                                        expected_approval_sha256=digest(encoded(f.approval)))
        self.assertFalse(json.loads(output.getvalue())["restore_verified"])
        self.assertFalse((f.root / "dispatch.lock").exists())

    def test_stale_python_cache_cannot_reach_native_input_loading(self):
        """A fresh -B process still imports cached code; the adapter must HOLD."""
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source_dir = Path(app.__file__).resolve().parent
            for name in ("online_scheduled_dispatch.py", "scheduled_dispatch.py",
                         "online_capture.py", "backup_runtime.py", "backup_protocol.py", "outer_recovery.py"):
                shutil.copyfile(source_dir / name, root / name)
            source = root / "online_capture.py"
            original = source.read_bytes()
            source.write_bytes(original + b'\nWI013_MARKER = "OLD"\n')
            os.utime(source, (1700000000, 1700000000))
            py_compile.compile(str(source), doraise=True)
            source.write_bytes(original + b'\nWI013_MARKER = "NEW"\n')
            os.utime(source, (1700000000, 1700000000))
            code = '''
from pathlib import Path
from unittest.mock import patch
import backup_runtime
import online_scheduled_dispatch as app
pins = {str(p): app.digest(p.read_bytes()) for p in Path.cwd().glob("*.py")}
app.verify_files(pins)
assert app.online.WI013_MARKER == "OLD"
print("STALE_BYTECODE_IMPORTED_WITH_MATCHING_SOURCE_PINS")
with patch.object(app.sys, "platform", "linux"), \\
     patch.object(app.os, "geteuid", return_value=0, create=True), \\
     patch.object(backup_runtime, "checked_path", side_effect=lambda p, **kw: Path(p)), \\
     patch.object(app, "load", side_effect=AssertionError("STALE_CODE_REACHED_INPUT_LOADING")):
    assert app.main(["--execute", "--config", "config", "--approval", "approval",
                     "--policy", "policy", "--pins", "pins", "--state", "state"]) == 2
'''
            result = subprocess.run([sys.executable, "-B", "-c", code], cwd=root,
                                    capture_output=True, text=True, timeout=10)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
            self.assertIn("STALE_BYTECODE_IMPORTED_WITH_MATCHING_SOURCE_PINS", result.stdout)
            self.assertIn('"status":"HOLD"', result.stdout)


if __name__ == "__main__":
    unittest.main()
