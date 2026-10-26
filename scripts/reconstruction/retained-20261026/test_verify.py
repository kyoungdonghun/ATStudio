"""Focused filesystem safety checks for the NEW retention helper only."""
import json
import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("retention_verify", Path(__file__).with_name("verify.py"))
verify = importlib.util.module_from_spec(spec)
spec.loader.exec_module(verify)


class RetentionTests(unittest.TestCase):
    def fixture(self, root):
        archive = root / "archive"
        path = archive / "files/build/example.py"
        path.parent.mkdir(parents=True)
        path.write_bytes(b"# retained, not executed\r\n")
        pin = verify.digest(path.read_bytes())
        entry = dict(source="build/example.py", archive="files/build/example.py",
                     mode="exact", bytes=path.stat().st_size,
                     source_sha256=pin, archive_sha256=pin, redacted_categories=[])
        (archive / "manifest.json").write_text(json.dumps(dict(
            schema="ats-retained-source-v1", records=[entry])), encoding="utf-8")
        return archive, path

    def test_exact_roundtrip_and_existing_destination_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            archive, original = self.fixture(root)
            _, payload = verify.verify(archive)
            destination = root / "new"
            verify.restore(destination, payload, archive)
            self.assertEqual(original.read_bytes(), (destination / "build/example.py").read_bytes())
            with self.assertRaisesRegex(ValueError, "DESTINATION_EXISTS"):
                verify.restore(destination, payload, archive)

    def test_corruption_rejected_before_restore(self):
        with tempfile.TemporaryDirectory() as temp:
            archive, original = self.fixture(Path(temp))
            original.write_bytes(b"different")
            with self.assertRaisesRegex(ValueError, "ARCHIVE_HASH"):
                verify.verify(archive)

    def test_path_escape_and_unexpected_member_rejected(self):
        for name in ("../outside", "/outside", "build/../outside", "build\\outside", "build//x", "C:/x"):
            with self.assertRaises(ValueError):
                verify.safe_relative(name)
        with tempfile.TemporaryDirectory() as temp:
            archive, _ = self.fixture(Path(temp))
            (archive / "files/build/extra.py").write_bytes(b"not selected")
            with self.assertRaisesRegex(ValueError, "UNEXPECTED_MEMBER"):
                verify.verify(archive)

    def test_reference_not_restored_as_executable_source(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            archive, _ = self.fixture(root)
            _, payload = verify.verify(archive)
            raw = b"private binding removed"
            item = dict(source="build/bound.py", mode="reference-only",
                        archive_sha256=verify.digest(raw))
            payload.append((item, raw))
            dest = root / "new"
            result = verify.restore(dest, payload, archive)
            self.assertEqual(result["references_copied_separately"], 1)
            self.assertFalse((dest / "build/bound.py").exists())
            self.assertEqual((dest / "reference-only/build/bound.py.reference.txt").read_bytes(), raw)


if __name__ == "__main__":
    unittest.main()
