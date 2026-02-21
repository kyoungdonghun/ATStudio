# WI-20260221-ATS-007 Summary

## 변경 요약

재생 기록(PlayHistory) CRUD API 3개 엔드포인트를 구현했습니다.

| 엔드포인트 | 설명 | 응답 |
|-----------|------|------|
| `POST /api/play-histories` | 재생 기록 저장 + play_count 원자적 +1 | 201 Created |
| `GET /api/play-histories?page=1&size=50` | 내 재생 목록 최신순 조회 (페이지네이션) | 200 OK |
| `DELETE /api/play-histories` | 선택 삭제 또는 전체 삭제 | 204 No Content |

## 핵심 구현 사항

- **play_count 동시성 안전**: `@Modifying @Query` JPQL UPDATE 사용 (entity setter 방식 금지 준수)
- **삭제 로직**: `historyIds` 빈 배열이면 전체 삭제, 값이 있으면 선택 삭제 (본인 기록만)
- **인증 필수**: SecurityConfig의 `/api/**` catch-all 규칙으로 인증 요구 확인 완료
- **DTO 표준 준수**: Java record + `@JsonInclude(NON_NULL)`, `@NotNull` 유효성 검증

## 생성/수정 파일 (7개)

- 신규 5개: Controller, Service, DTO 3종
- 수정 2개: TrackRepository (incrementPlayCount), PlayHistoryRepository (쿼리 메서드 3개)

## 리스크

- **없음**: 기존 코드 변경 최소 (Repository 인터페이스에 메서드 추가만), 기존 테스트에 영향 없음

## 빌드 검증

**수동 실행 필요**: `gradlew.bat build -x test`

## 후속 작업

- WI-010: PlayHistoryService 단위 테스트
- WI-011: PlayHistoryController 통합 테스트
