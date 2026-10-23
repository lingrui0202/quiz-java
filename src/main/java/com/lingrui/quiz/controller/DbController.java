package com.lingrui.quiz.controller;

import com.lingrui.quiz.common.Result;
import com.lingrui.quiz.mapper.SystemMapper;
import com.lingrui.quiz.service.QuestionService;
import com.lingrui.quiz.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 连通性自检。打开 http://localhost:8081/api/db/ping 一眼看出
 * Spring Boot / MyBatis / MySQL 这条链是不是通的。
 */
@RestController
@RequestMapping("/api/db")
public class DbController {

    private final SystemMapper systemMapper;
    private final QuestionService questionService;
    private final UserService userService;

    public DbController(SystemMapper systemMapper,
                        QuestionService questionService,
                        UserService userService) {
        this.systemMapper = systemMapper;
        this.questionService = questionService;
        this.userService = userService;
    }

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("mysqlVersion", systemMapper.version());
        info.put("database", systemMapper.database());
        info.put("dbTime", systemMapper.now());
        info.put("questionCount", questionService.count());
        info.put("userCount", userService.count());
        return Result.ok(info);
    }
}
