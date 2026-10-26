"""WI-20261011-ATS-001: offline fixtures only, never native service operations."""

from __future__ import annotations

from contextlib import redirect_stdout
import importlib.util
import io
import os
from pathlib import Path
import stat
import subprocess
import sys
from types import SimpleNamespace
import unittest
from unittest.mock import Mock, patch


ROOT = Path(__file__).absolute().parents[2]
ASSETS = ROOT / "deploy/single-vps/tls-quarantine"


def load(name: str, path: Path):
    """Import only local public source, without bytecode writes."""
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


render = load("tls_quarantine", Path(__file__).with_name("tls-quarantine.py"))
hook = load("quarantine_reload", ASSETS / "reload-hook.py")
CLIENT = "8.8.8.8/32"


class RenderTests(unittest.TestCase):
    def test_exact_apex(self):
        self.assertEqual(render.validate_inputs("atmusic.co.kr", CLIENT), CLIENT)

    def test_rejects_other_domain_and_directive_injection(self):
        for domain in ("", "www.atmusic.co.kr", "*.atmusic.co.kr", "example.com",
                       "atmusic.co.kr.", "ATMUSIC.CO.KR", "atmusic.co.kr;return 200",
                       "atmusic.co.kr\n", "https://atmusic.co.kr", "atmusic.co.kr:443"):
            with self.subTest(case=domain), self.assertRaises(render.GateError):
                render.validate_inputs(domain, CLIENT)

    def test_rejects_noncanonical_wide_or_multiple_clients(self):
        for client in ("", "8.8.8.8", "8.8.8.8/24", "0.0.0.0/0", "::1/128",
                       "8.8.8.8/32 1.1.1.1/32", "8.8.8.8/32; allow all;",
                       "8.8.8.8/32\n", " 8.8.8.8/32", "008.8.8.8/32",
                       "127.0.0.1/32", "10.0.0.1/32", "224.0.0.1/32",
                       "169.254.1.1/32", "255.255.255.255/32", "8.8.8.8/032"):
            with self.subTest(case=client), self.assertRaises(render.GateError):
                render.validate_inputs(render.DOMAIN, client)

    def test_both_phases_have_only_isolated_roots_and_no_includes(self):
        for phase in ("bootstrap", "tls"):
            text = render.render_config(render.DOMAIN, CLIENT, phase)
            for forbidden in ("proxy_pass", "fastcgi_pass", "uwsgi_pass", "include ",
                              "[::]", "/opt/atstudio", "/uploads", "real_ip_header",
                              "return 200", "return 30", "__", "index.html"):
                self.assertNotIn(forbidden, text)
            self.assertIn("access_log off;", text)
            self.assertIn("error_log /dev/null crit;", text)
            self.assertIn("disable_symlinks on;", text)
            self.assertIn("location ~ ^/\\.well-known/acme-challenge/[A-Za-z0-9_-]+$", text)
            self.assertIn('if ($request_uri !~ "^/\\.well-known/acme-challenge/[A-Za-z0-9_-]+$")', text)
            self.assertIn("try_files $uri =404;", text)
            self.assertNotIn("$request_uri;", text)

    def test_bootstrap_needs_no_certificate_or_443(self):
        text = render.render_config(render.DOMAIN, CLIENT, "bootstrap")
        self.assertNotIn("ssl_certificate", text)
        self.assertNotIn("listen 0.0.0.0:443", text)

    def test_tls_marker_uses_access_phase_not_return_success(self):
        text = render.render_config(render.DOMAIN, CLIENT, "tls")
        location = text.split("location = /ready.txt {", 1)[1].split("}", 1)[0]
        self.assertIn("satisfy all;", location)
        self.assertIn(f"allow {CLIENT};", location)
        self.assertIn("deny all;", location)
        self.assertNotIn("return 200", text)
        self.assertIn("try_files /ready.txt =404;", text)
        self.assertIn("ssl_reject_handshake on;", text)
        self.assertIn("if ($ssl_server_name != atmusic.co.kr)", text)
        self.assertIn('if ($http_host !~ "^atmusic\\.co\\.kr(:443)?$")', text)
        self.assertIn('if ($request_uri != /ready.txt)', text)
        self.assertIn("/etc/letsencrypt/live/atmusic.co.kr/privkey.pem", text)

    def test_invalid_phase_rejected(self):
        with self.assertRaises(render.GateError):
            render.render_config(render.DOMAIN, CLIENT, "production")

    def test_cli_failure_omits_input(self):
        result = subprocess.run(
            [sys.executable, "-B", str(Path(render.__file__)), "--client", "SYNTHETIC-PRIVATE",
             "--phase", "tls"], capture_output=True, text=True, check=False)
        self.assertEqual(result.returncode, 1)
        self.assertEqual(result.stdout, "")
        self.assertEqual(result.stderr.strip(), "TLS_QUARANTINE_HOLD INPUT")

    def test_cli_valid_output_is_deterministic(self):
        result = subprocess.run(
            [sys.executable, "-B", str(Path(render.__file__)), "--client", CLIENT,
             "--phase", "tls"], capture_output=True, text=True, check=False)
        self.assertEqual(result.returncode, 0)
        self.assertEqual(result.stdout, render.render_config(render.DOMAIN, CLIENT, "tls"))

    def test_unknown_cli_option_omits_input(self):
        result = subprocess.run(
            [sys.executable, "-B", str(Path(render.__file__)), "--SYNTHETIC-PRIVATE"],
            capture_output=True, text=True, check=False)
        self.assertEqual(result.returncode, 1)
        self.assertEqual(result.stderr.strip(), "TLS_QUARANTINE_HOLD INPUT")

    def test_unknown_template_placeholder_fails_closed(self):
        with patch.object(Path, "read_text", return_value="__UNREVIEWED__"), \
                self.assertRaises(render.GateError):
            render.render_config(render.DOMAIN, CLIENT, "bootstrap")

    def test_service_dropin_keeps_all_commands_on_quarantine_config(self):
        text = (ASSETS / "nginx-service.conf").read_text(encoding="ascii")
        self.assertIn("ExecStartPre=\n", text)
        self.assertIn("ExecStart=\n", text)
        self.assertIn("ExecReload=\n", text)
        self.assertEqual(text.count("-c /etc/nginx/tls-quarantine/nginx.conf"), 4)
        self.assertNotIn("atstudio.service", text)


def metadata(kind: int, mode: int = 0o755, uid: int = 0, links: int = 1):
    """Create synthetic Linux metadata on any host OS."""
    return SimpleNamespace(st_mode=kind | mode, st_uid=uid, st_gid=0,
                           st_nlink=links, st_size=100)


class HookTests(unittest.TestCase):
    def setUp(self):
        self.env = {"RENEWED_DOMAINS": hook.DOMAIN, "RENEWED_LINEAGE": hook.LINEAGE.as_posix()}

    def test_exact_domain_and_lineage(self):
        hook.check_event(self.env)

    def test_event_cannot_expand_or_redirect(self):
        for key, values in {
            "RENEWED_DOMAINS": ("", "www.atmusic.co.kr", "atmusic.co.kr extra", "atmusic.co.kr\n"),
            "RENEWED_LINEAGE": ("", str(hook.LINEAGE) + "/", "/tmp/atmusic.co.kr",
                                str(hook.LINEAGE) + "-0001", str(hook.LINEAGE) + "/../other"),
        }.items():
            for value in values:
                with self.subTest(field=key), self.assertRaises(hook.GateError):
                    hook.check_event(dict(self.env, **{key: value}))

    def test_missing_event_is_rejected(self):
        with self.assertRaises(hook.GateError):
            hook.check_event({})

    def test_metadata_guards(self):
        for item in (metadata(stat.S_IFREG, 0o666), metadata(stat.S_IFREG, uid=1000),
                     metadata(stat.S_IFREG, links=2), metadata(stat.S_IFLNK),
                     metadata(stat.S_IFIFO)):
            with self.subTest(mode=item.st_mode), self.assertRaises(hook.GateError):
                hook.check_metadata(item, directory=False)

    def test_private_key_must_be_root_only(self):
        hook.check_metadata(metadata(stat.S_IFREG, 0o600), private=True)
        for mode in (0o640, 0o644, 0o660, 0o601):
            with self.subTest(mode=mode), self.assertRaises(hook.GateError):
                hook.check_metadata(metadata(stat.S_IFREG, mode), private=True)

    def test_untrusted_group_and_live_directory_permissions_rejected(self):
        info = metadata(stat.S_IFREG)
        info.st_gid = 1000
        with self.assertRaises(hook.GateError):
            hook.check_metadata(info)
        with self.assertRaises(hook.GateError):
            hook.check_metadata(metadata(stat.S_IFDIR, 0o755), directory=True, private=True)

    def test_empty_or_oversized_archive_file_rejected(self):
        for size in (0, 65537):
            info = metadata(stat.S_IFLNK, 0o777)
            info.st_size = size
            with patch.object(hook, "check_path"), patch.object(Path, "lstat", return_value=info), \
                    patch.object(os, "readlink", return_value="../../archive/atmusic.co.kr/fullchain2.pem"), \
                    self.assertRaises(hook.GateError):
                hook.check_lineage()

    def test_lineage_regular_file_or_nonroot_link_is_rejected(self):
        for info in (metadata(stat.S_IFREG), metadata(stat.S_IFLNK, uid=1000),
                     metadata(stat.S_IFLNK, links=2)):
            with patch.object(hook, "check_path"), patch.object(Path, "lstat", return_value=info), \
                    self.assertRaises(hook.GateError):
                hook.check_lineage()

    def test_each_ancestor_is_checked(self):
        visited = []

        def fake_lstat(path):
            visited.append(path.as_posix())
            return metadata(stat.S_IFDIR if path.as_posix() != "/etc/example.conf" else stat.S_IFREG)

        with patch.object(Path, "lstat", fake_lstat):
            hook.check_path(Path("/etc/example.conf"))
        self.assertEqual(visited, ["/", "/etc", "/etc/example.conf"])

    def test_symlink_ancestor_rejected(self):
        def fake_lstat(path):
            return metadata(stat.S_IFLNK if path.as_posix() == "/etc" else stat.S_IFDIR)

        with patch.object(Path, "lstat", fake_lstat), self.assertRaises(hook.GateError):
            hook.check_path(Path("/etc/example.conf"))

    def test_standard_certbot_pair_is_allowed_without_reading_key(self):
        with patch.object(hook, "check_path") as guard, \
                patch.object(Path, "lstat", return_value=metadata(stat.S_IFLNK, 0o777)), \
                patch.object(os, "readlink", side_effect=[
                    "../../archive/atmusic.co.kr/fullchain2.pem",
                    "../../archive/atmusic.co.kr/privkey2.pem"]), \
                patch.object(Path, "read_bytes", side_effect=AssertionError("No key read")):
            self.assertEqual(hook.check_lineage(), hook.LINEAGE / "fullchain.pem")
        self.assertTrue(any(call.kwargs.get("private") for call in guard.call_args_list))

    def test_link_escape_mismatched_generation_and_missing_key_rejected(self):
        for targets in (["/etc/passwd", "../../archive/atmusic.co.kr/privkey2.pem"],
                        ["../../archive/other/fullchain2.pem", "../../archive/atmusic.co.kr/privkey2.pem"],
                        ["../../archive/atmusic.co.kr/fullchain2.pem", "../../archive/atmusic.co.kr/privkey3.pem"],
                        ["../../archive/atmusic.co.kr/fullchain2.pem", FileNotFoundError()]):
            with self.subTest(targets=str(targets)), patch.object(hook, "check_path"), \
                    patch.object(Path, "lstat", return_value=metadata(stat.S_IFLNK, 0o777)), \
                    patch.object(os, "readlink", side_effect=targets), \
                    self.assertRaises((hook.GateError, OSError)):
                hook.check_lineage()

    def test_san_is_exact_single_domain(self):
        hook.check_san(b"X509v3 Subject Alternative Name:\n    DNS:atmusic.co.kr\n")
        for raw in (b"DNS:atmusic.co.kr", b"X509v3 Subject Alternative Name:\n DNS:*.atmusic.co.kr\n",
                    b"X509v3 Subject Alternative Name:\n DNS:atmusic.co.kr, DNS:other.example\n",
                    b"X509v3 Subject Alternative Name:\n DNS:atmusic.co.kr, IP Address:8.8.8.8\n"):
            with self.assertRaises(hook.GateError):
                hook.check_san(raw)

    def test_native_order_and_no_private_key_command_output(self):
        calls = []

        def run(args, capture=False):
            calls.append(args)
            return b"X509v3 Subject Alternative Name:\n    DNS:atmusic.co.kr\n" if capture else b""

        with patch.object(hook, "check_path"), patch.object(hook, "check_lineage",
                return_value=hook.LINEAGE / "fullchain.pem"), patch.object(hook, "run", run):
            hook.reload_checked(self.env)
        test_pos = next(i for i, c in enumerate(calls) if c[0] == "/usr/sbin/nginx")
        reload_pos = next(i for i, c in enumerate(calls) if "reload" in c)
        self.assertLess(test_pos, reload_pos)
        self.assertEqual(calls[-1], ["/usr/bin/systemctl", "is-active", "--quiet", "nginx.service"])
        self.assertFalse(any("privkey" in argument for call in calls for argument in call))

    def test_failed_nginx_test_never_reloads(self):
        calls = []

        def run(args, capture=False):
            calls.append(args)
            if args[0] == "/usr/sbin/nginx":
                raise hook.GateError("COMMAND")
            return b"X509v3 Subject Alternative Name:\n    DNS:atmusic.co.kr\n" if capture else b""

        with patch.object(hook, "check_path"), patch.object(hook, "check_lineage",
                return_value=hook.LINEAGE / "fullchain.pem"), patch.object(hook, "run", run), \
                self.assertRaises(hook.GateError):
            hook.reload_checked(self.env)
        self.assertFalse(any("reload" in call for call in calls))

    def test_failed_trust_or_inactive_service_never_reloads(self):
        for fail_operation in ("verify", "is-active"):
            calls = []

            def run(args, capture=False):
                calls.append(args)
                if fail_operation in args:
                    raise hook.GateError("COMMAND")
                return b"X509v3 Subject Alternative Name:\n    DNS:atmusic.co.kr\n" if capture else b""

            with patch.object(hook, "check_path"), patch.object(hook, "check_lineage",
                    return_value=hook.LINEAGE / "fullchain.pem"), patch.object(hook, "run", run), \
                    self.assertRaises(hook.GateError):
                hook.reload_checked(self.env)
            self.assertFalse(any("reload" in call for call in calls))

    def test_any_earlier_failure_stops_reload(self):
        for function in ("check_event", "check_lineage", "check_path"):
            with self.subTest(guard=function), \
                    patch.object(hook, "check_event") as event, \
                    patch.object(hook, "check_path") as path, \
                    patch.object(hook, "check_lineage", return_value=hook.LINEAGE / "fullchain.pem") as lineage, \
                    patch.object(hook, "check_san") as san, \
                    patch.object(hook, "run") as native, \
                    patch.object(Path, "lstat", side_effect=AssertionError("Unexpected filesystem access")) as filesystem:
                guards = {"check_event": event, "check_path": path, "check_lineage": lineage}
                target = guards[function]
                target.side_effect = hook.GateError("GUARD")
                with self.assertRaises(hook.GateError) as error:
                    hook.reload_checked(self.env)
                self.assertEqual(str(error.exception), "GUARD")
                expected = {"check_event": (self.env,), "check_path": (hook.CONFIG,), "check_lineage": ()}
                target.assert_called_once_with(*expected[function])
                san.assert_not_called()
                native.assert_not_called()
                filesystem.assert_not_called()

    def test_missing_filesystem_stops_before_lineage_and_native_calls(self):
        with patch.object(hook, "check_event") as event, \
                patch.object(hook, "check_path", wraps=hook.check_path) as path, \
                patch.object(hook, "check_lineage") as lineage, \
                patch.object(hook, "check_san") as san, \
                patch.object(hook, "run") as native, \
                patch.object(Path, "lstat", side_effect=FileNotFoundError("SYNTHETIC_MISSING")) as filesystem:
            with self.assertRaises(FileNotFoundError) as error:
                hook.reload_checked(self.env)
            self.assertEqual(error.exception.args, ("SYNTHETIC_MISSING",))
            event.assert_called_once_with(self.env)
            path.assert_called_once_with(hook.CONFIG)
            filesystem.assert_called_once_with()
            lineage.assert_not_called()
            san.assert_not_called()
            native.assert_not_called()

    def test_subprocess_is_fixed_timeout_clean_env_no_shell(self):
        with patch.object(subprocess, "run", return_value=SimpleNamespace(returncode=0, stdout=b"")) as call:
            hook.run(["/usr/bin/systemctl", "is-active", "--quiet", "nginx.service"])
        options = call.call_args.kwargs
        self.assertEqual(options["timeout"], 20)
        self.assertEqual(options["stderr"], subprocess.DEVNULL)
        self.assertEqual(options["stdout"], subprocess.DEVNULL)
        self.assertEqual(options["env"], {"PATH": "/usr/sbin:/usr/bin", "LC_ALL": "C"})
        self.assertNotIn("shell", options)

    def test_command_failures_are_bounded(self):
        for outcome in (SimpleNamespace(returncode=1, stdout=b"SYNTHETIC-PRIVATE"),
                        subprocess.TimeoutExpired("SYNTHETIC-PRIVATE", 20), OSError("SYNTHETIC-PRIVATE")):
            mock = Mock(side_effect=outcome) if isinstance(outcome, Exception) else Mock(return_value=outcome)
            with patch.object(subprocess, "run", mock), self.assertRaises(hook.GateError) as error:
                hook.run(["/usr/sbin/nginx", "-t"])
            self.assertEqual(str(error.exception), "COMMAND")

    def test_oversized_public_san_output_is_rejected(self):
        with patch.object(subprocess, "run", return_value=SimpleNamespace(returncode=0, stdout=b"x" * 4097)), \
                self.assertRaises(hook.GateError):
            hook.run(["/usr/bin/openssl"], capture=True)

    def test_main_success_names_request_not_served_proof(self):
        output = io.StringIO()
        with patch.object(hook.os, "geteuid", return_value=0, create=True), \
                patch.object(hook.sys, "platform", "linux"), \
                patch.object(hook, "reload_checked"), redirect_stdout(output):
            self.assertEqual(hook.main(), 0)
        self.assertEqual(output.getvalue(), "TLS_QUARANTINE_RELOAD_REQUESTED\n")

    def test_main_reports_only_fixed_failure(self):
        output = io.StringIO()
        with patch.object(hook.os, "geteuid", return_value=0, create=True), \
                patch.object(hook.sys, "platform", "linux"), \
                patch.object(hook, "reload_checked", side_effect=OSError("SYNTHETIC-PRIVATE")), \
                redirect_stdout(output):
            self.assertEqual(hook.main(), 1)
        self.assertEqual(output.getvalue(), "TLS_QUARANTINE_HOLD\n")

    def test_main_refuses_non_linux_or_non_root(self):
        for platform, uid in (("win32", 0), ("linux", 1000)):
            with patch.object(hook.sys, "platform", platform), \
                    patch.object(hook.os, "geteuid", return_value=uid, create=True), \
                    patch.object(hook, "reload_checked") as reload, redirect_stdout(io.StringIO()):
                self.assertEqual(hook.main(), 1)
            reload.assert_not_called()


if __name__ == "__main__":
    unittest.main()
