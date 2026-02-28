# WI-20260227-ATS-030 Evidence Pack — cr-B: Subscription·Whitelist·DownloadQueue·Likes

## cr-B 검토 결과: Subscription·Whitelist·DownloadQueue·Likes

### 6.x Subscription

| 도메인 | API | 판정 | 발견 이슈 | 파일:라인 |
|--------|-----|------|----------|---------:|
| Subscription | 6.1 GET /api/subscriptions | ⚠️ | CR-B-004: 잘못된 `userType` 문자열 → `UserType.valueOf()` → `IllegalArgumentException` → 500 (400이어야 함) | `SubscriptionService.java:25` |
| Subscription | 6.2 GET /api/subscriptions/{id} | ✅ | - | `SubscriptionService.java:37-41` |
| Subscription | 6.3 POST /api/user-subscriptions | ✅ | BUSINESS 인증 체크, 중복 구독 방지, 201 Created | `UserSubscriptionController.java:23-33` |
| Subscription | 6.4 GET /api/user-subscriptions/me | ✅ | 활성 구독 조회 | `UserSubscriptionController.java:37-45` |
| Subscription | 6.5 GET /api/user-subscriptions | ✅ | @EntityGraph 페이지네이션, DESC 정렬 | `UserSubscriptionController.java:49-54` |
| Subscription | 6.6 GET /api/user-subscriptions/{id} | ✅ | 단건 조회 | `UserSubscriptionController.java:58-66` |
| Subscription | 6.7 PUT /api/user-subscriptions/me | ❌ | CR-B-003: `proratedAmount.abs()` — 다운그레이드 환불(-) 을 추가 청구(+)로 변환 | `UserSubscriptionService.java:176` |
| Subscription | 6.8 PUT /api/user-subscriptions/{id} | ✅ | 관리자 상태/주기/만료일 수정 | `UserSubscriptionController.java:84-93` |
| Subscription | 6.9 DELETE /api/user-subscriptions/{id} | ❌ | CR-B-001: `ResponseEntity.ok()` 반환 (명세: `204 No Content`) | `UserSubscriptionController.java:97-103` |
| Subscription | 6.10 DELETE /api/user-subscriptions/me | ❌ | CR-B-002: `ResponseEntity.ok()` 반환 (명세: `204 No Content`) | `UserSubscriptionController.java:107-114` |

#### 6.x Subscription 비즈니스 규칙

| 규칙 | 판정 | 파일:라인 |
|------|------|----------|
| RULE-SUB-001: BUSINESS 회원 기업 인증 체크 | ✅ | `UserSubscriptionService.java:55-61` — `COMPANY_CERTIFICATION_REQUIRED` |
| RULE-SUB-002: 활성 구독 중복 방지 | ✅ | `UserSubscriptionService.java:63-67` — `SUBSCRIPTION_ALREADY_EXISTS` |
| RULE-SUB-003: 만료일 계산 (MONTHLY/YEARLY) | ✅ | `UserSubscriptionService.java:72-74` |
| RULE-SUB-004: Mock 결제 추상화 | ✅ | `PaymentService` interface + `MockPaymentServiceImpl` (`@Primary`) |
| RULE-SUB-005: proratedAmount 계산 | ✅ (부호) / ❌ (abs) | `UserSubscriptionService.java:159-167` — 계산 로직 정상, `line:176` `.abs()` 호출로 부호 소실 |
| RULE-SUB-006: 관리자 취소 응답 204 | ❌ | `UserSubscriptionController.java:97-103` — `ResponseEntity.ok()` 반환 |
| RULE-SUB-007: 셀프 취소 응답 204 | ❌ | `UserSubscriptionController.java:107-114` — `ResponseEntity.ok()` 반환 |

#### CR-B-003 상세: proratedAmount.abs() 버그

```
[정상 케이스: 업그레이드]
  refundAmount = 남은일수/전체일수 × 현재플랜가격   (e.g., 3,000원)
  newPrice = 새 플랜 가격                          (e.g., 10,000원)
  proratedAmount = 10,000 - 3,000 = +7,000원      ← 추가 청구 (정상)
  .abs() → +7,000원                               ← 동일 (무해)

[버그 케이스: 다운그레이드]
  refundAmount = 5,000원
  newPrice = 3,000원
  proratedAmount = 3,000 - 5,000 = -2,000원       ← 환불 (정상)
  .abs() → +2,000원                              ← 환불을 추가 청구로 반전 ❌

결과: ChangeSubscriptionResponse.proratedAmount 필드는 -2,000원을 반환하지만,
      실제 Mock 결제는 +2,000원으로 처리됨 (UserSubscriptionService.java:174-176)
```

---

### 12.x Whitelist Channels

| 도메인 | API | 판정 | 발견 이슈 | 파일:라인 |
|--------|-----|------|----------|---------:|
| Whitelist | 12.1 POST /api/whitelist-channels | ⚠️ | CR-B-005: URL 검증 `contains("youtube.com")` → `evil.site/youtube.com` 통과 | `WhitelistChannelService.java:98` |
| Whitelist | 12.2 GET /api/whitelist-channels | ✅ | 소유자 채널 목록, DESC 정렬 | `WhitelistChannelService.java:62-67` |
| Whitelist | 12.3 PUT /api/whitelist-channels/{id} | ⚠️ | CR-B-005: 동일 URL 검증 취약점 | `WhitelistChannelService.java:98` |
| Whitelist | 12.4 DELETE /api/whitelist-channels/{id} | ✅ | 204 No Content, 소유권 체크 | `WhitelistChannelController.java:66-72` |

#### CR-B-005 상세: Whitelist URL 검증

```java
// WhitelistChannelService.java:97-101
private void validateChannelUrl(String channelUrl) {
    if (!channelUrl.contains("youtube.com")) {         // ← 단순 포함 체크
        throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
    }
}

// 우회 가능한 URL 예시:
// evil.site/youtube.com/redirect     → "youtube.com" 포함 → 통과
// youtube.com.evil.site              → "youtube.com" 포함 → 통과
// https://www.youtube.com/@channel  → 정상 YouTube URL → 통과 (OK)

// 권장 수정: URL 파싱 후 host 체크
// URI uri = new URI(channelUrl);
// boolean valid = uri.getHost() != null &&
//                 (uri.getHost().equals("youtube.com") || uri.getHost().endsWith(".youtube.com"));
```

#### 12.x Whitelist 비즈니스 규칙

| 규칙 | 판정 | 파일:라인 |
|------|------|----------|
| 채널 한도 초과 검증 | ✅ | `WhitelistChannelService.java:44-48` — `countByUser >= maxWhitelistChannels` |
| 활성 구독 체크 | ✅ | `WhitelistChannelService.java:40-42` — `NO_ACTIVE_SUBSCRIPTION` |
| 소유권 체크 | ✅ | `WhitelistChannelService.java:113-116` — `checkOwnership()` |
| URL 도메인 검증 | ⚠️ | `WhitelistChannelService.java:97-101` — 우회 가능 (CR-B-005) |

---

### 11.x Download Queue

| 도메인 | API | 판정 | 발견 이슈 | 파일:라인 |
|--------|-----|------|----------|---------:|
| DownloadQueue | 11.1 POST /api/download-queue/{trackId} | ✅ | 201 Created | `DownloadQueueController.java:22-31` |
| DownloadQueue | 11.2 GET /api/download-queue | ✅ | 목록 조회 | `DownloadQueueController.java:33-40` |
| DownloadQueue | 11.3 DELETE /api/download-queue/{trackId} | ✅ | 204 No Content | `DownloadQueueController.java:42-48` |

**DownloadQueue 도메인: 이슈 없음 — CLEAN**

---

### 10.x Likes

| 도메인 | API | 판정 | 발견 이슈 | 파일:라인 |
|--------|-----|------|----------|---------:|
| Likes | 10.1 POST /api/likes/{trackId} | ✅ | 201 Created, 복합PK existsById 체크 | `LikeController.java:22-31` |
| Likes | 10.2 GET /api/likes | ✅ | 목록 조회 | `LikeController.java:33-40` |
| Likes | 10.3 DELETE /api/likes/{trackId} | ✅ | 204 No Content | `LikeController.java:42-48` |

**Likes 도메인: 이슈 없음 — CLEAN**

---

## 레이어별 코딩 표준 준수

| 파일 | @Transactional(readOnly) 클래스 | DTO 분리 | @RequiredArgsConstructor |
|------|--------------------------------|---------|--------------------------|
| SubscriptionService.java | ✅ (line 15) | ✅ | ✅ |
| UserSubscriptionService.java | ✅ (line 36) | ✅ | ✅ |
| WhitelistChannelService.java | ✅ (line 23) | ✅ | ✅ |
| DownloadQueueService.java | ✅ | ✅ | ✅ |
| LikeService.java | ✅ | ✅ | ✅ |
| SubscriptionController.java | N/A | ✅ | ✅ |
| UserSubscriptionController.java | N/A | ✅ | ✅ |
| WhitelistChannelController.java | N/A | ✅ | ✅ |
| DownloadQueueController.java | N/A | ✅ | ✅ |
| LikeController.java | N/A | ✅ | ✅ |
| 모든 Entity | N/A | N/A | @Setter 없음 ✅ |

---

## 발견 이슈 종합 목록

| # | 심각도 | 파일:라인 | 이슈 | 권장 조치 |
|---|--------|---------|------|---------:|
| CR-B-001 | ❌ MAJOR | `UserSubscriptionController.java:97-103` | 6.9 관리자 취소: `ResponseEntity.ok()` (명세: `204 No Content`) | `ResponseEntity.noContent().build()` 변경 |
| CR-B-002 | ❌ MAJOR | `UserSubscriptionController.java:107-114` | 6.10 셀프 취소: `ResponseEntity.ok()` (명세: `204 No Content`) | `ResponseEntity.noContent().build()` 변경 |
| CR-B-003 | ❌ MAJOR | `UserSubscriptionService.java:176` | 업/다운그레이드: `proratedAmount.abs()` → 다운그레이드 환불(-)을 추가 청구(+)로 변환 | `.abs()` 제거, Mock 결제에 절대값 전달 시 별도 변수 사용 |
| CR-B-004 | ⚠️ MINOR | `SubscriptionService.java:25` | 잘못된 `userType` 문자열 → `IllegalArgumentException` → 500 에러 (400이어야 함) | `try-catch` 또는 `Arrays.stream(UserType.values()).filter()` 패턴으로 교체 |
| CR-B-005 | ⚠️ MINOR | `WhitelistChannelService.java:98` | URL 검증 `contains("youtube.com")` → `evil.site/youtube.com` 우회 가능 | `URI.getHost()` 기반 정확한 도메인 검증으로 교체 |
| CR-B-006 | 📋 제안 | `UserSubscriptionService.java:88-90` | 6.3 구독 시 `paymentService.processPayment()` 실패 시 롤백 처리 미명시 | `@Transactional` 범위 내 있으므로 자동 롤백되나, 실 PG 전환 시 재검토 필요 |
| CR-B-007 | 📋 제안 | `UserSubscriptionService.java:150-162` | proratedAmount 응답(`ChangeSubscriptionResponse`)과 실 결제 금액 불일치 가능성 | Mock → 실 PG 전환 시 응답 필드 의미 명세화 필요 |
