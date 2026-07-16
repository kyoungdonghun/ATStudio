[WI HEADER]
WI ID: WI-20260716-ATS-006
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-004
Blocks: WI-20260716-ATS-011, WI-20260716-ATS-012, WI-20260716-ATS-013

[WI SUMMARY]
Why: 결제 대사의 고정 100건·active agreement 전수 조회를 bounded keyset batch로 바꾸고, 빌링키 암호화의 시작 검증·키 버전/교체 호환성과 스케줄러 시간대를 운영 가능한 계약으로 고정한다.
Scope (in/out):
- In: P2-03, P2-04의 단일 서버 범위, P2-18의 fresh-schema/index/EXPLAIN 준비, X-01의 migration rehearsal 문서 경계. Local reconciliation keyset/batch, issue aggregation bounds, query-aligned indexes, versioned key ring with legacy ciphertext compatibility, startup fail-closed when recurring Toss is enabled, explicit scheduler zone, tests·schema·manual patch·payment docs.
- Out: 멀티서버 scheduler lock, 실제 retained/운영 DB 적용, live Toss 호출, secret rotation 실행, PostgreSQL, 클라이언트 브랜치.
DoD:
- local payment order와 active agreement 대사가 configurable/bounded keyset batch로 전체 후보를 처리하고 고정 최근 100건 또는 unbounded entity list를 사용하지 않는다.
- issue detail/incident persistence가 unbounded 메모리 축적 없이 모든 batch를 처리한다.
- fresh schema와 manual migration에 필요한 query-aligned index가 정렬되고 MySQL 검증 또는 재현 가능한 EXPLAIN 절차가 있다.
- 새 billing key ciphertext는 명시적 key ID를 가진 versioned envelope를 사용하고 기존 v1 envelope를 계속 복호화한다.
- active key와 모든 decryption key를 시작 시 검증하고 placeholder/blank/unknown key ID는 fail-closed 한다. 비밀값은 출력하지 않는다.
- 결제 스케줄러는 `Asia/Seoul` 기본의 configurable zone을 명시한다. 단일 서버 정책을 문서화하고 분산 lock은 추가하지 않는다.
- 집중 테스트, schema/doc validation, summary, Evidence Pack이 완료된다.
Constraints/Forbidden:
- 기존 v1 ciphertext를 일괄 재암호화하거나 DB 데이터를 직접 변경하지 않는다.
- billing key·secret·provider payload 원문을 로그/테스트 산출물/문서에 기록하지 않는다.
- 결제 성공·실패·idempotency·환불·구독 정책을 변경하지 않는다.
- live provider와 클라이언트 worktree를 건드리지 않는다.
- 다른 작업자의 변경을 되돌리지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 100건을 넘는 local orders/agreements도 batch 경계를 넘어 검사되고 mismatch count와 incident가 보존된다.
- [ ] 마지막 page, 빈 page, 중간 삭제/skip에도 keyset loop가 종료되고 중복 처리하지 않는다.
- [ ] v2/key-ID envelope round trip, legacy v1 decrypt, unknown/removed key 실패, active key rotation 호환 테스트가 통과한다.
- [ ] TOSS_BILLING 활성 시 잘못된 key ring은 startup에서 실패하고 MOCK/비결제 개발 경로는 기존 정책을 유지한다.
- [ ] 모든 결제 관련 cron이 명시적 zone을 사용한다.
Performance:
- [ ] batch size가 설정 가능하고 repository query가 ID keyset 및 대응 index를 사용한다.
- [ ] local mismatch detail·incident 처리의 메모리 사용이 batch/명시적 cap으로 제한된다.
Quality:
- [ ] focused payment/crypto/scheduler/schema tests와 관련 전체 payment tests가 통과한다.
- [ ] schema.sql, manual patch, API/DB/payment/runbook 문서가 정렬되고 docs validation·diff check가 통과한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Required for se):
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Task Context):
- docs/design/remaining-remediation-design-20260716.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/design/p1-payment-db-integrity-design.md
- docs/design/db-schema.md
- docs/design/api-spec.md
- docs/payment/system-overview.md
- docs/payment/known-limits-and-next-steps.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-004-summary.md
- deliverables/user/WI-20260715-ATS-020-summary.md

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java
- src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java
- src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java
- src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java
- src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionServiceTest.java
- src/test/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCryptoTest.java

Repro/Logs:
- `gradlew.bat test --tests "*PaymentReconciliation*" --tests "*BillingKeyCrypto*" --tests "*SubscriptionScheduler*"`
- MySQL disposable-schema validation command from current payment DB integrity evidence

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-006-summary.md :
- Runtime behavior, compatibility, schema/index proof, remaining X-01 boundary
Agent-facing -> deliverables/agent/WI-20260716-ATS-006-evidence-pack.md :
- Standard Evidence Pack with Tier references, changed files/lines, commands, tests, rollback, follow-ups
Handoff Packet -> deliverables/agent/WI-20260716-ATS-006-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: focused payment/crypto/scheduler tests, schema validation where available, docs validation, diff check
Rollback: Revert code/config/docs and additive DDL before any deployment; never discard an old decryption key while v1/key-ID ciphertext still depends on it.
