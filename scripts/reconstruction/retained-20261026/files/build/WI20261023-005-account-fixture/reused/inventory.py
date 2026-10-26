"""Fixed, bounded nonsecret account projection; never authenticates or writes SQL."""
from datetime import date
import json
import re


def need(value, code='INVENTORY'):
    if not value:
        raise ValueError(code)


def queries(day):
    need(type(day) is str and re.fullmatch(r'\d{4}-\d{2}-\d{2}', day))
    date.fromisoformat(day)
    # BETWEEN is intentionally inclusive at BOTH ends, matching Spring Data.
    effective = f"us.status IN ('ACTIVE','CANCELLED') AND us.expires_at >= '{day}'"
    return ("SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;\n"
        "START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY;\n"
        "SELECT /*+ MAX_EXECUTION_TIME(3000) */ JSON_OBJECT('kind','counts','users',"
        "(SELECT COUNT(*) FROM users),'pending_audio',(SELECT COUNT(*) FROM tracks WHERE "
        "audio_processing_state IN ('PENDING','PROCESSING') OR pending_audio_file IS NOT NULL "
        "OR claimed_audio_file IS NOT NULL OR audio_claim_token IS NOT NULL),"
        "'unfinished_storage',(SELECT COUNT(*) FROM storage_mutations WHERE state <> 'DONE'));\n"
        "SELECT /*+ MAX_EXECUTION_TIME(3000) */ JSON_OBJECT('kind','account','id',u.id,'role',u.role,"
        "'deleted',CAST(u.is_deleted AS UNSIGNED),'verified',CAST(u.is_verified AS UNSIGNED),"
        "'password_present',IF(u.password IS NULL OR u.password='',0,1),'effective_subscriptions',"
        f"(SELECT COUNT(*) FROM user_subscriptions us WHERE us.user_id=u.id AND {effective}),"
        "'eligible_subscriptions',(SELECT COUNT(*) FROM user_subscriptions us JOIN subscriptions s "
        f"ON s.id=us.subscription_id WHERE us.user_id=u.id AND {effective} AND "
        "(s.download_per_day=-1 OR s.download_per_day>(SELECT COUNT(*) FROM track_downloads td "
        f"WHERE td.user_id=u.id AND td.downloaded_at BETWEEN '{day} 00:00:00' "
        f"AND DATE_ADD('{day} 00:00:00',INTERVAL 1 DAY))))) FROM users u ORDER BY u.id LIMIT 101;\n"
        "ROLLBACK;\n").encode('ascii')


def unique(pairs):
    result = {}
    for key, value in pairs:
        need(key not in result)
        result[key] = value
    return result


def parse(raw, bindings):
    need(type(raw) is bytes and len(raw) <= 65536)
    rows = [json.loads(line, object_pairs_hook=unique) for line in raw.splitlines()]
    need(1 <= len(rows) <= 101)
    counts = rows.pop(0)
    need(set(counts) == {'kind', 'users', 'pending_audio', 'unfinished_storage'} and counts['kind'] == 'counts')
    need(all(type(counts[k]) is int and 0 <= counts[k] <= 1000000 for k in counts if k != 'kind'))
    need(counts['users'] <= 100 and len(rows) == counts['users'], 'ACCOUNT_LIMIT')
    result = []
    last = 0
    for row in rows:
        need(set(row) == {'kind','id','role','deleted','verified','password_present','effective_subscriptions','eligible_subscriptions'})
        need(row['kind'] == 'account' and type(row['id']) is int and last < row['id'] <= 2**63-1)
        last = row['id']
        need(row['role'] in ('ADMIN','USER'))
        need(all(type(row[k]) is int and row[k] in (0,1) for k in ('deleted','verified','password_present')))
        need(all(type(row[k]) is int and 0 <= row[k] <= 100 for k in ('effective_subscriptions','eligible_subscriptions')))
        need(row['eligible_subscriptions'] <= row['effective_subscriptions'])
        auth = row['deleted'] == 0 and row['verified'] == row['password_present'] == 1
        provenance = sorted(label for label, uid in bindings.items() if uid == row['id'])
        result.append(dict(id=row['id'], role=row['role'], designation=provenance or ['UNBOUND_CANDIDATE'],
            account_auth_prerequisites=auth, deleted=bool(row['deleted']), email_verified=bool(row['verified']),
            local_password_present=bool(row['password_present']),
            effective_subscription_count=row['effective_subscriptions'],
            entitled_user_with_quota=auth and row['role']=='USER' and row['effective_subscriptions']==1 and row['eligible_subscriptions']==1,
            unentitled_user=auth and row['role']=='USER' and row['effective_subscriptions']==0))
    return dict(accounts=result, counts={k:v for k,v in counts.items() if k!='kind'},
        existing_work_quiet=counts['pending_audio']==counts['unfinished_storage']==0,
        phase2_authorized=False, password_login_policy='NOT_CHECKED', credentials_known='NOT_CHECKED')
