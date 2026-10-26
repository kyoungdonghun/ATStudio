#!/usr/bin/python3 -I
"""Certbot deploy hook: metadata gates, certificate validation, then bounded reload."""

from __future__ import annotations

import os
from pathlib import Path
import re
import stat
import subprocess
import sys
from typing import Mapping


DOMAIN = "atmusic.co.kr"
LINEAGE = Path("/etc/letsencrypt/live") / DOMAIN
ARCHIVE = Path("/etc/letsencrypt/archive") / DOMAIN
CONFIG = Path("/etc/nginx/tls-quarantine/nginx.conf")
HOOK = Path("/etc/letsencrypt/renewal-hooks/deploy/40-atstudio-quarantine")
DROPIN = Path("/etc/systemd/system/nginx.service.d/90-atstudio-quarantine.conf")


class GateError(ValueError):
    """A fixed, non-sensitive refusal category."""


def check_event(env: Mapping[str, str]) -> None:
    """Bind this hook to exactly one approved Certbot lineage and domain."""
    if (env.get("RENEWED_DOMAINS") != DOMAIN
            or env.get("RENEWED_LINEAGE") != LINEAGE.as_posix()):
        raise GateError("EVENT")


def check_metadata(info: os.stat_result, directory: bool = False,
                   private: bool = False) -> None:
    """Reject links, wrong owners, writable ancestry and non-private keys."""
    expected = stat.S_ISDIR if directory else stat.S_ISREG
    if (not expected(info.st_mode) or info.st_uid != 0 or info.st_gid != 0
            or info.st_mode & 0o022 or (not directory and info.st_nlink != 1)
            or (private and info.st_mode & 0o077)):
        raise GateError("METADATA")


def check_path(path: Path, directory: bool = False, private: bool = False) -> None:
    """lstat every ancestor before touching a fixed target; never follow links."""
    for parent in reversed(path.parents):
        check_metadata(parent.lstat(), directory=True)
    check_metadata(path.lstat(), directory=directory, private=private)


def check_lineage() -> Path:
    """Allow only Certbot's exact same-generation links into its protected archive."""
    check_path(LINEAGE.parent, directory=True, private=True)
    check_path(ARCHIVE.parent, directory=True, private=True)
    check_path(LINEAGE, directory=True)
    check_path(ARCHIVE, directory=True)
    generations = []
    for stem in ("fullchain", "privkey"):
        link = LINEAGE / f"{stem}.pem"
        info = link.lstat()
        if (not stat.S_ISLNK(info.st_mode) or info.st_uid != 0
                or info.st_gid != 0 or info.st_nlink != 1):
            raise GateError("LINK")
        target = os.readlink(link)
        match = re.fullmatch(
            rf"\.\./\.\./archive/{re.escape(DOMAIN)}/{stem}([1-9][0-9]{{0,6}})\.pem",
            target,
        )
        if match is None:
            raise GateError("LINK")
        generations.append(match[1])
        archived = ARCHIVE / f"{stem}{match[1]}.pem"
        check_path(archived, private=(stem == "privkey"))
        if not 1 <= archived.lstat().st_size <= 65536:
            raise GateError("SIZE")
    if generations[0] != generations[1]:
        raise GateError("PAIR")
    return LINEAGE / "fullchain.pem"


def run(args: list[str], capture: bool = False) -> bytes:
    """Run fixed OS tools without inherited hooks, stdin, raw errors or a shell."""
    try:
        result = subprocess.run(
            args, stdin=subprocess.DEVNULL,
            stdout=subprocess.PIPE if capture else subprocess.DEVNULL,
            stderr=subprocess.DEVNULL, timeout=20, check=False,
            env={"PATH": "/usr/sbin:/usr/bin", "LC_ALL": "C"},
        )
    except (OSError, subprocess.TimeoutExpired):
        raise GateError("COMMAND") from None
    if result.returncode != 0:
        raise GateError("COMMAND")
    output = result.stdout if capture else b""
    if len(output) > 4096:
        raise GateError("OUTPUT")
    return output


def check_san(output: bytes) -> None:
    """The public certificate must name only the approved apex."""
    if not re.fullmatch(
            rb"X509v3 Subject Alternative Name:(?: critical)?\s+DNS:atmusic\.co\.kr\s*",
            output):
        raise GateError("SAN")


def reload_checked(env: Mapping[str, str]) -> None:
    """Read certificate metadata only; Nginx itself checks the private-key match."""
    check_event(env)
    for path in (CONFIG, HOOK, DROPIN, Path("/usr/sbin/nginx"),
                 Path("/usr/bin/systemctl"), Path("/usr/bin/openssl")):
        check_path(path)
    certificate = check_lineage().as_posix()
    check_san(run(["/usr/bin/openssl", "x509", "-in", certificate,
                   "-noout", "-ext", "subjectAltName"], capture=True))
    run(["/usr/bin/openssl", "verify", "-purpose", "sslserver", "-verify_hostname",
         DOMAIN, "-untrusted", certificate, certificate])
    run(["/usr/sbin/nginx", "-t", "-q", "-c", CONFIG.as_posix()])
    run(["/usr/bin/systemctl", "is-active", "--quiet", "nginx.service"])
    run(["/usr/bin/systemctl", "reload", "nginx.service"])
    run(["/usr/bin/systemctl", "is-active", "--quiet", "nginx.service"])


def main() -> int:
    """A successful reload request still requires independent served-cert proof."""
    try:
        if sys.platform != "linux" or os.geteuid() != 0:
            raise GateError("PLATFORM")
        reload_checked(os.environ)
    except (GateError, OSError):
        print("TLS_QUARANTINE_HOLD")
        return 1
    print("TLS_QUARANTINE_RELOAD_REQUESTED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
