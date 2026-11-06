package com.lingrui.quiz.controller;

import com.lingrui.quiz.common.PageBean;
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
 *
 * 第八周补上的四个能力，和用户那边的写法一一对应：
 * 分页（/page）、关键词查询（/search）、逻辑删除（GET /delete 与 DELETE /{id}）。
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** 全部题目（不分页），兼容早先的调用方式 */
    @GetMapping
    public Result<List<Question>> list(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category) {
        return Result.ok(questionService.list(keyword, category));
    }

    /** 分页查询：page 从 1 开始，默认 1 / 5；keyword 可选 */
    @GetMapping("/page")
    public Result<PageBean<Question>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(questionService.page(page, pageSize, keyword));
    }

    /** 关键词查询，不分页 */
    @GetMapping("/search")
    public Result<List<Question>> search(@RequestParam(required = false) String keyword) {
        return Result.ok(questionService.search(keyword));
    }

    /** 逻辑删除的 GET 写法，和课堂一致：GET /api/questions/delete?id=3 */
    @GetMapping("/delete")
    public Result<String> deleteLogic(@RequestParam Long id) {
        return Result.ok(questionService.deleteById(id) ? "题目已删除" : "题目不存在或已被删除");
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

    /** 逻辑删除的 RESTful 写法 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.deleteById(id);
        return Result.ok();
    }
}
