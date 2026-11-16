"""Bounded, manifest-pinned listening frontend generations (local preparation only).

WI-20261117-ATS-002. No network, service or database operations.
"""
import hashlib
import json
import re
from pathlib import Path

MAX_GENERATIONS = 4  # new generation and at most three explicitly approved predecessors
MAX_FILES = 512
MAX_BYTES = 64 * 1024 * 1024
ASSET = re.compile(r"assets/[A-Za-z0-9_.-]+-[A-Za-z0-9_-]{8,}\.(?:js|css|png|jpg|jpeg|webp|svg|woff2?)\Z")
PIN = re.compile(r"[0-9a-f]{64}\Z")
SECURITY_HEADERS = """        add_header X-Content-Type-Options nosniff always;
        add_header Referrer-Policy no-referrer always;
        add_header X-Frame-Options DENY always;
"""
MAPS = """# BEGIN ATSTUDIO CACHE V1 (http context)
map $status $ats_html_cache {
    default "no-store";
    200 "no-cache";
    304 "no-cache";
}
map "$status:$uri" $ats_asset_cache {
    default "no-store";
    "~^200:/assets/[A-Za-z0-9_.-]+-[A-Za-z0-9_-]{8,}\\.(js|css|png|jpg|jpeg|webp|svg|woff2?)$" "public, max-age=31536000, immutable";
}
# END ATSTUDIO CACHE V1
"""


def need(value, code):
    if not value:
        raise ValueError(code)


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def pinned_generation(root, manifest, manifest_pin):
    root, manifest = Path(root), Path(manifest)
    for target in (root, manifest):
        need(all(not p.is_symlink() for p in (target, *target.parents)), "SYMLINK")
    need(PIN.fullmatch(manifest_pin) and sha(manifest.read_bytes()) == manifest_pin, "MANIFEST_PIN")
    expected = json.loads(manifest.read_bytes())
    need(isinstance(expected, dict) and 1 < len(expected) <= MAX_FILES and "index.html" in expected, "MANIFEST_LAYOUT")
    actual = {}
    for p in root.rglob("*"):
        need(not p.is_symlink(), "SYMLINK")
        if p.is_file():
            name = p.relative_to(root).as_posix()
            need(name == "index.html" or ASSET.fullmatch(name), "ASSET_SCOPE")
            need(p.stat().st_size <= MAX_BYTES, "BYTE_BOUND")
            actual[name] = p.read_bytes()
    need(set(actual) == set(expected), "FILE_SET")
    need(sum(map(len, actual.values())) <= MAX_BYTES, "BYTE_BOUND")
    need(all(PIN.fullmatch(pin) and sha(actual[name]) == pin for name, pin in expected.items()), "FILE_PIN")
    return actual


def assemble(generations, out):
    """Newest-first original manifests, never recursively retain a prior union."""
    need(1 <= len(generations) <= MAX_GENERATIONS, "GENERATION_BOUND")
    contents, lineage = {}, []
    for i, spec in enumerate(generations):
        need(set(spec) == {"root", "manifest", "sha256"}, "GENERATION_KEYS")
        files = pinned_generation(spec["root"], spec["manifest"], spec["sha256"])
        lineage.append({"manifest_sha256": spec["sha256"], "files": len(files)})
        for name, raw in files.items():
            if i and name == "index.html":
                continue
            need(name not in contents or contents[name] == raw, "ASSET_COLLISION")
            contents[name] = raw
    need(len(contents) <= MAX_FILES and sum(map(len, contents.values())) <= MAX_BYTES, "UNION_BOUND")
    out = Path(out)
    need(all(not p.is_symlink() for p in (out, *out.parents)), "OUTPUT_SYMLINK")
    out.mkdir(parents=True, exist_ok=False)
    for name, raw in sorted(contents.items()):
        p = out / name
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_bytes(raw)
    return {"files": {n: sha(v) for n, v in sorted(contents.items())}, "bytes": sum(map(len, contents.values())), "generations": lineage}


def render_site(raw, old_root, new_root):
    need(re.fullmatch(r"/opt/atstudio/releases/[a-zA-Z0-9_-]+/frontend", new_root), "NEW_ROOT")
    text = raw.decode("utf-8").replace("\r\n", "\n")
    # Deliberately reject already-managed input; future builds use the canonical
    # unmodified baseline/template and explicitly reviewed root. Never stack maps.
    need("ATSTUDIO CACHE" not in text and "ats_html_cache" not in text, "ALREADY_MANAGED")
    need(text.count("root " + old_root + ";") == 1, "ROOT_OCCURRENCE")
    text = text.replace("root " + old_root + ";", "root " + new_root + ";")
    html = "        try_files /index.html =404;"
    need(text.count(html) == 2, "HTML_LOCATIONS")
    text = text.replace(html, SECURITY_HEADERS + '        add_header Cache-Control $ats_html_cache always;\n' + html)
    assets = "    location /assets/ {\n        limit_except GET { deny all; }\n        try_files $uri =404;\n    }"
    need(text.count(assets) == 1, "ASSET_LOCATION")
    text = text.replace(assets, "    location /assets/ {\n        limit_except GET { deny all; }\n" + SECURITY_HEADERS + "        add_header Cache-Control $ats_asset_cache always;\n        try_files $uri =404;\n    }")
    return (MAPS + text).encode()
