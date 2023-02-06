package com.point.web;

import com.point.service.PointService;
import com.point.web.handler.ApiResult;
import com.point.web.handler.ResponseCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Api(tags = {"Point API"})
@RequestMapping("/point")
@RestController
public class PointApiController {

    private final PointService pointService;
    private final String NUM_REGEXP = "^[0-9]+$";

    @GetMapping("/{memNo}")
    @ApiOperation(value = "회원별 포인트 합계 조회", response = ApiResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memNo", value = "회원번호", required = true, dataType = "string", paramType = "path")
    })
    public ApiResult getPoint(@PathVariable String memNo) {
        // 파라미터 체크
        if (memNo != null && !"".equals(memNo) && memNo.matches(NUM_REGEXP)) {
            return new ApiResult<>(pointService.getPoint(Long.valueOf(memNo)));
        } else {
            return new ApiResult<>(ResponseCode.COMM_E001);
        }
    }

    @GetMapping("/{memNo}/history/{page}")
    @ApiOperation(value = "회원별 포인트 적립/사용 내역 조회", response = ApiResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memNo", value = "회원번호", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "페이지", required = true, dataType = "int", paramType = "path", example = "0")
    })
    public ApiResult getPointHistory(@PathVariable String memNo, @PathVariable int page) {
        // 파라미터 체크
        if (memNo != null && !"".equals(memNo) && memNo.matches(NUM_REGEXP) && page > 0) {
            return pointService.getPointHistory(Long.valueOf(memNo), page);
        } else {
            return new ApiResult<>(ResponseCode.COMM_E001);
        }
    }

    @PostMapping("/{memNo}/{point}")
    @ApiOperation(value = "회원별 포인트 적립", response = ApiResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memNo", value = "회원번호", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "point", value = "적립 포인트", required = true, dataType = "int", paramType = "path", example = "0")
    })
    public ApiResult accumulatePoint(@PathVariable String memNo, @PathVariable int point) {
        // 파라미터 체크
        if (memNo != null && !"".equals(memNo) && memNo.matches(NUM_REGEXP) && point > 0) {
            return pointService.accumulatePoint(Long.valueOf(memNo), point);
        } else {
            return new ApiResult<>(ResponseCode.COMM_E001);
        }
    }

    @PutMapping("/{memNo}/{point}")
    @ApiOperation(value = "회원별 포인트 사용", response = ApiResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memNo", value = "회원번호", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "point", value = "사용 포인트", required = true, dataType = "int", paramType = "path", example = "0")
    })
    public ApiResult usePoint(@PathVariable String memNo, @PathVariable int point) {
        // 파라미터 체크
        if (memNo != null && !"".equals(memNo) && memNo.matches(NUM_REGEXP) && point > 0) {
            return pointService.usePoint(Long.valueOf(memNo), point);
        } else {
            return new ApiResult<>(ResponseCode.COMM_E001);
        }
    }

    @DeleteMapping("/{memNo}/{pointId}")
    @ApiOperation(value = "회원별 포인트 사용취소 API 개발", response = ApiResult.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memNo", value = "회원번호", required = true, dataType = "string", paramType = "path"),
            @ApiImplicitParam(name = "pointId", value = "포인트 테이블 ID", required = true, dataType = "long", paramType = "path", example = "0")
    })
    public ApiResult cancelPoint(@PathVariable String memNo, @PathVariable Long pointId) {
        // 파라미터 체크
        if (memNo != null && !"".equals(memNo) && memNo.matches(NUM_REGEXP) && pointId > 0) {
            return pointService.cancelPoint(Long.valueOf(memNo), pointId);
        } else {
            return new ApiResult<>(ResponseCode.COMM_E001);
        }
    }
}
