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
import org.springframework.data.domain.PageRequest;
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
    // 회원별 포인트 합계 조회
    public PointDto getPoint(Long memNo) {
        LocalDateTime localDateTime = LocalDateTime.now();

        // 회원별 포인트 합계 조회
        Tuple tuple = jpaQueryFactory.select(point.memNo, point.earnedPoint.subtract(point.usedPoint).sum())
                .from(point)
                .where(point.createdDate.loe(localDateTime) // 유효기간(createdDate ~ expirationDate) 에 해당하는 포인트
                        .and(point.expirationDate.goe(localDateTime))
                        .and(point.memNo.eq(memNo))
                        .and(point.cancelTp.eq(0))  // 취소되지 않은 포인트
                        .and(point.earnedPoint.subtract(point.usedPoint).ne(0)))    // 모두 사용되지 않은 포인트
                .groupBy(point.memNo)
                .fetchOne();

        // 리턴값 PointDto 생성
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
    // 회원별 포인트 적립/사용 내역 조회
    public ApiResult getPointHistory(Long memNo, int page) {
        /**
         * page는 소스상에서는 0부터 시작하므로 1 차감
         * 한 페이지당 5개 내역을 출력하도록 설정
         */
        PageRequest pageRequest = PageRequest.of(page - 1, 5);

        // 회원별 포인트 적립/사용 내역 조회
        List<Tuple> listTuple = jpaQueryFactory.select(pointHistory.registedDate, pointHistory.changePoint.sum())
                .from(pointHistory)
                .where(pointHistory.memNo.eq(memNo))
                .groupBy(pointHistory.registedDate)     // 등록일 기준으로 그룹핑
                .orderBy(pointHistory.registedDate.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        // 레코드 총 개수
        long count = jpaQueryFactory.select(pointHistory.registedDate, pointHistory.changePoint.sum())
                .from(pointHistory)
                .where(pointHistory.memNo.eq(memNo))
                .groupBy(pointHistory.registedDate)
                .stream().count();

        // 리턴값 PointHistoryDto 생성
        List<PointHistoryDto.PointHistoryDetail> list = new ArrayList<>();
        PointHistoryDto pointHistoryDto = PointHistoryDto.builder()
                .totalPage((int) Math.ceil(count / 5.0))
                .list(list)
                .build();

        listTuple.forEach(tuple -> {
            if (tuple != null) {
                list.add(PointHistoryDto.PointHistoryDetail.builder().registedDate(tuple.get(pointHistory.registedDate))
                        .changePoint(tuple.get(1, Integer.class))
                        .build());
            }
        });

        return new ApiResult<>(pointHistoryDto);
    }

    // 회원별 포인트 적립
    @Transactional
    public ApiResult accumulatePoint(Long memNo, int earnedPoint) {
        Point point = pointRepository.save(Point.builder()
                .memNo(memNo)
                .earnedPoint(earnedPoint)   // 적립 포인트
                .usedPoint(0)               // 초기 사용 포인트
                .cancelTp(0)                // 취소 여부
                .build());

        /**
         * 포인트 만료일은 +1년 후, -1일로 세팅한다
         * ex) 2023-02-06 ~ 2024-02-05 23:59:59
         */
        point.setExpirationDate(point.getCreatedDate()
                .plusYears(1).minusDays(1)
                .withHour(23).withMinute(59).withSecond(59).withNano(0));

        // 회원별 포인트 적립
        pointHistoryRepository.save(PointHistory.builder()
                .memNo(memNo)
                .changePoint(earnedPoint)           // 변경 포인트
                .registedDate(LocalDateTime.now())  // 등록 날짜
                .point(point)
                .build());

        return new ApiResult<>(ResponseCode.COMM_S000);
    }

    // 회원별 포인트 사용
    @Transactional
    public ApiResult usePoint(Long memNo, int usePoint) {
        // 회원 포인트 합계를 조회
        PointDto pointDto = getPoint(memNo);
        // 포인트 그룹핑을 위한 등록 날짜 세팅
        LocalDateTime registedDate = LocalDateTime.now();

        // 회원의 총 포인트가 사용 포인트보다 크거나 같을 경우
        if (pointDto.getTotalPoint() >= usePoint) {
            LocalDateTime localDateTime = LocalDateTime.now();
            // 회원의 포인트 적립 내역을 조회
            List<Point> pointList = jpaQueryFactory.selectFrom(point)
                    .where(point.createdDate.loe(localDateTime)     // 유효기간(createdDate ~ expirationDate) 에 해당하는 포인트
                            .and(point.expirationDate.goe(localDateTime))
                            .and(point.memNo.eq(memNo))
                            .and(point.cancelTp.eq(0))      // 취소되지 않은 포인트
                            .and(point.earnedPoint.subtract(point.usedPoint).ne(0)))    // 모두 사용되지 않은 포인트
                    .orderBy(point.createdDate.desc())
                    .fetch();

            for (Point point : pointList) {
                // 적립 포인트가 사용된 포인트와 사용할 포인트의 합보다 크거나 같을 경우
                if (point.getUsedPoint() + usePoint <= point.getEarnedPoint()) {
                    // 사용된 포인트 변경
                    point.setUsedPoint(point.getUsedPoint() + usePoint);
                    // 히스토리 저장
                    pointHistoryRepository.save(PointHistory.builder()
                            .memNo(memNo)
                            .changePoint(-1 * usePoint)
                            .registedDate(registedDate)
                            .point(point)
                            .build());
                    break;
                } else {
                    // 해당 포인트에서 사용할 수 있는 포인트 계산 (minusPoint)
                    int minusPoint = point.getEarnedPoint() - point.getUsedPoint();
                    point.setUsedPoint(point.getEarnedPoint());
                    // 사용할 포인트(usePoint)를 minusPoint 만큼 차감
                    usePoint = usePoint - minusPoint;
                    // 히스토리 저장
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
            // 포인트 부족 오류 리턴
            return new ApiResult<>(ResponseCode.POINT_E000);
        }
    }

    // 회원별 포인트 사용취소 API 개발
    @Transactional
    public ApiResult cancelPoint(Long memNo, Long pointId) {
        // 포인트 정보 조회
        Point point = pointRepository.findByMemNoAndPointId(memNo, pointId);

        // 포인트 정보가 없다면, 조회 안됨 리턴
        if (point == null) {
            return new ApiResult<>(ResponseCode.POINT_E003);
        }

        // 사용된 포인트가 있다면, 취소 실패 리턴
        if (point.getUsedPoint() != 0) {
            return new ApiResult<>(ResponseCode.POINT_E001);
        }

        // 포인트가 이미 취소되었다면, 기취소 리턴
        if (point.getCancelTp() == 1) {
            return new ApiResult<>(ResponseCode.POINT_E002);
        }

        // 포인트 상태값을 취소로 변경
        point.setCancel();

        return new ApiResult<>(ResponseCode.COMM_S000);
    }
}
