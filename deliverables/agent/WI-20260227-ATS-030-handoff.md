[WI HEADER]
WI ID: WI-20260227-ATS-030
REQ: REQ-20260227-ATS-009
Agent: cr
Depends On: WI-20260227-ATS-028
Blocks: WI-20260227-ATS-032

[WI SUMMARY]
Why: 백엔드 감사 Phase 2-B. Subscription·Whitelist·DownloadQueue·Likes 도메인 코드를 WI-028 체크리스트 기준으로 검토.
Scope (in):
  - 6.x Subscription (10 APIs), 12.x Whitelist (4 APIs), 11.x DownloadQueue (3 APIs), 10.x Likes (3 APIs) — 총 20개 API
  - Controller / Service / Repository / Entity / DTO 전 레이어
Scope (out): 코드 수정, 타 도메인 검토
DoD: 20개 API 각각 ✅/⚠️/❌/📋 판정, 이슈에 파일·라인 포함
Constraints/Forbidden: 코드 수정 절대 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] Subscription 6.1~6.10 전 API 검토 완료 (Mock 결제 흐름, proratedAmount, COMPANY_CERTIFICATION_REQUIRED 체크 포함)
  - [ ] Whitelist 12.1~12.4 전 API 검토 완료 (채널 한도 초과 검증)
  - [ ] DownloadQueue 11.1~11.3 전 API 검토 완료
  - [ ] Likes 10.1~10.3 전 API 검토 완료 (복합PK 중복 처리)
  - [ ] 각 항목 ✅/⚠️/❌/📋 판정
Quality:
  - [ ] ❌ 항목에는 파일명·라인번호 포함

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

검토 기준 (반드시 먼저 읽을 것):
  - deliverables/agent/WI-20260227-ATS-028-evidence-pack.md

검토 대상 파일:
  - src/main/java/com/atstudio/atstudio/controller/SubscriptionController.java
  - src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
  - src/main/java/com/atstudio/atstudio/service/SubscriptionService.java
  - src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
  - src/main/java/com/atstudio/atstudio/service/payment/PaymentService.java
  - src/main/java/com/atstudio/atstudio/service/payment/MockPaymentServiceImpl.java
  - src/main/java/com/atstudio/atstudio/entity/Subscription.java
  - src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
  - src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
  - src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java
  - src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java
  - src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java
  - src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java
  - src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java
  - src/main/java/com/atstudio/atstudio/entity/DownloadQueue.java
  - src/main/java/com/atstudio/atstudio/controller/LikeController.java
  - src/main/java/com/atstudio/atstudio/service/LikeService.java
  - src/main/java/com/atstudio/atstudio/entity/Like.java
  - src/main/java/com/atstudio/atstudio/dto/subscription/  (DTO 전체)
  - src/main/java/com/atstudio/atstudio/repository/  (관련 Repository)
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java

[OUTPUT CONTRACT]
User-facing  → deliverables/user/WI-20260227-ATS-030-summary.md
Agent-facing → deliverables/agent/WI-20260227-ATS-030-evidence-pack.md
  형식:
  ## cr-B 검토 결과: Subscription·Whitelist·DownloadQueue·Likes
  | 도메인 | API | 판정 | 발견 이슈 | 파일:라인 |

[TRACEABILITY REQUIREMENTS]
Evidence: 파일명·라인 포인터 필수 (❌/⚠️ 항목)
Rollback: Read-only → 불필요
