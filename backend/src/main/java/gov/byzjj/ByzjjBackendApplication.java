package gov.byzjj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 住建局综合业务管理平台后端启动类。
 * 当前阶段仅提供健康检查接口，用于验证前后端连通。
 */
@SpringBootApplication
public class ByzjjBackendApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ByzjjBackendApplication.class, args);
    }
}
