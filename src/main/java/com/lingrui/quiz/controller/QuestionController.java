package com.lingrui.quiz.controller;

import com.lingrui.quiz.common.Result;
import com.lingrui.quiz.entity.Question;
import com.lingrui.quiz.service.QuestionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 题目接口。这是"跑通 controller"那一环：
 * 浏览器 / 前端 → Controller → Service → Mapper → MySQL。
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** 列表，支持 ?keyword=&category= 过滤 */
    @GetMapping
    public Result<List<Question>> list(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category) {
        return Result.ok(questionService.list(keyword, category));
    }

    @GetMapping("/{id}")
    public Result<Question> detail(@PathVariable Long id) {
        return Result.ok(questionService.get(id));
    }

    /** 每个分类的题目数（GROUP BY 演示） */
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> stats() {
        return Result.ok(questionService.stats());
    }

    @PostMapping
    public Result<Question> create(@RequestBody Question question) {
        return Result.ok(questionService.create(question));
    }

    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @RequestBody Question question) {
        return Result.ok(questionService.update(id, question));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.ok();
    }
}
