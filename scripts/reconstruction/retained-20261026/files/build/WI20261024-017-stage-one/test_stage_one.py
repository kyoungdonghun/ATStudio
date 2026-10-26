"""Focused changed-boundary checks; all records and host observations are synthetic."""
import ast
import base64
import copy
import json
from pathlib import Path
import stat
import sys
import time
from types import SimpleNamespace
from contextlib import contextmanager
import unittest
from unittest.mock import Mock,patch

HERE=Path(__file__).resolve().parent
sys.path.insert(0,str(HERE))
import stage_one as s
import cost_bound as c
import cost_metadata as m
old=s.load_backup()
sys.path.insert(0,str(s.BACKUP))
import test_preparation as fixture
import adapter


def sample():
    now=int(time.time())
    metadata=dict(status='BOUNDED_COST_METADATA_OBSERVED',source_bytes=80000000,files=160,directories=30,
        ancestors=10,manifest_bytes=65536,sql_bytes_bound=4*1024**2,sql_proof_bytes_bound=16384,
        payload_files=4,payload_directories=3,xattrs_absent=True,basename_utf8_bytes_max=255,
        local_owner_group_utf8_bytes_max=256,generic_attributes='LINUX_NONE',
        inventory_sha256='d'*64,units_sha256='e'*64)
    provider=dict(currency='KRW',observed_at=now,expires_at=now+1800,repository_actual_bytes=63569920,
        account_other_stored_bytes=0,storage_included_bytes=1000000000,other_growth_reserved_bytes=0,
        concurrent_writers_absent=True,account_allowance_verified=True,api_currently_free=True,
        same_provider_internal_traffic_free=True,same_internal_endpoint_verified=True,tax_on_zero_is_zero=True,
        retained_storage_current_terms_continue=True)
    proof=dict(restic='0.19.1',zstd='1.18.6',linux_generic_attributes_absent=True,new_pack_index_entries_once=True,
        index_entry_bytes_max=512,index_file_envelope_bytes_max=256,snapshot_lock_plaintext_bytes_max=65536,
        allocation_unit_bytes=4096,maximum_operation_seconds=1800,restic_commands=5,lock_refresh_seconds=300)
    return dict(metadata=metadata,provider=provider,format_proof=proof)


def scope(approval_raw,action,candidate='a'*64,cost_input=b''):
    approval=old.contract.decode(approval_raw)
    return s.canonical(dict(wi='WI-20261024-ATS-017',source='CURRENT_EXPLICIT_USER_DECISION_RECORDED_BY_MA',
        scope=s.SCOPE,action=action,candidate_sha256=candidate,backup_candidate_sha256=s.BACKUP_PIN,
        approval_sha256=s.sha(approval_raw),issued_at=approval['issued_at'],expires_at=approval['expires_at'],
        cost_input_sha256=s.sha(cost_input) if action=='capture' else None,
        no_business_mutation=True,no_app=True,no_additional_provider_operation=True))


@contextmanager
def two_release_tree():
    """Synthetic Linux-like metadata; execute the real retained complete-tree collector."""
    root=Path('/synthetic');current=root/'current';previous=root/'old';tools=root/'tools'
    media=tuple(root/name for name in ('public','private','tmp'))
    contents={current/'app.jar':b'synthetic-jar'}
    manifests=[]
    for release in (previous,current):
        files={}
        for index in range(143):
            name='index.html' if index==0 else 'assets/file-'+str(index)+'.js'
            body=('synthetic-'+release.name+'-'+str(index)).encode()
            contents[release/'frontend'/name]=body
            files[name]={'bytes':len(body),'sha256':s.sha(body)}
        manifests.append(files)
    artifact=tools/'binding/artifact.json';freeze=tools/'binding/freeze.json'
    contents[artifact]=s.canonical({'spa_files':manifests[1]})
    contents[freeze]=s.canonical({'files':{'binding/artifact.json':s.sha(contents[artifact])}})
    old_allow=s.canonical({'frontend_root':str(previous/'frontend'),'files':manifests[0]})
    directories=set(media)
    for path in list(contents)+list(media):directories.update(path.parents)
    def info(path,*args,**kwargs):
        if path==Path('/var/lib/atstudio'):return SimpleNamespace(st_dev=64769)
        if path not in contents and path not in directories:raise FileNotFoundError('SYNTHETIC_MISSING')
        return SimpleNamespace(st_dev=64769,st_mode=(stat.S_IFREG|0o600) if path in contents else (stat.S_IFDIR|0o700),
            st_nlink=1,st_uid=0,st_gid=0,st_size=len(contents[path]) if path in contents else 0)
    @contextmanager
    def scan(path):
        yield [SimpleNamespace(path=str(item)) for item in set(contents)|directories if item!=path and item.parent==path]
    original=adapter.current_members
    ctx=(SimpleNamespace(read=lambda path,*args:contents[path],decode=json.loads),None,
        SimpleNamespace(RELEASE=current,TOOLS=tools),None)
    try:
        with patch.object(adapter.cold,'ROOTS',(*media,root/'unused',previous)),\
             patch.object(adapter.cold,'RELEASE_SHA',s.sha(old_allow)),\
             patch.object(adapter.guard,'JAR',s.sha(contents[current/'app.jar'])),\
             patch.object(adapter.guard,'RUNTIME',s.sha(contents[freeze])),\
             patch.object(adapter.boot,'read_file',return_value=old_allow),\
             patch.object(Path,'stat',info),patch.object(Path,'lstat',info),\
             patch.object(adapter.cold.os,'scandir',scan),\
             patch.object(adapter.cold,'sha_file',side_effect=lambda path,*args:s.sha(contents[path])):
            m.install_release_inventory(adapter)
            yield ctx,contents,current,previous,tools,original
    finally:adapter.current_members=original


class StageOneTests(unittest.TestCase):
    def test_fixed_account_schema_real_query_protocol_and_source_stage(self):
        day='2026-10-24';grant=dict(id=1,user_id=3,subscription_id=1,status='CANCELLED',billing_cycle='MONTHLY',
            started_at=day,expires_at=day,pending_subscription_id=None,pending_billing_cycle=None)
        rows=[dict(kind='counts',users=3,selected_users=3,plans=1,subscription_rows=1,grants=1,
            admin_audits=1,pending_audio=0,unfinished_storage=0)]
        rows += [dict(kind='account',id=uid,role='ADMIN' if uid==2 else 'USER',deleted=0,verified=1,
            total_subscriptions=int(uid==3),effective_subscriptions=int(uid==3),eligible_subscriptions=int(uid==3))
            for uid in (1,2,3)]
        rows += [dict(kind='plan',id=1,user_type='INDIVIDUAL',is_active=1,download_per_day=5),dict(kind='grant',**grant)]
        protocol=b'\n'.join(s.canonical(row) for row in rows)+b'\n'
        original=adapter.accounts.queries;raw=original(day);inputs={};commands=[]
        obj=adapter.ExistingCapture.__new__(adapter.ExistingCapture)
        obj.counter=0;obj.work=Path('/synthetic');obj.approval={'expected_grant':grant}
        def command(argv,**kwargs):
            self.assertEqual(argv[:2],['/usr/bin/mysql','--no-defaults'])
            self.assertEqual(kwargs['label'],'MYSQL_QUERY');self.assertEqual(kwargs['cap'],65536)
            self.assertNotIn('atstudio_prod',' '.join(argv));body=inputs[kwargs['input_file']];commands.append(body)
            if body==b'SELECT 1;\n':return b'1\n'
            if not body.startswith(b'USE `atstudio_prod`;\n'):raise ValueError('SYNTHETIC_SQL_1046')
            self.assertEqual(body.split(b'\n',1)[1],original(day))
            return protocol
        obj.command=command
        class StopBeforeDump(Exception):pass
        obj.restic=Mock(side_effect=[s.canonical({'id':adapter.contract.REPOSITORY_ID}),
            s.canonical([{'id':adapter.restore.SNAPSHOT_ID}])])
        obj.start_mysql=Mock(return_value='synthetic-source.service')
        obj.sql_capture=Mock(side_effect=StopBeforeDump)
        try:
            with patch.object(adapter.boot,'exclusive',side_effect=lambda path,body:inputs.__setitem__(path,body)),\
                 patch.object(adapter.cold,'directory'),\
                 patch.object(adapter,'datetime',SimpleNamespace(now=lambda *_:__import__('datetime').datetime(2026,10,24))):
                with self.assertRaisesRegex(ValueError,'SYNTHETIC_SQL_1046'):obj.query(raw)
                m.install_account_query_context(adapter)
                self.assertEqual(adapter.accounts.queries(day),b'USE `atstudio_prod`;\n'+raw)
                observed=adapter.accounts.parse(obj.query(adapter.accounts.queries(day)),{})
                self.assertTrue(observed['expected_actor_roles_match']);self.assertEqual(observed['grants'],[grant])
                self.assertEqual(obj.query(b'SELECT 1;\n'),b'1\n')
                # Exact retained stages/query/parser through account admission, ending before any dump.
                with self.assertRaises(StopBeforeDump):obj.stages()
                obj.sql_capture.assert_called_once_with(obj.work/'payload')
                obj.start_mysql.assert_called_once_with('source')
                self.assertEqual(obj.stage,'SOURCE_SQL_WINDOW')
        finally:adapter.accounts.queries=original
        with patch.object(adapter.cold,'SCHEMA','unapproved_schema'),self.assertRaises(ValueError):
            m.install_account_query_context(adapter)

    def test_remaining_sql_protocol_is_schema_explicit_without_global_query_change(self):
        schema=adapter.cold.SCHEMA
        self.assertIs(adapter.ExistingCapture.query,adapter.cold.Capture.query)
        self.assertIn(b'FROM information_schema.TABLES',adapter.checks.mysql_inventory_query(schema))
        self.assertIn(b'FROM `atstudio_prod`.`users`',adapter.checks.row_count_query(schema,('users',)))
        self.assertIn(b'FROM information_schema.COLUMNS',adapter.equivalent.column_query(schema))
        for only_schema in (False,True):
            argv=adapter.cold.dump_arguments(only_schema)
            self.assertEqual(argv[-2:],['--databases','atstudio_prod'])
        # The retained import consumes that complete database dump into its isolated bound datadir.
        self.assertIn("input_file=dump",(s.BACKUP/'adapter.py').read_text())

    def test_two_release_shape_reproduces_old_failure_and_exact_repair(self):
        with two_release_tree() as (ctx,contents,current,previous,tools,original):
            with self.assertRaisesRegex(Exception,'UNREVIEWED_SOURCE_FILE'):original(ctx)
            roots,members=adapter.current_members(ctx)
            self.assertEqual(roots[-3:],(current,previous,tools))
            self.assertEqual({name for name,row in members.items() if row['type']=='file'},set(map(str,contents)))
            self.assertEqual(len([p for p in contents if p.is_relative_to(current/'frontend')]),143)
            self.assertEqual(len([p for p in contents if p.is_relative_to(previous/'frontend')]),143)
            # This is the actual retained current manifest provenance, never observed target hashes.
            runtime=HERE.parent/'WI067-auth-trial-v8r1/binding'
        artifact=(runtime/'artifact.json').read_bytes();frozen=json.loads((runtime/'freeze.json').read_bytes())
        self.assertEqual(s.sha(artifact),frozen['files']['binding/artifact.json'])
        self.assertEqual(len(json.loads(artifact)['spa_files']),143)

    def test_repaired_collect_preserves_unexpected_missing_size_hash_and_manifest_refusal(self):
        for change,code in [('unexpected','UNREVIEWED_SOURCE_FILE'),('missing','RELEASE_MISSING_FILE'),
            ('size','RELEASE_SIZE'),('hash','RELEASE_DRIFT'),('manifest','CURRENT_SPA_MANIFEST_PIN')]:
            with self.subTest(change=change),two_release_tree() as (ctx,contents,current,previous,tools,_):
                path=current/'frontend/index.html'
                if change=='unexpected':contents[current/'sidecar.bin']=b'unreviewed'
                elif change=='missing':contents.pop(path)
                elif change=='size':contents[path]+=b'x'
                elif change=='hash':contents[path]=b'x'*len(contents[path])
                else:contents[tools/'binding/artifact.json']+=b' '
                with self.assertRaisesRegex(Exception,code):adapter.current_members(ctx)

    def test_cost_formula_positive_zero_benefits_and_account_total(self):
        v=sample();result=c.calculate(**v)
        self.assertEqual(result['status'],'WITHIN_CURRENT_INCLUDED_STORAGE')
        self.assertEqual(result['authorization'],'NOT_ISSUED')
        self.assertGreater(result['new_repository_bytes_bound'],v['metadata']['source_bytes'])
        self.assertEqual(c.packs(524288,1),524288+126+24)
        v['provider']['account_other_stored_bytes']=900000000
        self.assertEqual(c.calculate(**v)['status'],'HOLD_STORAGE_BOUND')

    def test_unknown_false_stale_wrong_types_and_format_fail_closed(self):
        original=sample()
        for group,key,value in [('provider','api_currently_free',False),('provider','observed_at',0),
            ('provider','repository_actual_bytes',True),('metadata','xattrs_absent',False),
            ('format_proof','new_pack_index_entries_once',False)]:
            v=copy.deepcopy(original);v[group][key]=value
            with self.subTest(key=key),self.assertRaises(ValueError):c.calculate(**v)

    def test_metadata_measures_real_shape_no_xattr_values(self):
        members={'/srv/synthetic':dict(type='dir',mode=448,uid=0,gid=0),
            '/srv/synthetic/file':dict(type='file',mode=384,uid=0,gid=0,size=10,sha256='1'*64)}
        baseline=dict(members=members,source_bytes=10,source_sha256='1'*64,jar_sha256='2'*64,
            runtime_sha256='3'*64,inventory_sha256='4'*64,units_sha256='5'*64)
        boot=SimpleNamespace(read_file=Mock(return_value=b'root:x:0:0:synthetic\n'))
        info=lambda _:SimpleNamespace(st_mode=stat.S_IFDIR|0o700,st_uid=0,st_gid=0,st_ino=1,st_dev=1,st_nlink=1,st_size=0)
        result=m.observe(baseline,boot,lambda:None,lambda *a,**k:[],info)
        self.assertEqual(result['files'],1);self.assertEqual(result['source_bytes'],10)
        self.assertNotIn('/srv/',json.dumps(result))
        with self.assertRaises(ValueError):m.observe(baseline,boot,lambda:None,lambda *a,**k:['user.synthetic'],info)
        self.assertEqual(set(call.args[0].as_posix() for call in boot.read_file.call_args_list),{'/etc/passwd','/etc/group'})

    def test_raw_scope_and_cost_input_binding(self):
        contract=fixture.contract('inventory',s.BACKUP_PIN);approval=s.canonical(contract[0]);raw=scope(approval,'inventory')
        s.scope_record(raw,s.sha(raw),'a'*64,approval,'inventory')
        for changed in (raw+b' ',raw.replace(b'STAGE_ONE_CURRENT',b'OLD')):
            with self.assertRaises(ValueError):s.scope_record(changed,s.sha(raw),'a'*64,approval,'inventory')
        approval=s.canonical(fixture.contract('capture',s.BACKUP_PIN)[0]);input_raw=s.canonical(sample())
        raw=scope(approval,'capture',cost_input=input_raw)
        s.scope_record(raw,s.sha(raw),'a'*64,approval,'capture',input_raw)
        with self.assertRaises(ValueError):s.scope_record(raw,s.sha(raw),'a'*64,approval,'capture',input_raw+b' ')

    def test_retained_inventory_payload_and_exact_extension_compile(self):
        value=fixture.contract('inventory',s.BACKUP_PIN);approval=s.canonical(value[0]);raw_scope=scope(approval,'inventory')
        manifest={'files':{name:s.sha((HERE/name).read_bytes()) for name in ('cost_metadata.py','cost_bound.py')}}
        real_read=Path.read_bytes
        def read(path):return s.canonical(manifest) if path==HERE/'candidate.json' else real_read(path)
        with patch.object(s,'verify_tree',return_value={}),patch.object(Path,'read_bytes',read):
            payload=s.payload('inventory','a'*64,approval,value[1],value[2],raw_scope,s.sha(raw_scope))
        tree=ast.parse(payload);compile(tree,'<synthetic-payload>','exec')
        literals={node.targets[0].id:ast.literal_eval(node.value) for node in tree.body[:9]}
        self.assertEqual(base64.b64decode(literals['EXTRA']['scope.json']),raw_scope)
        self.assertIn(b'costmeta.install(entry,adapter',payload)
        self.assertNotIn(b"action='reset'",payload)

    def test_real_inherited_guard_and_native_shaped_capture_hook(self):
        self.assertTrue(callable(adapter.ExistingCapture.guard))
        obj=adapter.ExistingCapture.__new__(adapter.ExistingCapture)
        obj.cost={'expires_at':int(time.time())+100};obj.guard=adapter.ExistingCapture.guard.__get__(obj)
        with patch.object(adapter.cold.Capture,'guard',return_value=None) as guard:
            obj.guard();guard.assert_called_once_with()
        # Compile the exact remote DISPATCH block and execute only its capture hook;
        # retained entry.main is injected, so no native operation can occur.
        v=sample();cost_input=s.canonical(v);obj.baseline={};obj.cost.update(max_source_bytes=900000000)
        old_source=Mock();old_stages=Mock(return_value='SYNTHETIC_STAGE_PASS')
        original_source=adapter.ExistingCapture.source_unchanged;original_stages=adapter.ExistingCapture.stages
        fake_meta=SimpleNamespace(observe=Mock(return_value=v['metadata']),install_release_inventory=Mock(),
            install_account_query_context=Mock())
        extra={'cost_metadata.py':b'','cost_bound.py':b'', 'cost-input.json':cost_input}
        approval={'issued_at':int(time.time()),'expires_at':int(time.time())+100}
        native_scope=dict(adapter=adapter,types=SimpleNamespace(ModuleType=lambda name:fake_meta if 'metadata' in name else c),
            extra=extra,mod=old.contract,approval=approval,need=s.need,hashlib=__import__('hashlib'),
            private={'cost-source.json':s.canonical(dict(evidence_references=[s.sha(cost_input)],observed_repository_bytes=v['provider']['repository_actual_bytes']))},
            ACTION='capture',entry=SimpleNamespace(main=lambda argv:0),argv=[])
        try:
            adapter.ExistingCapture.source_unchanged=old_source;adapter.ExistingCapture.stages=old_stages
            script='\n'.join(line[4:] if line.startswith('    ') else line for line in s.DISPATCH.splitlines())
            with self.assertRaises(SystemExit):exec(compile(script,'<capture-hook>','exec'),native_scope)
            fake_meta.install_release_inventory.assert_called_once_with(adapter)
            fake_meta.install_account_query_context.assert_called_once_with(adapter)
            self.assertEqual(obj.stages(),'SYNTHETIC_STAGE_PASS')
            fake_meta.observe.assert_called_once_with(obj.baseline,adapter.boot,obj.guard)
            old_source.assert_called_once_with(obj);old_stages.assert_called_once_with(obj)
            v['metadata']['xattrs_absent']=False
            with self.assertRaises(ValueError):obj.stages()
            self.assertEqual(old_stages.call_count,1)
        finally:
            adapter.ExistingCapture.source_unchanged=original_source;adapter.ExistingCapture.stages=original_stages


if __name__=='__main__':unittest.main()
