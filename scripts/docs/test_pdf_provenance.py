"""Focused tests for deterministic client PDF provenance inputs."""

from __future__ import annotations

import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from pdf_provenance import (
    normalized_text_sha256,
    normalized_text_size,
    validate_font_input,
    write_utf8_lf_text,
)


class PdfProvenanceTest(unittest.TestCase):
    def test_committed_manifest_sources_use_the_normalized_contract(self) -> None:
        repo_root = Path(__file__).resolve().parents[2]
        manifest_path = (
            repo_root / "output/pdf/atstudio-client-testing-guide.manifest.json"
        )
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))

        for source in manifest["sources"]:
            path = repo_root / source["path"]
            with self.subTest(source=source["path"]):
                self.assertEqual(source["sha256"], normalized_text_sha256(path))
                self.assertEqual(source["bytes"], normalized_text_size(path))

    def test_lf_crlf_and_cr_sources_have_identical_hashes_and_sizes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            paths = {
                "lf": root / "lf.md",
                "crlf": root / "crlf.md",
                "cr": root / "cr.md",
            }
            paths["lf"].write_bytes("첫 줄\nsecond\n".encode("utf-8"))
            paths["crlf"].write_bytes("첫 줄\r\nsecond\r\n".encode("utf-8"))
            paths["cr"].write_bytes("첫 줄\rsecond\r".encode("utf-8"))

            hashes = {normalized_text_sha256(path) for path in paths.values()}
            sizes = {normalized_text_size(path) for path in paths.values()}

            self.assertEqual(1, len(hashes))
            self.assertEqual(1, len(sizes))

    def test_manifest_text_writer_emits_only_raw_lf_line_endings(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            manifest = Path(directory) / "manifest.json"
            write_utf8_lf_text(
                manifest,
                '{\r\n  "title": "AT.M",\r  "status": "PASS"\n}\r\n',
            )

            raw = manifest.read_bytes()

            self.assertEqual(
                b'{\n  "title": "AT.M",\n  "status": "PASS"\n}\n',
                raw,
            )
            self.assertNotIn(b"\r\n", raw)
            self.assertNotIn(b"\r", raw)

    def test_font_preflight_rejects_missing_and_unexpected_inputs(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            missing = root / "missing.ttf"
            with self.assertRaisesRegex(FileNotFoundError, "Required Malgun Gothic test font"):
                validate_font_input(
                    missing,
                    label="Malgun Gothic test",
                    expected_sha256="0" * 64,
                )

            unexpected = root / "unexpected.ttf"
            unexpected.write_bytes(b"not-the-approved-font")
            with self.assertRaisesRegex(ValueError, "Unexpected Malgun Gothic test font input"):
                validate_font_input(
                    unexpected,
                    label="Malgun Gothic test",
                    expected_sha256="0" * 64,
                )

    def test_font_preflight_accepts_the_expected_hash(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            font = Path(directory) / "approved.ttf"
            font.write_bytes(b"approved-font-fixture")
            expected_sha256 = hashlib.sha256(font.read_bytes()).hexdigest()

            resolved = validate_font_input(
                font,
                label="Malgun Gothic test",
                expected_sha256=expected_sha256,
            )

            self.assertEqual(font.resolve(), resolved)


if __name__ == "__main__":
    unittest.main()
