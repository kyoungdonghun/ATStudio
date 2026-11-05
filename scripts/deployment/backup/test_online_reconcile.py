"""Explicit recovery, immutable failure evidence, and unresolved transfer holds."""
from datetime import datetime, timezone
from pathlib import Path
import time
import unittest
from unittest.mock import patch
import uuid

import online_capture as online
import backup_protocol as protocol
import backup_runtime as runtime
import test_online_capture as fixtures


def approval_for(config, capture, resolution):
    now = time.time()
    stamp = lambda value: datetime.fromtimestamp(value, timezone.utc).isoformat()
    return {"operation": "online-reconcile", "config_sha256": protocol.digest(protocol.encoded(config)),
            "state_sha256": protocol.digest(capture.state_path.read_bytes()), "run_id": capture.approval["run_id"],
            "capture_id": capture.capture_id, "recovery_id": str(uuid.uuid4()), "resolution": resolution,
            "not_before": stamp(now - 10), "not_after": stamp(now + 300),
            "termination": {"helpers_ended": True, "readers_ended": True, "capture_id": capture.capture_id,
                "machine_sha256": config["online"]["machine_sha256"], "boot_sha256": config["online"]["boot_sha256"],
                "source_evidence_sha256": "f" * 64, "observed_at": stamp(now - 1)}}


class ReconcileTests(unittest.TestCase):
    setUp = fixtures.OnlineCaptureTests.setUp
    capture = fixtures.OnlineCaptureTests.capture

    def test_dump_failure_explicit_abandon_preserves_failed_attempt_and_allows_new_capture(self):
        capture, peer, _ = self.capture("dump")
        with self.assertRaises(protocol.GateError): capture.run()
        approval = approval_for(self.config, capture, "ABANDON_BEFORE_SEAL")
        result = online.reconcile(self.config, approval, capture.mailbox)
        self.assertEqual(result["status"], "RECONCILED")
        self.assertEqual(peer.phase, "ABORTED")
        state = runtime.read_json(capture.state_path)
        self.assertIsNone(state["last_good"])
        self.assertEqual(state["failure_code"], "ONLINE_DUMP_EXIT")
        intent = runtime.read_json(capture.directory / "recovery" / approval["recovery_id"] / "intent.json")
        self.assertEqual(intent["prior_state"]["stage"], "HOLD")
        online.OnlineCapture(self.config, {"run_id": uuid.uuid4().hex}, fixtures.Commands(self.config, peer), capture.mailbox, lambda: None)

    def test_lost_abort_reply_reconciles_same_intent_from_exact_abort_marker(self):
        capture, peer, _ = self.capture("dump")
        with self.assertRaises(protocol.GateError): capture.run()
        peer.failure = "lost-abort"
        approval = approval_for(self.config, capture, "ABANDON_BEFORE_SEAL")
        with self.assertRaisesRegex(protocol.GateError, "BRIDGE_REPLY_UNKNOWN"):
            online.reconcile(self.config, approval, capture.mailbox)
        self.assertEqual(runtime.read_json(capture.state_path)["stage"], "HOLD")
        self.assertEqual(online.reconcile(self.config, approval, capture.mailbox)["status"], "RECONCILED")
        self.assertEqual(peer.actions.count("ABORT"), 1)

    def test_lost_release_reply_uses_existing_verified_receipt_and_empty_marker(self):
        capture, peer, _ = self.capture("lost-release")
        with self.assertRaises(protocol.GateError): capture.run()
        approval = approval_for(self.config, capture, "CONFIRM_VERIFIED_RELEASE")
        self.assertEqual(online.reconcile(self.config, approval, capture.mailbox)["status"], "RECONCILED")
        self.assertEqual(runtime.read_json(capture.state_path)["last_good"]["snapshot_id"], fixtures.SNAPSHOT)
        self.assertEqual(peer.actions.count("RELEASE"), 1)

    def test_partial_transfer_cannot_be_abandoned_or_declared_verified(self):
        capture, peer, _ = self.capture("transfer")
        with self.assertRaises(protocol.GateError): capture.run()
        for action in ("ABANDON_BEFORE_SEAL", "CONFIRM_VERIFIED_RELEASE"):
            with self.assertRaisesRegex(protocol.GateError, "RECOVERY_TRANSFER_UNCERTAIN"):
                online.reconcile(self.config, approval_for(self.config, capture, action), capture.mailbox)
        self.assertEqual(peer.phase, "PINNED")
        self.assertEqual(runtime.read_json(capture.state_path)["stage"], "HOLD")

    def test_precheck_failure_can_reconcile_proved_no_capture(self):
        capture, peer, runner = self.capture()
        runner.run = lambda *args, **kwargs: fixtures.result(b"", 1)
        with self.assertRaises(protocol.GateError): capture.run()
        self.assertEqual(online.reconcile(self.config, approval_for(self.config, capture, "CONFIRM_NO_CAPTURE"),
                                         capture.mailbox)["status"], "RECONCILED")
        self.assertNotIn("ABORT", peer.actions)

    def test_missing_termination_or_changed_claim_never_releases(self):
        capture, peer, _ = self.capture("dump")
        with self.assertRaises(protocol.GateError): capture.run()
        approval = approval_for(self.config, capture, "ABANDON_BEFORE_SEAL")
        approval["termination"]["readers_ended"] = False
        with self.assertRaisesRegex(protocol.GateError, "RECOVERY_TERMINATION"):
            online.reconcile(self.config, approval, capture.mailbox)
        approval["termination"]["readers_ended"] = True
        (peer.path / "claims" / (capture.capture_id + ".json")).write_bytes(b"{}")
        with self.assertRaisesRegex(protocol.GateError, "RECOVERY_CLAIM"):
            online.reconcile(self.config, approval, capture.mailbox)
        self.assertNotIn("ABORT", peer.actions)


if __name__ == "__main__": unittest.main()
