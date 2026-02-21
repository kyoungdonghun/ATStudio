# WI-20260221-ATS-006 구현 요약

## 변경 내용

Tag 수정/삭제, User Admin CRUD, Util 상태조회 3개 영역에서 총 **8개 엔드포인트**를 구현했습니다.

### 1. Tag 완성 (2개 엔드포인트)
- `PUT /api/tags/{tagId}` — 태그 이름/타입 수정 (ADMIN 전용)
- `DELETE /api/tags/{tagId}` — 태그 삭제 (ADMIN 전용)

### 2. User Admin (3개 엔드포인트)
- `GET /api/users` — 유저 목록 조회 (검색, 페이지네이션, ADMIN 전용)
- `GET /api/users/{userId}` — 유저 상세 조회 (ADMIN 전용)
- `PUT /api/users/{userId}` — 유저 role/isVerified 수정 (ADMIN 전용)

### 3. Util 상태조회 (3개 엔드포인트)
- `GET /api/utils/subscription-status` — 현재 구독 상태 확인 (로그인 필요)
- `GET /api/utils/download-count` — 오늘 다운로드 횟수/한도/잔여 확인 (로그인 필요)
- `GET /api/utils/user-type` — 유저타입 + 직업 확인 (로그인 필요)

## 주요 결정사항

1. Tag/User 엔티티에 최소한의 도메인 메서드 추가 (`Tag.update()`, `User.updateByAdmin()`) — setter 노출 없이 기존 패턴(`User.updateProfile` 등)과 일관되게 처리
2. 유저 검색은 JPQL `@Query`로 keyword(닉네임/이메일) + userType 필터 구현
3. 구독 없는 유저의 download-count는 `dailyLimit=0, remaining=0` 반환
4. 무제한 플랜(`downloadPerDay=-1`)은 remaining도 `-1`로 반환

## 주의 필요 사항

- **빌드 검증 필요**: 세션 중 빌드 도구 실행이 불가했습니다. `gradlew.bat build -x test` 수동 실행 필요
- **Tag 삭제 시 FK 주의**: `track_tags` 테이블의 FK CASCADE 설정이 schema.sql에 있는지 확인 필요. 없으면 트랙에 연결된 태그 삭제 시 오류 발생
- **테스트**: WI-010, WI-011에서 담당 (본 WI 범위 밖)

## 생성/수정된 파일 (15개)

**신규 (7개)**:
- `src/main/java/.../dto/user/UserListItemResponse.java`
- `src/main/java/.../dto/user/UserDetailResponse.java`
- `src/main/java/.../dto/user/UserAdminUpdateRequest.java`
- `src/main/java/.../dto/util/SubscriptionStatusResponse.java`
- `src/main/java/.../dto/util/DownloadCountResponse.java`
- `src/main/java/.../dto/util/UserTypeResponse.java`
- `src/main/java/.../service/UtilService.java`

**수정 (8개)**:
- `src/main/java/.../entity/Tag.java` (update 메서드 추가)
- `src/main/java/.../entity/User.java` (updateByAdmin 메서드 추가)
- `src/main/java/.../service/TagService.java` (updateTag, deleteTag 추가)
- `src/main/java/.../controller/TagController.java` (PUT, DELETE 추가)
- `src/main/java/.../repository/UserRepository.java` (searchUsers 쿼리 추가)
- `src/main/java/.../service/UserService.java` (getUsers, getUser, updateUserByAdmin 추가)
- `src/main/java/.../controller/UserController.java` (GET, GET/{id}, PUT/{id} 추가)
- `src/main/java/.../controller/UtilController.java` (3개 유틸 엔드포인트 추가)
