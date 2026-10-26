---
version: 1.1.0
last_updated: 2026-10-23
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-010
dependencies:
  - path: WI-20261023-ATS-010-handoff.md
    reason: Approved read-only recheck and actual-result integration contract
---

# Evidence Pack: WI-20261023-ATS-010

## Summary

Completed the approved bounded read-only account/plan recheck: inventory PASS,
four DB cleanup checks PASS, target-bound post-stop probe PASS and original
firewall/provider/key/agent restoration confirmed. Input-window closure is
unconfirmed. WI008 -> WI009 -> MA native -> DocOps010 is complete; REQ007 remains
active. No account/business/schema writes, app/login or audio execution occurred.

## Scope / DoD Check

- [x] Verbatim Korean approval and continuing Astra Extra High setting recorded.
- [x] Minimal read-only query chain and MA-only native execution recorded.
- [x] Separate reader distinguished from unchanged/unactivated WI005 fixture tool.
- [x] No new-account input or verified-TEST approval required for this pure read.
- [x] Initial scoped documentation validation passed.
- [x] Exact candidate/review/test/preservation evidence integrated.
- [x] Actual account/plan/total-subscription results and unknowns integrated.
- [x] Four DB restoration checks, separate post-stop probe and access restoration integrated.
- [x] User report includes remaining input-window and separate-write-approval limits.
- [x] Final scoped documentation validation completed.

## Reference Documents (Tier 0-2)

MA and [WI010 handoff](WI-20261023-ATS-010-handoff.md) supplied the governing
context. Continuing create-wi-evidence-pack/validate-docs skills apply; the handoff
was verified before processing. Assignee DocOps; ATS documentation under the
existing context-injection rules. Korean conversation/REQ and English evidence.

| Tier | Document | Applied boundary |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md | Explicit narrow approval, preservation and traceability |
| 0 | docs/standards/development-standards.md | Exact source/runtime identity and limited verification |
| 0 | docs/standards/documentation-standards.md | Additive two-set evidence |
| 0 | docs/standards/glossary.md | Consistent REQ/WI terminology |
| 1 | docs/policies/security-policy.md | No secrets, invented authority or business writes |
| 1 | docs/policies/quality-gates.md | Independent exact-candidate review and restoration evidence |
| 2 | docs/index.md Current Decision Entry | Current versus historical checkpoints |
| 2 | deliverables/user/REQ-20261011-ATS-007.md | Latest explicit read-only approval |
| 2 | deliverables/user/WI-20261023-ATS-007-summary.md | Completed local fixture candidate and remaining native gates |
| 2 | deliverables/agent/WI-20261023-ATS-003-evidence-pack.md | Prior native inventory interpretation |

## Evidence Pointers

- [Current entry](../../docs/index.md) and [REQ007](../user/REQ-20261011-ATS-007.md): approval and chain.
- [SE008 handoff](WI-20261023-ATS-008-handoff.md): minimal read-only payload.
- [PG009 handoff](WI-20261023-ATS-009-handoff.md): exact independent review.
- [WI010 summary](../user/WI-20261023-ATS-010-summary.md): final result and preserved initial checkpoint.
- [SE008 evidence](WI-20261023-ATS-008-evidence-pack.md): minimal reader and 21 focused tests.
- [PG009 evidence](WI-20261023-ATS-009-evidence-pack.md): exact review, 8 independent tests and native interpretation.
- Retained implementation pointers: WI002 account-preflight, phase 1 native receipts,
  WI005 README, WI006 review receipt and WI067/WI069 lifecycle helpers in the handoff.

## Final Native Recheck and Exact Evidence

Verified on 2026-10-23, application day Asia/Seoul. MA owns the native/server and
provider/SSH observations. PG independently interpreted sanitized local receipts
and payloads without server access. DocOps read these safe receipts and hashed
the candidate, review, native interpretation, inventory/post-stop and restoration
files; it did not rerun tests or operational checks.

| Evidence | SHA-256 / result |
| --- | --- |
| [Five-file candidate](../../build/WI20261023-008-readonly-recheck/candidate.json) | `448253685c0bd2c2fc7fc3e0d2e70a7c99421dbe9c204bf0ebce2f09b4cdab3c` |
| Candidate implementation bundle | `3799c0145883e4fc201b2581f547287ce8f3e7c053a21d8536452e5c7fc76313` |
| [PG review receipt](../../build/WI20261023-009-readonly-review/review-receipt.json) | `fd27e370d61774540401aa939ecf890819e0c2168af5044f24a4c652b606ae8c`; ALLOW_EXACT_APPROVED_READONLY_ACCOUNT_PLAN_RECHECK |
| [Native interpretation](../../build/WI20261023-009-readonly-review/native-interpretation.json) | `749977f411fac5aa0d78ae9354b9988e41ae2631dc435e0109f2fb3fc09db66b`; DB result interpretation, access initially pending |
| [Inventory result](../../build/WI20261023-recheck-native/inventory-result.json) | `8618c109c87dfa7aae9b1d7a7cc577b93c397e2b01e32c223bcfb4aaf8782ea8`; INVENTORY_OBSERVED / PASS |
| [Target-bound post-stop probe](../../build/WI20261023-recheck-native/post-stop-probe.json) | `a49c91a28c0777ba57fc2e99860dec854ae0afc83c70928bab7a91054d1580da`; PREFLIGHT_PASS |
| [Final restoration](../../build/WI20261023-recheck-native/final-restoration.json) | `ad00aec62f49a879a9faa74421fc0d53eff9d5d903f2a4751dd3f71b4b1470e6` |
| [PG access interpretation](../../build/WI20261023-009-readonly-review/access-interpretation.json) | `7fc5ffc1fdddd70436958aea8cbb98fc2340d51a631d64cb7a0478d1115fb873`; restoration supported, input-window closure NOT_CONFIRMED |

PG009's final evidence/summary are stable with a separate access-restoration
addendum. The earlier native interpretation retains its original access-pending
boundary; its hash was not rewritten. The access interpretation is PG's local
receipt interpretation, while actual provider/browser/OS observations belong to
MA. DocOps checked the separate access-interpretation file hash.

Before native execution, SE's 21 focused implementation tests and PG's eight
independent tests passed, total 29 local tests. Commands are in their evidence
packs; native calls were mocked during those tests. PG verified source643/JAR
and earlier WI002/WI005/WI001/WI013 candidates unchanged. The new reader does not
modify or invoke WI005 fixture execution. PG compared generated probe/inventory/
post-stop payload AST bodies, five embedded sources, fixed expiry and target;
the inventory payload SHA is
`085b9d940893202dd30234f0a3a1c04c312cba6734dc4b76c9a99fffe695e02e`.

### Actual Account and Plan Findings

The only account is ID 1, USER with LATEST_AUTH_SIGNUP_DESIGNATED provenance,
verified/nondeleted, local password present and auth prerequisites true. Its
total_subscription_count=0 and effective_subscription_count=0, so no current
historical/expired subscription rows are present for this account at inspection.
There is no ADMIN. Pending audio and unfinished storage are 0. Credentials known
and password-login policy remain NOT_CHECKED; this is not a login-success claim.

| Plan ID | User type | Active | Downloads/day | Frozen fixture eligibility |
| --- | --- | --- | --- | --- |
| 1 | INDIVIDUAL | true | 5 | Eligible |
| 2 | INDIVIDUAL | true | 20 | Eligible |
| 3 | INDIVIDUAL | true | -1 (unlimited) | Excluded: finite quota required |
| 4 | BUSINESS | true | 10 | Excluded: INDIVIDUAL required |
| 5 | BUSINESS | true | 50 | Excluded: INDIVIDUAL required |
| 6 | BUSINESS | true | -1 (unlimited) | Excluded: type and finite quota |

Plan ID 1 is the smallest sufficient observed quota for the proposed single
download. This is a recommendation, not plan selection, grant or authorization.
No new account, role/audit, subscription, plan or other business row was written.
No app start/login, schema change, fixture apply, mail or audio test occurred.

### Native Sequence, Restoration and Preserved Limits

The [first probe](../../build/WI20261023-recheck-native/probe-result.json) returned
GUARD HOLD before DB startup. The [clock sample](../../build/WI20261023-recheck-native/clock-check.json)
observed the PC two seconds ahead of the server. The same exact probe later
passed; inventory and post-stop probe used the same scope/expiry and target.
This supports a possible <=900-second upper-boundary explanation, but the
sanitized GUARD receipt does not identify the failing check. The cause is not
proven; preserve the first HOLD instead of relabelling it PASS. No candidate
rebuild, widened scope or relaxed guard is claimed.

Inventory reports STOP, PID_ZERO, SOCKET_GONE and BASELINE PASS independently;
the separate target-bound post-stop PREFLIGHT_PASS confirms the observed cold
state. MySQL lifecycle/log effects are distinct from business-row changes.

After the explicitly authorized TCP22-source-only temporary change and recheck,
MA read back the original saved source restored. Ports 80/443 and all other
rules stayed unchanged. Provider logout was confirmed by its Login link; the
created tab was closed and remaining IAB tabs were zero. Key-removal exit 0 left
zero identities; agent Stopped/Disabled was reconfirmed. The final receipt cites
MA tool receipts `6ea818`, `88ae9f`, `9e7c20`, `b1350d` and `5e384c`.

The input process was present at its last observation and window closure remains
unconfirmed (`owned_input_window_closed=false`). Do not claim all windows closed.
No recovery console was used in this recheck. Exact IPs, token URLs and private
account/credential values are not included here; provider screenshots remain
under their original controlled evidence paths without duplication.

### Remaining Fixture and Audio Gates

The new observations resolve the earlier unknown total-row count and eligible
plan candidates only at this measurement. They do not replace WI005's future
private schema/snapshot digest or new-identity collision inspection. Actual new
identities/passwords, verified-TEST semantics, selected plan and exact KST date,
target/candidate/input binding and fresh fixture inspection/apply approval remain
required. Neither plan ID 1 nor an entitlement was selected/applied. No automatic
date extension, writes, account login or audio authority follows from this read.
Native audio stream/file-hash, playback and browser acceptance remain NOT_RUN.

## Initial Contract Checkpoint — Preserved

WI005 fixture inspection binds new-account private inputs and verified-TEST
semantics that this read-only request does not need. A separate minimal reader
extends successful WI002 queries only with plan information and total subscription
counts; the frozen fixture candidate is not changed or activated. Real new-account
credentials are not collected. Existing ID 1 remains unchanged.

MA must check source/JAR/runtime and target identity before authorized native
execution. Preserve immutable earlier unit logs and use a fresh scoped unit.
Unknown/changed prerequisites HOLD. Independently observe STOP, PID_ZERO,
SOCKET_GONE and BASELINE, then a separate post-stop probe and SSH restoration.
Engine lifecycle effects must not be confused with business-row writes.

Prior phase 1 effective-subscription count 0 does not prove absence of all
historical subscription rows. Fresh counts/plan ID/properties are unknown until
actual receipts arrive. Historical native success is not a new execution or
restoration result. This approval does not permit fixture writes, application
startup/login/mail or audio testing; no full authentication/audio replay.

## Commands & Outputs

DocOps read the generated WI010 handoff and current index/REQ headers. Changed
scope is only additive index/REQ checkpoints and own WI010 evidence/summary via
apply_patch. No server/provider/SSH call, DB/service/app start, credential read,
business query or product test was performed by DocOps. Initial documentation
checks cover only the newly changed owned sections/files.

Initial validation PASS: four owned scopes, 18 relative links, required metadata/
status/version, whitespace/new-file terminal newline and verbatim Korean
approval. No exact IPv4 literal or HTTP URL appeared in those scopes. No
historical link scan, product-test rerun or native operation was performed.

Final validation PASS: four owned files, seven new/changed sections, 27 relative
links in those sections, required metadata/version/status, whitespace and
terminal newline. No exact IPv4 literal or HTTP URL appeared in the checked
sections. REQ007 remains active and WI010 evidence/summary are stable. No broad
or historical-link scan, product-test rerun or native operation was performed.

## Risks / Rollback

Do not turn approval, local preparation or historical results into a new native
PASS. If transport or target gates block execution, retain sanitized receipts
and the precise boundary rather than widen access. Rollback removes only this
WI's additive documentation through reviewed edits; preserve unrelated changes,
earlier evidence and every frozen candidate. No data/schema deletion or reset
to HEAD is authorized.

## Follow-ups

WI008 -> WI009 -> MA bounded read-only execution/restoration -> DocOps010 is
complete. No additional WI is unblocked within this approved read-only scope.
REQ007 remains active; subsequent fixture/account/data writes, application login
and audio execution require separate approval and their exact readiness gates.
