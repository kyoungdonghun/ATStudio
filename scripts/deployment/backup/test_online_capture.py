"""Synthetic filesystem mailbox + in-memory repository tests; no live services."""
from __future__ import annotations

import copy
import json
from pathlib import Path
import tempfile
import threading
import time
import unittest
from unittest.mock import patch
import uuid

import online_capture as online
import backup_protocol as protocol
import backup_runtime as runtime
import test_guarded_backup as cold_fixture

config_fixture, result = cold_fixture.config_fixture, cold_fixture.result
REPOSITORY, SNAPSHOT = cold_fixture.REPOSITORY, cold_fixture.SNAPSHOT


class Peer:
    """Wire simulator, explicitly not Java/DB runtime acceptance."""
    def __init__(self, path, failure=None):
        self.path, self.failure, self.actions, self.phase = path, failure, [], "EMPTY"
        for name in ("requests", "responses", "claims", "captures"): (path / name).mkdir(parents=True)
        self.stop = threading.Event()
        self.thread = threading.Thread(target=self.serve, daemon=True)
        self.thread.start()

    def serve(self):
        while not self.stop.wait(.005):
            for request_path in (self.path / "requests").glob("*.json"):
                response = self.path / "responses" / request_path.name
                if response.exists(): continue
                request = json.loads(request_path.read_bytes())
                action = request["action"]
                capture = request["request_id"] if action == "FREEZE" else request["capture_id"]
                if request["request_id"] in getattr(self, "seen", set()): continue
                if not hasattr(self, "seen"): self.seen = set()
                self.seen.add(request["request_id"])
                runtime.create_bytes(self.path / "claims" / request_path.name, request_path.read_bytes())
                self.actions.append(action)
                reply = {"version": 1, "protocol": online.PROTOCOL, "request_id": request["request_id"],
                         "capture_id": capture, "request_sha256": protocol.digest(request_path.read_bytes())}
                directory = self.path / "captures" / capture
                if action == "FREEZE":
                    self.phase = "FROZEN"
                    directory.mkdir()
                    refs = [{"root": "PUBLIC", "key": "tracks/source.wav", "required": True, "present": True},
                            {"root": "PRIVATE", "key": "attachments/old.txt", "required": False, "present": False}]
                    if self.failure == "escape": refs[0]["key"] = "../outside"
                    manifest = {"version": 1, "protocol": online.PROTOCOL, "capture_id": capture, "refs": refs}
                    raw = protocol.encoded(manifest)
                    runtime.create_bytes(directory / "manifest.json", raw)
                    self.ref_sha = protocol.digest(raw)
                    reply.update(status="FROZEN", manifest_file="captures/" + capture + "/manifest.json",
                                 manifest_sha256=self.ref_sha)
                elif action == "SEAL":
                    self.phase = "PINNED"
                    runtime.create_bytes(directory / "seal.json", protocol.encoded({"capture_id": capture}))
                    reply.update(status="PINNED", manifest_sha256=self.ref_sha, dump_sha256=request["dump_sha256"])
                elif action == "RELEASE":
                    receipt = (directory / "verified-receipt.json").read_bytes()
                    if protocol.digest(receipt) != request["receipt_sha256"]: raise AssertionError("receipt hash mismatch")
                    self.phase = "RELEASED"
                    runtime.create_bytes(directory / "release.json", protocol.encoded({"capture_id": capture, "receipt_sha256": request["receipt_sha256"]}))
                    reply.update(status="RELEASED")
                elif action == "STATUS":
                    reply.update(status="EMPTY" if self.phase in {"RELEASED", "ABORTED"} else self.phase)
                elif action == "ABORT":
                    self.phase = "ABORTED"
                    runtime.create_bytes(directory / "abort.json", protocol.encoded({"capture_id": capture, "receipt_sha256": request["receipt_sha256"]}))
                    reply.update(status="ABORTED")
                else: raise AssertionError("unexpected bridge action")
                if self.failure == "lost-" + action.lower(): continue
                if self.failure == "wrong-reply": reply["capture_id"] = str(uuid.uuid4())
                temporary = response.with_suffix(".pending")
                runtime.create_bytes(temporary, protocol.encoded(reply))
                temporary.replace(response)

    def close(self):
        self.stop.set()
        self.thread.join(2)


class Commands:
    def __init__(self, config, peer, failure=None):
        self.config, self.peer, self.failure = config, peer, failure
        self.active, self.calls, self.blobs, self.nodes = {}, [], {}, {}

    def stop_owned(self): self.active.clear()

    def run(self, argv, max_bytes, output=None, *, hash_only=False):
        if argv[0] == "synthetic-mysql":
            self.calls.append("tables")
            return result(b"tracks\tBASE TABLE\tInnoDB\n")
        if argv[0] == "synthetic-dump":
            self.calls.append("sql")
            if self.peer is not None and self.peer.phase != "FROZEN": raise AssertionError("dump outside frozen reference boundary")
            data = b"CREATE TABLE `tracks` (id bigint);\nINSERT INTO `tracks` VALUES (1);\n"
            if self.failure == "dump": return result(b"", 1)
            runtime.create_bytes(output, data)
            return result(data)
        action = next(arg for arg in argv if arg in {"cat", "backup", "ls", "dump"})
        self.calls.append(action)
        if action == "cat": return result(protocol.encoded({"version": 2, "id": REPOSITORY}))
        if action == "backup":
            if self.peer is not None and self.peer.phase != "PINNED": raise AssertionError("transfer before seal")
            listing = Path(argv[argv.index("--files-from-verbatim") + 1]).read_text().splitlines()
            for name in listing:
                path = Path(name)
                self.blobs[name] = path.read_bytes()
                self.nodes.update(runtime.inventory([path], lambda: None, 1000, 10**6, 10**6,
                                                    {str(path): path.stat().st_dev}))
            return result(protocol.encoded({"message_type": "summary", "snapshot_id": SNAPSHOT}),
                          3 if self.failure == "transfer" else 0)
        if action == "ls":
            rows = [{"message_type": "snapshot", "id": SNAPSHOT}]
            rows += [{"message_type": "node", "path": name, **{key: value for key, value in item.items()
                     if key != "sha256"}} for name, item in self.nodes.items()]
            if self.failure == "missing-member": rows.pop()
            return result(b"\n".join(protocol.encoded(row) for row in rows))
        data = self.blobs[argv[-1]]
        if self.failure == "content": data = b"corrupt"
        return result(data)


class OnlineCaptureTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix="ats-online-synthetic-")
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.config = config_fixture()
        for key in ("public_root", "private_root", "release_root", "work_dir", "state_dir"):
            path = self.root / key
            path.mkdir()
            self.config[key] = str(path)
        self.config["release_link"] = self.config["release_root"]
        self.config["settings"] = [str(self.root / "settings")]
        (self.root / "settings").write_bytes(b"synthetic settings")
        (self.root / "release_root/app.jar").write_bytes(b"synthetic jar")
        (self.root / "public_root/tracks").mkdir()
        (self.root / "public_root/tracks/source.wav").write_bytes(b"synthetic original")
        self.config["online"] = {"schema": "fixture", "tables": ["tracks"], "mysql_binary": "synthetic-mysql",
                                 "mysqldump_binary": "synthetic-dump", "drain_seconds": 1,
                                 "machine_sha256": "d" * 64, "boot_sha256": "e" * 64}
        self.approval = {"run_id": "1" * 32}
        parser = patch.object(online, "snapshot_nodes", side_effect=cold_fixture.CaptureTests.parse_fixture_nodes)
        parser.start()
        self.addCleanup(parser.stop)

    def capture(self, failure=None):
        peer = Peer(self.root / "mailbox", failure)
        self.addCleanup(peer.close)
        runner = Commands(self.config, peer, failure)
        mailbox = online.Mailbox(peer.path, lambda: None, .15)
        return online.OnlineCapture(self.config, self.approval, runner, mailbox, lambda: None), peer, runner

    def test_connected_success_hashes_actual_selected_files_and_releases_only_after_verification(self):
        capture, peer, runner = self.capture()
        self.assertEqual(capture.run()["status"], "COMPLETE")
        self.assertEqual(peer.actions, ["FREEZE", "SEAL", "RELEASE"])
        self.assertIn(str(self.root / "public_root/tracks/source.wav"), runner.blobs)
        self.assertEqual(runner.calls.count("dump"), len(runner.blobs))
        receipt = runtime.read_json(capture.directory / "verified-receipt.json")
        self.assertFalse(receipt["restore_verified"])
        self.assertEqual(receipt["snapshot_id"], SNAPSHOT)
        self.assertTrue(receipt["content_verified"])
        self.assertFalse(runtime.read_json(capture.directory / "restore-preparation.json")["execution_authorized"])

    def test_lost_freeze_response_keeps_exact_request_without_new_capture(self):
        capture, peer, runner = self.capture("lost-freeze")
        with self.assertRaisesRegex(protocol.GateError, "BRIDGE_REPLY_UNKNOWN"): capture.run()
        self.assertEqual(peer.phase, "FROZEN")
        self.assertEqual(peer.actions, ["FREEZE"])
        self.assertNotIn("sql", runner.calls)
        with self.assertRaisesRegex(protocol.GateError, "PRIOR_ONLINE_HOLD"):
            online.OnlineCapture(self.config, self.approval, runner, capture.mailbox, lambda: None)

    def test_hash_mismatch_keeps_pins_and_last_good_unmodified(self):
        capture, peer, runner = self.capture("content")
        with self.assertRaisesRegex(protocol.GateError, "SNAPSHOT_CONTENT_HASH"): capture.run()
        self.assertEqual(peer.phase, "PINNED")
        self.assertNotIn("RELEASE", peer.actions)
        self.assertIsNone(runtime.read_json(capture.state_path)["last_good"])

    def test_partial_transfer_never_releases_pins(self):
        capture, peer, runner = self.capture("transfer")
        with self.assertRaisesRegex(protocol.GateError, "BACKUP_EXIT"): capture.run()
        self.assertNotIn("RELEASE", peer.actions)
        self.assertEqual(peer.phase, "PINNED")

    def test_dump_failure_keeps_freeze_and_never_seals(self):
        capture, peer, runner = self.capture("dump")
        with self.assertRaisesRegex(protocol.GateError, "ONLINE_DUMP_EXIT"): capture.run()
        self.assertEqual(peer.phase, "FROZEN")
        self.assertNotIn("SEAL", peer.actions)

    def test_full_content_retrieval_cost_bound_precedes_upload(self):
        self.config["limits"]["retrieval_bytes"] = 1
        capture, peer, runner = self.capture()
        with self.assertRaisesRegex(protocol.GateError, "FULL_RETRIEVAL_BUDGET"): capture.run()
        self.assertNotIn("backup", runner.calls)
        self.assertNotIn("RELEASE", peer.actions)

    def test_reference_escape_is_rejected_without_transfer(self):
        capture, peer, runner = self.capture("escape")
        with self.assertRaisesRegex(protocol.GateError, "REFERENCE_KEY"): capture.run()
        self.assertNotIn("backup", runner.calls)

    def test_lost_release_reply_retains_unknown_state_despite_verified_transfer(self):
        capture, peer, runner = self.capture("lost-release")
        with self.assertRaisesRegex(protocol.GateError, "BRIDGE_REPLY_UNKNOWN"): capture.run()
        state = runtime.read_json(capture.state_path)
        self.assertEqual(state["stage"], "HOLD")
        self.assertIsNone(state["last_good"])
        self.assertTrue((capture.directory / "verified-receipt.json").exists())


class OnlineAuthorityTests(unittest.TestCase):
    def test_pinned_fresh_online_proofs_accept_only_known_cost_and_exact_target(self):
        with tempfile.TemporaryDirectory() as temporary:
            config = config_fixture()
            config["online"] = {"machine_sha256": "d" * 64, "boot_sha256": "e" * 64, "proofs": {}}
            now = time.time()
            from datetime import datetime, timezone
            stamp = lambda value: datetime.fromtimestamp(value, timezone.utc).isoformat()
            proofs = {}
            for kind in ("target", "repository_acl", "recovery_access", "cost"):
                proof = {"version": 1, "kind": kind, "scope": "ONE_ONLINE_CAPTURE",
                         "repository_id": config["repository_id"], "machine_sha256": "d" * 64,
                         "boot_sha256": "e" * 64, "source_evidence_sha256": "f" * 64,
                         "observed_at": stamp(now - 1), "expires_at": stamp(now + 3600)}
                proof["subject_sha256"] = online.config_subject(config)
                if kind == "target":
                    proof.update({key: True for key in ("app_tuple_matched", "sole_app_writer",
                        "all_media_mutations_fenced", "schema_release_settings_rotation_fenced",
                        "no_uncoordinated_file_writers", "no_overlapping_deployment")})
                elif kind == "repository_acl": proof["access"] = "PRIVATE_VERIFIED"
                elif kind == "recovery_access": proof.update(operation="EXACT_SNAPSHOT_READ_VERIFIED", snapshot_id=SNAPSHOT)
                else:
                    proof.update(upper_bound_krw=config["limits"]["projected_cost_krw"],
                                 run_id="1" * 32, prior_state_sha256=protocol.digest(b""),
                                 approved_limit_krw=config["limits"]["cost_limit_krw"], unknown_components=[],
                                 new_bytes_bound=sum(config["limits"][k] for k in ("source_bytes", "dump_bytes", "metadata_bytes")),
                                 retrieval_bytes_bound=config["limits"]["retrieval_bytes"])
                path = Path(temporary) / (kind + ".json")
                raw = protocol.encoded(proof)
                path.write_bytes(raw)
                config["online"]["proofs"][kind] = {"path": str(path), "sha256": protocol.digest(raw)}
                proofs[kind] = proof
            # Windows fixtures cannot attest Linux root ownership; only that syscall gate is replaced.
            with patch.object(online, "checked_path", side_effect=lambda value, **kwargs: Path(value)):
                online.verify_proofs(config, now, "1" * 32, protocol.digest(b""))
                proofs["cost"]["unknown_components"] = ["unpriced-retrieval"]
                raw = protocol.encoded(proofs["cost"])
                Path(config["online"]["proofs"]["cost"]["path"]).write_bytes(raw)
                config["online"]["proofs"]["cost"]["sha256"] = protocol.digest(raw)
                with self.assertRaisesRegex(protocol.GateError, "COST_AUTHORITY"):
                    online.verify_proofs(config, now, "1" * 32, protocol.digest(b""))

    def test_cold_approval_cannot_authorize_online_capture(self):
        config = config_fixture()
        approval = {"operation": "cold-capture", "config_sha256": protocol.digest(protocol.encoded(config))}
        with self.assertRaisesRegex(protocol.GateError, "EXACT_ONLINE_APPROVAL"):
            online.validate_authority(config, approval, time.time())

    def test_cross_capture_reply_is_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            peer = Peer(Path(temporary) / "mailbox", "wrong-reply")
            try:
                mailbox = online.Mailbox(peer.path, lambda: None, 1)
                capture = str(uuid.uuid4())
                with self.assertRaisesRegex(protocol.GateError, "BRIDGE_REPLY_BINDING"):
                    mailbox.request("FREEZE", capture, request_id=capture, timeout_seconds=0)
            finally: peer.close()

    def test_default_plan_does_not_spawn_or_access_services(self):
        with patch.object(online, "execute") as execute:
            self.assertEqual(online.main([]), 0)
            execute.assert_not_called()


if __name__ == "__main__": unittest.main()
