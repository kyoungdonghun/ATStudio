"""WI063 local-only approval, receipt and framing tests."""

import contextlib
import ctypes
from datetime import datetime, timezone
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import struct
import types
import unittest
from unittest.mock import Mock, patch
import uuid


SPEC = importlib.util.spec_from_file_location("feature", Path(__file__).with_name("feature-provisioning.py"))
feature = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(feature)
NOW = datetime(2026, 10, 11, 12, 0, tzinfo=timezone.utc)


def approval() -> dict:
    return {"req": feature.REQ, "wi": feature.WI, "operation": "promote-first-admin",
            "nonce": "a" * 32, "bundle_sha256": "b" * 64, "machine_id_sha256": "c" * 64,
            "server_uuid_sha256": "d" * 64, "metadata_sha256": "e" * 64,
            "user_id": "7", "principal_sha256": "f" * 64, "designation_sha256": "1" * 64,
            "issued_at": "2026-10-11T11:59:00Z", "expires_at": "2026-10-11T12:14:00Z",
            "operator_checks": dict.fromkeys(feature.CHECKS, True)}


class FeatureTest(unittest.TestCase):
    def parse(self, value: dict) -> dict:
        return feature.parse_approval(json.dumps(value).encode(), "b" * 64, "c" * 64, NOW)

    def test_approval(self) -> None:
        self.assertEqual(self.parse(approval()), approval())

    def test_approval_rejects_scope_identity_and_time(self) -> None:
        for key, value in (("operation", "grant-test-entitlement"), ("req", "other"),
                           ("wi", "other"), ("bundle_sha256", "0" * 64),
                           ("machine_id_sha256", "0" * 64), ("user_id", "0"),
                           ("user_id", True), ("user_id", "9223372036854775808"),
                           ("nonce", "../escape"), ("principal_sha256", "bad"),
                           ("expires_at", "2026-10-11T11:59:59Z"),
                           ("expires_at", "2026-10-11T13:00:00Z"),
                           ("issued_at", "2026-10-11T12:01:00Z")):
            with self.subTest(key=key, value=value):
                candidate = approval()
                candidate[key] = value
                with self.assertRaises(feature.GateError):
                    self.parse(candidate)

    def test_checks_are_exact_booleans(self) -> None:
        for check in feature.CHECKS:
            for value in (False, 1, "true", None):
                with self.subTest(check=check, value=value):
                    candidate = approval()
                    candidate["operator_checks"][check] = value
                    with self.assertRaises(feature.GateError):
                        self.parse(candidate)

    def test_json_shape_duplicate_and_size(self) -> None:
        for raw in (b'{"req":1,"req":2}', b"[]", b"null", b"{", b" " * 9000):
            with self.assertRaises(feature.GateError):
                feature.parse_approval(raw, "b" * 64, "c" * 64, NOW)
        extra = approval() | {"password": "never-accepted"}
        with self.assertRaises(feature.GateError):
            self.parse(extra)

    def test_frame_and_private_protocol(self) -> None:
        payload = feature.frame("X" * 32, approval(), 0)
        self.assertEqual(payload[:4], b"AFP1")
        fields, offset = [], 4
        while offset < len(payload):
            size = struct.unpack_from(">I", payload, offset)[0]
            fields.append(payload[offset + 4:offset + 4 + size].decode())
            offset += 4 + size
        self.assertEqual(fields, ["X" * 32, "d" * 64, "e" * 64, "7", "f" * 64, "a" * 32, "0"])
        for password in ("short", "\n" * 32):
            with self.assertRaises(feature.GateError):
                feature.frame(password, approval(), 0)
        for code, raw in ((0, b"APPLIED:9\nsecret"), (0, b"APPLIED:0\n"),
                          (2, b"READY:0\n"), (0, b"unknown\n"), (2, b"HOLD_TARGET:3\n")):
            with self.assertRaises(feature.GateError):
                feature.checked_status(code, raw)
        self.assertEqual(feature.checked_status(0, b"APPLIED:9\n"), ("APPLIED", 9))
        self.assertEqual(feature.checked_status(2, b"HOLD_UNKNOWN:0\n"), ("HOLD_UNKNOWN", 0))

    def test_claim_is_exclusive_and_receipt_is_bound(self) -> None:
        directory = Path(__file__).resolve().parents[2] / "build" / ("WI063-receipts-" + uuid.uuid4().hex)
        directory.mkdir()
        intent = feature.intent(approval())
        with patch.object(feature, "sync_directory"):
            feature.persist_new(directory / "claim", intent)
            with self.assertRaises(FileExistsError):
                feature.persist_new(directory / "claim", intent)
        committed = {"intent": intent, "status": "APPLIED", "audit_id": 9}
        self.assertEqual(feature.receipt_id(committed, intent), 9)
        for changed in (committed | {"audit_id": True}, committed | {"status": "UNKNOWN"},
                        committed | {"audit_id": 0}, committed | {"extra": 1},
                        committed | {"intent": intent | {"user_id": "8"}}):
            with self.assertRaises(feature.GateError):
                feature.receipt_id(changed, intent)

    def test_native_cannot_read_private_inputs(self) -> None:
        with patch.object(feature.base, "read_private", side_effect=AssertionError("private read")):
            with self.assertRaises(feature.GateError):
                feature.native({}, Path("unused"), Path("unused"), "promote-first-admin")

    def simulate(self, operation: str, claim: bool, receipt: bool,
                 reply: tuple[int, bytes], publish_fails: bool = False) -> tuple[str, Mock, Mock]:
        candidate = approval()
        candidate["operation"] = operation
        candidate["machine_id_sha256"] = hashlib.sha256(b"mock-machine").hexdigest()
        expected = feature.intent(candidate)
        inputs = {feature.APPROVAL: json.dumps(candidate).encode(),
                  feature.CLAIM: json.dumps(expected).encode(),
                  feature.RECEIPT: json.dumps({"intent": expected, "status": "APPLIED", "audit_id": 9}).encode(),
                  feature.base.ENV_PATH: b"synthetic-placeholder"}
        plan = {"bundle_sha256": "b" * 64}
        root = Path(feature.__file__).absolute().parents[2]
        with contextlib.ExitStack() as stack:
            stack.enter_context(patch.object(feature, "NATIVE_RELEASED", True))
            stack.enter_context(patch.object(feature.sys, "platform", "linux"))
            stack.enter_context(patch.object(feature.os, "geteuid", return_value=0, create=True))
            stack.enter_context(patch.object(feature.os, "umask"))
            stack.enter_context(patch.dict("sys.modules", {"resource": types.SimpleNamespace(
                RLIMIT_CORE=4, setrlimit=Mock())}))
            stack.enter_context(patch.object(ctypes, "CDLL", return_value=types.SimpleNamespace(prctl=lambda *args: 0)))
            stack.enter_context(patch.object(feature.base, "secure_path"))
            stack.enter_context(patch.object(feature.base, "native_java", return_value=Path("/mock/java")))
            stack.enter_context(patch.object(feature.base, "read_private", side_effect=lambda path, limit: inputs[path]))
            stack.enter_context(patch.object(feature.base, "parse_env", return_value="X" * 32))
            stack.enter_context(patch.object(feature, "load_plan", return_value=plan))
            clock = stack.enter_context(patch.object(feature, "datetime", wraps=datetime))
            clock.now.return_value = NOW
            stack.enter_context(patch.object(Path, "read_bytes", return_value=b"mock-machine"))
            stack.enter_context(patch.object(Path, "exists", autospec=True,
                                            side_effect=lambda path: claim if path == feature.CLAIM else receipt))
            stack.enter_context(patch.object(Path, "is_symlink", return_value=False))
            publish = stack.enter_context(patch.object(feature, "persist_new"))
            if publish_fails:
                publish.side_effect = lambda path, value: (_ for _ in ()).throw(OSError("private")) \
                    if path == feature.RECEIPT else None
            child = stack.enter_context(patch.object(feature.base, "run_child", return_value=reply))
            try:
                status = feature.native(plan, root, root / "build/WI063-mock", operation)
            except (feature.GateError, OSError):
                status = "REJECTED"
            return status, publish, child

    def test_claim_before_child_and_committed_receipt(self) -> None:
        status, publish, child = self.simulate("promote-first-admin", False, False, (0, b"APPLIED:9\n"))
        self.assertEqual(status, "APPLIED")
        self.assertEqual([call.args[0] for call in publish.call_args_list], [feature.CLAIM, feature.RECEIPT])
        child.assert_called_once()
        self.assertNotIn("X" * 32, " ".join(child.call_args.args[0]))

    def test_replay_never_mutates_and_retained_claim_refuses_retry(self) -> None:
        for operation in ("inspect", "promote-first-admin"):
            status, publish, child = self.simulate(operation, True, True, (0, b"ALREADY_APPLIED:9\n"))
            self.assertEqual(status, "ALREADY_APPLIED")
            publish.assert_not_called()
            child.assert_called_once()
        status, publish, child = self.simulate("promote-first-admin", True, False, (0, b"APPLIED:9\n"))
        self.assertEqual(status, "REJECTED")
        publish.assert_not_called()
        child.assert_not_called()
        status, publish, _ = self.simulate("inspect", True, False, (0, b"READY:0\n"))
        self.assertEqual(status, "HOLD_RECONCILE")
        publish.assert_not_called()

    def test_ambiguous_commit_and_receipt_failure_do_not_retry(self) -> None:
        status, publish, child = self.simulate("promote-first-admin", False, False, (2, b"HOLD_UNKNOWN:0\n"))
        self.assertEqual(status, "HOLD_UNKNOWN")
        self.assertEqual(len(publish.call_args_list), 1)
        child.assert_called_once()
        status, publish, child = self.simulate("promote-first-admin", False, False,
                                               (0, b"APPLIED:9\n"), publish_fails=True)
        self.assertEqual(status, "REJECTED")
        self.assertEqual(len(publish.call_args_list), 2)
        child.assert_called_once()

    def test_unexpected_replay_protocol_is_rejected(self) -> None:
        for claim, receipt, reply in ((True, True, b"ALREADY_APPLIED:10\n"),
                                      (False, False, b"ALREADY_APPLIED:9\n"),
                                      (True, True, b"APPLIED:9\n"),
                                      (False, False, b"READY:0\n"),
                                      (True, True, b"READY:0\n")):
            status, _, _ = self.simulate("promote-first-admin", claim, receipt, (0, reply))
            self.assertEqual(status, "REJECTED")

    def test_safe_cli_and_child_env(self) -> None:
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            code = feature.main(["secret-invalid-argument"])
        self.assertEqual(code, 2)
        self.assertNotIn("secret-invalid-argument", output.getvalue())
        with patch.dict("os.environ", {"JAVA_TOOL_OPTIONS": "unsafe", "DB_PASSWORD": "secret"}):
            self.assertNotIn("JAVA_TOOL_OPTIONS", feature.base.child_env())
            self.assertNotIn("DB_PASSWORD", feature.base.child_env())


if __name__ == "__main__":
    unittest.main()
