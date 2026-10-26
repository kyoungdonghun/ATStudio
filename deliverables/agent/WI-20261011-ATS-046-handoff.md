---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Bounded trial and newly approved HTTPS-only recovery
  - path: WI-20261011-ATS-045-handoff.md
    reason: Actual trial and failure integration owner
---

# WI-20261011-ATS-046 Handoff

> Purpose: Prepare a narrow, independently reviewed recovery of the original TLS quarantine only.

[WI HEADER]
WI ID: WI-20261011-ATS-046
REQ: REQ-20261011-ATS-006
Agent: se
Depends On: MA actual failed WI042 trial and user recovery approval
Blocks: WI-20261011-ATS-047 PG -> MA one-time recovery -> WI045 final integration

[WI SUMMARY]
Why: Final native cleanup failed moving overlays across filesystems. The app and MySQL are stopped/masked and Nginx is stopped; two /run overlays remain. User approved HTTPS-only recovery, not another app trial.
Scope: NEW small recovery tool, synthetic tests, exact MA recipe and public pins under build/WI046-quarantine-recovery; two reports. No edits to any frozen WI042 or previous evidence, application, JAR, SPA, DB or normal deployment settings.
DoD: Fresh exact-target checks; durable preservation and same-filesystem detachment of two reviewed overlay files; daemon-reload; exact original Nginx/MySQL baseline and original TLS-byte checks before starting original Nginx once; postcheck app+DB remain stopped/masked and no application listeners. No retry if ambiguous.
Constraints: SE local only. No SSH, browser, network, remote secret reads, services or native execution. No deletion or overwrite. No app/MySQL start/unmask, reset-failed, source repair, packages, firewall, domain/certificate, purchases, commits or pushes. Keep recovery minimal and reviewable. Use apply_patch.

[APPROVAL SNAPSHOT]
User: "기존 HTTPS 복구만 승인" after this exact scope: preserve/detach the two Nginx/MySQL temporary configs; recover only prior HTTPS trial configuration after local tests/independent review; leave app/DB stopped/blocked; no data changes/public opening/new purchases; retain existing temporary SSH until recovery completes then remove/disable.
Initial trial remains consumed. No L2/L3 rerun or new app start. Existing failed result.json must remain untouched.

[ACTUAL TARGET SNAPSHOT]
2026-10-11 17:00:52 S4 dispatched once;17:01:12 OPEN/admission PASS/app_start VERIFIED.
17:01:23 approximately L2 emitted all17 API cases200/PASS and BOUNDED_HTTP_PASS, then its wrapper threw HOLD_ALLOWED_HTTP due to LASTEXITCODE check. No L3/browser work.
17:01:33 finish requested once. Final17:01:44/57 result HOLD/READ_ONLY_CHECKS/POST_STATE_CHANGED; view FINISHED; cleanup FAILED. This post-state cause is NOT_DETERMINED; do not conflate with recovery error or claim DB unchanged after window.
17:02:21 read-only: atstudio-restricted-web-wi042.service masked/failed/PID0; mysql masked/inactive/dead/PID0; nginx loaded/inactive/dead/enabled/PID0; atstudio-wi042-supervisor loaded/static/failed/PID0; certbot.timer active/waiting/enabled. No java/mysqld/nginx/ffmpeg processes.
Both /run/systemd/system/nginx.service.d/95-wi042-web.conf and /run/systemd/system/mysql.service.d/95-wi042-bound.conf exist and match their reviewed tools bytes. Each st_dev differs from /var/lib/atstudio/wi042-control; corresponding retained target absent. Source uses os.rename across these filesystems, so EXDEV is strongly supported, raw errno was not captured.
All original TLS bytes passed final cleanup checks. Preserve actual baseline.json/control/claims/results, past WI035/WI039 objects and all artifacts.

[ACCEPTANCE CRITERIA]
- [ ] Exact reviewed host/boot and file/type/owner/mode/ancestry/symlink/link-count checks; no wildcard mutation.
- [ ] Safely preserve exact overlays durably with O_EXCL/fsync and compare; detach only these two files by same-filesystem exclusive rename away from *.conf discovery. No file deletion/overwrite; collisions/partials HOLD.
- [ ] Handle Linux distinct /run and /var filesystems by design and test; do not rely on cross-device rename.
- [ ] Before original Nginx start, compare original service command arrays/order and TLS bytes, masks/stopped state, no live workers, nginx -t, no conflicting jobs.
- [ ] Start original quarantine Nginx at most once, verify command/config/cgroup/listeners; app+DB remain stopped/masked. Never relabel failed trial as success.
- [ ] No secrets/config bodies/logs/management-IP output. Only fixed safe receipt stages/checks. No source repairs for other defects in this WI.
- [ ] Local regression tests include collision, wrong pin, symlink, changed/running service, partial recovery, original service mismatch and failure to restart; bind exact command/hash pins for PG047.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md (same directory).
Tier1: docs/policies/security-policy.md.
Context: REQ006; WI043/044 evidence; WI045 handoff; MA snapshot above.
Read-only source: build/WI042-restricted-web-check/web_session.py (retain_overlay/restore/service/nginx_baseline/mysql_baseline); runtime.py; freeze.json; README.md; three original TLS pins in runtime.TLS. Reuse exact reviewed helpers when appropriate, with pins and no mutable imports.
Skills: create-wi-evidence-pack; test. Tier0 and security context retained from prior SE invocation, no change.

[OUTPUT CONTRACT]
Only build/WI046-quarantine-recovery/**, deliverables/agent/WI-20261011-ATS-046-evidence-pack.md and deliverables/user/WI-20261011-ATS-046-summary.md. Exact small recipe for MA; fresh root-private recovery record; freeze/public pins and local tests. No edits to REQ/index (DocOps owns).
Return ready-for-review summary promptly. MA immediately delegates WI047; do not run native tool or wait for server access.

[TRACEABILITY REQUIREMENTS]
Separate observed state, inferred EXDEV, undetermined POST_STATE_CHANGED and local-only test proof. Report public hashes, tested failure modes, bounded rollback/hold behavior. Recovery must fail closed and leave app/DB stopped. No unconditional guarantee against host failure.
