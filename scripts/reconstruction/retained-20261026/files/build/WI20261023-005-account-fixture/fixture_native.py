"""Future root adapter. Entry requires exact external approval; inert on import."""
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import signal
import stat
import struct
import sys
import time
import fixture_core as core
from mysql_session import MysqlSession

HERE=Path(__file__).resolve().parent
TARGET=Path('/opt/atstudio/wi20261023-005-fixture')
EVIDENCE=Path('/var/lib/atstudio/wi20261023-005-fixture')
APPROVAL=Path('/etc/atstudio/wi20261023-005-native-approval.json')
HUMAN=Path('/etc/atstudio/wi20261023-005-human-approval.json')
RUNTIME_PIN='3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d'


def load_module(name,path):
    spec=importlib.util.spec_from_file_location(name,path)
    module=importlib.util.module_from_spec(spec)
    sys.modules[name]=module
    spec.loader.exec_module(module)
    return module


def runtime():
    """Validate the existing frozen runtime before import, as WI002 does."""
    root=Path('/opt/atstudio/wi067-tools')
    def read(path):
        for parent in path.parents:
            st=parent.lstat()
            core.need(stat.S_ISDIR(st.st_mode) and st.st_uid==0 and not st.st_mode&0o022)
        st=path.lstat()
        core.need(stat.S_ISREG(st.st_mode) and st.st_uid==0 and not st.st_mode&0o022 and st.st_nlink==1 and st.st_size<=2*1024*1024)
        return path.read_bytes()
    raw=read(root/'binding/freeze.json')
    core.need(hashlib.sha256(raw).hexdigest()==RUNTIME_PIN)
    manifest=json.loads(raw)
    observed={}
    blob=bytearray()
    for path in sorted(root.rglob('*')):
        core.need(not path.is_symlink())
        if path.is_file() and path!=root/'binding/freeze.json':
            name=path.relative_to(root).as_posix()
            raw=read(path)
            observed[name]=hashlib.sha256(raw).hexdigest()
            blob.extend(name.encode()+b'\0'+raw)
    core.need(observed==manifest['files'] and hashlib.sha256(blob).hexdigest()==manifest['bundle_sha256'])
    sys.path[:0]=[str(root/'binding'),str(root)]
    import host as h,native as n,release as r
    sys.path.insert(0,str(HERE/'reused'))
    op=load_module('fixture_phase1_operator',HERE/'reused/account_operator.py')
    source=load_module('fixture_source_operator',HERE/'reused/source_operator.py')
    bounded=load_module('fixture_bounded_query',HERE/'reused/diagnostic.py')
    return h,n,r,op,source,bounded


def inspect_database(db,private,schema,plan_id=None):
    """All returned row hashes and identities stay in private inspection evidence."""
    tables=db.query("SELECT JSON_OBJECT('name',TABLE_NAME,'engine',ENGINE,'type',TABLE_TYPE) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() ORDER BY TABLE_NAME LIMIT 101")
    core.need({row['name'] for row in tables}==set(schema) and len(tables)==len(schema)
        and all(row['engine']=='InnoDB' and row['type']=='BASE TABLE' for row in tables),'TABLE_SCHEMA')
    columns=db.query("SELECT JSON_OBJECT('table',TABLE_NAME,'name',COLUMN_NAME,'type',COLUMN_TYPE,'nullable',IS_NULLABLE,'default',COLUMN_DEFAULT,'extra',EXTRA,'collation',COLLATION_NAME) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() ORDER BY TABLE_NAME,ORDINAL_POSITION LIMIT 1001")
    core.need(len(columns)<1001,'METADATA_LIMIT')
    for table,spec in schema.items():
        core.need([row['name'] for row in columns if row['table']==table]==spec['columns'],'COLUMN_SCHEMA')
    indexes=db.query("SELECT JSON_OBJECT('table',TABLE_NAME,'index',INDEX_NAME,'nonunique',NON_UNIQUE,'seq',SEQ_IN_INDEX,'column',COLUMN_NAME,'subpart',SUB_PART,'type',INDEX_TYPE) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX LIMIT 1001")
    core.need(len(indexes)<1001,'METADATA_LIMIT')
    for table,spec in schema.items():
        core.need([row['column'] for row in indexes if row['table']==table and row['index']=='PRIMARY']==spec['primary'],'PRIMARY_SCHEMA')
    groups={}
    for row in indexes:
        if row['table']=='users' and row['nonunique']==0:groups.setdefault(row['index'],[]).append(row)
    unique_users={rows[0]['column'] for rows in groups.values() if len(rows)==1 and rows[0]['seq']==1 and rows[0]['subpart'] is None}
    core.need({'email','nickname'}<=unique_users,'UNIQUENESS')
    summary=db.query("SELECT JSON_OBJECT('database',DATABASE(),'server_uuid_sha256',SHA2(@@server_uuid,256),'triggers',(SELECT COUNT(*) FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA=DATABASE()),'other_clients',(SELECT COUNT(*) FROM information_schema.PROCESSLIST WHERE ID<>CONNECTION_ID() AND COMMAND<>'Daemon'),'admin_count',(SELECT COUNT(*) FROM users WHERE role='ADMIN'),'existing_work',(SELECT COUNT(*) FROM tracks WHERE audio_processing_state IN ('PENDING','PROCESSING') OR pending_audio_file IS NOT NULL OR claimed_audio_file IS NOT NULL OR audio_claim_token IS NOT NULL)+(SELECT COUNT(*) FROM storage_mutations WHERE state<>'DONE'),'id1',(SELECT COUNT(*) FROM users WHERE id=1 AND role='USER' AND is_deleted=0 AND is_verified=1 AND password IS NOT NULL AND password<>'' AND NOT EXISTS(SELECT 1 FROM user_subscriptions WHERE user_id=1)))")
    core.need(len(summary)==1 and summary[0]['database']=='atstudio_prod' and summary[0]['triggers']==summary[0]['other_clients']==0,'DATABASE_GUARD')
    row=summary[0]
    query="SELECT JSON_OBJECT('id',id,'user_type',user_type,'is_active',CAST(is_active AS UNSIGNED),'download_per_day',download_per_day) FROM subscriptions"
    if plan_id is not None:query+=core.sql(' WHERE id=?',plan_id)
    plans=db.query(query+' ORDER BY id LIMIT 101')
    core.need(len(plans)<=100,'PLAN_LIMIT')
    collision=db.query(core.sql("SELECT JSON_OBJECT('count',COUNT(*)) FROM users WHERE email IN (?,?) OR nickname IN (?,?)",
        private['admin']['email'],private['buyer']['email'],private['admin']['nickname'],private['buyer']['nickname']))
    core.need(len(collision)==1)
    return dict(status='INSPECTED',schema_sha256=core.digest([tables,columns,indexes]),server_uuid_sha256=row['server_uuid_sha256'],
        admin_count=row['admin_count'],existing_work=row['existing_work'],id1_eligible_unentitled=row['id1']==1,
        collision_count=collision[0]['count'],plans=plans)


def encode_passwords(private,bounded,env):
    raw=b''.join(struct.pack('>I',len(private[a]['password'].encode()))+private[a]['password'].encode() for a in ('admin','buyer'))
    cp=':'.join(str(HERE/'crypto'/name) for name in ('','spring-security-crypto-7.0.7.jar','commons-logging-1.3.6.jar'))
    command=('/usr/bin/java','-cp',cp,'FixturePasswords')
    code,out,_=bounded.bounded_query(command,raw,env,seconds=15)
    core.need(code==0 and len(out)<=124,'BCRYPT_CHILD')
    values=out.decode('ascii').splitlines()
    core.need(len(values)==2 and all(re.fullmatch(r'\$2a\$10\$[./A-Za-z0-9]{53}',v) for v in values),'BCRYPT_OUTPUT')
    return dict(zip(('admin','buyer'),values))


def mysql_arguments(helper,name,mode):
    argv=helper['mysql_unit_arguments'](name,'source')
    core.need(mode in ('inspect','apply','reconcile'),'MODE')
    if mode=='apply':
        for switch in ('--read-only=ON','--super-read-only=ON'):
            core.need(argv.count(switch)==1,'MYSQL_FLAGS')
            argv[argv.index(switch)]=switch.replace('=ON','=OFF')
    return argv


def cleanup(checks):
    result={}
    for label,check in checks:
        try:check();result[label]='PASS'
        except BaseException:result[label]='FAILED'
    return result


def native(mode,proposal,private,candidate_sha,approval_sha,human_sha):
    result={'status':'HOLD','phase':'AUTHORITY','cleanup':{},'native':'NOT_STARTED'}
    armed=False
    db=None
    handlers={}
    try:
        core.need(os.name=='posix' and os.geteuid()==0 and HERE==TARGET,'NATIVE_TARGET')
        h,n,r,op,source,bounded=runtime()
        core.inputs(private)
        core.need(core.digest(private)==proposal['input_sha256'],'INPUT_BINDING')
        def guard():
            raw=h.read(APPROVAL,16384,True)
            human=h.read(HUMAN,16384,True)
            core.need(h.sha(raw)==approval_sha and h.sha(human)==human_sha,'APPROVAL_BYTES')
            record=h.decode(human)
            core.need(record=={'wi':core.WI,'scope':core.SCOPE,'mode':mode,'plan_sha256':core.digest(proposal),
                'candidate_sha256':candidate_sha,'authorization':'EXPLICIT_FUTURE_NATIVE_APPROVAL',
                'verified_test_fixture':'APPROVED_NO_MAIL_OR_CONSENT_EVENT'},'HUMAN_APPROVAL')
            core.authorize(h.decode(raw),proposal,candidate_sha,human_sha,mode)
        guard()
        identity_assertion=dict(scope='REQ007_20261023_PHASE1_DB_ONLY',boot_id=proposal['boot_id'],
            machine_id_sha256=proposal['machine_id_sha256'],expires_at=int(time.time())+900)
        historical=op.identity(h,n,r,identity_assertion)
        for path,pin in source.PINS.items():core.need(h.sha(h.read(Path(path),100*1024*1024))==pin,'SOURCE_PIN')
        before_cold=op.cold(h,n,r,historical)
        core.need(not os.path.lexists('/run/mysqld'),'SOCKET_BUSY')
        name='atstudio-wi20261023-005-'+proposal['nonce']+'-'+mode+'-'+approval_sha[:12]+'.service'
        def state():return op.properties(h,name,('LoadState','MainPID','ControlPID','Job'))
        core.need(state()=={'LoadState':'not-found','MainPID':'0','ControlPID':'0','Job':''},'UNIT_EXISTS')
        helper=source.helpers((HERE/'reused/cold_capture.py').read_bytes(),h)
        env=helper['mysql_client_environment']()
        schema=json.loads((HERE/'reused/schema.json').read_bytes())
        h.secure(EVIDENCE,0o700,True)
        directory=EVIDENCE/proposal['nonce']
        if mode=='reconcile':h.secure(directory,0o700,True)
        else:
            core.need(not os.path.lexists(directory),'REPLAY')
            h.directory(directory)
        def record(file,value):h.publish(directory/file,core.canonical(value))
        if mode!='reconcile':
            record('claim.json',dict(wi=core.WI,mode=mode,plan_sha256=core.digest(proposal),candidate_sha256=candidate_sha,
                approval_sha256=approval_sha,human_evidence_sha256=human_sha))
            record('plan.json',proposal)
        else:
            claim=h.decode(h.read(directory/'claim.json',16384,True))
            core.need(claim['mode']=='apply' and claim['plan_sha256']==core.digest(proposal)
                and claim['candidate_sha256']==candidate_sha,'RECONCILE_BINDING')
        for signum in (signal.SIGHUP,signal.SIGINT,signal.SIGTERM):
            def interrupted(*_):raise ValueError('INTERRUPTED')
            handlers[signum]=signal.signal(signum,interrupted)
        guard()
        result['phase']='MYSQL_START'
        helper['prepare_mysql_log'](None,name)
        argv=mysql_arguments(helper,name,mode)
        armed=True
        result['native']='START_POSSIBLE'
        h.command(tuple(argv),seconds=40)
        deadline=time.monotonic()+45
        while not Path(op.SOCKET).exists():
            core.need(time.monotonic()<deadline,'MYSQL_SOCKET_TIMEOUT')
            time.sleep(0.2)
        sock=Path(op.SOCKET).lstat()
        core.need(stat.S_ISSOCK(sock.st_mode) and (sock.st_uid,sock.st_gid)==(110,112))
        pid=state()['MainPID']
        core.need(re.fullmatch('[1-9][0-9]*',pid) and Path('/proc/'+pid+'/cgroup').read_text().strip()=='0::/system.slice/'+name)
        expected=Path('/var/lib/mysql').stat();actual=Path('/proc/'+pid+'/root/var/lib/mysql').stat()
        core.need((expected.st_dev,expected.st_ino)==(actual.st_dev,actual.st_ino),'DATABASE_MOUNT')
        db=MysqlSession(['/usr/bin/mysql','--no-defaults','--protocol=socket','--socket='+op.SOCKET,'--user=root',
            '--batch','--raw','--skip-column-names','--unbuffered','--binary-mode','--skip-reconnect',
            '--connect-timeout=3','--default-character-set=utf8mb4','--database=atstudio_prod'],env)
        containment=db.query("SELECT JSON_OBJECT('version',@@version,'network',@@skip_networking,'events',@@event_scheduler,'infile',@@local_infile,'readonly',@@read_only,'super',@@super_read_only)")
        flag=0 if mode=='apply' else 1
        core.need(containment==[dict(version='8.0.46-0ubuntu0.24.04.4',network=1,events='DISABLED',infile=0,readonly=flag,super=flag)],'CONTAINMENT')
        db.query('SET SESSION max_execution_time=3000')
        guard()
        result['phase']='TRANSACTION'
        if mode=='apply':
            passwords=encode_passwords(private,bounded,env)
            result.update(core.transact(db,proposal,private,passwords,schema,
                lambda session,identities:inspect_database(session,identities,schema,proposal['plan']['id']),record,guard))
        else:
            db.query('SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ')
            db.query('START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY')
            info=inspect_database(db,private,schema,proposal.get('plan',{}).get('id'))
            snap=core.snapshot(db,schema)
            db.query('ROLLBACK')
            if mode=='inspect':
                info.update(snapshot_sha256=core.digest(snap),observed_at=int(time.time()),boot_id=proposal['boot_id'],machine_id_sha256=proposal['machine_id_sha256'])
                record('inspection.json',info)
                record('snapshot.json',snap)
                result.update(status='INSPECTION_RECORDED',phase='COMPLETE',eligible_plan_ids=[p['id'] for p in info['plans'] if p['user_type']=='INDIVIDUAL' and p['is_active']==1 and p['download_per_day']>=1])
            else:
                expected_path=directory/'expected-after.json'
                expected=h.decode(h.read(expected_path,4*1024*1024,True)) if expected_path.exists() else None
                before=h.decode(h.read(directory/'before.json',4*1024*1024,True))
                status='APPLIED_CONFIRMED' if snap==expected else 'NOT_APPLIED_CONFIRMED' if snap==before else 'HOLD_PARTIAL_CHANGE'
                record('reconcile-'+approval_sha+'.json',{'status':status})
                result.update(status=status,phase='COMPLETE')
    except BaseException:
        result['status']='HOLD_RECONCILE' if result.get('commit') in ('POSSIBLE','ACKNOWLEDGED') else 'HOLD'
    finally:
        for signum in handlers:signal.signal(signum,signal.SIG_IGN)
        if db is not None:
            try:db.close();result['cleanup']['DB_SESSION']='PASS'
            except BaseException:result['cleanup']['DB_SESSION']='FAILED';result['status']='HOLD_RECONCILE'
        if armed:
            checks=(('STOP',lambda:h.ctl('stop',name,seconds=30)),
                ('PID_ZERO',lambda:core.need(state()['MainPID']==state()['ControlPID']=='0' and not state()['Job'])),
                ('SOCKET_GONE',lambda:core.need(not os.path.lexists('/run/mysqld'))),
                ('BASELINE',lambda:core.need(op.cold(h,n,r,historical)==before_cold)))
            result['cleanup'].update(cleanup(checks))
            if 'FAILED' in result['cleanup'].values():result['status']='HOLD_RECONCILE'
        for signum,handler in handlers.items():signal.signal(signum,handler)
    return result
