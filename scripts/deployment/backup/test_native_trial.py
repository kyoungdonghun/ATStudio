"""WI022 synthetic packaging/assembly only; no native probe is run."""
import copy
import json
import stat
import os
from types import SimpleNamespace
from datetime import datetime, timedelta, timezone
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch, mock_open

from backup_protocol import GateError, digest, encoded, validate_config
from native_trial import FILES, BOOTSTRAP, assemble, assemble_scheduled, package, probe_payload, boot_identity, verify_quarantine_response, private_directory
from scheduled_dispatch import load
from test_guarded_backup import config_fixture
from host_adapter import UNITS, MASKED
from backup_runtime import restic_command, checked_setting_source


class NativeTrialTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.bundle = self.root / "bundle"
        self.remote = "/root/atstudio-trials/wi022-synthetic"
        package(self.bundle, self.remote)
        c = config_fixture()
        c.update(maintenance_mode="cold-preserve")
        c["limits"].update(cost_limit_krw=0, projected_cost_krw=0, seconds=900,
                           dump_bytes=1024*1024, source_bytes=2*1024*1024,
                           metadata_bytes=1024*1024, retrieval_bytes=2*1024*1024,
                           upload_kib=1024, download_kib=1024)
        units = {n: {"LoadState": "loaded", "ActiveState": "inactive", "SubState": "dead", "MainPID": "0", "ControlPID": "0", "UnitFileState": "masked" if n in MASKED else "disabled"} for n in UNITS}
        units["nginx.service"].update(ActiveState="active", SubState="running", MainPID="1", UnitFileState="enabled")
        artifacts = {p: "5"*64 for p in ("/usr/bin/systemctl", "/usr/bin/systemd-run", "/usr/sbin/mysqld", "/usr/bin/mysql", "/usr/bin/mysqldump", "/etc/apparmor.d/usr.sbin.mysqld", "/etc/mysql-cold.cnf")}
        c["cold_host"] = {"machine_sha256": "1"*64, "boot_sha256": "2"*64,
                          "quarantine_sha256": "6"*64, "quarantine_file": "/etc/quarantine",
                          "mysql_config": "/etc/mysql-cold.cnf", "datadir": "/var/lib/mysql",
                          "mysql_identity": {"uid": 1200, "gid": 1201},
                          "mysql_log_directory": {"uid": 1200, "gid": 4, "mode": 0o750, "device": 1, "inode": 2},
                          "mysql_log_parent": {"uid": 0, "gid": 100, "mode": 0o775, "device": 1, "inode": 3},
                          "schema": "synthetic", "tables": [f"t{x:02}" for x in range(43)],
                          "baseline_units": units, "artifact_pins": artifacts}
        sources = [c[k] for k in ("public_root", "private_root", "release_root")] + c["settings"]
        c["expected_filesystems"] = {p: 1 for p in sources + [self.remote + "/" + n for n in ("state", "work", "cache", "temp")]}
        self.spec = {"config": c, "run_id": "a"*32, "observed_source_bytes": 100, "expected_http_status": 404,
                     "credential_references": {k: c[k] for k in ("password_file", "aws_credentials_file")},
                     "approved_source_paths": sources, "observed_members_sha256": "3"*64,
                     "observed_originals": {}}
        now = datetime.now(timezone.utc)
        raw = self.root / "raw-observation.txt"
        raw.write_bytes(b"synthetic raw observation; not live authority")
        for kind in ("repository_acl", "recovery_access", "source_inventory", "cost"):
            row = dict(record_type="MA_OBSERVED_ORIGINAL", kind=kind, classification="OBSERVED", synthetic=False,
                       repository_id=c["repository_id"], machine_sha256="1"*64, boot_sha256="2"*64,
                       observed_at=(now-timedelta(minutes=1)).isoformat(), expires_at=(now+timedelta(hours=1)).isoformat(),
                       originrefs=[{"path": str(raw), "sha256": digest(raw.read_bytes())}], access="PRIVATE_VERIFIED",
                       operation="EXACT_SNAPSHOT_READ_VERIFIED", snapshot_id="4"*64, members_sha256="3"*64,
                       upper_bound_krw=0, approved_limit_krw=0, new_bytes_bound=4*1024*1024,
                       scope="ONE_COLD_CAPTURE", unknown_components=[])
            path = self.root / (kind + ".json")
            path.write_bytes(encoded(row))
            self.spec["observed_originals"][kind] = {"path": str(path), "sha256": digest(encoded(row))}

    def test_credential_drift_rejected_before_inputs(self):
        self.spec['config']['password_file'] = self.remote + '/stolen-reference'
        with self.assertRaisesRegex(GateError, 'CREDENTIAL_BINDING'):
            assemble(self.bundle, self.spec)
        self.assertFalse((self.bundle / 'inputs').exists())

    def scheduled_spec(self):
        spec = copy.deepcopy(self.spec)
        del spec['run_id']
        start = (datetime.now(timezone(timedelta(hours=9))) + timedelta(minutes=2)).replace(second=0, microsecond=0)
        if (start + timedelta(seconds=900)).date() != start.date():
            start = (start + timedelta(days=1)).replace(hour=0, minute=0)
        manifest = load(self.bundle / 'manifest.json')
        python = {'resolved_path': '/usr/bin/python3.12', 'sha256': 'f' * 64}
        pins = {row['remote']: row['payload_sha256'] for row in manifest['files'].values()}
        pins[python['resolved_path']] = python['sha256']
        spec['scheduled'] = {'policy_id': 'synthetic-wi008-one-attempt', 'kst_date': str(start.date()),
                             'start_hhmm': start.strftime('%H:%M'), 'start_grace_seconds': 60, 'window_seconds': 900,
                             'not_before': start.isoformat(), 'expires_at': (start + timedelta(seconds=900)).isoformat(),
                             'python': python, 'code_pins': pins,
                             'budget': {'max_attempts': 1, 'per_attempt_bytes': 4 * 1024**2,
                                        'per_attempt_cost_krw': 0, 'existing_bytes': 100, 'reserve_bytes': 100,
                                        'capacity_bytes': 5 * 1024**2, 'cost_cap_krw': 0, 'max_output_bytes': 65536}}
        return spec

    def test_scheduled_assembly_v2_dispatch_and_retained_ledger(self):
        from scheduled_dispatch import dispatch
        from backup_protocol import timestamp
        spec = self.scheduled_spec()
        result = assemble_scheduled(self.bundle, spec)
        direct = load(self.bundle / 'execution-plan.json')
        plan = load(self.bundle / 'scheduled-execution-plan.json')
        config = load(self.bundle / 'inputs/config.json')
        policy = load(self.bundle / 'inputs/scheduled-policy.json')
        budget = load(self.bundle / 'inputs/scheduled-budget.json')
        pins = load(self.bundle / 'inputs/scheduled-pins.json')
        manifest = load(self.bundle / 'inputs/scheduled-manifest.json')
        self.assertIn('execute', direct['command'])
        self.assertIn(self.remote + '/code/scheduled_dispatch.py', plan['command'])
        self.assertEqual(plan['final_probe_binding']['run_id'], result['run_id'])
        self.assertEqual(plan['cleanup_command'][-1], self.remote + '/work/' + result['run_id'])
        self.assertEqual(plan['ledger'], config['state_dir'] + '/scheduled-ledger')
        self.assertFalse(plan['native_prerequisites_verified'])
        self.assertFalse((self.bundle / 'state').exists())
        self.assertEqual(set(policy['code_pins']), set(spec['scheduled']['code_pins']))
        self.assertEqual(pins[self.remote + '/inputs/config.json'], digest((self.bundle / 'inputs/config.json').read_bytes()))
        ledger = self.root / 'synthetic-ledger'
        ledger.mkdir(mode=0o700)
        receipt = encoded({'status': 'COMPLETE', 'restore': 'NOT_RUN', 'copy': 'NOT_RUN'})
        runner = SimpleNamespace(run=lambda argv, cap: SimpleNamespace(code=0, data=receipt, size=len(receipt)))
        with patch('scheduled_dispatch.verify_files'):
            response = dispatch(ledger, config, policy, budget, plan['dispatcher_argv'], pins, runner,
                                timestamp(policy['not_before']), manifest=manifest)
            self.assertEqual(response, {'status': 'COMPLETE'})
            with self.assertRaisesRegex(GateError, 'ATTEMPT_BUDGET_EXHAUSTED'):
                dispatch(ledger, config, policy, budget, plan['dispatcher_argv'], pins, runner,
                         timestamp(policy['not_before']), manifest=manifest)
        private_directory(self.bundle / 'inputs')

    def test_scheduled_mismatched_run_and_pins_reject_before_inputs(self):
        spec = self.scheduled_spec()
        spec['run_id'] = 'a' * 32
        with self.assertRaisesRegex(GateError, 'SCHEDULE_RUN_ID'):
            assemble_scheduled(self.bundle, spec)
        del spec['run_id']
        spec['scheduled']['code_pins']['/unexpected'] = 'b' * 64
        with self.assertRaisesRegex(GateError, 'SCHEDULE_CODE_PINS'):
            assemble_scheduled(self.bundle, spec)
        self.assertFalse((self.bundle / 'inputs').exists())

    def test_scheduled_existing_authority_preserved(self):
        spec = self.scheduled_spec()
        marker = self.bundle / 'scheduled-execution-plan.json'
        marker.write_bytes(b'synthetic prior authority - preserve')
        with self.assertRaisesRegex(GateError, 'EXISTING_ASSEMBLY_PRESERVED'):
            assemble_scheduled(self.bundle, spec)
        self.assertEqual(marker.read_bytes(), b'synthetic prior authority - preserve')
        self.assertFalse((self.bundle / 'inputs').exists())

    def test_scheduled_scope_and_time_rejected_before_inputs(self):
        spec = self.scheduled_spec()
        spec['scheduled']['budget']['max_attempts'] = 2
        with self.assertRaisesRegex(GateError, 'ONE_ATTEMPT_REQUIRED'):
            assemble_scheduled(self.bundle, spec)
        spec['scheduled']['budget']['max_attempts'] = 1
        spec['scheduled']['expires_at'] = spec['scheduled']['not_before']
        with self.assertRaisesRegex(GateError, 'SCHEDULE_AUTHORITY_WINDOW'):
            assemble_scheduled(self.bundle, spec)
        self.assertFalse((self.bundle / 'inputs').exists())

    def test_noncanonical_paths_are_rejected(self):
        from native_trial import remote_root, bind_trial_paths
        for value in (self.remote + '/', self.remote.replace('/wi022-', '//wi022-')):
            with self.assertRaisesRegex(GateError, 'FRESH_REMOTE_ROOT'):
                remote_root(value)
        refs = dict(self.spec['credential_references'])
        refs['password_file'] = '/root/../root/password'
        config = copy.deepcopy(self.spec['config'])
        config.update(refs)
        with self.assertRaisesRegex(GateError, 'CREDENTIAL_BINDING'):
            bind_trial_paths(config, remote_root(self.remote), refs)

    @unittest.skipUnless(os.name == 'nt', 'Native Windows ACL boundary')
    def test_windows_default_mode_directory_rejected_before_private_write(self):
        from native_trial import private_write_new
        unsafe = self.root / 'python-mode-only'
        unsafe.mkdir(mode=0o700)
        with self.assertRaisesRegex(GateError, 'PRIVATE_OUTPUT_ACL'):
            private_write_new(unsafe / 'synthetic.json', {'synthetic': True})
        self.assertFalse((unsafe / 'synthetic.json').exists())

    def test_exact_runtime_paths_do_not_relocate_credentials(self):
        assemble(self.bundle, self.spec)
        result = load(self.bundle / 'inputs/config.json')
        for key, value in self.spec['credential_references'].items():
            self.assertEqual(result[key], value)
        self.assertEqual(result['mysql_defaults_file'], self.remote + '/inputs/mysql-unused.cnf')
        private_directory(self.bundle / 'inputs')

    def test_lf_executable_bytes_pinned_and_complete(self):
        manifest = load(self.bundle / "manifest.json")
        self.assertEqual(set(manifest["files"]), set(FILES))
        for name, row in manifest["files"].items():
            data = (self.bundle / "code" / name).read_bytes()
            self.assertNotIn(b"\r", data)
            self.assertEqual(digest(data), row["payload_sha256"])
        self.assertTrue((self.bundle / "code/host_adapter.py").read_bytes().startswith(b"#!/usr/bin/python3 -B\n"))
        compile(BOOTSTRAP, "bootstrap", "exec")

    def test_assembly_binds_config_without_issuing_authority(self):
        result = assemble(self.bundle, self.spec)
        self.assertEqual(result["approval"], "NOT_ISSUED")
        c = load(self.bundle / "inputs/config.json")
        self.assertEqual(digest(encoded(c)), result["config_sha256"])
        plan = load(self.bundle / "execution-plan.json")
        self.assertIn("--property=Nice=10", plan["command"])
        self.assertIn("--property=RuntimeMaxSec=1080", plan["command"])
        self.assertIn("--property=StandardOutput=append:" + self.remote + "/state/executor.stdout", plan["command"])
        self.assertIn("--property=StandardError=append:" + self.remote + "/state/executor.stderr", plan["command"])
        self.assertFalse((self.bundle / "inputs/approval.json").exists())

    def test_no_overwrite(self):
        with self.assertRaisesRegex(GateError, "DESTINATION_EXISTS"):
            package(self.bundle, self.remote)

    def test_origin_tamper_rejected(self):
        (self.root / "raw-observation.txt").write_bytes(b"changed")
        with self.assertRaisesRegex(GateError, "ORIGIN_PIN"):
            assemble(self.bundle, self.spec)

    def test_source_scope_cannot_expand(self):
        self.spec["config"]["settings"].append("/etc/new-secret")
        with self.assertRaisesRegex(GateError, "APPROVED_SOURCE_SCOPE"):
            assemble(self.bundle, self.spec)

    def test_dump_included_in_source_capacity(self):
        self.spec["observed_source_bytes"] = 2*1024*1024
        with self.assertRaisesRegex(GateError, "SOURCE_PLUS_DUMP_REQUIRED"):
            assemble(self.bundle, self.spec)

    def test_short_transfer_window_rejected(self):
        self.spec["config"]["limits"]["seconds"] = 300
        with self.assertRaisesRegex(GateError, "TRANSFER_WINDOW_TOO_SHORT"):
            assemble(self.bundle, self.spec)

    def test_probe_payload_is_inert_and_bound(self):
        target = self.bundle / "probe-input.json"
        result = probe_payload(self.bundle, {"remote_root": self.remote}, target)
        self.assertEqual(result["status"], "PROBE_PAYLOAD_PREPARED_NOT_EXECUTED")
        self.assertEqual(set(json.loads(target.read_bytes())["code"]), set(FILES))

    def test_existing_bucket_root_repository_allowed_without_new_prefix(self):
        config = config_fixture()
        config["repository"] = "s3:https://storage.example.invalid/existing-bucket"
        with self.assertRaisesRegex(GateError, "PRIVATE_REPOSITORY"):
            validate_config(config)
        config["repository_layout"] = "existing-bucket-root"
        validate_config(config)
        for uri in ("s3:https://storage.example.invalid/", "s3:https://user:pass@storage.example.invalid/bucket", "s3:https://storage.example.invalid/bucket?key=value"):
            config["repository"] = uri
            with self.assertRaisesRegex(GateError, "PRIVATE_REPOSITORY"):
                validate_config(config)

    def test_incomplete_host_config_rejected_locally(self):
        del self.spec["config"]["cold_host"]["tables"]
        with self.assertRaises(KeyError):
            assemble(self.bundle, self.spec)

    def test_procfs_boot_attribute_does_not_use_regular_file_size(self):
        raw = b"11111111-2222-3333-4444-555555555555\n"
        with patch.object(Path, "open", mock_open(read_data=raw)), patch.object(Path, "stat", side_effect=AssertionError("proc stat size is zero")):
            self.assertEqual(boot_identity(), digest(raw))
        with patch.object(Path, "open", mock_open(read_data=b"not-a-boot-id")):
            with self.assertRaisesRegex(GateError, "BOOT_ATTRIBUTE"):
                boot_identity()

    def test_quarantine_status_and_config_are_exactly_bound(self):
        for status in (404, 503):
            spec = {"expected_http_status": status, "quarantine_sha256": "a" * 64}
            verify_quarantine_response(spec, str(status).encode(), "a" * 64)
            with self.assertRaisesRegex(GateError, "HTTP_QUARANTINE_STATUS_MISMATCH"):
                verify_quarantine_response(spec, b"200", "a" * 64)
            with self.assertRaisesRegex(GateError, "QUARANTINE_CONFIG_DRIFT"):
                verify_quarantine_response(spec, str(status).encode(), "b" * 64)
        for status in (200, True, "404"):
            with self.assertRaisesRegex(GateError, "QUARANTINE_STATUS_BINDING"):
                verify_quarantine_response({"expected_http_status": status, "quarantine_sha256": "a" * 64}, b"404", "a" * 64)

    def test_explicit_s3_path_and_region_are_fixed_options(self):
        config = config_fixture()
        baseline = restic_command(config, ["cat", "config"])
        self.assertFalse(any(x.startswith("s3.bucket-lookup=") or x.startswith("s3.region=") for x in baseline))
        config.update(s3_bucket_lookup="path", region="default")
        validate_config(config)
        command = restic_command(config, ["cat", "config"])
        self.assertEqual(command[-6:], ["-o", "s3.bucket-lookup=path", "-o", "s3.region=default", "cat", "config"])
        for key, value in (("s3_bucket_lookup", "virtual"), ("s3_bucket_lookup", "path -o unsafe"), ("region", "default\n-o unsafe")):
            changed = {**config, key: value}
            with self.assertRaisesRegex(GateError, "S3_PATH_OPTIONS"):
                validate_config(changed)
            with self.assertRaisesRegex(GateError, "S3_PATH_OPTIONS"):
                restic_command(changed, ["cat", "config"])

    def test_only_exact_cold_setting_owner_binding_is_allowed(self):
        path = "/var/lib/atstudio/tmp"
        row = {"uid": 1200, "gid": 1201, "mode": 0o700, "device": 1, "inode": 23}
        config = config_fixture()
        config.update(maintenance_mode="cold-preserve", cold_host={"source_ownership": {path: row}})
        config["settings"].append(path)
        validate_config(config)
        info = SimpleNamespace(st_uid=1200, st_gid=1201, st_mode=stat.S_IFDIR | 0o700, st_dev=1, st_ino=23)
        with patch("backup_runtime.checked_path", return_value=Path(path).parent) as ancestors, patch.object(Path, "lstat", return_value=info):
            self.assertEqual(checked_setting_source(config, path), Path(path))
            ancestors.assert_called_once_with(Path(path).parent, trusted=True)
        for change in ({"st_uid": 1202}, {"st_mode": stat.S_IFDIR | 0o770}, {"st_mode": stat.S_IFLNK | 0o700}):
            changed = SimpleNamespace(**{**vars(info), **change})
            with patch("backup_runtime.checked_path"), patch.object(Path, "lstat", return_value=changed), self.assertRaises(GateError):
                checked_setting_source(config, path)
        with patch("backup_runtime.checked_path", side_effect=GateError("PATH_OWNERSHIP")), self.assertRaisesRegex(GateError, "PATH_OWNERSHIP"):
            checked_setting_source(config, path)
        config["maintenance_mode"] = "live-resume"
        with self.assertRaisesRegex(GateError, "COLD_SOURCE_SCOPE"):
            checked_setting_source(config, path)
        with self.assertRaisesRegex(GateError, "COLD_SOURCE_SCOPE"):
            validate_config(config)


if __name__ == "__main__":
    unittest.main()
