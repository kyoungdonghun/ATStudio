"""Isolated adapter tests; no systemd, DB, provider or native capture execution."""
from __future__ import annotations

import copy
from datetime import datetime, timezone
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import uuid

import live_service_capture as live
from backup_protocol import GateError, digest, encoded
from test_guarded_backup import config_fixture


def fixture() -> tuple[dict, dict, dict, dict, dict, float]:
    config = config_fixture()
    config.update(capture_mode="ats-online-v1", maintenance_mode="live-resume")
    config.pop("cold_host", None)
    config["online"] = {
        "version": 1, "bridge_dir": "/run/ats-test-mailbox", "app_uid": 1000, "app_gid": 1000,
        "bridge_seconds": 10, "drain_seconds": 5, "schema": "fixture",
        "tables": ["admin_operation_audit_logs", "site_settings", "tracks"],
        "machine_sha256": "d" * 64, "boot_sha256": "e" * 64,
        "mysql_sha256": "a" * 64, "mysqldump_sha256": "b" * 64,
        "mysql_binary": "/usr/bin/mysql", "mysqldump_binary": "/usr/bin/mysqldump",
        "proofs": {key: {"path": "/proofs/" + key, "sha256": "a" * 64}
                   for key in ("target", "repository_acl", "recovery_access", "cost")},
        "module_sha256": {key: "a" * 64 for key in
                          ("backup_protocol.py", "backup_runtime.py", "online_capture.py")}}
    now = datetime(2026, 11, 5, 12, 0, 1, tzinfo=timezone.utc).timestamp()
    inner = {"operation": "online-capture", "config_sha256": digest(encoded(config)),
             "run_id": "1" * 32, "not_before": "2026-11-05T12:00:00Z",
             "not_after": "2026-11-05T12:02:00Z", "release_authorized": True}
    services = {}
    for index, name in enumerate(("app", "db")):
        services[name] = {"unit": "synthetic-" + name + ".service", "pid": 101 + index,
            "start_ticks": 500 + index, "invocation_id": str(index + 1) * 32,
            "cgroup": "/system.slice/synthetic-" + name + ".service",
            "exe": "/usr/bin/synthetic-" + name, "exe_sha256": "a" * 64,
            "cmdline_sha256": "b" * 64, "unit_fragment": "/etc/systemd/system/" + name + ".service",
            "unit_sha256": "c" * 64, "uid": 1000 + index, "gid": 1000 + index,
            "netns_inode": 2000}
    attachment = {"version": 1, "configured": True,
                  "online_config_sha256": digest(encoded(config)),
                  "online_approval_sha256": digest(encoded(inner)),
                  "services": services, "app_jar": config["release_root"] + "/application.jar",
                  "app_jar_sha256": "f" * 64, "systemctl_sha256": "a" * 64,
                  "pins_sha256": "0" * 64,
                  "runtime_proof": {"path": "/private/runtime-proof.json", "sha256": "0" * 64}}
    authority = {"version": 1, "operation": "live-service-capture", "max_attempts": 1,
                 "run_id": inner["run_id"], "not_before": inner["not_before"],
                 "not_after": inner["not_after"], "service_control_authorized": False}
    proof = {"version": 1, "kind": "live-service-runtime", "scope": "ONE_EXISTING_SERVICE_CAPTURE",
             "run_id": inner["run_id"], "machine_sha256": "d" * 64, "boot_sha256": "e" * 64,
             "services_sha256": digest(encoded(services)), "source_evidence_sha256": "f" * 64,
             "observed_at": "2026-11-05T11:59:50Z", "expires_at": "2026-11-05T12:03:00Z",
             "effective_mode": dict(live.EFFECTIVE_MODE),
             **{key: True for key in live.REQUIRED_ASSERTIONS}}
    return config, inner, attachment, authority, proof, now


class Backend:
    """No service-control method is even exposed by this test collaborator."""
    def __init__(self, services: dict, run_id: str):
        self.services = copy.deepcopy(services)
        self.value = {"status": "COMPLETE", "restore": "NOT_RUN", "capture_id": str(uuid.UUID(run_id))}
        self.calls = []
        self.capture_error = None
        self.observe_error_at = None
        self.change_after = False
        self.cleanup_error = False
        self.observations = 0

    def observe(self) -> dict:
        self.calls.append("show")
        self.observations += 1
        if self.observations == self.observe_error_at:
            raise OSError("synthetic source unavailable")
        value = copy.deepcopy(self.services)
        if self.change_after and self.observations > 1:
            value["app"]["start_ticks"] += 1
        return value

    def capture(self) -> dict:
        self.calls.append("capture")
        if self.capture_error:
            raise self.capture_error
        return self.value

    def cleanup(self) -> None:
        self.calls.append("helper-cleanup")
        if self.cleanup_error:
            raise GateError("SYNTHETIC_HELPERS_REMAIN")


class LiveServiceTests(unittest.TestCase):
    def setUp(self) -> None:
        self.tmp = tempfile.TemporaryDirectory(prefix="ats-live-attach-fixture-")
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name) / "ledger"
        self.root.mkdir(mode=0o700)
        source = Path(self.tmp.name) / "fixture-code"
        source.write_bytes(b"synthetic code pin")
        self.pins = {str(source): digest(source.read_bytes())}
        self.config, self.inner, self.attachment, self.authority, self.proof, self.now = fixture()
        self.bind()
        self.backend = Backend(self.attachment["services"], self.inner["run_id"])

    def bind(self) -> None:
        self.inner["config_sha256"] = digest(encoded(self.config))
        self.attachment["online_config_sha256"] = digest(encoded(self.config))
        self.attachment["online_approval_sha256"] = digest(encoded(self.inner))
        self.attachment["pins_sha256"] = digest(encoded(self.pins))
        self.proof["subject_sha256"] = live.subject(self.attachment)
        self.attachment["runtime_proof"]["sha256"] = digest(encoded(self.proof))
        self.authority["attachment_sha256"] = digest(encoded(self.attachment))

    def run_capture(self, clock=None) -> dict:
        return live.attach_once(self.root, self.config, self.inner, self.attachment,
                                self.authority, self.proof, self.pins, self.backend,
                                clock or (lambda: self.now))

    def hold(self) -> dict:
        self.assertTrue((self.root / "attachment.lock").is_dir())
        return json.loads((self.root / ("attempt-" + self.inner["run_id"]) / "hold.json").read_bytes())

    def test_complete_preserves_services_and_separates_restore(self) -> None:
        result = self.run_capture()
        self.assertEqual(result["status"], "COMPLETE")
        self.assertFalse(result["restore_verified"])
        self.assertTrue(result["service_identity_unchanged"])
        self.assertEqual(self.backend.calls, ["show", "capture", "show", "helper-cleanup"])
        self.assertFalse((self.root / "attachment.lock").exists())

    def test_duplicate_attempt_cannot_capture_twice(self) -> None:
        self.run_capture()
        with self.assertRaisesRegex(GateError, "DUPLICATE_ATTACHMENT"):
            self.run_capture()
        self.assertEqual(self.backend.calls.count("capture"), 1)

    def test_expired_authority_has_no_claim(self) -> None:
        with self.assertRaisesRegex(GateError, "ONLINE_WINDOW"):
            self.run_capture(lambda: self.now + 200)
        self.assertEqual(list(self.root.iterdir()), [])
        self.assertEqual(self.backend.calls, [])

    def test_unconfigured_example_never_runs(self) -> None:
        self.attachment["configured"] = False
        self.bind()
        with self.assertRaisesRegex(GateError, "ATTACHMENT_DISABLED"):
            self.run_capture()

    def test_authority_cannot_grant_service_control_or_multiple_attempts(self) -> None:
        for key, value in (("service_control_authorized", True), ("max_attempts", 2)):
            with self.subTest(key=key):
                before = self.authority[key]
                self.authority[key] = value
                with self.assertRaisesRegex(GateError, "ATTACHMENT_AUTHORITY"):
                    self.run_capture()
                self.authority[key] = before
        self.assertEqual(self.backend.calls, [])

    def test_missing_catalog_table_is_not_assumed_in_dump(self) -> None:
        self.config["online"]["tables"].remove("site_settings")
        self.bind()
        with self.assertRaisesRegex(GateError, "CATALOG_TABLES_REQUIRED"):
            self.run_capture()

    def test_missing_effective_runtime_evidence_rejects(self) -> None:
        self.proof["catalog_admission_verified"] = False
        self.bind()
        with self.assertRaisesRegex(GateError, "RUNTIME_CONTRACT"):
            self.run_capture()

    def test_trial_mode_is_not_live_listening_mode(self) -> None:
        self.proof["effective_mode"]["app.background-tasks.capture-control-only"] = True
        self.bind()
        with self.assertRaisesRegex(GateError, "RUNTIME_CONTRACT"):
            self.run_capture()

    def test_proof_subject_and_hash_are_both_bound(self) -> None:
        self.proof["source_evidence_sha256"] = "e" * 64
        with self.assertRaisesRegex(GateError, "PROOF_HASH"):
            self.run_capture()

    def test_stale_runtime_proof_cannot_run(self) -> None:
        self.proof["observed_at"] = "2026-11-04T11:59:50Z"
        self.bind()
        with self.assertRaisesRegex(GateError, "RUNTIME_PROOF_STALE"):
            self.run_capture()

    def test_changed_code_pin_rejects_before_claim(self) -> None:
        Path(next(iter(self.pins))).write_bytes(b"changed")
        with self.assertRaisesRegex(GateError, "PIN_MISMATCH"):
            self.run_capture()
        self.assertEqual(list(self.root.iterdir()), [])

    def test_missing_before_identity_consumes_attempt_and_holds(self) -> None:
        self.backend.observe_error_at = 1
        with self.assertRaises(OSError):
            self.run_capture()
        self.assertNotIn("capture", self.backend.calls)
        self.assertEqual(self.hold()["failed_stage"], "OBSERVE_BEFORE")
        with self.assertRaisesRegex(GateError, "ATTACHMENT_LOCKED_OR_HOLD"):
            self.run_capture()

    def test_wrong_before_identity_never_captures(self) -> None:
        self.backend.services["db"]["pid"] += 10
        with self.assertRaisesRegex(GateError, "SERVICE_IDENTITY_BEFORE"):
            self.run_capture()
        self.assertNotIn("capture", self.backend.calls)
        self.assertFalse(self.hold()["service_identity_observed_unchanged"])

    def test_capture_unknown_response_keeps_claim_and_does_not_retry(self) -> None:
        self.backend.capture_error = TimeoutError("synthetic lost reply")
        with self.assertRaises(TimeoutError):
            self.run_capture()
        self.assertEqual(self.hold()["failed_stage"], "CAPTURE")
        self.assertIn("helper-cleanup", self.backend.calls)
        with self.assertRaisesRegex(GateError, "ATTACHMENT_LOCKED_OR_HOLD"):
            self.run_capture()
        self.assertEqual(self.backend.calls.count("capture"), 1)

    def test_capture_interrupt_also_preserves_hold(self) -> None:
        self.backend.capture_error = KeyboardInterrupt()
        with self.assertRaises(KeyboardInterrupt):
            self.run_capture()
        self.assertEqual(self.hold()["online_outcome"], "RECONCILE_SEPARATELY")

    def test_online_proof_failure_not_reissued(self) -> None:
        original = encoded(self.config)
        self.backend.capture_error = GateError("PROOF_STALE")
        with self.assertRaisesRegex(GateError, "PROOF_STALE"):
            self.run_capture()
        self.assertEqual(encoded(self.config), original)
        self.assertEqual(self.hold()["failed_stage"], "CAPTURE")

    def test_restart_after_capture_is_hold_even_with_complete_capture(self) -> None:
        self.backend.change_after = True
        with self.assertRaisesRegex(GateError, "SERVICE_IDENTITY_AFTER"):
            self.run_capture()
        self.assertFalse(self.hold()["service_identity_observed_unchanged"])
        self.assertEqual(self.backend.calls.count("capture"), 1)

    def test_show_after_failure_is_not_success(self) -> None:
        self.backend.observe_error_at = 2
        with self.assertRaises(OSError):
            self.run_capture()
        self.assertEqual(self.hold()["failed_stage"], "OBSERVE_AFTER")

    def test_bad_capture_receipt_is_hold(self) -> None:
        self.backend.value["restore"] = "VERIFIED"
        with self.assertRaisesRegex(GateError, "ONLINE_RECEIPT_REQUIRED"):
            self.run_capture()
        self.hold()

    def test_cleanup_failure_keeps_claim_and_cannot_succeed(self) -> None:
        self.backend.cleanup_error = True
        with self.assertRaisesRegex(GateError, "SYNTHETIC_HELPERS_REMAIN"):
            self.run_capture()
        self.assertFalse(self.hold()["adapter_cleanup_confirmed"])

    def test_elapsed_window_after_capture_does_not_mark_complete(self) -> None:
        values = iter([self.now, self.now, self.now, self.now + 200])
        with self.assertRaisesRegex(GateError, "ATTACHMENT_WINDOW_EXPIRED"):
            self.run_capture(lambda: next(values))
        self.hold()

    def test_code_change_during_capture_is_hold(self) -> None:
        original = self.backend.capture
        def capture():
            Path(next(iter(self.pins))).write_bytes(b"changed during capture")
            return original()
        self.backend.capture = capture
        with self.assertRaisesRegex(GateError, "PIN_MISMATCH"):
            self.run_capture()
        self.hold()

    def test_retained_uncertain_attempt_cannot_be_ignored(self) -> None:
        entry = self.root / ("attempt-" + "2" * 32)
        entry.mkdir()
        (entry / "claim.json").write_bytes(encoded({"run_id": "2" * 32}))
        with self.assertRaises(FileNotFoundError):
            self.run_capture()
        self.assertEqual(self.backend.calls, [])
        self.assertTrue(entry.exists())

    def test_default_cli_inert_and_native_blocked_on_windows(self) -> None:
        with patch("sys.stdout", new=io.StringIO()) as output:
            self.assertEqual(live.main([]), 0)
            self.assertIn('"side_effects":false', output.getvalue())
        with patch.object(live.sys, "platform", "win32"), patch("sys.stdout", new=io.StringIO()):
            self.assertEqual(live.main(["--execute"]), 2)

    def test_native_source_command_is_show_only_and_bounded(self) -> None:
        raw = "\n".join(key + "=" for key in live.UNIT_PROPERTIES).encode()
        with patch.object(live.subprocess, "run") as run:
            run.return_value.returncode = 0
            run.return_value.stdout = raw
            run.return_value.stderr = b""
            live.NativeBackend.show("synthetic-app.service")
            argv = run.call_args.args[0]
            self.assertEqual(argv[:3], ["/usr/bin/systemctl", "show", "--no-pager"])
            self.assertNotIn("stop", argv)
            self.assertNotIn("start", argv)
            self.assertEqual(run.call_args.kwargs["timeout"], 10)

    def test_native_handoff_pins_existing_capture_approval(self) -> None:
        backend = live.NativeBackend(self.config, self.inner, self.attachment, {
            "config": Path("/private/config.json"), "online_approval": Path("/private/approval.json")}, {})
        with patch.object(live, "load", side_effect=[{}, {}]), \
                patch.object(live, "checked_path", side_effect=lambda p, **kwargs: p), \
                patch.object(live, "validate_source_custody"), patch.object(live.online, "execute") as execute:
            backend.loaded = {"config": {}, "online_approval": {}}
            backend.capture()
            self.assertEqual(execute.call_count, 1)
            self.assertEqual(execute.call_args.kwargs["expected_approval_sha256"], digest(encoded(self.inner)))

    def test_corrupt_prior_capture_receipt_cannot_authorize_next_work(self) -> None:
        self.run_capture()
        entry = self.root / ("attempt-" + self.inner["run_id"])
        result = json.loads((entry / "result.json").read_bytes())
        result["capture_id"] = str(uuid.UUID("2" * 32))
        (entry / "result.json").write_bytes(encoded(result))
        with self.assertRaisesRegex(GateError, "PRIOR_ATTACHMENT_HOLD"):
            self.run_capture()
        self.assertEqual(self.backend.calls.count("capture"), 1)

    def test_native_observation_timeout_is_reported_without_traceback(self) -> None:
        with patch.object(live.sys, "platform", "linux"), \
                patch.object(live.os, "geteuid", return_value=0, create=True), \
                patch.object(live, "validate_source_custody",
                             side_effect=live.subprocess.TimeoutExpired("synthetic-show", 10)), \
                patch("sys.stdout", new=io.StringIO()) as output:
            self.assertEqual(live.main(["--execute"]), 2)
            self.assertIn("LIVE_SERVICE_CAPTURE_STOPPED", output.getvalue())

    def test_duplicate_show_properties_rejected(self) -> None:
        with self.assertRaisesRegex(GateError, "SHOW_PROPERTIES"):
            live.parse_properties(b"MainPID=1\nMainPID=2\n")


if __name__ == "__main__":
    unittest.main()
