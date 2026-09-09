
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A096: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a096). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260817-ATS-030 Summary

## 결과: PARTIAL - External-effect stage is BLOCKED

새 로컬 리허설 DB는 guarded loopback preflight와 current `schema.sql -> seed.sql` baseline으로 생성되었고, 43-table manifest 검증을 통과했습니다. 현재 브랜치는 `codex/v1-release-rehearsal-fixes`였으며, Spring은 새 리허설 경계에서 Hibernate `ddl-auto=validate`로 실제 기동되었습니다.

기존 클라이언트 리스너 `5173`/`8080`은 시작 전후 그대로 두었고, 리허설 전용 `18080`/`18081`/`18082`만 사용한 뒤 모두 종료했습니다. 리허설 DB와 storage는 보존했습니다.

## Verified Live Evidence

| Boundary | Result | Evidence |
|---|---|---|
| Runtime / persistence | PASS | Guarded preflight and repeated manifest validation passed: 43 tables, 511 columns, 175 indexes, 91 foreign keys, and 6 plans/plan keys. Spring reached ready on the isolated backend port with Hibernate validation enabled. |
| Storage recovery / scheduler boundary | PARTIAL | Spring task scheduler initialized in the clone-only runtime. Startup recovery was permitted only with the new DB and storage roots. Per-item recovery completion was not observable in this bounded run. |
| Provider / SMTP | PASS for non-execution boundary | No provider, payment, refund, or SMTP workflow was invoked. Provider and SMTP settings in the rehearsal process were fail-closed to an unbound loopback target; the bounded run did not overlap the payment cron. |
| API catalogue and tags | PASS | Live catalogue keyword request, tag request, public-capabilities request, and the isolated frontend proxy catalogue request returned HTTP 200. |
| Authentication / protected mutation | PARTIAL | Password login was intentionally disabled in this rehearsal runtime; unauthenticated session request returned 401. Two repeated protected playlist mutation requests both returned 401 before controller-side mutation. |
| Browser UI | BLOCKED | The isolated static UI proxy was ready, but the headless Chrome DevTools WebSocket returned HTTP 500 even after an owned-browser restart. No browser DOM result is claimed. |
| Playback metadata / waveform | BLOCKED | The current schema/seed clone contained no catalogue tracks, so no live track detail, duration, or waveform response existed to verify. No data was invented or imported. |

## Exact Blockers

1. The guarded schema/seed clone has no track records. Completing live playback metadata/waveform validation requires an approved, read-only data clone with representative track assets, or separately approved clone-only fixtures.
2. The local headless Chrome DevTools endpoint refused the WebSocket connection with HTTP 500. Browser DOM/navigation evidence was therefore not completed; only backend API and static-proxy reachability are verified.
3. Full authenticated-session and UI cancellation/retry behavior is not proven. This runtime deliberately disabled test-user bootstrap and password login to avoid creating extra fixture state outside the required fresh baseline.

## External-effect Decision

**Do not start the external-effect rehearsal stage.** Hibernate validation and safe API boundary checks passed, but the live browser paths, real playable-track response, and full authenticated mutation cancellation/retry evidence remain incomplete.

## Retention

- Retained: the newly created local rehearsal DB, isolated runtime root, and isolated storage.
- Stopped: only the runtime processes owned on ports `18080`, `18081`, and `18082`.
- Not performed: client/development DB or runtime requests/mutations, Cloudflare, SMTP delivery, Toss/provider calls, payments, refunds, renewals, settlement, source/tracked configuration changes, and deletion of any database or storage root.
