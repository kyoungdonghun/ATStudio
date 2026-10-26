"""Local-only builder for reviewed SSH stdin; no network or private file access."""
import argparse
import base64
import hashlib
import json
from pathlib import Path
import re
import time

HERE=Path(__file__).resolve().parent
BOOTSTRAP=r'''
import base64,hashlib,json,os,stat,sys,time,types
from pathlib import Path
sys.dont_write_bytecode=True
def need(ok):
    if not ok: raise ValueError('BOOTSTRAP_HOLD')
def secure_read(path,limit):
    for parent in path.parents:
        st=parent.lstat()
        need(stat.S_ISDIR(st.st_mode) and st.st_uid==0 and not st.st_mode&0o022)
    st=path.lstat()
    need(stat.S_ISREG(st.st_mode) and st.st_uid==0 and st.st_nlink==1 and not st.st_mode&0o022 and st.st_size<=limit)
    with os.fdopen(os.open(path,os.O_RDONLY|os.O_NOFOLLOW),'rb') as stream:
        opened=os.fstat(stream.fileno())
        need((st.st_dev,st.st_ino)==(opened.st_dev,opened.st_ino))
        raw=stream.read(limit+1)
        after=os.fstat(stream.fileno())
    need(len(raw)<=limit and (st.st_size,st.st_mtime_ns,st.st_ctime_ns)==(after.st_size,after.st_mtime_ns,after.st_ctime_ns))
    return raw
try:
    need(os.name=='posix' and os.geteuid()==0)
    root=Path('/opt/atstudio/wi067-tools')
    freeze=secure_read(root/'binding/freeze.json',262144)
    need(hashlib.sha256(freeze).hexdigest()=='3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d')
    manifest=json.loads(freeze)
    actual={}
    bundle=bytearray()
    for path in sorted(root.rglob('*')):
        need(not path.is_symlink())
        if path.is_file() and path!=root/'binding/freeze.json':
            name=path.relative_to(root).as_posix()
            raw=secure_read(path,2*1024*1024)
            actual[name]=hashlib.sha256(raw).hexdigest()
            bundle.extend(name.encode()+b'\0'+raw)
    need(actual==manifest['files'] and hashlib.sha256(bundle).hexdigest()==manifest['bundle_sha256'])
    decoded={key:base64.b64decode(value['body'],validate=True) for key,value in SOURCES.items()}
    need(all(hashlib.sha256(raw).hexdigest()==SOURCES[key]['sha256'] for key,raw in decoded.items()))
    sys.path[:0]=[str(root/'binding'),str(root)]
    import host as h,native as n,release as r
    for name in ('inventory','backup_evidence','account_operator'):
        mod=types.ModuleType(name)
        sys.modules[name]=mod
        exec(compile(decoded[name],'<reviewed-'+name+'>','exec'),mod.__dict__)
    op=sys.modules['account_operator']
    boot=Path('/proc/sys/kernel/random/boot_id').read_text().strip()
    machine=h.sha(h.read(Path('/etc/machine-id')))
    if EXPECTED_BOOT is not None:
        need(boot==EXPECTED_BOOT and machine==EXPECTED_MACHINE)
    approved=dict(scope='REQ007_20261024_READONLY_ACCOUNT_GRANT_BACKUP_EVIDENCE',boot_id=boot,
        machine_id_sha256=machine,expires_at=EXPIRES_AT)
    result=op.run(h,n,r,decoded['source_operator'],decoded['cold_capture'],decoded['diagnostic'],approved,MODE=='probe')
    result['authorization_receipt']=dict(approved,reference='WI-20261024-ATS-008_CURRENT_EXPLICIT_READONLY_APPROVAL')
    if MODE=='probe' and result['status']=='PREFLIGHT_PASS':
        result['target']={'boot_id':boot,'machine_id_sha256':machine}
    print(json.dumps(result,sort_keys=True))
    raise SystemExit(0 if result['status'] in ('PREFLIGHT_PASS','INVENTORY_OBSERVED') else 2)
except SystemExit:
    raise
except BaseException:
    print('{"status":"HOLD","phase":"BOOTSTRAP","code":"BOOTSTRAP_HOLD"}')
    raise SystemExit(2)
'''


def payload(mode,boot=None,machine=None,expires_at=None):
    if mode not in ('probe','inventory') or (mode=='inventory' and (not boot or not machine)):
        raise ValueError('EXACT_TARGET_REQUIRED')
    if type(expires_at) is not int or not 0 < expires_at-time.time() <= 900:
        raise ValueError('FRESH_ABSOLUTE_EXPIRY_REQUIRED')
    if (boot is not None or machine is not None) and not (
        type(boot) is str and re.fullmatch(r'[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}',boot)
        and type(machine) is str and re.fullmatch(r'[a-f0-9]{64}',machine)):
        raise ValueError('EXACT_TARGET_REQUIRED')
    paths=dict(inventory=HERE/'inventory.py',backup_evidence=HERE/'backup_evidence.py',account_operator=HERE/'account_operator.py',
        source_operator=HERE.parent/'WI067-ledger-diagnostic-v2/source_operator.py',
        diagnostic=HERE.parent/'WI067-ledger-diagnostic-v2/diagnostic.py',
        cold_capture=HERE.parent/'WI069-backup/cold_capture.py')
    sources={}
    for key,path in paths.items():
        raw=path.read_bytes()
        sources[key]=dict(sha256=hashlib.sha256(raw).hexdigest(),body=base64.b64encode(raw).decode())
    text='SOURCES='+repr(sources)+'\nMODE='+repr(mode)+'\nEXPECTED_BOOT='+repr(boot)+'\nEXPECTED_MACHINE='+repr(machine)+'\nEXPIRES_AT='+repr(expires_at)+'\n'+BOOTSTRAP
    compile(text,'<reviewed-payload>','exec')
    return text.encode()


if __name__=='__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('mode',choices=('probe','inventory'))
    parser.add_argument('--boot')
    parser.add_argument('--machine')
    parser.add_argument('--expires-at',required=True,type=int)
    parser.add_argument('--output',required=True)
    args=parser.parse_args()
    raw=payload(args.mode,args.boot,args.machine,args.expires_at)
    with Path(args.output).open('xb') as stream:stream.write(raw)
    print(json.dumps(dict(status='LOCAL_PAYLOAD_BUILT',bytes=len(raw),sha256=hashlib.sha256(raw).hexdigest())))
