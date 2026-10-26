---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved parent work and explicit local diagnostic implementation follow-up
  - path: WI-20261011-ATS-050-evidence-pack.md
    reason: Confirmed comparison and persistence gaps
---

# WI-20261011-ATS-052 Handoff

> Purpose: Add bounded per-check state diagnostics while retaining every existing security decision.

[WI HEADER]
WI ID: WI-20261011-ATS-052
REQ: REQ-20261011-ATS-006, latest explicit local-only implementation amendment
Agent: se
Depends On: WI050/051 diagnosis and latest user approval
Blocks: WI053 independent PG review, then WI054 documentation integration

[WI SUMMARY]
Approval (verbatim): "오케이 그럼 보안 검사는 유지하고, 어떤 항목이 언제 달라졌는지 개별 기록하도록 검사 도구 보완하고 로컬 테스트·검토까지 진행해줘. 앱 재실행이나 외부 공개는 아직 하지 말고, 추가 비용이나 내가 직접 해야 할 건 먼저 알려줘."
Scope: A fresh local-only review candidate derived from the preserved WI042 checker, diagnostic-only changes and isolated regression tests. Record precise check/phase/observation timing, bounded differences and skipped checks. No new library or security-policy change.
Out: No SSH/agent/UI/cloud/server access, service/app/DB start, secret or private-log reads, SQL/network, purchases/installations, deployment, source JAR/SPA changes, old evidence edits, commits/push or memory writes. Test fixtures must be synthetic; native boundaries mocked.
Authority: This explicit user instruction approves this local amendment; DocOps records it into the parent REQ, whose SE/PG/DocOps strategy applies. It is not a new native trial approval.

[ACCEPTANCE CRITERIA]
- [ ] Keep WI042 and all historical runtime/freeze/receipt bytes unchanged, record before/after pins.
- [ ] Preserve exact policy/BPF/DB comparisons, original error categories, short-circuit order, failure precedence, time bounds and cleanup. Do not normalize deny ordering, ignore BPF IDs, update baselines or fix unrelated HTTP/cleanup/recovery issues.
- [ ] Give baseline capture, prestart, PRE_VIEW and POST_VIEW checks stable bounded phase labels; mark the planned daemon-reload before/after without additional native queries or changing its order. Observation time is not the exact instant of a change.
- [ ] Record each required comparison as PASS, DIFFERENT, ERROR or NOT_RUN with monotonic sequence/elapsed timing and optional bounded UTC observation timestamp. First failure still stops later reads; never perform an extra DB/BPF read just for diagnostics.
- [ ] Useful differences: allowlisted policy field names only; safe BPF identity/direction/multiplicity change indicators only; DB equality/validation category only. No raw values, table names/rows/count dumps, raw IDs, configuration paths/addresses, private hashes, environment, command output, exception text or credentials in exported diagnostics. Malformed data fails closed.
- [ ] Integrate diagnostics into existing result persistence and strict public_result projection; preserve the primary failure even when diagnostic projection/recording fails. Missing/incomplete diagnostics must not be interpreted as PASS. Bounded row/size/type/finite-time/enum validation is mandatory; no arbitrary string passthrough.
- [ ] Add regression tests for policy-only/BPF-only/DB-only mismatch and thrown errors, short-circuit NOT_RUN, unchanged PASS, baseline/prestart failures, pre/post-view attribution, reload event ordering, same-rule/new-BPF-ID and deny-order differences still HOLD, hostile secret sentinels and unknown fields, malformed/oversized/nonfinite diagnostics, JSON persistence/public renderer roundtrip and old error precedence. Exercise actual candidate runtime/session integration under strict mocks, not only a standalone helper.
- [ ] Run existing relevant checker regressions plus new tests locally; no app build or native execution. Report real counts, failures and platform limits.
- [ ] Provide a concise README explaining diagnostic schema, local reproduction and explicit NOT_NATIVE_READY status. No new remote transport recipes; future operational packaging/known unrelated defects remain out of scope. Make accidental native entrypoint execution reject LOCAL_ONLY in this candidate, without weakening tested comparator behavior.

[INPUT POINTERS]
Tier 0 (MA injected): docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; relevant fail-closed/secret handling rules; SE role.
Tier 2: docs/index.md current entry; REQ006; WI050 evidence/summary; WI051 final integration; build/WI042-restricted-web-check/runtime.py, web_session.py, existing tests/freeze; WI045 native receipt (redacted historical data only).
Skills: create-wi-handoff-packet (MA); test (Python tooling suite only); create-wi-evidence-pack.

[OUTPUT CONTRACT]
Allowed writes only: build/WI052-state-diagnostics/ new candidate/tests/manifests/local logs/README; deliverables/agent/WI-20261011-ATS-052-evidence-pack.md; deliverables/user/WI-20261011-ATS-052-summary.md. Use apply_patch for manual code/doc edits. Mechanical copying may use native PowerShell within the exact new path; no deletion/overwrite of old artifacts.
Provide a focused semantic diff from WI042, frozen new candidate hashes and reproducible local tests. Do not copy obsolete runnable recipes. Stop with a specific finding if a required change exceeds this diagnostic-only scope.

[TRACEABILITY REQUIREMENTS]
WI042 runtime SHA256 ad3e8a20ddb17fd1e3e4878ac2096bfa74a63561b9b1e8572b6bc2a7cda7a7b6; session 3092ae8c5598d9b97e1dedc6e0bb74ca9c0d3a8989493465114229d45727257a. Branch main/HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6, unrelated dirty work preserved. Original native cause stays NOT_DETERMINED; local diagnostic readiness is not deployment readiness. On completion immediately return to MA to trigger WI053; no native follow-up within this approval.
