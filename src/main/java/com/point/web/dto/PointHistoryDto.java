package com.point.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryDto {

    private LocalDateTime registedDate;     // 등록 날짜
    private int changePoint;                // 변경된 포인트
}
