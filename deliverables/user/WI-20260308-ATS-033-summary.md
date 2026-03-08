[WI SUMMARY — User-Facing]
WI ID: WI-20260308-ATS-033
REQ: REQ-20260308-ATS-011
Status: Completed
Date: 2026-03-08
Agent: docops

---

## What Changed

`docs/standards/development-standards.md` §2A.4 에 "Lookup Data Exception" 규칙이 추가되었습니다.

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| §2A.4 Controller 템플릿 다음 | 바로 Service 템플릿 시작 | "Lookup Data Exception" 규칙 섹션 추가 |
| 문서 버전 | 2.0 | 2.1 |
| last_updated | 2026-01-15 | 2026-03-08 |

## 추가된 규칙 요약

- **제목**: Lookup Data Exception
- **허용 조건 (두 가지 모두 충족 필요)**:
  1. `docs/design/api-spec.md`에 raw array 응답으로 명세된 엔드포인트
  2. 페이지네이션 없는 읽기 전용 참조 데이터
- **현재 적용 엔드포인트**: `GET /api/tags` (api-spec §2.2) — `ResponseEntity<List<TagResponse>>` 반환
- **신규 엔드포인트 적용**: api-spec 명세 확인 후에만 허용 (추측 적용 금지)

## 영향 범위

- TagController의 기존 코드: 변경 없음 (문서 추가만)
- api-spec: 변경 없음
- 다른 컨트롤러의 동작: 변경 없음

## 승인 포인트

- 없음 (문서 추가만, 코드 변경 없음, REQ-011 승인 완료)
