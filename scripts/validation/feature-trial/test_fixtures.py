"""WI063 bounded fixture tests; outputs are retained under build/WI063-* ."""

import hashlib
import importlib.util
from pathlib import Path
import struct
import types
import unittest
from unittest.mock import patch
import uuid
import wave


HERE = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("fixtures", HERE / "fixtures.py")
fixtures = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(fixtures)
ROOT = HERE.parents[2]


class FixturesTest(unittest.TestCase):
    def output(self) -> Path:
        return ROOT / "build" / ("WI063-fixtures-" + uuid.uuid4().hex)

    def test_fixed_pair_and_decoded_bytes(self) -> None:
        output = self.output()
        records = fixtures.generate(output)
        self.assertEqual(len(records), 2)
        self.assertEqual(len({item["sha256"] for item in records}), 2)
        self.assertEqual({p.name for p in output.iterdir()}, set(fixtures.NAMES))
        for item in records:
            path = output / item["name"]
            raw = path.read_bytes()
            self.assertEqual(len(raw), 1764044)
            self.assertEqual(hashlib.sha256(raw).hexdigest(), item["sha256"])
            self.assertEqual(item["sha256"], fixtures.HASHES[item["name"]])
            with wave.open(str(path), "rb") as wav:
                self.assertEqual((wav.getnchannels(), wav.getsampwidth(),
                                  wav.getframerate(), wav.getnframes()), (2, 2, 44100, 441000))
                decoded = wav.readframes(441001)
                self.assertEqual(len(decoded), 1764000)
                self.assertEqual(wav.readframes(1), b"")
                self.assertGreater(max(struct.unpack("<" + "h" * 1000, decoded[:2000])), 0)

    def test_reproducible_without_overwrite(self) -> None:
        first, second = self.output(), self.output()
        a, b = fixtures.generate(first), fixtures.generate(second)
        self.assertEqual(a, b)
        before = {p.name: p.read_bytes() for p in first.iterdir()}
        with self.assertRaises(fixtures.FixtureError):
            fixtures.generate(first)
        self.assertEqual(before, {p.name: p.read_bytes() for p in first.iterdir()})

    def test_nonempty_directory_refused(self) -> None:
        output = self.output()
        output.mkdir()
        (output / "retained.txt").write_bytes(b"retained")
        with self.assertRaises(fixtures.FixtureError):
            fixtures.generate(output)
        self.assertEqual([p.name for p in output.iterdir()], ["retained.txt"])

    def test_traversal_refused(self) -> None:
        with self.assertRaises(fixtures.FixtureError):
            fixtures.generate(ROOT / "build" / ".." / "escape")

    def test_link_refused(self) -> None:
        target, link = self.output(), self.output()
        target.mkdir()
        try:
            link.symlink_to(target, target_is_directory=True)
        except OSError:
            self.skipTest("Windows symlink privilege unavailable")
        with self.assertRaises(fixtures.FixtureError):
            fixtures.generate(link)

    def test_reparse_attribute_refused_without_privilege(self) -> None:
        with patch.object(Path, "lstat", return_value=types.SimpleNamespace(
                st_mode=0o40755, st_file_attributes=0x400)):
            with self.assertRaises(fixtures.FixtureError):
                fixtures.no_links(self.output())

    def test_invalid_period_refused(self) -> None:
        for period in (0, 1, 99, 151):
            with self.assertRaises(fixtures.FixtureError):
                fixtures.wav_bytes(period)


if __name__ == "__main__":
    unittest.main()
