# Next Project — Kick List

> ATStudio 프로젝트에서 실제로 겪었던 것들. 다음 프로젝트 착수 시 체크.
> 상세 근거: `docs/archive/retrospective/`

---

## 프로젝트 시작 전 반드시 확정할 것

- [ ] **도메인 어휘 선 확정** — 유사 개념(e.g. Playlist vs Album)을 REQ-001에서 명확히 구분. 나중에 고치면 여러 세션 낭비.
- [ ] **API 응답 envelope 패턴 통일** — `{ "dataList": [...] }` vs raw array 둘 중 하나. 중간에 바꾸면 15개 API 수정.
- [ ] **HTTP 상태코드 정의표 작성** — 특히 중복=409, 삭제=204, 생성=201. 안 하면 중간에 400→409 retrofit 발생.
- [ ] **Soft delete 전략 결정** — 어떤 엔티티가 soft / hard delete인지 미리 정의. is_deleted 있는 모든 쿼리에 필터 필수.

---

## Spring Boot / JPA 체크리스트

- [ ] **SecurityConfig 규칙 순서** — `/api/users/me` 같은 specific path를 `/api/users/*` wildcard보다 **앞에** 선언. 순서 반대면 정상 유저 차단됨. MockMvc 테스트로 안 잡힘.
- [ ] **@Transactional 기본값** — 클래스 레벨 `readOnly=true`, mutating 메서드만 `@Transactional` 오버라이드.
- [ ] **N+1 방지** — List/Page 쿼리에 LAZY 연관 있으면 `@EntityGraph` 필수. 단, Pageable과 함께 쓰면 `HHH90003004` 경고 (in-memory pagination) → 레코드 많아지면 two-query 패턴으로 전환.
- [ ] **복합 PK 엔티티** — `save()` = `merge()`. 중복 방지 필요하면 `existsById()` 직접 체크.
- [ ] **JPA Criteria join** — `root.join("fieldName")`의 fieldName은 컬럼명이 아니라 Entity 필드명. 오타는 런타임 오류. Mockito에서 잡히지 않음.
- [ ] **enum valueOf()** — try-catch 감싸기. 안 하면 잘못된 입력에 500 반환.
- [ ] **GlobalExceptionHandler** — `AccessDeniedException`은 `Exception` catch-all에 잡힘 → 명시적 분기 or 별도 핸들러 필요.
- [ ] **cascade delete 순서** — 부모 삭제 시 자식 순서 명시 (예: Attachment → Answer → Question). DB cascade 설정 없으면 service에서 명시적 처리.
- [ ] **is_deleted 필터 누락** — soft delete 쓰는 엔티티는 `findByEmail` 같은 쿼리에 `AndIsDeletedFalse` 필수. 빠지면 탈퇴 유저 PII 노출.

---

## 테스트 체크리스트

- [ ] **`@WithMockUser` 한계** — `SecurityFilterChain` 우회함. Security 규칙 순서 버그는 `@SpringBootTest + @AutoConfigureMockMvc`로만 잡힘.
- [ ] **CustomUserDetails + `@WithMockUser`** — `@AuthenticationPrincipal CustomUserDetails`에 null 주입됨 → service mock 필수.
- [ ] **Spring Boot 4.x 패키지** — `AutoConfigureMockMvc`: `org.springframework.boot.webmvc.test.autoconfigure` (3.x 패키지 다름).
- [ ] **Status machine 전이 테스트** — 유효하지 않은 전이 모두 명시적으로 테스트. e.g. APPROVED→approve() = throw.
- [ ] **무제한 플랜(-1) 체크** — `downloadPerDay == -1` guard 빠지면 limit 체크가 반전됨.

---

## 멀티에이전트 프로세스 체크리스트

- [ ] **WI 체인 규칙** — WI 완료 직후, REQ PARALLEL WORK PLAN에서 unblock된 WI 즉시 트리거. deliverable 쓰다가 잊기 쉬움.
- [ ] **se / re 역할 분리** — se = 구현 + 테스트 작성. re = 실행 + 검증 보고. re가 테스트 새로 쓰면 중복.
- [ ] **cr 실행 타이밍** — re PASS 이후에 cr. cr이 먼저 돌면 테스트 실패 후 코드 바뀌는 케이스에 리뷰 낭비.
- [ ] **re/cr sandbox 제한** — 환경에 따라 Write 권한 없음. deliverable 파일(evidence pack 등) MA가 직접 Write 도구로 작성.
- [ ] **context injection 순서** — Tier 0 (standards) → Tier 1 (policies) → Tier 2 (task context). 프롬프트 캐싱 효율을 위해 순서 고정.

---

## 보안 / 파일 저장 체크리스트

- [ ] **Path Traversal 방어** — 파일 업로드/삭제/조회 서비스에서 `Paths.get(filename).getFileName()` sanitize + `resolvedPath.startsWith(basePath)` 검증 필수. `../` 공격으로 시스템 파일 읽기/삭제 가능.
- [ ] **basePath 절대경로 변환** — `@PostConstruct`에서 `Paths.get(basePath).toAbsolutePath().normalize()` 한 번만 계산. 상대경로 그대로 쓰면 CWD 변경 시 경로 탈출.
- [ ] **정적 리소스 핸들러 경로** — WebConfig `addResourceLocations("file:...")` 도 절대경로 변환 필수.

---

## Seed Data / 테스트 데이터 체크리스트

- [ ] **INSERT IGNORE + auto_increment 함정** — `INSERT IGNORE`로 스킵된 행도 MySQL auto_increment 카운터가 증가함. FK 참조하는 seed data가 있다면 **명시적 ID 지정** 필수 (`INSERT INTO tags (id, name, type)`). 안 하면 ID가 매번 달라져서 FK 매핑이 깨짐.
- [ ] **raw SQL INSERT와 BaseEntity 날짜** — `@CreatedDate`/`@LastModifiedDate`는 JPA persist() 경유 시에만 작동. seed.sql의 raw INSERT는 `created_at`에 NULL/zero 넣음 → JDBC URL에 `zeroDateTimeBehavior=convertToNull` 추가 + seed 끝에 `UPDATE ... SET created_at = ... WHERE created_at IS NULL` 보정.
- [ ] **seed data 양적 테스트** — 더미 데이터를 "현실적 개수"로 넣어서 레이아웃 스트레스 테스트. 태그 3개일 때 안 깨져도 68개일 때 깨질 수 있다. 극단값 최소 1건은 넣어둘 것.

---

## 필터 UI 설계 체크리스트

- [ ] **인라인 필터 + 모달 검색 조합** — 선택지가 10개 이상이면 인라인 나열은 레이아웃 파괴. 행별 `overflow: hidden`으로 width 안에 들어가는 만큼만 보여주고, 나머지는 모달(텍스트 검색)으로 접근. 활성 태그를 배열 앞으로 sort해서 overflow로 잘려도 보이게 할 것.
- [ ] **필터바 3행 구조** — 카테고리별 독립 행으로 분리하면 각 행이 독립적으로 overflow 처리 가능. 한 행에 모든 카테고리를 넣으면 앞쪽 카테고리가 공간을 독점.
- [ ] **find() vs filter() 주의** — 태그 표시할 때 `tags.find(t => t.type === 'GENRE')`는 첫 번째 1개만 반환. 여러 개 표시하려면 `filter()` 사용. 코드 리뷰 시 `.find()`가 의도적 단일 선택인지 실수인지 확인.

---

## 파일 다운로드 / 첨부파일 체크리스트

- [ ] **`<a href download>`는 JWT 미포함** — 인증 필요한 파일 다운로드에 `<a>` 태그 직접 사용 불가. Axios blob 요청 (`responseType: 'blob'`) + `URL.createObjectURL` + 프로그래밍 방식 다운로드 필수. 이 패턴을 모든 첨부파일 다운로드에 통일.
- [ ] **record DTO에 MultipartFile 추가 시** — Java record는 `@ModelAttribute` 바인딩이 제한적. `List<MultipartFile>` 필드 추가하려면 record → class(Lombok `@Getter`/`@Setter`) 전환 필요. 이때 호출부 `.title()` → `.getTitle()` 일괄 변경 필수.
- [ ] **공지사항 첨부 = public, 문의 첨부 = auth** — 같은 첨부파일 패턴이라도 접근 권한이 다를 수 있음. 엔드포인트별 `@AuthenticationPrincipal` 유무 확인.

---

## 멀티 파일 업로드 체크리스트

- [ ] **순차 업로드 + 개별 에러 처리** — 전체 batch API 없이 기존 단일 API를 재사용할 때, 한 건 실패가 전체를 중단하지 않도록 설계. 성공/실패 상태를 각 항목에 표시하고, 실패건만 재시도 가능하게.
- [ ] **파일 수 제한** — `<input multiple>`은 브라우저가 제한을 안 걸음. 프론트에서 명시적 상한(예: 20곡) 체크 필수. 초과분은 잘라내고 사용자에게 알림.

---

## 프론트엔드 상태관리 체크리스트

- [ ] **Zustand Set/Map 반응성** — `Set.delete()`, `Map.set()` 직접 호출은 리렌더 트리거 안 됨. 반드시 `new Set(prev)` 복사 후 변경 → `setState()`. 배열도 동일 (`[...prev]`).
- [ ] **공유 타입 파일(types/index.ts) drift 방지** — 각 API 모듈이 인라인 타입을 쓰면 공유 타입 파일과 괴리 발생. 정기적으로 백엔드 DTO와 대조하거나, API 모듈에서 공유 타입을 re-export하는 패턴 사용.

---

## 프론트엔드 CSS 아키텍처 체크리스트

- [ ] **CSS specificity 전략을 페이지 구현 전에 수립** — 48개 페이지를 만든 후에야 `.table thead th`(0,1,2)가 `.thRight`(0,1,0)을 오버라이드하는 문제를 발견. base selector의 specificity를 정하고, override 패턴을 확정한 뒤에 페이지를 만들어야 한다. 안 하면 한 곳 고칠 때 전체 테이블이 깨진다.
- [ ] **컴포넌트 경계를 넘는 테이블 반응형 규칙 동기화** — `<thead>`는 Page 컴포넌트, `<tbody><tr>`은 Row 컴포넌트로 분리될 때, 한쪽에서 `display: none`으로 컬럼을 숨기면 다른 쪽도 반드시 동기화. 이 규칙이 없으면 모바일에서 th 4개 / td 2개로 어긋난다.
- [ ] **공통 스타일 수정 시 전체 grep 필수** — base selector(`.table th`, `.cell` 등) 수정 시, 프로젝트 전체에서 같은 패턴을 사용하는 곳을 검색하고 영향 확인. 보고된 곳만 고치면 다른 페이지에서 동일 버그 재발.

---

## 구독 모델 설계 (참고)

> 이번 프로젝트에서 확정한 방식. 재사용 가능.

| 액션 | 타이밍 | 청구 |
|------|--------|------|
| 업그레이드 | 즉시 적용 | `(newDailyRate - oldDailyRate) × 남은 일수` |
| 다운그레이드 | 다음 결제일부터 | 없음 (현재 기간 유지) |
| 취소 | status=CANCELLED 즉시 | 없음 (expires_at까지 서비스 유지) |

- 다운그레이드 예약: `user_subscriptions`에 `pendingSubscriptionId`, `pendingBillingCycle` 컬럼 필요
- 무제한 플랜: `downloadPerDay = -1` 컨벤션, guard 필수

---

## 전수조사 기법 — 역할×화면 매트릭스 감사

> grep 기반 코드 패턴 스캔만으로는 **플로우 기반 문제**(역할별 UI 분기, 권한 불일치)를 못 잡는다.
> ATStudio에서 ADMIN에게 좋아요/재생목록 노출, GUEST에게 인증 필수 버튼 노출 등을 놓친 후 도입.

### 방법

1. **라우터에서 전체 화면 목록 추출** (48개 등)
2. **역할(GUEST/USER/ADMIN) × 화면 매트릭스** 작성
3. 각 셀에서 검증:
   - 라우트 가드 (ProtectedRoute minRole)가 백엔드 SecurityConfig와 일치하는가?
   - 페이지 내 조건부 렌더링이 역할에 맞는가?
   - API 호출이 해당 역할로 성공하는가?
   - 에러 경로는 적절한가?
4. **그룹별 병렬 에이전트** (Public, Subscriber, Admin 등)로 분산 스캔
5. 불일치 리포트 → 심각도별 정렬 → 수정

### 잡히는 유형 (grep으론 못 잡는 것들)

| 유형 | 예시 |
|------|------|
| 역할별 UI 분기 누락 | ADMIN에게 좋아요/재생목록 노출 |
| GUEST에게 인증 버튼 노출 | 좋아요/다운로드/대기열 버튼 → 401 사일런트 실패 |
| 서비스 레이어 역할 불일치 | DownloadService가 ADMIN 구독 체크 → NO_ACTIVE_SUBSCRIPTION |
| 소유자 체크 누락 | 다른 사용자 문의의 삭제 버튼 노출 |
| 네비게이션 역할 분기 | Header에서 role별 메뉴 미분기 |

### 3-way와의 관계

기존 3-way (spec→code→spec)는 **정합성** 검증. 역할×화면은 **접근 권한** 검증.
둘 다 해야 완전한 전수조사가 된다.
