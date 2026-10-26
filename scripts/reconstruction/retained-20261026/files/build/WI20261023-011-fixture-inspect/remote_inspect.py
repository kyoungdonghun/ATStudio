"""Embedded by inspect_tools; no standalone CLI or selectable write branch."""
import base64,hashlib,json,os,re,stat,sys,time
from pathlib import Path
sys.dont_write_bytecode=True
ROOT=Path('/opt/atstudio/wi20261023-005-fixture')
EVIDENCE=Path('/var/lib/atstudio/wi20261023-005-fixture')


def need(ok):
    if not ok:raise ValueError('INSPECT_GATE')


def canonical(value):return json.dumps(value,sort_keys=True,separators=(',',':')).encode()


def secure(path,directory=False,mode=None):
    for parent in path.parents:
        st=parent.lstat()
        need(stat.S_ISDIR(st.st_mode) and st.st_uid==0 and not st.st_mode&0o022)
    st=path.lstat()
    need(st.st_uid==0 and not st.st_mode&0o022 and not path.is_symlink())
    need(stat.S_ISDIR(st.st_mode) if directory else stat.S_ISREG(st.st_mode) and st.st_nlink==1)
    if mode is not None:need(stat.S_IMODE(st.st_mode)==mode)


def directory(path,mode):
    if not os.path.lexists(path):path.mkdir(mode=mode)
    secure(path,True,mode)


def publish(path,raw,mode=0o600,reuse=False):
    secure(path.parent,True)
    if os.path.lexists(path):
        secure(path,False,mode)
        need(reuse and path.read_bytes()==raw);return
    with os.fdopen(os.open(path,os.O_WRONLY|os.O_CREAT|os.O_EXCL|os.O_NOFOLLOW,mode),'wb') as stream:
        stream.write(raw);stream.flush();os.fsync(stream.fileno())
    secure(path,False,mode)


def project(info):
    need(type(info) is dict and info['status']=='INSPECTED')
    counts={key:info[key] for key in ('admin_count','existing_work','collision_count')}
    need(all(type(v) is int and 0<=v<=1000000 for v in counts.values()))
    need(type(info['id1_eligible_unentitled']) is bool and type(info['plans']) is list and len(info['plans'])<=100)
    plans=[];last=0
    for value in info['plans']:
        need(set(value)=={'id','user_type','is_active','download_per_day'} and type(value['id']) is int and last<value['id']<2**63)
        last=value['id']
        need(value['user_type'] in ('INDIVIDUAL','BUSINESS') and type(value['is_active']) is int and value['is_active'] in (0,1)
            and type(value['download_per_day']) is int and -1<=value['download_per_day']<=1000000)
        plans.append(dict(value,fixture_plan_eligible=value['user_type']=='INDIVIDUAL' and value['is_active']==1 and value['download_per_day']>=1))
    conditions=all(v==0 for v in counts.values()) and info['id1_eligible_unentitled'] and any(v['fixture_plan_eligible'] for v in plans)
    reasons=[label for key,label in (('admin_count','EXISTING_ADMIN'),('existing_work','EXISTING_WORK'),('collision_count','IDENTITY_COLLISION')) if counts[key]]
    if not info['id1_eligible_unentitled']:reasons.append('ID1_STATE')
    if not any(v['fixture_plan_eligible'] for v in plans):reasons.append('NO_ELIGIBLE_PLAN')
    return dict(**counts,id1_eligible_unentitled=info['id1_eligible_unentitled'],plans=plans,
        inspected_conditions_satisfied=conditions,readiness='INSPECTED_CONDITIONS_PASS' if conditions else 'HOLD',
        hold_reasons=reasons,apply_authorized=False,date_selected=False)


def run():
    need(os.name=='posix' and os.geteuid()==0 and MODE in ('stage','inspect'))
    os.umask(0o077)
    record_raw=base64.b64decode(RECORD_B64,validate=True)
    need(len(record_raw)<=16384 and hashlib.sha256(record_raw).hexdigest()==RECORD_PIN)
    record=json.loads(record_raw)
    need(set(record)=={'wi','scope','mode','decision_reference','candidate_sha256','boot_id','machine_id_sha256','nonce',
        'issued_at','expires_at','verified_test_semantics_approved','allow_writes'})
    need(record['wi']=='WI-20261023-ATS-011' and record['scope']=='WI011_INSPECT_ONLY_STAGE_PRIVATE_INPUTS_DB_READ_STOP'
        and record['mode']=='inspect' and record['decision_reference']=='CURRENT_EXPLICIT_USER_INSPECT_ONLY_APPROVAL'
        and record['candidate_sha256']==PIN and record['allow_writes'] is False and record['verified_test_semantics_approved'] is True)
    need(type(record['issued_at']) is int and type(record['expires_at']) is int
        and record['issued_at']<=time.time()<record['expires_at']<=record['issued_at']+900)
    need(type(record['nonce']) is str and re.fullmatch('[a-f0-9]{32}',record['nonce']))
    need(Path('/proc/sys/kernel/random/boot_id').read_text().strip()==record['boot_id']
        and hashlib.sha256(Path('/etc/machine-id').read_bytes()).hexdigest()==record['machine_id_sha256'])
    files={name:base64.b64decode(raw,validate=True) for name,raw in FILES.items()}
    need(hashlib.sha256(files['candidate.json']).hexdigest()==PIN)
    manifest=json.loads(files['candidate.json'])
    need(len(manifest['files'])==22 and set(files)==set(manifest['files'])|{'candidate.json'})
    for name,pin in manifest['files'].items():
        need(not Path(name).is_absolute() and '..' not in Path(name).parts and hashlib.sha256(files[name]).hexdigest()==pin)
    directory(ROOT,0o700)
    for path in ROOT.rglob('*'):
        need(not path.is_symlink())
        if path.is_file():need(path.relative_to(ROOT).as_posix() in files)
    for name,raw in sorted(files.items()):
        path=ROOT/name
        if path.parent!=ROOT:directory(path.parent,0o700)
        publish(path,raw,reuse=True)
    sys.path.insert(0,str(ROOT))
    import fixture_cli,fixture_core as core,fixture_native as native
    fixture_cli.verify_candidate(PIN)
    h,n,r,op,source,bounded=native.runtime()
    approved=dict(scope='REQ007_20261023_PHASE1_DB_ONLY',boot_id=record['boot_id'],
        machine_id_sha256=record['machine_id_sha256'],expires_at=record['expires_at'])
    historical=op.identity(h,n,r,approved)
    for path,pin in source.PINS.items():need(h.sha(h.read(Path(path),100*1024*1024))==pin)
    op.cold(h,n,r,historical)
    need(not os.path.lexists('/run/mysqld'))
    directory(EVIDENCE,0o700)
    if MODE=='stage':
        need(PRIVATE_B64 is None)
        need(not os.path.lexists(native.HUMAN) and not os.path.lexists(native.APPROVAL)
            and not os.path.lexists(EVIDENCE/record['nonce'])
            and not os.path.lexists(EVIDENCE/('private-inputs-'+record['nonce'])))
        return dict(status='INSPECT_STAGING_READY',nonce=record['nonce'],candidate_sha256=PIN)
    private=core.inputs(json.loads(base64.b64decode(PRIVATE_B64,validate=True)))
    request=dict(wi=core.WI,scope=core.SCOPE,allow_native=False,nonce=record['nonce'],
        boot_id=record['boot_id'],machine_id_sha256=record['machine_id_sha256'],input_sha256=core.digest(private))
    human=dict(wi=core.WI,scope=core.SCOPE,mode='inspect',plan_sha256=core.digest(request),candidate_sha256=PIN,
        authorization='EXPLICIT_FUTURE_NATIVE_APPROVAL',verified_test_fixture='APPROVED_NO_MAIL_OR_CONSENT_EVENT')
    human_raw=core.canonical(human);human_pin=hashlib.sha256(human_raw).hexdigest()
    approval=dict(wi=core.WI,allow_native=True,scope=core.SCOPE,mode='inspect',issued_at=record['issued_at'],expires_at=record['expires_at'],
        candidate_sha256=PIN,human_evidence_sha256=human_pin,plan_sha256=core.digest(request),
        verified_test_fixture_explicitly_approved=True,no_consent_or_mail_event=True)
    approval_raw=core.canonical(approval);approval_pin=hashlib.sha256(approval_raw).hexdigest()
    core.authorize(approval,request,PIN,human_pin,'inspect')
    imported=EVIDENCE/('private-inputs-'+record['nonce'])
    need(not os.path.lexists(imported));directory(imported,0o700)
    publish(imported/'operator-record.json',record_raw)
    publish(imported/'operator-record.sha256',RECORD_PIN.encode('ascii'))
    for name,value in (('inputs.json',private),('request.json',request)):
        publish(imported/name,core.canonical(value))
    publish(native.HUMAN,human_raw,reuse=True)
    publish(native.APPROVAL,approval_raw,reuse=True)
    # Literal inspect only. The external MA record attests to the actual current
    # human decision; these legacy records only bind it to this private request.
    result=native.native('inspect',request,private,PIN,approval_pin,human_pin)
    allowed={'status','phase','cleanup','native','eligible_plan_ids'}
    need(set(result)<=allowed)
    public={key:result[key] for key in ('status','phase','cleanup','native')}
    if result['status']=='INSPECTION_RECORDED':
        try:public['inspection']=project(h.decode(h.read(EVIDENCE/record['nonce']/'inspection.json',4*1024*1024,True)))
        except BaseException:public.update(status='HOLD',code='INSPECTION_PROJECTION_BOUNDARY')
    return public


if __name__=='__main__':
    try:
        result=run();print(json.dumps(result,sort_keys=True))
        raise SystemExit(0 if result['status'] in ('INSPECT_STAGING_READY','INSPECTION_RECORDED') else 2)
    except SystemExit:raise
    except BaseException:
        print('{"status":"HOLD","code":"INSPECT_STAGE_OR_PROJECTION_BOUNDARY"}')
        raise SystemExit(2)
