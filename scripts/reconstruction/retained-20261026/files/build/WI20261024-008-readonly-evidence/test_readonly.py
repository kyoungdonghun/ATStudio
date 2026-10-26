"""Synthetic delta tests; no host, DB, process, network or private input use."""
import ast
import base64
import copy
import hashlib
import json
from pathlib import Path
from types import SimpleNamespace
import time
import unittest
from unittest.mock import patch
import account_operator as op
import backup_evidence as backup
import inventory as inv
import make_payload as builder

HERE=Path(__file__).resolve().parent
OLD=HERE.parent/'WI20261023-008-readonly-recheck'


def rows():
    values=[dict(kind='counts',users=3,selected_users=3,plans=1,subscription_rows=1,grants=1,admin_audits=1,pending_audio=0,unfinished_storage=0)]
    values += [dict(kind='account',id=i,role='ADMIN' if i==2 else 'USER',deleted=0,verified=1,
        total_subscriptions=int(i==3),effective_subscriptions=int(i==3),eligible_subscriptions=int(i==3)) for i in (1,2,3)]
    values += [dict(kind='plan',id=1,user_type='INDIVIDUAL',is_active=1,download_per_day=5),
        dict(kind='grant',id=1,user_id=3,subscription_id=1,status='CANCELLED',billing_cycle='MONTHLY',
        started_at='2026-10-24',expires_at='2026-10-24',pending_subscription_id=None,pending_billing_cycle=None)]
    return values


def encoded(values):return b'\n'.join(json.dumps(v).encode() for v in values)+b'\n'


def metadata():
    counts={table:0 for table in backup.TABLES.values()}
    counts.update({'synthetic_'+str(i):0 for i in range(39)})
    counts['subscriptions']=6
    proof=dict(counts=counts,schema_sha256='a'*64,dump_sha256='b'*64)
    return dict(manifest=dict(version=1,scope='pre-feature-WI060-runtime-no-runtime-keys',members={'/srv/atstudio/public':{'type':'dir'}},
            units_sha256='c'*64,inventory_sha256='d'*64,full_disaster_recovery=False),sql_proof=proof,
        verified_content=dict(repository_id='e'*64,snapshot_id='f'*64,original_run_id=backup.ORIGINAL,original_sql=proof,
            recovered_sql=copy.deepcopy(proof),full_disaster_recovery=False,equivalence=dict(
            proof='MYSQL_8046_REDUNDANT_COLUMN_CHARSET_INSERTIONS',edits=209,identities=[['synthetic','column']]*209,
            raw_hashes_equal=False,all_other_bytes_equal=True,actual_column_metadata_equal=True)),
        restore_result=dict(status='BOUNDED_RESTORE_ONLY_PASS',operation='wi069-exact-snapshot-restore-only',run_id=backup.RESTORE,
            original_run_id=backup.ORIGINAL,repository_id='e'*64,snapshot_id='f'*64,stage='COMPLETE',detail=None,
            cleanup=True,cleanup_errors=[],source_release='WI060-existing-runtime',app_start='NOT_RUN',
            backup='NOT_RUN_EXISTING_SNAPSHOT',full_disaster_recovery=False))


class ReadonlyTests(unittest.TestCase):
    def test_actual_grant_and_actor_projection(self):
        result=inv.parse(encoded(rows()),{})
        self.assertTrue(result['expected_actor_roles_match'])
        self.assertEqual(result['grants'][0]['id'],1)
        self.assertIsNone(result['grants'][0]['pending_subscription_id'])
        self.assertEqual(result['plans'][0]['download_per_day'],5)
        self.assertEqual(result['accounts'][0]['total_subscription_count'],0)
        self.assertEqual(result['password_state'],'NOT_QUERIED')

    def test_grant_bounds_enums_and_nullable_fields(self):
        for delta in ({'id':True},{'user_id':4},{'subscription_id':2},{'status':'PRIVATE'},
                {'billing_cycle':'OTHER'},{'expires_at':'2026-02-30'},{'pending_subscription_id':False},
                {'pending_billing_cycle':'OTHER'},{'email':'PRIVATE'}):
            values=rows();values[-1].update(delta)
            with self.assertRaises(ValueError):inv.parse(encoded(values),{})
        for index,delta in ((0,{'grants':0}),(0,{'selected_users':True}),(1,{'total_subscriptions':1}),(1,{'id':2})):
            values=rows();values[index].update(delta)
            with self.assertRaises(ValueError):inv.parse(encoded(values),{})
        values=rows();values[-1].update(status='EXPIRED',pending_subscription_id=1,pending_billing_cycle='YEARLY')
        self.assertEqual(inv.parse(encoded(values),{})['grants'][0]['status'],'EXPIRED')

    def test_sql_select_only_and_date_rejection(self):
        sql=inv.queries('2026-10-24').decode()
        self.assertEqual([s.split()[0] for s in sql.split(';') if s.strip()],['SET','START','SELECT','SELECT','SELECT','SELECT','ROLLBACK'])
        for secret in ('password','email','nickname','refresh_token','SHA2','OUTFILE','FOR UPDATE','CALL '):self.assertNotIn(secret,sql)
        self.assertIn('WITH CONSISTENT SNAPSHOT, READ ONLY',sql)
        self.assertIn('WHERE u.id IN (1,2,3)',sql)
        for day in (None,"2026-10-24';DELETE",'2026-02-30'):
            with self.assertRaises(ValueError):inv.queries(day)

    def test_historical_pass_never_establishes_current_coverage(self):
        values=metadata();current=inv.parse(encoded(rows()),{})['counts']
        result=backup.project(values,current)
        self.assertEqual(result['historical_total_rows'],6)
        self.assertEqual(result['coverage_reason'],'HISTORICAL_COUNTS_DIFFER')
        self.assertEqual(result['current_fixture_coverage'],'NOT_ESTABLISHED')
        current.update({field:values['sql_proof']['counts'][table] for field,table in backup.TABLES.items()})
        self.assertEqual(backup.project(values,current)['coverage_reason'],'MATCHING_COUNTS_ARE_NOT_CONTENT_PROOF')
        public=json.dumps(result)
        for private in ('repository_id','snapshot_id','schema_sha256','dump_sha256','identities','/var/','e'*64):self.assertNotIn(private,public)

    def test_fixed_reads_pins_missing_and_invalid_records(self):
        values=metadata();data={backup.FILES[k]:json.dumps(v).encode() for k,v in values.items()}
        pins={k:hashlib.sha256(data[backup.FILES[k]]).hexdigest() for k in backup.PINS}
        calls=[]
        def read(path,limit,private):
            calls.append(path);self.assertEqual(limit,262144);self.assertIs(private,True)
            return data[path]
        with patch.object(backup,'PINS',pins):
            result=backup.observe(SimpleNamespace(read=read),inv.parse(encoded(rows()),{})['counts'])
            self.assertEqual(calls,list(backup.FILES.values()))
            self.assertEqual(result['status'],'HISTORICAL_METADATA_VALIDATED')
            data[backup.FILES['restore_result']]=b'{"private":"DO_NOT_OUTPUT"}'
            self.assertEqual(backup.observe(SimpleNamespace(read=read),rows()[0])['status'],'HOLD_METADATA')
            del data[backup.FILES['sql_proof']]
            result=backup.observe(SimpleNamespace(read=read),rows()[0])
            self.assertEqual(result['files']['sql_proof'],'UNAVAILABLE_OR_INVALID')
            self.assertNotIn('DO_NOT_OUTPUT',json.dumps(result))
        data[backup.FILES['sql_proof']]=b'{}'
        self.assertEqual(backup.observe(SimpleNamespace(read=read),rows()[0])['status'],'HOLD_METADATA')

    def test_restore_binding_cleanup_and_original_content_required(self):
        for key,value in (('cleanup',False),('cleanup_errors',['PRIVATE']),('snapshot_id','1'*64),('stage','INCOMPLETE')):
            values=metadata();values['restore_result'][key]=value
            with self.assertRaises(ValueError):backup.project(values,rows()[0])
        values=metadata();values['verified_content']['recovered_sql']['counts']['users']=3
        with self.assertRaises(ValueError):backup.project(values,rows()[0])

    def test_original_candidate_and_lifecycle_exact_delta(self):
        manifest=json.loads((OLD/'candidate.json').read_text())
        for name,pin in manifest['files'].items():self.assertEqual(hashlib.sha256((OLD/name).read_bytes()).hexdigest(),pin)
        expected=(OLD/'account_operator.py').read_text().replace('import inventory\n','import inventory\nimport backup_evidence\n').replace(
            'atstudio-wi20261023-008-account-plan-source.service',op.NAME).replace('REQ007_20261023_READONLY_ACCOUNT_PLAN_RECHECK',op.SCOPE).replace(
            "        result['inventory']['application_day_asia_seoul']=day\n",
            "        result['inventory']['application_day_asia_seoul']=day\n        result['backup_evidence']=backup_evidence.observe(h,result['inventory']['counts'])\n")
        self.assertEqual(expected,(HERE/'account_operator.py').read_text())

    def test_payload_six_exact_sources_fresh_scope_and_expiry(self):
        expiry=int(time.time())+600
        raw=builder.payload('probe',expires_at=expiry)
        sources=ast.literal_eval(ast.parse(raw).body[0].value)
        self.assertEqual(len(sources),6)
        for name in ('inventory','backup_evidence','account_operator'):
            self.assertEqual(base64.b64decode(sources[name]['body']),(HERE/(name+'.py')).read_bytes())
        for name,pin in (('source_operator',op.SOURCE_OPERATOR),('cold_capture',op.COLD),('diagnostic',op.CORE)):
            self.assertEqual(hashlib.sha256(base64.b64decode(sources[name]['body'])).hexdigest(),pin)
        self.assertIn(op.SCOPE.encode(),raw)
        self.assertIn(b"MODE='probe'",raw)
        boot='11111111-1111-1111-1111-111111111111'
        compile(builder.payload('inventory',boot,'a'*64,expiry),'synthetic','exec')
        for expiry in (0,True,int(time.time())+901):
            with self.assertRaises(ValueError):builder.payload('probe',expires_at=expiry)
        with self.assertRaises(ValueError):builder.payload('inventory',expires_at=int(time.time())+600)
        with self.assertRaises(ValueError):op.authority(dict(scope='REQ007_20261023_READONLY_ACCOUNT_PLAN_RECHECK',
            boot_id=boot,machine_id_sha256='a'*64,expires_at=int(time.time())+600))


if __name__=='__main__':unittest.main(verbosity=2)
