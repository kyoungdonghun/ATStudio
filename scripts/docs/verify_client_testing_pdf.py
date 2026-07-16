#!/usr/bin/env python3
"""Verify client PDF provenance, metadata, order, and source-body coverage."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import unicodedata
from pathlib import Path

from pypdf import PdfReader

EXPECTED_TITLE = "AT.M 클라이언트 테스트 가이드"


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def strip_frontmatter(text: str) -> str:
    lines = text.splitlines()
    if lines and lines[0].strip() == "---":
        for index in range(1, len(lines)):
            if lines[index].strip() == "---":
                return "\n".join(lines[index + 1 :])
    return text


def visible_segments(text: str) -> list[str]:
    result: list[str] = []
    for raw in strip_frontmatter(text).splitlines():
        line = raw.strip()
        if not line or line.startswith("```") or re.fullmatch(r"[-|: ]+", line):
            continue
        line = re.sub(r"^#{1,3}\s+", "", line)
        line = re.sub(r"^[-*]\s+", "", line)
        line = re.sub(r"^\d+\.\s+", "", line)
        line = line.replace("[ ] ", "").replace("[x] ", "")
        line = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", line)
        line = line.replace("**", "").replace("`", "")
        if line.startswith("|"):
            result.extend(cell.strip() for cell in line.strip("|").split("|") if cell.strip())
        else:
            result.append(line)
    return [item for item in result if len(canonical(item)) >= 4]


def canonical(text: str) -> str:
    text = unicodedata.normalize("NFKC", text).lower()
    return re.sub(r"[^0-9a-z가-힣]+", "", text)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo-root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--pdf", type=Path)
    parser.add_argument("--manifest", type=Path)
    args = parser.parse_args()
    repo_root = args.repo_root.resolve()
    pdf = (args.pdf or repo_root / "output/pdf/atstudio-client-testing-guide.pdf").resolve()
    manifest_path = (args.manifest or repo_root / "output/pdf/atstudio-client-testing-guide.manifest.json").resolve()
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    assert manifest["document"]["title"] == EXPECTED_TITLE
    assert manifest["output"]["sha256"] == sha256(pdf)
    reader = PdfReader(str(pdf))
    assert reader.metadata.title == EXPECTED_TITLE
    assert len(reader.pages) == manifest["output"]["pages"]
    page_texts = [(page.extract_text() or "").strip() for page in reader.pages]
    assert all(len(canonical(text)) >= 10 for text in page_texts), "blank or textless page detected"
    extracted = "\n".join(page_texts)
    assert "�" not in extracted
    canonical_pdf = canonical(extracted)
    source_titles: list[str] = []
    missing: list[str] = []
    total_segments = 0
    matched_segments = 0
    for source in manifest["sources"]:
        path = repo_root / source["path"]
        assert sha256(path) == source["sha256"]
        body = strip_frontmatter(path.read_text(encoding="utf-8"))
        title = next(line.lstrip("# ").strip() for line in body.splitlines() if line.startswith("# "))
        source_titles.append(title)
        for segment in visible_segments(body):
            total_segments += 1
            if canonical(segment) in canonical_pdf:
                matched_segments += 1
            else:
                missing.append(f"{source['path']}: {segment}")
    cursor = 0
    positions: list[int] = []
    for title in source_titles:
        position = canonical_pdf.find(canonical(title), cursor)
        assert position >= 0, f"source title missing or out of order: {title}"
        positions.append(position)
        cursor = position + len(canonical(title))
    coverage = matched_segments / total_segments if total_segments else 1.0
    assert coverage >= 0.98, f"source-body coverage {coverage:.2%}; missing={missing[:10]}"
    print(f"PAGES={len(reader.pages)}")
    print(f"TITLE={reader.metadata.title}")
    print(f"SOURCE_SEGMENTS={matched_segments}/{total_segments} ({coverage:.2%})")
    print(f"SHA256={sha256(pdf)}")
    print("VERIFY=PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
