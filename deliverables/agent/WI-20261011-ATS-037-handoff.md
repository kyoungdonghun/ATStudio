# WI-20261011-ATS-037 Handoff Packet

[WI HEADER]
WI ID: WI-20261011-ATS-037
REQ: REQ-20261011-ATS-006
Agent: pg
Depends On: WI-20261011-ATS-035, WI-20261011-ATS-036
Blocks: MA bounded native web trial; WI-20261011-ATS-038
Session ID: current

[WI SUMMARY]
Why: Independently gate the exact restricted-web bundle and native execution recipe before any server configuration change or app/DB start.
Scope (in): Read the frozen WI035 candidate, WI036 source/artifact and route contract, independent target-free regressions, exact-byte/pin and operational recipe review.
Scope (out): No native SSH, browser actions, credentials/private environment reads, provider changes, uploads, app/DB start, purchases, packages, source fixes, commits or pushes.
DoD: Two-set verdict with exact reviewed SHA256 values and every MA command block pinned; actionable HOLD on any unproven containment/restore condition.
Constraints: Preserve all earlier failed/successful evidence. Do not edit implementation or relax tests. This is not a new approval to execute a trial.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exact current backend/frontend identity and a finite code-reviewed GET target list; no mutating GET, broad API pass-through, authenticated or payment/mail action.
- [ ] Existing management IPv4 only, provider firewall unchanged, Host/SNI and spoofed-forwarding denial, no raw/protected uploads, no unexpected static files, source maps or private env.
- [ ] CSP and routing block external OAuth/payment/mail/media requests; no cookie/token forwarding. Legitimate approved browser navigation and API queries remain possible.
- [ ] Existing app egress proof and disabled jobs/read-only operation remain effective. Review dynamic settings and any configuration provenance.
- [ ] App+DB maximum 1800 seconds, including startup and cleanup budget, enforced on server independent of local client disappearance. No automatic retry.
- [ ] Recoverable prior Nginx bytes/ownership/mode, syntax check, effective config activation, and exact restoration with old worker connections handled; ACME/certificate/renewal/provider rules preserved.
- [ ] Final app/DB stopped/masked, no app or probe listeners, no lingering jobs/temporary exposure, native failure diagnostics redacted and bounded.
- [ ] Fresh unique paths/claim and exact artifacts, no overwrite of history or secret bodies in command/output. Execution blocks gated against stale/wrong target.
Performance:
- [ ] Bounded CPU/memory/disk/log/time usage; no new load/capacity claim.
Quality:
- [ ] Independent target-free tests and meaningful negative/fault-path coverage.
- [ ] Mark native-only assumptions explicitly; never convert mocked PASS into target PASS.
- [ ] Report package/network installation requirements as HOLD.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/pg.md
- docs/policies/security-policy.md
Tier 2 and current evidence:
- docs/index.md (Current Decision Entry)
- deliverables/user/REQ-20261011-ATS-006.md
- deliverables/agent/WI-20261011-ATS-035-handoff.md
- deliverables/agent/WI-20261011-ATS-035-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-036-handoff.md
- deliverables/agent/WI-20261011-ATS-036-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-034-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-034-native-receipt.json
Files:
- build/WI035-restricted-web-check/ (read only; final bundle/recipe paths supplied on dispatch)
- build/WI036-web-contract/ (read only; exact route/identity artifact supplied on dispatch)
- deploy/single-vps/first-start/
- deploy/single-vps/tls-quarantine/
- frontend/src/api/client.ts
- frontend/src/pages/public/HomePage.tsx
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/
Authoritative native context:
- Previous WI034 BOUNDED_PASS was a different once-only attempt, not approval/reusable proof for this trial.
- MA 2026-10-11 14:36 KST read-only baseline: expected boot/JAR/TLS pins match; MySQL inactive masked; old app attempts masked PID0; Nginx and certbot active; root tty1 recovery present.
- This-turn SSH activation and local key unlock have now been explicitly approved and completed. Provider22/443 are management-only; no changes made.
- Re-check immediately before the real run. No candidate native upload or app/DB start has occurred.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20261011-ATS-037-summary.md
Agent-facing -> deliverables/agent/WI-20261011-ATS-037-evidence-pack.md
Optional target-free test receipts -> build/WI037-restricted-web-review/
Handoff -> this file.
Use create-wi-evidence-pack skill. Output English technical evidence; brief Korean summary may be in final response.

[TRACEABILITY REQUIREMENTS]
Required exact file hashes, test commands/outcomes, source line pointers, artifact identity, replay/restore limits, blockers and next-chain handoff.
Only assigned report/test-receipt paths writable. Findings require MA routing before any implementation change.

