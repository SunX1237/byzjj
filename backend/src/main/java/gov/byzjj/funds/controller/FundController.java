package gov.byzjj.funds.controller;

import gov.byzjj.common.ApiResponse;
import gov.byzjj.funds.dto.FundQueryResponse;
import gov.byzjj.funds.dto.FundTreeResponse;
import gov.byzjj.funds.service.FundQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 住宅专项维修资金查询接口。
 */
@RestController
@RequestMapping("/api/funds")
public class FundController {

    private final FundQueryService fundQueryService;

    /**
     * 构造查询控制器。
     *
     * @param fundQueryService 维修资金查询服务
     */
    public FundController(FundQueryService fundQueryService) {
        this.fundQueryService = fundQueryService;
    }

    /**
     * 查询项目、楼幢、单元、房屋层级树。
     *
     * @return 层级树
     */
    @GetMapping("/tree")
    public ApiResponse<FundTreeResponse> tree() {
        return ApiResponse.success(fundQueryService.getTree());
    }

    /**
     * 按项目、楼幢、单元或房屋查询资金信息。
     *
     * @param projectId  项目 ID，必填
     * @param buildingId 楼幢 ID，可选
     * @param unitId     单元 ID，可选
     * @param houseId    房屋 ID，可选
     * @return 资金汇总与明细
     */
    @GetMapping("/query")
    public ApiResponse<FundQueryResponse> query(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long houseId) {
        return ApiResponse.success(fundQueryService.query(projectId, buildingId, unitId, houseId));
    }
}
