package com.atstudio.atstudio.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BUSINESS_ERROR {

    // ── General ──────────────────────────────────────────────────────────────
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "요청하신 정보를 찾을 수 없습니다.",
            "요청 리소스가 존재하지 않습니다."),

    RESOURCE_NOT_ACCESS(
            HttpStatus.FORBIDDEN,
            "해당 정보를 열람할 수 없습니다.",
            "리소스 접근에 대한 적절한 권한이 없습니다."),

    RESOURCE_DUPLICATE(
            HttpStatus.CONFLICT,
            "이미 존재하는 데이터입니다.",
            "중복된 리소스를 생성하려고 했습니다."),

    INVALID_ARGUMENT(
            HttpStatus.BAD_REQUEST,
            "입력값이 올바르지 않습니다. 다시 확인해주세요.",
            "입력값이 올바르지 않습니다."),

    INVALID_STATE_TRANSITION(
            HttpStatus.BAD_REQUEST,
            "유효하지 않은 상태 전이입니다.",
            "현재 상태에서 해당 상태로 전이할 수 없습니다."),

    INVALID_TYPE(
            HttpStatus.BAD_REQUEST,
            "잘못된 요청 형식입니다. 입력값의 타입을 확인해주세요.",
            "요청 파라미터의 타입이 일치하지 않습니다."),

    INVALID_VALID(
            HttpStatus.BAD_REQUEST,
            "입력값이 유효하지 않습니다. 필수 항목을 확인하거나 형식을 맞춰주세요.",
            "@Valid, @Validated 유효성 검사 실패."),

    INVALID_VALIDATED(
            HttpStatus.BAD_REQUEST,
            "입력값이 유효하지 않습니다. 필수 항목을 확인하거나 형식을 맞춰주세요.",
            "@ModelAttribute, @RequestParam 유효성 검사 실패."),

    IO_LARGE(
            HttpStatus.CONTENT_TOO_LARGE,
            "파일 크기가 너무 큽니다. 제한된 크기를 확인해주세요.",
            "업로드 파일 크기가 허용된 제한을 초과했습니다."),

    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "잘못된 요청입니다. 요청 방식을 확인해주세요.",
            "허용되지 않은 HTTP 메서드 요청입니다."),

    DATA_INTEGRITY_VIOLATION(
            HttpStatus.CONFLICT,
            "요청을 처리할 수 없습니다. 이미 존재하는 데이터이거나 참조 관계에 문제가 있습니다.",
            "데이터 무결성 제약 위반 (DataIntegrityViolationException fallback)."),

    // ── ATStudio Domain ───────────────────────────────────────────────────────
    NO_ACTIVE_SUBSCRIPTION(
            HttpStatus.FORBIDDEN,
            "구독이 필요한 서비스입니다.",
            "활성 구독이 없는 사용자의 구독자 전용 기능 접근."),

    DOWNLOAD_LIMIT_EXCEEDED(
            HttpStatus.FORBIDDEN,
            "오늘의 다운로드 한도를 초과했습니다.",
            "일일 다운로드 제한 초과."),

    NICKNAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "이미 사용 중인 닉네임입니다.",
            "닉네임 중복."),

    WHITELIST_CHANNEL_LIMIT_EXCEEDED(
            HttpStatus.FORBIDDEN,
            "채널 등록 한도를 초과했습니다.",
            "구독 플랜 최대 채널 수 초과."),

    COMPANY_CERTIFICATION_REQUIRED(
            HttpStatus.FORBIDDEN,
            "기업 인증 심사 승인 후 이용 가능합니다.",
            "기업회원 Company Certification 미승인 상태."),

    SUBSCRIPTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "구독 정보를 찾을 수 없습니다.",
            "활성 구독 레코드가 존재하지 않습니다."),

    SUBSCRIPTION_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "이미 활성 구독이 존재합니다.",
            "중복 구독 시도."),

    SUBSCRIPTION_USER_TYPE_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "회원 유형에 맞지 않는 구독 플랜입니다.",
            "개인/기업 회원 유형과 구독 플랜의 user_type이 불일치합니다."),

    PLAYLIST_LIMIT_EXCEEDED(
            HttpStatus.CONFLICT,
            "구독 플랜의 재생목록 한도를 초과했습니다.",
            "구독 플랜 maxPlaylists 초과 시도."),

    PAYMENT_ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "결제 정보를 찾을 수 없습니다.",
            "payment order가 존재하지 않습니다."),

    PAYMENT_ORDER_INVALID_STATE(
            HttpStatus.BAD_REQUEST,
            "현재 결제 상태에서는 처리할 수 없습니다.",
            "payment order 상태 전이가 유효하지 않습니다."),

    PAYMENT_ORDER_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "결제 시간이 만료되었습니다. 다시 시도해주세요.",
            "payment order가 만료되었습니다."),

    PAYMENT_ORDER_TERMINAL(
            HttpStatus.CONFLICT,
            "이전 결제 준비 시도가 종료되었습니다. 새 결제 시도를 시작해주세요.",
            "payment order is terminal and safe to replace with a fresh prepare attempt."),

    PAYMENT_AMOUNT_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "결제 금액이 일치하지 않습니다.",
            "클라이언트 결제 금액과 서버 결제 주문 금액이 일치하지 않습니다."),

    PAYMENT_PURPOSE_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "\uC694\uCCAD\uD55C \uACB0\uC81C \uBAA9\uC801\uC774 \uD604\uC7AC \uAD6C\uB3C5 \uC0C1\uD0DC\uC640 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.",
            "requested payment purpose does not match authoritative subscription state."),

    PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID(
            HttpStatus.BAD_REQUEST,
            "결제 준비 요청 키가 올바르지 않습니다.",
            "Idempotency-Key must be a canonical UUID v4 value."),

    PAYMENT_PREPARE_ATTEMPT_CONFLICT(
            HttpStatus.CONFLICT,
            "결제 준비 요청 정보가 기존 시도와 일치하지 않습니다.",
            "claimed prepare attempt changed its authoritative tuple."),

    SETTLEMENT_IMPORT_IDEMPOTENCY_KEY_INVALID(
            HttpStatus.BAD_REQUEST,
            "정식 소문자 UUIDv4 형식의 Idempotency-Key가 필요합니다.",
            "Settlement import Idempotency-Key is invalid."),

    SETTLEMENT_IMPORT_ATTEMPT_IN_PROGRESS(
            HttpStatus.CONFLICT,
            "정산 가져오기가 아직 처리 중입니다. 같은 Idempotency-Key로 복구하세요.",
            "Settlement import attempt is already processing."),

    SETTLEMENT_IMPORT_ATTEMPT_COMPLETED(
            HttpStatus.CONFLICT,
            "정산 가져오기가 완료되었습니다. 같은 Idempotency-Key로 결과를 복구하세요.",
            "Settlement import attempt is already completed."),

    SETTLEMENT_IMPORT_ATTEMPT_FAILED(
            HttpStatus.CONFLICT,
            "정산 가져오기에 실패했습니다. 새 작업을 시작하기 전에 복구 결과를 확인하세요.",
            "Settlement import attempt is already failed."),

    SETTLEMENT_IMPORT_ORCHESTRATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "정산 가져오기를 완료하지 못했습니다. 새 작업을 시작하기 전에 처리 결과를 복구하세요.",
            "Settlement import orchestration failed."),

    PAYMENT_CONFIRM_FAILED(
            HttpStatus.BAD_REQUEST,
            "결제 승인에 실패했습니다. 다시 시도해주세요.",
            "payment provider confirm 실패."),

    PAYMENT_PROVIDER_NOT_CONFIGURED(
            HttpStatus.BAD_REQUEST,
            "결제 설정이 준비되지 않았습니다. 관리자에게 문의해주세요.",
            "payment provider configuration is missing or invalid."),

    BILLING_AGREEMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "자동결제 등록 정보를 찾을 수 없습니다.",
            "billing agreement가 존재하지 않습니다."),

    BILLING_AGREEMENT_ALREADY_ACTIVE(
            HttpStatus.CONFLICT,
            "이미 자동결제가 등록되어 있습니다.",
            "활성 billing agreement가 이미 존재합니다."),

    BILLING_AGREEMENT_INVALID_STATE(
            HttpStatus.BAD_REQUEST,
            "현재 자동결제 상태에서는 처리할 수 없습니다.",
            "billing agreement 상태 전이가 유효하지 않습니다."),

    BILLING_AGREEMENT_REAUTH_REQUIRED(
            HttpStatus.CONFLICT,
            "자동결제 등록이 더 이상 유효하지 않습니다. 결제수단을 다시 등록해주세요.",
            "provider billing key is removed or invalid; billing agreement must be re-registered."),

    BILLING_AGREEMENT_CONFIRM_FAILED(
            HttpStatus.BAD_REQUEST,
            "자동결제 등록 또는 최초 결제에 실패했습니다. 다시 시도해주세요.",
            "billing agreement confirm 또는 initial recurring charge 실패."),

    BILLING_AGREEMENT_CANCEL_FAILED(
            HttpStatus.BAD_REQUEST,
            "자동결제 해지에 실패했습니다. 다시 시도해주세요.",
            "billing agreement cancel/delete 실패."),

    // ── Auth ──────────────────────────────────────────────────────────────────
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다.",
            "로그인 인증 실패."),

    PASSWORD_LOGIN_DISABLED(
            HttpStatus.FORBIDDEN,
            "현재 이 환경에서는 이메일 로그인과 회원가입이 비활성화되어 있습니다.",
            "Password login mode is disabled for this environment."),

    EMAIL_VERIFICATION_REQUIRED(
            HttpStatus.FORBIDDEN,
            "이메일 인증 후 로그인할 수 있습니다.",
            "Password session issuance requires an email-verified account."),

    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "인증이 만료되었습니다. 다시 로그인해주세요.",
            "JWT Access Token 만료."),

    REFRESH_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "세션이 만료되었습니다. 다시 로그인해주세요.",
            "Refresh Token 만료. 재로그인 필요."),

    REFRESH_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "세션이 만료되었습니다. 다시 로그인해주세요.",
            "Refresh Token이 유효하지 않거나 DB 불일치."),

    INVALID_TOKEN(
            HttpStatus.BAD_REQUEST,
            "유효하지 않은 인증 링크입니다.",
            "토큰이 존재하지 않거나 이미 사용됨."),

    SOCIAL_AUTH_FAILED(
            HttpStatus.UNAUTHORIZED,
            "소셜 로그인에 실패했습니다. 다시 시도해주세요.",
            "소셜 프로바이더 인증 코드 교환 실패."),

    PROFILE_ALREADY_COMPLETE(
            HttpStatus.BAD_REQUEST,
            "이미 프로필이 완성된 계정입니다.",
            "isProfileComplete=true인 사용자가 complete-profile 호출."),

    ACCOUNT_DEACTIVATED(
            HttpStatus.UNAUTHORIZED,
            "탈퇴한 계정입니다.",
            "isDeleted=true인 사용자 로그인 시도."),

    RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "짧은 시간에 너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.",
            "Public auth endpoint rate limit exceeded."),

    EMAIL_ALREADY_REGISTERED(
            HttpStatus.CONFLICT,
            "이미 가입된 이메일입니다.",
            "회원가입 시 이메일 중복."),

    PHONE_ALREADY_REGISTERED(
            HttpStatus.CONFLICT,
            "이미 등록된 전화번호입니다.",
            "회원가입/프로필 수정 시 전화번호 중복."),

    SELF_ADMIN_DEMOTION_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "자신의 관리자 권한은 직접 해제할 수 없습니다.",
            "관리자가 자신의 ADMIN 역할을 USER로 변경하려고 했습니다."),

    LAST_ADMIN_REQUIRED(
            HttpStatus.CONFLICT,
            "최소 한 명의 관리자가 남아 있어야 합니다.",
            "마지막 활성 관리자를 USER로 변경하려고 했습니다."),

    ADMIN_ROLE_REQUIRED(
            HttpStatus.FORBIDDEN,
            "관리자 권한이 변경되었습니다. 다시 확인해주세요.",
            "역할 변경 적용 직전 요청자의 현재 DB 역할이 ADMIN이 아닙니다."),

    ADMIN_OPERATION_REASON_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "역할 변경 사유를 입력해주세요.",
            "실제 관리자 역할 변경 요청에 운영 사유가 없습니다."),

    // ── Track / Tag ───────────────────────────────────────────────────────────
    TRACK_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "트랙 정보를 찾을 수 없습니다.",
            "trackId에 해당하는 Track이 존재하지 않습니다."),

    TRACK_THUMBNAIL_NOT_SQUARE(
            HttpStatus.BAD_REQUEST,
            "트랙 썸네일은 가로와 세로 길이가 같은 1:1 이미지여야 합니다.",
            "Decoded Track thumbnail dimensions must be exactly square."),

    AUDIO_ANALYSIS_FAILED(
            HttpStatus.BAD_REQUEST,
            "음원 파일을 분석할 수 없습니다. MP3 또는 WAV 파일을 확인해주세요.",
            "업로드 음원에서 유효한 duration과 waveform을 함께 추출하지 못했습니다."),

    TAG_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "태그 정보를 찾을 수 없습니다.",
            "tagId에 해당하는 Tag가 존재하지 않습니다."),

    TAG_NAME_INVALID(
            HttpStatus.BAD_REQUEST,
            "태그 이름 형식을 확인해주세요.",
            "원시 태그 이름이 200자를 초과했거나 정규화된 이름이 비어 있거나 50자를 초과하거나 허용 문자 집합을 위반했습니다."),

    TAG_NAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "이미 존재하는 태그 이름입니다.",
            "태그 이름 중복."),

    ALBUM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "앨범 정보를 찾을 수 없습니다.",
            "albumId에 해당하는 Album이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String clientMessage;
    private final String developerMessage;
}
