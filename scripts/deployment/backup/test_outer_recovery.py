"""WI003 local synthetic recovery tests. No native custody or service execution."""
from contextlib import contextmanager
from datetime import datetime, timezone
import copy
import json
import os
from pathlib import Path
import time
import unittest
from unittest.mock import patch
import uuid

import outer_recovery as outer
import online_capture as online
from backup_protocol import GateError, digest, encoded
import backup_runtime as runtime
import test_online_capture as fixtures
from test_online_reconcile import approval_for


class RecoveryTests(unittest.TestCase):
    def setUp(self):
        fixtures.OnlineCaptureTests.setUp(self)
        self.config["lock_file"] = str(self.root / "operations.lock")
        self.config["online"]["bridge_dir"] = str(self.root / "mailbox")
        self.config["online"]["module_sha256"] = {k: "a" * 64 for k in outer.MODULES}
        self.config["recovery_gate"] = {"version": 2, "owner_unit": "fixture.service",
            "source_units": {"app": "fixture-app.service", "db": "fixture-db.service"},
            "systemctl_sha256": "a" * 64, "python_sha256": "b" * 64}
        self.ledger = outer.ledger(self.config)
        self.ledger.mkdir(mode=0o700)
        self.locks = set()
        @contextmanager
        def lock(path):
            path = Path(path)
            if path in self.locks: raise GateError("OPERATIONS_BUSY")
            if not path.exists(): path.write_bytes(b"")
            self.locks.add(path)
            try: yield
            finally: self.locks.remove(path)
        for p in (patch.object(outer, "operations_lock", lock),
                  patch.object(outer, "checked_path", lambda p, **kw: Path(p)),
                  patch.object(outer, "native_owner", lambda c: {"version": 2, "synthetic": "owned"})):
            p.start(); self.addCleanup(p.stop)
        self.review = self.root / "review.raw"
        self.review.write_bytes(b"synthetic independent containment and source evidence")
        self.calls = 0

    capture = fixtures.OnlineCaptureTests.capture

    def prepare(self, failure="dump"):
        with outer.gate(self.config) as owner:
            self.entry, self.claim = outer.begin_capture(self.config, self.approval, owner)
            outer.call_capture(self.config, owner)
            self.capture_instance, self.peer, self.runner = self.capture(failure)
            if failure:
                with self.assertRaises(GateError): self.capture_instance.run()
                outer.durable(self.entry / "hold.json", encoded({"status": "HOLD"}))
            else: self.capture_instance.run()
        return self.make_approval("INNER_RECONCILED", "ABANDON_BEFORE_SEAL")

    def make_approval(self, resolution, inner_resolution=None):
        now = time.time()
        stamp = lambda v: datetime.fromtimestamp(v, timezone.utc).isoformat()
        manifest, _, _ = outer.evidence(self.config, self.approval["run_id"], 64 * 1024**2, 10000)
        ref = {"path": str(self.review), "sha256": digest(self.review.read_bytes())}
        inner = approval_for(self.config, self.capture_instance, inner_resolution) if inner_resolution else None
        return {"version": 2, "operation": "outer-dispose", "recovery_id": str(uuid.uuid4()),
            "run_id": self.approval["run_id"], "capture_id": str(uuid.UUID(self.approval["run_id"])),
            "config_sha256": digest(encoded(self.config)), "manifest_sha256": digest(encoded(manifest)),
            "resolution": resolution, "inner_approval": inner,
            "not_before": inner["not_before"] if inner else stamp(now - 1),
            "not_after": inner["not_after"] if inner else stamp(now + 300), "max_dispositions": 1,
            "max_bytes": 64 * 1024**2, "max_entries": 10000, "evidence_owner": "operator", "reviewer": "reviewer",
            "review_evidence": ref, "source_evidence": ref}

    def reconcile(self, approval, owner):
        outer.require_owner(owner, self.config)
        self.calls += 1
        return online.reconcile(self.config, approval, self.capture_instance.mailbox)

    def dispose(self, approval, **kwargs):
        with outer.gate(self.config) as owner:
            return outer.dispose(self.config, approval, owner, self.reconcile,
                observe_ended=lambda c, launch: {"owner_sha256": digest(encoded(launch)), "synthetic": "empty"}, **kwargs)

    def test_reconcile_dispose_fresh_capture_later_scan(self):
        approval = self.prepare()
        original = (self.entry / "hold.json").read_bytes()
        before_state = outer.raw_state(self.config)
        lock_id = outer.identity(self.ledger / "attachment.lock")
        result = self.dispose(approval)
        self.assertFalse(result["capture_success_asserted"])
        self.assertFalse(result["restore_verified"])
        recovery = self.ledger / "recovery" / approval["recovery_id"]
        self.assertEqual((recovery / "before/online-state.raw").read_bytes(), before_state)
        self.assertEqual((self.entry / "hold.json").read_bytes(), original)
        self.assertEqual(self.dispose(approval), result)
        self.assertEqual(self.calls, 1)
        self.approval = {"run_id": "2" * 32}
        with outer.gate(self.config) as owner:
            entry, claim = outer.begin_capture(self.config, self.approval, owner)
            outer.call_capture(self.config, owner)
            runner = fixtures.Commands(self.config, self.peer)
            fresh = online.OnlineCapture(self.config, self.approval, runner, self.capture_instance.mailbox, lambda: None)
            self.peer.failure = None
            fresh.run()
            outer.finish_capture(self.config, owner, {"status": "COMPLETE", "run_id": self.approval["run_id"],
                "capture_id": fresh.capture_id, "claim_sha256": digest(encoded(claim)),
                "kind": "direct-inner", "restore_verified": False})
            self.assertIsNotNone(outer.scan(self.config, owner))
        self.assertEqual(outer.identity(self.ledger / "attachment.lock"), lock_id)
        with outer.gate(self.config) as owner: self.assertIsNotNone(outer.scan(self.config, owner))

    def test_all_three_inner_resolutions(self):
        # ABANDON is covered by causal-chain test; precheck uses actual existing reconcile.
        approval = self.prepare("lost-release")
        approval = self.make_approval("INNER_RECONCILED", "CONFIRM_VERIFIED_RELEASE")
        self.assertEqual(self.dispose(approval)["status"], "DISPOSED")

    def test_precheck_no_capture(self):
        with outer.gate(self.config) as owner:
            self.entry, self.claim = outer.begin_capture(self.config, self.approval, owner)
            outer.call_capture(self.config, owner)
            self.capture_instance, self.peer, self.runner = self.capture()
            self.runner.run = lambda *a, **kw: fixtures.result(b"", 1)
            with self.assertRaises(GateError): self.capture_instance.run()
        approval = self.make_approval("INNER_RECONCILED", "CONFIRM_NO_CAPTURE")
        self.assertEqual(self.dispose(approval)["status"], "DISPOSED")

    def test_positive_pre_call_barrier_allows_no_capture(self):
        with outer.gate(self.config) as owner:
            self.entry, self.claim = outer.begin_capture(self.config, self.approval, owner)
        mailbox = Path(self.config["online"]["bridge_dir"])
        for name in ("requests", "claims", "responses", "captures"): (mailbox / name).mkdir(parents=True, exist_ok=True)
        approval = self.make_approval("NO_CAPTURE")
        self.assertEqual(self.dispose(approval)["status"], "DISPOSED")

    def test_call_gap_cannot_be_no_capture(self):
        self.prepare()
        approval = self.make_approval("NO_CAPTURE")
        with self.assertRaisesRegex(GateError, "NO_ENTRY_UNPROVEN"): self.dispose(approval)

    def test_unknown_transfer_stays_hold(self):
        approval = self.prepare("transfer")
        with self.assertRaisesRegex(GateError, "RECOVERY_TRANSFER_UNCERTAIN"): self.dispose(approval)
        self.assertEqual(self.peer.phase, "PINNED")

    def test_original_byte_change_rejected_before_reconcile(self):
        approval = self.prepare()
        (self.entry / "hold.json").write_bytes(b"{ malformed")
        with self.assertRaisesRegex(GateError, "EVIDENCE_CAS"): self.dispose(approval)
        self.assertEqual(self.calls, 0)

    def test_expired_authority_has_no_recovery_intent(self):
        approval = self.prepare()
        with self.assertRaisesRegex(GateError, "RECOVERY_WINDOW"):
            self.dispose(approval, clock=lambda: time.time() + 1000)
        self.assertFalse((self.ledger / "recovery").exists())

    def test_legacy_unresolved_hold_rejected(self):
        approval = self.prepare()
        claim = json.loads((self.entry / "claim.json").read_bytes()); claim["version"] = 1
        (self.entry / "claim.json").write_bytes(encoded(claim))
        approval = self.make_approval("INNER_RECONCILED", "ABANDON_BEFORE_SEAL")
        with self.assertRaisesRegex(GateError, "LEGACY_HOLD_UNSUPPORTED"): self.dispose(approval)

    def test_unexplained_state_change_after_disposition_blocks(self):
        approval = self.prepare(); self.dispose(approval)
        runtime.atomic_json(outer.state_path(self.config), {"stage": "COMPLETE"})
        with outer.gate(self.config) as owner:
            with self.assertRaisesRegex(GateError, "SHARED_STATE_UNEXPLAINED"): outer.scan(self.config, owner)

    def test_opaque_capability_rejects_forgery_expiry_and_wrong_config(self):
        with self.assertRaisesRegex(GateError, "OWNED_GATE_REQUIRED"): outer.require_owner(object(), self.config)
        with outer.gate(self.config) as owner:
            with outer.gate(self.config, owner) as nested: self.assertIs(nested, owner)
            changed = copy.deepcopy(self.config); changed["repository_id"] = "f" * 64
            with self.assertRaisesRegex(GateError, "OWNED_GATE_REQUIRED"): outer.require_owner(owner, changed)
        with self.assertRaisesRegex(GateError, "OWNED_GATE_REQUIRED"): outer.require_owner(owner, self.config)

    def test_competing_gate_rejected_and_legacy_blocked(self):
        with outer.gate(self.config):
            with self.assertRaisesRegex(GateError, "OPERATIONS_BUSY"):
                with outer.gate(self.config): pass
        legacy = copy.deepcopy(self.config); legacy.pop("recovery_gate")
        with self.assertRaisesRegex(GateError, "LEGACY_GATE_INCOMPATIBLE"):
            with outer.gate(legacy): pass

    def test_fsync_failure_before_protocol_keeps_hold(self):
        approval = self.prepare()
        with patch.object(outer, "sync_dir", side_effect=OSError("synthetic fsync")):
            with self.assertRaises(OSError): self.dispose(approval)
        self.assertEqual(self.calls, 0)

    def test_ack_loss_after_disposition_is_idempotent(self):
        approval = self.prepare()
        original = outer.durable
        def fail(path, raw):
            original(path, raw)
            if path.name == "disposition.json": raise OSError("synthetic lost acknowledgment")
        with patch.object(outer, "durable", fail):
            with self.assertRaises(OSError): self.dispose(approval)
        self.assertEqual(self.dispose(approval)["status"], "DISPOSED")
        self.assertEqual(self.calls, 1)

    def test_conflicting_recovery_cannot_supersede(self):
        approval = self.prepare(); self.dispose(approval)
        other = copy.deepcopy(approval); other["recovery_id"] = str(uuid.uuid4())
        with self.assertRaisesRegex(GateError, "RECOVERY_CONFLICT"): self.dispose(other)

    def test_terminal_archive_interruption_resumes_without_protocol_replay(self):
        approval = self.prepare()
        original = outer.preserve
        def interrupted(directory, *args):
            original(directory, *args)
            if directory.name == "after": raise OSError("synthetic terminal archive cut")
        with patch.object(outer, "preserve", interrupted):
            with self.assertRaises(OSError): self.dispose(approval)
        self.assertEqual(self.calls, 1)
        self.assertEqual(self.dispose(approval)["status"], "DISPOSED")
        self.assertEqual(self.calls, 1)

    def test_unclaimed_mailbox_request_blocks_before_reconcile(self):
        self.prepare()
        request = {"version": 1, "protocol": online.PROTOCOL, "action": "STATUS",
                   "request_id": str(uuid.uuid4()), "capture_id": str(uuid.uuid4())}
        self.peer.close()
        (self.peer.path / "requests" / (request["request_id"] + ".json")).write_bytes(encoded(request))
        approval = self.make_approval("INNER_RECONCILED", "ABANDON_BEFORE_SEAL")
        with self.assertRaisesRegex(GateError, "PENDING_MAILBOX_WORK"): self.dispose(approval)
        self.assertEqual(self.calls, 0)

    def test_legacy_dispatch_rejects_v2_before_ledger_mutation(self):
        import scheduled_dispatch
        with patch.object(scheduled_dispatch, "validate_config"):
            with self.assertRaisesRegex(GateError, "LEGACY_GATE_INCOMPATIBLE"):
                scheduled_dispatch.dispatch(self.root / "cold-ledger", self.config, {}, {}, [], {}, None, time.time())
        self.assertFalse((self.root / "cold-ledger").exists())


if __name__ == "__main__": unittest.main()
