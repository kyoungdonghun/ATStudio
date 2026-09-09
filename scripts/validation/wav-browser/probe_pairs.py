"""Read-only WI027 original/MP3 probe for an MA-provided sanitized JSON list."""

import argparse
import json
import math
from pathlib import Path
import subprocess
import sys

from generate_fixtures import BIN, ROOT, native_environment, sha256


PUBLIC_ROOT = Path(r"C:\Users\jm991\Desktop\project\ATStudio\uploads")
TOLERANCE_SECONDS = 0.25


def contained(root: Path, relative: str) -> Path:
    candidate = Path(relative)
    if candidate.is_absolute() or candidate.drive or ".." in candidate.parts:
        raise ValueError("PATH_OUTSIDE_SCOPE")
    path = root / candidate
    if not path.resolve().is_relative_to(root.resolve()):
        raise ValueError("PATH_OUTSIDE_SCOPE")
    return path


def run_native(arguments: list[str], timeout: int = 180) -> str:
    result = subprocess.run(arguments, capture_output=True, text=True,
                            env=native_environment(), timeout=timeout, check=False)
    if result.returncode:
        raise ValueError("MEDIA_COMMAND_FAILED")
    return result.stdout


def media_info(path: Path) -> dict:
    data = json.loads(run_native([
        str(BIN / "ffprobe.exe"), "-v", "error", "-protocol_whitelist", "file",
        "-select_streams", "a:0", "-show_entries",
        "stream=codec_name,sample_rate,channels,bit_rate,duration:format=duration",
        "-of", "json", str(path),
    ]))
    stream = data["streams"][0]
    duration = float(stream.get("duration", data.get("format", {}).get("duration", 0)))
    if not math.isfinite(duration) or duration <= 0:
        raise ValueError("INVALID_DURATION")
    return {"codec": stream["codec_name"], "sample_rate": int(stream["sample_rate"]),
            "channels": int(stream["channels"]), "bit_rate": int(stream.get("bit_rate", 0)),
            "duration_seconds": duration}


def decoded_duration(path: Path, demuxer: str) -> float:
    progress = run_native([
        str(BIN / "ffmpeg.exe"), "-nostdin", "-hide_banner", "-loglevel", "error",
        "-xerror", "-err_detect", "explode", "-protocol_whitelist", "file", "-f", demuxer,
        "-i", str(path), "-map", "0:a:0", "-af", "asetpts=N/SR/TB",
        "-progress", "pipe:1", "-nostats", "-f", "null", "-",
    ])
    times = [int(line.partition("=")[2]) for line in progress.splitlines()
             if line.startswith("out_time_us=") and line.partition("=")[2].isdigit()]
    if "progress=end" not in progress or not times or max(times) <= 0:
        raise ValueError("FULL_DECODE_UNCONFIRMED")
    return max(times) / 1_000_000


def check_pair(fixture: dict, source: Path, original: Path, derivative: Path) -> dict:
    result = {"ok": False, "checks": {}, "errors": []}
    checks = result["checks"]
    for label, path in (("fixture", source), ("original", original), ("stream", derivative)):
        result[f"{label}_bytes"] = path.stat().st_size
        result[f"{label}_sha256"] = sha256(path)
    checks["fixture_manifest_hash"] = result["fixture_sha256"] == fixture["sha256"]
    checks["original_preserved"] = result["original_sha256"] == fixture["sha256"]
    result["original_media"] = media_info(original)
    result["stream_media"] = media_info(derivative)
    original_media, stream_media = result["original_media"], result["stream_media"]
    expected_duration = fixture["duration_seconds"]
    checks["original_format"] = (original_media["codec"], original_media["channels"],
                                 original_media["sample_rate"]) == ("pcm_s16le", 2, 44100)
    checks["stream_format"] = (stream_media["codec"], stream_media["channels"],
                               stream_media["sample_rate"]) == ("mp3", 2, 44100)
    checks["stream_128000bps"] = stream_media["bit_rate"] == 128000
    for label, path, demuxer in (("original", original, "wav"), ("stream", derivative, "mp3")):
        result[f"{label}_decoded_seconds"] = decoded_duration(path, demuxer)
        checks[f"{label}_full_duration"] = (
            abs(result[f"{label}_media"]["duration_seconds"] - expected_duration)
            <= TOLERANCE_SECONDS
            and abs(result[f"{label}_decoded_seconds"] - expected_duration)
            <= TOLERANCE_SECONDS
        )
    result["errors"] = [name for name, passed in checks.items() if not passed]
    result["ok"] = not result["errors"]
    return result


def validate_records(records: object, fixtures: dict) -> list[dict]:
    if not isinstance(records, list) or not 1 <= len(records) <= 24:
        raise ValueError("EXPECTED_NONEMPTY_LIST_MAX24")
    seen_ids, seen_names = set(), set()
    required = {"track_id", "fixture_name", "original_relative_path", "stream_relative_path", "state"}
    for row in records:
        if not isinstance(row, dict) or set(row) != required:
            raise ValueError("RECORD_FIELDS_INVALID")
        track_id, name = row["track_id"], row["fixture_name"]
        if type(track_id) is not int or track_id <= 0 or not isinstance(name, str):
            raise ValueError("RECORD_ID_INVALID")
        if track_id in seen_ids or name in seen_names:
            raise ValueError("DUPLICATE_RECORD")
        seen_ids.add(track_id)
        seen_names.add(name)
        if name not in fixtures or fixtures[name]["kind"] != "valid":
            raise ValueError("UNKNOWN_OR_CORRUPT_FIXTURE")
        if row["state"] != "READY":
            raise ValueError("RECORD_NOT_READY")
        for key in ("original_relative_path", "stream_relative_path"):
            if not isinstance(row[key], str) or "\\" in row[key]:
                raise ValueError("STORAGE_KEY_INVALID")
            if not row[key].startswith("tracks/audio/"):
                raise ValueError("STORAGE_KEY_OUTSIDE_AUDIO")
            contained(PUBLIC_ROOT, row[key])
    return records


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--records", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    for path in (args.records, args.output):
        if not path.resolve().is_relative_to(ROOT.resolve()):
            raise ValueError("INPUT_OUTPUT_MUST_STAY_PRIVATE")
    if args.output.exists():
        raise ValueError("OUTPUT_ALREADY_EXISTS")
    manifest = json.loads((ROOT / "fixture-manifest.json").read_text(encoding="utf-8-sig"))
    fixtures = {item["name"]: item for item in manifest["files"]}
    records = validate_records(json.loads(args.records.read_text(encoding="utf-8-sig")), fixtures)
    results = []
    for row in records:
        fixture = fixtures[row["fixture_name"]]
        source = ROOT / ("batch20" if fixture["name"].startswith("WI027-sine-") else "edge") / fixture["name"]
        try:
            observed = check_pair(fixture, source, contained(PUBLIC_ROOT, row["original_relative_path"]),
                                  contained(PUBLIC_ROOT, row["stream_relative_path"]))
        except (OSError, ValueError, KeyError, IndexError, subprocess.SubprocessError) as error:
            observed = {"ok": False, "errors": [type(error).__name__]}
        results.append({"track_id": row["track_id"], "fixture_name": row["fixture_name"], **observed})
    report = {"wi": "WI-20260909-ATS-027", "scope": "LOCAL_STORED_PAIRS_ONLY_NOT_BROWSER",
              "ok": all(item["ok"] for item in results), "record_count": len(results),
              "duration_tolerance_seconds": TOLERANCE_SECONDS, "results": results}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("x", encoding="utf-8") as output:
        json.dump(report, output, indent=2)
        output.write("\n")
    print(json.dumps({"ok": report["ok"], "record_count": len(results), "report": str(args.output)}))
    return 0 if report["ok"] else 1


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, KeyError, IndexError, subprocess.SubprocessError) as error:
        print(json.dumps({"ok": False, "error": type(error).__name__}), file=sys.stderr)
        sys.exit(2)
