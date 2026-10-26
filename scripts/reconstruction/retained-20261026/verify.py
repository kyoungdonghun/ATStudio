"""Verify retained source bytes; optionally reconstruct into a NEW empty tree.

Never imports/executes retained tools. Private-binding derivatives stay reference-only.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import stat

HERE = Path(__file__).resolve().parent
MAX_FILE = 2 * 1024 * 1024
MAX_TOTAL = 8 * 1024 * 1024


def need(condition, code):
    if not condition:
        raise ValueError(code)


def safe_relative(value):
    need(isinstance(value, str) and value and "\\" not in value and ":" not in value,
         "RELATIVE_PATH")
    path = PurePosixPath(value)
    need(not path.is_absolute() and ".." not in path.parts and "." not in path.parts
         and str(path) == value, "RELATIVE_PATH")
    return path


def regular_chain(path):
    for parent in (path, *path.parents):
        st = parent.lstat()
        need(not stat.S_ISLNK(st.st_mode)
             and not getattr(st, "st_file_attributes", 0) & 0x400, "REPARSE_PATH")
    need(path.is_file(), "REGULAR_FILE")


def read_file(path):
    regular_chain(path)
    need(path.stat().st_size <= MAX_FILE, "SIZE_LIMIT")
    return path.read_bytes()


def digest(raw):
    return hashlib.sha256(raw).hexdigest()


def verify(archive=HERE, originals=None):
    archive = Path(archive)
    manifest = json.loads(read_file(archive / "manifest.json"))
    need(manifest.get("schema") == "ats-retained-source-v1", "SCHEMA")
    records = manifest.get("records")
    need(isinstance(records, list) and 0 < len(records) <= 256, "RECORDS")
    paths, sources, payloads = set(), set(), []
    total = 0
    for item in records:
        source = safe_relative(item["source"])
        stored = safe_relative(item["archive"])
        mode = item["mode"]
        need(source.parts[0] == "build" and str(source) not in sources, "SOURCE_PATH")
        expected = ("files/" + str(source) if mode == "exact"
                    else "references/" + str(source) + ".reference.txt")
        need(mode in ("exact", "reference-only") and str(stored) == expected
             and str(stored) not in paths, "ARCHIVE_PATH")
        raw = read_file(archive.joinpath(*stored.parts))
        need(len(raw) == item["bytes"] and digest(raw) == item["archive_sha256"], "ARCHIVE_HASH")
        if mode == "exact":
            need(item["source_sha256"] == item["archive_sha256"], "EXACT_IDENTITY")
        else:
            need(item.get("redacted_categories") and item["source_sha256"] != item["archive_sha256"],
                 "REFERENCE_IDENTITY")
        if originals is not None:
            original = Path(originals).joinpath(*source.parts)
            need(digest(read_file(original)) == item["source_sha256"], "ORIGINAL_DRIFT")
        total += len(raw)
        need(total <= MAX_TOTAL, "TOTAL_LIMIT")
        paths.add(str(stored))
        sources.add(str(source))
        payloads.append((item, raw))
    actual = set()
    for folder in (archive / "files", archive / "references"):
        if folder.exists():
            for path in folder.rglob("*"):
                st = path.lstat()
                need(not stat.S_ISLNK(st.st_mode)
                     and not getattr(st, "st_file_attributes", 0) & 0x400, "REPARSE_PATH")
                if path.is_file():
                    actual.add(path.relative_to(archive).as_posix())
    need(actual == paths, "UNEXPECTED_MEMBER")
    return manifest, payloads


def restore(destination, payloads, archive=HERE):
    destination = Path(destination)
    need(".." not in destination.parts, "DESTINATION_PATH")
    destination = destination.absolute()
    need(not os.path.lexists(destination), "DESTINATION_EXISTS")
    need(destination.parent.is_dir(), "PARENT_MISSING")
    for parent in destination.parents:
        st = parent.lstat()
        need(not stat.S_ISLNK(st.st_mode)
             and not getattr(st, "st_file_attributes", 0) & 0x400, "REPARSE_PATH")
    need(not destination.is_relative_to(Path(archive).resolve()), "DESTINATION_IN_ARCHIVE")
    destination.mkdir(mode=0o700)
    exact = references = 0
    for item, raw in payloads:
        if item["mode"] == "exact":
            relative = safe_relative(item["source"])
            exact += 1
        else:
            relative = safe_relative("reference-only/" + item["source"] + ".reference.txt")
            references += 1
        output = destination.joinpath(*relative.parts)
        output.parent.mkdir(parents=True, exist_ok=True)
        with output.open("xb") as stream:
            stream.write(raw)
        need(digest(read_file(output)) == item["archive_sha256"], "ROUNDTRIP_HASH")
    return {"exact_reconstructed": exact, "references_copied_separately": references}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--compare-originals", type=Path)
    parser.add_argument("--restore-to", type=Path)
    args = parser.parse_args(argv)
    try:
        manifest, payloads = verify(originals=args.compare_originals)
        result = {"status": "RETAINED_SOURCE_VERIFIED", "files": len(payloads),
                  "originals": "MATCH" if args.compare_originals else "NOT_CHECKED",
                  "runtime": "NOT_EXECUTED", "complete_runtime_reconstruction": False}
        if args.restore_to:
            result.update(restore(args.restore_to, payloads))
        print(json.dumps(result, sort_keys=True))
        return 0
    except (ValueError, KeyError, TypeError, OSError):
        print(json.dumps({"status": "HOLD", "code": "RETENTION_VALIDATION_FAILED",
                          "runtime": "NOT_EXECUTED"}))
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
