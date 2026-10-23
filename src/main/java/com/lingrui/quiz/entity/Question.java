package com.lingrui.quiz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 题目表 t_question 的映射对象。
 *
 * 数据库里选项拆成 option_a ~ option_d 四列（方便 SQL 查询和教学演示），
 * 对外返回时额外拼一个 options 数组，前端用起来更顺手。
 */
@Data
public class Question {

    private Long id;

    /** 分类，例如 HTML BASICS / CSS BASICS / JAVASCRIPT */
    private String category;

    /** 题干 */
    private String content;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    /** 正确选项下标：0=A, 1=B, 2=C, 3=D */
    private Integer answer;

    private LocalDateTime createdAt;

    /**
     * 只读派生字段：把四列拼成数组。
     * access = READ_ONLY 表示只在序列化时出现，反序列化（POST/PUT 入参）时忽略它。
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<String> getOptions() {
        return Arrays.asList(optionA, optionB, optionC, optionD);
    }
}
