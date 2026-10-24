package gov.byzjj.controller;

import gov.byzjj.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统健康检查控制器。
 * 用于验证前端页面与后端服务是否连通，不承载业务功能。
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 查询后端服务健康状态。
     *
     * @return 包含服务状态、服务名称和当前时间的统一响应
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "byzjj-backend");
        data.put("timestamp", LocalDateTime.now());
        return ApiResponse.success(data);
    }
}
