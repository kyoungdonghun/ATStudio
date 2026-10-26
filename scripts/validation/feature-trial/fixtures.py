"""WI063: create exactly two deterministic 10-second PCM WAVs, never upload them."""

from __future__ import annotations

import argparse
from contextlib import ExitStack
import hashlib
import io
import json
import os
from pathlib import Path
import stat
import struct
import wave


NAMES = ("WI063-tone-a.wav", "WI063-tone-b.wav")
HASHES = {
    NAMES[0]: "d7b1011a45c84eb0c0d0308cc744289aa277b8b43ff0a4b97d6c59a1254ffa13",
    NAMES[1]: "8d8d993e59a8ea7c80d5b06d4a9560e66c677635cf87728542bc9d13ead948df",
}


class FixtureError(Exception):
    """Fixed public failure without filesystem details."""


def wav_bytes(period: int) -> bytes:
    """Build integer triangle waves, avoiding platform-dependent sine rounding."""
    if period not in (100, 150):
        raise FixtureError("FIXTURE_SPEC")
    cycle = b"".join(struct.pack("<hh", value, value) for value in (
        (4 * 3000 * abs(phase - period // 2) // period) - 3000
        for phase in range(period)))
    pcm = (cycle * ((441000 + period - 1) // period))[:1764000]
    target = io.BytesIO()
    with wave.open(target, "wb") as wav:
        wav.setparams((2, 2, 44100, 441000, "NONE", "not compressed"))
        wav.writeframes(pcm)
    return target.getvalue()


def no_links(path: Path) -> None:
    for part in (path, *path.parents):
        info = part.lstat()
        if stat.S_ISLNK(info.st_mode) or getattr(info, "st_file_attributes", 0) & 0x400:
            raise FixtureError("FIXTURE_PATH")


def generate(output: Path) -> list[dict]:
    """Use an explicit empty directory; retain partial files after any failure."""
    if ".." in output.parts:
        raise FixtureError("FIXTURE_PATH")
    output = output.absolute()
    no_links(output.parent)
    if output.exists() or output.is_symlink():
        no_links(output)
        if not output.is_dir() or any(output.iterdir()):
            raise FixtureError("FIXTURE_EXISTS")
    else:
        output.mkdir(mode=0o700)
    no_links(output)
    records = []
    with ExitStack() as stack:
        streams = [stack.enter_context((output / name).open("xb")) for name in NAMES]
        for name, period, stream in zip(NAMES, (100, 150), streams):
            raw = wav_bytes(period)
            stream.write(raw)
            stream.flush()
            os.fsync(stream.fileno())
            records.append({"name": name, "title": "Synthetic Tone " + name[-5].upper(),
                            "bpm": 120, "tonality": "A" if period == 100 else "D",
                            "bytes": len(raw), "duration_seconds": 10,
                            "sample_rate": 44100, "channels": 2, "bits_per_sample": 16,
                            "sha256": hashlib.sha256(raw).hexdigest()})
    return records


class Parser(argparse.ArgumentParser):
    def error(self, message: str) -> None:
        raise FixtureError("FIXTURE_ARGUMENTS")


def main() -> int:
    try:
        parser = Parser(description=__doc__)
        parser.add_argument("--output", type=Path, required=True)
        print(json.dumps({"status": "LOCAL_FIXTURES", "files": generate(parser.parse_args().output)},
                         sort_keys=True))
        return 0
    except Exception:
        print('{"status":"HOLD","rule":"FIXTURE_REJECTED"}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
