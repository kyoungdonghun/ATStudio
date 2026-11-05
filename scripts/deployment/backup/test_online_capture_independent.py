"""WI009 independent failure tests using synthetic files and the public online route."""
from pathlib import Path
from datetime import datetime, timezone, timedelta
import unittest
import uuid
from unittest.mock import Mock, patch
from types import SimpleNamespace

import backup_protocol as protocol
import backup_runtime as runtime
import online_capture as online
import test_online_capture as fixtures


class IndependentOnlineFailureTests(unittest.TestCase):
    setUp = fixtures.OnlineCaptureTests.setUp
    capture = fixtures.OnlineCaptureTests.capture

    def test_source_change_after_upload_keeps_capture_pinned_and_does_not_publish_success(self):
        capture, peer, runner = self.capture()
        original = runner.run

        def changed_source(argv, *args, **kwargs):
            reply = original(argv, *args, **kwargs)
            if "ls" in argv:
                Path(self.config["public_root"], "tracks/source.wav").write_bytes(b"different live source")
            return reply

        runner.run = changed_source
        with self.assertRaisesRegex(protocol.GateError, "PINNED_SOURCE_CHANGED"):
            capture.run()
        self.assertEqual(peer.phase, "PINNED")
        self.assertNotIn("RELEASE", peer.actions)
        self.assertFalse((capture.directory / "verified-receipt.json").exists())

    def test_newline_in_release_filename_cannot_expand_files_from_list(self):
        # NTFS rejects newline names; inject inventory at the precise cross-platform boundary.
        capture, peer, runner = self.capture()
        original = capture.files

        def newline_member(*args):
            members = original(*args)
            row = dict(next(item for item in members.values() if item["type"] == "file"))
            members[str(self.root / "release_root") + "/safe\n/unapproved"] = row
            return members

        capture.files = newline_member
        with self.assertRaisesRegex(protocol.GateError, "FILE_LIST_PATH"):
            capture.run()
        self.assertNotIn("backup", runner.calls)
        self.assertNotIn("RELEASE", peer.actions)

    def test_unterminated_reader_prevents_success_receipt_and_release(self):
        capture, peer, runner = self.capture()
        original = runner.run

        def lingering_reader(argv, *args, **kwargs):
            reply = original(argv, *args, **kwargs)
            if "dump" in argv:
                runner.active["synthetic-reader"] = True
            return reply

        runner.run = lingering_reader
        with self.assertRaisesRegex(protocol.GateError, "READERS_NOT_ENDED"):
            capture.run()
        self.assertNotIn("RELEASE", peer.actions)
        self.assertEqual(runtime.read_json(capture.state_path)["stage"], "HOLD")

    def recovery_fixture(self):
        self.config["online"].update(machine_sha256="a" * 64, boot_sha256="b" * 64)
        capture, peer, runner = self.capture("dump")
        with self.assertRaises(protocol.GateError):
            capture.run()
        state = runtime.read_json(capture.state_path)
        now = datetime.now(timezone.utc)
        approval = {"operation": "online-reconcile", "config_sha256": protocol.digest(protocol.encoded(self.config)),
                    "state_sha256": protocol.digest(capture.state_path.read_bytes()), "run_id": state["run_id"],
                    "capture_id": state["capture_id"], "recovery_id": str(uuid.uuid4()),
                    "resolution": "ABANDON_BEFORE_SEAL", "not_before": (now - timedelta(seconds=5)).isoformat(),
                    "not_after": (now + timedelta(seconds=120)).isoformat(),
                    "termination": {"helpers_ended": True, "readers_ended": True,
                        "capture_id": state["capture_id"], "machine_sha256": "a" * 64, "boot_sha256": "b" * 64,
                        "source_evidence_sha256": "c" * 64, "observed_at": now.isoformat()}}
        return capture, approval

    def test_recovery_stale_state_approval_cannot_touch_mailbox_or_clear_hold(self):
        capture, approval = self.recovery_fixture()
        approval["state_sha256"] = "0" * 64
        mailbox = Mock()
        with self.assertRaisesRegex(protocol.GateError, "RECOVERY_BINDING"):
            online.reconcile(self.config, approval, mailbox)
        mailbox.request.assert_not_called()
        self.assertEqual(runtime.read_json(capture.state_path)["stage"], "HOLD")

    def test_recovery_unknown_reader_cannot_touch_mailbox_or_clear_hold(self):
        capture, approval = self.recovery_fixture()
        approval["termination"]["readers_ended"] = False
        mailbox = Mock()
        with self.assertRaisesRegex(protocol.GateError, "RECOVERY_TERMINATION"):
            online.reconcile(self.config, approval, mailbox)
        mailbox.request.assert_not_called()
        self.assertEqual(runtime.read_json(capture.state_path)["stage"], "HOLD")

    def test_queued_freeze_cannot_be_declared_no_effect_even_if_peer_reports_empty(self):
        self.config["online"].update(machine_sha256="a" * 64, boot_sha256="b" * 64)
        capture, peer, runner = self.capture()
        peer.close()  # Deliberately leave a later FREEZE queued, not claimed by the application.
        runner.run = lambda *args, **kwargs: fixtures.result(b"", 1)
        with self.assertRaises(protocol.GateError):
            capture.run()
        state = runtime.read_json(capture.state_path)
        state["failed_stage"] = "FREEZE_REQUESTED"
        runtime.atomic_json(capture.state_path, state)
        request = {"version": 1, "protocol": online.PROTOCOL, "request_id": capture.capture_id,
                   "action": "FREEZE", "timeout_seconds": 0}
        runtime.create_bytes(peer.path / "requests" / (capture.capture_id + ".json"), protocol.encoded(request))
        import test_online_reconcile as recovery_fixture
        approval = recovery_fixture.approval_for(self.config, capture, "CONFIRM_NO_CAPTURE")
        mailbox = Mock(directory=peer.path)
        mailbox.request.return_value = {"status": "EMPTY"}
        with self.assertRaisesRegex(protocol.GateError, "RECOVERY_CLAIM"):
            online.reconcile(self.config, approval, mailbox)
        mailbox.request.assert_not_called()
        self.assertEqual(runtime.read_json(capture.state_path)["stage"], "HOLD")

    def test_next_run_precheck_failure_does_not_inherit_prior_snapshot_or_receipt(self):
        capture, peer, runner = self.capture()
        capture.run()
        previous = runtime.read_json(capture.state_path)
        runner.run = lambda *args, **kwargs: fixtures.result(b"", 1)
        following = online.OnlineCapture(self.config, {"run_id": uuid.uuid4().hex}, runner, capture.mailbox, lambda: None)
        with self.assertRaises(protocol.GateError):
            following.run()
        state = runtime.read_json(following.state_path)
        self.assertNotIn("snapshot_id", state)
        self.assertNotIn("receipt_sha256", state)
        self.assertEqual(state["last_good"], previous["last_good"])
        self.assertEqual(runtime.read_json(following.directory / "previous-state.json"), previous)
        import test_online_reconcile as recovery_fixture
        approval = recovery_fixture.approval_for(self.config, following, "CONFIRM_NO_CAPTURE")
        self.assertEqual(online.reconcile(self.config, approval, following.mailbox)["status"], "RECONCILED")
        runner.run = Mock()
        consumed = online.OnlineCapture(self.config, capture.approval, runner, capture.mailbox, lambda: None)
        with self.assertRaises(FileExistsError):
            consumed.run()
        runner.run.assert_not_called()

    def test_media_on_an_unexpected_nested_device_blocks_before_upload(self):
        capture, peer, runner = self.capture()
        selected = Path(self.config["public_root"], "tracks/source.wav")
        original = Path.stat

        def foreign_device(path, *args, **kwargs):
            info = original(path, *args, **kwargs)
            if path == selected:
                return SimpleNamespace(st_mode=info.st_mode, st_dev=info.st_dev + 1)
            return info

        with patch.object(Path, "stat", foreign_device):
            with self.assertRaisesRegex(protocol.GateError, "NESTED_MOUNT_REVIEW_REQUIRED"):
                capture.run()
        self.assertNotIn("backup", runner.calls)
        self.assertNotIn("RELEASE", peer.actions)


if __name__ == "__main__":
    unittest.main()
