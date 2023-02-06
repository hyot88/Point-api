package com.point.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointDto {

    private Long memNo;     // 회원 번호
    private int totalPoint; // 회원의 총 포인트
}
