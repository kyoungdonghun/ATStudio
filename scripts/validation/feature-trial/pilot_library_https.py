"""Bounded HTTPS acceptance with the two dedicated WI010 QA identities.

Run only after the owner has reconciled current grants and session custody for
IDs 5 and 6. The public check never loads credentials. Persistent receipts contain
IDs and a generated playlist marker only. An interrupted run resumes in cleanup.
"""
import argparse
import contextlib
import http.client
import json
import os
from pathlib import Path
import ssl
import sys
import uuid


ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'build' / 'WI-20261123-ATS-018'
RECEIPT = WORK / 'qa-library-receipt.json'
HOST = 'atmusic.co.kr'
ACCOUNTS = (5, 6)


class CheckFailed(Exception):
    pass


def require(ok, code):
    if not ok:
        raise CheckFailed(code)


def https(method, path, *, token=None, data=None, title=None, expected=(200,), media=False):
    headers = {'User-Agent': 'ATStudio-pilot-library-QA', 'Accept-Encoding': 'identity'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    if media:
        headers['Range'] = 'bytes=0-255'
    payload = None
    if title is not None:
        boundary = uuid.uuid4().hex
        headers['Content-Type'] = 'multipart/form-data; boundary=' + boundary
        payload = (f'--{boundary}\r\nContent-Disposition: form-data; name="title"\r\n\r\n'
                   f'{title}\r\n--{boundary}--\r\n').encode()
    elif data is not None:
        headers['Content-Type'] = 'application/json'
        payload = json.dumps(data).encode()
    conn = http.client.HTTPSConnection(HOST, timeout=25, context=ssl.create_default_context())
    try:
        conn.request(method, path, payload, headers)
        res = conn.getresponse()
        require(res.status in expected, f'{method} {path} HTTP_{res.status}')
        if media:
            require(res.status == 206 and (res.getheader('Content-Range') or '').startswith('bytes 0-255/'), 'PUBLIC_RANGE')
            require(len(res.read(257)) == 256, 'PUBLIC_AUDIO_BYTES')
            return
        limit = 2_000_000
        body = res.read(limit + 1)
        require(len(body) <= limit, 'RESPONSE_SIZE')
        if not body:
            return None
        return json.loads(body)
    finally:
        conn.close()


def public_tracks():
    rows = https('GET', '/api/tracks')['dataList']
    ids = [r['id'] for r in rows]
    require(ids and all(type(i) is int and i > 0 for i in ids), 'NO_PUBLIC_TRACK')
    https('GET', f'/api/tracks/{ids[0]}/stream', expected=(206,), media=True)
    return ids


def save(receipt):
    temp = RECEIPT.with_suffix('.tmp')
    with temp.open('w', encoding='utf-8') as out:
        json.dump(receipt, out)
        out.flush()
        os.fsync(out.fileno())
    os.replace(temp, RECEIPT)


def lists(path, token):
    return https('GET', '/api/' + path, token=token)['dataList']


def snapshot(token):
    return {
        'likes': sorted(row['trackId'] for row in lists('likes', token)),
        'playlists': sorted(row['id'] for row in lists('playlists', token)),
        'quota': https('GET', '/api/utils/download-count', token=token)['data']['remaining'],
    }


def qa_credentials():
    sys.path.insert(0, str(ROOT / 'build' / 'WI-20261121-ATS-010'))
    from pilot_fixture import CUSTODY, dpapi, read
    original = read()['users']
    current = json.loads(dpapi((CUSTODY.parent / 'ID5-20261130.dpapi').read_bytes(), True))
    require(len(original) == 2 and current['email'] == original[0]['email']
            and original[1]['email'] != current['email']
            and all(row['email'].endswith('@example.invalid') for row in original), 'QA_CUSTODY')
    return [current, original[1]]


def login(credentials, account):
    private = credentials[ACCOUNTS.index(account)]
    result = https('POST', '/api/auth/login',
                   data={'email': private['email'], 'password': private['password']})
    return result['data']['accessToken']


def confirm_owner(token, account, credentials):
    me = https('GET', '/api/users/me', token=token)['data']
    require(me['id'] == account and me['role'] == 'USER' and
            me['email'] == credentials[ACCOUNTS.index(account)]['email'], 'QA_IDENTITY')


def detail_ids(token, playlist_id):
    detail = https('GET', f'/api/playlists/{playlist_id}', token=token)['data']
    return [item['trackId'] for item in detail['tracks']]


def start(tokens, public):
    before = {str(i): snapshot(tokens[i]) for i in ACCOUNTS}
    capacity = https('GET', '/api/playlists/capacity', token=tokens[5])['data']['maxPlaylists']
    require(len(before['5']['playlists']) < capacity, 'QA_PLAYLIST_FULL')
    choices = [i for i in public if all(i not in before[str(a)]['likes'] for a in ACCOUNTS)]
    require(len(choices) >= 2, 'NEED_TWO_UNLIKED_PUBLIC_TRACKS')
    marker = 'ATStudio QA ' + uuid.uuid4().hex
    require(all(p['title'] != marker for p in lists('playlists', tokens[5])), 'MARKER_COLLISION')
    receipt = {'state': 'started', 'marker': marker, 'before': before, 'tracks': choices[:2],
               'like_intent': [], 'playlist_intent': False}
    save(receipt)
    return receipt


def trial(receipt, tokens, credentials):
    x, y = receipt['tracks']
    for account, track in zip(ACCOUNTS, [x, y]):
        receipt['like_intent'].append(account)
        save(receipt)
        https('POST', f'/api/likes/{track}', token=tokens[account], expected=(201,))
    receipt['playlist_intent'] = True
    save(receipt)
    created = https('POST', '/api/playlists', token=tokens[5], title=receipt['marker'], expected=(201,))
    playlist_id = created['data']['id']
    require(type(playlist_id) is int and playlist_id > 0 and
            playlist_id not in receipt['before']['5']['playlists'], 'NEW_LIST_ID')
    base = f'/api/playlists/{playlist_id}'
    for track in [x, y]:
        https('POST', base + '/tracks', token=tokens[5], data={'trackId': track}, expected=(201,))
    https('PUT', base + '/tracks', token=tokens[5],
          data={'tracks': [{'trackId': y, 'trackOrder': 0}, {'trackId': x, 'trackOrder': 1}]})
    https('POST', '/api/auth/logout', token=tokens[5], expected=(204,))
    del tokens[5]
    tokens[5] = login(credentials, 5)
    confirm_owner(tokens[5], 5, credentials)
    require(detail_ids(tokens[5], playlist_id) == [y, x], 'SAVED_ORDER')
    for account, track in zip(ACCOUNTS, [x, y]):
        now = snapshot(tokens[account])
        old = receipt['before'][str(account)]
        require(now['likes'] == sorted(old['likes'] + [track]), 'OWN_LIKES_ONLY')
        require(now['playlists'] == sorted(old['playlists'] + ([playlist_id] if account == 5 else [])),
                'OWN_PLAYLISTS_ONLY')
        require(now['quota'] == old['quota'], 'QUOTA_CHANGED')
    https('GET', f'/api/tracks/{y}/stream', expected=(206,), media=True)
    receipt['trial_passed'] = True
    save(receipt)


def restore(receipt, tokens):
    # GET reconciliation always precedes a cleanup write, including pending writes
    # whose response was lost. No unrelated track or playlist may be removed.
    if receipt['playlist_intent']:
        owned = [p for p in lists('playlists', tokens[5]) if p['title'] == receipt['marker']]
        require(len(owned) <= 1, 'AMBIGUOUS_MARKER')
        if owned:
            pid = owned[0]['id']
            require(pid not in receipt['before']['5']['playlists'], 'BASELINE_LIST')
            https('DELETE', f'/api/playlists/{pid}', token=tokens[5], expected=(204,))
    for account, track in zip(ACCOUNTS, receipt['tracks']):
        if account not in receipt['like_intent']:
            continue
        require(track not in receipt['before'][str(account)]['likes'], 'BASELINE_LIKE')
        # An unpublished track is invisible even when a like row still exists.
        require(https('GET', f'/api/tracks/{track}')['data']['id'] == track, 'PUBLICATION_CHANGED')
        if any(row['trackId'] == track for row in lists('likes', tokens[account])):
            https('DELETE', f'/api/likes/{track}', token=tokens[account], expected=(204,))
    for account in ACCOUNTS:
        require(snapshot(tokens[account]) == receipt['before'][str(account)], 'BASELINE_MISMATCH')
    receipt['state'] = 'restored'
    save(receipt)


@contextlib.contextmanager
def operator_lock():
    import msvcrt
    WORK.mkdir(parents=True, exist_ok=True)
    with (WORK / 'qa-library.lock').open('a+b') as lock:
        if lock.tell() == 0:
            lock.write(b'0')
            lock.flush()
        lock.seek(0)
        msvcrt.locking(lock.fileno(), msvcrt.LK_NBLCK, 1)
        yield


def execute(mode):
    if mode == 'public':
        public_tracks()
        print('PUBLIC_CATALOG_AND_RANGE_PASS')
        return
    with operator_lock():
        receipt = json.loads(RECEIPT.read_text('utf-8')) if RECEIPT.exists() else None
        if receipt and receipt['state'] == 'restored':
            require(receipt.get('logout_confirmed'), 'BASELINE_RESTORED_VERIFY_QA_LOGOUT')
            print('ALREADY_RESTORED', 'TRIAL_PASS' if receipt.get('trial_passed') else 'TRIAL_INCOMPLETE')
            require(mode != 'run' or receipt.get('trial_passed'), 'PREVIOUS_TRIAL_INCOMPLETE')
            return
        require(not receipt or mode == 'cleanup', 'RECEIPT_PENDING_USE_CLEANUP')
        require(mode != 'cleanup' or receipt, 'NO_CLEANUP_RECEIPT')
        credentials = qa_credentials()
        tokens = {}
        logout_failed = []
        try:
            for account in ACCOUNTS:
                tokens[account] = login(credentials, account)
                confirm_owner(tokens[account], account, credentials)
            if mode == 'run':
                receipt = start(tokens, public_tracks())
                try:
                    trial(receipt, tokens, credentials)
                finally:
                    if set(tokens) == set(ACCOUNTS):
                        restore(receipt, tokens)
            else:
                restore(receipt, tokens)
        finally:
            for account, token in tokens.items():
                try:
                    https('POST', '/api/auth/logout', token=token, expected=(204,))
                except Exception:
                    logout_failed.append(account)
            require(not logout_failed, 'QA_LOGOUT_UNCONFIRMED')
            if receipt and receipt.get('state') == 'restored':
                receipt['logout_confirmed'] = True
                save(receipt)
        print('LIBRARY_TRIAL_PASS' if receipt.get('trial_passed') else 'TRIAL_INCOMPLETE',
              'BASELINE_RESTORED', 'QA_LOGOUT_PASS')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('mode', choices=['public', 'run', 'cleanup'])
    parser.add_argument('--dedicated-qa-ready', action='store_true',
                        help='Owner has verified that ID 5/6 are the eligible dedicated idle QA pair')
    args = parser.parse_args()
    try:
        require(args.mode == 'public' or args.dedicated_qa_ready, 'QA_PREFLIGHT_REQUIRED')
        execute(args.mode)
    except Exception as exc:
        print('HOLD', str(exc) if isinstance(exc, CheckFailed) else type(exc).__name__)
        raise SystemExit(1)
