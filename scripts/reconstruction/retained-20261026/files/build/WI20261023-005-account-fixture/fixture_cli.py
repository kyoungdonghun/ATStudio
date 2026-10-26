"""Default local-only CLI. Native modes require independently supplied exact pins."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import stat
import sys
sys.dont_write_bytecode=True
HERE=Path(__file__).resolve().parent


def unique(pairs):
    result={}
    for key,value in pairs:
        if key in result:raise ValueError('DUPLICATE_JSON')
        result[key]=value
    return result


def read_json(path,private=False):
    target=Path(path)
    if private:
        for parent in target.parents:
            s=parent.lstat()
            if not stat.S_ISDIR(s.st_mode) or s.st_uid!=0 or s.st_mode&0o022:raise ValueError('PRIVATE_PATH')
        s=target.lstat()
        if not stat.S_ISREG(s.st_mode) or s.st_uid!=0 or stat.S_IMODE(s.st_mode)!=0o600 or s.st_nlink!=1:raise ValueError('PRIVATE_FILE')
    raw=target.read_bytes()
    if len(raw)>4*1024*1024:raise ValueError('FILE_BOUND')
    return json.loads(raw,object_pairs_hook=unique)


def write_new(path,value):
    raw=(json.dumps(value,indent=2)+'\n').encode()
    fd=os.open(path,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
    with os.fdopen(fd,'wb') as f:f.write(raw);f.flush();os.fsync(f.fileno())


def verify_candidate(pin):
    manifest=HERE/'candidate.json'
    if hashlib.sha256(manifest.read_bytes()).hexdigest()!=pin:raise ValueError('CANDIDATE_PIN')
    value=read_json(manifest)
    actual=set()
    for path in HERE.rglob('*'):
        if path.is_symlink():raise ValueError('CANDIDATE_LINK')
        if path.is_file() and path!=manifest:actual.add(path.relative_to(HERE).as_posix())
    if actual!=set(value['files']):raise ValueError('CANDIDATE_INVENTORY')
    for name,expected in value['files'].items():
        path=HERE/name
        if path.is_symlink() or '..' in Path(name).parts or Path(name).is_absolute():raise ValueError('CANDIDATE_PATH')
        for parent in path.parents:
            st=parent.lstat()
            if st.st_uid!=0 or st.st_mode&0o022 or not stat.S_ISDIR(st.st_mode):raise ValueError('CANDIDATE_OWNER')
        st=path.lstat()
        if not stat.S_ISREG(st.st_mode) or st.st_uid!=0 or st.st_mode&0o022 or st.st_nlink!=1:raise ValueError('CANDIDATE_OWNER')
        if hashlib.sha256(path.read_bytes()).hexdigest()!=expected:raise ValueError('CANDIDATE_BYTES')


def main(argv=None):
    parser=argparse.ArgumentParser()
    parser.add_argument('mode',nargs='?',default='status',choices=('status','request','plan','inspect','apply','reconcile'))
    for arg in ('inputs','inspection','nonce','day','boot','machine','output','proposal','candidate-pin','approval-pin','human-pin'):
        parser.add_argument('--'+arg)
    parser.add_argument('--plan-id',type=int)
    args=parser.parse_args(argv)
    try:
        if args.mode=='status':
            print('{"status":"LOCAL_PREPARATION_ONLY","native_authorized":false}')
            return 0
        if args.mode in ('request','plan'):
            sys.path.insert(0,str(HERE))
            import fixture_core as core
            private=core.inputs(read_json(args.inputs))
            if args.mode=='request':
                core.need(core.pin(args.machine) and type(args.boot) is str and len(args.boot)==36
                    and type(args.nonce) is str and __import__('re').fullmatch('[a-f0-9]{32}',args.nonce))
                result=dict(wi=core.WI,scope=core.SCOPE,allow_native=False,nonce=args.nonce,day=core.day_now(),
                    boot_id=args.boot,machine_id_sha256=args.machine,input_sha256=core.digest(private))
            else:result=core.plan(read_json(args.inspection),private,args.plan_id,args.day,args.nonce)
            write_new(args.output,result)
            print('{"status":"LOCAL_PLAN_WRITTEN","native_authorized":false}')
            return 0
        if os.name!='posix' or os.geteuid()!=0:raise ValueError('NATIVE_ROOT')
        if not all((args.proposal,args.inputs,args.candidate_pin,args.approval_pin,args.human_pin)):raise ValueError('EXTERNAL_APPROVAL_REQUIRED')
        verify_candidate(args.candidate_pin)
        sys.path.insert(0,str(HERE))
        import fixture_native
        value=fixture_native.native(args.mode,read_json(args.proposal,True),read_json(args.inputs,True),
            args.candidate_pin,args.approval_pin,args.human_pin)
        print(json.dumps(value,sort_keys=True))
        return 0 if value['status'] in ('INSPECTION_RECORDED','APPLIED','APPLIED_CONFIRMED','NOT_APPLIED_CONFIRMED') else 2
    except BaseException:
        print('{"status":"HOLD","code":"FIXTURE_GATE","native_authorized":false}')
        return 2


if __name__=='__main__':raise SystemExit(main())
