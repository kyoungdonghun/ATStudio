"""Bounded in-memory ledger diagnosis; no database lifecycle or raw persistence."""

import json
import os
import re
import subprocess
import threading
import time

MAX_OUT = 4 * 1024 * 1024
MAX_ERR = 16384
PHASES = ('GUARD', 'QUERY_BUILD', 'MYSQL_EXIT', 'SNAPSHOT_PARSE', 'LEDGER_INIT', 'COMPLETE')
CODES = ('NONE', 'QUERY_FAILED', 'MYSQL_FAILED', 'TIMEOUT', 'OUTPUT_LIMIT',
         'IO_FAILED', 'REAP_FAILED', 'SCHEMA', 'LEDGER', 'LEDGER_BUDGET',
         'CONFIG', 'PREFLIGHT', 'REDACTED')
STATES = ('HY000', '42000', '42S02', '42S22', '28000', '08S01', '23000', '22001')


class DiagnosticError(Exception):
    def __init__(self, code: str) -> None:
        super().__init__(code)
        self.code = code


def mysql_error(raw: bytes) -> tuple[int, str]:
    matches = re.findall(rb'(?m)^ERROR ([1-9][0-9]{3,4}) \(([A-Z0-9]{5})\)(?: at line [0-9]+)?:', raw)
    if len(matches) != 1:
        return 0, 'UNKNOWN'
    number, state = matches[0]
    text = state.decode('ascii')
    return int(number), text if text in STATES else 'UNKNOWN'


def bounded_query(argv: tuple, sql: bytes, env: dict, seconds: float = 20) -> tuple[int, bytes, bytes]:
    """Drain both pipes within caps and join every owned thread before returning."""
    if not 0 < seconds <= 20 or type(sql) is not bytes or len(sql) > 1024 * 1024:
        raise DiagnosticError('IO_FAILED')
    out, err = bytearray(), bytearray()
    failure = []
    process = subprocess.Popen(argv, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE, env=env, shell=False)
    def drain(stream: object, buffer: bytearray, cap: int) -> None:
        try:
            while True:
                chunk = stream.read(min(8192, cap + 1 - len(buffer)))
                if not chunk:
                    return
                buffer.extend(chunk)
                if len(buffer) > cap:
                    failure.append('OUTPUT_LIMIT')
                    return
        except Exception:
            failure.append('IO_FAILED')
    def feed() -> None:
        try:
            process.stdin.write(sql)
            process.stdin.flush()
        except BrokenPipeError:
            pass
        except Exception:
            failure.append('IO_FAILED')
        finally:
            try:
                process.stdin.close()
            except BrokenPipeError:
                pass
            except Exception:
                failure.append('IO_FAILED')
    threads = [threading.Thread(target=drain, args=(process.stdout, out, MAX_OUT)),
               threading.Thread(target=drain, args=(process.stderr, err, MAX_ERR)),
               threading.Thread(target=feed)]
    end = time.monotonic() + seconds
    try:
        for thread in threads:
            thread.start()
        while process.poll() is None or any(thread.is_alive() for thread in threads):
            if failure:
                raise DiagnosticError(failure[0])
            if time.monotonic() >= end:
                raise DiagnosticError('TIMEOUT')
            time.sleep(0.01)
        if failure:
            raise DiagnosticError(failure[0])
        return process.returncode, bytes(out), bytes(err)
    finally:
        cleanup_failed = False
        try:
            if process.poll() is None:
                process.kill()
            process.wait(timeout=2)
        except Exception:
            cleanup_failed = True
        for thread in threads:
            if thread.ident is not None:
                thread.join(timeout=2)
                cleanup_failed |= thread.is_alive()
        for stream in (process.stdin, process.stdout, process.stderr):
            try:
                stream.close()
            except Exception:
                cleanup_failed = True
        if cleanup_failed:
            raise DiagnosticError('REAP_FAILED')


def diagnose(ledger: object, schema: dict, person: dict, query: object) -> dict:
    """Return fixed metadata only; query is the reviewed socket-only invocation."""
    result = dict(status='HOLD', phase='QUERY_BUILD', code='REDACTED',
                  mysql_errno=0, sqlstate='UNKNOWN', checks={})
    try:
        sql = ledger.queries(schema, person).encode()
        result['phase'] = 'MYSQL_EXIT'
        code, raw, stderr = query(sql)
        if code != 0:
            result.update(code='MYSQL_FAILED')
            result['mysql_errno'], result['sqlstate'] = mysql_error(stderr)
            return result
        result['phase'] = 'SNAPSHOT_PARSE'
        snapshot = ledger.parse_snapshot(raw, schema)
        result['phase'] = 'LEDGER_INIT'
        result['checks'] = {
            'matches_zero': snapshot['checks']['matches'] == 0,
            'work_zero': snapshot['checks']['work'] == 0,
            'verification_empty': not snapshot['tables']['email_verification_tokens'],
            'reset_empty': not snapshot['tables']['password_reset_tokens'],
        }
        ledger.Ledger(snapshot, person)
        result.update(status='PASS', phase='COMPLETE', code='NONE')
    except DiagnosticError as error:
        result['code'] = error.code if error.code in CODES else 'REDACTED'
    except Exception as error:
        from contract import Hold
        if type(error) is Hold and len(error.args) == 1 and error.args[0] in CODES:
            result['code'] = error.args[0]
        elif result['phase'] == 'QUERY_BUILD':
            result['code'] = 'QUERY_FAILED'
    return result


if __name__ == '__main__':
    print(json.dumps({'status': 'PREPARATION_ONLY', 'native': 'NOT_RUN'}))
