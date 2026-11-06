package com.lingrui.quiz.service;

import com.lingrui.quiz.common.PageBean;
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
 *
 * 结构和 QuestionService 的对家 UserService 保持一致：
 * 列表 / 分页 / 关键词查询 / 逻辑删除，两边方法名与参数含义都对齐，
 * 以后加"试卷""选项"之类的表照这个模板抄就行。
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
        return questionMapper.findById(id);
    }

    /** 逻辑删除：删到了返回 true，没这条 / 已删过返回 false */
    public boolean deleteById(Long id) {
        return questionMapper.logicDeleteById(id) > 0;
    }

    public List<Map<String, Object>> stats() {
        return questionMapper.countByCategory();
    }

    public int count() {
        return questionMapper.countAll();
    }

    /** 用户名那套的分页逻辑原样搬过来：起始行 = (页码 - 1) × 每页条数 */
    public PageBean<Question> page(int page, int pageSize, String keyword) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 5;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int start = (page - 1) * pageSize;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String kw = hasKeyword ? keyword.trim() : null;

        long total = hasKeyword ? questionMapper.countByKeyword(kw) : questionMapper.countAll();
        List<Question> rows = hasKeyword
                ? questionMapper.selectPageByKeyword(start, pageSize, kw)
                : questionMapper.selectPage(start, pageSize);

        return PageBean.of(total, rows);
    }

    /** 关键词查询，不分页 */
    public List<Question> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return questionMapper.findAll(null, null);
        }
        return questionMapper.findAll(keyword.trim(), null);
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
