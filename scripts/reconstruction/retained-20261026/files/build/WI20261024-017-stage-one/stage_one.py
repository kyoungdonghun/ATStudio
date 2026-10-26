"""Local-only assembly around frozen WI011. Actual records are supplied by MA."""
import argparse
import base64
import hashlib
import importlib.util
import json
from pathlib import Path
import sys
import time

HERE=Path(__file__).resolve().parent
BACKUP=HERE.parent/'WI20261024-011-backup-preparation'
BACKUP_PIN='158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839'
SCOPE='STAGE_ONE_CURRENT_BACKUP_COST_METADATA'


def need(ok):
    if not ok:raise ValueError('STAGE_ONE_HOLD')


def canonical(value):return json.dumps(value,sort_keys=True,separators=(',',':')).encode()
def sha(raw):return hashlib.sha256(raw).hexdigest()


def verify_tree(root,expected):
    raw=(root/'candidate.json').read_bytes();need(sha(raw)==expected)
    value=json.loads(raw)
    actual={p.relative_to(root).as_posix():sha(p.read_bytes()) for p in root.rglob('*')
        if p.is_file() and p!=root/'candidate.json'}
    need(not any(p.is_symlink() for p in root.rglob('*')) and actual==value['files'])
    return value


def load_backup():
    verify_tree(BACKUP,BACKUP_PIN)
    sys.path.insert(0,str(BACKUP))
    for name in ('backup_plan','wi017_retained_serializer'):
        filename='backup_plan.py' if name=='backup_plan' else 'make_payload.py'
        spec=importlib.util.spec_from_file_location(name,BACKUP/filename)
        module=importlib.util.module_from_spec(spec);sys.modules[name]=module;spec.loader.exec_module(module)
    return sys.modules['wi017_retained_serializer']


def scope_record(raw,pin,candidate,approval_raw,action,cost_input_raw=b'',now=None):
    now=int(time.time()) if now is None else now
    need(sha(raw)==pin)
    contract=sys.modules['backup_plan'];value=contract.decode(raw);approval=contract.decode(approval_raw)
    need(value==dict(wi='WI-20261024-ATS-017',source='CURRENT_EXPLICIT_USER_DECISION_RECORDED_BY_MA',
        scope=SCOPE,action=action,candidate_sha256=candidate,backup_candidate_sha256=BACKUP_PIN,
        approval_sha256=sha(approval_raw),issued_at=approval['issued_at'],expires_at=approval['expires_at'],
        cost_input_sha256=sha(cost_input_raw) if action=='capture' else None,
        no_business_mutation=True,no_app=True,no_additional_provider_operation=True))
    need(approval['issued_at']<=now<approval['expires_at']<=approval['issued_at']+1800)
    return value


INJECTION=r'''
    extra={key:base64.b64decode(value,validate=True) for key,value in EXTRA.items()}
    need(all(hashlib.sha256(value).hexdigest()==EXTRA_PINS[key] for key,value in extra.items()))
    need(hashlib.sha256(extra['candidate.json']).hexdigest()==STAGE_ONE_PIN)
    extension=json.loads(extra['candidate.json'])
    need(all(hashlib.sha256(extra[name]).hexdigest()==extension['files'][name]
        for name in ('cost_metadata.py','cost_bound.py')))
    scope=mod.decode(extra['scope.json'])
    need(hashlib.sha256(extra['scope.json']).hexdigest()==SCOPE_PIN)
    need(scope==dict(wi='WI-20261024-ATS-017',source='CURRENT_EXPLICIT_USER_DECISION_RECORDED_BY_MA',
        scope='STAGE_ONE_CURRENT_BACKUP_COST_METADATA',action=ACTION,candidate_sha256=STAGE_ONE_PIN,
        backup_candidate_sha256=CANDIDATE,approval_sha256=hashlib.sha256(private['approval.json']).hexdigest(),
        cost_input_sha256=hashlib.sha256(extra['cost-input.json']).hexdigest() if ACTION=='capture' else None,
        issued_at=approval['issued_at'],expires_at=approval['expires_at'],no_business_mutation=True,
        no_app=True,no_additional_provider_operation=True))
'''

DISPATCH=r'''
    import adapter
    costmeta=types.ModuleType('wi017_cost_metadata')
    exec(compile(extra['cost_metadata.py'],'<wi017-cost-metadata>','exec'),costmeta.__dict__)
    costmeta.install_release_inventory(adapter)
    if ACTION=='inventory':
        costmeta.install(entry,adapter,adapter.boot,mod,approval,private['decision.json'],DECISION_PIN,CANDIDATE)
    else:
        costmeta.install_account_query_context(adapter)
        bounds=types.ModuleType('wi017_cost_bound')
        exec(compile(extra['cost_bound.py'],'<wi017-cost-bound>','exec'),bounds.__dict__)
        supplied=mod.decode(extra['cost-input.json'])
        need(supplied['provider']['expires_at']>=approval['expires_at'])
        cost_source=mod.decode(private['cost-source.json'])
        need(hashlib.sha256(extra['cost-input.json']).hexdigest() in cost_source['evidence_references'])
        need(cost_source['observed_repository_bytes']==supplied['provider']['repository_actual_bytes'])
        original=adapter.ExistingCapture.source_unchanged
        def source_unchanged(self):
            original(self)
            current=costmeta.observe(self.baseline,adapter.boot,self.guard)
            need(current==supplied['metadata'])
            result=bounds.calculate(current,supplied['provider'],supplied['format_proof'],admitted_at=approval['issued_at'])
            need(result['status']=='WITHIN_CURRENT_INCLUDED_STORAGE')
            need(result['source_payload_bytes_bound']<=self.cost['max_source_bytes'])
        adapter.ExistingCapture.source_unchanged=source_unchanged
        original_stages=adapter.ExistingCapture.stages
        def stages(self):
            source_unchanged(self)
            return original_stages(self)
        adapter.ExistingCapture.stages=stages
    raise SystemExit(entry.main(argv))
'''


def payload(action,candidate,approval_raw,decision_raw,decision_pin,scope_raw,scope_pin,
        cost_raw=b'',cost_source_raw=b'',cost_input_raw=b''):
    own=verify_tree(HERE,candidate);old=load_backup()
    need(action in ('inventory','capture'))
    scope_record(scope_raw,scope_pin,candidate,approval_raw,action,cost_input_raw)
    raw=old.payload(action,BACKUP_PIN,approval_raw,decision_raw,decision_pin,cost_raw,cost_source_raw)
    extra={'cost_metadata.py':(HERE/'cost_metadata.py').read_bytes(),'cost_bound.py':(HERE/'cost_bound.py').read_bytes(),
        'scope.json':scope_raw,'candidate.json':(HERE/'candidate.json').read_bytes()}
    if action=='capture':
        need(0<len(cost_input_raw)<=262144)
        import cost_bound
        value=old.contract.decode(cost_input_raw)
        need(value['provider']['expires_at']>=old.contract.decode(approval_raw)['expires_at'])
        source=old.contract.decode(cost_source_raw)
        need(sha(cost_input_raw) in source['evidence_references']
            and source['observed_repository_bytes']==value['provider']['repository_actual_bytes'])
        result=cost_bound.calculate(value['metadata'],value['provider'],value['format_proof'])
        need(result['status']=='WITHIN_CURRENT_INCLUDED_STORAGE')
        extra['cost-input.json']=cost_input_raw
    text=raw.decode()
    marker="    need(Path('/proc/sys/kernel/random/boot_id').read_text().strip()==approval['boot_id'])"
    need(text.count(marker)==1 and text.count('    raise SystemExit(entry.main(argv))')==1)
    text=text.replace(marker,INJECTION+'\n'+marker)
    text=text.replace('    raise SystemExit(entry.main(argv))',DISPATCH)
    values=dict(EXTRA={k:base64.b64encode(v).decode() for k,v in extra.items()},
        EXTRA_PINS={k:sha(v) for k,v in extra.items()},SCOPE_PIN=scope_pin,STAGE_ONE_PIN=candidate)
    text=''.join(k+'='+repr(v)+'\n' for k,v in values.items())+text
    need(len(text.encode())<=2*1024**2);compile(text,'<wi017-native-stdin>','exec')
    return text.encode()


def main():
    parser=argparse.ArgumentParser();parser.add_argument('action',choices=('inventory','capture','check-cost'))
    for name in ('candidate','approval','decision','decision-pin','scope-record','scope-pin','cost','cost-source','cost-input','output'):
        parser.add_argument('--'+name)
    args=parser.parse_args()
    try:
        if args.action=='check-cost':
            import cost_bound
            value=json.loads(Path(args.cost_input).read_bytes())
            result=cost_bound.calculate(value['metadata'],value['provider'],value['format_proof'])
            print(json.dumps(result,sort_keys=True))
            return 0 if result['status']=='WITHIN_CURRENT_INCLUDED_STORAGE' else 2
        read=lambda name:Path(name).read_bytes() if name else b''
        raw=payload(args.action,args.candidate,read(args.approval),read(args.decision),args.decision_pin,
            read(args.scope_record),args.scope_pin,read(args.cost),read(args.cost_source),read(args.cost_input))
        with Path(args.output).open('xb') as stream:stream.write(raw)
        print('{"status":"PRIVATE_PAYLOAD_BUILT","network":"NOT_RUN"}');return 0
    except BaseException:
        print('{"status":"HOLD","code":"STAGE_ONE_HOLD"}');return 2


if __name__=='__main__':raise SystemExit(main())
