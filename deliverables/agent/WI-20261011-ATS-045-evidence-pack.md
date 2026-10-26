---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-045-handoff.md
    reason: Documentation-only authority and actual-outcome contract
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Renewed trial and separate HTTPS-only recovery approvals
  - path: WI-20261011-ATS-049-evidence-pack.md
    reason: Exact v2 continuation verdict and local evidence
---

# Evidence Pack: WI-20261011-ATS-045

> Purpose: Preserve failed trials and waiting checkpoints while recording bounded HTTPS recovery and final access cleanup.

## Summary and Scope

**TL;DR: WI045 bounded recording/recovery/access cleanup COMPLETE. MA's WI048 continuation recovered original HTTPS; the original web objective remains unmet and REQ006 stays active/HOLD. No new app/DB trial or public acceptance.**

Waiting and login-reply checkpoints below remain historical; the later actual recovery and cleanup are recorded separately. Scope is unchanged.

- [x] Renewed trial, security, HTTPS-only recovery and continuation approvals recorded verbatim in the Korean REQ.
- [x] MA receipts separate local identity, native trial, HTTP checker/wrapper, recovery failure and current waiting state.
- [x] Prior results preserved; no DocOps native operation, candidate audit, test replay, source/secret/network access or Git mutation.
- [x] MA actual WI048 outcome and final service/HTTPS postcheck received.
- [x] Owned-key removal, agent Stopped/Disabled and root-console logout evidence received.

WI045 closes its bounded evidence integration, not the REQ's web objective. MA owns every target/browser/access observation; SE/PG own local tests. Observation date is2026-10-11 KST, distinct from document updates. Minute-only times below have no invented seconds.

## Evidence Pointers

[Machine receipt](WI-20261011-ATS-045-native-receipt.json) preserves the supplied native result and inner L2 JSON, with separately attributed diagnostic objects. Transport wrappers are reduced to receipt ID/time/outcome; private values and raw logs are omitted.

| Observation / owner MA | Evidence and limit |
| --- | --- |
|16:55:31.3814102 and16:57:53.6874984 local identity | `e88901`, `19534a`: backend641/frontend336/SPA143, existing JAR; no rebuild. Upload153 once, root-stage10, S2 PASS at16:58:27. |
|16:59:35 supplemental HOLD | `a72e35`, `e1e239`, `097165`: process-name false positive resolved as expected shutdown waiter; not a candidate failure, package operation or trial retry. |
|17:00:52 dispatch;17:01:12 OPEN | `464fd4`, `77d174`: one trial, admission PASS/app VERIFIED. L2 `7ede70`:17 HTTP200/PASS cases and BOUNDED_HTTP_PASS, then wrapper exit1/HOLD_ALLOWED_HTTP. Local `ab0943` supports unset-LASTEXITCODE wrapper failure; no exact time supplied and no fix claimed. |
|17:01:33 finish;17:01:44/57 final polls | `61f2ef`, `03f783`, `1bf5f7`: HOLD/POST_STATE_CHANGED, cleanup FAILED;36 rows =25 PASS/2 NOT_APPLICABLE/9 FAILED. Same result polls, not repeated trials. L3/browser/S7 NOT_RUN. |
|17:02:21 failure postcheck | `6daa1f`: app/DB masked PID0; Nginx inactive PID0. TLS3 pins PASS is not live HTTPS restoration. Cross-device overlay rename failure is strongly supported, but native errno was not captured. POST_STATE_CHANGED remains NOT_DETERMINED. |
|WI046 consumed recovery | `19c3b3`, `0c3198`: staging PASS; execution exit1/HOLD/ORIGINAL_SERVICES/BASELINE after both copies/detaches and reload. Nginx start NOT_RUN. stdout durability PASS differs from stored PENDING observed at17:35:30 (`e662e0`); both remain exact. |
|17:32:23 and17:32:55 diagnosis | `78b51d`, `896737`: only nginx.After serialization differs; exact token multiset/count7 equal. This explains WI046's comparison HOLD, not WI042's POST_STATE_CHANGED. Retained originals and durable copies remain preserved. |
|17:45:07 and17:51:18 preparation | `1dd45e`:14-file freeze PASS; `22d5d6`: bounded read-only host/boot, app/DB masks, Nginx inactive, jobs/workers, prior evidence/fresh targets PASS. This is not WI048 L1/L2 execution. `certbot_inactive=true` is not a timer-disable claim. |
|17:52:30/55 transport diagnosis | `552000`: TLS3 true, peer false, overall probe HOLD. `eed102`: corrected transport-argument peer match true; sudo lacked SSH_CONNECTION. No observed network drift. Renewal next2026-10-12 05:42:18 KST is that observation's schedule only. |
|Latest human/access checkpoint | Provider session expired at the member login page; user login requested, reply absent, fresh firewall UI check pending. MA17:52 root prompt still logged in;17:54 agent Running/Manual with expected sole key. Temporary retention is approved, cleanup is not done. |

## Final Recovery and Access Outcome

- MA confirmed the provider management-only rules and root recovery prompt without publishing addresses/screenshots. Fresh gate `9bb74f`,18:04:42.841 KST, and local14+7 pins `e8b7e9` passed. Exact WI048 v2 L1 `a92291` and L2 `4c5b60` ran once each; no exact L1/L2 completion times were supplied.
- L1 exit0/STAGED_WI048; L2 exit0/RECOVERED_ORIGINAL_HTTPS, code OK/stage DONE, nginx_start VERIFIED, receipt_durability PASS, safety_stop NOT_NEEDED, trial_status HOLD;12 checks PASS. Only reported fields/count are recorded; unnamed check rows are not invented.
- HTTPS `27b884`,18:05:40.145: no proxy, default certificate validation, `/ready.txt`200 with exact quarantine body. Read-only `de821f`,18:05:42.088: app masked/failed/PID0, MySQL masked/inactive/PID0, original Nginx active/MainPID29877/ControlPID0, supervisor failed/PID0; TLS3/prior receipt/copy pins exact, original overlays absent, no java/mysqld/ffmpeg.
- New disk receipt is provisional PENDING_DURABILITY, SHA256 `366d744e1b19203b97ea0b8095c342c04929058635b9ffa3744d0088cf6584dd`. Success authority is exit0/bounded stdout plus independent postchecks, not those provisional disk bytes.
- `2eab61`,18:06:23.716 exit0: only the expected owned key removed with `ssh-add -d`, agent empty PASS. `57f619`,18:06:28.282 exit0: agent Stopped/Disabled. User replied "완료" to noVNC exit; MA at18:08 (minute precision) explicitly saw Ubuntu tty1 login prompt/no root shell. Earlier blank cursor was not logout proof. Owned noVNC tab28 closed; provider tab25 retained for handoff; unrelated tab untouched. No required native exec sessions remain.
- No app/DB start, SQL, secret read, firewall/certificate change, public opening or new purchase occurred in this recovery per MA. WI042 POST_STATE_CHANGED remains NOT_DETERMINED and its cleanup FAILED is not rewritten. HTTPS recovery is not app/browser acceptance; L3/S7 remain NOT_RUN.

## Identity and Local Review

| Exact identity | SHA256 |
| --- | --- |
| Existing70,306,351-byte JAR | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| WI042 trial bundle | `78ce821757111b6f4bd440a79680ed12e4e40b6592616293e4d768ddd6dbe00c` |
| WI046 recovery candidate | `7ab8be7b2b1b94df4eeabbc8be5c03620d0ad219749ce571014d587a92ad1d94` |
| WI048 v2 ordered14-file bundle | `b3befcd2a2310ef8c7461800ce35f75f2aad918eb479de12f480433c55fcacfe` |
| WI048 freeze-v2.json | `26c47507668074aea79c5f96fd5d0a99d7dd7d1c86259edb3898baf170240b2e` |

Full source/SPA/manifest, candidate/README/recipe and preserved-file pins are in the machine receipt and linked owner/reviewer reports. DocOps did not rehash candidates or replay their tests.

- [WI043](WI-20261011-ATS-043-evidence-pack.md): LOCAL_READY;106 owner Python/8 PowerShell, independently9 plus owner106/8 reruns, local only.
- [WI046](WI-20261011-ATS-046-evidence-pack.md) / [WI047](WI-20261011-ATS-047-evidence-pack.md):23 owner and7/6 independent mocked tests; conditional ALLOW preceded the actual consumed HOLD.
- [WI048](WI-20261011-ATS-048-evidence-pack.md):21 current owner and23 inherited mocked regressions. [WI049](WI-20261011-ATS-049-evidence-pack.md): independent9, owner21 rerun, actual local L1 prefix and2 PowerShell ASTs PASS; v2 CONDITIONAL_ALLOW, not native success. v1 invalid local input path/RED preserved; only corrected README-v2 is covered.
- These counts overlap; they are not unique native proofs or measured coverage. Test commands and exact outputs remain in those reports.

## Documentation Validation

Final bounded skill validation passed (`e86cb3`, exit0): five selected Markdown bodies,90 supported IDs,0 errors/0 warnings. Direct metadata/dependency/JSON/whitespace checks passed, including untracked outputs; prior index/REQ sections, native/HTTP results, WI046 results, waiting checkpoint and7/7 selected historical document hashes were preserved. Final recovery/access fields match the supplied MA observations. Scoped diff check `eb9acf` exited0, with the existing index CRLF-to-LF advisory only. The initial scan misread a Python dictionary call as a Markdown link; equivalent example syntax resolved it without validator changes. No application tests or candidate checks ran.

Reproduce the skill's bounded body scan from repository root (PowerShell):

```powershell
@'
import runpy
from pathlib import Path
from unittest.mock import patch
r = Path.cwd()
m = runpy.run_path(str(r / '.agents/skills/validate-docs/scripts/validate_docs.py'))
m['configure_stdio']()
files = [r/'docs/index.md', r/'deliverables/user/REQ-20261011-ATS-006.md']
files += [r/f'deliverables/agent/WI-20261011-ATS-045-{s}.md' for s in ('handoff', 'evidence-pack')]
files += [r/'deliverables/user/WI-20261011-ATS-045-summary.md']
original = Path.rglob
v = m.get('DocValidator')(r)
with patch.object(Path, 'rglob', lambda p, q: iter(files) if p == r else original(p, q)):
    result = v.run()
print('SELECTED', len(files), 'ERRORS', len(v.errors), 'WARNINGS', len(v.warnings))
raise SystemExit(result)
'@ | python -I -B -
git diff --check -- docs/index.md deliverables/user/REQ-20261011-ATS-006.md deliverables/agent/WI-20261011-ATS-045-evidence-pack.md deliverables/user/WI-20261011-ATS-045-summary.md deliverables/agent/WI-20261011-ATS-045-native-receipt.json
```

Direct checks also cover required frontmatter/dependency targets, JSON parsing/receipt fields, terminal newline/trailing whitespace including untracked outputs, prior index/REQ sections, and seven selected historical document hashes. The skill retains its Tier0 existence/index inventory checks; this is not a full-repository body audit or deployment validation.

## Drift, Risks and Next Step

Current entry now distinguishes native failure, waiting and HTTPS recovery from the dated local-only checkpoint. Only five assigned paths changed: index, REQ006, this evidence, user summary and machine receipt. No inventory-count refactor or historical correction was performed. Preserve WI026's unresolved cleanup cause, WI034 bounded success, WI035/WI039 failures and their old review verdicts. Popular-album/latest and Tag-document discrepancies remain deferred.

No whole-window DB immutability/SQL audit, nonmanagement denial, Internet/IPv6 delivery, populated media/login/playback/download, mail/payment, pristine browser, backup, capacity or production-release acceptance follows. No new paid product; existing server billing continues.

**Chain closeout:** WI048 -> WI049 -> MA once-only original-HTTPS recovery -> WI045 integration is complete, including access cleanup. WI045 Blocks none; no successor is automatically dispatched. Original web acceptance and root-cause investigation remain outside this closeout and need separately approved work, not a retry under the consumed trial. Documentation rollback would affect only this scoped integration and preserve concurrent edits; no operational rollback is authorized here.

## References and Related Documents

Retained injection order: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary; DocOps role/security-policy; WI045 handoff and current evidence. The evidence-pack and validate-docs skills were read and applied. No subdelegation or memory writes.

- [WI045 Handoff](WI-20261011-ATS-045-handoff.md): Authority and pending final evidence.
- [REQ006](../user/REQ-20261011-ATS-006.md): Korean approvals and active/HOLD progress.
- [Current Entry](../../docs/index.md#current-decision-entry): Latest waiting checkpoint above preserved history.
- [Machine Receipt](WI-20261011-ATS-045-native-receipt.json): Exact inner results and reduced MA observations.
- [User Summary](../user/WI-20261011-ATS-045-summary.md): Concise waiting state and human handoff.
