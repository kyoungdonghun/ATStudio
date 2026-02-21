# WI-20260221-ATS-008 Summary

## 변경 요약

좋아요(Likes)와 다운로드 큐(Download Queue) API 6개 엔드포인트를 구현했습니다.

### 구현된 엔드포인트

| 메서드 | 경로 | 응답 코드 | 설명 |
|--------|------|-----------|------|
| POST | `/api/likes/{trackId}` | 201 | 좋아요 추가 |
| GET | `/api/likes` | 200 | 내 좋아요 목록 조회 |
| DELETE | `/api/likes/{trackId}` | 204 | 좋아요 해제 |
| POST | `/api/download-queue/{trackId}` | 201 | 다운로드 큐 추가 |
| GET | `/api/download-queue` | 200 | 내 다운로드 큐 조회 |
| DELETE | `/api/download-queue/{trackId}` | 204 | 다운로드 큐 제거 |

### 에러 처리

- 중복 추가 시: 409 CONFLICT
- 존재하지 않는 트랙: 404 NOT FOUND
- 비인증 요청: 401 UNAUTHORIZED (Spring Security)
- 삭제 시 본인 소유가 아닌 경우: 404 NOT FOUND (다른 사용자의 데이터는 조회되지 않음)

### 응답 필드 (목록 조회)

trackId, title, bpm, tonality, thumbnail, createdAt

---

## 리스크

- **빌드 미검증:** 에이전트 세션에서 Bash 실행이 차단되어 `./gradlew build -x test`를 직접 실행하지 못했습니다. 사용자가 수동으로 실행하여 컴파일 오류 여부를 확인해야 합니다.

---

## 검증 절차

1. `./gradlew build -x test` 실행하여 컴파일 확인
2. `./gradlew test` 실행하여 기존 테스트 영향 없음 확인
3. 인증 후 각 엔드포인트 호출 테스트

---

## 생성/수정 파일 (8개)

**신규 생성 (6개):**
- `src/main/java/com/atstudio/atstudio/dto/like/LikeResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/downloadqueue/DownloadQueueResponse.java`
- `src/main/java/com/atstudio/atstudio/service/LikeService.java`
- `src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java`
- `src/main/java/com/atstudio/atstudio/controller/LikeController.java`
- `src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java`

**수정 (2개):**
- `src/main/java/com/atstudio/atstudio/repository/LikeRepository.java`
- `src/main/java/com/atstudio/atstudio/repository/DownloadQueueRepository.java`
