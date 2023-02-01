package com.point.domain.point;

import com.point.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
@ToString
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pointHisId;

    @Column(nullable = false)
    private Long memNo;

    @Column(nullable = false)
    private int changePoint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "point_id", nullable = false)
    private Point point;

    @Builder
    public PointHistory(Long memNo, int changePoint, Point point) {
        this.memNo = memNo;
        this.changePoint = changePoint;
        this.point = point;
    }
}
