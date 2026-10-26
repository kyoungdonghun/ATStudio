---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-028-handoff.md
    reason: Approved four-file ownership and receipt gate
  - path: WI-20261010-ATS-025-evidence-pack.md
    reason: Initial bootstrap candidate and correction history
  - path: WI-20261010-ATS-026-evidence-pack.md
    reason: Local artifact preparation and approved frontend completion
  - path: WI-20261010-ATS-027-evidence-pack.md
    reason: Independent bounded-sequence disposition
---

# Evidence Pack: WI-20261010-ATS-028

> Purpose: Integrate actual MA receipts without treating local checks as native
> success or incomplete bootstrap as deployment approval.

## Summary

TL;DR: **REQ008/WI028 complete in the approved preparation scope; native result
BOOTSTRAP_APPLIED_NOT_RUNNABLE; production HOLD.** MA applied reviewed v3 once
at22:17:00KST, independently rechecked observations/grants/receipt and stopped/
remasked MySQL. Final host22:17:35KST and Windows cleanup22:17:53KST receipts
close this window. JAR/SPA remain local; no app/ADMIN/mail/public activation.
All earlier HOLD/pending checkpoints below are historical and superseded by
the final v3 native and cleanup sections, not erased or claimed successful.

Evidence owners: SE preparation, QA local builds, PG independent local review,
MA actual host/Windows operations, DOCOPS local identity/documentation checks.
Date 2026-10-10; local Windows/PowerShell shared checkout; MA's existing approved
Ubuntu24.04 target only. DOCOPS ran no remote command or native database check.

## Scope / DoD Check

- [x] Existing handoff and all four injected Tier 0 documents confirmed.
- [x] WI025/026/027 initial completion and frontend addendum reconciled.
- [x] Actual pre-mutation HOLD and user-approved correction recorded separately.
- [x] Four owned documents only; historical receipts and hashes retained.
- [x] Corrected candidate and final PG disposition integrated.
- [x] Final native outcome integrated.
- [x] Final MySQL/session/key/agent cleanup for the completed window received.
- [x] Final scoped document validation and history-preservation checks recorded.
- [x] Explicit next-WI responsibility and production exclusions retained.

Write set: docs/index.md, REQ008, WI028 user summary and this Evidence Pack.
No source, template, runbook, handoff, prior Evidence Pack, Git index, secret or
runtime modification. No descendants, package install, commit or push.

## Reference Documents (Tier 0-2)

| Tier | Pointer | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md | Fully injected STD-001/004/002/005; approval, language, evidence and terminology |
| 1 | .claude/agents/docops.md; docs/policies/security-policy.md | Injected role and sensitive-output boundary |
| 1 | docs/policies/access-control-policy.md; quality-gates.md | Read locally; least privilege and bounded results |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md; validate-docs/SKILL.md | Read locally; handoff precondition, two outputs, scoped checks |
| 2 | docs/standards/evidence-pack-standard.md; docs/templates/eval-report-template.md | Read locally; reproduction, result and evaluation |
| 2 | docs/index.md; deploy/single-vps/CONFIGURATION.md; runbooks/deployment.md; runbooks/production-bootstrap.md | Current entry, configuration provenance and native/startup gates |
| 2 | scripts/reconstruction/README.md; docs/design/db-schema.md | Target/manifest and recovery boundaries, relevant sections searched |
| Context | REQ008; WI024, WI014, WI025/026/027 Evidence Packs; WI026 artifact.json | Approved scope, dated history, local completion and actual-receipt integration |
| Conditional | test/SKILL.md; build-check/SKILL.md; react-best-practices/AGENTS.md; frontend-standards.md | Handoff pointers only; no test/build or frontend edit by DOCOPS |

ATS tag and docops required tiers [0,1] checked in workspace.json and
context-injection-rules.json. Source pointers from the handoff were used through
the public hash inventory and completion evidence, not as permission to inspect
private configuration. Memory was a navigation hint; current CONFIGURATION/WI014
supersede older planning-only resource notes. No memory was changed.

## Drift Report and Patch Scope

| Finding | Impact / priority | Owned correction |
|---|---|---|
| Current entry ended at WI024 and REQ008 retained only its initial state | High: new native failure or completed local build could be missed | Add newest checkpoint; retain older dated entries and initial REQ line |
| WI026 opening HOLD predates its approved completion addendum | High: reading the prefix alone would misstate SPA readiness | Link the addendum and current manifest; leave original evidence unchanged |
| Initial WI027 PASS predates native NULL rejection and correction | High: stale approval could be applied to changed bytes | Preserve original hashes/PASS, require new candidate and PG disposition |
| WI024 cleanup belongs to the preceding window | High: temporary access could be mislabeled closed | Keep this window's final cleanup pending until MA receipts arrive |

No category document was added; category counts are unchanged. Root index links
the live REQ/receipts, while two-set deliverables serve separate audiences.
No policy or Tier 0 reshuffle is in scope. Existing optional PAM/AppArmor
warnings remain historical, not a new security finding or a claimed repair.

## MA Receipt Ledger

Supplied MA observations below are not DOCOPS reruns or imported raw logs.
No host endpoint, raw host identity, credential, private configuration body or
secret-derived authentication hash is included.

| Checkpoint | Actual observation | Limit / current effect |
|---|---|---|
| 21:34 KST initial access | Strict-pinned administrator SSH/sudo; same boot Ubuntu24.04, MySQL8.0.46, Python3.12.3; MySQL/Nginx masked inactive; no app unit/env/current release; about97GB free | Snapshot, not capacity or final cleanup |
| Initial config/capture checks | Merged mysqld validate-config exit0; root client files and persisted override absent; no enabled sudo I/O logging found | Bounded observation, not secret-file reading or global capture guarantee |
| 21:49 KST capture check | No matching active remote rsyslog forwarding, journal-upload absent/inactive, no named user-space capture/forwarding processes; app/DB/proxy writers absent | MA-attributed, not exclusion of privileged provider monitoring |
| Initial local transfer archive | 200704 bytes; exact16 public members/source hashes reported, initial bundle below | Archive preparation alone was not server transfer |
| Native staging, later receipt | Trusted root archive extraction, all16 hashes and native plan PASS | Initial staging preserved; not a reviewed-v2 receipt |
| 21:54 KST native start | MySQL temporarily started loopback-only | Approved maintenance lifecycle, not automatic service enablement |
| Native guard HOLD | `MYSQL_CONFIGURATION` before approval JSON or any DB/account/secret creation | No apply success or partial initialization inferred |
| Read-only diagnosis | Other checked predicates1; admin_address empty-string comparison NULL; admin_address IS NULL1; named DB/account absent and approval/env/claim/receipt absent | Actual native disabled-address representation; synthetic PASS did not prove compatibility |
| Failure-window cleanup | MySQL stopped/remasked, confirmed exit0; old staging preserved | Any later start needs its own stop/remask receipt; final agent/session cleanup still pending |
| Subsequent user decision | Narrow NULL-guard correction and independent re-review approved; MA reopened WI025 through skill handoff/new SE | Proposed COALESCE predicate and regression; no code change by DOCOPS |
| Proposed corrected staging | `/root/wi025-v2`, old stage unchanged | PG review and corrected hashes pending; no approval reuse across changed bytes |
| WI025 correction completion, subsequent receipt | One-line NULL-safe predicate;34 local synthetic tests PASS,32 existing plus2 new; corrected bundle below; MA reports new local archive prepared only | Prior39 validator tests not rerun; final PG re-review and native semantics still pending |
| WI027 corrected FINAL PASS, subsequent receipt | PG independently retested34/34; new bundle/helper and exact fresh v2 staging/native sequence accepted | Old execution identity superseded, history retained. Native SELECT expected1,1,0,0,0,1 plus full preflight required before one apply |
| Second native preflight, later MA receipt | Corrected stage, native plan and NULL truth table1,1,0,0,0,1 PASS; ACTIVE AUDIT plugin guard refused sha2_cache_cleaner and mysqlx_cache_cleaner | Closes native NULL semantics only; no full preflight/apply PASS |
| Second HOLD target observations | Named DB/account absent; env/approval/claim/receipt absent; components none, replication0, loopback3306 only, no X/admin port | No DB/account/secret creation and no partial bootstrap inferred |
| Second HOLD cleanup and review | MySQL stopped/remasked; PG read-only built-in cache-cleaner classification review underway | No approval for another guard fix; no charge; final agent/session cleanup still pending |
| Subsequent precise-plugin approval | PG classification advisory complete; user approved exact identification of the two built-ins; WI025 reopened via skill addendum | Supersedes earlier no-fix-approval state only. Implementation/tests, independent re-review and new native gates still required; no apply |
| WI025 v3 completion | Exact BINARY PLUGIN_NAME plus SQL NULL library classification;37/37 local tests PASS; new bundle below | MA triggered independent WI027 re-review immediately. New archive local preparation only; no target DB/apply |
| Final native outcome and access cleanup | PENDING, owned by MA and integrated through WI028 | Do not finalize until success or accurate HOLD and final cleanup are supplied |
| Final PG v3 review |37/37 independent tests PASS and exact v3 procedure accepted | Local review only; subsequent rows establish actual native results |
| V3 staging / a93d3e,22:15:44KST | Trusted root ancestry,16 exact regular files, hashes and native plan PASS; old v1/v2 helper hashes unchanged | New v3 archive identity below; previous stages preserved |
| Upload precheck refusal | Expected home750 differed from actual700; stopped before mkdir; metadata confirmed safer700; corrected probe created fresh700 upload directory then SCP | No destructive operation or SQL retry; no weakened permissions |
| Native preflight /5f6c5a,22:16:29KST | NULL truth1,1,0,0,0,1; exact plugin predicate37 fixture cases,0 mismatches/unknowns; full host/DB guards PASS; fresh root0600 approval enrolled; PREFLIGHT_ONLY | Approval matched v3 bundle; preflight-window stop/mask PASS; no apply yet |
| Exactly one apply /3f3470,22:17:00KST | BOOTSTRAP_APPLIED_NOT_RUNNABLE;43 tables,6 exact plans,0 non-plan rows;520 columns,234 index rows,91 FK rows | Fresh atstudio_prod and loopback atstudio_app only; server-only DB/JWT/billing secrets; runtime TCP NOT_RUN, canonical manifest UNRECORDED |
| Post-apply read-only verification | Independent re-import observe/verify_grants PASS; safe receipt matches; account unlocked, exactly4 nongrantable DML; private files root0600/single-link | Env body not read; no TCP/JDBC authentication or Hibernate proof |
| Post-apply service cleanup | MySQL stop/mask, no pending job and no3306/33060/33062 listeners PASS | Nginx/app not started; no public ports opened |
| Final host /07f459,22:17:35KST | Fresh strict-pinned SSH, same approved boot; MySQL/Nginx exact /dev/null masks inactive; app unit not-found; no80/443/8080/3306/33060/33062 listeners or relevant jobs; sshd -t PASS | /etc/atstudio root:root700; env/claim/receipt/approval root:root600/single-link; no env body read |
| Final Windows /c5ac1e,22:17:53KST | Exactly approved key removed via ssh-add -d with public-key selector; agent empty confirmed rc1; hidden elevated cleanup0; fresh Win32_Service Stopped/Disabled | All MA command sessions complete; supersedes pending cleanup above; not reused WI024 evidence |

The Windows agent was temporarily activated and the user unlocked only the
approved existing key. No passphrase was supplied to DOCOPS. New-window key
removal, empty-agent confirmation, Stopped/Disabled and session closure are
established by the final receipt above, not earlier WI024 completion.

## Local Evidence and Historical Identities

Branch main, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; dirty/untracked
before this WI. DOCOPS tools: PowerShell7.6.5 and Python3.14.3. QA toolchain and
execution dates remain QA-attributed in WI026: JDK17.0.12, Gradle9.3.0,
Node24.14.0, TypeScript5.6.3, Vite6.4.3, React18.3.1. No build rerun here.

At 21:54:38 KST DOCOPS rehashed all974 recorded source inputs,143 previous-dist
files, two preserved JARs, current executable JAR and143 new SPA files. All
matched WI026 manifest; new output count143 matched. This checks the recorded
set and SPA count, not clean-room reproducibility, extra source-file absence,
general secret DLP, browser behavior or target readiness.

Initial WI025 synthetic32 and existing39 tests passed; PG independently repeated
them. QA's focused backend57 passed; frontend typecheck/isolated build passed
at21:44:58-21:45:12KST. DOCOPS did not repeat tests. Full suite, coverage, target
TCP/JDBC, Hibernate, browser, load and Provider/mail tests are not this PASS.

| Object at initial integration | SHA-256 |
|---|---|
| Initial approved WI025 bundle, before native correction | 891578294cbb621256ad9324dbaee9d5ed2efe3e2f51fbb22b33d2972de8e80c |
| Initial MA public archive, MA-reported | 7ab722b888c53c816a99becd615498c0609bba343ffeb2206bac7ce383b2c973 |
| Initial production-bootstrap.py | BF5CB781792B2DA39ACDD2CAB029926CBA94F8DBE40676BC5121BE0BD94E86A2 |
| Initial test_production_bootstrap.py | 9512FDB606D5AAEE763EACBF9AD84F5C5D8C1B16AEA3F6FAB3436C06E858E490 |
| Initial production-bootstrap.md | 14A0C00BE2BD88793B9F326CAA817FA69E89F50B1D5BD461D2ADF19CA33AE080 |
| validate_bundle.py | 19361E515E694718B509E28C6223A3FFC0BF647DDF6BB972F8AA4904CC1CB593 |
| schema.sql | 6FB7DF1518394371BBFC5513DF5EBE2C97E067E7F77A1F20661AFB1C00B63FB7 |
| seed.sql | C848E0DA6B9B35EB26563D6AF3666B67FC119430C1AFD888EA7B0246800A579A |
| application.yml | A493FFE54DA9DA77B56009338438F9C0718A2CA45CAD1C21241548E1052C9709 |
| Current executable JAR | 517B745479792A7EAE6CE75357C7D1299AB644B50D5BC5B331FEA384CCB10E0A |
| New SPA sorted-record aggregate, QA/PG receipt | 43A0384C5F48D100E4AF6B20D989A2186B34E6900CF4E69C94B010E9671C8647 |
| WI026 final artifact.json | FBEB74425ADE0FE3882A9DDDC4C97F7B67B75DDA711FFA37ACBD755A02FA4D9C |
| WI024 Evidence Pack | 846DE832115EDAD8D24D95297CE088F896A8EDC76197AA4C1AA21E710A1A216C |
| WI025 initial Evidence Pack | C7DFA5C083DF72E64A25BC2D59B94EF74D25FEE0513FA88DC8449362615B7006 |
| WI026 completed Evidence Pack | 7C0544EA0F4C1EFCC5B2D49CE93495ED850C388754AE81174069AD7AF47ED8F8 |
| WI027 initial Evidence Pack | EA6EF883574CDB0E3C5DC87A9600B7F60D959BB246A1193C94AE946D790EB462 |
| WI028 handoff | EFFFFEF5ADB591ED4C2C2A6E9ABF661B68911780BA8475C108C701282DC28F06 |

All local per-file hashes above were read by DOCOPS before the approved
correction; MA archive and aggregate bundle/SPAs are attributed as indicated.
Later correction receipts must append new identities, not replace these rows.
No private secret/env hash was computed. WI026's original manifest hash and
report-prefix hashes remain in its preserved completion addendum.

Before integration, docs/index.md SHA was
`8EFFFF19387E55AC6D1002D5789F64F1402211637739EC06251867B52333A067`;
REQ008 SHA was `204AD06D2050590180FD34DD25A2645BCBA24EED54E275ABC1EACB0024766007`.
The index suffix beginning with the OS Maintenance heading, normalized CRLF to
LF for history preservation checking, hashes to
`F59FD64AB6C5615A909A101968C017B6B0F5618AAA279848E8065090EB51FB47`.

### Corrected Candidate Checkpoint

DOCOPS read WI025's appended correction record and rehashed these public files.
Initial rows above remain the identities of the refused native candidate.
Replacement SE owns the34-test result (RED1 failure before correction, GREEN34
after); the old39-test result remains historical. No DOCOPS test rerun.

| Corrected object | SHA-256 |
|---|---|
| Bundle, SE offline plan receipt | 15aa91639be237e03c71b7f2df2b4923879b849981b9abdf71efda19aedea91d |
| production-bootstrap.py | 28E473A075E4C48D70B542CBAA36CEFBEDC48A9F0B5102FFA276430B58750191 |
| test_production_bootstrap.py | 44746325FBACE0B58821384160DF0ADBC717F2FC4ABA0A5A50128341ADDF4D73 |
| production-bootstrap.md with appended correction | E04DA5BC53AADAD843161EA49251EF1F47960297BB8F60E4C0A18127A65D376D |
| WI025 Evidence Pack with appended correction | 84D83958DFEB8D717BB0F1C2F205524619BE5C9DFAC9432E622273C0885E6FB7 |

Only local archive preparation is MA-reported at this checkpoint. Final PG
review, native NULL/empty/nonempty SELECT-only truth table, fresh target gates
and any apply result remain pending. No target DB/account/secret yet; MySQL
remains masked per MA. Old staging is preserved, not replaced by the new plan.

DOCOPS independently matched the first14496 bytes of the corrected WI025
Evidence Pack to its original C7DFA5C0... SHA registered above. The full WI024,
WI026 Evidence Pack/manifest and WI028 handoff still match their initial hashes.
This proves original report-prefix preservation, not a restored old helper.

### Final PG Re-review Checkpoint

PG's appended native-HOLD/corrected-candidate addendum is now FINAL PASS for the
bounded v2 sequence. Independent34 tests passed,0 failures/errors/skips,0.365s;
offline plan confirmed the new bundle and15 unchanged inputs. This is PG local
verification approximately22:00KST, not actual native SQL evaluation. MA reports
proceeding only with this exact new sequence; final native results are pending.

DOCOPS read/rehashed the updated PG Evidence Pack:
`AAB7409A6D87F9C3FBFF6BB933840211FEAEA5D67B437CC304CCD3943A961226`.
PG records the new MA-attributed public archive as201216 bytes, SHA-256
`a594247f88887efcf45e3ba78dcf60e4d4e8b31681920e2b0c6ad770637650c3`.
No archive was opened by DOCOPS or PG; the actual exclusive root copy/member/
hash checks remain MA-owned. Native SELECT must return1,1,0,0,0,1, followed by
full preflight PASS and only then apply once with a fresh matching approval.
Initial bundle/stage/archive hashes remain historical and must not be reused.

### Second Native HOLD Checkpoint

MA subsequently reports successful corrected staging/native plan and all six
NULL truth-table results1,1,0,0,0,1. Full native preflight still failed: the
plugin guard sees two ACTIVE AUDIT entries, sha2_cache_cleaner and
mysqlx_cache_cleaner. Components were absent, replication count0, only permitted
loopback3306 listened and no X/admin listener existed. DB/account and every
approval/env/claim/receipt path were absent. MA stopped/remasked MySQL again.

These facts supersede the earlier pending native truth-table status only.
Do not infer that either plugin records SQL or is safe to allow from its name
alone. PG's read-only built-in classification review is pending, with no user
approval for a second code correction. DOCOPS edits no guard and issues no
native retry. No additional charge, target initialization or secret generation.

### Second Precise Correction Approval

MA subsequently supplied the user's exact approval for identifying only the two
built-in functions, recorded in Korean in REQ008. PG's read-only classification
advisory is complete. WI025 is reopened through its skill addendum for bounded
implementation/testing, followed by independent PG re-review. Do not generalize
this to allowing every AUDIT plugin or changing native plugin/global settings.

At that approval checkpoint MA again confirms MySQL stopped/masked and named
DB/account/secrets/approval absent; no native apply. A new implementation hash,
tests, final PG disposition and exact native receipts remain pending under
WI028. Earlier no-approval and failed-preflight rows remain dated history.

### V3 Local Completion Checkpoint

DOCOPS read WI025's second completion addendum and rehashed the public files.
SE reports37/37 local helper tests PASS, the preceding34 plus3 new regressions;
validator39 was not rerun. Fixture/query-contract tests are not a SQL evaluator.
Only byte-exact names for the two approved built-ins with SQL NULL library are
exempt; NULL names, aliases and non-NULL libraries remain rejected in the
recorded contract. Other active mysqlx/group_replication and existing listener/
component/capture guards remain independent gates, not removed checks.

| V3 object | SHA-256 |
|---|---|
| Bundle, SE offline plan receipt | b91766934c522b1c4549b8a3057f95eae94c9a542e107fb9c7c39d7df3b24b9c |
| production-bootstrap.py | 7333B068D0264045B8130519AFC6E0F62589912BA2EF0B31772FF84033960A1B |
| test_production_bootstrap.py | 497089DBC38892745E72D652D22B302DEFED7CC84E76D0EA8E0722ECC0A3D507 |
| production-bootstrap.md with both addenda | A76379E2B5E108392C67D07DC51081C06779352A53DB73284605B9C02C09AAC2 |
| WI025 Evidence Pack with both addenda | BE410657A090A11F88D69794DE1B01BDBBEE01A3217867A0D6B4ECA3CD5C4FBF |

Independent WI027 final review was triggered immediately. New archive preparation
is MA-local only at this checkpoint; native remains fenced, no DB yet. The
candidate runbook names fresh `/root/wi025-v3`, direct helper and matching --root;
both previous stages/evidence must remain. Final review/native results are not
inferred from the approved concept or local tests.

## Final V3 Native Result and Cleanup

This section supersedes the historical pending states above. DOCOPS read the
final WI027 v3 addendum: independent37 tests PASS,0 failures/errors/skips,0.764s,
and exact prior-helper/test reverse-diff checks. Review owner PG, local Windows,
approximately22:12-22:16KST; no remote execution by PG. The final reviewed
Evidence Pack SHA-256 is
`43FD8C70C321EE236E644E640239EC28A4585259B0CED9BF259CCC91666431E8`.
DOCOPS independently rehashed it and the final WI025/helper/WI026 manifest.

MA then supplied actual v3 staging, semantic/preflight, single apply, independent
read-only verification and two final cleanup receipts listed in the ledger.
These establish named-target preparation, not source-only success or production
readiness. Neither of the preceding HOLDs reached DB/account/secret creation.
The upload precheck refusal also occurred before mkdir; the later corrected
metadata expectation did not retry an uncertain write or weaken permissions.

| Actual v3 identity, MA receipt | Value |
|---|---|
| Public TAR length |201216 bytes |
| Public TAR SHA-256 |0b85a11779039b45c72cfc4f35d0e6efedd01bf43867cb88687d80dc41310a72 |
| Applied bundle SHA-256 |b91766934c522b1c4549b8a3057f95eae94c9a542e107fb9c7c39d7df3b24b9c |
| Applied helper SHA-256 |7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b |
| Target-specific structural observation digest |4e3ba94304b4e0f9345a92d38c7a7b7c70ebed38edb7111cce14632e32c71c2d |
| Bounded data inventory |43 tables;6 exact plans;0 non-plan rows;520 columns;234 index rows;91 FK rows |
| Target DB/account |atstudio_prod; atstudio_app on the exact approved loopback host |
| Privileges/account state |Exactly SELECT/INSERT/UPDATE/DELETE, nongrantable; account unlocked |
| Native output |BOOTSTRAP_APPLIED_NOT_RUNNABLE; runtime_tcp_authentication=NOT_RUN; canonical_fresh_manifest=UNRECORDED; app_runnable=false |

The structural digest is a public schema observation, not a secret-derived
authentication hash or a replacement canonical manifest. It does not assert
Hibernate compatibility or runtime password authentication. Native37 semantic
fixtures used the exact v3 predicate and produced0 mismatches/unknowns; this is
separate from both local37-test runs. No sum is presented as a full test suite.

DB/JWT/billing secrets were created only on the approved server. Final metadata
checks covered the root0700 directory and root0600 single-link env/claim/receipt/
approval files. The env body was not read. Nothing secret was transferred,
printed, hashed into these reports or committed. This is not external encrypted
backup/recovery, a guarantee against privileged capture or runnable app config.

Final host07f459 confirms the same boot and inactive exact masks; final local
c5ac1e confirms one-key removal, empty agent before stopping and fresh
Stopped/Disabled. All MA command sessions completed. No app/Nginx startup,
public opening, SMTP/Provider/ADMIN action, extra installation/charge, domain
purchase, reboot, commit or push occurred in this bounded preparation window.

MA also supplied final local receipts: faef7d confirms main/HEAD unchanged at
`58b1011dad796017e47dbcc82a4225ca449bcdf6`; diff --check0 with pre-existing
line-ending warnings only. bc222b reconfirms the registered current JAR,
WI026 manifest and v3 helper hashes. SE/PG completed and closed; DOCOPS alone
finished the four-file integration. These MA-local checks supplement, not
replace, DOCOPS's scoped document checks below.

## Commands and Verification

Executed: `git status --short --branch`, `git log -1 --format="%H %s"`,
`python --version`, PowerShell version/date query, scoped Get-Content/rg and
`Get-FileHash -Algorithm SHA256 -LiteralPath <public paths>`; all exited0.
Read-only WI026 reproduction used its published input/preservation loops and
frontend identity loop without rebuilding, output `WI028_RECORDED_IDENTITIES_PASS`.
The packaged-entry loop was not rerun by DOCOPS; PG's independent check remains
in WI027. No artifact/private configuration body was printed.

Draft validation at21:59KST: unchanged validate-docs implementation returned0,
Tier0 presence/internal file links PASS,38 supported traceability IDs found,
no scoped warnings. It has no scoped CLI, so the earlier WI's in-process
enumeration restriction was reused without changing the validator:

```powershell
python -B -c "import runpy; from pathlib import Path; from unittest.mock import patch; m=runpy.run_path('.agents/skills/validate-docs/scripts/validate_docs.py'); m.get('configure_stdio')(); root=Path.cwd(); files=[root/p for p in ('docs/index.md','deliverables/user/REQ-20261010-ATS-008.md','deliverables/user/WI-20261010-ATS-028-summary.md','deliverables/agent/WI-20261010-ATS-028-evidence-pack.md')]; scoped=lambda path,pattern: iter(p for p in files if p.is_relative_to(path)); scope=patch.object(Path,'rglob',scoped); scope.start(); result=m.get('DocValidator')(root).run(); scope.stop(); raise SystemExit(result)"
git diff --check -- docs/index.md deliverables/user/REQ-20261010-ATS-008.md deliverables/user/WI-20261010-ATS-028-summary.md deliverables/agent/WI-20261010-ATS-028-evidence-pack.md
```

Diff check exited0 with only Git's CRLF normalization warning. Direct PowerShell
checks covered all four files, including untracked additions: seven required
metadata fields, dependency-path existence, trailing whitespace and final LF
PASS. The unchanged historical index suffix matched its registered normalized
hash at21:59:18KST. Final validation must be repeated after later MA receipts.
External URLs, all repository orphans and historical anchors are outside this
scoped check; its index PASS is not a repository-wide coverage claim.

After the second native HOLD update, the same scoped validator again returned0
with38 supported IDs and no errors/warnings at22:05KST. A read-only prefix hash
check independently confirmed WI027's original20262 bytes still match
`EA6EF883574CDB0E3C5DC87A9600B7F60D959BB246A1193C94AE946D790EB462`.
This preserves the original PG review as dated history, not current native PASS.

### Final Closure Validation

DOCOPS final closure checks at22:20KST used the same four-document command above.
validate-docs exited0: Tier0 existence and internal file links PASS,38 supported
IDs, no errors/warnings. The scoped index check passed; no repository-wide
orphan/anchor/external-URL claim is made. The same scoped diff --check exited0
with only the existing CRLF normalization warning. Direct checks of all four
files, including untracked outputs, passed seven metadata fields, dependency
existence, trailing whitespace and final-newline checks. Historical index tail
SHA F59FD64A... matched the full registered hash at22:20:42KST.

Only the four approved documentation paths were edited by DOCOPS. Source,
templates, runbooks, earlier receipts, handoff and unrelated dirty work were
not modified. Tests/builds/native SQL were not rerun by DOCOPS; remote and final
Windows results remain precisely attributed to MA. Final verification covers
documentation integration, not production readiness.

## Risks / Rollback / Evaluation

Native NULL handling exposed a compatibility gap despite synthetic PASS. Keep
the refused run and no-mutation/stop evidence; corrected tests and local review
alone cannot establish a later native success. The initial PG result is bound
to its exact bytes and reviewed stage. No guard bypass, overwrite, automatic
partial replay or unapproved recovery is authorized.

Root-only incomplete environment is not off-host encrypted recovery or runnable
configuration. A future structural digest is not canonical fresh-manifest
acceptance, TCP authentication or Hibernate validation. Domain/SMTP, first
ADMIN, scheduler ownership, external isolation, backup/restore, alerts and
mixed-load acceptance remain separately gated. Preserve2vCPU/8GB/100GB and
current resource candidates; about97GB free is a snapshot, not capacity proof.
Monthly70000KRW is a budget, not an automatic billing cap.

Documentation rollback: apply a scoped correction to these four files while
retaining superseded dated receipts. Never reset the dirty checkout or delete
historical records/staging/DB/files. Any partial native state later observed
requires MA's separately approved recovery decision; DOCOPS cannot undo it.

Evaluation so far: explicit chronology prevents old HOLDs from hiding later
completion and prevents earlier PASS from concealing a native failure. No
retrospective, policy, template or memory update is authorized in this WI.

## Follow-ups / WI Chain

WI025/026/027/028 are complete within the approved preparation scope; WI028
Blocks: none. Actual MA native and final MySQL/SSH/key/agent cleanup receipts
are integrated. Close bounded REQ008 only; production remains HOLD. No descendant
was delegated and no additional operation is authorized by this closure.

Next-WI candidates after this receipt gate are MA-owned, not yet assigned:
secret-safe runtime TCP/JDBC/Hibernate and canonical-manifest reconciliation;
off-host encrypted secret recovery and backup/restore; domain purchase before
DNS/HTTPS, app/ADMIN/mail activation and operational acceptance. Each needs an
exact scope and appropriate approval; no new operation is authorized here.

## Related Documents

- [Handoff](WI-20261010-ATS-028-handoff.md): Four-file ownership and receipt gate.
- [REQ008](../user/REQ-20261010-ATS-008.md): Approval/progress/remaining gates.
- [User summary](../user/WI-20261010-ATS-028-summary.md): Concise current state.
- [Current entry](../../docs/index.md#2026-10-10-production-preparation-window): Latest checkpoint.
- [WI025](WI-20261010-ATS-025-evidence-pack.md): Initial preparation and later correction.
- [WI026 completion](WI-20261010-ATS-026-evidence-pack.md#completion-addendum-approved-isolated-frontend-build): Approved isolated SPA completion.
- [WI027](WI-20261010-ATS-027-evidence-pack.md): Initial disposition and later re-review.
- [Native runbook](../../deploy/single-vps/runbooks/production-bootstrap.md): Guard and mandatory cleanup procedure.
