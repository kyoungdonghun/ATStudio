"""Build the approved domain guide from MA-verified facts and UI captures."""

from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
from pathlib import Path

from PIL import Image
from pypdf import PdfReader
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph


ROOT = Path(__file__).resolve().parent
REPO = ROOT.parents[2]
PDF = ROOT / "atmusic-domain-purchase-guide.pdf"
QA = ROOT / "qa"
PAGE_W, PAGE_H = A4
LEFT = 40.0
WIDTH = PAGE_W - 2 * LEFT
INK = colors.HexColor("#20252B")
MUTED = colors.HexColor("#5B6670")
TEAL = colors.HexColor("#087E86")
HIGHLIGHT = colors.HexColor("#EF6045")
PRESERVED = {
    "docs/client-guides/iwinv/build_guide.py":
        "d3e0b6b039f05b9e93f21f91de735a35775f0c1f4416f9f357135ba5c9b5051a",
    "docs/client-guides/iwinv/iwinv-console-quick-guide.pdf":
        "216eee0f6a6eff946e0b90e849b750fe84f16340f4104cde0d7475833e75f0a3",
    "docs/client-guides/iwinv/qa/guide-1.png":
        "d9ef62c23c16b42f6bf7198802d005148708356420ef28b03a0ff5c20a28af2b",
    "docs/client-guides/iwinv/qa/guide-2.png":
        "63c0319f4a9914355924187b2286c43cf8c5d25aff4a0e9871aeff2823c5fe19",
    "docs/client-guides/iwinv/qa/validation.json":
        "4e803bd3cf206549c8385670b3aa4612415b155df8d1a53cbbf0269372a94b7d",
    "docs/client-guides/iwinv/screenshots/01-console-main.jpg":
        "f435b9fe21ef7659272669435a21024655bb0b561a334b99e727884b9ca6ecf4",
    "docs/client-guides/iwinv/screenshots/02-compute-menu.jpg":
        "4b0a16aaea792175c0347732cc54251af16c9b79d8d1a864a475477230a251c6",
    "docs/client-guides/iwinv/screenshots/03-virtual-server.jpg":
        "e90e53678c83e329000fa9d1790129488bc8712c625ed1302e449ca6e98b75b1",
    "output/pdf/ATStudio-server-purchase-guide.pdf":
        "33ceb77131d05f1abe8997e03b09ecee37cf3a4c1f8cc2f115abd45a9dd592fe",
}
CAPTURES = {
    "domain-01-register-menu.jpg": (1300, 380),
    "domain-02-public-search.jpg": (1300, 350),
    "domain-03-search-result.jpg": (1300, 530),
}
URLS = [
    "https://console.iwinv.kr/domain",
    "https://www.iwinv.kr/account/domain",
    "https://docs.iwinv.kr/service/domain/domain/domain_guide/domain_create/",
    "https://docs.iwinv.kr/service/domain/domain/domain_guide/domain_period/",
]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify_preservation() -> dict[str, str]:
    actual = {name: sha256(REPO / name) for name in PRESERVED}
    if actual != PRESERVED:
        raise ValueError("Prior guide assets changed")
    return actual


def text(
    pdf: canvas.Canvas,
    content: str,
    top: float,
    x: float = LEFT,
    width: float = WIDTH,
    size: float = 10.5,
    bold: bool = False,
    color: colors.Color = INK,
    leading: float | None = None,
) -> float:
    style = ParagraphStyle(
        "domain-guide",
        fontName="MalgunBold" if bold else "Malgun",
        fontSize=size,
        leading=leading or size * 1.5,
        textColor=color,
        wordWrap="CJK",
    )
    paragraph = Paragraph(content, style)
    _, height = paragraph.wrap(width, PAGE_H)
    if top + height > PAGE_H - 30:
        raise ValueError(f"Text overflow: {content[:40]}")
    paragraph.drawOn(pdf, x, PAGE_H - top - height)
    return top + height


def step(pdf: canvas.Canvas, number: int, title: str, top: float) -> None:
    pdf.setFillColor(TEAL)
    pdf.circle(LEFT + 10, PAGE_H - top - 10, 10, stroke=0, fill=1)
    pdf.setFillColor(colors.white)
    pdf.setFont("MalgunBold", 10)
    pdf.drawCentredString(LEFT + 10, PAGE_H - top - 13.5, str(number))
    text(pdf, title, top - 1, LEFT + 30, WIDTH - 30, 13, True)


def screenshot(
    pdf: canvas.Canvas,
    name: str,
    top: float,
    target: tuple[int, int, int, int] | None = None,
) -> float:
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
    if target:
        # 실제 캡처는 보존하고 클릭 위치만 PDF 위에 표시한다.
        x, y, w, h = target
        pdf.setStrokeColor(HIGHLIGHT)
        pdf.setLineWidth(1.8)
        pdf.rect(LEFT + x * scale, PAGE_H - top - (y + h) * scale,
                 w * scale, h * scale, fill=0)
    return top + drawn_height


def footer(pdf: canvas.Canvas, page: int) -> None:
    pdf.setStrokeColor(colors.HexColor("#D7DDE1"))
    pdf.setLineWidth(0.6)
    pdf.line(LEFT, 49, PAGE_W - LEFT, 49)
    text(pdf, "AT.M | 도메인 구매 안내 · 2026.10.10 확인", PAGE_H - 44,
         width=WIDTH - 40, size=8.2, color=MUTED, leading=10)
    pdf.setFont("Malgun", 8.2)
    pdf.setFillColor(MUTED)
    pdf.drawRightString(PAGE_W - LEFT, 29, f"{page} / 2")


def build(font_dir: Path) -> None:
    for name, file in [("Malgun", "malgun.ttf"), ("MalgunBold", "malgunbd.ttf")]:
        pdfmetrics.registerFont(TTFont(name, str(font_dir / file)))
    pdf = canvas.Canvas(str(PDF), pagesize=A4, pageCompression=1, invariant=1)
    pdf.setTitle("AT.M - atmusic.co.kr 도메인 구매 안내")
    pdf.setAuthor("ATStudio")
    pdf.setSubject("실제 메뉴와 검색 결과, 가격 및 구매 전 확인 사항")

    text(pdf, "AT.M / 도메인 구매 안내", 31, size=9, bold=True, color=TEAL)
    text(pdf, "atmusic.co.kr", 58, size=27, bold=True, leading=35)
    text(pdf, "사이트 주소를 준비하는 안내서입니다. 구매는 계정 명의자가 직접 진행해 주세요.",
         105, size=10.2, color=MUTED)
    step(pdf, 1, "콘솔에서 ‘등록하기’ 찾기", 150)
    text(pdf, f'<link href="{URLS[0]}" color="#087E86">콘솔 도메인 화면 열기</link>'
         "  ·  왼쪽 도메인 → 펼쳐진 도메인 → 등록하기", 183, size=10)
    screenshot(pdf, "domain-01-register-menu.jpg", 211, (330, 116, 102, 40))
    text(pdf, "현재 계정에서는 ‘등록하기’를 누르면 본인인증·카드등록 안내가 나옵니다.",
         375, size=10.2, bold=True)
    text(pdf, "계정 명의자가 먼저 완료해 주세요. 인증 이후의 신청 화면은 아직 확인하지 않았어요.",
         399, size=9.5, color=MUTED)

    step(pdf, 2, "구매 전, 공개 페이지에서 검색하기", 442)
    text(pdf, f'<link href="{URLS[1]}" color="#087E86">'
         "https://www.iwinv.kr/account/domain</link>", 475, size=10)
    screenshot(pdf, "domain-02-public-search.jpg", 505, (1115, 203, 159, 69))
    text(pdf, "검색창에는 atmusic만 입력하고 ‘검색’을 눌러 주세요.",
         662, size=11, bold=True, color=TEAL)
    text(pdf, "이 공개 검색창에는 .co.kr을 붙이지 않습니다.<br/>"
         "결과 목록에서 정확히 atmusic.co.kr을 확인해 주세요.",
         689, size=10, leading=16)
    text(pdf, "이 안내서 작성 중에는 도메인을 선택하거나 신청·구매하지 않았어요.<br/>"
         "다음 입력 단계(소유자·연락처·주소 연결 설정)는 공식 안내 기준이며 실제 화면은 미확인입니다.",
         742, size=8, color=MUTED, leading=12)
    footer(pdf, 1)
    pdf.showPage()

    text(pdf, "AT.M / 도메인 선택과 결제 전 확인", 31, size=9, bold=True, color=TEAL)
    text(pdf, "이 주소와 금액을 확인해 주세요", 58, size=22, bold=True, leading=30)
    text(pdf, "여러 결과 중 atmusic.co.kr 한 개만 확인해 주세요.",
         102, size=10.5, color=MUTED)
    screenshot(pdf, "domain-03-search-result.jpg", 132, (87, 148, 1201, 68))

    pdf.setFillColor(colors.HexColor("#EAF5F3"))
    pdf.rect(LEFT, PAGE_H - 422, WIDTH, 67, fill=1, stroke=0)
    text(pdf, "1년 예상 비용 18,700원", 363, LEFT + 12, WIDTH - 24,
         16, True, TEAL, 22)
    text(pdf, "17,000원 + 부가세 1,700원  ·  월 요금이 아닌 1년 선불 비용", 392,
         LEFT + 12, WIDTH - 24, 9.5)
    text(pdf, "2026.10.10 공개 검색에서 선택 가능한 상태였으며, 신규·갱신 공개 요금은 같았어요.<br/>"
         "주소가 예약된 것은 아닙니다. 구매 시 등록 가능 여부·기간·부가세 포함 최종 금액을 다시 확인해 주세요.",
         434, size=8.7, color=MUTED, leading=13)

    text(pdf, "결제 전 체크리스트", 477, size=13, bold=True)
    text(pdf, "명의·연락처", 509, size=10, bold=True)
    text(pdf, "실제 운영할 클라이언트 명의와 수신 가능한 이메일·전화번호를 입력해요.",
         509, LEFT + 85, WIDTH - 85, 9.5, leading=14)
    text(pdf, "기간·총액", 540, size=10, bold=True)
    text(pdf, "atmusic.co.kr / 1년 / 부가세 포함 총액을 확인해요.",
         540, LEFT + 85, WIDTH - 85, 9.5, leading=14)
    text(pdf, "갱신·만료", 571, size=10, bold=True)
    text(pdf, "자동 갱신 적용 여부·갱신 비용·만료 알림 연락처·결제카드를 확인해요.<br/>"
         "현재 계정의 실제 설정은 아직 확인하지 않았어요.",
         571, LEFT + 85, WIDTH - 85, 9.5, leading=14)
    text(pdf, "최종 신청", 616, size=10, bold=True, color=HIGHLIGHT)
    text(pdf, "공식 안내상 마지막 ‘신청하기’는 등록과 선불 결제를 함께 진행해요.<br/>"
         "입력 정보·주소 연결 설정(네임서버)·약관을 확인한 뒤 직접 눌러 주세요.",
         616, LEFT + 85, WIDTH - 85, 9.5, leading=14)

    pdf.setFillColor(colors.HexColor("#F1F4F6"))
    pdf.rect(LEFT, PAGE_H - 726, WIDTH, 57, fill=1, stroke=0)
    text(pdf, "구매 완료 후 담당자에게 ‘완료’만 알려 주세요.", 678,
         LEFT + 12, WIDTH - 24, 11, True, TEAL)
    text(pdf, "서버 연결은 이후 함께 진행해요. 비밀번호·인증번호·카드정보는 보내지 마세요.",
         702, LEFT + 12, WIDTH - 24, 9.1)
    text(pdf, f'공식 절차(클릭): <link href="{URLS[2]}" color="#087E86">등록 안내</link>'
         f' / <link href="{URLS[3]}" color="#087E86">기간 연장 안내</link>'
         "  ·  화면·가격은 2026.10.10 확인 기준입니다.",
         746, size=8.2, color=MUTED, leading=12)
    footer(pdf, 2)
    pdf.save()


def validate() -> dict:
    reader = PdfReader(PDF)
    if len(reader.pages) != 2:
        raise ValueError("Expected exactly two pages")
    all_text = "\n".join(page.extract_text() for page in reader.pages)
    required = ["atmusic.co.kr", "도메인", "등록하기", "본인인증", "카드등록",
                "부가세", "갱신", "만료", "담당자"]
    if any(word not in all_text for word in required) or "\ufffd" in all_text:
        raise ValueError("Korean text validation failed")
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
    embedded_families = {name.split("+", 1)[-1] for name in embedded}
    if sorted(links) != sorted(URLS) or embedded_families != {
        "MalgunGothic", "MalgunGothicBold"
    }:
        raise ValueError("Links or embedded Korean fonts failed")
    return {
        "pdf": str(PDF), "page_count": len(reader.pages),
        "size_bytes": PDF.stat().st_size, "sha256": sha256(PDF),
        "embedded_fonts": sorted(embedded), "embedded_families": sorted(embedded_families),
        "links": links,
        "replacement_characters": all_text.count("\ufffd"),
        "prior_artifact_sha256": verify_preservation(),
        "screenshot_sha256": {
            name: sha256(ROOT / "screenshots" / name) for name in CAPTURES
        },
        "visual_review": "required after rendering; not an automated claim",
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--font-dir", type=Path, default=Path("C:/Windows/Fonts"))
    parser.add_argument("--render-with", type=Path)
    args = parser.parse_args()
    verify_preservation()
    before = {name: sha256(ROOT / "screenshots" / name) for name in CAPTURES}
    build(args.font_dir)
    if before != {name: sha256(ROOT / "screenshots" / name) for name in CAPTURES}:
        raise ValueError("Source screenshots changed")
    report = validate()
    QA.mkdir(exist_ok=True)
    if args.render_with:
        subprocess.run([str(args.render_with), "-r", "144", "-png", str(PDF),
                        str(QA / "domain-guide")], check=True)
        report["render_tool"] = str(args.render_with)
        report["render_dpi"] = 144
        report["renders"] = [str(QA / f"domain-guide-{n}.png") for n in (1, 2)]
    (QA / "domain-validation.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(report, ensure_ascii=True, indent=2))


if __name__ == "__main__":
    main()
