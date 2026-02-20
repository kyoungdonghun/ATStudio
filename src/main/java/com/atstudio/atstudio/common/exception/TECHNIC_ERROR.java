package com.atstudio.atstudio.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TECHNIC_ERROR {

    IO_EXCEPTION(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            "네트워크 또는 입출력 처리 중 오류 발생."),

    CONNECT_TIMEOUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            "네트워크 통신 시간 초과."),

    CONNECT_EXCEPTION(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            "서버 연결 실패."),

    DATA_SQL_EXCEPTION(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            "SQL 실행 중 오류 발생."),

    DATA_ACCESS_EXCEPTION(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            "데이터 접근 중 오류 발생."),

    UNEXPECTED_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            "예기치 않은 시스템 오류 발생.");

    private final HttpStatus status;
    private final String clientMessage;
    private final String developerMessage;
}
