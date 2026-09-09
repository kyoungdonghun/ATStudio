"""Generate private, deterministic WI027 WAV inputs without overwriting files."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import wave


ROOT = Path(os.environ["LOCALAPPDATA"]) / "ATStudio/validation/wav-browser-20260909"
BIN = Path(os.environ["LOCALAPPDATA"]) / (
    "ATStudio/tools/ffmpeg-128k-20260909/unpacked/"
    "ffmpeg-9.0.1-essentials_build/bin"
)
MIB = 1024 * 1024


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(MIB), b""):
            digest.update(chunk)
    return digest.hexdigest()


def native_environment() -> dict[str, str]:
    allowed = {"systemroot", "windir", "systemdrive", "path", "pathext",
               "temp", "tmp", "lang", "lc_all", "lc_ctype", "tz"}
    return {key: value for key, value in os.environ.items() if key.lower() in allowed}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ffmpeg", type=Path, default=BIN / "ffmpeg.exe")
    args = parser.parse_args()
    if not args.ffmpeg.is_file():
        raise SystemExit("FFmpeg executable not found.")
    specs = [(f"batch20/WI027-sine-{i:02d}.wav", 220 + 17 * i, 10 * MIB)
             for i in range(1, 21)]
    specs += [("edge/WI027-small-valid.wav", 660, 44 + 2 * 44100 * 4),
              ("edge/WI027-near-100MiB.wav", 880, 100 * MIB - 4096),
              ("edge/WI027-over-100MiB.wav", 990, 100 * MIB + 4096)]
    targets = [ROOT / name for name, _, _ in specs]
    targets += [ROOT / "edge/WI027-corrupt.wav", ROOT / "fixture-manifest.json"]
    if any(path.exists() for path in targets):
        raise SystemExit("Refusing to overwrite existing WI027 fixtures or manifest.")
    if ROOT.resolve() != ROOT.absolute():
        raise SystemExit("Fixture root must not redirect through a link.")
    records = []
    for name, frequency, expected_size in specs:
        path = ROOT / name
        path.parent.mkdir(parents=True, exist_ok=True)
        frames = (expected_size - 44) // 4
        command = [str(args.ffmpeg), "-nostdin", "-hide_banner", "-loglevel", "error",
                   "-n", "-f", "lavfi", "-i", f"sine=frequency={frequency}:sample_rate=44100",
                   "-af", f"atrim=end_sample={frames}", "-ac", "2", "-ar", "44100",
                   "-c:a", "pcm_s16le", "-fflags", "+bitexact", "-flags:a", "+bitexact",
                   "-map_metadata", "-1", str(path)]
        result = subprocess.run(command, env=native_environment(), capture_output=True,
                                timeout=120, check=False)
        if result.returncode:
            raise SystemExit(f"FFmpeg failed for {name}; exit={result.returncode}")
        with wave.open(str(path), "rb") as wav:
            observed = (wav.getnchannels(), wav.getframerate(), wav.getsampwidth(),
                        wav.getnframes(), wav.getcomptype())
            if observed != (2, 44100, 2, frames, "NONE"):
                raise SystemExit(f"Unexpected WAV metadata: {name}")
            decoded_bytes = 0
            while block := wav.readframes(262144):
                decoded_bytes += len(block)
        if path.stat().st_size != expected_size or decoded_bytes != frames * 4:
            raise SystemExit(f"WAV byte/frame mismatch: {name}")
        records.append({"path": str(path), "name": path.name, "kind": "valid",
                        "frequency_hz": frequency, "bytes": path.stat().st_size,
                        "frames": frames, "duration_seconds": frames / 44100,
                        "sample_rate": 44100, "channels": 2, "codec": "pcm_s16le",
                        "sha256": sha256(path), "ffmpeg_arguments": command[1:]})
        if len(records) == 20:
            print(f"BATCH20_READY {ROOT / 'batch20'}", flush=True)
    corrupt = ROOT / "edge/WI027-corrupt.wav"
    with corrupt.open("xb") as output:
        output.write(b"WI027 deliberately corrupt WAV: no RIFF PCM data.\r\n")
    records.append({"path": str(corrupt), "name": corrupt.name, "kind": "corrupt",
                    "bytes": corrupt.stat().st_size, "duration_seconds": None,
                    "sha256": sha256(corrupt)})
    batch = records[:20]
    if len({item["sha256"] for item in batch}) != 20:
        raise SystemExit("Batch is not byte-distinct.")
    version = subprocess.run([str(args.ffmpeg), "-version"], capture_output=True,
                             text=True, env=native_environment(), timeout=10, check=True)
    manifest = {"wi": "WI-20260909-ATS-027", "fixture_root": str(ROOT),
                "ffmpeg_path": str(args.ffmpeg.resolve()),
                "ffmpeg_sha256": sha256(args.ffmpeg),
                "ffmpeg_version": version.stdout.splitlines()[0],
                "audio_limit_bytes": 100 * MIB, "files": records}
    with (ROOT / "fixture-manifest.json").open("x", encoding="utf-8") as output:
        json.dump(manifest, output, indent=2)
        output.write("\n")
    print(json.dumps({"fixture_root": str(ROOT), "manifest": str(ROOT / 'fixture-manifest.json'),
                      "batch_count": 20, "unique_batch_sha256": 20,
                      "files": len(records), "total_bytes": sum(r["bytes"] for r in records)}))


if __name__ == "__main__":
    main()
