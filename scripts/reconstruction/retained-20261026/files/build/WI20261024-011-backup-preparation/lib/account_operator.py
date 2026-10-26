"""MA-only read-only account/plan recheck; original WI002 source-mode lifecycle."""
from datetime import datetime, timedelta, timezone
import hashlib
import os
from pathlib import Path
import re
import signal
import stat
import time
import inventory
import backup_evidence

RUNTIME = '3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d'
SOURCE_OPERATOR = 'c737d94dd8d184982167489de39b86e0c8a59a46d8c851ba6622adb7bf8b8435'
CORE = '84ce1f82769c5c6a70bcc3b5090a929dd21d526dc590198a3b221545af469c7e'
COLD = 'd577629170099da3345c528e69a5fb60d7b52892dc20bf2de7b52307a86db766'
JAR = 'f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81'
SOURCE = 'c6fc15d8f38c6597d159c7dd258048d25e74b3d03f5eb5596b701219a8ae207e'
NAME = 'atstudio-wi20261024-008-account-grant-evidence-source.service'
SCOPE = 'REQ007_20261024_READONLY_ACCOUNT_GRANT_BACKUP_EVIDENCE'
SOCKET = '/run/mysqld/mysqld.sock'
OLD_WRITERS = ('atstudio-restricted-web-wi060.service','atstudio-restricted-web-wi055.service',
    'atstudio-restricted-web-wi042.service','atstudio-first-start-wi026.service','atstudio-first-start-wi032.service')


def authority(value):
    inventory.need(type(value) is dict and set(value)=={'scope','boot_id','machine_id_sha256','expires_at'})
    inventory.need(value['scope']==SCOPE)
    inventory.need(type(value['boot_id']) is str and re.fullmatch(r'[a-f0-9-]{36}',value['boot_id']))
    inventory.need(type(value['machine_id_sha256']) is str and re.fullmatch('[a-f0-9]{64}',value['machine_id_sha256']))
    inventory.need(type(value['expires_at']) is int and 0 < value['expires_at']-time.time() <= 900)


def bindings(h, r, machine):
    """Only fixed receipt projections; no passwords, profile fields or cell hashes escape."""
    result = {}
    def designated(value):
        uid=value.get('user_id')
        inventory.need(value.get('req')=='REQ-20261011-ATS-007' and value.get('wi')=='WI-20261011-ATS-063'
            and value.get('machine_id_sha256')==machine and type(uid) is str and
            re.fullmatch('[1-9][0-9]{0,18}',uid) and int(uid)<=2**63-1 and
            re.fullmatch('[a-f0-9]{64}',str(value.get('designation_sha256',''))))
        return int(uid)
    approval_path = Path('/etc/atstudio/feature-provisioning-approval.json')
    if approval_path.exists():
        value = h.decode(h.read(approval_path,8192,True))
        result['FIRST_ADMIN_DESIGNATION_ONLY']=designated(value)
    receipt_path=Path('/etc/atstudio/feature-first-admin.receipt.json')
    claim_path=Path('/etc/atstudio/feature-first-admin.claim.json')
    if receipt_path.exists():
        receipt=h.decode(h.read(receipt_path,8192,True))
        claim=h.decode(h.read(claim_path,8192,True))
        inventory.need(set(receipt)=={'intent','status','audit_id'} and receipt['intent']==claim
            and receipt['status']=='APPLIED' and type(receipt['audit_id']) is int and 0<receipt['audit_id']<2**63)
        uid=designated(claim)
        inventory.need(result.get('FIRST_ADMIN_DESIGNATION_ONLY',uid)==uid)
        result.pop('FIRST_ADMIN_DESIGNATION_ONLY',None)
        result['FIRST_ADMIN_APPLIED_RECEIPT']=uid
    baseline_path = r.CONTROL/'ledger-baseline.json'
    if baseline_path.exists():
        before = h.decode(h.read(baseline_path,4*1024*1024,True))['tables']['users']
        paths = sorted(r.CONTROL.glob('ledger-[0-9][0-9][0-9].json'))
        inventory.need(len(paths)<=96)
        total = 0
        signup = []
        for path in paths:
            raw = h.read(path,4*1024*1024,True)
            total += len(raw)
            inventory.need(total<=32*1024*1024)
            value = h.decode(raw)
            if value.get('action')=='SIGNUP':
                after = value['snapshot']['tables']['users']
                added = set(after)-set(before)
                inventory.need(set(before)<=set(after) and len(added)==1)
                uid = added.pop()
                inventory.need(type(uid) is str and re.fullmatch('[1-9][0-9]{0,18}',uid) and int(uid)<=2**63-1)
                signup.append(int(uid))
        inventory.need(len(signup)<=1)
        if signup:
            result['LATEST_AUTH_SIGNUP_DESIGNATED'] = signup[0]
    return result


def identity(h,n,r,approved):
    authority(approved)
    h.need(os.name=='posix' and os.geteuid()==0)
    h.need(Path('/proc/sys/kernel/random/boot_id').read_text().strip()==approved['boot_id'])
    h.need(h.sha(h.read(Path('/etc/machine-id')))==approved['machine_id_sha256'])
    h.need(r.verify_tree()==RUNTIME)
    artifact=h.decode(h.read(r.TOOLS/'binding'/'artifact.json'))
    h.need(artifact['jar']['sha256']==JAR and artifact['source']['sha256']==SOURCE)
    h.need(h.sha(h.read(r.RELEASE/'app.jar',80*1024*1024))==JAR)
    h.need(h.sha(h.read(r.EVIDENCE/'source-before.json',262144,True))==SOURCE)
    historical=h.decode(h.read(r.CONTROL/'approval.json',private=True))
    h.need(historical['candidate_sha256']==RUNTIME and historical['boot_id']==approved['boot_id'])
    h.need(h.read(r.CONTROL/'approval.json',private=True)==h.read(r.APPROVAL,private=True))
    for file in ('attempt.json','result.json'):
        receipt=h.decode(h.read(r.CONTROL/file,private=True))
        h.need(receipt.get('status')=='BOUNDED_AUTH_FLOW_OBSERVED' and receipt.get('cleanup')=='PASS'
            and receipt.get('app_start')=='JAVA_RELEASED')
    for path,pin in historical['quarantine_sha256'].items():
        h.need(path in r.TLS and h.sha(h.read(Path(path)))==pin)
    h.need(set(historical['quarantine_sha256'])==set(r.TLS))
    return historical


def properties(h,unit,keys):
    """--all keeps native empty properties present; reject missing/duplicate fields."""
    raw=h.ctl('show','--all','--no-pager','--property='+','.join(keys),'--',unit)
    pairs=[line.partition('=') for line in raw.decode('ascii').splitlines()]
    h.need(len(pairs)==len(keys) and {key for key,sep,value in pairs}==set(keys)
        and all(sep for key,sep,value in pairs))
    return {key:value for key,sep,value in pairs}


def cold(h,n,r,historical):
    states=[]
    keys=('LoadState','ActiveState','MainPID','ControlPID','Job','DropInPaths')
    for unit in (r.APP,r.SUPERVISOR,'mysql.service',*OLD_WRITERS):
        state=properties(h,unit,keys)
        h.need(state['ActiveState'] in ('inactive','failed') and state['MainPID']==state['ControlPID']=='0'
            and not state['Job'] and not state['DropInPaths'])
        if unit==r.SUPERVISOR and state['LoadState']=='loaded':
            h.need(h.read(r.SUPERVISOR_FILE)==n.supervisor_unit().encode())
        else:
            h.need(state['LoadState'] in ('masked','not-found'))
        if unit=='mysql.service':
            h.need(state['LoadState']=='masked' and os.readlink('/etc/systemd/system/mysql.service')=='/dev/null')
        states.append(state)
    h.need(not n.uid_members(999) and not n.uid_members(110))
    n.uid_sockets_absent(999)
    h.need(not {'java','ffmpeg','mysqld'} & set(h.command(('/usr/bin/ps','-e','-o','comm=')).decode().split()))
    nft=h.decode(h.command(('/usr/sbin/nft','-j','list','tables')))
    h.need(set(nft)=={'nftables'} and all(set(row)=={'metainfo'} for row in nft['nftables']))
    listeners=n.listeners()
    n.listener_guard(listeners)
    web=n.service('nginx.service')
    h.need(web['DropInPaths']==r.TLS[1] and h.properties('nginx.service',('ActiveState',))['ActiveState']=='active')
    for path,pin in historical['quarantine_sha256'].items():
        h.need(h.sha(h.read(Path(path)))==pin)
    return states,web,listeners


def run(h,n,r,source_raw,cold_raw,core_raw,approved,probe_only=False):
    result=dict(status='HOLD',phase='GUARD',code='NATIVE_HOLD',inventory_status='NOT_RUN',cleanup={})
    armed=False
    handlers={}
    def state():
        return properties(h,NAME,('LoadState','ActiveState','MainPID','ControlPID','Job'))
    def interrupt(*_):
        raise RuntimeError('INTERRUPTED')
    try:
        historical=identity(h,n,r,approved)
        for raw,pin in ((source_raw,SOURCE_OPERATOR),(cold_raw,COLD),(core_raw,CORE)):
            h.need(hashlib.sha256(raw).hexdigest()==pin)
        source={}
        exec(compile(source_raw,'<pinned-source-operator>','exec'),source)
        for path,pin in source['PINS'].items():
            h.need(h.sha(h.read(Path(path),100*1024*1024))==pin)
        helper=source['helpers'](cold_raw,h)
        core={'__name__':'account_query_primitives'}
        exec(compile(core_raw,'<pinned-query>','exec'),core)
        before=cold(h,n,r,historical)
        h.need(not os.path.lexists('/run/mysqld'))
        own=state()
        h.need(own['LoadState']=='not-found' and own['MainPID']==own['ControlPID']=='0' and not own['Job'])
        actors=bindings(h,r,approved['machine_id_sha256'])
        result['bindings']=actors
        result['baseline']='COLD_QUARANTINE_MATCH'
        if probe_only:
            result.update(status='PREFLIGHT_PASS',phase='COMPLETE',code='NONE')
            return result
        env=helper['mysql_client_environment']()
        def query(sql):
            return core['bounded_query'](('/usr/bin/mysql','--no-defaults','--protocol=socket',
                '--socket='+SOCKET,'--user=root','--batch','--raw','--skip-column-names',
                '--connect-timeout=3','--database=atstudio_prod'),sql,env)
        for signum in (signal.SIGTERM,signal.SIGHUP,signal.SIGINT):
            handlers[signum]=signal.signal(signum,interrupt)
        authority(approved)
        result['phase']='LOG_PREPARE'
        helper['prepare_mysql_log'](None,NAME)
        armed=True
        result['phase']='START_COMMAND'
        h.command(tuple(helper['mysql_unit_arguments'](NAME,'source')),seconds=40)
        result['phase']='SOCKET_WAIT'
        end=time.monotonic()+45
        while not Path(SOCKET).exists():
            h.need(time.monotonic()<end)
            time.sleep(0.2)
        result['phase']='SOURCE_IDENTITY'
        info=Path(SOCKET).lstat()
        h.need(stat.S_ISSOCK(info.st_mode) and (info.st_uid,info.st_gid)==(110,112))
        pid=state()['MainPID']
        h.need(re.fullmatch('[1-9][0-9]*',pid))
        h.need(Path('/proc/'+pid+'/cgroup').read_text().strip()=='0::/system.slice/'+NAME)
        expected=Path('/var/lib/mysql').stat()
        actual=Path('/proc/'+pid+'/root/var/lib/mysql').stat()
        h.need((expected.st_dev,expected.st_ino)==(actual.st_dev,actual.st_ino))
        result['phase']='CONTAINMENT'
        code,raw,_=query(b'SELECT @@version,@@skip_networking,@@event_scheduler,@@local_infile,@@read_only,@@super_read_only;\n')
        h.need(code==0 and raw.strip().split(b'\t')==[b'8.0.46-0ubuntu0.24.04.4',b'1',b'DISABLED',b'0',b'1',b'1'])
        result['phase']='INVENTORY'
        day=datetime.now(timezone(timedelta(hours=9))).date().isoformat()
        code,raw,error=query(inventory.queries(day))
        if code:
            result['mysql_errno'],result['sqlstate']=core['mysql_error'](error)
            raise ValueError('MYSQL_QUERY')
        result['inventory']=inventory.parse(raw,actors)
        result['inventory']['application_day_asia_seoul']=day
        result['backup_evidence']=backup_evidence.observe(h,result['inventory']['counts'])
        result.update(status='INVENTORY_OBSERVED',phase='COMPLETE',code='NONE',inventory_status='PASS')
    except BaseException:
        result['status']='HOLD'
        result['code']='NATIVE_HOLD'
    finally:
        for signum in handlers:
            signal.signal(signum,signal.SIG_IGN)
        if armed:
            checks=(('STOP',lambda:h.ctl('stop',NAME,seconds=30)),
                ('PID_ZERO',lambda:h.need(state()['MainPID']==state()['ControlPID']=='0' and not state()['Job'])),
                ('SOCKET_GONE',lambda:h.need(not os.path.lexists('/run/mysqld'))),
                ('BASELINE',lambda:h.need(cold(h,n,r,historical)==before)))
            for label,check in checks:
                try:
                    check()
                    result['cleanup'][label]='PASS'
                except BaseException:
                    result['cleanup'][label]='FAILED'
            if 'FAILED' in result['cleanup'].values():
                result['status']='HOLD'
        for signum,handler in handlers.items():
            signal.signal(signum,handler)
    return result
