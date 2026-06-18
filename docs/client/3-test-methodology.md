---
version: 1.0
last_updated: 2026-04-17
project: ATS
owner: qa
category: guide
status: stable
dependencies: []
---

# 3. 테스트 방법론

### 3.1 블랙박스 테스트 (Black-box Testing)
내부 코드를 모르는 상태에서 **입력과 출력만 확인**하는 방법.
- "회원가입 버튼을 누르면 정상적으로 가입되는가?"
- "잘못된 이메일을 넣으면 에러가 뜨는가?"
- 테스터가 주로 수행하는 방식

### 3.2 경계값 분석 (Boundary Value Analysis)
**허용 범위의 경계**에서 테스트하는 방법.

| 필드 | 최소 경계 | 최대 경계 | 초과 |
|------|----------|----------|------|
| 닉네임 | 2자 (OK) | 20자 (OK) | 21자 (거부) |
| 비밀번호 | 8자 (OK) | 100자 (OK) | 7자 (거부) |
| BPM | 1 (OK) | 999 (OK) | 0 (거부), 1000 (거부) |
| 재생목록 | 1개 (OK) | 3개 (OK) | 4번째 생성 버튼 비노출 |

### 3.3 동등 분할 (Equivalence Partitioning)
입력값을 **유효/무효 그룹**으로 나누어 각 그룹에서 대표값 하나만 테스트.

예) 이메일 입력:
- 유효: `user@example.com` → 성공
- 무효(형식): `userexample.com` → 에러
- 무효(빈 값): `` → 에러

### 3.4 상태 전이 테스트 (State Transition Testing)
**상태가 변하는 기능**에서 전이가 올바른지 확인.

예) 구독 상태:
```
없음 → ACTIVE (가입)
ACTIVE → CANCELLED (취소)
CANCELLED → EXPIRED (만료일 도달)
ACTIVE → ACTIVE (업그레이드)
```

예) 기업 인증:
```
없음 → PENDING (신청)
PENDING → APPROVED (승인)
PENDING → REJECTED (반려)
PENDING → REVISION_REQUESTED (수정 요청)
REVISION_REQUESTED → PENDING (보완 서류 재제출)
REJECTED → PENDING (새 신청)
```

### 3.5 시나리오 기반 테스트 (Scenario-based Testing)
실제 사용자 행동 흐름을 따라 **처음부터 끝까지** 테스트.

예) "신규 기업 회원이 음원을 다운로드하기까지":
1. 회원가입 (기업 선택) → 2. 로그인 → 3. 기업 인증 신청 → 4. (관리자 승인) → 5. 기업용 플랜 가입 → 6. 음원 검색 → 7. 다운로드 → 8. 라이선스 확인

### 3.6 탐색적 테스트 (Exploratory Testing)
**정해진 시나리오 없이** 자유롭게 사이트를 사용하면서 문제를 발견.
- "이상한 동작은 없는가?"
- "직관적이지 않은 부분은 없는가?"
- "모바일에서도 잘 보이는가?"

---
