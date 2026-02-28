# WI-20260227-ATS-031 Summary — cr-C 검토: User·Auth·Inquiry·Notice·CompanyCert·Util

**검토 범위:** ~27개 API (5.x User / Auth / 8.x Inquiry / 9.x Notice / 13.x CompanyCert / 14.x Util)
**최종 판정:** CONDITIONAL PASS — CRITICAL 2건, MAJOR 7건 수정 후 승인

---

## 판정 통계

| 판정 | 건수 |
|------|------|
| CRITICAL | 2 |
| MAJOR | 7 |
| MINOR | 4 |
| 📋 제안 | 3 |
| **합계** | **16** |

---

## CRITICAL (즉시 수정 필수)

### CR-C-001: Question 삭제 cascade 누락 — 런타임 DataIntegrityViolationException
- **API**: 8.7 DELETE /api/questions/{id}
- `QuestionService.java:182-188` — `questionRepository.delete(question)` 호출 전 `answers`, `question_attachments` 자식 레코드 삭제 누락
- DB 스키마(`schema.sql:403,421`)에 `ON DELETE CASCADE` 없음 → 답변/첨부파일 있는 질문 삭제 시 **반드시 런타임 크래시**
- **수정**: `answerRepository.deleteAllByQuestion(question)` + `attachmentRepository.deleteAllByQuestion(question)` 선행 호출

### CR-C-002: AuthService / OAuth2Service 클래스 레벨 `@Transactional` 미흡
- `AuthService.java:24`, `OAuth2Service.java:22` — 클래스 레벨에 `@Transactional`(쓰기 모드) 적용
- 모든 읽기 작업(토큰 검증, refresh 등)이 불필요하게 쓰기 트랜잭션으로 열림
- **수정**: `@Transactional(readOnly = true)` 클래스 레벨로 변경, mutating 메서드만 `@Transactional` override

---

## MAJOR (프론트 전 반드시 수정)

| # | 도메인 | 이슈 | 파일:라인 |
|---|--------|------|---------:|
| CR-C-003 | User | `searchUsers()` JPQL에 `is_deleted=false` 필터 없음 → 탈퇴 회원이 관리자 목록에 노출 | `UserRepository.java:21-27` |
| CR-C-004 | CompanyCert | `RESOURCE_DUPLICATE` → HTTP 400 BAD_REQUEST (명세: 409 Conflict) | `BUSINESS_ERROR.java:22-25` |
| CR-C-005 | CompanyCert | `findByUser()` — REJECTED 후 재신청 시 복수 레코드 존재, 비결정적 반환 | `CompanyCertificationRepository.java:14` |
| CR-C-006 | CompanyCert | `process()` 상태 전환 검증 없음 (APPROVED→PENDING 등 비정상 전환 가능) | `CompanyCertification.java:42-48` |
| CR-C-007 | Inquiry | `updateStatus()` 상태 플로우 검증 없음 (RULE-INQ-002 위반) | `Question.java:44-46` |
| CR-C-008 | Common | `TestController` — `/test`, `/health` 인증 없이 운영 노출 | `TestController.java:1-18` |
| CR-C-009 | Auth | `application.yml:36` — JWT 기본 시크릿 fallback 값 하드코딩 (보안 위험) | `application.yml:36` |

---

## MINOR (권장 수정)

| # | 이슈 | 파일 |
|---|------|------|
| CR-C-010 | 회원가입 시 `phonePersonal` 유니크 체크 누락 | `UserService.java:28-47` |
| CR-C-011 | `QuestionAttachment` — `BaseEntity` 미상속 (기능적 문제 없으나 비일관적) | `QuestionAttachment.java:17` |
| CR-C-012 | `getMyStatus()` — 데이터 없을 때 `null` 반환 (타 엔드포인트와 불일관적) | `CompanyCertificationService.java:77` |
| CR-C-013 | OAuth2 토큰 교환 응답 null 체크 누락 → NPE 가능 | `OAuth2Service.java:117-158` |

---

## 도메인별 한줄 평가

| 도메인 | 결과 | 비고 |
|--------|------|------|
| Auth (JWT, OAuth2) | ⚠️ | CRITICAL #2 (트랜잭션 모드) + JWT 시크릿 fallback |
| User (5.x) | ⚠️ | MAJOR #3 (탈퇴 회원 노출) |
| Inquiry (8.x) | ❌ | CRITICAL #1 (cascade 크래시) + MAJOR #7 (상태 검증) |
| Notice (9.x) | ✅ | 이슈 없음 — 클린 |
| CompanyCert (13.x) | ⚠️ | MAJOR 3건 (#4/#5/#6) |
| Util (14.x) | ✅ | 이슈 없음 — 클린 |
| Common/Exception | ⚠️ | TestController 운영 노출, RESOURCE_DUPLICATE HTTP 상태 |

---

## 전반적 평가

Notice·Util 도메인은 완전 클린. Auth/User/Inquiry/CompanyCert에서 CRITICAL 2건·MAJOR 7건 발견.
가장 위험한 건: Question cascade 삭제 누락(런타임 크래시 확실) + AuthService 트랜잭션 모드(성능·일관성).
코딩 표준(readOnly, DTO 분리, @EntityGraph, LAZY) 전반적 준수, BUSINESS_ERROR HTTP 상태 오류와 TestController 운영 노출은 보안/QA 관점 필수 수정.
