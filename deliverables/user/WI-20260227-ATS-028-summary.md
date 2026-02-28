# WI-20260227-ATS-028 Summary: Backend Audit Phase 1

> **WI**: WI-20260227-ATS-028
> **Role**: SA (Read-only document analysis)
> **Date**: 2026-02-27
> **REQ**: REQ-20260227-ATS-009

---

## 1. Document Coverage Summary

| Document | Items | Status |
|----------|-------|--------|
| API Spec v5 | 80 APIs (79 in summary table -- see discrepancy below) | Read complete |
| DB Schema v4 | 21 tables | Read complete |
| Use Cases v4 | 16 files, 79 UCs | Read complete |
| Development Standards | Java/Spring Boot coding rules | Read complete |
| Core Principles | Constitutional baseline | Read complete |

---

## 2. Top 10 Attention Points for Phase 2-3

These are the areas where cr/pg reviewers should focus the most effort during code review.

### HIGH RISK

| # | Area | Description | Phase 2 Reviewer |
|---|------|-------------|-----------------|
| 1 | **Subscription (6.x) -- 10 APIs unimplemented** | Entire subscription domain is unimplemented (0/10). PG payment integration, proration logic, BUSINESS certification gate, 1-user-1-subscription constraint (UNIQUE on user_id). This is the largest remaining risk surface. | cr-B |
| 2 | **Track Download (1.5) -- multi-concern orchestration** | Single API does: subscription check + daily download COUNT query + file serving + track_downloads record + license auto-issuance (dedup). Five concerns in one endpoint. N+1 risk on license lookup. | cr-A |
| 3 | **Inquiry visibility/access control (8.x)** | Complex permission matrix: public vs private inquiry, owner vs admin, OPEN-only delete for users, auto-status-transition on admin's first answer. Multiple 403 paths to verify. | cr-C |
| 4 | **Social Login two-step flow (5.3/5.10)** | isProfileComplete derived from nullable columns (phone_personal, job). Edge cases: partial completion, concurrent social+email registration with same email. | cr-C |

### MEDIUM RISK

| # | Area | Description | Phase 2 Reviewer |
|---|------|-------------|-----------------|
| 5 | **Playlist subscriber-only gate (3.x)** | All 8 playlist APIs require ACTIVE subscription. Verify subscription check is enforced at service layer, not just controller annotation. Subscription expiry mid-session edge case. | cr-A |
| 6 | **Whitelist channel limit enforcement (12.1)** | Must check current channel count against subscriptions.max_whitelist_channels. Race condition possible on concurrent registration. | cr-B |
| 7 | **Company Certification status transitions (13.5)** | PENDING->APPROVED generates UUID certification_code + records approved_at. Reapplication after REJECTED/REVISION_REQUESTED must be allowed. Duplicate PENDING/APPROVED prevention. | cr-C |
| 8 | **Play history + play_count sync (4.1)** | play_count increment must be atomic. Concurrent play requests could cause lost updates without proper locking or atomic increment. | cr-A |

### LOW RISK (but verify)

| # | Area | Description | Phase 2 Reviewer |
|---|------|-------------|-----------------|
| 9 | **Composite PK entities (likes, download_queue)** | save() = merge() for @EmbeddedId entities. Must use explicit existsById() check, not rely on DataIntegrityViolationException. Known lesson learned. | cr-B |
| 10 | **Tag deletion cascade (2.4)** | Tag deletion must first delete track_tags records (application-level, not DB cascade). Confirmed lesson learned. | cr-A |

---

## 3. Document Discrepancies / Ambiguities

| # | Issue | Location | Severity |
|---|-------|----------|----------|
| 1 | **API count mismatch**: Heading says "Full API Summary (80)" but table totals 79 | api-spec.md line 1583 vs line 1601 | Low (cosmetic) |
| 2 | **Admin subscription delete (6.9) ambiguity**: UC PAYMENT-009 says "deletes record (or sets status=CANCELLED)". Which one? Physical delete or soft cancel? | user-subscription.md PAYMENT-009 | Medium -- cr-B must verify implementation choice |
| 3 | **Track soft delete vs tag cascade**: SOUND-016 UC says "deletes tag mappings from track_tags" on soft delete. But soft delete = is_active=0, so why physically delete mappings? Could cause data loss if track is reactivated. | sound-track.md SOUND-016 | Low -- design decision, but cr-A should verify consistency |
| 4 | **Subscription plan seed data pricing**: All prices are [TBD]. If tests reference specific amounts, they must use test fixtures, not hardcoded prices. | db-schema.md subscriptions seed data | Low |
| 5 | **business-license.md still exists**: Redirect file exists at `docs/design/usecase/business-license.md` pointing to company-certification.md. Stale artifact. | usecase/business-license.md | Low (cleanup item) |
| 6 | **isProfileComplete derivation**: Defined as "phone_personal IS NOT NULL AND job IS NOT NULL" in db-schema comments. Not a DB column -- derived in application. Must be consistent across 5.3 response and 5.10 gate. | db-schema.md users section | Medium |

---

## 4. Phase 2 Domain Split Confirmation

| Group | Domains | API Count | Reviewer Focus |
|-------|---------|-----------|---------------|
| **cr-A** | Track(1.x), License(7.x), Tag(2.x), Playlist(3.x), PlayHistory(4.x) | 7+4+4+8+3 = 26 | File I/O, N+1, subscription gates, tag cascade |
| **cr-B** | Subscription(6.x), Whitelist(12.x), DownloadQueue(11.x), Likes(10.x) | 10+4+3+3 = 20 | Payment logic, composite PKs, channel limits |
| **cr-C** | User(5.x), Auth(5.2/5.3/14.1), Inquiry(8.x), Notice(9.x), CompanyCert(13.x), Util(14.x) | 10+7+5+5+7 = 34 | Auth/security, access control, status machines |

> Note: Auth APIs (5.2, 5.3, 14.1) are grouped with cr-C for security review coherence.

---

## 5. Deliverables

| File | Content |
|------|---------|
| This file | User-facing summary |
| `deliverables/agent/WI-20260227-ATS-028-evidence-pack.md` | Full 79-API checklist, 21-table DB checklist, coding standards checklist, business rules, domain split details |
