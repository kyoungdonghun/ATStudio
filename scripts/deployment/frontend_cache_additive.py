"""WI002 exact-target frontend-only native apply. Executed only by approved MA transport."""
import hashlib, json, os, re, signal, stat, subprocess, sys, tarfile, time
from pathlib import Path, PurePosixPath
import urllib.request
import fcntl
OLD = Path('/opt/atstudio/releases/20261116-publish-phase1')
NEW = Path('/opt/atstudio/releases/20261117-cache-phase1')
BASE = Path('/var/lib/atstudio-site-deploy/20261117-cache-tag-additive')
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



EXPECTED = {'assets/AudioProcessingStatus-BbfLs2LQ.css': 'fd1e40b6771351573a4c11bd228238d24ddf262ab82e26a36a54cbaec1d11122', 'assets/AudioProcessingStatus-CeGho6-V.js': 'd3f284ac1677e33f7759f6fa39510726338a6222446c6058a0c120d0afc39b1a', 'assets/AudioProcessingStatus-DY9-fmES.js': '22f6886123b430e69e0adf46db533b60dca3755c6d6ecbef2c61b85eed84b9a3', 'assets/AudioProcessingStatus-URw9AXWq.js': 'f5fab6b8233379a61f2da1d2453c1c6239cda3a687422d65e5ea481593d08458', 'assets/CatalogImage-B2WBP9kZ.js': 'a33065c95233b3f29006a3627782362591ee57c85332fe101cab22ca692fa858', 'assets/CatalogImage-B9bMN6qv.js': 'cfb381592464552ec022f2c36c3c8c919090c8e58ce38640ec8a304c655982c5', 'assets/CatalogImage-DwaCk8mu.js': '7a0b85442fc5dc182b8f484d1c5d655084aec0e6c2119a47666cb859f060d902', 'assets/LoginPage-Crb6DZsx.js': 'd1544d0998faef0b2b71b64be9739c8b46019ef8c515de3ef7a58973aa5e396a', 'assets/LoginPage-D9n2N-sv.js': '5f70964a8940ad96b5cbc81bdef2fcacea7a0377430ffbfbc71c616e7122e3ad', 'assets/LoginPage-YXVOa97I.js': '858a60052f026800f83b86d248b46e8fe0abc4759205c4238b4abcbe67c5e7e8', 'assets/LoginPage-f6_JfC66.css': '1c447444032cdd3bff54a749499b1afb5fa23844b8b42f2f27d13e99cebe7b53', 'assets/NotFoundPage-BAja2pBx.js': '1e7a22e6c6ab08d466b4c255eea914f19a4fcd9e4f49569100f483d6c3b4b716', 'assets/NotFoundPage-BKgKEcsp.js': '8e5595935d42f246e4cf7a5240ec0f768e0fd41173a88de4d0346b03cddb4443', 'assets/NotFoundPage-CDm0Un2D.js': '0106092160aa77847ce6a0a9461f1be9169e90cf6cb94f7f5f583a9b8f625f89', 'assets/NotFoundPage-D3hmJ33E.css': '199a56f5d7268653c16b65e77b5b3749b1558e01e85afdd05cf4d0e4a7c2a3e4', 'assets/Pagination-CjKK9-XM.js': '5975b7551e9403ae03261f4436a0f9335c17d8c30b25561df49b277924b4e4e6', 'assets/Pagination-DRAtz-6K.js': '712f29f77e238b5c2d9e98dff238bc73a7ca5b55556026ce904568d10c462855', 'assets/Pagination-DTsGRP5N.js': '85cea6ca4aff41b8019a809769f8b749779e10c8a87d372e3bf77ab5d6f29eaa', 'assets/Pagination-MUhHXn0l.css': '14d6a182bf3f571be3b21b5d352b3276033ff363f83c79311e1795f219677cc2', 'assets/TagManagePage-C2PrL_kO.js': '189c53daa5a7c197d9d90c36a8ecc76498ccd845980923fcf19613a30aecea87', 'assets/TagManagePage-DWSLt7yh.js': '8141acef3a708a5fd36cb23862fc1dcb454390742adc6b467afae178692013c1', 'assets/TagManagePage-MMYjwISp.css': '01d6d6740255d2cb98a0c13998d878697db41c4e603807b48c66ec317ac71ec3', 'assets/TagManagePage-y5s6om4N.js': 'f4ef3e400816b74690653d070b6f8591d0b7804b06429296c390f84988e0caee', 'assets/TrackDetailPage-BsGO4seR.js': '02aed6d58d0019410463aaa4a168468824cea07758437d9a9b955147096b5976', 'assets/TrackDetailPage-C9L_v9yH.js': '78ab4f293b5ac0e22d34db0afe365c9c2fe3908b94fb300683c80393fb8a7f97', 'assets/TrackDetailPage-DrogRp62.js': '36ea65a0763f55b6f560e10b88a004f7e76fca81d26fda0dd51b008a337c5bf9', 'assets/TrackDetailPage-SBr0pXsM.css': '782b089aa2edf69f111326b508485ac886e0387535d8efe1280ec1f989bb32d0', 'assets/TrackEditPage-B3fbYwL7.js': '483be91ab7cf93f51053116fe52b5c1fc4427bb467582d04d86db522f53f1430', 'assets/TrackEditPage-C46re6eo.js': '3a5a7db413b7e0ed49be3d53a3a7aa9e9ce4131fcd42e0d0d80fae23631b856a', 'assets/TrackEditPage-h-VQ_5mA.js': 'abe6fa1472cd40344149a9814058d3366334176527f3414bdcc395e44f72e4b1', 'assets/TrackEditPage-mBX18Co2.css': 'df5d9e9b880608f893bb14113ae7bcaca558ad10a51e6b755cf1a037f8cb6955', 'assets/TrackListPage-BHZ2mFwN.css': 'fb479b68dfaf4199c6a368a4f021ba3324359c5b9f7fbedb36fa539b4e69a7cb', 'assets/TrackListPage-CxJZqAcf.js': '14dcc93cbcbf84f0ca765fee4a7d4e84100bc7e97137f08d345ae1bb1f7e656f', 'assets/TrackListPage-CxOiCRes.js': '1df444d162753e41c34719b4545f648e529dcc5d1f300f08956f5f75e00d7f5d', 'assets/TrackListPage-DowxQBuN.js': 'cfa520066f497c9dfb499fe81a7fb34cbb4db13c9f282729009c3c8d222d3fe3', 'assets/TrackManagePage-BbjaR9b3.js': '30847ad8b9deb2b19d03e78bee236863366b6b542f9bf55c3418d65c4b798677', 'assets/TrackManagePage-CC0Hzob3.js': '128b6a0a48e7fa8505b9c3810a89204c31826efc45405ef5a3fdce7f56238a80', 'assets/TrackManagePage-CTd-Rdvw.css': '3dc5dea7a4b7908383f03dce09efa98e19d43fe5e16185f90ce2a9d4f76bef70', 'assets/TrackManagePage-DB9cCqN2.js': '2ee21147aa35a8134993545b29f03f57d395a759ac4daab53b2f6b77efea64fa', 'assets/TrackThumbnailField-BlEoog55.js': '3707d28f474d7af22ce8fbcda88867756b7c27245725660c0841cf445b88c8f8', 'assets/TrackThumbnailField-D08STRAc.js': 'fbc869573768674b7f0e066af1fddf8795983eb0ca3e661da961fcbdb71e9bd4', 'assets/TrackThumbnailField-DH-OBpTc.css': '19d1ba611cade9f530e937a531cd4c76115288329e09e420ae89b970a0b8a215', 'assets/TrackThumbnailField-lpAuD84o.js': '58bab5c9632863f4ae5ffa854c8c2884a4fcf71dd11f4f28d74f1bbaf5ee2d25', 'assets/TrackUploadPage-C3he5oaP.css': '2e87c3091b107f95290701b2c0a8e9dc9753f0e6c8fd66476c46e8b0265c0302', 'assets/TrackUploadPage-DKQly9V8.js': '5f641c0174f7d757b068ac47ba0c234bbc602460b82d681b51770f2b0399ad16', 'assets/TrackUploadPage-DLdmSUrW.js': 'c6f39eb5a92a0dacec5488906a5cff695776eb66fd05b1ce110a132a62eda71e', 'assets/TrackUploadPage-rysMwhQy.js': '0d54494c94cfe9f03a0abb4c851de1d5b9b17a796fd80714bd9ed7d4807f4a64', 'assets/auth-B6OA5-HX.js': '6db264a2b3df9a7d6b1751ceba347e0fb09fcf7d89d969145b4740cb80c9015c', 'assets/auth-CBHvXJY1.js': 'ca7d97d5b926e4b56ab22f7f7aee3c35b37b926d6872a0dd224bf933797fb030', 'assets/auth-ypnxwhpr.js': '2e2476eac9cbf8e2028f9771cf737c19e20b736a262906315ec9a1b228b2d8a3', 'assets/format-JxAHuoE8.js': 'f02f16d7849086d485294ddd8d071f59c505f2166f78f909ffae165857419870', 'assets/index-CdqTTw7B.js': '0c2fc230214272281f3ce1aaa862dcec603e907354fa15251458d415ea18fd9e', 'assets/index-DaWlFLWx.js': '843075dfe920fd24b921e9413537dc03a3cafc57290d0e1644259ee321391c06', 'assets/index-Di84VtT3.css': '80d81e157d141aeae505462c15d3d7fcf448b894f189181cacda7d3695a0fecc', 'assets/index-cxlVgpFD.js': '75d05142a227327a60f39018c6e84d1513fa76729fe0766dde4b76ff9e6308f1', 'assets/tagName-DbQV6ZKc.js': '98faa42d6123c43e56f11dfd011b029259f05cd2dcd1ad3644f38b3905b77982', 'assets/tags-BEvVuFh3.js': 'c25b6369323a0df5acb2bac58a771fe4b87600a57f64756d55e9e6da57377685', 'assets/tags-BybfqIL2.js': 'c6c0951cb370ab630aea9c8d10e04c5f58ac5b791d0f4e969080063d1190bc39', 'assets/tags-CV0ACCdh.js': '6fe85be1cdd075e4c2c116cfacbb8afcef30826a236a12d18cd0b2e770fb97ee', 'assets/useAdminTrackThumbnail-CmH6TvGC.js': '4ab80171634db24ff9b5d1e9b3991369b6e3a0a48ffee8574f4c860e575d337b', 'assets/useAdminTrackThumbnail-DQ-Car7O.js': '16e9ee4917d1ef83a98f62d9bfc09d0be60f483dfa0207d40bd62139edf81400', 'assets/useAdminTrackThumbnail-DW68OCh2.js': '66bf062de5bdb91d3ba19eac77a97cf31dc00c5a08a34fc0d91d076c049de613', 'assets/useAdminTrackThumbnail-uwAqvS_b.css': 'ee8d9dff54d5a76ad5340e256ae7e86a602082909d743915a37a09d9569bca79', 'assets/validation-YZ1l_3tp.js': '6d0333f95b1531bfcfea39994790000ba4d5cb9597a96ef18300dbba772a684a', 'index.html': '6b772cb19077fda1be5f6a5cca2915c13955f65a3c1e6415406396e7766196f8'}
ADDITIONS = {'assets/AudioProcessingStatus-F4v2YEGc.js': '8fecd5db47aec43b8e8a00218222ba0e8aedaff766bd78dbb37db93e878f3d64', 'assets/auth-BAlUHTHm.js': '7a762710fae915c0f3ba86705043c483822ef2ffc176b1200c2542ece9eb71ae', 'assets/CatalogImage-BmvpS2Lg.js': '6cc873d17987bee864296a4f6387c248e976d4129519024c58e389d876015aeb', 'assets/index-7EeFqHAZ.js': '3e8aea1d1bf188d1b66c0025ebda9e819b7d3a71d35ba305b3e51982b4ce54ac', 'assets/LoginPage-BF5nh1vR.js': '48509ec8cf7dc171b4d9d0997c15b3b4b4a6d1f2f88372d580e8e8bb0d5689cc', 'assets/NotFoundPage-CNgEfOFX.js': 'ee10326abc40b845514977ba12675caf315f73f01196c5c8140c37045cdc09b0', 'assets/Pagination-C3S8SI7J.js': '3524e775f45f39ce6a755833c6a82ff760a5cb631bc820f9aaa1daa5b364c58d', 'assets/TagManagePage-D3FdIwU0.js': '0c9b3683c5d7afe472c0767cc9bcca56b192d91e5f2aec9f504793341e88f019', 'assets/tags-D4bABT5n.js': '0d515168735b08d870e5d7688757fe28b23f1934c1cacb9e69e567e484f73004', 'assets/TrackDetailPage-CyFEVd32.js': '2eaf0741670d1ab3fced19525f4051734f1fb2924adcc4b8ee2f59aad6e5ae73', 'assets/TrackEditPage-CWlzqGIW.js': 'f89888a58add5e804ba61bad95ed124c3bb7d9cb7ab98baeb60eefaa20ba50ce', 'assets/TrackListPage-uFQB2qzq.js': '50c1aead7b8136d14ed2539697c37635dd78f4c95dc28b2d465147665ea1c49f', 'assets/TrackManagePage-BUMiOkhp.js': '9739388dddcacd798aea2ea2d1064b0dd3d3f7c7a88ca816c212b01ca4f529ee', 'assets/TrackThumbnailField-BEz9M6Ny.js': 'e56b1daf03c3cd5ef37bb1df47ec05f5ae71eb5a84e0fdcd4770d0367ef2be77', 'assets/TrackUploadPage-C5uHEYlb.js': '06221550dd71fdd0883ea29acb2d6b7635cd9757965b4d0d9de5855f3912580b', 'assets/useAdminTrackThumbnail-CjIbRbuu.js': '0ec1bbb69459bc0cb8beb27c0ad57d5ec1b534e1086f55d11f441dd715b116ff'}
ARCHIVE_PIN = '43edf2ccc48d25007b2e0e7e0d13ed16049e3fedddc1bc1f7120a8d41318c942'
MANIFEST_PIN = 'd91cc68c9907fb79fe0bdb894a25e21f015cbbce52458555fc55be7e4be8ea04'


def files_now():
    root = trusted(NEW / 'frontend')
    files = {}
    for p in root.rglob('*'):
        trusted(p)
        if p.is_file(): files[p.relative_to(root).as_posix()] = sha(p)
    return files


def load_additions():
    archive=trusted(BASE/'payload.tar');need(archive.stat().st_size==501760 and sha(archive)==ARCHIVE_PIN,'ARCHIVE_PIN')
    with tarfile.open(archive,'r:') as t:
        members=t.getmembers();names=[m.name for m in members]
        need(len(names)==17 and len(set(names))==17 and set(names)==set(ADDITIONS)|{'manifest.json'},'ARCHIVE_SCOPE')
        need(all(m.isfile() and 0<m.size<=1048576 for m in members),'ARCHIVE_TYPE')
        raw=t.extractfile('manifest.json').read();need(digest(raw)==MANIFEST_PIN and json.loads(raw)==ADDITIONS,'MANIFEST_PIN')
        result={name:t.extractfile(name).read() for name in ADDITIONS}
        need(all(digest(raw)==ADDITIONS[name] for name,raw in result.items()),'FILE_PIN')
        return result


def atomic_add(path, raw):
    """Publish complete bytes with hard-link no-replace semantics on same FS."""
    trusted(path.parent)
    need(not path.exists() and not path.is_symlink(),'TARGET_EXISTS')
    tmp=BASE/('asset-'+str(time.time_ns()))
    newfile(tmp,raw,0o644)
    os.link(tmp,path,follow_symlinks=False)  # fails if another writer owns path
    tmp.unlink()
    fd=os.open(path.parent,os.O_RDONLY|os.O_DIRECTORY)
    try:os.fsync(fd)
    finally:os.close(fd)


def apply(spec):
    need(set(spec)=={'action','expires_at','identity','preflight','archive_sha256','manifest_sha256'},'SPEC_KEYS')
    need(spec['action']=='apply' and type(spec['expires_at']) is int and 0<spec['expires_at']-time.time()<=900,'SPEC_WINDOW')
    need(spec['archive_sha256']==ARCHIVE_PIN and spec['manifest_sha256']==MANIFEST_PIN,'SPEC_ARTIFACT')
    before=spec['preflight'];need(0<=time.time()-before['observed_at']<=1800 and spec['identity']==identity(),'PREFLIGHT_WINDOW')
    current=inspect()
    for key in ('identity','configs','services','old_jar_sha256','old_frontend','tls'):need(current[key]==before[key],'PREFLIGHT_DRIFT')
    need(current['old_jar_sha256']=='feff6919a291caff985d0c386d23ba30ee53ff0b2a9a32fd5d162208bf401a91','APP_PIN')
    need(current['configs'][str(SITE)]=='b5964410d3a98e0ee3708a6b827609a5b6f6d27c79b0ccae8adfdca5882ffadc','SITE_PIN')
    need(current['free_bytes']>=256*1024*1024,'FREE_SPACE')
    trusted(BASE);need(BASE.stat().st_dev==trusted(NEW/'frontend/assets').stat().st_dev,'SAME_FILESYSTEM')
    need(files_now()==EXPECTED,'EXACT_CURRENT_FRONTEND')
    data=load_additions();need(not set(data)&set(EXPECTED),'NO_REPLACE')
    newfile(BASE/'apply.claim',b'one additive attempt\n')
    added=[]
    try:
        # Publish entry last; no existing HTML/assets/config are changed.
        for name in sorted(data,key=lambda n:(n.startswith('assets/index-'),n)):
            atomic_add(NEW/'frontend'/name,data[name]);added.append(name)
        need(files_now()==EXPECTED|ADDITIONS,'FINAL_FRONTEND_PIN')
        after=inspect()
        for key in ('identity','configs','services','old_jar_sha256','old_frontend','tls'):need(after[key]==before[key],'RUNTIME_DRIFT')
        result={'status':'APPROVED_TAG_ASSETS_ADDED','added':ADDITIONS,'frontend_files':81,'runtime':after,'services_reloaded_or_restarted':False}
        newfile(BASE/'apply.result.json',json.dumps(result,sort_keys=True).encode());return result
    except BaseException:
        # Availability-only additions can safely remain; never remove files that
        # an in-flight browser may now use, or overwrite/replay an unknown write.
        for sig in (signal.SIGTERM,signal.SIGHUP,signal.SIGINT):signal.signal(sig,signal.SIG_IGN)
        newfile(BASE/'apply.failed.json',json.dumps({'status':'HOLD_PARTIAL_ADDITIONS_PRESERVED','added':added}).encode())
        raise


def interrupted(signum,frame):raise InterruptedError('INTERRUPTED')


if __name__=='__main__':
    for sig in (signal.SIGTERM,signal.SIGHUP,signal.SIGINT):signal.signal(sig,interrupted)
    try:
        raw=sys.stdin.buffer.read(2097153);need(len(raw)<=2097152,'INPUT_BOUND');spec=json.loads(raw)
        trusted(BASE);fd=os.open(BASE,os.O_RDONLY|os.O_DIRECTORY)
        try:fcntl.flock(fd,fcntl.LOCK_EX|fcntl.LOCK_NB);result=apply(spec)
        finally:os.close(fd)
        print(json.dumps(result,sort_keys=True))
    except Exception as e:
        code=str(e) if type(e) is ValueError and re.fullmatch('[A-Z_]+',str(e)) else type(e).__name__
        print(json.dumps({'status':'HOLD','code':code}));sys.exit(1)
