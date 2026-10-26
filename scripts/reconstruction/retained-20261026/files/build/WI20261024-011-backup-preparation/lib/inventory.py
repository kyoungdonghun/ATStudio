"""Fixed numeric actors and grant metadata; bounded SELECT-only snapshot."""
from datetime import date
import json
import re


def need(value,code='INVENTORY'):
    if not value:raise ValueError(code)


def unique(pairs):
    result={}
    for key,value in pairs:
        need(key not in result)
        result[key]=value
    return result


def valid_day(day):
    need(type(day) is str and re.fullmatch(r'\d{4}-\d{2}-\d{2}',day))
    date.fromisoformat(day)
    return day


def queries(day):
    valid_day(day)
    effective=f"us.status IN ('ACTIVE','CANCELLED') AND us.expires_at >= '{day}'"
    return ("SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;\n"
        "START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY;\n"
        "SELECT /*+ MAX_EXECUTION_TIME(3000) */ JSON_OBJECT('kind','counts',"
        "'users',(SELECT COUNT(*) FROM users),'selected_users',(SELECT COUNT(*) FROM users WHERE id IN (1,2,3)),"
        "'plans',(SELECT COUNT(*) FROM subscriptions),'subscription_rows',(SELECT COUNT(*) FROM user_subscriptions),"
        "'grants',(SELECT COUNT(*) FROM user_subscriptions WHERE user_id IN (1,2,3)),"
        "'admin_audits',(SELECT COUNT(*) FROM admin_operation_audit_logs),"
        "'pending_audio',(SELECT COUNT(*) FROM tracks WHERE audio_processing_state IN ('PENDING','PROCESSING') "
        "OR pending_audio_file IS NOT NULL OR claimed_audio_file IS NOT NULL OR audio_claim_token IS NOT NULL),"
        "'unfinished_storage',(SELECT COUNT(*) FROM storage_mutations WHERE state <> 'DONE'));\n"
        "SELECT /*+ MAX_EXECUTION_TIME(3000) */ JSON_OBJECT('kind','account','id',u.id,'role',u.role,"
        "'deleted',CAST(u.is_deleted AS UNSIGNED),'verified',CAST(u.is_verified AS UNSIGNED),"
        "'total_subscriptions',(SELECT COUNT(*) FROM user_subscriptions us WHERE us.user_id=u.id),"
        f"'effective_subscriptions',(SELECT COUNT(*) FROM user_subscriptions us WHERE us.user_id=u.id AND {effective}),"
        "'eligible_subscriptions',(SELECT COUNT(*) FROM user_subscriptions us JOIN subscriptions s "
        f"ON s.id=us.subscription_id WHERE us.user_id=u.id AND {effective} AND "
        "(s.download_per_day=-1 OR s.download_per_day>(SELECT COUNT(*) FROM track_downloads td "
        f"WHERE td.user_id=u.id AND td.downloaded_at BETWEEN '{day} 00:00:00' "
        f"AND DATE_ADD('{day} 00:00:00',INTERVAL 1 DAY))))) FROM users u WHERE u.id IN (1,2,3) ORDER BY u.id LIMIT 4;\n"
        "SELECT /*+ MAX_EXECUTION_TIME(3000) */ JSON_OBJECT('kind','plan','id',s.id,"
        "'user_type',s.user_type,'is_active',CAST(s.is_active AS UNSIGNED),"
        "'download_per_day',s.download_per_day) FROM subscriptions s ORDER BY s.id LIMIT 101;\n"
        "SELECT /*+ MAX_EXECUTION_TIME(3000) */ JSON_OBJECT('kind','grant','id',us.id,'user_id',us.user_id,"
        "'subscription_id',us.subscription_id,'status',us.status,'billing_cycle',us.billing_cycle,"
        "'started_at',CAST(us.started_at AS CHAR),'expires_at',CAST(us.expires_at AS CHAR),"
        "'pending_subscription_id',us.pending_subscription_id,'pending_billing_cycle',us.pending_billing_cycle) "
        "FROM user_subscriptions us WHERE us.user_id IN (1,2,3) ORDER BY us.id LIMIT 101;\nROLLBACK;\n").encode('ascii')


def integer(value,lower=0,upper=1000000):
    need(type(value) is int and lower<=value<=upper)
    return value


def parse(raw,bindings):
    need(type(raw) is bytes and 0<len(raw)<=65536)
    rows=[json.loads(line,object_pairs_hook=unique) for line in raw.splitlines()]
    need(1<=len(rows)<=204)
    counts=rows.pop(0)
    need(type(counts) is dict and set(counts)=={'kind','users','selected_users','plans','subscription_rows','grants','admin_audits','pending_audio','unfinished_storage'} and counts['kind']=='counts')
    for key,value in counts.items():
        if key!='kind':integer(value)
    need(counts['selected_users']<=min(3,counts['users']) and counts['plans']<=100
        and counts['grants']<=min(100,counts['subscription_rows'])
        and len(rows)==counts['selected_users']+counts['plans']+counts['grants'],'ROW_LIMIT')
    accounts=[]
    last=0
    for row in rows[:counts['selected_users']]:
        need(set(row)=={'kind','id','role','deleted','verified','total_subscriptions','effective_subscriptions','eligible_subscriptions'} and row['kind']=='account')
        integer(row['id'],last+1,3);last=row['id']
        need(row['role'] in ('ADMIN','USER'))
        for key in ('deleted','verified'):integer(row[key],0,1)
        for key in ('total_subscriptions','effective_subscriptions','eligible_subscriptions'):integer(row[key],0,100)
        need(row['eligible_subscriptions']<=row['effective_subscriptions']<=row['total_subscriptions'])
        accounts.append(dict(id=row['id'],role=row['role'],deleted=bool(row['deleted']),email_verified=bool(row['verified']),
            historical_designation=sorted(label for label,uid in bindings.items() if uid==row['id']),
            total_subscription_count=row['total_subscriptions'],effective_subscription_count=row['effective_subscriptions'],
            eligible_subscription_count=row['eligible_subscriptions']))
    plans=[];last=0
    start=counts['selected_users'];end=start+counts['plans']
    for row in rows[start:end]:
        need(set(row)=={'kind','id','user_type','is_active','download_per_day'} and row['kind']=='plan')
        integer(row['id'],last+1,2**63-1);last=row['id']
        need(row['user_type'] in ('INDIVIDUAL','BUSINESS'))
        integer(row['is_active'],0,1);integer(row['download_per_day'],-1)
        plans.append(dict(id=row['id'],user_type=row['user_type'],is_active=bool(row['is_active']),download_per_day=row['download_per_day']))
    grants=[];last=0
    for row in rows[end:]:
        need(set(row)=={'kind','id','user_id','subscription_id','status','billing_cycle','started_at','expires_at','pending_subscription_id','pending_billing_cycle'} and row['kind']=='grant')
        integer(row['id'],last+1,2**63-1);last=row['id']
        integer(row['user_id'],1,3);integer(row['subscription_id'],1,2**63-1)
        need(row['status'] in ('ACTIVE','CANCELLED','EXPIRED') and row['billing_cycle'] in ('MONTHLY','YEARLY'))
        need(valid_day(row['started_at'])<=valid_day(row['expires_at']))
        if row['pending_subscription_id'] is not None:integer(row['pending_subscription_id'],1,2**63-1)
        need(row['pending_billing_cycle'] in (None,'MONTHLY','YEARLY'))
        grants.append({key:value for key,value in row.items() if key!='kind'})
    need(all(sum(g['user_id']==a['id'] for g in grants)==a['total_subscription_count'] for a in accounts))
    need(all(g['user_id'] in {a['id'] for a in accounts} and g['subscription_id'] in {p['id'] for p in plans} for g in grants))
    actors={a['id']:a for a in accounts}
    expected_roles={1:'USER',2:'ADMIN',3:'USER'}
    return dict(accounts=accounts,plans=plans,grants=grants,counts={k:v for k,v in counts.items() if k!='kind'},
        expected_actor_roles_match=set(actors)==set(expected_roles) and all(actors[i]['role']==role for i,role in expected_roles.items()),
        existing_work_quiet=counts['pending_audio']==counts['unfinished_storage']==0,
        password_state='NOT_QUERIED',login='NOT_RUN',audio_authorized=False)
