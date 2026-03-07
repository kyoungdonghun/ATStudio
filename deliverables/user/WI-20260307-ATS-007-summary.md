[WI SUMMARY — User-Facing]
WI ID: WI-20260307-ATS-007
REQ: REQ-20260307-ATS-008
Date: 2026-03-07
Prepared by: docops

---

## 검증 범위

Batch 1: Auth/User + PlayHistory + Likes + License + Whitelist 도메인

검증 대상 문서 (10종):
- api-spec.md §5 (Users/Auth), §4 (PlayHistory), §10 (Likes), §7 (Licenses), §12 (Whitelist)
- usecase: user-info.md, sound-playhistory.md, likes.md, user-license.md, whitelist.md
- front-list: A-1~A-4, Screen 10, D-1, E-1, F-1, F-2, H-1
- modal-list: M-01, M-02, M-21, M-23
- screen-flow: §2 인증 흐름, §8 개인 페이지 흐름

---

## 발견 건수 요약

| 심각도 | 건수 |
|--------|------|
| CRITICAL | 0 |
| MAJOR | 2 |
| MINOR | 6 |
| SUGGESTION | 1 |
| **합계** | **9** |

---

## 주요 이슈 목록

### MAJOR-001: 비밀번호 변경 응답 코드 불일치 (M-01 vs API 5.11)

- 위치: modal-list.md (Section 3, Flow 4) vs api-spec.md §5.11
- 내용: modal-list.md Flow 4 에서 비밀번호 변경 성공 시 `200 OK` 로 표기되어 있으나, API 5.11은 `204 No Content` 를 반환한다.
- 영향: 프론트엔드 구현 시 응답 코드 기준으로 성공 분기를 처리하면 토스트 미표시 오류 발생 가능.
- 권고: modal-list.md Flow 4의 `200 OK` 를 `204 No Content` 로 수정 필요.

### MAJOR-002: 로그아웃 API 미정의 (screen-flow §2 vs api-spec §5)

- 위치: screen-flow.md §2 vs api-spec.md §5 전체
- 내용: screen-flow §2에 "로그아웃 → [1 메인] (비로그인 상태)" 가 정의되어 있으나, api-spec §5에 로그아웃 엔드포인트(서버측 토큰 무효화)가 없다.
- 영향: 서버 측 토큰 무효화 방식이 확정되지 않으면 Refresh Token 재사용 보안 위험이 있다.
- 권고: 로그아웃을 "프론트 전용(토큰 폐기, 서버 무효화 없음)" 으로 명시적으로 결정하거나, `POST /api/auth/logout` API를 api-spec에 추가하는 것 중 하나를 결정하여 문서화 필요.

---

## MINOR 이슈 요약 (6건)

| # | 도메인 | 내용 |
|---|--------|------|
| MINOR-001 | PlayHistory | screen-flow §8 E-1 "전체 삭제 → confirm()" vs UC SOUND-015 "Delete All 버튼 클릭(confirm 미언급)" — 인터랙션 불일치 |
| MINOR-002 | Auth | front-list A-2 (일반 회원가입) 관련 API에 중복 확인 유틸 API(14.2, 14.3, 14.7)가 미기재 |
| MINOR-003 | Likes | UC LIKE-003에서 D-1 목록 삭제(M-23 모달)와 트랙 목록/상세에서의 토글 삭제 경로를 구분하지 않음 |
| MINOR-004 | License | front-list F-2 에서 사용되는 7.3 API의 UC 코드(INFO-011) 교차 참조 미기재 |
| MINOR-005 | Whitelist | front-list H-1 / screen-flow §8 에 채널 등록 한도 초과(403 WHITELIST_CHANNEL_LIMIT_EXCEEDED) 에러 처리 흐름 미정의 |
| MINOR-006 | Auth (api-spec) | api-spec §5 엔드포인트 번호 비순차 (5.1, 5.2, 5.3, 5.10, 5.4, ...) — 참조 혼란 가능 |

---

## SUGGESTION

| # | 내용 |
|---|------|
| SGT-001 | UC INFO-008 (Login)에 토큰 저장 방식 (Access Token: 메모리, Refresh Token: httpOnly cookie) 이 명시되어 있으나, screen-flow §2 A-1에는 해당 내용이 없음. screen-flow에 짧게 주석으로 참조 추가 고려. |

---

## 검증 완료 항목 (이상 없음)

| 항목 | 결과 |
|------|------|
| API 4.1/4.2/4.3 vs UC SOUND-004/009/015 응답 코드 일치 | 이상 없음 |
| API 5.3 isProfileComplete 필드 vs UC INFO-013 vs front-list A-3/A-4 vs screen-flow §2 | 이상 없음 |
| API 5.9 (회원탈퇴) vs UC INFO-007 vs M-02 InputModal vs screen-flow §8 | 이상 없음 |
| API 10.1/10.2/10.3 vs UC LIKE-001/002/003 응답 코드 일치 | 이상 없음 |
| API 7.1/7.3 vs UC INFO-009/INFO-011 vs front-list F-1/F-2 | 이상 없음 |
| API 7.2/7.4 (Admin) vs UC INFO-010/012 vs front-list K-3 | 이상 없음 |
| API 12.1 Auth (subscribers only) vs UC WL-001 Preconditions | 이상 없음 |
| API 12.4 vs UC WL-004 vs M-21 ConfirmModal vs screen-flow §8 H-1 | 이상 없음 |
| screen-flow §8 M-23 (좋아요 취소) vs modal-list M-23 | 이상 없음 |
| front-list D-1 (10.1~10.3) vs api-spec §10 | 이상 없음 |
| front-list Screen 10 API 목록 (5.4/5.7/5.9/5.11) vs api-spec §5 | 이상 없음 |

---

## 다음 단계

이 WI는 Phase 1 Batch1 발견 보고 완료 (수정 금지).
Phase 2: MA가 전체 배치 취합 후 사용자 컨펌 진행.
Phase 3: 컨펌 확정 후 docops가 문서 수정 WI 수행.
