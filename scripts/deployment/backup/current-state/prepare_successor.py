"""Deterministically prepare a LOCAL successor; never invokes its native entry.

Retained source contains private target bindings. Output belongs under ignored
build/WI-20261026-ATS-008 only, never in a public source or deliverable directory.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import stat

HERE = Path(__file__).resolve().parent
REPO = HERE.parents[3]
BACKUP_SOURCE = "build/WI20261024-011-backup-preparation"
STAGE_SOURCE = "build/WI20261024-017-stage-one"
BACKUP_PIN = "158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839"
STAGE_PIN = "18d4b2208da305acceaafe0f8b2eac438f8ae11f1f96f372b14b10a9a88e2221"
SCHEMA_SOURCE = "build/WI20261023-005-account-fixture/reused/schema.json"
SCHEMA_PIN = "648a4b93de43986e1340e697bb7e653eabece2ae1ab2a3583d3a7e6ec69fd520"


def need(ok, code):
    if not ok:
        raise ValueError(code)


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(",", ":"), allow_nan=False).encode()


def regular(path):
    for parent in (path, *path.parents):
        st = parent.lstat()
        need(not stat.S_ISLNK(st.st_mode)
             and not getattr(st, "st_file_attributes", 0) & 0x400, "SOURCE_LINK")
    need(path.is_file() and path.stat().st_size <= 2 * 1024**2, "SOURCE_FILE")
    return path.read_bytes()


def source_bundle(root, pin):
    raw = regular(root / "candidate.json")
    need(sha(raw) == pin, "SOURCE_CANDIDATE_PIN")
    manifest = json.loads(raw)
    files = {p.relative_to(root).as_posix(): regular(p) for p in root.rglob("*")
             if p.is_file() and p != root / "candidate.json"}
    need({p: sha(body) for p, body in files.items()} == manifest["files"], "SOURCE_TREE")
    return files


def replace_once(body, before, after):
    text = body.decode("utf-8-sig").replace("\r\n", "\n")
    need(text.count(before) == 1, "PATCH_CONTEXT")
    return text.replace(before, after).encode()


def edit(files, name, before, after):
    files[name] = replace_once(files[name], before, after)


def manifest(files, scope):
    bundle = b"".join(name.encode() + b"\0" + body for name, body in sorted(files.items()))
    return canonical(dict(wi="WI-20261026-ATS-008", scope=scope,
                          files={name: sha(body) for name, body in sorted(files.items())},
                          bundle_sha256=sha(bundle)))


def derive(backup, stage, schema_raw, guard_raw):
    """In-memory exact-context edits; preserved helper bytes stay unchanged."""
    backup = dict(backup)
    stage = dict(stage)
    backup["current_state.py"] = guard_raw
    backup["state-schema.json"] = schema_raw
    edit(backup, "lib/inventory.py", "expected_roles={1:'USER',2:'ADMIN',3:'USER'}",
         "expected_roles={1:'ADMIN',2:'ADMIN',3:'USER'}")
    edit(backup, "backup_plan.py", "from datetime import date", "from datetime import date\nimport current_state")
    edit(backup, "backup_plan.py", "WI='WI-20261024-ATS-011'", "WI='WI-20261026-ATS-008'")
    edit(backup, "backup_plan.py", "SCOPE='EXISTING_REPOSITORY_CURRENT_CAPTURE_ISOLATED_VERIFY'",
         "SCOPE='CURRENT_APPROVED_STATE_CAPTURE_ISOLATED_VERIFY'")
    edit(backup, "backup_plan.py", "'units_sha256','expected_fixture_counts','expected_grant'}",
         "'units_sha256','expected_fixture_counts','expected_grant',\n"
         "        'approved_state_anchor','approved_state_anchor_sha256'}")
    edit(backup, "backup_plan.py", "expected['admin_operation_audit_logs'] in (1,2)",
         "3<=expected['admin_operation_audit_logs']<=256")
    edit(backup, "backup_plan.py", "    need(grant['started_at']<=grant['expires_at'],'GRANT_DATES')",
         "    current_state.validate_grant(grant)\n    current_state.validate_anchor(approval,mode,now)")
    edit(backup, "backup_plan.py", "expected_fixture_counts=expected,expected_grant=grant,no_repository_initialization=True",
         "expected_fixture_counts=expected,expected_grant=grant,\n"
         "        approved_state_anchor=approval['approved_state_anchor'],\n"
         "        approved_state_anchor_sha256=approval['approved_state_anchor_sha256'],no_repository_initialization=True")
    edit(backup, "backup_plan.py", "grant_date_change='PROPOSAL_ONLY'", "grant_date_change='NOT_RUN'")
    edit(backup, "adapter.py", "import backup_plan as contract", "import backup_plan as contract\nimport current_state")
    edit(backup, "adapter.py", "        source=self.sql_capture(payload)",
         "        state_proof=current_state.observe(self)\n"
         "        source=self.sql_capture(payload)\n"
         "        contract.need(source['counts']==state_proof['table_counts'],'CURRENT_ALL_TABLE_COUNTS')")
    # Before staging, the bootstrap executes the contract in memory. Load the
    # pinned pure helper there too; all bytes have already passed member pins.
    edit(backup, "make_payload.py", "    mod=types.ModuleType('backup_plan');exec(",
         "    state=types.ModuleType('current_state')\n"
         "    exec(compile(decoded['current_state.py'],'<pinned-current-state>','exec'),state.__dict__)\n"
         "    state.SCHEMA=json.loads(decoded['state-schema.json'])\n"
         "    sys.modules['current_state']=state\n"
         "    mod=types.ModuleType('backup_plan');exec(")
    backup_manifest = manifest(backup, "CURRENT_APPROVED_STATE_CAPTURE_ISOLATED_VERIFY")
    edit(stage, "stage_one.py", "BACKUP=HERE.parent/'WI20261024-011-backup-preparation'",
         "BACKUP=HERE.parent/'backup'")
    edit(stage, "stage_one.py", "BACKUP_PIN='" + BACKUP_PIN + "'", "BACKUP_PIN='" + sha(backup_manifest) + "'")
    # Both local and remote scope records must use the new work identity.
    text = stage["stage_one.py"].decode().replace("\r\n", "\n")
    need(text.count("WI-20261024-ATS-017") == 2 and
         text.count("STAGE_ONE_CURRENT_BACKUP_COST_METADATA") == 2, "STAGE_SCOPE_CONTEXT")
    stage["stage_one.py"] = text.replace("WI-20261024-ATS-017", "WI-20261026-ATS-008").replace(
        "STAGE_ONE_CURRENT_BACKUP_COST_METADATA", "CURRENT_STATE_CAPTURE_COST_METADATA").encode()
    for name, body in {**{"backup/" + p: b for p, b in backup.items()},
                       **{"stage/" + p: b for p, b in stage.items()}}.items():
        if name.endswith(".py"):
            compile(body, name, "exec")
    return backup, backup_manifest, stage, manifest(stage, "CURRENT_STATE_CAPTURE_COST_METADATA")


def prepare(destination, repo=REPO):
    repo = Path(repo).resolve()
    destination = Path(destination).absolute()
    allowed = repo / "build" / "WI-20261026-ATS-008"
    need(".." not in destination.parts and destination.is_relative_to(allowed)
         and destination != allowed and not os.path.lexists(destination), "NEW_PRIVATE_DESTINATION")
    need(destination.parent.is_dir(), "DESTINATION_PARENT")
    for parent in destination.parents:
        st = parent.lstat()
        need(not stat.S_ISLNK(st.st_mode)
             and not getattr(st, "st_file_attributes", 0) & 0x400, "DESTINATION_LINK")
    backup = source_bundle(repo / BACKUP_SOURCE, BACKUP_PIN)
    stage = source_bundle(repo / STAGE_SOURCE, STAGE_PIN)
    schema_raw = regular(repo / SCHEMA_SOURCE)
    need(sha(schema_raw) == SCHEMA_PIN, "SCHEMA_SOURCE_PIN")
    guard_raw = regular(HERE / "current_state.py")
    b, bm, s, sm = derive(backup, stage, schema_raw, guard_raw)
    # No private input/authority is copied or generated; only reviewed tool sources.
    destination.mkdir(mode=0o700)
    for folder, files, m in (("backup", b, bm), ("stage", s, sm)):
        for name, raw in {**files, "candidate.json": m}.items():
            path = destination / folder / name
            path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
            with path.open("xb") as stream:
                stream.write(raw)
    need(source_bundle(repo / BACKUP_SOURCE, BACKUP_PIN) == backup
         and source_bundle(repo / STAGE_SOURCE, STAGE_PIN) == stage, "ORIGINAL_CHANGED")
    need(source_bundle(destination / "backup", sha(bm)) == b
         and source_bundle(destination / "stage", sha(sm)) == s, "DERIVED_TREE")
    return dict(status="LOCAL_SUCCESSOR_PREPARED", original_files_verified=len(backup) + len(stage),
                derived_backup_files=len(b), derived_stage_files=len(s),
                backup_candidate_sha256=sha(bm), stage_candidate_sha256=sha(sm),
                native_authority="NOT_CREATED", network="NOT_RUN")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    if args.output is None:
        print('{"status":"LOCAL_PREPARATION_ONLY","native_authority":"NOT_CREATED"}')
        return 0
    try:
        result = prepare(args.output)
        print(json.dumps(result, sort_keys=True))
        return 0
    except (OSError, ValueError, KeyError, TypeError):
        print('{"status":"HOLD","code":"SUCCESSOR_PREPARATION_FAILED","automatic_retry":false}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
