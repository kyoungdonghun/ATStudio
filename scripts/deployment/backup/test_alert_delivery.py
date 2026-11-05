"""Fake SMTP and filesystem only; no actual credential or network access."""
import ssl
import io
import unittest
from types import SimpleNamespace
from unittest.mock import patch

import alert_delivery as alert
from backup_protocol import GateError


class FakeSMTP:
    seen = []
    failure = False

    def __init__(self, host, port, *, context, timeout):
        assert context.check_hostname and context.verify_mode == ssl.CERT_REQUIRED
        self.seen.append("connect")

    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False

    def login(self, *args):
        self.seen.append("login")

    def send_message(self, message):
        self.seen.append(message.get_content())
        if self.failure:
            raise TimeoutError("PRIVATE TEST RESPONSE MUST NOT LEAK")
        return {}


class AlertTests(unittest.TestCase):
    def setUp(self):
        FakeSMTP.seen = []
        FakeSMTP.failure = False
        self.config = {"host": "smtp.example.invalid", "port": 465,
                       "sender": "sender@example.invalid", "recipient": "recipient@example.invalid",
                       "credential_ref": "synthetic-reference"}

    def send(self):
        return alert.deliver({"alerts": ["CAPTURE_MISSED"]}, "event-1", "/fake",
                             self.config, lambda ref: ("fake", "fake"), smtp_factory=FakeSMTP)

    def test_missing_stale_dispatch_and_provenance(self):
        report = alert.check({}, 1000000, 999999, expected_since=999000,
                             dispatch_code="DISPATCH_UNCERTAIN")
        for code in ("CAPTURE_26H_STALE", "CAPTURE_MISSED", "DISPATCH_REQUIRES_OPERATOR"):
            self.assertIn(code, report["alerts"])
        self.assertEqual(report["receipt_trust"], "CALLER_MUST_VERIFY_PROVENANCE")

    def test_reservation_before_send_and_acceptance_not_receipt(self):
        def reserved(*args):
            FakeSMTP.seen.append("reserved")
            return True
        with patch.object(alert, "reserve", side_effect=reserved):
            result = self.send()
        self.assertEqual(FakeSMTP.seen[0], "reserved")
        self.assertEqual(result, {"delivery": "SMTP_ACCEPTED", "recipient_received": "UNVERIFIED"})
        self.assertNotIn("event-1", FakeSMTP.seen[-1])

    def test_ambiguous_then_duplicate_no_send(self):
        FakeSMTP.failure = True
        with patch.object(alert, "reserve", side_effect=[True, False]):
            result = self.send()
            again = self.send()
        self.assertEqual(result["delivery"], "OUTCOME_UNKNOWN_HOLD")
        self.assertEqual(again["delivery"], "RESERVED_HOLD")
        self.assertEqual(FakeSMTP.seen.count("connect"), 1)
        self.assertNotIn("PRIVATE", str(result))

    def test_invalid_headers_and_unknown_codes(self):
        with patch.object(alert, "reserve") as reserve:
            self.config["recipient"] += "\r\nBcc: bad@example.invalid"
            with self.assertRaisesRegex(GateError, "MAIL_ADDRESS"):
                self.send()
            with self.assertRaisesRegex(GateError, "ALERT_CODES"):
                alert.deliver({"alerts": ["private path"]}, "event", "/fake", {}, lambda _: ())
            reserve.assert_not_called()

    def test_ledger_failure_blocks_transport(self):
        with patch.object(alert, "reserve", side_effect=GateError("LEDGER_IO_FAILED")):
            with self.assertRaisesRegex(GateError, "LEDGER_IO_FAILED"):
                self.send()
        self.assertEqual(FakeSMTP.seen, [])

    def test_windows_reservation_fails_closed(self):
        with patch.object(alert.os, "name", "nt"), patch.object(alert.os, "open") as opened:
            with self.assertRaisesRegex(GateError, "NATIVE_PERMISSION_CHECK_UNAVAILABLE"):
                alert.reserve("C:/fake", "event")
            opened.assert_not_called()

    def test_fake_durable_claim_order_and_existing_claim(self):
        calls = []
        meta = SimpleNamespace(st_uid=10, st_mode=0o40700, st_dev=1, st_ino=2)
        class Stream(io.BytesIO):
            def fileno(self):
                return 12
            def flush(self):
                calls.append("flush")
        with patch.multiple(alert.os, name="posix", O_NOFOLLOW=0x20000,
                            O_DIRECTORY=0x10000, create=True), \
             patch.object(alert.os, "geteuid", return_value=10, create=True), \
             patch.object(alert.os, "open", side_effect=[10, 11, 12]), \
             patch.object(alert.os, "fstat", return_value=meta), \
             patch.object(alert.os, "stat", return_value=meta), \
             patch.object(alert.os, "fdopen", return_value=Stream()), \
             patch.object(alert.os, "fsync", side_effect=lambda fd: calls.append(fd)), \
             patch.object(alert.os, "close"):
            self.assertTrue(alert.reserve("/fake", "event"))
        self.assertEqual(calls, ["flush", 12, 11])

    def test_old_success_not_refreshed_by_check(self):
        state = {"last_good": {"snapshot_id": "a" * 64, "captured_at": "2026-01-01T00:00:00Z"}}
        from backup_protocol import timestamp
        report = alert.check(state, timestamp("2026-01-03T00:00:00Z"))
        self.assertIn("CAPTURE_26H_STALE", report["alerts"])
        self.assertEqual(state["last_good"]["captured_at"], "2026-01-01T00:00:00Z")

    def test_failed_dispatch_durable_ledger_to_alert(self):
        from test_scheduled_dispatch import DispatchTests
        fixture = DispatchTests()
        fixture.setUp()
        try:
            fixture.runner.run = lambda *args: SimpleNamespace(code=2, data=b"", size=0)
            with self.assertRaisesRegex(GateError, "CAPTURE_OUTCOME_UNCERTAIN"):
                fixture.call()
            report = alert.check_with_dispatch_ledger({}, fixture.now, fixture.root)
            self.assertIn("DISPATCH_REQUIRES_OPERATOR", report["alerts"])
        finally:
            fixture.doCleanups()

    def test_stable_window_event_deduplicates_polling(self):
        events = set()
        def reserve_once(directory, event):
            if event in events:
                return False
            events.add(event)
            return True
        with patch.object(alert, "reserve", side_effect=reserve_once):
            first = alert.notify({"alerts": ["CAPTURE_MISSED"]}, "window-1", "/fake",
                                 self.config, lambda _: ("fake", "fake"), smtp_factory=FakeSMTP)
            second = alert.notify({"alerts": ["CAPTURE_MISSED"]}, "window-1", "/fake",
                                  self.config, lambda _: ("fake", "fake"), smtp_factory=FakeSMTP)
        self.assertEqual(first["delivery"], "SMTP_ACCEPTED")
        self.assertEqual(second["delivery"], "RESERVED_HOLD")
        self.assertEqual(FakeSMTP.seen.count("connect"), 1)


if __name__ == "__main__":
    unittest.main()
