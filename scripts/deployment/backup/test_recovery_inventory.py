"""Synthetic-only WI017 tests; no private files or native secrets read."""
import hashlib
import stat
import unittest
from types import SimpleNamespace
from unittest.mock import patch

import recovery_inventory as inventory
from backup_protocol import GateError


def info(mode=stat.S_IFREG | 0o600, links=1, size=3):
    return SimpleNamespace(st_dev=1, st_ino=2, st_size=size, st_mtime_ns=1,
                           st_ctime_ns=1, st_mode=mode, st_uid=10, st_nlink=links)


class InventoryTests(unittest.TestCase):
    def entries(self):
        return [{"path": "/synthetic/" + c, "category": c,
                 "key_ids": ["retained-a", "retained-b"] if c == "billing_keyring" else []}
                for c in sorted(inventory.CATEGORIES)]

    def test_collect_safe_summary_and_private_manifest(self):
        with patch.object(inventory, "secure_read", return_value={"bytes": 3, "sha256": "a" * 64}):
            result = inventory.collect(self.entries(), ["retained-a", "retained-b"])
        self.assertEqual(result["public"]["files"], 6)
        self.assertNotIn("synthetic", str(result["public"]))
        self.assertNotIn("aaaa", str(result["public"]))
        self.assertEqual(result["public"]["semantic_key_completeness"], "UNVERIFIED")

    def test_missing_category_and_retained_key_fail_before_read(self):
        with patch.object(inventory, "secure_read") as reader:
            with self.assertRaisesRegex(GateError, "RECOVERY_CATEGORY_MISSING"):
                inventory.collect(self.entries()[:-1], ["retained-a", "retained-b"])
            with self.assertRaisesRegex(GateError, "RETAINED_KEY_SET_MISMATCH"):
                inventory.collect(self.entries(), ["retained-a"])
            reader.assert_not_called()

    def test_total_bound(self):
        with patch.object(inventory, "secure_read", return_value={"bytes": 3}):
            with self.assertRaisesRegex(GateError, "TOTAL_BOUND"):
                inventory.collect(self.entries(), ["retained-a", "retained-b"], total_limit=3)

    def run_read(self, file_info=None, read=None, named=None, private=True):
        root = info(stat.S_IFDIR | 0o755, size=0)
        file_info = file_info or info()
        with patch.multiple(inventory.os, name="posix", O_NOFOLLOW=0x20000,
                            O_DIRECTORY=0x10000, O_NONBLOCK=0x800, create=True), \
             patch.object(inventory.os, "geteuid", return_value=10, create=True), \
             patch.object(inventory.os, "open", side_effect=[10, 11]), \
             patch.object(inventory.os, "fstat", side_effect=[root, file_info, file_info]), \
             patch.object(inventory.os, "read", side_effect=read or [b"abc", b""]), \
             patch.object(inventory.os, "stat", return_value=named or file_info), \
             patch.object(inventory.os, "close"):
            return inventory.secure_read("/synthetic", 3, private=private)

    def test_fake_descriptor_hash(self):
        self.assertEqual(self.run_read()["sha256"], hashlib.sha256(b"abc").hexdigest())

    def test_hardlink_and_unsafe_mode(self):
        for candidate, code in [(info(links=2), "REGULAR_SINGLE_LINK_REQUIRED"),
                                (info(mode=stat.S_IFREG | 0o644), "PRIVATE_FILE_MODE"),
                                (info(mode=stat.S_IFIFO | 0o600), "REGULAR_SINGLE_LINK_REQUIRED")]:
            with self.assertRaisesRegex(GateError, code):
                self.run_read(candidate)

    def test_replacement_and_bounded_read(self):
        changed = info()
        changed.st_ino = 99
        with self.assertRaisesRegex(GateError, "FILE_CHANGED"):
            self.run_read(named=changed)
        with self.assertRaisesRegex(GateError, "READ_BOUND"):
            self.run_read(read=[b"abcd"])

    def test_windows_fails_closed(self):
        with patch.object(inventory.os, "name", "nt"), patch.object(inventory.os, "open") as opened:
            with self.assertRaisesRegex(GateError, "NATIVE_PERMISSION_CHECK_UNAVAILABLE"):
                inventory.secure_read("C:/synthetic", 3)
            opened.assert_not_called()

    def test_explicit_nonsecret_readable_config(self):
        self.assertEqual(self.run_read(info(mode=stat.S_IFREG | 0o644), private=False)["bytes"], 3)
        with self.assertRaisesRegex(GateError, "PRIVATE_FILE_MODE"):
            self.run_read(info(mode=stat.S_IFREG | 0o666), private=False)
        with self.assertRaisesRegex(GateError, "UNSAFE_SPECIAL_MODE"):
            self.run_read(info(mode=stat.S_IFREG | 0o4600))


if __name__ == "__main__":
    unittest.main()
