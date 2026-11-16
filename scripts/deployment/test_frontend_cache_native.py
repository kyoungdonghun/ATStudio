"""Native apply fault tests on temporary files; no server/service execution."""
import importlib.util
import json
from pathlib import Path
import sys
import tempfile
import time
import types
import unittest
from unittest.mock import patch

if sys.platform == 'win32': sys.modules['fcntl'] = types.SimpleNamespace()
spec=importlib.util.spec_from_file_location('native_cache',Path(__file__).with_name('frontend_cache_native.py'))
n=importlib.util.module_from_spec(spec);spec.loader.exec_module(n)


class NativeTest(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.root=Path(self.temp.name)
        self.base=self.root/'private';self.base.mkdir();self.release=self.root/'release'
        self.site=self.root/'site.conf';self.old=b'old site';self.site.write_bytes(self.old)
        self.before={'observed_at':int(time.time()),'old_jar_sha256':'feff6919a291caff985d0c386d23ba30ee53ff0b2a9a32fd5d162208bf401a91',
                     'configs':{str(self.site):'4772ec565862b9eb3b38eca6786b32b2dc7cd114ccfc0aab50fd43bb4ae1086e'},'free_bytes':1024**3}
        self.spec={'action':'apply','expires_at':int(time.time())+890,'identity':{},'preflight':self.before,'archive_sha256':'x','manifest_sha256':'y'}
        self.new=b'# BEGIN ATSTUDIO CACHE V1\n'+str(self.release/'frontend').encode()
        self.calls=[]
        self.patches=[patch.object(n,'BASE',self.base),patch.object(n,'NEW',self.release),patch.object(n,'SITE',self.site),
            patch.object(n,'trusted',lambda p:Path(p)),patch.object(n,'identity',lambda:{}),patch.object(n,'unchanged',lambda *a:{'free_bytes':1024**3}),
            patch.object(n,'contents',lambda s:{'active-site.conf':self.new,'frontend/index.html':b'index','frontend/assets/index-AbCd1234.js':b'js'}),
            patch.object(n.signal,'signal',lambda *a:None),patch.object(n.signal,'SIGHUP',1,create=True),patch.object(n,'replace_exact',self.replace),patch.object(n,'newfile',self.newfile),patch.object(n,'run',self.command)]
        self.read=Path.read_bytes
        def read(path):
            if path.as_posix()=='/etc/atstudio/nginx/nginx.conf':return b'include '+str(self.site).encode()+b';'
            return self.read(path)
        self.patches.append(patch.object(Path,'read_bytes',read))
        for p in self.patches:p.start()
    def tearDown(self):
        for p in reversed(self.patches):p.stop()
        self.temp.cleanup()
    def replace(self,path,expected,raw):
        self.assertEqual(n.sha(path),expected);path.write_bytes(raw)
    def newfile(self,path,raw,mode=0o600):
        with path.open('xb') as f:f.write(raw)
    def command(self,args,timeout=30):self.calls.append(args);return b''
    def test_success_no_app_db_restart(self):
        result=n.apply(self.spec)
        self.assertEqual(result['status'],'FRONTEND_CACHE_APPLIED')
        self.assertFalse(result['app_db_restarted'])
        self.assertEqual(self.site.read_bytes(),self.new)
        self.assertTrue(all('restart' not in a and 'start' not in a for a in self.calls))
        self.assertEqual(sum(a[1:]==['reload','nginx.service'] for a in self.calls),1)
    def test_syntax_failure_before_active_write(self):
        with patch.object(n,'run',side_effect=ValueError('COMMAND_FAILED')):
            with self.assertRaises(ValueError):n.apply(self.spec)
        self.assertEqual(self.site.read_bytes(),self.old)
        self.assertEqual(json.loads((self.base/'apply.failed.json').read_bytes())['rollback'],'NO_ACTIVE_CHANGE')
    def test_reload_failure_restores_only_owned_site(self):
        failures=0
        def run(args,timeout=30):
            nonlocal failures
            self.calls.append(args)
            if args[1:]==['reload','nginx.service']:
                failures+=1
                if failures==1:raise ValueError('COMMAND_FAILED')
            return b''
        with patch.object(n,'run',run):
            with self.assertRaises(ValueError):n.apply(self.spec)
        self.assertEqual(self.site.read_bytes(),self.old)
        self.assertEqual(failures,2)
    def test_after_rename_error_is_reconciled(self):
        def replace(path,pin,raw):
            self.replace(path,pin,raw)
            if raw==self.new:raise OSError('fsync failure after rename')
        with patch.object(n,'replace_exact',replace):
            with self.assertRaises(OSError):n.apply(self.spec)
        self.assertEqual(self.site.read_bytes(),self.old)
    def test_foreign_site_not_overwritten_on_failure(self):
        def run(args,timeout=30):
            if args[1:]==['reload','nginx.service']:
                self.site.write_bytes(b'foreign');raise ValueError('COMMAND_FAILED')
            return b''
        with patch.object(n,'run',run):
            with self.assertRaises(ValueError):n.apply(self.spec)
        self.assertEqual(self.site.read_bytes(),b'foreign')
        self.assertEqual(json.loads((self.base/'apply.failed.json').read_bytes())['rollback'],'HOLD_INSPECT_OWNERSHIP_AND_RUNTIME')
    def test_consumed_attempt_cannot_replay(self):
        n.apply(self.spec)
        with self.assertRaisesRegex(ValueError,'FRESH_RELEASE'):n.apply(self.spec)
    def test_expired_spec_rejected_before_effects(self):
        self.spec['expires_at']=0
        with self.assertRaisesRegex(ValueError,'SPEC_WINDOW'):n.apply(self.spec)
        self.assertFalse(self.release.exists())


if __name__=='__main__':unittest.main()
