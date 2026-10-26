---
version: 1.1
last_updated: 2026-10-10
project: ATS
owner: tr
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261010-ATS-006-handoff.md
    reason: Approved bounded assignment and output contract
  - path: ../user/REQ-20261010-ATS-004.md
    reason: Investigation authorization without purchase
  - path: ../user/WI-20261010-ATS-006-summary.md
    reason: User-facing practice comparison
---

# Evidence Pack: WI-20261010-ATS-006

## Summary

Compared the planned single-VPS music storefront against nine official primary references, including an authorized follow-up on Ubuntu security maintenance. Single-host operation is conditionally reasonable, but not HA or a measured capacity result. MySQL lifecycle checks distinguish Oracle's support stage from positive evidence of Ubuntu-supplied patches.

## Scope / DoD Check

- [x] Nine dated primary references, narrow supported claims and direct URLs; P9 added under explicit follow-up approval.
- [x] Initial versus later requirements, including protection, recovery and monitoring.
- [x] No universal server size or mandatory CDN/managed DB/LB claim.
- [x] Version availability, application compatibility and maintenance distinguished.
- [x] No account access, purchase, provider contact, runtime/DB changes or benchmark.
- [x] Only the assigned summary and Evidence Pack created.

Evidence date: **2026-10-10 KST**. Owner: **TR**. Environment: local Windows repository read-only inspection and public web retrieval. Branch/HEAD at inspection: `main`, `58b1011dad796017e47dbcc82a4225ca449bcdf6`. Existing dirty/untracked work was present and preserved. No deployed-artifact identity was checked.

## Reference Documents (Tier 0-2)

MA supplied the assignment/Tier 0 constraints; the following on-disk pointers were consulted. Skills: `create-wi-evidence-pack` and `validate-docs`.

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, privacy, bounded recommendations |
| 0 | `docs/standards/documentation-standards.md` | Metadata, English docs, sources |
| 0 | `docs/standards/development-standards.md` | Traceability and validation boundaries |
| 0 | `docs/standards/glossary.md` | Public Listening versus Official Download |
| 1 | `docs/policies/security-policy.md` | Existing media/privacy contract; no secret access |
| 2 | `docs/index.md#current-decision-entry` | Existing TEST evidence and production HOLD |
| 2 | `deploy/single-vps/README.md` | Planned topology; not live-host evidence |
| Context | `deliverables/user/REQ-20261010-ATS-004.md` | Scope and WI chain |
| Context | `deliverables/agent/WI-20261010-ATS-006-handoff.md` | Assignee/output limits |

Assignee: TR; task type: research. Handoff exists and the current request expressly approves this bounded two-document assignment. Source sizing belongs to SA, operational gap inspection to PG, and iwinv billing/product facts to MA. Those results are not duplicated or assumed here.

## Primary Source Register

All nine sources were retrieved **2026-10-10 KST**. P1-P8 belong to the initial research; P9 was independently opened in the authorized lifecycle follow-up. These are vendor/practitioner primary documents, not a representative survey of competitors. No long verbatim passages were copied.

| ID | Direct primary URL | Narrow evidence / locator |
|---|---|---|
| P1 | [AWS Lightsail load balancing](https://docs.aws.amazon.com/lightsail/latest/userguide/understanding-lightsail-load-balancers.html) | "Recommended applications" says an LB is not needed by every application; multi-instance LAMP uses centralized DB and may use shared media. Supports staged topology, not iwinv sizing. |
| P2 | [MySQL 8.0 dedicated-server configuration](https://dev.mysql.com/doc/refman/8.0/en/innodb-dedicated-server.html) | Section 17.8.12 discourages dedicated-server autosizing when resources are shared; table 17.8 uses 75% above 4 GB. Not an ATStudio memory measurement. |
| P3 | [NGINX HTTP core](https://nginx.org/en/docs/http/ngx_http_core_module.html) | `internal` rejects external access and allows upstream internal redirects; `client_max_body_size` defaults to `1m`; `limit_rate` applies per request. Not evidence of current effective proxy settings. |
| P4 | [AWS REL09-BP04 recovery testing](https://docs.aws.amazon.com/wellarchitected/latest/framework/rel_backing_up_data_periodic_recovery_testing_data.html) | Validate restored data, integrity/access and measured recovery against RPO/RTO, periodically. AWS implementation examples do not require buying AWS services. |
| P5 | [Google SRE monitoring](https://sre.google/sre-book/monitoring-distributed-systems/) | Four golden signals and black-box/white-box distinction; audio traffic can use network I/O or concurrent sessions. Not a prescription to deploy Google's infrastructure. |
| P6 | [Ubuntu 24.04 release notes](https://discourse.ubuntu.com/t/ubuntu-24-04-lts-noble-numbat-release-notes/39890) | "Support lifespan" gives 2029-05-31; "OpenJDK" gives default 21 with 17 supported. Does not establish every third-party package's lifecycle or the provider image contents. |
| P7 | [MySQL support notices](https://www.mysql.com/support/eol-notice.html) | 2026-04-21 notice places MySQL 8.0 in Oracle Sustaining Support and encourages a newer LTS. No inference that Ubuntu's distribution support is identical. |
| P8 | [Spring Boot 4.0 requirements](https://docs.spring.io/spring-boot/4.0/system-requirements.html) | Retrieved page describes 4.0.8 with Java 17 minimum; project runtime patch/dependency compatibility still needs exact-version testing. |
| P9 | [Ubuntu USN-8700-1](https://ubuntu.com/security/notices/USN-8700-1) | Published 2026-08-31. Details report backported patches for 24.04; Update instructions list `mysql-server-8.0` `8.0.46-0ubuntu0.24.04.4`. Positive distribution-maintenance evidence after P7's transition, not target APT state or indefinite coverage. |

Retrieval note: an initially attempted AWS recovery URL returned an internal error. P4 is the successfully opened canonical framework page; the failed URL is not evidence. Ubuntu release notes contained no FFmpeg match, so exact package/encoder assertions were excluded. Search results and other versions were not substituted for the version-specific MySQL 8.0 manual.

## Evidence Pointers

- `deliverables/user/WI-20261010-ATS-006-summary.md`: Conclusion, Practice Comparison, Version Gate and MA Handoff.
- `deliverables/agent/WI-20261010-ATS-006-evidence-pack.md`: Source register, inference boundaries and reproducibility.
- `deploy/single-vps/README.md`: Section 1 planned one-host/one-worker topology; Section 2 approval sequence.
- `docs/index.md`: Current Decision Entry and 2026-10-03 Stage A Closeout; historical Gmail/WAV results are not reset by this research.

## Interpretation and Boundaries

The small-public-operation recommendation is an inference from the planned workload and conditional architecture guidance, not a quotation or quantified industry norm. No private competitor infrastructure was inspected. No exact traffic/customer/concurrency ceiling is established by these sources.

Recommended shared-host checks are engineering applications of P2/P5: total process memory and headroom, mixed-workload tail latency, OOM/swap, disk/temp growth, conversion backlog, network bytes and recovery duration. CPU conversion load and one-worker behavior require measurement; no codec throughput is inferred from the 128kbps output bit rate. Numeric acceptance/alert thresholds must come from the approved workload and objectives, not arbitrary "industry standard" percentages.

NGINX offload is optional future work. Authorization must precede protected original delivery, while public listening remains public but controller-mediated. P3's per-request transfer limit is not an aggregate host/monthly spend cap. CDN/cache or shared storage changes need separate entitlement, origin-access and cost review; no migration is authorized.

MySQL's support notice requires **target package/support verification**, not rejection of every new 8.0 installation or a mandatory 8.4 migration. P9 supplies the previously missing positive Ubuntu maintenance evidence; P7 must not be generalized to that distribution channel. Still verify actual APT sources, candidate/installed version and security-update policy on the approved host. P9's fixed-version entry is not evidence of the latest candidate, installed patches, all-CVE coverage or a promised future support period. A newer DB release would require separately approved project/restore compatibility tests. This WI made no package changes.

### Authorized Lifecycle Follow-up (2026-10-10)

MA supplied P9 and authorized changes only to these two owned records. TR independently opened the notice, added source P9 and revised the summary/current finding above. This supplements the initial eight-source result without changing Oracle's historical notice, the planned topology or any target version. Actual host package inspection remains NOT RUN.

## Commands & Outputs

- `git status --short --branch`: `main...origin/main`; pre-existing tracked and untracked changes observed, not reverted.
- `git rev-parse HEAD`: `58b1011dad796017e47dbcc82a4225ca449bcdf6`.
- `Get-Content` / `rg`: read assigned handoff, approved REQ, relevant governing documents and current-entry pointers; no private files read.
- Initial public web search/open/find: P1-P8; no provider account interaction.
- `apply_patch`: create only the two assigned deliverables.
- Authorized follow-up: public `web.open` of P9 and `apply_patch` edits to the same two files only; no account access, APT execution, package update or automatic version change.

## Tests

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS, exit 0; Tier 0, internal links, index and 746 traceability IDs passed.
- `git diff --check`: PASS, exit 0; existing CRLF conversion warnings only. Since these two deliverables are untracked, a separate PowerShell check also confirmed both files are ASCII with no trailing whitespace.
- Lifecycle follow-up rerun: documentation validator PASS (746 IDs), `git diff --check` exit 0, and both owned files' ASCII/trailing-whitespace checks PASS; no new validation failures.
- Application tests, package installs, live Linux compatibility, SSH, restore and load tests: **NOT RUN**, outside research scope.

## Risks / Rollback

- Public documentation changes over time; recheck version/support facts during named-target provisioning.
- Single-host recovery entails downtime; business acceptance of recovery objectives is required, not implied.
- Actual resource sufficiency, security completeness and iwinv costs remain with SA/PG/MA and later target evidence.
- Rollback: correct or supersede only these two research documents via reviewed edits. No runtime rollback is necessary; no application/infrastructure state was changed. Do not delete unrelated files.

## Follow-ups

**WI006 research complete; Blocks WI-20261010-ATS-007.** MA coordinates immediate synthesis when WI004, WI005 and provider research are ready. This TR assignment neither closes the overall REQ nor approves purchase, public deployment or architecture changes.

## Related Documents

- [WI006 summary](../user/WI-20261010-ATS-006-summary.md)
- [WI006 handoff](WI-20261010-ATS-006-handoff.md)
- [Approved REQ](../user/REQ-20261010-ATS-004.md)
