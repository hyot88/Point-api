package com.point.web.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryDto {

    private List<PointHistoryDetail> list;
    private int totalPage;                  // 총 페이지 수

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class PointHistoryDetail {
        private LocalDateTime registedDate;     // 등록 날짜
        private int changePoint;                // 변경된 포인트
    }
}
