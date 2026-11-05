"""WI017 independent receipt checker and at-most-once SMTP attempt boundary."""
from __future__ import annotations

import hashlib
import os
import re
import smtplib
import ssl
from email.message import EmailMessage

from backup_protocol import GateError, encoded, require, status_report

CODES = {"CAPTURE_FAILED", "CAPTURE_INCOMPLETE", "MAINTENANCE_REQUIRES_OPERATOR",
         "CAPTURE_26H_STALE", "CAPTURE_MISSED", "SCHEDULE_NOT_CONFIGURED",
         "INDEPENDENT_COPY_8D_STALE", "DISPATCH_REQUIRES_OPERATOR"}


def check(state, now, expected_by=None, copy=None, expected_since=None, *, dispatch_code=None):
    require(dispatch_code in {None, "DISPATCH_UNCERTAIN", "DISPATCH_FAILED"}, "DISPATCH_CODE")
    report = status_report(state, now, expected_by, copy, expected_since)
    if dispatch_code:
        report["alerts"].append("DISPATCH_REQUIRES_OPERATOR")
        report["status"] = "ATTENTION"
    report["external_send"] = "NOT_ATTEMPTED"
    report["receipt_trust"] = "CALLER_MUST_VERIFY_PROVENANCE"
    return report


def check_with_dispatch_ledger(state, now, ledger_root, expected_by=None, copy=None,
                               expected_since=None):
    """Explicit trusted local ledger bridge; caller owns receipt/transport provenance."""
    from scheduled_dispatch import ledger_failure_code
    return check(state, now, expected_by, copy, expected_since,
                 dispatch_code=ledger_failure_code(ledger_root))


def reserve(directory: str, event: str) -> bool:
    """Durable exclusive claim; never remove even when send fails/has unknown outcome."""
    require(os.name == "posix", "NATIVE_PERMISSION_CHECK_UNAVAILABLE")
    from pathlib import PurePosixPath
    path = PurePosixPath(directory)
    require(path.is_absolute() and ".." not in path.parts, "LEDGER_PATH")
    descriptors = []
    try:
        fd = os.open("/", os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
        descriptors.append(fd)
        for part in path.parts[1:]:
            fd = os.open(part, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW, dir_fd=fd)
            descriptors.append(fd)
        for parent in descriptors:
            info = os.fstat(parent)
            require(info.st_uid in {0, os.geteuid()} and not info.st_mode & 0o022, "UNSAFE_PARENT")
        info = os.fstat(fd)
        require(info.st_uid == os.geteuid() and not info.st_mode & 0o077, "PRIVATE_LEDGER_MODE")
        def verify_chain():
            for index, part in enumerate(path.parts[1:]):
                held = os.fstat(descriptors[index + 1])
                named = os.stat(part, dir_fd=descriptors[index], follow_symlinks=False)
                require((held.st_dev, held.st_ino, held.st_mode, held.st_uid) ==
                        (named.st_dev, named.st_ino, named.st_mode, named.st_uid), "LEDGER_CHANGED")
        verify_chain()
        name = hashlib.sha256(event.encode("ascii")).hexdigest() + ".reserved"
        try:
            claim = os.open(name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600, dir_fd=fd)
        except FileExistsError:
            return False
        with os.fdopen(claim, "wb") as stream:
            stream.write(b"RESERVED: reconcile manually; never automatic resend\n")
            stream.flush()
            os.fsync(stream.fileno())
        os.fsync(fd)
        verify_chain()
        return True
    except OSError:
        raise GateError("LEDGER_IO_FAILED") from None
    finally:
        for fd in reversed(descriptors):
            os.close(fd)


def deliver(report, event, directory, config, credential_resolver, *, smtp_factory=None):
    """Call only from an approved independent host; SMTP acceptance is not receipt."""
    require(type(event) is str and re.fullmatch(r"[A-Za-z0-9_-]{1,128}", event), "EVENT_ID")
    codes = report.get("alerts")
    require(type(codes) is list and all(type(c) is str and c in CODES for c in codes), "ALERT_CODES")
    if not codes:
        return {"delivery": "NO_ALERT", "recipient_received": "UNVERIFIED"}
    for name in ("sender", "recipient"):
        require(type(config.get(name)) is str and re.fullmatch(
            r"[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9.-]+", config[name]), "MAIL_ADDRESS")
    require(type(config.get("host")) is str and re.fullmatch(r"[A-Za-z0-9.-]{1,253}", config["host"]), "SMTP_HOST")
    require(config.get("port") == 465 and type(config.get("port")) is int, "SMTP_TLS_PORT")
    require(type(config.get("credential_ref")) is str and config["credential_ref"], "CREDENTIAL_REF")
    if not reserve(directory, event):
        return {"delivery": "RESERVED_HOLD", "recipient_received": "UNVERIFIED"}
    context = ssl.create_default_context()
    require(context.check_hostname and context.verify_mode == ssl.CERT_REQUIRED, "TLS_VERIFICATION_REQUIRED")
    message = EmailMessage()
    message["From"], message["To"] = config["sender"], config["recipient"]
    test_id = config.get("test_id")
    require(test_id is None or (type(test_id) is str and re.fullmatch(r"[A-Za-z0-9_-]{1,40}", test_id)), "TEST_ID")
    prefix = "[TEST " + test_id + "] " if test_id else ""
    message["Subject"] = prefix + "ATStudio backup attention"
    message.set_content(prefix + "ATStudio backup needs operator attention.\n" + "\n".join(sorted(set(codes))))
    accepted = False
    try:
        credentials = credential_resolver(config["credential_ref"])
        factory = smtp_factory or smtplib.SMTP_SSL
        with factory(config["host"], config["port"], context=context, timeout=20) as smtp:
            smtp.login(credentials[0], credentials[1])
            refused = smtp.send_message(message)
            accepted = not refused
    except Exception:
        # Exception text may include private addresses/authentication/server responses.
        pass
    return {"delivery": "SMTP_ACCEPTED" if accepted else "OUTCOME_UNKNOWN_HOLD",
            "recipient_received": "UNVERIFIED"}


def notify(report, window_id, directory, config, credential_resolver, *, smtp_factory=None):
    """Stable schedule-window/config/code event; polling time is deliberately excluded."""
    require(type(window_id) is str and re.fullmatch(r"[A-Za-z0-9_-]{1,128}", window_id), "WINDOW_ID")
    codes = report.get("alerts")
    require(type(codes) is list and all(type(c) is str and c in CODES for c in codes), "ALERT_CODES")
    identity = {"window": window_id, "codes": sorted(set(codes)),
                "smtp": {key: config.get(key) for key in ("host", "port", "sender", "recipient")}}
    if config.get("test_id") is not None:
        identity["test_id"] = config["test_id"]
    event = hashlib.sha256(encoded(identity)).hexdigest()
    return deliver(report, event, directory, config, credential_resolver, smtp_factory=smtp_factory)


if __name__ == "__main__":
    print("INERT: no receipt, credentials, ledger or network accessed")
