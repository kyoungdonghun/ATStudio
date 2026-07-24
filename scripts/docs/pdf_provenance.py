"""Shared deterministic inputs for the client testing PDF."""

from __future__ import annotations

import hashlib
from pathlib import Path

MALGUN_GOTHIC_REGULAR_SHA256 = (
    "0086c19e81d293a542e7d75564c645fb58070cc850aefebf8fa1c397858e510c"
)
MALGUN_GOTHIC_BOLD_SHA256 = (
    "a541834fb9cdde9bd6d2c3ff1565cb0bb5ccf7a15e4e9daf2cb58d9b1cf282aa"
)


def normalized_utf8_lf_bytes(path: Path) -> bytes:
    """Return UTF-8 text bytes after normalizing all line endings to LF."""
    try:
        text = path.read_bytes().decode("utf-8")
    except UnicodeDecodeError as exc:
        raise ValueError(f"PDF text source must be valid UTF-8: {path}") from exc
    normalized = text.replace("\r\n", "\n").replace("\r", "\n")
    return normalized.encode("utf-8")


def normalized_text_sha256(path: Path) -> str:
    return hashlib.sha256(normalized_utf8_lf_bytes(path)).hexdigest()


def normalized_text_size(path: Path) -> int:
    return len(normalized_utf8_lf_bytes(path))


def write_utf8_lf_text(path: Path, text: str) -> None:
    """Write UTF-8 text with literal LF bytes on every platform."""
    normalized = text.replace("\r\n", "\n").replace("\r", "\n")
    path.write_bytes(normalized.encode("utf-8"))


def validate_font_input(
    path: Path,
    *,
    label: str,
    expected_sha256: str,
) -> Path:
    """Validate that a required PDF font is the approved deterministic input."""
    resolved = path.expanduser().resolve()
    if not resolved.is_file():
        raise FileNotFoundError(
            f"Required {label} font is missing: {resolved}. "
            "Install the approved Malgun Gothic font or pass its file path explicitly."
        )

    actual_sha256 = hashlib.sha256(resolved.read_bytes()).hexdigest()
    if actual_sha256 != expected_sha256:
        raise ValueError(
            f"Unexpected {label} font input: {resolved}. "
            f"Expected SHA-256 {expected_sha256}, received {actual_sha256}. "
            "Use the approved Malgun Gothic font input."
        )
    return resolved
