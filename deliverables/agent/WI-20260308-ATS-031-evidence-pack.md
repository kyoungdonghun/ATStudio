[EVIDENCE PACK]
WI ID: WI-20260308-ATS-031
REQ: REQ-20260308-ATS-010
Agent: docops
Completed: 2026-03-08

---

## 수정 항목별 Evidence

### Item 1 — §10.1 POST /api/likes/{trackId}: 409 Conflict 추가

- File: `docs/design/api-spec.md`
- Line pointer: 1305~1308
- Source: `docs/design/usecase/likes.md:27` — LIKE-001 "Track already in likes: 409 Conflict"
- Basis: `deliverables/agent/WI-20260307-ATS-023-evidence-pack.md` MINOR-001
- Added:
```json
{ "status": 409, "error": "Conflict", "errorCode": "TRACK_ALREADY_IN_LIKES", "message": "이미 좋아요한 트랙입니다." }
```
- Format reference: `docs/design/api-spec.md:436` (§3.1 PLAYLIST_LIMIT_EXCEEDED 패턴)

---

### Item 2 — §10.3 DELETE /api/likes/{trackId}: 404 Not Found 추가

- File: `docs/design/api-spec.md`
- Line pointer: 1340~1343
- Source: `docs/design/usecase/likes.md:74` — LIKE-003 "Track not in likes: 404"
- Basis: `deliverables/agent/WI-20260307-ATS-023-evidence-pack.md` MINOR-002
- Added:
```json
{ "status": 404, "error": "Not Found", "errorCode": "TRACK_NOT_IN_LIKES", "message": "좋아요 목록에 없는 트랙입니다." }
```
- Format reference: `docs/design/api-spec.md:1001` (§6.10 SUBSCRIPTION_NOT_FOUND 패턴)

---

### Item 3 — §11.1 POST /api/download-queue/{trackId}: 409 Conflict 추가

- File: `docs/design/api-spec.md`
- Line pointer: 1360~1363
- Source: `docs/design/usecase/download-queue.md:29` — DLQ-001 "Track already in queue: 409 Conflict"
- Basis: `deliverables/agent/WI-20260307-ATS-023-evidence-pack.md` MINOR-003
- Added:
```json
{ "status": 409, "error": "Conflict", "errorCode": "TRACK_ALREADY_IN_QUEUE", "message": "이미 다운로드 큐에 있는 트랙입니다." }
```
- Format reference: `docs/design/api-spec.md:436` (§3.1 PLAYLIST_LIMIT_EXCEEDED 패턴)

---

### Item 4 — §11.3 DELETE /api/download-queue/{trackId}: 404 Not Found 추가

- File: `docs/design/api-spec.md`
- Line pointer: 1395~1398
- Source: `docs/design/usecase/download-queue.md:79` — DLQ-003 "Track not in queue: 404"
- Basis: `deliverables/agent/WI-20260307-ATS-023-evidence-pack.md` MINOR-004
- Added:
```json
{ "status": 404, "error": "Not Found", "errorCode": "TRACK_NOT_IN_QUEUE", "message": "다운로드 큐에 없는 트랙입니다." }
```
- Format reference: `docs/design/api-spec.md:1001` (§6.10 SUBSCRIPTION_NOT_FOUND 패턴)

---

## Format Compliance

| Field | Standard | Applied |
|-------|----------|---------|
| `status` | HTTP status code (integer) | 409 / 404 |
| `error` | HTTP reason phrase | "Conflict" / "Not Found" |
| `errorCode` | Domain error code (SCREAMING_SNAKE_CASE) | TRACK_ALREADY_IN_LIKES / TRACK_NOT_IN_LIKES / TRACK_ALREADY_IN_QUEUE / TRACK_NOT_IN_QUEUE |
| `message` | User-facing message (Korean) | included |

Standard defined at: `docs/design/api-spec.md:92~104` (Common Error Response)

---

## Acceptance Criteria Check

- [x] §10.1에 409 Conflict 에러 응답 추가 (라인 1305~1308)
- [x] §10.3에 404 Not Found 에러 응답 추가 (라인 1340~1343)
- [x] §11.1에 409 Conflict 에러 응답 추가 (라인 1360~1363)
- [x] §11.3에 404 Not Found 에러 응답 추가 (라인 1395~1398)
- [x] 에러 응답 포맷이 기존 다른 섹션과 일치 (§3.1, §6.10 패턴 동일)
