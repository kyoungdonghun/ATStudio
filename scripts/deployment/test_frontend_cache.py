"""Focused cache-generation and nginx rendering regressions (no remote actions)."""
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('cache', Path(__file__).with_name('frontend_cache.py'))
c = importlib.util.module_from_spec(spec); spec.loader.exec_module(c)
ROOT = Path(__file__).resolve().parents[2]


class CacheTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(); self.base = Path(self.temp.name)
    def tearDown(self): self.temp.cleanup()
    def generation(self, label, files):
        root = self.base / label; root.mkdir()
        for name, raw in files.items():
            p = root / name; p.parent.mkdir(exist_ok=True); p.write_bytes(raw)
        manifest = self.base / (label + '.json')
        manifest.write_text(json.dumps({n:c.sha(v) for n,v in files.items()}))
        return {'root':str(root),'manifest':str(manifest),'sha256':c.sha(manifest.read_bytes())}
    def fixture(self, label='one', value=b'a'):
        return self.generation(label, {'index.html':label.encode(),'assets/chunk-AbCd1234.js':value})
    def test_retains_assets_never_old_html(self):
        a=self.fixture(); b=self.generation('two',{'index.html':b'old','assets/old-XyZa5678.js':b'oldjs'})
        result=c.assemble([a,b], self.base/'out')
        self.assertEqual((self.base/'out/index.html').read_bytes(), b'one')
        self.assertIn('assets/old-XyZa5678.js', result['files'])
    def test_collision_rejected_before_write(self):
        a=self.fixture(); b=self.fixture('two',b'changed')
        with self.assertRaisesRegex(ValueError,'COLLISION'): c.assemble([a,b],self.base/'out')
        self.assertFalse((self.base/'out').exists())
    def test_extra_source_map_rejected(self):
        a=self.fixture(); (Path(a['root'])/'assets/leak-AbCd1234.js.map').write_text('secret')
        with self.assertRaisesRegex(ValueError,'SCOPE'): c.assemble([a],self.base/'out')
    def test_unhashed_and_traversal_manifest_rejected(self):
        a=self.generation('one',{'index.html':b'a','assets/config.js':b'b'})
        with self.assertRaisesRegex(ValueError,'SCOPE'): c.assemble([a],self.base/'out')
    def test_tampering_rejected(self):
        a=self.fixture(); (Path(a['root'])/'index.html').write_text('changed')
        with self.assertRaisesRegex(ValueError,'FILE_PIN'): c.assemble([a],self.base/'out')
    def test_manifest_pin_rejected(self):
        a=self.fixture(); a['sha256']='0'*64
        with self.assertRaisesRegex(ValueError,'MANIFEST_PIN'): c.assemble([a],self.base/'out')
    def test_retention_bound(self):
        a=self.fixture()
        with self.assertRaisesRegex(ValueError,'GENERATION_BOUND'): c.assemble([a]*5,self.base/'out')
    def test_union_size_bound(self):
        a=self.fixture(); old=c.MAX_BYTES;c.MAX_BYTES=1
        try:
            with self.assertRaisesRegex(ValueError,'BYTE_BOUND'): c.assemble([a],self.base/'out')
        finally: c.MAX_BYTES=old
    def test_headers_preserve_locations_and_security(self):
        raw=(ROOT/'deploy/single-vps/nginx/listening-authenticated-admin-https.conf.template').read_bytes()
        result=c.render_site(raw,'/opt/atstudio/current/frontend','/opt/atstudio/releases/test/frontend').decode()
        self.assertEqual(result.count('add_header Cache-Control $ats_html_cache always;'),2)
        self.assertEqual(result.count('add_header X-Frame-Options DENY always;'),4)
        self.assertIn('"~^200:/assets/',result)
        self.assertIn('default "no-store";',result)
        self.assertIn('try_files $uri =404;',result)
        self.assertEqual(result.count('include /etc/atstudio/nginx/proxy.conf;'),raw.decode().count('include /etc/atstudio/nginx/proxy.conf;'))
        self.assertNotIn('$request_uri" "public',result)
    def test_baseline_drift_and_double_render_rejected(self):
        raw=(ROOT/'deploy/single-vps/nginx/listening-authenticated-admin-https.conf.template').read_bytes()
        result=c.render_site(raw,'/opt/atstudio/current/frontend','/opt/atstudio/releases/test/frontend')
        with self.assertRaisesRegex(ValueError,'ALREADY_MANAGED'): c.render_site(result,'/opt/atstudio/releases/test/frontend','/opt/atstudio/releases/test2/frontend')


if __name__ == '__main__': unittest.main()
