"""Private synthetic tests only; never read DB or mutate runtime media/processes."""

import json
from pathlib import Path
import subprocess
import unittest
import uuid
import wave

from generate_fixtures import BIN, MIB, ROOT, native_environment, sha256
from probe_pairs import check_pair, contained, decoded_duration, media_info, validate_records


class HelperTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.manifest = json.loads((ROOT / "fixture-manifest.json").read_text(encoding="utf-8"))
        cls.fixtures = {item["name"]: item for item in cls.manifest["files"]}
        cls.fixture = cls.fixtures["WI027-small-valid.wav"]
        cls.source = Path(cls.fixture["path"])
        cls.output = ROOT / "self-test" / uuid.uuid4().hex
        cls.output.mkdir(parents=True)
        cls.pairs = {}
        for name, bitrate, duration in (("valid", "128k", None), ("wrong-bitrate", "96k", None),
                                         ("shortened", "128k", "0.5")):
            path = cls.output / f"{name}.mp3"
            arguments = [str(BIN / "ffmpeg.exe"), "-nostdin", "-hide_banner", "-loglevel",
                         "error", "-n", "-i", str(cls.source), "-c:a", "libmp3lame",
                         "-b:a", bitrate, "-abr", "0", "-threads", "1"]
            if duration:
                arguments += ["-t", duration]
            result = subprocess.run(arguments + [str(path)], capture_output=True,
                                    env=native_environment(), timeout=60, check=False)
            if result.returncode:
                raise RuntimeError("Synthetic MP3 preparation failed")
            cls.pairs[name] = path

    def row(self) -> dict:
        return {"track_id": 1, "fixture_name": self.fixture["name"], "state": "READY",
                "original_relative_path": "tracks/audio/WI027-example.wav",
                "stream_relative_path": "tracks/audio/WI027-example.mp3"}

    def test_all_fixture_hashes_sizes_and_frames(self) -> None:
        self.assertEqual(24, len(self.fixtures))
        for item in self.fixtures.values():
            path = Path(item["path"])
            with self.subTest(name=item["name"]):
                self.assertEqual(item["sha256"], sha256(path))
                self.assertEqual(item["bytes"], path.stat().st_size)
                if item["kind"] == "valid":
                    with wave.open(str(path), "rb") as wav:
                        self.assertEqual((2, 44100, 2, item["frames"]),
                                         (wav.getnchannels(), wav.getframerate(),
                                          wav.getsampwidth(), wav.getnframes()))
                    self.assertAlmostEqual(item["duration_seconds"],
                                           media_info(path)["duration_seconds"], places=5)
        batch = [item for item in self.fixtures.values() if item["name"].startswith("WI027-sine-")]
        self.assertEqual(20, len(batch))
        self.assertEqual(20, len({item["sha256"] for item in batch}))
        self.assertEqual({10 * MIB}, {item["bytes"] for item in batch})

    def test_boundary_full_decodes(self) -> None:
        for name, size in (("WI027-near-100MiB.wav", 100 * MIB - 4096),
                           ("WI027-over-100MiB.wav", 100 * MIB + 4096)):
            item = self.fixtures[name]
            self.assertEqual(size, item["bytes"])
            self.assertAlmostEqual(item["duration_seconds"],
                                   decoded_duration(Path(item["path"]), "wav"), delta=2 / 44100)

    def test_corrupt_rejected(self) -> None:
        with self.assertRaises(ValueError):
            media_info(Path(self.fixtures["WI027-corrupt.wav"]["path"]))

    def test_valid_pair(self) -> None:
        result = check_pair(self.fixture, self.source, self.source, self.pairs["valid"])
        self.assertTrue(result["ok"], result)
        with (self.output / "synthetic-pair-result.json").open("x", encoding="utf-8") as output:
            json.dump(result, output, indent=2)

    def test_wrong_bitrate_rejected(self) -> None:
        result = check_pair(self.fixture, self.source, self.source, self.pairs["wrong-bitrate"])
        self.assertFalse(result["ok"])
        self.assertIn("stream_128000bps", result["errors"])

    def test_shortened_rejected(self) -> None:
        result = check_pair(self.fixture, self.source, self.source, self.pairs["shortened"])
        self.assertFalse(result["ok"])
        self.assertIn("stream_full_duration", result["errors"])

    def test_wrong_original_rejected(self) -> None:
        wrong = Path(self.fixtures["WI027-sine-01.wav"]["path"])
        result = check_pair(self.fixture, self.source, wrong, self.pairs["valid"])
        self.assertFalse(result["ok"])
        self.assertIn("original_preserved", result["errors"])

    def test_record_guards(self) -> None:
        self.assertEqual(1, len(validate_records([self.row()], self.fixtures)))
        invalid = [[], [self.row(), self.row()], [{**self.row(), "state": "FAILED"}],
                   [{**self.row(), "fixture_name": "unrelated.wav"}],
                   [{**self.row(), "original_relative_path": "tracks/audio/../../outside"}],
                   [{**self.row(), "stream_relative_path": "private-uploads/item.mp3"}],
                   [{**self.row(), "password": "unwanted-field"}]]
        for records in invalid:
            with self.subTest(records=records), self.assertRaises(ValueError):
                validate_records(records, self.fixtures)

    def test_path_escape_rejected(self) -> None:
        for path in ("../outside", "C:/outside", "tracks/audio/../../../outside"):
            with self.subTest(path=path), self.assertRaises(ValueError):
                contained(ROOT, path)


if __name__ == "__main__":
    suite = unittest.defaultTestLoader.loadTestsFromTestCase(HelperTests)
    result = unittest.TextTestRunner(verbosity=1).run(suite)
    report = {"scope": "SYNTHETIC_HELPER_TESTS_NOT_RUNTIME_ACCEPTANCE",
              "run": result.testsRun, "failures": len(result.failures), "errors": len(result.errors),
              "ok": result.wasSuccessful(), "artifact_directory": str(HelperTests.output)}
    with (HelperTests.output / "test-result.json").open("x", encoding="utf-8") as output:
        json.dump(report, output, indent=2)
        output.write("\n")
    print(json.dumps(report))
    raise SystemExit(0 if result.wasSuccessful() else 1)
