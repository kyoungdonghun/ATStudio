#!/usr/bin/env python3
"""Generate the deterministic AT.M client testing PDF and provenance manifest."""

from __future__ import annotations

import argparse
import hashlib
import html
import json
import platform
import re
import shutil
import subprocess
import sys
from pathlib import Path

import reportlab
from pdf_provenance import (
    MALGUN_GOTHIC_BOLD_SHA256,
    MALGUN_GOTHIC_REGULAR_SHA256,
    normalized_text_sha256,
    normalized_text_size,
    validate_font_input,
    write_utf8_lf_text,
)
from pypdf import PdfReader
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import (
    CondPageBreak,
    HRFlowable,
    KeepTogether,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

GENERATOR_VERSION = "1.5.0"
DOCUMENT_TITLE = "AT.M 클라이언트 테스트 가이드"
DOCUMENT_DATE = "2026-07-16"
FIXED_TIMESTAMP = "2026-07-16T00:00:00+09:00"
REPLAY_SCRIPT = "scripts/docs/replay-client-testing-pdf.ps1"
DEPENDENCY_LOCK = "scripts/docs/client-testing-pdf-requirements.txt"
REPLAY_COMMAND = (
    "powershell -NoProfile -ExecutionPolicy Bypass -File "
    "scripts/docs/replay-client-testing-pdf.ps1 "
    "-PythonExecutable $env:ATSTUDIO_PDF_PYTHON "
    "-RenderTool $env:ATSTUDIO_PDF_RENDER_TOOL"
)
SOURCE_PATHS = [
    "docs/client/testing-guide.md",
    "docs/client/1-quick-checklist.md",
    "docs/client/2-full-feature-checklist.md",
    "docs/client/3-admin-checklist.md",
    "docs/client/4-sr-format.md",
    "docs/client/5-ai-prompt.md",
    "docs/client/0-site-policy.md",
]
EXCLUDED_PATHS = ["docs/client/index.md", "docs/client/_internal-feature-map.md"]


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def resolve_executable(executable: str) -> Path:
    candidate = Path(executable).expanduser()
    if candidate.is_file():
        return candidate.resolve()
    resolved = shutil.which(executable)
    if resolved:
        return Path(resolved).resolve()
    raise FileNotFoundError(f"Executable path or PATH command not found: {executable}")


def executable_version(executable: Path) -> str:
    result = subprocess.run(
        [str(executable), "-v"],
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    output = "\n".join(part for part in (result.stdout, result.stderr) if part).strip()
    match = re.search(r"\b(\d+(?:\.\d+){1,3})\b", output)
    if not match:
        raise RuntimeError(f"Unable to determine version for {executable.name}")
    return match.group(1)


def strip_frontmatter(text: str) -> str:
    lines = text.splitlines()
    if lines and lines[0].strip() == "---":
        for index in range(1, len(lines)):
            if lines[index].strip() == "---":
                return "\n".join(lines[index + 1 :]).strip()
    return text.strip()


def inline_markup(text: str) -> str:
    escaped = html.escape(text, quote=False)
    escaped = re.sub(r"`([^`]+)`", r'<font name="MalgunGothic" color="#0F5D4E">\1</font>', escaped)
    escaped = re.sub(r"\*\*([^*]+)\*\*", r"<b>\1</b>", escaped)
    escaped = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r'<link href="\2" color="#0F5D4E">\1</link>', escaped)
    return escaped


def make_styles() -> dict[str, ParagraphStyle]:
    base = getSampleStyleSheet()
    return {
        "cover_title": ParagraphStyle(
            "CoverTitle",
            parent=base["Title"],
            fontName="MalgunGothicBold",
            fontSize=25,
            leading=34,
            textColor=colors.HexColor("#17201E"),
            alignment=TA_LEFT,
            spaceAfter=8 * mm,
        ),
        "cover_subtitle": ParagraphStyle(
            "CoverSubtitle",
            parent=base["Normal"],
            fontName="MalgunGothic",
            fontSize=11,
            leading=18,
            textColor=colors.HexColor("#4E5C58"),
        ),
        "h1": ParagraphStyle(
            "H1",
            parent=base["Heading1"],
            fontName="MalgunGothicBold",
            fontSize=19,
            leading=26,
            textColor=colors.HexColor("#17201E"),
            spaceBefore=2 * mm,
            spaceAfter=5 * mm,
            keepWithNext=True,
        ),
        "h2": ParagraphStyle(
            "H2",
            parent=base["Heading2"],
            fontName="MalgunGothicBold",
            fontSize=13,
            leading=19,
            textColor=colors.HexColor("#0F5D4E"),
            spaceBefore=5 * mm,
            spaceAfter=2.5 * mm,
            keepWithNext=True,
        ),
        "h3": ParagraphStyle(
            "H3",
            parent=base["Heading3"],
            fontName="MalgunGothicBold",
            fontSize=10.5,
            leading=16,
            textColor=colors.HexColor("#293633"),
            spaceBefore=3.5 * mm,
            spaceAfter=1.5 * mm,
            keepWithNext=True,
        ),
        "body": ParagraphStyle(
            "Body",
            parent=base["BodyText"],
            fontName="MalgunGothic",
            fontSize=9.3,
            leading=15,
            textColor=colors.HexColor("#27302E"),
            spaceAfter=2.2 * mm,
            wordWrap="CJK",
        ),
        "bullet": ParagraphStyle(
            "Bullet",
            parent=base["BodyText"],
            fontName="MalgunGothic",
            fontSize=9.2,
            leading=14.5,
            leftIndent=5 * mm,
            firstLineIndent=-3.5 * mm,
            bulletIndent=1 * mm,
            textColor=colors.HexColor("#27302E"),
            spaceAfter=1.2 * mm,
            wordWrap="CJK",
        ),
        "code": ParagraphStyle(
            "Code",
            parent=base["Code"],
            fontName="MalgunGothic",
            fontSize=8.2,
            leading=12.5,
            leftIndent=4 * mm,
            rightIndent=4 * mm,
            borderColor=colors.HexColor("#CAD4D0"),
            borderWidth=0.5,
            borderPadding=3 * mm,
            backColor=colors.HexColor("#F3F6F5"),
            textColor=colors.HexColor("#26302D"),
            spaceBefore=2 * mm,
            spaceAfter=3 * mm,
            wordWrap="CJK",
        ),
        "table": ParagraphStyle(
            "TableCell",
            parent=base["BodyText"],
            fontName="MalgunGothic",
            fontSize=7.8,
            leading=11.5,
            textColor=colors.HexColor("#27302E"),
            wordWrap="CJK",
        ),
        "table_head": ParagraphStyle(
            "TableHead",
            parent=base["BodyText"],
            fontName="MalgunGothicBold",
            fontSize=7.8,
            leading=11.5,
            textColor=colors.white,
            wordWrap="CJK",
        ),
        "footer": ParagraphStyle(
            "Footer",
            parent=base["Normal"],
            fontName="MalgunGothic",
            fontSize=7.3,
            leading=9,
            textColor=colors.HexColor("#65716E"),
            alignment=TA_CENTER,
        ),
    }


def table_widths(column_count: int, available: float) -> list[float]:
    if column_count == 2:
        return [available * 0.28, available * 0.72]
    if column_count == 3:
        return [available * 0.18, available * 0.22, available * 0.60]
    return [available / column_count] * column_count


def render_table(rows: list[str], styles: dict[str, ParagraphStyle], width: float) -> Table:
    parsed: list[list[str]] = []
    for line in rows:
        cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
        if cells and all(re.fullmatch(r":?-{3,}:?", cell) for cell in cells):
            continue
        parsed.append(cells)
    column_count = max(len(row) for row in parsed)
    normalized = [row + [""] * (column_count - len(row)) for row in parsed]
    data = []
    for row_index, row in enumerate(normalized):
        style = styles["table_head"] if row_index == 0 else styles["table"]
        data.append([Paragraph(inline_markup(cell), style) for cell in row])
    table = Table(data, colWidths=table_widths(column_count, width), repeatRows=1, hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#315C52")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#C4CECB")),
                ("BACKGROUND", (0, 1), (-1, -1), colors.white),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F7F9F8")]),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 4),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
            ]
        )
    )
    return table


def markdown_story(text: str, styles: dict[str, ParagraphStyle], width: float) -> list:
    lines = strip_frontmatter(text).splitlines()
    story: list = []
    index = 0
    paragraph: list[str] = []

    def flush_paragraph() -> None:
        if paragraph:
            story.append(Paragraph(inline_markup(" ".join(item.strip() for item in paragraph)), styles["body"]))
            paragraph.clear()

    def append_list(items: list[Paragraph]) -> None:
        if story and isinstance(story[-1], Paragraph) and story[-1].style.name in {"H1", "H2", "H3"}:
            items.insert(0, story.pop())
        story.append(KeepTogether(items))

    while index < len(lines):
        line = lines[index]
        stripped = line.strip()
        if stripped.startswith("```"):
            flush_paragraph()
            code_lines: list[str] = []
            index += 1
            while index < len(lines) and not lines[index].strip().startswith("```"):
                code_lines.append(lines[index])
                index += 1
            code_html = "<br/>".join(html.escape(item) if item else " " for item in code_lines)
            story.append(Paragraph(code_html, styles["code"]))
        elif stripped.startswith("|") and "|" in stripped[1:]:
            flush_paragraph()
            rows: list[str] = []
            while index < len(lines) and lines[index].strip().startswith("|"):
                rows.append(lines[index])
                index += 1
            story.append(render_table(rows, styles, width))
            story.append(Spacer(1, 3 * mm))
            continue
        elif re.match(r"^#{1,3}\s+", stripped):
            flush_paragraph()
            level = len(stripped) - len(stripped.lstrip("#"))
            title = stripped[level:].strip()
            story.append(Paragraph(inline_markup(title), styles[f"h{level}"]))
        elif stripped in {"---", "***"}:
            flush_paragraph()
            story.append(HRFlowable(width="100%", thickness=0.6, color=colors.HexColor("#CBD3D1"), spaceBefore=2 * mm, spaceAfter=3 * mm))
        elif re.match(r"^[-*]\s+", stripped):
            flush_paragraph()
            items = []
            while index < len(lines) and re.match(r"^[-*]\s+", lines[index].strip()):
                item = re.sub(r"^[-*]\s+", "", lines[index].strip())
                if item.startswith("[ ] "):
                    item = "□ " + item[4:]
                elif item.lower().startswith("[x] "):
                    item = "■ " + item[4:]
                items.append(Paragraph(inline_markup(item), styles["bullet"], bulletText="•"))
                index += 1
            append_list(items)
            continue
        elif re.match(r"^\d+\.\s+", stripped):
            flush_paragraph()
            items = []
            while index < len(lines) and re.match(r"^\d+\.\s+", lines[index].strip()):
                match = re.match(r"^(\d+)\.\s+(.*)$", lines[index].strip())
                assert match
                items.append(
                    Paragraph(
                        inline_markup(match.group(2)),
                        styles["bullet"],
                        bulletText=f"{match.group(1)}.",
                    )
                )
                index += 1
            append_list(items)
            continue
        elif not stripped:
            flush_paragraph()
        else:
            paragraph.append(line)
        index += 1
    flush_paragraph()
    return story


class DeterministicCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        kwargs["invariant"] = 1
        super().__init__(*args, **kwargs)
        self.setTitle(DOCUMENT_TITLE)
        self.setAuthor("ATStudio")
        self.setSubject("AT.M client acceptance testing guide")
        self.setCreator(f"scripts/docs/generate_client_testing_pdf.py {GENERATOR_VERSION}")


def page_decorations(styles: dict[str, ParagraphStyle]):
    def draw(pdf: canvas.Canvas, document: SimpleDocTemplate) -> None:
        pdf.saveState()
        page_width, page_height = A4
        pdf.setStrokeColor(colors.HexColor("#D3DBD8"))
        pdf.setLineWidth(0.5)
        pdf.line(document.leftMargin, 16 * mm, page_width - document.rightMargin, 16 * mm)
        footer = Paragraph(f"AT.M · {DOCUMENT_DATE} · {document.page}", styles["footer"])
        footer.wrapOn(pdf, document.width, 8 * mm)
        footer.drawOn(pdf, document.leftMargin, 7 * mm)
        if document.page > 1:
            pdf.setFont("MalgunGothic", 7.5)
            pdf.setFillColor(colors.HexColor("#65716E"))
            pdf.drawString(document.leftMargin, page_height - 13 * mm, DOCUMENT_TITLE)
        pdf.restoreState()

    return draw


def build_pdf(repo_root: Path, output: Path, font_regular: Path, font_bold: Path) -> None:
    pdfmetrics.registerFont(TTFont("MalgunGothic", str(font_regular), subfontIndex=0))
    pdfmetrics.registerFont(TTFont("MalgunGothicBold", str(font_bold), subfontIndex=0))
    styles = make_styles()
    document = SimpleDocTemplate(
        str(output),
        pagesize=A4,
        rightMargin=22 * mm,
        leftMargin=22 * mm,
        topMargin=23 * mm,
        bottomMargin=22 * mm,
        title=DOCUMENT_TITLE,
        author="ATStudio",
    )
    story: list = [
        Spacer(1, 29 * mm),
        Paragraph(DOCUMENT_TITLE, styles["cover_title"]),
        HRFlowable(width="34%", thickness=3, color=colors.HexColor("#65A84F"), hAlign="LEFT", spaceAfter=8 * mm),
        Paragraph("비기술 사용자용 기능 확인, 관리자 점검, 문제 제보 안내", styles["cover_subtitle"]),
        Spacer(1, 8 * mm),
        Paragraph(f"문서 기준일: {DOCUMENT_DATE}", styles["cover_subtitle"]),
        Spacer(1, 18 * mm),
        Paragraph("포함 문서", styles["h2"]),
    ]
    for number, source in enumerate(SOURCE_PATHS, start=1):
        story.append(Paragraph(f"{number}. {source}", styles["body"]))
    story.extend(
        [
            Spacer(1, 8 * mm),
            Paragraph("이 PDF는 고정된 7개 Markdown 원문에서 재현 가능하게 생성됩니다.", styles["cover_subtitle"]),
        ]
    )
    for source_index, source in enumerate(SOURCE_PATHS):
        if source_index == 0:
            story.append(PageBreak())
        else:
            story.extend([CondPageBreak(75 * mm), Spacer(1, 6 * mm)])
        source_path = repo_root / source
        story.extend(markdown_story(source_path.read_text(encoding="utf-8"), styles, document.width))
    decoration = page_decorations(styles)
    document.build(story, onFirstPage=decoration, onLaterPages=decoration, canvasmaker=DeterministicCanvas)


def write_manifest(
    repo_root: Path,
    output: Path,
    manifest_path: Path,
    font_regular: Path,
    font_bold: Path,
    render_tool: Path,
) -> None:
    reader = PdfReader(str(output))
    source_records = []
    for source in SOURCE_PATHS:
        path = repo_root / source
        source_records.append(
            {
                "path": source,
                "sha256": normalized_text_sha256(path),
                "bytes": normalized_text_size(path),
            }
        )
    manifest = {
        "schema_version": 3,
        "document": {
            "title": DOCUMENT_TITLE,
            "document_date": DOCUMENT_DATE,
            "generated_at": FIXED_TIMESTAMP,
            "language": "ko-KR",
        },
        "generator": {
            "path": "scripts/docs/generate_client_testing_pdf.py",
            "version": GENERATOR_VERSION,
            "python_implementation": platform.python_implementation(),
            "python_version": sys.version.split()[0],
            "reportlab_version": reportlab.Version,
            "pypdf_version": __import__("pypdf").__version__,
            "deterministic": True,
            "reportlab_invariant": 1,
        },
        "replay": {
            "script": REPLAY_SCRIPT,
            "script_sha256": normalized_text_sha256(repo_root / REPLAY_SCRIPT),
            "command": REPLAY_COMMAND,
            "dependency_lock": DEPENDENCY_LOCK,
            "dependency_lock_sha256": normalized_text_sha256(repo_root / DEPENDENCY_LOCK),
            "text_hash_contract": "SHA-256 of UTF-8 text with LF line endings",
            "python_input": "ATSTUDIO_PDF_PYTHON: explicit Python 3.10+ executable path",
            "render_tool_input": "ATSTUDIO_PDF_RENDER_TOOL: explicit pdftoppm executable path",
            "isolated_environment": "temporary venv created and removed by the replay script",
        },
        "verification": {
            "script": "scripts/docs/verify_client_testing_pdf.py",
            "render_tool": "pdftoppm",
            "render_tool_version": executable_version(render_tool),
            "render_tool_input_contract": "explicit executable path supplied to replay wrapper",
            "render_command": "pdftoppm -png -r 144 "
            "output/pdf/atstudio-client-testing-guide.pdf <temporary-render-prefix>",
            "intermediate_directory": "operating-system temporary directory",
            "version_source": "queried from the explicit --render-tool path",
        },
        "layout": {
            "heading_keep_with_next": True,
            "heading_and_contiguous_list_keep_together": True,
            "later_source_conditional_page_break_mm": 75,
            "later_source_spacing_mm": 6,
        },
        "fonts": [
            {"name": "Malgun Gothic", "path": str(font_regular).replace("\\", "/"), "sha256": sha256(font_regular)},
            {"name": "Malgun Gothic Bold", "path": str(font_bold).replace("\\", "/"), "sha256": sha256(font_bold)},
        ],
        "sources": source_records,
        "excluded_from_body": EXCLUDED_PATHS,
        "output": {
            "path": output.relative_to(repo_root).as_posix(),
            "sha256": sha256(output),
            "bytes": output.stat().st_size,
            "pages": len(reader.pages),
            "pdf_title": reader.metadata.title,
        },
    }
    write_utf8_lf_text(
        manifest_path,
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
    )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo-root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--output", type=Path)
    parser.add_argument("--manifest", type=Path)
    parser.add_argument("--font-regular", type=Path, default=Path("C:/Windows/Fonts/malgun.ttf"))
    parser.add_argument("--font-bold", type=Path, default=Path("C:/Windows/Fonts/malgunbd.ttf"))
    parser.add_argument(
        "--render-tool",
        required=True,
        help="Explicit pdftoppm executable path used to query its version.",
    )
    args = parser.parse_args()
    repo_root = args.repo_root.resolve()
    output = (args.output or repo_root / "output/pdf/atstudio-client-testing-guide.pdf").resolve()
    manifest = (args.manifest or repo_root / "output/pdf/atstudio-client-testing-guide.manifest.json").resolve()
    for source in SOURCE_PATHS:
        if not (repo_root / source).is_file():
            raise FileNotFoundError(source)
    font_regular = validate_font_input(
        args.font_regular,
        label="Malgun Gothic regular",
        expected_sha256=MALGUN_GOTHIC_REGULAR_SHA256,
    )
    font_bold = validate_font_input(
        args.font_bold,
        label="Malgun Gothic bold",
        expected_sha256=MALGUN_GOTHIC_BOLD_SHA256,
    )
    render_tool = resolve_executable(args.render_tool)
    output.parent.mkdir(parents=True, exist_ok=True)
    build_pdf(repo_root, output, font_regular, font_bold)
    write_manifest(repo_root, output, manifest, font_regular, font_bold, render_tool)
    print(f"PDF={output}")
    print(f"MANIFEST={manifest}")
    print(f"SHA256={sha256(output)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
