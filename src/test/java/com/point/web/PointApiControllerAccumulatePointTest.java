package com.point.web;

import com.point.domain.point.Point;
import com.point.domain.point.PointHistory;
import com.point.domain.point.PointHistoryRepository;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 회원별 포인트 적립 API 테스트 클래스
 */
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
public class PointApiControllerAccumulatePointTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Test
    @Transactional
    public void point_파라미터_체크() throws Exception {
        Long memNo = 1234L;

        // memNo에 " " 값 전달
        mvc.perform(post("/point/ /2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 영문
        mvc.perform(post("/point/test/2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 특수문자
        mvc.perform(post("/point/∑♂/2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // point에 " " 값 전달
        mvc.perform(post("/point/" + memNo + "/ "))
                .andExpect(status().isBadRequest());

        // point에 숫자 아닌 값 전달 - 영문
        mvc.perform(post("/point/" + memNo + "/test"))
                .andExpect(status().isBadRequest());

        // point에 숫자 아닌 값 전달 - 특수문자
        mvc.perform(post("/point/" + memNo + "/∑♂"))
                .andExpect(status().isBadRequest());

        // point에 1보다 작은 숫자 전달
        mvc.perform(post("/point/" + memNo + "/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // 정상 케이스
        mvc.perform(post("/point/" + memNo + "/2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())));
    }

    @Test
    @Transactional
    public void point_포인트_적립() throws Exception {
        Long memNo = 1234L;
        int amount = 2000;

        // 포인트 적립
        mvc.perform(post("/point/" + memNo + "/" + amount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())));

        // 적립한 포인트 정보 확인
        Point point = pointRepository.findByMemNoAndPointId(memNo, 1L);
        assertThat(point.getMemNo()).isEqualTo(memNo);
        assertThat(point.getEarnedPoint()).isEqualTo(amount);
        assertThat(point.getUsedPoint()).isEqualTo(0);
        assertThat(point.getCancelTp()).isEqualTo(0);
        assertThat(point.getCreatedDate().plusYears(1).minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0))
                .isEqualTo(point.getExpirationDate());

        // 적립한 포인트 히스토리 정보 확인
        PointHistory pointHistory = pointHistoryRepository.findFirstByMemNoAndPointOrderByPointHisIdDesc(memNo, point);
        assertThat(pointHistory.getMemNo()).isEqualTo(memNo);
        assertThat(pointHistory.getChangePoint()).isEqualTo(amount);
        assertThat(pointHistory.getPoint().getPointId()).isEqualTo(1L);
    }
}
