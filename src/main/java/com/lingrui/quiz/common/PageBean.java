package com.lingrui.quiz.common;

import java.util.List;

/**
 * 分页返回结构（9-7 里叫 PageBean）。
 *
 * 前端要两样东西：一共多少条（算总页数）、当前页这几条。
 * 所以不管是用户列表还是别的表，这个壳子都能直接复用。
 *
 * 用 record：字段只读，正好符合"从数据库查出来就交给前端"的用途。
 */
public record PageBean<T>(long total, List<T> list) {

    public static <T> PageBean<T> of(long total, List<T> list) {
        return new PageBean<>(total, list);
    }
}
