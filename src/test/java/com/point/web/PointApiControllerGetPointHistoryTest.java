package com.point.web;

import com.point.web.handler.ResponseCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원별 포인트 적립/사용 내역 조회 API 테스트 클래스
 */
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
public class PointApiControllerGetPointHistoryTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void point_파라미터_체크() throws Exception {
        Long memNo = 1234L;

        // memNo에 " " 값 전달
        mvc.perform(get("/point/ /history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 영문
        mvc.perform(get("/point/test/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // memNo에 숫자 아닌 값 전달 - 특수문자
        mvc.perform(get("/point/∑♂/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // page에 " " 값 전달
        mvc.perform(get("/point/" + memNo + "/history/ "))
                .andExpect(status().isBadRequest());

        // page에 숫자 아닌 값 전달 - 영문
        mvc.perform(get("/point/" + memNo + "/history/test"))
                .andExpect(status().isBadRequest());

        // page에 숫자 아닌 값 전달 - 특수문자
        mvc.perform(get("/point/" + memNo + "/history/∑♂"))
                .andExpect(status().isBadRequest());

        // page에 1보다 작은 숫자 전달
        mvc.perform(get("/point/" + memNo + "/history/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_E001.getCode())));

        // 정상 케이스
        mvc.perform(get("/point/" + memNo + "/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())));
    }

    @Test
    @Transactional
    public void point_포인트_내역_조회() throws Exception {
        Long memNo = 1234L;

        // 포인트 2,000원씩 7번 적립 = 총 +14,000원
        for (int i = 0; i < 7; i++) {
            mvc.perform(post("/point/" + memNo + "/2000"));
        }

        // 포인트 3,000원씩 4번 사용 = 총 -12,000원
        for (int i = 0; i < 4; i++) {
            mvc.perform(put("/point/" + memNo + "/3000"));
        }

        // 포인트 내역 조회 - 1page
        mvc.perform(get("/point/" + memNo + "/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())))
                .andExpect(jsonPath("$.data.list", hasSize(5)))
                .andExpect(jsonPath("$.data.totalPage", is(3)));

        // 포인트 내역 조회 - 2page
        mvc.perform(get("/point/" + memNo + "/history/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())))
                .andExpect(jsonPath("$.data.list", hasSize(5)))
                .andExpect(jsonPath("$.data.totalPage", is(3)));

        // 포인트 내역 조회 - 3page
        mvc.perform(get("/point/" + memNo + "/history/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.totalPage", is(3)));

        // 포인트 내역 조회 - 4page (내역 없음)
        mvc.perform(get("/point/" + memNo + "/history/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(ResponseCode.COMM_S000.getCode())))
                .andExpect(jsonPath("$.data.list", hasSize(0)))
                .andExpect(jsonPath("$.data.totalPage", is(3)));
    }
}
