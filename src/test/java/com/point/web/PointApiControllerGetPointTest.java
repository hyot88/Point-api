package com.point.web;

import com.point.domain.point.Point;
import com.point.domain.point.PointRepository;
import com.point.web.handler.ResponseCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원별 포인트 합계 조회 API 테스트 클래스
 */
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
public class PointApiControllerGetPointTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PointRepository pointRepository;

    @Test
    public void point_파라미터_체크() throws Exception {

        // memNo에 " " 값 전달
        mvc.perform(get("/point/ "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 영문
        mvc.perform(get("/point/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 특수문자
        mvc.perform(get("/point/∑♂"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // 정상 케이스
        mvc.perform(get("/point/1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())));
    }

    @Test
    @Transactional
    public void point_포인트_합계_조회() throws Exception {
        Long memNo = 1234L; // 테스트 회원번호
        LocalDateTime expirationDate = LocalDateTime.now().plusYears(1).minusDays(1)
                .withHour(23).withMinute(59).withSecond(59).withNano(0);    // 포인트 만료일

        // 유효기간 끝난 포인트 추가
        pointRepository.save(Point.builder()
                .memNo(memNo)
                .earnedPoint(15000)
                .usedPoint(2500)
                .cancelTp(0)
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build());

        // 다른 회원의 포인트 추가
        pointRepository.save(Point.builder()
                .memNo(5678L)
                .earnedPoint(7000)
                .usedPoint(5000)
                .cancelTp(0)
                .expirationDate(expirationDate)
                .build());

        // 취소된 포인트 추가
        pointRepository.save(Point.builder()
                .memNo(memNo)
                .earnedPoint(2500)
                .usedPoint(0)
                .cancelTp(1)
                .expirationDate(expirationDate)
                .build());

        // 다 사용한 포인트 추가
        pointRepository.save(Point.builder()
                .memNo(memNo)
                .earnedPoint(3000)
                .usedPoint(3000)
                .cancelTp(0)
                .expirationDate(expirationDate)
                .build());

        // 덜 사용한 포인트 추가
        pointRepository.save(Point.builder()
                .memNo(memNo)
                .earnedPoint(5000)
                .usedPoint(4000)
                .cancelTp(0)
                .expirationDate(expirationDate)
                .build());

        // 사용 안한 포인트 추가
        pointRepository.save(Point.builder()
                .memNo(memNo)
                .earnedPoint(2000)
                .usedPoint(0)
                .cancelTp(0)
                .expirationDate(expirationDate)
                .build());

        // 포인트 조회
        mvc.perform(get("/point/" + memNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())))
                .andExpect(jsonPath("$.data.memNo", is(memNo.intValue())))
                .andExpect(jsonPath("$.data.totalPoint", is(3000)));
    }
}
