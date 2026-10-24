package gov.byzjj.common;

/**
 * 业务异常。
 * 用于表达可预期的业务错误，由全局异常处理器转换为统一响应。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 创建业务异常。
     *
     * @param code    业务状态码，同时作为 HTTP 状态码
     * @param message 中文错误提示
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务状态码。
     *
     * @return 业务状态码
     */
    public int getCode() {
        return code;
    }
}
