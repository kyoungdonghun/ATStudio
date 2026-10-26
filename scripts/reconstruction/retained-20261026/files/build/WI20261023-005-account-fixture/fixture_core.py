"""Exact two-new-account TEST fixture planner and transaction; no native imports."""
from datetime import datetime,timedelta,timezone,date
import hashlib
import json
import re
import time

WI='WI-20261023-ATS-005'
SCOPE='CREATE_TWO_NEW_VERIFIED_TEST_FIXTURES'
BEFORE='{"role":"USER","isDeleted":false}'
AFTER='{"role":"ADMIN","isDeleted":false}'
ALLOWED={'users','admin_operation_audit_logs','user_subscriptions'}


def need(value,code='CONTRACT'):
    if not value:raise ValueError(code)


def canonical(value):return json.dumps(value,sort_keys=True,separators=(',',':'),ensure_ascii=True).encode()
def digest(value):return hashlib.sha256(canonical(value)).hexdigest()
def cell(value):return None if value is None else hashlib.sha256(str(value).encode()).hexdigest()
def day_now():return datetime.now(timezone(timedelta(hours=9))).date().isoformat()
def pin(value):return type(value) is str and re.fullmatch('[a-f0-9]{64}',value) is not None


def inputs(value):
    need(type(value) is dict and set(value)=={'admin','buyer'})
    for actor in value.values():
        need(type(actor) is dict and set(actor)=={'email','nickname','password'})
        need(type(actor['email']) is str and re.fullmatch(r'[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]{1,60}\.[A-Za-z]{2,12}',actor['email'])
            and len(actor['email'])<=100,'EMAIL')
        need(type(actor['nickname']) is str and re.fullmatch(r'ats_test_[A-Za-z0-9_]{1,11}',actor['nickname']),'TEST_NICKNAME')
        password=actor['password']
        need(type(password) is str and 12<=len(password.encode('utf-8'))<=72
            and all(33<=ord(c)<=126 for c in password),'PASSWORD_BOUND')
    need(value['admin']['email'].casefold()!=value['buyer']['email'].casefold()
        and value['admin']['nickname'].casefold()!=value['buyer']['nickname'].casefold(),'COLLISION')
    return value


def plan(inspection,private,plan_id,day,nonce):
    inputs(private)
    need(type(day) is str and re.fullmatch(r'\d{4}-\d{2}-\d{2}',day),'DAY')
    date.fromisoformat(day)
    need(type(nonce) is str and re.fullmatch('[a-f0-9]{32}',nonce),'NONCE')
    need(inspection['status']=='INSPECTED' and type(plan_id) is int and plan_id>0)
    for key in ('snapshot_sha256','schema_sha256','server_uuid_sha256','machine_id_sha256'):
        need(pin(inspection[key]))
    need(inspection['admin_count']==0 and inspection['existing_work']==0 and inspection['id1_eligible_unentitled'] is True,'EXISTING_STATE')
    candidates=[row for row in inspection['plans'] if row['id']==plan_id]
    need(len(candidates)==1,'PLAN_ABSENT')
    chosen=candidates[0]
    need(chosen['user_type']=='INDIVIDUAL' and chosen['is_active']==1 and type(chosen['download_per_day']) is int
        and 1<=chosen['download_per_day']<=1000000,'PLAN_INELIGIBLE')
    need(inspection['collision_count']==0,'COLLISION')
    return dict(wi=WI,allow_native=False,scope=SCOPE,nonce=nonce,day=day,plan=chosen,
        input_sha256=digest(private),snapshot_sha256=inspection['snapshot_sha256'],schema_sha256=inspection['schema_sha256'],
        server_uuid_sha256=inspection['server_uuid_sha256'],machine_id_sha256=inspection['machine_id_sha256'],
        boot_id=inspection['boot_id'],inspected_at=inspection['observed_at'],
        effects={'users_insert':2,'new_admin_role_update':1,'admin_audit_insert':1,'user_subscriptions_insert':1},
        verified_fixture_semantics='TEST_FLAG_ONLY_NO_EMAIL_EVENT_OR_CONSENT',
        entitlement={'billing_cycle':'MONTHLY','status':'CANCELLED','started_at':day,'expires_at':day,
            'pending_subscription_id':None,'pending_billing_cycle':None},protected_existing_user_id=1)


def authorize(approval,proposal,candidate_sha,human_sha,mode,now=None):
    now=int(time.time()) if now is None else now
    keys={'wi','allow_native','scope','issued_at','expires_at','candidate_sha256','human_evidence_sha256',
        'plan_sha256','mode','verified_test_fixture_explicitly_approved','no_consent_or_mail_event'}
    need(type(approval) is dict and set(approval)==keys,'APPROVAL_SHAPE')
    need(approval['wi']==WI and approval['allow_native'] is True and approval['scope']==SCOPE
        and approval['mode']==mode and mode in ('inspect','apply','reconcile'),'APPROVAL_SCOPE')
    need(type(approval['issued_at']) is int and type(approval['expires_at']) is int
        and approval['issued_at']<=now<approval['expires_at']<=approval['issued_at']+900,'APPROVAL_TIME')
    need(pin(candidate_sha) and pin(human_sha) and approval['candidate_sha256']==candidate_sha
        and approval['human_evidence_sha256']==human_sha and approval['plan_sha256']==digest(proposal),'APPROVAL_BINDING')
    need(approval['verified_test_fixture_explicitly_approved'] is True and approval['no_consent_or_mail_event'] is True,'FIXTURE_SEMANTICS')
    need(proposal['allow_native'] is False and proposal['wi']==WI and proposal['scope']==SCOPE,'PLAN_DEFAULT_DENY')
    need(type(proposal['nonce']) is str and re.fullmatch('[a-f0-9]{32}',proposal['nonce'])
        and type(proposal['boot_id']) is str and re.fullmatch(r'[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}',proposal['boot_id'])
        and pin(proposal['machine_id_sha256']) and pin(proposal['input_sha256']),'PLAN_IDENTITY')
    if mode in ('apply','reconcile'):
        need(proposal['protected_existing_user_id']==1 and proposal['effects']=={'users_insert':2,'new_admin_role_update':1,'admin_audit_insert':1,'user_subscriptions_insert':1}
            and proposal['verified_fixture_semantics']=='TEST_FLAG_ONLY_NO_EMAIL_EVENT_OR_CONSENT','PLAN_EFFECTS')
        need(all(pin(proposal[key]) for key in ('snapshot_sha256','schema_sha256','server_uuid_sha256')),'PLAN_PINS')
        chosen=proposal['plan']
        need(set(chosen)=={'id','user_type','is_active','download_per_day'} and type(chosen['id']) is int and chosen['id']>0
            and chosen['user_type']=='INDIVIDUAL' and chosen['is_active']==1 and type(chosen['download_per_day']) is int
            and 1<=chosen['download_per_day']<=1000000,'PLAN_INELIGIBLE')
        need(proposal['entitlement']=={'billing_cycle':'MONTHLY','status':'CANCELLED','started_at':proposal['day'],
            'expires_at':proposal['day'],'pending_subscription_id':None,'pending_billing_cycle':None},'PLAN_ENTITLEMENT')
    if mode=='apply':
        need(proposal['day']==day_now() and 0<=now-proposal['inspected_at']<=900,'FRESH_INSPECTION')
    return dict(approval)


def identifier(name):
    need(type(name) is str and re.fullmatch('[a-z_]+',name),'IDENTIFIER')
    return '`'+name+'`'


def literal(value):
    """Only scalar parameter values; caller controls the complete fixed SQL template."""
    if value is None:return 'NULL'
    if type(value) is int:
        need(-(2**63)<value<2**63)
        return str(value)
    need(type(value) is str and len(value.encode())<=1024,'PARAMETER')
    raw=value.encode('utf-8').hex()
    return "CONVERT(0x"+raw+" USING utf8mb4) COLLATE utf8mb4_unicode_ci" if raw else "'' COLLATE utf8mb4_unicode_ci"


def sql(template,*values):
    need(template.count('?')==len(values),'PARAMETER_COUNT')
    parts=template.split('?')
    return ''.join(piece+literal(values[i]) if i<len(values) else piece for i,piece in enumerate(parts))


def snapshot(db,schema):
    result={}
    for table,spec in sorted(schema.items()):
        columns=','.join("'"+column+"',IF("+identifier(column)+' IS NULL,NULL,SHA2(CAST('+identifier(column)+' AS CHAR CHARACTER SET utf8mb4),256))' for column in spec['columns'])
        key=','.join(identifier(column) for column in spec['primary'])
        rows=db.query("SELECT JSON_OBJECT('key',JSON_ARRAY("+key+"),'cells',JSON_OBJECT("+columns+')) FROM '+identifier(table)+' ORDER BY '+key+' LIMIT 257')
        need(len(rows)<=256,'ROW_LIMIT')
        values={}
        for row in rows:
            need(set(row)=={'key','cells'} and set(row['cells'])==set(spec['columns'])
                and all(v is None or pin(v) for v in row['cells'].values()),'SNAPSHOT')
            index=json.dumps(row['key'],separators=(',',':'))
            need(index not in values,'SNAPSHOT_KEY')
            values[index]=row['cells']
        result[table]=values
    need(len(canonical(result))<=4*1024*1024,'SNAPSHOT_BOUND')
    return result


def expected_users(private,passwords,ids,stamp):
    result={}
    for actor in ('admin','buyer'):
        row=dict(id=ids[actor],nickname=private[actor]['nickname'],email=private[actor]['email'],password=passwords[actor],
            phone_company=None,phone_personal=None,is_verified=1,role='ADMIN' if actor=='admin' else 'USER',job=None,
            company_name=None,user_type='INDIVIDUAL',is_deleted=0,refresh_token=None,created_at=stamp,updated_at=stamp)
        result[json.dumps([ids[actor]])]={k:cell(v) for k,v in row.items()}
    return result


def verify_delta(before,after,private,passwords,ids,proposal,stamp):
    need(set(before)==set(after),'TABLE_DELTA')
    need(ids['admin']!=ids['buyer'] and all(type(v) is int and v>1 for v in (ids['admin'],ids['buyer'])),'NEW_IDS')
    for table,rows in before.items():
        need(all(after[table].get(key)==row for key,row in rows.items()),'EXISTING_ROW_CHANGED')
        if table not in ALLOWED:need(after[table]==rows,'EXTRA_TABLE_DELTA')
    audit=dict(id=ids['audit'],action='USER_ROLE_CHANGE',target_type='USER',target_id=ids['admin'],actor_user_id=None,
        outcome='SUCCEEDED',before_state=BEFORE,after_state=AFTER,reason_code='ROLE_CHANGED',
        reason_note='TEST_FIXTURE_NEW_ADMIN:'+proposal['nonce'],created_at=stamp,updated_at=stamp)
    grant=dict(id=ids['grant'],user_id=ids['buyer'],subscription_id=proposal['plan']['id'],billing_cycle='MONTHLY',
        status='CANCELLED',started_at=proposal['day'],expires_at=proposal['day'],pending_subscription_id=None,
        pending_billing_cycle=None,created_at=stamp,updated_at=stamp)
    expected={'users':expected_users(private,passwords,ids,stamp),
        'admin_operation_audit_logs':{json.dumps([ids['audit']]):{k:cell(v) for k,v in audit.items()}},
        'user_subscriptions':{json.dumps([ids['grant']]):{k:cell(v) for k,v in grant.items()}}}
    for table,added in expected.items():
        need(not set(added)&set(before[table]) and after[table]=={**before[table],**added},'ROW_DELTA')
    need(before['users'].get('[1]') is not None and after['users']['[1]']==before['users']['[1]'],'ID1_CHANGED')


def transact(db,proposal,private,passwords,schema,inspect,record,guard):
    """Caller has verified external approval and written an exclusive durable claim."""
    result={'status':'HOLD','phase':'BEGIN','commit':'NOT_SENT','ids':{}}
    before=None
    commit_possible=False
    try:
        guard()
        inputs(private)
        need(digest(private)==proposal['input_sha256'] and proposal['day']==day_now(),'INPUT_BINDING')
        need(set(passwords)=={'admin','buyer'} and all(re.fullmatch(r'\$2a\$10\$[./A-Za-z0-9]{53}',v) for v in passwords.values()),'BCRYPT')
        db.query('SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE')
        db.query('SET SESSION innodb_lock_wait_timeout=3')
        db.query('START TRANSACTION')
        result['phase']='FRESH_GUARDS'
        observed=inspect(db,private)
        need(observed['schema_sha256']==proposal['schema_sha256'] and observed['server_uuid_sha256']==proposal['server_uuid_sha256'],'DATABASE_IDENTITY')
        need(observed['plans']==[proposal['plan']] and observed['admin_count']==observed['existing_work']==observed['collision_count']==0
            and observed['id1_eligible_unentitled'] is True,'FRESH_GUARDS')
        before=snapshot(db,schema)
        need(digest(before)==proposal['snapshot_sha256'],'SNAPSHOT_DRIFT')
        record('before.json',before)
        stamp=db.query("SELECT JSON_OBJECT('stamp',DATE_FORMAT(UTC_TIMESTAMP(),'%Y-%m-%d %H:%i:%s'))")[0]['stamp']
        need(type(stamp) is str and re.fullmatch(r'\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}',stamp),'STAMP')
        ids={}
        result['phase']='WRITE'
        guard()
        for actor in ('admin','buyer'):
            statement=sql('INSERT INTO users(nickname,email,password,is_verified,created_at,updated_at) VALUES(?,?,?,1,?,?)',
                private[actor]['nickname'],private[actor]['email'],passwords[actor],stamp,stamp)
            rows=db.query(statement+"; SELECT JSON_OBJECT('affected',ROW_COUNT(),'id',LAST_INSERT_ID())")
            need(len(rows)==1 and rows[0]['affected']==1 and type(rows[0]['id']) is int and rows[0]['id']>1,'INSERT_USERS')
            ids[actor]=rows[0]['id']
        need(ids['admin']!=ids['buyer'],'NEW_IDS')
        rows=db.query(sql("UPDATE users SET role='ADMIN',updated_at=? WHERE id=? AND role='USER' AND is_deleted=0 AND is_verified=1 AND refresh_token IS NULL",stamp,ids['admin'])+"; SELECT JSON_OBJECT('affected',ROW_COUNT())")
        need(rows==[{'affected':1}],'PROMOTION')
        rows=db.query(sql("INSERT INTO admin_operation_audit_logs(action,target_type,target_id,actor_user_id,outcome,before_state,after_state,reason_code,reason_note,created_at,updated_at) VALUES('USER_ROLE_CHANGE','USER',?,NULL,'SUCCEEDED',?,?,'ROLE_CHANGED',?,?,?)",
            ids['admin'],BEFORE,AFTER,'TEST_FIXTURE_NEW_ADMIN:'+proposal['nonce'],stamp,stamp)+"; SELECT JSON_OBJECT('affected',ROW_COUNT(),'id',LAST_INSERT_ID())")
        need(len(rows)==1 and rows[0]['affected']==1 and type(rows[0]['id']) is int and rows[0]['id']>0,'AUDIT')
        ids['audit']=rows[0]['id']
        rows=db.query(sql("INSERT INTO user_subscriptions(user_id,subscription_id,billing_cycle,status,started_at,expires_at,pending_subscription_id,pending_billing_cycle,created_at,updated_at) VALUES(?,?,'MONTHLY','CANCELLED',?,?,NULL,NULL,?,?)",
            ids['buyer'],proposal['plan']['id'],proposal['day'],proposal['day'],stamp,stamp)+"; SELECT JSON_OBJECT('affected',ROW_COUNT(),'id',LAST_INSERT_ID())")
        need(len(rows)==1 and rows[0]['affected']==1 and type(rows[0]['id']) is int and rows[0]['id']>0,'GRANT')
        ids['grant']=rows[0]['id']
        result['phase']='DELTA'
        after=snapshot(db,schema)
        verify_delta(before,after,private,passwords,ids,proposal,stamp)
        need(proposal['day']==day_now(),'DAY_ROLLOVER')
        guard()
        record('expected-after.json',after)
        record('commit-intent.json',{'plan_sha256':digest(proposal),'before_sha256':digest(before),'after_sha256':digest(after),'ids':ids})
        commit_possible=True
        result.update(phase='COMMIT',commit='POSSIBLE')
        db.query('COMMIT')
        result.update(status='APPLIED',phase='COMPLETE',commit='ACKNOWLEDGED',ids=ids)
        record('commit-ack.json',{'status':'APPLIED','ids':ids})
    except BaseException:
        result['status']='HOLD_RECONCILE' if commit_possible else 'HOLD'
        try:
            db.query('ROLLBACK')
            result['rollback']='ACKNOWLEDGED'
        except BaseException:
            result['rollback']='UNCONFIRMED'
            result['status']='HOLD_RECONCILE'
        if not commit_possible:result['ids']={}
    return result
