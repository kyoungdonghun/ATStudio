"""Frozen WI005 lifecycle reused; stage2 authority and transaction only."""
import os,re,signal,stat,time,json
from pathlib import Path
import fixture_core as core
import fixture_native as old
from mysql_session import MysqlSession
import transaction as tx
HERE=old.HERE
TARGET=old.TARGET
EVIDENCE=Path('/var/lib/atstudio/wi20261024-023-stage-two')
runtime=old.runtime
mysql_arguments=old.mysql_arguments
cleanup=old.cleanup

def inspect_database(db,schema):
    placeholders={a:dict(email='stage2-unused-'+a+'@invalid.example',nickname='stage2-unused-'+a) for a in ('admin','buyer')}
    return old.inspect_database(db,placeholders,schema,1)

def native(mode,proposal,candidate_sha):
    result={'status':'HOLD','phase':'AUTHORITY','cleanup':{},'native':'NOT_STARTED'}
    armed=False
    db=None
    handlers={}
    try:
        core.need(os.name=='posix' and os.geteuid()==0 and HERE==TARGET,'NATIVE_TARGET')
        h,n,r,op,source,bounded=runtime()
        def guard():
            tx.validate(proposal,mode,candidate_sha)
        inspection=None
        if mode=='apply':
            claim=h.decode(h.read(EVIDENCE/proposal['inspection_nonce']/'claim.json',16384,True))
            receipt=h.decode(h.read(EVIDENCE/proposal['inspection_nonce']/'result.json',16384,True))
            core.need(claim==dict(wi=tx.WI,mode='inspect',nonce=proposal['inspection_nonce'],candidate_sha256=candidate_sha)
                and receipt['status']=='INSPECTION_RECORDED'
                and receipt['cleanup']==dict.fromkeys(('DB_SESSION','STOP','PID_ZERO','SOCKET_GONE','BASELINE'),'PASS'),'INSPECTION_CLAIM')
            inspection=h.decode(h.read(EVIDENCE/proposal['inspection_nonce']/'inspection.json',4*1024*1024,True))
            core.need(inspection['boot_id']==proposal['boot_id'] and inspection['machine_id_sha256']==proposal['machine_id_sha256']
                and 0<=int(time.time())-inspection['observed_at']<=900,'FRESH_INSPECTION')
        guard()
        identity_assertion=dict(scope='REQ007_20261023_PHASE1_DB_ONLY',boot_id=proposal['boot_id'],
            machine_id_sha256=proposal['machine_id_sha256'],expires_at=int(time.time())+900)
        historical=op.identity(h,n,r,identity_assertion)
        for path,pin in source.PINS.items():core.need(h.sha(h.read(Path(path),100*1024*1024))==pin,'SOURCE_PIN')
        before_cold=op.cold(h,n,r,historical)
        core.need(not os.path.lexists('/run/mysqld'),'SOCKET_BUSY')
        attempt=proposal['reconcile_attempt'] if mode=='reconcile' else proposal['nonce']
        name='atstudio-wi20261024-023-'+attempt+'-'+mode+'-'+candidate_sha[:12]+'.service'
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
            record('claim.json',dict(wi=tx.WI,mode=mode,nonce=proposal['nonce'],candidate_sha256=candidate_sha))
            record('plan.json',proposal)
        else:
            claim=h.decode(h.read(directory/'claim.json',16384,True))
            core.need(claim['mode']=='apply' and claim['nonce']==proposal['nonce'] and claim['candidate_sha256']==candidate_sha,'RECONCILE_BINDING')
            record('reconcile-'+attempt+'-plan.json',proposal)
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
            result.update(tx.transact(db,proposal,schema,lambda session:inspect_database(session,schema),record,guard,inspection))
        else:
            db.query('SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ')
            db.query('START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY')
            info=inspect_database(db,schema)
            snap=core.snapshot(db,schema)
            db.query('ROLLBACK')
            if mode=='inspect':
                tx.eligible(snap)
                core.need(info['existing_work']==0,'EXISTING_WORK')
                info.update(snapshot_sha256=core.digest(snap),observed_at=int(time.time()),boot_id=proposal['boot_id'],machine_id_sha256=proposal['machine_id_sha256'])
                record('inspection.json',info)
                record('snapshot.json',snap)
                result.update(status='INSPECTION_RECORDED',phase='COMPLETE',eligible_plan_ids=[p['id'] for p in info['plans'] if p['user_type']=='INDIVIDUAL' and p['is_active']==1 and p['download_per_day']>=1])
            else:
                expected_path=directory/'expected-after.json'
                expected=h.decode(h.read(expected_path,4*1024*1024,True)) if expected_path.exists() else None
                before=h.decode(h.read(directory/'before.json',4*1024*1024,True))
                status='APPLIED_CONFIRMED' if snap==expected else 'NOT_APPLIED_CONFIRMED' if snap==before else 'HOLD_PARTIAL_CHANGE'
                record('reconcile-'+attempt+'.json',{'status':status})
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
    try:
        if mode!='reconcile' and 'directory' in locals():
            record('result.json',result)
        elif mode=='reconcile' and 'attempt' in locals() and 'record' in locals():
            record('reconcile-'+attempt+'-result.json',result)
    except BaseException:
        result['status']='HOLD_RECONCILE'
    return result
