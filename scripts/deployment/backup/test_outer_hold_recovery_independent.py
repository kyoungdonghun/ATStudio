"""WI004 independent synthetic counterexamples; never runs native/provider commands."""
from contextlib import contextmanager
import copy
import json
import os
from pathlib import Path
import stat
import subprocess
import sys
import tempfile
import threading
import time
from types import SimpleNamespace
import unittest
from unittest.mock import patch
import uuid

import outer_recovery as outer
import online_capture as online
import backup_runtime as runtime
from backup_protocol import GateError, digest, encoded
import test_outer_recovery as author
import test_online_capture as fixtures
import live_service_capture as live

NATIVE_SHOW_OWNER = outer.show_owner


class IndependentRecoveryTests(unittest.TestCase):
    # Reuse only fixture construction, not the author's test methods/counts.
    setUp = author.RecoveryTests.setUp
    capture = author.RecoveryTests.capture
    prepare = author.RecoveryTests.prepare
    make_approval = author.RecoveryTests.make_approval
    reconcile = author.RecoveryTests.reconcile
    dispose = author.RecoveryTests.dispose

    def test_live_capability_cannot_cross_thread_or_process(self):
        with outer.gate(self.config) as owner:
            errors = []
            def other_thread():
                try:
                    outer.require_owner(owner, self.config)
                except BaseException as error:
                    errors.append(error)
            thread = threading.Thread(target=other_thread)
            thread.start()
            thread.join(2)
            self.assertFalse(thread.is_alive())
            self.assertEqual(len(errors), 1)
            self.assertIsInstance(errors[0], GateError)
            pid = os.getpid()
            with patch.object(outer.os, "getpid", return_value=pid + 1):
                with self.assertRaisesRegex(GateError, "OWNED_GATE_REQUIRED"):
                    outer.require_owner(owner, self.config)

    def test_guard_replacement_invalidates_already_issued_capability(self):
        with outer.gate(self.config) as owner:
            guard = self.ledger / "operation.guard"
            guard.replace(self.ledger / "old-guard-for-fixture")
            guard.write_bytes(b"replacement inode")
            with self.assertRaisesRegex(GateError, "GUARD_REPLACED"):
                outer.require_owner(owner, self.config)

    def test_guard_replaced_between_lock_acquisition_and_token_is_rejected(self):
        original = outer.operations_lock
        @contextmanager
        def replaced_at_acquire(path):
            with original(path):
                path = Path(path)
                if path.name == "operation.guard":
                    path.replace(path.with_name("old-guard-for-fixture"))
                    path.write_bytes(b"unlocked replacement")
                yield
        with patch.object(outer, "operations_lock", replaced_at_acquire):
            with self.assertRaises(GateError):
                with outer.gate(self.config) as owner:
                    outer.require_owner(owner, self.config)

    def test_first_success_without_recovery_can_admit_next_capture(self):
        self.prepare(None)
        with outer.gate(self.config) as owner:
            # prepare intentionally leaves outer final publication to the test.
            outer.require_owner(owner, self.config)["attempt"] = self.entry
            outer.finish_capture(self.config, owner, {
                "status": "COMPLETE", "run_id": self.approval["run_id"],
                "capture_id": self.capture_instance.capture_id,
                "claim_sha256": digest(encoded(self.claim)),
                "kind": "direct-inner", "restore_verified": False})
        with outer.gate(self.config) as owner:
            self.assertIsNotNone(outer.scan(self.config, owner))
            entry, claim = outer.begin_capture(self.config, {"run_id": "2" * 32}, owner)
            self.assertEqual(claim["run_id"], "2" * 32)
            self.assertIsNotNone(claim["predecessor"])

    def test_raw_predecessor_whitespace_is_not_reserialized(self):
        raw = b'{ "stage" : "COMPLETE", "run_id" : "99999999999999999999999999999999" }\n'
        outer.state_path(self.config).write_bytes(raw)
        with outer.gate(self.config) as owner:
            self.entry, self.claim = outer.begin_capture(self.config, self.approval, owner)
        for name in ("requests", "claims", "responses", "captures"):
            (Path(self.config["online"]["bridge_dir"]) / name).mkdir(parents=True, exist_ok=True)
        approval = self.make_approval("NO_CAPTURE")
        self.dispose(approval)
        directory = self.ledger / "recovery" / approval["recovery_id"]
        self.assertEqual((directory / "before/online-state.raw").read_bytes(), raw)
        self.assertEqual((directory / "after/online-state.raw").read_bytes(), raw)
        self.assertEqual(outer.raw_state(self.config), raw)

    def test_disposition_status_tamper_rejected_by_later_scan(self):
        approval = self.prepare()
        self.dispose(approval)
        path = self.ledger / "recovery" / approval["recovery_id"] / "disposition.json"
        value = json.loads(path.read_bytes())
        value["status"] = "COMPLETE"
        path.write_bytes(encoded(value))
        with outer.gate(self.config) as owner:
            with self.assertRaises(GateError):
                outer.scan(self.config, owner)

    def test_unrecognized_file_inside_committed_recovery_blocks(self):
        approval = self.prepare()
        self.dispose(approval)
        path = self.ledger / "recovery" / approval["recovery_id"] / "unexpected.partial"
        path.write_bytes(b"interrupted unsupported work")
        with outer.gate(self.config) as owner:
            with self.assertRaises(GateError):
                outer.scan(self.config, owner)

    def test_incomplete_other_recovery_blocks_completed_history(self):
        approval = self.prepare()
        self.dispose(approval)
        path = self.ledger / "recovery" / str(uuid.uuid4())
        path.mkdir()
        (path / "intent.json").write_bytes(b"{}")
        with outer.gate(self.config) as owner:
            with self.assertRaises((GateError, OSError, KeyError, ValueError)):
                outer.scan(self.config, owner)

    def test_expiry_during_before_archive_prevents_inner_effect(self):
        approval = self.prepare()
        now = time.time()
        clock = [now]
        original = outer.preserve
        def slow_archive(directory, *args):
            original(directory, *args)
            if directory.name == "before":
                clock[0] = now + 1000
        with patch.object(outer, "preserve", slow_archive):
            with self.assertRaises(GateError):
                self.dispose(approval, clock=lambda: clock[0])
        self.assertEqual(self.calls, 0, "No ABORT/RELEASE is authorized after expiry")

    def test_expiry_during_terminal_archive_prevents_disposition(self):
        approval = self.prepare()
        now = time.time()
        clock = [now]
        original = outer.preserve
        def slow_archive(directory, *args):
            original(directory, *args)
            if directory.name == "after":
                clock[0] = now + 1000
        with patch.object(outer, "preserve", slow_archive):
            with self.assertRaises(GateError):
                self.dispose(approval, clock=lambda: clock[0])
        self.assertFalse((self.ledger / "recovery" / approval["recovery_id"] / "disposition.json").exists())

    def test_changed_review_authority_before_inner_effect_is_rejected(self):
        approval = self.prepare()
        original = outer.preserve
        def change_reference(directory, *args):
            original(directory, *args)
            if directory.name == "before":
                self.review.write_bytes(b"changed reviewed authority")
        with patch.object(outer, "preserve", change_reference):
            with self.assertRaises(GateError):
                self.dispose(approval)
        self.assertEqual(self.calls, 0)

    def test_unrelated_mailbox_write_after_reconcile_stays_hold(self):
        approval = self.prepare()
        original = self.reconcile
        def changed(inner, owner):
            result = original(inner, owner)
            (Path(self.config["online"]["bridge_dir"]) / "foreign-actor.json").write_bytes(b"{}")
            return result
        with outer.gate(self.config) as owner:
            with self.assertRaises(GateError):
                outer.dispose(self.config, approval, owner, changed,
                    observe_ended=lambda c, launch: {"owner_sha256": digest(encoded(launch)), "synthetic": "empty"})
        self.assertFalse((self.ledger / "recovery" / approval["recovery_id"] / "disposition.json").exists())

    def test_complete_label_with_false_verification_receipt_cannot_dispose(self):
        self.prepare(None)
        work = Path(self.config["work_dir"]) / self.approval["run_id"]
        capture_dir = Path(self.config["online"]["bridge_dir"]) / "captures" / self.capture_instance.capture_id
        receipt = json.loads((work / "verified-receipt.json").read_bytes())
        receipt["content_verified"] = False
        raw = encoded(receipt)
        (work / "verified-receipt.json").write_bytes(raw)
        (capture_dir / "verified-receipt.json").write_bytes(raw)
        marker = json.loads((capture_dir / "release.json").read_bytes())
        marker["receipt_sha256"] = digest(raw)
        (capture_dir / "release.json").write_bytes(encoded(marker))
        state = json.loads(outer.raw_state(self.config))
        state.update(receipt_sha256=digest(raw), last_good=receipt)
        runtime.atomic_json(outer.state_path(self.config), state)
        approval = self.make_approval("INNER_COMPLETE")
        with self.assertRaisesRegex(GateError, "TERMINAL_VERIFIED_RECEIPT"):
            self.dispose(approval)

    def test_immutable_evidence_limits_stop_before_reconcile(self):
        approval = self.prepare()
        approval["max_entries"] = 1
        with self.assertRaisesRegex(GateError, "EVIDENCE_COUNT"):
            self.dispose(approval)
        self.assertEqual(self.calls, 0)
        self.assertFalse((self.ledger / "recovery").exists())

    def test_different_owner_record_cannot_replace_capture_call_custody(self):
        self.prepare()
        # The original call still hashes the original launch record; a newly observed
        # empty but unrelated owner must not authorize ending the original capture.
        (self.entry / "owner.json").write_bytes(encoded({"version": 2, "synthetic": "different-ended-owner"}))
        approval = self.make_approval("INNER_RECONCILED", "ABANDON_BEFORE_SEAL")
        with self.assertRaises(GateError):
            self.dispose(approval)
        self.assertEqual(self.calls, 0)

    def test_no_capture_requires_original_claim_config_binding(self):
        with outer.gate(self.config) as owner:
            self.entry, self.claim = outer.begin_capture(self.config, self.approval, owner)
        for name in ("requests", "claims", "responses", "captures"):
            (Path(self.config["online"]["bridge_dir"]) / name).mkdir(parents=True, exist_ok=True)
        claim = json.loads((self.entry / "claim.json").read_bytes())
        claim["online_config_sha256"] = "f" * 64
        (self.entry / "claim.json").write_bytes(encoded(claim))
        approval = self.make_approval("NO_CAPTURE")
        with self.assertRaises(GateError):
            self.dispose(approval)

    def test_guard_inode_replacement_between_invocations_blocks_history(self):
        approval = self.prepare()
        self.dispose(approval)
        guard = self.ledger / "operation.guard"
        guard.rename(self.root / "original-guard")
        guard.write_bytes(b"")
        with self.assertRaises(GateError):
            with outer.gate(self.config) as owner:
                outer.scan(self.config, owner)

    def test_ledger_directory_replacement_preserving_children_still_blocks(self):
        approval = self.prepare()
        self.dispose(approval)
        original = self.root / "original-ledger-container"
        self.ledger.rename(original)
        self.ledger.mkdir()
        for child in original.iterdir():
            child.rename(self.ledger / child.name)
        with self.assertRaises(GateError):
            with outer.gate(self.config) as owner:
                outer.scan(self.config, owner)

    def test_ledger_replacement_during_archival_blocks_before_inner_effect(self):
        approval = self.prepare()
        original_preserve = outer.preserve
        def replace_container(directory, *args):
            original_preserve(directory, *args)
            if directory.name == "before":
                original = self.root / "original-active-ledger-container"
                self.ledger.rename(original)
                self.ledger.mkdir()
                for child in original.iterdir():
                    child.rename(self.ledger / child.name)
        with patch.object(outer, "preserve", replace_container):
            with self.assertRaisesRegex(GateError, "LEDGER_REPLACED"):
                self.dispose(approval)
        self.assertEqual(self.calls, 0, "The new ledger cannot authorize an inner mutation")
        self.assertFalse((self.ledger / "recovery" / approval["recovery_id"] / "disposition.json").exists())

    def test_outer_resume_after_lost_abort_reply_uses_exact_marker_without_replay(self):
        approval = self.prepare()
        self.peer.failure = "lost-abort"
        with self.assertRaisesRegex(GateError, "BRIDGE_REPLY_UNKNOWN"):
            self.dispose(approval)
        self.assertEqual(self.peer.actions.count("ABORT"), 1)
        result = self.dispose(approval)
        self.assertEqual(result["status"], "DISPOSED")
        self.assertEqual(self.peer.actions.count("ABORT"), 1)

    def test_original_lost_release_reply_is_confirmed_without_second_release(self):
        self.prepare("lost-release")
        approval = self.make_approval("INNER_RECONCILED", "CONFIRM_VERIFIED_RELEASE")
        self.assertEqual(self.peer.actions.count("RELEASE"), 1)
        self.assertEqual(self.dispose(approval)["status"], "DISPOSED")
        self.assertEqual(self.peer.actions.count("RELEASE"), 1)

    def test_outer_resume_after_its_release_reply_is_lost_never_reissues(self):
        with outer.gate(self.config) as owner:
            self.entry, self.claim = outer.begin_capture(self.config, self.approval, owner)
            outer.call_capture(self.config, owner)
            self.capture_instance, self.peer, self.runner = self.capture()
            original = self.capture_instance.mailbox.request
            def stop_before_release(action, *args, **kwargs):
                if action == "RELEASE": raise GateError("SYNTHETIC_RELEASE_NOT_SENT")
                return original(action, *args, **kwargs)
            with patch.object(self.capture_instance.mailbox, "request", stop_before_release):
                with self.assertRaisesRegex(GateError, "SYNTHETIC_RELEASE_NOT_SENT"):
                    self.capture_instance.run()
        self.assertEqual(self.peer.actions.count("RELEASE"), 0)
        approval = self.make_approval("INNER_RECONCILED", "CONFIRM_VERIFIED_RELEASE")
        self.peer.failure = "lost-release"
        with self.assertRaisesRegex(GateError, "BRIDGE_REPLY_UNKNOWN"):
            self.dispose(approval)
        self.assertEqual(self.peer.actions.count("RELEASE"), 1)
        self.assertEqual(self.dispose(approval)["status"], "DISPOSED")
        self.assertEqual(self.peer.actions.count("RELEASE"), 1)


class IndependentNativeObservationTests(unittest.TestCase):
    """Actual native observation functions with redirected synthetic procfs and show replies."""
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix="ats-wi004-native-observation-")
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.pid = 919191
        self.group = "/system.slice/wi004-fixture.service"
        self.properties = {"InvocationID": "a" * 32, "ControlGroup": self.group,
            "MainPID": str(self.pid), "ControlPID": "0", "ActiveState": "active", "SubState": "running",
            "Restart": "no", "TriggeredBy": "", "Delegate": "no", "ProtectControlGroups": "yes",
            "NoNewPrivileges": "yes", "CapabilityBoundingSet": "cap_chown cap_dac_override", "KillMode": "control-group"}
        self.config = {"online": {"machine_sha256": digest(b"machine"), "boot_sha256": digest(b"boot")},
                       "recovery_gate": {"owner_unit": "wi004-fixture.service", "python_sha256": digest(b"python image"),
                           "source_units": {"app": "fixture-app.service", "db": "fixture-db.service"}}}
        for name, raw in {"etc/machine-id": b"machine\n", "proc/sys/kernel/random/boot_id": b"boot\n",
            "proc/self/cgroup": ("0::" + self.group + "\n").encode(),
            f"proc/{self.pid}/exe": b"python image",
            f"proc/{self.pid}/stat": (f"{self.pid} (fixture (nested comm)) " + " ".join(["S"] + ["0"] * 18 + ["1234"] + ["0"] * 10)).encode(),
            "sys/fs/cgroup" + self.group + "/cgroup.events": b"populated 0\nfrozen 0\n",
            "sys/fs/cgroup" + self.group + "/cgroup.procs": b""}.items():
            path = self.root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(raw)
        self.sources = {}
        for index, role in enumerate(("app", "db"), 1):
            pid = self.pid + index
            proc = self.root / "proc" / str(pid)
            proc.mkdir()
            (proc / "exe").write_bytes((role + " image").encode())
            (proc / "stat").write_text(f"{pid} ({role}) " + " ".join(["S"] + ["0"] * 18 + [str(2000 + index)]))
            fragment = self.root / (role + ".service")
            fragment.write_bytes((role + " unit").encode())
            self.sources["fixture-" + role + ".service"] = {"MainPID": str(pid), "FragmentPath": str(fragment),
                "ControlGroup": "/system.slice/fixture-" + role + ".service", "InvocationID": str(index) * 32,
                "ActiveState": "active", "SubState": "running"}
        real_path = Path
        def redirect(value):
            text = str(value).replace("\\", "/")
            if text.startswith(("/proc", "/sys", "/etc")):
                return self.root / text.lstrip("/")
            return real_path(value)
        self.redirect = redirect
        for item in (patch.object(outer, "Path", redirect),
                     patch.object(outer, "checked_path", side_effect=lambda p, **kw: redirect(p)),
                     patch.object(outer.os, "getpid", return_value=self.pid),
                     patch.object(outer, "show_owner", side_effect=lambda unit: copy.deepcopy(self.properties)),
                     patch.object(live.NativeBackend, "show", side_effect=lambda unit: copy.deepcopy(self.sources[unit]))):
            item.start()
            self.addCleanup(item.stop)

    def stop_fixture(self):
        self.properties.update(MainPID="0", ActiveState="inactive", SubState="dead")
        # Preserve the original PID directory with different start ticks to test reuse.
        path = self.root / f"proc/{self.pid}/stat"
        path.write_text(path.read_text().replace("1234", "5678"))

    def test_actual_observer_accepts_bound_empty_retained_cgroup_and_pid_reuse(self):
        launch = outer.native_owner(self.config)
        self.assertEqual(launch["process"]["start_ticks"], 1234)
        self.stop_fixture()
        result = outer.terminated(self.config, launch)
        self.assertEqual(result["owner_sha256"], digest(encoded(launch)))
        self.assertEqual(set(result["source"]), {"app", "db"})

    def test_actual_owner_rejects_source_cgroup_overlap(self):
        self.sources["fixture-app.service"]["ControlGroup"] = self.group + "/child"
        with self.assertRaisesRegex(GateError, "HELPER_SOURCE_CGROUP_OVERLAP"):
            outer.native_owner(self.config)

    def test_actual_source_snapshot_records_changed_current_process(self):
        launch = outer.native_owner(self.config)
        self.stop_fixture()
        app = self.root / "proc" / str(self.pid + 1) / "stat"
        app.write_text(app.read_text().replace("2001", "3001"))
        result = outer.terminated(self.config, launch)
        self.assertEqual(result["source"]["app"]["process"]["start_ticks"], 3001)
        self.assertNotEqual(result["source"], launch["source"])

    def test_actual_show_parser_rejects_duplicate_and_missing_properties(self):
        raw = "\n".join(key + "=" + self.properties[key] for key in outer.OWNER_PROPERTIES).encode()
        def response(data): return SimpleNamespace(returncode=0, stdout=data, stderr=b"")
        with patch.object(outer.subprocess, "run", return_value=response(raw)) as command:
            self.assertEqual(NATIVE_SHOW_OWNER("fixture.service"), self.properties)
            self.assertEqual(command.call_args.args[0][1], "show")
        for bad in (raw + b"\nMainPID=919191", raw.split(b"\n", 1)[1]):
            with self.subTest(bad=bad[:20]):
                with patch.object(outer.subprocess, "run", return_value=response(bad)):
                    with self.assertRaises(GateError):
                        NATIVE_SHOW_OWNER("fixture.service")

    def test_actual_observer_rejects_empty_invocation_identity(self):
        self.properties["InvocationID"] = ""
        with self.assertRaises(GateError):
            outer.native_owner(self.config)

    def test_actual_observer_rejects_unpinned_running_python(self):
        self.config["recovery_gate"]["python_sha256"] = "f" * 64
        with self.assertRaises(GateError):
            outer.native_owner(self.config)

    def test_actual_observer_rejects_surviving_descendant_or_original_pid(self):
        launch = outer.native_owner(self.config)
        self.properties.update(MainPID="0", ActiveState="inactive", SubState="dead")
        with self.assertRaisesRegex(GateError, "OWNER_PROCESS_REMAINS"):
            outer.terminated(self.config, launch)
        self.stop_fixture()
        events = self.root / ("sys/fs/cgroup" + self.group + "/cgroup.events")
        events.write_bytes(b"populated 1\nfrozen 0\n")
        with self.assertRaisesRegex(GateError, "READERS_REMAIN"):
            outer.terminated(self.config, launch)

    def test_actual_observer_rejects_replaced_group_and_changed_invocation(self):
        launch = outer.native_owner(self.config)
        self.stop_fixture()
        self.properties["InvocationID"] = "b" * 32
        with self.assertRaisesRegex(GateError, "OWNER_NOT_ENDED"):
            outer.terminated(self.config, launch)
        self.properties["InvocationID"] = "a" * 32
        group = self.root / ("sys/fs/cgroup" + self.group)
        group.rename(group.with_name("old-cgroup"))
        group.mkdir()
        with self.assertRaisesRegex(GateError, "OWNER_CGROUP_CHANGED"):
            outer.terminated(self.config, launch)

    def test_actual_observer_rejects_missing_group_and_privilege_expansion(self):
        launch = outer.native_owner(self.config)
        self.stop_fixture()
        self.properties["CapabilityBoundingSet"] += " cap_sys_admin"
        with self.assertRaisesRegex(GateError, "OWNER_CONFINEMENT"):
            outer.terminated(self.config, launch)
        self.properties["CapabilityBoundingSet"] = "cap_chown cap_dac_override"
        group = self.root / ("sys/fs/cgroup" + self.group)
        group.rename(group.with_name("removed-cgroup"))
        with self.assertRaises((GateError, OSError)):
            outer.terminated(self.config, launch)


class IndependentNativeEntrypointTests(unittest.TestCase):
    def test_real_script_entry_and_online_callback_share_capability_registry(self):
        # Isolated local Python process: the real CLI executes only inert 'plan'.
        # A trace captures its actual __main__ globals before the normal SystemExit.
        code = r'''
import os, runpy, sys
path = os.path.abspath(sys.argv[1])
captured = []
def trace(frame, event, arg):
    if event == "call" and os.path.abspath(frame.f_code.co_filename) == path and frame.f_code.co_name == "main":
        captured.append(frame.f_globals)
    return None
sys.argv = [path, "plan"]
sys.settrace(trace)
try:
    runpy.run_path(path, run_name="__main__")
except SystemExit as result:
    assert result.code == 0, result.code
finally:
    sys.settrace(None)
import online_capture
assert captured, "Real script main was not observed"
assert online_capture.outer._owners is captured[0]["_owners"], "CLI and callback registries diverged"
token = object()
captured[0]["_owners"][token] = {"synthetic": "live-owner"}
assert online_capture.outer._owners[token] == {"synthetic": "live-owner"}
del captured[0]["_owners"][token]
print("CANONICAL_REGISTRY_CONFIRMED")
'''
        result = subprocess.run([sys.executable, "-B", "-c", code, str(Path(outer.__file__).resolve())],
            cwd=Path(outer.__file__).resolve().parent, capture_output=True, text=True, timeout=20)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("CANONICAL_REGISTRY_CONFIRMED", result.stdout)

    def test_actual_execute_preserves_capability_through_native_pin_loop(self):
        # Stop at the actual gate boundary; no runner/systemctl/DB/provider is constructed.
        from test_live_service_capture import fixture
        config, approval, *_ = fixture()
        config["hook_binary"] = online.__file__
        for name in ("restic", "hook"):
            config[name + "_sha256"] = "a" * 64
        for name in ("mysql", "mysqldump"):
            config["online"][name + "_sha256"] = "a" * 64
        config["online"]["machine_sha256"] = digest(b"machine")
        config["online"]["boot_sha256"] = digest(b"boot")
        config["expected_filesystems"] = {config[k]: 1 for k in
            ("public_root", "private_root", "release_root", "state_dir", "work_dir", "cache_dir", "temp_dir")}
        config["expected_filesystems"].update({name: 1 for name in config["settings"]})
        class FixturePath(type(Path())):
            def is_dir(self): return True
            def is_file(self): return True
            def resolve(self, strict=False): return self
            def read_bytes(self):
                name = str(self).replace("\\", "/")
                if name == "/etc/machine-id": return b"machine\n"
                if name == "/proc/sys/kernel/random/boot_id": return b"boot\n"
                raise AssertionError("Unexpected native read: " + name)
            def stat(self, **kwargs):
                return SimpleNamespace(st_uid=config["online"]["app_uid"],
                    st_gid=config["online"]["app_gid"], st_mode=stat.S_IFDIR | 0o700)
        config["release_link"] = config["release_root"]
        seen = []
        class ReachedGate(Exception): pass
        @contextmanager
        def stop_at_gate(value, owner=None):
            seen.append(owner)
            raise ReachedGate()
            yield
        from contextlib import ExitStack
        with ExitStack() as stack:
            for item in (patch.object(online.sys, "platform", "linux"),
                         patch.object(online.os, "geteuid", return_value=0, create=True),
                         patch.object(online, "Path", FixturePath),
                         patch.object(online, "validate_online"),
                         patch.object(online, "validate_authority"),
                         patch.object(online, "checked_path", side_effect=lambda p, **kw: FixturePath(p)),
                         patch.object(online, "checked_setting_source"),
                         patch.object(online, "read_json", return_value=approval),
                         patch.object(online.socket, "gethostname", return_value=config["target_host"]),
                         patch.object(online.os, "access", return_value=True),
                         patch.object(online, "hash_file", return_value=(1, "a" * 64)),
                         patch.object(outer, "gate", stop_at_gate)):
                stack.enter_context(item)
            token = object()
            for owner in (None, token):
                with self.subTest(owner_is_none=owner is None):
                    with self.assertRaises(ReachedGate):
                        online.execute(config, "synthetic-config", "synthetic-approval", owner=owner)
                    self.assertIs(seen[-1], owner)


if __name__ == "__main__":
    unittest.main()
