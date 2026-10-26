"""WI011 local synthetic tests. No real env, database, sockets or remote commands."""

from contextlib import redirect_stdout
from datetime import datetime, timezone
import importlib.util
import io
import json
import os
from pathlib import Path
import shutil
import sys
import unittest
from unittest.mock import patch
import uuid


SPEC = importlib.util.spec_from_file_location("release_readiness", Path(__file__).with_name("release-readiness.py"))
probe = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(probe)
ROOT = Path(__file__).absolute().parents[2]
NOW = datetime(2026, 10, 11, 0, 0, tzinfo=timezone.utc)
SENTINEL = "SYNTHETIC-NOT-SECRET-" + "x" * 11


def approval() -> dict:
    return {"req": probe.REQ, "review_wi": "WI-20261011-ATS-012", "bundle_sha256": "a" * 64,
            "machine_id_sha256": "b" * 64, "server_uuid_sha256": "c" * 64,
            "issued_at": "2026-10-10T23:59:00Z", "expires_at": "2026-10-11T00:10:00Z",
            "operator_checks": dict.fromkeys(probe.CHECKS, True)}


def environment() -> bytes:
    values = dict(probe.jdbc.DB_VALUES, SPRING_DATASOURCE_PASSWORD=SENTINEL)
    return "".join(f'{key}="{value}"\n' for key, value in values.items()).encode()


def bootstrap_environment() -> bytes:
    """Render every public bootstrap key with deterministic synthetic secrets."""
    path = ROOT / "scripts/deployment/production-bootstrap.py"
    probe.require(probe.jdbc.sha_file(path) ==
                  "7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b", "SOURCE_IDENTITY")
    probe.require(probe.jdbc.sha_file(path.with_name("validate_bundle.py")) ==
                  "19361e515e694718b509e28c6223a3ffc0bf647ddf6bb972f8aa4904cc1cb593", "SOURCE_IDENTITY")
    spec = importlib.util.spec_from_file_location("wi011_bootstrap_fixture", path)
    bootstrap = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = bootstrap
    spec.loader.exec_module(bootstrap)
    validator = bootstrap.validator
    env = validator.parse_env((ROOT / validator.ENV).read_text(encoding="utf-8"))
    contract = json.loads((ROOT / validator.CONTRACT).read_bytes())
    probe.require(set(env) == set(contract["required_env"]) == set(validator.EXPECTED_ENV)
                  and env == validator.EXPECTED_ENV, "ENV_KEYS")
    bundle = bootstrap.Bundle(b"", b"", env, [], {}, "SYNTHETIC")
    with patch.object(bootstrap, "run_private", side_effect=AssertionError("NO_NATIVE")), \
            patch.object(bootstrap.secrets, "token_bytes", side_effect=AssertionError("NO_SECRETS")):
        raw = bootstrap.render_env(bundle, SENTINEL, b"J" * 32, b"B" * 32)
    rendered_keys = {line.split(b"=", 1)[0].decode("ascii") for line in raw.splitlines()
                     if line and not line.startswith(b"#")}
    probe.require(rendered_keys == set(env), "ENV_KEYS")
    return raw


class ReleaseTests(unittest.TestCase):
    def test_approval_accepts_only_current_req_and_review(self) -> None:
        self.assertEqual(probe.parse_approval(json.dumps(approval()).encode(), "a" * 64, "b" * 64, NOW), approval())
        for key, value in (("req", probe.jdbc.REQ), ("review_wi", "WI-20261010-ATS-031"),
                           ("review_wi", "WI-20261011-ATS-007"),
                           ("review_wi", "WI-20261011-ATS-010"),
                           ("bundle_sha256", "d" * 64), ("machine_id_sha256", "d" * 64),
                           ("server_uuid_sha256", "wrong")):
            sample = dict(approval(), **{key: value})
            with self.subTest(key=key), self.assertRaises(probe.jdbc.GateError):
                probe.parse_approval(json.dumps(sample).encode(), "a" * 64, "b" * 64, NOW)
        sample = approval()
        sample["operator_checks"]["pg_wi010_exact_bundle_pass"] = sample["operator_checks"].pop(
            "pg_wi012_exact_bundle_pass")
        with self.assertRaisesRegex(probe.jdbc.GateError, "^APPROVAL_CHECKS$"):
            probe.parse_approval(json.dumps(sample).encode(), "a" * 64, "b" * 64, NOW)

    def test_approval_time_extra_duplicate_and_boolean_rejected(self) -> None:
        for replacement in ({"expires_at": "2026-10-11T00:00:00Z"},
                            {"expires_at": "2026-10-11T00:31:00Z"},
                            {"issued_at": "2026-10-11T00:01:00Z"}, {"extra": True}):
            with self.assertRaises(probe.jdbc.GateError):
                probe.parse_approval(json.dumps(dict(approval(), **replacement)).encode(), "a" * 64, "b" * 64, NOW)
        for check in probe.CHECKS:
            for value in (False, 1, "true"):
                sample = approval()
                sample["operator_checks"][check] = value
                with self.assertRaises(probe.jdbc.GateError):
                    probe.parse_approval(json.dumps(sample).encode(), "a" * 64, "b" * 64, NOW)
        raw = json.dumps(approval()).replace('"req":', '"req":"wrong","req":').encode()
        with self.assertRaises(probe.jdbc.GateError):
            probe.parse_approval(raw, "a" * 64, "b" * 64, NOW)

    def test_public_release_hashes_and_enumeration(self) -> None:
        result = probe.check_release(ROOT)
        self.assertEqual([result[name]["count"] for name in ("backend", "frontend", "spa")], [638, 336, 143])
        self.assertEqual(result["jar_sha256"], probe.jdbc.JAR_SHA)

    def test_manifest_tamper_rejected_before_parse(self) -> None:
        with patch.object(probe.jdbc, "sha_file", return_value="f" * 64), self.assertRaises(probe.jdbc.GateError):
            probe.manifest(ROOT)

    def test_enumeration_mismatch_never_hashes_unexpected_file(self) -> None:
        with patch.object(probe.jdbc, "sha_file") as hasher, self.assertRaises(probe.jdbc.GateError):
            probe.compare(ROOT, [{"path": "known"}], {"known", "unexpected-private"})
        hasher.assert_not_called()

    def test_hash_drift_fails(self) -> None:
        with patch.object(probe.jdbc, "sha_file", return_value="b" * 64), self.assertRaises(probe.jdbc.GateError):
            probe.compare(ROOT, [{"path": "known", "sha256": "a" * 64, "bytes": 1}], {"known"})

    def test_no_broad_coverage_exclusion(self) -> None:
        original = probe.tree
        def additional(root: Path, relative: str) -> set[str]:
            names = original(root, relative)
            if relative == "frontend/src":
                names.add("frontend/src/test/coverage/Unexpected.tsx")
            return names
        with patch.object(probe, "tree", side_effect=additional), self.assertRaises(probe.jdbc.GateError):
            probe.check_release(ROOT)

    def test_path_confinement(self) -> None:
        self.assertEqual(probe.output_path(ROOT, "build/WI011-schema-v5"), ROOT / "build/WI011-schema-v5")
        for name in ("build/WI029-old", "build/WI006-schema-v3", "build/WI009-schema-v4",
                     "build/WI011-../outside", "../WI011-a", "/tmp/WI011-a", "build/WI011-a/b"):
            with self.assertRaises(probe.jdbc.GateError):
                probe.output_path(ROOT, name)

    def test_exact_extraction_excludes_lifecycle_and_resources(self) -> None:
        selected = probe.selected_entries(probe.manifest(ROOT))
        self.assertEqual(len([name for name in selected if name.startswith("lib/")]), 30)
        self.assertFalse(any(token in name for name in selected for token in (
            "AtStudioApplication", "/config/", "/controller/", "/repository/", "/bootstrap/",
            "application.yml", "schema.sql", "Hikari", "logback", "tomcat", "mysqltest")))
        self.assertEqual(len([name for name in selected if "/service/" in name]), 4)

    def test_schema_env_reuses_jdbc_target_and_rejects_overrides(self) -> None:
        self.assertEqual(probe.schema_password(environment()), SENTINEL)
        safe = environment() + b'SPRING_JPA_HIBERNATE_DDL_AUTO="validate"\nSPRING_JPA_SHOW_SQL="false"\n'
        self.assertEqual(probe.schema_password(safe), SENTINEL)
        for line in (b'SPRING_JPA_HIBERNATE_DDL_AUTO="update"', b'SPRING_JPA_SHOW_SQL="true"',
                     b'SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA="wrong"',
                     b'SPRING_CONFIG_IMPORT="private"', b'HIBERNATE_DIALECT="wrong"'):
            with self.assertRaises(probe.jdbc.GateError):
                probe.schema_password(environment() + line + b"\n")
        with self.assertRaises(probe.jdbc.GateError):
            probe.schema_password(environment().replace(b"127.0.0.1", b"localhost"))

    def test_full_bootstrap_environment_accepts_packaged_only_location(self) -> None:
        raw = bootstrap_environment()
        self.assertIn(b'SPRING_CONFIG_LOCATION="classpath:/"\n', raw)
        with patch.object(probe.jdbc, "run_child") as child, \
                patch.object(probe.jdbc, "read_private") as private:
            self.assertEqual(probe.schema_password(raw), SENTINEL)
        child.assert_not_called()
        private.assert_not_called()

    def test_legacy_v4_rejects_full_bootstrap_environment(self) -> None:
        path = ROOT / "build/WI009-upload-v4/scripts/deployment/release-readiness.py"
        self.assertEqual(probe.jdbc.sha_file(path),
                         "a1e0492623901ab71086920dddc1c7100265b5016d281abe4410e82dc363f96a")
        spec = importlib.util.spec_from_file_location("wi011_legacy_v4", path)
        legacy = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(legacy)
        with patch.object(legacy.jdbc, "run_child") as child, \
                patch.object(legacy.jdbc, "read_private") as private:
            with self.assertRaisesRegex(legacy.jdbc.GateError, "^ENV_KEYS$"):
                legacy.schema_password(bootstrap_environment())
        child.assert_not_called()
        private.assert_not_called()

    def test_full_bootstrap_environment_rejects_other_locations(self) -> None:
        raw = bootstrap_environment()
        for location in (b"", b"file:/", b"file:./", b"https://invalid.example/",
                         b"optional:classpath:/", b"classpath:/config/", b"classpath:*",
                         b"classpath:/,file:/", b"classpath:/;file:/", b"CLASSPATH:/",
                         b" classpath:/", b"classpath:/ "):
            with self.subTest(location=location), \
                    self.assertRaisesRegex(probe.jdbc.GateError, "^ENV_KEYS$"):
                probe.schema_password(raw.replace(b'"classpath:/"', b'"' + location + b'"'))

    def test_full_bootstrap_environment_rejects_override_keys(self) -> None:
        raw = bootstrap_environment()
        for line in (b'SPRING_CONFIG_ADDITIONAL_LOCATION="classpath:/"',
                     b'SPRING_CONFIG_IMPORT=""', b'SPRING_CONFIG_IMPORT="classpath:/"',
                     b'SPRING_CONFIG_NAME="application"', b'SPRING_CONFIG_UNKNOWN="false"',
                     b'SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA="wrong"',
                     b'SPRING_HIBERNATE_UNKNOWN="false"', b'HIBERNATE_DIALECT="wrong"',
                     b'JAKARTA_PERSISTENCE_SCHEMA_GENERATION_DATABASE_ACTION="create"',
                     b'SPRING_DATASOURCE_UNKNOWN="false"', b'JAVA_TOOL_OPTIONS="-Xmx1g"',
                     b'JDK_JAVA_OPTIONS="-Xmx1g"', b'CLASSPATH="classpath:/"'):
            with self.subTest(key=line.split(b"=", 1)[0]), \
                    self.assertRaisesRegex(probe.jdbc.GateError, "^ENV_KEYS$"):
                probe.schema_password(raw + line + b"\n")
        for old, new in ((b'"validate"', b'"update"'), (b'SPRING_JPA_SHOW_SQL="false"',
                                                        b'SPRING_JPA_SHOW_SQL="true"')):
            with self.assertRaisesRegex(probe.jdbc.GateError, "^ENV_KEYS$"):
                probe.schema_password(raw.replace(old, new))

    def test_full_bootstrap_environment_rejects_duplicates_and_bad_grammar(self) -> None:
        raw = bootstrap_environment()
        line = b'SPRING_CONFIG_LOCATION="classpath:/"\n'
        for duplicate in (line, b'SPRING_CONFIG_LOCATION="file:/"\n'):
            with self.assertRaisesRegex(probe.jdbc.GateError, "^ENV_KEYS$"):
                probe.schema_password(raw + duplicate)
        for bad in (raw[:-1], b"\xef\xbb\xbf" + raw, raw.replace(b"\n", b"\r\n"),
                    raw.replace(line, b"SPRING_CONFIG_LOCATION=classpath:/\n"),
                    raw.replace(line, b"SPRING_CONFIG_LOCATION='classpath:/'\n"),
                    raw.replace(line, b" " + line), raw.replace(line, line[:-2] + b"\n"),
                    raw + b'BAD="\x00"\n', raw + b'BAD="\\q"\n'):
            with self.assertRaisesRegex(probe.jdbc.GateError, "^ENV_GRAMMAR$"):
                probe.schema_password(bad)

    def test_safe_protocol_success_and_failure_matrix(self) -> None:
        self.assertEqual(probe.checked_status(0, b"MAPPING_READY\n", "smoke")["status"], "MAPPING_READY")
        self.assertEqual(probe.checked_status(0, b"PASS|43|500|80|40|" + b"a" * 64 + b"\n", "probe")["columns"], 500)
        self.assertEqual(probe.checked_status(2, b"SCHEMA_REJECTED\n", "probe")["status"], "SCHEMA_REJECTED")
        for code, raw, mode in ((0, b"SCHEMA_REJECTED\n", "probe"), (2, b"MAPPING_READY\n", "smoke"),
                                (0, b"PASS|43|4097|80|40|" + b"a" * 64 + b"\n", "probe"),
                                (2, SENTINEL.encode(), "probe"), (0, b"MAPPING_READY\nextra", "smoke")):
            with self.assertRaises(probe.jdbc.GateError):
                probe.checked_status(code, raw, mode)

    def test_jvm_flags_and_explicit_classpath(self) -> None:
        command = probe.java_command(Path("java"), ROOT / "build/WI011-test", "probe")
        self.assertNotIn("*", " ".join(command))
        self.assertNotIn(probe.jdbc.JAR, " ".join(command))
        self.assertIn("-XX:+DisableAttachMechanism", command)
        self.assertIn("-XX:-HeapDumpOnOutOfMemoryError", command)
        self.assertNotIn(SENTINEL, " ".join(command))

    def test_top_level_sanitizes_unknown_exceptions_and_bad_args(self) -> None:
        for args in (["check"], ["not-a-mode", SENTINEL]):
            capture = io.StringIO()
            with patch.object(probe, "check_release", side_effect=RuntimeError(SENTINEL)), redirect_stdout(capture):
                self.assertEqual(probe.main(args), 2)
            self.assertNotIn(SENTINEL, capture.getvalue())
            self.assertEqual(json.loads(capture.getvalue())["status"], "HOLD")

    def test_native_platform_gate_before_private_read(self) -> None:
        with patch.object(sys, "platform", "win32"), patch.object(probe.jdbc, "read_private") as reader:
            with self.assertRaises(probe.jdbc.GateError):
                probe.native_probe({}, ROOT, ROOT / "build/WI011-test")
            reader.assert_not_called()


class CompiledTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.output = ROOT / ("build/WI011-tests-" + uuid.uuid4().hex)
        probe.prepare(ROOT, cls.output)
        cls.plan = probe.load_plan(ROOT, cls.output)
        cls.java = Path(shutil.which("java")).resolve(strict=True)

    def test_actual_mapping_no_connection(self) -> None:
        code, raw = probe.jdbc.run_child(probe.java_command(self.java, self.output, "smoke"), timeout=90, limit=256)
        self.assertEqual(probe.checked_status(code, raw, "smoke")["status"], "MAPPING_READY")

    def test_invalid_native_frame_fails_without_connecting(self) -> None:
        for payload in (b"", b"INVALID", probe.jdbc.frame(SENTINEL, "c" * 64) + b"extra"):
            code, raw = probe.jdbc.run_child(probe.java_command(self.java, self.output, "probe"), payload, limit=256)
            self.assertEqual((code, raw), (2, b"INPUT_REJECTED\n"))

    def test_synthetic_provider_without_mysql_driver_on_classpath(self) -> None:
        harness = ROOT / ("build/WI011-harness-" + uuid.uuid4().hex)
        harness.mkdir()
        cp = probe.classpath(self.output)
        compiler = Path(shutil.which("javac")).resolve(strict=True)
        command = [str(compiler), "--release", "17", "-proc:none", "-cp", cp, "-d", str(harness),
                   str(ROOT / "scripts/deployment/RuntimeSchemaProbeHarness.java")]
        code, _ = probe.jdbc.run_child(command, timeout=60, limit=1024)
        self.assertEqual(code, 0)
        safe_cp = os.pathsep.join([str(harness), *[part for part in cp.split(os.pathsep)
                                                if "mysql-connector" not in part]])
        command = [str(self.java), "-Dorg.jboss.logging.provider=jdk", "-cp", safe_cp,
                   "RuntimeSchemaProbeHarness", str(self.output / "entities.txt")]
        code, raw = probe.jdbc.run_child(command, timeout=60, limit=1024)
        self.assertEqual((code, raw), (0, b"SYNTHETIC_PASS\n"))

    def run_actual_validator(self, candidate: Path) -> tuple[int, bytes]:
        harness = ROOT / ("build/WI011-validator-" + uuid.uuid4().hex)
        harness.mkdir()
        cp = probe.classpath(candidate)
        safe_cp = os.pathsep.join(part for part in cp.split(os.pathsep) if "mysql-connector" not in part)
        compiler = Path(shutil.which("javac")).resolve(strict=True)
        command = [str(compiler), "--release", "17", "-encoding", "UTF-8", "-proc:none", "-cp", safe_cp,
                   "-d", str(harness), str(ROOT / "scripts/deployment/RuntimeSchemaValidatorHarness.java")]
        code, _ = probe.jdbc.run_child(command, timeout=60, limit=1024)
        self.assertEqual(code, 0)
        command = [str(self.java), "-Xmx256m", "-XX:ActiveProcessorCount=1",
                   "-Dorg.jboss.logging.provider=jdk", "-cp", str(harness) + os.pathsep + safe_cp,
                   "RuntimeSchemaValidatorHarness", str(candidate / "entities.txt")]
        result = probe.jdbc.run_child(command, timeout=60, limit=256)
        with (harness / "result.log").open("xb") as stream:
            stream.write(f"exit={result[0]}\n".encode() + result[1])
        return result

    def test_actual_validator_match_and_missing_table_column(self) -> None:
        self.assertEqual(self.run_actual_validator(self.output),
                         (0, b"ACTUAL_VALIDATOR_SYNTHETIC_PASS\n"))

    def test_legacy_v3_actual_validator_reproduces_type_failure(self) -> None:
        candidate = ROOT / "build/WI006-schema-v3"
        self.assertEqual(probe.jdbc.sha_file(candidate / "artifact.json"),
                         "ffadd34b644481bed92a7688f740270047adede4da458f9805230e68fb3c7e21")
        record = json.loads((candidate / "artifact.json").read_bytes())
        for name, expected in record["files"].items():
            self.assertEqual(probe.jdbc.sha_file(ROOT / name), expected)
        self.assertEqual(self.run_actual_validator(candidate),
                         (2, b"ACTUAL_VALIDATOR_TYPE_REJECTED\n"))

    def test_plan_enumeration_rejects_extra_class(self) -> None:
        actual = probe.tree(ROOT, self.output.relative_to(ROOT).as_posix())
        with patch.object(probe, "tree", return_value=actual | {"unexpected.class"}), self.assertRaises(probe.jdbc.GateError):
            probe.load_plan(ROOT, self.output)

    def test_source_change_rejects_prepared_bundle(self) -> None:
        original = probe.jdbc.sha_file
        def changed(path: Path) -> str:
            return "0" * 64 if path.name == "RuntimeSchemaProbe.java" else original(path)
        with patch.object(probe.jdbc, "sha_file", side_effect=changed), self.assertRaises(probe.jdbc.GateError):
            probe.load_plan(ROOT, self.output)


if __name__ == "__main__":
    unittest.main()
