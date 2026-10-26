"""Default inert entry; native authority is supplied later by MA, never fabricated."""
import argparse
import json
import os
from pathlib import Path
import signal
import sys
import backup_plan as contract

HERE=Path(__file__).resolve().parent


def verify_candidate(expected):
    raw=(HERE/'candidate.json').read_bytes()
    contract.need(contract.pin(expected) and contract.sha(raw)==expected,'CANDIDATE_PIN')
    manifest=contract.decode(raw)
    actual={p.relative_to(HERE).as_posix():contract.sha(p.read_bytes()) for p in HERE.rglob('*')
        if p.is_file() and p!=HERE/'candidate.json' and 'authority' not in p.relative_to(HERE).parts}
    contract.need(not any(p.is_symlink() for p in HERE.rglob('*')) and actual==manifest['files'],'CANDIDATE_TREE')
    return expected


def dispatch(args):
    if args.action=='plan':return contract.plan()
    contract.need(args.invoke and os.name=='posix' and os.geteuid()==0,'EXPLICIT_NATIVE_DISPATCH_REQUIRED')
    candidate=verify_candidate(args.candidate_sha256)
    sys.path.insert(0,str(HERE/'lib'))
    import bootstrap as boot
    boot.secure(HERE,directory=True,private=True)
    for path in HERE.rglob('*'):
        boot.secure(path,directory=path.is_dir(),private=True)
    approval=contract.decode(boot.read_file(Path(args.approval),262144,private=True))
    decision=boot.read_file(Path(args.decision),262144,private=True)
    cost=boot.read_file(Path(args.cost),262144,private=True) if args.cost else b''
    source=boot.read_file(Path(args.cost_source),262144,private=True) if args.cost_source else b''
    verified=contract.authority(approval,decision,args.decision_sha256,cost,source,candidate,args.action)
    import adapter
    def interrupted(*_):raise RuntimeError('INTERRUPTED')
    signal.signal(signal.SIGTERM,interrupted);signal.signal(signal.SIGHUP,interrupted)
    if args.action=='inventory':
        _,baseline=adapter.inventory(approval)
        # MA keeps this transport response private; no metadata file bodies are exposed.
        return dict(status='CURRENT_COLD_INVENTORY_OBSERVED',inventory_sha256=baseline['inventory_sha256'],
            units_sha256=baseline['units_sha256'],source_bytes=baseline['source_bytes'],
            members=len(baseline['members']),backup='NOT_RUN',restore='NOT_RUN',business_mutation='NOT_RUN')
    args.native_dispatched=True
    return adapter.execute(approval,verified)


def main(argv=None):
    parser=argparse.ArgumentParser()
    parser.add_argument('action',choices=('plan','inventory','capture'),nargs='?',default='plan')
    parser.add_argument('--invoke',action='store_true')
    for name in ('candidate-sha256','approval','decision','decision-sha256','cost','cost-source'):
        parser.add_argument('--'+name)
    args=parser.parse_args(argv)
    args.native_dispatched=False
    try:
        result=dispatch(args)
        print(json.dumps(result,sort_keys=True))
        return 0 if result['status'] in ('LOCAL_PREPARATION_ONLY','CURRENT_COLD_INVENTORY_OBSERVED','CURRENT_CAPTURE_ISOLATED_VERIFY_PASS') else 2
    except BaseException:
        print(json.dumps(dict(status='HOLD',code='NATIVE_AUTHORITY_OR_DISPATCH_HOLD',automatic_retry=False,
            snapshot_state='UNKNOWN_POSSIBLE' if args.native_dispatched else 'NOT_ATTEMPTED')))
        return 2


if __name__=='__main__':raise SystemExit(main())
