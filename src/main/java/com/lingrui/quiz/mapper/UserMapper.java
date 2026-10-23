package com.lingrui.quiz.mapper;

import com.lingrui.quiz.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper。
 *
 * 这一套用注解写 SQL（@Select / @Insert），
 * 和 QuestionMapper 的 XML 写法形成对比——两种都是 MyBatis 的常规用法。
 */
public interface UserMapper {

    @Select("SELECT id, username, password_hash, email, created_at FROM t_user ORDER BY id")
    List<User> findAll();

    @Select("SELECT id, username, password_hash, email, created_at FROM t_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Insert("""
            INSERT INTO t_user (username, password_hash, email)
            VALUES (#{username}, #{passwordHash}, #{email})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT COUNT(*) FROM t_user")
    int count();
}
