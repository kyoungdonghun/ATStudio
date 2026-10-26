# Stage-two operating decisions and local preflight

Observed 2026-10-25 KST; owner SE / WI-20261025-ATS-005.
Status: LOCAL_PREPARED_NOT_APPLIED. No service, account, mail, billing or DNS operation.

## Fixed decisions

- Deployment approval: user. Reviewed execution, rollback and evidence: MA.
- Reuse IDs1/2 ADMIN, ID3 USER, the daily-five nonbilling grant through Nov24 and six existing plans. No seeding, deletion or billing-policy edits.
- Keep one approved application JVM as the scheduler owner. No duplicate OS cron invoking application renewals and no second app process running the same scheduled work. Backup scheduling is a separate stage-three responsibility.
- Existing Gmail stays active until a separately verified NCP switch. Preserve its actual SMTP auth/TLS settings, not false template defaults. ID1 mailbox is the selected reply/operations destination; no extra mailbox purchase or repeated address input.
- Reuse current billing encryption key ring and recovery reference. Never replace it with the acceptance bundle. TEST and LIVE credentials are separate; existing TEST presence is not contract/MID validity.
- Existing production storage, datasource, JWT and effective origin stay unchanged. Known public candidate origin is https://atmusic.co.kr with checkout success/fail paths already in the first-start template; compare effective origin before target changes.

## Modes: existing implementation, not a new scheduler

| Mode | master enabled | audio-only | audio worker | Meaning |
|---|---|---|---|---|
| Cold preparation | false | true | false | No background worker or Spring scheduled jobs. App remains stopped in the current session. |
| Bounded audio candidate | true | true | true | Audio executor enabled; Spring scheduled jobs and startup storage recovery suppressed. Requires exact runtime scope, effective settings and job checks before startup. |
| Full operations (not executable candidate here) | true | false | true | General schedules active, including provider calls and data mutations; requires provider and job activation gates. |

`operating-modes.json` contains only the first two candidate overlays. It is NOT an env file and symbolic/private bindings must never be exported. `APP_ACCEPTANCE_ENABLED=false`, test bootstrap=false, DDL validate, SQL logging=false, Swagger=false and notifications=false are explicit. It does not overwrite DB, keys or SMTP.

Evidence: BackgroundTasksEnabledCondition.java:17-27 separates master/audio-only; BackgroundTasksConfiguration.java:15-35 guards scheduler registration; TrackAudioProcessingWorker.java:31-40 uses its own executor. StorageMutationRecoveryService.java:42-46 suppresses startup recovery in audio-only mode.

**Audio-only is not a payment kill switch.** Synchronous checkout/upgrade/refund APIs remain reachable according to app authorization, and WithdrawalBillingCleanupCoordinator.java:19-27 still handles after-commit events. Disabled operator notifications also do not suppress authentication or all other mail. Therefore neither candidate is a safe blanket LIVE/no-side-effect mode. Do not expose payment flows with LIVE keys or trigger withdrawal during a no-payment audio trial.

Full-operation jobs owned by the single app: renewal 00:00, pending-order expiry00:10, subscription expiry00:30, reconciliation01:00 and withdrawal cleanup01:15, all Asia/Seoul. Storage recovery is periodic (default60s) and startup-driven; audio polls via its own executor (default5s). List pending work before enabling any of them. No activation was performed.

## Reusable local commands

From repository root, Python3.10+ standard library:

```powershell
& "$env:LOCALAPPDATA/Python/pythoncore-3.14-64/python.exe" -B build/WI-20261025-ATS-005/check_local.py
& "$env:LOCALAPPDATA/Python/pythoncore-3.14-64/python.exe" -B -m unittest discover -s build/WI-20261025-ATS-005 -p test_modes.py -v
& "$env:LOCALAPPDATA/Python/pythoncore-3.14-64/python.exe" -B scripts/deployment/validate_bundle.py --json
```

The first command reads a fixed allowlist of 11 public source/preparation paths and the known JAR, detects drift and rejects scheduling/seeding/unknown env injection into the candidate. It never reads process env, private files, invokes a process, or uses network/DB. Fixed-path manifest remains trusted local reviewed input; no hostile concurrent-filesystem sandbox is claimed. The existing bundle validator is reused unchanged. Its template PASS still reports production HOLD; it is not a live operational readiness test and its generic first-deployment gates must be reconciled with the latest completed target evidence, not used to recreate DB.

## Concrete MA target preflight and rollback

1. Reuse the reviewed WI014 probe builder and strict SSH transport from the successful WI003 sequence; fresh time/host/boot/source/JAR identity and cold baseline. Do not rerun stage-one trials or apply account/grant again.
2. Before any operational overlay, capture a bounded protected manifest of effective systemd unit, drop-ins, EnvironmentFile references, actual JVM argv and relevant environment origins. Report only safe key names, flags and identity hashes; preserve secret values privately. Tracked `/etc/atstudio/atstudio.env` is a template reference, not proof of all active overrides. No current target snapshot was collected by this WI.
3. Reconcile selected candidate with actual origins, storage, SMTP, key ring and TEST mode. No whole acceptance-bundle import. Check exactly one future scheduler owner and pending-work inventory; stop if unknown or LIVE mutation could occur.
4. Stage a reviewed, root-only config candidate with exact changed-key allowlist and retained old-config hash. Validate without starting the app. No release pointer, unit, timer or DNS write is supplied by this local tool.
5. Runtime activation requires the concrete approved scenario and provider/cost boundaries. Start only that mode, obtain effective-config proof, then bounded behavior/cleanup receipts. Full operations is not authorized by a local PASS.
6. If configuration rollout fails, first stop the candidate runtime and establish DB/provider outcomes. Restore only exact previous config/release pointers from protected retained identities, preserving the existing storage and key ring. Do not restore the whole old DB, undo today's IDs/grant, delete audits, or replay uncertain payments/mail. Changed JARs require new review and hash; unchanged JAR remains f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81.
7. Repeat independent cold probe, restore approved temporary access, remove only task SSH key, stop/disable agent and logout provider. Reuse WI003 cleanup evidence pattern.

## Mail integration preparation boundary

Current EmailService centralizes SMTP send via JavaMailSender. A minimal selectable transport can preserve all templates and token semantics: default SMTP plus NCP MAILv2 transport using existing Java17 HTTP/HMAC and JSON facilities. It needs exact official contract, signature vector, response/timeout ambiguity tests, no automatic SMTP fallback after an ambiguous API send, and preserved EmailService auth/reset tests. No speculative API code or new dependency was added here; a changed artifact must not be called the historical verified JAR. Account/contract/provider values are supplied through the existing private mechanism at their actual boundary.

## Reviewed WI008 source-pin update

On 2026-10-25, WI006 reviewed the additive mail selector/configuration changes from WI008. Only the application.yml source pin in this manifest was refreshed to 1f3151a554124831997cca8979b4927a8f5bff4baf349afe9326e4d2da2955e6; all other source pins, operating modes and preserved historical JAR remain unchanged. Original manifest/result/hash list are retained with -v1 suffixes. The rerun checks current public source identity plus historical rollback JAR, not source-to-old-JAR equivalence. WI008 separately identifies/tests the new mail candidate JAR. SMTP remains the default; no NCP selector, credentials, deployment or activation was applied by WI005.
