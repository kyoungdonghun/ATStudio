"""WI019 synthetic metadata tests; no host account or filesystem changes."""

from __future__ import annotations

import contextlib
import io
import stat
import unittest
from pathlib import PurePosixPath
from types import SimpleNamespace
from unittest.mock import Mock, patch

import check_layout as app


OWNERS = {"root": (0, 0), "atstudio": (991, 991), "nginx": (33, 33)}


def directory(uid: int = 0, gid: int = 0, mode: int = 0o755,
              inode: int = 1) -> SimpleNamespace:
    return SimpleNamespace(st_mode=stat.S_IFDIR | mode, st_uid=uid, st_gid=gid,
                           st_dev=1, st_ino=inode)


def fixture(after: bool = False) -> dict[str, SimpleNamespace]:
    paths = {"/": directory()}
    for name in app.LAYOUT:
        for parent in PurePosixPath(name).parents:
            if str(parent) not in app.LAYOUT:
                paths[str(parent)] = directory(inode=len(paths) + 1)
    if after:
        for name, (owner, mode) in app.LAYOUT.items():
            paths[name] = directory(*OWNERS[owner], mode, len(paths) + 1)
    return paths


class LayoutTests(unittest.TestCase):
    def check(self, paths: dict, phase: str = "before", attrs: tuple = ()) -> None:
        def lstat(path: str) -> SimpleNamespace:
            if path not in paths:
                raise FileNotFoundError
            return paths[path]
        with patch.object(app.os, "lstat", side_effect=lstat), \
                patch.object(app, "read_attributes", return_value=attrs):
            app.check_layout(phase, OWNERS)

    def test_empty_layout_accepts(self) -> None:
        self.check(fixture())

    def test_exact_created_layout_accepts(self) -> None:
        self.check(fixture(True), "after")

    def test_any_existing_owned_path_rejects_even_correct_directory(self) -> None:
        for name in app.LAYOUT:
            paths = fixture()
            paths[name] = directory()
            with self.subTest(name=name), self.assertRaises(app.GateError):
                self.check(paths)

    def test_symlink_in_each_ancestor_rejects(self) -> None:
        for name in fixture():
            paths = fixture()
            paths[name].st_mode = stat.S_IFLNK | 0o777
            with self.subTest(name=name), self.assertRaises(app.GateError):
                self.check(paths)

    def test_dangling_leaf_symlink_rejects(self) -> None:
        paths = fixture()
        paths["/srv/atstudio"] = directory()
        paths["/srv/atstudio"].st_mode = stat.S_IFLNK | 0o777
        with self.assertRaises(app.GateError):
            self.check(paths)

    def test_non_directory_ancestor_rejects(self) -> None:
        paths = fixture()
        paths["/srv"].st_mode = stat.S_IFREG | 0o644
        with self.assertRaises(app.GateError):
            self.check(paths)

    def test_missing_system_ancestor_rejects(self) -> None:
        paths = fixture()
        del paths["/srv"]
        with self.assertRaises(app.GateError):
            self.check(paths)

    def test_untrusted_ancestor_owner_and_write_permissions_reject(self) -> None:
        for uid, mode in ((991, 0o755), (0, 0o775), (0, 0o777), (0, 0o1777)):
            paths = fixture()
            paths["/var/lib"] = directory(uid=uid, mode=mode)
            with self.subTest(uid=uid, mode=mode), self.assertRaises(app.GateError):
                self.check(paths)

    def test_posix_access_and_default_acl_reject(self) -> None:
        for attr in ("system.posix_acl_access", "system.posix_acl_default"):
            with self.subTest(attr=attr), self.assertRaises(app.GateError):
                self.check(fixture(), attrs=(attr,))

    def test_other_metadata_attribute_is_allowed(self) -> None:
        self.check(fixture(), attrs=("security.selinux",))

    def test_stat_permission_error_propagates_to_cli(self) -> None:
        with patch.object(app.os, "lstat", side_effect=PermissionError), \
                self.assertRaises(PermissionError):
            app.check_layout("before", OWNERS)

    def test_after_missing_path_wrong_owner_group_mode_or_link_reject(self) -> None:
        for field, value in (("st_uid", 0), ("st_gid", 0),
                             ("st_mode", stat.S_IFDIR | 0o750),
                             ("st_mode", stat.S_IFLNK | 0o700)):
            paths = fixture(True)
            setattr(paths["/srv/atstudio/private"], field, value)
            with self.subTest(field=field, value=value), self.assertRaises(app.GateError):
                self.check(paths, "after")
        paths = fixture(True)
        del paths["/srv/atstudio/private"]
        with self.assertRaises(app.GateError):
            self.check(paths, "after")

    def test_aliasing_directory_inodes_reject(self) -> None:
        paths = fixture(True)
        paths["/srv/atstudio/private"] = paths["/srv/atstudio/public"]
        with self.assertRaises(app.GateError):
            self.check(paths, "after")

    def test_unknown_phase_rejects(self) -> None:
        with self.assertRaises(app.GateError):
            app.check_layout("apply", OWNERS)

    def test_cli_refuses_non_linux_before_inspection(self) -> None:
        with patch.object(app.sys, "platform", "win32"), \
                patch.object(app, "check_layout") as check, \
                contextlib.redirect_stdout(io.StringIO()) as output:
            self.assertEqual(app.main(["before"]), 2)
            check.assert_not_called()
            self.assertIn("HOLD", output.getvalue())

    def test_cli_masks_operating_system_error(self) -> None:
        with patch.object(app.sys, "platform", "linux"), \
                patch.object(app.os, "geteuid", return_value=0, create=True), \
                patch.object(app, "check_layout", side_effect=OSError("private detail")), \
                contextlib.redirect_stdout(io.StringIO()) as output:
            self.assertEqual(app.main(["before"]), 2)
            self.assertNotIn("private detail", output.getvalue())

    def test_acl_inspection_failure_is_not_ignored(self) -> None:
        with patch.object(app.os, "lstat", return_value=directory()), \
                patch.object(app, "read_attributes", side_effect=PermissionError), \
                self.assertRaises(PermissionError):
            app.check_layout("before", OWNERS)

    def test_cli_refuses_unprivileged_linux_before_inspection(self) -> None:
        with patch.object(app.sys, "platform", "linux"), \
                patch.object(app.os, "geteuid", return_value=1000, create=True), \
                patch.object(app, "check_layout") as check, \
                contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(app.main(["before"]), 2)
            check.assert_not_called()

    def test_cli_success_is_read_only(self) -> None:
        with patch.object(app.sys, "platform", "linux"), \
                patch.object(app.os, "geteuid", return_value=0, create=True), \
                patch.object(app, "check_layout") as check, \
                contextlib.redirect_stdout(io.StringIO()) as output:
            self.assertEqual(app.main(["before"]), 0)
            check.assert_called_once_with("before", {})
            self.assertIn("read-only", output.getvalue())

    def test_cli_after_requires_resolved_identity(self) -> None:
        with patch.object(app.sys, "platform", "linux"), \
                patch.object(app.os, "geteuid", return_value=0, create=True), \
                patch.object(app, "resolve_owners", side_effect=app.GateError("identity-required")), \
                patch.object(app, "check_layout") as check, \
                contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(app.main(["after"]), 2)
            check.assert_not_called()

    def test_resolves_only_named_safe_accounts(self) -> None:
        service = SimpleNamespace(pw_uid=991, pw_gid=991, pw_shell="/usr/sbin/nologin",
                                  pw_dir="/nonexistent")
        nginx = SimpleNamespace(pw_uid=33, pw_gid=33)
        lookup = Mock(side_effect=[service, nginx])
        with patch.dict(app.sys.modules, {"pwd": SimpleNamespace(getpwnam=lookup)}):
            self.assertEqual(app.resolve_owners("www-data"), OWNERS)
            self.assertEqual([call.args[0] for call in lookup.call_args_list],
                             ["atstudio", "www-data"])

    def test_unsafe_identity_values_reject(self) -> None:
        with patch.dict(app.sys.modules, {"pwd": SimpleNamespace()}):
            for value in (None, "", "root", "atadmin", "atstudio"):
                with self.subTest(value=value), self.assertRaises(app.GateError):
                    app.resolve_owners(value)
        for field, value in (("pw_uid", 0), ("pw_uid", 33), ("pw_gid", 0),
                             ("pw_shell", "/bin/bash"), ("pw_dir", "/home/atstudio")):
            service = SimpleNamespace(pw_uid=991, pw_gid=991,
                                      pw_shell="/usr/sbin/nologin", pw_dir="/nonexistent")
            setattr(service, field, value)
            lookup = Mock(side_effect=[service, SimpleNamespace(pw_uid=33, pw_gid=33)])
            with self.subTest(field=field, value=value), \
                    patch.dict(app.sys.modules, {"pwd": SimpleNamespace(getpwnam=lookup)}), \
                    self.assertRaises(app.GateError):
                app.resolve_owners("www-data")

    def test_after_privileged_nginx_rejects(self) -> None:
        for uid, gid in ((0, 33), (33, 0)):
            service = SimpleNamespace(pw_uid=991, pw_gid=991,
                                      pw_shell="/usr/sbin/nologin", pw_dir="/nonexistent")
            lookup = Mock(side_effect=[service, SimpleNamespace(pw_uid=uid, pw_gid=gid)])
            with self.subTest(uid=uid, gid=gid), \
                    patch.dict(app.sys.modules, {"pwd": SimpleNamespace(getpwnam=lookup)}), \
                    self.assertRaises(app.GateError):
                app.resolve_owners("www-data")

    def test_cli_missing_account_does_not_leak_name(self) -> None:
        with patch.object(app.sys, "platform", "linux"), \
                patch.object(app.os, "geteuid", return_value=0, create=True), \
                patch.object(app, "resolve_owners", side_effect=KeyError("private-name")), \
                contextlib.redirect_stdout(io.StringIO()) as output:
            self.assertEqual(app.main(["after", "--nginx-user", "www-data"]), 2)
            self.assertNotIn("private-name", output.getvalue())


if __name__ == "__main__":
    unittest.main()
