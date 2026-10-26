"""Read-only local preparation check. No network, process, private env or writes."""
import hashlib
import json
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
SOURCE_PATHS = {
    'src/main/resources/application.yml',
    'src/main/java/com/atstudio/atstudio/config/BackgroundTasksEnabledCondition.java',
    'src/main/java/com/atstudio/atstudio/config/BackgroundTasksConfiguration.java',
    'src/main/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorker.java',
    'src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java',
    'src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java',
    'src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java',
    'src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java',
    'deploy/single-vps/systemd/atstudio.service',
    'scripts/deployment/validate_bundle.py',
    'build/WI20261024-024-provider-preparation/operating-config-proposal.json',
}
JAR_PATH = 'build/WI068-release-run-02/build/libs/ATStudio-0.0.1-SNAPSHOT.jar'


def check_modes(value):
    expected = {
        'cold_preparation': ('false', 'true', 'false'),
        'bounded_audio_candidate': ('true', 'true', 'true'),
    }
    if set(value['modes']) != set(expected):
        raise ValueError('MODE_SET')
    for mode, flags in expected.items():
        env = value['modes'][mode]
        required = dict(zip(('APP_BACKGROUND_TASKS_ENABLED', 'APP_BACKGROUND_TASKS_AUDIO_ONLY', 'APP_AUDIO_WORKER_ENABLED'), flags))
        required.update(APP_BOOTSTRAP_TEST_USERS_ENABLED='false', APP_ACCEPTANCE_ENABLED='false',
                        APP_PAYMENT_SCHEDULER_ZONE='Asia/Seoul', PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED='false',
                        SPRING_JPA_HIBERNATE_DDL_AUTO='validate', SPRING_JPA_SHOW_SQL='false', SWAGGER_ENABLED='false')
        if env != required:
            raise ValueError('MODE_UNSAFE_OR_UNKNOWN_ENV')


def main():
    try:
        value = json.loads((HERE / 'operating-modes.json').read_bytes())
        check_modes(value)
        if set(value['source_pins']) != SOURCE_PATHS or value['release']['path'] != JAR_PATH:
            raise ValueError('FIXED_PATH_SET')
        for path, expected in value['source_pins'].items():
            target = ROOT / path
            if not target.resolve().is_relative_to(ROOT) or target.is_symlink() or target.stat().st_size > 512 * 1024:
                raise ValueError('SOURCE_BOUNDARY')
            if hashlib.sha256(target.read_bytes()).hexdigest() != expected:
                raise ValueError('SOURCE_DRIFT')
        jar = ROOT / value['release']['path']
        if not jar.resolve().is_relative_to(ROOT) or jar.is_symlink():
            raise ValueError('ARTIFACT_BOUNDARY')
        digest = hashlib.sha256()
        with jar.open('rb') as stream:
            for chunk in iter(lambda: stream.read(1024 * 1024), b''):
                digest.update(chunk)
        if digest.hexdigest() != value['release']['sha256']:
            raise ValueError('ARTIFACT_DRIFT')
        print(json.dumps({'status': 'LOCAL_PREPARATION_PASS', 'source_pins': len(value['source_pins']),
                          'jar': 'PRESERVED_MATCH', 'target': 'NOT_OBSERVED', 'activation': 'NOT_AUTHORIZED_BY_CHECK',
                          'external_gates': value['external_gates']}))
        return 0
    except (ValueError, KeyError, TypeError, OSError):
        print('{"status":"HOLD_LOCAL_PREPARATION","target":"NOT_OBSERVED"}')
        return 2


if __name__ == '__main__':
    raise SystemExit(main())
