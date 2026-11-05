#!/usr/bin/python3
"""Bounded online-trial assembly; plan is offline, execute needs separate authority.

This module never issues provider proofs or capture/restore approvals. The native
adapter starts exactly its two named transient units and a separately bounded
capture helper. Its app and source DB share an isolated network namespace; the
helper queries MySQL over an AF_UNIX socket. All state survives cleanup.
"""
from __future__ import annotations

import argparse
import json
import os
from pathlib import Path, PurePosixPath
import re
import stat
import sys
import time

from backup_protocol import digest, encoded, exact_id, require, timestamp
from backup_runtime import checked_path, create_bytes, hash_file, read_json, atomic_json
from host_adapter import Commands, ColdHost, properties, FIELDS, UNITS, MASKED

MIB = 1024 ** 2
GIB = 1024 ** 3
JAR_SHA = '94b2c668602d14b88e323899c8bc6b5d5a524d2de7883e530e93be098506954a'
JAR_BYTES = 70356576
IDENT = re.compile(r'[A-Za-z_][A-Za-z0-9_]{0,63}\Z')


def path(value):
    require(isinstance(value, str) and re.fullmatch(r'/[A-Za-z0-9_./-]+', value) is not None
            and '..' not in PurePosixPath(value).parts, 'TRIAL_PATH')
    return PurePosixPath(value)


def identity_pair(raw):
    """Keep legacy raw identity distinct from online_capture's canonical identity."""
    require(isinstance(raw, bytes) and 1 <= len(raw) <= 128, 'IDENTITY_BYTES')
    return {'raw': digest(raw), 'canonical': digest(raw.strip())}


def plan(config):
    require(config.get('version') == 1 and config.get('scope') == 'ONE_ONLINE_TRIAL', 'TRIAL_SCOPE')
    run = config['run_id']
    require(isinstance(run, str) and re.fullmatch('[a-f0-9]{32}', run), 'TRIAL_RUN_ID')
    root = path(config['root'])
    require(str(root) == '/var/lib/atstudio-online-trial/' + run, 'TRIAL_ROOT')
    for field in ('machine', 'boot'):
        require(set(config['identity'][field]) == {'raw', 'canonical'}, 'DUAL_IDENTITY_REQUIRED')
        for value in config['identity'][field].values(): exact_id(value)
    require(config['jar_sha256'] == JAR_SHA and config['jar_bytes'] == JAR_BYTES, 'FINAL_JAR_REQUIRED')
    for identity in ('app_identity', 'mysql_identity'):
        require(set(config[identity]) == {'uid', 'gid'} and
                all(type(v) is int and v > 0 for v in config[identity].values()), 'ACCOUNT_IDENTITY')
    require(config['app_identity'] != config['mysql_identity'], 'DISTINCT_ACCOUNT')
    for key in ('public_root', 'private_root', 'tmp_root', 'datadir', 'mysql_config', 'mysql_log_dir',
                'environment_file', 'online_config', 'online_approval', 'online_executor', 'python', 'java'):
        path(config[key])
    require(config['public_root'] == '/srv/atstudio/public' and config['private_root'] == '/srv/atstudio/private'
            and config['tmp_root'] == '/var/lib/atstudio/tmp', 'RETAINED_MEDIA_ROOTS')
    require(config['datadir'] == '/var/lib/mysql' and config['mysql_log_dir'] == '/var/log/mysql', 'RETAINED_DB_LAYOUT')
    require(IDENT.fullmatch(config['schema']), 'SCHEMA_IDENTIFIER')
    for key in ('app_port', 'db_port'):
        require(type(config[key]) is int and 1024 <= config[key] <= 65535, 'TRIAL_PORT')
    require(config['app_port'] != config['db_port'], 'DISTINCT_PORTS')
    for key in ('environment_file', 'online_config', 'online_approval'):
        require(path(config[key]).is_relative_to(root / 'inputs'), 'PRIVATE_TRIAL_INPUT')
    require(config['online_executor'] == str(root / 'code/online_capture.py'), 'TRIAL_CODE_ROOT')
    contract_keys = {'target_host', 'repository', 'repository_id', 'region', 'password_file',
                     'aws_credentials_file', 'restic_binary', 'restic_sha256', 'settings'}
    contract = config['capture_contract']
    require(set(contract) == contract_keys and isinstance(contract['settings'], list) and
            bool(contract['settings']), 'CAPTURE_CONTRACT')
    require({'/var/lib/atstudio/tmp', '/opt/atstudio/releases/wi060-web-20261011',
             '/opt/atstudio/wi067-tools'} <= set(contract['settings']), 'RETAINED_SOURCE_COVERAGE')
    for setting in contract['settings']: path(setting)
    exact_id(contract['repository_id']); exact_id(contract['restic_sha256'])
    for key in ('java', 'python', 'online_executor', 'mysql_config', 'environment_file'):
        require(config[key] in config['pins'], 'PIN_COVERAGE')
    for name, value in config['pins'].items(): path(name); exact_id(value)
    for required in ('/usr/bin/systemctl', '/usr/bin/systemd-run', '/usr/sbin/mysqld', '/usr/bin/mysql'):
        require(required in config['pins'], 'PIN_COVERAGE')
    require(set(config['baseline_units']) == set(UNITS), 'BASELINE_COVERAGE')
    for name, row in config['baseline_units'].items():
        if name == 'nginx.service':
            require(row.get('ActiveState') == 'active' and row.get('SubState') == 'running' and int(row.get('MainPID', '0')) > 0, 'NGINX_BASELINE')
        else:
            require(row.get('ActiveState') in {'inactive', 'failed'} and row.get('MainPID') == row.get('ControlPID') == '0' and
                    (name not in MASKED or row.get('UnitFileState') == 'masked'), 'COLD_BASELINE')
    limits = config['limits']
    require(limits == {'app_memory': 3 * GIB, 'db_memory': 768 * MIB, 'helper_memory': GIB,
                       'reserve_memory': GIB, 'seconds': 1800, 'source_bytes': 256 * MIB,
                       'dump_bytes': 16 * MIB, 'metadata_bytes': 16 * MIB,
                       'capture_retrieval_bytes': 288 * MIB, 'restore_retrieval_bytes': 288 * MIB}, 'TRIAL_LIMITS')
    units = {name: 'atstudio-online-' + name + '-' + run + '.service' for name in ('db', 'app', 'helper')}
    # Retained AppArmor contract. Never invent a new MySQL socket/log location.
    runtime = '/run/mysqld'
    continuity, bridge = str(root / 'continuity'), str(root / 'mailbox')
    release = str(root / 'release')
    jar = release + '/application.jar'
    common = ['Type=exec', 'UMask=0077', 'NoNewPrivileges=yes', 'ProtectSystem=strict',
              'ProtectHome=yes', 'PrivateTmp=yes', 'KillMode=control-group', 'TimeoutStopSec=20',
              'SendSIGKILL=yes', 'RuntimeMaxSec=2400', 'MemorySwapMax=0', 'CPUQuota=100%',
              'TasksMax=256', 'LimitCORE=0', 'StandardOutput=null', 'StandardError=null']
    db_props = common + ['User=' + str(config['mysql_identity']['uid']),
        'Group=' + str(config['mysql_identity']['gid']), 'PrivateNetwork=yes',
        'RestrictAddressFamilies=AF_UNIX AF_INET', 'MemoryMax=768M',
        'RuntimeDirectory=' + runtime.removeprefix('/run/'), 'RuntimeDirectoryMode=0750',
        'ReadWritePaths=' + config['datadir'] + ' ' + runtime + ' ' + config['mysql_log_dir'] + '/' + units['db'] + '.log']
    db = ['/usr/sbin/mysqld', '--defaults-file=' + config['mysql_config'], '--datadir=' + config['datadir'],
          '--socket=' + runtime + '/mysqld.sock', '--pid-file=' + runtime + '/mysqld.pid',
          '--log-error=' + config['mysql_log_dir'] + '/' + units['db'] + '.log',
          '--skip-networking=OFF', '--bind-address=127.0.0.1', '--port=' + str(config['db_port']), '--mysqlx=OFF',
          '--event-scheduler=DISABLED', '--skip-slave-start', '--skip-log-bin', '--persisted-globals-load=OFF',
          '--local-infile=OFF', '--secure-file-priv=NULL', '--innodb-buffer-pool-size=134217728',
          '--max-connections=16', '--performance-schema=OFF', '--skip-name-resolve',
          '--default-time-zone=+00:00', '--upgrade=NONE', '--read-only=ON', '--super-read-only=ON']
    app_props = common + ['User=' + str(config['app_identity']['uid']),
        'Group=' + str(config['app_identity']['gid']), 'PrivateNetwork=yes',
        'JoinsNamespaceOf=' + units['db'], 'Requires=' + units['db'], 'After=' + units['db'],
        'RestrictAddressFamilies=AF_UNIX AF_INET', 'MemoryMax=3G',
        'UnsetEnvironment=JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS JAVA_OPTS CLASSPATH SPRING_APPLICATION_JSON SPRING_CONFIG_IMPORT SPRING_CONFIG_ADDITIONAL_LOCATION SPRING_PROFILES_INCLUDE LOADER_PATH',
        'EnvironmentFile=' + config['environment_file'],
        'ReadWritePaths=' + ' '.join([config['public_root'], config['private_root'], config['tmp_root'], continuity, bridge])]
    options = {'server.address': '127.0.0.1', 'server.port': str(config['app_port']),
        'spring.config.location': 'classpath:/application.yml', 'spring.profiles.active': 'prod',
        'spring.datasource.url': 'jdbc:mysql://127.0.0.1:' + str(config['db_port']) + '/' + config['schema'] + '?serverTimezone=UTC',
        'spring.sql.init.mode': 'never', 'spring.jpa.hibernate.ddl-auto': 'validate',
        'spring.mail.test-connection': 'false', 'app.background-tasks.enabled': 'false',
        'app.background-tasks.audio-only': 'false', 'app.background-tasks.capture-control-only': 'true',
        'app.audio.worker-enabled': 'false', 'app.acceptance.enabled': 'false',
        'app.bootstrap.test-users.enabled': 'false', 'app.storage.capture-control-enabled': 'true',
        'app.storage.capture-control-path': bridge, 'app.storage.continuity-state-path': continuity,
        'app.storage.public-path': config['public_root'], 'app.storage.private-path': config['private_root'],
        'app.storage.require-explicit-roots': 'true', 'app.storage.integrity.strict-on-startup': 'true',
        'spring.servlet.multipart.location': config['tmp_root']}
    app = [config['java'], '-Xms256m', '-Xmx2g', '-jar', jar] + ['--' + k + '=' + v for k, v in options.items()]
    def unit(name, props, argv, wait=False):
        return ['/usr/bin/systemd-run', '--quiet', '--unit=' + units[name], *(['--wait'] if wait else []),
                '--property=Description=ATS online trial ' + run + ' ' + digest(encoded(config)),
                *['--property=' + p for p in props], '--', *argv]
    # Repository endpoint/DNS addresses are explicit fresh inputs, never wildcard egress.
    import ipaddress
    networks = config['helper_allow_ips']
    require(isinstance(networks, list) and 1 <= len(networks) <= 12, 'HELPER_EGRESS')
    for item in networks:
        net = ipaddress.ip_network(item, strict=True)
        require(net.prefixlen == net.max_prefixlen and not net.is_unspecified, 'HELPER_EGRESS')
    helper_props = common + ['User=root', 'Group=root', 'MemoryHigh=768M', 'MemoryMax=1G',
        'Nice=10', 'IPAddressDeny=any', 'IPAddressAllow=' + ' '.join(networks),
        'UnsetEnvironment=PYTHONPATH PYTHONHOME PYTHONSTARTUP PYTHONINSPECT PYTHONSAFEPATH',
        'RestrictAddressFamilies=AF_UNIX AF_INET AF_INET6',
        'ReadWritePaths=' + str(root) + ' /run/atstudio-operations']
    require(config['helper_write_paths'] == '/run/atstudio-operations', 'HELPER_WRITE_PATHS')
    # Commands supplies only PATH/LC_ALL. -I would suppress reviewed sibling modules.
    helper = [config['python'], '-B', config['online_executor'], 'execute',
              '--config', config['online_config'], '--approval', config['online_approval']]
    return {'status': 'PREPARED_NOT_AUTHORITY', 'config_sha256': digest(encoded(config)),
        'run_id': run, 'root': str(root), 'units': units, 'socket': runtime + '/mysqld.sock',
        'continuity': continuity, 'bridge': bridge, 'release': release, 'jar': jar,
        'commands': {'db': unit('db', db_props, db), 'app': unit('app', app_props, app),
                     'helper': unit('helper', helper_props, helper, True)},
        'peak_memory_bytes': sum(limits[k] for k in ('app_memory', 'db_memory', 'helper_memory', 'reserve_memory')),
        'retrieval_bound_bytes': limits['capture_retrieval_bytes'] + limits['restore_retrieval_bytes'],
        'restore_order': 'AFTER_APP_DB_STOPPED', 'restore_verified': False}


def authority(config, approval, proof, now):
    prepared = plan(config)
    require(approval.get('scope') == 'ONE_ONLINE_TRIAL' and approval.get('version') == 1 and
            approval.get('config_sha256') == prepared['config_sha256'] and
            approval.get('run_id') == config['run_id'], 'TRIAL_AUTHORITY')
    start, end = timestamp(approval['not_before']), timestamp(approval['not_after'])
    require(start <= now and now + 2040 <= end and end - start <= 3600, 'TRIAL_WINDOW')
    require(proof.get('config_sha256') == prepared['config_sha256'] and proof.get('run_id') == config['run_id']
            and proof.get('identity') == config['identity'] and proof.get('scope') == 'ONE_ONLINE_TRIAL', 'TRIAL_PROOF_BINDING')
    exact_id(proof['source_evidence_sha256'])
    observed, expiry = timestamp(proof['observed_at']), timestamp(proof['expires_at'])
    require(start - 3600 <= observed <= now and now + 2040 <= expiry and expiry - observed <= 6 * 3600, 'TRIAL_PROOF_FRESHNESS')
    require(proof['checks'] == {'cold_baseline': True, 'source_inventory': True,
        'no_other_writers': True, 'nginx_quarantine': True, 'settings_allowlist': True,
        'source_database_and_accounts': True, 'helper_egress_enforcement': True,
        'repository_acl': True, 'cost_and_retrieval': True}, 'TRIAL_PROOF_INCOMPLETE')
    require(proof['cost_upper_bound_krw'] == approval['cost_limit_krw'] == 0 and
            proof['retrieval_bound_bytes'] >= prepared['retrieval_bound_bytes'], 'TRIAL_COST')
    return prepared


def finish_capture(config, prepared, backend, authorize):
    """Owned resumed session: even invalid/expired capture input closes its units."""
    failure = None
    try:
        authorize()
        backend.capture(config, prepared)
    except BaseException:
        failure = 'CAPTURE_FAILED'
        raise
    finally:
        backend.stop_owned(prepared, failure)
    return {'status': 'CAPTURE_COMPLETE_OWNED_UNITS_STOPPED', 'run_id': config['run_id'], 'restore_verified': False}


class NativeTrial:
    def __init__(self):
        self.command = Commands()
        self.claimed = None

    def show(self, unit):
        return properties(self.command.run(['/usr/bin/systemctl', 'show', '--no-pager', '--property=' + FIELDS, '--', unit]))

    def preflight(self, config, prepared):
        require(sys.platform == 'linux' and os.geteuid() == 0, 'LINUX_ROOT_REQUIRED')
        for field, filename in (('machine', '/etc/machine-id'), ('boot', '/proc/sys/kernel/random/boot_id')):
            require(identity_pair(Path(filename).read_bytes()) == config['identity'][field], 'TRIAL_HOST_IDENTITY')
        require(Path('/sys/fs/cgroup/cgroup.controllers').exists(), 'CGROUP_V2_REQUIRED')
        mem = next(int(x.split()[1]) * 1024 for x in Path('/proc/meminfo').read_text().splitlines() if x.startswith('MemAvailable:'))
        require(mem >= prepared['peak_memory_bytes'], 'COMBINED_MEMORY')
        for filename, expected in config['pins'].items():
            source = checked_path(filename, trusted=True)
            require(hash_file(source, lambda: None, 256 * MIB)[1] == expected, 'TRIAL_PIN')
        jar = checked_path(prepared['jar'], trusted=True)
        require(hash_file(jar, lambda: None, JAR_BYTES) == (JAR_BYTES, JAR_SHA), 'TRIAL_JAR')
        root = checked_path(prepared['root'], trusted=True)
        require(stat.S_IMODE(root.stat().st_mode) == 0o751, 'TRIAL_TRAVERSAL')
        volume = os.statvfs(root)
        total = volume.f_blocks * volume.f_frsize
        require(volume.f_bavail * volume.f_frsize >= max(total // 5, 10 * GIB) + 6 * GIB and
                volume.f_files > 0 and volume.f_favail >= volume.f_files // 10, 'COMBINED_DISK_RESERVE')
        require(not (root / 'trial-owned.json').exists(), 'TRIAL_ALREADY_CLAIMED')
        require(not Path('/run/mysqld').exists() and not Path('/run/mysqld').is_symlink(), 'UNOWNED_DB_RUNTIME')
        # Root-owned systemd input may carry secrets, but cannot carry JVM agents,
        # alternate Spring configuration, loader paths or arbitrary environment.
        allowed = {'SPRING_DATASOURCE_USERNAME', 'SPRING_DATASOURCE_PASSWORD', 'JWT_SECRET',
                   'JWT_ACCESS_SECRET', 'JWT_REFRESH_SECRET', 'MAIL_FROM', 'TOSS_CLIENT_KEY',
                   'TOSS_SECRET_KEY', 'TOSS_BILLING_CLIENT_KEY', 'TOSS_BILLING_SECRET_KEY',
                   'PAYMENT_BILLING_KEY_ACTIVE_KEY_ID', 'PAYMENT_BILLING_KEY_0_ID',
                   'PAYMENT_BILLING_KEY_0_SECRET'}
        body = checked_path(config['environment_file'], private=True).read_bytes()
        require(len(body) <= 65536, 'ENVIRONMENT_BOUND')
        names = []
        for line in body.decode('utf-8').splitlines():
            if not line.strip() or line.lstrip().startswith('#'): continue
            key, separator, value = line.partition('=')
            require(separator and key in allowed and '\x00' not in value and not value.endswith('\\'), 'ENVIRONMENT_ALLOWLIST')
            names.append(key)
        require(len(names) == len(set(names)) and {'SPRING_DATASOURCE_USERNAME', 'SPRING_DATASOURCE_PASSWORD', 'JWT_SECRET'} <= set(names), 'ENVIRONMENT_KEYS')
        for unit in prepared['units'].values(): require(self.show(unit).get('LoadState') == 'not-found', 'TRIAL_UNIT_EXISTS')
        for name, expected in config['baseline_units'].items():
            require(self.show(name) == expected, 'BASELINE_CHANGED')
        for proc in Path('/proc').iterdir():
            if proc.name.isdigit():
                try: comm = (proc / 'comm').read_text().strip()
                except FileNotFoundError: continue
                require(comm not in {'java', 'mysqld', 'mariadbd', 'ffmpeg'}, 'UNOWNED_WRITER')
        for directory in (prepared['continuity'], prepared['bridge']):
            info = checked_path(directory).stat()
            require(stat.S_ISDIR(info.st_mode) and stat.S_IMODE(info.st_mode) == 0o700 and
                    info.st_uid == config['app_identity']['uid'] and info.st_gid == config['app_identity']['gid'], 'APP_CONTROL_OWNER')
        self.log_adapter = TrialLog(config, prepared)
        self.log_adapter.log_layout()
        self.config = config

    def bind_capture(self, config, prepared):
        # This inner approval can only be assembled AFTER start returned observations.
        # MA supplies it within the separately approved native trial; no new human gate.
        ready = read_json(checked_path(Path(prepared['root']) / 'trial-ready.json', private=True))
        require(ready['run_id'] == config['run_id'] and ready['config_sha256'] == prepared['config_sha256'], 'READY_BINDING')
        inner_path = Path(prepared['root']) / 'inputs/capture-stage-binding.json'
        inner = read_json(checked_path(inner_path, private=True))
        require(inner['trial_config_sha256'] == prepared['config_sha256'] and
                inner['run_id'] == config['run_id'] and inner['ready_sha256'] == digest(encoded(ready)), 'CAPTURE_STAGE_AUTHORITY')
        require(time.time() + 1840 <= ready['deadline'], 'SHARED_TRIAL_DEADLINE')
        for field in ('online_config', 'online_approval'):
            require(digest(checked_path(config[field], private=True).read_bytes()) == inner[field + '_sha256'], 'CAPTURE_STAGE_PIN')
        online = read_json(Path(config['online_config']), 1024 * 1024)
        require(not online.get('recovery_gate') and not
                (Path(online['state_dir']) / 'live-service-ledger' / 'operation.guard').exists(),
                'LEGACY_GATE_INCOMPATIBLE')
        require(all(online.get(key) == value for key, value in config['capture_contract'].items()), 'CAPTURE_SCOPE_WIDENED')
        require(online['public_root'] == config['public_root'] and online['private_root'] == config['private_root'] and
                online['online']['schema'] == config['schema'] and online['hook_binary'] == config['online_executor'], 'CAPTURE_SOURCE_BINDING')
        capture_approval = read_json(Path(config['online_approval']))
        require(capture_approval['run_id'] == config['run_id'] and timestamp(capture_approval['not_after']) <= ready['deadline'], 'CAPTURE_DEADLINE_BINDING')
        root = Path(prepared['root'])
        require(online['online']['machine_sha256'] == config['identity']['machine']['canonical'] and
                online['online']['boot_sha256'] == config['identity']['boot']['canonical'], 'ONLINE_IDENTITY_REPRESENTATION')
        require(online['online']['bridge_dir'] == prepared['bridge'] and online['release_root'] == prepared['release']
                and online['online']['app_uid'] == config['app_identity']['uid']
                and online['online']['app_gid'] == config['app_identity']['gid'], 'ONLINE_CONFIG_CONNECTION')
        for key in ('state_dir', 'work_dir', 'cache_dir', 'temp_dir'):
            require(online[key] == str(root / key), 'FRESH_HELPER_ROOTS')
            checked_path(online[key], private=True)
        require(online['lock_file'] == '/run/atstudio-operations/operations.lock', 'OPERATION_LOCK')
        require(online['release_link'] == str(root / 'release-link') and
                Path(online['release_link']).resolve(strict=True) == Path(prepared['release']), 'TRIAL_RELEASE_LINK')
        require(online['mysql_defaults_file'] == str(root / 'inputs/mysql.cnf'), 'MYSQL_SOCKET_OPTIONS')
        expected_options = ('[client]\nprotocol=socket\nsocket=' + prepared['socket'] + '\nuser=root\n').encode()
        require(checked_path(online['mysql_defaults_file'], private=True).read_bytes() == expected_options, 'MYSQL_SOCKET_OPTIONS')
        require(online['limits']['source_bytes'] <= config['limits']['source_bytes'] and
                online['limits']['dump_bytes'] <= config['limits']['dump_bytes'] and
                online['limits']['metadata_bytes'] <= config['limits']['metadata_bytes'] and
                online['limits']['retrieval_bytes'] <= config['limits']['capture_retrieval_bytes'] and
                online['limits']['seconds'] <= 1800 and online['limits']['upload_kib'] <= 1024 and
                online['limits']['download_kib'] <= 1024 and online['limits']['cost_limit_krw'] == 0, 'CAPTURE_LIMIT_BINDING')
        self.online = online

    def claim(self, prepared):
        claim = Path(prepared['root']) / 'trial-owned.json'
        create_bytes(claim, encoded(self.binding(prepared)))
        self.claimed = prepared

    def binding(self, prepared):
        return {'run_id': prepared['run_id'], 'config_sha256': prepared['config_sha256'],
                'units': prepared['units'], 'identity': self.config['identity']}

    def resume(self, config, prepared):
        self.config = config
        for field, filename in (('machine', '/etc/machine-id'), ('boot', '/proc/sys/kernel/random/boot_id')):
            require(identity_pair(Path(filename).read_bytes()) == config['identity'][field], 'TRIAL_HOST_IDENTITY')
        require(read_json(checked_path(Path(prepared['root']) / 'trial-owned.json', private=True)) == self.binding(prepared), 'TRIAL_RESUME_BINDING')
        self.claimed = prepared

    def start(self, name, prepared):
        if name == 'db': self.log_adapter.prepare_log()
        self.command.run(prepared['commands'][name], seconds=45)

    def db_ready(self, config, prepared):
        end = time.monotonic() + 30
        while not Path(prepared['socket']).exists():
            require(time.monotonic() < end, 'DB_READY_TIMEOUT'); time.sleep(.1)
        reply = self.command.run(['/usr/bin/mysql', '--no-defaults', '--protocol=socket', '--socket=' + prepared['socket'],
            '--user=root', '--batch', '--skip-column-names', '--execute=SELECT @@event_scheduler,@@read_only,@@super_read_only,@@local_infile,@@skip_networking,@@bind_address,@@port;'])
        expected = ('DISABLED\t1\t1\t0\t0\t127.0.0.1\t' + str(config['db_port'])).encode()
        require(reply.strip() == expected, 'SOURCE_DB_READ_ONLY')

    def app_ready(self, config, prepared):
        end = time.monotonic() + 90
        while True:
            app = self.show(prepared['units']['app']); db = self.show(prepared['units']['db'])
            require(app.get('ActiveState') not in {'failed', 'inactive'}, 'APP_START_FAILED')
            if int(app.get('MainPID', '0')) > 0 and (Path(prepared['bridge']) / 'requests').is_dir():
                tcp = Path('/proc/' + app['MainPID'] + '/net/tcp').read_text().splitlines()[1:]
                listening = {int(line.split()[1].split(':')[1], 16) for line in tcp if line.split()[3] == '0A'}
                if config['app_port'] in listening:
                    require(listening <= {config['app_port'], config['db_port']}, 'UNEXPECTED_PRIVATE_LISTENER')
                    break
            require(time.monotonic() < end, 'APP_READY_TIMEOUT'); time.sleep(.2)
        netns = [Path('/proc/' + row['MainPID'] + '/ns/net').stat().st_ino for row in (app, db)]
        require(netns[0] == netns[1] != Path('/proc/1/ns/net').stat().st_ino, 'PRIVATE_NETNS_REQUIRED')
        interfaces = {line.split(':')[0].strip() for line in Path('/proc/' + app['MainPID'] + '/net/dev').read_text().splitlines() if ':' in line}
        require(interfaces == {'lo'}, 'PRIVATE_INTERFACES_REQUIRED')
        ticks = []
        for row, executable in ((app, config['java']), (db, '/usr/sbin/mysqld')):
            process = Path('/proc') / row['MainPID']
            require(str((process / 'exe').resolve(strict=True)) == executable, 'PROCESS_EXECUTABLE')
            ticks.append((process / 'stat').read_text().rpartition(')')[2].split()[19])
        return {'app_pid': app['MainPID'], 'db_pid': db['MainPID'], 'app_start_ticks': ticks[0],
                'db_start_ticks': ticks[1], 'netns': netns[0]}

    def capture(self, config, prepared):
        self.bind_capture(config, prepared)
        ready = read_json(Path(prepared['root']) / 'trial-ready.json')
        require(self.app_ready(config, prepared) == ready['processes'], 'RUNNING_TUPLE_CHANGED')
        self.command.run(prepared['commands']['helper'], cap=65536, seconds=1840)
        state = read_json(checked_path(Path(self.online['state_dir']) / 'online-state.json', private=True))
        require(state.get('stage') == 'COMPLETE' and state.get('run_id') == config['run_id'], 'CAPTURE_NOT_COMPLETE')

    def stop_owned(self, prepared, failure):
        require(self.claimed == prepared, 'UNOWNED_CLEANUP')
        expected = self.binding(prepared)
        require(read_json(checked_path(Path(prepared['root']) / 'trial-owned.json', private=True)) == expected, 'CLEANUP_BINDING')
        stopped, errors = [], []
        for name in ('helper', 'app', 'db'):
            unit = prepared['units'][name]
            try:
                description = properties(self.command.run(['/usr/bin/systemctl', 'show', '--no-pager',
                    '--property=LoadState,Description', '--', unit]))
                require(description.get('LoadState') == 'not-found' or description.get('Description') ==
                        'ATS online trial ' + config_run(prepared), 'UNIT_DESCRIPTION_BINDING')
                # Claim and exact generated name authorize stop even if show fails.
                if description.get('LoadState') != 'not-found':
                    self.command.run(['/usr/bin/systemctl', 'stop', '--', unit], seconds=35)
            except Exception:
                errors.append(name + '_STOP_UNCONFIRMED')
            try:
                row = self.show(unit)
                require(row.get('MainPID', '0') == row.get('ControlPID', '0') == '0' and
                        row.get('ActiveState') in {'inactive', 'failed'}, 'OWNED_UNIT_REMAINS')
                stopped.append(name)
            except Exception:
                errors.append(name + '_REMAINS_UNCONFIRMED')
        receipt = {'version': 1, 'run_id': prepared['run_id'],
            'config_sha256': prepared['config_sha256'], 'stopped': stopped, 'failure': failure,
            'cleanup_errors': errors, 'state_preserved': True, 'restore_verified': False}
        create_bytes(Path(prepared['root']) / ('trial-cleanup-' + str(time.time_ns()) + '.json'), encoded(receipt))
        stopped_path = Path(prepared['root']) / 'trial-stopped.json'
        if not errors and not stopped_path.exists(): create_bytes(stopped_path, encoded(receipt))
        require(not errors, 'OWNED_CLEANUP_HOLD')


class TrialLog(ColdHost):
    """Reuse only inherited log_layout/prepare_log; no cold DB or 43-table hooks."""
    def __init__(self, config, prepared):
        self.fs = Path
        self.host = {key: config[key] for key in ('mysql_identity', 'mysql_log_directory', 'mysql_log_parent')}
        self.log = Path(config['mysql_log_dir']) / (prepared['units']['db'] + '.log')


def config_run(prepared):
    return prepared['run_id'] + ' ' + prepared['config_sha256']


def stage_layout(config):
    """Native-only exclusive layout creation after reviewed artifact upload.

    Parent/root/release/inputs are uploaded as root-owned non-writable artifacts.
    This creates only fresh control/helper leaves and a trial-local release link.
    """
    prepared = plan(config)
    require(sys.platform == 'linux' and os.geteuid() == 0, 'LINUX_ROOT_REQUIRED')
    root = checked_path(prepared['root'], trusted=True)
    require(stat.S_IMODE(root.stat().st_mode) == 0o751 and
            (root / 'release').is_dir() and (root / 'inputs').is_dir(), 'STAGED_LAYOUT')
    checked_path(root / 'inputs', private=True)
    checked_path(root / 'release', trusted=True)
    require(hash_file(root / 'release/application.jar', lambda: None, JAR_BYTES) ==
            (JAR_BYTES, JAR_SHA), 'STAGED_JAR_PIN')
    entries = ['continuity', 'mailbox', 'state_dir', 'work_dir', 'cache_dir', 'temp_dir', 'release-link', 'inputs/mysql.cnf']
    require(all(not (root / name).exists() and not (root / name).is_symlink() for name in entries), 'LAYOUT_ALREADY_EXISTS')
    for name in entries[:6]:
        directory = root / name
        directory.mkdir(mode=0o700)
        if name in {'continuity', 'mailbox'}:
            os.chown(directory, config['app_identity']['uid'], config['app_identity']['gid'])
    os.symlink(prepared['release'], root / 'release-link')
    create_bytes(root / 'inputs/mysql.cnf', ('[client]\nprotocol=socket\nsocket=' + prepared['socket'] + '\nuser=root\n').encode())
    return {'status': 'LAYOUT_STAGED_NO_SERVICE_STARTED', 'run_id': config['run_id']}


def prepare_manifest(jar, code_directory):
    """Local read-only preparation; no payload values, runtime facts or authority."""
    jar = Path(jar)
    require(hash_file(jar, lambda: None, JAR_BYTES) == (JAR_BYTES, JAR_SHA), 'FINAL_JAR_REQUIRED')
    modules = ('backup_protocol.py', 'backup_runtime.py', 'scheduled_dispatch.py',
               'host_adapter.py', 'online_capture.py', 'online_restore.py', 'phase_b_trial.py', 'outer_recovery.py')
    files = {'release/application.jar': {'sha256': JAR_SHA, 'bytes': JAR_BYTES, 'mode': '0444', 'owner': 'root'}}
    for filename in modules:
        source = Path(code_directory) / filename
        size, sha = hash_file(source, lambda: None, 1024 * 1024)
        files['code/' + filename] = {'sha256': sha, 'bytes': size, 'mode': '0555', 'owner': 'root'}
    return {'version': 1, 'status': 'PREPARED_NOT_AUTHORITY', 'files': files,
        'layout': {'root': 'root:root 0751', 'inputs': 'root:root 0700', 'release': 'root:root 0555',
                   'code': 'root:root 0555', 'app_leaves': 'explicit app UID/GID 0700'},
        'private_inputs': ['reviewed filtered environment', 'dual raw/canonical host identities',
            'trial config/approval/proof', 'post-start online config/approval/proofs', 'capture-stage binding',
            'separate restore config/approval/proofs'], 'execution_authorized': False}


def prepare_capture_binding(config, ready, online_raw, approval_raw):
    """Bind existing authorities to observed start; does not issue an approval."""
    from online_capture import validate_online, validate_authority
    prepared = plan(config)
    require(ready['config_sha256'] == prepared['config_sha256'] and ready['run_id'] == config['run_id'], 'READY_BINDING')
    online, approval = json.loads(online_raw), json.loads(approval_raw)
    validate_online(online)
    validate_authority(online, approval, time.time())
    require(approval['run_id'] == config['run_id'] and timestamp(approval['not_after']) <= ready['deadline'], 'CAPTURE_DEADLINE_BINDING')
    return {'version': 1, 'trial_config_sha256': prepared['config_sha256'], 'run_id': config['run_id'],
            'ready_sha256': digest(encoded(ready)), 'online_config_sha256': digest(online_raw),
            'online_approval_sha256': digest(approval_raw), 'approval_issued': False}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['prepare-manifest', 'prepare-capture-binding', 'plan', 'stage', 'start', 'capture', 'stop'])
    parser.add_argument('--config')
    parser.add_argument('--jar')
    parser.add_argument('--ready')
    parser.add_argument('--online-config')
    parser.add_argument('--online-approval')
    parser.add_argument('--approval')
    parser.add_argument('--proof')
    args = parser.parse_args()
    if args.action == 'prepare-manifest':
        require(isinstance(args.jar, str), 'JAR_REQUIRED')
        return prepare_manifest(args.jar, Path(__file__).parent)
    require(isinstance(args.config, str), 'CONFIG_REQUIRED')
    config = read_json(Path(args.config), 1024 * 1024)
    if args.action == 'prepare-capture-binding':
        ready = read_json(Path(args.ready))
        return prepare_capture_binding(config, ready, Path(args.online_config).read_bytes(), Path(args.online_approval).read_bytes())
    if args.action == 'plan': return plan(config)
    require(sys.platform == 'linux' and os.geteuid() == 0, 'LINUX_ROOT_REQUIRED')
    checked_path(args.config, private=True)
    prepared, backend = plan(config), NativeTrial()
    require(not (Path(prepared['root']) / 'state_dir/live-service-ledger/operation.guard').exists(),
            'LEGACY_GATE_INCOMPATIBLE')
    if args.action == 'stop':
        backend.resume(config, prepared)
        backend.stop_owned(prepared, 'EXPLICIT_OWNED_STOP')
        return {'status': 'OWNED_UNITS_STOPPED_STATE_PRESERVED'}
    if args.action == 'capture':
        backend.resume(config, prepared)
        def authorize_capture():
            require(not (Path(prepared['root']) / 'trial-stopped.json').exists(), 'TRIAL_ALREADY_STOPPED')
            for value in (args.approval, args.proof):
                require(isinstance(value, str), 'PRIVATE_AUTHORITY_REQUIRED'); checked_path(value, private=True)
            approval = read_json(Path(args.approval)); proof = read_json(Path(args.proof))
            require(digest(Path(args.proof).read_bytes()) == approval['proof_sha256'], 'TRIAL_PROOF_PIN')
            authority(config, approval, proof, time.time())
            create_bytes(Path(prepared['root']) / 'capture-stage-claimed.json', encoded({'run_id': config['run_id']}))
        return finish_capture(config, prepared, backend, authorize_capture)
    for value in (args.config, args.approval, args.proof):
        require(isinstance(value, str), 'PRIVATE_AUTHORITY_REQUIRED'); checked_path(value, private=True)
    approval = read_json(Path(args.approval)); proof = read_json(Path(args.proof))
    require(digest(Path(args.proof).read_bytes()) == approval['proof_sha256'], 'TRIAL_PROOF_PIN')
    authority(config, approval, proof, time.time())
    if args.action == 'stage': return stage_layout(config)
    if args.action == 'start':
        backend.preflight(config, prepared)
        backend.claim(prepared)
        started_at = time.time()
        try:
            backend.start('db', prepared); backend.db_ready(config, prepared)
            backend.start('app', prepared)
            observed = backend.app_ready(config, prepared)
            ready = {'version': 1, 'run_id': config['run_id'], 'config_sha256': prepared['config_sha256'],
                'processes': observed, 'observed_at': time.time(),
                'deadline': min(timestamp(approval['not_after']), started_at + 2280),
                'capture_authority_issued': False}
            create_bytes(Path(prepared['root']) / 'trial-ready.json', encoded(ready))
            return {'status': 'STARTED_AWAITING_ACTUAL_CAPTURE_EVIDENCE', 'ready_sha256': digest(encoded(ready)),
                    'run_id': config['run_id'], 'deadline': ready['deadline']}
        except BaseException:
            backend.stop_owned(prepared, 'START_FAILED'); raise


if __name__ == '__main__':
    try: print(json.dumps(main(), sort_keys=True))
    except Exception as error:
        code = str(error) if type(error).__name__ == 'GateError' else type(error).__name__
        print(json.dumps({'status': 'HOLD', 'code': code})); sys.exit(1)
