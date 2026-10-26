"""Local preparation only; private payloads use the existing MA SSH stdin caller."""
import argparse
import base64
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import stat
import subprocess
import sys
import time
sys.dont_write_bytecode=True
HERE=Path(__file__).resolve().parent
FIXTURE=HERE.parent/'WI20261023-005-account-fixture'
PIN='3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c'
SCOPE='WI011_INSPECT_ONLY_STAGE_PRIVATE_INPUTS_DB_READ_STOP'


def need(ok):
    if not ok:raise ValueError('INSPECT_BOUNDARY')


def canonical(value):return json.dumps(value,sort_keys=True,separators=(',',':')).encode()


def unique(pairs):
    value={}
    for key,item in pairs:
        need(key not in value);value[key]=item
    return value


def private_read(path,limit):
    st=path.lstat()
    need(stat.S_ISREG(st.st_mode) and not path.is_symlink() and st.st_nlink==1 and st.st_size<=limit)
    raw=path.read_bytes();need(len(raw)<=limit)
    return raw


def read_json(path,limit=16384):
    return json.loads(private_read(path,limit),object_pairs_hook=unique)


def pinned_record(directory):
    raw=private_read(directory/'operator-record.json',16384)
    pin=private_read(directory/'operator-record.sha256',128).decode('ascii').strip()
    need(re.fullmatch('[a-f0-9]{64}',pin) and hashlib.sha256(raw).hexdigest()==pin)
    operator_record(json.loads(raw,object_pairs_hook=unique))
    return raw,pin


def fixture_files():
    raw=(FIXTURE/'candidate.json').read_bytes()
    need(hashlib.sha256(raw).hexdigest()==PIN)
    manifest=json.loads(raw)
    need(len(manifest['files'])==22)
    files={'candidate.json':raw}
    need({p.relative_to(FIXTURE).as_posix() for p in FIXTURE.rglob('*') if p.is_file()}==set(manifest['files'])|{'candidate.json'})
    for name,pin in manifest['files'].items():
        path=FIXTURE/name
        need(not path.is_symlink() and '..' not in Path(name).parts and not Path(name).is_absolute())
        data=path.read_bytes()
        need(hashlib.sha256(data).hexdigest()==pin)
        files[name]=data
    return files


def core():
    fixture_files()
    spec=importlib.util.spec_from_file_location('wi011_frozen_core',FIXTURE/'fixture_core.py')
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    return module


def operator_record(value,now=None):
    now=int(time.time()) if now is None else now
    need(type(value) is dict and set(value)=={'wi','scope','mode','decision_reference','candidate_sha256',
        'boot_id','machine_id_sha256','nonce','issued_at','expires_at','verified_test_semantics_approved','allow_writes'})
    need(value['wi']=='WI-20261023-ATS-011' and value['scope']==SCOPE and value['mode']=='inspect'
        and value['decision_reference']=='CURRENT_EXPLICIT_USER_INSPECT_ONLY_APPROVAL'
        and value['candidate_sha256']==PIN and value['verified_test_semantics_approved'] is True and value['allow_writes'] is False)
    need(type(value['boot_id']) is str and re.fullmatch(r'[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}',value['boot_id'])
        and type(value['machine_id_sha256']) is str and re.fullmatch('[a-f0-9]{64}',value['machine_id_sha256'])
        and type(value['nonce']) is str and re.fullmatch('[a-f0-9]{32}',value['nonce']))
    need(type(value['issued_at']) is int and type(value['expires_at']) is int
        and value['issued_at']<=now<value['expires_at']<=value['issued_at']+900)
    return value


def verify_private(directory):
    need(os.name=='nt' and directory.is_absolute())
    result=subprocess.run(['powershell.exe','-NoProfile','-NonInteractive','-ExecutionPolicy','Bypass','-File',
        str(HERE/'capture.ps1'),'-Action','Verify','-PrivateDirectory',str(directory)],capture_output=True,timeout=15)
    need(result.returncode==0 and result.stdout.strip()==b'PRIVATE_ACL_OK')
    for path in directory.iterdir():
        st=path.lstat()
        need(stat.S_ISREG(st.st_mode) and not path.is_symlink() and st.st_nlink==1)


def ready(directory):
    verify_private(directory)
    raw,pin=pinned_record(directory)
    record=json.loads(raw)
    receipt=read_json(directory/'stage-result.json')
    need(receipt=={'status':'INSPECT_STAGING_READY','nonce':record['nonce'],'candidate_sha256':PIN})
    need(not (directory/'inputs.json').exists())
    core()
    return record


def build_payload(mode,record_raw,record_pin,private=None):
    need(mode in ('stage','inspect'))
    need(type(record_raw) is bytes and hashlib.sha256(record_raw).hexdigest()==record_pin)
    operator_record(json.loads(record_raw,object_pairs_hook=unique))
    files=fixture_files()
    if mode=='inspect':core().inputs(private)
    else:need(private is None)
    header='MODE='+repr(mode)+'\nPIN='+repr(PIN)+'\nFILES='+repr({n:base64.b64encode(v).decode() for n,v in files.items()})+'\n'
    header+='RECORD_B64='+repr(base64.b64encode(record_raw).decode())+'\nRECORD_PIN='+repr(record_pin)+'\n'
    header+='PRIVATE_B64='+repr(base64.b64encode(canonical(private)).decode() if private is not None else None)+'\n'
    raw=header.encode()+(HERE/'remote_inspect.py').read_bytes()
    compile(raw,'<private-inspect-payload>','exec')
    return raw


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('action',choices=('validate','ready','stage','inspect'))
    parser.add_argument('--private-directory')
    args=parser.parse_args()
    try:
        if args.action=='validate':
            raw=sys.stdin.buffer.read(8193);need(len(raw)<=8192)
            core().inputs(json.loads(raw,object_pairs_hook=unique))
            print('INPUT_VALID');return 0
        directory=Path(args.private_directory)
        if args.action=='ready':ready(directory);print('CAPTURE_READY');return 0
        verify_private(directory)
        record_raw,record_pin=pinned_record(directory)
        private=read_json(directory/'inputs.json',8192) if args.action=='inspect' else None
        payload=build_payload(args.action,record_raw,record_pin,private)
        destination=directory/(args.action+'-payload.py')
        with destination.open('xb') as stream:stream.write(payload);stream.flush();os.fsync(stream.fileno())
        with destination.with_suffix('.sha256').open('xb') as stream:
            stream.write(hashlib.sha256(payload).hexdigest().encode());stream.flush();os.fsync(stream.fileno())
        verify_private(directory)
        print('PRIVATE_PAYLOAD_READY');return 0
    except BaseException:
        print('HOLD_INSPECT_BOUNDARY');return 2


if __name__=='__main__':raise SystemExit(main())
