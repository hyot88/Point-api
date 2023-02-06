package com.point.domain.point;

import com.point.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@ToString
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pointHisId;                // 포인트 히스토리 ID

    @Column(nullable = false)
    private Long memNo;                     // 회원 번호

    @Column(nullable = false)
    private int changePoint;                // 변경 포인트

    @Column(nullable = false)
    private LocalDateTime registedDate;     // 등록 날짜 (사용 내역 조회 시, 그룹핑 하기 위한 날짜)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    private Point point;

    @Builder
    public PointHistory(Long memNo, int changePoint, LocalDateTime registedDate, Point point) {
        this.memNo = memNo;
        this.changePoint = changePoint;
        this.registedDate = registedDate;
        this.point = point;
    }
}
