package com.lingrui.quiz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置。
 *
 * 前端 Vue dev server 和后端不同端口，浏览器直接调会被同源策略拦掉。
 * 两种解法都在：这里开后端 CORS，vue.config.js 里也配了 devServer.proxy。
 * 生产环境应该把 allowedOrigins 收窄到真实域名。
 *
 * 两套路径都放行：
 * - /api/**          本项目自己的 REST 接口
 * - /users、/register、/deleteById 等  课堂示例兼容接口（见 CourseCompatController）
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // 课堂示例兼容接口只有几个固定路径，逐个放行比用 /** 安全，
        // 不会顺手把静态资源之类也放开。
        for (String path : new String[]{"/users", "/findUser", "/register", "/deleteById",
                                  "/questions", "/findQuestion", "/addQuestion", "/delQuestion",
                                  "/getQuestion"}) {
            registry.addMapping(path)
                    .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                    .allowedMethods("GET", "POST", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }
}
