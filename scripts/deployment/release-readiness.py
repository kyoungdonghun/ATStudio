"""WI011: public environment compatibility and an MA-only schema probe."""

from __future__ import annotations

from datetime import datetime, timezone
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import sys
import zipfile


sys.dont_write_bytecode = True
HELPER = Path(__file__).with_name("jdbc-readiness.py")
HELPER_SHA = "13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051"
try:
    helper_matches = hashlib.sha256(HELPER.read_bytes()).hexdigest() == HELPER_SHA
except OSError:
    helper_matches = False
if not helper_matches:
    print('{"status":"HOLD","rule":"HELPER_IDENTITY"}')
    raise SystemExit(2)
SPEC = importlib.util.spec_from_file_location("wi006_jdbc", HELPER)
jdbc = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(jdbc)
require = jdbc.require
WI = "WI-20261011-ATS-011"
REQ = "REQ-20261011-ATS-001"
MANIFEST = "deliverables/agent/WI-20261010-ATS-026-artifact.json"
MANIFEST_SHA = "fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c"
SOURCES = ("scripts/deployment/release-readiness.py", "scripts/deployment/RuntimeSchemaProbe.java",
           "scripts/deployment/jdbc-readiness.py", "scripts/deployment/RuntimeJdbcProbe.java")
LIBRARIES = (
    "hibernate-core-7.2.24.Final.jar", "hibernate-models-1.0.1.jar",
    "jakarta.persistence-api-3.2.0.jar", "jakarta.transaction-api-2.0.1.jar",
    "jakarta.inject-api-2.0.1.jar", "jboss-logging-3.6.3.Final.jar", "classmate-1.7.3.jar",
    "byte-buddy-1.17.8.jar", "antlr4-runtime-4.13.2.jar",
    "jakarta.xml.bind-api-4.0.5.jar", "jaxb-runtime-4.0.9.jar", "jaxb-core-4.0.9.jar",
    "jakarta.activation-api-2.1.4.jar", "angus-activation-2.0.3.jar",
    "txw2-4.0.9.jar", "istack-commons-runtime-4.1.2.jar",
    "spring-boot-hibernate-4.0.8.jar", "spring-data-jpa-4.0.7.jar",
    "spring-data-commons-4.0.7.jar", "spring-core-7.0.9.jar", "spring-beans-7.0.9.jar",
    "spring-context-7.0.9.jar", "spring-tx-7.0.9.jar", "spring-expression-7.0.9.jar",
    "spring-aop-7.0.9.jar", "spring-web-7.0.9.jar", "commons-logging-1.3.6.jar",
    "spring-aspects-7.0.9.jar", "aspectjweaver-1.9.25.1.jar",
    "mysql-connector-j-9.7.0.jar",
)
MODEL_EXTRA = (
    "common/entity/BaseEntity.class", "common/exception/BusinessException.class",
    "common/exception/BUSINESS_ERROR.class", "service/storage/StorageDomain.class",
    "service/storage/StorageMutationState.class", "service/storage/StorageMutationType.class",
    "service/storage/StorageRoot.class",
)
COVERAGE_EXCLUSIONS = frozenset("frontend/src/test/coverage/" + name for name in (
    "adminSubscriberGaps.coverage.test.tsx", "adminSubscriberPages.coverage.test.tsx",
    "publicAuthShell.coverage.test.tsx", "shellCatalogRouterGaps.coverage.test.tsx",
))
CHECKS = (*jdbc.CHECKS, "pg_wi012_exact_bundle_pass", "restricted_tls_timer_unchanged",
          "quarantine_has_no_app_upstream", "no_mapping_overrides")
APPROVAL_PATH = jdbc.CONFIG / "release-readiness-approval.json"
CODES = jdbc.CODES | {"MANIFEST_IDENTITY", "INPUT_DRIFT", "INPUT_ENUMERATION",
                     "LIBRARY_IDENTITY", "MODEL_IDENTITY", "MAPPING_REJECTED", "HELPER_IDENTITY"}


def digest(value: object) -> str:
    return hashlib.sha256(json.dumps(value, sort_keys=True, separators=(",", ":")).encode()).hexdigest()


def manifest(root: Path) -> dict:
    require(jdbc.sha_file(root / MANIFEST) == MANIFEST_SHA, "MANIFEST_IDENTITY")
    return json.loads((root / MANIFEST).read_bytes(), object_pairs_hook=jdbc.unique_object)


def tree(root: Path, relative: str) -> set[str]:
    path = root / relative
    if not path.exists():
        return set()
    jdbc.no_links(path)
    names = set()
    for directory, dirs, files in os.walk(path, followlinks=False):
        for name in dirs + files:
            jdbc.no_links(Path(directory) / name)
        for name in files:
            names.add((Path(directory) / name).relative_to(root).as_posix())
        require(len(names) <= 4096, "INPUT_ENUMERATION")
    return names


def compare(root: Path, records: list[dict], actual: set[str]) -> dict:
    expected = {record["path"]: record for record in records}
    require(len(expected) == len(records) and set(expected) == actual, "INPUT_ENUMERATION")
    for name, record in expected.items():
        require(jdbc.sha_file(root / name) == record["sha256"].lower()
                and (root / name).stat().st_size == record["bytes"], "INPUT_DRIFT")
    return {"count": len(records), "records_sha256": digest(records)}


def check_release(root: Path) -> dict:
    value = manifest(root)
    backend = value["sourceIdentity"]["files"]
    frontend = value["frontend"]["files"]
    spa = value["frontendCompletion"]["artifact"]
    back_names = tree(root, "src") | tree(root, "gradle") | {
        "build.gradle", "settings.gradle", "gradlew", "gradlew.bat"}
    front_names = tree(root, "frontend/src") | tree(root, "frontend/public")
    excluded = front_names & COVERAGE_EXCLUSIONS
    front_names -= excluded
    # 비공개 env는 이름만 제외하며 내용을 읽거나 해시하지 않는다.
    for path in (root / "frontend").iterdir():
        if path.is_file() and not path.name.startswith(".env") and path.suffix in {
                ".json", ".ts", ".cjs", ".html"}:
            front_names.add(path.relative_to(root).as_posix())
    front_names.update({"frontend/.env.example", "frontend/.gitignore", "frontend/.prettierrc"})
    result = {"backend": compare(root, backend, back_names),
              "frontend": compare(root, frontend, front_names),
              "spa": compare(root, spa["files"], tree(root, spa["outputPath"]))}
    require(jdbc.sha_file(root / jdbc.JAR) == jdbc.JAR_SHA, "JAR_IDENTITY")
    result.update({"jar_sha256": jdbc.JAR_SHA, "wi026_manifest_sha256": MANIFEST_SHA,
                   "historical_head": value["sourceIdentity"]["head"],
                   "wi026_excluded_coverage_files": len(excluded)})
    return result


def selected_entries(value: dict) -> dict[str, dict]:
    artifact = value["backend"]["artifact"]
    entries = {record["entry"]: record for record in artifact["libraries"] + artifact["classes"]}
    prefix = "BOOT-INF/classes/com/atstudio/atstudio/"
    chosen = {"lib/" + name: entries["BOOT-INF/lib/" + name] for name in LIBRARIES}
    for entry, record in entries.items():
        if entry.startswith(prefix + "entity/") or entry in {prefix + name for name in MODEL_EXTRA}:
            chosen["model/" + entry.removeprefix("BOOT-INF/classes/")] = record
    return chosen


def output_path(root: Path, relative: str) -> Path:
    require(re.fullmatch(r"build/WI011-[A-Za-z0-9-]+", relative) is not None, "OUTPUT_PATH")
    jdbc.no_links(root / "build")
    return root / relative


def classpath(output: Path) -> str:
    return os.pathsep.join(str(path) for path in [output / "classes", output / "model",
                           *(output / "lib" / name for name in LIBRARIES)])


def java_command(java: Path, output: Path, mode: str) -> list[str]:
    return [str(java), "-Xms32m", "-Xmx256m", "-XX:ActiveProcessorCount=1",
            "-XX:-CreateCoredumpOnCrash", "-XX:-HeapDumpOnOutOfMemoryError",
            "-XX:ErrorFile=" + os.devnull, "-XX:-UsePerfData", "-XX:+DisableAttachMechanism",
            "-Xlog:disable", "-Dorg.jboss.logging.provider=jdk", "-cp", classpath(output),
            "RuntimeSchemaProbe", mode, str(output / "entities.txt")]


def prepare(root: Path, output: Path) -> None:
    release = check_release(root)
    sources = {name: jdbc.sha_file(root / name) for name in SOURCES}
    compiler_name = shutil.which("javac")
    require(compiler_name is not None, "JAVA17_REQUIRED")
    compiler = Path(compiler_name).resolve(strict=True)
    code, version = jdbc.run_child([str(compiler), "-version"])
    require(code == 0 and re.fullmatch(rb"javac 17\.\d+\.\d+\r?\n", version) is not None,
            "JAVA17_REQUIRED")
    selected = selected_entries(manifest(root))
    output.mkdir(mode=0o700, exist_ok=False)
    (output / "classes").mkdir(mode=0o700)
    with zipfile.ZipFile(root / jdbc.JAR) as archive:
        require(len(archive.namelist()) == len(set(archive.namelist())), "ARTIFACT_SHAPE")
        for name, record in selected.items():
            raw = archive.read(record["entry"])
            require(hashlib.sha256(raw).hexdigest() == record["sha256"].lower(), "ARTIFACT_IDENTITY")
            path = output / name
            path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
            with path.open("xb") as stream:
                stream.write(raw)
    entities = sorted(name.removeprefix("model/").removesuffix(".class").replace("/", ".")
                      for name in selected if re.fullmatch(
                          r"model/com/atstudio/atstudio/entity/[A-Z][A-Za-z0-9]*\.class", name))
    require(len(entities) == 43, "MODEL_IDENTITY")
    with (output / "entities.txt").open("x", encoding="ascii", newline="\n") as stream:
        stream.write("\n".join(entities) + "\n")
    code, _ = jdbc.run_child([str(compiler), "--release", "17", "-encoding", "UTF-8", "-proc:none",
                              "-Xlint:all", "-cp", classpath(output), "-d", str(output / "classes"),
                              *(str(root / name) for name in SOURCES if name.endswith(".java"))],
                             timeout=90, limit=4096)
    require(code == 0, "COMPILE_FAILED")
    require(release == check_release(root) and sources ==
            {name: jdbc.sha_file(root / name) for name in SOURCES}, "SOURCE_IDENTITY")
    files = {name: jdbc.sha_file(root / name) for name in sorted(tree(root, output.relative_to(root).as_posix()))}
    record = {"schema": 1, "sources": sources, "release": release, "files": files,
              "javac": version.decode("ascii").strip(), "compiler_sha256": jdbc.sha_file(compiler)}
    with (output / "artifact.json").open("x", encoding="ascii", newline="\n") as stream:
        json.dump(record, stream, sort_keys=True)
        stream.write("\n")


def load_plan(root: Path, output: Path) -> dict:
    selected = selected_entries(manifest(root))
    require(jdbc.sha_file(root / jdbc.JAR) == jdbc.JAR_SHA, "JAR_IDENTITY")
    jdbc.no_links(output / "artifact.json")
    require((output / "artifact.json").stat().st_size <= 65536, "ARTIFACT_SHAPE")
    record = json.loads((output / "artifact.json").read_bytes(), object_pairs_hook=jdbc.unique_object)
    require(set(record) == {"schema", "sources", "release", "files", "javac", "compiler_sha256"}
            and record["schema"] == 1, "ARTIFACT_SHAPE")
    sources = {name: jdbc.sha_file(root / name) for name in SOURCES}
    require(record["sources"] == sources, "SOURCE_IDENTITY")
    relative = output.relative_to(root).as_posix()
    actual = tree(root, relative) - {relative + "/artifact.json"}
    allowed = {relative + "/" + name for name in selected} | {relative + "/entities.txt"}
    classes = {relative + "/classes/" + name for name in (
        "RuntimeJdbcProbe.class", "RuntimeSchemaProbe.class", "RuntimeSchemaProbe$1.class",
        "RuntimeSchemaProbe$ReadOnlyProvider.class")}
    require(actual == allowed | classes and actual == set(record["files"]), "ARTIFACT_SHAPE")
    hashes = {name: jdbc.sha_file(root / name) for name in sorted(actual)}
    require(hashes == record["files"], "ARTIFACT_IDENTITY")
    for name, expected in selected.items():
        require(hashes[relative + "/" + name] == expected["sha256"].lower(), "ARTIFACT_IDENTITY")
    hashes.update(sources)
    hashes.update({jdbc.JAR: jdbc.JAR_SHA, MANIFEST: MANIFEST_SHA,
                   relative + "/artifact.json": jdbc.sha_file(output / "artifact.json")})
    return {"bundle_sha256": digest(hashes), "input_sha256": hashes}


def parse_approval(raw: bytes, identity: str, machine_hash: str, now: datetime) -> dict:
    value = json.loads(raw, object_pairs_hook=jdbc.unique_object)
    require(isinstance(value, dict) and value.get("req") == REQ
            and value.get("review_wi") == "WI-20261011-ATS-012", "APPROVAL_IDENTITY")
    require(value.get("operator_checks") == dict.fromkeys(CHECKS, True)
            and all(type(item) is bool for item in value["operator_checks"].values()), "APPROVAL_CHECKS")
    # 기존 검증기의 엄격한 형식/시간 검증을 재사용하되, 구 승인서는 먼저 거부한다.
    translated = dict(value, req=jdbc.REQ, review_wi="WI-20261010-ATS-031",
                      operator_checks=dict.fromkeys(jdbc.CHECKS, True))
    jdbc.parse_approval(json.dumps(translated).encode(), identity, machine_hash, now)
    return value


def checked_status(code: int, raw: bytes, mode: str) -> dict:
    if code == 0 and mode == "smoke" and raw == b"MAPPING_READY\n":
        return {"status": "MAPPING_READY", "hibernate": "MAPPING_ONLY_NO_CONNECTION"}
    if code == 0 and mode == "probe":
        match = re.fullmatch(rb"PASS\|43\|([0-9]{1,4})\|([0-9]{1,4})\|([0-9]{1,4})\|([0-9a-f]{64})\n", raw)
        require(match is not None and all(int(item) <= 4096 for item in match.groups()[:3]), "JAVA_PROTOCOL")
        return {"status": "PASS", "hibernate": "VALIDATE_PASS", "tables": 43,
                "columns": int(match[1]), "index_entries": int(match[2]),
                "foreign_key_columns": int(match[3]), "target_structure_sha256": match[4].decode("ascii")}
    failures = {name.encode() + b"\n": name for name in (
        "INPUT_REJECTED", "DRIVER_REJECTED", "MAPPING_REJECTED", "SESSION_REJECTED",
        "SNAPSHOT_REJECTED", "SCHEMA_REJECTED", "CLOSE_FAILED")}
    require(code == 2 and raw in failures, "JAVA_PROTOCOL")
    return {"status": failures[raw], "hibernate": "NOT_PROVEN"}


def schema_password(raw: bytes) -> str:
    password = jdbc.parse_env(raw)
    permitted = {b'SPRING_JPA_HIBERNATE_DDL_AUTO="validate"', b'SPRING_JPA_SHOW_SQL="false"',
                 b'SPRING_CONFIG_LOCATION="classpath:/"'}
    for line in raw.splitlines():
        if line.startswith((b"SPRING_JPA_", b"SPRING_HIBERNATE_", b"HIBERNATE_",
                            b"JAKARTA_", b"SPRING_CONFIG_")):
            require(line in permitted, "ENV_KEYS")
    return password


def native_probe(plan: dict, root: Path, output: Path) -> dict:
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    import ctypes
    import resource

    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
    require(ctypes.CDLL(None).prctl(4, 0, 0, 0, 0) == 0, "PROCESS_DUMP_GUARD")
    os.umask(0o077)
    require(Path(__file__).absolute() == root / SOURCES[0], "SOURCE_IDENTITY")
    for name in plan["input_sha256"]:
        jdbc.secure_path(root / name)
    jdbc.secure_path(jdbc.CONFIG, private=True, directory=True)
    java = jdbc.native_java()
    machine = Path("/etc/machine-id")
    jdbc.secure_path(machine)
    machine_hash = hashlib.sha256(machine.read_bytes().strip()).hexdigest()
    raw = jdbc.read_private(APPROVAL_PATH, 8192)
    approved = parse_approval(raw, plan["bundle_sha256"], machine_hash, datetime.now(timezone.utc))
    require(load_plan(root, output) == plan, "ARTIFACT_IDENTITY")
    status = checked_status(*jdbc.run_child(java_command(java, output, "smoke"), timeout=90, limit=256), "smoke")
    require(status["status"] == "MAPPING_READY", "MAPPING_REJECTED")
    parse_approval(raw, plan["bundle_sha256"], machine_hash, datetime.now(timezone.utc))
    password = schema_password(jdbc.read_private(jdbc.ENV_PATH, jdbc.ENV_LIMIT))
    payload = jdbc.frame(password, approved["server_uuid_sha256"])
    del password
    try:
        return checked_status(*jdbc.run_child(java_command(java, output, "probe"), payload,
                                              timeout=90, limit=256), "probe")
    finally:
        del payload


def main(argv: list[str] | None = None) -> int:
    result = {"wi": WI, "status": "HOLD", "production_status": "HOLD", "app_runnable": False,
              "hibernate": "NOT_RUN", "canonical_fresh_manifest": "UNRECORDED"}
    try:
        parser = jdbc.SafeParser(description=__doc__)
        parser.add_argument("mode", choices=("check", "prepare", "plan", "smoke", "probe"))
        parser.add_argument("--root", type=Path, default=Path(__file__).absolute().parents[2])
        parser.add_argument("--output")
        args = parser.parse_args(argv)
        root = args.root.absolute()
        if args.mode == "check":
            result.update({"status": "INPUTS_MATCH", "release": check_release(root)})
        else:
            require(args.output is not None, "OUTPUT_PATH")
            output = output_path(root, args.output)
            if args.mode == "prepare":
                prepare(root, output)
            plan = load_plan(root, output)
            result["bundle_sha256"] = plan["bundle_sha256"]
            if args.mode == "probe":
                result.update(native_probe(plan, root, output))
            elif args.mode == "smoke":
                java = shutil.which("java")
                require(java is not None, "JAVA17_REQUIRED")
                result.update(checked_status(*jdbc.run_child(java_command(Path(java).resolve(strict=True), output,
                                                                         "smoke"), timeout=90, limit=256), "smoke"))
            else:
                result["status"] = "PREPARED" if args.mode == "prepare" else "PLAN_ONLY"
        print(json.dumps(result, sort_keys=True))
        return 0 if result["status"] in {"INPUTS_MATCH", "PREPARED", "PLAN_ONLY", "MAPPING_READY", "PASS"} else 2
    except jdbc.GateError as error:
        result["rule"] = str(error) if str(error) in CODES else "UNEXPECTED_FAILURE"
    except KeyboardInterrupt:
        result["rule"] = "INTERRUPTED"
    except Exception:
        result["rule"] = "UNEXPECTED_FAILURE"
    print(json.dumps(result, sort_keys=True))
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
