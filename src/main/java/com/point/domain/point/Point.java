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
public class Point extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pointId;

    @Column(nullable = false)
    private Long memNo;

    @Column(nullable = false)
    private int availablePoint;

    @Column(nullable = false)
    private int usedPoint;

    @Column(nullable = false)
    private int cancelTp;

    @Column
    private LocalDateTime expirationDate;

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setUsedPoint(int usedPoint) {
        this.usedPoint = usedPoint;
    }

    @Builder
    public Point(Long memNo, int availablePoint, int usedPoint, int cancelTp) {
        this.memNo = memNo;
        this.availablePoint = availablePoint;
        this.usedPoint = usedPoint;
        this.cancelTp = cancelTp;
    }
}
