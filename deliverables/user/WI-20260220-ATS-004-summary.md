# WI-20260220-ATS-004 Summary: Auth 기능 구현

## 변경 요약
Auth 기능 3개 엔드포인트 (로그인, 소셜 로그인, 토큰 재발급) 구현 완료.

### 생성된 파일 (10개)

| # | 파일 | 설명 |
|---|------|------|
| 1 | `src/main/java/.../dto/auth/LoginRequest.java` | 이메일+비밀번호 로그인 요청 DTO |
| 2 | `src/main/java/.../dto/auth/AuthResponse.java` | 토큰 응답 DTO (record) |
| 3 | `src/main/java/.../dto/auth/RefreshRequest.java` | 토큰 재발급 요청 DTO |
| 4 | `src/main/java/.../dto/auth/SocialLoginRequest.java` | 소셜 로그인 요청 DTO |
| 5 | `src/main/java/.../dto/auth/SocialAuthResponse.java` | 소셜 로그인 응답 DTO (record) |
| 6 | `src/main/java/.../service/auth/SocialUserInfo.java` | 소셜 유저 정보 record |
| 7 | `src/main/java/.../service/auth/AuthService.java` | 인증 비즈니스 로직 (로그인/토큰재발급) |
| 8 | `src/main/java/.../service/auth/OAuth2Service.java` | 소셜 로그인 OAuth2 플로우 |
| 9 | `src/main/java/.../controller/AuthController.java` | REST API 컨트롤러 |
| 10 | `src/main/java/.../config/AppConfig.java` | RestClient 빈 설정 |

### 수정된 파일 (1개)

| 파일 | 변경 내용 |
|------|----------|
| `src/main/resources/application.yml` | oauth2 섹션 추가 (Google/Kakao/Naver) |

## API 엔드포인트

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| POST | `/api/auth/login` | PUBLIC | 이메일+비밀번호 로그인 |
| POST | `/api/auth/social/{provider}` | PUBLIC | 소셜 로그인 (GOOGLE/KAKAO/NAVER) |
| POST | `/api/auth/refresh` | PUBLIC | Refresh Token 재발급 |

## 보안 규칙 적용

- SEC-07: Refresh Token DB 불일치 시 clear 후 거부
- SEC-08: 탈퇴 계정 (isDeleted=true) 토큰 재발급 차단
- SEC-09: 소셜 로그인 시 이메일 중복 계정 자동 연결 금지

## 리스크

- **LOW**: OAuth2 provider 연동은 실제 client-id/secret 없이는 테스트 불가. 환경변수 기본값은 빈 문자열.
- **NONE**: 기존 SecurityConfig의 permitAll 규칙에 이미 auth 엔드포인트가 등록되어 있음.

## 검증 방법

```bash
gradlew.bat compileJava
```

컴파일 성공 시 구현 완료. Bash 실행 권한 부재로 SE가 직접 검증하지 못함 -- 사용자 또는 QA가 검증 필요.
