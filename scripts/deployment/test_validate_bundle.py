"""Synthetic-only regressions for the WI-003/WI-012 deployment template audit."""

from __future__ import annotations

import contextlib
import hashlib
import io
import json
import os
import re
import socket
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest import mock
from urllib.parse import unquote, urlsplit

import validate_bundle as audit


REPOSITORY = Path(__file__).absolute().parents[2]
SENTINEL = "PRIVATE_SENTINEL_must_not_appear_987654"


class BundleTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory(prefix="ats-wi012-")
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        for relative in audit.TEMPLATE_PATHS | {audit.SOURCE}:
            target = self.root / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes((REPOSITORY / relative).read_bytes())

    def edit(self, relative: str, before: str, after: str) -> None:
        path = self.root / relative
        text = path.read_text(encoding="utf-8")
        self.assertIn(before, text)
        path.write_text(text.replace(before, after), encoding="utf-8", newline="\n")

    def contract(self) -> dict:
        return json.loads((self.root / audit.CONTRACT).read_text(encoding="utf-8"))

    def save_contract(self, value: object) -> None:
        (self.root / audit.CONTRACT).write_text(json.dumps(value) + "\n", encoding="utf-8")

    def assert_failed(self, rule: str | None = None) -> dict:
        result = audit.validate(self.root)
        self.assertEqual("FAIL", result["template_status"])
        self.assertFalse(result["deployment_ready"])
        self.assertEqual("HOLD", result["production_status"])
        self.assertNotIn(SENTINEL, json.dumps(result))
        if rule:
            self.assertIn(rule, {error["rule"] for error in result["errors"]})
        return result

    def test_baseline_has_unresolved_gates_not_deployment_pass(self) -> None:
        result = audit.validate(self.root)
        self.assertEqual([], result["errors"])
        self.assertEqual("PASS", result["template_status"])
        self.assertFalse(result["deployment_ready"])
        self.assertEqual(7, len(result["target_gates"]))
        self.assertEqual({"HOLD", "NOT_RUN"}, {g["status"] for g in result["target_gates"]})
        self.assertEqual(83, len(audit.EXPECTED_ENV))
        self.assertEqual(11, len(audit.TEMPLATE_PATHS))

    def test_every_required_file_missing_fails(self) -> None:
        for relative in sorted(audit.TEMPLATE_PATHS | {audit.SOURCE}):
            with self.subTest(path=relative):
                path = self.root / relative
                original = path.read_bytes()
                path.unlink()
                self.assert_failed("FILE_READ_UTF8")
                path.write_bytes(original)

    def test_every_environment_entry_missing_fails(self) -> None:
        path = self.root / audit.ENV
        original = path.read_text(encoding="utf-8")
        for key in audit.EXPECTED_ENV:
            with self.subTest(key=key):
                path.write_text("\n".join(line for line in original.splitlines()
                                          if not line.startswith(key + "=")) + "\n", encoding="utf-8")
                self.assert_failed("ENV_LITERAL." + key)
        path.write_text(original, encoding="utf-8")

    def test_every_fixed_literal_and_placeholder_rejects_drift(self) -> None:
        path = self.root / audit.ENV
        original = path.read_text(encoding="utf-8")
        for key in audit.EXPECTED_ENV:
            with self.subTest(key=key):
                path.write_text("\n".join(key + "=" + SENTINEL if line.startswith(key + "=")
                                          else line for line in original.splitlines()) + "\n", encoding="utf-8")
                self.assert_failed("ENV_LITERAL." + key)

    def test_public_backend_and_bootstrap_provider_activation(self) -> None:
        path = self.root / audit.ENV
        original = path.read_text(encoding="utf-8")
        for key, value in {
            "SERVER_ADDRESS": "0.0.0.0", "APP_ACCEPTANCE_ENABLED": "true",
            "APP_BOOTSTRAP_TEST_USERS_ENABLED": "true", "TOSS_SECRET_KEY": "live_" + SENTINEL,
            "GOOGLE_CLIENT_ID": SENTINEL, "APP_AUTH_PASSWORD_LOGIN_ENABLED": "true",
            "SPRING_JPA_HIBERNATE_DDL_AUTO": "update",
        }.items():
            with self.subTest(key=key):
                path.write_text(original, encoding="utf-8")
                self.edit(audit.ENV, key + "=" + audit.EXPECTED_ENV[key], key + "=" + value)
                self.assert_failed("ENV_LITERAL." + key)

    def test_env_duplicate_malformed_shell_and_multiline_rejected(self) -> None:
        path = self.root / audit.ENV
        original = path.read_text(encoding="utf-8")
        for line in ["SERVER_PORT=8080", SENTINEL, "export A=" + SENTINEL,
                     "A=\"" + SENTINEL, "A=$(" + SENTINEL + ")", "A=`" + SENTINEL + "`",
                     "A=hello\\", "A=hello world", "a=" + SENTINEL, "A=one # " + SENTINEL]:
            with self.subTest(case=line[:12]):
                path.write_text(original + line + "\n", encoding="utf-8")
                self.assert_failed("ENV_SYNTAX_DUPLICATE")

    def test_unknown_env_key_never_echoed(self) -> None:
        with (self.root / audit.ENV).open("a", encoding="utf-8") as stream:
            stream.write(SENTINEL.upper() + "=" + SENTINEL + "\n")
        result = self.assert_failed("ENV_KEYS")
        self.assertNotIn(SENTINEL.upper(), json.dumps(result))

    def test_json_malformed_duplicate_nonfinite_deep_or_wrong_shape(self) -> None:
        path = self.root / audit.CONTRACT
        for value in ['{"' + SENTINEL + '":', '{"x":1,"x":2}', '["' + SENTINEL + '"]',
                      '{"schema_version":NaN}', '[' * 1500 + '0' + ']' * 1500]:
            with self.subTest(case=value[:12]):
                path.write_text(value + "\n", encoding="utf-8")
                self.assert_failed()

    def test_contract_version_and_malformed_sections(self) -> None:
        original = self.contract()
        for key in ("schema_version", "required_template_paths", "required_env", "safe_literals",
                    "must_be_empty", "required_placeholders", "deployment_roots", "nginx", "target_gates"):
            for invalid in (None, True, SENTINEL, [], {SENTINEL: SENTINEL}):
                with self.subTest(key=key, kind=type(invalid).__name__):
                    self.save_contract(original | {key: invalid})
                    self.assert_failed()

    def test_contract_path_injection_never_opens_unlisted_file(self) -> None:
        original = self.contract()
        for value in ("../" + SENTINEL, "/etc/" + SENTINEL, "C:/" + SENTINEL,
                      "\\\\host\\" + SENTINEL, "application-local.yml",
                      "deploy/single-vps/env/../" + SENTINEL, SENTINEL):
            with self.subTest(case=value):
                self.save_contract(original | {"required_template_paths": original["required_template_paths"] + [value]})
                opened: list[str] = []
                real_read = audit.read_text

                def observed(root: Path, relative: str, **kwargs: object) -> str:
                    opened.append(relative)
                    return real_read(root, relative, **kwargs)

                with mock.patch.object(audit, "read_text", side_effect=observed):
                    self.assert_failed("CONTRACT_PATHS")
                self.assertEqual([audit.CONTRACT], opened)

    def test_contract_cannot_weaken_safety_with_env(self) -> None:
        contract = self.contract()
        contract["safe_literals"]["SERVER_ADDRESS"] = "0.0.0.0"
        self.save_contract(contract)
        self.edit(audit.ENV, "SERVER_ADDRESS=127.0.0.1", "SERVER_ADDRESS=0.0.0.0")
        self.assert_failed("CONTRACT_SAFETY")

    def test_contract_omitted_or_duplicate_key_obligations_fail(self) -> None:
        original = self.contract()
        for field in ("required_env", "must_be_empty", "required_template_paths"):
            for changed in (original[field][1:], original[field] + original[field][:1]):
                self.save_contract(original | {field: changed})
                self.assert_failed()

    def test_contract_placeholder_root_include_and_gate_drift_fail(self) -> None:
        original = self.contract()
        for section, key, value in (
            ("required_placeholders", "__JWT_BASE64_32_BYTES_MINIMUM__", {"env": [], "kind": "secret"}),
            ("deployment_roots", "private_storage", "/srv/atstudio/public"),
            ("nginx", "include_targets", {"/etc/private/" + SENTINEL: [SENTINEL]}),
        ):
            contract = json.loads(json.dumps(original))
            contract[section][key] = value
            self.save_contract(contract)
            self.assert_failed()
        original["target_gates"][0]["status"] = "PASS"
        self.save_contract(original)
        self.assert_failed("CONTRACT_GATES")

    def test_each_active_configuration_line_is_pinned(self) -> None:
        for relative in audit.SHAPES:
            path = self.root / (audit.BASE + relative)
            original = path.read_text(encoding="utf-8")
            lines = original.splitlines(keepends=True)
            for index, line in enumerate(lines):
                if not line.strip() or line.lstrip().startswith("#"):
                    continue
                with self.subTest(path=relative, line=index + 1):
                    path.write_text("".join(lines[:index] + lines[index + 1:]), encoding="utf-8")
                    self.assert_failed("TEMPLATE_IDENTITY_DRIFT")
            path.write_text(original, encoding="utf-8")

    def test_trust_storage_acl_and_single_service_regressions(self) -> None:
        mutations = [
            ("nginx/proxy.conf.template", "$remote_addr", "$http_x_forwarded_for"),
            ("nginx/proxy.conf.template", "proxy_pass_request_headers off;", "proxy_pass_request_headers on;"),
            ("nginx/https.conf.template", "tracks/audio|", ""),
            ("nginx/https.conf.template", "location /uploads/", "location ^~ /uploads/"),
            ("nginx/https.conf.template", "include /etc/atstudio/nginx/restricted-access.conf;", "allow all;"),
            ("nginx/restricted-access.conf", "deny all;", "allow all;"),
            ("systemd/atstudio.service", "User=atstudio", "User=root"),
            ("systemd/atstudio.service", "KillMode=control-group", "KillMode=process"),
            ("systemd/atstudio.service", "Type=simple", "Type=forking"),
            ("mysql/atstudio.cnf", "bind-address=127.0.0.1", "bind-address=0.0.0.0"),
        ]
        for relative, before, after in mutations:
            relative = audit.BASE + relative
            path = self.root / relative
            original = path.read_bytes()
            self.edit(relative, before, after)
            self.assert_failed("TEMPLATE_IDENTITY_DRIFT")
            path.write_bytes(original)

    def test_added_overrides_and_logging_content_fail_redacted(self) -> None:
        for relative in audit.SHAPES:
            path = self.root / (audit.BASE + relative)
            original = path.read_bytes()
            path.write_bytes(original + ("# fixture\n" + SENTINEL + "\n").encode())
            self.assert_failed("TEMPLATE_IDENTITY_DRIFT")
            path.write_bytes(original)

    def test_both_newline_styles_and_comment_changes_are_supported(self) -> None:
        for relative in audit.TEMPLATE_PATHS - {audit.CONTRACT}:
            path = self.root / relative
            content = path.read_text(encoding="utf-8")
            path.write_bytes(content.replace("\n", "\r\n").encode())
        path = self.root / (audit.BASE + "nginx/proxy.conf.template")
        path.write_bytes(path.read_bytes() + b"# Additional reviewed explanation.\r\n")
        self.assertEqual("PASS", audit.validate(self.root)["template_status"])

    def test_media_positive_negative_cases_and_limits(self) -> None:
        path = self.root / (audit.BASE + "nginx/https.conf.template")
        original = path.read_text(encoding="utf-8")
        self.assertEqual([], audit.nginx_regressions(original))
        self.assertEqual(9, sum(audit.MEDIA_CASES.values()))
        self.assertEqual(7, sum(not value for value in audit.MEDIA_CASES.values()))
        for line in ("limit_req zone=ats_media burst=8 nodelay;", "limit_conn ats_client 8;",
                     "limit_conn ats_server 32;", "limit_conn ats_media_client 4;",
                     "limit_conn ats_media_server 16;", "limit_conn ats_download_server 2;",
                     "limit_conn ats_download_client 1;", "limit_conn ats_multipart_server 2;",
                     "limit_conn ats_multipart_client 1;", "limit_rate $ats_media_rate;"):
            self.assertIn("NGINX_MEDIA_LIMITS", audit.nginx_regressions(original.replace(line, "")))

    def assert_reapproved_mutation_fails(self, relative: str, before: str,
                                       after: str, rule: str) -> None:
        path = self.root / (audit.BASE + relative)
        original = path.read_bytes()
        self.edit(audit.BASE + relative, before, after)
        shape = audit.active_shape(path.read_text(encoding="utf-8"))
        with mock.patch.dict(audit.SHAPES, {relative: shape}):
            result = self.assert_failed(rule)
        self.assertNotIn("TEMPLATE_IDENTITY_DRIFT", {e["rule"] for e in result["errors"]})
        path.write_bytes(original)

    def test_resource_contract_is_independent_of_reapproved_hash(self) -> None:
        for relative, before, after in (
            ("systemd/atstudio.service", "-Xmx1024m", "-Xmx4096m"),
            ("systemd/atstudio.service", "-Xms256m", "-Xms128m"),
            ("systemd/atstudio.service", "MaxMetaspaceSize=256m", "MaxMetaspaceSize=512m"),
            ("systemd/atstudio.service", "MemoryHigh=2G", "MemoryHigh=4G"),
            ("systemd/atstudio.service", "MemoryMax=3G", "MemoryMax=8G"),
            ("systemd/atstudio.service", "TasksMax=256", "TasksMax=512"),
            ("systemd/atstudio.service", "MemoryMax=3G", "MemoryMax=3G\nMemoryMax=8G"),
            ("mysql/atstudio.cnf", "innodb-buffer-pool-size=512M", "innodb-buffer-pool-size=4G"),
            ("mysql/atstudio.cnf", "max-connections=30", "max-connections=100"),
        ):
            with self.subTest(before=before):
                self.assert_reapproved_mutation_fails(relative, before, after, "RESOURCE_BASELINE")

    def test_maps_zones_and_child_inheritance_survive_hash_reapproval(self) -> None:
        cases = (
            ("nginx/nginx.conf.template", "map $uri $ats_download_scope", "map $request_uri $ats_download_scope", "NGINX_MAPS"),
            ("nginx/nginx.conf.template", "~*^multipart/ multipart;", "~^multipart/form-data multipart;", "NGINX_MAPS"),
            ("nginx/nginx.conf.template", "original $binary_remote_addr;", "original $http_x_forwarded_for;", "NGINX_MAPS"),
            ("nginx/nginx.conf.template", "original 2m;", "original 20m;", "NGINX_MAPS"),
            ("nginx/nginx.conf.template", "default 256k;", "default 512k;", "NGINX_MAPS"),
            ("nginx/nginx.conf.template", "zone=ats_download_server:1m", "zone=ats_unused:1m", "NGINX_ZONES"),
            ("nginx/https.conf.template", "location /api/ {", "location /api/ {\n        limit_conn ats_client 8;", "NGINX_EFFECTIVE_LIMITS"),
            ("nginx/https.conf.template", "location /uploads/ {", "location /uploads/ {\n        limit_conn ats_server 32;", "NGINX_EFFECTIVE_LIMITS"),
            ("nginx/https.conf.template", "limit_conn ats_multipart_server 2;", "limit_conn ats_multipart_server 20;", "NGINX_EFFECTIVE_LIMITS"),
            ("nginx/proxy.conf.template", "limit_req zone=ats_api burst=40 nodelay;", "", "NGINX_EFFECTIVE_RATES"),
            ("nginx/proxy.conf.template", "proxy_request_buffering off;", "proxy_request_buffering on;", "NGINX_PROXY_SAFETY"),
            ("nginx/proxy.conf.template", "X-ATStudio-Client-IP $remote_addr;", "X-ATStudio-Client-IP $http_x_forwarded_for;", "NGINX_PROXY_SAFETY"),
            ("nginx/restricted-access.conf", "deny all;", "allow all;", "NGINX_INVITATION_DEFAULT"),
            ("nginx/https.conf.template", "tracks/audio|", "", "NGINX_PROTECTED_ROUTES"),
            ("nginx/https.conf.template", "location /uploads/", "location ^~ /uploads/", "NGINX_PROTECTED_ROUTES"),
            ("nginx/nginx.conf.template", "limit_conn_status 429;", "limit_conn_status 429;\n    limit_conn_dry_run on;", "NGINX_ENFORCEMENT"),
            ("nginx/https.conf.template", "limit_rate $ats_media_rate;", "limit_rate $ats_media_rate;\n        limit_rate_after 100m;", "NGINX_ENFORCEMENT"),
            ("nginx/https.conf.template", "limit_rate $ats_media_rate;", "limit_rate $ats_media_rate;\n        limit_conn_status 503;", "NGINX_ENFORCEMENT"),
        )
        for relative, before, after, rule in cases:
            with self.subTest(before=before):
                self.assert_reapproved_mutation_fails(relative, before, after, rule)

    def test_new_source_bindings_must_be_at_effective_property_paths(self) -> None:
        source_path = self.root / audit.SOURCE
        original = source_path.read_text(encoding="utf-8")
        for property_path, placeholder in audit.SOURCE_BINDINGS.items():
            with self.subTest(property=property_path):
                leaf = property_path.rsplit(".", 1)[-1]
                source_path.write_text(original, encoding="utf-8")
                self.edit(audit.SOURCE, leaf + ": " + placeholder,
                          "wrong-" + leaf + ": " + placeholder)
                self.assert_failed("SOURCE_BINDING." + property_path)
        source_path.write_text(original, encoding="utf-8")

    def test_contract_cannot_raise_admission_or_media_limits(self) -> None:
        original = self.contract()
        for category in ("general", "media", "download", "multipart"):
            contract = json.loads(json.dumps(original))
            contract["nginx"]["connection_limits"][category]["global"] = 999
            self.save_contract(contract)
            self.assert_failed("CONTRACT_NGINX")

    def test_normalized_media_and_download_classification_fixtures(self) -> None:
        cases = {
            "/api/tracks/1/stream": (True, False),
            "/api/tracks/+1/download": (True, True),
            "/api/tracks/0x1/download;v=1/": (True, True),
            "/api;v=1/tracks;x=y/1;z=2/stream;v=1": (True, False),
            "/%61pi/tracks/%2B1/%64ownload?stream=1": (True, True),
            "/api/tracks/1/%73tream?download=1": (True, False),
            "/api%3Bv=1/tracks%3Bx=y/1/download%3Bv=1": (True, True),
            "/api/tracks/1/download/extra": (False, False),
            "/api/tracks/1/streaming": (False, False),
            "/api/tracks/1?download=1": (False, False),
            "/uploads/tracks/audio/1": (False, False),
        }
        main = (self.root / (audit.BASE + "nginx/nginx.conf.template")).read_text()
        http = next(node.children for node in audit.parse_nginx(main) if node.words == ("http",))
        mapping = next(node for node in http if node.words == ("map", "$uri", "$ats_download_scope"))
        self.assertEqual("~" + audit.DOWNLOAD_PATTERN, mapping.children[1].words[0])
        for uri, expected in cases.items():
            with self.subTest(uri=uri):
                normalized = unquote(urlsplit(uri).path)
                self.assertEqual(expected, (bool(re.search(audit.MEDIA_PATTERN, normalized)),
                                            bool(re.search(audit.DOWNLOAD_PATTERN, normalized))))

    def test_multipart_map_is_path_method_and_auth_independent(self) -> None:
        main = (self.root / (audit.BASE + "nginx/nginx.conf.template")).read_text()
        http = next(node.children for node in audit.parse_nginx(main) if node.words == ("http",))
        mapping = next(node for node in http if node.words == ("map", "$http_content_type", "$ats_multipart_scope"))
        self.assertEqual([("default", ""), ("~*^multipart/", "multipart")],
                         [entry.words for entry in mapping.children])
        for content_type, expected in (
            ("multipart/form-data; boundary=fake", True),
            ("MULTIPART/FORM-DATA; boundary=fake", True),
            ("multipart/form-data", True), ("multipart/mixed", True),
            ("application/json", False), ("", False), ("x-multipart/form-data", False),
        ):
            for method in ("POST", "PUT", "PATCH", "DELETE", "GET"):
                for authenticated in (False, True):
                    with self.subTest(type=content_type, method=method, auth=authenticated):
                        self.assertEqual(expected, bool(re.match(r"^multipart/", content_type, re.I)))

    def test_source_binding_comments_duplicates_and_wrong_scope_fail(self) -> None:
        text = (self.root / audit.SOURCE).read_text(encoding="utf-8")
        for path, placeholder in audit.SOURCE_BINDINGS.items():
            leaf = path.rsplit(".", 1)[-1]
            line = next(line for line in text.splitlines() if leaf + ": " + placeholder in line)
            for replacement in ("# " + line, line + "\n" + line, line.replace(placeholder, "1")):
                with self.subTest(property=path, replacement=replacement[:16]):
                    self.assertIn("SOURCE_BINDING." + path,
                                  audit.source_binding_regressions(text.replace(line, replacement)))
        self.assertIn("SOURCE_BINDING.spring.mail.properties.mail.smtp.timeout",
                      audit.source_binding_regressions(text.replace("  mail:\n", "  not-mail:\n")))

    def test_nginx_structure_errors_are_bounded_and_redacted(self) -> None:
        for invalid in ("http {", "}", 'map "' + SENTINEL, "http {" * 22, "http { ; }"):
            with self.subTest(value=invalid[:12]):
                self.assertEqual(["NGINX_STRUCTURE"],
                                 audit.nginx_structure_regressions(invalid, "", "", ""))

    def test_structural_parser_accepts_comments_and_indentation(self) -> None:
        names = ("nginx.conf.template", "https.conf.template", "proxy.conf.template", "restricted-access.conf")
        texts = [(self.root / (audit.BASE + "nginx/" + name)).read_text() for name in names]
        changed = ["\n".join("  " + line if line else "" for line in text.splitlines()) +
                   "\n# independent semantic positive fixture\n" for text in texts]
        self.assertEqual([], audit.nginx_structure_regressions(*changed))

    def test_numeric_only_media_fails_even_if_identity_hash_is_reapproved(self) -> None:
        relative = "nginx/https.conf.template"
        self.edit(audit.BASE + relative, audit.MEDIA_PATTERN, audit.LEGACY_MEDIA_PATTERN)
        text = (self.root / (audit.BASE + relative)).read_text(encoding="utf-8")
        with mock.patch.dict(audit.SHAPES, {relative: audit.active_shape(text)}):
            result = self.assert_failed("NGINX_MEDIA_CASES")
        self.assertNotIn("TEMPLATE_IDENTITY_DRIFT", {e["rule"] for e in result["errors"]})

    def test_frame_protection_required_on_spa_even_if_hash_is_reapproved(self) -> None:
        relative = "nginx/https.conf.template"
        path = self.root / (audit.BASE + relative)
        original = path.read_text(encoding="utf-8")
        for replacement in ("", "# add_header X-Frame-Options DENY always;",
                            "add_header X-Frame-Options DENY;",
                            "add_header X-Frame-Options SAMEORIGIN always;"):
            text = original.replace("        add_header X-Frame-Options DENY always;\n",
                                    "        " + replacement + "\n" if replacement else "")
            path.write_text(text, encoding="utf-8")
            with mock.patch.dict(audit.SHAPES, {relative: audit.active_shape(text)}):
                self.assert_failed("NGINX_SPA_FRAME")

    def test_unsupported_media_patterns_fail_without_regex_execution(self) -> None:
        relative = audit.BASE + "nginx/https.conf.template"
        self.edit(relative, audit.MEDIA_PATTERN, "^/(stream|download)(a+)+$")
        self.assert_failed("NGINX_MEDIA_UNSUPPORTED")

    def test_template_byte_hygiene_for_untracked_files(self) -> None:
        path = self.root / audit.ENV
        original = path.read_bytes()
        for suffix in (b"\xff\n", b"\x00\n", b"\x1b\n", b"\x7f\n", b"\r", b"x", b"x \n",
                       "# nonascii \u2028\n".encode(), b"\xef\xbb\xbf\n"):
            with self.subTest(suffix=suffix):
                path.write_bytes(original + suffix)
                self.assert_failed()

    def test_oversize_file_is_bounded(self) -> None:
        (self.root / audit.CONTRACT).write_bytes(b" " * (audit.MAX_BYTES + 1))
        self.assert_failed("FILE_SIZE")

    def test_source_reference_presence_not_runtime_binding(self) -> None:
        self.edit(audit.SOURCE, "${APP_STORAGE_REQUIRE_EXPLICIT_ROOTS:false}", "false")
        self.assert_failed("SOURCE_REFERENCE.APP_STORAGE_REQUIRE_EXPLICIT_ROOTS")

    def test_symlink_and_windows_reparse_stat_rejected(self) -> None:
        real_lstat = Path.lstat
        target = self.root / audit.ENV
        for mode, attributes in ((0o120777, 0), (0o100644, 0x400)):
            def replaced(path: Path) -> object:
                if path == target:
                    return type("SyntheticStat", (), {"st_mode": mode, "st_file_attributes": attributes})()
                return real_lstat(path)

            with mock.patch.object(Path, "lstat", replaced):
                self.assert_failed("FILE_LINK")

    def test_invalid_root_and_unc_are_controlled(self) -> None:
        for root in (self.root / SENTINEL, self.root / audit.ENV, Path("//" + SENTINEL + "/share")):
            result = audit.validate(root)
            self.assertEqual("ROOT_LOCAL_DIRECTORY", result["errors"][0]["rule"])
            self.assertNotIn(SENTINEL, json.dumps(result))

    def test_cli_exit_status_and_redaction(self) -> None:
        for args, expected in ((["--root", str(self.root)], 0),
                               (["--root", str(self.root), "--json"], 0),
                               (["--root", str(self.root / SENTINEL)], 1)):
            out = io.StringIO()
            with contextlib.redirect_stdout(out):
                self.assertEqual(expected, audit.main(args))
            self.assertNotIn(SENTINEL, out.getvalue())
            self.assertIn("HOLD", out.getvalue())
        out = io.StringIO()
        with contextlib.redirect_stderr(out), self.assertRaises(SystemExit) as raised:
            audit.main(["--" + SENTINEL])
        self.assertEqual(2, raised.exception.code)
        self.assertNotIn(SENTINEL, out.getvalue())

    def test_default_root_is_script_repository_not_cwd(self) -> None:
        with mock.patch.object(audit, "validate", return_value={"errors": []}) as call:
            with contextlib.redirect_stdout(io.StringIO()):
                audit.main(["--json"])
            self.assertEqual(REPOSITORY, call.call_args.args[0])

    def test_read_only_fixed_allowlist_no_environment_network_or_process(self) -> None:
        before = {str(p.relative_to(self.root)): hashlib.sha256(p.read_bytes()).hexdigest()
                  for p in self.root.rglob("*") if p.is_file()}
        opened: list[str] = []
        real_open = io.open

        def read_only(file: object, mode: str = "r", *args: object, **kwargs: object) -> object:
            self.assertEqual("rb", mode)
            relative = Path(file).relative_to(self.root).as_posix()
            self.assertIn(relative, audit.TEMPLATE_PATHS | {audit.SOURCE})
            opened.append(relative)
            return real_open(file, mode, *args, **kwargs)

        with mock.patch("io.open", side_effect=read_only), \
                mock.patch.object(socket, "socket", side_effect=AssertionError("network")), \
                mock.patch.object(subprocess, "Popen", side_effect=AssertionError("process")), \
                mock.patch.object(os, "system", side_effect=AssertionError("shell")), \
                mock.patch.object(os, "getenv", side_effect=AssertionError("environment")), \
                mock.patch.object(os, "environ", new={}):
            result = audit.validate(self.root)
        self.assertEqual("PASS", result["template_status"])
        self.assertEqual(audit.TEMPLATE_PATHS | {audit.SOURCE}, set(opened))
        after = {str(p.relative_to(self.root)): hashlib.sha256(p.read_bytes()).hexdigest()
                 for p in self.root.rglob("*") if p.is_file()}
        self.assertEqual(before, after)

    def test_owned_source_syntax_and_document_hygiene(self) -> None:
        files = ["scripts/deployment/validate_bundle.py", "scripts/deployment/test_validate_bundle.py",
                 "scripts/deployment/README.md", "deliverables/user/WI-20261003-ATS-003-summary.md",
                 "deliverables/agent/WI-20261003-ATS-003-evidence-pack.md",
                 "deliverables/user/WI-20261010-ATS-012-summary.md",
                 "deliverables/agent/WI-20261010-ATS-012-evidence-pack.md"]
        for relative in files:
            path = REPOSITORY / relative
            text = path.read_bytes().decode("utf-8")
            self.assertTrue(text.isascii(), relative)
            self.assertTrue(text.endswith("\n"), relative)
            self.assertNotIn("\r", text.replace("\r\n", ""), relative)
            self.assertTrue(all(line == line.rstrip() for line in text.splitlines()), relative)
            if relative.endswith(".py"):
                compile(text, relative, "exec")
                continue
            self.assertTrue(text.startswith("---\n") or text.startswith("---\r\n"), relative)
            metadata = text.split("---", 2)[1]
            for key in ("version", "last_updated", "project", "owner", "category", "status", "dependencies"):
                self.assertRegex(metadata, r"(?m)^" + key + r":")
            for link in re.findall(r"\[[^\]]+\]\(([^)]+)\)", text):
                if "://" not in link and not link.startswith("#"):
                    self.assertTrue((path.parent / link.split("#", 1)[0]).is_file(), (relative, link))


if __name__ == "__main__":
    unittest.main()
