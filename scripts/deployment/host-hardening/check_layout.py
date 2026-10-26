"""Read-only, fixed-path WI019 preflight. Never creates or repairs directories."""

from __future__ import annotations

import argparse
import os
import stat
import sys
from pathlib import PurePosixPath


LAYOUT = {
    "/opt/atstudio": ("root", 0o755),
    "/opt/atstudio/releases": ("root", 0o755),
    "/srv/atstudio": ("root", 0o755),
    "/srv/atstudio/public": ("atstudio", 0o700),
    "/srv/atstudio/private": ("atstudio", 0o700),
    "/var/lib/atstudio": ("root", 0o755),
    "/var/lib/atstudio/tmp": ("atstudio", 0o700),
    "/var/lib/atstudio/nginx-body": ("nginx", 0o700),
    "/var/lib/atstudio/acme": ("root", 0o755),
    "/var/lib/atstudio/acme/.well-known": ("root", 0o755),
    "/var/lib/atstudio/acme/.well-known/acme-challenge": ("root", 0o755),
    "/etc/atstudio": ("root", 0o700),
    "/etc/atstudio/tls": ("root", 0o700),
    "/etc/atstudio/nginx": ("root", 0o700),
}


class GateError(Exception):
    """A fixed, non-sensitive gate failure category."""


def read_attributes(path: str) -> list[str]:
    """Inspect ACL presence without reading attribute values or file contents."""
    return os.listxattr(path, follow_symlinks=False)


def check_layout(phase: str, owners: dict[str, tuple[int, int]]) -> None:
    """Check absence before creation or exact identities/modes after creation.

    Raises:
        GateError: An invariant is not satisfied.
        OSError: Metadata cannot be inspected completely.
    """
    if phase not in ("before", "after"):
        raise GateError("invalid-phase")
    paths = set(LAYOUT)
    for name in LAYOUT:
        paths.update(str(parent) for parent in PurePosixPath(name).parents)
    identities = set()
    for path in sorted(paths, key=lambda item: (len(PurePosixPath(item).parts), item)):
        try:
            observed = os.lstat(path)
        except FileNotFoundError:
            if phase == "before" and path in LAYOUT:
                continue
            raise GateError("missing-directory") from None
        if phase == "before" and path in LAYOUT:
            raise GateError("existing-target")
        if not stat.S_ISDIR(observed.st_mode):
            raise GateError("symlink-or-nondirectory")
        attributes = read_attributes(path)
        if any(name in attributes for name in
               ("system.posix_acl_access", "system.posix_acl_default")):
            raise GateError("posix-acl-present")
        mode = stat.S_IMODE(observed.st_mode)
        if path not in LAYOUT:
            if observed.st_uid != 0 or mode & 0o022:
                raise GateError("untrusted-ancestor")
            continue
        owner, expected_mode = LAYOUT[path]
        if (observed.st_uid, observed.st_gid) != owners[owner] or mode != expected_mode:
            raise GateError("identity-or-mode-mismatch")
        identity = (observed.st_dev, observed.st_ino)
        if identity in identities:
            raise GateError("aliased-directories")
        identities.add(identity)


def resolve_owners(nginx_user: str | None) -> dict[str, tuple[int, int]]:
    """Resolve only approved local identities, without enumerating accounts."""
    import pwd

    if not nginx_user or nginx_user in ("root", "atadmin", "atstudio"):
        raise GateError("nginx-identity-required")
    service = pwd.getpwnam("atstudio")
    nginx = pwd.getpwnam(nginx_user)
    if (service.pw_uid == 0 or nginx.pw_uid == 0 or service.pw_uid == nginx.pw_uid
            or service.pw_gid == 0 or nginx.pw_gid == 0
            or service.pw_shell != "/usr/sbin/nologin"
            or service.pw_dir != "/nonexistent"):
        raise GateError("unsafe-service-identity")
    return {"root": (0, 0), "atstudio": (service.pw_uid, service.pw_gid),
            "nginx": (nginx.pw_uid, nginx.pw_gid)}


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("phase", choices=("before", "after"))
    parser.add_argument("--nginx-user")
    args = parser.parse_args(argv)
    if sys.platform != "linux" or os.geteuid() != 0:
        print("HOLD: linux-root-inspection-required")
        return 2
    try:
        owners = resolve_owners(args.nginx_user) if args.phase == "after" else {}
        check_layout(args.phase, owners)
    except GateError as error:
        print(f"HOLD: {error}")
        return 2
    except (OSError, KeyError):
        print("HOLD: metadata-or-account-unavailable")
        return 2
    print(f"PASS: layout-{args.phase}; read-only; not activation approval")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
