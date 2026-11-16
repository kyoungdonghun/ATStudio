#!/usr/bin/python3 -I
"""Reload the live listening vhost for the exact apex+www certificate lineage."""
import os
from pathlib import Path
import re
import stat
import subprocess
import sys

DOMAINS = frozenset(('atmusic.co.kr', 'www.atmusic.co.kr'))
LINEAGE = Path('/etc/letsencrypt/live/atmusic.co.kr')
ARCHIVE = Path('/etc/letsencrypt/archive/atmusic.co.kr')
CONFIG = Path('/etc/atstudio/nginx/nginx.conf')
HOOK = Path('/etc/letsencrypt/renewal-hooks/deploy/40-atstudio-quarantine')
DROPIN = Path('/etc/systemd/system/nginx.service.d/99-atstudio-listening.conf')

def need(ok, code):
    if not ok:
        raise ValueError(code)

def trusted(path, private=False, directory=False):
    path = Path(path)
    for p in [*reversed(path.parents), path]:
        s = p.lstat()
        need(not stat.S_ISLNK(s.st_mode) and s.st_uid == 0 and s.st_gid == 0
             and not s.st_mode & 0o022, 'METADATA')
    info = path.lstat()
    need((stat.S_ISDIR(info.st_mode) if directory else stat.S_ISREG(info.st_mode)), 'TYPE')
    if not directory:
        need(info.st_nlink == 1, 'LINK_COUNT')
    if private:
        need(not info.st_mode & 0o077, 'PRIVATE_MODE')
    return path

def check_event(env):
    raw = env.get('RENEWED_DOMAINS', '')
    names = raw.split(' ')
    need(len(names) == 2 and frozenset(names) == DOMAINS
         and env.get('RENEWED_LINEAGE') == str(LINEAGE), 'EVENT')

def check_san(raw):
    match = re.fullmatch(rb'X509v3 Subject Alternative Name:(?: critical)?\s+([^\r\n]+)\s*', raw)
    need(match is not None, 'SAN')
    names = match[1].strip().split(b', ')
    need(len(names) == 2 and set(names) == {b'DNS:' + n.encode() for n in DOMAINS}, 'SAN')

def check_lineage():
    trusted(LINEAGE.parent, private=True, directory=True)
    trusted(ARCHIVE.parent, private=True, directory=True)
    trusted(LINEAGE, directory=True)
    trusted(ARCHIVE, directory=True)
    generations = []
    for stem in ('fullchain', 'privkey'):
        link = LINEAGE / (stem + '.pem')
        s = link.lstat()
        need(stat.S_ISLNK(s.st_mode) and s.st_uid == 0 and s.st_gid == 0 and s.st_nlink == 1, 'LINK')
        target = os.readlink(link)
        m = re.fullmatch(r'\.\./\.\./archive/atmusic\.co\.kr/' + stem + r'([1-9][0-9]{0,6})\.pem', target)
        need(m is not None, 'LINK_TARGET')
        archived = trusted(ARCHIVE / (stem + m[1] + '.pem'), private=(stem == 'privkey'))
        need(0 < archived.stat().st_size <= 65536, 'SIZE')
        generations.append(m[1])
    need(len(set(generations)) == 1, 'PAIR')

def run(argv, capture=False):
    p = subprocess.run(argv, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                       stderr=subprocess.PIPE, timeout=20,
                       env={'PATH': '/usr/sbin:/usr/bin', 'LC_ALL': 'C'})
    need(p.returncode == 0 and len(p.stdout) <= 8192, 'COMMAND')
    return p.stdout if capture else b''

def reload_checked(env):
    check_event(env)
    for p in (CONFIG, HOOK, DROPIN, Path('/usr/sbin/nginx'), Path('/usr/bin/systemctl'), Path('/usr/bin/openssl')):
        trusted(p)
    check_lineage()
    cert = str(LINEAGE / 'fullchain.pem')
    check_san(run(['/usr/bin/openssl', 'x509', '-in', cert, '-noout', '-ext', 'subjectAltName'], True))
    for domain in sorted(DOMAINS):
        run(['/usr/bin/openssl', 'verify', '-purpose', 'sslserver', '-verify_hostname', domain,
             '-untrusted', cert, cert])
    # The systemd reload must target the same live config checked here.
    raw = trusted(DROPIN).read_text()
    need('[Service]' in raw and 'ExecStart=/usr/sbin/nginx -c /etc/atstudio/nginx/nginx.conf' in raw,
         'ACTIVE_CONFIG')
    run(['/usr/sbin/nginx', '-t', '-q', '-c', str(CONFIG)])
    run(['/usr/bin/systemctl', 'is-active', '--quiet', 'nginx.service'])
    run(['/usr/bin/systemctl', 'reload', 'nginx.service'])
    run(['/usr/bin/systemctl', 'is-active', '--quiet', 'nginx.service'])

def main():
    try:
        need(sys.platform == 'linux' and os.geteuid() == 0, 'PLATFORM')
        reload_checked(os.environ)
    except (ValueError, OSError, subprocess.SubprocessError):
        print('TLS_LISTENING_HOLD')
        return 1
    print('TLS_LISTENING_RELOAD_REQUESTED')
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
