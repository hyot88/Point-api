package com.point.web.handler;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // 공통
    COMM_S000(0, "OK"),
    COMM_E000(1000, "Internal Server Error"),
    COMM_E001(1001, "잘못된 파라미터 입니다."),

    // 포인트
    POINT_E000(2000, "포인트가 부족합니다.");

    private int code;
    private String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
