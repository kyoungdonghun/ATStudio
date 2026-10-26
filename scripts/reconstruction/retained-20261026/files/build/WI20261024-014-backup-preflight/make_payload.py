"""Local serializer; MA alone transmits the result over reviewed SSH stdin."""
import argparse
import base64
import hashlib
import json
from pathlib import Path
import re
import sys
import time

HERE=Path(__file__).resolve().parent
OLD=HERE.parent/'WI20261024-008-readonly-evidence'
DEPENDENCIES={
    'inventory':(OLD/'inventory.py','fafa8059c6988aae21d0e135ce7abf44dbee5255344ed878f5f6b5536806f37d'),
    'backup_evidence':(OLD/'backup_evidence.py','1b1f6a8d9815f3a5003460600355fbb1655e8f71f4a00ef578dfad3c6574a754'),
    'account_operator':(OLD/'account_operator.py','d35dbf67103e3f0b913a6af292ff899971389e452da32547bfc91ae53b8c1e45'),
    'bootstrap':(HERE.parent/'WI20261024-011-backup-preparation/lib/bootstrap.py','f2f7c90c609a3a0689153c3bb40201cec684974e53ae6d6990d47eec86f98fe8')}
BUILDER_PIN='9d4797bb73f3a13ec7e5fc56781aeaf0e09d7b937a05c13263df2d13170dfb1c'


def need(ok):
    if not ok:raise ValueError('LOCAL_PREFLIGHT_HOLD')


def sha(raw):return hashlib.sha256(raw).hexdigest()


def read_pinned(path,pin):
    raw=path.read_bytes();need(sha(raw)==pin);return raw


def own_candidate():
    value=json.loads((HERE/'candidate.json').read_bytes())
    actual={p.name:sha(p.read_bytes()) for p in HERE.iterdir() if p.is_file() and p.name!='candidate.json'}
    need(actual==value['files'] and sha(json.dumps(actual,sort_keys=True,separators=(',',':')).encode())==value['bundle_sha256'])
    return actual


def transport_bootstrap():
    module={'__name__':'retained_local_builder','__file__':str(OLD/'make_payload.py')}
    exec(compile(read_pinned(OLD/'make_payload.py',BUILDER_PIN),'<pinned-builder>','exec'),module)
    text=module['BOOTSTRAP']
    changes={
        "('inventory','backup_evidence','account_operator')":"('inventory','backup_evidence','account_operator','bootstrap','preflight')",
        "scope='REQ007_20261024_READONLY_ACCOUNT_GRANT_BACKUP_EVIDENCE'":"scope='REQ007_20261024_READONLY_BACKUP_REFERENCE_PREFLIGHT'",
        "result=op.run(h,n,r,decoded['source_operator'],decoded['cold_capture'],decoded['diagnostic'],approved,MODE=='probe')":"result=sys.modules['preflight'].run(h,n,r,op,sys.modules['bootstrap'],approved,MODE,EXPECTED_ESCROW_SHA256)",
        "    result['authorization_receipt']=dict(approved,reference='WI-20261024-ATS-008_CURRENT_EXPLICIT_READONLY_APPROVAL')\n":"",
        "('PREFLIGHT_PASS','INVENTORY_OBSERVED')":"('PREFLIGHT_PASS','REFERENCES_OBSERVED')"}
    for old,new in changes.items():
        need(text.count(old)==1);text=text.replace(old,new)
    return text


def payload(mode,boot=None,machine=None,expires_at=None,expected_escrow_sha256=None):
    need(mode in ('probe','inspect') and (mode!='inspect' or (boot is not None and machine is not None)))
    need(type(expires_at) is int and 0<expires_at-time.time()<=600)
    if boot is not None or machine is not None:
        need(type(boot) is str and re.fullmatch(r'[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}',boot)
            and type(machine) is str and re.fullmatch('[a-f0-9]{64}',machine))
    need(expected_escrow_sha256 is None or (mode=='inspect' and type(expected_escrow_sha256) is str
        and re.fullmatch('[a-f0-9]{64}',expected_escrow_sha256)))
    files=own_candidate()
    raw_sources={key:read_pinned(path,pin) for key,(path,pin) in DEPENDENCIES.items()}
    raw_sources['preflight']=read_pinned(HERE/'preflight.py',files['preflight.py'])
    sources={key:dict(sha256=sha(raw),body=base64.b64encode(raw).decode()) for key,raw in raw_sources.items()}
    values=dict(SOURCES=sources,MODE=mode,EXPECTED_BOOT=boot,EXPECTED_MACHINE=machine,
        EXPIRES_AT=expires_at,EXPECTED_ESCROW_SHA256=expected_escrow_sha256)
    text=''.join(key+'='+repr(value)+'\n' for key,value in values.items())+transport_bootstrap()
    need(len(text.encode())<=262144);compile(text,'<reviewed-readonly-payload>','exec')
    return text.encode()


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('mode',choices=('probe','inspect'))
    parser.add_argument('--boot');parser.add_argument('--machine')
    parser.add_argument('--expires-at',required=True,type=int)
    parser.add_argument('--escrow-pin-stdin',action='store_true')
    parser.add_argument('--output',required=True)
    args=parser.parse_args()
    try:
        expected=None
        if args.escrow_pin_stdin:
            raw=sys.stdin.buffer.read(66);need(len(raw) in (64,65) and (len(raw)==64 or raw[-1:]==b'\n'))
            expected=raw.rstrip(b'\n').decode('ascii')
        raw=payload(args.mode,args.boot,args.machine,args.expires_at,expected)
        with Path(args.output).open('xb') as stream:stream.write(raw)
        print('{"status":"LOCAL_PAYLOAD_BUILT"}')
        return 0
    except BaseException:
        print('{"status":"HOLD","code":"LOCAL_PREFLIGHT_HOLD"}')
        return 2


if __name__=='__main__':raise SystemExit(main())
