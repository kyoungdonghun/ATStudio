# WI-20260221-ATS-009 요약

## 변경 내용

공지사항(Notice) CRUD API 5개 엔드포인트를 구현했습니다.

| 엔드포인트 | 권한 | 설명 |
|-----------|------|------|
| `POST /api/notices` | ADMIN | 공지사항 생성 (201) |
| `GET /api/notices?page=1&size=20` | PUBLIC | 공지사항 목록 조회 -- 고정 공지 우선, 최신순 정렬 |
| `GET /api/notices/{noticeId}` | PUBLIC | 공지사항 상세 조회 |
| `PUT /api/notices/{noticeId}` | ADMIN | 공지사항 수정 (부분 수정 지원) |
| `DELETE /api/notices/{noticeId}` | ADMIN | 공지사항 삭제 (물리 삭제) |

## 정렬 로직

목록 조회 시 `isPinned DESC, createdAt DESC` 순서로 정렬합니다. 고정된 공지(isPinned=true)가 항상 먼저 표시되고, 같은 고정 상태 내에서는 최신 순으로 정렬됩니다.

## 생성된 파일

- DTO 4개: `NoticeCreateRequest`, `NoticeUpdateRequest`, `NoticeListItemResponse`, `NoticeResponse`
- Service: `NoticeService` (5개 메서드)
- Controller: `NoticeController` (5개 엔드포인트)

## 위험 사항

- **낮음**: Notice 엔티티에 `update()` 도메인 메서드를 추가함. JPA dirty checking 기반 부분 수정 패턴으로, 기존 기능에 영향 없음.
- SecurityConfig는 이미 notices 관련 규칙이 설정되어 있어 변경하지 않음.

## 검증

- `gradlew.bat build -x test` 실행 필요 (Bash 도구 접근 제한으로 자동 실행 불가)
- 기존 테스트에 영향 없음 (새 파일 추가만 진행, 기존 파일 최소 수정)
