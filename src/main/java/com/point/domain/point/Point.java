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
    private Long pointId;       // 포인트 ID

    @Column(nullable = false)
    private Long memNo;         // 회원 번호

    @Column(nullable = false)
    private int earnedPoint;    // 적립 포인트

    @Column(nullable = false)
    private int usedPoint;      // 사용된 포인트

    @Column(nullable = false)
    private int cancelTp;       // 취소 여부 상태값 (0: 정상, 1: 취소)

    @Column
    private LocalDateTime expirationDate;   // 포인트 만료 일자

    // 포인트 만료 일자 세팅
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    // 사용된 포인트 세팅
    public void setUsedPoint(int usedPoint) {
        this.usedPoint = usedPoint;
    }

    // 취소 처리
    public void setCancel() {
        this.cancelTp = 1;
    }

    @Builder
    public Point(Long memNo, int earnedPoint, int usedPoint, int cancelTp, LocalDateTime expirationDate) {
        this.memNo = memNo;
        this.earnedPoint = earnedPoint;
        this.usedPoint = usedPoint;
        this.cancelTp = cancelTp;
        this.expirationDate = expirationDate;
    }
}
