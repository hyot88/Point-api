package com.point.domain.point;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    PointHistory findFirstByMemNoAndPointOrderByPointHisIdDesc(Long memNo, Point point); // 테스트용
}
