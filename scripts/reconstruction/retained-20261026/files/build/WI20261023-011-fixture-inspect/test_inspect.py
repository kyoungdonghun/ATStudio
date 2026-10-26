"""Synthetic preparation tests only; no network, GUI input or database."""
import ast
import base64
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import time
import unittest
import uuid
import inspect_tools as tool
import remote_inspect as remote

HERE=Path(__file__).resolve().parent


def record():
    return dict(wi='WI-20261023-ATS-011',scope=tool.SCOPE,mode='inspect',
        decision_reference='CURRENT_EXPLICIT_USER_INSPECT_ONLY_APPROVAL',candidate_sha256=tool.PIN,
        boot_id='11111111-1111-1111-1111-111111111111',machine_id_sha256='a'*64,nonce='b'*32,
        issued_at=int(time.time()),expires_at=int(time.time())+600,verified_test_semantics_approved=True,allow_writes=False)


def private():
    return dict(admin=dict(email='admin@example.invalid',nickname='ats_test_admin',password='SYNTHETIC-admin-2026!'),
        buyer=dict(email='buyer@example.invalid',nickname='ats_test_buyer',password='SYNTHETIC-buyer-2026!'))


class Tests(unittest.TestCase):
    @unittest.skipUnless(os.name=='nt','Windows private ACL boundary')
    def test_private_preparation_ready_and_builder_without_human_or_network(self):
        directory=Path(os.environ['LOCALAPPDATA'])/('ATStudio-WI011-synthetic-flow-'+uuid.uuid4().hex)
        result=subprocess.run(['powershell.exe','-NoProfile','-NonInteractive','-ExecutionPolicy','Bypass','-File',
            str(HERE/'capture.ps1'),'-Action','Prepare','-PrivateDirectory',str(directory)],capture_output=True)
        self.assertEqual((result.returncode,result.stdout.strip()),(0,b'PRIVATE_DIRECTORY_READY'))
        value=record();raw=json.dumps(value,indent=2).encode()
        (directory/'operator-record.json').write_bytes(raw)
        (directory/'operator-record.sha256').write_text(hashlib.sha256(raw).hexdigest())
        (directory/'stage-result.json').write_bytes(tool.canonical(dict(status='INSPECT_STAGING_READY',nonce=value['nonce'],candidate_sha256=tool.PIN)))
        self.assertEqual(tool.ready(directory),value)
        self.assertEqual(tool.pinned_record(directory)[0],raw)
        (directory/'inputs.json').write_bytes(tool.canonical(private()))
        result=subprocess.run([sys.executable,'-I','-S','-B',str(HERE/'inspect_tools.py'),'inspect',
            '--private-directory',str(directory)],capture_output=True)
        self.assertEqual((result.returncode,result.stdout.strip(),result.stderr),(0,b'PRIVATE_PAYLOAD_READY',b''))
        self.assertTrue((directory/'inspect-payload.py').exists())
        self.assertNotIn(b'SYNTHETIC',result.stdout)

    def test_frozen_22_and_stdin_no_leak(self):
        self.assertEqual(len(tool.fixture_files()),23)
        raw=tool.canonical(private())
        result=subprocess.run([sys.executable,'-I','-S','-B',str(HERE/'inspect_tools.py'),'validate'],input=raw,capture_output=True)
        self.assertEqual((result.returncode,result.stdout.strip(),result.stderr),(0,b'INPUT_VALID',b''))
        self.assertNotIn(b'SYNTHETIC',result.stdout+result.stderr)

    def test_exact_contract_and_oversize_password(self):
        core=tool.core();core.inputs(private())
        for field,value in (('email','bad'),('nickname','bad'),('password','a'*73),('password','a'*11),('password','a'*11+' ')):
            bad=private();bad['admin'][field]=value
            with self.assertRaises(ValueError):core.inputs(bad)
        bad=private();bad['buyer']['email']=bad['admin']['email'].upper()
        with self.assertRaises(ValueError):core.inputs(bad)

    def test_record_requires_current_inspect_scope_and_external_pin(self):
        tool.operator_record(record())
        for changes in ({'allow_writes':True},{'mode':'apply'},{'expires_at':0},{'expires_at':int(time.time())+901},
                {'verified_test_semantics_approved':False}):
            with self.assertRaises(ValueError):tool.operator_record(dict(record(),**changes))
        raw=tool.canonical(record())
        with self.assertRaises(ValueError):tool.build_payload('inspect',raw,'0'*64,private())

    def test_payload_exact_sources_only_inspect_and_no_date(self):
        raw=tool.canonical(record());pin=hashlib.sha256(raw).hexdigest()
        for mode in ('stage','inspect'):
            payload=tool.build_payload(mode,raw,pin,private() if mode=='inspect' else None)
            compile(payload,'synthetic-payload','exec')
            tree=ast.parse(payload)
            headers={n.targets[0].id:ast.literal_eval(n.value) for n in tree.body[:6]}
            self.assertEqual(hashlib.sha256(base64.b64decode(headers['RECORD_B64'])).hexdigest(),pin)
            self.assertEqual(set(headers['FILES']),set(tool.fixture_files()))
        with self.assertRaises(ValueError):tool.build_payload('apply',raw,pin,private())
        code=(HERE/'remote_inspect.py').read_text()
        self.assertIn("native.native('inspect',",code)
        self.assertNotIn('core.plan(',code)
        self.assertNotIn('day_now(',code)
        self.assertNotIn("native.native('apply'",code)

    def test_public_projection_never_reports_private_values_or_implies_apply(self):
        value=dict(status='INSPECTED',admin_count=0,existing_work=0,collision_count=1,id1_eligible_unentitled=True,
            plans=[dict(id=1,user_type='INDIVIDUAL',is_active=1,download_per_day=5)],
            snapshot_sha256='PRIVATE_DIGEST',machine_id_sha256='PRIVATE_DIGEST')
        public=remote.project(value)
        self.assertEqual(public['readiness'],'HOLD')
        self.assertEqual(public['hold_reasons'],['IDENTITY_COLLISION'])
        self.assertFalse(public['apply_authorized']);self.assertFalse(public['date_selected'])
        self.assertNotIn('PRIVATE',json.dumps(public))
        value['collision_count']=0
        self.assertTrue(remote.project(value)['inspected_conditions_satisfied'])
        value['plans'][0]['download_per_day']=-1
        self.assertEqual(remote.project(value)['hold_reasons'],['NO_ELIGIBLE_PLAN'])

    def test_capture_ui_retains_input_after_expiry_and_has_no_password_truncation(self):
        text=(HERE/'capture.ps1').read_text()
        self.assertIn('UseSystemPasswordChar=$true',text)
        self.assertNotIn('MaxLength=72',text)
        handler=text.split('$button.Add_Click({')[1].split('[void]$form.ShowDialog')[0]
        self.assertNotIn("Invoke-Validator 'ready'",handler)
        self.assertIn("Invoke-Validator 'validate' $json",handler)
        self.assertIn("-cne $boxes[$actor+'_confirmation'].Text",handler)
        self.assertIn('CreateDirectoryW',text)
        self.assertIn('SetAccessRuleProtection($true,$false)',text)
        self.assertIn('[IO.FileMode]::CreateNew',text)

    def test_private_input_parse_rejects_duplicate_json_keys(self):
        with self.assertRaises(ValueError):json.loads('{"admin":1,"admin":2}',object_pairs_hook=tool.unique)


if __name__=='__main__':unittest.main(verbosity=2)
