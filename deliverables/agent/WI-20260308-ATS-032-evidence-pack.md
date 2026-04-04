[EVIDENCE PACK]
WI ID: WI-20260308-ATS-032
REQ: REQ-20260308-ATS-010
Agent: docops
Completed: 2026-03-08

---

## 수정 항목별 Evidence

### [FILE 1] docs/ui/atstudio-front-list.md

---

**AC-1: 헤더 버전 표기 갱신**

- File: `docs/ui/atstudio-front-list.md` line 3
- Before: `> API Spec v5 기준 | v4 2026-03-07`
- After: `> API Spec v6 기준 | v4 2026-03-07`
- Rationale: api-spec.md 현재 버전이 v6임. v5 기준 표기는 구버전 참조 오류.

---

**AC-2: §1.5 tracks/{id}/download — 장바구니 섹션**

- File: `docs/ui/atstudio-front-list.md` line 73
- Before: `1.5 GET /api/tracks/{id}/download`
- After: `1.5 GET /api/tracks/{trackId}/download`
- API Spec ref: §1.5 `GET /api/tracks/{trackId}/download`

---

**AC-3: §3.3 playlists/{id} — C-1 화면**

- File: `docs/ui/atstudio-front-list.md` line 51
- Before: `3.3 GET /api/playlists/{id}`
- After: `3.3 GET /api/playlists/{playlistId}`
- API Spec ref: §3.3 `GET /api/playlists/{playlistId}`

---

**AC-4: §3.5, §3.8 playlists/{id} — Screen 9 화면**

- File: `docs/ui/atstudio-front-list.md` line 53
- Before: `3.5 PUT /api/playlists/{id}` ... `3.8 DELETE /api/playlists/{id}`
- After: `3.5 PUT /api/playlists/{playlistId}` ... `3.8 DELETE /api/playlists/{playlistId}`
- API Spec ref: §3.5 `PUT /api/playlists/{playlistId}`, §3.8 `DELETE /api/playlists/{playlistId}`

---

**AC-5: §7.4 users/{id}/licenses/{id} — K-3 화면**

- File: `docs/ui/atstudio-front-list.md` line 132
- Before: `7.2 GET /api/users/{id}/licenses` `7.4 GET /api/users/{id}/licenses/{id}`
- After: `7.2 GET /api/users/{userId}/licenses` `7.4 GET /api/users/{userId}/licenses/{licenseId}`
- API Spec ref: §7.2 `GET /api/users/{userId}/licenses`, §7.4 `GET /api/users/{userId}/licenses/{licenseId}`

---

### [FILE 2] docs/ui/modal-list.md

---

**AC-6: M-05 — playlists/{id}/tracks**

- File: `docs/ui/modal-list.md` Section 1 테이블 M-05행
- Before: `3.4 POST /api/playlists/{id}/tracks`
- After: `3.4 POST /api/playlists/{playlistId}/tracks`
- API Spec ref: §3.4 `POST /api/playlists/{playlistId}/tracks`

---

**AC-7: M-07 — playlists/{id}**

- File: `docs/ui/modal-list.md` Section 1 테이블 M-07행
- Before: `3.8 DELETE /api/playlists/{id}`
- After: `3.8 DELETE /api/playlists/{playlistId}`
- API Spec ref: §3.8 `DELETE /api/playlists/{playlistId}`

---

**AC-8: M-11 — tracks/{id}**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-11행
- Before: `1.7 DELETE /api/tracks/{id}`
- After: `1.7 DELETE /api/tracks/{trackId}`
- API Spec ref: §1.7 `DELETE /api/tracks/{trackId}`

---

**AC-9: M-12 — playlists/{id}/tracks**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-12행
- Before: `3.4 POST /api/playlists/{id}/tracks`
- After: `3.4 POST /api/playlists/{playlistId}/tracks`
- API Spec ref: §3.4 `POST /api/playlists/{playlistId}/tracks`

---

**AC-10: M-13 — playlists/{id}**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-13행
- Before: `3.8 DELETE /api/playlists/{id}`
- After: `3.8 DELETE /api/playlists/{playlistId}`
- API Spec ref: §3.8 `DELETE /api/playlists/{playlistId}`

---

**AC-11: M-18 — questions/{id}/status**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-18행
- Before: `8.6 PUT /api/questions/{id}/status`
- After: `8.6 PUT /api/questions/{questionId}/status`
- API Spec ref: §8.6 `PUT /api/questions/{questionId}/status`

---

**AC-12: M-21 — whitelist-channels/{id}**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-21행
- Before: `12.4 DELETE /api/whitelist-channels/{id}`
- After: `12.4 DELETE /api/whitelist-channels/{channelId}`
- API Spec ref: §12.4 `DELETE /api/whitelist-channels/{channelId}`

---

**AC-13: M-24 — user-subscriptions/{id}**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-24행
- Before: `6.9 DELETE /api/user-subscriptions/{id}`
- After: `6.9 DELETE /api/user-subscriptions/{userSubscriptionId}`
- API Spec ref: §6.9 `DELETE /api/user-subscriptions/{userSubscriptionId}`

---

**AC-14: M-25 — users/{id}**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-25행
- Before: `5.8 PUT /api/users/{id}`
- After: `5.8 PUT /api/users/{userId}`
- API Spec ref: §5.8 `PUT /api/users/{userId}`

---

**AC-15: M-28 — tags/{id}**

- File: `docs/ui/modal-list.md` Section 2 테이블 M-28행
- Before: `2.4 DELETE /api/tags/{id}`
- After: `2.4 DELETE /api/tags/{tagId}`
- API Spec ref: §2.4 `DELETE /api/tags/{tagId}`

---

**AC-16: Flow 1 다이어그램 — playlists/{id}**

- File: `docs/ui/modal-list.md` Section 3 Flow 1 code block
- Before: `3.8 DELETE /api/playlists/{id}`
- After: `3.8 DELETE /api/playlists/{playlistId}`
- Rationale: 테이블과 다이어그램 내 표기 일관성 유지.

---

**AC-17: Flow 3 다이어그램 — playlists/{id}/tracks**

- File: `docs/ui/modal-list.md` Section 3 Flow 3 code block
- Before: `3.4 POST /api/playlists/{id}/tracks`
- After: `3.4 POST /api/playlists/{playlistId}/tracks`
- Rationale: 테이블과 다이어그램 내 표기 일관성 유지.

---

### [FILE 3] docs/ui/screen-flow.md

---

**AC-18: Section 4 재생목록 생성 플로우 레이블 추가**

- File: `docs/ui/screen-flow.md` Section 4 (line 98)
- Before: `[재생목록 생성]`
- After: `[8 재생목록 생성]`
- Rationale: atstudio-front-list.md에서 Screen 8 = "재생목록 생성"으로 명시되어 있음. Screen ID 없이 암묵적 참조는 에이전트 인젝션 시 화면 식별 실패 위험. front-list 기준 명칭으로 명시화.

---

**AC-19: K-5 화면명 통일**

- File: `docs/ui/screen-flow.md` Section 10 (line 282)
- Before: `[K-5 기업인증 심사]`
- After: `[K-5 기업 인증 목록 / 심사 처리]`
- Rationale: atstudio-front-list.md K-5 화면명 = "기업 인증 목록 / 심사 처리". screen-flow.md가 "기업인증 심사"로 단축 표기하여 불일치 발생. front-list 기준 명칭이 1차 소스이므로 동기화.

---

**AC-20: Section 6 tracks/{id}/download**

- File: `docs/ui/screen-flow.md` Section 6 (line 157)
- Before: `1.5 GET /api/tracks/{id}/download`
- After: `1.5 GET /api/tracks/{trackId}/download`
- API Spec ref: §1.5 `GET /api/tracks/{trackId}/download`
- Rationale: 핸드오프 패킷 참조 지시 (screen-flow.md 내 API 경로 표기 동일 기준 적용).

---

## 검증 사항

- api-spec.md (수정 금지): path parameter명 확인용으로만 참조. 파일 변경 없음.
- `{id}` 잔존 여부: 3개 파일 모두 `{id}` 단축 표기 0건 (grep 확인 완료).
- front-list.md 버전 표기: "API Spec v6 기준"으로 갱신 완료.
- K-5 레이블: screen-flow.md 내 1개소 전체 치환 완료.

## 관련 파일

- `docs/ui/atstudio-front-list.md` (수정)
- `docs/ui/modal-list.md` (수정)
- `docs/ui/screen-flow.md` (수정)
- `docs/design/api-spec.md` (참조만, 수정 없음)
