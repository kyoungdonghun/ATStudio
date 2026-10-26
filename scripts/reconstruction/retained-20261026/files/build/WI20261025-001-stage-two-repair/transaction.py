"""Bounded existing-actor changes. No startup, credentials or network on import."""
import copy
import re
import time
import fixture_core as core

WI = 'WI-20261024-ATS-023'
SCOPE = 'REQ20261024_ATS001_EXISTING_ACTORS_AND_GRANT'
END = '2026-11-24'
START = '2026-10-24'


def validate(p, mode, candidate, now=None):
    now = int(time.time()) if now is None else now
    core.need(set(p) == {'wi','scope','decision_reference','mode','nonce','inspection_nonce',
        'boot_id','machine_id_sha256','issued_at','expires_at','candidate_sha256','allow_writes',
        'end_date','recovery_evidence_reference'} | ({'reconcile_attempt'} if mode=='reconcile' else set()), 'AUTHORITY_SHAPE')
    core.need(p['wi']==WI and p['scope']==SCOPE and p['decision_reference']=='USER_APPROVED_STAGE_TWO_20261024', 'AUTHORITY_SCOPE')
    core.need(mode in ('inspect','apply','reconcile') and p['mode']==mode and p['allow_writes'] is (mode=='apply'), 'MODE')
    core.need(core.pin(candidate) and p['candidate_sha256']==candidate and p['end_date']==END, 'CANDIDATE_DATE')
    core.need(type(p['issued_at']) is int and type(p['expires_at']) is int and p['issued_at']<=now<p['expires_at']<=p['issued_at']+900, 'TIME')
    core.need(mode=='reconcile' or core.day_now()<=END, 'EXPIRED_END_DATE')
    core.need(type(p['boot_id']) is str and re.fullmatch(r'[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}',p['boot_id']) and core.pin(p['machine_id_sha256']), 'TARGET')
    core.need(all(type(p[k]) is str and re.fullmatch('[a-f0-9]{32}',p[k]) for k in ('nonce','inspection_nonce')), 'NONCE')
    core.need((p['nonce']==p['inspection_nonce']) is (mode=='inspect'), 'INSPECTION_BINDING')
    if mode=='reconcile':
        core.need(type(p['reconcile_attempt']) is str and re.fullmatch('[a-f0-9]{32}',p['reconcile_attempt'])
            and p['reconcile_attempt'] not in (p['nonce'],p['inspection_nonce']), 'RECONCILE_ATTEMPT')
    core.need(type(p['recovery_evidence_reference']) is str and re.fullmatch('[A-Za-z0-9_./:-]{1,200}',p['recovery_evidence_reference']), 'RECOVERY_REFERENCE')


def eligible(s):
    def match(table,key,fields):
        row=s[table].get(key)
        core.need(row is not None and all(row.get(k)==core.cell(v) for k,v in fields.items()),'TARGET_STATE')
    for i,role in ((1,'USER'),(2,'ADMIN'),(3,'USER')):
        match('users','['+str(i)+']',dict(id=i,role=role,is_deleted=0,is_verified=1))
    match('user_subscriptions','[1]',dict(id=1,user_id=3,subscription_id=1,billing_cycle='MONTHLY',status='CANCELLED',started_at=START,expires_at=START,pending_subscription_id=None,pending_billing_cycle=None))
    core.need(set(s['subscriptions'])=={'['+str(i)+']' for i in range(1,7)},'PLAN_COUNT')
    for i,typ,monthly,yearly,daily,channels,playlists in (
        (1,'INDIVIDUAL',9900,99000,5,1,3),(2,'INDIVIDUAL',19900,199000,20,2,10),
        (3,'INDIVIDUAL',29900,299000,-1,2,10),(4,'BUSINESS',19900,199000,10,1,3),
        (5,'BUSINESS',49900,499000,50,2,10),(6,'BUSINESS',99900,999000,-1,2,10)):
        match('subscriptions','['+str(i)+']',dict(id=i,user_type=typ,is_active=1,price_monthly=str(monthly)+'.00',price_yearly=str(yearly)+'.00',download_per_day=daily,max_whitelist_channels=channels,max_playlists=playlists))
    core.need(not any(row.get('user_id')==core.cell(3) and key!='[1]' for key,row in s['user_subscriptions'].items()),'EXTRA_BUYER_GRANT')


def audit_rows(ids,nonce,stamp):
    common=dict(actor_user_id=None,outcome='SUCCEEDED',reason_note='REQ-20261024-ATS-001:OFFLINE_OPERATOR:'+nonce,created_at=stamp,updated_at=stamp)
    return [dict(common,id=ids[0],action='USER_ROLE_CHANGE',target_type='USER',target_id=1,
            before_state='{"role":"USER","isDeleted":false}',after_state='{"role":"ADMIN","isDeleted":false}',reason_code='ROLE_CHANGED'),
        dict(common,id=ids[1],action='USER_SUBSCRIPTION_CORRECTION',target_type='USER_SUBSCRIPTION',target_id=1,
            before_state='{"expiresAt":"2026-10-24"}',after_state='{"expiresAt":"2026-11-24"}',reason_code='APPROVED_TEST_GRANT_EXTENSION')]


def expected(before,ids,nonce,stamp):
    eligible(before)
    core.need(len(ids)==2 and all(type(v) is int and v>0 for v in ids) and len(set(ids))==2,'AUDIT_IDS')
    after=copy.deepcopy(before)
    after['users']['[1]'].update(role=core.cell('ADMIN'),updated_at=core.cell(stamp))
    after['user_subscriptions']['[1]'].update(expires_at=core.cell(END),updated_at=core.cell(stamp))
    for row in audit_rows(ids,nonce,stamp):
        key='['+str(row['id'])+']'
        core.need(key not in after['admin_operation_audit_logs'],'AUDIT_COLLISION')
        after['admin_operation_audit_logs'][key]={k:core.cell(v) for k,v in row.items()}
    return after


def transact(db,p,schema,inspect,record,guard,inspection):
    result=dict(status='HOLD',phase='BEGIN',commit='NOT_SENT')
    possible=False
    try:
        guard()
        db.query('SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE')
        db.query('SET SESSION innodb_lock_wait_timeout=3')
        db.query('START TRANSACTION')
        observed=inspect(db)
        core.need(observed['schema_sha256']==inspection['schema_sha256'] and observed['server_uuid_sha256']==inspection['server_uuid_sha256'] and observed['existing_work']==0,'FRESH_DATABASE')
        before=core.snapshot(db,schema)
        core.need(core.digest(before)==inspection['snapshot_sha256'],'SNAPSHOT_DRIFT')
        eligible(before)
        record('before.json',before)
        recovery=db.query("SELECT JSON_OBJECT('user_role',u.role,'user_updated_at',CAST(u.updated_at AS CHAR),'grant_expires_at',CAST(g.expires_at AS CHAR),'grant_updated_at',CAST(g.updated_at AS CHAR)) FROM users u JOIN user_subscriptions g ON g.id=1 WHERE u.id=1")
        core.need(len(recovery)==1 and set(recovery[0])=={'user_role','user_updated_at','grant_expires_at','grant_updated_at'},'RECOVERY_SHAPE')
        for key,table,column in (('user_role','users','role'),('user_updated_at','users','updated_at'),('grant_expires_at','user_subscriptions','expires_at'),('grant_updated_at','user_subscriptions','updated_at')):
            core.need(core.cell(recovery[0][key])==before[table]['[1]'][column],'RECOVERY_HASH')
        record('recovery-before.json',recovery[0])
        stamp=db.query("SELECT JSON_OBJECT('stamp',DATE_FORMAT(UTC_TIMESTAMP(),'%Y-%m-%d %H:%i:%s'))")[0]['stamp']
        core.need(type(stamp) is str and re.fullmatch(r'\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}',stamp),'STAMP')
        guard()
        result['phase']='WRITE'
        for sql in (core.sql("UPDATE users SET role='ADMIN',updated_at=? WHERE id=1 AND role='USER' AND is_deleted=0 AND is_verified=1",stamp),
                    core.sql("UPDATE user_subscriptions SET expires_at=?,updated_at=? WHERE id=1 AND user_id=3 AND subscription_id=1 AND expires_at=? AND started_at=? AND status='CANCELLED' AND billing_cycle='MONTHLY' AND pending_subscription_id IS NULL AND pending_billing_cycle IS NULL",END,stamp,START,START)):
            core.need(db.query(sql+"; SELECT JSON_OBJECT('affected',ROW_COUNT())")==[{'affected':1}],'UPDATE_COUNT')
        ids=[]
        for row in audit_rows([1,2],p['nonce'],stamp):
            row.pop('id')
            cols=list(row)
            sql='INSERT INTO admin_operation_audit_logs('+','.join(cols)+') VALUES('+','.join('?' for _ in cols)+')'
            r=db.query(core.sql(sql,*(row[k] for k in cols))+"; SELECT JSON_OBJECT('affected',ROW_COUNT(),'id',LAST_INSERT_ID())")
            core.need(len(r)==1 and r[0]['affected']==1 and type(r[0]['id']) is int and r[0]['id']>0,'AUDIT_INSERT')
            ids.append(r[0]['id'])
        after=core.snapshot(db,schema)
        core.need(after==expected(before,ids,p['nonce'],stamp),'EXACT_DELTA')
        guard()
        record('expected-after.json',after)
        record('commit-intent.json',dict(before_sha256=core.digest(before),after_sha256=core.digest(after),audit_ids=ids))
        possible=True
        result.update(phase='COMMIT',commit='POSSIBLE')
        db.query('COMMIT')
        result.update(status='APPLIED',phase='COMPLETE',commit='ACKNOWLEDGED')
        record('commit-ack.json',dict(status='APPLIED',audit_ids=ids))
    except BaseException:
        result['status']='HOLD_RECONCILE' if possible else 'HOLD'
        try:db.query('ROLLBACK');result['rollback']='ACKNOWLEDGED'
        except BaseException:result.update(status='HOLD_RECONCILE',rollback='UNCONFIRMED')
    return result
