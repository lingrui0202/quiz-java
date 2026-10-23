package com.lingrui.quiz.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * 用来验证"确实连上了 MySQL"的极简 Mapper。
 */
public interface SystemMapper {

    @Select("SELECT VERSION()")
    String version();

    @Select("SELECT DATABASE()")
    String database();

    @Select("SELECT NOW()")
    String now();
}
