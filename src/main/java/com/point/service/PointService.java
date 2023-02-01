package com.point.service;

import com.point.domain.point.PointHistory;
import com.point.web.dto.PointDto;
import com.point.web.dto.PointHistoryDto;
import com.point.web.handler.ApiResult;
import com.point.web.handler.ResponseCode;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.point.domain.point.QPoint.point;
import static com.point.domain.point.QPointHistory.pointHistory;

@Service
@RequiredArgsConstructor
public class PointService {

    private final JPAQueryFactory jpaQueryFactory;

    @SuppressWarnings("all")
    public ApiResult getPoint(Long memNo) {
        LocalDateTime localDateTime = LocalDateTime.now();

        /**
         * <범위 체크 사용 예시>
         * - x.goe(y); (x >= y)
         * - x.gt(y); (x > y)
         * - x.loe(y); (x <= y)
         * - x.lt(y); (x < y)
         */
        // 회원 포인트 조회
        Tuple tuple = jpaQueryFactory.select(point.memNo, point.availablePoint.subtract(point.usedPoint).sum())
                .from(point)
                .where(point.createdDate.loe(localDateTime)
                        .and(point.expirationDate.goe(localDateTime))
                        .and(point.memNo.eq(memNo))
                        .and(point.cancelTp.eq(0)))
                .groupBy(point.memNo)
                .fetchOne();

        if (tuple != null) {
            Long tempMemNo = tuple.get(point.memNo);
            int totalPoint = tuple.get(1, Integer.class);

            return new ApiResult<>(PointDto.builder()
                    .memNo(tempMemNo)
                    .totalPoint(totalPoint)
                    .build());
        } else {
            return new ApiResult<>(PointDto.builder()
                    .memNo(memNo)
                    .totalPoint(0)
                    .build());
        }
    }

    public ApiResult getPointHistory(Long memNo, int page) {
        List<PointHistoryDto> listPointHistoryDto = new ArrayList<>();
        List<PointHistory> listPointHistory = jpaQueryFactory.selectFrom(pointHistory)
                .where(point.memNo.eq(memNo))
                .fetch();

        listPointHistory.forEach(history -> {
            if (history != null) {
                listPointHistoryDto.add(PointHistoryDto.builder()
                        .createdDate(history.getCreatedDate())
                        .changePoint(history.getChangePoint())
                        .build());
            }
        });

        return new ApiResult<>(listPointHistoryDto);
    }

    public ApiResult accumulatePoint(Long memNo, int point) {
        return new ApiResult<>(ResponseCode.COMM_S000);
    }

    public ApiResult usePoint(Long memNo, int point) {
        return new ApiResult<>(ResponseCode.COMM_S000);
    }

    public ApiResult cancelPoint(Long memNo, Long pointId) {
        return new ApiResult<>(ResponseCode.COMM_S000);
    }
}
