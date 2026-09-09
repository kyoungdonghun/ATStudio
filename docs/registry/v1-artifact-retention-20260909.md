---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: docops
category: registry
status: stable
dependencies:
  - path: ../policies/archive-policy.md
    reason: Historical claims, local archive boundaries, and exact manifest gates
  - path: ../../deliverables/user/REQ-20260909-ATS-003.md
    reason: Approved exact-scope artifact cleanup
  - path: ../../deliverables/user/REQ-20260909-ATS-004.md
    reason: Approved semantic history recovery and privacy-reviewed derivatives
  - path: development-history-recovery-20260909.md
    reason: Per-document content review and original-to-public hash mapping
---

# V1 Artifact Retention Register

> Purpose: Resolve all 205 original artifact identities and distinguish the original cleanup disposition from the privacy-reviewed history recovery.

## Scope And Availability

Archive ID: `v1-artifacts-20260909-084405`. Original set: **205 paths** from
source checkpoint `915dbd2e34efd212df931c563ded3c9c80148d95`.

The original WI016 classification retained 13 documents unchanged and archived
192 paths. Its dated removal receipt records 192 source removals and all 205
source/restored prehashes verified. This was a mechanical dependency-closure
selection, not a semantic determination that the other 83 documents lacked value.

**WI016/WI017 cleanup is closed**, not still awaiting handoff review. The
[WI016 MA delivery closeout](../../deliverables/agent/WI-20260909-ATS-016-evidence-pack.md#ma-delivery-closeout)
and [WI017 subsequent MA delivery receipt](../../deliverables/agent/WI-20260909-ATS-017-evidence-pack.md#subsequent-ma-delivery-receipt)
record independent review, final source-only documentation validation and actual
publication of cleanup commit `1904805841fcb9b863bf14ca2143ad3f840a9c87`,
matched to `origin/main`. That historical publication is **not** publication
evidence for this new WI018 recovery.

WI018 reviewed the semantic contents of **all 96 original documents**, including
the 83 previously local-only documents. All 96 contain useful decision,
approval, failure, verification or handoff history. The 13 previously retained
documents remain byte-identical; 83 privacy-reviewed public derivatives are now
present at their original repository paths. Their dates, language, decisions and
reported claims remain historical. **The new recovery is a working-tree
candidate; WI020 review, MA integration and verified Git publication are separate.**

| Original group | Original | Previous RETAIN_GIT | Previous ARCHIVE_LOCAL | Current RETAIN_GIT | Current RESTORED_DERIVATIVE | Current ARCHIVE_LOCAL |
|---|---:|---:|---:|---:|---:|---:|
| Agent historical documents | 62 | 9 | 53 | 9 | 53 | 0 |
| User historical documents | 34 | 4 | 30 | 4 | 30 | 0 |
| Security / remediation / runtime outputs | 80 | 0 | 80 | 0 | 0 | 80 |
| Screenshots | 27 | 0 | 27 | 0 | 0 | 27 |
| Screenshot ZIP | 1 | 0 | 1 | 0 | 0 | 1 |
| Manual SQL patch | 1 | 0 | 1 | 0 | 0 | 1 |
| **Total** | **205** | **13** | **192** | **13** | **83** | **109** |

- **RETAIN_GIT**: The 13 previously selected historical documents remain at
  their original paths with unchanged bytes.
- **RESTORED_DERIVATIVE**: A public, privacy-reviewed historical copy at the
  original path. Its bytes differ from the original due to a provenance notice
  and, for 10 documents, narrow identifier/path sanitization. Two other
  derivatives also remove one redundant final LF under the documented E1
  normalization. This does not
  replace, alter or reclassify the private original as public. The original
  SHA-256 below must not be used as the derivative's checksum.
- **ARCHIVE_LOCAL**: The 109 raw outputs, screenshots, ZIP and SQL stay private
  and outside the source checkout. Their bodies were not part of the 96-document
  semantic review. No raw artifact was republished or executed.
- The original private `manifest.json` and `disposition.json` remain unchanged.
  Previous/current columns below are additive public accounting, not rewritten
  original receipts. Original anchors A001-A205 remain stable.
- Historical code-formatted paths and filenames remain lookup keys, not
  promises of clone availability. Raw originals in this population resolve
  through A097-A205; other private/generated pointers may be outside this archive.
- Current instructions remain the [Documentation Index](../index.md), applicable
  design/policy/runbook documents and approved current REQ/WI. Historical
  handoffs are not fresh execution, DB, cleanup or production authority.

## Recovery

The custodian keeps `workspace-artifacts.zip`, `manifest.json`,
`verification.json` and `disposition.json` outside the repository under
`%LOCALAPPDATA%/ATStudio/archives/v1-artifacts-20260909-084405/`.
They are not shipped with a source checkout. MA's new task receipt is
`%LOCALAPPDATA%/ATStudio/archives/development-recovery-20260909-094234/baseline.json`.

| MA-supplied original preservation identity | SHA-256 |
|---|---|
| Original ZIP | `486744B423DDDDD4DAFDB96224F5612EBCB858DF2FB128417AC4CFD5C52E4163` |
| Original manifest | `C3D1F9D90DC36D440AC0659E4ED29C26592457457D5EC478782CD9E452F7EE28` |

MA supplied a matching 205-member restore check, unchanged 955 protected and 79
local files, three unchanged process start identities and four HTTP 200s.
These are MA preservation receipts, not WI018-executed runtime or DB tests.
WI018 did not repeat the full original hash walk. Its own 96-document review and
public-file identity checks are recorded in the
[development history recovery register](development-history-recovery-20260909.md).

If the private archive is lost, the source candidate still contains all 96
public historical documents, but not the original private bytes or raw 109.
Remote availability requires MA's later verified Git delivery. An off-device
private backup destination is unanswered; **no off-device backup or actual
machine/DB/media recovery is claimed**.

For separately authorized original recovery, verify the ZIP against the saved
receipt, extract only requested manifest members to a separate private directory,
and compare their SHA-256 with the original manifest and this table. Do not
overwrite source/runtime/media, execute archived SQL or expose private content.
Public derivatives have their own checksums in the recovery register.

## Original Path Register

Entry IDs follow the original manifest order. SHA-256 identifies **original
bytes only**. Document links now open retained originals or explicit public
derivatives; raw paths remain search keys. Per-document transformation,
semantic rationale and public SHA-256: [recovery register](development-history-recovery-20260909.md#per-document-review-and-provenance).

| Entry | Original repository-relative path | Original SHA-256 | Previous WI016 disposition | Current WI018 disposition |
|---|---|---|---|---|
| <a id="a001"></a>A001 | [deliverables/agent/WI-20260809-ATS-068-decision-register.md](../../deliverables/agent/WI-20260809-ATS-068-decision-register.md) | `F19E8A22C2D84160A3BEC6323E33534E40F5D681FF6B4D122E0177D8EB204B0D` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a002"></a>A002 | [deliverables/agent/WI-20260809-ATS-068-evidence-pack.md](../../deliverables/agent/WI-20260809-ATS-068-evidence-pack.md) | `E028E23A5552D71DC4692B8B39A322C58E5303459990006592F616928270F521` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a003"></a>A003 | [deliverables/agent/WI-20260809-ATS-068-final-evidence-audit.md](../../deliverables/agent/WI-20260809-ATS-068-final-evidence-audit.md) | `7846FC7EAABCF1488BA90FD0A08563CCD1D10019233E47CCF7929F399B088C79` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a004"></a>A004 | [deliverables/agent/WI-20260809-ATS-068-handoff.md](../../deliverables/agent/WI-20260809-ATS-068-handoff.md) | `31E0D7B021B7B3C9E1B2F9AD568CD4DB28F425D068785D3BB804D18447D43E62` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a005"></a>A005 | [deliverables/agent/WI-20260809-ATS-068-qa-integ-r2-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-r2-review.md) | `F4363244D0F18FC25FE341ECF38F7EBB0CD14C572E4482F5BBF7FA0BA7CC9CAD` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a006"></a>A006 | [deliverables/agent/WI-20260809-ATS-068-qa-integ-r3-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-r3-review.md) | `5FE3A90DB863900C7C07643F6F2868B0A39E0BDD7FCA27D2E861A74A47F219C6` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a007"></a>A007 | [deliverables/agent/WI-20260809-ATS-068-qa-integ-r4-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-r4-review.md) | `05A34115829318B619846D23604123FC9B7559B1A6C7DCCD09314B16CA98FA67` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a008"></a>A008 | [deliverables/agent/WI-20260809-ATS-068-qa-integ-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-review.md) | `F7038DA65AD6F08D56C4E3E400F6CD83227DD39783C579BDD6070E5551412576` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a009"></a>A009 | [deliverables/agent/WI-20260816-ATS-001-evidence-pack.md](../../deliverables/agent/WI-20260816-ATS-001-evidence-pack.md) | `34F4A80863B19F316080E5E164E9CF38143275A0ABE8DC610CB1F49A5B3860AB` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a010"></a>A010 | [deliverables/agent/WI-20260816-ATS-001-handoff.md](../../deliverables/agent/WI-20260816-ATS-001-handoff.md) | `19DECDDD4BB1F34041EBA454E3972626EF9E4A4D55953456E2AC4B1278993167` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a011"></a>A011 | [deliverables/agent/WI-20260816-ATS-002-evidence-pack.md](../../deliverables/agent/WI-20260816-ATS-002-evidence-pack.md) | `389B26DADE6561A23888C508E4BF96C29E9AF2C6EC30CA2302FBE564B146A16D` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a012"></a>A012 | [deliverables/agent/WI-20260816-ATS-002-handoff.md](../../deliverables/agent/WI-20260816-ATS-002-handoff.md) | `02B6D9E3AF7C02D815778F2083D721777E483BD6706C0C4DBEFDE806834D264D` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a013"></a>A013 | [deliverables/agent/WI-20260817-ATS-001-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-001-evidence-pack.md) | `0885E296C1982FB171FCFD4EB7C9A792F1781D927A729D72853CAC3B903938D0` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a014"></a>A014 | [deliverables/agent/WI-20260817-ATS-001-handoff.md](../../deliverables/agent/WI-20260817-ATS-001-handoff.md) | `4FAF19431BCFA3F118EC7CA5091910F50380105A41203A7BE2E1F61A2A705A2F` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a015"></a>A015 | [deliverables/agent/WI-20260817-ATS-002-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-002-evidence-pack.md) | `C6200765A08ACE3526E5C38A66572AA310EAD7363D141F0804F079A9E7F7FCA4` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a016"></a>A016 | [deliverables/agent/WI-20260817-ATS-002-handoff.md](../../deliverables/agent/WI-20260817-ATS-002-handoff.md) | `29C09C27BABE2FCDC03578962DD3478381E6784BBA9AB3C15C09C1B17192AD3D` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a017"></a>A017 | [deliverables/agent/WI-20260817-ATS-003-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-003-evidence-pack.md) | `020CCE7EFD25664C594F6296A7EBF6E38BB928791762C68E9F7ADB92480A2B8B` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a018"></a>A018 | [deliverables/agent/WI-20260817-ATS-003-handoff.md](../../deliverables/agent/WI-20260817-ATS-003-handoff.md) | `94F2BFAA968FDBBA31171870F4377A1122D3AB499CDF88FFD278E0F69E0181A0` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a019"></a>A019 | [deliverables/agent/WI-20260817-ATS-004-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-004-evidence-pack.md) | `BB72DCC8E4179BB5D68C5B693A730FF76AF11E6CC2662D7428F847370F3CB51C` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a020"></a>A020 | [deliverables/agent/WI-20260817-ATS-004-handoff.md](../../deliverables/agent/WI-20260817-ATS-004-handoff.md) | `E05343E804E0CF96B0FCBCFB3A11DF2D0C15CEA3D27753126515038821E49397` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a021"></a>A021 | [deliverables/agent/WI-20260817-ATS-005-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-005-evidence-pack.md) | `4635B6722679AF5D97D06AE4988A9B85122B4DE85ABEB04F513C32F650772116` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a022"></a>A022 | [deliverables/agent/WI-20260817-ATS-005-handoff.md](../../deliverables/agent/WI-20260817-ATS-005-handoff.md) | `866FB1990DB0E184E341B6DAFAFCC6F764C5025EAA752B50431AE2E7105AFF3C` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a023"></a>A023 | [deliverables/agent/WI-20260817-ATS-007-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-007-evidence-pack.md) | `A4664FBA3CECA5514A12A4FCD30BD2D19C6851E3A2A6E3656A40BCD2EBB062E3` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a024"></a>A024 | [deliverables/agent/WI-20260817-ATS-007-handoff.md](../../deliverables/agent/WI-20260817-ATS-007-handoff.md) | `A1FACCB11C88701310C7730278F3A4D539F1ACDA947CB7767AFB9599492FD8D2` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a025"></a>A025 | [deliverables/agent/WI-20260817-ATS-008-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-008-evidence-pack.md) | `95D025A1BF66361F009DE8D64B53766AA13C5DAB47DDAB0D7854413DFB05DC28` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a026"></a>A026 | [deliverables/agent/WI-20260817-ATS-008-handoff.md](../../deliverables/agent/WI-20260817-ATS-008-handoff.md) | `F123C226784769614358B631CC6CF6101CBDFAF168FE7B219BFEA18090994939` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a027"></a>A027 | [deliverables/agent/WI-20260817-ATS-009-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-009-evidence-pack.md) | `D5D5D69CF50B448720F0084C32746AD7BE2646C177170E6C855B39C165B26688` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a028"></a>A028 | [deliverables/agent/WI-20260817-ATS-009-handoff.md](../../deliverables/agent/WI-20260817-ATS-009-handoff.md) | `906DFDBB958E26624607615B04252DCE5CA1C979597DCC999C7F46757603651D` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a029"></a>A029 | [deliverables/agent/WI-20260817-ATS-010-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-010-evidence-pack.md) | `29356A8690BB9177B3D6AEB05D7F1C5D6A0406A79CE15156C6BB5E200058D2BF` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a030"></a>A030 | [deliverables/agent/WI-20260817-ATS-010-handoff.md](../../deliverables/agent/WI-20260817-ATS-010-handoff.md) | `7F492B3C3788E04FB80F3C8C0D1ED79F84B4BD48BD7A3B6D343491C80D899B6B` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a031"></a>A031 | [deliverables/agent/WI-20260817-ATS-011-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-011-evidence-pack.md) | `D12B566DFD8ECB564A434A251186A62750FA22BDA03A997084E686E348A704FE` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a032"></a>A032 | [deliverables/agent/WI-20260817-ATS-011-handoff.md](../../deliverables/agent/WI-20260817-ATS-011-handoff.md) | `C3DEF1B0932D8ADE55F0BC87F7708DBD34EE6DDEE003803A4262ABD9940464E0` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a033"></a>A033 | [deliverables/agent/WI-20260817-ATS-012-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-012-evidence-pack.md) | `82036B0DC5F4DD2038BFA5EEAE8347B36820812096C25190CA865FAEEC0F04AF` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a034"></a>A034 | [deliverables/agent/WI-20260817-ATS-012-handoff.md](../../deliverables/agent/WI-20260817-ATS-012-handoff.md) | `5892A100C5C756C0FB3F4B4F6C2D429806BB6858773BF990E6109153BA18435F` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a035"></a>A035 | [deliverables/agent/WI-20260817-ATS-013-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-013-evidence-pack.md) | `19CFACD8E08ADA3959ABD237FB05B2263BEA6DFCB0E7C0A6E48B731C405DEDD8` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a036"></a>A036 | [deliverables/agent/WI-20260817-ATS-013-handoff.md](../../deliverables/agent/WI-20260817-ATS-013-handoff.md) | `52F63785CB0F1FA5BE6CEAAB411A89D94BA966CB0D4FAB55B4DE74922F4BBD9C` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a037"></a>A037 | [deliverables/agent/WI-20260817-ATS-014-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-014-evidence-pack.md) | `C71E7ACD5B5981B4B7F1F7625CAEADC44AE2A20857E45529D190D593D7BA2F71` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a038"></a>A038 | [deliverables/agent/WI-20260817-ATS-014-handoff.md](../../deliverables/agent/WI-20260817-ATS-014-handoff.md) | `7BF0F510A9843F74FAA05BD5E2465103744A9E4FF1D50AEF064EF751937B9DBF` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a039"></a>A039 | [deliverables/agent/WI-20260817-ATS-015-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-015-evidence-pack.md) | `B36A50F7B2BD53BED71EC8E5D39092AF621CC824DF7753C7597EFA88B3C021CE` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a040"></a>A040 | [deliverables/agent/WI-20260817-ATS-015-handoff.md](../../deliverables/agent/WI-20260817-ATS-015-handoff.md) | `973CB07D4D9CD01A81397FF1A490DB07AC58615E3A61B63B1145C4B704A93A56` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a041"></a>A041 | [deliverables/agent/WI-20260817-ATS-017-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-017-evidence-pack.md) | `81B6A4DA704CFD1BC81C2089893C335680096E94B027045D32E589BDEE718AEF` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a042"></a>A042 | [deliverables/agent/WI-20260817-ATS-017-handoff.md](../../deliverables/agent/WI-20260817-ATS-017-handoff.md) | `92A54626F889CFB47EA4925AB0D43F2AF00DE26259D4BF57AEA5B29F1D445AAA` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a043"></a>A043 | [deliverables/agent/WI-20260817-ATS-018-handoff.md](../../deliverables/agent/WI-20260817-ATS-018-handoff.md) | `44374FD46CE88ED10482A794C1CEC6EE09E997722B53F9EE3AB1959FB7E95AA2` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a044"></a>A044 | [deliverables/agent/WI-20260817-ATS-019-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-019-evidence-pack.md) | `D19EEE08E3C12A2854507C4DB33433546D07CDDFB08CA5B9B4C252FEAD34B4C7` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a045"></a>A045 | [deliverables/agent/WI-20260817-ATS-019-handoff.md](../../deliverables/agent/WI-20260817-ATS-019-handoff.md) | `CEAAD86ED51F5F617B82E620C8CC9101246663AA541128FC6D876C486B9F8D18` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a046"></a>A046 | [deliverables/agent/WI-20260817-ATS-021-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-021-evidence-pack.md) | `9DC8E504E1CA308E3AFA57D6ACBBC1B1A9813CAC085D059133799AB126E1D087` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a047"></a>A047 | [deliverables/agent/WI-20260817-ATS-021-handoff.md](../../deliverables/agent/WI-20260817-ATS-021-handoff.md) | `83EB1106D9879E97D39EBFC9F90756AAEC13962EAED42E98C9BB982010F2B4DE` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a048"></a>A048 | [deliverables/agent/WI-20260817-ATS-022-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-022-evidence-pack.md) | `E45673B0ED77213B7924C46A03D80D4E2113BF0FD6393DE89D16D3C6921879C5` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a049"></a>A049 | [deliverables/agent/WI-20260817-ATS-022-handoff.md](../../deliverables/agent/WI-20260817-ATS-022-handoff.md) | `3B427D3DB53B9F6711E15161698FB624EADCD3B58B58622516AD38D80BAA93EB` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a050"></a>A050 | [deliverables/agent/WI-20260817-ATS-023-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-023-evidence-pack.md) | `6A43174C4BA46A3E253C2ECB3E50EA0BFF9C9E52DA6AA027E9B8B69D7D767A08` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a051"></a>A051 | [deliverables/agent/WI-20260817-ATS-023-handoff.md](../../deliverables/agent/WI-20260817-ATS-023-handoff.md) | `E2B34E1CAA628216621BD14D80A337A09A795B310EDC8406AB93592D148FFD86` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a052"></a>A052 | [deliverables/agent/WI-20260817-ATS-027-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-027-evidence-pack.md) | `227CD5C5A2A90836DF11EDE94B41670820F72F72C2CE1B756A75D6FBD65AC00C` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a053"></a>A053 | [deliverables/agent/WI-20260817-ATS-027-handoff.md](../../deliverables/agent/WI-20260817-ATS-027-handoff.md) | `A441DFE629224052B92DAF79A2C31142B82FED7D419176E67F9BAD0D8D0856DE` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a054"></a>A054 | [deliverables/agent/WI-20260817-ATS-028-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-028-evidence-pack.md) | `BE232AB7EF41F824FB653D0E81C39FD3D953B5ED60424DF294FB03C41FE0E4C3` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a055"></a>A055 | [deliverables/agent/WI-20260817-ATS-028-handoff.md](../../deliverables/agent/WI-20260817-ATS-028-handoff.md) | `381053F4C55C994F247A6D26BA8D809F40E8EF05A529F74B363D7F8A801F4374` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a056"></a>A056 | [deliverables/agent/WI-20260817-ATS-029-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-029-evidence-pack.md) | `85D3DFE338BF2EAC8308414B4D767E0E2F16F71CAB64FD55DDA58A94B54E9826` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a057"></a>A057 | [deliverables/agent/WI-20260817-ATS-029-handoff.md](../../deliverables/agent/WI-20260817-ATS-029-handoff.md) | `BE9E0E6B3E93540BC6123F517D5D0E13715408EEB3C9E74B87DBBBAC7FAC2373` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a058"></a>A058 | [deliverables/agent/WI-20260817-ATS-030-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-030-evidence-pack.md) | `FCADF61BC63EF462874B2A4326BF4F0EA16B267D97DA6DF90BB765BFF19DA2C6` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a059"></a>A059 | [deliverables/agent/WI-20260817-ATS-030-handoff.md](../../deliverables/agent/WI-20260817-ATS-030-handoff.md) | `B3DF56143880A7500700FBA405A677CDE5103F94BCDB0A1604779AD679ED4B97` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a060"></a>A060 | [deliverables/agent/WI-20260817-ATS-031-handoff.md](../../deliverables/agent/WI-20260817-ATS-031-handoff.md) | `DF5132D5CF751DA14A54D099B199749267BC864815939E97F78071A8B21C8FC0` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a061"></a>A061 | [deliverables/agent/WI-20260817-ATS-032-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-032-evidence-pack.md) | `93D113CC61142A1BDB52A55FC894B92999C5C8101B5C14FEA3FF834FE2F4640A` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a062"></a>A062 | [deliverables/agent/WI-20260817-ATS-032-handoff.md](../../deliverables/agent/WI-20260817-ATS-032-handoff.md) | `E650D9D31ABA7869573F823F9C289CE2310B5EB5EEE69944AEB29DA80B3FA3C3` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a063"></a>A063 | [deliverables/user/REQ-20260816-ATS-001.md](../../deliverables/user/REQ-20260816-ATS-001.md) | `F440478D25A3D3B731D2449DF91D5EB2C4EE4BD3E38A32B80ADCF42AA90A7B9F` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a064"></a>A064 | [deliverables/user/REQ-20260817-ATS-001.md](../../deliverables/user/REQ-20260817-ATS-001.md) | `B483116299DDDF4CEE62006572DFDA27EE7F076BA24BA8754FC5BC2FA73440EF` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a065"></a>A065 | [deliverables/user/REQ-20260817-ATS-002.md](../../deliverables/user/REQ-20260817-ATS-002.md) | `47812491BD7577EC1A136C3EC0612478CED2C0D663859861258AD52B983D3BA6` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a066"></a>A066 | [deliverables/user/REQ-20260817-ATS-003.md](../../deliverables/user/REQ-20260817-ATS-003.md) | `04A3B62F47DB41BAA695FE3E11C193A83044137B273625D6F5618096732976F1` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a067"></a>A067 | [deliverables/user/REQ-20260817-ATS-004.md](../../deliverables/user/REQ-20260817-ATS-004.md) | `7E1F38C6DAD8C9E6CF942FDCB786B1E77730EBAF6A6227505ABE0755FDA7B670` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a068"></a>A068 | [deliverables/user/REQ-20260817-ATS-005.md](../../deliverables/user/REQ-20260817-ATS-005.md) | `2DF00EFC3E4B496576A5C357D93B86DABAF17A2FAFC6F2C540B4535B1E21480C` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a069"></a>A069 | [deliverables/user/REQ-20260817-ATS-006.md](../../deliverables/user/REQ-20260817-ATS-006.md) | `84E5574DEC21FDDC386BA044889D5B69FA3D882BC53B9955333C08029560D66B` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a070"></a>A070 | [deliverables/user/REQ-20260817-ATS-007.md](../../deliverables/user/REQ-20260817-ATS-007.md) | `9042BA266D9B796E1BA03299EACCAFF9681B664EED73EFC83F05ED3306594417` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a071"></a>A071 | [deliverables/user/REQ-20260817-ATS-008.md](../../deliverables/user/REQ-20260817-ATS-008.md) | `B4944D6D9958542EED2503747BF68D741632DC8C9AAB62335AF46F1912922416` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a072"></a>A072 | [deliverables/user/REQ-20260817-ATS-011.md](../../deliverables/user/REQ-20260817-ATS-011.md) | `EBABEB6EF4CB786D6A5ABFDCDA53DFA8FB1C2B27AAA6634CE4BBD67D45F16487` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a073"></a>A073 | [deliverables/user/WI-20260809-ATS-068-summary.md](../../deliverables/user/WI-20260809-ATS-068-summary.md) | `A8764AF2D484157ECB7A0B605E56A23EC4B82E8DF4C71E5FD5137CB26140399A` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a074"></a>A074 | [deliverables/user/WI-20260816-ATS-001-summary.md](../../deliverables/user/WI-20260816-ATS-001-summary.md) | `6264CA74C66679A7648910D6C86C31785272E6E7654A4442ED17D16374061B93` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a075"></a>A075 | [deliverables/user/WI-20260816-ATS-002-summary.md](../../deliverables/user/WI-20260816-ATS-002-summary.md) | `76BCA57D5854D5EE11D022AC1E3884E0A640A4D7586B730E318C64C2D62CDA79` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a076"></a>A076 | [deliverables/user/WI-20260817-ATS-001-summary.md](../../deliverables/user/WI-20260817-ATS-001-summary.md) | `C3153DDE5205A9EA3DA443D7734E2A8868C51C0ACC4209FFB237FF552BC607A5` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a077"></a>A077 | [deliverables/user/WI-20260817-ATS-002-summary.md](../../deliverables/user/WI-20260817-ATS-002-summary.md) | `541667BD029DCD306D4756304E2F86FBBA0EBB949B9ABBBCA37258D12504BE87` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a078"></a>A078 | [deliverables/user/WI-20260817-ATS-003-summary.md](../../deliverables/user/WI-20260817-ATS-003-summary.md) | `A2F5DAE1A838D3EF549CA05EC96623655E1B09D9A91DFE6351B190814C356FE0` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a079"></a>A079 | [deliverables/user/WI-20260817-ATS-004-summary.md](../../deliverables/user/WI-20260817-ATS-004-summary.md) | `82224DF29FB4634505C7B75D8204A23CE32536D91B45FF2AB7B686597AE528AB` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a080"></a>A080 | [deliverables/user/WI-20260817-ATS-005-summary.md](../../deliverables/user/WI-20260817-ATS-005-summary.md) | `EF3AE74132872CFE73D03D1938D2EAB755C389E5EBEC70C2E19AF7BEB4A9FF98` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a081"></a>A081 | [deliverables/user/WI-20260817-ATS-007-summary.md](../../deliverables/user/WI-20260817-ATS-007-summary.md) | `D4CD78F8E6DAD1513C55A5100168EA62D3E0A3DB41A2DA4B9F9D8126E1BAD6D9` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a082"></a>A082 | [deliverables/user/WI-20260817-ATS-008-summary.md](../../deliverables/user/WI-20260817-ATS-008-summary.md) | `A369E27099E23BE914BD4D9D79F08CBED68ED8443205820BD0229BFDD2932647` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a083"></a>A083 | [deliverables/user/WI-20260817-ATS-009-summary.md](../../deliverables/user/WI-20260817-ATS-009-summary.md) | `8FA24269561610D1AC9D923D2F5B62B2B8DF2439EF40E0B55B8A72D254DA8E65` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a084"></a>A084 | [deliverables/user/WI-20260817-ATS-010-summary.md](../../deliverables/user/WI-20260817-ATS-010-summary.md) | `8E6170FAFD61E77574EFE6C955BBB74742BE278147583B1A1619777DFB982950` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a085"></a>A085 | [deliverables/user/WI-20260817-ATS-011-summary.md](../../deliverables/user/WI-20260817-ATS-011-summary.md) | `CD1F8026D584DFBE66FB1C37F8B27AEEB6D15F398B4C20D6932CB4073E48D51E` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a086"></a>A086 | [deliverables/user/WI-20260817-ATS-012-summary.md](../../deliverables/user/WI-20260817-ATS-012-summary.md) | `9A66832D38AD93C61BC10D7D8BBBEF7E3B37D0BB2E13D62B98C797693EF012DE` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a087"></a>A087 | [deliverables/user/WI-20260817-ATS-013-summary.md](../../deliverables/user/WI-20260817-ATS-013-summary.md) | `0A6C0EFBF3B33EF4476D07D9EE947A075D13A1FEAE769733AD33066F2BF86A70` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a088"></a>A088 | [deliverables/user/WI-20260817-ATS-014-summary.md](../../deliverables/user/WI-20260817-ATS-014-summary.md) | `799603E796E4D0AC900FD25E004690AEC30D69DBEC73989D0B99AF3788DA54B5` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a089"></a>A089 | [deliverables/user/WI-20260817-ATS-015-summary.md](../../deliverables/user/WI-20260817-ATS-015-summary.md) | `560EDE320C6AAD5AAB52E985842EF105229F24A3B80A0BC387B04CD833549F9E` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a090"></a>A090 | [deliverables/user/WI-20260817-ATS-017-summary.md](../../deliverables/user/WI-20260817-ATS-017-summary.md) | `C3DF2593A1139DE0D70CC09911E224158EDF3160469648CD585BAA7D7085DFDA` | `RETAIN_GIT` | `RETAIN_GIT` |
| <a id="a091"></a>A091 | [deliverables/user/WI-20260817-ATS-019-summary.md](../../deliverables/user/WI-20260817-ATS-019-summary.md) | `5983C37068CC8E07658E878D9A9239DFBAACE281725CFAC6CA99047C48FBDEEE` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a092"></a>A092 | [deliverables/user/WI-20260817-ATS-023-summary.md](../../deliverables/user/WI-20260817-ATS-023-summary.md) | `8BAF2860A06722AC1B92B0B3B88ABCD7030016364558ED9C77974ED53218C7C4` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a093"></a>A093 | [deliverables/user/WI-20260817-ATS-027-summary.md](../../deliverables/user/WI-20260817-ATS-027-summary.md) | `51D5B143B3FF2E8AE8ABF457F988A86710277632AC725D5D3556141A134580DF` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a094"></a>A094 | [deliverables/user/WI-20260817-ATS-028-summary.md](../../deliverables/user/WI-20260817-ATS-028-summary.md) | `3BD2B4E2A8B79FDC3A5B629340FBD3DC3F7E733A1F94BA1A7E924D85FC12DF8C` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a095"></a>A095 | [deliverables/user/WI-20260817-ATS-029-summary.md](../../deliverables/user/WI-20260817-ATS-029-summary.md) | `A1A5AFFB4F7BB2C2527F6914EDD01C2033890B280C36C3B324F9B4D5EC9ECC45` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a096"></a>A096 | [deliverables/user/WI-20260817-ATS-030-summary.md](../../deliverables/user/WI-20260817-ATS-030-summary.md) | `DBBADDA052B401CCFA54382CDCEC33884039E3F229F2A677B5A780E510DB9B84` | `ARCHIVE_LOCAL` | `RESTORED_DERIVATIVE` |
| <a id="a097"></a>A097 | `output/client-demo-screenshots-20260716-140514.zip` | `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a098"></a>A098 | `output/release-remediation-20260909/backend-coverage-final.json` | `80E00CBFCDC3815765E333D811F4AD3CCD73012790C78C41F1BD5232C29A2542` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a099"></a>A099 | `output/release-remediation-20260909/backend-full-final-results.json` | `AFD9898B6363AFA04C17320F83C72DCA603D93B72FF7B72542FF080BD5CE4797` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a100"></a>A100 | `output/release-remediation-20260909/backend-full-final.log` | `9CAE86406653CF78BABB158DEF42CB737FEFBE83C9171600A096B47BBACF400C` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a101"></a>A101 | `output/release-remediation-20260909/backend-full-first.log` | `63DA128547ED836FFC2985BEB178A299980FEF2D41F976EFA305D055A238BCE6` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a102"></a>A102 | `output/release-remediation-20260909/backend-full-second-results.json` | `E3D7AA4ECB82B770E1965B14421CF421D02D83134A67C139FEAE296AD32834D3` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a103"></a>A103 | `output/release-remediation-20260909/backend-full-second.log` | `097A59BB6DA15CEDBD4BBB67DA03A64A716DB704EB54F507A25F3E7F70746668` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a104"></a>A104 | `output/release-remediation-20260909/backend-multipart-focused-results.json` | `2ED801EC86B5DA256AEF4D1EDC2848CC885593F5CFA80F29EE8F3C6EFAF39737` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a105"></a>A105 | `output/release-remediation-20260909/backend-multipart-focused.log` | `BA155FDE1C90F9009EADA5AC19E8EE5F9620129E31D0E6AE0F1014AAC2226B74` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a106"></a>A106 | `output/release-remediation-20260909/backend-tested-source-manifest.json` | `9C2972820E1DE4AD3918AFC0C174691F5E26F87991D3D24B49FAFA911D1B785B` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a107"></a>A107 | `output/release-remediation-20260909/built-artifact-dependencies.json` | `BCB7ECEE4DFA1D7C8A6D6C951119B752FCC5F7C69956A5A66D45EE0FC3C2ECDD` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a108"></a>A108 | `output/release-remediation-20260909/command-exit-status.json` | `B19ECD456FAC5A03760EF1B195B793939FEA470633C413DE458BE7B869987C76` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a109"></a>A109 | `output/release-remediation-20260909/compile-initial.log` | `DFCE25C3CCF94B36CD7E1797E423E6AF7BA757011016EB65B2C8D65D8C0258BD` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a110"></a>A110 | `output/release-remediation-20260909/current-change-inventory.json` | `1D7584F3F82717B539CAAA9CD368535A0C61792A2C262EBAA438AA43AFBD29DB` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a111"></a>A111 | `output/release-remediation-20260909/docs-final.log` | `7E485C0916E080B6FC3D9B792BD8F72DF69262A9E67DC28A97158F3676CE0D9E` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a112"></a>A112 | `output/release-remediation-20260909/docs-initial.log` | `7E485C0916E080B6FC3D9B792BD8F72DF69262A9E67DC28A97158F3676CE0D9E` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a113"></a>A113 | `output/release-remediation-20260909/docs-postupdate.log` | `7E485C0916E080B6FC3D9B792BD8F72DF69262A9E67DC28A97158F3676CE0D9E` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a114"></a>A114 | `output/release-remediation-20260909/docs-preclosure.log` | `7E485C0916E080B6FC3D9B792BD8F72DF69262A9E67DC28A97158F3676CE0D9E` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a115"></a>A115 | `output/release-remediation-20260909/focused-initial-results.json` | `91999F00D3B1736D7DC9271A8EDFC60A3BEB1931D8C080264E708AD0232D963E` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a116"></a>A116 | `output/release-remediation-20260909/focused-initial.log` | `5AEDF4B0ED55FC43466AD4BB7B71A9EB07DF20D1144477E891214C468398CE89` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a117"></a>A117 | `output/release-remediation-20260909/frontend-audit-final.json` | `CF4E269EC69D2FD2786330B6392ACE191C6E21780FAFE635889F04DD5CF91A81` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a118"></a>A118 | `output/release-remediation-20260909/frontend-build.log` | `15D7E98A38CCC99726435BF8F37382257018C18BC5CF63340B6E49F4D030EA89` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a119"></a>A119 | `output/release-remediation-20260909/frontend-coverage-final.json` | `55AB39BBEC25BB9C3DE1056325981D6517D761C0B8D38BA2673908228E87B410` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a120"></a>A120 | `output/release-remediation-20260909/frontend-coverage-summary.json` | `5A975BB101070B69F609BC78C1B70DE616ECDE2CA908DE16D49E1DE07413CD1F` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a121"></a>A121 | `output/release-remediation-20260909/frontend-entrypoint-focused-final.log` | `BCFAD4500C737D4AA0551B4F5194BFF5726C6BC2570BF199A8C972812FCA048A` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a122"></a>A122 | `output/release-remediation-20260909/frontend-entrypoint-focused.log` | `F3416CEBF3E75F7C83ECBF9D5D85280287FA7E0E659B0582C1954059D156A452` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a123"></a>A123 | `output/release-remediation-20260909/frontend-final-build.log` | `D7A388222753CF70E69D09BE4A1B8AF27FCF8D0E8C053A24D9039F882DE68098` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a124"></a>A124 | `output/release-remediation-20260909/frontend-final-format.log` | `3FB734AC82663E5623D79AAE32855628B81FA1D15F9D42FC8B968DCCA368663E` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a125"></a>A125 | `output/release-remediation-20260909/frontend-final-lint.log` | `C4C47E50498BADC4D8ED21074828D2032D31995CBF84B8AE53E2089C5B77D526` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a126"></a>A126 | `output/release-remediation-20260909/frontend-final-snapshot.json` | `F182B3E3921F692BED260166C08BAE036BBD5792D47E1E860AA0D3490353C530` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a127"></a>A127 | `output/release-remediation-20260909/frontend-final-typecheck.log` | `60A6F6E3B322355690CEA80154C8347322E4D21C03FF61BE62162D17D3D2C0AA` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a128"></a>A128 | `output/release-remediation-20260909/frontend-focused-initial.log` | `8F8DE4B6C6CFCC803899EC96A22A8C0C2A4DAECD709E032F93BE048FE1A86BFC` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a129"></a>A129 | `output/release-remediation-20260909/frontend-format.log` | `80467C92D23C14AABDEC1E4782A7611C322FC22B12650EE6583EF86CE40941EE` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a130"></a>A130 | `output/release-remediation-20260909/frontend-full-coverage.log` | `F86E095A92CDAE72EB8D018B464661E29B90CC356D3A1ACF8EEDC63D3AF7D570` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a131"></a>A131 | `output/release-remediation-20260909/frontend-full-final-coverage.log` | `4AF0B43123719D34D0A41590B315D6AC50C45E285070B060E93CCA0395AA9F79` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a132"></a>A132 | `output/release-remediation-20260909/frontend-full-pre-r1-coverage-summary.json` | `D4745757D820A0ED0FAA237F4DB739B97FBA94FCA13E69C415EFB456F8D0DC1B` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a133"></a>A133 | `output/release-remediation-20260909/frontend-incomplete-snapshot-coverage-summary.json` | `1F8D729D4109C1DF63B22316EE7BE08C8AA9F7F8886BBDF8E34A5C7EA65933CA` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a134"></a>A134 | `output/release-remediation-20260909/frontend-incomplete-snapshot-coverage.log` | `7A82190F79681AEFFA4E114CB45C839DC9B4FD329E373EC75A010FAE7DE2C500` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a135"></a>A135 | `output/release-remediation-20260909/frontend-install.log` | `E529151E6602C07F952758E3EE770088D68B7A97745E7125EE3A616B379A107E` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a136"></a>A136 | `output/release-remediation-20260909/frontend-lint.log` | `C4C47E50498BADC4D8ED21074828D2032D31995CBF84B8AE53E2089C5B77D526` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a137"></a>A137 | `output/release-remediation-20260909/frontend-r1-full-final-coverage.log` | `0120E0D4156E342A9E12A95131F0105DEC60E11CC5E1F38FE13594E1AD79359D` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a138"></a>A138 | `output/release-remediation-20260909/frontend-r1-green-focused.log` | `7A96459F7C4697C4BB85E8B5082886E5F274A367A8DB0D69780A86B66DA65642` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a139"></a>A139 | `output/release-remediation-20260909/frontend-r1-red-snapshot.json` | `9714E430770E2B434F4ED944ACB9400806D990702E769DB269E179E27E4A76E9` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a140"></a>A140 | `output/release-remediation-20260909/frontend-r1-red.log` | `BEB4124090E7DBC31A97BDDC1C138DF4CC0F123655F84ABEA0C6AE3610134D50` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a141"></a>A141 | `output/release-remediation-20260909/frontend-snapshot.json` | `11D2425ED21CF48EF6BFB7C675DE7C6440EB393E0F755A6F835DE7580A3C431D` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a142"></a>A142 | `output/release-remediation-20260909/frontend-typecheck.log` | `60A6F6E3B322355690CEA80154C8347322E4D21C03FF61BE62162D17D3D2C0AA` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a143"></a>A143 | `output/release-remediation-20260909/payment-initial-results.json` | `FE5FF7CC9A55D503CE3F09F19B9DBB8246DB24E38FDB8D2C3AE155650D32791A` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a144"></a>A144 | `output/release-remediation-20260909/payment-initial.log` | `F9FAE16B5904ED177646FB3CD42D9660CF2BDE09FC72AAF53150FF2DB40ACD41` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a145"></a>A145 | `output/release-remediation-20260909/preservation-checkpoint.json` | `A4928605067910992436506A7943D30F356C61184DC56A5046FAAF7F1BCC054C` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a146"></a>A146 | `output/release-remediation-20260909/preservation-final.json` | `15A357E527216D1133054CCE007CCAB3705BB0A4D1D243936D934B9F0B3AE5EF` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a147"></a>A147 | `output/release-remediation-20260909/preserved-artifacts.json` | `86154FF7B669B7B43088606DA17457681262BE64C111C4D3E9D9EED871FC93BA` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a148"></a>A148 | `output/release-remediation-20260909/prior-SR-93.snapshot` | `CFA0302B4A1F126BBF7B82EAD066D230ACE8F8F2BB14A3A4FAF2958B2CAED6DE` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a149"></a>A149 | `output/release-remediation-20260909/runtime-final-readonly.json` | `CFDC7E02625488405DBA1AA8E1747CF88BB89941DE98A2A22DB29E8346FA563A` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a150"></a>A150 | `output/release-remediation-20260909/runtime-readonly-probes.json` | `84F9F765F6DB6F2E736452A9EB1FE7DB1AAC7C1143205A5EE9BA467BE0738BB9` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a151"></a>A151 | `output/release-remediation-20260909/scope-final.json` | `D1E8AB72B86FA83A42400F81B10622567F2C1438F4AC1193CAC3A61138A18D17` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a152"></a>A152 | `output/release-remediation-20260909/storage-auth-second-results.json` | `EDEFF18528710F25C79E99517BB230B6EE895F6FCA7C707522EC22778C495958` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a153"></a>A153 | `output/release-remediation-20260909/storage-auth-second.log` | `0DFE7B484CA0C3C3DE35C762EB0180E0EEF4DFCBBE8F1406322C91E20D3F8EF7` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a154"></a>A154 | `output/release-remediation-20260909/tested-source-final-check.json` | `B54C64E7F8FCBB1D974382BEF590999EAEF3070CFE27C56C71457A0017B90C43` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a155"></a>A155 | `output/security-readiness-20260908/backend-runtime-libraries.json` | `D3A6CF0DCFE472B9E60BA06725AFAE4BA720CA7BEAC70C63265349A4EBEEF410` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a156"></a>A156 | `output/security-readiness-20260908/backend-tests.log` | `D009945905DEBCC26FE44C4E763D59F303CE5ACFE3DC2D75D25DABB0FE65C9BC` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a157"></a>A157 | `output/security-readiness-20260908/docs-validation.log` | `1BC29CAA0C9A0E12E469C6864B9BA30ED6BA77C82F3D07CC55C060A8BEA945FB` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a158"></a>A158 | `output/security-readiness-20260908/frontend-tests.log` | `4DA3B5FD0476D86F14B572D1F582508A6E3907D8D67EB20AC314A0D5029274CC` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a159"></a>A159 | `output/security-readiness-20260908/http-backend-probes.json` | `EA8FF47B0D04764A31ABD688CB3AE3AA9C0AD7320B7DF5FA47BBD08D44F5B331` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a160"></a>A160 | `output/security-readiness-20260908/http-probes.json` | `D48D459BBA97931D827C1F9F5E8E0D8005BCAA9E3240EA9AEFD2BACDAE36F142` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a161"></a>A161 | `output/security-readiness-20260908/npm-audit-production.json` | `B99DB88E38FF2292DA11E1E8101C263F944AAC007BE23796C3E2809069CFF459` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a162"></a>A162 | `output/security-readiness-20260908/npm-audit.json` | `EA6086E00F09DF1D8EC185420028D1EDDAFB3FB8402B02599C8C54886A0C0C3D` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a163"></a>A163 | `output/security-readiness-20260908/npm-dependency-paths.json` | `D03B292D400669C8AA51E14D4096C32657C31651276D78ACE25FF593F1D7B334` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a164"></a>A164 | `output/security-readiness-20260908/preservation-check.json` | `2E3D6D9EEF25F0B12D8E1A970911E581F2C02D98F37151731BFADEFAA0E149D5` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a165"></a>A165 | `output/test-application-20260909/artifact.json` | `A3568E77BE13B21E525556201A997DBC49EC6B9924F72BE8FA646B4818B7A8EB` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a166"></a>A166 | `output/test-application-20260909/backend-http.json` | `39AEE62467DCEA2543F1142CE083C64EE6890533F2449FB775433B71DAF50003` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a167"></a>A167 | `output/test-application-20260909/backend-start.json` | `0330AAF7C54514DA2F949ADE3929B3B9100894DA43FA0CE3301431BC0F6C2F4F` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a168"></a>A168 | `output/test-application-20260909/commit-paths.json` | `75396B8EBA5D6E89257A42F613BAEC538B5081183C81FE78EDB5DE51FEF32A71` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a169"></a>A169 | `output/test-application-20260909/config-before.json` | `2F55616A3A88E3001C072A3F863E7AA63693789A12781377E23F7CCB2A0D16B4` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a170"></a>A170 | `output/test-application-20260909/db-after.txt` | `220B6E2C1724AC3598426858C3E7A9014407A1CD53F2D606C60520040606C47C` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a171"></a>A171 | `output/test-application-20260909/db-before.txt` | `220B6E2C1724AC3598426858C3E7A9014407A1CD53F2D606C60520040606C47C` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a172"></a>A172 | `output/test-application-20260909/final-http.json` | `D73A60ECEDA0B0A4A048716B883A77E24CC2FB5D0FAD42A224DEAC1DE0813045` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a173"></a>A173 | `output/test-application-20260909/frontend-start.json` | `8092D48F6110DF9642162FE518D9D4D520CFF60F279CAFDE3AECA73C13C2BED8` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a174"></a>A174 | `output/test-application-20260909/historical-preservation.json` | `ECDA4C16EE104D57EA5278AC38285B2CE78A01D72B3490F2CB072478C8F8D9EE` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a175"></a>A175 | `output/test-application-20260909/media-before.json` | `BDE5100E9BF6B371826A13E0E163EA3AABE16F88DBAB04D8A297EF1FEEB269F4` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a176"></a>A176 | `output/test-application-20260909/npm-audit-current.json` | `4811731A1F3C5A120AA6F650C259E7F0A1C5BAF8F5E12941B147F92DE2D74A10` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a177"></a>A177 | `output/test-application-20260909/preservation-after.json` | `11B5BD9B6235B4063AFE2F12E3C3BE6ADDD01D710AE169551412A0431C08B3F0` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a178"></a>A178 | `output/ui-ux-audit/20260809/WI-021/F-UI-021-001_PUB-09_VD_missing-notice.png` | `B835A957EA22C522F1E12C516F0867928193FD690247A276C61B9A57BBF426F6` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a179"></a>A179 | `output/ui-ux-audit/20260809/WI-021/G-PUBLIC_PUB-01_SH-01_VD_home.png` | `65134CFDEF89E2DC7F9C4C3EFF953A486CA390F9A6A348C710A18D200517FD0C` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a180"></a>A180 | `output/ui-ux-audit/20260809/WI-021/G-PUBLIC_SH-01_VM_home.png` | `1C3B89C6889BFFFC8C04727043B6C2C1CBD7C22E7A049D332A4D4AAE5F38A8AE` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a181"></a>A181 | `output/ui-ux-audit/20260809/WI-021/PUB-08_VD_notice-list.png` | `8C87826DADDBC60C2C2DB088CBB94DC2FF10B02456189293B433AFA77313E4DA` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a182"></a>A182 | `output/ui-ux-audit/20260809/WI-021/PUB-08_VM_notice-list.png` | `F5317AD7060AD28EAE43D750AA172F3BA1EEC8517B68EB5E225D23D1F56CAA65` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a183"></a>A183 | `output/ui-ux-audit/20260809/WI-022/AUTH-01_VD_login.png` | `51F9758D91546F3E45EBF3E9FD0D9E3D4A6BCF0AC5232318CC8E3B5AC1F39EBB` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a184"></a>A184 | `output/ui-ux-audit/20260809/WI-022/AUTH-02_VM_signup.png` | `EFCF677A24F83B3E69F207E99E2525F5C1F58A4070FEECCF126D4AD503F0F69D` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a185"></a>A185 | `output/ui-ux-audit/20260809/WI-022/AUTH-03_VD_invalid-token.png` | `421305996A0C8DB2CA06EB26C96EA34AFDAF6B59F83DD88070F5765D915135AC` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a186"></a>A186 | `output/ui-ux-audit/20260809/WI-023/PUB-01_VD_home.png` | `73F03BC7EBF5C9FF12B3CA6FB99FDEA9BB2DA9FC8967F911BC9A586A50D1F5A5` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a187"></a>A187 | `output/ui-ux-audit/20260809/WI-023/PUB-02_VD_tracks-filtered.png` | `35CD0888804CCDC70C0C4A681B7CE9D9AE4832088DDF0CFF1A556B890F565F38` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a188"></a>A188 | `output/ui-ux-audit/20260809/WI-023/PUB-02_VM360_tracks.png` | `C62E17CC06707E5FB8AA45AF5CFD1C687E1C28EF7BBD7CC24DD5FA5523C2A417` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a189"></a>A189 | `output/ui-ux-audit/20260809/WI-023/PUB-03_VD_track-playing.png` | `A2212D47DF23607A2B7C798D5EC29800A2E78BE541B44568558366F7006D65E2` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a190"></a>A190 | `output/ui-ux-audit/20260809/WI-023/PUB-03_VM_track-detail-viewport.png` | `A392D985C2ED1DD868F9796ECDFD1580B4479942AD35B41725AE5416352C69D4` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a191"></a>A191 | `output/ui-ux-audit/20260809/WI-023/PUB-03_VM_track-detail.png` | `CA225D4D3C0202AA118A81050105875408D2AD3911CEDD22D8E0B5DBDAD19F0C` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a192"></a>A192 | `output/ui-ux-audit/20260809/WI-023/PUB-04_VD_albums-grid.png` | `782E8922BA22C46C357941B30AD5AEF5C2A2C9FD1CBDCE041C31599E804639FF` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a193"></a>A193 | `output/ui-ux-audit/20260809/WI-023/PUB-04_VT1024_albums.png` | `066E4E9296AFFA8AEC39A0BD80E1ED36D86C3513EBE02CC7E722FDE8953961A6` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a194"></a>A194 | `output/ui-ux-audit/20260809/WI-023/PUB-06_VD_album-detail.png` | `DFB9C7ED675203198DB6F9884D441926A39F28FC82584726CC0683287F53061B` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a195"></a>A195 | `output/ui-ux-audit/20260809/WI-023/SH-01_VM360_menu-open.png` | `FA1D60BEDD0E780DC1DA11D1C931482BD37B980FBA171C2BACB6471BD3A5D2D6` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a196"></a>A196 | `output/ui-ux-audit/20260809/WI-023/SH-02_VM_player-collapsed.png` | `9105919343143E1BF763B670C712FF4DED98B5F24A552C4304759F5FF265DFCB` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a197"></a>A197 | `output/ui-ux-audit/20260809/WI-023/SH-02_VM_player-expanded.png` | `508885C47F15588F0C335DC24CCAE631D7C76CD525E29EDA106953FF67594B03` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a198"></a>A198 | `output/ui-ux-audit/20260809/WI-024/G-SUB_VM390_anonymous-login.png` | `E1170098C0659364CE85B7BD9CA0E7D549F899DA8EE73432356E1343794B39DC` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a199"></a>A199 | `output/ui-ux-audit/20260809/WI-024/SH-03_VM360_history-modal.png` | `4E8F69E801FDB2CE785E96DD422B8310F9B2DE1659FE7A30E8C41AB43BE8F32C` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a200"></a>A200 | `output/ui-ux-audit/20260809/WI-024/SH-04_VD1440_guest-drawer.png` | `38E726BACCCD07A7ABD478FA317F4E6189A6EB968895D5F7F82796693005DA22` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a201"></a>A201 | `output/ui-ux-audit/20260809/WI-024/SH-04_VM360_guest-drawer.png` | `EE592016DA547AB1BB992D4161E0C9789D48D971A9A37A61B0A4DD8F4040DF73` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a202"></a>A202 | `output/ui-ux-audit/20260809/WI-024/SH-04_VM390_guest-drawer.png` | `0CE6F33DF849771986B52E6E26B3F77C3EBCEAC9D52328ADDFD7DE3E890FD300` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a203"></a>A203 | `output/ui-ux-audit/20260809/WI-024/SH-04_VT1024_guest-drawer.png` | `EC04AF85BF8F1D086F2308D247646DFF2F7CF8E4672EC01B0E7E5D9B5D224FFD` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a204"></a>A204 | `output/ui-ux-audit/20260809/WI-027/PUB-07-business-yearly-1280x720-observed.png` | `11EF297839A11A83CE31BD2F68C62446EDFC0229EA148B059EF2DFDDCB8A77FD` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
| <a id="a205"></a>A205 | `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql` | `377BC80B458F9475CED58800EC90EF5E912C739553EA122BE0D16D6225EA0E2F` | `ARCHIVE_LOCAL` | `ARCHIVE_LOCAL` |
