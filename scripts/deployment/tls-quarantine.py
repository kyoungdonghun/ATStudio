"""Render one isolated Nginx configuration to stdout; never install or connect."""

from __future__ import annotations

import argparse
import ipaddress
from pathlib import Path
import sys


DOMAIN = "atmusic.co.kr"
ASSETS = Path(__file__).absolute().parents[2] / "deploy/single-vps/tls-quarantine"


class GateError(ValueError):
    """Reject a candidate without echoing operator inputs."""


def validate_inputs(domain: str, client: str) -> str:
    """Accept only the approved apex and one canonical public IPv4 /32."""
    if domain != DOMAIN:
        raise GateError("INPUT")
    try:
        network = ipaddress.IPv4Network(client, strict=True)
    except (ValueError, TypeError):
        raise GateError("INPUT") from None
    address = network.network_address
    if (network.prefixlen != 32 or str(network) != client or not address.is_global
            or address.is_multicast or address.is_reserved):
        raise GateError("INPUT")
    return client


def render_config(domain: str, client: str, phase: str) -> str:
    """Use repository templates only; no operator-supplied paths or directives."""
    validate_inputs(domain, client)
    if phase not in ("bootstrap", "tls"):
        raise GateError("INPUT")
    tls = ""
    if phase == "tls":
        tls = (ASSETS / "https.conf.template").read_text(encoding="ascii")
        tls = tls.replace("__CLIENT_IPV4_32__", client)
    result = (ASSETS / "nginx.conf.template").read_text(encoding="ascii")
    result = result.replace("__TLS_BLOCK__", tls)
    if "__" in result:
        raise GateError("TEMPLATE")
    return result


class QuietParser(argparse.ArgumentParser):
    """Do not echo potentially sensitive malformed arguments."""

    def error(self, message: str) -> None:
        raise GateError("INPUT")


def main() -> int:
    """Emit public configuration only, or a fixed failure category."""
    parser = QuietParser(description=__doc__)
    parser.add_argument("--domain", default=DOMAIN)
    parser.add_argument("--client", required=True)
    parser.add_argument("--phase", choices=("bootstrap", "tls"), required=True)
    try:
        args = parser.parse_args()
        result = render_config(args.domain, args.client, args.phase)
        sys.stdout.write(result)
    except (GateError, OSError, UnicodeError):
        print("TLS_QUARANTINE_HOLD INPUT", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
