---
version: 1.1
last_updated: 2026-03-29
project: ATS
owner: SE
category: standard
status: stable
dependencies:
  - path: development-standards.md
    reason: Layer architecture reference
  - path: dto-standards.md
    reason: ExceptionResponseDTO definition
tier: 1
target_agents:
  - se
  - sa
  - cr
  - pg
  - qa
task_types:
  - implementation
  - review
  - security
---

# Exception Handling Standards

> **Purpose:** Define ATStudio's exception handling strategy to ensure consistent error responses, clear separation of client-caused vs system-caused errors, and effective debugging through dual-message patterns.

---

## 1. Exception Taxonomy

All exceptions are classified into two categories:

| Category | Cause | HTTP Range | Log Level |
|----------|-------|-----------|-----------|
| `BusinessException` | Client action (invalid input, not found, access denied) | 4xx | `WARN` |
| `TechnicException` | System internal (DB, network, I/O, unexpected) | 5xx | `ERROR` |

### 1.1 BusinessException

Thrown explicitly by developers when business rules are violated.

- Client caused the problem (bad input, missing resource, unauthorized access).
- Client message is safe to display; developer message aids debugging.
- Logged at `WARN` level — expected behavior, not a system failure.

### 1.2 TechnicException

Thrown for infrastructure or system-level failures.

- Client receives a generic safe message; no internal details exposed.
- Developer message contains the technical root cause.
- Logged at `ERROR` level — requires investigation.

---

## 2. ENUM-Based Error Codes

Each exception carries an ENUM value that defines HTTP status, client message, and developer message.

### 2.1 BusinessException Error Codes

| Code | HTTP Status | Client Message | Developer Message |
|------|------------|----------------|-------------------|
| `RESOURCE_NOT_FOUND` | 404 NOT_FOUND | 요청하신 정보를 찾을 수 없습니다. | 요청 리소스가 존재하지 않습니다. |
| `RESOURCE_NOT_ACCESS` | 403 FORBIDDEN | 해당 정보를 열람할 수 없습니다. | 리소스 접근에 대한 적절한 권한이 없습니다. |
| `RESOURCE_DUPLICATE` | 400 BAD_REQUEST | 이미 존재하는 데이터입니다. | 중복된 리소스를 생성하려고 했습니다. |
| `INVALID_ARGUMENT` | 400 BAD_REQUEST | 입력값이 올바르지 않습니다. 다시 확인해주세요. | 입력값이 올바르지 않습니다. |
| `INVALID_TYPE` | 400 BAD_REQUEST | 잘못된 요청 형식입니다. 입력값의 타입을 확인해주세요. | 요청 파라미터의 타입이 일치하지 않습니다. |
| `INVALID_VALID` | 400 BAD_REQUEST | 입력값이 유효하지 않습니다. 필수 항목을 확인하거나 형식을 맞춰주세요. | @Valid, @Validated 유효성 검사 실패. |
| `INVALID_VALIDATED` | 400 BAD_REQUEST | 입력값이 유효하지 않습니다. 필수 항목을 확인하거나 형식을 맞춰주세요. | @ModelAttribute, @RequestParam 유효성 검사 실패. |
| `UNEXPECTED_BIND` | 400 BAD_REQUEST | 입력값을 처리할 수 없습니다. 필드 형식이 맞는지 확인해주세요. | 바인딩 유효성 검사 실패. |
| `IO_LARGE` | 413 CONTENT_TOO_LARGE | 파일 크기가 너무 큽니다. 제한된 크기를 확인해주세요. | 업로드 파일 크기가 허용된 제한을 초과했습니다. |
| `METHOD_NOT_ALLOWED` | 405 METHOD_NOT_ALLOWED | 잘못된 요청입니다. 요청 방식을 확인해주세요. | 허용되지 않은 HTTP 메서드 요청입니다. |
| `DATA_INTEGRITY_VIOLATION` | 409 CONFLICT | 요청을 처리할 수 없습니다. 이미 존재하는 데이터이거나 참조 관계에 문제가 있습니다. | 데이터 무결성 제약 위반 (DataIntegrityViolationException fallback). |

### 2.2 TechnicException Error Codes

| Code | HTTP Status | Client Message | Developer Message |
|------|------------|----------------|-------------------|
| `IO_EXCEPTION` | 500 INTERNAL_SERVER_ERROR | 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요. | 네트워크 또는 입출력 처리 중 오류 발생. |
| `CONNECT_TIMEOUT` | 504 GATEWAY_TIMEOUT | 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요. | 네트워크 통신 시간 초과. |
| `CONNECT_EXCEPTION` | 500 INTERNAL_SERVER_ERROR | 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요. | 서버 연결 실패. |
| `DATA_SQL_EXCEPTION` | 500 INTERNAL_SERVER_ERROR | 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요. | SQL 실행 중 오류 발생. |
| `DATA_ACCESS_EXCEPTION` | 500 INTERNAL_SERVER_ERROR | 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요. | 데이터 접근 중 오류 발생. |
| `UNEXPECTED_ERROR` | 500 INTERNAL_SERVER_ERROR | 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요. | 예기치 않은 시스템 오류 발생. |

> **Note:** Client messages for TechnicException must never expose internal technical terms (e.g., "무결성 위반", "SQL", "데이터베이스"). Use generic safe messages.
> **Classification rule:** 4xx responses (client-caused errors) belong to BusinessException. 5xx responses (system-level failures) belong to TechnicException.

### 2.3 ATStudio Domain Error Codes (Active Enum: `BUSINESS_ERROR`)

The following codes are all defined in `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`.

**General / Domain:**

| Code | HTTP Status | Client Message | Developer Message |
|------|------------|----------------|-------------------|
| `INVALID_STATE_TRANSITION` | 400 BAD_REQUEST | 유효하지 않은 상태 전이입니다. | 현재 상태에서 해당 상태로 전이할 수 없습니다. |
| `UNAUTHORIZED_ACTION` | 401 UNAUTHORIZED | 인증이 필요합니다. 다시 로그인해주세요. | JWT 토큰 만료 또는 무효. |
| `PAYMENT_FAILED` | 400 BAD_REQUEST | 결제 처리에 실패했습니다. | 결제 게이트웨이 오류. |
| `FILE_FORMAT_INVALID` | 400 BAD_REQUEST | 지원하지 않는 파일 형식입니다. | 허용되지 않은 음악 파일 포맷. |
| `STORAGE_LIMIT_EXCEEDED` | 403 FORBIDDEN | 업로드 용량을 초과했습니다. | 사용자 스토리지 한도 초과. |
| `NO_ACTIVE_SUBSCRIPTION` | 403 FORBIDDEN | 구독이 필요한 서비스입니다. | 활성 구독이 없는 사용자의 구독자 전용 기능 접근. |
| `DOWNLOAD_LIMIT_EXCEEDED` | 403 FORBIDDEN | 오늘의 다운로드 한도를 초과했습니다. | 일일 다운로드 제한 초과. |
| `NICKNAME_DUPLICATED` | 409 CONFLICT | 이미 사용 중인 닉네임입니다. | 닉네임 중복. |
| `WHITELIST_CHANNEL_LIMIT_EXCEEDED` | 403 FORBIDDEN | 채널 등록 한도를 초과했습니다. | 구독 플랜 최대 채널 수 초과. |
| `COMPANY_CERTIFICATION_REQUIRED` | 403 FORBIDDEN | 기업 인증 심사 승인 후 이용 가능합니다. | 기업회원 Company Certification 미승인 상태. |
| `SUBSCRIPTION_NOT_FOUND` | 404 NOT_FOUND | 구독 정보를 찾을 수 없습니다. | 활성 구독 레코드가 존재하지 않습니다. |
| `SUBSCRIPTION_ALREADY_EXISTS` | 409 CONFLICT | 이미 활성 구독이 존재합니다. | 중복 구독 시도. |
| `BILLING_AGREEMENT_REAUTH_REQUIRED` | 409 CONFLICT | 자동결제 등록이 더 이상 유효하지 않습니다. 결제수단을 다시 등록해주세요. | Provider billing key is removed or invalid; billing agreement must be re-registered. |
| `PLAYLIST_LIMIT_EXCEEDED` | 409 CONFLICT | 플레이리스트는 최대 3개까지 생성할 수 있습니다. | 활성 플레이리스트 3개 초과 시도. |

**Auth:**

| Code | HTTP Status | Client Message | Developer Message |
|------|------------|----------------|-------------------|
| `INVALID_CREDENTIALS` | 401 UNAUTHORIZED | 이메일 또는 비밀번호가 올바르지 않습니다. | 로그인 인증 실패. |
| `TOKEN_EXPIRED` | 401 UNAUTHORIZED | 인증이 만료되었습니다. 다시 로그인해주세요. | JWT Access Token 만료. |
| `REFRESH_TOKEN_EXPIRED` | 401 UNAUTHORIZED | 세션이 만료되었습니다. 다시 로그인해주세요. | Refresh Token 만료. 재로그인 필요. |
| `REFRESH_TOKEN_INVALID` | 401 UNAUTHORIZED | 세션이 만료되었습니다. 다시 로그인해주세요. | Refresh Token이 유효하지 않거나 DB 불일치. |
| `INVALID_TOKEN` | 400 BAD_REQUEST | 유효하지 않은 인증 링크입니다. | 토큰이 존재하지 않거나 이미 사용됨. |
| `SOCIAL_AUTH_FAILED` | 401 UNAUTHORIZED | 소셜 로그인에 실패했습니다. 다시 시도해주세요. | 소셜 프로바이더 인증 코드 교환 실패. |
| `PROFILE_ALREADY_COMPLETE` | 400 BAD_REQUEST | 이미 프로필이 완성된 계정입니다. | isProfileComplete=true인 사용자가 complete-profile 호출. |
| `ACCOUNT_DEACTIVATED` | 401 UNAUTHORIZED | 탈퇴한 계정입니다. | isDeleted=true인 사용자 로그인 시도. |
| `EMAIL_ALREADY_REGISTERED` | 409 CONFLICT | 이미 가입된 이메일입니다. | 회원가입 시 이메일 중복. |

**Track / Tag / Album:**

| Code | HTTP Status | Client Message | Developer Message |
|------|------------|----------------|-------------------|
| `TRACK_NOT_FOUND` | 404 NOT_FOUND | 트랙 정보를 찾을 수 없습니다. | trackId에 해당하는 Track이 존재하지 않습니다. |
| `TAG_NOT_FOUND` | 404 NOT_FOUND | 태그 정보를 찾을 수 없습니다. | tagId에 해당하는 Tag가 존재하지 않습니다. |
| `TAG_NAME_DUPLICATED` | 409 CONFLICT | 이미 존재하는 태그 이름입니다. | 태그 이름 중복. |
| `ALBUM_NOT_FOUND` | 404 NOT_FOUND | 앨범 정보를 찾을 수 없습니다. | albumId에 해당하는 Album이 존재하지 않습니다. |

---

## 3. Dual-Message Pattern

Every exception carries two messages:

| Message | Audience | Content | Exposure |
|---------|----------|---------|----------|
| **Client Message** | End user | Safe, actionable, no technical terms | Returned in API response |
| **Developer Message** | Developer | Detailed root cause, technical context | Logged only, never returned |

```java
public class BusinessException extends RuntimeException {
    private final BUSINESS_ERROR errorCode;

    public BusinessException(BUSINESS_ERROR errorCode) {
        super(errorCode.getDeveloperMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() { return errorCode.getStatus(); }
    public String getClientMessage() { return errorCode.getClientMessage(); }
    public String getDeveloperMessage() { return errorCode.getDeveloperMessage(); }
}
```

---

## 4. GlobalExceptionHandler

Central exception handling via `@RestControllerAdvice`.

### 4.1 Direct Handlers (Explicitly Thrown)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponseDTO> handleBusinessException(BusinessException ex) {
        logger.warn("BusinessException(Throw): {}. Detail: {}",
            ex.getDeveloperMessage(), ex.toString(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getClientMessage());
    }

    @ExceptionHandler(TechnicException.class)
    public ResponseEntity<ExceptionResponseDTO> handleTechnicException(TechnicException ex) {
        logger.error("TechnicException(Throw): {}. Detail: {}",
            ex.getDeveloperMessage(), ex.toString(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getClientMessage());
    }
}
```

### 4.2 Fallback Handler (Unthrown Exceptions)

Catches all exceptions not explicitly thrown by developers. Maps them to Business or Technic categories.

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ExceptionResponseDTO> handleAllExceptions(Exception ex) {
    BusinessException businessEx = null;
    TechnicException technicEx = null;

    if (ex instanceof MethodArgumentNotValidException
            || ex instanceof ConstraintViolationException) {
        businessEx = new BusinessException(BUSINESS_ERROR.INVALID_VALID);
        logger.warn("BusinessException(Fallback): {}. Detail: {}",
            businessEx.getDeveloperMessage(), ex.toString(), ex);
    }
    // ... additional mappings ...
    else {
        technicEx = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);
        logger.error("TechnicException(Fallback): {}. Detail: {}",
            technicEx.getDeveloperMessage(), ex.toString(), ex);
    }

    if (businessEx != null) {
        return buildErrorResponse(businessEx.getStatus(), businessEx.getClientMessage());
    } else {
        return buildErrorResponse(technicEx.getStatus(), technicEx.getClientMessage());
    }
}
```

### 4.3 Scaling Strategy

When exception mappings exceed 10 entries, consider refactoring the `instanceof` chain to a `Map<Class, ErrorCode>` lookup:

```java
private static final Map<Class<? extends Exception>, ErrorCode> EXCEPTION_MAP = Map.of(
    MethodArgumentNotValidException.class, BUSINESS_ERROR.INVALID_VALID,
    ConstraintViolationException.class, BUSINESS_ERROR.INVALID_VALID,
    ConnectTimeoutException.class, TECHNIC_ERROR.CONNECT_TIMEOUT
);
```

---

## 5. Response Normalization

All error responses use a single `buildErrorResponse` method to guarantee consistent format.

`ExceptionResponseDTO` uses `@JsonInclude(NON_NULL)` — `errorCode` is omitted when null (generic errors).

```java
private ResponseEntity<ExceptionResponseDTO> buildErrorResponse(
        HttpStatus status, String clientMessage) {
    return buildErrorResponse(status, clientMessage, null);
}

private ResponseEntity<ExceptionResponseDTO> buildErrorResponse(
        HttpStatus status, String clientMessage, String errorCode) {
    ExceptionResponseDTO response = ExceptionResponseDTO.builder()
        .status(status.value())
        .error(status.getReasonPhrase())
        .errorCode(errorCode)
        .message(clientMessage)
        .build();
    return new ResponseEntity<>(response, status);
}
```

**Result — Generic error (no errorCode):**

```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "입력값이 올바르지 않습니다. 다시 확인해주세요."
}
```

**Result — Domain-specific error (with errorCode):**

```json
{
    "status": 403,
    "error": "Forbidden",
    "errorCode": "NO_ACTIVE_SUBSCRIPTION",
    "message": "구독이 필요한 서비스입니다."
}
```

---

## 6. Logging Strategy

| Exception Type | Log Level | Content |
|---------------|-----------|---------|
| BusinessException (thrown) | `WARN` | Developer message + exception detail |
| BusinessException (fallback) | `WARN` | Developer message + original exception |
| TechnicException (thrown) | `ERROR` | Developer message + exception detail |
| TechnicException (fallback) | `ERROR` | Developer message + original exception |

**Rationale:**
- `WARN` for business exceptions — expected behavior, does not require immediate investigation.
- `ERROR` for technical exceptions — may indicate infrastructure issues, higher priority.
