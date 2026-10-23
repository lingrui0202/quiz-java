package com.lingrui.quiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 第六次课作业：Spring Boot + MyBatis + MySQL。
 *
 * 启动后访问 http://localhost:8081/api/db/ping 可直接验证数据库连通性。
 */
@SpringBootApplication
@MapperScan("com.lingrui.quiz.mapper")
public class QuizJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizJavaApplication.class, args);
    }
}
