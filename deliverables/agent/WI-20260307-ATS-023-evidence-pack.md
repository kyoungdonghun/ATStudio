[EVIDENCE PACK]
WI ID: WI-20260307-ATS-023
REQ: REQ-20260307-ATS-009
Agent: cr
Completed: 2026-03-08

---

## Verification Matrix

| api-spec | UC ID | URL | Method | Auth | Status | Fields |
|----------|-------|-----|--------|------|--------|--------|
| 1.1 | SOUND-001 | OK | OK | OK | OK | OK |
| 1.2 | SOUND-005 | OK | OK | OK | OK | OK |
| 1.3 | SOUND-006 | OK | OK | OK | OK | OK |
| 1.4 | SOUND-010 | OK | OK | OK | OK | OK |
| 1.5 | SOUND-011 | OK | OK | OK | OK | OK |
| 1.6 | SOUND-012 | OK | OK | OK | OK | OK |
| 1.7 | SOUND-016 | OK | OK | OK | OK | OK |
| 1.8 | **MISSING UC** | -- | -- | -- | -- | -- |
| 2.1 | SOUND-003 | OK | OK | OK | OK | OK |
| 2.2 | (Tag List sub-UC) | OK | OK | OK | OK | OK |
| 2.3 | SOUND-014 | OK | OK | OK | OK | OK |
| 2.4 | SOUND-018 | OK | OK | OK | OK | OK |
| 3.1 | SOUND-002 | OK | OK | OK | OK | OK |
| 3.2 | SOUND-007 | OK | OK | OK | OK | OK |
| 3.3 | SOUND-008 | OK | OK | OK | OK | OK |
| 3.4 | SOUND-019 | OK | OK | OK | OK | OK |
| 3.5 | SOUND-013 A | OK | OK | OK | OK | OK |
| 3.6 | SOUND-013 B | OK | OK | OK | OK | OK |
| 3.7 | SOUND-020 | OK | OK | OK | OK | OK |
| 3.8 | SOUND-017 | OK | OK | OK | OK | OK |
| 4.1 | SOUND-004 | OK | OK | OK | OK | OK |
| 4.2 | SOUND-009 | OK | OK | OK | OK | OK |
| 4.3 | SOUND-015 | OK | OK | OK | OK | OK |
| 10.1 | LIKE-001 | OK | OK | OK | partial | OK |
| 10.2 | LIKE-002 | OK | OK | OK | OK | OK |
| 10.3 | LIKE-003 | OK | OK | OK | partial | OK |
| 11.1 | DLQ-001 | OK | OK | OK | partial | OK |
| 11.2 | DLQ-002 | OK | OK | OK | OK | OK |
| 11.3 | DLQ-003 | OK | OK | OK | partial | OK |

(partial = 성공 상태코드 일치, 에러 응답 미문서화)

---

## Detailed Issues

### MAJOR-001: api-spec §1.8 대응 UC 없음
- api-spec: `docs/design/api-spec.md:291~331` (§1.8 List All Tracks Admin)
- usecase: `docs/design/usecase/sound-track.md` — 전체 검토 결과 admin 트랙 목록 UC 없음
- index: `docs/design/usecase/index.md:14` — sound-track.md = 7 UCs (SOUND-001/005/006/010/011/012/016/019)
- 수정 제안: sound-track.md에 SOUND-021 (Admin Track List) UC 추가, index.md 카운트 갱신

### MINOR-001: §10.1 409 누락
- api-spec: `docs/design/api-spec.md:1297~1303` — 201 Created만 문서화, 409 없음
- usecase: `docs/design/usecase/likes.md:27` — LIKE-001 "Track already in likes: 409 Conflict"
- 수정 제안: api-spec §10.1에 `409 Conflict` 에러 케이스 추가

### MINOR-002: §10.3 404 누락
- api-spec: `docs/design/api-spec.md:1327~1333` — 204 No Content만, 404 없음
- usecase: `docs/design/usecase/likes.md:74` — LIKE-003 "Track not in likes: 404"
- 수정 제안: api-spec §10.3에 `404 Not Found` 에러 케이스 추가

### MINOR-003: §11.1 409 누락
- api-spec: `docs/design/api-spec.md:1342~1348` — 201 Created만, 409 없음
- usecase: `docs/design/usecase/download-queue.md:29` — DLQ-001 "Track already in queue: 409 Conflict"
- 수정 제안: api-spec §11.1에 `409 Conflict` 에러 케이스 추가

### MINOR-004: §11.3 404 누락
- api-spec: `docs/design/api-spec.md:1372~1378` — 204 No Content만, 404 없음
- usecase: `docs/design/usecase/download-queue.md:79` — DLQ-003 "Track not in queue: 404"
- 수정 제안: api-spec §11.3에 `404 Not Found` 에러 케이스 추가
