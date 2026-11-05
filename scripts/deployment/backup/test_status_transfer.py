"""Synthetic real filesystem handoff with fake SMTP; NativeStore never activated."""
from contextlib import contextmanager
from pathlib import Path
import hashlib
import hmac
import os
import tempfile
import unittest
from unittest.mock import patch

import status_transfer as transfer
from backup_protocol import GateError, encoded, timestamp
from test_alert_delivery import FakeSMTP


class SyntheticStore:
    """Test adapter only, explicitly NOT a native permission or durability proof."""
    def __init__(self, root):
        self.root = Path(root)
        self.root.mkdir()
        self.fail_write = False

    def read(self, name, missing=False):
        transfer.token(name)
        path = self.root / name
        if missing and not path.exists():
            return None
        return path.read_bytes()

    def write(self, name, data):
        transfer.token(name)
        if self.fail_write:
            raise GateError("STORE_IO")
        pending = self.root / "pending"
        with pending.open("wb") as stream:
            stream.write(data)
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(pending, self.root / name)

    @contextmanager
    def lock(self):
        lock = self.root / "transfer-lock"
        try:
            with lock.open("xb") as stream:
                stream.write(b"reserved")
                stream.flush()
                os.fsync(stream.fileno())
        except FileExistsError:
            raise GateError("TRANSFER_LOCKED") from None
        yield
        lock.unlink()


class TransferTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.source = SyntheticStore(Path(self.tmp.name) / "source")
        self.receiver = SyntheticStore(Path(self.tmp.name) / "receiver")
        self.capture = SyntheticStore(Path(self.tmp.name) / "capture")
        self.secret = b"synthetic-HMAC-key-32-bytes-exactly"
        for store in (self.source, self.receiver):
            store.write("auth-key", self.secret)
        self.receiver.write("smtp-ref", encoded({"username": "fake", "password": "fake"}))
        self.state = {"attempt": {"stage": "FAILED", "maintenance": False}, "last_good": {
            "snapshot_id": "a" * 64, "captured_at": "2026-10-24T03:00:00Z"}}
        self.capture.write("state.json", encoded(self.state))
        self.now = timestamp("2026-10-26T03:00:00Z")
        self.config = {"host": "smtp.example.invalid", "port": 465,
                       "sender": "sender@example.invalid", "recipient": "recipient@example.invalid",
                       "credential_ref": "smtp-ref"}
        FakeSMTP.seen, FakeSMTP.failure = [], False

    def export(self, when="2026-10-26T03:00:00Z"):
        with patch("scheduled_dispatch.ledger_failure_code", return_value="DISPATCH_UNCERTAIN"):
            return transfer.export(self.source, "auth-key", "source-a", "monitor-a", when,
                                   dispatch_root="synthetic-only", source_store=self.capture)

    def receive(self, data):
        return transfer.receive(self.receiver, data, "auth-key", "source-a", "monitor-a", self.now)

    def resign(self, data, **changes):
        value = transfer.decode(data)
        value["payload"].update(changes)
        value["mac"] = hmac.new(self.secret, encoded(value["payload"]), hashlib.sha256).hexdigest()
        return encoded(value)

    def test_roundtrip_preserves_capture_and_duplicate_rechecks(self):
        data = self.export()
        first = self.receive(data)
        self.assertEqual(first, self.receive(data))
        self.assertEqual(first["state"]["last_good"]["captured_at"], self.state["last_good"]["captured_at"])
        self.assertEqual(first["sequence"], 1)

    def test_export_verify_check_fake_smtp(self):
        from test_scheduled_dispatch import DispatchTests
        from types import SimpleNamespace
        fixture = DispatchTests()
        fixture.setUp()
        self.addCleanup(fixture.doCleanups)
        fixture.runner.run = lambda *args: SimpleNamespace(code=2, data=b"", size=0)
        with self.assertRaisesRegex(GateError, "CAPTURE_OUTCOME_UNCERTAIN"):
            fixture.call()
        data = transfer.export(self.source, "auth-key", "source-a", "monitor-a", "2026-10-26T03:00:00Z",
                               dispatch_root=fixture.root, source_store=self.capture)
        with patch("alert_delivery.reserve", return_value=True):
            result = transfer.monitor(self.receiver, data, "auth-key", "source-a", "monitor-a",
                self.now, "window-a", "synthetic-alert-ledger", self.config, smtp_factory=FakeSMTP)
        self.assertEqual(result["delivery"], "SMTP_ACCEPTED")
        self.assertIn("CAPTURE_26H_STALE", result["alerts"])
        self.assertIn("DISPATCH_REQUIRES_OPERATOR", result["alerts"])
        self.assertNotIn("snapshot", str(result))

    def test_rollback_and_conflict(self):
        first = self.export()
        second = self.export()
        self.receive(second)
        with self.assertRaisesRegex(GateError, "REPLAY_ROLLBACK"):
            self.receive(first)
        # Isolated second receiver, so the known conservative held lock is not bypassed.
        other = SyntheticStore(Path(self.tmp.name) / "other")
        other.write("auth-key", self.secret)
        transfer.receive(other, first, "auth-key", "source-a", "monitor-a", self.now)
        changed = self.resign(first, dispatch_code=None)
        with self.assertRaisesRegex(GateError, "SEQUENCE_CONFLICT"):
            transfer.receive(other, changed, "auth-key", "source-a", "monitor-a", self.now)

    def test_wrong_target_key_and_forgery(self):
        data = self.export()
        with self.assertRaisesRegex(GateError, "TARGET_MISMATCH"):
            transfer.receive(self.receiver, data, "auth-key", "source-a", "monitor-b", self.now)
        self.receiver.write("wrong-key", b"x" * 32)
        with self.assertRaisesRegex(GateError, "AUTHENTICATION_FAILED"):
            transfer.receive(self.receiver, data, "wrong-key", "source-a", "monitor-a", self.now)
        bad = transfer.decode(data)
        bad["payload"]["sequence"] = 100
        with self.assertRaisesRegex(GateError, "AUTHENTICATION_FAILED"):
            self.receive(encoded(bad))

    def test_canonical_truncated_duplicate_nan_and_bounds(self):
        for bad in (b'{"x":1,"x":2}', b'{"x":NaN}', b'{"x":', b'{ "x": 1 }', b'x' * 32769):
            with self.assertRaises(GateError):
                transfer.decode(bad)
        value = transfer.decode(self.export())
        value["mac"] = "é"
        with self.assertRaisesRegex(GateError, "ARTIFACT_FIELDS"):
            self.receive(encoded(value))

    def test_future_and_capture_rollback(self):
        first = self.export()
        with self.assertRaisesRegex(GateError, "FUTURE_ARTIFACT"):
            self.receive(self.resign(first, exported_at="2026-10-27T03:00:00Z"))
        self.receive(first)
        changed = transfer.clean_state(self.state)
        changed["last_good"]["captured_at"] = "2026-10-23T03:00:00Z"
        with self.assertRaisesRegex(GateError, "CAPTURE_ROLLBACK"):
            self.receive(self.resign(first, sequence=2, state=changed))

    def test_publication_failure_preserves_previous_and_lock(self):
        data = self.export()
        self.receive(data)
        self.receiver.fail_write = True
        with self.assertRaisesRegex(GateError, "STORE_IO"):
            self.receive(self.resign(data, sequence=2))
        self.assertEqual(self.receiver.read("accepted-record"), data)
        self.assertTrue((self.receiver.root / "transfer-lock").exists())

    def test_reexport_does_not_refresh_capture_age(self):
        first = self.export("2026-10-25T03:00:00Z")
        second = self.export()
        self.assertEqual(transfer.decode(first)["payload"]["state"], transfer.decode(second)["payload"]["state"])
        with patch("alert_delivery.reserve", return_value=True):
            result = transfer.monitor(self.receiver, second, "auth-key", "source-a", "monitor-a", self.now,
                                      "window-a", "synthetic", self.config, smtp_factory=FakeSMTP)
        self.assertIn("CAPTURE_26H_STALE", result["alerts"])

    def test_no_new_artifact_keeps_failure_visible(self):
        self.receive(self.export())
        with patch("alert_delivery.reserve", return_value=False):
            result = transfer.monitor(self.receiver, None, "auth-key", "source-a", "monitor-a", self.now,
                                      "window-a", "synthetic", self.config, smtp_factory=FakeSMTP)
        self.assertIn("CAPTURE_FAILED", result["alerts"])
        self.assertEqual(result["delivery"], "RESERVED_HOLD")

    def test_stale_export_is_not_refreshed_on_delivery(self):
        data = self.export("2026-10-25T03:00:00Z")
        with patch("alert_delivery.reserve", return_value=True):
            result = transfer.monitor(self.receiver, data, "auth-key", "source-a", "monitor-a", self.now,
                                      "window-a", "synthetic", self.config, smtp_factory=FakeSMTP)
        self.assertEqual(result["transfer"], "STALE")
        self.assertIn("CAPTURE_INCOMPLETE", result["alerts"])

    def test_private_refs_traversal_and_format(self):
        with self.assertRaisesRegex(GateError, "REFERENCE_INVALID"):
            transfer.credential_resolver(self.receiver)("../secret")
        self.assertEqual(transfer.credential_resolver(self.receiver)("smtp-ref"), ("fake", "fake"))
        self.receiver.write("bad-ref", b'{"username":"a","password":"b","password":"c"}')
        with self.assertRaisesRegex(GateError, "DUPLICATE_FIELD"):
            transfer.credential_resolver(self.receiver)("bad-ref")

    def test_native_windows_denied_without_reads(self):
        with patch.object(transfer.os, "name", "nt"), patch.object(transfer.os, "open") as opened:
            with self.assertRaisesRegex(GateError, "NATIVE_PERMISSION_CHECK_UNAVAILABLE"):
                transfer.NativeStore("C:/synthetic")
            opened.assert_not_called()


if __name__ == "__main__":
    unittest.main()
