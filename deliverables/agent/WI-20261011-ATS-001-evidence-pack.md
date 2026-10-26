---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: stable
related_wi: WI-20261011-ATS-001
dependencies:
  - path: WI-20261011-ATS-001-handoff.md
    reason: Existing skill-created handoff and disjoint ownership
  - path: ../user/REQ-20261010-ATS-010.md
    reason: Approved local preparation, independent review and MA execution gates
  - path: WI-20261010-ATS-032-evidence-pack.md
    reason: Prior bounded native JDBC and masked-state cleanup evidence
---

# Evidence Pack: WI-20261011-ATS-001

## Summary

TL;DR: **Local preparation complete; 35/35 offline tests PASS; independent PG and
all native TLS gates remain.** Seven focused implementation files, two deliverables,
no full application proxy duplication. SE worked on shared dirty `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`, Windows/PowerShell/Python3.14.3 on
2026-10-11. Existing tracked and untracked work was preserved; no Git write.

## Scope / DoD Check

- [x] Existing approved REQ and WI handoff checked before implementation.
- [x] Exact apex/public IPv4/32 renderer, isolated HTTP token and HTTPS marker.
- [x] Unknown Host/SNI and path/method denial, access-phase marker ACL, IPv4 only.
- [x] Protected standard lineage hook with metadata/link/pair/SAN/trust/native-test gates.
- [x] Explicit service config selection, bounded operator procedure and masked-state recovery.
- [x] Tests and two-set deliverables within assigned ownership.
- [ ] Native Nginx parsing/phase behavior, Linux permissions and actual TLS: MA only.
- [ ] Independent PG PASS and target application: next WI002 / MA, not SE completion claims.

No remote/native target connection, browser, package installation, private-file
read, DNS/firewall change, registration/issuance, app/DB/service operation,
descendant, commit or push. Public official documentation was read through the
web tool; no browser UI or authenticated account was used. No server/client IP
was copied into candidates. Test IPv4 is a synthetic input, never contacted.

## Reference Documents (Tier 0-2)

| Tier | Source and actual use |
|---|---|
| 0 | Fully injected STD-001/004/002/005: core-principles, documentation-standards, development-standards, glossary. Not reread from disk. |
| 1 | Fully injected SE persona and security-policy; read access-control-policy and quality-gates. |
| 2 | Read test, create-wi-evidence-pack and validate-docs skills; evidence-pack-standard; relevant portions of first-host-setup, host-hardening, deployment, jdbc-readiness and production-readiness. |
| Context | Read REQ010, existing WI001 handoff, current decision entry, WI032 receipt ledger; inspected all existing nginx templates/restricted-access and existing Python test conventions. |
| Provenance | workspace.json confirms ATS; context-injection-rules gives SE required tiers[0], with security context explicitly supplied. Tier0 -> role/security -> context -> snapshot retained. |

Reuse decision: retain existing Nginx deny/default-host and isolated ACME concepts,
but do not install the app's root/proxy/include graph. Use two templates, one
renderer, one deploy hook and one service drop-in, with standard Certbot lineage.
No library/architecture replacement or general operator framework added.

MA current-window observations are explicitly attributed in runbook section1:
00:04-00:05 KST host/masks/source/package/DNS survey, subsequent NTP/dpkg/sshd/
capture-limit observations, and root noVNC recovery at00:07 KST. Distro `nginx -t`
success does not validate these candidates. The two latest MA messages clarify
package2.9 help/provenance gates and optional exact-agreement no-contact consent;
no actual CA consent is asserted.

## Evidence Pointers / Complete Changed Paths

All nine files are new. Existing handoff, REQ, product source, other runbooks,
indexes, prior evidence and full app proxy are unchanged by SE.

| Path | Main contract |
|---|---|
| `scripts/deployment/tls-quarantine.py` | `validate_inputs`, `render_config`, `QuietParser`: exact domain and canonical public IPv4/32; stdout only; bounded failures. |
| `scripts/deployment/test_tls_quarantine.py` | `RenderTests`, `HookTests`:35 positive/negative tests with Linux metadata/command mocks. |
| `deploy/single-vps/tls-quarantine/nginx.conf.template` | Complete independent HTTP config, strict raw token target, isolated root, no request logs, no includes or IPv6 sockets. |
| `deploy/single-vps/tls-quarantine/https.conf.template` | Rejected default SNI; explicit Host/SNI guards; exact marker with satisfy/allow/deny and static handler. |
| `deploy/single-vps/tls-quarantine/nginx-service.conf` | Clear/replace StartPre/Start/Reload with exact quarantine `-c`; leave vendor config intact. |
| `deploy/single-vps/tls-quarantine/reload-hook.py` | `check_event`, `check_path`, `check_lineage`, `check_san`, `reload_checked`; exact same-generation archive links and root-only private key. |
| `deploy/single-vps/runbooks/domain-tls.md` | Sections1-6: latest MA facts, preflight/packages, bootstrap, consent/staging/production, TLS/renewal, HOLD/closure. |
| `deliverables/user/WI-20261011-ATS-001-summary.md` | Bounded result, risk and remaining MA gates. |
| `deliverables/agent/WI-20261011-ATS-001-evidence-pack.md` | This skill-formatted report and identities. |

## Public SHA-256 Identities

Hash exact source bytes before transfer; target render hashes differ with MA's
private approved source and must be recorded only in protected operational evidence.
The final report hash is returned separately, avoiding self-reference.

| Public file | SHA-256 |
|---|---|
| `scripts/deployment/tls-quarantine.py` | `723ed64dbbb48ca1a729dce7684498f6ff74fb6c85f0167e11e046783d761ace` |
| `scripts/deployment/test_tls_quarantine.py` | `b479cec7c47eb4bfee7b5b42f7a3537c3a8ecf968b6aae54f2cf208ba9f65320` |
| `deploy/single-vps/tls-quarantine/nginx.conf.template` | `ebc0ae231780ae2b16e82cf571cd144dd1ae7e2bdd9cb9d4ff564ad0c4a527ea` |
| `deploy/single-vps/tls-quarantine/https.conf.template` | `4fe91f83763303600149c532591b59ac043e28edc25dbdf395bc748ff83e42cb` |
| `deploy/single-vps/tls-quarantine/nginx-service.conf` | `cecaa38ef49f92518e6c5a776cedc7c03cecb54764afd1455b8a404f13161b93` |
| `deploy/single-vps/tls-quarantine/reload-hook.py` | `4c9d7ab410e6fa34597827fa679cc1e434f2cef108a9a7043edd32ab9f9104f0` |
| `deploy/single-vps/runbooks/domain-tls.md` | `ff9405ec71a39fe619780570b49f011773b001ab876b795827e34da00c0f8ab7` |
| `deliverables/user/WI-20261011-ATS-001-summary.md` | `8f9cd035935aff233385a92c2dde9619fad2b17201f4c6ed30abee35cb779866` |

## Commands & Outputs / Test Evidence

```powershell
python -B -m unittest discover -s scripts/deployment -p test_tls_quarantine.py -v
git diff --check -- scripts/deployment/tls-quarantine.py scripts/deployment/test_tls_quarantine.py deploy/single-vps/tls-quarantine deploy/single-vps/runbooks/domain-tls.md deliverables/user/WI-20261011-ATS-001-summary.md deliverables/agent/WI-20261011-ATS-001-evidence-pack.md
Get-FileHash -Algorithm SHA256 -LiteralPath <one-public-path-from-table>
```

Initial red check failed import because the renderer did not exist (exit1),
confirming missing implementation. First implementation passed27 tests; added
input/parser, metadata/size, trust-failure and bounded-output cases passed35.
Final implementation test run: exit0,35 passed,0 failed,0 skipped,0.367s.
Tests create no temporary fixture files, sockets or keys; native commands are
mocked. CLI tests spawn only the local Python renderer with synthetic arguments.
`-B` avoids bytecode writes. No full app/Gradle/frontend tests are applicable.

Test boundaries: string assertions establish candidate containment/ACL placement,
not actual Nginx phase execution. Metadata mocks cover ownership/mode/symlink
ancestry, same-generation archive containment, missing pairs, root-only key,
size bound, SAN and fail-before-reload ordering. Native `nginx -t` supplies the
real key-pair check; OpenSSL trust/expiry checks and systemd reload are NOT RUN.
No measured coverage percentage or100% coverage claim is made.

`Get-Command python,nginx,bash,openssl` found only Python on this Windows PATH;
the lookup returned1 for missing tools. No install or WSL substitute was used.
Scoped `git diff --check` exited0, but untracked files require direct byte checks.
Final scoped document/byte/hash verification is recorded below.

### Completion Checks

The existing validate-docs skill validator was run read-only with `Path.rglob`
scoped to the three new Markdown files. Tier0 existence, internal file links
and5 supported traceability IDs passed;0 errors/0 warnings. No repository-wide
index/orphan audit or external fragment validation is claimed. Direct PowerShell
byte checks passed all nine files: ASCII, LF-only, final newline and no trailing
whitespace; all three Markdown files have the seven required metadata fields.
Final identity checks compare each of the eight table hashes with local bytes;
the Evidence Pack's own hash is supplied in the final handoff response.

## Risks / Rollback

- Runtime effective unit selection, Linux filesystem ACLs/mounts, actual Nginx
  feature/phase behavior, IPv6 and independent external denial remain MA gates.
  POSIX mode checks alone do not audit extended ACLs; metadata-only protection
  depends on root-owned ancestry and an exclusive trusted change window.
- Request logs are intentionally disabled; the hook reports fixed results only.
  Certbot/OS/provider logs are separate and must remain private. No generic capture
  guarantee or infrastructure sanitization claim is made.
- Reload success is only a request plus active-state check, not a served-certificate
  proof. Dry-run uses staging and the active production certificate for hooks.
  A Certbot overall exit0 does not substitute for hook/served-TLS evidence.
- No CA agreement/contact approval currently exists. One action-time approval can
  cover staging/production/renewal. Optional no-contact issuance needs explicit
  current LE agreement v1.8 and no-contact-sharing consent BEFORE reviewing exact
  noninteractive flags; no pre-accepted command is supplied.
- LE expiry emails are discontinued. Independent alert ownership/transport/recipient
  and delivery, app/DB/SMTP/backup/load, continuing/boot service posture remain open.

Before adoption rollback is withholding candidates. After MA adoption, follow
runbook section6: preserve SSH/egress, withdraw only new provider80/443 rules,
stop the window-owned scheduling, stop/quiesce/remask Nginx, keep DB masked and
app absent. Preserve certificates/keys/data/packages/DNS/candidates and failed
receipts. No deletion, recursive permission repair, default-site restart or
automatic rollback over newer files. Close exact-key/empty-agent/Stopped-Disabled
and session cleanup with fresh receipts. Unconfirmed cleanup is HOLD.

## Follow-ups / Exact Remaining MA Gates

WI001 **Blocks WI-20261011-ATS-002**. MA must immediately use
create-wi-handoff-packet and delegate PG review of these exact nine paths,35 tests
and execution/consent sequence. No descendant is launched by SE. WI002 PASS is
required before applying configuration, package/service/firewall/DNS/CA actions.

MA then owns: fresh pinned host/source/recovery and masked DB/absent app; protected
no-overwrite staging/backups; official apt provenance, scripts and exact plan;
native2.9 help and Nginx features/effective drop-in; authoritative DNS and exact
provider rules/no IPv6 opening; synthetic challenge; action-time consent and
staging account/challenge; production issue; protected lineage/native TLS parsing;
allowed/denied/Host/SNI/path probes; dry-run/reload/served certificate and timer
selection; explicit continuing/boot posture; monitoring-gap recording; full
host/SSH-agent cleanup. WI003 DocOps integrates those actual receipts after PG.
REQ010 is not closed by local preparation. Index integration is outside SE's
disjoint ownership; the runbook is linked from both deliverables for this handoff.

## Related Documents

- [Summary](../user/WI-20261011-ATS-001-summary.md): User-facing bounded result.
- [Operator runbook](../../deploy/single-vps/runbooks/domain-tls.md): Exact procedure and official references.
- [Current entry](../../docs/index.md#current-decision-entry): Prior completion chronology, not a native TLS receipt.
- [Certbot2.9 CLI](https://github.com/certbot/certbot/blob/v2.9.0/certbot/certbot/_internal/cli/__init__.py): Version-specific hook/dry-run semantics checked against official source.
- [LE expiration emails](https://letsencrypt.org/docs/expiration-emails/): Discontinued service, not expiry monitoring.

## 2026-10-11 Test Isolation Correction

### Scope / Superseding Status

**Corrected local candidate complete; Windows36/36 PASS. Ubuntu3.12 retest and
PG rereview PENDING; native application HOLD continues.** This is the approved
resumption of WI001 under its saved skill-created handoff, not a new requirement
or permission to weaken the implementation. User approval explicitly covers the
test-only fix, Windows/Ubuntu retesting and independent rereview.

MA reported that exact-hash native Ubuntu/Python3.12 testing failed
`test_any_earlier_failure_stops_reload`: in the check_lineage subcase, the real
unmocked check_path raised FileNotFoundError before the injected target guard.
Windows35 PASS had accepted an incidental GateError, so the earlier claim that
this test proved target-guard isolation is withdrawn. The earlier Windows result,
original hashes and original report text remain historical evidence, not a native
PASS. No failure timestamp or total native-test count was supplied or invented.
MA reports only public candidate staging and otherwise held native changes.

### Complete Changes / New Identity

Only three paths changed in this correction:

- `scripts/deployment/test_tls_quarantine.py`: existing guard-failure test now
  mocks all non-target guards (event/path/lineage/SAN), injects failure only into
  the named target, asserts its exact arguments/one call and exact GUARD error,
  and asserts no native call, SAN evaluation or filesystem access. A patched
  Path.lstat tripwire makes accidental real filesystem access fail on any OS.
- The new `test_missing_filesystem_stops_before_lineage_and_native_calls` exercises
  the real path guard against mocked FileNotFoundError. It asserts the specific
  missing-file exception, event/path reachability and no later lineage/SAN/native
  call, without relying on the host's actual /etc or root ownership.
- `deliverables/user/WI-20261011-ATS-001-summary.md` and this Evidence Pack:
  additive correction sections only; all prior text/identities retained.

| Test candidate | SHA-256 |
|---|---|
| Historical35-test file, retained native stage | `b479cec7c47eb4bfee7b5b42f7a3537c3a8ecf968b6aae54f2cf208ba9f65320` |
| Corrected36-test file, current local candidate | `a9ca1406ef209a9eb67406dbf14c889ca85d5c5eab01274933b1cd78bcf56436` |

The six non-test implementation/runbook hashes in the original identity table
remain authoritative and unchanged. The historical test/summary hashes in that
table identify their prior revisions, not the corrected file/additive summary.
Before this append, report hash was
`5679ed91008c833c9f79fceadfb9bd0179a70db8de192bb5d3f934a310d42f52`,
and summary hash was
`8f9cd035935aff233385a92c2dde9619fad2b17201f4c6ed30abee35cb779866`.
Current report hashes are returned separately; no self-hash is embedded.

### Verification / Remaining Gates

SE, local Windows/Python3.14.3,2026-10-11, same main/HEAD:

```powershell
python -B -m unittest discover -s scripts/deployment -p test_tls_quarantine.py -v
Get-FileHash -Algorithm SHA256 -LiteralPath scripts/deployment/test_tls_quarantine.py
```

Exit0:36 tests,36 passed,0 failed,0 skipped,0.296s. No implementation, deploy hook,
template, service drop-in or runbook edit; no remote/private/native service action,
installation, descendant, commit or push. Original Tier0/security injection and
skill evidence structure still apply; existing handoff was reread for this scope.

MA immediately routes these exact changed bytes to PG for WI002 rereview. After
review, MA retains the original stage and transfers the test-only replacement to
a fresh root-stage suffix with the same implementation hashes. Run the identical
focused command there on Ubuntu/Python3.12, record36 tests and exact identity,
then obtain the independent completion verdict. No native PASS or rollout approval
is claimed here. CA consent, effective config, exposure, renewal and cleanup gates
are unchanged. Rollback means retaining both candidates and remaining HOLD, never
restoring the historical test as valid guard-isolation evidence or loosening guards.
