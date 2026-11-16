"""Additive migration archive and atomic-no-replace checks; no server access."""
import importlib.util
from pathlib import Path
import os
import shutil
import sys
import tempfile
import types
import unittest
from unittest.mock import patch
if sys.platform=='win32':sys.modules['fcntl']=types.SimpleNamespace()
s=importlib.util.spec_from_file_location('a',Path(__file__).with_name('frontend_cache_additive.py'))
a=importlib.util.module_from_spec(s);s.loader.exec_module(a)
ROOT=Path(__file__).resolve().parents[2]
class AdditiveTest(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory();self.base=Path(self.tmp.name)
        self.patches=[patch.object(a,'BASE',self.base),patch.object(a,'trusted',lambda p:Path(p))]
        for p in self.patches:p.start()
    def tearDown(self):
        for p in reversed(self.patches):p.stop()
        self.tmp.cleanup()
    def test_exact_historical_payload_scope(self):
        fixture=ROOT/'build/WI-20261117-ATS-002/additive-tag/payload.tar'
        if not fixture.is_file():
            self.skipTest('Optional historical integration fixture is retained locally, not in Git')
        shutil.copyfile(fixture,self.base/'payload.tar')
        result=a.load_additions()
        self.assertEqual(len(result),16);self.assertEqual(len(a.EXPECTED|a.ADDITIONS),81)
        self.assertTrue(all(n.endswith('.js') and n.startswith('assets/') for n in result))
        self.assertFalse(set(result)&set(a.EXPECTED))
    def test_payload_tampering_rejected(self):
        (self.base/'payload.tar').write_bytes(b'not reviewed')
        with self.assertRaisesRegex(ValueError,'ARCHIVE_PIN'):a.load_additions()
    def test_existing_asset_rejected_without_change(self):
        target=self.base/'old.js';target.write_bytes(b'keep')
        with self.assertRaisesRegex(ValueError,'TARGET_EXISTS'):a.atomic_add(target,b'new')
        self.assertEqual(target.read_bytes(),b'keep')
    def test_atomic_link_complete_bytes_and_no_temporary_left(self):
        def newfile(path,raw,mode):
            with path.open('xb') as f:f.write(raw)
        realopen=os.open
        def open(path,flags,*args):
            return realopen(os.devnull,os.O_RDONLY) if Path(path).is_dir() else realopen(path,flags,*args)
        with patch.object(a,'newfile',newfile),patch.object(a.os,'O_DIRECTORY',0,create=True),patch.object(a.os,'open',open),patch.object(a.os,'fsync',lambda fd:None):
            a.atomic_add(self.base/'new.js',b'complete bytes')
        self.assertEqual((self.base/'new.js').read_bytes(),b'complete bytes')
        self.assertEqual([p.name for p in self.base.iterdir()],['new.js'])
if __name__=='__main__':unittest.main()
