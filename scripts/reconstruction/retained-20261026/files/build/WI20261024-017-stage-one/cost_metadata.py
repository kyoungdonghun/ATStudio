"""MA-only in-memory extension of the unchanged WI011 cold inventory."""
import json
import os
from pathlib import Path
import stat


def need(ok):
    if not ok:raise ValueError('COST_METADATA_HOLD')


def canonical(value):return json.dumps(value,sort_keys=True,separators=(',',':')).encode()


def observe(baseline,boot,tick,attributes=None,info=None):
    """No DB/cloud calls. Xattrs are checked for absence without reading their values."""
    attributes=os.listxattr if attributes is None else attributes
    info=lambda_path_stat if info is None else info
    members=baseline['members'];need(type(members) is dict and 0<len(members)<=20000)
    paths={Path(name) for name in members}
    ancestors={parent for path in paths for parent in path.parents}
    # Future payload ancestors are fixed; newly created children inherit no default ACL.
    ancestors.update(Path('/var/lib/atstudio-backup/wi069-captures').parents)
    ancestors.add(Path('/var/lib/atstudio-backup/wi069-captures'))
    for path in sorted(paths|ancestors):
        tick();st=info(path)
        need(not stat.S_ISLNK(st.st_mode) and (stat.S_ISDIR(st.st_mode) or stat.S_ISREG(st.st_mode)))
        need(not attributes(path,follow_symlinks=False))
        need(len(path.name.encode('utf-8'))<=255 and len(str(path).encode('utf-8'))<=4096)
        need(all(type(v) is int and 0<=v<2**64 for v in (st.st_uid,st.st_gid,st.st_ino,st.st_dev,st.st_nlink,st.st_size)))
    # Read local name tables only; never call NSS/network lookup or return names.
    for filename in ('/etc/passwd','/etc/group'):
        raw=boot.read_file(Path(filename),262144)
        for line in raw.splitlines():
            if line and not line.startswith(b'#'):
                need(len(line.split(b':',1)[0])<=256)
    files=sum(row['type']=='file' for row in members.values())
    directories=sum(row['type']=='dir' for row in members.values())
    need(files+directories==len(members))
    source_bytes=sum(row.get('size',0) for row in members.values())
    need(source_bytes==baseline['source_bytes'] and 0<source_bytes<900000000)
    # Same future manifest shape as the retained adapter; hashes stay private.
    manifest=dict(version=1,scope='CURRENT_SQL_MEDIA_PINNED_RUNTIME_NO_SECRETS',members=members,
        source_sha256=baseline['source_sha256'],jar_sha256=baseline['jar_sha256'],runtime_sha256=baseline['runtime_sha256'],
        inventory_sha256=baseline['inventory_sha256'],units_sha256=baseline['units_sha256'],full_disaster_recovery=False)
    return dict(status='BOUNDED_COST_METADATA_OBSERVED',source_bytes=source_bytes,files=files,directories=directories,
        ancestors=len(ancestors-paths),manifest_bytes=len(canonical(manifest)),sql_bytes_bound=2*2*1024**2,
        sql_proof_bytes_bound=16384,payload_files=4,payload_directories=3,xattrs_absent=True,
        basename_utf8_bytes_max=255,local_owner_group_utf8_bytes_max=256,
        generic_attributes='LINUX_NONE',inventory_sha256=baseline['inventory_sha256'],units_sha256=baseline['units_sha256'])


def lambda_path_stat(path):return path.lstat()


def install_account_query_context(adapter):
    """Select the fixed source schema only for the retained account inventory SQL."""
    need(adapter.cold.SCHEMA=='atstudio_prod')
    original=adapter.accounts.queries
    def queries(day):
        return b'USE `atstudio_prod`;\n'+original(day)
    adapter.accounts.queries=queries


def install_release_inventory(adapter):
    """Complete the retained allowlist from the already pinned current SPA manifest."""
    def current_members(ctx,tick=lambda:None):
        h,n,r,historical=ctx
        boot=adapter.boot;cold=adapter.cold;contract=adapter.contract
        frontend=cold.ROOTS[4]
        raw=boot.read_file(adapter.HERE/'lib/release-allowlist.json',128*1024)
        contract.need(boot.sha(raw)==cold.RELEASE_SHA,'FRONTEND_ALLOWLIST_PIN')
        old=adapter.checks.strict_json(raw)
        contract.need(old['frontend_root']==str(frontend/'frontend'),'FRONTEND_ROOT')
        allowed={}
        def add_spa(root,files):
            contract.need(type(files) is dict and 0<len(files)<=10000,'SPA_MANIFEST')
            for name,record in files.items():
                contract.need(type(name) is str and name and not name.startswith('/')
                    and ':' not in name and '..' not in Path(name).parts and '\\' not in name
                    and Path(name).as_posix()==name,'FRONTEND_PATH')
                contract.need(type(record) is dict and set(record)=={'bytes','sha256'}
                    and type(record['bytes']) is int and 0<=record['bytes']<=16*1024**2
                    and contract.pin(record['sha256']),'SPA_MANIFEST')
                path=str(root/name);contract.need(path not in allowed,'SPA_DUPLICATE')
                allowed[path]=record
        add_spa(frontend/'frontend',old['files'])
        jar=r.RELEASE/'app.jar'
        allowed[str(jar)]={'bytes':jar.stat().st_size,'sha256':adapter.guard.JAR}
        frozen_raw=h.read(r.TOOLS/'binding/freeze.json',262144)
        contract.need(boot.sha(frozen_raw)==adapter.guard.RUNTIME,'RUNTIME_PIN')
        frozen=h.decode(frozen_raw)
        artifact_raw=h.read(r.TOOLS/'binding/artifact.json',262144)
        contract.need(boot.sha(artifact_raw)==frozen['files']['binding/artifact.json'],'CURRENT_SPA_MANIFEST_PIN')
        add_spa(r.RELEASE/'frontend',h.decode(artifact_raw)['spa_files'])
        for relative,pin in frozen['files'].items():
            path=r.TOOLS/relative
            allowed[str(path)]={'bytes':path.stat().st_size,'sha256':pin}
        allowed[str(r.TOOLS/'binding/freeze.json')]={'bytes':len(frozen_raw),'sha256':adapter.guard.RUNTIME}
        roots=(*cold.ROOTS[:3],r.RELEASE,frontend,r.TOOLS)
        members=cold.collect(cold.ROOTS[:3],tick=tick)
        members.update(cold.collect((r.RELEASE,frontend,r.TOOLS),allowed,tick))
        contract.need(sum(row.get('size',0) for row in members.values())<900000000,'CURRENT_SOURCE_LIMIT')
        return roots,members
    adapter.current_members=current_members


def install(entry,adapter,boot,contract,approval,decision,decision_pin,candidate):
    """Patch only the in-memory dispatch result; all frozen files remain unchanged."""
    original=adapter.inventory;observations=[]
    def tick():contract.authority(approval,decision,decision_pin,b'',b'',candidate,'inventory')
    def inventory(value):
        tick();ctx,baseline=original(value)
        metadata=observe(baseline,boot,tick)
        tick()
        need(adapter.current_members(ctx,tick)[1]==baseline['members'])
        need(adapter.guard.cold(*ctx)==baseline['cold'])
        observations.append(metadata)
        return ctx,baseline
    original_dispatch=entry.dispatch
    def dispatch(args):
        need(args.action=='inventory')
        result=original_dispatch(args)
        need(len(observations)==1 and result['status']=='CURRENT_COLD_INVENTORY_OBSERVED')
        result['cost_metadata']=observations[0]
        return result
    adapter.inventory=inventory;entry.dispatch=dispatch
