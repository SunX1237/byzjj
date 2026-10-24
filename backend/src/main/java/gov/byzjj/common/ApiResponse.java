package gov.byzjj.common;

import java.time.LocalDateTime;

/**
 * 统一 API 响应结构。
 * 所有后端接口都使用该结构返回数据，便于前端统一处理。
 *
 * @param code      业务状态码，0 表示成功
 * @param message   提示信息
 * @param data      响应数据
 * @param timestamp 响应时间
 * @param <T>       响应数据类型
 */
public record ApiResponse<T>(int code, String message, T data, LocalDateTime timestamp) {

    /**
     * 创建成功响应。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 统一成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, LocalDateTime.now());
    }
}
