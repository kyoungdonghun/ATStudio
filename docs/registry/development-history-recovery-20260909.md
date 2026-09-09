---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: docops
category: registry
status: stable
dependencies:
  - path: v1-artifact-retention-20260909.md
    reason: Stable original identities and all 205 previous/current dispositions
  - path: ../policies/archive-policy.md
    reason: Preserve history and distinguish public derivatives from private originals
  - path: ../policies/security-policy.md
    reason: Do not publish credentials, account identifiers or private machine paths
  - path: ../../deliverables/user/REQ-20260909-ATS-004.md
    reason: Approved development recovery scope
  - path: ../../deliverables/agent/WI-20260909-ATS-018-handoff.md
    reason: DocOps ownership and semantic review contract
---

# Development History Recovery Register

> Purpose: Keep useful development decisions inspectable without the original PC or conversation, while preserving original evidence privately and keeping historical claims separate from current authority.

## Result And Boundary

WI-20260909-ATS-018 reviewed the substantive contents of all **96 original
Markdown documents** in archive `v1-artifacts-20260909-084405`, not just their
headings, links or filenames. This includes the 83 previously local-only
documents. Review covered approval constraints, decisions, failure attribution,
commands and observed outputs, test scope, corrective reviews, rollback and
handoff boundaries. Identical repeated boilerplate was matched to already-read
text; all distinct body blocks were read. Every original has an individual
semantic rationale below.

**All 96 are useful history.** The 13 previously retained documents stay
byte-identical. The other 83 are public derivatives at their original paths,
preserving existing link destinations. Seventy-one derivatives add provenance
only; two also normalize a redundant empty line at EOF (E1), and ten redact
private identifiers/paths. Their historic frontmatter,
language, dates, status and claims are preserved, not refreshed to today.

| Population | Prior WI016 availability | Published recovery availability |
|---|---|---|
| Original documents | 13 in Git; 83 local-only | 13 unchanged plus 83 public derivatives at original paths |
| Raw evidence | 109 local-only | 109 local-only, not republished |
| All original identities | 205: 13 retained / 192 archived | 205: 13 retained / 83 derivative-backed / 109 raw private |

A derivative-backed original **still remains unchanged in the private archive**.
This table describes availability of a reviewed public representation, not
removal or publication of the private source. The
[205-entry ledger](v1-artifact-retention-20260909.md#original-path-register)
preserves each original path/hash and both dispositions; it supersedes only
public availability accounting, not the original private receipt.

This source/history delivery is **published and remote-clone verified** at
`65b8cce9d7c60370d761e3e6d3c34821a7c7e675` (104 scoped files). MA supplied the
exact `origin/main` match and normal full-origin clone fast-forward: clean status,
38 source checks, 33/33 synthetic tests, docs PASS/exit 0, all 96 public hashes
matched, 104 selected tracked tooling files present and no private inputs. WI020 completed
PASS with no open findings. These are MA publication/clone executions, not
retroactive DocOps tests. [REQ004's MA receipt](../../deliverables/user/REQ-20260909-ATS-004.md#ma-delivery-receipt)
is the current delivery boundary; private off-device recovery remains incomplete.
Restored handoffs do not authorize fresh commands, DB changes, retries, cleanup,
payment, SMTP, deployment or production acceptance.

## Reading Map

Read current entry points before historical evidence. These are routing pointers,
not a new product audit or replacements for existing policies.

| Need | Current authority / entry point | Historical decisions worth retaining |
|---|---|---|
| Start a development session | [Project guidance](../../AGENTS.md), [Documentation Index](../index.md), [Core Principles](../standards/core-principles.md) | REQ single gate, bounded WI ownership, no inferred retries; A004, A010-A034, A063-A071 |
| Consent and session contracts | [API specification](../design/api-spec.md), [Screen Flow](../ui/screen-flow.md), [DB schema](../design/db-schema.md) | Always-transmitted marketing boolean versus affirmative-only ledger; verified password sessions; 204-only logout; A001-A008, A073 |
| Database verification | [Database scripts](../../scripts/database/README.md), [DB schema](../design/db-schema.md) | Source count is not MySQL proof; compiler/capture failure is not DB failure; guarded Inventory is not cleanup authority; A009-A034, A074-A086 |
| Secrets and evidence | [Security Policy](../policies/security-policy.md), [Archive Policy](../policies/archive-policy.md) | No credential values/raw exceptions in public output; private account/path substitution; fixed-target correlation; A021-A034, A046-A060 |
| Local runtime and storage recovery | [Runtime Storage Operations](../design/runtime-storage-operations.md), [Acceptance scripts](../../scripts/acceptance/README.md) | Child environment isolation, exact process ownership, DB plus public/private storage consistency, startup side effects; A044-A061 |
| Acceptance and release | [Payment pack](../payment/index.md), [SR-93](../SR/SR-93.md) | Source quality, mocked/H2 tests, actual local observations, external acceptance and production readiness are distinct; A035-A043, A052-A062 |
| Supported seed workflow | [Acceptance scripts](../../scripts/acceptance/README.md), [API specification](../design/api-spec.md) | Aggregate counts do not establish canonical identity/order parity; independent FAIL must survive producer success claims; A052-A055, A072, A093-A094 |

The reusable principles above are already routed to current entry points.
No current policy was rewritten to fit historical implementations, and no
product, database, runtime or dependency audit was repeated.

## Historical Reading Notes

1. **Consent R2/R3/R4 chronology:** A005's missing decision register and A006's
   unapproved safe-return attribution are not erased by A007's final PASS.
   A007 reran neither tests nor MySQL. A002's focused test counts and unapplied
   SQL remain dated evidence, not proof that the current DB has that state.
2. **Failed wrappers and later evidence:** A014 corrects a private-bundle blame
   to PowerShell's automatic-variable collision. A021 records the predicate
   failure; A023 separately records a supplied operator exception and one patch.
   A025's invoked Observe with unknown cleanup is not the zero-action case from
   earlier attempts. A033's zero count is limited to its guarded predicate and
   does not retroactively supply missing evidence to A027/A031.
3. **Scoped authorization is historical:** A063's original target exclusion
   differs from later handoffs/amended approvals. A051 both prohibits starting
   runtime and requires runtime readiness, while A050/A092 report actual ready
   results. These tensions are preserved; neither document authorizes a fresh
   operation in WI018.
4. **Seed producer versus independent review:** A052/A093 report two-target
   aggregate parity and a direct-JDBC account exception. A072 forbids direct SQL,
   and A054/A094 independently FAIL the account path, canonical parity and
   environment-binding evidence. Do not merge those records into accepted
   workflow parity or treat the exception as current policy.
5. **Rehearsal, isolation and corrections:** A059 requests a development DB
   clone; A058/A096 instead report a fresh schema/seed clone with no tracks and
   blocked browser work. A061/A062 are explicitly later MA-supplied observations,
   not a rewrite of WI030 partial or WI031 interrupted status. Mocked UI/H2
   passes, actual local runtime observations and download browser-event timeouts
   remain separately described. Unavailable companion WI031 outputs are not
   fabricated or added to the original population.
6. **Dated quality/release claims:** A037/A088's router/audit results apply to
   their recorded versions and dates, not perpetual security. A035 reports
   1,614 total with 19 skipped, while retained A087 says 1,614 passed alongside
   19 skipped; this reporting discrepancy is preserved, not normalized into a
   new test claim. A041-A043's release planning is not production approval.
   The September cleanup publication receipt is separately linked from the
   retention register and does not publish WI018.

## Privacy And Provenance Method

The actual original manifest is an object with `archiveId`, `createdAt`,
`sourceCommit`, `sourceRoot`, `count` and `artifacts`; each artifact has
`path`, `bytes` and `sha256`. WI018 used structured JSON parsing of this
schema and preserved its 205-member order. Private `sourceRoot` is not copied
into public output. The previous private disposition stays untouched.

- **P0**: Original retained bytes, no edit (13 documents).
- **P1**: English historical-provenance notice without privacy substitutions
  (73 documents: 71 notice-only and two with the E1 modifier below).
- **P2**: P1 plus private Windows home prefix replaced with
  `$USERPROFILE` (8 documents, 10 occurrences). The remainder stays a
  historical lookup key, not a machine-independent executable path.
- **P3**: P1 plus private contributor branch label replaced with
  `dev/PRIVATE-CONTRIBUTOR` (2 documents, 2 occurrences). Branch identity
  can only be recovered from the private original if separately authorized.
- **E1**: Public-derivative EOF normalization, only A056 and A095. Remove one
  redundant terminal LF byte while retaining the final content-line terminator.
  Original archive bytes, all historical facts, body text and internal hard breaks
  remain unchanged. This modifier does not change the P1 classification.

No actual credential, token or account value was copied into the public
derivatives. No original was modified. Full-body review and pattern screening
found no email-address, provider-key, JWT or private-key payload in the 96
Markdown originals; this is a bounded review finding, not a guarantee about raw
109 contents or unseen private bundles. Private homes and contributor branch
identifiers were removed. API routes such as `/api/users/me` are preserved.

All 83 new files have a notice after any original frontmatter. Original
frontmatter fields and substantive text were retained except the P2/P3
substitutions. Transport newlines may normalize to the workspace convention;
the two E1 derivatives additionally remove only the redundant final empty line
identified by MA's staged whitespace gate. Their public hashes below were
regenerated after the one-byte correction in each file;
original bytes remain identifiable by their own SHA-256. No historical
Markdown destination or YAML dependency needed repair. Code-formatted references
to local/generated/private files were retained, with an explicit availability
warning. Per-file public SHA-256 below identifies the actual delivered bytes,
not a reconstruction of the private original.

## Evidence Availability

Original archive identity, as supplied by MA and confirmed against the
structured baseline receipt:

| Item | Identity / result |
|---|---|
| ZIP SHA-256 | `486744B423DDDDD4DAFDB96224F5612EBCB858DF2FB128417AC4CFD5C52E4163` |
| Manifest SHA-256 | `C3D1F9D90DC36D440AC0659E4ED29C26592457457D5EC478782CD9E452F7EE28` |
| Baseline receipt | `%LOCALAPPDATA%/ATStudio/archives/development-recovery-20260909-094234/baseline.json` |
| MA preservation | 205 restored members matched; 955 protected and 79 local files unchanged; three process start identities unchanged; four HTTP 200s |
| WI018 own work | 96 semantic reviews; 83 derivative files; 96 public-file SHA-256 checks including 13 retained-original equality checks |
| Not executed by WI018 | Original 205-member rehash, product tests/builds, HTTP/process probes, DB/runtime actions, upload, commit/push or source-clone verification |

The archive and its raw 109 remain local-only. An off-device private backup
destination has not been supplied, and no upload, DB dump, actual recovery or
production approval is claimed. The actual published origin clone retains safe
decisions and all 96 verified public documents; it cannot recover private configuration, secrets,
DB records, media or original raw evidence by itself.

MA's final `remote-validation.json`, `candidate-validation.json` and
`final-preservation.json` remain private under the baseline receipt directory
above. They record actual source delivery and preservation, not a new-OS install,
DB restoration or fresh live-row-state inspection. REQ004 is partial; production
steps 2-4 remain unchanged.

The ledger's A097-A205 identify raw members of this specific archive.
Other code-formatted private evidence, generated build reports, runtime bundles
and interrupted-work companion files may be outside the 205-member population.
Their mention does not establish archived or future availability. Request only
necessary private originals from the custodian and verify their original hashes;
never execute the archived SQL or overwrite current data as a document-recovery
step. Future public changes require their own privacy review.

## Per-Document Review And Provenance

Each row links the original identity to its retained or derivative representation.
P0/P1/P2/P3 and the scoped E1 modifier are defined above. Original SHA-256 is copied from the manifest;
public SHA-256 is measured from the workspace file. A path alone never equates
the two byte identities. Each A001-A096 also resolves in the
[original ledger](v1-artifact-retention-20260909.md#original-path-register).

| Entry | Historical document at original path | Original SHA-256 | Public SHA-256 | Method | Semantic preservation rationale |
|---|---|---|---|---|---|
| <a id="a001"></a>[A001](v1-artifact-retention-20260909.md#a001) | [WI-20260809-ATS-068-decision-register.md](../../deliverables/agent/WI-20260809-ATS-068-decision-register.md) | `F19E8A22C2D84160A3BEC6323E33534E40F5D681FF6B4D122E0177D8EB204B0D` | `834AB7966EE19D2E571BE7EEF520A9B381A209367F8BA47A728B804608962F25` | P1 | Accepted consent ledger, verified-password session and 204-only logout decisions; DG-068-05 excluded; MySQL unrecorded. |
| <a id="a002"></a>[A002](v1-artifact-retention-20260909.md#a002) | [WI-20260809-ATS-068-evidence-pack.md](../../deliverables/agent/WI-20260809-ATS-068-evidence-pack.md) | `E028E23A5552D71DC4692B8B39A322C58E5303459990006592F616928270F521` | `0BAA82CDD4BFB75C83561F2110CFA09D10210669A0E9ACAC4BA6C02698EF5DDE` | P1 | Final R4 PASS after initial/R2/R3 conditional findings; focused test counts and unapplied SQL preserved. |
| <a id="a003"></a>[A003](v1-artifact-retention-20260909.md#a003) | [WI-20260809-ATS-068-final-evidence-audit.md](../../deliverables/agent/WI-20260809-ATS-068-final-evidence-audit.md) | `7846FC7EAABCF1488BA90FD0A08563CCD1D10019233E47CCF7929F399B088C79` | `7EE30A8481160647471485B7D48CFF5C9F15EEA291550326F218BE0A17D8CA7C` | P1 | Truthfulness audit separates source counts from MySQL proof and records historical hard-break whitespace. |
| <a id="a004"></a>[A004](v1-artifact-retention-20260909.md#a004) | [WI-20260809-ATS-068-handoff.md](../../deliverables/agent/WI-20260809-ATS-068-handoff.md) | `31E0D7B021B7B3C9E1B2F9AD568CD4DB28F425D068785D3BB804D18447D43E62` | `B8804F78B0B40990DFEA07775AD242AF52146C6D986BEC673C949795D2C8403A` | P1 | Approved implementation scope excludes social changes and DB patch execution; preserves allowed test and caller ownership. |
| <a id="a005"></a>[A005](v1-artifact-retention-20260909.md#a005) | [WI-20260809-ATS-068-qa-integ-r2-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-r2-review.md) | `F4363244D0F18FC25FE341ECF38F7EBB0CD14C572E4482F5BBF7FA0BA7CC9CAD` | `E829C033AFE5266246CFB1D318F23D41C35F071B8EC5F33247B4AD84B5D6C33B` | P1 | R2 conditional verdict: missing decision register despite corrected 43-table and marketing-wire documentation. |
| <a id="a006"></a>[A006](v1-artifact-retention-20260909.md#a006) | [WI-20260809-ATS-068-qa-integ-r3-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-r3-review.md) | `5FE3A90DB863900C7C07643F6F2868B0A39E0BDD7FCA27D2E861A74A47F219C6` | `5962563F395456F5BE0FB8C45519A07D1D80812BFA44C4E6F73B9803F0C60D19` | P1 | R3 conditional verdict: implemented safe-return is not approval evidence for DG-068-05. |
| <a id="a007"></a>[A007](v1-artifact-retention-20260909.md#a007) | [WI-20260809-ATS-068-qa-integ-r4-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-r4-review.md) | `05A34115829318B619846D23604123FC9B7559B1A6C7DCCD09314B16CA98FA67` | `18CC019248EE5B7B3E97251C6C5AD19F4D16E823336C2A2E65E85903B30B1E9A` | P1 | R4 closes only scope-attribution defect; no database or test rerun. |
| <a id="a008"></a>[A008](v1-artifact-retention-20260909.md#a008) | [WI-20260809-ATS-068-qa-integ-review.md](../../deliverables/agent/WI-20260809-ATS-068-qa-integ-review.md) | `F7038DA65AD6F08D56C4E3E400F6CD83227DD39783C579BDD6070E5551412576` | `79200CBC62AA1CA84DCF588817B5FED0818832310A2C3200388F7275B116EA78` | P1 | Initial integration review distinguishes transmitted marketing boolean, affirmative ledger storage and stale MySQL claims. |
| <a id="a009"></a>[A009](v1-artifact-retention-20260909.md#a009) | [WI-20260816-ATS-001-evidence-pack.md](../../deliverables/agent/WI-20260816-ATS-001-evidence-pack.md) | `34F4A80863B19F316080E5E164E9CF38143275A0ABE8DC610CB1F49A5B3860AB` | `79E97783D7E1D7308382758DBBB72EDA0820D6905F9C958D1E1FA3301FF32D68` | P1 | Three preflight failures; zero connection or SQL; single-use attempt exhausted and successor blocked. |
| <a id="a010"></a>[A010](v1-artifact-retention-20260909.md#a010) | [WI-20260816-ATS-001-handoff.md](../../deliverables/agent/WI-20260816-ATS-001-handoff.md) | `19DECDDD4BB1F34041EBA454E3972626EF9E4A4D55953456E2AC4B1278993167` | `3718C743F48D37B271AE9B4404BB645016B3EC86098DBBDCC353D83D73B50860` | P1 | At-most-once consent patch approval with fixed opaque input, fail-closed and no automatic rollback. |
| <a id="a011"></a>[A011](v1-artifact-retention-20260909.md#a011) | [WI-20260816-ATS-002-evidence-pack.md](../../deliverables/agent/WI-20260816-ATS-002-evidence-pack.md) | `389B26DADE6561A23888C508E4BF96C29E9AF2C6EC30CA2302FBE564B146A16D` | `90253064395C9F6E609538E98EFF630A3851C6CFBBEA97F91A99F64AC1067E62` | P1 | 20 guards passed; target capture incomplete before DB action; source remains UNRECORDED. |
| <a id="a012"></a>[A012](v1-artifact-retention-20260909.md#a012) | [WI-20260816-ATS-002-handoff.md](../../deliverables/agent/WI-20260816-ATS-002-handoff.md) | `02B6D9E3AF7C02D815778F2083D721777E483BD6706C0C4DBEFDE806834D264D` | `8E16AA9E423920E3930A1726870B19CD808BF20953CC4684AE64FB96DD2846A9` | P1 | One generated isolated target; current schema then seed; no old patch, inferred test or guard bypass. |
| <a id="a013"></a>[A013](v1-artifact-retention-20260909.md#a013) | [WI-20260817-ATS-001-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-001-evidence-pack.md) | `0885E296C1982FB171FCFD4EB7C9A792F1781D927A729D72853CAC3B903938D0` | `F3AE7F0074524673978515B2B6B7B8F073010006CC05C7F29A67724A02B22157` | P1 | One-shot runner compile stopped before connection; temporary artifacts removed. |
| <a id="a014"></a>[A014](v1-artifact-retention-20260909.md#a014) | [WI-20260817-ATS-001-handoff.md](../../deliverables/agent/WI-20260817-ATS-001-handoff.md) | `4FAF19431BCFA3F118EC7CA5091910F50380105A41203A7BE2E1F61A2A705A2F` | `29B764C5ABD1D5100D1E611F49E917FC0CF82899C99DD2067A0A6CBFD7358152` | P1 | Corrects prior bundle blame to PowerShell automatic-variable collision; no retry authority. |
| <a id="a015"></a>[A015](v1-artifact-retention-20260909.md#a015) | [WI-20260817-ATS-002-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-002-evidence-pack.md) | `C6200765A08ACE3526E5C38A66572AA310EAD7363D141F0804F079A9E7F7FCA4` | `C2EA29224EDA3975DCE27A0EB46A0E8207AEE3E7FE00ABCF19D35D3122332C47` | P1 | Source-only toolchain readiness PASS; successor handoff preparation only, no DB proof. |
| <a id="a016"></a>[A016](v1-artifact-retention-20260909.md#a016) | [WI-20260817-ATS-002-handoff.md](../../deliverables/agent/WI-20260817-ATS-002-handoff.md) | `29C09C27BABE2FCDC03578962DD3478381E6784BBA9AB3C15C09C1B17192AD3D` | `41F821B280F389D74C439AE177C2AAC6AD7969BDBB2C42D175CFCBBFC24920DF` | P1 | Compile-only diagnostic categories and mandatory temporary cleanup; automatic-variable avoidance. |
| <a id="a017"></a>[A017](v1-artifact-retention-20260909.md#a017) | [WI-20260817-ATS-003-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-003-evidence-pack.md) | `020CCE7EFD25664C594F6296A7EBF6E38BB928791762C68E9F7ADB92480A2B8B` | `11F254DB869352C9EFE37E019365D31EC7E63B8EEF7781B67372957B042D22A3` | P1 | Immediate pre-execution stop despite predecessor PASS; zero SQL and no docs validation. |
| <a id="a018"></a>[A018](v1-artifact-retention-20260909.md#a018) | [WI-20260817-ATS-003-handoff.md](../../deliverables/agent/WI-20260817-ATS-003-handoff.md) | `94F2BFAA968FDBBA31171870F4377A1122D3AB499CDF88FFD278E0F69E0181A0` | `C7E543CE896424ADBA315662218D6B44FB867A56602C2F37C6BB2259E3757B77` | P1 | Fresh runner and one connection/absence/patch/delta sequence; no reused runners or disposable proof. |
| <a id="a019"></a>[A019](v1-artifact-retention-20260909.md#a019) | [WI-20260817-ATS-004-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-004-evidence-pack.md) | `BB72DCC8E4179BB5D68C5B693A730FF76AF11E6CC2662D7428F847370F3CB51C` | `C266F21D8AF3CF8883D1E5A1B43F21BC82661B6631224C8ED170BFB0AA93726E` | P1 | Compiled final runner but private validation unstarted; fail-closed zero DB; cleanup and docs PASS. |
| <a id="a020"></a>[A020](v1-artifact-retention-20260909.md#a020) | [WI-20260817-ATS-004-handoff.md](../../deliverables/agent/WI-20260817-ATS-004-handoff.md) | `E05343E804E0CF96B0FCBCFB3A11DF2D0C15CEA3D27753126515038821E49397` | `5EC6F0330E753EDEDD3C67FC7E271BB77D692A84972F90E1C0CD4B06101E197E` | P1 | Distinguishes MA interruption from technical failure; terminal-only one-shot sequence. |
| <a id="a021"></a>[A021](v1-artifact-retention-20260909.md#a021) | [WI-20260817-ATS-005-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-005-evidence-pack.md) | `4635B6722679AF5D97D06AE4988A9B85122B4DE85ABEB04F513C32F650772116` | `6EF79EA55EB7083C13EFE73A45CBBD4A8783E8E99B5F69EA2FD2E66ECDF26477` | P1 | P1-P4 PASS, P5 FAIL, prerequisite-aware downstream codes; WI006 still blocked. |
| <a id="a022"></a>[A022](v1-artifact-retention-20260909.md#a022) | [WI-20260817-ATS-005-handoff.md](../../deliverables/agent/WI-20260817-ATS-005-handoff.md) | `866FB1990DB0E184E341B6DAFAFCC6F764C5025EAA752B50431AE2E7105AFF3C` | `1D9AE3C01AC8C979D2B90A9CA27EC8711D41F003C693C01C26306D1965D428C2` | P1 | Per-predicate observability without secret/raw exception output; no Java or DB diagnostic. |
| <a id="a023"></a>[A023](v1-artifact-retention-20260909.md#a023) | [WI-20260817-ATS-007-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-007-evidence-pack.md) | `A4664FBA3CECA5514A12A4FCD30BD2D19C6851E3A2A6E3656A40BCD2EBB062E3` | `0AC380E59AF9E73F7D2DBBAB3F72AFE43019B194109C66FFA7D40D60BA32ED0F` | P1 | Preserves P5 false-negative then separate supplied operator exception: one patch, expected delta only. |
| <a id="a024"></a>[A024](v1-artifact-retention-20260909.md#a024) | [WI-20260817-ATS-007-handoff.md](../../deliverables/agent/WI-20260817-ATS-007-handoff.md) | `A1FACCB11C88701310C7730278F3A4D539F1ACDA947CB7767AFB9599492FD8D2` | `9E3799C54C4E2B6258AFB75792C455F3470E2712A5CA1C6992A5C9BE1D5E26FD` | P1 | Corrected hashtable predicates before source/compiler/DB; aggregate-only results and no retry. |
| <a id="a025"></a>[A025](v1-artifact-retention-20260909.md#a025) | [WI-20260817-ATS-008-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-008-evidence-pack.md) | `95D025A1BF66361F009DE8D64B53766AA13C5DAB47DDAB0D7854413DFB05DC28` | `8A50926F82CF8131506CA0B27FC05451DC93DEC9AE9FC4AAF88BEB906C7B6ABC` | P1 | Preflight and one Observe ran; field cardinality failed and cleanup remains unclear, not presumed absent. |
| <a id="a026"></a>[A026](v1-artifact-retention-20260909.md#a026) | [WI-20260817-ATS-008-handoff.md](../../deliverables/agent/WI-20260817-ATS-008-handoff.md) | `F123C226784769614358B631CC6CF6101CBDFAF168FE7B219BFEA18090994939` | `F6B0B6A301FC70FE88DCEB9083332FA38BE66E0712220115768877D6A55FE870` | P1 | Same-target correlation and unique safe fields; observation is not manifest recording or cleanup authorization. |
| <a id="a027"></a>[A027](v1-artifact-retention-20260909.md#a027) | [WI-20260817-ATS-009-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-009-evidence-pack.md) | `D5D5D69CF50B448720F0084C32746AD7BE2646C177170E6C855B39C165B26688` | `FF183CE4C640A2592E1B047861F91975BB41BFFC996B1525FF9E65529446F5B0` | P1 | Sole inventory blocked with no count; possible orphan undetermined, no cleanup authorized. |
| <a id="a028"></a>[A028](v1-artifact-retention-20260909.md#a028) | [WI-20260817-ATS-009-handoff.md](../../deliverables/agent/WI-20260817-ATS-009-handoff.md) | `906DFDBB958E26624607615B04252DCE5CA1C979597DCC999C7F46757603651D` | `508A305E0BBA2902D3D210B86712282CA4BCC242DC40ACFE7FAEC9000F74CC9B` | P1 | Root-only parameterless aggregate inventory and database-side exact name guard; separate destructive approval. |
| <a id="a029"></a>[A029](v1-artifact-retention-20260909.md#a029) | [WI-20260817-ATS-010-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-010-evidence-pack.md) | `29356A8690BB9177B3D6AEB05D7F1C5D6A0406A79CE15156C6BB5E200058D2BF` | `54C92734A189621FC190DF4CADBA247E816002B5031587F11CC5F020452838E9` | P1 | Supported Inventory implementation; 25 non-DB guards and compile; runtime intentionally not invoked. |
| <a id="a030"></a>[A030](v1-artifact-retention-20260909.md#a030) | [WI-20260817-ATS-010-handoff.md](../../deliverables/agent/WI-20260817-ATS-010-handoff.md) | `7F492B3C3788E04FB80F3C8C0D1ED79F84B4BD48BD7A3B6D343491C80D899B6B` | `A6A4DBF7AB08167FF258A9C49BCF2B7E29F5B1ED3FD204183793B9D94DE75970` | P1 | Preserve legacy actions; Inventory only one root count independent of UNRECORDED manifest. |
| <a id="a031"></a>[A031](v1-artifact-retention-20260909.md#a031) | [WI-20260817-ATS-011-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-011-evidence-pack.md) | `D12B566DFD8ECB564A434A251186A62750FA22BDA03A997084E686E348A704FE` | `9CF4573380352765B5DDD7766B05824BC13BBD1653BE98895F3EE36BF9F2BD20` | P1 | One Inventory wrapper stream rejected by safe gate; orphan count unknown, no retry. |
| <a id="a032"></a>[A032](v1-artifact-retention-20260909.md#a032) | [WI-20260817-ATS-011-handoff.md](../../deliverables/agent/WI-20260817-ATS-011-handoff.md) | `C3DEF1B0932D8ADE55F0BC87F7708DBD34EE6DDEE003803A4262ABD9940464E0` | `8ECE32BD544B50D6159C5605AB598D3B6019EAE4306AF67538130CD69867F2C3` | P1 | Guard-only companion is not target; single wrapper input/output contract and positive-count stop. |
| <a id="a033"></a>[A033](v1-artifact-retention-20260909.md#a033) | [WI-20260817-ATS-012-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-012-evidence-pack.md) | `82036B0DC5F4DD2038BFA5EEAE8347B36820812096C25190CA865FAEEC0F04AF` | `18803027B7FAE086839A79570BB5B28EBB5DA16894440597DD67D10508FEFD93` | P1 | Corrected two-group capture accepts count zero; conclusion limited to fixed orphan predicate. |
| <a id="a034"></a>[A034](v1-artifact-retention-20260909.md#a034) | [WI-20260817-ATS-012-handoff.md](../../deliverables/agent/WI-20260817-ATS-012-handoff.md) | `5892A100C5C756C0FB3F4B4F6C2D429806BB6858773BF990E6109153BA18435F` | `8F159CA00D148C17DDF9E9A10368517535FFB7E320F38D0E243EC29D35364AE3` | P1 | Internal Preflight then Inventory strict ordering and cardinality; no second action or wider cleanup. |
| <a id="a035"></a>[A035](v1-artifact-retention-20260909.md#a035) | [WI-20260817-ATS-013-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-013-evidence-pack.md) | `19CFACD8E08ADA3959ABD237FB05B2263BEA6DFCB0E7C0A6E48B731C405DEDD8` | `19CFACD8E08ADA3959ABD237FB05B2263BEA6DFCB0E7C0A6E48B731C405DEDD8` | P0 | Test-only consent fixture, safe auth fallback and duplicate logout repair; combined dirty-worktree suite counts. |
| <a id="a036"></a>[A036](v1-artifact-retention-20260909.md#a036) | [WI-20260817-ATS-013-handoff.md](../../deliverables/agent/WI-20260817-ATS-013-handoff.md) | `52F63785CB0F1FA5BE6CEAAB411A89D94BA966CB0D4FAB55B4DE74922F4BBD9C` | `52F63785CB0F1FA5BE6CEAAB411A89D94BA966CB0D4FAB55B4DE74922F4BBD9C` | P0 | Align stale tests to approved behavior without product changes. |
| <a id="a037"></a>[A037](v1-artifact-retention-20260909.md#a037) | [WI-20260817-ATS-014-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-014-evidence-pack.md) | `C71E7ACD5B5981B4B7F1F7625CAEADC44AE2A20857E45529D190D593D7BA2F71` | `C71E7ACD5B5981B4B7F1F7625CAEADC44AE2A20857E45529D190D593D7BA2F71` | P0 | React Router 6.30.4 to 7.18.2 data-router compatibility and dated production audit zero. |
| <a id="a038"></a>[A038](v1-artifact-retention-20260909.md#a038) | [WI-20260817-ATS-014-handoff.md](../../deliverables/agent/WI-20260817-ATS-014-handoff.md) | `7BF0F510A9843F74FAA05BD5E2465103744A9E4FF1D50AEF064EF751937B9DBF` | `7BF0F510A9843F74FAA05BD5E2465103744A9E4FF1D50AEF064EF751937B9DBF` | P0 | Controlled V7 peer checks and narrow compatibility edits; no framework-mode redesign. |
| <a id="a039"></a>[A039](v1-artifact-retention-20260909.md#a039) | [WI-20260817-ATS-015-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-015-evidence-pack.md) | `B36A50F7B2BD53BED71EC8E5D39092AF621CC824DF7753C7597EFA88B3C021CE` | `B36A50F7B2BD53BED71EC8E5D39092AF621CC824DF7753C7597EFA88B3C021CE` | P0 | Current-doc 43-source/UNRECORDED and router correction; SR93 remains open. |
| <a id="a040"></a>[A040](v1-artifact-retention-20260909.md#a040) | [WI-20260817-ATS-015-handoff.md](../../deliverables/agent/WI-20260817-ATS-015-handoff.md) | `973CB07D4D9CD01A81397FF1A490DB07AC58615E3A61B63B1145C4B704A93A56` | `973CB07D4D9CD01A81397FF1A490DB07AC58615E3A61B63B1145C4B704A93A56` | P0 | Preserve historical 39/41/42-table evidence and dated audit results, not permanent security claims. |
| <a id="a041"></a>[A041](v1-artifact-retention-20260909.md#a041) | [WI-20260817-ATS-017-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-017-evidence-pack.md) | `81B6A4DA704CFD1BC81C2089893C335680096E94B027045D32E589BDEE718AEF` | `81B6A4DA704CFD1BC81C2089893C335680096E94B027045D32E589BDEE718AEF` | P0 | Independent release audit and six unstaged commit groups; accepts predecessor MySQL proof but no lifecycle rerun. |
| <a id="a042"></a>[A042](v1-artifact-retention-20260909.md#a042) | [WI-20260817-ATS-017-handoff.md](../../deliverables/agent/WI-20260817-ATS-017-handoff.md) | `92A54626F889CFB47EA4925AB0D43F2AF00DE26259D4BF57AEA5B29F1D445AAA` | `92A54626F889CFB47EA4925AB0D43F2AF00DE26259D4BF57AEA5B29F1D445AAA` | P0 | Release candidate checklist separates repository, acceptance and production gates; read-only audit. |
| <a id="a043"></a>[A043](v1-artifact-retention-20260909.md#a043) | [WI-20260817-ATS-018-handoff.md](../../deliverables/agent/WI-20260817-ATS-018-handoff.md) | `44374FD46CE88ED10482A794C1CEC6EE09E997722B53F9EE3AB1959FB7E95AA2` | `44374FD46CE88ED10482A794C1CEC6EE09E997722B53F9EE3AB1959FB7E95AA2` | P0 | Approved six-commit assembly ordering and hunk review; no push/excluded SQL or raw outputs. |
| <a id="a044"></a>[A044](v1-artifact-retention-20260909.md#a044) | [WI-20260817-ATS-019-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-019-evidence-pack.md) | `D19EEE08E3C12A2854507C4DB33433546D07CDDFB08CA5B9B4C252FEAD34B4C7` | `820D4459F65DB23908FAB9760F712381BFC55FB1494455B8368CEA879F8E2745` | P3 | Branch ancestry and Vite-only listener snapshot; local frontend is not acceptance runtime. |
| <a id="a045"></a>[A045](v1-artifact-retention-20260909.md#a045) | [WI-20260817-ATS-019-handoff.md](../../deliverables/agent/WI-20260817-ATS-019-handoff.md) | `CEAAD86ED51F5F617B82E620C8CC9101246663AA541128FC6D876C486B9F8D18` | `CBE8E8187CB0C60E273A41D61868A5149D89CE6B224478857EA4BB5D146E7F2D` | P1 | Read-only branch/runtime inventory; separate origins, databases, schedulers and external effects before action. |
| <a id="a046"></a>[A046](v1-artifact-retention-20260909.md#a046) | [WI-20260817-ATS-021-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-021-evidence-pack.md) | `9DC8E504E1CA308E3AFA57D6ACBBC1B1A9813CAC085D059133799AB126E1D087` | `FDAB5FCD9A496C8FCED79EA997DEF9189A3835CE61B35FC8741533A7D604D392` | P2 | Frozen source worktree created; seven environment assertions failed before launch; original Vite preserved. |
| <a id="a047"></a>[A047](v1-artifact-retention-20260909.md#a047) | [WI-20260817-ATS-021-handoff.md](../../deliverables/agent/WI-20260817-ATS-021-handoff.md) | `83EB1106D9879E97D39EBFC9F90756AAEC13962EAED42E98C9BB982010F2B4DE` | `B55E7103DC28A824554066023FCFECCD2FBCEA3C98956263AD51CCC0754081FF` | P1 | Frozen client snapshot and exact-process ownership before guarded launch; no duplicate scheduler. |
| <a id="a048"></a>[A048](v1-artifact-retention-20260909.md#a048) | [WI-20260817-ATS-022-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-022-evidence-pack.md) | `E45673B0ED77213B7924C46A03D80D4E2113BF0FD6393DE89D16D3C6921879C5` | `934AC28751A3600E9F005F36578E11CC16FEE4725DA26A64BA0C6D7728AEA5AD` | P2 | Explicit child environment maps, continuous log drainage and atomic probe files; five fresh synthetic passes. |
| <a id="a049"></a>[A049](v1-artifact-retention-20260909.md#a049) | [WI-20260817-ATS-022-handoff.md](../../deliverables/agent/WI-20260817-ATS-022-handoff.md) | `3B427D3DB53B9F6711E15161698FB624EADCD3B58B58622516AD38D80BAA93EB` | `130B682A714DBE882F05010A7D69A7ADE2A26B0525924FFB8597153E21BA4445` | P1 | Repair environment isolation without skipped assertions or actual bundle use. |
| <a id="a050"></a>[A050](v1-artifact-retention-20260909.md#a050) | [WI-20260817-ATS-023-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-023-evidence-pack.md) | `6A43174C4BA46A3E253C2ECB3E50EA0BFF9C9E52DA6AA027E9B8B69D7D767A08` | `F25D802AB81327CD3D3E0710C24C09EB532F57408FF9AE59846C6B13961D55F3` | P2 | Historical isolated DB and ACL copy plus runtime ready/HTTP200 claim; not current recovery proof. |
| <a id="a051"></a>[A051](v1-artifact-retention-20260909.md#a051) | [WI-20260817-ATS-023-handoff.md](../../deliverables/agent/WI-20260817-ATS-023-handoff.md) | `E2B34E1CAA628216621BD14D80A337A09A795B310EDC8406AB93592D148FFD86` | `463DAA43B965F011662AFFF17BDF0B315D8C28FBB5F31E779E46FCECA894577E` | P2 | Historical handoff internally conflicts on runtime start prohibition versus ready criterion; preserve contradiction, not fresh authority. |
| <a id="a052"></a>[A052](v1-artifact-retention-20260909.md#a052) | [WI-20260817-ATS-027-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-027-evidence-pack.md) | `227CD5C5A2A90836DF11EDE94B41670820F72F72C2CE1B756A75D6FBD65AC00C` | `3C0095005DF5F7D7876DA68EC402BDBC57566847D7153D9F29B0306AB83B3B4D` | P1 | Two-target demo seeding aggregate parity, JDBC admin exception and additive schema claim; later review disputes sufficiency. |
| <a id="a053"></a>[A053](v1-artifact-retention-20260909.md#a053) | [WI-20260817-ATS-027-handoff.md](../../deliverables/agent/WI-20260817-ATS-027-handoff.md) | `A441DFE629224052B92DAF79A2C31142B82FED7D419176E67F9BAD0D8D0856DE` | `024976D885B5C47899D8D1E6E8A57751140C4C2A508DBC4FF3F8A835E9C6329F` | P1 | Two-target ten-track one-album scope and API workflow; JDBC account exception conflicts with parent policy noted by WI028. |
| <a id="a054"></a>[A054](v1-artifact-retention-20260909.md#a054) | [WI-20260817-ATS-028-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-028-evidence-pack.md) | `BE232AB7EF41F824FB653D0E81C39FD3D953B5ED60424DF294FB03C41FE0E4C3` | `BBFFF58B3152FB88D60830E9BEB023EFAB511F3369CC0EAE043BBC5C3C5B3A13` | P1 | Independent FAIL: JDBC account bypass, absent canonical identity/order parity and target-binding proof. |
| <a id="a055"></a>[A055](v1-artifact-retention-20260909.md#a055) | [WI-20260817-ATS-028-handoff.md](../../deliverables/agent/WI-20260817-ATS-028-handoff.md) | `381053F4C55C994F247A6D26BA8D809F40E8EF05A529F74B363D7F8A801F4374` | `7D71AB606A82DE593176FA32396289F35FDB661C223A195E475023FAD53D9E5E` | P1 | Read-only review contract demands concrete isolation/parity evidence, not inference from counts. |
| <a id="a056"></a>[A056](v1-artifact-retention-20260909.md#a056) | [WI-20260817-ATS-029-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-029-evidence-pack.md) | `85D3DFE338BF2EAC8308414B4D767E0E2F16F71CAB64FD55DDA58A94B54E9826` | `72B2C7A3D3CE657EF67B133A9C8CC612FD6364D10AA02CFD2C407771174E6C60` | P1 | 306 mocked UI and 211 H2 tests pass; live Hibernate boot blocked by startup recovery mutation. E1: one redundant terminal LF removed from public derivative only. |
| <a id="a057"></a>[A057](v1-artifact-retention-20260909.md#a057) | [WI-20260817-ATS-029-handoff.md](../../deliverables/agent/WI-20260817-ATS-029-handoff.md) | `BE9E0E6B3E93540BC6123F517D5D0E13715408EEB3C9E74B87DBBBAC7FAC2373` | `B40728B7EDAB51192365FF65ED0342DA102819CB7DA71DFA3AD1A1CD0935ECAF` | P2 | Layer-specific regression verification and mutation-free live-boot gate; generated artifacts retained. |
| <a id="a058"></a>[A058](v1-artifact-retention-20260909.md#a058) | [WI-20260817-ATS-030-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-030-evidence-pack.md) | `FCADF61BC63EF462874B2A4326BF4F0EA16B267D97DA6DF90BB765BFF19DA2C6` | `BFAA50460F65452F6A07A1D25B906DF5DF8393E7DBB785FDB219831DC5622C9C` | P1 | Fresh schema/seed rehearsal boots; no tracks and CDP500 block UI; owned processes stopped, DB/storage retained. |
| <a id="a059"></a>[A059](v1-artifact-retention-20260909.md#a059) | [WI-20260817-ATS-030-handoff.md](../../deliverables/agent/WI-20260817-ATS-030-handoff.md) | `B3DF56143880A7500700FBA405A677CDE5103F94BCDB0A1604779AD679ED4B97` | `24A8BB91B5A4B41B939EDF10056281F6E9E24103A59C39ACE52B92EB21F72B6A` | P2 | Isolated rehearsal only; handoff asks development clone while result is schema/seed clone, preserve distinction. |
| <a id="a060"></a>[A060](v1-artifact-retention-20260909.md#a060) | [WI-20260817-ATS-031-handoff.md](../../deliverables/agent/WI-20260817-ATS-031-handoff.md) | `DF5132D5CF751DA14A54D099B199749267BC864815939E97F78071A8B21C8FC0` | `A4656A87BA1BAFB3E54D552990A247AFCCD8CD15701F05C82A8EEBD1E120F7B3` | P2 | Approved demo-only fixture transfer excludes private storage/payment/tokens; runtime left for parent browser check. |
| <a id="a061"></a>[A061](v1-artifact-retention-20260909.md#a061) | [WI-20260817-ATS-032-evidence-pack.md](../../deliverables/agent/WI-20260817-ATS-032-evidence-pack.md) | `93D113CC61142A1BDB52A55FC894B92999C5C8101B5C14FEA3FF834FE2F4640A` | `DFC6E7B03A2E5508FA664753D189E60D7A71C6609D5BBD30A322949E5FA7F675` | P1 | Corrective MA-supplied observations preserve WI030 partial and WI031 interrupted; CORS runner-only, download event timeout. |
| <a id="a062"></a>[A062](v1-artifact-retention-20260909.md#a062) | [WI-20260817-ATS-032-handoff.md](../../deliverables/agent/WI-20260817-ATS-032-handoff.md) | `E650D9D31ABA7869573F823F9C289CE2310B5EB5EEE69944AEB29DA80B3FA3C3` | `FA109C0075DEE9DBEE2E8512839A68B6CA8983E3E3F8A93166588D7ED6757DF8` | P1 | Record-only supplied browser/API/persistence evidence; no new external acceptance or recovery approval. |
| <a id="a063"></a>[A063](v1-artifact-retention-20260909.md#a063) | [REQ-20260816-ATS-001.md](../../deliverables/user/REQ-20260816-ATS-001.md) | `F440478D25A3D3B731D2449DF91D5EB2C4EE4BD3E38A32B80ADCF42AA90A7B9F` | `C2419BA07F6FE893DC573C4015E196865D0B124AB282A06E1C14C845D343DDEE` | P1 | Original sequential patch and isolated proof approval; original atstudio exclusion conflicts with later amended target authority. |
| <a id="a064"></a>[A064](v1-artifact-retention-20260909.md#a064) | [REQ-20260817-ATS-001.md](../../deliverables/user/REQ-20260817-ATS-001.md) | `B483116299DDDF4CEE62006572DFDA27EE7F076BA24BA8754FC5BC2FA73440EF` | `F7655471CD16CDE75C1F79AF42DDDFCD6D77254B7F22DECC4F2B5F25E0AEB48E` | P1 | Approves correction of Host-variable false classification with fixed loopback one-shot target. |
| <a id="a065"></a>[A065](v1-artifact-retention-20260909.md#a065) | [REQ-20260817-ATS-002.md](../../deliverables/user/REQ-20260817-ATS-002.md) | `47812491BD7577EC1A136C3EC0612478CED2C0D663859861258AD52B983D3BA6` | `11D1F446F172DBDB8AE11A8003EC78162AB9AA1A9B78EA7E1EA3C256BE45522C` | P1 | Two-phase compiler readiness then new DB runner; no inherited retry or broader approval. |
| <a id="a066"></a>[A066](v1-artifact-retention-20260909.md#a066) | [REQ-20260817-ATS-003.md](../../deliverables/user/REQ-20260817-ATS-003.md) | `04A3B62F47DB41BAA695FE3E11C193A83044137B273625D6F5618096732976F1` | `25767EB210DE34506808F28A5FA8E7E6E7A5479E85858AF8BEB18276BC365870` | P1 | Final-attempt approval classifies prior MA interruption separately from technical failure. |
| <a id="a067"></a>[A067](v1-artifact-retention-20260909.md#a067) | [REQ-20260817-ATS-004.md](../../deliverables/user/REQ-20260817-ATS-004.md) | `7E1F38C6DAD8C9E6CF942FDCB786B1E77730EBAF6A6227505ABE0755FDA7B670` | `797211A57E79F96D4C2468A41F1E4DD5FAD049AB1358227EEAFE3E67FD5DA9CC` | P1 | Predicate-specific diagnostic approval before conditional WI006; generic catches must not imply bundle defect. |
| <a id="a068"></a>[A068](v1-artifact-retention-20260909.md#a068) | [REQ-20260817-ATS-005.md](../../deliverables/user/REQ-20260817-ATS-005.md) | `2DF00EFC3E4B496576A5C357D93B86DABAF17A2FAFC6F2C540B4535B1E21480C` | `E2B53F5D950E06BEB8646ED50F6F86407903E7E1AF5E79193B7A4179DD7511E6` | P1 | Correct P5 aggregate with structured hashtable API; preserve false-negative history and one-shot limits. |
| <a id="a069"></a>[A069](v1-artifact-retention-20260909.md#a069) | [REQ-20260817-ATS-006.md](../../deliverables/user/REQ-20260817-ATS-006.md) | `84E5574DEC21FDDC386BA044889D5B69FA3D882BC53B9955333C08029560D66B` | `306A3A390ED85A8EBDDE7AB270997102A1A432332B514F80DB8FBC0425D01071` | P1 | Single read-only orphan inventory plus narrow false follow-up prohibition correction; cleanup separate. |
| <a id="a070"></a>[A070](v1-artifact-retention-20260909.md#a070) | [REQ-20260817-ATS-007.md](../../deliverables/user/REQ-20260817-ATS-007.md) | `9042BA266D9B796E1BA03299EACCAFF9681B664EED73EFC83F05ED3306594417` | `C82776E19A6D10E5C56C0E4A8135CC57767FFB6ADF1D6794440A220C3F61EDCE` | P1 | Reusable supported Inventory action after inconclusive temporary wrappers; count does not establish ownership or deletion right. |
| <a id="a071"></a>[A071](v1-artifact-retention-20260909.md#a071) | [REQ-20260817-ATS-008.md](../../deliverables/user/REQ-20260817-ATS-008.md) | `B4944D6D9958542EED2503747BF68D741632DC8C9AAB62335AF46F1912922416` | `5C6CAA4BEB18A7B880FBE62616DBCE49E9DF1997844088A8043F6D8998400E1D` | P1 | Explicit corrected two-group capture attempt; preserve WI011 unknown result and no cleanup. |
| <a id="a072"></a>[A072](v1-artifact-retention-20260909.md#a072) | [REQ-20260817-ATS-011.md](../../deliverables/user/REQ-20260817-ATS-011.md) | `EBABEB6EF4CB786D6A5ABFDCDA53DFA8FB1C2B27AAA6634CE4BBD67D45F16487` | `4C182015432D10D92F087307A62FA004ECE35A2F3F9D84F3C3975E57BF72799B` | P1 | Two-target supported-workflow seed, exact identity/order parity, credential nonretention and Ultra notice rule. |
| <a id="a073"></a>[A073](v1-artifact-retention-20260909.md#a073) | [WI-20260809-ATS-068-summary.md](../../deliverables/user/WI-20260809-ATS-068-summary.md) | `A8764AF2D484157ECB7A0B605E56A23EC4B82E8DF4C71E5FD5137CB26140399A` | `C5E3A8571DC1E45592BDB356F1CBE02A633FA244EE6FE011B3F4A2482C0FEDAD` | P1 | Approval-facing consent/logout R4 closure with original conditional-review chronology and source-only limits. |
| <a id="a074"></a>[A074](v1-artifact-retention-20260909.md#a074) | [WI-20260816-ATS-001-summary.md](../../deliverables/user/WI-20260816-ATS-001-summary.md) | `6264CA74C66679A7648910D6C86C31785272E6E7654A4442ED17D16374061B93` | `C409A083AE77952645F7C2C1CDB79CAB11A632EFBDE2895DF15620BF137708C5` | P1 | Preflight block and earlier failed attempts; predecessor gate unsuccessful at this checkpoint. |
| <a id="a075"></a>[A075](v1-artifact-retention-20260909.md#a075) | [WI-20260816-ATS-002-summary.md](../../deliverables/user/WI-20260816-ATS-002-summary.md) | `76BCA57D5854D5EE11D022AC1E3884E0A640A4D7586B730E318C64C2D62CDA79` | `A982FEA83438B2993158E3D9D2ED551E715E7E1E3ECD98FF6BC62421BDC5DDA9` | P1 | Operator exception accepted but new isolated proof capture blocked; no DB action. |
| <a id="a076"></a>[A076](v1-artifact-retention-20260909.md#a076) | [WI-20260817-ATS-001-summary.md](../../deliverables/user/WI-20260817-ATS-001-summary.md) | `C3153DDE5205A9EA3DA443D7734E2A8868C51C0ACC4209FFB237FF552BC607A5` | `2842C44D4DE403A2796CB3B2EE5DE0E8EBD5E4AC16C954D6AD225BCA463F8B95` | P1 | Compiler-stop summary preserves zero connection and temporary cleanup. |
| <a id="a077"></a>[A077](v1-artifact-retention-20260909.md#a077) | [WI-20260817-ATS-002-summary.md](../../deliverables/user/WI-20260817-ATS-002-summary.md) | `541667BD029DCD306D4756304E2F86FBBA0EBB949B9ABBBCA37258D12504BE87` | `CB5D19FB9975759E41C11DB8DBC9EC0B23270DE9EA44F8960434C097314D5266` | P1 | Compile readiness PASS permits only successor handoff preparation. |
| <a id="a078"></a>[A078](v1-artifact-retention-20260909.md#a078) | [WI-20260817-ATS-003-summary.md](../../deliverables/user/WI-20260817-ATS-003-summary.md) | `A2F5DAE1A838D3EF549CA05EC96623655E1B09D9A91DFE6351B190814C356FE0` | `2D5F91BDEF60FB2B885F5E520D173A9C1A33640FC1FCF0C22F87C3D47479BFAB` | P1 | Pre-execution blocked summary with all action counts zero. |
| <a id="a079"></a>[A079](v1-artifact-retention-20260909.md#a079) | [WI-20260817-ATS-004-summary.md](../../deliverables/user/WI-20260817-ATS-004-summary.md) | `82224DF29FB4634505C7B75D8204A23CE32536D91B45FF2AB7B686597AE528AB` | `A48BE744924B9DC260FE5F2E03A11EC011CB2D3C7AAEA6F1A5ECB317EB7C1385` | P1 | Final runner compiled; private validation unstarted and zero DB, terminal fail-closed. |
| <a id="a080"></a>[A080](v1-artifact-retention-20260909.md#a080) | [WI-20260817-ATS-005-summary.md](../../deliverables/user/WI-20260817-ATS-005-summary.md) | `EF3AE74132872CFE73D03D1938D2EAB755C389E5EBEC70C2E19AF7BEB4A9FF98` | `D4B3DBB3836D268AAA8E9F4D00AD67D8A4F41506CDFB888272A487D636D16194` | P1 | Predicate P5 failure and prerequisite-coded downstream block with no artifacts. |
| <a id="a081"></a>[A081](v1-artifact-retention-20260909.md#a081) | [WI-20260817-ATS-007-summary.md](../../deliverables/user/WI-20260817-ATS-007-summary.md) | `D4CD78F8E6DAD1513C55A5100168EA62D3E0A3DB41A2DA4B9F9D8126E1BAD6D9` | `26C9E33B2AD59B78D6FF50212EF62C2839FE7E6D2E9746FD3B8FB1FCE8474350` | P1 | Agent false-negative and later operator-exception one-patch evidence kept separate. |
| <a id="a082"></a>[A082](v1-artifact-retention-20260909.md#a082) | [WI-20260817-ATS-008-summary.md](../../deliverables/user/WI-20260817-ATS-008-summary.md) | `A369E27099E23BE914BD4D9D79F08CBED68ED8443205820BD0229BFDD2932647` | `46A14C75EA502D950D35DCA9C197B0DB0A6881443A747C20817309C991186216` | P1 | Observe capture mismatch leaves exact-target removal unknown; no inferred manifest or retry. |
| <a id="a083"></a>[A083](v1-artifact-retention-20260909.md#a083) | [WI-20260817-ATS-009-summary.md](../../deliverables/user/WI-20260817-ATS-009-summary.md) | `8FA24269561610D1AC9D923D2F5B62B2B8DF2439EF40E0B55B8A72D254DA8E65` | `733C78221E4ECA24E91DEEDF8388DDA2CB40831B50AB9FC75D35ABD348AA03F4` | P1 | Blocked inventory retains no count; corrects follow-up authority without rewriting original observation. |
| <a id="a084"></a>[A084](v1-artifact-retention-20260909.md#a084) | [WI-20260817-ATS-010-summary.md](../../deliverables/user/WI-20260817-ATS-010-summary.md) | `8E6170FAFD61E77574EFE6C955BBB74742BE278147583B1A1619777DFB982950` | `DF1FE550A82D7E81FA6861BFFE6BDA34CFE3D4966941A7DC8267CE0FDFF747C2` | P1 | Inventory guard-only companion and count semantics; compile/non-DB verification not live execution. |
| <a id="a085"></a>[A085](v1-artifact-retention-20260909.md#a085) | [WI-20260817-ATS-011-summary.md](../../deliverables/user/WI-20260817-ATS-011-summary.md) | `CD1F8026D584DFBE66FB1C37F8B27AEEB6D15F398B4C20D6932CB4073E48D51E` | `D7DC998EC255348BA81CEFDCC29F753B53CAE8A1F483A2563C529A5A81B8F24C` | P1 | One rejected stream leaves orphan state unknown and no cleanup right. |
| <a id="a086"></a>[A086](v1-artifact-retention-20260909.md#a086) | [WI-20260817-ATS-012-summary.md](../../deliverables/user/WI-20260817-ATS-012-summary.md) | `9A66832D38AD93C61BC10D7D8BBBEF7E3B37D0BB2E13D62B98C797693EF012DE` | `E954322BE5380D502DF73FE3F07AF161E06FDCA1E52ABFB995CC4DA6C825301D` | P1 | Two ordered groups accepted with zero count; limited predicate conclusion. |
| <a id="a087"></a>[A087](v1-artifact-retention-20260909.md#a087) | [WI-20260817-ATS-013-summary.md](../../deliverables/user/WI-20260817-ATS-013-summary.md) | `0A6C0EFBF3B33EF4476D07D9EE947A075D13A1FEAE769733AD33066F2BF86A70` | `0A6C0EFBF3B33EF4476D07D9EE947A075D13A1FEAE769733AD33066F2BF86A70` | P0 | Test-repair summary reports 1614 passed alongside 19 skipped; preserve as original reporting discrepancy. |
| <a id="a088"></a>[A088](v1-artifact-retention-20260909.md#a088) | [WI-20260817-ATS-014-summary.md](../../deliverables/user/WI-20260817-ATS-014-summary.md) | `799603E796E4D0AC900FD25E004690AEC30D69DBEC73989D0B99AF3788DA54B5` | `799603E796E4D0AC900FD25E004690AEC30D69DBEC73989D0B99AF3788DA54B5` | P0 | V7 compatibility and audit outcome bounded to dated installed versions. |
| <a id="a089"></a>[A089](v1-artifact-retention-20260909.md#a089) | [WI-20260817-ATS-015-summary.md](../../deliverables/user/WI-20260817-ATS-015-summary.md) | `560EDE320C6AAD5AAB52E985842EF105229F24A3B80A0BC387B04CD833549F9E` | `560EDE320C6AAD5AAB52E985842EF105229F24A3B80A0BC387B04CD833549F9E` | P0 | Payment/SR documentation corrections and open deployment/provider/backup gates. |
| <a id="a090"></a>[A090](v1-artifact-retention-20260909.md#a090) | [WI-20260817-ATS-017-summary.md](../../deliverables/user/WI-20260817-ATS-017-summary.md) | `C3DF2593A1139DE0D70CC09911E224158EDF3160469648CD585BAA7D7085DFDA` | `C3DF2593A1139DE0D70CC09911E224158EDF3160469648CD585BAA7D7085DFDA` | P0 | Release checklist and six unstaged groups; mixed-worktree hunk review and external gates separate. |
| <a id="a091"></a>[A091](v1-artifact-retention-20260909.md#a091) | [WI-20260817-ATS-019-summary.md](../../deliverables/user/WI-20260817-ATS-019-summary.md) | `5983C37068CC8E07658E878D9A9239DFBAACE281725CFAC6CA99047C48FBDEEE` | `7A7AB9A4700EAA723991AD5267DF4D487CC800356B72CCB00003DA2B9FD3C4FA` | P3 | Branch preservation before deletion and existing Vite conflict; no complete runtime at historical checkpoint. |
| <a id="a092"></a>[A092](v1-artifact-retention-20260909.md#a092) | [WI-20260817-ATS-023-summary.md](../../deliverables/user/WI-20260817-ATS-023-summary.md) | `8BAF2860A06722AC1B92B0B3B88ABCD7030016364558ED9C77974ED53218C7C4` | `DC81FA44F1465F86464F5F5A5907912B60F9C7EC7D6C606B8C3EE58830505B30` | P2 | Separate client DB/bundle ready claim with protected local path; retention requires future approval. |
| <a id="a093"></a>[A093](v1-artifact-retention-20260909.md#a093) | [WI-20260817-ATS-027-summary.md](../../deliverables/user/WI-20260817-ATS-027-summary.md) | `51D5B143B3FF2E8AE8ABF457F988A86710277632AC725D5D3556141A134580DF` | `17BA35588446D051876564160279ADDB42DE4EF51B8080D603BDD93D6771E548` | P1 | Demo aggregate parity and direct local credential storage claim, challenged by subsequent independent review. |
| <a id="a094"></a>[A094](v1-artifact-retention-20260909.md#a094) | [WI-20260817-ATS-028-summary.md](../../deliverables/user/WI-20260817-ATS-028-summary.md) | `3BD2B4E2A8B79FDC3A5B629340FBD3DC3F7E733A1F94BA1A7E924D85FC12DF8C` | `AD2ED5F90E41026A879DCE10400141B67C437DFA2DEFAD0D9D17EF59C9590381` | P1 | Independent FAIL lists account bypass, count-only parity, unbound target evidence and missing per-environment UI. |
| <a id="a095"></a>[A095](v1-artifact-retention-20260909.md#a095) | [WI-20260817-ATS-029-summary.md](../../deliverables/user/WI-20260817-ATS-029-summary.md) | `A1A5AFFB4F7BB2C2527F6914EDD01C2033890B280C36C3B324F9B4D5EC9ECC45` | `92658EB0DD0C17497F11EFEA965ADE3C2F663354D733646B3AC8CA90A2959906` | P1 | Korean summary separates mocked/H2 PASS from mutation-risk live boot BLOCKED. E1: one redundant terminal LF removed from public derivative only. |
| <a id="a096"></a>[A096](v1-artifact-retention-20260909.md#a096) | [WI-20260817-ATS-030-summary.md](../../deliverables/user/WI-20260817-ATS-030-summary.md) | `DBBADDA052B401CCFA54382CDCEC33884039E3F229F2A677B5A780E510DB9B84` | `605556DFCEFF29640FB479AA227F945DC8871FA9989FA7E996C94A3EB6EAB20E` | P1 | Rehearsal partial: fresh seed empty catalogue, CDP500, no authenticated flow or external-effect approval. |

## Verification And Handoff

Scope-specific checks and limitations are recorded in the
[WI018 Evidence Pack](../../deliverables/agent/WI-20260909-ATS-018-evidence-pack.md).
The public rows support independent hash verification without private source
access; original equality and full transformation verification require the
preserved private originals.

WI018/WI019 were integrated and WI020 completed independent review with no open
findings. MA supplied actual publication/clone and final preservation receipts;
see [REQ004](../../deliverables/user/REQ-20260909-ATS-004.md#ma-delivery-receipt)
for their precise source-only limits and the unresolved private backup. This
closeout changes no original/public identity row or historical derivative.

Rollback, if separately approved, is limited to removing this WI's 83 added
derivatives and its new records, and reversing its specific registry edits.
Keep all 13 retained files, unrelated worktree changes and private originals.
For the published records, use a narrowly scoped Git reversal through MA;
do not rewrite historical receipts, reset the shared worktree or recover raw
SQL into an executable path. No rollback or deletion was performed by WI018.
