"""WI003 local-only tests. No service, DB, network or credential access."""
import copy
import json
from pathlib import Path
import tempfile
import unittest
import stat
from types import SimpleNamespace
from unittest.mock import patch

import phase_b_trial as trial
from backup_protocol import GateError, encoded, digest
from host_adapter import UNITS, MASKED


def configuration():
    run = '1' * 32
    root = '/var/lib/atstudio-online-trial/' + run
    config = dict(version=1, scope='ONE_ONLINE_TRIAL', run_id=run, root=root,
        identity={key: trial.identity_pair(b'example-id\n') for key in ('machine', 'boot')},
        jar_sha256=trial.JAR_SHA, jar_bytes=trial.JAR_BYTES,
        app_identity={'uid': 999, 'gid': 988}, mysql_identity={'uid': 110, 'gid': 110},
        public_root='/srv/atstudio/public', private_root='/srv/atstudio/private', tmp_root='/var/lib/atstudio/tmp',
        datadir='/var/lib/mysql', mysql_config='/etc/mysql/mysql.conf.d/mysqld.cnf', mysql_log_dir='/var/log/mysql',
        environment_file=root + '/inputs/app.env', online_config=root + '/inputs/online.json',
        online_approval=root + '/inputs/online-approval.json', online_executor=root + '/code/online_capture.py',
        python='/usr/bin/python3.12', java='/usr/lib/jvm/java-17-openjdk-amd64/bin/java', schema='atstudio_prod',
        app_port=18081, db_port=13306, helper_allow_ips=['192.0.2.10/32'], helper_write_paths='/run/atstudio-operations',
        limits={'app_memory': 3*trial.GIB, 'db_memory': 768*trial.MIB, 'helper_memory': trial.GIB,
                'reserve_memory': trial.GIB, 'seconds': 1800, 'source_bytes': 256*trial.MIB,
                'dump_bytes': 16*trial.MIB, 'metadata_bytes': 16*trial.MIB,
                'capture_retrieval_bytes': 288*trial.MIB, 'restore_retrieval_bytes': 288*trial.MIB})
    config['capture_contract'] = dict(target_host='example.invalid', repository='s3:https://example.invalid/bucket', repository_id='4'*64, region='test', password_file='/private/password', aws_credentials_file='/private/credentials', restic_binary='/opt/bin/restic', restic_sha256='4'*64, settings=['/var/lib/atstudio/tmp','/opt/atstudio/releases/wi060-web-20261011','/opt/atstudio/wi067-tools'])
    config['pins'] = {config[k]: '2'*64 for k in ('java','python','online_executor','mysql_config','environment_file')}
    config['pins'].update({k:'2'*64 for k in ('/usr/bin/systemctl','/usr/bin/systemd-run','/usr/sbin/mysqld','/usr/bin/mysql')})
    config['baseline_units'] = {name: {'ActiveState':'inactive','SubState':'dead','MainPID':'0','ControlPID':'0',
                                     'UnitFileState':'masked' if name in MASKED else 'static'} for name in UNITS}
    config['baseline_units']['nginx.service'].update(ActiveState='active',SubState='running',MainPID='100')
    return config


def authority(config):
    approval = dict(version=1, scope='ONE_ONLINE_TRIAL', config_sha256=digest(encoded(config)), run_id=config['run_id'],
                    not_before='2026-10-30T10:00:00Z', not_after='2026-10-30T11:00:00Z', cost_limit_krw=0)
    proof = dict(scope='ONE_ONLINE_TRIAL',config_sha256=approval['config_sha256'],run_id=config['run_id'],
                 identity=config['identity'],source_evidence_sha256='3'*64,
                 observed_at='2026-10-30T10:00:00Z',expires_at='2026-10-30T11:00:00Z',cost_upper_bound_krw=0,
                 retrieval_bound_bytes=576*trial.MIB,
                 checks={k:True for k in ('cold_baseline','source_inventory','no_other_writers','nginx_quarantine',
                    'settings_allowlist','source_database_and_accounts','helper_egress_enforcement','repository_acl','cost_and_retrieval')})
    return approval,proof


class FakeBackend:
    def __init__(self, fail=None): self.events=[]; self.fail=fail
    def event(self, name):
        self.events.append(name)
        if self.fail==name: raise GateError('SYNTHETIC_FAILURE')
    def preflight(self,*args): self.event('preflight')
    def claim(self,*args): self.event('claim')
    def start(self,name,*args): self.event(name)
    def db_ready(self,*args): self.event('db_ready')
    def app_ready(self,*args): self.event('app_ready')
    def capture(self,*args): self.event('capture')
    def stop_owned(self,*args): self.event('stop')


class TrialTests(unittest.TestCase):
    def test_plan_no_commands_executed_and_sequential_resource_bound(self):
        with patch('subprocess.Popen',side_effect=AssertionError('NO_PROCESS')):
            result=trial.plan(configuration())
        self.assertEqual(result['status'],'PREPARED_NOT_AUTHORITY')
        self.assertEqual(result['retrieval_bound_bytes'],576*trial.MIB)
        self.assertEqual(result['peak_memory_bytes'],int(5.75*trial.GIB))

    def test_private_namespace_and_jdbc_socket_split(self):
        p=trial.plan(configuration()); db=p['commands']['db']; app=p['commands']['app']
        self.assertIn('--property=PrivateNetwork=yes',db)
        self.assertIn('--property=JoinsNamespaceOf='+p['units']['db'],app)
        self.assertIn('--socket=/run/mysqld/mysqld.sock',db)
        self.assertIn('--bind-address=127.0.0.1',db)
        self.assertIn('--property=IPAddressDeny=any',p['commands']['helper'])
        self.assertNotIn('-I',p['commands']['helper'])

    def test_no_schema_scheduler_or_seed_enable(self):
        app=trial.plan(configuration())['commands']['app']
        for flag in ('--spring.sql.init.mode=never','--spring.jpa.hibernate.ddl-auto=validate',
                     '--app.background-tasks.enabled=false','--app.background-tasks.capture-control-only=true',
                     '--app.audio.worker-enabled=false','--app.bootstrap.test-users.enabled=false'):
            self.assertIn(flag,app)

    def test_manager_jvm_and_spring_injection_variables_removed(self):
        app=trial.plan(configuration())['commands']['app']
        removed=next(value for value in app if value.startswith('--property=UnsetEnvironment='))
        for name in ('JAVA_TOOL_OPTIONS','_JAVA_OPTIONS','JDK_JAVA_OPTIONS','SPRING_APPLICATION_JSON','SPRING_CONFIG_IMPORT'):
            self.assertIn(name,removed)

    def test_manager_python_path_and_interactive_injection_removed(self):
        helper=trial.plan(configuration())['commands']['helper']
        removed=next(value for value in helper if value.startswith('--property=UnsetEnvironment='))
        for name in ('PYTHONPATH','PYTHONHOME','PYTHONSTARTUP','PYTHONINSPECT','PYTHONSAFEPATH'):
            self.assertIn(name,removed)

    def test_legacy_and_canonical_identity_not_conflated(self):
        pair=trial.identity_pair(b'id\n')
        self.assertNotEqual(pair['raw'],pair['canonical'])
        self.assertEqual(pair['canonical'],digest(b'id'))

    def test_reject_broad_write_or_egress(self):
        for field,value in [('helper_write_paths','/var'),('helper_allow_ips',['0.0.0.0/0'])]:
            c=configuration();c[field]=value
            with self.assertRaises(GateError):trial.plan(c)

    def test_reject_wrong_jar_or_originalroot(self):
        for field,value in [('jar_sha256','5'*64),('root','/srv/atstudio/private')]:
            c=configuration();c[field]=value
            with self.assertRaises(GateError):trial.plan(c)

    def test_reject_missing_baseline(self):
        c=configuration();c['baseline_units']={}
        with self.assertRaises(GateError):trial.plan(c)

    def test_missing_evidence_prevents_all_effects(self):
        c=configuration();a,p=authority(c);p['checks']['repository_acl']=False;b=FakeBackend()
        with self.assertRaises(GateError):trial.finish_capture(c,trial.plan(c),b,lambda:trial.authority(c,a,p,trial.timestamp(a['not_before'])))
        self.assertEqual(b.events,['stop'])

    def test_near_expiry_rejected_before_effect(self):
        c=configuration();a,p=authority(c);b=FakeBackend()
        with self.assertRaises(GateError):trial.finish_capture(c,trial.plan(c),b,lambda:trial.authority(c,a,p,trial.timestamp(a['not_after'])-10))
        self.assertEqual(b.events,['stop'])

    def test_capture_failure_always_stops_owned_units(self):
        c=configuration();a,p=authority(c);b=FakeBackend('capture')
        with self.assertRaises(GateError):trial.finish_capture(c,trial.plan(c),b,lambda:trial.authority(c,a,p,trial.timestamp(a['not_before'])))
        self.assertEqual(b.events[-1],'stop')

    def test_failed_exclusive_capture_claim_still_closes_owned_units(self):
        c=configuration();b=FakeBackend()
        def claimed(): raise FileExistsError('retained capture claim')
        with self.assertRaises(FileExistsError):trial.finish_capture(c,trial.plan(c),b,claimed)
        self.assertEqual(b.events,['stop'])

    def test_success_stops_but_does_not_claim_restore(self):
        c=configuration();a,p=authority(c);b=FakeBackend()
        result=trial.finish_capture(c,trial.plan(c),b,lambda:trial.authority(c,a,p,trial.timestamp(a['not_before'])))
        self.assertFalse(result['restore_verified']);self.assertEqual(b.events[-1],'stop')

    def test_native_cleanup_continues_after_first_stop_error(self):
        c=configuration();p=trial.plan(c)
        with tempfile.TemporaryDirectory() as temp:
            p['root']=temp;b=trial.NativeTrial();b.config=c;b.claimed=p
            Path(temp,'trial-owned.json').write_bytes(encoded(b.binding(p)))
            seen=[]
            class Runner:
                def run(self,argv,**kwargs):
                    if argv[1]=='stop':
                        seen.append(argv[-1])
                        if len(seen)==1:raise GateError('STOP_FAILED')
                    return ('LoadState=loaded\nDescription=ATS online trial '+trial.config_run(p)+'\n').encode()
            b.command=Runner();b.show=lambda _:dict(MainPID='0',ControlPID='0',ActiveState='inactive')
            with patch.object(trial,'checked_path',lambda value,**kw:Path(value)):
                with self.assertRaises(GateError):b.stop_owned(p,'CAPTURE_FAILED')
            self.assertEqual(seen,list(p['units'][name] for name in ('helper','app','db')))
            receipts=list(Path(temp).glob('trial-cleanup-*.json'))
            self.assertEqual(len(receipts),1)
            self.assertFalse(Path(temp,'trial-stopped.json').exists())

    def test_online_setting_exception_exact_owner_only(self):
        import backup_runtime as runtime
        row=dict(uid=999,gid=988,mode=0o700,device=4,inode=5)
        c=dict(capture_mode='ats-online-v1',maintenance_mode='live-resume',settings=['/var/lib/atstudio/tmp'],
               online=dict(app_uid=999,app_gid=988,source_ownership={'/var/lib/atstudio/tmp':row}))
        info=SimpleNamespace(st_uid=999,st_gid=988,st_mode=stat.S_IFDIR|0o700,st_dev=4,st_ino=5)
        with patch.object(runtime,'checked_path',lambda p,**kw:Path(p)),patch.object(Path,'lstat',return_value=info):
            runtime.checked_setting_source(c,'/var/lib/atstudio/tmp')
            for field,value in [('uid',1000),('mode',0o755),('inode',6)]:
                changed=copy.deepcopy(c);changed['online']['source_ownership']['/var/lib/atstudio/tmp'][field]=value
                with self.assertRaises(GateError):runtime.checked_setting_source(changed,'/var/lib/atstudio/tmp')

    def test_setting_exception_does_not_relax_other_paths(self):
        import backup_runtime as runtime
        c=dict(capture_mode='ats-online-v1',maintenance_mode='live-resume',settings=['/var/lib/atstudio/tmp'],
               online=dict(app_uid=999,app_gid=988,source_ownership={'/var/lib/atstudio/tmp':{}}))
        with patch.object(runtime,'checked_path',side_effect=GateError('ROOT_TRUST_REQUIRED')):
            with self.assertRaisesRegex(GateError,'ROOT_TRUST_REQUIRED'):runtime.checked_setting_source(c,'/control')


if __name__=='__main__':unittest.main()

