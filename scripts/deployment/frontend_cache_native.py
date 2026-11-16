"""WI002 exact-target frontend-only native apply. Executed only by approved MA transport."""
import hashlib, json, os, re, signal, stat, subprocess, sys, tarfile, time
from pathlib import Path, PurePosixPath
import urllib.request
import fcntl
OLD = Path('/opt/atstudio/releases/20261116-publish-phase1')
NEW = Path('/opt/atstudio/releases/20261117-cache-phase1')
BASE = Path('/var/lib/atstudio-site-deploy/20261117-cache-phase1')
UNIT = Path('/etc/systemd/system/atstudio.service')
SITE = Path('/etc/atstudio/nginx/active-site.conf')
CONFIGS = [UNIT, Path('/etc/systemd/system/atstudio-db.service'), SITE, Path('/etc/atstudio/nginx/nginx.conf'), Path('/etc/atstudio/nginx/proxy.conf'), Path('/etc/systemd/system/nginx.service.d/99-atstudio-listening.conf'), Path('/etc/letsencrypt/renewal-hooks/deploy/40-atstudio-quarantine'), Path('/etc/letsencrypt/renewal/atmusic.co.kr.conf')]
UNITS = ['atstudio.service', 'atstudio-db.service', 'nginx.service']

def tls_identity():
    cert = Path('/etc/letsencrypt/live/atmusic.co.kr/fullchain.pem')
    target = cert.resolve(strict=True)
    need(target.parent == Path('/etc/letsencrypt/archive/atmusic.co.kr'), 'CERT_LINEAGE')
    return {'fullchain_sha256': sha(trusted(target))}

def need(v, code):
    if not v:
        raise ValueError(code)

def digest(raw):
    return hashlib.sha256(raw).hexdigest()

def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as f:
        for b in iter(lambda: f.read(1048576), b''):
            h.update(b)
    return h.hexdigest()

def trusted(path):
    p = Path(path)
    for q in [*reversed(p.parents), p]:
        s = q.lstat()
        need(not stat.S_ISLNK(s.st_mode) and s.st_uid == 0 and (not s.st_mode & 18), 'PATH_TRUST')
    return p

def run(args, timeout=30):
    p = subprocess.run(args, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout, env={'PATH': '/usr/sbin:/usr/bin:/sbin:/bin', 'LC_ALL': 'C'})
    need(p.returncode == 0, 'COMMAND_FAILED')
    return p.stdout

def state(unit):
    raw = run(['/usr/bin/systemctl', 'show', unit, '--property=ActiveState,SubState,MainPID,ControlPID,FragmentPath,InvocationID,UnitFileState', '--no-pager'])
    return dict((x.split('=', 1) for x in raw.decode().splitlines() if '=' in x))

def identity():
    return {k: digest(Path(p).read_bytes()) for k, p in [('machine', '/etc/machine-id'), ('boot', '/proc/sys/kernel/random/boot_id')]}

def inspect():
    need(sys.platform == 'linux' and os.geteuid() == 0, 'LINUX_ROOT')
    configs = {str(p): sha(trusted(p)) for p in CONFIGS}
    services = {u: state(u) for u in UNITS}
    for u, s in services.items():
        need(s['ActiveState'] == 'active' and s['MainPID'] != '0' and (s['ControlPID'] == '0'), 'SERVICE_NOT_STABLE')
        need(s['UnitFileState'] == 'enabled', 'SERVICE_NOT_ENABLED')
    need(services['atstudio.service']['FragmentPath'] == str(UNIT), 'APP_FRAGMENT')
    cmd = Path('/proc/' + services['atstudio.service']['MainPID'] + '/cmdline').read_bytes().split(b'\x00')
    idx = cmd.index(b'-jar')
    need(cmd[idx + 1].decode() == str(OLD / 'app.jar'), 'APP_RELEASE_CHANGED')
    files = {p.relative_to(OLD / 'frontend').as_posix(): sha(trusted(p)) for p in sorted((OLD / 'frontend').rglob('*')) if p.is_file()}
    need('index.html' in files and 0 < len(files) < 500, 'FRONTEND_LAYOUT')
    return {'status': 'READ_ONLY_PREFLIGHT', 'observed_at': int(time.time()), 'identity': identity(), 'configs': configs, 'services': services, 'old_jar_sha256': sha(trusted(OLD / 'app.jar')), 'old_frontend': files, 'tls': tls_identity(), 'free_bytes': os.statvfs(OLD).f_bavail * os.statvfs(OLD).f_frsize}


def newfile(p, raw, mode=0o600):
    trusted(p.parent)
    fd = os.open(p, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, mode)
    with os.fdopen(fd, 'wb') as f:
        f.write(raw); f.flush(); os.fsync(f.fileno())


def replace_exact(p, expected, raw):
    trusted(p); need(sha(p) == expected, 'CONFIG_OWNERSHIP')
    tmp = BASE / ('site-next-' + str(time.time_ns()))
    newfile(tmp, raw, 0o644)
    need(sha(p) == expected, 'CONFIG_RACE')
    os.replace(tmp, p)
    fd = os.open(p.parent, os.O_RDONLY | os.O_DIRECTORY)
    try: os.fsync(fd)
    finally: os.close(fd)


def unchanged(before, site_pin):
    current = inspect()
    expected = dict(before['configs']); expected[str(SITE)] = site_pin
    need(current['configs'] == expected, 'CONFIG_DRIFT')
    for key in ('identity', 'services', 'old_jar_sha256', 'old_frontend', 'tls'):
        need(current[key] == before[key], 'RUNTIME_DRIFT')
    return current


def contents(spec):
    archive = trusted(BASE / 'payload.tar')
    need(archive.stat().st_size <= 70 * 1024 * 1024 and sha(archive) == spec['archive_sha256'], 'ARCHIVE_PIN')
    with tarfile.open(archive, 'r:') as tar:
        members = tar.getmembers(); names = [m.name for m in members]
        need(3 <= len(names) <= 515 and len(set(names)) == len(names), 'ARCHIVE_MEMBERS')
        for m in members:
            need(m.isfile() and 0 < m.size <= 64 * 1024 * 1024, 'ARCHIVE_TYPE')
            need(m.name in ('manifest.json', 'active-site.conf', 'frontend/index.html') or
                 re.fullmatch(r'frontend/assets/[A-Za-z0-9_.-]+-[A-Za-z0-9_-]{8,}\.(?:js|css|png|jpg|jpeg|webp|svg|woff2?)', m.name), 'ARCHIVE_SCOPE')
        need(sum(m.size for m in members) <= 65 * 1024 * 1024, 'ARCHIVE_BOUND')
        raw = tar.extractfile('manifest.json').read()
        need(digest(raw) == spec['manifest_sha256'], 'MANIFEST_PIN')
        manifest = json.loads(raw)
        need(set(manifest) == set(names) - {'manifest.json'} and
             {'active-site.conf', 'frontend/index.html'} <= set(manifest), 'MANIFEST_LAYOUT')
        result = {n: tar.extractfile(n).read() for n in manifest}
        need(all(re.fullmatch('[0-9a-f]{64}', pin) and digest(result[n]) == pin for n, pin in manifest.items()), 'MEMBER_PIN')
        return result


def apply(spec):
    need(set(spec) == {'action','expires_at','identity','preflight','archive_sha256','manifest_sha256'}, 'SPEC_KEYS')
    need(spec['action'] == 'apply' and type(spec['expires_at']) is int and 0 < spec['expires_at'] - time.time() <= 900, 'SPEC_WINDOW')
    before = spec['preflight']
    need(0 <= time.time() - before['observed_at'] <= 1800 and spec['identity'] == identity(), 'PREFLIGHT_WINDOW')
    need(before['old_jar_sha256'] == 'feff6919a291caff985d0c386d23ba30ee53ff0b2a9a32fd5d162208bf401a91', 'APP_PIN')
    need(before['configs'][str(SITE)] == '4772ec565862b9eb3b38eca6786b32b2dc7cd114ccfc0aab50fd43bb4ae1086e', 'SITE_PIN')
    current = unchanged(before, before['configs'][str(SITE)])
    need(current['free_bytes'] >= 256 * 1024 * 1024, 'FREE_SPACE')
    trusted(BASE); trusted(NEW.parent)
    need(not NEW.exists() and not NEW.is_symlink(), 'FRESH_RELEASE')
    data = contents(spec); oldsite = SITE.read_bytes(); newsite = data.pop('active-site.conf')
    need(newsite.count(str(NEW / 'frontend').encode()) == 1 and b'# BEGIN ATSTUDIO CACHE V1' in newsite, 'SITE_TARGET')
    newfile(BASE / 'apply.claim', b'exclusive attempt\n')
    changed = False
    try:
        NEW.mkdir(mode=0o755); (NEW / 'frontend').mkdir(mode=0o755); (NEW / 'frontend/assets').mkdir(mode=0o755)
        for name, raw in data.items(): newfile(NEW / name, raw, 0o644)
        need(all(sha(trusted(NEW / n)) == digest(v) for n, v in data.items()), 'EXTRACTED_PIN')
        newfile(BASE / 'previous-site.conf', oldsite)
        newfile(BASE / 'candidate-site.conf', newsite, 0o644)
        wrapper = Path('/etc/atstudio/nginx/nginx.conf').read_bytes()
        target = str(SITE).encode(); need(wrapper.count(target) == 1, 'WRAPPER_INCLUDE')
        candidate = BASE / 'candidate-nginx.conf'
        newfile(candidate, wrapper.replace(target, str(BASE / 'candidate-site.conf').encode()), 0o644)
        run(['/usr/sbin/nginx', '-t', '-c', str(candidate)])
        unchanged(before, digest(oldsite))
        replace_exact(SITE, digest(oldsite), newsite); changed = True
        run(['/usr/sbin/nginx', '-t', '-c', '/etc/atstudio/nginx/nginx.conf'])
        run(['/usr/bin/systemctl', 'reload', 'nginx.service'])
        final = unchanged(before, digest(newsite))
        result = {'status':'FRONTEND_CACHE_APPLIED','site_sha256':digest(newsite),
                  'frontend':{n.removeprefix('frontend/'):digest(v) for n,v in data.items()},
                  'runtime':final,'app_db_restarted':False,'previous_release_preserved':True}
        newfile(BASE / 'apply.result.json', json.dumps(result,sort_keys=True).encode())
        return result
    except BaseException:
        for sig in (signal.SIGTERM, signal.SIGHUP, signal.SIGINT): signal.signal(sig, signal.SIG_IGN)
        rollback = 'NO_ACTIVE_CHANGE'
        # replace/fsync may fail after rename; reconcile ownership before rollback.
        if changed or sha(trusted(SITE)) == digest(newsite):
            try:
                replace_exact(SITE, digest(newsite), oldsite)
                run(['/usr/sbin/nginx','-t','-c','/etc/atstudio/nginx/nginx.conf'])
                run(['/usr/bin/systemctl','reload','nginx.service'])
                unchanged(before, digest(oldsite)); rollback = 'SITE_RESTORED_NO_APP_DB_RESTART'
            except BaseException: rollback = 'HOLD_INSPECT_OWNERSHIP_AND_RUNTIME'
        newfile(BASE / 'apply.failed.json', json.dumps({'status':'FAILED_PRESERVED','rollback':rollback}).encode())
        raise


def interrupted(signum, frame): raise InterruptedError('INTERRUPTED')


if __name__ == '__main__':
    for sig in (signal.SIGTERM,signal.SIGHUP,signal.SIGINT): signal.signal(sig,interrupted)
    try:
        raw = sys.stdin.buffer.read(2097153); need(len(raw) <= 2097152, 'INPUT_BOUND')
        spec = json.loads(raw)
        if spec == {'action':'inspect'}: result = inspect()
        else:
            # Serialize with this exact native tool; never overwrite a consumed claim.
            trusted(BASE)
            fd = os.open(BASE, os.O_RDONLY | os.O_DIRECTORY)
            try:
                fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
                result = apply(spec)
            finally: os.close(fd)
        print(json.dumps(result,sort_keys=True))
    except Exception as e:
        code = str(e) if type(e) is ValueError and re.fullmatch('[A-Z_]+',str(e)) else type(e).__name__
        print(json.dumps({'status':'HOLD','code':code})); sys.exit(1)
