[WI SUMMARY]
WI ID: WI-20260308-ATS-031
REQ: REQ-20260308-ATS-010
Status: Completed

## 수정 내역

| # | 섹션 | 파일:라인 | 추가 내용 |
|---|------|-----------|-----------|
| 1 | §10.1 POST /api/likes/{trackId} | `docs/design/api-spec.md:1305~1308` | 409 Conflict 에러 응답 추가 (`TRACK_ALREADY_IN_LIKES`) |
| 2 | §10.3 DELETE /api/likes/{trackId} | `docs/design/api-spec.md:1340~1343` | 404 Not Found 에러 응답 추가 (`TRACK_NOT_IN_LIKES`) |
| 3 | §11.1 POST /api/download-queue/{trackId} | `docs/design/api-spec.md:1360~1363` | 409 Conflict 에러 응답 추가 (`TRACK_ALREADY_IN_QUEUE`) |
| 4 | §11.3 DELETE /api/download-queue/{trackId} | `docs/design/api-spec.md:1395~1398` | 404 Not Found 에러 응답 추가 (`TRACK_NOT_IN_QUEUE`) |

## 범위

- 수정 대상: `docs/design/api-spec.md` (§10.1, §10.3, §11.1, §11.3)
- 성공 응답 변경 없음
- 다른 섹션 수정 없음
- 백엔드 코드 수정 없음
