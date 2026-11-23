"""Exercise the acceptance journal and cleanup with no external connection."""
import json
import sys
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch

import pilot_library_https as qa


class CredentialBinding(unittest.TestCase):
    def test_rotated_password_is_bound_to_the_original_qa_owner(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            old_a = dict(email='a@example.invalid', password='old-fixture')
            old_b = dict(email='b@example.invalid', password='second-fixture')
            rotated = dict(email=old_a['email'], password='rotated-fixture')
            ref = root / 'ID5-20261130.dpapi'
            ref.write_text(json.dumps(rotated), encoding='utf-8')
            fixture = SimpleNamespace(CUSTODY=root / 'original', dpapi=lambda blob, decrypt: blob,
                                      read=lambda: {'users': [old_a, old_b]})
            with patch.dict(sys.modules, pilot_fixture=fixture), patch.object(sys, 'path', list(sys.path)):
                self.assertEqual(qa.qa_credentials(), [rotated, old_b])
                ref.write_text(json.dumps(dict(rotated, email=old_b['email'])), encoding='utf-8')
                with self.assertRaisesRegex(qa.CheckFailed, 'QA_CUSTODY'):
                    qa.qa_credentials()


class LocalServer:
    def __init__(self):
        self.likes = {5: {90}, 6: {91}}
        self.playlists = {5: {81: {'id': 81, 'title': 'existing', 'tracks': [90]}}, 6: {}}
        self.limit = 3
        self.loss = False
        self.public = {1, 2, 90, 91}

    def call(self, method, path, *, token=None, data=None, title=None, expected=(200,), media=False):
        if path == '/api/auth/login':
            return {'data': {'accessToken': str(data['email'])}}
        if path == '/api/auth/logout':
            return
        if path == '/api/tracks':
            return {'dataList': [{'id': i} for i in sorted(self.public)]}
        if path.startswith('/api/tracks/'):
            track = int(path.split('/')[3])
            qa.require(track in self.public, 'UNPUBLISHED')
            return {'data': {'id': track}}
        user = int(token)
        if path == '/api/users/me':
            return {'data': {'id': user, 'email': token, 'role': 'USER'}}
        if path == '/api/utils/download-count':
            return {'data': {'remaining': 4}}
        if path == '/api/playlists/capacity':
            return {'data': {'maxPlaylists': self.limit}}
        if path == '/api/likes':
            return {'dataList': [{'trackId': i} for i in self.likes[user] & self.public]}
        if path.startswith('/api/likes/'):
            track = int(path.split('/')[-1])
            if method == 'POST':
                self.likes[user].add(track)
            elif method == 'DELETE':
                self.likes[user].remove(track)
            else:
                raise AssertionError(method)
            return
        if path == '/api/playlists' and method == 'GET':
            return {'dataList': list(self.playlists[user].values())}
        if path == '/api/playlists' and method == 'POST':
            self.playlists[user][101] = {'id': 101, 'title': title, 'tracks': []}
            if self.loss:
                raise OSError('synthetic lost response')
            return {'data': {'id': 101}}
        if path.startswith('/api/playlists/'):
            assert path.split('/')[3] == '101'
            item = self.playlists[user][101]
            if method == 'DELETE':
                del self.playlists[user][101]
            elif method == 'POST':
                item['tracks'].append(data['trackId'])
            elif method == 'PUT':
                item['tracks'] = [t['trackId'] for t in data['tracks']]
            elif method == 'GET':
                return {'data': {'tracks': [{'trackId': x} for x in item['tracks']]}}
            else:
                raise AssertionError(method)
            return
        raise AssertionError(path)


class Reconciliation(unittest.TestCase):
    def setUp(self):
        folder = tempfile.TemporaryDirectory()
        self.addCleanup(folder.cleanup)
        self.server = LocalServer()
        for mocked in (patch.object(qa, 'RECEIPT', Path(folder.name) / 'receipt.json'),
                       patch.object(qa, 'https', self.server.call)):
            mocked.start()
            self.addCleanup(mocked.stop)
        self.tokens = {5: '5', 6: '6'}
        self.credentials = [{'email': str(i), 'password': 'synthetic'} for i in qa.ACCOUNTS]
        self.before = {i: qa.snapshot(t) for i, t in self.tokens.items()}

    def test_normal_trial_and_restore(self):
        receipt = qa.start(self.tokens, qa.public_tracks())
        qa.trial(receipt, self.tokens, self.credentials)
        self.assertTrue(receipt['trial_passed'])
        qa.restore(receipt, self.tokens)
        self.assertEqual(receipt['state'], 'restored')
        self.assertEqual({i: qa.snapshot(t) for i, t in self.tokens.items()}, self.before)
        self.assertEqual(self.server.playlists[5][81]['tracks'], [90])

    def test_response_lost_after_create_reconcile_and_restore_without_reissuing(self):
        receipt = qa.start(self.tokens, qa.public_tracks())
        self.server.loss = True
        with self.assertRaises(OSError):
            qa.trial(receipt, self.tokens, self.credentials)
        receipt = json.loads(qa.RECEIPT.read_text())
        self.assertFalse(receipt.get('trial_passed'))
        qa.restore(receipt, self.tokens)
        qa.restore(receipt, self.tokens)
        self.assertEqual(self.before, {i: qa.snapshot(t) for i, t in self.tokens.items()})

    def test_no_room_does_not_mutate_any_existing_playlist(self):
        self.server.limit = 1
        with self.assertRaisesRegex(qa.CheckFailed, 'QA_PLAYLIST_FULL'):
            qa.start(self.tokens, qa.public_tracks())
        self.assertFalse(qa.RECEIPT.exists())

    def test_private_track_holds_cleanup_without_claiming_like_was_removed(self):
        receipt = qa.start(self.tokens, qa.public_tracks())
        qa.trial(receipt, self.tokens, self.credentials)
        self.server.public.remove(1)
        with self.assertRaisesRegex(qa.CheckFailed, 'UNPUBLISHED'):
            qa.restore(receipt, self.tokens)
        self.assertEqual(json.loads(qa.RECEIPT.read_text())['state'], 'started')


if __name__ == '__main__':
    unittest.main()
