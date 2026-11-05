"""Synthetic stores and fake SMTP only; no network/native service or secrets."""
from contextlib import contextmanager
import unittest
from unittest.mock import patch

from backup_protocol import GateError, encoded, timestamp
import monitor_runner as runner
from test_alert_delivery import FakeSMTP


class Store:
    def __init__(self):
        self.data = {}
    def read(self, name, missing=False):
        if missing:
            return self.data.get(name)
        return self.data[name]
    def write(self, name, data):
        self.data[name] = data
    def close(self):
        pass
    @contextmanager
    def lock(self):
        yield


class RunnerTests(unittest.TestCase):
    def setUp(self):
        self.now = timestamp("2026-10-28T03:00:00Z")
        self.c = {"version": 1, "enabled": True, "mode": "test", "test_id": "case1", "root": "/synthetic",
                  "source_machine_sha256": "a" * 64, "monitor_machine_sha256": "b" * 64,
                  "timezone": "Asia/Seoul", "start_hhmm": "03:00", "window_seconds": 900,
                  "not_before": "2026-10-28T00:00:00Z", "expires_at": "2026-10-29T00:00:00Z",
                  "key_ref": "key", "max_export_age_seconds": 3600, "send_authorized": True,
                  "smtp": {"host": "smtp.example.invalid", "port": 465, "sender": "a@example.invalid",
                           "recipient": "b@example.invalid", "credential_ref": "smtp"}}
        self.source, self.monitor = {}, {}
        self.claims = set()
        FakeSMTP.seen, FakeSMTP.failure = [], False

    def factory(self, stores):
        def get(path):
            if path not in stores:
                stores[path] = Store()
            return stores[path]
        return get

    def provision(self, stores):
        p = runner.layout(self.c)
        get = self.factory(stores)
        get(p['journal']).write('key', b'synthetic-key-32-bytes-not-a-secret!')
        get(p['journal']).write('smtp', encoded({'username': 'fake', 'password': 'fake'}))
        return get, p

    def reserve(self, path, event):
        item = path, event
        if item in self.claims:
            return False
        self.claims.add(item)
        return True

    def test_export_monitor_duplicate_and_test_label(self):
        get, p = self.provision(self.source)
        state = {'attempt': {'stage': 'FAILED', 'maintenance': False}}
        get(p['capture']).write('state.json', encoded(state))
        with patch('scheduled_dispatch.ledger_failure_code', return_value='DISPATCH_UNCERTAIN'):
            result = runner.run(self.c, 'export', self.now, 'a'*64, store_factory=get)
        self.assertEqual(result['status'], 'EXPORTED')
        receive, p = self.provision(self.monitor)
        receive(p['inbox']).write('handoff.json', get(p['outbox']).read('handoff.json'))
        with patch('alert_delivery.reserve', side_effect=self.reserve):
            result = runner.run(self.c, 'monitor', self.now, 'b'*64, store_factory=receive, smtp_factory=FakeSMTP)
            duplicate = runner.run(self.c, 'monitor', self.now+1, 'b'*64, store_factory=receive, smtp_factory=FakeSMTP)
        self.assertEqual(result['delivery'], 'SMTP_ACCEPTED')
        self.assertEqual(duplicate['delivery'], 'RESERVED_HOLD')
        self.assertIn('[TEST case1]', FakeSMTP.seen[-1])

    def test_missing_source_and_unknown_send_hold(self):
        get, p = self.provision(self.monitor)
        FakeSMTP.failure = True
        with patch('alert_delivery.reserve', side_effect=self.reserve):
            first = runner.run(self.c, 'monitor', self.now, 'b'*64, store_factory=get, smtp_factory=FakeSMTP)
            second = runner.run(self.c, 'monitor', self.now+1, 'b'*64, store_factory=get, smtp_factory=FakeSMTP)
        self.assertIn('CAPTURE_MISSED', first['alerts'])
        self.assertEqual(first['delivery'], 'OUTCOME_UNKNOWN_HOLD')
        self.assertEqual(second['delivery'], 'RESERVED_HOLD')

    def test_host_and_namespace_collisions(self):
        with self.assertRaises(GateError):
            runner.run(self.c, 'monitor', self.now, 'a'*64)
        for extra in ({'capture_directory':'/production'}, {'monitor_machine_sha256':'a'*64}, {'test_id':'../prod'}):
            with self.subTest(extra=extra), self.assertRaises(GateError):
                runner.layout({**self.c, **extra})

    def test_expiry_and_send_authority(self):
        get, p = self.provision(self.monitor)
        for config, now in ((self.c, self.now+86400), ({**self.c, 'send_authorized':False}, self.now)):
            with self.assertRaises(GateError):
                runner.run(config,'monitor',now,'b'*64,store_factory=get,smtp_factory=FakeSMTP)
        self.assertEqual(FakeSMTP.seen, [])

    def test_stable_kst_window_and_separate_mode(self):
        before = timestamp('2026-10-27T17:59:59Z')
        after = timestamp('2026-10-27T18:00:00Z')
        self.assertTrue(runner.window(self.c,before)[0].endswith('20261027'))
        self.assertTrue(runner.window(self.c,after)[0].endswith('20261028'))
        prod = {**self.c, 'mode':'production', 'test_id':None, 'capture_directory':'/capture', 'dispatch_directory':'/dispatch'}
        self.assertNotEqual(runner.layout(prod)['alerts'],runner.layout(self.c)['alerts'])
        self.assertNotEqual(runner.window(prod,after)[0],runner.window(self.c,after)[0])

    def test_default_cli_and_checker_error_are_bounded(self):
        from contextlib import redirect_stdout
        import io
        out=io.StringIO()
        with redirect_stdout(out):
            self.assertEqual(runner.main([]),0)
            self.assertEqual(runner.main(['--execute','--action','monitor','--config','/missing']),2)
        self.assertIn('MONITOR_RUNNER_FAILED',out.getvalue())
        self.assertNotIn('/missing',out.getvalue())

    def test_supervisor_status(self):
        self.assertEqual(runner.supervisor_exit({'status':'EXPORTED'}), 0)
        self.assertEqual(runner.supervisor_exit({'status':'CURRENT','delivery':'NO_ALERT'}), 0)
        for delivery in ('SMTP_ACCEPTED', 'RESERVED_HOLD', 'OUTCOME_UNKNOWN_HOLD'):
            self.assertEqual(runner.supervisor_exit({'status':'ATTENTION','delivery':delivery}), 2)


if __name__ == '__main__':
    unittest.main()
