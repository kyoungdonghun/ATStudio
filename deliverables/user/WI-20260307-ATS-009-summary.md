[WI SUMMARY — User-Facing]
WI ID: WI-20260307-ATS-009
REQ: REQ-20260307-ATS-008 Phase 1 Batch3
Domain: Album + Playlist
Date: 2026-03-07
Author: docops

---

## 검증 결과 요약

| 심각도 | 건수 |
|--------|------|
| CRITICAL | 0 |
| MAJOR | 4 |
| MINOR | 8 |
| SUGGESTION | 2 |
| **합계** | **14** |

이상 없음 항목: API-Spec § 비교 기준, URL/Method/Auth 전반 일치 확인됨 (상세는 evidence-pack 참조).

---

## 주요 이슈 (MAJOR 4건)

### MAJOR-001 — Album: 앨범 목록(L-1/L-2)에서 삭제 흐름 누락 (screen-flow vs modal-list 충돌)

- modal-list M-14: 발생 화면을 `L-1/L-2 (앨범 목록)`로 명시
- screen-flow §5: L-1/L-2에서 앨범 삭제 버튼 흐름이 없음 (L-5 수정 화면에서만 정의)
- 영향: 프론트 구현 시 앨범 목록에 삭제 버튼을 넣을지 여부가 불명확

### MAJOR-002 — Album: 트랙 순서 변경 요청 형식 불일치 (api-spec vs usecase)

- api-spec §15.8 Request Body: `{ "trackOrders": [{ "trackId", "order" }] }`
- usecase ALBUM-008: `Frontend sends [{trackId, order}, ...]` — 래퍼 객체(`trackOrders`) 없이 배열 직접 전송
- 영향: 프론트엔드 구현 시 잘못된 요청 형식으로 빌드할 수 있음

### MAJOR-003 — Playlist: Playlist 3개 제한 오류 케이스가 api-spec §3.1에 미기재

- api-spec §3.1 Error Cases 섹션 없음
- modal-list Playlist 3-Item Limit Handling: PLAYLIST_LIMIT_EXCEEDED 409 명시
- 실제 코드(PlaylistService.java:46-48)에는 구현 완료
- 영향: API spec만 참조하는 프론트 개발자가 409 처리 코드 누락 가능

### MAJOR-004 — Playlist: SOUND-002 유스케이스에 3개 제한 예외 흐름 없음

- usecase SOUND-002 (Create Playlist) Exception/Alternative Flow: 비어있음(`-`)
- 핵심 비즈니스 규칙(최대 3개 제한)이 정식 유스케이스에 미정의
- 영향: 유스케이스 기반 테스트 시나리오 작성 시 누락

---

## 검증 완료 항목 (이상 없음)

- api-spec §3 URL/Method/인증 vs front-list Screen 4/5/8/9/C-1: 일치
- modal-list M-05/M-07/M-12/M-13 API 참조: api-spec §3과 일치
- modal-list M-06/M-08/M-14 API 참조: api-spec §15와 일치
- screen-flow §4 재생목록 흐름 API 번호: api-spec §3과 일치
- screen-flow §5 앨범 흐름 API 번호: api-spec §15와 일치
- Playlist 3-Item Limit — modal-list vs screen-flow 간 일치 (버튼 비노출 정책 동일)
- Album CRUD ALBUM-001~008 유스케이스 vs api-spec §15: 흐름 전반 일치 (MAJOR-002 제외)
- Album soft delete(is_active=false) 정책: usecase ALBUM-005와 정합

---

## 다음 단계

이 보고서는 REQ-20260307-ATS-008 Phase 2 (MA 취합 → 사용자 컨펌)에 입력됩니다.
상세 발견 목록 및 파일:섹션 포인터는 `deliverables/agent/WI-20260307-ATS-009-evidence-pack.md` 참조.
