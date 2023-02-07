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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 포인트 취소 API 테스트 클래스
 */
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
public class PointApiControllerCancelPointTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PointRepository pointRepository;

    @Test
    @Transactional
    public void point_파라미터_체크() throws Exception {
        Long memNo = 1234L;

        // memNo에 " " 값 전달
        mvc.perform(delete("/point/ /1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 영문
        mvc.perform(delete("/point/test/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 특수문자
        mvc.perform(delete("/point/∑♂/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // pointId에 " " 값 전달
        mvc.perform(delete("/point/" + memNo + "/ "))
                .andExpect(status().isBadRequest());

        // point에 숫자 아닌 값 전달 - 영문
        mvc.perform(delete("/point/" + memNo + "/test"))
                .andExpect(status().isBadRequest());

        // point에 숫자 아닌 값 전달 - 특수문자
        mvc.perform(delete("/point/" + memNo + "/∑♂"))
                .andExpect(status().isBadRequest());

        // point에 1보다 작은 숫자 전달
        mvc.perform(delete("/point/" + memNo + "/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));
    }

    @Test
    @Transactional
    public void point_포인트_취소() throws Exception {
        Long memNo = 1234L;

        // 없는 ID로 포인트 취소
        mvc.perform(delete("/point/" + memNo + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.POINT_E003.getCode())));

        // 포인트 적립
        mvc.perform(post("/point/" + memNo + "/" + 2000));

        // 포인트 일부 사용
        mvc.perform(put("/point/" + memNo + "/" + 500));

        // 최근 적립한 포인트 정보 조회
        Point point = pointRepository.findFirstByMemNoOrderByPointIdDesc(memNo);

        // 일부 사용한 포인트를 취소
        mvc.perform(delete("/point/" + memNo + "/" + point.getPointId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.POINT_E001.getCode())));

        // 포인트 적립
        mvc.perform(post("/point/" + memNo + "/" + 2000));

        // 최근 적립한 포인트 정보 조회
        point = pointRepository.findFirstByMemNoOrderByPointIdDesc(memNo);

        // 사용안한 포인트를 취소
        mvc.perform(delete("/point/" + memNo + "/" + point.getPointId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())));

        // 취소한 포인트 정보 조회
        point = pointRepository.findFirstByMemNoOrderByPointIdDesc(memNo);
        assertThat(point.getCancelTp()).isEqualTo(1);
    }
}
