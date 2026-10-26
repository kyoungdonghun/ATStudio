"""Synthetic-only planner/security/lifecycle checks; no DB/app/network startup."""
import copy
import contextlib
import io
import json
import os
from pathlib import Path
import struct
import subprocess
import time
import unittest
from unittest.mock import patch
import fixture_core as c
import fixture_cli as cli
import fixture_native as native

HERE=Path(__file__).resolve().parent


class FixtureTests(unittest.TestCase):
    def setUp(self):
        self.private=json.loads((HERE/'synthetic-inputs.json').read_bytes())
        self.inspection=json.loads((HERE/'synthetic-inspection.json').read_bytes())
        self.inspection['observed_at']=int(time.time())
        self.proposal=c.plan(self.inspection,self.private,7,c.day_now(),'a'*32)

    def approval(self,mode='apply'):
        return dict(wi=c.WI,scope=c.SCOPE,allow_native=True,mode=mode,issued_at=int(time.time())-1,
            expires_at=int(time.time())+60,candidate_sha256='5'*64,human_evidence_sha256='6'*64,
            plan_sha256=c.digest(self.proposal),verified_test_fixture_explicitly_approved=True,no_consent_or_mail_event=True)

    def test_plan_has_exact_effects_and_one_day_cancelled_grant(self):
        self.assertFalse(self.proposal['allow_native'])
        self.assertEqual(self.proposal['effects'],{'users_insert':2,'new_admin_role_update':1,'admin_audit_insert':1,'user_subscriptions_insert':1})
        self.assertEqual(self.proposal['entitlement'],dict(billing_cycle='MONTHLY',status='CANCELLED',started_at=c.day_now(),expires_at=c.day_now(),pending_subscription_id=None,pending_billing_cycle=None))
        self.assertNotIn(self.private['admin']['password'],json.dumps(self.proposal))

    def test_no_auto_plan_creation_and_invalid_plan_holds(self):
        for field,value in (('download_per_day',0),('download_per_day',-1),('user_type','BUSINESS'),('is_active',0)):
            observed=copy.deepcopy(self.inspection);observed['plans'][0][field]=value
            with self.assertRaises(ValueError):c.plan(observed,self.private,7,c.day_now(),'a'*32)
        with self.assertRaises(ValueError):c.plan(self.inspection,self.private,99,c.day_now(),'a'*32)

    def test_existing_admin_work_and_id1_change_hold(self):
        for field,value in (('admin_count',1),('existing_work',1),('collision_count',1),('id1_eligible_unentitled',False)):
            observed=dict(self.inspection,**{field:value})
            with self.assertRaises(ValueError):c.plan(observed,self.private,7,c.day_now(),'a'*32)

    def test_collision_and_password_72_byte_bound(self):
        modified=copy.deepcopy(self.private);modified['buyer']['email']=modified['admin']['email'].upper()
        with self.assertRaises(ValueError):c.inputs(modified)
        for password in ('x'*73,'é'*36,'short'):
            modified=copy.deepcopy(self.private);modified['buyer']['password']=password
            with self.assertRaises(ValueError):c.inputs(modified)
        modified['buyer']['password']='x'*72
        c.inputs(modified)

    def test_quote_injection_is_hex_data_and_columns_not_user_controlled(self):
        value="x'); DELETE FROM users; --"
        statement=c.sql('SELECT ? COLLATE utf8mb4_unicode_ci',value)
        self.assertNotIn(value,statement)
        self.assertIn(value.encode().hex(),statement)
        self.assertNotIn(';',statement)
        with self.assertRaises(ValueError):c.identifier('users;DELETE')

    def test_external_approval_binds_candidate_plan_human_and_semantics(self):
        approval=self.approval()
        c.authorize(approval,self.proposal,'5'*64,'6'*64,'apply')
        for key,value in (('allow_native',False),('candidate_sha256','7'*64),('plan_sha256','7'*64),
                ('human_evidence_sha256','7'*64),('expires_at',0),('verified_test_fixture_explicitly_approved',False),('no_consent_or_mail_event',False)):
            with self.assertRaises(ValueError):c.authorize(dict(approval,**{key:value}),self.proposal,'5'*64,'6'*64,'apply')

    def test_new_day_and_stale_inspection_require_new_approval(self):
        for change in ({'day':'2000-01-01'},{'inspected_at':0},{'nonce':'../other'}):
            proposal=dict(self.proposal,**change)
            approval=self.approval();approval['plan_sha256']=c.digest(proposal)
            with self.assertRaises(ValueError):c.authorize(approval,proposal,'5'*64,'6'*64,'apply')

    def test_default_cli_and_unapproved_native_do_not_start(self):
        with contextlib.redirect_stdout(io.StringIO()) as output:
            self.assertEqual(cli.main([]),0)
        self.assertEqual(json.loads(output.getvalue())['native_authorized'],False)
        with patch.object(native,'runtime',side_effect=AssertionError('MUST_NOT_LOAD')),contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(cli.main(['apply']),2)

    def test_write_flags_change_exactly_two_and_keep_scheduler_network(self):
        original=['--skip-networking','--event-scheduler=DISABLED','--skip-slave-start','--persisted-globals-load=OFF','--read-only=ON','--super-read-only=ON']
        helper={'mysql_unit_arguments':lambda *_:list(original)}
        for mode in ('inspect','reconcile'):self.assertEqual(native.mysql_arguments(helper,'unit',mode),original)
        actual=native.mysql_arguments(helper,'unit','apply')
        self.assertEqual([(a,b) for a,b in zip(original,actual) if a!=b],[('--read-only=ON','--read-only=OFF'),('--super-read-only=ON','--super-read-only=OFF')])

    def test_each_cleanup_failure_does_not_skip_later_cleanup(self):
        for failure in ('STOP','PID_ZERO','SOCKET_GONE','BASELINE'):
            calls=[]
            def step(name):
                calls.append(name)
                if name==failure:raise RuntimeError('PRIVATE')
            names=('STOP','PID_ZERO','SOCKET_GONE','BASELINE')
            result=native.cleanup([(name,lambda name=name:step(name)) for name in names])
            self.assertEqual(calls,list(names))
            self.assertEqual(result[failure],'FAILED')
            self.assertNotIn('PRIVATE',json.dumps(result))

    def test_reused_files_are_exact(self):
        for old,name in (('WI20261023-002-account-preflight','account_operator.py'),('WI20261023-002-account-preflight','inventory.py'),
                ('WI067-ledger-diagnostic-v2','source_operator.py'),('WI067-ledger-diagnostic-v2','diagnostic.py'),
                ('WI069-backup','cold_capture.py'),('WI067-auth-trial-v8r1','schema.json')):
            self.assertEqual((HERE/'reused'/name).read_bytes(),(HERE.parent/old/name).read_bytes())

    def test_retained_crypto_accepts72_and_rejects73_without_secret_publication(self):
        cp=os.pathsep.join(str(HERE/'crypto'/name) for name in ('','spring-security-crypto-7.0.7.jar','commons-logging-1.3.6.jar'))
        java='C:/Program Files/Java/jdk-17/bin/java.exe'
        for size,expected in ((72,0),(73,2)):
            raw=b''.join(struct.pack('>I',len(value))+value for value in (b'A'*size,b'Synthetic_ONLY_123!'))
            result=subprocess.run([java,'-cp',cp,'FixturePasswords'],input=raw,capture_output=True,timeout=10)
            self.assertEqual(result.returncode,expected)
            self.assertNotIn(b'A'*size,result.stdout+result.stderr)
            if expected==0:self.assertEqual(len(result.stdout.splitlines()),2)
            else:self.assertEqual(result.stdout,b'')


if __name__=='__main__':unittest.main(verbosity=2)
