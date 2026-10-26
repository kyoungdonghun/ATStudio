---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: docops
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261003-ATS-002.md
    reason: Approved client artifact request
  - path: ../agent/WI-20261003-ATS-007-evidence-pack.md
    reason: Reproducible artifact checks
---

# WI-20261003-ATS-007 Summary

> Purpose: Deliver the approved easy Korean client server-cost brief.

## Result

[Client PDF](../../output/pdf/ATStudio-server-purchase-guide.pdf) is one A4 page with embedded Korean fonts, VAT-inclusive monthly costs, an illustrative traffic calculation, extra-cost warnings and signup-only next steps. It requests no passwords, verification codes, immediate server creation or payment.

Monthly server cost is KRW 53,020; average backup storage of 50-100 GB adds KRW 1,100-2,200. The recurring total is KRW 54,120-55,220, not a bill ceiling or one-time fee.

## Verification

On 2026-10-03, docops verified page size/count, 21 text expectations, three exact clickable URLs, font embedding, glyph coverage and arithmetic. The [PNG render](../../tmp/pdfs/ats-purchase-brief/ATStudio-server-purchase-guide.png) was visually inspected with no clipping, missing glyphs or overlap. MA subsequently reported independent clean-layout, Korean readability, page-count and link checks.

Provider pricing comes from MA's same-date supplied official-source verification. No purchase, external communication, deployment or hardware performance test was performed. Final plan/traffic terms still require confirmation before payment. No other documents or application files were edited.

## Related Documents

- [Approved REQ](REQ-20261003-ATS-002.md)
- [Evidence Pack](../agent/WI-20261003-ATS-007-evidence-pack.md)
