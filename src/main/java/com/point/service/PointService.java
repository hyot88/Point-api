package com.point.service;

import com.point.domain.point.Point;
import com.point.domain.point.PointHistory;
import com.point.domain.point.PointHistoryRepository;
import com.point.domain.point.PointRepository;
import com.point.web.dto.PointDto;
import com.point.web.dto.PointHistoryDto;
import com.point.web.handler.ApiResult;
import com.point.web.handler.ResponseCode;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.point.domain.point.QPoint.point;
import static com.point.domain.point.QPointHistory.pointHistory;

@Service
@RequiredArgsConstructor
public class PointService {

    private final JPAQueryFactory jpaQueryFactory;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @SuppressWarnings("all")
    public PointDto getPoint(Long memNo) {
        LocalDateTime localDateTime = LocalDateTime.now();

        // 회원 포인트 조회
        Tuple tuple = jpaQueryFactory.select(point.memNo, point.availablePoint.subtract(point.usedPoint).sum())
                .from(point)
                .where(point.createdDate.loe(localDateTime)
                        .and(point.expirationDate.goe(localDateTime))
                        .and(point.memNo.eq(memNo))
                        .and(point.cancelTp.eq(0))
                        .and(point.availablePoint.subtract(point.usedPoint).ne(0)))
                .groupBy(point.memNo)
                .fetchOne();

        if (tuple != null) {
            Long tempMemNo = tuple.get(point.memNo);
            int totalPoint = tuple.get(1, Integer.class);

            return PointDto.builder()
                    .memNo(tempMemNo)
                    .totalPoint(totalPoint)
                    .build();
        } else {
            return PointDto.builder()
                    .memNo(memNo)
                    .totalPoint(0)
                    .build();
        }
    }

    @SuppressWarnings("all")
    public ApiResult getPointHistory(Long memNo, int page) {
        List<PointHistoryDto> listPointHistoryDto = new ArrayList<>();
        List<Tuple> listTuple = jpaQueryFactory.select(pointHistory.registedDate, pointHistory.changePoint.sum())
                .from(pointHistory)
                .where(pointHistory.memNo.eq(memNo))
                .groupBy(pointHistory.registedDate)
                .orderBy(pointHistory.registedDate.desc())
                .fetch();

        listTuple.forEach(tuple -> {
            if (tuple != null) {
                listPointHistoryDto.add(PointHistoryDto.builder()
                        .registedDate(tuple.get(pointHistory.registedDate))
                        .changePoint(tuple.get(1, Integer.class))
                        .build());
            }
        });

        return new ApiResult<>(listPointHistoryDto);
    }

    @Transactional
    public ApiResult accumulatePoint(Long memNo, int availablePoint) {
        Point point = pointRepository.save(Point.builder()
                .memNo(memNo)
                .availablePoint(availablePoint)
                .usedPoint(0)
                .cancelTp(0)
                .build());

        point.setExpirationDate(point.getCreatedDate().plusYears(1).minusDays(1));

        pointHistoryRepository.save(PointHistory.builder()
                .memNo(memNo)
                .changePoint(availablePoint)
                .registedDate(LocalDateTime.now())
                .point(point)
                .build());

        return new ApiResult<>(ResponseCode.COMM_S000);
    }

    @Transactional
    public ApiResult usePoint(Long memNo, int usePoint) {
        PointDto pointDto = getPoint(memNo);
        LocalDateTime registedDate = LocalDateTime.now();

        if (pointDto.getTotalPoint() >= usePoint) {
            LocalDateTime localDateTime = LocalDateTime.now();
            List<Point> pointList = jpaQueryFactory.selectFrom(point)
                    .where(point.createdDate.loe(localDateTime)
                            .and(point.expirationDate.goe(localDateTime))
                            .and(point.memNo.eq(memNo))
                            .and(point.cancelTp.eq(0))
                            .and(point.availablePoint.subtract(point.usedPoint).ne(0)))
                    .orderBy(point.createdDate.desc())
                    .fetch();

            for (Point point : pointList) {
                if (point.getUsedPoint() + usePoint <= point.getAvailablePoint()) {
                    point.setUsedPoint(point.getUsedPoint() + usePoint);
                    pointHistoryRepository.save(PointHistory.builder()
                            .memNo(memNo)
                            .changePoint(-1 * usePoint)
                            .registedDate(registedDate)
                            .point(point)
                            .build());
                    break;
                } else {
                    int minusPoint = point.getAvailablePoint() - point.getUsedPoint();
                    point.setUsedPoint(point.getAvailablePoint());
                    usePoint = usePoint - minusPoint;
                    pointHistoryRepository.save(PointHistory.builder()
                            .memNo(memNo)
                            .changePoint(-1 * minusPoint)
                            .registedDate(registedDate)
                            .point(point)
                            .build());
                }
            }

            return new ApiResult<>(ResponseCode.COMM_S000);
        } else {
            return new ApiResult<>(ResponseCode.POINT_E000);
        }
    }

    @Transactional
    public ApiResult cancelPoint(Long memNo, Long pointId) {
        Point point = pointRepository.findByMemNoAndPointId(memNo, pointId);

        if (point.getUsedPoint() != 0) {
            return new ApiResult<>(ResponseCode.POINT_E001);
        }

        if (point.getCancelTp() == 1) {
            return new ApiResult<>(ResponseCode.POINT_E002);
        }

        point.setCancel();

        return new ApiResult<>(ResponseCode.COMM_S000);
    }
}
