import copy,importlib.util,json,sys,time,unittest
from pathlib import Path
from unittest.mock import patch
HERE=Path(__file__).resolve().parent
sys.path.insert(0,str(HERE.parent/'WI20261023-005-account-fixture'))
import fixture_core as core
import transaction as tx

def snap():
    s={k:{} for k in json.loads((HERE.parent/'WI20261023-005-account-fixture/reused/schema.json').read_bytes())}
    s['users']={f'[{i}]':{k:core.cell(v) for k,v in dict(id=i,role=r,is_deleted=0,is_verified=1,password='private-stable',refresh_token='preserve',updated_at='old').items()} for i,r in ((1,'USER'),(2,'ADMIN'),(3,'USER'))}
    s['user_subscriptions']['[1]']={k:core.cell(v) for k,v in dict(id=1,user_id=3,subscription_id=1,billing_cycle='MONTHLY',status='CANCELLED',started_at=tx.START,expires_at=tx.START,pending_subscription_id=None,pending_billing_cycle=None,updated_at='old').items()}
    plans=((1,'INDIVIDUAL',9900,99000,5,1,3),(2,'INDIVIDUAL',19900,199000,20,2,10),(3,'INDIVIDUAL',29900,299000,-1,2,10),(4,'BUSINESS',19900,199000,10,1,3),(5,'BUSINESS',49900,499000,50,2,10),(6,'BUSINESS',99900,999000,-1,2,10))
    for i,typ,m,y,d,c,p in plans:s['subscriptions'][f'[{i}]']={k:core.cell(v) for k,v in dict(id=i,user_type=typ,is_active=1,price_monthly=f'{m}.00',price_yearly=f'{y}.00',download_per_day=d,max_whitelist_channels=c,max_playlists=p).items()}
    s['payment_orders']['[12]']={'id':core.cell(12),'status':core.cell('EXISTING')}
    return s

def proposal(mode='apply'):
    now=int(time.time())
    value=dict(wi=tx.WI,scope=tx.SCOPE,decision_reference='USER_APPROVED_STAGE_TWO_20261024',mode=mode,nonce='a'*32,inspection_nonce='a'*32 if mode=='inspect' else 'b'*32,boot_id='a'*8+'-'+('a'*4+'-')*3+'a'*12,machine_id_sha256='d'*64,issued_at=now-1,expires_at=now+600,candidate_sha256='c'*64,allow_writes=mode=='apply',end_date=tx.END,recovery_evidence_reference='APPROVED_BOUNDED_BEFORE_RECORD')
    if mode=='reconcile':value['reconcile_attempt']='e'*32
    return value

class DB:
    def __init__(self,fail=None):self.sql=[];self.ids=20;self.fail=fail
    def query(self,s):
        self.sql.append(s)
        if self.fail=='commit' and s=='COMMIT':raise RuntimeError('lost reply')
        if 'UTC_TIMESTAMP' in s:return [{'stamp':'2026-10-24 01:02:03'}]
        if "'user_role'" in s:return [dict(user_role='USER',user_updated_at='old',grant_expires_at=tx.START,grant_updated_at='old')]
        if s.startswith('UPDATE'):return [{'affected':0 if self.fail=='update' else 1}]
        if s.startswith('INSERT'):self.ids+=1;return [{'affected':1,'id':self.ids}]
        return []

class Checks(unittest.TestCase):
    def test_authority_modes(self):
        for m in ('inspect','apply','reconcile'):tx.validate(proposal(m),m,'c'*64)
    def test_wrong_authority(self):
        for k,v in [('end_date','2026-11-25'),('allow_writes',False),('candidate_sha256','e'*64),('expires_at',0),('decision_reference','other'),('inspection_nonce','a'*32)]:
            with self.subTest(k=k):
                p=proposal();p[k]=v
                with self.assertRaises(ValueError):tx.validate(p,'apply','c'*64)
    def test_fresh_source_state(self):tx.eligible(snap())
    def test_reject_drift(self):
        for t,key,col,val in [('users','[2]','role','USER'),('users','[3]','is_deleted',1),('user_subscriptions','[1]','expires_at',tx.END),('user_subscriptions','[1]','status','ACTIVE'),('subscriptions','[6]','price_yearly','1.00')]:
            with self.subTest(t=t,col=col):
                s=snap();s[t][key][col]=core.cell(val)
                with self.assertRaises(ValueError):tx.eligible(s)
    def test_exact_expected_preserves_secrets_sessions_existing_rows(self):
        before=snap();after=tx.expected(before,[21,22],'a'*32,'2026-10-24 01:02:03')
        self.assertEqual(before['users']['[2]'],after['users']['[2]'])
        self.assertEqual(before['users']['[3]'],after['users']['[3]'])
        for col in ('password','refresh_token'):self.assertEqual(before['users']['[1]'][col],after['users']['[1]'][col])
        for table in before:
            if table not in ('users','user_subscriptions','admin_operation_audit_logs'):self.assertEqual(before[table],after[table])
        self.assertEqual(tx.audit_rows([21,22],'a'*32,'stamp')[0]['actor_user_id'],None)
    def test_duplicate_audit_ids(self):
        with self.assertRaises(ValueError):tx.expected(snap(),[21,21],'a'*32,'stamp')
    def run_tx(self,fail=None,drift=False,extra=False):
        before=snap();p=proposal();after=tx.expected(before,[21,22],p['nonce'],'2026-10-24 01:02:03')
        if extra:after['payment_orders']['[12]']['status']=core.cell('CHANGED')
        db=DB(fail);records={};inspection=dict(schema_sha256='s',server_uuid_sha256='v',snapshot_sha256=core.digest(before) if not drift else 'wrong')
        with patch.object(core,'snapshot',side_effect=[before,after]):
            result=tx.transact(db,p,{},lambda _:dict(schema_sha256='s',server_uuid_sha256='v',existing_work=0),lambda n,v:records.__setitem__(n,v),lambda:None,inspection)
        return result,db.sql,records
    def test_success_one_commit(self):
        r,sql,rec=self.run_tx();self.assertEqual(r['status'],'APPLIED');self.assertEqual(sql.count('COMMIT'),1);self.assertIn('commit-intent.json',rec)
    def test_drift_no_dml(self):
        r,sql,_=self.run_tx(drift=True);self.assertEqual(r['status'],'HOLD');self.assertFalse(any(s.startswith('UPDATE') for s in sql))
    def test_update_mismatch_rollback(self):
        r,sql,_=self.run_tx(fail='update');self.assertEqual(r['status'],'HOLD');self.assertIn('ROLLBACK',sql);self.assertNotIn('COMMIT',sql)
    def test_extra_cell_change_rollback(self):
        r,sql,_=self.run_tx(extra=True);self.assertEqual(r['status'],'HOLD');self.assertNotIn('COMMIT',sql)
    def test_commit_timeout_never_retries(self):
        r,sql,rec=self.run_tx(fail='commit');self.assertEqual(r['status'],'HOLD_RECONCILE');self.assertEqual(sql.count('COMMIT'),1);self.assertIn('expected-after.json',rec)

if __name__=='__main__':unittest.main(verbosity=2)

