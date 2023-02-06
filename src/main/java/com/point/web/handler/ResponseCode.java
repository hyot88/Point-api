package com.point.web.handler;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // 공통
    COMM_S000(0, "OK"),
    COMM_E000(1000, "Internal Server Error"),
    COMM_E001(1001, "잘못된 파라미터 입니다."),

    // 포인트
    POINT_E000(2000, "포인트가 부족합니다."),
    POINT_E001(2001, "포인트가 사용되어 취소할 수 없습니다."),
    POINT_E002(2002, "이미 포인트가 취소되어있습니다."),
    POINT_E003(2003, "포인트가 조회되지 않습니다.");

    private int code;
    private String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
