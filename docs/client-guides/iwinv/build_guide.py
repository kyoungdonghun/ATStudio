"""Build the approved Korean console guide from MA-supplied UI captures."""

from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
from pathlib import Path

from PIL import Image
from pypdf import PdfReader
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph


ROOT = Path(__file__).resolve().parent
PDF = ROOT / "iwinv-console-quick-guide.pdf"
OLD_PDF = ROOT.parents[2] / "output/pdf/ATStudio-server-purchase-guide.pdf"
OLD_SHA256 = "33ceb77131d05f1abe8997e03b09ecee37cf3a4c1f8cc2f115abd45a9dd592fe"
PAGE_W, PAGE_H = A4
LEFT = 40.0
WIDTH = PAGE_W - 2 * LEFT
INK = colors.HexColor("#20252B")
MUTED = colors.HexColor("#5B6670")
TEAL = colors.HexColor("#087E86")
HIGHLIGHT = colors.HexColor("#EF6045")
CAPTURES = {
    "01-console-main.jpg": (1100, 430),
    "02-compute-menu.jpg": (1100, 460),
    "03-virtual-server.jpg": (1300, 540),
}
URLS = [
    "https://console.iwinv.kr/main",
    "https://console.iwinv.kr/instance",
    "https://console.iwinv.kr/member/join",
]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def text(
    pdf: canvas.Canvas,
    content: str,
    x: float,
    top: float,
    width: float = WIDTH,
    size: float = 10.5,
    bold: bool = False,
    color: colors.Color = INK,
    leading: float | None = None,
) -> float:
    style = ParagraphStyle(
        "guide",
        fontName="MalgunBold" if bold else "Malgun",
        fontSize=size,
        leading=leading or size * 1.5,
        textColor=color,
        alignment=TA_LEFT,
        wordWrap="CJK",
    )
    paragraph = Paragraph(content, style)
    _, height = paragraph.wrap(width, PAGE_H)
    if top + height > PAGE_H - 32:
        raise ValueError(f"Text overflow: {content[:40]}")
    paragraph.drawOn(pdf, x, PAGE_H - top - height)
    return top + height


def step(pdf: canvas.Canvas, number: int, title: str, top: float) -> None:
    pdf.setFillColor(TEAL)
    pdf.circle(LEFT + 10, PAGE_H - top - 10, 10, stroke=0, fill=1)
    pdf.setFillColor(colors.white)
    pdf.setFont("MalgunBold", 10)
    pdf.drawCentredString(LEFT + 10, PAGE_H - top - 13.5, str(number))
    text(pdf, title, LEFT + 30, top - 1, WIDTH - 30, 13, True)


def screenshot(
    pdf: canvas.Canvas,
    name: str,
    top: float,
    number: int,
    target: tuple[int, int, int, int],
) -> None:
    path = ROOT / "screenshots" / name
    with Image.open(path) as image:
        width, height = image.size
    if (width, height) != CAPTURES[name]:
        raise ValueError(f"Unexpected capture size: {name}")
    scale = WIDTH / width
    drawn_height = height * scale
    pdf.drawImage(str(path), LEFT, PAGE_H - top - drawn_height, WIDTH, drawn_height)
    pdf.setStrokeColor(colors.HexColor("#CDD4DA"))
    pdf.setLineWidth(0.5)
    pdf.rect(LEFT, PAGE_H - top - drawn_height, WIDTH, drawn_height, fill=0)

    # 원본 캡처는 그대로 두고 PDF 레이어에만 클릭 위치를 표시한다.
    x, y, w, h = target
    pdf.setStrokeColor(HIGHLIGHT)
    pdf.setLineWidth(1.8)
    pdf.rect(LEFT + x * scale, PAGE_H - top - (y + h) * scale,
             w * scale, h * scale, fill=0)
    cx = LEFT + (x + w) * scale + 12
    cy = PAGE_H - top - (y + h / 2) * scale
    pdf.setFillColor(HIGHLIGHT)
    pdf.circle(cx, cy, 8, fill=1, stroke=0)
    pdf.setFillColor(colors.white)
    pdf.setFont("MalgunBold", 8)
    pdf.drawCentredString(cx, cy - 2.8, str(number))


def footer(pdf: canvas.Canvas, page: int) -> None:
    pdf.setStrokeColor(colors.HexColor("#D7DDE1"))
    pdf.setLineWidth(0.6)
    pdf.line(LEFT, 49, PAGE_W - LEFT, 49)
    text(pdf, "AT.M | iwinv 콘솔 안내 · 2026.10.10 확인", LEFT, PAGE_H - 44,
         WIDTH - 40, 8.2, color=MUTED, leading=10)
    pdf.setFont("Malgun", 8.2)
    pdf.setFillColor(MUTED)
    pdf.drawRightString(PAGE_W - LEFT, 29, f"{page} / 2")


def header(pdf: canvas.Canvas, title: str, subtitle: str) -> None:
    text(pdf, "AT.M / 서버 준비 안내", LEFT, 31, size=9, bold=True, color=TEAL)
    text(pdf, title, LEFT, 58, size=23, bold=True, leading=31)
    text(pdf, subtitle, LEFT, 102, size=10.5, color=MUTED)


def build(font_dir: Path) -> None:
    if sha256(OLD_PDF) != OLD_SHA256:
        raise ValueError("Historical purchase-guide hash changed before generation")
    for name, file in [("Malgun", "malgun.ttf"), ("MalgunBold", "malgunbd.ttf")]:
        pdfmetrics.registerFont(TTFont(name, str(font_dir / file)))
    before = {name: sha256(ROOT / "screenshots" / name) for name in CAPTURES}
    pdf = canvas.Canvas(str(PDF), pagesize=A4, pageCompression=1, invariant=1)
    pdf.setTitle("AT.M - iwinv 콘솔 시작 안내")
    pdf.setAuthor("ATStudio")
    pdf.setSubject("서버 신청 위치와 본인인증·카드등록 준비 안내")

    header(pdf, "서버 신청 메뉴 찾기", "iwinv에 로그인한 뒤, 아래 순서로 이동해 주세요.")
    text(pdf, f'접속 주소: <link href="{URLS[0]}" color="#087E86">'
         "https://console.iwinv.kr/main</link>", LEFT, 129, size=10)
    step(pdf, 1, "왼쪽 메뉴에서 ‘컴퓨트’를 눌러 주세요.", 168)
    screenshot(pdf, "01-console-main.jpg", 202, 1, (25, 324, 230, 38))
    step(pdf, 2, "펼쳐진 메뉴에서 ‘가상 서버’를 눌러 주세요.", 435)
    screenshot(pdf, "02-compute-menu.jpg", 469, 2, (61, 370, 191, 32))
    text(pdf, "이동 순서  ·  콘솔 → 컴퓨트 → 가상 서버 → 생성하기", LEFT, 711,
         size=11, bold=True, color=TEAL)
    text(pdf, "화면 크기나 계정 상태에 따라 메뉴 위치가 조금 달라질 수 있어요.",
         LEFT, 740, size=9, color=MUTED)
    footer(pdf, 1)
    pdf.showPage()

    header(pdf, "여기까지 준비해 주세요", "서버의 최종 생성과 결제는 담당자와 상품·옵션을 확인한 뒤 진행해요.")
    step(pdf, 3, "가상 서버 화면의 ‘생성하기’를 눌러 주세요.", 141)
    screenshot(pdf, "03-virtual-server.jpg", 175, 3, (326, 206, 110, 50))
    text(pdf, "이 버튼은 신청 절차의 입구입니다. 이후 최종 생성·결제는 진행하지 마세요.",
         LEFT, 400, size=9.5, color=MUTED)

    step(pdf, 4, "본인인증·카드등록 안내가 나오면", 433)
    text(pdf, "확인한 계정에서는 본인인증과 카드등록을 먼저 완료하라는 안내가 나왔어요.",
         LEFT, 464, size=10.5)
    text(pdf, "계정 명의자가 직접 본인인증을 하고, 약관을 확인한 뒤 카드등록을 진행해 주세요.",
         LEFT, 488, size=10.5)
    text(pdf, "비밀번호·인증번호·카드정보는 채팅이나 메일로 보내지 마세요.",
         LEFT, 524, size=10.5, bold=True, color=TEAL)

    text(pdf, "사업자 정보는 꼭 필요한가요?", LEFT, 561, size=12, bold=True)
    text(pdf, "확인한 화면은 사업자 정보와 세금계산서 신청을 별도로 안내하고 있어요.<br/>"
         "개인 이용자 모두에게 필수라는 안내는 아니었어요.<br/>사업자 명의로 이용하거나 "
         "세금계산서가 필요하다면 실제 사업자 정보를 확인해 주세요.",
         LEFT, 587, size=10.2, leading=16)
    text(pdf, "개인/사업자 중 어느 명의로 운영할지 정한 뒤 입력해 주세요.",
         LEFT, 645, size=8.5, color=MUTED, leading=13)

    pdf.setFillColor(colors.HexColor("#EAF5F3"))
    pdf.rect(LEFT, PAGE_H - 724, WIDTH, 50, fill=1, stroke=0)
    text(pdf, "인증·카드등록이 끝나면 담당자에게 ‘완료’만 알려 주세요.",
         LEFT + 12, 684, WIDTH - 24, 11, True, TEAL)
    text(pdf, "서버 사양·옵션 확인과 최종 생성은 그다음에 함께 진행합니다.",
         LEFT + 12, 706, WIDTH - 24, 9.5)
    text(pdf, "화면 확인: 2026.10.10. 메뉴 캡처는 이번 작업, 인증 안내는 같은 날 앞선 확인 기준.",
         LEFT, 738, size=8.1, color=MUTED, leading=12)
    text(pdf, f'공식 화면: <link href="{URLS[1]}" color="#087E86">가상 서버</link>'
         f' / <link href="{URLS[2]}" color="#087E86">본인인증</link>'
         "  ·  카드등록 상세 화면은 이 안내서에 포함하지 않았어요.",
         LEFT, 755, size=8.1, color=MUTED, leading=12)
    footer(pdf, 2)
    pdf.save()
    if before != {name: sha256(ROOT / "screenshots" / name) for name in CAPTURES}:
        raise ValueError("Source screenshots changed")
    if sha256(OLD_PDF) != OLD_SHA256:
        raise ValueError("Historical purchase-guide hash changed after generation")


def validate() -> dict:
    reader = PdfReader(PDF)
    if len(reader.pages) != 2:
        raise ValueError("Expected exactly two pages")
    all_text = "\n".join(page.extract_text() for page in reader.pages)
    required = ["컴퓨트", "가상 서버", "생성하기", "본인인증", "카드등록",
                "사업자", "최종 생성", "담당자"]
    if any(word not in all_text for word in required) or "\ufffd" in all_text:
        raise ValueError("Required Korean text missing or replacement character found")
    links = []
    embedded = set()
    for page in reader.pages:
        if abs(float(page.mediabox.width) - PAGE_W) > 0.1:
            raise ValueError("Not A4 width")
        if abs(float(page.mediabox.height) - PAGE_H) > 0.1:
            raise ValueError("Not A4 height")
        for annotation in page.get("/Annots", []):
            action = annotation.get_object().get("/A", {})
            if "/URI" in action:
                links.append(str(action["/URI"]))
        for font in page["/Resources"]["/Font"].values():
            obj = font.get_object()
            descriptor = obj.get("/FontDescriptor")
            if descriptor and "/FontFile2" in descriptor.get_object():
                embedded.add(str(obj["/BaseFont"]))
    if sorted(links) != sorted(URLS) or len(embedded) != 2:
        raise ValueError("Link or Korean font embedding validation failed")
    return {
        "pdf": str(PDF), "page_count": len(reader.pages), "size_bytes": PDF.stat().st_size,
        "sha256": sha256(PDF), "embedded_fonts": sorted(embedded), "links": links,
        "replacement_characters": all_text.count("\ufffd"),
        "historical_pdf_sha256": sha256(OLD_PDF),
        "screenshot_sha256": {name: sha256(ROOT / "screenshots" / name) for name in CAPTURES},
        "visual_review": "required after rendering; not an automated claim",
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--font-dir", type=Path, default=Path("C:/Windows/Fonts"))
    parser.add_argument("--render-with", type=Path, help="Absolute pdftoppm executable path")
    args = parser.parse_args()
    build(args.font_dir)
    report = validate()
    qa = ROOT / "qa"
    qa.mkdir(exist_ok=True)
    if args.render_with:
        subprocess.run([str(args.render_with), "-r", "144", "-png", str(PDF),
                        str(qa / "guide")], check=True)
        report["render_tool"] = str(args.render_with)
        report["render_dpi"] = 144
        report["renders"] = [str(qa / f"guide-{number}.png") for number in (1, 2)]
    (qa / "validation.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(report, ensure_ascii=True, indent=2))


if __name__ == "__main__":
    main()
