[WI EVIDENCE PACK — Agent-Facing]
WI ID: WI-20260308-ATS-033
REQ: REQ-20260308-ATS-011
Status: Completed
Date: 2026-03-08
Agent: docops

---

## 1. Change Rationale

TagController의 `GET /api/tags`는 `ResponseDTO` 래퍼 없이 `List<TagResponse>`를 직접 반환한다.
이는 api-spec §2.2에 raw array(`[ {...}, {...} ]`)로 명세된 의도된 설계이다.
그러나 개발 표준(§2A.4)에 Controller 템플릿은 `ResponseDTO<T>` 래퍼를 사용하는 예시만 있었으므로,
코드 리뷰 시 "표준 위반"으로 오인될 수 있는 상태였다.

이 WI는 예외 규칙을 문서화하여 해당 패턴이 의도된 예외임을 명시한다.

---

## 2. Files Modified

| File | Change | Lines |
|------|--------|-------|
| `docs/standards/development-standards.md` | §2A.4 Controller 템플릿 코드 블록 직후에 "Lookup Data Exception" 섹션 추가 | 172~185 |
| `docs/standards/development-standards.md` | 메타데이터 version 2.0 → 2.1, last_updated 갱신 | 2~3 |

---

## 3. Diff Summary

**Before (line 172 영역):**
```
```java (Controller template 코드 블록 끝)
```

**Service (Business Logic):**
```java
```

**After (line 172~186 영역):**
```
#### Lookup Data Exception

Endpoints that serve **lookup (reference) data** may return a raw JSON array directly — without a `ResponseDTO` wrapper — when **both** of the following conditions hold:

1. The endpoint is specified as a raw array response in `docs/design/api-spec.md`.
2. The data is read-only, stateless reference data (e.g., filter options, tag lists) with no pagination.

**Current applicable endpoint:**

| Endpoint | api-spec Reference | Return Type |
|----------|--------------------|-------------|
| `GET /api/tags` | §2.2 | `ResponseEntity<List<TagResponse>>` |

> **Note:** Any new endpoint claiming this exception must be explicitly named in `api-spec.md` as a raw array response before implementation. Do not apply this exception speculatively.

**Service (Business Logic):**
```java
```

---

## 4. Verification Pointers

| Check | Result | Pointer |
|-------|--------|---------|
| 추가 위치 | §2A.4 Controller 템플릿 직후 | `docs/standards/development-standards.md` line 172 |
| api-spec §2.2 raw array 확인 | `[ { "id":1, ... }, { "id":2, ... } ]` 형식 | `docs/design/api-spec.md` line 375~379 |
| TagController 코드 일치 | `ResponseEntity<List<TagResponse>>` 반환 확인 | `src/main/java/com/atstudio/atstudio/controller/TagController.java` line 37~39 |
| 기존 내용 보존 | 삭제/변경 없음, 추가만 | 전체 diff 확인 |
| 문서 버전 | 2.1 (Minor: 내용 추가) | `docs/standards/development-standards.md` line 2 |

---

## 5. Standards Compliance

| Standard | Check |
|----------|-------|
| documentation-standards.md §5.1 버전 규칙 | Minor 변경(내용 추가) → 0.x 증가 → 2.0 → 2.1 (준수) |
| documentation-standards.md §1.3 최대 3레벨 depth | #### (4번째 `#`) = h4 = 3레벨 이내 (준수) |
| core-principles.md Language Policy | 문서 섹션 영문 작성 (준수) |
| documentation-standards.md §2.2 Format Rules | 이모지 없음, 표 마크다운 형식 (준수) |

---

## 6. Acceptance Criteria Verification

| Criterion | Status |
|-----------|--------|
| §2A.4 Controller 섹션 근처에 예외 규칙 추가 | ✅ Controller 템플릿 코드 블록 직후 |
| "Lookup Data Exception" 제목 포함 | ✅ line 172 |
| 조건: api-spec raw array 명세 조건 | ✅ 두 가지 조건 명시 |
| 현재 적용 엔드포인트: GET /api/tags (api-spec §2.2) | ✅ 표에 명시 |
| 기존 문서 포맷/스타일 일치 | ✅ #### 서브헤더 + 표 + blockquote 스타일 기존 문서와 일치 |
| 기존 내용 삭제/변경 없음 | ✅ 추가만 수행 |

---

## 7. Input Documents Referenced

| Document | Purpose |
|----------|---------|
| `docs/standards/core-principles.md` | STD-001 헌법 — Language Policy, Tier 0 확인 |
| `docs/standards/documentation-standards.md` | STD-004 — 버전 규칙, 포맷 규칙, depth 규칙 |
| `docs/standards/glossary.md` | STD-005 — 용어 기준 확인 |
| `deliverables/user/REQ-20260308-ATS-011.md` | 승인된 REQ — Scope, 예외 규칙 추가 지시 |
| `docs/design/api-spec.md` §2.2 | raw array 명세 확인 (수정 금지 참조만) |
| `src/.../controller/TagController.java` | 현재 구현 패턴 확인 (수정 금지 참조만) |

---

## 8. Follow-up Items

| Item | Priority | Note |
|------|----------|------|
| WI-034 (Pending-B) 코드 리팩토링 | HIGH | REQ-011 병렬 Phase 1 — se 담당, 독립 실행 가능 |
