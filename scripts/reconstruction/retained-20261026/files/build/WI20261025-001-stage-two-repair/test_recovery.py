"""Focused real-adapter flow with synthetic OS/systemd/MySQL boundaries only."""
import ast,copy,json,os,re,stat,sys,tempfile,types,unittest
from pathlib import Path
from unittest.mock import patch
HERE=Path(__file__).resolve().parent
FROZEN=HERE.parent/'WI20261023-005-account-fixture'
sys.path.insert(0,str(FROZEN));sys.path.insert(0,str(HERE))
import native_adapter as native
import transaction as tx
from test_stage_two import proposal

class Recovery(unittest.TestCase):
    def test_cutoff_keeps_write_denial_and_readonly_reconciliation(self):
        for day in ('2026-11-24','2026-11-25'):
            for mode in ('inspect','apply','reconcile'):
                p=proposal(mode)
                with self.subTest(day=day,mode=mode),patch.object(tx.core,'day_now',return_value=day):
                    if day>tx.END and mode!='reconcile':
                        with self.assertRaisesRegex(ValueError,'EXPIRED_END_DATE'):tx.validate(p,mode,'c'*64)
                    else:tx.validate(p,mode,'c'*64)

    def test_reconcile_still_requires_fresh_readonly_exact_authority(self):
        for key,value in (('allow_writes',True),('expires_at',0),('reconcile_attempt','a'*32),('reconcile_attempt','x'*32),('candidate_sha256','f'*64)):
            p=proposal('reconcile');p[key]=value
            with self.subTest(key=key),patch.object(tx.core,'day_now',return_value='2026-11-25'):
                with self.assertRaises(ValueError):tx.validate(p,'reconcile','c'*64)

    def test_native_retry_after_log_then_start_failure_keeps_original_claim(self):
        # Native adapter and actual log helper execute. Only Linux ownership,
        # filesystem/proc/socket views, systemd and MySQL are synthetic.
        root=Path(tempfile.mkdtemp(prefix='recovery-',dir=HERE))
        evidence=root/'evidence';evidence.mkdir()
        logs=root/'logs';logs.mkdir();expected_directory=logs.stat()
        p=proposal('reconcile');directory=evidence/p['nonce'];directory.mkdir()
        original=dict(wi=tx.WI,mode='apply',nonce=p['nonce'],candidate_sha256='c'*64)
        before={'synthetic':{'[1]':{'v':'before'}}};after={'synthetic':{'[1]':{'v':'after'}}}
        for filename,value in [('claim.json',original),('before.json',before),('expected-after.json',after)]:
            (directory/filename).write_text(json.dumps(value))
        claim_bytes=(directory/'claim.json').read_bytes()
        state=types.SimpleNamespace(running=False,unit=None,starts=0,stops=0,fail=True,sql=[],argv=[])
        realos=os
        class FileBoundary:
            O_RDONLY=os.O_RDONLY;O_DIRECTORY=0;O_NOFOLLOW=0;O_WRONLY=os.O_WRONLY;O_CREAT=os.O_CREAT;O_EXCL=os.O_EXCL
            def open(self,name,flags,mode=0o777,dir_fd=None):
                if dir_fd is None:
                    assert Path(name)==logs;return -99
                assert dir_fd==-99;return realos.open(logs/name,flags,mode)
            def fstat(self,fd):assert fd==-99;return expected_directory
            def fchmod(self,fd,mode):assert mode==0o600
            def fchown(self,fd,uid,gid):assert (uid,gid)==(110,112)
            def fsync(self,fd):
                if fd!=-99:realos.fsync(fd)
            def close(self,fd):
                if fd!=-99:realos.close(fd)
        tree=ast.parse((FROZEN/'reused/cold_capture.py').read_bytes())
        logpath=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='mysql_log_path')
        logfunc=next(n for c in tree.body if isinstance(c,ast.ClassDef) for n in c.body if isinstance(n,ast.FunctionDef) and n.name=='prepare_mysql_log')
        scope=dict(Path=Path,re=re,require=tx.core.need,MYSQL_LOGS=logs,os=FileBoundary(),mysql_log_directory=lambda:expected_directory)
        exec(compile(ast.Module(body=[logpath,logfunc],type_ignores=[]),'<actual-retained-log-helper>','exec'),scope)
        helper={'mysql_client_environment':lambda:{},'prepare_mysql_log':scope['prepare_mysql_log']}
        class H:
            def decode(self,b):return json.loads(b)
            def read(self,p,*_):return p.read_bytes()
            def secure(self,p,*_):assert p.is_dir()
            def directory(self,p):p.mkdir()
            def publish(self,p,b):
                with p.open('xb') as f:f.write(b)
            def command(self,argv,seconds):
                state.argv.append(argv);state.starts+=1;state.unit=argv[0]
                assert argv[1]=='reconcile'
                if state.fail:state.fail=False;raise RuntimeError('synthetic start failure after log')
                state.running=True
            def ctl(self,action,name,seconds):
                assert action=='stop' and name==state.unit;state.stops+=1;state.running=False
        h=H()
        class OP:
            SOCKET='/run/synthetic-stage2.sock'
            def identity(self,*_):return 'synthetic-identity'
            def cold(self,*_):assert not state.running;return {'cold':True}
            def properties(self,h,name,fields):
                return dict(LoadState='loaded' if state.running and name==state.unit else 'not-found',MainPID='123' if state.running and name==state.unit else '0',ControlPID='0',Job='')
        op=OP()
        class Virtual:
            def __init__(self,s):self.s=s
            def exists(self):return state.running
            def lstat(self):return types.SimpleNamespace(st_mode=stat.S_IFSOCK,st_uid=110,st_gid=112)
            def stat(self):return types.SimpleNamespace(st_dev=1,st_ino=2)
            def read_text(self):return '0::/system.slice/'+state.unit
        def paths(s):
            s=str(s)
            if s in (op.SOCKET,'/var/lib/mysql') or s.startswith('/proc/123/'):return Virtual(s)
            return Path(s)
        class DB:
            def __init__(self,*_):pass
            def query(self,s):
                state.sql.append(s)
                if "'version'" in s:return [dict(version='8.0.46-0ubuntu0.24.04.4',network=1,events='DISABLED',infile=0,readonly=1,super=1)]
                return []
            def close(self):pass
        source=types.SimpleNamespace(PINS={},helpers=lambda *_:helper)
        fakeos=types.SimpleNamespace(name='posix',geteuid=lambda:0,path=types.SimpleNamespace(lexists=lambda p:state.running if str(p)=='/run/mysqld' else os.path.lexists(p)))
        signals=types.SimpleNamespace(SIGHUP=1,SIGINT=2,SIGTERM=15,SIG_IGN=0,signal=lambda *_:0)
        with patch.multiple(native,HERE=FROZEN,TARGET=FROZEN,EVIDENCE=evidence,os=fakeos,signal=signals,Path=paths,
                runtime=lambda:(h,None,None,op,source,None),MysqlSession=DB,
                mysql_arguments=lambda helper,name,mode:(name,mode),inspect_database=lambda *_:{}),patch.object(tx.core,'snapshot',return_value=after):
            first=native.native('reconcile',p,'c'*64)
            self.assertEqual(first['status'],'HOLD')
            self.assertEqual(first['cleanup'],dict.fromkeys(('STOP','PID_ZERO','SOCKET_GONE','BASELINE'),'PASS'))
            second_p=copy.deepcopy(p);second_p['reconcile_attempt']='f'*32
            second=native.native('reconcile',second_p,'c'*64)
            self.assertEqual(second['status'],'APPLIED_CONFIRMED')
            self.assertEqual(second['cleanup'],dict.fromkeys(('DB_SESSION','STOP','PID_ZERO','SOCKET_GONE','BASELINE'),'PASS'))
            # Same attempt cannot reuse its log or evidence; a new attempt is required.
            replay=native.native('reconcile',second_p,'c'*64)
            self.assertIn(replay['status'],('HOLD','HOLD_RECONCILE'))
        self.assertEqual(state.starts,2);self.assertEqual(state.stops,2)
        self.assertEqual(len(list(logs.iterdir())),2)
        self.assertEqual((directory/'claim.json').read_bytes(),claim_bytes)
        self.assertEqual(p['nonce'],second_p['nonce'])
        self.assertNotEqual(state.argv[0][0],state.argv[1][0])
        self.assertTrue(all(re.fullmatch(r'[a-z0-9-]{1,100}\.service',a[0]) for a in state.argv))
        self.assertFalse(any(re.search(r'\b(UPDATE|INSERT|DELETE|COMMIT)\b',s) for s in state.sql))
        self.assertIn('START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY',state.sql)
        self.assertFalse(state.running)

if __name__=='__main__':unittest.main(verbosity=2)
