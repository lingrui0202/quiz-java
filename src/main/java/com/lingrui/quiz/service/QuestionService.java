package com.lingrui.quiz.service;

import com.lingrui.quiz.entity.Question;
import com.lingrui.quiz.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 题目业务层。
 *
 * 注意这里没有任何 JDBC 代码——一句 SQL 都没手写，
 * 全部通过 QuestionMapper 接口 + XML 完成，这就是 MyBatis 的作用。
 */
@Service
public class QuestionService {

    private final QuestionMapper questionMapper;

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    public List<Question> list(String keyword, String category) {
        return questionMapper.findAll(keyword, category);
    }

    public Question get(Long id) {
        Question q = questionMapper.findById(id);
        if (q == null) {
            throw new IllegalArgumentException("题目不存在：id=" + id);
        }
        return q;
    }

    public Question create(Question question) {
        validate(question);
        questionMapper.insert(question);
        // 回查一次，把数据库生成的 created_at 带回去（insert 不返回默认值）
        return questionMapper.findById(question.getId());
    }

    public Question update(Long id, Question question) {
        question.setId(id);
        validate(question);
        if (questionMapper.update(question) == 0) {
            throw new IllegalArgumentException("题目不存在：id=" + id);
        }
        return question;
    }

    public void delete(Long id) {
        if (questionMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("题目不存在：id=" + id);
        }
    }

    public List<Map<String, Object>> stats() {
        return questionMapper.countByCategory();
    }

    public int count() {
        return questionMapper.count();
    }

    private void validate(Question q) {
        if (q.getCategory() == null || q.getCategory().isBlank()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        if (q.getContent() == null || q.getContent().isBlank()) {
            throw new IllegalArgumentException("题干不能为空");
        }
        if (q.getAnswer() == null || q.getAnswer() < 0 || q.getAnswer() > 3) {
            throw new IllegalArgumentException("answer 必须是 0-3");
        }
        if (q.getOptionA() == null || q.getOptionB() == null
                || q.getOptionC() == null || q.getOptionD() == null) {
            throw new IllegalArgumentException("四个选项都不能为空");
        }
    }
}
