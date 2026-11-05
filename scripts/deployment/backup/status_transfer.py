"""WI020 authenticated filesystem handoff. CLI/import are inert; no network transport."""
from __future__ import annotations

from contextlib import contextmanager
import hashlib
import hmac
import json
import os
import re
import stat
from pathlib import PurePosixPath

from backup_protocol import GateError, encoded, require, timestamp
import alert_delivery

MAX_BYTES = 32768


def token(value):
    require(type(value) is str and re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]{0,79}", value)
            and ".." not in value, "REFERENCE_INVALID")
    return value


def decode(data):
    require(type(data) is bytes and len(data) <= MAX_BYTES, "ARTIFACT_BOUND")
    try:
        def unique(items):
            result = {}
            for key, value in items:
                require(key not in result, "DUPLICATE_FIELD")
                result[key] = value
            return result
        value = json.loads(data, object_pairs_hook=unique)
        require(type(value) is dict and encoded(value) == data, "CANONICAL_OBJECT_REQUIRED")
        return value
    except (ValueError, TypeError, RecursionError, UnicodeError):
        raise GateError("ARTIFACT_INVALID") from None


class NativeStore:
    """Private POSIX directory, retained descriptors, safe relative files only."""
    def __init__(self, directory):
        require(os.name == "posix", "NATIVE_PERMISSION_CHECK_UNAVAILABLE")
        path = PurePosixPath(directory)
        require(path.is_absolute() and ".." not in path.parts, "STORE_PATH")
        self.fds, self.parts = [], path.parts[1:]
        try:
            fd = os.open("/", os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
            self.fds.append(fd)
            for part in self.parts:
                fd = os.open(part, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW, dir_fd=fd)
                self.fds.append(fd)
            self.verify()
        except Exception:
            self.close()
            raise GateError("STORE_UNSAFE") from None

    def close(self):
        for fd in reversed(self.fds):
            os.close(fd)
        self.fds = []

    def verify(self):
        try:
            self._verify()
        except OSError:
            raise GateError("STORE_IO") from None

    def _verify(self):
        require(bool(self.fds), "STORE_CLOSED")
        for index, fd in enumerate(self.fds):
            meta = os.fstat(fd)
            require(meta.st_uid in {0, os.geteuid()} and not meta.st_mode & 0o022, "STORE_UNSAFE")
            if index:
                named = os.stat(self.parts[index - 1], dir_fd=self.fds[index - 1], follow_symlinks=False)
                require((meta.st_dev, meta.st_ino, meta.st_mode, meta.st_uid) ==
                        (named.st_dev, named.st_ino, named.st_mode, named.st_uid), "STORE_CHANGED")
        meta = os.fstat(self.fds[-1])
        require(meta.st_uid == os.geteuid() and not meta.st_mode & 0o077, "STORE_UNSAFE")

    def read(self, name, missing=False):
        token(name)
        self.verify()
        try:
            fd = os.open(name, os.O_RDONLY | os.O_NOFOLLOW | os.O_NONBLOCK, dir_fd=self.fds[-1])
        except FileNotFoundError:
            if missing:
                return None
            raise GateError("STORE_MISSING") from None
        except OSError:
            raise GateError("STORE_IO") from None
        try:
            before = os.fstat(fd)
            require(stat.S_ISREG(before.st_mode) and before.st_nlink == 1 and
                    before.st_uid == os.geteuid() and not before.st_mode & 0o7077,
                    "PRIVATE_FILE_REQUIRED")
            require(before.st_size <= MAX_BYTES, "ARTIFACT_BOUND")
            data = bytearray()
            while len(data) <= MAX_BYTES:
                chunk = os.read(fd, min(4096, MAX_BYTES + 1 - len(data)))
                if not chunk:
                    break
                data.extend(chunk)
            after = os.fstat(fd)
            named = os.stat(name, dir_fd=self.fds[-1], follow_symlinks=False)
            identity = lambda m: (m.st_dev, m.st_ino, m.st_mode, m.st_uid, m.st_size, m.st_mtime_ns, m.st_ctime_ns, m.st_nlink)
            require(identity(before) == identity(after) == identity(named), "FILE_CHANGED")
            require(len(data) == before.st_size and len(data) <= MAX_BYTES, "ARTIFACT_BOUND")
            self.verify()
            return bytes(data)
        except OSError:
            raise GateError("STORE_IO") from None
        finally:
            os.close(fd)

    def write(self, name, data):
        token(name)
        require(type(data) is bytes and len(data) <= MAX_BYTES, "ARTIFACT_BOUND")
        self.verify()
        temporary = "pending-" + os.urandom(16).hex()
        try:
            fd = os.open(temporary, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW,
                         0o600, dir_fd=self.fds[-1])
            with os.fdopen(fd, "wb") as stream:
                stream.write(data)
                stream.flush()
                os.fsync(stream.fileno())
            self.verify()
            os.replace(temporary, name, src_dir_fd=self.fds[-1], dst_dir_fd=self.fds[-1])
            os.fsync(self.fds[-1])
            self.verify()
        except OSError:
            raise GateError("STORE_IO") from None

    @contextmanager
    def lock(self):
        self.verify()
        try:
            fd = os.open("transfer-lock", os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW,
                         0o600, dir_fd=self.fds[-1])
            try:
                os.fsync(fd)
            finally:
                os.close(fd)
            os.fsync(self.fds[-1])
        except OSError:
            raise GateError("TRANSFER_LOCKED") from None
        # Exceptions retain the lock: outcomes must be reconciled, never guessed.
        yield
        self.verify()
        try:
            os.unlink("transfer-lock", dir_fd=self.fds[-1])
            os.fsync(self.fds[-1])
        except OSError:
            raise GateError("STORE_IO") from None


def key(store, reference):
    secret = store.read(token(reference))
    require(type(secret) is bytes and 32 <= len(secret) <= 128, "KEY_LENGTH")
    return secret


def credential_resolver(store):
    def resolve(reference):
        value = decode(store.read(token(reference)))
        require(set(value) == {"username", "password"} and all(
            type(v) is str and 0 < len(v) <= 4096 and "\r" not in v and "\n" not in v
            for v in value.values()), "CREDENTIAL_FORMAT")
        return value["username"], value["password"]
    return resolve


def clean_state(state):
    require(type(state) is dict, "STATE_INVALID")
    attempt = state.get("attempt", {})
    require(type(attempt) is dict, "STATE_INVALID")
    stage = attempt.get("stage")
    require((stage is None or type(stage) is str) and stage in {None, "PRECHECK", "CLOSE_ADMISSION", "QUIESCENT", "DUMP", "CAPTURE",
                      "VERIFY", "CAPTURE_COMPLETE", "RESUME", "COMPLETE", "FAILED"}, "STATE_INVALID")
    maintenance = attempt.get("maintenance", False)
    require(type(maintenance) is bool, "STATE_INVALID")
    result = {"attempt": {"stage": stage, "maintenance": maintenance}}
    good = state.get("last_good")
    if good:
        require(type(good) is dict and type(good.get("snapshot_id")) is str and
                re.fullmatch(r"[0-9a-f]{64}", good["snapshot_id"]), "STATE_INVALID")
        timestamp(good.get("captured_at"))
        result["last_good"] = {"snapshot_id": good["snapshot_id"], "captured_at": good["captured_at"]}
    return result


def verify_artifact(data, secret, source, target, now):
    value = decode(data)
    require(set(value) == {"payload", "mac"} and type(value["mac"]) is str and
            re.fullmatch(r"[0-9a-f]{64}", value["mac"]), "ARTIFACT_FIELDS")
    payload = value["payload"]
    require(type(payload) is dict and set(payload) == {"version", "source", "target", "sequence", "exported_at", "state", "dispatch_code"}, "ARTIFACT_FIELDS")
    require(hmac.compare_digest(value["mac"], hmac.new(secret, encoded(payload), hashlib.sha256).hexdigest()), "AUTHENTICATION_FAILED")
    require(payload["source"] == token(source) and payload["target"] == token(target), "TARGET_MISMATCH")
    require(type(payload["version"]) is int and payload["version"] == 1 and
            type(payload["sequence"]) is int and payload["sequence"] > 0, "SEQUENCE_INVALID")
    exported = timestamp(payload["exported_at"])
    require(exported <= now, "FUTURE_ARTIFACT")
    require(clean_state(payload["state"]) == payload["state"], "STATE_INVALID")
    good = payload["state"].get("last_good")
    require(not good or timestamp(good["captured_at"]) <= exported, "FUTURE_CAPTURE")
    require((payload["dispatch_code"] is None or type(payload["dispatch_code"]) is str) and
            payload["dispatch_code"] in {None, "DISPATCH_FAILED", "DISPATCH_UNCERTAIN"}, "DISPATCH_CODE")
    return payload


def export(store, key_ref, source, target, exported_at, *, dispatch_root,
           source_store, state_ref="state.json"):
    from scheduled_dispatch import ledger_failure_code
    secret = key(store, key_ref)
    with store.lock():
        previous = store.read("export-record", missing=True)
        sequence = 1
        if previous:
            sequence = verify_artifact(previous, secret, source, target, timestamp(exported_at))["sequence"] + 1
        payload = {"version": 1, "source": token(source), "target": token(target),
                   "sequence": sequence, "exported_at": exported_at,
                   "state": clean_state(decode(source_store.read(token(state_ref)))),
                   "dispatch_code": ledger_failure_code(dispatch_root)}
        data = encoded({"payload": payload, "mac": hmac.new(secret, encoded(payload), hashlib.sha256).hexdigest()})
        verify_artifact(data, secret, source, target, timestamp(exported_at))
        store.write("export-record", data)
    return data  # private handoff artifact; never print it


def receive(store, data, key_ref, source, target, now):
    secret = key(store, key_ref)
    candidate = verify_artifact(data, secret, source, target, now)
    with store.lock():
        previous = store.read("accepted-record", missing=True)
        if previous:
            old = verify_artifact(previous, secret, source, target, now)
            require(candidate["sequence"] >= old["sequence"], "REPLAY_ROLLBACK")
            if candidate["sequence"] == old["sequence"]:
                require(data == previous, "SEQUENCE_CONFLICT")
            else:
                require(timestamp(candidate["exported_at"]) >= timestamp(old["exported_at"]), "TIME_ROLLBACK")
                old_good, new_good = old["state"].get("last_good"), candidate["state"].get("last_good")
                require(not old_good or (new_good and timestamp(new_good["captured_at"]) >= timestamp(old_good["captured_at"])), "CAPTURE_ROLLBACK")
                store.write("accepted-record", data)
        else:
            store.write("accepted-record", data)
    return candidate  # duplicate still evaluated, not silently discarded


def monitor(store, data, key_ref, source, target, now, window_id, alert_directory,
            smtp_config, *, expected_since=None, expected_by=None, smtp_factory=None,
            max_export_age_seconds=3600):
    require(type(max_export_age_seconds) is int and 0 < max_export_age_seconds <= 86400,
            "EXPORT_AGE_BOUND")
    invalid = False
    if data is not None:
        try:
            payload = receive(store, data, key_ref, source, target, now)
        except GateError:
            invalid = True
            payload = None
    else:
        payload = None
    if payload is None:
        previous = store.read("accepted-record", missing=True)
        payload = verify_artifact(previous, key(store, key_ref), source, target, now) if previous else None
    report = alert_delivery.check(payload["state"] if payload else {}, now, expected_by,
                                  expected_since=expected_since,
                                  dispatch_code=payload["dispatch_code"] if payload else None)
    stale_transfer = bool(payload and now - timestamp(payload["exported_at"]) > max_export_age_seconds)
    if (invalid or stale_transfer) and "CAPTURE_INCOMPLETE" not in report["alerts"]:
        report["alerts"].append("CAPTURE_INCOMPLETE")
        report["status"] = "ATTENTION"
    report["receipt_trust"] = "AUTHENTICATED_KEY_HOLDER_ASSERTION" if payload else "NO_AUTHENTICATED_RECEIPT"
    delivery = alert_delivery.notify(report, window_id, alert_directory, smtp_config,
                                     credential_resolver(store), smtp_factory=smtp_factory)
    return {"status": report["status"], "alerts": report["alerts"],
            "transfer": "REJECTED" if invalid else "STALE" if stale_transfer else "CHECKED", **delivery}


if __name__ == "__main__":
    print("INERT: explicit protected references and authorized file handoff required")
