---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-003.md
    reason: Approved native attempt and evidence integration
---
# Handoff: WI-20261011-ATS-028

[WI HEADER]
WI ID: WI-20261011-ATS-028
REQ: REQ-20261011-ATS-003
Agent: docops
Depends On: WI026/WI027 and MA one-attempt result plus independent postcheck
Blocks: none within current approval; cleanup diagnostic changes require new user approval

[WI SUMMARY]
Why: Current entry must distinguish real private startup success from automated cleanup HOLD and preserve the failed historical attempt.
Scope: Update docs/index.md current-entry with a concise new checkpoint; update REQ003 progress/DoD and retain active/HOLD, not complete. Create own evidence pack, concise summary, sanitized machine receipt. Add missing metadata to WI026/027 handoffs only if needed for document validation; do not alter contracts or frozen runtime/readme/reports.
DoD: Correct current evidence, times/owners/limits, exact pointers/pins, docs validation, no claim of overall PASS or publication. No lengthy architecture analysis.
Forbidden: No source/config/canonical manifest change, historical evidence rewrite, server/SSH/browser/secret access, app/DB operation, tests that boot apps, installation, deletion, commit/push or memory writes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] New latest checkpoint ahead of preserved REQ002 history.
- [ ] First app startup and GET success distinct from cleanupFAILED and bounded independent recovery state.
- [ ] Latest source/JAR pin and exact current preflight6b1d7 recorded; old snapshot mistake transparent.
- [ ] Fresh one-attempt exhausted; no automatic retry; overall REQ active/HOLD.
Quality:
- [ ] Validate links/docs and scoped diff check, report preexisting unrelated findings without fixing.
- [ ] Brief two-set deliverables and JSON receipt; evidence owner MA, not new DocOps native verification.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md, documentation-standards.md, development-standards.md, glossary.md (already injected to this agent; retained)
Tier1: docs/policies/security-policy.md; .claude/agents/docops.md (injected with task)
Tier2: docs/index.md#current-decision-entry; .agents/skills/validate-docs/SKILL.md; .agents/skills/create-wi-evidence-pack/SKILL.md
Context: REQ003, WI022-027 handoffs/evidence, WI020-artifact.json; build/WI026-private-start/freeze.json, recipe-pins.json, source-preservation.json; frozen helper/readme READONLY.

[OUTPUT CONTRACT]
- deliverables/agent/WI-20261011-ATS-028-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-028-native-result.json
- deliverables/user/WI-20261011-ATS-028-summary.md
- docs/index.md (new checkpoint only + appropriate version)
- deliverables/user/REQ-20261011-ATS-003.md (incremental current progress, approved/active/HOLD, do not close)
- optional YAML metadata only WI026/027 handoffs.

[MA EVIDENCE - 2026-10-11 KST]
Historical WI024: exact v2 attempt12:56:16 HOLD/PREPARE/PATH_ANCESTRY; app NOT_RUN, cleanupPASS. /var/log root:syslog0775 rejected strict root ancestor gate. Source failure retained; no parent permission change. WI026 uses new /var/lib/atstudio/wi026-private-logs root0700, root0600 log, fresh WI026 tool/upload/run/release/unit/approval/claim names.
WI026local tests42+8+3+4PASS; WI027 independently same+6PASS, overlap not63 distinctnativeproofs. PG027 conditionalALLOW exactbundle7136e749de6b7dd2ae6ecbf2585a3dc1ddb0fd083ef81ab48f96e2f82aa44f05,READMEe2305e742db1af5da6a272c31d3b641fc2386e96053f7511fb99035c23c52360. Frozen alloldWI02421files preserved.
MA13:03:30 source641/641 unchanged, main/58b1011dad796017e47dbcc82a4225ca449bcdf6 dirty. JAR70306351bytesSHA01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb. No source rebuild this task.
MA13:07 accidentally used historical preflightfbd216, got mysql_configfalse because loopback Xbind rejected. No config edits/retries were made to correct it; selected already-approved superseding build/WI014-final-xbind-preflight-80d4a7.py hash6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383. Latest13:20:09 exactcurrentpreflight23/24true, solefalse historical release-readiness-approval absence expected/preserved. No blanket24/24PASS.
MA13:19:49 fresh fourpublicfiles upload;13:19:57 exactrootstage STAGED_PUBLIC_FILES=4;13:20:16 freshrootapprovalcreated; block3 invoked exactly once.
Native receipt13:20:33.780KST, ssh_exit2, stderr_emptytrue:
{"app_start":"VERIFIED","bpf_attachment_checked":true,"capability_gets":1,"cleanup":"FAILED","cleanup_code":"CLEANUP_VERIFICATION_FAILED","internet_proof":"NOT_RUN","negative_deliveries":0,"proof_scope":"ON_HOST_ONLY","stage":"READ_ONLY_CHECKS","status":"HOLD","tag_gets":1,"tcp_positive_controls":2,"udp_positive_controls":2}
VERIFIED reached after readiness/strictaudit markers, disabled authentication/mail capability flags, exactemptyTagDTO, no appchildren/knownworkerthreads, unchangedpolicy/BPF and equal43tablecounts (subscriptions6,rest0). Background masterOFF rests on exact unit/overlay/source review, not a scheduler capability endpoint. Matched row counts are not a full SQL audit or proof against transient DML/updates. The helper compresses failures in cleanup into genericcode, so rootcauseNOTDETERMINED. Do NOTclaim cleanupfalse is provenfalsepositive or fullrestorationPASS.
MAindependentmetadata immediatelyafter: appunit atstudio-first-start-wi026.service LoadStatemasked/ActiveStatefailed/SubStatefailed/UnitFileStatemasked/MainPID0/ControlPID0/Resultexit-code. ExecMainCode1/ExecMainStatus143;started13:20:23,ended13:20:32. MySQLinactive/dead/masked/PID0/Resultsuccess. atstudio.serviceabsent. nginxactiveenabledsamePID11156;certbottimeractiveenabled.
MAindependent13:22:09: no java/ffmpeg/mysqld/atstudio processes, no pendingjobs, both/etc/systemd/system/mysql.service and appunit masklinks exactly/dev/null. ssbaselineunchanged9rows (5IPv4TCP +1IPv6TCP +3UDP); no3306/33060/33062/8080/8081/55124/55125ports. ExistingTLS3publicpinsmatch. No rawlogs/privateenvcontentsprinted.
PGreadonlycauseanalysis: app masked/failed is explicitly accepted bycleanup, cannotitselfexplainfalse. Priorstop/mask returncodes/timeouts/transientchecks or finalbaselinecomparison mayfail; no per-stepcodewasretained, cannotreconstruct. KeeporiginalHOLD andseparateindependentpostcheck; do NOTrepeatstop/start toguess.
MA13:23:05 keyownedfingerprintuD4... removed, ssh-add-l reportednoidentities, serviceStopped/Disabled restored. UnlockPID31436alreadygone, nootherterminalkilled.
No additionalpurchase/install/firewall/DNSchange, no external app/DBpublication, no businessDML/DDL, realmail/payment/OAuth, deletion, commit/push, automaticretry.
Residual: exactcleanupfailurecauseunresolved; no overallREQcompletion, no internet/IPv6deliveryproof, nofullsite/frontend/auth/media/load/mail/backuptest. Originalcanonicalapp_runnablefalse/productionHOLD unchanged. Firstboundedprivatebackendstartup nowverified, notproductionready.

[TRACEABILITY]
Preserve old receipts/hashes; add new currentcheckpoint. State observationtime/MAownership separately from docupdate. Nextpossible cleanup-step diagnostic hardening or anothernativeattempt requires userapproval; do not create or perform it.
