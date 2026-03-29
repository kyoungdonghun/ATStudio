# SR-C-01

## 대상

중복 리소스 생성 시 `DATA_INTEGRITY_VIOLATION` 오용 — 4개 서비스 파일

- `src/main/java/com/atstudio/atstudio/service/LikeService.java` (line 39)
- `src/main/java/com/atstudio/atstudio/service/AlbumLikeService.java` (line 42)
- `src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java` (line 39)
- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java` (line 121)

## 수정 내용

**현재 패턴 (4곳 동일):**
```java
if (repository.existsById(id)) {
    throw new BusinessException(BUSINESS_ERROR.DATA_INTEGRITY_VIOLATION);
}
```

**문제:**
`DATA_INTEGRITY_VIOLATION`은 `GlobalExceptionHandler` Fallback에서 JPA `DataIntegrityViolationException`을 잡기 위해 설계된 코드다 (개발자 메시지: "데이터 무결성 제약 위반 (DataIntegrityViolationException fallback)"). 개발자가 명시적으로 existsById 체크 후 throw하는 비즈니스 로직에서 이 코드를 사용하는 것은 의미론적으로 부적절하다.

**제안:**
```java
if (repository.existsById(id)) {
    throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
}
```

`RESOURCE_DUPLICATE` (409 CONFLICT, "이미 존재하는 데이터입니다.", "중복된 리소스를 생성하려고 했습니다.")가 의도를 정확하게 표현한다.

**영향:**
- HTTP 상태 코드는 동일 (409)하므로 API 계약 파괴 없음.
- 관련 테스트 파일 4곳도 함께 수정 필요:
  - `LikeServiceTest.java` (line 71)
  - `PlaylistServiceTest.java` (line 270)
  - `DownloadQueueServiceTest.java` (line 102)
  - AlbumLikeService 테스트 (해당 파일 확인 필요)
