package gov.byzjj.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 相关配置。
 * 当前仅配置跨域访问，允许前端开发服务器直接调用后端接口。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注册跨域规则。
     * 开发阶段前端运行在 4000 端口，后端运行在 3000 端口，需要允许跨域请求。
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
