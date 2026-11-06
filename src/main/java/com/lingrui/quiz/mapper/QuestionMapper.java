package com.lingrui.quiz.mapper;

import com.lingrui.quiz.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 题目 Mapper。
 *
 * 这一套用 XML 写 SQL（resources/mapper/QuestionMapper.xml），
 * 对应课堂视频里"把 SQL 和 Java 分开"的做法。
 *
 * 和 UserMapper 一样，所有查询都只认 is_delete = 0 的行。
 */
public interface QuestionMapper {

    /** 全部题目，可按关键词 / 分类过滤（动态 SQL） */
    List<Question> findAll(@Param("keyword") String keyword,
                           @Param("category") String category);

    Question findById(@Param("id") Long id);

    int insert(Question question);

    int update(Question question);

    /** 逻辑删除：把 is_delete 置 1 */
    int logicDeleteById(@Param("id") Long id);

    /** 每个分类各几道题——用来演示 GROUP BY */
    List<Map<String, Object>> countByCategory();

    int countAll();

    int countByKeyword(@Param("keyword") String keyword);

    List<Question> selectPage(@Param("start") int start, @Param("pageSize") int pageSize);

    List<Question> selectPageByKeyword(@Param("start") int start,
                                       @Param("pageSize") int pageSize,
                                       @Param("keyword") String keyword);
}
