package com.point.web;

import com.point.service.PointService;
import com.point.web.handler.ApiResult;
import com.point.web.handler.ResponseCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Api(tags = {"Point API"})
@RestController("/point")
public class PointApiController {

    private final PointService pointService;

    @GetMapping("")
    @ApiOperation(value = "회원별 포인트 합계 조회", response = ApiResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memNo", value = "회원번호", required = true, dataType = "string", paramType = "query")
    })
    public ApiResult getPoint(@RequestParam("memNo") String memNo) {
        // 파라미터 체크
        String regExp = "^[0-9]+$";

        if (memNo != null && !"".equals(memNo) && memNo.matches(regExp)) {
            return pointService.getPoint(Long.valueOf(memNo));
        } else {
            return new ApiResult<>(ResponseCode.COMM_E001);
        }
    }

    @GetMapping("/history")
    @ApiOperation(value = "회원별 포인트 적립/사용 내역 조회", response = ApiResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memNo", value = "회원번호", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "페이지", required = true, dataType = "int", paramType = "query", example = "0")
    })
    public ApiResult getPointHistory(@RequestParam("memNo") String memNo, @RequestParam("page") int page) {
        // 파라미터 체크
        String regExp = "^[0-9]+$";

        if (memNo != null && !"".equals(memNo) && memNo.matches(regExp) && page > 0) {
            return pointService.getPointHistory(Long.valueOf(memNo), page);
        } else {
            return new ApiResult<>(ResponseCode.COMM_E001);
        }
    }
}
