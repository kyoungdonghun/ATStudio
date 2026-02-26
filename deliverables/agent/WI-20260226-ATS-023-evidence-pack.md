# WI-20260226-ATS-023 Evidence Pack

## WI: Whitelist Channels 전체 구현 (12.1~12.4)
## REQ: REQ-20260226-ATS-006
## Date: 2026-02-26
## Agent: se

---

## 1. File Changes

### New Files (6)

| # | File | Lines |
|---|------|-------|
| 1 | `src/main/java/com/atstudio/atstudio/dto/whitelist/WhitelistChannelRequest.java` | 9 |
| 2 | `src/main/java/com/atstudio/atstudio/dto/whitelist/WhitelistChannelResponse.java` | 20 |
| 3 | `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java` | 105 |
| 4 | `src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java` | 71 |
| 5 | `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java` | ~240 |
| 6 | `src/test/java/com/atstudio/atstudio/controller/WhitelistChannelControllerTest.java` | ~180 |

### Modified Files (2)

| # | File | Change |
|---|------|--------|
| 1 | `src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java` | Added `update(String, String)` method (L30-33) |
| 2 | `src/main/java/com/atstudio/atstudio/repository/WhitelistChannelRepository.java` | Added `findByUserOrderByCreatedAtDesc(User)`, `countByUser(User)` |

---

## 2. Implementation Details

### Business Logic (WhitelistChannelService)

| Method | Validations | Error Codes |
|--------|------------|-------------|
| `registerChannel()` | URL contains "youtube.com", active subscription, channel count < max | INVALID_ARGUMENT, NO_ACTIVE_SUBSCRIPTION, WHITELIST_CHANNEL_LIMIT_EXCEEDED |
| `getMyChannels()` | User exists | RESOURCE_NOT_FOUND |
| `updateChannel()` | Channel exists, ownership, URL validation | RESOURCE_NOT_FOUND, RESOURCE_NOT_ACCESS, INVALID_ARGUMENT |
| `deleteChannel()` | Channel exists, ownership | RESOURCE_NOT_FOUND, RESOURCE_NOT_ACCESS |

### Standards Compliance

- Entity-DTO separation: WhitelistChannelResponse.from(entity) static factory
- @Transactional(readOnly=true) class-level, @Transactional on mutating methods
- Controller thin: delegates all logic to service
- DTOs as Java records
- Lombok: @RequiredArgsConstructor, @Getter, @Builder
- Test: JUnit5 + Mockito (service), @SpringBootTest + MockMvc (controller)

---

## 3. Test Cases

### WhitelistChannelServiceTest (10 cases)

| # | Test | Assertion |
|---|------|-----------|
| 1 | registerChannel_success | id=1L, channelUrl, channelName match |
| 2 | registerChannel_invalidUrl | INVALID_ARGUMENT |
| 3 | registerChannel_noSubscription | NO_ACTIVE_SUBSCRIPTION |
| 4 | registerChannel_limitExceeded | WHITELIST_CHANNEL_LIMIT_EXCEEDED |
| 5 | getMyChannels_success | size=2, names match |
| 6 | updateChannel_success | updated URL/name |
| 7 | updateChannel_notOwner | RESOURCE_NOT_ACCESS |
| 8 | updateChannel_notFound | RESOURCE_NOT_FOUND |
| 9 | deleteChannel_success | verify delete called |
| 10 | deleteChannel_notOwner | RESOURCE_NOT_ACCESS |

### WhitelistChannelControllerTest (12 cases)

| # | Test | Expected Status |
|---|------|----------------|
| 1 | POST unauthenticated | 401 |
| 2 | POST authenticated | 201 |
| 3 | POST invalid URL | 400 |
| 4 | POST no subscription | 403 |
| 5 | GET unauthenticated | 401 |
| 6 | GET authenticated | 200 |
| 7 | PUT unauthenticated | 401 |
| 8 | PUT authenticated | 200 |
| 9 | PUT forbidden | 403 |
| 10 | DELETE unauthenticated | 401 |
| 11 | DELETE authenticated | 204 |
| 12 | DELETE forbidden | 403 |

---

## 4. Build Verification

```
BUILD SUCCESSFUL
Total tests: 345 (323 existing + 22 new)
Failures: 0
```

---

## 5. Patterns Referenced

- Service pattern: QuestionService.java
- Controller pattern: QuestionController.java
- Service test pattern: QuestionServiceTest.java
- Controller test pattern: QuestionControllerTest.java

---

## 6. Follow-up

- SecurityConfig: `/api/whitelist-channels/**` is covered by `.requestMatchers("/api/**").authenticated()` -- no changes needed
- No new BUSINESS_ERROR entries needed (all pre-existing)
