package com.point.web;

import com.point.domain.point.Point;
import com.point.domain.point.PointHistory;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    }
}
