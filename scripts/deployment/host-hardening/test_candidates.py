"""WI019 fixed candidate contracts; these are not native daemon validation."""

from __future__ import annotations

import hashlib
import unittest
from pathlib import Path


DIRECTORY = Path(__file__).resolve().parent
REPO = DIRECTORY.parents[2]


def directives(name: str) -> list[str]:
    return [line for line in (DIRECTORY / name).read_text(encoding="ascii").splitlines()
            if line and not line.startswith("#")]


class CandidateTests(unittest.TestCase):
    def test_ssh_policy_is_exact_and_does_not_restrict_shell_or_accounts(self) -> None:
        lines = directives("00-atstudio-admin.conf")
        expected = {
            "PermitRootLogin": "no", "PubkeyAuthentication": "yes",
            "PasswordAuthentication": "no", "KbdInteractiveAuthentication": "no",
            "PermitEmptyPasswords": "no", "AuthenticationMethods": "publickey",
            "DisableForwarding": "yes", "X11Forwarding": "no",
            "AllowAgentForwarding": "no", "AllowTcpForwarding": "no",
            "AllowStreamLocalForwarding": "no", "PermitTunnel": "no",
        }
        self.assertEqual(len(lines), len(expected))
        self.assertEqual(dict(line.split() for line in lines), expected)

    def test_wrapper_contains_only_ordered_includes(self) -> None:
        self.assertEqual(directives("sshd-validation.conf"), [
            "Include /root/wi019/00-atstudio-admin.conf",
            "Include /etc/ssh/sshd_config",
        ])

    def test_sudo_candidate_has_only_approved_principal_and_authority(self) -> None:
        self.assertEqual(directives("90-atstudio-admin"),
                         ["atadmin ALL=(ALL:ALL) NOPASSWD: ALL"])

    def test_candidates_are_ascii_lf_without_bom(self) -> None:
        for name in ("00-atstudio-admin.conf", "90-atstudio-admin", "sshd-validation.conf"):
            with self.subTest(name=name):
                data = (DIRECTORY / name).read_bytes()
                data.decode("ascii")
                self.assertNotIn(b"\r", data)
                self.assertTrue(data.endswith(b"\n"))

    def test_runbook_references_each_candidate_and_correct_journal_target(self) -> None:
        body = (REPO / "deploy/single-vps/runbooks/host-hardening.md").read_text()
        for name in ("00-atstudio-admin.conf", "90-atstudio-admin", "sshd-validation.conf"):
            self.assertIn(name, body)
        self.assertIn("/etc/systemd/journald.conf.d/zz-atstudio.conf", body)
        self.assertNotIn("/etc/systemd/journald.conf.d/99-atstudio.conf", body)
        self.assertEqual(sorted(["syslog.conf", "zz-atstudio.conf", "99-atstudio.conf"]),
                         ["99-atstudio.conf", "syslog.conf", "zz-atstudio.conf"])
        for root in ("/usr/lib", "/usr/local/lib", "/run", "/etc"):
            self.assertIn(root + "/systemd/journald.conf.d", body)
        self.assertIn("LC_ALL=C", body)
        query = next(line for line in body.splitlines()
                     if line.startswith("timeout 10s") and "mysql" in line)
        self.assertIn("env -u MYSQL_PWD -u MYSQL_HOME -u MYSQL_TEST_LOGIN_FILE "
                      "HOME=/root mysql --no-defaults --protocol=SOCKET", query)
        self.assertIn('--socket="${MYSQL_SOCKET:?}" --user=root', query)
        self.assertNotIn("--no-login-paths", query)
        self.assertNotIn("--password", query)
        for path in ("/root/.mylogin.cnf", "/root/.my.cnf"):
            guard = f"test ! -e {path} && test ! -L {path}"
            self.assertIn(guard, body)
            self.assertLess(body.index(guard), body.index(query))
        self.assertNotIn("/etc/ssh/sshd_config.d/sshd-validation.conf", body)

    def test_existing_mysql_and_journal_source_bytes_preserved(self) -> None:
        expected = {
            "mysql/atstudio.cnf": "008136130093048f395c4f414a937541ad314615e656af7a64a170e4f376a233",
            "journald/atstudio.conf": "229473c1d88e2cf30dac6e9f4ed34327a39a66a74653dc72465e42239b2fc963",
        }
        for name, digest in expected.items():
            with self.subTest(name=name):
                self.assertEqual(hashlib.sha256(
                    (REPO / "deploy/single-vps" / name).read_bytes()).hexdigest(), digest)


if __name__ == "__main__":
    unittest.main()
