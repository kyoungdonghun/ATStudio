"""WI005 independent synthetic boundary checks; never starts native services."""
import copy
import json
from pathlib import Path
import sys
import tempfile
import unittest
from datetime import datetime, timezone
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[3]
sys.path.insert(0, str(ROOT / 'scripts/deployment/backup'))
import phase_b_trial as trial
import online_restore as restore
from backup_protocol import GateError, digest, encoded


class IdentityAndSqlTests(unittest.TestCase):
    def test_identity_dual_representation_never_conflates_newline(self):
        raw = b'0123456789abcdef0123456789abcdef\n'
        pair = trial.identity_pair(raw)
        self.assertEqual(pair['raw'], digest(raw))
        self.assertEqual(pair['canonical'], digest(raw.rstrip(b'\n')))
        self.assertNotEqual(pair['raw'], pair['canonical'])

    def test_sql_binary_and_escaped_strings_preserve_value_identity(self):
        source = (b'CREATE TABLE `t` (`id` int,`v` varbinary(32));\n'
                  b"INSERT INTO `t` VALUES (7,0x6162);\n"
                  b"INSERT INTO `t` VALUES (8,'quote\\\'; semicolon');\n")
        result = restore.compare_sql(source, source, 'ats', ['t'])
        self.assertEqual(result['rows'], {'t': 2})
        with self.assertRaisesRegex(GateError, 'RESTORE_SQL_DIFFERENT'):
            restore.compare_sql(source, source.replace(b'0x6162', b'0x6163'), 'ats', ['t'])

    def test_executable_comment_cannot_hide_schema_mutation(self):
        base = b'CREATE TABLE `t` (`id` int);\n'
        for hostile in (b'/*!50003 DROP TABLE `t` */;',
                        b'/*!50003 SET @x=1; DROP TABLE `t` */;',
                        b'CREATE EVENT `x` ON SCHEDULE EVERY 1 HOUR DO SELECT 1;'):
            with self.subTest(hostile=hostile), self.assertRaises(GateError):
                restore.sql_model(base + hostile, 'ats', ['t'])


class CaptureBindings(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.capture = self.root / ('a' * 32)
        self.capture.mkdir()
        dump = b'CREATE TABLE `t` (`id` int);\nINSERT INTO `t` VALUES (1);\n'
        self.dump = self.capture / 'database.sql'
        self.dump.write_bytes(dump)
        row = {'type': 'file', 'sha256': digest(dump), 'size': len(dump),
               'uid': 0, 'gid': 0, 'mode': 0o600}
        manifest = {'version': 1, 'protocol': 'ats-online-v1', 'capture_id': 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
                    'manifest_sha256': '1' * 64, 'dump_sha256': digest(dump),
                    'config_sha256': '2' * 64, 'members': {str(self.dump): row}}
        manifest_raw = encoded(manifest)
        (self.capture / 'manifest.json').write_bytes(manifest_raw)
        self.receipt = {'version': 1, 'protocol': 'ats-online-v1', 'status': 'VERIFIED',
                        'run_id': self.capture.name, 'capture_id': manifest['capture_id'],
                        'manifest_sha256': manifest['manifest_sha256'], 'dump_sha256': digest(dump),
                        'content_manifest_sha256': digest(manifest_raw), 'snapshot_id': '3' * 64,
                        'repository_id': '4' * 64, 'helpers_ended': True,
                        'inventory_verified': True, 'content_verified': True, 'restore_verified': False}
        (self.capture / 'verified-receipt.json').write_bytes(encoded(self.receipt))
        prep = {k: self.receipt[k] for k in ('capture_id', 'dump_sha256', 'content_manifest_sha256', 'snapshot_id', 'repository_id')}
        prep.update(version=1, execution_authorized=False, restore_verified=False)
        (self.capture / 'restore-preparation.json').write_bytes(encoded(prep))
        self.state = {'stage': 'COMPLETE', 'run_id': self.capture.name,
                      'capture_id': self.receipt['capture_id'], 'last_good': self.receipt,
                      'receipt_sha256': digest(encoded(self.receipt))}
        self.state_path = self.root / 'online-state.json'
        self.state_path.write_bytes(encoded(self.state))
        self.config = {'online_state_path': str(self.state_path), 'repository_id': '4' * 64,
                       'capture_config_sha256': '2' * 64,
                       'limits': {'metadata_bytes': 1024 * 1024, 'dump_bytes': 1024 * 1024}}

    def bind(self):
        # Windows temp paths stand in for private Linux paths for binding-only tests.
        # Pure path policy is checked separately; no native or filesystem-ownership PASS implied.
        with patch.object(restore, 'linux_path', side_effect=lambda p: p):
            return restore.bind_capture(self.capture, self.config)

    def test_real_online_receipt_shape_binds_and_preserves_capture_flag(self):
        receipt, members = self.bind()
        self.assertEqual(receipt['snapshot_id'], '3' * 64)
        self.assertEqual(members[str(self.dump)]['sha256'], self.receipt['dump_sha256'])
        self.assertIs(receipt['restore_verified'], False)

    def test_hold_capture_never_restores_even_with_verified_receipt(self):
        self.state['stage'] = 'HOLD'
        self.state_path.write_bytes(encoded(self.state))
        with self.assertRaisesRegex(GateError, 'RESTORE_CAPTURE_NOT_COMPLETE'):
            self.bind()

    def test_state_for_another_attempt_is_not_this_completed_capture(self):
        self.state['run_id'] = 'b' * 32
        self.state_path.write_bytes(encoded(self.state))
        with self.assertRaises(GateError):
            self.bind()

    def test_prepare_snapshot_substitution_rejected(self):
        path = self.capture / 'restore-preparation.json'
        value = json.loads(path.read_bytes())
        value['snapshot_id'] = '5' * 64
        path.write_bytes(encoded(value))
        with self.assertRaisesRegex(GateError, 'RESTORE_PREPARATION_BINDING'):
            self.bind()

    def test_content_manifest_tamper_rejected_before_any_fetch(self):
        path = self.capture / 'manifest.json'
        value = json.loads(path.read_bytes())
        value['members'][str(self.dump)]['sha256'] = '6' * 64
        path.write_bytes(encoded(value))
        with self.assertRaisesRegex(GateError, 'RESTORE_MANIFEST_BINDING'):
            self.bind()


class NativeRestoreContract(unittest.TestCase):
    def test_target_inside_original_database_rejected_even_with_omitted_source(self):
        from test_online_restore import config
        c = config()
        c['target_parent'] = '/var/lib/mysql'
        c['target_root'] = c['target_parent'] + '/' + c['restore_id']
        c['source_roots'] = [c['capture_dir']]
        a = {'operation': c['operation'], 'config_sha256': digest(encoded(c)),
             'not_before': '2026-10-30T00:00:00Z', 'not_after': '2026-10-30T00:05:00Z'}
        with self.assertRaisesRegex(GateError, 'RESTORE_TARGET'):
            restore.validate(c, a, restore.timestamp('2026-10-30T00:01:00Z'))

    def test_expired_main_monitor_cannot_prevent_second_unit_cleanup(self):
        from test_online_restore import config
        class Runner:
            active = {}
            def stop_owned(self):
                pass
        class Expired:
            def tick(self, *args, **kwargs):
                raise GateError('EXPIRED')
        backend = restore.NativeRestoreBackend(config(), {}, Runner(), Expired())
        backend.client = Runner()
        backend.units = {'first.service', 'second.service'}
        stopped = []
        def show(unit):
            if unit == 'first.service':
                raise GateError('ONE_UNIT_QUERY_FAILED')
            return {'LoadState': 'loaded', 'Description': backend.description,
                    'MainPID': '0', 'ControlPID': '0'}
        backend.show = show
        backend.command = lambda args, *rest: stopped.append(args)
        with self.assertRaisesRegex(GateError, 'RESTORE_CLEANUP_UNCERTAIN'):
            backend.stop_owned()
        self.assertEqual(stopped, [['/usr/bin/systemctl', 'stop', '--', 'second.service']])


class JoinedTrialContract(unittest.TestCase):
    """Exercise real phase gates with synthetic path/read observations, never native."""
    def capture_fixture(self):
        from test_phase_b_trial import configuration
        config = configuration()
        prepared = trial.plan(config)
        root = config['root']
        online = dict(config['capture_contract'])
        online.update(public_root=config['public_root'], private_root=config['private_root'],
                      hook_binary=config['online_executor'], release_root=prepared['release'],
                      release_link=root + '/release-link', mysql_defaults_file=root + '/inputs/mysql.cnf',
                      lock_file='/run/atstudio-operations/operations.lock',
                      online=dict(schema=config['schema'], machine_sha256=config['identity']['machine']['canonical'],
                                  boot_sha256=config['identity']['boot']['canonical'], bridge_dir=prepared['bridge'],
                                  app_uid=config['app_identity']['uid'], app_gid=config['app_identity']['gid']),
                      limits=dict(source_bytes=256*trial.MIB, dump_bytes=16*trial.MIB,
                                  metadata_bytes=16*trial.MIB, retrieval_bytes=288*trial.MIB,
                                  seconds=1800, upload_kib=1024, download_kib=1024, cost_limit_krw=0))
        for key in ('state_dir', 'work_dir', 'cache_dir', 'temp_dir'):
            online[key] = root + '/' + key
        ready = {'run_id': config['run_id'], 'config_sha256': prepared['config_sha256'], 'deadline': 4000}
        approval = {'run_id': config['run_id'], 'not_after': datetime.fromtimestamp(3500, timezone.utc).isoformat()}
        blobs = {config['online_config']: encoded(online), config['online_approval']: encoded(approval),
                 root + '/trial-ready.json': encoded(ready),
                 root + '/inputs/mysql.cnf': ('[client]\nprotocol=socket\nsocket=' + prepared['socket'] + '\nuser=root\n').encode()}
        inner = {'trial_config_sha256': prepared['config_sha256'], 'run_id': config['run_id'],
                 'ready_sha256': digest(encoded(ready)), 'online_config_sha256': digest(encoded(online)),
                 'online_approval_sha256': digest(encoded(approval))}
        blobs[root + '/inputs/capture-stage-binding.json'] = encoded(inner)
        from pathlib import PurePosixPath
        class ObservedPath:
            def __init__(self, value): self.value = PurePosixPath(str(value))
            def __str__(self): return str(self.value)
            def __truediv__(self, child): return ObservedPath(self.value / child)
            def __eq__(self, other): return str(self) == str(other)
            def read_bytes(self): return blobs[str(self)]
            def exists(self): return str(self) in blobs
            def resolve(self, **kw): return ObservedPath(prepared['release']) if str(self).endswith('/release-link') else self
        return config, prepared, online, blobs, ObservedPath

    def test_exact_outer_scope_survives_changed_inner_pin(self):
        config, prepared, online, blobs, ObservedPath = self.capture_fixture()
        # A JAR-only candidate may not silently replace the retained frontend set.
        omitted = copy.deepcopy(config)
        omitted['capture_contract']['settings'].remove('/opt/atstudio/releases/wi060-web-20261011')
        with self.assertRaisesRegex(GateError, 'RETAINED_SOURCE_COVERAGE'):
            trial.plan(omitted)
        def bind():
            with patch.object(trial, 'Path', ObservedPath), \
                 patch.object(trial, 'checked_path', side_effect=lambda p, **kw: ObservedPath(p)), \
                 patch.object(trial, 'read_json', side_effect=lambda p, *args: json.loads(blobs[str(p)])), \
                 patch.object(trial.time, 'time', return_value=1000):
                trial.NativeTrial().bind_capture(config, prepared)
        bind()
        for key, value in (('repository', 's3:https://different.invalid/other'),
                           ('settings', ['/etc']), ('public_root', '/other/source')):
            changed = copy.deepcopy(online); changed[key] = value
            blobs[config['online_config']] = encoded(changed)
            inner_path = config['root'] + '/inputs/capture-stage-binding.json'
            inner = json.loads(blobs[inner_path]); inner['online_config_sha256'] = digest(encoded(changed))
            blobs[inner_path] = encoded(inner)
            with self.subTest(field=key), self.assertRaises(GateError): bind()

    def test_raw_identity_cannot_substitute_for_online_canonical_identity(self):
        config, prepared, online, blobs, ObservedPath = self.capture_fixture()
        online['online']['machine_sha256'] = config['identity']['machine']['raw']
        blobs[config['online_config']] = encoded(online)
        inner_path = config['root'] + '/inputs/capture-stage-binding.json'
        inner = json.loads(blobs[inner_path]); inner['online_config_sha256'] = digest(encoded(online))
        blobs[inner_path] = encoded(inner)
        with patch.object(trial, 'Path', ObservedPath), \
             patch.object(trial, 'checked_path', side_effect=lambda p, **kw: ObservedPath(p)), \
             patch.object(trial, 'read_json', side_effect=lambda p, *args: json.loads(blobs[str(p)])), \
             patch.object(trial.time, 'time', return_value=1000):
            with self.assertRaisesRegex(GateError, 'ONLINE_IDENTITY_REPRESENTATION'):
                trial.NativeTrial().bind_capture(config, prepared)

    def test_owned_session_cleanup_on_authority_failure_before_capture(self):
        from test_phase_b_trial import configuration
        c = configuration(); events = []
        class Backend:
            def capture(self, *args): events.append('CAPTURE')
            def stop_owned(self, *args): events.append('STOP')
        for error in (GateError('EXPIRED_PROOF'), FileExistsError('PRESERVED_CLAIM')):
            events.clear()
            def reject(): raise error
            with self.assertRaises(type(error)):
                trial.finish_capture(c, trial.plan(c), Backend(), reject)
            self.assertEqual(events, ['STOP'])

    def test_real_capture_receipt_flows_to_restore_only_after_owned_stop(self):
        from test_online_capture import OnlineCaptureTests, SNAPSHOT
        from test_online_restore import config as restore_config
        from test_phase_b_trial import configuration
        fixture = OnlineCaptureTests('test_connected_success_hashes_actual_selected_files_and_releases_only_after_verification')
        fixture.setUp()
        self.addCleanup(fixture.doCleanups)
        capture, peer, repository = fixture.capture()
        events = []
        class TrialBackend:
            def capture(self, *args):
                events.append('CAPTURE')
                self.result = capture.run()
            def stop_owned(self, *args): events.append('STOP')
        c = configuration()
        trial.finish_capture(c, trial.plan(c), TrialBackend(), lambda: None)
        receipt_path = capture.directory / 'verified-receipt.json'
        original_receipt = receipt_path.read_bytes()
        c = restore_config()
        c.update(capture_dir=str(capture.directory), online_state_path=str(capture.state_path),
                 capture_receipt_sha256=digest(original_receipt), capture_config_sha256=digest(encoded(fixture.config)),
                 repository_id=fixture.config['repository_id'], source_roots=[str(fixture.root)],
                 schema='fixture', tables=['tracks'], target_root=str(fixture.root / 'quarantine'))
        owner = self
        class RestoreBackend:
            environment = 'SIMULATED_WINDOWS'
            closed = False
            def now(self): return 1
            def tick(self): pass
            def preflight(self, *args):
                owner.assertEqual(events, ['CAPTURE', 'STOP'])
                events.append('RESTORE')
            def verify_snapshot(self, receipt, members, directory):
                owner.assertEqual(receipt['snapshot_id'], SNAPSHOT)
                owner.assertEqual(set(members) | {str(directory / 'manifest.json')}, set(repository.blobs))
            def fetch(self, snapshot, source, output, cap):
                owner.assertEqual(snapshot, SNAPSHOT)
                owner.assertLessEqual(len(repository.blobs[source]), cap)
                output.write_bytes(repository.blobs[source])
            def roundtrip(self, dump, target): return dump.read_bytes()
            def stop_owned(self): self.closed = True
            def ended(self): return self.closed
        # Only Windows path/outer native authority is substituted. Capture, receipt
        # cross-binding, content hashes, SQL comparison and cleanup execute real code.
        with patch.object(restore, 'validate', lambda *a: None), \
             patch.object(restore, 'linux_path', lambda p: p):
            result = restore.RestoreSession(c, {}, capture.directory, RestoreBackend()).run()
        self.assertEqual(result['status'], 'SIMULATED_RESTORE_VERIFIED')
        self.assertFalse(result['restore_verified'])
        self.assertEqual(result['sql']['rows'], {'tracks': 1})
        self.assertEqual(result['files_verified'], len(repository.blobs) - 1)
        self.assertEqual(receipt_path.read_bytes(), original_receipt)
        self.assertEqual(peer.actions, ['FREEZE', 'SEAL', 'RELEASE'])


if __name__ == '__main__':
    unittest.main()
