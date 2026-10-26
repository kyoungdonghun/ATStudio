---
version: 1.1
last_updated: 2026-10-10
project: ATS
owner: tr
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261010-ATS-004.md
    reason: Approved pre-purchase research scope
  - path: ../agent/WI-20261010-ATS-006-handoff.md
    reason: Bounded research assignment
  - path: ../agent/WI-20261010-ATS-006-evidence-pack.md
    reason: Dated sources and verification boundaries
---

# WI-20261010-ATS-006 Summary

## Conclusion

**A single VPS is a defensible initial topology for small public operation, not only a friends-only demonstration. It is not high availability or a capacity guarantee.** This is TR's conditional assessment of the planned architecture, not an industry-wide server-size standard or approval to buy the selected product.

The comparison input is one Nginx/static React host with Java 17/Spring Boot 4, MySQL 8, one FFmpeg conversion worker, local originals/128kbps MP3 derivatives, and external backups, on a proposed 2 vCPU / 8 GB / 100 GB VPS. The [deployment bundle](../../deploy/single-vps/README.md) supplies this planned topology; SA owns current source sizing, PG operational gaps, and MA iwinv specifications/costs. TR did not benchmark a host or inspect account settings.

Verification: **2026-10-10 KST**, public official documentation and local governing documents; evidence owner **TR**. Nine primary references, including the authorized Ubuntu security-notice follow-up, are mapped in the [Evidence Pack](../agent/WI-20261010-ATS-006-evidence-pack.md#primary-source-register). Existing TEST successes remain valid within their original environment.

## Practice Comparison

| Area | Initial operation | Later trigger / limitation |
|---|---|---|
| Application and DB on one host | Reasonable conditional starting point with explicit resource budgets and accepted maintenance downtime. AWS explicitly says not every Lightsail application needs a load balancer. This supports a staged approach, not a claim about competitors' infrastructure. [AWS](https://docs.aws.amazon.com/lightsail/latest/userguide/understanding-lightsail-load-balancers.html) | Separate DB/shared media and multiple application instances when measured contention or availability requirements justify them. An LB alone does not remove a single DB/host failure point. |
| Shared-host MySQL memory | Avoid `innodb_dedicated_server` on this shared host. Its dedicated-host sizing can allocate 75% of detected memory above 4 GB, unsuitable to copy blindly beside Java and FFmpeg. [MySQL 8.0](https://dev.mysql.com/doc/refman/8.0/en/innodb-dedicated-server.html) | SA must budget actual peak process memory, not just Java heap or buffer pool. Adjust or separate workloads after measurements; 8 GB alone proves no user count. |
| Protected media delivery | Keep controller-mediated public listening and entitlement-checked original downloads. Do not expose protected directories through a public Nginx alias. Nginx supports internal-only locations for a possible future authorized `X-Accel-Redirect` design. [NGINX](https://nginx.org/en/docs/http/ngx_http_core_module.html#internal) | That offload is an option requiring application/security tests, not implemented by this WI. It does not require moving media to Object Storage. |
| Large uploads and conversion | Proposed safeguards: bounded conversion concurrency/queue and temporary-disk headroom. Verify Nginx body limits and timeouts against application uploads; its default body limit is `1m`. [NGINX](https://nginx.org/en/docs/http/ngx_http_core_module.html#client_max_body_size) | Test browsing/playback/download during conversion and backup. A one-worker setting alone is not a CPU ceiling. Split conversion only if measured contention or backlog warrants it. |
| Backups and recovery | Retain external, encrypted, restorable DB + media + required settings/key material under project policy. Define acceptable data loss (RPO) and recovery time (RTO), then test recovered content and elapsed time. A successful backup job alone is insufficient. [AWS recovery guidance](https://docs.aws.amazon.com/wellarchitected/latest/framework/rel_backing_up_data_periodic_recovery_testing_data.html) | Shorter RPO/RTO may require more frequent recovery points, replication or standby resources. Neither backup nor Object Storage automatically provides live application failover. |
| Monitoring | Observe latency, traffic, errors and saturation. For this workload, include playback/download bytes and sessions, API tail latency, CPU/RAM/disk pressure and conversion backlog, with an external availability check and actionable alerts. [Google SRE](https://sre.google/sre-book/monitoring-distributed-systems/) | Repeated objective breaches, shrinking disk runway or transfer-budget risk trigger investigation and an explicit capacity/cost decision; do not silently upgrade. Logs alone do not establish service health. |
| CDN / primary Object Storage | No evidence here makes either a day-one prerequisite. Local media with an external backup is the existing project choice. This is TR's conditional recommendation, not a universal rule. | Consider after measured origin-transfer pressure, latency, media growth or multi-host requirements. Preserve entitlement/cache boundaries and compare storage, request, egress and migration costs before approval. |

The comparison does **not** mandate RDS, a paid managed database, Kubernetes, a CDN or a load balancer for every production site. It also does not waive authentication, HTTPS, restricted management access, updates, backups, monitoring or public-release tests; PG owns their actual implementation audit.

## Version Gate

1. **MySQL 8.0 has distinct upstream and Ubuntu maintenance paths.** Oracle records Sustaining Support from **2026-04-21**. [Oracle notice](https://www.mysql.com/support/eol-notice.html) Ubuntu's **USN-8700-1, published 2026-08-31**, explicitly records backported security patches for Ubuntu 24.04 and lists `mysql-server-8.0` version `8.0.46-0ubuntu0.24.04.4`. This is dated evidence of continued Ubuntu-supplied 8.0 patching after Oracle's transition, not a guarantee about the target host or indefinite future coverage. [Ubuntu notice](https://ubuntu.com/security/notices/USN-8700-1) **Do not conclude that new 8.0 installation is prohibited or that 8.4 is mandatory solely from Oracle's notice.** Before deployment, verify the target's actual APT sources, candidate/installed version and security-update policy. The notice's version is a historical fixed version, not a claim about today's latest candidate. No automatic version change or separate DB purchase is authorized.
2. **Ubuntu defaults differ from the planned Java version.** Ubuntu 24.04's release notes name OpenJDK 21 as the default while retaining Java 17 support. Install/select the approved Java runtime explicitly. The release notes state OS security maintenance through **2029-05-31**; this is not a blanket lifecycle guarantee for every package. [Ubuntu release notes](https://discourse.ubuntu.com/t/ubuntu-24-04-lts-noble-numbat-release-notes/39890)
3. **Java 17 is not inherently incompatible with Spring Boot 4.0.** The current 4.0 requirements page states Java 17 minimum. Its patch release is not proof of the project's exact dependency tuple: record and test the actual artifact/runtime versions. [Spring Boot requirements](https://docs.spring.io/spring-boot/4.0/system-requirements.html)
4. **FFmpeg package/encoder check remains target work.** No exact Ubuntu FFmpeg version or encoder availability is claimed from these sources. Existing project evidence calls for external FFmpeg with `libmp3lame`; verify encoder availability and an actual 128kbps conversion on the approved host.

## MA Handoff

- Keep the single-VPS candidate unless SA/PG/provider evidence establishes a material blocker; this research cannot approve its measured capacity or purchase.
- Confirm the actual Ubuntu MySQL package source/candidate and update policy before provisioning the production database. P9 provides positive distribution-patching evidence; package availability, compatibility and target maintenance still require separate checks, not an automatic 8.4 migration.
- Before public release, require a finite mixed-workload test, protected-path/Range checks, external alert test and isolated DB/media restore against agreed objectives.
- **WI006 research complete; Blocks WI-20261010-ATS-007.** MA coordinates synthesis after WI004, WI005 and provider findings. Overall REQ, purchase and production remain outside this WI's completion.

## Related Documents

- [Current decision entry](../../docs/index.md#current-decision-entry)
- [Security policy](../../docs/policies/security-policy.md)
- [Evidence Pack](../agent/WI-20261010-ATS-006-evidence-pack.md)
