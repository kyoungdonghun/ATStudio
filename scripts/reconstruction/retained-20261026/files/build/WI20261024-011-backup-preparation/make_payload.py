"""Local serializer for the existing strict MA SSH stdin transport. No network."""
import argparse
import base64
import json
from pathlib import Path
import backup_plan as contract

HERE=Path(__file__).resolve().parent
BOOTSTRAP=r'''
import base64,hashlib,json,os,stat,sys,types
from pathlib import Path
sys.dont_write_bytecode=True
def need(ok):
    if not ok:raise ValueError('STAGE_HOLD')
def read(path,limit):
    for parent in path.parents:
        st=parent.lstat();need(stat.S_ISDIR(st.st_mode) and st.st_uid==0 and not st.st_mode&0o022)
    st=path.lstat();need(stat.S_ISREG(st.st_mode) and st.st_uid==0 and st.st_nlink==1 and not st.st_mode&0o022 and st.st_size<=limit)
    with os.fdopen(os.open(path,os.O_RDONLY|os.O_NOFOLLOW),'rb') as f:
        before=os.fstat(f.fileno());raw=f.read(limit+1);after=os.fstat(f.fileno())
    need((st.st_dev,st.st_ino)==(before.st_dev,before.st_ino) and len(raw)<=limit
        and (before.st_size,before.st_mtime_ns,before.st_ctime_ns)==(after.st_size,after.st_mtime_ns,after.st_ctime_ns))
    return raw
try:
    need(os.name=='posix' and os.geteuid()==0);os.umask(0o077)
    decoded={k:base64.b64decode(v,validate=True) for k,v in FILES.items()}
    need(hashlib.sha256(decoded['candidate.json']).hexdigest()==CANDIDATE)
    manifest=json.loads(decoded['candidate.json'])
    need(set(decoded)==set(manifest['files'])|{'candidate.json'})
    need(all(hashlib.sha256(decoded[k]).hexdigest()==v for k,v in manifest['files'].items()))
    mod=types.ModuleType('backup_plan');exec(compile(decoded['backup_plan.py'],'<pinned-contract>','exec'),mod.__dict__)
    private={k:base64.b64decode(v,validate=True) for k,v in PRIVATE.items()}
    approval=mod.decode(private['approval.json'])
    mod.authority(approval,private['decision.json'],DECISION_PIN,private.get('cost.json',b''),private.get('cost-source.json',b''),CANDIDATE,ACTION)
    need(Path('/proc/sys/kernel/random/boot_id').read_text().strip()==approval['boot_id'])
    need(hashlib.sha256(read(Path('/etc/machine-id'),4096)).hexdigest()==approval['machine_id_sha256'])
    root=Path('/opt/atstudio')/('wi011-backup-'+approval['run_id']+'-'+ACTION)
    for parent in root.parents:
        st=parent.lstat();need(stat.S_ISDIR(st.st_mode) and st.st_uid==0 and not st.st_mode&0o022)
    need(not os.path.lexists(root));root.mkdir(mode=0o700)
    for name,raw in sorted(decoded.items()):
        rel=Path(name);need(not rel.is_absolute() and '..' not in rel.parts and '\\' not in name)
        path=root/rel
        for parent in reversed(path.relative_to(root).parents):
            if str(parent)!='.':(root/parent).mkdir(mode=0o700,exist_ok=True)
        with os.fdopen(os.open(path,os.O_WRONLY|os.O_CREAT|os.O_EXCL|os.O_NOFOLLOW,0o600),'wb') as f:
            f.write(raw);f.flush();os.fsync(f.fileno())
    auth=root/'authority';auth.mkdir(mode=0o700)
    for name,raw in private.items():
        need(name in ('approval.json','decision.json','cost.json','cost-source.json'))
        with os.fdopen(os.open(auth/name,os.O_WRONLY|os.O_CREAT|os.O_EXCL|os.O_NOFOLLOW,0o600),'wb') as f:
            f.write(raw);f.flush();os.fsync(f.fileno())
    sys.path.insert(0,str(root));import entry
    argv=[ACTION,'--invoke','--candidate-sha256',CANDIDATE,'--approval',str(auth/'approval.json'),
        '--decision',str(auth/'decision.json'),'--decision-sha256',DECISION_PIN]
    if ACTION=='capture':argv+=['--cost',str(auth/'cost.json'),'--cost-source',str(auth/'cost-source.json')]
    raise SystemExit(entry.main(argv))
except SystemExit:raise
except BaseException:
    print('{"status":"HOLD","code":"BOOTSTRAP_HOLD","automatic_retry":false}')
    raise SystemExit(2)
'''


def payload(action,candidate,approval_raw,decision_raw,decision_pin,cost_raw=b'',cost_source_raw=b''):
    manifest_raw=(HERE/'candidate.json').read_bytes()
    contract.need(contract.sha(manifest_raw)==candidate,'CANDIDATE_PIN')
    manifest=contract.decode(manifest_raw)
    files={'candidate.json':base64.b64encode(manifest_raw).decode()}
    for name,pin in manifest['files'].items():
        path=HERE/name;raw=path.read_bytes()
        contract.need(not path.is_symlink() and contract.sha(raw)==pin,'CANDIDATE_MEMBER')
        files[name]=base64.b64encode(raw).decode()
    approval=contract.decode(approval_raw)
    contract.authority(approval,decision_raw,decision_pin,cost_raw,cost_source_raw,candidate,action)
    private={'approval.json':approval_raw,'decision.json':decision_raw}
    if action=='capture':private.update({'cost.json':cost_raw,'cost-source.json':cost_source_raw})
    text='FILES='+repr(files)+'\nPRIVATE='+repr({k:base64.b64encode(v).decode() for k,v in private.items()})
    text+='\nCANDIDATE='+repr(candidate)+'\nACTION='+repr(action)+'\nDECISION_PIN='+repr(decision_pin)+'\n'+BOOTSTRAP
    raw=text.encode();contract.need(len(raw)<=2*1024**2,'FRAME_LIMIT');compile(raw,'<native-stdin>','exec')
    return raw


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('action',choices=('inventory','capture'))
    for name in ('candidate-sha256','approval','decision','decision-sha256','output'):p.add_argument('--'+name,required=True)
    for name in ('cost','cost-source'):p.add_argument('--'+name)
    a=p.parse_args()
    try:
        raw=payload(a.action,a.candidate_sha256,Path(a.approval).read_bytes(),Path(a.decision).read_bytes(),a.decision_sha256,
            Path(a.cost).read_bytes() if a.cost else b'',Path(a.cost_source).read_bytes() if a.cost_source else b'')
        with Path(a.output).open('xb') as f:f.write(raw)
        print('{"status":"PRIVATE_PAYLOAD_BUILT","network":"NOT_RUN"}')
    except BaseException:
        print('{"status":"HOLD","code":"LOCAL_PAYLOAD_GATE"}');raise SystemExit(2)
