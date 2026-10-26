"""Local private builder; no network calls or account secret access."""
import argparse,base64,hashlib,importlib.util,json,os,sys
from pathlib import Path
sys.dont_write_bytecode=True
HERE=Path(__file__).resolve().parent
FROZEN=HERE.parent/'WI20261023-005-account-fixture'
PIN='3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c'

def load(name,path):
    if path.name=='inspect_tools.py':
        manifest=(path.parent/'candidate.json').read_bytes()
        assert hashlib.sha256(manifest).hexdigest()=='21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245'
        for n,pin in json.loads(manifest)['files'].items():
            assert hashlib.sha256((path.parent/n).read_bytes()).hexdigest()==pin
    s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m

BOOT='''import base64,hashlib,json,os,sys,types
from pathlib import Path
sys.dont_write_bytecode=True
def need(v):
    if not v:raise ValueError('BOOTSTRAP')
try:
    need(os.name=='posix' and os.geteuid()==0)
    os.umask(0o077)
    raw=base64.b64decode(MANIFEST,validate=True)
    need(hashlib.sha256(raw).hexdigest()==CANDIDATE)
    manifest=json.loads(raw)
    code={n:base64.b64decode(v,validate=True) for n,v in SOURCES.items()}
    need(set(code)==set(manifest['files']))
    for n,v in code.items():need(hashlib.sha256(v).hexdigest()==manifest['files'][n])
    old={n:base64.b64decode(v,validate=True) for n,v in FROZEN_FILES.items()}
    need(hashlib.sha256(old['candidate.json']).hexdigest()==FROZEN_PIN)
    old_manifest=json.loads(old['candidate.json'])
    need(set(old)==set(old_manifest['files'])|{'candidate.json'})
    for n,v in old_manifest['files'].items():need(hashlib.sha256(old[n]).hexdigest()==v)
    helper=base64.b64decode(IO_HELPER,validate=True)
    need(hashlib.sha256(helper).hexdigest()=='1df52a75453c98423e2a953690d5aa9362d32e9275954b03590d34cb62a35d66')
    io=types.ModuleType('verified_io');exec(compile(helper,'<verified-io>','exec'),io.__dict__)
    record_raw=base64.b64decode(RECORD,validate=True)
    need(hashlib.sha256(record_raw).hexdigest()==RECORD_PIN)
    p=json.loads(record_raw)
    need(Path('/proc/sys/kernel/random/boot_id').read_text().strip()==p['boot_id'] and hashlib.sha256(Path('/etc/machine-id').read_bytes()).hexdigest()==p['machine_id_sha256'])
    root=Path('/opt/atstudio/wi20261023-005-fixture')
    io.directory(root,0o700)
    for path in root.rglob('*'):
        need(not path.is_symlink())
        if path.is_file():need(path.relative_to(root).as_posix() in old)
    for n,v in sorted(old.items()):
        need(not Path(n).is_absolute() and '..' not in Path(n).parts)
        if (root/n).parent!=root:io.directory((root/n).parent,0o700)
        io.publish(root/n,v,reuse=True)
    sys.path.insert(0,str(root))
    import fixture_core
    tx=types.ModuleType('transaction');sys.modules['transaction']=tx
    exec(compile(code['transaction.py'],'<stage2-transaction>','exec'),tx.__dict__)
    tx.validate(p,p['mode'],CANDIDATE)
    native=types.ModuleType('stage2_native')
    exec(compile(code['native_adapter.py'],'<stage2-native>','exec'),native.__dict__)
    io.directory(native.EVIDENCE,0o700)
    result=native.native(p['mode'],p,CANDIDATE)
except BaseException:
    result=dict(status='HOLD',phase='PRECHECK',code='STAGE_TWO_BOUNDARY')
print(json.dumps(result,sort_keys=True))
raise SystemExit(0 if result['status'] in ('INSPECTION_RECORDED','APPLIED','APPLIED_CONFIRMED','NOT_APPLIED_CONFIRMED') else 2)
'''

def build(record_raw):
    old=load('frozen_private',HERE.parent/'WI20261023-011-fixture-inspect/inspect_tools.py')
    files=old.fixture_files()
    assert hashlib.sha256(files['candidate.json']).hexdigest()==PIN
    manifest=(HERE/'candidate.json').read_bytes();candidate=hashlib.sha256(manifest).hexdigest()
    sources={n:(HERE/n).read_bytes() for n in json.loads(manifest)['files']}
    assert all(hashlib.sha256(sources[n]).hexdigest()==v for n,v in json.loads(manifest)['files'].items())
    sys.path.insert(0,str(FROZEN))
    tx=load('stage2_contract',HERE/'transaction.py')
    p=json.loads(record_raw,object_pairs_hook=old.unique);tx.validate(p,p['mode'],candidate)
    b64=lambda b:base64.b64encode(b).decode('ascii')
    values=dict(MANIFEST=b64(manifest),CANDIDATE=candidate,SOURCES={n:b64(v) for n,v in sources.items()},
        FROZEN_PIN=PIN,FROZEN_FILES={n:b64(v) for n,v in files.items()},
        IO_HELPER=b64((HERE.parent/'WI20261023-011-fixture-inspect/remote_inspect.py').read_bytes()),
        RECORD=b64(record_raw),RECORD_PIN=hashlib.sha256(record_raw).hexdigest())
    payload=(''.join(k+'='+repr(v)+'\n' for k,v in values.items())+BOOT).encode()
    compile(payload,'<stage-two-payload>','exec')
    return payload

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--private-directory',required=True);a=parser.parse_args()
    try:
        old=load('frozen_acl',HERE.parent/'WI20261023-011-fixture-inspect/inspect_tools.py')
        d=Path(a.private_directory);old.verify_private(d)
        raw=old.private_read(d/'operator-record.json',16384)
        payload=build(raw)
        for name,value in (('payload.py',payload),('payload.sha256',hashlib.sha256(payload).hexdigest().encode())):
            with (d/name).open('xb') as f:f.write(value);f.flush();os.fsync(f.fileno())
        old.verify_private(d);print('PRIVATE_STAGE_TWO_PAYLOAD_READY');return 0
    except BaseException:print('HOLD_PRIVATE_PAYLOAD_BOUNDARY');return 2

if __name__=='__main__':raise SystemExit(main())
