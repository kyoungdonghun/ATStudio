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

    // ── Auth ──────────────────────────────────────────────────────────────────
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다.",
            "로그인 인증 실패."),

    PASSWORD_LOGIN_DISABLED(
            HttpStatus.FORBIDDEN,
            "현재 이 환경에서는 이메일 로그인과 회원가입이 비활성화되어 있습니다.",
            "Password login mode is disabled for this environment."),

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

    // ── Track / Tag ───────────────────────────────────────────────────────────
    TRACK_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "트랙 정보를 찾을 수 없습니다.",
            "trackId에 해당하는 Track이 존재하지 않습니다."),

    TAG_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "태그 정보를 찾을 수 없습니다.",
            "tagId에 해당하는 Tag가 존재하지 않습니다."),

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
